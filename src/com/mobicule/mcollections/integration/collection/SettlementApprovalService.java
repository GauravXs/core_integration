/**
 * 
 */
package com.mobicule.mcollections.integration.collection;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONArray;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.mobicule.component.activitylogger.beans.CommunicationActivity;
import com.mobicule.component.activitylogger.commons.ActivityLoggerConstants;
import com.mobicule.component.activitylogger.service.CommunicationActivityService;
import com.mobicule.component.activitylogger.service.UserActivityService;
import com.mobicule.component.activitylogger.threads.CommunicationActivityAddition;
import com.mobicule.component.activitylogger.threads.CommunicationActivityStatusUpdate;
import com.mobicule.component.activitylogger.threads.UserActivityAddition;
import com.mobicule.component.mapconversion.xml.MapToXML;
import com.mobicule.component.mapconversion.xml.XMLToMap;
import com.mobicule.component.usermapping.bean.Territory;
import com.mobicule.mcollections.core.beans.Portfolio;
import com.mobicule.mcollections.core.beans.Settlement;
import com.mobicule.mcollections.core.beans.SettlementConfigurationMapping;
import com.mobicule.mcollections.core.beans.SettlementEMIDetail;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.PortfolioService;
import com.mobicule.mcollections.core.service.SettlementService;
import com.mobicule.mcollections.core.service.SystemUserService;
import com.mobicule.mcollections.integration.commons.ServerUtilities;
import com.mobicule.mcollections.webservice.adapter.KotakCollectionWebserviceAdapter;
import com.mobicule.mcollections.core.commons.Utilities;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamImplicitCollection;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * @author amol
 *
 * Kakade
 */
public class SettlementApprovalService implements ICollectionsSubmissionService
{

	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private SettlementService settlementService;

	@Autowired
	private UserActivityService userActivityService;

	@Autowired
	private PortfolioService portfolioService;

	@Autowired
	private CommunicationActivityService communicationActivityService;

	@Autowired
	ApplicationConfiguration applicationConfiguration;

	@Autowired
	private SystemUserService systemUserService;

