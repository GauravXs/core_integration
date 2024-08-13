//before kafka production code start 13 mar 2020
/*
package com.mobicule.mcollections.integration.collection;

import com.mobicule.mcollections.core.commons.UpiUtility;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import org.apache.commons.codec.binary.Base64;
import com.mobicule.mcollections.integration.commons.Crypto;
import com.mobicule.mcollections.integration.commons.DeviceDetails;
import com.mobicule.mcollections.integration.commons.MerchantCollect;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.text.NumberFormat;
import java.util.Locale;
import com.mobicule.component.activitylogger.beans.NotificationActivity;
import java.util.Date;
import com.mobicule.component.activitylogger.threads.NotificationActivityStatusUpdate;
import com.mobicule.component.activitylogger.threads.NotificationActivityAddition;
import java.text.SimpleDateFormat;
import org.apache.commons.lang.StringUtils;
import com.mobicule.mcollections.core.commons.SmsFormXML;
import org.springframework.integration.support.MessageBuilder;
import org.json.me.JSONException;
import com.mobicule.mcollections.core.beans.Denomination;
import com.mobicule.component.activitylogger.beans.CommunicationActivity;
import java.text.ParseException;
import com.mobicule.component.mapconversion.xml.XMLToMap;
import com.mobicule.component.activitylogger.threads.CommunicationActivityStatusUpdate;
import com.mobicule.mcollections.webservice.adapter.KotakCollectionWebserviceAdapter;
import com.mobicule.component.activitylogger.threads.CommunicationActivityAddition;
import com.mobicule.component.mapconversion.xml.MapToXML;
import java.util.HashMap;
import java.util.Map;
import com.mobicule.component.activitylogger.beans.UserActivity;
import org.springframework.integration.MessageHeaders;
import com.mobicule.component.activitylogger.threads.UserActivityStatusUpdate;
import com.mobicule.component.system.db.StandardBean;
import com.mobicule.mcollections.core.beans.Agency;
import java.sql.Timestamp;
import com.mobicule.mcollections.core.beans.MPOSDetail;
import org.json.me.JSONArray;
import com.mobicule.mcollections.core.beans.TransactionType;
import com.mobicule.mcollections.core.beans.Cheque;
import java.util.List;
import com.mobicule.mcollections.core.beans.Feedback;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.beans.Collection;
import com.mobicule.mcollections.integration.commons.ServerUtilities;
import com.mobicule.component.activitylogger.threads.UserActivityAddition;
import org.json.me.JSONObject;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.beans.Image;
import java.util.ArrayList;
import org.springframework.integration.Message;
import org.slf4j.LoggerFactory;
import com.mobicule.mcollections.core.service.OfflineSMSService;
import com.mobicule.mcollections.core.commons.EmailUtilities;
import org.springframework.mail.SimpleMailMessage;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.AgencyService;
import com.mobicule.mcollections.core.service.CaseService;
import com.mobicule.mcollections.core.service.CollectionService;
import com.mobicule.component.activitylogger.service.NotificationActivityService;
import com.mobicule.component.activitylogger.service.CommunicationActivityService;
import com.mobicule.mcollections.core.service.SystemUserService;
import org.springframework.beans.factory.annotation.Autowired;
import com.mobicule.component.activitylogger.service.UserActivityService;
import org.slf4j.Logger;

public class CollectionsSubmissionService implements ICollectionsSubmissionService
{
    private Log log;
    @Autowired
    private UserActivityService userActivityService;
    @Autowired
    private SystemUserService systemUserService;
    @Autowired
    private CommunicationActivityService communicationActivityService;
    @Autowired
    private NotificationActivityService notificationActivityService;
    @Autowired
    private CollectionService collectionService;
    @Autowired
    private CaseService caseService;
    @Autowired
    private AgencyService agencyService;
    @Autowired
    ApplicationConfiguration applicationConfiguration;
    @Autowired
    private SimpleMailMessage simpleMailMessageForCashPaymentCreditCard;
    @Autowired
    private SimpleMailMessage simpleMailMessageForCashPaymentLoan;
    @Autowired
    private SimpleMailMessage simpleMailMessageForChequePaymentCreditCard;
    @Autowired
    private SimpleMailMessage simpleMailMessageForChequePaymentLoan;
    @Autowired
    private SimpleMailMessage simpleMailMessageForCardPayment;
    @Autowired
    private SimpleMailMessage simpleMailMessageForDDPDC;
    private EmailUtilities emailService;
    @Autowired
    private OfflineSMSService offlineSMSService;
    
    public CollectionsSubmissionService() {
        this.log = LogFactory.getLog((Class)this.getClass());
    }
    
    public EmailUtilities getEmailService() {
        return this.emailService;
    }
    
    public void setEmailService(final EmailUtilities emailService) {
        this.emailService = emailService;
    }
    
    public Message<String> execute(final Message<String> message) throws Throwable {
        this.log.info((Object)" -------- In CollectionsService -------- ");
        String status = "failure";
        String returnMessage = null;
        final String contractAccountNumber = "";
        String lockCode = "";
        String collectionCode = "";
        final String allocationNumber = "";
        final String bp = "";
        String amount = "0.0";
        final String pan = "";
        String email = "";
        String contact = "";
        String collectionStatus = "";
        String deviceDate = "";
        String revisitedDate = "";
        String area = "";
        final String mread = "";
        String emailAddressNew = "";
        String mobileNumberNew = "";
        String deviceTime = "";
        String collStatus = "";
        String receiptNumber = "";
        String remarks = "";
        final String billNo = "";
        final String batchNumber = "";
        String billCycle = "";
        String signaturePath = "";
        String signature = "";
        String caseId = "0L";
        String feedback_code = "";
        String ptpAmount = "0.00";
        String latitude = "";
        String longitude = "";
        final String partyName = "";
        String nextActionCode = "";
        String nextActionCodeDescription = "";
        boolean submissionFlag = false;
        final String regNo = "";
        String branchName = "";
        String paymentDate = "";
        String actioncode = "";
        String actionDesc = "";
        String resultCode = "";
        String resultDesc = "";
        String resultcodeAnddesc = "";
        String nextActionCodeValues = "";
        String start_lat = "";
        String start_long = "";
        String end_lat = "";
        String end_long = "";
        String allRisk = "";
        String team = "";
        String bucket = "";
        String result = "";
        List<Image> images = new ArrayList<Image>();
        final MessageHeaders messageHeader = message.getHeaders();
        final SystemUser systemUser = (SystemUser)messageHeader.get((Object)"user");
        try {
            final String requestSet = (String)message.getPayload();
            final JSONObject jsonObj = new JSONObject(requestSet);
            this.log.info((Object)("jsonObj-----------in collectionSubmissionService :: " + jsonObj));
            final JSONObject jsonData = (JSONObject)jsonObj.get("data");
            if (jsonData.has("images")) {
                jsonData.remove("images");
            }
            jsonObj.put("data", (Object)jsonData);
            final String requestWithoutImage = jsonObj.toString();
            final UserActivityAddition userActivityAddition = new UserActivityAddition(requestWithoutImage, this.userActivityService, "MSSQL");
            new Thread((Runnable)userActivityAddition).run();
            final UserActivity userActivity = userActivityAddition.extractUserActivity();
            final JSONObject jsonObject = new JSONObject(requestSet);
            this.log.info((Object)("jsonObject-----------in collectionSubmissionService :: " + jsonObject));
            final JSONObject data = (JSONObject)jsonObject.get("data");
            final JSONObject user = (JSONObject)jsonObject.get("user");
            final SystemUser systemUserNew = ServerUtilities.extractSystemUser(user);
            final SystemUser systemUserTemp = this.systemUserService.getUser(systemUserNew.getUserTableId());
            systemUserNew.setSupervisorMobileNumber(systemUserTemp.getSupervisorMobileNumber());
            systemUserNew.setSupervisorName(systemUserTemp.getSupervisorName());
            this.log.info((Object)("----system user ----" + systemUserNew));
            final Collection collection = new Collection();
            final Map reqMap = Utilities.createMapFromJSON(requestSet);
            final String type = (String)reqMap.get("type");
            final String requestEntity = (data.get("appl") == null) ? "" : data.getString("appl");
            collectionCode = (String)((data.get("collCode") == null) ? "" : data.get("collCode"));
            final String payMode = (String)((data.get("payMode") == null) ? "" : data.get("payMode"));
            try {
                if (collectionCode.equalsIgnoreCase("RTP") && payMode.equalsIgnoreCase("CSH")) {
                    result = this.systemUserService.checkDepositionLockedStatus((long)systemUserTemp.getUserTableId());
                    if (result.equalsIgnoreCase("R_100")) {
                        returnMessage = "Your limit is exceed more than 100% . Please deposit earlier collections.";
                        return this.responseBuilder(message, "failure", returnMessage, data.has("requId") ? data.getString("requId") : "");
                    }
                }
            }
            catch (Exception e) {
                this.log.error((Object)"---- Deposition Locked : Exception Detail ----", (Throwable)e);
            }
            if (type.toString().equalsIgnoreCase("collections")) {
                if (!data.has("lock")) {
                    returnMessage = "Error in sending Lock Code.";
                    return this.responseBuilder(message, status, returnMessage, "");
                }
                lockCode = (String)data.get("lock");
            }
            if (lockCode.equals("2")) {
                collection.setModifiedOn(Utilities.generateTimestamp("DD-mm-yyyy HH:mm:s"));
                this.log.info((Object)"before setting setModifiedBy value");
                collection.setModifiedBy(Long.valueOf(555555L));
                this.log.info((Object)"after setting setModifiedBy value");
                this.caseService.updateCase(collection, "COMPLETE");
                returnMessage = "Collection submitted Successfully.";
                status = "success";
                return this.responseBuilder(message, status, returnMessage, "");
            }
            if (lockCode.equals("0")) {
                collectionStatus = "INCOMPLETE";
            }
            if (lockCode.equals("1")) {
                collectionStatus = "COMPLETE";
            }
            if (type.toString().equalsIgnoreCase("collections")) {
                caseId = ((data.get("caseId") == null) ? "0L" : data.get("caseId").toString());
            }
            collectionCode = (String)((data.get("collCode") == null) ? "" : data.get("collCode"));
            if (data.has("feedback_code")) {
                feedback_code = data.getString("feedback_code");
            }
            if (!collectionCode.equalsIgnoreCase("RTP")) {
                revisitedDate = (String)((data.get("revisitDate") == null) ? "" : data.get("revisitDate"));
            }
            if (data.has("actionCode")) {
                collection.setActionCode(data.getString("actionCode"));
            }
            if (data.has("resultCode")) {
                collection.setResultCode(data.getString("resultCode"));
            }
            if (data.has("feedbackCode") && !collectionCode.equalsIgnoreCase("RTP")) {
                final JSONObject feedBackCodeJSON = data.getJSONObject("feedbackCode");
                final String actioncodeAnddesc = (feedBackCodeJSON.get("actionCode") == null) ? "" : feedBackCodeJSON.get("actionCode").toString();
                Boolean flag = false;
                if (actioncodeAnddesc.length() > 0) {
                    flag = true;
                }
                if (actioncodeAnddesc.contains("(")) {
                    actioncode = actioncodeAnddesc.substring(0, actioncodeAnddesc.indexOf("(") - 1).trim();
                    actionDesc = actioncodeAnddesc.substring(actioncodeAnddesc.indexOf("(") + 1, actioncodeAnddesc.indexOf(")"));
                }
                final JSONArray code = feedBackCodeJSON.getJSONArray("codes");
                final List<Feedback> feedBackCodeList = new ArrayList<Feedback>();
                for (int i = 0; i < code.length(); ++i) {
                    final JSONObject codeJSON = code.getJSONObject(i);
                    final Feedback feedBack = new Feedback();
                    resultcodeAnddesc = ((codeJSON.get("resultCode") == null) ? "" : codeJSON.get("resultCode").toString());
                    if (resultcodeAnddesc.length() > 0) {
                        flag = true;
                    }
                    if (resultcodeAnddesc.contains("(")) {
                        resultCode = resultcodeAnddesc.substring(0, resultcodeAnddesc.indexOf("(") - 1).trim();
                        resultDesc = resultcodeAnddesc.substring(resultcodeAnddesc.indexOf("(") + 1, resultcodeAnddesc.indexOf(")"));
                    }
                    if (codeJSON.has("nextActionCode")) {
                        nextActionCodeValues = ((codeJSON.get("nextActionCode") == null) ? "" : codeJSON.get("nextActionCode").toString());
                    }
                    if (nextActionCodeValues.length() > 0) {
                        flag = true;
                    }
                    if (nextActionCodeValues.contains("(")) {
                        nextActionCode = nextActionCodeValues.substring(0, nextActionCodeValues.indexOf("(") - 1).trim();
                        nextActionCodeDescription = nextActionCodeValues.substring(nextActionCodeValues.indexOf("(") + 1, nextActionCodeValues.indexOf(")"));
                    }
                    final String revisitedDate2 = "";
                    if (actioncode != "" && actionDesc != "") {
                        feedBack.setActionCode(actioncode);
                        feedBack.setActionDesc(actionDesc);
                    }
                    if (resultCode != "" && resultCode != "") {
                        feedBack.setResultCode(resultCode);
                        feedBack.setResultDesc(resultDesc);
                    }
                    if (nextActionCode != "" && nextActionCodeDescription != "") {
                        feedBack.setNextActionCode(nextActionCode);
                        feedBack.setNextActionCodeDescription(nextActionCodeDescription);
                    }
                    if (revisitedDate2 != "") {
                        feedBack.setRevisitDate(revisitedDate2);
                    }
                    if (flag) {
                        feedBackCodeList.add(feedBack);
                    }
                }
                collection.setFeedback((List)feedBackCodeList);
                this.log.info((Object)("feedBackCodeList : " + feedBackCodeList));
            }
            if ((!collectionCode.equalsIgnoreCase("RTP") || amount.equals("")) && !collectionCode.equalsIgnoreCase("PU")) {
                this.log.info((Object)"---test 1");
                if (data.has("ptpAmnt")) {
                    ptpAmount = data.getString("ptpAmnt");
                }
            }
            else if (collectionCode.equalsIgnoreCase("PU")) {
                this.log.info((Object)"---test 2");
                if (data.has("amnt")) {
                    ptpAmount = data.getString("amnt");
                }
            }
            else {
                this.log.info((Object)"---test 3");
                if (data.has("amnt")) {
                    amount = (String)data.get("amnt");
                }
            }
            deviceTime = (String)((data.get("deviceTime") == null) ? "" : data.get("deviceTime"));
            deviceDate = (String)((data.get("deviceDate") == null) ? "" : data.get("deviceDate"));
            area = (String)((data.get("area") == null) ? "" : data.get("area"));
            if (data.has("branchName")) {
                branchName = (String)((data.get("branchName") == null) ? "" : data.get("branchName"));
            }
            if (data.has("paymentDate")) {
                paymentDate = (String)((data.get("paymentDate") == null) ? "" : data.get("paymentDate"));
            }
            if (data.has("collStatus")) {
                collStatus = (String)((data.get("collStatus") == null) ? "" : data.get("collStatus"));
            }
            receiptNumber = (String)((data.get("receiptNum") == null) ? "" : data.get("receiptNum"));
            remarks = (String)((data.get("remarks") == null) ? "" : data.get("remarks"));
            if (type.toString().equalsIgnoreCase("collections")) {
                billCycle = (String)((data.get("bCycle") == null) ? "" : data.get("bCycle"));
            }
            if (data.has("alternateNumber")) {
                mobileNumberNew = ((data.get("alternateNumber") == null) ? "" : data.getString("alternateNumber"));
            }
            if (data.has("partyMobileNumber")) {
                contact = ((data.get("partyMobileNumber") == null) ? "" : data.getString("partyMobileNumber"));
            }
            if (data.has("allrisk")) {
                allRisk = ((data.get("allrisk") == null) ? "" : data.getString("allrisk"));
            }
            if (data.has("team")) {
                team = ((data.get("team") == null) ? "" : data.getString("team"));
            }
            if (data.has("bucket")) {
                bucket = ((data.get("bucket") == null) ? "" : data.getString("bucket"));
            }
            
            emailAddressNew = ((data.get("emailNew") == null) ? "" : data.getString("emailNew"));
            email = ((data.get("emailAdd") == null) ? "" : data.getString("emailAdd"));
            if (data.has("lat") && data.has("long")) {
                latitude = ((data.get("lat") == null) ? "" : data.getString("lat"));
                longitude = ((data.get("long") == null) ? "" : data.getString("long"));
            }
            else {
                latitude = "0.00";
                longitude = "0.00";
            }
            collection.setEmailAddress((data.get("emailAdd") == null) ? "" : data.getString("emailAdd"));
            collection.setCorrAddress((data.get("partyCorrAdd") == null) ? "" : data.getString("partyCorrAdd"));
            collection.setCorrLocation((data.getString("partycorrLoc") == null) ? "" : data.getString("partycorrLoc"));
            if (data.has("partycorrPin")) {
                collection.setCorrPin(data.getString("partycorrPin"));
            }
            if (data.has("secAdd")) {
                collection.setSecAddress(data.getString("secAdd"));
            }
            if (data.has("secLoc")) {
                collection.setSecLocation(data.getString("secLoc"));
            }
            if (data.has("secPin")) {
                collection.setSecPin(data.getString("secPin"));
            }
            if (data.has("dueDate")) {
                collection.setDueDate(data.getString("dueDate"));
            }
            if (type.toString().equalsIgnoreCase("collections")) {
                collection.setCaseId(Long.valueOf(Long.parseLong(caseId)));
            }
            collection.setOutstanding(Double.valueOf(Double.parseDouble((data.getString("totalOutstand") == null || data.getString("totalOutstand").equalsIgnoreCase("")) ? "0.0" : data.getString("totalOutstand"))));
            if (payMode.equals("CSH") && !amount.equals("0.0") && !collectionCode.equalsIgnoreCase("PU")) {
                if (!data.has("cash")) {
                    status = "failure";
                    returnMessage = "no cash details like found in request";
                    return this.responseBuilder(message, status, returnMessage, "");
                }
                this.extractCashDetails(pan, data, collection);
                this.log.info((Object)("###pan" + pan));
            }
            List<Cheque> cheques = new ArrayList<Cheque>();
            this.log.info((Object)("---collection code" + collectionCode));
            if (payMode.equalsIgnoreCase("DCARD") || payMode.equalsIgnoreCase("NB") || payMode.equalsIgnoreCase("UPI")) {
                collection.setCcapac("2003");
            }
            if (("CHQ".equals(payMode) || "DFT".equals(payMode) || "PDC".equals(payMode)) && !collectionCode.equalsIgnoreCase("PU")) {
                cheques = this.getCheques(systemUserNew, data);
                this.log.info((Object)("Cheques got : " + cheques));
                if (cheques == null || cheques.isEmpty()) {
                    returnMessage = "Cheque is mandatory in Cheque payment mode";
                    this.log.info((Object)("No Cheques, Return Message :" + returnMessage));
                    return this.responseBuilder(message, status, returnMessage, "");
                }
            }
            else if ("UPI".equalsIgnoreCase(payMode)) {
                final boolean flag2 = this.requestPayment(message);
                final String invoiceID = (data.get("txnId") == null) ? "" : data.getString("txnId");
                collection.setInvoiceId(invoiceID);
                if (!flag2) {
                    return this.responseBuilder(message, "Failure", "Some Error Occurred", "");
                }
            }
            if (data.has("loan")) {
                final JSONObject loanJSON = data.getJSONObject("loan");
                if (data.has("overdue")) {
                    collection.setOverdue(Double.valueOf(Double.parseDouble((loanJSON.get("overdue") == null || loanJSON.get("overdue").toString().equalsIgnoreCase("")) ? "0.0" : loanJSON.get("overdue").toString())));
                }
                final JSONArray transType = loanJSON.getJSONArray("transType");
                final List<TransactionType> transTypeList = new ArrayList<TransactionType>();
                for (int j = 0; j < transType.length(); ++j) {
                    final JSONObject transJSON = transType.getJSONObject(j);
                    final TransactionType transactionType = new TransactionType();
                    transactionType.setType(transJSON.get("tType").toString());
                    transactionType.setAmount((transJSON.get("amnt") == null || transJSON.getString("amnt").equalsIgnoreCase("")) ? "0.0" : transJSON.get("amnt").toString());
                    transTypeList.add(transactionType);
                }
                collection.setTransType((List)transTypeList);
                this.log.info((Object)("transTypeList : " + transTypeList));
            }
            if (data.has("CWO")) {
                final JSONObject ccJSON = (JSONObject)data.get("CWO");
                collection.setTad(Double.valueOf(Double.parseDouble((ccJSON.get("tad") == null || ccJSON.get("tad").toString().equalsIgnoreCase("")) ? "0.0" : ccJSON.get("tad").toString())));
                collection.setMad(Double.valueOf(Double.parseDouble((ccJSON.get("mad") == null || ccJSON.get("mad").toString().equalsIgnoreCase("")) ? "0.0" : ccJSON.get("mad").toString())));
                collection.setBuckAmt1(Double.valueOf(Double.parseDouble((ccJSON.get("bucket1") == null || ccJSON.get("bucket1").toString().equalsIgnoreCase("")) ? "0.0" : ccJSON.get("bucket1").toString())));
                collection.setBuckAmt2(Double.valueOf(Double.parseDouble((ccJSON.get("bucket2") == null || ccJSON.get("bucket2").toString().equalsIgnoreCase("")) ? "0.0" : ccJSON.get("bucket2").toString())));
                collection.setRollbackAmt(Double.valueOf(Double.parseDouble((ccJSON.get("rollbackAmnt") == null || ccJSON.get("rollbackAmnt").toString().equalsIgnoreCase("")) ? "0.0" : ccJSON.get("rollbackAmnt").toString())));
            }
            final JSONArray mPOSTransDetails = new JSONArray();
            JSONObject mPOSTransDetail = new JSONObject();
            final MPOSDetail mposDetail = new MPOSDetail();
            if (data.has("mPOSTransDetails")) {
                mPOSTransDetail = (JSONObject)data.get("mPOSTransDetails");
                mposDetail.setTransactionId(mPOSTransDetail.has("transactionId") ? mPOSTransDetail.get("transactionId").toString() : "");
                mposDetail.setBillNumber(mPOSTransDetail.has("billNumber") ? mPOSTransDetail.get("billNumber").toString() : "");
                mposDetail.setCardNo(mPOSTransDetail.has("cardNo") ? mPOSTransDetail.get("cardNo").toString() : "");
                mposDetail.setTransactionDateTime(mPOSTransDetail.has("transactionDatenTime") ? mPOSTransDetail.get("transactionDatenTime").toString() : "");
                mposDetail.setSwipeAmount(mPOSTransDetail.has("swipeAmount") ? mPOSTransDetail.get("swipeAmount").toString() : "");
                mposDetail.setCardHolderName(mPOSTransDetail.has("cardHolderName") ? mPOSTransDetail.get("cardHolderName").toString() : "");
                mposDetail.setCardType(mPOSTransDetail.has("cardType") ? mPOSTransDetail.get("cardType").toString() : "");
                mposDetail.setMdrAmnt(mPOSTransDetail.has("mdrAmnt") ? mPOSTransDetail.get("mdrAmnt").toString() : "");
                mposDetail.setServiceTaxAmnt(mPOSTransDetail.has("serviceTaxAmnt") ? mPOSTransDetail.get("serviceTaxAmnt").toString() : "");
            }
            collection.setMposDetail(mposDetail);
            if (type.toString().equalsIgnoreCase("collections")) {
                collection.setCollectionType("COLLECTIONS");
                collection.setCollectionNature("GENERAL");
            }
            if (type.toString().equalsIgnoreCase("randomCollections")) {
                collection.setCollectionType("RANDOM_COLLECTIONS");
                collection.setCollectionNature("GENERAL");
            }
            if (type.toString().equalsIgnoreCase("fileCollections")) {
                collection.setCollectionType("FILE_COLLECTIONS");
                collection.setCollectionNature("FILE");
            }
            if (type.toString().equalsIgnoreCase("fileRandomCollections")) {
                collection.setCollectionType("FILE_RANDOM_COLLECTIONS");
                collection.setCollectionNature("FILE");
            }
            if (type.toString().equalsIgnoreCase("collections") || type.toString().equalsIgnoreCase("fileCollections")) {
                this.log.info((Object)"--- file collection or else ---");
                if (data.has("partyName")) {
                    collection.setPartyName(data.getString("partyName"));
                }
            }
            if ((type.toString().equalsIgnoreCase("randomCollections") || type.toString().equalsIgnoreCase("fileRandomCollections")) && data.has("name")) {
                collection.setPartyName(data.getString("name"));
            }
            if ((type.toString().equalsIgnoreCase("collections") || type.toString().equalsIgnoreCase("fileCollections")) && data.has("partyId")) {
                collection.setContractAccountNumber(data.getString("partyId"));
            }
            if ((type.toString().equalsIgnoreCase("randomCollections") || type.toString().equalsIgnoreCase("fileRandomCollections")) && data.has("ca")) {
                collection.setContractAccountNumber(data.getString("ca"));
            }
            if (data.has("invoiceId")) {
                collection.setInvoiceId(data.getString("invoiceId"));
            }
            if (data.has("start_visit_time")) {
                collection.setVisitStartTime(data.getString("start_visit_time"));
                this.log.info((Object)("setVisitStartTime" + data.getString("start_visit_time")));
            }
            if (data.has("end_visit_time")) {
                collection.setVisitEndTime(data.getString("end_visit_time"));
                this.log.info((Object)("setVisitEndTime" + data.getString("end_visit_time")));
            }
            if (data.has("start_lat") && data.has("start_long")) {
                start_lat = ((data.get("start_lat") == null) ? "" : data.getString("start_lat"));
                start_long = ((data.get("start_long") == null) ? "" : data.getString("start_long"));
                this.log.info((Object)("startlatLong " + start_lat + start_long));
            }
            else {
                start_lat = "0.00";
                start_long = "0.00";
            }
            if (data.has("end_lat") && data.has("end_long")) {
                end_lat = ((data.get("end_lat") == null) ? "" : data.getString("end_lat"));
                end_long = ((data.get("end_long") == null) ? "" : data.getString("end_long"));
                this.log.info((Object)("endlatLong " + end_lat + end_long));
            }
            else {
                end_lat = "0.00";
                end_long = "0.00";
            }
            collection.setStartLatitude(start_lat);
            collection.setStartLongitude(start_long);
            collection.setEndLatitude(end_lat);
            collection.setEndLongitude(end_long);
            if (data.has("ccapac")) {
                collection.setCcapac(data.getString("ccapac"));
            }
            final int numberOfApacs = 1;
            collection.setNumberOfApacs(numberOfApacs);
            collection.setCollectionCode(collectionCode);
            collection.setAllocationNumber(allocationNumber);
            collection.setRequestId(data.has("requId") ? data.getString("requId") : new Timestamp(System.currentTimeMillis()).toString());
            collection.setMobileNumberNew(mobileNumberNew);
            collection.setEmailAddressNew(emailAddressNew);
            collection.setArea(area);
            collection.setChequeDetails((List)cheques);
            collection.setCollectionStatus(collStatus);
            collection.setCollectionCode(collectionCode);
            collection.setReceiptNumber(receiptNumber);
            collection.setRevisitDate(revisitedDate);
            if (data.has("alternateAddress")) {
                collection.setAlternateAddress((data.getString("alternateAddress") == null) ? "" : data.getString("alternateAddress"));
                this.log.info((Object)("alternateAddress" + data.getString("alternateAddress")));
            }
            collection.setMeterReading("sms");
            if (payMode.equalsIgnoreCase("Debit")) {
                collection.setPaymentMode("ORI");
            }
            else {
                collection.setPaymentMode(payMode);
            }
            this.log.info((Object)("paymode %%%%%%" + collection.getPaymentMode()));
            collection.setDeviceDate(deviceDate);
            collection.setDeviceTime(deviceTime);
            collection.setSubmissionDateTime(Utilities.sysDate());
            collection.setRemarks(remarks);
            collection.setCurrentBillNo(billNo);
            collection.setBusinessPartnerNumber(data.getString("unqNo"));
            collection.setAppl(data.getString("appl"));
            collection.setBillCycle(billCycle);
            if (data.has("partyMobileNumber")) {
                collection.setMobileNumber(data.getString("partyMobileNumber"));
            }
            collection.setSignaturePath(signaturePath);
            collection.setImages((List)images);
            collection.setUserName(systemUserNew.getName());
            collection.setUser(systemUserNew);
            collection.setContact(contact);
            collection.setEmail(email);
            collection.setBatchNumber(batchNumber);
            collection.setAmount(amount);
            collection.setAppropriateAmount(amount);
            collection.setArFeedbackCode((feedback_code == null) ? " " : feedback_code);
            collection.setPtpAmount(ptpAmount);
            collection.setBranchName(branchName);
            collection.setPaymentDate(paymentDate);
            collection.setDepositionStatus("COLLECTED AND NOT DEPOSITED");
            collection.setAllRisk(allRisk);
            collection.setTeam(team);
            collection.setBucket(bucket);
            this.log.info((Object)("----pan details----" + collection.getPan()));
            if (type.toString().equalsIgnoreCase("collections") || type.toString().equalsIgnoreCase("randomCollections")) {
                collection.setLatitude(latitude);
                collection.setLongitude(longitude);
            }
            final Agency agency = new Agency();
            agency.setAgencyId(systemUserNew.getAgencyId());
            final List<Agency> agencies = (List<Agency>)this.agencyService.searchAgency(agency);
            collection.setAgencyName(agencies.get(0).getAgencyName());
            this.log.info((Object)("collection trans type ------------------>" + collection.getTransType()));
            Utilities.primaryBeanSetter((StandardBean)collection, systemUserNew);
            this.log.info((Object)agencies.get(0).getAgencyName());
            if (data.has("sign")) {
                signature = (String)data.get("sign");
            }
            if (!signature.isEmpty()) {
                signaturePath = this.extractImagePath(collection, signature, "SIGNATURE_IMAGE_FILE_PATH", "");
            }
            if ((type.toString().equalsIgnoreCase("collections") || type.toString().equalsIgnoreCase("randomCollections") || type.toString().equalsIgnoreCase("fileCollections") || type.toString().equalsIgnoreCase("fileRandomCollections")) && data.has("images")) {
                final JSONArray imageDetails = data.getJSONArray("images");
                this.log.info((Object)("---image Details ----" + imageDetails));
                images = this.getImages(systemUserNew, imageDetails, collection);
                collection.setImages((List)images);
                this.log.info((Object)("--- image---" + images));
                if (images == null) {
                    status = "failure";
                    returnMessage = "Image Path Not Found";
                    return this.responseBuilder(message, status, returnMessage, "");
                }
            }
            if (collectionCode.equalsIgnoreCase("CU")) {
                try {
                    if (!this.caseService.checkDuplicateCustomerData(collection)) {
                        status = "success";
                        return this.responseBuilder(message, status, "Duplicate JSON !", "");
                    }
                    if (this.caseService.submitCustomerData(collection)) {
                        status = "success";
                        return this.responseBuilder(message, status, "Customer Data got submitted successfully", collection.getRequestId());
                    }
                    status = "failure";
                    return this.responseBuilder(message, status, "Some Error Occured", "");
                }
                catch (Exception e2) {
                    this.log.error((Object)"------Exception Detail while submission of customer is ", (Throwable)e2);
                    status = "failure";
                    return this.responseBuilder(message, status, "Some Error Occured", "");
                }
            }
            final int k = this.collectionService.checkDuplicateCollectionJSON(collection);
            if (k == 0) {
                this.log.info((Object)("collection cheque details ========----------->" + collection.getChequeDetails()));
                this.log.info((Object)("complete collection -------------->" + collection));
                submissionFlag = this.collectionService.submitCollection(collection);
                this.log.info((Object)("-------submissionFlag--------" + submissionFlag));
                if (submissionFlag) {
                    this.log.info((Object)"Collection submitted without violation");
                    this.caseService.updateCase(collection, collectionStatus);
                    try {
                        this.log.info((Object)"---PTP SMS ---");
                        this.log.info((Object)("getMeterReading " + collection.getMeterReading()));
                        this.log.info((Object)("collection type" + collection.getCollectionNature()));
                        if (collection.getCollectionCode().equalsIgnoreCase("RTP")) {
                            try {
                                this.sendCollectionsSms(collection, systemUserNew);
                                this.log.info((Object)"testing 1");
                            }
                            catch (Exception e3) {
                                this.log.error((Object)"---Error While sending SMS---", (Throwable)e3);
                            }
                        }
                    }
                    catch (Exception e3) {
                        this.log.info((Object)("Error while sending sms to PTP / Broken Promise / Door Lock" + e3));
                        e3.printStackTrace();
                    }
                    this.log.info((Object)"------------- Collection Submitted and Case Updated sucessfully -------------");
                    status = "success";
                    final UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity, "SUCCESS", this.userActivityService);
                    new Thread((Runnable)userActivityStatusUpdate).run();
                    if (result.equalsIgnoreCase("R_75")) {
                        returnMessage = "Your limit is exceed more than 75% . Please deposit earlier collections.";
                    }
                    else {
                        returnMessage = "Collection got submitted successfully";
                    }
                    return this.responseBuilder(message, status, returnMessage, collection.getRequestId());
                }
                System.out.println("Collection submitted with violation");
                this.log.info((Object)"Collection submitted with violation");
                status = "failure";
                final UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity, "FAILURE", this.userActivityService);
                new Thread((Runnable)userActivityStatusUpdate).run();
                return this.responseBuilder(message, status, "Some error has occured", "");
            }
            else {
                if (k == 1) {
                    this.log.info((Object)"--------- Collection Record already exists, JSON Duplicated! ------------");
                    status = "success";
                    returnMessage = "JSON DUPLICATED!!!";
                    if (type.toString().equalsIgnoreCase("collections")) {
                        returnMessage = "JSON DUPLICATED For Collections!!!";
                    }
                    if (type.toString().equalsIgnoreCase("randomCollections")) {
                        returnMessage = "JSON DUPLICATED For RandomCollections!!!";
                    }
                    if (type.toString().equalsIgnoreCase("fileCollections")) {
                        returnMessage = "JSON DUPLICATED For FileCollections!!!";
                    }
                    if (type.toString().equalsIgnoreCase("fileRandomCollections")) {
                        returnMessage = "JSON DUPLICATED For FileRandomCollections!!!";
                    }
                    final UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity, "IGNORE", this.userActivityService);
                    new Thread((Runnable)userActivityStatusUpdate).run();
                    return this.responseBuilder(message, status, returnMessage, collection.getRequestId());
                }
                this.log.info((Object)"Some error occured at Dao checkDuplicateCollectionJSON ");
                status = "failure";
                final UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity, "FAILURE", this.userActivityService);
                new Thread((Runnable)userActivityStatusUpdate).run();
                return this.responseBuilder(message, status, "Some error has occured", "");
            }
        }
        catch (Exception e4) {
            e4.printStackTrace();
            this.log.error((Object)("--- Exception In CollectionSubmissionService :: " + e4));
            returnMessage = "Collection submission Failed.";
            return this.responseBuilder(message, status, returnMessage, "");
        }
    }
    
    private void sendSms(final Collection collection, final SystemUser systemUserNew) {
        this.log.info((Object)("------- Before Sending SMS  --------" + collection));
        if (collection.getMobileNumber() != null && !collection.getMobileNumber().equalsIgnoreCase("")) {
            this.log.info((Object)"Sending sms to customer mobile number");
            this.callSMSDispatcher(collection, collection.getMobileNumber(), systemUserNew);
        }
        if (collection.getMobileNumberNew() != null && !collection.getMobileNumberNew().equalsIgnoreCase("") && !collection.getMobileNumberNew().equalsIgnoreCase(collection.getMobileNumber())) {
            this.log.info((Object)"Sending sms to customer alternate mobile number ");
            this.callSMSDispatcher(collection, collection.getMobileNumber(), systemUserNew);
        }
        if (systemUserNew.getMobileNumber() != null && !systemUserNew.getMobileNumber().equalsIgnoreCase("")) {
            this.log.info((Object)"Sending sms to FE mobile number ");
            this.generateSMSDispatcherMapForFE(collection, systemUserNew.getMobileNumber(), systemUserNew);
        }
    }
    
    private void generateSMSDispatcherMapForFE(final Collection collection, final String mobileNumber, final SystemUser systemUserNew) {
        this.log.info((Object)"---- Inside callSMSDispatcher --------");
        try {
            final String webserviceUrl = (String)this.applicationConfiguration.getValue((Object)"WEB_SERVICE_URL_SMS_DISPATCHER");
            final Map<String, Object> smsDispatcherMap = (Map<String, Object>)ServerUtilities.generateSMSDispatcherMapFEForNonRTP(collection, mobileNumber);
            final StringBuilder xmlRequest = MapToXML.convertMapToXML((Map)smsDispatcherMap, true, (Map)new HashMap());
            this.log.info((Object)("----- xmlRequest : -------" + (Object)xmlRequest));
            final CommunicationActivityAddition communicationActivityAddition = new CommunicationActivityAddition(systemUserNew.getUserTableId().toString(), systemUserNew.getImeiNo(), String.valueOf(collection.getAppl()) + "_" + collection.getCollectionType(), webserviceUrl, xmlRequest.toString(), this.communicationActivityService, "MSSQL");
            new Thread((Runnable)communicationActivityAddition).run();
            final KotakCollectionWebserviceAdapter kotakCollectionWebserviceAdapter = new KotakCollectionWebserviceAdapter();
            final String xmlResponse = kotakCollectionWebserviceAdapter.callWebserviceAndGetXmlString(xmlRequest.toString(), webserviceUrl);
            final CommunicationActivity communicationActivity = communicationActivityAddition.extractCommunicationActivity();
            Map<String, Object> result = null;
            if (xmlResponse != null && !xmlResponse.equals("")) {
                communicationActivity.setResponse(xmlResponse);
                final CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(communicationActivity, "SUCCESS", this.communicationActivityService);
                new Thread((Runnable)communicationActivityStatusUpdate).run();
                result = (Map<String, Object>)XMLToMap.convertXMLToMap(xmlResponse);
                this.log.info((Object)("----- Result of SMS Dispatch : -------" + result));
            }
            else {
                communicationActivity.setResponse("");
                final CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(communicationActivity, "FAILURE", this.communicationActivityService);
                new Thread((Runnable)communicationActivityStatusUpdate).run();
                this.log.info((Object)"----- Failure in sending SMS : -------");
            }
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
    }
    
    private void callSMSDispatcher(final Collection collection, final String mobileNumber, final SystemUser systemUserNew) {
        this.log.info((Object)"---- Inside callSMSDispatcher --------");
        try {
            final String webserviceUrl = (String)this.applicationConfiguration.getValue((Object)"WEB_SERVICE_URL_SMS_DISPATCHER");
            final Map<String, Object> smsDispatcherMap = (Map<String, Object>)ServerUtilities.generateSMSDispatcherMapForNonRTP(collection, mobileNumber);
            final StringBuilder xmlRequest = MapToXML.convertMapToXML((Map)smsDispatcherMap, true, (Map)new HashMap());
            this.log.info((Object)("----- xmlRequest : -------" + (Object)xmlRequest));
            final CommunicationActivityAddition communicationActivityAddition = new CommunicationActivityAddition(systemUserNew.getUserTableId().toString(), systemUserNew.getImeiNo(), String.valueOf(collection.getAppl()) + "_" + collection.getCollectionType(), webserviceUrl, xmlRequest.toString(), this.communicationActivityService, "MSSQL");
            new Thread((Runnable)communicationActivityAddition).run();
            final KotakCollectionWebserviceAdapter kotakCollectionWebserviceAdapter = new KotakCollectionWebserviceAdapter();
            final String xmlResponse = kotakCollectionWebserviceAdapter.callWebserviceAndGetXmlString(xmlRequest.toString(), webserviceUrl);
            final CommunicationActivity communicationActivity = communicationActivityAddition.extractCommunicationActivity();
            Map<String, Object> result = null;
            if (xmlResponse != null && !xmlResponse.equals("")) {
                communicationActivity.setResponse(xmlResponse);
                final CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(communicationActivity, "SUCCESS", this.communicationActivityService);
                new Thread((Runnable)communicationActivityStatusUpdate).run();
                result = (Map<String, Object>)XMLToMap.convertXMLToMap(xmlResponse);
                this.log.info((Object)("----- Result of SMS Dispatch : -------" + result));
            }
            else {
                communicationActivity.setResponse("");
                final CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(communicationActivity, "FAILURE", this.communicationActivityService);
                new Thread((Runnable)communicationActivityStatusUpdate).run();
                this.log.info((Object)"----- Failure in sending SMS : -------");
            }
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
    }
    
    public void extractCashDetails(String pan, final JSONObject data, final Collection collection) throws JSONException {
        JSONObject cashDetail = new JSONObject();
        cashDetail = (JSONObject)data.get("cash");
        collection.setDocType(cashDetail.get("docType").toString());
        collection.setDocRef(cashDetail.get("docRef").toString());
        if (collection.getDocType() != null && collection.getDocType().equalsIgnoreCase("PAN")) {
            collection.setPan(cashDetail.get("docRef").toString());
        }
        if (collection.getDocType() != null && collection.getDocType().equalsIgnoreCase("F60")) {
            this.log.info((Object)"@@ inside f60");
            collection.setDocRef("FORM60");
        }
        final JSONArray denominationArray = cashDetail.getJSONArray("denom");
        final List<Denomination> denominationList = new ArrayList<Denomination>();
        for (int i = 0; i < denominationArray.length(); ++i) {
            final JSONObject cashJSON = denominationArray.getJSONObject(i);
            final Denomination denomination = new Denomination();
            denomination.setNote((cashJSON.get("note") == null || cashJSON.get("note").toString().equalsIgnoreCase("")) ? "0" : cashJSON.get("note").toString());
            denomination.setNoteCount(Integer.parseInt((cashJSON.get("count") == null || cashJSON.get("count").toString().equalsIgnoreCase("")) ? "0" : cashJSON.get("count").toString()));
            denominationList.add(denomination);
        }
        collection.setDenomination((List)denominationList);
        if (cashDetail.has("pan")) {
            pan = (String)cashDetail.get("pan");
        }
        if (cashDetail.has("instDate")) {
            collection.setInstDate((String)cashDetail.get("instDate"));
        }
    }
    
    private Message<String> responseBuilder(final Message<String> message, final String status, final String returnMessage, final String reqId) throws JSONException {
        final JSONObject responseJSON = new JSONObject();
        final JSONObject data = new JSONObject();
        data.put("reqId", (Object)reqId);
        responseJSON.put("status", (Object)status);
        responseJSON.put("message", (Object)returnMessage);
        responseJSON.put("data", (Object)data);
        return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
    }
    
    private List<Cheque> getCheques(final SystemUser systemUser, final JSONObject data) throws JSONException {
        JSONArray chequeDetails = new JSONArray();
        if (data.has("cheque")) {
            chequeDetails = (JSONArray)data.get("cheque");
        }
        final List<Cheque> cheques = new ArrayList<Cheque>();
        JSONObject chequeDetail = new JSONObject();
        Cheque cheque = null;
        try {
            for (int index = 0; index < chequeDetails.length(); ++index) {
                chequeDetail = (JSONObject)chequeDetails.get(index);
                cheque = new Cheque();
                String chequeAmt = "0";
                String chequeDate = "";
                String micr = "";
                String chequeNumber = "";
                String bankName = "";
                String branch = "";
                String drawerAccountNumber = "";
                if (chequeDetail.has("drawAcNum")) {
                    drawerAccountNumber = (String)chequeDetail.get("drawAcNum");
                }
                if (chequeDetail.has("amnt")) {
                    chequeAmt = (String)chequeDetail.get("amnt");
                }
                if (chequeDetail.has("cheqDate")) {
                    chequeDate = (String)chequeDetail.get("cheqDate");
                }
                if (chequeDetail.has("micr")) {
                    micr = (String)chequeDetail.get("micr");
                }
                if (chequeDetail.has("cheqNum")) {
                    chequeNumber = (String)chequeDetail.get("cheqNum");
                }
                if (chequeDetail.has("bankName")) {
                    bankName = (String)chequeDetail.get("bankName");
                }
                if (chequeDetail.has("branch")) {
                    branch = (String)chequeDetail.get("branch");
                }
                cheque.setAmount(Double.valueOf(Double.parseDouble((chequeDetail.getString("amnt") == null || chequeDetail.getString("amnt").equalsIgnoreCase("")) ? "0.0" : chequeDetail.getString("amnt"))));
                cheque.setChequeDate(chequeDate);
                cheque.setChequeNo(chequeNumber);
                cheque.setMicrCode(micr);
                cheque.setDepositStatus("");
                cheque.setDepositDate("");
                cheque.setBankName(bankName);
                cheque.setBranch(branch);
                cheque.setDrawerAccountNumber(drawerAccountNumber);
                Utilities.primaryBeanSetter((StandardBean)cheque, systemUser);
                cheques.add(cheque);
            }
            return cheques;
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<Cheque>();
        }
    }
    
    private List<Image> getImages(final SystemUser systemUser, final JSONArray imageDetails, final Collection collection) throws JSONException {
        JSONObject imageDetail = new JSONObject();
        Image image = null;
        String imagePath = null;
        final List<Image> images = new ArrayList<Image>();
        for (int index = 0; index < imageDetails.length(); ++index) {
            imageDetail = (JSONObject)imageDetails.get(index);
            if (!imageDetail.has("image")) {
                return new ArrayList<Image>();
            }
            final String imageByteArray = (String)imageDetail.get("image");
            if (imageByteArray.isEmpty()) {
                return new ArrayList<Image>();
            }
            image = new Image();
            imagePath = this.extractImagePath(collection, imageByteArray, "IMAGE_FILE_PATH", String.valueOf(index));
            if (imagePath.equals("error")) {
                return null;
            }
            image = new Image();
            image.setPath(imagePath);
            Utilities.primaryBeanSetter((StandardBean)image, systemUser);
            images.add(image);
        }
        return images;
    }
    
    private String extractImagePath(final Collection collection, final String type, final String entity, final String index) {
        try {
            final String fileName = collection.getCaseId() + "_" + collection.getReceiptNumber() + "_" + System.currentTimeMillis();
            String filePath = "";
            if (index.equals("")) {
                filePath = Utilities.generateFilePath((String)this.applicationConfiguration.getValue((Object)entity), fileName);
            }
            else {
                filePath = Utilities.generateFilePath((String)this.applicationConfiguration.getValue((Object)entity), String.valueOf(fileName) + "_" + index);
            }
            if (Utilities.writeImage(filePath, type)) {
                return filePath;
            }
            return "error";
        }
        catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }
    
    private void sendCollectionsSms(final Collection collection, final SystemUser user) {
        this.log.info((Object)"------- IN Integration , Before Sending SMS  --------");
        try {
            String webserviceUrl = "";
            webserviceUrl = (String)this.applicationConfiguration.getValue((Object)"WEB_SERVICE_URL_SMS_DISPATCHER");
            final Map<String, Object> parametesMap = new HashMap<String, Object>();
            final Map<String, Object> parametersMaps = new HashMap<String, Object>();
            Map<String, Object> smsDispatcherMap = new HashMap<String, Object>();
            try {
                smsDispatcherMap = (Map<String, Object>)SmsFormXML.generateCollectionSmsXml((Map)parametersMaps, collection);
            }
            catch (Exception e) {
                this.log.info((Object)("Exception :- " + e));
            }
            this.log.info((Object)(("Request Sms request :- " + smsDispatcherMap.get("requestHeader") == null) ? "" : smsDispatcherMap.get("requestHeader")));
            final Map<String, Object> createUserParamMap = new HashMap<String, Object>();
            final String url = (String)((this.applicationConfiguration.getValue((Object)"emailSmsUrl") == null) ? "" : this.applicationConfiguration.getValue((Object)"emailSmsUrl"));
            createUserParamMap.put("ldapUrl", url);
            createUserParamMap.put("ldapRequest", (smsDispatcherMap.get("requestHeader") == null) ? "" : smsDispatcherMap.get("requestHeader"));
            try {
                final String responseXml = Utilities.postXML((Map)createUserParamMap);
                final String smsEmailType = (parametersMaps.get("smsEmailType") == null) ? "" :(String) parametersMaps.get("smsEmailType");
                parametesMap.put("request", (createUserParamMap == null) ? "" : createUserParamMap);
                parametesMap.put("response", (responseXml == null) ? "" : responseXml);
                parametesMap.put("smsEmailUrl", url);
                parametesMap.put("smsEmailType", "SMS");
                this.systemUserService.getInsertUpdateSmsEmailActivity((Map)parametesMap, user, this.communicationActivityService, collection);
                this.log.info((Object)("----- responseXml : -------" + responseXml));
            }
            catch (Exception e2) {
                this.log.info((Object)("Response :- " + e2));
            }
        }
        catch (Exception e3) {
            this.log.info((Object)("There is some error occured while sending sms to customer.In Integration" + e3));
        }
        if (!StringUtils.isEmpty(collection.getMobileNumberNew())) {
            try {
                String webserviceUrl = "";
                webserviceUrl = (String)this.applicationConfiguration.getValue((Object)"WEB_SERVICE_URL_SMS_DISPATCHER");
                final Map<String, Object> parametesMap = new HashMap<String, Object>();
                final Map<String, Object> parametersMaps = new HashMap<String, Object>();
                Map<String, Object> smsDispatcherMap = new HashMap<String, Object>();
                try {
                    smsDispatcherMap = (Map<String, Object>)SmsFormXML.generateCollectionAlterMobiSmsXml((Map)parametersMaps, collection);
                }
                catch (Exception e) {
                    this.log.info((Object)("Exception :- " + e));
                }
                this.log.info((Object)(("Request Sms request :- " + smsDispatcherMap.get("requestHeader") == null) ? "" : smsDispatcherMap.get("requestHeader")));
                final Map<String, Object> createUserParamMap = new HashMap<String, Object>();
                final String url = (String)((this.applicationConfiguration.getValue((Object)"emailSmsUrl") == null) ? "" : this.applicationConfiguration.getValue((Object)"emailSmsUrl"));
                createUserParamMap.put("ldapUrl", url);
                createUserParamMap.put("ldapRequest", (smsDispatcherMap.get("requestHeader") == null) ? "" : smsDispatcherMap.get("requestHeader"));
                try {
                    final String responseXml = Utilities.postXML((Map)createUserParamMap);
                    final String smsEmailType = (parametersMaps.get("smsEmailType") == null) ? "" :(String) parametersMaps.get("smsEmailType");
                    parametesMap.put("request", (createUserParamMap == null) ? "" : createUserParamMap);
                    parametesMap.put("response", (responseXml == null) ? "" : responseXml);
                    parametesMap.put("smsEmailUrl", url);
                    parametesMap.put("smsEmailType", "SMS");
                    this.systemUserService.getInsertUpdateSmsEmailActivity((Map)parametesMap, user, this.communicationActivityService, collection);
                    this.log.info((Object)("----- responseXml : -------" + responseXml));
                }
                catch (Exception e2) {
                    this.log.info((Object)("Response :- " + e2));
                }
            }
            catch (Exception e3) {
                this.log.info((Object)("Response :- " + e3));
            }
        }
    }
    
    private void callSMSDispatcher(final String amount, final String receiptNumber, final String paymentType, final String mobileNumber, final String type, final String apacCardNumber, final SystemUser user, final CommunicationActivityService communicationActivityService, final Collection collection) {
        this.log.info((Object)"---- Inside callSMSDispatcher --------");
        String webserviceUrl = "";
        if (type.equalsIgnoreCase("RSM")) {
            webserviceUrl = (String)this.applicationConfiguration.getValue((Object)"RSM_WEB_SERVICE_URL_SMS_DISPATCHER");
        }
        else {
            webserviceUrl = (String)this.applicationConfiguration.getValue((Object)"WEB_SERVICE_URL_SMS_DISPATCHER");
        }
        Map<String, Object> smsDispatcherMap;
        if (paymentType.equalsIgnoreCase("ORI")) {
            smsDispatcherMap = (Map<String, Object>)ServerUtilities.generateSMSDispatcherMapForDebit(amount, receiptNumber, paymentType, mobileNumber, type, apacCardNumber, collection);
        }
        else {
            smsDispatcherMap = (Map<String, Object>)ServerUtilities.generateSMSDispatcherMap(amount, receiptNumber, paymentType, mobileNumber, type, apacCardNumber);
        }
        final StringBuilder xmlRequest = MapToXML.convertMapToXML((Map)smsDispatcherMap, true, (Map)new HashMap());
        this.log.info((Object)("----- xmlRequest : -------" + (Object)xmlRequest));
    }
    
    private void generateSMSDispatcherMapForFE(final String amount, final String receiptNumber, final String paymentType, final String mobileNumber, final String type, final String feName, final SystemUser user, final CommunicationActivityService communicationActivityService, final Collection collection) {
        this.log.info((Object)"---- Inside generateSMSDispatcherMapForFE --------");
        final String webserviceUrl = (String)this.applicationConfiguration.getValue((Object)"WEB_SERVICE_URL_SMS_DISPATCHER");
        final Map<String, Object> smsDispatcherMap = (Map<String, Object>)ServerUtilities.generateSMSDispatcherMapForFE(amount, receiptNumber, paymentType, mobileNumber, type, feName);
        final StringBuilder xmlRequest = MapToXML.convertMapToXML((Map)smsDispatcherMap, true, (Map)new HashMap());
        this.log.info((Object)("----- xmlRequest : -------" + (Object)xmlRequest));
        final CommunicationActivityAddition communicationActivityAddition = new CommunicationActivityAddition(user.getUserTableId().toString(), user.getImeiNo(), String.valueOf(type) + "_" + collection.getCollectionType(), webserviceUrl, xmlRequest.toString(), communicationActivityService, "MSSQL");
        new Thread((Runnable)communicationActivityAddition).run();
        final KotakCollectionWebserviceAdapter kotakCollectionWebserviceAdapter = new KotakCollectionWebserviceAdapter();
        final String xmlResponse = kotakCollectionWebserviceAdapter.callWebserviceAndGetXmlString(xmlRequest.toString(), webserviceUrl);
        final CommunicationActivity communicationActivity = communicationActivityAddition.extractCommunicationActivity();
        Map<String, Object> result = null;
        if (xmlResponse != null && !xmlResponse.equals("")) {
            communicationActivity.setResponse(xmlResponse);
            final CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(communicationActivity, "SUCCESS", communicationActivityService);
            new Thread((Runnable)communicationActivityStatusUpdate).run();
            result = (Map<String, Object>)XMLToMap.convertXMLToMap(xmlResponse);
            this.log.info((Object)("----- Result of SMS Dispatch : -------" + result));
        }
        else {
            communicationActivity.setResponse("");
            final CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(communicationActivity, "FAILURE", communicationActivityService);
            new Thread((Runnable)communicationActivityStatusUpdate).run();
            this.log.info((Object)"----- Failure in sending SMS : -------");
        }
    }
    
    private void callEmailService(final Collection collection) {
        try {
            if (collection.getEmailAddress().equals("") && collection.getEmailAddressNew().equals("")) {
                this.log.info((Object)" -------- No Email Address found for Collection -------- ");
            }
            else {
                this.log.info((Object)"--- Sending Email ---");
                final String payMode = collection.getPaymentMode();
                this.log.info((Object)(" -------- payMode -------- " + payMode));
                if (payMode.equals("CSH")) {
                    this.sendEmailForCashPayment(collection);
                }
                if (payMode.equals("CHQ")) {
                    this.sendEmailForChequePayment(collection);
                }
                if (payMode.equalsIgnoreCase("PDC") || payMode.equalsIgnoreCase("DFT")) {
                    this.sendEmailForDDPDC(collection);
                }
            }
        }
        catch (Exception e) {
            this.log.info((Object)"-------Error Occured in sending Email---------", (Throwable)e);
            e.printStackTrace();
        }
    }
    
    private void sendEmailForCashPayment(final Collection collection) throws ParseException {
        this.log.info((Object)"---inside sendEmailForCashPayment---");
        String paymentDate = collection.getDeviceDate();
        this.log.info((Object)("---payment date---" + paymentDate));
        final SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        final SimpleDateFormat format2 = new SimpleDateFormat("dd-MMM-yyyy");
        final Date date = format1.parse(paymentDate);
        paymentDate = format2.format(date);
        this.log.info((Object)("---payment date after parsing---" + paymentDate));
        String emailText = "";
        if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase("CWO")) {
            this.log.info((Object)"---inside if condition---");
            this.log.info((Object)("----email text----" + this.simpleMailMessageForCashPaymentCreditCard.getText()));
            emailText = String.format(this.simpleMailMessageForCashPaymentCreditCard.getText(), collection.getName(), this.getFullFormApplType(collection.getAppl()), collection.getBusinessPartnerNumber(), collection.getReceiptNumber(), new StringBuilder(String.valueOf(collection.getAppropriateAmount())).toString(), new StringBuilder().append(paymentDate).toString(), collection.getBusinessPartnerNumber(), this.getTollFreeNumberForAppl(collection.getAppl()));
            this.log.info((Object)("----emailTest for card ----" + emailText));
        }
        else {
            this.log.info((Object)"--- inside else----");
            this.log.info((Object)("--- simpleMailMessageForCashPaymentLoan.getText() ----" + this.simpleMailMessageForCashPaymentLoan.getText()));
            this.log.info((Object)("---- collection.getName()-----" + collection.getName()));
            this.log.info((Object)("---- getTollFreeNumberForAppl-----" + this.getTollFreeNumberForAppl(collection.getAppl())));
            emailText = String.format(this.simpleMailMessageForCashPaymentLoan.getText(), collection.getName(), this.getFullFormApplType(collection.getAppl()), collection.getBusinessPartnerNumber(), collection.getReceiptNumber(), new StringBuilder(String.valueOf(collection.getAppropriateAmount())).toString(), new StringBuilder().append(paymentDate).toString(), collection.getBusinessPartnerNumber(), this.getTollFreeNumberForAppl(collection.getAppl()));
            this.log.info((Object)("---- email text is -----" + emailText));
        }
        String email = (collection.getEmailAddress() != null) ? collection.getEmailAddress() : "";
        this.log.info((Object)("---email----" + email));
        if (collection.getEmailAddress() != null && !collection.getEmailAddress().equalsIgnoreCase("")) {
            email = collection.getEmailAddress();
            this.log.info((Object)("---inside if email----" + email));
            if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase("CWO")) {
                final List<String> senderList = new ArrayList<String>();
                senderList.add(this.simpleMailMessageForCashPaymentCreditCard.getFrom());
                final List<String> receiverList = new ArrayList<String>();
                receiverList.add(email);
                final NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection.getUser().getUserTableId().toString(), "EMAIL", (List)senderList, (List)receiverList, this.simpleMailMessageForCashPaymentCreditCard.getSubject(), emailText, this.notificationActivityService, "MSSQL");
                new Thread((Runnable)notificationActivityAddition).run();
                if (this.emailService.sendMail(email, this.simpleMailMessageForCashPaymentCreditCard.getFrom(), this.simpleMailMessageForCashPaymentCreditCard.getSubject(), emailText)) {
                    final NotificationActivity notificationActivity = notificationActivityAddition.extractNotificationActivity();
                    final NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(notificationActivity, "SUCCESS", this.notificationActivityService);
                    new Thread((Runnable)notificationActivityStatusUpdate).run();
                }
                else {
                    final NotificationActivity notificationActivity = notificationActivityAddition.extractNotificationActivity();
                    final NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(notificationActivity, "FAILURE", this.notificationActivityService);
                    new Thread((Runnable)notificationActivityStatusUpdate).run();
                }
            }
            else {
                final List<String> senderList = new ArrayList<String>();
                senderList.add(this.simpleMailMessageForCashPaymentLoan.getFrom());
                this.log.info((Object)("simpleMailMessageForCashPaymentLoan.getFrom()" + this.simpleMailMessageForCashPaymentLoan.getFrom()));
                this.log.info((Object)("adding string data into reciverList" + email));
                final List<String> receiverList = new ArrayList<String>();
                receiverList.add(email);
                this.log.info((Object)("collection.getUser().getUserTableId()" + collection.getUser()));
                final NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection.getUser().getUserTableId().toString(), "EMAIL", (List)senderList, (List)receiverList, this.simpleMailMessageForCashPaymentLoan.getSubject(), emailText, this.notificationActivityService, "MSSQL");
                this.log.info((Object)("notificationActivityAddition" + notificationActivityAddition));
                new Thread((Runnable)notificationActivityAddition).run();
                if (this.emailService == null) {
                    this.log.info((Object)"emailService is null");
                }
                else {
                    this.log.info((Object)"emailService is not null");
                    this.log.info((Object)("email is : " + email));
                }
                if (this.emailService.sendMail(email, this.simpleMailMessageForCashPaymentLoan.getFrom(), this.simpleMailMessageForCashPaymentLoan.getSubject(), emailText)) {
                    final NotificationActivity notificationActivity = notificationActivityAddition.extractNotificationActivity();
                    final NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(notificationActivity, "SUCCESS", this.notificationActivityService);
                    new Thread((Runnable)notificationActivityStatusUpdate).run();
                }
                else {
                    final NotificationActivity notificationActivity = notificationActivityAddition.extractNotificationActivity();
                    final NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(notificationActivity, "FAILURE", this.notificationActivityService);
                    new Thread((Runnable)notificationActivityStatusUpdate).run();
                }
            }
        }
        if (collection.getEmailAddressNew() != null && !collection.getEmailAddressNew().equalsIgnoreCase("") && !collection.getEmailAddressNew().equalsIgnoreCase(email)) {
            email = collection.getEmailAddressNew();
            if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase("CWO")) {
                final List<String> senderList = new ArrayList<String>();
                senderList.add(this.simpleMailMessageForCashPaymentCreditCard.getFrom());
                final List<String> receiverList = new ArrayList<String>();
                receiverList.add(email);
                final NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection.getUser().getUserTableId().toString(), "EMAIL", (List)senderList, (List)receiverList, this.simpleMailMessageForCashPaymentCreditCard.getSubject(), emailText, this.notificationActivityService, "MSSQL");
                new Thread((Runnable)notificationActivityAddition).run();
                if (this.emailService.sendMail(collection.getEmailAddressNew(), this.simpleMailMessageForCashPaymentCreditCard.getFrom(), this.simpleMailMessageForCashPaymentCreditCard.getSubject(), emailText)) {
                    final NotificationActivity notificationActivity = notificationActivityAddition.extractNotificationActivity();
                    final NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(notificationActivity, "SUCCESS", this.notificationActivityService);
                    new Thread((Runnable)notificationActivityStatusUpdate).run();
                }
                else {
                    final NotificationActivity notificationActivity = notificationActivityAddition.extractNotificationActivity();
                    final NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(notificationActivity, "FAILURE", this.notificationActivityService);
                    new Thread((Runnable)notificationActivityStatusUpdate).run();
                }
            }
            else {
                final List<String> senderList = new ArrayList<String>();
                senderList.add(this.simpleMailMessageForCashPaymentLoan.getFrom());
                final List<String> receiverList = new ArrayList<String>();
                receiverList.add(email);
                final NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection.getUser().getUserTableId().toString(), "EMAIL", (List)senderList, (List)receiverList, this.simpleMailMessageForCashPaymentLoan.getSubject(), emailText, this.notificationActivityService, "MSSQL");
                new Thread((Runnable)notificationActivityAddition).run();
                if (this.emailService.sendMail(collection.getEmailAddressNew(), this.simpleMailMessageForCashPaymentLoan.getFrom(), this.simpleMailMessageForCashPaymentLoan.getSubject(), emailText)) {
                    final NotificationActivity notificationActivity = notificationActivityAddition.extractNotificationActivity();
                    final NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(notificationActivity, "SUCCESS", this.notificationActivityService);
                    new Thread((Runnable)notificationActivityStatusUpdate).run();
                }
                else {
                    final NotificationActivity notificationActivity = notificationActivityAddition.extractNotificationActivity();
                    final NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(notificationActivity, "FAILURE", this.notificationActivityService);
                    new Thread((Runnable)notificationActivityStatusUpdate).run();
                }
            }
        }
    }
    
    private void sendEmailForChequePayment(final Collection collection) throws ParseException {
        String paymentDate = collection.getDeviceDate();
        final SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        final SimpleDateFormat format2 = new SimpleDateFormat("dd-MMM-yyyy");
        final Date date = format1.parse(paymentDate);
        paymentDate = format2.format(date);
        final String email = (collection.getEmailAddress() != null) ? collection.getEmailAddress() : "";
        String chequeDetailString = "";
        final NumberFormat formatter = NumberFormat.getInstance(new Locale("en", "IN"));
        for (final Cheque cheque : collection.getChequeDetails()) {
            chequeDetailString = String.valueOf(chequeDetailString) + " Cheque No." + cheque.getChequeNo() + "     dated" + cheque.getChequeDate();
        }
        String emailText = "";
        if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase("CWO")) {
            emailText = String.format(this.simpleMailMessageForChequePaymentCreditCard.getText(), collection.getName(), this.getFullFormApplType(collection.getAppl()), collection.getBusinessPartnerNumber(), collection.getReceiptNumber(), new StringBuilder(String.valueOf(collection.getAppropriateAmount())).toString(), new StringBuilder().append(paymentDate).toString(), new StringBuilder().append(chequeDetailString).toString(), collection.getBusinessPartnerNumber(), this.getTollFreeNumberForAppl(collection.getAppl()));
        }
        else {
            emailText = String.format(this.simpleMailMessageForChequePaymentLoan.getText(), collection.getName(), this.getFullFormApplType(collection.getAppl()), collection.getBusinessPartnerNumber(), collection.getReceiptNumber(), new StringBuilder(String.valueOf(collection.getAppropriateAmount())).toString(), new StringBuilder().append(paymentDate).toString(), new StringBuilder().append(chequeDetailString).toString(), collection.getBusinessPartnerNumber(), this.getTollFreeNumberForAppl(collection.getAppl()));
        }
        if (collection.getEmailAddress() != null && !collection.getEmailAddress().equalsIgnoreCase("")) {
            if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase("CWO")) {
                final List<String> senderList = new ArrayList<String>();
                senderList.add(this.simpleMailMessageForChequePaymentCreditCard.getFrom());
                final List<String> receiverList = new ArrayList<String>();
                receiverList.add(email);
                final NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection.getUser().getUserTableId().toString(), "EMAIL", (List)senderList, (List)receiverList, this.simpleMailMessageForChequePaymentCreditCard.getSubject(), emailText, this.notificationActivityService, "MSSQL");
                new Thread((Runnable)notificationActivityAddition).run();
                if (this.emailService.sendMail(email, this.simpleMailMessageForChequePaymentCreditCard.getFrom(), this.simpleMailMessageForChequePaymentCreditCard.getSubject(), emailText)) {
                    final NotificationActivity notificationActivity = notificationActivityAddition.extractNotificationActivity();
                    final NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(notificationActivity, "SUCCESS", this.notificationActivityService);
                    new Thread((Runnable)notificationActivityStatusUpdate).run();
                }
                else {
                    final NotificationActivity notificationActivity = notificationActivityAddition.extractNotificationActivity();
                    final NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(notificationActivity, "FAILURE", this.notificationActivityService);
                    new Thread((Runnable)notificationActivityStatusUpdate).run();
                }
            }
            else {
                final List<String> senderList = new ArrayList<String>();
                senderList.add(this.simpleMailMessageForChequePaymentLoan.getFrom());
                final List<String> receiverList = new ArrayList<String>();
                receiverList.add(email);
                final NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection.getUser().getUserTableId().toString(), "EMAIL", (List)senderList, (List)receiverList, this.simpleMailMessageForChequePaymentLoan.getSubject(), emailText, this.notificationActivityService, "MSSQL");
                new Thread((Runnable)notificationActivityAddition).run();
                if (this.emailService.sendMail(email, this.simpleMailMessageForChequePaymentLoan.getFrom(), this.simpleMailMessageForChequePaymentLoan.getSubject(), emailText)) {
                    final NotificationActivity notificationActivity = notificationActivityAddition.extractNotificationActivity();
                    final NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(notificationActivity, "SUCCESS", this.notificationActivityService);
                    new Thread((Runnable)notificationActivityStatusUpdate).run();
                }
                else {
                    final NotificationActivity notificationActivity = notificationActivityAddition.extractNotificationActivity();
                    final NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(notificationActivity, "FAILURE", this.notificationActivityService);
                    new Thread((Runnable)notificationActivityStatusUpdate).run();
                }
            }
        }
        if (collection.getEmailAddressNew() != null && !collection.getEmailAddressNew().equalsIgnoreCase("") && !collection.getEmailAddressNew().equalsIgnoreCase(email)) {
            if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase("CWO")) {
                final List<String> senderList = new ArrayList<String>();
                senderList.add(this.simpleMailMessageForChequePaymentCreditCard.getFrom());
                final List<String> receiverList = new ArrayList<String>();
                receiverList.add(email);
                final NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection.getUser().getUserTableId().toString(), "EMAIL", (List)senderList, (List)receiverList, this.simpleMailMessageForChequePaymentCreditCard.getSubject(), emailText, this.notificationActivityService, "MSSQL");
                new Thread((Runnable)notificationActivityAddition).run();
                if (this.emailService.sendMail(collection.getEmailAddressNew(), this.simpleMailMessageForChequePaymentCreditCard.getFrom(), this.simpleMailMessageForChequePaymentCreditCard.getSubject(), emailText)) {
                    final NotificationActivity notificationActivity = notificationActivityAddition.extractNotificationActivity();
                    final NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(notificationActivity, "SUCCESS", this.notificationActivityService);
                    new Thread((Runnable)notificationActivityStatusUpdate).run();
                }
                else {
                    final NotificationActivity notificationActivity = notificationActivityAddition.extractNotificationActivity();
                    final NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(notificationActivity, "FAILURE", this.notificationActivityService);
                    new Thread((Runnable)notificationActivityStatusUpdate).run();
                }
            }
            else {
                final List<String> senderList = new ArrayList<String>();
                senderList.add(this.simpleMailMessageForChequePaymentLoan.getFrom());
                final List<String> receiverList = new ArrayList<String>();
                receiverList.add(email);
                final NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection.getUser().getUserTableId().toString(), "EMAIL", (List)senderList, (List)receiverList, this.simpleMailMessageForChequePaymentLoan.getSubject(), emailText, this.notificationActivityService, "MSSQL");
                new Thread((Runnable)notificationActivityAddition).run();
                if (this.emailService.sendMail(collection.getEmailAddressNew(), this.simpleMailMessageForChequePaymentLoan.getFrom(), this.simpleMailMessageForChequePaymentLoan.getSubject(), emailText)) {
                    final NotificationActivity notificationActivity = notificationActivityAddition.extractNotificationActivity();
                    final NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(notificationActivity, "SUCCESS", this.notificationActivityService);
                    new Thread((Runnable)notificationActivityStatusUpdate).run();
                }
                else {
                    final NotificationActivity notificationActivity = notificationActivityAddition.extractNotificationActivity();
                    final NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(notificationActivity, "FAILURE", this.notificationActivityService);
                    new Thread((Runnable)notificationActivityStatusUpdate).run();
                }
            }
        }
    }
    
    private void sendEmailForDDPDC(final Collection collection) throws ParseException {
        final String paymentDate = collection.getDeviceDate();
        final String collectionDate = collection.getDeviceDate();
        Date emailDate = new Date();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final SimpleDateFormat emailDateFormat = new SimpleDateFormat("ddMMMyyyy");
        emailDate = dateFormat.parse(collectionDate);
        final String email = collection.getEmailAddress().equals("") ? collection.getEmailAddress() : collection.getEmailAddress();
        String chequeDetailString = "";
        final NumberFormat formatter = NumberFormat.getInstance(new Locale("en", "IN"));
        String emailAmount = new StringBuilder(String.valueOf(collection.getAppropriateAmount())).toString();
        final DecimalFormat amountFormat = new DecimalFormat("#.00");
        try {
            emailAmount = amountFormat.format(Double.parseDouble(emailAmount));
        }
        catch (Exception e) {
            emailAmount = "0.00";
        }
        for (final Cheque cheque : collection.getChequeDetails()) {
            if (collection.getPaymentMode().equalsIgnoreCase("DFT")) {
                chequeDetailString = String.valueOf(chequeDetailString) + "<br/>Demand Draft No." + cheque.getChequeNo() + "     Dated " + new SimpleDateFormat("ddMMMyyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(cheque.getChequeDate()));
            }
            else {
                chequeDetailString = String.valueOf(chequeDetailString) + "<br/> PDC No." + cheque.getChequeNo() + "     Dated " + new SimpleDateFormat("ddMMMyyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(cheque.getChequeDate()));
            }
        }
        String emailText = "";
        if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase("CWO")) {
            if (collection.getPaymentMode().equalsIgnoreCase("DFT")) {
                emailText = String.format(this.simpleMailMessageForDDPDC.getText(), "Credit Card", "-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber(), collection.getReceiptNumber(), "Demand Draft", emailAmount, emailDateFormat.format(emailDate), chequeDetailString, "-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber());
            }
            else {
                emailText = String.format(this.simpleMailMessageForDDPDC.getText(), "Credit Card", "-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber(), collection.getReceiptNumber(), "PDC", emailAmount, emailDateFormat.format(emailDate), chequeDetailString, "-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber());
            }
        }
        else if (collection.getPaymentMode().equalsIgnoreCase("DFT")) {
            emailText = String.format(this.simpleMailMessageForDDPDC.getText(), "Personal Finance Loan ", "-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber(), collection.getReceiptNumber(), "Demand Draft", emailAmount, emailDateFormat.format(emailDate), chequeDetailString, "-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber());
        }
        else {
            emailText = String.format(this.simpleMailMessageForDDPDC.getText(), "Personal Finance Loan ", "-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber(), collection.getReceiptNumber(), "PDC", emailAmount, emailDateFormat.format(emailDate), chequeDetailString, "-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber());
        }
        if (collection.getEmailAddress() != null && !collection.getEmailAddress().equalsIgnoreCase("")) {
            if (!collection.getAppl().isEmpty()) {
                final List<String> senderList = new ArrayList<String>();
                senderList.add(this.simpleMailMessageForDDPDC.getFrom());
                final List<String> receiverList = new ArrayList<String>();
                receiverList.add(email);
                final NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection.getUser().getUserTableId().toString(), "EMAIL", (List)senderList, (List)receiverList, this.simpleMailMessageForDDPDC.getSubject(), emailText, this.notificationActivityService, "MSSQL");
                new Thread((Runnable)notificationActivityAddition).run();
                if (this.emailService.sendMail(email, this.simpleMailMessageForDDPDC.getFrom(), this.simpleMailMessageForDDPDC.getSubject(), emailText)) {
                    final NotificationActivity notificationActivity = notificationActivityAddition.extractNotificationActivity();
                    final NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(notificationActivity, "SUCCESS", this.notificationActivityService);
                    new Thread((Runnable)notificationActivityStatusUpdate).run();
                }
                else {
                    final NotificationActivity notificationActivity = notificationActivityAddition.extractNotificationActivity();
                    final NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(notificationActivity, "FAILURE", this.notificationActivityService);
                    new Thread((Runnable)notificationActivityStatusUpdate).run();
                }
            }
            else {
                this.log.info((Object)"----- Improper Information to Send Email");
            }
        }
        if (collection.getEmailAddressNew() != null && !collection.getEmailAddressNew().equalsIgnoreCase("") && !collection.getEmailAddressNew().equalsIgnoreCase(email)) {
            if (!collection.getAppl().isEmpty()) {
                final List<String> senderList = new ArrayList<String>();
                senderList.add(this.simpleMailMessageForCashPaymentCreditCard.getFrom());
                final List<String> receiverList = new ArrayList<String>();
                receiverList.add(email);
                final NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection.getUser().getUserTableId().toString(), "EMAIL", (List)senderList, (List)receiverList, this.simpleMailMessageForDDPDC.getSubject(), emailText, this.notificationActivityService, "MSSQL");
                new Thread((Runnable)notificationActivityAddition).run();
                if (this.emailService.sendMail(collection.getEmailAddressNew(), this.simpleMailMessageForDDPDC.getFrom(), this.simpleMailMessageForDDPDC.getSubject(), emailText)) {
                    final NotificationActivity notificationActivity = notificationActivityAddition.extractNotificationActivity();
                    final NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(notificationActivity, "SUCCESS", this.notificationActivityService);
                    new Thread((Runnable)notificationActivityStatusUpdate).run();
                }
                else {
                    final NotificationActivity notificationActivity = notificationActivityAddition.extractNotificationActivity();
                    final NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(notificationActivity, "FAILURE", this.notificationActivityService);
                    new Thread((Runnable)notificationActivityStatusUpdate).run();
                }
            }
            else {
                this.log.info((Object)"----- Improper Information to Send Email");
            }
        }
    }
    
    private String getFullFormApplType(final String appl) {
        if (appl.equalsIgnoreCase("CWO")) {
            return "Credit Card";
        }
        if (appl.equalsIgnoreCase("SPLN")) {
            return "Salaried Personal Loans-New";
        }
        if (appl.equalsIgnoreCase("RAR")) {
            return "Retail Asset Reconstruction";
        }
        if (appl.equalsIgnoreCase("CV")) {
            return "Commercial Vehicles";
        }
        if (appl.equalsIgnoreCase("HF")) {
            return "Home Finance";
        }
        if (appl.equalsIgnoreCase("CSG")) {
            return "Personal Finance";
        }
        if (appl.equalsIgnoreCase("SPL")) {
            return "Salaried Personal Loans";
        }
        if (appl.equalsIgnoreCase("SA")) {
            return "UNNATI [SARAL]";
        }
        if (appl.equalsIgnoreCase("TFE")) {
            return "Tractor and Farm Equipment Loans";
        }
        if (appl.equalsIgnoreCase("CE")) {
            return "Construction Equipment";
        }
        if (appl.equalsIgnoreCase("LAP")) {
            return "Loan Against Property";
        }
        if (appl.equalsIgnoreCase("SBG")) {
            return "Strategic Business Group";
        }
        if (appl.equalsIgnoreCase("GLN")) {
            return "Gold Loan";
        }
        if (appl.equalsIgnoreCase("LCV")) {
            return "Light Commercial Vehicles";
        }
        if (appl.equalsIgnoreCase("RHB")) {
            return "Rural Housing Business";
        }
        if (appl.equalsIgnoreCase("RARF")) {
            return "Retail ARD Funding";
        }
        if (appl.equalsIgnoreCase("CLF")) {
            return "Car Lease Finance";
        }
        if (appl.equalsIgnoreCase("CF")) {
            return "Car Finance";
        }
        return appl;
    }
    
    private String getTollFreeNumberForAppl(final String appl) {
        if (appl.equalsIgnoreCase("CWO") || appl.equalsIgnoreCase("HF") || appl.equalsIgnoreCase("LAP") || appl.equalsIgnoreCase("SPL") || appl.equalsIgnoreCase("SPLN") || appl.equalsIgnoreCase("CSG")) {
            return "1800 102 6022";
        }
        if (appl.equalsIgnoreCase("CV") || appl.equalsIgnoreCase("CE") || appl.equalsIgnoreCase("SA") || appl.equalsIgnoreCase("TFE") || appl.equalsIgnoreCase("LCV") || appl.equalsIgnoreCase("GLN")) {
            return "1800 209 5600";
        }
        if (appl.equalsIgnoreCase("RAR")) {
            return "1800 120 9820";
        }
        if (appl.equalsIgnoreCase("CF") || appl.equalsIgnoreCase("CLF")) {
            return "1800 209 5732";
        }
        return "";
    }
    
    public String GenerateChecksumValue(final Map<String, String> dataMap, final String confDesc, final String skey) throws Exception {
        this.log.info((Object)"---inside payement request-----");
        final MerchantCollect mrc = new MerchantCollect();
        final DeviceDetails deviceDetails = new DeviceDetails();
        deviceDetails.setApp("com.kcbg.upi");
        deviceDetails.setCapability("");
        deviceDetails.setGcmid("");
        deviceDetails.setGeocode("");
        deviceDetails.setId("");
        deviceDetails.setIp("");
        deviceDetails.setLocation("");
        deviceDetails.setMobile("919773861716");
        deviceDetails.setOs("");
        deviceDetails.setType("");
        mrc.setAggregatorVPA("kcbg@kotak");
        mrc.setAmount((String)dataMap.get("amount"));
        mrc.setCustomerId("919773861716");
        mrc.setDeviceDetails(deviceDetails);
        mrc.setExpiry(confDesc);
        mrc.setMerchantReferenceCode("CBGUPI");
        mrc.setOrderId((String)dataMap.get("receiptNumber"));
        mrc.setPayerVpa((String)dataMap.get("vpaAddress"));
        mrc.setReferenceId("");
        mrc.setRemarks(String.valueOf(dataMap.get("appl")) + dataMap.get("apacNumberValue"));
        mrc.setSubmerchantReferenceid("");
        mrc.setSubmerchantVPA("");
        mrc.setTimeStamp((String)dataMap.get("timestampForUPI"));
        mrc.setTxnId((String)dataMap.get("GenerateUUIDValue"));
        this.log.info((Object)("mrc.getInput() :: " + mrc.getInput()));
        final byte[] digest = Crypto.SHA256(mrc.getInput());
        final byte[] encData = Crypto.encrypt(Crypto.hexStringToByteArray(skey), digest);
        System.out.println("encData in hex Crypto.bytesToHex(encData) :" + Crypto.bytesToHex(encData));
        final String checkSumval = Base64.encodeBase64String(encData);
        return checkSumval;
    }
    
    public boolean requestPayment(final Message<String> message) {
        boolean flag = false;
        final JSONObject responseJSON = new JSONObject();
        try {
            final String requestSet = (String)message.getPayload();
            this.log.info((Object)("-------requestSet inside return-----" + requestSet));
            final String requestEntity = JSONPayloadExtractor.extract(requestSet, "entity");
            final String requestAction = JSONPayloadExtractor.extract(requestSet, "action");
            final String requestType = JSONPayloadExtractor.extract(requestSet, "type");
            final JSONObject jsonObject = new JSONObject(requestSet);
            final JSONObject user = (JSONObject)jsonObject.get("user");
            final SystemUser systemUserNew = ServerUtilities.extractSystemUser(user);
            final String imeiNo = "";
            final String draMobileNumber = "";
            final Long userTableId = 0L;
            this.log.info((Object)("-------requestSet is-----" + requestSet));
            this.log.info((Object)("-----requestEntity----" + requestEntity));
            this.log.info((Object)("-----requestAction----" + requestAction));
            this.log.info((Object)("-----requestType----" + requestType));
            this.log.info((Object)("---systemUserNew-----" + systemUserNew));
            this.log.info((Object)("---jsonObject-----" + jsonObject));
            final UserActivityAddition userActivityAddition = new UserActivityAddition(requestSet, this.userActivityService, "MSSQL");
            new Thread((Runnable)userActivityAddition).run();
            final UserActivity userActivity = userActivityAddition.extractUserActivity();
            final String fename = (user.getString("firstLastName") == null) ? "" : user.getString("firstLastName");
            final JSONObject data = (JSONObject)jsonObject.get("data");
            this.log.info((Object)("data part of json-------------------" + data));
            final String receiptNo = (data.getString("receiptNum") == null) ? "" : data.getString("receiptNum");
            this.log.info((Object)("--receiptNo--" + receiptNo));
            final String paymentMode = "";
            final String partyMobNo = (data.getString("partyMobNo") == null) ? "" : data.getString("partyMobNo");
            this.log.info((Object)("--partyMobNo--" + partyMobNo));
            final String mobileNew = (data.getString("mobileNew") == null) ? "" : data.getString("mobileNew");
            this.log.info((Object)("--mobileNew--" + mobileNew));
            final String appl = (data.getString("appl") == null) ? "" : data.getString("appl");
            this.log.info((Object)("--appl--" + appl));
            final String amount = (data.getString("amount") == null) ? "" : data.getString("amount");
            this.log.info((Object)("--amount--" + amount));
            final String vpa = (data.getString("payerVpa") == null) ? "" : data.getString("payerVpa");
            this.log.info((Object)("--vpa--" + vpa));
            final String txnId = (data.getString("txnId") == null) ? "" : data.getString("txnId");
            this.log.info((Object)("--txnId--" + txnId));
            final String unqNo = (data.getString("unqNo") == null) ? "" : data.getString("unqNo");
            this.log.info((Object)("--unqNo--" + unqNo));
            final Map<String, String> detailMap = new HashMap<String, String>();
            detailMap.put("appl", appl);
            detailMap.put("amount", amount);
            detailMap.put("receiptNumber", receiptNo);
            detailMap.put("apacNumberValue", unqNo);
            detailMap.put("vpaAddress", vpa);
            detailMap.put("GenerateUUIDValue", txnId);
            detailMap.put("timestampForUPI", Utilities.convertDate(new Timestamp(System.currentTimeMillis())));
            final String configurationCode = "UNVAL";
            String confDesc = "";
            String confType = "";
            final List<Map<String, Object>> rows = (List<Map<String, Object>>)this.collectionService.getAllPaySMSValidity(configurationCode);
            for (final Map row : rows) {
                confDesc = ((row.get("CONFIGURATION_DESCRIPTION") == null) ? "" : row.get("CONFIGURATION_DESCRIPTION").toString());
                confType = ((row.get("CONFIGURATION_TYPE") == null) ? "" : row.get("CONFIGURATION_TYPE").toString());
            }
            final String jsonRequestPaymentData = UpiUtility.generateUpiJsonReq((Map)detailMap, confDesc);
            this.log.info((Object)("jsonRequestPaymentData" + jsonRequestPaymentData));
            final String checkSumval = this.GenerateChecksumValue(detailMap, confDesc, "7A0D7DE6B5B0503A8044402B9653AB202887DD233378B9F3B4E72A71544B7AC0");
            this.log.info((Object)("checkSumval in UPI   " + checkSumval));
            final String jsonResponsePaymentData = UpiUtility.callWebserviceAndGetJsonString(jsonRequestPaymentData, checkSumval, this.applicationConfiguration);
            final Map<Object, Object> updateActivityMap = new HashMap<Object, Object>();
            if (jsonResponsePaymentData != null && !jsonResponsePaymentData.isEmpty()) {
                final Map<Object, Object> activityMap = new HashMap<Object, Object>();
                activityMap.put("REQUEST_TO_THIRD_PARTY", jsonRequestPaymentData);
                activityMap.put("THIRD_PARTY_STATUS", "PENDING");
                activityMap.put("DEVICE_REQUEST", requestSet);
                activityMap.put("CREATED_BY", systemUserNew.getUserTableId().toString());
                activityMap.put("MODIFIED_BY", systemUserNew.getUserTableId().toString());
                activityMap.put("RECEIPT_NUMBER", receiptNo);
                activityMap.put("UPI_TRANS_ID", txnId);
                activityMap.put("AMOUNT", amount);
                activityMap.put("VPA_ADDRESS", vpa);
                final int smsTableID = this.collectionService.smsPaymentActivityAddition((Map)activityMap);
                this.log.info((Object)("smsTableID ---- > " + smsTableID));
                final JSONObject jsonCode = new JSONObject(jsonResponsePaymentData);
                this.log.info((Object)("jsonCode--->" + jsonCode));
                final JSONObject responseData = (JSONObject)jsonCode.get("data");
                this.log.info((Object)("data part of json---" + responseData));
                String status = "";
                final Collection collection = new Collection();
                collection.setReceiptNumber(receiptNo);
                collection.setBusinessPartnerNumber(unqNo);
                collection.setAppl(appl);
                collection.setAmount(amount);
                collection.setFeName(fename);
                collection.setContact(partyMobNo);
                collection.setMobileNumberNew(mobileNew);
                if (((String)jsonCode.get("code")).equalsIgnoreCase("00")) {
                    status = "SUCCESS";
                    updateActivityMap.put("ID", smsTableID);
                    updateActivityMap.put("INVOICE_ID", txnId.toString());
                    updateActivityMap.put("THIRD_PARTY_RESPONSE", jsonResponsePaymentData);
                    updateActivityMap.put("MODIFIED_BY", systemUserNew.getUserTableId().toString());
                    updateActivityMap.put("THIRD_PARTY_STATUS", status);
                    updateActivityMap.put("DEVICE_RESPONSE_STATUS", status);
                    updateActivityMap.put("RECEIPT_NUMBER", receiptNo);
                    this.collectionService.smsPaymentActivityUpdation((Map)updateActivityMap);
                    flag = true;
                    collection.setPaymentMode(paymentMode);
                    this.log.info((Object)"Success in Request Payment----");
                }
                else {
                    status = "FAILURE";
                    updateActivityMap.put("ID", smsTableID);
                    updateActivityMap.put("THIRD_PARTY_RESPONSE", jsonResponsePaymentData);
                    updateActivityMap.put("MODIFIED_BY", systemUserNew.getUserTableId().toString());
                    updateActivityMap.put("THIRD_PARTY_STATUS", status);
                    updateActivityMap.put("DEVICE_RESPONSE_STATUS", status);
                    updateActivityMap.put("RECEIPT_NUMBER", receiptNo);
                    this.collectionService.smsPaymentActivityUpdation((Map)updateActivityMap);
                    flag = false;
                    collection.setPaymentMode(paymentMode);
                    this.sendAllPaySMSToCustomerAfterSubmittingReceipt(collection, status, systemUserNew.getMobileNumber(), systemUserNew.getUserTableId(), systemUserNew.getImeiNo());
                    this.log.info((Object)"Failure  in Request Payment----");
                }
            }
            else {
                flag = false;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }
    
    private void sendAllPaySMSToCustomerAfterSubmittingReceipt(final Collection collection, final String status, final String draMobileNumber, final Long userTableId, final String imeiNo) {
        final SystemUser systemUser = new SystemUser();
        systemUser.setImeiNo(imeiNo);
        systemUser.setUserTableId(userTableId);
        systemUser.setMobileNumber(draMobileNumber);
        if (collection.getContact() != null && !collection.getContact().equalsIgnoreCase("")) {
            this.log.info((Object)("Sending SMS on customer number " + collection.getContact()));
            this.generateSMSToCustomerOnSubmittingReceiptForAllPay(collection.getAmount(), collection.getReceiptNumber(), collection.getPaymentMode(), collection.getContact(), collection.getAppl(), collection.getFeName(), systemUser, this.communicationActivityService, collection, status);
        }
        if (collection.getMobileNumberNew() != null && !collection.getMobileNumberNew().equalsIgnoreCase("")) {
            this.log.info((Object)("Sending SMS on customer number " + collection.getContact()));
            this.generateSMSToCustomerOnSubmittingReceiptForAllPay(collection.getAmount(), collection.getReceiptNumber(), collection.getPaymentMode(), collection.getMobileNumberNew(), collection.getAppl(), collection.getFeName(), systemUser, this.communicationActivityService, collection, status);
        }
        if (draMobileNumber != null && !draMobileNumber.equalsIgnoreCase("")) {
            this.log.info((Object)("Sending sms to DRA mobile number " + collection.getAmount()));
            this.generateSMSToDRAOnSubmittingReceiptForAllPay(collection.getAmount(), collection.getReceiptNumber(), collection.getPaymentMode(), draMobileNumber, collection.getAppl(), collection.getFeName(), systemUser, this.communicationActivityService, collection, status);
        }
    }
    
    private void generateSMSToCustomerOnSubmittingReceiptForAllPay(final String amount, final String receiptNumber, final String paymentType, final String mobileNumber, final String appl, final String feName, final SystemUser user, final CommunicationActivityService communicationActivityService, final Collection collection, final String status) {
        this.log.info((Object)"---- Inside generateSMSToCustomerOnSubmittingReceiptForAllPay --------");
        final String webserviceUrl = (String)this.applicationConfiguration.getValue((Object)"WEB_SERVICE_URL_SMS_DISPATCHER");
        final Map<String, Object> smsDispatcherMap = (Map<String, Object>)ServerUtilities.generateAllPaySMSToCustomerOnSubmittingReceiptForAllPay(amount, receiptNumber, paymentType, mobileNumber, appl, feName, status, collection);
        this.log.info((Object)("---- Inside smsDispatcherMap --------" + smsDispatcherMap));
        final StringBuilder xmlRequest = MapToXML.convertMapToXML((Map)smsDispatcherMap, true, (Map)new HashMap());
        this.log.info((Object)("---- Inside xmlRequest --------" + (Object)xmlRequest));
        final CommunicationActivityAddition communicationActivityAddition = new CommunicationActivityAddition(user.getUserTableId().toString(), user.getImeiNo(), String.valueOf(appl) + "_" + collection.getCollectionType(), webserviceUrl, xmlRequest.toString(), communicationActivityService, "MSSQL");
        new Thread((Runnable)communicationActivityAddition).run();
        final KotakCollectionWebserviceAdapter kotakCollectionWebserviceAdapter = new KotakCollectionWebserviceAdapter();
        final String xmlResponse = kotakCollectionWebserviceAdapter.callWebserviceAndGetXmlString(xmlRequest.toString(), webserviceUrl);
        final CommunicationActivity communicationActivity = communicationActivityAddition.extractCommunicationActivity();
        Map<String, Object> result = null;
        if (xmlResponse != null && !xmlResponse.equals("")) {
            communicationActivity.setResponse(xmlResponse);
            final CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(communicationActivity, "SUCCESS", communicationActivityService);
            new Thread((Runnable)communicationActivityStatusUpdate).run();
            result = (Map<String, Object>)XMLToMap.convertXMLToMap(xmlResponse);
        }
        else {
            communicationActivity.setResponse("");
            final CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(communicationActivity, "FAILURE", communicationActivityService);
            new Thread((Runnable)communicationActivityStatusUpdate).run();
            this.log.info((Object)"----- Failure in sending SMS : -------");
        }
    }
    
    private void generateSMSToDRAOnSubmittingReceiptForAllPay(final String amount, final String receiptNumber, final String paymentType, final String mobileNumber, final String type, final String feName, final SystemUser user, final CommunicationActivityService communicationActivityService, final Collection collection, final String status) {
        this.log.info((Object)"---- Inside generateAllPaySMSOnSubmittingReceiptForAllPay --------");
        final String webserviceUrl = (String)this.applicationConfiguration.getValue((Object)"WEB_SERVICE_URL_SMS_DISPATCHER");
        final Map<String, Object> smsDispatcherMap = (Map<String, Object>)ServerUtilities.generateSMSToDRAOnSubmittingReceiptForAllPay(amount, receiptNumber, paymentType, mobileNumber, type, feName, status, collection);
        this.log.info((Object)("---- Inside smsDispatcherMap --------" + smsDispatcherMap));
        final StringBuilder xmlRequest = MapToXML.convertMapToXML((Map)smsDispatcherMap, true, (Map)new HashMap());
        this.log.info((Object)("---- Inside xmlRequest --------" + (Object)xmlRequest));
        final CommunicationActivityAddition communicationActivityAddition = new CommunicationActivityAddition(user.getUserTableId().toString(), user.getImeiNo(), String.valueOf(type) + "_" + collection.getCollectionType(), webserviceUrl, xmlRequest.toString(), communicationActivityService, "MSSQL");
        new Thread((Runnable)communicationActivityAddition).run();
        final KotakCollectionWebserviceAdapter kotakCollectionWebserviceAdapter = new KotakCollectionWebserviceAdapter();
        final String xmlResponse = kotakCollectionWebserviceAdapter.callWebserviceAndGetXmlString(xmlRequest.toString(), webserviceUrl);
        final CommunicationActivity communicationActivity = communicationActivityAddition.extractCommunicationActivity();
        Map<String, Object> result = null;
        if (xmlResponse != null && !xmlResponse.equals("")) {
            communicationActivity.setResponse(xmlResponse);
            final CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(communicationActivity, "SUCCESS", communicationActivityService);
            new Thread((Runnable)communicationActivityStatusUpdate).run();
            result = (Map<String, Object>)XMLToMap.convertXMLToMap(xmlResponse);
        }
        else {
            communicationActivity.setResponse("");
            final CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(communicationActivity, "FAILURE", communicationActivityService);
            new Thread((Runnable)communicationActivityStatusUpdate).run();
            this.log.info((Object)"----- Failure in sending SMS : -------");
        }
    }
}*/

