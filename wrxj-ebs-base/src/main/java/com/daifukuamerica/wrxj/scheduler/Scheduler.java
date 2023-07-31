package com.daifukuamerica.wrxj.scheduler;

//
//                      SKDIAFUFU Corporation
//                     International Center
//                 5202 Douglas Corrigan Way
//              Salt Lake City, Utah  84116-3192
//                      (801) 359-9900
//
// This software is furnished under a license and may be used
// and copied only in accordance with the terms of such license.
// This software or any other copies thereof in any form, may not be
// provided or otherwise made available, to any other person or company
// without written consent from SKDC Corporation.
//
// SKDC assumes no responsibility for the use or reliability of
// software which has been modified without approval.
//

import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Base Class for all types of schedulers.
 * This base class will do all the subscribing and publishing for all schedulers
 *
 * The <tt>Scheduler</tt> will publish the following events.
 * <i>LoadEvent, SchedulerEvent, StationEvent </i>.
 *
 * The <tt>Scheduler</tt> subscribes to the
 * <i>LoadEvent, StationEvent, SchedulerEvent</i>.  After the event happens the
 * received string is sent in the Abstract method for that event.  Each
 * child class can then do what it needs for that event.
 *
 * @author Ed Askew
 * @version 1.0
 */

public abstract class Scheduler extends Controller
{

  private String schedulerName = "";
  private boolean bProcessAgain = false;
  protected List<String> mpCollaborators = new ArrayList<String>();

  /**
   * Abstract Class used to process events about Stations.  From
   *  Screens Etc.
   */
  protected abstract void processStationEvent(String receivedString);

  /**
   * Abstract Class used to process events about load movement.  From
   * StationDevice, Screens Etc.
   */
  protected abstract void processLoadEvent(String receivedString);

  /**
   * Abstract method
   *  used to process events to and from the allocator.
   */
  protected abstract void processSchedulerEvent(String receivedString);

  /**
   * The scheduler Constructor will initialize the schedulerName. This is what
   * this scheduler will be called.  It will only be set at initailization.
   * @param name scheduler's key name
   */
  public Scheduler(String name)
  {
    setSchedulerName(name);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * The Base Controller initialized the controller by setting the controller's
   * KeyName, attaching a logger, starting up the topic subscribers and
   * publishers for the message service, and starting the Thread where messages
   * will be processed.
   *
   * The <tt>Scheduler</tt> finds its collaborators (but doesn't use them),
   * subscribe to <i>LoadEvent, SchedulerEvent, StationEvent</i>.
   *
   * @param aControllerKeyName the unique name that identifies this instance of Controller
   */

  @Override
  public void initialize(String aControllerKeyName)
  {
    super.initialize(aControllerKeyName);
    logger.logDebug("Scheduler.initialize() - Start");
    
    if (collaboratorCKN != null)
    {
      String[] vasCollabs = SKDCUtility.getTokens(collaboratorCKN, ",");
      if (vasCollabs.length == 0)
      {
        mpCollaborators.add(collaboratorCKN);
      }
      else
      {
        mpCollaborators = Arrays.asList(vasCollabs);
      }
    }
    
    subscribeLoadEvent();     // Load messages
    for (String vsCollab : mpCollaborators)
    {
      subscribeLoadEvent(vsCollab);
      subscribeStationEvent(vsCollab);   // Station messages
    }
    subscribeSchedulerEvent();  // Allocator messages
    subscribeStationEvent();   // Station messages
    subscribeControlEvent();   // Device online message
    logger.logDebug("Scheduler.initialize() - End");
  }


  /**
   * This method changes the controller status to Running
   */
  @Override
  public void startup()
  {
    super.startup();
    logger.logDebug("Scheduler.startup() - Start");
    setControllerStatus(ControllerConsts.STATUS_RUNNING);
    logger.logDebug("scheduler.startup() - End");
  }

  /**
   * This method Shuts down this controller by cancelling any timers
   */
  @Override
  public void shutdown()
  {
    logger.logDebug("Scheduler.shutdown() -- Start");
    schedulerName = null;
    logger.logDebug("AGCScheduler.shutdown() -- End");
    super.shutdown();
  }

  /**
   * This method prcessed the Inter-Process-Communication Message received
   * to the correct message and calls the process method for that particuler message.
   */
  @Override
  protected void processIPCReceivedMessage()
  {
//    logger.logDebug("Scheduler.processIPCReceivedMessage() - Start");
    super.processIPCReceivedMessage();
    if (!receivedMessageProcessed)
    {
      receivedMessageProcessed = true;
      switch (receivedEventType)
      {
        case MessageEventConsts.LOAD_EVENT_TYPE:
                                  processLoadEvent(receivedText);
                                  break;
        case MessageEventConsts.SCHEDULER_EVENT_TYPE:
                                  processSchedulerEvent(receivedText);
                                  break;
        case MessageEventConsts.STATION_EVENT_TYPE:
                                  processStationEvent(receivedText);
                                  break;
        default: receivedMessageProcessed = false;
      }
    }
    do
    {
      bProcessAgain = false; // Some methods may set this to true.
//      checkForWork();
    }
    while (bProcessAgain);
  }

  /**
   * This method publishes a LoadEvent using the string publishString.
   * @see com.daifukuamerica.wrxj.common.controller.Controller#publishLoadEvent()
   */
  protected void transmitLoadEvent(String publishString)
  {
    publishLoadEvent(publishString, 0, collaboratorCKN);
  }
  
  /**
   * This method publishes a LoadEvent to an AGCDevice
   * @param publishString The message to send
   * @param CKN The controller name of the AGCDevice that will receive the message.
   */
  protected void transmitLoadEvent(String publishString, String CKN)
  {
    publishLoadEvent(publishString, 0, CKN);
  }

  /**
   * This method publishes a SchedulerEvent using the string publishString.
   * @see com.daifukuamerica.wrxj.common.controller.Controller#publishSchedulerEvent()
   */
  protected void transmitSchedulerEvent(String publishString)
  {
    publishSchedulerEvent(publishString, 0);
  }

  /**
   * This method publishes a AllocateEvent to the AllocationController
   * @param publishString <code>String</code> containing message to send to
   *        allocator.
   * @see com.daifukuamerica.wrxj.common.controller.Controller#publishAllocateEvent()
   */
  protected void transmitAllocateEvent(String publishString)
  {
    transmitAllocateEvent(publishString, 0);
  }

  /**
   * This method publishes a AllocateEvent to the AllocationController
   * @param publishString <code>String</code> containing message to send to
   *        allocator.
   * @param messageData the int data content to be sent
   * @see com.daifukuamerica.wrxj.common.controller.Controller#publishAllocateEvent()
   */
  protected void transmitAllocateEvent(String publishString, int messageData )
  {
    publishAllocateEvent(publishString, messageData);
  }

  /**
   * This method publishes a StationEvent using the string publishString.
   * @see com.daifukuamerica.wrxj.common.controller.Controller#publishStationEvent()
   */
  protected void transmitStationEvent(String publishString)
  {
    logger.logDebug("transmitStationEvent() - \"" + publishString + "\"");
    publishStationEvent(publishString, 0, collaboratorCKN);
//    logger.logDebug("transmitStationEvent() -- \"" + publishString + "\"Done");
  }

  /**
   * This method sets the schedulerName to the string passed to it.  This method is only
   * called in the constructor.
   * @param isSchedulerName
   */
  private void setSchedulerName(String isSchedulerName)
  {
    schedulerName = isSchedulerName;
  }

  /**
   * This method returns the schedulerName.
   */
  public String getSchedulerName()
  {
    return schedulerName;
  }
}