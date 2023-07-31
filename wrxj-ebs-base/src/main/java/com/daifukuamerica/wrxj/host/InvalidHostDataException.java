package com.daifukuamerica.wrxj.host;

/****************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright ? 2004 Daifuku America Corporation  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND
  TREATIES. NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED
  COPIED, DISTRIBUTED, REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED,
  EXPANDED, COLLECTED, COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED
  WITHOUT THE PRIOR WRITTEN CONSENT OF Daifuku America Corporation ANY
  USE OR EXPLOITATION OF THIS WORK WITHOUT AUTHORIZATION COULD SUBJECT THE
  PERPETRATOR TO CRIMINAL AND CIVIL LIABILITY.
 ****************************************************************************/

import com.daifukuamerica.wrxj.host.messages.HostError;

 /**
  *  Exception class to handle Host data errors.
  *  @author A.D.    08-May-05
  */
public class InvalidHostDataException extends RuntimeException
{
  private static final long serialVersionUID = 0L;
  private int mnErrorCode = HostError.INVALID_DATA;
  private int mnOriginalSequence;
  private String msHostName = "";
  private String msErrorMessage;
  private boolean mzSetErrorFlag = true;

 /**
  * Constructs an <code>InvalidHostDataException</code> with <code>null</code>
  * as its error detail message.
  */
  public InvalidHostDataException()
  {
    super();
  }

 /**
  * Constructs an <code>InvalidHostDataException</code> with the specified detail
  * message. The error message string <code>message</code> can later be
  * retrieved by the <code>{@link java.lang.Throwable#getMessage}</code>
  * method of class <code>java.lang.Throwable</code>.
  *
  * @param isErrorMessage <code>String</code> containing the detail message.
  */
  public InvalidHostDataException(String isErrorMessage)
  {
    super(isErrorMessage);
    msErrorMessage = isErrorMessage;
  }

 /**
  * Convenience constructor with selective information for the host.
  * @param inErrorCode the error code.
  * @param isErrorMessage the Error message to send to host.
  *        being rejected.
  */
  public InvalidHostDataException(int inErrorCode, String isErrorMessage)
  {
    this(isErrorMessage);
    mnErrorCode = inErrorCode;
  }
  
 /**
  * Convenience constructor with selective information for the host.
  * @param inErrorCode the error code.
  * @param isErrorMessage the Error message to send to host.
  * @param inOriginalSequence the original sequence of the inbound message that is
  *        being rejected.
  */
  public InvalidHostDataException(int inErrorCode, String isErrorMessage,
                                  int inOriginalSequence)
  {
    this(inErrorCode, isErrorMessage);
    mnOriginalSequence = inOriginalSequence;
  }

 /**
  * Convenience constructor with selective information for the host.
  * @param inErrorCode the error code.
  * @param isHostName the host name of host for which this message is intended.
  * @param isErrorMessage the Error message to send to host.
  * @param inOriginalSequence the original sequence of the inbound message that is
  *        being rejected.
  */
  public InvalidHostDataException(int inErrorCode, String isHostName, String isErrorMessage,
                                  int inOriginalSequence)
  {
    this(inErrorCode, isErrorMessage, inOriginalSequence);
    msHostName = isHostName;
  }

 /**
  * Constructs an <code>InvalidHostDataException</code> wrapping
  * <code>ipRootExcep</code>.
  */
  public InvalidHostDataException(Throwable ipRootExcep)
  {
    super(ipRootExcep);
  }

  public InvalidHostDataException(String isErrorMessage, Throwable ipRootExcep)
  {
    this(isErrorMessage);
    initCause(ipRootExcep);
  }
  
  public InvalidHostDataException(int inErrorCode, int inOriginalSequence,
                                  Throwable ipRootExcep)
  {
    mnErrorCode = inErrorCode;
    mnOriginalSequence = inOriginalSequence;
    initCause(ipRootExcep);
  }

  public InvalidHostDataException(int inErrorCode, String isErrorMessage,
                                  int inOriginalSequence, Throwable ipRootExcep)
  {
    this(isErrorMessage, ipRootExcep);
    mnErrorCode = inErrorCode;
    mnOriginalSequence = inOriginalSequence;
  }

 /**
  * Mark the original host message that caused this exception as
  * {@link com.daifukuamerica.wrxj.jdbc.DBConstants#PROC_ERROR PROC_ERROR}
  * @param izSetErrorFlag
  */
  public void setInboundMessageErrorFlag(boolean izSetErrorFlag)
  {
    mzSetErrorFlag = izSetErrorFlag;
  }

  public boolean getInboundMessageErrorFlag()
  {
    return(mzSetErrorFlag);
  }

  public void setErrorCode(int inErrorCode)
  {
    mnErrorCode = inErrorCode;
  }
  
  public int getErrorCode()
  {
    return(mnErrorCode);
  }
  
  public void setHostName(String isHostName)
  {
    msHostName = isHostName;
  }
  
  public String getHostName()
  {
    return(msHostName);
  }
  
  public void setErrorMessage(String isErrorMesssage)
  {
    msErrorMessage = isErrorMesssage;
  }
  
  public String getErrorMessage()
  {
    return(msErrorMessage);
  }

  public void setOriginalSequence(int inOriginalSequence)
  {
    this.mnOriginalSequence = inOriginalSequence;
  }

  public int getOriginalSequence()
  {
    return(mnOriginalSequence);
  }
}
