package com.daifukuamerica.wrxj.device.port.block;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.device.port.PortConsts;
import com.daifukuamerica.wrxj.device.port.PortController;
import com.daifukuamerica.wrxj.timer.RestartableTimerTask;

/**
 * A base class communication device that handles transmitting and receiving
 * of data packets.
 * 
 * @author Stephen Kendorski
 * @version 1.1
 */
public class BlockSendReceivePort extends PortController
{
  //
  // Protocol specific data.
  //
  /**
   * The data byte packet terminating sequence.
   */
  protected byte[] endDelimiter = {PortConsts.ETX};//null;
  /**
   * The size (in bytes) of the data byte packet terminating sequence.
   */
  protected int endDelimiterLength = 1;//0;
  /**
   * The data byte packet beginning sequence.
   */
  protected byte[] startDelimiter = {PortConsts.STX};//null;
  /**
   * The size (in bytes) of the data byte packet beginning sequence.
   */
  protected int startDelimiterLength = 1;//0;
  protected boolean protocolStripsAddsDelimiters = true;

  protected int blockProtocolType = PortConsts.START_END_DELIMITED_BLOCK_PROTOCOL;
  protected int receiveCycleTime = 500;
  protected int fixedBlockLength = 0;

  protected ReceiveCycleTimeout receiveCycleTimeout = null;

  protected BeginReceiveCycle beginReceiveCycle = null;
  protected GetStxOfMessage getStxOfMessage = null;
  protected GetFixedLengthMessage getFixedLengthMessage = null;
  protected GetEndDelimitedMessage getEndDelimitedMessage = null;
  protected GetEtxOfMessage getEtxOfMessage = null;

  /*--------------------------------------------------------------------------*/
  /**
   *  Class constructor.  Create a bare minimum of a Port.  Whoever
   *  created this Controller will then call <tt>startup</tt> which should then
   *  do anything useful to get this devices commpunication port(s) running.
   */
  public BlockSendReceivePort()
  {
  }
  
  protected void setBlockProtocolType(int iiBlockProtocolType)
  {
    blockProtocolType = iiBlockProtocolType;
  }