// before kafka production code end 13 mar 2020

//kafka code start
/**
 ****************************************************************************** 
 * C O P Y R I G H T A N D C O N F I D E N T I A L I T Y N O T I C E
 * <p>
 * Copyright © 2013-2014 Mobicule Technologies Pvt. Ltd. All rights reserved.
 * This is proprietary information of Mobicule Technologies Pvt. Ltd.and is
 * subject to applicable licensing agreements. Unauthorized reproduction,
 * transmission or distribution of this file and its contents is a violation of
 * applicable laws.
 ****************************************************************************** 
 * 
 * @project mCollectionsKMIntegration-Phase2
 */
package com.mobicule.mcollections.integration.collection;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.mail.SimpleMailMessage;

import com.mobicule.component.activitylogger.beans.CommunicationActivity;
import com.mobicule.component.activitylogger.beans.NotificationActivity;
import com.mobicule.component.activitylogger.beans.UserActivity;
import com.mobicule.component.activitylogger.commons.ActivityLoggerConstants;
import com.mobicule.component.activitylogger.service.CommunicationActivityService;
import com.mobicule.component.activitylogger.service.NotificationActivityService;
import com.mobicule.component.activitylogger.service.UserActivityService;
import com.mobicule.component.activitylogger.threads.CommunicationActivityAddition;
import com.mobicule.component.activitylogger.threads.CommunicationActivityStatusUpdate;
import com.mobicule.component.activitylogger.threads.NotificationActivityAddition;
import com.mobicule.component.activitylogger.threads.NotificationActivityStatusUpdate;
import com.mobicule.component.activitylogger.threads.UserActivityAddition;
import com.mobicule.component.activitylogger.threads.UserActivityStatusUpdate;
import com.mobicule.component.mapconversion.xml.MapToXML;
import com.mobicule.component.mapconversion.xml.XMLToMap;
import com.mobicule.mcollections.core.beans.Agency;
import com.mobicule.mcollections.core.beans.Cheque;
import com.mobicule.mcollections.core.beans.Collection;
import com.mobicule.mcollections.core.beans.Denomination;
import com.mobicule.mcollections.core.beans.Feedback;
import com.mobicule.mcollections.core.beans.Image;
import com.mobicule.mcollections.core.beans.MPOSDetail;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.beans.TransactionType;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.EmailUtilities;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.SmsFormXML;
import com.mobicule.mcollections.core.commons.UpiUtility;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.AgencyService;
import com.mobicule.mcollections.core.service.CaseService;
import com.mobicule.mcollections.core.service.CollectionService;
import com.mobicule.mcollections.core.service.OfflineSMSService;
import com.mobicule.mcollections.core.service.SystemUserService;
import com.mobicule.mcollections.integration.commons.Crypto;
import com.mobicule.mcollections.integration.commons.DeviceDetails;
import com.mobicule.mcollections.integration.commons.MerchantCollect;
import com.mobicule.mcollections.integration.commons.ServerUtilities;
import com.mobicule.mcollections.webservice.adapter.KotakCollectionWebserviceAdapter;

