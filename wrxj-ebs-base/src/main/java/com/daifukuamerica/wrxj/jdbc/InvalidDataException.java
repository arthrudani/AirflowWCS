package com.daifukuamerica.wrxj.jdbc;

public class InvalidDataException extends Exception
{
  private static final long serialVersionUID = 0L;

 /**
  * Constructs an <code>InvalidDataException</code> with <code>null</code>
  * as its error detail message.
  */
  public InvalidDataException()
  {
    super();
  }

 /**
  * Constructs an <code>InvalidDataException</code> with the specified detail
  * message. The error message string <code>message</code> can later be
  * retrieved by the <code>{@link java.lang.Throwable#getMessage}</code>
  * method of class <code>java.lang.Throwable</code>.
  *
  * @param  message <code>String</code> containing the detail message.
  */
  public InvalidDataException(String message)
  {
	  super(message);
  }

 /**
  * Constructs an <code>InvalidDataException</code> wrapping
  * <code>rootExcep</code>.
  */
  public InvalidDataException(Throwable rootExcep)
  {
    super(rootExcep);
  }

  public InvalidDataException(String message, Throwable rootExcep)
  {
    super(message);
    initCause(rootExcep);
  }
}
