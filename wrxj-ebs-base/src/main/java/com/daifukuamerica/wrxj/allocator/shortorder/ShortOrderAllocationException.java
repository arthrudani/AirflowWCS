package com.daifukuamerica.wrxj.allocator.shortorder;

/**
 * Exception to stop allocating an order due to some rule being violated.
 * @author A.D.
 * @since  08-Apr-2009
 */
public class ShortOrderAllocationException extends RuntimeException
{
  private int mnNextOrderStatus;

  public ShortOrderAllocationException()
  {
    super();
  }

  public ShortOrderAllocationException(int inNextOrderStatus)
  {
    super();
    mnNextOrderStatus = inNextOrderStatus;
  }

 /**
  * Lets exception catcher know what the next short order status should be if
  * the allocation is to be stopped (rolled back).  This method only exists if
  * the exception catcher wishes further information about the nature of the
  * exception.
  * @return the next order state.
  */
  public int getOrderRollbackStatus()
  {
    return mnNextOrderStatus;
  }
}
