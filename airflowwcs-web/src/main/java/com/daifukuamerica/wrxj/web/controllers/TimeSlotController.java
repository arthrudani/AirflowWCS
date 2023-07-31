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

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.service.UIService;
import com.daifukuamerica.wrxj.web.service.dao.SysConfigService;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Administrator
 * Time slot controller handle the Timeslot business logic
 * added on : 2022-02-23
 *
 */
@Controller
@RequestMapping("/timeslot")
public class TimeSlotController {
	
	private static final Logger logger = LoggerFactory.getLogger("TimeSlot");
	private static final String defaultSchemaId = "1";
	
	@Autowired
	UIService uiService;
	
	@Autowired
	private SysConfigService sysConfigService;
	
	@RequestMapping("/view")
	public String view(Model model)
	{
		model.addAttribute("pageName", "TIME SLOT CONFIGURATION");
		//model.addAttribute("sessionColumns", new String[]{"Time","Action"});
		return UIConstants.VIEW_TIMESLOTS_CONFIG;
	}
	
	@RequestMapping(value="/list",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String list(@RequestParam("schemaId") String schemaId) throws DBException
	{
		Gson gson = new Gson();
		
		TableDataModel tableData = null;
		try(WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			if(schemaId == null || schemaId.isEmpty()){ 
				schemaId = defaultSchemaId;
			}			
			tableData = sysConfigService.listTimeSlotBySchemaId(schemaId);
		}
		catch(Exception e)
		{
			logger.error("Unable to get Time Slot for server | ERROR: {}", e.getMessage());
			e.printStackTrace(); 
		}
		return gson.toJson(tableData);
	}
	
	@RequestMapping(value = "/saveTimeslot", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String saveTimeslot(@RequestParam("timeSlot") String timeSlot, @RequestParam("schemaId") String schemaId, HttpSession session) throws DBException
	{
		AjaxResponse ajaxResponse = new AjaxResponse();		
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  ajaxResponse = sysConfigService.addTimeSlot(timeSlot, schemaId); 
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}{}", e.getMessage(), e.getStackTrace());
	    }
		return new Gson().toJson(ajaxResponse); 
	}
	
	/**
	 * Populate the model with dropdown options for the add/modify/search popups
	 * 
	 * <i><b>This is called everytime the controller is utilized.<b></i>
	 */
	@ModelAttribute("dropdownMenus")
	public Map<String, Object> getDropdowns()
	{
		Map<String,Object> dropdowns  = new HashMap<String, Object>();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  dropdowns = uiService.initSchemaDropDownOptions();
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}{}", e.getMessage(), e.getStackTrace());
	    }
		return dropdowns;
	}
	
	
	@RequestMapping(value="/delete",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse delete(@RequestParam("timeSlot") String timeSlot, @RequestParam("schemaId") String schemaId, HttpSession session)
	{
		AjaxResponse response = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
		    String[] vasTimeSlots = timeSlot.split(",");
		    for (int i = 0; i < vasTimeSlots.length; i++)
		    {
    			response = sysConfigService.deleteTimeSlot(vasTimeSlots[i].toString(), schemaId);
    			if (response.getResponseCode() != AjaxResponseCodes.SUCCESS)
    			{
    			    logger.error(response.getResponseMessage());
    		        return response;
    			}
		    }
	        response.setResponse(AjaxResponseCodes.SUCCESS, "Successfully deleted time slot"
	            + (vasTimeSlots.length > 1 ? "s" : "") +": " + timeSlot);
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
	        response.setResponse(AjaxResponseCodes.FAILURE, "Error deleting time slot: " + e.getMessage());
		}
		return response;
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

}
