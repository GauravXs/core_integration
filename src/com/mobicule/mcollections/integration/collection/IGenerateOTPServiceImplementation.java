package com.mobicule.mcollections.integration.collection;

import org.springframework.integration.Message;

public interface IGenerateOTPServiceImplementation {

	Message<String> execute(Message<String> message) throws Throwable;

	Message<String> checkOTPDetails(Message<String> message) throws Throwable;

}
