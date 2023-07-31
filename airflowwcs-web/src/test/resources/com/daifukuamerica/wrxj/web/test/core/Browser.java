package com.daifukuamerica.wrxj.web.test.core;

import org.junit.Assert;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Browser test object for configuring browser interaction testing. 
 * 
 * Author: dystout
 * Created : Feb 8, 2018
 *
 */
public class Browser
{
	
	public static WebDriver driver = new FirefoxDriver(); 
	public static WebDriverWait waitDriver = new WebDriverWait(driver, 5); 

	/**
	 * Uses default browser to navigate to given url
	 * @param url
	 */
	public static void goTo(String url)
	{ 
		driver.get(NavigationConstants.CONTEXT_URL + url);
	}
	
	 public static void waitForPageLoaded() {
	        ExpectedCondition<Boolean> expectation = new
	                ExpectedCondition<Boolean>() {
	                    public Boolean apply(WebDriver driver) {
	                        return ((JavascriptExecutor) driver).executeScript("return document.readyState").toString().equals("complete");
	                    }
	                };
	        try {
	            Thread.sleep(1000);
	            WebDriverWait wait = new WebDriverWait(driver, 30);
	            wait.until(expectation);
	        } catch (Throwable error) {
	            Assert.fail("Timeout waiting for Page Load Request to complete.");
	        }
	    }

	public static String title()
	{
		return driver.getTitle(); 
	}
	
	public static void close()
	{
		driver.close(); 
	}
	

}
