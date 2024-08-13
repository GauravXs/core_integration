/**
 ****************************************************************************** 
 * C O P Y R I G H T A N D C O N F I D E N T I A L I T Y N O T I C E
 * <p>
 * Copyright Â© 2013-2014 Mobicule Technologies Pvt. Ltd. All rights reserved.
 * This is proprietary information of Mobicule Technologies Pvt. Ltd.and is
 * subject to applicable licensing agreements. Unauthorized reproduction,
 * transmission or distribution of this file and its contents is a violation of
 * applicable laws.
 ****************************************************************************** 
 * 
 * @project mCollectionsKMIntegration-Phase2
 */
package com.mobicule.mcollections.integration.collection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

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
import com.mobicule.mcollections.core.beans.Cheque;
import com.mobicule.mcollections.core.beans.Collection;
import com.mobicule.mcollections.core.beans.Denomination;
import com.mobicule.mcollections.core.beans.Image;
import com.mobicule.mcollections.core.beans.Settlement;
import com.mobicule.mcollections.core.beans.SettlementCasesDocument;
import com.mobicule.mcollections.core.beans.SettlementEMIDetail;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.AES;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.EmailUtilities;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.AgencyService;
import com.mobicule.mcollections.core.service.CaseService;
import com.mobicule.mcollections.core.service.CollectionService;
import com.mobicule.mcollections.core.service.RandomCollectionsExternalService;
import com.mobicule.mcollections.core.service.SettlementService;
import com.mobicule.mcollections.core.service.SystemUserService;
import com.mobicule.mcollections.core.thread.SubmitSettlementImageThread;
import com.mobicule.mcollections.integration.commons.ServerUtilities;
import com.mobicule.mcollections.integration.commons.XMLConstants;
import com.mobicule.mcollections.webservice.adapter.KotakCollectionWebserviceAdapter;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
 * @copyright Â© 2013-2014 Mobicule Technologies Pvt. Ltd. All rights reserved.
 */
/** @author prashant */
public class SettlementSubmissionService implements ICollectionsSubmissionService
{

	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private UserActivityService userActivityService;

	@Autowired
	private SystemUserService systemUserService;

	@Autowired
	private CommunicationActivityService communicationActivityService;

	@Autowired
	private RandomCollectionsExternalService randomCollectionsExternalService;

	@Autowired
	private NotificationActivityService notificationActivityService;

	@Autowired
	private KotakCollectionWebserviceAdapter webserviceAdapter;

	@Autowired
	private CollectionService collectionService;

	@Autowired
	private SettlementService settlementService;

	@Autowired
	private CaseService caseService;

	@Autowired
	private AgencyService agencyService;

	@Autowired
	private ApplicationConfiguration<String, String> applicationConfiguration;

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

	public EmailUtilities getEmailService()
	{
		return emailService;
	}

	public void setEmailService(EmailUtilities emailService)
	{
		this.emailService = emailService;
	}

	@Override
	public Message<String> execute(Message<String> message) throws Throwable
	{
		log.info(" -------- Inside Submit Settment Case -------- ");
		
		int validityCount = 0;
		String status = JsonConstants.FAILURE;
		String returnMessage = null;
		String deviceDateTime = "";
		String appl = "";
		String apacNo = "";
		String apacdate = "";
		String partyName = "";

		String emiAmnt = "";
		String loanAmnt = "";
		String tenure = "";
		String noOfEmi = "";

		String baltenure = "";
		String longitude = "";
		String latitude = "";

		String principaloutstanding = "";
		String oPLUSs = "";
		String penalOstd = "";
		String fcCharges = "";
		String unqNo = "";
		String settlementAmnt = "";
		String partyCorrAdd = "";
		String settlmentTerm = "";
		String ca = "";
		String noofstroke = "";
		String detailsPDC = "";
		String area = "";
		String accountingLoss = "";
		String finacialLoss = "";
		String accLossPOSVal = "";
		String finLossFRAmnt = "";
		String recovery = "";
		String bucket = "";
		String dPD = "";
		String requId = "";
		String legalStatus = "";
		String cibilScore = "";
		String loanRecallNoticeSentDate = "";
		String sec138InitDate = "";
		String arbitrationInitDate = "";
		String payCollDate = "";
		String reasonForSettlement = "";
		String otherInfo = "";
		String firstostddate = "";
		String penalReceiv = "";
		String totalOutstand = "";
		String landlineNumber = "";
		String brokenrevenue = "";
		String legalcharges = "";
		String partyId = "";
		String regno = "";
		String emipaid = "";
		String closurebal = "";
		String overdue = "";
		String dueAmnt = "";
		String dueDate = "";

		String penalWaiver = "";
		String principleWaiver = "";
		String totalFR = "";
		String caseBrief = "";
		String promoCode = "";
		String caseId = "";
		String ptpAmnt = "";
		String remarks = "";
		String fileUrl = "";
		JSONArray imageDetails = new JSONArray();

		JSONArray emiDetailsArray = new JSONArray();

		ArrayList<SettlementEMIDetail> emiDetailsList = new ArrayList<SettlementEMIDetail>();

		boolean submissionFlag = false;

		List<Image> images = new ArrayList<Image>();
		MessageHeaders messageHeader = message.getHeaders();

		SystemUser systemUser = (SystemUser) messageHeader.get(Constants.SYSTEM_USER_BEAN);

		Settlement settlement = new Settlement();

		try
		{
			fileUrl = applicationConfiguration.getValue("settlementRestUrl");
			log.info("fileUrl :: " + fileUrl);
			String requestSet = message.getPayload();
			log.info("111111111--- requestSet == " + requestSet);
			JSONObject jsonObj = new JSONObject(requestSet);
			JSONObject jsonData = (JSONObject) jsonObj.get("data");
			jsonData.remove("images");
			jsonObj.put("data", jsonData);
			String reqSet = jsonObj.toString();
			log.info("222222222--- reqSet == " + reqSet);

			/*
			 * UserActivityAddition userActivityAddition = new UserActivityAddition(reqSet,
			 * userActivityService, ActivityLoggerConstants.DATABASE_MSSQL); new
			 * Thread(userActivityAddition).run();
			 * 
			 * UserActivity userActivity = userActivityAddition.extractUserActivity();
			 */

			JSONObject jsonObject = new JSONObject(requestSet);
			JSONObject data = (JSONObject) jsonObject.get(JsonConstants.DATA);
			
			jsonObject.get(JsonConstants.DATA);
			
			
			/*
			 * jsonObject.get(JsonConstants.DATA); JSONArray imageArray = (JSONArray)
			 * data.get("images"); String imageString = getImagesByte(imageArray); String
			 * fileName = settlement.getApacNo() + Constants.SYMBOL_UNDERSCORE +
			 * System.currentTimeMillis(); MultipartFile filePart =
			 * convertToMultipartFile(imageString.getBytes(), fileName);
			 * 
			 * InputStream is = filePart.getInputStream(); FileStorage fileStorage = null;
			 * 
			 * FileRef fileRef = uploadToFileStorage(fileStorage, is, filePart.getName());
			 * String fileData = fileRef.toString(); log.info("fileData :::; " + fileData);
			 */
			/*
			 * File outputFile = tempFolder.newFile("outputFile.jpg"); try (FileOutputStream
			 * outputStream = new FileOutputStream(outputFile)) {
			 * outputStream.write(dataForWriting); }
			 */
			/*
			 * OkHttpClient client = new OkHttpClient().newBuilder() .build(); MediaType
			 * mediaType = MediaType.parse("text/plain"); RequestBody body = new
			 * MultipartBody.Builder().setType(MultipartBody.FORM)
			 * .addFormDataPart("file","sample.jpg",
			 * RequestBody.create(MediaType.parse("application/octet-stream"),
			 * FileUtils.writeByteArrayToFile(new File("pathname"),
			 * imageString.getBytes())))
			 * 
			 * .addFormDataPart("filetype","FRONT_VIEW") .build(); Request request = new
			 * Request.Builder() .url("http://10.1.1.196:8083/rest/files") .method("POST",
			 * body) .addHeader("Authorization", "Bearer gmq88fZ83hwudIMn9Q43F3tMqD0")
			 * .addHeader("Cookie", "JSESSIONID=28E4051C8CEF780BCF0EB7A549477565") .build();
			 * Response response = client.newCall(request).execute();
			 */
			
			
			String checkApacs = Constants.EMPTY_STRING;
			
			if(data.has("apacNo")) {
				checkApacs = data.optString("apacNo") == null ? "" : data.optString("apacNo");
				data.put("unqNo", data.optString("apacNo") == null ? "" : data.optString("apacNo"));
				log.info("CheckApacs :::" + checkApacs);
			}
			if( checkApacs.equalsIgnoreCase(Constants.EMPTY_STRING)) {
				if(data.has(JsonConstants.SettlmentConstant.UNIQUE_NUMBER)) {
					checkApacs = data.get(JsonConstants.SettlmentConstant.UNIQUE_NUMBER)== null ? "" : (String) data.get(JsonConstants.SettlmentConstant.UNIQUE_NUMBER);
					unqNo = data.get(JsonConstants.SettlmentConstant.UNIQUE_NUMBER)== null ? "" : (String) data.get(JsonConstants.SettlmentConstant.UNIQUE_NUMBER);
					data.put("unqNo", unqNo);
					log.info("CheckApacs :::" + checkApacs);
				}
			}
			if(checkApacs.equalsIgnoreCase(Constants.EMPTY_STRING)) {
				if(data.has("apacCardNumber")) {
					checkApacs = data.get("apacCardNumber")== null ? "" : (String) data.get("apacCardNumber");
					data.put("unqNo", data.get("apacCardNumber")== null ? "" : (String) data.get("apacCardNumber"));
					log.info("CheckApacs :::" + checkApacs);
				}
			}
			
			
			JSONObject user = (JSONObject) jsonObject.get(JsonConstants.SYSTEM_USER);
			SystemUser systemUserNew = ServerUtilities.extractSystemUser(user);

			log.info("-----systemUserNew-----" + systemUserNew.toString());

			Map reqMap = Utilities.createMapFromJSON(requestSet);

			log.info("----reqmap----" + reqMap.toString());

			String type = (String) reqMap.get(JsonConstants.Key.TYPE);

			/*
			 * try {
			 */
			appl = data.get(JsonConstants.APPL) == null ? "" : (String) data.get(JsonConstants.APPL);
			
			caseId = data.optString("caseId") == null ? "" : (String) data.optString("caseId");
			log.info("caseId ::" + caseId);
			if (Utilities.checkInputForValidity(appl))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " appl = " + appl);
			}
			
			apacNo = data.optString(JsonConstants.APAC_NO) == null ? "" : (String) data.optString(JsonConstants.APAC_NO);
			if(apacNo.equalsIgnoreCase(Constants.EMPTY_STRING)) {
				apacNo = data.get(JsonConstants.SettlmentConstant.UNIQUE_NUMBER)== null ? "" : (String) data.get(JsonConstants.SettlmentConstant.UNIQUE_NUMBER);
			}
			else if(apacNo.equalsIgnoreCase(Constants.EMPTY_STRING)) {
				apacNo = data.get("apacCardNumber")== null ? "" : (String) data.get("apacCardNumber");
			}
			log.info("apacNo printed before validity check ::" + apacNo);
			
