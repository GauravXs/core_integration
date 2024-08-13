/**
 ****************************************************************************** 
 * C O P Y R I G H T A N D C O N F I D E N T I A L I T Y N O T I C E
 * <p>
 * Copyright � 2008-2009 Mobicule Technologies Pvt. Ltd. All rights reserved.
 * This is proprietary information of Mobicule Technologies Pvt. Ltd.and is
 * subject to applicable licensing agreements. Unauthorized reproduction,
 * transmission or distribution of this file and its contents is a violation of
 * applicable laws.
 ****************************************************************************** 
 * 
 * @project mSalesMGLServer
 */
package com.mobicule.mcollections.integration.commons;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

import com.mobicule.component.usermapping.bean.Territory;
import com.mobicule.mcollections.core.beans.Collection;
import com.mobicule.mcollections.core.beans.Denomination;
import com.mobicule.mcollections.core.beans.Settlement;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.Utilities;

public class ServerUtilities
{
	public static SystemUser extractSystemUser(JSONObject requestSystemUser)
	{

		SystemUser systemUser = new SystemUser();
		try
		{

			if (requestSystemUser.has("mobileNumber"))
			{
				systemUser.setMobileNumber(requestSystemUser.getString("mobileNumber"));
			}
			else
			{
				systemUser.setMobileNumber("");
			}

			if (requestSystemUser.has(JsonConstants.CC_APAC))
			{
				systemUser.setCcapac(requestSystemUser.getString(JsonConstants.CC_APAC));
			}
			else
			{
				systemUser.setCcapac("");
			}

			if (requestSystemUser.has(JsonConstants.EMAIL_ADDRESS))
			{
				systemUser.setEmailAddress(requestSystemUser.getString(JsonConstants.EMAIL_ADDRESS));
			}
			else
			{
				systemUser.setEmailAddress("");
			}

			if (requestSystemUser.has(JsonConstants.AGENCY_ID))
			{
				systemUser.setAgencyId(Long.parseLong(requestSystemUser.getString(JsonConstants.AGENCY_ID)));
			}
			if (requestSystemUser.has(JsonConstants.SYSTEM_USER_ID))
			{
				systemUser.setUserTableId(Long.parseLong(requestSystemUser.getString(JsonConstants.SYSTEM_USER_ID)));
			}
			else
			{
				systemUser.setUserTableId(0L);
			}

			systemUser.setImeiNo(requestSystemUser.getString(JsonConstants.IMEI_NUMBER));

			systemUser.setPassword(requestSystemUser.getString(JsonConstants.PASSWORD));

			systemUser.setUsername(requestSystemUser.getString(JsonConstants.USERNAME));
			
			//Added for Scratch sync report api
			systemUser.setFirstName(requestSystemUser.has("firstName") ? requestSystemUser.getString("firstName") : Constants.EMPTY_STRING);
			
			systemUser.setLastName(requestSystemUser.has("lastName") ? requestSystemUser.getString("lastName") : Constants.EMPTY_STRING);
			

			
			if(requestSystemUser.has("terrCode"))
			{
				try
				{
					
					JSONArray jArray = (JSONArray) requestSystemUser.getJSONArray("terrCode");
					List<Territory> territoryList = new ArrayList<Territory>();
					for (int i = 0; i < jArray.length(); i++)
					{
						Territory territory = new Territory();
						territory.setCode(jArray.get(i).toString());
						territoryList.add(territory);
					}
					systemUser.setTerritoryList(territoryList);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			
			/*if (requestSystemUser.has(JsonConstants.SYSTEM_USER_ID))
			{
				systemUser.setUserTableId(Long.parseLong(requestSystemUser.getString(JsonConstants.SYSTEM_USER_ID)));
			}*/

		}
		catch (JSONException e)
		{
			e.printStackTrace();

		}

		return systemUser;
	}

	public static Map<String, String> extractStreetwalkSequenceParameters(JSONObject requestStreetWalkSequence)
	{
		Map<String, String> streetwalkSequenceParametersMap = new HashMap<String, String>();

		try
		{
			streetwalkSequenceParametersMap.put(JsonConstants.STREETWALK_SEQUENCE_NAME,
					(requestStreetWalkSequence.getString(JsonConstants.STREETWALK_SEQUENCE_NAME)));
			streetwalkSequenceParametersMap.put(JsonConstants.STREETWALK_SEQUENCE_VERSION,
					(requestStreetWalkSequence.getString(JsonConstants.STREETWALK_SEQUENCE_VERSION)));
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

		return streetwalkSequenceParametersMap;
	}

	public static Map<String, Object> generateSMSDispatcherMap(String amount, String receiptNumber, String paymentType,
			String mobileNumber, String type, String apacCardNumber)
	{
		StringBuilder smsText = new StringBuilder();

		if (paymentType.equalsIgnoreCase("debit"))
		{

			smsText.append("Dear Customer, Thank you for payment of Rs.");
			smsText.append(amount);

			if (type.equalsIgnoreCase("RSM"))
			{

				smsText.append(" towards Phoenix ");

			}
			else
			{

				smsText.append(" towards Kotak ");

			}

			if (type != null && type.equalsIgnoreCase(Constants.APPL_CARD))
			{
				smsText.append("Credit Card" + " ");
				smsText.append(generateApacForSMS(apacCardNumber.substring((apacCardNumber.length()) - 3)));
			}
			else
			{
				smsText.append(type + " ");
				smsText.append(generateApacForSMS(apacCardNumber));
			}

			smsText.append(" received in Debit Card");

			smsText.append(" via receipt no ");
			smsText.append(receiptNumber);
			smsText.append(" on ");
			smsText.append(generateDateTimeForSMS(new Timestamp(System.currentTimeMillis()).toString()));

			smsText.append("Please note that Merchant Discount Rate of Rs ");
			smsText.append("has been additionally charged on your debit card Transaction.");

		}
		else
		{

			smsText.append("Dear Customer, Thank you for payment of Rs.");
			smsText.append(amount);

			if (type.equalsIgnoreCase("RSM"))
			{

				smsText.append(" towards Phoenix ");

			}
			else
			{

				smsText.append(" towards Kotak ");

			}

			if (type != null && type.equalsIgnoreCase(Constants.APPL_CARD))
			{
				smsText.append("Credit Card" + " ");
				smsText.append(generateApacForSMS(apacCardNumber.substring((apacCardNumber.length()) - 3)));
			}
			else
			{
				smsText.append(type + " ");
				smsText.append(generateApacForSMS(apacCardNumber));
			}

			smsText.append(" received in ");

			if (paymentType.equalsIgnoreCase("CSH"))
			{
				smsText.append("CASH");
			}
			else if (paymentType.equalsIgnoreCase("CHQ"))
			{
				smsText.append("CHEQUE");
			}
			else
			{
				smsText.append(paymentType);
			}
			smsText.append(" via receipt no ");
			smsText.append(receiptNumber);
			smsText.append(" on ");
			smsText.append(generateDateTimeForSMS(new Timestamp(System.currentTimeMillis()).toString()));

		}

		/* commented by bhushan for new SMS verbiage for prime */
		
		List<String> headerKeyList = new ArrayList<String>();
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQID_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY);

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY, XMLConstants.SMS_DISPATCHER_MCOLL_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_REQID_KEY, System.currentTimeMillis());
		headerMap.put(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY, XMLConstants.SMS_DISPATCHER_COLLECTION_VALUE);

		String headerListString = Utilities.generateXML(headerKeyList, headerMap).toString();

		List<String> bodyKeyList = new ArrayList<String>();
		bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY);
		bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTO_KEY);

