package com.daifukuamerica.wrxj.web.pages;

import org.openqa.selenium.By;

import com.daifukuamerica.wrxj.web.test.core.Browser;
import com.daifukuamerica.wrxj.web.test.core.BrowserBasedObject;
import com.daifukuamerica.wrxj.web.test.core.Navigable;
import com.daifukuamerica.wrxj.web.test.core.NavigationConstants;

public class LoginPage extends BrowserBasedObject implements Navigable
{

	public LoginPage()
	{
		super(); 
	}
	
	static String title = "Warehouse Rx - Client"; 

	@Override
	public void goTo()
	{
		Browser.goTo(""); 
	}

	@Override
	public boolean isAt()
	{
		// TODO Auto-generated method stub
		return Browser.title().equals(title);
	}
	
	public void loginAsrsUser()
	{
		 // open | /wrxj-web/ | 
	    Browser.driver.get(NavigationConstants.CONTEXT_URL);
	    // type | id=userName | asrs
	    Browser.driver.findElement(By.id("userName")).clear();
	    Browser.driver.findElement(By.id("userName")).sendKeys("asrs");
	    // type | id=password | asrs
	    Browser.driver.findElement(By.id("password")).clear();
	    Browser.driver.findElement(By.id("password")).sendKeys("asrs");
	    // click | //button[@type='submit'] | 
	    Browser.driver.findElement(By.xpath("//button[@type='submit']")).click();
	}
}
