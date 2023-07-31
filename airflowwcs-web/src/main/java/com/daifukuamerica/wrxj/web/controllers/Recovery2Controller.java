/* ***************************************************************************
  Copyright (c) 2019 Daifuku North America Holding Company. All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED,
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND
  CIVIL LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.web.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.daifukuamerica.wrxj.web.model.User;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.LoadDataModel;
import com.daifukuamerica.wrxj.web.service.UIService;
import com.daifukuamerica.wrxj.web.service.dao.Recovery2Service;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Recovery2 request/response handler for performing Recovery specific actions.
 *
 * Mapped using relative context mappings. (http://{serverhostname}:{port}/{context}/recovery/**)
 *
 * Author: mandrus
 * Created: 2019-04-24
 */
@Controller
@RequestMapping("/recovery2")
public class Recovery2Controller {

	/**
	 * Log4j logger: LoadController
	 */
	private static final Logger logger = LoggerFactory.getLogger("RECOVERY");

	private static final String WILDCARD = "ALL";
	private static final String UNDEFINED = "undefined";

	/**
	 * Service to provide popup and context menu dropdowns
	 */
	@Autowired
	private UIService uiService;

	/**
	 * Service to provide DB operations
	 */
	@Autowired
	Recovery2Service recoveryService;

	@Autowired
	private ServletContext context;

	@ModelAttribute("loadDataModel")
	public LoadDataModel initLoadDisplay()
	{
		return new LoadDataModel();
	}

	/**
	 * Field length for add dialog
	 * @return
	 */
	@ModelAttribute("maxLengthLoadId")
	public int maxLengthLoadId()
	{
		return DBInfo.getFieldLength("SLOADID");
	}

