package com.mobicule.mcollections.integration.report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.mobicule.component.mapconversion.json.MapToJSON;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.ReportService;

public class CollectionActivityReportService implements ICollectionActivityReportService
{
	Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	ApplicationConfiguration applicationConfiguration;

	private ReportService reportService;

	public ReportService getReportService()
	{
		return reportService;
	}

	public void setReportService(ReportService reportService)
	{
		this.reportService = reportService;
	}

	@Override
	public Message<String> execute(Message<String> message)
	{
		log.info("<---------------------------- Inside Collection Activity   ----------------------------->");
		JSONObject responseJSON = new JSONObject();
		Map<String, Object> responseMap = new HashMap<String, Object>();
		List<Map<String, Object>> collActivityReportMap = new ArrayList<Map<String, Object>>();
		List<String> collActivityReportDBHeader = (List<String>) applicationConfiguration
				.getValue("COLLECTION_ACTIVITY_REPORT_DB_COL");

		Map<String, String> collActivityReportJsonHeader = (Map<String, String>) applicationConfiguration
				.getValue("COLLECTION_ACTIVITY_REPORT_JSON_KEY");

		try
		{
			String messagePayload = message.getPayload();

			if (messagePayload.equals(Constants.EMPTY_STRING))
			{
				log.info("<----------------------- Empty Request message ----------------------->");
			}
			else
			{

				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, -90); // I just want date before 90 days. you can give that you want.

				SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); // you can specify your format here...

				String fromDate = s.format(new Date(cal.getTimeInMillis()));

				String data = JSONPayloadExtractor.extract(messagePayload, JsonConstants.DATA);
				String userData = JSONPayloadExtractor.extract(messagePayload, JsonConstants.SYSTEM_USER);
				String userId = JSONPayloadExtractor.extract(userData, JsonConstants.SYSTEM_USER_ID);
				//String fromDate = JSONPayloadExtractor.extract(data, JsonConstants.FROM_DATE) + " 00:00:00";
				String toDate = JSONPayloadExtractor.extract(data, JsonConstants.TO_DATE) + " 23:59:59";
				String portFolio = JSONPayloadExtractor.extract(data, JsonConstants.PORT_FOLIO);
				SystemUser user = new SystemUser();
				user.setUserTableId(Long.valueOf(userId));

				log.info("----fromDate----" + fromDate);

				if (data != null && userData != null)
				{

					collActivityReportMap = reportService.getCollectionActivityReport(user, portFolio, fromDate,
							toDate);

					log.info("---collActivityReportMap----" + collActivityReportMap);

					log.info("----collActivityReportDBHeader----" + collActivityReportDBHeader);

					log.info("-----collActivityReportJsonHeader-----" + collActivityReportJsonHeader);

					if ((collActivityReportMap == null || collActivityReportMap.equals(null)))
					{
						log.info("<---------------------- Null  Response --------------------->");
						responseMap = Utilities.generateReponseMap(JsonConstants.FAILURE,
								JsonConstants.MESSAGE_DAILY_VISIT_REPORT_FAILURE, collActivityReportMap);
					}
					else
					{
						collActivityReportMap = Utilities.eliminateNulls(collActivityReportMap);
						/*	collActivityReportMap = Utilities.eliminateUnwanted(collActivityReportMap,
									collActivityReportDBHeader);*/

						responseMap = Utilities.generateReponseMap(JsonConstants.SUCCESS,
								JsonConstants.MESSAGE_DAILY_VISIT_REPORT_SUCCESS, collActivityReportMap);
					}

				}
				else
				{
					log.info("<--------------- null user/data request ------------------->");
					responseMap = Utilities.generateReponseMap(JsonConstants.FAILURE,
							JsonConstants.MESSAGE_GENERAL_FAILURE, collActivityReportMap);
				}
				responseJSON = MapToJSON.convertMapToJSON(responseMap);
				
				//responseJSON = MapToJSON.convertMapToJSON(responseMap, collActivityReportJsonHeader);
			}

		}
		catch (Exception e)
		{

			log.error("exception detail----", e);

			responseMap = Utilities.generateReponseMap(JsonConstants.FAILURE, JsonConstants.MESSAGE_GENERAL_FAILURE,
					collActivityReportMap);
		}
		return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
	}

}
