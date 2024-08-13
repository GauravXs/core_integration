package com.mobicule.mcollections.integration.sync;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONObject;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.mobicule.mcollections.core.beans.Response;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.service.PortfolioSyncCoreService;

public class PortfolioSyncServiceImplementation implements IPortfolioSyncService
{
	private Logger log = LoggerFactory.getLogger(getClass());

	private PortfolioSyncCoreService portfolioSyncCoreService;
	
	public PortfolioSyncCoreService getPortfolioSyncCoreService()
	{
		return portfolioSyncCoreService;
	}

	public void setPortfolioSyncCoreService(PortfolioSyncCoreService portfolioSyncCoreService)
	{
		this.portfolioSyncCoreService = portfolioSyncCoreService;
	}

	@Override
	public Message<String> execute(Message<String> message)
	{
		log.info(" -------- In PortfolioSyncServiceImplementation / execute() -------- ");
		Response response=null;			
		try
		{
			String requestMessage = message.getPayload();

			if ((requestMessage.equals(Constants.EMPTY_STRING))
					|| (requestMessage.equals(Constants.STRING_NULL)) || (requestMessage == null))
			{
				log.info(" -------- No Data found in Request -------- ");

				
			}
			else
			{
				String type = JSONPayloadExtractor.extract(requestMessage, JsonConstants.Key.TYPE);
				String entity = JSONPayloadExtractor.extract(requestMessage, JsonConstants.Key.ENTITY);
				String method = JSONPayloadExtractor.extract(requestMessage, JsonConstants.Key.ACTION);

				if (type.equals(JsonConstants.Key.TYPE_SYNC))
				{
					JSONObject dataJSON = JSONPayloadExtractor.extractJSONObject(requestMessage, JsonConstants.Key.DATA);
					JSONObject userJSON = JSONPayloadExtractor.extractJSONObject(requestMessage, JsonConstants.SYSTEM_USER);

					String pageSize = dataJSON.getString(JsonConstants.Key.PAGE_SIZE);
					String pageNumber = dataJSON.getString(JsonConstants.Key.PAGE_NUMBER);
					String username = userJSON.getString(JsonConstants.USERNAME);

					String lastSyncDate = dataJSON.getString(JsonConstants.Key.LAST_SYNC_DATE);
					
					HashMap<String, String> dataMap=new HashMap<String, String>();
					dataMap.put(JsonConstants.Key.ACTION, method);
					dataMap.put(JsonConstants.Key.PAGE_SIZE, pageSize);
					dataMap.put(JsonConstants.Key.LAST_SYNC_DATE, lastSyncDate);
					dataMap.put(JsonConstants.Key.ENTITY, entity);
					dataMap.put(JsonConstants.Key.PAGE_NUMBER, pageNumber);
					dataMap.put(JsonConstants.USERNAME, username);

					if (method.equals(JsonConstants.Value.ACTION_SYNC_INITIALIZATION))
					{
						log.info(" -------- in  ACTION_SYNC_INITIALIZATION-------- ");
						response = portfolioSyncCoreService.extractInitialization(dataMap);
					}
					if ((method.equals(JsonConstants.Value.ACTION_SYNC_ADD)))
					{
						log.info(" -------- in  ACTION_SYNC_ADD-------- ");
						response = portfolioSyncCoreService.extractData(dataMap);
					}
					if (method.equals(JsonConstants.Value.ACTION_SYNC_COMPLETION))
					{
						log.info(" -------- in ACTION_SYNC_COMPLETION-------- ");
						response = portfolioSyncCoreService.extractCompletion();
					}
				}
			}
			
			System.out.println("response : "+response);
			
			JSONObject responseJSON = new JSONObject();
			responseJSON.put(JsonConstants.DATA, response.getData());
			responseJSON.put(JsonConstants.STATUS, response.getStatus());
			responseJSON.put(JsonConstants.MESSAGE, response.getMessage());
			
			return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
		}
		catch (Exception e)
		{
			log.info(" -------- Error: In PortfolioSyncServiceImplementation / execute() -------- "+e);

			return (MessageBuilder.withPayload(String.valueOf(response)).copyHeaders(message.getHeaders()).build());
		}
		

		
	}
}
