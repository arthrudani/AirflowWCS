package com.daifukuamerica.wrxj.jdbc;

import com.daifukuamerica.wrxj.application.Application;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.pool2.impl.GenericObjectPool;

public enum WebDBObject
{
  INSTANCE(new HashMap<>());

  private final Map<String, GenericObjectPool<DBObject>> dboMap;

  private WebDBObject(Map<String, GenericObjectPool<DBObject>> dboMap)
  {
    this.dboMap = dboMap;
  }

  /**
   * Check out a DBObject from the pool for the default database
   * 
   * @return
   * @throws Exception
   */
  public DBObject getDBObject() throws Exception
  {
    return getDBObject(Application.getString(DBObject.DATABASE_KEY, DBObject.DEFAULT_DATABASE_VALUE));
  }

  /**
   * Check out a DBObject from the pool for the specified database
   * 
   * @param dbConfig
   * @return
   * @throws Exception
   */
  public DBObject getDBObject(String dbConfig) throws Exception
  {
    if (!dboMap.containsKey(dbConfig))
    {
      dboMap.put(dbConfig, new GenericObjectPool<>(new WebDBObjectFactory(dbConfig)));
      dboMap.get(dbConfig).setMaxIdle(20);
      dboMap.get(dbConfig).setMaxTotal(50);
    }
//    System.out.println(String.format("%s -> %d active, %d idle, %d wait", dbConfig,
//        dboMap.get(dbConfig).getNumActive(),
//        dboMap.get(dbConfig).getNumIdle(),
//        dboMap.get(dbConfig).getNumWaiters()));

    return dboMap.get(dbConfig).borrowObject();
  }
  
  /**
   * Return a DBObject to the pool for the default database
   * 
   * @param dbo
   * @return
   * @throws Exception
   */
  public void returnDBObject(DBObject dbo) throws Exception
  {
    if (dbo != null)
      returnDBObject(Application.getString(DBObject.DATABASE_KEY,
          DBObject.DEFAULT_DATABASE_VALUE), dbo);
  }
  
  /**
   * Return a DBObject to the pool for the specified database
   * 
   * @param dbConfig
   * @param dbo
   * @return
   * @throws Exception
   */
  public void returnDBObject(String dbConfig, DBObject dbo) throws Exception
  {
    if (dbo != null)
      dboMap.get(dbConfig).returnObject(dbo);
  }
}
