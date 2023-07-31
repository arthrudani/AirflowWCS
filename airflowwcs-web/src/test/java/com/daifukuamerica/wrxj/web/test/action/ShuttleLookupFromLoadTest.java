package com.daifukuamerica.wrxj.web.test.action;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.Select;

import com.daifukuamerica.wrxj.web.test.LoggedInTest;
import com.daifukuamerica.wrxj.web.test.core.Browser;
import com.daifukuamerica.wrxj.web.pages.Pages;

public class ShuttleLookupFromLoadTest /*extends LoggedInTest*/
{

//	/**
//	 * Testing values - must be present in database. 
//	 */
//	public String fromStationSelection = "0011"; 			// station selected in dropdown
//	public String fromToteScan = "756543"; 				// tote barcode that is scanned
//	public String fromSscidLookup = "373258078037500408"; 	//sscid that belongs to the scanned tote
//	public String fromOrderId = "100729"; 
//	public String item = "0001142815386025"; 
//	public String pickToLoadId = "373258078038012470"; 
//	public String pickQuantity = "4"; 
//	
//	
//	@Test
//	public void testShuttleScanToteResponse()
//	{
//		Pages.shuttlePage().scanToteAtStation(fromToteScan, fromStationSelection); 
//	    try {
//	      assertEquals(fromSscidLookup, Browser.driver.findElement(By.id("sscid")).getAttribute("value"));
//	      assertEquals(fromOrderId, Browser.driver.findElement(By.id("orderNumber")).getAttribute("value")); 
//	      assertEquals(item, Browser.driver.findElement(By.id("item")).getAttribute("value")); 
//	      assertEquals(pickQuantity, Browser.driver.findElement(By.id("displayPickQuantity")).getAttribute("value"));
//	      assertEquals(pickToLoadId, Browser.driver.findElement(By.id("pickToLoadId")).getAttribute("value")); 
//	    } catch (Error e) {
//	    	System.out.println(e.getMessage());
//	    }
//	}

}
