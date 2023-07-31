package com.daifukuamerica.wrxj.device.controls;

/**
 * Title:        WRx-J
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      SK Daifuku
 * @author       Stephen Kendorski
 * @version 1.0
 */

import com.daifukuamerica.wrxj.clc.ControllerDefinition;
import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPortServer;
import com.daifukuamerica.wrxj.dbadapter.data.PortData;
import com.daifukuamerica.wrxj.device.port.PortConsts;
import com.daifukuamerica.wrxj.device.port.block.BlockSendReceivePort;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.timer.RestartableTimerTask;
import java.util.Arrays;

/**
 * ControlsPort is a Class that communicates with a PLC (or Think & Do).
 * The Class can handle the protocol for either the Controller or Equipment
 * (or Emulator).
 * 
 * <BR><BR>This class assumes that messages are in the following formats:
 * <LI><I>Message:</I> <code>[STX][SEQ(4)][Message][ETX]</code></LI>
 * <LI><I>Keep-Alive:</I> <code>[ETX]</code></LI>
 * <LI><I>ACK/NAK:</I> None
 * 
 * <BR><BR>This class also assumes that the sequence number starts at 1,
 * and that the maximum sequence number = (10^x)-1 where x is the number of
 * bytes in the sequence number.
 */
public class ControlsPort extends BlockSendReceivePort
{
  private static final int NO_ACTIVITY_TIME = 15000;//1*60*1000; // 1 min
  protected static final int MIN_SEQUENCE_NUMBER = 1;

  protected int mnTransmittedSequenceNumber = 0;
  protected int mnReceivedSequenceNumber = 0;
  protected int mnSequenceNumberLength = 4;
  protected int mnMaxSequenceNumber = (int)Math.pow(10, mnSequenceNumberLength) - 1;

  protected int mnKeepAliveInterval = NO_ACTIVITY_TIME;
  protected NoActivityTimeout mpNoActivityTimeout = new NoActivityTimeout();
  
  protected int mnReceivedKeepAliveInterval = NO_ACTIVITY_TIME + 10000;
  protected ReceivedKeepAliveTimeout mpReceivedKeepAliveTimeout = new ReceivedKeepAliveTimeout();

  protected byte[] mabKeepAlive = {PortConsts.ETX};
  protected boolean mzUniqueSequenceNumbers = false;
  
  protected Integer mpLock = 1;