/**
 * 
 * <enter description here>
 * 
 * @author Trupti
 * @see
 * 
 * @createdOn 25-May-2015
 * @modifiedOn
 * 
 * @copyright © 2013-2014 Mobicule Technologies Pvt. Ltd. All rights reserved.
 */
/**
 * @author prashant
 *
 */

public class CollectionsSubmissionService implements ICollectionsSubmissionService {

	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private UserActivityService userActivityService;

	@Autowired
	private SystemUserService systemUserService;

	@Autowired
	private CommunicationActivityService communicationActivityService;

	@Autowired
	private NotificationActivityService notificationActivityService;

	@Autowired
	private CollectionService collectionService;

	@Autowired
	private CaseService caseService;

	@Autowired
	private AgencyService agencyService;

	@Autowired
	ApplicationConfiguration applicationConfiguration;

	@Autowired
	private SimpleMailMessage simpleMailMessageForCashPaymentCreditCard;

	@Autowired
	private SimpleMailMessage simpleMailMessageForCashPaymentLoan;

	@Autowired
	private SimpleMailMessage simpleMailMessageForChequePaymentCreditCard;

	@Autowired
	private SimpleMailMessage simpleMailMessageForChequePaymentLoan;

	@Autowired
	private SimpleMailMessage simpleMailMessageForCardPayment;

