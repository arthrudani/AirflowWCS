package com.daifukuamerica.wrxj.ipc;

/**
 * @author Stephen Kendorski
 *
 */
public interface MessageEventHandling
{
  
  /**
   * Handles asynchronous message events from the Inter-Process-Communication service <i>Server</i>.
   * ALL Subscribed Topics and their EventTypes, and ALL Received Queue messages
   * (if message queues are used) for the Controller come to this one
   * event handler.
   *
   * @param msg the incoming message
   */
  void onTextMessage(IpcMessage msg);

}

