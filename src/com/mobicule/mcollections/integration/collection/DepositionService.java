package com.mobicule.mcollections.integration.collection;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONArray;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.mobicule.component.activitylogger.service.CommunicationActivityService;
import com.mobicule.component.activitylogger.service.UserActivityService;
import com.mobicule.component.mapconversion.json.MapToJSON;
import com.mobicule.mcollections.core.beans.Collection;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.SmsFormXML;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.CollectionService;
import com.mobicule.mcollections.core.service.SystemUserService;
import com.mobicule.mcollections.integration.commons.ServerUtilities;

public class DepositionService implements IDepositionService
{

	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private SystemUserService systemUserService;

	@Autowired
	private CollectionService collectionService;

	@Autowired
	ApplicationConfiguration applicationConfiguration;

	@Autowired
	private UserActivityService userActivityService;

	@Autowired
	private CommunicationActivityService communicationActivityService;
	
	@Override
	public Message<String> execute(Message<String> message) throws Throwable
	{
		JSONObject responseJSON = new JSONObject();

		String bankName = "";
		String branchName = "";
		String micr = "";
		String transactionId = "";
		String consolidatedAmount = "";
		String image = "";
		String payMode = "";
		String path = "";
		String numberOfDeposition = "";
		String payslipDate = "";
		String bnAPAC = "";
		String challanNumber = "";

		int validityCount = 0;

		log.info(" -------- In DepositionService -------- ");

		try
		{
			log.info("---- Inside Deposition Service -------");

			String requestSet = message.getPayload();

			log.info("----request set ---" + requestSet);

			/*	UserActivityAddition userActivityAddition = new UserActivityAddition(requestSet, userActivityService,
						ActivityLoggerConstants.DATABASE_MSSQL);

				new Thread(userActivityAddition).run();

				UserActivity userActivity = userActivityAddition.extractUserActivity();
			*/
			JSONObject jsonObject = new JSONObject(requestSet);
			JSONObject data = (JSONObject) jsonObject.get(JsonConstants.DATA);
			JSONObject user = (JSONObject) jsonObject.get(JsonConstants.SYSTEM_USER);

			SystemUser systemUser = ServerUtilities.extractSystemUser(user);

			log.info("---exctacter user ----" + systemUser);

			//SystemUser systemUserTemp = systemUserService.getUser(systemUser.getUserTableId());

			Map reqMap = Utilities.createMapFromJSON(requestSet);

			log.info("----reqmap----" + reqMap.toString());

			String action = (String) reqMap.get("action");

			if (action.equalsIgnoreCase("sync"))
			{

				log.info("--- Inside search data--- ");

				log.info("userId to get deposition ----" + systemUser.getUserTableId());

				log.info("---- Deposition for user -----" + systemUser.getUsername());

				Map<String, Object> responseMap = new HashMap<String, Object>();

				Map<String, Object> tempMap = new HashMap<String, Object>();

				List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();

				responseMap.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
				/*responseMap.put(JsonConstants.MESSAGE,
								Utilities.convertDateINYYYYMMDD(new Timestamp(System.currentTimeMillis())));*/

				responseMap
						.put(JsonConstants.MESSAGE, Utilities.convertDate(new Timestamp(System.currentTimeMillis())));

				/* Temp Code added for Prime */

				/*List<Map<String, Object>> map = collectionService.getCollectionByUser(systemUser);

				for (Map<String, Object> map2 : map)
				{

					map2.put("collType", "LOAN");
					tempList.add(map2);

				}

				log.info("---tempList----" + tempList);

				responseMap.put(JsonConstants.DATA, tempList);*/
				
				/* End */
				

				/* Actual producation code  */

				log.info("----request map-----"+collectionService.getCollectionByUser(systemUser));

			    responseMap.put(JsonConstants.DATA, (collectionService.getCollectionByUser(systemUser)));

				/* End */

				Map<String, String> headerMap = new HashMap<String, String>();

				headerMap.put("CONTRACT_ACCOUNT_NUMBER", Constants.Deposition.CONTRACT_ACCOUNT_NUMBER);
				headerMap.put("TYPE", Constants.Deposition.TYPE);
				headerMap.put("APPL", Constants.APPL);
				headerMap.put("RECEIPT_NUMBER", Constants.Deposition.RECEIPT_NUMBER);
				headerMap.put("SUBMISSION_DATE_TIME", Constants.Deposition.SUBMISSION_DATE_TIME);
				headerMap.put("AMOUNT", Constants.Deposition.AMOUNT);
				headerMap.put("PAYMENT_MODE", Constants.Deposition.PAYMENT_MODE);
				headerMap.put("BUSINESS_PARTNER_NUMBER", Constants.Deposition.BUSINESS_PARTNER_NUMBER);
				headerMap.put("PARTY_NAME", Constants.Deposition.PARTY_NAME);
				headerMap.put("REGNO", Constants.Deposition.REGNO);
				headerMap.put("DEPOSITION_STATUS", Constants.Deposition.DEPOSITION_STATUS);
				headerMap.put("COLLECTION_ID", Constants.Deposition.COLLECTION_ID);
				headerMap.put("COLLECTION_ID", Constants.Deposition.COLLECTION_ID);
				headerMap.put("collType", "collType");

				responseJSON = MapToJSON.convertMapToJSON(responseMap, headerMap);

				log.info("--- data send to device -----" + responseJSON);

			}
			else
			{

				log.info("--- Inside Submit Deposition Status ---");

				log.info("userId to submit deposition ----" + systemUser.getUserTableId());

				JSONArray dataArray = (JSONArray) data.get(Constants.Deposition.CUSTOMER);

				image = data.has(Constants.Deposition.DEPOSITION_SLIP_IMAGE) ? data
						.getString(Constants.Deposition.DEPOSITION_SLIP_IMAGE) : "";

				consolidatedAmount = data.has(Constants.Deposition.CONSOLIDATED_AMOUNT) ? data
						.getString(Constants.Deposition.CONSOLIDATED_AMOUNT) : "";

				if (Utilities.checkInputForDouble(consolidatedAmount))
				{

					validityCount = validityCount + 1;

				}

				transactionId = data.has(Constants.Deposition.TRANSACTION_ID) ? data
						.getString(Constants.Deposition.TRANSACTION_ID) : "";

				if (Utilities.checkInputForNumber(transactionId))
				{

					validityCount = validityCount + 1;

				}

				bankName = data.has(Constants.Deposition.BANK_NAME) ? data.getString(Constants.Deposition.BANK_NAME)
						: "";

				if (Utilities.checkInputForValidity(bankName))
				{

					validityCount = validityCount + 1;

				}

				branchName = data.has(Constants.Deposition.BRANCH_NAME) ? data
						.getString(Constants.Deposition.BRANCH_NAME) : "";

				if (Utilities.checkInputForValidity(branchName))
				{

					validityCount = validityCount + 1;

				}

				micr = data.has(Constants.Deposition.MICR_CODE) ? data.getString(Constants.Deposition.MICR_CODE) : "";

				if (Utilities.checkInputForValidity(micr))
				{

					validityCount = validityCount + 1;

				}

				payMode = data.has(Constants.Deposition.PAYMENT_MODE) ? data
						.getString(Constants.Deposition.PAYMENT_MODE) : "";
						
				if (payMode.equalsIgnoreCase("DD"))
				{
					payMode = "DFT";

				}

				if (Utilities.checkInputForString(payMode))
				{

					validityCount = validityCount + 1;

				}

				numberOfDeposition = data.has(Constants.Deposition.NUMBER_OF_DEPOSITION) ? data
						.getString(Constants.Deposition.NUMBER_OF_DEPOSITION) : "";

				if (Utilities.checkInputForValidity(numberOfDeposition))
				{

					validityCount = validityCount + 1;

				}

				payslipDate = data.has(Constants.Deposition.DEPOSITION_PAYSLIP_DATE) ? Utilities
						.changeDateFormatToYMD(data.getString(Constants.Deposition.DEPOSITION_PAYSLIP_DATE).toString())
						: "";

				if (Utilities.checkInputForDateValidity(payslipDate))
				{

					validityCount = validityCount + 1;

				}

				log.info("---deposition date ----" + payslipDate);

				bnAPAC = data.has(Constants.Deposition.DEPOSITION_CCAPAC) ? data
						.getString(Constants.Deposition.DEPOSITION_CCAPAC) : "";

				if (Utilities.checkInputForValidity(bnAPAC))
				{

					validityCount = validityCount + 1;

				}

				challanNumber = data.has(Constants.Deposition.CHALLAN_NUMBER) ? data
						.getString(Constants.Deposition.CHALLAN_NUMBER) : "";

				if (Utilities.checkInputForValidity(challanNumber))
				{

					validityCount = validityCount + 1;

				}

				path = extractImage(image, transactionId);

				log.info("----path----" + path);

				List<Collection> collectionList = new ArrayList<Collection>();
				List<Collection> kgiList = new ArrayList<Collection>();

				for (int i = 0; i < dataArray.length(); i++)
				{
					JSONObject individualDeposition = (JSONObject) dataArray.get(i);

					if (individualDeposition.getString("collType").equalsIgnoreCase("KGI"))
					{
						Collection collection = new Collection();
						collection.setDepositionImagePath(path);

						collection.setContractAccountNumber(individualDeposition
								.has(Constants.Deposition.CONTRACT_ACCOUNT_NUMBER) ? individualDeposition
								.getString(Constants.Deposition.CONTRACT_ACCOUNT_NUMBER) : "");

						if (Utilities.checkInputForValidity(individualDeposition
								.has(Constants.Deposition.CONTRACT_ACCOUNT_NUMBER) ? individualDeposition
								.getString(Constants.Deposition.CONTRACT_ACCOUNT_NUMBER) : ""))
						{

							validityCount = validityCount + 1;

						}

						collection.setBusinessPartnerNumber(individualDeposition
								.has(Constants.Deposition.BUSINESS_PARTNER_NUMBER) ? individualDeposition
								.getString(Constants.Deposition.BUSINESS_PARTNER_NUMBER) : "");

						if (Utilities.checkInputForValidity(individualDeposition
								.has(Constants.Deposition.BUSINESS_PARTNER_NUMBER) ? individualDeposition
								.getString(Constants.Deposition.BUSINESS_PARTNER_NUMBER) : ""))
						{

							validityCount = validityCount + 1;

						}

						collection
								.setCollectionType(individualDeposition.has(Constants.Deposition.TYPE) ? individualDeposition
										.getString(Constants.Deposition.TYPE) : "");

						if (Utilities
								.checkInputForValidity(individualDeposition.has(Constants.Deposition.TYPE) ? individualDeposition
										.getString(Constants.Deposition.TYPE) : ""))
						{

							validityCount = validityCount + 1;

						}

						collection.setAppl(individualDeposition.has(Constants.APPL) ? individualDeposition
								.getString(Constants.APPL) : "");

						if (Utilities
								.checkInputForValidity(individualDeposition.has(Constants.APPL) ? individualDeposition
										.getString(Constants.APPL) : ""))
						{

							validityCount = validityCount + 1;

						}

						collection
								.setReceiptNumber(individualDeposition.has(Constants.Deposition.RECEIPT_NUMBER) ? individualDeposition
										.getString(Constants.Deposition.RECEIPT_NUMBER) : "");

						if (Utilities.checkInputForValidity(individualDeposition
								.has(Constants.Deposition.RECEIPT_NUMBER) ? individualDeposition
								.getString(Constants.Deposition.RECEIPT_NUMBER) : ""))
						{

							validityCount = validityCount + 1;

						}

						collection.setDepositionId(transactionId);

						collection.setCollectionId(Long.parseLong((individualDeposition
								.has(Constants.Deposition.COLLECTION_ID) ? individualDeposition
								.getString(Constants.Deposition.COLLECTION_ID) : "")));

						if (Utilities.checkInputForValidity(challanNumber))
						{

							validityCount = validityCount + 1;

						}

						collection.setDepositionCCAPAC(bnAPAC);

						collection.setDepositionStatus(Constants.Deposition.DEVICE_DEPOSITION_DONE);

						collection.setConsolidatedAmount(consolidatedAmount);

						collection.setDepositionPaymentMode(payMode);

						collection.setDepositionBankName(bankName);

						collection.setDepositionBranchName(branchName);

						collection.setDepositionMICRCode(micr);

						collection.setNumberOfDeposition(numberOfDeposition);

						collection.setDepositionSubmissionDate(Utilities.sysDate().toString());

						collection.setDepositionpayslipDate(payslipDate);

						collection.setChallanNumber(challanNumber);

						kgiList.add(collection);

					}

					else
					{
						Collection collection = new Collection();

						collection.setDepositionImagePath(path);

						collection.setContractAccountNumber(individualDeposition
								.has(Constants.Deposition.CONTRACT_ACCOUNT_NUMBER) ? individualDeposition
								.getString(Constants.Deposition.CONTRACT_ACCOUNT_NUMBER) : "");

						if (Utilities.checkInputForValidity(individualDeposition
								.has(Constants.Deposition.CONTRACT_ACCOUNT_NUMBER) ? individualDeposition
								.getString(Constants.Deposition.CONTRACT_ACCOUNT_NUMBER) : ""))
						{

							validityCount = validityCount + 1;

						}

						collection.setBusinessPartnerNumber(individualDeposition
								.has(Constants.Deposition.BUSINESS_PARTNER_NUMBER) ? individualDeposition
								.getString(Constants.Deposition.BUSINESS_PARTNER_NUMBER) : "");

						if (Utilities.checkInputForValidity(individualDeposition
								.has(Constants.Deposition.BUSINESS_PARTNER_NUMBER) ? individualDeposition
								.getString(Constants.Deposition.BUSINESS_PARTNER_NUMBER) : ""))
						{

							validityCount = validityCount + 1;

						}

						collection
								.setCollectionType(individualDeposition.has(Constants.Deposition.TYPE) ? individualDeposition
										.getString(Constants.Deposition.TYPE) : "");

						if (Utilities
								.checkInputForValidity(individualDeposition.has(Constants.Deposition.TYPE) ? individualDeposition
										.getString(Constants.Deposition.TYPE) : ""))
						{

							validityCount = validityCount + 1;

						}

						collection.setAppl(individualDeposition.has(Constants.APPL) ? individualDeposition
								.getString(Constants.APPL) : "");

						if (Utilities
								.checkInputForValidity(individualDeposition.has(Constants.APPL) ? individualDeposition
										.getString(Constants.APPL) : ""))
						{

							validityCount = validityCount + 1;

						}

						collection
								.setReceiptNumber(individualDeposition.has(Constants.Deposition.RECEIPT_NUMBER) ? individualDeposition
										.getString(Constants.Deposition.RECEIPT_NUMBER) : "");

						if (Utilities.checkInputForValidity(individualDeposition
								.has(Constants.Deposition.RECEIPT_NUMBER) ? individualDeposition
								.getString(Constants.Deposition.RECEIPT_NUMBER) : ""))
						{

							validityCount = validityCount + 1;

						}

						collection.setDepositionId(transactionId);

						collection.setCollectionId(Long.parseLong((individualDeposition
								.has(Constants.Deposition.COLLECTION_ID) ? individualDeposition
								.getString(Constants.Deposition.COLLECTION_ID) : "")));

						if (Utilities.checkInputForValidity(challanNumber))
						{

							validityCount = validityCount + 1;

						}

						collection.setDepositionCCAPAC(bnAPAC);

						collection.setDepositionStatus(Constants.Deposition.DEVICE_DEPOSITION_DONE);

						collection.setConsolidatedAmount(consolidatedAmount);

						collection.setDepositionPaymentMode(payMode);

						collection.setDepositionBankName(bankName);

						collection.setDepositionBranchName(branchName);

						collection.setDepositionMICRCode(micr);

						collection.setNumberOfDeposition(numberOfDeposition);

						collection.setDepositionSubmissionDate(Utilities.sysDate().toString());

						collection.setDepositionpayslipDate(payslipDate);

						collection.setChallanNumber(challanNumber);

						collectionList.add(collection);

					}

				}

				boolean result = false;

				if (!kgiList.isEmpty())
				{

			       result = collectionService.submitDepositionData(kgiList, "KGI_COLLECTIONS");

				}
				if (!collectionList.isEmpty())
				{
					
					//result = collectionService.submitDepositionData(collectionList);
					
					
					result = collectionService.submitDepositionData(collectionList, "COLLECTIONS");
					
					if(result)
					{
						/* Collections acknowledgment SMS */
						Map<String, Object> parametersMap = new HashMap<String, Object>();
						Map<String, Object> parametersMaps = new HashMap<String, Object>();
						Collection collection = new Collection();
						//collection.getCreatedBy(systemUser.getUserTableId() == null ? "" : systemUser.getUserTableId().toString());
						
						collection.setCreatedBy(systemUser.getUserTableId() == null ? 0L : Long.valueOf(systemUser.getUserTableId()));
						List<SystemUser> getAllATRUser = collectionService.getAllAgencyUser(collection);
						
						log.info("getAllATRUser :-"+getAllATRUser.size());
						if(!getAllATRUser.isEmpty())
						{
							for(SystemUser sysUser : getAllATRUser)
							{
								collection.setEmail(sysUser.getEmailAddress() == null ? Constants.EMPTY_STRING :  sysUser.getEmailAddress());
								collection.setMobileNumber(sysUser.getMobileNumber()  == null  ? Constants.EMPTY_STRING  : sysUser.getMobileNumber());
								collection.setAmount(data.has(Constants.Deposition.CONSOLIDATED_AMOUNT) ? data
										.getString(Constants.Deposition.CONSOLIDATED_AMOUNT) : "");
								collection.setDepositionId(transactionId);
								collection.setDepositionLotNumber(transactionId);
								log.info("sysUser :- "+sysUser.toString());
								parametersMaps.put(Constants.smsParam.TYPE, Constants.smsParam.MOBICULE_EMAIL_DEPOSITION);
				
								log.info("parametersMaps :- "+parametersMaps);
								log.info("collection :- "+collection.toString());
								
								if(!StringUtils.isEmpty(collection.getEmail()))
								{
									Map<String, Object> receiptApproveEmailStatus = SmsFormXML
											.generateCasesSmsEmailXmlDeposition(parametersMaps, collection);
									
									parametersMap.put(Constants.LdapParam.LDAP_URL,
											applicationConfiguration.getValue(Constants.smsParam.SMS_EMAIL_URL));
									parametersMap.put(Constants.LdapParam.LDAPREQUEST,
											receiptApproveEmailStatus.get(Constants.smsParam.REQUESTHEDER) == null ? ""
													: receiptApproveEmailStatus.get(Constants.smsParam.REQUESTHEDER));
									try
									{
										String responseEmailString = Utilities.postXML(parametersMap);
									}
									catch(Exception exception)
									{
										exception.printStackTrace();
										log.info("exception :-" +exception);
									}
								}
								else
								{
									log.info("User email id is empty");
								}
							
							}
						}
					}

				}

				if(result)
				{
					if(!collectionList.isEmpty())
					{
						List<SystemUser> systemUserList =	systemUserService.getUserDetailsForSendSMS( systemUser);
						log.info("systemUserList :- "+systemUserList);
						for(Collection collections :collectionList)
						{
							Collection collection = new Collection();
							SystemUser userdata = new SystemUser();
							
							if(!systemUserList.isEmpty())
							{
								userdata = systemUserList.get(0);
								
							}
							
							log.info("userdata :- "+userdata);
							collection.setEmail(userdata.getEmailAddress() == null ? "" : userdata.getEmailAddress());
							collection.setMobileNumber(userdata.getMobileNumber() == null ? "" : userdata.getMobileNumber());
							collection.setAmount(collections.getConsolidatedAmount());
							collection.setDepositionId(transactionId);
							collection.setDepositionLotNumber(transactionId);
							Map<String, Object> parametersMaps = new HashMap<String, Object>();
							Map<String, Object> parametersMap = new HashMap<String, Object>();
							Map<String, Object> parametesMap = new HashMap<String, Object>();
							log.info("getCollectionObject "+ collection.toString());
							parametersMaps.put(Constants.smsParam.TYPE, Constants.smsParam.MOBICULE_EMAIL_DEPOSITION);
			
							Map<String, Object>depositionSubmissionStatus = SmsFormXML.generateCasesSmsEmailXmlDeposition(parametersMaps,
									collection);
			
							parametersMap.put(Constants.LdapParam.LDAP_URL,
									applicationConfiguration.getValue(Constants.smsParam.SMS_EMAIL_URL));
							parametersMap.put(Constants.LdapParam.LDAPREQUEST,
									depositionSubmissionStatus.get(Constants.smsParam.REQUESTHEDER) == null ? ""
											: depositionSubmissionStatus.get(Constants.smsParam.REQUESTHEDER));
							String responseString = Utilities.postXML(parametersMap);
							
							
							parametesMap.put(Constants.REQUEST,
									depositionSubmissionStatus.get(Constants.smsParam.REQUESTHEDER) == null ? ""
											: depositionSubmissionStatus.get(Constants.smsParam.REQUESTHEDER));
							parametesMap.put(Constants.RESPONSE,
									responseString == null ? Constants.EMPTY_STRING : responseString);
							parametesMap.put(Constants.SMSEMAILURL, applicationConfiguration.getValue(Constants.smsParam.SMS_EMAIL_URL));
							parametesMap.put(Constants.SMSEMAILTYPE, Constants.EMAIL);
							
							String responseSMSString = Utilities.postXML(parametersMap);
		
							parametersMaps.put(Constants.smsParam.TYPE, Constants.smsParam.MOBICULE_DEPOSITION_FOS);
							parametesMap.put(Constants.REQUEST,
									depositionSubmissionStatus.get(Constants.smsParam.REQUESTHEDER) == null ? ""
											: depositionSubmissionStatus.get(Constants.smsParam.REQUESTHEDER));
							parametesMap.put(Constants.RESPONSE,
									responseSMSString == null ? Constants.EMPTY_STRING : responseSMSString);
							parametesMap.put(Constants.SMSEMAILURL, applicationConfiguration.getValue(Constants.smsParam.SMS_EMAIL_URL));
							parametesMap.put(Constants.SMSEMAILTYPE, Constants.EMAIL);
		
							log.info("parametes Map :- " + parametesMap);
		
							log.info("user Map :- " + user);
		
							systemUserService.getInsertUpdateSmsEmailActivity(parametesMap, systemUser,
									communicationActivityService, collection);
						break;
						}
					}
				}
				
				if (result)
				{
					
					responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
					responseJSON.put(JsonConstants.MESSAGE, JsonConstants.SUCCESS);

				}
				else
				{
					responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
					responseJSON.put(JsonConstants.MESSAGE, JsonConstants.FAILURE);

				}

			}

		}
		catch (Exception e)
		{
			log.error("---- Error in getting depositions -----");
			responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
			responseJSON.put(JsonConstants.MESSAGE, JsonConstants.NO_DEPOSITION_FOUND);

		}

		return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();

	}

