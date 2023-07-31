package com.daifukuamerica.wrxj.comport.net;

/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SL Daifuku Corp.
 */

import com.daifukuamerica.wrxj.clc.ControllerDefinition;
import com.daifukuamerica.wrxj.comport.ComPortImpl;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPortServer;
import com.daifukuamerica.wrxj.dbadapter.data.PortData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A TCP/IP Socket Implementation.  Configuration parameters specify either a
 * Client/Connect or Server/Listen socket type.
 *
 * @author Stephen Kendorski
 * @version 1.0
 */
public class SocketComPort extends ComPortImpl
{
  private int            retryTotalTime = 0;
  private int            retryInterval = 5000;
  private RetryTimerTask retryTimerTask = null;
  
  // Server/Listen Socket connection
  protected ServerSocket connectedServerSocket = null;
  
  // Client/Connect Socket connection
  protected Socket       connectedClientSocket = null;

  int                  socketType = SOCKET_TYPE_LISTEN; // Listen/Server or Connect/Client
  protected InetAddress  inetAddress = null;
  private String       localHostName = "";
  private String       localHostIPAddress = "";

  protected String       connectToHostName = null;
  protected String       connectToHostIPAddress = "";
  protected int          connectToHostPortNumber = 0;

  protected String       hostName = "";
  protected String       hostIPAddress = "";
  protected int          hostPortNumber = 0;

  /*--------------------------------------------------------------------------*/
  public SocketComPort()
  {
  }

