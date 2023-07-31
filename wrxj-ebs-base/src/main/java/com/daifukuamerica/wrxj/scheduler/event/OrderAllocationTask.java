package com.daifukuamerica.wrxj.scheduler.event;

import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import java.util.ArrayList;
import java.util.List;

public class OrderAllocationTask extends TimedEventTask
{

	  private static final int BATCH_LENGTH = 15;
	  protected StandardOrderServer mpOrderServer;
	
  public OrderAllocationTask(String isName)
  {
    super(isName);
  }

 
  @Override
  public void run()
  {

	mpOrderServer = Factory.create(StandardOrderServer.class);
	    
    ensureDBConnection();
                                       // Cycle through the types of orders we
                                       // are interested in and get lists of order id's
                                       // matching this type.
    for(int vnOrderType : orderTypesToCheck())
    {
      List<String> vpOrders = getOrdersToAllocate(vnOrderType);
      if (vpOrders != null)
      {
        for(String vsOrderID : vpOrders)
        {
          if (mzInterrupted) break;
          try
          {
            Thread.sleep(200);
            allocateOrder(vsOrderID);
          }
          catch(InterruptedException e)
          {
            mpLogger.logError("TimedEventScheduler-->OrderAllocationTask: " +
                              "Sleep timer is prempted...");
          }
          catch(DBException ex)
          {
            mpLogger.logOperation("TimedEventScheduler-->OrderAllocationTask: " +
                "Allocation error... " + ex.getMessage());
          }
        }
      }
      if (mzInterrupted) break;
    }

    if (mzInterrupted)
    {
      try { mpDBObject.disconnect(false); }
      catch(DBException exc) {}
    }
  }

  protected List<String> trimOrderList(String[] iapOrderList)
  {
    List<String> vpTrimList = new ArrayList<String>();
    for (int i=0; i<BATCH_LENGTH && i<iapOrderList.length; i++)
      vpTrimList.add(iapOrderList[i]);

    return vpTrimList;
  }

  /**
   * Custom extension of this class can overwrite this method to filter
   * which orders we want to deal with.
   */
  protected List<String> getOrdersToAllocate(int inOrderType)
  {
    // Get listing of orders ready for allocation.
    try
    {
      OrderHeaderData vpOHD = Factory.create(OrderHeaderData.class);
      vpOHD.setKey(OrderHeaderData.ORDERTYPE_NAME, inOrderType);
      vpOHD.setInKey(OrderHeaderData.ORDERSTATUS_NAME, 0, DBConstants.READY,
                          DBConstants.ALLOCATENOW);
      String[] vpOrderList = mpOrderServer.getOrderChoices(vpOHD, null, false);
      return trimOrderList(vpOrderList);
    }
    catch(DBException e)
    {
      mpLogger.logError("TimedEventScheduler-->OrderAllocationTask: " +
                        "Error getting orders... " + e.getMessage());
      return null;
    }
  }

  protected int[] orderTypesToCheck()
  {
    return new int[]{DBConstants.ITEMORDER, DBConstants.FULLLOADOUT};
  }

  protected void allocateOrder(String isOrder) throws DBException
  {
    mpOrderServer.allocateOrder(isOrder);
  }

  @Override
  public boolean cancel()
  {
    boolean rtnval = super.cancel();
    mpOrderServer.cleanUp();
    mpOrderServer = null;
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
      return "INVALID OrderAllocation interval - " + vnSecs + " OrderAllocationTask will not be started.";
    return null;
  }
}
