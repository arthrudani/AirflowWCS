package com.daifukuamerica.wrxj.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.service.TableService;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience controller for datatables JSON responses.
 * Allows for pulling all table data at once for review on
 * page load.
 *
 * Author: dystout
 * Created : Apr 30, 2017
 *
 */
@Controller
@RequestMapping("/table")
public class TableController
{

	/**
	* Log4j logger: TableController
	*/
	private static final Logger logger = LoggerFactory.getLogger("Table");

	/**
	 * Service for db layer and json formatting
	 */
	@Autowired
	private TableService tableService;

	/**
	 * JSON response of type TableDataModel
	 *
	 * @param screen
	 * @return TableDataModel JSON
	 */
	@RequestMapping("/ajax")
	@ResponseBody
	public String getAjaxTableAll(@RequestParam("screen") String screen)
	{
		String json = null;
		try (WebDBObjectHelper dboh = new WebDBObjectHelper())
	    {
		  try
		  {
			json = tableService.getAllElementsJson(screen);
		  }
		  catch (DBException e)
		  {
			logger.error(e.getMessage());
			e.printStackTrace();
		  }
	    }
		catch (Exception e)
	    {
	    	logger.error("Error getting database connection{}", e.getMessage(), e);
	    }
		return json;
	}


	@RequestMapping("/empty")
	@ResponseBody
	public String getEmptyTable()
	{
		TableDataModel tdm = new TableDataModel();
		return new Gson().toJson(tdm);
	}




}