	@Autowired
	private SimpleMailMessage simpleMailMessageForDDPDC;

	private EmailUtilities emailService;

	@Autowired
	private OfflineSMSService offlineSMSService;

	/*
	 * @Autowired private KafkaProducer<String, String> kafkaProducer;
	 */

	/*
	 * public OfflineSMSService getOfflineSMSService() { return offlineSMSService; }
	 * 
	 * public void setOfflineSMSService(OfflineSMSService offlineSMSService) {
	 * this.offlineSMSService = offlineSMSService; }
	 */

	public EmailUtilities getEmailService() {
		return emailService;
	}

	public void setEmailService(EmailUtilities emailService) {
		this.emailService = emailService;
	}

	@Override
	public Message<String> execute(Message<String> message) throws Throwable {

		String request = message.getPayload();
		String partitionType;

		try {

			JSONObject jsonObj = new JSONObject(request);

			JSONObject data = (JSONObject) jsonObj.get(JsonConstants.DATA);

			JSONObject user = (JSONObject) jsonObj.get(JsonConstants.SYSTEM_USER);

			SystemUser systemUserNew = ServerUtilities.extractSystemUser(user);

			String collectionCode = data.get(JsonConstants.RequestData.COLLECTION_CODE) == null ? Constants.EMPTY_STRING
					: (String) data.get(JsonConstants.RequestData.COLLECTION_CODE);

			String payMode = data.get(JsonConstants.RequestData.PAY_MODE) == null ? Constants.EMPTY_STRING
					: (String) data.get(JsonConstants.RequestData.PAY_MODE);

			if (collectionCode.equalsIgnoreCase(Constants.READY_TO_PAY)
					&& payMode.equalsIgnoreCase(Constants.PAYMENT_MODE_CASH)) {

				if (systemUserService.checkDepositionLockedStatus(systemUserNew.getUserTableId())
						.equalsIgnoreCase("R_100")) {

					return responseBuilder(message, JsonConstants.FAILURE,
							"Your limit is exceed more than 100% . Please deposit earlier collection",
							data.has(JsonConstants.REQUEST_ID) == true ? data.getString(JsonConstants.REQUEST_ID)
									: Constants.EMPTY_STRING);

				}

			}
			partitionType = data.get(JsonConstants.RequestData.PRODUCT_TYPE) == null ? Constants.EMPTY_STRING
					: (String) data.get(JsonConstants.RequestData.PRODUCT_TYPE);

			sendMsgOnKafkaTopic(request, partitionType);
			
			Map<String, Object> parameterMap = new HashMap<>();
			
			parameterMap.put("allocStatus",data.optString("allocStatus"));
			parameterMap.put("collStatus",data.optString("collStatus"));
			parameterMap.put("apacCard",data.optString("apacCardNumber"));
			
			return responseBuilder(message, JsonConstants.SUCCESS, JsonConstants.COLLECTION_SUBMIT_SUCCESS,
					data.has(JsonConstants.REQUEST_ID) == true ? data.getString(JsonConstants.REQUEST_ID)
							: new Timestamp(System.currentTimeMillis()).toString());

		} catch (Exception e) {

			log.error("----Exception occured while producing data onto topic");

			return responseBuilder(message, JsonConstants.FAILURE, JsonConstants.SOME_ERROR, Constants.EMPTY_STRING);
		}

	}

	private void sendSms(Collection collection, SystemUser systemUserNew) {
		log.info("------- Before Sending SMS  --------" + collection);
		if (collection.getMobileNumber() != null
				&& !collection.getMobileNumber().equalsIgnoreCase(Constants.EMPTY_STRING)) {
			log.info("Sending sms to customer mobile number");

			callSMSDispatcher(collection, collection.getMobileNumber(), systemUserNew);
		}

		if (collection.getMobileNumberNew() != null
				&& !collection.getMobileNumberNew().equalsIgnoreCase(Constants.EMPTY_STRING)
				&& !collection.getMobileNumberNew().equalsIgnoreCase(collection.getMobileNumber())) {
			log.info("Sending sms to customer alternate mobile number ");

			callSMSDispatcher(collection, collection.getMobileNumber(), systemUserNew);
		}

		if (systemUserNew.getMobileNumber() != null
				&& !systemUserNew.getMobileNumber().equalsIgnoreCase(Constants.EMPTY_STRING)) {
			log.info("Sending sms to FE mobile number ");

			generateSMSDispatcherMapForFE(collection, systemUserNew.getMobileNumber(), systemUserNew);

		}

	}

