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

/**
 * Pong response for testing
 * 
 * @author mandrus
 */
public class PongResponse extends AbstractJsonAeMessageResponse
{
  private String messageType;
  private String pingTime;
  private String pongTime;

  /**
   * Constructor
   */
  public PongResponse()
  {
    setMessageType("Pong");
  }

  /**
   * Constructor
   * 
   * @param ipRequest
   */
  public PongResponse(PingRequest ipRequest)
  {
    this();
    setFields(ipRequest);
  }

  // Getters
  @Override
  public String getMessageType()          {    return messageType;   }
  public String getPingTime()             {    return pingTime;     }
  public String getPongTime()             {    return pongTime;     }

  // Setters
  public void setMessageType(String messageID)
  {
    this.messageType = messageID;
  }
  public void setPingTime(String pingTime)
  {
    this.pingTime = pingTime;
  }
  public void setPongTime(String pongTime)
  {
    this.pongTime = pongTime;
  }

  /**
   * Set all fields from a request message
   * 
   * @param ipRequest
   */
  public void setFields(PingRequest ipRequest)
  {
    setPingTime(ipRequest.getPingTime());
  }
}
