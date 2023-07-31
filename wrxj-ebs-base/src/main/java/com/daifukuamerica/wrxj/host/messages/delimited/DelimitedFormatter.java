package com.daifukuamerica.wrxj.host.messages.delimited;

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
import com.daifukuamerica.wrxj.host.messages.MessageFormatter;
import com.daifukuamerica.wrxj.host.messages.MessageHelper;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Description:<BR>
 * Class for formatting delimited messages.
 *
 * @author       A.D.
 * @version      1.0   03/03/2005
 */
public class DelimitedFormatter implements MessageFormatter 
{
  String sMessage = "";
  private SimpleDateFormat mpDateFmt = new SimpleDateFormat(SKDCConstants.HOST_DATE_FORMAT);

  /**
   * {@inheritDoc} The message content returned here has no sequence # or name
   * inserted. In the TCP/IP case, this info. is pre-pended before the message
   * is sent. In the DB Host case, it is up to the host program to read all the
   * fields in the database table to get the correct info. (i.e. the
   * sMessageIdentifier, and iMessageSequence columns in the HostToWRx table).
   */
  public String formatMessage(ColumnObject[] ipFields, MessageNameEnum ipMessageName)
  {
    String vsValue;
    
    for(ColumnObject vpCObj : ipFields)
    {
      if (sMessage.length() > 0)
      {
        sMessage += MessageHelper.HOST_MESSAGE_DELIM;
      }
      
      if (vpCObj.getColumnName().equalsIgnoreCase("dTransactionTime"))
        vsValue = mpDateFmt.format((Date)vpCObj.getColumnValue());
      else
        vsValue = (vpCObj.getColumnValue() != null) ? vpCObj.getColumnValue().toString() : "";

      sMessage += vsValue;
    }
    
    return(sMessage);
  }
  
  /**
   * {@inheritDoc} Method adds on Message ID and Sequence number
   * @param isMesgID
   * @param inMesgSeq
   */
  public String addPostFormatWrapper(String isContent, String isMesgID, int inMesgSeq)
  {
    String vsMesgWrapper = inMesgSeq + MessageHelper.HOST_MESSAGE_DELIM +
                           isMesgID + MessageHelper.HOST_MESSAGE_DELIM;

    return(vsMesgWrapper + isContent);
  }

  /**
   * {@inheritDoc}
   */
  public void clear()
  {
    sMessage = "";
  }
}
