/**
******************************************************************************
* C O P Y R I G H T  A N D  C O N F I D E N T I A L I T Y  N O T I C E
* <p>
* Copyright © 2013-2014 Mobicule Technologies Pvt. Ltd. All rights reserved. 
* This is proprietary information of Mobicule Technologies Pvt. Ltd.and is 
* subject to applicable licensing agreements. Unauthorized reproduction, 
* transmission or distribution of this file and its contents is a 
* violation of applicable laws.
******************************************************************************
*
* @project mCollectionsKMIntegration-Phase2
*/
package com.mobicule.mcollections.integration.feedback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONArray;
import org.json.me.JSONObject;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;

/**
* 
* <enter description here>
*
* @author Trupti
* @see 
*
* @createdOn 16-May-2015
* @modifiedOn
*
* @copyright © 2013-2014 Mobicule Technologies Pvt. Ltd. All rights reserved.
*/
public class FeedbackService implements IFeedbackService
{
	private Logger log = LoggerFactory.getLogger(getClass());

	private com.mobicule.mcollections.core.service.FeedbackService feedbackService ;
	

	
	public com.mobicule.mcollections.core.service.FeedbackService getFeedbackService()
	{
		return feedbackService;
	}
	public void setFeedbackService(com.mobicule.mcollections.core.service.FeedbackService feedbackService)
	{
		this.feedbackService = feedbackService;
	}
	/**
	 * <enter description here>
	 *
	 * <li>pre-condition <enter text> 
	 * <li>post-condition <enter text> 
	 *
	 * @param message
	 * @return
	 * @throws Throwable
	 *
	 * @author trupti
	 * @createdOn 16-May-2015
	 * @modifiedOn 16-May-2015 
	 * 
	 */
	@Override
	public Message<String> execute(Message<String> message) throws Throwable
	{

		log.info("---- Inside Feedback service Integration ------");
		
	 
		JSONObject responseJSON = new JSONObject();
		try
		{
			String requestSet = message.getPayload();

			//JSONObject requestSystemUser = JSONPayloadExtractor.extractJSON(requestSet, JsonConstants.SYSTEM_USER);

			JSONObject requestData = JSONPayloadExtractor.extractJSON(requestSet, JsonConstants.DATA);
			
			String APAC = requestData.getString("unqNo");
			
			String appl = requestData.getString("appl");
			
			APAC = APAC.replace(appl, "");
			//APAC = appl+APAC;
			
			log.info("---- Apac to get feedback History -----" + APAC);
			
			JSONArray feedbackData = feedbackService.getFollowUpHistory(APAC , appl);
			
			log.info("---- Data got ----- " +  feedbackData );
			
			responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
			responseJSON.put(JsonConstants.MESSAGE, JsonConstants.SUCCESS);
			responseJSON.put(JsonConstants.DATA,feedbackData);
			
			

		}
		catch (Exception e)
		{
			e.printStackTrace();
			responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
			responseJSON.put(JsonConstants.MESSAGE, "No Details Found for this APAC!");

		}

		return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
	}

}
