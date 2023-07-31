package com.daifukuamerica.wrxj.web.core.connection;

import java.util.Enumeration;

import javax.servlet.ServletContext;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.daifukuamerica.wrxj.jdbc.DBObject;

/**
 * Utility class for borrowing DBObjects from and returning
 * DBObjects to a global pool of DBObjects
 *
 * Author: dystout
 * Created : Jun 19, 2018
 *
 */
public class DBObjectPoolUtil
{

	/**
	 * Log4j logger: DBObjectPoolUtil
	 */
	private static final Logger logger = LoggerFactory.getLogger(DBObjectPoolUtil.class);

	public static DBObject borrowDBObject(ServletContext context)
	{
		Enumeration<String> enumContext = context.getAttributeNames();
		GenericObjectPool<DBObject> gDboPool = (GenericObjectPool<DBObject>) context.getAttribute("dbObjectPool");
		logger.debug("Borrowing object from Pool. Destroyed: {} Created: {} Active: {} Idle: {} Wait: {}", gDboPool.getDestroyedCount(), gDboPool.getCreatedCount(), gDboPool.getNumActive(), gDboPool.getNumIdle(), gDboPool.getNumWaiters());
		DBObject dbo = null;
		try
		{
			dbo = gDboPool.borrowObject();

		} catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Unable to borrow DBObject from pool: {}", e.getMessage());
		}
		return dbo;
	}

	public static void returnDBObject(ServletContext context, DBObject dbo)
	{
		Log.debug("Returning object to pool: " + dbo.toString());
		GenericObjectPool<DBObject> gDboPool = (GenericObjectPool<DBObject>) context.getAttribute("dbObjectPool");
		gDboPool.returnObject(dbo);
	}
}
