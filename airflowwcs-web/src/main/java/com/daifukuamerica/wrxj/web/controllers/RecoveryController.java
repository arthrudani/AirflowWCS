package com.daifukuamerica.wrxj.web.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.PickModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.PickMoveData;
import com.daifukuamerica.wrxj.web.model.json.wrx.StoreModel;
import com.daifukuamerica.wrxj.web.service.UIService;
import com.daifukuamerica.wrxj.web.service.dao.RecoveryService;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Recovery request/reponse handler for performing Recovery specific actions.
 *
 * Mapped using relative context mappings. (http://{serverhostname}:{port}/{context}/recovery/**)
 *
 * Author: dystout
 * Created : May 2, 2017
 *
 */
@Controller
@RequestMapping("/recovery")
public class RecoveryController
{

	/**
	* Log4j logger: RecoveryController
	*/
	private static final Logger logger = LoggerFactory.getLogger(RecoveryController.class);


	/**
	 * UI element rendering
	 */
	@Autowired
	UIService uiService;

	@Autowired
	RecoveryService recoveryService;


	@RequestMapping("/view")
	public String view(Model model)
	{
		model.addAttribute("pageName", "RECOVERY");
		model.addAttribute("moveData", new PickMoveData());
		model.addAttribute("pickModel",new PickModel());
		model.addAttribute("storeModel", new StoreModel());
		return UIConstants.VIEW_RECOVERY;
	}

	/**
	 * list
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
				tableData = recoveryService.list();
			}
			catch (NoSuchFieldException e)
			{
				logger.error("Error getting recovery list{}", e.getMessage(), e);
			}
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
		}
		return gson.toJson(tableData);
	}

	/**
	 * Populate the model with dropdown options for the add/modify/search popups
	 *
	 * <i><b>This is called everytime the controller is utilized.<b></i>
	 */
	@ModelAttribute("dropdownMenus")
	public Map<String, Object[]> getDropdowns(){
		return null;
	}


	/**
	 * Autopick the specified loadId
	 *
	 * @param stationId
	 * @return
	 */
	@RequestMapping(value="/autoPick",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse autoPickLoadId(@RequestParam("loadId") String loadId)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  try
		  {
			ajaxResponse = recoveryService.autoPickLoad(loadId);
		  }
		  catch (Exception e)
		  {
			logger.error("(autoPickLoadId) Error | Exception: {}", e.getMessage());
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Error Auto-Picking load: " + e.getMessage());
		  }
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		if(ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Unable to execute internal logic. Please verify recovery conditions met.");
		return ajaxResponse;
	}
}