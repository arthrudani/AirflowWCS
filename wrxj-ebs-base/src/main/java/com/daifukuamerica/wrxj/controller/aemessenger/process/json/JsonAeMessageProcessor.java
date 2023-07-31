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

import com.daifukuamerica.wrxj.controller.aemessenger.AEMessage;
import com.daifukuamerica.wrxj.controller.aemessenger.AbstractAEMessageProcessor;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Processes AE Messages.  Finds the correct processor class via reflection.
 * All messages must have a messageType field.  The corresponding class must be 
 * com.daifukuamerica.wrxj.custom.flna.ges.controller.aemessenger.process.json.[messageType]Request
 * and must NOT clean-up any servers in their process() method.
 * 
 * The GES version of this class includes progress indication via the Progress 
 * table.  That functionality is not included here.
 * 
 * @author mandrus
 */
public class JsonAeMessageProcessor extends AbstractAEMessageProcessor
{
  /**
   * Constructor
   * 
   * @param isProduct
   */
  public JsonAeMessageProcessor(String isProduct)
  {
    super(isProduct);
  }

  /**
   * Process a message
   */
  @SuppressWarnings("unchecked")
  @Override
  public byte[] process(AEMessage ipMessage)
  {
    long start = System.currentTimeMillis();
    
    String vsMsgType = null;
    byte[] vabResponse = null;
    
    try
    {
      // Extract the message type
      JsonObject vpJsonObj = new JsonParser().parse(ipMessage.getMessageDataAsString()).getAsJsonObject();
      JsonElement vpMsgType;
      if ((vpMsgType = vpJsonObj.get("messageType")) == null
          && (vpMsgType = vpJsonObj.get("MessageType")) == null)
      {
        throw new JsonSyntaxException("MessageType is required!");
      }
      
      // Get the implementing class
      vsMsgType = vpMsgType.getAsString();
      String vsMsgTypeClassName = JsonAeMessageRequest.class.getPackage().getName() + "." + vsMsgType + "Request";
      Class<? extends JsonAeMessageRequest> vcMsgTypeClass = (Class<? extends JsonAeMessageRequest>)Class.forName(vsMsgTypeClassName);

      // Parse the message
      JsonAeMessageRequest vpRequest = new Gson().fromJson(
          ipMessage.getMessageDataAsString(), vcMsgTypeClass);
      mpLogger.logRxEquipmentMessage(ipMessage.getMessageDataAsString(),
          vpRequest.getMessageType());

      // Process the message
      vabResponse = vpRequest.process(mpLogger);
          
      mpLogger.logOperation("Processed " + vsMsgType + " in " + (System.currentTimeMillis() - start) + " ms");
    }
    catch (ClassNotFoundException cnfe)
    {
      mpLogger.logRxEquipmentMessage(ipMessage.getMessageDataAsString(),
          "Invalid message: \"" + vsMsgType + "\"");
      mpLogger.logError("Unexpected message type \"" + vsMsgType
          + "\" from Sender: " + ipMessage.getSource());
    }
    catch (ClassCastException cce)
    {
      mpLogger.logRxEquipmentMessage(ipMessage.getMessageDataAsString(),
          "Invalid message processor for: \"" + vsMsgType + "\"");
      mpLogger.logError(
          "Bad message processor for \"" + vsMsgType + "\" from Sender: "
              + ipMessage.getSource() + ". (" + cce.getMessage() + ")");
      mpLogger.logException(cce);
    }
    catch (JsonSyntaxException e)
    {
      mpLogger.logRxEquipmentMessage(ipMessage.getMessageDataAsString(),
          "Unparseable message");

      mpLogger.logException(
          "Unexpected message \"" + ipMessage.getMessageDataAsString()
              + "\" from Sender: " + ipMessage.getSource(), e);
    }
    catch (Exception e)
    {
      mpLogger.logException("Error processing " + ipMessage.getMessageDataAsString(), e);
    }
    return vabResponse;
  }
}