	private void generateSMSDispatcherMapForFE(Collection collection, String mobileNumber, SystemUser systemUserNew) {
		log.info("---- Inside callSMSDispatcher --------");
		try {
			String webserviceUrl = (String) applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");

			Map<String, Object> smsDispatcherMap;

			smsDispatcherMap = ServerUtilities.generateSMSDispatcherMapFEForNonRTP(collection, mobileNumber);

			StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true, new HashMap<String, String>());

			log.info("----- xmlRequest : -------" + xmlRequest);

			CommunicationActivityAddition communicationActivityAddition = new CommunicationActivityAddition(
					systemUserNew.getUserTableId().toString(), systemUserNew.getImeiNo(),
					(collection.getAppl() + "_" + collection.getCollectionType()), webserviceUrl, xmlRequest.toString(),
					communicationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);

			new Thread(communicationActivityAddition).run();

			KotakCollectionWebserviceAdapter kotakCollectionWebserviceAdapter = new KotakCollectionWebserviceAdapter();

			String xmlResponse = kotakCollectionWebserviceAdapter.callWebserviceAndGetXmlString(xmlRequest.toString(),
					webserviceUrl);

			CommunicationActivity communicationActivity = communicationActivityAddition.extractCommunicationActivity();

			Map<String, Object> result = null;

			if (null != xmlResponse && !xmlResponse.equals(Constants.EMPTY_STRING)) {
				communicationActivity.setResponse(xmlResponse);

				CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
						communicationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), communicationActivityService);

				new Thread(communicationActivityStatusUpdate).run();

				result = XMLToMap.convertXMLToMap(xmlResponse);

