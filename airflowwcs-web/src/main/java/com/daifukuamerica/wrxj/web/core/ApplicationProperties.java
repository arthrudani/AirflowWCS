package com.daifukuamerica.wrxj.web.core;

import java.io.IOException;
import java.util.Properties;

import com.daifukuamerica.wrxj.web.core.connection.WrxjConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton - application properties defined in web.properties, an additional 
 * properties layer just for the settings needed by the web application portion
 * of WRXJ. 
 * 
 * Instantiated with context listeners
 * 
 * Author: dystout
 * Created : May 2, 2017
 *
 */
public class ApplicationProperties 
{
	/**
	* Log4j logger: ApplicationProperties
	*/
	private static final Logger logger = LoggerFactory.getLogger(ApplicationProperties.class);

	
	private static ApplicationProperties instance = null; 
	private Properties properties; 
	
	/**
	 * Only instantiate internally 
	 * @throws IOException
	 */
	protected ApplicationProperties() throws IOException{
		properties = new Properties(); 
		properties.load(WrxjConnection.class.getClassLoader().getResourceAsStream("web.properties"));
		logger.info("Successfully loaded application properties from: web.properties"); 
	}
	
	/**
	 * Singleton pattern - returns the only instance of ApplicationProperties. 
	 * 
	 * @return ApplicationProperties
	 */
	public static ApplicationProperties getInstance()
	{
		if(instance==null)
		{
			try
			{
				logger.info("Reading application properties from: web.properies");
				instance = new ApplicationProperties();
			} catch (IOException e)
			{
				logger.error("There was a problem loading the web.properties file: {}", e.getMessage());
			} 
		}
		return instance; 
	}
	
	/**
	 * Return a value from the web.properties file with the given key. 
	 * 
	 * @param key
	 * @return value 
	 */
	public String getValue(String key)
	{
		return properties.getProperty(key); 
	}
	
	/**
	 * getter for the properties object
	 * @return {@link Properties}
	 */
	public Properties getProperties(){
		return properties; 
	}
}
