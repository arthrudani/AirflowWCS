package com.daifukuamerica.wrxj.web.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.StationLoadLookupResponseModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.StoreModel;
import com.daifukuamerica.wrxj.web.service.UIService;
import com.daifukuamerica.wrxj.web.service.dao.LoadService;
import com.daifukuamerica.wrxj.web.service.dao.PurchaseOrderDetailService;
import com.daifukuamerica.wrxj.web.service.dao.PurchaseOrderService;
import com.daifukuamerica.wrxj.web.service.dao.StoreService;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Store request/response handler for performing store specific actions.
 *
 * Mapped using relative context mappings. (http://{serverhostname}:{port}/{context}/store/**)
 *
 * Author: dystout
 * Created : April 31, 2017
 *
 */
@Controller
@RequestMapping("/store")
public class StoreController
{

	/**
	 * Log4j logger: StoreController
	 */
	private static final Logger logger = LoggerFactory.getLogger(StoreController.class);

	protected AjaxResponse ajaxResponse;

	@Autowired
	UIService uiService;

	@Autowired
	StoreService storeService;

	@Autowired
	LoadService loadService;

	@Autowired
	PurchaseOrderService purchaseOrderService;

	@Autowired
	PurchaseOrderDetailService purchaseOrderDetailService;

	/**
	 * Populate the model with dropdown options for the station/container type
	 *
	 * This is called everytime the controller is utilized.
	 */
	@ModelAttribute("dropdownMenus")
	public Map<String, Object[]> getDropdowns()
	{
		Map<String, Object[]> dropdowns = new HashMap<String, Object[]>();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			dropdowns = uiService.initStoreDropDownOptions();
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
		}
		return dropdowns;
	}

	@RequestMapping("/view")
	public String view(Model model)
	{
		model.addAttribute("pageName", "STORE");
		LoadLineItemData lliData = new LoadLineItemData();
		model.addAttribute("storeModel", new StoreModel());
		model.addAttribute("lliData", lliData);
		return UIConstants.VIEW_STORE;
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String list() throws DBException
	{
		return new Gson().toJson(null);
	}

	/**
	 * Find item details for the given loadId.
	 *
	 * @return
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@RequestMapping(value = "/find", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String find(@RequestParam("loadId") String loadId) throws DBException, NoSuchFieldException
	{

		TableDataModel tdm = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			tdm = storeService.getStoreLoadTableData(loadId);
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
		}
		return new Gson().toJson(tdm);
	}

	/**
	 * Find item details for the given loadId.
	 *
	 * @return
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@RequestMapping(value = "/find/erExist", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String isExpectedReceiptExist(@RequestParam("erId") String expectedReceiptId)
			throws DBException, NoSuchFieldException
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = purchaseOrderService.exists(expectedReceiptId);
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
		}
		return new Gson().toJson(ajaxResponse);
	}

	/**
	 * Find item details for the given loadId.
	 *
	 * @return
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@RequestMapping(value = "/find/expectedReceipt", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String getExpectedReceipt(@RequestParam("erId") String expectedReceiptId)
			throws DBException, NoSuchFieldException
	{
		TableDataModel tdm = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			tdm = purchaseOrderDetailService.listDetail(expectedReceiptId);
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
		}
		return new Gson().toJson(tdm);
	}

	/**
	 * Find the load id that is marked arrived at given station id
	 *
	 * @param station
	 * @return
	 */
	@RequestMapping(value = "/stationLoad", method = RequestMethod.POST)
	@ResponseBody
	public StationLoadLookupResponseModel findLoadAtStation(@RequestParam("station") String station)
	{
		StationLoadLookupResponseModel stloadlookupModel = new StationLoadLookupResponseModel();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{

			LoadData loadData = storeService.getLoadDataAtStation(station);
			stloadlookupModel = new StationLoadLookupResponseModel(loadData.getLoadID(), loadData.getContainerType());
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
		}
		return stloadlookupModel;
	}

	/*
	 * Return empty table
	 */
	@RequestMapping(value = "/empty", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String returnEmpty()
	{
		return new Gson().toJson(new TableDataModel());
	}

	/*
	 * Return empty table
	 */
	@RequestMapping(value = "/picks", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String isPicksRemaining(@RequestParam("loadId") String loadId, @RequestParam("station") String stationId)
	{
		ajaxResponse = new AjaxResponse();
		boolean isPicksRemaining = false;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			isPicksRemaining = loadService.isPicksRemainingOnLoad(loadId);
		}
		catch (Exception e)
		{
			logger.error("Error Determining picks remaining: {}", e.getMessage());
			e.printStackTrace();
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
		}
		if (!isPicksRemaining)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "No picks remaining");
		} else
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE,
					"Picks remaining, please finish picking from <b>Pick</b> screen to release.");
		}
		return new Gson().toJson(ajaxResponse);
	}

	@RequestMapping(value = "/addItemDetail", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse addItemDetail(LoadLineItemData lliData)
	{
		return null;
	}

	/*
	 * Release load from Store Screen
	 */
	@RequestMapping(value = "/release", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String releaseStoreLoad(@RequestParam("loadId") String loadId, @RequestParam("station") String stationId,
			@RequestParam("amountFull") String amountFull)
	{
		ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			try
			{
				int iAmountFull = DBTrans.getIntegerValue(LoadData.AMOUNTFULL_NAME, amountFull);
				ajaxResponse = loadService.releaseLoad(loadId, stationId, iAmountFull);
			}
			catch (DBException | NoSuchFieldException e)
			{
				ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
				logger.error("Error releasing load: {} stationId: {} amountFull: {} ERROR: {}", loadId, stationId, amountFull, e.getMessage());
				e.printStackTrace();
			}
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
		}
		return new Gson().toJson(ajaxResponse);
	}

}
