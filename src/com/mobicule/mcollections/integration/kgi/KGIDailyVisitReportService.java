package com.mobicule.mcollections.integration.kgi;

import java.util.ArrayList;
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
import com.mobicule.mcollections.core.service.KGIServiceImplementation;


public class KGIDailyVisitReportService 
{

	Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	ApplicationConfiguration applicationConfiguration;
	
	
	KGIServiceImplementation kgiService;
	

	
	public KGIServiceImplementation getKgiService()
	{
		return kgiService;
	}



	public void setKgiService(KGIServiceImplementation kgiService)
	{
		this.kgiService = kgiService;
	}



	public Message<String> execute(Message<String> message)
	{
		log.info("<---------------------------- Inside DailyVisitReport / execute ----------------------------->");
		JSONObject responseJSON = new JSONObject();
		Map<String, Object> responseMap = new HashMap<String, Object>();
		List<Map<String, Object>> DailyStatusCollectionReportMap = new ArrayList<Map<String, Object>>();

		try
		{
			String messagePayload = message.getPayload();
			List<String> dailyStatusCollectionReportDBHeader = (List<String>) applicationConfiguration
					.getValue("KGI_DAILY_STATUS_REPORT_COLLECTION_DB_COLUMN");

			Map<String, String> dailyStatusReportJsonHeader = (Map<String, String>) applicationConfiguration
					.getValue("KGI_DAILY_STATUS_REPORT_JSON_KEY");

			if (messagePayload.equals(Constants.EMPTY_STRING))
			{
				log.info("<----------------------- Empty Request message ----------------------->");
			}
			else
			{
				String data = JSONPayloadExtractor.extract(messagePayload, JsonConstants.DATA);
				String userData = JSONPayloadExtractor.extract(messagePayload, JsonConstants.SYSTEM_USER);
				String username = JSONPayloadExtractor.extract(userData, JsonConstants.USERNAME);
				String fromDate = JSONPayloadExtractor.extract(data, JsonConstants.FROM_DATE) + " 00:00:00";
				String toDate = JSONPayloadExtractor.extract(data, JsonConstants.TO_DATE) + " 23:59:59";
				String portFolio = JSONPayloadExtractor.extract(data, JsonConstants.PORT_FOLIO);
				SystemUser user = new SystemUser();
				user.setUsername(username);

				if (data != null && userData != null)
				{

					DailyStatusCollectionReportMap = kgiService.getDailyVisitCollectionReport(user, portFolio,
							fromDate, toDate);

					if ((DailyStatusCollectionReportMap == null || DailyStatusCollectionReportMap.equals(null)))
					{
						log.info("<---------------------- Null  Response --------------------->");
						responseMap = Utilities.generateReponseMap(JsonConstants.FAILURE,
								JsonConstants.MESSAGE_DAILY_VISIT_REPORT_FAILURE, DailyStatusCollectionReportMap);
					}
					else
					{
						DailyStatusCollectionReportMap = Utilities.eliminateNulls(DailyStatusCollectionReportMap);
						DailyStatusCollectionReportMap = Utilities.eliminateUnwanted(DailyStatusCollectionReportMap,
								dailyStatusCollectionReportDBHeader);

						responseMap = Utilities.generateReponseMap(JsonConstants.SUCCESS,
								JsonConstants.MESSAGE_DAILY_VISIT_REPORT_SUCCESS, DailyStatusCollectionReportMap);
					}
				}
				else
				{
					log.info("<--------------- null user/data request ------------------->");
					responseMap = Utilities.generateReponseMap(JsonConstants.FAILURE,
							JsonConstants.MESSAGE_GENERAL_FAILURE, DailyStatusCollectionReportMap);
				}

				responseJSON = MapToJSON.convertMapToJSON(responseMap, dailyStatusReportJsonHeader);
			}

		}
		catch (Exception e)
		{
			log.info("<------------------ Exception while getting Daily Visit Report ------------------->");
			responseMap = Utilities.generateReponseMap(JsonConstants.FAILURE, JsonConstants.MESSAGE_GENERAL_FAILURE,
					DailyStatusCollectionReportMap);
		}
		return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
	}

}
