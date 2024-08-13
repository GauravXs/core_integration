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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;
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
import com.mobicule.mcollections.core.beans.Image;
import com.mobicule.mcollections.core.beans.MPOSDetail;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.beans.TransactionType;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.EmailUtilities;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.AgencyService;
import com.mobicule.mcollections.core.service.CaseService;
import com.mobicule.mcollections.core.service.CollectionService;
import com.mobicule.mcollections.core.service.MposService;
import com.mobicule.mcollections.core.service.OfflineSMSService;
import com.mobicule.mcollections.core.service.SystemUserService;
import com.mobicule.mcollections.integration.commons.ServerUtilities;
import com.mobicule.mcollections.webservice.adapter.KotakCollectionWebserviceAdapter;

/**
* 
* <enter description here>
*
* @Bhushan Patil
* @see 
*
* @createdOn 06-Sept-2017
* @modifiedOn
*
* @copyright © 2017-2018 Mobicule Technologies Pvt. Ltd. All rights reserved.
*/

public class MposFailedTransSubmissionService implements IMposFailedTransSubmissionService
{

	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private UserActivityService userActivityService;

	@Autowired
	private SystemUserService systemUserService;

	@Autowired
	private CommunicationActivityService communicationActivityService;

	
	@Autowired
	private  MposService mposService;

	
	@Override
	public Message<String> execute(Message<String> message) throws Throwable
	{
		log.info(" -------- In MposFailedTransSubmissionService -------- ");

		String status = JsonConstants.FAILURE;
		String returnMessage = null;
		String contractAccountNumber = "";		
		String collectionCode = "";		
		String amount = "0.0";		
		String email = "";
		String contact = "";
		String deviceDate = "";		
		String area = "";		
		String deviceTime = "";		
		String receiptNumber = "";		
		String ptpAmount = "0.00";			
		boolean submissionFlag = false;	
		

		List<Image> images = new ArrayList<Image>();
		MessageHeaders messageHeader = message.getHeaders();

		SystemUser systemUser = (SystemUser) messageHeader.get(Constants.SYSTEM_USER_BEAN);

		try
		{
			String requestSet = message.getPayload();

			/**/

			JSONObject jsonObj = new JSONObject(requestSet);

			JSONObject jsonData = (JSONObject) jsonObj.get(JsonConstants.DATA);
			if (jsonData.has("images"))
			{
				jsonData.remove("images");
			}

			jsonObj.put(JsonConstants.DATA, jsonData);

			String requestWithoutImage = jsonObj.toString();

			UserActivityAddition userActivityAddition = new UserActivityAddition(requestWithoutImage,
					userActivityService, ActivityLoggerConstants.DATABASE_MSSQL);

			new Thread(userActivityAddition).run();

			UserActivity userActivity = userActivityAddition.extractUserActivity();

			JSONObject jsonObject = new JSONObject(requestSet);

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

			String requestEntity = data.get(JsonConstants.APPL) == null ? "" : data.getString(JsonConstants.APPL);
			

			collectionCode = data.get(JsonConstants.RequestData.COLLECTION_CODE) == null ? "" : (String) data
					.get(JsonConstants.RequestData.COLLECTION_CODE);

			if (((!collectionCode.equalsIgnoreCase("RTP")) || amount.equals(Constants.EMPTY_STRING))
					&& !collectionCode.equalsIgnoreCase("PU"))
			{
				log.info("---test 1");

				if (data.has(JsonConstants.PTP_AMOUNT))
					ptpAmount = data.getString(JsonConstants.PTP_AMOUNT);

			}

			else if (collectionCode.equalsIgnoreCase("PU"))
			{

				log.info("---test 2");

				if (data.has(JsonConstants.RequestData.AMOUNT))
					ptpAmount = data.getString(JsonConstants.RequestData.AMOUNT);

			}
			else
			{

				log.info("---test 3");
				if (data.has(JsonConstants.RequestData.AMOUNT))
					amount = (String) data.get(JsonConstants.RequestData.AMOUNT);

			}

			deviceTime = data.get(JsonConstants.RequestData.DEVICE_TIME) == null ? "" : (String) data
					.get(JsonConstants.RequestData.DEVICE_TIME);

			deviceDate = data.get(JsonConstants.RequestData.DEVICE_DATE) == null ? "" : (String) data
					.get(JsonConstants.RequestData.DEVICE_DATE);

			area = data.get(JsonConstants.RequestData.AREA) == null ? "" : (String) data
					.get(JsonConstants.RequestData.AREA);
			
			receiptNumber = data.get(JsonConstants.RequestData.RECEIPT_NUMBER) == null ? "" : (String) data
					.get(JsonConstants.RequestData.RECEIPT_NUMBER);			

			email = data.get(JsonConstants.EMAIL_ADDRESS) == null ? "" : data.getString(JsonConstants.EMAIL_ADDRESS);

			contact = data.get(JsonConstants.MOBILE_NUMBER) == null ? "" : data.getString(JsonConstants.MOBILE_NUMBER);			
			

			String payMode = data.get(JsonConstants.RequestData.PAY_MODE) == null ? "" : (String) data
					.get(JsonConstants.RequestData.PAY_MODE);
		
			collection.setOutstanding(Double.parseDouble(data.getString(JsonConstants.OUTSTANDING) == null
					|| data.getString(JsonConstants.OUTSTANDING).equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0"
							: data.getString(JsonConstants.OUTSTANDING)));

			log.info("---collection code" + collectionCode);		
		

			JSONArray mPOSTransDetails = new JSONArray();

			JSONObject mPOSTransDetail = new JSONObject();
			MPOSDetail mposDetail = new MPOSDetail();
			if (data.has(JsonConstants.mPOS_TRANS_DETAILS))
			{
				mPOSTransDetail = (JSONObject) data.get(JsonConstants.mPOS_TRANS_DETAILS);
				

				mposDetail.setTransactionId(mPOSTransDetail.has(JsonConstants.mPOS_TRANS_ID) ? mPOSTransDetail.get(
						JsonConstants.mPOS_TRANS_ID).toString() : "");
				mposDetail.setBillNumber(mPOSTransDetail.has(JsonConstants.mPOS_BILL_NUMBER) ? mPOSTransDetail.get(
						JsonConstants.mPOS_BILL_NUMBER).toString() : "");
				mposDetail.setCardNo(mPOSTransDetail.has(JsonConstants.mPOS_CARD_NUMBER) ? mPOSTransDetail.get(
						JsonConstants.mPOS_CARD_NUMBER).toString() : "");
				mposDetail
						.setTransactionDateTime(mPOSTransDetail.has(JsonConstants.mPOS_TRANS_DATE_TIME) ? mPOSTransDetail
								.get(JsonConstants.mPOS_TRANS_DATE_TIME).toString() : "");
				mposDetail.setSwipeAmount(mPOSTransDetail.has(JsonConstants.mPOS_SWIPE_AMOUNT) ? mPOSTransDetail.get(
						JsonConstants.mPOS_SWIPE_AMOUNT).toString() : "");
				mposDetail.setCardHolderName(mPOSTransDetail.has(JsonConstants.mPOS_CARD_HOLDER_NAME) ? mPOSTransDetail
						.get(JsonConstants.mPOS_CARD_HOLDER_NAME).toString() : "");
				mposDetail.setCardType(mPOSTransDetail.has(JsonConstants.mPOS_CARD_TYPE) ? mPOSTransDetail.get(
						JsonConstants.mPOS_CARD_TYPE).toString() : "");
			}

			collection.setMposDetail(mposDetail);
			

			if (type.toString().equalsIgnoreCase("mPOSCollections"))
			{
				collection.setCollectionType(Constants.COLLECTIONS);

				collection.setCollectionNature(Constants.COLLECTION_NATURE_GENERAL);
			}
			

			if (type.toString().equalsIgnoreCase("mPOSCollections"))
			{
				if (data.has(JsonConstants.NAME))
				{
					collection.setPartyName(data.getString(JsonConstants.NAME));
				}
			}
			if (type.toString().equalsIgnoreCase("mPOSCollections"))
			{
				
				log.info("mPOSCollections  CONTRACT_ACCOUNT_NUMBER");
				if (data.has(JsonConstants.CONTRACT_ACCOUNT_NUMBER))
				{
					collection.setContractAccountNumber(data.getString(JsonConstants.CONTRACT_ACCOUNT_NUMBER));
				}
				log.info("CONTRACT_ACCOUNT_NUMBER  ::" +collection.getContractAccountNumber());
			}

			if (data.has(JsonConstants.CC_APAC))
			{
				collection.setCcapac(data.getString(JsonConstants.CC_APAC));
			}
			
			int numberOfApacs = 0;
			numberOfApacs = data.getString("noOfApac") == null ? 0 : Integer.parseInt(data.getString("noOfApac"));

			String DeviceDateTime=deviceDate+" "+deviceTime; 
			collection.setNumberOfApacs(numberOfApacs);
			
			collection.setCollectionCode(collectionCode);			
			collection.setRequestId(data.has(JsonConstants.REQUEST_ID) == true ? data
					.getString(JsonConstants.REQUEST_ID) : new Timestamp(System.currentTimeMillis()).toString());			
			collection.setArea(area);		
			collection.setCollectionCode(collectionCode);
			collection.setReceiptNumber(receiptNumber);			
			collection.setPaymentMode(payMode);
			collection.setDeviceDate(DeviceDateTime);			
			collection.setSubmissionDateTime(Utilities.sysDate());			
			collection.setBusinessPartnerNumber(data.getString(JsonConstants.UNIQUE_NUMBER)); // apac or card
																								// number
			collection.setAppl(data.getString(JsonConstants.APPL));			
			collection.setMobileNumber(data.getString(JsonConstants.MOBILE_NUMBER));			
			collection.setUserName(systemUserNew.getName());			
			collection.setUser(systemUserNew); // new added
			collection.setContact(contact);
			collection.setEmail(email);
			collection.setAmount(amount);
			Utilities.primaryBeanSetter(collection, systemUserNew);
			
			
			
			if (mposService.checkDuplicateMposFailureCollectionJSON(collection))
			{
				log.info("collection cheque details ========----------->" + collection.getChequeDetails());
				log.info("complete collection -------------->" + collection);

				submissionFlag = mposService.submitFailMposCollection(collection);
				log.info("-------submissionFlag--------" + submissionFlag);

				if (submissionFlag)
				{
					log.info("Collection submitted without violation");
					
					status = JsonConstants.SUCCESS;

					UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
							(ActivityLoggerConstants.STATUS_SUCCESS), userActivityService);
					new Thread(userActivityStatusUpdate).run();

					return responseBuilder(message, status, "Collection got submitted successfully",
							collection.getRequestId());

				}
				else
				{
					
					log.info("Collection submitted with violation");

					status = JsonConstants.FAILURE;

					UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
							(ActivityLoggerConstants.STATUS_FAILURE), userActivityService);
					new Thread(userActivityStatusUpdate).run();

					return responseBuilder(message, status, "Some error has occured", "");

				}

			}

			else
			{
				log.info("--------- Collection Record already exists, JSON Duplicated! ------------");
				status = JsonConstants.SUCCESS;
				

				returnMessage = "JSON DUPLICATED!!!";

				if (type.toString().equalsIgnoreCase("mPOSCollections"))
				{
					returnMessage = "JSON DUPLICATED For Collections!!!";
				}
				
				UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
						(ActivityLoggerConstants.STATUS_IGNORE), userActivityService);
				new Thread(userActivityStatusUpdate).run();

				return responseBuilder(message, status, returnMessage, collection.getRequestId());
			}
		}

		catch (Exception e)
		{
			e.printStackTrace();
			log.error("--- Exception In CollectionSubmissionService :: " + e);

			returnMessage = JsonConstants.COLLECTION_SUBMIT_FAILURE;
			return responseBuilder(message, status, returnMessage, "");
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
