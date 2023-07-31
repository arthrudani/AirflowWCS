package com.daifukuamerica.wrxj.web.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.daifukuamerica.wrxj.web.core.exceptions.AjaxException;
import com.daifukuamerica.wrxj.web.model.User;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.OrderDataModel;
import com.daifukuamerica.wrxj.web.service.UIService;
import com.daifukuamerica.wrxj.web.service.dao.OrderDetailService;
import com.daifukuamerica.wrxj.web.service.dao.OrderService;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Order request/response handler for Order specific actions.
 *
 * Mapped using relative context mappings. (http://{serverhostname}:{port}/{context}/order/**)
 *
 * Author: dystout
 * Created : Sept 9, 2017
 *
 */
@Controller
@RequestMapping("/order")
public class OrderController
{
	private static final Logger logger = LoggerFactory.getLogger("ORDERMAINT");

	private static final String WILDCARD = "ALL";

	/**
	 * Service to provide popup and context menu dropdowns
	 */
	@Autowired
	private UIService uiService;

	/**
	 * Service to proved load CRUD operations
	 */
	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderDetailService orderDetailService;

	@Autowired
	private ServletContext context;

	@ModelAttribute("orderDisplay")
	public OrderDataModel initOrderDisplay()
	{
		return new OrderDataModel();
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
			dropdowns = uiService.initOrderDropDownOptions(context);
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
		Map<String,Object[]> contextmenus  = new HashMap<String, Object[]>();
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


	@RequestMapping("/view")
	public String view(Model model)
	{
		model.addAttribute("pageName", "ORDER MAINTENANCE");
		return UIConstants.VIEW_ORDER;
	}

	/**
	 * Add n data model. Passed to wrx server to complete DB logic.
	 *
	 * @param dataModel
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @throws AjaxException
	 */
	@RequestMapping(value="/add",method=RequestMethod.POST, produces= "application/json; charset=utf-8")
	@ResponseBody
	public String add(@ModelAttribute OrderDataModel dataModel) throws ServletException, IOException, AjaxException
	{
		AjaxResponse ajaxResponse;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  try
		  {
			ajaxResponse = orderService.add(dataModel);
		  }
		  catch(Exception e)
		  {
			throw new AjaxException();
		  }
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    	throw new AjaxException();
	    }
		Gson gson = new Gson();
		return gson.toJson(ajaxResponse);

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
			tableData = orderService.list();
		  }
		  catch (NoSuchFieldException e)
		  {
			logger.error("Exception getting order listing. | Exception: {}", e.getMessage());
		  }
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return gson.toJson(tableData);
	}

	@RequestMapping(value="/listSearch",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String listSearch(@RequestParam("sOrderType") String orderType, @RequestParam("sOrderStatus") String orderStatus,
			@RequestParam("orderId") String orderId, @RequestParam("item") String item, HttpSession session) throws DBException
	{
		Gson gson = new Gson();
		TableDataModel tableData = null;
		OrderHeaderData ohData = new OrderHeaderData();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  try
		  {
			if (!orderType.equals(WILDCARD))
				ohData.setKey(OrderHeaderData.ORDERTYPE_NAME, DBTrans.getIntegerValue(OrderHeaderData.ORDERTYPE_NAME, orderType));

			if (!orderStatus.equals(WILDCARD))
				ohData.setKey(OrderHeaderData.ORDERSTATUS_NAME, DBTrans.getIntegerValue(OrderHeaderData.ORDERSTATUS_NAME, orderStatus));

			if (SKDCUtility.isNotBlank(orderId))
				ohData.setKey(OrderHeaderData.ORDERID_NAME, orderId);

			if (SKDCUtility.isBlank(item))
				item = null;
			else
				item = item.trim();

			tableData = orderService.listSearch(ohData, item);
		  }
		  catch (NoSuchFieldException e)
		  {
			logger.error("OrderController (listSearch) Exception : {}", e.getMessage());
		  }
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		if (tableData == null)
		{
			tableData = new TableDataModel();
		}
		return gson.toJson(tableData);
	}
	/**
	 * List to TableDataModel. Returns all n
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



	/**
	 * Delete n. Uses wrx server to complete db logic.
	 * @param load
	 * @return
	 */
	@RequestMapping(value="/delete",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String delete(@RequestParam("orderIds[]") String[] orderIds,  HttpSession session)
	{
		Gson gson = new Gson();
		User user = (User) session.getAttribute("user");
		AjaxResponse responseCode = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  for (String ord: orderIds)
		  {
		  	logger.info("User[{}] DELETING order: {}", user.getUserId(), ord);
		  }
		  responseCode = orderService.deleteOrders(orderIds);
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return gson.toJson(responseCode);

	}


	@RequestMapping(value="/modify",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String modify(@ModelAttribute OrderDataModel dataModel,  HttpSession session)
	{
		Gson gson = new Gson();
		User user = (User) session.getAttribute("user");
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  logger.info("User[{}] Modifying order: {}", user.getUserId(), dataModel.getOrderId());
		  ajaxResponse = orderService.modify(dataModel);
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return gson.toJson(ajaxResponse);
	}


	/**
	 *
	 * @param load
	 * @return
	 */
	@RequestMapping(value="/markReady",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String markReady(@RequestParam("orderIds[]") String[] orderIds, HttpSession session)
	{
		Gson gson = new Gson();
		User user = (User) session.getAttribute("user");
		AjaxResponse responseCode =  new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  for (String ord: orderIds)
		  {
			logger.info("User[{}] READYING order: {}", user.getUserId(), ord);
		  }
		  responseCode = orderService.markOrdersReady(orderIds);
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return gson.toJson(responseCode);

	}

	/**
	 *
	 * @param load
	 * @return
	 */
	@RequestMapping(value="/markHold",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String markHold(@RequestParam("orderIds[]") String[] orderIds, HttpSession session)
	{
		Gson gson = new Gson();
	    User user = (User) session.getAttribute("user");
	    AjaxResponse responseCode =  new AjaxResponse();
	    try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  for (String ord: orderIds)
		  {
			logger.info("User[{}] HOLDING order: {}", user.getUserId(), ord);
		  }
		  responseCode = orderService.markOrdersHold(orderIds);
	    }
	    catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return gson.toJson(responseCode);

	}


	/**
	 *
	 * @param load
	 * @return
	 */
	@RequestMapping(value="/allocate",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String allocate(@RequestParam("orderIds[]") String[] orderIds)
	{
		Gson gson = new Gson();
		AjaxResponse responseCode =  new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  logger.debug("ALLOCATING order(s): {}", orderIds);
		  responseCode = orderService.markAllocateOrders(orderIds);
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return gson.toJson(responseCode);

	}


	@RequestMapping(value="/find",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String findByOrderId(@RequestParam("order") String orderId) throws AjaxException
	{
		Gson gson = new Gson();
		OrderDataModel orderModel = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  orderModel = orderService.find(orderId);
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return gson.toJson(orderModel);
	}

	@RequestMapping(value="/listDetail",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String findDetailByOrderId(@RequestParam("order") String orderId) throws AjaxException
	{
		Gson gson = new Gson();
		TableDataModel detailTableData = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  try
		  {
			detailTableData = orderDetailService.listDetail(orderId);
		  }
		  catch (NoSuchFieldException e)
		  {
			logger.error("Unable to get order details for order: {} Exception: {}", orderId, e.getMessage());
		  }
		  catch (DBException e)
		  {
			logger.error("Unable to get order details for order: {} Exception: {}", orderId, e.getMessage());
		  }
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return gson.toJson(detailTableData);
	}

	/**
	 * Retrieve Load
	 *
	 * @param orderDataModel
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/retrieve", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String retrieve(@ModelAttribute OrderDataModel orderDataModel, HttpSession session) throws DBException
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		User user = (User) session.getAttribute("user");
		String vsLoads = orderDataModel.getOrderId();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
		  
            String[] vasLoadIDs = vsLoads.split(",");
            for (int i = 0; i < vasLoadIDs.length; i++)
            {
                logger.info("UserID=[{}] Retrieving LoadID=[{}] to Station=[{}] ({} of {})", user.getUserId(), orderDataModel.getOrderId(), orderDataModel.getDestStation(), (i + 1), vasLoadIDs.length);
                orderDataModel.setOrderId(vasLoadIDs[i]);
                ajaxResponse = orderService.retrieve(orderDataModel);
                if (ajaxResponse.getResponseCode() != AjaxResponseCodes.SUCCESS)
                {
                    logger.error(ajaxResponse.getResponseMessage());
                    return new Gson().toJson(ajaxResponse);
                }
                ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Successfully ordered load"
                    + (vasLoadIDs.length > 1 ? "s" : "") +": " + vsLoads);
            }
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Error deleting load: " + e.getMessage());
		}
		return new Gson().toJson(ajaxResponse);
	}
}
