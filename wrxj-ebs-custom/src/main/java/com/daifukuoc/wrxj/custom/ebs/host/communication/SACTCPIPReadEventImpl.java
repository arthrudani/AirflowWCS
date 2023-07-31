package com.daifukuoc.wrxj.custom.ebs.host.communication;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import com.daifukuamerica.TCPIPLogger;
import com.daifukuamerica.TCPIPReadEvent;
import com.daifukuamerica.impl.TCPIPConstants;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.dataserver.standard.StandardHostServer;
import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.HostInDelegate;
import com.daifukuamerica.wrxj.host.HostOutDelegate;
import com.daifukuamerica.wrxj.host.messages.HostError;
import com.daifukuamerica.wrxj.host.messages.MessageHelper;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
/**
 * Read Event Handler.
 *
 * @author KR
 * @since  5-May-2022
 */

public class SACTCPIPReadEventImpl implements TCPIPReadEvent
{
    private static final int SEQUENCE_POSITION_IN_HEADER = 2;
    private static final int MESSAGE_TYPE_POSITION_IN_HEADER = 3;

    public static final String STANDARD_HOST_SERVER_KEY_NAME = "SACTCPIPReadEventImpl";
	  
    protected boolean     mzUseAcks = false;
	  protected String      msHostName;
	  protected String      msHBString;
	  protected String      msCollaborator = "";
	  protected TCPIPLogger mpReadLogger = null;
	  protected Controller  mpSystemGateway;
	//  protected MessageProcessor mpMsgProcessorImp  ;

