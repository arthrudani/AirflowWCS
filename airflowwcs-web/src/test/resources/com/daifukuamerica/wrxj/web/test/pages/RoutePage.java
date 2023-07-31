package com.daifukuamerica.wrxj.web.test.pages;

import com.daifukuamerica.wrxj.web.test.core.Browser;
import com.daifukuamerica.wrxj.web.test.core.BrowserBasedObject;
import com.daifukuamerica.wrxj.web.test.core.Navigable;
import com.daifukuamerica.wrxj.web.test.core.NavigationConstants;

public class RoutePage extends BrowserBasedObject implements Navigable
{
	public RoutePage()
	{
		super(); 
	}
	
	static String title = "ROUTES | WarehouseRx"; 

	@Override
	public void goTo()
	{
		Browser.goTo(NavigationConstants.VIEW_ROUTE); 
	}

	@Override
	public boolean isAt()
	{
		// TODO Auto-generated method stub
		return Browser.title().equals(title);
	}
}