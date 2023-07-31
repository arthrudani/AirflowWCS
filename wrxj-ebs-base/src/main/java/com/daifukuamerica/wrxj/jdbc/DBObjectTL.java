package com.daifukuamerica.wrxj.jdbc;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.log.Logger;
import java.awt.EventQueue;
import java.util.HashMap;
import java.util.Map;

/**
 * A Thread Local database object collection. This allows each thread to use
 * different sets of database connections.
 *
 * @author avt
 * @version 1.0
 */
public final class DBObjectTL
{
  private static class ThreadLocalDBObjects extends ThreadLocal<Map<String, DBObject>>
  {
    
    private final Map<String,DBObject> mpEventThreadMap = initialValue();
    
    @Override
    public Map<String,DBObject> initialValue()
    {
      return new HashMap<String,DBObject>();
    }

    public Map<String, DBObject> getMap()
    {
      if (EventQueue.isDispatchThread())
        return mpEventThreadMap;
      return super.get();
    }
    
  }

  private static ThreadLocalDBObjects localDBObjects = new ThreadLocalDBObjects();
  private Map<String, DBObject> dbList;

  /**
   * Fetch the unique DB object for this thread and database. If there is
   * not a matching DBObject for this thread, one will be created. 
   * 
   * If designated as a web client in properties, use global DBObject enum 
   * to avoid thread local creation.
   *
   * @return DBObject containing matching DBObject.
   */
  public DBObject getDBObject(String dbName)
  {
    dbList = localDBObjects.getMap();
    if (!dbList.containsKey(dbName))
    {
      dbList.put(dbName, new DBObject(dbName));
    }
    return dbList.get(dbName);
  }

  /**
   * Fetch the unique DB object for this thread and default database.
   *
   * @return DBObject containing matching DBObject.
   */
  public DBObject getDBObject()
  {
    return getDBObject(Application.getString(DBObject.DATABASE_KEY, DBObject.DEFAULT_DATABASE_VALUE));
  }
  
  /**
   * Remove a DBObject from the available thread-linked objects
   * 
   * @param dbName
   */
  public void putDBObject(String dbName, DBObject dbo)
  {
    dbList = localDBObjects.getMap();
    dbList.put(dbName, dbo);
  }
  
  /**
   * Remove a DBObject from the available thread objects
   * 
   * @param dbName
   */
  public void putDBObject(DBObject dbo)
  {
    putDBObject(getDefaultConfig(), dbo);
  }

  /**
   * Remove a DBObject from the available thread-linked objects
   * 
   * @param dbName
   */
  public void removeDBObject(String dbName)
  {
    dbList = localDBObjects.getMap();
    dbList.remove(dbName);
  }
  
  /**
   * Remove a DBObject from the available thread objects
   * 
   * @param dbName
   */
  public void removeDBObject()
  {
    removeDBObject(getDefaultConfig());
  }
  
  /**
   * Get the default database configuration
   * 
   * @return
   */
  private String getDefaultConfig()
  {
    return Application.getString(DBObject.DATABASE_KEY, DBObject.DEFAULT_DATABASE_VALUE);
  }
  
  /**
   * Close the connection to the matching DBObject
   * 
   * @param dbName
   */
  public void close(String dbName)
  {
    try
    {
      new DBObjectTL().getDBObject().totalDisconnect();
    }
    catch (Exception e)
    {
      Logger.getLogger().logException(
          "Unable to disconnect database connection [" + dbName + "]", e);
    }
  }

  /**
   * Close the default connection
   */
  public void close()
  {
    close(getDefaultConfig());
  }
}
