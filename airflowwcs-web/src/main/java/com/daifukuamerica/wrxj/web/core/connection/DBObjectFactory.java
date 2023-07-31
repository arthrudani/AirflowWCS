package com.daifukuamerica.wrxj.web.core.connection;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DBObject factory for use in apache commons GenericObjectPool
 *
 * Controls state of pooled/active DBobjects
 *
 * Author: dystout
 * Created : Nov 22, 2017
 *
 */
public class DBObjectFactory implements PooledObjectFactory<DBObject>
{

	/**
	 * Key to database connection property.
	 *
	 * <p>
	 * <b>Details:</b> <code>DATABASE_KEY</code> is set to "<tt>database</tt>",
	 * the name of the application property containing the name of the database
	 * connection to use.
	 * </p>
	 */
	static final String DATABASE_KEY = "database";

	/**
	 * Default database connection.
	 *
	 * <p>
	 * <b>Details:</b> <code>DEFAULT_DATABASE_VALUE</code> is set to
	 * "<tt>OracleDB</tt>", the name of the default database connection to use.
	 * The default database connection is used if the application property keyed
	 * by <code>DATABASE_KEY</code> is not found.
	 * </p>
	 */
	static final String DEFAULT_DATABASE_VALUE = "OracleDB";

	/**
	 * Log4j logger: DBObjectFactory
	 */
	private static final Logger logger = LoggerFactory.getLogger(DBObjectFactory.class);

	/*
	 * (non-Javadoc)
	 * @see org.apache.tomcat.dbcp.pool2.PooledObjectFactory#activateObject(org.apache.tomcat.dbcp.pool2.PooledObject)
	 *
	 * When ready to borrowed from pool, we will first activate a passivated DBObject so it is ready to be collected
	 * from the pool ready for use. (connect it to db)
	 */
	@Override
	public void activateObject(PooledObject<DBObject> pooledDbObject) throws Exception
	{
		pooledDbObject.getObject().connect();
		logger.debug("DBObject activated in pool, state of DBObject connection for{} - connected:{}", pooledDbObject.getObject().getUrl(), pooledDbObject.getObject().checkConnected());
	}

	@Override
	public void destroyObject(PooledObject<DBObject> pooledDbObject) throws Exception
	{
		pooledDbObject.getObject().disconnect(false); //TODO -thread checking???? bool param
		logger.debug("Destroying DBObject {} - isConnected:{}", pooledDbObject.getObject().getUrl(), pooledDbObject.getObject().checkConnected());
	}

	/**
	 * Create DBObject for pool
	 */
	@Override
	public PooledObject<DBObject> makeObject() throws Exception
	{
		DBObject dbObject = new DBObject(Application.getString(DBObjectFactory.DATABASE_KEY, DBObjectFactory.DEFAULT_DATABASE_VALUE));
		logger.debug("DBObject: makeObject() called for connection pooling factory: {}-@-{}", dbObject.getUrl(), dbObject.getDBIdentifier());
		return new PooledDBObject(dbObject);
	}

	/**
	 * When DBObject is returned to pool, disconnect it to prevent idle connections
	 */
	@Override
	public void passivateObject(PooledObject<DBObject> pooledDbObject) throws Exception
	{
		pooledDbObject.getObject().disconnect(false); //TODO -thread checking???? bool param
		logger.debug("Passivating DBObject for pool: {}", pooledDbObject.getObject().getUrl());
	}


	/**
	 * Invoked on activated DBOBject instances in pool to VERIFY if they can be borrowed from pool.
	 *
	 * @return bool - can/cannot be validated as available for borrowing DBObject from pool
	 */
	@Override
	public boolean validateObject(PooledObject<DBObject> pooledDbObject)
	{
		boolean valid = false;
		valid = (pooledDbObject.getObject().isConnectionActive() ||  pooledDbObject.getObject().checkConnected()) ? true :false;
		logger.debug("Checked validity of Pooled DBObject{} - isValid(connected): {}", pooledDbObject.getObject().getUrl(), valid);
		return valid;
	}

}
