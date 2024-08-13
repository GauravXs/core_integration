package com.mobicule.mcollections.integration.collection;

import org.springframework.integration.Message;

public interface IExotelCallTransactionImplementation {

	Message<String> exotelCallInitateMethod(Message<String> message) throws Throwable;

	

}
