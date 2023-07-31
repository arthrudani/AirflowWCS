package com.daifukuamerica.wrxj.web.test.pages;

import com.daifukuamerica.wrxj.web.test.core.Browser;
import com.daifukuamerica.wrxj.web.test.core.BrowserBasedObject;
import com.daifukuamerica.wrxj.web.test.core.Navigable;
import com.daifukuamerica.wrxj.web.test.core.NavigationConstants;

public class LocationPage extends BrowserBasedObject implements Navigable
{
	public LocationPage()
	{
		super(); 
	}
	
	static String title = "LOCATIONS | WarehouseRx"; 

	@Override
	public void goTo()
	{
		Browser.goTo(NavigationConstants.VIEW_LOCATION); 
	}

	@Override
	public boolean isAt()
	{
		// TODO Auto-generated method stub
		return Browser.title().equals(title);
	}
}