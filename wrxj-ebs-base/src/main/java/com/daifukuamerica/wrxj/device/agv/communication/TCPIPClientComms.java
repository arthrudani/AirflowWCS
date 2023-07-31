package com.daifukuamerica.wrxj.device.agv.communication;

import com.daifukuamerica.TCPIPCommException;
import com.daifukuamerica.wrxj.device.agv.AGVLogger;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Object to create client connections to a server socket.  To do something
 * meaningful with this object the user should register a callback method.
 * The call back method effectively binds any message receive <i>event</i> to
 * a processing method.
 *
 * @author A.D.
 * @since  30-Jul-2009
 */
public class TCPIPClientComms
{
  protected static final int MESG_STX = 0x02;
  protected static final int MESG_ETX = 0x03;

  private boolean            mzConnectionEstablished = false;
  private final int          CONNECTION_TIMEOUT;
  private final int          CONNECT_PORT;
  private final String       HOST_NAME;
  private AGVLogger          mpLoggerInst;
  private SocketChannel      mpCommChannel;
  private TCPIPChannelReader mpReadThread = null;
  private volatile TCPEventListener mpEvtListener;

  public TCPIPClientComms(String isHostName, int inPort, int inConnectionTimeout,
                          AGVLogger ipLoggerInstance)
  {
    mpLoggerInst = ipLoggerInstance;
    HOST_NAME = isHostName;
    CONNECT_PORT = inPort;
    CONNECTION_TIMEOUT = inConnectionTimeout;
  }

  /**
   * Method to register a TCP/IP data receive listener.
   * @param ipEvtListener
   */
  public void registerTCPEventListener(TCPEventListener ipEvtListener)
  {
    mpEvtListener = ipEvtListener;
    if (mpReadThread != null && isSocketValid())
    {
      mpReadThread.setTCPEventListener(mpEvtListener);
    }
  }

 /**
  * Method to initialize connections to the host.  Normally the user should
  * proceed in the following sequence to avoid any threading problems:
  * <ul>
  *   <li>Create this object.</li>
  *   <li>Register a call back method.</li>
  *   <li>Call this method.</li>
  * </ul>
  * @throws TCPIPCommException if no connection could be made.
  */
  public void initCommunication() throws TCPIPCommException
  {
    openConnection();
    startReader();
  }

  public ByteBuffer sendMessage(byte[] iabBytes) throws TCPIPCommException
  {
    ByteBuffer vpWriteBuf = ByteBuffer.allocate(iabBytes.length + 2);
    vpWriteBuf.put((byte)MESG_STX)
              .put(iabBytes)
              .put((byte)MESG_ETX);
    vpWriteBuf.flip();
    try
    {
      mpCommChannel.write(vpWriteBuf);
      mpLoggerInst.logCommMessage(AGVLogger.OUTBOUND_MESG, new String(iabBytes));
    }
    catch(IOException ioe)
    {
      throw new TCPIPCommException("Error writing to socket!  Host may be down.", ioe);
    }

    return(vpWriteBuf);
  }

 /**
  * Method for AGC type keep-alive messages.
  * @param iabBytes byte array of data to send.
  * @return ByteBuffer containing data that was sent.
  * @throws TCPIPCommException if no connection could be made.
  */
  public ByteBuffer sendKeepAlive(byte[] iabBytes) throws TCPIPCommException
  {
    ByteBuffer vpWriteBuf = ByteBuffer.allocate(iabBytes.length);
    vpWriteBuf.put(iabBytes);
    vpWriteBuf.flip();
    try
    {
      mpCommChannel.write(vpWriteBuf);
      mpLoggerInst.logCommMessage(AGVLogger.OUTBOUND_MESG, new String(iabBytes));
    }
    catch (IOException ioe)
    {
      throw new TCPIPCommException("Error writing to socket!  Host may be down.", ioe);
    }
    return(vpWriteBuf);
  }

  public ByteBuffer sendMessage(String isMessage) throws TCPIPCommException
  {
    byte[] vabBytes = isMessage.getBytes();
    return(sendMessage(vabBytes));
  }

 /**
  * Method to close the connection to a server socket.  This method will
  * shutdown the reader thread and then the communication channel.
  */
  public void closeConnection()
  {
    mzConnectionEstablished = false;

    if (mpReadThread != null)
    {
      mpReadThread.stopThread();
      mpReadThread.interrupt();        // This should close the selector and
                                       // therefore its associated channels.
      mpReadThread = null;
    }

    if (mpCommChannel != null)
    {
      try
      {
        mpCommChannel.close();
        mpCommChannel = null;
      }
      catch(IOException ioe) {}
    }
  }

  public boolean isConnectionEstablished()
  {
    return(mzConnectionEstablished);
  }

 /**
  *  Method to check if connection to server is up.
  *  @return <code>boolean</code> of <code>true</code> only if host is accessible.
  */
  public boolean isSocketValid()
  {
    boolean vzRtn = false;

    if (mpCommChannel != null)
    {
      vzRtn = mpCommChannel.isConnected();
    }

    return(vzRtn);
  }

 /**
  * Method for information purposes.  Get the host name of connection.
  * @return the host name of connection.
  */
  public String getHostName()
  {
    return(HOST_NAME);
  }

 /**
  * Method to open a connection to a server socket.
  * @throws TCPIPCommException if no connection could be made.
  */
  public void openConnection() throws TCPIPCommException
  {
    try
    {
      mpCommChannel = SocketChannel.open();
      mpCommChannel.configureBlocking(false);
      mpCommChannel.connect(new InetSocketAddress(HOST_NAME, CONNECT_PORT));
      Thread.sleep(5000);
    }
    catch(Exception e)
    {
      throw new TCPIPCommException("Connection to host " + HOST_NAME +
                                 " failed for port " + CONNECT_PORT + ". ", e);
    }

    int vnTick = 0;

    try
    {
      int vnSleepTime = 2000;
      int vnMaxTries = CONNECTION_TIMEOUT/vnSleepTime;

      for(vnTick = 0; !mpCommChannel.finishConnect(); vnTick++)
      {
        if (vnTick < vnMaxTries)
        {
          try { Thread.sleep(vnSleepTime); }
          catch(InterruptedException exc) { return; }
        }
        else
        {
          throw new TCPIPCommException("Unable to connect to Host " + HOST_NAME +
                                       " after " + vnTick + " tries!");
        }
      }
      mzConnectionEstablished = true;
    }
    catch(IOException exc)
    {
      throw new TCPIPCommException("Error connecting to host... Host Socket not " +
                                   "available for host " + HOST_NAME +
                                   " Connection retried " + vnTick + " times!", exc);
    }
  }

 /**
  * Method to start a reader thread.  The sending thread is whoever instantiates
  * this object.
  */
  public void startReader()
  {
    if (mpReadThread != null)
    {                                  // Something is surely wrong if this happens!
                                       // Attempt to clean it up before going on.
      try
      {
        mpReadThread.stopThread();
        mpReadThread.interrupt();
        Thread.sleep(15000L);
      }
      catch (InterruptedException ex)
      {
        // Ignore it.
      }
    }
                                       // Create Reader thread, assign properties
                                       // to it, and start.
    mpReadThread = new TCPIPChannelReader("TCPIPClientComms.TCPIPReader");
    mpReadThread.setTCPEventListener(mpEvtListener);
    mpReadThread.setCommuncationChannel(mpCommChannel);
    mpReadThread.setLogger(mpLoggerInst);
    mpReadThread.start();
  }
}
