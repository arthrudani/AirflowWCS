package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.host.MessageNameEnum;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import java.util.Date;


/**
 * Description:<BR>
 *   Class to handle WrxToHost outbound data.  This class treats columns, and
 *   Keys as Objects.
 *
 * @author       A.D.  02/08/05
 * @version      1.0
 */
public class WrxToHostData extends AbstractSKDCData
{
  public static String HOSTNAME_NAME          = "SHOSTNAME";
  public static String MESSAGEIDENTIFIER_NAME = "SMESSAGEIDENTIFIER";
  public static String MESSAGE_NAME           = "SMESSAGE";
  public static String MESSAGEADDTIME_NAME    = "DMESSAGEADDTIME";
  public static String MESSAGEPROCESSED_NAME  = "IMESSAGEPROCESSED";
  public static String MESSAGESEQUENCE_NAME   = "IMESSAGESEQUENCE";
  public static String ORIGINALSQUENCE_NAME   = "IORIGINALSEQUENCE";
  public static String SENT_NAME              = "SENT";
  public static String RETRYCOUNT_NAME        = "RETRYCOUNT";
  public static String ACKED_NAME             = "ACKED";

  protected String  sHostName          = "";
  protected String  sMessageIdentifier = "";
  protected String  sMessage           = "";
  protected byte[]  xmlData            = null;
  protected Date    dMessageAddTime    = new Date();
  protected int     iMessageProcessed  = DBConstants.NO;
  protected int     iMessageSequence   = 0;
  protected int     iOriginalSequence  = 0;
  protected Date    sent               = null;
  protected int     retryCount         = 0;
  protected int     acked              = DBConstants.NO;
  private boolean retrieveCLOB      = false;

  public WrxToHostData()
  {
    super();
  }

  /**
   * This helps in debugging when we want to print the whole structure.
   */
  @Override
  public String toString()
  {
    String s = "sHostName:"          + sHostName                   +
               "\nsMessageIdentifier:" + sMessageIdentifier        +
               "\nsMessage:"         + sMessage                    +
               "\ndMessageAddTime:"  + sdf.format(dMessageAddTime) +
               "\niMessageSequence:" + iMessageSequence            +
               "\niOriginalSequence:" + iOriginalSequence          +
               "\nsent:" + (sent == null ? "" : sdf.format(sent))  +
               "\nretryCount:" + retryCount + 
               "\nacked:" + acked;
    try
    {
      s = s + "\niMessageProcessed:" + 
      DBTrans.getStringValue(MESSAGEPROCESSED_NAME, iMessageProcessed);
    }
    catch(NoSuchFieldException e)
    {
      s = s + "0";
    }
    s += super.toString();

    return(s);
  }

  /**
   *  Method to make a deep copy of this object.
   *
   *  @return copy of <code>WrxToHostData</code>.
   */
  @Override
  public WrxToHostData clone()
  {
    WrxToHostData vpClonedData = (WrxToHostData)super.clone();
//    vpClonedData.dMessageAddTime = (Date)dMessageAddTime.clone();

    return vpClonedData;
  }

  /**
   * Defines equality between two HostToWrxData objects.
   *
   * @param  absData <code>AbstractSKDCData</code> reference whose runtime type
   *         is expected to be <code>HostToWrxData</code>
   */
  @Override
  public boolean equals(AbstractSKDCData absData)
  {
    if (absData == null || !(absData instanceof WrxToHostData))
    {
      return(false);
    }
    WrxToHostData hidata = (WrxToHostData)absData;
    return(hidata.sHostName.equals(sHostName)                   &&
           hidata.sMessageIdentifier.equals(sMessageIdentifier) &&
           hidata.iMessageProcessed == iMessageProcessed        &&
           hidata.iMessageSequence == iMessageSequence);
  }

