package com.daifukuamerica.wrxj.dbadapter;

import com.daifukuamerica.wrxj.log.Logger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Wynsoft tables do not have the WRx standard columns
 */
public abstract class WynsoftData extends AbstractSKDCData
{
  // Unknown field logging causes a LOT of latency with large result sets.
  // To work around this, we won't always log the error.
  private static final int UNKNOWN_FIELD_LOG_INTERVAL = 50;
  private static Map<String, Map<String, Integer>> UNKNOWN_FIELD_CACHE = new HashMap<>();
  
  public WynsoftData()
  {
    super();
  }
  
  @Override
  public int setField(String columnName, Object columnValue)
  {
    if (super.setField(columnName, columnValue) != 0)
    {
      // Don't throw exceptions if Wynsoft DB's change. Just log it.
      logUnknownField(columnName);
    }
    return 0;
  }
  
  @Override
  public long getID()
  {
    throw new IllegalAccessError("WRx ID column is not valid for Wynsoft tables");
  }
  
  @Override
  public void setID(long idcolValue) {}
  
  @Override
  public void setAddMethod(String iscolValue) {}
  
  @Override
  public void setModifyTime(Date ipcolValue) {}
  
  @Override
  public void setUpdMethod(String iscolValue) {}

  /**
   * Foreign Wynsoft tables may change without notice
   * 
   * @param columnName
   */
  private void logUnknownField(String columnName)
  {
    String myClass = getClass().getSimpleName();
    Map<String,Integer> myUnknownColumns = UNKNOWN_FIELD_CACHE.get(myClass);
    if (myUnknownColumns == null)
    {
      myUnknownColumns = new HashMap<>();
      UNKNOWN_FIELD_CACHE.put(myClass, myUnknownColumns);
    }
    
    boolean log = false;
    if (myUnknownColumns.containsKey(columnName))
    {
      int i = myUnknownColumns.get(columnName);
      if (++i > UNKNOWN_FIELD_LOG_INTERVAL)
      {
        log = true;
      }
      else
      {
        myUnknownColumns.put(columnName, i);
      }
    }
    else
    {
      log = true;
    }
    if (log)
    {
      myUnknownColumns.put(columnName, 1);
      Logger.getLogger().logError(
          myClass + ": Unknown field [" + columnName + "]");
    }
  }
}
