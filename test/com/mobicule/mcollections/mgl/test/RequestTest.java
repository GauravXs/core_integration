package com.mobicule.mcollections.mgl.test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

/**
 * 
 * <enter description here>
 *
 * @author prabhakar <enter lastname>
 * @see
 *
 * @createdOn 19-Nov-2015
 * @modifiedOn
 *
 * @copyright © 2008-2009 Mobicule Technologies Pvt. Ltd. All rights reserved.
 */
public class RequestTest
{
	/*private final static String NAMESPACE = "http://tempuri.org/";
	private final static String URL = "http://ttavatar.iifl.in/CommercialloanWCF/CommercialLoan.svc";
	private final static String SOAP_ACTION = "http://tempuri.org/CommercialLoan/GetProspectDetails";
	private final static String METHOD_NAME = "GetProspectDetails";*/
	static Scanner scan = new Scanner(System.in);

	public static String getWS(String URL, String request)
	{
		String outputString = "";
		try
		{
			// Code to make a webservice HTTP request
			String responseString = "";

			// String wsURL =
			// "http://www.deeptraining.com/webservices/weather.asmx";
			URL url = new URL(URL);
			URLConnection connection = url.openConnection();
			HttpURLConnection httpConn = (HttpURLConnection) connection;
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			/*String xmlInput1 = " <soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://litwinconsulting.com/webservices/\">\n"
					+ " <soapenv:Header/>\n"
					+ " <soapenv:Body>\n"
					+ " <web:GetProspectDetails>\n"
					+ " <!--Optional:-->\n"
					+ " <web:prospectNo>1000067581</web:prospectNo>\n"
					+ " </web:GetProspectDetails>\n"
					+ " </soapenv:Body>\n"
					+ " </soapenv:Envelope>";
			
			String xmlInput="<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\">"
			+"<soapenv:Header/>"
			+"<soapenv:Body>"
			+"<tem:GetProspectDetails>"
			        +"<!-Optional:->"
			        +"<tem:prospectno>1000067581</tem:prospectno>"
			        +"<!-Optional:->"
			        +"<tem:Name>Mobicule</tem:Name>"
			     +"</tem:GetProspectDetails>"
			  +"</soapenv:Body>"
			+"</soapenv:Envelope>";*/

			byte[] buffer = new byte[request.length()];
			buffer = request.getBytes();
			bout.write(buffer);
			byte[] b = bout.toByteArray();
			// String SOAPAction
			// ="http://litwinconsulting.com/webservices/GetWeather";
			// Set the appropriate HTTP parameters.
			//httpConn.setRequestProperty("Content-Length", String.valueOf(b.length));
			//String requestDigest = convertToSHA2(request + "kotak2016");
			//connection.setRequestProperty("digest", requestDigest);

			/*httpConn.setRequestProperty("Content-Type", "application/json");
			httpConn.setRequestProperty("Authorization",
					"Basic 5UBIuAyqN0xR1h99WbaG2H2fWaYBETh+s75drMjFi93Vq5eBpwSEEoh6E3ZB5EkT");
			connection.setRequestProperty("digest", "");*/

			//httpConn.setRequestProperty("SOAPAction", SOAP_ACTION);
			httpConn.setRequestProperty("Content-Type", "application/xml");
			httpConn.setRequestMethod("POST");
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);
			OutputStream out = httpConn.getOutputStream();
			// Write the content of the request to the outputstream of the HTTP
			// Connection.
			out.write(b);
			out.close();
			// Ready with sending the request.

			// Read the response.
			InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
			BufferedReader in = new BufferedReader(isr);

			// Write the SOAP message response to a String.
			while ((responseString = in.readLine()) != null)
			{
				outputString = outputString + responseString;
			}

			/*
			 * //Parse the String output to a org.w3c.dom.Document and be able
			 * to reach every node with the org.w3c.dom API. Document document =
			 * parseXmlFile(outputString); NodeList nodeLst =
			 * document.getElementsByTagName("GetWeatherResult"); String
			 * weatherResult = nodeLst.item(0).getTextContent();
			 * System.out.println("Weather: " + weatherResult);
			 * 
			 * //Write the SOAP message formatted to the console. String
			 * formattedSOAPResponse = formatXML(outputString);
			 * System.out.println(formattedSOAPResponse);
			 */

		}
		catch (Exception e)
		{

			e.printStackTrace();
			//MobiculeLogger.error(e.getMessage());
		}
		return outputString;
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public static String readFile() throws IOException
	{
		File file = new File("/home/prashant/Desktop/SMS");
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();

		String str = new String(data, "UTF-8");
		System.out.println("Request :" + str);
		return str;
	}

