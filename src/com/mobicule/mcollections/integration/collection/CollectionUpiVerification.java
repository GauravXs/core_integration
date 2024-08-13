package com.mobicule.mcollections.integration.collection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

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
import com.mobicule.component.activitylogger.threads.UserActivityStatusUpdate;
import com.mobicule.mcollections.core.beans.Collection;
import com.mobicule.mcollections.core.beans.Configuration;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.UpiUtility;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.CollectionService;
import com.mobicule.mcollections.core.service.SystemUserService;
import com.mobicule.mcollections.integration.commons.ServerUtilities;

public class CollectionUpiVerification implements ICollectionUpiVerification
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

	@Override
	public Message<String> execute(Message<String> message) throws Throwable 
	{
	
		
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
			
			String customerId = data.getString(Constants.CUSTOMER_ID);
			log.info("customerId--" + customerId);
			
			String vpa = data.getString(Constants.VPA);
			log.info("Vpa--" + vpa);
			
			 Map<String, String> detailMap = new HashMap<String, String>();
			 detailMap.put("customerId", "919773861716");
	   	     detailMap.put("vpa", vpa);
	   		 detailMap.put("merchantId", "CBGUPI");

	   		

	 //   	String serverURL = UpiUtility.getUpiVerifyDetails(detailMap);
	   //		String responseJsonforUPI= UpiUtility.sendRequestHttpsPost(serverURL); //Prod
	   		//	String responseJsonforUPI ="{\"code\":\"00\",\"result\":\"Customer Not Found !!\",\"data\":\"Custom Not Found !!\"}"; //local
			


        	String maxLimit=collectionService.getMaxLimit();
        	
            double maxAmntDay=Double.valueOf(maxLimit);
        	
        	
            boolean flag = collectionService.getVPAAmountLimit(vpa,maxAmntDay);
            
            
            if(flag==true)
            {
            	responseJSON.put(JsonConstants.MESSAGE, "Maximum transaction limit per day exceed");
				responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
				responseJSON.put(JsonConstants.DATA, Constants.EMPTY_STRING);
            }
            else 
            
            {
	   		
            //	String serverURL="https://10.240.20.225:10061/Merchant/VerifyVPA";
            	
		    	String serverURL = (String)applicationConfiguration.getValue("WEBSERVICE_UPI_VERIFY_VPA");

	   	    	
	   	        String upiverifyJsonRequest=UpiUtility.verifyUpiJsonReq(detailMap);
	   	        
	   			log.info(" upiverifyJsonRequest ---" + upiverifyJsonRequest);


            	String responseJsonforUPI= UpiUtility.sendRequestHttpsPostWithData(serverURL,upiverifyJsonRequest); 
	   			
	   			log.info(" responseJsonforUPI ---" + responseJsonforUPI);
	   			
	   			Map<String, String> responseMap = UpiUtility.jsonToMap(responseJsonforUPI);
	
			
			
			if(responseMap!=null)
			{
				
			if  (responseMap.get(JsonConstants.UpiConstant.CODE).equalsIgnoreCase("00"))
			{
								
				responseJSON.put(JsonConstants.MESSAGE, "VPA Verified successfully");
				responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
				responseJSON.put(JsonConstants.DATA, vpa);
				
				UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(
						userActivity,
						(ActivityLoggerConstants.STATUS_SUCCESS),
						userActivityService);
				new Thread(userActivityStatusUpdate).run();
				
				status=Constants.AllPayCollectionsDao.SUCCESS;
				log.info("Success Response");
			}
				else 
				
			{
				log.info("Failure Response");				
				
				responseJSON.put(JsonConstants.MESSAGE,"Please enter valid VPA address");
				responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
				responseJSON.put(JsonConstants.DATA, Constants.EMPTY_STRING);

				UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(
						userActivity,
						(ActivityLoggerConstants.STATUS_FAILURE),
						userActivityService);
				new Thread(userActivityStatusUpdate).run();
				
				status=Constants.AllPayCollectionsDao.FAILURE;
				
				log.info("Success Response");
				
			}
		
			}
			else {
				
				responseJSON.put(JsonConstants.MESSAGE, "Something went wrong");
				responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
				responseJSON.put(JsonConstants.DATA, Constants.EMPTY_STRING);

				UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(
						userActivity,
						(ActivityLoggerConstants.STATUS_FAILURE),
						userActivityService);
				new Thread(userActivityStatusUpdate).run();
				
				status=Constants.AllPayCollectionsDao.FAILURE;
			}
			
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
