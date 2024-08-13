package com.mobicule.mcollections.integration.messaging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.support.MessageBuilder;

import com.mobicule.component.version.beans.Version;
import com.mobicule.component.version.service.VersionService;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.AES;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.SystemUserService;
import com.mobicule.mcollections.integration.commons.ServerUtilities;
import com.mobicule.mcollections.integration.systemuser.ISystemUserAuthenticationService;

public class SimpleRequestReceiver implements IRequestReceiver {
	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private ISystemUserAuthenticationService systemUserAuthenticationService;

	@Autowired
	private VersionService versionService;

	@Autowired
	private SystemUserService systemUserService;
	
	@Autowired
	private ApplicationConfiguration<String, Object> applicationConfigurationObject;

	@SuppressWarnings("unused")
	@Override
	public Message<String> receive(Message<String> message) {
		try {
			log.info(" -------- Request -------- ");

			String receivedMessage = message.getPayload();
			Map reqMap = Utilities.createMapFromJSON(receivedMessage);

			String type = (String) reqMap.get(JsonConstants.Key.TYPE);
			String entity = (String) reqMap.get(JsonConstants.Key.ENTITY);
			String action = (String) reqMap.get(JsonConstants.Key.ACTION);

			JSONObject requestData = JSONPayloadExtractor.extractJSON(receivedMessage, JsonConstants.DATA);
			
			JSONObject jsonUser = JSONPayloadExtractor.extractJSON(receivedMessage,JsonConstants.SYSTEM_USER);

			String reSendOtp = Constants.EMPTY_STRING;

			Map userMap = (Map) reqMap.get(JsonConstants.SYSTEM_USER);
			Double osVer = 0.0;
			SystemUser systemUser = new SystemUser();
			systemUser.setUsername(userMap.get("username").toString().toLowerCase());
			log.info("systemUser.setUsername :::: " + systemUser.getUsername());
			systemUser.setImeiNo(userMap.get("imei").toString());
			JSONObject deviceDetails = JSONPayloadExtractor.extractJSON(receivedMessage,JsonConstants.DEVICE_DETAILS);
			
			//systemUser.setUserTableId(Long.valueOf(systemUserId));
			
			
			
			//Android version check 
			try {
				if (deviceDetails.has("osVersion")) {
					String osVersion = deviceDetails.get("osVersion") == null ? ""
							: String.valueOf(deviceDetails.get("osVersion"));
					log.info("OsVesion in request::::::::" + osVersion);

					if (!osVersion.equalsIgnoreCase(Constants.EMPTY_STRING)) {

						float osVersionFloat = Float.parseFloat(osVersion);
						/*
						 * List<String> allowedOsversionList = (List<String>)
						 * applicationConfigurationObject .getValue("ALLOWED_ANDROID_VERSION_LIST");
						 */
						String  lowestAllowedVersion =  applicationConfigurationObject .getValue("ALLOWED_ANDROID_VERSION") == null ? "10" : (String) applicationConfigurationObject .getValue("ALLOWED_ANDROID_VERSION");
						long  allowedVersion  = Long.parseLong(lowestAllowedVersion); 
						
						log.info("lowest Allowed Version  ::::::::" + lowestAllowedVersion);

						
						if (allowedVersion > 0) {

							if (osVersionFloat<allowedVersion){
								JSONObject responseJSON = new JSONObject();
								responseJSON.put(JsonConstants.DATA, "");
								responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
								responseJSON.put(JsonConstants.MESSAGE,
										"We have detected that the application has been tampered.");
								return MessageBuilder.withPayload(String.valueOf(responseJSON))
										.copyHeaders(message.getHeaders()).build();
							}
						}

					}
				}
			} catch (Exception e) {
				log.info("Exception in Android version check  ::: " + e);
			}
			
			
			
			if (userMap.containsKey("osVersion")) {
				log.info("systemUser.getAndroidVersion()" + systemUser.getAndroidVersion());

				systemUser.setAndroidVersion(userMap.get("osVersion") == null ? Constants.EMPTY_STRING
						: userMap.get("osVersion").toString());
				if (systemUser.getAndroidVersion() == null || systemUser.getAndroidVersion().length() == 0) {
					osVer = osVer;
				} else {
					String ver = systemUser.getAndroidVersion().split("\\.")[0];
					log.info("verSplit " + ver);
					osVer = Double.parseDouble(ver);
					log.info("osVer " + osVer);
				}
			}
			
			
			

			SystemUser systemUserClone = (SystemUser) BeanUtils.cloneBean(systemUser);

			// changes for forgot password
			if (type.equalsIgnoreCase("changeRequest")) {
				log.info(" -------- Request is for change request -------- ");
				JSONObject versionJSON = JSONPayloadExtractor.extractJSONObject(receivedMessage,
						(JsonConstants.VERSION));

				Version version = versionService.extractVersion(versionJSON);

				version = versionService.checkVersionDatabase(version, false, false);

				if (!(version.getStatus()).equals("CHANGE")) {
					MessageHeaders messageHeader = message.getHeaders();

					String digestMatchStatus = messageHeader.get("digestMatchStatus") != null
							? (String) messageHeader.get("digestMatchStatus")
							: "Yes";

					if (digestMatchStatus.equalsIgnoreCase("Yes"))
					// if(true)
					{
						String userName = (String) userMap.get(Constants.USERNAME);

						if (systemUserAuthenticationService.isFutureDateExpired(userName)) {
							log.info(" -------- Future Date not reset before fixed interval -------- ");
							return MessageBuilder.withPayload(message.getPayload())
									.setHeader(JsonConstants.Key.ENTITY, entity)
									.setHeader(JsonConstants.Key.TYPE, type)
									.setHeader(JsonConstants.Key.ACTION, action)
									.setHeader(JsonConstants.Key.CHECK_REQUEST, "futureExpiry")
									.copyHeaders(message.getHeaders()).build();
						}

						if (!systemUserAuthenticationService.isResetBeforeFixedInterval(userName) && systemUser.getLoginType().equalsIgnoreCase("Normal")) {
							log.info(" -------- password not reset before fixed interval -------- ");
							return MessageBuilder.withPayload(message.getPayload())
									.setHeader(JsonConstants.Key.ENTITY, entity)
									.setHeader(JsonConstants.Key.TYPE, type)
									.setHeader(JsonConstants.Key.ACTION, action)
									.setHeader(JsonConstants.Key.CHECK_REQUEST, "autoResetPassword")
									.copyHeaders(message.getHeaders()).build();
						} else {
							return MessageBuilder.withPayload(message.getPayload())
									.setHeader(JsonConstants.Key.ENTITY, entity)
									.setHeader(JsonConstants.Key.TYPE, type)
									.setHeader(JsonConstants.Key.ACTION, action)
									.setHeader(JsonConstants.Key.CHECK_REQUEST, "valid")
									.copyHeaders(message.getHeaders()).build();
						}
					} else {
						Map<String, Object> errorMap = new HashMap<String, Object>();

						errorMap.put("status", "failure");
						errorMap.put("message", "Unauthorized Request");
						errorMap.put("data", "");

						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						new ObjectMapper().writeValue(baos, errorMap);

						MessageBuilder.withPayload(baos.toString())
								.setHeader("response", message.getHeaders().get("response")).build();
					}
				} else {
					return MessageBuilder.withPayload(message.getPayload()).setHeader(JsonConstants.Key.ENTITY, entity)
							.setHeader(JsonConstants.Key.TYPE, type).setHeader(JsonConstants.Key.ACTION, action)
							.setHeader(JsonConstants.Key.VERSION_NUMBER, version.getVersionOnServer())
							.setHeader(JsonConstants.Key.APPLICATION_DOWNLOAD_LINK, version.getURL())
							.setHeader(JsonConstants.Key.CHECK_REQUEST, "versionupgrade")
							.copyHeaders(message.getHeaders()).build();
				}
			}
			else if(type.equalsIgnoreCase("changeHandset"))
			{
				String successMsg = "ChangeHandset request successfully submitted";
				String failureMsg = "Failed to submit changeHandset request";
				
				Map<String,String> userDataMap = new HashMap<String, String>();
				
				String resultFlag = systemUserAuthenticationService.changeHandsetImei(userMap);
				
				if(resultFlag.equalsIgnoreCase("true"))
				{
					return  ChangeHandsetResponse(message , successMsg);
				
				}
				else
				{
					return ChangeHandsetFailureResponse(message , failureMsg);
				}
			}
			else
			{
				SystemUser systemUserNew = new SystemUser();
				String result = Constants.EMPTY_STRING;
				String isUserRegistered = Constants.EMPTY_STRING;
				systemUserNew.setAndroidVersion(systemUser.getAndroidVersion());
				if (action.equalsIgnoreCase("generateOtp") || action.equalsIgnoreCase("otpVerification")) {
					result = systemUserAuthenticationService.authenticateUser(userMap, systemUserNew, "AndroidTen");
				} else if (systemUser.getAndroidVersion() != null
						&& systemUser.getAndroidVersion() != Constants.EMPTY_STRING && osVer >= 10) {
					if (userMap.containsKey(JsonConstants.PASSWORD)) {
						String password = (String) userMap.get(JsonConstants.PASSWORD);
						password = AES.decrypt(password);
						systemUser.setPassword(password);
					}
					isUserRegistered = systemUserAuthenticationService.authenticateUser(systemUser);
					log.info("isUserRegistered :: " + isUserRegistered);
					if (isUserRegistered.equalsIgnoreCase(Constants.EMPTY_STRING)) {
						log.info("--- No data found--- ");
						JSONObject responseJSON = new JSONObject();
						responseJSON.put(JsonConstants.DATA, "");
						responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
						responseJSON.put(JsonConstants.MESSAGE, "You are not an authorized user. Please log out.");

						return MessageBuilder.withPayload(String.valueOf(responseJSON))
								.copyHeaders(message.getHeaders()).build();
					}
					if (isUserRegistered.equalsIgnoreCase("ImeiChanged")
							|| isUserRegistered.equalsIgnoreCase("RegStatusBlank")) {
						log.info("--- Android 10 user ImeiChanged--- ");
						JSONObject responseJSON = new JSONObject();
						responseJSON.put(JsonConstants.DATA, "");
						responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
						responseJSON.put(JsonConstants.MESSAGE,
								"You are using Android 10 & above device, do you want to register ?");
						return MessageBuilder.withPayload(String.valueOf(responseJSON))
								.copyHeaders(message.getHeaders()).build();
					}
					if (isUserRegistered.equalsIgnoreCase("ACTIVE")) {
						result = systemUserAuthenticationService.authenticateUser(userMap, systemUserNew);
					}
					if (isUserRegistered.equalsIgnoreCase("PENDING")) {
						log.info("--- Android 10 user is not registered--- ");
						JSONObject responseJSON = new JSONObject();
						responseJSON.put(JsonConstants.DATA, "");
						responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
						responseJSON.put(JsonConstants.MESSAGE,
								"Your request is not yet validated by system admin. Please try after some time.");

						return MessageBuilder.withPayload(String.valueOf(responseJSON))
								.copyHeaders(message.getHeaders()).build();
					}
					if (isUserRegistered.startsWith("B_")) {
						result = isUserRegistered;
					}
				} else {

					result = systemUserAuthenticationService.authenticateUser(userMap, systemUserNew);
				}
				log.info("result::" + result);
				log.info("---system user ----" + systemUserNew);
				if(userMap.containsKey("isAdfs") && userMap.get("isAdfs").equals("Yes")) {
					systemUserNew.setIsAdfs("Yes");
				}else {
					systemUserNew.setIsAdfs("No");
				}
				

				Long resultID = 0L;
				if(result.isEmpty() || result.startsWith("PF_"))
				{
					SystemUser invalidUser = new SystemUser();
					invalidUser.setUsername(userMap.get("username") == null ? Constants.EMPTY_STRING : userMap.get("username").toString());					
					invalidUser = systemUserService.getUser(invalidUser);
					invalidUser.setUsername(userMap.get("username") == null ? Constants.EMPTY_STRING : userMap.get("username").toString());
					invalidUser.setUserTableId(invalidUser.getId() == null ? 0L : Long.valueOf(invalidUser.getId()));
					boolean invalidLoginStatus = systemUserService.checkLockedStatus(invalidUser, false);
					log.info("--- invalid credantials --- " + invalidLoginStatus);
					
					if(!invalidLoginStatus) {
						return MessageBuilder.withPayload(message.getPayload())
								.setHeader(JsonConstants.Key.ENTITY, entity).setHeader(JsonConstants.Key.TYPE, type)
								.setHeader(JsonConstants.Key.ACTION, action)
								.setHeader(JsonConstants.Key.CHECK_REQUEST, "accountLocked")
								.copyHeaders(message.getHeaders()).build();
					}
				}

				// check for blocked user
				if (result.startsWith("IM_")) {
					log.info("--- Imei Number Already Exists. --- ");
				  JSONObject responseJSON = new JSONObject();
				  responseJSON.put(JsonConstants.DATA, "");
				  responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
				  responseJSON.put(JsonConstants.MESSAGE, "IMEI Number Already Exists.");
				  return MessageBuilder.withPayload(responseJSON.toString()).copyHeaders(message.getHeaders()).build();
				}
				if (result.startsWith("B_")) {
					log.info("--- user is blocked --- ");
					return MessageBuilder.withPayload(message.getPayload()).setHeader(JsonConstants.Key.ENTITY, entity)
							.setHeader(JsonConstants.Key.TYPE, type).setHeader(JsonConstants.Key.ACTION, action)
							.setHeader(JsonConstants.Key.CHECK_REQUEST, "blockedUser").copyHeaders(message.getHeaders())
							.build();
				}

				if (result.startsWith("S_")) {
					if (result.startsWith("F_")) {
						resultID = Long.parseLong(result.substring(2, result.length()));
						return MessageBuilder.withPayload(message.getPayload())
								.setHeader(JsonConstants.Key.ENTITY, entity).setHeader(JsonConstants.Key.TYPE, type)
								.setHeader(JsonConstants.Key.ACTION, action)
								.setHeader(JsonConstants.Key.CHECK_REQUEST, "invalid").copyHeaders(message.getHeaders())
								.build();
					}
					if (result.startsWith("S_")) {
						resultID = Long.parseLong(result.substring(2, result.length()));
						log.info("Result Id in Simple Request Receiver :: " + resultID);
					}
					systemUserClone.setUserTableId(resultID);

					boolean Status = true;
					
					if (type.equalsIgnoreCase("otp_login")) 
					{
						String loginType = Constants.EMPTY_STRING;
						reSendOtp= requestData.getString(JsonConstants.RequestData.RESENDOTP) == null ? "" : requestData.getString(JsonConstants.RequestData.RESENDOTP);
						if(jsonUser.has("loginType"))
						{
							loginType = jsonUser.getString("loginType") == null ? "" : jsonUser.getString("loginType");
						}
						log.info("reSendOtp ::: " + reSendOtp);
						if((reSendOtp!=null && reSendOtp.equalsIgnoreCase("Yes")) 
								&& (!loginType.equalsIgnoreCase(Constants.EMPTY_STRING) 
										&& loginType.equalsIgnoreCase("Normal")))
						{
							Status = systemUserService.checkLockedStatusWithoutUpdate(systemUser, Status);
						}
						else
						{
							Status= true;
						}
					}
					/*
					 * else if(type.equalsIgnoreCase("verifyOtp_login")) { Status =
					 * systemUserAuthenticationService.checkLockedStatus(systemUserClone, false);
					 * String otpAttempts = systemUserService.otpAttempts(systemUser.getUsername());
					 * log.info("user otpAttempts before ::"+ otpAttempts);
					 * systemUser.setAndroidOtpAttempt(Integer.valueOf(otpAttempts) + 1);
					 * log.info("user setAndroidOtpAttempt after ::"+
					 * systemUser.getAndroidOtpAttempt()); boolean flag =
					 * systemUserService.updateInvalidOTPAttempts(systemUser);
					 * 
					 * log.info("flag ::"+ flag); }
					 */

					/*
					 * if (systemUserAuthenticationService.checkLockedStatus(systemUserClone, true))
					 */
					/*if(userId != null && userId > 0)
					{*/
					
					if (systemUserService.checkLockedStatusWithoutUpdate(systemUser, Status)) {
						log.info("User Locked Status Chaked Successfully");
						JSONObject versionJSON = JSONPayloadExtractor.extractJSONObject(receivedMessage,
								(JsonConstants.VERSION));

						Version version = versionService.extractVersion(versionJSON);
						log.info("EXtracted Version :: " + version);

						version = versionService.checkVersionDatabase(version, false, false);
						log.info("Version Status :: " + version.getStatus());

						if (!(version.getStatus()).equals("CHANGE")) {
							log.info("No need to change version");
							MessageHeaders messageHeader = message.getHeaders();

							String digestMatchStatus = messageHeader.get("digestMatchStatus") != null
									? (String) messageHeader.get("digestMatchStatus")
									: "Yes";

							log.info("digestMatchStatus :: " + digestMatchStatus);

							if (digestMatchStatus.equalsIgnoreCase("Yes"))
							// if(true)
							{
								log.info("Digest Matched Successwfully");
								String userName = (String) userMap.get(Constants.USERNAME);

								if (systemUserAuthenticationService.isFutureDateExpired(userName)) {
									log.info(" -------- Future Date not reset before fixed interval -------- ");
									return MessageBuilder.withPayload(message.getPayload())
											.setHeader(JsonConstants.Key.ENTITY, entity)
											.setHeader(JsonConstants.Key.TYPE, type)
											.setHeader(JsonConstants.Key.ACTION, action)
											.setHeader(JsonConstants.Key.CHECK_REQUEST, "futureExpiry")
											.copyHeaders(message.getHeaders()).build();
								}

								if (!systemUserAuthenticationService.isResetBeforeFixedInterval(userName)) {
									log.info(" -------- password not reset before fixed interval -------- ");
									return MessageBuilder.withPayload(message.getPayload())
											.setHeader(JsonConstants.Key.ENTITY, entity)
											.setHeader(JsonConstants.Key.TYPE, type)
											.setHeader(JsonConstants.Key.ACTION, action)
											.setHeader(JsonConstants.Key.CHECK_REQUEST, "autoResetPassword")
											.copyHeaders(message.getHeaders()).build();
								} else {
									log.info("Type :: " + type);
									if (type.equalsIgnoreCase("login")) {
										HashMap responseMap = null;
										responseMap = systemUserAuthenticationService
												.createSuccessResponseData(systemUserNew);
										return systemUserAuthenticationService.generateResponse(message, responseMap);
									} else {
										log.info("Not a login type");
										return MessageBuilder.withPayload(message.getPayload())
												.setHeader(JsonConstants.Key.ENTITY, entity)
												.setHeader(JsonConstants.Key.TYPE, type)
												.setHeader(JsonConstants.Key.ACTION, action)
												.setHeader(JsonConstants.Key.CHECK_REQUEST, "valid")
												.copyHeaders(message.getHeaders()).build();
									}
									/*
									 * return MessageBuilder.withPayload(message.getPayload())
									 * .setHeader(JsonConstants.Key.ENTITY, entity)
									 * .setHeader(JsonConstants.Key.TYPE, type) .setHeader(JsonConstants.Key.ACTION,
									 * action) .setHeader(JsonConstants.Key.CHECK_REQUEST, "valid")
									 * .copyHeaders(message.getHeaders()).build();
									 */

								}
							} else {
								Map<String, Object> errorMap = new HashMap<String, Object>();

								errorMap.put("status", "failure");
								errorMap.put("message", "Unauthorized Request");
								errorMap.put("data", "");

								ByteArrayOutputStream baos = new ByteArrayOutputStream();
								new ObjectMapper().writeValue(baos, errorMap);

								MessageBuilder.withPayload(baos.toString())
										.setHeader("response", message.getHeaders().get("response")).build();
							}
						} else {
							return MessageBuilder.withPayload(message.getPayload())
									.setHeader(JsonConstants.Key.ENTITY, entity).setHeader(JsonConstants.Key.TYPE, type)
									.setHeader(JsonConstants.Key.ACTION, action)
									.setHeader(JsonConstants.Key.VERSION_NUMBER, version.getVersionOnServer())
									.setHeader(JsonConstants.Key.APPLICATION_DOWNLOAD_LINK, version.getURL())
									.setHeader(JsonConstants.Key.CHECK_REQUEST, "versionupgrade")
									.copyHeaders(message.getHeaders()).build();
						}
					} else {
						log.info(" -------- Account is Locked -------- ");
						return MessageBuilder.withPayload(message.getPayload())
								.setHeader(JsonConstants.Key.ENTITY, entity).setHeader(JsonConstants.Key.TYPE, type)
								.setHeader(JsonConstants.Key.ACTION, action)
								.setHeader(JsonConstants.Key.CHECK_REQUEST, "accountLocked")
								.copyHeaders(message.getHeaders()).build();
					}
					
				} else if (result.startsWith("F_")) {
					if (result.startsWith("F_")) {
						resultID = Long.parseLong(result.substring(2, result.length()));
					}
					systemUserClone.setUserTableId(resultID);

					if (systemUserAuthenticationService.checkLockedStatus(systemUserClone, false)) {
						return MessageBuilder.withPayload(message.getPayload())
								.setHeader(JsonConstants.Key.ENTITY, entity).setHeader(JsonConstants.Key.TYPE, type)
								.setHeader(JsonConstants.Key.ACTION, action)
								.setHeader(JsonConstants.Key.CHECK_REQUEST, "invalid").copyHeaders(message.getHeaders())
								.build();
					} else {
						log.info(" -------- Account is Locked -------- ");
						return MessageBuilder.withPayload(message.getPayload())
								.setHeader(JsonConstants.Key.ENTITY, entity).setHeader(JsonConstants.Key.TYPE, type)
								.setHeader(JsonConstants.Key.ACTION, action)
								.setHeader(JsonConstants.Key.CHECK_REQUEST, "accountLocked")
								.copyHeaders(message.getHeaders()).build();
					}
				} else {
					log.info(" -------- Account is Locked -------- ");
					return MessageBuilder.withPayload(message.getPayload()).setHeader(JsonConstants.Key.ENTITY, entity)
							.setHeader(JsonConstants.Key.TYPE, type).setHeader(JsonConstants.Key.ACTION, action)
							.setHeader(JsonConstants.Key.CHECK_REQUEST, "invalid").copyHeaders(message.getHeaders())
							.build();
				}
			}

		} catch (JsonParseException e) {
			log.error("--------Exception In CheckRequestService / checkRequest()------ ", e);
		} catch (JsonMappingException e) {
			log.error("--------Exception In CheckRequestService / checkRequest()------ ", e);
		} catch (IOException e) {
			log.error("--------Exception In CheckRequestService / checkRequest()------ ", e);
		} catch (Exception e2) {
			log.error("--------Exception In CheckRequestService / checkRequest()------ ", e2);
		}

		return MessageBuilder.withPayload(message.getPayload())
				.setHeader(JsonConstants.Key.CHECK_REQUEST, "unauthorizedrequest").copyHeaders(message.getHeaders())
				.build();
	}
	
