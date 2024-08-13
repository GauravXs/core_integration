package com.mobicule.mcollections.integration.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadCibilPDF extends HttpServlet
{
	Logger log = LoggerFactory.getLogger(this.getClass());

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException
	{
		log.info("<----------------------------- inside Download CIBIL PDF ----------------------------->");
		try
		{

			String filePath = request.getParameter("filePath");
			log.info("------- filePath -------" + filePath);
			String fileName = "";
			
			//fileName = filePath.substring(filePath.lastIndexOf("/")+1); //local
			fileName = filePath.substring(filePath.lastIndexOf("\\")+1); //uat & Prod
			
			log.info("------- fileName ------" + fileName);

			FileInputStream fileInputStream = null;
			fileInputStream = new FileInputStream(new File(filePath));

			response.setContentType("application/pdf");
			response.addHeader("Content-Disposition", "inline; filename=" + fileName);

			int b = 0;

			try
			{
				while ((b = fileInputStream.read()) != -1)
				{
					response.getOutputStream().write(b);
				}
			}
			catch (IOException e)
			{
				log.error("--- exception ---"+e);
			}

		}
		catch (Exception e)
		{
			log.error("<------------------- exception while downloading pdf file -------------------------->");
			//e.printStackTrace();
			log.error("--- exception ---"+e.getMessage());
			log.error("--- exception ---"+e.getCause());
			log.error("--- exception ---"+e.getStackTrace());
		}
	}
}
