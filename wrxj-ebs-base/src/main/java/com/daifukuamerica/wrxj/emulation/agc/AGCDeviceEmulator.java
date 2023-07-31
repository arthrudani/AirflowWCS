package com.daifukuamerica.wrxj.emulation.agc;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.RackLocationParser;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.dbadapter.data.WarehouseData;
import com.daifukuamerica.wrxj.device.agc.AGCDeviceConstants;
import com.daifukuamerica.wrxj.device.agc.AGCMCMessage;
import com.daifukuamerica.wrxj.device.agc.AGCMOSMessage;
import com.daifukuamerica.wrxj.device.agc.AGCMessage;
import com.daifukuamerica.wrxj.emulation.DeviceEmulator;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.messageformat.controlevent.ControlEventDataFormat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

/**
 * A Device Controller that emulates a Daifuku AGC.  The AGC monitors the
 * status of the AS/RS and its peripheral equipment, and schedules transfers
 * operations requested by the AGC Device Controller.
 *
 * @author Stephen Kendorski   (version 1.0)
 * @author Michael Andrus      (version 2.0)
 */
public class AGCDeviceEmulator extends DeviceEmulator
{
  protected static final String TRANSPORT_MODE_NA = "0";
  protected static final String TRANSPORT_MODE_STORE = "1";
  protected static final String TRANSPORT_MODE_RETRIEVE = "2";

  // Equipment Status
  protected static final String NO_ERROR = "0000000";
  protected static final int STATUS_ONLINE = 0;
  protected static final int STATUS_OFFLINE = 1;
  protected static final int STATUS_ERROR = 2;
  protected static final int STATUS_DISCONNECTED = 3;

  // ID 33 Completion Values
  protected static final int COMPLETE_NORMAL = 0;
  protected static final int COMPLETE_BIN_FULL = 1;
  protected static final int COMPLETE_BIN_EMPTY = 2;
  protected static final int COMPLETE_SIZE_MISMATCH = 3;
  protected static final int COMPLETE_CANCEL = 9;

  protected StandardLoadServer mpLoadServer;
  protected StandardLocationServer mpLocServer;
  protected StandardStationServer mpStationServer;

  protected Timer mpTimer;

  protected int storePickupCompletionDelay = 4900;
  protected int storeCompletionDelay = 5900;
  protected int retrieveCompletionDelay = 6000;
  protected int retrieveTriggerOfOperationIndicationDelay = 1400;
  protected int retrieveArrivalReportDelay = 2400;
  private int   mnID33CompletionClass = 0;

  private final int STATUSREPORTSPERMESSAGE = 33;

  protected AGCMCMessage  mpAGCMCMessage  = Factory.create(AGCMCMessage.class);
  protected AGCMOSMessage mpAGCMOSMessage = Factory.create(AGCMOSMessage.class);

  protected boolean mzAGCOnline       = false;
  protected boolean mzEquipmentOnline = false;

  protected boolean mzUsePickupCompletion = false;
  protected boolean mzUseTriggerOfOperation = false;

  protected Map<String,String> mpBidirectionalStatuses = new TreeMap<String,String>();

  /**
   * The unique <i>String</i> key that identifies an instantiated Controller's
   * SECOND communication port.
   */
  private String msEquipmentPortCKN2 = null;

  private int mnMCPortStatus  = ControllerConsts.STATUS_UNKNOWN;
  private int mnMOSPortStatus = ControllerConsts.STATUS_UNKNOWN;

  protected AGCMCMessage mpMachineStatusAGCMCMessage = new AGCMCMessage();

  protected int MAX_EQUIPMENT = 650;
  protected String[] masEquipmentIDs        = new String[MAX_EQUIPMENT];
  protected int[]    manEquipmentModels     = new int[MAX_EQUIPMENT];
  protected String[] masEquipmentNumbers    = new String[MAX_EQUIPMENT];
  protected int      mnEquipmentCount       = 0;

  protected int mnSpeedFactor = 1;

  public AGCDeviceEmulator()  // This has to be public for the Factory to work
  {
  }

  /*========================================================================*/
  /*  Controller methods                                                    */
  /*========================================================================*/

  @Override
  public void initialize(String aControllerKeyName)
  {
    super.initialize(aControllerKeyName);
    logger.logDebug("AGCDeviceEmulator.initialize() - Start");
    logger.addEquipmentLogger();
    msEquipmentPortCKN2 = getConfigProperty(DEVICE_PORT2);
    if (msEquipmentPortCKN2 == null)
    {
      logger.logDebug("Missing DevicePort2 (Device May Not Need One)");
      mnMOSPortStatus = ControllerConsts.STATUS_RUNNING;
    }
    else
    {
      logger.logDebug("DevicePort2 \"" + msEquipmentPortCKN2 + "\"");
    }
    subscribeStatusEvent(equipmentPortCKN);
    subscribeEquipmentEvent(equipmentPortCKN);
    if (msEquipmentPortCKN2 != null)
    {
      subscribeStatusEvent(msEquipmentPortCKN2);
      subscribeEquipmentEvent(msEquipmentPortCKN2);
    }
    subscribeControlEvent();

    logger.logDebug("AGCDeviceEmulator.initialize() - End");
  }

  /*--------------------------------------------------------------------------*/
  @Override
  public void startup()
  {
    super.startup();
    logger.logDebug("AGCDeviceEmulator.startup() - Start");

    mpLoadServer = Factory.create(StandardLoadServer.class, getName());
    mpLocServer = Factory.create(StandardLocationServer.class, getName());
    mpStationServer = Factory.create(StandardStationServer.class, getName());

    mpTimer = new Timer(getClass().getSimpleName());

    //
    // Request a status update from our ports in case they were already up and
    // we missed their status reports.
    //
    publishRequestEvent(equipmentPortCKN);
    if (msEquipmentPortCKN2 != null)
    {
      publishRequestEvent(msEquipmentPortCKN2);
    }
    List<String> vpTranList = getTransporterList();
    if (vpTranList != null)
    {
      mnEquipmentCount = 0;
      for (String vsID : vpTranList)
      {
        try
        {
          String vsModel = vsID.substring(0,2);
          String vsNum = vsID.substring(2,6);
          masEquipmentIDs[mnEquipmentCount] = vsModel + vsNum;
          manEquipmentModels[mnEquipmentCount] = Integer.parseInt(vsModel);
          masEquipmentNumbers[mnEquipmentCount] = vsNum;
        }
        catch (Exception e)
        {
          logger.logError("INVALID Equipment \"" + vsID + "\" - startup()");
          continue;
        }
        mnEquipmentCount++;
      }
      String[] ids = new String[mnEquipmentCount];
      int[] mdls = new int[mnEquipmentCount];
      String[] nums = new String[mnEquipmentCount];
      System.arraycopy(masEquipmentIDs, 0, ids, 0, mnEquipmentCount);
      System.arraycopy(manEquipmentModels, 0, mdls, 0, mnEquipmentCount);
      System.arraycopy(masEquipmentNumbers, 0, nums, 0, mnEquipmentCount);
      masEquipmentIDs = ids;
      manEquipmentModels = mdls;
      masEquipmentNumbers = nums;
      //
      // Start with machine status of "On" (0)
      //
      setEquipmentOnline(true);
    }
    else
    {
       logger.logError("NO Configuration \"TransporterList\" - startup()");
    }
    //
    storePickupCompletionDelay = getConfigPropertyAsInt("StorePickupCompletionDelay", storePickupCompletionDelay);
    storeCompletionDelay = getConfigPropertyAsInt("StoreCompletionDelay", storeCompletionDelay);
    retrieveCompletionDelay = getConfigPropertyAsInt("RetrieveCompletionDelay", retrieveCompletionDelay);
    retrieveTriggerOfOperationIndicationDelay = getConfigPropertyAsInt("RetrieveTriggerOfOperationIndicationDelay", retrieveTriggerOfOperationIndicationDelay);
    retrieveArrivalReportDelay = getConfigPropertyAsInt("RetrieveArrivalReportDelay", retrieveArrivalReportDelay);
    //
    setControllerStatus(ControllerConsts.STATUS_WAIT_PORT);

    setEquipmentOnline(true);

    setupEquipmentStatusChanges();

    /*
     * SpeedFactor--when we want the equipment to go FAST
     */
    mnSpeedFactor = getConfigPropertyAsInt("SpeedFactor", 2);
    if (mnSpeedFactor < 1)
    {
      logger.logError(getName() + " has invalid SpeedFactor: " + mnSpeedFactor
          + "; resetting to 1.");
      mnSpeedFactor = 1;
    }

    mzUsePickupCompletion = getConfigPropertyAsBoolean("UsePickupCompletion");
    mzUseTriggerOfOperation = getConfigPropertyAsBoolean("UseTriggerOfOperation");
    mnID33CompletionClass = getConfigPropertyAsInt("ID33CompletionClass", 0);

    logger.logDebug("AGCDeviceEmulator.startup() - End");
  }

  /**
   * Is this piece of equipment a transporter?  (Do we get MC status messages?)
   *
   * @param isMCID
   * @return
   */
  @Override
  protected boolean isTransporter(String isMCID)
  {
    // We only get MC status messages for cranes, conveyor, and STVs
    return isMCID.startsWith("11") ||  // Crane
           isMCID.startsWith("21") ||  // Conveyor
           isMCID.startsWith("23") ||  // Shuttle Lifter
           isMCID.startsWith("51") ||  // Shuttle Vehicle
           isMCID.startsWith("54") ||  // STV-S
           isMCID.startsWith("55") ||  // STV-L
           isMCID.startsWith("73") ||  // AGV
           isMCID.startsWith("81");    // Duosys
  }

