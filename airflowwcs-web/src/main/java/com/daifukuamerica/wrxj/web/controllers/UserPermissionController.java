package com.daifukuamerica.wrxj.web.controllers;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.web.core.exceptions.AjaxException;
import com.daifukuamerica.wrxj.web.model.User;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.HibernateTableDataModel;
import com.daifukuamerica.wrxj.web.service.dao.UserPermissionService;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/userpermission")
public class UserPermissionController
{
	public static final Gson PRETTY_PRINT_JSON = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
	private static final Logger logger = LoggerFactory.getLogger(UserPermissionController.class);


	@RequestMapping("/view")
	public String view(Model model, HttpSession session)
	{
		model.addAttribute("pageName", "USER PERMISSIONS");
		model.addAttribute("hidePermColumns", new String[]{ "id"});
		model.addAttribute("ajaxColumns", "Group Name,Number of Users");
		model.addAttribute("allUsersColumns", new String[]{"User","Granted Access"});
		model.addAttribute("inGroupColumns", new String[]{"User"});//must be ordered same as data returned
		model.addAttribute("tableButton", new String[]{"Button","Button2"});
		return UIConstants.VIEW_USER_PERMISSION;
	}

	@Autowired
	UserPermissionService userPermissionService;

	@RequestMapping(value="/list",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String list() throws AjaxException
	{
		HibernateTableDataModel tableData = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		   tableData = new HibernateTableDataModel(userPermissionService.getAuthGroups());
		}
		catch (Exception  e)
		{
			throw new AjaxException("Error occured getting user permissions: " + e.getMessage());
		}
		return PRETTY_PRINT_JSON.toJson(tableData);
	}


	@RequestMapping(value="/listcustom/{columns}",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String listCustom(@PathVariable String[] columns) throws AjaxException
	{
		HibernateTableDataModel tableData = null;
		try(WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			tableData = new HibernateTableDataModel(userPermissionService.getUserAuthGroupCount(columns));
		}
		catch (Exception  e)
		{
			throw new AjaxException("Error occured getting user permissions: " + e.getMessage());
		}
		return PRETTY_PRINT_JSON.toJson(tableData);
	}

	@RequestMapping(value="/count/users",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String countUsers() throws AjaxException
	{
		return PRETTY_PRINT_JSON.toJson(userPermissionService.getUserCount());
	}


	@RequestMapping(value="/users/empty",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String emptyCustomUserTable() throws AjaxException
	{
		return PRETTY_PRINT_JSON.toJson(new HibernateTableDataModel());
	}

	@RequestMapping(value="/users/all",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String getAllUser() throws AjaxException
	{
		Gson gson = new Gson();
		HibernateTableDataModel tableData = null;
		try(WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			tableData = new HibernateTableDataModel(userPermissionService.getUsers());
		}catch(Exception e)
		{
			throw new AjaxException("Error occured getting users "
									+ e.getMessage());
		}
		return gson.toJson(tableData);
	}

	@RequestMapping(value="/users/group/all",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String getAllUsersGroups() throws AjaxException
	{
		Gson gson = new Gson();
		HibernateTableDataModel tableData = null;
		try(WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			tableData = new HibernateTableDataModel(userPermissionService.getUsersAndGroups());
		}catch(Exception e)
		{
			throw new AjaxException("Error occured getting users "
									+ e.getMessage());
		}
		return gson.toJson(tableData);
	}


	@RequestMapping(value="/users/{group}",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String getUsersByGroup(@PathVariable String group) throws AjaxException
	{
		Gson gson = new Gson();
		HibernateTableDataModel tableData = null;
		try(WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			tableData = new HibernateTableDataModel(userPermissionService.getUsersByGroup(group));
		}catch(Exception e)
		{
			throw new AjaxException("Error occured getting users for group "
									+ group + " | " + e.getMessage());
		}
		return gson.toJson(tableData);
	}



	@RequestMapping(value="/users/notGroup/{group}",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String getUsersNotInGroup(@PathVariable String group) throws AjaxException
	{
		Gson gson = new Gson();
		HibernateTableDataModel tableData = null;
		try(WebDBObjectHelper dboh = new WebDBObjectHelper())
		{
			tableData = new HibernateTableDataModel(userPermissionService.getUsersNotInGroup(group));
		}catch(Exception e)
		{
			throw new AjaxException("Error occured getting users "
									+ e.getMessage());
		}
		return gson.toJson(tableData);
	}

	@RequestMapping(value="/users/add/{group}",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String addUsersToGroup(@RequestParam("users[]") String[] users, @PathVariable String group, HttpSession session)
	{
		Gson gson = new Gson();
		User user = (User) session.getAttribute("user");
		AjaxResponse responseCode = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {

		  for (String addUser : users)
		  {
			logger.info("User[{}] adding user [ {}] to group [{}]", user.getUserId(), addUser, group);
		  }
		  responseCode = userPermissionService.addUsersToGroup(users,group);
	    }
		catch (Exception e)
	    {
			responseCode.setResponse(AjaxResponseCodes.FAILURE, "Error getting database connection" +e.getMessage());
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return gson.toJson(responseCode);

	}

	@RequestMapping(value="/users/revoke/{group}",method=RequestMethod.POST, produces="application/json; charset=utf-8")
	@ResponseBody
	public String deleteUsersFromGroup(@RequestParam("users[]") String[] users, @PathVariable String group, HttpSession session)
	{
		Gson gson = new Gson();
		User user = (User) session.getAttribute("user");
		AjaxResponse responseCode = new AjaxResponse();
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  for (String addUser : users)
		  {
			logger.info("User[{}] adding user [ {}] to group [{}]", user.getUserId(), addUser, group);
		  }
		  responseCode = userPermissionService.deleteUsersFromGroup(users,group);
	    }
		catch (Exception e)
	    {
			responseCode.setResponse(AjaxResponseCodes.FAILURE, "Error getting database connection" +e.getMessage());
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return gson.toJson(responseCode);

	}








}
