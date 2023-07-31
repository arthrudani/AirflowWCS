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
 * JSON message from Communicator for pre-planning a new trip
 * 
 * @author mandrus
 */
public class SampleJsonAeMessageRequest implements JsonAeMessageRequest
{
  private String   messageType;
  private String   sampleString;
  private String[] sampleStrings;
  private Integer  sampleInt;
  
  

  /**
   * Constructor
   */
  public SampleJsonAeMessageRequest()
  {
    setMessageType("SampleAeMessage");
  }

  /**
   * toString
   */
  @Override
  public String toString()
  {
    return new Gson().toJson(this);
  }
  
  // Getters
  @Override
  public String   getMessageType()      {    return messageType;    }
  public String   getSampleString()     {    return sampleString;   }
  public String[] getSampleStrings()    {    return sampleStrings;  }
  public Integer  getSampleInt()        {    return sampleInt;      }

  // Setters
  public void setMessageType(String messageID)
  {
    this.messageType = messageID;
  }
  public void setSampleString(String sampleString)
  {
    this.sampleString = sampleString;
  }
  public void setSampleStrings(String[] sampleStrings)
  {
    this.sampleStrings = sampleStrings;
  }
  public void setSampleInt(Integer sampleInt)
  {
    this.sampleInt = sampleInt;
  }

  /**
   * Process this message
   */
  @Override
  public byte[] process(Logger ipLogger)
  {
    try
    {
      System.out.println(toString());
    }
    finally
    {
    }
    return null;
  }
}
