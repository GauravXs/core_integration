package com.mobicule.mcollections.integration.collection;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.mobicule.mcollections.core.beans.Cheque;
import com.mobicule.mcollections.core.beans.RandomCollection;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.beans.TransactionType;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.service.RandomCollectionsServiceImplementation;
import com.mobicule.mcollections.integration.commons.ServerUtilities;

public class FileRandomCollectionsSubmissionService implements IFileRandomCollectionsSubmissionService
{

	private Logger log = LoggerFactory.getLogger(FileRandomCollectionsSubmissionService.class);

	private RandomCollectionsServiceImplementation randomCollectionService;

	public RandomCollectionsServiceImplementation getRandomCollectionService()
	{
		return randomCollectionService;
	}

	public void setRandomCollectionService(RandomCollectionsServiceImplementation randomCollectionService)
	{
		this.randomCollectionService = randomCollectionService;
	}

	@Override
	public Message<String> execute(Message<String> message) throws Throwable
	{
		// TODO Auto-generated method stub

		log.info(" -------- In FileRandomCollectionsSubmissionService / execute() -------- ");

		JSONObject responseJSON = new JSONObject();

		try
		{
			String requestSet = message.getPayload();

			String requestType = JSONPayloadExtractor.extract(requestSet, JsonConstants.TYPE);
			String requestEntity = JSONPayloadExtractor.extract(requestSet, JsonConstants.ENTITY);
			String requestAction = JSONPayloadExtractor.extract(requestSet, JsonConstants.ACTION);

			JSONObject requestSystemUser = JSONPayloadExtractor.extractJSON(requestSet, JsonConstants.SYSTEM_USER);
			JSONObject requestData = JSONPayloadExtractor.extractJSON(requestSet, JsonConstants.DATA);

			SystemUser systemUser = ServerUtilities.extractSystemUser(requestSystemUser);

			String tempccapac = systemUser.getCcapac();

			if (requestAction.equalsIgnoreCase(JsonConstants.ACTION_SUBMIT))
			{
				submitFileRandomCollections(responseJSON, requestData, systemUser, requestEntity);
				log.info("----Submission Complete -----");
			}

		}
		catch (Exception e)
		{
			log.error(" -------- Error in File  Random  Collections  SubmissionService -------- ", e);
			e.printStackTrace();
			responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
			responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_RANDOM_COLLECTIONS_FAILURE);
			responseJSON.put(JsonConstants.DATA, "");
		}

