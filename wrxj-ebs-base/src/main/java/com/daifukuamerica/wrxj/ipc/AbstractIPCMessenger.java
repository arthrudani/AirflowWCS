/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright © 2007 Daifuku America Corporation  All Rights Reserved.
 
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS 
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND 
  CIVIL LIABILITY.
 ****************************************************************************/

package com.daifukuamerica.wrxj.ipc;

import com.daifukuamerica.wrxj.WarehouseRx;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.observer.ObservableControllerImpl;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.timer.RestartableTimer;
import com.daifukuamerica.wrxj.util.UnusedMethod;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observer;

/**
 * <B>Description:</B> This contains methods for IPC messaging that were
 * formerly located in both <code>Controller</code> and
 * <code>SystemGateway</code>. Yep, 1000 lines of duplicated code. Anyway,
 * this is now the superclass of both <code>Controller</code> and
 * <code>SystemGateway</code>, which handle messaging primarily for 
 * controllers and for user screens respectively.
 * 
 * <P><B>NOTE:</B>  For the publish*Event methods:
 * <UL>
 *  <LI> Anything that is "protected final" is currently only called only 
 *       by Controller (or it's subclasses).
 *  <LI> Anything that is "public final synchronized" is called by
 *       SystemGateway and possibly Controller.
 * </UL>
 * I don't think synchronization within <code>Controller</code> will hurt us, 
 * but if it does we can separate the the publish*Event methods back into 
 * <code>Controller</code> and <code>SystemGateway</code>.</P>
 * 
 * @author mandrus<BR>
 * @version 1.0
 * 
 * <BR>
 * Copyright (c) 2007 by Daifuku America Corporation
 */
