package com.daifukuamerica.wrxj.jdbc;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.log.Logger;
import java.io.Closeable;
import java.io.IOException;

public class WebDBObjectHelper implements Closeable
{
  private String msDbConfig;
  private DBObject mpDBO;
  
  public WebDBObjectHelper() throws Exception
  {
    this(Application.getString(DBObject.DATABASE_KEY, DBObject.DEFAULT_DATABASE_VALUE));
  }

  /**
   * Get a DBObject from the pool
   */
  public WebDBObjectHelper(String isDbConfig) throws Exception
  {
    msDbConfig = isDbConfig;
    mpDBO = WebDBObject.INSTANCE.getDBObject(msDbConfig);
  }

  /**
   * Return the DBObject to the pool
   */
  @Override
  public void close() throws IOException
  {
    try
    {
      WebDBObject.INSTANCE.returnDBObject(msDbConfig, mpDBO);
    }
    catch (Exception e)
    {
      Logger.getLogger().logException("Error returning DBObject to pool", e);
    }
  }
  
  /**
   * Get the DBObject
   * 
   * @return
   */
  public DBObject getDBObject()
  {
    return mpDBO;
  }
}
