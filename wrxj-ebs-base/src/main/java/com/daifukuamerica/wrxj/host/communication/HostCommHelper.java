package com.daifukuamerica.wrxj.host.communication;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dataserver.standard.StandardHostServer;
import com.daifukuamerica.wrxj.dbadapter.data.WrxToHostData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.HostCommException;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Helper methods for the Host Comm. package.
 *
 * @author A.D.
 * @since  16-Oct-2007
 */
public class HostCommHelper 
{
  private static Logger mpErrorLog; 
  private static FileWriter mpFileWriter;
  private static boolean _LOGMSG;
  private static StandardHostServer mpHostServ;
  private static Date mpLogDate;
  private static SimpleDateFormat mpDateFmt;
  
  private HostCommHelper()
  {
  }
  
  public static void init()
  {
    mpErrorLog = Logger.getLogger();
    _LOGMSG = Application.getBoolean("HostLog", false);
    String vsLogFile = Application.getString(LogConsts.BaseLogPath)
        + LogConsts.LOG_PATH + File.separator + "TCPHost.log";
    if (_LOGMSG)
    {
      try { mpFileWriter = new FileWriter(vsLogFile); }
      catch(IOException ioe) { mpErrorLog.logError("Error opening Host Comm. log file..." + ioe.getMessage()); }
      mpLogDate = new Date();
      mpDateFmt = new SimpleDateFormat(SKDCConstants.DateFormatString);
    }
    mpHostServ = Factory.create(StandardHostServer.class);
  }
  
 /**
  * Method to log host comm.
  * @param isDirection String indicating message direction.
  * @param isMessage String containing messsage to log.
  */
  public static void logMessage(String isDirection, String isMessage)
  {
    if (_LOGMSG)
    {
      mpLogDate.setTime(System.currentTimeMillis());
      ReentrantLock vpLock = new ReentrantLock();
      try
      {
        vpLock.lock();
        mpFileWriter.write(mpDateFmt.format(mpLogDate));
        mpFileWriter.write("  " + isDirection + ":  ");
        mpFileWriter.write(isMessage);
        mpFileWriter.write(SKDCConstants.EOL_CHAR);
        mpFileWriter.flush();
      }
      catch(IOException ioe)
      {
        mpErrorLog.logException("Error writing to communications log...", ioe);
      }
      finally
      {
        vpLock.unlock();
      }
    }
  }
  
 /**
  * Convenience method to log host comm.
  * @param isDirection String indicating message direction.
  * @param ipMessage Byte Buffer containing messsage to log.
  */
  public static void logMessage(String isDirection, ByteBuffer ipMessage)
  {
    ipMessage.flip();
    CharBuffer vpCharBuf = Charset.forName("US-ASCII").decode(ipMessage);
    logMessage(isDirection, vpCharBuf.toString());
  }

  public static int sendDataToSocket(SocketChannel ipSocketChannel, WrxToHostData ipData)
         throws HostCommException
  {
    if (ipSocketChannel == null) return(0);

    byte[] vpMessageBytes = ipData.getMessageBytes();
    int vnMessageSize = 0;

    if (vpMessageBytes != null)
      vnMessageSize = ipData.getMessageBytes().length;

    ByteBuffer vpOutMessage = ByteBuffer.allocate(vnMessageSize+4);
    vpOutMessage.put((byte)SKDCConstants.MESG_STX);
                                       // Message content.
    if (vpMessageBytes != null) vpOutMessage.put(vpMessageBytes);

    vpOutMessage.put((byte)SKDCConstants.MESG_ETX);
    vpOutMessage.flip();

    int vnSentBytes = 0;
    try
    {
      vnSentBytes = ipSocketChannel.write(vpOutMessage);
      HostCommHelper.logMessage("Warehouse Rx --> Host", vpOutMessage);
    }
    catch(IOException ioe)
    {
      throw new HostCommException("Sending message to host...", ioe);
    }

    return(vnSentBytes);
  }
  
  public static boolean isSocketAlive(SocketChannel ipSocketChannel)
  {
    boolean vzRtn = false;
    
    try
    {
      WrxToHostData vpWrxToHostData = mpHostServ.createHeartBeatMessageContent();
      if (vpWrxToHostData != null)
        vzRtn = (sendDataToSocket(ipSocketChannel, vpWrxToHostData) != 0);
    }
    catch(HostCommException he)
    {
      vzRtn = false;
    }
    
    return(vzRtn);
  }
}
