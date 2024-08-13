package com.mobicule.mcollections.integration.collection;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.client.RestTemplate;

import com.mobicule.component.activitylogger.commons.ActivityLoggerConstants;
import com.mobicule.component.activitylogger.service.CommunicationActivityService;
import com.mobicule.component.activitylogger.service.UserActivityService;
import com.mobicule.component.activitylogger.threads.CommunicationActivityAddition;
import com.mobicule.component.mapconversion.xml.MapToXML;
import com.mobicule.mcollections.core.beans.Configuration;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.beans.resendsms.SMSAPIReq;
import com.mobicule.mcollections.core.beans.resendsms.SmsHeader;
import com.mobicule.mcollections.core.beans.resendsms.SmsMessage;
import com.mobicule.mcollections.core.beans.resendsms.SmsMessages;
import com.mobicule.mcollections.core.beans.resendsms.SmsReq;
import com.mobicule.mcollections.core.beans.resendsms.Smsdestination;
import com.mobicule.mcollections.core.beans.resendsms.Smsdestinations;
import com.mobicule.mcollections.core.commons.AES;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.MailSenderUtility;
import com.mobicule.mcollections.core.commons.SMSTemplateXMLUtilities;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.ConfigurationService;
import com.mobicule.mcollections.core.service.SystemUserService;
import com.mobicule.mcollections.core.thread.Login2FaEmailThread;
import com.mobicule.mcollections.core.thread.Login2FaSMSThread;
import com.mobicule.mcollections.integration.commons.ServerUtilities;
import com.mobicule.mcollections.webservice.adapter.KotakCollectionWebserviceAdapter;

public class OtpGeneration implements IOtpGeneration {
	private static Logger log = LoggerFactory.getLogger(OtpGeneration.class);

	@Autowired
	private CommunicationActivityService communicationActivityService;
	
	@Autowired
	private SystemUserService systemUserService;
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private UserActivityService userActivityService;

	@Autowired
	ApplicationConfiguration<String, String> applicationConfiguration;
	
	public ApplicationConfiguration<String, String> getApplicationConfiguration() {
		return applicationConfiguration;
	}


	public void setApplicationConfiguration(ApplicationConfiguration<String, String> applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
	}
	
	@Autowired
	private ConfigurationService configurationService;
	
	@Autowired
	private SMSTemplateXMLUtilities smsTemplateXMLUtilities;
	@Autowired
	private MailSenderUtility mailSenderUtility; 
	
	Properties properties = Utilities.appConfigProperties();

