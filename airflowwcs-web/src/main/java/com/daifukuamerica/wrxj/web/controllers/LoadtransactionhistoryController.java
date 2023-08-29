package com.daifukuamerica.wrxj.web.controllers;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.daifukuamerica.wrxj.dbadapter.data.LoadTransactionHistoryData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.web.core.exceptions.AjaxException;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.LoadHistoryDataModel;
import com.daifukuamerica.wrxj.web.service.dao.LoadTransactionHistoryService;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for LoadTransactionHistory operations. This is the main mapping declaration and will
 * map URL patterns to the controller methods. For example the top level
 * controller in this instance would be {servletContext}/Alert
 *
 * Controller methods annotated with the @ResponseBody will not return a Model
 * or View it will return a straight string response to the client. In this
 * controller the @ResponseBody is used to return formatted JSON to the client
 * using the google GSON library. Jackson also provides a message formatter that
 * will handle this type of Object -> JSON response however the annotations and
 * simplicity of configuration with the GSON library by google allows for
 * greater control. Sometimes it may be necessary to add
 * procudes="application/json;charset=utf-8" to the
 * 
 * @RequestMapping annotation for the controller method or javascript methods
 *                 will not be able to differentiate the MIME type from a string
 *                 response and not parse the JSONs correctly.
 *
 *
 *                 Most Update/Delete/Modify actions will only be called from
 *                 the wrxj/history/loadtransactionhistory.jsp view. Read operations could be called
 *                 from elsewhere in the application.
 *
 *                 Author: dystout Created : May 2, 2017
 *
 */
@Controller
@RequestMapping("/loadtransactionhistory")
public class LoadtransactionhistoryController {

	/**
	 * Log4j logger: LoadTransactionController
	 */
	private static final Logger logger = LoggerFactory.getLogger("LoadTransactionHistory");

	private static final String WILDCARD = "ALL";
	private static final String UNDEFINED = "undefined";

	/** Drop-down options shouldn't change, so build them once and cache them */
	private static Map<String, Object[]> _dropdownMenus = null;
	
	/**
	 * loadTransaction Service
	 */
	@Autowired
	private LoadTransactionHistoryService historyService;

	@ModelAttribute("loadHistoryDataModel")
	public LoadHistoryDataModel initAlertDisplay() {
		return new LoadHistoryDataModel();
	}

	/**
	 * Capture Ajax exceptions here so we can send back a json response with the
	 * error message
	 * 
	 * @return
	 */
	@ExceptionHandler(AjaxException.class)
	@RequestMapping(produces = "application/json; charset=utf-8")
	@ResponseBody
	public String handleAjaxError() {
		AjaxResponse ajaxResponse = new AjaxResponse(AjaxResponseCodes.FAILURE,
				"An exception occured in the ajax call");
		Gson gson = new Gson();
		return gson.toJson(ajaxResponse);
	}

	/**
	 * Return the logical view name for the /wrxj/history/loadtransactionhistory.jsp page
	 * 
	 * @see {@link UIConstants} for mappings to logical view names
	 *
	 * @param model
	 * @return
	 */
	@RequestMapping("/view")
	public String view(Model model) {
		model.addAttribute("pageName", "LOAD TRANSACTION HISTORY");
		model.addAttribute("loadHistoryDataModel", new LoadHistoryDataModel());
		return UIConstants.VIEW_LOADTRANSACTIONHISTORY;
	}

	/**
	 * list
	 *
	 * @return
	 * @throws DBException
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String list() throws DBException {
		Gson gson = new Gson();
		TableDataModel tableData = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper()) {
			try {
				tableData = historyService.list();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			logger.error("Error getting database connection{}", e.getMessage(), e);
		}
		return gson.toJson(tableData);
	}

	/**
	 * Search with filters
	 *
	 * @param startingDate
	 * @param endingDate
	 * @param session
	 * @return
	 * @throws DBException
	 */
	@RequestMapping(value="/listSearch",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String listSearch(@RequestParam("startingDate") @DateTimeFormat(pattern="yyyyMMddHHmmss") Date startingDate,
			@RequestParam("endingDate") @DateTimeFormat(pattern="yyyyMMddHHmmss") Date endingDate, HttpSession session)
			throws DBException
	{
		System.out.println(startingDate+">"+endingDate);
		Gson gson = new Gson();
		TableDataModel tableData = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			try
			{
				LoadTransactionHistoryData key = Factory.create(LoadTransactionHistoryData.class);

				if (startingDate != null || endingDate != null)
					key.setDateRangeKey(startingDate, endingDate);

				tableData = historyService.listSearch(key);
			}
			catch (NoSuchFieldException e)
			{
				logger.error("LoadTransactionHistoryController (listSearch) Exception", e);
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
	 * empty table
	 *
	 * @return
	 */
	@RequestMapping("/empty")
	@ResponseBody
	public String getEmptyTable() {
		TableDataModel tdm = new TableDataModel();
		return new Gson().toJson(tdm);
	}
}
