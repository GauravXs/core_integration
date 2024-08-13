package com.mobicule.mcollections.integration.collection;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.mail.SimpleMailMessage;

import com.mobicule.component.activitylogger.beans.UserActivity;
import com.mobicule.component.activitylogger.commons.ActivityLoggerConstants;
import com.mobicule.component.activitylogger.service.CommunicationActivityService;
import com.mobicule.component.activitylogger.service.NotificationActivityService;
import com.mobicule.component.activitylogger.service.UserActivityService;
import com.mobicule.component.activitylogger.threads.UserActivityAddition;
import com.mobicule.component.activitylogger.threads.UserActivityStatusUpdate;
import com.mobicule.component.mapconversion.json.MapToJSON;
import com.mobicule.component.mapconversion.xml.XMLToMap;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.EmailUtilities;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.RandomCollectionsExternalService;
import com.mobicule.mcollections.core.service.RandomCollectionsService;
import com.mobicule.mcollections.core.service.SettlementService;
import com.mobicule.mcollections.integration.commons.ServerUtilities;
import com.mobicule.mcollections.integration.commons.XMLConstants;
import com.mobicule.mcollections.webservice.adapter.KotakCollectionWebserviceAdapter;

public class SettlementSearchService implements ISettlementSearchService
{
    private Logger log = LoggerFactory.getLogger(getClass());
    
    
    private SettlementService settlementService;   

	public SettlementService getSettlementService()
	{
		return settlementService;
	}

	public void setSettlementService(SettlementService settlementService)
	{
		this.settlementService = settlementService;
	}

	@Autowired
    private RandomCollectionsService randomCollectionsService;

    @Autowired
    private ApplicationConfiguration<String, String> applicationConfiguration;

    @Autowired
    private KotakCollectionWebserviceAdapter webserviceAdapter;

    @Autowired
    private RandomCollectionsExternalService randomCollectionsExternalService;

    @Autowired
    private UserActivityService userActivityService;

    @Autowired
    private CommunicationActivityService communicationActivityService;

    @Autowired
    private NotificationActivityService notificationActivityService;

    @Autowired
    private SimpleMailMessage simpleMailMessageForCashPaymentCreditCard;

    @Autowired
    private SimpleMailMessage simpleMailMessageForCashPaymentLoan;

    @Autowired
    private SimpleMailMessage simpleMailMessageForChequePaymentCreditCard;

    @Autowired
    private SimpleMailMessage simpleMailMessageForChequePaymentLoan;

    @Autowired
    private SimpleMailMessage simpleMailMessageForDDPDC;

    @Autowired
    private EmailUtilities emailService;

    @Override
    public Message<String> execute(org.springframework.integration.Message<String> message) throws Throwable
    {
	log.info("<----------------- inside execute/Settelement Service -------------------->");

	JSONObject responseJSON = new JSONObject();

	try
	{
	    String requestSet = message.getPayload();

	    String requestEntity = JSONPayloadExtractor.extract(requestSet, JsonConstants.ENTITY);
	    String requestAction = JSONPayloadExtractor.extract(requestSet, JsonConstants.ACTION);

	    JSONObject requestSystemUser = JSONPayloadExtractor.extractJSON(requestSet, JsonConstants.SYSTEM_USER);

	    JSONObject requestData = JSONPayloadExtractor.extractJSON(requestSet, JsonConstants.DATA);
	    log.info("requestData : - " + requestData);
	    SystemUser systemUser = ServerUtilities.extractSystemUser(requestSystemUser);

	    String tempccapac = systemUser.getCcapac();

	    systemUser.setCcapac(tempccapac);

			UserActivityAddition userActivityAddition = new UserActivityAddition(requestSet, userActivityService,
					ActivityLoggerConstants.DATABASE_MSSQL);

	    new Thread(userActivityAddition).run();

	    UserActivity userActivity = userActivityAddition.extractUserActivity();

	    if (requestAction.equalsIgnoreCase(JsonConstants.ACTION_SETTLEMENT_SUBMIT))
	    {

		log.info("----inside submit settlement data -----");
		/*
		 * submitSettlment(responseJSON, requestData, systemUser,
		 * requestEntity, userActivity, communicationActivityService);
		 */
	    }
	    else if (requestAction.equalsIgnoreCase(JsonConstants.ACTION_SETTLEMENT_SEARCH)
	    /* && requestEntity.equalsIgnoreCase(JsonConstants.SPLN) */)
	    {
		if (requestEntity.equalsIgnoreCase(JsonConstants.CREDIT_CARD))
		{
		    searchCWOSettlement(responseJSON, requestData, requestEntity, userActivity, communicationActivityService);
		}
		else
		{
		    searchSPLNSettlement(responseJSON, requestData, requestEntity, userActivity, communicationActivityService);
		}

	    }

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
	    responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_SETTLEMENT_SEARCH_FAILURE);
	    responseJSON.put(JsonConstants.DATA, "");
	}

