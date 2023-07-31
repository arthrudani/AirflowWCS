package com.daifukuamerica.wrxj.scheduler.event;

import com.daifukuamerica.wrxj.dataserver.standard.StandardPoReceivingServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;

/**
 * Class to handle Expected Receipt maintenance tasks.
 *
 * @author       A.T.    23-Mar-05    Original version
 * @author       A.D.    26-Aug-05    Converted from an inner class in
 *                                    TimedEventScheduler.
 * @version      2.0
 */
public class ExpectedReceiptTask extends TimedEventTask
{
  private int               mnDaysToKeep;
  private StandardPoReceivingServer mpPoReceivingServer;
  
  public ExpectedReceiptTask(String isName)
  {
    super(isName);
    mpPoReceivingServer = Factory.create(StandardPoReceivingServer.class);    
  }

  @Override
  public void run()
  {
    ensureDBConnection();
    mpLogger.logDebug("checkExpectedReceipts invoked");
    TransactionToken ttok = null;
    try
    {
      ttok = mpDBObject.startTransaction();
      mpPoReceivingServer.cleanupOldExpectedReceipts(mnDaysToKeep);
      mpDBObject.commitTransaction(ttok);
    }
    catch(DBException e)
    {
      mpLogger.logError("Error cleaning up old Expected Receipts.");
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
    mpPoReceivingServer.cleanUp();
    mpPoReceivingServer = null;
    return(rtnval);
  }

  @Override
  public String initTask()
  {
    mnInitialInterval = 30000;
    int vnSecs = getConfigValue(INTERVAL);
    msIntervalString = vnSecs + " seconds ";
    mnInterval = vnSecs * 1000;
    if (vnSecs < 1)
      return "INVALID ExpectedReceipt interval - " + vnSecs + " ExpectedReceiptTask will not be started.";
    
    mnDaysToKeep = getConfigValue("DaysToKeep");
    if (mnDaysToKeep < 0)
    {
      return("INVALID ExpectedReceipt DaysToKeep \"" + mnDaysToKeep +
                      "\" - startup().  EXPECTED RECEIPT TIMER CANCELLED DUE " +
                      "TO INVALID \"DAYS TO KEEP\" PARAMETER.");
    }
    
    return null;
  }
}