	public Message<String> ChangeHandsetResponse(Message<String> message,String massage)
	{
		log.info("-------------- in createChangeHandsetResponse() of systemuser Authentication service");
		HashMap dataMap = new HashMap();
		JSONObject responseJson = new JSONObject();
		try
		{
			responseJson.put(JsonConstants.MESSAGE, massage);
			responseJson.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
			responseJson.put(JsonConstants.DATA, dataMap);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return MessageBuilder.withPayload(responseJson.toString()).copyHeaders(message.getHeaders()).build();
	}
	
	public Message<String> ChangeHandsetFailureResponse(Message<String> message,String massage)
	{
		log.info("-------------- in createChangeHandsetResponse() of systemuser Authentication service");
		HashMap dataMap = new HashMap();
		JSONObject responseJson = new JSONObject();
		try
		{
			responseJson.put(JsonConstants.MESSAGE, massage);
			responseJson.put(JsonConstants.STATUS, JsonConstants.FAILURE);
			responseJson.put(JsonConstants.DATA, dataMap);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return MessageBuilder.withPayload(responseJson.toString()).copyHeaders(message.getHeaders()).build();
	}
	
	public Message<String> failureResponse(Message<String> message,String responseMessage)
	{
		log.info("-------------- in failureResponse() of systemuser Authentication service");
		HashMap dataMap = new HashMap();
		JSONObject responseJson = new JSONObject();
		try
		{
			responseJson.put(JsonConstants.MESSAGE, responseMessage);
			responseJson.put(JsonConstants.STATUS, JsonConstants.FAILURE);
			responseJson.put(JsonConstants.DATA, dataMap);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return MessageBuilder.withPayload(responseJson.toString()).copyHeaders(message.getHeaders()).build();
	}
}
