package com.mobicule.mcollections.integration.kgi;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.mobicule.component.activitylogger.beans.UserActivity;
import com.mobicule.component.activitylogger.commons.ActivityLoggerConstants;
import com.mobicule.component.activitylogger.service.CommunicationActivityService;
import com.mobicule.component.activitylogger.service.UserActivityService;
import com.mobicule.component.activitylogger.threads.UserActivityAddition;
import com.mobicule.component.activitylogger.threads.UserActivityStatusUpdate;
import com.mobicule.mcollections.core.beans.KGIConstant;
import com.mobicule.mcollections.core.beans.KGIRenewalCases;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.KGIService;
import com.mobicule.mcollections.core.service.SystemUserService;
import com.mobicule.mcollections.integration.commons.ServerUtilities;
import com.mobicule.mcollections.integration.commons.XMLConstants;
import com.mobicule.mcollections.webservice.adapter.KotakCollectionWebserviceAdapter;

public class RenewalCasesSearchServiceImpl implements IRenewalCasesSearchService
{
	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private CommunicationActivityService communicationActivityService;

	@Autowired
	private ApplicationConfiguration<String, String> applicationConfiguration;

	@Autowired
	private KotakCollectionWebserviceAdapter webserviceAdapter;

	@Autowired
	private UserActivityService userActivityService;

	@Autowired
	private SystemUserService systemUserService;

	private KGIService kgiService;

	public KGIService getKgiService()
	{
		return kgiService;
	}

	public void setKgiService(KGIService kgiService)
	{
		this.kgiService = kgiService;
	}

