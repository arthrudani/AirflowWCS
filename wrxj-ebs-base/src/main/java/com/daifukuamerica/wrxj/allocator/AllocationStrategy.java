package com.daifukuamerica.wrxj.allocator;

/*
 * Daifuku America Corporation
 * International Center
 * 5202 Douglas Corrigan Way
 * Salt Lake City, Utah  84116-3192
 * (801) 359-9900
 *
 * Copyright (c) 2004-2008 Daifuku America Corporation
 * 
 * This software is furnished under a license and may be used and copied only 
 * in accordance with the terms of such license. This software or any other 
 * copies thereof in any form, may not be provided or otherwise made available, 
 * to any other person or company without written consent from Daifuku America 
 * Corporation.
 * 
 * Daifuku America Corporation assumes no responsibility for the use or 
 * reliability of software which has been modified without approval.
 */

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import java.util.List;

public interface AllocationStrategy
{
  /**
   *  Sets the order data of the Order to be worked on
   *  @param ordData the Order information for order this strategy is allocating.
   */
  public void setAllocationOrder(AbstractSKDCData ordData);
  /**
   *  Sets the aisle group we want to allocate product for.
   *  @param aisleGroup the aisle group order will be allocated for.
   */
  public void setAisleGroup(int aisleGroup);
  /**
   *  Sets the output station this allocation strategy will allocate orders to.
   *  @param ipOutputStation the output station loads will be directed to.
   */
  public void setOutputStation(StationData ipOutputStation);
  /**
   *  Sets the logger this strategy will use.
   *  @param strategyLogger the logging instance to use for this strategy.
   */
  public void setLogger(Logger strategyLogger);
  /**
   *  Sets the allocation probe that will report allocation issues.
   *  @param allocationProbe the Allocation Probe to use for diagnostics.
   */
  public void setAllocationProbe(AllocationProbe allocationProbe);
  /**
   *  Allocation method.  This method does the work of finding the item details
   *  or loads for an order.
   *  @return java.util.List of all Loads that need to be moved.
   */
  public List<AllocationMessageDataFormat> allocate() 
         throws DBException, AllocationException;
}
