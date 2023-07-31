/* ***************************************************************************
  Copyright (c) 2017 Daifuku North America Holding Company. All Rights Reserved.
  
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.controller.aemessenger.process.json;

import com.daifukuamerica.wrxj.log.Logger;
import com.google.gson.Gson;

/**
 * Abstract response message
 * 
 * @author mandrus
 */
public abstract class AbstractJsonAeMessageResponse
{
  /**
   * Constructor
   */
  public AbstractJsonAeMessageResponse()
  {
  }

  /**
   * Get the message type
   * @return
   */
  public abstract String getMessageType();

  /**
   * Get the response message
   * 
   * @return
   */
  public byte[] getBytes()
  {
    return getBytes(null);
  }
  
  /**
   * Get the response message and log it
   * 
   * @param ipLogger
   * @return
   */
  public byte[] getBytes(Logger ipLogger)
  {
    String vsResponse = toJsonString();
    if (ipLogger != null)
    {
      // Not the best place for this, but the alternative is to parse the message
      // back out after sending it
      ipLogger.logTxEquipmentMessage(vsResponse, this.getMessageType());
    }
    return vsResponse.getBytes();
  }

  /**
   * Get the response message (no logging)
   * 
   * @param ipLogger
   * @return
   */
  public String toJsonString()
  {
    return new Gson().toJson(this);
  }
}
