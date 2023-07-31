package com.daifukuamerica.wrxj.web.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;


/**
 * Internal message broker for user notifications. 
 * 
 * TODO -- implement notification alerts with header navigation bar. 
 * 
 * Author: dystout
 * Created : May 31, 2017
 *
 */
@Deprecated
@Controller
public class JmsMessageController
{

	@MessageMapping("/jms")
	@SendTo("/topic/WRxJTopic")
	public String send(String message) throws Exception {
	    String time = new SimpleDateFormat("HH:mm").format(new Date());
	    return message + " " + time;
	}
}
