package com.mobicule.mcollections.integration.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.me.JSONArray;
import org.json.me.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;


import com.mobicule.component.mapconversion.json.MapToJSON;
import com.mobicule.mcollections.core.beans.FeedbackSnapshot;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.Constants;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.core.commons.Utilities;
import com.mobicule.mcollections.core.configurations.ApplicationConfiguration;
import com.mobicule.mcollections.core.service.ReportService;
import com.mobicule.mcollections.core.service.SyncService;

public class FeedbackSnapReportService implements IFeedbackSnapReportService
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
		log.info("<---------------------------- Inside FeedbackSnap Shot / execute ----------------------------->");
		JSONObject responseJSON = new JSONObject();
		Map<String, Object> responseMap = new HashMap<String, Object>();

		try
		{
			String messagePayload = message.getPayload();
			Map<String, Object> reportData = new HashMap<String, Object>();

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

				if (data != null && userData != null)
				{

					HashMap<String, String> parameter = new HashMap<>();

					parameter.put("fromDate", fromDate);

					parameter.put("toDate", toDate);

					parameter.put("username", username);

					
					
					List<FeedbackSnapshot> feedbackSnapshot = reportService.getFeedbackSnapshotReport(parameter);
					log.info("---feedbackSnapshot----" + feedbackSnapshot);

					if (feedbackSnapshot != null && feedbackSnapshot.size() > 0)
					{
						reportData.put("metCount", feedbackSnapshot.get(0).getMet());
						reportData.put("metNCallCount", feedbackSnapshot.get(0).getMetNCall());
						reportData.put("nonContactCount", feedbackSnapshot.get(0).getNotContactble());
						reportData.put("nonTotalAmnt", feedbackSnapshot.get(0).getTotalAmnt());
						reportData.put("totalCases", feedbackSnapshot.get(0).getTotalCases());
						reportData.put("totalAmount", feedbackSnapshot.get(0).getTotalAmnt());
						reportData.put("unattemptedCount", Integer.parseInt(feedbackSnapshot.get(0).getTotalCases())-Integer.parseInt(feedbackSnapshot.get(0).getMetNCall())-Integer.parseInt(feedbackSnapshot.get(0).getNotContactble())-Integer.parseInt(feedbackSnapshot.get(0).getMet()));

						responseJSON.put((JsonConstants.Key.DATA), MapToJSON.convertMapToJSON(reportData));

						responseJSON.put((JsonConstants.Key.STATUS), (JsonConstants.Value.STATUS_SUCCESS));
						responseJSON.put((JsonConstants.Key.MESSAGE), (JsonConstants.Value.MESSAGE_SUCCESS));
					}

				}
				else
				{
					responseJSON.put((JsonConstants.Key.DATA), MapToJSON.convertMapToJSON(reportData));

					responseJSON.put((JsonConstants.Key.STATUS), (JsonConstants.Value.STATUS_FAILURE));
					responseJSON.put((JsonConstants.Key.MESSAGE), (JsonConstants.Value.MESSAGE_SUCCESS));
				}

			}

		}
		catch (Exception e)
		{
			log.info("---- Exception ---", e);

		}
		return MessageBuilder.withPayload(String.valueOf(responseJSON)).copyHeaders(message.getHeaders()).build();
	}

}
