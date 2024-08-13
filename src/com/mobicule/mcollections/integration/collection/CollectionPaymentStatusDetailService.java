package com.mobicule.mcollections.integration.collection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.mobicule.component.mapconversion.json.MapToJSON;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.service.CollectionPaymentStatusService;
/**
 * 
 * @author tushar
 *
 */
public class CollectionPaymentStatusDetailService implements ICollectionPaymentStatusDetailService
{
	 Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private CollectionPaymentStatusService collectionPaymentStatusService;

	public CollectionPaymentStatusService getCollectionPaymentStatusService() {
		return collectionPaymentStatusService;
	}

	public void setCollectionPaymentStatusService(
			CollectionPaymentStatusService collectionPaymentStatusService) {
		this.collectionPaymentStatusService = collectionPaymentStatusService;
	}

	@Override
	public Message<String> execute(Message<String> message) throws Throwable
	{	
		JSONObject responseJSON = new JSONObject();
		
		try
		{
			String requestSet = message.getPayload();
			
			JSONObject requestData = JSONPayloadExtractor.extractJSON(requestSet, JsonConstants.DATA);
			
			/*Object invoiceId = requestData.getString("invoiceId") == null ? "" : requestData.getString("invoiceId");*/
			
			String receiptNumber = requestData.getString("receiptNumber") == null ? "" : requestData.getString("receiptNumber");
			
			/*if(StringUtils.isEmpty(invoiceId.toString()) || StringUtils.isEmpty(receiptNumber))*/
			if(StringUtils.isEmpty(receiptNumber))
			{
				StringBuilder errorMassage = new StringBuilder();
				
				errorMassage.append("The");
				/*errorMassage.append(invoiceId.equals("") && receiptNumber.equals("") ? " invoice id, receipt number " : invoiceId.equals("") ? " invoice id " : receiptNumber.equals("") ? " receipt number " : "");*/
				errorMassage.append(receiptNumber.equals("") ? " receipt number " :  receiptNumber.equals("") ? " receipt number " : "");
				errorMassage.append("is empty");
				
				responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
				responseJSON.put(JsonConstants.MESSAGE, errorMassage);
				responseJSON.put(JsonConstants.DATA,"");
				return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
			}
			
			/*List<Map<String, Object>> paymentStatusData = collectionPaymentStatusService.getPaymentStatus(invoiceId.toString(),receiptNumber);*/
			List<Map<String, Object>> paymentStatusData = collectionPaymentStatusService.getPaymentStatus(receiptNumber);
			
			log.info("---- Payment Status ----- " +  paymentStatusData );
			
			if(paymentStatusData != null)
			{
				Map<String, Object> paymentStatus= paymentStatusData.get(0);
				
				String status = (String) paymentStatus.get("ONLINE_PAYMENT_STATUS");
				
				responseJSON.put(JsonConstants.STATUS, status);
			}
			else
			{
				responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
			}

			Map<String, Object> data = new HashMap<String, Object>();
			/*data.put("invoiceId", invoiceId);*/
			data.put("receiptNumber", receiptNumber);
			responseJSON.put(JsonConstants.MESSAGE, "Payment not received from customer");
			responseJSON.put(JsonConstants.DATA,MapToJSON.convertMapToJSON(data));
			
		}
		catch(Exception e)
		{
			log.info("Exception"+e);
			e.printStackTrace();
			responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
			responseJSON.put(JsonConstants.MESSAGE, "No Details Found for this invoice id and receipt number!");
		}
		return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
	
	}
	
}
