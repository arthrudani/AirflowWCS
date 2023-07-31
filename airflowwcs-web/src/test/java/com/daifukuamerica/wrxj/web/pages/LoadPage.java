package com.daifukuamerica.wrxj.web.pages;

import com.daifukuamerica.wrxj.web.test.core.Browser;
import com.daifukuamerica.wrxj.web.test.core.BrowserBasedObject;
import com.daifukuamerica.wrxj.web.test.core.Navigable;
import com.daifukuamerica.wrxj.web.test.core.NavigationConstants;

public class LoadPage extends BrowserBasedObject implements Navigable
{
	public LoadPage()
	{
		super(); 
	}
	
	static String title = "LOADS | WarehouseRx"; 

	@Override
	public void goTo()
	{
		Browser.goTo(NavigationConstants.VIEW_LOAD); 
	}

	@Override
	public boolean isAt()
	{
		// TODO Auto-generated method stub
		return Browser.title().equals(title);
	}
}
