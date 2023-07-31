package com.daifukuamerica.wrxj.device.agv;

/**
 * Generic Database exception to isolate database interface errors.
 * @author A.D.
 * @since  01-Jun-2009
 */
@SuppressWarnings("serial")
public class AGVException extends Exception
{
  private int mnErrorCode = 0;
 /**
  *  This constructor sets the exception message and the cause to the given
  *  values.
  *
  * @param isMessage the message
  */
  public AGVException(String isMessage)
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
  public AGVException(String isMessage, Throwable ipCause)
  {
    super(isMessage, ipCause);
  }

 /**
  *  This constructor sets the exception message and the associated error code.
  *
  * @param isMessage the message
  * @param inErrorCode AGVDBInterface error code.
  */
  public AGVException(String isMessage, int inErrorCode)
  {
    super(isMessage);
    mnErrorCode = inErrorCode;
  }

 /**
  *  This constructor sets the exception message and the cause to the given
  *  values.
  *
  * @param isMessage the message
  * @param inErrorCode AGVDBInterface error code.
  * @param ipCause the root cause.
  * @see AGVDBInterface
  */
  public AGVException(String isMessage, int inErrorCode, Throwable ipCause)
  {
    super(isMessage, ipCause);
    mnErrorCode = inErrorCode;
  }

 /**
  * Gets generic error code
  * @return error code as specified in {@link AGVDBInterface AGVDBInterface}
  */
  public int getErrorCode()
  {
    return(mnErrorCode);
  }
}