	@Override
	public Message<String> execute(Message<String> message) throws Throwable {
		log.info("Inside OTP Generation");

		JSONObject responseJSON = new JSONObject();
		try {
			log.info("------------- in userRequestForOTP() in OtpGeneration");
			String draMobileNumber = Constants.EMPTY_STRING;
			String userName =Constants.EMPTY_STRING;
			String requestSet = message.getPayload();

			String requestEntity = JSONPayloadExtractor.extract(requestSet,
					JsonConstants.ENTITY);
			String requestAction = JSONPayloadExtractor.extract(requestSet,
					JsonConstants.ACTION);
			String requestType = JSONPayloadExtractor.extract(requestSet,
					JsonConstants.TYPE);

			JSONObject requestData = JSONPayloadExtractor.extractJSON(requestSet,
					JsonConstants.DATA);
			
			String reSendOtp= Constants.EMPTY_STRING;

			JSONObject jsonObject = new JSONObject(requestSet);
			JSONObject user = (JSONObject) jsonObject
					.get(JsonConstants.SYSTEM_USER);
			
			log.info("user---------"
					+ user);
			
			if (user.has(Constants.USERNAME)) {
				userName = user.getString(Constants.USERNAME) == null ? Constants.EMPTY_STRING
						: user.getString(Constants.USERNAME);
			}
					
			log.info("-------------in OtpGeneration:  user requested for otp---------"
					+ userName);
			
			List<Configuration> configurationList = new ArrayList<Configuration>();
	        Map<String, String> parametersMap = new HashMap<String, String>();
	        
	        parametersMap.put("CONFIGURATION_CODE", "OTPT");
	        
			configurationList = configurationService.getConfigurations(parametersMap, "CONFIGURATION");
			
			long configValue = 0L;
			
			if(configurationList != null && !configurationList.isEmpty())
			{
			
				List<Configuration> updatedConfigurationList = new ArrayList<Configuration>();
				updatedConfigurationList.add(configurationList.get(configurationList.size()-1));
				
				Configuration config = updatedConfigurationList.get(0);
				
				configValue = Long.valueOf(config.getConfigurationValue());
			}
				

			if (userName != null && userName != Constants.EMPTY_STRING) 
			{
				SystemUser systemUser = systemUserService
						.getUserDetailsForOTP(userName);
				log.info("Enc OTP " + systemUser.getOTP());
				
				
				if(requestType.equalsIgnoreCase("otp_login"))//otp for 2fa login CR
				{
					reSendOtp = requestData.getString(JsonConstants.RequestData.RESENDOTP) == null ? "" : requestData.getString(JsonConstants.RequestData.RESENDOTP);
					SystemUser sysUser = new SystemUser();
					/*
					 * boolean isAccountLocked = false; boolean authStatus = true; String reSendOtp=
					 * requestData.getString(JsonConstants.RequestData.RESENDOTP) == null ? "" :
					 * requestData.getString(JsonConstants.RequestData.RESENDOTP);
					 * log.info("reSendOtp::::"+reSendOtp);
					 */
					
					if (null != systemUser) 
					{
						log.info("systemUser.getOtpRequestDate() ----> " + systemUser.getOtpRequestDate());
						
						long otpDifference = getOTPDifference(String.valueOf(systemUser.getOtpRequestDate()));
						log.info("otpDifference---->"+otpDifference);
						
						log.info("------------- system user is not null ---------");
						
						log.info("systemUser.getAndroidOtpAttempt() -----> " + systemUser.getAndroidOtpAttempt() );
						
						if ((systemUser.getAndroidOtpAttempt()) >= (Constants.AUTHENTICATION_LIMIT) && otpDifference >= 10) {
							
							systemUser.setAndroidOtpAttempt(0);
							boolean updateStatus = systemUserService.updateInvalidOTPAttempts(systemUser);
							log.info("boolean update 0 status ----> " + updateStatus);
							
							systemUser.setResendOtpAttempts(0);
							boolean updateResendOtpStatus =  systemUserService.updateResendOTPAttempts(systemUser);
							log.info("boolean update resend 0 status ----> " + updateResendOtpStatus);
						}
						
						//Current timestamp for appsec---->
						Date currentTimestamp = new Date(System.currentTimeMillis());
						log.info("date_time---->"+currentTimestamp);
						//SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
						//String date = df.format(currentTimestamp);
						//log.info("date_time---->"+date);
						String time_stamp =AES.encrypt(System.currentTimeMillis()+"");
						log.info("time_stamp---->"+time_stamp);

						log.info("---user type----" + systemUser.getLoginType());
						
						
						draMobileNumber = systemUser
								.getMobileNumber();

						log.info("------------- supervisorMobileNumber = "
								+ draMobileNumber + "---------");
						
						
						/*
						 * if(reSendOtp.equalsIgnoreCase("Yes")) {
						 * sysUser.setAndroidOtpAttempt((sysUser.getAndroidOtpAttempt()) + 1);
						 * 
						 * log.info("user setAndroidOtpAttempt ::"+ sysUser.getAndroidOtpAttempt());
						 * boolean flag = systemUserService.updateInvalidOTPAttempts(sysUser);
						 * 
						 * log.info("flag ::"+ flag); isAccountLocked = false;
						 * log.info("isAccountLocked:: " + isAccountLocked); } else
						 * if(reSendOtp.equalsIgnoreCase("No")) { isAccountLocked =
						 * isAccountLocked(sysUser, authStatus); //log.info("UserId:: " + userId);
						 * log.info("isAccountLocked:: " + isAccountLocked); }
						 */
						 
						
						if (Constants.EMPTY_STRING != draMobileNumber
								&& null != draMobileNumber)
						{
						
							log.info("----------inside if after checking  draMobileNumber null or empty-------------");
							log.info("----------calilng generateOTPForDevice() -------------");
						
							String genratedOTP = generate2FaOTPForDevice();

							log.info("----------genrated otp ="
									+ genratedOTP + "-------------");

							systemUser.setOTP(genratedOTP);
							
							Map optSMSMap = ServerUtilities.generateSMSForAndroidOTP(genratedOTP,systemUser);

							log.info("----------genrated sms ="+ optSMSMap.toString()+ "-------------");
							  
							
							// new login otp update
							String dateOtpAttempt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
							log.info("timeStamp ---> " + dateOtpAttempt);

							String newOtpRequestDate = systemUser.getloginOtpAttemptDate() == null ? dateOtpAttempt.toString()
									: systemUser.getloginOtpAttemptDate().toString();
							log.info("NewOtpRequestDate:::::" + newOtpRequestDate);
							
							DateTime newCurrentTimestamp = new DateTime(System.currentTimeMillis());
							SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

							Date newDate = dateFormat.parse(newOtpRequestDate.toString());
							log.info("NewDate" + newDate);
							DateTime newOtpDate = new DateTime(newDate.getTime());

							Duration durationOtp = new Duration(newOtpDate, newCurrentTimestamp);
							log.info("new diff in minutes " + durationOtp.getStandardMinutes());

							long differenceOtp = durationOtp.getStandardMinutes();
							log.info("differenceOtp " + differenceOtp);

							/***/
							 

							//if (sendPdfSms(genratedOTP,systemUser,configValue)) 
							if(systemUser.getMobileNumber()==null||systemUser.getMobileNumber().equalsIgnoreCase(""))
							{
								 log.info("---------otp sms sent failed -------------");
								  
								  responseJSON = createFailureResponseData("OTP sms sending failed. Mobile Number Not Mapped");
							}
							else if(systemUser.getEmailAddress()==null||systemUser.getEmailAddress().equalsIgnoreCase(""))
							{
								log.info("---------otp email sent failed -------------");
								  
								  responseJSON = createFailureResponseData("OTP email sending failed. Email-Id Not Mapped");
							}
							else
							{
								if (systemUser.getResendOtpAttempts() >= 3) {
									if (otpDifference <= 10) {
										log.info("Your OTP limit has been exceeded, Please try after 10 mins");
										responseJSON = createFailureResponseData(
												"Your OTP limit has been exceeded, Please try after 10 mins");
									} else {
										log.info(" ::: Starting  Email Thread ::: ");
										Login2FaEmailThread login2FaEmailThread = new Login2FaEmailThread(systemUser,genratedOTP, systemUserService, mailSenderUtility);
										Thread two2FaEmailThread = new Thread(login2FaEmailThread);
										two2FaEmailThread.setName("2FaLoginOTPEmailThread : " + systemUser.getUsername());
										two2FaEmailThread.start();

										log.info(" ::: Starting  Sms Thread ::: ");
										String idAndSecrect =  applicationConfiguration.getValue(Constants.TwoFaSmsDetails.TWO_FA_SMS_CLIENT_ID)+":"+ applicationConfiguration.getValue(Constants.TwoFaSmsDetails.TWO_FA_SMS_CLIENT_SECRECT);
										log.info(" ::: Starting  Sms Thread ::: ");
										Login2FaSMSThread login2FaSMSThread = new Login2FaSMSThread(systemUser,genratedOTP.toUpperCase(), systemUserService,applicationConfiguration,restTemplate);
										Thread two2FaSmsThread = new Thread(login2FaSMSThread);
										two2FaSmsThread.setName("2FaLoginOTPSmsThread : " + systemUser.getUsername());
										two2FaSmsThread.start();

										systemUser.setOTP(AES.encrypt(genratedOTP));

										if (reSendOtp.equalsIgnoreCase("No")
												&& requestType.equalsIgnoreCase("otp_login")) {
											if (systemUserService.updateTwoFAOTP(systemUser)) {

												log.info("---------otp updated successfully in db-------------");

												responseJSON = createSuccessOtpGenerateResponseData(systemUser,
														genratedOTP.substring(0, 1), time_stamp);
											} else {
												log.info("---------otp update in db failed -------------");

												responseJSON = createFailureResponseData("otp not saved in db ");
											}
										} else if (reSendOtp.equalsIgnoreCase("Yes")) {
											sysUser.setResendOtpAttempts((systemUser.getResendOtpAttempts()) + 1);
											sysUser.setUsername(systemUser.getUsername());
											log.info("user setResendOtpAttempts ::" + sysUser);
											
											/*if() */{
												
												boolean flag = systemUserService.updateResendOTPAttempts(sysUser);
												log.info("flag:::::::::" + flag);

												systemUser.setDescription("reSendOtp");
												if (systemUserService.updateTwoFAOTP(systemUser)) {

													log.info("---------otp updated successfully in db-------------");

													responseJSON = createSuccessOtpGenerateResponseData(systemUser,
															genratedOTP.substring(0, 1), time_stamp);
												} else {
													log.info("---------otp update in db failed -------------");

													responseJSON = createFailureResponseData("otp not saved in db ");
												}
											}
											/*
											 * else { responseJSON =
											 * createFailureResponseData("You entered invalid OTP 3 times and please try to login after 10 min."
											 * ); }
											 */
											
										}

									}
								} else {

									log.info(" ::: Starting  Email Thread ::: ");
									Login2FaEmailThread login2FaEmailThread = new Login2FaEmailThread(systemUser,genratedOTP, systemUserService, mailSenderUtility);
									Thread two2FaEmailThread = new Thread(login2FaEmailThread);
									two2FaEmailThread.setName("2FaLoginOTPEmailThread : " + systemUser.getUsername());
									two2FaEmailThread.start();

									log.info(" ::: Starting  Sms Thread ::: ");
									Login2FaSMSThread login2FaSMSThread = new Login2FaSMSThread(systemUser,genratedOTP.toUpperCase(), systemUserService,applicationConfiguration,restTemplate);
									Thread two2FaSmsThread = new Thread(login2FaSMSThread);
									two2FaSmsThread.setName("2FaLoginOTPSmsThread : " + systemUser.getUsername());
									two2FaSmsThread.start();

									systemUser.setOTP(AES.encrypt(genratedOTP));

									if (reSendOtp.equalsIgnoreCase("No") && requestType.equalsIgnoreCase("otp_login")) {
										if (systemUserService.updateTwoFAOTP(systemUser)) {

											log.info("---------otp updated successfully in db-------------");

											responseJSON = createSuccessOtpGenerateResponseData(systemUser,
													genratedOTP.substring(0, 1), time_stamp);
										} else {
											log.info("---------otp update in db failed -------------");

											responseJSON = createFailureResponseData("otp not saved in db ");
										}
									} else if (reSendOtp.equalsIgnoreCase("Yes")) {
										sysUser.setResendOtpAttempts((systemUser.getResendOtpAttempts()) + 1);
										sysUser.setUsername(systemUser.getUsername());
										log.info("user setResendOtpAttempts ::" + sysUser); 
										boolean flag = systemUserService.updateResendOTPAttempts(sysUser);
										log.info("flag:::::::::" + flag);

										systemUser.setDescription("reSendOtp");
										if (systemUserService.updateTwoFAOTP(systemUser)) {

											log.info("---------otp updated successfully in db-------------");

											responseJSON = createSuccessOtpGenerateResponseData(systemUser,
													genratedOTP.substring(0, 1), time_stamp);
										} else {
											log.info("---------otp update in db failed -------------");

											responseJSON = createFailureResponseData("otp not saved in db ");
										}
									}
								}
							}
							
							
							
							 
							
							
						}
						else
						{
							log.info("------------- draMobileNumber is null---------");
							responseJSON = createFailureResponseData(" dra mobile number for username not available");
						}
						
					}
					else {
						log.info("--------- system user for requested username is null-------------");
						responseJSON = createFailureResponseData(" no user present for requested username");
					}
				}
				else
				{
					if(systemUser.getOTP() != null && systemUser.getOTP() != Constants.EMPTY_STRING)
					{
						if(systemUser.getOTP().length() > 5)
						{
							systemUser.setOTP(AES.decrypt(systemUser.getOTP()));
						}
						log.info("Dec OTP inside iF" + systemUser.getOTP());
					}
					log.info("Dec OTP outside if" + systemUser.getOTP());
					
					if (null != systemUser) {
						log.info("------------- system user is not null ---------");

						log.info("---user type----" + systemUser.getLoginType());

							if (!isSameDateOfOTPRequest(userName,configValue,systemUser.getOTP())) {
								log.info("------------- request is not on same date---------");

								draMobileNumber = systemUser
										.getMobileNumber();

								log.info("------------- supervisorMobileNumber = "
										+ draMobileNumber + "---------");
								if (Constants.EMPTY_STRING != draMobileNumber
										&& null != draMobileNumber) {
									log.info("----------inside if after checking  draMobileNumber null or empty-------------");
									log.info("----------calilng generateOTPForDevice() -------------");

									String genratedOTP = generateOTPForDevice();

									log.info("----------genrated otp ="
											+ genratedOTP + "-------------");

									systemUser.setOTP(genratedOTP);

									Map optSMSMap = ServerUtilities
											.generateSMSForAndroidOTP(genratedOTP,
													systemUser);

									log.info("----------genrated sms ="
											+ optSMSMap.toString()
											+ "-------------");

									if (sendPdfSms(genratedOTP,systemUser,configValue)) {
									
										log.info("---------otp sms sent successful -------------");
										
										if (systemUserService.updateAndroidOTP(systemUser)) {

											log.info("---------otp updated successfully in db-------------");

											responseJSON = createSuccessResponseData(systemUser);
										} else {
											log.info("---------otp update in db failed -------------");

											responseJSON = createFailureResponseData("otp not saved in db ");
										}

									} else {
										log.info("---------otp sms sent failed -------------");

										responseJSON = createFailureResponseData("otp sms sending failed");
									}
								} else {
									log.info("------------- draMobileNumber is null---------");
									responseJSON = createFailureResponseData(" dra mobile number for username not available");
								}

							} else {
								log.info("------------- in side else of date chk---------");
								log.info("------------- request is on same date---------");

								draMobileNumber = systemUser
										.getMobileNumber();

								if (null != draMobileNumber
										&& Constants.EMPTY_STRING != draMobileNumber) {
									String oldOTP = systemUserService
											.getOTP(userName);
									
									if(oldOTP != null && oldOTP != Constants.EMPTY_STRING)
									{
										if(oldOTP.length() > 5)
										{
											oldOTP = AES.decrypt(oldOTP);
										}
										log.info("Dec OTP inside iF" + oldOTP);
									}
									
									log.info("Dec OTP ouside iF" + oldOTP);
									
									
									if (null != oldOTP
											&& Constants.EMPTY_STRING != oldOTP) {
										Map optSMSMap = ServerUtilities
												.generateSMSForAndroidOTP(oldOTP,
														systemUser);
										
										log.info("----------genrated sms ="
												+ optSMSMap.toString()
												+ "-------------");
										systemUser.setOTP(oldOTP);
										if (sendPdfSms(oldOTP,systemUser,configValue)) {
										
											log.info("---------otp sms sent successful -------------");

										
											responseJSON = createSuccessResponseData(systemUser);
										} else {
											log.info("---------otp sms sent failed -------------");

											responseJSON = createFailureResponseData("otp sms sending failed");
										}
									} else {
										log.info("---------otp retrived form db is null or empty -------------");
										responseJSON = createFailureResponseData("otp is null or empty for username");
									}
								} else {
									log.info("------------- supervisorMobileNumber is null---------");
									responseJSON = createFailureResponseData(" supervisors mobile number for username not available");
								}

							}

					}

					else {
						log.info("--------- system user for requested username is null-------------");
						responseJSON = createFailureResponseData(" no user present for requested username");
					}
				}
				

			} else {
				log.info("--------- username in request is null -------------");
				responseJSON = createFailureResponseData(" please enter valid username");
			}

		} catch (Exception e) {
			log.info("Exception :: " +e);
			e.printStackTrace();
		}

		return MessageBuilder.withPayload(String.valueOf(responseJSON))
				.copyHeaders(message.getHeaders()).build();

	}
	
