package com.mobicule.mcollections.integration.collection;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.mail.SimpleMailMessage;

import com.mobicule.component.activitylogger.beans.CommunicationActivity;
import com.mobicule.component.activitylogger.beans.NotificationActivity;
import com.mobicule.component.activitylogger.beans.UserActivity;
import com.mobicule.component.activitylogger.commons.ActivityLoggerConstants;
import com.mobicule.component.activitylogger.service.CommunicationActivityService;
import com.mobicule.component.activitylogger.service.NotificationActivityService;
import com.mobicule.component.activitylogger.service.UserActivityService;
import com.mobicule.component.activitylogger.threads.CommunicationActivityAddition;
import com.mobicule.component.activitylogger.threads.CommunicationActivityStatusUpdate;
import com.mobicule.component.activitylogger.threads.NotificationActivityAddition;
import com.mobicule.component.activitylogger.threads.NotificationActivityStatusUpdate;
import com.mobicule.component.activitylogger.threads.UserActivityAddition;
import com.mobicule.component.activitylogger.threads.UserActivityStatusUpdate;
import com.mobicule.component.mapconversion.xml.MapToXML;
import com.mobicule.component.mapconversion.xml.XMLToMap;
import com.mobicule.mcollections.core.beans.Cheque;
import com.mobicule.mcollections.core.beans.Denomination;
import com.mobicule.mcollections.core.beans.RandomCollection;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.beans.TransactionType;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.EmailUtilities;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.RandomCollectionsExternalService;
import com.mobicule.mcollections.core.service.RandomCollectionsService;
import com.mobicule.mcollections.core.service.SystemUserService;
import com.mobicule.mcollections.integration.commons.ServerUtilities;
import com.mobicule.mcollections.integration.commons.XMLConstants;
import com.mobicule.mcollections.webservice.adapter.KotakCollectionWebserviceAdapter;

public class RandomCollectionsSubmissionService implements IRandomCollectionsSubmissionService
{
	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private RandomCollectionsService randomCollectionsService;

	@Autowired
	private ApplicationConfiguration<String, String> applicationConfiguration;

	@Autowired
	private KotakCollectionWebserviceAdapter webserviceAdapter;

	@Autowired
	private RandomCollectionsExternalService randomCollectionsExternalService;

	@Autowired
	private UserActivityService userActivityService;

	@Autowired
	private CommunicationActivityService communicationActivityService;

	@Autowired
	private NotificationActivityService notificationActivityService;

	@Autowired
	private SimpleMailMessage simpleMailMessageForCashPaymentCreditCard;

	@Autowired
	private SimpleMailMessage simpleMailMessageForCashPaymentLoan;

	@Autowired
	private SimpleMailMessage simpleMailMessageForChequePaymentCreditCard;

	@Autowired
	private SimpleMailMessage simpleMailMessageForChequePaymentLoan;

	@Autowired
	private SimpleMailMessage simpleMailMessageForDDPDC;

	@Autowired
	private EmailUtilities emailService;

	@Autowired
	private SystemUserService systemUserService;

	@Override
	public Message<String> execute(org.springframework.integration.Message<String> message) throws Throwable
	{
		log.info("<----------------- inside execute/RandomCollectionSubmission -------------------->");

		JSONObject responseJSON = new JSONObject();

		try
		{
			String requestSet = message.getPayload();

			String requestEntity = JSONPayloadExtractor.extract(requestSet, JsonConstants.ENTITY);
			String requestAction = JSONPayloadExtractor.extract(requestSet, JsonConstants.ACTION);

			JSONObject requestSystemUser = JSONPayloadExtractor.extractJSON(requestSet, JsonConstants.SYSTEM_USER);

			JSONObject requestData = JSONPayloadExtractor.extractJSON(requestSet, JsonConstants.DATA);

			SystemUser systemUser = ServerUtilities.extractSystemUser(requestSystemUser);

			String tempccapac = systemUser.getCcapac();

			systemUser.setCcapac(tempccapac);

			UserActivityAddition userActivityAddition = new UserActivityAddition(requestSet, userActivityService,
					ActivityLoggerConstants.DATABASE_MSSQL);

			new Thread(userActivityAddition).run();

			UserActivity userActivity = userActivityAddition.extractUserActivity();

			if (requestAction.equalsIgnoreCase(JsonConstants.ACTION_SUBMIT))
			{
				submitRandomCollections(responseJSON, requestData, systemUser, requestEntity, userActivity,
						communicationActivityService);
			}
			else if (requestAction.equalsIgnoreCase(JsonConstants.ACTION_SEARCH))
			{
				searchRandomCollection(responseJSON, requestData, requestEntity, userActivity,
						communicationActivityService, systemUser);
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
			responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
			responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_RANDOM_COLLECTIONS_FAILURE);
			responseJSON.put(JsonConstants.DATA, "");
		}

		return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
	}

