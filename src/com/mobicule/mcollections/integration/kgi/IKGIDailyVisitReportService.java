package com.mobicule.mcollections.integration.kgi;

import org.springframework.integration.Message;

public interface IKGIDailyVisitReportService
{
	Message<String> execute(Message<String> message);

}
