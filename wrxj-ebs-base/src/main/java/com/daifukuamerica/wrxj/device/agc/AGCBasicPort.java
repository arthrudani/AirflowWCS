package com.daifukuamerica.wrxj.device.agc;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.device.port.PortConsts;
import com.daifukuamerica.wrxj.device.port.PortController;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.timer.RestartableTimerTask;
import java.util.Random;

public class AGCBasicPort extends PortController
{
  private static final int CHECKSUM_LENGTH = 2;
  protected static final int SEQUENCE_NUMBER_LENGTH = 4;
  private static final int MAX_SEQUENCE_NUMBER = 9999;
  private static final int MIN_SEQUENCE_NUMBER = 1;
  private static final String CHECKSUM_STRING = "00";
  private static final String[] ASCII_DIGITS =
     {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
  private static final char[] CHAR_DIGITS =
     {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

  private static final byte[] ENQ_MSG = {PortConsts.ENQ, PortConsts.ETX};
  private static final byte[] EOT_MSG = {PortConsts.EOT, PortConsts.ETX};
  private static final byte[] ACK_MSG = {PortConsts.ACK, PortConsts.ETX};
  private static final byte[] NAK_MSG = {PortConsts.NAK, PortConsts.ETX};
  private static final byte[] STX = {PortConsts.STX};
  /**
   * The data byte packet terminating sequence.
   */
  private static final byte[] ETX = {PortConsts.ETX};//null;
  /**
   * The size (in bytes) of the data byte packet terminating sequence.
   */
  private static final int endDelimiterLength = 1;//0;
  //
  private static enum State {NEUTRAL,
                      WAITING_FOR_ENQ_RESPONSE,
                      WAITING_FOR_TEXT,
                      WAITING_FOR_TEXT_RESPONSE,
                      WAITING_FOR_EOT,
                      WAITING_FOR_EOT_TIMEOUT,
                      SHUTDOWN}
  private State meCurrentState = State.SHUTDOWN;
  //
  private int transmittedSequenceNumber = 0;
  private int mnReceivedSequenceNumber = 0;
  private boolean mzDuplicateSequenceNumber = false;
  private String msPreviousReceivedData = null;
  protected String msCurrentReceivedData = null;
  private boolean mzNakTextResponseSent = false;
  private boolean mzTextResent = false;
  //
  private byte[] baConditionedTransmitData = null;
  private int baConditionedTransmitDataLength = 0;
  //
  GetEnq mpGetEnq = null;
  GetEtxOfEnq mpGetEtxOfEnq = null;
  //
  GetEnqResponse mpGetEnqResponse = null;
  GetEtxOfEnqResponse mpGetEtxOfEnqResponse = null;
  //
  GetText mpGetText = null;
  GetEtxOfText mpGetEtxOfText = null;
  //
  GetTextResponse mpGetTextResponse = null;
  GetEtxOfTextResponse mpGetEtxOfTextResponse = null;
  //
  GetEot mpGetEot = null;
  GetEtxOfEot mpGetEtxOfEot = null;
  //
  int mnMaxEnqResends = 3;
  int mnEnqSendCount = 0;
  //
  int mnMaxTextResends = 3;
  int mnTextSendCount = 0;
  //
  int mnMaxTextResponseNaks = 3;
  int mnTextResponseNakCount = 0;
  //
  boolean mzThisIsThePrimaryDevice = true;
  boolean mzUseRandomT2orT3Interval = true;
  int mnMinRandomT2orT3Interval = 2000;
  int mnMaxRandomT2orT3Interval = 20000;
  Random mpRandom = new Random();
  //
  int mnT1ResponseWaitInterval = 30000;
  T1EnqResponseWaitTimeout mpT1EnqResponseWaitTimeout;
  T1TextResponseWaitTimeout mpT1TextResponseWaitTimeout;
  //
  int mnT2MainEnqResendInterval = 2000;
  int mnT3MainEnqResendInterval = 20000;
  int mnT2orT3MainEnqResendInterval = 2000;
  T2orT3MainEnqResendTimeout mpT2orT3MainEnqResendTimeout;
  //
  int mnT4EnqWaitAfterEOTSentInterval = 500;
  T4EnqWaitAfterEOTSentTimeout mpT4EnqWaitAfterEOTSentTimeout;
  //
  int mnResponseTransmitWaitInterval = 5000;
  ResponseTransmitWaitTimeout mpResponseTransmitWaitTimeout;
  //
  /*--------------------------------------------------------------------------*/
  /**
   *  Class constructor.  Create a Port to communicate with a Daifuku AGC (AS21
   *  Control System) using "Basic" protocol.
   */
  public AGCBasicPort()
  {
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Starts up the communications device by creating this ComDevice's actual
   * ComPort (communication physical layer).
   *
   * @param aControllerKeyName the unique name that identifies this instance of Controller
   */
  /*--------------------------------------------------------------------------*/
  @Override
  public void initialize(String aControllerKeyName)
  {
    super.initialize(aControllerKeyName);
  }

  /*--------------------------------------------------------------------------*/
  @Override
  public void startup()
  {
    super.startup();
    logger.logDebug("AGCBasicPort.startup() - Start");
    String vsPrimaryDevice = getConfigProperty("PrimaryDevice");
    if (vsPrimaryDevice != null)
    {
      mzThisIsThePrimaryDevice = getConfigPropertyAsBoolean("PrimaryDevice");
      logger.logDebug("PrimaryDevice: " + mzThisIsThePrimaryDevice + " - startup()");
    }
    else
    {
       logger.logDebug("Using DEFAULT PrimaryDevice: "
          + mzThisIsThePrimaryDevice
          + " NO PrimaryDevice in Config - startup()");
    }
    /*----------------------------------*/
    String vsT1ResponseWaitInterval = getConfigProperty("T1ResponseWaitInterval");
    if (vsT1ResponseWaitInterval != null)
    {
      int vnT1ResponseWaitInterval = getConfigPropertyAsInt("T1ResponseWaitInterval");
      if (vnT1ResponseWaitInterval >= 0)
      {
        logger.logDebug("T1ResponseWaitInterval: " + vnT1ResponseWaitInterval
            + " - startup()");
        mnT1ResponseWaitInterval = vnT1ResponseWaitInterval;
      }
      else
      {
        logger.logError("INVALID T1ResponseWaitInterval \""
            + vnT1ResponseWaitInterval + "\" - startup()");
      }
    }
    else
    {
       logger.logDebug("Using DEFAULT T1ResponseWaitInterval: "
          + mnT1ResponseWaitInterval
          + " NO T1ResponseWaitInterval in Config - startup()");
    }
    /*----------------------------------*/
    String vsMainEnqResendInterval = getConfigProperty("MainEnqResendInterval");
    if (vsMainEnqResendInterval != null)
    {
      int vnMainEnqResendInterval = getConfigPropertyAsInt("MainEnqResendInterval");
      if (vnMainEnqResendInterval >= 0)
      {
        logger.logDebug("MainEnqResendInterval: " + vnMainEnqResendInterval
            + " - startup()");
        mnT2orT3MainEnqResendInterval = vnMainEnqResendInterval;
      }
      else
      {
        logger.logError("INVALID MainEnqResendInterval \""
            + vnMainEnqResendInterval + "\" - startup()");
      }
    }
    else
    {
       logger.logDebug("Using DEFAULT MainEnqResendInterval: "
          + mnT2orT3MainEnqResendInterval
          + " NO MainEnqResendInterval in Config - startup()");
    }
    /*----------------------------------*/
    String vsUseRandomMainEnqResendInterval = getConfigProperty("UseRandomMainEnqResendInterval");
    if (vsUseRandomMainEnqResendInterval != null)
    {
      mzUseRandomT2orT3Interval = getConfigPropertyAsBoolean("UseRandomMainEnqResendInterval");
      logger.logDebug("UseRandomMainEnqResendInterval: "
          + mzUseRandomT2orT3Interval + " - startup()");
    }
    else
    {
       logger.logDebug("Using DEFAULT UseRandomMainEnqResendInterval: "
          + mzUseRandomT2orT3Interval
          + " NO UseRandomMainEnqResendInterval in Config - startup()");
    }
    //
    String vsMinRandomMainEnqResendInterval = getConfigProperty("MinRandomMainEnqResendInterval");
    if (vsMinRandomMainEnqResendInterval != null)
    {
      int vnMinRandomMainEnqResendInterval = getConfigPropertyAsInt("MinRandomMainEnqResendInterval");
      if (vnMinRandomMainEnqResendInterval >= 0)
      {
        logger.logDebug("MinRandomMainEnqResendInterval: "
            + vnMinRandomMainEnqResendInterval + " - startup()");
        mnMinRandomT2orT3Interval = vnMinRandomMainEnqResendInterval;
      }
      else
      {
        logger.logError("INVALID MinRandomMainEnqResendInterval \""
            + vnMinRandomMainEnqResendInterval + "\" - startup()");
      }
    }
    else
    {
       logger.logDebug("Using DEFAULT MinRandomMainEnqResendInterval: "
          + mnMinRandomT2orT3Interval
          + " NO MinRandomMainEnqResendInterval in Config - startup()");
    }
    /*----------------------------------*/
    String vsMaxRandomMainEnqResendInterval = getConfigProperty("MaxRandomMainEnqResendInterval");
    if (vsMaxRandomMainEnqResendInterval != null)
    {
      int vnMaxRandomMainEnqResendInterval = getConfigPropertyAsInt("MaxRandomMainEnqResendInterval");
      if (vnMaxRandomMainEnqResendInterval >= 0)
      {
        logger.logDebug("MaxRandomMainEnqResendInterval: "
            + vnMaxRandomMainEnqResendInterval + " - startup()");
        mnMaxRandomT2orT3Interval = vnMaxRandomMainEnqResendInterval;
      }
      else
      {
        logger.logError("INVALID MaxRandomMainEnqResendInterval \""
            + vnMaxRandomMainEnqResendInterval + "\" - startup()");
      }
    }
    else
    {
       logger.logDebug("Using DEFAULT MaxRandomMainEnqResendInterval: "
          + mnMaxRandomT2orT3Interval
          + " NO MaxRandomMainEnqResendInterval in Config - startup()");
    }
    /*----------------------------------*/
    String vsT4EnqWaitAfterEOTSentInterval = getConfigProperty("T4EnqWaitAfterEOTSentInterval");
    if (vsT4EnqWaitAfterEOTSentInterval != null)
    {
      int vnT4EnqWaitAfterEOTSentInterval = getConfigPropertyAsInt("T4EnqWaitAfterEOTSentInterval");
      if (vnT4EnqWaitAfterEOTSentInterval >= 0)
      {
        logger.logDebug("T4EnqWaitAfterEOTSentInterval: "
            + vnT4EnqWaitAfterEOTSentInterval + " - startup()");
        mnT4EnqWaitAfterEOTSentInterval = vnT4EnqWaitAfterEOTSentInterval;
      }
      else
      {
        logger.logError("INVALID T4EnqWaitAfterEOTSentInterval \""
            + vnT4EnqWaitAfterEOTSentInterval + "\" - startup()");
      }
    }
    else
    {
       logger.logDebug("Using DEFAULT T4EnqWaitAfterEOTSentInterval: "
          + mnT4EnqWaitAfterEOTSentInterval
          + " NO T4EnqWaitAfterEOTSentInterval in Config - startup()");
    }
    /*----------------------------------*/
    String vsResponseTransmitWaitInterval = getConfigProperty("ResponseTransmitWaitInterval");
    if (vsResponseTransmitWaitInterval != null)
    {
      int vnResponseTransmitWaitInterval = getConfigPropertyAsInt("ResponseTransmitWaitInterval");
      if (vnResponseTransmitWaitInterval >= 0)
      {
        logger.logDebug("ResponseTransmitWaitInterval: "
            + vnResponseTransmitWaitInterval + " - startup()");
        mnResponseTransmitWaitInterval = vnResponseTransmitWaitInterval;
      }
      else
      {
        logger.logError("INVALID ResponseTransmitWaitInterval \""
            + vnResponseTransmitWaitInterval + "\" - startup()");
      }
    }
    else
    {
       logger.logDebug("Using DEFAULT ResponseTransmitWaitInterval: "
          + mnResponseTransmitWaitInterval
          + " NO ResponseTransmitWaitInterval in Config - startup()");
    }
    /*----------------------------------*/
    String vsMaxEnqResends = getConfigProperty("MaxEnqResends");
    if (vsMaxEnqResends != null)
    {
      int vnMaxEnqResends = getConfigPropertyAsInt("MaxEnqResends");
      if (vnMaxEnqResends >= 0)
      {
        logger.logDebug("MaxEnqResends: " + vnMaxEnqResends + " - startup()");
        mnMaxEnqResends = vnMaxEnqResends;
      }
      else
      {
        logger.logError("INVALID MaxEnqResends \"" + vnMaxEnqResends
            + "\" - startup()");
      }
    }
    else
    {
       logger.logDebug("Using DEFAULT MaxEnqResends: " + mnMaxEnqResends
          + " NO MaxEnqResends in Config - startup()");
    }
    /*----------------------------------*/
    String vsMaxTextResends = getConfigProperty("MaxTextResends");
    if (vsMaxTextResends != null)
    {
      int vnMaxTextResends = getConfigPropertyAsInt("MaxTextResends");
      if (vnMaxTextResends >= 0)
      {
        logger.logDebug("MaxTextResends: " + vnMaxTextResends + " - startup()");
        mnMaxTextResends = vnMaxTextResends;
      }
      else
      {
        logger.logError("INVALID MaxTextResends \"" + vnMaxTextResends
            + "\" - startup()");
      }
    }
    else
    {
       logger.logDebug("Using DEFAULT MaxTextResends: " + mnMaxTextResends
          + " NO MaxTextResends in Config - startup()");
    }
    /*----------------------------------*/
    String vsMaxTextResponseNaks = getConfigProperty("MaxTextResponseNaks");
    if (vsMaxTextResponseNaks != null)
    {
      int vnMaxTextResponseNaks = getConfigPropertyAsInt("MaxTextResponseNaks");
      if (vnMaxTextResponseNaks >= 0)
      {
        logger.logDebug("MaxEnqNakResends: " + vnMaxTextResponseNaks
            + " - startup()");
        mnMaxTextResponseNaks = vnMaxTextResponseNaks;
      }
      else
      {
        logger.logError("INVALID MaxTextResponseNaks \""
            + vnMaxTextResponseNaks + "\" - startup()");
      }
    }
    else
    {
       logger.logDebug("Using DEFAULT MaxTextResponseNaks: "
          + mnMaxTextResponseNaks
          + " NO MaxTextResponseNaks in Config - startup()");
    }
    
    disconnectDB();
    
    logger.logDebug("AGCBasicPort.startup() - End");
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  @Override
  protected void startupProtocol()
  {
    super.startupProtocol();
    logger.logDebug("AGCBasicPort.startupProtocol() - Start");
    
    mpT1EnqResponseWaitTimeout = new T1EnqResponseWaitTimeout();
    mpT1TextResponseWaitTimeout = new T1TextResponseWaitTimeout();
    mpT2orT3MainEnqResendTimeout = new T2orT3MainEnqResendTimeout();
    mpT4EnqWaitAfterEOTSentTimeout = new T4EnqWaitAfterEOTSentTimeout();
    mpResponseTransmitWaitTimeout = new ResponseTransmitWaitTimeout();

    baConditionedTransmitData = null;
    if (mzThisIsThePrimaryDevice)
    {
      mnT2orT3MainEnqResendInterval = mnT2MainEnqResendInterval;
    }
    else
    {
      mnT2orT3MainEnqResendInterval = mnT3MainEnqResendInterval;
    }
    //
    mpGetEnq = new GetEnq();
    mpGetEtxOfEnq = new GetEtxOfEnq();
    //
    mpGetEnqResponse = new GetEnqResponse();
    mpGetEtxOfEnqResponse = new GetEtxOfEnqResponse();
    //
    mpGetText = new GetText();
    mpGetEtxOfText = new GetEtxOfText();
    //
    mpGetTextResponse = new GetTextResponse();
    mpGetEtxOfTextResponse = new GetEtxOfTextResponse();
    //
    mpGetEot = new GetEot();
    mpGetEtxOfEot = new GetEtxOfEot();
    //
    setupReceiveCycle(State.NEUTRAL);
    logger.logDebug("AGCBasicPort.startupProtocol() - End");
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  @Override
  protected void shutdownProtocol()
  {
    synchronized (meCurrentState)
    {
      if (meCurrentState != State.SHUTDOWN)
      {
        meCurrentState = State.SHUTDOWN;
        logger.logDebug("AGCBasicPort.shutdownProtocol() - Start");
        //
        baConditionedTransmitData = null;
        //
        timers.cancel(mpT1EnqResponseWaitTimeout);
        mpT1EnqResponseWaitTimeout = null;
        //
        timers.cancel(mpT1TextResponseWaitTimeout);
        mpT1TextResponseWaitTimeout = null;
        //
        timers.cancel(mpT2orT3MainEnqResendTimeout);
        mpT2orT3MainEnqResendTimeout = null;
        //
        timers.cancel(mpT4EnqWaitAfterEOTSentTimeout);
        mpT4EnqWaitAfterEOTSentTimeout = null;
        //
        timers.cancel(mpResponseTransmitWaitTimeout);
        mpResponseTransmitWaitTimeout = null;
        //
        mpGetEnq = null;
        mpGetEtxOfEnq = null;
        //
        mpGetEnqResponse = null;
        mpGetEtxOfEnqResponse = null;
        //
        mpGetText = null;
        mpGetEtxOfText = null;
        //
        mpGetTextResponse = null;
        mpGetEtxOfTextResponse = null;
        //
        mpGetEot = null;
        mpGetEtxOfEot = null;
        //
        logger.logDebug("AGCBasicPort.shutdownProtocol() - End");
        super.shutdownProtocol();
      }
    }
  }
  
  /*--------------------------------------------------------------------------
  --------------------------------------------------------------------------*/
  /**
   * We have received a message from a DEVICE (Controller) that wants to
   * transmit data to the EQUIPMENT that is attached to this PORT. The data
   * (String) to be transmitted is in
   * {@link com.daifukuamerica.wrxj.common.controller.Controller#receivedText receivedText}.
   */
  @Override
  protected void processEquipmentEvent()
  {
    //logger.logDebug("");
    //logger.logDebug("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    //logger.logDebug("");
    bufferedMessageQueue.add(receivedText);
    if (meCurrentState == State.NEUTRAL)
    {
      //logger.logDebug("\"" + receivedText + "\" - OkToTransmit - processEquipmentEvent()");
      sendENQ();
    }
    else
    {
      //logger.logDebug("\"" + receivedText + "\" - NOT OkToTransmit - processEquipmentEvent()");
    }
  }

  /*--------------------------------------------------------------------------*/
  // Setup our Protocol to Start a Receive Cycle.
  /*--------------------------------------------------------------------------*/
  private void setupReceiveCycle(State ieNewState)
  {
    //logger.logDebug("Current State: " + meCurrentState + " - setupReceiveCycle()");
    //logger.logDebug("New State: " + ieNewState + " - setupReceiveCycle()");
    receiveCycleActive = false;
    removeAllDataMatchEvents();
    receivedByteCount = 0;           // Show we didn't receive anything.
    inputProtocolSink = 0;
    //
    if (ieNewState != State.WAITING_FOR_ENQ_RESPONSE)
    {
      mnEnqSendCount = 0;
    }
    mnTextSendCount = 0;
    mnMaxTextResponseNaks = 0;
    meCurrentState = ieNewState;
    //
    // End Delimited -- just wait for a terminator.
    //
    switch (ieNewState)
    {
      case WAITING_FOR_EOT_TIMEOUT:
        mnEnqSendCount = 0;
        mnTextSendCount = 0;
        //logger.logDebug("Start mpT4EnqWaitAfterEOTSentTimeout");
        timers.setSSTimerEvent(mpT4EnqWaitAfterEOTSentTimeout, mnT4EnqWaitAfterEOTSentInterval);
        setDataAvailableEvent(mpGetEnq, 0); // When we get the ETX process the message here
        addDataMatchEvent(mpGetEtxOfEnq, AGCBasicPort.ETX, 1);
        break;
      case NEUTRAL:
        mnEnqSendCount = 0;
        mnTextSendCount = 0;
        if (timers.isScheduled(mpT2orT3MainEnqResendTimeout))
        {
          if (mzUseRandomT2orT3Interval)
          {
            int vnT2orT3MainEnqResendInterval = mnMinRandomT2orT3Interval
                + mpRandom.nextInt(mnMaxRandomT2orT3Interval);
            //logger.logDebug("Start mpT2orT3MainEnqResendTimeout - " + vnT2orT3MainEnqResendInterval);
            timers.setSSTimerEvent(mpT2orT3MainEnqResendTimeout, vnT2orT3MainEnqResendInterval);
          }
          else
          {
            //logger.logDebug("Start mpT2orT3MainEnqResendTimeout - " + mnT2orT3MainEnqResendInterval);
            timers.setSSTimerEvent(mpT2orT3MainEnqResendTimeout, mnT2orT3MainEnqResendInterval);
          }
        }
        else
        {
          //logger.logDebug("Cancel mpT2orT3MainEnqResendTimeout");
          timers.cancel(mpT2orT3MainEnqResendTimeout);
        }
        setDataAvailableEvent(mpGetEnq, 0); // When we get the ETX process the message here
        addDataMatchEvent(mpGetEtxOfEnq, AGCBasicPort.ETX, 1);
        break;
     case WAITING_FOR_ENQ_RESPONSE:
       setDataAvailableEvent(mpGetEnqResponse, 0); // When we get the ETX process the message here
       addDataMatchEvent(mpGetEtxOfEnqResponse, AGCBasicPort.ETX, 1);
       //logger.logDebug("Start mpT1EnqResponseWaitTimeout");
       timers.setSSTimerEvent(mpT1EnqResponseWaitTimeout, mnT1ResponseWaitInterval);
       break;
     case WAITING_FOR_TEXT:
        setDataAvailableEvent(mpGetText, 0); // When we get the ETX process the message here
        addDataMatchEvent(mpGetEtxOfText, AGCBasicPort.ETX, 1);
        //logger.logDebug("Start mpResponseTransmitWaitTimeout");
        timers.setSSTimerEvent(mpResponseTransmitWaitTimeout, mnResponseTransmitWaitInterval);
        break;
      case WAITING_FOR_TEXT_RESPONSE:
        setDataAvailableEvent(mpGetTextResponse, 0); // When we get the ETX process the message here
        addDataMatchEvent(mpGetEtxOfTextResponse, AGCBasicPort.ETX, 1);
        //logger.logDebug("Start mpT1EnqResponseWaitTimeout");
        timers.setSSTimerEvent(mpT1TextResponseWaitTimeout, mnT1ResponseWaitInterval);
        break;
      case WAITING_FOR_EOT:
        setDataAvailableEvent(mpGetEot, 0); // When we get the ETX process the message here
        addDataMatchEvent(mpGetEtxOfEot, AGCBasicPort.ETX, 1);
        //logger.logDebug("Start mpResponseTransmitWaitTimeout");
        timers.setSSTimerEvent(mpResponseTransmitWaitTimeout, mnResponseTransmitWaitInterval);
        break;
      case SHUTDOWN:
        // Do nothing
        break;
    }
    enableEventProcessing();   // We need to explicitly enable data match checking
  }

  private void sendENQ()
  {
    if (mnEnqSendCount > mnMaxEnqResends)
    {
      mnEnqSendCount = 0;
      setupReceiveCycle(State.WAITING_FOR_EOT_TIMEOUT);
      sendEOT();
    }
    else
    {
      //logger.logDebug("sendENQ()");
      mnEnqSendCount++;
      setupReceiveCycle(State.WAITING_FOR_ENQ_RESPONSE);
      putBlock(ENQ_MSG, 2);
    }
  }
  
  private void sendEOT()
  {
    //logger.logDebug("sendEOT");
    putBlock(EOT_MSG, 2);
  }
  
  private void sendACK()
  {
    //logger.logDebug("sendACK");
    putBlock(ACK_MSG, 2);
  }
  
  private void sendNAK()
  {
    //logger.logDebug("sendNAK");
    putBlock(NAK_MSG, 2);
  }
  
  /*--------------------------------------------------------------------------
  --------------------------------------------------------------------------*/
  private void sendBufferedMessage()
  {
    //logger.logDebug("sendBuffereredMessage()");
    if (comPortStatus == ControllerConsts.STATUS_RUNNING)
    {
      //
      // There are buffered messages and the ComPort is running, so transmit
      // one of the buffered messages. Only transmit one message at a time so
      // that we can check the ComPort status before each message.
      //
      transmitEquipmentData(bufferedMessageQueue.getFirst());
    }
  }  
  
  /*--------------------------------------------------------------------------*/
  /**
   * Give the data to be transmitted to the ComPort. Add Start & End Delimiters
   * (if needed) and convert the Data String to a byte array before transmitting
   * it.
   */
  @Override
  protected void transmitEquipmentData(String equipmentData)
  {
    //logger.logDebug("\"" + equipmentData + "\" - transmitEquipmentData()");
    if (baConditionedTransmitData == null)
    {
      //
      // In the "PortDevice" base-class "conditionDataToTransmit" converts the
      // "equipmentData" String to a byte-array.
      //
      // The CHILD Protocol should override "conditionDataToTransmit" to perform
      // any type of data conditioning (checksum, sequence number, etc.) before
      // the data BYTE-ARRAY is transmitted to the ComPort (and the actual
      // equipment that is connected to the ComPort).
      //
      byte[] equipmentDataByteArray = conditionDataToTransmit(equipmentData);
      int byteArrayLen = equipmentDataByteArray.length;
      int totalDataLength = byteArrayLen;
  
      //
      // Add the Start & End Delimiters here so that the data is transmitted as
      // one block.
      //
      totalDataLength += 1;
      System.arraycopy(STX, 0,
                       outputByteBuffer, 0, 1);
      //
      System.arraycopy(equipmentDataByteArray, 0,
                       outputByteBuffer, 1, byteArrayLen);
      //
      totalDataLength += endDelimiterLength;
      System.arraycopy(ETX, 0,
                       outputByteBuffer, 1 + byteArrayLen, endDelimiterLength);
      baConditionedTransmitDataLength = totalDataLength;
      baConditionedTransmitData = outputByteBuffer;
    }
    putBlock(baConditionedTransmitData, baConditionedTransmitDataLength);
  }

  /*--------------------------------------------------------------------------*/
  // In the "PortDevice" base-class "conditionDataToTransmit" converts the
  // "equipmentData" String to a byte-array.
  //
  // The CHILD Protocol should override "conditionDataToTransmit" to perform
  // any type of data conditioning (checksum, sequence number, etc.) before
  // the data BYTE-ARRAY is transmitted to the ComPort (and the actual equipment
  // that is connected to the ComPort).
/*--------------------------------------------------------------------------*/
  /**
   * Add the message sequence number and compute and add the BCC (Block Check
   * Character, or Checksum) to the data packet.
   */
  @Override
  protected byte[] conditionDataToTransmit(String sEquipmentData)
  {
    sEquipmentData = getTransmittedSequenceNumber() + sEquipmentData;
    //
    // "addTransmittedChecksum" will calculate and add the checksum and return
    // the BYTE ARRAY we need to transmit.
    //
    return addTransmittedChecksum(sEquipmentData);
  }

  /*--------------------------------------------------------------------------*/
  private String getTransmittedSequenceNumber()
  {
    String sResult = ASCII_DIGITS[transmittedSequenceNumber / 1000] +
                     ASCII_DIGITS[(transmittedSequenceNumber % 1000) / 100] +
                     ASCII_DIGITS[(transmittedSequenceNumber % 100) / 10] +
                     ASCII_DIGITS[(transmittedSequenceNumber % 10)];

    transmittedSequenceNumber++;
    if (transmittedSequenceNumber > MAX_SEQUENCE_NUMBER)
    {
      transmittedSequenceNumber = MIN_SEQUENCE_NUMBER;
    }
    return sResult;
  }
  /*--------------------------------------------------------------------------*/
  // Port's "OnDataAvailable" Handler to receive a DELIMITED message
  // terminated by an "ETX" (or some other delimiting unique data byte(s)).
  // When we come here the received message has already been buffered for us and
  // the ETX has been automatically discarded.
  //
  // We come here BEFORE we get to "GetEtxOfEnq()".
  /*--------------------------------------------------------------------------*/
  private class GetEnq implements DataAvailableEvent
  {
    public void dataAvailableEvent(int count)
    {
      getEnq_dataAvailableEvent(count);
    }
  }

  void getEnq_dataAvailableEvent(int count)
  {
    //logger.logDebug("Count: " + count + "  RBC: " + receivedByteCount + " - getEnq_dataAvailableEvent()");
    //
    // Just get the byte before the ETX.
    //
    receivedByteCount = count;
    //logger.logDebug("GetEnq Count: " + count + "  RBC: " + receivedByteCount);
  }

  /*--------------------------------------------------------------------------*/
  // This "OnTriggerData" handler detects the ETX at the end of the message and
  // as part of that process, generates an "OnDataAvailable" Event that
  // actually gets the data.  So, all we have to do here is remove the data
  // trigger that found the ETX.
  //
  // We come here AFTER we come to "GetEnq()".
  /*--------------------------------------------------------------------------*/
  private class GetEtxOfEnq implements DataMatchEvent
  {
    public void dataMatchEvent()
    {
      getEtxOfEnq_dataMatchEvent();
    }
  }
  
  void getEtxOfEnq_dataMatchEvent()
  {
    //logger.logDebug("GetEtxOfEnq()");
    byte vbResponse = inputProtocolByteBuffer[receivedByteCount - 1];
    switch (vbResponse)
    {
      case PortConsts.ENQ:
        //
        // We have received an ENQ, we are in a Neutral State, so send an ACK
        // and wait for a text message.
        //
        //logger.logDebug("Received ENQ - GetEnq()");
        mzNakTextResponseSent = false;
        mnTextResponseNakCount = 0;
        //logger.logDebug("Cancel mpT4EnqWaitAfterEOTSentTimeout");
        timers.cancel(mpT4EnqWaitAfterEOTSentTimeout);
        setupReceiveCycle(State.WAITING_FOR_TEXT);
        sendACK();
        break;
      case PortConsts.EOT:
        //logger.logDebug("Received EOT - GetEnq()");
        setupReceiveCycle(State.NEUTRAL);
        break;
      case PortConsts.ACK:
        //logger.logDebug("Received ACK - GetEnq()");
        setupReceiveCycle(State.NEUTRAL);
        break;
      case PortConsts.NAK:
        //logger.logDebug("Received NAK - GetEnq()");
        setupReceiveCycle(State.NEUTRAL);
        break;
      default:
        //
        // NOT just a protocol byte - Maybe a Text Message?
        //
        if (verifyReceivedData(false))
        {
          //logger.logDebug("Received Text - GetEnq()");
          setupReceiveCycle(State.NEUTRAL);
        }
        else
        {
          //logger.logDebug("Received Junk - GetEnq()");
          setupReceiveCycle(State.NEUTRAL);
          sendNAK();
        }         
        break;
    }
  }

  /*--------------------------------------------------------------------------*/
  // Port's "OnDataAvailable" Handler to receive a DELIMITED message
  // terminated by an "ETX" (or some other delimiting unique data byte(s)).
  // When we come here the received message has already been buffered for us and
  // the ETX has been automatically discarded.
  //
  // We come here BEFORE we get to "GetEtxOfEnqResponse()".
  /*--------------------------------------------------------------------------*/
  private class GetEnqResponse implements DataAvailableEvent
  {
    public void dataAvailableEvent(int count)
    {
      getEnqResponse_dataAvailableEvent(count);
    }
  }

  void getEnqResponse_dataAvailableEvent(int count)
  {
    //logger.logDebug("Count: " + count + "  RBC: " + receivedByteCount + " - getEnqResponse_dataAvailableEvent()");
    receivedByteCount = count;
    //logger.logDebug("GetEnqResponse Count: " + count + "  RBC: " + receivedByteCount);
  }

  /*--------------------------------------------------------------------------*/
  // This "OnTriggerData" handler detects the ETX at the end of the message and
  // as part of that process, generates an "OnDataAvailable" Event that
  // actually gets the data.  So, all we have to do here is remove the data
  // trigger that found the ETX.
  //
  // We come here AFTER we come to "GetEnqResponse()".
  /*--------------------------------------------------------------------------*/
  private class GetEtxOfEnqResponse implements DataMatchEvent
  {
    public void dataMatchEvent()
    {
      getEtxOfEnqResponse_dataMatchEvent();
    }
  }
  
  void getEtxOfEnqResponse_dataMatchEvent()
  {
    //logger.logDebug("GetEtxOfEnqResponse()");
    //logger.logDebug("Cancel mpT1EnqResponseWaitTimeout");
    timers.cancel(mpT1EnqResponseWaitTimeout);
    //logger.logDebug("Cancel mpT2orT3MainEnqResendTimeout");
    timers.cancel(mpT2orT3MainEnqResendTimeout);
    //
    // Just get the byte before the ETX.
    //
    byte vbResponse = inputProtocolByteBuffer[receivedByteCount - 1];
    switch (vbResponse)
    {
      case PortConsts.ACK:
        //
        // Our ENQ has been ACK'ed - Send the Text Message.
        //
        //logger.logDebug("ACK Received - GetEnqResponse()");
        mzTextResent = false;
        setupReceiveCycle(State.WAITING_FOR_TEXT_RESPONSE);
        sendBufferedMessage();
       break;
      case PortConsts.ENQ:
        //
        // The dreaded ENQ collision.
        //
        //logger.logDebug("ENQ Collision - GetEnqResponse()");
        setupReceiveCycle(State.NEUTRAL);
        if (mzUseRandomT2orT3Interval)
        {
          int vnT2orT3MainEnqResendInterval = mnMinRandomT2orT3Interval
              + mpRandom.nextInt(mnMaxRandomT2orT3Interval);
          //logger.logDebug("Start mpT2orT3MainEnqResendTimeout - " + vnT2orT3MainEnqResendInterval);
          timers.setSSTimerEvent(mpT2orT3MainEnqResendTimeout, vnT2orT3MainEnqResendInterval);
        }
        else
        {
          //logger.logDebug("Start mpT2orT3MainEnqResendTimeout - " + mnT2orT3MainEnqResendInterval);
          timers.setSSTimerEvent(mpT2orT3MainEnqResendTimeout, mnT2orT3MainEnqResendInterval);
        }
        break;
      case PortConsts.EOT:
        //
        // Resend the ENQ.
        //
        //logger.logDebug("Received EOT - GetEnqResponse()");
        setupReceiveCycle(State.WAITING_FOR_ENQ_RESPONSE);
        sendENQ();
        break;
      case PortConsts.NAK:
        //
        // Resend the ENQ.
        //
        //logger.logDebug("Received NAK - GetEnqResponse()");
        setupReceiveCycle(State.WAITING_FOR_ENQ_RESPONSE);
        sendENQ();
        break;
      default:
        //
        // NOT just a protocol byte - Maybe a Text Message?
        //
        if (verifyReceivedData(false))
        {
          //logger.logDebug("Received Text - GetEnqResponse()");
          setupReceiveCycle(State.WAITING_FOR_ENQ_RESPONSE);
          sendENQ();
        }
        else
        {
          //logger.logDebug("Received Junk - GetEnqResponse()");
          setupReceiveCycle(State.WAITING_FOR_ENQ_RESPONSE);
          sendENQ();
        }         
        break;
    }
  }

  /*--------------------------------------------------------------------------*/
  // Port's "OnDataAvailable" Handler to receive a DELIMITED message
  // terminated by an "ETX" (or some other delimiting unique data byte(s)).
  // When we come here the received message has already been buffered for us and
  // the ETX has been automatically discarded.
  //
  // We come here BEFORE we get to "GetEtxOfText()".
  /*--------------------------------------------------------------------------*/
  private class GetText implements DataAvailableEvent
  {
    public void dataAvailableEvent(int count)
    {
      getText_dataAvailableEvent(count);
    }
  }

  void getText_dataAvailableEvent(int count)
  {
    //logger.logDebug("Count: " + count + "  RBC: " + receivedByteCount + " - getText_dataAvailableEvent()");
    receivedByteCount = count;
    //logger.logDebug("GetEnq Count: " + count + "  RBC: " + receivedByteCount);
  }

  /*--------------------------------------------------------------------------*/
  // This "OnTriggerData" handler detects the ETX at the end of the message and
  // as part of that process, generates an "OnDataAvailable" Event that
  // actually gets the data.  So, all we have to do here is remove the data
  // trigger that found the ETX.
  //
  // We come here AFTER we come to "GetText()".
  /*--------------------------------------------------------------------------*/
  private class GetEtxOfText implements DataMatchEvent
  {
    public void dataMatchEvent()
    {
      getEtxOfText_dataMatchEvent();
    }
  }
  
  void getEtxOfText_dataMatchEvent()
  {
    //logger.logDebug("GetEtxOfText()");
    //logger.logDebug("Cancel mpResponseTransmitWaitTimeout");
    timers.cancel(mpResponseTransmitWaitTimeout);
    byte vbResponse = inputProtocolByteBuffer[receivedByteCount - 1];
    switch (vbResponse)
    {
      case PortConsts.ENQ:
        //
        // Send an ACK - go back to waiting for text.
        //
        //logger.logDebug("ENQ Received - GetText()");
        mnTextResponseNakCount = 0;
        setupReceiveCycle(State.WAITING_FOR_TEXT);
        //
        // Before we send an ACK to what may be a spurious ENQ, send
        // a NAK.  If the next response is another ENQ we will follow
        // the original protocol.
        if (mzNakTextResponseSent)
        {
          sendACK();
        }
        else
        {
          mzNakTextResponseSent = true;
          sendNAK();
        }
        break;
      case PortConsts.EOT:
        //
        // Go to NEUTRAL state.
        //
        //logger.logDebug("EOT Received - GetText()");
        //logger.logDebug("Cancel mpT1TextResponseWaitTimeout");
        timers.cancel(mpT1TextResponseWaitTimeout);
        setupReceiveCycle(State.NEUTRAL);
        if (! bufferedMessageQueue.isEmpty())
        {
          sendENQ();
        }
        break;
      case PortConsts.ACK:
        //
        // Go back to waiting for text.
        //
        //logger.logDebug("ACK Received - GetText()");
        mnTextResponseNakCount = 0;
        setupReceiveCycle(State.WAITING_FOR_TEXT);
        break;
      case PortConsts.NAK:
        //
        // Go back to waiting for text.
        //
        //logger.logDebug("NAK Received - GetText()");
        mnTextResponseNakCount = 0;
        setupReceiveCycle(State.WAITING_FOR_TEXT);
        break;
      default:
        //
        // NOT just a protocol byte - Maybe the expected Text Message?
        //
        mzDuplicateSequenceNumber = false;
        boolean vzDataOk = verifyReceivedData(true);
        if ((vzDataOk) || ((! vzDataOk ) && (mzDuplicateSequenceNumber)))
        {
          //logger.logDebug("Data Verified OK - GetText() - rcvdCnt: " + receivedByteCount +
          //    "  \"" + receivedDataString + "\" <<<-------<<<");
          //logger.logDebug("Cancel mpT1TextResponseWaitTimeout");
          timers.cancel(mpT1TextResponseWaitTimeout);
          setupReceiveCycle(State.WAITING_FOR_EOT);
          sendACK();
          if (! mzDuplicateSequenceNumber)
          {
            if (! msCurrentReceivedData.equals(msPreviousReceivedData))
            {
              msPreviousReceivedData = msCurrentReceivedData;
              publishEquipmentEvent(receivedDataString, 0);
            }
          }
        }
        else
        {
          //logger.logDebug("Data NOT Verified - GetText() - rcvdCnt: " + receivedByteCount +
          //    "  \"" + receivedDataString + "\" <<<-------<<<");
          if (mnTextResponseNakCount >  mnMaxTextResponseNaks)
          {
            //
            // Go to NEUTRAL state.
            //
            setupReceiveCycle(State.NEUTRAL);
            sendNAK();
            if (! bufferedMessageQueue.isEmpty())
            {
              sendENQ();
            }
          }
          else
          {
            ++mnTextResponseNakCount;
            setupReceiveCycle(State.WAITING_FOR_TEXT);
            sendNAK();
          }
        }         
        break;
    }
  }

  /*--------------------------------------------------------------------------*/
  // Port's "OnDataAvailable" Handler to receive a DELIMITED message
  // terminated by an "ETX" (or some other delimiting unique data byte(s)).
  // When we come here the received message has already been buffered for us and
  // the ETX has been automatically discarded.
  //
  // We come here BEFORE we get to "GetEtxOfTextResponse()".
  /*--------------------------------------------------------------------------*/
  private class GetTextResponse implements DataAvailableEvent
  {
    public void dataAvailableEvent(int count)
    {
      getTextResponse_dataAvailableEvent(count);
    }
  }

  void getTextResponse_dataAvailableEvent(int count)
  {
    //logger.logDebug("Count: " + count + "  RBC: " + receivedByteCount + " - getTextResponse_dataAvailableEvent()");
    receivedByteCount = count;
    //logger.logDebug("GetEnq Count: " + count + "  RBC: " + receivedByteCount);
  }

  /*--------------------------------------------------------------------------*/
  // This "OnTriggerData" handler detects the ETX at the end of the message and
  // as part of that process, generates an "OnDataAvailable" Event that
  // actually gets the data.  So, all we have to do here is remove the data
  // trigger that found the ETX.
  //
  // We come here AFTER we come to "GetTextResponse()".
  /*--------------------------------------------------------------------------*/
  private class GetEtxOfTextResponse implements DataMatchEvent
  {
    public void dataMatchEvent()
    {
      getEtxOfTextResponse_dataMatchEvent();
    }
  }
  
  void getEtxOfTextResponse_dataMatchEvent()
  {
    logger.logDebug("GetEtxOfTextResponse()");
    //logger.logDebug("Cancel mpT1TextResponseWaitTimeout");
    timers.cancel(mpT1TextResponseWaitTimeout);
    byte vbResponse = inputProtocolByteBuffer[receivedByteCount - 1];
    switch (vbResponse)
    {
      case PortConsts.ENQ:
        //logger.logDebug("ENQ Received - GetTextResponse()");
        setupReceiveCycle(State.WAITING_FOR_TEXT_RESPONSE);
        break;
      case PortConsts.EOT:
        //
        // Send an EOT.
        //
        //logger.logDebug("EOT Received - GetTextResponse()");
        setupReceiveCycle(State.WAITING_FOR_EOT_TIMEOUT);
        sendEOT();
        break;
      case PortConsts.ACK:
        //logger.logDebug("ACK Received - GetTextResponse()");
        if (comPortStatus == ControllerConsts.STATUS_RUNNING)
        {
          //
          // We're still running - assume message was transmitted Ok and delete it.
          // If we're NOT still running assume the message did not go out and leave
          // it in the DataList for later re-transmission.
          //
          bufferedMessageQueue.removeFirst();
          baConditionedTransmitData = null;
        }
        setupReceiveCycle(State.WAITING_FOR_EOT_TIMEOUT);
        sendEOT();
        break;
      case PortConsts.NAK:
        //
        // Resend text.
        //
        //logger.logDebug("NAK Received - GetTextResponse()");
        if (mnTextSendCount > mnMaxTextResends)
        {
          setupReceiveCycle(State.WAITING_FOR_EOT_TIMEOUT);
          sendEOT();
        }
        else
        {
          mnTextSendCount++;
          setupReceiveCycle(State.WAITING_FOR_TEXT_RESPONSE);
          sendBufferedMessage();
        }
        break;
      default:
        //
        // NOT just a protocol byte - Maybe a Text Message?
        //
        // In the "PortDevice" base-class "verifyReceivedData" converts the received data
        // in "inputProtocolByteBuffer" of length "receivedByteCount" to a String.
        //
        // The CHILD Protocol should override "verifyReceivedData" to perform any
        // type of data verification (checksum, sequence number, etc.) before the
        // received data STRING is published to the Inter-Process-Message bus.
        //
        // The String "receivedDataString" is updated by this method.
        //
        if (verifyReceivedData(false))
        {
          //logger.logDebug("Received Text Verified - GetTextResponse()");
          setupReceiveCycle(State.WAITING_FOR_TEXT_RESPONSE);
        }
        else
        {
          //logger.logDebug("Received Junk - GetTextResponse()");
          //
          // Send an EOT.
          //
          setupReceiveCycle(State.WAITING_FOR_EOT_TIMEOUT);
          sendEOT();
        }         
        break;
    }
  }

  /*--------------------------------------------------------------------------*/
  // Port's "OnDataAvailable" Handler to receive a DELIMITED message
  // terminated by an "ETX" (or some other delimiting unique data byte(s)).
  // When we come here the received message has already been buffered for us and
  // the ETX has been automatically discarded.
  //
  // We come here BEFORE we get to "GetEtxOfEot()".
  /*--------------------------------------------------------------------------*/
  private class GetEot implements DataAvailableEvent
  {
    public void dataAvailableEvent(int count)
    {
      getEot_dataAvailableEvent(count);
    }
  }

  void getEot_dataAvailableEvent(int count)
  {
    //logger.logDebug("Count: " + count + "  RBC: " + receivedByteCount + " - getEot_dataAvailableEvent()");
    receivedByteCount = count;
    //logger.logDebug("GetEnq Count: " + count + "  RBC: " + receivedByteCount);
  }

  /*--------------------------------------------------------------------------*/
  // This "OnTriggerData" handler detects the ETX at the end of the message and
  // as part of that process, generates an "OnDataAvailable" Event that
  // actually gets the data.  So, all we have to do here is remove the data
  // trigger that found the ETX.
  //
  // We come here AFTER we come to "GetEot()".
  /*--------------------------------------------------------------------------*/
  private class GetEtxOfEot implements DataMatchEvent
  {
    public void dataMatchEvent()
    {
      getEtxOfEot_dataMatchEvent();
    }
  }
  
  void getEtxOfEot_dataMatchEvent()
  {
    //logger.logDebug("GetEtxOfEot()");
    //logger.logDebug("Cancel mpResponseTransmitWaitTimeout");
    timers.cancel(mpResponseTransmitWaitTimeout);
    boolean vzENQReceived = false;
    byte vbResponse = inputProtocolByteBuffer[receivedByteCount - 1];
    switch (vbResponse)
    {
      case PortConsts.ENQ:
        //logger.logDebug("ENQ Received - GetEot()");
        //
        // We have received an ENQ, send an ACK and wait for a text message.
        //
        vzENQReceived = true;
        mzNakTextResponseSent = false;
        mnTextResponseNakCount = 0;
        setupReceiveCycle(State.WAITING_FOR_TEXT);
        sendACK();
        break;
      case PortConsts.EOT:
        //logger.logDebug("EOT Received - Normal End - GetEot()");
        break;
      case PortConsts.ACK:
        //logger.logDebug("ACK Received - GetEot()");
        break;
      case PortConsts.NAK:
        //logger.logDebug("NAK Received - GetEot()");
        break;
      default:
        //
        // NOT just a protocol byte - Maybe a Text Message?
        //
        if (verifyReceivedData(false))
        {
          //logger.logDebug("Data Verified OK - GetText() - rcvdCnt: " + receivedByteCount +
          //    "  \"" + receivedDataString + "\" <<<-------<<<");
        }
        else
        {
          logger.logDebug("Data NOT Verified - GetText() - rcvdCnt: "
              + receivedByteCount + "  \"" + receivedDataString
              + "\" <<<-------<<<");
        }         
        break;
    }
    if (! vzENQReceived)
    {
      //
      // Go to NEUTRAL state.
      //
      setupReceiveCycle(State.NEUTRAL);
      if (! bufferedMessageQueue.isEmpty())
      {
        sendENQ();
      }
    }
  }

  /*--------------------------------------------------------------------------*/
  // In the "PortDevice" base-class "verifyReceivedData" converts the received data
  // in "inputProtocolByteBuffer" of length "receivedByteCount" to a String.
  //
  // The CHILD Protocol should override "verifyReceivedData" to perform any
  // type of data verification (checksum, sequence number, etc.) before the
  // received data STRING is published to the Inter-Process-Message bus.
  //
  // The String "receivedDataString" is updated by this method.
  /*--------------------------------------------------------------------------*/
  /**
   * Confirm that the incoming data packet meets the protocol specification.
   * Verify that all data is displayable ASCII (0x20-0x7e), the sequence number
   * is not the same as in the previous message, and the received BCC (Block
   * Check Character, or Checksum) matches our computed BCC.
   */
  protected boolean verifyReceivedData(boolean izSetRcvdSequenceNumber)
  {
    boolean bResult = false;
    dataVerifier:
    {
      if (receivedByteCount <= 0)
      {
        //logger.logDebug("verifyReceivedData() -- receivedByteCount = " + receivedByteCount + "  <<<-----------------<<<");
        break dataVerifier;
      }
      //
      // Go back from end of message to find STX.
      //
      int vnCount = 0;
      for (int i = receivedByteCount -1; (i >= 0 ); --i)
      {
        if (inputProtocolByteBuffer[i] == PortConsts.STX)
        {
          System.arraycopy(inputProtocolByteBuffer, i+1, inputProtocolByteBuffer, 0, vnCount);
          receivedByteCount = vnCount;
          break;
        }
        ++vnCount;
      }
      if (!receivedDataAllAsciiDisplayable())
      {
        break dataVerifier;
      }
      if (!receivedChecksumOK())
      {
        break dataVerifier;
      }
      if (!verifyReceivedSequenceNumber(izSetRcvdSequenceNumber))
      {
        break dataVerifier;
      }
      //
      // We DO have a verified message from the AGC.  Convert the Message to a
      // String (dropping the Sequence Number and Checksum).
      //
      // But first, make a copy that still has tne Sequence Number and Checksum.
      // This will help us find duplicate messages that the protocol might let through.
      //
      msCurrentReceivedData = new String(inputProtocolByteBuffer, 0, receivedByteCount);
      int iMsgLen = receivedByteCount - SEQUENCE_NUMBER_LENGTH - CHECKSUM_LENGTH;
      receivedDataString = new String(inputProtocolByteBuffer, SEQUENCE_NUMBER_LENGTH, iMsgLen);
      bResult = true;
      break dataVerifier;
    } // dataVerifier
    return bResult;
  }

  /*--------------------------------------------------------------------------*/
  private void incReceivedSequenceNumber()
  {
    mnReceivedSequenceNumber++;
    if (mnReceivedSequenceNumber > MAX_SEQUENCE_NUMBER)
    {
      mnReceivedSequenceNumber = MIN_SEQUENCE_NUMBER;
    }
  }

  /*--------------------------------------------------------------------------*/
  protected boolean verifyReceivedSequenceNumber(boolean izSetRcvdSequenceNumber)
  {
    boolean bResult = true;
    byte b0 = inputProtocolByteBuffer[0];
    byte b1 = inputProtocolByteBuffer[1];
    byte b2 = inputProtocolByteBuffer[2];
    byte b3 = inputProtocolByteBuffer[3];
    if ((b0 < 0x30) || (b0 > 0x39))
    {
      bResult = false;
    }
    if ((b1 < 0x30) || (b1 > 0x39))
    {
      bResult = false;
    }
    if ((b2 < 0x30) || (b2 > 0x39))
    {
      bResult = false;
    }
    if ((b3 < 0x30) || (b3 > 0x39))
    {
      bResult = false;
    }
    if (!bResult)
    {
      logger.logDebug("verifyReceivedSequenceNumber() - ASCII 0-9 OUT-OF-RANGE");
      return bResult;
    }
    int i0 = b0 - 0x30;
    int i1 = b1 - 0x30;
    int i2 = b2 - 0x30;
    int i3 = b3 - 0x30;
    int vnReceivedSequenceNumber = (i0 * 1000) +
                                  (i1 * 100) +
                                  (i2 * 10) +
                                  (i3);

    if (izSetRcvdSequenceNumber)
    {
      //logger.logDebug("Expected: " +
      //  mnReceivedSequenceNumber + "  Received: " + vnReceivedSequenceNumber + " - verifyReceivedSequenceNumber()");
    }
    if (vnReceivedSequenceNumber == mnReceivedSequenceNumber)
    {
      //
      // The Received Sequence Number IS what we were expecting - OK.
      //
      //logger.logDebug("verifyReceivedSequenceNumber() - OK " + iReceivedSequenceNumber);
      if (izSetRcvdSequenceNumber)
      {
        incReceivedSequenceNumber();
      }
    }
    else
    {
      if (vnReceivedSequenceNumber == 0)
      {
        //
        // The Received Sequence Number is NOT what we were expecting, but a
        // zero is OK.
        //
        if (izSetRcvdSequenceNumber)
        {
          //logger.logDebug("verifyReceivedSequenceNumber() - Expected: " +
          //    mnReceivedSequenceNumber + "  Received: " + vnReceivedSequenceNumber);
          mnReceivedSequenceNumber = MIN_SEQUENCE_NUMBER;
        }
      }
      else
      {
        if (izSetRcvdSequenceNumber)
        {
          int vnPreviousSequenceNumber = mnReceivedSequenceNumber - 1;
          if (vnPreviousSequenceNumber < MIN_SEQUENCE_NUMBER)
          {
            vnPreviousSequenceNumber = MAX_SEQUENCE_NUMBER;
          }
          if (vnReceivedSequenceNumber == vnPreviousSequenceNumber)
          {
            //
            // The Received Sequence Number is NOT what we were expecting, but IS
            // the previous Sequence Number - assume it is a duplicate and can
            // be ignored.
            //
            logger.logDebug("verifyReceivedSequenceNumber() - DUPLICATE - "
                + vnReceivedSequenceNumber);
            mzDuplicateSequenceNumber = true;
            bResult = false;
          }
          else
          {
            //
            // The Received Sequence Number is NOT what we were expecting, and
            // it is NOT a duplicate, so assume it is OK (and resync our expected
            // Sequence Number).
            //
            //logger.logDebug("verifyReceivedSequenceNumber() - INCORRECT - " + vnReceivedSequenceNumber);
            mnReceivedSequenceNumber = vnReceivedSequenceNumber;
            incReceivedSequenceNumber();
          }
        }
      }
    }
    return bResult;
  }

  /*--------------------------------------------------------------------------*/
  protected byte[] addTransmittedChecksum(String sEquipmentData)
  {
    sEquipmentData += CHECKSUM_STRING;
    int len = sEquipmentData.length() - CHECKSUM_LENGTH;
    byte[] baData = sEquipmentData.getBytes();
    byte calculatedChecksum = 0;
    int i = 0;
    while (i < len)
    {
      calculatedChecksum ^= baData[i];
      i++;
    }
    baData[i] = (byte)CHAR_DIGITS[(calculatedChecksum >> 4) & 0x0f];
    i++;
    baData[i] = (byte)CHAR_DIGITS[calculatedChecksum & 0x0f];
    return baData;
  }

  /*--------------------------------------------------------------------------*/
  private boolean receivedChecksumOK()
  {
    boolean bResult = true;
    byte calculatedChecksum = 0;
    for (int i = 0; (i < (receivedByteCount - CHECKSUM_LENGTH)); i++)
    {
      calculatedChecksum ^= inputProtocolByteBuffer[i];
    }
    int b = inputProtocolByteBuffer[receivedByteCount - CHECKSUM_LENGTH];
    if ((b >= 'A') && (b <= 'F'))
    {
      b -= 7; // remove letter offset.
    }
    b = (b & 0x0f) << 4; // Shift to high nibble.
    int receivedChecksum = b;
    b = inputProtocolByteBuffer[receivedByteCount - (CHECKSUM_LENGTH - 1)];
    if ((b >= 'A') && (b <= 'F'))
    {
      b -= 7; // remove letter offset.
    }
    b = (b & 0x0f); // Get low nibble.
    receivedChecksum |= b;
    if (calculatedChecksum != (byte)receivedChecksum)
    {
      String sCalc = ASCII_DIGITS[calculatedChecksum / 0x10] +
                     ASCII_DIGITS[calculatedChecksum % 0x10];
      String sRcvd = ASCII_DIGITS[receivedChecksum / 0x10] +
                     ASCII_DIGITS[receivedChecksum % 0x10];
      logger.logDebug("NOT OK - Calculated: " +
                      sCalc + "  Received: " + sRcvd + "  - receivedChecksumOK()");
      bResult = false;
    }
    return bResult;
  }
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  // Timer Event handlers
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private class T1EnqResponseWaitTimeout extends RestartableTimerTask
  {
    /*------------------------------------------------------------------------*/
    /**
     * run -- the LocalTimerTask's run() needs to be synchronized on
     * thisController so that any work we do here is not interrupted by any
     * incoming messages or events that we generate here. We want to complete
     * anything we do here without being preempted.
     */
    public void run()
    {
      synchronized(AGCBasicPort.this)
      {
        //logger.logDebug("T1EnqResponseWaitTimeout - sendENQ()");
        sendENQ();
      }
    }
  }
  
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private class T1TextResponseWaitTimeout extends RestartableTimerTask
  {
    /*------------------------------------------------------------------------*/
    /**
     * run -- the LocalTimerTask's run() needs to be synchronized on
     * thisController so that any work we do here is not interrupted by any
     * incoming messages or events that we generate here. We want to complete
     * anything we do here without being preempted.
     */
    public void run()
    {
      synchronized(AGCBasicPort.this)
      {
        if (mzTextResent)
        {
          //logger.logDebug("T1TextResponseWaitTimeout - sendEOT()");
          setupReceiveCycle(State.WAITING_FOR_EOT_TIMEOUT);
          sendEOT();
        }
        else
        {
          //logger.logDebug("T1TextResponseWaitTimeout - Resend Text Message");
          mzTextResent = true;
          setupReceiveCycle(State.WAITING_FOR_TEXT_RESPONSE);
          sendBufferedMessage();
        }
      }
    }
  }
  
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private class T2orT3MainEnqResendTimeout extends RestartableTimerTask
  {
    /**
     * run -- the LocalTimerTask's run() needs to be synchronized on
     * thisController so that any work we do here is not interrupted by any
     * incoming messages or events that we generate here. We want to complete
     * anything we do here without being preempted.
     */
    public void run()
    {
      synchronized(AGCBasicPort.this)
      {
        //logger.logDebug("T2orT3MainEnqResendTimeout - sendENQ()");
        sendENQ();
      }
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private class T4EnqWaitAfterEOTSentTimeout extends RestartableTimerTask
  {
    /**
     * run -- the LocalTimerTask's run() needs to be synchronized on
     * thisController so that any work we do here is not interrupted by any
     * incoming messages or events that we generate here. We want to complete
     * anything we do here without being preempted.
     */
    public void run()
    {
      synchronized(AGCBasicPort.this)
      {
        //logger.logDebug("T4EnqWaitAfterEOTSentTimeout() - Current State " + meCurrentState);
//        State veState = State.WAITING_FOR_EOT_TIMEOUT;
//        State veState1 = meCurrentState;
        if (meCurrentState == State.WAITING_FOR_EOT_TIMEOUT)
        {
          //
          // Go to NEUTRAL state.
          //
          setupReceiveCycle(State.NEUTRAL);
          if (! bufferedMessageQueue.isEmpty())
          {
            sendENQ();
          }
        }
        else
        {
          //logger.logDebug("T4EnqWaitAfterEOTSentTimeout() - ELSE Current State " + meCurrentState);
        }
      }
    }
  }
  
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private class ResponseTransmitWaitTimeout extends RestartableTimerTask
  {
    /**
     * run -- the LocalTimerTask's run() needs to be synchronized on
     * thisController so that any work we do here is not interrupted by any
     * incoming messages or events that we generate here. We want to complete
     * anything we do here without being preempted.
     */
    public void run()
    {
      synchronized(AGCBasicPort.this)
      {
        //logger.logDebug("ResponseTransmitWaitTimeout() - Current State " + meCurrentState);
        switch (meCurrentState)
        {
          case WAITING_FOR_TEXT:
            //logger.logDebug("Start mpResponseTransmitWaitTimeout");
            if (mnTextResponseNakCount >  mnMaxTextResponseNaks)
            {
              //
              // Go to NEUTRAL state.
              //
              setupReceiveCycle(State.NEUTRAL);
              sendNAK();
              if (! bufferedMessageQueue.isEmpty())
              {
                sendENQ();
              }
            }
            else
            {
              ++mnTextResponseNakCount;
              setupReceiveCycle(State.WAITING_FOR_TEXT);
              sendNAK();
            }
            break;
          case WAITING_FOR_EOT:
            //
            // Go to NEUTRAL state.
            //
            setupReceiveCycle(State.NEUTRAL);
            if (! bufferedMessageQueue.isEmpty())
            {
              sendENQ();
            }
            break;
            
          case NEUTRAL:
          case SHUTDOWN:
          case WAITING_FOR_ENQ_RESPONSE:
          case WAITING_FOR_EOT_TIMEOUT:
          case WAITING_FOR_TEXT_RESPONSE:
            // Do nothing
            break;
        }
      }
    }
  }
  
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
    return Factory.create(AGCBasicPort.class);
  }
}
