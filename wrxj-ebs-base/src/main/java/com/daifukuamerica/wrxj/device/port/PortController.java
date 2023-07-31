package com.daifukuamerica.wrxj.device.port;

/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.comport.ComPort;
import com.daifukuamerica.wrxj.comport.ComPortEventHandling;
import com.daifukuamerica.wrxj.comport.net.SocketComPort;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.messageformat.controlevent.ControlEventDataFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A Controller that encapulates a communications port. This
 * device should handle anything related to establishing and monitoring the
 * physical implementation layer (RS-232, Socket, DI/DO, FTP, etc) and the
 * low-level protocol layer (STX/ETX, ACK/NAK, Block-Send/Receive, etc.).
 *
 * @author Stephen Kendorski
 * @version 1.0
 */
public class PortController extends Controller
                        implements ComPortEventHandling
{
  /**
   * The received data packet in {@link #inputProtocolByteBuffer inputProtocolByteBuffer}
   * of length {@link #receivedByteCount receivedByteCount}
   * represented as a String.
   */
  protected String receivedDataString  = null;

  /**
   * The listener interface for receiving received data events. The class that is
   * interested in processing received data implements this interface. The
   * object created with that class is then called as the event handler when
   * data is available.
   */
  protected interface DataAvailableEvent
  {
    void dataAvailableEvent(int count);
  }

  /**
   * The listener interface for receiving data pattern match events. The class that is
   * interested in processing data pattern matches implements this interface. The
   * object created with that class is then called as the event handler when
   * available data matches a specified pattern.
   */
  protected interface DataMatchEvent
  {
    void dataMatchEvent();
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private class DataMatches
  {
    List<DataMatch> dataMatchList = new ArrayList<DataMatch>();

    /*------------------------------------------------------------------------*/
    private class DataMatch
    {
      DataMatchEvent dataMatchEventHandler = null;
      byte   dataMatchByte = 0;
      int dataMatchLength = 0;
      boolean vbDiscardLeadingIfSame = false;

      /*----------------------------------------------------------------------*/
      protected void dataMatchEvent()
      {
        dataMatchEventHandler.dataMatchEvent();
      }

      void setDataMatchEvent(DataMatchEvent dmeDataMatchEvent)
      {
        dataMatchEventHandler = dmeDataMatchEvent;
      }
    }

    DataMatch dataMatch = null;

    /*------------------------------------------------------------------------*/
    // If we have a Data Match, process the DataAvailableEvent for the data
    // available up to the Data Match, discard the received data we actually
    // matched, and then generate an Event for the Data Match itself.
    //
    // inputByteBuffer   - the received data.
    // inputBufferSink   - the offset to where the {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort} will put data.
    // inputBufferSource - the offset to the start of the Unprocessed data that
    //                     this Port needs to process.
    /*------------------------------------------------------------------------*/
    void seeIfDataMatch()
    {
      while (true)
      {
        //  We're looking for a Data Match
        int iLen = findDataMatchCS1(); //...findDataMatch();
        //
        // If we find a Data Match in the input buffer we have the count of bytes
        // before (and including) the Data Match pattern. If not found, we have -1.
        //
        if (iLen < 0)
        {
          break; // No Match here - done
        }
        //
        // We have a Data Match.
        //
        if (iLen > 0)
        {
          //
          // Copy the data up to (but NOT including) the Data Match Pattern into
          // the Protocol's input buffer.
          //
          System.arraycopy(inputByteBuffer, inputBufferSource,
                           inputProtocolByteBuffer, inputProtocolSink, iLen);
        }
        // Put "rxDataCount" past Available Data and Matched Data.
        //
        // Update "rxDataCount" to show we have given the Protocol the data
        // before (and including) the Data Match.
        //
        // Also, update "inputProtocolSource" to show the same.
        //
        rxDataCount -= iLen;
        if (rxDataCount == 0)
        {
          inputBufferSink = 0;   // OK to reset these
          inputBufferSource = 0;
        }
        else
        {
          inputBufferSource += iLen;
        }
        //
        // Correct iLen to a count of up to the matched data.
        //
        iLen -= dataMatch.dataMatchLength;
        //
        // Give "iLen" Data Available to Port
        //
        // NOTE: We will only generate a DataAvailable Event if it is looking for
        // ZERO data bytes.  If we're waiting for a fixed-number of bytes OR a
        // data match, and we get the data match we assume the Data Available
        // Event was NOT for processing the unknown number of bytes up to the
        // Data Match but was an independent event which did not occur, so it
        // does NOT produce an event.
        //
        if  (dataWantedCount == 0)
        {
          dataAvailableEvent(iLen);
        }
        else
        {
          dataWantedCount = 0; // Reset this to prevent trouble in "ProcessAvailableData"
        }
        // Process Port Protocol's Data Match Event.
        //
        dataMatch.dataMatchEvent();
        break;
      } // while
    }

    /*----------------------------------------------------------------------*/
    // Find a Data Match - Case Sensitive - 1 byte long - can be done very
    // quickly, so we make it a separate routine.
    //
    // If we find a Data Match in the input buffer return the count of bytes
    // before (and including) the Data Match pattern. If not found, return -1.
    /*----------------------------------------------------------------------*/
    private int findDataMatchCS1()
    {
      int iResult = -1;
      int iInputToSearchIndex = inputToSearchIndex; // Start here in the input buffer.
      boolean bFoundMatch = false;
      while (iInputToSearchIndex < inputBufferSink)
      {
        //
        // If we have more than one Data Match to check, we need to perform
        // them all on every byte, so that we make sure we find the first match
        // in the data.
        //
        Iterator<DataMatch> iterator = dataMatchList.iterator();
        while (iterator.hasNext())
        {
          dataMatch = iterator.next();
          if (inputByteBuffer[iInputToSearchIndex] == dataMatch.dataMatchByte)
          {
             bFoundMatch = true;
             if (dataMatch.vbDiscardLeadingIfSame)
             {
               iInputToSearchIndex++;
               while (iInputToSearchIndex < inputBufferSink)
               {
                 if (inputByteBuffer[iInputToSearchIndex] != dataMatch.dataMatchByte)
                 {
                   break;
                 }
                 iInputToSearchIndex++;
               }
               --iInputToSearchIndex;
             }
             break;    // We had a match - we're done
          }
        }
        if (bFoundMatch)
        {
          break;
        }
        //
        // Bytes did NOT match - try again.
        //
        iInputToSearchIndex++;
      }
      if (bFoundMatch)
      {
        //
        // We DID find a match - Return count of bytes before (and including)
        // the Data Match pattern.
        //
        iResult = iInputToSearchIndex - inputBufferSource + 1;
        if ((dataWantedCount > 0) && (iResult > dataWantedCount))
        {
          //
          // We have a match OUTSIDE of the data that the user wants by length.
          // The length has the higher priority, so show NO match for now,
          // and let the user get the length of data he wants.
          //
          iResult = -1;             // Negative count says No data match.
         }
        }
      else
      {
        //
        // NO match before end of data - Save index to start at next time.
        // We can start from the inputBufferSink since we've been through
        // all the data in the input buffer without finding a data match.
        //
        inputToSearchIndex = inputBufferSink;
        iResult = -1;             // negative count says no data match
      }
      return iResult;
    }

    /**
     * @param dmeDataMatchEvent
     * @param baDataMatchBytes
     * @param iDataMatchLength
     */
    private void add(DataMatchEvent dmeDataMatchEvent, byte[] baDataMatchBytes, int iDataMatchLength)
    {
      add(dmeDataMatchEvent, baDataMatchBytes, iDataMatchLength, false);
    }

    /*------------------------------------------------------------------------*/
    void add(DataMatchEvent dmeDataMatchEvent,
                                   byte[] baDataMatchBytes,
                                   int iDataMatchLength,
                                   boolean ibDiscardAdjacentIfSame)
    {
      DataMatch vpDataMatch = new DataMatch();
      //
      vpDataMatch.setDataMatchEvent(dmeDataMatchEvent);
      vpDataMatch.dataMatchByte = baDataMatchBytes[0]; // For one byte pattern match.
      vpDataMatch.dataMatchLength = iDataMatchLength;
      vpDataMatch.vbDiscardLeadingIfSame = ibDiscardAdjacentIfSame;
      //
      // All DataMatch parameters set - add DataMatch To DataMatchList.
      //
      dataMatchList.add(vpDataMatch);
    }

    void clear()
    {
      dataMatchList.clear();
      dataMatch = null;
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * A byte array buffer which holds the received data that meets the protocol's
   * acceptance criteria.  Received data from the {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort}
   * which must be verified before being put in this buffer is held in {@link #inputByteBuffer inputByteBuffer}.
   */
  protected byte[] inputProtocolByteBuffer = new byte[PortConsts.COM_DEVICE_INPUT_BUFFER_SIZE];
  /**
   * Offset to where the protocol's next verified data is written into
   * {@link #inputProtocolByteBuffer inputProtocolByteBuffer}.
   */
  // inputProtocolSink -
  //
  protected int inputProtocolSink = 0;
  /**
   * A byte array buffer which holds received data from the {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort}
   * which must be verified before being put into the protocol's accepted data buffer,
   * {@link #inputProtocolByteBuffer inputProtocolByteBuffer}.
   */
  protected byte[] inputByteBuffer = new byte[PortConsts.COM_DEVICE_INPUT_BUFFER_SIZE];
  /**
   * A byte array buffer where the protocol assembles its data packets to give
   * to the {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort} for transmission.
   */
  protected byte[] outputByteBuffer = new byte[PortConsts.COM_DEVICE_OUTPUT_BUFFER_SIZE];
  /**
   * Semaphore to show that the protocol is in a data packet receive state.
   */
  protected boolean receiveCycleActive = false;
  /**
   * Semaphore to show that the protocol is in a data packet transmit state.
   */
  protected boolean transmitCycleActive = false;
  /**
   * The number of verified received data bytes in
   * {@link #inputProtocolByteBuffer inputProtocolByteBuffer}.
   */
  protected int receivedByteCount = 0;
  /**
   * Offset to where to put the next data bytes received from the {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort} into the byte array
   * received data buffer, {@link #inputByteBuffer inputByteBuffer}
   */
  protected int inputBufferSink = 0;
  /**
   * Offset to where to get the next data bytes received from the {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort} into the byte array
   * received data buffer, {@link #inputByteBuffer inputByteBuffer}
   */
  protected int inputBufferSource = 0;
  int inputToSearchIndex = 0;

  protected int comPortStatus = ControllerConsts.STATUS_UNKNOWN;
  protected boolean bufferDataWhenNotRunning = false;
  protected LinkedList<String> bufferedMessageQueue = new LinkedList<String>();
  protected int rxDataCount = 0;
  protected boolean getBlockNeedsData = false;
  private boolean processAgain = false;
  private boolean newDataAvailable = false;
  private DataMatches dataMatches = new DataMatches();

  protected DataAvailableEvent dataAvailableEventHandler = null;
  //
  // If this Port has two ComPorts assume the first is the receive port
  // and the second is the transmit port.
  //
  protected ComPort   comPort  = null;
  private ComPort   comPort2 = null;

  protected int dataWantedCount = 0;
  protected boolean discardingData = false;
  
  /**
   *  Class constructor.  Create a bare minimum of a Port.  Whoever
   *  created this Controller will then call <tt>startup</tt> which should then
   *  do anything useful to get this devices communication port(s) running.
   */
  protected PortController()
  {
  }

  /**
   * The Base Controller initialized the controller by setting the controller's
   * KeyName, attaching a logger, starting up the topic subscribers and
   * publishers for the message service, and starting the Thread where messages
   * will be processed.
   *
   * A child Controller should find its collaborators (but NOT use them),
   * subscribe to any events it needs from its collaborators, and perform any
   * other simple tasks which do NOT require logging.
   *
   * @param sControllerKeyName the unique name that identifies this instance of Controller
   */
  @Override
  public void initialize(String sControllerKeyName)
  {
    super.initialize(sControllerKeyName);
    logger.logDebug(getClass().getSimpleName() + ".initialize() - Start");

    setEquipmentPortCKN(sControllerKeyName); // To get messages sent to this Port.
    subscribeCommEvent();
    subscribeEquipmentEvent();
    subscribeControlEvent();
    logger.logDebug(getClass().getSimpleName() + ".initialize() - End");
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Activates the communications device by creating this ComDevice's actual
   * {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort} (communication physical layer).
   */
  /*--------------------------------------------------------------------------*/
  @Override
  public void startup()
  {
    super.startup();
    logger.logDebug(getClass().getSimpleName() + ".startup() - Start");
    startupComPort();
    
    /*
     * Starting up the comm port provides access to comm log. Show that we
     * started.
     */
    String vsStartUpMessage = "********** " + getName()
        + ": starting controller. **********";
    logger.logTxByteCommunication(vsStartUpMessage.getBytes(), 0,
        vsStartUpMessage.length());

    logger.logDebug(getClass().getSimpleName() + ".startup() - End");
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Deactivates this controller by cancelling any timers and shutting down the
   * communication ports.
   */
  /*--------------------------------------------------------------------------*/
  @Override
  public void shutdown()
  {
    logger.logDebug(getClass().getSimpleName() + ".shutdown() -- Start");
    setControllerStatus(ControllerConsts.STATUS_SHUTTING_DOWN);
    shutdownProtocol();
    shutdownComPort();
    bufferedMessageQueue = null;
    dataMatches = null;
    dataAvailableEventHandler = null;
    logger.logDebug(getClass().getSimpleName() + ".shutdown() -- End");
    super.shutdown();
  }

  /*--------------------------------------------------------------------------*/
  /**
   * The Port's {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort} uses this
   * procedure to notify this Port Controller
   * that there is received data available.  The Port Controller should use
   * {@link #getBlock(int) getBlock()} to actually get the received data.
   * The received data (bytes)
   * are put into {@link #inputByteBuffer inputByteBuffer} and
   * inputBufferSink is then updated.
   *
   * <p>This method is executing in the {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort}'s Thread! So synchronize.
   *
   * @param byteArray a buffer holding the received data
   * @param iOffset   the offset to the start of the received data in the buffer
   * @param iCount    the number of bytes of received data in the buffer
   */
  /*--------------------------------------------------------------------------*/
  public void comPortDataAvailableHandler(byte[] byteArray, int iOffset, int iCount)
  {
    synchronized(this)
    {
      int iBufferEmptyCount = PortConsts.COM_DEVICE_INPUT_BUFFER_SIZE - inputBufferSink;
      if (iBufferEmptyCount > 0)
      {
        //
        // Don't use "iCount", just get all of the available data that will fit
        // in our receive buffer.
        //
        iCount = comPort.getBlock(inputByteBuffer, inputBufferSink, iBufferEmptyCount);
        //logger.logDebug("comPortDataAvailableHandler -- getBlock() returned Bytes in Buffer: " + iCount);
        inputBufferSink += iCount;
        rxDataCount += iCount;
        //
        // We have the data in "inputByteBuffer" and "inputBufferSink" has been
        // updated.  We should NOT process the new data here since we are still
        // in the ComPort's Thread.  So, notify the Port Device's Thread.
        //
        newDataAvailable = true;
        notify();
      }
      else
      {
        logger.logError(getClass().getSimpleName() 
            + ".comPortDataAvailableHandler -- Rx Buffer FULL"); // Need to do something...?
      }
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
   * The Port's {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort} uses this method to notify this Port Controller
   * that the {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort} has changed status/state.  This method also changes the
   * {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort}'s status/state into a GENERIC CONTROLLER STATUS.
   *
   * <p>This is executing in the {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort}'s Thread! So synchronize.
   *
   * @param iState         the new state of the {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort}
   * @param iPrevState     the previous state of the {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort}
   * @param iComPortNumber the number of the {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort} reporting a state change
   */
  public void comPortStateChangeHandler(int iState, int iPrevState, int iComPortNumber)
  {
    synchronized(this)
    {
//      logger.logDebug("ComPortState was: " + ComPort.STATE_NAMES[iPrevState] +
//                      " -- now: " + ComPort.STATE_NAMES[iState]);
      switch (iState)
      {
        case ComPort.COM_PORT_STATE_RUNNING:
          if (iPrevState != ComPort.COM_PORT_STATE_RUNNING)
          {
            if ((iComPortNumber == 2) || (comPort2 == null))
            {
              /*
               * State is now Running - we are ready to use the ComPort.
               * We shouldn't do anything here since we are still in the ComPort's
               * Thread.
               */
              publishCommEvent("StatusChange", ControllerConsts.STATUS_RUNNING);
            }
          }
          break;
        case ComPort.COM_PORT_STATE_STOPPING:
          publishCommEvent("StatusChange", ControllerConsts.STATUS_STOPPING);
          break;
        case ComPort.COM_PORT_STATE_STOPPED:
          publishCommEvent("StatusChange", ControllerConsts.STATUS_STOPPED);
          break;
        case ComPort.COM_PORT_STATE_ERROR:
          if (iPrevState != ComPort.COM_PORT_STATE_ERROR)
          {
            //
            // ComPort just went into ERROR state.
            //
            publishCommEvent("StatusChange", ControllerConsts.STATUS_ERROR);
          }
          break;
        default: break;
      }
    }
  }

  /*--------------------------------------------------------------------------*/
  // Process System Inter-Process-Communication Message.
  /*--------------------------------------------------------------------------*/
  @Override
  protected void processIPCReceivedMessage()
  {
    //
    // (Decide how to) Process message here
    //
    // receivedText = receivedMessage.getMessageText();
    // receivedData = receivedMessage.getMessageData();
    // receivedEventType = receivedMessage.getEventType();
    // receivedEvent = receivedMessage.getEvent();
    //
    super.processIPCReceivedMessage();
    if (!receivedMessageProcessed)
    {
      receivedMessageProcessed = true;
      switch (receivedEventType)
      {
        case MessageEventConsts.COMM_EVENT_TYPE:  processCommunicationEvent(); break;
        case MessageEventConsts.EQUIPMENT_EVENT_TYPE:  processEquipmentEvent(); break;
        default: receivedMessageProcessed = false;
      }
    }
  }

  /*--------------------------------------------------------------------------*/
 // @Override
  protected void processLocalEvent()
  {
    super.processLocalEvent();
    if (newDataAvailable)
    {
      newDataAvailable = false;
      //
      // Data is available from the ComPort.
      //
      do
      {
        processAgain = false; // Enabling events will set this to true.
        processAvailableData();
      }
      while (processAgain);
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
      char chr0 = receivedText.charAt(0);
      switch (chr0)
      {
        case ControlEventDataFormat.CHAR_START_PORT:
          logger.logDebug("==========================     startupComPort() - processControlEvent()");
          setControllerStatus(ControllerConsts.STATUS_STARTING);
          startupComPort();
          break;
        case ControlEventDataFormat.CHAR_STOP_PORT:
          logger.logDebug("==========================     shutdownComPort() - processControlEvent()");
          setControllerStatus(ControllerConsts.STATUS_STOPPING);
          shutdownProtocol();
          shutdownComPort();
          setControllerStatus(ControllerConsts.STATUS_STOPPED);
          break;
        case ControlEventDataFormat.CHAR_PORT_TEST:
          logger.logDebug("==========================     testPort() - processControlEvent()");
          testPort();
          break;
        default:
          logger.logError(getClass().getSimpleName()
              + ".processControlEvent() -- UNKNOWN Event Type \"" + chr0
              + "\" -- processControlEvent()");
      }
    }
    catch(StringIndexOutOfBoundsException e)
    {
      logger.logSparseException(e, getClass().getSimpleName()
          + ".processControlEvent() - \"" + receivedText + "\"");
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Give a packet of data bytes to the {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort} to transmit.
   *
   * @param byteArray the array of data bytes to transmit.
   * @param iCount the number of data bytes to transmit
   */
  protected void putBlock(byte[] byteArray, int iCount)
  {
    putBlock(byteArray, 0, iCount);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Give a packet of data bytes to the {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort} to transmit.
   *
   * @param byteArray the array of data bytes to transmit.
   * @param iOffset  the index into the byte array of the first byte to transmit
   * @param iCount the number of data bytes to transmit
   */
  protected void putBlock(byte[] byteArray, int iOffset, int iCount)
  {
    //
    // If this Port has two ComPorts assume the first is the receive port
    // and the second is the transmit port.
    //
    if (comPort2 == null)
    {
      comPort.putBlock(byteArray, iOffset, iCount);
    }
    else
    {
      comPort2.putBlock(byteArray, iOffset, iCount);
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
   * getBlock -- Get a block of received bytes (INDIRECTLY) from the {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort}.
   * Do NOT get more data than actually available, and do NOT get more data
   * than the caller requested.  This method uses "processAvailableData()"
   * to actually put the "gotten" data into "inputProtocolByteBuffer".
   *
   * iCount: Number of bytes to receive (-1 says get all bytes in Input Buffer)
   * Returns -- The number of bytes actally given to the caller.
   */
  /*--------------------------------------------------------------------------*/
  protected int getBlock(int iCount)
  {
    if (iCount == 0)
    {
      return 0;
    }
    getBlockNeedsData = true;
    if (iCount < 0)
    {
      dataWantedCount = rxDataCount;
    }
    else
    {
      dataWantedCount = iCount;
    }
    int iGetBlockCount = dataWantedCount;
    processAvailableData();            // Data will be gotten here
    return iGetBlockCount;
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Discard any unprocessed received data.
   */
  protected void flushInputBuffer()
  {
    //
    // If this Port has two ComPorts assume the first is the receive port
    // and the second is the transmit port.
    //
    inputBufferSink = 0;
    inputBufferSource = 0;
    rxDataCount = 0;
    if (comPort != null)
    {
      comPort.flushInputBuffer();
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Discard any data that has not yet been transmitted.
   */
  protected void flushOutputBuffer()
  {
    //
    // If this Port has two ComPorts assume the first is the receive port
    // and the second is the transmit port.
    //
    if (comPort != null)
    {
      comPort.flushOutputBuffer();
    }
    else
    {
      comPort.flushOutputBuffer();
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Setup an Event Handler for receiving data.
   *
   * <p>NOTE: When the event is generated, dataWantedCount
   * is set to zero and no more "dataAvailableEvent" events will be generated
   * until another "setDataAvailableEvent" call (incoming data will still be
   * received and buffered, however).
   *
   * <p>NOTE: Set the param,eter "iDataWantedCount" to ZERO to receive the data
   * returned when a "Data Match" event occurs.  The "dataAvailableEvent"
   * Event Handler triggered by a Data Match receives the length of the data
   * received and buffered before the matched data. The Matched Data is discarded
   * and an "dataMatchEvent" Event is generated for the user.
   *
   * @param dataAvailableEvent User's Event Handler (called when "iDataWantedCount"
   * bytes are received and buffered).
   * @param iDataWantedCount number of bytes to receive before the "dataAvailableEvent"
   * Event is generated.
   */
  public void setDataAvailableEvent(DataAvailableEvent dataAvailableEvent,
                                       int iDataWantedCount)
  {
    dataAvailableEventHandler = dataAvailableEvent;
    dataWantedCount = iDataWantedCount;
  }

  /*--------------------------------------------------------------------------*/
  /**
   * The processor for received data events.  The Data Available Event Handler
   * is specified by the protocol state-machine.
   */
  public void dataAvailableEvent(int count)
  {
    try
    {
    	if(dataAvailableEventHandler != null) {
    		dataAvailableEventHandler.dataAvailableEvent(count);
    	}
    	else {
    		logger.logError("dataAvailableEventHandler == null - count: " + count);
    	}
    }
    catch (Exception e)
    {
      if (dataAvailableEventHandler == null)
      {
        logger.logException(e, "dataAvailableEventHandler == null - count: " + count);
      }
      else
      {
        logger.logException(e, "dataAvailableEventHandler != null - count: " + count);
      }
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Add an event handler to look for a speciic pattern in the received data.
   *
   * <p>NOTE: Any number of Data Match Events may be active simultaneously.
   *
   * <p>NOTE: When the Data Match occurs a {@link #dataAvailableEvent dataAvailableEvent}
   * is first generated
   * to give the user the data received up to the matched data.  The matched
   * data is discarded, and a "dataMatchEvent" is then generated to announce to
   * the user that the match occured.  The protocol should then clear all
   * Data Match Events and re-add Data Match Events as needed.
   *
   * @param dataMatchEvent user's Event Handler (called when the Data Match occurs)
   * @param baDataMatchBytes byte array containing the data pattern to match.
   * @param idataMatchLength number of bytes in the Data Match Pattern
   */
  protected void addDataMatchEvent(DataMatchEvent dataMatchEvent,
                                   byte[] baDataMatchBytes,
                                   int idataMatchLength)
  {
    dataMatches.add(dataMatchEvent, baDataMatchBytes, idataMatchLength);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Add an event handler to look for a speciic pattern in the received data.
   *
   * <p>NOTE: Any number of Data Match Events may be active simultaneously.
   *
   * <p>NOTE: When the Data Match occurs a {@link #dataAvailableEvent dataAvailableEvent}
   * is first generated
   * to give the user the data received up to the matched data.  The matched
   * data is discarded, and a "dataMatchEvent" is then generated to announce to
   * the user that the match occured.  The protocol should then clear all
   * Data Match Events and re-add Data Match Events as needed.
   *
   * @param dataMatchEvent user's Event Handler (called when the Data Match occurs)
   * @param baDataMatchBytes byte array containing the data pattern to match.
   * @param idataMatchLength number of bytes in the Data Match Pattern
   * @param ibDiscardAdjacentIfSame discard adjacent bytes if same as match
   */
  protected void addDataMatchEvent(DataMatchEvent dataMatchEvent,
                                   byte[] baDataMatchBytes,
                                   int idataMatchLength,
                                   boolean ibDiscardAdjacentIfSame)
  {
    dataMatches.add(dataMatchEvent, baDataMatchBytes, idataMatchLength, ibDiscardAdjacentIfSame);
  }
  /*--------------------------------------------------------------------------*/
  /**
   * Detach any event handlers looking for specific patterns in the received data.
   *
   */
  protected void removeAllDataMatchEvents()
  {
    dataMatches.clear();
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Activate all event listeners that process data match and data available
   * events.
   */
  protected void enableEventProcessing()
  {
    inputToSearchIndex = inputBufferSource;       // Start here.
    if (inputBufferSource != inputBufferSink)
    {
      processAgain = true;
    }
  }

  /*--------------------------------------------------------------------------
  --------------------------------------------------------------------------*/
  /**
   * We have received a message from a DEVICE (Controller) that wants to transmit
   * data to the EQUIPMENT that is attached to this PORT.  The data (String) to
   * be transmitted is in
   * {@link com.daifukuamerica.wrxj.common.controller.Controller#receivedText receivedText}.
   */
  protected void processEquipmentEvent()
  {
    //logger.logDebug(getClass().getSimpleName() + ".processEquipmentEvent() - Start");
    if (comPortStatus == ControllerConsts.STATUS_RUNNING)
    {
      discardingData = false;
      //
      // The ComPort IS running - Transmit the message.
      //
      if (bufferDataWhenNotRunning)
      {
        if (sendBufferedMessages())
        {
          //
          // Returns TRUE if there are still messages in the bufferedMessageQueue.
          //
          // There ARE messages that are waiting to be transmitted before
          // this one.  Add the new message to the buffered message queue.
          //
          bufferedMessageQueue.add(receivedText);
        }
        else
        {
          //
          // There are NO buffered messages that are waiting to be transmitted
          // before this one.  Transmit the new message.
          //
          transmitEquipmentData(receivedText);
        }
      }
      else
      {
        //
        // Buffering NOT selected - just transmit the new message.
        //
        transmitEquipmentData(receivedText);
      }
    }
    else
    {
      //
      // The ComPort is NOT Running - See if we should buffer the new message.
      //
      if (bufferDataWhenNotRunning)
      {
        //
        // The ComPort is NOT Running AND we should buffer the new message.
        // Only log this when we begin buffering.
        //
        if (bufferedMessageQueue.isEmpty())
        {
          logger.logError("Unable to Transmit - Buffering Messages");
        }
        bufferedMessageQueue.add(receivedText);
      }
      else
      {
        if (!discardingData)
        {
          logger.logError("Unable to Transmit - Discarding Messages");
          discardingData = true;
        }
      }
    }
    //logger.logDebug("processEquipmentEvent() - End");
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  // Protocol-Specific methods..
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Activate the communication link data receiving and transmitting.  Add any
   * required data match and data available event listeners.  Start any
   * required timers.
   */
  protected void startupProtocol()
  {
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * De-activate the communication link data receiving and transmitting.  Remove
   * all data match and data available event listeners.  Cancel any timers.
   */
  protected void shutdownProtocol()
  {
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Converts the passed-in parameter "sEquipmentData" String to a byte-array.
   *
   * <p>The instantiated CHILD Protocol should override this "conditionDataToTransmit()"
   * to perform any protocol-specific type of data conditioning (checksum,
   * sequence number, etc.) before the data BYTE-ARRAY is transmitted to the
   * {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort} (and the actual equipment that is connected to the {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort}).
   */
  protected byte[] conditionDataToTransmit(String sEquipmentData)
  {
    return sEquipmentData.getBytes();
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Verify that the received data packet meets the protocol's acceptance criteria.
   * In the {@link PortController} base-class "verifyReceivedData()" converts the
   * received data in {@link #inputProtocolByteBuffer inputProtocolByteBuffer}
   * of length {@link #receivedByteCount receivedByteCount} to a String.
   *
   * <p> The instantiated CHILD Protocol should override "verifyReceivedData()"
   * to perform any protocol-specific type of data validation (checksum,
   * sequence number, etc.) before the received data STRING is published to the
   * Inter-Process-Message bus.
   *
   * <p>The String "receivedDataString" is updated by this method.
   *
   * @return true, if data is acceptable
   */
  protected boolean verifyReceivedData()
  {
    if (receivedByteCount > 0)
    {
      receivedDataString = new String(inputProtocolByteBuffer, 0, receivedByteCount);
      return true;
    }
    else
    {
      return false;
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Give the data to be transmitted to the {@link com.daifukuamerica.wrxj.common.comport.ComPort ComPort}.  Add Start & End Delimiters
   * (if needed) and convert the Data String to a byte array.
   *
   * @param equipmentData the String to transmit
   */
  protected void transmitEquipmentData(String equipmentData)
  {
    //logger.logDebug("\"" + equipmentData + "\" - transmitEquipmentData()");
    //
    // In the "PortController" base-class "conditionDataToTransmit" converts the
    // "equipmentData" String to a byte-array.
    //
    // The CHILD Protocol should override "conditionDataToTransmit" to perform
    // any type of data conditioning (checksum, sequence number, etc.) before
    // the data BYTE-ARRAY is transmitted to the ComPort (and the actual equipment
    // that is connected to the ComPort).
    //
    byte[] equipmentDataByteArray = conditionDataToTransmit(equipmentData);
    int byteArrayLen = equipmentDataByteArray.length;

    putBlock(equipmentDataByteArray, byteArrayLen);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Verify that all of the received byte data in {@link #inputProtocolByteBuffer inputProtocolByteBuffer}
   * is displayable ASCII (0x20 - 0x7e).
   *
   * @return true, if all data are displayable
   */
  protected boolean receivedDataAllAsciiDisplayable()
  {
    boolean bResult = true;
    for (int i = 0; (i < receivedByteCount); i++)
    {
      if ((inputProtocolByteBuffer[i] < 0x20) ||
          (inputProtocolByteBuffer[i] > 0x7e))
      {
        logger.logDebug("receivedDataAllAsciiDisplayable() -- Byte " +
                        i + ": " + inputProtocolByteBuffer[i] +
                        " NOT OK <<<-----------------<<<");
        bResult = false;
        break;
      }
    }
    return bResult;
  }

  /*--------------------------------------------------------------------------*/
  // Private methods..
  /*--------------------------------------------------------------------------*/

  /*--------------------------------------------------------------------------*/
  private boolean dataMatchEventsExist()
  {
    return (!dataMatches.dataMatchList.isEmpty());
  }

  protected void startupComPort()
  {
    logger.logDebug(getClass().getSimpleName() + " -- comPort.startup()");
    // Startup this ComDevice's actual ComPort (communication physical layer).
    logger.logDebug(getClass().getSimpleName()
        + " -- startup() -- inputByteBufferSize: "
        + PortConsts.COM_DEVICE_INPUT_BUFFER_SIZE
        + " -- outputByteBufferSize: "
        + PortConsts.COM_DEVICE_OUTPUT_BUFFER_SIZE);

    comPort =  new SocketComPort();
    if (comPort != null)
    {
      comPort.setProperties(mpProperties);
      comPort.initialize(logger);
      comPort.setComPortEventHandler(this);
      comPort.startup();
    }
    else
    {
      logger.logError("ComPort NOT Started - startupComPort()");
    }
  }

  /*--------------------------------------------------------------------------*/
  protected void shutdownComPort()
  {
    logger.logDebug(getClass().getSimpleName() + ".shutdownComPort() -- Start");
    if (comPort != null)
    {
      comPort.shutdown();
      comPort = null;
    }
    if (comPort2 != null)
    {
      comPort2.shutdown();
      comPort2 = null;
    }
    logger.logDebug(getClass().getSimpleName() + ".shutdownComPort() -- End");
  }

  /*--------------------------------------------------------------------------
  * We come here when our ComPort notifies this Port that it has data available
  * for processing or has changed status (Connecting, Running, etc.).
  --------------------------------------------------------------------------*/
  protected void processCommunicationEvent()
  {
    //logger.logDebug(">=====> processCommunicationEvent() -- Start -- \"" + receivedText + "\"");
    try
    {
      char chr0 = receivedText.charAt(0);
      switch (chr0)
      {
        case 'S':
          if (comPortStatus != receivedData)
          {
            //
            // Status of the ComPort has changed.
            //
            logger.logDebug("StatusChange (from ComPort) - WAS: " +
                           ControllerConsts.STATUS_TEXT[comPortStatus] + "   NOW: " +
                            ControllerConsts.STATUS_TEXT[receivedData]);
            comPortStatus = receivedData;
 //...?     setControllerStatus(receivedData);
            switch (receivedData)
            {
              case ControllerConsts.STATUS_RUNNING:
                setControllerStatus(ControllerConsts.STATUS_RUNNING);
                startupProtocol();
                break;
              case ControllerConsts.STATUS_ERROR:
                setControllerStatus(ControllerConsts.STATUS_ERROR);
                logger.logDebug("processCommunicationEvent - shutdownProtocol()");
                shutdownProtocol();
                shutdownComPort();
                logger.logDebug("processCommunicationEvent - startupComPort()");
                setControllerStatus(ControllerConsts.STATUS_STARTING);
                startupComPort();
                //
                // startupProtocol() will be called when ComPort Status goes to running.
                //
                break;
            }
          }
        break;
        default:
          logger.logError(getClass().getSimpleName()
                + ".processCommunicationEvent() -- UNKNOWN Event Type \""
                + chr0 + "\" -- processCommunicationEvent()");
      }
    }
    catch(StringIndexOutOfBoundsException e)
    {
      logger.logSparseException(e, getClass().getSimpleName()
          + ".processCommunicationEvent() - \"" + receivedText + "\"");
    }
  }

  /*--------------------------------------------------------------------------
  // Data is available from the ComPort.
  //
  // inputByteBuffer   - the received data.
  // inputBufferSink   - the offset to where the ComPort will put data.
  // inputBufferSource - the offset to the start of the Unprocessed data that
  //                     this Port needs to process.
  --------------------------------------------------------------------------*/
  public void processAvailableData()
  {
    //logger.logDebug("processAvailableData() -- (from ComPort)");
    if (dataMatchEventsExist())
    {
      //
      // If we have a Data Match, process the DataAvailableEvent for the data
      // available up to the Data Match, discard the received data we actually
      // matched, and then generate an Event for the Data Match itself.
      //
      dataMatches.seeIfDataMatch();
      if (dataWantedCount == 0)
      {
        return;         // Everything taken care of in SeeIfDataMatch - ok to exit.
      }
    }
    if (dataWantedCount == 0)
    {
      if (getBlockNeedsData)
      {
        getBlockNeedsData = false;
      }
      return;          // Nobody wants any data - ok to exit
    }
    if (rxDataCount >= dataWantedCount)
    {
      //
      // We have (at least) the amount of data the user wants.  Copy the
      // Data Wanted Count into the Protocol's input buffer.
      //
      System.arraycopy(inputByteBuffer, inputBufferSource,
                       inputProtocolByteBuffer, inputProtocolSink, dataWantedCount);
      //
      // Update "rxDataCount" to show we have given the Protocol "dataWantedCount".
      // Also, update "inputBufferSource" & "inputProtocolSink" to show the same.
      //
      rxDataCount -= dataWantedCount;
      if (rxDataCount == 0)
      {
        inputBufferSink = 0;   // OK to reset these
        inputBufferSource = 0;
      }
      else
      {
        inputBufferSource += dataWantedCount;
        inputProtocolSink += dataWantedCount;
      }
      //
      // Make a local copy of "DataWantedCount" in case the "PortOnDataAvailable"
      // event handler calls "SetDataAvailable" and updates it while the event
      // handler is using the reference parameter.
      //
      int tmpDataWantedCount = dataWantedCount;
      dataWantedCount = 0; // User will update this to get more data
      if (getBlockNeedsData)
      {
        getBlockNeedsData = false;
      }
      else
      {
        dataAvailableEvent(tmpDataWantedCount);
      }
    }
  }

  /*--------------------------------------------------------------------------
    If we bufferDataWhenNotRunning then this routine will see if we were not
    running, but are now running.  And if so, transmit any messages that were
    buffered while we were not running.

    Returns TRUE if there are still messages in the bufferedMessageQueue.
  /*--------------------------------------------------------------------------*/
  protected boolean sendBufferedMessages()
  {
    int iBufferedMessageCount = bufferedMessageQueue.size();
    if (iBufferedMessageCount > 0)
    {
      logger.logError("Transmitting " + iBufferedMessageCount + " Buffered Messages");
    }
    while ((!bufferedMessageQueue.isEmpty()) &&
          (comPortStatus == ControllerConsts.STATUS_RUNNING))
    {
      //
      // There are buffered messages and the ComPort is running, so transmit
      // one of the buffered messages.  Only transmit one message at a time so
      // that we can check the ComPort status before each message.
      //
      transmitEquipmentData(bufferedMessageQueue.getFirst());
      if (comPortStatus == ControllerConsts.STATUS_RUNNING)
      {
        //
        // We're still running - assume message was transmitted Ok and delete it.
        // If we're NOT still running assume the message did not go out and leave
        // it in the DataList for later re-transmission.
        //
        bufferedMessageQueue.removeFirst();
      }
    }
    if (iBufferedMessageCount > 0)
    {
      //
      // We did have buffered messages to transmit - log how many were
      // actually transmitted.
      //
      iBufferedMessageCount -= bufferedMessageQueue.size();
      logger.logError(iBufferedMessageCount + " Buffered Messages Transmitted");
    }
    return (!bufferedMessageQueue.isEmpty());
  }

  private void testPort()
  {
    receivedText =
    //                nn1     s       2     s       3     s       4     s       5     s               .
    "30001402061402062051100020000000021000710000000210008200000002100093000000021001000000000";
    processEquipmentEvent();
/*    byte[] bbfr = new byte[100];
    bbfr[0] = 0x03;
    bbfr[1] = 0x02;
    int len = s.length() + 2;
    for (int i = 2; i < len; i++)
    {
      char c = s.charAt(i-2);
      bbfr[i] = (byte)c;
    }
    bbfr[len++] = 0x03;
    bbfr[len++] = 0x03;
    putBlock(bbfr, len);
*/
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
    return new PortController();
  }

}