  protected void setFixedBlockLength(int iiFixedBlockLength)
  {
    fixedBlockLength = iiFixedBlockLength;
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  @Override
  public void initialize(String aControllerKeyName)
  {
    super.initialize(aControllerKeyName);
    logger.logDebug("BlockSendReceivePort -- initialize()");
  }

  /*--------------------------------------------------------------------------*/
  @Override
  public void startup()
  {
    super.startup();
    logger.logDebug("BlockSendReceivePort -- startup()");
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Shuts down this controller by cancelling any timers and shutting down the
   * communication ports.
   * 
   * <P><I>Note: Yes, it does not make a whole lot of sense to override this 
   * method and then only call super.shutdown(), but this method is called 
   * rarely and it make the code more understandable, so I'm leaving it here.
   * </I></P>
   */
  @Override
  public void shutdown()
  {
    super.shutdown();
  }


  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  // Protocol-Specific methods..
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * We have received a block of data meeting the protocol requirements.
   * Publish it to the Inter-Process-Communication Message bus and then setup
   * to begin a new receive cycle.
   *
   * <p>The received data packet is in
   * {@link com.daifukuamerica.wrxj.common.device.port.PortController#inputProtocolByteBuffer inputProtocolByteBuffer}
   * and the length of the received data packet is
   * {@link com.daifukuamerica.wrxj.common.device.port.PortController#receivedByteCount receivedByteCount}.
   */
  protected void processReceivedDataBlock()
  {
    //logger.logDebug("processReceivedDataBlock()");
    transmitCycleActive = false;
    if (receiveCycleTime > 0)
    {
      timers.cancel(receiveCycleTimeout); // We're done with this if we're here
    }
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
    if (verifyReceivedData())
    {
      //logger.logDebug("Data Verified OK - rcvdCnt: " + receivedByteCount +
        //  "  \"" + receivedDataString + "\" - processReceivedDataBlock() <<<-------<<<");
      publishEquipmentEvent(receivedDataString, 0);
    }
//    else
//      logger.logDebug("Data NOT Verified - rcvdCnt: " + receivedByteCount +
//          "  \"" + receivedDataString + "\" - processReceivedDataBlock() <<<-------<<<");
    setupReceiveCycle();
  }


  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  @Override
  protected void startupProtocol()
  {
    super.startupProtocol();
    logger.logDebug("BlockSendReceivePort.startupProtocol() - Start");
    receiveCycleTimeout = new ReceiveCycleTimeout();

    beginReceiveCycle = new BeginReceiveCycle();
    getStxOfMessage = new GetStxOfMessage();
    getFixedLengthMessage = new GetFixedLengthMessage();
    getEndDelimitedMessage = new GetEndDelimitedMessage();
    getEtxOfMessage = new GetEtxOfMessage();

    setupReceiveCycle();
    logger.logDebug("BlockSendReceivePort.startupProtocol() - End");
    //putBlock(bbfr, 0, testCount);//testing...?
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  @Override
  protected void shutdownProtocol()
  {
    super.shutdownProtocol();
    logger.logDebug("BlockSendReceivePort.shutdownProtocol() - Start");
    receiveCycleActive = false;
    removeAllDataMatchEvents();
    receivedByteCount = 0;           // Show we didn't receive anything.
    inputProtocolSink = 0;
    beginReceiveCycle = null;
    if ((receiveCycleTime > 0) && (receiveCycleTimeout != null))
    {
      timers.cancel(receiveCycleTimeout);
    }
    receiveCycleTimeout = null;

    beginReceiveCycle = null;
    getStxOfMessage = null;
    getFixedLengthMessage = null;
    getEndDelimitedMessage = null;
    getEtxOfMessage = null;
    logger.logDebug("BlockSendReceivePort.shutdownProtocol() - End");
  }

  /*--------------------------------------------------------------------------*/
  /**
   *  Give the data to be transmitted to the ComPort.  Add Start & End Delimiters
   *  (if needed) and convert the Data String to a byte array before transmitting iy.
   */
  /*--------------------------------------------------------------------------*/
  @Override
  protected void transmitEquipmentData(String equipmentData)
  {
    //
    // In the "PortDevice" base-class "conditionDataToTransmit" converts the
    // "equipmentData" String to a byte-array.
    //
    // The CHILD Protocol should override "conditionDataToTransmit" to perform
    // any type of data conditioning (checksum, sequence number, etc.) before
    // the data BYTE-ARRAY is transmitted to the ComPort (and the actual equipment
    // that is connected to the ComPort).
    //
    byte[] equipmentDataByteArray = conditionDataToTransmit(equipmentData);
    transmitEquipmentData(equipmentDataByteArray);
  }

  /*--------------------------------------------------------------------------*/
  /**
   *  Give the data to be transmitted to the ComPort.  Add Start & End Delimiters
   *  (if needed) and convert the Data String to a byte array before transmitting iy.
   */
  /*--------------------------------------------------------------------------*/
  protected void transmitEquipmentData(byte[] equipmentDataByteArray)
  {
    //
    // In the "PortDevice" base-class "conditionDataToTransmit" converts the
    // "equipmentData" String to a byte-array.
    //
    // The CHILD Protocol should override "conditionDataToTransmit" to perform
    // any type of data conditioning (checksum, sequence number, etc.) before
    // the data BYTE-ARRAY is transmitted to the ComPort (and the actual equipment
    // that is connected to the ComPort).
    //
    int byteArrayLen = equipmentDataByteArray.length;
    int totalDataLength = byteArrayLen;

    if (protocolStripsAddsDelimiters)
    {
      //
      // Add the Start & End Delimiters here so that the data is transmitted as
      // one block.
      //
      if ((blockProtocolType == PortConsts.START_DELIMITED_BLOCK_PROTOCOL) ||
          (blockProtocolType == PortConsts.START_END_DELIMITED_BLOCK_PROTOCOL) ||
          (blockProtocolType == PortConsts.START_DELIMITED_WITH_LENGTH_BLOCK_PROTOCOL))
      {
        totalDataLength += startDelimiterLength;
        System.arraycopy(startDelimiter, 0,
                         outputByteBuffer, 0, startDelimiterLength);
      }
      //
      System.arraycopy(equipmentDataByteArray, 0,
                       outputByteBuffer, startDelimiterLength, byteArrayLen);
      //
      if ((blockProtocolType == PortConsts.END_DELIMITED_BLOCK_PROTOCOL) ||
          (blockProtocolType == PortConsts.START_END_DELIMITED_BLOCK_PROTOCOL))
      {
        totalDataLength += endDelimiterLength;
        System.arraycopy(endDelimiter, 0,
                         outputByteBuffer, startDelimiterLength + byteArrayLen, endDelimiterLength);
      }
      //
      putBlock(outputByteBuffer, totalDataLength);
    }
    else
    {
      putBlock(equipmentDataByteArray, byteArrayLen);
    }
  }

  /*--------------------------------------------------------------------------*/
  protected void seeIfKeepAlive()
  {
  }
  
  /*--------------------------------------------------------------------------*/
  // Setup our Block Protocol to Start a Receive Cycle.
  /*--------------------------------------------------------------------------*/
  protected void setupReceiveCycle()
  {
    //logger.logDebug("setupReceiveCycle()");
    receiveCycleActive = false;
    removeAllDataMatchEvents();
    receivedByteCount = 0;           // Show we didn't receive anything.
    inputProtocolSink = 0;
    if ((receiveCycleTime > 0) && (receiveCycleTimeout != null))
    {
      timers.cancel(receiveCycleTimeout);
    }
    //
    // Just get the FIRST byte.
    //
    setDataAvailableEvent(beginReceiveCycle, 1);
    enableEventProcessing();   // We need to explicitly enable data match checking
  }

  /*--------------------------------------------------------------------------*/
  // Port's "OnDataAvailable" Handler to start a Receive Cycle (we set this up
  // in "setupReceiveCycle").  Just get the first byte of a message when it
  // comes in.  We go to another "OnDataAvailable" handler to get the rest of the
  // message.
  /*--------------------------------------------------------------------------*/
  public class BeginReceiveCycle implements DataAvailableEvent
  {
    public void dataAvailableEvent(int count)
    {
      beginReceiveCycle_dataAvailableEvent(count);
    }
   }

  public void beginReceiveCycle_dataAvailableEvent(int count)
  {
    //logger.logDebug("Count: " + count + " - BeginReceiveCycle()");
    //
    // Setup to receive the rest of the message based on the protocol type.
    //
    switch (blockProtocolType)
    {
      case PortConsts.VARIABLE_LENGTH_BLOCK_PROTOCOL:
        //
        // The "SendResponseTimer" will deal with this
        //
        break;
      case PortConsts.FIXED_LENGTH_BLOCK_PROTOCOL:
      {
        //
        // Fixed length -- we know exactly how many bytes to expect (and we
        // now have the first byte). Get the rest of the message.
        //
        receivedByteCount = count;
        if (fixedBlockLength != count)
        {
          setDataAvailableEvent(getFixedLengthMessage, fixedBlockLength - count);
        }
        else
        {
          //
          // We have all the data we need - we're done.
          //
          processReceivedDataBlock();
        }
      }
      break;
      case PortConsts.START_DELIMITED_BLOCK_PROTOCOL:
      case PortConsts.START_END_DELIMITED_BLOCK_PROTOCOL:
      case PortConsts.START_DELIMITED_WITH_LENGTH_BLOCK_PROTOCOL:
      {
        //
        // We (MAY) have the Start Delimiter for the message - see how to get the
        // rest of the message.
        //
        if (inputProtocolByteBuffer[0] == startDelimiter[0])
        {
          if (protocolStripsAddsDelimiters)
          {
            //
            // Discard any garbage before the Start Delimiter (and discard the
            // Start Delimiter, too).
            //
            receivedByteCount = 0;
            inputProtocolSink = 0;
          }
          getStartOfMessage();
        }
        else
        {
          seeIfKeepAlive();
          setupReceiveCycle();
        }
      }
      break;
      case PortConsts.END_DELIMITED_BLOCK_PROTOCOL:
      {
        //
        // End Delimited -- just wait for the terminator.
        //
        receivedByteCount = count;
      }
      break;
      case PortConsts.PORT_SPECIFIC_BLOCK_PROTOCOL:
      {
        getReceivedData(count);
      }
      break;
    }
  }
  
  /*--------------------------------------------------------------------------*/
  // We (MAY) come here after "BeginReceiveCycle"
  /*--------------------------------------------------------------------------*/
  protected void getReceivedData(int inCount)
  {
    receivedByteCount = getBlock(-1) + 1;
    processReceivedDataBlock();
  }
  
  /*--------------------------------------------------------------------------*/
  // We (MAY) come here after "BeginReceiveCycle"
  /*--------------------------------------------------------------------------*/
  protected void getStartOfMessage()
  {
    //logger.logDebug("getStartOfMessage()");
    if (receiveCycleActive)
    {
      //
      // We've received an STX and we're in a Receive Cycle - the previous STX
      // must have been a false start. Process this STX as a new Start of Message.
      //
      //logger.logDebug("FALSE Start While in ReceiveCycle - getStartOfMessage()");
      beginReceiveCycle.dataAvailableEvent(startDelimiterLength);
    }
    receiveCycleActive = true;
    removeAllDataMatchEvents();
    //
    // Setup to receive the rest of the message type of message
    //
    switch (blockProtocolType)
    {
      case PortConsts.VARIABLE_LENGTH_BLOCK_PROTOCOL:
      {
        //
        // The "SendResponseTimer" will deal with this
        //
      }
      break;
      case PortConsts.FIXED_LENGTH_BLOCK_PROTOCOL:
      {
        //
        // Fixed Length will NOT come here.
        //
      }
      break;
      case PortConsts.START_DELIMITED_BLOCK_PROTOCOL:
      case PortConsts.START_DELIMITED_WITH_LENGTH_BLOCK_PROTOCOL:
      {
        //logger.logDebug("getStartOfMessage()- setup getFixedLengthMessage(): " + fixedBlockLength);
        setDataAvailableEvent(getFixedLengthMessage, fixedBlockLength);
        //
        // And keep thinking we got a false start.
        //
        addDataMatchEvent(getStxOfMessage, startDelimiter, startDelimiterLength, true);
      }
      break;
      case PortConsts.START_END_DELIMITED_BLOCK_PROTOCOL:
      case PortConsts.END_DELIMITED_BLOCK_PROTOCOL:
      {
        //
        // End Delimited -- just wait for a terminator
        //
        setDataAvailableEvent(getEndDelimitedMessage, 0); // When we get the ETX process the message here
        addDataMatchEvent(getEtxOfMessage, endDelimiter, endDelimiterLength);
        //
        // And keep thinking we got a false start of message, so we want to
        // look for another Start Delimiter.
        //
        addDataMatchEvent(getStxOfMessage, startDelimiter, startDelimiterLength, true);
      }
      break;
    }
    //
    // Set a timer to deal with the data if the message never finishes.
    //
    if (receiveCycleTime > 0)
    {
      timers.setSSTimerEvent(receiveCycleTimeout, receiveCycleTime);
    }
    enableEventProcessing();   // We need to explicitly enable data match checking
  }

  /*--------------------------------------------------------------------------*/
  // We come here after "BeginReceiveCycle"
  /*--------------------------------------------------------------------------*/
  public class GetStxOfMessage implements DataMatchEvent
  {
    public void dataMatchEvent()
    {
      getStxOfMessage_dataMatchEvent();
    }
  }

  void getStxOfMessage_dataMatchEvent()
  {
    //logger.logDebug("GetStxOfMessage()");
    if (receiveCycleActive)
    {
      //
      // We've received an STX and we're in a Receive Cycle - the previous STX
      // must have been a false start. Process this STX as a new Start of Message.
      //
      //logger.logDebug("FALSE Start While in ReceiveCycle - GetStxOfMessage()");
      beginReceiveCycle.dataAvailableEvent(startDelimiterLength);
    }
    receiveCycleActive = true;
    removeAllDataMatchEvents();
    //
    // Setup to receive the rest of the message type of message
    //
    switch (blockProtocolType)
    {
      case PortConsts.VARIABLE_LENGTH_BLOCK_PROTOCOL:
      {
        //
        // The "SendResponseTimer" will deal with this
        //
      }
      break;
      case PortConsts.FIXED_LENGTH_BLOCK_PROTOCOL:
      {
        //
        // Fixed Length will NOT come here.
        //
      }
      break;
      case PortConsts.START_DELIMITED_BLOCK_PROTOCOL:
      case PortConsts.START_DELIMITED_WITH_LENGTH_BLOCK_PROTOCOL:
      {
        //logger.logDebug("GetStxOfMessage()- setup getFixedLengthMessage(): " + fixedBlockLength);
        setDataAvailableEvent(getFixedLengthMessage, fixedBlockLength);
        //
        // And keep thinking we got a false start.
        //
        addDataMatchEvent(getStxOfMessage, startDelimiter, startDelimiterLength, true);
      }
      break;
      case PortConsts.START_END_DELIMITED_BLOCK_PROTOCOL:
      case PortConsts.END_DELIMITED_BLOCK_PROTOCOL:
      {
        //
        // End Delimited -- just wait for a terminator
        //
        setDataAvailableEvent(getEndDelimitedMessage, 0); // When we get the ETX process the message here
        addDataMatchEvent(getEtxOfMessage, endDelimiter, endDelimiterLength);
        //
        // And keep thinking we got a false start of message, so we want to
        // look for another Start Delimiter.
        //
        addDataMatchEvent(getStxOfMessage, startDelimiter, startDelimiterLength, true);
      }
      break;
    }
    //
    // Set a timer to deal with the data if the message never finishes.
    //
    if (receiveCycleTime > 0)
    {
      timers.setSSTimerEvent(receiveCycleTimeout, receiveCycleTime);
    }
    enableEventProcessing();   // We need to explicitly enable data match checking
  }

  /*--------------------------------------------------------------------------*/
  // When we come here the received message has already been buffered for us.
  /*--------------------------------------------------------------------------*/
  public class GetFixedLengthMessage implements DataAvailableEvent
  {
    public void dataAvailableEvent(int count)
    {
      getFixedLengthMessage_dataAvailableEvent(count);
    }
  }

  void getFixedLengthMessage_dataAvailableEvent(int count)
  {
    //logger.logDebug("GetFixedLengthMessage()");
    switch (blockProtocolType)
    {
      case PortConsts.START_DELIMITED_WITH_LENGTH_BLOCK_PROTOCOL:
      {
        //
        // Fixed length -- we know exactly how many bytes to expect (and we have
        // the first byte!).
        //
        removeAllDataMatchEvents();
        //logger.logDebug("GetFixedLengthMessage Count: " + count + " ReceivedByteCount: " + receivedByteCount);
        receivedByteCount += count;
        processReceivedDataBlock();
        //
        // Get the Length of the Data Block + the Length of the Data Type.
        //
        //
//        aPtr = @ReceivingByteBuffer[ReceivedByteCount];
//        ReceivedData.FixedByteToInt(DataLen, aPtr);
//        int dataLen = dataLen +  ctFixedByteLength;
//        receivedByteCount = receivedByteCount + count;
        //logger.logDebug("GetFixedLengthMessage Count = ' + IntToStr(Count) + ' ReceivedByteCount = ' +
          //   IntToStr(ReceivedByteCount) + ' Getting ' + IntToStr(DataLen));
        //
        // Get all remaining data.
        //
//        setDataAvailableEvent(getStartDelimitedWithLengthMessage, dataLen {ReceivingByteBuffer[ReceivedByteCount]});

      }
      break;
      default:
      {
        receivedByteCount += count;
        //logger.logDebug("GetFixedLengthMessage Count = " + count + " RBC = " + receivedByteCount);
        processReceivedDataBlock();
      }
    }
  }

  /*--------------------------------------------------------------------------*/
  // We have the Message Data Block - Setup to get the ETX
  /*--------------------------------------------------------------------------*/
//  private class GetStartDelimitedWithLengthMessage implements DataAvailableEvent
//  {
//    public void dataAvailableEvent(int count)
//    {
//      getStartDelimitedWithLengthMessage_dataAvailableEvent(count);
//    }
//  }

  void getStartDelimitedWithLengthMessage_dataAvailableEvent(int count)
  {
    removeAllDataMatchEvents();
    receivedByteCount += count;
    //logger.logDebug("GetStartDelimitedWithLengthMessage Count = " + count + " RBC = " + receivedByteCount);
    setDataAvailableEvent(getEndDelimitedMessage, 0); // When we get the ETX process the message here
    addDataMatchEvent(getEtxOfMessage, endDelimiter, endDelimiterLength);
    enableEventProcessing();   // We need to explicitly enable data match checking
  }
  /*--------------------------------------------------------------------------*/
  // Port's "OnDataAvailable" Handler to receive a DELIMITED message
  // terminated by an "ETX" (or some other delimiting unique data byte(s)).
  // When we come here the received message has already been buffered for us and
  // the ETX has been automatically discarded.  We (may) need the ETX, so put
  // one into our buffer.
  //
  // We come here BEFORE we get to "GetEtxOfMessage()".
  /*--------------------------------------------------------------------------*/
  public class GetEndDelimitedMessage implements DataAvailableEvent
  {
    public void dataAvailableEvent(int count)
    {
      getEndDelimitedMessage_dataAvailableEvent(count);
    }
  }

  void getEndDelimitedMessage_dataAvailableEvent(int count)
  {
    receivedByteCount += count;
    if (!protocolStripsAddsDelimiters)
    {
      System.arraycopy(endDelimiter, 0,
                       inputProtocolByteBuffer, receivedByteCount, endDelimiterLength);
      receivedByteCount += endDelimiterLength;
    }
    //logger.logDebug("GetEndDelimitedMessage Count: " + count + "  RBC: " + receivedByteCount);
  }
  /*--------------------------------------------------------------------------*/
  // This "OnTriggerData" handler detects the ETX at the end of the message and
  // as part of that process, generates an "OnDataAvailable" Event that
  // actually gets the data.  So, all we have to do here is remove the data
  // trigger that found the ETX.
  //
  // We come here AFTER we come to "getEndDelimitedMessage()".
  /*--------------------------------------------------------------------------*/
  public class GetEtxOfMessage implements DataMatchEvent
  {
    public void dataMatchEvent()
    {
      getEtxOfMessage_dataMatchEvent();
    }
  }

  void getEtxOfMessage_dataMatchEvent()
  {
    //logger.logDebug("GetEtxOfMessage()");
    //
    // We've found the end-of-text ETX - just remove data trigger that found it
    // (the data trigger to find another start-of-message is still there too, so
    // just remove all data match triggers)
    //
    removeAllDataMatchEvents();
    processReceivedDataBlock();   // We're done
  }
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  // Timer Event handlers
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/

  /*--------------------------------------------------------------------------*/
  // The Receive-Cycle-timer deals with any (partial) messages when it expires.
  /*--------------------------------------------------------------------------*/
  public class ReceiveCycleTimeout extends RestartableTimerTask
  {
    /*------------------------------------------------------------------------*/
    /**
     * run -- the LocalTimerTask's run() needs to be synchronized on thisController
     * so that any work we do here is not interrupted by any incoming messages
     * or events that we generate here.  We want to complete anything we do here
     * without being pre-empted.
     */
    public void run()
    {
      receiveCycleTimeout_run();
    }
  }
  
  void receiveCycleTimeout_run()
  {
    synchronized(BlockSendReceivePort.this)
    {
      if (blockProtocolType == PortConsts.VARIABLE_LENGTH_BLOCK_PROTOCOL)
      {
        //
        // "GetStartOfMessage" already got the 1st byte of real message
        // Get all data in the receiver buffer
        //
        receivedByteCount = getBlock(-1) + 1;
        processReceivedDataBlock();
      }
      else
      {
        //logger.logDebug("BlockSendReceivePort.ReceiveCycleTimeout()");
        flushInputBuffer();  // Dump any partial messages
        setupReceiveCycle(); // And setup to Start a new receive cycle.
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
    return new BlockSendReceivePort();
  }
}