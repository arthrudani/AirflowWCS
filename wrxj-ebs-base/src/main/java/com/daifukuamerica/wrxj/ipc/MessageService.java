package com.daifukuamerica.wrxj.ipc;

import javax.jms.Message;

/**
 * Application Inter-Process-Communication client.
 *
 * @author Stephen Kendorski
 * @version 1.0
 */
public interface MessageService
{
  // String constants, because magic strings suck
  static final String MESSAGE_SENDER = "MsgSndr";
  static final String MESSAGE_SENDER_GROUP = "MsgSndrCG";
  static final String MESSAGE_DATA = "MsgData";
  static final String MESSAGE_EVENT_TYPE = "EvtType";
  static final String MESSAGE_EVENT = "Event";
  static final String MESSAGE_TIME = "MsgTxTime";

  // JMS publish defaults
  static final int DEFAULT_JMS_PRIORITY = 4;
  static final int DEFAULT_STATUS_TIME_TO_LIVE = 60000;
  static final int DEFAULT_TIME_TO_LIVE = 900000;//15 minutes

  /**
   * Get the start-up failure reason, or null if one does not exist
   * @return
   */
  String getStartupFailReason();
  
  /**
   * Specify the received data processor.
   *
   * @param o a message processor
   */
  void setMessageEventHandler(MessageEventHandling o);

  /**
   * Specify the Inter-Process-Communication message service client properties
   *
   * @param keyName unique key name for the instantiated message service client
   */
  void initialize(String keyName);

  /**
   * Connect to the Inter-Process-Communication service <i>Server</i>.
   */
  void startup();

  /**
   * Disconnect from the Inter-Process-Communication service <i>Server</i>.
   */
  void shutdown();

  /**
   * Register to receive specific messages.
   *
   * @param selector received message filter criteria
   * @param noLocal if <i>non-null</i>, inhibits the delivery of messages from self
   */
  void addSubscriber(String selector, String noLocal);

  /**
   * Give a message to the Inter-Process-Communication service <i>Server</i>.
   *
   * @param messageSender unique key name identifying the message's sender
   * @param controllerGroup the message sender's controller group
   * @param messageText the String data content
   * @param messageData the long data data content
   * @param eventType the message classification
   * @param event the message detailed classification
   */
  void publishEvent(String messageSender, String controllerGroup,
                    String messageText, long messageData,
                    int eventType, String event);

  /**
   * Un-Register to receive specific messages.
   *
   * @param selectorlistener message filter criteria
   */
  void shutdownSubscriber(String selectorlistener);
  
  /**
   * Process a message
   * 
   * @param ipMessage
   * @param isSelector
   */
  void onMessage(Message ipMessage, String isSelector);
  
  /**
   * Get the provider name for this message service
   * @return
   */
  String getProviderName();
}

