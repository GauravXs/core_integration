package com.mobicule.mcollections.integration.collection;

import org.springframework.integration.Message;

public interface ICodeofConductTransactionImplementation {

	Message<String> execute(Message<String> message) throws Throwable;

	

}
