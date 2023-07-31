/* ***************************************************************************
  $Workfile$
  $Date$
  
  Copyright (c) 2017 Daifuku North America Holding Company. All Rights Reserved.
  
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.controller.aemessenger.process;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.controller.aemessenger.AEMessage;
import com.daifukuamerica.wrxj.controller.aemessenger.AEMessageProcessor;

/**
 * Process Global Settings messages from AED
 * 
 * @author mandrus
 */
public class GlobalSettingsMessageProcessor implements AEMessageProcessor
{
  /**
   * Processor for Global Settings messages
   * 
   * @param isProduct
   */
  public GlobalSettingsMessageProcessor(String isProduct)
  {
  }

  /**
   * Process the message
   */
  @Override
  public byte[] process(AEMessage ipMessage)
  {
    // This is a serialized .NET object... just refresh everything.
    // TODO: Limit to Global Settings?
    Application.refreshPropertiesLayers();
    return null;
  }
}
