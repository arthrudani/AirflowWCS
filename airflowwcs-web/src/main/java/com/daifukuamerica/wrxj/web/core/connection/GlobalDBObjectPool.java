package com.daifukuamerica.wrxj.web.core.connection;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.daifukuamerica.wrxj.jdbc.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global Singleton Pool.
 *
 * If for some reason the pool is GC or an exception clears a reference
 * the pool will be recreated. //TODO I would much rather implement JNDI with hibernate
 * and do away with this hacky kludge
 *
 * Author: dystout
 * Created : Jun 19, 2018
 *
 */
public class GlobalDBObjectPool
{

	private static GenericObjectPool<DBObject> instance;

	/**
	 * Log4j logger: GlobalDBObjectPool
	 */
	private static final Logger logger = LoggerFactory.getLogger(GlobalDBObjectPool.class);

	public static GenericObjectPool<DBObject> getInstance()
	{
		GenericObjectPool<DBObject> inst = instance;
		if (inst == null)
		{
			GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
			poolConfig.setMaxIdle(5);
			poolConfig.setMinIdle(1);
			poolConfig.setMaxTotal(30);
			poolConfig.setTestOnBorrow(true);
			poolConfig.setTestOnReturn(true);
			poolConfig.setLifo(true);
			poolConfig.setSoftMinEvictableIdleTimeMillis(3000);
			poolConfig.setTimeBetweenEvictionRunsMillis(3000);
			poolConfig.setTestWhileIdle(true);
			logger.debug("Creating Global DB Object pool");
			try
			{

				logger.debug("***** CREATING GLOBAL OBJECT POOL *******\n\n ");
				inst = new DBObjectPool(new DBObjectFactory(), poolConfig);
				logger.debug("CREATION STACK TRACE (INFO): {}", inst.toString());
			} catch (Exception e)
			{
				logger.error("ERROR:{}", e.getMessage());
				e.printStackTrace();
			}
		}

		return inst;
	}
}