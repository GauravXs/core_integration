package com.mobicule.mcollections.integration.messaging;

import org.springframework.integration.Message;

public interface IService
{
	public abstract Message<String> execute(Message<String> message) throws Throwable;

}
