/**
 * 
 */
package com.mobicule.mcollections.integration.sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONArray;
import org.json.me.JSONException;
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
import com.mobicule.mcollections.core.service.SpecialFeedbackService;
import com.mobicule.mcollections.core.service.SyncService;
import com.mobicule.mcollections.core.service.SystemUserService;
import com.mobicule.mcollections.integration.commons.ServerUtilities;

/**
 * @author amol
 *
 * Kakade
 */
public class SpecialFeedbackSyncService implements ISpecialFeedbackSyncService
{
	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private SystemUserService systemUserService;

	@Autowired
	private SyncService syncService;

	@Autowired
	private ApplicationConfiguration<String, Object> applicationConfiguration;

	private SpecialFeedbackService specialFeedbackService;

	public SpecialFeedbackService getSpecialFeedbackService()
	{
		return specialFeedbackService;
	}

	public void setSpecialFeedbackService(SpecialFeedbackService specialFeedbackService)
	{
		this.specialFeedbackService = specialFeedbackService;
	}

	@Override
	public Message<String> execute(Message<String> message) throws Throwable
	{
		JSONObject responseJSON = new JSONObject();

		try
		{
			String requestSet = message.getPayload();

			String requestEntity = JSONPayloadExtractor.extract(requestSet, JsonConstants.ENTITY);
			String requestAction = JSONPayloadExtractor.extract(requestSet, JsonConstants.ACTION);

			JSONObject requestSystemUser = JSONPayloadExtractor.extractJSON(requestSet, JsonConstants.SYSTEM_USER);
			JSONObject requestStreetWalkSequence = JSONPayloadExtractor.extractJSON(requestSet,
					JsonConstants.STREETWALK_SEQUENCE);
			JSONObject requestData = JSONPayloadExtractor.extractJSON(requestSet, JsonConstants.DATA);

			SystemUser systemUser = ServerUtilities.extractSystemUser(requestSystemUser);

			Map<String, String> streetwalkSequenceParametersMapPresent = ServerUtilities
					.extractStreetwalkSequenceParameters(requestStreetWalkSequence);

			Map<String, String> syncParametersMap = extractSyncParameters(requestData);

			syncParametersMap.remove(JsonConstants.SYNC_PAGE_SIZE);
			syncParametersMap.put(JsonConstants.SYNC_PAGE_SIZE, "10000");

			if (syncService.checkforStreetwalkSequenceAssignment(systemUser))
			{
				Map<String, String> streetwalkSequenceParametersMapFuture = syncService
						.extractStreetwalkSequenceVersion(systemUser);

				String streetwalkSequenceChange = streetSequenceComparision(streetwalkSequenceParametersMapPresent,
						streetwalkSequenceParametersMapFuture);
				log.info("streetwalkSequenceChange" + streetwalkSequenceChange);
				if ((requestEntity.equals(JsonConstants.ENTITY_SPECIAL_FEEDBACK))
						&& (requestAction.equals(JsonConstants.SYNC_INITIALIZATION)))
				{
					responseJSON = extractStreetwalkSequenceInitialization(syncParametersMap, streetwalkSequenceChange,
							streetwalkSequenceParametersMapFuture);

					log.info(" -------- Streetwalk Sequence Intitialization Data extracted Successfully -------- ");

					responseJSON.put(JsonConstants.MESSAGE, JsonConstants.SUCCESS);
				}
				else if ((requestEntity.equals(JsonConstants.ENTITY_SPECIAL_FEEDBACK))
						&& (requestAction.equals(JsonConstants.SYNC_ADD)))
				{
					responseJSON = extractStreetwalkSequence(syncParametersMap, JsonConstants.SYNC_ADD,
							streetwalkSequenceChange,
							(streetwalkSequenceParametersMapFuture.get(JsonConstants.STREETWALK_SEQUENCE_NAME)));

					log.info(" -------- Streetwalk Sequence Added extracted Successfully -------- ");

					responseJSON.put(JsonConstants.MESSAGE, JsonConstants.SYNC_SUCCESS_MESSAGE);
				}
				/*else if ((requestEntity.equals(JsonConstants.ENTITY_SPECIAL_FEEDBACK))
						&& (requestAction.equals(JsonConstants.SYNC_MODIFY)))
				{
					responseJSON = extractStreetwalkSequence(syncParametersMap, JsonConstants.SYNC_MODIFY,
							streetwalkSequenceChange,
							(streetwalkSequenceParametersMapFuture.get(JsonConstants.STREETWALK_SEQUENCE_NAME)));

					log.info(" -------- Streetwalk Sequence Modified extracted Successfully -------- ");

					responseJSON.put(JsonConstants.MESSAGE, JsonConstants.SYNC_SUCCESS_MESSAGE);
				}
				else if ((requestEntity.equals(JsonConstants.ENTITY_SPECIAL_FEEDBACK))
						&& (requestAction.equals(JsonConstants.SYNC_DELETE)))
				{
					responseJSON = extractStreetwalkSequence(syncParametersMap, JsonConstants.SYNC_DELETE,
							streetwalkSequenceChange,
							(streetwalkSequenceParametersMapFuture.get(JsonConstants.STREETWALK_SEQUENCE_NAME)));

					log.info(" -------- Streetwalk Sequence Deleted extracted Successfully -------- ");

					responseJSON.put(JsonConstants.MESSAGE, JsonConstants.SYNC_SUCCESS_MESSAGE);
				}
				else if ((requestEntity.equals(JsonConstants.ENTITY_SPECIAL_FEEDBACK))
						&& (requestAction.equals(JsonConstants.SYNC_COMPLETION)))
				{
					responseJSON = extractStreetwalkSequenceVersion(streetwalkSequenceParametersMapFuture);

					log.info(" -------- Streetwalk Sequence Version Details extracted Successfully -------- ");

					responseJSON.put(JsonConstants.MESSAGE, JsonConstants.SUCCESS);
				}*/

				responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
			}
			else
			{
				log.info(" -------- No Streetwalk Sequence assigned to this User -------- ");

				responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
				responseJSON.put(JsonConstants.MESSAGE, JsonConstants.STREETWALK_SEQUENCE_NOT_ASSIGNED_MESSAGE);
				JSONObject dataJSON = new JSONObject();
				dataJSON.put("cleanData", true);
				responseJSON.put(JsonConstants.DATA, dataJSON);
			}
		}
		catch (Exception e)
		{
			log.error(" -------- Error in StreetwalkSequenceSyncService -------- ", e);

			responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
			responseJSON.put(JsonConstants.MESSAGE, JsonConstants.SYNC_FAILURE_MESSAGE);
		}

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

	private JSONObject extractStreetwalkSequence(Map<String, String> syncParametersMap, String action,
			String streetSequenceChange, String streetwalkSequenceName)
	{
		JSONObject responseJSON = new JSONObject();
		try
		{

			log.info(" -------- Page Number for " + action + ": "
					+ (Integer.parseInt(syncParametersMap.get(JsonConstants.SYNC_PAGE_NUMBER))) + " -------- ");

			Map<String, String> headers = (Map<String, String>) applicationConfiguration
					.getValue("MAP_TO_JSON_SPECIAL_FEEDBACK_HEADER");
			String keyForGroupCases = (String) applicationConfiguration.getValue("keyForGroupCases");

			List<String> apacNumberList = new ArrayList<String>();

			List<Map<String, Object>> mapCaseList = syncService.extractStreetwalkSequence(
					(syncParametersMap.get(JsonConstants.SYNC_LAST_SYNC_DATE)),
					(Integer.parseInt(syncParametersMap.get(JsonConstants.SYNC_PAGE_NUMBER))),
					(Integer.parseInt(syncParametersMap.get(JsonConstants.SYNC_PAGE_SIZE))), action,
					streetSequenceChange, streetwalkSequenceName);

			//log.info("caseList:" + mapCaseList.size());

			mapCaseList = Utilities.eliminateNulls(mapCaseList);

			for (Map<String, Object> row : mapCaseList)
			{
				String apacNum = (String) row.get("APAC_CARD_NUMBER");
				String appl = (String) row.get("APPL");

				String regex = "[0-9]+";

				if (apacNum.matches(regex))
				{
					StringBuilder apacNumber = new StringBuilder();
					apacNumber.append(appl).append(apacNum);
					log.info("Apac Number :: " + apacNumber);
					apacNumberList.add(apacNumber.toString());
				}
				else
				{
					log.info("Apac Number :: " + apacNum);
					log.info("Appl :: " + appl);
					apacNumberList.add(apacNum.toString());
				}

			}

			List<Map<String, Object>> specialFeedback = new ArrayList<Map<String, Object>>();
			if (apacNumberList.size() != 0)
			{
				specialFeedback = specialFeedbackService.getSpecialFeedbacks(apacNumberList);
				apacNumberList.clear();
			}
			log.info("Special Feedback List:" + specialFeedback.size());

			JSONArray dataJSONArray = syncService.convertBeansToJSONSpecialFeedback(specialFeedback, keyForGroupCases,
					headers);

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

	/*private JSONObject extractStreetwalkSequenceVersion(Map<String, String> streetwalkSequenceParametersMap)
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
	}*/

	private JSONObject extractSyncCounts(Map<String, String> syncParametersMap, JSONObject dataJSON,
			String streetSequenceChange, String streetwalkSequenceName)
	{
		try
		{
			int syncCountAddition = extractCACount(syncParametersMap, JsonConstants.SYNC_ADD, streetSequenceChange,
					streetwalkSequenceName);
			log.info("syncContAddition " + syncCountAddition);

			dataJSON.put(JsonConstants.SYNC_ADD, syncCountAddition);
			dataJSON.put(JsonConstants.SYNC_MODIFY, 0);
			dataJSON.put(JsonConstants.SYNC_DELETE, 0);
		}
		catch (Exception e)
		{
			log.error(" --------------- Error in StreetwalkSequenceSyncService / extractSyncCounts() --------------- ",
					e);
		}

		return dataJSON;
	}

	private int extractCACount(Map<String, String> syncParametersMap, String action, String streetSequenceChange,
			String streetwalkSequenceName)
	{
		try
		{
			List<String> apacNumberList = new ArrayList<String>();
			List<Map<String, Object>> mapCaseList = syncService.extractStreetwalkSequence(
					(syncParametersMap.get(JsonConstants.SYNC_LAST_SYNC_DATE)),
					(Integer.parseInt(syncParametersMap.get(JsonConstants.SYNC_PAGE_NUMBER))),
					(Integer.parseInt(syncParametersMap.get(JsonConstants.SYNC_PAGE_SIZE))), action,
					streetSequenceChange, streetwalkSequenceName);

			mapCaseList = Utilities.eliminateNulls(mapCaseList);

			for (Map<String, Object> row : mapCaseList)
			{
				String apacNum = (String) row.get("APAC_CARD_NUMBER");
				String appl = (String) row.get("APPL");

				String regex = "[0-9]+";

				if (apacNum.matches(regex))
				{
					StringBuilder apacNumber = new StringBuilder();
					apacNumber.append(appl).append(apacNum);
					log.info("Apac Number :: " + apacNumber);
					apacNumberList.add(apacNumber.toString());
				}
				else
				{
					log.info("Apac Number :: " + apacNum);
					log.info("Appl :: " + appl);
					apacNumberList.add(apacNum.toString());
				}

			}

			List<Map<String, Object>> specialFeedback = new ArrayList<Map<String, Object>>();
			if (apacNumberList.size() != 0)
			{
				specialFeedback = specialFeedbackService.getSpecialFeedbacks(apacNumberList);
				apacNumberList.clear();
			}

			log.info("caseList:" + specialFeedback.size());
			if (specialFeedback.size() > 0)
			{
				return 1;
			}
		}
		catch (Exception e)
		{
			log.error(" --------------- Error in SpecialFeedbackSyncService / extractCACount() --------------- ", e);
		}
		return 0;
	}
}
