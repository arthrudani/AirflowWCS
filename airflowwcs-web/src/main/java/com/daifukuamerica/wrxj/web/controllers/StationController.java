package com.daifukuamerica.wrxj.web.controllers;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.daifukuamerica.wrxj.web.model.User;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/station")
public class StationController
{
	/**
	* Log4j logger: StationController
	*/
	private static final Logger logger = LoggerFactory.getLogger(StationController.class);
	
/*	@Autowired
	IkeaStationService stationService; 

	@RequestMapping(value="/lock",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String completePick(@RequestParam("station") String stationId, @RequestParam("ipAddress") String ipAddress, HttpSession session)
	{ 
		User user = (User) session.getAttribute("user");
		AjaxResponse ajaxResponse = new AjaxResponse();
		logger.debug(user.getUserId() + " -- Locking Station:  " + stationId + " to IP Address: " + ipAddress);
		ajaxResponse = stationService.lockClientToStation(stationId, ipAddress, user.getUserId());
		return new Gson().toJson(ajaxResponse);
	}*/

}
