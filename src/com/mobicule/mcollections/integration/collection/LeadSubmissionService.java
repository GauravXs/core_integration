package com.mobicule.mcollections.integration.collection;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;

import org.springframework.integration.support.MessageBuilder;

import com.mobicule.component.activitylogger.beans.UserActivity;
import com.mobicule.component.activitylogger.commons.ActivityLoggerConstants;
import com.mobicule.component.activitylogger.service.UserActivityService;
import com.mobicule.component.activitylogger.threads.UserActivityAddition;
import com.mobicule.component.activitylogger.threads.UserActivityStatusUpdate;
import com.mobicule.mcollections.core.beans.Lead;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.service.CaseService;
import com.mobicule.mcollections.core.service.LeadService;
import com.mobicule.mcollections.core.service.SystemUserService;
import com.mobicule.mcollections.integration.commons.ServerUtilities;

/**
 * @author prashant
 *
 */
public class LeadSubmissionService implements ILeadSubmissionService {
	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private SystemUserService systemUserService;

	@Autowired
	private UserActivityService userActivityService;

	@Autowired
	private CaseService caseService;

	@Autowired
	private LeadService leadService;

	private static final String TRANSACTION_ID = "transactionId";

	@Override
	public Message<String> execute(Message<String> message)
			throws JSONException {
		String status = JsonConstants.FAILURE;
		String returnMessage = null;

		Lead lead = new Lead();

		int validityCount = 0;

		log.info("------inside lead generation service------");

		try {
			String requestSet = message.getPayload();
			String requestEntity = JSONPayloadExtractor.extract(requestSet,
					JsonConstants.ENTITY);
			String requestAction = JSONPayloadExtractor.extract(requestSet,
					JsonConstants.ACTION);

			log.info("-------requestSet is-----" + requestSet);
			log.info("-----requestEntity----" + requestEntity);
			log.info("-----requestAction----" + requestAction);

			UserActivityAddition userActivityAddition = new UserActivityAddition(
					requestSet, userActivityService,
					ActivityLoggerConstants.DATABASE_MSSQL);

			new Thread(userActivityAddition).run();

			UserActivity userActivity = userActivityAddition
					.extractUserActivity();

			JSONObject jsonObject = new JSONObject(requestSet);
			JSONObject data = (JSONObject) jsonObject.get(JsonConstants.DATA);
			JSONObject user = (JSONObject) jsonObject
					.get(JsonConstants.SYSTEM_USER);
			SystemUser systemUserNew = ServerUtilities.extractSystemUser(user);

			SystemUser systemUserTemp = systemUserService.getUser(systemUserNew
					.getUserTableId());
			systemUserNew.setSupervisorMobileNumber(systemUserTemp
					.getSupervisorMobileNumber());
			systemUserNew.setSupervisorName(systemUserTemp.getSupervisorName());

			Map reqMap = Utilities.createMapFromJSON(requestSet);

			
			lead.setAppl(data.get(JsonConstants.leadData.PORTFOLIO).toString());

			if (Utilities.checkInputForString(data.get(
					JsonConstants.leadData.PORTFOLIO).toString())) {

				validityCount = validityCount + 1;
			}

			lead.setProduct(data.getString(JsonConstants.leadData.PRODUCT));

			if (Utilities.checkInputForString(data.get(
					JsonConstants.leadData.PRODUCT).toString())) {

				validityCount = validityCount + 1;
			}

			lead.setPartyName(data.getString(JsonConstants.leadData.PARTY_NAME));

			if (Utilities.checkInputForString(data.get(
					JsonConstants.leadData.PARTY_NAME).toString())) {

				validityCount = validityCount + 1;
			}

			lead.setAddress(data.getString(JsonConstants.leadData.ADDRESS));

			if (Utilities.checkInputForValidity(data.get(
					JsonConstants.leadData.ADDRESS).toString())) {

				validityCount = validityCount + 1;
			}

			lead.setMobileNumber(data
					.getString(JsonConstants.leadData.MOBILE_NUMBER));

			if (Utilities.checkInputForNumber(data.get(
					JsonConstants.leadData.MOBILE_NUMBER).toString())) {

				validityCount = validityCount + 1;
			}

			lead.setLandlineNumber(data
					.getString(JsonConstants.leadData.LANDLINE_NUMBER));

			if (Utilities.checkInputForNumber(data.get(
					JsonConstants.leadData.LANDLINE_NUMBER).toString())) {

				validityCount = validityCount + 1;
			}

			lead.setAmount(data.getString(JsonConstants.leadData.AMOUNT));

			if (Utilities.checkInputForNumberWithDecimal(data.get(
					JsonConstants.leadData.AMOUNT).toString())) {

				validityCount = validityCount + 1;
			}

			lead.setTenure(data.getString(JsonConstants.leadData.TENURE));

			if (Utilities.checkInputForNumber(data.get(
					JsonConstants.leadData.TENURE).toString())) {

				validityCount = validityCount + 1;
			}

			lead.setManufacturer(data
					.getString(JsonConstants.leadData.MANUFACTURER));

			if (Utilities.checkAlphaNumericValidity(data.get(
					JsonConstants.leadData.MANUFACTURER).toString())) {

				validityCount = validityCount + 1;
			}

			lead.setDealer(data.getString(JsonConstants.leadData.DEALER));

			if (Utilities.checkAlphaNumericValidity(data.get(
					JsonConstants.leadData.DEALER).toString())) {

				validityCount = validityCount + 1;
			}

			lead.setModel(data.getString(JsonConstants.leadData.MODEL));

			if (Utilities.checkAlphaNumericValidity(data.get(
					JsonConstants.leadData.MODEL).toString())) {

				validityCount = validityCount + 1;
			}

			lead.setRemarks(data.getString(JsonConstants.leadData.REMARKS));

			if (Utilities.checkAlphaNumericValidity(data.get(
					JsonConstants.leadData.LANDLINE_NUMBER).toString())) {

				validityCount = validityCount + 1;
			}

			lead.setTransactionID(data.get(TRANSACTION_ID).toString());

			if (Utilities.checkInputForNumber(data.get(TRANSACTION_ID)
					.toString())) {

				validityCount = validityCount + 1;
			}

			lead.setUserID(systemUserNew.getUserTableId().toString());

			lead.setLatitude(data.get("lat").toString());

			if (Utilities.checkInputForNumberWithDecimal(data.get("long")
					.toString())) {

				validityCount = validityCount + 1;
			}

			lead.setLongitude(data.get("long").toString());

			if (Utilities.checkInputForNumberWithDecimal(data.get("long")
					.toString())) {

				validityCount = validityCount + 1;
			}

			lead.setCollectorLocation(data.get("collectorLocation").toString());

			if (Utilities.checkInputForString(data.get("collectorLocation")
					.toString())) {

				validityCount = validityCount + 1;
			}

			// Added

			lead.setState(data.get(JsonConstants.leadData.STATE) == null ? Constants.EMPTY_STRING
					: (String) data.get(JsonConstants.leadData.STATE));

			if (Utilities.checkInputForString(data.get(
					JsonConstants.leadData.STATE).toString()))

			{
				validityCount = validityCount + 1;

			}

			lead.setDistrict(data.get(JsonConstants.leadData.DISTRICT) == null ? Constants.EMPTY_STRING
					: (String) data.get(JsonConstants.leadData.DISTRICT));

			if (Utilities.checkInputForString(data.get(
					JsonConstants.leadData.DISTRICT).toString())) {

				validityCount = validityCount + 1;

			}
			lead.setTaluka(data.get(JsonConstants.leadData.TALUKA) == null ? Constants.EMPTY_STRING
					: (String) data.get(JsonConstants.leadData.TALUKA));

			if (Utilities.checkInputForString(data.get(
					JsonConstants.leadData.TALUKA).toString())) {

				validityCount = validityCount + 1;

			}

			lead.setPincode(data.get(JsonConstants.leadData.PINCODE) == null ? Constants.EMPTY_STRING
					: (String) data.get(JsonConstants.leadData.PINCODE));

			if (Utilities.checkInputForNumber(data.get(
					JsonConstants.leadData.PINCODE).toString()))

			{
				validityCount = validityCount + 1;
			}

			lead.setVillage(data.get(JsonConstants.leadData.VILLAGE) == null ? Constants.EMPTY_STRING
					: (String) data.get(JsonConstants.leadData.VILLAGE));

			if (Utilities.checkInputForString(data.get(
					JsonConstants.leadData.VILLAGE).toString())) {
				validityCount = validityCount + 1;

			}

			lead.setProductDesc(data.get(JsonConstants.leadData.PRODUCT_DESC) == null ? Constants.EMPTY_STRING
					: data.get(JsonConstants.leadData.PRODUCT_DESC).toString());

			if (Utilities.checkInputForString(data.get(
					JsonConstants.leadData.PRODUCT_DESC).toString()))

			{
				validityCount = validityCount + 1;
			}

			/*
			 * if (validityCount > 0) {
			 * 
			 * 
			 * 
			 * return responseBuilder(message, status, "Invalid Content");
			 * 
			 * }
			 */

			/*
			 * }
			 */

			log.info("-----lead----" + lead);

			if (leadService.checkDublicateLead(data.get(TRANSACTION_ID)
					.toString())) {

			

				boolean flag = leadService.submitLead(lead);

				if (flag) {
					log.info("------------- lead Submitted and Case Updated sucessfully -------------");

					status = JsonConstants.SUCCESS;

					UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(
							userActivity,
							(ActivityLoggerConstants.STATUS_SUCCESS),
							userActivityService);
					new Thread(userActivityStatusUpdate).run();

					return responseBuilder(message, status,
							"Lead got submitted successfully");
				} else {

					status = JsonConstants.FAILURE;

					returnMessage = "Failure !!!";

					UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(
							userActivity,
							(ActivityLoggerConstants.STATUS_IGNORE),
							userActivityService);
					new Thread(userActivityStatusUpdate).run();

					return responseBuilder(message, status, returnMessage);
				}

			}

			else {

				log.info("--------- Lead Record already exists, JSON Duplicated! ------------");

				status = JsonConstants.SUCCESS;

				returnMessage = "JSON DUPLICATED!!!";

				UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(
						userActivity, (ActivityLoggerConstants.STATUS_IGNORE),
						userActivityService);
				new Thread(userActivityStatusUpdate).run();

				return responseBuilder(message, status, returnMessage);

			}

		}

		catch (Exception e) {

			returnMessage = Constants.FAILURE;

			return responseBuilder(message, status, returnMessage);

		}

	}

	private Message<String> responseBuilder(Message<String> message,
			String status, String returnMessage) throws JSONException {
		JSONObject responseJSON = new JSONObject();
		responseJSON.put(JsonConstants.STATUS, status);
		responseJSON.put(JsonConstants.MESSAGE, returnMessage);
		responseJSON.put(JsonConstants.DATA, Constants.EMPTY_STRING);

		return MessageBuilder.withPayload(String.valueOf(responseJSON))
				.copyHeaders(message.getHeaders()).build();
	}

}
