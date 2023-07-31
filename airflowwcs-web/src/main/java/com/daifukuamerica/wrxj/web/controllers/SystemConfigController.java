package com.daifukuamerica.wrxj.web.controllers;

import javax.servlet.ServletContext;
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
import com.daifukuamerica.wrxj.web.core.exceptions.AjaxException;
import com.daifukuamerica.wrxj.web.model.User;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.SysConfigModel;
import com.daifukuamerica.wrxj.web.service.UIService;
import com.daifukuamerica.wrxj.web.service.dao.SysConfigService;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Order request/response handler for Order specific actions.
 *
 * Mapped using relative context mappings. (http://{serverhostname}:{port}/{context}/order/**)
 *
 * Author: dystout
 * Created : Sept 9, 2017
 *
 */
@Controller
@RequestMapping("/sysconfig")
public class SystemConfigController
{
	private static final Logger logger = LoggerFactory.getLogger("SysConfig");

	/**
	 * Service to provide popup and context menu dropdowns
	 */
	@Autowired
	private UIService uiService;

	@Autowired
	private SysConfigService sysConfigService;


	@Autowired
	private ServletContext context;


	@RequestMapping("/view")
	public String view(Model model)
	{
		model.addAttribute("pageName", "SYSTEM CONFIGURATION");
		return UIConstants.VIEW_SYSCONFIG;
	}

	@ModelAttribute("sysConfigModel")
	public SysConfigModel initSysConfigModel()
	{
		return new SysConfigModel();
	}

	/**
	 * List to TableDataModel. Returns all n
	 *
	 * @return
	 * @throws DBException
	 */
	@RequestMapping(value="/listControllerConfig",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String listControllerConfig() throws DBException
	{
		Gson gson = new Gson();
		TableDataModel tableData = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  try
		  {
			tableData = sysConfigService.listControllerConfig();
		  }
		  catch (NoSuchFieldException e)
		  {
			logger.error("Exception getting Controller Config listing. | Exception: {}", e.getMessage());
	 	  }
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return gson.toJson(tableData);
	}

	@RequestMapping(value="/listSysConfig",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String listSysConfig() throws DBException
	{
		Gson gson = new Gson();
		TableDataModel tableData = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  try
		  {
			tableData = sysConfigService.listSysConfig();
		  }
		  catch (NoSuchFieldException e)
		  {
			logger.error("Exception getting SysConfig listing. | Exception: {}", e.getMessage());
	  	  }
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return gson.toJson(tableData);
	}

	@RequestMapping(value="/findCC",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String findSysConfig(@RequestParam("paramId") String paramId) throws AjaxException
	{
		Gson gson = new Gson();
		SysConfigModel sysConfigData =  new SysConfigModel();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  sysConfigData = sysConfigService.findControllerConfig(paramId);
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return gson.toJson(sysConfigData);
	}

	@RequestMapping(value="/modifyCC",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse modifySysConfig(@RequestParam("paramId") String paramId, @RequestParam("paramValue") String paramValue, HttpSession session) throws AjaxException
	{
		User user = (User) session.getAttribute("user");
		AjaxResponse response = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
			response = sysConfigService.modifyControllerConfig(paramId, paramValue, user.getUserId());
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return response;
	}
	/**
	 * List to TableDataModel. Returns all n
	 *
	 * @return
	 * @throws DBException
	 */
	@RequestMapping(value="/empty",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String empty() throws DBException
	{
		Gson gson = new Gson();
		TableDataModel tableData = new TableDataModel();
		return gson.toJson(tableData);
	}

}
