package com.daifukuamerica.wrxj.web.service;

import org.springframework.beans.factory.annotation.Autowired;


import com.daifukuamerica.wrxj.web.model.User;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageService
{
	
	
	/**
	* Log4j logger: MessageService
	*/
	private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

	public AjaxResponse sendTestOrderPickReply(User user)
	{
		AjaxResponse ajaxResponse = new AjaxResponse(); 
		
		return null; 
	}

	public AjaxResponse sendTestMessage(String string)
	{
		// TODO Auto-generated method stub
		return null;
	}
	

	

}
