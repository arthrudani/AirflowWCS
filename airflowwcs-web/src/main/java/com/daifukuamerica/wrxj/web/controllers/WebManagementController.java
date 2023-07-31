package com.daifukuamerica.wrxj.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.web.model.json.wrx.StoreModel;
import com.daifukuamerica.wrxj.web.ui.UIConstants;

@Controller
@RequestMapping("/webmanagement")
public class WebManagementController
{
	
	@RequestMapping("/view")
	public String view(Model model)
	{ 
		model.addAttribute("pageName", "WEB APPLICATION MANAGEMENT"); 
		return UIConstants.VIEW_WEB_MANAGEMENT; 
	}

}