	public static int menuList()
	{
		System.out.println();
		System.out.println("1.Send Request");
		System.out.println("0.Exit");
		System.out.println();

		System.out.print("Enter Your Choice:::");
		return scan.nextInt();
	}

	public static void sendRequest()
	{
		try
		{
			//https://mcoll.kotak.com/mCollectionProductIntegration-PhaseII/RequestGateway
			//http://14.141.97.30:8085/mCollectionsKMIntegration-PhaseIII/RequestGateway
			//https://mcoll.kotak.com///mCollectionProductIntegration-PhaseII/RequestGateway
			//https://mcoll.kotak.com/kotak-lms-prod/KotakAuthenticationGateway
			//System.out.println("Response :"+SoapTest.getWS(" http://10.1.1.49:8084/mCollectionsKMIntegration-Phase2-Platform/RequestGateway", SoapTest.readFile()));

			System.out.println("Response :"
					+ RequestTest.getWS("http://14.141.97.30:8085/TibcoTest/CollectionCoreServlet",
							RequestTest.readFile()));
			//System.out.println("Response :"+SoapTest.getWS(" https://lms.kotak.com/mCollectionPrimeIntegration-PhaseII/RequestGateway", SoapTest.readFile()));

			//System.out.println("Response :"+SoapTest.getWS(" https://mcoll.kotak.com/kotak-lms-prod/KotakAuthenticationGateway", SoapTest.readFile()));
			//System.out.println("Response :"+SoapTest.getWS(" http://14.141.97.30:8085/mCollectionsKMIntegration-PhaseIII/RequestGateway", SoapTest.readFile()));
			//System.out.println("Response :"+SoapTest.getWS(" http://10.1.1.49:8084/mCollectionsKMIntegration-Phase2/RequestGateway", SoapTest.readFile()));
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{

		int choice = 0;
		while ((choice = menuList()) != 0)
		{
			switch (choice)
			{
				case 1:
					sendRequest();
					break;

				default:
					System.out.println("Please Enter 0 Or 1");
			}
		}

		/*		try
				{
					System.out.println("Response :"+SoapTest.getWS("https://mcoll.kotak.com/mCollectionProductIntegration-PhaseII/RequestGateway", SoapTest.readFile()));
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
	}

	public static final boolean checkDigest(String digest, String reqDigest)
	{
		if (!(reqDigest.isEmpty()))
		{
			return reqDigest.trim().equals(digest.trim()) ? true : false;
		}
		else
		{
			return false;
		}
	}

	public static String convertToSHA2(String value)
	{
		try
		{
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(value.getBytes("UTF-8"));

			return convertToHex(hash);
		}
		catch (UnsupportedEncodingException e)
		{
			//log.error("Exception in convertToSHA2() ==" + e.toString());
		}
		catch (NoSuchAlgorithmException e)
		{
			//log.error("Exception in convertToSHA2() ==" + e.toString());
		}
		return value;

	}

	private static final String convertToHex(byte[] data)
	{
		if (data == null || data.length == 0)
		{
			return null;
		}

		final StringBuffer buffer = new StringBuffer();
		for (int byteIndex = 0; byteIndex < data.length; byteIndex++)
		{
			int halfbyte = (data[byteIndex] >>> 4) & 0x0F;
			int two_halfs = 0;
			do
			{
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buffer.append((char) ('0' + halfbyte));
				else
					buffer.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[byteIndex] & 0x0F;
			}
			while (two_halfs++ < 1);
		}

		return buffer.toString();
	}

}