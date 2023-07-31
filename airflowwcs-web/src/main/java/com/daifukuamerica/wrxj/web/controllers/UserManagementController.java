package com.daifukuamerica.wrxj.web.controllers;

import java.util.HashMap;
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

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.web.core.exceptions.AjaxException;
import com.daifukuamerica.wrxj.web.model.User;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.UserModel;
import com.daifukuamerica.wrxj.web.service.UIService;
import com.daifukuamerica.wrxj.web.service.dao.UserManagementService;
import com.daifukuamerica.wrxj.web.service.dao.UserPermissionService;
import com.daifukuamerica.wrxj.web.service.dao.UserPreferenceService;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User Management request/response handler for performing User Management specific actions.
 *
 * Mapped using relative context mappings. (http://{serverhostname}:{port}/{context}/users/**)
 *
 * Author: dystout
 * Created : May 31, 2017
 *
 */
@Controller
@RequestMapping("/users")
public class UserManagementController
{
	/**
	 * Log4j logger: UserManagementController
	 */
	private static final Logger logger = LoggerFactory.getLogger("USER");

	@ModelAttribute("dropdownMenus")
	public Map<String, Object[]> getDropdowns()
	{
		Map<String, Object[]> dropdowns = new HashMap<String, Object[]>();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			dropdowns = uiService.initUserDropDownOptions();
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
		}
		return dropdowns;
	}

	/**
	 * Field length for add dialog
	 * @return
	 */
	@ModelAttribute("maxLengthUserId")
	public int maxLengthUserId()
	{
		return DBInfo.getFieldLength("SUSERID");
	}

	/**
	 * Field length for add dialog
	 * @return
	 */
	@ModelAttribute("maxLengthUserName")
	public int maxLengthUserName()
	{
		return DBInfo.getFieldLength("SUSERNAME");
	}

	@RequestMapping("/view")
	public String view(Model model)
	{
		model.addAttribute("pageName", "USER MANAGEMENT");
		model.addAttribute("userModel", new UserModel()); // add blank userModel for spring forms
		model.addAttribute("userPasswordModel", new UserModel()); // add blank userModel for password reset form
		return UIConstants.VIEW_USERS;
	}

	@ExceptionHandler(AjaxException.class)
	@RequestMapping(produces="application/json; charset=utf-8")
	@ResponseBody
	public String handleAjaxError(Exception ex){
		AjaxResponse ajaxResponse = new AjaxResponse(AjaxResponseCodes.FAILURE, "An exception occured in the ajax call: " + ex.getMessage());
		Gson gson = new Gson();
		return gson.toJson(ajaxResponse);
	}

	@Autowired
	UserManagementService userService;

    @Autowired
    UserPermissionService userPermissionService;

    @Autowired
    UserPreferenceService userPreferenceService;

	@Autowired
	UIService uiService;

	@RequestMapping(value="/list",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String list() throws AjaxException
	{
		Gson gson = new Gson();
		TableDataModel tableData = null;
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			try
			{
				tableData = userService.list();
			}
			catch (DBException | NoSuchFieldException e)
			{
				ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Error occured getting users: " + e.getMessage());
			}
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
		}
		return gson.toJson(tableData);
	}

	@RequestMapping(value="/add",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String addUser(@ModelAttribute UserModel userModel) throws AjaxException
	{
		Gson gson = new Gson();
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = userService.add(userModel);
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
		}
		return gson.toJson(ajaxResponse);
	}

	@RequestMapping(value="/update",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse updateUser(@ModelAttribute UserModel userModel) throws AjaxException
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = userService.update(userModel);
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
		}
		return ajaxResponse;
	}


	@RequestMapping(value="/updatePassword", method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public AjaxResponse updateUserPassword(@ModelAttribute UserModel userModel) throws AjaxException
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			ajaxResponse = userService.updatePassword(userModel);
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
		}
		return ajaxResponse;
	}


	@RequestMapping(value="/delete", method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String deleteUser(@RequestParam("user") String userId, HttpSession session) throws AjaxException
	{
	    User user = (User) session.getAttribute("user");
		Gson gson = new Gson();
		AjaxResponse ajaxResponse = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
		    // This is a bit of a mess because it is partly in hibernate and partly in WRx
		    // I'd redo this if this wasn't throw-away code.
		  
            // Delete User
            ajaxResponse = userService.delete(user.getUserId(), userId);
            if (ajaxResponse.getResponseCode() != AjaxResponseCodes.FAILURE)
            {
                // Delete Preferences
                userPreferenceService.deleteUser(userId);
                userPermissionService.deleteUser(userId);
            }
		}
		catch (Exception e)
		{
            logger.error("Error deleting user=[{}]", userId, e);
            ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, 
                "Error deleting user=[" + userId + "]:" + e.getMessage());
		}
		return gson.toJson(ajaxResponse);
	}

	@RequestMapping(value="/find",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String findUser(@RequestParam("user") String userId) throws AjaxException
	{
		Gson gson = new Gson();
		UserModel userModel = new UserModel();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			userModel= userService.find(userId);
			userModel.setConfirmPassword(userModel.getPassword());
		}
		catch (Exception e)
		{
			logger.error("Error getting database connection{}", e.getMessage(), e);
		}
		return gson.toJson(userModel);
	}

	//TODO add a way to delete user preferences as another user so you can restore default user preferences without logging in as the user

}