/* ***************************************************************************
  Copyright (c) 2018 Daifuku North America Holding Company. All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED,
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND
  CIVIL LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.service.dao.ZoneGroupService;
import com.daifukuamerica.wrxj.web.service.dao.ZoneService;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Zone controller for handling requests/response for Zone actions.
 *
 * Mapped using relative context mappings.
 *
 * Author: dystout
 * Created : May 2, 2017
 *
 */
@Controller
@RequestMapping("/zone")
public class ZoneController
{
	/**
	 * Log4j logger: Zone
	 */
	private static final Logger logger = LoggerFactory.getLogger("Zone");

	@Autowired
	ZoneService zoneService;

	@Autowired
	ZoneGroupService zoneGroupService;

	/**
	 * View the Zone Definitions page
	 * @return logical view name
	 */
	@RequestMapping("/view")
	public String viewDefinition(Model model)
	{
		model.addAttribute("pageName", "ZONE DEFINITIONS");
		return UIConstants.VIEW_ZONE_DEFINITION;
	}

	/**
	 * View the Zone Groups page
	 * @return logical view name
	 */
	@RequestMapping("/viewGroup")
	public String viewGroup(Model model)
	{
		model.addAttribute("pageName", "ZONE GROUPS");
		return UIConstants.VIEW_ZONE_GROUP;
	}

	/**
	 * Return a JSON list of Zone Definitions.
	 *
	 * @return JSON - TableDataModel - Zones
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@RequestMapping(value="/list",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String list() throws DBException, NoSuchFieldException
	{
		Gson gson = new Gson();
		TableDataModel tableData = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			tableData = zoneService.list();
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}{}", e.getMessage(), e.getStackTrace());
		}
		return gson.toJson(tableData);
	}

	/**
	 * Return a JSON list of Zone Definitions.
	 *
	 * @return JSON - TableDataModel - Zones
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@RequestMapping(value="/listGroups",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String listGroups() throws DBException, NoSuchFieldException
	{
		Gson gson = new Gson();
		TableDataModel tableData = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  tableData = zoneGroupService.list();
	    }
		 catch (Exception e)
	    {
			 logger.error("Error getting database connection{}{}", e.getMessage(), e.getStackTrace());
	    }
		return gson.toJson(tableData);
	}
}
