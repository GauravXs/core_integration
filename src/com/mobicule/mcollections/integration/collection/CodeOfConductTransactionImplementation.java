package com.mobicule.mcollections.integration.collection;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;

import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.ICodeOfConductService;
import com.mobicule.mcollections.integration.commons.ServerUtilities;

public class CodeOfConductTransactionImplementation implements ICodeofConductTransactionImplementation 
{
	private Logger log = LoggerFactory.getLogger(getClass());
	
	
	
	@Autowired
	private ICodeOfConductService  codeOfConductServic;
	
	
	public ICodeOfConductService getCodeOfConductServic() {
		return codeOfConductServic;
	}

	public void setCodeOfConductServic(ICodeOfConductService codeOfConductServic) {
		this.codeOfConductServic = codeOfConductServic;
	}



	@Autowired
	private ApplicationConfiguration<String, String> applicationConfiguration;

	
	public ApplicationConfiguration<String, String> getApplicationConfiguration() {
		return applicationConfiguration;
	}

	public void setApplicationConfiguration(ApplicationConfiguration<String, String> applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
	}

	

	@Override
	public Message<String> execute(Message<String> message) throws Throwable
	{
		log.info(" -------- In CodeOfConductTransactionImplementation execute Method -------- ");
		
		HashMap dataMap = new HashMap();
		Map<String,Object> responseMap = new HashMap<String, Object>();

		try
		{
			String requestSet = message.getPayload();
			JSONObject jsonObject = new JSONObject(requestSet);
			JSONObject data = (JSONObject) jsonObject.get(JsonConstants.DATA);
			JSONObject user = (JSONObject) jsonObject.get(JsonConstants.SYSTEM_USER);
			SystemUser systemUser = ServerUtilities.extractSystemUser(user);
			String isCodeOfConductDone="N";
			
		    Date date = Calendar.getInstance().getTime();  
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");  
            String currentDate = dateFormat.format(date);  
			
            String []splitDate=currentDate.split("-");
            
			Map<String,String> fetchUserDataParameter = new HashMap<String, String>();
			
			fetchUserDataParameter.put(Constants.USER_ID, systemUser.getUserTableId() == null ? "" : systemUser.getUserTableId().toString() ); 
			fetchUserDataParameter.put(Constants.USERNAME, systemUser.getUsername() == null ? "" : systemUser.getUsername());			
			
			//Map<String,Object> fetchUserDetails = generateOTPService.fetchUserDetails(fetchUserDataParameter);
			Map<String,Object> fetchUserCodeOfConductDetails = codeOfConductServic.fetchUserDetails(fetchUserDataParameter);
		
			if(!fetchUserCodeOfConductDetails.isEmpty()) 
			{
				String codeOfConduct15 = fetchUserCodeOfConductDetails.get("code_of_conduct_15") == null ? Constants.EMPTY_STRING: fetchUserCodeOfConductDetails.get("code_of_conduct_15").toString();
				
				String codeOfConduct30 = fetchUserCodeOfConductDetails.get("code_of_conduct_30") == null ? Constants.EMPTY_STRING: fetchUserCodeOfConductDetails.get("code_of_conduct_30").toString();
						
				if((codeOfConduct15 == null || codeOfConduct15.equalsIgnoreCase("")) && (codeOfConduct30 == null || codeOfConduct30.equalsIgnoreCase("")))
				{
					isCodeOfConductDone="N";
				}
				else if(splitDate[0]!=null && !splitDate[0].equalsIgnoreCase("") && Integer.parseInt(splitDate[0])!=0 && Integer.parseInt(splitDate[0])>=15 && Integer.parseInt(splitDate[0])<=30)
				{
					if(codeOfConduct15 == null || codeOfConduct15.equalsIgnoreCase(""))
					{
						isCodeOfConductDone="N";
					}
					else
					{
						isCodeOfConductDone="Y";
					}
				}
				else
				{
					if(codeOfConduct30 == null || codeOfConduct30.equalsIgnoreCase(""))
					{
						isCodeOfConductDone="N";
					}
					else
					{
						isCodeOfConductDone="Y";
					}
				}
				
				
					dataMap.put("isCodeOfConductDone", isCodeOfConductDone);
			
					responseMap.put(Constants.MESSAGE, "Code of Conduct Status");
					responseMap.put(Constants.JSON_RESPONSE_STATUS, "Success");
					responseMap.put(Constants.DATA_MAP, dataMap);
					return Utilities.deviceResponse(message,responseMap);
				
			}
			else
			{
				responseMap.put(Constants.MESSAGE, "No data found");
				responseMap.put(Constants.JSON_RESPONSE_STATUS, "Success");
				responseMap.put(Constants.DATA_MAP, dataMap);
				return Utilities.deviceResponse(message,responseMap);
			}
		}
		catch (Exception e)
		{

			log.error("---- Exception in code of conduct Detail ----:-"+e);

		}
		
		responseMap.put(Constants.MESSAGE, "No data found");
		responseMap.put(Constants.JSON_RESPONSE_STATUS, "Success");
		responseMap.put(Constants.DATA_MAP, dataMap);
		
		
		return Utilities.deviceResponse(message,responseMap);

	}
	
	
	
