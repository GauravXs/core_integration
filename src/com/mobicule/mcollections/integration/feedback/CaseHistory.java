package com.mobicule.mcollections.integration.feedback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.me.JSONArray;
import org.json.me.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.CaseHistoryService;
import com.mobicule.mcollections.integration.commons.ServerUtilities;

public class CaseHistory implements ICaseHistory {

	private Logger log = LoggerFactory.getLogger(CaseHistory.class);

	private CaseHistoryService caseHistoryService;

	private ApplicationConfiguration<String, String> applicationConfiguration;

	public CaseHistoryService getCaseHistoryService() {
		return caseHistoryService;
	}

	public void setCaseHistoryService(CaseHistoryService caseHistoryService) {
		this.caseHistoryService = caseHistoryService;
	}

	public ApplicationConfiguration<String, String> getApplicationConfiguration() {
		return applicationConfiguration;
	}

	public void setApplicationConfiguration(ApplicationConfiguration<String, String> applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
	}

	@Override
	public Message<String> execute(Message<String> message) throws Throwable {

		log.info(":::: Inside CaseHistory::::");
		JSONObject responseJSON = new JSONObject();
		String apac = Constants.EMPTY_STRING;
		String appl = Constants.EMPTY_STRING;

		String startDateRange = Constants.EMPTY_STRING;
		String endDateRange = Constants.EMPTY_STRING;
		int pageSize = 0;
		int pageNumber = 0;
		List<Map<String, Object>> rows;
		try {
			String requestSet = message.getPayload();

			String requestEntity = JSONPayloadExtractor.extract(requestSet, JsonConstants.ENTITY);
			String requestAction = JSONPayloadExtractor.extract(requestSet, JsonConstants.ACTION);

			JSONObject requestSystemUser = JSONPayloadExtractor.extractJSON(requestSet, JsonConstants.SYSTEM_USER);

			JSONObject requestData = JSONPayloadExtractor.extractJSON(requestSet, JsonConstants.DATA);

			SystemUser systemUser = ServerUtilities.extractSystemUser(requestSystemUser);
			List<Map<String, Object>> getHistoryDetails = new ArrayList<>();
			Map<String, Object> getDashBoardDetails = new HashMap<>();
			Map<String, Object> parameters = new HashMap<>();

			String historyEarlierRange = applicationConfiguration.getValue("historyEarlierRange");

			if (requestData.has("accountNumber")) {
				apac = requestData.get("accountNumber") == null ? Constants.EMPTY_STRING
						: requestData.get("accountNumber").toString();
				parameters.put("apac", apac);
			}

			if (requestData.has("appl")) {
				appl = requestData.get("appl") == null ? Constants.EMPTY_STRING : requestData.get("appl").toString();
				parameters.put("appl", appl);
			}

			if (requestData.has("startDateRange")) {
				startDateRange = requestData.get("startDateRange") == null ? Constants.EMPTY_STRING
						: requestData.get("startDateRange").toString();
				parameters.put("startDateRange", startDateRange);
			}

			if (requestData.has("endDateRange")) {
				endDateRange = requestData.get("endDateRange") == null ? Constants.EMPTY_STRING
						: requestData.get("endDateRange").toString();
				if (endDateRange.equalsIgnoreCase("E")) {
					parameters.put("endDateRange", historyEarlierRange);
				} else {
					parameters.put("endDateRange", endDateRange);
				}

			}

			if (requestData.has("pageSize")) {
				pageSize = Integer.valueOf(requestData.get("pageSize").toString());
				parameters.put("pageSize", pageSize);
			}

			if (requestData.has("pageNumber")) {
				pageNumber = Integer.valueOf(requestData.get("pageNumber").toString());
				parameters.put("pageNumber", pageNumber);
			}
			
				
			parameters.put("userId", systemUser.getUserTableId());
			parameters.put("username", systemUser.getUsername());
			
			log.info("parameters ::: " + parameters);

			log.info("---- Apac -----" + apac);
			log.info("---- Appl -----" + appl);
			log.info("---- requestSet -----" + requestSet);
			log.info("---- requestAction -----" + requestAction);
			JSONObject dataJSON = new JSONObject();

			if (requestAction.equalsIgnoreCase("collections")) {
				
				int caseCount = caseHistoryService.getCollectionHistoryCount(parameters);
				log.info("getCollectionHistory caseCount::: " + caseCount);

				dataJSON.put("pageNumber", pageNumber);
				dataJSON.put("pageSize", pageSize);
				dataJSON.put("totalRecords", caseCount);
				dataJSON.put("totalPages", (String.valueOf(Utilities.calculatePageCount(caseCount, pageSize))));

				if (caseCount > 0) {
					getHistoryDetails = caseHistoryService.getCollectionHistory(parameters);
					log.info("getCollectionHistory::: " + getHistoryDetails);
					
					getHistoryDetails = getCollectionListKey(getHistoryDetails);
					log.info("getCollectionHistory::: " + getHistoryDetails);
					
					JSONArray getValue = listmap_to_jsonArray(getHistoryDetails);
					dataJSON.put("details", getValue);
				} else {
					dataJSON.put("details", new JSONArray());
				}

				if (parameters.get("endDateRange").equals("0") && caseCount > 0) {
					getDashBoardDetails = caseHistoryService.getDashboardDetails(parameters);
					log.info("getCollectionHistory getDashBoardDetails::: " + getDashBoardDetails);
					
					getHistoryDetails = getCollectionListKey(getHistoryDetails);
					log.info("getCollectionListKey::: " + getHistoryDetails);

					dataJSON.put("totalCollectedAmt", getDashBoardDetails.get("totalCollectedAmt"));
					dataJSON.put("fullOverdue", getDashBoardDetails.get("fullOverdue"));
					dataJSON.put("fullOverdueCount", getDashBoardDetails.get("fullOverdueCount"));
					dataJSON.put("partialOverdue", getDashBoardDetails.get("partialOverdue"));
					dataJSON.put("partialOverdueCount", getDashBoardDetails.get("partialOverdueCount"));
					dataJSON.put("notCollectedCount", getDashBoardDetails.get("notCollectedCount"));
				}
			} else if (requestAction.equalsIgnoreCase("activity")) {

				int caseCount = caseHistoryService.getActivityHistoryCount(parameters);
				log.info("getActivityHistoryCount caseCount::: " + caseCount);

				dataJSON.put("pageNumber", pageNumber);
				dataJSON.put("pageSize", pageSize);
				dataJSON.put("totalRecords", caseCount);
				dataJSON.put("totalPages", (String.valueOf(Utilities.calculatePageCount(caseCount, pageSize))));

				if (caseCount > 0) {
					getHistoryDetails = caseHistoryService.getActivityHistory(parameters);
					log.info("getActivityHistory::: " + getHistoryDetails);
					getHistoryDetails = getActivityListKey(getHistoryDetails);
					log.info("getCollectionListKey::: " + getHistoryDetails);
					JSONArray getValue = listmap_to_jsonArray(getHistoryDetails);
					dataJSON.put("details", getValue);
				} else {
					dataJSON.put("details", new JSONArray());
				}

			} else if (requestAction.equalsIgnoreCase("feedback")) {

				getHistoryDetails = caseHistoryService.getfeedbackHistory(parameters);
				log.info("getfeedbackHistory::: " + getHistoryDetails);
				
				getHistoryDetails = getFeedbackListKey(getHistoryDetails);
				log.info("getFeedbackListKey::: " + getHistoryDetails);

				dataJSON.put("pageNumber", pageNumber);
				dataJSON.put("pageSize", pageSize);
				dataJSON.put("totalRecords", 0);
				dataJSON.put("totalPages", 0);

				JSONArray getValue = listmap_to_jsonArray(getHistoryDetails);
				dataJSON.put("details", getValue);

			}

			responseJSON.put(JsonConstants.DATA, dataJSON);
			responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
			responseJSON.put(JsonConstants.MESSAGE, JsonConstants.SUCCESS);

		} catch (Exception e) {
			log.info("Exception ::: " + e);
			e.printStackTrace();
			responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
			responseJSON.put(JsonConstants.MESSAGE, JsonConstants.FAILURE);

		}

		return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
	}

