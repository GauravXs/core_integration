package com.mobicule.mcollections.integration.messaging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.me.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessageHandlingException;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.mobicule.mcollections.core.commons.Utilities;
import com.sun.servicetag.UnauthorizedAccessException;

public class HttpInboundGatewayServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	private Logger log = LoggerFactory.getLogger(getClass());

	public HttpInboundGatewayServlet()
	{
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.getOutputStream().write(((String) "In HTTP Get").getBytes());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException
	{
		if (log.isInfoEnabled())
		{
			log.info("-----In doPost-----");
		}

		doSpringIntegrationHandling(request, response);
	}

	private void doSpringIntegrationHandling(HttpServletRequest request, HttpServletResponse response)
			throws IOException
	{
		ApplicationContext applicationContext = WebApplicationContextUtils
				.getRequiredWebApplicationContext(getServletContext());

		try
		{
		//	HttpSession session = request.getSession();
			String data = readBody(request);
			log.info("request -----" + data);

			readQueryString(request);

			//code added for digest start here

			String digest = request.getHeader("digest") != null ? request.getHeader("digest") : "";
			log.info("Device Digest : " + digest);
			String requestDigest = Utilities.convertToSHA2(data + "mkoobtiackule");
			log.info("Server Digest mkoobtiackule : " + requestDigest);
			String requestDigest1 = Utilities.convertToSHA2(data + "d59u#frt$fdk7Pr@");
			log.info("Server Digest1 d59u#frt$fdk7Pr@ : " + requestDigest1);
			String requestDigest2 = Utilities.convertToSHA2(data + "kotak2016");
			log.info("Server Digest2 kotak2016 : " + requestDigest2);
			
			if(data.contains("<")) 
			{	
				log.info("---- inside validity count to check clicking jacking ----");
				
				JSONObject responseJson = new JSONObject();
				
				responseJson.put("status", "failure");
				responseJson.put("message", "Invalid Request!!!");
				HashMap data1 = new HashMap();
				responseJson.put("data", data1);
				String responseString = responseJson.toString();
				
				MessageChannel deviceResponseChannel = (MessageChannel) applicationContext.getBean("deviceResponseChannel");
				MessageBuilder<?> messageBuilder = MessageBuilder.withPayload(responseString).setHeader("response", response);
				deviceResponseChannel.send(messageBuilder.build());
				
			}
			if (Utilities.checkDigest(digest, requestDigest))
			{
				log.info("both digest are same mkoobtiackule");

				MessageChannel requestChannel = (MessageChannel) applicationContext.getBean("deviceRequestChannel");

				
				
				MessageBuilder<?> messageBuilder = MessageBuilder.withPayload(data).setHeader("response", response)
						.setHeader("digestMatchStatus", "Yes");
				requestChannel.send(messageBuilder.build());
			}
			else if (Utilities.checkDigest(digest, requestDigest1))
			{
				log.info("both digest are same d59u#frt$fdk7Pr@");

				MessageChannel requestChannel = (MessageChannel) applicationContext.getBean("deviceRequestChannel");

				
					
				MessageBuilder<?> messageBuilder = MessageBuilder.withPayload(data).setHeader("response", response)
						.setHeader("digestMatchStatus", "Yes");
				requestChannel.send(messageBuilder.build());
			}
			else if (Utilities.checkDigest(digest, requestDigest2))
			{
				log.info("both digest are same kotak2016");

				MessageChannel requestChannel = (MessageChannel) applicationContext.getBean("deviceRequestChannel");

				
					
				MessageBuilder<?> messageBuilder = MessageBuilder.withPayload(data).setHeader("response", response)
						.setHeader("digestMatchStatus", "Yes");
				requestChannel.send(messageBuilder.build());
			}
			else
			{
				log.info("both digest are not same ");

				MessageChannel requestChannel = (MessageChannel) applicationContext.getBean("deviceRequestChannel");

				MessageBuilder<?> messageBuilder = MessageBuilder.withPayload(data).setHeader("response", response)
						.setHeader("digestMatchStatus", "No");
				requestChannel.send(messageBuilder.build());
			}
			//code added for digest ends here
		}
		catch (MessageHandlingException e)
		{
			Throwable exceptionCause = e.getCause();

			if (exceptionCause instanceof UnauthorizedAccessException)
			{
				log.error("UnauthorizedAccessException : ");

				response.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);

				Map<String, Object> errorMap = new HashMap<String, Object>();

				errorMap.put("status", "error");
				errorMap.put("message", "02:Authentication Error");
				errorMap.put("data", "");

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				new ObjectMapper().writeValue(baos, errorMap);

				/*MessageChannel deviceResponseChannel = (MessageChannel) applicationContext
						.getBean(SystemConstants.DEVICE_RESPONSE_CHANNEL);
				deviceResponseChannel.send(MessageBuilder.withPayload(baos.toString())
						.setHeader(SystemConstants.HTTP_RESPONSE, response).build());*/
			}
			else
			{
				sendGenericErrorMessage(response, applicationContext);
			}
		}
		catch (Exception e)
		{
			sendGenericErrorMessage(response, applicationContext);
		}
	}

	private void sendGenericErrorMessage(HttpServletResponse response, ApplicationContext applicationContext)
	{
		MessageChannel deviceResponseChannel = (MessageChannel) applicationContext.getBean("deviceResponseChannel");
		deviceResponseChannel.send(MessageBuilder.withPayload("error").build());
	}

	private Map<String, String> createHeaderMap(HttpServletRequest request)
	{
		Enumeration<String> headerNames = request.getHeaderNames();

		Map<String, String> headerMap = new HashMap<String, String>();

		while (headerNames.hasMoreElements())
		{
			String header = (String) headerNames.nextElement();
			headerMap.put(header, request.getHeader(header));
		}

		return headerMap;
	}

	private void readQueryString(HttpServletRequest request)
	{
		Enumeration<String> enumeration = request.getParameterNames();

		while (enumeration.hasMoreElements())
		{
			String parameterName = enumeration.nextElement();

			String parameterValue = request.getParameter(parameterName);

			if (log.isInfoEnabled())
			{
				log.info(parameterName + " : " + parameterValue);
			}
		}

		if (log.isInfoEnabled())
		{
			log.info("");
		}
	}

	private String readBody(HttpServletRequest request) throws IOException
	{
		InputStream is = request.getInputStream();

		ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();

		byte[] buf = new byte[1000];

		for (int nChunk = is.read(buf); nChunk != -1; nChunk = is.read(buf))
		{
			arrayOutputStream.write(buf, 0, nChunk);
		}

		if (log.isInfoEnabled())
		{
			log.info("");
		}

		String data = new String(arrayOutputStream.toByteArray(), "utf-8");

		return data;
	}
}
