package com.daifukuamerica.wrxj.web.controllers;


import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.web.model.User;
import com.daifukuamerica.wrxj.web.model.json.HibernateTableDataModel;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Controller
@RequestMapping("/loadcounts")
public class LoadCountsController
{
	
	private static final Logger logger = LoggerFactory.getLogger("FILE");

	@RequestMapping("/view")
	public String view(Model model, HttpSession session)
	{ 
		model.addAttribute("pageName", "WRX LOAD Counts"); 
		model.addAttribute("ieColumns", new String[]{"Station","Error Count"}); 
		model.addAttribute("iepColumns", new String[]{"Station","Package ID","Error Count"}); 
		User user = (User) session.getAttribute("user"); 
	
		return UIConstants.VIEW_REPORTS_LOADCOUNTS; 
	}
	
	/*@Autowired
	private IkeaLoadCountService loadCountService; 
	
	
	@RequestMapping("/empty")
	@ResponseBody
	public String getEmptyTable()
	{
		TableDataModel tdm = new TableDataModel(); 
		return new Gson().toJson(tdm);
	}
	
	@RequestMapping(value="/listInductErrors",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String listInductErrors() throws DBException
	{
		Gson gson = new Gson(); 
		HibernateTableDataModel tableData = null;
		try{
			tableData = new HibernateTableDataModel(loadCountService.getInductErrorCounts()); 
		}catch(Exception e)
		{
			logger.error("Unable to get Induction Error Counts | ERROR: " + e.getMessage());
		}
		return gson.toJson(tableData); 
	}
	
	@RequestMapping(value="/listInductErrorsPalletId",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String listInductErrorsPalletId() throws DBException
	{
		Gson gson = new Gson(); 
		HibernateTableDataModel tableData = null;
		try{
			tableData = new HibernateTableDataModel(loadCountService.getInductErrorCountsPalletId()); 
		}catch(Exception e)
		{
			logger.error("Unable to get Induction Error Counts by Pallet ID| ERROR: " + e.getMessage());
		}
		return gson.toJson(tableData); 
	}
	
	@RequestMapping(value="/loadCountToday",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String loadCountToday() throws DBException
	{
		LoadCountLookUpResponse response = new LoadCountLookUpResponse();
		int loadCountsToday = loadCountService.getTotalTodayLoadCounts();
		response.setTotal_today_count(loadCountsToday);

		return new Gson().toJson(response); 
	}
	
	@RequestMapping(value="/goodInductionCountToday",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String goodInductionCountToday() throws DBException
	{
		LoadCountLookUpResponse response = new LoadCountLookUpResponse();
		int goodInductionCountToday = loadCountService.getTotalGoodInductionCountsToday();
		response.setTotal_goodinduction_count(goodInductionCountToday);

		return new Gson().toJson(response); 
	}
	
	@RequestMapping(value="/errorInductionCountToday",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String errorInductionCountToday() throws DBException
	{
		LoadCountLookUpResponse response = new LoadCountLookUpResponse();
		int errorInductionCountToday = loadCountService.getTotalErrorInductionCountsToday();
		response.setTotal_errorinduction_count(errorInductionCountToday);

		return new Gson().toJson(response); 
	}

	@RequestMapping(value="/movingLoadCountToday",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String movingLoadCountToday() throws DBException
	{
		LoadCountLookUpResponse response = new LoadCountLookUpResponse();
		int movingLoadCountToday = loadCountService.getTotalMovingLoadCountsToday();
		response.setTotal_movingloads_count(movingLoadCountToday);

		return new Gson().toJson(response); 
	}
	
	@RequestMapping(value="/loadCountWarhse",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String loadCountWarhse() throws DBException
	{
		LoadCountLookUpResponse response = new LoadCountLookUpResponse();
		IkeaWebLoadCountsData loadCountsData = loadCountService.getLoadCountsByWarhse();
		if (loadCountsData != null)
		{
			response.setKnp_count(loadCountsData.getKNPCount());
			response.setS1_count(loadCountsData.getS1Count());
			response.setS2_count(loadCountsData.getS2Count());
			response.setLtw_count(loadCountsData.getLTWCount());
			response.setTotal_count(loadCountsData.getKNPCount() + loadCountsData.getS1Count() + loadCountsData.getS2Count() + loadCountsData.getLTWCount());
		}
		else
		{
			response.setKnp_count(0);
			response.setS1_count(0);
			response.setS2_count(0);
			response.setLtw_count(0);	
			response.setTotal_count(0);
		}
		return new Gson().toJson(response); 
	}
	
	@RequestMapping(value="/loadCountAisle",method=RequestMethod.GET, produces="application/json; charset=utf-8")
	@ResponseBody
	public String loadCountAisle() throws DBException
	{
		LoadCountLookUpResponse response = new LoadCountLookUpResponse();
		IkeaWebLoadCountsData loadCountsData = loadCountService.getLoadCountsByDevice();
		if (loadCountsData != null)
		{
			response.setAisle1_count(loadCountsData.getAisle1Count());
			response.setAisle2_count(loadCountsData.getAisle2Count());
			response.setAisle3_count(loadCountsData.getAisle3Count());
			response.setAisle4_count(loadCountsData.getAisle4Count());
			response.setAisle5_count(loadCountsData.getAisle5Count());
			response.setAisle6_count(loadCountsData.getAisle6Count());
			response.setAisle7_count(loadCountsData.getAisle7Count());
			response.setAisle8_count(loadCountsData.getAisle8Count());
			response.setAisle9_count(loadCountsData.getAisle9Count());
			response.setAisle10_count(loadCountsData.getAisle10Count());
			response.setAisle11_count(loadCountsData.getAisle11Count());
			response.setAisle12_count(loadCountsData.getAisle12Count());
			response.setAisle13_count(loadCountsData.getAisle13Count());
			response.setAisle14_count(loadCountsData.getAisle14Count());
			response.setAisle15_count(loadCountsData.getAisle15Count());
			response.setAisle16_count(loadCountsData.getAisle16Count());
			response.setAisle17_count(loadCountsData.getAisle17Count());
			response.setAisle18_count(loadCountsData.getAisle18Count());
			response.setAisle19_count(loadCountsData.getAisle19Count());
			response.setAisle20_count(loadCountsData.getAisle20Count());
			response.setAisle21_count(loadCountsData.getAisle21Count());
			response.setAisle22_count(loadCountsData.getAisle22Count());
			response.setAisle23_count(loadCountsData.getAisle23Count());
			response.setAisle24_count(loadCountsData.getAisle24Count());
			response.setLtw1_count(loadCountsData.getLTW1Count());
			response.setLtw2_count(loadCountsData.getLTW2Count());
			response.setLtw3_count(loadCountsData.getLTW3Count());
			response.setLtw4_count(loadCountsData.getLTW4Count());
			response.setLtw5_count(loadCountsData.getLTW5Count());

		}
		else
		{
			response.setAisle1_count(0);
			response.setAisle2_count(0);
			response.setAisle3_count(0);
			response.setAisle4_count(0);
			response.setAisle5_count(0);
			response.setAisle6_count(0);
			response.setAisle7_count(0);
			response.setAisle8_count(0);
			response.setAisle9_count(0);
			response.setAisle10_count(0);
			response.setAisle11_count(0);
			response.setAisle12_count(0);
			response.setAisle13_count(0);
			response.setAisle14_count(0);
			response.setAisle15_count(0);
			response.setAisle16_count(0);
			response.setAisle17_count(0);
			response.setAisle18_count(0);
			response.setAisle19_count(0);
			response.setAisle20_count(0);
			response.setAisle21_count(0);
			response.setAisle22_count(0);
			response.setAisle23_count(0);
			response.setAisle24_count(0);
			response.setLtw1_count(0);
			response.setLtw2_count(0);
			response.setLtw3_count(0);
			response.setLtw4_count(0);
			response.setLtw5_count(0);		
		}
		return new Gson().toJson(response); 
	}*/
	
}