		Map<String, Object> bodyMap = new HashMap<String, Object>();
		bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY, smsText.toString().replaceAll("&", "&amp;"));
		bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTO_KEY, mobileNumber);

		String bodyListString = Utilities.generateXML(bodyKeyList, bodyMap).toString();

		List<String> posKeyList = new ArrayList<String>();
		posKeyList.add(XMLConstants.SMS_DISPATCHER_HEADER_KEY);
		posKeyList.add(XMLConstants.SMS_DISPATCHER_BODY_KEY);

		Map<String, Object> posMap = new HashMap<String, Object>();
		posMap.put(XMLConstants.SMS_DISPATCHER_HEADER_KEY, headerListString);
		posMap.put(XMLConstants.SMS_DISPATCHER_BODY_KEY, bodyListString);

		String posListString = Utilities.generateXML(posKeyList, posMap).toString();

		Map<String, Object> mainPOSMap = new HashMap<String, Object>();

		mainPOSMap.put(XMLConstants.SMS_DISPATCHER_POS_KEY, posListString);

		return mainPOSMap;	
		
		/*List<String> headerKeyList = new ArrayList<String>();
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SOURCEAPPID_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SMSVENDOR_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_UNIQUEREFNO_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_PRIORITY_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_FROM_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_TONUMBER_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_MESSAGE_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_INTNO_KEY);

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put(XMLConstants.SMS_DISPATCHER_SOURCEAPPID_KEY, XMLConstants.SMS_DISPATCHER_MCOLL_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_SMSVENDOR_KEY, XMLConstants.SMS_DISPATCHER_SMSVENDOR_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_UNIQUEREFNO_KEY, System.currentTimeMillis());
		headerMap.put(XMLConstants.SMS_DISPATCHER_PRIORITY_KEY, XMLConstants.SMS_DISPATCHER_PRIORITYANDINTNO_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_FROM_KEY, XMLConstants.SMS_DISPATCHER_FROM_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_TONUMBER_KEY, mobileNumber);
		headerMap.put(XMLConstants.SMS_DISPATCHER_MESSAGE_KEY, smsText.toString().replaceAll("&", "&amp;"));
		headerMap.put(XMLConstants.SMS_DISPATCHER_INTNO_KEY, XMLConstants.SMS_DISPATCHER_PRIORITYANDINTNO_VALUE);

		String headerListString = Utilities.generateXML(headerKeyList, headerMap).toString();

		Map<String, Object> mainPOSMap = new HashMap<String, Object>();

		mainPOSMap.put(XMLConstants.SMS_DISPATCHER_SMSAPIREQ_KEY, headerListString);

		return mainPOSMap;
*/
	}
	
	public static Map<String, Object> generateSMSDispatcherMapForDebit(String amount, String receiptNumber, String paymentType,
			String mobileNumber, String type, String apacCardNumber,Collection collection)
	{
		StringBuilder smsText = new StringBuilder();

		if (paymentType.equalsIgnoreCase("ORI"))
		{

			smsText.append("Dear Customer, Thank you for payment of Rs.");
			smsText.append(amount);

			if (type.equalsIgnoreCase("RSM"))
			{

				smsText.append(" towards Phoenix ");

			}
			else
			{

				smsText.append(" towards Kotak ");

			}

			if (type != null && type.equalsIgnoreCase(Constants.APPL_CARD))
			{
				smsText.append("Credit Card" + " ");
				smsText.append(generateApacForSMS(apacCardNumber.substring((apacCardNumber.length()) - 3)));
			}
			else
			{
				smsText.append(type + " ");
				smsText.append(generateApacForSMS(apacCardNumber));
			}

			smsText.append(" received in Debit Card");

			smsText.append(" via receipt no ");
			smsText.append(receiptNumber);
			smsText.append(" on ");
			smsText.append(generateDateTimeForSMS(new Timestamp(System.currentTimeMillis()).toString()));

			smsText.append(". Please note that Merchant Discount Rate of Rs.");
			smsText.append(collection.getMposDetail().getMdrAmnt());
			smsText.append(" has been additionally charged on your debit card Transaction.");

		}
		else
		{

			smsText.append("Dear Customer, Thank you for payment of Rs.");
			smsText.append(amount);

			if (type.equalsIgnoreCase("RSM"))
			{

				smsText.append(" towards Phoenix ");

			}
			else
			{

				smsText.append(" towards Kotak ");

			}

			if (type != null && type.equalsIgnoreCase(Constants.APPL_CARD))
			{
				smsText.append("Credit Card" + " ");
				smsText.append(generateApacForSMS(apacCardNumber.substring((apacCardNumber.length()) - 3)));
			}
			else
			{
				smsText.append(type + " ");
				smsText.append(generateApacForSMS(apacCardNumber));
			}

			smsText.append(" received in ");

			if (paymentType.equalsIgnoreCase("CSH"))
			{
				smsText.append("CASH");
			}
			else if (paymentType.equalsIgnoreCase("CHQ"))
			{
				smsText.append("CHEQUE");
			}
			else
			{
				smsText.append(paymentType);
			}
			smsText.append(" via receipt no ");
			smsText.append(receiptNumber);
			smsText.append(" on ");
			smsText.append(generateDateTimeForSMS(new Timestamp(System.currentTimeMillis()).toString()));

		}

		/* commented by bhushan for new SMS verbiage for prime */
		
		List<String> headerKeyList = new ArrayList<String>();
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQID_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY);

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY, XMLConstants.SMS_DISPATCHER_MCOLL_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_REQID_KEY, System.currentTimeMillis());
		headerMap.put(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY, XMLConstants.SMS_DISPATCHER_COLLECTION_VALUE);

		String headerListString = Utilities.generateXML(headerKeyList, headerMap).toString();

		List<String> bodyKeyList = new ArrayList<String>();
		bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY);
		bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTO_KEY);

		Map<String, Object> bodyMap = new HashMap<String, Object>();
		bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY, smsText.toString().replaceAll("&", "&amp;"));
		bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTO_KEY, mobileNumber);

		String bodyListString = Utilities.generateXML(bodyKeyList, bodyMap).toString();

		List<String> posKeyList = new ArrayList<String>();
		posKeyList.add(XMLConstants.SMS_DISPATCHER_HEADER_KEY);
		posKeyList.add(XMLConstants.SMS_DISPATCHER_BODY_KEY);

		Map<String, Object> posMap = new HashMap<String, Object>();
		posMap.put(XMLConstants.SMS_DISPATCHER_HEADER_KEY, headerListString);
		posMap.put(XMLConstants.SMS_DISPATCHER_BODY_KEY, bodyListString);

		String posListString = Utilities.generateXML(posKeyList, posMap).toString();

		Map<String, Object> mainPOSMap = new HashMap<String, Object>();

		mainPOSMap.put(XMLConstants.SMS_DISPATCHER_POS_KEY, posListString);

		return mainPOSMap;
		
		/*List<String> headerKeyList = new ArrayList<String>();
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SOURCEAPPID_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SMSVENDOR_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_UNIQUEREFNO_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_PRIORITY_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_FROM_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_TONUMBER_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_MESSAGE_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_INTNO_KEY);

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put(XMLConstants.SMS_DISPATCHER_SOURCEAPPID_KEY, XMLConstants.SMS_DISPATCHER_MCOLL_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_SMSVENDOR_KEY, XMLConstants.SMS_DISPATCHER_SMSVENDOR_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_UNIQUEREFNO_KEY, System.currentTimeMillis());
		headerMap.put(XMLConstants.SMS_DISPATCHER_PRIORITY_KEY, XMLConstants.SMS_DISPATCHER_PRIORITYANDINTNO_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_FROM_KEY, XMLConstants.SMS_DISPATCHER_FROM_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_TONUMBER_KEY, mobileNumber);
		headerMap.put(XMLConstants.SMS_DISPATCHER_MESSAGE_KEY, smsText.toString().replaceAll("&", "&amp;"));
		headerMap.put(XMLConstants.SMS_DISPATCHER_INTNO_KEY, XMLConstants.SMS_DISPATCHER_PRIORITYANDINTNO_VALUE);

		String headerListString = Utilities.generateXML(headerKeyList, headerMap).toString();

		Map<String, Object> mainPOSMap = new HashMap<String, Object>();

		mainPOSMap.put(XMLConstants.SMS_DISPATCHER_SMSAPIREQ_KEY, headerListString);

		return mainPOSMap;*/

	}

	public static Map<String, Object> generateSMSDispatcherMapForFE(String amount, String receiptNumber,
			String paymentType, String mobileNumber, String type, String feName)
	{
		StringBuilder smsText = new StringBuilder();
		smsText.append("The payment for Rs. ");
		smsText.append(amount);
		smsText.append(" against the receipt no: ");
		smsText.append(receiptNumber);
		smsText.append(" has been successfully processed by your Kotak Mahindra Mobile Payment Application towards ");
		smsText.append(type);
		smsText.append(" of Mr./Mrs./Miss. ");
		smsText.append(feName);
		
		/* commented by bhushan for new SMS verbiage for prime */

		List<String> headerKeyList = new ArrayList<String>();
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQID_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY);

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY, XMLConstants.SMS_DISPATCHER_MCOLL_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_REQID_KEY, System.currentTimeMillis());
		headerMap.put(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY, XMLConstants.SMS_DISPATCHER_COLLECTION_VALUE);

		String headerListString = Utilities.generateXML(headerKeyList, headerMap).toString();

		List<String> bodyKeyList = new ArrayList<String>();
		bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY);
		bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTO_KEY);

		Map<String, Object> bodyMap = new HashMap<String, Object>();
		bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY, smsText.toString().replaceAll("&", "&amp;"));
		bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTO_KEY, mobileNumber);

		String bodyListString = Utilities.generateXML(bodyKeyList, bodyMap).toString();

		List<String> posKeyList = new ArrayList<String>();
		posKeyList.add(XMLConstants.SMS_DISPATCHER_HEADER_KEY);
		posKeyList.add(XMLConstants.SMS_DISPATCHER_BODY_KEY);

		Map<String, Object> posMap = new HashMap<String, Object>();
		posMap.put(XMLConstants.SMS_DISPATCHER_HEADER_KEY, headerListString);
		posMap.put(XMLConstants.SMS_DISPATCHER_BODY_KEY, bodyListString);

		String posListString = Utilities.generateXML(posKeyList, posMap).toString();

		Map<String, Object> mainPOSMap = new HashMap<String, Object>();

		mainPOSMap.put(XMLConstants.SMS_DISPATCHER_POS_KEY, posListString);

		return mainPOSMap;
		
		/*List<String> headerKeyList = new ArrayList<String>();
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SOURCEAPPID_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SMSVENDOR_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_UNIQUEREFNO_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_PRIORITY_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_FROM_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_TONUMBER_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_MESSAGE_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_INTNO_KEY);

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put(XMLConstants.SMS_DISPATCHER_SOURCEAPPID_KEY, XMLConstants.SMS_DISPATCHER_MCOLL_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_SMSVENDOR_KEY, XMLConstants.SMS_DISPATCHER_SMSVENDOR_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_UNIQUEREFNO_KEY, System.currentTimeMillis());
		headerMap.put(XMLConstants.SMS_DISPATCHER_PRIORITY_KEY, XMLConstants.SMS_DISPATCHER_PRIORITYANDINTNO_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_FROM_KEY, XMLConstants.SMS_DISPATCHER_FROM_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_TONUMBER_KEY, mobileNumber);
		headerMap.put(XMLConstants.SMS_DISPATCHER_MESSAGE_KEY, smsText.toString().replaceAll("&", "&amp;"));
		headerMap.put(XMLConstants.SMS_DISPATCHER_INTNO_KEY, XMLConstants.SMS_DISPATCHER_PRIORITYANDINTNO_VALUE);

		String headerListString = Utilities.generateXML(headerKeyList, headerMap).toString();

		Map<String, Object> mainPOSMap = new HashMap<String, Object>();

		mainPOSMap.put(XMLConstants.SMS_DISPATCHER_SMSAPIREQ_KEY, headerListString);

		return mainPOSMap;*/
	}

	//----------method to genrate SMS for OTP
	public static Map<String, Object> genrateSMSForOTP(String OTP, SystemUser user)
	{

		StringBuilder smsText = new StringBuilder();
		smsText.append("Dear Supervisor, your reportee having USERNAME ");
		smsText.append(user.getUsername());
		smsText.append(" has raised a forgot password request. Find the OTP ");
		smsText.append(OTP);
		smsText.append(" generated for the user for sharing it with him/her.");

		/* commented by bhushan for new SMS verbiage for prime */
		
		List<String> headerKeyList = new ArrayList<String>();
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQID_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY);

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY, XMLConstants.SMS_DISPATCHER_MCOLL_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_REQID_KEY, System.currentTimeMillis());
		headerMap.put(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY, XMLConstants.SMS_DISPATCHER_COLLECTION_VALUE);

		String headerListString = Utilities.generateXML(headerKeyList, headerMap).toString();

		List<String> bodyKeyList = new ArrayList<String>();
		bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY);
		bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTO_KEY);

		Map<String, Object> bodyMap = new HashMap<String, Object>();
		bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY, smsText.toString().replaceAll("&", "&amp;"));
		bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTO_KEY, user.getSupervisorMobileNumber());

		String bodyListString = Utilities.generateXML(bodyKeyList, bodyMap).toString();

		List<String> posKeyList = new ArrayList<String>();
		posKeyList.add(XMLConstants.SMS_DISPATCHER_HEADER_KEY);
		posKeyList.add(XMLConstants.SMS_DISPATCHER_BODY_KEY);

		Map<String, Object> posMap = new HashMap<String, Object>();
		posMap.put(XMLConstants.SMS_DISPATCHER_HEADER_KEY, headerListString);
		posMap.put(XMLConstants.SMS_DISPATCHER_BODY_KEY, bodyListString);

		String posListString = Utilities.generateXML(posKeyList, posMap).toString();

		Map<String, Object> mainPOSMap = new HashMap<String, Object>();

		mainPOSMap.put(XMLConstants.SMS_DISPATCHER_POS_KEY, posListString);

		return mainPOSMap;
		
		/*List<String> headerKeyList = new ArrayList<String>();
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SOURCEAPPID_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SMSVENDOR_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_UNIQUEREFNO_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_PRIORITY_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_FROM_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_TONUMBER_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_MESSAGE_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_INTNO_KEY);

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put(XMLConstants.SMS_DISPATCHER_SOURCEAPPID_KEY, XMLConstants.SMS_DISPATCHER_MCOLL_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_SMSVENDOR_KEY, XMLConstants.SMS_DISPATCHER_SMSVENDOR_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_UNIQUEREFNO_KEY, System.currentTimeMillis());
		headerMap.put(XMLConstants.SMS_DISPATCHER_PRIORITY_KEY, XMLConstants.SMS_DISPATCHER_PRIORITYANDINTNO_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_FROM_KEY, XMLConstants.SMS_DISPATCHER_FROM_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_TONUMBER_KEY, user.getSupervisorMobileNumber());
		headerMap.put(XMLConstants.SMS_DISPATCHER_MESSAGE_KEY, smsText.toString().replaceAll("&", "&amp;"));
		headerMap.put(XMLConstants.SMS_DISPATCHER_INTNO_KEY, XMLConstants.SMS_DISPATCHER_PRIORITYANDINTNO_VALUE);

		String headerListString = Utilities.generateXML(headerKeyList, headerMap).toString();

		Map<String, Object> mainPOSMap = new HashMap<String, Object>();

		mainPOSMap.put(XMLConstants.SMS_DISPATCHER_SMSAPIREQ_KEY, headerListString);

		return mainPOSMap;*/
	}

	public static Map<String, Object> generateSMSDispatcherMapForNonRTP(Collection collection, String mobileNumber)
			throws ParseException
	{
		StringBuilder smsText = new StringBuilder();
		if (collection.getCollectionCode().equalsIgnoreCase("PTP"))
		{
			SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat format2 = new SimpleDateFormat("dd/MMM/yyyy");
			Date date = format1.parse(collection.getRevisitDate());

			String APAC = collection.getBusinessPartnerNumber();
			String appl = collection.getAppl();
			APAC = APAC.replace(appl, "");
			APAC = appl + APAC;

			smsText.append("Dear Customer, You had promised to make payments of Rs ");
			smsText.append(collection.getPtpAmount());
			smsText.append(" vide Cash/Cheque/DD/PO/NET on ");
			smsText.append(format2.format(date));

			smsText.append(" towards your ");
			if (collection.getAppl().equalsIgnoreCase("CWO"))
			{
				smsText.append(" card no. ");
			}
			else
			{
				smsText.append(" loan a/c ");
			}
			smsText.append(APAC);
			smsText.append(".");
		}
		else if (collection.getCollectionCode().equalsIgnoreCase("DL"))
		{
			String APAC = collection.getBusinessPartnerNumber();
			String appl = collection.getAppl();
			APAC = APAC.replace(appl, "");
			APAC = appl + APAC;

			smsText.append("Dear Customer, You were not available when our representative visited your address for follow-up of overdue payment towards your");

			if (collection.getAppl().equalsIgnoreCase("CWO"))
			{
				smsText.append(" card no. ");
			}
			else
			{
				smsText.append(" loan a/c ");
			}

			smsText.append(APAC);
			smsText.append(".");
		}
		else if (collection.getCollectionCode().equalsIgnoreCase("BRP"))
		{
			SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat format2 = new SimpleDateFormat("dd/MMM/yyyy");
			Date date = format1.parse(collection.getRevisitDate());

			String APAC = collection.getBusinessPartnerNumber();
			String appl = collection.getAppl();
			APAC = APAC.replace(appl, "");
			APAC = appl + APAC;

			smsText.append("Dear Customer, You have failed to honor your promise made for payment of Rs ");
			smsText.append(collection.getPtpAmount());
			smsText.append(" on ");
			smsText.append(format2.format(date));

			smsText.append(" towards your");

			if (collection.getAppl().equalsIgnoreCase("CWO"))
			{
				smsText.append(" card no. ");
			}
			else
			{
				smsText.append(" loan a/c ");
			}

			smsText.append(APAC);
			smsText.append(".");
		}

		/* commented by bhushan for new SMS verbiage for prime */
		
		List<String> headerKeyList = new ArrayList<String>();
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQID_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY);

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY, XMLConstants.SMS_DISPATCHER_MCOLL_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_REQID_KEY, System.currentTimeMillis());
		headerMap.put(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY, XMLConstants.SMS_DISPATCHER_COLLECTION_VALUE);

		String headerListString = Utilities.generateXML(headerKeyList, headerMap).toString();

		List<String> bodyKeyList = new ArrayList<String>();
		bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY);
		bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTO_KEY);

		Map<String, Object> bodyMap = new HashMap<String, Object>();
		bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY, smsText.toString().replaceAll("&", "&amp;"));
		bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTO_KEY, mobileNumber);

		String bodyListString = Utilities.generateXML(bodyKeyList, bodyMap).toString();

		List<String> posKeyList = new ArrayList<String>();
		posKeyList.add(XMLConstants.SMS_DISPATCHER_HEADER_KEY);
		posKeyList.add(XMLConstants.SMS_DISPATCHER_BODY_KEY);

		Map<String, Object> posMap = new HashMap<String, Object>();
		posMap.put(XMLConstants.SMS_DISPATCHER_HEADER_KEY, headerListString);
		posMap.put(XMLConstants.SMS_DISPATCHER_BODY_KEY, bodyListString);

		String posListString = Utilities.generateXML(posKeyList, posMap).toString();

		Map<String, Object> mainPOSMap = new HashMap<String, Object>();

		mainPOSMap.put(XMLConstants.SMS_DISPATCHER_POS_KEY, posListString);

		return mainPOSMap;	
		
		
		/*List<String> headerKeyList = new ArrayList<String>();
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SOURCEAPPID_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SMSVENDOR_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_UNIQUEREFNO_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_PRIORITY_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_FROM_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_TONUMBER_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_MESSAGE_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_INTNO_KEY);

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put(XMLConstants.SMS_DISPATCHER_SOURCEAPPID_KEY, XMLConstants.SMS_DISPATCHER_MCOLL_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_SMSVENDOR_KEY, XMLConstants.SMS_DISPATCHER_SMSVENDOR_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_UNIQUEREFNO_KEY, System.currentTimeMillis());
		headerMap.put(XMLConstants.SMS_DISPATCHER_PRIORITY_KEY, XMLConstants.SMS_DISPATCHER_PRIORITYANDINTNO_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_FROM_KEY, XMLConstants.SMS_DISPATCHER_FROM_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_TONUMBER_KEY, mobileNumber);
		headerMap.put(XMLConstants.SMS_DISPATCHER_MESSAGE_KEY, smsText.toString().replaceAll("&", "&amp;"));
		headerMap.put(XMLConstants.SMS_DISPATCHER_INTNO_KEY, XMLConstants.SMS_DISPATCHER_PRIORITYANDINTNO_VALUE);

		String headerListString = Utilities.generateXML(headerKeyList, headerMap).toString();

		Map<String, Object> mainPOSMap = new HashMap<String, Object>();

		mainPOSMap.put(XMLConstants.SMS_DISPATCHER_SMSAPIREQ_KEY, headerListString);

		return mainPOSMap;*/

	}

	public static Map<String, Object> generateSMSDispatcherMapFEForNonRTP(Collection collection, String mobileNumber)
			throws ParseException
	{
		StringBuilder smsText = new StringBuilder();
		if (collection.getCollectionCode().equalsIgnoreCase("PTP"))
		{
			SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat format2 = new SimpleDateFormat("dd/MMM/yyyy");
			Date date = format1.parse(collection.getRevisitDate());

			String APAC = collection.getBusinessPartnerNumber();
			String appl = collection.getAppl();
			APAC = APAC.replace(appl, "");
			APAC = appl + APAC;

			smsText.append("Mr/Mrs/Ms ");
			smsText.append(collection.getPartyName());
			smsText.append(" has promised to make payment of Rs ");
			smsText.append(collection.getPtpAmount());
			smsText.append(" vide Cash/Cheque/DD/PO/NET on ");
			smsText.append(format2.format(date));

			smsText.append(" towards a/c ");
			smsText.append(APAC);
			smsText.append(".");
		}
		else if (collection.getCollectionCode().equalsIgnoreCase("DL"))
		{
			String APAC = collection.getBusinessPartnerNumber();
			String appl = collection.getAppl();
			APAC = APAC.replace(appl, "");
			APAC = appl + APAC;

			smsText.append("Mr/Mrs/Ms ");
			smsText.append(collection.getPartyName());
			smsText.append(" was not available during follow-up for overdue payments towards a/c ");
			smsText.append(APAC);
			smsText.append(".");
		}
		else if (collection.getCollectionCode().equalsIgnoreCase("BRP"))
		{
			SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat format2 = new SimpleDateFormat("dd/MMM/yyyy");
			Date date = format1.parse(collection.getRevisitDate());

			String APAC = collection.getBusinessPartnerNumber();
			String appl = collection.getAppl();
			APAC = APAC.replace(appl, "");
			APAC = appl + APAC;

			smsText.append("Mr/Mrs/Ms ");
			smsText.append(collection.getPartyName());
			smsText.append(" has failed to make payment of Rs ");
			smsText.append(collection.getPtpAmount());
			smsText.append(" on ");
			smsText.append(format2.format(date));

			smsText.append(" towards a/c ");
			smsText.append(APAC);
			smsText.append(".");
		}
		
		/* commented by bhushan for new SMS verbiage for prime */

		List<String> headerKeyList = new ArrayList<String>();
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQID_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY);

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY, XMLConstants.SMS_DISPATCHER_MCOLL_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_REQID_KEY, System.currentTimeMillis());
		headerMap.put(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY, XMLConstants.SMS_DISPATCHER_COLLECTION_VALUE);

		String headerListString = Utilities.generateXML(headerKeyList, headerMap).toString();

		List<String> bodyKeyList = new ArrayList<String>();
		bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY);
		bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTO_KEY);

		Map<String, Object> bodyMap = new HashMap<String, Object>();
		bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY, smsText.toString().replaceAll("&", "&amp;"));
		bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTO_KEY, mobileNumber);

		String bodyListString = Utilities.generateXML(bodyKeyList, bodyMap).toString();

		List<String> posKeyList = new ArrayList<String>();
		posKeyList.add(XMLConstants.SMS_DISPATCHER_HEADER_KEY);
		posKeyList.add(XMLConstants.SMS_DISPATCHER_BODY_KEY);

		Map<String, Object> posMap = new HashMap<String, Object>();
		posMap.put(XMLConstants.SMS_DISPATCHER_HEADER_KEY, headerListString);
		posMap.put(XMLConstants.SMS_DISPATCHER_BODY_KEY, bodyListString);

		String posListString = Utilities.generateXML(posKeyList, posMap).toString();

		Map<String, Object> mainPOSMap = new HashMap<String, Object>();

		mainPOSMap.put(XMLConstants.SMS_DISPATCHER_POS_KEY, posListString);

		return mainPOSMap;
		
		/*List<String> headerKeyList = new ArrayList<String>();
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SOURCEAPPID_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SMSVENDOR_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_UNIQUEREFNO_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_PRIORITY_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_FROM_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_TONUMBER_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_MESSAGE_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_INTNO_KEY);

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put(XMLConstants.SMS_DISPATCHER_SOURCEAPPID_KEY, XMLConstants.SMS_DISPATCHER_MCOLL_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_SMSVENDOR_KEY, XMLConstants.SMS_DISPATCHER_SMSVENDOR_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_UNIQUEREFNO_KEY, System.currentTimeMillis());
		headerMap.put(XMLConstants.SMS_DISPATCHER_PRIORITY_KEY, XMLConstants.SMS_DISPATCHER_PRIORITYANDINTNO_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_FROM_KEY, XMLConstants.SMS_DISPATCHER_FROM_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_TONUMBER_KEY, mobileNumber);
		headerMap.put(XMLConstants.SMS_DISPATCHER_MESSAGE_KEY, smsText.toString().replaceAll("&", "&amp;"));
		headerMap.put(XMLConstants.SMS_DISPATCHER_INTNO_KEY, XMLConstants.SMS_DISPATCHER_PRIORITYANDINTNO_VALUE);

		String headerListString = Utilities.generateXML(headerKeyList, headerMap).toString();

		Map<String, Object> mainPOSMap = new HashMap<String, Object>();

		mainPOSMap.put(XMLConstants.SMS_DISPATCHER_SMSAPIREQ_KEY, headerListString);

		return mainPOSMap;*/
	}

	public static String generateApacForSMS(String apac)
	{
		String smsApacFormat = "";
		if ((!apac.isEmpty()) && (apac.length() > 0))
		{
			if (apac.length() < 3)
			{
				return apac;
			}
			for (int i = 0; i < apac.length() - 3; i++)
			{
				smsApacFormat = smsApacFormat + "*";
			}

			smsApacFormat = smsApacFormat + apac.substring(apac.length() - 3);
		}
		return smsApacFormat;
	}

	private static String generateDateTimeForSMS(String apac)
	{
		return apac.substring(0, apac.lastIndexOf('.'));
	}

	public static Map<String, Object> generateSMSDispatcherMapForSettlement(Settlement settlement, String mobileNumber,
			String Action) throws ParseException
	{

		StringBuilder smsText = new StringBuilder();

		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat format2 = new SimpleDateFormat("dd/MMM/yyyy");

		smsText.append("We would like to inform you that the settlement request Via Reference No "
				+ settlement.getRequId());
		smsText.append(" has been ");
		smsText.append(Action);
		smsText.append(" against the Kotak ");

		if (settlement.getAppl().equalsIgnoreCase("CWO"))
		{

			smsText.append("Credit Card No ");

		}
		else
		{
			smsText.append(" loan a/c no ");

		}
		smsText.append(settlement.getApacNo());

		smsText.append(".");
		
		/* commented by bhushan for new SMS verbiage for prime */
		
		List<String> headerKeyList = new ArrayList<String>();
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQID_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY);

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY, XMLConstants.SMS_DISPATCHER_MCOLL_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_REQID_KEY, System.currentTimeMillis());
		headerMap.put(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY, XMLConstants.SMS_DISPATCHER_COLLECTION_VALUE);

		String headerListString = Utilities.generateXML(headerKeyList, headerMap).toString();

		List<String> bodyKeyList = new ArrayList<String>();
		bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY);
		bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTO_KEY);

		Map<String, Object> bodyMap = new HashMap<String, Object>();
		bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY, smsText.toString().replaceAll("&", "&amp;"));
		bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTO_KEY, mobileNumber);

		String bodyListString = Utilities.generateXML(bodyKeyList, bodyMap).toString();

		List<String> posKeyList = new ArrayList<String>();
		posKeyList.add(XMLConstants.SMS_DISPATCHER_HEADER_KEY);
		posKeyList.add(XMLConstants.SMS_DISPATCHER_BODY_KEY);

		Map<String, Object> posMap = new HashMap<String, Object>();
		posMap.put(XMLConstants.SMS_DISPATCHER_HEADER_KEY, headerListString);
		posMap.put(XMLConstants.SMS_DISPATCHER_BODY_KEY, bodyListString);

		String posListString = Utilities.generateXML(posKeyList, posMap).toString();

		Map<String, Object> mainPOSMap = new HashMap<String, Object>();

		mainPOSMap.put(XMLConstants.SMS_DISPATCHER_POS_KEY, posListString);

		return mainPOSMap;
		
		
		/*List<String> headerKeyList = new ArrayList<String>();
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SOURCEAPPID_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SMSVENDOR_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_UNIQUEREFNO_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_PRIORITY_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_FROM_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_TONUMBER_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_MESSAGE_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_INTNO_KEY);

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put(XMLConstants.SMS_DISPATCHER_SOURCEAPPID_KEY, XMLConstants.SMS_DISPATCHER_MCOLL_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_SMSVENDOR_KEY, XMLConstants.SMS_DISPATCHER_SMSVENDOR_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_UNIQUEREFNO_KEY, System.currentTimeMillis());
		headerMap.put(XMLConstants.SMS_DISPATCHER_PRIORITY_KEY, XMLConstants.SMS_DISPATCHER_PRIORITYANDINTNO_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_FROM_KEY, XMLConstants.SMS_DISPATCHER_FROM_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_TONUMBER_KEY, mobileNumber);
		headerMap.put(XMLConstants.SMS_DISPATCHER_MESSAGE_KEY, smsText.toString().replaceAll("&", "&amp;"));
		headerMap.put(XMLConstants.SMS_DISPATCHER_INTNO_KEY, XMLConstants.SMS_DISPATCHER_PRIORITYANDINTNO_VALUE);

		String headerListString = Utilities.generateXML(headerKeyList, headerMap).toString();

		Map<String, Object> mainPOSMap = new HashMap<String, Object>();

		mainPOSMap.put(XMLConstants.SMS_DISPATCHER_SMSAPIREQ_KEY, headerListString);

		return mainPOSMap;*/

	}

	public static Map<String, Object> generateSMSForOldDenominination(String receiptNumber, String mobileNumber,
			Collection collection)
	{
		StringBuilder smsText = new StringBuilder();

		String old500Count = "0";
		String old1000Count = "0";

		if (collection.getDenomination() != null)
		{

			for (Denomination denomination : collection.getDenomination())
			{

				if (denomination.getNote().contains("500-Old Note"))
					old500Count = String.valueOf(denomination.getNoteCount());

				if (denomination.getNote().contains("1000"))
					old1000Count = String.valueOf(denomination.getNoteCount());
			}

		}

		smsText.append("Dear Customer, Thank you for payment");

		if (collection.getAppl() != null && collection.getAppl().equalsIgnoreCase(Constants.APPL_CARD))
		{
			smsText.append(" towards Kotak Card" + " ");
			smsText.append(collection.getAppl() + " ");
			smsText.append(generateApacForSMS(collection.getBusinessPartnerNumber()));
		}

		else if (collection.getAppl().equalsIgnoreCase("RSM"))
		{

			smsText.append(" towards Phoenix ");
			smsText.append(collection.getAppl() + " ");
			smsText.append(generateApacForSMS(collection.getBusinessPartnerNumber()));

		}
		else
		{
			smsText.append(" towards Kotak Loan ");
			smsText.append(collection.getAppl() + " ");
			smsText.append(generateApacForSMS(collection.getBusinessPartnerNumber()));

		}
		/*smsText.append(collection.getAppl() + " ");
		smsText.append(generateApacForSMS(collection.getBusinessPartnerNumber()));*/
		smsText.append(" received in CASH via receipt no " + collection.getReceiptNumber());
		smsText.append(" in Old Notes of ");

		smsText.append(old500Count + "/500 and ");
		smsText.append(old1000Count + "/1000");
		smsText.append(" on ");
		smsText.append(generateDateTimeForSMS(new Timestamp(System.currentTimeMillis()).toString()));

		/* commented by bhushan for new SMS verbiage for prime */
		
		List<String> headerKeyList = new ArrayList<String>();
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQID_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY);

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY, XMLConstants.SMS_DISPATCHER_MCOLL_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_REQID_KEY, System.currentTimeMillis());
		headerMap.put(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY, XMLConstants.SMS_DISPATCHER_COLLECTION_VALUE);

		String headerListString = Utilities.generateXML(headerKeyList, headerMap).toString();

		List<String> bodyKeyList = new ArrayList<String>();
		bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY);
		bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTO_KEY);

		Map<String, Object> bodyMap = new HashMap<String, Object>();
		bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY, smsText.toString().replaceAll("&", "&amp;"));
		bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTO_KEY, mobileNumber);

		String bodyListString = Utilities.generateXML(bodyKeyList, bodyMap).toString();

		List<String> posKeyList = new ArrayList<String>();
		posKeyList.add(XMLConstants.SMS_DISPATCHER_HEADER_KEY);
		posKeyList.add(XMLConstants.SMS_DISPATCHER_BODY_KEY);

		Map<String, Object> posMap = new HashMap<String, Object>();
		posMap.put(XMLConstants.SMS_DISPATCHER_HEADER_KEY, headerListString);
		posMap.put(XMLConstants.SMS_DISPATCHER_BODY_KEY, bodyListString);

		String posListString = Utilities.generateXML(posKeyList, posMap).toString();

		Map<String, Object> mainPOSMap = new HashMap<String, Object>();

		mainPOSMap.put(XMLConstants.SMS_DISPATCHER_POS_KEY, posListString);

		return mainPOSMap;
		
		/*List<String> headerKeyList = new ArrayList<String>();
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SOURCEAPPID_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SMSVENDOR_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_UNIQUEREFNO_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_PRIORITY_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_FROM_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_TONUMBER_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_MESSAGE_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_INTNO_KEY);

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put(XMLConstants.SMS_DISPATCHER_SOURCEAPPID_KEY, XMLConstants.SMS_DISPATCHER_MCOLL_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_SMSVENDOR_KEY, XMLConstants.SMS_DISPATCHER_SMSVENDOR_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_UNIQUEREFNO_KEY, System.currentTimeMillis());
		headerMap.put(XMLConstants.SMS_DISPATCHER_PRIORITY_KEY, XMLConstants.SMS_DISPATCHER_PRIORITYANDINTNO_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_FROM_KEY, XMLConstants.SMS_DISPATCHER_FROM_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_TONUMBER_KEY, mobileNumber);
		headerMap.put(XMLConstants.SMS_DISPATCHER_MESSAGE_KEY, smsText.toString().replaceAll("&", "&amp;"));
		headerMap.put(XMLConstants.SMS_DISPATCHER_INTNO_KEY, XMLConstants.SMS_DISPATCHER_PRIORITYANDINTNO_VALUE);

		String headerListString = Utilities.generateXML(headerKeyList, headerMap).toString();

		Map<String, Object> mainPOSMap = new HashMap<String, Object>();

		mainPOSMap.put(XMLConstants.SMS_DISPATCHER_SMSAPIREQ_KEY, headerListString);

		return mainPOSMap;*/
		
		}

	public static Map<String, Object> generateSMSMapForKGICust(String amount, String receiptNumber, String paymentType,
			String mobileNumber, String type, String apacCardNumber, String regiNumber)
	{
		StringBuilder smsText = new StringBuilder();

		smsText.append("Thank you for payment of Rs.");
		smsText.append(amount);

		smsText.append(" towards Kotak ");

		smsText.append(type + " ");
		smsText.append(generateApacForSMS(apacCardNumber));

		smsText.append("received in " + paymentType + "via receipt no " + receiptNumber + " on ");
		smsText.append(generateDateTimeForSMS(new Timestamp(System.currentTimeMillis()).toString()));

		/* commented by bhushan for new SMS verbiage for prime */
		
		List<String> headerKeyList = new ArrayList<String>();
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQID_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY);

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY, XMLConstants.SMS_DISPATCHER_MCOLL_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_REQID_KEY, System.currentTimeMillis());
		headerMap.put(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY, XMLConstants.SMS_DISPATCHER_COLLECTION_VALUE);

		String headerListString = Utilities.generateXML(headerKeyList, headerMap).toString();

		List<String> bodyKeyList = new ArrayList<String>();
		bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY);
		bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTO_KEY);

		Map<String, Object> bodyMap = new HashMap<String, Object>();
		bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY, smsText.toString().replaceAll("&", "&amp;"));
		bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTO_KEY, mobileNumber);

		String bodyListString = Utilities.generateXML(bodyKeyList, bodyMap).toString();

		List<String> posKeyList = new ArrayList<String>();
		posKeyList.add(XMLConstants.SMS_DISPATCHER_HEADER_KEY);
		posKeyList.add(XMLConstants.SMS_DISPATCHER_BODY_KEY);

		Map<String, Object> posMap = new HashMap<String, Object>();
		posMap.put(XMLConstants.SMS_DISPATCHER_HEADER_KEY, headerListString);
		posMap.put(XMLConstants.SMS_DISPATCHER_BODY_KEY, bodyListString);

		String posListString = Utilities.generateXML(posKeyList, posMap).toString();

		Map<String, Object> mainPOSMap = new HashMap<String, Object>();

		mainPOSMap.put(XMLConstants.SMS_DISPATCHER_POS_KEY, posListString);

		return mainPOSMap;
		
		/*List<String> headerKeyList = new ArrayList<String>();
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SOURCEAPPID_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SMSVENDOR_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_UNIQUEREFNO_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_PRIORITY_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_FROM_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_TONUMBER_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_MESSAGE_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_INTNO_KEY);

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put(XMLConstants.SMS_DISPATCHER_SOURCEAPPID_KEY, XMLConstants.SMS_DISPATCHER_MCOLL_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_SMSVENDOR_KEY, XMLConstants.SMS_DISPATCHER_SMSVENDOR_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_UNIQUEREFNO_KEY, System.currentTimeMillis());
		headerMap.put(XMLConstants.SMS_DISPATCHER_PRIORITY_KEY, XMLConstants.SMS_DISPATCHER_PRIORITYANDINTNO_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_FROM_KEY, XMLConstants.SMS_DISPATCHER_FROM_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_TONUMBER_KEY, mobileNumber);
		headerMap.put(XMLConstants.SMS_DISPATCHER_MESSAGE_KEY, smsText.toString().replaceAll("&", "&amp;"));
		headerMap.put(XMLConstants.SMS_DISPATCHER_INTNO_KEY, XMLConstants.SMS_DISPATCHER_PRIORITYANDINTNO_VALUE);

		String headerListString = Utilities.generateXML(headerKeyList, headerMap).toString();

		Map<String, Object> mainPOSMap = new HashMap<String, Object>();

		mainPOSMap.put(XMLConstants.SMS_DISPATCHER_SMSAPIREQ_KEY, headerListString);

		return mainPOSMap;*/
	}

	
	public static Map<String, Object> generateSMSMapForKGIFE(String amount, String receiptNumber,
			String paymentType, String mobileNumber, String type, String customerName)
	{
		StringBuilder smsText = new StringBuilder();
		smsText.append("The Payment of Rs. ");
		smsText.append(amount);
		smsText.append(" against Receipt No ");
		smsText.append(receiptNumber);
		smsText.append(" has been processed for vehicle insurance renewal premium");
		smsText.append(" of Mr./Mrs./Miss. ");
		smsText.append(customerName);
		
		/* commented by bhushan for new SMS verbiage for prime */

		List<String> headerKeyList = new ArrayList<String>();
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQID_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY);

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY, XMLConstants.SMS_DISPATCHER_MCOLL_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_REQID_KEY, System.currentTimeMillis());
		headerMap.put(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY, XMLConstants.SMS_DISPATCHER_COLLECTION_VALUE);

		String headerListString = Utilities.generateXML(headerKeyList, headerMap).toString();

		List<String> bodyKeyList = new ArrayList<String>();
		bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY);
		bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTO_KEY);

		Map<String, Object> bodyMap = new HashMap<String, Object>();
		bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY, smsText.toString().replaceAll("&", "&amp;"));
		bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTO_KEY, mobileNumber);

		String bodyListString = Utilities.generateXML(bodyKeyList, bodyMap).toString();

		List<String> posKeyList = new ArrayList<String>();
		posKeyList.add(XMLConstants.SMS_DISPATCHER_HEADER_KEY);
		posKeyList.add(XMLConstants.SMS_DISPATCHER_BODY_KEY);

		Map<String, Object> posMap = new HashMap<String, Object>();
		posMap.put(XMLConstants.SMS_DISPATCHER_HEADER_KEY, headerListString);
		posMap.put(XMLConstants.SMS_DISPATCHER_BODY_KEY, bodyListString);

		String posListString = Utilities.generateXML(posKeyList, posMap).toString();

		Map<String, Object> mainPOSMap = new HashMap<String, Object>();

		mainPOSMap.put(XMLConstants.SMS_DISPATCHER_POS_KEY, posListString);

		return mainPOSMap;
		
		/*List<String> headerKeyList = new ArrayList<String>();
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SOURCEAPPID_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SMSVENDOR_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_UNIQUEREFNO_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_PRIORITY_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_FROM_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_TONUMBER_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_MESSAGE_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_INTNO_KEY);

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put(XMLConstants.SMS_DISPATCHER_SOURCEAPPID_KEY, XMLConstants.SMS_DISPATCHER_MCOLL_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_SMSVENDOR_KEY, XMLConstants.SMS_DISPATCHER_SMSVENDOR_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_UNIQUEREFNO_KEY, System.currentTimeMillis());
		headerMap.put(XMLConstants.SMS_DISPATCHER_PRIORITY_KEY, XMLConstants.SMS_DISPATCHER_PRIORITYANDINTNO_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_FROM_KEY, XMLConstants.SMS_DISPATCHER_FROM_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_TONUMBER_KEY, mobileNumber);
		headerMap.put(XMLConstants.SMS_DISPATCHER_MESSAGE_KEY, smsText.toString().replaceAll("&", "&amp;"));
		headerMap.put(XMLConstants.SMS_DISPATCHER_INTNO_KEY, XMLConstants.SMS_DISPATCHER_PRIORITYANDINTNO_VALUE);

		String headerListString = Utilities.generateXML(headerKeyList, headerMap).toString();

		Map<String, Object> mainPOSMap = new HashMap<String, Object>();

		mainPOSMap.put(XMLConstants.SMS_DISPATCHER_SMSAPIREQ_KEY, headerListString);

		return mainPOSMap;*/
	}
	

