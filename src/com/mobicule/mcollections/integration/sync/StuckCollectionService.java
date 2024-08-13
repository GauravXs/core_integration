package com.mobicule.mcollections.integration.sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.mobicule.component.mapconversion.json.MapToJSON;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.CollectionService;

public class StuckCollectionService implements IStuckCollectionService
{
	Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	ApplicationConfiguration applicationConfiguration;

	public CollectionService getCollectionService()
	{
		return collectionService;
	}

	public void setCollectionService(CollectionService collectionService)
	{
		this.collectionService = collectionService;
	}

	private CollectionService collectionService;

	@Override
	public Message<String> execute(Message<String> message) throws Throwable
	{
		log.info("<-------------- Inside execute / StuckCollectionService ------------->");
		JSONObject responseJSON = new JSONObject();
		Map<String, Object> responseMap = new HashMap<String, Object>();
		try
		{
			String messagePayload = message.getPayload();
			String userdate = JSONPayloadExtractor.extract(messagePayload, JsonConstants.SYSTEM_USER);
			String userName = JSONPayloadExtractor.extract(userdate, JsonConstants.USERNAME);
			List<String> StuckCollectionDBColumn = (List<String>) applicationConfiguration
					.getValue("STUCK_COLLECTION_DB_COLUMN");
			Map<String, String> stuckCollectionJsonHeader = (Map<String, String>) applicationConfiguration
					.getValue("STUCK_COLLECTION_JSON_HEADER");

			if (messagePayload.equals(Constants.EMPTY_STRING))
			{
				log.info("<----------------------- Empty Request message ----------------------->");
			}
			else
			{
				if (userdate != null)
				{
					/**
					 * @author Shyam Yadav
					 * @usecase As of now there is no feature such as stuck cases in RBL
					 * @date: 19 March 2020
					 */
					List<Map<String, Object>> stuckCollectionData = new ArrayList<>();
					//stuckCollectionData = collectionService.getCollectionStuckCase(userName);
							
					if (stuckCollectionData != null)
					{
						stuckCollectionData = Utilities.eliminateNulls(stuckCollectionData);
						stuckCollectionData = Utilities.eliminateUnwanted(stuckCollectionData, StuckCollectionDBColumn);
						responseMap = Utilities.generateReponseMap(JsonConstants.SUCCESS,
								Constants.Messages.STUCK_COLLECTION_SUCCESS, stuckCollectionData);
					}
					else
					{
						responseMap = Utilities.generateReponseMap(JsonConstants.FAILURE,
								Constants.Messages.STUCK_COLLECTION_FAILURE, new ArrayList<Map<String, Object>>());
					}

				}
				else
				{
					log.info("<--------------- null user/data request ------------------->");
					responseMap = Utilities.generateReponseMap(JsonConstants.FAILURE,
							JsonConstants.MESSAGE_GENERAL_FAILURE, new ArrayList<Map<String, Object>>());
				}
				responseJSON = MapToJSON.convertMapToJSON(responseMap, stuckCollectionJsonHeader);
			}

		}
		catch (Exception ex)
		{
			log.info("<------------------ Exception while getting stuck collection ------------------->");
			responseMap = Utilities.generateReponseMap(JsonConstants.FAILURE, JsonConstants.MESSAGE_GENERAL_FAILURE,
					new ArrayList<Map<String, Object>>());
		}
		return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
	}

}
