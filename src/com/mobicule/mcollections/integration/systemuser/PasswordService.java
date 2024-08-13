package com.mobicule.mcollections.integration.systemuser;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.servlet.ModelAndView;

import com.mobicule.component.usermapping.bean.Authentication;
import com.mobicule.component.usermapping.service.AuthenticationService;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.AES;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.DBColumnNameConstants;
import com.mobicule.mcollections.core.commons.ForgotPasswordAPIUtilities;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.PasswordEncoder;
import com.mobicule.mcollections.core.commons.SmsFormXML;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.CollectionService;
import com.mobicule.mcollections.core.service.GenerateOTPService;
import com.mobicule.mcollections.core.service.SystemUserService;
import com.mobicule.mcollections.integration.commons.ServerUtilities;
import com.mobicule.mcollections.webservice.adapter.KotakCollectionWebserviceAdapter;

public class PasswordService
{

	private Logger log = LoggerFactory.getLogger(getClass());

	private SystemUserService userService;
	
	private AuthenticationService authenticationService;
	
	private CollectionService collectionService;

	public CollectionService getCollectionService()
	{
		return collectionService;
	}

	public void setCollectionService(CollectionService collectionService)
	{
		this.collectionService = collectionService;
	}

	@Autowired
	private GenerateOTPService  generateOTPService;
	
	@Autowired
	private ApplicationConfiguration<String, String> applicationConfiguration;

	public SystemUserService getUserService()
	{
		return userService;
	}

	public void setUserService(SystemUserService userService)
	{
		this.userService = userService;
	}

	public AuthenticationService getAuthenticationService()
	{
		return authenticationService;
	}

	public void setAuthenticationService(AuthenticationService authenticationService)
	{
		this.authenticationService = authenticationService;
	}

