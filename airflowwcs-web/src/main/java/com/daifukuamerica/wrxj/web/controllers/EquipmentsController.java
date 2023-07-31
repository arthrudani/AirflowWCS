package com.daifukuamerica.wrxj.web.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.daifukuamerica.wrxj.web.core.StackTraceFilter;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.LocationDataModel;
import com.daifukuamerica.wrxj.web.service.UIService;
import com.daifukuamerica.wrxj.web.service.dao.EquipmentsService;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;

/**
 * @author BT
 *
 */
@Controller
@RequestMapping("/equipments")
public class EquipmentsController
{
	private static final Logger logger = LoggerFactory.getLogger("Location");

	private static final String UNDEFINED = "undefined";

	@Autowired
	private ServletContext context;

	/**
	 * Service to provide popup and context menu dropdowns
	 */
	@Autowired
	private UIService uiService;

	@Autowired
	EquipmentsService equipmentsService;
	
	/**
	 * Model for search
	 * @return
	 */
	@ModelAttribute("locationModel")
	public LocationDataModel initLocationDisplay()
	{
		return new LocationDataModel();
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
	 * View page
	 *
	 * @param model
	 * @return
	 */
	@RequestMapping("/view")
	public String view(Model model)
	{
		model.addAttribute("pageName", "EQUIPMENTS");
		return UIConstants.VIEW_EQUIPMENTS;
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
				tableData = equipmentsService.list();
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

	/**
	 * listSearch
	 *
	 * @param warehouse
	 * @param address
	 * @param position - optional
	 * @param device
	 * @param session
	 * @return
	 * @throws DBException
	 */
	@RequestMapping(value = "/listSearch", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String listSearch(@RequestParam("warehouse") String warehouse,
			@RequestParam("address") String address,
			@RequestParam("shelfPosition") Optional<String> position,
			@RequestParam("deviceId") String deviceId,
			@RequestParam("zone") String zone,
			HttpSession session)
			throws DBException
	{
		Gson gson = new Gson();
		TableDataModel tableData = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			try
			{
				LocationData vpLocKey = Factory.create(LocationData.class);
				if (!SKDCConstants.ALL_STRING.equals(warehouse)) {
					vpLocKey.setWarehouse(warehouse);
					vpLocKey.setKey(LocationData.WAREHOUSE_NAME, warehouse);
				}

				if (SKDCUtility.isNotBlank(address)) {
					vpLocKey.setAddress(address);
					vpLocKey.setKey(LocationData.ADDRESS_NAME, address);
				}

				if (position.isPresent() && SKDCUtility.isNotBlank(position.get()) && !position.get().equalsIgnoreCase(UNDEFINED))
					vpLocKey.setKey(LocationData.SHELFPOSITION_NAME, position.get());

				if (!SKDCConstants.ALL_STRING.equals(deviceId)) {
					vpLocKey.setDeviceID(deviceId);
					vpLocKey.setKey(LocationData.DEVICEID_NAME, deviceId);
				}

				if (!SKDCConstants.ALL_STRING.equals(zone)) {
					vpLocKey.setZone(zone);
					vpLocKey.setKey(LocationData.ZONE_NAME, zone);
				}
				tableData = equipmentsService.list(vpLocKey);
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
	
}
