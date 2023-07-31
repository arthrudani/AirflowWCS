package com.daifukuoc.wrxj.custom.ebs.dataserver;

import java.util.Scanner;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.data.HostConfigData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.HostOutDelegate;
import com.daifukuamerica.wrxj.host.HostServerDelegate;
import com.daifukuamerica.wrxj.host.messages.MessageHelper;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.PlcToWrxData;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;
/**
 * EBSPlcPortServer class is used to preprocess the received message.
 * @author Administrator
 *
 */
public class EBSPlcPortServer extends EBSHostServer {
	/**
	 * Method processes messages that have a specially defined Header element in it.
	 * 
	 * @param isMessage    the data read off the socket minus the STX and ETX
	 *                     characters.
	 * @param ipPlcToWrxInData data class containing message content.
	 * @return -1 if unsuccessful, 0 if successful
	 */
	public int preprocessMessage(String isMessage, PlcToWrxData ipPlcToWrxInData) {
		int vnRtn = 0;

		switch (Application.getInt(HostConfigData.ACTIVE_DATA_TYPE, 0)) {
		case DBConstants.XML:
			preProcessXMLMessage(isMessage, ipPlcToWrxInData);
			break;

		case DBConstants.DELIMITED:
			preProcessDelimitedMessage(isMessage, ipPlcToWrxInData);
			break;

		case DBConstants.FIXEDLENGTH:
			preProcessFixedLengthMessage(isMessage, ipPlcToWrxInData);
			break;

		default:
			vnRtn = -1;
		}

		return (vnRtn);
	}

	protected int preProcessXMLMessage(String isMessage, PlcToWrxData ipPlcToWrxInData) {
		int vnRtn = 0;

		Scanner vpContentScanner = new Scanner(isMessage);
		vpContentScanner.useDelimiter("\\[|\\]");
		boolean vzHeader = true;
		while (vpContentScanner.hasNext()) {
			String vsContent = vpContentScanner.next();
			if (vzHeader) {
				vzHeader = false;
				String[] vpHeaderArray = vsContent.split(";");
				if (vpHeaderArray != null && vpHeaderArray.length == NUMBER_HEADER_ELEMENTS) {
					ipPlcToWrxInData.setOriginalMessageSequence(Integer.parseInt(vpHeaderArray[0]));
					ipPlcToWrxInData.setMessageIdentifier(vpHeaderArray[1]);
				} else {
					logError("Missing header info... Message: " + isMessage);
					vnRtn = -1;
				}
			} else {
				ipPlcToWrxInData.setMessageBytes(vsContent.getBytes());
			}
		}

		return (vnRtn);
	}

	/**
	 * This method is used to pre process the received message and push the data to the PlcToWrxData object
	 * @param isMessage
	 * @param ipPlcToWrxInData
	 * @return
	 */
	private int preProcessDelimitedMessage(String isMessage, PlcToWrxData ipPlcToWrxInData) {
		int vnRtn = 0;

		Scanner vpContentScanner = new Scanner(isMessage);
		vpContentScanner.useDelimiter(PLCConstants.PLC_MESSAGE_DELIM);

		if (vpContentScanner.hasNext()) {
			// The first token should be the Message Identifier.
			String vsMessageIdentifier = vpContentScanner.next();
			ipPlcToWrxInData.setMessageIdentifier(vsMessageIdentifier);

			// The next token should be the 
			if (vpContentScanner.hasNext() && vsMessageIdentifier!=null) {
				// Whatever is leftover is the actual data content.
				// Figure out offset of this content and save it.
				int vnDataContentOffset =  vsMessageIdentifier.length() + 1;
				String vsDataContent = isMessage.substring(vnDataContentOffset);
				ipPlcToWrxInData.setMessageBytes(vsDataContent.getBytes());
				ipPlcToWrxInData.setMessage(vsDataContent);
			} else
				vnRtn = -1;
		} else {
			vnRtn = -1;
		}

		return (vnRtn);
	}

