package com.daifukuamerica.wrxj.web.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.web.core.StackTraceFilter;
import com.daifukuamerica.wrxj.web.model.User;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.DeviceDataModel;
import com.daifukuamerica.wrxj.web.service.UIService;
import com.daifukuamerica.wrxj.web.service.dao.DeviceService;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Device  controller for handling requests/response for Device actions.
 *
 * Author: dystout
 * Created : May 2, 2017
 *
 */
@Controller
@RequestMapping("/device")
public class DeviceController
{
	private static final Logger logger = LoggerFactory.getLogger("Device");

	@Autowired
	private UIService uiService;
    @Autowired
    private DeviceService deviceService;

    /**
     * Model for modify
     * @return
     */
    @ModelAttribute("deviceModel")
    public DeviceDataModel initDisplay()
    {
        return new DeviceDataModel();
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
      try //(WebDBObjectHelper dboh = new WebDBObjectHelper())
      {
        // No DB connection
        dropdowns = uiService.initDeviceDropDownOptions();
      }
      catch (Exception e)
      {
        logger.error("Error filling dropdown lists", StackTraceFilter.filter(e));
      }
      return dropdowns;
    }

	/**
	 * View the device page.
	 * @return String - logical view name of device view
	 */
	@RequestMapping("/view")
	public String view(Model model)
	{
		model.addAttribute("pageName", "DEVICES");
		String[] highlightConditions = {
		    "Operational Status,(Online),highlight-cell-background-success",
		    "Operational Status,(Offline),highlight-cell-background-failure",
		    "Operational Status,(Inoperable),highlight-cell-background-warning"
		};
		model.addAttribute("regexHighlights",  highlightConditions);
		return UIConstants.VIEW_DEVICE;
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
    public String find(@RequestParam("deviceId") String deviceId, Model model) throws ServletException, IOException
    {
        DeviceDataModel dataModel = null;
        try (WebDBObjectHelper dboh = new WebDBObjectHelper())
        {
            dataModel = deviceService.find(deviceId);
        }
        catch (Exception e)
        {
            logger.error("Error getting database connection", e);
        }
        return new Gson().toJson(dataModel);
    }

	/**
	 * Respond with a JSON list of Devices in DataTable format.
	 *
	 * @return JSON - TableDataModel - Devices
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@RequestMapping(value="/list",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String list() throws DBException, NoSuchFieldException
	{
		Gson gson = new Gson();
		TableDataModel tableData = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {

		  tableData = deviceService.list();
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection", e);
	    }
		return gson.toJson(tableData);
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
    public String modify(@ModelAttribute DeviceDataModel dataModel, HttpSession session)
    {
        AjaxResponse ajaxResponse = new AjaxResponse();
        User user = (User) session.getAttribute("user");
        try (WebDBObjectHelper dboh = new WebDBObjectHelper())
        {
            logger.info("UserID=[{}] is modifying Device=[{}]", user.getUserId(), dataModel.getDeviceId());
            ajaxResponse = deviceService.modify(dataModel);
        }
        catch (Exception e)
        {
            logger.error("Error getting database connection", e);
            ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
        }
        return new Gson().toJson(ajaxResponse);
    }
}