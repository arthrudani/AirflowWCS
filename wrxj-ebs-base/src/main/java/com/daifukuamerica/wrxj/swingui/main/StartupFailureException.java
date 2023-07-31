package com.daifukuamerica.wrxj.swingui.main;

/**
 * Indicates initialization failure.
 *
 * <p><b>Details:</b> A <code>StartupFailureException</code> indicates that a
 * startup procedure in <code>MainStartup</code> failed.  Usually, this
 * indicates a fatal error that must be fixed before the application can run.
 * To learn about the nature of the failure, use <code>getMessage</code> and
 * <code>getCause</code>.</p>
 *
 * @author Sharky
 */
public class StartupFailureException extends Exception
{
  private static final long serialVersionUID = 0L;
  
  /**
   * Sets exception message.
   *
   * <p><b>Details:</b> This constructor initializes this exception with the
   * given message.  No cause exception is registered.</p>
   *
   * @param isMessage exception message
   */
  public StartupFailureException(final String isMessage)
  {
    super(isMessage);
  }

  /**
   * Sets exception message and cause.
   *
   * <p><b>Details:</b> This constructor initializes this exception with the
   * given message and cause exception.</p>
   *
   * @param isMessage exception message
   * @param ieCause exception cause
   */
  public StartupFailureException(final String isMessage, final Throwable ieCause)
  {
    super(isMessage, ieCause);
  }

}

