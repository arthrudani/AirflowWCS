package com.daifukuamerica.wrxj.util;

import java.io.PrintStream;
import java.util.Date;

/**
 * Base class of all Daifuku America Exceptions.
 * 
 * <p>
 * All specialized exceptions written by SkDaifuku should extend this class.
 * There are simple helper methods for handling root cause, but more importantly
 * it types our exceptions allowing for applications to narrow to any exception
 * that we might throw as needed.
 * 
 * @author cadams
 */
@SuppressWarnings("serial")
public class SkdException extends Exception
{
  /**
   * Data/time stamp used to determine when this exception occured.
   */
  protected Date date;

  /**
   * Default Constructor.
   * 
   * @see java.lang.Exception#Exception()
   */
  public SkdException()
  {
    super();
  }

  /**
   * Constructor with message.
   * 
   * @see java.lang.Exception#Exception(java.lang.String)
   */
  public SkdException(String message)
  {
    super(message);
  }

  /**
   * Constructor with cause.
   * 
   * @see java.lang.Exception#Exception(java.lang.Throwable)
   */
  public SkdException(Throwable cause)
  {
    super(cause);
  }

  /**
   * Constructor with message and cause.
   * 
   * @see java.lang.Exception#Exception(java.lang.String, java.lang.Throwable)
   */
  public SkdException(String message, Throwable cause)
  {
    super(message, cause);
  }

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
        root = ((SkdRtException) root).getCause();
        if (root == null)
          break;
      } while (true);
    }
  }
} 