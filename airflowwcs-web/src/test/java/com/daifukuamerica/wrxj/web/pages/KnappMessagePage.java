package com.daifukuamerica.wrxj.web.pages;

import com.daifukuamerica.wrxj.web.test.core.Browser;
import com.daifukuamerica.wrxj.web.test.core.BrowserBasedObject;
import com.daifukuamerica.wrxj.web.test.core.Navigable;
import com.daifukuamerica.wrxj.web.test.core.NavigationConstants;

public class KnappMessagePage extends BrowserBasedObject implements Navigable
{
	public KnappMessagePage()
	{
		super(); 
	}
	
	static String title = "KNAPP QUEUED MESSAGES | WarehouseRx"; 

	@Override
	public void goTo()
	{
		Browser.goTo(NavigationConstants.VIEW_KNAPPMESSAGES); 
	}

	@Override
	public boolean isAt()
	{
		// TODO Auto-generated method stub
		return Browser.title().equals(title);
	}
}