		return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();

	}

	private void submitFileRandomCollections(JSONObject responseJSON, JSONObject requestData, SystemUser systemUser,
			String requestEntity) throws JSONException
	{
		String amountForSms = "";
		String receiptNumberForSms = "";
		String paymentTypeForSms = "";
		String mobileNumberForSms = "";
		String type = "";
		String feName = "";
		String apacCardNumber = "";

		RandomCollection randomCollection = extractRandomCollection(requestData, systemUser);

		if (randomCollection == null)
		{
			log.info(" -------- Failure in extracting Random Collection -------- ");

			responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
			responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_GENERAL_FAILURE);
			responseJSON.put(JsonConstants.DATA, "");
		}
		else
		{
			log.info(" -------- Random Collection extracted Successfully -------- ");

			amountForSms = randomCollection.getAmount();
			receiptNumberForSms = System.currentTimeMillis() + "";
			paymentTypeForSms = randomCollection.getPaymentMode();
			mobileNumberForSms = randomCollection.getMobileNumber();
			feName = randomCollection.getName();
			apacCardNumber = randomCollection.getBusinessPartnerNumber();

			if (requestEntity.equalsIgnoreCase(JsonConstants.CREDIT_CARD))
			{
				type = "Card";
			}
			else
			{
				type = requestEntity;
			}

			if (randomCollectionService.checkDuplicateFileRandomCollectionJSON(randomCollection))
			{
				randomCollection.setThirdPartyStatus("");
				randomCollection.setThirdPartyStatusDesc("");
				boolean insertResult = randomCollectionService.addFileRandomCollection(randomCollection);

				log.info(" --File Random Collection got submitted to Database result : ---" + insertResult);

				responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);

				responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_RANDOM_COLLECTION_SUBMITTED);

			}
			else
			{
				log.info(" -------- Duplicate File Random Collection, Hence Ignored !-------- ");

				responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
				responseJSON.put(JsonConstants.MESSAGE, "JSON DUPLICATED!!!");
				responseJSON.put(JsonConstants.DATA, "");
			}

		}
	}

	private RandomCollection extractRandomCollection(JSONObject requestData, SystemUser systemUser)
	{
		RandomCollection randomCollection = new RandomCollection();

		log.info("--- Request Data ---" + requestData);

		try
		{
			randomCollection.setCcapac(systemUser.getCcapac());
			randomCollection.setRequestId(requestData.has(JsonConstants.REQUEST_ID) == true ? requestData
					.getString(JsonConstants.REQUEST_ID) : new Timestamp(System.currentTimeMillis()).toString());

			randomCollection.setContractAccountNumber(requestData.getString(JsonConstants.CONTRACT_ACCOUNT_NUMBER)); // party
																														// ID
			randomCollection.setBusinessPartnerNumber(requestData.getString(JsonConstants.UNIQUE_NUMBER)); // apac or card
																											// number
			randomCollection.setAppl(requestData.getString(JsonConstants.APPL));
			randomCollection.setName(requestData.getString(JsonConstants.NAME));
			randomCollection.setMobileNumber(requestData.getString(JsonConstants.MOBILE_NUMBER));
			randomCollection.setEmailAddress(requestData.getString(JsonConstants.EMAIL_ADDRESS));
			randomCollection.setCorrAddress(requestData.getString(JsonConstants.CORRESPONDENCE_ADDRESS));

			randomCollection.setCollectionCode(requestData.getString(JsonConstants.COLLECTION_CODE));

			randomCollection.setCorrLocation(requestData.getString(JsonConstants.CORRESPONDENCE_LOCATION));
			randomCollection.setCorrPin(requestData.getString(JsonConstants.CORRESPONDENCE_PINCODE));
			randomCollection.setSecAddress(requestData.getString(JsonConstants.SECOND_ADDRESS));
			randomCollection.setSecLocation(requestData.getString(JsonConstants.SECOND_LOCATION));
			randomCollection.setSecPin(requestData.getString(JsonConstants.SECOND_PINCODE));
			randomCollection.setReceiptNumber(requestData.getString(JsonConstants.RECEIPT_NUMBER));
			randomCollection.setDueDate(requestData.getString(JsonConstants.DUE_DATE));
			randomCollection
					.setOutstanding(Double.parseDouble(requestData.getString(JsonConstants.OUTSTANDING) == null
							|| requestData.getString(JsonConstants.OUTSTANDING)
									.equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0" : requestData
							.getString(JsonConstants.OUTSTANDING)));
			randomCollection.setPaymentMode(requestData.getString(JsonConstants.PAY_MODE));
			randomCollection.setAmount(requestData.getString(JsonConstants.AMOUNT));

			randomCollection.setEmailAddressNew(requestData.getString(JsonConstants.EMAIL_ADDRESS_NEW));
			randomCollection.setMobileNumberNew(requestData.getString(JsonConstants.MOBILE_NUMBER_NEW));

			List<Cheque> chequeList = new ArrayList<Cheque>();

			if ((randomCollection.getPaymentMode()).equals(Constants.PAYMENT_MODE_CASH))
			{
				JSONObject cash = (JSONObject) requestData.get("cash");

				randomCollection.setDocType(cash.getString(JsonConstants.DOCUMENT_TYPE));
				randomCollection.setDocRef(cash.getString(JsonConstants.DOCUMENT_REFERENCE));
				randomCollection.setInstDate(cash.getString(JsonConstants.INSTRUMENT_DATE));

				if (randomCollection.getDocType() != null && randomCollection.getDocType().equalsIgnoreCase("PAN"))
				{
					randomCollection.setPanNumber(cash.getString(JsonConstants.DOCUMENT_REFERENCE));
				}

			}
			/*
			 * else if ((randomCollection.getPaymentMode()).equals(Constants.
			 * PAYMENT_MODE_CHEQUE) ||
			 * (randomCollection.getPaymentMode()).equals
			 * (Constants.PAYMENT_MODE_DRAFT) ||
			 * (randomCollection.getPaymentMode
			 * ()).equals(Constants.PAYMENT_MODE_PDC)) {
			 * 
			 * randomCollection.setPanNumber(Constants.EMPTY_STRING);
			 * 
			 * JSONArray chequeJSONArray =
			 * requestData.getJSONArray(JsonConstants.CHEQUE);
			 * 
			 * for (int i = 0; i < (chequeJSONArray.length()); i++) { JSONObject
			 * chequeJSON = chequeJSONArray.getJSONObject(i);
			 * 
			 * Cheque cheque = new Cheque();
			 * cheque.setDrawerAccountNumber(chequeJSON
			 * .get(JsonConstants.DRAWER_ACCOUNT_NUMBER) == null ?
			 * Constants.EMPTY_STRING :
			 * chequeJSON.getString(JsonConstants.DRAWER_ACCOUNT_NUMBER));
			 * cheque
			 * .setChequeNo(chequeJSON.getString(JsonConstants.CHEQUE_NUMBER));
			 * cheque.setMicrCode(chequeJSON.getString(JsonConstants.MICR));
			 * cheque
			 * .setChequeDate(chequeJSON.getString(JsonConstants.CHEQUE_DATE));
			 * cheque.setBranch(chequeJSON.getString(JsonConstants.BRANCH));
			 * cheque
			 * .setBankName(chequeJSON.getString(JsonConstants.BANK_NAME));
			 * cheque
			 * .setAmount(Double.parseDouble(chequeJSON.getString(JsonConstants
			 * .AMOUNT) == null ||
			 * chequeJSON.getString(JsonConstants.AMOUNT).equalsIgnoreCase
			 * (Constants.EMPTY_STRING) ? "0.0" :
			 * chequeJSON.getString(JsonConstants.AMOUNT)));
			 * cheque.setDepositStatus(Constants.EMPTY_STRING);
			 * cheque.setDepositDate(Constants.EMPTY_STRING);
			 * cheque.setCreatedOn
			 * (Utilities.generateTimestamp(Constants.DATE_TIME_FORMAT));
			 * cheque.setCreatedBy(systemUser.getUserTableId());
			 * cheque.setModifiedOn
			 * (Utilities.generateTimestamp(Constants.DATE_TIME_FORMAT));
			 * cheque.setModifiedBy(systemUser.getUserTableId());
			 * cheque.setDeleteFlag(Constants.FLAG_FALSE);
			 * 
			 * chequeList.add(cheque); }
			 * 
			 * randomCollection.setChequeList(chequeList); }
			 */

			randomCollection.setDeviceDate(requestData.getString(JsonConstants.DEVICE_DATE));
			randomCollection.setDeviceTime(requestData.getString(JsonConstants.DEVICE_TIME));

			randomCollection.setArea(requestData.getString(JsonConstants.AREA));
			randomCollection.setRemarks(requestData.getString(JsonConstants.REMARKS));

			randomCollection.setSubmissionDate(Utilities.generateTimestamp(Constants.DATE_TIME_FORMAT));

			// code to extract cash keys
			if (requestData.has(JsonConstants.LOAN))
			{

				JSONObject loanJSON = (JSONObject) requestData.getJSONObject(JsonConstants.LOAN);

				randomCollection
						.setOverdue(Double.parseDouble(loanJSON.get(JsonConstants.LOAN_OVERDUE) == null
								|| loanJSON.get(JsonConstants.LOAN_OVERDUE).toString()
										.equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0" : loanJSON.get(
								JsonConstants.LOAN_OVERDUE).toString()));
				randomCollection.setPenalAmt(Double.parseDouble(loanJSON.get(JsonConstants.LOAN_PENAL_AMOUNT) == null
						|| loanJSON.get(JsonConstants.LOAN_PENAL_AMOUNT).toString()
								.equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0" : loanJSON.get(
						JsonConstants.LOAN_PENAL_AMOUNT).toString()));
				randomCollection
						.setAppropriateAmount(Double.parseDouble(loanJSON.get(JsonConstants.APPR_AMOUNT) == null
								|| loanJSON.get(JsonConstants.APPR_AMOUNT).toString()
										.equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0" : loanJSON.get(
								JsonConstants.APPR_AMOUNT).toString()));

				JSONArray transType = loanJSON.getJSONArray(JsonConstants.LOAN_TRANS_TYPE);

				List<TransactionType> transTypeList = new ArrayList<TransactionType>();

				for (int i = 0; i < (transType.length()); i++)
				{
					JSONObject transJSON = transType.getJSONObject(i);
					TransactionType transactionType = new TransactionType();
					transactionType.setType(transJSON.get(JsonConstants.LOAN_TRANS_TYPE_TYPE).toString());
					transactionType.setAmount(transJSON.get(JsonConstants.LOAN_TRANS_TYPE_AMOUNT) == null
									|| transJSON.getString(JsonConstants.LOAN_TRANS_TYPE_AMOUNT).equalsIgnoreCase(
											Constants.EMPTY_STRING) ? "0.0" : transJSON.get(
									JsonConstants.LOAN_TRANS_TYPE_AMOUNT).toString());
					transTypeList.add(transactionType);
				}
				
				randomCollection.setTransType(transTypeList);

			}

			if (requestData.has(JsonConstants.CREDIT_CARD))
			{
				JSONObject ccJSON = (JSONObject) requestData.get(JsonConstants.CREDIT_CARD);
				randomCollection.setTad(Double.parseDouble(ccJSON.get(JsonConstants.TAD) == null
						|| ccJSON.get(JsonConstants.TAD).toString().equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0"
						: ccJSON.get(JsonConstants.TAD).toString()));
				randomCollection.setMad(Double.parseDouble(ccJSON.get(JsonConstants.MAD) == null
						|| ccJSON.get(JsonConstants.MAD).toString().equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0"
						: ccJSON.get(JsonConstants.MAD).toString()));
				randomCollection.setBuckAmt1(Double.parseDouble(ccJSON.get(JsonConstants.BUCKET_AMOUNT_1) == null
						|| ccJSON.get(JsonConstants.BUCKET_AMOUNT_1).toString()
								.equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0" : ccJSON.get(
						JsonConstants.BUCKET_AMOUNT_1).toString()));
				randomCollection.setBuckAmt2(Double.parseDouble(ccJSON.get(JsonConstants.BUCKET_AMOUNT_2) == null
						|| ccJSON.get(JsonConstants.BUCKET_AMOUNT_2).toString()
								.equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0" : ccJSON.get(
						JsonConstants.BUCKET_AMOUNT_2).toString()));
				randomCollection.setRollbackAmt(Double.parseDouble(ccJSON.get(JsonConstants.ROLL_BACK_AMOUNT) == null
						|| ccJSON.get(JsonConstants.ROLL_BACK_AMOUNT).toString()
								.equalsIgnoreCase(Constants.EMPTY_STRING) ? "0.0" : ccJSON.get(
						JsonConstants.ROLL_BACK_AMOUNT).toString()));
			}

			// randomCollection.setBatchNumber(requestData.getString(JsonConstants.BATCH_NUMBER));
			randomCollection.setCreatedOn(Utilities.generateTimestamp(Constants.DATE_TIME_FORMAT));
			randomCollection.setCreatedBy(systemUser.getUserTableId());
			randomCollection.setModifiedOn(Utilities.generateTimestamp(Constants.DATE_TIME_FORMAT));
			randomCollection.setModifiedBy(systemUser.getUserTableId());
			randomCollection.setDeleteFlag(Constants.FLAG_FALSE);
		}
		catch (Exception e)
		{
			log.info(" -------- Failure in extracting Random Collection -------- ");

			e.printStackTrace();

			return null;
		}

		return randomCollection;
	}

}
