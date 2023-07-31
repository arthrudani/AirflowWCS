/* ***************************************************************************
  Copyright (c) 2019 Daifuku North America Holding Company. All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED,
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.web.controllers;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.daifukuamerica.wrxj.dbadapter.data.WrxEquipLogData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.web.core.StackTraceFilter;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.EquipLogViewerDataModel;
import com.daifukuamerica.wrxj.web.service.UIService;
import com.daifukuamerica.wrxj.web.service.dao.EquipLogViewService;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transaction History
 *
 * Mapped using relative context mappings.
 * (http://{serverhostname}:{port}/{context}/order/**)
 *
 * Author: mandrus Created : Mar 14, 2019
 */
@Controller
@RequestMapping("/equiplogview")
public class EquipLogViewController
{
	private static final Logger logger = LoggerFactory.getLogger("EQUIPLOGVIEW");

	/**
	 * Service to provide CRUD operations
	 */
	@Autowired
	private EquipLogViewService mpEquipLogService;
	
	/**
	 * Service to provide popup and context menu dropdowns
	 */
	@Autowired
	private UIService uiService;

	@Autowired
	private ServletContext context;

	
	@ModelAttribute("historyDisplay")
	public EquipLogViewerDataModel initOrderDisplay()
	{
		return new EquipLogViewerDataModel();
	}

	/**
	 * Populate the model with dropdown options for the load add/modify/search popups
	 *
	 * This is called everytime the controller is utilized.
	 */
	@ModelAttribute("dropdownMenus")
	public Map<String, Object> getDropdowns()
	{
		Map<String, Object> dropdowns = new HashMap<String, Object>();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			dropdowns = uiService.initLocationDropDownOptions(context);
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection", StackTraceFilter.filter(e));
		}
		return dropdowns;
	}

	
	/**
	 * View
	 *
	 * @param model
	 * @return
	 */
	@RequestMapping("/view")
	public String view(Model model)
	{
		model.addAttribute("pageName", "EQUIPMENT LOG VIEWER");
		return UIConstants.VIEW_EQUIP_LOGVIEW;
	}

	/**
	 * List to TableDataModel. Returns all n
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
				tableData = mpEquipLogService.list();
			}
			catch (Exception e)
			{
				logger.error("Exception getting history listing.", e);
			}
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection", e);
		}
		return gson.toJson(tableData);
	}

	/**
	 * Search with filters
	 *
	 * @param startingDate
	 * @param endingDate
	 * @param data
	 * @param session
	 * @return
	 * @throws DBException
	 */
	@RequestMapping(value = "/listSearch", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String listSearch(
			@RequestParam("startingDate") @DateTimeFormat(pattern = "yyyyMMddHHmmss") Date startingDate,
			@RequestParam("endingDate") @DateTimeFormat(pattern = "yyyyMMddHHmmss") Date endingDate,
			@RequestParam("deviceId") String deviceId,
			@RequestParam("data") String data, HttpSession session) throws DBException
	{
		TableDataModel tableData = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			try
			{
				WrxEquipLogData vpKey = Factory.create(WrxEquipLogData.class);

				if (startingDate != null || endingDate != null)
					vpKey.setBetweenKey(WrxEquipLogData.DATE_TIME_NAME, startingDate, endingDate);

				data = data.trim();
				if (!data.isEmpty())
					vpKey.setKey(WrxEquipLogData.DATA_NAME, "%" + data + "%", KeyObject.LIKE, KeyObject.AND);
				
				deviceId = deviceId.trim();
				if (!deviceId.equals(SKDCConstants.ALL_STRING))
					vpKey.setKey(WrxEquipLogData.DEVICEID_NAME, deviceId, KeyObject.EQUALITY);

				tableData = mpEquipLogService.listSearch(vpKey);
			}
			catch (Exception e)
			{
				logger.error("LogViewController (listSearch) Exception", e);
			}
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection", e);
		}

		if (tableData == null)
		{
			tableData = new TableDataModel();
		}
		return new Gson().toJson(tableData);
	}

	/**
	 * Empty list
	 *
	 * @return
	 * @throws DBException
	 */
	@RequestMapping(value = "/empty", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String empty() throws DBException
	{
		Gson gson = new Gson();
		TableDataModel tableData = new TableDataModel();
		return gson.toJson(tableData);
	}
}
