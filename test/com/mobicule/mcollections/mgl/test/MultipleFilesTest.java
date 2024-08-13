package com.mobicule.mcollections.mgl.test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MultipleFilesTest
{

	/**
	 * @param args
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException
	{
		String serverURL = "http://110.234.95.156:8080/mCollectionsMGLClientIntegration/";

		String smsUrl = "RequestGateway";
		String response;
		
		List<String> filesList = extractFileNamesAtDirectory("/home/jayesh/jayesh/ding/", ".txt");
		
		for (int i = 0; i < (filesList.size()); i++)
		{
			String path = "/home/jayesh/jayesh/ding/" + (String.valueOf(filesList.get(i)));
			
			System.out.println("File: " + path);
			
			response = sendRequest("", new URL(getUrl(serverURL, smsUrl, "", "", "", "")), path);

			System.out.println("RESPONSE is " + response);
		}
	}

	public static String getUrl(String serverUrl, String smsUrl, String longCode, String msisdn, String keyword,
			String param) throws MalformedURLException
	{

		System.out.println("Connecting to URl : " + new String(serverUrl + smsUrl));

		return new String(serverUrl + smsUrl);
	}

	public static String sendRequest(String request, URL url, String path)
	{
		String response = "";
		try
		{
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			//connection.setRequestProperty("Content-Length", String.valueOf(invoiceData.getBytes().length));

			DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());

			OutputStream outputStream = null;

			try
			{
				//dataOutputStream.writeUTF(invoiceData);

				outputStream = connection.getOutputStream();

				outputStream.write(inputRequest(path).getBytes());
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

	private static String inputRequest(String path)
	{
		StringBuilder contents = new StringBuilder();

		try
		{
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
	
	public static List<String> extractFileNamesAtDirectory(String directoryPath, String extension)
	{
		List<String> fileNamesList = new ArrayList<String>();

		File directory = new File(directoryPath);

		File[] filesList = directory.listFiles();

		for (int i = 0; i < (filesList.length); i++)
		{
			if (filesList[i].isFile())
			{
				String fileName = filesList[i].getName();

				if ((fileName.endsWith(extension.toUpperCase())) || (fileName.endsWith(extension.toLowerCase())))
				{
					fileNamesList.add(fileName);
				}
			}
		}

		return fileNamesList;
	}
}