  /**
   * Resets the data in this class to the default.
   */
  @Override
  public void clear()
  {
    super.clear();                     // Pull in default behaviour.

    sHostName          = "";
    sMessageIdentifier = "";
    sMessage           = "";
    dMessageAddTime    = new Date();
    iMessageProcessed  = DBConstants.NO;
    iMessageSequence   = 0;
    iOriginalSequence  = 0;
    sent               = null;
    retryCount         = 0;
    acked              = DBConstants.NO;
    xmlData            = null;
  }

/*---------------------------------------------------------------------------
                     Column value get methods go here.
  ---------------------------------------------------------------------------*/
  /**
   * Fetches host name.
   * @return HostName value as string
   */
  public String getHostName()
  {
    return(sHostName);
  }

  /**
   * Fetches host Message Identifier.
   * @return MessageIdentifier as string
   */
  public String getMessageIdentifier()
  {
    return(sMessageIdentifier);
  }

  /**
   *  Method gets the message identifier as an Enumeration.
   *  @return enumeration of type MessageNameEnum.
   *  @see com.daifukuamerica.wrxj.host.MessageNameEnum MessageNameEnum
   */
  public MessageNameEnum getMessageIdentifierEnum()
  {
    MessageNameEnum vpHostOutEnum;
    if (sMessageIdentifier.trim().length() == 0)
      vpHostOutEnum = MessageOutNames.NONE;
    else
      vpHostOutEnum = MessageOutNames.getEnumObject(sMessageIdentifier);

    return(vpHostOutEnum);
  }

  /**
   * Fetches host message value
   * @return Message string
   */
  public String getMessage()
  {
    return(sMessage);
  }

  /**
   * Fetches Host XML data as raw bytes.
   * @return XML Message as a byte array.
   */
  public byte[] getMessageBytes()
  {
    return(xmlData);
  }

  /**
   * Fetches MessageAddTime date time value.
   * @return MessageAddTime value as Date object
   */
  public Date getMessageAddTime()
  {
    return(dMessageAddTime);
  }

  /**
   * Fetches Message Processed flag.
   * @return MessageProcessed flag as integer
   */
  public int getMessageProcessed()
  {
    return(iMessageProcessed);
  }


  /**
   * Fetches Message Sequence value
   * @return MessageSequence value as integer
   */
  public int getMessageSequence()
  {
    return(iMessageSequence);
  }
  
  /**
   * Fetches Message Sequence value
   * @return MessageSequence value as integer
   */
  public int getOriginalMessageSequence()
  {
    return(iOriginalSequence);
  }
  
  public Date getSent() {
      return sent;
  }
  
  public int getRetryCount() {
      return retryCount;
  }
  
  public int getAcked() {
      return acked;
  }

  /**
   * Method to get Clob retrieval property for this object. If this property is
   * set, the corresponding
   * {@link com.daifukuamerica.wrxj.dbadapter.data.WrxToHost#WrxToHost BaseDBInterface}
   * class will read the CLOB data.
   * 
   * @return current CLOB retrieval flag.
   */
  public boolean getClobRetrieval()
  {
    return(retrieveCLOB);
  }

/*---------------------------------------------------------------------------
               ******** Column Setting methods go here. ********
  ---------------------------------------------------------------------------*/
  /**
   * Sets Host Name
   */
  public void setHostName(String isHostName)
  {
    sHostName = checkForNull(isHostName);
    addColumnObject(new ColumnObject(HOSTNAME_NAME, isHostName));
  }

  /**
   * Sets Message Identifier value.
   */
  public void setMessageIdentifier(String isMessageIdentifier)
  {
    sMessageIdentifier = checkForNull(isMessageIdentifier);
    addColumnObject(new ColumnObject(MESSAGEIDENTIFIER_NAME, isMessageIdentifier));
  }

  /**
   * Sets Message value.
   */
  public void setMessage(String isMessage)
  {
    sMessage = checkForNull(isMessage);
    addColumnObject(new ColumnObject(MESSAGE_NAME, isMessage));
  }

  /**
   * Sets Host XML data in raw bytes.
   */
  public void setMessageBytes(byte[] iabXmlData)
  {
    xmlData = iabXmlData;
  }

  /**
   * Sets Message add time.
   */
  public void setMessageAddTime(Date ipMessageAddTime)
  {
    dMessageAddTime = ipMessageAddTime;
    addColumnObject(new ColumnObject(MESSAGEADDTIME_NAME, ipMessageAddTime));
  }