			if (Utilities.checkInputForValidity(apacNo))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " apacNo = " + apacNo);
			}
			
			

			if(data.has(JsonConstants.APAC_DATE))
			{
				apacdate = data.get(JsonConstants.APAC_DATE) == null ? "" : (String) data.get(JsonConstants.APAC_DATE);
			if (Utilities.checkInputForDateValidity(apacdate))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " apacdate = " + apacdate);
			}
			}
			
			if (data.has(JsonConstants.LATITUDE) && data.has(JsonConstants.LONGITUDE))
			{
				latitude = data.get(JsonConstants.LATITUDE) == null ? "" : data.getString(JsonConstants.LATITUDE);
				if (Utilities.checkInputForDouble(latitude))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " latitude = " + latitude);
				}
				longitude = data.get(JsonConstants.LONGITUDE) == null ? "" : data.getString(JsonConstants.LONGITUDE);
				if (Utilities.checkInputForDouble(longitude))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " longitude = " + longitude);
				}
			}
			else
			{
				latitude = "0.00";
				longitude = "0.00";
			}

			penalWaiver = data.optString(JsonConstants.SettlmentConstant.PENAL_WAVER) == null ? "" : data.optString(
					JsonConstants.SettlmentConstant.PENAL_WAVER).toString();
			if (Utilities.checkInputForDouble(penalWaiver))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " penalWaiver = " + penalWaiver);
			}

			principleWaiver = data.optString(JsonConstants.SettlmentConstant.PRINCIPLE_WAVER) == null ? "" : data.optString(
					JsonConstants.SettlmentConstant.PRINCIPLE_WAVER).toString();
			if (Utilities.checkInputForDouble(principleWaiver))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " principleWaiver = " + principleWaiver);
			}

			partyName = data.optString(JsonConstants.PARTY_NAME) == null ? "" : (String) data.optString(JsonConstants.PARTY_NAME);
			/*
			 * if (Utilities.checkInputForValidity(partyName)) { validityCount =
			 * validityCount + 1; log.info("--- validityCount = " + validityCount +
			 * " partyName = " + partyName); }
			 */

			deviceDateTime = data.optString(JsonConstants.DEVICE_DATE_TIME) == null ? "" : (String) data.optString(JsonConstants.DEVICE_DATE_TIME);

			emiAmnt = data.optString(JsonConstants.SettlmentConstant.EMI_AMOUNT) == null ? "" : (String) data.optString(JsonConstants.SettlmentConstant.EMI_AMOUNT);
			if (Utilities.checkInputForDouble(emiAmnt))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " emiAmnt = " + emiAmnt);
			}

			if(data.has(JsonConstants.SettlmentConstant.LOAN_AMOUNT))
			{
			loanAmnt = data.optString(JsonConstants.SettlmentConstant.LOAN_AMOUNT) == null ? "" : (String) data
					.optString(JsonConstants.SettlmentConstant.LOAN_AMOUNT);
			}
			if (Utilities.checkInputForDouble(loanAmnt))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " loanAmnt = " + loanAmnt);
			}

			if(data.has(JsonConstants.SettlmentConstant.TENURE))
			{
			tenure = data.optString(JsonConstants.SettlmentConstant.TENURE) == null ? "" : (String) data
					.optString(JsonConstants.SettlmentConstant.TENURE);
			if (Utilities.checkInputForDouble(tenure))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " tenure = " + tenure);
			}
			}
			/* noOfEmi = data.get(JsonConstants.SettlmentConstant.NUMBER_OF_EMI)
			== null ? "" : (String) data
			.get(JsonConstants.SettlmentConstant.NUMBER_OF_EMI);*/

			if(data.has(JsonConstants.SettlmentConstant.BALNACE_TENURE))
			{
				baltenure = data.get(JsonConstants.SettlmentConstant.BALNACE_TENURE) == null ? "" : (String) data
						.get(JsonConstants.SettlmentConstant.BALNACE_TENURE);
				if (Utilities.checkInputForDouble(baltenure))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " baltenure = " + baltenure);
				}
			}

			/*
			 * tenure = data.get(JsonConstants.SettlmentConstant.TENURE) == null
			 * ? "" : (String) data
			 * .get(JsonConstants.SettlmentConstant.TENURE); //llll tenure =
			 * data.get(JsonConstants.SettlmentConstant.TENURE) == null ? "" :
			 * (String) data .get(JsonConstants.SettlmentConstant.TENURE);
			 */
			
			if(data.has(JsonConstants.SettlmentConstant.PRINCIPAL_OUTSTANDING))
			{
				principaloutstanding = data.get(JsonConstants.SettlmentConstant.PRINCIPAL_OUTSTANDING) == null ? ""
						: (String) data.get(JsonConstants.SettlmentConstant.PRINCIPAL_OUTSTANDING);
				
				if (Utilities.checkInputForDouble(principaloutstanding))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " principaloutstanding = " + principaloutstanding);
				}
			}
			if(data.has(JsonConstants.SettlmentConstant.O_PLUS_S))
			{
				log.info("O_PLUS_S :-" + data.get(JsonConstants.SettlmentConstant.O_PLUS_S));
				oPLUSs = data.get(JsonConstants.SettlmentConstant.O_PLUS_S) == null ? "" : (String) data
						.get(JsonConstants.SettlmentConstant.O_PLUS_S);
				log.info("oPLUSs :-" + oPLUSs);
				if (Utilities.checkInputForDouble(oPLUSs))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " oPLUSs = " + oPLUSs);
				}
			}

			if(data.has(JsonConstants.SettlmentConstant.PENAL_OUTSTANDING))
			{
				penalOstd = data.get(JsonConstants.SettlmentConstant.PENAL_OUTSTANDING) == null ? "" : (String) data
						.get(JsonConstants.SettlmentConstant.PENAL_OUTSTANDING);
				if (Utilities.checkInputForDouble(penalOstd))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " penalOstd = " + penalOstd);
				}
			}
			if(data.has(JsonConstants.SettlmentConstant.FC_CHARGES))
			{fcCharges = data.get(JsonConstants.SettlmentConstant.FC_CHARGES) == null ? "" : (String) data
					.get(JsonConstants.SettlmentConstant.FC_CHARGES);
			if (Utilities.checkInputForDouble(fcCharges))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " fcCharges = " + fcCharges);
			}
		}
			
		/*
		 * unqNo = data.get(JsonConstants.SettlmentConstant.UNIQUE_NUMBER)== null ? "" :
		 * (String) data.get(JsonConstants.SettlmentConstant.UNIQUE_NUMBER);
		 */
			 
			settlementAmnt = data.optString(JsonConstants.SettlmentConstant.SETTLEMENT_AMNT) == null ? "" : (String) data
					.optString(JsonConstants.SettlmentConstant.SETTLEMENT_AMNT);
			if (Utilities.checkInputForDouble(settlementAmnt))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " settlementAmnt = " + settlementAmnt);
			}

			if(data.has(JsonConstants.SettlmentConstant.PARTY_CORR_ADDRESS))
			{
			partyCorrAdd = data.get(JsonConstants.SettlmentConstant.PARTY_CORR_ADDRESS) == null ? "" : (String) data
					.get(JsonConstants.SettlmentConstant.PARTY_CORR_ADDRESS);
			}
			settlmentTerm = data.optString(JsonConstants.SettlmentConstant.SETTLEMENT_TERM) == null ? "" : (String) data
					.optString(JsonConstants.SettlmentConstant.SETTLEMENT_TERM);
			if (Utilities.checkInputForValidity(settlmentTerm))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " settlmentTerm = " + settlmentTerm);
			}

			/*
			 * ca = data.get(JsonConstants.SettlmentConstant.CA) == null ? "" :
			 * (String) data .get(JsonConstants.SettlmentConstant.CA);
			 */

			noofstroke = data.optString(JsonConstants.SettlmentConstant.NUMBER_OF_STROKE) == null ? "" : (String) data
					.optString(JsonConstants.SettlmentConstant.NUMBER_OF_STROKE);
			if (Utilities.checkInputForNumber(noofstroke))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " noofstroke = " + noofstroke);
			}

			detailsPDC = data.optString(JsonConstants.SettlmentConstant.DETAIL_PDC) == null ? "" : (String) data
					.optString(JsonConstants.SettlmentConstant.DETAIL_PDC);
			/*if (Utilities.checkInputForDouble(detailsPDC))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " detailsPDC = " + detailsPDC);
			}*/

			if(data.has(JsonConstants.SettlmentConstant.AREA))
			{
			area = data.get(JsonConstants.SettlmentConstant.AREA) == null ? "" : (String) data
					.get(JsonConstants.SettlmentConstant.AREA);
			if (Utilities.checkInputForValidity(area))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " area = " + area);
			}
			}

			finacialLoss = data.optString(JsonConstants.SettlmentConstant.FINACIAL_LOSS) == null ? "" : (String) data
					.optString(JsonConstants.SettlmentConstant.FINACIAL_LOSS);
			if (Utilities.checkInputForDouble(finacialLoss))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " finacialLoss = " + finacialLoss);
			}

			accountingLoss = data.optString(JsonConstants.SettlmentConstant.ACCOUNTING_LOSS) == null ? "" : (String) data
					.optString(JsonConstants.SettlmentConstant.ACCOUNTING_LOSS);
			if (Utilities.checkInputForDouble(accountingLoss))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " accountingLoss = " + accountingLoss);
			}

			accLossPOSVal = data.optString(JsonConstants.SettlmentConstant.ACCOUNT_LOSS_ON_POS) == null ? "" : (String) data
					.optString(JsonConstants.SettlmentConstant.ACCOUNT_LOSS_ON_POS);
			if (Utilities.checkInputForDouble(accLossPOSVal))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " accLossPOSVal = " + accLossPOSVal);
			}

			finLossFRAmnt = data.optString(JsonConstants.SettlmentConstant.FINCIAL_LOSS_ON_FR) == null ? "" : (String) data
					.optString(JsonConstants.SettlmentConstant.FINCIAL_LOSS_ON_FR);
			if (Utilities.checkInputForDouble(finLossFRAmnt))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " finLossFRAmnt = " + finLossFRAmnt);
			}

			recovery = data.optString(JsonConstants.SettlmentConstant.RECOVERY) == null ? "" : (String) data
					.optString(JsonConstants.SettlmentConstant.RECOVERY);
			if (Utilities.checkInputForDouble(recovery))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " recovery = " + recovery);
			}

			bucket = data.optString(JsonConstants.SettlmentConstant.BUCKET) == null ? "" : String.valueOf(data
					.optString(JsonConstants.SettlmentConstant.BUCKET));
			if (Utilities.checkInputForNumber(bucket))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " bucket = " + bucket);
			}

			requId = data.optString(JsonConstants.SettlmentConstant.REQUEST_ID) == null ? "" : (String) data
					.optString(JsonConstants.SettlmentConstant.REQUEST_ID);
			if (Utilities.checkInputForNumber(requId))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " requId = " + requId);
			}

			legalStatus = data.optString(JsonConstants.SettlmentConstant.LEGAL_STATUS) == null ? "" : (String) data
					.optString(JsonConstants.SettlmentConstant.LEGAL_STATUS);
			if (Utilities.checkInputForValidity(legalStatus))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " legalStatus = " + legalStatus);
			}

			loanRecallNoticeSentDate = data.optString(JsonConstants.SettlmentConstant.LOAN_RECALL_NOTICE_DATE) == null ? ""
					: (String) data.optString(JsonConstants.SettlmentConstant.LOAN_RECALL_NOTICE_DATE);
			if (Utilities.checkInputForDateValidity(loanRecallNoticeSentDate))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " loanRecallNoticeSentDate = "
						+ loanRecallNoticeSentDate);
			}

			sec138InitDate = data.optString(JsonConstants.SettlmentConstant.SECTION138_DATE) == null ? "" : (String) data
					.optString(JsonConstants.SettlmentConstant.SECTION138_DATE);
			if (Utilities.checkInputForDateValidity(sec138InitDate))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " sec138InitDate = " + sec138InitDate);
			}

			arbitrationInitDate = data.optString(JsonConstants.SettlmentConstant.ARBITRATION_DATE) == null ? ""
					: (String) data.optString(JsonConstants.SettlmentConstant.ARBITRATION_DATE);
			if (Utilities.checkInputForDateValidity(arbitrationInitDate))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " arbitrationInitDate = " + arbitrationInitDate);
			}

			payCollDate = data.optString(JsonConstants.SettlmentConstant.PAYMENT_COLL_DATE) == null ? "" : (String) data
					.optString(JsonConstants.SettlmentConstant.PAYMENT_COLL_DATE);
			if (Utilities.checkInputForDateValidity(payCollDate))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " payCollDate = " + payCollDate);
			}

			reasonForSettlement = data.optString(JsonConstants.SettlmentConstant.SETTLMENT_REASON) == null ? ""
					: (String) data.optString(JsonConstants.SettlmentConstant.SETTLMENT_REASON);
			if (Utilities.checkInputForValidity(reasonForSettlement))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " reasonForSettlement = " + reasonForSettlement);
			}

			/*//renamed as Case brief as per FRS
			 * 
			 * otherInfo = data.get(JsonConstants.SettlmentConstant.OTHER_INFO) == null ? "" : (String) data
					.get(JsonConstants.SettlmentConstant.OTHER_INFO);
			if (Utilities.checkInputForValidity(otherInfo))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " otherInfo = " + otherInfo);
			}*/
			
			
			if(data.has(JsonConstants.SettlmentConstant.FIRST_OSTD_DATE))
			{
						firstostddate = data.get(JsonConstants.SettlmentConstant.FIRST_OSTD_DATE) == null ? "" : (String) data
								.get(JsonConstants.SettlmentConstant.FIRST_OSTD_DATE);
						if (Utilities.checkInputForDateValidity(firstostddate))
						{
							validityCount = validityCount + 1;
							log.info("--- validityCount = " + validityCount + " firstostddate = " + firstostddate);
						}
			}
			penalReceiv = data.optString(JsonConstants.SettlmentConstant.PENAL_RECEIVABLE) == null ? "" : (String) data
					.optString(JsonConstants.SettlmentConstant.PENAL_RECEIVABLE);
			if (Utilities.checkInputForDateValidity(penalReceiv))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " penalReceiv = " + penalReceiv);
			}

			if(data.has(JsonConstants.SettlmentConstant.TOTAL_OUTSTANDING))
			{
			totalOutstand = data.get(JsonConstants.SettlmentConstant.TOTAL_OUTSTANDING) == null ? "" : (String) data
					.get(JsonConstants.SettlmentConstant.TOTAL_OUTSTANDING);
			if (Utilities.checkInputForDateValidity(totalOutstand))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " totalOutstand = " + totalOutstand);
			}
		}
			if(data.has(JsonConstants.SettlmentConstant.LANDLINE_NUMBER))
			{
				landlineNumber = data.get(JsonConstants.SettlmentConstant.LANDLINE_NUMBER) == null ? "" : (String) data
						.get(JsonConstants.SettlmentConstant.LANDLINE_NUMBER);
				if (Utilities.checkInputForNumber(landlineNumber))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " landlineNumber = " + landlineNumber);
				}
			}
			
			if(data.has(JsonConstants.SettlmentConstant.BROKEN_REVENUE))
			{
			brokenrevenue = data.get(JsonConstants.SettlmentConstant.BROKEN_REVENUE) == null ? "" : (String) data
					.get(JsonConstants.SettlmentConstant.BROKEN_REVENUE);
			if (Utilities.checkInputForDouble(brokenrevenue))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " brokenrevenue = " + brokenrevenue);
			}
			}

			if(data.has(JsonConstants.SettlmentConstant.LEGAL_CHARGES))
			{
			legalcharges = data.get(JsonConstants.SettlmentConstant.LEGAL_CHARGES) == null ? "" : (String) data
					.get(JsonConstants.SettlmentConstant.LEGAL_CHARGES);
			if (Utilities.checkInputForDouble(legalcharges))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " legalcharges = " + legalcharges);
			}
			}

			partyId = data.optString(JsonConstants.SettlmentConstant.PARTY_ID) == null ? "" : (String) data
					.optString(JsonConstants.SettlmentConstant.PARTY_ID);
			if (Utilities.checkAlphaNumericValidity(partyId))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " partyId = " + partyId);
			}

			/*
			 * regno =
			 * data.get(JsonConstants.SettlmentConstant.REGISTRATION_NUMBER) ==
			 * null ? "" : (String) data
			 * .get(JsonConstants.SettlmentConstant.REGISTRATION_NUMBER);
			 */

			if(data.has(JsonConstants.SettlmentConstant.EMI_PAID))
			{
				emipaid = data.get(JsonConstants.SettlmentConstant.EMI_PAID) == null ? "" : (String) data
						.get(JsonConstants.SettlmentConstant.EMI_PAID);
				if (Utilities.checkInputForDouble(emipaid))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " emipaid = " + emipaid);
				}
			}
			
			if(data.has(JsonConstants.SettlmentConstant.CLOSURE_BAL))
			{
				closurebal = data.get(JsonConstants.SettlmentConstant.CLOSURE_BAL) == null ? "" : (String) data
						.get(JsonConstants.SettlmentConstant.CLOSURE_BAL);
				if (Utilities.checkInputForDouble(closurebal))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " closurebal = " + closurebal);
				}
			}

			if(data.has(JsonConstants.SettlmentConstant.LOAN_OVERDUE))
			{
			overdue = data.get(JsonConstants.SettlmentConstant.LOAN_OVERDUE) == null ? "" : (String) data
					.get(JsonConstants.SettlmentConstant.LOAN_OVERDUE);
			if (Utilities.checkInputForDouble(overdue))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " overdue = " + overdue);
			}
			}

			/*
			 * dueAmnt = data.get(JsonConstants.SettlmentConstant.EMI_AMOUNT) ==
			 * null ? "" : (String) data
			 * .get(JsonConstants.SettlmentConstant.EMI_AMOUNT);
			 */

			if (data.has(JsonConstants.SettlmentConstant.DUE_DATE))
			{
				dueDate = data.get(JsonConstants.SettlmentConstant.DUE_DATE) == null ? "" : (String) data
						.get(JsonConstants.SettlmentConstant.DUE_DATE);
				if (Utilities.checkInputForDateValidity(dueDate))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " dueDate = " + dueDate);
				}
			}
			
			if(data.has(JsonConstants.SettlmentConstant.TOTAL_FR_AS_ON_DATE))
			{
			totalFR = data.get(JsonConstants.SettlmentConstant.TOTAL_FR_AS_ON_DATE) == null ? "" : (String) data
					.get(JsonConstants.SettlmentConstant.TOTAL_FR_AS_ON_DATE);
			}
			
			String requestEntity = data.optString(JsonConstants.APPL) == null ? "" : data.optString(JsonConstants.APPL);
			
			if(data.has(JsonConstants.SettlmentConstant.CASE_BRIEF))
			{
				caseBrief = data.get(JsonConstants.SettlmentConstant.CASE_BRIEF) == null ? "" : (String) data
						.get(JsonConstants.SettlmentConstant.CASE_BRIEF);
				if (Utilities.checkInputForValidity(caseBrief))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " caseBrief = " + caseBrief);
				}
			}
			
			if(data.has(JsonConstants.SettlmentConstant.PROMO_CODE))
			{
				promoCode = data.get(JsonConstants.SettlmentConstant.PROMO_CODE) == null ? "" : (String) data
						.get(JsonConstants.SettlmentConstant.PROMO_CODE);
				if (Utilities.checkInputForValidity(promoCode))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " promoCode = " + promoCode);
				}

			}
			
			if(data.has(JsonConstants.SettlmentConstant.EMI_DETAILS))
			{
				emiDetailsArray = data.getJSONArray(JsonConstants.SettlmentConstant.EMI_DETAILS);

				log.info("----getting emi details ----");

				String emiAmount = "";
				String bankName = "";
				String chqNumber = "";
				String emiDate = "";

				for (int i = 0; i < (emiDetailsArray.length()); i++)
				{

					JSONObject emiDetails = emiDetailsArray.getJSONObject(i);

					SettlementEMIDetail emiDet = new SettlementEMIDetail();

					emiAmount = emiDetails.optString("emiAmnt") == null ? "0" : emiDetails.optString("emiAmnt").toString();
					if (Utilities.checkInputForDouble(emiAmount))
					{
						validityCount = validityCount + 1;
						log.info("--- validityCount = " + validityCount + " emiAmount = " + emiAmount);
					}

					bankName = emiDetails.optString(JsonConstants.SettlmentConstant.BANK_NAME) == null ? "" : emiDetails.optString(
							JsonConstants.SettlmentConstant.BANK_NAME).toString();
					if (Utilities.checkInputForValidity(bankName))
					{
						validityCount = validityCount + 1;
						log.info("--- validityCount = " + validityCount + " bankName = " + bankName);
					}

					chqNumber = emiDetails.optString(JsonConstants.SettlmentConstant.CHEQUE_NUMBER) == null ? "" : emiDetails
							.optString(JsonConstants.SettlmentConstant.CHEQUE_NUMBER).toString();
					if (Utilities.checkInputForNumber(chqNumber))
					{
						validityCount = validityCount + 1;
						log.info("--- validityCount = " + validityCount + " chqNumber = " + chqNumber);
					}

					emiDate = emiDetails.optString(JsonConstants.SettlmentConstant.EMI_DATE) == null ? "" : emiDetails.optString(
							JsonConstants.SettlmentConstant.EMI_DATE).toString();
					if (Utilities.checkInputForDateValidity(emiDate))
					{
						validityCount = validityCount + 1;
						log.info("--- validityCount = " + validityCount + " emiDate = " + emiDate);
					}

					emiDet.setEmiAmount(emiAmount);
					emiDet.setBankName(bankName);
					emiDet.setChequeNo(chqNumber);
					emiDet.setEmiDate(emiDate);

					emiDetailsList.add(emiDet);
				}	
				
				settlement.setEmiDetails(emiDetailsList);
			}
					
			if(data.has("ptpAmnt")) {
                ptpAmnt = data.get("ptpAmnt") == null ? "" : (String) data
                        .get("ptpAmnt");
            }
			if (validityCount > 0)
			{
				log.info("--------- Validity Count is greater than Zero ------------");

				status = JsonConstants.FAILURE;

				returnMessage = "Invalid Data!!!";

				/*
				 * UserActivityStatusUpdate userActivityStatusUpdate = new
				 * UserActivityStatusUpdate(userActivity,
				 * (ActivityLoggerConstants.STATUS_IGNORE), userActivityService); new
				 * Thread(userActivityStatusUpdate).run(); return responseBuilder(message,
				 * status, returnMessage, requId);
				 */
			}

			log.info("---test log 2----");
			cibilScore = data.optString(JsonConstants.SettlmentConstant.CIBIL_SCRORE) == null ? "" : (String) data
					.optString(JsonConstants.SettlmentConstant.CIBIL_SCRORE);
			log.info("---cibilScore----");
			if (Utilities.checkInputForNumber(cibilScore))
			{
				log.info("---inside the false loop----" + cibilScore);
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " cibilScore = " + cibilScore);
			}
			dPD = data.optString(JsonConstants.SettlmentConstant.DPD) == null ? "" : (String) data
					.optString(JsonConstants.SettlmentConstant.DPD);
			if (Utilities.checkInputForNumber(dPD))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " dPD = " + dPD);
			}
			
			remarks = data.optString("remarks") == null ? "" : (String) data.optString("remarks");

			settlement.setAppl(appl);
			settlement.setApacNo(apacNo);
			settlement.setApacdate(apacdate);
			settlement.setPenalWaiver(penalWaiver);
			settlement.setPrincipleWaiver(principleWaiver);
			settlement.setPartyName(partyName);
			settlement.setDeviceDateTime(deviceDateTime);
			settlement.setEmiAmount(emiAmnt);
			settlement.setLoanAmnt(loanAmnt);
			settlement.setTenure(tenure);
			settlement.setNumberOfEmi(noOfEmi);
			settlement.setBaltenure(baltenure);
			settlement.setLongitude(longitude);
			settlement.setLatitude(latitude);
			settlement.setPrincipaloutstanding(principaloutstanding);
			settlement.setoPLUSs(oPLUSs);
			settlement.setPenalOstd(penalOstd);
			settlement.setFcCharges(fcCharges);
			settlement.setSettlementAmount(settlementAmnt);
			settlement.setPartyCorrAdd(partyCorrAdd);
			settlement.setSettlmentTerm(settlmentTerm);
			settlement.setPartyId(partyId);
			settlement.setNoofstroke(noofstroke);
			settlement.setDetailsPDC(detailsPDC);
			settlement.setArea(area);
			settlement.setAccountingLoss(accountingLoss);
			settlement.setFinacialLoss(finacialLoss);
			settlement.setAccLossPOSVal(accLossPOSVal);
			settlement.setFinLossFRAmnt(finLossFRAmnt);
			settlement.setRecovery(recovery);
			settlement.setBucket(bucket);
			settlement.setRequId(requId);
			settlement.setLegalStatus(legalStatus);
			settlement.setLoanRecallNoticeSentDate(loanRecallNoticeSentDate);
			settlement.setSec138InitDate(sec138InitDate);
			settlement.setArbitrationInitDate(arbitrationInitDate);
			settlement.setPayCollDate(payCollDate);
			settlement.setReasonForSettlement(reasonForSettlement);
			settlement.setOtherInfo(otherInfo);
			settlement.setFirstostddate(firstostddate);
			settlement.setPenalReceiv(penalReceiv);
			settlement.setTotalOutstand(totalOutstand);
			settlement.setLandlineNumber(landlineNumber);
			settlement.setBrokenrevenue(brokenrevenue);
			settlement.setLegalcharges(legalcharges);
			settlement.setPartyId(partyId);
			settlement.setEmipaid(emipaid);
			settlement.setClosurebal(closurebal);
			settlement.setOverdue(overdue);
			settlement.setTotalFR(totalFR);
			settlement.setDueDate(dueDate);
			settlement.setCreatedOn(Utilities.sysDate());
			settlement.setModifiedOn(Utilities.sysDate());
			settlement.setCreatedBy(String.valueOf(systemUserNew.getUserTableId()));
			settlement.setModifiedBy(String.valueOf(systemUserNew.getUserTableId()));
			settlement.setCibilScore(cibilScore);
			settlement.setDPD(dPD);
			settlement.setPromoCode(promoCode);
			settlement.setCaseBrief(caseBrief);
			settlement.setCa(caseId);
			settlement.setRemark(remarks);
			double settlementAmount = Double
					.parseDouble(settlementAmnt.equalsIgnoreCase("") || settlementAmnt == null ? "0" : settlementAmnt);
			settlement.setPtpAmount(ptpAmnt); 
			

			log.info("settlementAmnt :- " + settlementAmount);
			log.info("OPLUSS print here :- " + oPLUSs);
			double pos = Double.parseDouble(oPLUSs.equalsIgnoreCase("") || oPLUSs == null ? "0" : oPLUSs);
			log.info("pos value for calculation :-" + pos);
			double penalAmount = Double.parseDouble(penalOstd.equalsIgnoreCase("") || penalOstd == null ? "0"
					: penalOstd);
			log.info("Penal Outstandingat start :- " + penalAmount);
			double principleCalculation = 0;
			double penalCalculation = 0;

			if (settlementAmount >= pos)
			{
				log.info("inside first if");
				principleCalculation = settlementAmount - pos;
				penalCalculation = penalAmount - principleCalculation;
				settlement.setPrincipleCalc("0");
				//settlement.setPenalCalc(Double.toString(penalCalculation));
				settlement.setPenalCalc((BigDecimal.valueOf(penalCalculation)).toString());
				log.info("principleCalculation :- " + settlement.getPrincipleCalc());
				log.info("penalCalculation :- " + settlement.getPenalCalc());
			}
			else if (settlementAmount < pos)
			{
				log.info("inside second if");
				principleCalculation = pos - settlementAmount;
				penalCalculation = penalAmount;
				/*settlement.setPrincipleCalc(Double.toString(principleCalculation));
				settlement.setPenalCalc(Double.toString(penalCalculation));*/
				settlement.setPrincipleCalc((BigDecimal.valueOf(principleCalculation)).toString());
				settlement.setPenalCalc((BigDecimal.valueOf(penalCalculation)).toString());
				log.info("principleCalculation :- " + settlement.getPrincipleCalc());
				log.info("penalCalculation :- " + settlement.getPenalCalc());
			}
			else
			{
				settlement.setPrincipleCalc("0");
				settlement.setPenalCalc("0");
			}
			Map<String, Object> wrapperMap = new HashMap<String, Object>();

			long reqUID = createSearchRequestMap(data, appl, wrapperMap);

			Map<String, Object> webserviceResponseMap = new HashMap<String, Object>();
