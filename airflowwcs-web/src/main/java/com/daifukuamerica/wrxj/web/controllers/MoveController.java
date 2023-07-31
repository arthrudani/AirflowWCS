package com.daifukuamerica.wrxj.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.service.dao.MoveService;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Move request/response handler for Move specific actions.
 *
 * Mapped using relative context mappings. (http://{serverhostname}:{port}/{context}/move/**)
 *
 * Author: dystout
 * Created : May 2, 2017
 *
 */
@Controller
@RequestMapping("/move")
public class MoveController
{
	private static final Logger logger = LoggerFactory.getLogger("FILE");

	@Autowired
	MoveService moveService;

	@RequestMapping("/view")
	public String view(Model model)
	{
		model.addAttribute("pageName", "MOVES");
		return UIConstants.VIEW_MOVE;
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
			tableData = moveService.list();
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

	@RequestMapping(value="/listByLoadId/{loadId}",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String listByLoadId(@PathVariable String loadId) throws DBException
	{
		Gson gson = new Gson();
		TableDataModel tableData = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
	 	  try
		  {
			tableData = moveService.getMoveDataListByLoad(loadId);
		  }
	 	  catch (NoSuchFieldException e)
		  {
			logger.error("(move/listByLoadId) Exception : {}", e.getMessage());
		  }
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return gson.toJson(tableData);
	}

	@RequestMapping(value="/listByOrderId/{orderId}",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String listByOrderId(@PathVariable String orderId) throws DBException
	{
		Gson gson = new Gson();
		TableDataModel tableData = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  try
		  {
			tableData = moveService.getMoveDataListByOrder(orderId);
		  }
		  catch (NoSuchFieldException e)
		  {
			logger.error("(move/listByOrderId) Exception : {}", e.getMessage());
		  }
		}
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return gson.toJson(tableData);
	}
}
