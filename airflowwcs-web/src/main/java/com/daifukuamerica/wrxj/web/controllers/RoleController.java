package com.daifukuamerica.wrxj.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.RoleModel;
import com.daifukuamerica.wrxj.web.service.dao.WrxRoleService;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@RequestMapping("/role")
public class RoleController
{
	private static final Logger logger = LoggerFactory.getLogger("Role");

	@Autowired
	WrxRoleService roleService;

	@RequestMapping("/view")
	public String view(Model model)
	{
		model.addAttribute("pageName", "ROLES");
		return UIConstants.VIEW_ROLE;
	}

	@ModelAttribute("RoleModel")
	public RoleModel initItemDetailDisplay()
	{
		return new RoleModel();
	}

	@RequestMapping(value="/list",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String list() throws DBException
	{
		Gson gson = new Gson();
		TableDataModel tableData = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
	 	  try
		  {
			tableData = roleService.list();
		  }
	 	  catch (NoSuchFieldException e)
		  {
			logger.error("(move/list) Exception : {}", e.getMessage());
		  }
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return gson.toJson(tableData);
	}

	@RequestMapping(value="/option/list/{role}",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String optionList(@PathVariable String role) throws DBException
	{
		Gson gson = new Gson();
		TableDataModel tableData = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
	 	  try
		  {
			tableData = roleService.listRoleOption(role);
		  }
	 	  catch (NoSuchFieldException e)
		  {
			logger.error("Exception : {}", e.getMessage());
		  }
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return gson.toJson(tableData);
	}

	@RequestMapping(value="/modify",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String modify(@ModelAttribute RoleModel roleModel)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  ajaxResponse = 	roleService.modifyRole(roleModel);
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return new Gson().toJson(ajaxResponse);
	}

	@RequestMapping(value="/add",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String add(@ModelAttribute RoleModel roleModel)
	{
		AjaxResponse ajaxResponse =  new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  ajaxResponse = 	roleService.addRole(roleModel);
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return new Gson().toJson(ajaxResponse);
	}

	@RequestMapping(value="/delete",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String delete(@ModelAttribute RoleModel roleModel)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
		  ajaxResponse = 	roleService.deleteRole(roleModel);
		}
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return new Gson().toJson(ajaxResponse);
	}
}