	/*
	 * public boolean isAccountLocked(SystemUser validatedUser, boolean authStatus)
	 * { boolean accountLockedStatus = true;
	 * 
	 * accountLockedStatus = systemUserService.checkLockedStatus(validatedUser,
	 * authStatus);
	 * 
	 * if (accountLockedStatus) {
	 * log.info("----------Account is not Locked--------"); return false;
	 * 
	 * } else { log.info("----------Account Locked !!!--------"); return true;
	 * 
	 * } }
	 */

	private boolean isSameDateOfOTPRequest(String userName,long configValue,String otp) throws ParseException {
		log.info("------------- in isSameDateOfOTPRequest() in password service");
		DateTime currentTimestamp = new DateTime(System.currentTimeMillis());
		
		log.info("currentTimestamp  " + currentTimestamp);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");

		SystemUser checkOTP = systemUserService.getOTPDateRequested(userName);
		String otpRequestDate = checkOTP.getLastLoggedIn();
		log.info("otpRequestDate  " + otpRequestDate);
		
		if ((checkOTP.getAndroidOtpAttempt()) >= (Constants.AUTHENTICATION_LIMIT))
		{
			log.info("----- getAndroidOtpAttempt exceeds limit -----");
			return false;
		}

		if (otpRequestDate == null || otpRequestDate == Constants.EMPTY_STRING) {
			return false;
		} else {
			
			java.util.Date date = dateFormat.parse(otpRequestDate);
			
			log.info("date  " + date);
			
			DateTime otpDate = new DateTime(date.getTime());
			
			log.info("otpDate  " + otpDate);

			Duration duration = new Duration(otpDate, currentTimestamp);
			log.info("diff in minutes" + duration.getStandardMinutes());
			
			log.info("configValue" + configValue);
			
			long difference = duration.getStandardMinutes();

			if (difference <= configValue) 
			{
				
				if(otp != null || otp != Constants.EMPTY_STRING)
				{
					return true;
				}
			}
			
		}
		return false;
	}

