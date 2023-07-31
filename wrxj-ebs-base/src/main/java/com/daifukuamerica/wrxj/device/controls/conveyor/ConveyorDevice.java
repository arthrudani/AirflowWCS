package com.daifukuamerica.wrxj.device.controls.conveyor;

/**
 * Title:        WRx-J
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      Daifuku America Corporation
 */

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.device.controls.AbstractControlsDevice;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.log.LogConsts;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author       Stephen Kendorski
 * @version 1.0
 */

/**
 * A Device Controller that operates a PLC (or Think & Do). The PLC monitors
 * the status of its peripheral equipment, and schedules diverts operations
 * requested by the Controls Device Controller.
 */
public class ConveyorDevice extends AbstractControlsDevice
{
  protected List<String> mpDevicesStations = new ArrayList<String>();
  protected ConveyorMessage mpConveyorMessage;  // To avoid constant type-casting

  public ConveyorDevice()
  {
    mpConveyorMessage = new ConveyorMessage();
    mpControlsMessage = mpConveyorMessage;
  }

  /**
   * Initialize the controller
   */
  @Override
  public void initialize(String aControllerKeyName)
  {
    super.initialize(aControllerKeyName);
    String stations = getConfigProperty("Stations");
    if (stations == null)
    {
      logger.logError("Missing Stations Property");
    }
    else
    {
      logger.logDebug("Stations \"" + stations + "\"");
      StringTokenizer st = new StringTokenizer(stations, ",");
      while(st.hasMoreTokens())
      {
        String stationId = st.nextToken();
        mpDevicesStations.add(stationId);
        String msg = mpConveyorMessage.commandToString("ST", "00000000",
            stationId, "Unknown");
        mpConveyorMessage.toDataValues(msg);
      }
    }
  }

  /*========================================================================*/
  /*  The following methods are called in response to messages from the     */
  /*  actual device.                                                        */
  /*========================================================================*/

  /**
   * We have received a message from the PORT that is connected to the actual
   * Device/Equipment that this Transporter is controlling.
   * 
   * <BR>The received data (String) is in global field "receivedText".
   */
  @Override
  public void processEquipmentEvent()
  {
    mpConveyorMessage.toDataValues(receivedText);
    if ((mpConveyorMessage != null) && (!mpConveyorMessage.getValidMessage()))
    {
      String s = mpConveyorMessage.getInvalidMessageDescription();
      logger.logRxEquipmentMessage(receivedText, s);
      logger.logError("processEquipmentEvent() -- " + s);
    }
    else
    {
      String s = mpConveyorMessage.getParsedMessageString();
      logger.logRxEquipmentMessage(receivedText, s);
      logger.logDebug("Msg ID: " + s);
      String msgType = mpConveyorMessage.getType();
      if (msgType.equals("AR"))  // Arrival
      {
        processArrivalReport();
      }
      else if (msgType.equals("ST"))  // Status
      {
        processStatusReport();
      }
      else if (msgType.equals("TM"))  // Test Message Request
      {
        processTestMessageRequest();
      }
      else if (msgType.equals("TR"))  // Test Message Response
      {
        processTestMessageResponse();
      }
      else if (msgType.equals("DC"))  // Divert Complete
      {
        processDivertCompleteReport();
      }
      else
      {
        logger.logError("Msg Type \"" + mpConveyorMessage.getType()
            + "\" NOT Processed - processEquipmentEvent()");
      }
    }
  }
  
  protected void processDivertCompleteReport()
  {
    
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Process an arrival report
   */
  protected void processArrivalReport()          // ID AR
  {
    //
    String arrivalStation = mpConveyorMessage.getStation();
    String loadId = mpConveyorMessage.getData();
    if (mpConveyorMessage.isNoRead(loadId))
    {
      loadId = ConveyorMessage.NO_READ_STRING;  
    }

    String s = "LoadId \"" + loadId  + "\"  - Station: " +
               arrivalStation + " - Arrival Report (AR) Received";
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);

    mpConveyorMessage.setEquipmentLoadId(arrivalStation, loadId);
    publishStatusEvent(mpConveyorMessage.getEquipmentStatusReport());
    setDetailedControllerStatus("Arrival Report (AR) - Processed");
  }
  