	@Override
	public Message<String> execute(Message<String> message) throws Throwable
	{

		log.info("<----------------- inside RenewalCasesSearchService -------------------->");

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

			searchRenewalCase(responseJSON, requestData, requestEntity, userActivity, communicationActivityService,
					systemUser);

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

	private void searchRenewalCase(JSONObject responseJSON, JSONObject requestData, String entity,
			UserActivity userActivity, CommunicationActivityService communicationActivityService, SystemUser systemUser)
			throws JSONException
	{

		try
		{
			Map<String, Object> wrapperMap = new HashMap<String, Object>();

			long reqUID = createSearchRequestMap(requestData, entity, wrapperMap);
			KGIRenewalCases kgiRenewalCase = new KGIRenewalCases();

			Map<String, Object> webserviceResponseMap = new HashMap<String, Object>();
			try
			{

				kgiRenewalCase = kgiService.getKGIRenewalData(requestData.getString(JsonConstants.UNIQUE_ID),entity);

				log.info("---- kgiRenewalCase.getApacNumber() ---" + kgiRenewalCase.getApacNumber());

				if (kgiRenewalCase.getApacNumber() != null)
				{
					
					try {
					
					webserviceResponseMap = webserviceAdapter.callWebserviceAndGetMap(wrapperMap,
							applicationConfiguration.getValue("WEB_SERVICE_URL_MCORE"), userActivity,
							communicationActivityService);
					
					//String messageType = XMLConstants.MC0010;
					String messageType = XMLConstants.MC002;
					

					Map responseMap = (Map) webserviceResponseMap.get(messageType);
					Map dataMap = (Map) responseMap.get(XMLConstants.RESPONSE_DETAILS);

					log.info("");
					
					if (dataMap.containsKey(XMLConstants.PARTY_ID))
					{
						kgiRenewalCase.setPartyId(dataMap.get(XMLConstants.PARTY_ID).toString());
						
					}
					
					}
					catch(Exception e) {
						
						log.error("---Exception details is ----",e);
						
					}
					
					createSearchResponseJsonThroughKGITable(kgiRenewalCase, responseJSON, entity, systemUser.getUserTableId());
					

				}
				else
				{/*
					   log.info("--- Renewal Data is not avaible --- ");
					

					webserviceResponseMap = webserviceAdapter.callWebserviceAndGetMap(wrapperMap,
							applicationConfiguration.getValue("WEB_SERVICE_URL_MCORE"), userActivity,
							communicationActivityService);

					log.info("----webserviceResponseMap-----" + webserviceResponseMap);

					if (webserviceResponseMap == null)
					{
						responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
						responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_RENEWAL_CASES_SEARCH_FAILURE);
						responseJSON.put(JsonConstants.DATA, "");
						UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
								(ActivityLoggerConstants.STATUS_FAILURE), userActivityService);
						new Thread(userActivityStatusUpdate).run();
					}
					else
					{
						parseResponseMap(responseJSON, webserviceResponseMap, entity, reqUID,
								systemUser.getUserTableId());

						UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
								(ActivityLoggerConstants.STATUS_SUCCESS), userActivityService);
						new Thread(userActivityStatusUpdate).run();
					}

				*/}

			}
			catch (Exception e)
			{

				log.error("---Exception while searching the RollOver cases ----", e);
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

	private void parseResponseMap(JSONObject responseJSON, Map<String, Object> webserviceResponseMap, String entity,
			long reqUID, long userId) throws JSONException
	{
		String messageType = null;

		messageType = XMLConstants.MC002;
		//messageType = XMLConstants.MC0010;

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
				responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_RENEWAL_CASES_SEARCH_FAILURE);
				responseJSON.put(JsonConstants.DATA, "");
			}
		}
	}

	private void createSearchResponseJson(Map dataMap, JSONObject responseJSON, String entity, long userId)
			throws JSONException
	{
		JSONObject dataJSOn = new JSONObject();

		StringBuilder makeModel = new StringBuilder("");

		KGIConstant kgiConstant = kgiService.getKGIConstantValues();
		
		  KGIConstant kgiConst=kgiService.getKGIConfigurationValues("KGI");
		  
		  log.info("KGI Constant ####"+kgiConst);

		/*else
		{

		dataJSOn.put(JsonConstants.CA, dataMap.get(XMLConstants.PARTY_ID));

		dataJSOn.put(JsonConstants.NAME, dataMap.get(XMLConstants.PARTY_NAME));
		}*/

		if (dataMap.containsKey(XMLConstants.PARTY_ID))
		{
			//dataJSOn.put(JsonConstants.PARTY_ID, kgiConst.getConfigurationPartyId());
			
			dataJSOn.put(JsonConstants.PARTY_ID, dataMap.get(XMLConstants.PARTY_ID));
			
		}
		if (dataMap.containsKey(XMLConstants.PARTY_NAME))
		{
			dataJSOn.put(JsonConstants.INSURED_NAME, dataMap.get(XMLConstants.PARTY_NAME));
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
		/*if (dataMap.containsKey(XMLConstants.EMI_DUEDATE))
		{
			dataJSOn.put(JsonConstants.DUE_DATE, dataMap.get(XMLConstants.EMI_DUEDATE));
		}*/

		/*if (dataMap.containsKey(XMLConstants.PENAL_AMNT))
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
		}*/
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

		/*if (dataMap.containsKey(XMLConstants.ASSET_ID))
		{
			dataJSOn.put(JsonConstants.ASSET_ID, dataMap.get(XMLConstants.ASSET_ID));
		}*/

		if (dataMap.containsKey(XMLConstants.POLICY_NUMBER))
		{
			dataJSOn.put(JsonConstants.POLICY_NUMBER, dataMap.get(XMLConstants.POLICY_NUMBER));
		}

		/*  if (dataMap.containsKey(XMLConstants.POLICY_DATE))
		{
			dataJSOn.put(JsonConstants.POLICY_DATE, dataMap.get(XMLConstants.POLICY_DATE));
		}*/

		if (dataMap.containsKey(XMLConstants.POLICY_EXPIARY_DATE))
		{
			dataJSOn.put(JsonConstants.EXPIRY_DATE, dataMap.get(XMLConstants.POLICY_EXPIARY_DATE));
		}

		/*if (dataMap.containsKey(XMLConstants.INSURED_VALUE))
		{
			dataJSOn.put(JsonConstants.INSURED_VALUE, dataMap.get(XMLConstants.INSURED_VALUE));
		}*/

		if (dataMap.containsKey(XMLConstants.PREMIUM_AMOUNT))
		{
			dataJSOn.put(JsonConstants.PREMIUM_AMOUNT, dataMap.get(XMLConstants.PREMIUM_AMOUNT));
		}
		/*
				if (dataMap.containsKey(XMLConstants.BENIFICIARY))
				{
					dataJSOn.put(JsonConstants.BENIFICIARY, dataMap.get(XMLConstants.BENIFICIARY));
				}*/

		if (dataMap.containsKey(XMLConstants.INSURER))
		{
			dataJSOn.put(JsonConstants.INSURER, dataMap.get(XMLConstants.INSURER));
		}

		/*if (dataMap.containsKey(XMLConstants.INSURER_PARTY_ID))
		{
			dataJSOn.put(JsonConstants.INSURER_PARTY_ID, dataMap.get(XMLConstants.INSURER_PARTY_ID));
		}*/

		if (dataMap.containsKey(XMLConstants.CHASSIS))
		{
			dataJSOn.put(JsonConstants.CHASSIS_NO, dataMap.get(XMLConstants.CHASSIS));
		}

		/*		if (dataMap.containsKey(XMLConstants.ENGNO))
				{
					dataJSOn.put(JsonConstants.ENGINE_NO, dataMap.get(XMLConstants.ENGNO));
				}*/

		if (dataMap.containsKey(XMLConstants.MAKE))
		{

			makeModel.append(dataMap.get(XMLConstants.MAKE));
			//dataJSOn.put(JsonConstants.MAKE, dataMap.get(XMLConstants.MAKE));
		}

		if (dataMap.containsKey(XMLConstants.MODEL))
		{

			makeModel.append(dataMap.get(XMLConstants.MODEL));
			//dataJSOn.put(JsonConstants.MODEL, dataMap.get(XMLConstants.MODEL));
		}

		dataJSOn.put(JsonConstants.MAKE_MODEL, makeModel.toString());

		/*if (dataMap.containsKey(XMLConstants.INVOICE))
		{
			dataJSOn.put(JsonConstants.INVOICE, dataMap.get(XMLConstants.INVOICE));
		}*/

		if (dataMap.containsKey(XMLConstants.INVOICE_DATE))
		{
			dataJSOn.put(JsonConstants.INVOICE_DATE, dataMap.get(XMLConstants.INVOICE_DATE));
		}

		if (dataMap.containsKey(XMLConstants.INVOICE_VAL))
		{
				dataJSOn.put(JsonConstants.INVOICE_VALUE, dataMap.get(XMLConstants.INVOICE_VAL));

			//dataJSOn.put(JsonConstants.INVOICE_VALUE, "300000.0");
		}

		/*		if (dataMap.containsKey(XMLConstants.SALES_TAX_AMOUNT))
				{
					dataJSOn.put(JsonConstants.SALES_TAX_AMOUNT, dataMap.get(XMLConstants.SALES_TAX_AMOUNT));
				}

				if (dataMap.containsKey(XMLConstants.SUPPLIER))
				{
					dataJSOn.put(JsonConstants.SUPPLIER, dataMap.get(XMLConstants.SUPPLIER));
				}

				if (dataMap.containsKey(XMLConstants.FIRST_DATE))
				{
					dataJSOn.put(JsonConstants.FR_STATE, dataMap.get(XMLConstants.FIRST_DATE));
				}

				if (dataMap.containsKey(XMLConstants.MODEL2))
				{
					dataJSOn.put(JsonConstants.MODEL_2, dataMap.get(XMLConstants.MODEL2));
				}

				if (dataMap.containsKey(XMLConstants.ASTEST_VALUE))
				{
					dataJSOn.put(JsonConstants.ASTEST_VALUE, dataMap.get(XMLConstants.ASTEST_VALUE));
				}

				if (dataMap.containsKey(XMLConstants.ASTEST_DATE))
				{
					dataJSOn.put(JsonConstants.ASTEST_DATE, dataMap.get(XMLConstants.ASTEST_DATE));
				}

				if (dataMap.containsKey(XMLConstants.ASTEST_DESC))
				{
					dataJSOn.put(JsonConstants.ASTEST_DESC, dataMap.get(XMLConstants.ASTEST_DESC));
				}*/

		dataJSOn.put(JsonConstants.OD_PREMIUM_RATE,
				kgiConstant.getOdPremiumRate() == null ? "" : kgiConstant.getOdPremiumRate());

		dataJSOn.put(JsonConstants.TP_PREMIUM,
				kgiConstant.getTpPremiumRate() == null ? "" : kgiConstant.getTpPremiumRate());

		dataJSOn.put(JsonConstants.OWNER_DRIVER_PA,
				kgiConstant.getOwnerDriverPa() == null ? "" : kgiConstant.getOwnerDriverPa());

		dataJSOn.put(JsonConstants.LIABILITY_TO_DRIVER, kgiConstant.getLegalLiabilityToDriver() == null ? ""
				: kgiConstant.getLegalLiabilityToDriver());

		dataJSOn.put(JsonConstants.SERVICE_TAX, kgiConstant.getServiceTax() == null ? "" : kgiConstant.getServiceTax());

		dataJSOn.put(JsonConstants.NCB_PERCENTAGE, Constants.EMPTY_STRING);

		dataJSOn.put(JsonConstants.IDV, Constants.EMPTY_STRING);
		
		dataJSOn.put(JsonConstants.IS_CALCULATED,Constants.NO);

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

	private void createSearchResponseJsonThroughKGITable(KGIRenewalCases renewalCases, JSONObject responseJSON,
			String entity, long userId) throws JSONException
	{
		JSONObject dataJSOn = new JSONObject();

		StringBuilder makeModel = new StringBuilder("");

		
		KGIConstant kgiConstant = kgiService.getKGIConstantValues();
		
		  KGIConstant kgiConst=kgiService.getKGIConfigurationValues("KGI");
		  
		
		  log.info("KGI Constant ####"+kgiConst);

		
		  dataJSOn.put(JsonConstants.PARTY_ID, renewalCases.getPartyId() == null ? "" : renewalCases.getPartyId() );
		  //dataJSOn.put(JsonConstants.PARTY_ID, kgiConst.getConfigurationPartyId() == null ? "" : kgiConst.getConfigurationPartyId());

		dataJSOn.put(JsonConstants.INSURED_NAME,
				renewalCases.getProsperInsuredName() == null ? "" : renewalCases.getProsperInsuredName());

		dataJSOn.put(XMLConstants.REGISTRATION_NUMBER,
				renewalCases.getRegiNumber() == null ? "" : renewalCases.getRegiNumber());
		dataJSOn.put(JsonConstants.APPL, renewalCases.getApplication() == null ? "" : renewalCases.getApplication());
		dataJSOn.put(JsonConstants.APAC_NO, renewalCases.getApacNumber() == null ? "" : renewalCases.getApacNumber());

		/*if (dataMap.containsKey(XMLConstants.EMI_DUEDATE))
		{
			dataJSOn.put(JsonConstants.DUE_DATE, dataMap.get(XMLConstants.EMI_DUEDATE));
		}*/

		/*if (dataMap.containsKey(XMLConstants.PENAL_AMNT))
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
		}*/
		dataJSOn.put(JsonConstants.MOBILE_NUMBER,
				renewalCases.getMobileNumber() == null ? "" : renewalCases.getMobileNumber());
		dataJSOn.put(JsonConstants.EMAIL_ADDRESS,
				renewalCases.getEmailAddress() == null ? "" : renewalCases.getEmailAddress());
		dataJSOn.put(JsonConstants.LANDLINE_NUMBER,
				renewalCases.getLandlineNumber() == null ? "" : renewalCases.getLandlineNumber());
		dataJSOn.put(JsonConstants.CORRESPONDENCE_ADDRESS,
				renewalCases.getAddress() == null ? "" : renewalCases.getAddress());

		/*if (dataMap.containsKey(XMLConstants.ASSET_ID))
		{
			dataJSOn.put(JsonConstants.ASSET_ID, dataMap.get(XMLConstants.ASSET_ID));
		}*/

		dataJSOn.put(JsonConstants.POLICY_NUMBER,
				renewalCases.getPolicyNumber() == null ? "" : renewalCases.getPolicyNumber());

		/*  if (dataMap.containsKey(XMLConstants.POLICY_DATE))
		{
			dataJSOn.put(JsonConstants.POLICY_DATE, dataMap.get(XMLConstants.POLICY_DATE));
		}*/

		dataJSOn.put(JsonConstants.EXPIRY_DATE,
				renewalCases.getExpiryDate() == null ? "" : renewalCases.getExpiryDate());
		
		
		dataJSOn.put(JsonConstants.ENGINE_NO,
				renewalCases.getEngineNumber() == null ? "" : renewalCases.getEngineNumber());

		/*if (dataMap.containsKey(XMLConstants.INSURED_VALUE))
		{
			dataJSOn.put(JsonConstants.INSURED_VALUE, dataMap.get(XMLConstants.INSURED_VALUE));
		}*/

		dataJSOn.put(JsonConstants.PREMIUM_AMOUNT,
				renewalCases.getPremiumAmount() == null ? "" : renewalCases.getPremiumAmount());

		/*
				if (dataMap.containsKey(XMLConstants.BENIFICIARY))
				{
					dataJSOn.put(JsonConstants.BENIFICIARY, dataMap.get(XMLConstants.BENIFICIARY));
				}*/

		dataJSOn.put(JsonConstants.INSURER, "");

		/*if (dataMap.containsKey(XMLConstants.INSURER_PARTY_ID))
		{
			dataJSOn.put(JsonConstants.INSURER_PARTY_ID, dataMap.get(XMLConstants.INSURER_PARTY_ID));
		}*/

		dataJSOn.put(JsonConstants.CHASSIS_NO,
				renewalCases.getChassisNumber() == null ? "" : renewalCases.getChassisNumber());

		/*		if (dataMap.containsKey(XMLConstants.ENGNO))
				{
					dataJSOn.put(JsonConstants.ENGINE_NO, dataMap.get(XMLConstants.ENGNO));
				}*/

		//	makeModel.append(dataMap.get(XMLConstants.MAKE));
		//dataJSOn.put(JsonConstants.MAKE, dataMap.get(XMLConstants.MAKE));

		//makeModel.append(dataMap.get(XMLConstants.MODEL));
		//dataJSOn.put(JsonConstants.MODEL, dataMap.get(XMLConstants.MODEL));

		dataJSOn.put(JsonConstants.MAKE_MODEL,
				renewalCases.getModelVariant() == null ? "" : renewalCases.getModelVariant());

		/*if (dataMap.containsKey(XMLConstants.INVOICE))
		{
			dataJSOn.put(JsonConstants.INVOICE, dataMap.get(XMLConstants.INVOICE));
		}*/

		dataJSOn.put(JsonConstants.INVOICE_DATE, "");
		//	dataJSOn.put(JsonConstants.INVOICE_VALUE, dataMap.get(XMLConstants.INVOICE_VAL));

		dataJSOn.put(JsonConstants.INVOICE_VALUE, "");

		/*		if (dataMap.containsKey(XMLConstants.SALES_TAX_AMOUNT))
				{
					dataJSOn.put(JsonConstants.SALES_TAX_AMOUNT, dataMap.get(XMLConstants.SALES_TAX_AMOUNT));
				}

				if (dataMap.containsKey(XMLConstants.SUPPLIER))
				{
					dataJSOn.put(JsonConstants.SUPPLIER, dataMap.get(XMLConstants.SUPPLIER));
				}

				if (dataMap.containsKey(XMLConstants.FIRST_DATE))
				{
					dataJSOn.put(JsonConstants.FR_STATE, dataMap.get(XMLConstants.FIRST_DATE));
				}

				if (dataMap.containsKey(XMLConstants.MODEL2))
				{
					dataJSOn.put(JsonConstants.MODEL_2, dataMap.get(XMLConstants.MODEL2));
				}

				if (dataMap.containsKey(XMLConstants.ASTEST_VALUE))
				{
					dataJSOn.put(JsonConstants.ASTEST_VALUE, dataMap.get(XMLConstants.ASTEST_VALUE));
				}

				if (dataMap.containsKey(XMLConstants.ASTEST_DATE))
				{
					dataJSOn.put(JsonConstants.ASTEST_DATE, dataMap.get(XMLConstants.ASTEST_DATE));
				}

				if (dataMap.containsKey(XMLConstants.ASTEST_DESC))
				{
					dataJSOn.put(JsonConstants.ASTEST_DESC, dataMap.get(XMLConstants.ASTEST_DESC));
				}*/

		dataJSOn.put(JsonConstants.OD_PREMIUM_RATE,
				kgiConstant.getOdPremiumRate() == null ? "" : kgiConstant.getOdPremiumRate());

		dataJSOn.put(JsonConstants.TP_PREMIUM,
				kgiConstant.getTpPremiumRate() == null ? "" : kgiConstant.getTpPremiumRate());

		dataJSOn.put(JsonConstants.OWNER_DRIVER_PA,
				kgiConstant.getOwnerDriverPa() == null ? "" : kgiConstant.getOwnerDriverPa());

		dataJSOn.put(JsonConstants.LIABILITY_TO_DRIVER, kgiConstant.getLegalLiabilityToDriver() == null ? ""
				: kgiConstant.getLegalLiabilityToDriver());

		dataJSOn.put(JsonConstants.SERVICE_TAX, kgiConstant.getServiceTax() == null ? "" : kgiConstant.getServiceTax());

		dataJSOn.put(JsonConstants.NCB_PERCENTAGE, renewalCases.getNcbPercentage() == null ? "" : renewalCases.getNcbPercentage());

		dataJSOn.put(JsonConstants.IDV, renewalCases.getIdv() == null ? "" : renewalCases.getIdv());
		
		dataJSOn.put(JsonConstants.IS_CALCULATED,Constants.YES);

		responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
		responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_RANDOM_COLLECTION_SEARCH_SUCCESS);
		responseJSON.put(JsonConstants.DATA, dataJSOn);

	}

}
