package com.mobicule.mcollections.integration.sync;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.mobicule.component.devicesync.commons.DeviceSyncConstants;
import com.mobicule.component.devicesync.intelligence.ISyncIntelligence;
import com.mobicule.component.json.parser.JSONParser;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;

public class BankSyncService implements IBankSyncService
{
	public Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	public ApplicationConfiguration<String, Object> applicationConfiguration;

	@Autowired
	public ISyncIntelligence iSyncIntelligence;

	@Override
	public Message<String> execute(Message<String> message) throws Throwable
	{
		log.info(" -------- In BankSyncService / execute -------- ");

		JSONObject responseJSON = new JSONObject();

		try
		{
			String messagePayload = message.getPayload();

			String entity = JSONPayloadExtractor.extract(messagePayload, JsonConstants.Key.ENTITY);

			Map<String, String> mapOutput = new HashMap<String, String>();

			if (entity.equals(JsonConstants.ENTITY_BANK_CUSTOMERS))
			{
				mapOutput = (Map<String, String>) applicationConfiguration.getValue("BANK_CUSTOMERS");
			}
			else if (entity.equals("DEPOSITION_CCAPAC"))
			{
				log.info("----bank sync ---" + messagePayload);

				mapOutput = (Map<String, String>) applicationConfiguration.getValue("DEPOSITION_CCAPAC");

			}
			else if(entity.equalsIgnoreCase("ReasonMaster"))
			{
				log.info("----bank sync ReasonMaster---" + messagePayload);
				
				messagePayload = messagePayload.replace("\"entity\":\"ReasonMaster\"", "\"entity\":\"REASON_MASTER\"");

				mapOutput = (Map<String, String>) applicationConfiguration.getValue("ReasonMaster");
			}

			responseJSON = iSyncIntelligence.sync(messagePayload, (new HashMap<String, String>()), mapOutput,
					(DeviceSyncConstants.DATABASE_MSSQL));

			log.info(" ###" + entity + "---" + responseJSON);

		}
		catch (Exception e)
		{
			log.info(" -------- Error in BankSyncService / execute -------- ");

			e.printStackTrace();
		}

		return (MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build());
	}
}
