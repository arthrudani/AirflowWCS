package com.daifukuamerica.wrxj.jdbc;

/*
                       SKDC Corporation
                       International Center
                   5202 Douglas Corrigan Way
                Salt Lake City, Utah  84116-3192
                        (801) 359-9900

   This software is furnished under a license and may be used
   and copied only in accordance with the terms of such license.
   This software or any other copies thereof in any form, may not be
   provided or otherwise made available, to any other person or company
   without written consent from SKDC Corporation.

   SKDC assumes no responsibility for the use or reliability of
   software which has been modified without approval.
*/

import com.daifukuamerica.wrxj.util.SKDCConstants;

/**
 * An exception class used to report database problems.
 *
 * @author avt
 * @author A.D.  Added root cause exception wrapping.
 * @version 1.0
 */
public class DBException extends Exception
{
  private static final long serialVersionUID = 0L;
  
  // TODO: Replace this with a more appropriate name.
  public static final int ITEMCANNOTGOINLOAD = 1;

  private int errCode;
  private boolean dbDuplicate = false;

 /**
  * Create a data base exception.
  *
  */
  public DBException()
  {
    super();
  }

 /**
  * Create a data base exception using the reason given.
  *
  * @param reason Text containing problem description.
  *
  */
  public DBException(String reason)
  {
    super(reason);
  }

 /**
  * Create a data base exception using the reason given and for an attempt
  * to add duplicate data.
  *
  * @param reason Text containing problem description.
  * @param duplicate Set true if this was caused by duplicate entry in the
  * data base.
  *
  */
  public DBException(String reason, boolean duplicate)
  {
    super(reason);
    dbDuplicate = duplicate;
  }

 /**
  * Create a new DBException wrapping an existing exception.
  *
  * <p>The existing exception will be embedded in the new
  * one, and its message will become the default message for
  * the DBException.</p>
  *
  * @param rootExcep <code>Throwable</code> containing the root exception to be
  *        wrapped in a DBException.
  */
  public DBException(Throwable rootExcep)
  {
    super(rootExcep);
  }
    
    
 /**
  * Create a new DBException from an existing exception.
  *
  * <p>The existing exception will be embedded in the new
  * one, but the new exception will have its own message.</p>
  *
  * @param message <code>String</code> containing the error message.
  * @param rootExcep <code>Throwable</code> containing the root exception to be
  *        wrapped in a DBException.
  */
  public DBException(String message, Throwable rootExcep)
  {
    super(message, rootExcep);
    if (rootExcep instanceof DBException)
    {
      errCode = ((DBException)rootExcep).getErrorCode();
      dbDuplicate = ((DBException)rootExcep).getDuplicate();
    }
//    rootExcep.fillInStackTrace();
//    initCause(rootExcep);
  }

  public DBException(String isMessage, int inErrorCode)
  {
    super(isMessage);
    errCode = inErrorCode;
  }
  
  public int getErrorCode()
  {
    return(errCode);
  }
  
  public boolean getDuplicate()
  {
    return(dbDuplicate);
  }

  public void setErrorCode(int inErrorCode)
  {
    errCode = inErrorCode;
  }

 /**
  *  Method to show if caused by duplicate data.
  *
  *  @return boolean of <code>true</code> if caused by duplicate data.
  */
  public boolean isDuplicate()
  {
    return (dbDuplicate);
  }
  
  public static String toString(Throwable myExc)
  {
    String s = SKDCConstants.EOL_CHAR;
    
    if (myExc != null)
    {
      StackTraceElement[] tracedStack = myExc.getStackTrace();
      s = s + myExc.getMessage() + ". " + SKDCConstants.EOL_CHAR;
      
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
