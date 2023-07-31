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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.daifukuamerica.wrxj.web.model.User;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.LoadDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.LocationDataModel;
import com.daifukuamerica.wrxj.web.service.UIService;
import com.daifukuamerica.wrxj.web.service.dao.LoadService;
import com.daifukuamerica.wrxj.web.service.dao.LocationService;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Location request/response controller for Location specific actions.
 *
 * Mapped using relative context mappings.
 * (http://{serverhostname}:{port}/{context}/location/**)
 *
 * Author: dystout Created : May 2, 2017
 *
 */
/**
 * @author Administrator
 *
 */
@Controller
@RequestMapping("/location")
public class LocationController
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
	LocationService locationService;
	
	@Autowired
	LoadService loadService;

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
		model.addAttribute("pageName", "LOCATIONS");
		return UIConstants.VIEW_LOCATION;
	}

	/**
	 * find
	 *
	 * @param loadId
	 * @param model
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/find", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String viewLocation(@RequestParam("warehouse") String warehouse, @RequestParam("address") String address,
			@RequestParam("shelfPosition") Optional<String> position, Model model) throws ServletException, IOException
	{
		LocationDataModel dataModel = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			dataModel = locationService.find(warehouse, address, position.orElse(null));
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
		}
		return new Gson().toJson(dataModel);
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
				tableData = locationService.list();
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
			@RequestParam("locationStatus") Integer locationStatus,
			@RequestParam("emptyFlag") Integer emptyFlag,
			@RequestParam("type") Integer type,
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

				if (locationStatus != SKDCConstants.ALL_INT) {
					vpLocKey.setLocationStatus(locationStatus);
					vpLocKey.setKey(LocationData.LOCATIONSTATUS_NAME, locationStatus);
				}

				if (emptyFlag != SKDCConstants.ALL_INT) {
					vpLocKey.setEmptyFlag(emptyFlag);
					vpLocKey.setKey(LocationData.EMPTYFLAG_NAME, emptyFlag);
				}
				
				if (type != SKDCConstants.ALL_INT) {
					vpLocKey.setLocationType(type);
					vpLocKey.setKey(LocationData.LOCATIONTYPE_NAME, type);
				}

				tableData = locationService.list(vpLocKey);
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
	 * Location by Load ID
	 *
	 * @param loadId
	 * @return
	 * @throws DBException
	 */
	@RequestMapping(value = "/locationByLoadId/{loadId}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String listByLoadId(@PathVariable String loadId) throws DBException
	{
		Gson gson = new Gson();
		TableDataModel tableData = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			try
			{
				tableData = locationService.listByLoadId(loadId);
			}
			catch (NoSuchFieldException e)
			{
				logger.error("Error getting translation values", StackTraceFilter.filter(e));
			}
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection", StackTraceFilter.filter(e));
		}
		return gson.toJson(tableData);
	}
	
	/**
	 * Flush lane and retrieve bags
	 * New function added 2022-04-08
	 * @param loadId
	 * @return
	 * @throws DBException
	 */
	
	@RequestMapping(value="/flushLane",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse retrieveLoads(@RequestParam("addressId") String addressId, HttpSession session)
	{
		
		AjaxResponse ajaxResponse = new AjaxResponse();
		User user = (User) session.getAttribute("user");
		logger.info("User[{}] Flushing {}", user.getUserId(), addressId);
		
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  try
		  {			
			  ajaxResponse = loadService.flushLoads(addressId, user.getUserId());
		  } 
		  catch (DBException e)
		  {
			  logger.error("Error getting database connection : {}", e.getMessage(), e);
			  ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Error getting database connection" + e.getMessage());
			  e.printStackTrace();
		  } 
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}{}", e.getMessage(), e.getStackTrace());
	    }
		return ajaxResponse; 
	}
	
	/**
	 * modify
	 *
	 * @param loadDataModel
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/modify", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String modify(@ModelAttribute LocationDataModel locationDataModel, HttpSession session)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		User user = (User) session.getAttribute("user");
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = locationService.modify(locationDataModel);
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
		}
		return new Gson().toJson(ajaxResponse);
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
