package com.mobicule.mcollections.integration.collection;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.dao.GpsLocationDao;

public class GpsLocationSubmissionService implements IGpsLocationSubmissionService
{
	private Logger log = LoggerFactory.getLogger(GpsLocationSubmissionService.class);
	
	@Autowired
	private GpsLocationDao gpsLocationDao;

	public Message<String> execute(Message<String> message) throws Throwable
	{
		String requestSet = message.getPayload();
		
		JSONObject jsonObj = new JSONObject(requestSet);
		
		log.info("jsonObj"+jsonObj);
		
		JSONObject jsonData = (JSONObject) jsonObj.get(JsonConstants.DATA);
		
		JSONObject jsonUserData = (JSONObject) jsonObj.get(JsonConstants.SYSTEM_USER);
		
		log.info("jsonUserData"+jsonUserData);
		
		Map<String,Object> submissionParameter = new HashMap<String,Object>();
		
		String userId = (String) (jsonUserData.get("userId") == null ? "" : (String) jsonUserData.get("userId"));
		String receiptNo = (String) (jsonData.get("receiptNo") == null ? "" : (String) jsonData.get("receiptNo"));
		String modeOfTransport = (String) (jsonData.get("modeOfTransport") == null ? "" : (String) jsonData.get("modeOfTransport"));
		String amount = (String) (jsonData.get("amount") == null ? "" : (String) jsonData.get("amount"));
		String distance = (String) (jsonData.get("distance") == null ? "" : (String) jsonData.get("distance"));
		String reason = (String) (jsonData.get("reason") == null ? "" : (String) jsonData.get("reason"));
		String reqId = (String) (jsonData.get("reqId") == null ? "" : (String) jsonData.get("reqId"));
		String visitStartTime = (String) (jsonData.get("start_visit_time") == null ? "" : (String) jsonData.get("start_visit_time"));
		String visitEndTime = (String) (jsonData.get("end_visit_time") == null ? "" : (String) jsonData.get("end_visit_time"));
		String visitType = (String) (jsonData.get("visitType") == null ? "" : (String) jsonData.get("visitType"));
		
		submissionParameter.put(Constants.GpsLocationDao.USERID, userId);
		submissionParameter.put(Constants.GpsLocationDao.REQID, reqId);
		submissionParameter.put(Constants.GpsLocationDao.RECEIPT_NO, receiptNo);
		submissionParameter.put(Constants.GpsLocationDao.MODEOFTRANSPORT, modeOfTransport);
		submissionParameter.put(Constants.GpsLocationDao.AMOUNTVAL, amount);
		submissionParameter.put(Constants.GpsLocationDao.DISTANCEVAL, distance);
		submissionParameter.put(Constants.GpsLocationDao.REASONVAL, reason);
		submissionParameter.put(Constants.GpsLocationDao.VISIT_TYPE, visitType);
		submissionParameter.put(Constants.GpsLocationDao.VISIT_START_DATE_TIME, visitStartTime);
		submissionParameter.put(Constants.GpsLocationDao.VISIT_END_DATE_TIME, visitEndTime);
		
		if(checkDuplicateLocationJSONInDB(submissionParameter))
		{
			return responseBuilder(message, JsonConstants.FAILURE, "Duplicate expense request", "");
		}
		else
		{
			int flag = gpsLocationDao.gpsLocationSubmissionIntoDb(submissionParameter);
			
			if(flag > 0)
			{
				return responseBuilder(message, JsonConstants.SUCCESS, "GPS Location Submitted Successfully", "");
			}
			else
			{
				return responseBuilder(message, JsonConstants.FAILURE, "Error to Submit GPS Location", "");
			}
		}
	}
	
	private boolean checkDuplicateLocationJSONInDB(Map<String, Object> submissionParameter) 
	{
		// TODO Auto-generated method stub
		
		boolean flag = gpsLocationDao.checkDuplicateExpenseInDb(submissionParameter);
		
		if(flag)
		{
			return true;
		}
		else
		{
			return false;
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
}