	private Date getCurrentSystemDate() {
		log.info("------------- in getCurrentSystemDate() in password service");
		Date currentDate = new Date(
				new Timestamp(System.currentTimeMillis()).getTime());
		log.info("------------- currentSystemDate = " + currentDate
				+ "-------------");
		return currentDate;
	}

	private String generateOTPForDevice() {
		log.info("----------in genrated otp() in password service-------------");

		String date = Utilities.generateDate("yyyy-MM-dd");

		Timestamp t = Utilities.generateTimestamp("yyyy-MM-dd", date);

		long l = t.getTime() / 10000000;

		String str = String.valueOf(Math.floor(l * Math.random()));

		String newExitPassword = str.substring(0, str.indexOf("."));

		if (newExitPassword.length() != 4) {
			newExitPassword = newExitPassword.substring(0, 4);
		} else if (newExitPassword.length() < 5) {
			newExitPassword = newExitPassword;
		}
		
		return newExitPassword;
	}
	
	private String generate2FaOTPForDevice() {
		log.info("----------in genrated 2fa otp() -------------");

		String date = Utilities.generateDate("yyyy-MM-dd");

		Timestamp t = Utilities.generateTimestamp("yyyy-MM-dd", date);

		long l = t.getTime() / 10000000;

		String str = String.valueOf(Math.floor(l * Math.random()));

		String newExitPassword = str.substring(0, str.indexOf("."));

		if (newExitPassword.length() != 4) {
			newExitPassword = newExitPassword.substring(0, 4);
		} else if (newExitPassword.length() < 5) {
			newExitPassword = newExitPassword;
		}
		
		char firstLetter = (char) ((int)(Math.random()*100)%26+65);
		
		newExitPassword = firstLetter + newExitPassword;
		
		return newExitPassword;
	}

