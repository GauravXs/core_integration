package com.mobicule.mcollections.integration.upi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.mobicule.component.activitylogger.beans.CommunicationActivity;
import com.mobicule.component.activitylogger.commons.ActivityLoggerConstants;
import com.mobicule.component.activitylogger.service.CommunicationActivityService;
import com.mobicule.component.activitylogger.threads.CommunicationActivityAddition;
import com.mobicule.component.activitylogger.threads.CommunicationActivityStatusUpdate;
import com.mobicule.component.mapconversion.xml.MapToXML;
import com.mobicule.component.mapconversion.xml.XMLToMap;
import com.mobicule.mcollections.core.beans.Collection;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.AesUtil;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.UpiUtility;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.CollectionService;
import com.mobicule.mcollections.integration.commons.ServerUtilities;
import com.mobicule.mcollections.webservice.adapter.KotakCollectionWebserviceAdapter;

public class UPICallBackURL extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private CollectionService collectionService;
	@Autowired
	private CommunicationActivityService communicationActivityService;
	
	
	@Autowired
	ApplicationConfiguration applicationConfiguration;

	public UPICallBackURL()
	{
		super();
	}

	public void init(ServletConfig config) throws ServletException {
		    super.init(config);
		    /*SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this,
		      config.getServletContext());*/
		    SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
		  }
	 
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException
	{
		try
		{
			
		
			String imeiNo = Constants.EMPTY_STRING;
			String draMobileNumber = Constants.EMPTY_STRING;
			Long userTableId = 0L;
			String contactNo=Constants.EMPTY_STRING;
			String alterContactNo=Constants.EMPTY_STRING;
			String appl=Constants.EMPTY_STRING;
			String apac=Constants.EMPTY_STRING;
			String amount=Constants.EMPTY_STRING;
			
			
			AesUtil aes = new AesUtil("7F7E22D057BBD20D0DCBD232FF57F783");
			if (log.isInfoEnabled())
			{
				log.info("-----In doPost-----");
			}
	
			String callBackResponse = readBody(request);		
					
			if(!StringUtils.isEmpty(callBackResponse))
			{
			
				Map<String, String> callBackResponseInMap = UpiUtility.jsonToMap(callBackResponse);
				log.info("callBackResponseInMap " + callBackResponseInMap);

				Map<Object, Object> updateActivityMap = new HashMap<Object, Object>();
				
				//updateActivityMap.put(Constants.AllPayCollectionsDao.MODIFIED_BY, "");
				updateActivityMap.put(Constants.UpiCollectionsDao.RESPONSE, callBackResponse);
				updateActivityMap.put(Constants.UpiCollectionsDao.TRANSACTIONREFERENCENUMBER,callBackResponseInMap.get("transactionreferencenumber"));
				updateActivityMap.put(Constants.UpiCollectionsDao.AMOUNT, callBackResponseInMap.get("amount").toString());
				updateActivityMap.put(Constants.UpiCollectionsDao.STATUS, callBackResponseInMap.get("status").toString());
				updateActivityMap.put(Constants.UpiCollectionsDao.STATUSCODE, callBackResponseInMap.get("statusCode").toString());
				updateActivityMap.put(Constants.UpiCollectionsDao.TRANSACTIONID, callBackResponseInMap.get("transactionid").toString());
				updateActivityMap.put(Constants.UpiCollectionsDao.REFID, callBackResponseInMap.get("refid").toString());
				updateActivityMap.put(Constants.UpiCollectionsDao.TRANSACTIONTIMESTAMP, callBackResponseInMap.get("transactionTimestamp").toString());
				//updateActivityMap.put(Constants.UpiCollectionsDao.MODIFIED_BY,callBackResponseInMap.get("refid"));
				updateActivityMap.put(Constants.CALLBACKTYPE, Constants.UPI);
				
				List<Map<String, Object>> list = collectionService.getDRAMobileNumber(callBackResponseInMap.get("refid"));
				
				if (list != null && !list.isEmpty())

				{
					for (int i = 0; i < list.size(); i++)
					{

						Map<String, Object> mapOfList = list.get(i);

						
							draMobileNumber=mapOfList.get(Constants.AllPayCollectionsDao.MOBILE_NUMBER)==null?Constants.EMPTY_STRING:mapOfList.get(Constants.AllPayCollectionsDao.MOBILE_NUMBER).toString();
						
							imeiNo=(String)mapOfList.get(Constants.AllPayCollectionsDao.IMEI_NO);
						
							userTableId=(Long)mapOfList.get(Constants.AllPayCollectionsDao.ID);
						   
							contactNo=mapOfList.get("CONTACT")==null?Constants.EMPTY_STRING:mapOfList.get("CONTACT").toString();

							alterContactNo=mapOfList.get("ALTERNATE_MOBILE_NUMBER")==null?Constants.EMPTY_STRING:mapOfList.get("ALTERNATE_MOBILE_NUMBER").toString();
							
							appl=mapOfList.get("APPL")==null?Constants.EMPTY_STRING:mapOfList.get("APPL").toString();
							
							amount=mapOfList.get("AMOUNT")==null?Constants.EMPTY_STRING:mapOfList.get("AMOUNT").toString();
							
							apac=mapOfList.get("BUSINESS_PARTNER_NUMBER")==null?Constants.EMPTY_STRING:mapOfList.get("BUSINESS_PARTNER_NUMBER").toString();
					}

					}
				
				log.info("contactNo"+contactNo);
				log.info("imeiNo"+imeiNo);
				log.info("userTableId"+userTableId);

				log.info("appl"+appl);
				log.info("draMobileNumber"+draMobileNumber);


				
				
				Collection collection = new Collection();
				collection.setAppl(appl);
				collection.setMobileNumber(contactNo);
				collection.setAlternateContactNo(alterContactNo);
				collection.setMobileNumberNew(draMobileNumber);
				collection.setAmount(amount);
				collection.setReceiptNumber(callBackResponseInMap.get("refid"));
				collection.setBusinessPartnerNumber(apac);
				String status=Constants.EMPTY_STRING;
				boolean updateflag = collectionService.callBackOnlinePaymentActivityUpdation(updateActivityMap);
				
				log.info("updateflag----------"+updateflag);
				
				if(callBackResponseInMap.containsKey("status") && callBackResponseInMap.get("status").equalsIgnoreCase("SUCCESS"))
				{
					log.info("containsKey-------"+callBackResponseInMap.containsKey("status"));
					
				if(updateflag != true)
				{
					
					
					collection.setOnlinePaymentStatus(callBackResponseInMap.get("status").toString());
				//	collection.setPaymentStatusDescription(callBackResponseInMap.get("order_status").toString());
					collection.setInvoiceId(callBackResponseInMap.get("transactionid"));
					
					int addCount = collectionService.callBackOnlinePaymentActivityAddition(updateActivityMap);
					
					if(addCount > 0)
					{
						status="SUCCESS";
						boolean collectionUpdatedFlag = collectionService.updateAllPayCollection(collection);
						if(collectionUpdatedFlag == true){
						log.info("Call back all pay response added in collection succesfully SUCCESS");	
						sendAllPayStatusSms(collection,imeiNo,userTableId, status);
						}
						else
						{
				        status="FAILURE";
						log.info("Call back all pay response not added in online payment history FAILURE ");
						sendAllPayStatusSms(collection,imeiNo,userTableId, status);
						}

					}
					else
					{
						status="FAILURE";
						log.info("Error to add in online payment history Call back all pay response");
					//	sendAllPayStatusSms(collection,imeiNo,userTableId, status);
					}
					
				}
				else
				{

					boolean collectionUpdatedFlag = collectionService.updateAllPayCollection(collection);
					if(collectionUpdatedFlag == true){
					log.info("Call back all pay response added in collection succesfully SUCCESS");	
					sendAllPayStatusSms(collection,imeiNo,userTableId, status);
					}
					else
					{
			        status="FAILURE";
					log.info("Call back all pay response not added in online payment history FAILURE ");
					sendAllPayStatusSms(collection,imeiNo,userTableId, status);
					}

				
				}
				
				
				}
				else
				{
					status="FAILURE";
				//	sendAllPayStatusSms(collection,imeiNo,userTableId, status);

					log.info("Call back all pay response status received failure updated in online payment history");
				}
				
				//collectionService.submitCallBackFromKotak(map);
				
				/*sendAllPayStatusSms(collection,user,status);// Deploy	*/			
				
			}
			else
			{
				log.info("Call back all pay response is null");
			}

		}
		catch (Exception e)
		{

			log.error("---Exception---", e);
			e.printStackTrace();

		}

	}

	private String readBody(HttpServletRequest request) throws IOException
	{
		InputStream is = request.getInputStream();

		ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();

		byte[] buf = new byte[1000];

		for (int nChunk = is.read(buf); nChunk != -1; nChunk = is.read(buf))
		{
			arrayOutputStream.write(buf, 0, nChunk);
		}

		if (log.isInfoEnabled())
		{
			log.info("");
		}

		String data = new String(arrayOutputStream.toByteArray(), "utf-8");

		return data;
	}
	
	
	private void sendAllPayStatusSms(Collection collection,String imeiNo,Long userTableId, String status)
	{

	SystemUser systemUser=new SystemUser();
	systemUser.setImeiNo(imeiNo);
	systemUser.setUserTableId(userTableId);
	
	log.info("collection.getMobileNumber()-------"+collection.getMobileNumber());
	log.info("collection.getAlternateContactNo()-------"+collection.getAlternateContactNo());
	log.info("collection.getMobileNumberNew()-------"+collection.getMobileNumberNew());

	if(status.equalsIgnoreCase("SUCCESS")){
		
		if (collection.getMobileNumber() != null
				&& !collection.getMobileNumber().equalsIgnoreCase(Constants.EMPTY_STRING))
		{
			log.info("Sending sms to customer mobile number");

			allPaySMSToCustomerForSuccessfullPayment(collection, systemUser, collection.getMobileNumber(), status);
		}

		if (collection.getAlternateContactNo() != null
				&& !collection.getAlternateContactNo().equalsIgnoreCase(Constants.EMPTY_STRING) )
		{
			log.info("Sending sms to customer mobile number");

			allPaySMSToCustomerForSuccessfullPayment(collection, systemUser, collection.getAlternateContactNo(), status);
		}
	}
		
		if (collection.getMobileNumberNew() != null
				&& !collection.getMobileNumberNew().equalsIgnoreCase(Constants.EMPTY_STRING))
		{
			log.info("Sending sms to DRA mobile number " + collection.getAmount());

			allPaySMSToDRAForSuccessfullPayment(collection.getAmount(), collection.getReceiptNumber(),
					collection.getPaymentMode(), collection.getAppl(),
					collection.getFeName(),collection.getMobileNumberNew(), systemUser, communicationActivityService, collection, status);
		
		}

	}
	
	private void allPaySMSToCustomerForSuccessfullPayment(Collection collection, SystemUser systemUser,
			String mobilenumber, String status)
	{
		log.info("---- Inside allPaySMSToCustomerForSuccessfullPayment --------");

		String webserviceUrl = Constants.EMPTY_STRING;

		String appl = collection.getAppl();

		if (appl.equalsIgnoreCase("RSM"))
		{

			webserviceUrl = (String) applicationConfiguration.getValue("RSM_WEB_SERVICE_URL_SMS_DISPATCHER");
		}

		else
		{

			webserviceUrl = (String) applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");

		}

		log.info("webserviceUrl----"+webserviceUrl);
		
		String amount = String.valueOf(collection.getAmount());

		Map<String, Object> smsDispatcherMap = ServerUtilities.generateAllPayStatusSMSMapForCustomer(amount,
				collection.getReceiptNumber(),collection.getBusinessPartnerNumber(), mobilenumber, appl,
				status);

		StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true, new HashMap<String, String>());

		CommunicationActivityAddition communicationActivityAddition = new CommunicationActivityAddition(systemUser
				.getUserTableId().toString(), systemUser.getImeiNo(), (appl + "_" + collection.getCollectionType()),
				webserviceUrl, xmlRequest.toString(), communicationActivityService,
				ActivityLoggerConstants.DATABASE_MSSQL);

		new Thread(communicationActivityAddition).run();

		KotakCollectionWebserviceAdapter kotakCollectionWebserviceAdapter = new KotakCollectionWebserviceAdapter();

		String xmlResponse = kotakCollectionWebserviceAdapter.callWebserviceAndGetXmlString(xmlRequest.toString(),
				webserviceUrl);

		CommunicationActivity communicationActivity = communicationActivityAddition.extractCommunicationActivity();

		Map<String, Object> result = null;

		if (null != xmlResponse && !xmlResponse.equals(Constants.EMPTY_STRING))
		{
			communicationActivity.setResponse(xmlResponse);

			CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
					communicationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), communicationActivityService);

			new Thread(communicationActivityStatusUpdate).run();

			result = XMLToMap.convertXMLToMap(xmlResponse);

		}
		else
		{
			communicationActivity.setResponse(ActivityLoggerConstants.EMPTY_STRING);

			CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
					communicationActivity, (ActivityLoggerConstants.STATUS_FAILURE), communicationActivityService);

			new Thread(communicationActivityStatusUpdate).run();

			log.info("----- Failure in sending SMS : -------");
		}
	}

	private void allPaySMSToDRAForSuccessfullPayment(String amount, String receiptNumber, String paymentType,
			String appl, String feName, String mobileNumber,SystemUser systemUser,
			CommunicationActivityService communicationActivityService, Collection collection, String status)
	{
		log.info("---- Inside allPaySMSToDRAForSuccessfullPayment  getUserTableId--------"+systemUser.getUserTableId());
		String webserviceUrl = Constants.EMPTY_STRING;

		if (appl.equalsIgnoreCase("RSM"))
		{

			webserviceUrl = (String) applicationConfiguration.getValue("RSM_WEB_SERVICE_URL_SMS_DISPATCHER");
		}

		else
		{

			webserviceUrl = (String) applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");

		}

		log.info("webserviceUrl----"+webserviceUrl);

		Map<String, Object> smsDispatcherMap = ServerUtilities.generateAllPayStatusSMSMapForFE(amount, receiptNumber,
				paymentType,appl, feName, mobileNumber, status, collection.getBusinessPartnerNumber());

		log.info("---- Inside smsDispatcherMap --------" + smsDispatcherMap);

		StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true, new HashMap<String, String>());

		log.info("---- Inside xmlRequest --------" + xmlRequest);

		CommunicationActivityAddition communicationActivityAddition = new CommunicationActivityAddition(systemUser
				.getUserTableId().toString(), systemUser.getImeiNo(), (appl + "_" + collection.getCollectionType()),
				webserviceUrl, xmlRequest.toString(), communicationActivityService,
				ActivityLoggerConstants.DATABASE_MSSQL);

		new Thread(communicationActivityAddition).run();

		KotakCollectionWebserviceAdapter kotakCollectionWebserviceAdapter = new KotakCollectionWebserviceAdapter();

		String xmlResponse = kotakCollectionWebserviceAdapter.callWebserviceAndGetXmlString(xmlRequest.toString(),
				webserviceUrl);

		CommunicationActivity communicationActivity = communicationActivityAddition.extractCommunicationActivity();

		Map<String, Object> result = null;

		if (null != xmlResponse && !xmlResponse.equals(Constants.EMPTY_STRING))
		{
			communicationActivity.setResponse(xmlResponse);

			CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
					communicationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), communicationActivityService);

			new Thread(communicationActivityStatusUpdate).run();

			result = XMLToMap.convertXMLToMap(xmlResponse);

		}
		else
		{
			communicationActivity.setResponse(ActivityLoggerConstants.EMPTY_STRING);

			CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
					communicationActivity, (ActivityLoggerConstants.STATUS_FAILURE), communicationActivityService);

			new Thread(communicationActivityStatusUpdate).run();

			log.info("----- Failure in sending SMS : -------");
		}
	}


}
