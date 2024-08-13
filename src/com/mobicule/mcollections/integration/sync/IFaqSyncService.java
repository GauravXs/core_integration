package com.mobicule.mcollections.integration.sync;

import org.springframework.integration.Message;

public interface IFaqSyncService {

	Message<String> execute(Message<String> message) throws Throwable;

}
