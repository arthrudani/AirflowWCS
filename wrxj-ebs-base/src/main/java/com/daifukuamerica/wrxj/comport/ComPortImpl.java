package com.daifukuamerica.wrxj.comport;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.NamedThread;
import com.daifukuamerica.wrxj.log.Logger;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Timer;

/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      SKDC Corp.
 * @author Stephen Kendorski
 * @version 1.0
 */
public class ComPortImpl implements ComPort, Runnable
{
  // A collection of configuration name/value data properties.
  protected ReadOnlyProperties mpComPortConfig = null;

  // The Logging implementation for this named subsystem to use.
  protected Logger logger = null;
  
  // A timer to use for communication connection retries.
  protected Timer retryTimer = null;
  
  /*
   * Set true when this object is first created.  It is set to false so that
   * some create-time methods only occur once.
   */
  protected boolean justCreated = true;
  protected Object thisComPort = this;
  
  /*
   * The maximum size of a data block to be transmitted at the lowest level.
   * When we transmit data, the Baud Rate is used to determine how much data we
   * should transmit as a block.  The Serial Port transmit will not return
   * until all data has been TRANSMITTED (NOT put in a buffer).  So, we
   * want to transmit large data blocks in small chynks (expecially at low
   * baud rates).
   */
  protected int    txBlockSize = Integer.MAX_VALUE;

  protected volatile int    comPortState = COM_PORT_STATE_UNKNOWN;
  protected int      previousComPortState = COM_PORT_STATE_UNKNOWN;
  private Thread   selfThread = null;
  protected boolean  quit = false;

  public BufferedInputStream   inputStream = null;
  BufferedOutputStream  outputStream = null;

  public byte[] inputByteBuffer = new byte[COM_PORT_INPUT_BUFFER_SIZE];
  public int inputBufferSink = 0;
  protected int inputBufferSource = 0;
  public Object inputLock = new Object();

  byte[] outputByteBuffer = new byte[COM_PORT_OUTPUT_BUFFER_SIZE];
  int outputBufferSink = 0;
  int outputBufferSource = 0;
  Object outputLock = new Object();

  private ComPortEventHandling     comPortEventHandler = null;
  private int                     comPortNumber = 1;
  protected ControlThread controlThread = null;
  protected Object creationLock = new Object();


  /*========================================================================*/
  /*  Control Thread                                                        */
  /*========================================================================*/

  /**
   *  Inner class for establishing a Control thread
   *  within another thread, to prevent blocking.
   */
  public class ControlThread extends NamedThread
  {
    private boolean               ctBusy = false;
    private boolean               ctQuit = false;

    public ControlThread()
    {
      logger.logDebug("ControlThread() -- Created");
    }

    /**
     * run() -- Our State-Machine.
     */
    @Override
    public void run()
    {
      while (!ctQuit)
      {
        //
        // If we're here, we're busy doing something which may cause BLOCKING I/O!
        // If, so, set a flag so that some sort of shutdown can occur without
        // this thread's participation.
        //
        ctBusy = true;
        switch (comPortState)
        {
          case COM_PORT_STATE_CREATING:
            synchronized(creationLock)
            {
              try
              {
                createComPort();
                notifyControl();
              }
              catch (Exception e)
              {
                if (logger != null)
                {
                  logger.logException(e);
                }
              }
            }
            if (comPortState != COM_PORT_STATE_CREATING)
            {
              continue;
            }
            else
            {
              break;
            }
            //not reachable break;
            
          case COM_PORT_STATE_CONNECTING:
            synchronized(creationLock)
            {
              acceptConnection();
              notifyControl();
            }
            break;
          
          case COM_PORT_STATE_RUNNING:
            int vnPreviousComPortState = comPortState;
            //logger.logDebug("ControlThread.run() -- COM_PORT_STATE_RUNNING");
            ctInputAvailableData();
            //
            // "ctInputAvailableData" may have had an error while reading data
            // so make sure we're still running.
            //
            if (comPortState == COM_PORT_STATE_RUNNING)
            {
              ctOutputAvailableData();
            }
            synchronized (inputLock)
            {
              if ((inputBufferSink != inputBufferSource) ||
                  ((comPortState == COM_PORT_STATE_ERROR) &&
                   (comPortState != vnPreviousComPortState)))
              {
                //logger.logDebug("run() - (inputBufferSink != inputBufferSource)");
                notifyControl();
              }
            }
            break;
          case COM_PORT_STATE_STOPPED:
            break;
            
          default:
            //logError("UNKNOWN socketState: " + socketState); 
            break;
        }
        ctBusy = false;
        int timeout = 2000;
        if ((comPortState == COM_PORT_STATE_RUNNING) ||
            (comPortState == COM_PORT_STATE_CONNECT))
        {
         timeout = 10;
        }
        waitHere(timeout);
      }
    }

