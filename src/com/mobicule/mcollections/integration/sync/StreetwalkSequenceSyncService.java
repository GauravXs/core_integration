package com.mobicule.mcollections.integration.sync;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.mobicule.mcollections.core.service.SystemUserService;
import com.mobicule.mcollections.core.service.UserScratchSyncService;
import com.mobicule.mcollections.integration.commons.ServerUtilities;
import com.mobicule.mcollections.integration.thread.ScratchSyncThread;

public class StreetwalkSequenceSyncService implements IStreetwalkSequenceSyncService
{
	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private SystemUserService systemUserService;

	@Autowired
	private SyncService syncService;

	@Autowired
	private ApplicationConfiguration<String, Object> applicationConfiguration;
	
	@Autowired
	private UserScratchSyncService userScratchSyncService;
	

	@Override
	public Message<String> execute(Message<String> message) throws Throwable
	{
		JSONObject responseJSON = new JSONObject();
		String requestSet = "";
		try
		{
			log.info("----inside streetwalksequesnceservice------");
			requestSet = message.getPayload();

			String requestEntity = JSONPayloadExtractor.extract(requestSet, JsonConstants.ENTITY);
			String requestAction = JSONPayloadExtractor.extract(requestSet, JsonConstants.ACTION);

			log.info("---requestEntity---" + requestEntity);
			log.info("---requestAction---" + requestAction);

			JSONObject requestSystemUser = JSONPayloadExtractor.extractJSON(requestSet, JsonConstants.SYSTEM_USER);
			/*
			 * JSONObject requestStreetWalkSequence =
			 * JSONPayloadExtractor.extractJSON(requestSet,
			 * JsonConstants.STREETWALK_SEQUENCE);
			 */
			JSONObject requestData = JSONPayloadExtractor.extractJSON(requestSet, JsonConstants.DATA);

			log.info("---requestData----" + requestData);

			SystemUser systemUser = ServerUtilities.extractSystemUser(requestSystemUser);

			/*
			 * Map<String, String> streetwalkSequenceParametersMapPresent = ServerUtilities
			 * .extractStreetwalkSequenceParameters(requestStreetWalkSequence);
			 * 
			 * Map<String, String> syncParametersMap = extractSyncParameters(requestData);
			 */

			log.info("----system user----" + systemUser.toString());

			Map<String, String> syncParametersMap = extractSyncParameters(requestData);
			
			//Adding user id to request data to fetch user data
			syncParametersMap.put("userId", systemUser.getUserTableId().toString());
			
			String lastSyncDate = syncParametersMap.get(JsonConstants.SYNC_LAST_SYNC_DATE);
			log.info("lastSyncDate ::: " + lastSyncDate);
			if ((requestEntity.equals(JsonConstants.ENTITY_SYNC)) && (requestAction.equals(JsonConstants.SYNC_INITIALIZATION))){
				
				if(lastSyncDate!=null && !lastSyncDate.equalsIgnoreCase("") && (lastSyncDate.equalsIgnoreCase("0") || lastSyncDate.equalsIgnoreCase("0.0")) )
				{
					log.info("::: Inside lastSyncDate Condition::: ");
				ScratchSyncThread scratchSyncThreadClass = new ScratchSyncThread(systemUser,userScratchSyncService);
				Thread scratchThread = new Thread(scratchSyncThreadClass);
				scratchThread.setName("ScratchSyncThread : " + systemUser.getUsername());
				scratchThread.start();
				}
				responseJSON = getInitCount(syncParametersMap);

				log.info(" -------- Streetwalk Sequence Intitialization Data extracted Successfully -------- ");

				responseJSON.put(JsonConstants.MESSAGE, JsonConstants.SUCCESS);
			}
			else if ((requestEntity.equals(JsonConstants.ENTITY_SYNC)) && (requestAction.equals(JsonConstants.SYNC_ADD))) {
				
				responseJSON = getCasesData(syncParametersMap, JsonConstants.SYNC_ADD);

				log.info(" -------- Streetwalk Sequence Added extracted Successfully -------- ");

				responseJSON.put(JsonConstants.MESSAGE, JsonConstants.SYNC_SUCCESS_MESSAGE);
			} 
			else if ((requestEntity.equals(JsonConstants.ENTITY_SYNC)) && (requestAction.equals(JsonConstants.SYNC_MODIFY))) {
				
				responseJSON = getCasesData(syncParametersMap, JsonConstants.SYNC_MODIFY);

				log.info(" -------- Streetwalk Sequence Modified extracted Successfully -------- ");

				responseJSON.put(JsonConstants.MESSAGE, JsonConstants.SYNC_SUCCESS_MESSAGE);
			} 
			else if ((requestEntity.equals(JsonConstants.ENTITY_SYNC)) && (requestAction.equals(JsonConstants.SYNC_DELETE))) {
				
				responseJSON = getCasesData(syncParametersMap, JsonConstants.SYNC_DELETE);

				log.info(" -------- Streetwalk Sequence Deleted extracted Successfully -------- ");

				responseJSON.put(JsonConstants.MESSAGE, JsonConstants.SYNC_SUCCESS_MESSAGE);
			} 
			else if ((requestEntity.equals(JsonConstants.ENTITY_SYNC)) && (requestAction.equals(JsonConstants.SYNC_COMPLETION))) {
				
				//responseJSON = extractStreetwalkSequenceVersion(streetwalkSequenceParametersMapFuture);
				
				JSONObject dataJSON = new JSONObject();
				
				dataJSON.put(JsonConstants.LAST_SYNC_DATE, (System.currentTimeMillis()));

				responseJSON.put(JsonConstants.DATA, dataJSON);

				log.info(" -------- Streetwalk Sequence Version Details extracted Successfully -------- ");
			  
				responseJSON.put(JsonConstants.MESSAGE, JsonConstants.SUCCESS); 
			}			 

			responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
		
		}
		catch (Exception e)
		{
			log.error(" -------- Error in StreetwalkSequenceSyncService -------- ", e);

			responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
			responseJSON.put(JsonConstants.MESSAGE, JsonConstants.SYNC_FAILURE_MESSAGE);
		}

		log.info(" ##### requestSet ##### " + requestSet);
		//log.info(" #### responseJSON ####" + responseJSON.toString());

		return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
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
		catch (JSONException e)
		{
			e.printStackTrace();
		}

		return syncParametersMap;
	}

