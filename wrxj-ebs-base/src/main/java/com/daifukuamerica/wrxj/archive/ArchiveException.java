package com.daifukuamerica.wrxj.archive;

/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright (c) 2004-2008 Daifuku America Corporation  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/

import com.daifukuamerica.wrxj.util.SKDCConstants;

/**
 * An exception class used to report Archiving problems.
 *
 * @author A.D.
 * @version 1.0
 */
public class ArchiveException extends Exception
{
  private static final long serialVersionUID = 0L;

 /**
  * Creates an Archiving Exception.
  */
  public ArchiveException()
  {
    super();
  }

 /**
  * Creates an Archiving Exception allowing a message to be given.
  * @param message Text containing problem description.
  */
  public ArchiveException(String message)
  {
    super(message);
  }

 /**
  * Create a new ArchiveException wrapping an existing exception.
  *
  * <p>The existing exception will be embedded in the new
  * one, and its message will become the default message for
  * the ArchiveException.</p>
  *
  * @param rootExcep <code>Throwable</code> containing the root exception to be
  *        wrapped in a ArchiveException.
  */
  public ArchiveException(Throwable rootExcep)
  {
    super(rootExcep);
  }
    
    
 /**
  * Create a new ArchiveException from an existing exception.
  *
  * <p>The existing exception will be embedded in the new
  * one, but the new exception will have its own message.</p>
  *
  * @param message <code>String</code> containing the error message.
  * @param rootExcep <code>Throwable</code> containing the root exception to be
  *        wrapped in a ArchiveException.
  */
  public ArchiveException(String message, Throwable rootExcep)
  {
    super(message, rootExcep);
  }

  public static String toString(Exception myExc)
  {
    String s = SKDCConstants.EOL_CHAR;
    
    if (myExc != null)
    {
      StackTraceElement[] tracedStack = myExc.getStackTrace();
      for(int elem = 0; elem < tracedStack.length; elem++)
      {
        String tmpStr = tracedStack[elem].getClassName()  + "." +
                        tracedStack[elem].getMethodName() + "()-->Line: " +
                        tracedStack[elem].getLineNumber();
        s = s + tmpStr + SKDCConstants.EOL_CHAR;
      }
    }
    
    return(s);
  }
}