public static Map<String, Object> generateAllPaySMSDispatcherMapForFE(String amount, String receiptNumber,
			String paymentType, String mobileNumber, String customerNo, String feName ,String status)
	{
		StringBuilder smsText = new StringBuilder();
		if(status.equalsIgnoreCase("Success"))
		{
			/*smsText.append("The message with link for Rs.");
			smsText.append(amount);
			smsText.append(" towards receipt no ");
			smsText.append(receiptNumber);
			smsText.append(" has been delivered successfully on customer's mobile no. ");
			smsText.append(customerNo);
			smsText.append(" of Mr./Mrs./Miss. ");
			smsText.append(feName);*/
			smsText.append("The message with link for Rs.");
			smsText.append(amount);
			smsText.append(" towards receipt no has been delivered successfully on customer's mobile no.");			
			smsText.append(" of Mr./Mrs./Miss. ");
			smsText.append(feName);
			
		}
		else if(status.equalsIgnoreCase("Failure"))
		{		
			/*smsText.append("The message with link for Rs.");
			smsText.append(amount);
			smsText.append(" towards receipt no ");
			smsText.append(receiptNumber);
			smsText.append("  has not been delivered on customer's mobile no. ");
			smsText.append(customerNo);
			smsText.append(" for ( ");
			smsText.append("Some Technical error");
			smsText.append(" ) ");
			smsText.append(" of Mr./Mrs./Miss. ");
			smsText.append(feName);*/
			smsText.append("The message with link for Rs.");
			smsText.append(amount);
			smsText.append(" towards receipt no has not been delivered on customer's mobile no.");			
			smsText.append(" for ( ");
			smsText.append("Some Technical error");
			smsText.append(" ) ");
			smsText.append(" of Mr./Mrs./Miss. ");
			smsText.append(feName);
		}		

		List<String> headerKeyList = new ArrayList<String>();
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQID_KEY);
		headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY);

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY, XMLConstants.SMS_DISPATCHER_MCOLL_VALUE);
		headerMap.put(XMLConstants.SMS_DISPATCHER_REQID_KEY, System.currentTimeMillis());
		headerMap.put(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY, XMLConstants.SMS_DISPATCHER_COLLECTION_VALUE);

		String headerListString = Utilities.generateXML(headerKeyList, headerMap).toString();

		List<String> bodyKeyList = new ArrayList<String>();
		bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY);
		bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTO_KEY);

		Map<String, Object> bodyMap = new HashMap<String, Object>();
		bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY, smsText.toString().replaceAll("&", "&amp;"));
		bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTO_KEY, mobileNumber);

		String bodyListString = Utilities.generateXML(bodyKeyList, bodyMap).toString();

		List<String> posKeyList = new ArrayList<String>();
		posKeyList.add(XMLConstants.SMS_DISPATCHER_HEADER_KEY);
		posKeyList.add(XMLConstants.SMS_DISPATCHER_BODY_KEY);

		Map<String, Object> posMap = new HashMap<String, Object>();
		posMap.put(XMLConstants.SMS_DISPATCHER_HEADER_KEY, headerListString);
		posMap.put(XMLConstants.SMS_DISPATCHER_BODY_KEY, bodyListString);

		String posListString = Utilities.generateXML(posKeyList, posMap).toString();

		Map<String, Object> mainPOSMap = new HashMap<String, Object>();

		mainPOSMap.put(XMLConstants.SMS_DISPATCHER_POS_KEY, posListString);

		return mainPOSMap;	
	
	}


