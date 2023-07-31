package com.daifukuamerica.wrxj.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.service.dao.ContainerService;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container controller for handling requests/response for Container actions.
 *
 * Mapped using relative context mappings.
 *
 * Author: dystout
 * Created : May 2, 2017
 *
 */
@Controller
@RequestMapping("/container")
public class ContainerController
{
	/**
	 * Log4j logger: Container
	 */
	private static final Logger logger = LoggerFactory.getLogger("Container");


	/**
	 * View the Container page
	 * @return logical view name
	 */
	@RequestMapping("/view")
	public String view(Model model)
	{
		model.addAttribute("pageName", "CONTAINERS");
		return UIConstants.VIEW_CONTAINER;
	}

	@Autowired
	ContainerService containerService;

	/**
	 * Return a JSON list of containers.
	 *
	 * @return JSON - TableDataModel - Containers
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
		  tableData = containerService.list();
	    }
		catch (Exception e)
	    {
			 logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return gson.toJson(tableData);
	}
}
