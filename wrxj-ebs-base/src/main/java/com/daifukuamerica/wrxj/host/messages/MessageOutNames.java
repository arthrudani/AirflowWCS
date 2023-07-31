
package com.daifukuamerica.wrxj.host.messages;

/****************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright ? 2005 Daifuku America Corporation  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND
  TREATIES. NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED
  COPIED, DISTRIBUTED, REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED,
  EXPANDED, COLLECTED, COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED
  WITHOUT THE PRIOR WRITTEN CONSENT OF Daifuku America Corporation ANY
  USE OR EXPLOITATION OF THIS WORK WITHOUT AUTHORIZATION COULD SUBJECT THE
  PERPETRATOR TO CRIMINAL AND CIVIL LIABILITY.
 ****************************************************************************/
import com.daifukuamerica.wrxj.host.MessageNameEnum;
import java.util.EnumSet;
import java.util.Set;

/**
 *  Enumeration to define outbound message name space.
 *  
 *  @author   A.D.    06-Mar-06      Original Version
 *  @version  1.0
 */
public enum MessageOutNames implements MessageNameEnum
{
 /** Value for no message names being defined. */
  NONE("None"),
  
 /** Heart Beat Message identifier. */
  HEART_BEAT("HeartBeat"),

 /** Device Status Message identifier   */
  DEVICE_STATUS("DeviceStatusMessage"),
  
 /** Expected Receipt Complete Message identifier. */
  EXPECTED_RECEIPT_COMPLETE("ExpectedReceiptCompleteMessage"),
  
  /** Expected Receipt Ack Message identifier. */
  EXPECTED_RECEIPT_ACK("ExpectedReceiptAckMessage"),
 
 /**Host HostError Message identifier.
   */
  HOST_ERROR("ErrorMessage"),

 /** Load Arrival Message identifier.     */
  LOAD_ARRIVAL("LoadArrivalMessage"),

 /** Location Arrival Message identifier. */
  LOCATION_ARRIVAL("LocationArrivalMessage"),

 /** Inventory Adjust Message identifier. */
  INVENTORY_ADJUST("InventoryAdjustmentMessage"),

 /** Inventory Status Message identifier. */
  INVENTORY_STATUS("InventoryStatusMessage"),

 /** Inventory Upload Message identifier. */
  INVENTORY_UPLOAD("InventoryUploadMessage"),

 /** Order Complete Message identifier.   */
  ORDER_COMPLETE("OrderCompleteMessage"),
  
  /** Inventory Request By Flight Message identifier.   */
  INVENTORY_REQUEST_BY_FLIGHT("InventoryReqByFlightMessage"),
  
  INVENTORY_REQUEST_BY_FLIGHT_ACK("InventoryReqByFlightAckMessage"),

 /** Order Status Message identifier.     */
  ORDER_STATUS("OrderStatusMessage"),

 /** Store Complete Message identifier. (Mainly for RTS compatibility). */
  STORE_COMPLETE("StoreCompleteMessage"),

 /** Ship Complete Message identifier.    */
  SHIP_COMPLETE("ShipCompleteMessage"),

 /** Pick Complete Message identifier. (Mainly used for ASRS only customers.)*/
  PICK_COMPLETE("PickCompleteMessage"),
  
  /* Flight data update response message */
  FLIGHT_DATA_UPDATE("FlightDataUpdateMessage"),
	
  /** Inventory Update Response Message **/
  INVENTORY_UPDATE("InventoryUpdateMessage"),
	
  /**Item Released Message **/
  ITEM_RELEASE("EBSItemReleaseMessage"), 
  
  ITEMS_ORDER_COMPLETE("ItemOrderCompleteMessage"),
	
  /** Inventory Request By Warehouse **/ 
  INVENTORY_REQUEST_BY_WAREHOUSE("InventoryReqByWarehouseMessage"),
  /** Inventory Request Ack By Warehouse **/ 
  INVENTORY_REQUEST_BY_WAREHOUSE_ACK("InventoryReqByWarehouseAckMessage"),
  
  RETRIEVAL_FLIGHT_REQUEST_ACK("RetrievalOrderAckMessage"),
  
  RETRIEVAL_ITEM_REQUEST_ACK("RetrievalItemAckMessage"),

  ORDER_RESPONSE("OrderResponseMessage");



  private final String msMessageName;
  
  MessageOutNames()
  {
    msMessageName = "";
  }
  
  MessageOutNames(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  public String getValue()
  {
    return(msMessageName);
  }

 /**
  *  Method gets the fully qualified message name as defined in the Host
  *  Message Specification.
  *  
  *  @return returns String containing qualified message name.
  */
  public String getQualifiedName()
  {
    return(msMessageName + "Message");
  }
  
 /**
  *  Method to get correct enum. reference if the constant value is provided.
  * @param isConstantValue the value of the enum constant.
  * @return reference to this object.
  */
  public static MessageOutNames getEnumObject(String isConstantValue)
  {
    MessageOutNames vpTypeRef = null;
    
    for(MessageOutNames vpType : values())
    {
      if (vpType.getValue().equalsIgnoreCase(isConstantValue))
      {
        vpTypeRef = vpType;
        break;
      }
    }
    
    return(vpTypeRef);
  }
  
 /**
  * Method returns all defined outbound message names.
  * @return String[] of message names.
  */
  public static String[] getNames()
  {
    Set<MessageOutNames> vpNameSet = EnumSet.range(MessageOutNames.EXPECTED_RECEIPT_COMPLETE,MessageOutNames.INVENTORY_UPDATE);
    String[] vpNames = new String[vpNameSet.size()];
    int k = 0;
    
    for(MessageOutNames vpMessageName : vpNameSet)
    {
      vpNames[k++] = vpMessageName.getValue();
    }
    
    return(vpNames);
  }
}
