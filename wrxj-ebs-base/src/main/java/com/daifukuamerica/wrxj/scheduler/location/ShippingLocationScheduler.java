package com.daifukuamerica.wrxj.scheduler.location;
/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

//
//                   Daifuku America Corporation
//                       International Center
//                    5202 Douglas Corrigan Way
//                 Salt Lake City, Utah  84116-3192
//                          (801) 359-9900
//
// This software is furnished under a license and may be used
// and copied only in accordance with the terms of such license.
// This software or any other copies thereof in any form, may not be
// provided or otherwise made available, to any other person or company
// without written consent from Daifuku America Corporation.
//
// Daifuku America Corporation assumes no responsibility for the use 
// or reliability of software which has been modified without approval.
//

import com.daifukuamerica.wrxj.allocator.AllocationMessageDataFormat;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.scheduler.Scheduler;
import com.daifukuamerica.wrxj.timer.RestartableTimerTask;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The ShippingLocationScheduler is the object that schedules all load movement
 * between SRC's, The ShippingLocationScheduler determines if a station needs
 * more loads staged for it. If it does a scheduler event is published to tell
 * the allocator to allocate an order for this station. If a station needs a
 * load moved from it to a storage location or if a load needs to be moved from
 * a storage location to the station based on priority. A load event is
 * published to a station device to move the load. The ShippingLocationScheduler
 * updates the load move status based on messages received from the station
 * device through a load event.
 * 
 * @author A.T.
 * @version 1.0
 */
public class ShippingLocationScheduler extends Scheduler
{
  private boolean           _DEBUG         = false;
  protected AllocationMessageDataFormat allocatorMessage;
  private List stationList = new ArrayList();

  private int recheckInterval = 60000;
  private RecheckIntervalTimeout recheckIntervalTimeout =
                new RecheckIntervalTimeout();
  
  public ShippingLocationScheduler()
  {
    this(null);
  }
  
  public ShippingLocationScheduler(String name)
  {
    super(name);
    _DEBUG = Application.getBoolean("DBG");
  }

  /**
   *  Method to Initialize everything need to run the ShippingLocationScheduler.
   */
  @Override
  public void startup()
  {
    super.startup();
    logger.logDebug("ShippingLocationScheduler.startup() - Start Scheduler" + getSchedulerName());
    allocatorMessage = new AllocationMessageDataFormat(getSchedulerName());
    logger.logDebug("ShippingLocationScheduler.createLoadServer()");

    String [] stations = SKDCUtility.getTokens(getConfigProperty("StationList"), ",");
    if (stations.length == 0)
    {
      System.out.println("Shipping Stations are not defined.");
    }
    StandardStationServer stationServer = Factory.create(StandardStationServer.class);
     
    for (int i = 0; i < stations.length; i++)
    {
      StationData station = stationServer.getStation(stations[i]);
      if (station != null)
      {
        stationList.add(stations[i]);
      }
    }
    logger.logDebug("Adding Shipping Stations " + stationList.toString());
    
    String sRecheckInterval = getConfigProperty("RecheckInterval");
    if (sRecheckInterval != null)
    {
      int iRecheckInterval = getConfigPropertyAsInt("RecheckInterval");
      if (iRecheckInterval >= 0)
      {
        logger.logDebug("RecheckInterval: " + sRecheckInterval + " - startup()");
        recheckInterval = iRecheckInterval;
      }
      else
      {
        logger.logError("INVALID RecheckInterval \"" + sRecheckInterval + "\" - startup()");
      }
    }
    else
    {
       logger.logDebug("Using Default RecheckInterval: " + recheckInterval + " NO RecheckInterval in Config - startup()");
    }
    timers.setPeriodicTimerEvent(recheckIntervalTimeout, recheckInterval);
    logger.logDebug("ShippingLocationScheduler.startup() - End");
  }

  /**
   *  Shuts down this controller by cancelling any timers and shutting down the
   *  Equipment.
   */
  @Override
  public void shutdown()
  {
    logger.logDebug("ShippingLocationScheduler.shutdown() -- Start");
    allocatorMessage = null;
    logger.logDebug("ShippingLocationScheduler.shutdown() -- End");
    super.shutdown();
  }

  /**
   *  We have received an Station EVENT
   *  
   *
   *  @param receiveEventString the received data (String) is in "receivedText".
   */
  @Override
  protected void processStationEvent(String receiveEventString)
  {
  }

  /**
   *  We have received a Load EVENT.  This method will decode the message and
   *  determine which message was received.  The correct method will then be called
   *  to handle that message.
   *
   *  @param receiveEventString the received data (String) is in "receivedText".
   */
  @Override
  protected void processLoadEvent(String receiveEventString)
  {
  }
  
