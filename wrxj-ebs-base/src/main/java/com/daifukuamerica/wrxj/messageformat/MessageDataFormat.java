package com.daifukuamerica.wrxj.messageformat;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */
import java.util.StringTokenizer;

public abstract class MessageDataFormat implements Cloneable
{
//Data passed between the Station Device and the Scheduler
  private int iMessageType;     // Each device typr will have different type ie AGC diff from RF
  private String sSendersName;
  private String sDataString;

  public abstract void createDataString();
  public abstract boolean decodeDataString();

  public MessageDataFormat(int messageType, String senderName)
  {
    setMessageType(messageType);
    setSendersName(senderName);
  }


  protected void clearAllData()
  {
    setDataString(null);
  }

  /**
   *  Method to make a copy of this object.
   *
   *  @return copy of <code>MessageDataFormat</code>.
   */
  @Override
  public MessageDataFormat clone()
  {
    MessageDataFormat vpClonedMessage = null;

    try
    {
      vpClonedMessage = (MessageDataFormat)super.clone();
    }
    catch(CloneNotSupportedException e)
    {
      throw new InternalError(e.toString());
    }
    
    return(vpClonedMessage);
  }

  @Override
  public String toString()
  {
    String s = ", iMessageType = " + getMessageType() +
               ", sSendersName = " + getSendersName() +
               ", sDataString = " + getDataString();
    return (s);
  }

  public String createStringToSend()
  {
    createDataString();
    String s = getMessageType() + "," +
               getSendersName() + "," +
               getDataString();

    return (s);
  }

  public boolean decodeReceivedString(String myString)
  {
    boolean validMessage = true;
    StringTokenizer parser = new StringTokenizer(myString, ",");
    try
    {
      clearAllData();
      setMessageType(Integer.parseInt(parser.nextToken()));
      setSendersName(parser.nextToken());
      setDataString(parser.nextToken());
      decodeDataString();
    }
    catch (Exception e)
    {
      validMessage = false;
    }
    return validMessage;
  }

  private void setMessageType(int messageType)
  {
    iMessageType = messageType;

  }

  public int getMessageType()
  {
    return iMessageType ;

  }

  public String getScreenMessageType()
  {
    String myMessageType = String.valueOf(getMessageType());
    return myMessageType;
  }

  private void setSendersName(String sendersName)
  {
    if( sendersName == null )
    {
      sSendersName = sendersName;
    }
    else
    {
      sSendersName = sendersName.trim();
    }
  }

  public String getSendersName()
  {
    return sSendersName;
  }

  public String getScreenSendersName()
  {
    String mySendersName = getSendersName();
    return mySendersName;
  }

  public void setDataString(String dataString)
  {
/**
    if( dataString == null )
    {
      sDataString = dataString;
    }
    else
    {
      sDataString = dataString.trim();
    }
***/
    this.sDataString = dataString;
  }


  public String getDataString()
  {
    return sDataString;

  }

  public String getScreenDataString()
  {
    String myDataString = getDataString();
    return myDataString;
  }

}