	private JSONObject createSuccessResponseData(SystemUser systemUser) {
		log.info("--------- in createSuccessResponseData() of password service-------------");
		JSONObject map = new JSONObject();

		JSONObject successResponseJson = new JSONObject();

		try 
		{
			map.put("loginType", systemUser.getLoginType());

			if (systemUser.getLoginType().equalsIgnoreCase("Ldap")) 
			{
				map.put("otp", "");
			} 
			else
			{
				 map.put("otp", "");	
			}

			successResponseJson.put("status", JsonConstants.SUCCESS);
			successResponseJson.put("message", JsonConstants.SUCCESS);
			successResponseJson.put("data", map);

		}

		catch (Exception e) {

			log.error("---Exception Details ----", e);

		}
		return successResponseJson;
	}
	
	private JSONObject createSuccessOtpGenerateResponseData(SystemUser systemUser,String otp,String time_stamp)
	{
		log.info("--------- in createSuccessOtpGenerateResponseData() -------------");
		JSONObject map = new JSONObject();

		JSONObject successResponseJson = new JSONObject();

		try 
		{
			map.put("loginType", systemUser.getLoginType());

			/*
			 * if (systemUser.getLoginType().equalsIgnoreCase("Ldap")) {
			 */
				map.put("otp", otp);
				map.put("timestamp",time_stamp);
			/*} 
			else
			{
				 map.put("otp", "");	
			}*/

			successResponseJson.put("status", JsonConstants.SUCCESS);
			successResponseJson.put("message", "OTP Generated Successfully");
			successResponseJson.put("data", map);

		}

		catch (Exception e) {

			log.error("---Exception Details ----", e);

		}
		return successResponseJson;
	}

