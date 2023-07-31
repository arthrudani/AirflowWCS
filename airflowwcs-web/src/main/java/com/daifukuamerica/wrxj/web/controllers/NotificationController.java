package com.daifukuamerica.wrxj.web.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.daifukuamerica.wrxj.web.model.json.Message;
import com.daifukuamerica.wrxj.web.model.json.OutputMessage;
import com.google.gson.Gson;

/**
 * Internal message topic that runs in-memory in web context. 
 * Messages are accessible to WebSocketBroker. 
 * 
 * TODO No longer in use
 * 
 * Author: dystout
 * Created : May 16, 2017
 *
 */
@Deprecated
@Controller
public class NotificationController
{
	
	@MessageMapping("/notification")
	@SendTo("/topic/messages")
	public OutputMessage send(Message message) throws Exception {
	    String time = new SimpleDateFormat("HH:mm").format(new Date());
	    return new OutputMessage(message.getFrom(), message.getText(), time);
	}

}
