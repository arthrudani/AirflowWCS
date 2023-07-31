package com.daifukuamerica.wrxj.controller.aemessenger.tcp;

import com.daifukuamerica.wrxj.controller.aemessenger.AEMessage;

/**
 * Interface that handles the data after it comes off the communication interface.
 * @author A.D.
 * @since  06-Feb-2013
 */
public interface AEMReadEvent
{
  /**
   * Method to process the data after it is received off the socket.
   *
   * @param ipChannel the socket channel.
   * @param ipMessage the received data.
   * 
   */
  public void receivedData(AEMTcpipReaderWriter ipChannel, AEMessage ipMessage);
}