	@SuppressWarnings("unchecked")
	private JSONObject createFailureResponseData(String failureMessage) {
		log.info("--------- in createFailureResponseData() of password service-------------");
		HashMap data = new HashMap();
		
		JSONObject failureResponseJson = new JSONObject();
		try {
			failureResponseJson.put("status", JsonConstants.FAILURE);
			failureResponseJson.put("message", failureMessage);
			failureResponseJson.put("data", data);
		} catch (JSONException e) {
		
			log.info("Exception :: " +e);
		}
		return failureResponseJson;
	}

	private boolean sendOTPSMS(Map optSMSMap) {
		log.info("---------- in side sendOTPSMS() in password service -------------");

		try {
			KotakCollectionWebserviceAdapter kotakCollectionWebserviceAdapter = new KotakCollectionWebserviceAdapter();
			
			String serverURL = (String) applicationConfiguration
					.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");

			log.info("---- Before call to SMS Web Service---------");
			
			StringBuilder xmlRequest = MapToXML.convertMapToXML(optSMSMap, true, new HashMap<String, String>());
			
			CommunicationActivityAddition communicationActivityAddition = new CommunicationActivityAddition("1", "", (""),
					serverURL, xmlRequest.toString(), communicationActivityService,
					ActivityLoggerConstants.DATABASE_MSSQL);

			new Thread(communicationActivityAddition).run();

			log.info("----- Result of SMS Dispatch : -------" + "result");
			
		} catch (Exception e) {
			
			log.info("Exception :: " +e);
			return false;
		}

		return true;
	}
	