	/**
	 * Populate the model with dropdown options for the load add/modify/search popups
	 *
	 * This is called everytime the controller is utilized.
	 */
	@ModelAttribute("dropdownMenus")
	public Map<String, Object[]> getDropdowns()
	{
		Map<String, Object[]> dropdowns = new HashMap<String, Object[]>();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			dropdowns = uiService.initLoadDropDownOptions(context);
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
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
			logger.error("Error getting database connection{}", e.getMessage(), e);
		}
		return contextmenus;
	}

	/**
	 * Return the logical view name for the /wrxj/recovery2/recovery.jsp page
	 * @see {@link UIConstants} for mappings to logical view names
	 *
	 * @param model
	 * @return
	 */
	@RequestMapping("/view")
	public String view(Model model)
	{
		model.addAttribute("pageName", "RECOVERY");
		model.addAttribute("loadDataModel", new LoadDataModel());
		return UIConstants.VIEW_RECOVERY2;
	}

	/*==============================================================*/
	/* list															*/
	/*==============================================================*/

	/**
	 * list
	 *
	 * @return
	 * @throws DBException
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String list() throws DBException
	{
		TableDataModel tableData = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			try
			{
				tableData = recoveryService.list();
			}
			catch (NoSuchFieldException e)
			{
				logger.error("Error getting recovery list", e);
			}
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection", e);
		}
		return new Gson().toJson(tableData);
	}

	/**
	 * listSearch
	 *
	 * @param loadId
	 * @param mcKey
	 * @param warehouse
	 * @param address
	 * @param position - optional
	 * @param nextWarehouse
	 * @param nextAddress
	 * @param nextPosition - optional
	 * @param device
	 * @param session
	 * @return
	 * @throws DBException
	 */
	@RequestMapping(value = "/listSearch", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String listSearch(@RequestParam("loadId") String loadId,
			@RequestParam("mcKey") String mcKey,
			@RequestParam("warehouse") String warehouse,
			@RequestParam("address") String address,
			@RequestParam("shelfPosition") Optional<String> position,
			@RequestParam("nextWarehouse") String nextWarehouse,
			@RequestParam("nextAddress") String nextAddress,
			@RequestParam("nextShelfPosition") Optional<String> nextPosition,
			@RequestParam("device") String device,
			HttpSession session)
			throws DBException
	{
		TableDataModel tableData = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			LoadData loadData = new LoadData();
			try
			{
				if (SKDCUtility.isNotBlank(loadId))
					loadData.setKey(LoadData.LOADID_NAME, loadId + "%", KeyObject.LIKE);

				if (SKDCUtility.isNotBlank(mcKey))
					loadData.setKey(LoadData.MCKEY_NAME, mcKey);

				if (!warehouse.equals(WILDCARD))
					loadData.setKey(LoadData.WAREHOUSE_NAME, warehouse);

				if (SKDCUtility.isNotBlank(address))
					loadData.setKey(LoadData.ADDRESS_NAME, address + "%", KeyObject.LIKE);

				if (position.isPresent() && SKDCUtility.isNotBlank(position.get()) && !position.get().equals(UNDEFINED))
					loadData.setKey(LoadData.SHELFPOSITION_NAME, position.get());

				if (!nextWarehouse.equals(WILDCARD))
					loadData.setKey(LoadData.NEXTWAREHOUSE_NAME, nextWarehouse);

				if (SKDCUtility.isNotBlank(nextAddress))
					loadData.setKey(LoadData.NEXTADDRESS_NAME, nextAddress + "%", KeyObject.LIKE);

				if (nextPosition.isPresent() && SKDCUtility.isNotBlank(nextPosition.get()) && !nextPosition.get().equals(UNDEFINED))
					loadData.setKey(LoadData.NEXTSHELFPOSITION_NAME, nextPosition.get());

				if (!device.equals(WILDCARD))
					loadData.setKey(LoadData.DEVICEID_NAME, device);

				tableData = recoveryService.listSearch(loadData);
			}
			catch (NoSuchFieldException e)
			{
				logger.error("{} (listSearch) Exception : {}", getClass().getSimpleName(), e.getMessage());
			}
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection", e);
		}
		return new Gson().toJson(tableData);
	}

	/*==============================================================*/
	/* Recovery														*/
	/*==============================================================*/

	/**
	 * Select Load -> Recover
	 *
	 * @param loadId
	 * @return
	 */
	@RequestMapping(value="/recover",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse recover(@RequestParam("loadId") String loadId, HttpSession session)
	{
		AjaxResponse ajaxResponse = getDefaultResponse();
		User user = (User) session.getAttribute("user");
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = recoveryService.getRecoveryFlow(loadId, user);
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection", e);
		}
		return ajaxResponse;
	}

	/**
	 * Recover -> Auto-Pick
	 *
	 * @param loadId
	 * @return
	 */
	@RequestMapping(value="/autopick",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse autoPick(@RequestParam("loadId") String loadId, HttpSession session)
	{
		AjaxResponse ajaxResponse = getDefaultResponse();
		User user = (User) session.getAttribute("user");
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = recoveryService.autoPick(loadId, user);
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection", e);
		}
		return ajaxResponse;
	}

	/**
	 * Recover -> Cancel
	 *
	 * @param loadId
	 * @return
	 */
	@RequestMapping(value="/cancel",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse cancel(@RequestParam("loadId") String loadId, HttpSession session)
	{
		AjaxResponse ajaxResponse = getDefaultResponse();
		User user = (User) session.getAttribute("user");
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = recoveryService.cancel(loadId, user);
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection", e);
		}
		return ajaxResponse;
	}

	/**
	 * Recover -> Reschedule
	 *
	 * @param loadId
	 * @return
	 */
	@RequestMapping(value="/reschedule",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse reschedule(@RequestParam("loadId") String loadId, HttpSession session)
	{
		AjaxResponse ajaxResponse = getDefaultResponse();
		User user = (User) session.getAttribute("user");
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = recoveryService.reschedule(loadId, user);
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection", e);
		}
		return ajaxResponse;
	}

	/**
	 * Recover -> Send Arrival
	 *
	 * @param loadId
	 * @param height
	 * @return
	 */
	@RequestMapping(value="/sendarrival",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse sendArrival(@RequestParam("loadId") String loadId,
			@RequestParam("height") Optional<Integer> height, HttpSession session)
	{
		AjaxResponse ajaxResponse = getDefaultResponse();
		User user = (User) session.getAttribute("user");
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = recoveryService.sendArrival(loadId, height.isPresent() ? height.get() : null, user);
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection", e);
		}
		return ajaxResponse;
	}

	/**
	 * Recover -> Send Completion
	 *
	 * @param loadId
	 * @return
	 */
	@RequestMapping(value="/sendcompletion",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse sendCompletion(@RequestParam("loadId") String loadId, HttpSession session)
	{
		AjaxResponse ajaxResponse = getDefaultResponse();
		User user = (User) session.getAttribute("user");
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = recoveryService.sendCompletion(loadId, user);
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection", e);
		}
		return ajaxResponse;
	}

	/**
	 * Recover -> Store Mode
	 *
	 * @param loadId
	 * @return
	 */
	@RequestMapping(value="/storemode",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse storeMode(@RequestParam("loadId") String loadId, HttpSession session)
	{
		AjaxResponse ajaxResponse = getDefaultResponse();
		User user = (User) session.getAttribute("user");
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = recoveryService.storeMode(loadId, user);
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection", e);
		}
		return ajaxResponse;
	}

	/*==============================================================*/
	/* Template														*/
	/*==============================================================*/

	/**
	 * Recover -> XXX
	 *
	 * @param loadId
	 * @return
	 */
	@RequestMapping(value="/xxx",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse xxx(@RequestParam("loadId") String loadId, HttpSession session)
	{
		AjaxResponse ajaxResponse = getDefaultResponse();
		User user = (User) session.getAttribute("user");
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = recoveryService.xxx(loadId, user);
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection", e);
		}
		return ajaxResponse;
	}

	/*==============================================================*/
	/* Helper														*/
	/*==============================================================*/

	/**
	 * Default response
	 * @return
	 */
	private AjaxResponse getDefaultResponse()
	{
		return new AjaxResponse(AjaxResponseCodes.FAILURE,
				"Unable to execute internal logic. Please verify recovery conditions met.");
	}
}
