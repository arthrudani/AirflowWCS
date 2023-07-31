package com.daifukuamerica.wrxj.web.controllers;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.web.core.exceptions.AjaxException;
import com.daifukuamerica.wrxj.web.model.User;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.wrx.UserPreferenceModel;
import com.daifukuamerica.wrxj.web.service.UIService;
import com.daifukuamerica.wrxj.web.service.dao.UserPreferenceService;
import com.daifukuamerica.wrxj.web.service.dao.UserService;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * User preferences request/response handler for performing User Preference specific actions.
 *
 * Mapped using relative context mappings. (http://{serverhostname}:{port}/{context}/userpreference/**)
 *
 * Author: dystout
 * Created : May 31, 2017
 *
 */
@Controller
@RequestMapping("/userpreference")
public class UserPreferencesController
{
	private static final Logger logger = LoggerFactory.getLogger("FILE");

	@RequestMapping("/view")
	public String view(Model model, HttpSession session)
	{
		model.addAttribute("pageName", "USER PREFERENCES");
		User user = (User) session.getAttribute("user");
		UserPreferenceModel upm = new UserPreferenceModel();
		Map<String, Object[]> dropdowns = new HashMap<String, Object[]>();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  upm = userPrefService.getUserPreferences(user.getUserId());
		  dropdowns = uiService.initUserPreferencesDropDownOptions();
	    }
		catch (Exception e)
	    {
			logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		model.addAttribute("userPreference", upm);
		model.addAttribute("dropdownMenus",dropdowns);
		return UIConstants.VIEW_USER_PREFERENCES;
	}

	@Autowired
	private UIService uiService;

	@Autowired
	UserPreferenceService userPrefService;

	@Autowired
	UserService userService;

	@RequestMapping(value="/updateDebug",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse updateUser(@RequestParam("user") String userId, @RequestParam("val") String val, HttpServletRequest request) throws AjaxException
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {

		  ajaxResponse = userPrefService.updateDebugPreference(userId, val);
		  HttpSession session = request.getSession();
		  session.setAttribute("userPref", userPrefService.getUserPreferences(userId)); //immediately update user pref in session
	    }
		catch(Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
		}
		return ajaxResponse;
	}

	@RequestMapping(value="/updateLockSidebar",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse updateLockSidebar(@RequestParam("user") String userId, @RequestParam("val") String val, HttpServletRequest request) throws AjaxException
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  ajaxResponse = userPrefService.updateLockSidebarPreference(userId, val);
		  HttpSession session = request.getSession();
		  session.setAttribute("userPref", userPrefService.getUserPreferences(userId)); //immediately update user pref in session
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return ajaxResponse;
	}

	@RequestMapping(value="/updateTheme",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse updateUserTheme(@RequestParam("user") String userId, @RequestParam("val") String val, HttpServletRequest request) throws AjaxException
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  ajaxResponse = userPrefService.updateUserThemePreference(userId, val);
		  HttpSession session = request.getSession();
		  session.setAttribute("userPref", userPrefService.getUserPreferences(userId)); //immediately update user pref in session
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return ajaxResponse;
	}

	@RequestMapping(value="/updateColumnPref",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse updateColumnPref(@RequestParam("user") String userId, @RequestParam("val") String val, HttpServletRequest request) throws AjaxException
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  ajaxResponse = userPrefService.updateColumnVisibilityPreference(userId, val);
		  HttpSession session = request.getSession();
		  session.setAttribute("userPref", userPrefService.getUserPreferences(userId)); //immediately update user pref in session
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return ajaxResponse;
	}

	@RequestMapping(value="/authgroup",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String getUserAuthGroups(HttpSession session) throws AjaxException
	{
		Gson gson = new Gson();
		User user = (User) session.getAttribute("user");
		String response = "[]";
		try(WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			response = gson.toJson(userService.getUserAuthGroups(user.getUserId()));
		}
		catch(Exception e)
		{
			throw new AjaxException("Error occured getting user AuthGroups for USER "
									+ user.getUserId() + " | " + e.getMessage());
		}
		return response;
	}


}
