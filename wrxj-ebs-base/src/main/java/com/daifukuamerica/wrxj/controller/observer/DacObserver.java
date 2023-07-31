package com.daifukuamerica.wrxj.controller.observer;

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.log.Logger;
import java.util.Observer;

/**
 * <B>Description:</B> An observer that has methods for database connectivity
 *
 * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
public abstract class DacObserver implements Observer
{
  DBObject mpDB = null;
  Logger mpLogger;
  
  /**
   * Constructor with logger
   * 
   * @param ipLogger
   */
  public DacObserver(Logger ipLogger)
  {
    mpLogger = ipLogger;
  }
  
  /**
   * Because our connections are thread-based...
   */
  protected void ensureDBConnection()
  {
    if (mpDB == null)
    {
      mpDB = new DBObjectTL().getDBObject();
      try
      {
        mpDB.connect();                 // Get connection from connection pool.
      }
      catch (DBException e)
      {
        mpLogger.logException(e,
            "Error opening Database Connection for Observer");
      }
    }
  }
  

}
