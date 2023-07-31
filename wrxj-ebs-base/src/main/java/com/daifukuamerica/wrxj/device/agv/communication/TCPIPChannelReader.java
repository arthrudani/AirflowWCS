package com.daifukuamerica.wrxj.device.agv.communication;

import com.daifukuamerica.wrxj.device.agv.AGVLogger;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * Channel Reader thread.  This class reads messages off a socket channel.
 * <b>A communcation channel should already be established before starting this
 * thread.</b>
 * 
 * @author A.D.
 * @since  17-Jan-2006
 */
public class TCPIPChannelReader extends Thread
{
  private final int          STOP_RUNAWAY_COUNT = 5;
  private String             msThreadErrorInfo = "";
  private int                mnKillCounter = 0;
  private AGVLogger          mpLogger;
  private volatile boolean   mzThreadStop = false;
  private ByteBuffer         mpReadBuf = ByteBuffer.allocateDirect(512);
  private SocketChannel      mpCommChannel;
  private volatile TCPEventListener mpTCPEventListener;
  
  public TCPIPChannelReader(String isName)
  {
    super(isName);
  }

  public String getThreadErrorInfo()
  {
    return(msThreadErrorInfo);
  }

  public void setLogger(AGVLogger ipLogger)
  {
    mpLogger = ipLogger;
  }

 /**
  * Set up to propagate TCP receive data events.
  * @param ipEvent
  */
  public void setTCPEventListener(TCPEventListener ipEvent)
  {
    mpTCPEventListener = ipEvent;
  }

 /**
  * Message assigning a communcation channel.
  * @param ipCommChannel the current channel from which to read data.
  */
  public void setCommuncationChannel(SocketChannel ipCommChannel)
  {
    mpCommChannel = ipCommChannel;
  }
  
  @Override
  public void run()
  {
    StringBuffer vpAccumulationBuf = new StringBuffer(512);
    Selector vpSelector = null;
    try
    {
      mpCommChannel.configureBlocking(false);
      vpSelector = Selector.open();    // Create the Selector.

                                       // Register the channel with the selector
                                       // for read operations.
      mpCommChannel.register(vpSelector, SelectionKey.OP_READ);
    }
    catch(IOException e)
    {
      msThreadErrorInfo = "Selector error in reader thread... " +
                          "Reader Thread terminating.";
      return;
    }

    while(!mzThreadStop)
    {
      try
      {
        vpSelector.select();
      }
      catch(Exception exc)
      {
        // Get here if selecter is interrupted.
      }

      if (mzThreadStop || mpCommChannel.socket().isClosed())
      {
        try { vpSelector.close(); } catch(Exception ioe) {}
        continue;
      }

      Set vpSelectedKeySet = vpSelector.selectedKeys();
      for(Iterator vpIter = vpSelectedKeySet.iterator(); vpIter.hasNext();)
      {
        SelectionKey vpSelectedKey = (SelectionKey)vpIter.next();
        vpIter.remove();

        if (vpSelectedKey.isValid() && vpSelectedKey.isReadable())
        {
          SocketChannel vpSockChannel = (SocketChannel)vpSelectedKey.channel();
          try
          {
            if (vpSockChannel.read(mpReadBuf) != -1)
              processChannelData(mpReadBuf, vpAccumulationBuf);
            else
              mzThreadStop = true;
          }
          catch(Exception e)
          {
            mzThreadStop = true;
            msThreadErrorInfo = "Channel read error..." + e.getMessage();
            mpLogger.logErrorMessage("Connection dropped due to exception! " +
                                     e.getMessage());
          }
          finally
          {
            if (mzThreadStop)
            {
              try 
              {
                mpCommChannel.close();
                vpSelector.close(); 
              } catch(Exception ioe) {}
            }
          }
        }
      }
    }
  }

  public void stopThread()
  {
    mzThreadStop = true;
  }

 /**
  * Since TCP does not guarantee full packet delivery at the time of a read, this
  * method makes sure enough bytes are gathered off a channel to form a complete message.
  * @param ipReadBuf the primary buffer channel is flushed into.
  * @param ipAccumulationBuf accumulation buffer used to forma a complete message.
  */
  private void processChannelData(ByteBuffer ipReadBuf, StringBuffer ipAccumulationBuf)
          throws IOException
  {
    ipReadBuf.flip();
    byte vbByte = 0;
    mnKillCounter++;
    
    if (mnKillCounter > STOP_RUNAWAY_COUNT && !ipReadBuf.hasRemaining())
    {
      mzThreadStop = true;
      return;
    }
    
    while(ipReadBuf.hasRemaining())
    {
      vbByte = ipReadBuf.get();
      ipAccumulationBuf.append((char)vbByte);
      mnKillCounter = 0;
      if (vbByte == TCPIPClientComms.MESG_ETX)
      {
        String vsMessage = ipAccumulationBuf.toString();
        Scanner vpScanner = new Scanner(vsMessage);
        vpScanner.useDelimiter("\\x0" + TCPIPClientComms.MESG_STX + "|" +
                               "\\x0" + TCPIPClientComms.MESG_ETX);
        String vsStrippedStxEtx = vpScanner.next();
        vpScanner.close();
        mpLogger.logCommMessage(AGVLogger.INBOUND_MESG, vsStrippedStxEtx);
        
        if (mpTCPEventListener != null)
          mpTCPEventListener.receivedData(vsStrippedStxEtx);
        else
          mpLogger.logErrorMessage("No callback method registered. " +
                                   "Discarding message " + vsStrippedStxEtx);
        ipAccumulationBuf.delete(0, ipAccumulationBuf.length());
        ipAccumulationBuf.trimToSize();
      }
    }

    ipReadBuf.clear();
  }
}  // End TCPIPChannelReader Class.
