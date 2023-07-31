package com.daifukuamerica.wrxj.emulation.arc9y;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.device.arc9y.Arc9yMessage;
import com.daifukuamerica.wrxj.emulation.DeviceEmulator;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.messageformat.controlevent.ControlEventDataFormat;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A Device Controller that emulates a Daifuku ARC. The ARC monitors the status
 * of the AS/RS and its peripheral equipment, and schedules transfer operations
 * requested by the ARC Device Controller.
 * 
 * @author Stephen Kendorski   (version 1.0)
 * @author Michael Andrus      (version 2.0)
 */
public class Arc9yDeviceEmulator extends DeviceEmulator
{
  protected StandardStationServer mpStationServer;
  
  protected Timer mpTimer;
  
  protected int storePickupCompletionDelay = 4900;
  protected int storeCompletionDelay = 5900;
  protected int retrieveCompletionPickupDelay = 6000;
  protected int retrieveCompletionDepositDelay = 1400;
  protected int retrieveArrivalReportDelay = 2400;

  private final int STATUSREPORTSPERMESSAGE = 9;

  protected Arc9yMessage  mpArc9yMessage  = Factory.create(Arc9yMessage.class, true);
  
  protected boolean mzArc9yOnline = false;
  protected boolean mzEquipmentOnline = false;

  protected String msArc9yDeviceId = null;
  
  /**
   * The unique <i>String</i> key that identifies an instantiated Controller's
   * SECOND communication port.
   */
  private String msEquipmentPortCKN2 = null;

  private int mnMCPortStatus  = ControllerConsts.STATUS_UNKNOWN;
  private int mnMCPort2Status = ControllerConsts.STATUS_UNKNOWN;

  protected Arc9yMessage mpMachineStatusArc9yMessage = Factory.create(Arc9yMessage.class, true);

  protected int MAX_EQUIPMENT = 650;
  protected String[] masEquipmentIDs        = new String[MAX_EQUIPMENT];
  protected int[]    manEquipmentModels     = new int[MAX_EQUIPMENT];
  protected String[] masEquipmentNumbers    = new String[MAX_EQUIPMENT];
  protected int      mnEquipmentCount       = 0;

  private String[] stationsRequireArrival = null;
  
  private int mnSpeedFactor = 1;

  public Arc9yDeviceEmulator()  // This has to be public for the Factory to work
  {
  }

  /*========================================================================*/
  /*  Controller methods                                                    */
  /*========================================================================*/
  
  @Override
  public void initialize(String aControllerKeyName)
  {
    super.initialize(aControllerKeyName);
    logger.logDebug("Arc9yDeviceEmulator.initialize() - Start");
    logger.addEquipmentLogger();
    msEquipmentPortCKN2 = getConfigProperty(DEVICE_PORT2);
    if (msEquipmentPortCKN2 == null)
    {
      logger.logDebug("Missing DevicePort2 (Device May Not Need One)");
      mnMCPort2Status = ControllerConsts.STATUS_RUNNING;
    }
    else
    {
      logger.logDebug("DevicePort2 \"" + msEquipmentPortCKN2 + "\"");
      mnMCPort2Status = ControllerConsts.STATUS_UNKNOWN;
      publishRequestEvent(msEquipmentPortCKN2);
    }
    subscribeStatusEvent(equipmentPortCKN);
    subscribeEquipmentEvent(equipmentPortCKN);
    if (msEquipmentPortCKN2 != null)
    {
      subscribeStatusEvent(msEquipmentPortCKN2);
      subscribeEquipmentEvent(msEquipmentPortCKN2);
    }
    subscribeControlEvent();
    
    msArc9yDeviceId = getConfigProperty(DEVICE_ID);
    if (msArc9yDeviceId == null)
    {
      logger.logError("Missing 9YDeviceID configuration");
    }
    else
    {
      logger.logDebug("9YDeviceID \"" + msArc9yDeviceId + "\"");
    }
    String vsArrivalStations = getConfigProperty("StationsRequireArrival");
    if (vsArrivalStations != null)
    {
      stationsRequireArrival = SKDCUtility.getTokens(vsArrivalStations, ",");
    }
    if ((vsArrivalStations == null) || (stationsRequireArrival.length == 0))
    {
      logger.logDebug("StationsRequireArrival are not defined.");
    }

    logger.logDebug("Arc9yDeviceEmulator.initialize() - End");
  }

