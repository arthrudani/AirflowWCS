/* ***************************************************************************
  Copyright (c) 2018 Daifuku North America Holding Company. All Rights Reserved.
  
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.messageformat;

import com.daifukuamerica.wrxj.util.SKDCUtility;

/**
 * This class defines IPC "AE Message" messages that are converted to AE 
 * Messages and sent to .NET Wynsoft products.  
 * 
 * @author       mandrus<BR>
 * @version      1.0
 */
public class AEMessageEventDataFormat extends MessageDataFormat
{
  /*==========================================================================*/
  /* Message Text Constants                                                   */
  /*==========================================================================*/
  private static final String DELIMITER = "|";

  private String msTargetInstanceName = "";
  private String msMessageText = "";
  private String msMessageData = "";

  /**
   * Simple constructor
   * 
   * @param senderName
   */
  public AEMessageEventDataFormat(String senderName)
  {
    super(-1, senderName);
  }
  
  /**
   * Required constructor
   * 
   * @param messageType
   * @param senderName
   */
  public AEMessageEventDataFormat(int messageType, String senderName)
  {
    super(messageType, senderName);
  }

  /**
   * Convert to data string for transmission
   */
  @Override
  public void createDataString()
  {
    setDataString(msTargetInstanceName
        + DELIMITER + msMessageText
        + DELIMITER + msMessageData);
  }

  /**
   * Parse data string for reception
   */
  @Override
  public boolean decodeDataString()
  {
    if (SKDCUtility.isFilledIn(getDataString()))
    {
      String[] vasParts = getDataString().split("\\" + DELIMITER);
      if (vasParts.length == 3)
      {
        setTargetInstanceName(vasParts[0]);
        setMessageText(vasParts[1]);
        setMessageData(vasParts[2]);
        return true;
      }
    }
    return false;
  }

  /**
   * Get the Target Instance Name
   * @return
   */
  public String getTargetInstanceName()
  {
    return msTargetInstanceName;
  }

  /**
   * Set the Target Instance Name
   * 
   * @param msTargetInstanceName
   */
  public void setTargetInstanceName(String msTargetInstanceName)
  {
    this.msTargetInstanceName = msTargetInstanceName;
  }

  /**
   * Get the Message Text
   * @return
   */
  public String getMessageText()
  {
    return msMessageText;
  }

  /**
   * Set the Message Text
   * 
   * @param isMessageText
   */
  public void setMessageText(String isMessageText)
  {
    this.msMessageText = isMessageText;
  }
  
  /**
   * Get the Message Data
   * @return
   */
  public String getMessageData()
  {
    return msMessageData;
  }

  /**
   * Set the Message Data
   * 
   * @param isMessage
   */
  public void setMessageData(String isMessage)
  {
    this.msMessageData = isMessage;
  }
}
