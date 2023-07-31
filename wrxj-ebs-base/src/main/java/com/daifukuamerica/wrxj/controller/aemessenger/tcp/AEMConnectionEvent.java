package com.daifukuamerica.wrxj.controller.aemessenger.tcp;

import java.nio.channels.SocketChannel;
import java.util.Properties;

/**
 * Interface to inform of new connections.
 * @param ipClientChannel the channel to which a client is connected.
 *
 * @author A.D.
 * @since 07-Feb-2013
 */
public interface AEMConnectionEvent
{
  /**
   * Method to notify caller that a client has connected to this server.
   */
  public void connectionHandler(SocketChannel ipClientChannel, Properties ipProps);
}