				log.info("----- Result of SMS Dispatch : -------" + result);
			} else {
				communicationActivity.setResponse(ActivityLoggerConstants.EMPTY_STRING);

				CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
						communicationActivity, (ActivityLoggerConstants.STATUS_FAILURE), communicationActivityService);

				new Thread(communicationActivityStatusUpdate).run();

				log.info("----- Failure in sending SMS : -------");
			}
		} catch (ParseException e) {
// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void callSMSDispatcher(Collection collection, String mobileNumber, SystemUser systemUserNew) {
		log.info("---- Inside callSMSDispatcher --------");
		try {
			String webserviceUrl = (String) applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");

			Map<String, Object> smsDispatcherMap = ServerUtilities.generateSMSDispatcherMapForNonRTP(collection,
					mobileNumber);

			StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true, new HashMap<String, String>());

			log.info("----- xmlRequest : -------" + xmlRequest);

			CommunicationActivityAddition communicationActivityAddition = new CommunicationActivityAddition(
					systemUserNew.getUserTableId().toString(), systemUserNew.getImeiNo(),
					(collection.getAppl() + "_" + collection.getCollectionType()), webserviceUrl, xmlRequest.toString(),
					communicationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);

			new Thread(communicationActivityAddition).run();

			KotakCollectionWebserviceAdapter kotakCollectionWebserviceAdapter = new KotakCollectionWebserviceAdapter();

			String xmlResponse = kotakCollectionWebserviceAdapter.callWebserviceAndGetXmlString(xmlRequest.toString(),
					webserviceUrl);

			CommunicationActivity communicationActivity = communicationActivityAddition.extractCommunicationActivity();

			Map<String, Object> result = null;

			if (null != xmlResponse && !xmlResponse.equals(Constants.EMPTY_STRING)) {
				communicationActivity.setResponse(xmlResponse);

				CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
						communicationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), communicationActivityService);

				new Thread(communicationActivityStatusUpdate).run();

				result = XMLToMap.convertXMLToMap(xmlResponse);

				log.info("----- Result of SMS Dispatch : -------" + result);
			} else {
				communicationActivity.setResponse(ActivityLoggerConstants.EMPTY_STRING);

				CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
						communicationActivity, (ActivityLoggerConstants.STATUS_FAILURE), communicationActivityService);

				new Thread(communicationActivityStatusUpdate).run();

				log.info("----- Failure in sending SMS : -------");
			}
		} catch (ParseException e) {
// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void extractCashDetails(String pan, JSONObject data, Collection collection) throws JSONException {
		JSONObject cashDetail = new JSONObject();

		cashDetail = (JSONObject) data.get(JsonConstants.RequestData.CASH);

		collection.setDocType(cashDetail.get(JsonConstants.DOCUMENT_TYPE).toString());
		collection.setDocRef(cashDetail.get(JsonConstants.DOCUMENT_REFERENCE).toString());

		if (collection.getDocType() != null && collection.getDocType().equalsIgnoreCase("PAN")) {
			collection.setPan(cashDetail.get(JsonConstants.DOCUMENT_REFERENCE).toString());
		}
		if (collection.getDocType() != null && collection.getDocType().equalsIgnoreCase("F60")) {
			log.info("@@ inside f60");

// pan = pan + "FORM60";
			/* collection.setPan("FORM60"); */
			collection.setDocRef("FORM60");

		}

		JSONArray denominationArray = cashDetail.getJSONArray(JsonConstants.DENOMINATION);

		List<Denomination> denominationList = new ArrayList<Denomination>();

		for (int i = 0; i < (denominationArray.length()); i++) {
			JSONObject cashJSON = denominationArray.getJSONObject(i);

			Denomination denomination = new Denomination();

			denomination.setNote(cashJSON.get(JsonConstants.DENOMINATION_NOTE) == null
					|| cashJSON.get(JsonConstants.DENOMINATION_NOTE).toString().equalsIgnoreCase(Constants.EMPTY_STRING)
							? "0"
							: cashJSON.get(JsonConstants.DENOMINATION_NOTE).toString());
			denomination
					.setNoteCount(Integer.parseInt(cashJSON.get(JsonConstants.DENOMINATION_COUNT) == null || cashJSON
							.get(JsonConstants.DENOMINATION_COUNT).toString().equalsIgnoreCase(Constants.EMPTY_STRING)
									? "0"
									: cashJSON.get(JsonConstants.DENOMINATION_COUNT).toString()));

			denominationList.add(denomination);
		}
		collection.setDenomination(denominationList);

		if (cashDetail.has(JsonConstants.RequestData.PAN)) {
			pan = (String) cashDetail.get(JsonConstants.RequestData.PAN);
		}

		if (cashDetail.has(JsonConstants.INSTRUMENT_DATE)) {
			collection.setInstDate((String) cashDetail.get(JsonConstants.INSTRUMENT_DATE));
		}
	}

	private Message<String> responseBuilder(Message<String> message, String status, String returnMessage, String reqId)
			throws JSONException {
		JSONObject responseJSON = new JSONObject();
		JSONObject data = new JSONObject();

		data.put("reqId", reqId);

		responseJSON.put(JsonConstants.STATUS, status);
		responseJSON.put(JsonConstants.MESSAGE, returnMessage);
		responseJSON.put(JsonConstants.DATA, data);

		return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
	}

	private List<Cheque> getCheques(SystemUser systemUser, JSONObject data) throws JSONException {
		JSONArray chequeDetails = new JSONArray();

		if (data.has(JsonConstants.RequestData.CHEQUE)) {
			chequeDetails = (JSONArray) data.get(JsonConstants.RequestData.CHEQUE);
		}

		List<Cheque> cheques = new ArrayList<Cheque>();
		JSONObject chequeDetail = new JSONObject();
		Cheque cheque = null;

		try {
			for (int index = 0; index < chequeDetails.length(); index++) {
				chequeDetail = (JSONObject) chequeDetails.get(index);
				cheque = new Cheque();

				String chequeAmt = "0";
				String chequeDate = Constants.EMPTY_STRING;
				String micr = Constants.EMPTY_STRING;
				String chequeNumber = Constants.EMPTY_STRING;
				String bankName = Constants.EMPTY_STRING;
				String branch = Constants.EMPTY_STRING;
				String drawerAccountNumber = Constants.EMPTY_STRING;
				if (chequeDetail.has(JsonConstants.DRAWER_ACCOUNT_NUMBER)) {
					drawerAccountNumber = (String) chequeDetail.get(JsonConstants.DRAWER_ACCOUNT_NUMBER);
				}

				if (chequeDetail.has(JsonConstants.RequestData.AMOUNT)) {
					chequeAmt = (String) chequeDetail.get(JsonConstants.RequestData.AMOUNT);
				}

				if (chequeDetail.has(JsonConstants.CHEQUE_DATE)) {
					chequeDate = (String) chequeDetail.get(JsonConstants.CHEQUE_DATE);
				}

				if (chequeDetail.has(JsonConstants.RequestData.MICR)) {
					micr = (String) chequeDetail.get(JsonConstants.RequestData.MICR);
				}

				if (chequeDetail.has(JsonConstants.RequestData.CHEQUE_NUMBER)) {
					chequeNumber = (String) chequeDetail.get(JsonConstants.RequestData.CHEQUE_NUMBER);
				}

				if (chequeDetail.has(JsonConstants.RequestData.BANK_NAME)) {
					bankName = (String) chequeDetail.get(JsonConstants.RequestData.BANK_NAME);
				}

				if (chequeDetail.has(JsonConstants.RequestData.BRANCH)) {
					branch = (String) chequeDetail.get(JsonConstants.RequestData.BRANCH);
				}

				cheque.setAmount(Double.parseDouble(chequeDetail.getString(JsonConstants.AMOUNT) == null
						|| chequeDetail.getString(JsonConstants.AMOUNT).equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0"
								: chequeDetail.getString(JsonConstants.AMOUNT)));
				cheque.setChequeDate(chequeDate);
				cheque.setChequeNo(chequeNumber);
				cheque.setMicrCode(micr);
				cheque.setDepositStatus(Constants.EMPTY_STRING);
				cheque.setDepositDate(Constants.EMPTY_STRING);
				cheque.setBankName(bankName);
				cheque.setBranch(branch);
				cheque.setDrawerAccountNumber(drawerAccountNumber);
				Utilities.primaryBeanSetter(cheque, systemUser);
				cheques.add(cheque);
			}
			return cheques;
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<Cheque>();
		}
	}

	private List<Image> getImages(SystemUser systemUser, JSONArray imageDetails, Collection collection)
			throws JSONException {

		JSONObject imageDetail = new JSONObject();
		Image image = null;
		String imagePath = null;

		List<Image> images = new ArrayList<Image>();

		for (int index = 0; index < imageDetails.length(); index++) {

			imageDetail = (JSONObject) imageDetails.get(index);

			if (!imageDetail.has(JsonConstants.RequestData.IMAGE)) {

				return new ArrayList<Image>();
			}

			String imageByteArray = (String) imageDetail.get(JsonConstants.RequestData.IMAGE);
			if (imageByteArray.isEmpty()) {

				return new ArrayList<Image>();
			}
			image = new Image();

			imagePath = (extractImagePath(collection, imageByteArray, Constants.IMAGE_FILE_PATH,
					(String.valueOf(index))));

			if (imagePath.equals(JsonConstants.ERROR)) {

				return null;
			} else {

				image = new Image();
				image.setPath(imagePath);
				Utilities.primaryBeanSetter(image, systemUser);
				images.add(image);

			}
		}
		return images;
	}

	private String extractImagePath(Collection collection, String type, String entity, String index) {
		try {

			String fileName = collection.getCaseId() + Constants.SYMBOL_UNDERSCORE + collection.getReceiptNumber()
					+ Constants.SYMBOL_UNDERSCORE + System.currentTimeMillis();

			String filePath = Constants.EMPTY_STRING;

			if (index.equals(Constants.EMPTY_STRING)) {

				filePath = Utilities.generateFilePath((String) applicationConfiguration.getValue(entity), fileName);
			} else {

				filePath = Utilities.generateFilePath((String) applicationConfiguration.getValue(entity),
						(fileName + "_" + index));
			}

			if (Utilities.writeImage(filePath, type)) {

				return filePath;
			} else {
				return (JsonConstants.ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return (JsonConstants.ERROR);
		}
	}

	private void sendCollectionsSms(Collection collection, SystemUser user) {

		log.info("------- IN Integration , Before Sending SMS  --------");
		try {
			/*
			 * if (collection.getMobileNumber() != null &&
			 * !collection.getMobileNumber().equalsIgnoreCase(Constants.EMPTY_STRING)) {
			 * log.info("Sending sms to customer mobile number thorugh Integration");
			 * 
			 * callSMSDispatcher(collection.getAppropriateAmount() + Constants.EMPTY_STRING,
			 * collection.getReceiptNumber(), collection.getPaymentMode(),
			 * collection.getMobileNumber(), collection.getAppl(),
			 * collection.getBusinessPartnerNumber(), user, communicationActivityService,
			 * collection); }
			 * 
			 * if (collection.getMobileNumberNew() != null &&
			 * !collection.getMobileNumberNew().equalsIgnoreCase(Constants.EMPTY_STRING) &&
			 * !collection.getMobileNumberNew().equalsIgnoreCase(collection.getMobileNumber(
			 * ))) { log.
			 * info("Sending sms to customer alternate mobile number through Integration");
			 * 
			 * callSMSDispatcher(collection.getAppropriateAmount() + Constants.EMPTY_STRING,
			 * collection.getReceiptNumber(), collection.getPaymentMode(),
			 * collection.getMobileNumberNew(), collection.getAppl(),
			 * collection.getBusinessPartnerNumber(), collection.getUser(),
			 * communicationActivityService, collection); }
			 * 
			 * if (collection.getUser().getMobileNumber() != null &&
			 * !collection.getUser().getMobileNumber().equalsIgnoreCase(Constants.
			 * EMPTY_STRING)) {
			 * log.info("Sending sms to FE mobile number through Integration");
			 * 
			 * generateSMSDispatcherMapForFE(collection.getAppropriateAmount() +
			 * Constants.EMPTY_STRING, collection.getReceiptNumber(),
			 * collection.getPaymentMode(), collection.getUser().getMobileNumber(),
			 * collection.getAppl(), collection.getPartyName(), collection.getUser(),
			 * communicationActivityService, collection);
			 * 
			 * }
			 * 
			 * 
			 * if (collection.getUser().getSupervisorMobileNumber() != null &&
			 * !collection.getUser().getSupervisorMobileNumber()
			 * .equalsIgnoreCase(Constants.EMPTY_STRING)) { log.info(
			 * "Sending sms to Supervisor mobile number through Integration");
			 * 
			 * generateSMSDispatcherMapForFE(collection.getAppropriateAmount() +
			 * Constants.EMPTY_STRING, collection.getReceiptNumber(),
			 * collection.getPaymentMode(), collection.getUser()
			 * .getSupervisorMobileNumber(), collection.getAppl(),
			 * collection.getPartyName(), collection.getUser(),
			 * communicationActivityService, collection);
			 * 
			 * }
			 * 
			 */
			String webserviceUrl = Constants.EMPTY_STRING;
			webserviceUrl = (String) applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");

			/* Collections acknowledgment SMS */
			Map<String, Object> parametesMap = new HashMap<String, Object>();
			Map<String, Object> parametersMaps = new HashMap<String, Object>();
			Map<String, Object> smsDispatcherMap = new HashMap<String, Object>();
			try {

				smsDispatcherMap = SmsFormXML.generateCollectionSmsXml(parametersMaps, collection);
			} catch (Exception e) {
				log.info("Exception :- " + e);
			}

			/*
			 * StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true,
			 * new HashMap<String, String>());
			 */
			Map<String, Object> createUserParamMap = new HashMap<String, Object>();

			String url = (String) (applicationConfiguration.getValue(Constants.smsParam.SMS_EMAIL_URL) == null
					? Constants.EMPTY_STRING
					: applicationConfiguration.getValue(Constants.smsParam.SMS_EMAIL_URL));
			createUserParamMap.put(Constants.LdapParam.LDAP_URL, url);
			createUserParamMap.put(Constants.LdapParam.LDAPREQUEST,
					smsDispatcherMap.get(Constants.smsParam.REQUESTHEDER) == null ? Constants.EMPTY_STRING
							: smsDispatcherMap.get(Constants.smsParam.REQUESTHEDER));

			try {
				String responseXml = Utilities.postXML(createUserParamMap);
				String smsEmailType = parametersMaps.get(Constants.SMSEMAILTYPE) == null ? Constants.EMPTY_STRING
						: (String) parametersMaps.get(Constants.SMSEMAILTYPE);

				parametesMap.put(Constants.REQUEST,
						createUserParamMap == null ? Constants.EMPTY_STRING : createUserParamMap);
				parametesMap.put(Constants.RESPONSE, responseXml == null ? Constants.EMPTY_STRING : responseXml);
				parametesMap.put(Constants.SMSEMAILURL, url);
				parametesMap.put(Constants.SMSEMAILTYPE, Constants.SMS);

				systemUserService.getInsertUpdateSmsEmailActivity(parametesMap, user, communicationActivityService,
						collection);

				log.info("----- responseXml : -------" + responseXml);
			} catch (Exception e) {
				log.info("Response :- " + e);
			}

		} catch (Exception e) {
			log.info("There is some error occured while sending sms to customer.In Integration" + e);
		}

		if (!StringUtils.isEmpty(collection.getMobileNumberNew())) {
			try {
				String webserviceUrl = Constants.EMPTY_STRING;
				webserviceUrl = (String) applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");

				/* Collections acknowledgment SMS */
				Map<String, Object> parametesMap = new HashMap<String, Object>();
				Map<String, Object> parametersMaps = new HashMap<String, Object>();
				Map<String, Object> smsDispatcherMap = new HashMap<String, Object>();
				try {

					smsDispatcherMap = SmsFormXML.generateCollectionAlterMobiSmsXml(parametersMaps, collection);
				} catch (Exception e) {
					log.info("Exception :- " + e);
				}

				/*
				 * StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true,
				 * new HashMap<String, String>());
				 */
				Map<String, Object> createUserParamMap = new HashMap<String, Object>();

				String url = (String) (applicationConfiguration.getValue(Constants.smsParam.SMS_EMAIL_URL) == null
						? Constants.EMPTY_STRING
						: applicationConfiguration.getValue(Constants.smsParam.SMS_EMAIL_URL));
				createUserParamMap.put(Constants.LdapParam.LDAP_URL, url);
				createUserParamMap.put(Constants.LdapParam.LDAPREQUEST,
						smsDispatcherMap.get(Constants.smsParam.REQUESTHEDER) == null ? Constants.EMPTY_STRING
								: smsDispatcherMap.get(Constants.smsParam.REQUESTHEDER));

				try {
					String responseXml = Utilities.postXML(createUserParamMap);
					String smsEmailType = parametersMaps.get(Constants.SMSEMAILTYPE) == null ? Constants.EMPTY_STRING
							: (String) parametersMaps.get(Constants.SMSEMAILTYPE);

					parametesMap.put(Constants.REQUEST,
							createUserParamMap == null ? Constants.EMPTY_STRING : createUserParamMap);
					parametesMap.put(Constants.RESPONSE, responseXml == null ? Constants.EMPTY_STRING : responseXml);
					parametesMap.put(Constants.SMSEMAILURL, url);
					parametesMap.put(Constants.SMSEMAILTYPE, Constants.SMS);

					systemUserService.getInsertUpdateSmsEmailActivity(parametesMap, user, communicationActivityService,
							collection);

					log.info("----- responseXml : -------" + responseXml);
				} catch (Exception e) {
					log.info("Response :- " + e);
				}
			} catch (Exception e) {
				log.info("Response :- " + e);
			}
		}

	}

	private void callSMSDispatcher(String amount, String receiptNumber, String paymentType, String mobileNumber,
			String type, String apacCardNumber, SystemUser user,
			CommunicationActivityService communicationActivityService, Collection collection) {
		log.info("---- Inside callSMSDispatcher --------");

		String webserviceUrl = Constants.EMPTY_STRING;

		if (type.equalsIgnoreCase("RSM")) {

			webserviceUrl = (String) applicationConfiguration.getValue("RSM_WEB_SERVICE_URL_SMS_DISPATCHER");
		}

		else {

			webserviceUrl = (String) applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");

		}

		/* Collections acknowledgment SMS */

		Map<String, Object> smsDispatcherMap;

		if (paymentType.equalsIgnoreCase("ORI")) {
			smsDispatcherMap = ServerUtilities.generateSMSDispatcherMapForDebit(amount, receiptNumber, paymentType,
					mobileNumber, type, apacCardNumber, collection);
		} else {
			smsDispatcherMap = ServerUtilities.generateSMSDispatcherMap(amount, receiptNumber, paymentType,
					mobileNumber, type, apacCardNumber);
		}

		StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true, new HashMap<String, String>());

		log.info("----- xmlRequest : -------" + xmlRequest);

		/*
		 * CommunicationActivityAddition communicationActivityAddition = new
		 * CommunicationActivityAddition(user .getUserTableId().toString(),
		 * user.getImeiNo(), (type + "_" + collection.getCollectionType()),
		 * webserviceUrl, xmlRequest.toString(), communicationActivityService,
		 * ActivityLoggerConstants.DATABASE_MSSQL);
		 * 
		 * new Thread(communicationActivityAddition).run();
		 * 
		 * KotakCollectionWebserviceAdapter kotakCollectionWebserviceAdapter = new
		 * KotakCollectionWebserviceAdapter();
		 * 
		 * String xmlResponse =
		 * kotakCollectionWebserviceAdapter.callWebserviceAndGetXmlString(xmlRequest.
		 * toString(), webserviceUrl);
		 * 
		 * CommunicationActivity communicationActivity =
		 * communicationActivityAddition.extractCommunicationActivity();
		 * 
		 * Map<String, Object> result = null;
		 * 
		 * if (null != xmlResponse && !xmlResponse.equals(Constants.EMPTY_STRING)) {
		 * communicationActivity.setResponse(xmlResponse);
		 * 
		 * CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new
		 * CommunicationActivityStatusUpdate( communicationActivity,
		 * (ActivityLoggerConstants.STATUS_SUCCESS), communicationActivityService);
		 * 
		 * new Thread(communicationActivityStatusUpdate).run();
		 * 
		 * result = XMLToMap.convertXMLToMap(xmlResponse);
		 * 
		 * log.info("----- Result of SMS Dispatch : -------" + result); } else {
		 * communicationActivity.setResponse(ActivityLoggerConstants.EMPTY_STRING);
		 * 
		 * CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new
		 * CommunicationActivityStatusUpdate( communicationActivity,
		 * (ActivityLoggerConstants.STATUS_FAILURE), communicationActivityService);
		 * 
		 * new Thread(communicationActivityStatusUpdate).run();
		 * 
		 * log.info("----- Failure in sending SMS : -------"); }
		 */
		/* OLD Denomination acknowledgment SMS */

		/*
		 * if (collection.getPaymentMode().equalsIgnoreCase("CSH")) {
		 * 
		 * smsDispatcherMap =
		 * ServerUtilities.generateSMSForOldDenominination(receiptNumber, mobileNumber,
		 * collection);
		 * 
		 * xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true, new
		 * HashMap<String, String>());
		 * 
		 * log.info("----- xmlRequest for Old Denomination SMS: -------" + xmlRequest);
		 * 
		 * communicationActivityAddition = new
		 * CommunicationActivityAddition(user.getUserTableId().toString(),
		 * user.getImeiNo(), (type + "_" + collection.getCollectionType()),
		 * webserviceUrl, xmlRequest.toString(), communicationActivityService,
		 * ActivityLoggerConstants.DATABASE_MSSQL);
		 * 
		 * new Thread(communicationActivityAddition).run();
		 * 
		 * kotakCollectionWebserviceAdapter = new KotakCollectionWebserviceAdapter();
		 * 
		 * xmlResponse =
		 * kotakCollectionWebserviceAdapter.callWebserviceAndGetXmlString(xmlRequest.
		 * toString(), webserviceUrl);
		 * 
		 * communicationActivity =
		 * communicationActivityAddition.extractCommunicationActivity();
		 * 
		 * if (null != xmlResponse && !xmlResponse.equals(Constants.EMPTY_STRING)) {
		 * communicationActivity.setResponse(xmlResponse);
		 * 
		 * CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new
		 * CommunicationActivityStatusUpdate( communicationActivity,
		 * (ActivityLoggerConstants.STATUS_SUCCESS), communicationActivityService);
		 * 
		 * new Thread(communicationActivityStatusUpdate).run();
		 * 
		 * result = XMLToMap.convertXMLToMap(xmlResponse);
		 * 
		 * log.info("----- Result of Old Denomination SMS Dispatch : -------" + result);
		 * } else {
		 * communicationActivity.setResponse(ActivityLoggerConstants.EMPTY_STRING);
		 * 
		 * CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new
		 * CommunicationActivityStatusUpdate( communicationActivity,
		 * (ActivityLoggerConstants.STATUS_FAILURE), communicationActivityService);
		 * 
		 * new Thread(communicationActivityStatusUpdate).run();
		 * 
		 * log.info("----- Failure in sending Old Denomination SMS : -------"); } }
		 */
	}

	private void generateSMSDispatcherMapForFE(String amount, String receiptNumber, String paymentType,
			String mobileNumber, String type, String feName, SystemUser user,
			CommunicationActivityService communicationActivityService, Collection collection) {
		log.info("---- Inside generateSMSDispatcherMapForFE --------");

		String webserviceUrl = (String) applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");

		Map<String, Object> smsDispatcherMap = ServerUtilities.generateSMSDispatcherMapForFE(amount, receiptNumber,
				paymentType, mobileNumber, type, feName);

		StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true, new HashMap<String, String>());

		log.info("----- xmlRequest : -------" + xmlRequest);

		CommunicationActivityAddition communicationActivityAddition = new CommunicationActivityAddition(
				user.getUserTableId().toString(), user.getImeiNo(), (type + "_" + collection.getCollectionType()),
				webserviceUrl, xmlRequest.toString(), communicationActivityService,
				ActivityLoggerConstants.DATABASE_MSSQL);

		new Thread(communicationActivityAddition).run();

		KotakCollectionWebserviceAdapter kotakCollectionWebserviceAdapter = new KotakCollectionWebserviceAdapter();

		String xmlResponse = kotakCollectionWebserviceAdapter.callWebserviceAndGetXmlString(xmlRequest.toString(),
				webserviceUrl);

		CommunicationActivity communicationActivity = communicationActivityAddition.extractCommunicationActivity();

		Map<String, Object> result = null;

		if (null != xmlResponse && !xmlResponse.equals(Constants.EMPTY_STRING)) {
			communicationActivity.setResponse(xmlResponse);

			CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
					communicationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), communicationActivityService);

			new Thread(communicationActivityStatusUpdate).run();

			result = XMLToMap.convertXMLToMap(xmlResponse);

			log.info("----- Result of SMS Dispatch : -------" + result);
		} else {
			communicationActivity.setResponse(ActivityLoggerConstants.EMPTY_STRING);

			CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
					communicationActivity, (ActivityLoggerConstants.STATUS_FAILURE), communicationActivityService);

			new Thread(communicationActivityStatusUpdate).run();

			log.info("----- Failure in sending SMS : -------");
		}
	}

	private void callEmailService(Collection collection) {
		try {
			if (collection.getEmailAddress().equals(Constants.EMPTY_STRING)
					&& collection.getEmailAddressNew().equals(Constants.EMPTY_STRING)) {
				log.info(" -------- No Email Address found for Collection -------- ");
			} else {
				log.info("--- Sending Email ---");

				String payMode = collection.getPaymentMode();
				log.info(" -------- payMode -------- " + payMode);

				if (payMode.equals(Constants.PAYMENT_MODE_CASH)) {
					sendEmailForCashPayment(collection);
				}

				if (payMode.equals(Constants.PAYMENT_MODE_CHEQUE)) {
					sendEmailForChequePayment(collection);
				}

				if (payMode.equalsIgnoreCase(Constants.PAYMENT_MODE_PDC)
						|| payMode.equalsIgnoreCase(Constants.PAYMENT_MODE_DRAFT)) {
					sendEmailForDDPDC(collection);
				}
			}
		} catch (Exception e) {
			log.info("-------Error Occured in sending Email---------", e);
			e.printStackTrace();
		}
	}

	private void sendEmailForCashPayment(Collection collection) throws ParseException {
		log.info("---inside sendEmailForCashPayment---");

		String paymentDate = collection.getDeviceDate();
		log.info("---payment date---" + paymentDate);

		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat format2 = new SimpleDateFormat("dd-MMM-yyyy");
		Date date = format1.parse(paymentDate);
		paymentDate = format2.format(date);

		log.info("---payment date after parsing---" + paymentDate);

// String email = collection.getEmailAddress();

		String emailText = Constants.EMPTY_STRING;

		if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD)) {
			log.info("---inside if condition---");
			log.info("----email text----" + simpleMailMessageForCashPaymentCreditCard.getText());
			emailText = String.format(simpleMailMessageForCashPaymentCreditCard.getText(), collection.getName(),
					getFullFormApplType(collection.getAppl()), collection.getBusinessPartnerNumber(),
					collection.getReceiptNumber(), collection.getAppropriateAmount() + Constants.EMPTY_STRING,
					Constants.EMPTY_STRING + paymentDate, collection.getBusinessPartnerNumber(),
					getTollFreeNumberForAppl(collection.getAppl()));
			log.info("----emailTest for card ----" + emailText);

		} else {
			log.info("--- inside else----");
			log.info("--- simpleMailMessageForCashPaymentLoan.getText() ----"
					+ simpleMailMessageForCashPaymentLoan.getText());
			log.info("---- collection.getName()-----" + collection.getName());
			log.info("---- getTollFreeNumberForAppl-----" + getTollFreeNumberForAppl(collection.getAppl()));

			emailText = String.format(simpleMailMessageForCashPaymentLoan.getText(), collection.getName(),
					getFullFormApplType(collection.getAppl()), collection.getBusinessPartnerNumber(),
					collection.getReceiptNumber(), collection.getAppropriateAmount() + Constants.EMPTY_STRING,
					Constants.EMPTY_STRING + paymentDate, collection.getBusinessPartnerNumber(),
					getTollFreeNumberForAppl(collection.getAppl()));
			log.info("---- email text is -----" + emailText);

		}

		String email = collection.getEmailAddress() != null ? collection.getEmailAddress() : Constants.EMPTY_STRING;

		log.info("---email----" + email);

		if (collection.getEmailAddress() != null
				&& !collection.getEmailAddress().equalsIgnoreCase(Constants.EMPTY_STRING)) {
			email = collection.getEmailAddress();
			log.info("---inside if email----" + email);

			if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD)) {
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForCashPaymentCreditCard.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(
						collection.getUser().getUserTableId().toString(),
						ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList, receiverList,
						simpleMailMessageForCashPaymentCreditCard.getSubject(), emailText, notificationActivityService,
						ActivityLoggerConstants.DATABASE_MSSQL);
				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(email, simpleMailMessageForCashPaymentCreditCard.getFrom(),
						simpleMailMessageForCashPaymentCreditCard.getSubject(), emailText)) {
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS),
							notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				} else {
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE),
							notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			} else {
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForCashPaymentLoan.getFrom());
				log.info("simpleMailMessageForCashPaymentLoan.getFrom()"
						+ simpleMailMessageForCashPaymentLoan.getFrom());

				log.info("adding string data into reciverList" + email);
				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				log.info("collection.getUser().getUserTableId()" + collection.getUser());
				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(
						collection.getUser().getUserTableId().toString(),
						ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList, receiverList,
						simpleMailMessageForCashPaymentLoan.getSubject(), emailText, notificationActivityService,
						ActivityLoggerConstants.DATABASE_MSSQL);

				log.info("notificationActivityAddition" + notificationActivityAddition);
				new Thread(notificationActivityAddition).run();

				if (emailService == null) {
					log.info("emailService is null");
				} else {
					log.info("emailService is not null");
					log.info("email is : " + email);
				}
				if (emailService.sendMail(email, simpleMailMessageForCashPaymentLoan.getFrom(),
						simpleMailMessageForCashPaymentLoan.getSubject(), emailText)) {
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS),
							notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				} else {
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE),
							notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			}
		}

		if (collection.getEmailAddressNew() != null
				&& !collection.getEmailAddressNew().equalsIgnoreCase(Constants.EMPTY_STRING)
				&& !collection.getEmailAddressNew().equalsIgnoreCase(email)) {
			email = collection.getEmailAddressNew();
			if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD)) {
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForCashPaymentCreditCard.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(
						collection.getUser().getUserTableId().toString(),
						ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList, receiverList,
						simpleMailMessageForCashPaymentCreditCard.getSubject(), emailText, notificationActivityService,
						ActivityLoggerConstants.DATABASE_MSSQL);
				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(collection.getEmailAddressNew(),
						simpleMailMessageForCashPaymentCreditCard.getFrom(),
						simpleMailMessageForCashPaymentCreditCard.getSubject(), emailText)) {
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS),
							notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				} else {
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE),
							notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			} else {
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForCashPaymentLoan.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(
						collection.getUser().getUserTableId().toString(),
						ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList, receiverList,
						simpleMailMessageForCashPaymentLoan.getSubject(), emailText, notificationActivityService,
						ActivityLoggerConstants.DATABASE_MSSQL);
				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(collection.getEmailAddressNew(),
						simpleMailMessageForCashPaymentLoan.getFrom(), simpleMailMessageForCashPaymentLoan.getSubject(),
						emailText)) {
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS),
							notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				} else {
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE),
							notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			}
		}
	}

	private void sendEmailForChequePayment(Collection collection) throws ParseException {
		String paymentDate = collection.getDeviceDate();
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat format2 = new SimpleDateFormat("dd-MMM-yyyy");
		Date date = format1.parse(paymentDate);
		paymentDate = format2.format(date);

		String email = collection.getEmailAddress() != null ? collection.getEmailAddress() : Constants.EMPTY_STRING;
		;
		String chequeDetailString = Constants.EMPTY_STRING;

		NumberFormat formatter = NumberFormat.getInstance(new Locale("en", "IN"));

		for (Cheque cheque : collection.getChequeDetails()) {
			chequeDetailString = chequeDetailString + " Cheque No." + cheque.getChequeNo() + "     dated"
					+ cheque.getChequeDate();
		}

		String emailText = Constants.EMPTY_STRING;
		if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD)) {
			emailText = String.format(simpleMailMessageForChequePaymentCreditCard.getText(), collection.getName(),
					getFullFormApplType(collection.getAppl()), collection.getBusinessPartnerNumber(),
					collection.getReceiptNumber(), collection.getAppropriateAmount() + Constants.EMPTY_STRING,
					Constants.EMPTY_STRING + paymentDate, Constants.EMPTY_STRING + chequeDetailString,
					collection.getBusinessPartnerNumber(), getTollFreeNumberForAppl(collection.getAppl()));
		} else {
			emailText = String.format(simpleMailMessageForChequePaymentLoan.getText(), collection.getName(),
					getFullFormApplType(collection.getAppl()), collection.getBusinessPartnerNumber(),
					collection.getReceiptNumber(), collection.getAppropriateAmount() + Constants.EMPTY_STRING,
					Constants.EMPTY_STRING + paymentDate, Constants.EMPTY_STRING + chequeDetailString,
					collection.getBusinessPartnerNumber(), getTollFreeNumberForAppl(collection.getAppl()));
		}

		if (collection.getEmailAddress() != null
				&& !collection.getEmailAddress().equalsIgnoreCase(Constants.EMPTY_STRING)) {
			if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD)) {
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForChequePaymentCreditCard.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(
						collection.getUser().getUserTableId().toString(),
						ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList, receiverList,
						simpleMailMessageForChequePaymentCreditCard.getSubject(), emailText,
						notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);
				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(email, simpleMailMessageForChequePaymentCreditCard.getFrom(),
						simpleMailMessageForChequePaymentCreditCard.getSubject(), emailText)) {
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS),
							notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				} else {
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE),
							notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			} else {
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForChequePaymentLoan.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(
						collection.getUser().getUserTableId().toString(),
						ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList, receiverList,
						simpleMailMessageForChequePaymentLoan.getSubject(), emailText, notificationActivityService,
						ActivityLoggerConstants.DATABASE_MSSQL);
				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(email, simpleMailMessageForChequePaymentLoan.getFrom(),
						simpleMailMessageForChequePaymentLoan.getSubject(), emailText)) {
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS),
							notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				} else {
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE),
							notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			}
		}

		if (collection.getEmailAddressNew() != null
				&& !collection.getEmailAddressNew().equalsIgnoreCase(Constants.EMPTY_STRING)
				&& !collection.getEmailAddressNew().equalsIgnoreCase(email)) {
			if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD)) {
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForChequePaymentCreditCard.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(
						collection.getUser().getUserTableId().toString(),
						ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList, receiverList,
						simpleMailMessageForChequePaymentCreditCard.getSubject(), emailText,
						notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);
				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(collection.getEmailAddressNew(),
						simpleMailMessageForChequePaymentCreditCard.getFrom(),
						simpleMailMessageForChequePaymentCreditCard.getSubject(), emailText)) {
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS),
							notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				} else {
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE),
							notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			} else {
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForChequePaymentLoan.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(
						collection.getUser().getUserTableId().toString(),
						ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList, receiverList,
						simpleMailMessageForChequePaymentLoan.getSubject(), emailText, notificationActivityService,
						ActivityLoggerConstants.DATABASE_MSSQL);
				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(collection.getEmailAddressNew(),
						simpleMailMessageForChequePaymentLoan.getFrom(),
						simpleMailMessageForChequePaymentLoan.getSubject(), emailText)) {
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS),
							notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				} else {
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE),
							notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			}
		}
	}

	private void sendEmailForDDPDC(Collection collection) throws ParseException {
		String paymentDate = collection.getDeviceDate();
		String collectionDate = collection.getDeviceDate();

		Date emailDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat emailDateFormat = new SimpleDateFormat("ddMMMyyyy");
		emailDate = (dateFormat.parse(collectionDate));

		String email = collection.getEmailAddress().equals(Constants.EMPTY_STRING) ? collection.getEmailAddress()
				: collection.getEmailAddress();

		String chequeDetailString = Constants.EMPTY_STRING;

		NumberFormat formatter = NumberFormat.getInstance(new Locale("en", "IN"));

		String emailAmount = collection.getAppropriateAmount() + Constants.EMPTY_STRING;

		DecimalFormat amountFormat = new DecimalFormat("#.00");

		try {
			emailAmount = amountFormat.format(Double.parseDouble(emailAmount));
		} catch (Exception e) {
			emailAmount = "0.00";
		}

		for (Cheque cheque : collection.getChequeDetails()) {
			if (collection.getPaymentMode().equalsIgnoreCase(Constants.PAYMENT_MODE_DRAFT)) {
				chequeDetailString = chequeDetailString + "<br/>Demand Draft No." + cheque.getChequeNo() + "     Dated "
						+ new SimpleDateFormat("ddMMMyyyy")
								.format(new SimpleDateFormat("yyyy-MM-dd").parse(cheque.getChequeDate()));
			} else {
				chequeDetailString = chequeDetailString + "<br/> PDC No." + cheque.getChequeNo() + "     Dated "
						+ new SimpleDateFormat("ddMMMyyyy")
								.format(new SimpleDateFormat("yyyy-MM-dd").parse(cheque.getChequeDate()));
			}

		}

		String emailText = Constants.EMPTY_STRING;

		if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD)) {
			if (collection.getPaymentMode().equalsIgnoreCase(Constants.PAYMENT_MODE_DRAFT)) {
				emailText = String.format(simpleMailMessageForDDPDC.getText(), "Credit Card",
						"-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber(),
						collection.getReceiptNumber(), "Demand Draft", emailAmount, emailDateFormat.format(emailDate),
						chequeDetailString, "-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber());
			} else {
				emailText = String.format(simpleMailMessageForDDPDC.getText(), "Credit Card",
						"-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber(),
						collection.getReceiptNumber(), "PDC", emailAmount, emailDateFormat.format(emailDate),
						chequeDetailString, "-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber());
			}
		} else {
			if (collection.getPaymentMode().equalsIgnoreCase(Constants.PAYMENT_MODE_DRAFT)) {
				emailText = String.format(simpleMailMessageForDDPDC.getText(), "Personal Finance Loan ",
						"-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber(),
						collection.getReceiptNumber(), "Demand Draft", emailAmount, emailDateFormat.format(emailDate),
						chequeDetailString, "-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber());
			} else {
				emailText = String.format(simpleMailMessageForDDPDC.getText(), "Personal Finance Loan ",
						"-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber(),
						collection.getReceiptNumber(), "PDC", emailAmount, emailDateFormat.format(emailDate),
						chequeDetailString, "-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber());
			}
		}

		if (collection.getEmailAddress() != null
				&& !collection.getEmailAddress().equalsIgnoreCase(Constants.EMPTY_STRING)) {
			if (!collection.getAppl().isEmpty()) {

				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForDDPDC.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(
						collection.getUser().getUserTableId().toString(),
						ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList, receiverList,
						simpleMailMessageForDDPDC.getSubject(), emailText, notificationActivityService,
						ActivityLoggerConstants.DATABASE_MSSQL);

				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(email, simpleMailMessageForDDPDC.getFrom(),
						simpleMailMessageForDDPDC.getSubject(), emailText)) {
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS),
							notificationActivityService);

					new Thread(notificationActivityStatusUpdate).run();
				} else {
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE),
							notificationActivityService);

					new Thread(notificationActivityStatusUpdate).run();
				}

			} else {
				log.info("----- Improper Information to Send Email");
			}
		}

		if (collection.getEmailAddressNew() != null
				&& !collection.getEmailAddressNew().equalsIgnoreCase(Constants.EMPTY_STRING)
				&& !collection.getEmailAddressNew().equalsIgnoreCase(email)) {
			if (!collection.getAppl().isEmpty()) {
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForCashPaymentCreditCard.getFrom());

				List<String> receiverList = new ArrayList<String>();

				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(
						collection.getUser().getUserTableId().toString(),
						ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList, receiverList,
						simpleMailMessageForDDPDC.getSubject(), emailText, notificationActivityService,
						ActivityLoggerConstants.DATABASE_MSSQL);
				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(collection.getEmailAddressNew(), simpleMailMessageForDDPDC.getFrom(),
						simpleMailMessageForDDPDC.getSubject(), emailText)) {
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS),
							notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				} else {
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE),
							notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			} else {
				log.info("----- Improper Information to Send Email");
			}
		}
	}

	private String getFullFormApplType(String appl) {
		if (appl.equalsIgnoreCase(Constants.APPL_CARD)) {
			return "Credit Card";
		} else if (appl.equalsIgnoreCase("SPLN")) {
			return "Salaried Personal Loans-New";
		} else if (appl.equalsIgnoreCase("RAR")) {
			return "Retail Asset Reconstruction";
		} else if (appl.equalsIgnoreCase("CV")) {
			return "Commercial Vehicles";
		} else if (appl.equalsIgnoreCase("HF")) {
			return "Home Finance";
		} else if (appl.equalsIgnoreCase("CSG")) {
			return "Personal Finance";
		} else if (appl.equalsIgnoreCase("SPL")) {
			return "Salaried Personal Loans";
		} else if (appl.equalsIgnoreCase("SA")) {
			return "UNNATI [SARAL]";
		} else if (appl.equalsIgnoreCase("TFE")) {
			return "Tractor and Farm Equipment Loans";
		} else if (appl.equalsIgnoreCase("CE")) {
			return "Construction Equipment";
		} else if (appl.equalsIgnoreCase("LAP")) {
			return "Loan Against Property";
		} else if (appl.equalsIgnoreCase("SBG")) {
			return "Strategic Business Group";
		} else if (appl.equalsIgnoreCase("GLN")) {
			return "Gold Loan";
		} else if (appl.equalsIgnoreCase("LCV")) {
			return "Light Commercial Vehicles";
		} else if (appl.equalsIgnoreCase("RHB")) {
			return "Rural Housing Business";
		} else if (appl.equalsIgnoreCase("RARF")) {
			return "Retail ARD Funding";
		} else if (appl.equalsIgnoreCase("CLF")) {
			return "Car Lease Finance";
		} else if (appl.equalsIgnoreCase("CF")) {
			return "Car Finance";
		} else {
			return appl;
		}
	}

	private String getTollFreeNumberForAppl(String appl) {
		if (appl.equalsIgnoreCase(Constants.APPL_CARD) || appl.equalsIgnoreCase("HF") || appl.equalsIgnoreCase("LAP")
				|| appl.equalsIgnoreCase("SPL") || appl.equalsIgnoreCase("SPLN") || appl.equalsIgnoreCase("CSG")) {
			return "1800 102 6022";
		} else if (appl.equalsIgnoreCase("CV") || appl.equalsIgnoreCase("CE") || appl.equalsIgnoreCase("SA")
				|| appl.equalsIgnoreCase("TFE") || appl.equalsIgnoreCase("LCV") || appl.equalsIgnoreCase("GLN")) {
			return "1800 209 5600";
		} else if (appl.equalsIgnoreCase("RAR")) {
			return "1800 120 9820";
		} else if (appl.equalsIgnoreCase("CF") || appl.equalsIgnoreCase("CLF")) {
			return "1800 209 5732";
		} else {
			return Constants.EMPTY_STRING;
		}
	}

	public String GenerateChecksumValue(Map<String, String> dataMap, String confDesc, String skey) throws Exception {

		log.info("---inside payement request-----");

		MerchantCollect mrc = new MerchantCollect();
		DeviceDetails deviceDetails = new DeviceDetails();

		deviceDetails.setApp(JsonConstants.UpiConstant.HARDCODE_VALUE_OF_APP);
		deviceDetails.setCapability(Constants.EMPTY_STRING);
		deviceDetails.setGcmid(Constants.EMPTY_STRING);
		deviceDetails.setGeocode(Constants.EMPTY_STRING);
		deviceDetails.setId(Constants.EMPTY_STRING);
		deviceDetails.setIp(Constants.EMPTY_STRING);
		deviceDetails.setLocation(Constants.EMPTY_STRING);
		deviceDetails.setMobile(JsonConstants.UpiConstant.HARDCODE_VALUE_OF_CUSTOMER_ID);
		deviceDetails.setOs(Constants.EMPTY_STRING);
		deviceDetails.setType(Constants.EMPTY_STRING);

		mrc.setAggregatorVPA(JsonConstants.UpiConstant.HARDCODE_VALUE_OF_AGGREGATOR_VPA);
		mrc.setAmount(dataMap.get("amount"));
		mrc.setCustomerId(JsonConstants.UpiConstant.HARDCODE_VALUE_OF_CUSTOMER_ID);
		mrc.setDeviceDetails(deviceDetails);
		mrc.setExpiry(confDesc);
		mrc.setMerchantReferenceCode(JsonConstants.UpiConstant.HARDCODE_VALUE_OF_MERCHANT_REFERENCE_CODE);
		mrc.setOrderId(dataMap.get("receiptNumber"));
		mrc.setPayerVpa(dataMap.get("vpaAddress"));
		mrc.setReferenceId(Constants.EMPTY_STRING);
		mrc.setRemarks(dataMap.get("appl") + dataMap.get("apacNumberValue"));
		mrc.setSubmerchantReferenceid(Constants.EMPTY_STRING);
		mrc.setSubmerchantVPA(Constants.EMPTY_STRING);
		mrc.setTimeStamp(dataMap.get("timestampForUPI"));
		mrc.setTxnId(dataMap.get("GenerateUUIDValue"));

		log.info("mrc.getInput() :: " + mrc.getInput());

		byte[] digest = Crypto.SHA256(mrc.getInput());

		byte[] encData = Crypto.encrypt(Crypto.hexStringToByteArray(skey), digest);

		System.out.println("encData in hex Crypto.bytesToHex(encData) :" + Crypto.bytesToHex(encData));

		String checkSumval = Base64.encodeBase64String(encData);

		return checkSumval;
	}

	public boolean requestPayment(String request) {

		boolean flag = false;

		JSONObject responseJSON = new JSONObject();

		try {

			log.info("-------requestSet inside return-----" + request);

			String requestEntity = JSONPayloadExtractor.extract(request, JsonConstants.ENTITY);
			String requestAction = JSONPayloadExtractor.extract(request, JsonConstants.ACTION);
			String requestType = JSONPayloadExtractor.extract(request, JsonConstants.TYPE);

			JSONObject jsonObject = new JSONObject(request);
			JSONObject user = (JSONObject) jsonObject.get(JsonConstants.SYSTEM_USER);
			SystemUser systemUserNew = ServerUtilities.extractSystemUser(user);

			String imeiNo = Constants.EMPTY_STRING;
			String draMobileNumber = Constants.EMPTY_STRING;
			Long userTableId = 0L;

			log.info("-------requestSet is-----" + request);
			log.info("-----requestEntity----" + requestEntity);
			log.info("-----requestAction----" + requestAction);
			log.info("-----requestType----" + requestType);
			log.info("---systemUserNew-----" + systemUserNew);
			log.info("---jsonObject-----" + jsonObject);

			UserActivityAddition userActivityAddition = new UserActivityAddition(request, userActivityService,
					ActivityLoggerConstants.DATABASE_MSSQL);

			new Thread(userActivityAddition).run();

			UserActivity userActivity = userActivityAddition.extractUserActivity();

			String fename = user.getString("firstLastName") == null ? Constants.EMPTY_STRING
					: user.getString("firstLastName");

			JSONObject data = (JSONObject) jsonObject.get(JsonConstants.DATA);
			log.info("data part of json-------------------" + data);

			String receiptNo = data.getString(Constants.RECEIPTNUM) == null ? Constants.EMPTY_STRING
					: data.getString(Constants.RECEIPTNUM);
			log.info("--receiptNo--" + receiptNo);

			String paymentMode = Constants.EMPTY_STRING;

			String partyMobNo = data.getString("partyMobNo") == null ? Constants.EMPTY_STRING
					: data.getString("partyMobNo");
			log.info("--partyMobNo--" + partyMobNo);

			String mobileNew = data.getString("mobileNew") == null ? Constants.EMPTY_STRING
					: data.getString("mobileNew");
			log.info("--mobileNew--" + mobileNew);

			String appl = data.getString(Constants.APPL) == null ? Constants.EMPTY_STRING
					: data.getString(Constants.APPL);
			log.info("--appl--" + appl);

			String amount = data.getString(Constants.AllPayCollectionsDao.AMOUNT_LOWERCASE) == null
					? Constants.EMPTY_STRING
					: data.getString(Constants.AllPayCollectionsDao.AMOUNT_LOWERCASE);
			log.info("--amount--" + amount);

			String vpa = data.getString(Constants.PAYERVPA) == null ? Constants.EMPTY_STRING
					: data.getString(Constants.PAYERVPA);
			log.info("--vpa--" + vpa);

			String txnId = data.getString(JsonConstants.TNX_ID) == null ? Constants.EMPTY_STRING
					: data.getString(JsonConstants.TNX_ID);
			log.info("--txnId--" + txnId);

			String unqNo = data.getString("unqNo") == null ? Constants.EMPTY_STRING : data.getString("unqNo");
			log.info("--unqNo--" + unqNo);

			Map<String, String> detailMap = new HashMap<String, String>();
			detailMap.put("appl", appl);
			detailMap.put("amount", amount);
			detailMap.put("receiptNumber", receiptNo);
			detailMap.put("apacNumberValue", unqNo);
			detailMap.put("vpaAddress", vpa);
			detailMap.put("GenerateUUIDValue", txnId);
			detailMap.put("timestampForUPI", Utilities.convertDate(new Timestamp(System.currentTimeMillis())));

			String configurationCode = "UNVAL";
			String confDesc = Constants.EMPTY_STRING;
			String confType = Constants.EMPTY_STRING;

			List<Map<String, Object>> rows = collectionService.getAllPaySMSValidity(configurationCode);

			for (Map row : rows) {
				confDesc = row.get(Constants.AllPayCollectionsDao.CONFIGURATION_DESCRIPTION) == null
						? Constants.EMPTY_STRING
						: row.get(Constants.AllPayCollectionsDao.CONFIGURATION_DESCRIPTION).toString();
				confType = row.get(Constants.AllPayCollectionsDao.CONFIGURATION_TYPE) == null ? Constants.EMPTY_STRING
						: row.get(Constants.AllPayCollectionsDao.CONFIGURATION_TYPE).toString();
			}

			String jsonRequestPaymentData = UpiUtility.generateUpiJsonReq(detailMap, confDesc);

			log.info("jsonRequestPaymentData" + jsonRequestPaymentData);

			String checkSumval = GenerateChecksumValue(detailMap, confDesc,
					"7A0D7DE6B5B0503A8044402B9653AB202887DD233378B9F3B4E72A71544B7AC0");
// String checkSumval = GenerateChecksumValue(detailMap, confDesc,
// "376A3CCD74D7B33D92E4D452112A1B686EC3D45926310AD3AEF3B554B19D0FEF");

			log.info("checkSumval in UPI   " + checkSumval);

// String jsonResponsePaymentData=
// UpiUtility.callWebserviceAndGetJsonString(jsonRequestPaymentData,checkSumval);

			String jsonResponsePaymentData = UpiUtility.callWebserviceAndGetJsonString(jsonRequestPaymentData,
					checkSumval, applicationConfiguration);

// String jsonResponsePaymentData="{ \"code\":\"00\", \"result\":\"Accepted
// Collect Request\", \"data\":{ \"orderId\":\"MB123456789456123\",
// \"referenceId\":\"825020132031\", \"payerVpa\":\"917208429868@kotak\",
// \"payerName\":null, \"txnId\":\"KMBMKCBG9c2baea9443c4e6aac1e096a33f\",
// \"aggregatorVPA\":\"kcbg@kotak\", \"expiry\":\"1800\", \"amount\":\"15.00\",
// \"timeStamp\":\"07-09-2018 19:22:04 \" } }";

			Map<Object, Object> updateActivityMap = new HashMap<Object, Object>();

			if (jsonResponsePaymentData != null && !jsonResponsePaymentData.isEmpty()) {

				Map<Object, Object> activityMap = new HashMap<Object, Object>();

				activityMap.put(Constants.AllPayCollectionsDao.REQUEST_TO_THIRD_PARTY, jsonRequestPaymentData);
				activityMap.put(Constants.AllPayCollectionsDao.THIRD_PARTY_STATUS, "PENDING");
				activityMap.put(Constants.AllPayCollectionsDao.DEVICE_REQUEST, request);
				activityMap.put(Constants.AllPayCollectionsDao.CREATED_BY, systemUserNew.getUserTableId().toString());
				activityMap.put(Constants.AllPayCollectionsDao.MODIFIED_BY, systemUserNew.getUserTableId().toString());
				activityMap.put(Constants.AllPayCollectionsDao.RECEIPT_NUMBER, receiptNo);
				activityMap.put(Constants.UPIDao.UPI_TRANS_ID, txnId);
				activityMap.put(Constants.UPIDao.AMOUNT, amount);
				activityMap.put(Constants.UPIDao.VPA_ADDRESS, vpa);

				int smsTableID = collectionService.smsPaymentActivityAddition(activityMap);

				log.info("smsTableID ---- > " + smsTableID);

				JSONObject jsonCode = new JSONObject(jsonResponsePaymentData);
				log.info("jsonCode--->" + jsonCode);
				JSONObject responseData = (JSONObject) jsonCode.get(JsonConstants.DATA);
				log.info("data part of json---" + responseData);

				String status = Constants.EMPTY_STRING;

				Collection collection = new Collection();
				collection.setReceiptNumber(receiptNo);
				collection.setBusinessPartnerNumber(unqNo);
				collection.setAppl(appl);
				collection.setAmount(amount);
				collection.setFeName(fename);
				collection.setContact(partyMobNo);
				collection.setMobileNumberNew(mobileNew);

				if (((String) jsonCode.get("code")).equalsIgnoreCase("00")) {
					status = Constants.UPIDao.SUCCESS;

					updateActivityMap.put(Constants.UPIDao.ID, smsTableID);

// updateActivityMap.put(Constants.UPIDao.INVOICE_ID,
// responseData.get("orderId"));

					updateActivityMap.put(Constants.UPIDao.INVOICE_ID, txnId.toString());

					updateActivityMap.put(Constants.UPIDao.THIRD_PARTY_RESPONSE, jsonResponsePaymentData);

					updateActivityMap.put(Constants.UPIDao.MODIFIED_BY, systemUserNew.getUserTableId().toString());

					updateActivityMap.put(Constants.UPIDao.THIRD_PARTY_STATUS, status);

					updateActivityMap.put(Constants.UPIDao.DEVICE_RESPONSE_STATUS, status);

					updateActivityMap.put(Constants.AllPayCollectionsDao.RECEIPT_NUMBER, receiptNo);

					collectionService.smsPaymentActivityUpdation(updateActivityMap);

					flag = true;

					collection.setPaymentMode(paymentMode);

// sendAllPaySMSToCustomerAfterSubmittingReceipt(collection,status,systemUserNew.getMobileNumber(),systemUserNew.getUserTableId(),systemUserNew.getImeiNo());

					log.info("Success in Request Payment----");
				}

				else {
					status = Constants.UPIDao.FAILURE;

					updateActivityMap.put(Constants.UPIDao.ID, smsTableID);

					updateActivityMap.put(Constants.UPIDao.THIRD_PARTY_RESPONSE, jsonResponsePaymentData);

					updateActivityMap.put(Constants.UPIDao.MODIFIED_BY, systemUserNew.getUserTableId().toString());

					updateActivityMap.put(Constants.UPIDao.THIRD_PARTY_STATUS, status);

					updateActivityMap.put(Constants.UPIDao.DEVICE_RESPONSE_STATUS, status);

					updateActivityMap.put(Constants.AllPayCollectionsDao.RECEIPT_NUMBER, receiptNo);

					collectionService.smsPaymentActivityUpdation(updateActivityMap);

					flag = false;

					collection.setPaymentMode(paymentMode);

					sendAllPaySMSToCustomerAfterSubmittingReceipt(collection, status, systemUserNew.getMobileNumber(),
							systemUserNew.getUserTableId(), systemUserNew.getImeiNo());

					log.info("Failure  in Request Payment----");

				}

			} else {

				flag = false;

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return flag;

	}

	private void sendAllPaySMSToCustomerAfterSubmittingReceipt(Collection collection, String status,
			String draMobileNumber, Long userTableId, String imeiNo)

	{

		SystemUser systemUser = new SystemUser();
		systemUser.setImeiNo(imeiNo);
		systemUser.setUserTableId(userTableId);
		systemUser.setMobileNumber(draMobileNumber);

		if (collection.getContact() != null && !collection.getContact().equalsIgnoreCase(Constants.EMPTY_STRING)) {
			log.info("Sending SMS on customer number " + collection.getContact());

			generateSMSToCustomerOnSubmittingReceiptForAllPay(collection.getAmount(), collection.getReceiptNumber(),
					collection.getPaymentMode(), collection.getContact(), collection.getAppl(), collection.getFeName(),
					systemUser, communicationActivityService, collection, status);

		}

		if (collection.getMobileNumberNew() != null
				&& !collection.getMobileNumberNew().equalsIgnoreCase(Constants.EMPTY_STRING)) {
			log.info("Sending SMS on customer number " + collection.getContact());

			generateSMSToCustomerOnSubmittingReceiptForAllPay(collection.getAmount(), collection.getReceiptNumber(),
					collection.getPaymentMode(), collection.getMobileNumberNew(), collection.getAppl(),
					collection.getFeName(), systemUser, communicationActivityService, collection, status);

		}

		if (draMobileNumber != null && !draMobileNumber.equalsIgnoreCase(Constants.EMPTY_STRING)) {
			log.info("Sending sms to DRA mobile number " + collection.getAmount());

			generateSMSToDRAOnSubmittingReceiptForAllPay(collection.getAmount(), collection.getReceiptNumber(),
					collection.getPaymentMode(), draMobileNumber, collection.getAppl(), collection.getFeName(),
					systemUser, communicationActivityService, collection, status);

		}

	}

	private void generateSMSToCustomerOnSubmittingReceiptForAllPay(String amount, String receiptNumber,
			String paymentType, String mobileNumber, String appl, String feName, SystemUser user,
			CommunicationActivityService communicationActivityService, Collection collection, String status) {

		log.info("---- Inside generateSMSToCustomerOnSubmittingReceiptForAllPay --------");

		String webserviceUrl = (String) applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");

		Map<String, Object> smsDispatcherMap = ServerUtilities.generateAllPaySMSToCustomerOnSubmittingReceiptForAllPay(
				amount, receiptNumber, paymentType, mobileNumber, appl, feName, status, collection);

		log.info("---- Inside smsDispatcherMap --------" + smsDispatcherMap);

		StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true, new HashMap<String, String>());

		log.info("---- Inside xmlRequest --------" + xmlRequest);

		CommunicationActivityAddition communicationActivityAddition = new CommunicationActivityAddition(
				user.getUserTableId().toString(), user.getImeiNo(), (appl + "_" + collection.getCollectionType()),
				webserviceUrl, xmlRequest.toString(), communicationActivityService,
				ActivityLoggerConstants.DATABASE_MSSQL);

		new Thread(communicationActivityAddition).run();

		KotakCollectionWebserviceAdapter kotakCollectionWebserviceAdapter = new KotakCollectionWebserviceAdapter();

		String xmlResponse = kotakCollectionWebserviceAdapter.callWebserviceAndGetXmlString(xmlRequest.toString(),
				webserviceUrl);

		CommunicationActivity communicationActivity = communicationActivityAddition.extractCommunicationActivity();

		Map<String, Object> result = null;

		if (null != xmlResponse && !xmlResponse.equals(Constants.EMPTY_STRING)) {
			communicationActivity.setResponse(xmlResponse);

			CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
					communicationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), communicationActivityService);

			new Thread(communicationActivityStatusUpdate).run();

			result = XMLToMap.convertXMLToMap(xmlResponse);

		} else {
			communicationActivity.setResponse(ActivityLoggerConstants.EMPTY_STRING);

			CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
					communicationActivity, (ActivityLoggerConstants.STATUS_FAILURE), communicationActivityService);

			new Thread(communicationActivityStatusUpdate).run();

			log.info("----- Failure in sending SMS : -------");
		}
	}

	private void generateSMSToDRAOnSubmittingReceiptForAllPay(String amount, String receiptNumber, String paymentType,
			String mobileNumber, String type, String feName, SystemUser user,
			CommunicationActivityService communicationActivityService, Collection collection, String status) {
		log.info("---- Inside generateAllPaySMSOnSubmittingReceiptForAllPay --------");

		String webserviceUrl = (String) applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");

		Map<String, Object> smsDispatcherMap = ServerUtilities.generateSMSToDRAOnSubmittingReceiptForAllPay(amount,
				receiptNumber, paymentType, mobileNumber, type, feName, status, collection);

		log.info("---- Inside smsDispatcherMap --------" + smsDispatcherMap);

		StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true, new HashMap<String, String>());

		log.info("---- Inside xmlRequest --------" + xmlRequest);

