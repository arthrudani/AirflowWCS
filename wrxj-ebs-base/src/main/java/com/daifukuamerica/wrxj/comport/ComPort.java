package com.daifukuamerica.wrxj.comport;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.log.Logger;

/**
 * A wrapper for a physical communications port. It handles anything related to
 * establishing, monitoring and receiving and transmitting data to the actual
 * physical implementation layer (RS-232, Socket, DI/DO, FTP, etc.).
 *
 * @author Stephen Kendorski
 * @version 1.0
 */
public interface ComPort
{
  /*
   * Constants
   */
  static final int COM_PORT_STATE_ERROR      = 0;
  static final int COM_PORT_STATE_UNKNOWN    = 1;
  static final int COM_PORT_STATE_CREATE     = 2;
  static final int COM_PORT_STATE_CREATING   = 3;
  static final int COM_PORT_STATE_CREATED    = 4;
  static final int COM_PORT_STATE_CONNECT    = 5;
  static final int COM_PORT_STATE_CONNECTING = 6;
  static final int COM_PORT_STATE_CONNECTED  = 7;
  static final int COM_PORT_STATE_RUNNING    = 8;
  static final int COM_PORT_STATE_STOP       = 9;
  static final int COM_PORT_STATE_STOPPING   = 10;
  static final int COM_PORT_STATE_STOPPED    = 11;

  static final String[] STATE_NAMES = {
      "** Error **",
      "Unknown",
      "Create",
      "Creating",
      "Created",
      "Connect",
      "Connecting",
      "Connected",
      "Running",
      "Stop",
      "Stopping",
      "Stopped"};

  static final int SOCKET_TYPE_CONNECT       = 0; // Client
  static final int SOCKET_TYPE_LISTEN        = 1; // Server
  static final int SOCKET_TYPE_SINGLE_LISTEN = 2;

  static final int COM_PORT_INPUT_BUFFER_SIZE =  0x20000;
  static final int COM_PORT_OUTPUT_BUFFER_SIZE = 0x10000;

  /**
   * Attach the system logger and a rx/tx communicatiions logger.
   *
   * @param lLogger the system Logging implementation to attach
   */
  void initialize(Logger lLogger);
  /**
   * Activate the physical communication layer.
   */
  void startup();
  /**
   * De-activate the physical communication layer.
   */
  void shutdown();
  /**
   * Specify the properties collection for the ComPort to use.
   *
   * @param properties the properties collection
   */
  void setProperties(ReadOnlyProperties properties);
  /**
   * The listener interface for receiving status change and received data events.
   * The class that is interested in processing ComPort status and data implements
   * this interface. The object created with that class is then called as the
   * event handler when status changes, or data is available.
   *
   * @param oHandler the handler
   */
  void setComPortEventHandler(ComPortEventHandling oHandler);

  /**
   * Discard any unprocessed received data.
   */
  void flushInputBuffer();
  /**
   * Discard any data that has not yet been transmitted.
   */
  void flushOutputBuffer();
  /**
   * Write a single byte to the output buffer.  When the byte is in the buffer
   * the comport control thread is notifyed that there is data to transmit.
   *
   * @param bByte the byte to transmit
   */
  void putByte(byte bByte);
  /**
   * Write a block of bytes to the output buffer.  When the data is in the buffer
   * the comport control thread is notifyed that there is data to transmit.
   *
   * @param byteArray the array of data bytes to transmit.
   * @param iCount the number of data bytes to transmit
   * @return the number of bytes written to the output buffer
   */
  int putBlock(byte[] byteArray, int iCount);
  /**
   * Write a block of bytes to the output buffer.  When the data is in the buffer
   * the comport control thread is notifyed that there is data to transmit.
   *
   * @param byteArray the array of data bytes to transmit.
   * @param iOffset  the index into the byte array of the first byte to transmit
   * @param iCount the number of data bytes to transmit
   * @return the number of bytes written to the output buffer
   */
  int putBlock(byte[] byteArray, int iOffset, int iCount);
  /**
   * Read a single received data byte from the input buffer.
   *
   * @return a single received data byte
   */
  byte getByte();
  /**
   * Read a block of received data bytes from the input buffer.  Do NOT get
   * more data than actually available, and do NOT get more data than the
   * caller requested.
   *
   * @param byteArray the caller's supplied array for the received data
   * @param iOffset  the index into the byte array of where to put the first byte
   * @param iCount the number of requested data bytes
   * @return the number of bytes actually read
   */
  int getBlock(byte[] byteArray, int iOffset, int iCount);
}