	  public SACTCPIPReadEventImpl(String isHostName, String isCommGroup, TCPIPLogger ipLogger)
	  {
	    msHostName = isHostName;
	    mpReadLogger = ipLogger;
	    msHBString = Application.getString(Application.HOSTCFG_DOMAIN + isCommGroup +
	                                       "." + TCPIPConstants.HEART_BEAT_MSG);
	   //this will create a class if it is defined in factory.properties otherwise creates instance of the ConveyorProcessorImp
	   // mpMsgProcessorImp = Factory.create(ConveyorProcessorImp.class, ipLogger);
	 
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
	   * This is called from SACTCPIPReaderWriter.processChannelData method after processing the Received Data from PORT (SAC)
	   *
	   * @param ipCommChannel the communication channel.
	   * @param isReceivedData the received message it is a comma separated message.
	   */
	  @Override
	  public void receivedData(SocketChannel ipCommChannel, String isReceivedData)
	  {
		  //Validation: as expected a comma separated data
		 if(!isReceivedData.contains(",") )
		 {
			 mpReadLogger.logCommMessage(TCPIPLogger.MESG_DIR_RECV, isReceivedData);
		     return;
		 }

		//To avoid processing heart beat (msgType=1 first element) Example: 1,111,444,0,0,0,16,0,1
		String[] split = isReceivedData.split(",");
		if (split != null && split.length >= MESSAGE_TYPE_POSITION_IN_HEADER) {
		    if (split[MESSAGE_TYPE_POSITION_IN_HEADER-1].equals(String.valueOf(SACControlMessage.KEEPALIVE_MSG_TYPE))) {
		        //so it is heart beat msg ...
		        return;
		    }
		}

	    HostToWrxData vpHostInData = Factory.create(HostToWrxData.class);
	    HostInDelegate vpHostInDelegate = Factory.create(HostInDelegate.class);
	    HostOutDelegate vpHostOutDelegate = Factory.create(HostOutDelegate.class);
	    StandardHostServer vpHostServer = Factory.create(StandardHostServer.class, STANDARD_HOST_SERVER_KEY_NAME);//allocate the implementation 

	    vpHostInData.setHostName(msHostName);
	    
	    //KR: check and prepare the received message before processing
	    //**** KR: TODO : IMPORTANT: need to complete isValidMessage method to process all the message in the interface **** 
	    boolean bValidMsg = isValidMessage(isReceivedData, vpHostInData);
		
	    try
	    {  // Add the data to the HostToWrx table.
	      vpHostInDelegate.setInfo(vpHostInData);
	      vpHostServer.addToDataQueue(vpHostInData.getOriginalMessageSequence() ,vpHostInDelegate);
	      if (split != null && split.length >= MESSAGE_TYPE_POSITION_IN_HEADER) {
            if (split[MESSAGE_TYPE_POSITION_IN_HEADER-1].equals(String.valueOf(SACControlMessage.STORED_COMPLETE_ACK_MSG_TYPE))) {
                vpHostOutDelegate.setInfo(vpHostInData);
                vpHostServer.markMessageAsAcked(vpHostOutDelegate);
	        }
	      }

	      //We continue with validation and after that 
	      //we process the message in the Parse methods which is called from Host Message Integrator
	      
	      if(!bValidMsg )
	      {
	    	  mpReadLogger.logErrorMessage("KR: Received invalid msg: " + isReceivedData) ;
	      }
	      //WE CONTINUE with the message processing in the Parser. eg: ExpectedRecieptParser
	      
	      // Let Collaborator (HostMessageIntegrator) know that there is a message to process. ( this will update the HostInWRX msg status to completed )
	      mpSystemGateway.publishHostMesgReceiveEvent("", 0, msCollaborator);
	      
	    }
	    catch(DBException e)
	    {
	      if (e.getErrorCode() == HostError.DATA_QUEUE_FULL)
	      {
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
	  }
	  
	  /** KR: To be deleted
	   * Sends the immediate acknowledgement after receiving a message or when an error occurs if  UseAcks = true in the HostConfig table.
	   * 
	   * @param ipChannel
	   * @param inOrigSeqNum
	   * @param inAckNak
	   */
	  protected void sendAckNak(SocketChannel ipChannel, int inOrigSeqNum,
	                            int inAckNak)
	  {
		//TODO: KR- We need to format the data based on SAC new interface before sending to host (SAC)
		  //the new interface is not required any immediate acknowledgement or response. So the mzUseAcks should be set to false all the time 
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
			
	        mpReadLogger.logCommMessage(TCPIPLogger.MESG_DIR_SEND, vpCharBuf.toString());	      }
	      catch(IOException ioe)
	      {
	        mpReadLogger.logErrorMessage("Error writing ACK/NAK back to host.");
	      }
	    }
	  }
	
	  /**
	   * Prepare and validate the message before processing based on the received message from SAC
	   * @param isMessage
	   * @param ipHostInData
	   * @return
	   */
	  private boolean isValidMessage(String isMessage, HostToWrxData ipHostInData)
	  {
		  try
		  {
			  if(!isMessage.contains(",") )
			  {
				  return false;
			  }
			  String[] splitedData = isMessage.split(",");
			  if(splitedData.length >= MESSAGE_TYPE_POSITION_IN_HEADER )
			  {
			      String sequenceNumber = splitedData[1];
			      String messageType = splitedData[2];
				  if(messageType.equals(Integer.toString( MessageType.EXPECTEDRECEIPTMESSAGE.getMessageType() )) )
				  {
					  ipHostInData.setMessageIdentifier(MessageType.EXPECTEDRECEIPTMESSAGE.name()); 
				  }
				  else if(messageType.equals(Integer.toString( MessageType.FLIGHTDATAUPDATEMESSAGE.getMessageType() )) )
                  {
                      ipHostInData.setMessageIdentifier(MessageType.FLIGHTDATAUPDATEMESSAGE.name()); 
                  }
				  else if(messageType.equals(Integer.toString(MessageType.STOREDCOMPLETEACKMESSAGE.getMessageType())))
				  {
				      ipHostInData.setMessageIdentifier(MessageType.STOREDCOMPLETEACKMESSAGE.name());   
				  }
				  else if(messageType.equals(Integer.toString(MessageType.RETRIEVALORDERMESSAGE.getMessageType()))) //RETRIEVALORDERMESSAGE save to HostToWrxData table 
				  {
					  ipHostInData.setMessageIdentifier(MessageType.RETRIEVALORDERMESSAGE.name());
				  }
				  else if(messageType.equals(Integer.toString(MessageType.RETRIEVALITEMMESSAGE.getMessageType()))) //RETRIEVALORDELISTRMESSAGE save to HostToWrxData table 
				  {
					  ipHostInData.setMessageIdentifier(MessageType.RETRIEVALITEMMESSAGE.name());
				  }
				  ipHostInData.setMessage(isMessage);
				  ipHostInData.setMessageBytes(isMessage.getBytes());
				  int squenceNo = Integer.parseInt(sequenceNumber);
				  ipHostInData.setOriginalMessageSequence(squenceNo);
				  ipHostInData.setAddMethod(this.getClass().getSimpleName());
				
			  }
		  }catch(Exception ex)
		  {
			  mpReadLogger.logErrorMessage(" preprocessMessage is failed: " + ex.getMessage());
			  return false;
		  }
		  
		  return true;
	  }
}
