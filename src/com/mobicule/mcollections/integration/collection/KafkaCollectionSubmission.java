package com.mobicule.mcollections.integration.collection;
/*
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
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
import com.mobicule.mcollections.webservice.adapter.KotakCollectionWebserviceAdapter;*/

//@Controller
public class KafkaCollectionSubmission {
	/*
	 * private Logger log = LoggerFactory.getLogger(getClass());
	 * 
	 * @Autowired private UserActivityService userActivityService;
	 * 
	 * @Autowired private SystemUserService systemUserService;
	 * 
	 * @Autowired private CommunicationActivityService communicationActivityService;
	 * 
	 * @Autowired private NotificationActivityService notificationActivityService;
	 * 
	 * @Autowired private CollectionService collectionService;
	 * 
	 * @Autowired private CaseService caseService;
	 * 
	 * @Autowired private AgencyService agencyService;
	 * 
	 * @Autowired ApplicationConfiguration applicationConfiguration;
	 * 
	 * @Autowired private SimpleMailMessage
	 * simpleMailMessageForCashPaymentCreditCard;
	 * 
	 * @Autowired private SimpleMailMessage simpleMailMessageForCashPaymentLoan;
	 * 
	 * @Autowired private SimpleMailMessage
	 * simpleMailMessageForChequePaymentCreditCard;
	 * 
	 * @Autowired private SimpleMailMessage simpleMailMessageForChequePaymentLoan;
	 * 
	 * @Autowired private SimpleMailMessage simpleMailMessageForCardPayment;
	 * 
	 * @Autowired private SimpleMailMessage simpleMailMessageForDDPDC;
	 * 
	 * private EmailUtilities emailService;
	 * 
	 * @Autowired private OfflineSMSService offlineSMSService;
	 * 
	 * public EmailUtilities getEmailService() { return emailService; }
	 * 
	 * public void setEmailService(EmailUtilities emailService) { this.emailService
	 * = emailService; }
	 * 
	 * private void sendSms(Collection collection, SystemUser systemUserNew) {
	 * log.info("------- Before Sending SMS  --------" + collection); if
	 * (collection.getMobileNumber() != null &&
	 * !collection.getMobileNumber().equalsIgnoreCase(Constants.EMPTY_STRING)) {
	 * log.info("Sending sms to customer mobile number");
	 * 
	 * callSMSDispatcher(collection, collection.getMobileNumber(), systemUserNew); }
	 * 
	 * if (collection.getMobileNumberNew() != null &&
	 * !collection.getMobileNumberNew().equalsIgnoreCase(Constants.EMPTY_STRING) &&
	 * !collection.getMobileNumberNew().equalsIgnoreCase(collection.getMobileNumber(
	 * ))) { log.info("Sending sms to customer alternate mobile number ");
	 * 
	 * callSMSDispatcher(collection, collection.getMobileNumber(), systemUserNew); }
	 * 
	 * if (systemUserNew.getMobileNumber() != null &&
	 * !systemUserNew.getMobileNumber().equalsIgnoreCase(Constants.EMPTY_STRING)) {
	 * log.info("Sending sms to FE mobile number ");
	 * 
	 * generateSMSDispatcherMapForFE(collection, systemUserNew.getMobileNumber(),
	 * systemUserNew);
	 * 
	 * }
	 * 
	 * }
	 * 
	 * private void generateSMSDispatcherMapForFE(Collection collection, String
	 * mobileNumber, SystemUser systemUserNew) {
	 * log.info("---- Inside callSMSDispatcher --------"); try { String
	 * webserviceUrl = (String)
	 * applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");
	 * 
	 * Map<String, Object> smsDispatcherMap;
	 * 
	 * smsDispatcherMap =
	 * ServerUtilities.generateSMSDispatcherMapFEForNonRTP(collection,
	 * mobileNumber);
	 * 
	 * StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true,
	 * new HashMap<String, String>());
	 * 
	 * log.info("----- xmlRequest : -------" + xmlRequest);
	 * 
	 * CommunicationActivityAddition communicationActivityAddition = new
	 * CommunicationActivityAddition( systemUserNew.getUserTableId().toString(),
	 * systemUserNew.getImeiNo(), (collection.getAppl() + "_" +
	 * collection.getCollectionType()), webserviceUrl, xmlRequest.toString(),
	 * communicationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);
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
	 * log.info("----- Failure in sending SMS : -------"); } } catch (ParseException
	 * e) { // TODO Auto-generated catch block e.printStackTrace(); } }
	 * 
	 * private void callSMSDispatcher(Collection collection, String mobileNumber,
	 * SystemUser systemUserNew) {
	 * log.info("---- Inside callSMSDispatcher --------"); try { String
	 * webserviceUrl = (String)
	 * applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");
	 * 
	 * Map<String, Object> smsDispatcherMap =
	 * ServerUtilities.generateSMSDispatcherMapForNonRTP(collection, mobileNumber);
	 * 
	 * StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true,
	 * new HashMap<String, String>());
	 * 
	 * log.info("----- xmlRequest : -------" + xmlRequest);
	 * 
	 * CommunicationActivityAddition communicationActivityAddition = new
	 * CommunicationActivityAddition( systemUserNew.getUserTableId().toString(),
	 * systemUserNew.getImeiNo(), (collection.getAppl() + "_" +
	 * collection.getCollectionType()), webserviceUrl, xmlRequest.toString(),
	 * communicationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);
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
	 * log.info("----- Failure in sending SMS : -------"); } } catch (ParseException
	 * e) { // TODO Auto-generated catch block e.printStackTrace(); } }
	 * 
	 * public void extractCashDetails(String pan, JSONObject data, Collection
	 * collection) throws JSONException { JSONObject cashDetail = new JSONObject();
	 * 
	 * cashDetail = (JSONObject) data.get(JsonConstants.RequestData.CASH);
	 * 
	 * //before docType Error
	 * //collection.setDocType(cashDetail.get(JsonConstants.DOCUMENT_TYPE).toString(
	 * )); if(cashDetail.has(JsonConstants.DOCUMENT_TYPE)) {
	 * collection.setDocType(cashDetail.get(JsonConstants.DOCUMENT_TYPE).toString())
	 * ; }
	 * 
	 * //before docRef Error
	 * //collection.setDocRef(cashDetail.get(JsonConstants.DOCUMENT_REFERENCE).
	 * toString()); if(cashDetail.has(JsonConstants.DOCUMENT_REFERENCE)) {
	 * collection.setDocRef(cashDetail.get(JsonConstants.DOCUMENT_REFERENCE).
	 * toString()); }
	 * 
	 * if (collection.getDocType() != null &&
	 * collection.getDocType().equalsIgnoreCase("PAN")) {
	 * collection.setPan(cashDetail.get(JsonConstants.DOCUMENT_REFERENCE).toString()
	 * ); } if (collection.getDocType() != null &&
	 * collection.getDocType().equalsIgnoreCase("F60")) { log.info("@@ inside f60");
	 * 
	 * // pan = pan + "FORM60"; collection.setPan("FORM60");
	 * collection.setDocRef("FORM60");
	 * 
	 * }
	 * 
	 * if(cashDetail.has(JsonConstants.DENOMINATION)) { JSONArray denominationArray
	 * = cashDetail.getJSONArray(JsonConstants.DENOMINATION);
	 * 
	 * List<Denomination> denominationList = new ArrayList<Denomination>();
	 * 
	 * for (int i = 0; i < (denominationArray.length()); i++) { JSONObject cashJSON
	 * = denominationArray.getJSONObject(i);
	 * 
	 * Denomination denomination = new Denomination();
	 * 
	 * denomination.setNote(cashJSON.get(JsonConstants.DENOMINATION_NOTE) == null ||
	 * cashJSON.get(JsonConstants.DENOMINATION_NOTE).toString().equalsIgnoreCase(
	 * Constants.EMPTY_STRING) ? "0" :
	 * cashJSON.get(JsonConstants.DENOMINATION_NOTE).toString()); denomination
	 * .setNoteCount(Integer.parseInt(cashJSON.get(JsonConstants.DENOMINATION_COUNT)
	 * == null || cashJSON
	 * .get(JsonConstants.DENOMINATION_COUNT).toString().equalsIgnoreCase(Constants.
	 * EMPTY_STRING) ? "0" :
	 * cashJSON.get(JsonConstants.DENOMINATION_COUNT).toString()));
	 * 
	 * denominationList.add(denomination); }
	 * collection.setDenomination(denominationList);
	 * 
	 * }
	 * 
	 * 
	 * if (cashDetail.has(JsonConstants.RequestData.PAN)) { pan = (String)
	 * cashDetail.get(JsonConstants.RequestData.PAN); }
	 * 
	 * if (cashDetail.has(JsonConstants.INSTRUMENT_DATE)) {
	 * collection.setInstDate((String)
	 * cashDetail.get(JsonConstants.INSTRUMENT_DATE)); } }
	 * 
	 * private Message<String> responseBuilder(Message<String> message, String
	 * status, String returnMessage, String reqId) throws JSONException { JSONObject
	 * responseJSON = new JSONObject(); JSONObject data = new JSONObject();
	 * 
	 * data.put("reqId", reqId);
	 * 
	 * responseJSON.put(JsonConstants.STATUS, status);
	 * responseJSON.put(JsonConstants.MESSAGE, returnMessage);
	 * responseJSON.put(JsonConstants.DATA, data);
	 * 
	 * return
	 * MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.
	 * getHeaders()).build(); }
	 * 
	 * private List<Cheque> getCheques(SystemUser systemUser, JSONObject data)
	 * throws JSONException { JSONArray chequeDetails = new JSONArray();
	 * 
	 * if (data.has(JsonConstants.RequestData.CHEQUE)) { chequeDetails = (JSONArray)
	 * data.get(JsonConstants.RequestData.CHEQUE); }
	 * 
	 * List<Cheque> cheques = new ArrayList<Cheque>(); JSONObject chequeDetail = new
	 * JSONObject(); Cheque cheque = null;
	 * 
	 * try { for (int index = 0; index < chequeDetails.length(); index++) {
	 * chequeDetail = (JSONObject) chequeDetails.get(index); cheque = new Cheque();
	 * 
	 * String chequeAmt = "0"; String chequeDate = Constants.EMPTY_STRING; String
	 * micr = Constants.EMPTY_STRING; String chequeNumber = Constants.EMPTY_STRING;
	 * String bankName = Constants.EMPTY_STRING; String branch =
	 * Constants.EMPTY_STRING; String drawerAccountNumber = Constants.EMPTY_STRING;
	 * if (chequeDetail.has(JsonConstants.DRAWER_ACCOUNT_NUMBER)) {
	 * drawerAccountNumber = (String)
	 * chequeDetail.get(JsonConstants.DRAWER_ACCOUNT_NUMBER); }
	 * 
	 * if (chequeDetail.has(JsonConstants.RequestData.AMOUNT)) { chequeAmt =
	 * (String) chequeDetail.get(JsonConstants.RequestData.AMOUNT); }
	 * 
	 * if (chequeDetail.has(JsonConstants.CHEQUE_DATE)) { chequeDate = (String)
	 * chequeDetail.get(JsonConstants.CHEQUE_DATE); }
	 * 
	 * if (chequeDetail.has(JsonConstants.RequestData.MICR)) { micr = (String)
	 * chequeDetail.get(JsonConstants.RequestData.MICR); }
	 * 
	 * if (chequeDetail.has(JsonConstants.RequestData.CHEQUE_NUMBER)) { chequeNumber
	 * = (String) chequeDetail.get(JsonConstants.RequestData.CHEQUE_NUMBER); }
	 * 
	 * if (chequeDetail.has(JsonConstants.RequestData.BANK_NAME)) { bankName =
	 * (String) chequeDetail.get(JsonConstants.RequestData.BANK_NAME); }
	 * 
	 * if (chequeDetail.has(JsonConstants.RequestData.BRANCH)) { branch = (String)
	 * chequeDetail.get(JsonConstants.RequestData.BRANCH); }
	 * 
	 * cheque.setAmount(Double.parseDouble(chequeDetail.getString(JsonConstants.
	 * AMOUNT) == null ||
	 * chequeDetail.getString(JsonConstants.AMOUNT).equalsIgnoreCase(Constants.
	 * EMPTY_STRING) ? "0.0" : chequeDetail.getString(JsonConstants.AMOUNT)));
	 * cheque.setChequeDate(chequeDate); cheque.setChequeNo(chequeNumber);
	 * cheque.setMicrCode(micr); cheque.setDepositStatus(Constants.EMPTY_STRING);
	 * cheque.setDepositDate(Constants.EMPTY_STRING); cheque.setBankName(bankName);
	 * cheque.setBranch(branch); cheque.setDrawerAccountNumber(drawerAccountNumber);
	 * Utilities.primaryBeanSetter(cheque, systemUser); cheques.add(cheque); }
	 * return cheques; } catch (Exception e) { e.printStackTrace(); return new
	 * ArrayList<Cheque>(); } }
	 * 
	 * private List<Image> getImages(SystemUser systemUser, JSONArray imageDetails,
	 * Collection collection) throws JSONException {
	 * 
	 * JSONObject imageDetail = new JSONObject(); Image image = null; String
	 * imagePath = null;
	 * 
	 * List<Image> images = new ArrayList<Image>();
	 * 
	 * for (int index = 0; index < imageDetails.length(); index++) {
	 * 
	 * imageDetail = (JSONObject) imageDetails.get(index);
	 * 
	 * if (!imageDetail.has(JsonConstants.RequestData.IMAGE)) {
	 * 
	 * return new ArrayList<Image>(); }
	 * 
	 * String imageByteArray = (String)
	 * imageDetail.get(JsonConstants.RequestData.IMAGE); if
	 * (imageByteArray.isEmpty()) {
	 * 
	 * return new ArrayList<Image>(); } image = new Image();
	 * 
	 * imagePath = (extractImagePath(collection, imageByteArray,
	 * Constants.IMAGE_FILE_PATH, (String.valueOf(index))));
	 * 
	 * if (imagePath.equals(JsonConstants.ERROR)) {
	 * 
	 * return null; } else {
	 * 
	 * image = new Image(); image.setPath(imagePath);
	 * Utilities.primaryBeanSetter(image, systemUser); images.add(image);
	 * 
	 * } } return images; }
	 * 
	 * private String extractImagePath(Collection collection, String type, String
	 * entity, String index) { try {
	 * 
	 * String fileName = collection.getCaseId() + Constants.SYMBOL_UNDERSCORE +
	 * collection.getReceiptNumber() + Constants.SYMBOL_UNDERSCORE +
	 * System.currentTimeMillis();
	 * 
	 * String filePath = Constants.EMPTY_STRING;
	 * 
	 * if (index.equals(Constants.EMPTY_STRING)) {
	 * 
	 * filePath = Utilities.generateFilePath((String)
	 * applicationConfiguration.getValue(entity), fileName); } else {
	 * 
	 * filePath = Utilities.generateFilePath((String)
	 * applicationConfiguration.getValue(entity), (fileName + "_" + index)); }
	 * 
	 * if (Utilities.writeImage(filePath, type)) {
	 * 
	 * return filePath; } else { return (JsonConstants.ERROR); } } catch (Exception
	 * e) { e.printStackTrace(); return (JsonConstants.ERROR); } }
	 * 
	 * private void sendCollectionsSms(Collection collection, SystemUser user) {
	 * 
	 * log.info("------- IN Integration , Before Sending SMS  --------"); try {
	 * 
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
	 * 
	 * String webserviceUrl = Constants.EMPTY_STRING; webserviceUrl = (String)
	 * applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");
	 * 
	 * Collections acknowledgment SMS Map<String, Object> parametesMap = new
	 * HashMap<String, Object>(); Map<String, Object> parametersMaps = new
	 * HashMap<String, Object>(); Map<String, Object> smsDispatcherMap = new
	 * HashMap<String, Object>(); try {
	 * 
	 * smsDispatcherMap = SmsFormXML.generateCollectionSmsXml(parametersMaps,
	 * collection); } catch (Exception e) { log.info("Exception :- " + e); }
	 * 
	 * 
	 * StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true,
	 * new HashMap<String, String>());
	 * 
	 * Map<String, Object> createUserParamMap = new HashMap<String, Object>();
	 * 
	 * String url = (String)
	 * (applicationConfiguration.getValue(Constants.smsParam.SMS_EMAIL_URL) == null
	 * ? Constants.EMPTY_STRING :
	 * applicationConfiguration.getValue(Constants.smsParam.SMS_EMAIL_URL));
	 * createUserParamMap.put(Constants.LdapParam.LDAP_URL, url);
	 * createUserParamMap.put(Constants.LdapParam.LDAPREQUEST,
	 * smsDispatcherMap.get(Constants.smsParam.REQUESTHEDER) == null ?
	 * Constants.EMPTY_STRING :
	 * smsDispatcherMap.get(Constants.smsParam.REQUESTHEDER));
	 * 
	 * try { String responseXml = Utilities.postXML(createUserParamMap); String
	 * smsEmailType = parametersMaps.get(Constants.SMSEMAILTYPE) == null ?
	 * Constants.EMPTY_STRING : (String) parametersMaps.get(Constants.SMSEMAILTYPE);
	 * 
	 * parametesMap.put(Constants.REQUEST, createUserParamMap == null ?
	 * Constants.EMPTY_STRING : createUserParamMap);
	 * parametesMap.put(Constants.RESPONSE, responseXml == null ?
	 * Constants.EMPTY_STRING : responseXml);
	 * parametesMap.put(Constants.SMSEMAILURL, url);
	 * parametesMap.put(Constants.SMSEMAILTYPE, Constants.SMS);
	 * 
	 * systemUserService.getInsertUpdateSmsEmailActivity(parametesMap, user,
	 * communicationActivityService, collection);
	 * 
	 * log.info("----- responseXml : -------" + responseXml); } catch (Exception e)
	 * { log.info("Response :- " + e); }
	 * 
	 * } catch (Exception e) { log.
	 * info("There is some error occured while sending sms to customer.In Integration"
	 * + e); }
	 * 
	 * if (!StringUtils.isEmpty(collection.getMobileNumberNew())) { try { String
	 * webserviceUrl = Constants.EMPTY_STRING; webserviceUrl = (String)
	 * applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");
	 * 
	 * Collections acknowledgment SMS Map<String, Object> parametesMap = new
	 * HashMap<String, Object>(); Map<String, Object> parametersMaps = new
	 * HashMap<String, Object>(); Map<String, Object> smsDispatcherMap = new
	 * HashMap<String, Object>(); try {
	 * 
	 * smsDispatcherMap =
	 * SmsFormXML.generateCollectionAlterMobiSmsXml(parametersMaps, collection); }
	 * catch (Exception e) { log.info("Exception :- " + e); }
	 * 
	 * 
	 * StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true,
	 * new HashMap<String, String>());
	 * 
	 * Map<String, Object> createUserParamMap = new HashMap<String, Object>();
	 * 
	 * String url = (String)
	 * (applicationConfiguration.getValue(Constants.smsParam.SMS_EMAIL_URL) == null
	 * ? Constants.EMPTY_STRING :
	 * applicationConfiguration.getValue(Constants.smsParam.SMS_EMAIL_URL));
	 * createUserParamMap.put(Constants.LdapParam.LDAP_URL, url);
	 * createUserParamMap.put(Constants.LdapParam.LDAPREQUEST,
	 * smsDispatcherMap.get(Constants.smsParam.REQUESTHEDER) == null ?
	 * Constants.EMPTY_STRING :
	 * smsDispatcherMap.get(Constants.smsParam.REQUESTHEDER));
	 * 
	 * try { String responseXml = Utilities.postXML(createUserParamMap); String
	 * smsEmailType = parametersMaps.get(Constants.SMSEMAILTYPE) == null ?
	 * Constants.EMPTY_STRING : (String) parametersMaps.get(Constants.SMSEMAILTYPE);
	 * 
	 * parametesMap.put(Constants.REQUEST, createUserParamMap == null ?
	 * Constants.EMPTY_STRING : createUserParamMap);
	 * parametesMap.put(Constants.RESPONSE, responseXml == null ?
	 * Constants.EMPTY_STRING : responseXml);
	 * parametesMap.put(Constants.SMSEMAILURL, url);
	 * parametesMap.put(Constants.SMSEMAILTYPE, Constants.SMS);
	 * 
	 * systemUserService.getInsertUpdateSmsEmailActivity(parametesMap, user,
	 * communicationActivityService, collection);
	 * 
	 * log.info("----- responseXml : -------" + responseXml); } catch (Exception e)
	 * { log.info("Response :- " + e); } } catch (Exception e) {
	 * log.info("Response :- " + e); } }
	 * 
	 * }
	 * 
	 * private void callSMSDispatcher(String amount, String receiptNumber, String
	 * paymentType, String mobileNumber, String type, String apacCardNumber,
	 * SystemUser user, CommunicationActivityService communicationActivityService,
	 * Collection collection) { log.info("---- Inside callSMSDispatcher --------");
	 * 
	 * String webserviceUrl = Constants.EMPTY_STRING;
	 * 
	 * if (type.equalsIgnoreCase("RSM")) {
	 * 
	 * webserviceUrl = (String)
	 * applicationConfiguration.getValue("RSM_WEB_SERVICE_URL_SMS_DISPATCHER"); }
	 * 
	 * else {
	 * 
	 * webserviceUrl = (String)
	 * applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");
	 * 
	 * }
	 * 
	 * Collections acknowledgment SMS
	 * 
	 * Map<String, Object> smsDispatcherMap;
	 * 
	 * if (paymentType.equalsIgnoreCase("ORI")) { smsDispatcherMap =
	 * ServerUtilities.generateSMSDispatcherMapForDebit(amount, receiptNumber,
	 * paymentType, mobileNumber, type, apacCardNumber, collection); } else {
	 * smsDispatcherMap = ServerUtilities.generateSMSDispatcherMap(amount,
	 * receiptNumber, paymentType, mobileNumber, type, apacCardNumber); }
	 * 
	 * StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true,
	 * new HashMap<String, String>());
	 * 
	 * log.info("----- xmlRequest : -------" + xmlRequest);
	 * 
	 * 
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
	 * 
	 * OLD Denomination acknowledgment SMS
	 * 
	 * 
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
	 * 
	 * }
	 * 
	 * private void generateSMSDispatcherMapForFE(String amount, String
	 * receiptNumber, String paymentType, String mobileNumber, String type, String
	 * feName, SystemUser user, CommunicationActivityService
	 * communicationActivityService, Collection collection) {
	 * log.info("---- Inside generateSMSDispatcherMapForFE --------");
	 * 
	 * String webserviceUrl = (String)
	 * applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");
	 * 
	 * Map<String, Object> smsDispatcherMap =
	 * ServerUtilities.generateSMSDispatcherMapForFE(amount, receiptNumber,
	 * paymentType, mobileNumber, type, feName);
	 * 
	 * StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true,
	 * new HashMap<String, String>());
	 * 
	 * log.info("----- xmlRequest : -------" + xmlRequest);
	 * 
	 * CommunicationActivityAddition communicationActivityAddition = new
	 * CommunicationActivityAddition( user.getUserTableId().toString(),
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
	 * log.info("----- Failure in sending SMS : -------"); } }
	 * 
	 * private void callEmailService(Collection collection) { try { if
	 * (collection.getEmailAddress().equals(Constants.EMPTY_STRING) &&
	 * collection.getEmailAddressNew().equals(Constants.EMPTY_STRING)) {
	 * log.info(" -------- No Email Address found for Collection -------- "); } else
	 * { log.info("--- Sending Email ---");
	 * 
	 * String payMode = collection.getPaymentMode();
	 * log.info(" -------- payMode -------- " + payMode);
	 * 
	 * if (payMode.equals(Constants.PAYMENT_MODE_CASH)) {
	 * sendEmailForCashPayment(collection); }
	 * 
	 * if (payMode.equals(Constants.PAYMENT_MODE_CHEQUE)) {
	 * sendEmailForChequePayment(collection); }
	 * 
	 * if (payMode.equalsIgnoreCase(Constants.PAYMENT_MODE_PDC) ||
	 * payMode.equalsIgnoreCase(Constants.PAYMENT_MODE_DRAFT)) {
	 * sendEmailForDDPDC(collection); } } } catch (Exception e) {
	 * log.info("-------Error Occured in sending Email---------", e);
	 * e.printStackTrace(); } }
	 * 
	 * private void sendEmailForCashPayment(Collection collection) throws
	 * ParseException { log.info("---inside sendEmailForCashPayment---");
	 * 
	 * String paymentDate = collection.getDeviceDate();
	 * log.info("---payment date---" + paymentDate);
	 * 
	 * SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
	 * SimpleDateFormat format2 = new SimpleDateFormat("dd-MMM-yyyy"); Date date =
	 * format1.parse(paymentDate); paymentDate = format2.format(date);
	 * 
	 * log.info("---payment date after parsing---" + paymentDate);
	 * 
	 * // String email = collection.getEmailAddress();
	 * 
	 * String emailText = Constants.EMPTY_STRING;
	 * 
	 * if (!collection.getAppl().isEmpty() &&
	 * collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD)) {
	 * log.info("---inside if condition---"); log.info("----email text----" +
	 * simpleMailMessageForCashPaymentCreditCard.getText()); emailText =
	 * String.format(simpleMailMessageForCashPaymentCreditCard.getText(),
	 * collection.getName(), getFullFormApplType(collection.getAppl()),
	 * collection.getBusinessPartnerNumber(), collection.getReceiptNumber(),
	 * collection.getAppropriateAmount() + Constants.EMPTY_STRING,
	 * Constants.EMPTY_STRING + paymentDate, collection.getBusinessPartnerNumber(),
	 * getTollFreeNumberForAppl(collection.getAppl()));
	 * log.info("----emailTest for card ----" + emailText);
	 * 
	 * } else { log.info("--- inside else----");
	 * log.info("--- simpleMailMessageForCashPaymentLoan.getText() ----" +
	 * simpleMailMessageForCashPaymentLoan.getText());
	 * log.info("---- collection.getName()-----" + collection.getName());
	 * log.info("---- getTollFreeNumberForAppl-----" +
	 * getTollFreeNumberForAppl(collection.getAppl()));
	 * 
	 * emailText = String.format(simpleMailMessageForCashPaymentLoan.getText(),
	 * collection.getName(), getFullFormApplType(collection.getAppl()),
	 * collection.getBusinessPartnerNumber(), collection.getReceiptNumber(),
	 * collection.getAppropriateAmount() + Constants.EMPTY_STRING,
	 * Constants.EMPTY_STRING + paymentDate, collection.getBusinessPartnerNumber(),
	 * getTollFreeNumberForAppl(collection.getAppl()));
	 * log.info("---- email text is -----" + emailText);
	 * 
	 * }
	 * 
	 * String email = collection.getEmailAddress() != null ?
	 * collection.getEmailAddress() : Constants.EMPTY_STRING;
	 * 
	 * log.info("---email----" + email);
	 * 
	 * if (collection.getEmailAddress() != null &&
	 * !collection.getEmailAddress().equalsIgnoreCase(Constants.EMPTY_STRING)) {
	 * email = collection.getEmailAddress(); log.info("---inside if email----" +
	 * email);
	 * 
	 * if (!collection.getAppl().isEmpty() &&
	 * collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD)) { List<String>
	 * senderList = new ArrayList<String>();
	 * senderList.add(simpleMailMessageForCashPaymentCreditCard.getFrom());
	 * 
	 * List<String> receiverList = new ArrayList<String>(); receiverList.add(email);
	 * 
	 * NotificationActivityAddition notificationActivityAddition = new
	 * NotificationActivityAddition(
	 * collection.getUser().getUserTableId().toString(),
	 * ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList, receiverList,
	 * simpleMailMessageForCashPaymentCreditCard.getSubject(), emailText,
	 * notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL); new
	 * Thread(notificationActivityAddition).run();
	 * 
	 * if (emailService.sendMail(email,
	 * simpleMailMessageForCashPaymentCreditCard.getFrom(),
	 * simpleMailMessageForCashPaymentCreditCard.getSubject(), emailText)) {
	 * NotificationActivity notificationActivity = notificationActivityAddition
	 * .extractNotificationActivity(); NotificationActivityStatusUpdate
	 * notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
	 * notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS),
	 * notificationActivityService); new
	 * Thread(notificationActivityStatusUpdate).run(); } else { NotificationActivity
	 * notificationActivity = notificationActivityAddition
	 * .extractNotificationActivity(); NotificationActivityStatusUpdate
	 * notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
	 * notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE),
	 * notificationActivityService); new
	 * Thread(notificationActivityStatusUpdate).run(); } } else { List<String>
	 * senderList = new ArrayList<String>();
	 * senderList.add(simpleMailMessageForCashPaymentLoan.getFrom());
	 * log.info("simpleMailMessageForCashPaymentLoan.getFrom()" +
	 * simpleMailMessageForCashPaymentLoan.getFrom());
	 * 
	 * log.info("adding string data into reciverList" + email); List<String>
	 * receiverList = new ArrayList<String>(); receiverList.add(email);
	 * 
	 * log.info("collection.getUser().getUserTableId()" + collection.getUser());
	 * NotificationActivityAddition notificationActivityAddition = new
	 * NotificationActivityAddition(
	 * collection.getUser().getUserTableId().toString(),
	 * ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList, receiverList,
	 * simpleMailMessageForCashPaymentLoan.getSubject(), emailText,
	 * notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);
	 * 
	 * log.info("notificationActivityAddition" + notificationActivityAddition); new
	 * Thread(notificationActivityAddition).run();
	 * 
	 * if (emailService == null) { log.info("emailService is null"); } else {
	 * log.info("emailService is not null"); log.info("email is : " + email); } if
	 * (emailService.sendMail(email, simpleMailMessageForCashPaymentLoan.getFrom(),
	 * simpleMailMessageForCashPaymentLoan.getSubject(), emailText)) {
	 * NotificationActivity notificationActivity = notificationActivityAddition
	 * .extractNotificationActivity(); NotificationActivityStatusUpdate
	 * notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
	 * notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS),
	 * notificationActivityService); new
	 * Thread(notificationActivityStatusUpdate).run(); } else { NotificationActivity
	 * notificationActivity = notificationActivityAddition
	 * .extractNotificationActivity(); NotificationActivityStatusUpdate
	 * notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
	 * notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE),
	 * notificationActivityService); new
	 * Thread(notificationActivityStatusUpdate).run(); } } }
	 * 
	 * if (collection.getEmailAddressNew() != null &&
	 * !collection.getEmailAddressNew().equalsIgnoreCase(Constants.EMPTY_STRING) &&
	 * !collection.getEmailAddressNew().equalsIgnoreCase(email)) { email =
	 * collection.getEmailAddressNew(); if (!collection.getAppl().isEmpty() &&
	 * collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD)) { List<String>
	 * senderList = new ArrayList<String>();
	 * senderList.add(simpleMailMessageForCashPaymentCreditCard.getFrom());
	 * 
	 * List<String> receiverList = new ArrayList<String>(); receiverList.add(email);
	 * 
	 * NotificationActivityAddition notificationActivityAddition = new
	 * NotificationActivityAddition(
	 * collection.getUser().getUserTableId().toString(),
	 * ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList, receiverList,
	 * simpleMailMessageForCashPaymentCreditCard.getSubject(), emailText,
	 * notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL); new
	 * Thread(notificationActivityAddition).run();
	 * 
	 * if (emailService.sendMail(collection.getEmailAddressNew(),
	 * simpleMailMessageForCashPaymentCreditCard.getFrom(),
	 * simpleMailMessageForCashPaymentCreditCard.getSubject(), emailText)) {
	 * NotificationActivity notificationActivity = notificationActivityAddition
	 * .extractNotificationActivity(); NotificationActivityStatusUpdate
	 * notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
	 * notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS),
	 * notificationActivityService); new
	 * Thread(notificationActivityStatusUpdate).run(); } else { NotificationActivity
	 * notificationActivity = notificationActivityAddition
	 * .extractNotificationActivity(); NotificationActivityStatusUpdate
	 * notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
	 * notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE),
	 * notificationActivityService); new
	 * Thread(notificationActivityStatusUpdate).run(); } } else { List<String>
	 * senderList = new ArrayList<String>();
	 * senderList.add(simpleMailMessageForCashPaymentLoan.getFrom());
	 * 
	 * List<String> receiverList = new ArrayList<String>(); receiverList.add(email);
	 * 
	 * NotificationActivityAddition notificationActivityAddition = new
	 * NotificationActivityAddition(
	 * collection.getUser().getUserTableId().toString(),
	 * ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList, receiverList,
	 * simpleMailMessageForCashPaymentLoan.getSubject(), emailText,
	 * notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL); new
	 * Thread(notificationActivityAddition).run();
	 * 
	 * if (emailService.sendMail(collection.getEmailAddressNew(),
	 * simpleMailMessageForCashPaymentLoan.getFrom(),
	 * simpleMailMessageForCashPaymentLoan.getSubject(), emailText)) {
	 * NotificationActivity notificationActivity = notificationActivityAddition
	 * .extractNotificationActivity(); NotificationActivityStatusUpdate
	 * notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
	 * notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS),
	 * notificationActivityService); new
	 * Thread(notificationActivityStatusUpdate).run(); } else { NotificationActivity
	 * notificationActivity = notificationActivityAddition
	 * .extractNotificationActivity(); NotificationActivityStatusUpdate
	 * notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
	 * notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE),
	 * notificationActivityService); new
	 * Thread(notificationActivityStatusUpdate).run(); } } } }
	 * 
	 * private void sendEmailForChequePayment(Collection collection) throws
	 * ParseException { String paymentDate = collection.getDeviceDate();
	 * SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
	 * SimpleDateFormat format2 = new SimpleDateFormat("dd-MMM-yyyy"); Date date =
	 * format1.parse(paymentDate); paymentDate = format2.format(date);
	 * 
	 * String email = collection.getEmailAddress() != null ?
	 * collection.getEmailAddress() : Constants.EMPTY_STRING; ; String
	 * chequeDetailString = Constants.EMPTY_STRING;
	 * 
	 * NumberFormat formatter = NumberFormat.getInstance(new Locale("en", "IN"));
	 * 
	 * for (Cheque cheque : collection.getChequeDetails()) { chequeDetailString =
	 * chequeDetailString + " Cheque No." + cheque.getChequeNo() + "     dated" +
	 * cheque.getChequeDate(); }
	 * 
	 * String emailText = Constants.EMPTY_STRING; if
	 * (!collection.getAppl().isEmpty() &&
	 * collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD)) { emailText =
	 * String.format(simpleMailMessageForChequePaymentCreditCard.getText(),
	 * collection.getName(), getFullFormApplType(collection.getAppl()),
	 * collection.getBusinessPartnerNumber(), collection.getReceiptNumber(),
	 * collection.getAppropriateAmount() + Constants.EMPTY_STRING,
	 * Constants.EMPTY_STRING + paymentDate, Constants.EMPTY_STRING +
	 * chequeDetailString, collection.getBusinessPartnerNumber(),
	 * getTollFreeNumberForAppl(collection.getAppl())); } else { emailText =
	 * String.format(simpleMailMessageForChequePaymentLoan.getText(),
	 * collection.getName(), getFullFormApplType(collection.getAppl()),
	 * collection.getBusinessPartnerNumber(), collection.getReceiptNumber(),
	 * collection.getAppropriateAmount() + Constants.EMPTY_STRING,
	 * Constants.EMPTY_STRING + paymentDate, Constants.EMPTY_STRING +
	 * chequeDetailString, collection.getBusinessPartnerNumber(),
	 * getTollFreeNumberForAppl(collection.getAppl())); }
	 * 
	 * if (collection.getEmailAddress() != null &&
	 * !collection.getEmailAddress().equalsIgnoreCase(Constants.EMPTY_STRING)) { if
	 * (!collection.getAppl().isEmpty() &&
	 * collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD)) { List<String>
	 * senderList = new ArrayList<String>();
	 * senderList.add(simpleMailMessageForChequePaymentCreditCard.getFrom());
	 * 
	 * List<String> receiverList = new ArrayList<String>(); receiverList.add(email);
	 * 
	 * NotificationActivityAddition notificationActivityAddition = new
	 * NotificationActivityAddition(
	 * collection.getUser().getUserTableId().toString(),
	 * ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList, receiverList,
	 * simpleMailMessageForChequePaymentCreditCard.getSubject(), emailText,
	 * notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL); new
	 * Thread(notificationActivityAddition).run();
	 * 
	 * if (emailService.sendMail(email,
	 * simpleMailMessageForChequePaymentCreditCard.getFrom(),
	 * simpleMailMessageForChequePaymentCreditCard.getSubject(), emailText)) {
	 * NotificationActivity notificationActivity = notificationActivityAddition
	 * .extractNotificationActivity(); NotificationActivityStatusUpdate
	 * notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
	 * notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS),
	 * notificationActivityService); new
	 * Thread(notificationActivityStatusUpdate).run(); } else { NotificationActivity
	 * notificationActivity = notificationActivityAddition
	 * .extractNotificationActivity(); NotificationActivityStatusUpdate
	 * notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
	 * notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE),
	 * notificationActivityService); new
	 * Thread(notificationActivityStatusUpdate).run(); } } else { List<String>
	 * senderList = new ArrayList<String>();
	 * senderList.add(simpleMailMessageForChequePaymentLoan.getFrom());
	 * 
	 * List<String> receiverList = new ArrayList<String>(); receiverList.add(email);
	 * 
	 * NotificationActivityAddition notificationActivityAddition = new
	 * NotificationActivityAddition(
	 * collection.getUser().getUserTableId().toString(),
	 * ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList, receiverList,
	 * simpleMailMessageForChequePaymentLoan.getSubject(), emailText,
	 * notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL); new
	 * Thread(notificationActivityAddition).run();
	 * 
	 * if (emailService.sendMail(email,
	 * simpleMailMessageForChequePaymentLoan.getFrom(),
	 * simpleMailMessageForChequePaymentLoan.getSubject(), emailText)) {
	 * NotificationActivity notificationActivity = notificationActivityAddition
	 * .extractNotificationActivity(); NotificationActivityStatusUpdate
	 * notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
	 * notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS),
	 * notificationActivityService); new
	 * Thread(notificationActivityStatusUpdate).run(); } else { NotificationActivity
	 * notificationActivity = notificationActivityAddition
	 * .extractNotificationActivity(); NotificationActivityStatusUpdate
	 * notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
	 * notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE),
	 * notificationActivityService); new
	 * Thread(notificationActivityStatusUpdate).run(); } } }
	 * 
	 * if (collection.getEmailAddressNew() != null &&
	 * !collection.getEmailAddressNew().equalsIgnoreCase(Constants.EMPTY_STRING) &&
	 * !collection.getEmailAddressNew().equalsIgnoreCase(email)) { if
	 * (!collection.getAppl().isEmpty() &&
	 * collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD)) { List<String>
	 * senderList = new ArrayList<String>();
	 * senderList.add(simpleMailMessageForChequePaymentCreditCard.getFrom());
	 * 
	 * List<String> receiverList = new ArrayList<String>(); receiverList.add(email);
	 * 
	 * NotificationActivityAddition notificationActivityAddition = new
	 * NotificationActivityAddition(
	 * collection.getUser().getUserTableId().toString(),
	 * ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList, receiverList,
	 * simpleMailMessageForChequePaymentCreditCard.getSubject(), emailText,
	 * notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL); new
	 * Thread(notificationActivityAddition).run();
	 * 
	 * if (emailService.sendMail(collection.getEmailAddressNew(),
	 * simpleMailMessageForChequePaymentCreditCard.getFrom(),
	 * simpleMailMessageForChequePaymentCreditCard.getSubject(), emailText)) {
	 * NotificationActivity notificationActivity = notificationActivityAddition
	 * .extractNotificationActivity(); NotificationActivityStatusUpdate
	 * notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
	 * notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS),
	 * notificationActivityService); new
	 * Thread(notificationActivityStatusUpdate).run(); } else { NotificationActivity
	 * notificationActivity = notificationActivityAddition
	 * .extractNotificationActivity(); NotificationActivityStatusUpdate
	 * notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
	 * notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE),
	 * notificationActivityService); new
	 * Thread(notificationActivityStatusUpdate).run(); } } else { List<String>
	 * senderList = new ArrayList<String>();
	 * senderList.add(simpleMailMessageForChequePaymentLoan.getFrom());
	 * 
	 * List<String> receiverList = new ArrayList<String>(); receiverList.add(email);
	 * 
	 * NotificationActivityAddition notificationActivityAddition = new
	 * NotificationActivityAddition(
	 * collection.getUser().getUserTableId().toString(),
	 * ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList, receiverList,
	 * simpleMailMessageForChequePaymentLoan.getSubject(), emailText,
	 * notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL); new
	 * Thread(notificationActivityAddition).run();
	 * 
	 * if (emailService.sendMail(collection.getEmailAddressNew(),
	 * simpleMailMessageForChequePaymentLoan.getFrom(),
	 * simpleMailMessageForChequePaymentLoan.getSubject(), emailText)) {
	 * NotificationActivity notificationActivity = notificationActivityAddition
	 * .extractNotificationActivity(); NotificationActivityStatusUpdate
	 * notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
	 * notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS),
	 * notificationActivityService); new
	 * Thread(notificationActivityStatusUpdate).run(); } else { NotificationActivity
	 * notificationActivity = notificationActivityAddition
	 * .extractNotificationActivity(); NotificationActivityStatusUpdate
	 * notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
	 * notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE),
	 * notificationActivityService); new
	 * Thread(notificationActivityStatusUpdate).run(); } } } }
	 * 
	 * private void sendEmailForDDPDC(Collection collection) throws ParseException {
	 * String paymentDate = collection.getDeviceDate(); String collectionDate =
	 * collection.getDeviceDate();
	 * 
	 * Date emailDate = new Date(); SimpleDateFormat dateFormat = new
	 * SimpleDateFormat("yyyy-MM-dd"); SimpleDateFormat emailDateFormat = new
	 * SimpleDateFormat("ddMMMyyyy"); emailDate =
	 * (dateFormat.parse(collectionDate));
	 * 
	 * String email = collection.getEmailAddress().equals(Constants.EMPTY_STRING) ?
	 * collection.getEmailAddress() : collection.getEmailAddress();
	 * 
	 * String chequeDetailString = Constants.EMPTY_STRING;
	 * 
	 * NumberFormat formatter = NumberFormat.getInstance(new Locale("en", "IN"));
	 * 
	 * String emailAmount = collection.getAppropriateAmount() +
	 * Constants.EMPTY_STRING;
	 * 
	 * DecimalFormat amountFormat = new DecimalFormat("#.00");
	 * 
	 * try { emailAmount = amountFormat.format(Double.parseDouble(emailAmount)); }
	 * catch (Exception e) { emailAmount = "0.00"; }
	 * 
	 * for (Cheque cheque : collection.getChequeDetails()) { if
	 * (collection.getPaymentMode().equalsIgnoreCase(Constants.PAYMENT_MODE_DRAFT))
	 * { chequeDetailString = chequeDetailString + "<br/>Demand Draft No." +
	 * cheque.getChequeNo() + "     Dated " + new SimpleDateFormat("ddMMMyyyy")
	 * .format(new SimpleDateFormat("yyyy-MM-dd").parse(cheque.getChequeDate())); }
	 * else { chequeDetailString = chequeDetailString + "<br/> PDC No." +
	 * cheque.getChequeNo() + "     Dated " + new SimpleDateFormat("ddMMMyyyy")
	 * .format(new SimpleDateFormat("yyyy-MM-dd").parse(cheque.getChequeDate())); }
	 * 
	 * }
	 * 
	 * String emailText = Constants.EMPTY_STRING;
	 * 
	 * if (!collection.getAppl().isEmpty() &&
	 * collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD)) { if
	 * (collection.getPaymentMode().equalsIgnoreCase(Constants.PAYMENT_MODE_DRAFT))
	 * { emailText = String.format(simpleMailMessageForDDPDC.getText(),
	 * "Credit Card", "-" + collection.getAppl() + " " +
	 * collection.getBusinessPartnerNumber(), collection.getReceiptNumber(),
	 * "Demand Draft", emailAmount, emailDateFormat.format(emailDate),
	 * chequeDetailString, "-" + collection.getAppl() + " " +
	 * collection.getBusinessPartnerNumber()); } else { emailText =
	 * String.format(simpleMailMessageForDDPDC.getText(), "Credit Card", "-" +
	 * collection.getAppl() + " " + collection.getBusinessPartnerNumber(),
	 * collection.getReceiptNumber(), "PDC", emailAmount,
	 * emailDateFormat.format(emailDate), chequeDetailString, "-" +
	 * collection.getAppl() + " " + collection.getBusinessPartnerNumber()); } } else
	 * { if
	 * (collection.getPaymentMode().equalsIgnoreCase(Constants.PAYMENT_MODE_DRAFT))
	 * { emailText = String.format(simpleMailMessageForDDPDC.getText(),
	 * "Personal Finance Loan ", "-" + collection.getAppl() + " " +
	 * collection.getBusinessPartnerNumber(), collection.getReceiptNumber(),
	 * "Demand Draft", emailAmount, emailDateFormat.format(emailDate),
	 * chequeDetailString, "-" + collection.getAppl() + " " +
	 * collection.getBusinessPartnerNumber()); } else { emailText =
	 * String.format(simpleMailMessageForDDPDC.getText(), "Personal Finance Loan ",
	 * "-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber(),
	 * collection.getReceiptNumber(), "PDC", emailAmount,
	 * emailDateFormat.format(emailDate), chequeDetailString, "-" +
	 * collection.getAppl() + " " + collection.getBusinessPartnerNumber()); } }
	 * 
	 * if (collection.getEmailAddress() != null &&
	 * !collection.getEmailAddress().equalsIgnoreCase(Constants.EMPTY_STRING)) { if
	 * (!collection.getAppl().isEmpty()) {
	 * 
	 * List<String> senderList = new ArrayList<String>();
	 * senderList.add(simpleMailMessageForDDPDC.getFrom());
	 * 
	 * List<String> receiverList = new ArrayList<String>(); receiverList.add(email);
	 * 
	 * NotificationActivityAddition notificationActivityAddition = new
	 * NotificationActivityAddition(
	 * collection.getUser().getUserTableId().toString(),
	 * ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList, receiverList,
	 * simpleMailMessageForDDPDC.getSubject(), emailText,
	 * notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);
	 * 
	 * new Thread(notificationActivityAddition).run();
	 * 
	 * if (emailService.sendMail(email, simpleMailMessageForDDPDC.getFrom(),
	 * simpleMailMessageForDDPDC.getSubject(), emailText)) { NotificationActivity
	 * notificationActivity = notificationActivityAddition
	 * .extractNotificationActivity(); NotificationActivityStatusUpdate
	 * notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
	 * notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS),
	 * notificationActivityService);
	 * 
	 * new Thread(notificationActivityStatusUpdate).run(); } else {
	 * NotificationActivity notificationActivity = notificationActivityAddition
	 * .extractNotificationActivity(); NotificationActivityStatusUpdate
	 * notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
	 * notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE),
	 * notificationActivityService);
	 * 
	 * new Thread(notificationActivityStatusUpdate).run(); }
	 * 
	 * } else { log.info("----- Improper Information to Send Email"); } }
	 * 
	 * if (collection.getEmailAddressNew() != null &&
	 * !collection.getEmailAddressNew().equalsIgnoreCase(Constants.EMPTY_STRING) &&
	 * !collection.getEmailAddressNew().equalsIgnoreCase(email)) { if
	 * (!collection.getAppl().isEmpty()) { List<String> senderList = new
	 * ArrayList<String>();
	 * senderList.add(simpleMailMessageForCashPaymentCreditCard.getFrom());
	 * 
	 * List<String> receiverList = new ArrayList<String>();
	 * 
	 * receiverList.add(email);
	 * 
	 * NotificationActivityAddition notificationActivityAddition = new
	 * NotificationActivityAddition(
	 * collection.getUser().getUserTableId().toString(),
	 * ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL, senderList, receiverList,
	 * simpleMailMessageForDDPDC.getSubject(), emailText,
	 * notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL); new
	 * Thread(notificationActivityAddition).run();
	 * 
	 * if (emailService.sendMail(collection.getEmailAddressNew(),
	 * simpleMailMessageForDDPDC.getFrom(), simpleMailMessageForDDPDC.getSubject(),
	 * emailText)) { NotificationActivity notificationActivity =
	 * notificationActivityAddition .extractNotificationActivity();
	 * NotificationActivityStatusUpdate notificationActivityStatusUpdate = new
	 * NotificationActivityStatusUpdate( notificationActivity,
	 * (ActivityLoggerConstants.STATUS_SUCCESS), notificationActivityService); new
	 * Thread(notificationActivityStatusUpdate).run(); } else { NotificationActivity
	 * notificationActivity = notificationActivityAddition
	 * .extractNotificationActivity(); NotificationActivityStatusUpdate
	 * notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
	 * notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE),
	 * notificationActivityService); new
	 * Thread(notificationActivityStatusUpdate).run(); } } else {
	 * log.info("----- Improper Information to Send Email"); } } }
	 * 
	 * private String getFullFormApplType(String appl) { if
	 * (appl.equalsIgnoreCase(Constants.APPL_CARD)) { return "Credit Card"; } else
	 * if (appl.equalsIgnoreCase("SPLN")) { return "Salaried Personal Loans-New"; }
	 * else if (appl.equalsIgnoreCase("RAR")) { return
	 * "Retail Asset Reconstruction"; } else if (appl.equalsIgnoreCase("CV")) {
	 * return "Commercial Vehicles"; } else if (appl.equalsIgnoreCase("HF")) {
	 * return "Home Finance"; } else if (appl.equalsIgnoreCase("CSG")) { return
	 * "Personal Finance"; } else if (appl.equalsIgnoreCase("SPL")) { return
	 * "Salaried Personal Loans"; } else if (appl.equalsIgnoreCase("SA")) { return
	 * "UNNATI [SARAL]"; } else if (appl.equalsIgnoreCase("TFE")) { return
	 * "Tractor and Farm Equipment Loans"; } else if (appl.equalsIgnoreCase("CE")) {
	 * return "Construction Equipment"; } else if (appl.equalsIgnoreCase("LAP")) {
	 * return "Loan Against Property"; } else if (appl.equalsIgnoreCase("SBG")) {
	 * return "Strategic Business Group"; } else if (appl.equalsIgnoreCase("GLN")) {
	 * return "Gold Loan"; } else if (appl.equalsIgnoreCase("LCV")) { return
	 * "Light Commercial Vehicles"; } else if (appl.equalsIgnoreCase("RHB")) {
	 * return "Rural Housing Business"; } else if (appl.equalsIgnoreCase("RARF")) {
	 * return "Retail ARD Funding"; } else if (appl.equalsIgnoreCase("CLF")) {
	 * return "Car Lease Finance"; } else if (appl.equalsIgnoreCase("CF")) { return
	 * "Car Finance"; } else { return appl; } }
	 * 
	 * private String getTollFreeNumberForAppl(String appl) { if
	 * (appl.equalsIgnoreCase(Constants.APPL_CARD) || appl.equalsIgnoreCase("HF") ||
	 * appl.equalsIgnoreCase("LAP") || appl.equalsIgnoreCase("SPL") ||
	 * appl.equalsIgnoreCase("SPLN") || appl.equalsIgnoreCase("CSG")) { return
	 * "1800 102 6022"; } else if (appl.equalsIgnoreCase("CV") ||
	 * appl.equalsIgnoreCase("CE") || appl.equalsIgnoreCase("SA") ||
	 * appl.equalsIgnoreCase("TFE") || appl.equalsIgnoreCase("LCV") ||
	 * appl.equalsIgnoreCase("GLN")) { return "1800 209 5600"; } else if
	 * (appl.equalsIgnoreCase("RAR")) { return "1800 120 9820"; } else if
	 * (appl.equalsIgnoreCase("CF") || appl.equalsIgnoreCase("CLF")) { return
	 * "1800 209 5732"; } else { return Constants.EMPTY_STRING; } }
	 * 
	 * public String GenerateChecksumValue(Map<String, String> dataMap, String
	 * confDesc, String skey) throws Exception {
	 * 
	 * log.info("---inside payement request-----");
	 * 
	 * MerchantCollect mrc = new MerchantCollect(); DeviceDetails deviceDetails =
	 * new DeviceDetails();
	 * 
	 * deviceDetails.setApp(JsonConstants.UpiConstant.HARDCODE_VALUE_OF_APP);
	 * deviceDetails.setCapability(Constants.EMPTY_STRING);
	 * deviceDetails.setGcmid(Constants.EMPTY_STRING);
	 * deviceDetails.setGeocode(Constants.EMPTY_STRING);
	 * deviceDetails.setId(Constants.EMPTY_STRING);
	 * deviceDetails.setIp(Constants.EMPTY_STRING);
	 * deviceDetails.setLocation(Constants.EMPTY_STRING);
	 * deviceDetails.setMobile(JsonConstants.UpiConstant.
	 * HARDCODE_VALUE_OF_CUSTOMER_ID); deviceDetails.setOs(Constants.EMPTY_STRING);
	 * deviceDetails.setType(Constants.EMPTY_STRING);
	 * 
	 * mrc.setAggregatorVPA(JsonConstants.UpiConstant.
	 * HARDCODE_VALUE_OF_AGGREGATOR_VPA); mrc.setAmount(dataMap.get("amount"));
	 * mrc.setCustomerId(JsonConstants.UpiConstant.HARDCODE_VALUE_OF_CUSTOMER_ID);
	 * mrc.setDeviceDetails(deviceDetails); mrc.setExpiry(confDesc);
	 * mrc.setMerchantReferenceCode(JsonConstants.UpiConstant.
	 * HARDCODE_VALUE_OF_MERCHANT_REFERENCE_CODE);
	 * mrc.setOrderId(dataMap.get("receiptNumber"));
	 * mrc.setPayerVpa(dataMap.get("vpaAddress"));
	 * mrc.setReferenceId(Constants.EMPTY_STRING);
	 * mrc.setRemarks(dataMap.get("appl") + dataMap.get("apacNumberValue"));
	 * mrc.setSubmerchantReferenceid(Constants.EMPTY_STRING);
	 * mrc.setSubmerchantVPA(Constants.EMPTY_STRING);
	 * mrc.setTimeStamp(dataMap.get("timestampForUPI"));
	 * mrc.setTxnId(dataMap.get("GenerateUUIDValue"));
	 * 
	 * log.info("mrc.getInput() :: " + mrc.getInput());
	 * 
	 * byte[] digest = Crypto.SHA256(mrc.getInput());
	 * 
	 * byte[] encData = Crypto.encrypt(Crypto.hexStringToByteArray(skey), digest);
	 * 
	 * System.out.println("encData in hex Crypto.bytesToHex(encData) :" +
	 * Crypto.bytesToHex(encData));
	 * 
	 * String checkSumval = Base64.encodeBase64String(encData);
	 * 
	 * return checkSumval; }
	 * 
	 * public boolean requestPayment(String request) {
	 * 
	 * boolean flag = false;
	 * 
	 * JSONObject responseJSON = new JSONObject();
	 * 
	 * try {
	 * 
	 * log.info("-------requestSet inside return-----" + request);
	 * 
	 * String requestEntity = JSONPayloadExtractor.extract(request,
	 * JsonConstants.ENTITY); String requestAction =
	 * JSONPayloadExtractor.extract(request, JsonConstants.ACTION); String
	 * requestType = JSONPayloadExtractor.extract(request, JsonConstants.TYPE);
	 * 
	 * JSONObject jsonObject = new JSONObject(request); JSONObject user =
	 * (JSONObject) jsonObject.get(JsonConstants.SYSTEM_USER); SystemUser
	 * systemUserNew = ServerUtilities.extractSystemUser(user);
	 * 
	 * String imeiNo = Constants.EMPTY_STRING; String draMobileNumber =
	 * Constants.EMPTY_STRING; Long userTableId = 0L;
	 * 
	 * log.info("-------requestSet is-----" + request);
	 * log.info("-----requestEntity----" + requestEntity);
	 * log.info("-----requestAction----" + requestAction);
	 * log.info("-----requestType----" + requestType);
	 * log.info("---systemUserNew-----" + systemUserNew);
	 * log.info("---jsonObject-----" + jsonObject);
	 * 
	 * UserActivityAddition userActivityAddition = new UserActivityAddition(request,
	 * userActivityService, ActivityLoggerConstants.DATABASE_MSSQL);
	 * 
	 * new Thread(userActivityAddition).run();
	 * 
	 * UserActivity userActivity = userActivityAddition.extractUserActivity();
	 * 
	 * String fename = user.getString("firstLastName") == null ?
	 * Constants.EMPTY_STRING : user.getString("firstLastName");
	 * 
	 * JSONObject data = (JSONObject) jsonObject.get(JsonConstants.DATA);
	 * log.info("data part of json-------------------" + data);
	 * 
	 * String receiptNo = data.getString(Constants.RECEIPTNUM) == null ?
	 * Constants.EMPTY_STRING : data.getString(Constants.RECEIPTNUM);
	 * log.info("--receiptNo--" + receiptNo);
	 * 
	 * String paymentMode = Constants.EMPTY_STRING;
	 * 
	 * String partyMobNo = data.getString("partyMobNo") == null ?
	 * Constants.EMPTY_STRING : data.getString("partyMobNo");
	 * log.info("--partyMobNo--" + partyMobNo);
	 * 
	 * String mobileNew = data.getString("mobileNew") == null ?
	 * Constants.EMPTY_STRING : data.getString("mobileNew");
	 * log.info("--mobileNew--" + mobileNew);
	 * 
	 * String appl = data.getString(Constants.APPL) == null ? Constants.EMPTY_STRING
	 * : data.getString(Constants.APPL); log.info("--appl--" + appl);
	 * 
	 * String amount =
	 * data.getString(Constants.AllPayCollectionsDao.AMOUNT_LOWERCASE) == null ?
	 * Constants.EMPTY_STRING :
	 * data.getString(Constants.AllPayCollectionsDao.AMOUNT_LOWERCASE);
	 * log.info("--amount--" + amount);
	 * 
	 * String vpa = data.getString(Constants.PAYERVPA) == null ?
	 * Constants.EMPTY_STRING : data.getString(Constants.PAYERVPA);
	 * log.info("--vpa--" + vpa);
	 * 
	 * String txnId = data.getString(JsonConstants.TNX_ID) == null ?
	 * Constants.EMPTY_STRING : data.getString(JsonConstants.TNX_ID);
	 * log.info("--txnId--" + txnId);
	 * 
	 * String unqNo = data.getString("unqNo") == null ? Constants.EMPTY_STRING :
	 * data.getString("unqNo"); log.info("--unqNo--" + unqNo);
	 * 
	 * Map<String, String> detailMap = new HashMap<String, String>();
	 * detailMap.put("appl", appl); detailMap.put("amount", amount);
	 * detailMap.put("receiptNumber", receiptNo); detailMap.put("apacNumberValue",
	 * unqNo); detailMap.put("vpaAddress", vpa); detailMap.put("GenerateUUIDValue",
	 * txnId); detailMap.put("timestampForUPI", Utilities.convertDate(new
	 * Timestamp(System.currentTimeMillis())));
	 * 
	 * String configurationCode = "UNVAL"; String confDesc = Constants.EMPTY_STRING;
	 * String confType = Constants.EMPTY_STRING;
	 * 
	 * List<Map<String, Object>> rows =
	 * collectionService.getAllPaySMSValidity(configurationCode);
	 * 
	 * for (Map row : rows) { confDesc =
	 * row.get(Constants.AllPayCollectionsDao.CONFIGURATION_DESCRIPTION) == null ?
	 * Constants.EMPTY_STRING :
	 * row.get(Constants.AllPayCollectionsDao.CONFIGURATION_DESCRIPTION).toString();
	 * confType = row.get(Constants.AllPayCollectionsDao.CONFIGURATION_TYPE) == null
	 * ? Constants.EMPTY_STRING :
	 * row.get(Constants.AllPayCollectionsDao.CONFIGURATION_TYPE).toString(); }
	 * 
	 * String jsonRequestPaymentData = UpiUtility.generateUpiJsonReq(detailMap,
	 * confDesc);
	 * 
	 * log.info("jsonRequestPaymentData" + jsonRequestPaymentData);
	 * 
	 * String checkSumval = GenerateChecksumValue(detailMap, confDesc,
	 * "7A0D7DE6B5B0503A8044402B9653AB202887DD233378B9F3B4E72A71544B7AC0"); //
	 * String checkSumval = GenerateChecksumValue(detailMap, confDesc, //
	 * "376A3CCD74D7B33D92E4D452112A1B686EC3D45926310AD3AEF3B554B19D0FEF");
	 * 
	 * log.info("checkSumval in UPI   " + checkSumval);
	 * 
	 * // String jsonResponsePaymentData= //
	 * UpiUtility.callWebserviceAndGetJsonString(jsonRequestPaymentData,checkSumval)
	 * ;
	 * 
	 * String jsonResponsePaymentData =
	 * UpiUtility.callWebserviceAndGetJsonString(jsonRequestPaymentData,
	 * checkSumval, applicationConfiguration);
	 * 
	 * // String jsonResponsePaymentData="{ \"code\":\"00\", \"result\":\"Accepted
	 * // Collect Request\", \"data\":{ \"orderId\":\"MB123456789456123\", //
	 * \"referenceId\":\"825020132031\", \"payerVpa\":\"917208429868@kotak\", //
	 * \"payerName\":null, \"txnId\":\"KMBMKCBG9c2baea9443c4e6aac1e096a33f\", //
	 * \"aggregatorVPA\":\"kcbg@kotak\", \"expiry\":\"1800\", \"amount\":\"15.00\",
	 * // \"timeStamp\":\"07-09-2018 19:22:04 \" } }";
	 * 
	 * Map<Object, Object> updateActivityMap = new HashMap<Object, Object>();
	 * 
	 * if (jsonResponsePaymentData != null && !jsonResponsePaymentData.isEmpty()) {
	 * 
	 * Map<Object, Object> activityMap = new HashMap<Object, Object>();
	 * 
	 * activityMap.put(Constants.AllPayCollectionsDao.REQUEST_TO_THIRD_PARTY,
	 * jsonRequestPaymentData);
	 * activityMap.put(Constants.AllPayCollectionsDao.THIRD_PARTY_STATUS,
	 * "PENDING"); activityMap.put(Constants.AllPayCollectionsDao.DEVICE_REQUEST,
	 * request); activityMap.put(Constants.AllPayCollectionsDao.CREATED_BY,
	 * systemUserNew.getUserTableId().toString());
	 * activityMap.put(Constants.AllPayCollectionsDao.MODIFIED_BY,
	 * systemUserNew.getUserTableId().toString());
	 * activityMap.put(Constants.AllPayCollectionsDao.RECEIPT_NUMBER, receiptNo);
	 * activityMap.put(Constants.UPIDao.UPI_TRANS_ID, txnId);
	 * activityMap.put(Constants.UPIDao.AMOUNT, amount);
	 * activityMap.put(Constants.UPIDao.VPA_ADDRESS, vpa);
	 * 
	 * int smsTableID = collectionService.smsPaymentActivityAddition(activityMap);
	 * 
	 * log.info("smsTableID ---- > " + smsTableID);
	 * 
	 * JSONObject jsonCode = new JSONObject(jsonResponsePaymentData);
	 * log.info("jsonCode--->" + jsonCode); JSONObject responseData = (JSONObject)
	 * jsonCode.get(JsonConstants.DATA); log.info("data part of json---" +
	 * responseData);
	 * 
	 * String status = Constants.EMPTY_STRING;
	 * 
	 * Collection collection = new Collection();
	 * collection.setReceiptNumber(receiptNo);
	 * collection.setBusinessPartnerNumber(unqNo); collection.setAppl(appl);
	 * collection.setAmount(amount); collection.setFeName(fename);
	 * collection.setContact(partyMobNo); collection.setMobileNumberNew(mobileNew);
	 * 
	 * if (((String) jsonCode.get("code")).equalsIgnoreCase("00")) { status =
	 * Constants.UPIDao.SUCCESS;
	 * 
	 * updateActivityMap.put(Constants.UPIDao.ID, smsTableID);
	 * 
	 * // updateActivityMap.put(Constants.UPIDao.INVOICE_ID, //
	 * responseData.get("orderId"));
	 * 
	 * updateActivityMap.put(Constants.UPIDao.INVOICE_ID, txnId.toString());
	 * 
	 * updateActivityMap.put(Constants.UPIDao.THIRD_PARTY_RESPONSE,
	 * jsonResponsePaymentData);
	 * 
	 * updateActivityMap.put(Constants.UPIDao.MODIFIED_BY,
	 * systemUserNew.getUserTableId().toString());
	 * 
	 * updateActivityMap.put(Constants.UPIDao.THIRD_PARTY_STATUS, status);
	 * 
	 * updateActivityMap.put(Constants.UPIDao.DEVICE_RESPONSE_STATUS, status);
	 * 
	 * updateActivityMap.put(Constants.AllPayCollectionsDao.RECEIPT_NUMBER,
	 * receiptNo);
	 * 
	 * collectionService.smsPaymentActivityUpdation(updateActivityMap);
	 * 
	 * flag = true;
	 * 
	 * collection.setPaymentMode(paymentMode);
	 * 
	 * //
	 * sendAllPaySMSToCustomerAfterSubmittingReceipt(collection,status,systemUserNew
	 * .getMobileNumber(),systemUserNew.getUserTableId(),systemUserNew.getImeiNo());
	 * 
	 * log.info("Success in Request Payment----"); }
	 * 
	 * else { status = Constants.UPIDao.FAILURE;
	 * 
	 * updateActivityMap.put(Constants.UPIDao.ID, smsTableID);
	 * 
	 * updateActivityMap.put(Constants.UPIDao.THIRD_PARTY_RESPONSE,
	 * jsonResponsePaymentData);
	 * 
	 * updateActivityMap.put(Constants.UPIDao.MODIFIED_BY,
	 * systemUserNew.getUserTableId().toString());
	 * 
	 * updateActivityMap.put(Constants.UPIDao.THIRD_PARTY_STATUS, status);
	 * 
	 * updateActivityMap.put(Constants.UPIDao.DEVICE_RESPONSE_STATUS, status);
	 * 
	 * updateActivityMap.put(Constants.AllPayCollectionsDao.RECEIPT_NUMBER,
	 * receiptNo);
	 * 
	 * collectionService.smsPaymentActivityUpdation(updateActivityMap);
	 * 
	 * flag = false;
	 * 
	 * collection.setPaymentMode(paymentMode);
	 * 
	 * sendAllPaySMSToCustomerAfterSubmittingReceipt(collection, status,
	 * systemUserNew.getMobileNumber(), systemUserNew.getUserTableId(),
	 * systemUserNew.getImeiNo());
	 * 
	 * log.info("Failure  in Request Payment----");
	 * 
	 * }
	 * 
	 * } else {
	 * 
	 * flag = false;
	 * 
	 * }
	 * 
	 * } catch (Exception e) { e.printStackTrace(); }
	 * 
	 * return flag;
	 * 
	 * }
	 * 
	 * private void sendAllPaySMSToCustomerAfterSubmittingReceipt(Collection
	 * collection, String status, String draMobileNumber, Long userTableId, String
	 * imeiNo)
	 * 
	 * {
	 * 
	 * SystemUser systemUser = new SystemUser(); systemUser.setImeiNo(imeiNo);
	 * systemUser.setUserTableId(userTableId);
	 * systemUser.setMobileNumber(draMobileNumber);
	 * 
	 * if (collection.getContact() != null &&
	 * !collection.getContact().equalsIgnoreCase(Constants.EMPTY_STRING)) {
	 * log.info("Sending SMS on customer number " + collection.getContact());
	 * 
	 * generateSMSToCustomerOnSubmittingReceiptForAllPay(collection.getAmount(),
	 * collection.getReceiptNumber(), collection.getPaymentMode(),
	 * collection.getContact(), collection.getAppl(), collection.getFeName(),
	 * systemUser, communicationActivityService, collection, status);
	 * 
	 * }
	 * 
	 * if (collection.getMobileNumberNew() != null &&
	 * !collection.getMobileNumberNew().equalsIgnoreCase(Constants.EMPTY_STRING)) {
	 * log.info("Sending SMS on customer number " + collection.getContact());
	 * 
	 * generateSMSToCustomerOnSubmittingReceiptForAllPay(collection.getAmount(),
	 * collection.getReceiptNumber(), collection.getPaymentMode(),
	 * collection.getMobileNumberNew(), collection.getAppl(),
	 * collection.getFeName(), systemUser, communicationActivityService, collection,
	 * status);
	 * 
	 * }
	 * 
	 * if (draMobileNumber != null &&
	 * !draMobileNumber.equalsIgnoreCase(Constants.EMPTY_STRING)) {
	 * log.info("Sending sms to DRA mobile number " + collection.getAmount());
	 * 
	 * generateSMSToDRAOnSubmittingReceiptForAllPay(collection.getAmount(),
	 * collection.getReceiptNumber(), collection.getPaymentMode(), draMobileNumber,
	 * collection.getAppl(), collection.getFeName(), systemUser,
	 * communicationActivityService, collection, status);
	 * 
	 * }
	 * 
	 * }
	 * 
	 * private void generateSMSToCustomerOnSubmittingReceiptForAllPay(String amount,
	 * String receiptNumber, String paymentType, String mobileNumber, String appl,
	 * String feName, SystemUser user, CommunicationActivityService
	 * communicationActivityService, Collection collection, String status) {
	 * 
	 * log.
	 * info("---- Inside generateSMSToCustomerOnSubmittingReceiptForAllPay --------"
	 * );
	 * 
	 * String webserviceUrl = (String)
	 * applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");
	 * 
	 * Map<String, Object> smsDispatcherMap =
	 * ServerUtilities.generateAllPaySMSToCustomerOnSubmittingReceiptForAllPay(
	 * amount, receiptNumber, paymentType, mobileNumber, appl, feName, status,
	 * collection);
	 * 
	 * log.info("---- Inside smsDispatcherMap --------" + smsDispatcherMap);
	 * 
	 * StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true,
	 * new HashMap<String, String>());
	 * 
	 * log.info("---- Inside xmlRequest --------" + xmlRequest);
	 * 
	 * CommunicationActivityAddition communicationActivityAddition = new
	 * CommunicationActivityAddition( user.getUserTableId().toString(),
	 * user.getImeiNo(), (appl + "_" + collection.getCollectionType()),
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
	 * } else {
	 * communicationActivity.setResponse(ActivityLoggerConstants.EMPTY_STRING);
	 * 
	 * CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new
	 * CommunicationActivityStatusUpdate( communicationActivity,
	 * (ActivityLoggerConstants.STATUS_FAILURE), communicationActivityService);
	 * 
	 * new Thread(communicationActivityStatusUpdate).run();
	 * 
	 * log.info("----- Failure in sending SMS : -------"); } }
	 * 
	 * private void generateSMSToDRAOnSubmittingReceiptForAllPay(String amount,
	 * String receiptNumber, String paymentType, String mobileNumber, String type,
	 * String feName, SystemUser user, CommunicationActivityService
	 * communicationActivityService, Collection collection, String status) {
	 * log.info("---- Inside generateAllPaySMSOnSubmittingReceiptForAllPay --------"
	 * );
	 * 
	 * String webserviceUrl = (String)
	 * applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");
	 * 
	 * Map<String, Object> smsDispatcherMap =
	 * ServerUtilities.generateSMSToDRAOnSubmittingReceiptForAllPay(amount,
	 * receiptNumber, paymentType, mobileNumber, type, feName, status, collection);
	 * 
	 * log.info("---- Inside smsDispatcherMap --------" + smsDispatcherMap);
	 * 
	 * StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true,
	 * new HashMap<String, String>());
	 * 
	 * log.info("---- Inside xmlRequest --------" + xmlRequest);
	 * 
	 * // log.info("----- xmlRequest : -------" + xmlRequest);
	 * CommunicationActivityAddition communicationActivityAddition = new
	 * CommunicationActivityAddition( user.getUserTableId().toString(),
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
	 * } else {
	 * communicationActivity.setResponse(ActivityLoggerConstants.EMPTY_STRING);
	 * 
	 * CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new
	 * CommunicationActivityStatusUpdate( communicationActivity,
	 * (ActivityLoggerConstants.STATUS_FAILURE), communicationActivityService);
	 * 
	 * new Thread(communicationActivityStatusUpdate).run();
	 * 
	 * log.info("----- Failure in sending SMS : -------"); } }
	 * 
	 * @RequestMapping(value = "/collSubmit", method = RequestMethod.POST)
	 * 
	 * @ResponseBody public String submitCollections(@RequestBody String request)
	 * 
	 * {
	 * 
	 * log.info(" -------- After Kafka getting in Integration Server-------- ");
	 * log.info(" -------- In CollectionsService -------- " + request);
	 * 
	 * String status = JsonConstants.FAILURE; String returnMessage = null; String
	 * contractAccountNumber = Constants.EMPTY_STRING; String lockCode =
	 * Constants.EMPTY_STRING; String collectionCode = Constants.EMPTY_STRING;
	 * String allocationNumber = Constants.EMPTY_STRING; String bp =
	 * Constants.EMPTY_STRING; String amount = "0.0"; String pan =
	 * Constants.EMPTY_STRING; String email = Constants.EMPTY_STRING; String contact
	 * = Constants.EMPTY_STRING; String collectionStatus = Constants.EMPTY_STRING;
	 * String deviceDate = Constants.EMPTY_STRING; String revisitedDate =
	 * Constants.EMPTY_STRING; String area = Constants.EMPTY_STRING; String mread =
	 * Constants.EMPTY_STRING; String emailAddressNew = Constants.EMPTY_STRING;
	 * String mobileNumberNew = Constants.EMPTY_STRING; String deviceTime =
	 * Constants.EMPTY_STRING; String collStatus = Constants.EMPTY_STRING; String
	 * allocStatus = Constants.EMPTY_STRING; String receiptNumber =
	 * Constants.EMPTY_STRING; String remarks = Constants.EMPTY_STRING; String
	 * billNo = Constants.EMPTY_STRING; String batchNumber = Constants.EMPTY_STRING;
	 * String billCycle = Constants.EMPTY_STRING; String signaturePath =
	 * Constants.EMPTY_STRING; String signature = Constants.EMPTY_STRING; String
	 * caseId = "0L";
	 * 
	 * String feedback_code = Constants.EMPTY_STRING; String ptpAmount = "0.00";
	 * 
	 * String latitude = Constants.EMPTY_STRING; String longitude =
	 * Constants.EMPTY_STRING; String partyName = Constants.EMPTY_STRING; String
	 * nextActionCode = Constants.EMPTY_STRING; String nextActionCodeDescription =
	 * Constants.EMPTY_STRING; boolean submissionFlag = false; String regNo =
	 * Constants.EMPTY_STRING; String branchName = Constants.EMPTY_STRING; String
	 * paymentDate = Constants.EMPTY_STRING; String actioncode =
	 * Constants.EMPTY_STRING; String actionDesc = Constants.EMPTY_STRING; String
	 * resultCode = Constants.EMPTY_STRING; String resultDesc =
	 * Constants.EMPTY_STRING; String resultcodeAnddesc = Constants.EMPTY_STRING;
	 * String nextActionCodeValues = Constants.EMPTY_STRING;
	 * 
	 * // String start_lat = Constants.EMPTY_STRING; String start_long =
	 * Constants.EMPTY_STRING; String end_lat = Constants.EMPTY_STRING; String
	 * end_long = Constants.EMPTY_STRING; String allRisk = ""; String team = "";
	 * String bucket = "";
	 * 
	 * String result = Constants.EMPTY_STRING;
	 * 
	 * List<Image> images = new ArrayList<Image>();
	 * 
	 * try { String requestSet = request;
	 * 
	 * JSONObject jsonObj = new JSONObject(requestSet);
	 * 
	 * log.info("jsonObj-----------in collectionSubmissionService :: " + jsonObj);
	 * 
	 * JSONObject jsonData = (JSONObject) jsonObj.get(JsonConstants.DATA); if
	 * (jsonData.has("images")) { jsonData.remove("images"); }
	 * 
	 * jsonObj.put(JsonConstants.DATA, jsonData);
	 * 
	 * String requestWithoutImage = jsonObj.toString();
	 * 
	 * UserActivityAddition userActivityAddition = new
	 * UserActivityAddition(requestWithoutImage, userActivityService,
	 * ActivityLoggerConstants.DATABASE_MSSQL);
	 * 
	 * new Thread(userActivityAddition).run();
	 * 
	 * UserActivity userActivity = userActivityAddition.extractUserActivity();
	 * 
	 * JSONObject jsonObject = new JSONObject(requestSet);
	 * 
	 * log.info("jsonObject-----------in collectionSubmissionService :: " +
	 * jsonObject);
	 * 
	 * JSONObject data = (JSONObject) jsonObject.get(JsonConstants.DATA); JSONObject
	 * user = (JSONObject) jsonObject.get(JsonConstants.SYSTEM_USER); SystemUser
	 * systemUserNew = ServerUtilities.extractSystemUser(user);
	 * 
	 * SystemUser systemUserTemp =
	 * systemUserService.getUser(systemUserNew.getUserTableId());
	 * systemUserNew.setSupervisorMobileNumber(systemUserTemp.
	 * getSupervisorMobileNumber());
	 * systemUserNew.setSupervisorName(systemUserTemp.getSupervisorName());
	 * 
	 * log.info("----system user ----" + systemUserNew);
	 * 
	 * Collection collection = new Collection();
	 * 
	 * Map reqMap = Utilities.createMapFromJSON(requestSet); String type = (String)
	 * reqMap.get(JsonConstants.Key.TYPE);
	 * 
	 * String requestEntity = data.get(JsonConstants.APPL) == null ?
	 * Constants.EMPTY_STRING : data.getString(JsonConstants.APPL);
	 * 
	 * Deposition submission check
	 * 
	 * collectionCode = data.get(JsonConstants.RequestData.COLLECTION_CODE) == null
	 * ? Constants.EMPTY_STRING : (String)
	 * data.get(JsonConstants.RequestData.COLLECTION_CODE);
	 * 
	 * String payMode = data.get(JsonConstants.RequestData.PAY_MODE) == null ?
	 * Constants.EMPTY_STRING : (String)
	 * data.get(JsonConstants.RequestData.PAY_MODE);
	 * 
	 * if (type.toString().equalsIgnoreCase("collections")) { caseId =
	 * data.get("caseId") == null ? "0L" : data.get("caseId").toString(); }
	 * 
	 * collectionCode = data.get(JsonConstants.RequestData.COLLECTION_CODE) == null
	 * ? Constants.EMPTY_STRING : (String)
	 * data.get(JsonConstants.RequestData.COLLECTION_CODE);
	 * 
	 * if (data.has(JsonConstants.FEEDBACK_CODE)) { feedback_code =
	 * data.getString(JsonConstants.FEEDBACK_CODE);
	 * 
	 * }
	 * 
	 * if (!collectionCode.equalsIgnoreCase("RTP")) {
	 * 
	 * revisitedDate = data.get(JsonConstants.RequestData.REVISITED_DATE) == null ?
	 * Constants.EMPTY_STRING : (String)
	 * data.get(JsonConstants.RequestData.REVISITED_DATE);
	 * 
	 * }
	 * 
	 * if (data.has(JsonConstants.ACTIONCODE)) {
	 * collection.setActionCode(data.getString("actionCode")); } if
	 * (data.has(JsonConstants.RESULTCODE)) {
	 * collection.setResultCode(data.getString("resultCode")); }
	 * 
	 * // added by bhushan log.info("----test 1");
	 * 
	 * if (data.has(JsonConstants.FEEDBACKCODE) &&
	 * (!collectionCode.equalsIgnoreCase("RTP"))) { JSONObject feedBackCodeJSON =
	 * (JSONObject) data.getJSONObject(JsonConstants.FEEDBACKCODE);
	 * 
	 * String actioncodeAnddesc = feedBackCodeJSON.get(JsonConstants.ACTIONCODE) ==
	 * null ? Constants.EMPTY_STRING :
	 * feedBackCodeJSON.get(JsonConstants.ACTIONCODE).toString(); Boolean flag =
	 * false;
	 * 
	 * if (actioncodeAnddesc.length() > 0) { flag = true; }
	 * 
	 * if (actioncodeAnddesc.contains("(")) { actioncode =
	 * actioncodeAnddesc.substring(0, actioncodeAnddesc.indexOf("(") - 1).trim();
	 * 
	 * actionDesc = actioncodeAnddesc.substring(actioncodeAnddesc.indexOf("(") + 1,
	 * actioncodeAnddesc.indexOf(")")); }
	 * 
	 * JSONArray code = feedBackCodeJSON.getJSONArray(JsonConstants.CODES);
	 * 
	 * List<Feedback> feedBackCodeList = new ArrayList<Feedback>();
	 * 
	 * for (int i = 0; i < (code.length()); i++) { JSONObject codeJSON =
	 * code.getJSONObject(i); Feedback feedBack = new Feedback();
	 * 
	 * resultcodeAnddesc = codeJSON.get(JsonConstants.RESULTCODE) == null ?
	 * Constants.EMPTY_STRING : codeJSON.get(JsonConstants.RESULTCODE).toString();
	 * 
	 * if (resultcodeAnddesc.length() > 0) { flag = true; }
	 * 
	 * if (resultcodeAnddesc.contains("(")) { resultCode =
	 * resultcodeAnddesc.substring(0, resultcodeAnddesc.indexOf("(") - 1).trim();
	 * 
	 * resultDesc = resultcodeAnddesc.substring(resultcodeAnddesc.indexOf("(") + 1,
	 * resultcodeAnddesc.indexOf(")")); }
	 * 
	 * if (codeJSON.has(JsonConstants.NEXT_ACTION_CODE)) { nextActionCodeValues =
	 * codeJSON.get(JsonConstants.NEXT_ACTION_CODE) == null ? Constants.EMPTY_STRING
	 * : codeJSON.get(JsonConstants.NEXT_ACTION_CODE).toString(); }
	 * 
	 * if (nextActionCodeValues.length() > 0) { flag = true; }
	 * 
	 * if (nextActionCodeValues.contains("(")) { nextActionCode =
	 * nextActionCodeValues.substring(0, nextActionCodeValues.indexOf("(") - 1)
	 * .trim();
	 * 
	 * nextActionCodeDescription = nextActionCodeValues
	 * .substring(nextActionCodeValues.indexOf("(") + 1,
	 * nextActionCodeValues.indexOf(")")); }
	 * 
	 * String revisitedDate1 = Constants.EMPTY_STRING; if (actioncode !=
	 * Constants.EMPTY_STRING && actionDesc != Constants.EMPTY_STRING) {
	 * feedBack.setActionCode(actioncode); feedBack.setActionDesc(actionDesc); } if
	 * (resultCode != Constants.EMPTY_STRING && resultCode !=
	 * Constants.EMPTY_STRING) { feedBack.setResultCode(resultCode);
	 * feedBack.setResultDesc(resultDesc); }
	 * 
	 * if (nextActionCode != Constants.EMPTY_STRING && nextActionCodeDescription !=
	 * Constants.EMPTY_STRING) { feedBack.setNextActionCode(nextActionCode);
	 * feedBack.setNextActionCodeDescription(nextActionCodeDescription); }
	 * 
	 * if (revisitedDate1 != Constants.EMPTY_STRING) {
	 * feedBack.setRevisitDate(revisitedDate1); }
	 * 
	 * if (flag) { feedBackCodeList.add(feedBack); } }
	 * 
	 * collection.setFeedback(feedBackCodeList); log.info("feedBackCodeList : " +
	 * feedBackCodeList);
	 * 
	 * }
	 * 
	 * log.info("----test 2");
	 * 
	 * if (((!collectionCode.equalsIgnoreCase("RTP")) ||
	 * amount.equals(Constants.EMPTY_STRING)) &&
	 * !collectionCode.equalsIgnoreCase("PU")) {
	 * 
	 * if (data.has(JsonConstants.PTP_AMOUNT_NEW)) ptpAmount =
	 * data.getString(JsonConstants.PTP_AMOUNT_NEW);
	 * 
	 * }
	 * 
	 * else if (collectionCode.equalsIgnoreCase("PU")) {
	 * 
	 * if (data.has(JsonConstants.RequestData.AMOUNT)) ptpAmount =
	 * data.getString(JsonConstants.RequestData.AMOUNT);
	 * 
	 * } else {
	 * 
	 * if (data.has(JsonConstants.RequestData.AMOUNT)) amount = (String)
	 * data.get(JsonConstants.RequestData.AMOUNT);
	 * 
	 * }
	 * 
	 * if(ptpAmount.equalsIgnoreCase(Constants.EMPTY_STRING)) { ptpAmount = "0.00";
	 * }
	 * 
	 * if(ptpAmount.equalsIgnoreCase(Constants.EMPTY_STRING)) { amount = "0.0"; }
	 * 
	 * log.info("----test 3");
	 * 
	 * deviceTime = data.get(JsonConstants.RequestData.DEVICE_TIME) == null ?
	 * Constants.EMPTY_STRING : (String)
	 * data.get(JsonConstants.RequestData.DEVICE_TIME);
	 * 
	 * deviceDate = data.get(JsonConstants.RequestData.DEVICE_DATE) == null ?
	 * Constants.EMPTY_STRING : (String)
	 * data.get(JsonConstants.RequestData.DEVICE_DATE);
	 * 
	 * area = data.get(JsonConstants.RequestData.AREA) == null ?
	 * Constants.EMPTY_STRING : (String) data.get(JsonConstants.RequestData.AREA);
	 * 
	 * if (data.has(JsonConstants.RequestData.BRANCH_NAME)) { branchName =
	 * data.get(JsonConstants.RequestData.BRANCH_NAME) == null ?
	 * Constants.EMPTY_STRING : (String)
	 * data.get(JsonConstants.RequestData.BRANCH_NAME); }
	 * 
	 * if (data.has(JsonConstants.RequestData.PAYMENT_DATE)) { paymentDate =
	 * data.get(JsonConstants.RequestData.PAYMENT_DATE) == null ?
	 * Constants.EMPTY_STRING : (String)
	 * data.get(JsonConstants.RequestData.PAYMENT_DATE); }
	 * 
	 * if (data.has(JsonConstants.RequestData.COLLECTION_STATUS)) {
	 * 
	 * collStatus = data.get(JsonConstants.RequestData.COLLECTION_STATUS) == null ?
	 * Constants.EMPTY_STRING : (String)
	 * data.get(JsonConstants.RequestData.COLLECTION_STATUS); }
	 * 
	 * if (data.has("allocStatus")) {
	 * 
	 * allocStatus = data.get("allocStatus") == null ? Constants.EMPTY_STRING :
	 * (String) data.get("allocStatus"); }
	 * 
	 * receiptNumber = data.get(JsonConstants.RequestData.RECEIPT_NUMBER) == null ?
	 * Constants.EMPTY_STRING : (String)
	 * data.get(JsonConstants.RequestData.RECEIPT_NUMBER);
	 * 
	 * remarks = data.get(JsonConstants.RequestData.REMARKS) == null ?
	 * Constants.EMPTY_STRING : (String)
	 * data.get(JsonConstants.RequestData.REMARKS);
	 * 
	 * if (type.toString().equalsIgnoreCase("collections")) { billCycle =
	 * data.get(JsonConstants.BILL_CYCLE) == null ? Constants.EMPTY_STRING :
	 * (String) data.get(JsonConstants.BILL_CYCLE); }
	 * 
	 * if (data.has(JsonConstants.MOBILE_NUMBER_NEW)) { mobileNumberNew =
	 * data.get(JsonConstants.MOBILE_NUMBER_NEW) == null ? Constants.EMPTY_STRING :
	 * data.getString(JsonConstants.MOBILE_NUMBER_NEW);
	 * 
	 * }
	 * 
	 * if (data.has(JsonConstants.MOBILE_NUMBER)) { contact =
	 * data.get(JsonConstants.MOBILE_NUMBER) == null ? Constants.EMPTY_STRING :
	 * data.getString(JsonConstants.MOBILE_NUMBER); }
	 * 
	 * if (data.has(JsonConstants.ALL_RISK)) { allRisk =
	 * data.get(JsonConstants.ALL_RISK) == null ? "" :
	 * data.getString(JsonConstants.ALL_RISK); }
	 * 
	 * if (data.has(JsonConstants.TEAM)) { team = data.get(JsonConstants.TEAM) ==
	 * null ? "" : data.getString(JsonConstants.TEAM); }
	 * 
	 * if (data.has(JsonConstants.BUCKET)) { bucket = data.get(JsonConstants.BUCKET)
	 * == null ? "" : data.getString(JsonConstants.BUCKET); } emailAddressNew =
	 * data.get(JsonConstants.EMAIL_ADDRESS_NEW) == null ? Constants.EMPTY_STRING :
	 * data.getString(JsonConstants.EMAIL_ADDRESS_NEW);
	 * 
	 * email = data.get(JsonConstants.EMAIL_ADDRESS) == null ?
	 * Constants.EMPTY_STRING : data.getString(JsonConstants.EMAIL_ADDRESS);
	 * 
	 * if (data.has(JsonConstants.LATITUDE) && data.has(JsonConstants.LONGITUDE)) {
	 * latitude = data.get(JsonConstants.LATITUDE) == null ? Constants.EMPTY_STRING
	 * : data.getString(JsonConstants.LATITUDE); longitude =
	 * data.get(JsonConstants.LONGITUDE) == null ? Constants.EMPTY_STRING :
	 * data.getString(JsonConstants.LONGITUDE); } else { latitude = "0.00";
	 * longitude = "0.00"; }
	 * 
	 * collection.setEmailAddress(data.get(JsonConstants.EMAIL_ADDRESS) == null ?
	 * Constants.EMPTY_STRING : data.getString(JsonConstants.EMAIL_ADDRESS));
	 * 
	 * collection.setCorrAddress(data.get(JsonConstants.CORRESPONDENCE_ADDRESS) ==
	 * null ? Constants.EMPTY_STRING :
	 * data.getString(JsonConstants.CORRESPONDENCE_ADDRESS));
	 * 
	 * collection.setCorrLocation(
	 * data.getString(JsonConstants.CORRESPONDENCE_LOCATION) == null ?
	 * Constants.EMPTY_STRING :
	 * data.getString(JsonConstants.CORRESPONDENCE_LOCATION));
	 * 
	 * if (data.has(JsonConstants.CORRESPONDENCE_PINCODE)) {
	 * collection.setCorrPin(data.getString(JsonConstants.CORRESPONDENCE_PINCODE));
	 * }
	 * 
	 * if (data.has(JsonConstants.SECOND_ADDRESS)) {
	 * collection.setSecAddress(data.getString(JsonConstants.SECOND_ADDRESS)); }
	 * 
	 * if (data.has(JsonConstants.SECOND_LOCATION)) {
	 * collection.setSecLocation(data.getString(JsonConstants.SECOND_LOCATION)); }
	 * 
	 * if (data.has(JsonConstants.SECOND_PINCODE)) {
	 * collection.setSecPin(data.getString(JsonConstants.SECOND_PINCODE)); }
	 * 
	 * if (data.has(JsonConstants.DUE_DATE)) {
	 * collection.setDueDate(data.getString(JsonConstants.DUE_DATE)); }
	 * 
	 * //02 mar 2020 if (data.has(JsonConstants.ALTERNATE_ADDRESS)) {
	 * collection.setAlternateAddress(data.getString(JsonConstants.ALTERNATE_ADDRESS
	 * )); } if (data.has("alternateAddress")) {
	 * collection.setAlternateAddress(data.getString("alternateAddress")); }
	 * 
	 * 
	 * if (type.toString().equalsIgnoreCase("collections")) {
	 * collection.setCaseId(Long.parseLong(caseId)); }
	 * 
	 * collection.setOutstanding(Double.parseDouble(data.getString(JsonConstants.
	 * OUTSTANDING) == null ||
	 * data.getString(JsonConstants.OUTSTANDING).equalsIgnoreCase(Constants.
	 * EMPTY_STRING) ? "0.0" : data.getString(JsonConstants.OUTSTANDING)));
	 * 
	 * log.info("----test 4"); log.info("----payMode" + payMode);
	 * log.info("---amount " + amount);
	 * 
	 * if (payMode.equalsIgnoreCase(Constants.PAYMENT_MODE_CASH) &&
	 * !amount.equalsIgnoreCase("0.0") && !collectionCode.equalsIgnoreCase("PU")) {
	 * if (data.has(JsonConstants.RequestData.CASH)) {
	 * 
	 * log.info("---before chsh detail "); if(amount.equalsIgnoreCase("null")) {
	 * amount="0.0"; } extractCashDetails(pan, data, collection);
	 * log.info("---after chsh detail ");
	 * 
	 * }
	 * 
	 * else {
	 * 
	 * status = JsonConstants.FAILURE; } }
	 * 
	 * 
	 * //code done when amount was caputing 0.0 for RTP in collections table when
	 * amount was coming null from device on 26 Feb 2021 start
	 * if(collectionCode!=null && collectionCode.equalsIgnoreCase("RTP") &&
	 * (amount.equalsIgnoreCase("null") || amount.equalsIgnoreCase("0.0")) ) { log.
	 * info("::inside amount is null value or amount is 0.0  and collection code is RTP ::"
	 * +amount+" "+ collectionCode);
	 * 
	 * if (data.has(JsonConstants.LOAN)) { JSONObject loanJSON = (JSONObject)
	 * data.getJSONObject(JsonConstants.LOAN);
	 * 
	 * if(loanJSON.has(JsonConstants.LOAN_TRANS_TYPE)) { JSONArray transType =
	 * loanJSON.getJSONArray(JsonConstants.LOAN_TRANS_TYPE); if(transType!=null &&
	 * transType.length()>0) { JSONObject transJSON = transType.getJSONObject(0);
	 * amount=transJSON.get(JsonConstants.LOAN_TRANS_TYPE_AMOUNT) == null ||
	 * transJSON.getString(JsonConstants.LOAN_TRANS_TYPE_AMOUNT).equalsIgnoreCase(
	 * Constants.EMPTY_STRING) ? "0.0" :
	 * transJSON.get(JsonConstants.LOAN_TRANS_TYPE_AMOUNT).toString(); } }
	 * 
	 * 
	 * } else { amount="0.0"; }
	 * 
	 * } //code done when amount was caputing 0.0 for RTP in collections table when
	 * amount was coming null from device on 26 Feb 2021 end
	 * 
	 * List<Cheque> cheques = new ArrayList<Cheque>();
	 * 
	 * log.info("---collection code" + collectionCode);
	 * 
	 * if (payMode.equalsIgnoreCase("DCARD") || payMode.equalsIgnoreCase("NB") ||
	 * payMode.equalsIgnoreCase("UPI")) {
	 * 
	 * collection.setCcapac("2003"); }
	 * 
	 * if ((Constants.PAYMENT_MODE_CHEQUE.equals(payMode) ||
	 * Constants.PAYMENT_MODE_DRAFT.equals(payMode) ||
	 * Constants.PAYMENT_MODE_PDC.equals(payMode)) &&
	 * !collectionCode.equalsIgnoreCase("PU")) { cheques = getCheques(systemUserNew,
	 * data); log.info("Cheques got : " + cheques);
	 * 
	 * } else if (Constants.UPIDao.UPI.equalsIgnoreCase(payMode)) { boolean flag =
	 * requestPayment(request); String invoiceID = data.get(JsonConstants.TNX_ID) ==
	 * null ? Constants.EMPTY_STRING : data.getString(JsonConstants.TNX_ID);
	 * collection.setInvoiceId(invoiceID);
	 * 
	 * }
	 * 
	 * if (data.has(JsonConstants.LOAN)) { JSONObject loanJSON = (JSONObject)
	 * data.getJSONObject(JsonConstants.LOAN);
	 * 
	 * if (data.has(JsonConstants.LOAN_OVERDUE)) {
	 * collection.setOverdue(Double.parseDouble(
	 * loanJSON.get(JsonConstants.LOAN_OVERDUE) == null ||
	 * loanJSON.get(JsonConstants.LOAN_OVERDUE)
	 * .toString().equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0" :
	 * loanJSON.get(JsonConstants.LOAN_OVERDUE).toString())); }
	 * 
	 * collection.setPenalAmt(Double.parseDouble(loanJSON.get(JsonConstants.
	 * LOAN_PENAL_AMOUNT) == null ||
	 * loanJSON.get(JsonConstants.LOAN_PENAL_AMOUNT).toString()
	 * .equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0" : loanJSON.get(
	 * JsonConstants.LOAN_PENAL_AMOUNT).toString()));
	 * 
	 * // commented as getting error for appr amount and sam is set at // the bottom
	 * with amount
	 * 
	 * collection .setAppropriateAmount(Double.parseDouble(loanJSON
	 * .get(JsonConstants.APPR_AMOUNT) == null || loanJSON
	 * .get(JsonConstants.APPR_AMOUNT) .toString() .equalsIgnoreCase(
	 * Constants.EMPTY_STRING) ? "0.0" : loanJSON.get(JsonConstants.APPR_AMOUNT)
	 * .toString()));
	 * 
	 * 
	 * JSONArray transType = loanJSON.getJSONArray(JsonConstants.LOAN_TRANS_TYPE);
	 * 
	 * List<TransactionType> transTypeList = new ArrayList<TransactionType>();
	 * log.info("----test 0"); for (int i = 0; i < (transType.length()); i++) {
	 * JSONObject transJSON = transType.getJSONObject(i); TransactionType
	 * transactionType = new TransactionType();
	 * transactionType.setType(transJSON.get(JsonConstants.LOAN_TRANS_TYPE_TYPE).
	 * toString());
	 * transactionType.setAmount(transJSON.get(JsonConstants.LOAN_TRANS_TYPE_AMOUNT)
	 * == null || transJSON
	 * .getString(JsonConstants.LOAN_TRANS_TYPE_AMOUNT).equalsIgnoreCase(Constants.
	 * EMPTY_STRING) ? "0.0" :
	 * transJSON.get(JsonConstants.LOAN_TRANS_TYPE_AMOUNT).toString());
	 * 
	 * transTypeList.add(transactionType); }
	 * 
	 * collection.setTransType(transTypeList); log.info("transTypeList : " +
	 * transTypeList);
	 * 
	 * }
	 * 
	 * if (data.has(JsonConstants.CREDIT_CARD)) { JSONObject ccJSON = (JSONObject)
	 * data.get(JsonConstants.CREDIT_CARD);
	 * collection.setTad(Double.parseDouble(ccJSON.get(JsonConstants.TAD) == null ||
	 * ccJSON.get(JsonConstants.TAD).toString().equalsIgnoreCase(Constants.
	 * EMPTY_STRING) ? "0.0" : ccJSON.get(JsonConstants.TAD).toString()));
	 * collection.setMad(Double.parseDouble(ccJSON.get(JsonConstants.MAD) == null ||
	 * ccJSON.get(JsonConstants.MAD).toString().equalsIgnoreCase(Constants.
	 * EMPTY_STRING) ? "0.0" : ccJSON.get(JsonConstants.MAD).toString()));
	 * collection.setBuckAmt1( Double.parseDouble(ccJSON.get("bucket1") == null ||
	 * ccJSON.get("bucket1").toString() // key changes //
	 * JsonConstants.BUCKET_AMOUNT_1 .equalsIgnoreCase(Constants.EMPTY_STRING) ?
	 * "0.0" : ccJSON.get("bucket1").toString()));
	 * collection.setBuckAmt2(Double.parseDouble(ccJSON.get("bucket2") == null ||
	 * ccJSON.get("bucket2").toString().equalsIgnoreCase(Constants.EMPTY_STRING) ?
	 * "0.0" : ccJSON.get("bucket2").toString()));// key // change //
	 * JsonConstants.BUCKET_AMOUNT_2
	 * collection.setRollbackAmt(Double.parseDouble(ccJSON.get("rollbackAmnt") ==
	 * null || ccJSON.get("rollbackAmnt").toString().equalsIgnoreCase(Constants.
	 * EMPTY_STRING) ? "0.0" : ccJSON.get("rollbackAmnt").toString()));// key //
	 * change // JsonConstants.ROLLBACK_AMOUNT }
	 * 
	 * log.info("----test 5");
	 * 
	 * JSONArray mPOSTransDetails = new JSONArray();
	 * 
	 * JSONObject mPOSTransDetail = new JSONObject(); MPOSDetail mposDetail = new
	 * MPOSDetail(); if (data.has(JsonConstants.mPOS_TRANS_DETAILS)) {
	 * mPOSTransDetail = (JSONObject) data.get(JsonConstants.mPOS_TRANS_DETAILS);
	 * 
	 * for (int i = 0; i < mPOSTransDetails.length(); i++) {
	 * 
	 * 
	 * // mPOSTransDetail = (JSONObject) mPOSTransDetails.get(i);
	 * 
	 * mposDetail.setTransactionId(mPOSTransDetail.has(JsonConstants.mPOS_TRANS_ID)
	 * ? mPOSTransDetail.get(JsonConstants.mPOS_TRANS_ID).toString() :
	 * Constants.EMPTY_STRING);
	 * mposDetail.setBillNumber(mPOSTransDetail.has(JsonConstants.mPOS_BILL_NUMBER)
	 * ? mPOSTransDetail.get(JsonConstants.mPOS_BILL_NUMBER).toString() :
	 * Constants.EMPTY_STRING);
	 * mposDetail.setCardNo(mPOSTransDetail.has(JsonConstants.mPOS_CARD_NUMBER) ?
	 * mPOSTransDetail.get(JsonConstants.mPOS_CARD_NUMBER).toString() :
	 * Constants.EMPTY_STRING);
	 * mposDetail.setTransactionDateTime(mPOSTransDetail.has(JsonConstants.
	 * mPOS_TRANS_DATE_TIME) ?
	 * mPOSTransDetail.get(JsonConstants.mPOS_TRANS_DATE_TIME).toString() :
	 * Constants.EMPTY_STRING);
	 * mposDetail.setSwipeAmount(mPOSTransDetail.has(JsonConstants.
	 * mPOS_SWIPE_AMOUNT) ?
	 * mPOSTransDetail.get(JsonConstants.mPOS_SWIPE_AMOUNT).toString() :
	 * Constants.EMPTY_STRING);
	 * mposDetail.setCardHolderName(mPOSTransDetail.has(JsonConstants.
	 * mPOS_CARD_HOLDER_NAME) ?
	 * mPOSTransDetail.get(JsonConstants.mPOS_CARD_HOLDER_NAME).toString() :
	 * Constants.EMPTY_STRING);
	 * mposDetail.setCardType(mPOSTransDetail.has(JsonConstants.mPOS_CARD_TYPE) ?
	 * mPOSTransDetail.get(JsonConstants.mPOS_CARD_TYPE).toString() :
	 * Constants.EMPTY_STRING);
	 * mposDetail.setMdrAmnt(mPOSTransDetail.has(JsonConstants.mPOS_MDR_AMOUNT) ?
	 * mPOSTransDetail.get(JsonConstants.mPOS_MDR_AMOUNT).toString() :
	 * Constants.EMPTY_STRING);
	 * mposDetail.setServiceTaxAmnt(mPOSTransDetail.has(JsonConstants.
	 * mPOS_SERVICETAX_AMOUNT) ?
	 * mPOSTransDetail.get(JsonConstants.mPOS_SERVICETAX_AMOUNT).toString() :
	 * Constants.EMPTY_STRING); }
	 * 
	 * collection.setMposDetail(mposDetail);
	 * 
	 * }
	 * 
	 * if (type.toString().equalsIgnoreCase("collections")) {
	 * collection.setCollectionType(Constants.COLLECTIONS);
	 * 
	 * collection.setCollectionNature(Constants.COLLECTION_NATURE_GENERAL); } if
	 * (type.toString().equalsIgnoreCase("randomCollections")) {
	 * collection.setCollectionType(Constants.RANDOM_COLLECTIONS);
	 * 
	 * collection.setCollectionNature(Constants.COLLECTION_NATURE_GENERAL); } if
	 * (type.toString().equalsIgnoreCase("fileCollections")) {
	 * collection.setCollectionType(Constants.COLLECTION_TYPE_FILE);
	 * 
	 * collection.setCollectionNature(Constants.COLLECTION_NATURE_FILE); } if
	 * (type.toString().equalsIgnoreCase("fileRandomCollections")) {
	 * collection.setCollectionType(Constants.COLLECTION_TYPE_FILE_RANDOM);
	 * 
	 * collection.setCollectionNature(Constants.COLLECTION_NATURE_FILE); }
	 * 
	 * if (type.toString().equalsIgnoreCase("collections") ||
	 * type.toString().equalsIgnoreCase("fileCollections")) {
	 * log.info("--- file collection or else ---"); if
	 * (data.has(JsonConstants.PARTY_NAME)) {
	 * 
	 * collection.setPartyName(data.getString(JsonConstants.PARTY_NAME)); } } if
	 * (type.toString().equalsIgnoreCase("randomCollections") ||
	 * type.toString().equalsIgnoreCase("fileRandomCollections")) { if
	 * (data.has(JsonConstants.NAME)) {
	 * collection.setPartyName(data.getString(JsonConstants.NAME)); } }
	 * 
	 * if (type.toString().equalsIgnoreCase("collections") ||
	 * type.toString().equalsIgnoreCase("fileCollections")) {
	 * 
	 * if (data.has(JsonConstants.PARTY_ID)) {
	 * 
	 * collection.setContractAccountNumber(data.getString(JsonConstants.PARTY_ID));
	 * } }
	 * 
	 * if (type.toString().equalsIgnoreCase("randomCollections") ||
	 * type.toString().equalsIgnoreCase("fileRandomCollections")) { if
	 * (data.has(JsonConstants.CONTRACT_ACCOUNT_NUMBER)) {
	 * collection.setContractAccountNumber(data.getString(JsonConstants.
	 * CONTRACT_ACCOUNT_NUMBER)); } }
	 * 
	 * Added
	 * 
	 * if (data.has(JsonConstants.INVOICE_ID_DEVICE)) {
	 * collection.setInvoiceId(data.getString(JsonConstants.INVOICE_ID_DEVICE)); }
	 * 
	 * // 15 if (data.has(Constants.START_VISIT_TIME)) {
	 * collection.setVisitStartTime(data.getString(Constants.START_VISIT_TIME));
	 * log.info("setVisitStartTime" + data.getString(Constants.START_VISIT_TIME));
	 * 
	 * } if (data.has(Constants.END_VISIT_TIME)) {
	 * collection.setVisitEndTime(data.getString(Constants.END_VISIT_TIME));
	 * log.info("setVisitEndTime" + data.getString(Constants.END_VISIT_TIME)); }
	 * 
	 * // if (data.has(JsonConstants.LocationTracker.START_LAT) &&
	 * data.has(JsonConstants.LocationTracker.START_LONG)) { start_lat =
	 * data.get(JsonConstants.LocationTracker.START_LAT) == null ?
	 * Constants.EMPTY_STRING :
	 * data.getString(JsonConstants.LocationTracker.START_LAT); start_long =
	 * data.get(JsonConstants.LocationTracker.START_LONG) == null ?
	 * Constants.EMPTY_STRING :
	 * data.getString(JsonConstants.LocationTracker.START_LONG);
	 * log.info("startlatLong " + start_lat + start_long); } else { start_lat =
	 * "0.00"; start_long = "0.00"; }
	 * 
	 * if (data.has(JsonConstants.LocationTracker.END_LAT) &&
	 * data.has(JsonConstants.LocationTracker.END_LONG)) { end_lat =
	 * data.get(JsonConstants.LocationTracker.END_LAT) == null ?
	 * Constants.EMPTY_STRING :
	 * data.getString(JsonConstants.LocationTracker.END_LAT); end_long =
	 * data.get(JsonConstants.LocationTracker.END_LONG) == null ?
	 * Constants.EMPTY_STRING :
	 * data.getString(JsonConstants.LocationTracker.END_LONG);
	 * log.info("endlatLong " + end_lat + end_long); } else { end_lat = "0.00";
	 * end_long = "0.00"; } // 12 collection.setStartLatitude(start_lat);
	 * collection.setStartLongitude(start_long); collection.setEndLatitude(end_lat);
	 * collection.setEndLongitude(end_long);
	 * 
	 * String prodType=""; if(data.has("prodType")){ prodType=data.get("prodType")
	 * == null ? Constants.EMPTY_STRING : (String) data.get("prodType"); }
	 * 
	 * if(data.has("productType")){ prodType=data.get("productType") == null ?
	 * Constants.EMPTY_STRING : (String) data.get("productType"); }
	 * log.info("productType in kafka submission "+prodType);
	 * collection.setProdType(prodType); //
	 * collection.setProdType(data.has("prodType") == true ?
	 * data.getString("prodType") : Constants.EMPTY_STRING);
	 * 
	 * 
	 * if (type.toString().equalsIgnoreCase("collections") ||
	 * type.toString().equalsIgnoreCase("fileCollections")) { if
	 * (data.has(JsonConstants.CC_APAC)) {
	 * collection.setCcapac(data.getString(JsonConstants.CC_APAC)); } }
	 * 
	 * if (type.toString().equalsIgnoreCase("randomCollections") ||
	 * type.toString().equalsIgnoreCase("fileRandomCollections")) { if
	 * (data.has(JsonConstants.CC_APAC)) {
	 * collection.setCcapac(data.getString(JsonConstants.CC_APAC)); } }
	 * 
	 * 
	 * if (data.has(JsonConstants.CC_APAC)) {
	 * collection.setCcapac(data.getString(JsonConstants.CC_APAC)); }
	 * 
	 * // TODO Code to add Multiple APACS int numberOfApacs = 1; // numberOfApacs =
	 * data.getString("noOfApac") == null ? 0 : //
	 * Integer.parseInt(data.getString("noOfApac"));
	 * 
	 * collection.setNumberOfApacs(numberOfApacs);
	 * 
	 * // collection.setContractAccountNumber(contractAccountNumber);
	 * collection.setCollectionCode(collectionCode);
	 * collection.setAllocationNumber(allocationNumber); collection
	 * .setRequestId(data.has(JsonConstants.REQUEST_ID) == true ?
	 * data.getString(JsonConstants.REQUEST_ID) : new
	 * Timestamp(System.currentTimeMillis()).toString());
	 * collection.setMobileNumberNew(mobileNumberNew);
	 * 
	 * collection.setEmailAddressNew(emailAddressNew); collection.setArea(area);
	 * collection.setChequeDetails(cheques);
	 * collection.setCollectionStatus(collStatus);
	 * collection.setCollectionCode(collectionCode);
	 * collection.setReceiptNumber(receiptNumber);
	 * collection.setRevisitDate(revisitedDate); collection.setMeterReading("sms");
	 * if (payMode.equalsIgnoreCase("Debit")) { collection.setPaymentMode("ORI"); }
	 * else { collection.setPaymentMode(payMode); }
	 * 
	 * log.info("paymode %%%%%%" + collection.getPaymentMode());
	 * 
	 * collection.setDeviceDate(deviceDate); collection.setDeviceTime(deviceTime);
	 * collection.setSubmissionDateTime(Utilities.sysDate());
	 * collection.setRemarks(remarks); collection.setCurrentBillNo(billNo);
	 * collection.setBusinessPartnerNumber(data.getString(JsonConstants.
	 * UNIQUE_NUMBER)); // apac or card // number
	 * collection.setAppl(data.getString(JsonConstants.APPL));
	 * collection.setBillCycle(billCycle); //
	 * collection.setName(data.getString("name")); if
	 * (data.has(JsonConstants.MOBILE_NUMBER)) {
	 * collection.setMobileNumber(data.getString(JsonConstants.MOBILE_NUMBER)); }
	 * collection.setSignaturePath(signaturePath); collection.setImages(images);
	 * collection.setUserName(systemUserNew.getName()); // collection.setPan(pan);
	 * collection.setUser(systemUserNew); // new added
	 * collection.setContact(contact); collection.setEmail(email);
	 * collection.setBatchNumber(batchNumber); collection.setAmount(amount);
	 * collection.setAppropriateAmount(amount);
	 * collection.setArFeedbackCode(feedback_code == null ? " " : feedback_code);
	 * collection.setPtpAmount(ptpAmount); //
	 * collection.setNextActionCode(nextActionCode); //
	 * collection.setNextActionCodeDescription(nextActionCodeDescription);
	 * collection.setBranchName(branchName); collection.setPaymentDate(paymentDate);
	 * collection.setDepositionStatus(Constants.Deposition.INITIAL_DEPOSITION);
	 * collection.setAllRisk(allRisk); collection.setTeam(team);
	 * collection.setBucket(bucket);
	 * 
	 * log.info("----pan details----" + collection.getPan());
	 * 
	 * if (type.toString().equalsIgnoreCase("collections") ||
	 * type.toString().equalsIgnoreCase("randomCollections")) {
	 * collection.setLatitude(latitude); collection.setLongitude(longitude);
	 * 
	 * }
	 * 
	 * Agency agency = new Agency();
	 * agency.setAgencyId(systemUserNew.getAgencyId()); List<Agency> agencies =
	 * agencyService.searchAgency(agency); try {
	 * collection.setAgencyName(agencies.get(0).getAgencyName()); }catch(Exception
	 * e) { log.info("Exception ::: " + e); }
	 * log.info("collection trans type ------------------>" +
	 * collection.getTransType());
	 * 
	 * Utilities.primaryBeanSetter(collection, systemUserNew);
	 * 
	 * 
	 * if (!data.has(JsonConstants.RequestData.SIGN)) { returnMessage =
	 * JsonConstants.COLLECTION_SIGNATURE_MANDATORY; return responseBuilder(message,
	 * status, returnMessage, Constants.EMPTY_STRING); } else { signature = (String)
	 * data.get(JsonConstants.RequestData.SIGN); }
	 * 
	 * 
	 * if (data.has(JsonConstants.RequestData.SIGN)) { signature = (String)
	 * data.get(JsonConstants.RequestData.SIGN);
	 * 
	 * // signature = data.get(JsonConstants.RequestData.SIGN) == null ? //
	 * Constants.EMPTY_STRING : (String) //
	 * data.get(JsonConstants.RequestData.SIGN); }
	 * 
	 * if (!signature.isEmpty()) { signaturePath = extractImagePath(collection,
	 * signature, Constants.SIGNATURE_IMAGE_FILE_PATH, Constants.EMPTY_STRING); }
	 * 
	 * if (type.toString().equalsIgnoreCase("collections") ||
	 * type.toString().equalsIgnoreCase("randomCollections") ||
	 * type.toString().equalsIgnoreCase("fileCollections") ||
	 * type.toString().equalsIgnoreCase("fileRandomCollections")) { if
	 * (!data.has(JsonConstants.RequestData.IMAGES)) {
	 * 
	 * 
	 * returnMessage = JsonConstants.COLLECTION_SUBMIT_FAILURE; return
	 * responseBuilder(message, status, returnMessage, Constants.EMPTY_STRING);
	 * 
	 * } else {
	 * 
	 * JSONArray imageDetails = data.getJSONArray(JsonConstants.RequestData.IMAGES);
	 * 
	 * log.info("---image Details ----" + imageDetails);
	 * 
	 * images = getImages(systemUserNew, imageDetails, collection);
	 * 
	 * collection.setImages(images);
	 * 
	 * log.info("--- image---" + images);
	 * 
	 * if (images == null) {
	 * 
	 * status = JsonConstants.FAILURE; returnMessage = "Image Path Not Found";
	 * 
	 * } } }
	 * 
	 * if (collectionCode.equalsIgnoreCase(JsonConstants.CUSTOMER_UPDATE_CODE)) {
	 * try {
	 * 
	 * if (caseService.checkDuplicateCustomerData(collection)) { if
	 * (caseService.submitCustomerData(collection)) {
	 * 
	 * status = JsonConstants.SUCCESS;
	 * 
	 * }
	 * 
	 * else {
	 * 
	 * status = JsonConstants.FAILURE;
	 * 
	 * }
	 * 
	 * }
	 * 
	 * else { status = JsonConstants.SUCCESS;
	 * 
	 * }
	 * 
	 * } catch (Exception e) {
	 * 
	 * log.error("------Exception Detail while submission of customer is ", e);
	 * 
	 * status = JsonConstants.FAILURE;
	 * 
	 * }
	 * 
	 * }
	 * 
	 * int i = collectionService.checkDuplicateCollectionJSON(collection); if (i ==
	 * 0) { log.info("collection cheque details ========----------->" +
	 * collection.getChequeDetails());
	 * log.info("complete collection -------------->" + collection);
	 * 
	 * submissionFlag = collectionService.submitCollection(collection);
	 * log.info("-------submissionFlag--------" + submissionFlag);
	 * 
	 * if (submissionFlag) { log.info("Collection submitted without violation");
	 * caseService.updateCase(collection, collStatus, allocStatus);
	 * 
	 * // Code for sending sms to PTP / Broken Promisev / Door Lock
	 * 
	 * try {
	 * 
	 * 
	 * if (collection.getCollectionCode().equalsIgnoreCase("PTP") ||
	 * collection.getCollectionCode().equalsIgnoreCase("DL") ||
	 * collection.getCollectionCode().equalsIgnoreCase("BRP")) {
	 * 
	 * log.info("---PTP SMS ---");
	 * 
	 * // sendSms(collection, systemUserNew); }
	 * 
	 * log.info("getMeterReading " + collection.getMeterReading());
	 * log.info("collection type" + collection.getCollectionNature()); if
	 * (collection.getCollectionCode().equalsIgnoreCase("RTP")) {
	 * 
	 * try {
	 * 
	 * log.info("before sending sms testing "); //sendCollectionsSms(collection,
	 * systemUserNew);
	 * 
	 * log.info("testing 1");
	 * 
	 * // offlineSMSService.updateOfflineSMSData(collection.getReceiptNumber());
	 * 
	 * } catch (Exception e) { log.error("---Error While sending SMS---", e); }
	 * 
	 * 
	 * try { // callEmailService(collection); } catch (Exception e) {
	 * log.error("---Error While sending Email---", e); }
	 * 
	 * 
	 * }
	 * 
	 * }
	 * 
	 * } catch (Exception e) {
	 * log.info("Error while sending sms to PTP / Broken Promise / Door Lock" + e);
	 * e.printStackTrace(); }
	 * 
	 * log.
	 * info("------------- Collection Submitted and Case Updated sucessfully -------------"
	 * );
	 * 
	 * 
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
	 * 
	 * 
	 * status = JsonConstants.SUCCESS;
	 * 
	 * 
	 * UserActivityStatusUpdate userActivityStatusUpdate = new
	 * UserActivityStatusUpdate(userActivity,
	 * (ActivityLoggerConstants.STATUS_SUCCESS), userActivityService); new
	 * Thread(userActivityStatusUpdate).run();
	 * 
	 * 
	 * if (result.equalsIgnoreCase("R_75")) { returnMessage =
	 * JsonConstants.COLLECTIONS_FAILED_DUE_TO_DEPOSITION;
	 * 
	 * } else { returnMessage = "Collection got submitted successfully"; }
	 * 
	 * } else { System.out.println("Collection submitted with violation");
	 * log.info("Collection submitted with violation");
	 * 
	 * status = JsonConstants.FAILURE;
	 * 
	 * 
	 * UserActivityStatusUpdate userActivityStatusUpdate = new
	 * UserActivityStatusUpdate(userActivity,
	 * (ActivityLoggerConstants.STATUS_FAILURE), userActivityService); new
	 * Thread(userActivityStatusUpdate).run();
	 * 
	 * 
	 * } // status = JsonConstants.SUCCESS;
	 * 
	 * } else if (i == 1) { log.
	 * info("--------- Collection Record already exists, JSON Duplicated! ------------"
	 * ); status = JsonConstants.SUCCESS; // status = JsonConstants.FAILURE;
	 * 
	 * // returnMessage = JsonConstants.COLLECTION_SUBMIT_SUCCESS;
	 * 
	 * returnMessage = "JSON DUPLICATED!!!";
	 * 
	 * if (type.toString().equalsIgnoreCase("collections")) { returnMessage =
	 * "JSON DUPLICATED For Collections!!!"; } if
	 * (type.toString().equalsIgnoreCase("randomCollections")) { returnMessage =
	 * "JSON DUPLICATED For RandomCollections!!!"; } if
	 * (type.toString().equalsIgnoreCase("fileCollections")) { returnMessage =
	 * "JSON DUPLICATED For FileCollections!!!"; } if
	 * (type.toString().equalsIgnoreCase("fileRandomCollections")) { returnMessage =
	 * "JSON DUPLICATED For FileRandomCollections!!!"; }
	 * 
	 * 
	 * UserActivityStatusUpdate userActivityStatusUpdate = new
	 * UserActivityStatusUpdate(userActivity,
	 * (ActivityLoggerConstants.STATUS_IGNORE), userActivityService); new
	 * Thread(userActivityStatusUpdate).run();
	 * 
	 * 
	 * } else { log.info("Some error occured at Dao checkDuplicateCollectionJSON ");
	 * 
	 * status = JsonConstants.FAILURE;
	 * 
	 * 
	 * UserActivityStatusUpdate userActivityStatusUpdate = new
	 * UserActivityStatusUpdate(userActivity,
	 * (ActivityLoggerConstants.STATUS_FAILURE), userActivityService); new
	 * Thread(userActivityStatusUpdate).run();
	 * 
	 * 
	 * } }
	 * 
	 * catch (Exception e) { e.printStackTrace();
	 * log.error("--- Exception In CollectionSubmissionService :: " + e);
	 * 
	 * returnMessage = JsonConstants.COLLECTION_SUBMIT_FAILURE;
	 * 
	 * }
	 * 
	 * return status;
	 * 
	 * }
	 */

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

}
