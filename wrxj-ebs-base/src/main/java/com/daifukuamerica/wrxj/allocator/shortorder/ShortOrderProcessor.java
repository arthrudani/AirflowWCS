package com.daifukuamerica.wrxj.allocator.shortorder;

import com.daifukuamerica.wrxj.dataserver.standard.StandardAllocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;

/**
 * <B>Description:</B> This is the default short order processor.  It leaves 
 * the order status Short.
 * 
 * @author       mandrus<BR>
 * @version      1.0
 * 
 * <BR>Copyright (c) 2007 by Daifuku America Corporation
 */
public abstract class ShortOrderProcessor
{
  protected StandardOrderServer mpOrderServer;
  protected StandardAllocationServer mpAllocServer;
  protected Logger mpLogger;
  
 /**
  * Default public constructor for the Factory
  */
  public ShortOrderProcessor()
  {
    mpOrderServer = Factory.create(StandardOrderServer.class);
    mpAllocServer = Factory.create(StandardAllocationServer.class);
    mpLogger = Logger.getLogger();
  }

 /**
  * Set the next status for a short order, and potentially signal the caller
  * that additional processing needs to be done on the order.
  * @param isOrderID the order id.
  * @param isOrdDest the order destination.
  * @throws DBException for any DB errors.
  */
  public void setNextOrderState(String isOrderID, String isOrdDest) throws DBException
  {
    try
    {
      setNextOrderStateWithNotification(isOrderID, isOrdDest);
    }
    catch(ShortOrderAllocationException soa)
    {
      int vnNextOrdStat = soa.getOrderRollbackStatus();
      mpOrderServer.setOrderStatusValue(isOrderID, vnNextOrdStat);
    }
  }

 /**
  * Set the next status for a short order, and potentially signal the caller
  * that additional processing needs to be done on the order.
  * @param isOrderID the order id.
  * @param isOrdDest the order destination.
  * @throws DBException for any DB errors.
  */
  public abstract void setNextOrderStateWithNotification(String isOrderID, String isOrdDest)
         throws DBException;

  /**
  * Method to handle any additional Order handling due to order shortage.
  * @param isOrderID the order id.
  */
  public abstract void auxiliaryOrderHandling(String isOrderID);
}
