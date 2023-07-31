package com.daifukuamerica.wrxj.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.daifukuamerica.wrxj.web.ui.UIConstants;


/**
 * 
 * Warehouse response/request handler for performing warehouse specific actions. 
 * 
 * Mapped using relative context mappings. (http://{serverhostname}:{port}/{context}/warehouse) 
 * 
 * Author: dystout
 * Created : May 2, 2017
 */
@Controller
@RequestMapping("/warehouse")
public class WarehouseController
{

	@RequestMapping("/view")
	public String view(Model model)
	{ 
		model.addAttribute("pageName", "WAREHOUSES"); 
		return UIConstants.VIEW_WAREHOUSE; 
	}
}