  /*--------------------------------------------------------------------------*/
  @Override
  public void startup()
  {
    super.startup();
    logger.logDebug("Arc9yDeviceEmulator.startup() - Start");

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
    retrieveCompletionPickupDelay = getConfigPropertyAsInt("RetrieveCompletionPickupDelay", retrieveCompletionPickupDelay);
    retrieveCompletionDepositDelay = getConfigPropertyAsInt("RetrieveCompletionDepositDelay", retrieveCompletionDepositDelay);
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

    logger.logDebug("Arc9yDeviceEmulator.startup() - End");
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
    logger.logDebug("Arc9yDeviceEmulator.shutdown() -- Start");
    timers.cancel();
    mpTimer.cancel();
    mpMachineStatusArc9yMessage  = null;
    mpArc9yMessage = null;
    msEquipmentPortCKN2 = null;
    masEquipmentIDs = null;
    manEquipmentModels = null;
    masEquipmentNumbers = null;
    mpStationServer.cleanUp();
    mpStationServer = null;
    logger.logDebug("Arc9yDeviceEmulator.shutdown() -- End");
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
          if (receivedCKN.equals(equipmentPortCKN))
          {
            //
            // The message is from the Material handling Computer ("MC") Arc9y
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
            // Arc9y communication Port.
            //
            if (mnMCPort2Status == receivedData)
            {
              break;
            }
            else
            {
              //
              // Status of our Port has changed.
              //
              logger.logDebug("StatusChange (from Port) - WAS " +
                              ControllerConsts.STATUS_TEXT[mnMCPort2Status] + "  NOW " +
                              ControllerConsts.STATUS_TEXT[receivedData]);
              mnMCPort2Status = receivedData;
            }
          }
          if ((mnMCPortStatus == ControllerConsts.STATUS_RUNNING) &&
              (mnMCPort2Status == ControllerConsts.STATUS_RUNNING))
          {
            setControllerStatus(ControllerConsts.STATUS_RUNNING);
          }
          else if ((mnMCPortStatus == ControllerConsts.STATUS_ERROR) ||
                   (mnMCPort2Status == ControllerConsts.STATUS_ERROR))
          {
            setControllerStatus(ControllerConsts.STATUS_ERROR_PORT);
          }
          else if (controllerStatus == ControllerConsts.STATUS_ERROR_PORT)
          {
            setControllerStatus(ControllerConsts.STATUS_WAIT_PORT);
          }
          break;
        default:
          logger.logError("Arc9yDeviceEmulator.processStatusEvent() -- UNKNOWN Event Type \""
                + chr0 + "\" -- processStatusEvent()");
      }
    }
    catch(StringIndexOutOfBoundsException e)
    {
      logger.logSparseException(e, "Arc9yDeviceEmulator.processStatusEvent() - \"" + receivedText + "\"");
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
        default:
          logger.logError("UNKNOWN Event Type \"" + receivedData
              + "\" -- Arc9yDeviceEmulator.processControlEvent()");
      }
    }
    catch(StringIndexOutOfBoundsException e)
    {
      logger.logSparseException(e, "Arc9yDeviceEmulator.processControlEvent() - \"" 
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
    //
    // The message is from the Material handling Computer ("MC") ARC
    // communication Port.
    //
    processEquipmentMCEvent();
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
    
    mzArc9yOnline = izOnline;
    mzEquipmentOnline = izOnline;
    
    mpMachineStatusArc9yMessage.setNumberOfReports(mnEquipmentCount);
    for (int i = 0; i < mnEquipmentCount; i++)
    {
      mpMachineStatusArc9yMessage.setMachineStatus(i,
           masEquipmentIDs[i], manEquipmentModels[i], masEquipmentNumbers[i], vnStatus, "0000000");
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
      
      List<Arc9yMessage> vpResponses = new ArrayList<Arc9yMessage>();
      List<Integer>      vpDelays    = new ArrayList<Integer>();
      
      //
      // Statuses are changing -- MachineStatusReport (30).
      //
      mpMachineStatusArc9yMessage.setNumberOfReports(mnEquipmentCount);
      for (int i = 0; i < mnEquipmentCount; i++)
      {
        int vnStatus = (int)(Math.random() * 6) - 2;
        vnStatus = vnStatus < 0 ? 0 : vnStatus;
        String vsErrorCode = (vnStatus == 2) ? "1214029" : "0000000";
        mpMachineStatusArc9yMessage.setMachineStatus(i,
             masEquipmentIDs[i], manEquipmentModels[i], masEquipmentNumbers[i], vnStatus, vsErrorCode);
      }

      /*
       * Send MC messages
       */
      addStatusMessages(vpResponses, vpDelays);
      int vnInitialDelay = vpDelays.remove(0);
      setSSTimerEvent(new Arc9yMCResponse(vpResponses, vpDelays), vnInitialDelay);
      
      mzArc9yOnline = true;
      
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
      
      List<Arc9yMessage> vpResponses = new ArrayList<Arc9yMessage>();
      List<Integer>      vpDelays    = new ArrayList<Integer>();
      
      //
      // Statuses are changing -- MachineStatusReport (30).
      //
      mpMachineStatusArc9yMessage.setNumberOfReports(mnEquipmentCount);
      
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
          mpMachineStatusArc9yMessage.setMachineStatus(mnBrokenEquipment,
              masEquipmentIDs[mnBrokenEquipment], manEquipmentModels[mnBrokenEquipment], 
              masEquipmentNumbers[mnBrokenEquipment], 2, "1214029");
          break;
          
        case 1: // Set the broken one to offline
          mpMachineStatusArc9yMessage.setMachineStatus(mnBrokenEquipment,
              masEquipmentIDs[mnBrokenEquipment], manEquipmentModels[mnBrokenEquipment], 
              masEquipmentNumbers[mnBrokenEquipment], 1, "0000000");
          break;
          
        case 2: // Leave the broken one active for a few seconds.
          break;
          
        default:
          // It has already been fixed, and the messages sent.  Just return.
          return;
      }

      /*
       * Send MC messages
       */
      addStatusMessages(vpResponses, vpDelays);
      int vnInitialDelay = vpDelays.remove(0);
      setSSTimerEvent(new Arc9yMCResponse(vpResponses, vpDelays), vnInitialDelay);

      mzArc9yOnline = true;

      setDetailedControllerStatus("Equipment statuses changed ("+ mnBrokenEquipment + ")!");
    }
  }

  
  /*========================================================================*/
  /*  MC Messages                                                          */
  /*========================================================================*/

  /**
   * Process messages from the Material handling Computer ("MC")
   * Arc9y communication Port.
   * 
   * <BR>The received data (String) is in "receivedText".
   */
  void processEquipmentMCEvent()
  {
    mpArc9yMessage.toDataValues(receivedText);
    if ((mpArc9yMessage != null) && (!mpArc9yMessage.getValidMessage()))
    {
      String s = mpArc9yMessage.getInvalidMessageDescription();
      logger.logRxEquipmentMessage(receivedText, s);
      logger.logError("Arc9yDeviceEmulator.processEquipmentMCEvent() -- " + s);
    }
    else
    {
      String s = mpArc9yMessage.getParsedMessageString();
      logger.logRxEquipmentMessage(receivedText, s);
      logger.logDebug("Msg ID: " + s);

      switch (mpArc9yMessage.getID())
      {
        case  1: processRequestToStartOperation(); break;
        case  2: processDateTimeData(); break;
        case  3: processRequestToTerminateOperation(); break;
        case  4: processTransportDataCancel(); break;
        case  5: processTransportCommand(); break;
        //    6
        //    7
        //    8
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
        case 50: processMessage50(); break;
        default:
          logger.logError("Msg ID: " + mpArc9yMessage.getID() +
                          " NOT Processed - Arc9yDeviceEmulator.processEquipmentMCEvent()");
          break;
      }
    }
  }

  /*------------------------------------------------------------------------*/
  /**
   * Send Load Arrival
   */
  private void sendLoadArrivalReport()
  {
    ControlEventDataFormat vpCEDF = Factory.create(ControlEventDataFormat.class, 
        getName());
    vpCEDF.parseArrivalCommand(receivedText);
    
    //
    // Send an arrivalReport (26) to show that the load is coming back into
    // the input station.
    //
    Arc9yMessage vpArrivalMessage = Factory.create(Arc9yMessage.class, true);
    vpArrivalMessage.setMCKey(vpCEDF.getMCKey());
    vpArrivalMessage.setLoadInformation(1);
    vpArrivalMessage.setArrivalStationNumber(vpCEDF.getStation());
    vpArrivalMessage.setDimensionInformation(vpCEDF.getDimensionInfo());
    vpArrivalMessage.setBCData(vpCEDF.getBarCode());
    vpArrivalMessage.arrivalReportToString();
    transmitMessageToMCDevice(vpArrivalMessage);
  }

  /**
   * Add the correct number of message 30's
   * @param ipResponses
   * @param ipDelays
   */
  private void addStatusMessages(List ipResponses, List ipDelays)
  {
    int vnEquipmentCount = mnEquipmentCount;
    for (int i = 0; i < mnEquipmentCount; i = i + STATUSREPORTSPERMESSAGE)
    {
      Arc9yMessage vpArc9yResponse_30 = Factory.create(Arc9yMessage.class, true);
      
      int vnReports = Math.min(vnEquipmentCount, STATUSREPORTSPERMESSAGE);
      vpArc9yResponse_30.setNumberOfReports(vnReports);
      for (int x = 0; x < vnReports; x++)
      {
        vpArc9yResponse_30.setMachineStatusItem(x, 
            mpMachineStatusArc9yMessage.getMachineStatusItem(x + i));
      }
      vnEquipmentCount = vnEquipmentCount - STATUSREPORTSPERMESSAGE;
      vpArc9yResponse_30.setContinuationClassification(vnEquipmentCount > 0? 1:0);
      vpArc9yResponse_30.machineStatusReportToString();
      
      ipDelays.add(100);
      ipResponses.add(vpArc9yResponse_30);
    }
  }

  /*------------------------------------------------------------------------*/
  /**
   * MC ID 01
   * <BR>Need to reply with a 21, 22, 30
   */
  private void processRequestToStartOperation()
  {
    setDetailedControllerStatus("Request To Start Operation (01) - Received");
    
    List<Arc9yMessage> vpResponses = new ArrayList<Arc9yMessage>();
    List<Integer>      vpDelays    = new ArrayList<Integer>();
    
    //
    // First is responseToOperationStartRequest (21)
    //
    Arc9yMessage vpMCResponse_21 = Factory.create(Arc9yMessage.class, true);
    vpMCResponse_21.setResponseClassification(0); // Normal
    vpMCResponse_21.setErrorDetails("00");
    vpMCResponse_21.responseToOperationStartRequestToString();
    
    vpDelays.add(700);
    vpResponses.add(vpMCResponse_21);

    //
    // Next is Date/Time Request (22)
    // (not really, but this is a convenient place to test)
    //
    Arc9yMessage vpMCResponse_22 = Factory.create(Arc9yMessage.class, true);
    vpMCResponse_22.setRequestClassification(1);
    vpMCResponse_22.dateTimeRequestToString();
    
    vpDelays.add(700);
    vpResponses.add(vpMCResponse_22);

    //
    // Next is a MachineStatusReport (30).
    //
    setEquipmentOnline(true);
    addStatusMessages(vpResponses, vpDelays);
    
    mzArc9yOnline = true;

    int vnInitialDelay = vpDelays.remove(0);
    setSSTimerEvent(new Arc9yMCResponse(vpResponses, vpDelays), vnInitialDelay);

    setDetailedControllerStatus("Request To Start Operation (01) - Processed");
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
    List<Arc9yMessage> vpResponses = new ArrayList<Arc9yMessage>();
    List<Integer>      vpDelays    = new ArrayList<Integer>();

    /*
     * ALWAYS Need to respond with a 23
     */
    Arc9yMessage vpArc9yResponse_23 = Factory.create(Arc9yMessage.class, true);
    vpArc9yResponse_23.setResponseClassification(0); // Normal
    vpArc9yResponse_23.setResponseDetailsModelCode("00");
    vpArc9yResponse_23.setResponseDetailsMachineNumber("000000");
    vpArc9yResponse_23.setErrorDetails("00");
    vpArc9yResponse_23.responseToOperationTerminationRequestToString();

    if (mzArc9yOnline)
    {
      setEquipmentOnline(false);
      vpDelays.add(1280);
      vpResponses.add(vpArc9yResponse_23);

      //
      // Next is a MachineStatusReport (30).
      //
      addStatusMessages(vpResponses, vpDelays);
      
      int vnInitialDelay = vpDelays.remove(0);
      setSSTimerEvent(new Arc9yMCResponse(vpResponses, vpDelays), vnInitialDelay);
    }
    else
    {
      setSSTimerEvent(new Arc9yMCResponse(vpArc9yResponse_23), 40);
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
    Arc9yMessage vpArc9yResponse_24 = Factory.create(Arc9yMessage.class, true);
    vpArc9yResponse_24.setMCKey(mpArc9yMessage.getMCKey());
    vpArc9yResponse_24.setResponseClassification(0); // Normal
    vpArc9yResponse_24.responseToTransportDataCancelToString();

    setSSTimerEvent(new Arc9yMCResponse(vpArc9yResponse_24), 1000);
  }


  /*------------------------------------------------------------------------*/
  /**
   * MC ID 05
   * <BR>Need to reply with a 25, 64, 33 (station to rack)
   * <BR>Need to reply with a 25, 64, 68, 26 (if required) (station to station)
   */
  protected void processTransportCommand()
  {
    List<Arc9yMessage> vpResponses = new ArrayList<Arc9yMessage>();
    List<Integer>      vpDelays    = new ArrayList<Integer>();

    //
    // First is responseToTransportCommand (25).
    //
    Arc9yMessage vpArc9yResponse_25 = Factory.create(Arc9yMessage.class, true);
    vpArc9yResponse_25.setMCKey(mpArc9yMessage.getMCKey());
    vpArc9yResponse_25.setResponseClassification(0); // Normal
    vpArc9yResponse_25.responseToTransportCommandToString();
    
    vpDelays.add(60);
    vpResponses.add(vpArc9yResponse_25);

    //
    // Next is a operationCompletionReport (33).
    //
    String vsDestStation = mpArc9yMessage.getDestinationStationNumber();
    if (vsDestStation.equals(Arc9yMessage.RACK_STATION))
    {
      Arc9yMessage vpArc9yResponse_33 = Factory.create(Arc9yMessage.class, true);
      vpArc9yResponse_33.setRetrievalDataMCKey(0, mpArc9yMessage.getMCKey());
      vpArc9yResponse_33.setRetrievalDataTransportDivision(0, mpArc9yMessage.getTransportDivision());
      vpArc9yResponse_33.setRetrievalDataType(0, 2);
      vpArc9yResponse_33.setOperationCompleteRMNumber(msArc9yDeviceId);
      vpArc9yResponse_33.setRetrievalDataCompletionClassification(0, 0); // 0 = Normal
      vpArc9yResponse_33.setRetrievalDataSourceStationNumber(0, mpArc9yMessage.getSourceStationNumber());
      vpArc9yResponse_33.setRetrievalDataDestinationStationNumber(0, mpArc9yMessage.getDestinationStationNumber());
      vpArc9yResponse_33.setRetrievalDataLocationNumber(0, mpArc9yMessage.getLocationNumber(),mpArc9yMessage.getWarehouse());
//      vpArc9yResponse_33.setRetrievalDataShelfToShelfLocationNumber(0, mpArc9yMessage.getRetrievalDataShelfToShelfLocationNumber(0));
      vpArc9yResponse_33.setRetrievalDataBCData(0, mpArc9yMessage.getBCData());
      vpArc9yResponse_33.setRetrievalDataWorkNumber(0, mpArc9yMessage.getWorkNumber());
      vpArc9yResponse_33.setRetrievalDataType(1, 0);
      vpArc9yResponse_33.operationCompletionReportToString();

      vpDelays.add(storeCompletionDelay);
      vpResponses.add(vpArc9yResponse_33);
    }
    else
    {
      /*
       * Only send the arrival if arrivals are required
       */
      StandardStationServer vpStnServ =  Factory.create(StandardStationServer.class);
      boolean arrival = vpStnServ.doesStationGetRetrieveArrival(
          mpArc9yMessage.getDestinationStationNumber());
      if (arrival)
      {
        // check to see if this is a group station
        String vsChildStn = vpStnServ.getReprStationChild(vsDestStation);
        if (vsChildStn.trim().length() > 0)
          vsDestStation = vsChildStn;
        //
        // Next is arrivalReport (26)
        //
        Arc9yMessage vpArc9yResponse_26 = Factory.create(Arc9yMessage.class, true);
        vpArc9yResponse_26.setMCKey(mpArc9yMessage.getMCKey());
        vpArc9yResponse_26.setArrivalStationNumber(vsDestStation);
        vpArc9yResponse_26.setDimensionInformation(mpArc9yMessage.getDimensionInformation());
        vpArc9yResponse_26.setLoadInformation(1);
        vpArc9yResponse_26.setBCData(mpArc9yMessage.getBCData());
        vpArc9yResponse_26.arrivalReportToString();

        int vnArrivalDelay;
        if(mpStationServer.isSimulationOn(vsDestStation))
          vnArrivalDelay = mpStationServer.getStation(vsDestStation).getSimInterval();
        else
          vnArrivalDelay = storeCompletionDelay;
        vpDelays.add(vnArrivalDelay);
        vpResponses.add(vpArc9yResponse_26);
      }
//      /*
//       * Auto-press the pick complete button
//       */
//      else if ((vpStationData != null) && (vpStationData.getStationType() == DBConstants.PDSTAND))
//      {
//        Arc9yMessage vpArc9yResponse_26 = Factory.create(Arc9yMessage.class, true);
//        vpArc9yResponse_26.setMCKey("99999999");
//        vpArc9yResponse_26.setArrivalStationNumber(mpArc9yMessage.getDestinationStationNumber());
//        vpArc9yResponse_26.setDimensionInformation(mpArc9yMessage.getDimensionInformation());
//        vpArc9yResponse_26.setLoadInformation(1);
//        vpArc9yResponse_26.setBCData(mpArc9yMessage.getBCData());
//        vpArc9yResponse_26.setControlInformation(mpArc9yMessage.getControlInformation());
//        vpArc9yResponse_26.arrivalReportToString();
//        
//        vpDelays.add(storeCompletionDelay);
//        vpResponses.add(vpArc9yResponse_26);
//      }
    }
    
    int vnInitialDelay = vpDelays.remove(0);
    setSSTimerEvent(new Arc9yMCResponse(vpResponses, vpDelays), vnInitialDelay);
  }

  /*------------------------------------------------------------------------*/
  /**
   * MC ID 10
   * <BR>Need to reply with a 30
   */
  private void processMachineStatusInquiry()
  {
    List<Arc9yMessage> vpResponses = new ArrayList<Arc9yMessage>();
    List<Integer>      vpDelays    = new ArrayList<Integer>();
    
    //
    // Response is a MachineStatusReport (30).
    //
    addStatusMessages(vpResponses, vpDelays);

    int vnInitialDelay = vpDelays.remove(0);
    setSSTimerEvent(new Arc9yMCResponse(vpResponses, vpDelays), vnInitialDelay);
  }

  /*------------------------------------------------------------------------*/
  /**
   * MC ID 11
   * <BR>Need to reply with a 25, 64, 33
   */
  private void processAlternativeLocationCmd()
  {
    List<Arc9yMessage> vpResponses = new ArrayList<Arc9yMessage>();
    List<Integer>      vpDelays    = new ArrayList<Integer>();

    //
    // First is responseToAlternativeLocationCmd (31).
    //
    Arc9yMessage vpArc9yResponse_31 = Factory.create(Arc9yMessage.class, true);
    vpArc9yResponse_31.setMCKey(mpArc9yMessage.getMCKey());
    vpArc9yResponse_31.setResponseClassification(0); // Normal
    vpArc9yResponse_31.responseToAlternativeLocationCmdToString();
    
    vpDelays.add(2500);
    vpResponses.add(vpArc9yResponse_31);

    // If this is a data cancel, we're done
    if (mpArc9yMessage.getCommandClassification() == Arc9yMessage.CC_DATA_CANCEL)
    {
      int vnInitialDelay = vpDelays.remove(0);
      setSSTimerEvent(new Arc9yMCResponse(vpResponses, vpDelays), vnInitialDelay);
      return;
    }
    
    //
    // Next is arrivalReport (26) for stations or (33) for locations
    //
    String vsDestStation = mpArc9yMessage.getAlternateStationNumber();
    if (vsDestStation.equals(Arc9yMessage.RACK_STATION))
    {
      Arc9yMessage vpArc9yResponse_33 = Factory.create(Arc9yMessage.class, true);
      
      
      vpArc9yResponse_33.setRetrievalDataMCKey(0, mpArc9yMessage.getMCKey());
      vpArc9yResponse_33.setRetrievalDataTransportDivision(0, mpArc9yMessage.getTransportDivision());
      vpArc9yResponse_33.setRetrievalDataType(0, 2);
      vpArc9yResponse_33.setOperationCompleteRMNumber(msArc9yDeviceId);
      vpArc9yResponse_33.setRetrievalDataCompletionClassification(0, 0); // 0 = Normal
      vpArc9yResponse_33.setRetrievalDataSourceStationNumber(0, mpArc9yMessage.getSourceStationNumber());
      vpArc9yResponse_33.setRetrievalDataDestinationStationNumber(0, mpArc9yMessage.getAlternateStationNumber());
      vpArc9yResponse_33.setRetrievalDataLocationNumber(0, mpArc9yMessage.getAlternateLocation(), mpArc9yMessage.getWarehouse());
//      vpArc9yResponse_33.setRetrievalDataShelfToShelfLocationNumber(0, mpArc9yMessage.getRetrievalDataShelfToShelfLocationNumber(0));
      vpArc9yResponse_33.setRetrievalDataBCData(0, mpArc9yMessage.getBCData());
      vpArc9yResponse_33.setRetrievalDataWorkNumber(0, mpArc9yMessage.getWorkNumber());
      vpArc9yResponse_33.setRetrievalDataType(1, 0);
      vpArc9yResponse_33.operationCompletionReportToString();
      
      vpDelays.add(storeCompletionDelay);
      vpResponses.add(vpArc9yResponse_33);
    }
    else
    {
      /*
       * Only send the arrival if arrivals are required
       */
      StandardStationServer vpStnServ =  Factory.create(StandardStationServer.class);
      boolean arrival = vpStnServ.doesStationGetRetrieveArrival(
          mpArc9yMessage.getDestinationStationNumber());
      if (arrival)
      {
        //
        // Next is arrivalReport (26)
        //
        // check to see if this is a group station
        String vsChildStn = vpStnServ.getReprStationChild(vsDestStation);
        if (vsChildStn.trim().length() > 0)
          vsDestStation = vsChildStn;

        Arc9yMessage vpArc9yResponse_26 = Factory.create(Arc9yMessage.class, true);
        vpArc9yResponse_26.setMCKey(mpArc9yMessage.getMCKey());
        vpArc9yResponse_26.setArrivalStationNumber(vsDestStation);
        vpArc9yResponse_26.setDimensionInformation(mpArc9yMessage.getDimensionInformation());
        vpArc9yResponse_26.setLoadInformation(1);
        vpArc9yResponse_26.setBCData(mpArc9yMessage.getBCData());
        vpArc9yResponse_26.arrivalReportToString();

        int vnArrivalDelay;
        if(mpStationServer.isSimulationOn(vsDestStation))
          vnArrivalDelay = mpStationServer.getStation(vsDestStation).getSimInterval();
        else
          vnArrivalDelay = storeCompletionDelay*10;
        vpDelays.add(vnArrivalDelay);
        vpResponses.add(vpArc9yResponse_26);
      }
//      /*
//       * Auto-press the pick complete button
//       */
//      else if ((vpStationData != null) && (vpStationData.getStationType() == DBConstants.PDSTAND))
//      {
//        Arc9yMessage vpArc9yResponse_26 = Factory.create(Arc9yMessage.class, true);
//        vpArc9yResponse_26.setMCKey(mpArc9yMessage.getMCKey());
//        vpArc9yResponse_26.setArrivalStationNumber(vsDestStation);
//        vpArc9yResponse_26.setDimensionInformation(mpArc9yMessage.getDimensionInformation());
//        vpArc9yResponse_26.setLoadInformation(1);
//        vpArc9yResponse_26.setBCData(mpArc9yMessage.getBCData());
//        vpArc9yResponse_26.setControlInformation(mpArc9yMessage.getControlInformation());
//        vpArc9yResponse_26.arrivalReportToString();
//        
//        vpDelays.add(storeCompletionDelay);
//        vpResponses.add(vpArc9yResponse_26);
//      }
    }
    
    int vnInitialDelay = vpDelays.remove(0);
    setSSTimerEvent(new Arc9yMCResponse(vpResponses, vpDelays), vnInitialDelay);
  }

  /*------------------------------------------------------------------------*/
  /**
   * MC ID 12
   * <BR>Need to reply with a 32, 33, 68, 26
   */
  protected void processRetrievalCmd()
  {
    List<Arc9yMessage> vpResponses = new ArrayList<Arc9yMessage>();
    List<Integer>      vpDelays    = new ArrayList<Integer>();
    //
    // First is responseToRetrievalCmd (32).
    //
    Arc9yMessage vpArc9yResponse_32 = Factory.create(Arc9yMessage.class, true);
    int vnRetDataTransClass = mpArc9yMessage.getRetrievalDataTransportDivision(0);
    vpArc9yResponse_32.setMCKey(mpArc9yMessage.getRetrievalDataMCKey(0));
    vpArc9yResponse_32.setResponseClassification(0); // Normal
    vpArc9yResponse_32.setMCKey2(mpArc9yMessage.getRetrievalDataMCKey(1));
    vpArc9yResponse_32.setResponseClassification2(0); // Normal
    vpArc9yResponse_32.setRetrievalDataTransportDivision(0, vnRetDataTransClass);
    vpArc9yResponse_32.responseToRetrievalCmdToString();
    
    vpDelays.add(50);
    vpResponses.add(vpArc9yResponse_32);
    
    //
    // Next is a operationCompletionReport (33) for Retrieval pickup.
    //
    Arc9yMessage vpArc9yResponse_33 = Factory.create(Arc9yMessage.class, true);
    // Completion: 0 = Normal, 1 = Bin Full, 2 = Bin Empty, 9 = Cancel
    int vnCompletion = 0;
    for (int i = 0; i < 2; i++)
    {
      vpArc9yResponse_33.setRetrievalDataMCKey(i, mpArc9yMessage.getRetrievalDataMCKey(i));
      vpArc9yResponse_33.setRetrievalDataTransportDivision(i, mpArc9yMessage.getRetrievalDataTransportDivision(i));
      vpArc9yResponse_33.setRetrievalDataType(i, mpArc9yMessage.getRetrievalDataType(i));
      vpArc9yResponse_33.setRetrievalDataCompletionClassification(i, vnCompletion);
      vpArc9yResponse_33.setRetrievalDataSourceStationNumber(i, mpArc9yMessage.getRetrievalDataSourceStationNumber(i));
      vpArc9yResponse_33.setRetrievalDataDestinationStationNumber(i, mpArc9yMessage.getRetrievalDataDestinationStationNumber(i));
      vpArc9yResponse_33.setRetrievalDataLocationNumber(i, mpArc9yMessage.getRetrievalDataLocationNumber(i),mpArc9yMessage.getWarehouse());
      vpArc9yResponse_33.setRetrievalDataShelfToShelfLocationNumber(i, mpArc9yMessage.getRetrievalDataShelfToShelfLocationNumber(i));
      vpArc9yResponse_33.setRetrievalDataBCData(i, mpArc9yMessage.getRetrievalDataBCData(i));
      vpArc9yResponse_33.setRetrievalDataWorkNumber(i, mpArc9yMessage.getRetrievalDataWorkNumber(i));
      vpArc9yResponse_33.setRetrievalDataMCData(i, mpArc9yMessage.getRetrievalDataMCData(i));
    }
    //
    // Bin-to-Bin move - Need Bin-to-Bin Retrieval (4)
    //
    int vn33Delay = retrieveCompletionDepositDelay;
    if (vnRetDataTransClass == 5)
    {
      vpArc9yResponse_33.setRetrievalDataTransportDivision(0, 4);
      vn33Delay = retrieveCompletionPickupDelay;
    }
    vpArc9yResponse_33.operationCompletionReportToString();
    
    vpDelays.add(vn33Delay);
    vpResponses.add(vpArc9yResponse_33);

    //
    // If the completion code is not 0, we're done here
    //
    if (vnCompletion != 0)
    {
      int vnInitialDelay = vpDelays.remove(0);
      setSSTimerEvent(new Arc9yMCResponse(vpResponses, vpDelays), vnInitialDelay);
      return;
    }

    if (vnRetDataTransClass == 2)
    {
      //
      // Next is arrivalReport (26)
      //
      // Only send if arrival is required
      //
      StandardStationServer vpStnServ =  Factory.create(StandardStationServer.class);
      boolean arrival = needToSendArrival(mpArc9yMessage.getRetrievalDataDestinationStationNumber(0));
      if (arrival)
      {
        // check to see if this is a group station
        String vsDestStn = mpArc9yMessage.getRetrievalDataDestinationStationNumber(0);
        String vsChildStn = vpStnServ.getReprStationChild(vsDestStn);
        if (vsChildStn.trim().length() > 0)
          vsDestStn = vsChildStn;
        
        Arc9yMessage vpArc9yResponse_26 = Factory.create(Arc9yMessage.class, true);
        vpArc9yResponse_26.setMCKey(mpArc9yMessage.getRetrievalDataMCKey(0));
        vpArc9yResponse_26.setArrivalStationNumber(vsDestStn);
        vpArc9yResponse_26.setDimensionInformation(mpArc9yMessage.getDimensionInformation());
        vpArc9yResponse_26.setLoadInformation(1);
        vpArc9yResponse_26.setBCData(mpArc9yMessage.getRetrievalDataBCData(0));
        vpArc9yResponse_26.arrivalReportToString();
  
        int vnArrivalDelay;
        if (mpStationServer.isSimulationOn(vsDestStn))
          vnArrivalDelay = mpStationServer.getStation(vsDestStn).getSimInterval();
        else
          vnArrivalDelay = retrieveArrivalReportDelay;
        vpDelays.add(vnArrivalDelay);
        vpResponses.add(vpArc9yResponse_26);
      }
    }
    else
    {
      //
      // Next is a operationCompletionReport (33) for Retrieval store.
      //
      Arc9yMessage vpArc9yResponse_33b = Factory.create(Arc9yMessage.class, true);
      for (int i = 0; i < 2; i++)
      {
        vpArc9yResponse_33b.setRetrievalDataMCKey(i, mpArc9yMessage.getRetrievalDataMCKey(i));
        vpArc9yResponse_33b.setRetrievalDataTransportDivision(i, mpArc9yMessage.getRetrievalDataTransportDivision(i));
        vpArc9yResponse_33b.setRetrievalDataType(i, mpArc9yMessage.getRetrievalDataType(i));
        vpArc9yResponse_33b.setRetrievalDataCompletionClassification(i, 0); // 0 = Normal
        vpArc9yResponse_33b.setRetrievalDataSourceStationNumber(i, mpArc9yMessage.getRetrievalDataSourceStationNumber(i));
        vpArc9yResponse_33b.setRetrievalDataDestinationStationNumber(i, mpArc9yMessage.getRetrievalDataDestinationStationNumber(i));
        vpArc9yResponse_33b.setRetrievalDataLocationNumber(i, mpArc9yMessage.getRetrievalDataLocationNumber(i), mpArc9yMessage.getWarehouse());
        vpArc9yResponse_33b.setRetrievalDataShelfToShelfLocationNumber(i, mpArc9yMessage.getRetrievalDataShelfToShelfLocationNumber(i),
                                                                          mpArc9yMessage.getWarehouse());
        vpArc9yResponse_33b.setRetrievalDataBCData(i, mpArc9yMessage.getRetrievalDataBCData(i));
        vpArc9yResponse_33b.setRetrievalDataWorkNumber(i, mpArc9yMessage.getRetrievalDataWorkNumber(i));
      }
      //
      // Bin-to-Bin move - Need Bin-to-Bin Store (5)
      //
      vpArc9yResponse_33b.setRetrievalDataTransportDivision(0, 5);
      vpArc9yResponse_33b.operationCompletionReportToString();

      vpDelays.add(retrieveCompletionDepositDelay);
      vpResponses.add(vpArc9yResponse_33b);
    }
    
    int vnInitialDelay = vpDelays.remove(0);
    setSSTimerEvent(new Arc9yMCResponse(vpResponses, vpDelays), vnInitialDelay);
  }


  protected boolean needToSendArrival(String isStation)
  {
    StandardStationServer vpStnServ = Factory.create(StandardStationServer.class);
//    boolean vzHasTransfer = vpStnServ.doesStationNeedAGCTransfer(isStation);
//    return (vpStnServ.doesStationGetRetrieveArrival(isStation) && !vzHasTransfer);
    return vpStnServ.doesStationGetRetrieveArrival(isStation);
  }

  /*------------------------------------------------------------------------*/
  /**
   * MC ID 16
   * <BR>Start ARC - Need to respond with a 30, 30
   * <BR>Stop ARC - Need to respond with a 36
   */
  private void processSimultaneousStartStopCmd()
  {
    List<Arc9yMessage> vpResponses = new ArrayList<Arc9yMessage>();
    List<Integer>      vpDelays    = new ArrayList<Integer>();
    
    if (mpArc9yMessage.getStartStopClassification() == 1) // 1 = start, 2 = stop
    {
      //
      // START Equipment.
      //
      if (!mzArc9yOnline)
      {
        //
        // START Equipment - ARC is NOT Online - No response
        //
      }
      else
      {
        //
        // START Equipment - ARC IS Online - Only Reply ONCE.
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
          Arc9yMessage vpArc9yResponse_30b = Factory.create(Arc9yMessage.class, true);
          vpArc9yResponse_30b.setNumberOfReports(1);
          vpArc9yResponse_30b.setMachineStatus(0,
                 masEquipmentIDs[0], manEquipmentModels[0], masEquipmentNumbers[0], 0, "0000000");
          vpArc9yResponse_30b.machineStatusReportToString();
          vpDelays.add(4000);
          vpResponses.add(vpArc9yResponse_30b);

          //
          // Send the messages
          //
          int vnInitialDelay = vpDelays.remove(0);
          setSSTimerEvent(new Arc9yMCResponse(vpResponses, vpDelays), vnInitialDelay);
        }
      }
    }
    else
    {
      //
      // STOP Equipment.
      //
      if (mzArc9yOnline)
      {
        //
        // STOP Equipment - ARC IS Online - Only reply if Equipment ONLINE.
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
          Arc9yMessage vpArc9yResponse_30b = Factory.create(Arc9yMessage.class, true);
          vpArc9yResponse_30b.setNumberOfReports(1);
          vpArc9yResponse_30b.setMachineStatus(0,
                 masEquipmentIDs[0], manEquipmentModels[0], masEquipmentNumbers[0], 1, "0000000");
          vpArc9yResponse_30b.setContinuationClassification(1);
          vpArc9yResponse_30b.machineStatusReportToString();
          vpDelays.add(1000);
          vpResponses.add(vpArc9yResponse_30b);

          //
          // Send the messages
          //
          int vnInitialDelay = vpDelays.remove(0);
          setSSTimerEvent(new Arc9yMCResponse(vpResponses, vpDelays), vnInitialDelay);
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

    Arc9yMessage vpArc9yResponse_39 = Factory.create(Arc9yMessage.class, true);
    vpArc9yResponse_39.setCommunicationTestTextRequest(mpArc9yMessage.getCommunicationTestTextRequest());
    vpArc9yResponse_39.setCommunicationTestTextResponse(mpArc9yMessage.getCommunicationTestTextRequest());
    vpArc9yResponse_39.responseToCommunicationTestRequestToString();

    setSSTimerEvent(new Arc9yMCResponse(vpArc9yResponse_39), 100);
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
   * MC ID 50
   */
  protected void processMessage50()
  {
    logger.logDebug("MC ID 50: Need Some Code!!!");
  }

  
  /*========================================================================*/
  /*  Give the data to be transmitted to the Device/Equipment to the Port.  */
  /*========================================================================*/
  
  /**
   * Transmit an MC message
   */
  protected void transmitMessageToMCDevice(Arc9yMessage msg)
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
      logger.logError("Arc9yDeviceEmulator.transmitMessageToMCDevice() -- " +
          msg.getInvalidMessageDescription() + " -- \"" + msg.getMessageAsString() + "\"");
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
  protected class Arc9yMCResponse extends TimerTask
  {
    List<Arc9yMessage> mpArc9yMessages = new ArrayList<Arc9yMessage>();
    List<Integer>      mpDelays        = new ArrayList<Integer>();
    
    public Arc9yMCResponse(List<Arc9yMessage> ipArc9yMessages, List<Integer> ipDelays)
    {
      mpArc9yMessages = ipArc9yMessages;
      mpDelays        = ipDelays;
    }
    
    public Arc9yMCResponse(Object... iapParams)
    {
      int vnParams = iapParams.length;
      for (int i = 0; i < vnParams; i++)
      {
        if (iapParams[i] instanceof Arc9yMessage)
        {
          mpArc9yMessages.add((Arc9yMessage)iapParams[i]);
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
      transmitMessageToMCDevice(mpArc9yMessages.remove(0));
      if (mpArc9yMessages.size() > 0)
      {
        int vnDelay = mpDelays.remove(0);
        setSSTimerEvent(new Arc9yMCResponse(mpArc9yMessages, mpDelays), vnDelay);
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
    Controller vpController = Factory.create(Arc9yDeviceEmulator.class);
    vpController.setEquipmentPortCKN(ipConfig.getString(DEVICE_PORT));
    return vpController;
  }

}
