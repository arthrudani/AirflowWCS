package com.daifukuamerica.wrxj.factory;

/**
 * Indicates factory failure.
 *  
 * <p><b>Details:</b> This exception indicates that a reflection-<wbr>related
 * problem occured in the {@link Factory}.  To discover the cause of this 
 * exception, refer to the wrapped exception.</p>
 * 
 * @author Sharky
 */
public final class FactoryException extends RuntimeException
{

  private static final long serialVersionUID = 0;

  /**
   * Sets message and cause.
   * 
   * <p><b>Details:</b> This constructor sets the exception message and the 
   * cause to the given values.</p>
   * 
   * @param isMessage the message
   * @param ieCause the cause
   */
  public FactoryException(String isMessage, Throwable ieCause)
  {
    super(isMessage, ieCause);
  }

}

