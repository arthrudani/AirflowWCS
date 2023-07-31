package com.daifukuamerica.wrxj.web.test.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.Select;

import com.daifukuamerica.wrxj.web.test.core.Browser;
import com.daifukuamerica.wrxj.web.test.core.BrowserBasedObject;
import com.daifukuamerica.wrxj.web.test.core.Navigable;
import com.daifukuamerica.wrxj.web.test.core.NavigationConstants;
import com.daifukuamerica.wrxj.web.ui.UIConstants;

public class ShuttlePage extends BrowserBasedObject implements Navigable
{
	public ShuttlePage()
	{
		super(); 
	}
	
	static String title = "SHUTTLE PICK | WarehouseRx"; 

	@Override
	public void goTo()
	{
		Browser.goTo(NavigationConstants.VIEW_SHUTTLE); 
	}

	@Override
	public boolean isAt()
	{
		// TODO Auto-generated method stub
		return Browser.title().equals(title);
	}
	
	public void scanToteAtStation(String tote, String station)
	{
		goTo(); 
		Browser.waitForPageLoaded();
		 // select | id=station 
	    new Select(Browser.driver.findElement(By.id("station"))).selectByVisibleText(station);
	    // click | id=confirm-client-lock-button | 
	    Browser.driver.findElement(By.id("confirm-client-lock-button")).click();
	    // type | id=fromTote 
	    Browser.driver.findElement(By.id("fromTote")).clear();
	    Browser.driver.findElement(By.id("fromTote")).sendKeys(tote);
	    // sendKeys | id=fromTote | ${KEY_ENTER}
	    Browser.driver.findElement(By.id("fromTote")).sendKeys(Keys.ENTER);
	    // verifyValue | id=sscid 
	}
	
	public boolean isWallLocationInputEnabled()
	{
		return Browser.driver.findElement(By.id("wallLocation")).isEnabled(); 
	}
	
	public boolean isItemConfirmInputEnabled()
	{
		return Browser.driver.findElement(By.id("confirmItem")).isEnabled(); 
	}
	
	public boolean isQuantityConfirmInputEnabled()
	{
		return Browser.driver.findElement(By.id("confirmPickQuantity")).isEnabled(); 
	}
	
	public boolean hasPickToLoadId()
	{
		return Browser.driver.findElement(By.id("pickToLoadId")).getText()!=""; 
	}
	
	public void scanPutWallLocation(String putWallLocationId)
	{
		Browser.driver.findElement(By.id("wallLocation")).click();
		Browser.driver.findElement(By.id("wallLocation")).sendKeys(putWallLocationId);
		Browser.driver.findElement(By.id("wallLocation")).sendKeys(Keys.ENTER);
	}
	
	public void scanItemConfirm(String itemId)
	{
		Browser.driver.findElement(By.id("confirmItem")).click();
		Browser.driver.findElement(By.id("confirmItem")).sendKeys(itemId);
		Browser.driver.findElement(By.id("confirmItem")).sendKeys(Keys.ENTER);
	}
	
	public void scanQuantityConfirm(String quantity)
	{
		Browser.driver.findElement(By.id("confirmPickQuantity")).click();
		Browser.driver.findElement(By.id("confirmPickQuantity")).sendKeys(quantity);
		Browser.driver.findElement(By.id("confirmPickQuantity")).sendKeys(Keys.ENTER);
	}
	
	public String getStation()
	{
		return Browser.driver.findElement(By.id("station")).getAttribute("value"); 
	}
	
	public String getPutWallLocation()
	{
		return Browser.driver.findElement(By.id("wallLocation")).getAttribute("value"); 
	}

	public boolean isConfirmButtonEnabled()
	{
		return Browser.driver.findElement(By.id("confirm-shuttle-pick-button")).isEnabled();
	}

	public void submitPick()
	{
		Browser.driver.findElement(By.id("confirm-shuttle-pick-button")).click();		
	}
}