// log.info("----- xmlRequest : -------" + xmlRequest);
		CommunicationActivityAddition communicationActivityAddition = new CommunicationActivityAddition(
				user.getUserTableId().toString(), user.getImeiNo(), (type + "_" + collection.getCollectionType()),
				webserviceUrl, xmlRequest.toString(), communicationActivityService,
				ActivityLoggerConstants.DATABASE_MSSQL);

		new Thread(communicationActivityAddition).run();

		KotakCollectionWebserviceAdapter kotakCollectionWebserviceAdapter = new KotakCollectionWebserviceAdapter();

		String xmlResponse = kotakCollectionWebserviceAdapter.callWebserviceAndGetXmlString(xmlRequest.toString(),
				webserviceUrl);

		CommunicationActivity communicationActivity = communicationActivityAddition.extractCommunicationActivity();

		Map<String, Object> result = null;

		if (null != xmlResponse && !xmlResponse.equals(Constants.EMPTY_STRING)) {
			communicationActivity.setResponse(xmlResponse);

			CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
					communicationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), communicationActivityService);

			new Thread(communicationActivityStatusUpdate).run();

			result = XMLToMap.convertXMLToMap(xmlResponse);

		} else {
			communicationActivity.setResponse(ActivityLoggerConstants.EMPTY_STRING);

			CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
					communicationActivity, (ActivityLoggerConstants.STATUS_FAILURE), communicationActivityService);

			new Thread(communicationActivityStatusUpdate).run();

			log.info("----- Failure in sending SMS : -------");
		}
	}

	//@RequestMapping(value = "/collSubmit", method = RequestMethod.GET)
	public void submitCollections(String request)

	{

		log.info(" -------- In CollectionsService -------- ");

		String status = JsonConstants.FAILURE;
		String returnMessage = null;
		String contractAccountNumber = Constants.EMPTY_STRING;
		String lockCode = Constants.EMPTY_STRING;
		String collectionCode = Constants.EMPTY_STRING;
		String allocationNumber = Constants.EMPTY_STRING;
		String bp = Constants.EMPTY_STRING;
		String amount = "0.0";
		String pan = Constants.EMPTY_STRING;
		String email = Constants.EMPTY_STRING;
		String contact = Constants.EMPTY_STRING;
		String collectionStatus = Constants.EMPTY_STRING;
		String deviceDate = Constants.EMPTY_STRING;
		String revisitedDate = Constants.EMPTY_STRING;
		String area = Constants.EMPTY_STRING;
		String mread = Constants.EMPTY_STRING;
		String emailAddressNew = Constants.EMPTY_STRING;
		String mobileNumberNew = Constants.EMPTY_STRING;
		String deviceTime = Constants.EMPTY_STRING;
		String collStatus = Constants.EMPTY_STRING;
		String allocStatus = Constants.EMPTY_STRING;
		String receiptNumber = Constants.EMPTY_STRING;
		String remarks = Constants.EMPTY_STRING;
		String billNo = Constants.EMPTY_STRING;
		String batchNumber = Constants.EMPTY_STRING;
		String billCycle = Constants.EMPTY_STRING;
		String signaturePath = Constants.EMPTY_STRING;
		String signature = Constants.EMPTY_STRING;
		String caseId = "0L";

		String feedback_code = Constants.EMPTY_STRING;
		String ptpAmount = "0.00";

		String latitude = Constants.EMPTY_STRING;
		String longitude = Constants.EMPTY_STRING;
		String partyName = Constants.EMPTY_STRING;
		String nextActionCode = Constants.EMPTY_STRING;
		String nextActionCodeDescription = Constants.EMPTY_STRING;
		boolean submissionFlag = false;
		String regNo = Constants.EMPTY_STRING;
		String branchName = Constants.EMPTY_STRING;
		String paymentDate = Constants.EMPTY_STRING;
		String actioncode = Constants.EMPTY_STRING;
		String actionDesc = Constants.EMPTY_STRING;
		String resultCode = Constants.EMPTY_STRING;
		String resultDesc = Constants.EMPTY_STRING;
		String resultcodeAnddesc = Constants.EMPTY_STRING;
		String nextActionCodeValues = Constants.EMPTY_STRING;

//
		String start_lat = Constants.EMPTY_STRING;
		String start_long = Constants.EMPTY_STRING;
		String end_lat = Constants.EMPTY_STRING;
		String end_long = Constants.EMPTY_STRING;

		String result = Constants.EMPTY_STRING;

		List<Image> images = new ArrayList<Image>();

		try {
			String requestSet = request;

			JSONObject jsonObj = new JSONObject(requestSet);

			log.info("jsonObj-----------in collectionSubmissionService :: " + jsonObj);

			JSONObject jsonData = (JSONObject) jsonObj.get(JsonConstants.DATA);
			if (jsonData.has("images")) {
				jsonData.remove("images");
			}

			jsonObj.put(JsonConstants.DATA, jsonData);

			String requestWithoutImage = jsonObj.toString();

			UserActivityAddition userActivityAddition = new UserActivityAddition(requestWithoutImage,
					userActivityService, ActivityLoggerConstants.DATABASE_MSSQL);

			new Thread(userActivityAddition).run();

			UserActivity userActivity = userActivityAddition.extractUserActivity();

			JSONObject jsonObject = new JSONObject(requestSet);

			log.info("jsonObject-----------in collectionSubmissionService :: " + jsonObject);

			JSONObject data = (JSONObject) jsonObject.get(JsonConstants.DATA);
			JSONObject user = (JSONObject) jsonObject.get(JsonConstants.SYSTEM_USER);
			SystemUser systemUserNew = ServerUtilities.extractSystemUser(user);

			SystemUser systemUserTemp = systemUserService.getUser(systemUserNew.getUserTableId());
			systemUserNew.setSupervisorMobileNumber(systemUserTemp.getSupervisorMobileNumber());
			systemUserNew.setSupervisorName(systemUserTemp.getSupervisorName());

			log.info("----system user ----" + systemUserNew);

			Collection collection = new Collection();

			Map reqMap = Utilities.createMapFromJSON(requestSet);
			String type = (String) reqMap.get(JsonConstants.Key.TYPE);

			String requestEntity = data.get(JsonConstants.APPL) == null ? Constants.EMPTY_STRING
					: data.getString(JsonConstants.APPL);

			/* Deposition submission check */

			collectionCode = data.get(JsonConstants.RequestData.COLLECTION_CODE) == null ? Constants.EMPTY_STRING
					: (String) data.get(JsonConstants.RequestData.COLLECTION_CODE);

			String payMode = data.get(JsonConstants.RequestData.PAY_MODE) == null ? Constants.EMPTY_STRING
					: (String) data.get(JsonConstants.RequestData.PAY_MODE);

			if (type.toString().equalsIgnoreCase("collections")) {
				caseId = data.get("caseId") == null ? "0L" : data.get("caseId").toString();
			}

			collectionCode = data.get(JsonConstants.RequestData.COLLECTION_CODE) == null ? Constants.EMPTY_STRING
					: (String) data.get(JsonConstants.RequestData.COLLECTION_CODE);

			if (data.has(JsonConstants.FEEDBACK_CODE)) {
				feedback_code = data.getString(JsonConstants.FEEDBACK_CODE);

			}

			if (!collectionCode.equalsIgnoreCase("RTP")) {

				revisitedDate = data.get(JsonConstants.RequestData.REVISITED_DATE) == null ? Constants.EMPTY_STRING
						: (String) data.get(JsonConstants.RequestData.REVISITED_DATE);

			}

			if (data.has(JsonConstants.ACTIONCODE)) {
				collection.setActionCode(data.getString("actionCode"));
			}
			if (data.has(JsonConstants.RESULTCODE)) {
				collection.setResultCode(data.getString("resultCode"));
			}

// added by bhushan
			if (data.has(JsonConstants.FEEDBACKCODE) && (!collectionCode.equalsIgnoreCase("RTP"))) {
				JSONObject feedBackCodeJSON = (JSONObject) data.getJSONObject(JsonConstants.FEEDBACKCODE);

				String actioncodeAnddesc = feedBackCodeJSON.get(JsonConstants.ACTIONCODE) == null
						? Constants.EMPTY_STRING
						: feedBackCodeJSON.get(JsonConstants.ACTIONCODE).toString();
				Boolean flag = false;

				if (actioncodeAnddesc.length() > 0) {
					flag = true;
				}

				if (actioncodeAnddesc.contains("(")) {
					actioncode = actioncodeAnddesc.substring(0, actioncodeAnddesc.indexOf("(") - 1).trim();

					actionDesc = actioncodeAnddesc.substring(actioncodeAnddesc.indexOf("(") + 1,
							actioncodeAnddesc.indexOf(")"));
				}

				JSONArray code = feedBackCodeJSON.getJSONArray(JsonConstants.CODES);

				List<Feedback> feedBackCodeList = new ArrayList<Feedback>();

				for (int i = 0; i < (code.length()); i++) {
					JSONObject codeJSON = code.getJSONObject(i);
					Feedback feedBack = new Feedback();

					resultcodeAnddesc = codeJSON.get(JsonConstants.RESULTCODE) == null ? Constants.EMPTY_STRING
							: codeJSON.get(JsonConstants.RESULTCODE).toString();

					if (resultcodeAnddesc.length() > 0) {
						flag = true;
					}

					if (resultcodeAnddesc.contains("(")) {
						resultCode = resultcodeAnddesc.substring(0, resultcodeAnddesc.indexOf("(") - 1).trim();

						resultDesc = resultcodeAnddesc.substring(resultcodeAnddesc.indexOf("(") + 1,
								resultcodeAnddesc.indexOf(")"));
					}

					if (codeJSON.has(JsonConstants.NEXT_ACTION_CODE)) {
						nextActionCodeValues = codeJSON.get(JsonConstants.NEXT_ACTION_CODE) == null
								? Constants.EMPTY_STRING
								: codeJSON.get(JsonConstants.NEXT_ACTION_CODE).toString();
					}

					if (nextActionCodeValues.length() > 0) {
						flag = true;
					}

					if (nextActionCodeValues.contains("(")) {
						nextActionCode = nextActionCodeValues.substring(0, nextActionCodeValues.indexOf("(") - 1)
								.trim();

						nextActionCodeDescription = nextActionCodeValues
								.substring(nextActionCodeValues.indexOf("(") + 1, nextActionCodeValues.indexOf(")"));
					}

					String revisitedDate1 = Constants.EMPTY_STRING;
					if (actioncode != Constants.EMPTY_STRING && actionDesc != Constants.EMPTY_STRING) {
						feedBack.setActionCode(actioncode);
						feedBack.setActionDesc(actionDesc);
					}
					if (resultCode != Constants.EMPTY_STRING && resultCode != Constants.EMPTY_STRING) {
						feedBack.setResultCode(resultCode);
						feedBack.setResultDesc(resultDesc);
					}

					if (nextActionCode != Constants.EMPTY_STRING
							&& nextActionCodeDescription != Constants.EMPTY_STRING) {
						feedBack.setNextActionCode(nextActionCode);
						feedBack.setNextActionCodeDescription(nextActionCodeDescription);
					}

					if (revisitedDate1 != Constants.EMPTY_STRING) {
						feedBack.setRevisitDate(revisitedDate1);
					}

					if (flag) {
						feedBackCodeList.add(feedBack);
					}
				}

				collection.setFeedback(feedBackCodeList);
				log.info("feedBackCodeList : " + feedBackCodeList);

			}

			if (((!collectionCode.equalsIgnoreCase("RTP")) || amount.equals(Constants.EMPTY_STRING))
					&& !collectionCode.equalsIgnoreCase("PU")) {

				if (data.has(JsonConstants.PTP_AMOUNT_NEW))
					ptpAmount = data.getString(JsonConstants.PTP_AMOUNT_NEW);

			}

			else if (collectionCode.equalsIgnoreCase("PU")) {

				if (data.has(JsonConstants.RequestData.AMOUNT))
					ptpAmount = data.getString(JsonConstants.RequestData.AMOUNT);

			} else {

				if (data.has(JsonConstants.RequestData.AMOUNT))
					amount = (String) data.get(JsonConstants.RequestData.AMOUNT);

			}

			deviceTime = data.get(JsonConstants.RequestData.DEVICE_TIME) == null ? Constants.EMPTY_STRING
					: (String) data.get(JsonConstants.RequestData.DEVICE_TIME);

			deviceDate = data.get(JsonConstants.RequestData.DEVICE_DATE) == null ? Constants.EMPTY_STRING
					: (String) data.get(JsonConstants.RequestData.DEVICE_DATE);

			area = data.get(JsonConstants.RequestData.AREA) == null ? Constants.EMPTY_STRING
					: (String) data.get(JsonConstants.RequestData.AREA);

			if (data.has(JsonConstants.RequestData.BRANCH_NAME)) {
				branchName = data.get(JsonConstants.RequestData.BRANCH_NAME) == null ? Constants.EMPTY_STRING
						: (String) data.get(JsonConstants.RequestData.BRANCH_NAME);
			}

			if (data.has(JsonConstants.RequestData.PAYMENT_DATE)) {
				paymentDate = data.get(JsonConstants.RequestData.PAYMENT_DATE) == null ? Constants.EMPTY_STRING
						: (String) data.get(JsonConstants.RequestData.PAYMENT_DATE);
			}

			if (data.has(JsonConstants.RequestData.COLLECTION_STATUS)) {

				collStatus = data.get(JsonConstants.RequestData.COLLECTION_STATUS) == null ? Constants.EMPTY_STRING
						: (String) data.get(JsonConstants.RequestData.COLLECTION_STATUS);
			}
			
			if (data.has("allocStatus")) {

				allocStatus = data.get("allocStatus") == null ? Constants.EMPTY_STRING : (String) data.get("allocStatus");
			}

			receiptNumber = data.get(JsonConstants.RequestData.RECEIPT_NUMBER) == null ? Constants.EMPTY_STRING
					: (String) data.get(JsonConstants.RequestData.RECEIPT_NUMBER);

			remarks = data.get(JsonConstants.RequestData.REMARKS) == null ? Constants.EMPTY_STRING
					: (String) data.get(JsonConstants.RequestData.REMARKS);

			if (type.toString().equalsIgnoreCase("collections")) {
				billCycle = data.get(JsonConstants.BILL_CYCLE) == null ? Constants.EMPTY_STRING
						: (String) data.get(JsonConstants.BILL_CYCLE);
			}

			if (data.has(JsonConstants.MOBILE_NUMBER_NEW)) {
				mobileNumberNew = data.get(JsonConstants.MOBILE_NUMBER_NEW) == null ? Constants.EMPTY_STRING
						: data.getString(JsonConstants.MOBILE_NUMBER_NEW);

			}

			if (data.has(JsonConstants.MOBILE_NUMBER)) {
				contact = data.get(JsonConstants.MOBILE_NUMBER) == null ? Constants.EMPTY_STRING
						: data.getString(JsonConstants.MOBILE_NUMBER);
			}
			emailAddressNew = data.get(JsonConstants.EMAIL_ADDRESS_NEW) == null ? Constants.EMPTY_STRING
					: data.getString(JsonConstants.EMAIL_ADDRESS_NEW);

			email = data.get(JsonConstants.EMAIL_ADDRESS) == null ? Constants.EMPTY_STRING
					: data.getString(JsonConstants.EMAIL_ADDRESS);

			if (data.has(JsonConstants.LATITUDE) && data.has(JsonConstants.LONGITUDE)) {
				latitude = data.get(JsonConstants.LATITUDE) == null ? Constants.EMPTY_STRING
						: data.getString(JsonConstants.LATITUDE);
				longitude = data.get(JsonConstants.LONGITUDE) == null ? Constants.EMPTY_STRING
						: data.getString(JsonConstants.LONGITUDE);
			} else {
				latitude = "0.00";
				longitude = "0.00";
			}

			collection.setEmailAddress(data.get(JsonConstants.EMAIL_ADDRESS) == null ? Constants.EMPTY_STRING
					: data.getString(JsonConstants.EMAIL_ADDRESS));

			collection.setCorrAddress(data.get(JsonConstants.CORRESPONDENCE_ADDRESS) == null ? Constants.EMPTY_STRING
					: data.getString(JsonConstants.CORRESPONDENCE_ADDRESS));

			collection.setCorrLocation(
					data.getString(JsonConstants.CORRESPONDENCE_LOCATION) == null ? Constants.EMPTY_STRING
							: data.getString(JsonConstants.CORRESPONDENCE_LOCATION));

			if (data.has(JsonConstants.CORRESPONDENCE_PINCODE)) {
				collection.setCorrPin(data.getString(JsonConstants.CORRESPONDENCE_PINCODE));
			}

			if (data.has(JsonConstants.SECOND_ADDRESS)) {
				collection.setSecAddress(data.getString(JsonConstants.SECOND_ADDRESS));
			}

			if (data.has(JsonConstants.SECOND_LOCATION)) {
				collection.setSecLocation(data.getString(JsonConstants.SECOND_LOCATION));
			}

			if (data.has(JsonConstants.SECOND_PINCODE)) {
				collection.setSecPin(data.getString(JsonConstants.SECOND_PINCODE));
			}

			if (data.has(JsonConstants.DUE_DATE)) {
				collection.setDueDate(data.getString(JsonConstants.DUE_DATE));
			}

			if (type.toString().equalsIgnoreCase("collections")) {
				collection.setCaseId(Long.parseLong(caseId));
			}

			collection.setOutstanding(Double.parseDouble(data.getString(JsonConstants.OUTSTANDING) == null
					|| data.getString(JsonConstants.OUTSTANDING).equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0"
							: data.getString(JsonConstants.OUTSTANDING)));

			if (payMode.equals(Constants.PAYMENT_MODE_CASH) && !amount.equals("0.0")
					&& !collectionCode.equalsIgnoreCase("PU")) {
				if (data.has(JsonConstants.RequestData.CASH)) {
					extractCashDetails(pan, data, collection);

				}

				List<Cheque> cheques = new ArrayList<Cheque>();

				log.info("---collection code" + collectionCode);

				if (payMode.equalsIgnoreCase("DCARD") || payMode.equalsIgnoreCase("NB")
						|| payMode.equalsIgnoreCase("UPI")) {

					collection.setCcapac("2003");
				}

				if ((Constants.PAYMENT_MODE_CHEQUE.equals(payMode) || Constants.PAYMENT_MODE_DRAFT.equals(payMode)
						|| Constants.PAYMENT_MODE_PDC.equals(payMode)) && !collectionCode.equalsIgnoreCase("PU")) {
					cheques = getCheques(systemUserNew, data);
					log.info("Cheques got : " + cheques);

				} else if (Constants.UPIDao.UPI.equalsIgnoreCase(payMode)) {
					boolean flag = requestPayment(request);
					String invoiceID = data.get(JsonConstants.TNX_ID) == null ? Constants.EMPTY_STRING
							: data.getString(JsonConstants.TNX_ID);
					collection.setInvoiceId(invoiceID);

				}

				if (data.has(JsonConstants.LOAN)) {
					JSONObject loanJSON = (JSONObject) data.getJSONObject(JsonConstants.LOAN);

					if (data.has(JsonConstants.LOAN_OVERDUE)) {
						collection.setOverdue(Double.parseDouble(loanJSON.get(JsonConstants.LOAN_OVERDUE) == null
								|| loanJSON.get(JsonConstants.LOAN_OVERDUE).toString()
										.equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0"
												: loanJSON.get(JsonConstants.LOAN_OVERDUE).toString()));
					}
					/*
					 * collection.setPenalAmt(Double.parseDouble(loanJSON.get(JsonConstants.
					 * LOAN_PENAL_AMOUNT) == null ||
					 * loanJSON.get(JsonConstants.LOAN_PENAL_AMOUNT).toString()
					 * .equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0" : loanJSON.get(
					 * JsonConstants.LOAN_PENAL_AMOUNT).toString()));
					 */
// commented as getting error for appr amount and sam is set at
// the bottom with amount
					/*
					 * collection .setAppropriateAmount(Double.parseDouble(loanJSON
					 * .get(JsonConstants.APPR_AMOUNT) == null || loanJSON
					 * .get(JsonConstants.APPR_AMOUNT) .toString() .equalsIgnoreCase(
					 * Constants.EMPTY_STRING) ? "0.0" : loanJSON.get(JsonConstants.APPR_AMOUNT)
					 * .toString()));
					 */

					JSONArray transType = loanJSON.getJSONArray(JsonConstants.LOAN_TRANS_TYPE);

					List<TransactionType> transTypeList = new ArrayList<TransactionType>();

					for (int i = 0; i < (transType.length()); i++) {
						JSONObject transJSON = transType.getJSONObject(i);
						TransactionType transactionType = new TransactionType();
						transactionType.setType(transJSON.get(JsonConstants.LOAN_TRANS_TYPE_TYPE).toString());
						transactionType.setAmount(transJSON.get(JsonConstants.LOAN_TRANS_TYPE_AMOUNT) == null
								|| transJSON.getString(JsonConstants.LOAN_TRANS_TYPE_AMOUNT)
										.equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0"
												: transJSON.get(JsonConstants.LOAN_TRANS_TYPE_AMOUNT).toString());

						transTypeList.add(transactionType);
					}

					collection.setTransType(transTypeList);
					log.info("transTypeList : " + transTypeList);

				}

				if (data.has(JsonConstants.CREDIT_CARD)) {
					JSONObject ccJSON = (JSONObject) data.get(JsonConstants.CREDIT_CARD);
					collection.setTad(Double.parseDouble(ccJSON.get(JsonConstants.TAD) == null
							|| ccJSON.get(JsonConstants.TAD).toString().equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0"
									: ccJSON.get(JsonConstants.TAD).toString()));
					collection.setMad(Double.parseDouble(ccJSON.get(JsonConstants.MAD) == null
							|| ccJSON.get(JsonConstants.MAD).toString().equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0"
									: ccJSON.get(JsonConstants.MAD).toString()));
					collection.setBuckAmt1(
							Double.parseDouble(ccJSON.get("bucket1") == null || ccJSON.get("bucket1").toString()
// key changes
// JsonConstants.BUCKET_AMOUNT_1
									.equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0"
											: ccJSON.get("bucket1").toString()));
					collection.setBuckAmt2(Double.parseDouble(ccJSON.get("bucket2") == null
							|| ccJSON.get("bucket2").toString().equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0"
									: ccJSON.get("bucket2").toString()));// key
// change
// JsonConstants.BUCKET_AMOUNT_2
					collection.setRollbackAmt(Double.parseDouble(ccJSON.get("rollbackAmnt") == null
							|| ccJSON.get("rollbackAmnt").toString().equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0"
									: ccJSON.get("rollbackAmnt").toString()));// key
// change
// JsonConstants.ROLLBACK_AMOUNT
				}

				JSONArray mPOSTransDetails = new JSONArray();

				JSONObject mPOSTransDetail = new JSONObject();
				MPOSDetail mposDetail = new MPOSDetail();
				if (data.has(JsonConstants.mPOS_TRANS_DETAILS)) {
					mPOSTransDetail = (JSONObject) data.get(JsonConstants.mPOS_TRANS_DETAILS);
					/*
					 * for (int i = 0; i < mPOSTransDetails.length(); i++) {
					 */

// mPOSTransDetail = (JSONObject) mPOSTransDetails.get(i);

					mposDetail.setTransactionId(mPOSTransDetail.has(JsonConstants.mPOS_TRANS_ID)
							? mPOSTransDetail.get(JsonConstants.mPOS_TRANS_ID).toString()
							: Constants.EMPTY_STRING);
					mposDetail.setBillNumber(mPOSTransDetail.has(JsonConstants.mPOS_BILL_NUMBER)
							? mPOSTransDetail.get(JsonConstants.mPOS_BILL_NUMBER).toString()
							: Constants.EMPTY_STRING);
					mposDetail.setCardNo(mPOSTransDetail.has(JsonConstants.mPOS_CARD_NUMBER)
							? mPOSTransDetail.get(JsonConstants.mPOS_CARD_NUMBER).toString()
							: Constants.EMPTY_STRING);
					mposDetail.setTransactionDateTime(mPOSTransDetail.has(JsonConstants.mPOS_TRANS_DATE_TIME)
							? mPOSTransDetail.get(JsonConstants.mPOS_TRANS_DATE_TIME).toString()
							: Constants.EMPTY_STRING);
					mposDetail.setSwipeAmount(mPOSTransDetail.has(JsonConstants.mPOS_SWIPE_AMOUNT)
							? mPOSTransDetail.get(JsonConstants.mPOS_SWIPE_AMOUNT).toString()
							: Constants.EMPTY_STRING);
					mposDetail.setCardHolderName(mPOSTransDetail.has(JsonConstants.mPOS_CARD_HOLDER_NAME)
							? mPOSTransDetail.get(JsonConstants.mPOS_CARD_HOLDER_NAME).toString()
							: Constants.EMPTY_STRING);
					mposDetail.setCardType(mPOSTransDetail.has(JsonConstants.mPOS_CARD_TYPE)
							? mPOSTransDetail.get(JsonConstants.mPOS_CARD_TYPE).toString()
							: Constants.EMPTY_STRING);
					mposDetail.setMdrAmnt(mPOSTransDetail.has(JsonConstants.mPOS_MDR_AMOUNT)
							? mPOSTransDetail.get(JsonConstants.mPOS_MDR_AMOUNT).toString()
							: Constants.EMPTY_STRING);
					mposDetail.setServiceTaxAmnt(mPOSTransDetail.has(JsonConstants.mPOS_SERVICETAX_AMOUNT)
							? mPOSTransDetail.get(JsonConstants.mPOS_SERVICETAX_AMOUNT).toString()
							: Constants.EMPTY_STRING);
				}

				collection.setMposDetail(mposDetail);

				/* } */

				if (type.toString().equalsIgnoreCase("collections")) {
					collection.setCollectionType(Constants.COLLECTIONS);

					collection.setCollectionNature(Constants.COLLECTION_NATURE_GENERAL);
				}
				if (type.toString().equalsIgnoreCase("randomCollections")) {
					collection.setCollectionType(Constants.RANDOM_COLLECTIONS);

					collection.setCollectionNature(Constants.COLLECTION_NATURE_GENERAL);
				}
				if (type.toString().equalsIgnoreCase("fileCollections")) {
					collection.setCollectionType(Constants.COLLECTION_TYPE_FILE);

					collection.setCollectionNature(Constants.COLLECTION_NATURE_FILE);
				}
				if (type.toString().equalsIgnoreCase("fileRandomCollections")) {
					collection.setCollectionType(Constants.COLLECTION_TYPE_FILE_RANDOM);

					collection.setCollectionNature(Constants.COLLECTION_NATURE_FILE);
				}

				if (type.toString().equalsIgnoreCase("collections")
						|| type.toString().equalsIgnoreCase("fileCollections")) {
					log.info("--- file collection or else ---");
					if (data.has(JsonConstants.PARTY_NAME)) {

						collection.setPartyName(data.getString(JsonConstants.PARTY_NAME));
					}
				}
				if (type.toString().equalsIgnoreCase("randomCollections")
						|| type.toString().equalsIgnoreCase("fileRandomCollections")) {
					if (data.has(JsonConstants.NAME)) {
						collection.setPartyName(data.getString(JsonConstants.NAME));
					}
				}

				if (type.toString().equalsIgnoreCase("collections")
						|| type.toString().equalsIgnoreCase("fileCollections")) {

					if (data.has(JsonConstants.PARTY_ID)) {

						collection.setContractAccountNumber(data.getString(JsonConstants.PARTY_ID));
					}
				}

				if (type.toString().equalsIgnoreCase("randomCollections")
						|| type.toString().equalsIgnoreCase("fileRandomCollections")) {
					if (data.has(JsonConstants.CONTRACT_ACCOUNT_NUMBER)) {
						collection.setContractAccountNumber(data.getString(JsonConstants.CONTRACT_ACCOUNT_NUMBER));
					}
				}

				/* Added */

				if (data.has(JsonConstants.INVOICE_ID_DEVICE)) {
					collection.setInvoiceId(data.getString(JsonConstants.INVOICE_ID_DEVICE));
				}

// 15
				if (data.has(Constants.START_VISIT_TIME)) {
					collection.setVisitStartTime(data.getString(Constants.START_VISIT_TIME));
					log.info("setVisitStartTime" + data.getString(Constants.START_VISIT_TIME));

				}
				if (data.has(Constants.END_VISIT_TIME)) {
					collection.setVisitEndTime(data.getString(Constants.END_VISIT_TIME));
					log.info("setVisitEndTime" + data.getString(Constants.END_VISIT_TIME));
				}

//
				if (data.has(JsonConstants.LocationTracker.START_LAT)
						&& data.has(JsonConstants.LocationTracker.START_LONG)) {
					start_lat = data.get(JsonConstants.LocationTracker.START_LAT) == null ? Constants.EMPTY_STRING
							: data.getString(JsonConstants.LocationTracker.START_LAT);
					start_long = data.get(JsonConstants.LocationTracker.START_LONG) == null ? Constants.EMPTY_STRING
							: data.getString(JsonConstants.LocationTracker.START_LONG);
					log.info("startlatLong " + start_lat + start_long);
				} else {
					start_lat = "0.00";
					start_long = "0.00";
				}

				if (data.has(JsonConstants.LocationTracker.END_LAT)
						&& data.has(JsonConstants.LocationTracker.END_LONG)) {
					end_lat = data.get(JsonConstants.LocationTracker.END_LAT) == null ? Constants.EMPTY_STRING
							: data.getString(JsonConstants.LocationTracker.END_LAT);
					end_long = data.get(JsonConstants.LocationTracker.END_LONG) == null ? Constants.EMPTY_STRING
							: data.getString(JsonConstants.LocationTracker.END_LONG);
					log.info("endlatLong " + end_lat + end_long);
				} else {
					end_lat = "0.00";
					end_long = "0.00";
				}
// 12
				collection.setStartLatitude(start_lat);
				collection.setStartLongitude(start_long);
				collection.setEndLatitude(end_lat);
				collection.setEndLongitude(end_long);

				/*
				 * if (type.toString().equalsIgnoreCase("collections") ||
				 * type.toString().equalsIgnoreCase("fileCollections")) { if
				 * (data.has(JsonConstants.CC_APAC)) {
				 * collection.setCcapac(data.getString(JsonConstants.CC_APAC)); } }
				 * 
				 * if (type.toString().equalsIgnoreCase("randomCollections") ||
				 * type.toString().equalsIgnoreCase("fileRandomCollections")) { if
				 * (data.has(JsonConstants.CC_APAC)) {
				 * collection.setCcapac(data.getString(JsonConstants.CC_APAC)); } }
				 */

				if (data.has(JsonConstants.CC_APAC)) {
					collection.setCcapac(data.getString(JsonConstants.CC_APAC));
				}

// TODO Code to add Multiple APACS
				int numberOfApacs = 1;
// numberOfApacs = data.getString("noOfApac") == null ? 0 :
// Integer.parseInt(data.getString("noOfApac"));

				collection.setNumberOfApacs(numberOfApacs);

// collection.setContractAccountNumber(contractAccountNumber);
				collection.setCollectionCode(collectionCode);
				collection.setAllocationNumber(allocationNumber);
				collection.setRequestId(
						data.has(JsonConstants.REQUEST_ID) == true ? data.getString(JsonConstants.REQUEST_ID)
								: new Timestamp(System.currentTimeMillis()).toString());
				collection.setMobileNumberNew(mobileNumberNew);
				collection.setEmailAddressNew(emailAddressNew);
				collection.setArea(area);
				collection.setChequeDetails(cheques);
				collection.setCollectionStatus(collStatus);
				collection.setCollectionCode(collectionCode);
				collection.setReceiptNumber(receiptNumber);
				collection.setRevisitDate(revisitedDate);
				collection.setMeterReading("sms");
				if (payMode.equalsIgnoreCase("Debit")) {
					collection.setPaymentMode("ORI");
				} else {
					collection.setPaymentMode(payMode);
				}

				log.info("paymode %%%%%%" + collection.getPaymentMode());

				collection.setDeviceDate(deviceDate);
				collection.setDeviceTime(deviceTime);
				collection.setSubmissionDateTime(Utilities.sysDate());
				collection.setRemarks(remarks);
				collection.setCurrentBillNo(billNo);
				collection.setBusinessPartnerNumber(data.getString(JsonConstants.UNIQUE_NUMBER)); // apac or card
// number
				collection.setAppl(data.getString(JsonConstants.APPL));
				collection.setBillCycle(billCycle);
// collection.setName(data.getString("name"));
				if (data.has(JsonConstants.MOBILE_NUMBER)) {
					collection.setMobileNumber(data.getString(JsonConstants.MOBILE_NUMBER));
				}
				collection.setSignaturePath(signaturePath);
				collection.setImages(images);
				collection.setUserName(systemUserNew.getName());
// collection.setPan(pan);
				collection.setUser(systemUserNew); // new added
				collection.setContact(contact);
				collection.setEmail(email);
				collection.setBatchNumber(batchNumber);
				collection.setAmount(amount);
				collection.setAppropriateAmount(amount);
				collection.setArFeedbackCode(feedback_code == null ? " " : feedback_code);
				collection.setPtpAmount(ptpAmount);
// collection.setNextActionCode(nextActionCode);
// collection.setNextActionCodeDescription(nextActionCodeDescription);
				collection.setBranchName(branchName);
				collection.setPaymentDate(paymentDate);
				collection.setDepositionStatus(Constants.Deposition.INITIAL_DEPOSITION);

				log.info("----pan details----" + collection.getPan());

				if (type.toString().equalsIgnoreCase("collections")
						|| type.toString().equalsIgnoreCase("randomCollections")) {
					collection.setLatitude(latitude);
					collection.setLongitude(longitude);

				}

				Agency agency = new Agency();
				agency.setAgencyId(systemUserNew.getAgencyId());
				List<Agency> agencies = agencyService.searchAgency(agency);
				collection.setAgencyName(agencies.get(0).getAgencyName());

				log.info("collection trans type ------------------>" + collection.getTransType());

				Utilities.primaryBeanSetter(collection, systemUserNew);
				log.info(agencies.get(0).getAgencyName());
				/*
				 * if (!data.has(JsonConstants.RequestData.SIGN)) { returnMessage =
				 * JsonConstants.COLLECTION_SIGNATURE_MANDATORY; return responseBuilder(message,
				 * status, returnMessage, Constants.EMPTY_STRING); } else { signature = (String)
				 * data.get(JsonConstants.RequestData.SIGN); }
				 */

				if (data.has(JsonConstants.RequestData.SIGN)) {
					signature = (String) data.get(JsonConstants.RequestData.SIGN);

// signature = data.get(JsonConstants.RequestData.SIGN) == null ?
// Constants.EMPTY_STRING : (String)
// data.get(JsonConstants.RequestData.SIGN);
				}

				if (!signature.isEmpty()) {
					signaturePath = extractImagePath(collection, signature, Constants.SIGNATURE_IMAGE_FILE_PATH,
							Constants.EMPTY_STRING);
				}

				if (type.toString().equalsIgnoreCase("collections")
						|| type.toString().equalsIgnoreCase("randomCollections")
						|| type.toString().equalsIgnoreCase("fileCollections")
						|| type.toString().equalsIgnoreCase("fileRandomCollections")) {
					if (!data.has(JsonConstants.RequestData.IMAGES)) {

						/*
						 * returnMessage = JsonConstants.COLLECTION_SUBMIT_FAILURE; return
						 * responseBuilder(message, status, returnMessage, Constants.EMPTY_STRING);
						 */
					} else {

						JSONArray imageDetails = data.getJSONArray(JsonConstants.RequestData.IMAGES);

						log.info("---image Details ----" + imageDetails);

						images = getImages(systemUserNew, imageDetails, collection);

						collection.setImages(images);

						log.info("--- image---" + images);

						if (images == null) {

							status = JsonConstants.FAILURE;
							returnMessage = "Image Path Not Found";

						}
					}
				}

				if (collectionCode.equalsIgnoreCase(JsonConstants.CUSTOMER_UPDATE_CODE)) {
					try {

						if (caseService.checkDuplicateCustomerData(collection)) {
							if (caseService.submitCustomerData(collection)) {

								status = JsonConstants.SUCCESS;

							}

							else {

								status = JsonConstants.FAILURE;

							}

						}

						else {
							status = JsonConstants.SUCCESS;

						}

					} catch (Exception e) {

						log.error("------Exception Detail while submission of customer is ", e);

						status = JsonConstants.FAILURE;

					}

				}

				int i = collectionService.checkDuplicateCollectionJSON(collection);
				if (i == 0) {
					log.info("collection cheque details ========----------->" + collection.getChequeDetails());
					log.info("complete collection -------------->" + collection);

					submissionFlag = collectionService.submitCollection(collection);
					log.info("-------submissionFlag--------" + submissionFlag);

					if (submissionFlag) {
						log.info("Collection submitted without violation");
						caseService.updateCase(collection, collStatus, allocStatus);

// Code for sending sms to PTP / Broken Promisev / Door Lock

						try {

							/*
							 * if (collection.getCollectionCode().equalsIgnoreCase("PTP") ||
							 * collection.getCollectionCode().equalsIgnoreCase("DL") ||
							 * collection.getCollectionCode().equalsIgnoreCase("BRP")) {
							 */
							log.info("---PTP SMS ---");

// sendSms(collection, systemUserNew); }

							log.info("getMeterReading " + collection.getMeterReading());
							log.info("collection type" + collection.getCollectionNature());
							if (collection.getCollectionCode().equalsIgnoreCase("RTP")) {

								try {

									sendCollectionsSms(collection, systemUserNew);

									log.info("testing 1");

// offlineSMSService.updateOfflineSMSData(collection.getReceiptNumber());

								} catch (Exception e) {
									log.error("---Error While sending SMS---", e);
								}

								/*
								 * try { // callEmailService(collection); } catch (Exception e) {
								 * log.error("---Error While sending Email---", e); }
								 */

							}

							/* } */

						} catch (Exception e) {
							log.info("Error while sending sms to PTP / Broken Promise / Door Lock" + e);
							e.printStackTrace();
						}

						log.info("------------- Collection Submitted and Case Updated sucessfully -------------");

						/*
						 * if
						 * (!collection.getPaymentMode().equalsIgnoreCase(Constants.AllPayCollectionsDao
						 * .DCARD_PAYMODE) &&
						 * !collection.getPaymentMode().equalsIgnoreCase(Constants.AllPayCollectionsDao.
						 * NB_PAYMODE) &&
						 * !collection.getPaymentMode().equalsIgnoreCase(Constants.PAY_MODE_UPI)) {
						 * 
						 * sendCollectionsSms(collection, systemUserNew);
						 * 
						 * }
						 */

						status = JsonConstants.SUCCESS;

						UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
								(ActivityLoggerConstants.STATUS_SUCCESS), userActivityService);
						new Thread(userActivityStatusUpdate).run();

						if (result.equalsIgnoreCase("R_75")) {
							returnMessage = JsonConstants.COLLECTIONS_FAILED_DUE_TO_DEPOSITION;

						} else {
							returnMessage = "Collection got submitted successfully";
						}

					} else {
						System.out.println("Collection submitted with violation");
						log.info("Collection submitted with violation");

						status = JsonConstants.FAILURE;

						UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
								(ActivityLoggerConstants.STATUS_FAILURE), userActivityService);
						new Thread(userActivityStatusUpdate).run();

					} // status = JsonConstants.SUCCESS;

				} else if (i == 1) {
					log.info("--------- Collection Record already exists, JSON Duplicated! ------------");
					status = JsonConstants.SUCCESS;
// status = JsonConstants.FAILURE;

// returnMessage = JsonConstants.COLLECTION_SUBMIT_SUCCESS;

					returnMessage = "JSON DUPLICATED!!!";

					if (type.toString().equalsIgnoreCase("collections")) {
						returnMessage = "JSON DUPLICATED For Collections!!!";
					}
					if (type.toString().equalsIgnoreCase("randomCollections")) {
						returnMessage = "JSON DUPLICATED For RandomCollections!!!";
					}
					if (type.toString().equalsIgnoreCase("fileCollections")) {
						returnMessage = "JSON DUPLICATED For FileCollections!!!";
					}
					if (type.toString().equalsIgnoreCase("fileRandomCollections")) {
						returnMessage = "JSON DUPLICATED For FileRandomCollections!!!";
					}

					UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
							(ActivityLoggerConstants.STATUS_IGNORE), userActivityService);
					new Thread(userActivityStatusUpdate).run();

				} else {
					log.info("Some error occured at Dao checkDuplicateCollectionJSON ");

					status = JsonConstants.FAILURE;

					UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
							(ActivityLoggerConstants.STATUS_FAILURE), userActivityService);
					new Thread(userActivityStatusUpdate).run();

				}
			}
		}

		catch (Exception e) {
			e.printStackTrace();
			log.error("--- Exception In CollectionSubmissionService :: " + e);

			returnMessage = JsonConstants.COLLECTION_SUBMIT_FAILURE;

		}

	}

	/*
	 * public void sendMsgOnKafkaTopic(String request, String topicName) {
	 * 
	 * ListenableFuture<SendResult<String, String>> future =
	 * produceCollection.send(topicName, request);
	 * 
	 * future.addCallback(new ListenableFutureCallback<SendResult<String, String>>()
	 * {
	 * 
	 * @Override public void onSuccess(SendResult<String, String> result) {
	 * 
	 * log.info("---Data sent successfully on " + topicName);
	 * 
	 * }
	 * 
	 * @Override public void onFailure(Throwable ex) {
	 * 
	 * log.info("---Failed to sent data on " + topicName);
	 * 
	 * }
	 * 
	 * });
	 * 
	 * }
	 */

	public void sendMsgOnKafkaTopic(String request, String prodType) {

		int partition;

		if (prodType.equalsIgnoreCase(JsonConstants.RequestData.RETAIL_LOAN)) {
			partition = JsonConstants.RequestData.RETAIL_LOAN_PARTITION;
		} else if (prodType.equalsIgnoreCase(JsonConstants.RequestData.CREDIT_CARD)) {
			partition = JsonConstants.RequestData.CREDIT_CARD_PARTITION;
		} else if (prodType.equalsIgnoreCase(JsonConstants.RequestData.AGRI_LOAN)) {
			partition = JsonConstants.RequestData.AGRI_LOAN_PARTITION;
		} else if (prodType.equalsIgnoreCase(JsonConstants.RequestData.MSME_FINSERVE)) {
			partition = JsonConstants.RequestData.MSME_FINSERVE_PARTITION;
		} else {
			partition = JsonConstants.RequestData.DEFAULT_PARTITION;
		}

		try {

			log.error("--- before msg sent ----");
			//kafkaProducer.send(new ProducerRecord<>("main_topic", partition, prodType, request));

			log.error("--- Message sent successfully on main collection topic ----");

		} catch (Exception e) {

			log.error("--Exception occured while producing message on main topic----", e);

		}

	}

}

//kafka code end