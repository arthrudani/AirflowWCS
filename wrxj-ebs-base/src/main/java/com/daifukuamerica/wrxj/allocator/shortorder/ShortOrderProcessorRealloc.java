package com.daifukuamerica.wrxj.allocator.shortorder;

import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;

/**
 * <B>Description:</B> This is the Realloc short order processor.  If there is not
 * sufficient inventory in the system mark the order as REALLOC.
 *
 * @author       A.D.
 * @version      2.0
 */
public class ShortOrderProcessorRealloc extends ShortOrderProcessor
{
 /**
  * Default public constructor for the Factory
  */
  public ShortOrderProcessorRealloc()
  {
    super();
  }
  
 /**
  * Set the next status for a short order.  In this case, REALLOC.
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
      mpOrderServer.setOrderStatusValue(isOrderID, DBConstants.REALLOC);
      mpLogger.logDebug("Marking Order-->'" + isOrderID + "' REALLOC");
    }
  }

  @Override
  public void auxiliaryOrderHandling(String isOrderID)
  {
  }
}
