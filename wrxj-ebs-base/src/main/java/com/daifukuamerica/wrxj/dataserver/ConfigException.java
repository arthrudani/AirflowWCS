package com.daifukuamerica.wrxj.dataserver;

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

 /**
  *  Exception class to handle Configuration exceptions in WRx-J.
  *
  *  @author A.D.
  *  @since 03-May-2007
  */
@SuppressWarnings("serial")
public class ConfigException extends Exception
{
  private int mnErrorCode = 0;
  private String msErrorMessage;

 /**
  * Constructs an <code>ConfigException</code> with <code>null</code>
  * as its error detail message.
  */
  public ConfigException()
  {
    super();
  }

 /**
  * Constructs an <code>ConfigException</code> with the specified detail
  * message. The error message string <code>message</code> can later be
  * retrieved by the <code>{@link java.lang.Throwable#getMessage}</code>
  * method of class <code>java.lang.Throwable</code>.
  *
  * @param isErrorMessage <code>String</code> containing the detail message.
  */
  public ConfigException(String isErrorMessage)
  {
    super(isErrorMessage);
  }

 /**
  * Convenience constructor
  * @param inErrorCode the error code.
  * @param isErrorMessage <code>String</code> containing the detail message.
  */
  public ConfigException(int inErrorCode, String isErrorMessage)
  {
    super(isErrorMessage);
    mnErrorCode = inErrorCode;
  }
  
 /**
  * Constructs an <code>ConfigException</code> wrapping
  * <code>ipRootExcep</code>.
  * @param ipRootExcep  reference to the root exception in this exception chain.
  */
  public ConfigException(Throwable ipRootExcep)
  {
    super(ipRootExcep);
  }

 /**
  * Convenience constructor
  * @param isErrorMessage <code>String</code> containing the detail message.
  * @param ipRootExcep  reference to the root exception in this exception chain.
  */
  public ConfigException(String isErrorMessage, Throwable ipRootExcep)
  {
    super(isErrorMessage);
    initCause(ipRootExcep);
  }
  
  public void setErrorCode(int inErrorCode)
  {
    mnErrorCode = inErrorCode;
  }
  
  public int getErrorCode()
  {
    return(mnErrorCode);
  }
  
  public void setErrorMessage(String isErrorMesssage)
  {
    msErrorMessage = isErrorMesssage;
  }
  
  public String getErrorMessage()
  {
    return(msErrorMessage);
  }
}
