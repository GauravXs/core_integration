package com.mobicule.mcollections.integration.report;

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
import com.mobicule.mcollections.core.beans.FeedbackSnapshot;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.beans.VisitIntensity;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.ReportService;

public class VisitIntensityReportService implements IVisitIntensityReportService
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
		log.info(
				"<---------------------------- Inside VisitIntensityReportService / execute ----------------------------->");
		JSONObject responseJSON = new JSONObject();
		Map<String, Object> responseMap = new HashMap<String, Object>();
		List<Map<String, Object>> DailyStatusCollectionReportMap = new ArrayList<Map<String, Object>>();
		Map<String, Object> reportData = new HashMap<String, Object>();

		try
		{
			String messagePayload = message.getPayload();

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

					HashMap<String, String> parameter = new HashMap<>();

					parameter.put("fromDate", fromDate);

					parameter.put("toDate", toDate);

					parameter.put("username", username);

					List<VisitIntensity> visitIntensity = reportService.getvisitIntensityReport(parameter);

					if (visitIntensity != null && visitIntensity.size() > 0)
					{
						reportData.put("actionCases", visitIntensity.get(0).getActionCases());
						reportData.put("visitCount", visitIntensity.get(0).getVisitCount());
						reportData.put("totalAmount", visitIntensity.get(0).getTotalAmnt());
						reportData.put("allocatedCount", visitIntensity.get(0).getAllocCount());
						reportData.put("unattemptedCount", Integer.parseInt(visitIntensity.get(0).getAllocCount().toString())-Integer.parseInt(visitIntensity.get(0).getActionCases().toString()));

						responseJSON.put((JsonConstants.Key.DATA), MapToJSON.convertMapToJSON(reportData));

						responseJSON.put((JsonConstants.Key.STATUS), (JsonConstants.Value.STATUS_SUCCESS));
						responseJSON.put((JsonConstants.Key.MESSAGE), (JsonConstants.Value.MESSAGE_SUCCESS));
					}
					else
					{

						responseJSON.put((JsonConstants.Key.DATA), MapToJSON.convertMapToJSON(reportData));

						responseJSON.put((JsonConstants.Key.STATUS), (JsonConstants.Value.STATUS_FAILURE));
						responseJSON.put((JsonConstants.Key.MESSAGE), (JsonConstants.Value.MESSAGE_SUCCESS));

					}
				}
				else
				{
					log.info("<--------------- null user/data request ------------------->");
					responseMap = Utilities.generateReponseMap(JsonConstants.FAILURE,
							JsonConstants.MESSAGE_GENERAL_FAILURE, DailyStatusCollectionReportMap);
				}

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
