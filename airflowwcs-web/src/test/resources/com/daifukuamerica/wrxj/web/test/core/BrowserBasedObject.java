package com.daifukuamerica.wrxj.web.test.core;

import org.junit.After;

public abstract class BrowserBasedObject
{

	public BrowserBasedObject()
	{
		System.setProperty("webdriver.gecko.driver", "C:/projects/wrxj/wrxj-web-ikea/src/test/resources/geckodriver.exe");
	}
	
	
}
