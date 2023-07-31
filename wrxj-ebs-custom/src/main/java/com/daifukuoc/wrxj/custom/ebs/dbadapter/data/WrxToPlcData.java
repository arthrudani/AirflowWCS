package com.daifukuoc.wrxj.custom.ebs.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.host.MessageNameEnum;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import java.util.Date;


/**
 * Description:<BR>
 *   Class to handle WrxToPlc outbound data.  This class treats columns, and
 *   Keys as Objects.
 *
 * @author       A.D.  02/08/05
 * @version      1.0
 */
public class WrxToPlcData extends AbstractSKDCData
{
  public static String PORTNAME_NAME          = "SPORTNAME";
  public static String MESSAGEIDENTIFIER_NAME = "SMESSAGEIDENTIFIER";
  public static String MESSAGE_NAME           = "SMESSAGE";
  public static String MESSAGEADDTIME_NAME    = "DMESSAGEADDTIME";
  public static String MESSAGEPROCESSED_NAME  = "IMESSAGEPROCESSED";
  public static String MESSAGESEQUENCE_NAME   = "IMESSAGESEQUENCE";

  protected String  sPortName          = "";
  protected String  sMessageIdentifier = "";
  protected String  sMessage           = "";
  protected byte[]  xmlData            = null;
  protected Date    dMessageAddTime    = new Date();
  protected int     iMessageProcessed  = DBConstants.NO;
  protected int     iMessageSequence   = 0;
  private boolean retrieveCLOB      = false;

  public WrxToPlcData()
  {
    super();
  }

  /**
   * This helps in debugging when we want to print the whole structure.
   */
  @Override
  public String toString()
  {
    String s = "sPortName:"          + sPortName                   +
               "\nsMessageIdentifier:" + sMessageIdentifier        +
               "\nsMessage:"         + sMessage                    +
               "\ndMessageAddTime:"  + sdf.format(dMessageAddTime) +
               "\niMessageSequence:" + iMessageSequence;
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
   *  @return copy of <code>WrxToPlcData</code>.
   */
  @Override
  public WrxToPlcData clone()
  {
    WrxToPlcData vpClonedData = (WrxToPlcData)super.clone();
//    vpClonedData.dMessageAddTime = (Date)dMessageAddTime.clone();

    return vpClonedData;
  }

  /**
   * Defines equality between two PlcToWrxData objects.
   *
   * @param  absData <code>AbstractSKDCData</code> reference whose runtime type
   *         is expected to be <code>PlcToWrxData</code>
   */
  @Override
  public boolean equals(AbstractSKDCData absData)
  {
    if (absData == null || !(absData instanceof WrxToPlcData))
    {
      return(false);
    }
    WrxToPlcData hidata = (WrxToPlcData)absData;
    return(hidata.sPortName.equals(sPortName)                   &&
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

    sPortName          = "";
    sMessageIdentifier = "";
    sMessage           = "";
    dMessageAddTime    = new Date();
    iMessageProcessed  = DBConstants.NO;
    iMessageSequence   = 0;
    xmlData            = null;
  }

/*---------------------------------------------------------------------------
                     Column value get methods go here.
  ---------------------------------------------------------------------------*/
  /**
   * Fetches Port name.
   * @return PortName value as string
   */
  public String getPortName()
  {
    return(sPortName);
  }

  /**
   * Fetches Port Message Identifier.
   * @return MessageIdentifier as string
   */
  public String getMessageIdentifier()
  {
    return(sMessageIdentifier);
  }

  /**
   *  Method gets the message identifier as an Enumeration.
   *  @return enumeration of type MessageNameEnum.
   *  @see com.daifukuamerica.wrxj.Plc.MessageNameEnum MessageNameEnum
   */
  public MessageNameEnum getMessageIdentifierEnum()
  {
    MessageNameEnum vpPortOutEnum;
    if (sMessageIdentifier.trim().length() == 0)
      vpPortOutEnum = MessageOutNames.NONE;
    else
      vpPortOutEnum = MessageOutNames.getEnumObject(sMessageIdentifier);

    return(vpPortOutEnum);
  }

  /**
   * Fetches Port message value
   * @return Message string
   */
  public String getMessage()
  {
    return(sMessage);
  }

  /**
   * Fetches Plc XML data as raw bytes.
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
   * Method to get Clob retrieval property for this object. If this property is
   * set, the corresponding
   * {@link com.daifukuamerica.wrxj.dbadapter.data.WrxToPlc#WrxToPlc BaseDBInterface}
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
   * Sets Port Name
   */
  public void setPortName(String isPortName)
  {
    sPortName = checkForNull(isPortName);
    addColumnObject(new ColumnObject(PORTNAME_NAME, isPortName));
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
   * Sets Plc XML data in raw bytes.
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
        new Integer(inMessageProcessed)));
  }

  /**
   * Sets message sequence number.
   */
  public void setMessageSequence(int inMessageSequence)
  {
    iMessageSequence = inMessageSequence;
    addColumnObject(new ColumnObject(MESSAGESEQUENCE_NAME,
        new Integer(inMessageSequence)));
  }

  /**
   * Method to get Clob retrieval property for this object. If this property is
   * set, the corresponding
   * {@link com.daifukuamerica.wrxj.dbadapter.data.WrxToPlc#WrxToPlc BaseDBInterface}
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

    if (colName.equalsIgnoreCase(PORTNAME_NAME))
    {
      setPortName(colValue.toString());
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
    else
    {
      rtn = super.setField(colName, colValue);
    }

    return(rtn);
  }
}