public abstract class AbstractIPCMessenger
  implements Runnable, MessageEventHandling
{
  /**
   * Avoid endless "Thread cancer"
   */
  private int MAX_CANCER = 10;
  private int mnCurrentCancer = 0;
  
  /**
   * The Logging implementation for this named subsystem to use.
   */
  protected Logger logger = null;

  /**
   * Timers that are available to the instantiated objects to use.
   */
  protected RestartableTimer timers = null;


  /*========================================================================*/
  /*  Received Message Stuff  TODO: Convert to a class (or use IpcMessage)  */
  /*========================================================================*/
  
  /**
   * The EVENT type of a received inter-process-communication message.
   */
  protected int    receivedEventType = 0; // the int EVENT TYPE of the Message
  
  /**
   * A received inter-process-communication message's SENDER's key name.
   */
  protected String receivedCKN = "";
  
  /**
   * A received inter-process-communication message's SENDER's controller group.
   */
  protected String receivedControllerGroupName = "";

  /**
   *  A received inter-process-communication message's subscriber's selector.
   */
  protected String receivedSelector = null;

  /**
   *  A received inter-process-communication message's String data content.
   */
  protected String receivedText = null;
  
  /**
   *  A received inter-process-communication message's int data content.
   */
  protected int    receivedData = 0;
  
  /**
   *  A received inter-process-communication message's long data content.
   */
  protected long   receivedDataLong = 0;
  
  /**
   *  A received inter-process-communication message's transmit/send time.
   */
  protected long   receivedTxTime = 0;
  
  /**
   *  A received inter-process-communication message's receive time.
   */
  protected long   receivedRxTime = 0;

  /**
   * the String EVENT TYPE of the Message
   */
  protected String receivedEvent; 
  
  protected String receivedMessageSender;
  
  /**
   * Set true when a received inter-process-communication message has been
   * handled.  Messages un-processed by both the messenger and any observers
   * are logged as errors, since messengers should only be receiving messages
   * that they can handle.
   */
  protected boolean receivedMessageProcessed;
  
  /**
   * Set true when a received inter-process-communication message has been
   * handled by an event observer attached to a messenger. Messages un-processed
   * by both the messenger and any observers are logged as errors, since
   * messengers should only be receiving messages that they can handle.
   */
  protected boolean observedMessageProcessed;


  /* ======================================================================== */
  /*  Thread/Object Identity Stuff                                          */
  /*========================================================================*/
  /**
   * The mode the system is running in (Ctlrs or Client).
   */
  protected static String WRX_RUN_MODE = Application.getString(WarehouseRx.RUN_MODE);

  /**
   * The unique String identifier of the messenger within an entire system.
   */
  protected String controllersKeyName = null;

  /**
   * Thread for naming
   */
  protected Thread selfThread;

  /**
   * The name of this thread. For some strange reason, may be different than
   * controllersKeyName
   */
  protected String msName;

  
  /*========================================================================*/
  /*  Thread Management Fields                                              */
  /*========================================================================*/
  private volatile boolean mzQuit = false;
  private boolean mzNeedToStartup = false;
  private boolean mzNeedToShutdown = false;
  private boolean mzOkToProcessMessages = false;

  
  /*========================================================================*/
  /*  Message Management Fields                                             */
  /*========================================================================*/
  private MessageService mpMessageService;
  
  private List<IpcMessage> mpInMessageQueue = 
    Collections.synchronizedList(new ArrayList<IpcMessage>());

  private ObservableControllerImpl[] mapEventObservers = 
    new ObservableControllerImpl[MessageEventConsts.EVENT_TEXT.length];

  
  /*========================================================================*/
  /*  Getter & Setter Methods                                               */
  /*========================================================================*/

  /**
   * Set the name of this object
   * @param isName
   */
  protected void setName(String isName)
  {
    controllersKeyName = isName;
    msName = isName;
  }
  
  /**
   * Get the name of this object
   * @return
   */
  public final String getName()
  {
    return msName;
  }

  /**
   * Specify the client for the messenger to use for publishing/subscribing
   * message evnts to the Inter-Process-Communication message service 
   * <i>Server</i>.
   * 
   * @param ipMessageService an inter-process-communication provider
   */
  public void setMessageService(MessageService ipMessageService)
  {
    mpMessageService = ipMessageService;
  }

  /**
   * Fetch the client for the messenger to use for publishing/subscribing
   * message events to the Inter-Process-Communication message service 
   * <i>Server</i>.
   * 
   * @return an inter-process-communication provider
   */
  public MessageService getMessageService()
  {
    return mpMessageService;
  }

  /**
   * Attach an application Logging implementation. The logger being attached is
   * for the caller's named sub-system (the messenger).
   * 
   * @param ipLogger the application logger
   */
  public final void setLogger(Object ipLogger)
  {
    logger = (Logger)ipLogger;
  }



  /*========================================================================*/
  /*  Event / Message Reception                                             */
  /*========================================================================*/
  /**
   * Decode an IPC message
   * <LI>Convert an IpcMessage to Stephen's stupid global variables</LI>
   * <LI>Log the message if it is not log or heart-beat related</LI>
   */
  protected void decodeIpcMessage(IpcMessage receivedMessage)
  {
    receivedEventType = receivedMessage.getEventType(); // the int EVENT TYPE of the Message
    if ((receivedEventType != MessageEventConsts.LOG_EVENT_TYPE) &&
        (receivedEventType != MessageEventConsts.HEARTBEAT_REQUEST_EVENT_TYPE) &&
        (receivedEventType != MessageEventConsts.HEARTBEAT_RESPONSE_EVENT_TYPE))
    {
      logReceivedMessage(receivedMessage);
    }
    //MCM
    receivedMessageSender = receivedMessage.getMessageSender();
    receivedTxTime = receivedMessage.getMessageTxTime();
    receivedRxTime = receivedMessage.getMessageRxTime();
    receivedText = receivedMessage.getMessageText();
    receivedSelector = receivedMessage.getMessageSelector();
    receivedDataLong = receivedMessage.getMessageData();
    if (receivedDataLong <= Integer.MAX_VALUE)
    {
      receivedData = (int)receivedMessage.getMessageData();
    }
    else
    {
      receivedData = Integer.MAX_VALUE;
    }
    receivedEvent = receivedMessage.getEvent(); // the String EVENT TYPE of the Message
    receivedCKN = receivedMessage.getMessageSender();
    receivedControllerGroupName = receivedMessage.getMessageSenderControllerGroup();
  }

  
  /**
   * Extracts the message text and event type from a messenger message and gives
   * it to the message logger. This is in here and not our logger because our
   * logger does not (and probably should not) know about these kinds of
   * messages.
   * 
   * @param message the message to be logged
   */
  protected void logReceivedMessage(IpcMessage message)
  {
    logger.logReceivedMessage(message.getMessageText(), message.getEvent());
  }

  /**
   * Handle a message event received from the Inter-Process-Communication
   * message service <i>Server</i>.
   *
   * <p>The following data has been automatically retrieved from an ipc received
   * message and is available for the message's processing method to use:
   *
   * <p>String  receivedTxTime - the Date/Time (long) of the Message transmission
   * <p>String  receivedRxTime - the Date/Time (long) of the Message reception
   * <p>String  receivedEvent - the String EVENT TYPE of the Message
   * <p>int     receivedEventType - the int EVENT TYPE of the Message
   *
   * <p>String  receivedCKN - sender's name (String)
   *
   * <p>The message content (String, int & long) data fields.
   *
   * <p>String  receivedText
   * <p>int     receivedData
   * <p>long    receivedDataLong
   */
  protected void processIPCReceivedMessage()
  {
    // (Decide how to) Process message here
    receivedMessageProcessed = true;
    switch (receivedEventType)
    {
      case MessageEventConsts.STATUS_EVENT_TYPE:
        processStatusEvent();
        break;
      case MessageEventConsts.HEARTBEAT_REQUEST_EVENT_TYPE:
        processHeartbeatRequestEvent();
        break;
      case MessageEventConsts.CONTROL_EVENT_TYPE:
        processControlEvent();
        break;
      case MessageEventConsts.CONTROLLER_REQUEST_EVENT_TYPE:
        processRequestEvent();
        break;
      default:
        receivedMessageProcessed = false;
    }
    processEventObservers();
  }

  /**
   * Handle a received "Control" event message. "Control" events request changes
   * in Controller/Device/Machine states and elaborated states.
   */
  protected void processControlEvent() {}

  /**
   * Handle a received "Heartbeat Request" message event. The received Date/Time
   * is used to compute the time taken to receive the heartbeat request. The
   * elapsed time is then sent back as part of the heartbeat response.
   */
  protected void processHeartbeatRequestEvent() {}

  /**
   * Child messengers can use this method for any event processing beyond that
   * required for Inter-Process-Communication message processing.
   */
  protected void processLocalEvent() {}

  /**
   * Handle a received "Controller Request" message event.
   */
  protected void processRequestEvent() {}

  /**
   * Handle a received "Status" event message. "Status" events report changes in
   * Controller/Device/Machine states and elaborated states.
   */
  protected void processStatusEvent() {}

  
  /**
   * Wake up observers to process a newly received message
   */
  protected void processEventObservers()
  {
    observedMessageProcessed = false;
    if ((receivedEventType >= 0) && 
        (receivedEventType < MessageEventConsts.EVENT_TEXT.length))
    {
      ObservableControllerImpl vpObserver = mapEventObservers[receivedEventType];
      if (vpObserver.hasObservers(receivedSelector))
      {
        vpObserver.setKeyName(receivedCKN);
        vpObserver.setIntData(receivedData);
        vpObserver.setStringData(receivedText);
        vpObserver.setControllerGroupName(receivedControllerGroupName);
        vpObserver.notifyObservers(receivedSelector);
        observedMessageProcessed = true;
      }
    }
    else
    {
      logger.logError("INVALID Event Type: " + receivedEventType
          + " - processEventObservers()");
    }
  }


  /*========================================================================*/
  /*  Subscriptions                                                         */
  /*========================================================================*/
  /**
   * Register to receive specific messages.
   *
   * @param selector received message filter criteria
   * @param noLocal if <i>true</i>, inhibits the delivery of messages from self
   */
  protected void startupTopicSubscriber(String selector, boolean noLocal)
  {
    mpMessageService.addSubscriber(selector, noLocal ? getName() : null);
  }

  /**
   * Subscribe for message events filtered by the "Request" selector. <b>ALL</b>
   * Controllers subscribe to "Requests" and publish the appropriate status
   * event messages in reply.
   */
  protected final void subscribeRequestEvent()
  {
    startupTopicSubscriber(MessageEventConsts.REQUEST_EVENT_TYPE_TEXT + msName,
        true);
  }
  
  /**
   * Unsubscribe to event.
   *
   * <p><b>Details:</b> <code>unsubscribeForEvent</code> unsubscribes to the
   * inter-<wbr>process-<wbr>communication Event specified by the caller's
   * selector.</p>
   *
   * @param isSelector event filter
   */
  public synchronized void unsubscribeForEvent(String isSelector)
  {
    mpMessageService.shutdownSubscriber(isSelector);
  }

  
  /*========================================================================*/
  /*  Observer Methods                                                      */
  /*========================================================================*/

  /**
   * Add a message observer (ignore messages from self)
   * 
   * @param inMessageType
   * @param isSelector
   * @param ipObserver
   */
  public synchronized void addObserver(int inMessageType, String isSelector,
      Observer ipObserver)
  {
    addObserver(inMessageType, isSelector, ipObserver, true);
  }
  
  /**
   * Add a message observer
   * 
   * @param inMessageType
   * @param isSelector
   * @param ipObserver
   * @param izNoLocal
   */
  public synchronized void addObserver(int inMessageType, String isSelector,
      Observer ipObserver, boolean izNoLocal)
  {
    if ((inMessageType >= 0) && 
        (inMessageType < MessageEventConsts.EVENT_TEXT.length))
    {
      logger.logDebug("Adding Observer for Event Type: "
          + MessageEventConsts.EVENT_TEXT[inMessageType] + " - addObserver()");
      ObservableControllerImpl vpObserver = mapEventObservers[inMessageType];
      boolean vzSubscriptionNeeded = vpObserver.addObserver(isSelector,
          ipObserver);
      if ((isSelector.length() > 0) && (vzSubscriptionNeeded))
      {
        startupTopicSubscriber(isSelector, izNoLocal);
      }
    }
    else
    {
      logger.logError("INVALID Observer for Event Type: " + inMessageType
          + " - addObserver()");
    }
  }

  /**
   * Delete a message observer
   * 
   * @param inMessageType
   * @param ipObserver
   */
  public synchronized void deleteObserver(int inMessageType, Observer ipObserver)
  {
    if ((inMessageType >= 0)
        && (inMessageType < MessageEventConsts.EVENT_TEXT.length))
    {
      if (mapEventObservers != null)
      {
        ObservableControllerImpl eventObserver = mapEventObservers[inMessageType];
        String vsSelector = eventObserver.removeObserver(ipObserver);
        logger.logDebug("Deleting Observer for Event Type: "
            + MessageEventConsts.EVENT_TEXT[inMessageType]
            + " - deleteObserver()");
        if (vsSelector != null)
        {
          unsubscribeForEvent(vsSelector);
        }
      }
    }
    else
    {
      logger.logError("INVALID Observer Type: " + inMessageType
          + " - deleteObserver()");
    }
  }


  /*========================================================================*/
  /*  General Event Publishing                                              */
  /*========================================================================*/
  /**
   * Publish a message event to the Inter-Process-Communication message service
   * <i>Server</i>.
   *
   * @param messageSender unique key identifying the message's sender
   * @param messageText the String data content
   * @param messageData the int data data content
   * @param eventType the message classification
   * @param event  the message detailed classification
   */
  protected void publishEvent(String messageSender, String messageText,
      int messageData, int eventType, String event)
  {
    mpMessageService.publishEvent(messageSender, WRX_RUN_MODE, messageText,
        messageData, eventType, event);
  }

  /**
   * Publish a message event to the Inter-Process-Communication message service
   * <i>Server</i>.
   *
   * @param messageSender unique key identifying the message's sender
   * @param messageText the String data content
   * @param messageData the long data data content
   * @param eventType the message classification
   * @param event the message detailed classification
   */
  protected void publishEvent(String messageSender, String messageText,
      long messageData, int eventType, String event)
  {
    mpMessageService.publishEvent(messageSender, WRX_RUN_MODE, messageText,
        messageData, eventType, event);
  }

  
  /*========================================================================*/
  /*  Specific Event Publishing                                             */
  /*  SEE NOTE IN CLASS JAVADOC                                             */
  /*========================================================================*/
  /**
   * Publish an Allocate event to the AllocationController so it can find orders
   * to allocate.
   * 
   * @param messageText the String data content to be sent
   * @param messageData the int data content to be sent
   */
  protected final void publishAllocateEvent(String messageText, int messageData)
  {
    publishAllocateEvent(messageText, messageData, msName);
  }

  /**
   * Publish an Allocate event to the AllocationController so it can find orders
   * to allocate.
   * 
   * @param messageText the String data content to be sent
   * @param messageData the int data content to be sent
   * @param sCKN the message destination
   */
  protected final void publishAllocateEvent(String messageText, int messageData,
      String sCKN)
  {
    if (sCKN == null)
    {
      logger.logDebug("Destination Key-Name is NULL - publishAllocateEvent()");
      sCKN = "";
    }
    publishEvent(msName, messageText, messageData,
        MessageEventConsts.ALLOCATE_EVENT_TYPE,
        MessageEventConsts.ALLOCATE_EVENT_TYPE_TEXT + sCKN);
  }
  
  /**
   * Publish an Allocate event to the AllocationController so it can find orders
   * to allocate.
   * 
   * @param messageText the String data content to be sent
   * @param messageData the int data content to be sent
   * @param sCKN the message destination
   */
  public final synchronized void publishAllocationProbeEvent(String messageText,
      int messageData, String sCKN)
  {
    if (sCKN == null)
    {
      logger.logDebug("Destination Key-Name is NULL - publishAllocationProbeEvent()");
      sCKN = "";
    }
    publishEvent(msName, messageText, messageData,
        MessageEventConsts.ALLOCATION_PROBE_EVENT_TYPE,
        MessageEventConsts.ALLOCATION_PROBE_EVENT_TYPE_TEXT + sCKN);
  }

  /**
   * Publishes control message to device emulator.
   * 
   * <p><b>Details:</b> <code>publishControlEvent</code> publishes a Control
   * category message to the specified device using the 
   * inter-<wbr>process-<wbr>communication message service.</p>
   * 
   * @param isText the message text string to be sent as the message body
   * @param inData an int with data defined by the message type
   * @param isCkn (no information available)
   */
  public final synchronized void publishControlEvent(String isText, int inData,
      String isCkn)
  {
    publishEvent(msName, isText, inData,
        MessageEventConsts.CONTROL_EVENT_TYPE,
        MessageEventConsts.CONTROL_EVENT_TYPE_TEXT + isCkn);
  }

  /**
   * Publishes a custom message.
   * 
   * <P><I>Note that within baseline this is, by definition, an UnusedMethod.  
   * This method should never be used in baseline, and only exists to support
   * custom messages in customer projects.  See Liberty Hardware for an example
   * of this method in use.</I></P>
   * 
   * @param isText the message text string to be sent as the message body
   * @param inData an int with data defined by the message type
   * @param isCkn name of intended receiver
   * @param inEventType custom event type (make sure not to duplicate baseline)
   * @param isEventName custom event name
   */
  @UnusedMethod
  public final synchronized void publishCustomEvent(String isText, int inData,
      String isCkn, int inEventType, String isEventName)
  {
    publishEvent(msName, isText, inData, inEventType, isEventName + isCkn);
  }

  /**
   * Publish an "Equipment" Message to the Inter-Process-Communication message
   * service <i>Server</i>.
   * 
   * @param messageText the String data content to be sent
   * @param messageData the int data content to be sent
   * @param sCKN the message destination
   */
  public final void publishEquipmentEvent(String messageText,
      int messageData, String sCKN)
  {
    publishEvent(msName, messageText, messageData,
        MessageEventConsts.EQUIPMENT_EVENT_TYPE,
        MessageEventConsts.PORT_EQPMNT_EVENT_TYPE_TEXT + sCKN);
  }
  
  public final void publishCommEvent(String messageText,
          int messageData, String sCKN)
 {
   publishEvent(msName, messageText, messageData,
       MessageEventConsts.COMM_EVENT_TYPE,
       MessageEventConsts.COMM_EVENT_TYPE_TEXT + sCKN);
 }

  /**
   * Publish a "Heartbeat Request" Message to the Inter-Process-Communication
   * message service <i>Server</i>.
   * 
   * @param dateTime the echoed date/time of the heartbeat request
   */
  protected final void publishHeartbeatStatusEvent(long dateTime)
  {
    String nameGroup = msName;
    publishEvent(msName, ControllerConsts.HEARTBEAT_STATUS + " ", dateTime,
        MessageEventConsts.HEARTBEAT_RESPONSE_EVENT_TYPE,
        MessageEventConsts.HEARTBEAT_RESPONSE_EVENT_TYPE_TEXT + nameGroup);
  }

  /**
   * Publish a Host message receipt notification when a message is received from
   * the host.
   *
   * @param messageText the String data content to be sent
   * @param messageData the int data content to be sent
   * @param sCKN the name of a specific subscriber this message is directed to.
   */
  public final void publishHostMesgReceiveEvent(String messageText, int messageData,
                                          String sCKN)
  {
    if (sCKN == null)
    {
      logger.logDebug("Destination Key-Name is NULL - publishHostMesgReceiveEvent()");
      sCKN = "";
    }
    publishEvent(msName, messageText, messageData,
        MessageEventConsts.HOST_MESG_RECV_EVENT_TYPE,
        MessageEventConsts.HOST_MESG_RECV_TEXT + sCKN);
  }

  /**
   * Publish a Host message send notification when a message is ready to be sent
   * to the host.
   * 
   * @param messageText the String data content to be sent
   * @param messageData the int data content to be sent
   */
  public final void publishHostMesgSendEvent(String messageText, int messageData)
  {
    if (!msName.equals(controllersKeyName))
    {
      // Controller    used msName
      // SystemGateway used controllersKeyName
      // Mike thinks they're the same
      logger.logError("Mike was wrong: \"" + msName + "\" != \"" 
          + controllersKeyName + "\"");
    }
    publishHostMesgSendEvent(messageText, messageData, controllersKeyName);
  }

  /**
   * Publish a Host message send notification when a message is ready to be sent
   * to the host.
   *
   * @param messageText the String data content to be sent
   * @param messageData the int data content to be sent
   * @param sCKN the name of a specific subscriber this message is directed to.
   */
  public final void publishHostMesgSendEvent(String messageText, int messageData,
                                       String sCKN)
  {
    if (sCKN == null)
    {
      logger.logDebug("Destination Key-Name is NULL - publishHostMesgSendEvent()");
      sCKN = "";
    }
    publishEvent(msName, messageText, messageData,
        MessageEventConsts.HOST_MESG_SEND_EVENT_TYPE,
        MessageEventConsts.HOST_MESG_SEND_TEXT + sCKN);
  }
  
  /**
   * Publish a host exepected receipt event
   * @param expectedReceiptMessage csv formatted received message
   */
  public final void publishHostExpectedReceiptEvent(String expectedReceiptMessage)
  {
    publishEvent(msName, expectedReceiptMessage, 0,
        MessageEventConsts.HOST_EXPECTED_RECEIPT_EVENT_TYPE,
        MessageEventConsts.HOST_EXPECTED_RECEIPT_EVENT_TEXT);
  }
  
  public final void subscribeHostExpectedReceiptEvent(String sCKN)
  {
    if (sCKN != null)
    {
      startupTopicSubscriber(
          MessageEventConsts.HOST_EXPECTED_RECEIPT_EVENT_TEXT + sCKN, true);
    }
    else
    {
      logger.logError("subscribeHostExpectedReceiptEvent() -- name == null");
    }
  }
  
  /**
   * Publish a host flight data update event
   * @param flightDataUpdateMessage csv formatted received message
   */
  public final void publishHostFlightDataUpdateEvent(String flightDataUpdateMessage)
  {
    publishEvent(msName, flightDataUpdateMessage, 0,
        MessageEventConsts.HOST_FLIGHT_DATA_UPDATE_EVENT_TYPE,
        MessageEventConsts.HOST_FLIGHT_DATA_UPDATE_EVENT_TEXT);
  }
  
  /**
   * Publish a host inventory update event
   * @param inventoryUpdateMessage csv formatted received message
   */
  public final void publishHostInventoryUpdateEvent(String inventoryUpdateMessage)
  {
    publishEvent(msName, inventoryUpdateMessage, 0,
        MessageEventConsts.HOST_INVENTORY_UPDATE_EVENT_TYPE,
        MessageEventConsts.HOST_INVENTORY_UPDATE_EVENT_TEXT);
  }
  
  /**
   * Subscribe to the host flight data update event
   */
  public final void subscribeHostFlightDataUpdateEvent(String sCKN)
  {
    if (sCKN != null)
    {
      startupTopicSubscriber(
          MessageEventConsts.HOST_FLIGHT_DATA_UPDATE_EVENT_TEXT + sCKN, true);
    }
    else
    {
      logger.logError("subscribeHostHostFlightDataUpdateEvent() -- name == null");
    }
  }
  
  /**
   * Subscribe to the host inventory update event
   */
  public final void subscribeHostInventoryUpdateEvent(String sCKN)
  {
    if (sCKN != null)
    {
      startupTopicSubscriber(
          MessageEventConsts.HOST_INVENTORY_UPDATE_EVENT_TEXT + sCKN, true);
    }
    else
    {
      logger.logError("subscribeHostInventoryUpdateEvent() -- name == null");
    }
  }
  
  /**
   * Publish a host retrieval order event
   * @param retrievalOrderMessage csv formatted received message
   */
  public final void publishHostRetrievalOrderEvent(String retrievalOrderMessage)
  {
    publishEvent(msName, retrievalOrderMessage, 0,
        MessageEventConsts.HOST_RETRIEVAL_ORDER_EVENT_TYPE,
        MessageEventConsts.HOST_RETRIEVAL_ORDER_EVENT_TEXT);
  }
  
  /**
   * Publish a host inventory request by warehouse event
   * @param inventoryReqByWarehouseMessage csv formatted received message
   */
  public final void publishHostInventoryReqByWarehouseEvent(String inventoryReqByWarehouseMessage)
  {
    publishEvent(msName, inventoryReqByWarehouseMessage, 0,
        MessageEventConsts.HOST_INVENTORY_REQUEST_BY_WAREHOUSE_EVENT_TYPE,
        MessageEventConsts.HOST_INVENTORY_REQUEST_BY_WAREHOUSE_EVENT_TEXT);
  }
  
  /**
   * Subscribe to the host inventory request by warehouse event
   */
  public final void subscribeHostInventoryReqByWarehouseEvent(String sCKN)
  {
    if (sCKN != null)
    {
      startupTopicSubscriber(
          MessageEventConsts.HOST_INVENTORY_REQUEST_BY_WAREHOUSE_EVENT_TEXT + sCKN, true);
    }
    else
    {
      logger.logError("subscribeHostInventoryReqByWarehouseEvent() -- name == null");
    }
  }
  
  public final void publishHostInventoryRequestEvent (String inventoryRequestrMessage)
	{ 
	  publishEvent(msName, inventoryRequestrMessage, 0,
	        MessageEventConsts.HOST_INVENTORY_REQUEST_BY_FLIGHT_EVENT_TYPE,
	        MessageEventConsts.HOST_INVENTORY_REQUEST_EVENT_TEXT);
	
	}
  public final void publishHostRetrievalItemEvent(String retrievalItemMessage)
  {
    publishEvent(msName, retrievalItemMessage, 0,
        MessageEventConsts.HOST_RETRIEVAL_ITEM_EVENT_TYPE,
        MessageEventConsts.HOST_RETRIEVAL_ITEM_EVENT_TEXT);
  }
  /**
   * Subscribe to the host flight data update event
   */
  public final void subscribeHostRetrievalOrderEvent(String sCKN)
  {
    if (sCKN != null)
    {
      startupTopicSubscriber(
          MessageEventConsts.HOST_RETRIEVAL_ORDER_EVENT_TEXT + sCKN, true);
    }
    else
    {
      logger.logError("subscribeHostRetrievalOrderEvent() -- name == null");
    }
  }
  
  /**
   * Subscribe to the Inventory Request By Flight data update event
   */
  public final void subscribeHostInventoryRequestByFlightEvent(String sCKN)
  {
    if (sCKN != null)
    {
      startupTopicSubscriber(
          MessageEventConsts.HOST_INVENTORY_REQUEST_EVENT_TEXT + sCKN, true);
    }
    else
    {
      logger.logError("subscribeHostInventoryRequestEvent() -- name == null");
    }
  }
  
  /**
   * Subscribe to the host Item event
   */
  public final void subscribeHostRetrievalItemEvent(String sCKN)
  {
    if (sCKN != null)
    {
      startupTopicSubscriber(
          MessageEventConsts.HOST_RETRIEVAL_ITEM_EVENT_TEXT + sCKN, true);
    }
    else
    {
      logger.logError("subscribeHostRetrievalOrderEvent() -- name == null");
    }
  }
  
  /**
   * Subscribe for host to plc event
   * 
   * @param sCKN the subscribing Controller's key name
   */
  public final void subscribeHostToPlcEvent(String sCKN)
  {
    if (sCKN != null) {
      startupTopicSubscriber(MessageEventConsts.HOST_TO_PLC_EVENT_TEXT + sCKN, true);
    }
    else
    {
      logger.logError("subscribeHostToPlcEvent() -- name == null");
    }
  }
  
  /**
   * Publish a host to plc event
   * @param retrievalOrderMessage csv formatted received message
   */
  public final void publishHostToPlcEvent(String message)
  {
    publishEvent(msName, message, 0,
        MessageEventConsts.HOST_TO_PLC_EVENT_TYPE,
        MessageEventConsts.HOST_TO_PLC_EVENT_TEXT);
  }
  
  /**
   * Subscribe for plc to host event
   * 
   * @param sCKN the subscribing Controller's key name
   */
  public final void subscribePlcToHostEvent(String sCKN)
  {
    if (sCKN != null) {
       startupTopicSubscriber(MessageEventConsts.PLC_TO_HOST_EVENT_TEXT + sCKN, true);
    }
    else
    {
      logger.logError("subscribePlcToHostEvent() -- name == null");
    }
  }
  
  /**
   * Publish a plc to host event
   * @param message csv formatted received message
   */
  public final void publishPlcToHostEvent(String message)
  {
    publishEvent(msName, message, 0,
        MessageEventConsts.PLC_TO_HOST_EVENT_TYPE,
        MessageEventConsts.PLC_TO_HOST_EVENT_TEXT);
  }

  /**
   * Publishes load category message.
   * 
   * <p><b>Details:</b> <code>publishLoadEvent</code> publishes a Load category
   * message to the specified Controller using the inter-process-communication
   * message service.</p>
   * 
   * @param isText the message text string to be sent as the message body
   * @param inData an int with data defined by the message type
   * @param isCkn no information available
   */
  public final synchronized void publishLoadEvent(String isText, int inData, String isCkn)
  {
    if (isCkn == null)
    {
      logger.logError("Transporter Key-Name is NULL - publishLoadEvent()");
      isCkn = "";
    }
    publishEvent(msName, isText, inData, 
        MessageEventConsts.LOAD_EVENT_TYPE,
        MessageEventConsts.LOAD_EVENT_TYPE_TEXT + isCkn);
  }

  /**
   * Publish a "Load" Message to the Inter-Process-Communication message service
   * <i>Server</i>.
   * 
   * @param messageText the String data content to be sent
   * @param messageData the int data content to be sent
   */
  protected final void publishLoadEvent(String messageText, int messageData)
  {
    publishLoadEvent(messageText, messageData, msName);
  }
  
  /**
   * Publish a "Log" Message to the Inter-Process-Communication message service
   * <i>Server</i>.
   * 
   * @param isText the String data content to be sent
   * @param inData the int data content to be sent
   * @param isCkn the message destination
   */
  public final synchronized void publishLogEvent(String isText, int inData,
      String isCkn)
  {
    if (isCkn == null)
    {
      logger.logDebug("Destination Key-Name is NULL - publishLogEvent()");
      isCkn = "";
    }
    publishEvent(msName, isText, inData, MessageEventConsts.LOG_EVENT_TYPE,
        MessageEventConsts.LOG_EVENT_TYPE_TEXT + isCkn);
  }

  /**
   * Publish a "Order" Message to the Inter-Process-Communication message
   * service <i>Server</i>.
   *
   * @param messageText the String data content to be sent
   * @param messageData the int data content to be sent
   * @param sCKN the message destination
   */
  public final synchronized void publishOrderEvent(String messageText, int messageData, String sCKN)
  {
    if (sCKN == null)
    {
      logger.logDebug("Destination Key-Name is NULL - publishOrderEvent()");
      sCKN = "";
    }
    publishEvent(msName, messageText, messageData,
        MessageEventConsts.ORDER_EVENT_TYPE,
        MessageEventConsts.ORDER_EVENT_TYPE_TEXT + sCKN);
  }

  /**
   * Publish a "Scheduler" Message to the Inter-Process-Communication message
   * service <i>Server</i>.
   * 
   * @param messageText the String data content to be sent
   * @param messageData the int data content to be sent
   */
  protected final void publishSchedulerEvent(String messageText, int messageData)
  {
    publishSchedulerEvent(messageText, messageData, msName);
  }

  /**
   * Publish a "Scheduler" Message to the Inter-Process-Communication message
   * service <i>Server</i>.
   * 
   * @param messageText the String data content to be sent
   * @param messageData the int data content to be sent
   * @param sCKN the message destination
   */
  public final synchronized void publishSchedulerEvent(String messageText,
      int messageData, String sCKN)
  {
    if (sCKN == null)
    {
      logger.logDebug("Destination Key-Name is NULL - publishSchedulerEvent()");
      sCKN = "";
    }
    publishEvent(msName, messageText, messageData,
        MessageEventConsts.SCHEDULER_EVENT_TYPE,
        MessageEventConsts.SCHEDULER_EVENT_TYPE_TEXT + sCKN);
  }

  /**
   * Publishes station category message.
   * 
   * <p><b>Details:</b> <code>publishStationEvent</code> publishes a Station
   * category message to the specified Controller using the
   * inter-process-communication message service.</p>
   * 
   * @param isText the message text string to be sent as the message body
   * @param inData an int with data defined by the message type
   */
  public final synchronized void publishStationEvent(String isText, int inData)
  {
    publishEvent(msName, isText, inData,
        MessageEventConsts.STATION_EVENT_TYPE,
        MessageEventConsts.STATION_EVENT_TYPE_TEXT + msName);
  }

  /**
   * Publishes station category message.
   * 
   * <p><b>Details:</b> <code>publishStationEvent</code> publishes a Station
   * category message to the specified Controller using the
   * inter-process-communication message service.</p>
   * 
   * @param isText the message text string to be sent as the message body
   * @param inData an int with data defined by the message type
   * @param isCkn the destination Controller's Key Name
   */
  public final synchronized void publishStationEvent(String isText, int inData, String isCkn)
  {
    if (isCkn == null)
    {
      logger.logError("Transporter Key-Name is NULL - publishStationEvent()");
      isCkn = "";
    }
    publishEvent(msName, isText, inData,
        MessageEventConsts.STATION_EVENT_TYPE,
        MessageEventConsts.STATION_EVENT_TYPE_TEXT + isCkn);
  }

  /**
   * Publish a "Status" Message to the Inter-Process-Communication message
   * service <i>Server</i>.
   * 
   * @param messageText the String data content to be sent
   * @param messageData the int data content to be sent
   */
  protected final void publishStatusEvent(String messageText, int messageData)
  {
    publishEvent(msName, messageText, messageData,
        MessageEventConsts.STATUS_EVENT_TYPE,
        MessageEventConsts.CONTROLLER_STATUS_EVENT_TYPE_TEXT + msName);
  }

  /**
   * Publish a "Machine Status" Message to the Inter-Process-Communication
   * message service <i>Server</i>.
   * 
   * @param messageText the String data content to be sent
   */
  public final void publishStatusEvent(String messageText)
  {
    publishStatusEvent(messageText, 0);
  }

  /**
   * Publish a status update event
   * 
   * @param messageText
   */
  public final synchronized void publishUpdateEvent(String messageText)
  {
    publishEvent(msName, messageText, ControllerConsts.EQUIPMENT_STATUS,
        MessageEventConsts.UPDATE_EVENT_TYPE,
        MessageEventConsts.UPDATE_EVENT_TYPE_TEXT
            + MessageEventConsts.STATUS_EVENT_TEXT
            + MessageEventConsts.SUB_EVENT_TEXT
            + ControllerConsts.EQUIPMENT_STATUS
            + MessageEventConsts.UPDATE_EVENT_TYPE_TEXT2 + ":" + WRX_RUN_MODE);
  }

  
  /*========================================================================*/
  /*  Thread Management Methods                                             */
  /*========================================================================*/
  
  /**
   * Notify the Messenger's Thread to execute its {@link #startup() startup()}
   * method. This is done indirectly so that any object creation occurring in
   * startup() will be done in (and by) the Messenger's Thread.
   * 
   * <p>Inter-Process-Communication event processing is <i>NOT</i> yet enabled.
   */
  public final void initiateStartup()
  {
    logger.logDebug("Messenger.initiateStartup()");
    mzNeedToStartup = true;
    synchronized (this)
    {
      notify();
      while (mzNeedToStartup) // wait until done;
      {
        try
        {
          wait(100);
        }
        catch (InterruptedException e)
        {
          break;
        }
      }
    }
  }
  
  /**
   * Notify the Messenger's Thread to execute its
   * {@link #shutdown() shutdown()} method. This is done indirectly so that any
   * object freeing occurring in shutdown() will be done in (and by) the
   * Messenger's Thread.
   * 
   * <p>Inter-Process-Communication event processing <i>IS</i> still enabled.
   */
  public final synchronized void initiateShutdown()
  {
    mzNeedToShutdown = true;
    notify();
    while (mzNeedToShutdown) // wait until done;
    {
      try
      {
        wait(100);
      }
      catch (InterruptedException e)
      {
        break;
      }
    }
    msName = null;
  }

  /**
   * Handles asynchronous message events from the Inter-Process-Communication
   * service <i>Server</i>. ALL Subscribed Topics and their EventTypes, and ALL
   * Received Queue messages (if message queues are used) for the messenger
   * come to this one event handler. The incoming message is put in a message
   * queue and the thread's main run() method is awakened.
   * 
   * @param msg the incoming message to be processed by this messenger
   */
  @Override
  public final void onTextMessage(IpcMessage msg)
  {
    // If this messenger is shutting down don't get hung up here waiting
    // to process a message.
    if (!mzNeedToShutdown)
    {
      synchronized (this)
      {
        mpInMessageQueue.add(msg);
        notify();
      }
    }
  }

  /**
   * Notify everyone that we're shutting down
   */
  protected final void notifyQuit()
  {
    synchronized(this)
    {
      mzQuit = true;
      notifyAll();
    }
  }

  /**
   * Bring the messenger implementation to a minimal working state. The
   * base-class implementation initializes the instantiated messenger by setting
   * the Controller's KeyName, <i>controllersKeyName</i>, and creating event
   * observers.
   * 
   * <p>A child Controller should find its collaborators (but <b>NOT</b> publish
   * to them), subscribe to any events it needs from its collaborators, and
   * perform any other simple tasks which do NOT require logging. Any object
   * creation beyond basic java classes should be done in the
   * {@link #startup() startup()} method where logging and message services are
   * available.</p>
   * 
   * <p>Inter-Process-Communication event processing is <b>NOT</b> yet enabled.</p>
   * 
   * @param isControllerKeyName the unique name that identifies this instance of
   *            messenger
   */
  protected void initialize(String isControllerKeyName)
  {
    setName(isControllerKeyName);
    for (int i = 0; i < MessageEventConsts.EVENT_TEXT.length; i++)
    {
      ObservableControllerImpl eventObserver = new ObservableControllerImpl();
      mapEventObservers[i] = eventObserver;
    }
  }
  
  /**
   * Create any application objects the messenger implementation needs.
   *
   * <p>Inter-Process-Communication event processing is <i>NOT</i> yet enabled.
   * Inter-Process-Communication event processing will be enabled after all
   * messengers have completed startup.
   */
  protected void startup()
  {
    logger.logDebug("Messenger.startup() - Start");
    timers = new RestartableTimer("timers-" + msName);
  }

  /**
   * Terminate the messenger implementation's ability to do work. Terminate the
   * messenger implementation by canceling any timers, setting any created
   * objects to <i>null</i>, and notifying the inter-process-communication
   * event processing thread to quit.
   * 
   * <p>After shutdown the
   * {@link com.daifukuamerica.wrxj.common.controller.ControllerImplFactory ControllerImplFactory}
   * will shutdown the messenger's inter-process-communication message service
   * and set the messenger's logger to <i>null</i>.</p>
   */
  protected void shutdown()
  {
    mapEventObservers = null;
    if (timers != null)
    {
      timers.cancel();
      timers = null;
    }
    mzOkToProcessMessages = false;
    selfThread = null;
    mpInMessageQueue = null;
    logger.logDebug("Messenger \"" + msName + "\" is now SHUT-DOWN");
    notifyQuit();
  }
  
  /**
   * Enable Inter-Process-Communication event processing.
   */
  public final void startProcessingMessages()
  {
    logger.logDebug(msName + ".startProcessingMessages()");
    mzOkToProcessMessages = true;
    synchronized (this)
    {
      notify();
      logger.logDebug(msName + ".startProcessingMessages() - notify() - Done");
    }
  }
  
  /**
   * The main execution method of the messenger Thread.
   * Inter-Process-Communication message events are de-queued and process in
   * this method. Messages will be processed in the order received until the
   * message queue is empty. The method will then wait().
   */
  @Override
  public final synchronized void run()
  {
    while (!mzQuit)
    {
      try
      {
        if (mzNeedToStartup)
        {
          startup();
          mzNeedToStartup = false;
        }
        while (mzNeedToShutdown)
        {
          shutdown();
          mzNeedToShutdown = false;
          return;
        }
        while (mzOkToProcessMessages && !mpInMessageQueue.isEmpty())
        {
          IpcMessage receivedMessage = mpInMessageQueue.remove(0);
          decodeIpcMessage(receivedMessage);
          try
          {
            processIPCReceivedMessage();
          }
          catch (Exception e)
          {
            logger.logException(e, "processIPCReceivedMessage()");
            receivedMessageProcessed = true;
          }
          if (!receivedMessageProcessed && !observedMessageProcessed)
          {
            if (receivedEventType < MessageEventConsts.EVENT_TEXT.length)
            {
              logger.logError("UNPROCESSED MessageType: "
                  + MessageEventConsts.EVENT_TEXT[receivedEventType]);
            }
            else
            {
              logger.logError("UNKNOWN MessageType: " + receivedEventType);
            }
          }
          receivedMessage = null;
        }
        processLocalEvent();
        try
        {
          // This shouldn't be necessary, but sometimes the SystemHealthMonitor
          // locks up, and I'm hoping this will fix it.  - Mike
          wait(60000);
        }
        catch (InterruptedException e)
        {
        }
      }
      catch (Throwable ex)
      {
        mnCurrentCancer++;
        if (mnCurrentCancer > MAX_CANCER)
        {
          logger.logException("Shutting down due to errors.", ex);
          System.err.println(getClass().getSimpleName()
              + ": Shutting down due to thread cancer.");
          ex.printStackTrace();
          notifyQuit();
        }
        else
        {
          logger.logException("Abnormal messenger death prevented.", ex);
          System.err.println(getClass().getSimpleName()
              + ": Warning! Abnormal messenger death prevented - thread cancer may occur!");
        }
      }
    }
  }
}
