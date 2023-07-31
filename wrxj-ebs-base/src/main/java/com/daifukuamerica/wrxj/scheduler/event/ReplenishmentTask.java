package com.daifukuamerica.wrxj.scheduler.event;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDedicationServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.data.DedicatedLocationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import java.util.List;
import java.util.Map;

/**
 * Description:<BR>
 *  This class is used to check dedicated locations for Replenishments on the
 *  Wrx-J system and notify the allocator when they are found.
 *
 * @author       A.D.    31-Aug-05
 * @version      1.0
 */
public class ReplenishmentTask extends TimedEventTask
{
  private DedicatedLocationData mpDLData;
  private StandardDedicationServer mpDedicatedServer;
  
  public ReplenishmentTask(String isName)
  {
    super(isName);
    mpDLData = Factory.create(DedicatedLocationData.class);
  }
  
  @Override
  public void run()
  {
    ensureDBConnection();
    if (mpDedicatedServer == null)    
      mpDedicatedServer = Factory.create(StandardDedicationServer.class);
    List<Map> vpList = null;
    
    try
    {                                  // Get a list of active dedicated locations.
      vpList = mpDedicatedServer.getDedications(mpDLData);
    }
    catch(DBException exc)
    {
      mpLogger.logError("Database error retrieving lists of dedicated locations." +
                        exc.getMessage());
      return;
    }
                                       // Cycle through the dedicated locations
                                       // and send for a replenishment if needed.
    for(Map vpDLMap : vpList)
    {
      if (mzInterrupted) break;
      mpDLData.dataToSKDCData(vpDLMap);
      
      TransactionToken vpTTok = null;
      try
      {
        vpTTok = mpDBObject.startTransaction();
        mpDedicatedServer.replenishDedication(mpDLData);
        mpDBObject.commitTransaction(vpTTok);
      }
      catch(DBException e)
      {
        mpLogger.logError("TimedEventScheduler-->ReplenishmentTask. Failed to " +
                          "generate replenishment for location " + mpDLData.getWarehouse() +
                          "-" + mpDLData.getAddress() + ", Item " + mpDLData.getItem() +
                          "... " + e.getMessage());
      }
      finally
      {
        mpDBObject.endTransaction(vpTTok);
      }
    }
    
    if (mzInterrupted)
    {
      try { mpDBObject.disconnect(false); }
      catch(DBException exc) {}
    }
  }

  @Override
  public String initTask()
  {
    mnInitialInterval = 10000;
    int vnSecs = getConfigValue(INTERVAL);
    msIntervalString = vnSecs + " seconds ";
    mnInterval = vnSecs * 1000;
    if (vnSecs < 1)
      return "INVALID Replenishment interval - " + vnSecs + " ReplenishmentTask will not be started.";
    return null;
  }
}