	@Override
	public Message<String> execute(Message<String> message) throws Throwable
	{
		log.info("--- In SettlementApprovalService / Method execute --- ");

		String CibilDownloadURL = (String) applicationConfiguration.getValue("CIBIL_DOWNLOAD_URL");

		long userTableId = 0L;

		String flag = "DEVICE";

		JSONObject responseJSON = new JSONObject();

		try
		{
			String requestSet = message.getPayload();
			String requestAppl = JSONPayloadExtractor.extract(requestSet, JsonConstants.ENTITY);

			UserActivityAddition userActivityAddition = new UserActivityAddition(requestSet, userActivityService,
					ActivityLoggerConstants.DATABASE_MSSQL);

			new Thread(userActivityAddition).run();

			JSONObject requestSystemUser = JSONPayloadExtractor.extractJSON(requestSet, JsonConstants.SYSTEM_USER);

			SystemUser sysUser = ServerUtilities.extractSystemUser(requestSystemUser);

			userTableId = sysUser.getUserTableId();

			log.info("--- User Table Id :: " + userTableId);

			List<Portfolio> portfolioList = new ArrayList<Portfolio>();

			portfolioList = portfolioService.getAllPortfolio();

			long portfolioId = 0L;

			for (int i = 0; i < portfolioList.size(); i++)
			{
				if (requestAppl.equalsIgnoreCase(portfolioList.get(i).getPortfolioCode()))
				{
					portfolioId = portfolioList.get(i).getPortfolioId();
					break;
				}
			}

			List<Long> listOfUserProfile = systemUserService.getUserProfileList(userTableId);
			listOfUserProfile.add(6L);

			log.info("------profile ID list ----" + listOfUserProfile);

			Map<String, List<SettlementConfigurationMapping>> userMatrixDataMap = settlementService
					.getUserMatrixMappingDataDevice(userTableId, portfolioId, listOfUserProfile); //2769

			String roleId = settlementService.retriveUserRoleId(userTableId);
			List<Territory> territoriyList = sysUser.getTerritoryList();
			Map<String, Object> searchDataMap = new HashMap<String, Object>();
			String cibilScore = (String) applicationConfiguration.getValue("cibilScore");
			searchDataMap.put("status", "");
			searchDataMap.put("appl", "");
			searchDataMap.put("apacNo", "");
			searchDataMap.put("settlementFromDate", "");
			searchDataMap.put("settlementToDate", "");

			searchDataMap.put("cibilScore", cibilScore);

			searchDataMap.put("profileIdList", listOfUserProfile);

			searchDataMap.put("userTableId", userTableId);

			List<Settlement> settList = settlementService.getApprovalRequests(userMatrixDataMap, requestAppl, flag,
					roleId, territoriyList, listOfUserProfile, searchDataMap);

			JSONArray data = new JSONArray();

			/*			ArrayList<String> emiDetailsJSON=new ArrayList<String>();*/

			if (settList != null && settList.size() != 0)
			{
				for (Settlement settlement : settList)
				{
					Map<String, String> objecMap = new HashMap<String, String>();
					JSONObject dataObj = new JSONObject();

					//log.info("settlement.getApacNo() ----------->" + settlement.getApacNo());
					dataObj.put("apacNo", settlement.getApacNo());

					dataObj.put("settlementId", settlement.getSettlementId());
					dataObj.put(JsonConstants.SettlmentConstant.FINCIAL_LOSS_ON_FR, settlement.getTotalFR());
					dataObj.put(JsonConstants.SettlmentConstant.SETTLEMENT_AMNT, settlement.getSettlementAmount());
					dataObj.put(JsonConstants.SettlmentConstant.NUMBER_OF_EMI, settlement.getEmipaid());
					dataObj.put("noOfEmiPaid", settlement.getNumberOfEmi());
					dataObj.put(JsonConstants.SettlmentConstant.FINCIAL_LOSS_ON_FR, settlement.getTotalFR());
					dataObj.put(JsonConstants.SettlmentConstant.EMI_AMOUNT, settlement.getEmiAmount());
					dataObj.put(JsonConstants.SettlmentConstant.LEGAL_STATUS, settlement.getStatus());
					dataObj.put("Appl", settlement.getAppl());
					dataObj.put(JsonConstants.SettlmentConstant.APAC_DATE, settlement.getApacdate());
					dataObj.put(JsonConstants.SettlmentConstant.PARTY_NAME, settlement.getPartyName());
					dataObj.put(JsonConstants.SettlmentConstant.LOAN_AMOUNT, settlement.getLoanAmnt());
					dataObj.put(JsonConstants.SettlmentConstant.BALNACE_TENURE, settlement.getBaltenure());
					dataObj.put(JsonConstants.SettlmentConstant.PRINCIPAL_OUTSTANDING, settlement.getPreOut());
					dataObj.put(JsonConstants.SettlmentConstant.O_PLUS_S, settlement.getoPLUSs());
					dataObj.put(JsonConstants.SettlmentConstant.FC_CHARGES, settlement.getFcCharges());
					dataObj.put(JsonConstants.SettlmentConstant.NUMBER_OF_STROKE, settlement.getNoofstroke());
					dataObj.put(JsonConstants.SettlmentConstant.DETAIL_PDC, settlement.getDetailsPDC());
					dataObj.put(JsonConstants.SettlmentConstant.ACCOUNT_LOSS_ON_POS, settlement.getAccLossPOSVal());
					dataObj.put(JsonConstants.SettlmentConstant.FINACIAL_LOSS, settlement.getFinacialLoss());
					dataObj.put(JsonConstants.SettlmentConstant.ACCOUNTING_LOSS, settlement.getAccountingLoss());
					dataObj.put(JsonConstants.SettlmentConstant.FINCIAL_LOSS_ON_FR, settlement.getFinLossFRAmnt());
					dataObj.put(JsonConstants.SettlmentConstant.RECOVERY, settlement.getRecovery());
					dataObj.put(JsonConstants.SettlmentConstant.DPD, settlement.getDPD());
					dataObj.put(JsonConstants.SettlmentConstant.BUCKET, settlement.getBucket());
					dataObj.put(JsonConstants.SettlmentConstant.LEGAL_STATUS, settlement.getLegalStatus());
					dataObj.put(JsonConstants.SettlmentConstant.LOAN_RECALL_NOTICE_DATE,
							settlement.getLoanRecallNoticeSentDate());
					dataObj.put(JsonConstants.SettlmentConstant.SECTION138_DATE, settlement.getSec138InitDate());
					dataObj.put(JsonConstants.SettlmentConstant.ARBITRATION_DATE, settlement.getArbitrationInitDate());
					dataObj.put(JsonConstants.SettlmentConstant.PAYMENT_COLL_DATE, settlement.getPayCollDate());
					dataObj.put(JsonConstants.SettlmentConstant.SETTLMENT_REASON, settlement.getReasonForSettlement());
					dataObj.put(JsonConstants.SettlmentConstant.TOTAL_OUTSTANDING, settlement.getTotalOutstand());
					dataObj.put(JsonConstants.SettlmentConstant.OTHER_INFO, settlement.getOtherInfo());
					dataObj.put(JsonConstants.SettlmentConstant.PARTY_ID, settlement.getPartyId());
					dataObj.put(JsonConstants.SettlmentConstant.TENURE, settlement.getTenure());
					dataObj.put(JsonConstants.SettlmentConstant.SETTLEMENT_TERM, settlement.getSettlmentTerm());
					dataObj.put(JsonConstants.SettlmentConstant.PENAL_WAVER, settlement.getPenalWaiver());
					dataObj.put(JsonConstants.SettlmentConstant.PRINCIPLE_WAVER, settlement.getPrincipleWaiver());
					dataObj.put(JsonConstants.CARD_NO, settlement.getCardNumber());
					dataObj.put(JsonConstants.PRINCIPAL_AMOUNT, settlement.getPrincipalAmount());
					dataObj.put(JsonConstants.INTREST, settlement.getInterest());
					dataObj.put("lpc", settlement.getLpc());
					dataObj.put(JsonConstants.OTHER_CHARGES, settlement.getOtherCharges());
					dataObj.put(JsonConstants.CURRENT_BALANCE, settlement.getCurrentBalance());
					dataObj.put(JsonConstants.CD, settlement.getCd());
					dataObj.put(JsonConstants.CENTER_MGT_NAME, settlement.getCentreManagerName());
					dataObj.put(JsonConstants.ADDRESS, settlement.getAddress1());
					dataObj.put(JsonConstants.ADDRESS2, settlement.getAddress2());
					dataObj.put(JsonConstants.ADDRESS3, settlement.getAddress3());
					dataObj.put(JsonConstants.CITY, settlement.getCity());
					dataObj.put(JsonConstants.PINCODE, settlement.getPin());
					dataObj.put(JsonConstants.MOBILE_NUMBER, settlement.getMobileNumber());
					dataObj.put(JsonConstants.CYCLE_DATE_APPROVAL, settlement.getCycleDate());
					dataObj.put(JsonConstants.WAIVER_LOSS, settlement.getWaiverOrLoss());
					dataObj.put(JsonConstants.MOB, settlement.getMobileNumber());
					dataObj.put(JsonConstants.CREDIT_LIMIT, settlement.getCreditLimit());
					dataObj.put(JsonConstants.E_Block_Done_Or_Not, settlement.geteBlockStatus());
					dataObj.put(JsonConstants.EACH_EMI, settlement.getEachEmi());
					dataObj.put(JsonConstants.Last_EMI_Date, settlement.getLastEmiDate());
					dataObj.put("totalFrAsOnDate", settlement.getTotalFR());
					dataObj.put("brokenrevenue", settlement.getBrokenrevenue());
					dataObj.put("closurebal", settlement.getClosurebal());
					dataObj.put("penalOstd", settlement.getPenalOstd());
					dataObj.put("firstostddate", settlement.getFirstostddate());
					dataObj.put("npv", settlement.getNpv());
					dataObj.put("legalcharges", settlement.getLegalcharges());
					dataObj.put("cibilScore", settlement.getCibilScore());
					dataObj.put(JsonConstants.CIBIL_REPORT_URL, CibilDownloadURL + settlement.getCibilReportUrl());
					dataObj.put(JsonConstants.SettlmentConstant.PROMO_CODE, settlement.getPromoCode());
					dataObj.put(JsonConstants.SettlmentConstant.CASE_BRIEF, settlement.getCaseBrief());

					//dataObj.put(JsonConstants.REMARKS, settlement.get);

					Map<String, String> settlementMap = new HashMap<String, String>();

					settlementMap.put("SETTLEMENT_ID", String.valueOf(settlement.getSettlementId()));

					List<SettlementEMIDetail> emiDetailsList = settlementService.getEmiDetails(settlementMap);

					JSONArray emiDetails = new JSONArray();
					for (SettlementEMIDetail indvEmiDetl : emiDetailsList)
					{

						JSONObject emiObj = new JSONObject();

						emiObj.put("emiDate", indvEmiDetl.getEmiDate());
						emiObj.put("bankName", indvEmiDetl.getBankName());
						emiObj.put("emiAmount", indvEmiDetl.getEmiAmount());
						emiObj.put("chequeNo", indvEmiDetl.getChequeNo());
						emiDetails.put(emiObj);

					}
					dataObj.put("emiDetails", emiDetails);

					dataObj.put(JsonConstants.SettlmentConstant.PRINCIPLE_WAVER, settlement.getPrincipleWaiver());

					data.put(dataObj);
				}

				if (data.length() == 0)
				{
					responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
					responseJSON.put(JsonConstants.MESSAGE, "NO DATA");
					responseJSON.put("data", data);
					return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders())
							.build();
				}

				responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
				responseJSON.put(JsonConstants.MESSAGE, "DATA FOUND");
				responseJSON.put("data", data);
				log.info("JsonResponse For Search Approval :- " + responseJSON);
				return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders())
						.build();
			}
			else
			{
				responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
				responseJSON.put(JsonConstants.MESSAGE, "NO DATA");
				responseJSON.put("data", data);
				return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders())
						.build();
			}

		}
		catch (Exception e)
		{
			log.info("--- Exception In SettlementApprovalService / Method execute----");
			log.info("Exception"+e);
			e.printStackTrace();
			responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
			responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_SETTLEMENT_APPROVAL_SEARCH_FAILURE);
			responseJSON.put(JsonConstants.DATA, "");
		}

		log.info("--- End Of SettlementApprovalService / Method execute --- ");
		return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
	}

	public Message<String> submitApprovalStatus(Message<String> message) throws Throwable
	{
		log.info("--- In submitApprovalStatus / Method execute --- ");

		JSONObject responseJSON = new JSONObject();

		String webserviceUrl = "https://172.22.12.116:443/vmx";

		String appl = "";
		String apacNumber = "";
		int validityCount = 0;

		try
		{
			String requestSet = message.getPayload();
			log.info("requestSet ---> " + requestSet);

			JSONObject jsonObj = new JSONObject(requestSet);

			log.info("jsonobj =-=-=-=-=->" + jsonObj);

			appl = (String) jsonObj.get("entity");
			JSONArray jsonArr = jsonObj.getJSONArray(JsonConstants.DATA);

			JSONObject user = (JSONObject) jsonObj.get(JsonConstants.SYSTEM_USER);
			SystemUser systemUser = ServerUtilities.extractSystemUser(user);

			log.info("jsonArr =-=-=-=-=->" + jsonArr);

			List<Map<String, Object>> approveList = new ArrayList<Map<String, Object>>();

			for (int i = 0; i < jsonArr.length(); i++)
			{
				Map<String, Object> approvalMap = new HashMap<String, Object>();
				//approvalMap.put("Username",systemUser.getUsername());
				/*for action takenby column */
				approvalMap.put("Username", systemUser.getUserTableId().toString());
				JSONObject object = jsonArr.getJSONObject(i);
				log.info("in for object =-=-=-=-=->" + object);

				if (object.has("apacNo"))
				{

					apacNumber = object.getString("apacNo");
				}

				if (object.has("status") && object.getString("status").equalsIgnoreCase("Approve"))
				{

					if (object.has("settlementId"))
					{
						approvalMap.put("settlementId", object.getString("settlementId"));
					}
					if (object.has("status"))
					{
						approvalMap.put("status", Constants.Settlement.STATUS_APPROVED);
					}
					if (object.has("remark"))
					{
						approvalMap.put("remark", object.getString("remark"));
					}

					approveList.add(approvalMap);

				}
				if (object.has("status") && object.getString("status").equalsIgnoreCase("Reject"))
				{

					if (object.has("settlementId"))
					{
						approvalMap.put("settlementId", object.getString("settlementId"));
					}
					if (object.has("status"))
					{
						approvalMap.put("status", Constants.Settlement.STATUS_REJECTED);
					}
					if (object.has("remark"))
					{
						String remark = object.getString("remark");
						if (Utilities.checkInputForValidity(remark))
						{
							validityCount = validityCount + 1;
							log.info("--- validityCount = " + validityCount + " remark = " + remark);
						}
						approvalMap.put("remark", remark);
					}

					approveList.add(approvalMap);

				}
				if (object.has("status") && object.getString("status").equalsIgnoreCase("Hold"))
				{

					if (object.has("settlementId"))
					{
						approvalMap.put("settlementId", object.getString("settlementId"));
					}
					if (object.has("status"))
					{
						approvalMap.put("status", Constants.Settlement.STATUS_HOLD);
					}
					if (object.has("remark"))
					{
						approvalMap.put("remark", object.getString("remark"));
					}

					approveList.add(approvalMap);

				}

			}
			log.info("approveList =-=-=-=-=->" + approveList);
			if (validityCount >= 1)
			{
				log.info("--------- Validity Count is greater than zero ------------");

				responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
				responseJSON.put(JsonConstants.MESSAGE, "Invalid data!!!");
				responseJSON.put(JsonConstants.DATA, "");
				return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders())
						.build();
			}
			if (settlementService.updateSettlementApproveRequest(approveList))
			{

				try
				{

					if (approveList.size() > 0)
					{

						Settlement settlement = settlementService.getCollectorDetailsForSettlementSMS(approveList
								.get(0).get("settlementId").toString());

						settlement.setStatus(approveList.get(0).get("status").toString());

						try
						{

							sendSms(settlement, systemUser);
						}
						catch (Exception e)
						{

							log.error("---Failure in SMS sent ----", e);

						}

						if (approveList.get(0).get("status").toString().equalsIgnoreCase("APPROVED")
								&& appl.equalsIgnoreCase("CWO") && !apacNumber.isEmpty())
						{

							String xmlRequest = generateXmlRequest(apacNumber);

							log.info("generated xml request :---------> -->" + xmlRequest);

							CommunicationActivityAddition communicationActivityAddition = new CommunicationActivityAddition(
									systemUser.getUserTableId().toString(), systemUser.getImeiNo(),
									(settlement.getAppl() + "_" + "CardBlocking"), webserviceUrl,
									xmlRequest.toString(), communicationActivityService,
									ActivityLoggerConstants.DATABASE_MSSQL);

							new Thread(communicationActivityAddition).run();

							KotakCollectionWebserviceAdapter kotakCollectionWebserviceAdapter = new KotakCollectionWebserviceAdapter();

							String xmlResponse = kotakCollectionWebserviceAdapter.callWebserviceAndGetXmlString(
									xmlRequest.toString(), webserviceUrl);

							CommunicationActivity communicationActivity = communicationActivityAddition
									.extractCommunicationActivity();

							if (null != xmlResponse && !xmlResponse.equals(""))
							{
								communicationActivity.setResponse(xmlResponse);

								CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
										communicationActivity, (ActivityLoggerConstants.STATUS_SUCCESS),
										communicationActivityService);

								new Thread(communicationActivityStatusUpdate).run();

							}
							else
							{
								communicationActivity.setResponse(ActivityLoggerConstants.EMPTY_STRING);

								CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
										communicationActivity, (ActivityLoggerConstants.STATUS_FAILURE),
										communicationActivityService);

								new Thread(communicationActivityStatusUpdate).run();

								log.info("----- Failure in Blocking Card : -------");
							}

							log.info("xmlResponse to block card --------> --->" + xmlResponse);
						}

					}

				}

				catch (Exception e)
				{

					log.error("----- Exception Details ----- ", e);

				}

				responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
				responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_SETTLEMENT_APPROVAL_UPDATE_SUCCESS);
				responseJSON.put(JsonConstants.DATA, "");
				log.info("Response Json In Approval Service :-" + responseJSON);

			}
		}
		catch (Exception e)
		{
			// TODO: handle exception

			log.error("--- Exception In submitApprovalStatus / Method execute----", e);

			responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
			responseJSON.put(JsonConstants.MESSAGE, JsonConstants.MESSAGE_SETTLEMENT_APPROVAL_SEARCH_FAILURE);
			responseJSON.put(JsonConstants.DATA, "");

		}

		log.info("--- End Of SettlementApprovalService / Method execute --- ");
		return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();

	}

	private String generateXmlRequest(String cardNumber)
	{
		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder();
		builder.append("<VMX_ROOT>");
		builder.append("<VMX_HEADER>");
		builder.append("<MSGID>VMX.CMS.CRLIM.UPD</MSGID>");
		builder.append("<VERSION>M8V3</VERSION>");
		builder.append("<CLIENTID>6146</CLIENTID>");
		builder.append("<CORRELID>1234</CORRELID>");
		builder.append("<CONTEXT>KOTVMX</CONTEXT>");
		builder.append("<NAME>00000KOTVMX</NAME>");
		builder.append("</VMX_HEADER>");
		builder.append("<VMX_MSGIN>");
		builder.append("<ORG>406</ORG>");
		builder.append("<ACCT_NBR>");
		builder.append(cardNumber);
		builder.append("</ACCT_NBR>");
		builder.append("<UPDATE_FLAG>S</UPDATE_FLAG>");
		builder.append("<LOCAL_USE_FLAG></LOCAL_USE_FLAG>");
		builder.append("<ACCT_CRLIM></ACCT_CRLIM>");
		builder.append("<TEMP_ACCT_CRLIM></TEMP_ACCT_CRLIM>");
		builder.append("<ACCT_DTE_TEMP_CRLIM_EXP></ACCT_DTE_TEMP_CRLIM_EXP>");
		builder.append("<ACCT_BLK_CODE_1></ACCT_BLK_CODE_1>");
		builder.append("<ACCT_BLK_CODE_2>K</ACCT_BLK_CODE_2>");
		builder.append("</VMX_MSGIN>");
		builder.append("</VMX_ROOT>");

		return builder.toString();
	}

	private void sendSms(Settlement settlement, SystemUser systemUser)
	{

		log.info("Sending sms to Collector mobile number");

		callSMSDispatcher(settlement, systemUser.getMobileNumber(), systemUser);

	}

	private void callSMSDispatcher(Settlement settlement, String mobileNumber, SystemUser systemUser)
	{
		log.info("---- Inside callSMSDispatcher --------");
		try
		{
			String webserviceUrl = (String) applicationConfiguration.getValue("WEB_SERVICE_URL_SMS_DISPATCHER");

			Map<String, Object> smsDispatcherMap = ServerUtilities.generateSMSDispatcherMapForSettlement(settlement,
					mobileNumber, settlement.getStatus());

			StringBuilder xmlRequest = MapToXML.convertMapToXML(smsDispatcherMap, true, new HashMap<String, String>());

			log.info("----- xmlRequest : -------" + xmlRequest);

			CommunicationActivityAddition communicationActivityAddition = new CommunicationActivityAddition(systemUser
					.getUserTableId().toString(), systemUser.getImeiNo(), (settlement.getAppl() + "_" + "Settelment"),
					webserviceUrl, xmlRequest.toString(), communicationActivityService,
					ActivityLoggerConstants.DATABASE_MSSQL);

			new Thread(communicationActivityAddition).run();

			KotakCollectionWebserviceAdapter kotakCollectionWebserviceAdapter = new KotakCollectionWebserviceAdapter();

			String xmlResponse = kotakCollectionWebserviceAdapter.callWebserviceAndGetXmlString(xmlRequest.toString(),
					webserviceUrl);

			CommunicationActivity communicationActivity = communicationActivityAddition.extractCommunicationActivity();

			Map<String, Object> result = null;

			if (null != xmlResponse && !xmlResponse.equals(""))
			{
				communicationActivity.setResponse(xmlResponse);

				CommunicationActivityStatusUpdate communicationActivityStatusUpdate = new CommunicationActivityStatusUpdate(
						communicationActivity, (ActivityLoggerConstants.STATUS_SUCCESS), communicationActivityService);

				new Thread(communicationActivityStatusUpdate).run();

				result = XMLToMap.convertXMLToMap(xmlResponse);

				log.info("----- Result of SMS Dispatch : -------" + result);
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
		catch (ParseException e)
		{
			log.error("--Exception Detail --", e);
		}
	}

}