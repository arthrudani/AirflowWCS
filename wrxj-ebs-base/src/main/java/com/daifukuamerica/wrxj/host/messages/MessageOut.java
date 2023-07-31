package com.daifukuamerica.wrxj.host.messages;

/****************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright ? 2005 Daifuku America Corporation  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND
  TREATIES. NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED
  COPIED, DISTRIBUTED, REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED,
  EXPANDED, COLLECTED, COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED
  WITHOUT THE PRIOR WRITTEN CONSENT OF Daifuku America Corporation ANY
  USE OR EXPLOITATION OF THIS WORK WITHOUT AUTHORIZATION COULD SUBJECT THE
  PERPETRATOR TO CRIMINAL AND CIVIL LIABILITY.
 ****************************************************************************/
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dbadapter.data.HostConfigData;
import com.daifukuamerica.wrxj.host.MessageNameEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;

/**
 * Description:<BR>
 *   Abstract outbound message class.
 *
 * @author       A.D.
 * @version      1.0  03/21/2005
 */
public abstract class MessageOut
{
 /** The Host Name this message is intended for.  If this is left blank, the 
  *  message will be sent to all hosts listed in the HostOutAccess table.
  */
  private String msHostName = "";
 /** The message string */
  private String msMessage;
 /** Message identifier for this message. */
  protected MessageNameEnum enumMessageName;
 /** Indicates if a message has been wrapped with an identifier (Header and trailer). */
  protected boolean messageWrapped;
 /** Holds Formatter instance for this message. */
  protected MessageFormatter msgfmt;
 /** Fields for a given message. */
  protected ColumnObject[] messageFields;

 /**
  * Method assigns a particular host for which this message is intended.
  * @param isHostName  the name of the host.
  */
  public void assignHost(String isHostName)
  {
    msHostName = isHostName;
  }
  
 /**
  * Method to clear columns in this message.  This allows for reuse of this
  * class' instance.  <b>Warning:</b>this method should be used when multiple
  * messages are to be formatted and sent as one string.  This method does <u>not</u>
  * erase any previously formatted strings this object knows about.
  * @see MessageOut#clearAllState clearAllState
  */
  public void clear()
  {
    MessageHelper.defaultFields(messageFields);
  }

 /**
  *  Method will reset <u>all</u> state in this object.
  */
  public void clearAllState()
  {
    clear();
    msHostName = "";
    messageWrapped = false;
    msgfmt.clear();
  }
  
 /**
  *  Formats outbound messages to a host system.  This method will call the
  *  MessageFormatFactory to get the appropriate formatter.
  */
  public void format()
  {
                                       // Do the formatting.
    msMessage = msgfmt.formatMessage(messageFields, enumMessageName);
  }
  
  public String getAssignedHostName()
  {
    return(msHostName);
  }
  
  /**
   *  Method retrieves message identifier of current message.
   *  @return String containing messsage identifier.
   */
  public String getMessageIdentifier()
  {
    String vsMessageName;
    if (Application.getInt(HostConfigData.ACTIVE_DATA_TYPE) == DBConstants.XML)
      vsMessageName = enumMessageName.getQualifiedName();
    else
      vsMessageName = enumMessageName.getValue();
    
    return(vsMessageName);
  }
  
 /**
  * Method makes final preparations for a message to be sent to the host.  This
  * implementation adds a message wrapper around the text to be sent if in-turn
  * the corresponding message formatter implements this requirement.
  *
  * @param inMessageSequence a sequence number to imbed in the outbound message
  *        if it's required.  Normally only Fixed Length and Delimited message
  *        formatters will make use of this.
  * @return byte array of text to be sent.
  */
  public byte[] prepareMessageToSend(int inMessageSequence)
  {
    String vsMessageToSend = msgfmt.addPostFormatWrapper(msMessage,
            getMessageIdentifier(), inMessageSequence);
    return(vsMessageToSend.getBytes());
  }
}
