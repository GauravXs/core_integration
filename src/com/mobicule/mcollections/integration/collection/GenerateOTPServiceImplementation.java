package com.mobicule.mcollections.integration.collection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;

import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.DBColumnNameConstants;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.SmsFormXML;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.GenerateOTPService;
import com.mobicule.mcollections.integration.commons.ServerUtilities;

public class GenerateOTPServiceImplementation implements IGenerateOTPServiceImplementation 
{
	private Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private GenerateOTPService  generateOTPService;
	
	@Autowired
	private ApplicationConfiguration<String, String> applicationConfiguration;

	
	public ApplicationConfiguration<String, String> getApplicationConfiguration() {
		return applicationConfiguration;
	}

	public void setApplicationConfiguration(ApplicationConfiguration<String, String> applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
	}

	public GenerateOTPService getGenerateOTPService() {
		return generateOTPService;
	}

	public void setGenerateOTPService(GenerateOTPService generateOTPService) {
		this.generateOTPService = generateOTPService;
	}

	@Override
	public Message<String> execute(Message<String> message) throws Throwable
	{
		log.info(" -------- In GenerateOTPServiceImplementation execute Method -------- ");
		
		HashMap dataMap = new HashMap();
		Map<String,Object> responseMap = new HashMap<String, Object>();

		try
		{
			String requestSet = message.getPayload();
			JSONObject jsonObject = new JSONObject(requestSet);
			JSONObject data = (JSONObject) jsonObject.get(JsonConstants.DATA);
			JSONObject user = (JSONObject) jsonObject.get(JsonConstants.SYSTEM_USER);
			SystemUser systemUser = ServerUtilities.extractSystemUser(user);
			
			Map<String,String> fetchUserDataParameter = new HashMap<String, String>();
			
			fetchUserDataParameter.put(Constants.USER_ID, systemUser.getUserTableId() == null ? "" : systemUser.getUserTableId().toString() ); 
			fetchUserDataParameter.put(Constants.USERNAME, systemUser.getUsername() == null ? "" : systemUser.getUsername());			
			
			Map<String,Object> fetchUserDetails = generateOTPService.fetchUserDetails(fetchUserDataParameter);
		
			if(!fetchUserDetails.isEmpty()) 
			{
				String userMobileNumber = fetchUserDetails.get(DBColumnNameConstants.MOBILE_NUMBER) == null ? Constants.EMPTY_STRING
						: fetchUserDetails.get(DBColumnNameConstants.MOBILE_NUMBER)
								.toString();
				String userId = systemUser.getUserTableId() == null ? "" : systemUser.getUserTableId().toString();
				String type = data.get(Constants.REQ_TYPE) == null ? "" : data.getString(Constants.REQ_TYPE);
				String requestId = data.get(Constants.REQ_ID) == null ? "" : data.getString(Constants.REQ_ID);
				
				
				if(StringUtils.isEmpty(userMobileNumber))
				{
					responseMap.put(Constants.MESSAGE, Constants.Messages.OPT_GENERATE_FAILURE_MOBILE);
					responseMap.put(Constants.JSON_RESPONSE_STATUS, JsonConstants.MOB_EMPTY);
					responseMap.put(Constants.DATA_MAP, dataMap);
					return Utilities.deviceResponse(message,responseMap);
				}
				
				Map<String, Object> parameters = new HashMap<String, Object>();
				Map<String, Object> mergeVarParam = new HashMap<String, Object>();
				Map<String, Object> bodyParam = new HashMap<String, Object>();
				Map<String, Object> parametersMap = new HashMap<String, Object>();

				parameters.put(Constants.smsParam.SERVICEREQUESTID, Constants.smsParam.GEN_TYPE);
				parameters.put(Constants.smsParam.CHANNELID, Constants.smsParam.CHANNEL_ID_NAME);
				parameters.put(Constants.smsParam.MESSAGEDATETIME, "");
				parameters.put(Constants.smsParam.USERID, systemUser.getUsername() == null ? "" : systemUser.getUsername());
				parameters.put(Constants.smsParam.PASSWORD, "");

				bodyParam.put(Constants.smsParam.CHANNELID_VALUE, Constants.smsParam.CHANNEL_ID_NAME);
				bodyParam.put(Constants.smsParam.REQUESTID_VALUE, System.currentTimeMillis());
				bodyParam.put(Constants.smsParam.EMAIL_VALUE, "");
				bodyParam.put(Constants.smsParam.MOBILENO_VALUE, userMobileNumber);//userMobileNumber
				bodyParam.put(Constants.smsParam.CUSTOMERID_VALUE, "");
				bodyParam.put(Constants.smsParam.SERVICETYPE_VALUE, "s");
				bodyParam.put(Constants.smsParam.DELIVERYFLAG_VALUE, "S");


				String otpRequest = SmsFormXML.generateOtpSMSXml(parameters, mergeVarParam, bodyParam);

				parametersMap.put(Constants.LdapParam.LDAP_URL,
						applicationConfiguration.getValue(Constants.smsParam.OTP_GEN_URL));
				parametersMap.put(Constants.LdapParam.LDAPREQUEST, otpRequest == null ? "" : otpRequest);
				String responseString = Utilities.postXML(parametersMap);

				boolean otpResponse = SmsFormXML.generateOtpGenResBodySMSXml(responseString);
				
				  Map<String,Object> parameter = new HashMap<String, Object>();
				  
				  parameter.put(Constants.User_MOBILE_NUMBER, userMobileNumber );//userMobileNumber
				  parameter.put(Constants.USER_ID, userId); parameter.put(Constants.REQ_TYPE,
				  type ); parameter.put(Constants.REQ_ID, requestId );
				  parameter.put(Constants.DEVICE_OTP, System.currentTimeMillis()  );
				  
				  boolean otpSubmissionFlag = generateOTPService.insertOtpDetails(parameter);
				 
				
				if(otpResponse && otpSubmissionFlag)
				{
				/*	final Map<String,Object> parameters = new HashMap<String, Object>();
					Utilities.sendSMS(parameters);*/
					
		
					dataMap.put(Constants.REQ_ID, requestId);	
					responseMap.put(Constants.MESSAGE, Constants.Messages.OPT_GENERATE_SUCCESS);
					responseMap.put(Constants.JSON_RESPONSE_STATUS, JsonConstants.SUCCESS);
					responseMap.put(Constants.DATA_MAP, dataMap);
					return Utilities.deviceResponse(message,responseMap);
					
				}
			}
			else
			{
				responseMap.put(Constants.MESSAGE, Constants.Messages.OPT_GENERATE_FAILURE_MOBILE);
				responseMap.put(Constants.JSON_RESPONSE_STATUS, JsonConstants.FAILURE);
				responseMap.put(Constants.DATA_MAP, dataMap);
				return Utilities.deviceResponse(message,responseMap);
				
			}
			
		}
		catch (Exception e)
		{

			log.error("---- Exception Detail ----:-"+e);

		}
		
		responseMap.put(Constants.MESSAGE, Constants.Messages.OPT_GENERATE_FAILURE);
		responseMap.put(Constants.JSON_RESPONSE_STATUS, JsonConstants.FAILURE);
		responseMap.put(Constants.DATA_MAP, dataMap);
		
		return Utilities.deviceResponse(message,responseMap);

	}
	
	
	
