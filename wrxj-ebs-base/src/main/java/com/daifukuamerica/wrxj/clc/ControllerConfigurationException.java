package com.daifukuamerica.wrxj.clc;

/**
 * Indicates failure while processing controller configuration.
 *
 * <p><b>Details:</b> A {@link ControllerConfigurationException} is thrown when
 * an error occurs while answering controller configuration queries.  Such an error
 * may be due to an invalid configuration or it may be due to I/O trouble.</p>
 *
 * @author Sharky
 */
public class ControllerConfigurationException extends Exception
{
  private static final long serialVersionUID = 0L;

  /**
   * Initializes with detail message.
   *
   * <p><b>Details:</b> This constructor initialize the new exception with the
   * given detail message.</p>
   *
   * @param isMessage the detail message
   */
  public ControllerConfigurationException(String isMessage)
  {
    super(isMessage);
  }

  /**
   * Initializes with original exception.
   *
   * <p><b>Details:</b> This constructor initializes the new exception with a
   * detail message derived from the supplied exception.  Presumably, the
   * supplied exception was also the cause of the failure that resulted in the
   * generation of this exception.</p>
   *
   * @param ieCause
   */
  public ControllerConfigurationException(Throwable ieCause)
  {
    super(ieCause);
  }

}

