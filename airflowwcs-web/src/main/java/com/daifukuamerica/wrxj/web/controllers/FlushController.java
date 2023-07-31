package com.daifukuamerica.wrxj.web.controllers;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.web.model.User;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.service.UIService;
import com.daifukuamerica.wrxj.web.service.dao.LoadService;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Controller
@RequestMapping("/flush")
public class FlushController
{

	/**
	 * Log4j logger: Scale Controller
	 */
	private static final Logger logger = LoggerFactory.getLogger("LOAD");

	/**
	 * UI element rendering
	 */
	@Autowired
	UIService uiService;
	
	@Autowired
	LoadService loadService;
	
	@Autowired
	private ServletContext context;	

	@RequestMapping("/view")
	public String view(Model model, HttpServletRequest request)
	{
		model.addAttribute("pageName", "FLUSH LOADS FROM AISLE"); 
		return UIConstants.VIEW_FLUSH;
	}

	/**
	 * Populate the model with dropdown options for the add/modify/search popups
	 * 
	 * <i><b>This is called everytime the controller is utilized.<b></i>
	 */
	@ModelAttribute("dropdownMenus")
	public Map<String, Object[]> getDropdowns()
	{
		Map<String,Object[]> dropdowns  = new HashMap<String, Object[]>();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  dropdowns = uiService.initFlushDropDownOptions(context);
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}{}", e.getMessage(), e.getStackTrace());
	    }
		return dropdowns;
	}

	
	@RequestMapping(value = "/flushLoads", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String flushLoads(@RequestParam("srcAisle") String srcAisle, HttpSession session)
	{
		AjaxResponse ajaxResponse  = null;
		User user = (User) session.getAttribute("user");
		
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  try
		  {
			
			  ajaxResponse = loadService.flushLoads(srcAisle, user.getUserId());
		  } 
		  catch (DBException e)
		  {
			// TODO Auto-generated catch block
			e.printStackTrace();
		  } 
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}{}", e.getMessage(), e.getStackTrace());
	    }
		return new Gson().toJson(ajaxResponse); 
	}
}
