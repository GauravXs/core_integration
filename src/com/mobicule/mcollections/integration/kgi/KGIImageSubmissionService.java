/**
 * 
 */
package com.mobicule.mcollections.integration.kgi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.transaction.SystemException;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.support.MessageBuilder;

import com.mobicule.component.activitylogger.beans.UserActivity;
import com.mobicule.component.activitylogger.commons.ActivityLoggerConstants;
import com.mobicule.component.activitylogger.service.CommunicationActivityService;
import com.mobicule.component.activitylogger.service.UserActivityService;
import com.mobicule.component.activitylogger.threads.UserActivityAddition;
import com.mobicule.component.activitylogger.threads.UserActivityStatusUpdate;
import com.mobicule.mcollections.core.beans.Image;
import com.mobicule.mcollections.core.beans.KGI;
import com.mobicule.mcollections.core.beans.Settlement;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.KGIService;
import com.mobicule.mcollections.core.service.SettlementService;
import com.mobicule.mcollections.core.service.SystemUserService;
import com.mobicule.mcollections.integration.commons.ServerUtilities;

/**
 * @author prashant
 *
 */
public class KGIImageSubmissionService implements IKGIImageSubmissionService
{
	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private UserActivityService userActivityService;

	@Autowired
	private SystemUserService systemUserService;

	@Autowired
	private CommunicationActivityService communicationActivityService;

	@Autowired
	private KGIService kgiService;

	@Autowired
	ApplicationConfiguration applicationConfiguration;

	@Override
	public Message<String> execute(Message<String> message) throws Throwable
	{
		log.info("---- Inside SettlementImageSubmissionService ---- ");

		String deviceDateTime = "";

		String status = JsonConstants.FAILURE;

		String returnMessage = null;

		String reqId = "";

		String imageId = "";

		JSONArray imageDetails = new JSONArray();

		boolean submissionFlag = false;

		List<Image> images = new ArrayList<Image>();

		MessageHeaders messageHeader = message.getHeaders();

		//SystemUser systemUser = (SystemUser) messageHeader.get(Constants.SYSTEM_USER_BEAN);

		KGI kgi = new KGI();

		try
		{
			String requestSet = message.getPayload();

			log.info(" ---Original requestSet --- " + requestSet);

			JSONObject jsonObj = new JSONObject(requestSet);

			JSONObject jsonData = (JSONObject) jsonObj.get("data");

			jsonData.remove("images");

			jsonObj.put("data", jsonData);

			String reqSet = jsonObj.toString();

			log.info("--- without images reqSet ---- " + reqSet);

			UserActivityAddition userActivityAddition = new UserActivityAddition(reqSet, userActivityService,
					ActivityLoggerConstants.DATABASE_MSSQL);
			new Thread(userActivityAddition).run();

			UserActivity userActivity = userActivityAddition.extractUserActivity();

			JSONObject jsonObject = new JSONObject(requestSet);
			JSONObject data = (JSONObject) jsonObject.get(JsonConstants.DATA);
			JSONObject user = (JSONObject) jsonObject.get(JsonConstants.SYSTEM_USER);
			SystemUser systemUserNew = ServerUtilities.extractSystemUser(user);

			log.info("-----systemUserNew-----" + systemUserNew.toString());

			Map reqMap = Utilities.createMapFromJSON(requestSet);

			log.info("----reqmap----" + reqMap.toString());

			deviceDateTime = data.get(JsonConstants.DEVICE_DATE_TIME) == null ? "" : (String) data
					.get(JsonConstants.DEVICE_DATE_TIME);

			reqId = data.get(JsonConstants.SettlmentConstant.REQUEST_ID) == null ? "" : (String) data
					.get(JsonConstants.SettlmentConstant.REQUEST_ID);

			/*imageId = data.get(JsonConstants.SettlmentConstant.IMAGE_ID) == null ? "" : (String) data
					.get(JsonConstants.SettlmentConstant.IMAGE_ID);*/

			
			
			kgi.setReqId(reqId);

			imageDetails = data.getJSONArray(JsonConstants.SettlmentConstant.IMAGES);

			log.info("imageDetails" + imageDetails);
			log.info("imagelength" + imageDetails.length());

			images = getImages(systemUserNew, imageDetails, kgi);

			kgi.setImages(images);

			log.info("--- KGI Images ----" + kgi.getImages());

			if (images == null)
			{

				status = JsonConstants.FAILURE;
				returnMessage = "Image Not Found";
				return responseBuilder(message, status, returnMessage, "");
			}

			if (true)
			{

				submissionFlag = kgiService.submitKGIImage(kgi);

				log.info("-----submissionFlag-----" + submissionFlag);

				if (submissionFlag)
				{
					log.info("Settlement Image submitted without violation");

					status = JsonConstants.SUCCESS;

					UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
							(ActivityLoggerConstants.STATUS_SUCCESS), userActivityService);
					new Thread(userActivityStatusUpdate).run();

					return responseBuilder(message, status, "KGI image got submitted successfully", "");

				}
				else
				{

					log.info("Settlement image submitted with violation");

					status = JsonConstants.FAILURE;

					UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
							(ActivityLoggerConstants.STATUS_FAILURE), userActivityService);
					new Thread(userActivityStatusUpdate).run();

					return responseBuilder(message, status, "Some error has occured", "");

				}// status = JsonConstants.SUCCESS;

			}

