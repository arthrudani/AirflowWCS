package com.daifukuamerica.wrxj.web.core.listeners;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.daifukuamerica.wrxj.web.core.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dystout
 * Date: Nov 17, 2016
 *
 * Listener to load properties files into application context and instantiate 
 * a global properties layer 
 * 
 * TODO add DB Constants layer
 */
public class ApplicationPropertiesListener implements ServletContextListener
{
	
	protected static Properties properties = null;
	private static Logger logger = LoggerFactory.getLogger(ApplicationPropertiesListener.class);
	
	/**
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 *
	 *		Called on server startup. 
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) 
	{
		loadProperties();
		for(String name : properties.stringPropertyNames())
		{ 
			logger.info("Property: {}={}", name, properties.getProperty(name));			
		}		
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) 
	{
			// no shutdown value	
	}
	
	/**
	 * Load in web.properties
	 */
	public static void loadProperties()
	{
		logger.info("Loading startup application properties file");
		try
		{ 
			properties = ApplicationProperties.getInstance().getProperties(); 
			logger.info("Successfully loaded application properties from web.properties");
		}
		catch(Exception e)
		{ 
			logger.error("Unable to load properties from web.properties");
			e.printStackTrace();
		}
	}




}
