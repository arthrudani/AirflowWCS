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

/**
 * Interface for inbound JSON AE Messages
 * 
 * @author mandrus
 */
public interface JsonAeMessageRequest
{
  /**
   * Get the message type for this message
   * 
   * @return
   */
  String getMessageType();
  
  /**
   * Process this message
   * 
   * @param ipLogger - Logger to use when processing
   */
  byte[] process(Logger ipLogger);
}
