package com.mobicule.mcollections.integration.sync;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.mobicule.component.mapconversion.json.MapToJSON;
import com.mobicule.mcollections.core.beans.MPOSDetail;
import com.mobicule.mcollections.core.beans.Response;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.service.MposService;

public class MposConfigurationSyncService implements IMposConfigurationSyncService
{
	private Logger log = LoggerFactory.getLogger(getClass());

	private MposService mdrSyncCoreService;

	public MposService getMdrSyncCoreService()
	{
		return mdrSyncCoreService;
	}

	public void setMdrSyncCoreService(MposService mdrSyncCoreService)
	{
		this.mdrSyncCoreService = mdrSyncCoreService;
	}

	@Override
	public Message<String> execute(Message<String> message) throws Throwable
	{
		log.info(" -------- In Mops Data / execute() -------- ");
		JSONObject responseJSON = new JSONObject();
		Response response = null;
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

				log.info(" -------- in Mops add-------- ");
				responseJSON.put((JsonConstants.Key.DATA), (mdrSyncCoreService.getMposMapping()));
				responseJSON.put((JsonConstants.Key.STATUS), (JsonConstants.Value.STATUS_SUCCESS));
				responseJSON.put((JsonConstants.Key.MESSAGE), (JsonConstants.Value.MESSAGE_SUCCESS));

			}
		}
		catch (Exception e)
		{
			log.info("------ Exception in MposConfigrationSyncService -------");
			responseJSON.put((JsonConstants.Key.DATA), "");
			responseJSON.put((JsonConstants.Key.STATUS), (JsonConstants.Value.STATUS_FAILURE));
			responseJSON.put((JsonConstants.Key.MESSAGE), (JsonConstants.Value.MESSAGE_GENERIC_ERROR));

			log.error("Exception occured ", e);

		}
		return (MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build());
	}

}