public static Map<String, Object> generateAllPaySMSToCustomerOnSubmittingReceiptForAllPay(String amount, String receiptNumber,
		String paymentType, String mobileNumber, String appl, String feName ,String status,Collection collection)
{
	
	System.out.println("Status-----------------------"+status);

	StringBuilder smsText = new StringBuilder();
	/*if(status.equalsIgnoreCase("Success"))
	{
	
	smsText.append("Dear Customer,request for online payment of Rs. ");
	smsText.append(amount);
	smsText.append("has been generated to Pay Kotak Mahindra Bank against receipt no. ");		
	smsText.append(receiptNumber);
	smsText.append("for APAC No.");		
	smsText.append(collection.getBusinessPartnerNumber());
	smsText.append("Pay online at https://ccavenue.com/txn/5059393,on successful processing separate receipt will be sent through SMS.");	

		
	}	*/
	
	
	if(status.equalsIgnoreCase("Success"))
	{
	
	smsText.append("The payment of Rs. ");
	smsText.append(amount);
	smsText.append(" has been successfully received against request no. ");		
	smsText.append(receiptNumber);
	smsText.append(" for APAC No.");		
	smsText.append(collection.getBusinessPartnerNumber());
	smsText.append(".Subject to the successful receipt of payment, the amount will reflect in Statement of Account.");	

		
	}	

	List<String> headerKeyList = new ArrayList<String>();
	headerKeyList.add(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY);
	headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQID_KEY);
	headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY);

	Map<String, Object> headerMap = new HashMap<String, Object>();
	headerMap.put(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY, XMLConstants.SMS_DISPATCHER_MCOLL_VALUE);
	headerMap.put(XMLConstants.SMS_DISPATCHER_REQID_KEY, System.currentTimeMillis());
	headerMap.put(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY, XMLConstants.SMS_DISPATCHER_COLLECTION_VALUE);

	String headerListString = Utilities.generateXML(headerKeyList, headerMap).toString();

	List<String> bodyKeyList = new ArrayList<String>();
	bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY);
	bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTO_KEY);

	Map<String, Object> bodyMap = new HashMap<String, Object>();
	bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY, smsText.toString().replaceAll("&", "&amp;"));
	bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTO_KEY, mobileNumber);

	String bodyListString = Utilities.generateXML(bodyKeyList, bodyMap).toString();
	
	
	System.out.println("bodyListString-------------"+bodyListString);

	List<String> posKeyList = new ArrayList<String>();
	posKeyList.add(XMLConstants.SMS_DISPATCHER_HEADER_KEY);
	posKeyList.add(XMLConstants.SMS_DISPATCHER_BODY_KEY);

	Map<String, Object> posMap = new HashMap<String, Object>();
	posMap.put(XMLConstants.SMS_DISPATCHER_HEADER_KEY, headerListString);
	posMap.put(XMLConstants.SMS_DISPATCHER_BODY_KEY, bodyListString);

	String posListString = Utilities.generateXML(posKeyList, posMap).toString();

	Map<String, Object> mainPOSMap = new HashMap<String, Object>();

	mainPOSMap.put(XMLConstants.SMS_DISPATCHER_POS_KEY, posListString);

	return mainPOSMap;	

}


