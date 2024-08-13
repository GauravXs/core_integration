/**
 * 
 */
package com.mobicule.mcollections.integration.collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.mobicule.mcollections.core.beans.Collection;

import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.service.CollectionService;

/**
 * @author prashant
 *
 */
/**
* 
* <enter description here>
*
* @author @Prashant Kulkarni
* @see 
*
* @createdOn 14-Aug-2018
* @modifiedOn
*
* @copyright © 2018-2019 Mobicule Technologies Pvt. Ltd. All rights reserved.
*/
public class BluetoothPrintingService implements IBluetoothPrintingService
{

	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private CollectionService collectionService;

	/* (non-Javadoc)
	 * @see com.mobicule.mcollections.integration.messaging.IService#execute(org.springframework.integration.Message)
	 */
	@Override
	public Message<String> execute(Message<String> message) throws Throwable
	{

		log.info("----inside Bluetooth Printing Service -----");

		String requestSet = message.getPayload();

		String status = JsonConstants.FAILURE;

		String returnMessage = JsonConstants.MESSAGE_GENERAL_FAILURE;

		JSONObject jsonObject = new JSONObject(requestSet);
		JSONObject data = (JSONObject) jsonObject.get(JsonConstants.DATA);
		JSONObject user = (JSONObject) jsonObject.get(JsonConstants.SYSTEM_USER);

		//SystemUser systemUser = ServerUtilities.extractSystemUser(user);

		Collection collection = new Collection();

		collection.setRequestId(data.has(JsonConstants.REQUEST_ID) ? data.getString(JsonConstants.REQUEST_ID)
				: Constants.EMPTY_STRING);
		collection.setReceiptNumber(data.has(JsonConstants.RECEIPT_NUMBER) ? data
				.getString(JsonConstants.RECEIPT_NUMBER) : Constants.EMPTY_STRING);
		collection.setDeviceDate(data.has(JsonConstants.DEVICE_DATE_TIME) ? data
				.getString(JsonConstants.DEVICE_DATE_TIME) : Constants.EMPTY_STRING);
		collection.setBusinessPartnerNumber(data.has(JsonConstants.APAC_CARD_NUMBER) ? data
				.getString(JsonConstants.APAC_CARD_NUMBER) : Constants.EMPTY_STRING);
		collection.setAppl(data.has(JsonConstants.APPL) ? data.getString(JsonConstants.APPL) : Constants.EMPTY_STRING);

		collection.setDeviceSerialNo((data.has(JsonConstants.DEVICE_SERIAL_NUMBER) ? data
				.getString(JsonConstants.DEVICE_SERIAL_NUMBER) : Constants.EMPTY_STRING));
		
		collection.setCreatedBy(Long.valueOf(user.get(JsonConstants.SYSTEM_USER_ID).toString()));
		
		collection.setModifiedBy(Long.valueOf(user.get(JsonConstants.SYSTEM_USER_ID).toString()));
		

		if (!collectionService.checkDuplicatePrintindDetails(collection))
		{

			boolean result = collectionService.submitPrintingDetails(collection);

			if (result)
			{
				status = JsonConstants.SUCCESS;
				returnMessage = JsonConstants.GENERIC_SUCCESS_RESPONSE_MSG;

			}

		}

		else
		{

			status = JsonConstants.SUCCESS;
			returnMessage = JsonConstants.GENERIC_DUPLICATE_RESPONSE_MSG;

		}

		return responseBuilder(message, status, returnMessage, collection.getRequestId());

	}

	private Message<String> responseBuilder(Message<String> message, String status, String returnMessage, String reqId)
			throws JSONException
	{
		JSONObject responseJSON = new JSONObject();
		JSONObject data = new JSONObject();

		data.put(JsonConstants.REQUEST_ID, reqId);

		responseJSON.put(JsonConstants.STATUS, status);
		responseJSON.put(JsonConstants.MESSAGE, returnMessage);
		responseJSON.put(JsonConstants.DATA, data);

		return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
	}

}
