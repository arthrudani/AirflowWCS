package com.daifukuamerica.wrxj.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.daifukuamerica.wrxj.web.ui.UIConstants;

/**
 * Port request/response handler used for performing port specific actions. 
 * 
 * Mapped using relative context mappings. (http://{serverhostname}:{port}/{context}/port/**) 
 * 
 * Author: dystout
 * Created : May 2, 2017
 *
 */
@Controller
@RequestMapping("/port")
public class PortController
{

	@RequestMapping("/view")
	public String view(Model model)
	{ 
		model.addAttribute("pageName", "PORTS"); 
		return UIConstants.VIEW_PORT; 
	}
}