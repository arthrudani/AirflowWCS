package com.daifukuamerica.wrxj.device.agv.messages;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 *  Enumeration to define name space for Webb CMS/AGV Messages.
 *
 *  @author   A.D.
 *  @version  1.0
 *  @since    11-May-2009
 */
public enum AGVMessageNameEnum
{
/*===========================================================================
 *                           WRx-J --> CMS
 *===========================================================================*/
  /** Cancel previous move request */
  CAN_REQUEST("CAN"),

  /** Change a move request. */
  CHG_REQUEST("CHG"),

  /** Heart Beat message to validate connectivity. */
  HBT_REQUEST("HBT"),

  /** Send vehicles to Hold Stations.  All future move requests to CMS rejected. */
  HLD_REQUEST("HLD"),

  /** Move a load from one station to another.  */
  MOV_REQUEST("MOV"),

  /** Move two loads from multiple pick-up to multiple drop-off stations.
      -- <b>Future implementation</b> */
  MV2_REQUEST("MV2"),

  /** CMS pick-up cycle Enable/Disable. */
  PIC_REQUEST("PIC"),

  /** Reset/Clear CMS move buffer of any queued requests */
  RES_REQUEST("RES"),

  /** Release Vehicles from Hold stations (opposite of HLD_REQUEST command) */
  RSU_REQUEST("RSU"),
  
  /** Request System status report.  The specific report type is indicated using 
      a three character acronym ALM, QMR, SSR, or VSR in the message. */
  XMT_REQUEST("XMT"),

/*===========================================================================
 *                           WRx-J <-- CMS
 *===========================================================================*/
  /** Response to XMT_REQUEST request. -- <b>Future implementation</b> */
  ALM_RESPONSE("ALM"),

  /** End of report initiated by XMT_REQUEST request. */
  END_RESPONSE("END"),

  /** Response indicating move request is complete and load is now at requested
      location. */
  LAL_RESPONSE("LAL"),

  /** Response indicating load has been picked up.  */
  LPC_RESPONSE("LPC"),

  /** Response indicating a move request has been aborted due to some condition
      on the CMS system. If a move is aborted in this way, it must be manually
      completed by the operator, and a logical recovery performed using a
      screen on Wrx. */
  MAB_RESPONSE("MAB"),

  /** Direct response to CHG_REQUEST or CAN_REQUEST from WRx. <b>Note: </b>
      a "Request Type" field indicates which request this message is in respose to. */
  MRC_RESPONSE("MRC"),

  /** Direct response to XMT_REQUEST from Wrx with the QMR option in
      the message. -- <b>Future implementation</b> */
  QMR_RESPONSE("QMR"),

  /** Direct response to XMT_REQUEST from Wrx with the SSR option in
      the message. */
  SSR_RESPONSE("SSR"),

  /** Direct response to XMT_REQUEST from Wrx with the VSR option in
      the message. -- <b>Future implementation</b> */
  VSR_RESPONSE("VSR"),

/*===========================================================================
 *                           WRx-J <--> CMS
 *===========================================================================*/
  /** Acknowledge message for each message exchanged.  <b>Note:</b> this has
      nothing to do with the ascii ACK character!  */
  ACK_REQUEST_RESPONSE("ACK"),

  /** Previously acknowledged message was later found to have bad data.  This
      error could be reported as a result of deeper data validation than is
      afforded for an ACK message. */
  ERR_REQUEST_RESPONSE("ERR"),

  /** Startup message initiated by Wrx to synchronize operational data between
      two systems. */
  LSS_REQUEST_RESPONSE("LSS"),

  /** Negative Acknowledge message for each message that fails to process on
      the other side.  <b>Note:</b> this has nothing to do with the ascii NAK
      character! */
  NAK_REQUEST_RESPONSE("NAK");

  private final String msMessageName;

  AGVMessageNameEnum()
  {
    msMessageName = "";
  }

  AGVMessageNameEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  public String getValue()
  {
    return(msMessageName);
  }

 /**
  *  Method to get correct enum. reference if the constant value is provided.
  * @param isConstantValue the value of the enum constant.
  * @return reference to this object. {@code null} if no valid reference found.
  */
  public static AGVMessageNameEnum getEnumObject(String isConstantValue)
  {
    AGVMessageNameEnum vpTypeRef = null;

    for(AGVMessageNameEnum vpType : values())
    {
      if (vpType.getValue().equalsIgnoreCase(isConstantValue))
      {
        vpTypeRef = vpType;
        break;
      }
    }

    return(vpTypeRef);
  }

  public static String[] getInboundMessageNames()
  {
    Set<AGVMessageNameEnum> vpBidirectNameSet = EnumSet.range(AGVMessageNameEnum.ACK_REQUEST_RESPONSE,
                                                   AGVMessageNameEnum.NAK_REQUEST_RESPONSE);
    Set<AGVMessageNameEnum> vpNameSet = EnumSet.range(AGVMessageNameEnum.ALM_RESPONSE,
                                                   AGVMessageNameEnum.VSR_RESPONSE);
    int vnIdx = 0;
    String[] vasNames = new String[vpBidirectNameSet.size()+vpNameSet.size()];
    for (AGVMessageNameEnum vpName : vpBidirectNameSet)
    {
      vasNames[vnIdx++] = vpName.getValue();
    }

    for (AGVMessageNameEnum vpName : vpNameSet)
    {
      vasNames[vnIdx++] = vpName.getValue();
    }

    Arrays.sort(vasNames);
    return(vasNames);
  }

  public static String[] getOutboundMessageNames()
  {
    Set<AGVMessageNameEnum> vpBidirectNameSet = EnumSet.range(AGVMessageNameEnum.ACK_REQUEST_RESPONSE,
                                                   AGVMessageNameEnum.NAK_REQUEST_RESPONSE);
    Set<AGVMessageNameEnum> vpNameSet = EnumSet.range(AGVMessageNameEnum.CAN_REQUEST,
                                                   AGVMessageNameEnum.XMT_REQUEST);

    int vnIdx = 0;
    String[] vasNames = new String[vpBidirectNameSet.size()+vpNameSet.size()];
    for (AGVMessageNameEnum vpName : vpBidirectNameSet)
    {
      vasNames[vnIdx++] = vpName.getValue();
    }

    for (AGVMessageNameEnum vpName : vpNameSet)
    {
      vasNames[vnIdx++] = vpName.getValue();
    }

    Arrays.sort(vasNames);
    return(vasNames);
  }
}