	private String streetSequenceComparision(Map<String, String> streetwalkSequenceParametersMapPresent,
			Map<String, String> streetwalkSequenceParametersMapFuture)
	{
		if (!((streetwalkSequenceParametersMapPresent.get(JsonConstants.STREETWALK_SEQUENCE_NAME))
				.equals(streetwalkSequenceParametersMapFuture.get(JsonConstants.STREETWALK_SEQUENCE_NAME))))
		{
			return (JsonConstants.STREETWALK_SEQUENCE_NAME);
		}
		else if (!((streetwalkSequenceParametersMapPresent.get(JsonConstants.STREETWALK_SEQUENCE_VERSION))
				.equals(streetwalkSequenceParametersMapFuture.get(JsonConstants.STREETWALK_SEQUENCE_VERSION))))
		{
			return (JsonConstants.STREETWALK_SEQUENCE_VERSION);
		}

		return (Constants.EMPTY_STRING);
	}

	private JSONObject extractStreetwalkSequenceInitialization(Map<String, String> syncParametersMap,
			String streetSequenceChange, Map<String, String> streetwalkSequenceParametersMapFuture)
	{
		JSONObject responseJSON = new JSONObject();

		JSONObject dataJSON = new JSONObject();

		try
		{

			dataJSON = extractSyncCounts(syncParametersMap, dataJSON, streetSequenceChange,
					(streetwalkSequenceParametersMapFuture.get(JsonConstants.STREETWALK_SEQUENCE_NAME)));

			JSONObject streetwalkSequenceJSON = new JSONObject();

			streetwalkSequenceJSON.put(JsonConstants.STREETWALK_SEQUENCE_NAME,
					(streetwalkSequenceParametersMapFuture.get(JsonConstants.STREETWALK_SEQUENCE_NAME)));
			streetwalkSequenceJSON.put(JsonConstants.STREETWALK_SEQUENCE_VERSION,
					(streetwalkSequenceParametersMapFuture.get(JsonConstants.STREETWALK_SEQUENCE_VERSION)));

			dataJSON.put((JsonConstants.STREETWALK_SEQUENCE), streetwalkSequenceJSON);

			responseJSON.put(JsonConstants.DATA, dataJSON);
		}
		catch (JSONException e)
		{
			log.error(
					" --------------- Error in StreetwalkSequenceSyncService / extractStreetwalkSequenceInitialization() --------------- ",
					e);
		}

		return responseJSON;
	}

