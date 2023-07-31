package com.daifukuamerica.wrxj.allocator.shortorder;

import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;

/**
 * <B>Description:</B> This is the HOLD short order processor.  This class is designed
 * so that the method that marks the order on HOLD can be called with the
 * callers transaction boundaries intact.  We throw a run time exception as
 * a warning to the caller that it needs to stop what it was doing (roll back its
 * transaction) and execute any additional order handling functions.
 * 
 * @author       A.D.
 * @version      2.0
 */
public class ShortOrderProcessorHold extends ShortOrderProcessor
{
  /**
   * Default public constructor for the Factory
   */
  public ShortOrderProcessorHold()
  {
    super();
  }
  
 /**
  * Set the next status for a short order.  In this case, HOLD.
  * @param isOrderID the order ID.
  * @throws DBException for DB errors
  */
  @Override
  public void setNextOrderStateWithNotification(String isOrderID, String isOrdDest) throws DBException
  {
    if (mpOrderServer.isOrderFullyAllocated(isOrderID))
    {
      int vnOrdStatus = mpOrderServer.getOrderStatusValue(isOrderID);
      if (vnOrdStatus != DBConstants.SCHEDULED)
        mpOrderServer.setOrderStatusValue(isOrderID, DBConstants.SCHEDULED);
    }
    else
    {
      int vnOrderType = mpOrderServer.getOrderTypeValue(isOrderID);
      if (vnOrderType == DBConstants.ITEMORDER)
      {
        if (!mpAllocServer.itemOrderHasSufficientInventory(isOrderID, isOrdDest))
        {
          throw new ShortOrderAllocationException(DBConstants.HOLD);
        }
        else
        {
          // There is enough inventory in the system but it was likely at an input
          // station or PD stand when we looked to allocate it.  Mark the order
          // as REALLOC so that we try to allocate once the load is stored in the rack.
          mpOrderServer.setOrderStatusValue(isOrderID, DBConstants.REALLOC);
        }
      }
      else if (vnOrderType == DBConstants.FULLLOADOUT)
      {
        throw new ShortOrderAllocationException(DBConstants.HOLD);
      }
      else if (vnOrderType != DBConstants.CYCLECOUNT)
      {     // Don't Hold Empty Container and Replenishment orders.  Just
            // mark them SHORT.
        mpOrderServer.setOrderStatusValue(isOrderID, DBConstants.SHORT);
      }
    }
  }

  @Override
  public void auxiliaryOrderHandling(String isOrderID)
  {
    try
    {
      mpOrderServer.holdOrder(isOrderID);
      mpLogger.logDebug("Holding short Order-->'" + isOrderID + "'");
    }
    catch(DBException exc)
    {
      mpLogger.logException("Error Holding order in Short Order Processor!", exc);
    }
  }
}