	public boolean sendPdfSms(String genratedOTP,SystemUser systemUser,long configValue) {

		log.info("systemUser::::: " + systemUser);

		boolean isSmsSend = false;
		//UAT
		/*List<String> mobileNo = new ArrayList<String>();
		mobileNo.add("7304597783");//BSG
		mobileNo.add("8180948575");//Puja
		mobileNo.add("9987362539");//BA
		
		
		for(String number : mobileNo)
		{
		systemUser.setMobileNumber(number);
		
		log.info("MobileNumber::: " + systemUser.getMobileNumber());
		if (systemUser.getMobileNumber() != null
				&& !systemUser.getMobileNumber().equalsIgnoreCase(
						Constants.EMPTY_STRING)) {
			log.info("Sending Sms to customer mobile number");

			 isSmsSend = generateSmsForUserContactNo( communicationActivityService,
					genratedOTP,systemUser,configValue);
		
		}
	}*/
		//Prod
		log.info("MobileNumber::: " + systemUser.getMobileNumber());
		if (systemUser.getMobileNumber() != null
				&& !systemUser.getMobileNumber().equalsIgnoreCase(
						Constants.EMPTY_STRING)) {
			log.info("Sending Sms to customer mobile number");

			 isSmsSend = generateSmsForUserContactNo( communicationActivityService,
					genratedOTP,systemUser,configValue);
		}
		return isSmsSend;
	}
	@SuppressWarnings("unused")
	private boolean generateSmsForUserContactNo(CommunicationActivityService communicationActivityService,
			String genratedOTP,SystemUser user,long configValue) {
		
		log.info("systemUser::::: " + user);
		
		String webserviceUrl = Constants.EMPTY_STRING;
		
		String smsDispatcherMap = Constants.EMPTY_STRING;
		
		String xmlResponse = Constants.EMPTY_STRING;
	
		try
		{

		/*webserviceUrl = (String) applicationConfiguration
				.getValue("RESEND_SMS_WEBSERVICE");

		log.info("webserviceUrl----" + webserviceUrl);*/
			
		Map<String,Object> smsReqData = new HashMap<String, Object>();
			
		smsDispatcherMap = generateSmsVerbiageForUserContactNo(genratedOTP,user,configValue);
			
		log.info("---msg in callSMSDispatcher ---" + smsDispatcherMap);

		String time = Constants.EMPTY_STRING;
		
		if(configValue < 10)
		{
			time = "0"+configValue + ":00";
		}
		else
		{
			time = ""+configValue + ":00";
		}
		
		user.setCode(time);
		String contentTemplateId = "MCOLL_43";
			
		smsReqData.put(Constants.SMSTemplateConstants.COMMUNICATION_STATUS, Constants.NO);
		smsReqData.put(Constants.SMSTemplateConstants.CONTENT_TEMPLATE_ID, contentTemplateId);
		smsReqData.put(Constants.SMSTemplateConstants.SMSTEXT, smsDispatcherMap);
		smsReqData.put(Constants.SMSTemplateConstants.USER, user);
			
		log.info("smsReqData::: "+smsReqData);
			
		boolean status = smsTemplateXMLUtilities.getRequestStatus(smsReqData, user.getMobileNumber());
			
		log.info("status::: "+status);
		
		//Old
		 /*smsDispatcherMap = generateSmsVerbiageForUserContactNo(genratedOTP,user,configValue);

		log.info("---- Inside smsDispatcherMap --------" + smsDispatcherMap);

		KotakCollectionWebserviceAdapter kotakCollectionWebserviceAdapter = new KotakCollectionWebserviceAdapter();

		xmlResponse = kotakCollectionWebserviceAdapter
				.callWebserviceAndGetXmlString(smsDispatcherMap,
						webserviceUrl);
		
		
		log.info("xmlResponse :::  " + xmlResponse);

		Map<String, Object> result = null;

		if (null != xmlResponse && !xmlResponse.equals(Constants.EMPTY_STRING))
		{

			if (xmlResponse.contains("<StatusCode>1</StatusCode>"))
			{
				return true;
				
			}
			else if (xmlResponse.contains("<StatusCode>00</StatusCode>"))
			{
				return true;
			
			
			}
			else
			{
				return false;
				
			}
		}
		else
		{
			return false;
			
		}*/
		return status;
		}
		catch(Exception e)
		{
			log.info("-----Exception Occur in sending SMS : -------");
			log.error("Exception :::" + e);
			return false;
		
			
		}
	}
	
