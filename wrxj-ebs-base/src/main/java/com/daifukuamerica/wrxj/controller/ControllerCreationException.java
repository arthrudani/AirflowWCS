package com.daifukuamerica.wrxj.controller;

/**
 * Signals and explains controller creation failure.
 *
 * <p><b>Details:</b> A <code>ControllerCreationException</code> is thrown when
 * an error occurs during the attempted creation of a <code>Controller</code>.
 * The detail message reveals the cause of the cause of the error.</p>
 *
 * <p>To obtain the detail message, call <code>getMessage</code>.</p>
 *
 * @author Sharky
 */
public class ControllerCreationException extends Exception
{
  private static final long serialVersionUID = 0L;

  /**
   * <p><b>Details:</b> This constructor defines the detail message for this
   * exception, which reveals the reason why the requested controller could not
   * be instantiated.</p>
   *
   * @param isDetail
   */
  public ControllerCreationException(String isDetail)
  {
    super(isDetail);
  }

}

