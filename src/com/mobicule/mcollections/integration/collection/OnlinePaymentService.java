/**
 * 
 */
package com.mobicule.mcollections.integration.collection;

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
import com.mobicule.component.mapconversion.json.JSONToMap;
import com.mobicule.component.mapconversion.json.MapToJSON;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.CollectionService;
import com.mobicule.mcollections.core.service.SystemUserService;
import com.mobicule.mcollections.integration.commons.ServerUtilities;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.ws.http.HTTPException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

/**
 * @author bhushan
 *
 */
public class OnlinePaymentService implements IOnlinePaymentService
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
	public Message<String> execute(Message<String> message) throws Throwable
	{
		String status = JsonConstants.FAILURE;
		
		JSONObject responseJSON = new JSONObject();
		Map<String, Object> result = new HashMap<String, Object>();
		try
		{
			String requestSet = message.getPayload();
		/*	String requestEntity = JSONPayloadExtractor.extract(requestSet, JsonConstants.ENTITY);
			String requestAction = JSONPayloadExtractor.extract(requestSet, JsonConstants.ACTION);
			String requestType = JSONPayloadExtractor.extract(requestSet, JsonConstants.TYPE);
			log.info("requestEntity :: " +requestEntity + " :: requestAction :" + requestAction +" requestType:: "+ requestType);*/
			
			JSONObject jsonObject = new JSONObject(requestSet);
			JSONObject user = (JSONObject) jsonObject.get(JsonConstants.SYSTEM_USER);
			JSONObject data = (JSONObject) jsonObject.get(JsonConstants.DATA);
			
			log.info("---user json-----" + user.toString());
			log.info("---data json-----" + data.toString());
			
			SystemUser systemUserNew = ServerUtilities.extractSystemUser(user);
			
			log.info("---systemUserNew-----" + systemUserNew);

			UserActivityAddition userActivityAddition = new UserActivityAddition(
					requestSet, userActivityService,
					ActivityLoggerConstants.DATABASE_MSSQL);

			new Thread(userActivityAddition).run();

			UserActivity userActivity = userActivityAddition
					.extractUserActivity();
			
			JSONObject reqGeneratedForThirdParty =  genrateRazorpayRequest(data);			
			
			
			
			if (collectionService.checkDublicateReceiptNumber(data.getString("merchant_param3")
					.toString())) 
			{
				String response = postXMLToTIBCO(genrateRazorpayRequest(data).toString());
				
				log.info(" inside if block ");
				Map<Object, Object> activityMap = new HashMap<Object, Object>();
				Map<Object, Object> updateActivityMap = new HashMap<Object, Object>();	
				
				activityMap.put(Constants.AllPayCollectionsDao.DEVICE_REQUEST, requestSet);
				activityMap.put(Constants.AllPayCollectionsDao.DEVICE_RESPONSE_STATUS, JsonConstants.PENDING);
				activityMap.put(Constants.AllPayCollectionsDao.REQUEST_TO_THIRD_PARTY, reqGeneratedForThirdParty.toString());
				activityMap.put(Constants.AllPayCollectionsDao.THIRD_PARTY_STATUS, JsonConstants.PENDING);
				activityMap.put(Constants.AllPayCollectionsDao.CREATED_BY, systemUserNew.getUserTableId().toString());
				activityMap.put(Constants.AllPayCollectionsDao.MODIFIED_BY, systemUserNew.getUserTableId().toString());
				activityMap.put(Constants.AllPayCollectionsDao.RECEIPT_NUMBER,data.getString("merchant_param3")
						.toString());
				activityMap.put(Constants.AllPayCollectionsDao.AMOUNT,data.getString("amount")
						.toString());
				
				int smsTableID = collectionService.smsPaymentActivityAddition(activityMap);
				
				
				
				log.info("response from Razorpay :: " + response);
				
				
				result = jsonToMap(response);
				log.info("converted response in map  " + result);
				if(result.containsKey("error"))
				{
					
					
					log.info(" inside 2 if block ");
					
					UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(
							userActivity,
							(ActivityLoggerConstants.STATUS_FAILURE),
							userActivityService);
					new Thread(userActivityStatusUpdate).run();
					
					status=Constants.AllPayCollectionsDao.FAILURE;
					updateActivityMap.put(Constants.AllPayCollectionsDao.ID, smsTableID);
					
					updateActivityMap.put(Constants.AllPayCollectionsDao.THIRD_PARTY_RESPONSE, response);
					updateActivityMap.put(Constants.AllPayCollectionsDao.MODIFIED_BY, systemUserNew.getUserTableId().toString());
					updateActivityMap.put(Constants.AllPayCollectionsDao.THIRD_PARTY_STATUS, status);
					
					responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
					responseJSON.put(JsonConstants.MESSAGE, JsonConstants.SOME_ERROR);
					responseJSON.put(JsonConstants.DATA, Constants.EMPTY_STRING);
					
				}
				else 
				{
					
					log.info(" inside else part of inner if block ");
					responseJSON.put(JsonConstants.MESSAGE, JsonConstants.SMS_SENT_SUCCESSFULLY);
					responseJSON.put(JsonConstants.DATA, new JSONObject(response));
					responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);					
					
					UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(
							userActivity,
							(ActivityLoggerConstants.STATUS_SUCCESS),
							userActivityService);
					new Thread(userActivityStatusUpdate).run();
					
					status=Constants.AllPayCollectionsDao.SUCCESS;
					updateActivityMap.put(Constants.AllPayCollectionsDao.ID, smsTableID);				
					updateActivityMap.put(Constants.AllPayCollectionsDao.INVOICE_ID, result.get("id")); //production
					updateActivityMap.put(Constants.AllPayCollectionsDao.THIRD_PARTY_RESPONSE, response);
					updateActivityMap.put(Constants.AllPayCollectionsDao.MODIFIED_BY, systemUserNew.getUserTableId().toString());
					updateActivityMap.put(Constants.AllPayCollectionsDao.THIRD_PARTY_STATUS, status);				
					updateActivityMap.put(Constants.AllPayCollectionsDao.DEVICE_RESPONSE, responseJSON.toString());
					updateActivityMap.put(Constants.AllPayCollectionsDao.DEVICE_RESPONSE_STATUS, status);
					updateActivityMap.put(Constants.AllPayCollectionsDao.RECEIPT_NUMBER, data.getString("merchant_param3")
							.toString());
					
					collectionService.smsPaymentActivityUpdation(updateActivityMap);
					
					
				}
				
			}
			else
			{
				log.info(" inside else block ");
			    responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
				responseJSON.put(JsonConstants.MESSAGE,JsonConstants.JSON_DUPLICATED);
				responseJSON.put(JsonConstants.DATA, Constants.EMPTY_STRING);	
			}
			
		
			
			
		}
		catch (Exception e)
		{
			log.error("Exception occured in OnlinePaymentService" ,e );
			
			responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
			responseJSON.put(JsonConstants.MESSAGE, JsonConstants.SOME_ERROR);
			responseJSON.put(JsonConstants.DATA, Constants.EMPTY_STRING);		
		}
		return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
	}
	
	public String postXMLToTIBCO(String requestXML)
	{

		log.info("request ::" +requestXML);
		
		PostMethod post = null;
		String responseXML = null;
		String serverURL = "https://api.razorpay.com/v1/invoices/";
		log.info("TIBCO url ::" + serverURL);

		HttpClient httpclient = new HttpClient();

		post = new PostMethod(serverURL);

		post.setRequestEntity(new StringRequestEntity(requestXML));
		log.info(" Request XML ::" + requestXML);

		post.setRequestHeader("Content-type", "application/json");
		post.setRequestHeader("Authorization", "Basic cnpwX3Rlc3RfRVhIbEVGOTltMWNvSzU6cENkY1lZOFlxNkZtdHpGT05Pb1JBS3h6");
		
		try
		{
			log.info("CONNECTING TO TIBCO..");

			log.info("KEYSTORE " + System.getProperty("javax.net.ssl.trustStore"));

			System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

			int statusCode = httpclient.executeMethod(post);
			log.info("Post Method execution ::statusCode " + statusCode);

			log.info(post.getResponseBodyAsString());
			if (statusCode != HttpStatus.SC_OK)
			{
				log.info("Method failed: " + post.getStatusLine());
				log.info("Post Method execution ::statusCode " + post.getStatusLine());
			}
			responseXML = post.getResponseBodyAsString();

			log.info("responseXML :- " + responseXML);
		}
		catch (HTTPException e)
		{
			log.info("Fatal protocol violation: ");
			e.printStackTrace();
		}
		catch (IOException e)
		{
			log.info("Fatal transport error: ");
			e.printStackTrace();
		}
		catch (Exception e)
		{
			log.info("Exception while sending request to TIBCO: ");
			e.printStackTrace();
		}
		finally
		{
			post.releaseConnection();
			log.info("The connection is released.");
		}

		return responseXML;
	}
	
	public static Map<String,Object> jsonToMap(String str)
	{
		
		
		  HashMap<String,Object> map = new HashMap<String,Object>();
		  
		  try
			{
			  
	        JSONObject jObject= new JSONObject(str);
			
	        Iterator<?> keys = (Iterator<?>) jObject.keys();

	        while( keys.hasNext() ){
	            String key = (String)keys.next();
	            String value = jObject.getString(key); 
	            map.put(key, value);

	        }

	        System.out.println("json : "+jObject);
	        System.out.println("map : "+map);
	        
			}
		  
		  catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  return map;
	}
	
	public static JSONObject genrateRazorpayRequest(JSONObject data)
	{
		try
		{
			String customerMobileNo =data.getString("customer_mobile_no");
			String customerName =data.getString("customer_name");
			float amount = Float.parseFloat(data.getString("amount"));
			int truevalue =0;
			int falsevalue =1;
			
			String billDeliveryType =data.getString("bill_delivery_type");
			String reqId =data.getString("reqId");
			String merchantParam4 =data.getString("merchant_param4");
			String merchantParam3 =data.getString("merchant_param3");
			String merchantParam2 =data.getString("merchant_param2");
			String merchantParam1 =data.getString("merchant_param1");
			
			Map<String, Object> razorPayCustomerMap = new HashMap<String, Object>();
			razorPayCustomerMap.put("name", customerName);
			razorPayCustomerMap.put("email", "");
			razorPayCustomerMap.put("contact", customerMobileNo);
			
			Map<String, Object> razorPayMap = new HashMap<String, Object>();
			razorPayMap.put("type","link");
			razorPayMap.put("view_less",falsevalue);
			razorPayMap.put("amount",amount);			
			razorPayMap.put("currency","INR");
			razorPayMap.put("description","Payment Link for this purpose - cvb.");
			razorPayMap.put("receipt",merchantParam3);
			razorPayMap.put("sms_notify",falsevalue);
			razorPayMap.put("email_notify",falsevalue);
			razorPayMap.put("expire_by","1793630556");
			razorPayMap.put("customer",razorPayCustomerMap);
			
			JSONObject tempdataObject =  MapToJSON.convertMapToJSON(razorPayMap);
			return tempdataObject;
			
			
			
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new JSONObject();
		
	}

}
