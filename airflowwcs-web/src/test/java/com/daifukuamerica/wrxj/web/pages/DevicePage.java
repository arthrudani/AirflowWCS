package com.daifukuamerica.wrxj.web.pages;
import com.daifukuamerica.wrxj.web.test.core.Browser;
import com.daifukuamerica.wrxj.web.test.core.BrowserBasedObject;
import com.daifukuamerica.wrxj.web.test.core.Navigable;
import com.daifukuamerica.wrxj.web.test.core.NavigationConstants;

public class DevicePage extends BrowserBasedObject implements Navigable
{
	public DevicePage()
	{
		super(); 
	}
	
	static String title = "DEVICE | WarehouseRx"; 

	@Override
	public void goTo()
	{
		Browser.goTo(NavigationConstants.VIEW_DEVICE); 
	}

	@Override
	public boolean isAt()
	{
		// TODO Auto-generated method stub
		return Browser.title().equals(title);
	}
}
