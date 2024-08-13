package com.mobicule.mcollections.integration.collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.mobicule.component.activitylogger.beans.CommunicationActivity;
import com.mobicule.component.activitylogger.beans.UserActivity;
import com.mobicule.component.activitylogger.commons.ActivityLoggerConstants;
import com.mobicule.component.activitylogger.service.CommunicationActivityService;
import com.mobicule.component.activitylogger.service.UserActivityService;
import com.mobicule.component.activitylogger.threads.CommunicationActivityAddition;
import com.mobicule.component.activitylogger.threads.CommunicationActivityStatusUpdate;
import com.mobicule.component.activitylogger.threads.UserActivityAddition;
import com.mobicule.component.activitylogger.threads.UserActivityStatusUpdate;
import com.mobicule.component.devicesync.commons.DeviceSyncConstants;
import com.mobicule.component.mapconversion.json.MapToJSON;
import com.mobicule.component.mapconversion.xml.MapToXML;
import com.mobicule.component.mapconversion.xml.XMLToMap;
import com.mobicule.mcollections.core.ResponseSMSPayment;
import com.mobicule.mcollections.core.beans.Collection;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.AesUtil;
import com.mobicule.mcollections.core.commons.AllPayUtility;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.UpiUtility;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.CaseService;
import com.mobicule.mcollections.core.service.CollectionService;
import com.mobicule.mcollections.core.service.LeadService;
import com.mobicule.mcollections.core.service.SystemUserService;
import com.mobicule.mcollections.integration.commons.ServerUtilities;
import com.mobicule.mcollections.webservice.adapter.KotakCollectionWebserviceAdapter;

public class CollectionSyncService implements ICollectionSyncService
{
	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private SystemUserService systemUserService;

	@Autowired
	private UserActivityService userActivityService;

	@Autowired
	private CollectionService collectionService;	

	@Autowired
	private ApplicationConfiguration applicationConfiguration;


	@Autowired
	private CommunicationActivityService communicationActivityService;

