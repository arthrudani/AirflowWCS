package com.daifukuamerica.wrxj.emulation.scale;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.device.port.PortController;
import com.daifukuamerica.wrxj.factory.Factory;

public class ScaleEmulatorPort extends PortController
{
  //
  // Protocol specific data.
  //
  protected byte[] mabEndDelimiter = { 0x0A };
  protected byte[] mabOutEndDelimeter = { 0x0D, 0x0A };
  protected BeginReceiveCycle mpbeginReceiveCycle = null;
  GetEtxOfMessage mpgetEtxOfMessage = null;
  GetEndDelimitedMessage mpgetEndDelimitedMessage = null;
  private byte[] mabkeepAlive = { 0x03} ;
  private String msKeepAliveResponse = "ES";

  public ScaleEmulatorPort()
  {

  }

  void DataMatchEvent()
  {
    processReceivedDataBlock();
  }
    
    /*--------------------------------------------------------------------------*/
  // Setup our Block Protocol to Start a Receive Cycle.
  /*--------------------------------------------------------------------------*/
  protected void setupReceiveCycle()
  {
    removeAllDataMatchEvents();
    receivedByteCount = 0;           // Show we didn't receive anything.
    inputProtocolSink = 0;
    //
    // Just get the first byte.
    //
    setDataAvailableEvent(mpbeginReceiveCycle, 1);
    enableEventProcessing();   // We need to explicitly enable data match checking
  }
  
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
    if (verifyReceivedData())
    {
      //Do not send the keep alive to the device
      if(!receivedDataString.equals(new String(mabkeepAlive)))
      {
        publishEquipmentEvent(receivedDataString, 0);
      }
      else
      {
        transmitEquipmentData(msKeepAliveResponse);
      }
    }
    setupReceiveCycle();
  
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
    logger.logDebug("ScaleEmulatorPort.initialize()");
  }
  
  /*--------------------------------------------------------------------------*/
  @Override
  public void startup()
  { 
    super.startup();
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Shuts down this controller by cancelling any timers and shutting down the
   * communication ports.
   */
  /*--------------------------------------------------------------------------*/
  @Override
  public void shutdown()
  {
    
    super.shutdown();
    logger.logDebug("ScaleEmulatorPort -- shutdown() -- Start");
    logger.logDebug("ScaleEmulatorPort -- shutdown() -- End");
  }
  
  @Override
  protected void startupProtocol()
  {
    logger.logDebug("ScaleEmulatorPort.startupProtocol() - Start");
    logger.logDebug("ScaleEmulatorPort.startupProtocol() - End");
  }
  
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  @Override
  protected void shutdownProtocol()
  {
    super.shutdownProtocol();
    removeAllDataMatchEvents();
    logger.logDebug("ScalePort.shutdownProtocol() - Start");
    receivedByteCount = 0;           // Show we didn't receive anything.
    inputProtocolSink = 0;
    
    mpbeginReceiveCycle = null;
    mpbeginReceiveCycle = null;
    mpgetEndDelimitedMessage = null;
    mpgetEtxOfMessage = null;
    logger.logDebug("ScalePort.shutdownProtocol() - End");
  }
  
  /*
   * Method set data match event to find the end of the message
   */
  protected void getMessage()
  {
    removeAllDataMatchEvents();
    addDataMatchEvent(mpgetEtxOfMessage, mabEndDelimiter, 2);
    setDataAvailableEvent(mpgetEndDelimitedMessage, 0);
    enableEventProcessing();
  }
  
  /*--------------------------------------------------------------------------*/
  /**
   *  Give the data to be transmitted to the ComPort.  Add Start & End Delimiters
   *  (if needed) and convert the Data String to a byte array before transmitting iy.
   */
  /*--------------------------------------------------------------------------*/
  @Override
  protected void transmitEquipmentData(String isEquipmentData)
  {
    //
    // In the "PortDevice" base-class "conditionDataToTransmit" converts the
    // "equipmentData" String to a byte-array.
    //
    // The CHILD Protocol should override "conditionDataToTransmit" to perform
    // any type of data conditioning (checksum, sequence number, etc.) before
    // the data BYTE-ARRAY is transmitted to the ComPort (and the actual equipment
    // that is connected to the ComPort).
    byte[] vabEquipmentDataByteArray = conditionDataToTransmit(isEquipmentData);
    int vnByteArrayLen = vabEquipmentDataByteArray.length;
    int vnTotalDataLength = vnByteArrayLen + mabOutEndDelimeter.length;
    System.arraycopy(vabEquipmentDataByteArray, 0,
        outputByteBuffer, 0, vnByteArrayLen);
    System.arraycopy(mabOutEndDelimeter, 0,
        outputByteBuffer, vnByteArrayLen, mabOutEndDelimeter.length);
    putBlock(outputByteBuffer, vnTotalDataLength);
  }
  
  protected void scaleInitializer()
  {
    mpgetEtxOfMessage = new GetEtxOfMessage();
    mpbeginReceiveCycle = new BeginReceiveCycle();
    mpgetEndDelimitedMessage = new GetEndDelimitedMessage();
  }
   
  @Override
  protected void startupComPort()
   {
     scaleInitializer();
     setupReceiveCycle();
     super.startupComPort();
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
  private class GetEndDelimitedMessage implements DataAvailableEvent
  {
    public void dataAvailableEvent(int count)
    {
      getEndDelimitedMessage_dataAvailableEvent(count);
    }
  }

  void getEndDelimitedMessage_dataAvailableEvent(int count)
  {
    receivedByteCount += count;
  }
 
  /*--------------------------------------------------------------------------*/
  // This "OnTriggerData" handler detects the ETX at the end of the message and
  // as part of that process, generates an "OnDataAvailable" Event that
  // actually gets the data.  So, all we have to do here is remove the data
  // trigger that found the ETX.
  //
  // We come here AFTER we come to "getEndDelimitedMessage()".
  /*--------------------------------------------------------------------------*/
  private class GetEtxOfMessage implements DataMatchEvent
  {
    public void dataMatchEvent()
    {
      getEtxOfMessage_dataMatchEvent();
    }
  }

  void getEtxOfMessage_dataMatchEvent()
  {
    // We've found the end-of-text ETX - just remove data trigger that found it
    // (the data trigger to find another start-of-message is still there too, so
    // just remove all data match triggers)
    //
    removeAllDataMatchEvents();
    processReceivedDataBlock();   // We're done
  }
  
  /*--------------------------------------------------------------------------*/
  // Port's "OnDataAvailable" Handler to start a Receive Cycle (we set this up
  // in "setupReceiveCycle").  Just get the first byte of a message when it
  // comes in.  We go to another "OnDataAvailable" handler to get the rest of the
  // message.
  /*--------------------------------------------------------------------------*/
  private class BeginReceiveCycle implements DataAvailableEvent
  {
    public void dataAvailableEvent(int count)
    {
      beginReceiveCycle_dataAvailableEvent(count);
    }
   }

  void beginReceiveCycle_dataAvailableEvent(int count)
  {
            receivedByteCount = 1;
            inputProtocolSink = 1;
            getMessage();
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
    return Factory.create(ScaleEmulatorPort.class);
  }


}
