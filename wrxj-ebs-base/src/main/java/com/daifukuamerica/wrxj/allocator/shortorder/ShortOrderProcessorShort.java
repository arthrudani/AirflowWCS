package com.daifukuamerica.wrxj.allocator.shortorder;

import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;

/**
 * <B>Description:</B> This is the Short short order processor.  If there is not
 * sufficient inventory in the system mark the order as SHORT.  <b>Note:</b> if
 * the load containing this order's inventory is at a captive station being
 * picked, and there is sufficient inventory on that load, the order is
 * technically not SHORT.  In this special case the order will be marked REALLOC
 * so that it will be considered for allocation again -- this only applies to
 * item orders.
 *
 * For Load orders, if the order couldn't be filled, mark it SHORT (regardless
 * of whether it's at a station).
 *
 * @author       A.D.
 * @version      2.0
 */
public class ShortOrderProcessorShort extends ShortOrderProcessor
{
 /**
  * Default public constructor for the Factory
  */
  public ShortOrderProcessorShort()
  {
    super();
  }
  
 /**
  * Set the next status for a short order.  In this case, SHORT.
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
          mpOrderServer.setOrderStatusValue(isOrderID, DBConstants.SHORT);
          mpLogger.logDebug("Short Order Processor marking Order '" +
                            isOrderID + "' SHORT");
        }
        else
        {
          // There is enough inventory in the system but it was likely at an input
          // station or PD stand when we looked to allocate it.  Mark the order
          // as REALLOC so that we try to allocate once the load is stored in the rack.
          // If this behaviour is NOT desired for a SHORT order here, override this
          // method and make this order SHORT.
          mpOrderServer.setOrderStatusValue(isOrderID, DBConstants.REALLOC);
        }
      }
      else
      {
        mpOrderServer.setOrderStatusValue(isOrderID, DBConstants.SHORT);
        mpLogger.logDebug("Short Order Processor marking Order '" +
                          isOrderID + "' SHORT");
      }
    }
  }

  @Override
  public void auxiliaryOrderHandling(String isOrderID)
  {
  }
}
