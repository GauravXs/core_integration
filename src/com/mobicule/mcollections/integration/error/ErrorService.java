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
* @project mCollectionsMGLClientIntegration
*/
package com.mobicule.mcollections.integration.error;

import org.json.me.JSONObject;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import com.mobicule.mcollections.core.commons.JsonConstants;

/**
* 
* <enter description here>
*
* @author pranav <enter lastname>
* @see 
*
* @createdOn 11-Jun-2013
* @modifiedOn
*
* @copyright © 2013-2014 Mobicule Technologies Pvt. Ltd. All rights reserved.
*/
public class ErrorService
{
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
	 * @author pranav 
	 * @createdOn 11-Jun-2013
	 * @modifiedOn 11-Jun-2012 
	 * 
	 */
	public Message<String> execute(Message<String> message) throws Throwable
	{
		String authorizationMessage = "Authorization Failed";

		JSONObject responseJSON = new JSONObject();
		responseJSON.put(JsonConstants.Key.STATUS, JsonConstants.Value.STATUS_FAILURE);
		responseJSON.put(JsonConstants.Key.MESSAGE, authorizationMessage);

		return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
	}


}