	public Message<String> submitCodeOfConduct(Message<String> message) throws Throwable
	{
		log.info(" -------- In CodeOfConductTransactionImplementation updateCodeOfConduct Method -------- ");
		
		HashMap dataMap = new HashMap();
		Map<String,Object> responseMap = new HashMap<String, Object>();

		try
		{
			String requestSet = message.getPayload();
			JSONObject jsonObject = new JSONObject(requestSet);
			JSONObject data = (JSONObject) jsonObject.get(JsonConstants.DATA);
			JSONObject user = (JSONObject) jsonObject.get(JsonConstants.SYSTEM_USER);
			SystemUser systemUser = ServerUtilities.extractSystemUser(user);
			
			
		    Date date = Calendar.getInstance().getTime();  
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");  
            String currentDate = dateFormat.format(date);  
			
            String []splitDate=currentDate.split("-");
            
			Map<String,String> fetchUserDataParameter = new HashMap<String, String>();
			
			fetchUserDataParameter.put(Constants.USER_ID, systemUser.getUserTableId() == null ? "" : systemUser.getUserTableId().toString() ); 
			fetchUserDataParameter.put(Constants.USERNAME, systemUser.getUsername() == null ? "" : systemUser.getUsername());
			fetchUserDataParameter.put("currentDate", splitDate[0] == null ? "" : splitDate[0]);
			
			
			
				boolean flag=codeOfConductServic.updateCodeOfConductForUser(fetchUserDataParameter);
				
				if(flag)
				{
					dataMap.put("isCodeOfConductDone", "Y");
					
					responseMap.put(Constants.MESSAGE, "Code of Conduct Submitted");
					responseMap.put(Constants.JSON_RESPONSE_STATUS, "Success");
					responseMap.put(Constants.DATA_MAP, dataMap);
					return Utilities.deviceResponse(message,responseMap);
				}
				else
				{
					dataMap.put("isCodeOfConductDone", "N");
					
					responseMap.put(Constants.MESSAGE, "Code of Conduct not Submitted");
					responseMap.put(Constants.JSON_RESPONSE_STATUS, "Failure");
					responseMap.put(Constants.DATA_MAP, dataMap);
					return Utilities.deviceResponse(message,responseMap);
				}
			}
			catch(Exception e)
		{
				log.info("Exception occured while submitting code of conduct::"+e);
		}
		responseMap.put(Constants.MESSAGE, "Code of Conduct not Submitted");
		responseMap.put(Constants.JSON_RESPONSE_STATUS, "Failure");
		responseMap.put(Constants.DATA_MAP, dataMap);
		return Utilities.deviceResponse(message,responseMap);
	}
			
	
}
