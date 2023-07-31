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
package com.daifukuamerica.wrxj.controller.aemessenger.tcp;

import com.daifukuamerica.wrxj.controller.aemessenger.AEMessage;
import com.daifukuamerica.wrxj.controller.aemessenger.AEMessageProcessor;
import com.daifukuamerica.wrxj.log.Logger;

/**
   * Client Response Handler
 * 
 * @author mandrus
 */
public class AEMClientResponseHandler implements AEMReadEvent
{
  private String msName;
  private Logger mpLogger;
  private AEMessageProcessor mpProcessor;
  
  /**
   * Client Response Handler
   * 
   * @param isName
   * @param ipProcessor
   * @param ipLogger
   */
  public AEMClientResponseHandler(String isName,
      AEMessageProcessor ipProcessor, Logger ipLogger)
  {
    msName = isName;
    mpProcessor = ipProcessor;
    mpLogger = ipLogger;
  }

  /**
   * Process response messages from this client connection.
   */
  @Override
  public void receivedData(AEMTcpipReaderWriter ipChannel, AEMessage ipMsg)
  {
    try
    {
      mpLogger.logRxEquipmentMessage(ipMsg.getMessageDataAsString(), ipMsg.toString());
      
      // Process the message
      byte[] vabResponse = mpProcessor.process(ipMsg);
      if (vabResponse != null)
      {
        // Send the response if there is one
        ipChannel.sendMessage(ipMsg.getTransactionID(), vabResponse);
      }
    }
    catch (Exception e)
    {
      mpLogger.logException("Error processing " + msName + " response message", e);
    }
  }
}
