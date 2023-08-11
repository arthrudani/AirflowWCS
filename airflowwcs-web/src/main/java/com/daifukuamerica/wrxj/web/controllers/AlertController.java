package com.daifukuamerica.wrxj.web.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.daifukuamerica.wrxj.dbadapter.data.AlertsData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.daifukuamerica.wrxj.web.core.StackTraceFilter;
import com.daifukuamerica.wrxj.web.core.exceptions.AjaxException;
import com.daifukuamerica.wrxj.web.model.User;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.AlertDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.LoadDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.loadAndLLIDataModel;
import com.daifukuamerica.wrxj.web.service.UIService;
import com.daifukuamerica.wrxj.web.service.dao.AlertService;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for Alert operations. This is the main mapping declaration and will
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
 *                 the wrxj/alert/alert.jsp view. Read operations could be called
 *                 from elsewhere in the application.
 *
 *                 Author: dystout Created : May 2, 2017
 *
 */
@Controller
@RequestMapping("/alerts")
public class AlertController {

	/**
	 * Log4j logger: AlertController
	 */
	private static final Logger logger = LoggerFactory.getLogger("Alert");

	private static final String WILDCARD = "ALL";
	private static final String UNDEFINED = "undefined";

	/** Drop-down options shouldn't change, so build them once and cache them */
	private static Map<String, Object[]> _dropdownMenus = null;

	/**
	 * Service to provide popup and context menu dropdowns
	 */
	@Autowired
	private UIService uiService;
	
	/**
	 * Alert Service
	 */
	@Autowired
	private AlertService alertService;
	

	@Autowired
	private ServletContext context;

	@ModelAttribute("alertDataModel")
	public AlertDataModel initAlertDisplay() {
		return new AlertDataModel();
	}

	/**
	 * Populate the model with the context menu options for the load screen
	 * 
	 * @return
	 */
//	@ModelAttribute("contextMenus")
//	public Map<String, Object[]> getContextMenuOptions() {
//		Map<String, Object[]> contextmenus = new HashMap<String, Object[]>();
//		try (WebDBObjectHelper dboh = new WebDBObjectHelper()) {
//			contextmenus = uiService.initLoadContextMenus();
//		} catch (Exception e) {
//			logger.error("Error getting database connection{}", e.getMessage(), e);
//		}
//		return contextmenus;
//	}

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
	 * Return the logical view name for the /wrxj/alert/alert.jsp page
	 * 
	 * @see {@link UIConstants} for mappings to logical view names
	 *
	 * @param model
	 * @return
	 */
	@RequestMapping("/view")
	public String view(Model model) {
		model.addAttribute("pageName", "ALERT");
		model.addAttribute("alertDataModel", new AlertDataModel());
		return UIConstants.VIEW_ALERT;
	}

	/**
	 * Add a alert with a AlertDataModel pojo
	 *
	 * @param alertDataModel
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @throws AjaxException
	 */
//	@RequestMapping(value = "/add", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
//	@ResponseBody
//	public String add(@ModelAttribute loadAndLLIDataModel loadAndLLIDataModel) throws ServletException, IOException, AjaxException {
//		AjaxResponse ajaxResponse;
//		try (WebDBObjectHelper dboh = new WebDBObjectHelper()) {
//			try {
//				ajaxResponse = alertService.add(loadAndLLIDataModel);
//			} catch (Exception e) {
//				throw new AjaxException();
//			}
//		} catch (Exception e) {
//			logger.error("Error getting database connection{}", e.getMessage(), e);
//			throw new AjaxException();
//		}
//		Gson gson = new Gson();
//		return gson.toJson(ajaxResponse);
//	}

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
				tableData = alertService.list();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			logger.error("Error getting database connection{}", e.getMessage(), e);
		}
		return gson.toJson(tableData);
	}

	/**
	 * listSearch
	 *
	 * @param description
	 * @return
	 * @throws DBException
	 */
	@RequestMapping(value = "/listSearch", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String listSearch(@RequestParam("description") String description,HttpSession session) throws DBException {
		Gson gson = new Gson();
		TableDataModel tableData = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper()) {
			AlertsData alertData = new AlertsData();
			try {
					alertData.setDescription(description);

				tableData = alertService.listSearch(alertData);
			} catch (NoSuchFieldException e) {
				logger.error("LoadController (listSearch) Exception : {}", e.getMessage());
			}
		} catch (Exception e) {
			logger.error("Error getting database connection{}", e.getMessage(), e);
		}
		return gson.toJson(tableData);
	}

	/**
	 * delete
	 *
	 * @param alert
	 * @param session
	 * @return
	 */
//	@RequestMapping(value = "/delete", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
//	@ResponseBody
//	public AjaxResponse delete(@RequestParam("load") String load, HttpSession session) {
//		User user = (User) session.getAttribute("user");
//		AjaxResponse response = new AjaxResponse();
//		try (WebDBObjectHelper dboh = new WebDBObjectHelper()) {
//			String[] vasLoadIDs = load.split(",");
//			for (int i = 0; i < vasLoadIDs.length; i++) {
//				logger.info("UserID[{}] Deleting LoadID[{}] ({} of {})", user.getUserId(), vasLoadIDs[i], (i + 1),
//						vasLoadIDs.length);
//				response = alertService.delete(vasLoadIDs[i]);
//				if (response.getResponseCode() != AjaxResponseCodes.SUCCESS) {
//					logger.error(response.getResponseMessage());
//					return response;
//				}
//			}
//			response.setResponse(AjaxResponseCodes.SUCCESS,
//					"Successfully deleted load" + (vasLoadIDs.length > 1 ? "s" : "") + ": " + load);
//		} catch (Exception e) {
//			logger.error("Error getting database connection{}", e.getMessage(), e);
//			response.setResponse(AjaxResponseCodes.FAILURE, "Error deleting load: " + e.getMessage());
//		}
//		return response;
//	}

	/**
	 * modify
	 *
	 * @param alertDataModel
	 * @param session
	 * @return
	 */
//	@RequestMapping(value = "/modify", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
//	@ResponseBody
//	public String modify(@ModelAttribute LoadDataModel loadDataModel, HttpSession session) {
//		AjaxResponse ajaxResponse = new AjaxResponse();
//		User user = (User) session.getAttribute("user");
//		try (WebDBObjectHelper dboh = new WebDBObjectHelper()) {
//			logger.info("UserID[{}] Modifying LoadID[{}]", user.getUserId(), loadDataModel.getLoadId());
//			ajaxResponse = alertService.modify(loadDataModel);
//		} catch (Exception e) {
//			logger.error("Error getting database connection{}", e.getMessage(), e);
//		}
//		return new Gson().toJson(ajaxResponse);
//	}

	/**
	 * TODO reimplement
	 *
	 * @param req
	 * @param resp
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
//	@RequestMapping(value = "/search", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
//	@ResponseBody
//	public String search(@ModelAttribute LoadDataModel loadDataModel) throws ServletException, IOException {
//		TableDataModel tdm = null;
//		try (WebDBObjectHelper dboh = new WebDBObjectHelper()) {
//			tdm = alertService.searchLoads(loadDataModel);
//		} catch (Exception e) {
//			logger.error("Error getting database connection{}", e.getMessage(), e);
//		}
//		return new Gson().toJson(tdm);
//	}
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
