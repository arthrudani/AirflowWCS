package com.daifukuamerica.wrxj.comport;

/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

/**
 * Handles asynchronous received data available and status change events from the ComPort implementation.
 *
 * @author Stephen Kendorski
 * @version 1.0
 */

public interface ComPortEventHandling
{
  /**
   * Handles received data available events.
   *
   * <p>This method is executing in the {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort}'s Thread! So synchronize.

   * @param byteArray a buffer holding the received data
   * @param iOffset   the offset to the start of the received data in the buffer
   * @param iCount    the number of bytes of received data in the buffer
   */
  void comPortDataAvailableHandler(byte[] byteArray, int iOffset, int iCount);
  /**
   * The Port's {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort} uses this method to notify this Port Controller
   * that the {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort} has changed status/state.  This method also changes the
   * {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort}'s status/state into a GENERIC CONTROLLER STATUS.
   *
   * <p>This is executing in the {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort}'s Thread! So synchronize.
   *
   * @param iState         the new state of the {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort}
   * @param iPrevState     the previous state of the {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort}
   * @param iComPortNumber the number of the {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort} reporting a state change
   */
  void comPortStateChangeHandler(int iState, int iPrevState, int iComPortNumber);
}