	public Message<String> userRequestForOTP(Message<String> message)
	{
		JSONObject responseJson = null;
		try
		{
			log.info("------------- in userRequestForOTP() in password service");
			 boolean otpSubmissionFlag =false;
			String mobileNumber = null;
			String data = (String) JSONPayloadExtractor.extract(message.getPayload(), JsonConstants.DATA);
			String userName = JSONPayloadExtractor.extract(data, JsonConstants.USERNAME);
			log.info("-------------in password service:  user requested for otp---------" + userName);
			Map<String,Object> responseMap = new HashMap<String, Object>();
			
			if (userName != null && userName != Constants.EMPTY_STRING)
			{
				SystemUser systemUser = userService.getUserDetailsForOTP(userName);

				if (null != systemUser)
				{
					log.info("------------- system user is not null ---------");

					log.info("---user type----" + systemUser.getLoginType()+systemUser);

					if (systemUser.getLoginType().equalsIgnoreCase("Normal"))
					{

						String requestId = String.valueOf(System.currentTimeMillis());
					/*	if (!isSameDateOfOTPRequest(userName))
						{*/
							log.info("------------- request is not on same date---------");

							mobileNumber = systemUser.getMobileNumber();

							log.info("------------- MobileNumber = " + mobileNumber + "---------");
							if (Constants.EMPTY_STRING != mobileNumber && null != mobileNumber)
							{
								log.info("----------inside if after checking  mobileNumber null or empty-------------");
								log.info("----------calilng generateOTPForDevice() -------------");

								/*
								 * String genratedOTP = generateOTPForDevice();
								 * 
								 * log.info("----------genrated otp =" + genratedOTP + "-------------");
								 * 
								 * systemUser.setOTP(genratedOTP);
								 * 
								 * Map optSMSMap = ServerUtilities.genrateSMSForOTP(genratedOTP, systemUser);
								 * 
								 * log.info("----------genrated sms =" + optSMSMap.toString() +
								 * "-------------");
								 */
								
								try
								{
										HashMap dataMap = new HashMap();
										String userMobileNumber = systemUser.getMobileNumber() == null ? "" : systemUser.getMobileNumber().toString();
										String userId = systemUser.getUserTableId() == null ? "" : systemUser.getUserTableId().toString();
										String type = "GenforgotPassword";
										
										
										
										if(StringUtils.isEmpty(userMobileNumber))
										{
											responseMap.put(Constants.MESSAGE, Constants.Messages.OPT_GENERATE_FAILURE_MOBILE);
											responseMap.put(Constants.JSON_RESPONSE_STATUS, JsonConstants.FAILURE);
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
										bodyParam.put(Constants.smsParam.REQUESTID_VALUE, requestId);
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
										  parameter.put(Constants.DEVICE_OTP, System.currentTimeMillis());
										  parameter.put(Constants.REQUEST, otpRequest );
										  parameter.put(Constants.RESPONSE, responseString );
										  
										  otpSubmissionFlag = generateOTPService.insertOtpDetails(parameter);
									
								}
								catch (Exception e)
								{

									log.error("---- Exception Detail ----:-"+e);

								}
								 

							//	if (sendOTPSMS(optSMSMap))
								if(otpSubmissionFlag)
								{
									log.info("---------otp sms sent successful -------------");
									//userService.updateOTP(systemUser)
									if (true)
									{

										log.info("---------otp updated successfully in db-------------");

										//responseJson = createSuccessResponseData();
										systemUser.setOTP(requestId);
										responseJson = createSuccessResponseData(systemUser);
									}
									/*
									 * else { log.info("---------otp update in db failed -------------");
									 * 
									 * responseJson = createFailureResponseData("otp not saved in db "); }
									 */

								}
								else
								{
									log.info("---------otp sms sent failed -------------");

									responseJson = createFailureResponseData("otp sms sending failed");
								}
							}
							else
							{
								log.info("------------- supervisorMobileNumber is null---------");
								responseJson = createFailureResponseData("mobile number for username not available");
							}

						/*}
						else
						{*/
							/*
							 * log.info("------------- in side else of date chk---------");
							 * log.info("------------- request is on same date---------");
							 * 
							 * mobileNumber = systemUser.getMobileNumber();
							 * 
							 * if (null != mobileNumber && Constants.EMPTY_STRING != mobileNumber) { String
							 * oldOTP = userService.getOTP(userName);
							 * System.out.println("printing on console"); System.out.println("otp in db =" +
							 * systemUser.getOTP()); if (null != oldOTP && Constants.EMPTY_STRING != oldOTP)
							 * { Map optSMSMap = ServerUtilities.genrateSMSForOTP(oldOTP, systemUser);
							 * 
							 * if(true) //if (sendOTPSMS(optSMSMap)) {
							 * log.info("---------otp sms sent successful -------------");
							 * 
							 * //responseJson = createSuccessResponseData(); responseJson =
							 * createSuccessResponseData(systemUser); } else {
							 * log.info("---------otp sms sent failed -------------");
							 * 
							 * responseJson = createFailureResponseData("otp sms sending failed"); } } else
							 * { log.info("---------otp retrived form db is null or empty -------------");
							 * responseJson =
							 * createFailureResponseData("otp is null or empty for username"); } } else {
							 * log.info("------------- MobileNumber is null---------"); responseJson =
							 * createFailureResponseData("mobile number for username not available"); }
							 * 
							} */
						/*
						 * } else {
						 */
						/*
						 * 
						 * log.info("--- -- inside Ldap user -----");
						 * 
						 * try { String genratedOTP = generateOTPForDevice();
						 * log.info("Generated OTP / Capcha For Ldap User :: " + genratedOTP);
						 * systemUser.setOTP(genratedOTP);
						 * 
						 * if (userService.updateOTP(systemUser)) { responseJson =
						 * createSuccessResponseData(systemUser); } else {
						 * log.info("---------otp update in db failed -------------");
						 * 
						 * responseJson = createFailureResponseData("otp not saved in db "); }
						 * 
						 * }
						 * 
						 * catch (Exception e) {
						 * log.error("---Exception in Ldap Change Password block-----", e);
						 * 
						 * }
						 * 
						 *//*}

				}

				else
				{
					log.info("--------- system user for requested username is null-------------");
					responseJson = createFailureResponseData(" no user present for requested username");
				}
*/
			}
			else
			{
				log.info("--------- username in request is null -------------");
				responseJson = createFailureResponseData(" please enter valid username");
			}
			}
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			log.info("---------exception in get user request for otp-------------");
			e.printStackTrace();
		}
		return genrateResponse(message, responseJson);
	}

	public Message<String> userRequestForChangePassword(Message<String> message)
	{
		JSONObject responseJson = null;
		log.info("---------- in side userRequestForChangePassword() in password service -------------");
		String data = (String) JSONPayloadExtractor.extract(message.getPayload(), JsonConstants.DATA);
		String userName = JSONPayloadExtractor.extract(data, JsonConstants.USERNAME);
		String otp = JSONPayloadExtractor.extract(data, JsonConstants.OTP);
		String reqId = JSONPayloadExtractor.extract(data, JsonConstants.REQID);
		String capcha = JSONPayloadExtractor.extract(data, "capcha");
		String newPassword = JSONPayloadExtractor.extract(data, JsonConstants.PASSWORD);
		String oldPassword = JSONPayloadExtractor.extract(data, "oldPassword");

		try
		{
			String key = applicationConfiguration.getValue("key");
			String iv = applicationConfiguration.getValue("iv");

			IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
			SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");

			Cipher aes1 = Cipher.getInstance("AES/CBC/NoPadding");
			aes1.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
			byte[] decodedArray = new Base64().decode(oldPassword.getBytes());

			oldPassword = new String(aes1.doFinal(decodedArray));
			oldPassword = oldPassword.trim();

			byte[] decodedArray1 = new Base64().decode(newPassword.getBytes());

			newPassword = new String(aes1.doFinal(decodedArray1));
			newPassword = newPassword.trim();

			log.info("---- oldPassword decrypted password -----" + oldPassword);
			log.info("---- new decrypted password -----" + newPassword);

		}
		catch (Exception e)
		{
			log.error("Exception while decrypting password :: " + e);
		}

		SystemUser sysUser = userService.getUser(userName);

		if (sysUser.getLoginType().equalsIgnoreCase("Normal"))
		{
			log.info("--- data from request(userName, otp,newPassword)" + userName + otp + newPassword
					+ "-------------");
			if ((null != userName && Constants.EMPTY_STRING != userName)
					&& (null != otp && Constants.EMPTY_STRING != otp)
					&& (null != newPassword && Constants.EMPTY_STRING != newPassword))
			{
				SystemUser user = new SystemUser();
				user.setUsername(userName);
				user.setOTP(otp);
				user.setPassword(newPassword);
				user.setModifiedBy(sysUser.getUserTableId());
				user.setReqId(reqId);
				log.info("------ calling isValidOTP() method-------------");
				if (isValidOTP(user))
				{
					log.info("------ isValidOTP()  successful-------------");
					log.info("------ calling updatePassword() method-------------");
					
					
					//To check previous password Device start
					Authentication authenticationTemp = new Authentication();
					authenticationTemp.setLogin(userName);
					authenticationTemp.setPassword(newPassword);
					authenticationTemp.setSystemUserId(sysUser.getUserTableId());
					
					/*log.info("authenticationTemp.getLogin() :: " + authenticationTemp.getLogin());
					log.info("authenticationTemp.getPassword() decrypted:: " + authenticationTemp.getPassword());
					log.info("authenticationTemp.getSystemUserId() :: " + authenticationTemp.getSystemUserId());*/

					if (userService.checkPreviousPasswords(authenticationTemp))
					{
						log.info("--- New password Matched with privious 5 password --- ");
						responseJson = createFailureResponseData("Previous password not allowed");
					}
					//To check previous passwords Device end
					else
					{
						if (userService.updatePassword(user))
						{
							log.info("------ updatePassword()  successful-------------");
							
							//To add entry in password_reset_history Device Start
							
							//authenticationTemp.setPassword(AES.encrypt(authenticationTemp.getPassword()));
							authenticationTemp.setPassword(PasswordEncoder.encode(authenticationTemp.getPassword()));
							log.info("authenticationTemp.getPassword() Encrypted :: " + authenticationTemp.getPassword());
							//need encrypted password
							if (userService.addPasswordResetHistory(authenticationTemp,sysUser.getUserTableId()))
							{
								log.info("--- Entry added in password_Reset_History table ---");
							}
							else
							{
								log.info("--- Unable to add entry in password_Reset_History table ---");
							}
							//To add entry in password_reset_history Device End
							
							responseJson = createSuccessResponseData();
						}
						else
						{
							log.info("--------- updatepassword() failed -------------");
							responseJson = createFailureResponseData("password reset  failed please try again");
						}
					}
					
					
				}
				else
				{
					log.info("---------otp in request is not valid -------------");
					responseJson = createFailureResponseData("Please enter valid OTP !!");
				}
			}
			else
			{
				log.info("---------user credentials in request empty string or null-------------");
				responseJson = createFailureResponseData("please enter valid username or OTP !!");
			}

		}
		else
		{

			log.info("--- data from request(userName, otp,newPassword)" + userName + capcha + newPassword
					+ "-------------");
			SystemUser user = new SystemUser();
			if ((null != userName && Constants.EMPTY_STRING != userName)
					&& (null != capcha && Constants.EMPTY_STRING != capcha)
					&& (null != newPassword && Constants.EMPTY_STRING != newPassword))
			{
				user.setUsername(userName);
				user.setPassword(newPassword);
				user.setOldPassword(oldPassword);
				user.setOTP(capcha);

				log.info("------ calling isValidOTP() / capcha method-------------");
				if (isValidOTP(user))
				{
					log.info("------ ValidOTP / ValidCapcha  successful For LDap -------------");
					log.info("------ calling changedLdapPassword() method For LDap -------------");
					try
					{
						String searchbase = applicationConfiguration.getValue("LDAP_SEARCH_BASE");
						String ldapURL = applicationConfiguration.getValue("LDAP_URL");
						String usernameprefix = applicationConfiguration.getValue("PREFIX_SEARCH_USERNAME");

						log.info("---searchbase---" + searchbase);
						log.info("-----ldapURL----" + ldapURL);
						log.info("----usernameprefix----" + usernameprefix);
						if (userService.changedLdapPassword(user, searchbase, ldapURL, usernameprefix))
						{
							responseJson = createSuccessResponseData();

						}
						else
						{
							log.info("--------- changedLdapPassword failed -------------");
							responseJson = createFailureResponseData("password reset  failed please try again");

						}

					}
					catch (Exception e)
					{
						log.error("-----exception Details is ------", e);

						responseJson = createFailureResponseData("password reset  failed please try again");

					}
				}
				else
				{
					log.info("---------OTP / Capcha in request is not valid -------------");
					responseJson = createFailureResponseData("Please enter valid capcha !!");
				}
			}
			else
			{
				log.info("---------user credentials in request empty string or null-------------");
				responseJson = createFailureResponseData("please enter valid username or Capcha !!");
			}

		}

		return genrateResponse(message, responseJson);
	}

	private String generateOTPForDevice()
	{
		log.info("----------in genrated otp() in password service-------------");

		String date = Utilities.generateDate("yyyy-MM-dd");

		Timestamp t = Utilities.generateTimestamp("yyyy-MM-dd", date);

		long l = t.getTime() / 10000000;

		String str = String.valueOf(Math.floor(l * Math.random()));

		String newExitPassword = str.substring(0, str.indexOf("."));

		if (newExitPassword.length() > 4)
		{
			newExitPassword = newExitPassword.substring(0, 4);
		}
		else if (newExitPassword.length() < 4)
		{
			newExitPassword = newExitPassword + "9";
		}
		return newExitPassword;
	}

	private boolean sendOTPSMS(Map optSMSMap)
	{
		log.info("---------- in side sendOTPSMS() in password service -------------");

		try
		{
			
			KotakCollectionWebserviceAdapter kotakCollectionWebserviceAdapter = new KotakCollectionWebserviceAdapter();

			log.info("---- Before call to SMS Web Service---------");

			Map<String, Object> result = kotakCollectionWebserviceAdapter.callWebserviceAndGetMap(optSMSMap.toString(),
					(String) applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER"));

			log.info("----- Result of SMS Dispatch : -------" + result);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private JSONObject createSuccessResponseData()
	{
		log.info("--------- in createSuccessResponseData() of password service-------------");
		HashMap data = new HashMap();
		JSONObject successResponseJson = new JSONObject();
		try
		{
			successResponseJson.put("status", JsonConstants.SUCCESS);
			successResponseJson.put("message", JsonConstants.SUCCESS);
			successResponseJson.put("data", data);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return successResponseJson;
	}

	@SuppressWarnings("unchecked")
	private JSONObject createFailureResponseData(String failureMessage)
	{
		log.info("--------- in createFailureResponseData() of password service-------------");
		HashMap data = new HashMap();
		//		data.put("failureMessage", failureMessage);
		JSONObject failureResponseJson = new JSONObject();
		try
		{
			failureResponseJson.put("status", JsonConstants.FAILURE);
			failureResponseJson.put("message", failureMessage);
			failureResponseJson.put("data", data);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return failureResponseJson;
	}

	private Message<String> genrateResponse(Message<String> message, JSONObject responseJson)
	{
		log.info("--------- in genrateResponse() of password service-------------");
		String responseString = responseJson.toString();
		return MessageBuilder.withPayload(responseString).copyHeaders(message.getHeaders()).build();
	}

	private boolean isValidOTP(SystemUser systemUser)
	{
		/*
		 * String otp = userService.getOTP(user.getUsername());
		 * System.out.println("otp in db=" + otp); System.out.println("otp in request" +
		 * user.getOTP()); if (user.getOTP().equalsIgnoreCase(otp)) { return true; }
		 */
		
		Map<String,String> checkOTPDataParameter = new HashMap<String, String>();
		
		checkOTPDataParameter.put(Constants.USER_ID, systemUser.getUserTableId() == null ? "" : systemUser.getUserTableId().toString() );
		
		String requestId = systemUser.getReqId() == null ? "" : systemUser.getReqId().toString();
		String requetsOTP = systemUser.getOTP() == null ? "" : systemUser.getOTP().toString();			
		
		/*
		 * List<Map<String, Object>> fetchUserOTPDetails =
		 * generateOTPService.checkOTPDetails(checkOTPDataParameter);
		 * 
		 * if(!fetchUserOTPDetails.isEmpty()) {
		 */
			/*
			 * Map<String, Object> fetchUserOTPDetailsMap = fetchUserOTPDetails.get(0);
			 * String latestOtp =
			 * fetchUserOTPDetailsMap.get(DBColumnNameConstants.REQUESTED_OTP) == null ?
			 * Constants.EMPTY_STRING :
			 * fetchUserOTPDetailsMap.get(DBColumnNameConstants.REQUESTED_OTP).toString();
			 * 
			 * if(StringUtils.isEmpty(latestOtp)) { return false; }
			 */
			
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
			bodyParam.put(Constants.smsParam.REQUESTID_VALUE,requestId);
			bodyParam.put(Constants.smsParam.OTP_VALUE, requetsOTP);

			String otpRequest = SmsFormXML.generateOtpSMSXml(parameters, mergeVarParam, bodyParam);

			parametersMap.put(Constants.LdapParam.LDAP_URL,
					applicationConfiguration.getValue(Constants.smsParam.VAL_TYPE));
			parametersMap.put(Constants.LdapParam.LDAPREQUEST, otpRequest == null ? "" : otpRequest);
			String responseString = Utilities.postXML(parametersMap);

			boolean isOtpValid = SmsFormXML.generateOtpValResBodySMSXml(responseString);
			
			if(isOtpValid)
			{
				return true;
			 } 
		/*
		 * } else { return false;
		 * 
		 * }
		 */
		
		return false;
	}

	private Date getCurrentSystemDate()
	{
		log.info("------------- in getCurrentSystemDate() in password service");
		Date currentDate = new Date(new Timestamp(System.currentTimeMillis()).getTime());
		log.info("------------- currentSystemDate = " + currentDate + "-------------");
		return currentDate;
	}

	private boolean isSameDateOfOTPRequest(String userName)
	{
		log.info("------------- in isSameDateOfOTPRequest() in password service");
		Date currentDate = getCurrentSystemDate();
		log.info("------------- current systemdate= -------" + currentDate);
		Date otpRequestDate = userService.getOTPRequestDate(userName);
		log.info("------------- otpRequestDate= -------" + otpRequestDate);
		if (null != otpRequestDate)
		{
			if ((currentDate.toString()).equalsIgnoreCase(otpRequestDate.toString()))
			{
				log.info("------------- otp requested on same date-------");
				return true;
			}
		}
		return false;
	}

	private JSONObject createSuccessResponseData(SystemUser systemUser)
	{
		log.info("--------- in createSuccessResponseData() of password service-------------");
		JSONObject map = new JSONObject();

		JSONObject successResponseJson = new JSONObject();

		try
		{

			map.put("loginType", systemUser.getLoginType());

			if (systemUser.getLoginType().equalsIgnoreCase("Ldap"))
			{
				map.put("capcha", systemUser.getOTP());

			}
			else
			{
				map.put("reqId", systemUser.getOTP());
			}
			
			map.put("roleID", String.valueOf(systemUser.getRoleId()));

			successResponseJson.put("status", JsonConstants.SUCCESS);
			successResponseJson.put("message", JsonConstants.SUCCESS);
			successResponseJson.put("data", map);

		}

		catch (Exception e)
		{

			log.error("---Exception Details ----", e);

		}
		return successResponseJson;
	}
	
	//This method will get called when user login first time or when click on reset password link 
	public Message<String> changeUserPassword(Message<String> message)
	{
		log.info("--- In PasswordService / changeUserPassword Method ---");
		JSONObject responseJson = null;
		String data = (String) JSONPayloadExtractor.extract(message.getPayload(), JsonConstants.DATA);
		String userName = JSONPayloadExtractor.extract(data, JsonConstants.USERNAME);
		String newPassword = JSONPayloadExtractor.extract(data, JsonConstants.PASSWORD);
		String oldPassword = JSONPayloadExtractor.extract(data, "oldPassword");

		try
		{
			log.info("test1");
			String key = applicationConfiguration.getValue("key");
			String iv = applicationConfiguration.getValue("iv");

			IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
			SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");

			Cipher aes1 = Cipher.getInstance("AES/CBC/NoPadding");
			aes1.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
			byte[] decodedArray = new Base64().decode(oldPassword.getBytes());

			oldPassword = new String(aes1.doFinal(decodedArray));
			oldPassword = oldPassword.trim();

			byte[] decodedArray1 = new Base64().decode(newPassword.getBytes());

			newPassword = new String(aes1.doFinal(decodedArray1));
			newPassword = newPassword.trim();

			log.info("---- oldPassword decrypted password -----" + oldPassword);
			log.info("---- new decrypted password -----" + newPassword);

		}
		catch (Exception e)
		{
			log.error("Exception while decrypting password :: " + e);
		}
		
		/*oldPassword = AES.decrypt(oldPassword);
		newPassword = AES.decrypt(newPassword);*/
		SystemUser sysUser = null;
		try
		{
			sysUser = userService.getUser(userName);
		}
		catch(Exception e)
		{
			
		}
		if (sysUser == null)
		{
			log.info("--- invalid username ---");
			responseJson = createFailureResponseData("invalid username or Old Password");
		}
		else
		{
			Long tempUserId = 0L;
	
			Authentication authentication = new Authentication();
			authentication.setLogin(userName == null ? "" : userName);
			authentication.setPassword(oldPassword);
			authentication.setSystemUserId(sysUser.getUserTableId());
	
			if (sysUser.getLoginType().equalsIgnoreCase("Normal"))
			{
				
				log.info("--- data from request(userName, oldPassword ,newPassword :: )" + userName + " , " + oldPassword + " , " + newPassword
						+ "-------------");
				if ((null != userName && Constants.EMPTY_STRING != userName)
						&& (null != oldPassword && Constants.EMPTY_STRING != oldPassword)
						&& (null != newPassword && Constants.EMPTY_STRING != newPassword))
				{
					//tempUserId = authenticationService.validateMember(authentication);
					tempUserId = userService.validateMember(authentication);
					SystemUser user = new SystemUser();
					user.setUsername(userName);
					user.setPassword(newPassword);
					user.setModifiedBy(sysUser.getUserTableId());
					log.info("------ calling isValidOTP() method-------------");
					if (tempUserId != null && tempUserId > 0)
					{
						log.info("------ userAuthentication  successful-------------");
						log.info("------ checking previous password -------------");
						
						
						//To check previous password Device start
						Authentication authenticationTemp = new Authentication();
						authenticationTemp.setLogin(userName);
						authenticationTemp.setPassword(newPassword);
						authenticationTemp.setSystemUserId(sysUser.getUserTableId());
						
	
						/*
						 * if (userService.checkPreviousPasswords(authenticationTemp)) {
						 * log.info("--- New password Matched with privious 5 password --- ");
						 * responseJson = createFailureResponseData("Previous password not allowed"); }
						 * //To check previous passwords Device end else {
						 */
							if(isValidPassword(user.getPassword()) == false)
							{
								log.info("--------- pattern not matched -------------");
								responseJson = createFailureResponseData("Password does not meet the requirements.");
							}
							else if (userService.updatePassword(user))
							{
								log.info("------ updatePassword()  successful-------------");
								
								//To add entry in password_reset_history Device Start
								
								//authenticationTemp.setPassword(AES.encrypt(authenticationTemp.getPassword()));
								authenticationTemp.setPassword(PasswordEncoder.encode(authenticationTemp.getPassword()));
								log.info("authenticationTemp.getPassword() Encrypted :: " + authenticationTemp.getPassword());
								//need encrypted password
								if (userService.addPasswordResetHistory(authenticationTemp,sysUser.getUserTableId()))
								{
									log.info("--- Entry added in password_Reset_History table ---");
								}
								else
								{
									log.info("--- Unable to add entry in password_Reset_History table ---");
								}
								//To add entry in password_reset_history Device End
								
								responseJson = createSuccessResponseData();
							}
							else
							{
								log.info("--------- updatepassword() failed -------------");
								responseJson = createFailureResponseData("password reset  failed please try again");
							}
						//}
						
						
					}
					else
					{
						log.info("---------invalid username or Old Password -------------");
						responseJson = createFailureResponseData("invalid username or Old Password");
					}
				}
				else
				{
					log.info("---------user credentials in request empty string or null-------------");
					responseJson = createFailureResponseData("please enter valid username or old password");
				}
	
			}
			else
			{
	
				log.info("--- Ldap users ---");
				log.info("--- data from request(userName, otp,newPassword)" + userName + oldPassword + newPassword
						+ "-------------");
				SystemUser user = new SystemUser();
				if ((null != userName && Constants.EMPTY_STRING != userName)
						&& (null != oldPassword && Constants.EMPTY_STRING != oldPassword)
						&& (null != newPassword && Constants.EMPTY_STRING != newPassword))
				{
					user.setUsername(userName);
					user.setPassword(newPassword);
					user.setOldPassword(oldPassword);
					//user.setOTP(capcha);
	
					//log.info("------ calling isValidOTP() / capcha method-------------");
					/*if (isValidOTP(user))
					{*/
						//log.info("------ ValidOTP / ValidCapcha  successful For LDap -------------");
						//log.info("------ calling changedLdapPassword() method For LDap -------------");
						try
						{
							String searchbase = applicationConfiguration.getValue("LDAP_SEARCH_BASE");
							String ldapURL = applicationConfiguration.getValue("LDAP_URL");
							String usernameprefix = applicationConfiguration.getValue("PREFIX_SEARCH_USERNAME");
	
							log.info("---searchbase---" + searchbase);
							log.info("-----ldapURL----" + ldapURL);
							log.info("----usernameprefix----" + usernameprefix);
							if(isValidPassword(user.getPassword()) == false)
							{
								log.info("--------- pattern not matched -------------");
								responseJson = createFailureResponseData("Password does not meet the requirements.");
							}
							else if (userService.changedLdapPassword(user, searchbase, ldapURL, usernameprefix))
							{
								responseJson = createSuccessResponseData();
	
							}
							else
							{
								log.info("--------- changedLdapPassword failed -------------");
								responseJson = createFailureResponseData("password reset  failed please try again");
	
							}
	
						}
						catch (Exception e)
						{
							log.error("-----exception Details is ------", e);
	
							responseJson = createFailureResponseData("password reset  failed please try again");
	
						}
					/*}
					else
					{
						log.info("---------OTP / Capcha in request is not valid -------------");
						responseJson = createFailureResponseData("Please enter valid capcha !!");
					}*/
				}
				else
				{
					log.info("---------user credentials in request empty string or null-------------");
					responseJson = createFailureResponseData("please enter valid username or Capcha !!");
				}
	
			}
		}

		return genrateResponse(message, responseJson);
	}
	
	public Message<String> generateForgotPasswordOtp(Message<String> message)
	{
		JSONObject responseJson = null;

		log.info("--------- generateForgotPasswordOtp called -------------");

		String mobileNumber = null;

		try
		{
			
			String data = (String) JSONPayloadExtractor.extract(message.getPayload(), JsonConstants.DATA);
			String userName = JSONPayloadExtractor.extract(data, JsonConstants.USERNAME);
			
			log.info("-------------in password service:  user requested for otp---------" + userName);
			
			Map<String,Object> responseMap = new HashMap<String, Object>();
			
			if (userName != null && userName != Constants.EMPTY_STRING)
			{
				SystemUser systemUser = userService.getUser(userName);

				if (null != systemUser)
				{
					log.info("------------- system user is not null ---------");

					log.info("---user type----" + systemUser.getLoginType()+systemUser);

					if (systemUser.getLoginType().equalsIgnoreCase("Normal"))
					{

							String requestId = String.valueOf(System.currentTimeMillis());
			
							mobileNumber = systemUser.getMobileNumber();
							
							systemUser.setOTP(requestId);

							log.info("------------- MobileNumber = " + mobileNumber + "---------");
							if (Constants.EMPTY_STRING != mobileNumber && null != mobileNumber)
							{
								
								try
								{
										HashMap dataMap = new HashMap();
										String userMobileNumber = systemUser.getMobileNumber() == null ? "" : systemUser.getMobileNumber().toString();
										String userId = systemUser.getUserTableId() == null ? "" : systemUser.getUserTableId().toString();
										String type = "GenforgotPassword";
										
										if(StringUtils.isEmpty(userMobileNumber))
										{
											responseMap.put(Constants.MESSAGE, Constants.Messages.OPT_GENERATE_FAILURE_MOBILE);
											responseMap.put(Constants.JSON_RESPONSE_STATUS, JsonConstants.FAILURE);
											responseMap.put(Constants.DATA_MAP, dataMap);
											return Utilities.deviceResponse(message,responseMap);
										}
									
										HashMap<String,Object> configMap = collectionService.getRblConfig(Constants.FORGOT_PASSWORD_OTP_URL_CONFIG_KEY);
										if(configMap.containsKey(Constants.URL))
										{
											ForgotPasswordAPIUtilities forgotPasswordAPIUtilities = new ForgotPasswordAPIUtilities();
											
											HashMap<String, String> otpMap = new HashMap<String,String>();
											otpMap.put(Constants.MOBILE_NUMBER,userMobileNumber);
											otpMap.put(Constants.REQ_ID,requestId);
											otpMap.put(Constants.URL,(String) configMap.get(Constants.URL));
											
											boolean otpGenerated = forgotPasswordAPIUtilities.generateOTP(otpMap);

											if(otpGenerated)
											{
												collectionService.resetOtpAttemts(systemUser);
												
												Map<String, Object> updateOtpReqId = new HashMap<String, Object>();
												updateOtpReqId.put(Constants.USER_TABLE_ID,
														systemUser.getUserTableId() == null ? Constants.EMPTY_STRING
																: systemUser.getUserTableId());
									
												updateOtpReqId.put(Constants.smsParam.REQUESTID_VALUE, requestId);

												boolean dbUpdated = collectionService.updateFOtpRequestId(updateOtpReqId, requestId);
												
												if(dbUpdated)
												{
													log.info("---------otp updated successfully in db-------------");
													responseJson = createSuccessResponseData(systemUser);

												}
												else
												{
													log.info("---------otp update in db failed -------------");

													responseJson = createFailureResponseData("otp not saved in db ");
												}
											}
											else
											{
												log.info("---------otp sms sent failed -------------");

												responseJson = createFailureResponseData("otp sms sending failed");
											}
										}
										else
										{
											log.info("---------OTP URL configuration failed-------------");

											responseJson = createFailureResponseData("OTP URL configuration failed");
										}
										
										
								}
								catch (Exception e)
								{

									log.error("---- Exception Detail ----:-"+e);

								}
								 

							//	
							}
							else
							{
								log.info("------------- supervisorMobileNumber is null---------");
								responseJson = createFailureResponseData("mobile number for username not available");
							}
					}
					else
					{
						log.info("--------- username in request is ldap user -------------");
						responseJson = createFailureResponseData( Constants.LDAP_MESSAGE);
					}
				}
				else
				{
					log.info("--------- username in request is null -------------");
					responseJson = createFailureResponseData("please enter valid username");
				}
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			log.info("---------exception in get user request for otp-------------");
			e.printStackTrace();
		}
		return genrateResponse(message, responseJson);

	}
	
	public Message<String> validateForgotPasswordOtp(Message<String> message)
	{
		JSONObject responseJson = null;
		log.info("---------- in side validateForgotPasswordOtp() in password service -------------");
		String data = (String) JSONPayloadExtractor.extract(message.getPayload(), JsonConstants.DATA);
		String userName = JSONPayloadExtractor.extract(data, JsonConstants.USERNAME);
		String otp = JSONPayloadExtractor.extract(data, JsonConstants.OTP);
		String reqId = JSONPayloadExtractor.extract(data, JsonConstants.REQID);
		String capcha = JSONPayloadExtractor.extract(data, "capcha");
		String newPassword = JSONPayloadExtractor.extract(data, JsonConstants.PASSWORD);
		String oldPassword = JSONPayloadExtractor.extract(data, "oldPassword");

		try
		{
			String key = applicationConfiguration.getValue("key");
			String iv = applicationConfiguration.getValue("iv");

			IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
			SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");

			Cipher aes1 = Cipher.getInstance("AES/CBC/NoPadding");
			aes1.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
			byte[] decodedArray = new Base64().decode(oldPassword.getBytes());

			oldPassword = new String(aes1.doFinal(decodedArray));
			oldPassword = oldPassword.trim();

			byte[] decodedArray1 = new Base64().decode(newPassword.getBytes());

			newPassword = new String(aes1.doFinal(decodedArray1));
			newPassword = newPassword.trim();

			log.info("---- oldPassword decrypted password -----" + oldPassword);
			log.info("---- new decrypted password -----" + newPassword);

		}
		catch (Exception e)
		{
			log.error("Exception while decrypting password :: " + e);
		}

		SystemUser sysUser = userService.getUser(userName);

		if(sysUser!=null)
		{
			if (sysUser.getLoginType().equalsIgnoreCase("Normal"))
			{
				log.info("--- data from request(userName, otp,newPassword)" + userName + otp + newPassword
						+ "-------------");
				if ((null != userName && Constants.EMPTY_STRING != userName)
						&& (null != otp && Constants.EMPTY_STRING != otp)
						&& (null != newPassword && Constants.EMPTY_STRING != newPassword))
				{
					SystemUser user = new SystemUser();
					user.setUsername(userName);
					user.setOTP(otp);
					user.setPassword(newPassword);
					user.setModifiedBy(sysUser.getUserTableId());
					user.setReqId(reqId);
					log.info("------ calling isValidOTP() method-------------");
				
						HashMap<String,Object> configMap = collectionService.getRblConfig(Constants.FORGOT_PASSWORD_OTP_URL_CONFIG_KEY);

						if(configMap.containsKey(Constants.URL_VALIDATE))
						{
							ForgotPasswordAPIUtilities forgotPasswordAPIUtilities = new ForgotPasswordAPIUtilities();

							HashMap<String, String> otpMap = new HashMap<String,String>();
							otpMap.put(Constants.OTP,otp);
							otpMap.put(Constants.REQ_ID,reqId);
							otpMap.put(Constants.URL,(String) configMap.get(Constants.URL_VALIDATE));
							
							String status = forgotPasswordAPIUtilities.validateOTP(otpMap);
				
							if (status.equalsIgnoreCase("VALID"))//isOtpValid)
							{
								log.info("------ isValidOTP()  successful-------------");
								log.info("------ calling updatePassword() method-------------");
								
								//To check previous password Device start
								Authentication authenticationTemp = new Authentication();
								authenticationTemp.setLogin(userName);
								authenticationTemp.setPassword(newPassword);
								authenticationTemp.setSystemUserId(sysUser.getUserTableId());
								
								/*log.info("authenticationTemp.getLogin() :: " + authenticationTemp.getLogin());
								log.info("authenticationTemp.getPassword() decrypted:: " + authenticationTemp.getPassword());
								log.info("authenticationTemp.getSystemUserId() :: " + authenticationTemp.getSystemUserId());*/

								/*
								 * if (userService.checkPreviousPasswords(authenticationTemp)) {
								 * log.info("--- New password Matched with privious 5 password --- ");
								 * responseJson = createFailureResponseData("Previous password not allowed"); }
								 * //To check previous passwords Device end else
								 */
								{
									if (userService.updatePassword(user))
									{
										log.info("------ updatePassword()  successful-------------");
										
										responseJson = createSuccessResponseData();
									}
									else
									{
										log.info("--------- updatepassword() failed -------------");
										responseJson = createFailureResponseData("password reset failed please try again");
									}
								}
								
								
							}
							else if(status.equalsIgnoreCase("TIMEOUT"))
							{
								log.info("---------otp expired -------------");
								responseJson = createFailureResponseData("OTP Expired.");
							}
							else
							{
								boolean otpAttemtValid = collectionService.ValidateFOtp(sysUser);
								
								if(otpAttemtValid)
								{
									log.info("---------otp in request is not valid -------------");
									responseJson = createFailureResponseData("Please enter valid OTP !!");
								}
								else
								{
									log.info("---------otp in request max attemp extint -------------");
									responseJson = createFailureResponseData("3 attempts reached, generate new Otp to proceed.");
							
								}
								
							}
						}
						else
						{
							log.info("---------otp in request is not valid -------------");
							responseJson = createFailureResponseData("OTP URL configuration failed !!");
						}
					
				}
				else
				{
					log.info("---------user credentials in request empty string or null-------------");
					responseJson = createFailureResponseData("please enter valid username or OTP !!");
				}

			}
			else
			{

				log.info("--- data from request(userName, otp,newPassword)" + userName + capcha + newPassword
						+ "-------------");
				responseJson = createFailureResponseData(Constants.LDAP_MESSAGE);

			}
		}
		else
		{
			responseJson = createFailureResponseData("Invalid user.");
		}
		return genrateResponse(message, responseJson);
	}
	
	public static boolean isValidPassword(String password)
	{
		String regex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^{}])[a-zA-Z0-9!@#$%^{}]{8,}$";
		Pattern p = Pattern.compile(regex);
		if (password == null) {
            return false;
        }
		
		Matcher m = p.matcher(password);
		return m.matches();
	}
}
