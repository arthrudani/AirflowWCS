package com.daifukuamerica.wrxj.host.messages;

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
import com.daifukuamerica.wrxj.host.MessageNameEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;

/**
 * Description:<BR>
 *  Interface for message formatters.
 *
 * @author       A.D.
 * @version      1.0   02/21/2005
 */
public interface MessageFormatter
{
 /**
  *  Method formats a message.  <i>Note: if position of the elements in the 
  *  message is important, the order of the ColumnObject array determines this.</i>
  *  @param fields ColumnObject array containing field names and values to
  *         include in the message.
  *  @param enumMessageName enumerated type containing message name object.
  *  @return String of formatted message.
  */
  public String formatMessage(ColumnObject[] fields, MessageNameEnum enumMessageName);
 /**
  * Method adds header and trailer to a collection of messages <i>after</i> they've
  * been formatted.  This is useful for nesting XML messages in a final wrapper
  * before sending:<p>
  * <code>message identifier prefix + Collection of Messages</code>.</p>
  * 
  * @param isContent the message content to be wrapped.
  * @param isWrapper the wrapper string.
  * @param inMessageSeq a sequence number to imbed in the outbound message
  *        if it's required.  Normally only Fixed Length and Delimited message
  *        formatters will make use of this.
  * @return String containing formatted message.
  */
  public String addPostFormatWrapper(String isContent, String isWrapper,
                                     int inMessageSeq);
 /**
  *  Method clears any pre-existing formatted string this object knows about.
  */
  public void clear();
}
