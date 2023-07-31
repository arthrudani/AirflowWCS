package com.daifukuamerica.wrxj.host.communication;

import com.daifukuamerica.TCPIPLogger;
import com.daifukuamerica.TCPIPReadEvent;
import com.daifukuamerica.impl.TCPIPConstants;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.dataserver.standard.StandardHostServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.HostInDelegate;
import com.daifukuamerica.wrxj.host.messages.HostError;
import com.daifukuamerica.wrxj.host.messages.MessageHelper;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Read Event Handler.
 *
 * @author A.D.
 * @since  15-Jun-2013
 */
public class TCPIPReadEventImpl implements TCPIPReadEvent
{
  protected boolean     mzUseAcks = false;
  protected String      msHostName;
  protected String      msHBString;
  protected String      msCollaborator = "";
  protected TCPIPLogger mpReadLogger = null;
  protected Controller  mpSystemGateway;
  private Pattern       mpAckNakPattern = Pattern.compile("[\\cF|\\cU]+");

  public TCPIPReadEventImpl(String isHostName, String isCommGroup, TCPIPLogger ipLogger)
  {
    msHostName = isHostName;
    mpReadLogger = ipLogger;
    msHBString = Application.getString(Application.HOSTCFG_DOMAIN + isCommGroup +
                                       "." + TCPIPConstants.HEART_BEAT_MSG);
  }

  public void useAckNak(boolean izUseAcks)
  {
    mzUseAcks = izUseAcks;
  }

  public void setupCollaboratorNotification(Controller ipJMSGateway, String isCollaborator)
  {
    msCollaborator = isCollaborator;
    mpSystemGateway = ipJMSGateway;
  }

  /**
   * Handler for the received data from the host.
   *
   * @param ipCommChannel the communication channel.
   * @param isReceivedData the received message sans STX/ETX characters.
   */
  @Override
  public void receivedData(SocketChannel ipCommChannel, String isReceivedData)
  {
    Matcher vpAckNakMatcher = mpAckNakPattern.matcher(isReceivedData);
    if (vpAckNakMatcher.find())
    {
      mpReadLogger.logCommMessage(TCPIPLogger.MESG_DIR_RECV, isReceivedData);
      return;
    }
    else if (isReceivedData.contains(msHBString) || isReceivedData.isEmpty())
    {
      return;
    }

    HostToWrxData vpHostInData = Factory.create(HostToWrxData.class);
    HostInDelegate vpHostInDelegate = Factory.create(HostInDelegate.class);
    StandardHostServer vpHostServer = Factory.create(StandardHostServer.class, "TCPIPReadEventImpl");

    vpHostInData.setHostName(msHostName);

    if (vpHostServer.preprocessMessage(isReceivedData, vpHostInData) == -1)
    {
      mpReadLogger.logErrorMessage("Incorrect message format received from the host...");
      sendAckNak(ipCommChannel, vpHostInData.getOriginalMessageSequence(), SKDCConstants.MESG_NAK);
      return;
    }
    else
    {
      sendAckNak(ipCommChannel, vpHostInData.getOriginalMessageSequence(), SKDCConstants.MESG_ACK);
    }

    DBObject vpDBObj = new DBObjectTL().getDBObject();
    TransactionToken vpTranTok = null;
    try
    {                                  // Add the data to the HostToWrx table.
      vpTranTok = vpDBObj.startTransaction();
      vpHostInDelegate.setInfo(vpHostInData);
      vpHostServer.addToDataQueue(vpHostInDelegate);
      vpDBObj.commitTransaction(vpTranTok);

                                       // Let Collaborators know of waiting
                                       // message.
      mpSystemGateway.publishHostMesgReceiveEvent("", 0, msCollaborator);
    }
    catch(DBException e)
    {
      if (e.getErrorCode() == HostError.DATA_QUEUE_FULL)
      {
        vpDBObj.endTransaction(vpTranTok);
                                       // We know of these types of errors immediately
                                       // so let Host know about it now.
        try
        {
          vpHostServer.writeHostError(HostError.DATA_QUEUE_FULL, vpHostInData.getOriginalMessageSequence(),
                                       msHostName, e.getMessage() +
                                      "Please clear Warehouse Rx data queue of " +
                                      "all processed messages before attempting to " +
                                      "send more messages!");
        }
        catch(DBException exc)
        {
          mpReadLogger.logErrorMessage("Writing message to host!", exc);
        }
      }
      else
      {
        mpReadLogger.logErrorMessage("Inside TCPIPReadEventImpl-->receivedData", e);
      }
    }
    finally
    {
      vpDBObj.endTransaction(vpTranTok);
    }
  }

  protected void sendAckNak(SocketChannel ipChannel, int inOrigSeqNum,
                            int inAckNak)
  {
    if (mzUseAcks)
    {
      try
      {
        // <STX> + Original Seq# + <ACK/NAK> + <ETX>
        String vsOrigSeq = SKDCUtility.preZeroFill(inOrigSeqNum, MessageHelper.MESSAGE_SEQUENCE_LENGTH);

        byte[] vpContent = vsOrigSeq.getBytes();
        ByteBuffer vpOutMessage = ByteBuffer.allocateDirect(vpContent.length+3);
        vpOutMessage.put((byte)SKDCConstants.MESG_STX);
        vpOutMessage.put(vpContent);
        vpOutMessage.put((byte)inAckNak);
        vpOutMessage.put((byte)SKDCConstants.MESG_ETX);
        vpOutMessage.flip();

        ipChannel.write(vpOutMessage);

        vpOutMessage.flip();
        CharBuffer vpCharBuf = Charset.forName("US-ASCII").decode(vpOutMessage);

        mpReadLogger.logCommMessage(TCPIPLogger.MESG_DIR_SEND, vpCharBuf.toString());
      }
      catch(IOException ioe)
      {
        mpReadLogger.logErrorMessage("Error writing ACK/NAK back to host.");
      }
    }
  }
}
