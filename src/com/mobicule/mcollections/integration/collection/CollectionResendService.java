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
import com.mobicule.mcollections.core.beans.Collection;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.SmsFormXML;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.CollectionPaymentStatusService;
/**
 * 
 * @author tushar
 *
 */
public class CollectionResendService implements ICollectionResendService
{
	 Logger log = LoggerFactory.getLogger(getClass());
	
	 
	ApplicationConfiguration applicationConfiguration;

	
	public ApplicationConfiguration getApplicationConfiguration() {
		return applicationConfiguration;
	}


	public void setApplicationConfiguration(ApplicationConfiguration applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
	}


	@Override
	public Message<String> execute(Message<String> message) throws Throwable
	{	
		JSONObject responseJSON = new JSONObject();
		
		try
		{
			String requestSet = message.getPayload();
			
			JSONObject requestData = JSONPayloadExtractor.extractJSON(requestSet, JsonConstants.DATA);
			
			String paymentMode = requestData.getString("paymentMode") == null ? "" : requestData.getString("paymentMode");
			String receiptNumber = requestData.getString("receiptNumber") == null ? "" : requestData.getString("receiptNumber");
			String amount = requestData.getString("amount") == null ? "" : requestData.getString("amount");
			String date = requestData.getString("date") == null ? "" : requestData.getString("date");
			String acctNo = requestData.getString("acctNo") == null ? "" : requestData.getString("acctNo");
			String chqNo = requestData.getString("chqNo") == null ? "" : requestData.getString("chqNo");
			String ddNo = requestData.getString("ddNo") == null ? "" : requestData.getString("ddNo");
			String customerEmailId = requestData.getString("customerEmailId") == null ? "" : requestData.getString("customerEmailId");
			String customerMobileNo = requestData.getString("customerMobileNo") == null ? "" : requestData.getString("customerMobileNo");
			String collectorEmailId = requestData.getString("collectorEmailId") == null ? "" : requestData.getString("collectorEmailId");
			String collectorMobileNo = requestData.getString("collectorMobileNo") == null ? "" : requestData.getString("collectorMobileNo");
			
			Collection collection = new Collection();
			collection.setReceiptNumber(receiptNumber);
			collection.setAmount(amount);
			collection.setDeviceDate(date);
			collection.setPaymentMode(paymentMode);
			collection.setBusinessPartnerNumber(acctNo);
			if(!StringUtils.isEmpty(chqNo))
			collection.setChequeNumber(chqNo);
			else if(!StringUtils.isEmpty(ddNo))
				collection.setChequeNumber(ddNo);
				
			collection.setEmail(customerEmailId);
			collection.setMobileNumber(customerMobileNo);
			
			/* Collections acknowledgment SMS */
			
			log.info("collection :-" + collection == null ? Constants.EMPTY_STRING : collection.toString());

			Map<String, Object> smsDispatcherMap = new HashMap<String, Object>();
			try
			{
				Map<String, Object> parametersMaps = new HashMap<String, Object>();
				smsDispatcherMap = SmsFormXML.generateCollectionSmsXml( parametersMaps ,collection);
			}
			catch(Exception e)
			{
				log.info("Exception :- "+e);
			}

			/*
			 * StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true,
			 * new HashMap<String, String>());
			 */
			Map<String, Object> createUserParamMap = new HashMap<String, Object>();

			String url = (String) (applicationConfiguration.getValue(Constants.smsParam.SMS_EMAIL_URL) == null ? "" : applicationConfiguration.getValue(Constants.smsParam.SMS_EMAIL_URL));
			createUserParamMap.put(Constants.LdapParam.LDAP_URL,url);
			createUserParamMap.put(Constants.LdapParam.LDAPREQUEST, smsDispatcherMap.get(Constants.smsParam.REQUESTHEDER) == null ? "" : smsDispatcherMap.get(Constants.smsParam.REQUESTHEDER));

			try
			{
				String responseXml = Utilities.postXML(createUserParamMap);
				log.info("----- responseXml : -------" + responseXml);
				responseJSON.put(JsonConstants.DATA,"");
				responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
				responseJSON.put(JsonConstants.MESSAGE, "Collection sms resend successfully!");
			}
			catch (Exception e)
			{
				log.info("Response :- " + e);
			}
		
			
		}
		catch(Exception e)
		{
			log.info("Exception"+e);
			e.printStackTrace();
			responseJSON.put(JsonConstants.DATA,"");
			responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
			responseJSON.put(JsonConstants.MESSAGE, "Failed to send resend Sms");
		}
		return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
	
	}
	
}
