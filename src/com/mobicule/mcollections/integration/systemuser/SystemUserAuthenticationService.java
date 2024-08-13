package com.mobicule.mcollections.integration.systemuser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.support.MessageBuilder;

import com.mobicule.component.activitylogger.service.UserActivityService;
import com.mobicule.component.usermapping.bean.Authentication;
import com.mobicule.component.usermapping.bean.Territory;
import com.mobicule.component.usermapping.service.AuthenticationService;
import com.mobicule.component.version.commons.ComponentConstants;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.AES;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.DBColumnNameConstants;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.SystemUserService;

public class SystemUserAuthenticationService implements ISystemUserAuthenticationService
{
    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserActivityService userActivityService;



    private ApplicationConfiguration<String, Object> applicationConfigurationObject;


    public ApplicationConfiguration<String, Object> getApplicationConfigurationObject() {
        return applicationConfigurationObject;
    }

    public void setApplicationConfigurationObject(ApplicationConfiguration<String, Object> applicationConfigurationObject) {
        this.applicationConfigurationObject = applicationConfigurationObject;
    }

    private SystemUserService userService;

    private AuthenticationService authenticationService;

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public void setAuthenticationService(
            AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public SystemUserService getUserService() {
        return userService;
    }

    public void setUserService(SystemUserService userService) {
        this.userService = userService;
    }

    public ApplicationConfiguration<String, String> getApplicationConfiguration() {
        return applicationConfiguration;
    }

    public void setApplicationConfiguration(
            ApplicationConfiguration<String, String> applicationConfiguration) {
        this.applicationConfiguration = applicationConfiguration;
    }

    private ApplicationConfiguration<String, String> applicationConfiguration;

    private ObjectMapper objectMapper = new ObjectMapper();



    @Override
    public Message<String> execute(Message<String> message) throws Throwable {
        log.info("--------------In SystemUserAuthenticationService() / execute ----------------");

        HashMap responseMap = null;

        try
        {
            String userDetails = JSONPayloadExtractor.extract(message.getPayload(), JsonConstants.SYSTEM_USER);
            return generateResponse(message, responseMap);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String authenticateUser(Map userMap, SystemUser systemUser) {
        log.info("--------------In SystemUserAuthenticationService() / authenticateUser ----------------");

        String imei = "";
        String password = "";
        String username = "";
        String loginType = "";
        String receiveTime = "";    //Authentication Bypass High
        long receiveTimeMilli = 0l; //Authentication Bypass High

        if (userMap.containsKey(JsonConstants.IMEI_NUMBER))
        {
            imei = (String) userMap.get(JsonConstants.IMEI_NUMBER);
        }
        if (userMap.containsKey(JsonConstants.PASSWORD))
        {
            password = (String) userMap.get(JsonConstants.PASSWORD);
            try {
                password = AES.decrypt(password);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                log.info("Exception :");
            }
        }
        if (userMap.containsKey(JsonConstants.USERNAME))
        {
            username = (String) userMap.get(JsonConstants.USERNAME);
        }
        if (userMap.containsKey(JsonConstants.LOGIN_TYPE))
        {
            loginType = (String) userMap.get(JsonConstants.LOGIN_TYPE);
        }
        if (imei == "" || password == "" || username == "")
        {
            return "F";
        }

        // Authentication Bypass High
        /*
         * if (userMap.containsKey("id")) { receiveTime = (String) userMap.get("id");
         * log.info("receiveTime 1 " + receiveTime); receiveTime =
         * AES.decrypt(receiveTime); log.info("receiveTime 2 " + receiveTime);
         * receiveTimeMilli = Long.valueOf(receiveTime); receiveTimeMilli =
         * receiveTimeMilli + 1; log.info("receiveTimeMilli 3 " + receiveTimeMilli);
         * receiveTime = String.valueOf(receiveTimeMilli); log.info("receiveTime 4 " +
         * receiveTime); receiveTime = AES.encrypt(receiveTime); }
         */

        systemUser.setDeleteFlag(Constants.DELETE_FLAG_FALSE);
        systemUser.setUsername(username);
        systemUser.setPassword(password);
        systemUser.setImeiNo(imei);
        systemUser.setLoginType(loginType);

        //Authentication Bypass High
        systemUser.setIdProofType(receiveTime);

        systemUser = userService.authenticateUser(systemUser, Constants.AUTHENTICATION_TYPE_SERVER);

        Long userId = 0L;
        if (systemUser != null)
        {
            //check for blocked user
            /*
             * if ((systemUser.getBlockedStatus()!=null &&
             * !systemUser.getBlockedStatus().equalsIgnoreCase(Constants.EMPTY_STRING)) &&
             * systemUser.getBlockedStatus().equalsIgnoreCase("BLOCKED"))
             */
            if ((systemUser.getImeiNo()!=null
                    && !systemUser.getImeiNo().equalsIgnoreCase(Constants.EMPTY_STRING))
                    && !systemUser.getImeiNo().equalsIgnoreCase(imei))
            {
                return "F";
            }
            if ((systemUser.isImeiExist()))
            {
                return "IM_" + systemUser.getUserTableId();
            }
            if ((systemUser.getBlockedStatus()!=null && !systemUser.getBlockedStatus().equalsIgnoreCase(Constants.EMPTY_STRING))
                    && systemUser.getBlockedStatus().equalsIgnoreCase("false"))
            {
                return "B_" + systemUser.getUserTableId();
            }

            if (systemUser.getLoginType() != null && systemUser.getLoginType().equalsIgnoreCase("Normal"))
            {
                Authentication authentication = new Authentication();
                authentication.setLogin(systemUser.getUsername() == null ? "" : systemUser.getUsername());
                authentication.setPassword(systemUser.getPassword());
                authentication.setId(systemUser.getUserTableId());

                //userId = authenticationService.validateMember(authentication);
                userId = userService.validateMember(authentication);
                log.info("userId :::" + userId);
                if(userId == null || userId == 0)
                {
                    systemUser.setPasswordValidate(true);
                    return "PF_" + systemUser.getUserTableId();
                }
                log.info("--------userId after validation : -----" + userId);
            }
            else if (systemUser.getLoginType() != null && systemUser.getLoginType().equalsIgnoreCase("Ldap"))
            {
                /*
                 * String ldapUrl = applicationConfiguration.getValue("LDAP_URL"); String
                 * prefixUsername = applicationConfiguration.getValue("PREFIX_USERNAME");
                 *
                 * log.info("--------ldapUrl : -----" + ldapUrl);
                 * log.info("--------systemUser.getUsername() : -----" +
                 * systemUser.getUsername());
                 * log.info("--------systemUser.getPassword() : -----" +
                 * systemUser.getPassword()); log.info("--------prefixUsername : -----" +
                 * prefixUsername);
                 *
                 * boolean ldapLoginStatus = Utilities.checkLdapLogin(ldapUrl,
                 * systemUser.getUsername(), systemUser.getPassword(), prefixUsername);
                 *
                 * log.info("--------ldapLoginStatus : -----" + ldapLoginStatus);
                 * log.info("--------ldapLoginStatus : -----" + systemUser.getUserTableId());
                 */

                if(userMap.containsKey("isAdfs") && userMap.get("isAdfs").equals("Yes"))
                {
                    userId = systemUser.getUserTableId();
                }else {
                    userId = 0l;
                }


                log.info("--------ldapLoginStatus : -----" + systemUser.getUserTableId());
            }
            if (userId != null && userId > 0)
            {
                return "S_" + systemUser.getUserTableId();
            }
            else
            {
                return "F_" + systemUser.getUserTableId();
            }
        }
        else
        {
            return "F";
        }
    }

    public Message<String> createLoginFailure(Message<String> message)
            throws IOException, JsonGenerationException, JsonMappingException {
        HashMap responseMap = null;
        responseMap = createErrorResponseData();
        return generateResponse(message, responseMap);
    }

    public Message<String> createUnauthorizedrequest(Message<String> message)
            throws IOException, JsonGenerationException, JsonMappingException {
        HashMap responseMap = null;
        responseMap = createUnauthorizedResponseData();
        return generateResponse(message, responseMap);
    }

    public Message<String> createVersionUpgradeRequest(Message<String> message)
            throws IOException, JsonGenerationException, JsonMappingException {
        HashMap responseMap = null;
        responseMap = createVersionUpgradeResponseData(message);
        return generateResponse(message, responseMap);
    }

    public Message<String> createLoginSuccess(Message<String> message) throws IOException, JsonGenerationException,
            JsonMappingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException
    {
        HashMap responseMap = null;
        /* String osVersion = Constants.EMPTY_STRING; */
        String userDetails = JSONPayloadExtractor.extract(message.getPayload(), JsonConstants.SYSTEM_USER);
        String imei = JSONPayloadExtractor.extract(userDetails, JsonConstants.IMEI_NUMBER);
        String password = JSONPayloadExtractor.extract(userDetails, JsonConstants.PASSWORD);
        String username = JSONPayloadExtractor.extract(userDetails, JsonConstants.USERNAME);
        /*
         * String isAdfs = Constants.EMPTY_STRING; try { isAdfs =
         * JSONPayloadExtractor.extract(userDetails, "isAdfs"); }catch (Exception e) {
         * log.info("Eception ::: " + e); e.printStackTrace(); }
         */
        /*
         * if(userDetails.contains("osVersion")) { osVersion =
         * JSONPayloadExtractor.extract(userDetails, "osVersion"); }
         */
        SystemUser systemUser = new SystemUser();
        systemUser.setDeleteFlag(Constants.DELETE_FLAG_FALSE);
        systemUser.setUsername(username);
        systemUser.setPassword(password);
        systemUser.setImeiNo(imei);

        /* systemUser.setAndroidVersion(osVersion); */
        log.info("User set as ::::" + systemUser);
        systemUser = userService.authenticateUser(systemUser, Constants.AUTHENTICATION_TYPE_SERVER);
        log.info("--- System User after auth : " + systemUser);
        Long userId;
        /*
         * if((isAdfs!=null && !isAdfs.equalsIgnoreCase(Constants.EMPTY_STRING)) &&
         * isAdfs.equalsIgnoreCase("Yes")) { log.info("--- Inside isAdfs : " ); String
         * newPassword = Utilities.generatePassword();
         * systemUser.setPassword(newPassword); log.info("--- Inside isAdfs 1: " +
         * systemUser.getPassword()); int count =
         * userService.updateLdapLoginPassword(systemUser);
         *
         * log.info("count :: " + count); userId = 0L;
         *
         * AES.encrypt(systemUser.getPassword());
         *
         * log.info("--- Inside isAdfs 2: " + systemUser.getPassword());
         *
         * }
         */
        if (systemUser != null)
        {
            Authentication authentication = new Authentication();
            authentication.setLogin(systemUser.getUsername() == null ? "" : systemUser.getUsername());
            authentication.setPassword(AES.decrypt(systemUser.getPassword()));
            authentication.setId(systemUser.getUserTableId());
            //userId = authenticationService.validateMember(authentication);
            userId = userService.validateMember(authentication);
            log.info("--------userId after validation : -----" + userId);
        }
        else
        {
            userId = 0L;
        }
        if (userId != null && userId > 0)
        {
            responseMap = createSuccessResponseData(systemUser);
        }
        else
        {
            responseMap = createErrorResponseData();
        }
        return generateResponse(message, responseMap);
    }

    public Message<String> generateResponse(Message<String> message, HashMap responseMap) throws IOException,
            JsonGenerationException, JsonMappingException
    {
        String responseString = "";
        //responseString = createLoginResponse(responseMap);
        //log.info("response string :::" + responseString);

        //to update last_logged_in date start
        if (responseMap != null && responseMap.get(JsonConstants.STATUS).toString().equalsIgnoreCase("success"))
        {

            try
            {
                HashMap dataMap = (HashMap) responseMap.get(JsonConstants.DATA);
                String userTableId = dataMap.get(JsonConstants.SYSTEM_USER_ID).toString();
                log.info("User Table Id ::: " + userTableId);
                String lastLoggedInDate = dataMap.get(JsonConstants.LAST_LOGGED_IN).toString();
                log.info("lastLoggedInDate ::: " + lastLoggedInDate);
                String loginType = dataMap.get(JsonConstants.LOGIN_TYPE).toString();
                //String osVersion = dataMap.get("osVersion") == null ? "" : dataMap.get("osVersion").toString();
                String firstLogin = "";
                //Capture device details
                String version = "";
                String appVersion = "";
                version = JSONPayloadExtractor.extract(message.getPayload(), JsonConstants.VERSION);
                appVersion = JSONPayloadExtractor.extract(version, "ver");


                String deviceDetails = "";
                deviceDetails = JSONPayloadExtractor.extract(message.getPayload(),JsonConstants.DEVICE_DETAILS);
                String osVersion = "";
                osVersion = JSONPayloadExtractor.extract(deviceDetails,"osVersion");




                //if (true)
                if (!userService.isFirstTimeLogin(Long.parseLong(userTableId))
                        || loginType.equalsIgnoreCase("ldap"))
                {
                    SystemUser loggedInUser = new SystemUser();

                    loggedInUser.setUserTableId(Long.parseLong(userTableId));
                    loggedInUser.setLastLoggedIn(new Timestamp(System.currentTimeMillis()).toString());
                    loggedInUser.setAndroidVersion(osVersion);
                    log.info("last login");
                    boolean flag = userService.updateLastLoggedIn(loggedInUser);

                    // inserting in login_logout_details
                    Map<String, Object> parameterValue = new HashMap<>();

                    parameterValue.put("osVersion", osVersion == null ? "" : osVersion);
                    parameterValue.put("appVersion", appVersion == null ? "" :appVersion);


                    long insertId = userService.insertLoginLogout(parameterValue,loggedInUser);
                    log.info("Login logout insertion id :: "+insertId);

                    if(insertId > 0L) {
                        dataMap.put("sessionId", String.valueOf(insertId));
                    }
                }
                else
                {
                    firstLogin = "Yes";
                }

                dataMap.put(JsonConstants.FIRST_LOGIN, firstLogin);
                responseMap.put(JsonConstants.DATA, dataMap);

                responseString = createLoginResponse(responseMap);

            }
            catch (Exception e)
            {
                log.error("--- Exception In SystemUserAuthenticationService ::: " + e);
                responseString = createLoginResponse(responseMap);
            }
        }
        else
        {
            responseString = createLoginResponse(responseMap);
        }
        // to update last_logged_in date End

        return MessageBuilder.withPayload(responseString).copyHeaders(message.getHeaders()).build();
    }


    /**
     * @author Shyam
     * Purpose: To set First Login = Y if user password has been expired and
     * user has not reset the same within given no of days i.e 30, 45 or 90 as per the configuration in app-config.xml file.
     * Config key is AUTO_PWD_RESET_INTERVAL
     */
    public Message<String> generateResponseForPasswordResetExpired(Message<String> message, HashMap responseMap) throws IOException, JsonGenerationException, JsonMappingException
    {
        log.info(" Inside generateResponseForPasswordResetExpired Method ");

        String responseString = "";

        if (responseMap != null && responseMap.get(JsonConstants.STATUS).toString().equalsIgnoreCase("success"))
        {
            try
            {
                HashMap dataMap = (HashMap) responseMap.get(JsonConstants.DATA);

                String userTableId = dataMap.get(JsonConstants.SYSTEM_USER_ID).toString();
                log.info("User Table Id ::: " + userTableId);

                String lastLoggedInDate = dataMap.get(JsonConstants.LAST_LOGGED_IN).toString();
                log.info("lastLoggedInDate ::: " + lastLoggedInDate);

                String loginType = dataMap.get(JsonConstants.LOGIN_TYPE).toString();
                log.info("---loginType---: "+loginType);

                dataMap.put(JsonConstants.FIRST_LOGIN, "Yes");
                responseMap.put(JsonConstants.DATA, dataMap);

                log.info("responseMap: " + responseMap);

                responseString = createLoginResponse(responseMap);

            }
            catch (Exception e)
            {
                log.error("--- Exception In SystemUserAuthenticationService -> generateResponseForPasswordResetExpired " + e);
                responseString = createLoginResponse(responseMap);
            }
        }
        else
        {
            responseString = createLoginResponse(responseMap);
        }

        return MessageBuilder.withPayload(responseString).copyHeaders(message.getHeaders()).build();
    }

    private String createLoginResponse(HashMap responseMap) throws IOException, JsonGenerationException, JsonMappingException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        objectMapper.writeValue(baos, responseMap);
        String responseString = baos.toString();

        return responseString;
    }

    @SuppressWarnings("unchecked")
    public HashMap createSuccessResponseData(SystemUser systemUser) {
        log.info("in createSuccessResponseData..");

        HashMap loginMap = new HashMap();
        HashMap dataMap = new HashMap();
        ArrayList<String> list = new ArrayList<String>();


        if((systemUser.getIsAdfs()!=null && !systemUser.getIsAdfs().equalsIgnoreCase(Constants.EMPTY_STRING))
                && systemUser.getIsAdfs().equalsIgnoreCase("Yes")) {
            log.info("--- Inside isAdfs : " );
            String newPassword = Utilities.generatePassword();
            systemUser.setPassword(newPassword);
            log.info("--- Inside isAdfs 1: " + systemUser.getPassword());
            int count = userService.updateLdapLoginPassword(systemUser);

            log.info("count :: " + count);

            String newDecPassword = AES.encrypt(systemUser.getPassword());

            log.info("--- Inside isAdfs 2: " + newDecPassword);

            dataMap.put("uuid", newDecPassword);

        }else {
            dataMap.put("uuid", "");
        }

        int totaldays = Integer.parseInt(applicationConfiguration.getValue("AUTO_PWD_RESET_INTERVAL"));

        try
        {
            Timestamp lastModifiedDate = userService.getLastModifiedPasswordDate(systemUser);

            int days = Utilities.calculateDays(lastModifiedDate);
            totaldays = totaldays - days;
        }
        catch (Exception e)
        {
            log.error("------Exception in createSuccessResponseData ------", e);

        }

        dataMap.put(JsonConstants.SYSTEM_USER_ID, (String.valueOf(systemUser.getUserTableId())));
        /*Commented 1.0 territory logic*/
		/*if (systemUser.getTerritoryList() != null && systemUser.getTerritoryList().size() > 0) {

			dataMap.put(JsonConstants.TERRITORY_ID, (String.valueOf(systemUser.getTerritoryList() != null
					&& !systemUser.getTerritoryList().isEmpty() ? systemUser.getTerritoryList().get(0).getId() : "")));

			for (Territory territory : systemUser.getTerritoryList())
			{

				list.add(territory.getCode());
			}

		} else {
			dataMap.put(JsonConstants.TERRITORY_ID, "");

		}
		dataMap.put(JsonConstants.TERRITORY_CODE, list);
		*/

        List<String> ccapacOnlineList =  (List<String>) applicationConfigurationObject.getValue("CCAPAC_PANINDIA");
        //DLLN
        List<String> ccapacDllnList =  (List<String>) applicationConfigurationObject.getValue("CCAPAC_DLLN");
        log.info("CCAPAC_ONLINE LIST :::::"+ccapacOnlineList);
        log.info("ccapacDllnList LIST :::::"+ccapacDllnList);
        dataMap.put("CCAPAC_ONLINE", ccapacOnlineList);
        dataMap.put("CCAPAC_DLLN", ccapacDllnList);



        /*
         * dataMap.put(JsonConstants.TERRITORY_NAME,
         * (String.valueOf(systemUser.getTerritoryList() != null &&
         * !systemUser.getTerritoryList().isEmpty() ?
         * systemUser.getTerritoryList().get(0).getName() : "")));
         */
        /*
         * dataMap.put(JsonConstants.TERRITORY_CODE,
         * (String.valueOf(systemUser.getTerritoryList() != null &&
         * !systemUser.getTerritoryList().isEmpty() ?
         * systemUser.getTerritoryList().get(0).getCode() : "")));
         */

        dataMap.put(JsonConstants.TERRITORY_CODE, list);

        dataMap.put(JsonConstants.USER_EMAIL_ADDRESS, (String
                .valueOf(systemUser.getEmailAddress() != null ? systemUser
                        .getEmailAddress() : "")));
        dataMap.put(JsonConstants.USER_MOBILE_NUMBER, (String
                .valueOf(systemUser.getMobileNumber() != null ? systemUser
                        .getMobileNumber() : "")));

        dataMap.put(JsonConstants.CC_APAC, (String.valueOf(systemUser
                .getCcapac() != null ? systemUser.getCcapac() : "")));
        dataMap.put(
                JsonConstants.COLLECTION_AMOUNT_LIMIT,
                (String.valueOf(systemUser.getCollectionAmountLimit() != null ? systemUser
                        .getCollectionAmountLimit() : "")));
        dataMap.put(
                JsonConstants.COLLECTION_COUNT_LIMIT,
                (String.valueOf(systemUser.getCollectionCountLimit() != null ? systemUser
                        .getCollectionCountLimit() : "")));

        dataMap.put(JsonConstants.AGENCY_ID,
                (String.valueOf(systemUser.getAgencyId())));

        dataMap.put(JsonConstants.BACKGROUND_ACTIVITY_DELAY, String
                .valueOf(applicationConfiguration
                        .getValue(Constants.BACKGROUND_ACTIVITY_DELAY)));
        dataMap.put(JsonConstants.AUTO_TRIGGER_ALARM_DELAY, String
                .valueOf(applicationConfiguration
                        .getValue(Constants.AUTO_TRIGGER_ALARM_DELAY)));
        dataMap.put("pwdResetDays", String.valueOf(totaldays));

        /*
         * dataMap.put(JsonConstants.RECEIPT_NUMBER_HIGHEST,
         * (userService.generateHighestReceiptNumber(systemUser)));
         */
        dataMap.put(JsonConstants.RECEIPT_NUMBER_HIGHEST, "");

        // Added last_logged_in date in datamap
        log.info("before putting in dataMap lastloggedIn date :: "
                + systemUser.getLastLoggedIn());
        dataMap.put(JsonConstants.LAST_LOGGED_IN, systemUser.getLastLoggedIn());
        // Added firstLogin in datamap
        dataMap.put(JsonConstants.FIRST_LOGIN, "");
        dataMap.put(JsonConstants.LOGIN_TYPE, systemUser.getLoginType());
        dataMap.put(JsonConstants.FIRST_LAST_NAME, systemUser.getFirstName()
                + " " + systemUser.getLastName());
        // Added
        dataMap.put(JsonConstants.SUPERVISOR, systemUser.getSupervisorName());
        dataMap.put(JsonConstants.FIRST_NAME, systemUser.getFirstName());
        dataMap.put(JsonConstants.LAST_NAME, systemUser.getLastName());
        String is2Fa = 	systemUser.getIs2FA();

        log.info("is2Fa status --> " + is2Fa);

        if(is2Fa != null && is2Fa.equalsIgnoreCase("YES"))
        {
            dataMap.put("is2FA", "Y");
        }
        else if(is2Fa != null && is2Fa.equalsIgnoreCase("NO"))
        {
            dataMap.put("is2FA", "N");
        }

        else {
            dataMap.put("is2FA", systemUser.getIs2FA());
        }

        String cocEnable = applicationConfiguration.getValue("IS_COC_ENABLE");

        log.info("IS_COC_ENABLE --> " + cocEnable);

        if(cocEnable != null && cocEnable.equalsIgnoreCase("true"))
        {
            dataMap.put("isCOCEnabled", true);
        }else {
            dataMap.put("isCOCEnabled", false);
        }

        dataMap.put(JsonConstants.CHANGE_HANDSET_FLAG,
                systemUser.getChangeHandsetFlag());

        dataMap.put(JsonConstants.AGENCY_NAME,systemUser.getAgencyName() == null ? "" : systemUser.getAgencyName());
        String pattern = "dd-MM-yyyy";
        SimpleDateFormat myFormat = new SimpleDateFormat("dd-MM-yyyy");
        String currentDateInString = new SimpleDateFormat(pattern)
                .format(new Date());
        long daysBetween = 10l;

        if (systemUser.getLoginType().equalsIgnoreCase("Normal")
                && null != systemUser.getExpiryDate()
                && Constants.EMPTY_STRING != systemUser.getExpiryDate()) {

            try {
                Date dateBefore = myFormat.parse(currentDateInString);
                Date dateAfter = myFormat.parse(systemUser.getExpiryDate());
                long difference = dateAfter.getTime() - dateBefore.getTime();
                daysBetween = (difference / (1000 * 60 * 60 * 24));
                log.info("Number of Days between dates: " + daysBetween);

            } catch (Exception e) {
                log.error("Exception ", e);
            }

        }
        //Authentication Bypass High
        if (systemUser.getIdProofType() != null && systemUser.getIdProofType() != Constants.EMPTY_STRING ) {
            dataMap.put("id", systemUser.getIdProofType());
        }

        dataMap.put("userExpiry", String.valueOf(daysBetween));
        /* dataMap.put("osVersion",systemUser.getAndroidVersion()); */

        loginMap.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
        loginMap.put(JsonConstants.MESSAGE,
                JsonConstants.MESSAGE_SUCCESSFULL_LOGIN);

        loginMap.put(JsonConstants.DATA, dataMap);
        log.info("returning from createSuccess......." + dataMap);
        return loginMap;
    }

    private HashMap createErrorResponseData() {
        log.info("in createErrorResponseData..");
        HashMap loginMap = new HashMap();
        HashMap dataMap = new HashMap();

        List<HashMap> dataList = new LinkedList<HashMap>();

        loginMap.put(JsonConstants.STATUS, JsonConstants.FAILURE);
        loginMap.put(JsonConstants.MESSAGE,
                JsonConstants.MESSAGE_FAILURE_AUTHENTICATION);
        loginMap.put(JsonConstants.DATA, dataList);

        loginMap.put(JsonConstants.DATA, dataMap);

        return loginMap;
    }

    private HashMap createUnauthorizedResponseData() {
        log.info("in createUnauthorizedResponseData..");
        HashMap loginMap = new HashMap();
        HashMap dataMap = new HashMap();

        List<HashMap> dataList = new LinkedList<HashMap>();

        loginMap.put(JsonConstants.STATUS, JsonConstants.FAILURE);
        loginMap.put(JsonConstants.MESSAGE, JsonConstants.AUTHORIZATION_FAILURE);
        loginMap.put(JsonConstants.DATA, dataList);

        loginMap.put(JsonConstants.DATA, dataMap);

        return loginMap;
    }

    private HashMap createVersionUpgradeResponseData(Message<String> message) {
        log.info("in createUnauthorizedResponseData..");
        HashMap loginMap = new HashMap();
        HashMap dataMap = new HashMap();

        MessageHeaders messageHdr = message.getHeaders();
        String downloadLink = (String) messageHdr
                .get(JsonConstants.Key.APPLICATION_DOWNLOAD_LINK);
        String versionNumber = (String) messageHdr
                .get(JsonConstants.Key.VERSION_NUMBER);

        dataMap.put("ver", versionNumber);
        dataMap.put("url", downloadLink);
        dataMap.put("type", "");

        loginMap.put(JsonConstants.STATUS, ComponentConstants.STATUS_CHANGE);
        loginMap.put(
                JsonConstants.MESSAGE,
                "There is a new Version of the Application available. Kindly click on OK to download the same.");
        loginMap.put(JsonConstants.DATA, dataMap);

        loginMap.put(JsonConstants.DATA, dataMap);

        return loginMap;
    }

    @Override
    public boolean isResetBeforeFixedInterval(String userName)
    {
        log.info("------------ in isResetBeforeFixedInterval() of systemuser Authentication service");

        int fixedInterval = Integer.parseInt(applicationConfiguration.getValue("AUTO_PWD_RESET_INTERVAL"));

        //Timestamp lastModifiedDate = userService.getLastModifiiedDate(userName);
        Timestamp lastModifiedDate = userService.getLastModifiedDateJmix(userName);

        log.info("---- Timestamp ---- " + lastModifiedDate);

        int numberOfDaysBeforePasswrodMomdified = Days.daysBetween(
                        new DateTime(lastModifiedDate),
                        new DateTime(new Timestamp(System.currentTimeMillis())))
                .getDays();

        if (fixedInterval <= numberOfDaysBeforePasswrodMomdified)
        {
            log.info("-------- password reset not done before fixed interval");
            return false;
        }

        log.info("-------- password reset done before fixed interval");

        return true;
    }

    public Message<String> createAutoPasswordResetRequest(Message<String> message)
    {
        log.info("-------------- in createAutoPasswordResetRequest() of systemuser Authentication service");

        HashMap dataMap = new HashMap();
        JSONObject responseJson = new JSONObject();

        try
        {
            responseJson.put(JsonConstants.MESSAGE, "Password Reset");
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

    public Message<String> createAccountLockedRequest(Message<String> message) {
        log.info("-------------- in createAccountLockedRequest() of systemuser Authentication service");
        HashMap dataMap = new HashMap();
        JSONObject responseJson = new JSONObject();
        try {
            responseJson
                    .put(JsonConstants.MESSAGE,
                            "Your Account is Locked. Kindly contact the Administrator.");
            responseJson.put(JsonConstants.STATUS, JsonConstants.FAILURE);
            responseJson.put(JsonConstants.DATA, dataMap);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return MessageBuilder.withPayload(responseJson.toString())
                .copyHeaders(message.getHeaders()).build();
    }

    @Override
    public boolean checkLockedStatus(SystemUser systemUser,
                                     boolean authentication) {
        return userService.checkLockedStatus(systemUser, authentication);

    }

    public Message<String> createBlockedUserResponse(Message<String> message) {
        log.info("-------------- in createBlockedUserResponse() of systemuser Authentication service");
        HashMap dataMap = new HashMap();
        JSONObject responseJson = new JSONObject();
        try {
            responseJson.put(JsonConstants.MESSAGE, Constants.BLOCKED_USER_ID);
            responseJson.put(JsonConstants.STATUS, JsonConstants.FAILURE);
            responseJson.put(JsonConstants.DATA, dataMap);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return MessageBuilder.withPayload(responseJson.toString())
                .copyHeaders(message.getHeaders()).build();
    }

    public Message<String> createFutureExpiryResponse(Message<String> message) {
        log.info("-------------- in createfitureExpiryResponse() of systemuser Authentication service");
        HashMap dataMap = new HashMap();
        JSONObject responseJson = new JSONObject();
        try {
            responseJson.put(JsonConstants.MESSAGE,
                    "Your account has been expired, please contact SMS Team");
            responseJson.put(JsonConstants.STATUS, JsonConstants.FAILURE);
            responseJson.put(JsonConstants.DATA, dataMap);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return MessageBuilder.withPayload(responseJson.toString())
                .copyHeaders(message.getHeaders()).build();
    }

    @Override
    public boolean isFutureDateExpired(String userName) {
        log.info("------------ in isFutureDateExpired() of systemuser Authentication service");
        boolean flag = false;

        List<Map<String, Object>> futureDateCase = new ArrayList<Map<String, Object>>();
        SimpleDateFormat myFormat = new SimpleDateFormat("dd-MMM-yyyy");
        String pattern = "dd-MMM-yyyy";
        String loginType = "";
        String dbFurureDate = "";
        String currentDateInString = new SimpleDateFormat(pattern)
                .format(new Date());

        log.info("currentDateInString " + currentDateInString);

        futureDateCase = userService.isFutureDateExpired(userName);
        log.info("futureDateCase" + futureDateCase);
        log.info("futureDateCase size" + futureDateCase.size());

        if (futureDateCase != null && futureDateCase.size() != 0) {
            log.info("test1");
            for (Map row : futureDateCase) {
                loginType = row.get("LOGIN_TYPE") == null ? "" : row.get(
                        "LOGIN_TYPE").toString();
                dbFurureDate = row.get("FUTURE_EXPIRY") == null ? "" : row.get(
                        "FUTURE_EXPIRY").toString();
            }
            log.info("dbFurureDate " + dbFurureDate);
            log.info("loginType " + loginType);

            if (loginType.equalsIgnoreCase("Normal") && null != dbFurureDate
                    && Constants.EMPTY_STRING != dbFurureDate) {
                log.info("test2");

                try {
                    if(dbFurureDate!=null && !dbFurureDate.equalsIgnoreCase(Constants.EMPTY_STRING)) {
                        Date dateBefore = myFormat.parse(dbFurureDate);
                        Date dateAfter = myFormat.parse(currentDateInString);
                        long difference = dateAfter.getTime()
                                - dateBefore.getTime();
                        float daysBetween = (difference / (1000 * 60 * 60 * 24));
                        log.info("Number of Days between dates: " + daysBetween);
                        if (daysBetween > 0) {
                            flag = true;
                        }
                    }
                } catch (Exception e) {
                    log.error("Exception ", e);
                }

            }
            /*
             * else if (loginType.equalsIgnoreCase("Normal")) {
             * log.info("in else if exipiry"); flag = true; }
             */
        }

        return flag;
    }

    // For Rbl

    @Override
    public String changeHandsetImei(Map<String, String> userMap) {
        log.info("--------------In SystemUserAuthenticationService() / changeHandsetImei ----------------");

        String imei = "";
        String password = "";
        String username = "";
        String userId = "";

        if (userMap.containsKey(JsonConstants.IMEI_NUMBER)) {
            imei = (String) userMap.get(JsonConstants.IMEI_NUMBER);
        }
        if (userMap.containsKey(JsonConstants.PASSWORD)) {
            password = (String) userMap.get(JsonConstants.PASSWORD);
            password = AES.decrypt(password);
        }
        if (userMap.containsKey(JsonConstants.USERNAME)) {
            username = (String) userMap.get(JsonConstants.USERNAME);
        }
        if (userMap.containsKey(JsonConstants.SYSTEM_USER_ID)) {
            userId = (String) userMap.get(JsonConstants.SYSTEM_USER_ID);
        }
        if (imei == "" || password == "" || username == "") {
            return "false";
        }

        SystemUser systemUser = new SystemUser();
        systemUser.setDeleteFlag(Constants.DELETE_FLAG_FALSE);
        systemUser.setUsername(username);
        systemUser.setPassword(password);
        systemUser.setImeiNo(imei);
        systemUser.setId(Long.valueOf(userId));

        systemUser = userService.authenticateUser(systemUser,
                Constants.AUTHENTICATION_TYPE_SERVER);

        if (systemUser != null) {
            Map<String, Object> updateFlagMap = new HashMap<String, Object>();

            systemUser.setImeiNo("");
            updateFlagMap = userService.updateAuthenticateUserImei(systemUser);

            log.info("changeHandsetImei :- "+updateFlagMap);

            String resultFlag = (String) (updateFlagMap.get("resultFlag") == null ? ""
                    : updateFlagMap.get("resultFlag"));

            return resultFlag;
        }

        return "false";
    }

    @Override
    public String authenticateUser(Map userMap, SystemUser systemUser,String AndroidTen)
    {

        log.info("--------------In SystemUserAuthenticationService() / authenticateUser ----------------");

        String imei = "";
        String password = "";
        String username = "";
        String receiveTime = "";    //Authentication Bypass High
        long receiveTimeMilli = 0l; //Authentication Bypass High

        if (userMap.containsKey(JsonConstants.IMEI_NUMBER))
        {
            imei = (String) userMap.get(JsonConstants.IMEI_NUMBER);
        }
        if (userMap.containsKey(JsonConstants.PASSWORD))
        {
            password = (String) userMap.get(JsonConstants.PASSWORD);
            try {
                password = AES.decrypt(password);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                log.info("Exception ::"+e);
            }
        }
        if (userMap.containsKey(JsonConstants.USERNAME))
        {
            username = (String) userMap.get(JsonConstants.USERNAME);
        }

        if(AndroidTen.equalsIgnoreCase("AndroidTen"))
        {
            if (password == "" || username == "")
            {
                return "F";
            }
        }
        else
        {
            if (imei == "" || password == "" || username == "")
            {
                return "F";
            }
        }

        // Authentication Bypass High
        if (userMap.containsKey("id")) {
            receiveTime = (String) userMap.get("id");
            log.info("receiveTime 1 " + receiveTime);
            receiveTime = AES.decrypt(receiveTime);
            log.info("receiveTime 2 " + receiveTime);
            receiveTimeMilli = Long.valueOf(receiveTime);
            receiveTimeMilli = receiveTimeMilli + 1;
            log.info("receiveTimeMilli 3 " + receiveTimeMilli);
            receiveTime = String.valueOf(receiveTimeMilli);
            log.info("receiveTime 4 " + receiveTime);
            receiveTime = AES.encrypt(receiveTime);
        }

        //SystemUser systemUser = new SystemUser();
        systemUser.setDeleteFlag(Constants.DELETE_FLAG_FALSE);
        systemUser.setUsername(username);
        systemUser.setPassword(password);
        systemUser.setImeiNo(imei);

        //Authentication Bypass High
        systemUser.setIdProofType(receiveTime);

        systemUser = userService.authenticateUser(systemUser, Constants.AUTHENTICATION_TYPE_SERVER,"AndroidTen");

        log.info("systemUser " + systemUser);

        Long userId = 0L;
        if (systemUser != null)
        {
            //check for blocked user
            if (systemUser.getBlockedStatus().equalsIgnoreCase("BLOCKED"))
            {
                return "B_" + systemUser.getUserTableId();
            }

            if (systemUser.getLoginType() != null && systemUser.getLoginType().equals("Normal"))
            {
                Authentication authentication = new Authentication();
                authentication.setLogin(systemUser.getUsername() == null ? "" : systemUser.getUsername());
                authentication.setPassword(systemUser.getPassword());
                authentication.setId(systemUser.getUserTableId());

                //userId = authenticationService.validateMember(authentication);
                userId = userService.validateMember(authentication);
                log.info("userId :::" + userId);
                if(userId == null || userId == 0)
                {
                    systemUser.setPasswordValidate(true);
                    return "PF_" + systemUser.getUserTableId();
                }
                log.info("--------userId after validation : -----" + userId);
            }
            else if (systemUser.getLoginType() != null && systemUser.getLoginType().equals("Ldap"))
            {
                /*
                 * String ldapUrl = applicationConfiguration.getValue("LDAP_URL"); String
                 * prefixUsername = applicationConfiguration.getValue("PREFIX_USERNAME");
                 *
                 * log.info("--------ldapUrl : -----" + ldapUrl);
                 * log.info("--------systemUser.getUsername() : -----" +
                 * systemUser.getUsername());
                 * log.info("--------systemUser.getPassword() : -----" +
                 * systemUser.getPassword()); log.info("--------prefixUsername : -----" +
                 * prefixUsername);
                 *
                 * boolean ldapLoginStatus = Utilities.checkLdapLogin(ldapUrl,
                 * systemUser.getUsername(), systemUser.getPassword(), prefixUsername);
                 *
                 * log.info("--------ldapLoginStatus : -----" + ldapLoginStatus);
                 * log.info("--------ldapLoginStatus : -----" + systemUser.getUserTableId());
                 *
                 * if (ldapLoginStatus) { userId = systemUser.getUserTableId(); }
                 * log.info("--------ldapLoginStatus : -----" + systemUser.getUserTableId());
                 */

                if(userMap.containsKey("isAdfs") && userMap.get("isAdfs").equals("Yes"))
                {
                    userId = systemUser.getUserTableId();
                }else {
                    userId = 0l;
                }

                log.info("--------ldapLoginStatus : -----" + systemUser.getUserTableId());

            }
            if (userId != null && userId > 0)
            {
                return "S_" + systemUser.getUserTableId();
            }
            else
            {
                return "F_" + systemUser.getUserTableId();
            }
        }
        else
        {
            return "F";
        }

    }

    public String authenticateUser(SystemUser systemUser)
    {
        return userService.isAndroid10UserReg(systemUser);

    }

    private List<String> territoryNames(String territoryNames) {
        List<String> terrList = new ArrayList<>();
        try {
            // Split the input string by pipe character and store in an array
            String[] parts = territoryNames.split("\\|");

            // Create a set to store distinct city names
            Set<String> citySet = new HashSet<>();

            for (String part : parts) {
                // Split each part by hyphen to get the city name
                String[] cityAndValue = part.split("-");
                if (cityAndValue.length >= 1) {
                    String cityName = cityAndValue[0];
                    citySet.add(cityName);
                }
            }

            // Convert the set to a list
            terrList = new ArrayList<>(citySet);

            // Print the distinct city names
            log.info("terrList  " + terrList);
        }catch (Exception e) {
            log.info("Exception ::: " + e);
        }
        return terrList;

    }

}
