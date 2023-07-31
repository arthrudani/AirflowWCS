package com.daifukuamerica.wrxj.device.port;

/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 * @author Stephen Kendorski
 * @version 1.0
 */

public interface PortConsts
{
  /**
   * ASCII Start-Of-Text control code.
   */
  static final byte STX = 0x02;
  /**
   * ASCII End-Of-Text control code.
   */
  static final byte ETX = 0x03;
  /**
   * ASCII End-Of-Transmission control code.
   */
  static final byte EOT = 0x04;
  /**
   * ASCII Enquiry control code.
   */
  static final byte ENQ = 0x05;
  /**
   * ASCII Acknowledge control code.
   */
  static final byte ACK = 0x06;
  /**
   * ASCII Negative Acknowledge control code.
   */
  static final byte NAK = 0x15;
  
  /**
   * ComPort receive data buffer size (in bytes).
   */
  static final int COM_DEVICE_INPUT_BUFFER_SIZE =  0x20000;
  /**
   * ComPort transmit data buffer size (in bytes).
   */
  static final int COM_DEVICE_OUTPUT_BUFFER_SIZE = 0x10000;
  /**
   * Specifier for transmitting and receiving data packets that vary in size.
   */
  static final int VARIABLE_LENGTH_BLOCK_PROTOCOL             = 0;
  /**
   * Specifier for transmitting and receiving data packets that are always the
   * same number of bytes.
   */
  static final int FIXED_LENGTH_BLOCK_PROTOCOL                = 1;
  /**
   * Specifier for transmitting and receiving data packets that can vary in
   * size, but have an End-Of-Text/Transmission control code that marks the end of
   * the data packet.
   */
  static final int END_DELIMITED_BLOCK_PROTOCOL               = 2;
  /**
   * Specifier for transmitting and receiving data packets that can vary in
   * size, but have a Start-Of-Text/Transmission control code that marks the beginning of
   * the data packet.
   */
  static final int START_DELIMITED_BLOCK_PROTOCOL             = 3;
  /**
   * Specifier for transmitting and receiving data packets that can vary in
   * size, but have both a Start-Of-Text/Transmission control code that marks the beginning of
   * the data packet and an End-Of-Text/Transmission control code that marks the end of
   * the data packet.
   */
  static final int START_END_DELIMITED_BLOCK_PROTOCOL         = 4;
  /**
   * Specifier for transmitting and receiving data packets that are always the
   * same number of bytes and have a Start-Of-Text/Transmission control code that
   * marks the beginning of the data packet.
   */
  static final int START_DELIMITED_WITH_LENGTH_BLOCK_PROTOCOL = 5;
  /**
   * Specifier for transmitting and receiving data packets that are varying the
   * port specific Transmission control code.
   */
  static final int PORT_SPECIFIC_BLOCK_PROTOCOL               = 6;
}