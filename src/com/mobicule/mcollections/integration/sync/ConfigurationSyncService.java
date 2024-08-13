/**
 ****************************************************************************** 
 * C O P Y R I G H T A N D C O N F I D E N T I A L I T Y N O T I C E
 * <p>
 * Copyright © 2013-2014 Mobicule Technologies Pvt. Ltd. All rights reserved.
 * This is proprietary information of Mobicule Technologies Pvt. Ltd.and is
 * subject to applicable licensing agreements. Unauthorized reproduction,
 * transmission or distribution of this file and its contents is a violation of
 * applicable laws.
 ****************************************************************************** 
 * 
 * @project mCollectionsKMIntegration-Phase2
 */
package com.mobicule.mcollections.integration.sync;

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
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.ConfigurationService;
import com.mobicule.mcollections.core.service.SystemUserService;

/**
* 
* <enter description here>
*
* @author Trupti
* @see 
*
* @createdOn 07-Apr-2015
* @modifiedOn
*
* @copyright © 2013-2014 Mobicule Technologies Pvt. Ltd. All rights reserved.
*/
public class ConfigurationSyncService implements IConfigurationSyncService
{
	public ConfigurationService configurationService;

	public ApplicationConfiguration<String, Object> applicationConfiguration;

	public ApplicationConfiguration<String, Object> getApplicationConfiguration()
	{
		return applicationConfiguration;
	}

	public void setApplicationConfiguration(ApplicationConfiguration<String, Object> applicationConfiguration)
	{
		this.applicationConfiguration = applicationConfiguration;
	}

	private ApplicationConfiguration<String, Object> applicationConfigurationObject;


	public ApplicationConfiguration<String, Object> getApplicationConfigurationObject() {
		return applicationConfigurationObject;
	}

	public void setApplicationConfigurationObject(ApplicationConfiguration<String, Object> applicationConfigurationObject) {
		this.applicationConfigurationObject = applicationConfigurationObject;
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

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	public void setConfigurationService(ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	public Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * <enter description here>
	 *
	 * <li>pre-condition <enter text> 
	 * <li>post-condition <enter text> 
	 *
	 * @param arg0
	 * @return
	 * @throws Throwable
	 *
	 * @author trupti
	 * @createdOn 07-Apr-2015
	 * @modifiedOn 07-Apr-2015 
	 * 
	 */
	@Override
	public Message<String> execute(Message<String> message) throws Throwable
	{
		log.info(" -------- In AbstractTariffPlanSyncService / execute() -------- ");

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

					// get System User bean in detail
					systemUser = userService.getUser(systemUser.getUsername());

					//Capture Data Json
					JSONObject dataJSON = JSONPayloadExtractor
							.extractJSONObject(messagePayload, JsonConstants.Key.DATA);

					int pageSize = Integer.parseInt(dataJSON.getString(JsonConstants.Key.PAGE_SIZE));

					int pageNumber = Integer.parseInt(dataJSON.getString(JsonConstants.Key.PAGE_NUMBER));

					String lastSyncDate = dataJSON.getString(JsonConstants.Key.LAST_SYNC_DATE);

					if (action.equals(JsonConstants.Value.ACTION_SYNC_INITIALIZATION))
					{
						responseJSON.put(
								(JsonConstants.Key.DATA),
								(configurationService.extractInitializationConfiguration(systemUser,
										(Long.parseLong(lastSyncDate)), pageSize)));

					}
					else if ((action.equals(JsonConstants.Value.ACTION_SYNC_ADD))
							|| (action.equals(JsonConstants.Value.ACTION_SYNC_MODIFY))
							|| (action.equals(JsonConstants.Value.ACTION_SYNC_DELETE)))
					{

						Map<String, String> headersMap = (Map<String, String>) applicationConfiguration
								.getValue("CONFIGURATION_SYNC_HEADERS_MAP");

						/*Map<String ,Object> headersMap = new HashMap<String, Object>();

						headersMap.put("portfolioCode", "pCode");
						headersMap.put("portfolioName", "pName");
						headersMap.put("configurationList", "config");*/

						log.info("-- Configurations Sync Map ----" + headersMap);

						responseJSON.put(
								(JsonConstants.Key.DATA),
								(configurationService.extractDataConfiguration(systemUser,
										(Long.parseLong(lastSyncDate)), action, pageSize, pageNumber, headersMap)));
					}
					else if (action.equals(JsonConstants.Value.ACTION_SYNC_COMPLETION))
					{
						responseJSON.put((JsonConstants.Key.DATA),
								(configurationService.extractCompletionConfiguration()));
					}

					responseJSON.put((JsonConstants.Key.STATUS), (JsonConstants.Value.STATUS_SUCCESS));
					responseJSON.put((JsonConstants.Key.MESSAGE), (JsonConstants.Value.MESSAGE_SUCCESS));

				}
			}

		}
		catch (Exception e)
		{
			log.info("------ Exception in Configuration sync -------");
			responseJSON.put((JsonConstants.Key.STATUS), (JsonConstants.Value.STATUS_FAILURE));
			responseJSON.put((JsonConstants.Key.MESSAGE), (JsonConstants.Value.MESSAGE_GENERIC_ERROR));

			e.printStackTrace();

		}

		return (MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build());
	}

}
