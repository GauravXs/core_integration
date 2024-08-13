package com.mobicule.mcollections.integration.systemuser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.integration.Message;

import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.integration.messaging.IService;

public interface ISystemUserAuthenticationService extends IService
{
	public String authenticateUser(Map userMap, SystemUser systemUser);

	boolean isResetBeforeFixedInterval(String userName);
	
	public boolean checkLockedStatus(SystemUser systemUser, boolean authentication);

	public HashMap createSuccessResponseData(SystemUser systemUser);

	public Message<String> generateResponse(Message<String> message, HashMap responseMap) throws IOException, JsonGenerationException, JsonMappingException;
	
	
	/**
	 * @author Shyam
	 */
	public Message<String> generateResponseForPasswordResetExpired(Message<String> message, HashMap responseMap) throws IOException, JsonGenerationException, JsonMappingException;
	
	boolean isFutureDateExpired(String userName);

	// for Rbl
	public String changeHandsetImei(Map<String, String> userMap);
	
	public String authenticateUser(Map userMap, SystemUser systemUser,String AndroidTen);
	
	public String authenticateUser(SystemUser systemUser);
}
