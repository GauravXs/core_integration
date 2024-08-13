/**
 * 
 */
package com.mobicule.mcollections.integration.systemuser;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.support.MessageBuilder;

import com.mobicule.component.activitylogger.beans.UserActivity;
import com.mobicule.component.activitylogger.commons.ActivityLoggerConstants;
import com.mobicule.component.activitylogger.service.UserActivityService;
import com.mobicule.component.activitylogger.threads.UserActivityAddition;
import com.mobicule.component.activitylogger.threads.UserActivityStatusUpdate;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.service.SystemUserService;
import com.mobicule.mcollections.integration.commons.ServerUtilities;

/**
 * @author prashant
 *
 */
public class RootedDeviceService implements IRootedDeviceService
{
	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private SystemUserService systemUserService;

	@Autowired
	private UserActivityService userActivityService;

	@Override
	public Message<String> execute(Message<String> message) throws Throwable
	{
		String status = JsonConstants.FAILURE;
		String returnMessage = null;
		MessageHeaders messageHeader = message.getHeaders();

		boolean flag = false;

		log.info("------inside root device updation service------");

		try
		{
			String requestSet = message.getPayload();

			log.info("----request set ----" + requestSet);
			String requestEntity = JSONPayloadExtractor.extract(requestSet, JsonConstants.ENTITY);
			String requestAction = JSONPayloadExtractor.extract(requestSet, JsonConstants.ACTION);

			UserActivityAddition userActivityAddition = new UserActivityAddition(requestSet, userActivityService,
					ActivityLoggerConstants.DATABASE_MSSQL);

			new Thread(userActivityAddition).run();

			UserActivity userActivity = userActivityAddition.extractUserActivity();

			JSONObject jsonObject = new JSONObject(requestSet);
			JSONObject data = jsonObject.getJSONObject("data");
			JSONObject user = (JSONObject) jsonObject.get(JsonConstants.SYSTEM_USER);
			SystemUser systemUserNew = ServerUtilities.extractSystemUser(user);

			Map reqMap = Utilities.createMapFromJSON(requestSet);

			log.info("----reqMap----" + reqMap);

			String type = (String) reqMap.get(JsonConstants.Key.TYPE);

			log.info("----data-----" + data);

			log.info("-------type------" + type);

			if (type.toString().equalsIgnoreCase("updateRootedDevice"))
			{
				log.info("---isRooted---" + data.get("isRooted"));

				systemUserNew.setIsRooted((String) data.get("isRooted"));

				flag = systemUserService.updateRootedDevice(systemUserNew);

				if (flag)
				{
					log.info("------------- User Updated sucessfully -------------");

					status = JsonConstants.SUCCESS;

					UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
							(ActivityLoggerConstants.STATUS_SUCCESS), userActivityService);
					new Thread(userActivityStatusUpdate).run();

					return responseBuilder(message, status, "Device Info. got submitted successfully");
				}
				else
				{

					status = JsonConstants.FAILURE;

					returnMessage = "Failure !!!";

					UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
							(ActivityLoggerConstants.STATUS_IGNORE), userActivityService);
					new Thread(userActivityStatusUpdate).run();

					return responseBuilder(message, status, returnMessage);
				}

			}
			else
			{
				status = JsonConstants.FAILURE;

				returnMessage = "Failure !!!";

				UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
						(ActivityLoggerConstants.STATUS_IGNORE), userActivityService);
				new Thread(userActivityStatusUpdate).run();

				return responseBuilder(message, status, returnMessage);
			}

		}

		catch (Exception e)
		{
			status = JsonConstants.FAILURE;
			returnMessage = "Failure...";
			return responseBuilder(message, status, returnMessage);
		}

	}

	private Message<String> responseBuilder(Message<String> message, String status, String returnMessage)
			throws JSONException
	{
		JSONObject responseJSON = new JSONObject();
		responseJSON.put(JsonConstants.STATUS, status);
		responseJSON.put(JsonConstants.MESSAGE, returnMessage);
		responseJSON.put(JsonConstants.DATA, "");

		return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
	}
}