  /**
   *  We have received an scheduler EVENT.  This is a load that is retrieve pending
   *  for one of this schedulers stations and I need to schedule it.  There may be
   *  higher priority or older loads waiting before it so check to see if I can
   *  move any loads.  If I can publish the loadEvent.
   *
   *  @param receiveEventString the received data (String) is in "receivedText".
   */
  @Override
  protected void processSchedulerEvent(String receiveEventString)
  {
  }

  /**
  * Send a message to the allocator that this station needs more loads staged.
  *
  * @param stationName the name of the station that needs loads
  */
  protected void publishMessageToAllocator(String stationName, String warehouse)
  {
    if(stationName != null)
    {
      allocatorMessage.clear();
      allocatorMessage.setOutputStation(stationName);
      allocatorMessage.setFromWarehouse(warehouse);
      allocatorMessage.createDataString();
      transmitAllocateEvent(allocatorMessage.createStringToSend(), AllocationMessageDataFormat.ALLOCATE_SHIPPING);
    }
  }

  /**
   * Factory for ControllerImplFactory.
   *
   * <p><b>Details:</b> <code>create</code> is a factory method used exclusively
   * by <code>ControllerImplFactory</code>.  Configurable properties of a new
   * controller created using this method are initialized using data in the
   * supplied properties object.  If the controller cannot be created, a
   * <code>ControllerCreationException</code> is thrown.</p>
   *
   * @param ipConfig configurable property definitions
   * @return the created controller
   * @throws ControllerCreationException if an error occurred while creating the controller
   */
  public static Controller create(ReadOnlyProperties ipConfig) throws ControllerCreationException
  {
//    String vsDeviceId = ipConfig.getString(DEVICE_ID);
//    if (vsDeviceId == null)
//      throw new ControllerCreationException("Unable to create ShippingingLocationScheduler: DeviceID undefined");
//    Controller vpController = new AGCScheduler(vsDeviceId);
//    vpController.setCollaboratorCKN(ipConfig.getString(COLLABORATOR));
    Controller vpController = Factory.create(ShippingLocationScheduler.class);
    return vpController;
  }
  
  /*--------------------------------------------------------------------------*/
  private class RecheckIntervalTimeout extends RestartableTimerTask
  {
    /*------------------------------------------------------------------------*/
    /**
     * run -- the LocalTimerTask's run() needs to be synchronized on thisController
     * so that any work we do here is not interrupted by any incoming messages
     * or events that we generate here.  We want to complete anything we do here
     * without being pre-empted.
     */
    public void run()
    {
      synchronized(ShippingLocationScheduler.this)
      {
        checkForTimerWork();
     }
    }
  }

  /**
   *  Wakes up the ShippingLocationScheduler and does all checks for its stations.  This will
   *  check to see if any stations needs more loads staged (needs work).  It also
   *  checks to see if there are any loads to retrieve to any stations and it will
   *  see if there are any loads to store at any of the schedulers stations
   *  Equipment.
   */
  protected void checkForTimerWork()
  {
    logger.logDebug("ShippingLocationScheduler.checkForTimerWork()");
    for(Iterator it = stationList.iterator(); it.hasNext();)
    {
      checkStation(new String(it.next().toString()));
    }
  }

 /**
  *  Method to check for location avaibility and to keep the station busy.
  *
  */
  public void checkStation(String isStation)
  {
//    logger.logDebug("Checking station: " + isStation);
    StandardLocationServer locationServer = Factory.create(StandardLocationServer.class);
    StandardStationServer stationServer = Factory.create(StandardStationServer.class);

    try
    {
      // make sure station has allocations enabled for it
      
      StationData station = stationServer.getStation(isStation);
      
      if (station.getAllocationEnabled() == DBConstants.NO)
      {
        if (_DEBUG) logger.logDebug("Station " + isStation + " has allocations disabled");
        return;
      }
      
      // see if we have locations that need work
      
      int locationCount = locationServer.getLocationCount(null, null,
          stationServer.getStationAisleGroup(isStation),
          DBConstants.LCSHIPPING, DBConstants.LCAVAIL, DBConstants.UNOCCUPIED,
          -1);
      if (locationCount > 0)
      {
        if (_DEBUG) logger.logDebug("Found " + locationCount + " locations needing assignment");
        publishMessageToAllocator(station.getStationName(), station.getWarehouse());
      }
      
    }
    catch (DBException e)
    {
       logger.logError(e.getMessage());
    }
  }

}
