package com.daifukuamerica.wrxj.util;

import java.io.PrintStream;
import java.util.Date;

/**
 * Base class of all Daifuku America Corporation Runtime Exceptions
 * 
 * <p>
 * The class level javadoc for java.lang.Exception states "The class Exception
 * and its subclasses are a form of Throwable that indicates conditions that a
 * reasonable application might want to catch."
 * <p>
 * The intention of this class is to pick up exceptions that a "reasonable"
 * application would not know what to do with. In these cases, it is very
 * code/maintenance intensive to propogate throws and try/catch blocks to code
 * that will not be able to make any corrections to the exception.
 * 
 * <p>
 * It is expected that this class might be extended and used at "impl"
 * implementation layers of interfaces where the specialization of that impl is
 * unknown to the consumers of the generic interfaces of the implementation. If
 * the exception can not be handled by the implemenation, the generic layers and
 * application layers are not going to know what to do with it either.
 * 
 * <p>
 * Use of this class should be considered for implementations of interfaces
 * where the implementation could be running on a different tier than the
 * generic interface.
 */
public class SkdRtException extends RuntimeException
{
  private static final long serialVersionUID = 0L;

  /**
   * Data/time stamp used to determine when this exception occured.
   */
  protected Date date;

  /**
   * Constructs an SkdRtException with no detail message.
   */
  public SkdRtException()
  {
    this((String) null, (Throwable) null);
  }

  /**
   * Constructs an SkdRtException with the specified detail message.
   * 
   * <p>
   * A detail message is a String that describes this particular exception.
   * 
   * @param s The detail message.
   */
  public SkdRtException(String s)
  {
    this(s, (Throwable) null);
  }

  /**
   * Constructs an SkdRtException with the root cause.
   * 
   * <p>
   * The rootException is the orginal exception that has been re-thrown as an
   * SkdRtException.
   * 
   * @param rootException The root exception.
   */
  public SkdRtException(Throwable rootException)
  {
    this((String) null, rootException);
  }

  /**
   * Constructs an SkdRtException with a generic detail message and sets the
   * root cause.
   * 
   * A detail message is a String that describes this particular exception. The
   * rootException is the orginal exception that has been re-thrown as an
   * SkdRtException.
   * 
   * @param s The detail message.
   * @param rootException The root exception.
   * 
   */
  public SkdRtException(String s, Throwable rootException)
  {
    super(s, rootException);
    date = new Date();
  }

  /**
   * Returns the date that this exeception occured.
   * 
   * @return The Date when this execption occured.
   */
  public Date getDate()
  {
    return (date);
  } // getDate ()

  /**
   * Routine used to dump the entire trace to the standard output device.
   */
  public void printAllStackTraces()
  {
    printAllStackTraces(System.out);
  }

  /**
   * Routine used to dump the entire trace to the print stream supplied.
   * 
   * @param s The print stream where this info should be saved.
   */
  public void printAllStackTraces(PrintStream s)
  {
    // dump top most
    s.println("\nSkdException caught: " + getClass().getName());
    s.println("\nSkdException time: " + date);
    printStackTrace(s);

    if (getCause() != null)
    {
      Throwable root = getCause();
      do
      {
        s.println("Root Cause: " + root.getClass().getName() + " msg: "
            + root.getMessage());
        root.printStackTrace(s);
        root = root.getCause();
        if (root == null)
          break;
      } while (true);
    }
  }
} // class SkdRtException
