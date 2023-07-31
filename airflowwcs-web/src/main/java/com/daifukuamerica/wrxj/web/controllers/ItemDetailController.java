package com.daifukuamerica.wrxj.web.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.web.core.exceptions.AjaxException;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.ItemDetailModel;
import com.daifukuamerica.wrxj.web.service.UIService;
import com.daifukuamerica.wrxj.web.service.dao.ItemDetailService;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Item details request/response controller for item details actions.
 *
 * Author: dystout
 * Created : May 2, 2017
 *
 */
@Controller
@RequestMapping("/itemdetail")
public class ItemDetailController
{
	/**
	 * Log4j logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(ItemDetailController.class);

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



	/**
	 * Service to provide popup and context menu dropdowns
	 */
	@Autowired
	private UIService uiService;

	@Autowired
	private ServletContext context;

	@Autowired
	private ItemDetailService itemDetailService;

	@ModelAttribute("ItemDetailModel")
	public ItemDetailModel initItemDetailDisplay()
	{
		return new ItemDetailModel();
	}

	/**
	 * Populate the model with dropdown options for the itemdetail add/modify/search popups
	 *
	 * This is called everytime the controller is utilized.
	 */
	@ModelAttribute("dropdownMenus")
	public Map<String, Object[]> getDropdowns()
	{
		Map<String,Object[]> dropdowns  = new HashMap<String, Object[]>();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
			dropdowns = uiService.initItemDetailsDropDownOptions();
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return dropdowns;
	}


	/**
	 * View the Item details screen
	 * @return String - logical view name of the Item Details jsp (uses view resolver)
	 */
	@RequestMapping("/view")
	public String view(Model model)
	{
		model.addAttribute("pageName", "ITEM DETAILS");
		return UIConstants.VIEW_ITEMDETAILS;
	}


	/**
	 * View the item details screen filtered by the load given as url param.
	 *
	 * @param loadId
	 * @param model
	 * @return String - logical view name of the Item details by load jsp
	 */
	@RequestMapping(value="/view",params={"loadId"})
	public String viewByLoad(@RequestParam("loadId") String loadId, Model model)
	{
		model.addAttribute("loadId",loadId);
		model.addAttribute("pageName", "ITEM DETAILS");
		return UIConstants.VIEW_ITEMDETAILS_BYLOAD;
	}

	/**
	 * View the item details by the given load line item id.
	 *
	 * @param itemId
	 * @param model
	 * @return String - logical view name of the Item details by load line item jsp
	 */
	@RequestMapping(value="/view",params={"itemId"})
	public String viewByLoadLineItem(@RequestParam("itemId") String itemId, Model model)
	{
		model.addAttribute("pageName", "ITEM DETAILS");
		model.addAttribute("itemId",itemId);
		return UIConstants.VIEW_ITEMDETAILS_BYLOADLINEITEM;
	}

	/**
	 * Respond with a JSON list of all unfiltered item datails.
	 * @return JSON - TableDataModel - Item Details
	 * @throws DBException
	 */
	@RequestMapping(value="/list",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String list() throws DBException
	{
		Gson gson = new Gson();
		TableDataModel itemDetail = new TableDataModel();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  try
		  {
			itemDetail = itemDetailService.list();
		  }
		  catch (NoSuchFieldException e)
		  {
			// TODO Auto-generated catch block
			e.printStackTrace();
		  }
	    }
		catch (Exception e)
	    {
			logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return gson.toJson(itemDetail);
	}

	/**
	 * Respond with a JSON list of Item Details associated with a given load id.
	 *
	 * @param loadId
	 * @return JSON - TableDataModel - Item Details
	 */
	@RequestMapping(value="/byload/{loadId}",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String listByLoad(@PathVariable String loadId)
	{
		Gson gson = new Gson();
		TableDataModel itemDetailByLoad = new TableDataModel();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  try
		  {
			itemDetailByLoad = itemDetailService.listByLoadId(loadId);
		  }
		  catch (NoSuchFieldException | DBException e)
		  {
			// TODO Auto-generated catch block
			e.printStackTrace();
		  }
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return gson.toJson(itemDetailByLoad);
	}

	/**
	 * Respond with a JSON list of Item Details by given load line item id.
	 *
	 * @param itemId
	 * @return JSON - TableDataModel - Item Details
	 */
	@RequestMapping(value="/byLli/{itemId}",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String listByLoadLineItem(@PathVariable String itemId)
	{
		Gson gson = new Gson();
		TableDataModel itemDetailByLoad = new TableDataModel();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  try
		  {
			itemDetailByLoad = itemDetailService.listByLoadLineItem(itemId);
		  }
		  catch (NoSuchFieldException | DBException e)
		  {
			logger.error("(listByLoadLineItem) Exception: {}", e.getMessage());
		  }
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return gson.toJson(itemDetailByLoad);

	}
	@RequestMapping(value="/find",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String viewLoad(@RequestParam("load") String load, @RequestParam("item") String item) throws ServletException, IOException
	{

		Gson gson = new Gson();
		ItemDetailModel itemdetailDataModel = new ItemDetailModel();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
			itemdetailDataModel = itemDetailService.findLoadLineItem(load, item);
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return gson.toJson(itemdetailDataModel);
	}
	@RequestMapping(value="/add",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String add(@ModelAttribute ItemDetailModel itemDetailModel)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		   ajaxResponse = 	itemDetailService.add(itemDetailModel);
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return new Gson().toJson(ajaxResponse);
	}
	@RequestMapping(value="/modify",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String modify(@ModelAttribute ItemDetailModel itemDetailModel)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		   ajaxResponse = 	itemDetailService.modify(itemDetailModel);
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return new Gson().toJson(ajaxResponse);
	}
	
}
