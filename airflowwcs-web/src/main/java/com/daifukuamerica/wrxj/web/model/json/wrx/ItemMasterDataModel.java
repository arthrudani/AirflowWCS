package com.daifukuamerica.wrxj.web.model.json.wrx;

import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO - move this class under a the model package for IKEA (project specific class)
public class ItemMasterDataModel 
{
	private static final Logger logger = LoggerFactory.getLogger("FILE");
	private String item; 

	private String description;

	private String dohFlag;
	private Integer defaultToteQty;
	
	private WebItemMasterData itemMasterData = null; 
	
	public ItemMasterDataModel()
	{
		//
	}
	
	public ItemMasterDataModel(ItemMasterData imd)
	{
		this.item = imd.getItem(); 

		this.description = imd.getDescription();

		


	}

	protected class WebItemMasterData extends ItemMasterData
	{
		public WebItemMasterData(ItemMasterDataModel imd) throws NoSuchFieldException
		{
		
		}
	}

	public String getItem()
	{
		return item;
	}

	public void setItem(String item)
	{
		this.item = item;
	}
	

	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDohFlag() {
		return dohFlag;
	}

	public void setDohFlag(String dohFlag) {
		this.dohFlag = dohFlag;
	}

	public Integer getDefaultToteQty() {
		return defaultToteQty;
	}

	public void setDefaultToteQty(Integer defaultToteQty) {
		this.defaultToteQty = defaultToteQty;
	}

	public ItemMasterData getItemMasterData() throws NoSuchFieldException
	{
		WebItemMasterData wimd = null; 
		if(this.itemMasterData == null)
		{
			wimd  = new WebItemMasterData(this); 
		}else{
			wimd = this.itemMasterData; 
		}
		return wimd; 
	}
	
	public void setLoadLineItemData(WebItemMasterData wimd)
	{
		this.itemMasterData = wimd; 
	}
	
	
}
