package com.mobicule.mcollections.integration.collection;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;

import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.beans.TwilioResponse;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.IExotelCallService;
import com.mobicule.mcollections.integration.commons.ServerUtilities;

import okhttp3.Credentials;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ExotelCallTransactionImplementation implements IExotelCallTransactionImplementation 
{
	private Logger log = LoggerFactory.getLogger(getClass());
	
	
	
	@Autowired
	private IExotelCallService  exotelCallServic;
	
	
	public IExotelCallService getExotelCallServic() {
		return exotelCallServic;
	}

	public void setExotelCallServic(IExotelCallService exotelCallServic) {
		this.exotelCallServic = exotelCallServic;
	}



	@Autowired
	private ApplicationConfiguration<String, Object> applicationConfiguration;



	public ApplicationConfiguration<String, Object> getApplicationConfiguration() {
		return applicationConfiguration;
	}

	public void setApplicationConfiguration(ApplicationConfiguration<String, Object> applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
	}

	@Override
	public Message<String> exotelCallInitateMethod(Message<String> message) throws Throwable
	{
		log.info(" -------- In ExotelCallTransactionImplementation exotelCallInitateMethod Method -------- ");
		
		HashMap dataMap = new HashMap();
		Map<String,Object> responseMap = new HashMap<String, Object>();
		 String xmlResponseFromExotelForCallInitate="";

		try
		{
			String requestSet = message.getPayload();
			JSONObject jsonObject = new JSONObject(requestSet);
			JSONObject data = (JSONObject) jsonObject.get(JsonConstants.DATA);
			JSONObject user = (JSONObject) jsonObject.get(JsonConstants.SYSTEM_USER);
			SystemUser systemUser = ServerUtilities.extractSystemUser(user);
			boolean flag=false;
			
			///////////exotel call initate start
			
			if(data !=null)
			{
				Map<String, String> exotelDetails =  (Map<String, String>) applicationConfiguration.getValue("EXOTEL_DETAILS");
				 String exotelSid     = exotelDetails.get("EXOTEL_SID");//rblbank1m
				 String exotelApiKey         = exotelDetails.get("EXOTEL_API_KEY");//32f46e9f85a804defe55954bf785ba56bd561cb7d481fbc0
				 String exotelApitoken      = exotelDetails.get("EXOTEL_API_TOKEN");//d89e8f7914f37ea19b8504afccff358fa289d4ddffb81660
				 
				String exotelURL="https://"+exotelApiKey+":"+exotelApitoken+"@api.in.exotel.com/v1/Accounts/"+exotelSid+"/Calls/connect";
				 TwilioResponse twilioResponse=null;
				 Map<String,Object> parameterMap=new HashMap<String, Object>();
				String accountNumber=data.getString("accountNumber")==null?"":data.getString("accountNumber");
				String agencyName=data.getString("agencyName")==null?"":data.getString("agencyName");
				String fosName=data.getString("fosName")==null?"":data.getString("fosName");
				String fosId=data.getString("fosId")==null?"":data.getString("fosId");
				String fosMobile=data.getString("fosMobile")==null?"":data.getString("fosMobile");
				String customerName=data.getString("customerName")==null?"":data.getString("customerName");
				String customerId=data.getString("customerId")==null?"":data.getString("customerId");
				String customerMobile=data.getString("customerMobile")==null?"":data.getString("customerMobile");
				String latitude=data.getString("latitude")==null?"":data.getString("latitude");
				String longitude=data.getString("longitude")==null?"":data.getString("longitude");
				
				OkHttpClient client = new OkHttpClient().newBuilder().build();
				RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
						.addFormDataPart("From", fosMobile)
						.addFormDataPart("To"  , customerMobile ).build();
				
				//String credentials = Credentials.basic("32f46e9f85a804defe55954bf785ba56bd561cb7d481fbc0", "d89e8f7914f37ea19b8504afccff358fa289d4ddffb81660");
				String credentials = Credentials.basic(exotelApiKey,exotelApitoken);
				Request request = new Request.Builder()
						  //.url("https://32f46e9f85a804defe55954bf785ba56bd561cb7d481fbc0:d89e8f7914f37ea19b8504afccff358fa289d4ddffb81660@api.in.exotel.com/v1/Accounts/rblbank1m/Calls/connect")
						  .url(exotelURL)
						  .post(body)
						  .addHeader("Authorization", credentials)
						 // .addHeader("Authorization", "")
						  .addHeader("content-type", "multipart/form-data")
						  .build();
				log.info("exotel call intiate url :: "+exotelURL);
				log.info("exotel call intiate request :: "+request.toString());
				
				try {
					Response response = client.newCall(request).execute();
					
					if(response!=null)
					{
						InputStream is=response.body().byteStream();
				        ByteArrayOutputStream result = new ByteArrayOutputStream();
				        byte[] buffer = new byte[8192];
				        int length;
				        while ((length = is.read(buffer)) != -1) {
				            result.write(buffer, 0, length);
				        }

				       
				        log.info( "exotel call intiate response ::"+result.toString(StandardCharsets.UTF_8.name()));
				        xmlResponseFromExotelForCallInitate=result.toString(StandardCharsets.UTF_8.name());
				        
				        JAXBContext jaxbContext;
				        try
				        {
				            jaxbContext = JAXBContext.newInstance(TwilioResponse.class);              
				         
				            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				         
				             twilioResponse = (TwilioResponse) jaxbUnmarshaller.unmarshal(new StringReader(xmlResponseFromExotelForCallInitate));
				             
				            log.info("TwilioResponse ::"+twilioResponse.toString());
				            if(twilioResponse!=null && twilioResponse.getCall()!=null )
				            {
				            	flag=true;
				            }
				            else
				            {
				            	flag=false;
				            }
				        }
				        catch (JAXBException e) 
				        {
				        	 flag=false;
				        	log.info("Exception while parsing exotel call intiate response to TwilioResponse:"+e);
				            e.printStackTrace();
				        }
				       

					}
					else
					{
						log.info( "exotel call intiate response is null");
					}
								        
			      
					//added call intiate details in exotel call detail table
					
					parameterMap.put("accountNumber", accountNumber);
					parameterMap.put("agencyName", agencyName);
					parameterMap.put("fosName", fosName);
					parameterMap.put("fosId", fosId);
					parameterMap.put("fosMobile", fosMobile);
					parameterMap.put("customerName", customerName);
					parameterMap.put("customerId", customerId);
					parameterMap.put("customerMobile",customerMobile);
					parameterMap.put("latitude", latitude);
					parameterMap.put("longitude", longitude);
					parameterMap.put("twilioResponse",twilioResponse);
					parameterMap.put("xmlResponseFromExotelForCallInitate",xmlResponseFromExotelForCallInitate);
					
					boolean flagInsert=exotelCallServic.insertExotelCallIntiateDetails(parameterMap);
					if(flagInsert)
					{
						log.info("data insert in exotel table :: "+flagInsert);
					}
						
					if(flag)
					{
						responseMap.put(Constants.MESSAGE, "Call Initated");
						responseMap.put(Constants.JSON_RESPONSE_STATUS, "Success");
						responseMap.put(Constants.DATA_MAP, dataMap);
						return Utilities.deviceResponse(message,responseMap); 
						
						
					}
					else
					{
						responseMap.put(Constants.MESSAGE, "No call initated");
						responseMap.put(Constants.JSON_RESPONSE_STATUS, "Failure");
						responseMap.put(Constants.DATA_MAP, dataMap);
						return Utilities.deviceResponse(message,responseMap);
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}

				

				
			}
						
			/////////exotel call initate end
			
			
		}
		catch (Exception e)
		{

			log.error("---- Exception in exotel call intialisation ----:-"+e);

		}
		
		responseMap.put(Constants.MESSAGE, "No call intiated");
		responseMap.put(Constants.JSON_RESPONSE_STATUS, "Failure");
		responseMap.put(Constants.DATA_MAP, dataMap);
		
		
		return Utilities.deviceResponse(message,responseMap);

	}
	
	
	
				
	
}
