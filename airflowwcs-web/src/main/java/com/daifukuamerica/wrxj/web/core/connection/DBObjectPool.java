package com.daifukuamerica.wrxj.web.core.connection;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.daifukuamerica.wrxj.jdbc.DBObject;

/**
 * Implementation of a pool of WRX DBObject(s) using apache commons GenericObjectPool
 *
 * Author: dystout
 * Created : Nov 22, 2017
 *
 */
public class DBObjectPool extends GenericObjectPool<DBObject>
{

	/**
     * Constructor.
     *
     * It uses the default configuration for pool provided by
     * apache-commons-pool2.
     *
     * @param factory
     */
	public DBObjectPool(PooledObjectFactory<DBObject> factory)
	{
		super(factory);
	}

	 /**
     * Constructor.
     *
     * This can be used to have full control over the pool using configuration
     * object.
     *
     * @param factory
     * @param config
     */
	public DBObjectPool(PooledObjectFactory<DBObject> factory,
			GenericObjectPoolConfig config)
	{
		super(factory,config);
	}

}
