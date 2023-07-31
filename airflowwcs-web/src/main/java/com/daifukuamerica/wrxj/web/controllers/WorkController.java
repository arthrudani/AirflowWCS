package com.daifukuamerica.wrxj.web.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.daifukuamerica.wrxj.dbadapter.data.MoveCommandData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.daifukuamerica.wrxj.web.core.StackTraceFilter;
import com.daifukuamerica.wrxj.web.core.exceptions.AjaxException;
import com.daifukuamerica.wrxj.web.model.User;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.WorkDataModel;
import com.daifukuamerica.wrxj.web.service.UIService;
import com.daifukuamerica.wrxj.web.service.dao.WorkMaintenanceService;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;

@Controller
@RequestMapping("/work")
public class WorkController {
	
	private static final Logger logger = LoggerFactory.getLogger("WORKMAINT");


	/**
	 * Service to provide popup and context menu dropdowns
	 */
	@Autowired
	private UIService uiService;
	
	@Autowired
	private ServletContext context;
	
	@Autowired
	private WorkMaintenanceService workService;
	
	@ModelAttribute("workDataModel")
	public WorkDataModel initLoadDisplay()
	{
		return new WorkDataModel();
	}
	
	@RequestMapping("/view")
	public String view(Model model)
	{
		model.addAttribute("pageName", "WORK MAINTENANCE");
		return UIConstants.VIEW_WORK;
	}
	
	@RequestMapping(value = "/listSearch", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String listSearch(@RequestParam("loadId") String loadId, @RequestParam("lot") String lot, 
			@RequestParam("lineId") String lineId,@RequestParam("deviceId") String deviceId,HttpSession session)
			throws IOException, Exception
	{
		Gson gson = new Gson();
		TableDataModel tableData = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			MoveCommandData key = Factory.create(MoveCommandData.class);
			try
			{
				if (!loadId.trim().isEmpty())
					key.setLoadID(loadId);
				
				if (!deviceId.trim().isEmpty())
					key.setDeviceID(deviceId);
			
				if (SKDCUtility.isBlank(lineId))
					lineId = null;
				else
					lineId = lineId.trim();
				
				if (SKDCUtility.isBlank(lot))
					lot = null;
				else
					lot = lot.trim();
				
				tableData = workService.listSearch(key, lineId, lot);
			
			}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
		}
		if (tableData == null)
		{
			tableData = new TableDataModel();
		}
		return gson.toJson(tableData);
		}
		
	}
	@RequestMapping(value = "/list", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String list() throws DBException
	{
		Gson gson = new Gson();
		TableDataModel tableData = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			try
			{
				tableData = workService.listSearch(null, null, null);
			}
			catch (Exception e)
			{
				logger.error("Error listing locations", StackTraceFilter.filter(e));
			}
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection", StackTraceFilter.filter(e));
		}
		return gson.toJson(tableData);
	}
	@RequestMapping(value="/empty",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String empty() throws DBException
	{
		Gson gson = new Gson();
		TableDataModel tableData = new TableDataModel();
		return gson.toJson(tableData);
	}
	
	@RequestMapping(value = "/add", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String add(@ModelAttribute WorkDataModel workDataModel ) throws ServletException, IOException, AjaxException
	{
		AjaxResponse ajaxResponse;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			try
			{
				ajaxResponse = workService.add(workDataModel);
			}
			catch (Exception e)
			{
				throw new AjaxException();
			}
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
			throw new AjaxException();
		}
		Gson gson = new Gson();
		return gson.toJson(ajaxResponse);
	}
	
	@RequestMapping(value="/byload/{loadId}",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String listByLoad(@PathVariable String loadId)
	{
		Gson gson = new Gson();
		TableDataModel itemDetailByLoad = new TableDataModel();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  try
		  {
			itemDetailByLoad = workService.listByLoadId(loadId);
			if(itemDetailByLoad.getTableData().size() == 0 || itemDetailByLoad == null)
				return gson.toJson(new AjaxResponse(AjaxResponseCodes.FAILURE, "Load Data of Load Id "+loadId+" does not exists"));  
		  }
		  catch (NoSuchFieldException | DBException e)
		  {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return gson.toJson(new AjaxResponse(AjaxResponseCodes.FAILURE, "Failed to load data from loadId"+loadId));  
		  }
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
			return gson.toJson(new AjaxResponse(AjaxResponseCodes.FAILURE, "Failed to load data from loadId"+loadId));  
	    }
		return gson.toJson(itemDetailByLoad);
	}
	
	@RequestMapping(value="/delete",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse delete(@RequestParam("id") String id, HttpSession session)
	{
		User user = (User) session.getAttribute("user");
		AjaxResponse response = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
    			response = workService.delete(id);
    			if (response.getResponseCode() != AjaxResponseCodes.SUCCESS)
    			{
    		        response.setResponse(AjaxResponseCodes.FAILURE, response.getResponseMessage());
    			    logger.error(response.getResponseMessage());
    		        return response;
    			}
	        response.setResponse(AjaxResponseCodes.SUCCESS, "Successfully deleted Command");
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
	        response.setResponse(AjaxResponseCodes.FAILURE, "Error deleting Command: " + e.getMessage());
		}
		return response;
	}
	@RequestMapping(value = "/find", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String find(@RequestParam("id") String id, Model model) throws DBException, NoSuchFieldException
	{

		WorkDataModel wdm = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			wdm = workService.getMoveCommandData(id);
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
		}
		return new Gson().toJson(wdm);
	}
	@RequestMapping(value="/modify",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String modify(@ModelAttribute WorkDataModel workDataModel,@QueryParam("iId") int iId)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		   ajaxResponse = 	workService.modify(workDataModel,iId);
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return new Gson().toJson(ajaxResponse);
	}
	@ModelAttribute("dropdownMenus")
	public Map<String, Object[]> getDropdowns()
	{
		Map<String, Object[]> dropdowns = new HashMap<String, Object[]>();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			dropdowns = uiService.initWorkDropDownOptions(context);
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection", StackTraceFilter.filter(e));
		}
		return dropdowns;
	}
	
	@ModelAttribute("contextMenus")
	public Map<String, Object[]> getContextMenuOptions()
	{
		Map<String, Object[]> contextmenus = new HashMap<String, Object[]>();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			contextmenus = uiService.initLoadContextMenus();
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
		}
		return contextmenus;
	}

}