public static Map<String, Object> generateSMSToDRAOnSubmittingReceiptForAllPay(String amount, String receiptNumber,
		String paymentType, String mobileNumber, String appl, String feName ,String status,Collection collection)
{
	
	StringBuilder smsText = new StringBuilder();
	/*if(status.equalsIgnoreCase("Success"))
	{
	
	smsText.append("Request for online payment of Rs.");
	smsText.append(amount);
	smsText.append(" has been generated against receipt no. ");		
	smsText.append(receiptNumber);
	smsText.append(" for APAC no.");		
	smsText.append(appl+"-"+collection.getBusinessPartnerNumber());

		
	}*/	
	
	
	if(status.equalsIgnoreCase("Success"))
	{
	
	smsText.append("The payment of Rs. ");
	smsText.append(amount);
	smsText.append(" has been successfully received against request no. ");		
	smsText.append(receiptNumber);
	smsText.append(" for APAC no.");		
	smsText.append(appl+"-"+collection.getBusinessPartnerNumber());

	}	
	else
	{
				
		smsText.append("The payment of Rs. ");
		smsText.append(amount);
		smsText.append(" has failed against request no. ");		
		smsText.append(receiptNumber);
		smsText.append(" for Portfolio ");		
		smsText.append(appl);
		smsText.append(" and APAC Number ");		
		smsText.append(collection.getBusinessPartnerNumber());
		smsText.append(".Please contact customer and generate new request number. ");		



	}

	List<String> headerKeyList = new ArrayList<String>();
	headerKeyList.add(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY);
	headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQID_KEY);
	headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY);

	Map<String, Object> headerMap = new HashMap<String, Object>();
	headerMap.put(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY, XMLConstants.SMS_DISPATCHER_MCOLL_VALUE);
	headerMap.put(XMLConstants.SMS_DISPATCHER_REQID_KEY, System.currentTimeMillis());
	headerMap.put(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY, XMLConstants.SMS_DISPATCHER_COLLECTION_VALUE);

	String headerListString = Utilities.generateXML(headerKeyList, headerMap).toString();

	List<String> bodyKeyList = new ArrayList<String>();
	bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY);
	bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTO_KEY);

	Map<String, Object> bodyMap = new HashMap<String, Object>();
	bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY, smsText.toString().replaceAll("&", "&amp;"));
	bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTO_KEY, mobileNumber);

	String bodyListString = Utilities.generateXML(bodyKeyList, bodyMap).toString();
	
	
	System.out.println("bodyListString-------------"+bodyListString);

	List<String> posKeyList = new ArrayList<String>();
	posKeyList.add(XMLConstants.SMS_DISPATCHER_HEADER_KEY);
	posKeyList.add(XMLConstants.SMS_DISPATCHER_BODY_KEY);

	Map<String, Object> posMap = new HashMap<String, Object>();
	posMap.put(XMLConstants.SMS_DISPATCHER_HEADER_KEY, headerListString);
	posMap.put(XMLConstants.SMS_DISPATCHER_BODY_KEY, bodyListString);

	String posListString = Utilities.generateXML(posKeyList, posMap).toString();

	Map<String, Object> mainPOSMap = new HashMap<String, Object>();

	mainPOSMap.put(XMLConstants.SMS_DISPATCHER_POS_KEY, posListString);

	return mainPOSMap;	

}