	return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
    }

	private void searchSPLNSettlement(JSONObject responseJSON, JSONObject requestData, String entity,
			UserActivity userActivity, CommunicationActivityService communicationActivityService) throws JSONException
    {

	try
	{

	    log.info("--- inside searchSPLNSettlement -----");

	    Map<String, Object> wrapperMap = new HashMap<String, Object>();
	    long reqUID = createSearchRequestMap(requestData, entity, wrapperMap);

	    Map<String, Object> webserviceResponseMap = new HashMap<String, Object>();
	    try
	    {

			webserviceResponseMap = webserviceAdapter.callWebserviceAndGetMap(wrapperMap,
						applicationConfiguration.getValue("WEB_SERVICE_URL_MCORE"), userActivity,
						communicationActivityService);

		/* Dummy Response */

		// reqID hard-coded

		/*reqUID = 700758;

		webserviceResponseMap = XMLToMap
						.convertXMLToMap("<mc005><response_header><sourceappcode>MCOLL</sourceappcode><RqUID>700758</RqUID><message_type_cd>MC005</message_type_cd><message_datetime>2016-04-27 16:49:45</message_datetime></response_header><response_details><partyid>22848</partyid><partyname>Dayalsingh Kundansingh</partyname><appl>CSG</appl><apacnum>400011327</apacnum><EMIduedate>05-MAY-2016</EMIduedate><Penalamount>0</Penalamount><Overdueamount>126000</Overdueamount><TotaloutstandingAmount>126000</TotaloutstandingAmount><EMIdueAmount>18000</EMIdueAmount><mobilenum></mobilenum><email></email><phonenum>774852</phonenum><address>21, GURUDWARA MARKET , CHHANI ROAD, CHHANI BARODA, VADODARA -391740, Gujarat - India</address><regno></regno><byrpartyid></byrpartyid><byrpartyname></byrpartyname><Apacdate>09-SEP-2015</Apacdate><Tenure>36</Tenure><AGRVALUE>500000</AGRVALUE><EMIPAID>0</EMIPAID><BALTENURE>36</BALTENURE><PrincipleOutstanding>421577.08</PrincipleOutstanding><FCCHARGES>19307.57</FCCHARGES><BROKENREVENUE>4594.31</BROKENREVENUE><LEGALCAGRGES>0</LEGALCAGRGES><BUCKET>7</BUCKET><FIRSTOSTDDATE>22-apr-2016</FIRSTOSTDDATE><CLOSUREBAL>0</CLOSUREBAL></response_details><response_status><errorcode>0</errorcode><errordesc>SUCCESS</errordesc></response_status></mc005>");*/

		log.info("-----webserviceResponseMap-----" + webserviceResponseMap.toString());

	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
	    }

	    if (null == webserviceResponseMap)
	    {
		responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
		responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_SETTLEMENT_SEARCH_FAILURE);
		responseJSON.put(JsonConstants.DATA, "");
				UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
						(ActivityLoggerConstants.STATUS_FAILURE), userActivityService);
		new Thread(userActivityStatusUpdate).run();
	    }
	    else
	    {
		parseResponseMap(responseJSON, webserviceResponseMap, entity, reqUID);

				UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
						(ActivityLoggerConstants.STATUS_SUCCESS), userActivityService);
		new Thread(userActivityStatusUpdate).run();
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();

			UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
					(ActivityLoggerConstants.STATUS_ERROR), userActivityService);
	    new Thread(userActivityStatusUpdate).run();
	}
    }

	private void searchCWOSettlement(JSONObject responseJSON, JSONObject requestData, String entity,
			UserActivity userActivity, CommunicationActivityService communicationActivityService) throws JSONException
    {

	try
	{

	    log.info("--- inside searchCWOSettlement -----");

	    Map<String, Object> wrapperMap = new HashMap<String, Object>();
			long reqUID = createCardSearchRequestMap(requestData, entity, wrapperMap);
	    JSONObject response = null;

	    Map<String, Object> webserviceResponseMap = new HashMap<String, Object>();
	    List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
	    Map row = null;
	    try
	    {
				rows = randomCollectionsExternalService.searchCardSettlement(requestData
						.getString(JsonConstants.CARD_NO));
				

		if (rows != null && rows.size() != 0)
		{
			String product ="";
			if(requestData.getString(JsonConstants.CARD_NO).length()>10)
			{
				 product = requestData.getString(JsonConstants.CARD_NO).substring(6, 9);
			}	
			
			log.info("product :: " + product);

			HashMap<String, Object> productMap = new HashMap<String, Object>();

			productMap.put(JsonConstants.SettlmentConstant.PRODUCT_CODE, product);

			String productdesc = settlementService.getproductDetail(productMap);
			
		    row = rows.get(0);

		    webserviceResponseMap.put(JsonConstants.CARD_NO, requestData.getString(JsonConstants.CARD_NO));
					webserviceResponseMap.put(JsonConstants.CURRENT_BALANCE, row.get("Current_Balance") == null ? ""
							: row.get("Current_Balance").toString());
					webserviceResponseMap.put(JsonConstants.NAME, row.get("NAME") == null ? "" : row.get("NAME")
							.toString());
					webserviceResponseMap.put(JsonConstants.CORRESPONDENCE_ADDRESS,
							row.get("RESIDENCE_ADDRESS_1") == null ? "" : row.get("RESIDENCE_ADDRESS_1").toString());
					webserviceResponseMap.put(JsonConstants.ADDRESS2, row.get("RESIDENCE_ADDRESS_2") == null ? "" : row
							.get("RESIDENCE_ADDRESS_2").toString());
					webserviceResponseMap.put(JsonConstants.ADDRESS3, row.get("RESIDENCE_ADDRESS_3") == null ? "" : row
							.get("RESIDENCE_ADDRESS_3").toString());
					webserviceResponseMap.put(JsonConstants.CITY, row.get("CITY") == null ? "" : row.get("CITY")
							.toString());
					webserviceResponseMap.put(JsonConstants.PINCODE,
							row.get("PINCODE") == null ? "" : row.get("PINCODE").toString());
					webserviceResponseMap.put(JsonConstants.MOBILE_NUMBER, row.get("MOBILE_PHONE") == null ? "" : row
							.get("MOBILE_PHONE").toString());
					webserviceResponseMap.put(JsonConstants.BILLINGCYCLE, row.get("BILLING_CYCLE") == null ? "" : row
							.get("BILLING_CYCLE").toString());
					webserviceResponseMap.put(JsonConstants.MOB, row.get("MOB") == null ? "" : row.get("MOB")
							.toString());
					webserviceResponseMap.put(JsonConstants.CREDIT_LIMIT, row.get("Creditlimit") == null ? "" : row
							.get("Creditlimit").toString());
					webserviceResponseMap.put(JsonConstants.CD, row.get("CD") == null ? "" : row.get("CD").toString()); //Pmt_cycle_due as CD		   
					webserviceResponseMap.put(JsonConstants.SettlmentConstant.PRINCIPAL_OUTSTANDING,
							row.get("Principal_Amount") == null ? "" : row.get("Principal_Amount").toString());
					webserviceResponseMap.put(JsonConstants.INTREST,
							row.get("Interest") == null ? "" : row.get("Interest").toString());
					webserviceResponseMap.put(JsonConstants.LPC, row.get("LPC") == null ? "" : row.get("LPC")
							.toString());//sum(LATE_CHG_BNP) as LPC
					webserviceResponseMap.put(JsonConstants.OTHER_CHARGES, row.get("Other_charges") == null ? "" : row
							.get("Other_charges").toString());
					webserviceResponseMap.put(JsonConstants.SettlmentConstant.PROMO_CODE,row.get("PROMO") == null ? "" : row
							.get("PROMO").toString());
					
					webserviceResponseMap.put(JsonConstants.SettlmentConstant.PRODUCT_CODE,productdesc);
					
					response = MapToJSON.convertMapToJSON(webserviceResponseMap);

					log.info("response :------------->" + response.toString());
					UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
							(ActivityLoggerConstants.STATUS_SUCCESS), userActivityService);
					new Thread(userActivityStatusUpdate).run();

		    responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
		    responseJSON.put(JsonConstants.MESSAGE, "Data Found");
		    responseJSON.put(JsonConstants.DATA, response);

		}	
		// for local		
		/*if(true)
		{
			String product ="";
			if(requestData.getString(JsonConstants.CARD_NO).length()>10)
			{
				 product = requestData.getString(JsonConstants.CARD_NO).substring(6, 9);
			}	
			
			log.info("product :: " + product);

			HashMap<String, Object> productMap = new HashMap<String, Object>();

			productMap.put(JsonConstants.SettlmentConstant.PRODUCT_CODE, product);

			String productdesc = settlementService.getproductDetail(productMap);
		
			webserviceResponseMap.put("cardNo", requestData.getString("cardNo"));
	        webserviceResponseMap.put("currentBalance", "1500");
	        webserviceResponseMap.put("name", "Hemlata");
	        webserviceResponseMap.put("partyCorrAdd", "MUMBAI,MAHARASHATRA");
	        webserviceResponseMap.put("address2", "Pune");
	        webserviceResponseMap.put("address3", "");
	        webserviceResponseMap.put("city", "MUMBAI");
	        webserviceResponseMap.put("pincode", "425401");
	        webserviceResponseMap.put("partyMobNo", "5558544456");
	        webserviceResponseMap.put("billingCycle", "01");
	        webserviceResponseMap.put("mob", "22547.32");
	        webserviceResponseMap.put("creditLimit", "2000");
	        webserviceResponseMap.put("CD", "1110");
	        webserviceResponseMap.put("principaloutstanding", "1150");
	        webserviceResponseMap.put("interest", "1300");
	        webserviceResponseMap.put("LPC", "900");
	        webserviceResponseMap.put("otherCharges", "100");
	        webserviceResponseMap.put("promoCode", "ESMR");
	        webserviceResponseMap.put(JsonConstants.SettlmentConstant.PRODUCT_CODE,productdesc);
	        
	        response = MapToJSON.convertMapToJSON(webserviceResponseMap);

	        this.log.info("response :------------->" + response.toString());

	        responseJSON.put("status", "success");
	        responseJSON.put("message", "Data Found");
	        responseJSON.put("data", response);
	        
			
		}*/		
		else if (rows != null && rows.isEmpty())
		{
		    	
		    log.info("--- rows.isEmpty() = true ---");
		    responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
		    responseJSON.put(JsonConstants.MESSAGE, "Data Not Found");
		    responseJSON.put(JsonConstants.DATA, "");
		}
		else
		{
					log.info("---MESSAGE :::  SETTLEMENT_SEARCH_FAILURE --- ");
		    responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
		    responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_SETTLEMENT_SEARCH_FAILURE);
		    responseJSON.put(JsonConstants.DATA, "");
					UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
							(ActivityLoggerConstants.STATUS_FAILURE), userActivityService);
		    new Thread(userActivityStatusUpdate).run();
		}
	    }
	    catch (Exception e)
	    {
				log.error("--- Exception In searchCWOSettlement Method Inner Catch --- " + e);
		responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
		responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_SETTLEMENT_SEARCH_FAILURE);
		responseJSON.put(JsonConstants.DATA, "");
				UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
						(ActivityLoggerConstants.STATUS_FAILURE), userActivityService);
		new Thread(userActivityStatusUpdate).run();
	    }

	}
	catch (Exception e)
	{
			log.error("--- Exception In searchCWOSettlement Method Outer Catch --- " + e);

			UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
					(ActivityLoggerConstants.STATUS_ERROR), userActivityService);
	    new Thread(userActivityStatusUpdate).run();
	}
    }

	private void parseResponseMap(JSONObject responseJSON, Map<String, Object> webserviceResponseMap, String entity,
			long reqUID) throws JSONException
    {
	String messageType = null;

	messageType = XMLConstants.MC005;

	Map responseMap = (Map) webserviceResponseMap.get(messageType);
	Map statusMap = (Map) responseMap.get(XMLConstants.RESPONSE_STATUS);

	if (statusMap.containsKey(XMLConstants.ERROR_CODE) && statusMap.containsKey(XMLConstants.ERROR_DESC))
	{
			if (((String) statusMap.get(XMLConstants.ERROR_CODE)).equalsIgnoreCase(XMLConstants.SUCCESS_CODE)
					&& (((String) statusMap.get(XMLConstants.ERROR_DESC))
							.equalsIgnoreCase(XMLConstants.SUCCESS_RESPONSE)))
	    {
		Map<String, Object> headerMap = (Map<String, Object>) responseMap.get(XMLConstants.RESPONSE_HEADER);

		Long reqID = Long.valueOf(String.valueOf(headerMap.get(XMLConstants.UID)));

		if (reqID.equals(new Long(reqUID)))
		{
		    Map dataMap = (Map) responseMap.get(XMLConstants.RESPONSE_DETAILS);

		    createSearchResponseJson(dataMap, responseJSON, entity);
		}
		else
		{
		    responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
		    responseJSON.put(JsonConstants.MESSAGE, (String) statusMap.get(XMLConstants.ERROR_DESC));
		    responseJSON.put(JsonConstants.DATA, "");
		}
	    }
	    else
	    {
		responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
		responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_SETTLEMENT_SEARCH_FAILURE);
		responseJSON.put(JsonConstants.DATA, "");
	    }
	}
    }

    private void createSearchResponseJson(Map dataMap, JSONObject responseJSON, String entity) throws JSONException
    {
	JSONObject dataJSOn = new JSONObject();

	if (dataMap.containsKey(XMLConstants.BUYER_ID)
	/*
	 * &&
	 * !dataMap.get(XMLConstants.BUYER_ID).toString().equalsIgnoreCase("0")
	 */)
	{

	    dataJSOn.put(XMLConstants.BUYER_ID, dataMap.get(XMLConstants.BUYER_ID));

	}

	if (dataMap.containsKey(XMLConstants.BUYER_NAME))
	{

	    dataJSOn.put(XMLConstants.BUYER_NAME, dataMap.get(XMLConstants.BUYER_NAME));

	}

	if (dataMap.containsKey(XMLConstants.PARTY_ID))
	{
	    dataJSOn.put(JsonConstants.PARTY_ID, dataMap.get(XMLConstants.PARTY_ID));
	}
	if (dataMap.containsKey(XMLConstants.PARTY_NAME))
	{
	    dataJSOn.put(JsonConstants.SettlmentConstant.PARTY_NAME, dataMap.get(XMLConstants.PARTY_NAME));
	}

	if (dataMap.containsKey(XMLConstants.REGISTRATION_NUMBER))
	{

	    dataJSOn.put(XMLConstants.REGISTRATION_NUMBER, dataMap.get(XMLConstants.REGISTRATION_NUMBER));
	}

	if (dataMap.containsKey(XMLConstants.APPL))
	{
	    dataJSOn.put(JsonConstants.APPL, dataMap.get(XMLConstants.APPL));
	}
	if (dataMap.containsKey(XMLConstants.APACNUM))
	{
	    dataJSOn.put(JsonConstants.APAC_NO, dataMap.get(XMLConstants.APACNUM));
	}
	if (dataMap.containsKey(XMLConstants.EMI_DUEDATE))
	{
	    dataJSOn.put(JsonConstants.DUE_DATE, dataMap.get(XMLConstants.EMI_DUEDATE));
	}

	if (dataMap.containsKey(XMLConstants.PENAL_AMNT))
	{
	    dataJSOn.put(JsonConstants.SettlmentConstant.PENAL_OUTSTANDING, dataMap.get(XMLConstants.PENAL_AMNT));
	}
	if (dataMap.containsKey(XMLConstants.OVERDUE_AMNT))
	{
	    dataJSOn.put(JsonConstants.OVERDUE, dataMap.get(XMLConstants.OVERDUE_AMNT));
	}
	if (dataMap.containsKey(XMLConstants.TOTAL_OUTSTANDING_AMNT))
	{
	    dataJSOn.put(JsonConstants.OUTSTANDING, dataMap.get(XMLConstants.TOTAL_OUTSTANDING_AMNT));
	}
	if (dataMap.containsKey(XMLConstants.EMI_DUE_AMNT))
	{
	    dataJSOn.put(JsonConstants.SettlmentConstant.EMI_AMOUNT, dataMap.get(XMLConstants.EMI_DUE_AMNT));
	}
	if (dataMap.containsKey(XMLConstants.MOBILE))
	{
	    dataJSOn.put(JsonConstants.MOBILE_NUMBER, dataMap.get(XMLConstants.MOBILE));
	}
	if (dataMap.containsKey(XMLConstants.EMAIL))
	{
	    dataJSOn.put(JsonConstants.EMAIL_ADDRESS, dataMap.get(XMLConstants.EMAIL));
	}
	if (dataMap.containsKey(XMLConstants.PHONE_NUM))
	{
	    dataJSOn.put(JsonConstants.LANDLINE_NUMBER, dataMap.get(XMLConstants.PHONE_NUM));
	}
	if (dataMap.containsKey(XMLConstants.ADDRESS))
	{
	    dataJSOn.put(JsonConstants.CORRESPONDENCE_ADDRESS, dataMap.get(XMLConstants.ADDRESS));
	}

	if (dataMap.containsKey(XMLConstants.APAC_DATE))
	{
	    dataJSOn.put(JsonConstants.APAC_DATE, dataMap.get(XMLConstants.APAC_DATE));
	}

	if (dataMap.containsKey(XMLConstants.TENURE))
	{
	    dataJSOn.put(JsonConstants.TENURE, dataMap.get(XMLConstants.TENURE));
	}

	if (dataMap.containsKey(XMLConstants.EMI_PAID))
	{
	    dataJSOn.put(JsonConstants.SettlmentConstant.EMI_PAID, dataMap.get(XMLConstants.EMI_PAID));

	}

	if (dataMap.containsKey(XMLConstants.BAL_TENURE))
	{
	    dataJSOn.put(JsonConstants.BAL_TENURE, dataMap.get(XMLConstants.BAL_TENURE));
	}

	if (dataMap.containsKey(XMLConstants.PRINCIPAL_OUTSTANDING))
	{
	    dataJSOn.put(JsonConstants.PRINCIPAL_OUTSTANDING, dataMap.get(XMLConstants.PRINCIPAL_OUTSTANDING));
	}

	if (dataMap.containsKey(XMLConstants.FC_CHARGES))
	{
	    dataJSOn.put(JsonConstants.SettlmentConstant.FC_CHARGES, dataMap.get(XMLConstants.FC_CHARGES));
	}

	if (dataMap.containsKey(XMLConstants.BROKEN_REVENUE))
	{
	    dataJSOn.put(JsonConstants.BROKEN_REVENUE, dataMap.get(XMLConstants.BROKEN_REVENUE));
	}

	if (dataMap.containsKey(XMLConstants.LEGAL_CHARGES))
	{
	    dataJSOn.put(JsonConstants.SettlmentConstant.LEGAL_CHARGES, dataMap.get(XMLConstants.LEGAL_CHARGES));
	}

	if (dataMap.containsKey(XMLConstants.BUCKET))
	{
	    dataJSOn.put(JsonConstants.BUCKET, dataMap.get(XMLConstants.BUCKET));
	}

	if (dataMap.containsKey(XMLConstants.FIRST_OSTD_DATE))
	{
	    dataJSOn.put(JsonConstants.FIRST_OSTD_DATE, dataMap.get(XMLConstants.FIRST_OSTD_DATE));
	}

	if (dataMap.containsKey(XMLConstants.CLOSURE_BAL))
	{
	    dataJSOn.put(JsonConstants.CLOSURE_BAL, dataMap.get(XMLConstants.CLOSURE_BAL));
	}

	if (dataMap.containsKey("AGRVALUE"))
	{
	    dataJSOn.put(JsonConstants.LOAN_AMOUNT, dataMap.get("AGRVALUE"));
	}
	// ------newly added by chetan for CWO------
	if (dataMap.containsKey("otherCharges"))
	{
	    dataJSOn.put(JsonConstants.OTHER_CHARGES, dataMap.get("otherCharges"));
	}
	if (dataMap.containsKey("creditLimit"))
	{
	    dataJSOn.put(JsonConstants.CREDIT_LIMIT, dataMap.get("creditLimit"));
	}
	if (dataMap.containsKey("address2"))
	{
	    dataJSOn.put(JsonConstants.ADDRESS2, dataMap.get("address2"));
	}
	if (dataMap.containsKey("address3"))
	{
	    dataJSOn.put(JsonConstants.ADDRESS3, dataMap.get("address3"));
	}
	if (dataMap.containsKey("partyCorrAdd"))
	{
	    dataJSOn.put(JsonConstants.CORRESPONDENCE_ADDRESS, dataMap.get("partyCorrAdd"));
	}
	if (dataMap.containsKey("city"))
	{
	    dataJSOn.put(JsonConstants.CITY, dataMap.get("city"));
	}
	if (dataMap.containsKey("cardNo"))
	{
	    dataJSOn.put(JsonConstants.CARD_NO, dataMap.get("cardNo"));
	}
	if (dataMap.containsKey("pincode"))
	{
	    dataJSOn.put(JsonConstants.PINCODE, dataMap.get("pincode"));
	}
	if (dataMap.containsKey("LPC"))
	{
	    dataJSOn.put(JsonConstants.LPC, dataMap.get("LPC"));
	}
	if (dataMap.containsKey("partyMobNo"))
	{
	    dataJSOn.put(JsonConstants.MOBILE_NUMBER, dataMap.get("partyMobNo"));
	}
	if (dataMap.containsKey("principleoutstanding"))
	{
	    dataJSOn.put(JsonConstants.PRINCPLE_OUTSTANDING, dataMap.get("principleoutstanding"));
	}
	if (dataMap.containsKey("interest"))
	{
	    dataJSOn.put(JsonConstants.INTREST, dataMap.get("interest"));
	}
	if (dataMap.containsKey("name"))
	{
	    dataJSOn.put(JsonConstants.NAME, dataMap.get("name"));
	}
	if (dataMap.containsKey("CD"))
	{
	    dataJSOn.put(JsonConstants.CD, dataMap.get("CD"));
	}
	if (dataMap.containsKey("currentBalnance"))
	{
	    dataJSOn.put(JsonConstants.CURRENT_BALANCE, dataMap.get("currentBalnance"));
	}
	if (dataMap.containsKey("mob"))
	{
	    dataJSOn.put(JsonConstants.MOB, dataMap.get("mob"));
	}
	if (dataMap.containsKey("billingCycle"))
	{
	    dataJSOn.put(JsonConstants.BILLINGCYCLE, dataMap.get("billingCycle"));
	}
	if (dataMap.containsKey("principalAmount"))
	{
	    dataJSOn.put(JsonConstants.PRINCIPAL_AMOUNT, dataMap.get("principalAmount"));
	}
	responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
	responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_SETTLEMENT_SEARCH_SUCCESS);
	responseJSON.put(JsonConstants.DATA, dataJSOn);

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

	private long createCardSearchRequestMap(JSONObject requestData, String entity, Map<String, Object> wrapperMap)
			throws JSONException
	{
		Map<String, Object> requestMap = new HashMap<String, Object>();

		Map<String, Object> requestDetails = new HashMap<String, Object>();
		String uniqueId = requestData.getString(JsonConstants.CARD_NO);

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

	private void submitRandomCollections(JSONObject responseJSON, JSONObject requestData, SystemUser systemUser,
			String requestEntity, UserActivity userActivity, CommunicationActivityService communicationActivityService)
			throws JSONException
    {

    }

}