			else
			{
				log.info("--------- Settlement Image Record already exists, JSON Duplicated! ------------");

				status = JsonConstants.SUCCESS;

				returnMessage = "JSON DUPLICATED!!!";

				UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
						(ActivityLoggerConstants.STATUS_IGNORE), userActivityService);
				new Thread(userActivityStatusUpdate).run();

				return responseBuilder(message, status, returnMessage, "");
			}

		}
		catch (Exception e)
		{

			log.error("---Exception in image submission service -----", e);

			return responseBuilder(message, status, "Some error has occured", "");
		}

	}

	private List<Image> getImages(SystemUser systemUser, JSONArray imageDetails, KGI kgi)
			throws JSONException
	{

		JSONObject imageDetail = new JSONObject();
		Image image = null;
		String imagePath = null;

		List<Image> images = new ArrayList<Image>();

		log.info("---image details length ----" + imageDetails.length());

		for (int index = 0; index < imageDetails.length(); index++)
		{

			log.info("----inside loop ----");

			imageDetail = (JSONObject) imageDetails.get(index);

			if (!imageDetail.has(JsonConstants.RequestData.IMAGE))
			{

				return new ArrayList<Image>();
			}

			String imageId = (String) imageDetail.get(JsonConstants.SettlmentConstant.IMAGE_ID);
			String imageName = (String) imageDetail.get(JsonConstants.SettlmentConstant.IMAGE_NAME);
			String imageByteArray = (String) imageDetail.get(JsonConstants.RequestData.IMAGE);

			if (imageByteArray.isEmpty())
			{

				return new ArrayList<Image>();
			}
			image = new Image();

			imagePath = (extractImagePath(kgi, imageId, imageByteArray, (String.valueOf(index))));

			if (imagePath.equals(JsonConstants.ERROR))
			{

				return null;
			}
			else
			{
			//	imagePath = kgi.getReqId() + "/" + imageName + Constants.EXTENSION_IMAGE;

				log.info("--imagePath----" + imagePath);

				//reqId + "/" + imageName + Constants.EXTENSION_IMAGE;

				image = new Image();
				image.setPath(imagePath);
				image.setImageId(imageId);
				image.setImageName(imageName);

				Utilities.primaryBeanSetter(image, systemUser);

				images.add(image);

			}
		}
		return images;
	}

	private String extractImagePath(KGI kgi, String imageId, String type, String index)
	{
		try
		{

			String fileName =imageId + Constants.SYMBOL_UNDERSCORE + kgi.getReqId()
					+ Constants.SYMBOL_UNDERSCORE + System.currentTimeMillis();

			String filePath = Constants.EMPTY_STRING;

			if (index.equals(Constants.EMPTY_STRING))
			{

				filePath = generateFilePath(
						(String) applicationConfiguration.getValue(Constants.Settlement.KGI_IMAGE_PATH),
						fileName, kgi.getReqId());
			}
			else
			{

				filePath = generateFilePath(
						(String) applicationConfiguration.getValue(Constants.Settlement.KGI_IMAGE_PATH),
						(fileName + "_" + index), kgi.getReqId());
			}

			
			log.info("----filePath----"+filePath);
			
			if (writeImage(filePath, type))
			{

				return filePath;
			}
			else
			{
				return (JsonConstants.ERROR);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return (JsonConstants.ERROR);
		}
	}

	private Message<String> responseBuilder(Message<String> message, String status, String returnMessage, String imageId)
			throws JSONException
	{
		JSONObject responseJSON = new JSONObject();
		JSONObject data = new JSONObject();

		data.put("imageId", imageId);

		responseJSON.put(JsonConstants.STATUS, status);
		responseJSON.put(JsonConstants.MESSAGE, returnMessage);
		responseJSON.put(JsonConstants.DATA, data);

		return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
	}

	public static boolean writeImage(String filePath, String image)
	{
		try
		{
			byte[] imageBytes = Base64.decodeBase64(toByteArray(image));

			File file = new File(filePath);

			OutputStream outputStream = new FileOutputStream(file);

			outputStream.write(imageBytes);
			outputStream.close();

			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();

			return false;
		}
	}

	private static byte[] toByteArray(String s) throws SystemException
	{
		try
		{
			return s.getBytes("UTF-8");
		}
		catch (Exception e)
		{
			SystemException se = new SystemException();

			throw se;
		}
	}

	public static String generateFilePath(String filePath, String fileName, String reqId)
	{

		if (!(Utilities.validateDirectoryExistence(filePath + reqId)))
		{
			(new File(filePath + reqId)).mkdir();
		}

		filePath =  filePath +reqId+"/"+fileName + Constants.EXTENSION_IMAGE;

		return filePath;
	}

	public static boolean validateDirectoryExistence(String directoryPath)
	{
		File directory = new File(directoryPath);

		if (directory.exists())
		{
			return true;
		}

		return false;
	}

}
