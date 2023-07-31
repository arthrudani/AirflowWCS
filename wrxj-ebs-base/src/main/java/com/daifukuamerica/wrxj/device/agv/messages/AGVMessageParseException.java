package com.daifukuamerica.wrxj.device.agv.messages;

/**
 * Exception class for parsing errors.
 *
 * @author A.D.
 * @since  15-May-2009
 */
@SuppressWarnings("serial")
public class AGVMessageParseException extends Exception
{
 /**
  *  This constructor sets the exception message and the cause to the given
  *  values.
  *
  * @param isMessage the message
  */
  public AGVMessageParseException(String isMessage)
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
  public AGVMessageParseException(String isMessage, Throwable ipCause)
  {
    super(isMessage, ipCause);
  }
}
