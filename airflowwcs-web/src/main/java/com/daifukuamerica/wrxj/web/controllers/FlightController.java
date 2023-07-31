/**
 * 
 */
package com.daifukuamerica.wrxj.web.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
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

import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.daifukuamerica.wrxj.web.core.StackTraceFilter;
import com.daifukuamerica.wrxj.web.core.exceptions.AjaxException;
import com.daifukuamerica.wrxj.web.model.User;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.LoadDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.OrderDataModel;
import com.daifukuamerica.wrxj.web.service.UIService;
import com.daifukuamerica.wrxj.web.service.dao.LoadService;
import com.daifukuamerica.wrxj.web.service.dao.OrderService;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSOrderServer;
import com.google.gson.Gson;

import oracle.ucp.common.waitfreepool.Factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Administrator
 *
 */
@Controller
@RequestMapping("/flight")
public class FlightController {

	private static final Logger logger = LoggerFactory.getLogger("Flight");
	
	/** Drop-down options shouldn't change, so build them once and cache them */
	private static Map<String, Object[]> _dropdownMenus = null;
	
	/**
	 * Service to provide popup and context menu dropdowns
	 */
	@Autowired
	private UIService uiService;
	
	private static final String WILDCARD = "ALL";
	
	/**
	 * Load Service
	 */
	@Autowired
	private LoadService loadService;
	
	@Autowired
	private ServletContext context;
	
	/**
	 *  Order Service
	 */
	@Autowired
	private OrderService orderService;
	