    /**
     * ControlThread - waitHere
     * @param timeout
     */
    private synchronized void waitHere(int timeout)
    {
      try
      {
        wait(timeout);
      }
      catch (InterruptedException e)
      {
        if (logger != null)
        {
          logger.logDebug("ControlThread.run() -- InterruptedException: " + e.toString());
        }
      }
    }

    /**
     * ControlThread - ctInputAvailableData
     */
    protected void ctInputAvailableData()
    {
      try
      {
        if (inputStream.available() != 0)
        {
          synchronized(inputLock)
          {
            int bufferFreeCount = COM_PORT_INPUT_BUFFER_SIZE - inputBufferSink;
            if (bufferFreeCount > 0)
            {
              int bytesRead = inputStream.read(inputByteBuffer, inputBufferSink, bufferFreeCount);
              //
              // We can be told that bytes are available, but when we read there is
              // no data there - so check if we actually read anything.
              //
              if (bytesRead != 0)
              {
                logger.logRxByteCommunication(inputByteBuffer, inputBufferSink, bytesRead);
                inputBufferSink += bytesRead;
                if (inputBufferSink > COM_PORT_INPUT_BUFFER_SIZE)
                {
                  logError("ctInputAvailableData() -- Rx Buffer OVERFLOW -- bufferFreeCount:" + bufferFreeCount);
                  inputBufferSink = COM_PORT_INPUT_BUFFER_SIZE;
                }
              }
            }
            else
            {
              logger.logDebug("ctInputAvailableData() -- Rx Buffer FULL -- bufferFreeCount:" + bufferFreeCount);
            }
          } // synchronized
        }
      }
      catch (IOException ioe)
      {
        logger.logDebug("ctInputAvailableData() - inputStream.available() -- IOException: " + ioe.toString());
        //
        // Not much we can do here except kick it upstairs.
        //
        comPortState = COM_PORT_STATE_ERROR;
      }
    }

    /**
     * ControlThread - ctOutputAvailableData
     */
    private void ctOutputAvailableData()
    {
      synchronized(outputLock)
      {
        try
        {
          while (outputBufferSink != 0)
          {
            int bytesToTransmit = outputBufferSink - outputBufferSource;
            if (bytesToTransmit > txBlockSize)
            {
              bytesToTransmit = txBlockSize;
            }
            try
            {
              logger.logTxByteCommunication(outputByteBuffer, outputBufferSource, bytesToTransmit);
              outputStream.write(outputByteBuffer, outputBufferSource, bytesToTransmit);
              outputStream.flush();
              outputBufferSource += bytesToTransmit;
              if (outputBufferSource == outputBufferSink)
              {
                outputBufferSource = 0;
                outputBufferSink = 0;
              }
            }
            catch (IOException ioe)
            {
              logError("ctOutputAvailableData() - outputStream.flush() -- IOException: " + ioe.toString());
              //
              // Not much we can do here except kick it upstairs.
              //
              comPortState = COM_PORT_STATE_ERROR;
              break;
            }
            catch(ArrayIndexOutOfBoundsException e)
            {
              logError("ctOutputAvailableData() -- ArrayIndexOutOfBoundsException: " + e.toString());
            }
            if (txBlockSize != Integer.MAX_VALUE)
            {
              //
              // If we're transmitting in blocks only do one at a time.
              //
              break;
            }
            yield();
          }
        }
        catch (Exception e1)
        {
          logError("ctOutputAvailableData() -- Exception: " + e1.toString());
        }
      } // synchronized
    }

    /**
     *  ControlThread - ctNotifyControlThread
     */
    protected void ctNotifyControlThread()
    {
      synchronized(this)
      {
        notify();
      }
    }

