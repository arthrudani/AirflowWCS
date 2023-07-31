package com.daifukuamerica.wrxj.web.core.listeners;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.web.core.connection.GlobalDBObjectPool;
import com.daifukuamerica.wrxj.web.core.connection.WrxjConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dystout
 * Date: Jul 20, 2016
 *
 * Description: Server startup/shutdown servlet context utilized to handle starting/stopping of database
 * connections. It is necessary that a <listener> tag is placed in the web.xml file for this context listener
 * to be started with the server. 
 * 
 */
public class WrxjConnectionListener implements ServletContextListener
{
	
	protected static Logger logger = LoggerFactory.getLogger(WrxjConnectionListener.class); 
	
	/**
	 * Add other inits for database connections here
	 * 
	 * Creates database connections on server startup. 
	 */
	@Override
	public void contextInitialized(ServletContextEvent event) 
	{
		logger.info("Starting database connection service.");
		// Start Wrxj connection
		logger.info("Starting WRxJ Connection... ");
		WrxjConnection.getInstance(); 
		logger.info("** Creating GLOBAL DB Object Pool and injecting into servlet context **");
		event.getServletContext().setAttribute("dbObjectPool",GlobalDBObjectPool.getInstance()); //add DBObject pool to servlet context
		
	}

	/**
	 * Add other database connection closing methods here.
	 * 
	 * Closes database connections if active when server is shutdown.
	 */
	@Override
	public void contextDestroyed(ServletContextEvent arg0) 
	{
		logger.info("Shutting down database connections.");
		logger.info("Closing WRxJ Connection...");
		WrxjConnection.getInstance().close();
	}

}