	// Flight Details related methods
	/**
	 * View the Flight Details page
	 * @return logical view name
	 */
	@RequestMapping(value ="/viewDetails", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public String viewGroup(Model model, @RequestParam("flightId") String flightId, HttpServletRequest request) throws ServletException, IOException
	{
		String pageName= "FLIGHT DETAILS FOR : " + flightId;
		model.addAttribute("pageName", pageName);
		model.addAttribute("loadDataModel", new LoadDataModel());
		request.getSession().setAttribute("flightId", flightId);
		return UIConstants.VIEW_FLIGHT_DETAILS;
	}
	
	@ModelAttribute("loadDataModel")
	public LoadDataModel initLoadDisplay()
	{
		return new LoadDataModel();
	}
	
	@ModelAttribute("orderDataModel")
	public OrderDataModel initOrderDisplay()
	{
		return new OrderDataModel();
	}
	
	/**
	* Populate the model with dropdown options for the load add/modify/search popups
	 *
	 * This is called every time the controller is utilized.
	 */
	@ModelAttribute("dropdownMenus")
	public Map<String, Object[]> getDropdowns()
	{
		if (_dropdownMenus == null)
		{
			Map<String, Object[]> dropdowns = new HashMap<String, Object[]>();
			try (WebDBObjectHelper dboh = new WebDBObjectHelper())
			{
				dropdowns = uiService.initLoadDropDownOptions(context);
				_dropdownMenus = dropdowns;
			}
			catch (Exception e)
			{
				logger.error("Error getting database connection", StackTraceFilter.filter(e));
				return dropdowns;
			}
		}
		return _dropdownMenus;
	}
	
	/**
	 * find flight Details by Id
	 *
	 * @param loadId
	 * @param model
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/byFlightId", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String viewLoad(@RequestParam("FlightId") String flightId ) throws ServletException, IOException
	{
		Gson gson = new Gson();
		TableDataModel tableData = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{			
			try
			{
				tableData = loadService.listFlightDetailById(flightId);
			}
			catch (DBException e)
			{
				logger.error("FlightController (byFlight) Exception", StackTraceFilter.filter(e));
			}
			catch (NoSuchFieldException e)
			{
				logger.error("FlightController (byFlight) Exception", StackTraceFilter.filter(e));
			}
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection", StackTraceFilter.filter(e));
		}
		return gson.toJson(tableData);
	}
	
	@RequestMapping(value="/retrieveLoads",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse retrieveLoads(@RequestParam("loadIds") String loadIds, @RequestParam("destStation") String destStation, HttpSession session)
	{
		User user = (User) session.getAttribute("user");
		AjaxResponse response = new AjaxResponse();
		OrderDataModel orderData = new OrderDataModel();
		
		logger.info("User[{}] RETRIEVING destStation {}", user.getUserId(), destStation);
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			  
				logger.info("User[{}] RETRIEVING Load ID: {}", user.getUserId(), loadIds);
				orderData.setOrderId(loadIds);
				orderData.setDestStation(destStation);
				orderData.setPriority(5);
				response = orderService.retrieve(orderData);
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
			response.setResponse(AjaxResponseCodes.FAILURE, "Error getting database connection" + e.getMessage());
		}
		return response;
	}
	/**
	 * Creates order to retrieve all bags associated with this flight (lot)
	 * @param lot
	 * @param session
	 * @return
	 */
	@RequestMapping(value="/makeOrderForFlight",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse makeOrderForFlight(@RequestParam("lot") String lot,  HttpSession session)
	{
		User user = (User) session.getAttribute("user");
		AjaxResponse response = new AjaxResponse();
		
		
		logger.info("User[{}] RETRIEVING Flight {}", user.getUserId(), lot);
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			logger.info("User[{}] RETRIEVING Loads fro lot: {}", user.getUserId(), lot);
			response  = orderService.buildOrderForFlight(lot);
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
			response.setResponse(AjaxResponseCodes.FAILURE, "Error getting database connection" + e.getMessage());
		}
		return response;
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
	 * listSearch by flight Id
	 *
	 * @param warehouse
	 * @param address
	 * @param deviceId
	 * @param loadId
	 * @param mcKey
	 * @param amountFull
	 * @param item
	 * @param session
	 * @return
	 * @throws DBException
	 */
	@RequestMapping(value = "/listSearch", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String listSearch(@RequestParam("warehouse") String warehouse, @RequestParam("address") String address,
			@RequestParam("deviceId") String deviceId, @RequestParam("loadId") String loadId, @RequestParam("item") String item, HttpSession session)
			throws DBException
	{
		Gson gson = new Gson();
		TableDataModel tableData = null;
		String flightId = (String) session.getAttribute("flightId");
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			LoadData loadData = new LoadData();
			try
			{
				if (!warehouse.equals(WILDCARD))
					loadData.setWarehouse(warehouse);

				if (SKDCUtility.isNotBlank(address))
					loadData.setAddress(address);

				if (!deviceId.equals(WILDCARD))
					loadData.setDeviceID(deviceId);

				if (SKDCUtility.isNotBlank(loadId))
					loadData.setLoadID(loadId);
				
				if (SKDCUtility.isBlank(item))
					item = null;
				else
					item = item.trim();

				tableData = loadService.listSearchFlights(loadData, item, flightId);
			}
			catch (NoSuchFieldException e)
			{
				logger.error("LoadController (listSearch) Exception : {}", e.getMessage());
			}
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
		}
		return gson.toJson(tableData);
	}

	//End of Flight Details methods

	
	@RequestMapping("/view")
	public String view(Model model)
	{
		model.addAttribute("pageName", "FLIGHTS");
		return UIConstants.VIEW_FLIGHT;
	}

	/**
	 * Return a JSON list of flights.
	 *
	 * @return JSON - TableDataModel - Flights
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String list() throws DBException
	{
		Gson gson = new Gson();
		TableDataModel tableData = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			tableData = loadService.listFlight();
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
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
	public String getEmptyTable()
	{
		TableDataModel tdm = new TableDataModel();
		return new Gson().toJson(tdm);
	}
	
	/**
	 * Capture Ajax exceptions here so we can send back a json response with the error message
	 * @return
	 */
	@ExceptionHandler(AjaxException.class)
	@RequestMapping(produces="application/json; charset=utf-8")
	@ResponseBody
	public String handleAjaxError(){
		AjaxResponse ajaxResponse = new AjaxResponse(AjaxResponseCodes.FAILURE, "An exception occured in the ajax call");
		Gson gson = new Gson();
		return gson.toJson(ajaxResponse);
	}
}
