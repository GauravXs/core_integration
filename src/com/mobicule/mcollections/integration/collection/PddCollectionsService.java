/**
 * 
 */
package com.mobicule.mcollections.integration.collection;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.mail.javamail.JavaMailSender;

import antlr.debug.NewLineEvent;

import com.mobicule.component.activitylogger.beans.UserActivity;
import com.mobicule.component.activitylogger.commons.ActivityLoggerConstants;
import com.mobicule.component.activitylogger.service.CommunicationActivityService;
import com.mobicule.component.activitylogger.service.UserActivityService;
import com.mobicule.component.activitylogger.threads.UserActivityAddition;
import com.mobicule.component.activitylogger.threads.UserActivityStatusUpdate;
import com.mobicule.mcollections.core.beans.PddCollection;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.EmailUtilitiesPdd;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.PddService;
import com.mobicule.mcollections.integration.commons.ServerUtilities;
import com.mobicule.mcollections.webservice.adapter.KotakCollectionWebserviceAdapter;
import com.mobicule.component.mapconversion.json.MapToJSON;

/**
 * @author bhushan
 *
 */

public class PddCollectionsService implements IPddCollectionsService
{
	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private UserActivityService userActivityService;

	@Autowired
	public PddService pddService;

	@Autowired
	private CommunicationActivityService communicationActivityService;

	@Autowired
	private KotakCollectionWebserviceAdapter webserviceAdapter;

	private ApplicationConfiguration<String, String> applicationConfiguration;

	@Autowired
	private JavaMailSender mailSender;

	@Override
	public Message<String> execute(Message<String> message) throws Throwable
	{

		log.info("---- Inside  PddCollectionsService / execute Methode ----");

		JSONObject responseJSON = new JSONObject();

		try
		{
			String requestSet = message.getPayload();

			String requestEntity = JSONPayloadExtractor.extract(requestSet, JsonConstants.ENTITY);
			String requestAction = JSONPayloadExtractor.extract(requestSet, JsonConstants.ACTION);

			JSONObject requestSystemUser = JSONPayloadExtractor.extractJSON(requestSet, JsonConstants.SYSTEM_USER);

			JSONObject requestData = JSONPayloadExtractor.extractJSON(requestSet, JsonConstants.DATA);

			SystemUser systemUser = ServerUtilities.extractSystemUser(requestSystemUser);

			//String tempccapac = systemUser.getCcapac();

			//systemUser.setCcapac(tempccapac);

			UserActivityAddition userActivityAddition = new UserActivityAddition(requestSet, userActivityService,
					ActivityLoggerConstants.DATABASE_MSSQL);

			new Thread(userActivityAddition).run();

			UserActivity userActivity = userActivityAddition.extractUserActivity();

			if (requestAction.equalsIgnoreCase(JsonConstants.ACTION_SUBMIT))
			{
				submitPddCollection(responseJSON, requestSet, systemUser, requestEntity, userActivity,
						communicationActivityService);
			}
			else if (requestAction.equalsIgnoreCase(JsonConstants.ACTION_SEARCH))
			{
				searchPddCollection(responseJSON, requestData, requestEntity, userActivity,
						communicationActivityService, systemUser);
			}

		}
		catch (Exception e)
		{
			log.error("Exception :: " + e);
			e.printStackTrace();
			responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
			responseJSON.put(JsonConstants.MESSAGE, "Failure while Submitting Pdd Collection.");
			responseJSON.put(JsonConstants.DATA, Constants.EMPTY_STRING);

		}

		return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
	}

