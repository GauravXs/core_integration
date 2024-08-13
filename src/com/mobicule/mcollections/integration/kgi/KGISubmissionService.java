package com.mobicule.mcollections.integration.kgi;

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
import org.springframework.integration.MessageHeaders;
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
import com.mobicule.mcollections.core.beans.Agency;
import com.mobicule.mcollections.core.beans.Cheque;
import com.mobicule.mcollections.core.beans.Collection;
import com.mobicule.mcollections.core.beans.Denomination;
import com.mobicule.mcollections.core.beans.Image;
import com.mobicule.mcollections.core.beans.KGI;
import com.mobicule.mcollections.core.beans.KGIRenewalCases;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.beans.TransactionType;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.EmailUtilities;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.AgencyService;
import com.mobicule.mcollections.core.service.CaseService;
import com.mobicule.mcollections.core.service.CollectionService;
import com.mobicule.mcollections.core.service.KGIService;
import com.mobicule.mcollections.core.service.OfflineSMSService;
import com.mobicule.mcollections.core.service.SystemUserService;
import com.mobicule.mcollections.integration.commons.ServerUtilities;
import com.mobicule.mcollections.webservice.adapter.KotakCollectionWebserviceAdapter;

public class KGISubmissionService implements IKGISubmissionService
{

	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private UserActivityService userActivityService;

	@Autowired
	private SystemUserService systemUserService;

	@Autowired
	private CommunicationActivityService communicationActivityService;

	@Autowired
	private NotificationActivityService notificationActivityService;

	@Autowired
	private CollectionService collectionService;

	@Autowired
	private CaseService caseService;

	@Autowired
	private AgencyService agencyService;

	@Autowired
	ApplicationConfiguration applicationConfiguration;

	@Autowired
	private SimpleMailMessage simpleMailMessageForCashPaymentCreditCard;

	@Autowired
	private SimpleMailMessage simpleMailMessageForCashPaymentLoan;

	@Autowired
	private SimpleMailMessage simpleMailMessageForChequePaymentCreditCard;

	@Autowired
	private SimpleMailMessage simpleMailMessageForChequePaymentLoan;

	@Autowired
	private SimpleMailMessage simpleMailMessageForCardPayment;

	@Autowired
	private SimpleMailMessage simpleMailMessageForDDPDC;

	private EmailUtilities emailService;

	@Autowired
	private OfflineSMSService offlineSMSService;

	@Autowired
	private KGIService kgiService;

	/*public OfflineSMSService getOfflineSMSService()
	{
		return offlineSMSService;
	}

	public void setOfflineSMSService(OfflineSMSService offlineSMSService)
	{
		this.offlineSMSService = offlineSMSService;
	}*/

	public EmailUtilities getEmailService()
	{
		return emailService;
	}

	public void setEmailService(EmailUtilities emailService)
	{
		this.emailService = emailService;
	}

