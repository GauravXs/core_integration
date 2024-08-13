package com.mobicule.mcollections.integration.collection;

import org.springframework.integration.Message;

public interface ICollectionResendService {

	Message<String> execute(Message<String> message) throws Throwable;

}
