package com.mobicule.mcollections.integration.collection;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
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
import com.mobicule.mcollections.core.beans.PddCollection;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.PddService;
import com.mobicule.mcollections.core.service.SystemUserService;
import com.mobicule.mcollections.integration.commons.ServerUtilities;
/**
 * @author bhushan
 *
 */
public class PddImageSubmissionService implements IPddImageSubmissionService
{

	static private Logger log = LoggerFactory.getLogger(PddImageSubmissionService.class);

	
	@Autowired
	private UserActivityService userActivityService;

	@Autowired
	private SystemUserService systemUserService;

	@Autowired
	private CommunicationActivityService communicationActivityService;

	@Autowired
	private PddService pddService;

	@Autowired
	ApplicationConfiguration applicationConfiguration;

	@Override
	public Message<String> execute(Message<String> message) throws Throwable
	{
		log.info("---- Inside PddImageSubmissionService ---- ");

		String status = JsonConstants.FAILURE;

		String returnMessage = null;

		String pddCollId = "";

		String imageId = "";

		JSONArray imageDetails = new JSONArray();

		boolean submissionFlag = false;

		List<Image> images = new ArrayList<Image>();

		MessageHeaders messageHeader = message.getHeaders();

		PddCollection pddCollection = new PddCollection();

		String imageName = "";

		try
		{

			String requestSet = message.getPayload();

			JSONObject jsonObj = new JSONObject(requestSet);

			JSONObject jsonData = (JSONObject) jsonObj.get("data");

			jsonData.remove("images");

			jsonObj.put("data", jsonData);

			String reqSet = jsonObj.toString();

			log.error("--- without images reqSet ---- " + reqSet);

			UserActivityAddition userActivityAddition = new UserActivityAddition(reqSet, userActivityService,
					ActivityLoggerConstants.DATABASE_MSSQL);
			new Thread(userActivityAddition).run();

			UserActivity userActivity = userActivityAddition.extractUserActivity();

			JSONObject jsonObject = new JSONObject(requestSet);
			JSONObject data = (JSONObject) jsonObject.get(JsonConstants.DATA);
			JSONObject user = (JSONObject) jsonObject.get(JsonConstants.SYSTEM_USER);
			String userName = user.get(JsonConstants.USERNAME) == null ? "" : (String) user.get(JsonConstants.USERNAME);

			SystemUser systemUserNew = ServerUtilities.extractSystemUser(user);

			log.info("-----systemUserNew-----" + systemUserNew.toString());

			Map reqMap = Utilities.createMapFromJSON(requestSet);

			pddCollId = data.get(JsonConstants.PddJSONConstants.PDD_COLL_ID) == null ? "" : (String) data
					.get(JsonConstants.PddJSONConstants.PDD_COLL_ID);

			if (pddCollId.equalsIgnoreCase(""))
			{

				status = JsonConstants.FAILURE;
				returnMessage = "pddCollId is not available For Case";
				return responseBuilder(message, status, returnMessage, "");
			}
			else
			{

				pddCollection.setPddCollId(pddCollId);

				imageDetails = data.getJSONArray(JsonConstants.PddJSONConstants.IMAGES);


				log.info("imagelength" + imageDetails.length());

				images = getImages(systemUserNew, imageDetails, pddCollId);
				if (images == null)
				{

					status = JsonConstants.FAILURE;
					returnMessage = "Image Not Found";
					return responseBuilder(message, status, returnMessage, "");
				}
				else
				{
					for (Image image : images)
					{

						String path = image.getPath();
						String type = image.getType();
						String id = image.getPddCollId();
						imageName = image.getImageName();
						String deviceImgStamp = image.getDescription(); 
						

						String dbPath = id + "/" + type + "/" + imageName;

						submissionFlag = pddService.submitPddCollectionImage(dbPath, type, id, userName, imageName ,deviceImgStamp);
						log.info("-----submissionFlag-----" + submissionFlag);

					}

					status = JsonConstants.SUCCESS;

					UserActivityStatusUpdate userActivityStatusUpdate = new UserActivityStatusUpdate(userActivity,
							(ActivityLoggerConstants.STATUS_SUCCESS), userActivityService);
					new Thread(userActivityStatusUpdate).run();

					return responseBuilder(message, status, "PddCollection image got submitted successfully", imageName);

				}
			}

		}
		catch (Exception e)
		{

			log.error("---Exception in image submission service -----", e);

			return responseBuilder(message, status, "Some error has occured", imageName);
		}

	}

