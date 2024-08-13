package com.mobicule.mcollections.mgl.test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Test
{

	/**
	 * @param args
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException
	{
		//String serverURL = "https://lms.kotak.com/mCollectionPrimeIntegration-PhaseII/";
		//String serverURL = "https://mcoll.kotak.com/mCollectionProductIntegration-PhaseII/";

		//String serverURL = "http://localhost:8080/mCollectionsKMIntegration-Phase2/";
		String serverURL = "https://mcoll.kotak.com/TibcoTest/CollectionCoreServlet";

		//	String serverURL = "http://localhost:8080/mCollectionsKMIntegration-Phase2/";

		String smsUrl = "";//RequestGateway
		String res;

		res = sendRequest("", new URL(getUrl(serverURL, smsUrl, "", "", "", "")));

		System.out.println("RESPONSE is " + res);

	}

	public static String getUrl(String serverUrl, String smsUrl, String longCode, String msisdn, String keyword,
			String param) throws MalformedURLException
	{

		System.out.println("Connecting to URl : " + new String(serverUrl + smsUrl));

		return new String(serverUrl + smsUrl);
	}

	public static String sendRequest(String request, URL url)
	{
		String response = "";
		try
		{
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "text/xml");
			/*connection.setRequestProperty("Authorization",
					"Basic cEIwUXJuS1VXa3o1MnhoK25TWjlVeVdKdVVKUzB4SWhMbU1JakY1Q1IyMD06");*/

			DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());

			OutputStream outputStream = null;

			try
			{
				//dataOutputStream.writeUTF(invoiceData);

				outputStream = connection.getOutputStream();

				outputStream.write(inputRequest().getBytes());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				outputStream.flush();
				outputStream.close();
			}

			InputStreamReader streamReader = null;

			try
			{
				ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();

				streamReader = new InputStreamReader(connection.getInputStream());

				int charRead;

				while ((charRead = streamReader.read()) != -1)
				{
					arrayOutputStream.write(charRead);
				}

				response = new String(arrayOutputStream.toByteArray());

				System.out.println("ResponseCode : " + connection.getResponseCode());
				System.out.println("ResponseMessage : " + connection.getResponseMessage());

				for (int i = 0;; i++)
				{
					String headerName = connection.getHeaderFieldKey(i);
					String headerValue = connection.getHeaderField(i);

					if (headerName == null && headerValue == null)
					{
						// No more headers
						break;
					}
					if (headerName == null)
					{
						System.out.println(headerValue);
					}
					else
					{
						System.out.println(headerName + " : " + headerValue);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				if (streamReader != null)
				{
					streamReader.close();
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return response;
	}

	private static String inputRequest()
	{
		StringBuilder contents = new StringBuilder();

		try
		{
			//String path = "/home/vikas/VIKASDOC/Kotak_Collection/API/login";
			//String path = "/home/vikas/VIKASDOC/Kotak_Collection/API/portfolioSync";
			String path = "/home/prashant/kotak/SMS Format";
			//String path = "/home/vikas/VIKASDOC/Kotak_Collection/API/randomsearch";

			FileReader fileReader = new FileReader(path);
			BufferedReader input = new BufferedReader(fileReader);

			String line = null;

			while ((line = input.readLine()) != null)
			{
				contents.append(line);
				contents.append(System.getProperty("line.separator"));
			}

			System.out.println("**Input Contents are " + contents.toString());
			String customData = new String(contents);

			return customData;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;

	}
}
