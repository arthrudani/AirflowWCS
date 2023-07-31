package com.daifukuamerica.wrxj.web.controllers;

import java.util.HashMap;
import java.util.Map;

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
import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.web.model.User;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.PickModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.PickMoveData;
import com.daifukuamerica.wrxj.web.model.json.wrx.StationLoadLookupResponseModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.StoreModel;
import com.daifukuamerica.wrxj.web.service.UIService;
import com.daifukuamerica.wrxj.web.service.dao.LoadService;
import com.daifukuamerica.wrxj.web.service.dao.MoveService;
import com.daifukuamerica.wrxj.web.service.dao.PickService;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pick request/reponse handler for performing Pick specific actions.
 *
 * Mapped using relative context mappings. (http://{serverhostname}:{port}/{context}/pick/**)
 *
 * Author: dystout
 * Created : May 2, 2017
 *
 */
@Controller
@RequestMapping("/pick")
public class PickController
{

	/**
	* Log4j logger: PickController
	*/
	private static final Logger logger = LoggerFactory.getLogger(PickController.class);


	/**
	 * UI element rendering
	 */
	@Autowired
	UIService uiService;

	@Autowired
	PickService pickService;

	@Autowired
	LoadService loadService;

	@Autowired
	MoveService moveService;



	@RequestMapping("/view")
	public String view(Model model)
	{
		model.addAttribute("pageName", "PICK");
		model.addAttribute("moveData", new PickMoveData());
		model.addAttribute("pickModel",new PickModel());
		model.addAttribute("storeModel", new StoreModel());
		return UIConstants.VIEW_PICK;
	}

	/**
	 * Populate the model with dropdown options for the add/modify/search popups
	 *
	 * <i><b>This is called everytime the controller is utilized.<b></i>
	 */
	@ModelAttribute("dropdownMenus")
	public Map<String, Object[]> getDropdowns()
	{
		Map<String, Object[]> dropdowns = new HashMap<String, Object[]>();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
			dropdowns = uiService.initPickDropDownOptions();
	    }
		catch (Exception e)
	    {
	    	logger.error("(dropdownMenus) Error getting database connection{}{}", e.getMessage(), e.getStackTrace());
	    }
		return dropdowns;
	}

	/**
	 * Find load id at station
	 *
	 * @param stationId
	 * @return
	 */
	@RequestMapping(value="/findLoad",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public StationLoadLookupResponseModel findLoadAtStation(@RequestParam("station") String stationId)
	{
		StationLoadLookupResponseModel stlookupResponse = new StationLoadLookupResponseModel();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  LoadData loadData = loadService.getLoadDataAtStation(stationId);
		  stlookupResponse = new StationLoadLookupResponseModel(loadData.getLoadID(),loadData.getContainerType());
	    }
		catch (Exception e)
	    {
	    	logger.error("(findLoadAtStation) Error getting database connection{}{}", e.getMessage(), e.getStackTrace());
	    }
		return stlookupResponse;
	}

	/**
	 * Find associated picks for the load ID
	 *
	 * @param stationId
	 * @return
	 */
	@RequestMapping(value="/findPicks",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String findPicksForLoad(@RequestParam("loadId") String loadId)
	{
		TableDataModel tdm = new TableDataModel();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  try
		  {
			tdm = moveService.getMoveDataListByLoad(loadId);
		  }
		  catch(DBException | NoSuchFieldException e)
		  {
			logger.error("DB EXCEPTION: {}", e.getMessage());
		  }
	    }
		catch (Exception e)
	    {
	    	logger.error("(findPicksForLoad)  Error getting database connection{}{}", e.getMessage(), e.getStackTrace());
	    }
		return new Gson().toJson(tdm);
	}


	/**
	 * Find the NEXT pick for the current load at station
	 *
	 * @param stationId
	 * @return
	 */
	@RequestMapping(value="/findNextPick",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public PickMoveData findNextPickForLoad(@RequestParam("loadId") String loadId, Model model)
	{
		PickMoveData pmd = new PickMoveData();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
	  	  try
		  {
			pmd = moveService.getNextMoveForLoad(loadId);
		  }
	  	  catch (DBException e)
		  {
			logger.error(e.getMessage());
			e.printStackTrace();
		  }
	    }
		catch (Exception e)
	    {
	    	logger.error("(findNextPickForLoad) Error getting database connection{}{}", e.getMessage(), e.getStackTrace());
	    }
		return pmd;
	}

	/**
	 *	Complete pick for given move data
	 *
	 * @param stationId
	 * @return
	 */
	@RequestMapping(value="/completePick",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String completePick(@ModelAttribute PickMoveData moveData, HttpSession session, Model model)
	{
		User user = (User) session.getAttribute("user");
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  logger.debug("{} completing pick: {}", user.getUserId(), moveData.getMoveID());
		  ajaxResponse = pickService.completePick(moveData, user);
	    }
		catch (Exception e)
	    {
	    	logger.error("(completePick) Error getting database connection{}{}", e.getMessage(), e.getStackTrace());
	    }
		return new Gson().toJson(ajaxResponse);
	}








}