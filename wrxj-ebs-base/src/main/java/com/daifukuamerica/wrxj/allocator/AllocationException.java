package com.daifukuamerica.wrxj.allocator;

/**
 * Class to create an Allocation Exception.
 * @author       A.D.
 * @version      1.0
 */
@SuppressWarnings("serial")
public class AllocationException extends Exception
{
  /** Error code -1 means output station's Device is Inoperable */
  public static final int DEVICE_INOP = -1;
  private int mnExceptionCode = 0;
  
  public AllocationException()
  {
    super();
  }

  public AllocationException(String mesg)
  {
    super(mesg);
  }

 /**
  * Constructor to specify an optional exception code to direct additional
  * handling of this exception.
  * @param isMesg the error message.
  * @param inExceptionCode the exception code.  Currently the following codes
  *        are defined:
  *    <ul>
  *      <li>{@link #DEVICE_INOP DEVICE_INOP} if the station's device is inoperable.</li>
  *    </ul>
  *        
  */
  public AllocationException(String isMesg, final int inExceptionCode)
  {
    super(isMesg);
    mnExceptionCode = inExceptionCode;
  }

  public AllocationException(Throwable exc)
  {
    super(exc);
  }
  
  public AllocationException(String mesg, Throwable exc)
  {
    super(mesg);
    initCause(exc);
  }
  
  public int getExceptionCode()
  {
    return(mnExceptionCode);
  }
  
  public void setExceptionCode(int inExceptionCode)
  {
    mnExceptionCode = inExceptionCode;
  }
}