  /**
   * Sets Message processed flag.
   */
  public void setMessageProcessed(int inMessageProcessed)
  {
    try
    {
      DBTrans.getStringValue(MESSAGEPROCESSED_NAME, inMessageProcessed);
    }
    catch(NoSuchFieldException e)
    {                                  // Passed value wasn't valid. Default it
      inMessageProcessed = DBConstants.YES;
    }
    iMessageProcessed = inMessageProcessed;
    addColumnObject(new ColumnObject(MESSAGEPROCESSED_NAME,
      Integer.valueOf(inMessageProcessed)));
  }

  /**
   * Sets message sequence number.
   */
  public void setMessageSequence(int inMessageSequence)
  {
    iMessageSequence = inMessageSequence;
    addColumnObject(new ColumnObject(MESSAGESEQUENCE_NAME,
      Integer.valueOf(inMessageSequence)));
  }

  /**
   * Sets the original Host sent message sequence number.
   */
  public void setOriginalMessageSequence(int inOriginalSequence)
  {
    iOriginalSequence = inOriginalSequence;
    addColumnObject(new ColumnObject(ORIGINALSQUENCE_NAME,
      Integer.valueOf(inOriginalSequence)));
  }
  
  public void setSent(Date sent)
  {
    this.sent = sent;
    addColumnObject(new ColumnObject(SENT_NAME, sent));
  }
  
  public void setRetryCount(int retryCount)
  {
    this.retryCount = retryCount;
    addColumnObject(new ColumnObject(RETRYCOUNT_NAME,
      Integer.valueOf(retryCount)));
  }
  
  
  public void setAcked(int acked)
  {
    this.acked = acked;
    addColumnObject(new ColumnObject(ACKED_NAME,
      Integer.valueOf(acked)));
  }
  
  /**
   * Method to get Clob retrieval property for this object. If this property is
   * set, the corresponding
   * {@link com.daifukuamerica.wrxj.dbadapter.data.WrxToHost#WrxToHost BaseDBInterface}
   * class will read the CLOB data.
   * 
   * @param izRetrieveClob <code>boolean</code> of true indicates CLOB should
   *            be retrieved.
   */
  public void setClobRetrieval(boolean izRetrieveClob)
  {
    retrieveCLOB = izRetrieveClob;
  }

  /**
   * Required set field method. This method figures out what column was passed
   * to it and sets the value. This allows us to have a generic method for all
   * DB interfaces.
   */
  @Override
  public int setField(String colName, Object colValue)
  {
    int rtn = 0;

    if (colName.equalsIgnoreCase(HOSTNAME_NAME))
    {
      setHostName(colValue.toString());
    }
    else if (colName.equalsIgnoreCase(MESSAGEIDENTIFIER_NAME))
    {
      setMessageIdentifier(colValue.toString());
    }
    else if (colName.equalsIgnoreCase(MESSAGE_NAME))
    {
      setMessage(colValue.toString());
    }
    else if (colName.equalsIgnoreCase(MESSAGEADDTIME_NAME))
    {
      setMessageAddTime((Date)colValue);
    }
    else if (colName.equalsIgnoreCase(MESSAGEPROCESSED_NAME))
    {
      setMessageProcessed(((Integer)colValue).intValue());
    }
    else if (colName.equalsIgnoreCase(MESSAGESEQUENCE_NAME))
    {
      setMessageSequence(((Integer)colValue).intValue());
    }
    else if (colName.equalsIgnoreCase(ORIGINALSQUENCE_NAME))
    {
      setOriginalMessageSequence(((Integer)colValue).intValue());
    }
    else if (colName.equalsIgnoreCase(SENT_NAME))
    {
        setSent((Date)colValue);
    }
    else if (colName.equalsIgnoreCase(RETRYCOUNT_NAME))
    {
        setRetryCount(((Integer)colValue).intValue());
    }
    else if (colName.equalsIgnoreCase(ACKED_NAME))
    {
        setAcked(((Integer)colValue).intValue());
    }
    else
    {
      rtn = super.setField(colName, colValue);
    }

    return(rtn);
  }
}
