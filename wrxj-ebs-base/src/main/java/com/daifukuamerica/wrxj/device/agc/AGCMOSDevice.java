package com.daifukuamerica.wrxj.device.agc;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.controller.status.StatusModel;
import com.daifukuamerica.wrxj.device.monitor.SystemHealthMonitor;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.io.PropertyReader;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.messageformat.controlevent.ControlEventDataFormat;
import com.daifukuamerica.wrxj.timer.RestartableTimerTask;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class AGCMOSDevice extends Controller
{
  protected AGCMOSMessage mpAgcMOSMessage = Factory.create(AGCMOSMessage.class);
  private List<AGCMOSMessage> mpStatusRequestMessages = new ArrayList<AGCMOSMessage>();
  private int mnEquipmentPortStatus = ControllerConsts.STATUS_UNKNOWN;
  
  private int mnStatusPollInterval = 1000;
  private StatusPollIntervalTimeout mpStatusPollIntervalTimeout = new StatusPollIntervalTimeout();
  private boolean mzStatusPollingActive = false;
  private boolean mzPollForTrackingQuantity = true;
  private boolean mzEquipmentStarted = false;
  private int mnPollCounter = -1;
  private List<String> mpPollingUsers = new ArrayList<String>();
  
  private String msLastSentBarCode = null;
  private Map<String,String> mpStationMap = null;
  private boolean mzHidePollingResults = false;

  /**
   * Public constructor for Factory
   */
  public AGCMOSDevice()
  {
  }

  /*========================================================================*/
  /*  Controller initialize, startup, shutdown, etc.                        */
  /*========================================================================*/

  /**
   * @see com.daifukuamerica.wrxj.controller.Controller#initialize(java.lang.String)
   */
  @Override
  public void initialize(String aControllerKeyName)
  {
    super.initialize(aControllerKeyName);
    
    logger.logDebug(getClass().getSimpleName() + ".initialize() - Start");
    
    logger.addEquipmentLogger();
    subscribePortStatusEvent();
    subscribeEquipmentEvent();
    subscribeControlEvent();
    mzHidePollingResults = getConfigPropertyAsBoolean("HidePollingResults");

    logger.logDebug(getClass().getSimpleName() + ".initialize() - End");
  }

  /**
   * @see com.daifukuamerica.wrxj.controller.Controller#startup()
   */
  @Override
  public void startup()
  {
    super.startup();
    logger.logDebug(getClass().getSimpleName() + ".startup() - Start");
    /*
     * Request a status update from our port in case it were already up and we
     * missed its status reports.
     */
    publishRequestEvent(equipmentPortCKN);
    
    /*
     * Polling interval
     */
    int vnPollInterval = getConfigPropertyAsInt("StatusPollInterval");
    if (vnPollInterval > 0)
    {
      mnStatusPollInterval = vnPollInterval;
    }
    /*
     * If tracking is published more often than status changes are published, we
     * end up with duplicates. This method is to provide some visibility for the
     * status change interval to the MOS controllers to prevent that problem.
     */
    int vnChangeInterval = SystemHealthMonitor.getStatusChangesInterval();
    if (mnStatusPollInterval < vnChangeInterval)
    {
      mnStatusPollInterval = vnChangeInterval;
      logger.logDebug(
          "StatusPollInterval cannot be less than SystemMonitorChangeInterval");
    }
    
    /*
     * Auto-Poll on start-up
     */
    String vsAutoPoll = getConfigProperty("AutoStatusPoll");
    if (vsAutoPoll != null)
    {
      vsAutoPoll = vsAutoPoll.substring(0,1);
      mzStatusPollingActive = ((vsAutoPoll.equalsIgnoreCase("Y")) || (vsAutoPoll.equalsIgnoreCase("T")));
    }
    
    /*
     * MOS number
     */
    int vnMosNumber = getConfigPropertyAsInt("MosNumber",1);
    mpAgcMOSMessage.setMosNumber(vnMosNumber);
    
    /*
     * Transporter List
     */
    String vsDeviceList = getConfigProperty("TransporterList");
    processTransporterList(vsDeviceList, vnMosNumber);
    
    setControllerStatus(ControllerConsts.STATUS_WAIT_PORT);
    
    // Properties reading can create a database connection.  Close it. 
    disconnectDB();
    logger.logDebug(getClass().getSimpleName() + ".startup() - End");
  }

  /**
   * Process the TransportList parameter. Each transporter in the list will be
   * queried for status. Generally speaking, we seem to have a single
   * transporter ("103000").
   * 
   * @param isDeviceList
   * @param inMosNumber
   */
  private void processTransporterList(String isDeviceList, int inMosNumber)
  {
    // If one is not defined, use the default
    if (isDeviceList == null)
    {
      isDeviceList = "103000";
    }
    
    StringTokenizer st = new StringTokenizer(isDeviceList, ",");
    while (st.hasMoreTokens())
    {
      String sEquipment = st.nextToken();
      try
      {
        String div = sEquipment.substring(0, 1);
        String mdl = sEquipment.substring(1, 3);
        String num = sEquipment.substring(3, 6);

        boolean vzFound = false;
        for (AGCMOSMessage vpStatusRequestMsg : mpStatusRequestMessages)
        {
          //
          // All equipment with the same model code can be in one message.
          //
          if (mdl.equals(vpStatusRequestMsg.getModelCode()))
          {
            vpStatusRequestMsg.addEquipment(div, mdl, num);
            vzFound = true;
          }
        }
        if (!vzFound)
        {
          AGCMOSMessage vpStatusRequestMsg = new AGCMOSMessage();
          vpStatusRequestMsg.setModelCode(AGCMOSMessage.AGC_MODEL_CODE);
          vpStatusRequestMsg.setMosNumber(inMosNumber);
          vpStatusRequestMsg.addEquipment(div, mdl, num);
          mpStatusRequestMessages.add(vpStatusRequestMsg);
        }
      }
      catch (Exception e)
      {
        logger.logError("INVALID Equipment \"" + sEquipment
            + "\" - startup()");
        continue;
      }
    }
  }
  
  /**
   * Shuts down this controller by cancelling any timers and shutting down the
   * Device/Equipment.
   * 
   * @see com.daifukuamerica.wrxj.controller.Controller#shutdown()
   */
  @Override
  public void shutdown()
  {
    logger.logDebug(getClass().getSimpleName() + ".shutdown() -- Start");
    timers.cancel(mpStatusPollIntervalTimeout);
    mpStatusPollIntervalTimeout = null;
    mpAgcMOSMessage = null;
    mpStatusRequestMessages = null;
    logger.logDebug(getClass().getSimpleName() + ".shutdown() -- End");
    super.shutdown();
  }

  /*========================================================================*/
  /*  IPC Messaging                                                         */
  /*========================================================================*/
  
  /**
   * Process System Inter-Process-Communication Message.
   * 
   * @see com.daifukuamerica.wrxj.ipc.AbstractIPCMessenger#processIPCReceivedMessage()
   */
  @Override
  protected void processIPCReceivedMessage()
  {
    super.processIPCReceivedMessage();
    if (!receivedMessageProcessed)
    {
      receivedMessageProcessed = true;
      switch (receivedEventType)
      {
        case MessageEventConsts.EQUIPMENT_EVENT_TYPE:
          processEquipmentEvent();
          break;
        default:
          receivedMessageProcessed = false;
      }
    }
  }

  /**
   * We come here when our Port notifies this Transporter that it has
   * changed status (Connecting, Running, etc.).
   * 
   * @see com.daifukuamerica.wrxj.ipc.AbstractIPCMessenger#processStatusEvent()
   */
  @Override
  protected void processStatusEvent()
  {
    super.processStatusEvent(); // Make sure we let the base class process this.
    try
    {
      char chr0 = receivedText.charAt(0);
      switch (chr0)
      {
        case ControllerConsts.CONTROLLER_STATUS:
          if (mnEquipmentPortStatus == receivedData)
          {
            break;
          }
          
          //
          // Status of our Port has changed.
          //
          logger.logDebug("StatusChange (from Port) - WAS "
              + ControllerConsts.STATUS_TEXT[mnEquipmentPortStatus] + "  NOW "
              + ControllerConsts.STATUS_TEXT[receivedData]);
          mnEquipmentPortStatus = receivedData;
          switch (receivedData)
          {
            case ControllerConsts.STATUS_RUNNING:
              //
              // Our Port is running - we can go online.
              //
              setControllerStatus(ControllerConsts.STATUS_RUNNING);
              initializeConnection();
              if (mzStatusPollingActive)
              {
                startStatusPolling(null);
              }
              break;
            case ControllerConsts.STATUS_ERROR:
              setControllerStatus(ControllerConsts.STATUS_ERROR_PORT);
              setEquipmentStatus();
              if (mzStatusPollingActive)
              {
                stopStatusPolling(null);
              }
              break;
            default:
              if (controllerStatus == ControllerConsts.STATUS_ERROR_PORT)
              {
                setControllerStatus(ControllerConsts.STATUS_WAIT_PORT);
                setEquipmentStatus();
                if (mzStatusPollingActive)
                {
                  stopStatusPolling(null);
                }
              }
              break;
          }
          break;
        default:
          logger.logError(getClass().getSimpleName()
              + ".processStatusEvent() -- UNKNOWN Event Type \"" + chr0
              + "\" -- processStatusEvent()");
      }
    }
    catch(StringIndexOutOfBoundsException e)
    {
      logger.logSparseException(e, getClass().getSimpleName()
          + ".processStatusEvent() - \"" + receivedText + "\"");
    }
  }

  /**
   * We have received a CONTROL EVENT (probably from a User's Form)
   * 
   * @see com.daifukuamerica.wrxj.ipc.AbstractIPCMessenger#processControlEvent()
   */
  @Override
  protected void processControlEvent()
  {
    try
    {
      switch (receivedData)
      {
        case ControlEventDataFormat.MOS_STOP_POLLING:
          stopStatusPolling(receivedCKN);
          break;
        case ControlEventDataFormat.MOS_START_POLLING:
          startStatusPolling(receivedCKN);
          break;
        case ControlEventDataFormat.MOS_COMM_TEST:
          testConnection();
          break;
        case ControlEventDataFormat.MOS_START_AISLE:
          startEquipment();
          break;
        case ControlEventDataFormat.MOS_STOP_AISLE:
          stopEquipment();
          break;
        case ControlEventDataFormat.MOS_RESET_ERROR:
          errorReset();
          break;
        case ControlEventDataFormat.MOS_DELETE_TRACK:
          deleteLoadTracking();
          break;
        case ControlEventDataFormat.MOS_DISCONNECT:
          disconnectEquipment();
          break;
        case ControlEventDataFormat.MOS_RECOVER_DATA:
          recoverEquipment();
          break;
        case ControlEventDataFormat.MOS_SAVE_ALL_LOGS:
          saveLogs();
          break;
        case ControlEventDataFormat.MOS_SILENCE_ERROR:
          alarmReset();
          break;
        case ControlEventDataFormat.MOS_SEND_BAR_CODE:
          sendBarCode();
          break;
        case ControlEventDataFormat.MOS_STATUS_REQUEST:
          sendStatusRequest(receivedText);
          break;
        case ControlEventDataFormat.MOS_START_EQUIP:
          startEquipment(receivedText);
          break;
        case ControlEventDataFormat.MOS_STOP_EQUIP:
          stopEquipment(receivedText);
          break;
        case ControlEventDataFormat.MOS_LATCH_CLEAR:
          latchClear(receivedText);
          break;
        default:
          logger.logError("UNKNOWN Event Type \"" + receivedData + "\" -- "
              + getClass().getSimpleName() + ".processControlEvent()");
          break;
      }
    }
    catch(StringIndexOutOfBoundsException e)
    {
      logger.logSparseException(e, getClass().getSimpleName()
          + ".processControlEvent() - \"" + receivedText + "\"");
    }
  }

  /**
   * We have received a message from the PORT that is connected to the actual
   * Device/Equipment that this Transporter is controlling.
   * 
   * The received data (String) is in "receivedText".
   */
  protected void processEquipmentEvent()
  {
    mpAgcMOSMessage.toDataValues(receivedText);
    if ((mpAgcMOSMessage != null) && (!mpAgcMOSMessage.getValidMessage()))
    {
      String s = mpAgcMOSMessage.getInvalidMessageDescription();
      logger.logRxEquipmentMessage(receivedText, s);
      logger.logError(getClass().getSimpleName()
          + ".processEquipmentEvent() -- " + s);
    }
    else
    {
      String vsParsed = mpAgcMOSMessage.getParsedMessageString();
      int vnMsgId = mpAgcMOSMessage.getID();
      /*
       * We get these two messages during status polling in volume, so don't log
       * them here.
       */
      if (!mzHidePollingResults || (vnMsgId != 102 && vnMsgId != 103))
      {
        logger.logRxEquipmentMessage(receivedText, vsParsed);
        logger.logDebug("Msg ID: " + vsParsed);
      }
      switch (vnMsgId)
      {
        case  101: processEquipmentStatusReport(); break;
        case  102: processTransportDataQuantityReport(); break;
        case  103: processTransportDataReport(); break;
        case  109: processDetailedErrorReport(); break;
        case  121: processStartupStopResponse(); break;
        case  124: processDisconnectionRecoveryDataReport(); break;
        case  125: processDisconnectionRecoveryCommandResponse(); break;
        case  134: processDateAndTimeReport(); break;
        case  141: processTransportDataChangeCommandResponse(); break;
        case  150: processWarningReport(); break;
        case  151: processBarCodeDataResponse(); break;
        case  161: processSystemModeChangeResponse(); break;
        case  166: processLogDataSaveCommandResponse(); break;
        case  184: processTransferHistoryReport(); break;
        case  180: processCommunicationTestReponse(); break;
        case  186: processLoadArrivalReport(); break;
        default:
          logger.logError("Msg ID: " + mpAgcMOSMessage.getID()
              + " NOT Processed - " + getClass().getSimpleName()
              + ".processEquipmentEvent()");
          break;
      }
    }
  }

  /**
   * Publish status changes, if any.
   */
  private void setEquipmentStatus()
  {
    mpAgcMOSMessage.setEquipmentStatus();
    if (mpAgcMOSMessage.equipmentStatusChanges())
    {
      publishStatusEvent(mpAgcMOSMessage.getEquipmentStatusMessage());
    }
  }

  /*========================================================================*/
  /* AGC MOS Messaging                                                      */
  /*========================================================================*/
  
  /**
   * Give the data to be transmitted to the Device/Equipment to the Port.
   * 
   * @param ipMessage
   */
  protected void transmitMessageToDevice(AGCMOSMessage ipMessage)
  {
    String vsParsed = ipMessage.getParsedMessageString();
    int vnMessageID = ipMessage.getID();
    if ((((vnMessageID == 2) || (vnMessageID == 3)) && (mnPollCounter == 0))
        || ((vnMessageID != 2) && (vnMessageID != 3)))
    {
      logger.logDebug("Msg ID: " + vsParsed);
    }
    if (ipMessage.getValidMessage())
    {
      if ((((vnMessageID == 2) || (vnMessageID == 3)) && (mnPollCounter == 0 || !mzHidePollingResults))
          || ((vnMessageID != 2) && (vnMessageID != 3)))
      {
        logger.logTxEquipmentMessage(ipMessage.getMessageAsString(), vsParsed);
      }
      publishEquipmentEvent(ipMessage.getMessageAsString(), 0);
    }
    else
    {
      logger.logError(getClass().getSimpleName()
          + ".transmitMessageToDevice() -- "
          + ipMessage.getInvalidMessageDescription() + " -- \""
          + ipMessage.getMessageAsString() + "\"");
    }
  }

  /*========================================================================*/
  /*  AGC MOS Message Processing (MOS -> WRx)                               */
  /*========================================================================*/

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void processEquipmentStatusReport()     // ID 101
  {
    setDetailedControllerStatus("Equipment Status Report (101) - Received");
    if (mpAgcMOSMessage.equipmentStatusChanges())
    {
      publishStatusEvent(mpAgcMOSMessage.getEquipmentStatusReportMessage());
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void processTransportDataQuantityReport()     // ID 102
  {
    if (mnPollCounter == 0)
      setDetailedControllerStatus("Transport Data Qty Rpt (102) - Received");
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void processTransportDataReport()     // ID 103
  {
    if (mnPollCounter == 0)
      setDetailedControllerStatus("Transport Data Report (103) - Received");
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void processDetailedErrorReport()     // ID 109
  {
    setDetailedControllerStatus("Detailed Error Report (109) - "
        + mpAgcMOSMessage.getErrorCode() + " - Received");
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void processLoadArrivalReport()     // ID 186
  {
    setDetailedControllerStatus("Load Arrival Report (186) - Received");
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void processStartupStopResponse()     // ID 121
  {
    setDetailedControllerStatus("Startup/Stop Response (121) - Received");
    logger.logDebug("Need Some Code!!!");
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  protected void processDisconnectionRecoveryDataReport()     // ID 124
  {
    String s = mpAgcMOSMessage.getDisconnectionRecoveryClassificationParsed(
        mpAgcMOSMessage.getDisconnectionRecoveryClassification());
    setDetailedControllerStatus(s
        + " Data Report (124) - Received - Response: "
        + mpAgcMOSMessage.getResponseFlag());
    
    // Respond with a 24 (and auto-delete any tracking)
    if (mpAgcMOSMessage.responseOk())
    {
      setDetailedControllerStatus(s + " Command (025) machineId: "
          + mpAgcMOSMessage.getMachineId());
      
      //
      // Use the data in the just received "124" report to do the actual
      // Disconnect/Recover.
      //
      mpAgcMOSMessage.setProcessClassification(0);
      mpAgcMOSMessage.setDataProcessClassification("0");
      mpAgcMOSMessage.disconnectionRecoveryCommandToString();

      transmitMessageToDevice(mpAgcMOSMessage);
    }
    else
    {
      logger.logError("Invalid " + s + " Command (024)");
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void processDisconnectionRecoveryCommandResponse()     // ID 125
  {
    String s = mpAgcMOSMessage.getDisconnectionRecoveryClassificationParsed(
        mpAgcMOSMessage.getDisconnectionRecoveryClassification());
    setDetailedControllerStatus(s
        + " Command Response (125) - Received - Response: "
        + mpAgcMOSMessage.getResponseFlag());
    
    if (!mpAgcMOSMessage.responseOk())
    {
      logger.logError("Invalid " + s + " Command (025)");
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void processDateAndTimeReport()     // ID 134
  {
    setDetailedControllerStatus("Date/Time Report (134) - Received");
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void processTransportDataChangeCommandResponse()     // ID 141
  {
    setDetailedControllerStatus("Transport Data Change Command Response (141) - Received - Response: "
        + mpAgcMOSMessage.getResponseFlag());
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void processWarningReport()     // ID 150
  {
    setDetailedControllerStatus("Warning Report (150) - Received");
//    logger.logError("Warning Report (150) - Received - " + agcMOSMessage.getMachineId() +
//                    " - " + agcMOSMessage.getErrorCode());
  }
  
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void processBarCodeDataResponse()     // ID 151
  {
    setDetailedControllerStatus("Bar Code Data Response (151) Received " +
         mpAgcMOSMessage.getResponseFlag() + " - \"" + msLastSentBarCode  +
         "\" machineId: " + mpAgcMOSMessage.getMachineId());
    String s = "LoadId \"" + msLastSentBarCode  + "\" machineId: " +
         mpAgcMOSMessage.getMachineId() + " - BarCodeDataResponse (151) " +
         mpAgcMOSMessage.getResponseFlag() + " Received";
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
  }
  
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void processSystemModeChangeResponse()     // ID 161
  {
    setDetailedControllerStatus("System Mode Change Response (161) - Received");
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void processLogDataSaveCommandResponse()     // ID 166
  {
    setDetailedControllerStatus("Log Data Save Response (166) - Received");
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void processTransferHistoryReport()     // ID 184
  {
    setDetailedControllerStatus("Transfer History Report (184) - Received");
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void processCommunicationTestReponse()     // ID 180
  {
    logger.logDebug("   180 - processCommunicationTestReponse()");
    if (mpAgcMOSMessage.getCommunicationTestResult())
    {
      setDetailedControllerStatus("Communication Test - OK");
      logger.logOperation(LogConsts.OPR_DEVICE, "Communication Test (180) - OK");
    }
    else
    {
      setDetailedControllerStatus("Communication Test - *FAIL*");
      logger.logOperation(LogConsts.OPR_DEVICE, "Communication Test (180) - *FAIL*");
    }
  }

  /*========================================================================*/
  /*  AGC MOS Message Processing (WRx -> MOS)                               */
  /*========================================================================*/

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void initializeConnection()
  {
    setDetailedControllerStatus("Initializing Connection (019)");
 
    // Reseting ModelCode on re-initialization fixes comms problem
    mpAgcMOSMessage.setMsgModelCode(AGCMOSMessage.AGC_MODEL_CODE);

    mpAgcMOSMessage.setDateTimeCorrectionClassification(AGCMOSMessage.DATE_TIME_REQUEST);
    mpAgcMOSMessage.dateAndTimeCorrectionRequestToString();
    transmitMessageToDevice(mpAgcMOSMessage);

    mpAgcMOSMessage.setDateTimeCorrectionClassification(AGCMOSMessage.SET_DATE_TIME);
    mpAgcMOSMessage.dateAndTimeCorrectionRequestToString();
    transmitMessageToDevice(mpAgcMOSMessage);

//    agcMOSMessage.addEquipmentStatusChangeText(">>========>> initializeConnection()");
    mpAgcMOSMessage.setNeedAllConnectionInformation();
//    agcMOSMessage.addUnneededConnectionInformation(AGCMOSMessage.EQUIPMENT_STATUS_NOT_NEEDED);
    mpAgcMOSMessage.addUnneededConnectionInformation(AGCMOSMessage.MESSAGE_DATA_NOT_NEEDED);
    mpAgcMOSMessage.addUnneededConnectionInformation(AGCMOSMessage.WARNING_REPORT_NOT_NEEDED);
    mpAgcMOSMessage.addUnneededConnectionInformation(AGCMOSMessage.PARK_POSITION_CHANGE_NOT_NEEDED);
    mpAgcMOSMessage.addUnneededConnectionInformation(AGCMOSMessage.TRANSFER_HISTORY_NOT_NEEDED);
    mpAgcMOSMessage.connectionInformationToString();
    transmitMessageToDevice(mpAgcMOSMessage);
    sendRequestEquipmentStatus();
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void startEquipment()
  {
    setDetailedControllerStatus("Starting Equipment (021)");
//    agcMOSMessage.addEquipmentStatusChangeText(">>========>> Starting Equipment");
    mpAgcMOSMessage.setStartupStopAllEquipment(true);
    mpAgcMOSMessage.setModelCode(AGCMOSMessage.SV_MODEL_CODE);
    mpAgcMOSMessage.startupStopCommandToString();
    //
    transmitMessageToDevice(mpAgcMOSMessage);
    //
    mpAgcMOSMessage.setSystemMode(AGCMOSMessage.SYSTEM_ONLINE_MODE); // ID 061
    mpAgcMOSMessage.setModelCode(AGCMOSMessage.SV_MODEL_CODE);
    mpAgcMOSMessage.systemModeChangeRequestToString();
    //
    transmitMessageToDevice(mpAgcMOSMessage);
    mzEquipmentStarted = true;
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void stopEquipment()
  {
    setDetailedControllerStatus("Stopping Equipment (021)");
//    agcMOSMessage.addEquipmentStatusChangeText(">>========>> Stopping Equipment");
    mpAgcMOSMessage.setStartupStopAllEquipment(false);
    mpAgcMOSMessage.setModelCode(AGCMOSMessage.SV_MODEL_CODE);
    mpAgcMOSMessage.startupStopCommandToString();
    //
    transmitMessageToDevice(mpAgcMOSMessage);
    mzEquipmentStarted = (!mzEquipmentStarted);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void startEquipment(String isMOSID)
  {
    setDetailedControllerStatus("Starting Equipment (021)");
    mpAgcMOSMessage.setStartupStopAllEquipment(true);
    mpAgcMOSMessage.setModelCode(AGCMOSMessage.SV_MODEL_CODE);
    mpAgcMOSMessage.startupStopCommandToString(isMOSID);
    //
    transmitMessageToDevice(mpAgcMOSMessage);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void stopEquipment(String isMOSID)
  {
    setDetailedControllerStatus("Stopping Equipment (021)");
    mpAgcMOSMessage.setStartupStopAllEquipment(false);
    mpAgcMOSMessage.setModelCode(AGCMOSMessage.SV_MODEL_CODE);
    mpAgcMOSMessage.startupStopCommandToString(isMOSID);
    //
    transmitMessageToDevice(mpAgcMOSMessage);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void alarmReset()
  {
    setDetailedControllerStatus("Alarm Reset (022)");
    mpAgcMOSMessage.setModelCode(AGCMOSMessage.SV_MODEL_CODE);
    mpAgcMOSMessage.setAllEquipment();
    mpAgcMOSMessage.alarmResetCommandToString();
    //
    transmitMessageToDevice(mpAgcMOSMessage);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void errorReset()
  {
    setDetailedControllerStatus("Error Reset Command (023)");
    mpAgcMOSMessage.setModelCode(AGCMOSMessage.SV_MODEL_CODE);
    mpAgcMOSMessage.setAllEquipment();
    mpAgcMOSMessage.errorResetCommandToString();
    //
    transmitMessageToDevice(mpAgcMOSMessage);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void disconnectEquipment()
  {
    StringTokenizer st = new StringTokenizer(receivedText);
    String machineId = st.nextToken();
    setDetailedControllerStatus("Disconnect Data Request (024) machineId: " + machineId);
    mpAgcMOSMessage.setMachineId(machineId);
    String mCode = machineId.substring(1,3);
    mpAgcMOSMessage.setModelCode(mCode);
    mpAgcMOSMessage.setDisconnectionRecoveryClassification(AGCMOSMessage.DISCONNECTION_CLASSIFICATION);
    mpAgcMOSMessage.disconnectionRecoveryDataRequestToString();

    transmitMessageToDevice(mpAgcMOSMessage);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void recoverEquipment()
  {
    StringTokenizer st = new StringTokenizer(receivedText);
    String machineId = st.nextToken();
    setDetailedControllerStatus("Recover Data Request (024) machineId: "
        + machineId);
    mpAgcMOSMessage.setMachineId(machineId);
    String mCode = machineId.substring(1, 3);
    mpAgcMOSMessage.setModelCode(mCode);
    mpAgcMOSMessage.setDisconnectionRecoveryClassification(AGCMOSMessage.RECOVERY_CLASSIFICATION);
    mpAgcMOSMessage.disconnectionRecoveryDataRequestToString();

    transmitMessageToDevice(mpAgcMOSMessage);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void deleteLoadTracking()
  {
    String mcKey = null;
    String machineId = null;
    try
    {
      StringTokenizer st = new StringTokenizer(receivedText);
      st.nextToken(); // Skip off message description text.
      mcKey = st.nextToken();
      machineId = st.nextToken();
      setDetailedControllerStatus("Deleting Load Tracking (041) MC-Key: \""
          + mcKey + "\" machineId: " + machineId);

      mpAgcMOSMessage.setMachineId(machineId);
      mpAgcMOSMessage.setMCKey(mcKey);
      mpAgcMOSMessage.setModelCode(machineId.substring(1, 3));
      mpAgcMOSMessage.transportDataChangeCommandToString();
      transmitMessageToDevice(mpAgcMOSMessage);
    }
    catch (Exception e)
    {
      logger.logException(e, "MC-Key: \"" + mcKey + "\" machineId: "
          + machineId + " - receivedText \"" + receivedText + "\"");
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void latchClear(String isMachineID)
  {
    setDetailedControllerStatus("Latch Clear (049)");
    mpAgcMOSMessage.setMachineId(isMachineID);
    mpAgcMOSMessage.latchClearCommandToString();

    transmitMessageToDevice(mpAgcMOSMessage);
  }
  
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void sendBarCode()
  {
    ControlEventDataFormat vpCEDF = new ControlEventDataFormat("Parse");
    vpCEDF.parseMosBcrCommand(receivedText);
    String machineId = vpCEDF.getMachineID();
    msLastSentBarCode = vpCEDF.getBarCode();

    mpAgcMOSMessage.setModelCode(AGCMOSMessage.AGC_MODEL_CODE);  // Always sent by "AGC"
    mpAgcMOSMessage.setMachineId(machineId);
    mpAgcMOSMessage.setBarCode(msLastSentBarCode);
    mpAgcMOSMessage.barCodeDataCommandToString();

    setDetailedControllerStatus("Bar Code Data Command (051) - \""
        + msLastSentBarCode + "\" machineId: " + machineId);
    String s = "LoadId \"" + msLastSentBarCode + "\" machineId: " + machineId
        + " - BarCodeDataCmd (051) Sent";
    logger.logOperation(LogConsts.OPR_DEVICE, s);

    transmitMessageToDevice(mpAgcMOSMessage);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void saveLogs()
  {
    setDetailedControllerStatus("Save Logs Request (066)");
    StringTokenizer st = new StringTokenizer(receivedText);
    st.nextToken(); // Skip off message description text.
    String logName = st.nextToken();
    mpAgcMOSMessage.setLogName(logName);
    mpAgcMOSMessage.logDataSaveCommandToString();
    //
    transmitMessageToDevice(mpAgcMOSMessage);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void testConnection()
  {
    setDetailedControllerStatus("Communication Test Request (080)");
    logger.logOperation(LogConsts.OPR_DEVICE, "Communication Test Request (080)");
    mpAgcMOSMessage.setCommunicationTestRandomTextRequest();
    mpAgcMOSMessage.communicationTestRequestToString();
    //
    transmitMessageToDevice(mpAgcMOSMessage);
  }

  /*========================================================================*/
  /*  AGC MOS Message Processing (Status Polling)                           */
  /*========================================================================*/
  
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void startStatusPolling(String isUser)
  {
    logger.logDebug("Start Status Polling for " + isUser);
    if ((isUser != null) && (! mpPollingUsers.contains(isUser)))
    {
      mpPollingUsers.add(isUser);
    }
    if ((mpPollingUsers.size() == 1) || 
        ((mpPollingUsers.size() > 0) && (isUser == null)))
    {
      try
      {
        timers.cancel(mpStatusPollIntervalTimeout);
      }
      catch (Exception e)
      {
        logger.logException (e, "startStatusPolling() - statusPollIntervalTimeout.cancel();");
      }
      mpAgcMOSMessage.resetEquipmentTransportDataQuantities();
      //
      // Make sure we start with an empty list of ALL equipment load tracking
      // (Our Map of ALL equipment that currently has tracking).
      //
      mpAgcMOSMessage.clearEquipmentsLoadTrackingLists();
      mzPollForTrackingQuantity = true;
      timers.setPeriodicTimerEvent(mpStatusPollIntervalTimeout, mnStatusPollInterval);
      mzStatusPollingActive = true;
      mnPollCounter = -1;
    }
  }

  /*--------------------------------------------------------------------------*/
  private void stopStatusPolling(String isUser)
  {
    logger.logDebug("Stop Status Polling for " + isUser);
    if (isUser != null)
    {
      mpPollingUsers.remove(isUser);
      if (mpPollingUsers.size() == 0)
      {
        mzStatusPollingActive = false;
      }
    }
    else
    {
      mzStatusPollingActive = false;
    }
    if ((mpPollingUsers.size() == 0) || (isUser == null))
    {
      timers.cancel(mpStatusPollIntervalTimeout);
      mpAgcMOSMessage.resetEquipmentTransportDataQuantities();
      mpAgcMOSMessage.clearEquipmentsLoadTrackingLists();
      mzPollForTrackingQuantity = true;
      sendRequestEquipmentStatus();
      mnPollCounter = -1;
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void sendRequestEquipmentStatus()
  {
    if (mzPollForTrackingQuantity)
    {
      mnPollCounter++;
      if (mnPollCounter >= 59)
        mnPollCounter = -1;
      //
      // First, publish any changes from the last polling cycle.
      //
      if (mpAgcMOSMessage.transportDataChanges())
      {
        String trackingStatus = mpAgcMOSMessage.getStatusMessageForTransportDataReport();
        if (trackingStatus != null)
        {
          publishStatusEvent(trackingStatus);
        }
      }
      Iterator<AGCMOSMessage> messages = mpStatusRequestMessages.iterator();
      while (messages.hasNext())
      {
        AGCMOSMessage statusRequestMsg = messages.next();
        statusRequestMsg.transportDataQuantityRequestToString();
        transmitMessageToDevice(statusRequestMsg);
      }
    }
    else
    {
      //
      // Now, use any quantity changes to request the actual tracking data.
      //
      Iterator<String> equipmentChangeKeys = mpAgcMOSMessage.getEquipmentTransportKeys();
      while (equipmentChangeKeys.hasNext())
      {
        String s = equipmentChangeKeys.next();
        mpAgcMOSMessage.setTransportDataEquipment(s);
        mpAgcMOSMessage.transportDataRequestToString();
        transmitMessageToDevice(mpAgcMOSMessage);
      }
    }
    mzPollForTrackingQuantity = (!mzPollForTrackingQuantity);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Send a status request for a station.  If our StatusModel didn't suck, we 
   * wouldn't need this.
   * 
   * @param isStationID
   */
  private void sendStatusRequest(String isStationID)
  {
    String vsMachine = getMachineID(isStationID);
    if (vsMachine != null)
    {
      mpAgcMOSMessage.setEquipment(//vsMachine.substring(0,1),
          vsMachine.substring(1,3), vsMachine.substring(3));
      mpAgcMOSMessage.equipmentStatusRequestToString();

      String s = "Request Status (001) - " + isStationID;
      setDetailedControllerStatus(s);
      logger.logOperation(LogConsts.OPR_DEVICE, s);

      transmitMessageToDevice(mpAgcMOSMessage);
    }
  }
  
  /**
   * Get the MOS machine ID for a station
   * 
   * @param isStationID
   * @return
   */
  private String getMachineID(String isStationID)
  {
    if (mpStationMap == null)
    {
      initializeStationMap();
    }
    return mpStationMap.get(isStationID);
  }

  /**
   * Initialize the station->machineID mapping
   */
  private void initializeStationMap()
  {
    mpStationMap = new TreeMap<String,String>();

    try
    {
      PropertyReader vpReader = PropertyReader.newInstance();
      List<List<String>> vpEquipPropList = vpReader.getAllPropertyCollections(
          Application.getString(StatusModel.EQUIPMENT_CONFIGURATION_KEY), 
          StatusModel.EQUIPMENT_NAME);
      for (List<String> vpEq : vpEquipPropList)
      {
        String vsMOSCtlr = vpReader.getProperty(vpEq, StatusModel.MOS_CONTROLLER);
        if (vsMOSCtlr != null && vsMOSCtlr.equals(getName()))
        {
          String vsMOSID = vpReader.getProperty(vpEq, StatusModel.MOS_ID);
          String vsStation = vpReader.getProperty(vpEq, StatusModel.STATION_ID);
          if (vsMOSID != null && !vsMOSID.equals("*NONE*") && 
              vsStation != null && !vsStation.equals("*NONE*") &&
              !vsStation.startsWith("9"))
          {
            mpStationMap.put(vsStation, vsMOSID);
          }
        }
      }
    }
    catch (Exception e)
    {
      logger.logException(e);
    }
  }
  
  /*--------------------------------------------------------------------------*/
  private class StatusPollIntervalTimeout extends RestartableTimerTask
  {
    /**
     * run -- the LocalTimerTask's run() needs to be synchronized on thisController
     * so that any work we do here is not interrupted by any incoming messages
     * or events that we generate here.  We want to complete anything we do here
     * without being pre-empted.
     */
    @Override
    public void run()
    {
      synchronized(AGCMOSDevice.this)
      {
        sendRequestEquipmentStatus();
     }
    }
  }

  /**
   * Factory for ControllerImplFactory.
   *
   * <p><b>Details:</b> <code>create</code> is a factory method used exclusively
   * by <code>ControllerImplFactory</code>.  Configurable properties of a new
   * controller created using this method are initialized using data in the
   * supplied properties object.  If the controller cannot be created, a
   * <code>ControllerCreationException</code> is thrown.</p>
   *
   * @param ipConfig configurable property definitions
   * @return the created controller
   * @throws ControllerCreationException if an error occurred while creating the controller
   */
  public static Controller create(ReadOnlyProperties ipConfig) throws ControllerCreationException
  {
    Controller vpDev = Factory.create(AGCMOSDevice.class);
    vpDev.setEquipmentPortCKN(ipConfig.getString(DEVICE_PORT));
    return vpDev;
  }
}
