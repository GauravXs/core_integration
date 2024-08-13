package com.mobicule.mcollections.integration.sync;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.me.JSONArray;
import org.json.me.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.mobicule.component.devicesync.commons.DeviceSyncConstants;
import com.mobicule.component.devicesync.intelligence.ISyncIntelligence;
import com.mobicule.component.mapconversion.json.MapToJSON;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.SyncService;

public class DepositionBankMappingSync implements ICasesSyncService{

	private Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	public ApplicationConfiguration<String, Object> applicationConfiguration;

	@Autowired
	private SyncService syncService;
	
	public ApplicationConfiguration<String, Object> getApplicationConfiguration() {
		return applicationConfiguration;
	}

	public void setApplicationConfiguration(
			ApplicationConfiguration<String, Object> applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
	}
	
	@Override
	public Message<String> execute(Message<String> message) throws Throwable {
		
		log.info(" -------- In DepositionBankMappingSync / execute() -------- ");
		
		JSONObject responseJSON = new JSONObject();

		try{
			String messagePayload = message.getPayload();
			
			log.info("-------- MessagePayload ----------" + messagePayload);

			if ((messagePayload.equals(Constants.EMPTY_STRING)) || (messagePayload == null)){
				
				log.info(" -------- Error: No Data found in Request -------- ");

				responseJSON.put((JsonConstants.Key.STATUS), (JsonConstants.Value.STATUS_FAILURE));
				responseJSON.put((JsonConstants.Key.MESSAGE), (JsonConstants.Value.MESSAGE_GENERIC_ERROR));
			}
			else{
				String type = JSONPayloadExtractor.extract(messagePayload, JsonConstants.Key.TYPE);
				String entity = JSONPayloadExtractor.extract(messagePayload, JsonConstants.Key.ENTITY);
				String action = JSONPayloadExtractor.extract(messagePayload, JsonConstants.Key.ACTION);
				JSONObject requestData = JSONPayloadExtractor.extractJSON(messagePayload, JsonConstants.DATA);

				log.info("type : " + type);
				log.info("entity : " + entity);
				log.info("action : " + action);
				
				Map<String, String> syncParametersMap = extractSyncParameters(requestData);
				
				if (type.equals(JsonConstants.Key.TYPE_SYNC)) {
					
					if (entity.equals(JsonConstants.ENTITY_DEPOSITION_BANK_MAPPING)) {
						
						JSONObject dataJSON = JSONPayloadExtractor.extractJSONObject(messagePayload, JsonConstants.Key.DATA);

						int pageSize = Integer.parseInt(dataJSON.getString(JsonConstants.Key.PAGE_SIZE));

						int pageNumber = Integer.parseInt(dataJSON.getString(JsonConstants.Key.PAGE_NUMBER));

						String lastSyncDate = dataJSON.getString(JsonConstants.Key.LAST_SYNC_DATE);
						
						SystemUser systemUser = new SystemUser();
						
						if (action.equals(JsonConstants.Value.ACTION_SYNC_INITIALIZATION)) {
							
							responseJSON.put(JsonConstants.DATA, extractSyncCounts(syncParametersMap,systemUser));
							
							log.info(" -------- Deposition Bank Mapping Intitialization Successfully -------- ");

							responseJSON.put(JsonConstants.MESSAGE, JsonConstants.SUCCESS);
						}
						else if (action.equals(JsonConstants.SYNC_ADD)){
							
							responseJSON = extractSyncData(syncParametersMap, JsonConstants.SYNC_ADD);

							log.info(" -------- Deposition Bank Mapping Added Successfully -------- ");

							responseJSON.put(JsonConstants.MESSAGE, JsonConstants.SYNC_SUCCESS_MESSAGE);
						}
						else if (action.equals(JsonConstants.SYNC_MODIFY)){
							
							responseJSON = extractSyncData(syncParametersMap, JsonConstants.SYNC_MODIFY);
							
							log.info(" -------- Deposition Bank Mapping Modified Successfully -------- ");

							responseJSON.put(JsonConstants.MESSAGE, JsonConstants.SYNC_SUCCESS_MESSAGE);
						}
						else if (action.equals(JsonConstants.SYNC_DELETE)){
							
							responseJSON = extractSyncData(syncParametersMap, JsonConstants.SYNC_DELETE);

							log.info(" -------- Deposition Bank Mapping Deleted Successfully -------- ");

							responseJSON.put(JsonConstants.MESSAGE, JsonConstants.SYNC_SUCCESS_MESSAGE);
						}
						else if (action.equals(JsonConstants.SYNC_COMPLETION))
							
							responseJSON.put(JsonConstants.DATA, new JSONObject().put(JsonConstants.LAST_SYNC_DATE, (String.valueOf(System.currentTimeMillis()))));
						
							log.info(" -------- Deposition Bank Mapping Intitialization completed Successfully -------- ");

							responseJSON.put(JsonConstants.MESSAGE, JsonConstants.SUCCESS);
						}

						responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
					}
				}
			
		}
		catch (Exception e){
			log.info(" -------- Error in DepositionBankMappingSync / execute -------- ");

			e.printStackTrace();
		}
		
		return (MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build());
	}
	