	@Override
	public Message<String> execute(Message<String> message)
			throws JSONException {
		

		String status = JsonConstants.FAILURE;
		
		JSONObject responseJSON = new JSONObject();

		try {
			
			String requestSet = message.getPayload();
			
			String requestEntity = JSONPayloadExtractor.extract(requestSet,
					JsonConstants.ENTITY);
			String requestAction = JSONPayloadExtractor.extract(requestSet,
					JsonConstants.ACTION);
			String requestType = JSONPayloadExtractor.extract(requestSet,
					JsonConstants.TYPE);
			
			String imeiNo = Constants.EMPTY_STRING;
			String draMobileNumber = Constants.EMPTY_STRING;
			String paymentMode=Constants.EMPTY_STRING;
			String businessPartnerNumber=Constants.EMPTY_STRING;
			String appl=Constants.EMPTY_STRING;
			
			JSONObject jsonObject = new JSONObject(requestSet);
			JSONObject user = (JSONObject) jsonObject.get(JsonConstants.SYSTEM_USER);
			JSONObject responseData = (JSONObject) jsonObject.get(JsonConstants.DATA);
			
			String fename=user.getString("firstLastName") == null ? "" : user.getString("firstLastName");
			
			UserActivityAddition userActivityAddition = new UserActivityAddition(
					requestSet, userActivityService,
					ActivityLoggerConstants.DATABASE_MSSQL);

			new Thread(userActivityAddition).run();

			UserActivity userActivity = userActivityAddition
					.extractUserActivity();
			
			Map<String, String> formSendLinkJsonParam = new HashMap<String, String>();
			
			String request = AllPayUtility.generatePayLinkReq(formSendLinkJsonParam);
			
			if(!request.isEmpty())
			{
				String response = AllPayUtility.sendSmsPayLink(request);
			
				if(!response.isEmpty())
				{
					Map<String,Object> responseMap = AllPayUtility.jsonToMap(response);
					
					if(!responseMap.isEmpty())
					{
						if(responseMap.get("status").toString().equalsIgnoreCase("issued"))
						{
							responseJSON.put(JsonConstants.MESSAGE, JsonConstants.SMS_SENT_SUCCESSFULLY);
							responseJSON.put(JsonConstants.DATA, "");
							responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
								
							/*updateActivityMap.put(Constants.AllPayCollectionsDao.ID, smsTableID);				
							updateActivityMap.put(Constants.AllPayCollectionsDao.INVOICE_ID, map.get("invoice_id")); //production
							updateActivityMap.put(Constants.AllPayCollectionsDao.THIRD_PARTY_RESPONSE, responseSMSPayment.getEncryptResponse().getEncResponse());
							updateActivityMap.put(Constants.AllPayCollectionsDao.MODIFIED_BY, systemUserNew.getUserTableId().toString());
							updateActivityMap.put(Constants.AllPayCollectionsDao.THIRD_PARTY_STATUS, status);				
							updateActivityMap.put(Constants.AllPayCollectionsDao.DEVICE_RESPONSE, responseJSON.toString());
							updateActivityMap.put(Constants.AllPayCollectionsDao.DEVICE_RESPONSE_STATUS, status);
							updateActivityMap.put(Constants.AllPayCollectionsDao.RECEIPT_NUMBER, receiptNo);
							collectionService.smsPaymentActivityUpdation(updateActivityMap);*/
						}
						else
						{
							
						}
					}
					else
					{
						log.info("Error in converting Response to map.");				
						
						Map<Object,Object> updateActivityMap= new HashMap<Object,Object>();
						responseJSON.put(JsonConstants.MESSAGE, JsonConstants.FAILURE);
						responseJSON.put(JsonConstants.DATA, Constants.EMPTY_STRING);
						responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);

						UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(
								userActivity,
								(ActivityLoggerConstants.STATUS_FAILURE),
								userActivityService);
						new Thread(userActivityStatusUpdate).run();
						
						status=Constants.AllPayCollectionsDao.FAILURE;
						updateActivityMap.put(Constants.AllPayCollectionsDao.ID,"" );
						
						updateActivityMap.put(Constants.AllPayCollectionsDao.THIRD_PARTY_RESPONSE,"" );
						updateActivityMap.put(Constants.AllPayCollectionsDao.MODIFIED_BY, "");
						updateActivityMap.put(Constants.AllPayCollectionsDao.THIRD_PARTY_STATUS, "");
						collectionService.smsPaymentActivityUpdation(updateActivityMap);
					}
				}
				else
				{
					log.info("Failure Response");				
					
					Map<Object,Object> updateActivityMap= new HashMap<Object,Object>();
					responseJSON.put(JsonConstants.MESSAGE, JsonConstants.FAILURE);
					responseJSON.put(JsonConstants.DATA, Constants.EMPTY_STRING);
					responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);

					UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(
							userActivity,
							(ActivityLoggerConstants.STATUS_FAILURE),
							userActivityService);
					new Thread(userActivityStatusUpdate).run();
					
					status=Constants.AllPayCollectionsDao.FAILURE;
					updateActivityMap.put(Constants.AllPayCollectionsDao.ID,"" );
					
					updateActivityMap.put(Constants.AllPayCollectionsDao.THIRD_PARTY_RESPONSE,"" );
					updateActivityMap.put(Constants.AllPayCollectionsDao.MODIFIED_BY, "");
					updateActivityMap.put(Constants.AllPayCollectionsDao.THIRD_PARTY_STATUS, "");
					collectionService.smsPaymentActivityUpdation(updateActivityMap);
				

				}
			}
			else
			{

				log.info("Failure Request");				
				
				Map<Object,Object> updateActivityMap= new HashMap<Object,Object>();
				responseJSON.put(JsonConstants.MESSAGE, JsonConstants.FAILURE);
				responseJSON.put(JsonConstants.DATA, Constants.EMPTY_STRING);
				responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);

				UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(
						userActivity,
						(ActivityLoggerConstants.STATUS_FAILURE),
						userActivityService);
				new Thread(userActivityStatusUpdate).run();
				
				status=Constants.AllPayCollectionsDao.FAILURE;
				updateActivityMap.put(Constants.AllPayCollectionsDao.ID,"" );
				
