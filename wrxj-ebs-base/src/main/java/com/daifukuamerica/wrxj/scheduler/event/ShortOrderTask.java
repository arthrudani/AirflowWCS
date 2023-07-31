package com.daifukuamerica.wrxj.scheduler.event;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dataserver.standard.StandardAllocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardConfigurationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Description:<BR>
 *  This class is used to check for short orders on the Wrx-J system and notify
 *  the allocator when they are found.
 *
 * @author       A.D.    25-Aug-05
 * @version      1.0
 */
public class ShortOrderTask extends TimedEventTask
{
  /** 
   * Orders-Per-Batch keeps the allocator from being saturated with orders in
   * a system that has lots of short orders simultaneously.  This parameter 
   * should be tuned based on order and interval size, and the average number 
   * of Short orders in the system.
   */
  private int   mnOrdersPerBatch;
  protected int[] mpOrderTypes = {DBConstants.ITEMORDER, DBConstants.FULLLOADOUT, 
                                DBConstants.CONTAINER, DBConstants.REPLENISHMENT};
  protected StandardOrderServer         mpOrderServer;
  protected StandardAllocationServer    mpAllocServer;
  protected StandardConfigurationServer mpConfigServer;
  
  public ShortOrderTask(String isName)
  {
    super(isName);
  }

  @Override
  public void run()
  {
    ensureDBConnection();
    if (mpOrderServer == null)
      mpOrderServer = Factory.create(StandardOrderServer.class);

    if (mpAllocServer == null)
      mpAllocServer = Factory.create(StandardAllocationServer.class);

    if (mpConfigServer == null)
      mpConfigServer = Factory.create(StandardConfigurationServer.class);

                                       // Cycle through the types of orders we
                                       // are interested in and get lists of order id's
                                       // matching this type.
    for(int vnOrderType : mpOrderTypes)
    {                                  // Get listing of short orders.
      int[] vpOrderStatus = new int[] {DBConstants.REALLOC};
      String[] vpOrderList = mpOrderServer.getCheckedShortOrderList(vpOrderStatus, vnOrderType);
      String[] vpTrimmedList =  trimOrderList(vpOrderList, vnOrderType);

      for(String vsOrderID : vpTrimmedList)
      {
        if (mzInterrupted) break;
        try
        {
          Thread.sleep(1000);          // Slow it down by one second so that
                                       // the interaction between JMS and ControllerImpl
                                       // will function correctly.

          // MCM Sep2019 
          // Move the checktime update to before the allocator event,  we saw a couple cases of DB lock contention 
          // with SQLServer
          mpOrderServer.updateShortOrderCheckTime(vsOrderID, new Date());
          
          mpOrderServer.allocateOrder(vsOrderID);
        }
        catch(DBException e)
        {
          mpLogger.logError("TimedEventScheduler-->ShortOrderTask:: " +
                            "Error sending allocator order event for order " +
                            vsOrderID + "... " + e.getMessage());
        }
        catch(InterruptedException e)
        {
          mpLogger.logError("TimedEventScheduler-->ShortOrderTask:: " +
                            "Sleep timer is preempted...");
        }
      }
      if (mzInterrupted) break;
    }
    
    if (mzInterrupted && mpDBObject != null)
    {
      try { mpDBObject.disconnect(false); }
      catch(DBException exc) {}
    }
  }

 /**
  * Method trims the list of candidate orders down to the number specified in the 
  * orders-per-batch parameter.  This keeps the allocator from being saturated 
  * with orders.
  * @param ipOrderList the complete list of orders.
  * @param inOrderType  the order type.
  * @return the trimmed list of orders.
  */
  protected String[] trimOrderList(String[] ipOrderList, int inOrderType)
  {
    String[] vasTrimList;

    if (mnOrdersPerBatch == -1 || ipOrderList.length <= mnOrdersPerBatch)
    {
      vasTrimList = ipOrderList;
    }
    else
    {
      vasTrimList = new String[mnOrdersPerBatch];
      System.arraycopy(ipOrderList, 0, vasTrimList, 0, mnOrdersPerBatch);
    }

/*============================================================================
    If it's a full load order, trim it further possibly based on which JVM is
    being used.
  ============================================================================*/
    try
    {
      if (vasTrimList.length > 0 && inOrderType == DBConstants.FULLLOADOUT &&
          mpConfigServer.isSplitSystem())
      {
        List<String> vpTrimList = new ArrayList<String>(vasTrimList.length);
        for(int vnIdx = 0; vnIdx < vasTrimList.length; vnIdx++)
        {
          vpTrimList.add(vasTrimList[vnIdx]);
        }

        for (Iterator<String> vpIter = vpTrimList.iterator(); vpIter.hasNext(); )
        {
          if (!mpAllocServer.anyLoadOrderLinesToAllocate(vpIter.next()))
          {
            vpIter.remove();
          }
        }

        if (vpTrimList.size() < vasTrimList.length)
        {
          vasTrimList = vpTrimList.toArray(new String[0]);
        }
      }
    }
    catch(DBException ex)
    {
      mpLogger.logError("DB error finding Load Orders to trim! " + ex.getMessage());
      vasTrimList= new String[0];
    }

    return(vasTrimList);
  }

  @Override
  public String initTask()
  {
    mnInitialInterval = 30000;
    int vnSecs = getConfigValue(INTERVAL);
    msIntervalString = vnSecs + " seconds ";
    mnInterval = vnSecs * 1000;
    if (vnSecs < 1)
      return "INVALID ShortOrderCheck interval - " + vnSecs + " ShortOrderTask will not be started.";
    
    mnOrdersPerBatch = Application.getInt(msName + "." + "OrdersPerBatch", 5);
    
    return null;
  }
}
