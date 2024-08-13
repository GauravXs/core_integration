package com.mobicule.mcollections.integration.messaging;

import org.springframework.integration.Message;

public interface IRequestReceiver
{
	public Message<String> receive(Message<String> message);
}
