package com.daifukuamerica.wrxj.device.agc;

/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      SKDC Corp.
 * @author Stephen Kendorski
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

/**
 * AGCPort is a Class that communicates with a Daifuku AGC (AS21 Control System).
 * The Class can handle the protocol for either the Controller or Equipment
 * (or Emulator).
 */
public class AGCPort extends BlockSendReceivePort
{
  private static final int NO_ACTIVITY_TIME = 60000;//1*60*1000; // 1 min
  private static final int CHECKSUM_LENGTH = 2;
  protected static final int SEQUENCE_NUMBER_LENGTH = 4;
  private static final int MAX_SEQUENCE_NUMBER = 9999;
  private static final int MIN_SEQUENCE_NUMBER = 1;
  private static final String CHECKSUM_STRING = "00";
  private static final String[] ASCII_DIGITS =
     {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
  private static final char[] CHAR_DIGITS =
     {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

  private int transmittedSequenceNumber = 0;
  private int receivedSequenceNumber = 0;

  int keepAliveInterval = NO_ACTIVITY_TIME;
  NoActivityTimeout noActivityTimeout = new NoActivityTimeout();
  int receivedKeepAliveInterval = NO_ACTIVITY_TIME + 10000;
  private ReceivedKeepAliveTimeout receivedKeepAliveTimeout = new ReceivedKeepAliveTimeout();

  byte[] keepAlive = {PortConsts.ETX};

  /*--------------------------------------------------------------------------*/
  /**
   *  Class constructor.  Create a Port to communicate with a Daifuku AGC (AS21
   *  Control System).
   */
  public AGCPort()
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
    logger.logDebug("AGCPort.initialize()");
  }

  /*--------------------------------------------------------------------------*/
  @Override
  public void startup()
  {
    super.startup();
    logger.logDebug("AGCPort.startup() - Start");

    String vsName = getConfigProperty(ControllerDefinition.CONTROLLER_NAME);
    PortData vpPort = null;
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
      keepAliveInterval = vpPort.getSndKeepAliveInterval();
      // Keep-Alive interval, Device -> WRx
      receivedKeepAliveInterval = vpPort.getRcvKeepAliveInterval();
    }
    else
    {
      logger.logError(vsName + " can't find my Port record (null).  Using defaults."); 
    }

    logger.logDebug("AGCPort.startup() - End");
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
  @Override
  protected void startupProtocol()
  {
    super.startupProtocol();
    logger.logDebug("AGCPort.startupProtocol() - Start");
    if (keepAliveInterval > 0)
    {
      if (noActivityTimeout == null)
      {
        noActivityTimeout = new NoActivityTimeout();
      }
      timers.setSSTimerEvent(noActivityTimeout, keepAliveInterval);
      logger.logDebug("AGCPort.startupProtocol() -- Sending \"Keep-Alive\"");
      putBlock(keepAlive, keepAlive.length);
    }
    if (receivedKeepAliveInterval > 0)
    {
      if (receivedKeepAliveTimeout == null)
      {
        receivedKeepAliveTimeout = new ReceivedKeepAliveTimeout();
      }
    }
    logger.logDebug("AGCPort.startupProtocol() - End");
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  @Override
  protected void shutdownProtocol()
  {
    super.shutdownProtocol();
    logger.logDebug("AGCPort.shutdownProtocol() - Start");
    if (noActivityTimeout != null && timers != null)
    {
      timers.cancel(noActivityTimeout);
      noActivityTimeout = null;
    }
    if (receivedKeepAliveTimeout != null && timers != null)
    {
      timers.cancel(receivedKeepAliveTimeout);
      receivedKeepAliveTimeout = null;
    }
    logger.logDebug("AGCPort.shutdownProtocol() - End");
  }

  @Override
  protected void transmitEquipmentData(String equipmentData)
  {
    super.transmitEquipmentData(equipmentData);
    if (keepAliveInterval > 0)
    {
      timers.setSSTimerEvent(noActivityTimeout, keepAliveInterval);
    }
  }

  /*--------------------------------------------------------------------------*/
  @Override
  protected void processReceivedDataBlock()
  {
    super.processReceivedDataBlock();
    if (receivedKeepAliveInterval > 0)
    {
      timers.setSSTimerEvent(receivedKeepAliveTimeout, receivedKeepAliveInterval);
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
   * Add the message sequence number and compute and add the BCC (Block Check Character,
   * or Checksum) to the data packet.
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
  @Override
  protected boolean verifyReceivedData()
  {
    boolean bResult = false;
    dataVerifier:
    {
      if (receivedByteCount <= 0)
      {
        logger.logDebug("verifyReceivedData() -- receivedByteCount = " + receivedByteCount + "  <<<-----------------<<<");
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
      if (!receivedChecksumOK())
      {
        break dataVerifier;
      }
      //
      // We DO have a verified message from the AGC.  Convert the Message to a
      // String (dropping the Sequence Number and Checksum).
      //
      int iMsgLen = receivedByteCount - SEQUENCE_NUMBER_LENGTH - CHECKSUM_LENGTH;
      receivedDataString = new String(inputProtocolByteBuffer, SEQUENCE_NUMBER_LENGTH, iMsgLen);
      bResult = true;
      break dataVerifier;
    } // dataVerifier
    return bResult;
  }

  /*--------------------------------------------------------------------------*/
  @Override
  protected void seeIfKeepAlive()
  {
    if ((inputProtocolByteBuffer[0] == keepAlive[0]) &&
        (receivedKeepAliveInterval > 0) &&
        (receivedKeepAliveTimeout != null))
    {
      timers.setSSTimerEvent(receivedKeepAliveTimeout, receivedKeepAliveInterval);
    }
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
  private void incReceivedSequenceNumber()
  {
    receivedSequenceNumber++;
    if (receivedSequenceNumber > MAX_SEQUENCE_NUMBER)
    {
      receivedSequenceNumber = MIN_SEQUENCE_NUMBER;
    }
  }

  /*--------------------------------------------------------------------------*/
  protected boolean verifyReceivedSequenceNumber()
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
    int iReceivedSequenceNumber = (i0 * 1000) +
                                  (i1 * 100) +
                                  (i2 * 10) +
                                  (i3);

    if (iReceivedSequenceNumber == receivedSequenceNumber)
    {
      //
      // The Received Sequence Number IS what we were expecting - OK.
      //
      //logger.logDebug("verifyReceivedSequenceNumber() - OK " + iReceivedSequenceNumber);
      incReceivedSequenceNumber();
    }
    else
    {
      if (iReceivedSequenceNumber == 0)
      {
        //
        // The Received Sequence Number is NOT what we were expecting, but a
        // zero is OK.
        //
        logger.logDebug("verifyReceivedSequenceNumber() - Expected: " +
               receivedSequenceNumber + "  Received: " + iReceivedSequenceNumber);
        receivedSequenceNumber = MIN_SEQUENCE_NUMBER;
      }
      else
      {
        int previousSequenceNumber = receivedSequenceNumber - 1;
        if (previousSequenceNumber < MIN_SEQUENCE_NUMBER)
        {
          previousSequenceNumber = MAX_SEQUENCE_NUMBER;
        }
        if (iReceivedSequenceNumber == previousSequenceNumber)
        {
          //
          // The Received Sequence Number is NOT what we were expecting, but IS
          // the previous Sequence Number - assume it is a duplicate and can
          // be ignored.
          //
          logger.logDebug("verifyReceivedSequenceNumber() - DUPLICATE - " + iReceivedSequenceNumber);
          bResult = false;
        }
        else
        {
          //
          // The Received Sequence Number is NOT what we were expecting, and
          // it is NOT a duplicate, so assume it is OK (and resync our expected
          // Sequence Number).
          //
          logger.logDebug("verifyReceivedSequenceNumber() - INCORRECT - " + iReceivedSequenceNumber);
          receivedSequenceNumber = iReceivedSequenceNumber;
          incReceivedSequenceNumber();
        }
      }
    }
    return bResult;
  }

  /*--------------------------------------------------------------------------*/
  private byte[] addTransmittedChecksum(String sEquipmentData)
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
      logger.logError("Message BCC Failed - Calculated: " +
                      sCalc + "  Received: " + sRcvd + " Sequence # "+ receivedSequenceNumber );
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
  // The No-Activity timer needs to send a "keep-alive" byte.
  /*--------------------------------------------------------------------------*/
  private class NoActivityTimeout extends RestartableTimerTask
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
      noActivityTimeout_run();
    }
  }

  void noActivityTimeout_run()
  {
    synchronized(AGCPort.this)
    {
      putBlock(keepAlive, keepAlive.length);
      if (keepAliveInterval > 0)
      {
        timers.setSSTimerEvent(noActivityTimeout, keepAliveInterval);
      }
   }
  }
  /*--------------------------------------------------------------------------*/
  // The ReceivedKeepAlive timer needs to shutdown the port.
  /*--------------------------------------------------------------------------*/
  private class ReceivedKeepAliveTimeout extends RestartableTimerTask
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
      receivedKeepAliveTimeout_run();
    }
  }

  void receivedKeepAliveTimeout_run()
  {
    synchronized(AGCPort.this)
    {
      logger.logOperation(LogConsts.OPR_DEVICE, "ReceivedKeepAlive Timeout (" +
                receivedKeepAliveInterval + " msec) - Shutting Down Port");
      String s = "ReceivedKeepAliveTimeout (" + receivedKeepAliveInterval + " msec) - Shutting Down ComPort";
      logger.logTxByteCommunication(s.getBytes(), 0, s.length());
      setControllerStatus(ControllerConsts.STATUS_ERROR);
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
    return Factory.create(AGCPort.class);
  }
}
