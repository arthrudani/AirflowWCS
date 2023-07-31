package com.daifukuamerica.wrxj.web.pages;

import com.daifukuamerica.wrxj.web.test.core.Browser;
import com.daifukuamerica.wrxj.web.test.core.BrowserBasedObject;
import com.daifukuamerica.wrxj.web.test.core.Navigable;
import com.daifukuamerica.wrxj.web.test.core.NavigationConstants;

public class ShuttleQAPage extends BrowserBasedObject implements Navigable
{
	public ShuttleQAPage()
	{
		super(); 
	}
	
	static String title = "SHUTTLE QA - WarehouseRx"; 

	@Override
	public void goTo()
	{
		Browser.goTo(NavigationConstants.VIEW_SHUTTLEQA); 
	}

	@Override
	public boolean isAt()
	{
		// TODO Auto-generated method stub
		return Browser.title().equals(title);
	}
}
