package com.daifukuamerica.wrxj.host.messages.xml;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dbadapter.data.HostConfig;
import com.daifukuamerica.wrxj.dbadapter.data.HostConfigData;
import com.daifukuamerica.wrxj.host.MessageNameEnum;
import com.daifukuamerica.wrxj.host.messages.MessageFormatter;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;

/**
 * Description:<BR>
 * Class for formatting XML messages
 *
 * @author       A.D.
 * @version      1.0   03/03/2005
 */
public class XMLFormatter implements MessageFormatter 
{
  /** XML Declaration */
  protected final String XML_DECL = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
  protected String sMessage = "";
  protected SimpleDateFormat mpDateFmt = new SimpleDateFormat(SKDCConstants.HOST_DATE_FORMAT);
  protected boolean mzSendEmptyTags = Application.getBoolean(Application.HOSTCFG_DOMAIN +
          this.getClass().getSimpleName() + ".SendEmptyFields", true);

 /**
  * {@inheritDoc} This method does the actual work of formatting an XML Message.
  *  @param ipFields the message field name-value pair.
  *  @param ipMessageName enumerated type containing message name object.
  *  @return a XML formatted string.
  */
  @Override
  public synchronized String formatMessage(ColumnObject[] ipFields, MessageNameEnum ipMessageName)
  {
    String vsMessageName = ipMessageName.getValue();
                                       // Form the body of the message.
    sMessage += ("<" + vsMessageName + ">");
    sMessage += SKDCConstants.EOL_CHAR;
    sMessage += createXMLElement(ipFields);
    sMessage += ("</" + vsMessageName + ">");
    sMessage += SKDCConstants.EOL_CHAR;
    
    return(sMessage);
  }

 /**
  * {@inheritDoc}
  * @param isXMLContent The actual XML message content.
  * @param isQualifiedMessageID Overall message identifier so that multiple
  *        messages can be nested inside.
  * @return formatted message string.
  */
  @Override
  public String addPostFormatWrapper(String isXMLContent, String isQualifiedMessageID,
                                     int inMessageSeq)
  {
    String vsXMLHeader = "";

    if (Application.getInt(HostConfigData.ACTIVE_TRANSPORT_TYPE) == HostConfig.TCPIP)
    {
      vsXMLHeader = "[" + inMessageSeq + ";" + isQualifiedMessageID + ";]";
    }
    vsXMLHeader = vsXMLHeader + XML_DECL + SKDCConstants.EOL_CHAR;

    return(vsXMLHeader + "<" + isQualifiedMessageID + ">" + SKDCConstants.EOL_CHAR +
           isXMLContent + "</" + isQualifiedMessageID + ">" + SKDCConstants.EOL_CHAR);
  }

 /**
  *  {@inheritDoc}
  */
  @Override
  public void clear()
  {
    sMessage = "";
  }
  
 /**
  * Method creates start-tags, end-tags for all message contents in the passed
  * in array.
  * @param ipFields Array of ColumnObject data containing fields to format.
  * @return XML formatted string.
  */
  protected String createXMLElement(ColumnObject[] ipFields)
  {
    String vsXMLContent = "";
    
    for(ColumnObject vpCObj : ipFields)
    {
      String vsTemp = "";

      if (vpCObj.getColumnName().equalsIgnoreCase("dTransactionTime"))
      {
        Date vpDate = (Date)vpCObj.getColumnValue();
        vsTemp = createXMLTag(mpDateFmt.format(vpDate), vpCObj.getColumnName());
      }
      else
      {
        vsTemp = createXMLTag(vpCObj.getColumnValue().toString(),
                              vpCObj.getColumnName());
      }

      if (!vsTemp.isEmpty())
        vsXMLContent += vsTemp;
    }

    return(vsXMLContent);
  }
  
 /**
  * Method creates start-tags, end-tags for all message contents in the passed
  * in array.
  * @param isContent String to be wrapped.
  * @param isWrapper String to wrap content with.
  * @return XML formatted string.
  */
  protected String createXMLTag(String isContent, String isWrapper)
  {
    if (isContent == null || isContent.trim().length() == 0)
    {
      if (mzSendEmptyTags)
        return("  <" + isWrapper + "/>" + SKDCConstants.EOL_CHAR);
      else
        return("");
    }
    else
    {
      return("  <" + isWrapper + ">" + isContent + "</" + isWrapper + ">" +
             SKDCConstants.EOL_CHAR);
    }
  }
}