	private JSONObject extractSyncCounts(Map<String, String> syncParametersMap, SystemUser systemUser)
	{
		int syncCountModification = 0;
		int syncCountDeletion = 0;
		
		JSONObject dataJSON = new JSONObject();
		
		try
		{
			String lastSyncDate = syncParametersMap.get(JsonConstants.SYNC_LAST_SYNC_DATE);

			int syncCountAddition = syncService.extractDepositionBankMappingCount(lastSyncDate, JsonConstants.SYNC_ADD);
			log.info("syncContAddition " + syncCountAddition);

			if (!(lastSyncDate.equals("0")))
			{
				log.info("inside the SYNC_MODIFY ");
				syncCountModification = syncService.extractDepositionBankMappingCount(lastSyncDate, JsonConstants.SYNC_MODIFY);

				syncCountDeletion = syncService.extractDepositionBankMappingCount(lastSyncDate, JsonConstants.SYNC_DELETE);
			}

			dataJSON.put(
					JsonConstants.SYNC_ADD,
					(String.valueOf(Utilities.calculatePageCount(syncCountAddition,
							(Integer.parseInt(syncParametersMap.get(JsonConstants.SYNC_PAGE_SIZE)))))));
			dataJSON.put(
					JsonConstants.SYNC_MODIFY,
					(String.valueOf(Utilities.calculatePageCount(syncCountModification,
							(Integer.parseInt(syncParametersMap.get(JsonConstants.SYNC_PAGE_SIZE)))))));
			dataJSON.put(
					JsonConstants.SYNC_DELETE,
					(String.valueOf(Utilities.calculatePageCount(syncCountDeletion,
							(Integer.parseInt(syncParametersMap.get(JsonConstants.SYNC_PAGE_SIZE)))))));

			log.info(" ####### syncCountDeletion " + syncCountDeletion);
			log.info(" ####### syncCountModification " + syncCountModification);
			log.info(" ####### syncCountAddition " + syncCountAddition);

		}
		catch (Exception e)
		{
			log.error(" --------------- Error in StreetwalkSequenceSyncService / extractSyncCounts() --------------- ",
					e);
		}

		return dataJSON;
	}
	
	private Map<String, String> extractSyncParameters(JSONObject requestData)
	{
		Map<String, String> syncParametersMap = new HashMap<String, String>();

		try
		{
			syncParametersMap.put((JsonConstants.SYNC_LAST_SYNC_DATE),
					(requestData.getString(JsonConstants.SYNC_LAST_SYNC_DATE)));
			syncParametersMap.put((JsonConstants.SYNC_PAGE_NUMBER),
					(requestData.getString(JsonConstants.SYNC_PAGE_NUMBER)));
			syncParametersMap
					.put((JsonConstants.SYNC_PAGE_SIZE), (requestData.getString(JsonConstants.SYNC_PAGE_SIZE)));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return syncParametersMap;
	}
	
	private JSONObject extractSyncData(Map<String, String> syncParametersMap, String action){
		
		JSONObject responseJSON = new JSONObject();
		try
		{

			log.info(" -------- Page Number for " + action + ": "
					+ (Integer.parseInt(syncParametersMap.get(JsonConstants.SYNC_PAGE_NUMBER))) + " -------- ");

			Map<String, String> headers = (Map<String, String>) applicationConfiguration.getValue("DEPOSITION_BANK_MAPPING");
			
			log.info("headers::: " + headers);
			
			List<Map<String, Object>> mapCaseList = syncService.extractDepositionBankMappingData(
					(syncParametersMap.get(JsonConstants.SYNC_LAST_SYNC_DATE)),
					(Integer.parseInt(syncParametersMap.get(JsonConstants.SYNC_PAGE_NUMBER))),
					(Integer.parseInt(syncParametersMap.get(JsonConstants.SYNC_PAGE_SIZE))), action);

			mapCaseList = Utilities.eliminateNulls(mapCaseList);
			
			JSONArray dataJSONArray = new JSONArray();
			
			Iterator<Map<String, Object>> iterator = mapCaseList.iterator();
			
			while (iterator.hasNext()) {
				
				Map<String, Object> mapCase = iterator.next();
				
				if(mapCase.containsKey("ROW")) {
					mapCase.remove("ROW");
				}
				dataJSONArray.put(MapToJSON.convertMapToJSON(mapCase, headers));
			}

			log.info("-----extractDepositionBankMapping :: CaseList -------" + mapCaseList);
			log.info("extractDepositionBankMapping :: dataJSONArray :: " + dataJSONArray);

			responseJSON.put(JsonConstants.DATA, dataJSONArray);
		}
		catch (Exception e)
		{
			log.error("Error in extractSyncData() :: ",e);
		}

		return responseJSON;
	}
	
}