    /**
     *  ControlThread - ctNotifyQuitControlThread
     */
    void ctNotifyQuitControlThread()
    {
      if (!ctBusy)
      {
        synchronized(this)
        {
          ctQuit = true;
          notify();
        }
      }
      else
      {
        logger.logDebug("ctNotifyQuitControlThread() -- Thread is Busy -- NOT Notifying");
      }
    }
  }

  /*========================================================================*/
  /*  ComPortImpl                                                           */
  /*========================================================================*/
  public ComPortImpl()
  {
  }

  /**
   * setProperties
   * 
   * @param ipComPortConfig - <code>ReadOnlyProperties</code>
   */
  public void setProperties(ReadOnlyProperties ipComPortConfig)
  {
    mpComPortConfig = ipComPortConfig;
  }

  /**
   * initialize
   * 
   * @param ipLogger - <code>Logger</code>
   */
  public void initialize(Logger ipLogger)
  {
    synchronized(this)
    {
      logger = ipLogger;
      logger.addCommLogger();
      logger.logDebug("ComPort.initialize()");
    }
  }

  /*--------------------------------------------------------------------------*/
  public void startup()
  {
    synchronized(this)
    {
      logger.logDebug("ComPort.startup()");
      /**
       * This Class's Child Classes should call startThread();
       */
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public void shutdown()
  {
    logger.logDebug("ComPort.shutdown() - Start");
    logTxDebug("Shutdown ComPort");
    comPortState = COM_PORT_STATE_STOPPING;
    processComPortStateChange();
    if (retryTimer != null)
    {
      retryTimer.cancel();
      retryTimer = null;
    }
    setProperties(null);
    try
    {
      inputStream.close();
    }
    catch (Exception e) {}
    inputStream = null;
    try
    {
      outputStream.close();
    }
    catch (Exception e) {}
    outputStream = null;
    inputLock = null;
    outputLock = null;
    comPortEventHandler = null;
    creationLock = null;
    logger.logDebug("ComPort.shutdown() -- notifyQuitControlThread -- Start");
    if (controlThread != null)
    {
      controlThread.ctNotifyQuitControlThread();
    }
    logger.logDebug("ComPort.shutdown() -- notifyQuit -- Start");
    notifyQuit();
    if (logger != null)
    {
      logger.logDebug("ComPort.shutdown() - End");
    }
    mpComPortConfig = null;
    selfThread = null;
    controlThread = null;
    thisComPort = null;
  }

  public void setComPortEventHandler(ComPortEventHandling oHandler)
  {
    comPortEventHandler = oHandler;
  }

  /*--------------------------------------------------------------------------*/
  public void flushOutputBuffer()
  {
    synchronized(outputLock)
    {
      outputBufferSink = 0;
      outputBufferSource = 0;
    }
  }

  /*--------------------------------------------------------------------------*/
  public void flushInputBuffer()
  {
    synchronized(inputLock)
    {
      inputBufferSink = 0;
      inputBufferSource = 0;
    }
  }

  /*--------------------------------------------------------------------------*/
  public void putByte(byte bByte)
  {
    synchronized(outputLock)
    {
      outputByteBuffer[outputBufferSink] = bByte;
      outputBufferSink++;
      notifyControl();
    }
  }

  /*--------------------------------------------------------------------------*/
  public int putBlock(byte[] byteArray, int iCount)
  {
    int result = 0;
    synchronized(outputLock)
    {
      result = putBlock(byteArray, 0, iCount);
    }
    return result;
  }


  /*--------------------------------------------------------------------------*/
  public int putBlock(byte[] byteArray, int iOffset, int iCount)
  {
    synchronized(outputLock)
    {
      int iToTxCount = iCount;
      int iCanTxCount = 0;
      while (iToTxCount > 0)
      {
        if (iToTxCount > (COM_PORT_OUTPUT_BUFFER_SIZE - outputBufferSink))
        {
          //
          // Not enough room in our Tx buffer, take what will fit.
          //
          iCanTxCount =  COM_PORT_OUTPUT_BUFFER_SIZE - outputBufferSink;
        }
        else
        {
          iCanTxCount = iToTxCount;
        }
        if (iCanTxCount > 0)
        {
          try
          {
            System.arraycopy(byteArray, iOffset, outputByteBuffer, outputBufferSink, iCanTxCount);
          }
          catch(ArrayIndexOutOfBoundsException e)
          {
            logError("putBlock() -- ArrayIndexOutOfBoundsException: " + e.toString());
          }
          catch(NullPointerException e)
          {
            logError("putBlock() -- NullPointerException: " + e.toString());
          }
          outputBufferSink += iCanTxCount;
          iToTxCount -= iCanTxCount;
          notifyControl();
        }
      } //  while (iToTxCount > 0)
    }
    return iCount;
  }


  /*--------------------------------------------------------------------------*/
  public byte getByte()
  {
    byte bByte = 0;
    synchronized(inputLock)
    {
      bByte = inputByteBuffer[inputBufferSource];
      inputBufferSource -= 1;
      if (inputBufferSource == inputBufferSink)
      {
        inputBufferSink = 0;
        inputBufferSource = 0;
      }
    }
    return bByte;
  }

  /**
   * Get a block of received bytes from this ComPort.  Do NOT get
   * more data than actually available, and do NOT get more data than the
   * caller requested.
   *
   * @param byteArray destination array
   * @param iOffset offset into destination array
   * @param iCount number of bytes requested
   * @return number of bytes actally written
   */
  public int getBlock(byte[] byteArray, int iOffset, int iCount)
  {
    synchronized(inputLock)
    {
      try
      {
        //
        // Don't get more data than is available.
        //
        int bytesAvailable = inputBufferSink - inputBufferSource;
        if ((iCount > bytesAvailable) || (iCount < 0))
        {
          iCount = bytesAvailable;
        }
        System.arraycopy(inputByteBuffer, inputBufferSource, byteArray, iOffset, iCount);
      }
      catch(ArrayIndexOutOfBoundsException e)
      {
        logError("getBlock() -- ArrayIndexOutOfBoundsException: " + e.toString());
      }
      catch(NullPointerException e)
      {
        logError("getBlock() -- NullPointerException: " + e.toString());
      }
      inputBufferSource += iCount;
      if (inputBufferSource == inputBufferSink)
      {
        inputBufferSink = 0;
        inputBufferSource = 0;
      }
    }
    return iCount;
  }

  /*--------------------------------------------------------------------------*/
  public void run()
  {
    while (!quit)
    {
      int tmpComPortState = COM_PORT_STATE_UNKNOWN;
      while (tmpComPortState != comPortState)
      {
        tmpComPortState = comPortState; // update for next pass.
        switch (comPortState)
        {
          case COM_PORT_STATE_ERROR: ; break;
          case COM_PORT_STATE_UNKNOWN: ; break;
          case COM_PORT_STATE_CREATE:
          {
            comPortState = COM_PORT_STATE_CREATING;
            createComPortControlThread();
          }
          break;
          case COM_PORT_STATE_CREATING:
          {
            synchronized(creationLock)
            {
              if (isCreated())
              {
                 comPortState = COM_PORT_STATE_CREATED;
              }
            }
          }
          break;
          case COM_PORT_STATE_CREATED:
          {
            synchronized(creationLock)
            {
              updateCreatedState();
            }
          }
          break;
          case COM_PORT_STATE_CONNECT:
          {
            //
            // We need to have our ServerSocket ACCEPT a Connection.
            //
            synchronized(creationLock)
            {
              comPortState = COM_PORT_STATE_CONNECTING;
              notifyControlThread();
            }
          }
          break;
          case COM_PORT_STATE_CONNECTING:
          {
            synchronized(creationLock)
            {
              if (isConnected())
              {
                 comPortState = COM_PORT_STATE_CONNECTED;
              }
            }
          }
          break;
          case COM_PORT_STATE_CONNECTED:
          {
            //
            // We have a Connection established to our ClientSocket.
            // We need to attach Input/Output DataStreams.
            //
            attachIOStreams();
            //
            // Allow whatever we connected to some time to get itself organized.
            //
            try
            {
              synchronized(this)
              {
                wait(2500);
              }
            }
            catch (InterruptedException e)
            {
            }
            comPortState = COM_PORT_STATE_RUNNING;
          }
          break;
          case COM_PORT_STATE_RUNNING:
          {
            if (inputBufferSource != inputBufferSink)
            {
              dataIsAvailable();
            }
            notifyControlThread();
          }
          break;
          case COM_PORT_STATE_STOP: ; break;
          case COM_PORT_STATE_STOPPING: ; break;
          case COM_PORT_STATE_STOPPED: ; break;
          default: logError("UNKNOWN comPortState: " + comPortState); break;
        }
        if (comPortState != previousComPortState)
        {
          processComPortStateChange();
        }
      }
      try
      {
        synchronized(this)
        {
          wait();
        }
      }
      catch (InterruptedException e)
      {
      }
    }
    if (logger != null)
    {
      logger.logDebug("run2() -- DONE - call closeComPort() and EXIT");
    }
    closeComPort();
    logger = null;
  }

  /*--------------------------------------------------------------------------*/
  protected void startThread()
  {
    comPortState = COM_PORT_STATE_CREATE;

    selfThread = new NamedThread(this);
    selfThread.setName("ComPortImpl-Thread-" + logger.getLoggerInstanceName());
    selfThread.start(); // Start the thread-- executes run().
  }

  /*--------------------------------------------------------------------------*/
  protected void notifyControlThread()
  {
    if (controlThread != null)
    {
      controlThread.ctNotifyControlThread();
    }
  }

  /*--------------------------------------------------------------------------*/
  protected void updateCreatedState()
  {
  }

  /*------------------------------------------------------------------------*/
  protected void createComPort()
  {
  }

  /*------------------------------------------------------------------------*/
  protected void acceptConnection()
  {
  }

  /*--------------------------------------------------------------------------*/
  protected void closeComPort()
  {
  }

  /*--------------------------------------------------------------------------*/
  protected void attachIOStreams()
  {
  }

  protected void setInputStream(BufferedInputStream bisStream)
  {
    inputStream = bisStream;
  }
  protected void setOutputStream(BufferedOutputStream bosStream)
  {
    outputStream = bosStream;
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Is our ComPort created?
   */
  protected boolean isCreated()
  {
    return false;
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Are we connected?
   */
  protected boolean isConnected()
  {
    return false;
  }

  /*--------------------------------------------------------------------------*/
  /**
   * If there is any received data, give it to the actual
   * PortDevice that owns this ComPort.  The PortDevice should use {@link #getBlock()}
   * to actually get the received data.
   *
   * <p>This method executing in the ComPort's Thread!
   */
  protected void dataIsAvailable()
  {
    int iCount = inputBufferSink - inputBufferSource;
    comPortEventHandler.comPortDataAvailableHandler(inputByteBuffer, inputBufferSource, iCount);
  }

  /*--------------------------------------------------------------------------*/
  void notifyControl()
  {
    synchronized(this)
    {
      notify();
    }
  }

  /*--------------------------------------------------------------------------*/
  private void notifyQuit()
  {
    synchronized(this)
    {
      quit = true;
      notify();
    }
  }

  /*--------------------------------------------------------------------------*/
  protected void processComPortStateChange()
  {
    if (comPortEventHandler != null)
    {
      comPortEventHandler.comPortStateChangeHandler(comPortState, previousComPortState, comPortNumber);
      previousComPortState = comPortState;
    }
  }

  /*--------------------------------------------------------------------------*/
  protected void createComPortControlThread()
  {
    if (controlThread == null)
    {
      logger.logDebug("createComPortControlThread -- Start");

      // Create a new Control THREAD (which will then create the actual Socket).
      logger.logDebug("createComPortControlThread -- Creating ControlThread");
      controlThread = new ControlThread();
      controlThread.setName("ComPortImpl-ControlThread-" + logger.getLoggerInstanceName());
      logger.logDebug("createComPortControlThread -- Starting ControlThread");
      
      /*
       * Start the ControlThread running. "run()" will CREATE the socket
       * and, if it is a Client Socket, CONNECT it (and BLOCK while connecting!).
       */
      controlThread.start();
      logger.logDebug("createComPortControlThread -- End");
    }
  }

  /*========================================================================*/
  /*  Logging convenience methods                                           */
  /*========================================================================*/
  
  /**
   * Log an error in the error log and the equipment logs
   * @param s
   */
  protected void logError(String s)
  {
    logger.logError(s);
    s = "**** " + s + " ****";
    logger.logTxByteCommunication(s.getBytes(), 0, s.length());
  }

  /**
   * Log a debug message in the equipment logs
   * @param s
   */
  protected void logTxDebug(String s)
  {
    logger.logTxByteCommunication(s.getBytes(), 0, s.length());
  }
}
