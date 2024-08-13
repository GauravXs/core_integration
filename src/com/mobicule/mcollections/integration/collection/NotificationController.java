package com.mobicule.mcollections.integration.collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.security.core.context.SecurityContextHolder;

import java.sql.Timestamp;
import java.util.Vector;

import com.mobicule.component.notification.service.NotificationSubscriberService;
import com.mobicule.mcollections.core.beans.NotificationSubscriber;
import com.mobicule.mcollections.core.beans.NotificationSubscriberMapping;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.service.NotificationService;


public class NotificationController 
{
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	NotificationSubscriberService notificationSubscriberService;
	
	@Autowired
	NotificationService notificationService;
	
	
	public NotificationSubscriberService getNotificationSubscriberService() {
		return notificationSubscriberService;
	}



	public void setNotificationSubscriberService(
			NotificationSubscriberService notificationSubscriberService) {
		this.notificationSubscriberService = notificationSubscriberService;
	}



	public NotificationService getNotificationService() {
		return notificationService;
	}



	public void setNotificationService(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	public Message<String> execute(Message<String> message) throws Throwable
	{
		JSONObject responseJSON = new JSONObject();
		String errorMassage = "Error occurred while notification subscribers.";
		String successMassage = "Notification Submission Successful";
		
		try
		{
			String requestSet = message.getPayload();
			JSONObject jsonObj = new JSONObject(requestSet);
			JSONObject jsonData = (JSONObject) jsonObj.get(JsonConstants.DATA);
			JSONObject userJsonData = (JSONObject) jsonObj.get(JsonConstants.SYSTEM_USER);
			
			String deviceId = "";
			String platform = "";
			String model = "";
			String osVersion = "";
			String macAddress = "";
			long userTableId;
			
			
		
			NotificationSubscriber notificationSubscribers = new NotificationSubscriber();

			//String userName = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			
			String userName = (String) (userJsonData.get("username") == null ? "" : userJsonData.get("username"));
			
			deviceId = (String) (jsonData.get("deviceid") == null ? "" : jsonData.get("deviceid"));
			platform = (String) (jsonData.get("platform") == null ? "" : jsonData.get("platform"));
			model = (String) (jsonData.get("model") == null ? "" : jsonData.get("model"));
			osVersion = (String) (jsonData.get("osVersion") == null ? "" : jsonData.get("osVersion"));
			//macAddress = (String) (jsonData.get("macAddress") == null ? "" : jsonData.get("macAddress"));
			userTableId=  (userJsonData.get("userId") == null ? 0L : Long.parseLong(userJsonData.get("userId").toString()));
		
			notificationSubscribers.setId(2L);
			notificationSubscribers.setDeviceid(deviceId);
			notificationSubscribers.setActive("ACT");
			notificationSubscribers.setPlatform(platform);
			notificationSubscribers.setModel(model);
			notificationSubscribers.setOsVersion(osVersion);
			notificationSubscribers.setMacAddress("");
			notificationSubscribers.setCreatedBy(userTableId);
			notificationSubscribers.setModifiedBy(userTableId);
			notificationSubscribers.setCreatedOn(new Timestamp(System.currentTimeMillis()));
			notificationSubscribers.setModifiedOn(new Timestamp(System.currentTimeMillis()));
			notificationSubscribers.setDeleteFlag("F");
			
			//boolean notificationSubscriberFlag = notificationSubscriberService.subscribeToNotifications(notificationSubscribers);
			
			Number notificationSubscriberId = notificationService.subscribeToNotifications(notificationSubscribers);
			
			if(notificationSubscriberId != null)
			{
			
							try
							
							{
								    ///String credentials = new String(AES.decrypt(st.nextToken()));
									NotificationSubscriberMapping notificationSubscriberMapping =   new NotificationSubscriberMapping();
									
									notificationSubscriberMapping.setUserTableId(userTableId);
									notificationSubscriberMapping.setNotificationSubscriberId(Long.parseLong(notificationSubscriberId.toString()));
									notificationSubscriberMapping.setCreatedBy(userTableId);
									notificationSubscriberMapping.setModifiedBy(userTableId);
									notificationSubscriberMapping.setDeleteFlag("F");

									boolean insertFlag = notificationService.insertFCMTokenMapping(notificationSubscriberMapping);
									
									if(insertFlag)
									{
										responseJSON.put(JsonConstants.STATUS, JsonConstants.SUCCESS);
										responseJSON.put(JsonConstants.MESSAGE, errorMassage );
										responseJSON.put(JsonConstants.DATA,successMassage);
										return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
									}
									
							}
							catch(Exception error)
							{
								log.info("Error occurred while notification subscribers"+error);
								responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
								responseJSON.put(JsonConstants.MESSAGE, errorMassage);
								responseJSON.put(JsonConstants.DATA,"");
								return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
							}
			}
			else
			{
				responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
				responseJSON.put(JsonConstants.MESSAGE, errorMassage);
				responseJSON.put(JsonConstants.DATA,"");
				return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
			}
			
		}
		catch(Exception error)
		{
			log.info("Error occurred while notification subscribers"+error);
			responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
			responseJSON.put(JsonConstants.MESSAGE, errorMassage);
			responseJSON.put(JsonConstants.DATA,"");
			return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
		}
		
		responseJSON.put(JsonConstants.STATUS, JsonConstants.FAILURE);
		responseJSON.put(JsonConstants.MESSAGE, errorMassage);
		responseJSON.put(JsonConstants.DATA,"");
		return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
	}
}
