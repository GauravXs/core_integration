package com.mobicule.mcollections.integration.collection;


import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.mobicule.component.activitylogger.service.UserActivityService;
import com.mobicule.mcollections.core.beans.Configuration;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.AES;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.MailSenderUtility;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.ConfigurationService;
import com.mobicule.mcollections.core.service.StreetwalkService;
import com.mobicule.mcollections.core.service.SystemUserService;
import com.mobicule.mcollections.core.thread.ImeiRegistrationEmail;

public class OtpVerification implements IOtpGeneration
{
	private Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private SystemUserService systemUserService;

	@Autowired
	private UserActivityService userActivityService;

	@Autowired
	ApplicationConfiguration applicationConfiguration;
	
	@Autowired
	private ConfigurationService configurationService;
	
	@Autowired
	private MailSenderUtility mailSenderUtility; 
	
	@Autowired
	private StreetwalkService streetwalkService;
	
	@Override
	public Message<String> execute(Message<String> message) throws Throwable {
		JSONObject responseJSON = new JSONObject();
		log.info("---------- in side OtpVerification()  -------------");
		
		String userName = Constants.EMPTY_STRING;
		String otp = Constants.EMPTY_STRING;
		String osVersion = Constants.EMPTY_STRING;
		
		String imei = Constants.EMPTY_STRING;
		
		
		String requestSet = message.getPayload();
		String requestEntity = JSONPayloadExtractor.extract(requestSet,
				JsonConstants.ENTITY);
		String requestAction = JSONPayloadExtractor.extract(requestSet,
				JsonConstants.ACTION);
		String requestType = JSONPayloadExtractor.extract(requestSet,
				JsonConstants.TYPE);

		JSONObject requestData = JSONPayloadExtractor.extractJSON(requestSet,
				JsonConstants.DATA);

		JSONObject jsonObject = new JSONObject(requestSet);
		JSONObject userJson = (JSONObject) jsonObject
				.get(JsonConstants.SYSTEM_USER);
		
		if (userJson.has(Constants.USERNAME)) {
			userName = userJson.getString(Constants.USERNAME) == null ? Constants.EMPTY_STRING
					: userJson.getString(Constants.USERNAME);
		}
		if (requestData.has(JsonConstants.OTP)) {
			otp = requestData.getString(JsonConstants.OTP) == null ? Constants.EMPTY_STRING
					: requestData.getString(JsonConstants.OTP);
			
			log.info("Request Data OTP ::: " + otp);
		}
		if (userJson.has("osVersion")) {
			osVersion = userJson.getString("osVersion") == null ? Constants.EMPTY_STRING
					: userJson.getString("osVersion");
		}
		
		if (userJson.has("imei")) {
			imei = userJson.getString("imei") == null ? Constants.EMPTY_STRING
					: userJson.getString("imei");
		}
		
		try
		{
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
			SystemUser userBean = new SystemUser();
			userBean.setUsername(userName);
			
			if(otp != null && otp != Constants.EMPTY_STRING)
			{
				if(otp.length() > 5)
				{
					otp = AES.decrypt(otp);
					log.info("Dec OTP inside iF" + otp);
				}
				
			}
			log.info("otp ::: " +otp);
			SystemUser sysUser = systemUserService.getUser(userBean);
			SystemUser sysUserLockTable = systemUserService.extractUser(userBean);
			
			SystemUser user = new SystemUser();
			user.setUsername(userName);
			user.setOTP(otp);
			user.setAndroidVersion(osVersion);
			user.setModifiedBy(sysUser.getId());
			user.setImeiNo(imei);
			user.setFirstName(sysUser.getFirstName());
			user.setLastName(sysUser.getLastName());
			user.setMobileNumber(sysUser.getMobileNumber());
			user.setAuthenticationCounter(sysUserLockTable.getAuthenticationCounter());
			log.info("user + " + user);
			
			if(requestType.equalsIgnoreCase("verifyOtp_login"))
			{
				
				String otpValidation = isValid2FaOTP(user,sysUser,configValue);
				log.info("------ otpValidation-------------"+otpValidation);
				sysUser.setUserTableId(sysUser.getId());
				SystemUser getUserDetails = systemUserService.getUser(sysUser.getUsername());
				/*
				 * if(systemUserService.checkLockedStatus(sysUser, false)) { responseJSON =
				 * createFailureResponseData("Your Account has been Locked,Please contact Admin!"
				 * );
				 * 
				 * } else
				 */
				{
					if(otpValidation.equals("OTP Authentication Successful."))
					{
						log.info("------ isValidOTP()  successful-------------");
						SystemUser vlidOtpUser = systemUserService.getUser(sysUser);
						sysUser.setResendOtpAttempts(0);
						boolean flag = systemUserService.updateResendOTPAttempts(sysUser);
						log.info("flag:::::::::"+flag);
						vlidOtpUser.setUsername(sysUser.getUsername() == null ? Constants.EMPTY_STRING : sysUser.getUsername().toString());
						vlidOtpUser.setUserTableId(vlidOtpUser.getId() == null ? 0L : Long.valueOf(vlidOtpUser.getId()));
						sysUser.setAndroidOtpAttempt(0);
						log.info("user ::"+ sysUser);
						boolean updateFlag = systemUserService.updateInvalidOTPAttempts(sysUser);
						log.info("updateInvalidOTPAttempts flag ::"+ updateFlag);
						systemUserService.checkLockedStatus(sysUser, true);
						responseJSON = createSuccessResponseData(otpValidation);
						
					}
					else if(otpValidation.equals("You entered invalid OTP 3 times and please try to login after 10 min."))
					{
						
						log.info("----- authenticationCounter exceeds limit -----");
						responseJSON = createFailureResponseData("You entered invalid OTP 3 times and please try to login after 10 min.");
					}
					else if(otpValidation.equals("Your Account has been Locked,Please contact Admin!"))
					{
						
						log.info("----- authenticationCounter exceeds limit -----");
						sysUser.setAuthenticationCounter(Constants.AUTHENTICATION_LIMIT);
						responseJSON = createFailureResponseData("OTP Authentication Failed, Your Account has been Locked, Please contact Admin!");
					}
					else if(otpValidation.contains("OTP Expired"))
					{						
						log.info("----- OTP EXPIRED -----");
						responseJSON = createFailureResponseData("OTP Expired. Click On Resend OTP");
					}
					else
					{
						if ((sysUser.getAndroidOtpAttempt()) >= (Constants.AUTHENTICATION_LIMIT))
						{
							sysUser.setUserTableId(sysUser.getId());
							log.info("----- authenticationCounter exceeds limit -----");
							sysUser.setAuthenticationCounter(Constants.AUTHENTICATION_LIMIT);
							responseJSON = createFailureResponseData("You entered invalid OTP 3 times and please try to login after 10 min.");
						}
						else
						{
							SystemUser inavlidOtpUser = systemUserService.getUser(sysUser);
							inavlidOtpUser.setUsername(sysUser.getUsername() == null ? Constants.EMPTY_STRING : sysUser.getUsername().toString());
							inavlidOtpUser.setUserTableId(inavlidOtpUser.getId() == null ? 0L : Long.valueOf(inavlidOtpUser.getId()));
							
							String otpAttempts = systemUserService.otpAttempts(sysUser.getUsername());
							log.info("user otpAttempts before ::"+ otpAttempts);
							
							if(otpAttempts != null) {
								sysUser.setAndroidOtpAttempt(Integer.valueOf(otpAttempts) + 1);
							}else {
								sysUser.setAndroidOtpAttempt(1);
							}
							
							log.info("user setAndroidOtpAttempt after ::"+ sysUser.getAndroidOtpAttempt());
							
							boolean flag = systemUserService.updateInvalidOTPAttempts(sysUser);
							
							log.info("flag ::"+ flag);
							
							responseJSON = createFailureResponseData(otpValidation+(Constants.AUTHENTICATION_LIMIT - sysUser.getAndroidOtpAttempt())+" Attemps Left.");
						
							if ((sysUser.getAndroidOtpAttempt()) >= (Constants.AUTHENTICATION_LIMIT))
							{
								log.info("----- authenticationCounter exceeds limit -----");
								responseJSON = createFailureResponseData("You entered invalid OTP 3 times and please try to login after 10 min.");
							}
						}
					}
				}
				
			}
			else
			{
				if (isValidOTP(user,sysUser,configValue))
				{
					log.info("------ isValidOTP()  successful-------------");
					
					boolean updateImei = false;
					if (streetwalkService.isCasesAllocatedToUser(sysUser.getId()))
					{
						log.info(" in isCasesAllocatedToUser ::");
						SystemUser updateStreetwalk = new SystemUser();
						updateStreetwalk.setImeiNo(sysUser.getImeiNo());
						updateStreetwalk.setModifiedOn(new Timestamp(System.currentTimeMillis()));
						updateStreetwalk.setModifiedBy(sysUser.getId());
						updateStreetwalk.setUserTableId(sysUser.getId());
						updateStreetwalk.setAndroidVersion(osVersion);
						updateStreetwalk.setRegistrationStatus("Pending");
						log.info("updateStreetwalk " + updateStreetwalk);
						updateImei =systemUserService.updateIMEIOf10StreetwalkUser(imei, updateStreetwalk);
					}
					else
					{
						log.info(" No isCasesAllocatedToUser ::");
						updateImei = systemUserService.updateIMEIForAndroid10(user);
					}
					
					
					if(updateImei)
					{
						List<Map<String, Object>> listOfHoUsers = new ArrayList<Map<String,Object>>();
						Map<String, Object> listOfEmailId = new HashMap<String, Object>();
						
						
						Map<String, ArrayList<String>> userportfolioList = systemUserService.getusersPortfolioList(userName);

						log.info("systemUser.getName()" +userName);
						
						ArrayList<String> portfoliolist = userportfolioList.get(userName);
						
						log.info("portfoliolist" +portfoliolist);
						Set<String> toEmailIdList = new HashSet<String>();
						Set<String> ccEmailIdList = new HashSet<String>();
						
						
						String portfolio = Constants.EMPTY_STRING;
						if (portfoliolist != null && !portfoliolist.isEmpty())
						{

							for (String userportfolio : portfoliolist)
							{
								 List<String> toEmail = new ArrayList<String>();
								 toEmail = ((List<String>) applicationConfiguration
										.getValue(userportfolio.toUpperCase() +"_to"));
								 if(toEmail != null && !toEmail.isEmpty())
								 {
									 toEmailIdList.addAll(toEmail);
								 }

							}
						}
						else
						{

							toEmailIdList = new HashSet<String>();
						}
						if (portfoliolist != null && !portfoliolist.isEmpty())
						{

							for (String userportfolio : portfoliolist)
							{
								List<String> ccEmail = new ArrayList<String>();
								 ccEmail = ((List<String>) applicationConfiguration
										.getValue(userportfolio.toUpperCase() +"_cc"));
								 if(ccEmail != null && !ccEmail.isEmpty())
								 {
									 ccEmailIdList.addAll(ccEmail);
								 }

							}
						}
						else
						{

							ccEmailIdList = new HashSet<String>();
						}
						log.info("toEmailId" +toEmailIdList);
						log.info("ccEmailId" +ccEmailIdList);
						
						
						ImeiRegistrationEmail emailService = new ImeiRegistrationEmail(mailSenderUtility,toEmailIdList,ccEmailIdList,user);
						Thread emailToHo = new Thread(emailService);
						emailToHo.setName("ImeiRegistrationEmailHo");
						emailToHo.start();
						
						responseJSON = createSuccessResponseData("Imei updated Successfully");
					}
					else
					{
						responseJSON = createFailureResponseData("Failure in Imei updatedation");
					}
				}
				else
				{
					log.info("---------otp in request is not valid -------------");
					if ((sysUser.getAndroidOtpAttempt()) >= (Constants.AUTHENTICATION_LIMIT))
					{
						log.info("----- authenticationCounter exceeds limit -----");
						responseJSON = createFailureResponseData("Maximum OTP attempts have been reached. Kindly click on resend OTP to generate new OTP");
					}
					else
					{
					
						sysUser.setAndroidOtpAttempt((sysUser.getAndroidOtpAttempt()) + 1);
						
						log.info("user ::"+ sysUser);
						boolean flag = systemUserService.updateInvalidOTPAttempts(sysUser);
						log.info("flag ::"+ flag);
						responseJSON = createFailureResponseData("Please enter valid OTP !!");
					}
				}
			}
			
		}
		catch(Exception e)
		{
			log.info("Exception :: " +e);
		}
		return MessageBuilder.withPayload(String.valueOf(responseJSON))
				.copyHeaders(message.getHeaders()).build();
	}
	private boolean isValidOTP(SystemUser user,SystemUser systemUser,long configValue) throws ParseException
	{
		
		log.info("systemUser : "+systemUser);
		DateTime currentTimestamp = new DateTime(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		log.info("currentTimestamp" + currentTimestamp);
		
		String OtpRequestDate = systemUser.getOtpRequestDate() == null ? Constants.EMPTY_STRING : systemUser.getOtpRequestDate().toString();
		
		if ((systemUser.getAndroidOtpAttempt()) >= (Constants.AUTHENTICATION_LIMIT))
		{
			return false;
		}
		
		if(OtpRequestDate == null || OtpRequestDate == Constants.EMPTY_STRING)
		{
			return false;
		}
		else
		{
		
		Date date = dateFormat.parse(systemUser.getOtpRequestDate().toString());
		log.info("date" + date);
		DateTime otpDate = new DateTime(date.getTime());
		log.info("otpDate" + otpDate);
		
		Duration duration = new Duration(otpDate,currentTimestamp );
		log.info("diff in minutes" + duration.getStandardMinutes());
		log.info("configValue" + configValue);
		long difference = duration.getStandardMinutes();
		
		String otp = systemUser.getOTP();
		log.info("otp enc from DB :: " + otp);
		
		if(otp != null && otp != Constants.EMPTY_STRING)
		{
			if(otp.length() > 5)
			{
				try {
					otp = com.mobicule.mcollections.core.commons.AES.decrypt(otp);
				}catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			log.info("Dec OTP inside iF DB ::: " + systemUser.getOTP());
		}
		
		
		log.info("otp dec DB :::" + otp);
			if(otp != null || otp != Constants.EMPTY_STRING)
			{
				log.info("Inside if OTP :::" + otp);
				
				if(difference <= configValue)
				{
					log.info("Inside if Diff :::" + otp);
					log.info("user.getOTP() :::" + user.getOTP()  + ":::::: " + "otp::: " + otp);
					if (user.getOTP().equalsIgnoreCase(otp))
					{
						return true;
					}
				}
				
			}
			return false;
		}
		
	}
	
	private String isValid2FaOTP(SystemUser user,SystemUser systemUser,long configValue) throws ParseException
	{

		log.info("systemUser : " + systemUser);
		DateTime currentTimestamp = new DateTime(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		log.info("currentTimestamp" + currentTimestamp);

		String OtpRequestDate = systemUser.getOtpRequestDate() == null ? Constants.EMPTY_STRING
				: systemUser.getOtpRequestDate().toString();
		
		// new login otp update
		String dateOtpAttempt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		log.info("timeStamp ---> " + dateOtpAttempt);

		String newOtpRequestDate = systemUser.getloginOtpAttemptDate() == null ? dateOtpAttempt.toString()
				: systemUser.getloginOtpAttemptDate().toString();
		log.info("NewOtpRequestDate:::::" + newOtpRequestDate);

		Date newDate = dateFormat.parse(OtpRequestDate.toString());
		log.info("NewDate" + newDate);
		DateTime newOtpDate = new DateTime(newDate.getTime());

		Duration durationOtp = new Duration(newOtpDate, currentTimestamp);
		log.info("new diff in minutes " + durationOtp.getStandardMinutes());

		long differenceOtp = durationOtp.getStandardMinutes();
		log.info("differenceOtp " + differenceOtp);
		
		long disableOtpForUserTimeLimit = 10;

		/***/

		if ((systemUser.getAndroidOtpAttempt()) >= (Constants.AUTHENTICATION_LIMIT) && differenceOtp <= disableOtpForUserTimeLimit) {
			return "You entered invalid OTP 3 times and please try to login after 10 min.";
		}

		if (OtpRequestDate == null || OtpRequestDate.equalsIgnoreCase(Constants.EMPTY_STRING)) {
			return "OTP Authentication Failed, Request date not available.";
		} else {

			Date date = dateFormat.parse(systemUser.getOtpRequestDate().toString());
			log.info("date" + date);
			DateTime otpDate = new DateTime(date.getTime());
			log.info("otpDate" + otpDate);

			Duration duration = new Duration(otpDate, currentTimestamp);
			log.info("diff in minutes" + duration.getStandardMinutes());
			log.info("configValue" + configValue);

			long difference = duration.getStandardMinutes();
			log.info("difference" + difference);

			String otp = systemUser.getOTP();
			log.info("otp enc from DB :: " + otp);

			if (otp != null && otp != Constants.EMPTY_STRING) {
				if (otp.length() > 5) {
					try {
						otp = com.mobicule.mcollections.core.commons.AES.decrypt(otp);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				log.info("Dec OTP inside iF DB ::: " + systemUser.getOTP());
			}

			log.info("otp dec DB :::" + otp);
			if (otp != null || otp != Constants.EMPTY_STRING) {
				log.info("Inside if OTP :::" + otp);
				
				if ((systemUser.getAndroidOtpAttempt()) >= (Constants.AUTHENTICATION_LIMIT) && differenceOtp >= disableOtpForUserTimeLimit) {
					systemUser.setAndroidOtpAttempt(0);
					boolean updateStatus = systemUserService.updateInvalidOTPAttempts(systemUser);
					log.info("boolean update 0 status ----> " + updateStatus);
				}

				if (difference <= configValue) {
					log.info("Inside if Diff :::" + otp);
					log.info("user.getOTP() :::" + user.getOTP() + ":::::: " + "otp::: " + otp);
					if (user.getOTP().equalsIgnoreCase(otp)) {
						return "OTP Authentication Successful.";
					} else {
						return "OTP Authentication Failed.";
					}
				} 
				else if(differenceOtp <= disableOtpForUserTimeLimit) {
					return "You entered invalid OTP 3 times and please try to login after 10 min.";
				}
				else {
					return "OTP Expired. Click On Resend OTP";
				}

			}
			return "OTP Authentication Failed";
		}

	}
	
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
	
	private JSONObject createSuccessResponseData(String failureMessage) {
		log.info("--------- in createSuccessResponseData() of password service-------------");
		HashMap data = new HashMap();
		
		JSONObject failureResponseJson = new JSONObject();
		try {
			failureResponseJson.put("status", JsonConstants.SUCCESS);
			failureResponseJson.put("message", failureMessage);
			failureResponseJson.put("data", data);
		} catch (JSONException e) {
			log.info("Exception :: " +e);
		}
		return failureResponseJson;
	}

}