public static String generateReceiptInXX(String receiptNumber)
{
	String smsReceiptFormat = "";
	
	if ((!receiptNumber.isEmpty()) && (receiptNumber.length() > 0))
	{
		for (int i = receiptNumber.length() ; i >= 2; i--)
		{
			smsReceiptFormat = smsReceiptFormat + "*";
		}

		smsReceiptFormat = receiptNumber.substring(0,2)+smsReceiptFormat;
		
		System.out.println("smsReceiptFormat :: "+smsReceiptFormat);
	}
	return smsReceiptFormat;
}

public static String generateAmountInXX(String amount)
{
	String smsAmountFormat = "";
	
	if ((!amount.isEmpty()) && (amount.length() > 0))
	{
		
		for (int i = 0 ; i <= amount.length(); i++)
		{
			smsAmountFormat = smsAmountFormat + "*";
		}

		
		System.out.println("smsAmountFormat :: "+smsAmountFormat);
	}
	return smsAmountFormat;
}



public static String generateApacInXX(String apac)
{
	String smsApacFormat = "";
	
	if ((!apac.isEmpty()) && (apac.length() > 0))
	{
		
		for (int i = 0 ; i <= apac.length(); i++)
		{
			smsApacFormat = smsApacFormat + "*";
		}

		
		System.out.println("smsApacFormat :: "+smsApacFormat);
	}
	return smsApacFormat;
}