	private void searchRandomCollection(JSONObject responseJSON, JSONObject requestData, String entity,
			UserActivity userActivity, CommunicationActivityService communicationActivityService, SystemUser systemUser)
			throws JSONException
	{
		if (entity.equalsIgnoreCase(JsonConstants.CREDIT_CARD))
		{
			fetchCreditCardRandomCollectionDetails(requestData, responseJSON, userActivity, systemUser.getUserTableId());
		}
		else
		{
			try
			{
				Map<String, Object> wrapperMap = new HashMap<String, Object>();
				long reqUID = createSearchRequestMap(requestData, entity, wrapperMap);

				
				Map<String, Object> webserviceResponseMap = new HashMap<String, Object>();
				try
				{
					webserviceResponseMap = webserviceAdapter.callWebserviceAndGetMap(wrapperMap,
							applicationConfiguration.getValue("WEB_SERVICE_URL_MCORE"), userActivity,
							communicationActivityService);

					log.info("----webserviceResponseMap-----" + webserviceResponseMap);


				}
				catch (Exception e)
				{

					log.error("---Exception while searching the randome collections ----", e);
				}

				if (null == webserviceResponseMap)
				{
					responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
					responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_RANDOM_COLLECTION_SEARCH_FAILURE);
					responseJSON.put(JsonConstants.DATA, "");
					UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
							(ActivityLoggerConstants.STATUS_FAILURE), userActivityService);
					new Thread(userActivityStatusUpdate).run();
				}
				else
				{
					parseResponseMap(responseJSON, webserviceResponseMap, entity, reqUID, systemUser.getUserTableId());

					UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
							(ActivityLoggerConstants.STATUS_SUCCESS), userActivityService);
					new Thread(userActivityStatusUpdate).run();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();

				UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
						(ActivityLoggerConstants.STATUS_ERROR), userActivityService);
				new Thread(userActivityStatusUpdate).run();
			}
		}
	}

	private void fetchCreditCardRandomCollectionDetails(JSONObject requestData, JSONObject responseJSON,
			UserActivity userActivity, long userId) throws JSONException
	{
		String depositionblocked = "No";

		String uniqueId = requestData.getString(JsonConstants.UNIQUE_ID);

		try
		{
			List<Map<String, Object>> searchResult = randomCollectionsExternalService
					.searchCreditCardRandomCollection(uniqueId);

			if (null != searchResult && !searchResult.isEmpty())
			{
				Map<String, Object> firstRecord = searchResult.get(0);

				JSONObject dataMap = new JSONObject();

				dataMap.put(JsonConstants.APPL, JsonConstants.CREDIT_CARD);
				dataMap.put(JsonConstants.APAC_NO, firstRecord.get("Active_Card_No"));
				dataMap.put(JsonConstants.CA, firstRecord.get("Crn"));

				dataMap.put(JsonConstants.OUTSTANDING, firstRecord.get("Peak_Balance"));
				dataMap.put(JsonConstants.TAD, firstRecord.get("TAD"));
				dataMap.put(JsonConstants.MAD, firstRecord.get("MAD"));
				dataMap.put(JsonConstants.ROLL_BACK_AMOUNT, firstRecord.get("Rollback_Amt"));

				dataMap.put(JsonConstants.DUE_DATE, firstRecord.get("PYMT_DUE_DATE"));
				dataMap.put(JsonConstants.BUCKET_AMOUNT_1, firstRecord.get("Bucket_1"));
				dataMap.put(JsonConstants.BUCKET_AMOUNT_2, firstRecord.get("Bucket_2"));

				dataMap.put(JsonConstants.NAME, firstRecord.get("Party_name"));
				dataMap.put(JsonConstants.CORRESPONDENCE_ADDRESS, firstRecord.get("Address"));
				dataMap.put(JsonConstants.CORRESPONDENCE_LOCATION, Constants.EMPTY_STRING);
				dataMap.put(JsonConstants.CORRESPONDENCE_PINCODE, Constants.EMPTY_STRING);
				dataMap.put(JsonConstants.MOBILE_NUMBER, firstRecord.get("MOBILE_PHONE"));
				dataMap.put(JsonConstants.EMAIL_ADDRESS, firstRecord.get("EMAIL"));
				dataMap.put(JsonConstants.LANDLINE_NUMBER, firstRecord.get("Land_Line_Nos"));

				try
				{

					/*
					 * if (systemUserService.checkDepositionLockedStatus(userId)) {
					 * 
					 * depositionblocked = "Yes";
					 * 
					 * }
					 */
				}

				catch (Exception e)
				{

					log.error("---Exception Occured while getting deposition lock status ---", e);

				}
				dataMap.put("isCollectionBlocked", depositionblocked);

				responseJSON.put(JsonConstants.DATA, dataMap);

				responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
				responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_RANDOM_COLLECTION_SEARCH_SUCCESS);

				UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
						(ActivityLoggerConstants.STATUS_SUCCESS), userActivityService);
				new Thread(userActivityStatusUpdate).run();

			}
			else
			{
				responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
				responseJSON.put(JsonConstants.MESSAGE, JsonConstants.NO_DATA_FOUND);
				responseJSON.put(JsonConstants.DATA, "");
				UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
						(ActivityLoggerConstants.STATUS_ERROR), userActivityService);
				new Thread(userActivityStatusUpdate).run();
			}
		}
		catch (Exception e)
		{

			e.printStackTrace();
			responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
			responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_GENERAL_FAILURE);
			responseJSON.put(JsonConstants.DATA, "");
			UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
					(ActivityLoggerConstants.STATUS_FAILURE), userActivityService);
			new Thread(userActivityStatusUpdate).run();
		}

	}

	private void parseResponseMap(JSONObject responseJSON, Map<String, Object> webserviceResponseMap, String entity,
			long reqUID, long userId) throws JSONException
	{
		String messageType = null;

		messageType = XMLConstants.MC002;

		Map responseMap = (Map) webserviceResponseMap.get(messageType);
		Map statusMap = (Map) responseMap.get(XMLConstants.RESPONSE_STATUS);

		if (statusMap.containsKey(XMLConstants.ERROR_CODE) && statusMap.containsKey(XMLConstants.ERROR_DESC))
		{
			if (((String) statusMap.get(XMLConstants.ERROR_CODE)).equalsIgnoreCase(XMLConstants.SUCCESS_CODE)
					&& (((String) statusMap.get(XMLConstants.ERROR_DESC))
							.equalsIgnoreCase(XMLConstants.SUCCESS_RESPONSE)))
			{
				Map<String, Object> headerMap = (Map<String, Object>) responseMap.get(XMLConstants.RESPONSE_HEADER);

				Long reqID = Long.valueOf(String.valueOf(headerMap.get(XMLConstants.UID)));

				if (reqID.equals(new Long(reqUID)))
				{
					Map dataMap = (Map) responseMap.get(XMLConstants.RESPONSE_DETAILS);

					createSearchResponseJson(dataMap, responseJSON, entity, userId);
				}
				else
				{
					responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
					responseJSON.put(JsonConstants.MESSAGE, (String) statusMap.get(XMLConstants.ERROR_DESC));
					responseJSON.put(JsonConstants.DATA, "");
				}
			}
			else
			{
				responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
				responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_RANDOM_COLLECTION_SEARCH_FAILURE);
				responseJSON.put(JsonConstants.DATA, "");
			}
		}
	}

	private void createSearchResponseJson(Map dataMap, JSONObject responseJSON, String entity, long userId)
			throws JSONException
	{
		JSONObject dataJSOn = new JSONObject();

		String depositionblocked = "No";

		if (dataMap.containsKey(XMLConstants.BUYER_ID)
		/*&& !dataMap.get(XMLConstants.BUYER_ID).toString().equalsIgnoreCase("0")*/)
		{

			dataJSOn.put(XMLConstants.BUYER_ID, dataMap.get(XMLConstants.BUYER_ID));

		}

		if (dataMap.containsKey(XMLConstants.BUYER_NAME))
		{

			dataJSOn.put(XMLConstants.BUYER_NAME, dataMap.get(XMLConstants.BUYER_NAME));

		}

		/*else
		{

			dataJSOn.put(JsonConstants.CA, dataMap.get(XMLConstants.PARTY_ID));

			dataJSOn.put(JsonConstants.NAME, dataMap.get(XMLConstants.PARTY_NAME));
		}*/

		if (dataMap.containsKey(XMLConstants.PARTY_ID))
		{
			dataJSOn.put(JsonConstants.CA, dataMap.get(XMLConstants.PARTY_ID));
		}
		if (dataMap.containsKey(XMLConstants.PARTY_NAME))
		{
			dataJSOn.put(JsonConstants.NAME, dataMap.get(XMLConstants.PARTY_NAME));
		}

		if (dataMap.containsKey(XMLConstants.REGISTRATION_NUMBER))
		{

			dataJSOn.put(XMLConstants.REGISTRATION_NUMBER, dataMap.get(XMLConstants.REGISTRATION_NUMBER));
		}

		if (dataMap.containsKey(XMLConstants.APPL))
		{
			dataJSOn.put(JsonConstants.APPL, dataMap.get(XMLConstants.APPL));
		}
		if (dataMap.containsKey(XMLConstants.APACNUM))
		{
			dataJSOn.put(JsonConstants.APAC_NO, dataMap.get(XMLConstants.APACNUM));
		}
		if (dataMap.containsKey(XMLConstants.EMI_DUEDATE))
		{
			dataJSOn.put(JsonConstants.DUE_DATE, dataMap.get(XMLConstants.EMI_DUEDATE));
		}

		if (dataMap.containsKey(XMLConstants.PENAL_AMNT))
		{
			dataJSOn.put(JsonConstants.LOAN_PENAL_AMOUNT, dataMap.get(XMLConstants.PENAL_AMNT));
		}
		if (dataMap.containsKey(XMLConstants.OVERDUE_AMNT))
		{
			dataJSOn.put(JsonConstants.OVERDUE, dataMap.get(XMLConstants.OVERDUE_AMNT));
		}
		if (dataMap.containsKey(XMLConstants.TOTAL_OUTSTANDING_AMNT))
		{
			dataJSOn.put(JsonConstants.OUTSTANDING, dataMap.get(XMLConstants.TOTAL_OUTSTANDING_AMNT));
		}
		if (dataMap.containsKey(XMLConstants.EMI_DUE_AMNT))
		{
			dataJSOn.put(JsonConstants.DUE_AMNT, dataMap.get(XMLConstants.EMI_DUE_AMNT));
		}
		if (dataMap.containsKey(XMLConstants.MOBILE))
		{
			dataJSOn.put(JsonConstants.MOBILE_NUMBER, dataMap.get(XMLConstants.MOBILE));
		}
		if (dataMap.containsKey(XMLConstants.EMAIL))
		{
			dataJSOn.put(JsonConstants.EMAIL_ADDRESS, dataMap.get(XMLConstants.EMAIL));
		}
		if (dataMap.containsKey(XMLConstants.PHONE_NUM))
		{
			dataJSOn.put(JsonConstants.LANDLINE_NUMBER, dataMap.get(XMLConstants.PHONE_NUM));
		}
		if (dataMap.containsKey(XMLConstants.ADDRESS))
		{
			dataJSOn.put(JsonConstants.CORRESPONDENCE_ADDRESS, dataMap.get(XMLConstants.ADDRESS));
		}

		if (dataMap.containsKey(XMLConstants.LOCATION))
		{
			dataJSOn.put(JsonConstants.CORRESPONDENCE_LOCATION, dataMap.get(XMLConstants.LOCATION));
		}
		if (dataMap.containsKey(XMLConstants.PINCODE))
		{
			dataJSOn.put(JsonConstants.CORRESPONDENCE_PINCODE, dataMap.get(XMLConstants.PINCODE));
		}

		try
		{

			/*
			 * if (systemUserService.checkDepositionLockedStatus(userId)) {
			 * 
			 * depositionblocked = "Yes";
			 * 
			 * }
			 */

		}

		catch (Exception e)
		{

			log.error("---Exception Occured while getting deposition lock status ---", e);

		}

		dataJSOn.put("isCollectionBlocked", depositionblocked);

		responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
		responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_RANDOM_COLLECTION_SEARCH_SUCCESS);
		responseJSON.put(JsonConstants.DATA, dataJSOn);

	}

	private long createSearchRequestMap(JSONObject requestData, String entity, Map<String, Object> wrapperMap)
			throws JSONException
	{
		Map<String, Object> requestMap = new HashMap<String, Object>();

		Map<String, Object> requestDetails = new HashMap<String, Object>();
		String uniqueId = requestData.getString(JsonConstants.UNIQUE_ID);

		requestDetails.put(XMLConstants.APPL, entity);
		requestDetails.put(XMLConstants.APACNUM, uniqueId);

		List<String> detailsList = new ArrayList<String>();
		detailsList.add(XMLConstants.APPL);
		detailsList.add(XMLConstants.APACNUM);

		StringBuilder requestXMLString = Utilities.generateXML(detailsList, requestDetails);

		long reqUID = System.currentTimeMillis();

		requestMap.put(XMLConstants.APP_CODE, XMLConstants.APP_CODE_VALUE);

		requestMap.put(XMLConstants.UID, reqUID);
		requestMap.put(XMLConstants.MESSAGE_TYPE, XMLConstants.MESSAGE_TYPE_MC002);
		requestMap.put(XMLConstants.MESSAGE_DATETIME, new Timestamp(reqUID));

		requestMap.put(XMLConstants.REQ_DETAILS, requestXMLString);

		List<String> headerList = new ArrayList<String>();
		headerList.add(XMLConstants.APP_CODE);
		headerList.add(XMLConstants.UID);

		headerList.add(XMLConstants.MESSAGE_TYPE);

		headerList.add(XMLConstants.MESSAGE_DATETIME);
		headerList.add(XMLConstants.REQ_DETAILS);

		StringBuilder requestBodyXMLString = Utilities.generateXML(headerList, requestMap);
		wrapperMap.put(XMLConstants.MC002, requestBodyXMLString);

		return reqUID;
	}

	private void submitRandomCollections(JSONObject responseJSON, JSONObject requestData, SystemUser systemUser,
			String requestEntity, UserActivity userActivity, CommunicationActivityService communicationActivityService)
			throws JSONException
	{
		String amountForSms = "";
		String receiptNumberForSms = "";
		String paymentTypeForSms = "";
		String mobileNumberForSms = "";
		String type = "";
		String feName = "";
		String apacCardNumber = "";

		RandomCollection randomCollection = extractRandomCollection(requestData, systemUser);

		UserActivityStatusUpdate userActivityStatusUpdate = null;

		if (randomCollection == null)
		{
			responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
			responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_GENERAL_FAILURE);
			responseJSON.put(JsonConstants.DATA, "");
			userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
					(ActivityLoggerConstants.STATUS_FAILURE), userActivityService);

			new Thread(userActivityStatusUpdate).run();
		}
		else
		{
			amountForSms = randomCollection.getAmount();
			receiptNumberForSms = System.currentTimeMillis() + "";
			paymentTypeForSms = randomCollection.getPaymentMode();
			mobileNumberForSms = randomCollection.getMobileNumber();
			feName = randomCollection.getName();
			apacCardNumber = randomCollection.getBusinessPartnerNumber();

			if (requestEntity.equalsIgnoreCase(JsonConstants.CREDIT_CARD))
			{
				type = "Card";
			}
			else
			{
				type = requestEntity;
			}

			if (randomCollectionsService.checkDuplicateRandomCollectionJSON(randomCollection))
			{
				KotakCollectionWebserviceAdapter kotakCollectionWebserviceAdapter = new KotakCollectionWebserviceAdapter();
				Map result = null;

				String resultString = "";

				if (randomCollection.getCollectionCode().equalsIgnoreCase("RTP"))
				{
					if (requestEntity.equalsIgnoreCase(JsonConstants.CREDIT_CARD))
					{
						Map<String, Object> randomCollectionCCMap = randomCollectionsService
								.generateRandomCollectionCCMap(randomCollection, systemUser);

						result = kotakCollectionWebserviceAdapter.callWebserviceAndGetMap(randomCollectionCCMap,
								applicationConfiguration.getValue("WEB_SERVICE_URL_MCARD"), userActivity,
								communicationActivityService);
					}
					else
					{
						Map<String, Object> randomCollectionLoanMap = randomCollectionsService
								.generateRandomCollectionLoanMap(randomCollection, systemUser);
						result = kotakCollectionWebserviceAdapter.callWebserviceAndGetMap(randomCollectionLoanMap,
								applicationConfiguration.getValue("WEB_SERVICE_URL_MCORE"), userActivity,
								communicationActivityService);
					}
					resultString = checkResult(result, requestEntity);
				}
				else
				{
					resultString = "01";
				}

				if (resultString.startsWith("$"))
				{
					String errorDesc = "";
					errorDesc = resultString.substring(1, resultString.length());
					randomCollection.setThirdPartyStatus("FAILURE");
					randomCollection.setThirdPartyStatusDesc(errorDesc);

					randomCollectionsService.addStuckRandomCases(randomCollection, systemUser);

					responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
					responseJSON.put(JsonConstants.MESSAGE, resultString.replace("$", ""));
					responseJSON.put(JsonConstants.DATA, "");
					userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
							(ActivityLoggerConstants.STATUS_ERROR), userActivityService);

					new Thread(userActivityStatusUpdate).run();
				}
				else
				{
					JSONObject jsonObject = new JSONObject();

					userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
							(ActivityLoggerConstants.STATUS_SUCCESS), userActivityService);

					new Thread(userActivityStatusUpdate).run();

					randomCollection.setTransactionId(resultString);

					randomCollection.setThirdPartyStatus("SUCCESS");
					randomCollection.setThirdPartyStatusDesc("Random Collection Submitted Successfully");

					boolean insertResult = randomCollectionsService.addRandomCollection(randomCollection);

					responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);

					responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_RANDOM_COLLECTION_SUBMITTED);

					responseJSON.put(JsonConstants.DATA, jsonObject);

					if (randomCollection.getCollectionCode().equalsIgnoreCase("RTP"))
					{
						callEmailService(randomCollection, systemUser, userActivity);

						try
						{
							receiptNumberForSms = randomCollection.getReceiptNumber();
							if (randomCollection.getMobileNumber() != null
									&& !randomCollection.getMobileNumber().equalsIgnoreCase(Constants.EMPTY_STRING))
							{
								callSMSDispatcher(amountForSms, receiptNumberForSms, paymentTypeForSms,
										mobileNumberForSms, type, apacCardNumber, userActivity);
							}

							if (randomCollection.getMobileNumberNew() != null
									&& !randomCollection.getMobileNumberNew().equalsIgnoreCase(Constants.EMPTY_STRING)
									&& !randomCollection.getMobileNumberNew().equalsIgnoreCase(mobileNumberForSms))
							{
								callSMSDispatcher(amountForSms, receiptNumberForSms, paymentTypeForSms,
										randomCollection.getMobileNumberNew(), type, apacCardNumber, userActivity);
							}
							if (systemUser.getMobileNumber() != null
									&& !systemUser.getMobileNumber().equalsIgnoreCase(Constants.EMPTY_STRING))
							{
								generateSMSDispatcherMapForFE(amountForSms, receiptNumberForSms, paymentTypeForSms,
										systemUser.getMobileNumber(), type, feName, userActivity);
							}
						}
						catch (Exception e)
						{
							log.info("There is some error occured while sending sms to customer." + e);
						}
					}
					else if (randomCollection.getCollectionCode().equalsIgnoreCase("PTP")) //Verbiage need to finalise
					{
						callEmailService(randomCollection, systemUser, userActivity);

						try
						{
							receiptNumberForSms = randomCollection.getReceiptNumber();
							if (randomCollection.getMobileNumber() != null
									&& !randomCollection.getMobileNumber().equalsIgnoreCase(Constants.EMPTY_STRING))
							{
								callSMSDispatcher(amountForSms, receiptNumberForSms, paymentTypeForSms,
										mobileNumberForSms, type, apacCardNumber, userActivity);
							}

							if (randomCollection.getMobileNumberNew() != null
									&& !randomCollection.getMobileNumberNew().equalsIgnoreCase(Constants.EMPTY_STRING)
									&& !randomCollection.getMobileNumberNew().equalsIgnoreCase(mobileNumberForSms))
							{
								callSMSDispatcher(amountForSms, receiptNumberForSms, paymentTypeForSms,
										randomCollection.getMobileNumberNew(), type, apacCardNumber, userActivity);
							}
							if (systemUser.getMobileNumber() != null
									&& !systemUser.getMobileNumber().equalsIgnoreCase(Constants.EMPTY_STRING))
							{
								generateSMSDispatcherMapForFE(amountForSms, receiptNumberForSms, paymentTypeForSms,
										systemUser.getMobileNumber(), type, feName, userActivity);
							}
						}
						catch (Exception e)
						{
							log.info("There is some error occured while sending sms to customer." + e);
						}
					}
					else if (randomCollection.getCollectionCode().equalsIgnoreCase("DRL")) //Verbiage need to finalise
					{
						callEmailService(randomCollection, systemUser, userActivity);

						try
						{
							receiptNumberForSms = randomCollection.getReceiptNumber();
							if (randomCollection.getMobileNumber() != null
									&& !randomCollection.getMobileNumber().equalsIgnoreCase(Constants.EMPTY_STRING))
							{
								callSMSDispatcher(amountForSms, receiptNumberForSms, paymentTypeForSms,
										mobileNumberForSms, type, apacCardNumber, userActivity);
							}

							if (randomCollection.getMobileNumberNew() != null
									&& !randomCollection.getMobileNumberNew().equalsIgnoreCase(Constants.EMPTY_STRING)
									&& !randomCollection.getMobileNumberNew().equalsIgnoreCase(mobileNumberForSms))
							{
								callSMSDispatcher(amountForSms, receiptNumberForSms, paymentTypeForSms,
										randomCollection.getMobileNumberNew(), type, apacCardNumber, userActivity);
							}
							if (systemUser.getMobileNumber() != null
									&& !systemUser.getMobileNumber().equalsIgnoreCase(Constants.EMPTY_STRING))
							{
								generateSMSDispatcherMapForFE(amountForSms, receiptNumberForSms, paymentTypeForSms,
										systemUser.getMobileNumber(), type, feName, userActivity);
							}
						}
						catch (Exception e)
						{
							log.info("There is some error occured while sending sms to customer." + e);
						}
					}
				}
			}
			else
			{
				responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
				responseJSON.put(JsonConstants.MESSAGE, "JSON DUPLICATED!!!");

				userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
						(ActivityLoggerConstants.STATUS_IGNORE), userActivityService);

				new Thread(userActivityStatusUpdate).run();
			}
		}
	}

	private void callSMSDispatcher(String amount, String receiptNumber, String paymentType, String mobileNumber,
			String type, String apacCardNumber, UserActivity userActivity)
	{
		String webserviceUrl = (String) applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");

		Map<String, Object> smsDispatcherMap = ServerUtilities.generateSMSDispatcherMap(amount, receiptNumber,
				paymentType, mobileNumber, type, apacCardNumber);

		StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true, new HashMap<String, String>());

		CommunicationActivityAddition communicationActivityAddition = new CommunicationActivityAddition(
				userActivity.getUserId(), userActivity.getDeviceId(),
				(ActivityLoggerConstants.TYPE_COMMUNICATION_WEB_SERVICE), webserviceUrl, xmlRequest.toString(),
				communicationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);

		new Thread(communicationActivityAddition).run();

		KotakCollectionWebserviceAdapter kotakCollectionWebserviceAdapter = new KotakCollectionWebserviceAdapter();

		String xmlResponse = kotakCollectionWebserviceAdapter.callWebserviceAndGetXmlString(xmlRequest.toString(),
				webserviceUrl);

		CommunicationActivity communicationActivity = communicationActivityAddition.extractCommunicationActivity();

		Map<String, Object> result = null;

		if (null != xmlResponse && !xmlResponse.equals(""))
		{
			communicationActivity.setResponse(xmlResponse);

			CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
					communicationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), communicationActivityService);

			new Thread(communicationActivityStatusUpdate).run();

			result = XMLToMap.convertXMLToMap(xmlResponse);
		}
		else
		{
			communicationActivity.setResponse(ActivityLoggerConstants.EMPTY_STRING);

			CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
					communicationActivity, (ActivityLoggerConstants.STATUS_FAILURE), communicationActivityService);

			new Thread(communicationActivityStatusUpdate).run();
		}
	}

	private void generateSMSDispatcherMapForFE(String amount, String receiptNumber, String paymentType,
			String mobileNumber, String type, String feName, UserActivity userActivity)
	{
		String webserviceUrl = (String) applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");

		Map<String, Object> smsDispatcherMap = ServerUtilities.generateSMSDispatcherMapForFE(amount, receiptNumber,
				paymentType, mobileNumber, type, feName);

		StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true, new HashMap<String, String>());

		CommunicationActivityAddition communicationActivityAddition = new CommunicationActivityAddition(
				userActivity.getUserId(), userActivity.getDeviceId(),
				(ActivityLoggerConstants.TYPE_COMMUNICATION_WEB_SERVICE), webserviceUrl, xmlRequest.toString(),
				communicationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);

		new Thread(communicationActivityAddition).run();

		KotakCollectionWebserviceAdapter kotakCollectionWebserviceAdapter = new KotakCollectionWebserviceAdapter();

		String xmlResponse = kotakCollectionWebserviceAdapter.callWebserviceAndGetXmlString(xmlRequest.toString(),
				webserviceUrl);

		CommunicationActivity communicationActivity = communicationActivityAddition.extractCommunicationActivity();

		Map<String, Object> result = null;

		if (null != xmlResponse && !xmlResponse.equals(""))
		{
			communicationActivity.setResponse(xmlResponse);

			CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
					communicationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), communicationActivityService);

			new Thread(communicationActivityStatusUpdate).run();

			result = XMLToMap.convertXMLToMap(xmlResponse);
		}
		else
		{
			communicationActivity.setResponse(ActivityLoggerConstants.EMPTY_STRING);

			CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
					communicationActivity, (ActivityLoggerConstants.STATUS_FAILURE), communicationActivityService);

			new Thread(communicationActivityStatusUpdate).run();
		}
	}

	private String checkResult(Map result, String requestEntity)
	{
		if (result != null)
		{
			Map mainMap = null;

			if (requestEntity.equalsIgnoreCase(JsonConstants.CREDIT_CARD))
			{
				mainMap = (Map) result.get(Constants.MC003);
			}
			else
			{
				mainMap = (Map) result.get(Constants.MC001);
			}

			Map statusMap = (Map) mainMap.get("response_status");
			Map responseDetails = (Map) mainMap.get("response_details");

			if (statusMap != null && statusMap.get(Constants.ERROR_CODE) != null
					&& statusMap.get(Constants.ERROR_CODE).toString().equalsIgnoreCase("0"))
			{
				return responseDetails.get(Constants.TRANS_ID).toString();
			}
			else if (statusMap != null && statusMap.get(Constants.ERROR_CODE) != null)
			{
				return "$".concat(statusMap.get(Constants.ERROR_DESC).toString());
			}
			else
			{
				return "$-1";
			}
		}
		else
		{
			return "$-1";
		}
	}

	private RandomCollection extractRandomCollection(JSONObject requestData, SystemUser systemUser)
	{
		RandomCollection randomCollection = new RandomCollection();

		try
		{
			randomCollection.setCcapac(systemUser.getCcapac());
			randomCollection.setRequestId(requestData.has(JsonConstants.REQUEST_ID) == true ? requestData
					.getString(JsonConstants.REQUEST_ID) : new Timestamp(System.currentTimeMillis()).toString());

			randomCollection.setContractAccountNumber(requestData.getString(JsonConstants.CONTRACT_ACCOUNT_NUMBER)); //party ID
			randomCollection.setBusinessPartnerNumber(requestData.getString(JsonConstants.UNIQUE_NUMBER)); //apac or card number
			randomCollection.setAppl(requestData.getString(JsonConstants.APPL));
			randomCollection.setName(requestData.getString(JsonConstants.NAME));
			randomCollection.setMobileNumber(requestData.getString(JsonConstants.MOBILE_NUMBER));
			randomCollection.setEmailAddress(requestData.getString(JsonConstants.EMAIL_ADDRESS));
			randomCollection.setCorrAddress(requestData.getString(JsonConstants.CORRESPONDENCE_ADDRESS));

			randomCollection.setCollectionCode(requestData.getString(JsonConstants.COLLECTION_CODE));

			randomCollection.setCorrLocation(requestData.getString(JsonConstants.CORRESPONDENCE_LOCATION));
			randomCollection.setCorrPin(requestData.getString(JsonConstants.CORRESPONDENCE_PINCODE));
			randomCollection.setSecAddress(requestData.getString(JsonConstants.SECOND_ADDRESS));
			randomCollection.setSecLocation(requestData.getString(JsonConstants.SECOND_LOCATION));
			randomCollection.setSecPin(requestData.getString(JsonConstants.SECOND_PINCODE));
			randomCollection.setReceiptNumber(requestData.getString(JsonConstants.RECEIPT_NUMBER));
			randomCollection.setDueDate(requestData.getString(JsonConstants.DUE_DATE));
			randomCollection
					.setOutstanding(Double.parseDouble(requestData.getString(JsonConstants.OUTSTANDING) == null
							|| requestData.getString(JsonConstants.OUTSTANDING)
									.equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0" : requestData
							.getString(JsonConstants.OUTSTANDING)));
			randomCollection.setPaymentMode(requestData.getString(JsonConstants.PAY_MODE));
			randomCollection.setAmount(requestData.getString(JsonConstants.AMOUNT));

			randomCollection.setEmailAddressNew(requestData.getString(JsonConstants.EMAIL_ADDRESS_NEW));
			randomCollection.setMobileNumberNew(requestData.getString(JsonConstants.MOBILE_NUMBER_NEW));

			List<Cheque> chequeList = new ArrayList<Cheque>();

			if ((randomCollection.getPaymentMode()).equals(Constants.PAYMENT_MODE_CASH))
			{
				JSONObject cash = (JSONObject) requestData.get("cash");

				randomCollection.setDocType(cash.getString(JsonConstants.DOCUMENT_TYPE));
				randomCollection.setDocRef(cash.getString(JsonConstants.DOCUMENT_REFERENCE));
				randomCollection.setInstDate(cash.getString(JsonConstants.INSTRUMENT_DATE));

				if (randomCollection.getDocType() != null && randomCollection.getDocType().equalsIgnoreCase("PAN"))
				{
					randomCollection.setPanNumber(cash.getString(JsonConstants.DOCUMENT_REFERENCE));
				}

				JSONArray denominationArray = cash.getJSONArray(JsonConstants.DENOMINATION);

				List<Denomination> denominationList = new ArrayList<Denomination>();

				for (int i = 0; i < (denominationArray.length()); i++)
				{
					JSONObject cashJSON = denominationArray.getJSONObject(i);

					Denomination denomination = new Denomination();

					denomination.setNote(cashJSON.get(JsonConstants.DENOMINATION_NOTE) == null
							|| cashJSON.get(JsonConstants.DENOMINATION_NOTE).toString()
									.equalsIgnoreCase(Constants.EMPTY_STRING) ? "0" : cashJSON.get(
							JsonConstants.DENOMINATION_NOTE).toString());
					denomination.setNoteCount(Integer.parseInt(cashJSON.get(JsonConstants.DENOMINATION_COUNT) == null
							|| cashJSON.get(JsonConstants.DENOMINATION_COUNT).toString()
									.equalsIgnoreCase(Constants.EMPTY_STRING) ? "0" : cashJSON.get(
							JsonConstants.DENOMINATION_COUNT).toString()));

					denominationList.add(denomination);
				}

				randomCollection.setDenomination(denominationList);

			}
			else if ((randomCollection.getPaymentMode()).equals(Constants.PAYMENT_MODE_CHEQUE)
					|| (randomCollection.getPaymentMode()).equals(Constants.PAYMENT_MODE_DRAFT)
					|| (randomCollection.getPaymentMode()).equals(Constants.PAYMENT_MODE_PDC))
			{

				randomCollection.setPanNumber(Constants.EMPTY_STRING);

				JSONArray chequeJSONArray = requestData.getJSONArray(JsonConstants.CHEQUE);

				for (int i = 0; i < (chequeJSONArray.length()); i++)
				{
					JSONObject chequeJSON = chequeJSONArray.getJSONObject(i);

					Cheque cheque = new Cheque();
					cheque.setDrawerAccountNumber(chequeJSON.get(JsonConstants.DRAWER_ACCOUNT_NUMBER) == null ? Constants.EMPTY_STRING
							: chequeJSON.getString(JsonConstants.DRAWER_ACCOUNT_NUMBER));
					cheque.setChequeNo(chequeJSON.getString(JsonConstants.CHEQUE_NUMBER));
					cheque.setMicrCode(chequeJSON.getString(JsonConstants.MICR));
					cheque.setChequeDate(chequeJSON.getString(JsonConstants.CHEQUE_DATE));
					cheque.setBranch(chequeJSON.getString(JsonConstants.BRANCH));
					cheque.setBankName(chequeJSON.getString(JsonConstants.RequestData.BANK_NAME));
					cheque.setAmount(Double.parseDouble(chequeJSON.getString(JsonConstants.AMOUNT) == null
							|| chequeJSON.getString(JsonConstants.AMOUNT).equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0"
							: chequeJSON.getString(JsonConstants.AMOUNT)));
					cheque.setDepositStatus(Constants.EMPTY_STRING);
					cheque.setDepositDate(Constants.EMPTY_STRING);
					cheque.setCreatedOn(Utilities.generateTimestamp(Constants.DATE_TIME_FORMAT));
					cheque.setCreatedBy(systemUser.getUserTableId());
					cheque.setModifiedOn(Utilities.generateTimestamp(Constants.DATE_TIME_FORMAT));
					cheque.setModifiedBy(systemUser.getUserTableId());
					cheque.setDeleteFlag(Constants.FLAG_FALSE);

					chequeList.add(cheque);
				}

				randomCollection.setChequeList(chequeList);
			}
			else if ((randomCollection.getPaymentMode()).equals(Constants.PAYMENT_MODE_CREDIT_CARD))
			{
				//code to set credit card details..currently N/A
			}

			randomCollection.setImage(extractImage((randomCollection.getBusinessPartnerNumber()),
					(requestData.getString(JsonConstants.IMAGE)), true));

			randomCollection.setSignature(extractImage((randomCollection.getBusinessPartnerNumber()),
					(requestData.getString(JsonConstants.SIGN)), false));

			if (((randomCollection.getImage()) == null) || ((randomCollection.getSignature()) == null))
			{
				log.info(" -------- Failure in saving Image or Signature / No Image -------- ");
			}

			randomCollection.setDeviceDate(requestData.getString(JsonConstants.DEVICE_DATE));
			randomCollection.setDeviceTime(requestData.getString(JsonConstants.DEVICE_TIME));

			randomCollection.setArea(requestData.getString(JsonConstants.AREA));
			randomCollection.setRemarks(requestData.getString(JsonConstants.REMARKS));

			randomCollection.setLatitude(requestData.getString(JsonConstants.LATITUDE));
			randomCollection.setLongitude(requestData.getString(JsonConstants.LONGITUDE));

			randomCollection.setSubmissionDate(Utilities.generateTimestamp(Constants.DATE_TIME_FORMAT));

			if (requestData.has(JsonConstants.LOAN))
			{

				JSONObject loanJSON = (JSONObject) requestData.getJSONObject(JsonConstants.LOAN);

				randomCollection
						.setOverdue(Double.parseDouble(loanJSON.get(JsonConstants.LOAN_OVERDUE) == null
								|| loanJSON.get(JsonConstants.LOAN_OVERDUE).toString()
										.equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0" : loanJSON.get(
								JsonConstants.LOAN_OVERDUE).toString()));
				randomCollection.setPenalAmt(Double.parseDouble(loanJSON.get(JsonConstants.LOAN_PENAL_AMOUNT) == null
						|| loanJSON.get(JsonConstants.LOAN_PENAL_AMOUNT).toString()
								.equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0" : loanJSON.get(
						JsonConstants.LOAN_PENAL_AMOUNT).toString()));
				randomCollection
						.setAppropriateAmount(Double.parseDouble(loanJSON.get(JsonConstants.APPR_AMOUNT) == null
								|| loanJSON.get(JsonConstants.APPR_AMOUNT).toString()
										.equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0" : loanJSON.get(
								JsonConstants.APPR_AMOUNT).toString()));

				JSONArray transType = loanJSON.getJSONArray(JsonConstants.LOAN_TRANS_TYPE);

				List<TransactionType> transTypeList = new ArrayList<TransactionType>();

				for (int i = 0; i < (transType.length()); i++)
				{
					JSONObject transJSON = transType.getJSONObject(i);
					TransactionType transactionType = new TransactionType();
					transactionType.setType(transJSON.get(JsonConstants.LOAN_TRANS_TYPE_TYPE).toString());
					transactionType.setAmount(transJSON.get(JsonConstants.LOAN_TRANS_TYPE_AMOUNT) == null
							|| transJSON.getString(JsonConstants.LOAN_TRANS_TYPE_AMOUNT).equalsIgnoreCase(
									Constants.EMPTY_STRING) ? "0.0" : transJSON.get(
							JsonConstants.LOAN_TRANS_TYPE_AMOUNT).toString());
					transTypeList.add(transactionType);
				}
				randomCollection.setTransType(transTypeList);

			}

			if (requestData.has(JsonConstants.CREDIT_CARD))
			{
				JSONObject ccJSON = (JSONObject) requestData.get(JsonConstants.CREDIT_CARD);
				randomCollection.setTad(Double.parseDouble(ccJSON.get(JsonConstants.TAD) == null
						|| ccJSON.get(JsonConstants.TAD).toString().equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0"
						: ccJSON.get(JsonConstants.TAD).toString()));
				randomCollection.setMad(Double.parseDouble(ccJSON.get(JsonConstants.MAD) == null
						|| ccJSON.get(JsonConstants.MAD).toString().equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0"
						: ccJSON.get(JsonConstants.MAD).toString()));
				randomCollection.setBuckAmt1(Double.parseDouble(ccJSON.get(JsonConstants.BUCKET_AMOUNT_1) == null
						|| ccJSON.get(JsonConstants.BUCKET_AMOUNT_1).toString()
								.equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0" : ccJSON.get(
						JsonConstants.BUCKET_AMOUNT_1).toString()));
				randomCollection.setBuckAmt2(Double.parseDouble(ccJSON.get(JsonConstants.BUCKET_AMOUNT_2) == null
						|| ccJSON.get(JsonConstants.BUCKET_AMOUNT_2).toString()
								.equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0" : ccJSON.get(
						JsonConstants.BUCKET_AMOUNT_2).toString()));
				randomCollection.setRollbackAmt(Double.parseDouble(ccJSON.get(JsonConstants.ROLL_BACK_AMOUNT) == null
						|| ccJSON.get(JsonConstants.ROLL_BACK_AMOUNT).toString()
								.equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0" : ccJSON.get(
						JsonConstants.ROLL_BACK_AMOUNT).toString()));
			}

			randomCollection.setCreatedOn(Utilities.generateTimestamp(Constants.DATE_TIME_FORMAT));
			randomCollection.setCreatedBy(systemUser.getUserTableId());
			randomCollection.setModifiedOn(Utilities.generateTimestamp(Constants.DATE_TIME_FORMAT));
			randomCollection.setModifiedBy(systemUser.getUserTableId());
			randomCollection.setDeleteFlag(Constants.FLAG_FALSE);
		}
		catch (Exception e)
		{
			e.printStackTrace();

			return null;
		}

		return randomCollection;
	}

	private String extractImage(String businessPartnerNumber, String image, boolean imageStatus)
	{
		String imagePath = Constants.EMPTY_STRING;

		String fileName = Constants.EMPTY_STRING;

		if (image.equals(Constants.EMPTY_STRING))
		{
			return imagePath;
		}

		if (imageStatus) // for Image
		{
			fileName = businessPartnerNumber + Constants.SYMBOL_UNDERSCORE
					+ (Utilities.generateDate(Constants.DATE_FORMAT));

			imagePath = Utilities.generateFilePath(
					(String.valueOf(applicationConfiguration.getValue(Constants.RC_IMAGE_FILE_PATH))), fileName);
		}
		else
		// for Signature
		{
			fileName = businessPartnerNumber + Constants.SYMBOL_UNDERSCORE
					+ (Utilities.generateDate(Constants.DATE_FORMAT));

			imagePath = Utilities.generateFilePath(
					(String.valueOf(applicationConfiguration.getValue(Constants.RC_SIGNATURE_IMAGE_FILE_PATH))),
					fileName);
		}

		if (Utilities.writeImage(imagePath, image))
		{
			return imagePath;
		}

		return (Constants.ERROR);
	}

	private void callEmailService(RandomCollection randomCollection, SystemUser systemUser, UserActivity userActivity)
	{
		try
		{
			if (randomCollection.getEmailAddress().equals(Constants.EMPTY_STRING)
					&& randomCollection.getEmailAddressNew().equals(Constants.EMPTY_STRING))
			{
				log.info(" -------- No Email Address found for Random Collection -------- ");
			}
			else
			{
				String payMode = randomCollection.getPaymentMode();

				if (payMode.equals(Constants.PAYMENT_MODE_CASH))
				{
					sendEmailForCashPayment(randomCollection, systemUser, userActivity);
				}

				if (payMode.equals(Constants.PAYMENT_MODE_CHEQUE))
				{
					sendEmailForChequePayment(randomCollection, systemUser, userActivity);
				}

				if (payMode.equalsIgnoreCase(Constants.PAYMENT_MODE_PDC)
						|| payMode.equalsIgnoreCase(Constants.PAYMENT_MODE_DRAFT))
				{
					sendEmailForDDPDC(randomCollection, systemUser, userActivity);
				}
			}
		}
		catch (Exception e)
		{
			log.info("-------Error Occured in sending Email---------", e);
		}
	}

	private String getFullFormApplType(String appl)
	{
		if (appl.equalsIgnoreCase(Constants.APPL_CARD))
		{
			return "Credit Card";
		}
		else if (appl.equalsIgnoreCase("SPLN"))
		{
			return "Salaried Personal Loans-New";
		}
		else if (appl.equalsIgnoreCase("RAR"))
		{
			return "Retail Asset Reconstruction";
		}
		else if (appl.equalsIgnoreCase("CV"))
		{
			return "Commercial Vehicles";
		}
		else if (appl.equalsIgnoreCase("HF"))
		{
			return "Home Finance";
		}
		else if (appl.equalsIgnoreCase("CSG"))
		{
			return "Personal Finance";
		}
		else if (appl.equalsIgnoreCase("SPL"))
		{
			return "Salaried Personal Loans";
		}
		else if (appl.equalsIgnoreCase("SA"))
		{
			return "UNNATI [SARAL]";
		}
		else if (appl.equalsIgnoreCase("TFE"))
		{
			return "Tractor and Farm Equipment Loans";
		}
		else if (appl.equalsIgnoreCase("CE"))
		{
			return "Construction Equipment";
		}
		else if (appl.equalsIgnoreCase("LAP"))
		{
			return "Loan Against Property";
		}
		else if (appl.equalsIgnoreCase("SBG"))
		{
			return "Strategic Business Group";
		}
		else if (appl.equalsIgnoreCase("GLN"))
		{
			return "Gold Loan";
		}
		else if (appl.equalsIgnoreCase("LCV"))
		{
			return "Light Commercial Vehicles";
		}
		else if (appl.equalsIgnoreCase("RHB"))
		{
			return "Rural Housing Business";
		}
		else if (appl.equalsIgnoreCase("RARF"))
		{
			return "Retail ARD Funding";
		}
		else if (appl.equalsIgnoreCase("CLF"))
		{
			return "Car Lease Finance";
		}
		else if (appl.equalsIgnoreCase("CF"))
		{
			return "Car Finance";
		}
		else
		{
			return appl;
		}
	}

	private String getTollFreeNumberForAppl(String appl)
	{
		if (appl.equalsIgnoreCase(Constants.APPL_CARD) || appl.equalsIgnoreCase("HF") || appl.equalsIgnoreCase("LAP")
				|| appl.equalsIgnoreCase("SPL") || appl.equalsIgnoreCase("SPLN") || appl.equalsIgnoreCase("CSG"))
		{
			return "1800 102 6022";
		}
		else if (appl.equalsIgnoreCase("CV") || appl.equalsIgnoreCase("CE") || appl.equalsIgnoreCase("SA")
				|| appl.equalsIgnoreCase("TFE") || appl.equalsIgnoreCase("LCV") || appl.equalsIgnoreCase("GLN"))
		{
			return "1800 209 5600";
		}
		else if (appl.equalsIgnoreCase("RAR"))
		{
			return "1800 120 9820";
		}
		else if (appl.equalsIgnoreCase("CF") || appl.equalsIgnoreCase("CLF"))
		{
			return "1800 209 5732";
		}
		else
		{
			return "";
		}
	}

	private void sendEmailForChequePayment(RandomCollection collection, SystemUser systemUserNew,
			UserActivity userActivity) throws ParseException
	{
		String paymentDate = collection.getDeviceDate();
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat format2 = new SimpleDateFormat("dd-MMM-yyyy");
		Date date = format1.parse(paymentDate);
		paymentDate = format2.format(date);

		String email = collection.getEmailAddress();
		String chequeDetailString = "";

		NumberFormat formatter = NumberFormat.getInstance(new Locale("en", "IN"));

		for (Cheque cheque : collection.getChequeList())
		{
			chequeDetailString = chequeDetailString + " Cheque No." + cheque.getChequeNo() + "     dated"
					+ cheque.getChequeDate();
		}

		String emailText = "";
		if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD))
		{
			emailText = String.format(simpleMailMessageForChequePaymentCreditCard.getText(), collection.getName(),
					getFullFormApplType(collection.getAppl()), collection.getBusinessPartnerNumber(),
					collection.getReceiptNumber(), collection.getAmount() + "", "" + paymentDate, ""
							+ chequeDetailString, collection.getBusinessPartnerNumber(),
					getTollFreeNumberForAppl(collection.getAppl()));
		}
		else
		{
			emailText = String.format(simpleMailMessageForChequePaymentLoan.getText(), collection.getName(),
					getFullFormApplType(collection.getAppl()), collection.getBusinessPartnerNumber(),
					collection.getReceiptNumber(), collection.getAmount() + "", "" + paymentDate, ""
							+ chequeDetailString, collection.getBusinessPartnerNumber(),
					getTollFreeNumberForAppl(collection.getAppl()));
		}

		if (collection.getEmailAddress() != null
				&& !collection.getEmailAddress().equalsIgnoreCase(Constants.EMPTY_STRING))
		{
			if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD))
			{
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForChequePaymentCreditCard.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(
						userActivity.getUserId(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList,
						receiverList, simpleMailMessageForChequePaymentCreditCard.getSubject(), emailText,
						notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);
				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(email, simpleMailMessageForChequePaymentCreditCard.getFrom(),
						simpleMailMessageForChequePaymentCreditCard.getSubject(), emailText))
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
				else
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			}
			else
			{
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForChequePaymentLoan.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(
						userActivity.getUserId(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList,
						receiverList, simpleMailMessageForChequePaymentLoan.getSubject(), emailText,
						notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);
				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(email, simpleMailMessageForChequePaymentLoan.getFrom(),
						simpleMailMessageForChequePaymentLoan.getSubject(), emailText))
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
				else
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			}
		}

		if (collection.getEmailAddressNew() != null
				&& !collection.getEmailAddressNew().equalsIgnoreCase(Constants.EMPTY_STRING)
				&& !collection.getEmailAddressNew().equalsIgnoreCase(email))
		{
			if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD))
			{
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForChequePaymentCreditCard.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(
						userActivity.getUserId(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList,
						receiverList, simpleMailMessageForChequePaymentCreditCard.getSubject(), emailText,
						notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);
				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(collection.getEmailAddressNew(),
						simpleMailMessageForChequePaymentCreditCard.getFrom(),
						simpleMailMessageForChequePaymentCreditCard.getSubject(), emailText))
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
				else
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			}
			else
			{
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForChequePaymentLoan.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(
						userActivity.getUserId(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList,
						receiverList, simpleMailMessageForChequePaymentLoan.getSubject(), emailText,
						notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);
				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(collection.getEmailAddressNew(),
						simpleMailMessageForChequePaymentLoan.getFrom(),
						simpleMailMessageForChequePaymentLoan.getSubject(), emailText))
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
				else
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			}
		}
	}

	private void sendEmailForDDPDC(RandomCollection collection, SystemUser systemUserNew, UserActivity userActivity)
			throws ParseException
	{
		String paymentDate = collection.getDeviceDate();
		String collectionDate = collection.getDeviceDate();

		Date emailDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat emailDateFormat = new SimpleDateFormat("ddMMMyyyy");
		emailDate = (dateFormat.parse(collectionDate));

		String email = collection.getEmailAddress().equals(Constants.EMPTY_STRING) ? collection.getEmailAddress()
				: collection.getEmailAddress();

		String chequeDetailString = "";

		NumberFormat formatter = NumberFormat.getInstance(new Locale("en", "IN"));

		String emailAmount = collection.getAmount() + "";

		DecimalFormat amountFormat = new DecimalFormat("#.00");

		try
		{
			emailAmount = amountFormat.format(Double.parseDouble(emailAmount));
		}
		catch (Exception e)
		{
			emailAmount = "0.00";
		}

		for (Cheque cheque : collection.getChequeList())
		{
			if (collection.getPaymentMode().equalsIgnoreCase(Constants.PAYMENT_MODE_DRAFT))
			{
				chequeDetailString = chequeDetailString
						+ "<br/>Demand Draft No."
						+ cheque.getChequeNo()
						+ "     Dated "
						+ new SimpleDateFormat("ddMMMyyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(cheque
								.getChequeDate()));
			}
			else
			{
				chequeDetailString = chequeDetailString
						+ "<br/> PDC No."
						+ cheque.getChequeNo()
						+ "     Dated "
						+ new SimpleDateFormat("ddMMMyyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(cheque
								.getChequeDate()));
			}

		}

		String emailText = "";

		if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD))
		{
			if (collection.getPaymentMode().equalsIgnoreCase(Constants.PAYMENT_MODE_DRAFT))
			{
				emailText = String.format(simpleMailMessageForDDPDC.getText(), "Credit Card",
						"-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber(),
						collection.getReceiptNumber(), "Demand Draft", emailAmount, emailDateFormat.format(emailDate),
						chequeDetailString, "-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber());
			}
			else
			{
				emailText = String.format(simpleMailMessageForDDPDC.getText(), "Credit Card",
						"-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber(),
						collection.getReceiptNumber(), "PDC", emailAmount, emailDateFormat.format(emailDate),
						chequeDetailString, "-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber());
			}
		}
		else
		{
			if (collection.getPaymentMode().equalsIgnoreCase(Constants.PAYMENT_MODE_DRAFT))
			{
				emailText = String.format(simpleMailMessageForDDPDC.getText(), "Personal Finance Loan ", "-"
						+ collection.getAppl() + " " + collection.getBusinessPartnerNumber(),
						collection.getReceiptNumber(), "Demand Draft", emailAmount, emailDateFormat.format(emailDate),
						chequeDetailString, "-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber());
			}
			else
			{
				emailText = String.format(simpleMailMessageForDDPDC.getText(), "Personal Finance Loan ", "-"
						+ collection.getAppl() + " " + collection.getBusinessPartnerNumber(),
						collection.getReceiptNumber(), "PDC", emailAmount, emailDateFormat.format(emailDate),
						chequeDetailString, "-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber());
			}
		}

		if (collection.getEmailAddress() != null
				&& !collection.getEmailAddress().equalsIgnoreCase(Constants.EMPTY_STRING))
		{
			if (!collection.getAppl().isEmpty())
			{

				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForDDPDC.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(
						userActivity.getUserId(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList,
						receiverList, simpleMailMessageForDDPDC.getSubject(), emailText, notificationActivityService,
						ActivityLoggerConstants.DATABASE_MSSQL);

				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(email, simpleMailMessageForDDPDC.getFrom(),
						simpleMailMessageForDDPDC.getSubject(), emailText))
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), notificationActivityService);

					new Thread(notificationActivityStatusUpdate).run();
				}
				else
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE), notificationActivityService);

					new Thread(notificationActivityStatusUpdate).run();
				}

			}
			else
			{
				log.info("----- Improper Information to Send Email");
			}
		}

		if (collection.getEmailAddressNew() != null
				&& !collection.getEmailAddressNew().equalsIgnoreCase(Constants.EMPTY_STRING)
				&& !collection.getEmailAddressNew().equalsIgnoreCase(email))
		{
			if (!collection.getAppl().isEmpty())
			{
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForCashPaymentCreditCard.getFrom());

				List<String> receiverList = new ArrayList<String>();

				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(
						userActivity.getUserId(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList,
						receiverList, simpleMailMessageForDDPDC.getSubject(), emailText, notificationActivityService,
						ActivityLoggerConstants.DATABASE_MSSQL);
				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(collection.getEmailAddressNew(), simpleMailMessageForDDPDC.getFrom(),
						simpleMailMessageForDDPDC.getSubject(), emailText))
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
				else
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			}
			else
			{
				log.info("----- Improper Information to Send Email");
			}
		}
	}

	private void sendEmailForCashPayment(RandomCollection collection, SystemUser systemUserNew,
			UserActivity userActivity) throws ParseException
	{
		String paymentDate = collection.getDeviceDate();
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat format2 = new SimpleDateFormat("dd-MMM-yyyy");
		Date date = format1.parse(paymentDate);
		paymentDate = format2.format(date);

		String email = collection.getEmailAddress();

		String emailText = "";

		if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD))
		{
			emailText = String.format(simpleMailMessageForCashPaymentCreditCard.getText(), collection.getName(),
					getFullFormApplType(collection.getAppl()), collection.getBusinessPartnerNumber(),
					collection.getReceiptNumber(), collection.getAmount() + "", "" + paymentDate,
					collection.getBusinessPartnerNumber(), getTollFreeNumberForAppl(collection.getAppl()));
		}
		else
		{
			emailText = String.format(simpleMailMessageForCashPaymentLoan.getText(), collection.getName(),
					getFullFormApplType(collection.getAppl()), collection.getBusinessPartnerNumber(),
					collection.getReceiptNumber(), collection.getAmount() + "", "" + paymentDate,
					collection.getBusinessPartnerNumber(), getTollFreeNumberForAppl(collection.getAppl()));
		}

		if (collection.getEmailAddress() != null
				&& !collection.getEmailAddress().equalsIgnoreCase(Constants.EMPTY_STRING))
		{
			if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD))
			{
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForCashPaymentCreditCard.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(
						userActivity.getUserId(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList,
						receiverList, simpleMailMessageForCashPaymentCreditCard.getSubject(), emailText,
						notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);
				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(email, simpleMailMessageForCashPaymentCreditCard.getFrom(),
						simpleMailMessageForCashPaymentCreditCard.getSubject(), emailText))
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
				else
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			}
			else
			{
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForCashPaymentLoan.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(
						userActivity.getUserId(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList,
						receiverList, simpleMailMessageForCashPaymentLoan.getSubject(), emailText,
						notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);
				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(email, simpleMailMessageForCashPaymentLoan.getFrom(),
						simpleMailMessageForCashPaymentLoan.getSubject(), emailText))
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
				else
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			}
		}

		if (collection.getEmailAddressNew() != null
				&& !collection.getEmailAddressNew().equalsIgnoreCase(Constants.EMPTY_STRING)
				&& !collection.getEmailAddressNew().equalsIgnoreCase(email))
		{
			if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD))
			{
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForCashPaymentCreditCard.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(
						userActivity.getUserId(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList,
						receiverList, simpleMailMessageForCashPaymentCreditCard.getSubject(), emailText,
						notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);
				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(collection.getEmailAddressNew(),
						simpleMailMessageForCashPaymentCreditCard.getFrom(),
						simpleMailMessageForCashPaymentCreditCard.getSubject(), emailText))
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
				else
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			}
			else
			{
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForCashPaymentLoan.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(
						userActivity.getUserId(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList,
						receiverList, simpleMailMessageForCashPaymentLoan.getSubject(), emailText,
						notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);
				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(collection.getEmailAddressNew(),
						simpleMailMessageForCashPaymentLoan.getFrom(),
						simpleMailMessageForCashPaymentLoan.getSubject(), emailText))
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
				else
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			}
		}
	}
}