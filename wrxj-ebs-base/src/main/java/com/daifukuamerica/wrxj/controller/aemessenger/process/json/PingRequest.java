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
package com.daifukuamerica.wrxj.controller.aemessenger.process.json;

import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.log.Logger;
import java.util.Date;

/**
 * Ping request message for testing
 * 
 * @author mandrus
 */
public class PingRequest implements JsonAeMessageRequest
{
  private String messageType;
  private String pingTime;

  /**
   * Constructor
   */
  public PingRequest()
  {
    setMessageType("Ping");
  }

  /**
   * toString
   */
  @Override
  public String toString()
  {
    return getMessageType() + " @ " + getPingTime();
  }
  
  // Getters
  @Override
  public String getMessageType()          {    return messageType;   }
  public String getPingTime()             {    return pingTime;     }

  // Setters
  public void setMessageType(String messageID)
  {
    this.messageType = messageID;
  }
  public void setPingTime(String pingTime)
  {
    this.pingTime = pingTime;
  }
  
  /**
   * Process this message
   * @param ipLogger
   * @return byte[] PongResponse
   */
  @SuppressWarnings("deprecation")
  @Override
  public byte[] process(Logger ipLogger)
  {
    PongResponse vpResponse = Factory.create(PongResponse.class, this);
    vpResponse.setPongTime(new Date().toLocaleString());
    return vpResponse.getBytes(ipLogger);
  }
}
