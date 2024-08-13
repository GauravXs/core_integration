package com.mobicule.mcollections.integration.collection;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.mobicule.component.activitylogger.beans.UserActivity;
import com.mobicule.component.activitylogger.commons.ActivityLoggerConstants;
import com.mobicule.component.activitylogger.service.CommunicationActivityService;
import com.mobicule.component.activitylogger.service.UserActivityService;
import com.mobicule.component.activitylogger.threads.UserActivityAddition;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.CollectionService;
import com.mobicule.mcollections.core.service.SystemUserService;
import com.mobicule.mcollections.integration.commons.ServerUtilities;

public class CollectionUpiNotification implements ICollectionUpiNotification 
{
	private Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private SystemUserService systemUserService;
	
	@Autowired
	private UserActivityService userActivityService;
	
	@Autowired
	private CollectionService collectionService;
	
	@Autowired
	ApplicationConfiguration applicationConfiguration;
	
	
	public ApplicationConfiguration getApplicationConfiguration() {
		return applicationConfiguration;
	}


	public void setApplicationConfiguration(
			ApplicationConfiguration applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
	}

	private CommunicationActivityService communicationActivityService;

	@Override
	public Message<String> execute(Message<String> message) throws Throwable {
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
			
			JSONObject jsonObject = new JSONObject(requestSet);
			JSONObject user = (JSONObject) jsonObject.get(JsonConstants.SYSTEM_USER);			
			SystemUser systemUserNew = ServerUtilities.extractSystemUser(user);

			log.info("-------requestSet is-----" + requestSet);
			log.info("-----requestEntity----" + requestEntity);
			log.info("-----requestAction----" + requestAction);
			log.info("-----requestType----" + requestType);
			log.info("---systemUserNew-----" + systemUserNew);

			UserActivityAddition userActivityAddition = new UserActivityAddition(
					requestSet, userActivityService,
					ActivityLoggerConstants.DATABASE_MSSQL);
			
			new Thread(userActivityAddition).run();
			
			UserActivity userActivity = userActivityAddition
					.extractUserActivity();
			
			JSONObject data = (JSONObject) jsonObject.get(JsonConstants.DATA);
			log.info("data part of json" +data);

			String receiptNo = data.getString(Constants.AllPayCollectionsDao.RECEIPT_NO) == null ? "" : data.getString(Constants.AllPayCollectionsDao.RECEIPT_NO);
			log.info("--receiptNo--" + receiptNo);
			
			Map<String, String> detailMap = new HashMap<String, String>();
    		/*detailMap.put("appl", collection.getAppl());
    		detailMap.put("amount", collection.getBusinessPartnerNumber());
    		detailMap.put("receiptNumber", collection.getReceiptNumber());
    		detailMap.put("apacNumberValue", collection.getBusinessPartnerNumber());
          
           String jsonRequestPaymentData=UpiUtility.generateUpiJsonReq(detailMap);*/
          
         //  log.info("jsonRequestPaymentData     "+jsonRequestPaymentData);
            
        //  String jsonResponsePaymentData= UpiUtility.callWebserviceAndGetJsonString(jsonRequestPaymentData,"X-Check");
           
           
         String jsonResponsePaymentData="{ \"code\":\"00\", \"result\":\"Accepted Collect Request\", \"data\":{ \"orderId\":\"MB123456789456123\", \"referenceId\":\"825020132031\", \"payerVpa\":\"917208429868@kotak\", \"payerName\":null, \"txnId\":\"KMBMKCBG9c2baea9443c4e6aac1e096a33f\", \"aggregatorVPA\":\"kcbg@kotak\", \"expiry\":\"1800\", \"amount\":\"15.00\", \"timeStamp\":\"07-09-2018 19:22:04 \" } }";
           
           Map<Object, Object> updateActivityMap = new HashMap<Object, Object>();

           if(jsonResponsePaymentData!=null && !jsonResponsePaymentData.isEmpty())
           {
        	
   			Map<Object, Object> activityMap = new HashMap<Object, Object>();

        	activityMap.put(Constants.AllPayCollectionsDao.REQUEST_TO_THIRD_PARTY,jsonResponsePaymentData);
   			activityMap.put(Constants.AllPayCollectionsDao.THIRD_PARTY_STATUS, "PENDING");
   			activityMap.put(Constants.AllPayCollectionsDao.CREATED_BY, systemUserNew.getUserTableId().toString());
   			activityMap.put(Constants.AllPayCollectionsDao.MODIFIED_BY, systemUserNew.getUserTableId().toString());
   			activityMap.put(Constants.AllPayCollectionsDao.RECEIPT_NUMBER,receiptNo);


   			int smsTableID = collectionService.smsPaymentActivityAddition(activityMap);
   			
   			log.info("smsTableID ---- > " + smsTableID);
        	   	        	   
       				JSONObject jsonCode = new JSONObject(jsonResponsePaymentData);
       				log.info("jsonCode--->" + jsonCode);
       				JSONObject responseData = (JSONObject) jsonCode.get(JsonConstants.DATA);
       				log.info("data part of json---" +responseData);
       				
			 // String status=Constants.EMPTY_STRING;

				if (( (String) jsonCode.get("code")).equalsIgnoreCase("00"))
				{
					    status=Constants.UPIDao.SUCCESS;			
						
					    updateActivityMap.put(Constants.UPIDao.ID, smsTableID);
						
						updateActivityMap.put(Constants.UPIDao.INVOICE_ID, responseData.get("orderId")); 
					
						updateActivityMap.put(Constants.UPIDao.THIRD_PARTY_RESPONSE, jsonResponsePaymentData);

						updateActivityMap.put(Constants.UPIDao.MODIFIED_BY, systemUserNew.getUserTableId().toString());
						
						updateActivityMap.put(Constants.UPIDao.THIRD_PARTY_STATUS, status);
						
						collectionService.smsPaymentActivityUpdation(updateActivityMap);							

						
				}
				
				else
				{
					status=Constants.UPIDao.FAILURE;			
					
				    updateActivityMap.put(Constants.UPIDao.ID, smsTableID);
					
					updateActivityMap.put(Constants.UPIDao.INVOICE_ID, responseData.get("orderId")); 
				
					updateActivityMap.put(Constants.UPIDao.THIRD_PARTY_RESPONSE, jsonResponsePaymentData);

					updateActivityMap.put(Constants.UPIDao.MODIFIED_BY, systemUserNew.getUserTableId().toString());
					
					updateActivityMap.put(Constants.UPIDao.THIRD_PARTY_STATUS, status);
					
					collectionService.smsPaymentActivityUpdation(updateActivityMap);
								
				}
        	   

        			
           }
         else
         {
        	  

        	   
         }
        }

		catch (Exception e) {

			log.error("--- Exception In CollectionSyncService Method Inner Catch --- " + e);					
			responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
			responseJSON.put(JsonConstants.MESSAGE, JsonConstants.SOME_ERROR);
			responseJSON.put(JsonConstants.DATA, Constants.EMPTY_STRING);			
			
		}		
		return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
	}
	


}
