package com.mobicule.mcollections.integration.kgi;

import org.json.me.JSONObject;
import org.springframework.integration.support.MessageBuilder;

import com.mobicule.component.activitylogger.beans.UserActivity;
import com.mobicule.component.activitylogger.commons.ActivityLoggerConstants;
import com.mobicule.component.activitylogger.threads.UserActivityAddition;
import com.mobicule.mcollections.core.beans.SystemUser;
import com.mobicule.mcollections.core.commons.JSONPayloadExtractor;
import com.mobicule.mcollections.core.commons.JsonConstants;
import com.mobicule.mcollections.integration.commons.ServerUtilities;
import com.mobicule.mcollections.integration.messaging.IService;

public interface IRollOverCasesSearchService extends IService
{}
