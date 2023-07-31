package com.daifukuamerica.wrxj.web.model.json;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HibernateTableDataModel
{
	/**
	 * Field in JSON
	 * IE: {<b>data=</b> [{LoadData},...]}
	 */
	@SerializedName("data")
	@Expose
	public List<? extends Object> tableData = new ArrayList<Object>(); 
	
	public HibernateTableDataModel(List<? extends Object> tableData)
	{
		this.tableData = tableData; 
	}
	
	public HibernateTableDataModel()
	{
		
	}

	public List<? extends Object> getTableData()
	{
		return tableData;
	}

	public void setTableData(List<Object> tableData)
	{
		this.tableData = tableData;
	}
	
	
}
