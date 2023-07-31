package com.daifukuamerica.wrxj.web.model.json.wrx;

import com.daifukuamerica.wrxj.dbadapter.data.ContainerTypeData;

public class ContainerTypeDataModel
{
	
	private String containerType; 
	private double weight; 
	private double maxWeight; 
	private double contLength; 
	private double contWidth; 
	private double contHeight; 
	
	public ContainerTypeDataModel()
	{
		//
	}
	
	
	public ContainerTypeDataModel(ContainerTypeData ctd)
	{
		this.containerType = ctd.getContainer(); 
		this.weight = ctd.getWeight(); 
		this.maxWeight = ctd.getMaxWeight(); 
		this.contLength = ctd.getContLength(); 
		this.contWidth = ctd.getContWidth(); 
		this.contHeight = ctd.getContHeight(); 
	}
	

	private WebContainerTypeData containerTypeData = null; 

	protected class WebContainerTypeData extends ContainerTypeData
	{

		public WebContainerTypeData(ContainerTypeDataModel ctdm)
		{
			super(); 
			this.setContainer(ctdm.getContainerType());
			this.setWeight(ctdm.getWeight());
			this.setMaxWeight(ctdm.getMaxWeight());
			this.setContLength(ctdm.getContLength());
			this.setContWidth(ctdm.getContWidth());
			this.setContHeight(ctdm.getContHeight());
		}
		
	}

	/**
	 * If we dont already have an instance of the 
	 * encapsulated container type data, construct one 
	 * using the current state of the outer class
	 * 
	 * @return LoadLineItemData - the encapsulated load line item data
	 * @throws NoSuchFieldException
	 */
	public ContainerTypeData getContainerTypeData() throws NoSuchFieldException
	{
		WebContainerTypeData wctd = null; 
		if(this.containerTypeData == null)
		{
			wctd  = new WebContainerTypeData(this); 
		}else{
			wctd = this.containerTypeData; 
		}
		return wctd; 
	}

	public void setContainerTypeData(WebContainerTypeData containerTypeData)
	{
		this.containerTypeData = containerTypeData;
	}


	public String getContainerType()
	{
		return containerType;
	}


	public void setContainerType(String containerType)
	{
		this.containerType = containerType;
	}


	public double getWeight()
	{
		return weight;
	}


	public void setWeight(double weight)
	{
		this.weight = weight;
	}


	public double getMaxWeight()
	{
		return maxWeight;
	}


	public void setMaxWeight(double maxWeight)
	{
		this.maxWeight = maxWeight;
	}


	public double getContLength()
	{
		return contLength;
	}


	public void setContLength(double contLength)
	{
		this.contLength = contLength;
	}


	public double getContWidth()
	{
		return contWidth;
	}


	public void setContWidth(double contWidth)
	{
		this.contWidth = contWidth;
	}


	public double getContHeight()
	{
		return contHeight;
	}


	public void setContHeight(double contHeight)
	{
		this.contHeight = contHeight;
	}
	
	
}
