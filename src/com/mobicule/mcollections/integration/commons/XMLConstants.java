/**
 ****************************************************************************** 
 * C O P Y R I G H T A N D C O N F I D E N T I A L I T Y N O T I C E
 * <p>
 * Copyright © 2012-2013 Mobicule Technologies Pvt. Ltd. All rights reserved.
 * This is proprietary information of Mobicule Technologies Pvt. Ltd.and is
 * subject to applicable licensing agreements. Unauthorized reproduction,
 * transmission or distribution of this file and its contents is a violation of
 * applicable laws.
 ****************************************************************************** 
 * 
 * @project mCollectionsKMIntegration
 */
package com.mobicule.mcollections.integration.commons;

/**
 * 
 * <enter description here>
 * 
 * @author shalini <nair>
 * @see
 * 
 * @createdOn 16-Jun-2014
 * @modifiedOn
 * 
 * @copyright © 2012-2013 Mobicule Technologies Pvt. Ltd. All rights reserved.
 */
/**
 * @author prashant
 *
 */
public interface XMLConstants
{
	String MC002 = "mc002";

	String MC005 = "mc005";

	String MC006 = "mc006";

	String MC0010 = "mc0010";

	String APP_CODE = "sourceappcode";

	String APP_CODE_VALUE = "MCOLL";

	String UID = "RqUID";

	String MESSAGE_TYPE = "message_type_cd";

	String MESSAGE_TYPE_MC002 = "MC002";

	String MESSAGE_TYPE_MC005 = "MC005";

	String MESSAGE_TYPE_MC006 = "MC006";
	
	String MESSAGE_TYPE_MC0010 = "MC0010";

	String MESSAGE_DATETIME = "message_datetime";

	String REQ_DETAILS = "request_details";

	//SPLN search request tags

	String APPL = "appl";

	String APACNUM = "apacnum";

	//CRN search request tags

	String CRN = "crn";

	String CARDNUM = "cardnum";

	//Search Response Tags

	String RESPONSE_HEADER = "response_header";

	String RESPONSE_STATUS = "response_status";

	String RESPONSE_DETAILS = "response_details";

	String ERROR_CODE = "errorcode";

	String ERROR_DESC = "errordesc";

	String SUCCESS_CODE = "0";

	String SUCCESS_RESPONSE = "SUCCESS";

	String PARTY_ID = "partyid";

	String PARTY_NAME = "partyname";

	String EMI_DUEDATE = "EMIduedate";

	String PENAL_AMNT = "Penalamount";

	String TOTAL_OUTSTANDING_AMNT = "TotaloutstandingAmount";

	String EMI_DUE_AMNT = "EMIdueAmount";

	String MOBILE = "mobilenum";

	String EMAIL = "email";

	String PHONE_NUM = "phonenum";

	String ADDRESS = "address";

	String LOCATION = "location";

	String PINCODE = "pincode";

	String OVERDUE_AMNT = "Overdueamount";

	String APAC_DATE = "Apacdate";

	String TENURE = "Tenure";

	String EMI_PAID = "EMIPAID";

	String BAL_TENURE = "BALTENURE";

	String PRINCIPAL_OUTSTANDING = "PrincipleOutstanding";

	String FC_CHARGES = "FCCHARGES";

	String BROKEN_REVENUE = "BROKENREVENUE";

	String LEGAL_CHARGES = "LEGALCAGRGES";

	String BUCKET = "BUCKET";

	String FIRST_OSTD_DATE = "FIRSTOSTDDATE";

	String CLOSURE_BAL = "CLOSUREBAL";

	String LOAN_AMOUNT = "AGRVALUE";

	//xml constant for sms dispatcher
	String SMS_DISPATCHER_SOURCEAPPCODE_KEY = "sourceappcode";

	String SMS_DISPATCHER_REQID_KEY = "ReqID";

	String SMS_DISPATCHER_REQTYPE_KEY = "ReqType";

	String SMS_DISPATCHER_MCOLL_VALUE = "MCOLL";

	String SMS_DISPATCHER_COLLECTION_VALUE = "COLLECTION";

	String SMS_DISPATCHER_SMSTXT_KEY = "SMSTxt";

	String SMS_DISPATCHER_SMSTO_KEY = "SMSTo";

	String SMS_DISPATCHER_HEADER_KEY = "Header";

	String SMS_DISPATCHER_BODY_KEY = "Body";

	String SMS_DISPATCHER_POS_KEY = "POS";

	String REGISTRATION_NUMBER = "regno";

	String BUYER_ID = "byrpartyid";

	String BUYER_NAME = "byrpartyname";
	
	/*xml constant for sms dispatcher of Prime*/

	String SMS_DISPATCHER_SMSAPIREQ_KEY = "SMSAPIReq xmlns=\"http://www.kotak.com/schemas/SMSAPIReq.xsd\"";

	String SMS_DISPATCHER_SOURCEAPPID_KEY = "sourceAppID";

	String SMS_DISPATCHER_SMSVENDOR_KEY = "smsVendor";

	String SMS_DISPATCHER_UNIQUEREFNO_KEY = "uniqueRefNo";

	String SMS_DISPATCHER_PRIORITY_KEY = "priority";

	String SMS_DISPATCHER_FROM_KEY = "from";

	String SMS_DISPATCHER_TONUMBER_KEY = "toNumber";

	String SMS_DISPATCHER_MESSAGE_KEY = "message";

	String SMS_DISPATCHER_INTNO_KEY = "intNo";	
	
	String SMS_DISPATCHER_SMSVENDOR_VALUE="gs";
	
	String SMS_DISPATCHER_PRIORITYANDINTNO_VALUE="1";
	
	String SMS_DISPATCHER_FROM_VALUE="KOTAKB";

	/*KGI Rollver cases*/

	String ASSET_ID = "assetid";

	String POLICY_NUMBER = "policyno";

	String POLICY_DATE = "policydt";

	String POLICY_EXPIARY_DATE = "policyexpdt";

	String INSURED_VALUE = "Insuredvalue";

	String PREMIUM_AMOUNT = "PremiumAmt";

	String BENIFICIARY = "Benificiary";

	String INSURER = "Insurer";

	String INSURER_PARTY_ID = "InsurerPartyid";

	String CHASSIS = "chassis";

	String ENGNO = "engnno";

	String MAKE = "make";

	String MODEL = "model";

	String INVOICE = "invoice";

	String INVOICE_DATE = "invoicedt";

	String INVOICE_VAL = "invoiceval";

	String SALES_TAX_AMOUNT = "salestaxamt";

	String SUPPLIER = "supplier";

	String FIRST_DATE = "frstate";

	String MODEL2 = "model2";

	String ASTEST_VALUE = "ASTESTVALUE";

	String ASTEST_DATE = "ASTESTDT";

	String ASTEST_DESC = "ASTESTDESC";

	interface URLConstants
	{
		String RANDOM_COLLECTIONS_SEARCH_URL = "https://10.10.1.40/MCORE/"; //LIVE
		/*String RANDOM_COLLECTIONS_SEARCH_URL = "https://10.10.19.28/MCORE/";*///UAT
	}

}
