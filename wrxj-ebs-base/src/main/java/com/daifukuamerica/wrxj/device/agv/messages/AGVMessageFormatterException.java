package com.daifukuamerica.wrxj.device.agv.messages;

/**
 * Exception indicating HostFormatter retrieval error.
 *
 * @author A.D.
 * @since 12-May-2009
 */
@SuppressWarnings("serial")
public class AGVMessageFormatterException extends Exception
{
 /**
  *  This constructor sets the exception message and the cause to the given
  *  values.
  *
  * @param isMessage the message
  */
  public AGVMessageFormatterException(String isMessage)
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
  public AGVMessageFormatterException(String isMessage, Throwable ipCause)
  {
    super(isMessage, ipCause);
  }
}
