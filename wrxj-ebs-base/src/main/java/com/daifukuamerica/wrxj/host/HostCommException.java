package com.daifukuamerica.wrxj.host;

public class HostCommException extends Exception
{
  private static final long serialVersionUID = 0L;

 /**
  * Constructs an <code>HostCommException</code> with <code>null</code>
  * as its error detail message.
  */
  public HostCommException()
  {
    super();
  }

 /**
  * Constructs an <code>HostCommException</code> with the specified detail
  * message. The error message string <code>message</code> can later be
  * retrieved by the <code>{@link java.lang.Throwable#getMessage}</code>
  * method of class <code>java.lang.Throwable</code>.
  *
  * @param  message <coe>String</code> containing the detail message.
  */
  public HostCommException(String message)
  {
	  super(message);
  }

 /**
  * Constructs an <code>HostCommException</code> wrapping
  * <code>rootExcep</code>.
  */
  public HostCommException(Throwable rootExcep)
  {
    super(rootExcep);
  }

  public HostCommException(String message, Throwable rootExcep)
  {
    super(message);
    initCause(rootExcep);
  }
}
