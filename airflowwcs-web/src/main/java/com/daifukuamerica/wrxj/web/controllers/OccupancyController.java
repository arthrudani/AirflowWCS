package com.daifukuamerica.wrxj.web.controllers;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.web.core.StackTraceFilter;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.service.UIService;
import com.daifukuamerica.wrxj.web.service.dao.OccupancyService;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;

@Controller
@RequestMapping("occupancy")
public class OccupancyController {
	
	private static final Logger logger = LoggerFactory.getLogger("Occupancy");

	@Autowired
	private OccupancyService occupancyService;
	
	/*
	 * @ModelAttribute("occupancyDataModel") public OccupancyDataModel
	 * initOccupancyDisplay() { return new OccupancyDataModel(); }
	 */
	
	@RequestMapping("/view")
	public String view(Model model)
	{
		model.addAttribute("pageName", "OCCUPANCY");
		return UIConstants.VIEW_OCCUPANCY;
	}
	/**
	 * List all
	 *
	 * @return
	 * @throws DBException
	 */
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
				tableData = occupancyService.list();
			}
			catch (Exception e)
			{
				logger.error("Error listing occupancy", StackTraceFilter.filter(e));
			}
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection", StackTraceFilter.filter(e));
		}
		return gson.toJson(tableData);
	}
	
	
	@RequestMapping(value = "/executeSp", method = RequestMethod.GET)
	@ResponseBody
	public void ExecuteSP() throws DBException
	{
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			try
			{
				occupancyService.executeSp();
			}
			catch (Exception e)
			{
				logger.error("Error Executing SP", StackTraceFilter.filter(e));
			}
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection", StackTraceFilter.filter(e));
		}
	}
}
