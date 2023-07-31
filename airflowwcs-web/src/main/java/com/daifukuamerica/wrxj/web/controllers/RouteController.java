package com.daifukuamerica.wrxj.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.daifukuamerica.wrxj.web.ui.UIConstants;

/**
 * Route request/response handler for performing route specific actions. 
 * 
 * 
 * Mapped using relative context mappings. (http://{serverhostname}:{port}/{context}/route) 
 * 
 * Author: dystout
 * Created : May 2, 2017
 *
 */
@Controller
@RequestMapping("/route")
public class RouteController
{

	@RequestMapping("/view")
	public String view(Model model)
	{ 
		model.addAttribute("pageName", "ROUTES"); 
		return UIConstants.VIEW_ROUTE; 
	}
}