/*
			webserviceResponseMap = webserviceAdapter.callWebserviceAndGetMap(wrapperMap,
					applicationConfiguration.getValue("WEB_SERVICE_URL_MCORE"), userActivity,
					communicationActivityService);
*/
			/*reqUID = 700758;

			webserviceResponseMap = XMLToMap
				.convertXMLToMap("<mc005><response_header><sourceappcode>MCOLL</sourceappcode><RqUID>700758</RqUID><message_type_cd>MC005</message_type_cd><message_datetime>2016-04-27 16:49:45</message_datetime></response_header><response_details><partyid>22848</partyid><partyname>Dayalsingh Kundansingh</partyname><appl>CSG</appl><apacnum>400011327</apacnum><EMIduedate>05-MAY-2016</EMIduedate><Penalamount>0</Penalamount><Overdueamount>126000</Overdueamount><TotaloutstandingAmount>126000</TotaloutstandingAmount><EMIdueAmount>18000</EMIdueAmount><mobilenum></mobilenum><email></email><phonenum>774852</phonenum><address>21, GURUDWARA MARKET , CHHANI ROAD, CHHANI BARODA, VADODARA -391740, Gujarat - India</address><regno></regno><byrpartyid></byrpartyid><byrpartyname></byrpartyname><Apacdate>09-SEP-2015</Apacdate><Tenure>36</Tenure><AGRVALUE>500000</AGRVALUE><EMIPAID>0</EMIPAID><BALTENURE>36</BALTENURE><PrincipleOutstanding>421577.08</PrincipleOutstanding><FCCHARGES>19307.57</FCCHARGES><BROKENREVENUE>4594.31</BROKENREVENUE><LEGALCAGRGES>0</LEGALCAGRGES><BUCKET>7</BUCKET><FIRSTOSTDDATE>22-apr-2016</FIRSTOSTDDATE><CLOSUREBAL>0</CLOSUREBAL></response_details><response_status><errorcode>0</errorcode><errordesc>SUCCESS</errordesc></response_status></mc005>");*/
				
			
			/*log.info("-----webserviceResponseMap-----" + webserviceResponseMap.toString());

			log.info("before server device check validityCount ::" + validityCount);

			Map<String, Object> newMap1 = webserviceResponseMap.get("mc005") == null ? (Map<String, Object>) webserviceResponseMap
					.get("mc002") : (Map<String, Object>) webserviceResponseMap.get("mc005");

			Map<String, Object> newMap = (Map<String, Object>) newMap1.get("response_details");

			String Penalamount = newMap.get("Penalamount").toString();
			if (!Penalamount.equalsIgnoreCase(settlement.getPenalOstd()))
			{
				validityCount = validityCount + 1;
				log.info("Penalamount Server = " + Penalamount);
				log.info("Penalamount Device = " + settlement.getPenalOstd());
				log.info("Penalamount validityCount ====== " + validityCount);
			}

			String Overdueamount = newMap.get("Overdueamount").toString();
			if (!Overdueamount.equalsIgnoreCase(settlement.getOverdue()))
			{
				validityCount = validityCount + 1;
				log.info("Overdueamount Server = " + Overdueamount);
				log.info("Overdueamount Device = " + settlement.getOverdue());
				log.info("Overdueamount validityCount ====== " + validityCount);
			}

			String TotaloutstandingAmount = newMap.get("TotaloutstandingAmount").toString();
			
			if (!TotaloutstandingAmount.equalsIgnoreCase(settlement.getTotalOutstand()))
			{
				validityCount = validityCount + 1;
				log.info("TotaloutstandingAmount Server = " + TotaloutstandingAmount);
				log.info("TotaloutstandingAmount Device = " + TotaloutstandingAmount);
				log.info("TotaloutstandingAmount validityCount====== " + validityCount);
			}

			String EMIdueAmount = newMap.get("EMIdueAmount").toString();
			if (!EMIdueAmount.equalsIgnoreCase(settlement.getEmiAmount()))
			{
				validityCount = validityCount + 1;
				log.info("EMIdueAmount Server = " + EMIdueAmount);
				log.info("EMIdueAmount Device = " + settlement.getEmiAmount());
				log.info("EMIdueAmount validityCount ====== " + validityCount);
			}

			String Tenure = newMap.get("Tenure").toString();
			if (!Tenure.equalsIgnoreCase(settlement.getTenure()))
			{
				validityCount = validityCount + 1;
				log.info("Tenure Server = " + Tenure);
				log.info("Tenure Device = " + settlement.getTenure());
				log.info("Tenure validityCount ====== " + validityCount);
			}

			String AGRVALUE = newMap.get("AGRVALUE").toString();
			if (!AGRVALUE.equalsIgnoreCase(settlement.getLoanAmnt()))
			{
				validityCount = validityCount + 1;
				log.info(" Server = " + AGRVALUE);
				log.info(" Device = " + settlement.getLoanAmnt());
				log.info("AGRVALUE validityCount ====== " + validityCount);
			}

			String EMIPAID = newMap.get("EMIPAID").toString();
			if (!EMIPAID.equalsIgnoreCase(settlement.getEmipaid()))
			{
				validityCount = validityCount + 1;
				log.info(" Server = " + EMIPAID);
				log.info(" Device = " + EMIPAID);
				log.info("EMIPAID validityCount ====== " + validityCount);
			}

			String BALTENURE = newMap.get("BALTENURE").toString();
			if (!BALTENURE.equalsIgnoreCase(settlement.getBaltenure()))
			{
				validityCount = validityCount + 1;
				log.info(" Server = " + BALTENURE);
				log.info(" Device = " + BALTENURE);
				log.info("BALTENURE validityCount ====== " + validityCount);
			}*/
			//images = getImages(systemUserNew, imageDetails, settlement);
			//settlement.setImages(images);

			/*if (images == null)
			{

				status = JsonConstants.FAILURE;
				returnMessage = "Image Path Not Found";
				return responseBuilder(message, status, returnMessage, "");
			}
			*/
			/*
			 * } catch (Exception e) {
			 * 
			 * log.error("---exception is ", e); }
			 */

			/*
			 * status = JsonConstants.FAILURE; returnMessage =
			 * JsonConstants.CASH_DETAILS_ABSENT; return
			 * responseBuilder(message, status, returnMessage, "");
			 */

			log.info("----setttlement------" + settlement);

			if (settlementService.checkDuplicateSettlementJSON(settlement))
			{

				long returnValue = settlementService.submitSettlement(settlement,systemUserNew);
				log.info("file URL :: " + fileUrl);
				if (returnValue != 0L)
				{
		            String fileName = settlement.getApacNo() + "_" + System.currentTimeMillis()+".jpg";
		            log.info("fileName:: " + fileName);
		            
					log.info("data.optString(\"mposCode\") before" + data.optString("mposCode"));
					String token = data.optString("mposCode") == null ? Constants.EMPTY_STRING : AES.decrypt(data.optString("mposCode"));
					log.info("token decrypted:: " + token);
					
					JSONArray imageArray = (JSONArray)data.get("images");
					String imageString = getImagesByte(imageArray);
					
					File outputFile = Files.createTempFile(null,null).toFile();
			        
			        byte[] decodedBytes = Base64.getDecoder().decode(imageString);
					 FileUtils.writeByteArrayToFile(outputFile, decodedBytes);

			
						/*
						 * SubmitSettlementImageThread submitThread = new
						 * SubmitSettlementImageThread(settlement , systemUserNew , returnValue ,
						 * settlementService , fileName , token , imageString,fileUrl);
						 * 
						 * Thread threadImage = new Thread(submitThread);
						 * threadImage.setName("SubmitSettlementImageThread"); threadImage.start();
						 */
					
					getDefaultPathFile(fileName, token, imageString,settlement,systemUserNew,fileUrl,returnValue);
					 
					log.info("SubmitSettlementImageThread Started:: ");
					submissionFlag = true;
				}
				else
				{
					submissionFlag = false;
				}
				 

				log.info("-----submissionFlag-----" + submissionFlag);

				if (submissionFlag)
				{

					try
					{
						
						  //fz //sendSms(settlement, systemUserNew); JSONObject getData = (JSONObject)
							/*
							 * jsonObject.get(JsonConstants.DATA); JSONArray imageArray = (JSONArray)
							 * data.get("images"); String imageString = getImagesByte(imageArray); String
							 * fileName = settlement.getApacNo() + Constants.SYMBOL_UNDERSCORE +
							 * System.currentTimeMillis(); MultipartFile filePart =
							 * convertToMultipartFile(imageString.getBytes(), fileName);
							 * 
							 * InputStream is = filePart.getInputStream(); FileStorage fileStorage = null;
							 * 
							 * FileRef fileRef = uploadToFileStorage(fileStorage, is, filePart.getName());
							 * String fileData = fileRef.toString(); log.info("fileData :::; " + fileData);
							 */
					}

					catch (Exception e)
					{
						log.error("------Exception while submission of settlement SMS ----" + e);

					}

					log.info("Settlement submitted without violation");

					status = JsonConstants.SUCCESS;

					/*
					 * UserActivityStatusUpdate userActivityStatusUpdate = new
					 * UserActivityStatusUpdate(userActivity,
					 * (ActivityLoggerConstants.STATUS_SUCCESS), userActivityService); new
					 * Thread(userActivityStatusUpdate).run();
					 */

					return responseBuilder(message, status, "Settlement got submitted successfully",
							settlement.getRequId());

				}
				else
				{
					System.out.println("Settlement submitted with violation");
					log.info("Settlement submitted with violation");

					status = JsonConstants.FAILURE;

					/*
					 * UserActivityStatusUpdate userActivityStatusUpdate = new
					 * UserActivityStatusUpdate(userActivity,
					 * (ActivityLoggerConstants.STATUS_FAILURE), userActivityService); new
					 * Thread(userActivityStatusUpdate).run();
					 */

					return responseBuilder(message, status, "Some error has occured", "");

				}// status = JsonConstants.SUCCESS;

			}

			else
			{
				log.info("--------- Settlement Record already exists, JSON Duplicated! ------------");

				status = JsonConstants.SUCCESS;

				returnMessage = "JSON DUPLICATED!!!";

				/*
				 * UserActivityStatusUpdate userActivityStatusUpdate = new
				 * UserActivityStatusUpdate(userActivity,
				 * (ActivityLoggerConstants.STATUS_IGNORE), userActivityService); new
				 * Thread(userActivityStatusUpdate).run();
				 */

				return responseBuilder(message, status, returnMessage, settlement.getRequId());
			}
		}

		catch (Exception e)
		{
			log.error("----Exception details---", e);
			e.printStackTrace();

			returnMessage = "Failure";
			return responseBuilder(message, status, returnMessage, "");
		}
	}

	private void sendSms(Settlement settlement, SystemUser systemUserNew)
	{

		log.info("Sending sms to Collector mobile number");

		callSMSDispatcher(settlement, systemUserNew.getMobileNumber(), systemUserNew);

	}

	private void callSMSDispatcher(Settlement settlement, String mobileNumber, SystemUser systemUserNew)
	{
		log.info("---- Inside callSMSDispatcher --------");
		try
		{
			String webserviceUrl = (String) applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");

			Map<String, Object> smsDispatcherMap = ServerUtilities.generateSMSDispatcherMapForSettlement(settlement,
					mobileNumber, "raised");

			StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true, new HashMap<String, String>());

			log.info("----- xmlRequest : -------" + xmlRequest);

			CommunicationActivityAddition communicationActivityAddition = new CommunicationActivityAddition(
					systemUserNew.getUserTableId().toString(), systemUserNew.getImeiNo(),
					(settlement.getAppl() + "_" + "Settelment"), webserviceUrl, xmlRequest.toString(),
					communicationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);

			new Thread(communicationActivityAddition).run();

			KotakCollectionWebserviceAdapter kotakCollectionWebserviceAdapter = new KotakCollectionWebserviceAdapter();

			String xmlResponse ="";

			CommunicationActivity communicationActivity = communicationActivityAddition.extractCommunicationActivity();

			Map<String, Object> result = null;

			if (null != xmlResponse && !xmlResponse.equals(""))
			{
				communicationActivity.setResponse(xmlResponse);

				CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
						communicationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), communicationActivityService);

				new Thread(communicationActivityStatusUpdate).run();

				result = XMLToMap.convertXMLToMap(xmlResponse);

				log.info("----- Result of SMS Dispatch : -------" + result);
			}
			else
			{
				communicationActivity.setResponse(ActivityLoggerConstants.EMPTY_STRING);

				CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
						communicationActivity, (ActivityLoggerConstants.STATUS_FAILURE), communicationActivityService);

				new Thread(communicationActivityStatusUpdate).run();

				log.info("----- Failure in sending SMS : -------");
			}
		}
		catch (ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void extractCashDetails(String pan, JSONObject data, Collection collection) throws JSONException
	{
		JSONObject cashDetail = new JSONObject();

		cashDetail = (JSONObject) data.get(JsonConstants.RequestData.CASH);

		collection.setDocType(cashDetail.get(JsonConstants.DOCUMENT_TYPE).toString());
		collection.setDocRef(cashDetail.get(JsonConstants.DOCUMENT_REFERENCE).toString());

		if (collection.getDocType() != null && collection.getDocType().equalsIgnoreCase("PAN"))
		{
			collection.setPan(cashDetail.get(JsonConstants.DOCUMENT_REFERENCE).toString());
		}

		JSONArray denominationArray = cashDetail.getJSONArray(JsonConstants.DENOMINATION);

		List<Denomination> denominationList = new ArrayList<Denomination>();

		for (int i = 0; i < (denominationArray.length()); i++)
		{
			JSONObject cashJSON = denominationArray.getJSONObject(i);

			Denomination denomination = new Denomination();

			denomination.setNote(cashJSON.get(JsonConstants.DENOMINATION_NOTE) == null
					|| cashJSON.get(JsonConstants.DENOMINATION_NOTE).toString()
							.equalsIgnoreCase(Constants.EMPTY_STRING) ? "0" : cashJSON.get(
					JsonConstants.DENOMINATION_NOTE).toString());
			denomination.setNoteCount(Integer.parseInt(cashJSON.get(JsonConstants.DENOMINATION_COUNT) == null
					|| cashJSON.get(JsonConstants.DENOMINATION_COUNT).toString()
							.equalsIgnoreCase(Constants.EMPTY_STRING) ? "0" : cashJSON.get(
					JsonConstants.DENOMINATION_COUNT).toString()));

			denominationList.add(denomination);
		}
		collection.setDenomination(denominationList);

		if (cashDetail.has(JsonConstants.RequestData.PAN))
		{
			pan = (String) cashDetail.get(JsonConstants.RequestData.PAN);
		}

		if (cashDetail.has(JsonConstants.INSTRUMENT_DATE))
		{
			collection.setInstDate((String) cashDetail.get(JsonConstants.INSTRUMENT_DATE));
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

	private List<Image> getImages(SystemUser systemUser, JSONArray imageDetails, Settlement settlement)
			throws JSONException
	{

		JSONObject imageDetail = new JSONObject();
		Image image = null;
		String imagePath = null;

		List<Image> images = new ArrayList<Image>();

		for (int index = 0; index < imageDetails.length(); index++)
		{

			imageDetail = (JSONObject) imageDetails.get(index);

			if (!imageDetail.has(JsonConstants.RequestData.IMAGE))
			{

				return new ArrayList<Image>();
			}

			String imageByteArray = (String) imageDetail.get(JsonConstants.RequestData.IMAGE);
			if (imageByteArray.isEmpty())
			{

				return new ArrayList<Image>();
			}
			image = new Image();

			imagePath = (extractImagePath(settlement, imageByteArray, Constants.IMAGE_FILE_PATH,
					(String.valueOf(index))));

			if (imagePath.equals(JsonConstants.ERROR))
			{

				return null;
			}
			else
			{

				image = new Image();
				image.setPath(imagePath);
				Utilities.primaryBeanSetter(image, systemUser);
				images.add(image);

			}
		}
		return images;
	}

	private String extractImagePath(Settlement settlement, String type, String entity, String index)
	{
		try
		{

			String fileName = settlement.getRequId() + Constants.SYMBOL_UNDERSCORE + settlement.getApacNo()
					+ Constants.SYMBOL_UNDERSCORE + System.currentTimeMillis();

			String filePath = Constants.EMPTY_STRING;

			if (index.equals(Constants.EMPTY_STRING))
			{

				filePath = Utilities.generateFilePath((String) applicationConfiguration.getValue(entity), fileName);
			}
			else
			{

				filePath = Utilities.generateFilePath((String) applicationConfiguration.getValue(entity), (fileName
						+ "_" + index));
			}

			if (Utilities.writeImage(filePath, type))
			{

				return filePath;
			}
			else
			{
				return (JsonConstants.ERROR);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return (JsonConstants.ERROR);
		}
	}

	private void sendCollectionsSms(Collection collection, SystemUser user)
	{

		log.info("------- IN Integration , Before Sending SMS  --------");
		try
		{
			if (collection.getMobileNumber() != null
					&& !collection.getMobileNumber().equalsIgnoreCase(Constants.EMPTY_STRING))
			{
				log.info("Sending sms to customer mobile number thorugh Integration");

				callSMSDispatcher(collection.getAppropriateAmount() + "", collection.getReceiptNumber(),
						collection.getPaymentMode(), collection.getMobileNumber(), collection.getAppl(),
						collection.getBusinessPartnerNumber(), user, communicationActivityService, collection);
			}

			if (collection.getMobileNumberNew() != null
					&& !collection.getMobileNumberNew().equalsIgnoreCase(Constants.EMPTY_STRING)
					&& !collection.getMobileNumberNew().equalsIgnoreCase(collection.getMobileNumber()))
			{
				log.info("Sending sms to customer alternate mobile number through Integration");

				callSMSDispatcher(collection.getAppropriateAmount() + "", collection.getReceiptNumber(),
						collection.getPaymentMode(), collection.getMobileNumberNew(), collection.getAppl(),
						collection.getBusinessPartnerNumber(), collection.getUser(), communicationActivityService,
						collection);
			}

			if (collection.getUser().getMobileNumber() != null
					&& !collection.getUser().getMobileNumber().equalsIgnoreCase(Constants.EMPTY_STRING))
			{
				log.info("Sending sms to FE mobile number through Integration");

				generateSMSDispatcherMapForFE(collection.getAppropriateAmount() + "", collection.getReceiptNumber(),
						collection.getPaymentMode(), collection.getUser().getMobileNumber(), collection.getAppl(),
						collection.getPartyName(), collection.getUser(), communicationActivityService, collection);

			}

			/*
			 * if (collection.getUser().getSupervisorMobileNumber() != null &&
			 * !collection.getUser().getSupervisorMobileNumber()
			 * .equalsIgnoreCase(Constants.EMPTY_STRING)) { log.info(
			 * "Sending sms to Supervisor mobile number through Integration");
			 * 
			 * generateSMSDispatcherMapForFE(collection.getAppropriateAmount() +
			 * "", collection.getReceiptNumber(), collection.getPaymentMode(),
			 * collection.getUser() .getSupervisorMobileNumber(),
			 * collection.getAppl(), collection.getPartyName(),
			 * collection.getUser(), communicationActivityService, collection);
			 * 
			 * }
			 */
		}
		catch (Exception e)
		{
			log.info("There is some error occured while sending sms to customer.In Integration" + e);
		}

	}

	private void callSMSDispatcher(String amount, String receiptNumber, String paymentType, String mobileNumber,
			String type, String apacCardNumber, SystemUser user,
			CommunicationActivityService communicationActivityService, Collection collection)
	{
		log.info("---- Inside callSMSDispatcher --------");

		String webserviceUrl = (String) applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");

		Map<String, Object> smsDispatcherMap = ServerUtilities.generateSMSDispatcherMap(amount, receiptNumber,
				paymentType, mobileNumber, type, apacCardNumber);

		StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true, new HashMap<String, String>());

		log.info("----- xmlRequest : -------" + xmlRequest);

		CommunicationActivityAddition communicationActivityAddition = new CommunicationActivityAddition(user
				.getUserTableId().toString(), user.getImeiNo(), (type + "_" + collection.getCollectionType()),
				webserviceUrl, xmlRequest.toString(), communicationActivityService,
				ActivityLoggerConstants.DATABASE_MSSQL);

		new Thread(communicationActivityAddition).run();

		KotakCollectionWebserviceAdapter kotakCollectionWebserviceAdapter = new KotakCollectionWebserviceAdapter();

		String xmlResponse = kotakCollectionWebserviceAdapter.callWebserviceAndGetXmlString(xmlRequest.toString(),
				webserviceUrl);

		CommunicationActivity communicationActivity = communicationActivityAddition.extractCommunicationActivity();

		Map<String, Object> result = null;

		if (null != xmlResponse && !xmlResponse.equals(""))
		{
			communicationActivity.setResponse(xmlResponse);

			CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
					communicationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), communicationActivityService);

			new Thread(communicationActivityStatusUpdate).run();

			result = XMLToMap.convertXMLToMap(xmlResponse);

			log.info("----- Result of SMS Dispatch : -------" + result);
		}
		else
		{
			communicationActivity.setResponse(ActivityLoggerConstants.EMPTY_STRING);

			CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
					communicationActivity, (ActivityLoggerConstants.STATUS_FAILURE), communicationActivityService);

			new Thread(communicationActivityStatusUpdate).run();

			log.info("----- Failure in sending SMS : -------");
		}
	}

	private void generateSMSDispatcherMapForFE(String amount, String receiptNumber, String paymentType,
			String mobileNumber, String type, String feName, SystemUser user,
			CommunicationActivityService communicationActivityService, Collection collection)
	{
		log.info("---- Inside generateSMSDispatcherMapForFE --------");

		String webserviceUrl = (String) applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");

		Map<String, Object> smsDispatcherMap = ServerUtilities.generateSMSDispatcherMapForFE(amount, receiptNumber,
				paymentType, mobileNumber, type, feName);

		StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true, new HashMap<String, String>());

		log.info("----- xmlRequest : -------" + xmlRequest);

		CommunicationActivityAddition communicationActivityAddition = new CommunicationActivityAddition(user
				.getUserTableId().toString(), user.getImeiNo(), (type + "_" + collection.getCollectionType()),
				webserviceUrl, xmlRequest.toString(), communicationActivityService,
				ActivityLoggerConstants.DATABASE_MSSQL);

		new Thread(communicationActivityAddition).run();

		KotakCollectionWebserviceAdapter kotakCollectionWebserviceAdapter = new KotakCollectionWebserviceAdapter();

		String xmlResponse = kotakCollectionWebserviceAdapter.callWebserviceAndGetXmlString(xmlRequest.toString(),
				webserviceUrl);

		CommunicationActivity communicationActivity = communicationActivityAddition.extractCommunicationActivity();

		Map<String, Object> result = null;

		if (null != xmlResponse && !xmlResponse.equals(""))
		{
			communicationActivity.setResponse(xmlResponse);

			CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
					communicationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), communicationActivityService);

			new Thread(communicationActivityStatusUpdate).run();

			result = XMLToMap.convertXMLToMap(xmlResponse);

			log.info("----- Result of SMS Dispatch : -------" + result);
		}
		else
		{
			communicationActivity.setResponse(ActivityLoggerConstants.EMPTY_STRING);

			CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
					communicationActivity, (ActivityLoggerConstants.STATUS_FAILURE), communicationActivityService);

			new Thread(communicationActivityStatusUpdate).run();

			log.info("----- Failure in sending SMS : -------");
		}
	}

	private void callEmailService(Collection collection)
	{
		try
		{
			if (collection.getEmailAddress().equals(Constants.EMPTY_STRING)
					&& collection.getEmailAddressNew().equals(Constants.EMPTY_STRING))
			{
				log.info(" -------- No Email Address found for Collection -------- ");
			}
			else
			{
				log.info("--- Sending Email ---");

				String payMode = collection.getPaymentMode();
				log.info(" -------- payMode -------- " + payMode);

				if (payMode.equals(Constants.PAYMENT_MODE_CASH))
				{
					sendEmailForCashPayment(collection);
				}

				if (payMode.equals(Constants.PAYMENT_MODE_CHEQUE))
				{
					sendEmailForChequePayment(collection);
				}

				if (payMode.equalsIgnoreCase(Constants.PAYMENT_MODE_PDC)
						|| payMode.equalsIgnoreCase(Constants.PAYMENT_MODE_DRAFT))
				{
					sendEmailForDDPDC(collection);
				}
			}
		}
		catch (Exception e)
		{
			log.info("-------Error Occured in sending Email---------", e);
			e.printStackTrace();
		}
	}

	private void sendEmailForCashPayment(Collection collection) throws ParseException
	{
		log.info("---inside sendEmailForCashPayment---");

		String paymentDate = collection.getDeviceDate();
		log.info("---payment date---" + paymentDate);

		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat format2 = new SimpleDateFormat("dd-MMM-yyyy");
		Date date = format1.parse(paymentDate);
		paymentDate = format2.format(date);

		log.info("---payment date after parsing---" + paymentDate);

		// String email = collection.getEmailAddress();

		String emailText = "";

		if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD))
		{
			log.info("---inside if condition---");
			log.info("----email text----" + simpleMailMessageForCashPaymentCreditCard.getText());
			emailText = String.format(simpleMailMessageForCashPaymentCreditCard.getText(), collection.getName(),
					getFullFormApplType(collection.getAppl()), collection.getBusinessPartnerNumber(),
					collection.getReceiptNumber(), collection.getAppropriateAmount() + "", "" + paymentDate,
					collection.getBusinessPartnerNumber(), getTollFreeNumberForAppl(collection.getAppl()));
			log.info("----emailTest for card ----" + emailText);

		}
		else
		{
			log.info("--- inside else----");
			log.info("--- simpleMailMessageForCashPaymentLoan.getText() ----"
					+ simpleMailMessageForCashPaymentLoan.getText());
			log.info("---- collection.getName()-----" + collection.getName());
			log.info("---- getTollFreeNumberForAppl-----" + getTollFreeNumberForAppl(collection.getAppl()));

			emailText = String.format(simpleMailMessageForCashPaymentLoan.getText(), collection.getName(),
					getFullFormApplType(collection.getAppl()), collection.getBusinessPartnerNumber(),
					collection.getReceiptNumber(), collection.getAppropriateAmount() + "", "" + paymentDate,
					collection.getBusinessPartnerNumber(), getTollFreeNumberForAppl(collection.getAppl()));
			log.info("---- email text is -----" + emailText);

		}

		String email = collection.getEmailAddress() != null ? collection.getEmailAddress() : "";

		log.info("---email----" + email);

		if (collection.getEmailAddress() != null
				&& !collection.getEmailAddress().equalsIgnoreCase(Constants.EMPTY_STRING))
		{
			email = collection.getEmailAddress();
			log.info("---inside if email----" + email);

			if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD))
			{
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForCashPaymentCreditCard.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection
						.getUser().getUserTableId().toString(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL,
						senderList, receiverList, simpleMailMessageForCashPaymentCreditCard.getSubject(), emailText,
						notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);
				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(email, simpleMailMessageForCashPaymentCreditCard.getFrom(),
						simpleMailMessageForCashPaymentCreditCard.getSubject(), emailText))
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
				else
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			}
			else
			{
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForCashPaymentLoan.getFrom());
				log.info("simpleMailMessageForCashPaymentLoan.getFrom()"
						+ simpleMailMessageForCashPaymentLoan.getFrom());

				log.info("adding string data into reciverList" + email);
				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				log.info("collection.getUser().getUserTableId()" + collection.getUser());
				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection
						.getUser().getUserTableId().toString(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL,
						senderList, receiverList, simpleMailMessageForCashPaymentLoan.getSubject(), emailText,
						notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);

				log.info("notificationActivityAddition" + notificationActivityAddition);
				new Thread(notificationActivityAddition).run();

				if (emailService == null)
				{
					log.info("emailService is null");
				}
				else
				{
					log.info("emailService is not null");
					log.info("email is : " + email);
				}
				if (emailService.sendMail(email, simpleMailMessageForCashPaymentLoan.getFrom(),
						simpleMailMessageForCashPaymentLoan.getSubject(), emailText))
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
				else
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			}
		}

		if (collection.getEmailAddressNew() != null
				&& !collection.getEmailAddressNew().equalsIgnoreCase(Constants.EMPTY_STRING)
				&& !collection.getEmailAddressNew().equalsIgnoreCase(email))
		{
			email = collection.getEmailAddressNew();
			if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD))
			{
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForCashPaymentCreditCard.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection
						.getUser().getUserTableId().toString(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL,
						senderList, receiverList, simpleMailMessageForCashPaymentCreditCard.getSubject(), emailText,
						notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);
				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(collection.getEmailAddressNew(),
						simpleMailMessageForCashPaymentCreditCard.getFrom(),
						simpleMailMessageForCashPaymentCreditCard.getSubject(), emailText))
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
				else
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			}
			else
			{
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForCashPaymentLoan.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection
						.getUser().getUserTableId().toString(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL,
						senderList, receiverList, simpleMailMessageForCashPaymentLoan.getSubject(), emailText,
						notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);
				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(collection.getEmailAddressNew(),
						simpleMailMessageForCashPaymentLoan.getFrom(),
						simpleMailMessageForCashPaymentLoan.getSubject(), emailText))
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
				else
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			}
		}
	}

	private void sendEmailForChequePayment(Collection collection) throws ParseException
	{
		String paymentDate = collection.getDeviceDate();
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat format2 = new SimpleDateFormat("dd-MMM-yyyy");
		Date date = format1.parse(paymentDate);
		paymentDate = format2.format(date);

		String email = collection.getEmailAddress() != null ? collection.getEmailAddress() : "";
		;
		String chequeDetailString = "";

		NumberFormat formatter = NumberFormat.getInstance(new Locale("en", "IN"));

		for (Cheque cheque : collection.getChequeDetails())
		{
			chequeDetailString = chequeDetailString + " Cheque No." + cheque.getChequeNo() + "     dated"
					+ cheque.getChequeDate();
		}

		String emailText = "";
		if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD))
		{
			emailText = String.format(simpleMailMessageForChequePaymentCreditCard.getText(), collection.getName(),
					getFullFormApplType(collection.getAppl()), collection.getBusinessPartnerNumber(),
					collection.getReceiptNumber(), collection.getAppropriateAmount() + "", "" + paymentDate, ""
							+ chequeDetailString, collection.getBusinessPartnerNumber(),
					getTollFreeNumberForAppl(collection.getAppl()));
		}
		else
		{
			emailText = String.format(simpleMailMessageForChequePaymentLoan.getText(), collection.getName(),
					getFullFormApplType(collection.getAppl()), collection.getBusinessPartnerNumber(),
					collection.getReceiptNumber(), collection.getAppropriateAmount() + "", "" + paymentDate, ""
							+ chequeDetailString, collection.getBusinessPartnerNumber(),
					getTollFreeNumberForAppl(collection.getAppl()));
		}

		if (collection.getEmailAddress() != null
				&& !collection.getEmailAddress().equalsIgnoreCase(Constants.EMPTY_STRING))
		{
			if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD))
			{
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForChequePaymentCreditCard.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection
						.getUser().getUserTableId().toString(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL,
						senderList, receiverList, simpleMailMessageForChequePaymentCreditCard.getSubject(), emailText,
						notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);
				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(email, simpleMailMessageForChequePaymentCreditCard.getFrom(),
						simpleMailMessageForChequePaymentCreditCard.getSubject(), emailText))
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
				else
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			}
			else
			{
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForChequePaymentLoan.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection
						.getUser().getUserTableId().toString(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL,
						senderList, receiverList, simpleMailMessageForChequePaymentLoan.getSubject(), emailText,
						notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);
				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(email, simpleMailMessageForChequePaymentLoan.getFrom(),
						simpleMailMessageForChequePaymentLoan.getSubject(), emailText))
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
				else
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			}
		}

		if (collection.getEmailAddressNew() != null
				&& !collection.getEmailAddressNew().equalsIgnoreCase(Constants.EMPTY_STRING)
				&& !collection.getEmailAddressNew().equalsIgnoreCase(email))
		{
			if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD))
			{
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForChequePaymentCreditCard.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection
						.getUser().getUserTableId().toString(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL,
						senderList, receiverList, simpleMailMessageForChequePaymentCreditCard.getSubject(), emailText,
						notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);
				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(collection.getEmailAddressNew(),
						simpleMailMessageForChequePaymentCreditCard.getFrom(),
						simpleMailMessageForChequePaymentCreditCard.getSubject(), emailText))
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
				else
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			}
			else
			{
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForChequePaymentLoan.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection
						.getUser().getUserTableId().toString(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL,
						senderList, receiverList, simpleMailMessageForChequePaymentLoan.getSubject(), emailText,
						notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);
				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(collection.getEmailAddressNew(),
						simpleMailMessageForChequePaymentLoan.getFrom(),
						simpleMailMessageForChequePaymentLoan.getSubject(), emailText))
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
				else
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			}
		}
	}

	private void sendEmailForDDPDC(Collection collection) throws ParseException
	{
		String paymentDate = collection.getDeviceDate();
		String collectionDate = collection.getDeviceDate();

		Date emailDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat emailDateFormat = new SimpleDateFormat("ddMMMyyyy");
		emailDate = (dateFormat.parse(collectionDate));

		String email = collection.getEmailAddress().equals(Constants.EMPTY_STRING) ? collection.getEmailAddress()
				: collection.getEmailAddress();

		String chequeDetailString = "";

		NumberFormat formatter = NumberFormat.getInstance(new Locale("en", "IN"));

		String emailAmount = collection.getAppropriateAmount() + "";

		DecimalFormat amountFormat = new DecimalFormat("#.00");

		try
		{
			emailAmount = amountFormat.format(Double.parseDouble(emailAmount));
		}
		catch (Exception e)
		{
			emailAmount = "0.00";
		}

		for (Cheque cheque : collection.getChequeDetails())
		{
			if (collection.getPaymentMode().equalsIgnoreCase(Constants.PAYMENT_MODE_DRAFT))
			{
				chequeDetailString = chequeDetailString
						+ "<br/>Demand Draft No."
						+ cheque.getChequeNo()
						+ "     Dated "
						+ new SimpleDateFormat("ddMMMyyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(cheque
								.getChequeDate()));
			}
			else
			{
				chequeDetailString = chequeDetailString
						+ "<br/> PDC No."
						+ cheque.getChequeNo()
						+ "     Dated "
						+ new SimpleDateFormat("ddMMMyyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(cheque
								.getChequeDate()));
			}

		}

		String emailText = "";

		if (!collection.getAppl().isEmpty() && collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD))
		{
			if (collection.getPaymentMode().equalsIgnoreCase(Constants.PAYMENT_MODE_DRAFT))
			{
				emailText = String.format(simpleMailMessageForDDPDC.getText(), "Credit Card",
						"-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber(),
						collection.getReceiptNumber(), "Demand Draft", emailAmount, emailDateFormat.format(emailDate),
						chequeDetailString, "-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber());
			}
			else
			{
				emailText = String.format(simpleMailMessageForDDPDC.getText(), "Credit Card",
						"-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber(),
						collection.getReceiptNumber(), "PDC", emailAmount, emailDateFormat.format(emailDate),
						chequeDetailString, "-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber());
			}
		}
		else
		{
			if (collection.getPaymentMode().equalsIgnoreCase(Constants.PAYMENT_MODE_DRAFT))
			{
				emailText = String.format(simpleMailMessageForDDPDC.getText(), "Personal Finance Loan ", "-"
						+ collection.getAppl() + " " + collection.getBusinessPartnerNumber(),
						collection.getReceiptNumber(), "Demand Draft", emailAmount, emailDateFormat.format(emailDate),
						chequeDetailString, "-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber());
			}
			else
			{
				emailText = String.format(simpleMailMessageForDDPDC.getText(), "Personal Finance Loan ", "-"
						+ collection.getAppl() + " " + collection.getBusinessPartnerNumber(),
						collection.getReceiptNumber(), "PDC", emailAmount, emailDateFormat.format(emailDate),
						chequeDetailString, "-" + collection.getAppl() + " " + collection.getBusinessPartnerNumber());
			}
		}

		if (collection.getEmailAddress() != null
				&& !collection.getEmailAddress().equalsIgnoreCase(Constants.EMPTY_STRING))
		{
			if (!collection.getAppl().isEmpty())
			{

				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForDDPDC.getFrom());

				List<String> receiverList = new ArrayList<String>();
				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection
						.getUser().getUserTableId().toString(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL,
						senderList, receiverList, simpleMailMessageForDDPDC.getSubject(), emailText,
						notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);

				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(email, simpleMailMessageForDDPDC.getFrom(),
						simpleMailMessageForDDPDC.getSubject(), emailText))
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), notificationActivityService);

					new Thread(notificationActivityStatusUpdate).run();
				}
				else
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE), notificationActivityService);

					new Thread(notificationActivityStatusUpdate).run();
				}

			}
			else
			{
				log.info("----- Improper Information to Send Email");
			}
		}

		if (collection.getEmailAddressNew() != null
				&& !collection.getEmailAddressNew().equalsIgnoreCase(Constants.EMPTY_STRING)
				&& !collection.getEmailAddressNew().equalsIgnoreCase(email))
		{
			if (!collection.getAppl().isEmpty())
			{
				List<String> senderList = new ArrayList<String>();
				senderList.add(simpleMailMessageForCashPaymentCreditCard.getFrom());

				List<String> receiverList = new ArrayList<String>();

				receiverList.add(email);

				NotificationActivityAddition notificationActivityAddition = new NotificationActivityAddition(collection
						.getUser().getUserTableId().toString(), ActivityLoggerConstants.TYPE_NOTIFICATION_EMAIL,
						senderList, receiverList, simpleMailMessageForDDPDC.getSubject(), emailText,
						notificationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);
				new Thread(notificationActivityAddition).run();

				if (emailService.sendMail(collection.getEmailAddressNew(), simpleMailMessageForDDPDC.getFrom(),
						simpleMailMessageForDDPDC.getSubject(), emailText))
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
				else
				{
					NotificationActivity notificationActivity = notificationActivityAddition
							.extractNotificationActivity();
					NotificationActivityStatusUpdate notificationActivityStatusUpdate = new NotificationActivityStatusUpdate(
							notificationActivity, (ActivityLoggerConstants.STATUS_FAILURE), notificationActivityService);
					new Thread(notificationActivityStatusUpdate).run();
				}
			}
			else
			{
				log.info("----- Improper Information to Send Email");
			}
		}
	}

	private String getFullFormApplType(String appl)
	{
		if (appl.equalsIgnoreCase(Constants.APPL_CARD))
		{
			return "Credit Card";
		}
		else if (appl.equalsIgnoreCase("SPLN"))
		{
			return "Salaried Personal Loans-New";
		}
		else if (appl.equalsIgnoreCase("RAR"))
		{
			return "Retail Asset Reconstruction";
		}
		else if (appl.equalsIgnoreCase("CV"))
		{
			return "Commercial Vehicles";
		}
		else if (appl.equalsIgnoreCase("HF"))
		{
			return "Home Finance";
		}
		else if (appl.equalsIgnoreCase("CSG"))
		{
			return "Personal Finance";
		}
		else if (appl.equalsIgnoreCase("SPL"))
		{
			return "Salaried Personal Loans";
		}
		else if (appl.equalsIgnoreCase("SA"))
		{
			return "UNNATI [SARAL]";
		}
		else if (appl.equalsIgnoreCase("TFE"))
		{
			return "Tractor and Farm Equipment Loans";
		}
		else if (appl.equalsIgnoreCase("CE"))
		{
			return "Construction Equipment";
		}
		else if (appl.equalsIgnoreCase("LAP"))
		{
			return "Loan Against Property";
		}
		else if (appl.equalsIgnoreCase("SBG"))
		{
			return "Strategic Business Group";
		}
		else if (appl.equalsIgnoreCase("GLN"))
		{
			return "Gold Loan";
		}
		else if (appl.equalsIgnoreCase("LCV"))
		{
			return "Light Commercial Vehicles";
		}
		else if (appl.equalsIgnoreCase("RHB"))
		{
			return "Rural Housing Business";
		}
		else if (appl.equalsIgnoreCase("RARF"))
		{
			return "Retail ARD Funding";
		}
		else if (appl.equalsIgnoreCase("CLF"))
		{
			return "Car Lease Finance";
		}
		else if (appl.equalsIgnoreCase("CF"))
		{
			return "Car Finance";
		}
		else
		{
			return appl;
		}
	}

	private String getTollFreeNumberForAppl(String appl)
	{
		if (appl.equalsIgnoreCase(Constants.APPL_CARD) || appl.equalsIgnoreCase("HF") || appl.equalsIgnoreCase("LAP")
				|| appl.equalsIgnoreCase("SPL") || appl.equalsIgnoreCase("SPLN") || appl.equalsIgnoreCase("CSG"))
		{
			return "1800 102 6022";
		}
		else if (appl.equalsIgnoreCase("CV") || appl.equalsIgnoreCase("CE") || appl.equalsIgnoreCase("SA")
				|| appl.equalsIgnoreCase("TFE") || appl.equalsIgnoreCase("LCV") || appl.equalsIgnoreCase("GLN"))
		{
			return "1800 209 5600";
		}
		else if (appl.equalsIgnoreCase("RAR"))
		{
			return "1800 120 9820";
		}
		else if (appl.equalsIgnoreCase("CF") || appl.equalsIgnoreCase("CLF"))
		{
			return "1800 209 5732";
		}
		else
		{
			return "";
		}
	}

	public Message<String> submitCardSettlement(Message<String> message) throws Throwable
	{

		log.info(" -------- submitCardSettlement()-------- ");

		String webserviceUrl = "https://172.22.12.116:443/vmx";

		int validityCount = 0;
		String status = JsonConstants.FAILURE;
		String returnMessage = null;
		String requId = "";
		String appl = "";
		String cardNo = "";
		String partyName = "";
		String dueAmnt = "";
		String otherCharges = "";
		String creditLimit = "";
		String address2 = "";
		String address3 = "";
		String partyCorrAdd = "";
		String city = "";
		String pincode = "";
		String lPC = "";
		String partyMobNo = "";
		String principaloutstanding = "";
		String interest = "";
		String cd = "";
		String cycleDate = "";
		String eachEMI = "";
		String waiverLoss = "";
		String noOfEmi = "";
		String npv = "";
		String eBlockDoneOrNot = "";
		String centerManagementName = "";
		String lastEMIDate = "";
		String settlementAmnt = "";
		String longitude = "";
		String latitude = "";
		String mob = "";
		String caseBrief = "";
		String promoCode = "";
		String cibilScore="";
		String dPD ="";
		String loanRecallNoticeSentDate = "";
		String sec138InitDate = "";
		String arbitrationInitDate = "";
		String reasonForSettlement ="";
		String legalStatus="";
		String productCode = "";
		String currentBalance = "";

		JSONArray imageDetails = new JSONArray();

		boolean submissionFlag = false;

		List<Image> images = new ArrayList<Image>();

		ArrayList<SettlementEMIDetail> emiDetailsList = new ArrayList<SettlementEMIDetail>();

		JSONArray emiDetailsArray = new JSONArray();

		MessageHeaders messageHeader = message.getHeaders();

		SystemUser systemUser = (SystemUser) messageHeader.get(Constants.SYSTEM_USER_BEAN);

		Settlement settlement = new Settlement();

		try
		{
			String requestSet = message.getPayload();
			log.info("111111111--- requestSet == " + requestSet);
			JSONObject jsonObj = new JSONObject(requestSet);
			JSONObject jsonData = (JSONObject) jsonObj.get("data");
			jsonData.remove("images");
			jsonObj.put("data", jsonData);
			String reqSet = jsonObj.toString();
			log.info("222222222--- reqSet == " + reqSet);

			UserActivityAddition userActivityAddition = new UserActivityAddition(reqSet, userActivityService,
					ActivityLoggerConstants.DATABASE_MSSQL);
			new Thread(userActivityAddition).run();

			UserActivity userActivity = userActivityAddition.extractUserActivity();

			JSONObject jsonObject = new JSONObject(requestSet);
			JSONObject data = (JSONObject) jsonObject.get(JsonConstants.DATA);
			JSONObject user = (JSONObject) jsonObject.get(JsonConstants.SYSTEM_USER);
			SystemUser systemUserNew = ServerUtilities.extractSystemUser(user);

			log.info("-----systemUserNew-----" + systemUserNew.toString());

			Map reqMap = Utilities.createMapFromJSON(requestSet);

			log.info("----reqmap----" + reqMap.toString());

			String type = (String) reqMap.get(JsonConstants.Key.TYPE);

			appl = data.get(JsonConstants.APPL) == null ? "" : (String) data.get(JsonConstants.APPL);
			if (Utilities.checkInputForValidity(appl))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " appl = " + appl);
			}

			cardNo = data.get(JsonConstants.CARD_NO) == null ? "" : (String) data.get(JsonConstants.CARD_NO);
			if (Utilities.checkInputForNumber(cardNo))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " cardNo = " + cardNo);
			}

			if (data.has(JsonConstants.LATITUDE) && data.has(JsonConstants.LONGITUDE))
			{
				latitude = data.get(JsonConstants.LATITUDE) == null ? "" : data.getString(JsonConstants.LATITUDE);
				if (Utilities.checkInputForDouble(latitude))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " latitude = " + latitude);
				}
				longitude = data.get(JsonConstants.LONGITUDE) == null ? "" : data.getString(JsonConstants.LONGITUDE);
				if (Utilities.checkInputForDouble(longitude))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " longitude = " + longitude);
				}
			}
			else
			{
				latitude = "0.00";
				longitude = "0.00";
			}
			otherCharges = data.get(JsonConstants.OTHER_CHARGES) == null ? "" : data.get(JsonConstants.OTHER_CHARGES)
					.toString();
			if (Utilities.checkInputForDouble(otherCharges))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " otherCharges = " + otherCharges);
			}

			address2 = data.get(JsonConstants.ADDRESS2) == null ? "" : data.get(JsonConstants.ADDRESS2).toString();

			address3 = data.get(JsonConstants.ADDRESS3) == null ? "" : data.get(JsonConstants.ADDRESS3).toString();

			creditLimit = data.get(JsonConstants.CREDIT_LIMIT) == null ? "" : data.get(JsonConstants.CREDIT_LIMIT)
					.toString();
			if (Utilities.checkInputForDouble(creditLimit))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " creditLimit = " + creditLimit);
			}

			city = data.get(JsonConstants.CITY) == null ? "" : data.get(JsonConstants.CITY).toString();
			if (Utilities.checkInputForValidity(city))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " city = " + city);
			}

			pincode = data.get(JsonConstants.PINCODE) == null ? "" : data.get(JsonConstants.PINCODE).toString();
			if (Utilities.checkInputForNumber(pincode))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " pincode = " + pincode);
			}

			lPC = data.get(JsonConstants.LPC) == null ? "" : data.get(JsonConstants.LPC).toString();
			if (Utilities.checkInputForDouble(lPC))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " lPC = " + lPC);
			}

			partyMobNo = data.get(JsonConstants.MOBILE_NUMBER) == null ? "" : data.get(JsonConstants.MOBILE_NUMBER)
					.toString();
			if (Utilities.checkInputForNumber(partyMobNo))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " partyMobNo = " + partyMobNo);
			}

			eachEMI = data.get(JsonConstants.EACH_EMI) == null ? "" : data.get(JsonConstants.EACH_EMI).toString();
			if (Utilities.checkInputForDouble(eachEMI))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " eachEMI = " + eachEMI);
			}

			partyName = data.get(JsonConstants.NAME) == null ? "" : (String) data.get(JsonConstants.NAME);
			if (Utilities.checkInputForValidity(partyName))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " partyName = " + partyName);
			}

			noOfEmi = data.get(JsonConstants.SettlmentConstant.NUMBER_OF_EMI) == null ? "" : (String) data
					.get(JsonConstants.SettlmentConstant.NUMBER_OF_EMI);
			if (Utilities.checkInputForNumber(noOfEmi))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " noOfEmi = " + noOfEmi);
			}

			principaloutstanding = data.get(JsonConstants.SettlmentConstant.PRINCIPAL_OUTSTANDING) == null ? ""
					: (String) data.get(JsonConstants.PRINCIPAL_OUTSTANDING);
			if (Utilities.checkInputForDouble(principaloutstanding))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " principaloutstanding = " + principaloutstanding);
			}

			interest = data.get(JsonConstants.INTREST) == null ? "" : (String) data.get(JsonConstants.INTREST);
			if (Utilities.checkInputForDouble(interest))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " interest = " + interest);
			}

			cd = data.get(JsonConstants.CD) == null ? "" : (String) data.get(JsonConstants.CD);
			if (Utilities.checkInputForDouble(cd))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " cd = " + cd);
			}

			settlementAmnt = data.get(JsonConstants.SettlmentConstant.SETTLEMENT_AMNT) == null ? "" : (String) data
					.get(JsonConstants.SettlmentConstant.SETTLEMENT_AMNT);
			if (Utilities.checkInputForDouble(settlementAmnt))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " settlementAmnt = " + settlementAmnt);
			}

			partyCorrAdd = data.get(JsonConstants.SettlmentConstant.PARTY_CORR_ADDRESS) == null ? "" : (String) data
					.get(JsonConstants.SettlmentConstant.PARTY_CORR_ADDRESS);

			requId = data.get(JsonConstants.SettlmentConstant.REQUEST_ID) == null ? "" : (String) data
					.get(JsonConstants.SettlmentConstant.REQUEST_ID);
			if (Utilities.checkInputForNumber(requId))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " requId = " + requId);
			}

			/*dueAmnt = data.get(JsonConstants.SettlmentConstant.EMI_AMOUNT) == null ? "" : (String) data
					.get(JsonConstants.SettlmentConstant.EMI_AMOUNT);*/

			cycleDate = data.get("cycleDate") == null ? "" : (String) data.get("cycleDate");
			if (Utilities.checkInputForDateValidity(cycleDate))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " cycleDate = " + cycleDate);
			}

			waiverLoss = data.get(JsonConstants.WAIVER_LOSS) == null ? "" : (String) data
					.get(JsonConstants.WAIVER_LOSS);
			if (Utilities.checkInputForDouble(waiverLoss))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " waiverLoss = " + waiverLoss);
			}

			npv = data.get(JsonConstants.NPV) == null ? "" : (String) data.get(JsonConstants.NPV);
			if (Utilities.checkInputForDouble(npv))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " npv = " + npv);
			}

			eBlockDoneOrNot = data.get(JsonConstants.E_Block_Done_Or_Not) == null ? "" : (String) data
					.get(JsonConstants.E_Block_Done_Or_Not);
			if (Utilities.checkInputForValidity(eBlockDoneOrNot))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " eBlockDoneOrNot = " + eBlockDoneOrNot);
			}

			centerManagementName = data.get(JsonConstants.CENTER_MGT_NAME) == null ? "" : (String) data
					.get(JsonConstants.CENTER_MGT_NAME);
			if (Utilities.checkInputForValidity(centerManagementName))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " centerManagementName = " + centerManagementName);
			}

			lastEMIDate = data.get(JsonConstants.Last_EMI_Date) == null ? "" : (String) data
					.get(JsonConstants.Last_EMI_Date);
			if (Utilities.checkInputForDateValidity(lastEMIDate))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " lastEMIDate = " + lastEMIDate);
			}

			String requestEntity = data.get(JsonConstants.APPL) == null ? "" : data.getString(JsonConstants.APPL);
			if (Utilities.checkInputForValidity(requestEntity))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " requestEntity = " + requestEntity);
			}

			emiDetailsArray = data.getJSONArray(JsonConstants.SettlmentConstant.EMI_DETAILS);

			log.info("----getting emi details ----");

			String emiAmount = "";
			String bankName = "";
			String chqNumber = "";
			String emiDate = "";

			for (int i = 0; i < (emiDetailsArray.length()); i++)
			{

				JSONObject emiDetails = emiDetailsArray.getJSONObject(i);

				SettlementEMIDetail emiDet = new SettlementEMIDetail();

				emiAmount = emiDetails.get("emiAmnt") == null ? "0" : emiDetails.get("emiAmnt").toString();
				if (Utilities.checkInputForDouble(emiAmount))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " emiAmount = " + emiAmount);
				}

				bankName = emiDetails.get(JsonConstants.SettlmentConstant.BANK_NAME) == null ? "" : emiDetails.get(
						JsonConstants.SettlmentConstant.BANK_NAME).toString();
				if (Utilities.checkInputForValidity(bankName))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " bankName = " + bankName);
				}

				chqNumber = emiDetails.get(JsonConstants.SettlmentConstant.CHEQUE_NUMBER) == null ? "" : emiDetails
						.get(JsonConstants.SettlmentConstant.CHEQUE_NUMBER).toString();
				if (Utilities.checkInputForNumber(chqNumber))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " chqNumber = " + chqNumber);
				}

				emiDate = emiDetails.get(JsonConstants.SettlmentConstant.EMI_DATE) == null ? "" : emiDetails.get(
						JsonConstants.SettlmentConstant.EMI_DATE).toString();
				if (Utilities.checkInputForDateValidity(emiDate))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " emiDate = " + emiDate);
				}

				emiDet.setEmiAmount(emiAmount);
				emiDet.setBankName(bankName);
				emiDet.setChequeNo(chqNumber);
				emiDet.setEmiDate(emiDate);

				emiDetailsList.add(emiDet);

			}
			
			if(data.has(JsonConstants.SettlmentConstant.PROMO_CODE))
			{
				promoCode = data.get(JsonConstants.SettlmentConstant.PROMO_CODE) == null ? "" : (String) data
						.get(JsonConstants.SettlmentConstant.PROMO_CODE);
				if (Utilities.checkInputForValidity(promoCode))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " promoCode = " + promoCode);
				}

			}
			
			if(data.has(JsonConstants.SettlmentConstant.CASE_BRIEF))
			{
				caseBrief = data.get(JsonConstants.SettlmentConstant.CASE_BRIEF) == null ? "" : (String) data
						.get(JsonConstants.SettlmentConstant.CASE_BRIEF);
				if (Utilities.checkInputForValidity(caseBrief))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " caseBrief = " + caseBrief);
				}
			}
			
			if(data.has(JsonConstants.SettlmentConstant.CIBIL_SCRORE))
			{
				cibilScore = data.get(JsonConstants.SettlmentConstant.CIBIL_SCRORE) == null ? "" : (String) data
						.get(JsonConstants.SettlmentConstant.CIBIL_SCRORE);
				log.info("---cibilScore----");
				if (Utilities.checkInputForNumber(cibilScore))
				{
					log.info("---inside the false loop----" + cibilScore);
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " cibilScore = " + cibilScore);
				}
				
			}
			
			if(data.has(JsonConstants.SettlmentConstant.DPD))
			{
				dPD = data.get(JsonConstants.SettlmentConstant.DPD) == null ? "" : (String) data
						.get(JsonConstants.SettlmentConstant.DPD);
				if (Utilities.checkInputForNumber(dPD))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " dPD = " + dPD);
				}
			}
			
			
			if(data.has(JsonConstants.SettlmentConstant.LOAN_RECALL_NOTICE_DATE))
			{
				loanRecallNoticeSentDate = data.get(JsonConstants.SettlmentConstant.LOAN_RECALL_NOTICE_DATE) == null ? ""
						: (String) data.get(JsonConstants.SettlmentConstant.LOAN_RECALL_NOTICE_DATE);
				if (Utilities.checkInputForDateValidity(loanRecallNoticeSentDate))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " loanRecallNoticeSentDate = "
							+ loanRecallNoticeSentDate);
				}
			}
			
			if(data.has(JsonConstants.SettlmentConstant.SECTION138_DATE))
			{
				sec138InitDate = data.get(JsonConstants.SettlmentConstant.SECTION138_DATE) == null ? "" : (String) data
						.get(JsonConstants.SettlmentConstant.SECTION138_DATE);
				if (Utilities.checkInputForDateValidity(sec138InitDate))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " sec138InitDate = " + sec138InitDate);
				}
			}
			
			if(data.has(JsonConstants.SettlmentConstant.ARBITRATION_DATE))
			{
				arbitrationInitDate = data.get(JsonConstants.SettlmentConstant.ARBITRATION_DATE) == null ? ""
						: (String) data.get(JsonConstants.SettlmentConstant.ARBITRATION_DATE);
				if (Utilities.checkInputForDateValidity(arbitrationInitDate))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " arbitrationInitDate = " + arbitrationInitDate);
				}
			}
			
			if(data.has(JsonConstants.SettlmentConstant.SETTLMENT_REASON))
			{
				reasonForSettlement = data.get(JsonConstants.SettlmentConstant.SETTLMENT_REASON) == null ? ""
						: (String) data.get(JsonConstants.SettlmentConstant.SETTLMENT_REASON);
				if (Utilities.checkInputForValidity(reasonForSettlement))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " reasonForSettlement = " + reasonForSettlement);
				}				
			}
			
			if(data.has(JsonConstants.SettlmentConstant.LEGAL_STATUS))
			{
				legalStatus = data.get(JsonConstants.SettlmentConstant.LEGAL_STATUS) == null ? "" : (String) data
						.get(JsonConstants.SettlmentConstant.LEGAL_STATUS);
				if (Utilities.checkInputForValidity(legalStatus))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " legalStatus = " + legalStatus);
				}
			}

			if(data.has(JsonConstants.SettlmentConstant.PRODUCT_CODE))
			{
				productCode = data.get(JsonConstants.SettlmentConstant.PRODUCT_CODE) == null ? "" : (String) data
						.get(JsonConstants.SettlmentConstant.PRODUCT_CODE);
				if (Utilities.checkInputForValidity(legalStatus))
				{
					validityCount = validityCount + 1;
					log.info("--- validityCount = " + validityCount + " legalStatus = " + legalStatus);
				}
			}

			
			
			if (validityCount > 0)
			{
				log.info("--------- Validity Count is greater than zero ------------");

				status = JsonConstants.FAILURE;

				returnMessage = "Invalid data!!!";

				UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
						(ActivityLoggerConstants.STATUS_IGNORE), userActivityService);
				new Thread(userActivityStatusUpdate).run();

				return responseBuilder(message, status, returnMessage, settlement.getRequId());
			}
			currentBalance = data.get(JsonConstants.CURRENT_BALANCE) == null ? "" : (String) data
					.get(JsonConstants.CURRENT_BALANCE);

			settlement.setEmiDetails(emiDetailsList);

			/*imageDetails = data.getJSONArray(JsonConstants.SettlmentConstant.IMAGES);*/

			mob = data.get(JsonConstants.MOB) == null ? "" : data.get(JsonConstants.MOB).toString();
			if (Utilities.checkInputForNumber(partyMobNo))
			{
				validityCount = validityCount + 1;
				log.info("--- validityCount = " + validityCount + " mob = " + mob);
			}

			settlement.setAppl(appl);
			settlement.setApacNo(cardNo);
			settlement.setPartyName(partyName);
			settlement.setLongitude(longitude);
			settlement.setLatitude(latitude);
			settlement.setOtherCharges(otherCharges);
			settlement.setAddress2(address2);
			settlement.setAddress3(address3);
			settlement.setCreditLimit(creditLimit);
			settlement.setCity(city);
			settlement.setPin(pincode);
			settlement.setLpc(lPC);
			settlement.setMobileNumber(partyMobNo);
			settlement.setEachEmi(eachEMI);
			settlement.setNumberOfEmi(noOfEmi);
			settlement.setPrincipaloutstanding(principaloutstanding);
			settlement.setInterest(interest);
			settlement.setCd(cd);
			settlement.setSettlementAmount(settlementAmnt);
			settlement.setPartyCorrAdd(partyCorrAdd);
			settlement.setRequId(requId);
			//settlement.setDueAmnt(dueAmnt);
			//settlement.setDueDate(dueDate)
			settlement.setCycleDate(cycleDate);
			settlement.setWaiverOrLoss(waiverLoss);
			settlement.setNpv(npv);
			settlement.seteBlockStatus(eBlockDoneOrNot);
			settlement.setCurrentBalance(currentBalance);
			settlement.setCentreManagerName(centerManagementName);
			settlement.setLastEmiDate(lastEMIDate);
			settlement.setCreatedOn(Utilities.sysDate());
			settlement.setModifiedOn(Utilities.sysDate());
			settlement.setCreatedBy(String.valueOf(systemUserNew.getUserTableId()));
			settlement.setModifiedBy(String.valueOf(systemUserNew.getUserTableId()));
			settlement.setMob(mob);
			settlement.setPromoCode(promoCode);
			settlement.setCaseBrief(caseBrief);
			settlement.setCibilScore(cibilScore);
			settlement.setDPD(dPD);
			settlement.setLoanRecallNoticeSentDate(loanRecallNoticeSentDate);
			settlement.setSec138InitDate(sec138InitDate);
			settlement.setArbitrationInitDate(arbitrationInitDate);
			settlement.setReasonForSettlement(reasonForSettlement);
			settlement.setLegalStatus(legalStatus);
			settlement.setProduct(productCode);
			
			
			String diffBetwPrincipalSettlementAmt = String.valueOf(Double.parseDouble(principaloutstanding
					.equalsIgnoreCase("") || principaloutstanding == null ? "0" : principaloutstanding)
					- Double.parseDouble(settlementAmnt.equalsIgnoreCase("") || settlementAmnt == null ? "0"
							: settlementAmnt));
			
			settlement.setDiffBetwPrincipalSettlementAmt(diffBetwPrincipalSettlementAmt);
			
			/*images = getImages(systemUserNew, imageDetails, settlement);
			settlement.setImages(images);*/

			List<Map<String, Object>> rows = randomCollectionsExternalService.searchCardSettlement(cardNo);
			Map<String, Object> webserviceResponseMap = new HashMap<String, Object>();
			Map row = null;
			if (rows != null && rows.size() != 0)
			{
				row = rows.get(0);

				webserviceResponseMap.put(JsonConstants.CARD_NO, cardNo);
				webserviceResponseMap.put(JsonConstants.CURRENT_BALANCE, row.get("Current_Balance") == null ? "" : row
						.get("Current_Balance").toString());
				webserviceResponseMap
						.put(JsonConstants.NAME, row.get("NAME") == null ? "" : row.get("NAME").toString());
				webserviceResponseMap.put(JsonConstants.CORRESPONDENCE_ADDRESS,
						row.get("RESIDENCE_ADDRESS_1") == null ? "" : row.get("RESIDENCE_ADDRESS_1").toString());
				webserviceResponseMap.put(JsonConstants.ADDRESS2, row.get("RESIDENCE_ADDRESS_2") == null ? "" : row
						.get("RESIDENCE_ADDRESS_2").toString());
				webserviceResponseMap.put(JsonConstants.ADDRESS3, row.get("RESIDENCE_ADDRESS_3") == null ? "" : row
						.get("RESIDENCE_ADDRESS_3").toString());
				webserviceResponseMap
						.put(JsonConstants.CITY, row.get("CITY") == null ? "" : row.get("CITY").toString());
				webserviceResponseMap.put(JsonConstants.PINCODE, row.get("PINCODE") == null ? "" : row.get("PINCODE")
						.toString());
				webserviceResponseMap.put(JsonConstants.MOBILE_NUMBER,
						row.get("MOBILE_PHONE") == null ? "" : row.get("MOBILE_PHONE").toString());
				webserviceResponseMap.put(JsonConstants.BILLINGCYCLE,
						row.get("BILLING_CYCLE") == null ? "" : row.get("BILLING_CYCLE").toString());
				webserviceResponseMap.put(JsonConstants.MOB, row.get("MOB") == null ? "" : row.get("MOB").toString());// DATEDIFF(M,DATE_OPENED_G,GETDATE())
				// AS
				// MOB
				webserviceResponseMap.put(JsonConstants.CREDIT_LIMIT,
						row.get("Creditlimit") == null ? "" : row.get("Creditlimit").toString());
				webserviceResponseMap.put(JsonConstants.CD, row.get("CD") == null ? "" : row.get("CD").toString()); // Pmt_cycle_due
				// as
				// CD
				webserviceResponseMap.put(JsonConstants.PRINCIPAL_AMOUNT, row.get("Principal_Amount") == null ? ""
						: row.get("Principal_Amount").toString());
				webserviceResponseMap.put(JsonConstants.INTREST, row.get("Interest") == null ? "" : row.get("Interest")
						.toString());
				webserviceResponseMap.put(JsonConstants.LPC, row.get("LPC") == null ? "" : row.get("LPC").toString());// sum(LATE_CHG_BNP)
				// as
				// LPC
				webserviceResponseMap.put(JsonConstants.OTHER_CHARGES,
						row.get("Other_charges") == null ? "" : row.get("Other_charges").toString());
			}

			if (webserviceResponseMap != null && !webserviceResponseMap.isEmpty())
			{
				log.info("---- webserviceResponseMap--- " + webserviceResponseMap.toString());
				String currentBal = webserviceResponseMap.get(JsonConstants.CURRENT_BALANCE).toString();

				if (!currentBal.equalsIgnoreCase(settlement.getCurrentBalance()))
				{
					log.info("---- currentBal  -----validityCount -- " + validityCount);
					validityCount = validityCount + 1;
				}

				String Creditlimit = webserviceResponseMap.get(JsonConstants.CREDIT_LIMIT).toString();

				if (!Creditlimit.equalsIgnoreCase(settlement.getCreditLimit()))
				{
					log.info("---- Creditlimit  -----validityCount -- " + validityCount);
					validityCount = validityCount + 1;
				}

				String CD = webserviceResponseMap.get("CD").toString();

				if (!CD.equalsIgnoreCase(settlement.getCd()))
				{
					log.info("---- CD  -----validityCount -- " + validityCount);
					validityCount = validityCount + 1;
				}

				String principalAmount = webserviceResponseMap.get(JsonConstants.PRINCIPAL_AMOUNT).toString();

				if (!principalAmount.equalsIgnoreCase(settlement.getPrincipaloutstanding()))
				{
					log.info("---- principalAmount  -----validityCount -- " + validityCount);
					validityCount = validityCount + 1;
				}

				String Interest = webserviceResponseMap.get(JsonConstants.INTREST).toString();

				if (!Interest.equalsIgnoreCase(settlement.getInterest()))
				{
					log.info("---- Interest  -----validityCount -- " + validityCount);
					validityCount = validityCount + 1;
				}

				String lpc = webserviceResponseMap.get(JsonConstants.LPC).toString();

				if (!lpc.equalsIgnoreCase(settlement.getLpc()))
				{
					log.info("---- lpc  -----validityCount -- " + validityCount);
					validityCount = validityCount + 1;
				}

				String otherCharge = webserviceResponseMap.get(JsonConstants.OTHER_CHARGES).toString();

				if (!otherCharge.equalsIgnoreCase(settlement.getOtherCharges()))
				{
					log.info("---- otherCharge  -----validityCount -- " + validityCount);
					validityCount = validityCount + 1;
				}
			}

			if (validityCount > 0)
			{
				log.info("--------- Validity Count is greater than zero ------------");

				status = JsonConstants.FAILURE;

				returnMessage = "Invalid data!!!";

				UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
						(ActivityLoggerConstants.STATUS_IGNORE), userActivityService);
				new Thread(userActivityStatusUpdate).run();

				return responseBuilder(message, status, returnMessage, settlement.getRequId());
			}
			if (images == null)
			{

				status = JsonConstants.FAILURE;
				returnMessage = "Image Path Not Found";
				return responseBuilder(message, status, returnMessage, "");
			}
			log.info("----setttlement------" + settlement);

			if (settlementService.checkDuplicateSettlementJSON(settlement))
			{

				submissionFlag = settlementService.submitCardSettlement(settlement);

				log.info("-----submissionFlag-----" + submissionFlag);

				if (submissionFlag)
				{
					log.info("Settlement submitted without violation");

					try
					{
						//sendSms(settlement, systemUserNew);

					}
					catch (Exception e)
					{
						log.error("----Exception in SMS ----", e);

					}

					//add web service to block card

					/*String xmlRequest = generateXmlRequest();

					log.info("generated xml request :---------> -->" + xmlRequest);

					CommunicationActivityAddition communicationActivityAddition = new CommunicationActivityAddition(
							systemUserNew.getUserTableId().toString(), systemUserNew.getImeiNo(), (settlement.getAppl()
									+ "_" + "CardBlocking"), webserviceUrl, xmlRequest.toString(),
							communicationActivityService, ActivityLoggerConstants.DATABASE_MSSQL);

					new Thread(communicationActivityAddition).run();

					KotakCollectionWebserviceAdapter kotakCollectionWebserviceAdapter = new KotakCollectionWebserviceAdapter();

					String xmlResponse = kotakCollectionWebserviceAdapter.callWebserviceAndGetXmlString(
							xmlRequest.toString(), webserviceUrl);

					CommunicationActivity communicationActivity = communicationActivityAddition
							.extractCommunicationActivity();

					Map<String, Object> result = null;

					if (null != xmlResponse && !xmlResponse.equals(""))
					{
						communicationActivity.setResponse(xmlResponse);

						CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
								communicationActivity, (ActivityLoggerConstants.STATUS_SUCCESS),
								communicationActivityService);

						new Thread(communicationActivityStatusUpdate).run();

					}
					else
					{
						communicationActivity.setResponse(ActivityLoggerConstants.EMPTY_STRING);

						CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
								communicationActivity, (ActivityLoggerConstants.STATUS_FAILURE),
								communicationActivityService);

						new Thread(communicationActivityStatusUpdate).run();

						log.info("----- Failure in Blocking Card : -------");
					}

					log.info("xmlResponse to block card --------> --->" + xmlResponse);
					*/
					status = JsonConstants.SUCCESS;

					UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
							(ActivityLoggerConstants.STATUS_SUCCESS), userActivityService);
					new Thread(userActivityStatusUpdate).run();

					return responseBuilder(message, status, "Settlement got submitted successfully",
							settlement.getRequId());

				}
				else
				{
					System.out.println("Settlement submitted with violation");
					log.info("Settlement submitted with violation");

					status = JsonConstants.FAILURE;

					UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
							(ActivityLoggerConstants.STATUS_FAILURE), userActivityService);
					new Thread(userActivityStatusUpdate).run();

					return responseBuilder(message, status, "Some error has occured", "");

				}// status = JsonConstants.SUCCESS;

			}

			else
			{
				log.info("--------- Settlement Record already exists, JSON Duplicated! ------------");

				status = JsonConstants.SUCCESS;

				returnMessage = "JSON DUPLICATED!!!";

				UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
						(ActivityLoggerConstants.STATUS_IGNORE), userActivityService);
				new Thread(userActivityStatusUpdate).run();

				return responseBuilder(message, status, returnMessage, settlement.getRequId());
			}
		}

		catch (Exception e)
		{
			log.error("----Exception details---", e);
			e.printStackTrace();

			returnMessage = "Failure";
			return responseBuilder(message, status, returnMessage, "");
		}

	}

	/**
	 * @return
	 */
	private String generateXmlRequest()
	{
		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder();
		builder.append("<VMX_ROOT>");
		builder.append("<VMX_HEADER>");
		builder.append("<MSGID>VMX.CMS.CRLIM.UPD</MSGID>");
		builder.append("<VERSION>M8V3</VERSION>");
		builder.append("<CLIENTID>6146</CLIENTID>");
		builder.append("<CORRELID>1234</CORRELID>");
		builder.append("<CONTEXT>KOTVMX</CONTEXT>");
		builder.append("<NAME>00000KOTVMX</NAME>");
		builder.append("<UPDATE_FLAG>S</UPDATE_FLAG>");
		builder.append("<LOCAL_USE_FLAG></LOCAL_USE_FLAG>");
		builder.append("<ACCT_CRLIM></ACCT_CRLIM>");
		builder.append("<ACCT_NBR>0004166464300000437</ACCT_NBR>");
		builder.append("<TEMP_ACCT_CRLIM></TEMP_ACCT_CRLIM>");
		builder.append("<ACCT_DTE_TEMP_CRLIM_EXP></ACCT_DTE_TEMP_CRLIM_EXP>");
		builder.append("<ACCT_BLK_CODE_1></ACCT_BLK_CODE_1>");
		builder.append("<ACCT_BLK_CODE_2>K</ACCT_BLK_CODE_2>");
		builder.append("</VMX_MSGIN>");
		builder.append("</VMX_ROOT>");

		return builder.toString();
	}

	private long createSearchRequestMap(JSONObject requestData, String entity, Map<String, Object> wrapperMap)
			throws JSONException
	{
		Map<String, Object> requestMap = new HashMap<String, Object>();

		Map<String, Object> requestDetails = new HashMap<String, Object>();
		String uniqueId = requestData.getString(JsonConstants.UNIQUE_ID);

		requestDetails.put(XMLConstants.APPL, entity);
		requestDetails.put(XMLConstants.APACNUM, uniqueId);

		List<String> detailsList = new ArrayList<String>();
		detailsList.add(XMLConstants.APPL);
		detailsList.add(XMLConstants.APACNUM);

		StringBuilder requestXMLString = Utilities.generateXML(detailsList, requestDetails);

		long reqUID = System.currentTimeMillis();

		requestMap.put(XMLConstants.APP_CODE, XMLConstants.APP_CODE_VALUE);

		requestMap.put(XMLConstants.UID, reqUID);
		requestMap.put(XMLConstants.MESSAGE_TYPE, XMLConstants.MESSAGE_TYPE_MC005);
		requestMap.put(XMLConstants.MESSAGE_DATETIME, new Timestamp(reqUID));

		requestMap.put(XMLConstants.REQ_DETAILS, requestXMLString);

		List<String> headerList = new ArrayList<String>();
		headerList.add(XMLConstants.APP_CODE);
		headerList.add(XMLConstants.UID);

		headerList.add(XMLConstants.MESSAGE_TYPE);

		headerList.add(XMLConstants.MESSAGE_DATETIME);
		headerList.add(XMLConstants.REQ_DETAILS);

		StringBuilder requestBodyXMLString = Utilities.generateXML(headerList, requestMap);
		wrapperMap.put(XMLConstants.MC005, requestBodyXMLString);

		return reqUID;
	}
	

	public MultipartFile convertToMultipartFile(byte[] bytes, String fileName) {
	    ByteArrayMultipartFile file = new ByteArrayMultipartFile(bytes, fileName);
	    MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
	    map.add("file", file);
	    return file;
	}

	class ByteArrayMultipartFile implements MultipartFile {
	    private final byte[] content;
	    private final String name;
	    private final String contentType;

	    public ByteArrayMultipartFile(byte[] content, String name) {
	        this.content = content;
	        this.name = name;
	        this.contentType = "application/octet-stream";
	    }

	    @Override
	    public String getName() {
	        return name;
	    }

	    @Override
	    public String getOriginalFilename() {
	        return name;
	    }

	    @Override
	    public String getContentType() {
	        return contentType;
	    }

	    @Override
	    public boolean isEmpty() {
	        return content.length == 0;
	    }

	    @Override
	    public long getSize() {
	        return content.length;
	    }

	    @Override
	    public byte[] getBytes() {
	        return content;
	    }

		@Override
		public InputStream getInputStream() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void transferTo(File dest) throws IOException, IllegalStateException {
			// TODO Auto-generated method stub
			
		}

	}
	
	private String getImagesByte(JSONArray imageDetails)
			throws JSONException
	{
		
		JSONObject imageDetail = new JSONObject();
		String imageByteArray = Constants.EMPTY_STRING;
		for (int index = 0; index < imageDetails.length(); index++)
		{

			imageDetail = (JSONObject) imageDetails.get(index);

			if (!imageDetail.has(JsonConstants.RequestData.IMAGE))
			{

				return Constants.EMPTY_STRING;
			}

			imageByteArray = (String) imageDetail.get(JsonConstants.RequestData.IMAGE);
			if (imageByteArray.isEmpty())
			{

				return Constants.EMPTY_STRING;
			}
		

			
		}
		return imageByteArray;
	}
	 
	public void getDefaultPathFile(String fileName ,String token , String imageString,Settlement settlement,SystemUser user
			,String url,Long settlementId) {
		JSONObject json = new JSONObject();
		log.info("Inside getDefaultPathFile ::: ");
		try {
			File outputFile = Files.createTempFile(null,null).toFile();
	        
	        byte[] decodedBytes = Base64.getDecoder().decode(imageString);
			 FileUtils.writeByteArrayToFile(outputFile, decodedBytes);
			
			OkHttpClient client = new OkHttpClient().newBuilder().build();
			MediaType mediaType = MediaType.parse("text/plain");
			RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
					.addFormDataPart("file", fileName,
							RequestBody.create(MediaType.parse("application/octet-stream"), outputFile))
					//.addFormDataPart("filetype", "FRONT_VIEW")
					.build();
			Request request = new Request.Builder().url(url).method("POST", body)
					.addHeader("Authorization", "Bearer "+token)
					//.addHeader("Cookie", "JSESSIONID=28E4051C8CEF780BCF0EB7A549477565")
					.build();
			Response response = client.newCall(request).execute();
			String responseBody = response.body().string();
			log.info("String json :: "+responseBody);
			outputFile.deleteOnExit();
			json = new JSONObject(responseBody);
			log.info("json :: " + json);
			String getFileRef = json.optString("fileRef") == null ? Constants.EMPTY_STRING : json.optString("fileRef");
			String getDocumentName = json.optString("name") == null ? Constants.EMPTY_STRING : json.optString("name");
			
			if(getFileRef!=null && !getFileRef.equals(Constants.EMPTY_STRING)) {
				SettlementCasesDocument settlementCasesDocument = new SettlementCasesDocument();
				settlementCasesDocument.setDocument(getFileRef);
				settlementCasesDocument.setDocumentName(getDocumentName);
				
				boolean flag = settlementService.InsertSettlementImage(settlement, user, settlementCasesDocument, settlementId);
				log.info("Flag of db update ::" + flag);
			}
			
		}catch (Exception e) {
			log.info("Exception :: " + e);
		}
		
	}
	
}