	public static String generateSmsVerbiageForUserContactNo(String genratedOTP,SystemUser user,long configValue) {
		System.out.println("systemUser::::: " + user);
		StringBuilder smsText = new StringBuilder();
		SMSAPIReq smsApiReq = new SMSAPIReq();
		SmsHeader smsHeader = new SmsHeader();
		SmsReq smsReq = new SmsReq();
		SmsMessages smsMessages = new SmsMessages();
		SmsMessage smsMessage = new SmsMessage();
		Smsdestinations smsDestinations = new Smsdestinations();
		Smsdestination smsDestination = new Smsdestination();
		String xml = Constants.EMPTY_STRING;
		
		
		String time = Constants.EMPTY_STRING;
	
		
		if(configValue < 10)
		{
			time = "0"+configValue;
		}
		else
		{
			time = ""+configValue;
		}

		try {

			smsText.append("Dear " + user.getFirstName()+", \n");
			smsText.append(user.getOTP() + " is the One Time Password (OTP) to complete Authentication on your Device \n");
			smsText.append("for Android 10 & above. Please note OTP will be valid for \n");
			smsText.append(time + ":00 minutes only. Kindly ");
			smsText.append("do not share this with anyone. \n\n");
			smsText.append("Thanks & Regards, \n");
			smsText.append("Collection Team- HO");
			
			smsHeader.setSrcAppCd(Constants.MCOLL);
			smsHeader.setRequestID(String.valueOf(System.currentTimeMillis()));
			smsReq.setMsgReqID(String.valueOf(System.currentTimeMillis()));
			smsReq.setTimestamp(String.valueOf(System.currentTimeMillis()));
			smsReq.setPriority(Constants.RESEND_SMS_PRIORITY);
			smsReq.setType(Constants.RESEND_SMS_TYPE);
			smsReq.setUrlShortening(Constants.RESEND_SMS_URLSHORTENING);
			smsReq.setTrack(Constants.RESEND_SMS_TRACK);
			smsMessage.setFrom(Constants.RESEND_SMS_MESSAGE_FROM);
			smsDestination.setTo(user.getMobileNumber()); 
			smsDestination.setMessageID(String.valueOf(System
					.currentTimeMillis()));
			smsApiReq.setSmsHeader(smsHeader);
			smsMessages.setSmsMessage(smsMessage);
			smsReq.setMessages(smsMessages);
			smsMessage.setText(smsText.toString());
			smsDestinations.setSmsdestination(smsDestination);
			smsMessage.setSmsdestinations(smsDestinations);
			smsApiReq.setSmsReq(smsReq);

			xml = Utilities.marshal(smsApiReq);
			
			System.out.println("XML :: " +xml);
		
		} catch (Exception e) {
			
			log.info("Exception :: " +e);
		}

		return smsText.toString();

	}
	
	private long getOTPDifference(String loginOtpDate)
	{
		log.info("----------getOTPDifference-------------");
		long diff = 0;
		try {
			Date currentTimestamp1 = new Date(System.currentTimeMillis());
			//DateTime currentTimestamp = new DateTime(currentTimestamp1);
			
			log.info("currentTimestamp  " + currentTimestamp1);
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			java.util.Date date = dateFormat.parse(loginOtpDate);
			
			log.info("date  " + date);
			
			DateTime otpDate = new DateTime(date.getTime());
			DateTime currentTimestamp = new DateTime(currentTimestamp1.getTime());
			
			log.info("otpDate  " + otpDate);

			Duration duration = new Duration(otpDate,currentTimestamp);
			log.info("diff in minutes" + duration.getStandardMinutes());
			 diff = duration.getStandardMinutes();
			
		}catch (Exception e) {
			log.info("Exception ::" + e);
			return 0;
		}
		return diff;
		
	
	}

}