	private void searchPddCollection(JSONObject responseJSON, JSONObject requestData, String entity,
			UserActivity userActivity, CommunicationActivityService communicationActivityService, SystemUser systemUser)
			throws JSONException
	{
		try
		{
			log.info("---- Inside  PddCollectionsService / searchPddCollection Methode ----");

			JSONObject response = null;

			Map<String, Object> webserviceResponseMap = new HashMap<String, Object>();
			List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
			Map row = null;
			try
			{
				rows = pddService.searchPddCollections(requestData.getString(JsonConstants.UNIQUE_ID), entity);

				log.info("Rows :: " + rows.toString());

				if (rows != null && rows.size() != 0)
				{
					row = rows.get(0);

					if (row.get(Constants.PddViewConstants.IS_COLLECTED_R_INV) != null
							&& row.get(Constants.PddViewConstants.IS_COLLECTED_R_INV).toString().equalsIgnoreCase("Y")
							&& row.get(Constants.PddViewConstants.IS_COLLECTED_R_IP) != null
							&& row.get(Constants.PddViewConstants.IS_COLLECTED_R_IP).toString().equalsIgnoreCase("Y")
							&& row.get(Constants.PddViewConstants.IS_COLLECTED_R_RCB) != null
							&& row.get(Constants.PddViewConstants.IS_COLLECTED_R_RCB).toString().equalsIgnoreCase("Y"))
					{
						responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
						responseJSON.put(JsonConstants.MESSAGE, "Documents are CORE Updated.");
						responseJSON.put(JsonConstants.DATA, Constants.EMPTY_STRING);
					}
					else
					{

						String appl = row.get(Constants.PddViewConstants.APPL).toString();
						String apac = row.get(Constants.PddViewConstants.APAC).toString();
						String uniqueNo = appl + apac;

						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.AGR_DATE,
								row.get(Constants.PddViewConstants.AGR_DATE) == null ? Constants.EMPTY_STRING : (dateFormatter(row.get(
										Constants.PddViewConstants.AGR_DATE).toString())));
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.AGR_VALUE,
								row.get(Constants.PddViewConstants.AGR_VALUE) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.AGR_VALUE).toString());

						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.APAC,
								row.get(Constants.PddViewConstants.APAC) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.APAC).toString());
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.APAC_STATUS,
								row.get(Constants.PddViewConstants.APAC_STATUS) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.APAC_STATUS).toString());
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.APPL,
								row.get(Constants.PddViewConstants.APPL) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.APPL).toString());
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.ASSET_ID,
								row.get(Constants.PddViewConstants.ASSET_ID) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.ASSET_ID).toString());
						//added by bhushan after confirmation from fevina

						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.ASSET_VALUE,
								row.get(Constants.PddViewConstants.ASSET_VALUE) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.ASSET_VALUE).toString());

						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.BENEFICIARY,
								row.get(Constants.PddViewConstants.BENEFICIARY) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.BENEFICIARY).toString());

						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.CHASIS_NO,
								row.get(Constants.PddViewConstants.CHASIS_NO) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.CHASIS_NO).toString());

						webserviceResponseMap.put(JsonConstants.PddJSONConstants.COLLECTED_DATE_R_CV, row
								.get(Constants.PddViewConstants.COLLECTED_DATE_R_CV) == null ? Constants.EMPTY_STRING : (dateFormatter(row
								.get(Constants.PddViewConstants.COLLECTED_DATE_R_CV).toString())));
						webserviceResponseMap.put(JsonConstants.PddJSONConstants.COLLECTED_DATE_R_INV, row
								.get(Constants.PddViewConstants.COLLECTED_DATE_R_INV) == null ? Constants.EMPTY_STRING : (dateFormatter(row
								.get(Constants.PddViewConstants.COLLECTED_DATE_R_INV).toString())));
						webserviceResponseMap.put(JsonConstants.PddJSONConstants.COLLECTED_DATE_R_IP, row
								.get(Constants.PddViewConstants.COLLECTED_DATE_R_IP) == null ? Constants.EMPTY_STRING : (dateFormatter(row
								.get(Constants.PddViewConstants.COLLECTED_DATE_R_IP).toString())));
						webserviceResponseMap.put(JsonConstants.PddJSONConstants.COLLECTED_DATE_R_RCB, row
								.get(Constants.PddViewConstants.COLLECTED_DATE_R_RCB) == null ? Constants.EMPTY_STRING : (dateFormatter(row
								.get(Constants.PddViewConstants.COLLECTED_DATE_R_RCB).toString())));
						/*	webserviceResponseMap.put(
									JsonConstants.PddJSONConstants.CREATED_DATE,
									row.get(Constants.PddViewConstants.CREATED_DATE) == null ? Constants.EMPTY_STRING : row.get(
											Constants.PddViewConstants.CREATED_DATE).toString());*/
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.DOC_TYPE_LIST_R_CV,
								row.get(Constants.PddViewConstants.DOC_TYPE_LIST_R_CV) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.DOC_TYPE_LIST_R_CV).toString());

						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.DOC_TYPE_LIST_R_INV,
								row.get(Constants.PddViewConstants.DOC_TYPE_LIST_R_INV) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.DOC_TYPE_LIST_R_INV).toString());
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.DOC_TYPE_LIST_R_IP,
								row.get(Constants.PddViewConstants.DOC_TYPE_LIST_R_IP) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.DOC_TYPE_LIST_R_IP).toString());
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.DOC_TYPE_LIST_R_RCB,
								row.get(Constants.PddViewConstants.DOC_TYPE_LIST_R_RCB) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.DOC_TYPE_LIST_R_RCB).toString());

						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.ENGINE_NO,
								row.get(Constants.PddViewConstants.ENGINE_NO) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.ENGINE_NO).toString());

						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.EXPIRY_DATE,
								row.get(Constants.PddViewConstants.EXPIRY_DATE) == null ? Constants.EMPTY_STRING : (dateFormatter(row.get(
										Constants.PddViewConstants.EXPIRY_DATE).toString())));

						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.INSURED_VALUE,
								row.get(Constants.PddViewConstants.INSURED_VALUE) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.INSURED_VALUE).toString());
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.INSURER,
								row.get(Constants.PddViewConstants.INSURER) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.INSURER).toString());
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.INVOICE_DATE,
								row.get(Constants.PddViewConstants.INVOICE_DATE) == null ? Constants.EMPTY_STRING : (dateFormatter(row.get(
										Constants.PddViewConstants.INVOICE_DATE).toString())));
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.INVOICE_NO,
								row.get(Constants.PddViewConstants.INVOICE_NO) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.INVOICE_NO).toString());

						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.INVOICE_VALUE,
								row.get(Constants.PddViewConstants.INVOICE_VALUE) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.INVOICE_VALUE).toString());
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.IS_COLLECTED_R_CV,
								row.get(Constants.PddViewConstants.IS_COLLECTED_R_CV) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.IS_COLLECTED_R_CV).toString());
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.IS_COLLECTED_R_INV,
								row.get(Constants.PddViewConstants.IS_COLLECTED_R_INV) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.IS_COLLECTED_R_INV).toString());
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.IS_COLLECTED_R_IP,
								row.get(Constants.PddViewConstants.IS_COLLECTED_R_IP) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.IS_COLLECTED_R_IP).toString());
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.IS_COLLECTED_R_RCB,
								row.get(Constants.PddViewConstants.IS_COLLECTED_R_RCB) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.IS_COLLECTED_R_RCB).toString());
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.IS_REQUIRED_R_CV,
								row.get(Constants.PddViewConstants.IS_REQUIRED_R_CV) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.IS_REQUIRED_R_CV).toString());
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.IS_REQUIRED_R_INV,
								row.get(Constants.PddViewConstants.IS_REQUIRED_R_INV) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.IS_REQUIRED_R_INV).toString());
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.IS_REQUIRED_R_IP,
								row.get(Constants.PddViewConstants.IS_REQUIRED_R_IP) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.IS_REQUIRED_R_IP).toString());
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.IS_REQUIRED_R_RCB,
								row.get(Constants.PddViewConstants.IS_REQUIRED_R_RCB) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.IS_REQUIRED_R_RCB).toString());
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.LOCATION,
								row.get(Constants.PddViewConstants.LOCATION) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.LOCATION).toString());

						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.MODEL,
								row.get(Constants.PddViewConstants.MODEL) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.MODEL).toString());

						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.MODEL_DESCR,
								row.get(Constants.PddViewConstants.MODEL_DESCR) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.MODEL_DESCR).toString());
						/*webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.MODIFIED_DATE,
								row.get(Constants.PddViewConstants.MODIFIED_DATE) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.MODIFIED_DATE).toString());*/

						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.OLD_ASSET_FLAG,
								row.get(Constants.PddViewConstants.OLD_ASSET_FLAG) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.OLD_ASSET_FLAG).toString());
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.PARTY_ID,
								row.get(Constants.PddViewConstants.PARTY_ID) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.PARTY_ID).toString());
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.PARTY_NAME,
								row.get(Constants.PddViewConstants.PARTY_NAME) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.PARTY_NAME).toString());
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.POLICY_DATE,
								row.get(Constants.PddViewConstants.POLICY_DATE) == null ? Constants.EMPTY_STRING : (dateFormatter(row.get(
										Constants.PddViewConstants.POLICY_DATE).toString())));
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.POLICY_NO,
								row.get(Constants.PddViewConstants.POLICY_NO) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.POLICY_NO).toString());
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.PREMIUM_AMT,
								row.get(Constants.PddViewConstants.PREMIUM_AMT) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.PREMIUM_AMT).toString());

						webserviceResponseMap.put(JsonConstants.PddJSONConstants.PROCESSED_DATE,
								row.get(Constants.PddViewConstants.PROCESSED_DATE) == null ? Constants.EMPTY_STRING : (dateFormatter(row
										.get(Constants.PddViewConstants.PROCESSED_DATE).toString())));
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.PRODUCT,
								row.get(Constants.PddViewConstants.PRODUCT) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.PRODUCT).toString());
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.REG_FLAG,
								row.get(Constants.PddViewConstants.REG_FLAG) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.REG_FLAG).toString());
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.REGISTRATION_NO,
								row.get(Constants.PddViewConstants.REGISTRATION_NO) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.REGISTRATION_NO).toString());
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.RM_PARTY_ID,
								row.get(Constants.PddViewConstants.RM_PARTY_ID) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.RM_PARTY_ID).toString());
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.SALES_TAX_AMOUNT,
								row.get(Constants.PddViewConstants.SALES_TAX_AMOUNT) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.SALES_TAX_AMOUNT).toString());

						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.SALESTAX_STATE,
								row.get(Constants.PddViewConstants.SALESTAX_STATE) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.SALESTAX_STATE).toString());

						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.SALESTAX_STATE_DESCR,
								row.get(Constants.PddViewConstants.SALESTAX_STATE_DESCR) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.SALESTAX_STATE_DESCR).toString());

						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.SRM_PARTY_ID,
								row.get(Constants.PddViewConstants.SRM_PARTY_ID) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.SRM_PARTY_ID).toString());
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.SUB_LOCATION,
								row.get(Constants.PddViewConstants.SUB_LOCATION) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.SUB_LOCATION).toString());
						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.SUPPLIER_DETAILS,
								row.get(Constants.PddViewConstants.SUPPLIER_DETAILS) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.SUPPLIER_DETAILS).toString());

						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.DEALER_PARTY_ID,
								row.get(Constants.PddViewConstants.DEALER_PARTY_ID) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.DEALER_PARTY_ID).toString());

						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.DEALER_PARTY_NAME,
								row.get(Constants.PddViewConstants.DEALER_PARTY_NAME) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.DEALER_PARTY_NAME).toString());

						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.PARTY_ADDRESS,
								row.get(Constants.PddViewConstants.PARTY_ADDRESS) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.PARTY_ADDRESS).toString());

						webserviceResponseMap.put(
								JsonConstants.PddJSONConstants.PARTY_PHONE,
								row.get(Constants.PddViewConstants.PARTY_PHONE) == null ? Constants.EMPTY_STRING : row.get(
										Constants.PddViewConstants.PARTY_PHONE).toString());

						webserviceResponseMap.put(JsonConstants.PddJSONConstants.UNIQUE_NUMBER, uniqueNo);

						/* Code to send PDD Case Status while searching Random Collection*/
						try
						{

							Map<String, Object> row1 = pddService.pddCollectionStatus(uniqueNo,
									systemUser.getUserTableId());

							if (row1.size() > 0)
							{

								webserviceResponseMap.put(
										JsonConstants.PddJSONConstants.INVOICE_STATUS,
										row1.get(Constants.PddViewConstants.INVOICE_STATUS) == null ? "" : row1.get(
												Constants.PddViewConstants.INVOICE_STATUS).toString());

								webserviceResponseMap
										.put(JsonConstants.PddJSONConstants.INSURANCE_STATUS,
												row1.get(Constants.PddViewConstants.INSURANCE_STATUS) == null ? Constants.EMPTY_STRING
														: row1.get(Constants.PddViewConstants.INSURANCE_STATUS)
																.toString());

								webserviceResponseMap.put(JsonConstants.PddJSONConstants.RC_STATUS, row1
										.get(Constants.PddViewConstants.RC_STATUS) == null ? Constants.EMPTY_STRING
										: row1.get(Constants.PddViewConstants.RC_STATUS).toString());

								webserviceResponseMap.put(
										"rmNameInvoice",
										row1.get("RM_NAME_INVOICE") == null ? Constants.EMPTY_STRING : row1.get(
												"RM_NAME_INVOICE").toString());

								webserviceResponseMap.put(
										JsonConstants.PddJSONConstants.RM_NAME,
										row1.get("RM_NAME_INVOICE") == null ? Constants.EMPTY_STRING : row1.get(
												"RM_NAME_INVOICE").toString());

								webserviceResponseMap.put(
										JsonConstants.PddJSONConstants.RM_NAME_INSURANCE,
										row1.get("RM_NAME_INSURANCE") == null ? Constants.EMPTY_STRING : row1.get(
												"RM_NAME_INSURANCE").toString());

								webserviceResponseMap.put(JsonConstants.PddJSONConstants.RM_NAME_RC, row1
										.get("RM_NAME_RC") == null ? Constants.EMPTY_STRING : row1.get("RM_NAME_RC")
										.toString());

								webserviceResponseMap
										.put(JsonConstants.PddJSONConstants.RM_PARTY_ID,
												((row1.get("RM_PARTY_ID_INV") == null) || (row1.get("RM_PARTY_ID_INV")
														.toString().equalsIgnoreCase(Constants.EMPTY_STRING))) ? (row
														.get(Constants.PddViewConstants.RM_PARTY_ID) == null ? Constants.EMPTY_STRING
														: row.get(Constants.PddViewConstants.RM_PARTY_ID).toString())
														: row1.get("RM_PARTY_ID_INV").toString());

								webserviceResponseMap.put(
										JsonConstants.PddJSONConstants.RM_PARTY_ID_RC,
										row1.get("RM_PARTY_ID_RC") == null ? Constants.EMPTY_STRING : row1.get(
												"RM_PARTY_ID_RC").toString());

								webserviceResponseMap.put(
										JsonConstants.PddJSONConstants.RM_PARTY_ID_INSU,
										row1.get("RM_PARTY_ID_INSU") == null ? Constants.EMPTY_STRING : row1.get(
												"RM_PARTY_ID_INSU").toString());

								webserviceResponseMap
										.put(JsonConstants.PddJSONConstants.INSURED_VALUE,
												((row1.get("INSURED_VALUE") == null) || (row1.get("INSURED_VALUE")
														.toString().equalsIgnoreCase(Constants.EMPTY_STRING))) ? (row
														.get(Constants.PddViewConstants.INSURED_VALUE) == null ? Constants.EMPTY_STRING
														: row.get(Constants.PddViewConstants.INSURED_VALUE).toString())
														: row1.get("INSURED_VALUE").toString());

								webserviceResponseMap
										.put(JsonConstants.PddJSONConstants.CHASIS_NO,
												((row1.get("CHASIS_NUMBER") == null) || (row1.get("CHASIS_NUMBER")
														.equals(Constants.EMPTY_STRING))) ? (row
														.get(Constants.PddViewConstants.CHASIS_NO) == null ? Constants.EMPTY_STRING
														: row.get(Constants.PddViewConstants.CHASIS_NO).toString())
														: row1.get("CHASIS_NUMBER").toString());
								webserviceResponseMap
										.put(JsonConstants.PddJSONConstants.ENGINE_NO,
												((row1.get("ENGINE_NUMBER") == null) || (row1.get("ENGINE_NUMBER")
														.toString().equalsIgnoreCase(Constants.EMPTY_STRING))) ? (row
														.get(Constants.PddViewConstants.ENGINE_NO) == null ? Constants.EMPTY_STRING
														: row.get(Constants.PddViewConstants.ENGINE_NO).toString())
														: row1.get("ENGINE_NUMBER").toString());

								webserviceResponseMap
										.put(JsonConstants.PddJSONConstants.EXPIRY_DATE,
												((row1.get("EXPIREY_DATE_INSURENCE") == null) || (row1.get(
														"EXPIREY_DATE_INSURENCE").toString()
														.equalsIgnoreCase(Constants.EMPTY_STRING))) ? (row
														.get(Constants.PddViewConstants.EXPIRY_DATE) == null ? Constants.EMPTY_STRING
														: (dateFormatter(row
																.get(Constants.PddViewConstants.EXPIRY_DATE).toString())))
														: row1.get("EXPIREY_DATE_INSURENCE").toString());

								webserviceResponseMap
										.put(JsonConstants.PddJSONConstants.INVOICE_DATE,
												((row1.get("INVOICE_DATE") == null) || (row1.get("INVOICE_DATE")
														.toString().equalsIgnoreCase(Constants.EMPTY_STRING))) ? (row
														.get(Constants.PddViewConstants.INVOICE_DATE) == null ? Constants.EMPTY_STRING
														: (dateFormatter(row.get(
																Constants.PddViewConstants.INVOICE_DATE).toString())))
														: row1.get("INVOICE_DATE").toString());

								webserviceResponseMap.put(
										JsonConstants.PddJSONConstants.MODEL,
										((row1.get("MODEL") == null) || (row1.get("MODEL").toString()
												.equalsIgnoreCase(Constants.EMPTY_STRING))) ? (row
												.get(Constants.PddViewConstants.MODEL) == null ? Constants.EMPTY_STRING
												: row.get(Constants.PddViewConstants.MODEL).toString()) : row1.get(
												"MODEL").toString());

								webserviceResponseMap
										.put(JsonConstants.PddJSONConstants.INVOICE_VALUE,
												((row1.get("INVOICE_VALUE") == null) || (row1.get("INVOICE_VALUE")
														.equals(Constants.EMPTY_STRING))) ? (row
														.get(Constants.PddViewConstants.INVOICE_VALUE) == null ? Constants.EMPTY_STRING
														: row.get(Constants.PddViewConstants.INVOICE_VALUE).toString())
														: row1.get("INVOICE_VALUE").toString());

								webserviceResponseMap.put(
										JsonConstants.PddJSONConstants.RECEIVED_RC,
										row1.get("RECEIVED_RC") == null ? Constants.EMPTY_STRING : row1.get(
												"RECEIVED_RC").toString());

								webserviceResponseMap
										.put(JsonConstants.PddJSONConstants.REGISTRATION_NO,
												((row1.get("REGISTRATION_NO") == null) || (row1.get("REGISTRATION_NO")
														.equals(Constants.EMPTY_STRING))) ? (row
														.get(Constants.PddViewConstants.REGISTRATION_NO) == null ? Constants.EMPTY_STRING
														: row.get(Constants.PddViewConstants.REGISTRATION_NO)
																.toString())
														: row1.get("REGISTRATION_NO").toString());

								webserviceResponseMap
										.put(JsonConstants.PddJSONConstants.INVOICE_NO,
												((row1.get("INVOICE_NUMBER") == null) || (row1.get("INVOICE_NUMBER")
														.toString().equalsIgnoreCase(Constants.EMPTY_STRING))) ? (row
														.get(Constants.PddViewConstants.INVOICE_NO) == null ? Constants.EMPTY_STRING
														: row.get(Constants.PddViewConstants.INVOICE_NO).toString())
														: row1.get("INVOICE_NUMBER").toString());

								webserviceResponseMap.put(
										JsonConstants.PddJSONConstants.ASSIGNED_TO_RC,
										row1.get("ASSIGNED_TO_RC") == null ? Constants.EMPTY_STRING : row1.get(
												"ASSIGNED_TO_RC").toString());

								webserviceResponseMap.put(
										JsonConstants.PddJSONConstants.ASSIGNED_TO_INSURANCE,
										row1.get("ASSIGNED_TO_INSURANCE") == null ? Constants.EMPTY_STRING : row1.get(
												"ASSIGNED_TO_INSURANCE").toString());

								webserviceResponseMap.put(
										JsonConstants.PddJSONConstants.ASSIGNED_TO_INVOICE,
										row1.get("ASSIGNED_TO_INVOICE") == null ? Constants.EMPTY_STRING : row1.get(
												"ASSIGNED_TO_INVOICE").toString());

								webserviceResponseMap.put(
										JsonConstants.PddJSONConstants.ASSIGNED_BY_INVOICE,
										row1.get("ASSIGNED_BY_INVOICE") == null ? Constants.EMPTY_STRING : row1.get(
												"ASSIGNED_BY_INVOICE").toString());

								webserviceResponseMap.put(
										JsonConstants.PddJSONConstants.ASSIGNED_BY_INSURANCE,
										row1.get("ASSIGNED_BY_INSURANCE") == null ? Constants.EMPTY_STRING : row1.get(
												"ASSIGNED_BY_INSURANCE").toString());

								webserviceResponseMap.put(
										JsonConstants.PddJSONConstants.ASSIGNED_BY_RC,
										row1.get("ASSIGNED_BY_RC") == null ? Constants.EMPTY_STRING : row1.get(
												"ASSIGNED_BY_RC").toString());

								// add by bhushan
								webserviceResponseMap.put(
										"assignedToInvoice",
										row1.get("ASSIGNED_TO_INVOICE") == null ? Constants.EMPTY_STRING : row1.get(
												"ASSIGNED_TO_INVOICE").toString());
								webserviceResponseMap.put(
										"assignedToRc",
										row1.get("ASSIGNED_TO_RC") == null ? Constants.EMPTY_STRING : row1.get(
												"ASSIGNED_TO_RC").toString());
								webserviceResponseMap.put(
										"assignedToInsurance",
										row1.get("ASSIGNED_TO_INSURANCE") == null ? Constants.EMPTY_STRING : row1.get(
												"ASSIGNED_TO_INSURANCE").toString());
								webserviceResponseMap.put(
										"assignedByInvoice",
										row1.get("ASSIGNED_BY_INVOICE") == null ? Constants.EMPTY_STRING : row1.get(
												"ASSIGNED_BY_INVOICE").toString());
								webserviceResponseMap.put(
										"assignedByRc",
										row1.get("ASSIGNED_BY_RC") == null ? Constants.EMPTY_STRING : row1.get(
												"ASSIGNED_BY_RC").toString());
								webserviceResponseMap.put(
										"assignedByInsurance",
										row1.get("ASSIGNED_BY_INSURANCE") == null ? Constants.EMPTY_STRING : row1.get(
												"ASSIGNED_BY_INSURANCE").toString());

								webserviceResponseMap.put(
										"isInvoiceFreeze",
										row1.get("isInvoicefreeze") == null ? Constants.EMPTY_STRING : row1.get(
												"isInvoicefreeze").toString());
								webserviceResponseMap.put("isRcFreeze",
										row1.get("isRcfreeze") == null ? Constants.EMPTY_STRING : row1
												.get("isRcfreeze").toString());
								webserviceResponseMap.put(
										"isInsuranceFreeze",
										row1.get("isInsurancefreeze") == null ? Constants.EMPTY_STRING : row1.get(
												"isInsurancefreeze").toString());

								if (row1.get("RECEIPT_NUMBER") != null && row1.get("PDD_COLL_ID") != null)
								{

									webserviceResponseMap.put(
											JsonConstants.PddJSONConstants.RECEIPT_NUMBER,
											row1.get("RECEIPT_NUMBER") == null ? Constants.EMPTY_STRING : row1.get(
													"RECEIPT_NUMBER").toString());

									webserviceResponseMap.put(
											JsonConstants.PddJSONConstants.PDD_COLL_ID,
											row1.get("PDD_COLL_ID") == null ? Constants.EMPTY_STRING : row1.get(
													"PDD_COLL_ID").toString());

								}

								if (row1.get("ASSIGNED_BY_INSURANCE") != null
										&& row1.get("ASSIGNED_BY_INSURANCE").toString().equalsIgnoreCase("RCU"))
								{
									webserviceResponseMap.put(JsonConstants.PddJSONConstants.QUERIES_INSURENCE,
											row1.get("RCU_QUERIES_INSURENCE") == null ? Constants.EMPTY_STRING : row1
													.get("RCU_QUERIES_INSURENCE").toString());

									webserviceResponseMap.put(JsonConstants.PddJSONConstants.VERIFIED_BY_INSURANCE,
											row1.get("RCU_VERIFIED_BY_INSURANCE") == null ? Constants.EMPTY_STRING
													: row1.get("RCU_VERIFIED_BY_INSURANCE").toString());

								}
								else
								{
									webserviceResponseMap.put(JsonConstants.PddJSONConstants.QUERIES_INSURENCE,
											row1.get("OPS_QUERIES_INSURENCE") == null ? Constants.EMPTY_STRING : row1
													.get("OPS_QUERIES_INSURENCE").toString());

									webserviceResponseMap.put(JsonConstants.PddJSONConstants.VERIFIED_BY_INSURANCE,
											row1.get("OPS_VERIFIED_BY_INSURANCE") == null ? Constants.EMPTY_STRING
													: row1.get("OPS_VERIFIED_BY_INSURANCE").toString());
								}

								if (row1.get("ASSIGNED_BY_RC") != null
										&& row1.get("ASSIGNED_BY_RC").toString().equalsIgnoreCase("RCU"))
								{
									webserviceResponseMap.put(
											JsonConstants.PddJSONConstants.QUERIES_RC,
											row1.get("RCU_QUERIES_RC") == null ? Constants.EMPTY_STRING : row1.get(
													"RCU_QUERIES_RC").toString());

									webserviceResponseMap.put(
											JsonConstants.PddJSONConstants.VERIFIED_BY_RC,
											row1.get("RCU_VERIFIED_BY_RC") == null ? Constants.EMPTY_STRING : row1.get(
													"RCU_VERIFIED_BY_RC").toString());
								}
								else
								{
									webserviceResponseMap.put(
											JsonConstants.PddJSONConstants.QUERIES_RC,
											row1.get("OPS_QUERIES_RC") == null ? Constants.EMPTY_STRING : row1.get(
													"OPS_QUERIES_RC").toString());

									webserviceResponseMap.put(
											JsonConstants.PddJSONConstants.VERIFIED_BY_RC,
											row1.get("OPS_VERIFIED_BY_RC") == null ? Constants.EMPTY_STRING : row1.get(
													"OPS_VERIFIED_BY_RC").toString());
								}

								if (row1.get("ASSIGNED_BY_INVOICE") != null
										&& row1.get("ASSIGNED_BY_INVOICE").toString().equalsIgnoreCase("RCU"))
								{
									webserviceResponseMap.put(JsonConstants.PddJSONConstants.QUERIES_INVOICE,
											row1.get("RCU_QUERIES_INVOICE") == null ? Constants.EMPTY_STRING : row1
													.get("RCU_QUERIES_INVOICE").toString());

									webserviceResponseMap.put(JsonConstants.PddJSONConstants.VERIFIED_BY_INVOICE, row1
											.get("RCU_VERIFIED_BY_INVOICE") == null ? Constants.EMPTY_STRING : row1
											.get("RCU_VERIFIED_BY_INVOICE").toString());
								}
								else
								{
									webserviceResponseMap.put(JsonConstants.PddJSONConstants.QUERIES_INVOICE,
											row1.get("OPS_QUERIES_INVOICE") == null ? Constants.EMPTY_STRING : row1
													.get("OPS_QUERIES_INVOICE").toString());

									webserviceResponseMap.put(JsonConstants.PddJSONConstants.VERIFIED_BY_INVOICE, row1
											.get("OPS_VERIFIED_BY_INVOICE") == null ? Constants.EMPTY_STRING : row1
											.get("OPS_VERIFIED_BY_INVOICE").toString());
								}

								webserviceResponseMap.put(
										JsonConstants.PddJSONConstants.DEALER_PARTY_ID,
										row1.get("DEALER_PARTY_ID") == null ? Constants.EMPTY_STRING : row1.get(
												"DEALER_PARTY_ID").toString());

								webserviceResponseMap.put(
										JsonConstants.PddJSONConstants.DEALER_PARTY_NAME,
										row1.get("DEALER_PARTY_NAME") == null ? Constants.EMPTY_STRING : row1.get(
												"DEALER_PARTY_NAME").toString());

							/*	webserviceResponseMap.put(
										JsonConstants.PddJSONConstants.INVOICE_DATE,
										row1.get("INVOICE_DATE") == null ? Constants.EMPTY_STRING : row1.get(
												"INVOICE_DATE").toString());*/

								/*webserviceResponseMap.put(JsonConstants.PddJSONConstants.MODEL,
										row1.get("MODEL") == null ? Constants.EMPTY_STRING : row1.get("MODEL")
												.toString());*/

								webserviceResponseMap
										.put(JsonConstants.PddJSONConstants.MODEL_DESCR,
												((row1.get("MODEL_DESCR") == null) || (row1.get("MODEL_DESCR")
														.toString().equalsIgnoreCase(Constants.EMPTY_STRING))) ? (row
														.get(Constants.PddViewConstants.MODEL_DESCR) == null ? Constants.EMPTY_STRING
														: row.get(Constants.PddViewConstants.MODEL_DESCR).toString())
														: row1.get("MODEL_DESCR").toString());

								webserviceResponseMap
										.put(JsonConstants.PddJSONConstants.POLICY_NO,
												((row1.get("POLICY_NUMBER") == null) || (row1.get("POLICY_NUMBER")
														.toString().equalsIgnoreCase(Constants.EMPTY_STRING))) ? (row
														.get(Constants.PddViewConstants.POLICY_NO) == null ? Constants.EMPTY_STRING
														: row.get(Constants.PddViewConstants.POLICY_NO).toString())
														: row1.get("POLICY_NUMBER").toString());

								webserviceResponseMap
								.put(JsonConstants.PddJSONConstants.POLICY_DATE,
								((row1.get("POLICY_DATE") == null) || (row1.get("POLICY_DATE")
								.toString().equalsIgnoreCase(Constants.EMPTY_STRING))) ? (row
								.get(Constants.PddViewConstants.POLICY_DATE) == null ? Constants.EMPTY_STRING
								: (dateFormatter(row.get(Constants.PddViewConstants.POLICY_DATE).toString())))
								: row1.get("POLICY_DATE").toString());
								
								
								
								/*webserviceResponseMap.put(
										JsonConstants.PddJSONConstants.EXPIRY_DATE,
										row1.get("EXPIREY_DATE_INSURENCE") == null ? Constants.EMPTY_STRING : row1.get(
												"EXPIREY_DATE_INSURENCE").toString());*/

								/*	webserviceResponseMap.put(
											JsonConstants.PddJSONConstants.INSURED_VALUE,
											row1.get("INSURED_VALUE") == null ? Constants.EMPTY_STRING : row1.get(
													"INSURED_VALUE").toString());*/

								webserviceResponseMap.put(JsonConstants.PddJSONConstants.FORM_B,
										row1.get("FORM_B") == null ? Constants.EMPTY_STRING : row1.get("FORM_B")
												.toString());

								// till here

							}
							/* */
							/*	End */

						}
						catch (Exception e)
						{

							log.error("---- Exception Detail ---", e);

						}

						response = MapToJSON.convertMapToJSON(webserviceResponseMap);
						log.info("response :------------->" + response.toString());

						JSONArray responseData = new JSONArray();
						responseData.put(response);
						responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
						responseJSON.put(JsonConstants.MESSAGE, "Data Found");
						responseJSON.put(JsonConstants.DATA, responseData);

						UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
								(ActivityLoggerConstants.STATUS_SUCCESS), userActivityService);
						new Thread(userActivityStatusUpdate).run();
					}

				}
				else if (rows != null && rows.isEmpty())
				{

					log.info("--- rows.isEmpty() = true ---");
					responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
					responseJSON.put(JsonConstants.MESSAGE, "Data Not Found");
					responseJSON.put(JsonConstants.DATA, Constants.EMPTY_STRING);
				}
				else
				{
					log.info("---MESSAGE :::  PDD_SEARCH_FAILURE --- ");
					responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
					responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_PDD_COLLECTION_SEARCH_FAILURE);
					responseJSON.put(JsonConstants.DATA, Constants.EMPTY_STRING);

					UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
							(ActivityLoggerConstants.STATUS_FAILURE), userActivityService);
					new Thread(userActivityStatusUpdate).run();
				}
			}
			catch (Exception e)
			{
				log.error("--- Exception In searchPddCollections Method Inner Catch --- " + e);
				responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
				responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_PDD_COLLECTION_SEARCH_FAILURE);
				responseJSON.put(JsonConstants.DATA, Constants.EMPTY_STRING);
				UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
						(ActivityLoggerConstants.STATUS_FAILURE), userActivityService);
				new Thread(userActivityStatusUpdate).run();
			}

		}
		catch (Exception e)
		{
			log.error("--- Exception In searchPddCollections Method Outer Catch --- " + e);

			UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
					(ActivityLoggerConstants.STATUS_ERROR), userActivityService);
			new Thread(userActivityStatusUpdate).run();
		}
	}

	private void submitPddCollection(JSONObject responseJSON, String requestSet, SystemUser systemUser,
			String requestEntity, UserActivity userActivity, CommunicationActivityService communicationActivityService)
			throws JSONException
	{
		log.info("---- In PddCollectionsService / submitPddCollection Methode ----");
		Map<String, Object> pddSubmissionMap = null;
		try
		{
			pddSubmissionMap = (Map<String, Object>) Utilities.createMapFromJSON(requestSet);
			log.info("pddSubmissionMap :: " + pddSubmissionMap.toString());
		}
		catch (IOException e)
		{
			log.error("Exception  :: " + e);
			e.printStackTrace();
		}

		Map<String, String> pddDataMap = (Map<String, String>) pddSubmissionMap.get("data");
		Map<String, String> userMap = (Map<String, String>) pddSubmissionMap.get("user");

		UserActivityStatusUpdate userActivityStatusUpdate = null;
		String pddCollectionId = null;

		if (checkData(pddDataMap))
		{
			responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
			responseJSON.put(JsonConstants.MESSAGE, "Invalid Data!!");
			responseJSON.put(JsonConstants.DATA, Constants.EMPTY_STRING);
			userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
					(ActivityLoggerConstants.STATUS_FAILURE), userActivityService);

			new Thread(userActivityStatusUpdate).run();
		}
		else
		{
			if (pddDataMap.get("submitFrom") != null
					&& pddDataMap.get("submitFrom").toString().equalsIgnoreCase("pending")
					&& pddDataMap.get("module").toString().equalsIgnoreCase("Invoice"))//first time submission must be Invoice
			{
				log.info("--- Pending ---> Invoice ---");

				log.info("--- Pending First Submission---> Invoice ---");
				JSONObject responseJson = pddService.checkForDuplicateJson(pddDataMap);
				if (responseJson == null)
				{
					pddCollectionId = pddDataMap.get(JsonConstants.PddJSONConstants.PDD_COLL_ID);
					JSONObject data = new JSONObject();
					responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
					responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_GENERAL_FAILURE);
					data.put("pddCollId", pddCollectionId);
					responseJSON.put(JsonConstants.DATA, data);
					userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
							(ActivityLoggerConstants.STATUS_FAILURE), userActivityService);

					new Thread(userActivityStatusUpdate).run();
				}
				else
				{
					if (responseJson.has(JsonConstants.PddJSONConstants.PDD_COLL_ID))
					{
						log.info("--- Json Duplicated!!! --- ");
						/*JSONObject data = new JSONObject();
						data.put("pddCollId", row.get("PDD_COLL_ID"));
						data.put("receiptNumber", row.get("RECEIPT_NUMBER"));
						
						data.put("chasisNo", row.get("CHASIS_NUMBER"));
						data.put("engineNo", row.get("ENGINE_NUMBER"));*/

						// need to update modified_on because on next sync duplicate collection should come on device
						/*Map<String, Object> parameterMap = new HashMap<String, Object>();
						parameterMap.put("pddCollId",  row.get("PDD_COLL_ID"));
						pddService.updateCollection(parameterMap);*/

						// Commented above code because we are sending data in submission's response it self 

						responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
						responseJSON.put(JsonConstants.MESSAGE, "Json Duplicated!!!");
						responseJSON.put(JsonConstants.DATA, responseJson);
						userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
								(ActivityLoggerConstants.STATUS_IGNORE), userActivityService);

						new Thread(userActivityStatusUpdate).run();
					}
					else
					{
						long pddCollId = pddService.submitPddCollection(pddSubmissionMap);

						log.info("-------pddCollId--------" + pddCollId);

						if (pddCollId != -1)
						{
							try
							{
								if (pddDataMap.get("isCollectedRInv").equalsIgnoreCase("N"))
								{
									//Send mail for invoice to RCU Only
									Map<String, Object> pMap = new HashMap<String, Object>();
									pMap.put("roleId", "36");//36 for rcu from role table
									pMap.put("location", pddDataMap.get("location"));

									List<String> emailList = pddService.getUserDetailsForPddEmail(pMap);

									if (emailList != null && !emailList.isEmpty())
									{
										Map<String, Object> emailDataMap = new HashMap<String, Object>();
										emailDataMap.put("toEmailAddress", emailList);
										StringBuilder subject = new StringBuilder();
										subject.append("PDD :Apac No :");
										subject.append(pddDataMap.get("appl") + pddDataMap.get("apac"));
										subject.append(" RM " + userMap.get("username") + "-"
												+ userMap.get("firstLastName"));
										emailDataMap.put("fromEmailAddress", "kmblPDD.alert@kotak.com");
										emailDataMap.put("subject", subject);

										createEmailFormat(pddSubmissionMap, emailDataMap);

										//sendEmail
										EmailUtilitiesPdd utilitiesPdd = new EmailUtilitiesPdd(emailDataMap, mailSender);
										new Thread(utilitiesPdd).start();
									}
									else
									{
										log.info("--- Email id not available : emailList ---> " + emailList);
									}
								}
							}
							catch (Exception e)
							{
								log.error("Exception :: ", e);
							}

							if ((pddDataMap.get("isCollectedRIp") != null && pddDataMap.get("isCollectedRRcb") != null)
									&& (pddDataMap.get("isCollectedRIp").equalsIgnoreCase("Y") || pddDataMap.get(
											"isCollectedRRcb").equalsIgnoreCase("Y")))
							{
								Map<String, Object> parameterMap = new HashMap<String, Object>();
								parameterMap.put("appl", pddDataMap.get("appl"));
								parameterMap.put("apac", pddDataMap.get("apac"));
								List<String> uniqueNumberList  = new ArrayList<String>();
								uniqueNumberList.add( pddDataMap.get("appl")+pddDataMap.get("apac"));
								parameterMap.put("uniqueNumberList", uniqueNumberList );

								List<Map> pddDetailsMapList = pddService.getDetailsFromView(parameterMap);
								log.info("pddDetailsMapList :: " + pddDetailsMapList);

								if (pddDetailsMapList != null && !pddDetailsMapList.isEmpty())
								{
									Map<String, Object> pddDetailsMap = pddDetailsMapList.get(0);
									pddDetailsMap.put("pddCollId", pddCollId);
									pddDetailsMap.put("uniqueNo", pddDataMap.get("uniqueNo"));
									pddDetailsMap.put("receiptNumber", pddDataMap.get("receiptNumber"));

									Map<String, Object> pddCollMap = new HashMap<String, Object>();
									pddCollMap.put("user", pddSubmissionMap.get("user"));

									if (pddDetailsMap.get("isCollectedRIp") != null
											&& pddDetailsMap.get("isCollectedRIp").toString().equalsIgnoreCase("Y"))
									{
										pddDetailsMap.put("module", "Insurance");
										pddDetailsMap.put("beneficiary", "KMBL");
										pddDetailsMap.put("assignedToRIp", "CORE");
										pddDetailsMap.put("assignedByRIp", "RM");
										pddDetailsMap.put("receivedInsurence", "Yes");
										pddDetailsMap.put("submitFrom", "Pending");

										pddCollMap.put("data", pddDetailsMap);
										if (pddService.updatePddCollection(pddCollMap))
										{
											log.info("--- PddDetail from view for insurance updated successfully ---");
										}
										else
										{
											log.info("--- Unable to Update PddDetail from view for insurance ---");
										}

									}
									if (pddDetailsMap.get("isCollectedRRcb") != null
											&& pddDetailsMap.get("isCollectedRRcb").toString().equalsIgnoreCase("Y"))
									{

										pddDetailsMap.put("module", "Rc");
										pddDetailsMap.put("assignedToRRcb", "CORE");
										pddDetailsMap.put("assignedByRRcb", "RM");
										pddDetailsMap.put("receivedRc", "Yes");
										pddDetailsMap.put("submitFrom", "Pending");

										pddCollMap.put("data", pddDetailsMap);

										if (pddService.updatePddCollection(pddCollMap))
										{
											log.info("--- PddDetail from view for RC updated successfully ---");
										}
										else
										{
											log.info("--- Unable to Update PddDetail from view for Rc ---");
										}

									}

								}
							}

							JSONObject data = new JSONObject();
							data.put("pddCollId", pddCollId);
							responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
							responseJSON.put(JsonConstants.MESSAGE, "Invoice submitted successfully");
							responseJSON.put(JsonConstants.DATA, data);

							userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
									(ActivityLoggerConstants.STATUS_SUCCESS), userActivityService);

							new Thread(userActivityStatusUpdate).run();

						}
						else
						{
							responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
							responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_GENERAL_FAILURE);
							responseJSON.put(JsonConstants.DATA, Constants.EMPTY_STRING);
							userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
									(ActivityLoggerConstants.STATUS_FAILURE), userActivityService);

							new Thread(userActivityStatusUpdate).run();

						}
					}
				}

			}
			else if (pddDataMap.get("submitFrom") != null
					&& pddDataMap.get("submitFrom").equalsIgnoreCase("pending")
					&& (pddDataMap.get("module").toString().equalsIgnoreCase("Rc") || pddDataMap.get("module")
							.toString().equalsIgnoreCase("Insurance")))
			{
				log.info("--- Pending ---> Insurance Or Rc ---");

				if (pddDataMap.get("module").toString().equalsIgnoreCase("Rc"))
				{

					log.info("--- Pending ---> Rc ---");

					JSONObject responseJson = pddService.checkForDuplicateJson(pddDataMap);
					if (responseJson == null)
					{
						pddCollectionId = pddDataMap.get(JsonConstants.PddJSONConstants.PDD_COLL_ID);
						JSONObject data = new JSONObject();
						responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
						responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_GENERAL_FAILURE);
						data.put("pddCollId", pddCollectionId);
						responseJSON.put(JsonConstants.DATA, data);
						userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
								(ActivityLoggerConstants.STATUS_FAILURE), userActivityService);

						new Thread(userActivityStatusUpdate).run();
					}
					else
					{
						if (responseJson.has(JsonConstants.PddJSONConstants.PDD_COLL_ID))
						{
							log.info("--- Json Duplicated!!! --- ");
							responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
							responseJSON.put(JsonConstants.MESSAGE, "Json Duplicated!!!");
							responseJSON.put(JsonConstants.DATA, responseJson);
							userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
									(ActivityLoggerConstants.STATUS_IGNORE), userActivityService);

							new Thread(userActivityStatusUpdate).run();
						}
						else
						{
							long pddCollId = pddService.submitRcPddCollection(pddSubmissionMap);

							log.info("-------pddCollId--------" + pddCollId);

							if (pddCollId != -1)
							{
								try
								{
									//Send mail to RCU Only
									Map<String, Object> pMap = new HashMap<String, Object>();
									pMap.put("roleId", "36");//36 for rcu from role table
									pMap.put("location", pddDataMap.get("location"));

									List<String> emailList = pddService.getUserDetailsForPddEmail(pMap);
									//List<String> ccEmailAddress = new ArrayList<String>();
									//List<String> bccEmailAddress = new ArrayList<String>();

									if (emailList != null && !emailList.isEmpty())
									{
										Map<String, Object> emailDataMap = new HashMap<String, Object>();
										emailDataMap.put("toEmailAddress", emailList);
										StringBuilder subject = new StringBuilder();
										subject.append("PDD :Apac No :");
										subject.append(pddDataMap.get("appl") + pddDataMap.get("apac"));
										subject.append(" RM " + userMap.get("username") + "-"
												+ userMap.get("firstLastName"));
										//emailDataMap.put("ccEmailAddress",ccEmailAddress);
										//emailDataMap.put("bccEmailAddress", bccEmailAddress);
										emailDataMap.put("fromEmailAddress", "kmblPDD.alert@kotak.com");
										emailDataMap.put("subject", subject);

										createEmailFormat(pddSubmissionMap, emailDataMap);

										//sendEmail
										EmailUtilitiesPdd utilitiesPdd = new EmailUtilitiesPdd(emailDataMap, mailSender);
										new Thread(utilitiesPdd).start();
									}
									else
									{
										log.info("--- Email id not available : emailList ---> " + emailList);
									}

								}
								catch (Exception e)
								{
									log.error("Exception :: ", e);
								}

								if ((pddDataMap.get("isCollectedRIp") != null && pddDataMap.get("isCollectedRInv") != null)
										&& (pddDataMap.get("isCollectedRIp").equalsIgnoreCase("Y") || pddDataMap.get(
												"isCollectedRInv").equalsIgnoreCase("Y")))
								{
									Map<String, Object> parameterMap = new HashMap<String, Object>();
									parameterMap.put("appl", pddDataMap.get("appl"));
									parameterMap.put("apac", pddDataMap.get("apac"));
									List<String> uniqueNumberList  = new ArrayList<String>();
									uniqueNumberList.add( pddDataMap.get("appl")+pddDataMap.get("apac"));
									parameterMap.put("uniqueNumberList", uniqueNumberList );

									List<Map> pddDetailsMapList = pddService.getDetailsFromView(parameterMap);
									log.info("pddDetailsMapList :: " + pddDetailsMapList);

									if (pddDetailsMapList != null && !pddDetailsMapList.isEmpty())
									{
										Map<String, Object> pddDetailsMap = pddDetailsMapList.get(0);
										pddDetailsMap.put("pddCollId", pddCollId);
										pddDetailsMap.put("uniqueNo", pddDataMap.get("uniqueNo"));
										pddDetailsMap.put("receiptNumber", pddDataMap.get("receiptNumber"));

										Map<String, Object> pddCollMap = new HashMap<String, Object>();
										pddCollMap.put("user", pddSubmissionMap.get("user"));

										if (pddDetailsMap.get("isCollectedRIp") != null
												&& pddDetailsMap.get("isCollectedRIp").toString().equalsIgnoreCase("Y"))
										{
											pddDetailsMap.put("module", "Insurance");
											pddDetailsMap.put("beneficiary", "KMBL");
											pddDetailsMap.put("assignedToRIp", "CORE");
											pddDetailsMap.put("assignedByRIp", "RM");
											pddDetailsMap.put("receivedInsurence", "Yes");
											pddDetailsMap.put("submitFrom", "Pending");

											pddCollMap.put("data", pddDetailsMap);
											if (pddService.updatePddCollection(pddCollMap))
											{
												log.info("--- PddDetail from view for insurance updated successfully ---");
											}
											else
											{
												log.info("--- Unable to Update PddDetail from view for insurance ---");
											}

										}
										if (pddDataMap.get("isCollectedRInv") != null
												&& pddDataMap.get("isCollectedRInv").toString().equalsIgnoreCase("Y"))
										{

											pddDetailsMap.put("module", "Invoice");
											pddDetailsMap.put("assignedToRInv", "CORE");
											pddDetailsMap.put("assignedByRInv", "RM");
											pddDetailsMap.put("receivedInvoice", "Yes");
											pddDetailsMap.put("submitFrom", "Pending");

											pddCollMap.put("data", pddDetailsMap);

											if (pddService.updatePddCollection(pddCollMap))
											{
												log.info("--- PddDetail from view for Invoice updated successfully ---");
											}
											else
											{
												log.info("--- Unable to Update PddDetail from view for Invoice ---");
											}

										}

									}
								}

								JSONObject data = new JSONObject();
								data.put("pddCollId", pddCollId);
								responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
								responseJSON.put(JsonConstants.MESSAGE, "RC submitted successfully");
								responseJSON.put(JsonConstants.DATA, data);

								userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
										(ActivityLoggerConstants.STATUS_SUCCESS), userActivityService);

								new Thread(userActivityStatusUpdate).run();

							}
							else
							{
								responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
								responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_GENERAL_FAILURE);
								responseJSON.put(JsonConstants.DATA, Constants.EMPTY_STRING);
								userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
										(ActivityLoggerConstants.STATUS_FAILURE), userActivityService);

								new Thread(userActivityStatusUpdate).run();

							}
						}
					}

				}
				else if (pddDataMap.get("module").toString().equalsIgnoreCase("Insurance"))
				{

					log.info("--- Pending ---> Insurance ---");

					JSONObject responseJson = pddService.checkForDuplicateJson(pddDataMap);
					if (responseJson == null)
					{
						pddCollectionId = pddDataMap.get(JsonConstants.PddJSONConstants.PDD_COLL_ID);
						JSONObject data = new JSONObject();
						responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
						responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_GENERAL_FAILURE);
						data.put("pddCollId", pddCollectionId);
						responseJSON.put(JsonConstants.DATA, data);
						userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
								(ActivityLoggerConstants.STATUS_FAILURE), userActivityService);

						new Thread(userActivityStatusUpdate).run();
					}
					else
					{
						if (responseJson.has(JsonConstants.PddJSONConstants.PDD_COLL_ID))
						{
							log.info("--- Json Duplicated!!! --- ");

							responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
							responseJSON.put(JsonConstants.MESSAGE, "Json Duplicated!!!");
							responseJSON.put(JsonConstants.DATA, responseJson);
							userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
									(ActivityLoggerConstants.STATUS_IGNORE), userActivityService);

							new Thread(userActivityStatusUpdate).run();
						}
						else
						{
							long pddCollId = pddService.submitInsuransePddCollection(pddSubmissionMap);

							log.info("-------pddCollId--------" + pddCollId);

							if (pddCollId != -1)
							{
								try
								{
									//Send mail to RCU Only
									Map<String, Object> pMap = new HashMap<String, Object>();
									pMap.put("roleId", "36");//36 for rcu from role table
									pMap.put("location", pddDataMap.get("location"));

									List<String> emailList = pddService.getUserDetailsForPddEmail(pMap);
									//List<String> ccEmailAddress = new ArrayList<String>();
									//List<String> bccEmailAddress = new ArrayList<String>();

									if (emailList != null && !emailList.isEmpty())
									{
										Map<String, Object> emailDataMap = new HashMap<String, Object>();
										emailDataMap.put("toEmailAddress", emailList);
										StringBuilder subject = new StringBuilder();
										subject.append("PDD :Apac No :");
										subject.append(pddDataMap.get("appl") + pddDataMap.get("apac"));
										subject.append(" RM " + userMap.get("username") + "-"
												+ userMap.get("firstLastName"));
										//emailDataMap.put("ccEmailAddress",ccEmailAddress);
										//emailDataMap.put("bccEmailAddress", bccEmailAddress);
										emailDataMap.put("fromEmailAddress", "kmblPDD.alert@kotak.com");
										emailDataMap.put("subject", subject);

										createEmailFormat(pddSubmissionMap, emailDataMap);

										//sendEmail
										EmailUtilitiesPdd utilitiesPdd = new EmailUtilitiesPdd(emailDataMap, mailSender);
										new Thread(utilitiesPdd).start();
									}
									else
									{
										log.info("--- Email id not available : emailList ---> " + emailList);
									}

								}
								catch (Exception e)
								{
									log.error("Exception :: ", e);
								}

								if ((pddDataMap.get("isCollectedRInv") != null && pddDataMap.get("isCollectedRRcb") != null)
										&& (pddDataMap.get("isCollectedRInv").equalsIgnoreCase("Y") || pddDataMap.get(
												"isCollectedRRcb").equalsIgnoreCase("Y")))
								{
									Map<String, Object> parameterMap = new HashMap<String, Object>();
									parameterMap.put("appl", pddDataMap.get("appl"));
									parameterMap.put("apac", pddDataMap.get("apac"));
									
									List<String> uniqueNumberList  = new ArrayList<String>();
									uniqueNumberList.add( pddDataMap.get("appl")+pddDataMap.get("apac"));
									parameterMap.put("uniqueNumberList", uniqueNumberList );
									

									List<Map> pddDetailsMapList = pddService.getDetailsFromView(parameterMap);
									log.info("pddDetailsMapList :: " + pddDetailsMapList);

									if (pddDetailsMapList != null && !pddDetailsMapList.isEmpty())
									{
										Map<String, Object> pddDetailsMap = pddDetailsMapList.get(0);
										pddDetailsMap.put("pddCollId", pddCollId);
										pddDetailsMap.put("uniqueNo", pddDataMap.get("uniqueNo"));
										pddDetailsMap.put("receiptNumber", pddDataMap.get("receiptNumber"));

										Map<String, Object> pddCollMap = new HashMap<String, Object>();
										pddCollMap.put("user", pddSubmissionMap.get("user"));

										if (pddDataMap.get("isCollectedRInv") != null
												&& pddDataMap.get("isCollectedRInv").toString().equalsIgnoreCase("Y"))
										{
											pddDetailsMap.put("module", "Invoice");
											pddDetailsMap.put("assignedToRInv", "CORE");
											pddDetailsMap.put("assignedByRInv", "RM");
											pddDetailsMap.put("receivedInvoice", "Yes");
											pddDetailsMap.put("submitFrom", "Pending");

											pddCollMap.put("data", pddDetailsMap);
											if (pddService.updatePddCollection(pddCollMap))
											{
												log.info("--- PddDetail from view for Invoice updated successfully ---");
											}
											else
											{
												log.info("--- Unable to Update PddDetail from view for Invoice ---");
											}

										}
										if (pddDetailsMap.get("isCollectedRRcb") != null
												&& pddDetailsMap.get("isCollectedRRcb").toString()
														.equalsIgnoreCase("Y"))
										{

											pddDetailsMap.put("module", "Rc");
											pddDetailsMap.put("assignedToRRcb", "CORE");
											pddDetailsMap.put("assignedByRRcb", "RM");
											pddDetailsMap.put("receivedRc", "Yes");
											pddDetailsMap.put("submitFrom", "Pending");

											pddCollMap.put("data", pddDetailsMap);

											if (pddService.updatePddCollection(pddCollMap))
											{
												log.info("--- PddDetail from view for RC updated successfully ---");
											}
											else
											{
												log.info("--- Unable to Update PddDetail from view for Rc ---");
											}

										}

									}
								}

								JSONObject data = new JSONObject();
								data.put("pddCollId", pddCollId);
								responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
								responseJSON.put(JsonConstants.MESSAGE, "Insurance submitted successfully");
								responseJSON.put(JsonConstants.DATA, data);

								userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
										(ActivityLoggerConstants.STATUS_SUCCESS), userActivityService);

								new Thread(userActivityStatusUpdate).run();

							}
							else
							{
								responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
								responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_GENERAL_FAILURE);
								responseJSON.put(JsonConstants.DATA, Constants.EMPTY_STRING);
								userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
										(ActivityLoggerConstants.STATUS_FAILURE), userActivityService);

								new Thread(userActivityStatusUpdate).run();

							}
						}
					}
				}

			}
			else if (pddDataMap.get("submitFrom") != null
					&& pddDataMap.get("submitFrom").toString().equalsIgnoreCase("status"))//Rejected submission
			{
				log.info("--- Status ---> All ---");
				pddCollectionId = pddDataMap.get(JsonConstants.PddJSONConstants.PDD_COLL_ID);
				boolean updateFlag = pddService.updatePddCollection(pddSubmissionMap);

				log.info("-------updateFlag --------" + updateFlag);

				if (updateFlag != false)
				{

					try
					{
						//Send mail
						Map<String, Object> pMap = new HashMap<String, Object>();
						/*if (pddDataMap.get("module").equalsIgnoreCase("INVOICE"))
						{
							if (pddDataMap.get("assignedToRInv").equalsIgnoreCase("OPS"))
							{
								pMap.put("roleId", "37"); //37 for ops from role table
							}
							else if (pddDataMap.get("assignedToRInv").equalsIgnoreCase("RCU"))
							{
								pMap.put("roleId", "36");//36 for rcu from role table
							}
							
							pMap.put("roleId", "36");//36 for rcu from role table
						}
						else if (pddDataMap.get("module").equalsIgnoreCase("INSURANCE"))
						{
							if (pddDataMap.get("assignedToRIp").equalsIgnoreCase("OPS"))
							{
								pMap.put("roleId", "37"); //37 for ops from role table
							}
							else if (pddDataMap.get("assignedToRIp").equalsIgnoreCase("RCU"))
							{
								pMap.put("roleId", "36");//36 for rcu from role table
							}
							
							pMap.put("roleId", "36");//36 for rcu from role table
						}
						else if (pddDataMap.get("module").equalsIgnoreCase("Rc"))
						{
							if (pddDataMap.get("assignedToRRcb").equalsIgnoreCase("OPS"))
							{
								pMap.put("roleId", "37"); //37 for ops from role table
							}
							else if (pddDataMap.get("assignedToRRcb").equalsIgnoreCase("RCU"))
							{
								pMap.put("roleId", "36");//36 for rcu from role table
							}
						}*/

						pMap.put("location", pddDataMap.get("location"));
						pMap.put("roleId", "36");//36 for rcu from role table

						List<String> emailList = pddService.getUserDetailsForPddEmail(pMap);

						if (emailList != null && !emailList.isEmpty())
						{
							Map<String, Object> emailDataMap = new HashMap<String, Object>();
							emailDataMap.put("toEmailAddress", emailList);
							StringBuilder subject = new StringBuilder();
							subject.append("PDD :Apac No :");
							subject.append(pddDataMap.get("appl") + pddDataMap.get("apac"));
							subject.append(" RM " + userMap.get("username") + "-" + userMap.get("firstLastName"));
							emailDataMap.put("fromEmailAddress", "kmblPDD.alert@kotak.com");
							emailDataMap.put("subject", subject);

							createEmailFormat(pddSubmissionMap, emailDataMap);

							//sendEmail
							EmailUtilitiesPdd utilitiesPdd = new EmailUtilitiesPdd(emailDataMap, mailSender);
							new Thread(utilitiesPdd).start();
						}
						else
						{
							log.info("--- Email id not available : emailList ---> " + emailList);
						}

					}
					catch (Exception e)
					{
						log.error("Exception :: ", e);
					}

					log.info("--- Updated Successfully ---");
					JSONObject data = new JSONObject();
					data.put("pddCollId", pddCollectionId);
					responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
					if (pddDataMap.get("module").equalsIgnoreCase("Invoice"))
						responseJSON.put(JsonConstants.MESSAGE, "Invoice submitted successfully");
					else if (pddDataMap.get("module").equalsIgnoreCase("Insurance"))
						responseJSON.put(JsonConstants.MESSAGE, "Insurance submitted successfully");
					else if (pddDataMap.get("module").equalsIgnoreCase("Rc"))
						responseJSON.put(JsonConstants.MESSAGE, "RC submitted successfully");
					else
						responseJSON.put(JsonConstants.MESSAGE, "Pdd Collection got Updated successfully");
					responseJSON.put(JsonConstants.DATA, data);

					userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
							(ActivityLoggerConstants.STATUS_SUCCESS), userActivityService);

					new Thread(userActivityStatusUpdate).run();

				}
				else
				{
					JSONObject data = new JSONObject();
					responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
					responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_GENERAL_FAILURE);
					data.put("pddCollId", pddCollectionId);
					responseJSON.put(JsonConstants.DATA, data);
					userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
							(ActivityLoggerConstants.STATUS_FAILURE), userActivityService);

					new Thread(userActivityStatusUpdate).run();

				}

			}
			else
			{
				log.info("--- Invalid Request ---");
				JSONObject data = new JSONObject();
				responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
				responseJSON.put(JsonConstants.MESSAGE, "Invalid Request");
				data.put("pddCollId", pddCollectionId);
				responseJSON.put(JsonConstants.DATA, data);
				userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
						(ActivityLoggerConstants.STATUS_FAILURE), userActivityService);

				new Thread(userActivityStatusUpdate).run();
			}
		}

	}

	private boolean checkData(Map<String, String> pddDataMap)
	{
		boolean flag = false;
		try
		{
			log.info("---- Inside checkData ---- ");

			if (pddDataMap.get("isCollectedRIp") == null || pddDataMap.get("isCollectedRIp").equalsIgnoreCase(Constants.EMPTY_STRING))
			{
				flag = true;
			}
			else if (pddDataMap.get("isCollectedRRcb") == null
					|| pddDataMap.get("isCollectedRRcb").equalsIgnoreCase(Constants.EMPTY_STRING))
			{
				flag = true;
			}
			else if (pddDataMap.get("isCollectedRInv") == null
					|| pddDataMap.get("isCollectedRInv").equalsIgnoreCase(Constants.EMPTY_STRING))
			{
				flag = true;
			}
			else if (pddDataMap.get("receiptNumber") == null || pddDataMap.get("receiptNumber").equalsIgnoreCase(Constants.EMPTY_STRING))
			{
				flag = true;
			}
			else
			{
				log.info("---- In else part of checkData methode ----");
			}

			/*
			if(pddDataMap.get(Constants.EMPTY_STRING) == null || pddDataMap.get(Constants.EMPTY_STRING).equalsIgnoreCase(Constants.EMPTY_STRING))
			{
				flag = true ;
			}*/
		}
		catch (Exception e)
		{
			log.error("Exception :: " + e);
			e.printStackTrace();
			return flag;
		}

		return flag;
	}

	public String dateFormatter(String stringDate)
	{
		try
		{
			SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");

			Date date = dateFormat1.parse(stringDate);
			SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd-MMM-yyyy");
			return dateFormat2.format(date);
		}
		catch (Exception e)
		{
			this.log.error("stringDate :: " + stringDate);
			this.log.error("Exception :: " + e);
		}
		return Constants.EMPTY_STRING;
	}

	private void createEmailFormat(Map<String, Object> pddSubmissionMap, Map<String, Object> emailDataMap)
	{
		log.info("--- in createEmailFormat Method ---");
		StringBuilder message = new StringBuilder();
		Map<String, String> pddDataMap = (Map<String, String>) pddSubmissionMap.get("data");
		Map<String, String> userMap = (Map<String, String>) pddSubmissionMap.get("user");

		String document = pddDataMap.get("module") == null ? Constants.EMPTY_STRING : pddDataMap.get("module");
		String module = Constants.EMPTY_STRING;
		if (document.equalsIgnoreCase("RC"))
		{
			module = "RC";
		}
		else
		{
			module = pddDataMap.get("module");
		}

		message.append("<html>");
		message.append("<body>");

		message.append("Dear Sir/Madam,");
		message.append("<br>");
		message.append("<br>");
		message.append("Please find below proposed APAC With Following document details for verification. ");
		message.append("Kindly verify the same on portal.");
		message.append("<br>");
		message.append("<br>");
		message.append("<b>PDD Details :</b>");
		message.append("<br>");
		message.append("Apac No :" + pddDataMap.get("appl") + pddDataMap.get("apac"));
		message.append("<br>");
		message.append("<br>");
		message.append("Document Details : " + module);
		message.append("<br>");
		message.append("<br>");
		message.append("Submitted by :RM " + userMap.get("username") + "- " + userMap.get("firstLastName") + " on "
				+ Utilities.dateFormatter(new Timestamp(System.currentTimeMillis())) + ".");//Submitted by :RM KMBL1234- Vikas Kene on 15-05- 17.
		message.append("<br>");
		//message.append(bean.getApacNo().equalsIgnoreCase(Constants.EMPTY_STRING) ? bean.getCardNumber() : bean.getApacNo());
		message.append("<br>");
		message.append("<table border=\"2px;\" style=\"border-collapse: collapse;\">");
		message.append("<tr>");
		/*if (pddDataMap.get("module").equalsIgnoreCase("Invoice"))
		{
			message.append("<td colspan=\"2\"> <b>Invoice Details:</b> </td>");
			message.append("<tr>");
			
			message.append("<td>Dealer : " + pddDataMap.get("supplierDetails") + "</td>");
			message.append("<td>Invoice no : " + pddDataMap.get("invoiceNo") + "</td>");
			
			message.append("</tr>");
			
			message.append("<tr>");
			
			message.append("<td>Invoice Date : " + pddDataMap.get("invoiceDate") + "</td>");
			message.append("<td>Model : " + pddDataMap.get("model") + "</td>");
			
			message.append("</tr>");
			
			message.append("<tr>");
			
			message.append("<td>Engine No : " + pddDataMap.get("engineNo") + "</td>");
			message.append("<td>Chassis No : " + pddDataMap.get("chasisNo") + "</td>");
			
			message.append("</tr>");
			
			message.append("<tr>");
			
			message.append("<td>Invoice Value : " + pddDataMap.get("invoiceValue") + "</td>");
			message.append("<td>Sales Tax Amount : " + pddDataMap.get("salesTaxAmount") + "</td>");
			
			message.append("</tr>");
			
			message.append("<tr>");
			
			message.append("<td>Sales Tax From State : " + pddDataMap.get("salestaxState") + "</td>");
			message.append("<td>Received Invoice : " + pddDataMap.get("receivedInvoice") + "</td>");
			
			message.append("</tr>");
			
			message.append("<tr>");
			
			message.append("<td>Original Invoice : " + pddDataMap.get("originalInvoice") + "</td>");
			message.append("<td>No. of Images : " + pddDataMap.get("imageCount") + "</td>");
			
			message.append("</tr>");
		}
		else if (pddDataMap.get("module").equalsIgnoreCase("RC"))
		{
			message.append("<td colspan=\"2\"><b>RC Details:</b> </td>");
			

			message.append("<tr>");
			
			message.append("<td>Registration No : " + pddDataMap.get("registrationNo") + "</td>");
			message.append("<td>Chassis No : " + pddDataMap.get("chasisNo") + "</td>");
			
			message.append("</tr>");
			
			message.append("<tr>");
			
			message.append("<td>Engine No : " + pddDataMap.get("engineNo") + "</td>");
			message.append("<td>Form B : " + pddDataMap.get("formB") + "</td>");
			
			message.append("</tr>");
			
			message.append("<tr>");
			
			message.append("<td>Received RC : " + pddDataMap.get("receivedRc") + "</td>");
			message.append("<td>No. of Images : " + pddDataMap.get("imageCount") + "</td>");
			
			message.append("</tr>");
			
			
		}
		else if (pddDataMap.get("module").equalsIgnoreCase("Insurance"))
		{
			message.append("<td colspan=\"2\"><b>Insurance Details:</b> </td>");
			
			message.append("<tr>");
			
			message.append("<td>Policy No : " + pddDataMap.get("policyNo") + "</td>");
			message.append("<td>Insurance Received : " + pddDataMap.get("receivedInsurance") + "</td>");
			
			message.append("</tr>");
			
			message.append("<tr>");
			
			message.append("<td>Policy Date : " + pddDataMap.get("policyDate") + "</td>");
			message.append("<td>Expiry Date : " + pddDataMap.get("expiryDate") + "</td>");
			
			message.append("</tr>");
			
			message.append("<tr>");
			
			message.append("<td>Insured Value : " + pddDataMap.get("insuredValue") + "</td>");
			message.append("<td>Premium Amount : " + pddDataMap.get("premiumAmt") + "</td>");
			
			message.append("</tr>");
			
			message.append("<tr>");
			
			message.append("<td>Benefiary : " + pddDataMap.get("beneficiary") + "</td>");
			message.append("<td>Insurer : " + pddDataMap.get("insurer") + "</td>");
			
			message.append("</tr>");
			
			message.append("<tr>");
			
			message.append("<td>Engine No : " + pddDataMap.get("engineNo") + "</td>");
			message.append("<td>Chassis No : " + pddDataMap.get("chasisNo") + "</td>");
			
			message.append("</tr>");
			
			message.append("<tr>");
			
			message.append("<td>No. of Images : " + pddDataMap.get("imageCount") + "</td>");
			message.append("<td></td>");
			
			message.append("</tr>");
		}*/

		if (pddDataMap.get("module").equalsIgnoreCase("Invoice"))
		{
			message.append("<td colspan=\"4\"> <b>Invoice Details:</b> </td>");

			message.append("<tr>");
			message.append("<td>Invoice Received : </td>");
			message.append("<td>" + pddDataMap.get("receivedInvoice") + "</td>");
			message.append("<td>Dealer : </td>");
			message.append("<td>" + pddDataMap.get("supplierDetails") + "</td>");
			message.append("</tr>");

			message.append("<tr>");
			message.append("<td>Invoice no : </td>");
			message.append("<td>" + pddDataMap.get("invoiceNo") + "</td>");
			message.append("<td>Invoice Date : </td>");
			message.append("<td>" + pddDataMap.get("invoiceDate") + "</td>");
			message.append("</tr>");

			message.append("<tr>");
			message.append("<td>Model : </td>");
			message.append("<td>" + pddDataMap.get("model") + "</td>");
			message.append("<td>Chassis No : </td>");
			message.append("<td>" + pddDataMap.get("chasisNo") + "</td>");
			message.append("</tr>");

			message.append("<tr>");
			message.append("<td>Engine No : </td>");
			message.append("<td>" + pddDataMap.get("engineNo") + "</td>");
			message.append("<td>Invoice Value (Incl. all tax) : </td>");
			message.append("<td>" + pddDataMap.get("invoiceValue") + "</td>");
			message.append("</tr>");

			/*message.append("<tr>");
			message.append("<td>Sales Tax Amount : </td>");
			message.append("<td>" + pddDataMap.get("salesTaxAmount") + "</td>");
			message.append("</tr>");*/

			/*message.append("<tr>");
			message.append("<td>Sales Tax From State : </td>");
			message.append("<td>" + pddDataMap.get("salestaxState") + "</td>");
			message.append("<td>Received Invoice : </td>");
			message.append("<td>" + pddDataMap.get("receivedInvoice") + "</td>");
			message.append("</tr>");*/

			message.append("<tr>");
			/*message.append("<td>Original Invoice : </td>");
			message.append("<td>" + pddDataMap.get("originalInvoice") + "</td>");*/
			message.append("<td>No. of Images : </td>");
			message.append("<td>" + pddDataMap.get("imageCount") + "</td>");
			message.append("</tr>");
		}
		else if (pddDataMap.get("module").equalsIgnoreCase("RC"))
		{
			message.append("<td colspan=\"4\"><b>Registration Certificate Details:</b> </td>");

			message.append("<tr>");
			message.append("<td>RC Received : </td>");
			message.append("<td>" + pddDataMap.get("receivedRc") + "</td>");
			message.append("<td>FOP Received : </td>");
			message.append("<td>" + pddDataMap.get("formB") + "</td>");
			message.append("</tr>");

			message.append("<tr>");
			message.append("<td>REG No : </td>");
			message.append("<td>" + pddDataMap.get("registrationNo") + "</td>");
			message.append("<td>Chassis No : </td>");
			message.append("<td>" + pddDataMap.get("chasisNo") + "</td>");
			message.append("</tr>");

			message.append("<tr>");
			message.append("<td>Engine No : </td>");
			message.append("<td>" + pddDataMap.get("engineNo") + "</td>");
			message.append("<td>No. of Images : </td>");
			message.append("<td>" + pddDataMap.get("imageCount") + "</td>");
			message.append("</tr>");

		}
		else if (pddDataMap.get("module").equalsIgnoreCase("Insurance"))
		{
			message.append("<td colspan=\"4\"><b>Insurance Details:</b> </td>");

			message.append("<tr>");
			message.append("<td>Insurance Received : </td>");
			message.append("<td>" + pddDataMap.get("receivedInsurance") + "</td>");
			message.append("<td>Policy No : </td>");
			message.append("<td>" + pddDataMap.get("policyNo") + "</td>");
			message.append("</tr>");

			message.append("<tr>");
			message.append("<td>Valid from Date : </td>");
			message.append("<td>" + pddDataMap.get("policyDate") + "</td>");
			message.append("<td>Expiry Date : </td>");
			message.append("<td>" + pddDataMap.get("expiryDate") + "</td>");
			message.append("</tr>");

			message.append("<tr>");
			message.append("<td>Insured Value : </td>");
			message.append("<td>" + pddDataMap.get("insuredValue") + "</td>");
			message.append("<td>Chassis No : </td>");
			message.append("<td>" + pddDataMap.get("chasisNo") + "</td>");

			/*message.append("<td>Premium Amount : </td>");
			message.append("<td>" + pddDataMap.get("premiumAmt") + "</td>");*/
			message.append("</tr>");

			/*message.append("<tr>");
			message.append("<td>Benefiary : </td>");
			message.append("<td>" + pddDataMap.get("beneficiary") + "</td>");
			message.append("<td>Insurer : </td>");
			message.append("<td>" + pddDataMap.get("insurer") + "</td>");
			message.append("</tr>");*/

			message.append("<tr>");
			message.append("<td>Engine No : </td>");
			message.append("<td>" + pddDataMap.get("engineNo") + "</td>");
			message.append("<td>No. of Images : </td>");
			message.append("<td>" + pddDataMap.get("imageCount") + "</td>");
			message.append("</tr>");
		}

		message.append("</tr>");

		message.append("</table>");
		message.append("<br>");
		message.append("<br>");
		message.append("The images are getting processed in the background.Kindly wait for a moment. ");
		message.append("<br>");
		message.append("<br>");
		message.append("Thanks,");
		message.append("<br>");
		message.append("Kotak Team");
		message.append("</body>");
		message.append("</html>");

		emailDataMap.put("message", message.toString());

	}

}