	@Override
	public Message<String> execute(Message<String> message) throws Throwable
	{
		log.info(" -------- In CollectionsService -------- ");

		String status = JsonConstants.FAILURE;
		String returnMessage = null;
		String contractAccountNumber = "";
		String lockCode = "";
		String collectionCode = "";
		String allocationNumber = "";
		String bp = "";
		String amount = "0.0";
		String pan = "";
		String email = "";
		String contact = "";
		String collectionStatus = "";
		String deviceDate = "";
		String revisitedDate = "";
		String area = "";
		String mread = "";
		String emailAddressNew = "";
		String mobileNumberNew = "";
		String deviceTime = "";
		String collStatus = "";
		String receiptNumber = "";
		String remarks = "";
		String billNo = "";
		String batchNumber = "";
		String billCycle = "";
		String signaturePath = "";
		String signature = "";
		String caseId = "0L";

		String feedback_code = "";
		String ptpAmount = "0.00";

		String latitude = "";
		String longitude = "";
		String partyName = "";
		String nextActionCode = "";
		String nextActionCodeDescription = "";
		boolean submissionFlag = false;
		String regNo = "";
		String branchName = "";

		String paymentDate = "";

		List<Image> images = new ArrayList<Image>();
		MessageHeaders messageHeader = message.getHeaders();

		SystemUser systemUser = (SystemUser) messageHeader.get(Constants.SYSTEM_USER_BEAN);

		try
		{
			String requestSet = message.getPayload();

			UserActivityAddition userActivityAddition = new UserActivityAddition(requestSet, userActivityService,
					ActivityLoggerConstants.DATABASE_MSSQL);

			new Thread(userActivityAddition).run();

			UserActivity userActivity = userActivityAddition.extractUserActivity();

			JSONObject jsonObject = new JSONObject(requestSet);
			JSONObject data = (JSONObject) jsonObject.get(JsonConstants.DATA);
			JSONObject user = (JSONObject) jsonObject.get(JsonConstants.SYSTEM_USER);
			SystemUser systemUserNew = ServerUtilities.extractSystemUser(user);

			SystemUser systemUserTemp = systemUserService.getUser(systemUserNew.getUserTableId());
			systemUserNew.setSupervisorMobileNumber(systemUserTemp.getSupervisorMobileNumber());
			systemUserNew.setSupervisorName(systemUserTemp.getSupervisorName());

			log.info("----system user ----" + systemUserNew);

			Collection collection = new Collection();

			Map reqMap = Utilities.createMapFromJSON(requestSet);
			String type = (String) reqMap.get(JsonConstants.Key.TYPE);

			String requestEntity = data.get(JsonConstants.APPL) == null ? "" : data.getString(JsonConstants.APPL);

			/*Deposition submission check*/

			/*try {
				
			
			if (type.toString().equalsIgnoreCase("collections") || (data.has("isOffline") && data.get("isOffline").toString().equalsIgnoreCase("Yes"))) {
				
				if (systemUserService.checkDepositionLockedStatus(systemUserTemp.getUserTableId()))
				{
					returnMessage = JsonConstants.COLLECTIONS_FAILED_DUE_TO_DEPOSITION;
					return responseBuilder(message, JsonConstants.FAILURE, returnMessage,data.has(JsonConstants.REQUEST_ID) == true ? data
							.getString(JsonConstants.REQUEST_ID) : "");
				}
			}
			
			}
			catch(Exception e){
				
				log.error("---- Deposition Locked : Exception Detail ----",e);
				
			}*/

			/*			

						if (type.toString().equalsIgnoreCase("RNC") || type.toString().equalsIgnoreCase("ROC"))
						{
							if (!data.has(JsonConstants.RequestData.LOCK_CODE))
							{
								returnMessage = JsonConstants.COLLECTION_LOCK_CODE_ERROR;
								return responseBuilder(message, status, returnMessage, "");
							}
							else
							{
								lockCode = (String) data.get(JsonConstants.RequestData.LOCK_CODE);
							}
						}

						if (lockCode.equals(Constants.Collection.LOCK_CODE_FULLY_COMPLETE))
						{
							collection.setModifiedOn(Utilities.generateTimestamp(Constants.DATE_TIME_FORMAT));

							log.info("before setting setModifiedBy value");
							collection.setModifiedBy((long) 555555);
							log.info("after setting setModifiedBy value");

							caseService.updateCase(collection, Constants.COLLECTIONS_STATUS_COMPLETE);

							returnMessage = JsonConstants.COLLECTION_SUBMIT_SUCCESS;

							status = JsonConstants.SUCCESS;

							return responseBuilder(message, status, returnMessage, "");
						}

						if (lockCode.equals(Constants.Collection.LOCK_CODE_INCOMPLETE))
						{
							collectionStatus = Constants.COLLECTIONS_STATUS_INCOMPLETE;
						}

						if (lockCode.equals(Constants.Collection.LOCK_CODE_COMPLETE))
						{
							collectionStatus = Constants.COLLECTIONS_STATUS_COMPLETE;
						}*/

			/*if (type.toString().equalsIgnoreCase("collections"))
			{
				caseId = data.get("caseId") == null ? "0L" : data.get("caseId").toString();
			}*/

			collectionCode = data.get(JsonConstants.RequestData.COLLECTION_CODE) == null ? "" : (String) data
					.get(JsonConstants.RequestData.COLLECTION_CODE);

			if (data.has(JsonConstants.FEEDBACK_CODE))
			{
				feedback_code = data.getString(JsonConstants.FEEDBACK_CODE);

			}

			if (data.has(JsonConstants.NEXT_ACTION_CODE))
			{
				if (data.getString(JsonConstants.NEXT_ACTION_CODE).contains("("))
				{
					String[] nextActionCodeValues = (data.getString(JsonConstants.NEXT_ACTION_CODE)).split("\\(");
					nextActionCode = nextActionCodeValues[0].trim();
					nextActionCodeDescription = nextActionCodeValues[1].replace(")", "").trim();
				}

			}

			if (((!collectionCode.equalsIgnoreCase("RTP")) || amount.equals(Constants.EMPTY_STRING))
					&& !collectionCode.equalsIgnoreCase("PU"))
			{

				if (data.has(JsonConstants.PTP_AMOUNT))
					ptpAmount = data.getString(JsonConstants.PTP_AMOUNT);

			}

			else if (collectionCode.equalsIgnoreCase("PU"))
			{

				if (data.has(JsonConstants.RequestData.AMOUNT))
					ptpAmount = data.getString(JsonConstants.RequestData.AMOUNT);

			}
			else
			{

				if (data.has(JsonConstants.RequestData.AMOUNT))
					amount = (String) data.get(JsonConstants.RequestData.AMOUNT);

			}

			if (!collectionCode.equalsIgnoreCase("RTP"))
			{

				revisitedDate = data.get(JsonConstants.RequestData.REVISITED_DATE) == null ? "" : (String) data
						.get(JsonConstants.RequestData.REVISITED_DATE);

			}

			deviceTime = data.get(JsonConstants.RequestData.DEVICE_TIME) == null ? "" : (String) data
					.get(JsonConstants.RequestData.DEVICE_TIME);

			deviceDate = data.get(JsonConstants.RequestData.DEVICE_DATE) == null ? "" : (String) data
					.get(JsonConstants.RequestData.DEVICE_DATE);

			area = data.get(JsonConstants.RequestData.AREA) == null ? "" : (String) data
					.get(JsonConstants.RequestData.AREA);

			if (data.has(JsonConstants.RequestData.BRANCH_NAME))
			{
				branchName = data.get(JsonConstants.RequestData.BRANCH_NAME) == null ? "" : (String) data
						.get(JsonConstants.RequestData.BRANCH_NAME);
			}

			if (data.has(JsonConstants.RequestData.PAYMENT_DATE))
			{
				paymentDate = data.get(JsonConstants.RequestData.PAYMENT_DATE) == null ? "" : (String) data
						.get(JsonConstants.RequestData.PAYMENT_DATE);
			}

			if (data.has(JsonConstants.RequestData.COLLECTION_STATUS))
			{

				collStatus = data.get(JsonConstants.RequestData.COLLECTION_STATUS) == null ? "" : (String) data
						.get(JsonConstants.RequestData.COLLECTION_STATUS);
			}

			receiptNumber = data.get(JsonConstants.RequestData.RECEIPT_NUMBER) == null ? "" : (String) data
					.get(JsonConstants.RequestData.RECEIPT_NUMBER);

			remarks = data.get(JsonConstants.RequestData.REMARKS) == null ? "" : (String) data
					.get(JsonConstants.RequestData.REMARKS);

			if (type.toString().equalsIgnoreCase("collections"))
			{
				billCycle = data.get(JsonConstants.BILL_CYCLE) == null ? "" : (String) data
						.get(JsonConstants.BILL_CYCLE);
			}

			mobileNumberNew = data.get(JsonConstants.MOBILE_NUMBER_NEW) == null ? "" : data
					.getString(JsonConstants.MOBILE_NUMBER_NEW);

			emailAddressNew = data.get(JsonConstants.EMAIL_ADDRESS_NEW) == null ? "" : data
					.getString(JsonConstants.EMAIL_ADDRESS_NEW);

			email = data.get(JsonConstants.EMAIL_ADDRESS) == null ? "" : data.getString(JsonConstants.EMAIL_ADDRESS);

			contact = data.get(JsonConstants.MOBILE_NUMBER) == null ? "" : data.getString(JsonConstants.MOBILE_NUMBER);

			if (data.has(JsonConstants.LATITUDE) && data.has(JsonConstants.LONGITUDE))
			{
				latitude = data.get(JsonConstants.LATITUDE) == null ? "" : data.getString(JsonConstants.LATITUDE);
				longitude = data.get(JsonConstants.LONGITUDE) == null ? "" : data.getString(JsonConstants.LONGITUDE);
			}
			else
			{
				latitude = "0.00";
				longitude = "0.00";
			}

			String payMode = data.get(JsonConstants.RequestData.PAY_MODE) == null ? "" : (String) data
					.get(JsonConstants.RequestData.PAY_MODE);

			collection.setEmailAddress(data.get(JsonConstants.EMAIL_ADDRESS) == null ? "" : data
					.getString(JsonConstants.EMAIL_ADDRESS));

			collection.setCorrAddress(data.get(JsonConstants.CORRESPONDENCE_ADDRESS) == null ? "" : data
					.getString(JsonConstants.CORRESPONDENCE_ADDRESS));

			collection.setCorrLocation(data.getString(JsonConstants.CORRESPONDENCE_LOCATION) == null ? "" : data
					.getString(JsonConstants.CORRESPONDENCE_LOCATION));

			collection.setCorrPin(data.getString(JsonConstants.CORRESPONDENCE_PINCODE));

			collection.setSecAddress(data.getString(JsonConstants.SECOND_ADDRESS));

			collection.setSecLocation(data.getString(JsonConstants.SECOND_LOCATION));

			collection.setSecPin(data.getString(JsonConstants.SECOND_PINCODE));

			collection.setDueDate(data.getString(JsonConstants.DUE_DATE));

			/*if (type.toString().equalsIgnoreCase("collections"))
			{
				collection.setCaseId(Long.parseLong(caseId));
			}*/

			/*Out */
		/*	collection.setOutstanding(data.getString(JsonConstants.OUTSTANDING) == null
					|| data.getString(JsonConstants.OUTSTANDING).equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0"
					: data.getString(JsonConstants.OUTSTANDING));*/

			if (payMode.equals(Constants.PAYMENT_MODE_CASH) && !amount.equals("0.0")
					&& !collectionCode.equalsIgnoreCase("PU"))
			{
				if (data.has(JsonConstants.RequestData.CASH))
				{
					extractCashDetails(pan, data, collection);

					log.info("###pan" + pan);
				}
				else
				{
					status = JsonConstants.FAILURE;
					returnMessage = JsonConstants.CASH_DETAILS_ABSENT;
					return responseBuilder(message, status, returnMessage, "");
				}

			}

			List<Cheque> cheques = new ArrayList<Cheque>();

			log.info("---collection code" + collectionCode);

			if ((Constants.PAYMENT_MODE_CHEQUE.equals(payMode) || Constants.PAYMENT_MODE_DRAFT.equals(payMode) || Constants.PAYMENT_MODE_PDC
					.equals(payMode)) && !collectionCode.equalsIgnoreCase("PU"))
			{
				cheques = getCheques(systemUserNew, data);
				log.info("Cheques got : " + cheques);
				if (null == cheques || cheques.isEmpty())
				{
					returnMessage = JsonConstants.COLLECTION_CHEQUE_MANDATORY;
					log.info("No Cheques, Return Message :" + returnMessage);
					return responseBuilder(message, status, returnMessage, "");
				}

			}

			if (data.has(JsonConstants.LOAN))
			{
				JSONObject loanJSON = (JSONObject) data.getJSONObject(JsonConstants.LOAN);

				collection.setOverdue(Double.parseDouble(loanJSON.has(JsonConstants.LOAN_OVERDUE) ? loanJSON.get(
						JsonConstants.LOAN_OVERDUE).toString() : "0.0"));
				collection.setPenalAmt(Double.parseDouble(loanJSON.get(JsonConstants.LOAN_PENAL_AMOUNT) == null
						|| loanJSON.get(JsonConstants.LOAN_PENAL_AMOUNT).toString()
								.equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0" : loanJSON.get(
						JsonConstants.LOAN_PENAL_AMOUNT).toString()));

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

				collection.setTransType(transTypeList);
				log.info("transTypeList : " + transTypeList);

			}

			
			if(type.equalsIgnoreCase("RNC")) {
				
				type="RENEWAL";
				
			}
			
			if(type.equalsIgnoreCase("ROC")) {
				
				type="ROLLOVER";

			}
			collection.setCollectionType(type + "_" + Constants.RANDOM_COLLECTIONS);

			collection.setCollectionNature(Constants.COLLECTION_NATURE_GENERAL);

			if (data.has(JsonConstants.NAME))
			{
				collection.setPartyName(data.getString(JsonConstants.NAME));
			}

			if (data.has(JsonConstants.PARTY_ID))
			{

				collection.setContractAccountNumber(data.getString(JsonConstants.PARTY_ID));
			}

			/*if (data.has(JsonConstants.CONTRACT_ACCOUNT_NUMBER))
			{
				collection.setContractAccountNumber(data.getString(JsonConstants.CONTRACT_ACCOUNT_NUMBER));
			}*/

			if (data.has(JsonConstants.CC_APAC))
			{
				collection.setCcapac(data.getString(JsonConstants.CC_APAC));
			}

			int numberOfApacs = 0;
			numberOfApacs = data.getString("noOfApac") == null ? 0 : Integer.parseInt(data.getString("noOfApac"));

			collection.setNumberOfApacs(numberOfApacs);

			collection.setCollectionCode(collectionCode);
			collection.setAllocationNumber(allocationNumber);
			collection.setRequestId(data.has(JsonConstants.REQUEST_ID) == true ? data
					.getString(JsonConstants.REQUEST_ID) : new Timestamp(System.currentTimeMillis()).toString());
			collection.setMobileNumberNew(mobileNumberNew);
			collection.setEmailAddressNew(emailAddressNew);
			collection.setArea(area);
			collection.setChequeDetails(cheques);
			collection.setCollectionStatus(collStatus);
			collection.setCollectionCode(collectionCode);
			collection.setReceiptNumber(receiptNumber);
			collection.setRevisitDate(revisitedDate);
			collection.setMeterReading("sms");
			collection.setPaymentMode(payMode);
			collection.setDeviceDate(deviceDate);
			collection.setDeviceTime(deviceTime);
			collection.setSubmissionDateTime(Utilities.sysDate());
			collection.setRemarks(remarks);
			collection.setCurrentBillNo(billNo);
			collection.setBusinessPartnerNumber(data.getString(JsonConstants.UNIQUE_NUMBER));

			collection.setAppl(data.getString(JsonConstants.APPL));
			collection.setBillCycle(billCycle);

			collection.setMobileNumber(data.getString(JsonConstants.MOBILE_NUMBER));
			collection.setSignaturePath(signaturePath);
			collection.setImages(images);
			collection.setUserName(systemUserNew.getName());

			collection.setUser(systemUserNew); // new added
			collection.setContact(contact);
			collection.setEmail(email);
			collection.setBatchNumber(batchNumber);
			collection.setAmount(amount);
			collection.setAppropriateAmount(amount);
			collection.setArFeedbackCode(feedback_code == null ? " " : feedback_code);
			collection.setPtpAmount(ptpAmount);
			collection.setNextActionCode(nextActionCode);
			collection.setNextActionCodeDescription(nextActionCodeDescription);
			collection.setBranchName(branchName);
			collection.setPaymentDate(paymentDate);
			collection.setDepositionStatus(Constants.Deposition.INITIAL_DEPOSITION);
			
			collection.setRegNo(data.has(JsonConstants.SettlmentConstant.REGISTRATION_NUMBER) ? data.get(JsonConstants.SettlmentConstant.REGISTRATION_NUMBER).toString()
					: "");
			
			collection.setExpiryDate(data.has("expiryDate") ? data.get("expiryDate").toString()
					: "");
			
			collection.setEngineNumber(data.has("engiNo") ? data.get("engiNo").toString()
					: "");
			
			

			KGI kgi = new KGI();

			kgi.setTpPremiumRate(data.has(JsonConstants.TP_PREMIUM) ? data.get(JsonConstants.TP_PREMIUM).toString()
					: "0.0");
			kgi.setOdPremiumRate(data.has(JsonConstants.OD_PREMIUM_RATE) ? data.get(JsonConstants.OD_PREMIUM_RATE)
					.toString() : "0.0");
			kgi.setServiceTax(data.has(JsonConstants.SERVICE_TAX) ? data.get(JsonConstants.SERVICE_TAX).toString()
					: "0.0");
			kgi.setNcbPercentage(data.has(JsonConstants.NCB_PERCENTAGE) ? data.get(JsonConstants.NCB_PERCENTAGE)
					.toString() : "0.0");
			kgi.setLegalLiabilityToDriver(data.has(JsonConstants.LIABILITY_TO_DRIVER) ? data.get(
					JsonConstants.LIABILITY_TO_DRIVER).toString() : "0.0");
			kgi.setIdv(data.has(JsonConstants.IDV) ? data.get(JsonConstants.IDV).toString() : "0.0");
			kgi.setPremiumAmount(data.has("premiumAmount") ? data.get("premiumAmount")
					.toString() : "");
			
			log.info("----kgi premium amnt---"+kgi.getPremiumAmount());
			
			kgi.setPolicyNumber(data.has(JsonConstants.POLICY_NUMBER) ? data.get(JsonConstants.POLICY_NUMBER)
					.toString() : "");

			kgi.setInvoiceValue(data.has(JsonConstants.INVOICE_VALUE) ? data.get(JsonConstants.INVOICE_VALUE)
					.toString() : "");
			kgi.setModelVariant(data.has(JsonConstants.MODEL_VARIANT) ? data.get(JsonConstants.MODEL_VARIANT)
					.toString() : "");
			kgi.setChassisNumber(data.has(JsonConstants.CHASSIS_NO) ? data.get(JsonConstants.CHASSIS_NO).toString()
					: "");

			collection.setKgi(kgi);

			log.info("----pan details----" + collection.getPan());

			if (type.toString().equalsIgnoreCase("collections")
					|| type.toString().equalsIgnoreCase("randomCollections"))
			{
				collection.setLatitude(latitude);
				collection.setLongitude(longitude);
			}

			Agency agency = new Agency();
			agency.setAgencyId(systemUserNew.getAgencyId());
			List<Agency> agencies = agencyService.searchAgency(agency);
			collection.setAgencyName(agencies.get(0).getAgencyName());

			log.info("collection trans type ------------------>" + collection.getTransType());

			Utilities.primaryBeanSetter(collection, systemUserNew);

			

			/*if (!data.has(JsonConstants.RequestData.SIGN))
			{
				returnMessage = JsonConstants.COLLECTION_SIGNATURE_MANDATORY;
				return responseBuilder(message, status, returnMessage, "");
			}
			else
			{
				signature = (String) data.get(JsonConstants.RequestData.SIGN);
			}*/

			/*if (!signature.isEmpty())
			{
				signaturePath = extractImagePath(collection, signature, Constants.SIGNATURE_IMAGE_FILE_PATH,
						Constants.EMPTY_STRING);
			}*/

			/*			if (type.toString().equalsIgnoreCase("collections")
								|| type.toString().equalsIgnoreCase("randomCollections")
								|| type.toString().equalsIgnoreCase("fileCollections")
								|| type.toString().equalsIgnoreCase("fileRandomCollections"))
						{
							if (!data.has(JsonConstants.RequestData.IMAGES))
							{

								returnMessage = JsonConstants.COLLECTION_SUBMIT_FAILURE;
								return responseBuilder(message, status, returnMessage, "");
							}
							else
							{

								JSONArray imageDetails = data.getJSONArray(JsonConstants.RequestData.IMAGES);
								images = getImages(systemUserNew, imageDetails, collection);
								collection.setImages(images);

								if (images == null)
								{

									status = JsonConstants.FAILURE;
									returnMessage = "Image Path Not Found";
									return responseBuilder(message, status, returnMessage, "");
								}
							}
						}*/

			/*			if (collectionCode.equalsIgnoreCase(JsonConstants.CUSTOMER_UPDATE_CODE))
						{
							try
							{

								if (caseService.checkDuplicateCustomerData(collection))
								{
									if (caseService.submitCustomerData(collection))
									{

										status = JsonConstants.SUCCESS;
										return responseBuilder(message, status, "Customer Data got submitted successfully",
												collection.getRequestId());
									}

									else
									{

										status = JsonConstants.FAILURE;

										return responseBuilder(message, status, "Some Error Occured", "");

									}

								}
								else
								{
									status = JsonConstants.SUCCESS;
									return responseBuilder(message, status, "Duplicate JSON !", "");

								}

							}
							catch (Exception e)
							{

								log.error("------Exception Detail while submission of customer is ", e);

								status = JsonConstants.FAILURE;

								return responseBuilder(message, status, "Some Error Occured", "");

							}

						}*/

			if (kgiService.checkDuplicateKGICollections(collection))
			{
				log.info("collection cheque details ========----------->" + collection.getChequeDetails());
				log.info("complete collection -------------->" + collection);

				submissionFlag = kgiService.submitCollection(collection);
				log.info("-------submissionFlag--------" + submissionFlag);

				if (submissionFlag)
				{
					log.info("Collection submitted without violation");
					System.out.println("Collection submitted without violation");
					//caseService.updateCase(collection, collectionStatus);

					// Code for sending sms to PTP / Broken Promisev / Door Lock
					try
					{

						

							if (collection.getCollectionCode().equalsIgnoreCase("PTP")
									|| collection.getCollectionCode().equalsIgnoreCase("DL")
									|| collection.getCollectionCode().equalsIgnoreCase("BRP"))
							{
								//sendSms(collection, systemUserNew);
							}

							
							if (collection.getCollectionCode().equalsIgnoreCase("RTP")
									)
							{

								try
								{

									sendKGICollectionsSms(collection, systemUserNew);

									log.info("testing 1");

									//offlineSMSService.updateOfflineSMSData(collection.getReceiptNumber());

								}
								catch (Exception e)
								{

									log.error("---Error While sending SMS---", e);

								}

								try
								{

									// callEmailService(collection);

								}
								catch (Exception e)
								{

								}

							}

						
					}
					catch (Exception e)
					{
						log.info("Error while sending sms to PTP / Broken Promise / Door Lock" + e);
						e.printStackTrace();
					}

					log.info("------------- Collection Submitted and Case Updated sucessfully -------------");

					status = JsonConstants.SUCCESS;

					UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
							(ActivityLoggerConstants.STATUS_SUCCESS), userActivityService);
					new Thread(userActivityStatusUpdate).run();

					return responseBuilder(message, status, "Collection got submitted successfully",
							collection.getRequestId());

				}
				else
				{
					System.out.println("Collection submitted with violation");
					log.info("Collection submitted with violation");

					status = JsonConstants.FAILURE;

					UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
							(ActivityLoggerConstants.STATUS_FAILURE), userActivityService);
					new Thread(userActivityStatusUpdate).run();

					return responseBuilder(message, status, "Some error has occured", "");

				}// status = JsonConstants.SUCCESS;

			}

			else
			{
				log.info("--------- Collection Record already exists, JSON Duplicated! ------------");
				status = JsonConstants.SUCCESS;
				// status = JsonConstants.FAILURE;

				// returnMessage = JsonConstants.COLLECTION_SUBMIT_SUCCESS;

				returnMessage = "JSON DUPLICATED!!!";

				if (type.toString().equalsIgnoreCase("collections"))
				{
					returnMessage = "JSON DUPLICATED For Collections!!!";
				}
				if (type.toString().equalsIgnoreCase("randomCollections"))
				{
					returnMessage = "JSON DUPLICATED For RandomCollections!!!";
				}
				if (type.toString().equalsIgnoreCase("fileCollections"))
				{
					returnMessage = "JSON DUPLICATED For FileCollections!!!";
				}
				if (type.toString().equalsIgnoreCase("fileRandomCollections"))
				{
					returnMessage = "JSON DUPLICATED For FileRandomCollections!!!";
				}

				UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
						(ActivityLoggerConstants.STATUS_IGNORE), userActivityService);
				new Thread(userActivityStatusUpdate).run();

				return responseBuilder(message, status, returnMessage, collection.getRequestId());
			}
		}

		catch (Exception e)
		{
			e.printStackTrace();
			log.error("--- Exception In CollectionSubmissionService :: " + e);

			returnMessage = JsonConstants.COLLECTION_SUBMIT_FAILURE;
			return responseBuilder(message, status, returnMessage, "");
		}
	}

	private void sendSms(Collection collection, SystemUser systemUserNew)
	{
		log.info("------- Before Sending SMS  --------" + collection);
		if (collection.getMobileNumber() != null
				&& !collection.getMobileNumber().equalsIgnoreCase(Constants.EMPTY_STRING))
		{
			log.info("Sending sms to customer mobile number");

			callSMSDispatcher(collection, collection.getMobileNumber(), systemUserNew);
		}

		if (collection.getMobileNumberNew() != null
				&& !collection.getMobileNumberNew().equalsIgnoreCase(Constants.EMPTY_STRING)
				&& !collection.getMobileNumberNew().equalsIgnoreCase(collection.getMobileNumber()))
		{
			log.info("Sending sms to customer alternate mobile number ");

			callSMSDispatcher(collection, collection.getMobileNumber(), systemUserNew);
		}

		if (systemUserNew.getMobileNumber() != null
				&& !systemUserNew.getMobileNumber().equalsIgnoreCase(Constants.EMPTY_STRING))
		{
			log.info("Sending sms to FE mobile number ");

			generateSMSDispatcherMapForFE(collection, systemUserNew.getMobileNumber(), systemUserNew);

		}

	}

	private void generateSMSDispatcherMapForFE(Collection collection, String mobileNumber, SystemUser systemUserNew)
	{
		log.info("---- Inside callSMSDispatcher --------");
		try
		{
			String webserviceUrl = (String) applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");

			Map<String, Object> smsDispatcherMap;

			smsDispatcherMap = ServerUtilities.generateSMSDispatcherMapFEForNonRTP(collection, mobileNumber);

			StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true, new HashMap<String, String>());

			log.info("----- xmlRequest : -------" + xmlRequest);

			CommunicationActivityAddition communicationActivityAddition = new CommunicationActivityAddition(
					systemUserNew.getUserTableId().toString(), systemUserNew.getImeiNo(),
					(collection.getAppl() + "_" + collection.getCollectionType()), webserviceUrl,
					xmlRequest.toString(), communicationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);

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

				log.info("----- Result of SMS Dispatch : -------" + result);
			}
			else
			{
				communicationActivity.setResponse(ActivityLoggerConstants.EMPTY_STRING);

				CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
						communicationActivity, (ActivityLoggerConstants.STATUS_FAILURE), communicationActivityService);

				new Thread(communicationActivityStatusUpdate).run();

				log.info("----- Failure in sending SMS : -------");
			}
		}
		catch (ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void callSMSDispatcher(Collection collection, String mobileNumber, SystemUser systemUserNew)
	{
		log.info("---- Inside callSMSDispatcher --------");
		try
		{
			String webserviceUrl = (String) applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");

			Map<String, Object> smsDispatcherMap = ServerUtilities.generateSMSDispatcherMapForNonRTP(collection,
					mobileNumber);

			StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true, new HashMap<String, String>());

			log.info("----- xmlRequest : -------" + xmlRequest);

			CommunicationActivityAddition communicationActivityAddition = new CommunicationActivityAddition(
					systemUserNew.getUserTableId().toString(), systemUserNew.getImeiNo(),
					(collection.getAppl() + "_" + collection.getCollectionType()), webserviceUrl,
					xmlRequest.toString(), communicationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);

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

				log.info("----- Result of SMS Dispatch : -------" + result);
			}
			else
			{
				communicationActivity.setResponse(ActivityLoggerConstants.EMPTY_STRING);

				CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
						communicationActivity, (ActivityLoggerConstants.STATUS_FAILURE), communicationActivityService);

				new Thread(communicationActivityStatusUpdate).run();

				log.info("----- Failure in sending SMS : -------");
			}
		}
		catch (ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void extractCashDetails(String pan, JSONObject data, Collection collection) throws JSONException
	{
		JSONObject cashDetail = new JSONObject();

		cashDetail = (JSONObject) data.get(JsonConstants.RequestData.CASH);

		collection.setDocType(cashDetail.get(JsonConstants.DOCUMENT_TYPE).toString());
		collection.setDocRef(cashDetail.get(JsonConstants.DOCUMENT_REFERENCE).toString());

		if (collection.getDocType() != null && collection.getDocType().equalsIgnoreCase("PAN"))
		{
			collection.setPan(cashDetail.get(JsonConstants.DOCUMENT_REFERENCE).toString());
		}
		if (collection.getDocType() != null && collection.getDocType().equalsIgnoreCase("F60"))
		{
			log.info("@@ inside f60");

			//pan = pan + "FORM60";
			/*collection.setPan("FORM60");*/
			collection.setDocRef("FORM60");

		}

		JSONArray denominationArray = cashDetail.getJSONArray(JsonConstants.DENOMINATION);

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
		collection.setDenomination(denominationList);

		if (cashDetail.has(JsonConstants.RequestData.PAN))
		{
			pan = (String) cashDetail.get(JsonConstants.RequestData.PAN);
		}

		if (cashDetail.has(JsonConstants.INSTRUMENT_DATE))
		{
			collection.setInstDate((String) cashDetail.get(JsonConstants.INSTRUMENT_DATE));
		}
	}

	private Message<String> responseBuilder(Message<String> message, String status, String returnMessage, String reqId)
			throws JSONException
	{
		JSONObject responseJSON = new JSONObject();
		JSONObject data = new JSONObject();

		data.put("reqId", reqId);

		responseJSON.put(JsonConstants.STATUS, status);
		responseJSON.put(JsonConstants.MESSAGE, returnMessage);
		responseJSON.put(JsonConstants.DATA, data);

		return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
	}

	private List<Cheque> getCheques(SystemUser systemUser, JSONObject data) throws JSONException
	{
		JSONArray chequeDetails = new JSONArray();

		if (data.has(JsonConstants.RequestData.CHEQUE))
		{
			chequeDetails = (JSONArray) data.get(JsonConstants.RequestData.CHEQUE);
		}

		List<Cheque> cheques = new ArrayList<Cheque>();
		JSONObject chequeDetail = new JSONObject();
		Cheque cheque = null;

		try
		{
			for (int index = 0; index < chequeDetails.length(); index++)
			{
				chequeDetail = (JSONObject) chequeDetails.get(index);
				cheque = new Cheque();

				String chequeAmt = "0";
				String chequeDate = "";
				String micr = "";
				String chequeNumber = "";
				String bankName = "";
				String branch = "";
				String drawerAccountNumber = "";
				if (chequeDetail.has(JsonConstants.DRAWER_ACCOUNT_NUMBER))
				{
					drawerAccountNumber = (String) chequeDetail.get(JsonConstants.DRAWER_ACCOUNT_NUMBER);
				}

				if (chequeDetail.has(JsonConstants.RequestData.AMOUNT))
				{
					chequeAmt = (String) chequeDetail.get(JsonConstants.RequestData.AMOUNT);
				}

				if (chequeDetail.has(JsonConstants.CHEQUE_DATE))
				{
					chequeDate = (String) chequeDetail.get(JsonConstants.CHEQUE_DATE);
				}

				if (chequeDetail.has(JsonConstants.RequestData.MICR))
				{
					micr = (String) chequeDetail.get(JsonConstants.RequestData.MICR);
				}

				if (chequeDetail.has(JsonConstants.RequestData.CHEQUE_NUMBER))
				{
					chequeNumber = (String) chequeDetail.get(JsonConstants.RequestData.CHEQUE_NUMBER);
				}

				if (chequeDetail.has(JsonConstants.RequestData.BANK_NAME))
				{
					bankName = (String) chequeDetail.get(JsonConstants.RequestData.BANK_NAME);
				}

				if (chequeDetail.has(JsonConstants.RequestData.BRANCH))
				{
					branch = (String) chequeDetail.get(JsonConstants.RequestData.BRANCH);
				}

				cheque.setAmount(Double.parseDouble(chequeDetail.getString(JsonConstants.AMOUNT) == null
						|| chequeDetail.getString(JsonConstants.AMOUNT).equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0"
						: chequeDetail.getString(JsonConstants.AMOUNT)));
				cheque.setChequeDate(chequeDate);
				cheque.setChequeNo(chequeNumber);
				cheque.setMicrCode(micr);
				cheque.setDepositStatus(Constants.EMPTY_STRING);
				cheque.setDepositDate(Constants.EMPTY_STRING);
				cheque.setBankName(bankName);
				cheque.setBranch(branch);
				cheque.setDrawerAccountNumber(drawerAccountNumber);
				Utilities.primaryBeanSetter(cheque, systemUser);
				cheques.add(cheque);
			}
			return cheques;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return new ArrayList<Cheque>();
		}
	}

	private List<Image> getImages(SystemUser systemUser, JSONArray imageDetails, Collection collection)
			throws JSONException
	{

		JSONObject imageDetail = new JSONObject();
		Image image = null;
		String imagePath = null;

		List<Image> images = new ArrayList<Image>();

		for (int index = 0; index < imageDetails.length(); index++)
		{

			imageDetail = (JSONObject) imageDetails.get(index);

			if (!imageDetail.has(JsonConstants.RequestData.IMAGE))
			{

				return new ArrayList<Image>();
			}

			String imageByteArray = (String) imageDetail.get(JsonConstants.RequestData.IMAGE);
			if (imageByteArray.isEmpty())
			{

				return new ArrayList<Image>();
			}
			image = new Image();

			imagePath = (extractImagePath(collection, imageByteArray, Constants.IMAGE_FILE_PATH,
					(String.valueOf(index))));

			if (imagePath.equals(JsonConstants.ERROR))
			{

				return null;
			}
			else
			{

				image = new Image();
				image.setPath(imagePath);
				Utilities.primaryBeanSetter(image, systemUser);
				images.add(image);

			}
		}
		return images;
	}

	private String extractImagePath(Collection collection, String type, String entity, String index)
	{
		try
		{

			String fileName = collection.getCaseId() + Constants.SYMBOL_UNDERSCORE + collection.getReceiptNumber()
					+ Constants.SYMBOL_UNDERSCORE + System.currentTimeMillis();

			String filePath = Constants.EMPTY_STRING;

			if (index.equals(Constants.EMPTY_STRING))
			{

				filePath = Utilities.generateFilePath((String) applicationConfiguration.getValue(entity), fileName);
			}
			else
			{

				filePath = Utilities.generateFilePath((String) applicationConfiguration.getValue(entity), (fileName
						+ "_" + index));
			}

			if (Utilities.writeImage(filePath, type))
			{

				return filePath;
			}
			else
			{
				return (JsonConstants.ERROR);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return (JsonConstants.ERROR);
		}
	}

	private void sendKGICollectionsSms(Collection collection, SystemUser user)
	{

		log.info("------- IN Integration , Before Sending SMS  --------");
		try
		{
			if (collection.getMobileNumber() != null
					&& !collection.getMobileNumber().equalsIgnoreCase(Constants.EMPTY_STRING))
			{
				log.info("Sending sms to customer mobile number thorugh Integration");

				callSMSDispatcher(collection.getAppropriateAmount() + "", collection.getReceiptNumber(),
						collection.getPaymentMode(), collection.getMobileNumber(), collection.getAppl(),
						collection.getBusinessPartnerNumber(), user, communicationActivityService, collection);
			}

			if (collection.getMobileNumberNew() != null
					&& !collection.getMobileNumberNew().equalsIgnoreCase(Constants.EMPTY_STRING)
					&& !collection.getMobileNumberNew().equalsIgnoreCase(collection.getMobileNumber()))
			{
				log.info("Sending sms to customer alternate mobile number through Integration");

				callSMSDispatcher(collection.getAppropriateAmount() + "", collection.getReceiptNumber(),
						collection.getPaymentMode(), collection.getMobileNumberNew(), collection.getAppl(),
						collection.getBusinessPartnerNumber(), collection.getUser(), communicationActivityService,
						collection);
			}

			if (collection.getUser().getMobileNumber() != null
					&& !collection.getUser().getMobileNumber().equalsIgnoreCase(Constants.EMPTY_STRING))
			{
				log.info("Sending sms to FE mobile number through Integration");

				generateSMSMapForKGIFE(collection.getAppropriateAmount() + "", collection.getReceiptNumber(),
						collection.getPaymentMode(), collection.getUser().getMobileNumber(), collection.getAppl(),
						collection.getPartyName(), collection.getUser(), communicationActivityService, collection);

			}

		/*
			 * if (collection.getUser().getSupervisorMobileNumber() != null &&
			 * !collection.getUser().getSupervisorMobileNumber()
			 * .equalsIgnoreCase(Constants.EMPTY_STRING)) { log.info(
			 * "Sending sms to Supervisor mobile number through Integration");
			 * 
			 * generateSMSDispatcherMapForFE(collection.getAppropriateAmount() +
			 * "", collection.getReceiptNumber(), collection.getPaymentMode(),
			 * collection.getUser() .getSupervisorMobileNumber(),
			 * collection.getAppl(), collection.getPartyName(),
			 * collection.getUser(), communicationActivityService, collection);
			 * 
			 * }
			 */
		}
		catch (Exception e)
		{
			log.info("There is some error occured while sending sms to customer.In Integration" + e);
		}

	}

	private void callSMSDispatcher(String amount, String receiptNumber, String paymentType, String mobileNumber,
			String type, String apacCardNumber, SystemUser user,
			CommunicationActivityService communicationActivityService, Collection collection)
	{
		log.info("---- Inside callSMSDispatcher --------");

		String webserviceUrl = "";

		if (type.equalsIgnoreCase("RSM"))
		{

			webserviceUrl = (String) applicationConfiguration.getValue("RSM_WEB_SERVICE_URL_SMS_DISPATCHER");
		}

		else
		{

			webserviceUrl = (String) applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");

		}

		/* Collections acknowledgment SMS */

		Map<String, Object> smsDispatcherMap = ServerUtilities.generateSMSMapForKGICust(amount, receiptNumber,
				paymentType, mobileNumber, type, apacCardNumber,collection.getRegNo());

		StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true, new HashMap<String, String>());

		log.info("----- xmlRequest : -------" + xmlRequest);

		CommunicationActivityAddition communicationActivityAddition = new CommunicationActivityAddition(user
				.getUserTableId().toString(), user.getImeiNo(), (type + "_" + collection.getCollectionType()),
				webserviceUrl, xmlRequest.toString(), communicationActivityService,
				ActivityLoggerConstants.DATABASE_MSSQL);

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

			log.info("----- Result of SMS Dispatch : -------" + result);
		}
		else
		{
			communicationActivity.setResponse(ActivityLoggerConstants.EMPTY_STRING);

			CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
					communicationActivity, (ActivityLoggerConstants.STATUS_FAILURE), communicationActivityService);

			new Thread(communicationActivityStatusUpdate).run();

			log.info("----- Failure in sending SMS : -------");
		}

		/*  OLD Denomination acknowledgment SMS  */

		/*if (collection.getPaymentMode().equalsIgnoreCase("CSH"))
		{

			smsDispatcherMap = ServerUtilities.generateSMSForOldDenominination(receiptNumber, mobileNumber, collection);

			xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true, new HashMap<String, String>());

			log.info("----- xmlRequest for Old Denomination SMS: -------" + xmlRequest);

			communicationActivityAddition = new CommunicationActivityAddition(user.getUserTableId().toString(),
					user.getImeiNo(), (type + "_" + collection.getCollectionType()), webserviceUrl,
					xmlRequest.toString(), communicationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);

			new Thread(communicationActivityAddition).run();

			kotakCollectionWebserviceAdapter = new KotakCollectionWebserviceAdapter();

			xmlResponse = kotakCollectionWebserviceAdapter.callWebserviceAndGetXmlString(xmlRequest.toString(),
					webserviceUrl);

			communicationActivity = communicationActivityAddition.extractCommunicationActivity();

			if (null != xmlResponse && !xmlResponse.equals(""))
			{
				communicationActivity.setResponse(xmlResponse);

				CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
						communicationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), communicationActivityService);

				new Thread(communicationActivityStatusUpdate).run();

				result = XMLToMap.convertXMLToMap(xmlResponse);

				log.info("----- Result of Old Denomination SMS Dispatch : -------" + result);
			}
			else
			{
				communicationActivity.setResponse(ActivityLoggerConstants.EMPTY_STRING);

				CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
						communicationActivity, (ActivityLoggerConstants.STATUS_FAILURE), communicationActivityService);

				new Thread(communicationActivityStatusUpdate).run();

				log.info("----- Failure in sending Old Denomination SMS : -------");
			}
		}
		*/
	}

	private void generateSMSMapForKGIFE(String amount, String receiptNumber, String paymentType,
			String mobileNumber, String type, String customerName, SystemUser user,
			CommunicationActivityService communicationActivityService, Collection collection)
	{
		log.info("---- Inside generateSMSDispatcherMapForFE --------");

		String webserviceUrl = (String) applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");

		Map<String, Object> smsDispatcherMap = ServerUtilities.generateSMSMapForKGIFE(amount, receiptNumber,
				paymentType, mobileNumber, type, customerName);

		StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true, new HashMap<String, String>());

		log.info("----- xmlRequest : -------" + xmlRequest);

		CommunicationActivityAddition communicationActivityAddition = new CommunicationActivityAddition(user
				.getUserTableId().toString(), user.getImeiNo(), (type + "_" + collection.getCollectionType()),
				webserviceUrl, xmlRequest.toString(), communicationActivityService,
				ActivityLoggerConstants.DATABASE_MSSQL);

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

			log.info("----- Result of SMS Dispatch : -------" + result);
		}
		else
		{
			communicationActivity.setResponse(ActivityLoggerConstants.EMPTY_STRING);

			CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
					communicationActivity, (ActivityLoggerConstants.STATUS_FAILURE), communicationActivityService);

			new Thread(communicationActivityStatusUpdate).run();

			log.info("----- Failure in sending SMS : -------");
		}
	}

	private void callEmailService(Collection collection)
	{
		try
		{
			if (collection.getEmailAddress().equals(Constants.EMPTY_STRING)
					&& collection.getEmailAddressNew().equals(Constants.EMPTY_STRING))
			{
				log.info(" -------- No Email Address found for Collection -------- ");
			}
			else
			{
				log.info("--- Sending Email ---");

				String payMode = collection.getPaymentMode();
				log.info(" -------- payMode -------- " + payMode);

				if (payMode.equals(Constants.PAYMENT_MODE_CASH))
				{
					sendEmailForCashPayment(collection);
				}

				if (payMode.equals(Constants.PAYMENT_MODE_CHEQUE))
				{
					sendEmailForChequePayment(collection);
				}

				if (payMode.equalsIgnoreCase(Constants.PAYMENT_MODE_PDC)
						|| payMode.equalsIgnoreCase(Constants.PAYMENT_MODE_DRAFT))
				{
					sendEmailForDDPDC(collection);
				}
			}
		}
		catch (Exception e)
		{
			log.info("-------Error Occured in sending Email---------", e);
			e.printStackTrace();
		}
	}

	private void sendEmailForCashPayment(Collection collection) throws ParseException
	{
		log.info("---inside sendEmailForCashPayment---");

		String paymentDate = collection.getDeviceDate();
		log.info("---payment date---" + paymentDate);

		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat format2 = new SimpleDateFormat("dd-MMM-yyyy");
		Date date = format1.parse(paymentDate);
		paymentDate = format2.format(date);

		log.info("---payment date after parsing---" + paymentDate);

		// String email = collection.getEmailAddress();

		String emailText = "";

		if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD))
		{
			log.info("---inside if condition---");
			log.info("----email text----" + simpleMailMessageForCashPaymentCreditCard.getText());
			emailText = String.format(simpleMailMessageForCashPaymentCreditCard.getText(), collection.getName(),
					getFullFormApplType(collection.getAppl()), collection.getBusinessPartnerNumber(),
					collection.getReceiptNumber(), collection.getAppropriateAmount() + "", "" + paymentDate,
					collection.getBusinessPartnerNumber(), getTollFreeNumberForAppl(collection.getAppl()));
			log.info("----emailTest for card ----" + emailText);

		}
		else
		{
			log.info("--- inside else----");
			log.info("--- simpleMailMessageForCashPaymentLoan.getText() ----"
					+ simpleMailMessageForCashPaymentLoan.getText());
			log.info("---- collection.getName()-----" + collection.getName());
			log.info("---- getTollFreeNumberForAppl-----" + getTollFreeNumberForAppl(collection.getAppl()));

			emailText = String.format(simpleMailMessageForCashPaymentLoan.getText(), collection.getName(),
					getFullFormApplType(collection.getAppl()), collection.getBusinessPartnerNumber(),
					collection.getReceiptNumber(), collection.getAppropriateAmount() + "", "" + paymentDate,
					collection.getBusinessPartnerNumber(), getTollFreeNumberForAppl(collection.getAppl()));
			log.info("---- email text is -----" + emailText);

		}

		String email = collection.getEmailAddress() != null ? collection.getEmailAddress() : "";

		log.info("---email----" + email);

		if (collection.getEmailAddress() != null
				&& !collection.getEmailAddress().equalsIgnoreCase(Constants.EMPTY_STRING))
		{
			email = collection.getEmailAddress();
			log.info("---inside if email----" + email);

			if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD))
			{
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForCashPaymentCreditCard.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection
						.getUser().getUserTableId().toString(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL,
						senderList, receiverList, simpleMailMessageForCashPaymentCreditCard.getSubject(), emailText,
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
				log.info("simpleMailMessageForCashPaymentLoan.getFrom()"
						+ simpleMailMessageForCashPaymentLoan.getFrom());

				log.info("adding string data into reciverList" + email);
				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				log.info("collection.getUser().getUserTableId()" + collection.getUser());
				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection
						.getUser().getUserTableId().toString(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL,
						senderList, receiverList, simpleMailMessageForCashPaymentLoan.getSubject(), emailText,
						notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);

				log.info("notificationActivityAddition" + notificationActivityAddition);
				new Thread(notificationActivityAddition).run();

				if (emailService == null)
				{
					log.info("emailService is null");
				}
				else
				{
					log.info("emailService is not null");
					log.info("email is : " + email);
				}
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
			email = collection.getEmailAddressNew();
			if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD))
			{
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForCashPaymentCreditCard.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection
						.getUser().getUserTableId().toString(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL,
						senderList, receiverList, simpleMailMessageForCashPaymentCreditCard.getSubject(), emailText,
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

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection
						.getUser().getUserTableId().toString(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL,
						senderList, receiverList, simpleMailMessageForCashPaymentLoan.getSubject(), emailText,
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

	private void sendEmailForChequePayment(Collection collection) throws ParseException
	{
		String paymentDate = collection.getDeviceDate();
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat format2 = new SimpleDateFormat("dd-MMM-yyyy");
		Date date = format1.parse(paymentDate);
		paymentDate = format2.format(date);

		String email = collection.getEmailAddress() != null ? collection.getEmailAddress() : "";
		;
		String chequeDetailString = "";

		NumberFormat formatter = NumberFormat.getInstance(new Locale("en", "IN"));

		for (Cheque cheque : collection.getChequeDetails())
		{
			chequeDetailString = chequeDetailString + " Cheque No." + cheque.getChequeNo() + "     dated"
					+ cheque.getChequeDate();
		}

		String emailText = "";
		if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD))
		{
			emailText = String.format(simpleMailMessageForChequePaymentCreditCard.getText(), collection.getName(),
					getFullFormApplType(collection.getAppl()), collection.getBusinessPartnerNumber(),
					collection.getReceiptNumber(), collection.getAppropriateAmount() + "", "" + paymentDate, ""
							+ chequeDetailString, collection.getBusinessPartnerNumber(),
					getTollFreeNumberForAppl(collection.getAppl()));
		}
		else
		{
			emailText = String.format(simpleMailMessageForChequePaymentLoan.getText(), collection.getName(),
					getFullFormApplType(collection.getAppl()), collection.getBusinessPartnerNumber(),
					collection.getReceiptNumber(), collection.getAppropriateAmount() + "", "" + paymentDate, ""
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

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection
						.getUser().getUserTableId().toString(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL,
						senderList, receiverList, simpleMailMessageForChequePaymentCreditCard.getSubject(), emailText,
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

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection
						.getUser().getUserTableId().toString(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL,
						senderList, receiverList, simpleMailMessageForChequePaymentLoan.getSubject(), emailText,
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

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection
						.getUser().getUserTableId().toString(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL,
						senderList, receiverList, simpleMailMessageForChequePaymentCreditCard.getSubject(), emailText,
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

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection
						.getUser().getUserTableId().toString(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL,
						senderList, receiverList, simpleMailMessageForChequePaymentLoan.getSubject(), emailText,
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

	private void sendEmailForDDPDC(Collection collection) throws ParseException
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

		String emailAmount = collection.getAppropriateAmount() + "";

		DecimalFormat amountFormat = new DecimalFormat("#.00");

		try
		{
			emailAmount = amountFormat.format(Double.parseDouble(emailAmount));
		}
		catch (Exception e)
		{
			emailAmount = "0.00";
		}

		for (Cheque cheque : collection.getChequeDetails())
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

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection
						.getUser().getUserTableId().toString(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL,
						senderList, receiverList, simpleMailMessageForDDPDC.getSubject(), emailText,
						notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);

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

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection
						.getUser().getUserTableId().toString(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL,
						senderList, receiverList, simpleMailMessageForDDPDC.getSubject(), emailText,
						notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);
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

}
