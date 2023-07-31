package com.daifukuamerica.wrxj.device.agv.messages;

/**
 * Exception signaling problem with an AGV message factory.
 * 
 * @author A.D.
 * @since  12-May-2009
 */
@SuppressWarnings("serial")
public class AGVMessageFactoryException extends Exception
{
 /**
  *  This constructor sets the exception message and the cause to the given
  *  values.
  *
  * @param isMessage the message
  */
  public AGVMessageFactoryException(String isMessage)
  {
    super(isMessage);
  }

 /**
  *  This constructor sets the exception message and the cause to the given
  *  values.
  *
  * @param isMessage the message
  * @param ipCause the cause
  */
  public AGVMessageFactoryException(String isMessage, Throwable ipCause)
  {
    super(isMessage, ipCause);
  }
}
