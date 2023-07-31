package com.daifukuamerica.wrxj.device.arc9y;

/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      SKDC Corp.
 * @author Stephen Kendorski
 * @version 1.0
 */

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.device.agc.AGCBasicPort;
import com.daifukuamerica.wrxj.device.port.PortConsts;

/**
 * Arc9yPort is a Class that communicates with a Daifuku AGC or SRC using the 9y
 * message set. The Class can handle the protocol for either the Controller or
 * Equipment (or Emulator).
 */
public class Arc9yPort extends AGCBasicPort
{
  /**
   * Class constructor.
   */
  public Arc9yPort()
  {
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
      int iMsgLen = receivedByteCount - SEQUENCE_NUMBER_LENGTH;
      receivedDataString = new String(inputProtocolByteBuffer, SEQUENCE_NUMBER_LENGTH, iMsgLen);
      bResult = true;
      break dataVerifier;
    } // dataVerifier
    return bResult;
  }

  /*--------------------------------------------------------------------------*/
  @Override
  protected byte[] addTransmittedChecksum(String sEquipmentData)
  {
    return sEquipmentData.getBytes();
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
    return new Arc9yPort();
  }
}
