
package com.daifukuamerica.wrxj.device.agv;

/**
 * Interface to bridge multiple types of message loggers across differing
 * products.
 * @author A.D.
 * @since  10-Jun-2009
 */
public interface AGVLogger
{
  public final int INBOUND_MESG = 1;
  public final int OUTBOUND_MESG = 2;
  
 /**
  * Method to log serious errors.
  * @param isErrorMessage the error message.
  */
  public void logErrorMessage(String isErrorMessage);
 /**
  * Method to log Debug info.
  * @param isDebugMessage the Debug message.
  */
  public void logDebugMessage(String isDebugMessage);
 /**
  * Method to log communication messages.
  * @param inDirection message direction.  Value of {@link AGVLogger#INBOUND_MESG INBOUND_MESG}
  *        or {@link AGVLogger#OUTBOUND_MESG OUTBOUND_MESG}
  * @param isCommMessage the message.
  */
  public void logCommMessage(int inDirection, String isCommMessage);
}
