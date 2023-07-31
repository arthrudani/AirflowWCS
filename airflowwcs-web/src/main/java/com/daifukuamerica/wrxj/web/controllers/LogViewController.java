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

import com.daifukuamerica.wrxj.dbadapter.data.WrxLogData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.LogViewerDataModel;
import com.daifukuamerica.wrxj.web.service.dao.LogViewService;
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
@RequestMapping("/logview")
public class LogViewController
{
	private static final Logger logger = LoggerFactory.getLogger("LOGVIEW");

	/**
	 * Service to provide CRUD operations
	 */
	@Autowired
	private LogViewService mpLogService;

	@ModelAttribute("historyDisplay")
	public LogViewerDataModel initOrderDisplay()
	{
		return new LogViewerDataModel();
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
		model.addAttribute("pageName", "LOG VIEWER");
		return UIConstants.VIEW_LOGVIEW;
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
				tableData = mpLogService.list();
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
	 * @param description
	 * @param session
	 * @return
	 * @throws DBException
	 */
	@RequestMapping(value = "/listSearch", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String listSearch(
			@RequestParam("startingDate") @DateTimeFormat(pattern = "yyyyMMddHHmmss") Date startingDate,
			@RequestParam("endingDate") @DateTimeFormat(pattern = "yyyyMMddHHmmss") Date endingDate,
			@RequestParam("description") String description, HttpSession session) throws DBException
	{
		TableDataModel tableData = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			try
			{
				WrxLogData vpKey = Factory.create(WrxLogData.class);

				if (startingDate != null || endingDate != null)
					vpKey.setBetweenKey(WrxLogData.DATE_TIME_NAME, startingDate, endingDate);

				description = description.trim();
				if (!description.isEmpty())
					vpKey.setKey(WrxLogData.DESCRIPTION_NAME, "%" + description, KeyObject.LIKE, KeyObject.AND);

				tableData = mpLogService.listSearch(vpKey);
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
