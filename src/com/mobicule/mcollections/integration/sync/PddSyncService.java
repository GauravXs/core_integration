package com.mobicule.mcollections.integration.sync;

/**
 * @author bhushan
 *
 */

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONObject;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.service.PddService;
import com.mobicule.mcollections.core.service.SystemUserService;

public class PddSyncService implements IBankSyncService
{
	public Logger log = LoggerFactory.getLogger(this.getClass()); 

	public PddService pddService;

	public PddService getPddService()
	{
		return pddService;
	}

	public void setPddService(PddService pddService)
	{
		this.pddService = pddService;
	}

	public SystemUserService userService;

	public SystemUserService getUserService()
	{
		return userService;
	}

	public void setUserService(SystemUserService userService)
	{
		this.userService = userService;
	}

	@Override
	public Message<String> execute(Message<String> message) throws Throwable
	{
		log.info(" ----- Inside PddSyncService / execute() ---- ");

		JSONObject responseJSON = new JSONObject();
		try
		{
			String messagePayload = message.getPayload();

			log.info("-------- MessagePayload ----------" + messagePayload);

			if ((messagePayload.equals(Constants.EMPTY_STRING)) || (messagePayload == null))
			{
				log.info(" -------- Error: No Data found in Request -------- ");

				responseJSON.put((JsonConstants.Key.STATUS), (JsonConstants.Value.STATUS_FAILURE));
				responseJSON.put((JsonConstants.Key.MESSAGE), (JsonConstants.Value.MESSAGE_GENERIC_ERROR));
			}
			else

			{
				String type = JSONPayloadExtractor.extract(messagePayload, JsonConstants.Key.TYPE);
				String entity = JSONPayloadExtractor.extract(messagePayload, JsonConstants.Key.ENTITY);
				String action = JSONPayloadExtractor.extract(messagePayload, JsonConstants.Key.ACTION);

				log.info("type : " + type);
				log.info("entity : " + entity);
				log.info("action : " + action);

				if (type.equals(JsonConstants.Key.TYPE_SYNC))
				{
					JSONObject userJSON = JSONPayloadExtractor.extractJSONObject(messagePayload,
							(JsonConstants.SYSTEM_USER));

					SystemUser systemUser = new SystemUser();

					systemUser.setUsername(userJSON.getString(JsonConstants.USERNAME));
					systemUser.setPassword(userJSON.getString(JsonConstants.PASSWORD));
					systemUser.setImeiNo(userJSON.getString(JsonConstants.IMEI_NUMBER));
					systemUser.setUserTableId(Long.parseLong(userJSON.getString(JsonConstants.SYSTEM_USER_ID)));

					JSONObject dataJSON = JSONPayloadExtractor
							.extractJSONObject(messagePayload, JsonConstants.Key.DATA);

					int pageSize = Integer.parseInt(dataJSON.getString(JsonConstants.Key.PAGE_SIZE));

					int pageNumber = Integer.parseInt(dataJSON.getString(JsonConstants.Key.PAGE_NUMBER));

					String lastSyncDate = dataJSON.getString(JsonConstants.Key.LAST_SYNC_DATE);

					if (action.equals(JsonConstants.Value.ACTION_SYNC_INITIALIZATION))
					{
						responseJSON.put((JsonConstants.Key.DATA), (pddService.extractInitializationPdd(systemUser,
								(Long.parseLong(lastSyncDate)), pageSize)));

					}
					else if ((action.equals(JsonConstants.Value.ACTION_SYNC_ADD))
							|| (action.equals(JsonConstants.Value.ACTION_SYNC_MODIFY))
							|| (action.equals(JsonConstants.Value.ACTION_SYNC_DELETE)))
					{
						responseJSON.put((JsonConstants.Key.DATA), (pddService.extractDataPdd(systemUser,
								(Long.parseLong(lastSyncDate)), action, pageSize, pageNumber)));
					}
					else if (action.equals(JsonConstants.Value.ACTION_SYNC_COMPLETION))
					{
						responseJSON.put((JsonConstants.Key.DATA), (pddService.extractCompletionPdd()));
					}

					responseJSON.put((JsonConstants.Key.STATUS), (JsonConstants.Value.STATUS_SUCCESS));
					responseJSON.put((JsonConstants.Key.MESSAGE), (JsonConstants.Value.MESSAGE_SUCCESS));

				}
			}

		}
		catch (Exception e)
		{
			log.info("------ Exception in PddSyncServiceSyncService -------");
			responseJSON.put((JsonConstants.Key.DATA), "");
			responseJSON.put((JsonConstants.Key.STATUS), (JsonConstants.Value.STATUS_FAILURE));
			responseJSON.put((JsonConstants.Key.MESSAGE), (JsonConstants.Value.MESSAGE_GENERIC_ERROR));

			e.printStackTrace();

		}

		return (MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build());
	}