	public JSONArray listmap_to_jsonArray(List<Map<String, Object>> list) {
		JSONArray json_arr = new JSONArray();
		for (Map<String, Object> map : list) {
			JSONObject json_obj = new JSONObject();
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue() == null ? Constants.EMPTY_STRING : entry.getValue();
				try {
					json_obj.put(key, value);
				} catch (Exception e) {
					log.info("Exception ::: " + e);
				}
			}
			json_arr.put(json_obj);
		}
		return json_arr;
	}
	
	public List<Map<String, Object>> getCollectionListKey(List<Map<String, Object>> oldList) {
		log.info("getCollectionList :::");
		List<Map<String, Object>> newList = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> map : oldList) {
		    Map<String, Object> newMap = new HashMap<String, Object>();
		    for (Map.Entry<String, Object> entry : map.entrySet()) {
		        String key = entry.getKey();
		        Object value = entry.getValue();
		        if (key.equals("submissiondate")) {
		            key = "submissionDate";
		        } 
		        if (key.equals("resultcode")) {
		            key = "resultCode";
		        } 
		        if (key.equals("customername")) {
		            key = "customerName";
		        } 
		        if (key.equals("actioncode")) {
		            key = "actionCode";
		        } 
		        if (key.equals("amount")) {
		            key = "amount";
		        } 
		        if (key.equals("remark")) {
		            key = "remark";
		        } 
		        if (key.equals("apaccardnumber")) {
		            key = "apacCardNumber";
		        } 
		        if (key.equals("partyid")) {
		            key = "partyId";
		        } 
		        if (key.equals("receiptnumber")) {
		            key = "receiptNumber";
		        } 
		        if (key.equals("paymentmode")) {
		            key = "paymentMode";
		        } 
		        if (key.equals("onlinepaymentstatus")) {
		            key = "onlinePaymentStatus";
		        } 
		        newMap.put(key, value);
		    }
		    newList.add(newMap);
		}
		return newList;
	}
	
	public List<Map<String, Object>> getActivityListKey(List<Map<String, Object>> oldList) {
		log.info("getActivityListKey :::");
		List<Map<String, Object>> newList = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> map : oldList) {
		    Map<String, Object> newMap = new HashMap<String, Object>();
		    for (Map.Entry<String, Object> entry : map.entrySet()) {
		        String key = entry.getKey();
		        Object value = entry.getValue();
		        if (key.equals("outstandingbalance")) {
		            key = "outstandingBalance";
		        } 
		        if (key.equals("customername")) {
		            key = "customerName";
		        } 
		        if (key.equals("submissiondate")) {
		            key = "submissionDate";
		        } 
		        if (key.equals("appl")) {
		            key = "appl";
		        } 
		        if (key.equals("apaccardnumber")) {
		            key = "apacCardNumber";
		        } 
		        if (key.equals("alternatemobilenumber")) {
		            key = "alternateMobileNumber";
		        } 
		        if (key.equals("amount")) {
		            key = "amount";
		        } 
		        if (key.equals("amount")) {
		            key = "amount";
		        }
		        if (key.equals("remark")) {
		            key = "remark";
		        }
		        if (key.equals("emiamount")) {
		            key = "emiAmount";
		        }
		        if (key.equals("actioncode")) {
		            key = "actionCode";
		        }
		        if (key.equals("partymobilenumber")) {
		            key = "partyMobileNumber";
		        }
		        if (key.equals("resultcode")) {
		            key = "resultCode";
		        }
		        if (key.equals("paymentmode")) {
		            key = "paymentMode";
		        }
		        if (key.equals("partyid")) {
		            key = "partyId";
		        } 
		        if (key.equals("receiptnumber")) {
		            key = "receiptNumber";
		        } 
		        if (key.equals("onlinepaymentstatus")) {
		            key = "onlinePaymentStatus";
		        } 
		        newMap.put(key, value);
		    }
		    newList.add(newMap);
		}
		return newList;
	}
	
	public List<Map<String, Object>> getFeedbackListKey(List<Map<String, Object>> oldList) {
		log.info("getFeedbackListKey :::");
		List<Map<String, Object>> newList = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> map : oldList) {
		    Map<String, Object> newMap = new HashMap<String, Object>();
		    for (Map.Entry<String, Object> entry : map.entrySet()) {
		        String key = entry.getKey();
		        Object value = entry.getValue();
		        if (key.equals("outstandingbalance")) {
		            key = "outstandingBalance";
		        } 
		        if (key.equals("customername")) {
		            key = "customerName";
		        } 
		        if (key.equals("submissiondate")) {
		            key = "submissionDate";
		        } 
		        if (key.equals("appl")) {
		            key = "appl";
		        } 
		        if (key.equals("apaccardnumber")) {
		            key = "apacCardNumber";
		        } 
		        if (key.equals("alternatemobilenumber")) {
		            key = "alternateMobileNumber";
		        } 
		        if (key.equals("amount")) {
		            key = "amount";
		        } 
		        if (key.equals("remark")) {
		            key = "remark";
		        } 
		        if (key.equals("emiamount")) {
		            key = "emiAmount";
		        } 
		        if (key.equals("actioncode")) {
		            key = "actionCode";
		        } 
		        if (key.equals("partymobilenumber")) {
		            key = "partyMobileNumber";
		        } 
		        if (key.equals("resultcode")) {
		            key = "resultCode";
		        } 
		        if (key.equals("partyid")) {
		            key = "partyId";
		        } 
		        if (key.equals("receiptnumber")) {
		            key = "receiptNumber";
		        } 
		        if (key.equals("paymentmode")) {
		            key = "paymentMode";
		        } 
		        if (key.equals("onlinepaymentstatus")) {
		            key = "onlinePaymentStatus";
		        } 
		        newMap.put(key, value);
		    }
		    newList.add(newMap);
		}
		return newList;
	}
}
