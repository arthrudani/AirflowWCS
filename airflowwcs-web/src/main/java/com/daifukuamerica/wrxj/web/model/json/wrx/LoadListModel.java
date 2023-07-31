package com.daifukuamerica.wrxj.web.model.json.wrx;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * @author dystout
 * 
 * GSON Base <code>data</code> encapsulation model of Load
 *
 */
public class LoadListModel 
{
	/**
	 * Field in JSON
	 * <b>data:</b> [{GLoadData},...]
	 */
	@SerializedName("data")
	public List<LoadDataModel> loadData = new ArrayList<LoadDataModel>(); 
	

}