  /*--------------------------------------------------------------------------*/
  /**
   * Process a status report
   */
  protected void processStatusReport()           // ID ST
  {
    setDetailedControllerStatus("Status Report (ST) - Received");
    if (mpConveyorMessage.equipmentStatusChanges())
    {
      publishStatusEvent(mpConveyorMessage.getEquipmentStatusReport());
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Process a test message request
   */
  protected void processTestMessageRequest()     // ID TM
  {
    setDetailedControllerStatus("Communications Test Request (TM) - Received");
    mpConveyorMessage.setType("TR");
    transmitMessageToDevice();
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Process a test message response
   */
  protected void processTestMessageResponse()     // ID TR
  {
    if (mpConveyorMessage.getCommunicationTestResult())
    {
      logger.logDebug("Communication Test (TR) - OK");
      setDetailedControllerStatus("Communication Test - OK");
    }
    else
    {
      logger.logDebug("Communication Test (TR) - *FAIL*");
      setDetailedControllerStatus("Communication Test - *FAIL*");
    }
  }

  /*========================================================================*/
  /*  The following methods are normally called in response to user input   */
  /*  on the Equipment Monitor frame.                                       */
  /*========================================================================*/
  
  /**
   * Send Communication Test Request to the device
   * <BR>This is initiated from the Equipment Monitor
   * <BR>This is also called upon connecting to the device
   */
  @Override
  protected void sendCommunicationTestRequest()
  {
    mpConveyorMessage.setCommunicationTestText("TM");
    transmitMessageToDevice();
  }

  /**
   * Send Equipment Status Change
   */
  @Override
  protected void sendEquipmentStatusChange()
  {
    //
    // Get Tokens: Status status Station stationID
    //
    StringTokenizer st = new StringTokenizer(receivedText);
    st.nextToken(); // skip over text label
    String status = st.nextToken(); // ON, OF, RE
    st.nextToken(); // skip over text label
    String stationId = st.nextToken();
    mpConveyorMessage.commandToString("ST", "00000000", stationId, status);
    String sStatus = null;
    if (status.equals(ConveyorMessage.STATUS_ONLINE))
    {
      sStatus = "Online";
    }
    else if (status.equals(ConveyorMessage.STATUS_OFFLINE))
    {
      sStatus = "Offline";
    }
    else if (status.equals(ConveyorMessage.STATUS_RESET))
    {
      sStatus = "Reset";
    }
    String s = "StationId \"" + stationId + "\" - Change status to: " + sStatus
        + " - Equipment Status Change (ST) Sent";
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    transmitMessageToDevice();
   }

  /**
   * Send Equipment Status Report
   */
  @Override
  protected void sendEquipmentStatusReport()
  {
    //
    // Get Tokens: Status status Station stationID
    //
    StringTokenizer st = new StringTokenizer(receivedText);
    st.nextToken(); // skip over text label
    String status = st.nextToken(); // ON, OF, RE
    st.nextToken(); // skip over text label
    String stationId = st.nextToken();
    if (stationId.equals("ALLS"))
    {
      Iterator stations = mpDevicesStations.iterator();
      while (stations.hasNext())
      {
        String station = (String)stations.next();
        String msg = mpConveyorMessage.commandToString("ST", "00000000", station,
            status);
        mpConveyorMessage.toDataValues(msg);
        transmitMessageToDevice();
      }
    }
    else
    {
      String msg = mpConveyorMessage.commandToString("ST", "00000000", stationId,
          status);
      mpConveyorMessage.toDataValues(msg);
      transmitMessageToDevice();
    }
    publishStatusEvent(mpConveyorMessage.getEquipmentStatusReport());
    String sStatus = null;
    if (status.equals(ConveyorMessage.STATUS_ONLINE))
    {
      sStatus = "Online";
    }
    else if (status.equals(ConveyorMessage.STATUS_OFFLINE))
    {
      sStatus = "Offline";
    }
    String s = "StationId \"" + stationId + "\" - Report status of: " + sStatus
        + " - Equipment Status Change Reported";
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
  }
  
  /*--------------------------------------------------------------------------*/
  /**
   * Factory for ControllerImplFactory.
   * 
   * <p><b>Details:</b> <code>create</code> is a factory method used exclusively
   * by <code>ControllerImplFactory</code>. Configurable properties of a new
   * controller created using this method are initialized using data in the
   * supplied properties object. If the controller cannot be created, a
   * <code>ControllerCreationException</code> is thrown.</p>
   * 
   * @param ipConfig configurable property definitions
   * @return the created controller
   * @throws ControllerCreationException if an error occurred while creating the
   *             controller
   */
  public static Controller create(ReadOnlyProperties ipConfig)
      throws ControllerCreationException
  {
    Controller vpController = Factory.create(ConveyorDevice.class);
    vpController.setEquipmentPortCKN(ipConfig.getString(DEVICE_PORT));
    return vpController;
  }
}
