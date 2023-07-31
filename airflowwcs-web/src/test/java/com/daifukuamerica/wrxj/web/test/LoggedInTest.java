package com.daifukuamerica.wrxj.web.test;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;

import com.daifukuamerica.wrxj.web.test.core.Browser;
import com.daifukuamerica.wrxj.web.test.core.ResourceFile;
import com.daifukuamerica.wrxj.web.pages.Pages;

/**
 * A test that needs to be logged in to execute. 
 * 
 * Most tests will want to extend this as most actions performed within the application 
 * will require the user to be logged in. 
 * 
 * Author: dystout
 * Created : Feb 15, 2018
 *
 */
public abstract class LoggedInTest
{
	
	/**
	 * Load in resource files for test values
	 */
	@Rule
	public ResourceFile shuttlePickResources = new ResourceFile("/shuttle.txt"); 

	/**
	 * Login the ASRS/ASRS user and assert that we are on welcome page before proceeding. 
	 */
	@Before
	public void loginAsrsUser()
	{
		Pages.loginPage().loginAsrsUser();
		// assertText | css=h3.main-title | Welcome ASRS-User!
	    Assert.assertEquals("Welcome ASRS-User!", Browser.driver.findElement(By.cssSelector("h3.main-title")).getText());
	}
	

	/**
	 * Assert action response success/fail
	 * 
	 * <b>IMPORTANT: Use with knowledge that if a action is performed and response 
	 * is not received then the last action performed will be evaluated 
	 * (javascript variable will be populated with last action response)</b>
	 * 
	 * Every AjaxResponse-able action performed will save off its SUCCESS/FAIL 
	 * value into a JavaScript variable "lastResponse" on the page. Retrieve it 
	 * using WebDriver and assert equals. 
	 * 
	 * @return last action pefromed pass/fail
	 */
	public boolean isResponseSuccess()
	{
		String lastResponse = (String) ((JavascriptExecutor) Browser.driver).executeScript("return lastResponse;"); 
		return lastResponse.equals("SUCCESS"); 
	}
}
