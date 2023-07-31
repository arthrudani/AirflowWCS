/**
 * 
 */
package com.daifukuamerica.wrxj.web.model.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

/**
 * Author: dystout
 * Created : Apr 6, 2017
 * 
 * JSON used to populate Tables w/DataTable API
 * need field "data" to bind to DataTable's json
 * response handler.  
 * 
 * Accepts a List<Map> which is most of the return types
 * for WRxJ data access methods. 
 */
public class TableDataModel
{
	/**
	 * Field in JSON
	 * IE: {<b>data=</b> [{LoadData},...]}
	 */
	@SerializedName("data")
	public List<Map> tableData = new ArrayList<Map>(); 
	
	public TableDataModel(List<Map> tableData)
	{
		this.tableData = tableData; 
	}
	
	public TableDataModel()
	{
		
	}

	public List<Map> getTableData()
	{
		return tableData;
	}

	public void setTableData(List<Map> tableData)
	{
		this.tableData = tableData;
	}
	
	
	

}