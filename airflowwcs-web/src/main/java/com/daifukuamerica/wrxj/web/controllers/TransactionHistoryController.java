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

import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.TransactionHistoryDataModel;
import com.daifukuamerica.wrxj.web.service.UIService;
import com.daifukuamerica.wrxj.web.service.dao.TransactionHistoryService;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transaction History
 *
 * Mapped using relative context mappings. (http://{serverhostname}:{port}/{context}/order/**)
 *
 * Author: mandrus
 */
@Controller
@RequestMapping("/history")
public class TransactionHistoryController
{
	private static final Logger logger = LoggerFactory.getLogger("TRANSACTIONHISTORY");

	/**
	 * Service to provide popup and context menu dropdowns
	 */
	@Autowired
	private UIService uiService;

	/**
	 * Service to proved load CRUD operations
	 */
	@Autowired
	private TransactionHistoryService historyService;

	@Autowired
	private ServletContext context;

	@ModelAttribute("historyDisplay")
	public TransactionHistoryDataModel initOrderDisplay()
	{
		return new TransactionHistoryDataModel();
	}

	/**
	 * Populate the model with dropdown options for the load add/modify/search popups
	 *
	 * This is called every time the controller is utilized.
	 */
	@ModelAttribute("dropdownMenus")
	public Map<String, Object[]> getDropdowns()
	{
		Map<String, Object[]> dropdowns = new HashMap<String, Object[]>();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			dropdowns = uiService.initOrderDropDownOptions(context);
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection", e);
		}
		return dropdowns;
	}

	/**
	 * Populate the model with the context menu options for the load screen
	 * @return
	 */
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
			logger.error("Error getting database connection", e);
		}
		return contextmenus;
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
		model.addAttribute("pageName", "TRANSACTION HISTORY");
		return UIConstants.VIEW_TRANSACTIONHISTORY;
	}

	/**
	 * List to TableDataModel. Returns all n
	 *
	 * @return
	 * @throws DBException
	 */
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
				tableData = historyService.list();
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
	 * @param loadId
	 * @param session
	 * @return
	 * @throws DBException
	 */
	@RequestMapping(value="/listSearch",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String listSearch(@RequestParam("startingDate") @DateTimeFormat(pattern="yyyyMMddHHmmss") Date startingDate,
			@RequestParam("endingDate") @DateTimeFormat(pattern="yyyyMMddHHmmss") Date endingDate,
			@RequestParam("loadId") String loadId, @RequestParam("lot") String lot, @RequestParam("lineId") String lineId, HttpSession session)
			throws DBException
	{
		Gson gson = new Gson();
		TableDataModel tableData = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			try
			{
				TransactionHistoryData key = Factory.create(TransactionHistoryData.class);

				if (startingDate != null || endingDate != null)
					key.setDateRangeKey(startingDate, endingDate);

				if (!loadId.trim().isEmpty())
					key.setKey(TransactionHistoryData.LOADID_NAME, loadId, KeyObject.LIKE);
				
				if (!lot.trim().isEmpty())
					key.setKey(TransactionHistoryData.LOT_NAME, lot, KeyObject.LIKE);
				
				if (!lineId.trim().isEmpty())
					key.setKey(TransactionHistoryData.LINEID_NAME, lineId, KeyObject.LIKE);

				tableData = historyService.listSearch(key);
			}
			catch (NoSuchFieldException e)
			{
				logger.error("TransactionHistoryController (listSearch) Exception", e);
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
		return gson.toJson(tableData);
	}

	/**
	 * Empty list
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
