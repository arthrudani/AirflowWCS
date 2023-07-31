package com.daifukuamerica.wrxj.scheduler.event;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dataserver.standard.StandardConfigurationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.util.SKDCConstants;

/**
 * Class to handle item master maintenance tasks.
 *
 * @author       A.T.    23-Mar-05    Original version
 * @author       A.D.    26-Aug-05    Converted from an inner class in
 *                                    TimedEventScheduler.
 * @version      2.0
 */
public class ItemMasterTask extends TimedEventTask
{
  private StandardInventoryServer mpInventoryServer;
  
  public ItemMasterTask(String isName)
  {
    super(isName);
    mpInventoryServer = Factory.create(StandardInventoryServer.class);
  }
  
  @Override
  public void run()
  {
    ensureDBConnection();
    mpLogger.logDebug("checkAutoDeleteItemMasters invoked");
    TransactionToken ttok = null;
    try
    {
      ttok = mpDBObject.startTransaction();
      mpInventoryServer.cleanupAllAutoDeletableItemMasters();
      mpDBObject.commitTransaction(ttok);
    }
    catch(DBException e)
    {
      mpLogger.logError("Error cleaning up Auto-Delete Item Master records.");
    }
    finally
    {
      mpDBObject.endTransaction(ttok);
    }
    
    if (mzInterrupted)
    {
      try { mpDBObject.disconnect(false); }
      catch(DBException exc) {}
    }
  }

  @Override
  public boolean cancel()
  {
    boolean rtnval = super.cancel();
    mpInventoryServer.cleanUp();
    mpInventoryServer = null;
    return(rtnval);
  }
  
  @Override
  public String initTask()
  {
    try
    {
      StandardConfigurationServer mpConfigSrvr = Factory.create(StandardConfigurationServer.class);
      if (mpConfigSrvr.isSplitSystem() && mpConfigSrvr.isThisPrimaryJVM() == false)
      {
        mpLogger.logOperation("INVALID JVM (" + 
                Application.getString(SKDCConstants.JVM_IDENTIFIER_KEY) +
                ") - a Secondary JVM - ItemMasterTask will not be started.");
        return " ";
      }
    }
    catch (DBException e)
    {
      return e.getMessage();
    }
    
    mnInitialInterval = 30000;
    int vnSecs = getConfigValue(INTERVAL);
    msIntervalString = vnSecs + " seconds ";
    mnInterval = vnSecs * 1000;
    if (vnSecs < 1)
      return "INVALID ItemMaster interval - " + vnSecs + " ItemMasterTask will not be started.";
    return null;
  }
}