  /**
   *  Set up emulated status changes
   */
  synchronized private void setupEquipmentStatusChanges()
  {
    int vnDefaultInterval = 60000;

    String vsType = getConfigProperty("StatusChangeType");
    int vnInterval = getConfigPropertyAsInt("StatusChangeInterval", vnDefaultInterval);
    if (vnInterval < 10000)
    {
      logger.logError(getName() + " has invalid StatusChangeInterval "
          + vnInterval + "; resetting to " + vnDefaultInterval + ".");
      vnInterval = vnDefaultInterval;
    }

    if (vsType != null)
    {
      if (vsType.equalsIgnoreCase("RANDOM"))
      {
        logger.logDebug(getName() + " will have RANDOM status updates.");
        mpTimer.schedule(new TimedEquipmentStatusChange(true), 30000, vnInterval);
      }
      else if (vsType.equalsIgnoreCase("ROTATE"))
      {
        logger.logDebug(getName() + " will have ROTATING status updates.");
        mpTimer.schedule(new TimedEquipmentStatusChange(false), 30000, vnInterval);
      }
      else if (vsType.equalsIgnoreCase("NONE"))
      {
        logger.logDebug(getName() + " will not have emulated status updates.");
      }
      else
      {
        logger.logError("Invalid configuration property for " + getName() +
            ":" + "StatusChangeType.  Valid choices are RANDOM, ROTATE, and NONE.");
      }
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Shuts down this controller by cancelling any timers and shutting down the
   * Device/Equipment.
   */
  @Override
  public void shutdown()
  {
    logger.logDebug("AGCDeviceEmulator.shutdown() -- Start");
    timers.cancel();
    mpTimer.cancel();
    mpMachineStatusAGCMCMessage  = null;
    mpAGCMCMessage = null;
    mpAGCMOSMessage = null;
    msEquipmentPortCKN2 = null;
    masEquipmentIDs = null;
    manEquipmentModels = null;
    masEquipmentNumbers = null;
    mpLoadServer.cleanUp();
    mpLoadServer = null;
    mpLocServer.cleanUp();
    mpLocServer = null;
    mpStationServer.cleanUp();
    mpStationServer = null;
    logger.logDebug("AGCDeviceEmulator.shutdown() -- End");
    super.shutdown();
  }

  /*========================================================================*/
  /*  Process Messages & Events                                             */
  /*========================================================================*/

  @Override
  protected void processIPCReceivedMessage()
  {
    //
    // (Decide how to) Process message here
    //
    super.processIPCReceivedMessage();
    if (!receivedMessageProcessed)
    {
      receivedMessageProcessed = true;
      switch (receivedEventType)
      {
        case MessageEventConsts.EQUIPMENT_EVENT_TYPE:
                  processEquipmentEvent();
                  break;
        default: receivedMessageProcessed = false;
      }
    }
  }

  /*--------------------------------------------------------------------------
  * We come here when our Port notifies this Transporter that it has
  * changed status (Connecting, Running, etc.).
  --------------------------------------------------------------------------*/
  @Override
  protected void processStatusEvent()
  {
    super.processStatusEvent();  // Make sure we let the base class process this.
    //logger.logDebug(">=====> Transporter.processStatusEvent() -- Start -- \"" + receivedText + "\"");
    try
    {
      char chr0 = receivedText.charAt(0);
      switch (chr0)
      {
        case ControllerConsts.CONTROLLER_STATUS:
        {
          if (receivedCKN.equals(equipmentPortCKN))
          {
            //
            // The message is from the Material handling Computer ("MC") AGC
            // communication Port.
            //
            if (mnMCPortStatus == receivedData)
            {
              break;
            }
            else
            {
              //
              // Status of our Port has changed.
              //
              logger.logDebug("StatusChange (from Port) - WAS " +
                              ControllerConsts.STATUS_TEXT[mnMCPortStatus] + "  NOW " +
                              ControllerConsts.STATUS_TEXT[receivedData]);
              mnMCPortStatus = receivedData;
            }
          }
          else
          {
            //
            // The message is from the Monitoring and Operating support System ("MOS")
            // AGC communication Port.
            //
            if (mnMOSPortStatus == receivedData)
            {
              break;
            }
            else
            {
              //
              // Status of our Port has changed.
              //
              logger.logDebug("StatusChange (from Port) - WAS " +
                              ControllerConsts.STATUS_TEXT[mnMOSPortStatus] + "  NOW " +
                              ControllerConsts.STATUS_TEXT[receivedData]);
              mnMOSPortStatus = receivedData;
            }
          }
          if ((mnMCPortStatus == ControllerConsts.STATUS_RUNNING) &&
              (mnMOSPortStatus == ControllerConsts.STATUS_RUNNING))
          {
            setControllerStatus(ControllerConsts.STATUS_RUNNING);
          }
          else if ((mnMCPortStatus == ControllerConsts.STATUS_ERROR) ||
                   (mnMOSPortStatus == ControllerConsts.STATUS_ERROR))
          {
            setControllerStatus(ControllerConsts.STATUS_ERROR_PORT);
          }
          else if (controllerStatus == ControllerConsts.STATUS_ERROR_PORT)
          {
            setControllerStatus(ControllerConsts.STATUS_WAIT_PORT);
          }
        }
        break;
        default:
        {
          logger.logError("AGCDeviceEmulator.processStatusEvent() -- UNKNOWN Event Type \"" + chr0 + "\" -- processStatusEvent()");
        }
      }
    }
    catch(StringIndexOutOfBoundsException e)
    {
      logger.logSparseException(e, "AGCDeviceEmulator.processStatusEvent() - \"" + receivedText + "\"");
    }
  }

  /*--------------------------------------------------------------------------*/
  // We have received a CONTROL EVENT (probably from a User's Form)
  /*--------------------------------------------------------------------------*/
  @Override
  protected void processControlEvent()
  {
    try
    {
      switch (receivedData)
      {
        case ControlEventDataFormat.SEND_ARRIVAL_TO_EMULATOR:
          logger.logDebug(receivedData + " -- processControlEvent()");
          sendLoadArrivalReport();
          break;
        case ControlEventDataFormat.SEND_70_MESSAGE_TO_EMULATOR:
          send70Message();
          break;
        case ControlEventDataFormat.SEND_TRANSPORT_DATA_DELETE_TO_EMULATOR:
          logger.logDebug(receivedData + " -- processControlEvent()");
          sendTransportDataDelete(receivedText);
          break;
        default:
          logger.logError("UNKNOWN Event Type \"" + receivedData
              + "\" -- AGCDeviceEmulator.processControlEvent()");
      }
    }
    catch(StringIndexOutOfBoundsException e)
    {
      logger.logSparseException(e, "AGCDeviceEmulator.processControlEvent() - \""
          + receivedText + "\"");
    }
  }

  /*--------------------------------------------------------------------------
   We have received a message from the PORT that is connected to the actual
   Device/Equipment that this Transporter is controlling.

   The received data (String) is in "receivedText".
  --------------------------------------------------------------------------*/
  public void processEquipmentEvent()
  {
    if (receivedCKN.equals(equipmentPortCKN))
    {
      //
      // The message is from the Material handling Computer ("MC") AGC
      // communication Port.
      //
      processEquipmentMCEvent();
    }
    else
    {
      //
      // The message is from the Monitoring and Operating support System ("MOS")
      // AGC communication Port.
      //
      processEquipmentMosEvent();
    }
  }

  /**
   * Change the equipment status
   *
   * @param izOnline
   */
  protected void setEquipmentOnline(boolean izOnline)
  {
    /*
     *  0 = online
     *  1 = offline
     */
    int vnStatus = izOnline ? 0 : 1;

    mzAGCOnline = izOnline;
    mzEquipmentOnline = izOnline;

    mpMachineStatusAGCMCMessage.setMachineStatusSize(mnEquipmentCount);
    mpMachineStatusAGCMCMessage.setNumberOfReports(mnEquipmentCount);
    for (int i = 0; i < mnEquipmentCount; i++)
    {
      mpMachineStatusAGCMCMessage.setMachineStatus(i,
           masEquipmentIDs[i], manEquipmentModels[i], masEquipmentNumbers[i], vnStatus, NO_ERROR);
    }
  }

  /**
   * Change the equipment status
   *
   * @param inStatus
   * @param isErrorCode
   * @param isMOSID
   */
  protected void setEquipmentOnline(int inStatus, String isErrorCode, String isMOSID)
  {
    for (int i = 0; i < mnEquipmentCount; i++)
    {
      String vsMCID = mpMachineStatusAGCMCMessage.getMachineStatusMachineId(i);
      if (isMOSID.equals(AGCMOSMessage.getMOSIDfromMCID(vsMCID)))
      {
        mpMachineStatusAGCMCMessage.setMachineStatus(i, masEquipmentIDs[i],
            manEquipmentModels[i], masEquipmentNumbers[i], inStatus, isErrorCode);
        break;
      }
    }
  }

  /**
   * Timer task to send periodic status updates
   */
  protected class TimedEquipmentStatusChange  extends TimerTask
  {
    boolean mzRandomChanges;
    public TimedEquipmentStatusChange(boolean izRandomChanges)
    {
      mzRandomChanges = izRandomChanges;
    }

    @Override
    public void run()
    {
      if (mzRandomChanges)
      {
        randomlyChangeStatuses();
      }
      else
      {
        rotateErrorStatuses();
      }
    }

    /**
     * Yep, 'cuz this is what it's like
     */
    private void randomlyChangeStatuses()
    {
      setDetailedControllerStatus("Changing equipment statuses...");

      List<AGCMCMessage> vpResponses = new ArrayList<AGCMCMessage>();
      List<Integer>      vpDelays    = new ArrayList<Integer>();

      //
      // Statuses are changing -- MachineStatusReport (30).
      //
      mpMachineStatusAGCMCMessage.setMachineStatusSize(mnEquipmentCount);
      mpMachineStatusAGCMCMessage.setNumberOfReports(mnEquipmentCount);
      for (int i = 0; i < mnEquipmentCount; i++)
      {
        int vnStatus = (int)(Math.random() * 6) - 2;
        vnStatus = vnStatus < 0 ? 0 : vnStatus;
        String vsErrorCode = (vnStatus == 2) ? "1214029" : NO_ERROR;
        mpMachineStatusAGCMCMessage.setMachineStatus(i,
             masEquipmentIDs[i], manEquipmentModels[i], masEquipmentNumbers[i], vnStatus, vsErrorCode);
      }

      /*
       * Only send MC messages if we aren't emulating a MOS
       */
      if (msEquipmentPortCKN2 != null)
      {
        sendMosStatusMessages();
      }
      /*
       * If we aren't emulating a MOS, send MC messages
       */
      else
      {
        addStatusMessages(vpResponses, vpDelays);
        int vnInitialDelay = vpDelays.remove(0);
        setSSTimerEvent(new AGCMCResponse(vpResponses, vpDelays), vnInitialDelay);
      }
      mzAGCOnline = true;

      setDetailedControllerStatus("Equipment statuses changed!");
    }

    /**
     * Yep, 'cuz this is what it's like, too
     */
    private int mnBrokenEquipment = 0;
    private int mnCurrentState = 0;
    private void rotateErrorStatuses()
    {
      setDetailedControllerStatus("Changing equipment statuses...");

      List<AGCMCMessage> vpResponses = new ArrayList<AGCMCMessage>();
      List<Integer>      vpDelays    = new ArrayList<Integer>();

      //
      // Statuses are changing -- MachineStatusReport (30).
      //
      mpMachineStatusAGCMCMessage.setMachineStatusSize(mnEquipmentCount);
      mpMachineStatusAGCMCMessage.setNumberOfReports(mnEquipmentCount);

      /*
       * Fix the broken one
       */
      setEquipmentOnline(true);

      /*
       * Break a new one
       */
      mnCurrentState = (mnCurrentState+1)%5;
      switch (mnCurrentState)
      {
        case 0: // Everything online-break something new
          mnBrokenEquipment++;
          if (mnBrokenEquipment >= mnEquipmentCount) mnBrokenEquipment = 0;
          mpMachineStatusAGCMCMessage.setMachineStatus(mnBrokenEquipment,
              masEquipmentIDs[mnBrokenEquipment], manEquipmentModels[mnBrokenEquipment],
              masEquipmentNumbers[mnBrokenEquipment], 2, "1214029");
          break;

        case 1: // Set the broken one to offline
          mpMachineStatusAGCMCMessage.setMachineStatus(mnBrokenEquipment,
              masEquipmentIDs[mnBrokenEquipment], manEquipmentModels[mnBrokenEquipment],
              masEquipmentNumbers[mnBrokenEquipment], 1, NO_ERROR);
          break;

        case 2: // Leave the broken one active for a few seconds.
          break;

        default:
          // It has already been fixed, and the messages sent.  Just return.
          return;
      }

      /*
       * Only send MC messages if we aren't emulating a MOS
       */
      if (msEquipmentPortCKN2 != null)
      {
        sendMosStatusMessages();
      }
      /*
       * If we aren't emulating a MOS, send MC messages
       */
      else
      {
        addStatusMessages(vpResponses, vpDelays);
        int vnInitialDelay = vpDelays.remove(0);
        setSSTimerEvent(new AGCMCResponse(vpResponses, vpDelays), vnInitialDelay);
      }
      mzAGCOnline = true;

      setDetailedControllerStatus("Equipment statuses changed ("+ mnBrokenEquipment + ")!");
    }
  }


  /*========================================================================*/
  /*  MC Messages                                                          */
  /*========================================================================*/

  /**
   * Process messages from the Material handling Computer ("MC")
   * AGC communication Port.
   *
   * <BR>The received data (String) is in "receivedText".
   */
  void processEquipmentMCEvent()
  {
    if (mpAGCMCMessage == null)
    {
      logger.logError("AGCDeviceEmulator-->processEquipmentMCEvent():: Received null equipment message.");
      return;
    }

    mpAGCMCMessage.toDataValues(receivedText);
    if (!mpAGCMCMessage.getValidMessage())
    {
      String s = mpAGCMCMessage.getInvalidMessageDescription();
      logger.logRxEquipmentMessage(receivedText, s);
      logger.logError("AGCDeviceEmulator.processEquipmentMCEvent() -- " + s);
    }
    else
    {
      String s = mpAGCMCMessage.getParsedMessageString();
      logger.logRxEquipmentMessage(receivedText, s);
      logger.logDebug("Msg ID: " + s);

      switch (mpAGCMCMessage.getID())
      {
        case  1: processRequestToStartOperation(); break;
        case  2: processDateTimeData(); break;
        case  3: processRequestToTerminateOperation(); break;
        case  4: processTransportDataCancel(); break;
        case  5: processTransportCommand(); break;
        //    6
        //    7
        case  8: processDestinationStationChangeCmd(); break;
        //    9
        case 10: processMachineStatusInquiry(); break;
        case 11: processAlternativeLocationCmd(); break;
        case 12: processRetrievalCmd(); break;
        //   13
        //   14
        //   15
        case 16: processSimultaneousStartStopCmd(); break;
        //   17
        //   18
        case 19: processCommunicationTestRequest(); break;
        case 20: processResponseToCommunicationTestRequest(); break;
        case 41: processResponseToOperationModeChangeRequest(); break;
        case 42: processOperationModeChangeCmd(); break;
        //   43
        //   44
        case 45: processMCOperationComplete();break;
        case 46: processResponseToRetrievalTrigger(); break;
        case 47: processRequestRetrievalTriggerRepetition(); break;
        case 50: process50Message(); break;
        case 51: processRequestAccessImpossibleLocations(); break;
        //   52
        //   53
        case 54: processDOOutputInstruction();break;
        //   55
        //   56
        //   57
        case 58: processRequestToStartSystemRecovery(); break;
        case 59: processRequestToTerminateSystemRecovery(); break;
        default:
          logger.logError("Msg ID: " + mpAGCMCMessage.getID() +
                          " NOT Processed - AGCDeviceEmulator.processEquipmentMCEvent()");
          break;
      }
    }
  }

  /*------------------------------------------------------------------------*/
  /**
   * Send Load Arrival
   */
  protected void sendLoadArrivalReport()
  {
    ControlEventDataFormat vpCEDF = Factory.create(ControlEventDataFormat.class,
        getName());
    vpCEDF.parseArrivalCommand(receivedText);

    //
    // Send an arrivalReport (26) to show that the load is coming back into
    // the input station.
    //
    AGCMCMessage vpArrivalMessage = new AGCMCMessage();
    vpArrivalMessage.setMCKey(vpCEDF.getMCKey());
    vpArrivalMessage.setLoadInformation(vpCEDF.getLoadInfo());
    vpArrivalMessage.setControlInformation(vpCEDF.getControlInformation());
    vpArrivalMessage.setArrivalStationNumber(vpCEDF.getStation());
    vpArrivalMessage.setDimensionInformation(vpCEDF.getDimensionInfo());
    vpArrivalMessage.setBCData(vpCEDF.getBarCode());
    vpArrivalMessage.arrivalReportToString();
    transmitMessageToMCDevice(vpArrivalMessage);
  }

  /**
   * Send a transport data delete message
   *
   * @param isMessage
   */
  protected void sendTransportDataDelete(String isMessage)
  {
    ControlEventDataFormat vpCEDF = Factory.create(ControlEventDataFormat.class,
        getName());
    vpCEDF.parseTransportDataDeleteCommand(isMessage);

    AGCMCMessage vpDeleteMessage = new AGCMCMessage();
    vpDeleteMessage.setMessageData("1");
    vpDeleteMessage.setMCKey(vpCEDF.getMCKey());
    vpDeleteMessage.setDestinationStationNumber(vpCEDF.getStation());
    vpDeleteMessage.setControlInformation(vpCEDF.getControlInformation());
    vpDeleteMessage.transportDataDeletionReportToString();
    transmitMessageToMCDevice(vpDeleteMessage);
  }

  /**
   * Method stub
   * Custom 70 message to be created in extended class
   */
  protected void send70Message()
  {

  }

  /**
   * Add the correct number of message 30's
   * @param ipResponses
   * @param ipDelays
   */
  private void addStatusMessages(List<AGCMCMessage> ipResponses,
      List<Integer> ipDelays)
  {
    int vnEquipmentCount = mnEquipmentCount;
    for (int i = 0; i < mnEquipmentCount; i = i + STATUSREPORTSPERMESSAGE)
    {
      AGCMCMessage vpAGCMCResponse_30 = new AGCMCMessage();

      int vnReports = Math.min(vnEquipmentCount, STATUSREPORTSPERMESSAGE);
      vpAGCMCResponse_30.setNumberOfReports(vnReports);
      for (int x = 0; x < vnReports; x++)
      {
        vpAGCMCResponse_30.setMachineStatusItem(x,
            mpMachineStatusAGCMCMessage.getMachineStatusItem(x + i));
      }
      vnEquipmentCount = vnEquipmentCount - STATUSREPORTSPERMESSAGE;
      vpAGCMCResponse_30.setContinuationClassification(vnEquipmentCount > 0? 1:2);
      vpAGCMCResponse_30.machineStatusReportToString();

      ipDelays.add(100);
      ipResponses.add(vpAGCMCResponse_30);
    }
  }

  /**
   * Add a status message for one piece of equipment
   *
   * @param ipResponses
   * @param ipDelays
   * @param isMOSID
   */
  protected void addOneStatusMessage(List<AGCMCMessage> ipResponses,
      List<Integer> ipDelays, String isMOSID)
  {
    for (int i = 0; i < mnEquipmentCount; i++)
    {
      String vsMCID = mpMachineStatusAGCMCMessage.getMachineStatusMachineId(i);
      if (isMOSID.equals(AGCMOSMessage.getMOSIDfromMCID(vsMCID)))
      {
        AGCMCMessage vpAGCMCResponse_30 = new AGCMCMessage();

        vpAGCMCResponse_30.setNumberOfReports(1);
        vpAGCMCResponse_30.setMachineStatusItem(0,
            mpMachineStatusAGCMCMessage.getMachineStatusItem(i));
        vpAGCMCResponse_30.setContinuationClassification(2);
        vpAGCMCResponse_30.machineStatusReportToString();

        ipDelays.add(1000);
        ipResponses.add(vpAGCMCResponse_30);
        break;
      }
    }
  }

  /*------------------------------------------------------------------------*/
  /**
   * MC ID 01
   * <BR>Need to reply with a 21, 30, 71
   */
  private void processRequestToStartOperation()
  {
    setDetailedControllerStatus("Request To Start Operation (01) - Received");

    List<AGCMCMessage> vpResponses = new ArrayList<AGCMCMessage>();
    List<Integer>      vpDelays    = new ArrayList<Integer>();

    //
    // First is responseToOperationStartRequest (21)
    //
    AGCMCMessage vpMCResponse_21 = new AGCMCMessage();
    vpMCResponse_21.setResponseClassification(0); // Normal
    vpMCResponse_21.setErrorDetails("00");
    vpMCResponse_21.setSystemRecoveryReport(1); // System Recovery Conducted
    vpMCResponse_21.responseToOperationStartRequestToString();

    vpDelays.add(700);
    vpResponses.add(vpMCResponse_21);

    //
    // Next is a MachineStatusReport (30).
    //
    setEquipmentOnline(true);
    addStatusMessages(vpResponses, vpDelays);

    //
    // Next is a ImpossibleLocationReport (71).
    //
    addImpossibleLocationsMessages(vpResponses, vpDelays);

    mzAGCOnline = true;

    int vnInitialDelay = vpDelays.remove(0);
    setSSTimerEvent(new AGCMCResponse(vpResponses, vpDelays), vnInitialDelay);

    setDetailedControllerStatus("Request To Start Operation (01) - Processed");
  }

  /**
   * Add the Impossible Locations messages
   *
   * @param ipResponses
   * @param vpDelays
   */
  @SuppressWarnings("rawtypes")
  private void addImpossibleLocationsMessages(List<AGCMCMessage> ipResponses,
      List<Integer> vpDelays)
  {
    // Get the list of prohibited locations from the database.
    List<Map> vpProhibitedLocs = null;
    LocationData vpLocData = Factory.create(LocationData.class);
    StandardLocationServer vpLocServer = Factory.create(StandardLocationServer.class);
    try
    {
      vpLocData.setKey(LocationData.LOCATIONSTATUS_NAME, DBConstants.LCPROHIBIT);
      vpLocData.addOrderByColumn(LocationData.ADDRESS_NAME);
      vpProhibitedLocs = vpLocServer.getLocationData(vpLocData);
    }
    catch (DBException dbe)
    {
      logger.logException("Error reading prohibited locations", dbe);
    }

    // We have to send something, even if there are none.
    if (vpProhibitedLocs == null || vpProhibitedLocs.size() == 0)
    {
      AGCMCMessage vpMCResponse_71 = new AGCMCMessage();
      vpMCResponse_71.setNumberOfReports(0);
      vpMCResponse_71.setContinuationClassification(2);
      vpMCResponse_71.accessImpossibleLocationsReportToString();

      vpDelays.add(100);
      ipResponses.add(vpMCResponse_71);
      return;
    }

    // We have prohibited locations.  Build the messages.
    do
    {
      int vnReports = Math.min(AGCMCMessage.MAX_IMPOSSIBLE_REPORTS, vpProhibitedLocs.size());
      int vnContinuation = vpProhibitedLocs.size() > vnReports ? 1 : 2;

      AGCMCMessage vpMCResponse_71 = new AGCMCMessage();
      vpMCResponse_71.setNumberOfReports(vnReports);
      vpMCResponse_71.setContinuationClassification(vnContinuation);

      for (int i = 0; i < vnReports; i++)
      {
        Map m = vpProhibitedLocs.remove(0);
        vpLocData.dataToSKDCData(m);
        String vsEquipWarehouse = "0";
        try
        {
          WarehouseData vpWHData = Factory.create(WarehouseData.class);
          vpWHData.setKey(WarehouseData.WAREHOUSE_NAME, vpLocData.getWarehouse());
          vpWHData.dataToSKDCData(vpLocServer.getWarehouseData(vpWHData).get(0));
          vsEquipWarehouse = vpWHData.getEquipWarehouse();
        }
        catch (Exception e)
        {
          logger.logException("Error finding warehouse '"
              + vpLocData.getWarehouse() + "'", e);
        }
        try
        {
          RackLocationParser vpRLP = RackLocationParser.parse(vpLocData.getAddress(), true);

          vpMCResponse_71.setImpossibleLocationStatus(i, 1);
          vpMCResponse_71.setImpossibleLocationStorageClassification(i, vsEquipWarehouse);
          vpMCResponse_71.setImpossibleLocationBankNumber(i, vpRLP.getBankString().substring(1));
          // Real equipment gives a rectangle.  This is close enough for now.
          vpMCResponse_71.setImpossibleLocationStartBayNumber(i, vpRLP.getBayString());
          vpMCResponse_71.setImpossibleLocationStartLevelNumber(i, vpRLP.getTierString());
          vpMCResponse_71.setImpossibleLocationEndBayNumber(i, vpRLP.getBayString());
          vpMCResponse_71.setImpossibleLocationEndLevelNumber(i, vpRLP.getTierString());
        }
        catch (IOException e)
        {
          logger.logException("Error parsing " + vpLocData.getAddress(), e);
          return;
        }
      }

      vpMCResponse_71.accessImpossibleLocationsReportToString();

      vpDelays.add(100);
      ipResponses.add(vpMCResponse_71);
    }
    while (vpProhibitedLocs.size() > 0);
  }



  /*------------------------------------------------------------------------*/
  /**
   * MC ID 02
   */
  private void processDateTimeData()
  {
    logger.logDebug("MC ID 02: Need Some Code!!!");
  }

  /*------------------------------------------------------------------------*/
  /**
   * MC ID 03
   * <BR>Need to reply with a 23
   */
  private void processRequestToTerminateOperation()
  {
    List<AGCMCMessage> vpResponses = new ArrayList<AGCMCMessage>();
    List<Integer>      vpDelays    = new ArrayList<Integer>();

    /*
     * ALWAYS Need to respond with a 23
     */
    AGCMCMessage vpAGCMCResponse_23 = new AGCMCMessage();
    vpAGCMCResponse_23.setResponseClassification(0); // Normal
    vpAGCMCResponse_23.setResponseDetailsModelCode("00");
    vpAGCMCResponse_23.setResponseDetailsMachineNumber("000000");
    vpAGCMCResponse_23.responseToOperationTerminationRequestToString();

    if (mzAGCOnline)
    {
      setEquipmentOnline(false);
      vpDelays.add(1280);
      vpResponses.add(vpAGCMCResponse_23);

      //
      // Next is a MachineStatusReport (30).
      //
      addStatusMessages(vpResponses, vpDelays);

      int vnInitialDelay = vpDelays.remove(0);
      setSSTimerEvent(new AGCMCResponse(vpResponses, vpDelays), vnInitialDelay);
    }
    else
    {
      setSSTimerEvent(new AGCMCResponse(vpAGCMCResponse_23), 40);
    }
  }

  /*------------------------------------------------------------------------*/
  /**
   * MC ID 04
   * <BR>Need to reply with a 24
   */
  private void processTransportDataCancel()
  {
    //
    // Just need responseToTransportDataCancel (24).
    //
    AGCMCMessage vpAGCMCResponse_24 = new AGCMCMessage();
    vpAGCMCResponse_24.setMCKey(mpAGCMCMessage.getMCKey());
    vpAGCMCResponse_24.setResponseClassification(0); // Normal
    vpAGCMCResponse_24.responseToTransportDataCancelToString();

    setSSTimerEvent(new AGCMCResponse(vpAGCMCResponse_24), 1000);
  }


  /*------------------------------------------------------------------------*/
  /**
   * MC ID 05
   * <BR>Need to reply with a 25, 64, 33 (station to rack)
   * <BR>Need to reply with a 25, 64, 68, 26 (if required) (station to station)
   */
  protected void processTransportCommand()
  {
    List<AGCMCMessage> vpResponses = new ArrayList<AGCMCMessage>();
    List<Integer>      vpDelays    = new ArrayList<Integer>();

    //
    // First is responseToTransportCommand (25).
    //
    AGCMCMessage vpAGCMCResponse_25 = new AGCMCMessage();
    vpAGCMCResponse_25.setMCKey(mpAGCMCMessage.getMCKey());
    vpAGCMCResponse_25.setResponseClassification(0); // Normal
    vpAGCMCResponse_25.responseToTransportCommandToString();

    vpDelays.add(60);
    vpResponses.add(vpAGCMCResponse_25);

    if (mzUsePickupCompletion)
    {
      //
      // Next is a pickupCompletionReport (64).
      //
      AGCMCMessage vpAGCMCResponse_64 = new AGCMCMessage();
      vpAGCMCResponse_64.setMCKey(mpAGCMCMessage.getMCKey());
      vpAGCMCResponse_64.setSourceStationNumber(mpAGCMCMessage.getSourceStationNumber());
      vpAGCMCResponse_64.setNumberOfReports (1);
      vpAGCMCResponse_64.pickupCompletionReportToString();

      vpDelays.add(storePickupCompletionDelay);
      vpResponses.add(vpAGCMCResponse_64);
    }

    //
    // Next is a operationCompletionReport (33).
    //
    String vsDestStation = mpAGCMCMessage.getDestinationStationNumber();
    if (vsDestStation.equals(AGCDeviceConstants.RACKSTATION))
    {
      // Completion: 0 = Normal, 1 = Bin Full, 2 = Bin Empty, 3 = Size Mismatch

      AGCMCMessage vpAGCMCResponse_33 = Factory.create(AGCMCMessage.class);
      vpAGCMCResponse_33.setRetrievalDataMCKey(0, mpAGCMCMessage.getMCKey());
      vpAGCMCResponse_33.setRetrievalDataTransportationClassification(0, mpAGCMCMessage.getTransportClassification());
      vpAGCMCResponse_33.setRetrievalDataCategory(0, 2);
      vpAGCMCResponse_33.setRetrievalDataCompletionClassification(0, mnID33CompletionClass);
      vpAGCMCResponse_33.setRetrievalDataSourceStationNumber(0, mpAGCMCMessage.getSourceStationNumber());
      vpAGCMCResponse_33.setRetrievalDataDestinationStationNumber(0, mpAGCMCMessage.getDestinationStationNumber());
      vpAGCMCResponse_33.setRetrievalDataLocationNumber(0, mpAGCMCMessage.getLocationNumber());
      vpAGCMCResponse_33.setRetrievalDataShelfPosition(0, mpAGCMCMessage.getShelfPosition());
//      vpAGCMCResponse_33.setRetrievalDataShelfToShelfLocationNumber(0, mpAGCMCMessage.getRetrievalDataShelfToShelfLocationNumber(0));
      vpAGCMCResponse_33.setRetrievalDataDimension(0, mpAGCMCMessage.getDimensionInformation());
      vpAGCMCResponse_33.setRetrievalDataBCData(0, mpAGCMCMessage.getBCData());
      vpAGCMCResponse_33.setRetrievalDataWorkNumber(0, mpAGCMCMessage.getWorkNumber());
      vpAGCMCResponse_33.setRetrievalDataControlInformation(0, mpAGCMCMessage.getControlInformation());
      vpAGCMCResponse_33.setRetrievalDataCategory(1, 0);
      vpAGCMCResponse_33.operationCompletionReportToString();

      vpDelays.add(storeCompletionDelay);
      vpResponses.add(vpAGCMCResponse_33);

      if (mnID33CompletionClass != 0)
      {
        try
        {
          LoadData vpLoadData = mpLoadServer.getLoad(mpAGCMCMessage.getMCKey());
          LocationData vpLocData = mpLocServer.getLocationRecord(
              vpLoadData.getNextWarehouse(), vpLoadData.getNextAddress());
          String vsMosID = getMOSIDForDevice(vpLocData.getDeviceID());
          String vsErrorCode = NO_ERROR;
          switch(mnID33CompletionClass)
          {
            case COMPLETE_BIN_FULL:
              vsErrorCode = AGCMCMessage.ERROR_BIN_FULL_AT_LOCATION;
              break;
            case COMPLETE_BIN_EMPTY:
              vsErrorCode = AGCMCMessage.ERROR_BIN_EMPTY_AT_LOCATION;
              break;
            case COMPLETE_SIZE_MISMATCH:
              vsErrorCode = AGCMCMessage.ERROR_SIZE_MISMATCH;
              break;
          }
          setEquipmentOnline(STATUS_ERROR, vsErrorCode, vsMosID);
          addOneStatusMessage(vpResponses, vpDelays, vsMosID);
        }
        catch (Exception e)
        {
          logger.logException(e);
        }
      }
    }
    else
    {
      // Handle representative stations
      String vsChildStn = mpStationServer.getReprStationChild(vsDestStation);
      if (vsChildStn.trim().length() > 0)
        vsDestStation = vsChildStn;

      // Does this station need an arrival?
      boolean vzSendArrival = needToSendArrival(vsDestStation);

      // Only send Trigger of Operation when arrivals are NOT required
      if (mzUseTriggerOfOperation && !vzSendArrival)
      {
        AGCMCMessage vpAGCMCResponse_68 = new AGCMCMessage();
        vpAGCMCResponse_68.setMCKey(mpAGCMCMessage.getMCKey());
        vpAGCMCResponse_68.setDestinationStationNumber(vsDestStation);
        vpAGCMCResponse_68.setControlInformation(mpAGCMCMessage.getControlInformation());
        vpAGCMCResponse_68.triggerOfOperationIndicationToString();

        vpDelays.add(storeCompletionDelay);
        vpResponses.add(vpAGCMCResponse_68);
      }

      /*
       * Only send the arrival if arrivals are required
       */
      if (vzSendArrival || autoPressCompleteButton(vsDestStation))
      {
        //
        // Next is arrivalReport (26)
        //
        AGCMCMessage vpAGCMCResponse_26 = new AGCMCMessage();
        vpAGCMCResponse_26.setMCKey(mpAGCMCMessage.getMCKey());
        vpAGCMCResponse_26.setArrivalStationNumber(vsDestStation);
        vpAGCMCResponse_26.setDimensionInformation(mpAGCMCMessage.getDimensionInformation());
        vpAGCMCResponse_26.setLoadInformation(1);
        vpAGCMCResponse_26.setBCData(mpAGCMCMessage.getBCData());
        vpAGCMCResponse_26.setControlInformation(mpAGCMCMessage.getControlInformation());
        vpAGCMCResponse_26.arrivalReportToString();

        int vnArrivalDelay;
        if(mpStationServer.isSimulationOn(vsDestStation))
          vnArrivalDelay = mpStationServer.getStation(vsDestStation).getSimInterval();
        else
          vnArrivalDelay = storeCompletionDelay;
        vpDelays.add(vnArrivalDelay);
        vpResponses.add(vpAGCMCResponse_26);
      }
    }

    int vnInitialDelay = vpDelays.remove(0);
    setSSTimerEvent(new AGCMCResponse(vpResponses, vpDelays), vnInitialDelay);
  }

  /*------------------------------------------------------------------------*/
  /**
   * MC ID 08
   * <BR>Need to reply with a 28
   */
  protected void processDestinationStationChangeCmd()
  {
    AGCMCMessage vpAGCMCResponse_28 = new AGCMCMessage();
    //
    // Just need responseToDestinationStationChangeCmd (28).
    //
    vpAGCMCResponse_28.setMCKey(mpAGCMCMessage.getMCKey());
    vpAGCMCResponse_28.setCommandClassification(mpAGCMCMessage.getCommandClassification());
    vpAGCMCResponse_28.setLocationNumber(mpAGCMCMessage.getLocationNumber()); // Normal
    vpAGCMCResponse_28.setShelfPosition(mpAGCMCMessage.getShelfPosition()); // Normal
    vpAGCMCResponse_28.setDestinationStationNumber(mpAGCMCMessage.getRejectStationNumber()); // Normal
    vpAGCMCResponse_28.setResponseClassification(0); // Normal
    vpAGCMCResponse_28.responseToDestinationStationChangeCmdToString();

    setSSTimerEvent(new AGCMCResponse(vpAGCMCResponse_28), 1000);
  }

  /*------------------------------------------------------------------------*/
  /**
   * MC ID 10
   * <BR>Need to reply with a 30
   */
  private void processMachineStatusInquiry()
  {
    List<AGCMCMessage> vpResponses = new ArrayList<AGCMCMessage>();
    List<Integer>      vpDelays    = new ArrayList<Integer>();

    //
    // Response is a MachineStatusReport (30).
    //
    addStatusMessages(vpResponses, vpDelays);

    int vnInitialDelay = vpDelays.remove(0);
    setSSTimerEvent(new AGCMCResponse(vpResponses, vpDelays), vnInitialDelay);
  }

  /*------------------------------------------------------------------------*/
  /**
   * MC ID 11
   * <BR>Need to reply with a 25, 64, 33
   */
  private void processAlternativeLocationCmd()
  {
    List<AGCMCMessage> vpResponses = new ArrayList<AGCMCMessage>();
    List<Integer>      vpDelays    = new ArrayList<Integer>();

    //
    // First is responseToAlternativeLocationCmd (31).
    //
    AGCMCMessage vpAGCMCResponse_31 = new AGCMCMessage();
    vpAGCMCResponse_31.setMCKey(mpAGCMCMessage.getMCKey());

    // Test the access impossible location response
//  if (vsDestStation.equals(AGCDeviceConstants.RACKSTATION))
//  {
//    vpAGCMCResponse_31.setResponseClassification(6); // Impossible!
//    vpAGCMCResponse_31.responseToAlternativeLocationCmdToString();
//
//    vpDelays.add(2500);
//    vpResponses.add(vpAGCMCResponse_31);
//
//    int vnInitialDelay = vpDelays.remove(0);
//    setSSTimerEvent(new AGCMCResponse(vpResponses, vpDelays), vnInitialDelay);
//    return;
//  }

    vpAGCMCResponse_31.setResponseClassification(0); // Normal
    vpAGCMCResponse_31.responseToAlternativeLocationCmdToString();

    vpDelays.add(2500);
    vpResponses.add(vpAGCMCResponse_31);

    try
    {
      LoadData vpLoadData = mpLoadServer.getLoad(mpAGCMCMessage.getMCKey());
      LocationData vpLocData;
      if (mpAGCMCMessage.getCommandClassification() == AGCMCMessage.CC_BINEMPTY_CANCEL)
      {
        vpLocData = mpLocServer.getLocationRecord(
            vpLoadData.getWarehouse(), vpLoadData.getAddress());
      }
      else
      {
        vpLocData = mpLocServer.getLocationRecord(
            vpLoadData.getNextWarehouse(), vpLoadData.getNextAddress());
      }
      String vsMosID = getMOSIDForDevice(vpLocData.getDeviceID());

      setEquipmentOnline(STATUS_OFFLINE, NO_ERROR, vsMosID);
      addOneStatusMessage(vpResponses, vpDelays, vsMosID);
      setEquipmentOnline(STATUS_ONLINE, NO_ERROR, vsMosID);
      addOneStatusMessage(vpResponses, vpDelays, vsMosID);
    }
    catch (Exception e)
    {
      logger.logException(e);
    }

    // If this is a data cancel, we're done
    if (mpAGCMCMessage.getCommandClassification() == AGCMCMessage.CC_BINEMPTY_CANCEL ||
        mpAGCMCMessage.getCommandClassification() == AGCMCMessage.CC_BINFULL_CANCEL ||
        mpAGCMCMessage.getCommandClassification() == AGCMCMessage.CC_SIZEMIS_CANCEL)
    {
      int vnInitialDelay = vpDelays.remove(0);
      setSSTimerEvent(new AGCMCResponse(vpResponses, vpDelays), vnInitialDelay);
      return;
    }

    //
    // Next is arrivalReport (26) for stations or (33) for locations
    //
    String vsDestStation = mpAGCMCMessage.getAlternateStationNumber();
    if (vsDestStation.equals(AGCDeviceConstants.RACKSTATION))
    {
      AGCMCMessage vpAGCMCResponse_33 = new AGCMCMessage();
      vpAGCMCResponse_33.setRetrievalDataMCKey(0, mpAGCMCMessage.getMCKey());
      vpAGCMCResponse_33.setRetrievalDataTransportationClassification(0, mpAGCMCMessage.getTransportClassification());
      vpAGCMCResponse_33.setRetrievalDataCategory(0, 2);
      vpAGCMCResponse_33.setRetrievalDataCompletionClassification(0, 0); // 0 = Normal, 3=Height Mis-match
      vpAGCMCResponse_33.setRetrievalDataSourceStationNumber(0, mpAGCMCMessage.getSourceStationNumber());
      vpAGCMCResponse_33.setRetrievalDataDestinationStationNumber(0, mpAGCMCMessage.getAlternateStationNumber());
      vpAGCMCResponse_33.setRetrievalDataLocationNumber(0, mpAGCMCMessage.getAlternateLocation());
      vpAGCMCResponse_33.setRetrievalDataShelfPosition(0, mpAGCMCMessage.getShelfPosition());
//      vpAGCMCResponse_33.setRetrievalDataShelfToShelfLocationNumber(0, mpAGCMCMessage.getRetrievalDataShelfToShelfLocationNumber(0));
      vpAGCMCResponse_33.setRetrievalDataDimension(0, mpAGCMCMessage.getDimensionInformation());
      vpAGCMCResponse_33.setRetrievalDataBCData(0, mpAGCMCMessage.getBCData());
      vpAGCMCResponse_33.setRetrievalDataWorkNumber(0, mpAGCMCMessage.getWorkNumber());
      vpAGCMCResponse_33.setRetrievalDataControlInformation(0, mpAGCMCMessage.getControlInformation());
      vpAGCMCResponse_33.setRetrievalDataCategory(1, 0);
      vpAGCMCResponse_33.operationCompletionReportToString();

      vpDelays.add(storeCompletionDelay);
      vpResponses.add(vpAGCMCResponse_33);
    }
    else
    {
      // Handle representative stations
      String vsChildStn = mpStationServer.getReprStationChild(vsDestStation);
      if (vsChildStn.trim().length() > 0)
        vsDestStation = vsChildStn;

      // Does this station need an arrival?
      boolean vzSendArrival = needToSendArrival(vsDestStation);

      // Only send Trigger of Operation when arrivals are NOT required
      if (mzUseTriggerOfOperation && !vzSendArrival)
      {
        AGCMCMessage vpAGCMCResponse_68 = new AGCMCMessage();
        vpAGCMCResponse_68.setMCKey(mpAGCMCMessage.getMCKey());
        vpAGCMCResponse_68.setDestinationStationNumber(vsDestStation);
        vpAGCMCResponse_68.setControlInformation(mpAGCMCMessage.getControlInformation());
        vpAGCMCResponse_68.triggerOfOperationIndicationToString();

        vpDelays.add(storeCompletionDelay);
        vpResponses.add(vpAGCMCResponse_68);
      }

      /*
       * Only send the arrival if arrivals are required
       */
      if (vzSendArrival || autoPressCompleteButton(vsDestStation))
      {
        AGCMCMessage vpAGCMCResponse_26 = new AGCMCMessage();
        vpAGCMCResponse_26.setMCKey(mpAGCMCMessage.getMCKey());
        vpAGCMCResponse_26.setArrivalStationNumber(vsDestStation);
        vpAGCMCResponse_26.setDimensionInformation(mpAGCMCMessage.getDimensionInformation());
        vpAGCMCResponse_26.setLoadInformation(1);
        vpAGCMCResponse_26.setBCData(mpAGCMCMessage.getBCData());
        vpAGCMCResponse_26.setControlInformation(mpAGCMCMessage.getControlInformation());
        vpAGCMCResponse_26.arrivalReportToString();

        int vnArrivalDelay;
        if(mpStationServer.isSimulationOn(vsDestStation))
          vnArrivalDelay = mpStationServer.getStation(vsDestStation).getSimInterval();
        else
          vnArrivalDelay = storeCompletionDelay*10;
        vpDelays.add(vnArrivalDelay);
        vpResponses.add(vpAGCMCResponse_26);
      }
    }

    int vnInitialDelay = vpDelays.remove(0);
    setSSTimerEvent(new AGCMCResponse(vpResponses, vpDelays), vnInitialDelay);
  }

  /*------------------------------------------------------------------------*/
  /**
   * MC ID 12
   * <BR>Need to reply with a 32, 33, 68, 26
   */
  protected void processRetrievalCmd()
  {
    List<AGCMCMessage> vpResponses = new ArrayList<AGCMCMessage>();
    List<Integer>      vpDelays    = new ArrayList<Integer>();
    //
    // First is responseToRetrievalCmd (32).
    //
    AGCMCMessage vpAGCMCResponse_32 = new AGCMCMessage();
    int vnRetDataTransClass = mpAGCMCMessage.getRetrievalDataTransportationClassification(0);
    vpAGCMCResponse_32.setMCKey(mpAGCMCMessage.getRetrievalDataMCKey(0));
    vpAGCMCResponse_32.setResponseClassification(0); // Normal
    vpAGCMCResponse_32.setMCKey2(mpAGCMCMessage.getRetrievalDataMCKey(1));
    vpAGCMCResponse_32.setResponseClassification2(0); // Normal
    vpAGCMCResponse_32.setRetrievalDataTransportationClassification(0, vnRetDataTransClass);
    vpAGCMCResponse_32.responseToRetrievalCmdToString();

    vpDelays.add(50);
    vpResponses.add(vpAGCMCResponse_32);

    //
    // Next is a operationCompletionReport (33) for Retrieval pickup.
    //
    AGCMCMessage vpAGCMCResponse_33 = new AGCMCMessage();
    for (int i = 0; i < 2; i++)
    {
      // Completion: 0 = Normal, 1 = Bin Full, 2 = Bin Empty, 3 = Size Mismatch,
      // 9 = Cancel
      int vnCompletion = COMPLETE_NORMAL;

      vpAGCMCResponse_33.setRetrievalDataMCKey(i, mpAGCMCMessage.getRetrievalDataMCKey(i));
      vpAGCMCResponse_33.setRetrievalDataTransportationClassification(i, mpAGCMCMessage.getRetrievalDataTransportationClassification(i));
      vpAGCMCResponse_33.setRetrievalDataCategory(i, mpAGCMCMessage.getRetrievalDataCategory(i));
      vpAGCMCResponse_33.setRetrievalDataCompletionClassification(i, vnCompletion);
      vpAGCMCResponse_33.setRetrievalDataSourceStationNumber(i, mpAGCMCMessage.getRetrievalDataSourceStationNumber(i));
      vpAGCMCResponse_33.setRetrievalDataDestinationStationNumber(i, mpAGCMCMessage.getRetrievalDataDestinationStationNumber(i));
      vpAGCMCResponse_33.setRetrievalDataLocationNumber(i, mpAGCMCMessage.getRetrievalDataLocationNumber(i));
      vpAGCMCResponse_33.setRetrievalDataShelfPosition(i, mpAGCMCMessage.getRetrievalDataShelfPosition(i));
      vpAGCMCResponse_33.setRetrievalDataShelfToShelfLocationNumber(i, mpAGCMCMessage.getRetrievalDataShelfToShelfLocationNumber(i));
      vpAGCMCResponse_33.setRetrievalDataShelfToShelfPosition(i, mpAGCMCMessage.getRetrievalDataShelfToShelfPosition(i));
      vpAGCMCResponse_33.setRetrievalDataDimension(i, mpAGCMCMessage.getRetrievalDataDimension(i));
      vpAGCMCResponse_33.setRetrievalDataBCData(i, mpAGCMCMessage.getRetrievalDataBCData(i));
      vpAGCMCResponse_33.setRetrievalDataWorkNumber(i, mpAGCMCMessage.getRetrievalDataWorkNumber(i));
      vpAGCMCResponse_33.setRetrievalDataControlInformation(i, mpAGCMCMessage.getRetrievalDataControlInformation(i));
    }

    //
    // Bin-to-Bin move - Need Bin-to-Bin Retrieval (4)
    //
    if (vnRetDataTransClass == 5)
    {
      vpAGCMCResponse_33.setRetrievalDataTransportationClassification(0, 4);
    }
    vpAGCMCResponse_33.operationCompletionReportToString();

    vpDelays.add(retrieveCompletionDelay);
    vpResponses.add(vpAGCMCResponse_33);

    /**
     * If someone tried setting bin empty flag above, we are done.
     */
    if (vpAGCMCResponse_33.getRetrievalDataCompletionClassification(0) != COMPLETE_NORMAL ||
        vpAGCMCResponse_33.getRetrievalDataCompletionClassification(1) != COMPLETE_NORMAL)
    {
      try
      {
        String vsErrorCode = NO_ERROR;
        LoadData vpLoadData;
        int vnCompletion = vpAGCMCResponse_33.getRetrievalDataCompletionClassification(0);
        if (vnCompletion == COMPLETE_NORMAL)
        {
          vnCompletion = vpAGCMCResponse_33.getRetrievalDataCompletionClassification(1);
          vpLoadData = mpLoadServer.getLoad(mpAGCMCMessage.getRetrievalDataMCKey(1));
        }
        else
        {
          vpLoadData = mpLoadServer.getLoad(mpAGCMCMessage.getRetrievalDataMCKey(0));
        }
        if (vnCompletion != COMPLETE_CANCEL)
        {
          LocationData vpLocData = mpLocServer.getLocationRecord(
              vpLoadData.getWarehouse(), vpLoadData.getAddress());
          String vsMosID = getMOSIDForDevice(vpLocData.getDeviceID());
          switch (vnCompletion)
          {
            case COMPLETE_BIN_FULL:
              vsErrorCode = AGCMCMessage.ERROR_BIN_FULL_AT_LOCATION;
              break;
            case COMPLETE_BIN_EMPTY:
              vsErrorCode = AGCMCMessage.ERROR_BIN_EMPTY_AT_LOCATION;
              break;
            case COMPLETE_SIZE_MISMATCH:
              vsErrorCode = AGCMCMessage.ERROR_SIZE_MISMATCH;
              break;
          }
          setEquipmentOnline(STATUS_ERROR, vsErrorCode, vsMosID);
          addOneStatusMessage(vpResponses, vpDelays, vsMosID);
        }
      }
      catch (Exception e)
      {
        logger.logException(e);
      }

      int vnInitialDelay = vpDelays.remove(0);
      setSSTimerEvent(new AGCMCResponse(vpResponses, vpDelays), vnInitialDelay);
      return;
    }

    //
    //
    //
    if (vnRetDataTransClass == 2)
    {
      // Handle representative stations
      String vsDestStation = mpAGCMCMessage.getRetrievalDataDestinationStationNumber(0);
      String vsChildStn = mpStationServer.getReprStationChild(vsDestStation);
      if (vsChildStn.trim().length() > 0)
        vsDestStation = vsChildStn;

      // Does this station need an arrival?
      boolean vzSendArrival = needToSendArrival(vsDestStation);

      // Only send Trigger of Operation when arrivals are NOT required
      if (mzUseTriggerOfOperation && !vzSendArrival)
      {
        AGCMCMessage vpAGCMCResponse_68 = new AGCMCMessage();
        vpAGCMCResponse_68.setMCKey(mpAGCMCMessage.getRetrievalDataMCKey(0));
        vpAGCMCResponse_68.setDestinationStationNumber(vsDestStation);
        vpAGCMCResponse_68.setControlInformation(mpAGCMCMessage.getRetrievalDataControlInformation(0));
        vpAGCMCResponse_68.triggerOfOperationIndicationToString();

        vpDelays.add(retrieveTriggerOfOperationIndicationDelay);
        vpResponses.add(vpAGCMCResponse_68);
      }

      //
      // Next is arrivalReport (26)
      //
      // Only send if arrival is required
      //
      if (vzSendArrival || autoPressCompleteButton(vsDestStation))
      {
        AGCMCMessage vpAGCMCResponse_26 = new AGCMCMessage();
        vpAGCMCResponse_26.setMCKey(mpAGCMCMessage.getRetrievalDataMCKey(0));
        vpAGCMCResponse_26.setArrivalStationNumber(vsDestStation);
        vpAGCMCResponse_26.setDimensionInformation(mpAGCMCMessage.getRetrievalDataDimension(0));
        if (isUnitRetrieval(vsDestStation))
          vpAGCMCResponse_26.setLoadInformation(0);
        else
          vpAGCMCResponse_26.setLoadInformation(1);

		  vpAGCMCResponse_26.setBCData(mpAGCMCMessage.getRetrievalDataBCData(0));
        vpAGCMCResponse_26.setControlInformation(mpAGCMCMessage.getRetrievalDataControlInformation(0));
        vpAGCMCResponse_26.arrivalReportToString();

        int vnArrivalDelay;
        if(mpStationServer.isSimulationOn(vsDestStation))
          vnArrivalDelay = mpStationServer.getStation(vsDestStation).getSimInterval();
        else
          vnArrivalDelay = retrieveArrivalReportDelay;
        vpDelays.add(vnArrivalDelay);
        vpResponses.add(vpAGCMCResponse_26);
      }
    }
    else
    {
      //
      // Next is a operationCompletionReport (33) for Retrieval store.
      //
      AGCMCMessage vpAGCMCResponse_33b = new AGCMCMessage();
      for (int i = 0; i < 2; i++)
      {
        vpAGCMCResponse_33b.setRetrievalDataMCKey(i, mpAGCMCMessage.getRetrievalDataMCKey(i));
        vpAGCMCResponse_33b.setRetrievalDataTransportationClassification(i, mpAGCMCMessage.getRetrievalDataTransportationClassification(i));
        vpAGCMCResponse_33b.setRetrievalDataCategory(i, mpAGCMCMessage.getRetrievalDataCategory(i));
        vpAGCMCResponse_33b.setRetrievalDataCompletionClassification(i, 0); // 0 = Normal
        vpAGCMCResponse_33b.setRetrievalDataSourceStationNumber(i, mpAGCMCMessage.getRetrievalDataSourceStationNumber(i));
        vpAGCMCResponse_33b.setRetrievalDataDestinationStationNumber(i, mpAGCMCMessage.getRetrievalDataDestinationStationNumber(i));
        /*
         * For some unfathomable reason, the locations are REVERSED for the
         * shelf to shelf work complete.  It's just the way it is.
         */
        vpAGCMCResponse_33b.setRetrievalDataLocationNumber(i, mpAGCMCMessage.getRetrievalDataShelfToShelfLocationNumber(i));
        vpAGCMCResponse_33b.setRetrievalDataShelfPosition(i, mpAGCMCMessage.getRetrievalDataShelfToShelfPosition(i));
        vpAGCMCResponse_33b.setRetrievalDataShelfToShelfLocationNumber(i, mpAGCMCMessage.getRetrievalDataLocationNumber(i));
        vpAGCMCResponse_33b.setRetrievalDataShelfToShelfPosition(i, mpAGCMCMessage.getRetrievalDataShelfPosition(i));
        vpAGCMCResponse_33b.setRetrievalDataDimension(i, mpAGCMCMessage.getRetrievalDataDimension(i));
        vpAGCMCResponse_33b.setRetrievalDataBCData(i, mpAGCMCMessage.getRetrievalDataBCData(i));
        vpAGCMCResponse_33b.setRetrievalDataWorkNumber(i, mpAGCMCMessage.getRetrievalDataWorkNumber(i));
        vpAGCMCResponse_33b.setRetrievalDataControlInformation(i, mpAGCMCMessage.getRetrievalDataControlInformation(i));
      }
      //
      // Bin-to-Bin move - Need Bin-to-Bin Store (5)
      //
      vpAGCMCResponse_33b.setRetrievalDataTransportationClassification(0, 5);
      vpAGCMCResponse_33b.operationCompletionReportToString();

      vpDelays.add(retrieveTriggerOfOperationIndicationDelay);
      vpResponses.add(vpAGCMCResponse_33b);
    }

    int vnInitialDelay = vpDelays.remove(0);
    setSSTimerEvent(new AGCMCResponse(vpResponses, vpDelays), vnInitialDelay);
  }

  /**
   * Do we need to send an arrival?
   *
   * @param isStation
   * @return
   */
  protected boolean needToSendArrival(String isStation)
  {
    return mpStationServer.doesStationGetRetrieveArrival(isStation);
  }

  protected boolean isUnitRetrieval(String isStation)
  {
    boolean vzRtn = false;
    try
    {
      vzRtn = mpStationServer.isUnitRetrievalStation(isStation);
    }
    catch(DBException exc)
    {
    }

    return(vzRtn);
  }

  /**
   * Do we automatically press the completion button? Provided for
   * extensibility.
   *
   * @param isStation
   * @return
   */
  protected boolean autoPressCompleteButton(String isStation)
  {
    return false;
  }

  /*------------------------------------------------------------------------*/
  /**
   * MC ID 16
   * <BR>Start AGC - Need to respond with a 30, 30
   * <BR>Stop AGC - Need to respond with a 36
   */
  private void processSimultaneousStartStopCmd()
  {
    List<AGCMCMessage> vpResponses = new ArrayList<AGCMCMessage>();
    List<Integer>      vpDelays    = new ArrayList<Integer>();

    if (mpAGCMCMessage.getStartStopClassification() == 1) // 1 = start, 2 = stop
    {
      //
      // START Equipment.
      //
      if (!mzAGCOnline)
      {
        //
        // START Equipment - AGC is NOT Online - ALWAYS reply with a 36
        //
        AGCMCMessage vpAGCMCResponse_36 = new AGCMCMessage();
        vpAGCMCResponse_36.setInabiltyToStartReason(01); // Not in condition to start
        vpAGCMCResponse_36.simultaneousStartImproperReportToString(1); // 36

        setSSTimerEvent(new AGCMCResponse(vpAGCMCResponse_36), 40);
      }
      else
      {
        //
        // START Equipment - AGC IS Online - Only Reply ONCE.
        //
        if (!mzEquipmentOnline)
        {
          setEquipmentOnline(true);

          //
          // First is a MachineStatusReport.
          //
          addStatusMessages(vpResponses, vpDelays);

          //
          // Next is a MachineStatusReport.
          //
          AGCMCMessage vpAGCMCResponse_30b = new AGCMCMessage();
          vpAGCMCResponse_30b.setNumberOfReports(1);
          vpAGCMCResponse_30b.setMachineStatus(0,
                 masEquipmentIDs[0], manEquipmentModels[0], masEquipmentNumbers[0], 0, NO_ERROR);
          vpAGCMCResponse_30b.machineStatusReportToString();
          vpDelays.add(4000);
          vpResponses.add(vpAGCMCResponse_30b);

          //
          // Send the messages
          //
          int vnInitialDelay = vpDelays.remove(0);
          setSSTimerEvent(new AGCMCResponse(vpResponses, vpDelays), vnInitialDelay);
        }
      }
    }
    else
    {
      //
      // STOP Equipment.
      //
      if (mzAGCOnline)
      {
        //
        // STOP Equipment - AGC IS Online - Only reply if Equipment ONLINE.
        //
        if (mzEquipmentOnline)
        {
          //
          // Stop - Equipment is ONLINE.
          //
          setEquipmentOnline(false);

          //
          // First is a MachineStatusReport.
          //
          addStatusMessages(vpResponses, vpDelays);

          //
          // Next is a MachineStatusReport.
          //
          AGCMCMessage vpAGCMCResponse_30b = new AGCMCMessage();
          vpAGCMCResponse_30b.setNumberOfReports(1);
          vpAGCMCResponse_30b.setMachineStatus(0,
                 masEquipmentIDs[0], manEquipmentModels[0], masEquipmentNumbers[0], 1, NO_ERROR);
          vpAGCMCResponse_30b.setContinuationClassification(1);
          vpAGCMCResponse_30b.machineStatusReportToString();
          vpDelays.add(1000);
          vpResponses.add(vpAGCMCResponse_30b);

          //
          // Send the messages
          //
          int vnInitialDelay = vpDelays.remove(0);
          setSSTimerEvent(new AGCMCResponse(vpResponses, vpDelays), vnInitialDelay);
        }
      }
    }
  }

  /*------------------------------------------------------------------------*/
  /**
   * MC ID 19
   */
  private void processCommunicationTestRequest()
  {
    setDetailedControllerStatus("MC Comm Test Request (019) - Received");

    AGCMCMessage vpAGCMCResponse_39 = new AGCMCMessage();
    vpAGCMCResponse_39.setCommunicationTestTextRequest(mpAGCMCMessage.getCommunicationTestTextRequest());
    vpAGCMCResponse_39.setCommunicationTestTextResponse(mpAGCMCMessage.getCommunicationTestTextRequest());
    vpAGCMCResponse_39.responseToCommunicationTestRequestToString();

    setSSTimerEvent(new AGCMCResponse(vpAGCMCResponse_39), 100);
  }

  /*------------------------------------------------------------------------*/
  /**
   * MC ID 20
   */
  private void processResponseToCommunicationTestRequest()
  {
    logger.logDebug("MC ID 20: Need Some Code!!!");
  }

  /*------------------------------------------------------------------------*/
  /**
   * MC ID 41.  Need to reply with a 63.  This message is normally a response from the MC
   * when the AGC initiates a mode change request to the MC.  Normal message sequence:
   * <pre>
   *   AGC         MC
   *   61   -----&gt;
   *       &lt;-----  41
   *   63   -----&gt;
   * </pre>
   */
  protected void processResponseToOperationModeChangeRequest()
  {
                                       // Station requesting mode change.
    if (mpAGCMCMessage.getRequestResponse() == 0)
    {
      String vsStation = mpAGCMCMessage.getOperationModeChangeStation();
      /*
       * Was the station changed to Store mode with a 61 earlier?  If not,
       * assume Retrieve Mode.
       */
      int vnCompletionMode = (mpStationServer.isStationInStoreMode(vsStation)) ? 1 : 2;
      AGCMCMessage vpAGCMCResponse_63 = new AGCMCMessage();
      vpAGCMCResponse_63.setOperationModeChangeStation(vsStation);
      vpAGCMCResponse_63.setCompletionMode(vnCompletionMode);
      vpAGCMCResponse_63.operationModeChangeCompletionReportToString();
      setSSTimerEvent(new AGCMCResponse(vpAGCMCResponse_63), 500);
    }
  }

  /*------------------------------------------------------------------------*/
  /**
   * MC ID 42
   * <BR>Need to reply with a 62, 63
   */
  private void processOperationModeChangeCmd()
  {
    String vsMode = TRANSPORT_MODE_NA;
    //
    // First is ResponseToOpModeChangeCmd (62).
    //
    AGCMCMessage vpAGCMCResponse_62 = new AGCMCMessage();
    vpAGCMCResponse_62.setOperationModeChangeStation(mpAGCMCMessage.getOperationModeChangeStation());
    vpAGCMCResponse_62.setRequestClassification(0); // Normal
    vpAGCMCResponse_62.responseToOperationModeChangeCmdToString();

    //
    // Next is a operationModeChangeCompletionReport (63).
    //
    AGCMCMessage vpAGCMCResponse_63 = new AGCMCMessage();
    int vnModeChangeCmd = mpAGCMCMessage.getOperationModeChangeCmd();
    int vnCompletionMode = 0;
    switch (vnModeChangeCmd)
    {
      case 1:
      case 2:
        vnCompletionMode = 1;
        vsMode = TRANSPORT_MODE_STORE;
        break;
      case 3:
      case 4:
        vnCompletionMode = 2;
        vsMode = TRANSPORT_MODE_RETRIEVE;
        break;
      default:
        logger.logError("Unknown Mode Change Command for "
            + mpAGCMCMessage.getOperationModeChangeStation() + ": "
            + vnModeChangeCmd);
        return;
    }
    updateTransportMode(mpAGCMCMessage.getOperationModeChangeStation(), vsMode);

    vpAGCMCResponse_63.setOperationModeChangeStation(mpAGCMCMessage.getOperationModeChangeStation());
    vpAGCMCResponse_63.setCompletionMode(vnCompletionMode);
    vpAGCMCResponse_63.operationModeChangeCompletionReportToString();

    //
    //  Send the messages
    //
    setSSTimerEvent(new AGCMCResponse(vpAGCMCResponse_62, 1000, vpAGCMCResponse_63), 60);
  }

  /*------------------------------------------------------------------------*/
  /**
   * MC ID 45
   * need to respond with a dummy arrival
   */
  protected void processMCOperationComplete()
  {
    LoadData vpLoadData = mpLoadServer.getLoad(mpAGCMCMessage.getMCKey());
    StationData vpStationData = mpStationServer.getStation(
        mpAGCMCMessage.getTerminalOperationCompleteStationNumber());
    if(vpLoadData != null)
    {
      if(vpStationData != null)
      {
        AGCMCMessage vpAGCMCResponse_26 = new AGCMCMessage();
        vpAGCMCResponse_26.setMCKey(AGCDeviceConstants.AGCDUMMYLOAD);
        vpAGCMCResponse_26.setArrivalStationNumber(vpStationData.getStationName());
        vpAGCMCResponse_26.setDimensionInformation(vpLoadData.getHeight());
        vpAGCMCResponse_26.setLoadInformation(1);
        vpAGCMCResponse_26.setBCData(vpLoadData.getBCRData());
        vpAGCMCResponse_26.arrivalReportToString();
        setSSTimerEvent(new AGCMCResponse(vpAGCMCResponse_26), 1000);
      }
      else
      {
        logger.logError("Station: " + mpAGCMCMessage.getSourceStationNumber() +
            " does not exist");
      }
    }
    else
    {
      logger.logError("Load: " + mpAGCMCMessage.getMCKey() +
          " does not exist AGCDeviceEmulator.processMCOperationComplete");
    }
  }

  /*------------------------------------------------------------------------*/
  /**
   * MC ID 46
   */
  private void processResponseToRetrievalTrigger()
  {
    logger.logDebug("MC ID 46: Need Some Code!!!");
  }

  /*------------------------------------------------------------------------*/
  /**
   * MC ID 47
   */
  private void processRequestRetrievalTriggerRepetition()
  {
    logger.logDebug("MC ID 47: Need Some Code!!!");
  }

  /**
   * MC ID 50
   * Method stub to be implemented in extended class
   */
  protected void process50Message()
  {
    logger.logDebug("MC ID 50: Need Some Code!!!");
  }

  /*------------------------------------------------------------------------*/
  /**
   * MC ID 51
   */
  private void processRequestAccessImpossibleLocations()
  {
    logger.logDebug("MC ID 51: Need Some Code!!!");
  }
  
  /**
   * MC ID 54
   * Method stub to be implemented in extended class
   */
  private void processDOOutputInstruction()
  {
    logger.logDebug("MC ID 51: Need Some Code!!!");
  }

  /*------------------------------------------------------------------------*/
  /**
   * MC ID 58
   */
  private void processRequestToStartSystemRecovery()
  {
    //
    // Respond with a responseToRequestToStartSystemRecovery.
    //
    logger.logDebug("MC ID 58: Need Some Code!!!");
  }

  /*------------------------------------------------------------------------*/
  /**
   * MC ID 59
   */
  private void processRequestToTerminateSystemRecovery()
  {
    //
    // Respond with a responseToRequestToTerminateSystemRecovery.
    //
    logger.logDebug("MC ID 59: Need Some Code!!!");
  }

  /*========================================================================*/
  /*  MOS Messages                                                          */
  /*========================================================================*/

  /**
   * Process messages from the Monitoring and Operating support System ("MOS")
   * AGC communication Port.
   */
  void processEquipmentMosEvent()
  {
    mpAGCMOSMessage.toDataValues(receivedText);
    if ((mpAGCMOSMessage != null) && (!mpAGCMOSMessage.getValidMessage()))
    {
      String s = mpAGCMOSMessage.getInvalidMessageDescription();
      logger.logRxEquipmentMessage(receivedText, s);
      logger.logError("AGCDeviceEmulator.processEquipmentMosEvent() -- " + s);
    }
    else
    {
      String s = mpAGCMOSMessage.getParsedMessageString();
      logger.logRxEquipmentMessage(receivedText, s);
      logger.logDebug("Msg ID: " + s);
      switch (mpAGCMOSMessage.getID())
      {
        case  1: processEquipmentStatusRequest(); break;
        case  2: processTransportQuantityRequest(); break;
        case  3: processTransportDataRequest(); break;
        case 19: processConnectionInformation(); break;
        case 21: processStartupStopCommand(); break;
        case 22: processAlarmResetCommand(); break;
        case 23: processErrorResetCommand(); break;
        case 24: processDisconnectionRecoveryDataRequest(); break;
        case 25: processDisconnectionRecoveryCommand(); break;
        case 34: processDateAndTimeCorrectionRequest(); break;
        case 41: processTransportDataChangeCommand(); break;
        case AGCMOSMessage.LATCH_CLEAR: processLatchClearCommand(); break;
        case 51: processBarCodeDataCommand(); break;
        case 61: processSystemModeChangeRequest(); break;
        case 66: processLogDataSaveCommand(); break;
        case 80: processMOSCommunicationTestRequest(); break;
        default:
          logger.logError("Msg ID: " + mpAGCMOSMessage.getID() +
                          " NOT Processed - AGCDeviceEmulator.processEquipmentMosEvent()");
          break;
      }
    }
  }

  /*------------------------------------------------------------------------*/
  /**
   * MOS ID 001
   */
  private void processEquipmentStatusRequest()
  {
    logger.logDebug("     001 - processEquipmentStatusRequest()");
    setDetailedControllerStatus("MOS Eqpmnt Status Request (001) - Received");

    //
    // Only response is equipmentStatusReport (101)
    //
    // TODO: Limit to only the requested equipment
    sendMosStatusMessages();
  }

  /*------------------------------------------------------------------------*/
  /**
   * MOS ID 002
   */
  private void processTransportQuantityRequest()
  {
    AGCMOSMessage vpAGCMOSResponse = new AGCMOSMessage();
    for (String s : getEquipmentList())
    {
      String vsModel = s.substring(1,3);
      if (vsModel.equals("11") || vsModel.equals("21")) // crane/conveyor
      {
        // Sure, it's random, but it's better than nothing for testing
        vpAGCMOSResponse.addEquipmentTransportDataQuantities(s,
            (int)(Math.random() * 10));
      }
    }
    vpAGCMOSResponse.transportDataQuantityReportToString();

    setSSTimerEvent(new AGCMOSResponse(vpAGCMOSResponse), 20);
  }


  /*------------------------------------------------------------------------*/
  /**
   * MOS ID 003
   */
  private void processTransportDataRequest()
  {
    String vsStation = getStationID(mpAGCMOSMessage.getTransportDataEquipment());
    if (vsStation != null)
    {
      AGCMOSMessage vpAGCMOSResponse = new AGCMOSMessage();
      vpAGCMOSResponse.setTransportDataEquipment(mpAGCMOSMessage.getTransportDataEquipment());
      String vsLoadID = "EMUL-" + mpAGCMOSMessage.getTransportDataEquipment().substring(3);

      if( vsStation.startsWith("SR"))
      {
    	  String vsTransportClass = "2";
          vpAGCMOSResponse.addTransportDataReport("000", "5", "0001", "5", "101",
                  vsLoadID, vsTransportClass, "1", "0", "0", "0", "0", "000", "9000",
                  vsStation, "9000", "0101", "003002001000", "000000000000", "01",
                  "BARCODE-" + vsLoadID + "              ", vsLoadID,
                  "000000000000000000000000000000");
      }
      else
      {
       String vsTransportClass = "" + vsStation.charAt(1);
       if (vsStation.charAt(1) == '1')
       {
        vpAGCMOSResponse.addTransportDataReport("000", "5", "0001", "5", "101",
            vsLoadID, vsTransportClass, "1", "0", "0", "0", "0", "000", vsStation,
            vsStation, "9000", "0101", "001002003000", "000000000000", "01",
            "BARCODE-" + vsLoadID + "              ", vsLoadID,
            "000000000000000000000000000000");
       }
       else
       {
        vsTransportClass = "2";
        vpAGCMOSResponse.addTransportDataReport("000", "5", "0001", "5", "101",
            vsLoadID, vsTransportClass, "1", "0", "0", "0", "0", "000", "9000",
            vsStation, "9000", "0101", "003002001000", "000000000000", "01",
            "BARCODE-" + vsLoadID + "              ", vsLoadID,
            "000000000000000000000000000000");
       }
      }
      vpAGCMOSResponse.transportDataReportToString();

      setSSTimerEvent(new AGCMOSResponse(vpAGCMOSResponse), 20);
    }
  }


  /*--------------------------------------------------------------------------*/
  /**
   * MOS ID 019
   * - Send Status Reports for all processes and equipment.
   */
  private void processConnectionInformation()
  {
    logger.logDebug("     019 - processConnectionInformation()");
    setDetailedControllerStatus("MOS Connection Information (019) - Received");

    sendMosStatusMessages();
  }

  /**
   * MOS ID 101
   * Send status messages over the MOS port, using internal device statuses
   */
  private void sendMosStatusMessages()
  {
    List<String> vpEquipList = getEquipmentList();
    if (vpEquipList.size() > 0)
    {
      List<AGCMOSMessage> vpResponses = new ArrayList<AGCMOSMessage>();
      List<Integer>       vpDelays    = new ArrayList<Integer>();

      AGCMOSMessage vpAGCMOSResponse = new AGCMOSMessage();
      vpAGCMOSResponse.setSystemMode("9");

      for (String vsEquip : vpEquipList)
      {
        /*
         * Check to see if we're maintaining status for this piece of equipment.
         * If so, use its status.
         * If not, default to overall status.
         */
        int vnOperationalStatus = mzEquipmentOnline ? 1 : 2;
        String vsErrorCode = NO_ERROR;
        boolean vzNotDone = true;
        for (int i = 0; i < mnEquipmentCount && vzNotDone; i++)
        {
          String vsMCID = mpMachineStatusAGCMCMessage.getMachineStatusMachineId(i);
          if (vsEquip.equals(AGCMOSMessage.getMOSIDfromMCID(vsMCID)))
          {
            vnOperationalStatus = mpMachineStatusAGCMCMessage.getMachineStatusStatus(i);
            vnOperationalStatus = AGCMOSMessage.getMOSStatusFromMCStatus(vnOperationalStatus);

            vsErrorCode = mpMachineStatusAGCMCMessage.getMachineStatusErrorCode(i);
            vzNotDone = false;
          }
        }

        String vsEquipmentStatus;
        String vsModel = vsEquip.substring(1,3);
        if (vsModel.equals("01"))
        {
          vsEquipmentStatus = "0" + vnOperationalStatus + "000000" + vsErrorCode;
        }
        else if (vsModel.equals("11"))
        {
          vsEquipmentStatus = "1" + vnOperationalStatus + "120000" + vsErrorCode;
        }
        else if (vsModel.equals("21"))
        {
          String vsTransportMode = getTransportMode(vsEquip);
          vsEquipmentStatus = "1" + vnOperationalStatus + "100"
                  + vsTransportMode + "00" + vsErrorCode;
        }
        else
        {
          vsEquipmentStatus = "0" + vnOperationalStatus + "100000" + vsErrorCode;
        }
        vpAGCMOSResponse.addEquipmentStatusReport(vsEquip + vsEquipmentStatus);
      }

      vpAGCMOSResponse.equipmentStatusReportToString();
      vpDelays.add(20);
      vpResponses.add(vpAGCMOSResponse);
      setSSTimerEvent(new AGCMOSResponse(vpResponses, vpDelays), 200);
    }
    else
    {
       logger.logError("NO Configuration \"EquipmentList\" - startup()");
    }
  }

  /**
   * Keep track of transport modes
   *
   * @param isStation
   * @param inCompletionMode
   */
  protected void updateTransportMode(String isStation, String isMode)
  {
    if (mpBidirectionalStatuses.get(isStation) != null)
    {
      mpBidirectionalStatuses.remove(isStation);
    }
    mpBidirectionalStatuses.put(isStation, isMode);
  }

  /**
   * Get the transport mode (mostly for bidirectional stations)
   *
   * @param isEquipID
   * @return
   */
  protected String getTransportMode(String isEquipID)
  {
    String vsStationID = getStationID(isEquipID);
    String vsTransportMode = null;
    if (vsStationID != null)
    {
      vsTransportMode = mpBidirectionalStatuses.get(vsStationID);
    }
    if (vsTransportMode == null)
    {
      vsTransportMode = TRANSPORT_MODE_NA;
    }
    return vsTransportMode;
  }

  /*------------------------------------------------------------------------*/
  /**
   * MOS ID 021
   */
  private void processStartupStopCommand()
  {
    logger.logDebug("     021 - processStartupStopCommand()");
    setDetailedControllerStatus("MOS Startup/Stop Cmd (021) - Received");
    //
    // Response is startupStopResponse (121)
    //
    AGCMOSMessage vpAGCMOSResponse = new AGCMOSMessage();
    vpAGCMOSResponse.setModelCode(mpAGCMOSMessage.getModelCode());
    vpAGCMOSResponse.setStartupStopAllEquipment(mpAGCMOSMessage.isStartCommand());
    vpAGCMOSResponse.startupStopResponseToString();

    setSSTimerEvent(new AGCMOSResponse(vpAGCMOSResponse), 20);

    // Update statuses
    for (String s : mpAGCMOSMessage.getEquipment())
    {
      if (s.startsWith("199")) // Supervisor
      {
        setEquipmentOnline(mpAGCMOSMessage.isStartCommand());
      }
      else
      {
        setEquipmentOnline(mpAGCMOSMessage.isStartCommand() ? STATUS_ONLINE
            : STATUS_OFFLINE, NO_ERROR, s);
      }
      sendMosStatusMessages();
    }
  }

  /*------------------------------------------------------------------------*/
  /**
   * MOS ID 022
   */
  private void processAlarmResetCommand()
  {
    logger.logDebug("     022 - processAlarmResetCommand()");
    setDetailedControllerStatus("MOS Alarm Reset Cmd (022) - Received");
    //
    // No response needed...?
    //
  }

  /*------------------------------------------------------------------------*/
  /**
   * MOS ID 023
   */
  private void processErrorResetCommand()
  {
    logger.logDebug("     023 - processErrorResetCommand()");
    setDetailedControllerStatus("MOS Error Reset Cmd (023) - Received");
    //
    // No response needed...?
    //
  }

  /*------------------------------------------------------------------------*/
  /**
   * MOS ID 024
   */
  private void processDisconnectionRecoveryDataRequest()
  {
    String s = mpAGCMOSMessage.getDisconnectionRecoveryClassificationParsed(
        mpAGCMOSMessage.getDisconnectionRecoveryClassification());
    setDetailedControllerStatus("MOS " + s + " Data Request (024) - Received");
    //
    // Only response is disconnectionRecoveryDataReport (124)
    //
    AGCMOSMessage vpAGCMOSResponse = new AGCMOSMessage();
    vpAGCMOSResponse.setDisconnectionRecoveryClassification(
        mpAGCMOSMessage.getDisconnectionRecoveryClassification());
    vpAGCMOSResponse.setMachineId(mpAGCMOSMessage.getMachineId());
    vpAGCMOSResponse.setDataProcessClassification("9999");
    vpAGCMOSResponse.disconnectionRecoveryDataReportToString();

    setSSTimerEvent(new AGCMOSResponse(vpAGCMOSResponse), 20);
  }

  /*------------------------------------------------------------------------*/
  /**
   * MOS ID 025
   */
  private void processDisconnectionRecoveryCommand()
  {
    String s = mpAGCMOSMessage.getDisconnectionRecoveryClassificationParsed(
        mpAGCMOSMessage.getDisconnectionRecoveryClassification());
    setDetailedControllerStatus("MOS " + s + " Cmd (025) - Received");
    //
    // Only response is disconnectionRecoveryCommandResponse (125)
    //
    AGCMOSMessage vpAGCMOSResponse = new AGCMOSMessage();
    vpAGCMOSResponse.setDisconnectionRecoveryClassification(
        mpAGCMOSMessage.getDisconnectionRecoveryClassification());
    vpAGCMOSResponse.setMachineId(mpAGCMOSMessage.getMachineId());
    vpAGCMOSResponse.setProcessClassification(0);
    vpAGCMOSResponse.setDataProcessClassification("0");
    vpAGCMOSResponse.setResponseFlag(0);
    vpAGCMOSResponse.disconnectionRecoveryCommandResponseToString(mpAGCMOSMessage.getDisconnectRecoverCmds());

    setSSTimerEvent(new AGCMOSResponse(vpAGCMOSResponse), 20);
  }

  /*------------------------------------------------------------------------*/
  /**
   * MOS ID 034
   */
  private void processDateAndTimeCorrectionRequest()
  {
    logger.logDebug("     034 - processCommunicationTestRequest()");
    setDetailedControllerStatus("MOS Date/Time Correction Request (034) - Received");
    //
    // Only response is dateAndTimeReport (134)
    //
    AGCMOSMessage vpAGCMOSResponse = new AGCMOSMessage();
    vpAGCMOSResponse.dateAndTimeReportToString();

    setSSTimerEvent(new AGCMOSResponse(vpAGCMOSResponse), 20);
  }

  /*------------------------------------------------------------------------*/
  /**
   * MOS ID 041
   */
  private void processTransportDataChangeCommand()
  {
    logger.logDebug("     041 - processTransportDataChangeCommand()");
    setDetailedControllerStatus("MOS TransportDataChangeCommand (041) - Received");
    //
    // Only response is TransportDataChangeCommandResponse (141)
    //
    AGCMOSMessage vpAGCMOSResponse = new AGCMOSMessage();
    vpAGCMOSResponse.setMachineId(mpAGCMOSMessage.getMachineId());
    vpAGCMOSResponse.setMCKey(mpAGCMOSMessage.getMCKey());
    vpAGCMOSResponse.transportDataChangeCommandResponseToString();

    setSSTimerEvent(new AGCMOSResponse(vpAGCMOSResponse), 20);
  }

  /*------------------------------------------------------------------------*/
  /**
   * MOS ID 049
   */
  private void processLatchClearCommand()
  {
    logger.logDebug("     049 - processLatchClearCommand()");
    setDetailedControllerStatus("MOS Latch Clear Command (049) - Received");
    //
    // No response
    //
  }

  /*------------------------------------------------------------------------*/
  /**
   * MOS ID 051
   */
  private void processBarCodeDataCommand()
  {
    logger.logDebug("     051 - processBarCodeDataCommand()");
    setDetailedControllerStatus("MOS Bar Code Data Command (051) - Received");
    //
    // Only response is barCodeDataResponse (151)
    //
    AGCMOSMessage vpAGCMOSResponse = new AGCMOSMessage();
    String s = mpAGCMOSMessage.getMachineId();
    vpAGCMOSResponse.setMachineId(s);
    vpAGCMOSResponse.barCodeDataResponseToString();

    setSSTimerEvent(new AGCMOSResponse(vpAGCMOSResponse), 20);
  }

  /*------------------------------------------------------------------------*/
  /**
   * Process ID 61
   */
  private void processSystemModeChangeRequest()
  {
    setDetailedControllerStatus("MOS System Mode Change Request (061) - Received");

    //
    // Only response is System Mode Change Response (161)
    //
    AGCMOSMessage vpAGCMOSResponse = new AGCMOSMessage();
    String s = mpAGCMOSMessage.getSystemMode();
    vpAGCMOSResponse.setSystemMode(s);
    vpAGCMOSResponse.setResponseFlag(0);
    vpAGCMOSResponse.systemModeChangeResponseToString();

    setSSTimerEvent(new AGCMOSResponse(vpAGCMOSResponse), 20);
  }

  /*------------------------------------------------------------------------*/
  /**
   * MOS ID 066
   */
  private void processLogDataSaveCommand()
  {
    setDetailedControllerStatus("MOS Log Data Save Cmd (066) - Received");
    //
    // Only response is LogDataSaveCommandResponse (166)
    //
    AGCMOSMessage vpAGCMOSResponse = new AGCMOSMessage();
    vpAGCMOSResponse.logDataSaveCommandResponseToString();

    setSSTimerEvent(new AGCMOSResponse(vpAGCMOSResponse), 20);
  }

  /*------------------------------------------------------------------------*/
  /**
   * MOS ID 080
   */
  private void processMOSCommunicationTestRequest()
  {
    setDetailedControllerStatus("MOS Comm Test Request (080) - Received");
    //
    // Only response is communicationTestResponse (180)
    //
    AGCMOSMessage vpAGCMOSResponse = new AGCMOSMessage();
    vpAGCMOSResponse.setCommunicationTestTextResponse(
        mpAGCMOSMessage.getCommunicationTestTextRequest());
    vpAGCMOSResponse.setCommunicationTestResponse(0);
    vpAGCMOSResponse.communicationTestResponseToString();

    setSSTimerEvent(new AGCMOSResponse(vpAGCMOSResponse), 500);
  }

  /*========================================================================*/
  /*  Give the data to be transmitted to the Device/Equipment to the Port.  */
  /*========================================================================*/

  /**
   * Transmit an MC message
   */
  protected void transmitMessageToMCDevice(AGCMessage msg)
  {
    String s = msg.getParsedMessageString();
    logger.logDebug(s);
    if (msg.getValidMessage())
    {
      logger.logTxEquipmentMessage(msg.getMessageAsString(), s);
      publishEquipmentEvent(msg.getMessageAsString(), 0);
    }
    else
    {
      logger.logError("AGCDeviceEmulator.transmitMessageToMCDevice() -- " +
          msg.getInvalidMessageDescription() + " -- \"" + msg.getMessageAsString() + "\"");
    }
  }

  /**
   * Transmit a MOS message
   */
  private void transmitMessageToMOSDevice(AGCMessage msg)
  {
    if (msEquipmentPortCKN2 != null)
    {
      String s = msg.getParsedMessageString();
      logger.logDebug("Msg ID: " + s);
      if (msg.getValidMessage())
      {
        logger.logTxEquipmentMessage(msg.getMessageAsString(), s);
        publishEquipmentEvent(msg.getMessageAsString(), 0, msEquipmentPortCKN2);
      }
      else
      {
        logger.logError("AGCDeviceEmulator.transmitMessageToMOSDevice() -- " +
            msg.getInvalidMessageDescription() + " -- \"" + msg.getMessageAsString() + "\"");
      }
    }
  }

  /*========================================================================*/
  /*  TimerTasks for message sending                                        */
  /*========================================================================*/

  /**
   * Handles MC Responses
   * <BR>
   * The task's run() is synchronized on thisController so that any work we
   * do here is not interrupted by any incoming messages or events that we
   * generate here.  We want to complete anything we do here without being
   * pre-empted.
   */
  protected class AGCMCResponse extends TimerTask
  {
    List<AGCMCMessage> mpAGCMCMessages = new ArrayList<AGCMCMessage>();
    List<Integer>      mpDelays        = new ArrayList<Integer>();

    public AGCMCResponse(List<AGCMCMessage> ipAGCMCMessages, List<Integer> ipDelays)
    {
      mpAGCMCMessages = ipAGCMCMessages;
      mpDelays        = ipDelays;
    }

    public AGCMCResponse(Object... iapParams)
    {
      int vnParams = iapParams.length;
      for (int i = 0; i < vnParams; i++)
      {
        if (iapParams[i] instanceof AGCMCMessage)
        {
          mpAGCMCMessages.add((AGCMCMessage)iapParams[i]);
        }
        else if (iapParams[i] instanceof Integer)
        {
          mpDelays.add((Integer)iapParams[i]);
        }
      }
    }

    @Override
    public void run()
    {
      transmitMessageToMCDevice(mpAGCMCMessages.remove(0));
      if (mpAGCMCMessages.size() > 0)
      {
        int vnDelay = mpDelays.remove(0);
        setSSTimerEvent(new AGCMCResponse(mpAGCMCMessages, mpDelays), vnDelay);
      }
    }
  }

  /**
   * Handles MOS Responses
   * <BR>
   * The task's run() is synchronized on thisController so that any work we
   * do here is not interrupted by any incoming messages or events that we
   * generate here.  We want to complete anything we do here without being
   * pre-empted.
   */
  protected class AGCMOSResponse extends TimerTask
  {
    List<AGCMOSMessage> mpAGCMOSMessages = new ArrayList<AGCMOSMessage>();
    List<Integer>       mpDelays         = new ArrayList<Integer>();

    public AGCMOSResponse(List<AGCMOSMessage> ipAGCMOSMessages, List<Integer> ipDelays)
    {
      mpAGCMOSMessages = ipAGCMOSMessages;
      mpDelays        = ipDelays;
    }

    public AGCMOSResponse(Object... iapParams)
    {
      int vnParams = iapParams.length;
      for (int i = 0; i < vnParams; i++)
      {
        if (iapParams[i] instanceof AGCMOSMessage)
        {
          mpAGCMOSMessages.add((AGCMOSMessage)iapParams[i]);
        }
        else if (iapParams[i] instanceof Integer)
        {
          mpDelays.add((Integer)iapParams[i]);
        }
      }
    }

    @Override
    public void run()
    {
      transmitMessageToMOSDevice(mpAGCMOSMessages.remove(0));
      if (mpAGCMOSMessages.size() > 0)
      {
        int vnDelay = mpDelays.remove(0);
        setSSTimerEvent(new AGCMOSResponse(mpAGCMOSMessages, mpDelays), vnDelay);
      }
    }
  }

  /**
   * Way to speed up or slow down flow of messages
   *
   * @param task
   * @param delay
   */
  public void setSSTimerEvent(TimerTask ipTask, int inDelay)
  {
    mpTimer.schedule(ipTask, inDelay/mnSpeedFactor);
  }

  /*========================================================================*/
  /*  Factory for ControllerImplFactory                                     */
  /*========================================================================*/

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
    Controller vpController = Factory.create(AGCDeviceEmulator.class);
    vpController.setEquipmentPortCKN(ipConfig.getString(DEVICE_PORT));
    return vpController;
  }

}
