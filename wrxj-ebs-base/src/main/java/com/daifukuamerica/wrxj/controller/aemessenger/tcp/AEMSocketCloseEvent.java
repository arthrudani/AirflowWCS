package com.daifukuamerica.wrxj.controller.aemessenger.tcp;

/**
 * Event handler for Client Socket Close notifications.  This is interface is
 * provided in case the caller wants to do any additional resource clean-up when
 * the client socket closes.  This event will be generated when a graceful
 * connection close occurs from the client.  If there is an abrupt network drop
 * this interface will be likely ineffective.
 *
 * @author A.D.
 * @since  12-Feb-2012
 */
public interface AEMSocketCloseEvent
{
  /**
   * Method to handle client socket close operations
   *
   * @param ipThread the client thread that is closing.
   */
  public void onSocketClose(AEMTcpipReaderWriter ipThread);
}
