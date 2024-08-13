package com.mobicule.mcollections.integration.report;

import org.springframework.integration.Message;

public interface ICollectionActivityReportService
{
	Message<String> execute(Message<String> message);
}
