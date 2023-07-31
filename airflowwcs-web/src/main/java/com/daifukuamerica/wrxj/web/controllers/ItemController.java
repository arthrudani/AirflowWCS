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

import java.util.Map;

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

import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.daifukuamerica.wrxj.web.core.exceptions.AjaxException;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.ItemMasterDataModel;
import com.daifukuamerica.wrxj.web.service.UIService;
import com.daifukuamerica.wrxj.web.service.dao.ItemService;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Item Master controller for request/response handling Item actions.
 *
 * Author: dystout
 * Created : May 2, 2017
 *
 */
@Controller
@RequestMapping("/item")
public class ItemController
{
	/**
	 * Log4j logger: ItemController
	 */
	private static final Logger logger = LoggerFactory.getLogger("FILE");

	/**
	 * Service to provide load CRUD operations
	 */
	@Autowired
	private ItemService itemService;

	private AjaxResponse ajaxResponse;


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

/*	@Autowired
	private IkeaItemService itemService; */


	@ModelAttribute("itemMasterDataModel")
	public ItemMasterDataModel initItemDisplay()
	{
		return new ItemMasterDataModel();
	}

	/**
	 * Populate the model with dropdown options
	 *
	 * This is called everytime the controller is utilized.
	 */
	@ModelAttribute("dropdownMenus")
	public Map<String, Object[]> getDropdowns(){
		return uiService.initItemMasterDropDownOptions();
	}

	/**
	 * View the Item Master page
	 * @return String - logical view name of the item master jsp
	 */
	@RequestMapping("/view")
	public String view(Model model)
	{
		model.addAttribute("pageName", "Item Master ");
		model.addAttribute("itemMasterDataModel", new ItemMasterDataModel());
		return UIConstants.VIEW_ITEM;
	}

	/**
	 * List Item Masters
	 *
	 * @param item
	 * @param description
	 * @param session
	 * @return
	 * @throws DBException
	 */
	@RequestMapping(value="/listSearch",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String listSearch(@RequestParam("item") String item, @RequestParam("description") String description,
			HttpSession session) throws DBException
	{
		TableDataModel tableData = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ItemMasterData imData = new ItemMasterData();
			try
			{
				if (SKDCUtility.isNotBlank(item))
					imData.setKey(ItemMasterData.ITEM_NAME, item);

				if (SKDCUtility.isNotBlank(description))
					imData.setKey(ItemMasterData.DESCRIPTION_NAME, '%' + description, KeyObject.LIKE);

				tableData = itemService.listSearch(imData);
			}
			catch (NoSuchFieldException e)
			{
				logger.error("Exception(listSearch) : {}", e.getMessage());
			}
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
		}

		return new Gson().toJson(tableData);
	}

	/*
	@RequestMapping(value="/modifyMinMax",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String modifyMinMax(@RequestParam("itemIds[]") String[] itemIds, @RequestParam("minQty") int minQty,
			              @RequestParam("maxQty") int maxQty, HttpSession session)
	{
		Gson gson = new Gson();
		User user = (User) session.getAttribute("user");
		AjaxResponse responseCode =  itemService.modifyMinMaxQty(itemIds, minQty, maxQty, user.getUserId());
		return gson.toJson(responseCode);

	}

	@RequestMapping(value="/modifyToteQty",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String modifyToteQty(@RequestParam("itemIds[]") String[] itemIds, @RequestParam("toteQty") int toteQty, HttpSession session)
	{
		Gson gson = new Gson();
		User user = (User) session.getAttribute("user");
		AjaxResponse responseCode =  itemService.modifyToteQty(itemIds, toteQty, user.getUserId());
		return gson.toJson(responseCode);

	}

	@RequestMapping(value="/modifyDOHAuto",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String modifyDOHAuto(@RequestParam("itemIds[]") String[] itemIds, HttpSession session)
	{
		Gson gson = new Gson();
		User user = (User) session.getAttribute("user");
		AjaxResponse responseCode =  itemService.modifyDOH(itemIds, IkeaDBConstants.DOH_AUTO, user.getUserId());
		return gson.toJson(responseCode);

	}

	@RequestMapping(value="/modifyDOHManual",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String modifyDOHManual(@RequestParam("itemIds[]") String[] itemIds, HttpSession session)
	{
		Gson gson = new Gson();
		User user = (User) session.getAttribute("user");
		AjaxResponse responseCode =  itemService.modifyDOH(itemIds, IkeaDBConstants.DOH_MANUAL, user.getUserId());
		return gson.toJson(responseCode);

	}*/

}
