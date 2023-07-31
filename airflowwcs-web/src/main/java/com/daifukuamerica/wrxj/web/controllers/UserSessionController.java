package com.daifukuamerica.wrxj.web.controllers;


import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.web.model.User;
import com.daifukuamerica.wrxj.web.model.json.HibernateTableDataModel;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.service.UIService;
import com.daifukuamerica.wrxj.web.service.dao.UserService;
import com.daifukuamerica.wrxj.web.service.dao.UserSessionService;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Controller
@RequestMapping("/usersession")
public class UserSessionController
{

	private static final Logger logger = LoggerFactory.getLogger(UserSessionController.class);

	@RequestMapping("/view")
	public String view(Model model, HttpSession session)
	{
		model.addAttribute("pageName", "USER SESSIONS");
		model.addAttribute("sessionColumns", new String[]{"User Session","IP","Machine Name","Granted User Access","Login Time"});
		String[] highlightConditions = {"Granted User Access,(ROLE_ADMIN),highlight-cell-background-success","Granted User Access,(ROLE_MASTER),highlight-cell-background-success"};
		model.addAttribute("regexHighlights",  highlightConditions);
		User user = (User) session.getAttribute("user");

//		model.addAttribute("dropdownMenus",uiService.initUserPreferencesDropDownOptions()); //TODO dropdown options
//
		return UIConstants.VIEW_USER_SESSION;
	}

	@Autowired
	private UIService uiService;


	@Autowired
	UserService userService;

	@Autowired
	UserSessionService userSessionService;

	@RequestMapping("/empty")
	@ResponseBody
	public String getEmptyTable()
	{
		TableDataModel tdm = new TableDataModel();
		return new Gson().toJson(tdm);
	}


	@RequestMapping(value="/list",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String list() throws DBException
	{
		Gson gson = new Gson();
		HibernateTableDataModel tableData = null;
		try(WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			tableData = new HibernateTableDataModel(userSessionService.getUserSessions());
		}
		catch(Exception e)
		{
			logger.error("Unable to get User Sessions for server | ERROR: {}", e.getMessage());
			e.printStackTrace(); //TODO remove
		}
		return gson.toJson(tableData);
	}

	/**
	 * Get the login counts for the active sessions.
	 * @return
	 * @throws DBException
	 */
	@RequestMapping(value="/count",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String count() throws DBException
	{
		Gson gson = new Gson();
		Map<String,Long> userSessions = new HashMap<String,Long>();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
			userSessions = userSessionService.getUserSessionCounts();
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return gson.toJson(userSessions);
	}

//
//	@RequestMapping(value="/updateDebug",method=RequestMethod.POST, produces="application/json; charset=utf-8")
//	@ResponseBody
//	public AjaxResponse updateUser(@RequestParam("user") String userId, @RequestParam("val") String val, HttpServletRequest request) throws AjaxException
//	{
//		Gson gson = new Gson();
//		AjaxResponse ajaxResponse = new AjaxResponse();
//		ajaxResponse = userPrefService.updateDebugPreference(userId, val);
//		HttpSession session = request.getSession();
//		session.setAttribute("userPref", userPrefService.getUserPreferences(userId)); //immediately update user pref in session
//		return ajaxResponse;
//	}
//
//	@RequestMapping(value="/updateLockSidebar",method=RequestMethod.POST, produces="application/json; charset=utf-8")
//	@ResponseBody
//	public AjaxResponse updateLockSidebar(@RequestParam("user") String userId, @RequestParam("val") String val, HttpServletRequest request) throws AjaxException
//	{
//		Gson gson = new Gson();
//		AjaxResponse ajaxResponse = new AjaxResponse();
//		ajaxResponse = userPrefService.updateLockSidebarPreference(userId, val);
//		HttpSession session = request.getSession();
//		session.setAttribute("userPref", userPrefService.getUserPreferences(userId)); //immediately update user pref in session
//		return ajaxResponse;
//	}
//
//	@RequestMapping(value="/updateTheme",method=RequestMethod.POST, produces="application/json; charset=utf-8")
//	@ResponseBody
//	public AjaxResponse updateUserTheme(@RequestParam("user") String userId, @RequestParam("val") String val, HttpServletRequest request) throws AjaxException
//	{
//		Gson gson = new Gson();
//		AjaxResponse ajaxResponse = new AjaxResponse();
//		ajaxResponse = userPrefService.updateUserThemePreference(userId, val);
//		HttpSession session = request.getSession();
//		session.setAttribute("userPref", userPrefService.getUserPreferences(userId)); //immediately update user pref in session
//		return ajaxResponse;
//	}
//
//	@RequestMapping(value="/updateColumnPref",method=RequestMethod.POST, produces="application/json; charset=utf-8")
//	@ResponseBody
//	public AjaxResponse updateColumnPref(@RequestParam("user") String userId, @RequestParam("val") String val, HttpServletRequest request) throws AjaxException
//	{
//		Gson gson = new Gson();
//		AjaxResponse ajaxResponse = new AjaxResponse();
//		ajaxResponse = userPrefService.updateColumnVisibilityPreference(userId, val);
//		HttpSession session = request.getSession();
//		session.setAttribute("userPref", userPrefService.getUserPreferences(userId)); //immediately update user pref in session
//		return ajaxResponse;
//	}
//
//	@RequestMapping(value="/authgroup",method=RequestMethod.GET, produces="application/json; charset=utf-8")
//	@ResponseBody
//	public String getUserAuthGroups(HttpSession session) throws AjaxException
//	{
//		Gson gson = new Gson();
//		User user = (User) session.getAttribute("user");
//		String response = "[]";
//		try
//		{
//			response = gson.toJson(userService.getUserAuthGroups(user.getUserId()));
//		}catch(Exception e)
//		{
//			throw new AjaxException("Error occured getting user AuthGroups for USER "
//									+ user.getUserId() + " | " + e.getMessage());
//		}
//		return response;
//	}


}