  /*--------------------------------------------------------------------------*/
  /**
   *  Class constructor.  Create a Port to communicate with a PLC (or Think & Do).
   */
  public ControlsPort()
  {
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Starts up the communications device by creating this ComDevice's actual
   * ComPort (communication physical layer).
   * 
   * @param aControllerKeyName the unique name that identifies this instance of
   *            Controller
   */
  /*--------------------------------------------------------------------------*/
  @Override
  public void initialize(String aControllerKeyName)
  {
    super.initialize(aControllerKeyName);
    logger.logDebug("initialize() - Start");
    bufferDataWhenNotRunning = true;
    logger.logDebug("initialize() - End");
  }

  /*--------------------------------------------------------------------------*/
  @Override
  public void startup()
  {
    super.startup();
    logger.logDebug("startup() - Start");
    
    String vsName = getConfigProperty(ControllerDefinition.CONTROLLER_NAME);
    PortData vpPort = Factory.create(PortData.class);
    StandardPortServer vpPortServer = Factory.create(StandardPortServer.class);
    try
    {
      vpPort = vpPortServer.getPort(vsName);
      vpPortServer.cleanUp();
    }
    catch (DBException ex)
    {
      logger.logException(vsName + " can't find my Port record.", ex);
    }

    if (vpPort != null)
    {
      // Keep-Alive interval, WRx -> Device
      mnKeepAliveInterval = vpPort.getSndKeepAliveInterval();
      // Keep-Alive interval, Device -> WRx
      mnReceivedKeepAliveInterval = vpPort.getRcvKeepAliveInterval();
    }
    else
    {
      logger.logError(vsName + " can't find my Port record (null).  Using defaults."); 
    }
    
    // Sequence numbers
    String sUniqueSequenceNumbers = getConfigProperty("UniqueSequenceNumbers");
    if (sUniqueSequenceNumbers != null)
    {
      sUniqueSequenceNumbers = sUniqueSequenceNumbers.substring(0,1);
      if ((sUniqueSequenceNumbers.equalsIgnoreCase("Y")) ||
          (sUniqueSequenceNumbers.equalsIgnoreCase("T")))
      {
       mzUniqueSequenceNumbers = true;
      }
    }
    if (mzUniqueSequenceNumbers)
    {
      logger.logDebug("startup() - Unique Sequence Numbers");
    }
    else
    {
      logger.logDebug("startup() - Accept Duplicate Sequence Numbers");
    }
    logger.logDebug("startup() - End");
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
    logger.logDebug("shutdown() -- Start");
    logger.logDebug("shutdown() -- End");
    super.shutdown();
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  @Override
  protected void startupProtocol()
  {
    super.startupProtocol();
    logger.logDebug("startupProtocol() - Start");
    if (mnKeepAliveInterval > 0)
    {
      if (mpNoActivityTimeout == null)
      {
        mpNoActivityTimeout = new NoActivityTimeout();
      }
      timers.setSSTimerEvent(mpNoActivityTimeout, mnKeepAliveInterval);
      logger.logDebug("startupProtocol() -- Sending \"Keep-Alive\"");
      putBlock(mabKeepAlive, mabKeepAlive.length);
    }
    if (mnReceivedKeepAliveInterval > 0)
    {
      if (mpReceivedKeepAliveTimeout == null)
      {
        mpReceivedKeepAliveTimeout = new ReceivedKeepAliveTimeout();
      }
    }
    logger.logDebug("startupProtocol() - End");
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  @Override
  protected void shutdownProtocol()
  {
    super.shutdownProtocol();
    logger.logDebug("shutdownProtocol() - Start");
    if (mpNoActivityTimeout != null)
    {
      timers.cancel(mpNoActivityTimeout);
      mpNoActivityTimeout = null;
    }
    if (mpReceivedKeepAliveTimeout != null)
    {
      timers.cancel(mpReceivedKeepAliveTimeout);
      mpReceivedKeepAliveTimeout = null;
    }
    logger.logDebug("shutdownProtocol() - End");
  }

  @Override
  protected void transmitEquipmentData(String equipmentData)
  {
    super.transmitEquipmentData(equipmentData);
    if (mnKeepAliveInterval > 0)
    {
      timers.setSSTimerEvent(mpNoActivityTimeout, mnKeepAliveInterval);
    }
  }

  @Override
  protected void transmitEquipmentData(byte[] equipmentData)
  {
    super.transmitEquipmentData(equipmentData);
    if (mnKeepAliveInterval > 0)
    {
      timers.setSSTimerEvent(mpNoActivityTimeout, mnKeepAliveInterval);
    }
  }

  /*--------------------------------------------------------------------------*/
  @Override
  protected void processReceivedDataBlock()
  {
    super.processReceivedDataBlock();
    if (mnReceivedKeepAliveInterval > 0)
    {
      timers.setSSTimerEvent(mpReceivedKeepAliveTimeout, mnReceivedKeepAliveInterval);
    }
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
   * Add the message sequence number to the data packet.
   */
  @Override
  protected byte[] conditionDataToTransmit(String sEquipmentData)
  {
    sEquipmentData = getTransmittedSequenceNumber() + sEquipmentData;
    //
    // Return the BYTE ARRAY we need to transmit.
    //
    byte[] baData = sEquipmentData.getBytes();
    return baData;
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
   * Verify that all data is displayable ASCII (0x20-0x7e), and the sequence number
   * is not the same as in the previous message.
   */
  @Override
  protected boolean verifyReceivedData()
  {
    boolean bResult = false;
    dataVerifier:
    {
      if (receivedByteCount <= 0)
      {
        logger.logDebug("verifyReceivedData() -- receivedByteCount = "
            + receivedByteCount + "  <<<-----------------<<<");
        break dataVerifier;
      }
      if (!receivedDataAllAsciiDisplayable())
      {
        break dataVerifier;
      }
      if (!verifyReceivedSequenceNumber())
      {
        break dataVerifier;
      }
      //
      // We DO have a verified message from the device. Convert the Message to a
      // String (dropping the Sequence Number).
      //
      int iMsgLen = receivedByteCount - mnSequenceNumberLength;
      receivedDataString = new String(inputProtocolByteBuffer, mnSequenceNumberLength, iMsgLen);
      bResult = true;
      break dataVerifier;
    } // dataVerifier
    return bResult;
  }

  /*--------------------------------------------------------------------------*/
  protected String getTransmittedSequenceNumber()
  {
    String sResult = "" + mnTransmittedSequenceNumber;
    while (sResult.length() < mnSequenceNumberLength)
    {
      sResult = "0" + sResult;
    }

    mnTransmittedSequenceNumber++;
    if (mnTransmittedSequenceNumber > mnMaxSequenceNumber)
    {
      mnTransmittedSequenceNumber = MIN_SEQUENCE_NUMBER;
    }
    return sResult;
  }

  /*--------------------------------------------------------------------------*/
  protected void incReceivedSequenceNumber()
  {
    mnReceivedSequenceNumber++;
    if (mnReceivedSequenceNumber > mnMaxSequenceNumber)
    {
      mnReceivedSequenceNumber = MIN_SEQUENCE_NUMBER;
    }
  }

  /**
   * Validate the Received Sequence Number
   * @return true if okay, false if not
   */
  protected boolean verifyReceivedSequenceNumber()
  {
    boolean vzResult = true;
    
    int vnReceivedSequence = 0;
    for (int i = 0; i < mnSequenceNumberLength; i++)
    {
      if (inputProtocolByteBuffer[i] < 0x30 || inputProtocolByteBuffer[i] > 0x39)
      {
        logger.logDebug("verifyReceivedSequenceNumber() - ASCII 0-9 OUT-OF-RANGE");
        return false;
      }
      vnReceivedSequence = vnReceivedSequence * 10 + (inputProtocolByteBuffer[i] - 0x30);
    }
    
    if (vnReceivedSequence == mnReceivedSequenceNumber)
    {
      //
      // The Received Sequence Number IS what we were expecting - OK.
      //
      incReceivedSequenceNumber();
    }
    else
    {
      if (vnReceivedSequence == 0)
      {
        //
        // The Received Sequence Number is NOT what we were expecting, but a
        // zero is OK.
        //
        logger.logDebug("verifyReceivedSequenceNumber() - EXPECTED "
            + mnReceivedSequenceNumber + "  RECEIVED " + vnReceivedSequence);
        mnReceivedSequenceNumber = MIN_SEQUENCE_NUMBER;
      }
      else
      {
        int previousSequenceNumber = mnReceivedSequenceNumber - 1;
        if (previousSequenceNumber < MIN_SEQUENCE_NUMBER)
        {
          previousSequenceNumber = mnMaxSequenceNumber;
        }
        if (vnReceivedSequence == previousSequenceNumber)
        {
          //
          // The Received Sequence Number is NOT what we were expecting, but IS
          // the previous Sequence Number - assume it is a duplicate and can
          // be ignored.
          //
          logger.logDebug("verifyReceivedSequenceNumber() - DUPLICATE - "
              + vnReceivedSequence);
          if (mzUniqueSequenceNumbers)
          {
            vzResult = false;
          }
        }
        else
        {
          //
          // The Received Sequence Number is NOT what we were expecting, and
          // it is NOT a duplicate, so assume it is OK (and resync our expected
          // Sequence Number).
          //
          logger.logDebug("verifyReceivedSequenceNumber() - INCORRECT - "
              + vnReceivedSequence);
          mnReceivedSequenceNumber = vnReceivedSequence;
          incReceivedSequenceNumber();
        }
      }
    }
    return vzResult;
  }

  
  /*========================================================================*/
  /*  Methods to ease extension                                             */
  /*========================================================================*/
  
  /**
   * Set the keep-alive message
   * @param iabKeepAlive - must include any STX/ETX/whatever
   */
  protected void setKeepAliveMessage(byte[] iabKeepAlive)
  {
    mabKeepAlive = Arrays.copyOf(iabKeepAlive, iabKeepAlive.length);
  }
  
  /**
   * Set the length of the sequence number 
   * @param inBytes (valid range is 1 - 6)
   */
  protected void setSequenceLength(int inBytes)
  {
    if (inBytes < 1 || inBytes > 6)
    {
      throw new IllegalArgumentException("Valid range is 1 - 6 (" + inBytes + ")");
    }
    mnSequenceNumberLength = inBytes;
    mnMaxSequenceNumber = (int)Math.pow(10, mnSequenceNumberLength) - 1;
  }


  /*========================================================================*/
  /* Timer Event handlers                                                   */
  /*========================================================================*/

  /**
   * Send the keep-alive message if we don't send anything for the time 
   * specified by mnKeepAliveInterval
   */
  public class NoActivityTimeout extends RestartableTimerTask
  {
    public void run()
    {
      noActivityTimeout_run();
    }
  }

  /**
   * noActivityTimeout_run -- the NoActivityTimeout run() needs to be
   * synchronized so that any work we do here is not interrupted by any incoming
   * messages or events that we generate here. We want to complete anything we
   * do here without being preempted.
   */
  protected void noActivityTimeout_run()
  {
    synchronized (mpLock)
    {
      mpLock = 3;

      putBlock(mabKeepAlive, mabKeepAlive.length);
      if (mnKeepAliveInterval > 0)
      {
        timers.setSSTimerEvent(mpNoActivityTimeout, mnKeepAliveInterval);
      }
    }
  }

  /**
   * Reset the port if we don't receive any messages for the time specified by
   * mnReceivedKeepAliveInterval
   */
  public class ReceivedKeepAliveTimeout extends RestartableTimerTask
  {
    public void run()
    {
      receivedKeepAliveTimeout_run();
    }
  }

  /**
   * receivedKeepAliveTimeout_run -- the ReceivedKeepAliveTimeout's run() needs
   * to be synchronized so that any work we do here is not interrupted by any
   * incoming messages or events that we generate here. We want to complete
   * anything we do here without being preempted.
   */
  private void receivedKeepAliveTimeout_run()
  {
    synchronized (mpLock)
    {
      mpLock = 2;
      setControllerStatus(ControllerConsts.STATUS_ERROR);
      logger.logOperation(LogConsts.OPR_DEVICE, "ReceivedKeepAliveTimeout - Shutting Down Port");
      shutdownProtocol();
      shutdownComPort();
      logger.logDebug("ReceivedKeepAliveTimeout - startupComPort()");
      setControllerStatus(ControllerConsts.STATUS_STARTING);
      startupComPort();
      //
      // startupProtocol() will be called when ComPort Status goes to running.
      //
    }
  }
  
  /*--------------------------------------------------------------------------*/
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
    return new ControlsPort();
  }
}
