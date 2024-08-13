package com.mobicule.mcollections.integration.report;

import org.springframework.integration.Message;

public interface IFeedbackSnapReportService
{
	Message<String> execute(Message<String> message);
}