	/**
	 * Method processes messages that have no formal header element in them. This is
	 * assumed to be a <b>non-XML</b> message.
	 *
	 * @param isMessage        the data read off the socket minus the STX and ETX
	 *                         characters. The message is assumed to be of the form:
	 *                         <p>
	 *                         <b>iMessageSequence+sMessageIdentifier+Message
	 *                         Data</b>
	 *                         </p>
	 *                         <p>
	 *                         The iMessageSequence field is left justified and
	 *                         blank padded if necessary to 8 character length.
	 *                         <i>If the host chooses, it may also zero-prefill the
	 *                         sequence number to 8 chars. This code works either
	 *                         way.</i>
	 *                         </p>
	 *                         <p>
	 *                         The Message Identifier is assumed to be up to 30
	 *                         chars. blank padded if necessary.
	 *                         </p>
	 * @param ipPlcToWrxInData data class containing message content.
	 * @return 0 if sucessful, -1 otherwise.
	 */
	protected int preProcessFixedLengthMessage(String isMessage, PlcToWrxData ipPlcToWrxInData) {
		int vnRtn = 0;
		try {
			// The first token should be the sequence number.
			String vsMessageSequence;
			vsMessageSequence = isMessage.substring(0, MessageHelper.MESSAGE_SEQUENCE_LENGTH);
			ipPlcToWrxInData.setOriginalMessageSequence(Integer.parseInt(vsMessageSequence.trim()));

			// The next token should be the Message Identifier.
			String vsMessageIdentifier;
			vsMessageIdentifier = isMessage.substring(MessageHelper.MESSAGE_SEQUENCE_LENGTH,
					MessageHelper.MESSAGE_SEQUENCE_LENGTH + MessageHelper.MESSAGE_IDENTIFIER_LENGTH);
			ipPlcToWrxInData.setMessageIdentifier(vsMessageIdentifier.trim());

			// Whatever is leftover is the actual data content.
			// Figure out offset of this content and save it.
			String vsDataContent;
			vsDataContent = isMessage
					.substring(MessageHelper.MESSAGE_SEQUENCE_LENGTH + MessageHelper.MESSAGE_IDENTIFIER_LENGTH);
			ipPlcToWrxInData.setMessageBytes(vsDataContent.getBytes());
		} catch (IndexOutOfBoundsException iob) {
			vnRtn = -1;
			logError("Missing properly formatted Sequence # or Message " + "Identifier for message: " + isMessage);
		} catch (Exception ex) {
			vnRtn = -1;
			logException(ex, "Inside TCPClientTransport-->preProcessFixedLengthMessage");
		}

		return (vnRtn);
	}
	
	@Override
	public void addToDataQueue(HostServerDelegate hostDelegate)
		      throws DBException
	  {
	    if (!hostDelegate.isInfoUnderstood())
	    { // Delegate doesn't have enough information.
	      throw new DBException(
	          "Unspecified information for operation.... Request denied!");
	    }

	    TransactionToken ttok = null;
	    try
	    {
	      ttok = startTransaction();
	      hostDelegate.addToDataQueue();
	      commitTransaction(ttok);
	      // If it's an outbound message being
	      // added to the queue, send host controller
	      // wakeup event so that it will send the data.
	      if (hostDelegate instanceof HostOutDelegate && getSystemGateway() != null)
	      {
	    	  notifyWaitingOutboundMsg();
	      }
	    }
	    catch (DBException e)
	    {
	      logException(e, "Inside StandardHostServer-->addToDataQueue");
	      throw e;
	    }
	    finally
	    {
	      endTransaction(ttok);
	    }
	  }
	
	  public void notifyWaitingOutboundMsg()
	  {
	    getSystemGateway().publishHostMesgSendEvent("MessageSend", 0);
	  }
	  
	  /**
	   * Convenience method to write error message to the host system.
	   *
	   * @param errorCode the error code indicating why a message failed to be
	   *            integrated into WRx-J.
	   * @param iOriginalSequence the original message sequence number from the
	   *            host.
	   * @param sHostName the host name of the host for which this message is
	   *            intended.
	   * @param errorMessage an user defined error message.
	   * @throws DBException when there are database write or access errors.
	   * @see com.daifukuamerica.wrxj.host.messages.HostError#setErrorCode
	   *      setErrorCode
	   * @see com.daifukuamerica.wrxj.host.messages.HostError#setErrorMessage
	   *      setErrorMessage
	   */
	  public void writeRecvMsgError(int errorCode, int iOriginalSequence,
	      String sHostName, String errorMessage) throws DBException
	  {
	    writeRecvMsgError(errorCode, iOriginalSequence, sHostName, errorMessage, null);
	  }
	  
	  public void writeRecvMsgError(int errorCode, int iOriginalSequence,
		      String sHostName, String errorMessage, String[] errorFields)
		      throws DBException
		  {
		    initHostError();
		    if (sHostName != null && sHostName.trim().length() != 0)
		      mpHostError.assignHost(sHostName);

		    mpHostError.setErrorCode(errorCode);
		    mpHostError.setOriginalMessageSequence(iOriginalSequence);
		    mpHostError.setErrorMessage(errorMessage, errorFields);
		    mpHostError.format();
		    addToDataQueue(Factory.create(HostOutDelegate.class, mpHostError));
		  }
}