	@Override
	public Message<String> checkOTPDetails(Message<String> message) throws Throwable
	{
		log.info(" -------- In GenerateOTPServiceImplementation checkOTP Method -------- ");
		
		HashMap<String, Object> dataMap = new HashMap<String, Object>();
		Map<String,Object> responseMap = new HashMap<String, Object>();

		try
		{
			String requestSet = message.getPayload();
			JSONObject jsonObject = new JSONObject(requestSet);
			JSONObject data = (JSONObject) jsonObject.get(JsonConstants.DATA);
			JSONObject user = (JSONObject) jsonObject.get(JsonConstants.SYSTEM_USER);
			SystemUser systemUser = ServerUtilities.extractSystemUser(user);
			
			Map<String,String> checkOTPDataParameter = new HashMap<String, String>();
			
			checkOTPDataParameter.put(Constants.USER_ID, systemUser.getUserTableId() == null ? "" : systemUser.getUserTableId().toString() );
			
			String requestId = data.get(Constants.REQ_ID) == null ? "" : data.getString(Constants.REQ_ID);
			String requetsOTP = data.get(Constants.OTP) == null ? "" : data.getString(Constants.OTP);			
			
			List<Map<String, Object>> fetchUserOTPDetails = generateOTPService.checkOTPDetails(checkOTPDataParameter);
		
			if(!fetchUserOTPDetails.isEmpty()) 
			{
				Map<String, Object> fetchUserOTPDetailsMap = fetchUserOTPDetails.get(0);
				String latestOtp = fetchUserOTPDetailsMap.get(DBColumnNameConstants.REQUESTED_OTP) == null ? Constants.EMPTY_STRING : fetchUserOTPDetailsMap.get(DBColumnNameConstants.REQUESTED_OTP).toString();
				
				if(StringUtils.isEmpty(latestOtp))
				{
					responseMap.put(Constants.MESSAGE, Constants.Messages.OTP_CHECK_FAILURE);
					responseMap.put(Constants.JSON_RESPONSE_STATUS, JsonConstants.FAILURE);
					responseMap.put(Constants.DATA_MAP, dataMap);
					return Utilities.deviceResponse(message,responseMap);
				}
				
				Map<String, Object> parameters = new HashMap<String, Object>();
				Map<String, Object> mergeVarParam = new HashMap<String, Object>();
				Map<String, Object> bodyParam = new HashMap<String, Object>();
				Map<String, Object> parametersMap = new HashMap<String, Object>();

				parameters.put(Constants.smsParam.SERVICEREQUESTID, Constants.smsParam.VAL_TYPE);
				parameters.put(Constants.smsParam.CHANNELID, Constants.smsParam.CHANNEL_ID_NAME);
				parameters.put(Constants.smsParam.MESSAGEDATETIME, "");
			//	parameters.put(Constants.smsParam.USERID, username);
				parameters.put(Constants.smsParam.PASSWORD, "");

				bodyParam.put(Constants.smsParam.CHANNELID_VALUE, Constants.smsParam.CHANNEL_ID_NAME);
				bodyParam.put(Constants.smsParam.REQUESTID_VALUE,latestOtp);
				bodyParam.put(Constants.smsParam.OTP_VALUE, requetsOTP);

				String otpRequest = SmsFormXML.generateOtpSMSXml(parameters, mergeVarParam, bodyParam);

				parametersMap.put(Constants.LdapParam.LDAP_URL,
						applicationConfiguration.getValue(Constants.smsParam.VAL_TYPE));
				parametersMap.put(Constants.LdapParam.LDAPREQUEST, otpRequest == null ? "" : otpRequest);
				String responseString = Utilities.postXML(parametersMap);

				boolean isOtpValid = SmsFormXML.generateOtpValResBodySMSXml(responseString);
				
				if(isOtpValid)
				{
		
					dataMap.put(Constants.REQ_ID, requestId);	
					responseMap.put(Constants.MESSAGE, Constants.Messages.OTP_CHECK_SUCCESS);
					responseMap.put(Constants.JSON_RESPONSE_STATUS, JsonConstants.SUCCESS);
					responseMap.put(Constants.DATA_MAP, dataMap);
					return Utilities.deviceResponse(message,responseMap);
					
				 } 
			}
			else
			{
				responseMap.put(Constants.MESSAGE, Constants.Messages.INVALID_LOGIN);
				responseMap.put(Constants.JSON_RESPONSE_STATUS, JsonConstants.FAILURE);
				responseMap.put(Constants.DATA_MAP, dataMap);
				return Utilities.deviceResponse(message,responseMap);
				
			}
			
		}
		catch (Exception e)
		{

			log.error("---- Exception Detail ----:-"+e);

		}
		
		responseMap.put(Constants.MESSAGE, Constants.Messages.INVALID_LOGIN);
		responseMap.put(Constants.JSON_RESPONSE_STATUS, JsonConstants.FAILURE);
		responseMap.put(Constants.DATA_MAP, dataMap);
		
		return Utilities.deviceResponse(message,responseMap);

	}
}