	private List<Image> getImages(SystemUser systemUser, JSONArray imageDetails, String pddCollId) throws JSONException
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

			String imageName = (String) imageDetail.get(JsonConstants.PddJSONConstants.IMAGE_NAME);
			String imageType = (String) imageDetail.get(JsonConstants.PddJSONConstants.IMAGE_TYPE);
			String imageByteArray = (String) imageDetail.get(JsonConstants.PddJSONConstants.IMAGE_STRING);			
			String deviceImgStamp ="";
			
			if(imageDetail.has("deviceImgStamp"))
			{
				deviceImgStamp = (String) imageDetail.get("deviceImgStamp");
			}

			log.info("deviceImgStamp ::" + deviceImgStamp);
			log.info("imageName ::" + imageName);			
			log.info("imageType ::" + imageType);

			if (imageByteArray.isEmpty())
			{

				return new ArrayList<Image>();
			}
			image = new Image();

			imagePath = (extractImagePath(pddCollId, imageName, imageByteArray, (String.valueOf(index)), imageType ,deviceImgStamp));

			log.info("--imagePath1----" + imagePath);

			if (imagePath.equals(JsonConstants.ERROR))
			{

				return null;
			}
			else
			{

				image = new Image();
				image.setPath(imagePath);
				image.setType(imageType);
				image.setPddCollId(pddCollId);
				image.setImageName(imageName);
				image.setDescription(deviceImgStamp); //no need to change

				Utilities.primaryBeanSetter(image, systemUser);

				images.add(image);

			}
		}
		return images;
	}

	private String extractImagePath(String pddCollId, String imageName, String type, String index, String imageType,String deviceImgStamp)
	{
		try
		{

			String fileName = imageName;

			String filePath = Constants.EMPTY_STRING;

			filePath = generateFilePath(
					(String) applicationConfiguration.getValue(Constants.PddViewConstants.PDD_IMAGE_PATH), fileName,
					pddCollId, imageType);
			log.info(" config path :: "
					+ (String) applicationConfiguration.getValue(Constants.PddViewConstants.PDD_IMAGE_PATH));

			if (writeImage(filePath, type,deviceImgStamp))
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
			log.error("Exception while writing Pdd Image :: " , e);
			return (JsonConstants.ERROR);
		}
	}

	private Message<String> responseBuilder(Message<String> message, String status, String returnMessage,
			String imageName) throws JSONException
	{
		JSONObject responseJSON = new JSONObject();
		JSONObject data = new JSONObject();

		data.put("imageName", imageName);

		responseJSON.put(JsonConstants.STATUS, status);
		responseJSON.put(JsonConstants.MESSAGE, returnMessage);
		responseJSON.put(JsonConstants.DATA, data);

		return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
	}

	public static boolean writeImage(String filePath, String image ,String deviceImgStamp)
	{
		try
		{
			byte[] imageBytes = Base64.decodeBase64(toByteArray(image));

			File file = new File(filePath);

			OutputStream outputStream = new FileOutputStream(file);

			outputStream.write(imageBytes);
			outputStream.close();
			
			
			// added by bhushan
			
			
			try
			{

				BufferedImage img = ImageIO.read(new File(filePath));
				BufferedImage tempPNG = null;
				BufferedImage imgWithOutResize = null;
				File newFilePNG = null;
				if (img.getHeight() > 500 && img.getWidth() > 700)
				{
					log.info("image size greated than 500*700 ");
					tempPNG = resizeImage(img, 700, 500,deviceImgStamp);
					newFilePNG = new File(filePath);
					ImageIO.write(tempPNG, "png", newFilePNG);
				}
				else if (!deviceImgStamp.isEmpty() && deviceImgStamp != null)
				{
					log.info("image size less than 500*700 ");
					imgWithOutResize =applyDeviceStamp(img,img.getWidth(),img.getHeight(),deviceImgStamp);
					newFilePNG = new File(filePath);
					ImageIO.write(imgWithOutResize, "png", newFilePNG);
				}
			}
			catch (Exception e)
			{
				log.info("Exception in writeImage  "+e);
			}
			
			// added by bhushan till here 	
			return true;
		}
		catch (Exception e)
		{
			log.error("Exception in writeImage method :: " + e);
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

	public static String generateFilePath(String filePath, String fileName, String pddCollId, String imageType)
	{
		String str = filePath + pddCollId;
		if (!(Utilities.validateDirectoryExistence(str)))
		{
			System.out.println("Testing ---");
			log.info("Is created ::" + new File(str).mkdir());
		}

		str = str + "/" + imageType; // for local
		//str = str + "\\" + imageType; // for Prod
		if (!(Utilities.validateDirectoryExistence(str)))
		{
			log.info("Is created ::" + new File(str).mkdir());
		}

		filePath = str + "/" + fileName; // for local	
		//filePath = str+ "\\" + fileName;	// for Prod
		log.info("image path " + filePath);
		//log.info("----inside loop ----");

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
	
	public static BufferedImage resizeImage(final BufferedImage image, int width, int height, String deviceImgStamp)
	{
		//deviceImgStamp ="originaly verified by bhushan on date 31-nov-2018  \n pdd:121:pdd Cv21455 \n collected from collection app ";
		final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		final Graphics2D graphics2D = bufferedImage.createGraphics();
		graphics2D.setComposite(AlphaComposite.Src);
		//below three lines are for RenderingHints for better image quality at cost of higher processing time
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics2D.drawImage(image, 0, 0, width, height, null);
		if (!deviceImgStamp.isEmpty() && deviceImgStamp != null)
		{
			String[] strArray = deviceImgStamp.split("\n");  
			String strFirst=strArray[0];
			String strSecond=strArray[1];
			String strThird = strArray[2];
			log.info("strFirst  :: " + strFirst);
			log.info("strSecond :: " + strSecond);
			log.info("strThird  :: " + strThird); 
			
			
			Font font = new Font("Arial", Font.BOLD, Math.round(30));
			graphics2D.setColor(Color.BLACK);
			
			graphics2D.drawString(strFirst.trim(), 5, 450);
			graphics2D.drawString(strSecond.trim(), 5, 470);
			graphics2D.drawString(strThird.trim(), 5, 490);

		}
		graphics2D.dispose();
		return bufferedImage;
	}
	
	public static BufferedImage applyDeviceStamp(final BufferedImage image, int width, int height, String deviceImgStamp)
	{
		
		//deviceImgStamp ="originaly verified by bhushan on date 31-nov-2018  \n pdd:121:pdd Cv21455 \n collected from collection app ";
		final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		final Graphics2D graphics2D = bufferedImage.createGraphics();
		graphics2D.setComposite(AlphaComposite.Src);
		//below three lines are for RenderingHints for better image quality at cost of higher processing time
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics2D.drawImage(image, 0, 0, width, height, null);
		if (!deviceImgStamp.isEmpty() && deviceImgStamp != null)
		{
			
			String[] strArray = deviceImgStamp.split("\n");  
			String strFirst=strArray[0];
			String strSecond=strArray[1];
			String strThird = strArray[2];
			log.info("strFirst  :: " + strFirst);
			log.info("strSecond :: " + strSecond);
			log.info("strThird  :: " + strThird); 
			
			
			Font font = new Font("Arial", Font.BOLD, Math.round(30));
			graphics2D.setColor(Color.BLACK);
			
			graphics2D.drawString(strFirst.trim(), 5, height - 50);
			graphics2D.drawString(strSecond.trim(), 5, height - 30);
			graphics2D.drawString(strThird.trim(), 5, height - 10);			
		}
		graphics2D.dispose();
		return bufferedImage;
	}
}
