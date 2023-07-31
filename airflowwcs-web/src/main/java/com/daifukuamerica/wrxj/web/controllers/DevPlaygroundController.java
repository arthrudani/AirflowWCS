package com.daifukuamerica.wrxj.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.daifukuamerica.wrxj.web.ui.UIConstants;

@Controller
@RequestMapping("/playground")
public class DevPlaygroundController
{
	/**
	 * View the dev playground page 
	 * @return String - logical view name of device view 
	 */
	@RequestMapping("/view")
	public String view(Model model)
	{ 
		model.addAttribute("pageName", "DEV PLAYGROUND"); 
		return UIConstants.VIEW_PLAYGROUND; 
	}

}
