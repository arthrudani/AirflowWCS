package com.daifukuamerica.wrxj.controller.aemessenger.tcp;

import com.daifukuamerica.TCPIPCommException;
import com.daifukuamerica.TCPIPLogger;
import com.daifukuamerica.impl.ConnectionType;
import com.daifukuamerica.impl.TCPIPConstants;
import com.daifukuamerica.impl.TCPIPLoggerImpl;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.controller.aemessenger.AEMessage;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Channel Reader thread.  This class reads messages off a socket channel. To
 * effectively use this class these rules must be followed:
 *
 * <ul>
 *   <li>If using this thread in relation to a TCP/IP Server socket, a communication
 * channel should already be established before starting this thread. Namely, a
 * server connection should already be established and then pass a client
 * communication channel to this thread.</li>
 *
 *   <li>If using this thread in relation to a TCP/IP Client socket, use the
 * {@link #TCPIPReaderWriter(java.util.Properties) TCPIPReaderWriter(java.util.Properties)}
 * constructor followed by a call to {@link #connToServer() connToServer()}</li>
 * </ul>
 *
 * @author A.D.
 * @since  11-Feb-2013
 */
public class AEMTcpipReaderWriter extends Thread
{
/*============================================================================
 *                      Configuration settings.
 *============================================================================*/
  public static final String MSG_SOURCE = "CfgMsgSrc";
  public static final String MSG_TARGET = "CfgMsgTrgt";
  
  private static final byte[] STX = new byte[] {(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA};
  private static final byte[] ETX = new byte[] {(byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB};
  private static final int    HEADER_SIZE = 24;
  private static final int    WRAPPER_SIZE = STX.length + HEADER_SIZE + ETX.length;
  
  private int              mnStxIdx = 0;
  private int              mnEtxIdx = 0;
  private boolean          mzSTXFound = false;
  private boolean          mzETXFound = false;
  
  private Long             mnRemoteId = null;
  private int              mnSource;
  private String           msClientAddress = "";
  private String           msLogDir = "";
  private String           msServerIP = "";
  private int              mnClientRetryIntvl = 6000;
  private int              mnConnectPort = 0;

  private final int        STOP_RUNAWAY_COUNT = 5;
  private int              mnKillCounter = 0;
  protected Properties     mpProperties;
  protected TCPIPLogger    mpLogger;
  protected SocketChannel  mpCommChannel;
  private Selector         mpSelector = null;
  
  private boolean          mzIsConnecting = false;

  private boolean          mpTraceLog = Application.getBoolean("AEMessengerConsoleTrace", false);
  
  // Send Buffer
  private class AEMQueuedMessage
  {
    long mnTransactionId;
    String msMessage;
    
    public AEMQueuedMessage(long inTransactionId, String isMessage)
    {
      mnTransactionId = inTransactionId;
      msMessage = isMessage;
    }
  }
  protected LinkedBlockingQueue<AEMQueuedMessage> mpSendBuffer = new LinkedBlockingQueue<>(100);

  
/*============================================================================
 *                      Thread related variables.
 *============================================================================*/
  private volatile boolean               mzThreadStop = false;
  private volatile AEMReadEvent        mpTCPReadHandler;
  private volatile AEMSocketCloseEvent mpTCPClientCloseHandler;

  /**
   * <b>Constructor to use for Client type connections.</b>  This constructor call must be
   * followed by a {@link #connToServer connToServer()} and then a thread start call.
   * @param ipConfigProp a reference to a tcp/ip configuration file (or properties)
   * @param ipLogger logger.
   */
  public AEMTcpipReaderWriter(Properties ipConfigProp, TCPIPLogger ipLogger) throws TCPIPCommException
  {
    super();
    String vsConnectionType = ipConfigProp.getProperty(TCPIPConstants.SOCKET_TYPE);
    if (ConnectionType.isServer(vsConnectionType))
    {
      throw new UnsupportedOperationException("Invalid constructor chosen for Server Type connection!");
    }
    initConfigSettings(ipConfigProp);
    mpLogger = ipLogger;
  }

  /**
   * <b>This constructor should normally be used from a Server Type connection
   * since in that case the established Server connection will provide the appropriate
   * communications channel to talk to the client</b>
   *
   * @param ipConfigProp a reference to a tcp/ip configuration file (or properties)
   * @param ipCommChannel Channel to which this client is connected.
   * @param ipLogger logger if null use default.
   */
  public AEMTcpipReaderWriter(Properties ipConfigProp, SocketChannel ipCommChannel, TCPIPLogger ipLogger)
         throws TCPIPCommException
  {
    super();
    if (ipCommChannel != null && msClientAddress.isEmpty())
    {
      InetSocketAddress vpAddr = (InetSocketAddress)ipCommChannel.socket().getRemoteSocketAddress();
      msClientAddress = vpAddr.getAddress().getHostAddress();
    }

    /*
     * Name this thread with the remote client's ip address for easy identification.
     */
    setName("AEMessenger_" + msClientAddress + "_#" + getId());
    mpCommChannel = ipCommChannel;

    initConfigSettings(ipConfigProp);
    if(ipLogger == null)
    {
      mpLogger = new TCPIPLoggerImpl("com.daifukuamerica.impl", msLogDir, "Client_" + msClientAddress + "_%g.log");
    }
    else
    {
      mpLogger = ipLogger;
    }
  }

  /**
   * Make a new copy of a dead AEMTcpipReaderWriter
   * 
   * @param ipCloneMe
   * @throws TCPIPCommException
   */
  public AEMTcpipReaderWriter(AEMTcpipReaderWriter ipCloneMe)
      throws TCPIPCommException
  {
    this(ipCloneMe.mpProperties, ipCloneMe.mpCommChannel, ipCloneMe.mpLogger);
    mpTCPReadHandler = ipCloneMe.mpTCPReadHandler;
    mpSendBuffer.addAll(ipCloneMe.mpSendBuffer);
    warnIfOverThreshold();
  }
  
  /**
   * Get the client IP
   * @return
   */
  public String getClientIPAddress()
  {
    return msClientAddress;
  }

  /**
   * Get the remote ID
   * @return
   */
  public Long getRemoteId()
  {
    return mnRemoteId;
  }

  /**
   * Set up to consume data from clients.
   * @param ipEventHandler reference of type {@link com.daifukuamerica.TCPIPReadEvent TCPIPReadEvent}
   */
  public <E extends AEMReadEvent> void registerReadEvent(E ipEventHandler)
  {
    mpTCPReadHandler = ipEventHandler;
  }

  /**
   * Register for notification when client closes connection and this thread dies.
   * @param ipEventHandler reference of type {@link com.daifukuamerica.TCPIPSocketCloseEvent TCPIPSocketCloseEvent}
   */
  public <E extends AEMSocketCloseEvent> void registerCloseEvent(E ipEventHandler)
  {
    mpTCPClientCloseHandler = ipEventHandler;
  }

  /**
   * Tests if connection is active with server or client.
   *
   * @return <code>true</code> if connection is alive, <code>false</code> otherwise.
   */
  public boolean isConnectionAlive()
  {
    return mpCommChannel != null ? mpCommChannel.isConnected() : false;
  }

  /**
   * Is this instance trying to connect?
   */
  public boolean isConnecting()
  {
    return mzIsConnecting;
  }
  
  /**
   * For a Client type connection this method needs to be called to connect to
   * the server.  <i>This method should not be called for Server type connections.</i>
   *
   * @throws TCPIPCommException if connection attempt fails for any reason.
   */
  public void connToServer() throws TCPIPCommException
  {
    try
    {
      mzIsConnecting = true;
      
      if (mpCommChannel != null)
      {
        // Connection must already be established!
        return;
      }
  
      try
      {
        mpCommChannel = SocketChannel.open();
        mpCommChannel.configureBlocking(true);
      }
      catch(IOException e)
      {
        throw new TCPIPCommException("Error configuring Socket Channel...", e);
      }
  
      // Wait for the connection
      long vnStart = System.currentTimeMillis();
      int vnTick = 0;
      int vnMaxAttempts = 5;
      try
      {
        while (true)
        {
          try
          {
            mpCommChannel.socket().connect(new InetSocketAddress(msServerIP, mnConnectPort), mnClientRetryIntvl);
            mpCommChannel.configureBlocking(false);
            break;
          }
          catch (SocketTimeoutException ste)
          {
            if (vnTick >= vnMaxAttempts)
            {
              mpCommChannel.close();
              throw new TCPIPCommException("Error connecting to host [" + msServerIP
                  + ":" + mnConnectPort + "]. Failed connection after " + vnTick
                  + " tries in " + (System.currentTimeMillis() - vnStart) + " ms!");
            }
            vnTick++;
            mpLogger.logErrorMessage("Trying to connect to Server " + msServerIP
                + ":" + mnConnectPort + " (attempt #" + vnTick + ")");
          }
        }
      }
      catch (IOException exc)
      {
        throw new TCPIPCommException("Error connecting to host [" + msServerIP
            + ":" + mnConnectPort + "]. Failed connection after " + vnTick
            + " tries in " + (System.currentTimeMillis() - vnStart) + " ms!",
            exc);
      }
    }
    finally
    {
      mzIsConnecting = false;
    }
  }

  /**
   * Send a message
   * 
   * @param inTransactionId
   * @param isMessage
   * @return
   * @throws TCPIPCommException
   */
  public ByteBuffer sendMessage(long inTransactionId, String isMessage)
      throws TCPIPCommException
  {
    return sendMessage(inTransactionId, isMessage.getBytes());
  }

  /**
   * Method to send message to receiver.
   *
   * <P><I>NOTE: This is a blocking-send operation, regardless of the socket
   * blocking mode.</I></P>
   *
   * @param mabMessage Byte array containing message.
   * @return ByteBuffer containing complete message sent to host.
   *
   * @throws TCPIPCommException if there is a communication error.
   */
  public ByteBuffer sendMessage(long inTransactionId, byte[] mabMessage)
      throws TCPIPCommException
  {
    ByteBuffer vpWriteBuf = ByteBuffer.allocate(
        mabMessage.length + WRAPPER_SIZE);
    vpWriteBuf.order(ByteOrder.LITTLE_ENDIAN);
    vpWriteBuf.put(STX)
              .putLong(mnSource)
              .putLong(inTransactionId)
              .putLong(mabMessage.length)
              .put(mabMessage)
              .put(ETX);
    vpWriteBuf.flip();

    try
    {
      byte[] vpTemp = vpWriteBuf.array();

      int vnSent = mpCommChannel.write(vpWriteBuf);
      // This loop keeps us from dropping bytes on a non-blocking port when the
      // buffer is full.
      while (vnSent < vpTemp.length)
      {
        // Give the port a chance to clear out.
        SKDCUtility.sleep(100);
        
        vnSent += mpCommChannel.write(vpWriteBuf);
      }

      mpLogger.logCommMessage(TCPIPLogger.MESG_DIR_SEND, new String(vpTemp));
    }
    catch(IOException ioe)
    {
      throw new TCPIPCommException("Error writing to socket!  Host may be down.", ioe);
    }

    return(vpWriteBuf);
  }

  @Override
  public void run()
  {
    ByteBuffer vpReadByteBuf = ByteBuffer.allocate(512);
    ByteBuffer vpAccumulationBuf = ByteBuffer.allocate(8192);
    vpAccumulationBuf.order(ByteOrder.LITTLE_ENDIAN); // Match .NET Int32

    try
    {
      if (!isConnectionAlive())
      {
        connToServer();
      }
    }
    catch (TCPIPCommException e)
    {
      mpLogger.logErrorMessage("Unable to connect... Reader Thread terminating.");
      return;
    }
    catch (UnresolvedAddressException uae)
    {
      mpLogger.logErrorMessage("Unresolved address [" + msServerIP
          + "]... Reader Thread terminating.");
      return;
    }
    
    if (mzThreadStop)
    {
      return;
    }
    
    try
    {
      mpCommChannel.configureBlocking(false);
      mpSelector = Selector.open();    // Create the Selector.

      // Register the channel with the selector for read operations.
      mpCommChannel.register(mpSelector, SelectionKey.OP_READ);
    }
    catch(IOException e)
    {
      mpLogger.logErrorMessage("Selector error in reader thread... " +
                               "Reader Thread terminating.");
      return;
    }

    while(!mzThreadStop)
    {
      try
      {
        if (hasBufferedMessages())
        {
          mpSelector.select(500);
        }
        else
        {
          mpSelector.select(30000);
        }
      }
      catch(IOException exc)
      {
        // Get here if selector is interrupted.
      }

      if (mzThreadStop || mpCommChannel.socket().isClosed())
      {
        try { mpSelector.close(); } catch(Exception ioe) {}
        mzThreadStop = true;
        continue;
      }

      try
      {
        // Receive
        receive(vpReadByteBuf, vpAccumulationBuf);

        // Send
        if (!mzThreadStop)
        {
          sendQueuedMessages();
        }
      }
      catch (IOException e)
      {
        mzThreadStop = true;
        mpLogger.logErrorMessage("Connection to " + msClientAddress + " ("
            + (mnRemoteId == null ? "n/a" : mnRemoteId.toString())
            + ") dropped: " + e.getMessage());
      }
      catch (TCPIPCommException e)
      {
        mzThreadStop = true;
        mpLogger.logErrorMessage("Connection to " + msClientAddress + " ("
            + (mnRemoteId == null ? "n/a" : mnRemoteId.toString())
            + ") dropped: " + e.getMessage());
      }
      catch (Exception exc)
      {
        mpLogger.logErrorMessage("Data Error!", exc);
      }
      finally
      {
        if (mzThreadStop)
        {
          closeSocket();
        }
      }
    }
  }

  /**
   * Receive
   * 
   * @param vpReadByteBuf
   * @param vpAccumulationBuf
   * @throws Exception
   */
  private void receive(ByteBuffer vpReadByteBuf, ByteBuffer vpAccumulationBuf)
      throws Exception
  {
    Set<SelectionKey> vpSelectedKeySet = mpSelector.selectedKeys();
    for(Iterator<SelectionKey> vpIter = vpSelectedKeySet.iterator(); vpIter.hasNext();)
    {
      SelectionKey vpSelectedKey = vpIter.next();
      vpIter.remove();

      if (vpSelectedKey.isValid() && vpSelectedKey.isReadable())
      {
        SocketChannel vpSockChannel = (SocketChannel)vpSelectedKey.channel();
        if (vpSockChannel.read(vpReadByteBuf) != -1)
          processChannelData(vpReadByteBuf, vpAccumulationBuf);
        else mzThreadStop = true;
      }
    }
  }
  
  /**
   * Stop this thread
   */
  public void stopThread()
  {
    mzThreadStop = true;
    interrupt();
    closeSocket();
  }

 /**
  * Since TCP does not guarantee full packet delivery at the time of a read, this
  * method makes sure enough bytes are gathered off a channel to form a complete message.
  * @param ipReadBuf the primary buffer channel is flushed into.
  * @param ipAccumulationBuf accumulation buffer used to form a complete message.
  */
  private void processChannelData(ByteBuffer ipReadBuf, ByteBuffer ipAccumulationBuf)
          throws IOException
  {
    ipReadBuf.flip();
    byte vbByte = 0;
    boolean vzClearBuffer = true;
    mnKillCounter++;

    if (mnKillCounter > STOP_RUNAWAY_COUNT && !ipReadBuf.hasRemaining())
    {
      mnStxIdx = 0;
      mzSTXFound = false;
      mzThreadStop = true;
      mpLogger.logErrorMessage("Could not find STX.  Closing socket.");
      return;
    }

    while(ipReadBuf.hasRemaining())
    {
      int vnCurrPos = ipReadBuf.position();
      vbByte = ipReadBuf.get();
      traceLogByte(vbByte);

      //get rid of nulls and replace with space to prevent string errors
      if (vbByte == 0x00)
      {
        ipReadBuf.put(vnCurrPos, (byte)0x20);
        vnCurrPos = ipReadBuf.position();
      }
      // STX
      if (!mzSTXFound)
      {
        if (vbByte == STX[mnStxIdx])
        {
          mnStxIdx++;
          if (mnStxIdx >= STX.length)
          {
            mnStxIdx = 0;
            mzSTXFound = true;
          }
        }
        else
        {
          // Still looking for an STX
          clearAccumulation(ipAccumulationBuf);
          mnStxIdx = 0;
          // TODO: accumulate and log discarded bytes in chunks
          mpLogger.logErrorMessage("Discarding byte (no STX): " + byteToString(vbByte));
          continue;
        }
      }

      ipAccumulationBuf.put(vbByte);
      mnKillCounter = 0;

      if (mzSTXFound)
      {
        // ETX
        if (vbByte == ETX[mnEtxIdx])
        {
          mnEtxIdx++;
          if (mnEtxIdx >= ETX.length)
          {
            mnEtxIdx = 0;
            mzETXFound = true;
            traceLogMsg("\n");
          }
        }
        else
        {
          mnEtxIdx = 0;
        }

        if (mzETXFound)
        {
          mzSTXFound = false;
          mzETXFound = false;
          
          ipAccumulationBuf.flip();
          byte[] vabLogMe = new byte[ipAccumulationBuf.limit()];
          ipAccumulationBuf.get(vabLogMe);
          String vsMessage = new String(vabLogMe);
          
          mpLogger.logCommMessage(TCPIPLogger.MESG_DIR_RECV, vsMessage);
          if (mpTCPReadHandler != null)
          {
            ipAccumulationBuf.position(STX.length);
            AEMessage vpMsg = new AEMessage();
            vpMsg.setSource(ipAccumulationBuf.getLong());
            vpMsg.setTransactionID(ipAccumulationBuf.getLong());
            vpMsg.setSize(ipAccumulationBuf.getLong());
            byte[] vabMsgBody = new byte[ipAccumulationBuf.limit() - WRAPPER_SIZE];
            ipAccumulationBuf.get(vabMsgBody, 0, vabMsgBody.length);
            vpMsg.setMessageBody(vabMsgBody);
            if (mnRemoteId == null)
            {
              mnRemoteId = vpMsg.getSource();
            }
            else
            {
              if (mnRemoteId.longValue() != vpMsg.getSource().longValue())
              {
                mpLogger.logErrorMessage(
                    "Unexpected remote instance change from " + mnRemoteId
                        + " to " + vpMsg.getSource());
                mnRemoteId = vpMsg.getSource();
              }
            }
            mpTCPReadHandler.receivedData(this, vpMsg);
          }
          else
          {
            mpLogger.logErrorMessage(
                "No callback method registered. Discarding message "
                    + vsMessage);
          }

          clearAccumulation(ipAccumulationBuf);

          if (ipReadBuf.remaining() == 0)
          {
            break;
          }

          ByteBuffer vpTmpBuff = ByteBuffer.allocate(ipReadBuf.remaining()+1);
          vpTmpBuff.put(ipReadBuf.array(), ipReadBuf.position(), vpTmpBuff.limit() - 1);
          vpTmpBuff.flip();
          if (incompleteMessage(vpTmpBuff))
          {
            ipReadBuf.clear();
            vpTmpBuff.flip();
            ipReadBuf.put(vpTmpBuff);
            vzClearBuffer = false;
            break;
          }
        }
      }
    }

    if (vzClearBuffer)
      ipReadBuf.clear();
    
    traceLogMsg("Waiting for more data...\n");
  }
  
  /**
   * 
   * @param ipAccumulationBuf
   */
  private void clearAccumulation(ByteBuffer ipAccumulationBuf)
  {
    ipAccumulationBuf.clear();
    ipAccumulationBuf.put(new byte[8192]);
    ipAccumulationBuf.clear();
  }

  /**
   * 
   * @param ipMessageBytes
   * @return
   */
  private boolean incompleteMessage(ByteBuffer ipMessageBytes)
  {
    boolean vzIncomplete = true;

    traceLogMsg("-----> INCOMPLETE CHECK:");
    int vnEndOfText = 0;
    while(ipMessageBytes.hasRemaining())
    {
      byte vbByte = ipMessageBytes.get();
      traceLogByte(vbByte);
      if (vbByte == ETX[vnEndOfText])
      {
        vnEndOfText++;
        if (vnEndOfText >= ETX.length)
        {
          vzIncomplete = false;
          mzSTXFound = false;
          break;
        }
      }
    }

    traceLogMsg(" -> " + vzIncomplete + "\n");
    return(vzIncomplete);
  }

  private String byteToString(byte b)
  {
    StringBuffer sb = new StringBuffer();
    if (b < 32 || b > 127)
    {
      sb.append("[");
      if (Byte.toUnsignedInt(b) < 16)
      {
        sb.append("0");
      }
      sb.append(Integer.toHexString(Byte.toUnsignedInt(b)).toUpperCase());
      sb.append("]");
    }
    else
    {
      sb.append((char)b);
    }
    return sb.toString();
  }
  
  /**
   * Debug log a byte to console
   * @param b
   */
  private void traceLogByte(byte b)
  {
    if (mpTraceLog)
    {
      System.out.print(byteToString(b));
    }
  }
  
  /**
   * Debug a string to console
   * @param isMessage
   */
  private void traceLogMsg(String isMessage)
  {
    if (mpTraceLog)
    {
      System.out.print(isMessage);
    }
  }
  
  /**
   * Initialize
   * @param ipConfigProp
   * @throws TCPIPCommException
   */
  private void initConfigSettings(Properties ipConfigProp)
      throws TCPIPCommException
  {
    mpProperties = ipConfigProp;
    
    String vsConnectionType = ipConfigProp.getProperty(TCPIPConstants.SOCKET_TYPE);
    msLogDir = ipConfigProp.getProperty(TCPIPConstants.LOG_PATH);
    msServerIP = ipConfigProp.getProperty(TCPIPConstants.SERVER_IP, "");
    if (!ConnectionType.isServer(vsConnectionType) && msServerIP.isEmpty())
    {
      throw new TCPIPCommException("Invalid TCP/IP configuration file entry! "
          + TCPIPConstants.SERVER_IP + " not specified!");
    }

    // This is the port this client will connect to.
    mnConnectPort = Integer.parseInt(ipConfigProp.getProperty(TCPIPConstants.LISTEN_PORT));
    if (mnConnectPort == 0)
    {
      throw new TCPIPCommException("Invalid TCP/IP configuration file entry!"
          + TCPIPConstants.LISTEN_PORT + " not specified!");
    }
    // Connection retry interval. Default to 6 seconds if not specified.
    mnClientRetryIntvl = 1000 * Integer.parseInt(
        ipConfigProp.getProperty(TCPIPConstants.CLIENT_RETRY_INTERVAL, "6"));
    
    // This is the message source
    mnSource = Integer.parseInt(ipConfigProp.getProperty(MSG_SOURCE));
    
    // This is the message destination name
    String vsName = ipConfigProp.getProperty(MSG_TARGET);
    if (SKDCUtility.isFilledIn(vsName))
    {
      setName("AEMessenger:" + vsName + ":" + msServerIP + ":" + mnConnectPort);
    }
  }

  /**
   * Close the socket
   */
  protected void closeSocket()
  {
    try
    {
      if (mpCommChannel != null)
        mpCommChannel.close();

      if (mpSelector != null)
        mpSelector.close();
      if (mpTCPClientCloseHandler != null)
        mpTCPClientCloseHandler.onSocketClose(this);

      mpCommChannel = null;
    }
    catch(IOException ioe)
    {
    }
  }
  
  public Long getRemoteInstanceId()
  {
    return mnRemoteId;
  }

  /*====================================================================*/
  /* Outbound message queuing                                           */
  /*====================================================================*/
  
  private boolean hasBufferedMessages()
  {
    synchronized (mpSendBuffer)
    {
      return !mpSendBuffer.isEmpty();
    }
  }
  
  /**
   * Queue a message for sending
   * 
   * @param inTransactionId
   * @param isMessage
   * @return
   */
  public boolean queueMessage(long inTransactionId, String isMessage)
  {
    boolean rtnval;
    synchronized (mpSendBuffer)
    {
      rtnval = mpSendBuffer.offer(new AEMQueuedMessage(inTransactionId, isMessage));
    }
    warnIfOverThreshold();
    if (mpSelector != null)
    {
      mpSelector.wakeup();
    }
    return rtnval;
  }

  /**
   * Send queued messages
   */
  private void sendQueuedMessages() throws TCPIPCommException
  {
    int vnMsgCount = 0;
    synchronized (mpSendBuffer)
    {
      AEMQueuedMessage vpQM = mpSendBuffer.peek();
      while (vpQM != null && vnMsgCount < 5)
      {
        vnMsgCount++;
        sendMessage(vpQM.mnTransactionId, vpQM.msMessage);
        mpSendBuffer.remove(vpQM);
        vpQM = mpSendBuffer.peek();
        if (vpQM != null)
        {
          SKDCUtility.sleep(50);
        }
      }
      warnIfOverThreshold();
    }
  }
  
  /**
   * Warn if the buffered message count is over a threshold value
   */
  private void warnIfOverThreshold()
  {
    int vnSize = mpSendBuffer.size();
    if (vnSize > 5)
    {
      mpLogger.logErrorMessage("There are " + vnSize + " buffered messages!");
    }
  }
}  // End TCPIPReaderWriter Class.

