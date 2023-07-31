package com.daifukuamerica.wrxj.ipc;


/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

/**
 * @author Stephen Kendorski
 * @version 1.0
 *
 * The data packet used for inter-process-communication between Controllers.
 */
public class IpcMessage
{
  private String messageSender = null;
  private String messageSenderControllerGroup = null;
  private String messageText = null;
  private long   messageData = 0;
  private int    eventType = 0;
  private String event = null;
  private long   messageTxTime;
  private long   messageRxTime;
  private String messageSelector = null;

  /**
   * Construct a data packet and fill its data fields.
   *
   * @param msgSender the unique key identifying the message's originator
   * @param msgSenderControllerGroup the message sender's controller group
   * @param msgText the text data content
   * @param msgData the long data content
   * @param evtType the event category
   * @param evt the event selector
   * @param msgTxTime the message transmit/send time
   * @param msgRxTime the message receive time
   * @param msgSelector the message subscriber's selector
   */
  public IpcMessage(String msgSender, String msgSenderControllerGroup,
                    String msgText, long msgData, int evtType,
                    String evt, long msgTxTime, long msgRxTime, String msgSelector)
  {
    messageSender = msgSender;
    messageSenderControllerGroup = msgSenderControllerGroup;
    messageText = msgText;
    messageData = msgData;
    eventType = evtType;
    event = evt;
    messageTxTime = msgTxTime;
    messageRxTime = msgRxTime;
    messageSelector = msgSelector;
  }

  @Override
  public String toString()
  {
    String vsStr = "Sender=" + messageSender;
    vsStr += "|SCGroup=" + messageSenderControllerGroup;
    vsStr += "|Text=" + messageText;
    vsStr += "|Data=" + messageData;
    vsStr += "|Type=" + eventType + " (" + MessageEventConsts.EVENT_TEXT[eventType] + ")";
    vsStr += "|Event=" + event;
    vsStr += "|Selector=" + messageSelector;

    return vsStr;
  }
  
  /**
   * Fetch the the message sender's controller group.
   *
   * @return the key
   */
  public String getMessageSenderControllerGroup()
  {
    return messageSenderControllerGroup;
  }

  /**
   * Fetch the message's originating Controller's unique identifying key name.
   *
   * @return the key
   */
  public String getMessageSender()
  {
    return messageSender;
  }

  /**
   * Fetch the message's text data content.
   *
   * @return the text
   */
  public String getMessageText()
  {
    return messageText;
  }

  /**
   * Fetch the message's long data content.
   *
   * @return the long
   */
  public long getMessageData()
  {
    return messageData;
  }

  /**
   * Fetch the int EVENT TYPE of the Message.
   *
   * @return the category
   */
  public int getEventType()
  {
    return eventType;
  }

  /**
   * Fetch the event that Subscribers can apply selectors to for getting messages.
   *
   * @return the event
   */
  public String getEvent()
  {
    return event;
  }

  /**
   * Fetch the message's transmit/send date/time.
   *
   * @return the date/time (as a long)
   */
  public long getMessageTxTime()
  {
    return messageTxTime;
  }

  /**
   * Fetch the message's receive date/time.
   *
   * @return the date/time (as a long)
   */
  public long getMessageRxTime()
  {
    return messageRxTime;
  }

  /**
   * Fetch the message subcribers's selection content.
   *
   * @return the text
   */
  public String getMessageSelector()
  {
    return messageSelector;
  }
}