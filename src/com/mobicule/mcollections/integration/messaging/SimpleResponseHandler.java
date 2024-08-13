package com.mobicule.mcollections.integration.messaging;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;

import com.mobicule.mcollections.core.commons.Utilities;

public class SimpleResponseHandler implements MessageHandler
{

	private Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void handleMessage(Message<?> message) throws MessagingException
	{
		log.info("--- In Response Handler ---");
		HttpServletResponse response = (HttpServletResponse) message.getHeaders().get("response");
		
		log.info("**** Decice Response **** " + message.getPayload());

		try
		{
			byte[] bytes = ((String) message.getPayload()).getBytes();

			int contentLength = bytes.length;

			String responseStr = (String) message.getPayload();
			
			log.info("responseStr " + responseStr);
			//String responseDigest = Utilities.convertToSHA2(responseStr + "d59u#frt$fdk7Pr@");
			String responseDigest = Utilities.convertToSHA2(responseStr + "mkoobtiackule");
			
			log.info("responseDigest " + responseDigest);
			
			response.setHeader("digest", responseDigest);
			response.setHeader("X-Frame-Options", "SAMEORIGIN"); // added for appsec
			response.setHeader("X-Content-Type-Options", "nosniff"); // added for appsec
			response.setHeader("X-XSS-Protection", "1; mode=block"); // added for appsec
			response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains"); // added for appsec
			response.setHeader("Content-Security-Policy", "self"); // added for appsec
			response.setContentLength(contentLength);
			response.getOutputStream().write(bytes);
		}
		catch (Exception e)
		{
			log.info("Exception :: " , e);
			throw new RuntimeException("Error while wring to response output stream.", e);
		}

	}
}