				updateActivityMap.put(Constants.AllPayCollectionsDao.THIRD_PARTY_RESPONSE, "");
				updateActivityMap.put(Constants.AllPayCollectionsDao.MODIFIED_BY, "");
				updateActivityMap.put(Constants.AllPayCollectionsDao.THIRD_PARTY_STATUS, "");
				collectionService.smsPaymentActivityUpdation(updateActivityMap);
			
			}
		}
		catch (Exception e) {
			
		}
			
		
		return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
	}

	
	private void sendAllPaySms(Collection collection, SystemUser systemUserNew,String status)
	{
		if (systemUserNew.getMobileNumber() != null
				&& !systemUserNew.getMobileNumber().equalsIgnoreCase(Constants.EMPTY_STRING))
		{
			generateAllPaySMSDispatcherMapForFE(collection.getAppropriateAmount() + Constants.EMPTY_STRING , collection.getReceiptNumber(),
					collection.getPaymentMode(), systemUserNew.getMobileNumber(), collection.getAppl(),
					collection.getPartyName(), systemUserNew, communicationActivityService, collection ,status);

		}

	}
	
	private void generateAllPaySMSDispatcherMapForFE(String amount, String receiptNumber, String paymentType,
			String mobileNumber, String type, String feName, SystemUser user,
			CommunicationActivityService communicationActivityService, Collection collection ,String status)
	{

		String webserviceUrl = (String) applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");

		Map<String, Object> smsDispatcherMap = ServerUtilities.generateAllPaySMSDispatcherMapForFE(amount, receiptNumber,
				paymentType, mobileNumber, collection.getMobileNumber(), feName,status);

		StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true, new HashMap<String, String>());

		
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

		if (null != xmlResponse && !xmlResponse.equals(Constants.EMPTY_STRING ))
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

	
	private void sendAllPaySMSToCustomerAfterSubmittingReceipt(Collection collection,
			String status,String draMobileNumber,Long userTableId,String imeiNo)
			
			
	{

		SystemUser systemUser=new SystemUser();
		systemUser.setImeiNo(imeiNo);
		systemUser.setUserTableId(userTableId);
		
		
		if (collection.getContact() != null
				&& !collection.getContact().equalsIgnoreCase(Constants.EMPTY_STRING))
		{
			log.info("Sending SMS on customer number " + collection.getContact());

			generateSMSToCustomerOnSubmittingReceiptForAllPay(collection.getAmount(), collection.getReceiptNumber(),
					collection.getPaymentMode(), collection.getContact(), collection.getAppl(),
					collection.getFeName(), systemUser, communicationActivityService, collection, status);

		}

		if (draMobileNumber != null
				&& !draMobileNumber.equalsIgnoreCase(Constants.EMPTY_STRING))
		{
			log.info("Sending sms to DRA mobile number " + collection.getAmount());

			generateSMSToDRAOnSubmittingReceiptForAllPay(collection.getAmount(), collection.getReceiptNumber(),
					collection.getPaymentMode(), draMobileNumber, collection.getAppl(),
					collection.getFeName(), systemUser, communicationActivityService, collection, status);

		}
	}
	
	private void generateSMSToCustomerOnSubmittingReceiptForAllPay(String amount, String receiptNumber,
			String paymentType, String mobileNumber, String type, String feName, SystemUser user,
			CommunicationActivityService communicationActivityService, Collection collection, String status)
	{
		log.info("---- Inside generateSMSToCustomerOnSubmittingReceiptForAllPay --------");

		String webserviceUrl = (String) applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");

		Map<String, Object> smsDispatcherMap = ServerUtilities.generateAllPaySMSToCustomerOnSubmittingReceiptForAllPay(
				amount, receiptNumber, paymentType, mobileNumber, type, feName, status, collection);

		log.info("---- Inside smsDispatcherMap --------" + smsDispatcherMap);

		StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true, new HashMap<String, String>());

		log.info("---- Inside xmlRequest --------" + xmlRequest);

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

		if (null != xmlResponse && !xmlResponse.equals(Constants.EMPTY_STRING))
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

			log.info("----- Failure in sending SMS : -------");
		}
	}

	private void generateSMSToDRAOnSubmittingReceiptForAllPay(String amount, String receiptNumber, String paymentType,
			String mobileNumber, String type, String feName, SystemUser user,
			CommunicationActivityService communicationActivityService, Collection collection, String status)
	{
		log.info("---- Inside generateAllPaySMSOnSubmittingReceiptForAllPay --------");

		String webserviceUrl = (String) applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");

		Map<String, Object> smsDispatcherMap = ServerUtilities.generateSMSToDRAOnSubmittingReceiptForAllPay(amount,
				receiptNumber, paymentType, mobileNumber, type, feName, status, collection);

		log.info("---- Inside smsDispatcherMap --------" + smsDispatcherMap);

		StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true, new HashMap<String, String>());

		log.info("---- Inside xmlRequest --------" + xmlRequest);

		//log.info("----- xmlRequest : -------" + xmlRequest);
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

		if (null != xmlResponse && !xmlResponse.equals(Constants.EMPTY_STRING))
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

			log.info("----- Failure in sending SMS : -------");
		}
	}
	

}