public static Map<String, Object> generateAllPayStatusSMSMapForCustomer(String amount, String receiptNumber,
		String apac, String mobileNumber, String appl, String  status)
{
	StringBuilder smsText = new StringBuilder();
	
	if(status.equalsIgnoreCase("Success"))
	{

		smsText.append("The payment of Rs. ");
		smsText.append(amount);
		smsText.append(" has been successfully received against receipt no.");		
		smsText.append(receiptNumber);
		smsText.append(" for APAC no.");		
		smsText.append(apac);
		smsText.append(".Amount will be reflected in Statement of Account subject to realization.");	
		
		
	}

	List<String> headerKeyList = new ArrayList<String>();
	headerKeyList.add(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY);
	headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQID_KEY);
	headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY);

	Map<String, Object> headerMap = new HashMap<String, Object>();
	headerMap.put(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY, XMLConstants.SMS_DISPATCHER_MCOLL_VALUE);
	headerMap.put(XMLConstants.SMS_DISPATCHER_REQID_KEY, System.currentTimeMillis());
	headerMap.put(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY, XMLConstants.SMS_DISPATCHER_COLLECTION_VALUE);

	String headerListString = Utilities.generateXML(headerKeyList, headerMap).toString();

	List<String> bodyKeyList = new ArrayList<String>();
	bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY);
	bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTO_KEY);

	Map<String, Object> bodyMap = new HashMap<String, Object>();
	bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY, smsText.toString().replaceAll("&", "&amp;"));
	bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTO_KEY, mobileNumber);

	String bodyListString = Utilities.generateXML(bodyKeyList, bodyMap).toString();

	List<String> posKeyList = new ArrayList<String>();
	posKeyList.add(XMLConstants.SMS_DISPATCHER_HEADER_KEY);
	posKeyList.add(XMLConstants.SMS_DISPATCHER_BODY_KEY);

	Map<String, Object> posMap = new HashMap<String, Object>();
	posMap.put(XMLConstants.SMS_DISPATCHER_HEADER_KEY, headerListString);
	posMap.put(XMLConstants.SMS_DISPATCHER_BODY_KEY, bodyListString);

	String posListString = Utilities.generateXML(posKeyList, posMap).toString();

	Map<String, Object> mainPOSMap = new HashMap<String, Object>();

	mainPOSMap.put(XMLConstants.SMS_DISPATCHER_POS_KEY, posListString);

	return mainPOSMap;	

}