  /*--------------------------------------------------------------------------*/
  private void setHost(String sHost)
  {
    //
    // Determine if the sHost string is a host name or ip address (assumes an
    // ip address is four numeric fields separated by periods.
    //
    StringTokenizer st = new StringTokenizer(sHost, ".");
    int iInts = 0;
    boolean vbHostName = true;
    while (st.hasMoreTokens())
    {
      String s = st.nextToken();
      int c = 0;
      boolean done = false;
      for (int i = 0; i < s.length(); i++)
      {
        int ch = s.charAt(i);
        if ((ch < 0x30) || (ch > 0x39))
        {
          done = true;
          break;
        }
        c = (c * 10) + ch - 0x30;
      }
      if (done)
      {
        break;
      }
      iInts++;
    }
    if (iInts == 4)
    {
      vbHostName = false;
    }
    if (vbHostName)
    {
      connectToHostName = sHost;
      connectToHostIPAddress = "";
    }
    else
    {
      connectToHostIPAddress = sHost;
      connectToHostName = null;
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  @Override
  public void startup()
  {
    super.startup();
    logger.logDebug("SocketComPort.startup() - Start");
    synchronized(this)
    {
      String vsName = mpComPortConfig.getString(ControllerDefinition.CONTROLLER_NAME);
      PortData vpPort;
      try
      {
        StandardPortServer vpPortServer = Factory.create(StandardPortServer.class); 
        vpPort = vpPortServer.getPort(vsName);
        if (vpPort == null)
        {
          throw new DBException("Port [" + vsName + "] not found!");
        }
        vpPortServer.cleanUp();
      }
      catch (DBException ex)
      {
        logSocketError("UNKNOWN Port \"" + vsName + "\" - startup()");
        System.err.println("SocketComPort.startup: Port " + vsName + " could not be found.");
        return;
      }
      String vsHost = vpPort.getServerName();
      int vnSocketType = vpPort.getCommunicationMode();
      setHost(vsHost);
      String vsPortNumber = vpPort.getSocketNumber();
      if (vpPort.getRetryInterval() < PortData.MINIMUM_INTERVAL)
      {
        logSocketDebug("Invalid Retry Interval: " + vpPort.getRetryInterval()
            + "--using " + retryInterval);
      }
      else
      {
        retryInterval = vpPort.getRetryInterval();
      }
      
      if (vnSocketType == DBConstants.SLAVE)
      {
        logSocketDebug("SocketType: Connect - startup()");
        socketType = SOCKET_TYPE_CONNECT;
      }
      else
      {
        if (vnSocketType == DBConstants.MASTER)
        {
          logSocketDebug("SocketType: Listen - startup()");
          socketType = SOCKET_TYPE_LISTEN;
        }
        else
        {
          logSocketError("UNKNOWN SocketType \"" + vnSocketType + "\" - startup()");
        }
      }
      if (vsPortNumber != null)
      {
        int vnPortNumber = -1;
        try
        {
          vnPortNumber = Integer.parseInt(vpPort.getSocketNumber());
        }
        catch (NumberFormatException ex)
        {
          // log error below
        }
        if (vnPortNumber >= 0)
        {
          logSocketDebug("PortNumber: " + vsPortNumber + " - startup()");
          connectToHostPortNumber = vnPortNumber;
        }
        else
        {
          logSocketError("INVALID PortNumber \"" + vsPortNumber + "\" - startup()");
        }
      }
      else
      {
         logSocketError("MISSING PortNumber: " + vsPortNumber + " - startup()");
      }
       try
      {
        inetAddress = InetAddress.getLocalHost(); // Convert host into an InetAddress.
        localHostName = inetAddress.getHostName();
        localHostIPAddress = inetAddress.getHostAddress();
        logSocketDebug("localHost: Name \"" + localHostName +
                 "\"  IPAddr \"" + localHostIPAddress + "\" - startup()");
      }
      catch (UnknownHostException e)
      {
        /**
         * The IP address of a host could not be determined.
         */
        logSocketDebug("startup() -- UnknownHostException -- ???");
      }
//      if (retryTimer == null)
//      {
//        retryTimer = new Timer("SocketComPort-" + logger.getLoggerInstanceName());
//      }
//      if (retryTimerTask == null)
//      {
//        retryTimerTask = new RetryTimerTask();
//      }
      startThread();
    }
    logger.logDebug("SocketComPort.startup() - End");
  }

  /*------------------------------------------------------------------------*/
  /*------------------------------------------------------------------------*/
  @Override
  protected void createComPort()
  {
    if (connectedClientSocket != null)
    {
//      System.out.println(mpComPortConfig.getString(ControllerDefinition.CONTROLLER_NAME) + "--already connected!");
      return;
    }
    super.createComPort();
    // Socket used for establishing a connection
    Socket clientSock = null;
    ServerSocket serverSock = null;
    if (socketType == SOCKET_TYPE_CONNECT)
    {
      logAllDebug("Attempting to Connect to Host: Name \"" + connectToHostName +
           "\"  IPAddr \"" + connectToHostIPAddress +
           "\"  Port: " + connectToHostPortNumber);
      try
      {
        // Was a name or an inet specified?
        if (connectToHostName != null)
        {
          // Create and Connect to a (named) remote host - BLOCKING I/O!
          clientSock = new Socket(connectToHostName, connectToHostPortNumber);
        }
        else
        {
          // Create and Connect to a remote host (at IP address) - BLOCKING I/O!
          clientSock = new Socket(connectToHostIPAddress, connectToHostPortNumber);
        }
        //
        // If socket constructor returned without error, then Client Socket
        // is connected (but, may NOT yet be accepted!).
        //
        if (comPortState == COM_PORT_STATE_STOPPING)
        {
          return;
        }
        inetAddress = clientSock.getInetAddress(); // Convert host into an InetAddress.
        hostName = inetAddress.getHostName();
        hostIPAddress = inetAddress.getHostAddress();
        hostPortNumber = clientSock.getPort();
        if (comPortState == COM_PORT_STATE_STOPPING)
        {
          return;
        }
        logAllDebug("Connected OK to Host: Name \"" + hostName +
               "\"  IPAddr \"" + hostIPAddress +
               "\"  Port: " + hostPortNumber);
        connectedClientSocket = clientSock;
      }
      catch (ConnectException e)
      {
         /*
          * Typically, the connection was refused remotely (e.g., no process is
          * listening on the remote address/port).
          *
          * This is NOT a hard error, so we need to retry to connect.
          */
        logSocketDebug("Connection REFUSED to Host: Name \"" + connectToHostName +
           "\"  IPAddr \"" + connectToHostIPAddress +
           "\"  Port: " + connectToHostPortNumber + " -- UNABLE to Connect");
        justCreated = false;
        startRetryTimer();
      }
      catch (NoRouteToHostException e)
      {
        /*
         * Typically, the remote host cannot be reached because of an
         * intervening firewall, or if an intermediate router is down.
         */
        logSocketDebug("NO Route to Host: Name \"" + connectToHostName +
           "\"  IPAddr \"" + connectToHostIPAddress +
           "\"  Port: " + connectToHostPortNumber + " -- UNABLE to Connect");
      }
      catch (BindException e)
      {
        /*
         * Typically, the port is in use, or the requested local address could
         * not be assigned.
         */
        logError("Port Is In Use to Host: Name \"" + connectToHostName +
           "\"  IPAddr \"" + connectToHostIPAddress +
           "\"  Port: " + connectToHostPortNumber + " -- UNABLE to Connect");
      }
     catch (UnknownHostException e)
      {
        /*
         * The IP address of a host could not be determined.
         */
        logSocketDebug("UNKNOWN Host: Name \"" + connectToHostName +
           "\"  IPAddr \"" + connectToHostIPAddress +
           "\"  Port: " + connectToHostPortNumber + " -- UNABLE to Connect");
      }
      catch (IOException ioe)
      {
          logSocketDebug("IOException: " + ioe.toString() + " -- UNABLE to Connect");
        // Assign to our exception member variable
      }
    }
    else       // Listen/SingleListen/Server Socket
    {
      if (socketType == SOCKET_TYPE_LISTEN)
      {
        logAllDebug("Creating Listen/ServerSocket at Port: " + connectToHostPortNumber);
      }
      else
      {
        logAllDebug("Creating Single Listen/ServerSocket at Port: " + connectToHostPortNumber);
      }
      try
      {
        // Create Server Socket on a Port - BLOCKING I/O!
        serverSock = new ServerSocket(connectToHostPortNumber);
        //
        // If ServerSocket constructor returned without error,
        // then connection is Listening.
        //
        logAllDebug("Listen/ServerSocket Created OK -- Name \"" + connectToHostName +
           "\"  IPAddr \"" + connectToHostIPAddress +
           "\"  Port: " + connectToHostPortNumber);
        connectedServerSocket = serverSock;
        //
        // Now, we must Accept a Client connection - BLOCKING I/O!
        //
      }
      catch (BindException e)
      {
        /*
         * Typically, the port is in use, or the requested local address could
         * not be assigned.
         */
        logError("Port: " + connectToHostPortNumber + " Is Already In Use -- UNABLE to Listen");
      }
      catch (IOException ioe)
      {
          logSocketDebug("IOException: " + ioe.toString() + " -- UNABLE to Listen");
        // Assign to our exception member variable
      }
    }
  }

  /*------------------------------------------------------------------------*/
  /*------------------------------------------------------------------------*/
  @Override
  protected void acceptConnection()
  {
    // Socket used for establishing a connection
    Socket clientSock = null;
    logAllDebug("Listen/ServerSocket WAITING to Accept Connection -- Name \"" + connectToHostName 
        + "\"  IPAddr \"" + connectToHostIPAddress
        + "\"  Port: " + connectToHostPortNumber);
    try
    {
      //
      // Now, we must BLOCK/WAIT to Accept a Client connection - BLOCKING I/O!
      //
      clientSock = connectedServerSocket.accept();
      //
      // If Accept returned without error, then we accepted Client Socket
      // connection.
      //
      inetAddress = clientSock.getInetAddress(); // Convert host into an InetAddress.
      hostName = inetAddress.getHostName();
      hostIPAddress = inetAddress.getHostAddress();
      hostPortNumber = clientSock.getPort();
      logAllDebug("Accepted Connection from Host: Name \"" + hostName
          + "\"  IPAddr \"" + hostIPAddress 
          + "\"  Port: " + hostPortNumber);
      connectedClientSocket = clientSock;
      synchronized (this)
      {
        notifyAll();
      }
    }
    catch (IOException ioe)
    {
      /*
       * Typically, the listen socket was blocking/waiting to accept a
       * connection when we closed the socket (during shutdown).
       */
      logSocketDebug("Waiting to Accept Connection -- IOException: " + ioe.toString());
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  @Override
  protected void attachIOStreams()
  {
    try
    {
      setInputStream(new BufferedInputStream(connectedClientSocket.getInputStream()));
      logSocketDebug("attachIOStreams() -- inputStream Connected");
    }
    catch (IOException ioe)
    {
      logSocketDebug("getInputStream() -- IOException: " + ioe.toString());
    }
    try
    {
      setOutputStream(new BufferedOutputStream(connectedClientSocket.getOutputStream()));
      logSocketDebug("attachIOStreams() -- outputStream Connected");
    }
    catch (IOException ioe)
    {
      logSocketDebug("getOutputStream() -- IOException: " + ioe.toString());
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  @Override
  protected void closeComPort()
  {
    if (retryTimer != null)
    {
      retryTimer.cancel();
      retryTimer = null;
    }
    if (connectedClientSocket != null)
    {
      logSocketDebug("closeComPort() -- Closing connectedClientSocket");
      try
      {
        connectedClientSocket.close();
      }
      catch (IOException ioe)
      {
        logSocketDebug("closeComPort() -- IOException: " + ioe.toString());
      }
      connectedClientSocket = null;
    }
    if (connectedServerSocket != null)
    {
      logSocketDebug("closeComPort() -- Closing ServerSocket (may be Waiting to Connect)");
      try
      {
        connectedServerSocket.close();
        logSocketDebug("closeComPort() -- Closed ServerSocket");
      }
      catch (IOException ioe)
      {
        /*
         * We can get an IOException if we close the ServerSocket while it is
         * still waiting to accept a connection (BLOCKING I/O!)...?
         */
        logSocketDebug("closeComPort() -- IOException: " + ioe.toString());
      }
    connectedServerSocket = null;
    }
    super.closeComPort();
  }

  /*--------------------------------------------------------------------------*/
  /**
   * If this is a Connect/ClientSocket we are now CONNECTED.
   * If this is a Listen/ServerSocket we need to ACCEPT a
   * connection (and BLOCK I/O while we do it).
   */
  @Override
  protected void updateCreatedState()
  {
    if (socketType == SOCKET_TYPE_CONNECT)
    {
      comPortState = COM_PORT_STATE_CONNECTED;
    }
    else
    {
      comPortState = COM_PORT_STATE_CONNECT;
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
   *  Is our Socket created?
   */
  @Override
  protected boolean isCreated()
  {
    if (socketType == SOCKET_TYPE_CONNECT)
    {
      return (connectedClientSocket != null);
    }
    else
    {
      return (connectedServerSocket != null);
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
   *  Are we connected?
   */
  @Override
  protected boolean isConnected()
  {
    return (connectedClientSocket != null);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void startRetryTimer()
  {
    if ((socketType == SOCKET_TYPE_CONNECT) && (!justCreated))
    {
      //
      // If an attempt to Connect to a Listening Socket fails just retry
      // after a timeout.  Only log the first time, to avoid endless
      // repeated logs (but send a log out every 5 minutes to show we're
      // still working on it).
      //
      if (retryTotalTime < (retryInterval*3))
      {
        logSocketDebug("Starting " + retryInterval + "ms Connection Retry Timer -- "
            + "Connect Socket Unable to Connect to Host: Name \"" + connectToHostName 
            + "\"  IPAddr \"" + connectToHostIPAddress
            + "\"  Port: " + connectToHostPortNumber);
      }
      else
      {
        if (retryTotalTime >= 5*60*1000)
        {
          //
          // Send a log update out every 5 minutes
          //
          logSocketDebug("Continuing " + retryInterval + "ms Connection Retries -- " 
              + "Connect Socket STILL Unable to Connect to to Host: Name \"" + connectToHostName 
              + "\"  IPAddr \"" + connectToHostIPAddress
              + "\"  Port: " + connectToHostPortNumber);
          retryTotalTime = 0;
        }
      }
    }
    retryTotalTime += retryInterval;
    justCreated = false;
    if (retryTimer == null)
    {
      retryTimer = new Timer("SocketComPort-" + logger.getLoggerInstanceName());
      retryTimerTask = new RetryTimerTask();
      retryTimer.schedule(retryTimerTask, retryInterval);
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  class RetryTimerTask extends TimerTask
  {
    /**
     * run -- the LocalTimerTask's run() needs to be synchronized on thisComPort
     * so that any work we do here is not interrupted by any incoming messages
     * or events that we generate here.  We want to complete anything we do here
     * without being pre-empted.
     */
    @Override
    public void run()
    {
      synchronized(thisComPort)
      {
        closeComPort();
        notifyControlThread();
      }
    }
  }
  
  /*========================================================================*/
  /*  Logging convenience methods                                           */
  /*========================================================================*/
  
  /**
   * Log a socket error in the error log and the equipment logs
   * @param s
   */
  protected void logSocketError(String s)
  {
    logError("  # SOCKET #  " + s);
  }

  /**
   * Log a socket debug message
   * @param s
   */
  protected void logSocketDebug(String s)
  {
    if (logger != null)
      logger.logDebug("  # SOCKET #  " + s);
  }
  
  /**
   * Log a socket debug message
   * @param s
   */
  protected void logAllDebug(String s)
  {
    logSocketDebug(s);
    logTxDebug(s);
  }
}