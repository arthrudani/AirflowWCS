package com.daifukuamerica.wrxj.web.controllers;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.web.core.messaging.JMSProducer;
import com.daifukuamerica.wrxj.web.model.User;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.service.MessageService;
import com.daifukuamerica.wrxj.web.service.UIService;
import com.daifukuamerica.wrxj.web.service.dao.RecoveryService;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message response/request handler for Message specific actions.
 *
 * Mapped using relative context mappings. (http://{serverhostname}:{port}/{context}/message/**)
 *
 * This controller may not perform many actions because most of the message code is client-side JS.
 *
 * Author: dystout
 * Created : May 2, 2017
 *
 */
@Controller
@RequestMapping("/message")
public class MessageController
{
	private static final Logger logger = LoggerFactory.getLogger("Message");
	@RequestMapping("/view")
	public String view(Model model)
	{
		model.addAttribute("pageName", "JMS MESSAGES");
		return UIConstants.VIEW_MESSAGE;
	}
	
	@Autowired
	private JMSProducer jmsProducer;


	@Autowired
	MessageService messageService;

	@Autowired
	RecoveryService recoveryService;

	@Autowired
	UIService uiService;

	/**
	 * Populate the model with dropdown options for the load add/modify/search popups
	 *
	 * This is called everytime the controller is utilized.
	 */
	@ModelAttribute("dropdownMenus")
	public Map<String, Object[]> getDropdowns()
	{
		Map<String,Object[]> dropdowns = new HashMap<String, Object[]>();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
			dropdowns = uiService.initMessageDropDownOptions();
	    }
		catch (Exception e)
	    {
			logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return dropdowns;
	}

	/**
	 *	Complete pick for given move data
	 *
	 * @param stationId
	 * @return
	 */
	@RequestMapping(value="/test/orderPickReply",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String completePick(HttpSession session, Model model)
	{
		User user = (User) session.getAttribute("user");
		AjaxResponse ajaxResponse = new AjaxResponse();
		ajaxResponse = messageService.sendTestOrderPickReply(user);
		return new Gson().toJson(ajaxResponse);
	}

	@RequestMapping(value="/testMessage/{message}",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String testMessage(HttpSession session, Model model, @PathVariable String message)
	{

		AjaxResponse ajaxResponse = new AjaxResponse();
		ajaxResponse = jmsProducer.sendTestMessages(message); 
		return new Gson().toJson(ajaxResponse);
	}

}
