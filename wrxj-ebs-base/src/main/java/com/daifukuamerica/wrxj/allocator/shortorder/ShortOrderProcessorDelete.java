package com.daifukuamerica.wrxj.allocator.shortorder;

import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;

/**
 * <B>Description:</B> This is the DELETE short order processor.  This class is designed
 * so that the method that marks the order to DONE can be called with the
 * callers transaction boundaries intact.  We throw a run time exception as
 * a warning to the caller that it needs to stop what it was doing (roll back its
 * transaction) and execute any additional order handling functions.
 * 
 * @author   A.D.
 * @version  2.0
 * @since    09-Apr-2009
 */
public class ShortOrderProcessorDelete extends ShortOrderProcessor
{
  /**
   * Default public constructor for the Factory
   */
  public ShortOrderProcessorDelete()
  {
    super();
  }
  
  /**
   * Set the next status for a short order. The default behavior is:
   * <OL>
   * <LI>if the order is Short and there are moves, or there are item details
   *     picked for this order, set order to KILLED.
   * <LI>delete Short order if (1) fails.
   * </OL>
   * 
   * @param isOrderID the order id.
   * @throws DBException if there is a database access error.
   * @throws ShortOrderAllocationException if the allocation should be rolled
   *         back.
   */
  @Override
  public void setNextOrderStateWithNotification(String isOrderID, String isOrdDest)
         throws DBException, ShortOrderAllocationException
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
          throw new ShortOrderAllocationException(DBConstants.DONE);
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
        throw new ShortOrderAllocationException(DBConstants.DONE);
      }
      else                             // Don't delete Container and CycleCount
      {                                // orders. Just mark them SHORT.
        mpOrderServer.setOrderStatusValue(isOrderID, DBConstants.SHORT);
      }
    }
  }

  @Override
  public void auxiliaryOrderHandling(String isOrderID)
  {
    try
    {
      mpOrderServer.setOrderStatusValue(isOrderID, DBConstants.HOLD);
      mpOrderServer.deleteOrder(isOrderID);
      mpLogger.logDebug("Deleting Short Order '" + isOrderID +
                        "' due to Short Order configuration setting.");
    }
    catch(DBException exc)
    {
      mpLogger.logException("Error deleting order in Short Order Processor!", exc);
    }
  }
}