	private JSONObject extractSyncCounts(Map<String, String> syncParametersMap, JSONObject dataJSON,
			String streetSequenceChange, String streetwalkSequenceName)
	{
		int syncCountModification = 0;
		int syncCountDeletion = 0;

		try
		{
			String lastSyncDate = syncParametersMap.get(JsonConstants.SYNC_LAST_SYNC_DATE);

			int syncCountAddition = syncService.extractCACount(lastSyncDate, JsonConstants.SYNC_ADD,
					streetSequenceChange, streetwalkSequenceName);
			log.info("syncContAddition " + syncCountAddition);

			log.info("######## streetSequenceChange " + streetSequenceChange);
			log.info(" ######## streetwalkSequenceName " + streetwalkSequenceName);
			log.info(" ######## lastSyncDate " + lastSyncDate);
			

			if (!(lastSyncDate.equalsIgnoreCase("0")))
			{
				log.info("inside the SYNC_MODIFY ");
				syncCountModification = syncService.extractCACount(lastSyncDate, JsonConstants.SYNC_MODIFY,
						streetSequenceChange, streetwalkSequenceName);

				syncCountDeletion = syncService.extractCACount(lastSyncDate, JsonConstants.SYNC_DELETE,
						streetSequenceChange, streetwalkSequenceName);
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

	private JSONObject extractStreetwalkSequenceVersion(Map<String, String> streetwalkSequenceParametersMap)
	{
		JSONObject responseJSON = new JSONObject();

		try
		{
			JSONObject dataJSON = new JSONObject();
			JSONObject streetWalkSequenceJSON = new JSONObject();

			streetWalkSequenceJSON.put(JsonConstants.STREETWALK_SEQUENCE_NAME,
					(streetwalkSequenceParametersMap.get(JsonConstants.STREETWALK_SEQUENCE_NAME)));
			streetWalkSequenceJSON.put(JsonConstants.STREETWALK_SEQUENCE_VERSION,
					(streetwalkSequenceParametersMap.get(JsonConstants.STREETWALK_SEQUENCE_VERSION)));
			streetWalkSequenceJSON.put(JsonConstants.STREETWALK_SEQUENCE_START_DATE,
					(streetwalkSequenceParametersMap.get(JsonConstants.STREETWALK_SEQUENCE_START_DATE)));
			streetWalkSequenceJSON.put(JsonConstants.STREETWALK_SEQUENCE_END_DATE,
					(streetwalkSequenceParametersMap.get(JsonConstants.STREETWALK_SEQUENCE_END_DATE)));
			streetWalkSequenceJSON.put(JsonConstants.STREETWALK_SEQUENCE_NOTIFICATION_DELAY,
					(applicationConfiguration.getValue(Constants.STREETWALK_SEQUENCE_NOTIFICATION_DELAY).toString()));

			dataJSON.put(JsonConstants.STREETWALK_SEQUENCE, streetWalkSequenceJSON);

			dataJSON.put(JsonConstants.LAST_SYNC_DATE, (System.currentTimeMillis()));

			responseJSON.put(JsonConstants.DATA, dataJSON);
		}
		catch (Exception e)
		{
			log.error(
					" --------------- Error in StreetwalkSequenceSyncService / extractStreetwalkSequenceVersion() --------------- ",
					e);
		}

		return responseJSON;
	}

	private JSONObject extractStreetwalkSequence(Map<String, String> syncParametersMap, String action,
			String streetSequenceChange, String streetwalkSequenceName)
	{
		JSONObject responseJSON = new JSONObject();
		try
		{

			log.info(" -------- Page Number for " + action + ": "
					+ (Integer.parseInt(syncParametersMap.get(JsonConstants.SYNC_PAGE_NUMBER))) + " -------- ");

			Map<String, String> headers = (Map<String, String>) applicationConfiguration
					.getValue("MAP_TO_JSON_CASES_HEADER");
			String keyForGroupCases = (String) applicationConfiguration.getValue("keyForGroupCases");

			List<Map<String, Object>> mapCaseList = syncService.extractStreetwalkSequence(
					(syncParametersMap.get(JsonConstants.SYNC_LAST_SYNC_DATE)),
					(Integer.parseInt(syncParametersMap.get(JsonConstants.SYNC_PAGE_NUMBER))),
					(Integer.parseInt(syncParametersMap.get(JsonConstants.SYNC_PAGE_SIZE))), action,
					streetSequenceChange, streetwalkSequenceName);

			mapCaseList = Utilities.eliminateNulls(mapCaseList);

			JSONArray dataJSONArray = syncService.convertBeansToJSON(mapCaseList, keyForGroupCases, headers,0,0,0,0);

			log.info("-----extractStreetwalkSequence CaseList -------" + mapCaseList);

			responseJSON.put(JsonConstants.DATA, dataJSONArray);
		}
		catch (Exception e)
		{
			log.error(
					" --------------- Error in StreetwalkSequenceSyncService / extractStreetwalkSequence() --------------- ",
					e);
		}

		return responseJSON;
	}
	
	private JSONObject getInitCount(Map<String, String> syncParametersMap)
	{
		JSONObject responseJSON = new JSONObject();

		JSONObject dataJSON = new JSONObject();

		try
		{
			dataJSON = getCounts(syncParametersMap, dataJSON);

			responseJSON.put(JsonConstants.DATA, dataJSON);
		}
		catch (JSONException e)
		{
			log.error(" --------------- Error in StreetwalkSequenceSyncService / getInitCount() --------------- ",e);
		}

		return responseJSON;
	}
	
	private JSONObject getCounts(Map<String, String> syncParametersMap, JSONObject dataJSON)
	{
		int syncCountModification = 0;
		int syncCountDeletion = 0;

		try
		{
			String lastSyncDate = syncParametersMap.get(JsonConstants.SYNC_LAST_SYNC_DATE);
			
			int syncCountAddition = syncService.getCasesCount(lastSyncDate, JsonConstants.SYNC_ADD, syncParametersMap);
			
			log.info("syncContAddition " + syncCountAddition);

			log.info(" ######## lastSyncDate " + lastSyncDate);
			

			if (!(lastSyncDate.equalsIgnoreCase("0")))
			{
				log.info("inside the SYNC_MODIFY ");
				syncCountModification = syncService.getCasesCount(lastSyncDate, JsonConstants.SYNC_MODIFY, syncParametersMap);

				syncCountDeletion = syncService.getCasesCount(lastSyncDate, JsonConstants.SYNC_DELETE, syncParametersMap);
			}

			dataJSON.put(JsonConstants.SYNC_ADD,
					(String.valueOf(Utilities.calculatePageCount(syncCountAddition,
							(Integer.parseInt(syncParametersMap.get(JsonConstants.SYNC_PAGE_SIZE)))))));
			dataJSON.put(JsonConstants.SYNC_MODIFY,
					(String.valueOf(Utilities.calculatePageCount(syncCountModification,
							(Integer.parseInt(syncParametersMap.get(JsonConstants.SYNC_PAGE_SIZE)))))));
			dataJSON.put(JsonConstants.SYNC_DELETE,
					(String.valueOf(Utilities.calculatePageCount(syncCountDeletion,
							(Integer.parseInt(syncParametersMap.get(JsonConstants.SYNC_PAGE_SIZE)))))));

			log.info(" ####### syncCountDeletion " + syncCountDeletion);
			log.info(" ####### syncCountModification " + syncCountModification);
			log.info(" ####### syncCountAddition " + syncCountAddition);

		}
		catch (Exception e)
		{
			log.error(" --------------- Error in StreetwalkSequenceSyncService / extractSyncCounts() --------------- ", e);
		}

		return dataJSON;
	}
	
	private JSONObject getCasesData(Map<String, String> syncParametersMap, String action)
	{
		JSONObject responseJSON = new JSONObject();
		try
		{

			log.info(" -------- Page Number for " + action + ": "+ (Integer.parseInt(syncParametersMap.get(JsonConstants.SYNC_PAGE_NUMBER))) + " -------- ");

			Map<String, String> headers = (Map<String, String>) applicationConfiguration.getValue("MAP_TO_JSON_CASES_HEADER");
			
			String keyForGroupCases = (String) applicationConfiguration.getValue("keyForGroupCases");

			List<Map<String, Object>> mapCaseList = syncService.getCasesData(syncParametersMap, action);

			mapCaseList = Utilities.eliminateNulls(mapCaseList);

			JSONArray dataJSONArray = syncService.convertBeansToJSON(mapCaseList, keyForGroupCases, headers,0,0,0,0);

			log.info("-----extractStreetwalkSequence CaseList -------" + mapCaseList.size());

			responseJSON.put(JsonConstants.DATA, dataJSONArray);
		}
		catch (Exception e)
		{
			log.error(
					" --------------- Error in StreetwalkSequenceSyncService / extractStreetwalkSequence() --------------- ",
					e);
		}

		return responseJSON;
	}
}