	public Message<String> executeView(Message<String> message) throws Throwable
	{

		log.info(" ----- Inside PddSyncService / execute Model/State/Insurer() ---- ");

		JSONObject responseJSON = new JSONObject();
		try
		{
			String messagePayload = message.getPayload();

			log.info("-------- MessagePayload ----------" + messagePayload);

			if ((messagePayload.equals(Constants.EMPTY_STRING)) || (messagePayload == null))
			{
				log.info(" -------- Error: No Data found in Request -------- ");

				responseJSON.put((JsonConstants.Key.STATUS), (JsonConstants.Value.STATUS_FAILURE));
				responseJSON.put((JsonConstants.Key.MESSAGE), (JsonConstants.Value.MESSAGE_GENERIC_ERROR));
			}
			else

			{
				String type = JSONPayloadExtractor.extract(messagePayload, JsonConstants.Key.TYPE);
				String entity = JSONPayloadExtractor.extract(messagePayload, JsonConstants.Key.ENTITY);
				String action = JSONPayloadExtractor.extract(messagePayload, JsonConstants.Key.ACTION);

				log.info("type : " + type);
				log.info("entity : " + entity);
				log.info("action : " + action);

				/*	if (type.equals(JsonConstants.Key.TYPE_SYNC))
					{
						
						Map syncMap =Utilities.createMapFromJSON(messagePayload);
						log.info("map " + syncMap);
						String Entity = syncMap.get("entity").toString();
						log.info("entity from map :: " + Entity);
						log.info("user from map " + syncMap.get("user"));
						 Map userMap = (Map) syncMap.get("user");
						 String userID =(String)userMap.get("userId");
						 log.info("userid :== " + userID );
					}*/

				if (type.equals(JsonConstants.Key.TYPE_SYNC))
				{
					Map syncMap = Utilities.createMapFromJSON(messagePayload);

					if (action.equals(JsonConstants.Value.ACTION_SYNC_INITIALIZATION))
					{
						responseJSON.put((JsonConstants.Key.DATA), (pddService.extractInitializationFromView(syncMap)));

					}
					else if ((action.equals(JsonConstants.Value.ACTION_SYNC_ADD))
							|| (action.equals(JsonConstants.Value.ACTION_SYNC_MODIFY))
							|| (action.equals(JsonConstants.Value.ACTION_SYNC_DELETE)))
					{
						responseJSON.put((JsonConstants.Key.DATA), (pddService.extractDataFromView(syncMap)));
					}
					else if (action.equals(JsonConstants.Value.ACTION_SYNC_COMPLETION))
					{
						responseJSON.put((JsonConstants.Key.DATA), (pddService.extractCompletionFromView()));
					}

					responseJSON.put((JsonConstants.Key.STATUS), (JsonConstants.Value.STATUS_SUCCESS));
					responseJSON.put((JsonConstants.Key.MESSAGE), (JsonConstants.Value.MESSAGE_SUCCESS));

				}
			}

		}
		catch (Exception e)
		{
			log.info("------ Exception in PddSyncServiceSyncService -------");
			responseJSON.put((JsonConstants.Key.DATA), "");
			responseJSON.put((JsonConstants.Key.STATUS), (JsonConstants.Value.STATUS_FAILURE));
			responseJSON.put((JsonConstants.Key.MESSAGE), (JsonConstants.Value.MESSAGE_GENERIC_ERROR));

			log.error("Exception :: " , e);

		}

		return (MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build());

	}

}