	private String extractImage(String image, String transactionId)
	{
		String imagePath = Constants.EMPTY_STRING;

		String fileName = Constants.EMPTY_STRING;

		String filePath = Constants.EMPTY_STRING;

		if (image.equals(Constants.EMPTY_STRING))
		{
			return imagePath;
		}

		{

			fileName = transactionId + Constants.SYMBOL_UNDERSCORE + System.currentTimeMillis();

			imagePath = generateFilePath(
					(String.valueOf(applicationConfiguration.getValue(Constants.Deposition.DEPOSITION_IMAGE_PATH))),
					fileName, transactionId);

			filePath = transactionId + "/" + fileName + Constants.EXTENSION_IMAGE;

			log.info("====imagePath    " + imagePath);

			//filePath = imagePath + fileName + Constants.EXTENSION_IMAGE;

			log.info("filePath" + imagePath);

		}

		log.info("writing image -->" + imagePath);

		if (Utilities.writeImage(imagePath, image))
		{
			return filePath;
		}

		return (Constants.ERROR);
	}

	public static String generateFilePath(String filePath, String fileName, String reqId)
	{

		if (!(Utilities.validateDirectoryExistence(filePath + reqId)))
		{
			(new File(filePath + reqId)).mkdir();
		}

		filePath = filePath + reqId + "/" + fileName + Constants.EXTENSION_IMAGE;

		return filePath;
	}

}
