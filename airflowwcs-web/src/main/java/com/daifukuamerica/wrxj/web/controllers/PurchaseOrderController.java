package com.daifukuamerica.wrxj.web.controllers;

import java.io.IOException;
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

import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.web.core.exceptions.AjaxException;
import com.daifukuamerica.wrxj.web.model.User;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.PurchaseOrderDataModel;
import com.daifukuamerica.wrxj.web.service.UIService;
import com.daifukuamerica.wrxj.web.service.dao.PurchaseOrderDetailService;
import com.daifukuamerica.wrxj.web.service.dao.PurchaseOrderService;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Purchase Order/Expected Receipt request/response handler for related actions.
 *
 * Mapped using relative context mappings. (http://{serverhostname}:{port}/{context}/order/**)
 *
 * Author: mandrus
 * Created : Sept 25, 2018
 *
 */
@Controller
@RequestMapping("/expected")
public class PurchaseOrderController
{
	private static final Logger logger = LoggerFactory.getLogger("PURCHASEORDER");

	/**
	 * Service to provide popup and context menu dropdowns
	 */
	@Autowired
	private UIService uiService;

	/**
	 * Service to proved load CRUD operations
	 */
	@Autowired
	private PurchaseOrderService orderService;

	@Autowired
	private PurchaseOrderDetailService orderDetailService;

	@Autowired
	private ServletContext context;

	@ModelAttribute("orderDisplay")
	public PurchaseOrderDataModel initOrderDisplay()
	{
		return new PurchaseOrderDataModel();
	}

	/**
	 * Populate the model with dropdown options for the load add/modify/search popups
	 *
	 * This is called every time the controller is utilized.
	 */
	@ModelAttribute("dropdownMenus")
	public Map<String, Object[]> getDropdowns()
	{
		return uiService.initOrderDropDownOptions(context);
	}

	/**
	 * Populate the model with the context menu options for the load screen
	 * @return
	 */
	@ModelAttribute("contextMenus")
	public Map<String, Object[]> getContextMenuOptions(){
		return uiService.initLoadContextMenus();
	}


	@RequestMapping("/view")
	public String view(Model model)
	{
		model.addAttribute("pageName", "EXPECTED RECEIPTS");
		return UIConstants.VIEW_PURCHASEORDER;
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
	public String add(@ModelAttribute PurchaseOrderDataModel dataModel) throws ServletException, IOException, AjaxException
	{
		AjaxResponse ajaxResponse;
		try
		{
			ajaxResponse = orderService.add(dataModel);
		}
		catch (Exception e)
		{
			throw new AjaxException();
		}
		return new Gson().toJson(ajaxResponse);
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
		try
		{
			tableData = orderService.list();
		} catch (NoSuchFieldException e)
		{
			logger.error("Exception getting order listing. | Exception: {}", e.getMessage());
		}
		return gson.toJson(tableData);
	}

	@RequestMapping(value="/listSearch",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String listSearch(@RequestParam("orderId") String orderId, @RequestParam("loadId") String loadId,
			@RequestParam("item") String item, HttpSession session) throws DBException
	{
		Gson gson = new Gson();
		TableDataModel tableData = null;
		PurchaseOrderHeaderData ohData = Factory.create(PurchaseOrderHeaderData.class);
		PurchaseOrderLineData olData = Factory.create(PurchaseOrderLineData.class);
		try
		{
			if (!orderId.trim().isEmpty())
				ohData.setKey(PurchaseOrderHeaderData.ORDERID_NAME, orderId, KeyObject.LIKE);

			if (!loadId.trim().isEmpty())
				olData.setKey(PurchaseOrderLineData.LOADID_NAME, loadId, KeyObject.LIKE);

			if (!item.trim().isEmpty())
				olData.setKey(PurchaseOrderLineData.ITEM_NAME, item, KeyObject.LIKE);

			tableData = orderService.listSearch(ohData, olData);
		}
		catch (NoSuchFieldException e)
		{
			logger.error("OrderController (listSearch) Exception : {}", e.getMessage());
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

		for (String ord: orderIds)
		{
			logger.info("User[{}] DELETING order: {}", user.getUserId(), ord);
		}
		AjaxResponse responseCode = orderService.deleteOrders(orderIds);
		return gson.toJson(responseCode);
	}

	/**
	 * Modify
	 *
	 * @param dataModel
	 * @param session
	 * @return
	 */
	@RequestMapping(value="/modify",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String modify(@ModelAttribute PurchaseOrderDataModel dataModel,  HttpSession session)
	{
		Gson gson = new Gson();
		User user = (User) session.getAttribute("user");

		logger.info("User[{}] Modifying order: {}", user.getUserId(), dataModel.getOrderId());
		AjaxResponse ajaxResponse = orderService.modify(dataModel);
		return gson.toJson(ajaxResponse);
	}

	/**
	 * Find
	 *
	 * @param orderId
	 * @return
	 * @throws AjaxException
	 */
	@RequestMapping(value="/find",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String findByOrderId(@RequestParam("order") String orderId) throws AjaxException
	{
		Gson gson = new Gson();
		PurchaseOrderDataModel orderModel= orderService.find(orderId);
		return gson.toJson(orderModel);
	}

	/**
	 * List Details
	 *
	 * @param orderId
	 * @return
	 * @throws AjaxException
	 */
	@RequestMapping(value="/listDetail",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String findDetailByOrderId(@RequestParam("order") String orderId) throws AjaxException
	{
		Gson gson = new Gson();
		TableDataModel detailTableData = null;
		try
		{
			detailTableData = orderDetailService.listDetail(orderId);
		}
		catch (Exception e)
		{
			logger.error("Unable to get order details for order: {} Exception: {}", orderId, e.getMessage());
		}
		return gson.toJson(detailTableData);
	}
}