public static Map<String, Object> generateAllPayStatusSMSMapForFE(String amount, String receiptNumber,
		String paymentType,String appl ,String feName,String mobileNumber,String status,String apac)
{
	
	StringBuilder smsText = new StringBuilder();
	
	if(status.equalsIgnoreCase("Success"))
	{
		smsText.append("The payment for Rs.");
		smsText.append(amount);
		smsText.append(" has been successfully received against receipt number no. ");		
		smsText.append(receiptNumber);
		smsText.append(" for and APAC Number ");		
		smsText.append(appl+"-"+apac);

		
	}
	else if(status.equalsIgnoreCase("Failure"))
	{		
		
		smsText.append("The payment of Rs. ");
		smsText.append(amount);
		smsText.append(" has been failed against receipt number ");		
		smsText.append(receiptNumber);
		smsText.append(" for Portfolio-");		
		smsText.append(appl);
		smsText.append(" and ");
		smsText.append(" APAC Number ");
		smsText.append(apac);
		smsText.append(".Please contact customer and generate new receipt.");



	}		

	List<String> headerKeyList = new ArrayList<String>();
	headerKeyList.add(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY);
	headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQID_KEY);
	headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY);

	Map<String, Object> headerMap = new HashMap<String, Object>();
	headerMap.put(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY, XMLConstants.SMS_DISPATCHER_MCOLL_VALUE);
	headerMap.put(XMLConstants.SMS_DISPATCHER_REQID_KEY, System.currentTimeMillis());
	headerMap.put(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY, XMLConstants.SMS_DISPATCHER_COLLECTION_VALUE);

	String headerListString = Utilities.generateXML(headerKeyList, headerMap).toString();

	List<String> bodyKeyList = new ArrayList<String>();
	bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY);
	bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTO_KEY);

	Map<String, Object> bodyMap = new HashMap<String, Object>();
	bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY, smsText.toString().replaceAll("&", "&amp;"));
	bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTO_KEY, mobileNumber);

	String bodyListString = Utilities.generateXML(bodyKeyList, bodyMap).toString();
	
	
	System.out.println("bodyListString-------------"+bodyListString);

	List<String> posKeyList = new ArrayList<String>();
	posKeyList.add(XMLConstants.SMS_DISPATCHER_HEADER_KEY);
	posKeyList.add(XMLConstants.SMS_DISPATCHER_BODY_KEY);

	Map<String, Object> posMap = new HashMap<String, Object>();
	posMap.put(XMLConstants.SMS_DISPATCHER_HEADER_KEY, headerListString);
	posMap.put(XMLConstants.SMS_DISPATCHER_BODY_KEY, bodyListString);

	String posListString = Utilities.generateXML(posKeyList, posMap).toString();

	Map<String, Object> mainPOSMap = new HashMap<String, Object>();

	mainPOSMap.put(XMLConstants.SMS_DISPATCHER_POS_KEY, posListString);

	return mainPOSMap;	

}

/*Added for Android 10 otp generation*/

public static Map<String, Object> generateSMSForAndroidOTP(String OTP, SystemUser user)
{

	StringBuilder smsText = new StringBuilder();
	smsText.append("Dear reportee having USERNAME ");
	smsText.append(user.getUsername());
	smsText.append(" has raised a OTP request. Find the OTP ");
	smsText.append(OTP);
	smsText.append(" generated for the user for sharing it with him/her.");

	/* commented by bhushan for new SMS verbiage for prime */
	
	List<String> headerKeyList = new ArrayList<String>();
	headerKeyList.add(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY);
	headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQID_KEY);
	headerKeyList.add(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY);

	Map<String, Object> headerMap = new HashMap<String, Object>();
	headerMap.put(XMLConstants.SMS_DISPATCHER_SOURCEAPPCODE_KEY, XMLConstants.SMS_DISPATCHER_MCOLL_VALUE);
	headerMap.put(XMLConstants.SMS_DISPATCHER_REQID_KEY, System.currentTimeMillis());
	headerMap.put(XMLConstants.SMS_DISPATCHER_REQTYPE_KEY, XMLConstants.SMS_DISPATCHER_COLLECTION_VALUE);

	String headerListString = Utilities.generateXML(headerKeyList, headerMap).toString();

	List<String> bodyKeyList = new ArrayList<String>();
	bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY);
	bodyKeyList.add(XMLConstants.SMS_DISPATCHER_SMSTO_KEY);

	Map<String, Object> bodyMap = new HashMap<String, Object>();
	bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTXT_KEY, smsText.toString().replaceAll("&", "&amp;"));
	bodyMap.put(XMLConstants.SMS_DISPATCHER_SMSTO_KEY, user.getMobileNumber());

	String bodyListString = Utilities.generateXML(bodyKeyList, bodyMap).toString();

	List<String> posKeyList = new ArrayList<String>();
	posKeyList.add(XMLConstants.SMS_DISPATCHER_HEADER_KEY);
	posKeyList.add(XMLConstants.SMS_DISPATCHER_BODY_KEY);

	Map<String, Object> posMap = new HashMap<String, Object>();
	posMap.put(XMLConstants.SMS_DISPATCHER_HEADER_KEY, headerListString);
	posMap.put(XMLConstants.SMS_DISPATCHER_BODY_KEY, bodyListString);

	String posListString = Utilities.generateXML(posKeyList, posMap).toString();

	Map<String, Object> mainPOSMap = new HashMap<String, Object>();

	mainPOSMap.put(XMLConstants.SMS_DISPATCHER_POS_KEY, posListString);

	return mainPOSMap;
	
	
}

}
