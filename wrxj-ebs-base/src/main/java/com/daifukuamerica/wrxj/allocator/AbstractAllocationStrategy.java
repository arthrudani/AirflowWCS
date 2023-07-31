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

import com.daifukuamerica.wrxj.allocator.shortorder.ShortOrderProcessor;
import com.daifukuamerica.wrxj.dataserver.standard.StandardAllocationServer;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.log.Logger;

public abstract class AbstractAllocationStrategy implements AllocationStrategy
{
  protected StandardAllocationServer mpAllocServer;
  protected StationData      mpOutStation;
  protected String           msOrder;
  protected int              mnAisle;
  protected int              iOriginalOrderStatus;
  protected OrderHeaderData  mpOHD;
  protected ShortOrderProcessor mpShortOrderProcess;
  /**
   * Used to log debug and error messages.
   */
  protected Logger          mpLogger;
  /**
   * Probe used in allocation diagnostics.
   */
  protected AllocationProbe  mpProbe;
  protected boolean          mzAllocationDiagnostics;

  public AbstractAllocationStrategy()
  {
    mpShortOrderProcess = Factory.create(ShortOrderProcessor.class);
  }

  /**
   * {@inheritDoc}
   */
  public void setAisleGroup(int inAisleGroup)
  {
    mnAisle = inAisleGroup;
  }

  /**
   * {@inheritDoc}
   */
  public void setAllocationOrder(AbstractSKDCData ordData)
  {
    mpOHD = (OrderHeaderData) ordData;
    msOrder = mpOHD.getOrderID();
    iOriginalOrderStatus = mpOHD.getOrderStatus();
  }

  /**
   * {@inheritDoc}
   */
  public void setAllocationProbe(AllocationProbe ipAllocationProbe)
  {
    mpProbe = ipAllocationProbe;
    mpAllocServer.setAllocationProbe(ipAllocationProbe);
    mzAllocationDiagnostics = true;
  }

  /**
   * {@inheritDoc}
   */
  public void setLogger(Logger ipLogger)
  {
    mpLogger = ipLogger;
  }

  /**
   * {@inheritDoc}
   */
  public void setOutputStation(StationData ipOutputStation)
  {
    mpOutStation = ipOutputStation;
  }

  protected void addToProbe(String isMethod, String isMsg)
  {
    if (mzAllocationDiagnostics)
      mpProbe.addProbeDetails(isMethod, isMsg);
  }
}
