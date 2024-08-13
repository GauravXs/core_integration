package com.mobicule.mcollections.integration.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.me.JSONArray;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.SyncService;
import com.mobicule.mcollections.integration.commons.ServerUtilities;

public class SearchCaseOnline implements ISearchCaseOnline
{
	private Log log = LogFactory.getLog(getClass());
	
	@Autowired
	private SyncService syncService;

	@Autowired
	private ApplicationConfiguration<String, Object> applicationConfiguration;
	
	@Override
	public Message<String> execute(Message<String> message) throws Throwable {
		JSONObject responseJSON = new JSONObject();
		try {
			String requestSet = message.getPayload();

			String requestEntity = JSONPayloadExtractor.extract(requestSet,
					JsonConstants.ENTITY);
			String requestAction = JSONPayloadExtractor.extract(requestSet,
					JsonConstants.ACTION);
			String requestType = JSONPayloadExtractor.extract(requestSet,
					JsonConstants.TYPE);

			JSONObject requestData = JSONPayloadExtractor.extractJSON(
					requestSet, JsonConstants.DATA);

			JSONObject jsonObject = new JSONObject(requestSet);
			JSONObject user = (JSONObject) jsonObject
					.get(JsonConstants.SYSTEM_USER);
			SystemUser systemUserNew = ServerUtilities.extractSystemUser(user);

			log.info("-------requestSet is-----" + requestSet);
			log.info("-----requestEntity----" + requestEntity);
			log.info("-----requestAction----" + requestAction);
			log.info("-----requestType----" + requestType);
			log.info("---systemUserNew-----" + systemUserNew);
			log.info("-----requestData------" + requestData);
			
			String searchBy = Constants.EMPTY_STRING;
			
			
			if(requestData.has(JsonConstants.SearchCase.SEARCH_BY))
			{
				searchBy = requestData.getString(JsonConstants.SearchCase.SEARCH_BY) == null ? "" : requestData.getString(JsonConstants.SearchCase.SEARCH_BY);
			}
			
			int pageNumber = 0;
			if(requestData.has("pageNumber"))
			{
				pageNumber = Integer.parseInt(requestData.getString("pageNumber") == null ? "" : requestData.getString("pageNumber"));
			}
			log.info("----- pageNumber for online search cases ::: " + pageNumber);
			
			int pageSize = 100;
			int totalRecords = 0;
			int totalPages = 0;
			
			Map<String, String> parameters = new HashMap<String, String>();
			List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
			parameters.put("LoggedInUser",String.valueOf(systemUserNew.getUserTableId()));
			parameters.put("searchBy",searchBy);
			
			if(searchBy != null && !searchBy.equalsIgnoreCase(Constants.EMPTY_STRING)) {
				log.info("Inside searchBy value not Empty");
			
			dataList = syncService.fetchSearchCaseOnlineData(parameters,pageNumber,pageSize);  //added for online search cases pagination
			log.info("----- dataList for online search cases ::: " + dataList.size());
			
			totalRecords = syncService.getSearchCaseOnlineDataCount(parameters,pageNumber,pageSize);  //added for online search cases pagination
			log.info("----- totalRecords for online search cases ::: " + totalRecords);
			
			totalPages = Utilities.calculatePageCount(totalRecords, pageSize);
			
			}
			else {
				log.info("Inside searchBy value Empty");
				dataList = new ArrayList<Map<String, Object>>();
			}
			log.info("----- dataList for online search cases ::: " + dataList.size());
			/*int totalPages = totalRecords / pageSize;
			
			int reminder = totalPages % pageSize;
			
			if(reminder != 0) {
				totalPages = totalPages + 1;
			}*/
			
			if(dataList.isEmpty())
			{
				log.info("Inside No data found");
				   responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
				   responseJSON.put(JsonConstants.MESSAGE, "Case not found");
				   responseJSON.put(JsonConstants.DATA, new JSONArray());
			}
			else
			{
				log.info("Inside data found");
				
			@SuppressWarnings("unchecked")
			Map<String, String> headers = (Map<String, String>) applicationConfiguration.getValue("MAP_TO_JSON_CASES_HEADER");
			headers.put("ID","caseAssignedToId");
			headers.put("FIRST_NAME","caseAssignedToFirstName");
			headers.put("LAST_NAME","caseAssignedToLastName");
			headers.put("U","caseAssignedToUsername");
			
			log.info("----- after headers mapping ::: " + headers);
			
			String keyForGroupCases = (String) applicationConfiguration.getValue("keyForGroupCases");
			
			dataList = Utilities.eliminateNulls(dataList);

			JSONArray dataJSONArray = syncService.convertBeansToJSON(dataList, keyForGroupCases, headers, pageNumber,
					pageSize, totalRecords, totalPages); //added for online search cases
			log.info("----- after dataJSONArray mapping ::: " + dataJSONArray.length());

			   responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
			   responseJSON.put(JsonConstants.MESSAGE, "Case found");
			   responseJSON.put(JsonConstants.DATA, dataJSONArray);
			}

		} catch (Exception e) {
			log.error("Exception ::: " + e);
		}

		return MessageBuilder.withPayload(String.valueOf(responseJSON))
				.copyHeaders(message.getHeaders()).build();
	}

}
