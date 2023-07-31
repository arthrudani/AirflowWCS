package com.daifukuamerica.wrxj.web.model.json.wrx;

public class StationLoadLookupResponseModel
{
	public StationLoadLookupResponseModel()
	{
		//
	}
	public StationLoadLookupResponseModel(String loadId, String containerType)
	{
		this.loadId = loadId; 
		this.containerType = containerType;
	}
	
	private String loadId;
	private String containerType;  
	
	public String getLoadId(){return loadId;}
	public void setLoadId(String loadId){this.loadId = loadId;}
	public String getContainerType(){return containerType;}
	public void setContainerType(String containerType){this.containerType = containerType;} 
}
