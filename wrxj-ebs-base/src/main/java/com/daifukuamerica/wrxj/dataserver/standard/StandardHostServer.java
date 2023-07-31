package com.daifukuamerica.wrxj.dataserver.standard;

import com.daifukuamerica.impl.ConnectionType;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.HostConfig;
import com.daifukuamerica.wrxj.dbadapter.data.HostConfigData;
import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.WrxToHostData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.HostInDelegate;
import com.daifukuamerica.wrxj.host.HostOutDelegate;
import com.daifukuamerica.wrxj.host.HostServerDelegate;
import com.daifukuamerica.wrxj.host.messages.DeviceStatus;
import com.daifukuamerica.wrxj.host.messages.ExpectedReceiptComplete;
import com.daifukuamerica.wrxj.host.messages.HostError;
import com.daifukuamerica.wrxj.host.messages.InventoryAdjustment;
import com.daifukuamerica.wrxj.host.messages.InventoryStatus;
import com.daifukuamerica.wrxj.host.messages.LoadArrival;
import com.daifukuamerica.wrxj.host.messages.LocationArrival;
import com.daifukuamerica.wrxj.host.messages.MessageHelper;
import com.daifukuamerica.wrxj.host.messages.MessageOutFactory;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.host.messages.OrderComplete;
import com.daifukuamerica.wrxj.host.messages.OrderStatus;
import com.daifukuamerica.wrxj.host.messages.PickComplete;
import com.daifukuamerica.wrxj.host.messages.ShipComplete;
import com.daifukuamerica.wrxj.host.messages.StoreComplete;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.util.SKDCConstants;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Description:<BR>
 * Transaction Mediator to handle Host related operations. This is the main
 * interface for changing the WrxToHost and HostToWrx tables. This server deals
 * with a
 * {@link com.daifukuamerica.wrxj.host.HostServerDelegate HostServerDelegate}
 * object that should have been given enough information to carry out the
 * designated operation; if this is unknown to the delegate the operation
 * request is rejected.
 *
 * @author A.D.
 * @version 1.0 07-Feb-05
 */
public class StandardHostServer extends StandardServer
{
  /** Number of header elements to parse in TCP/IP XML messages. */
  protected static final int NUMBER_HEADER_ELEMENTS = 2;
  protected HostError mpHostError;
  protected HostConfig mpHostCfg;


  protected WrxToHostData mpWrxToHostData = null;
  protected StandardLoadServer mpLoadServ = null;
  protected StandardOrderServer mpOrderServ = null;

  public StandardHostServer()
  {
    this(null);
  }

  public StandardHostServer(String keyName)
  {
    super(keyName);
    mpHostCfg = Factory.create(HostConfig.class);
    logDebug("StandardHostServer.createHostServer()");
  }

  /**
   * Web application constructor for per user connection pooling
   * @param keyName
   * @param dbo
   */
  public StandardHostServer(String keyName, DBObject dbo)
  {
	  super(keyName, dbo);
	  mpHostCfg = Factory.create(HostConfig.class);
	  logDebug("StandardHostServer.createHostServer()");
  }


  /**
   * {@inheritDoc}
   */
  public void addToDataQueue(HostServerDelegate hostDelegate)
      throws DBException
  {
      addToDataQueue(-1, hostDelegate);
  }
  
  public void addToDataQueue(int originalMessageSequenceNumber, HostServerDelegate hostDelegate)
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
          hostDelegate.addToDataQueue(originalMessageSequenceNumber);          
          commitTransaction(ttok);
          // If it's an outbound message being
          // added to the queue, send host controller
          // wakeup event so that it will send the data.
          if (hostDelegate instanceof HostOutDelegate && getSystemGateway() != null)
          {
            notifyHostOutMesg();
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

  /**
   * {@inheritDoc}
   */
  public void deleteProcessedMessage(HostServerDelegate hostDelegate)
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
      hostDelegate.deleteProcessedMessages();
      commitTransaction(ttok);
    }
    catch (DBException e)
    {
      logException(e, "Inside StandardHostServer-->deleteProcessedMessages");
      throw e;
    }
    finally
    {
      endTransaction(ttok);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void deleteAllProcessedMessages(HostServerDelegate hostDelegate)
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
      hostDelegate.deleteProcessedMessages();
      commitTransaction(ttok);
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

  /**
   * Deletes processed host messages older than the specified date.
   *
   * @param ipHostDelegate the delegate carrying info. for the deletion.
   *
   * @throws DBException
   */
  public void deleteProcessedMessages(HostServerDelegate ipHostDelegate)
      throws DBException
  {
    if (!ipHostDelegate.isInfoUnderstood())
    { // Delegate doesn't have enough information.
      throw new DBException(
          "Unspecified information for operation.... Request denied!");
    }

    TransactionToken ttok = null;
    try
    {
      ttok = startTransaction();
      ipHostDelegate.deleteProcessedMessages();
      commitTransaction(ttok);
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

  /**
   * This method is called from any object that wants to mark a message from the
   * inbound or outbound data queue as processed. This method exists to wrap the
   * update operation in a transaction.
   *
   * @param hostDelegate an object implementing the
   *            {@link com.daifukuamerica.wrxj.host.HostServerDelegate HostServerDelegate}
   *            interface.
   * @throws DBException if there is an update error.
   */
  public void markMessageAsProcessed(HostServerDelegate hostDelegate)
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
      hostDelegate.markMessageProcessed();
      commitTransaction(ttok);
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
  
  public boolean isPendingMessageFound(HostServerDelegate hostDelegate, int ackTimeoutInSeconds) throws DBException {
      if (!hostDelegate.isInfoUnderstood())
      { // Delegate doesn't have enough information.
        throw new DBException(
            "Unspecified information for operation.... Request denied!");
      }
      if (hostDelegate instanceof HostOutDelegate == false)
      { // Delegate doesn't have enough information.
        throw new DBException(
            "Available for only host out!");
      }

      return ((HostOutDelegate)hostDelegate).isPendingMessageFound(ackTimeoutInSeconds);
  }
  
  public List<WrxToHostData> getAllPendingMessages(HostServerDelegate hostOutDelegate, int ackTimeoutInSeconds) throws DBException {
    if (hostOutDelegate instanceof HostOutDelegate) {
      return ((HostOutDelegate)hostOutDelegate).getAllPendingMessages(ackTimeoutInSeconds);
    }
  
    return List.<WrxToHostData>of();
  }

  public void markMessageAsRetransmitted(HostServerDelegate hostOutDelegate) throws DBException {

    if (hostOutDelegate instanceof HostOutDelegate) {
      TransactionToken ttok = null;
      try
      {
        ttok = startTransaction();
        ((HostOutDelegate)hostOutDelegate).markMessageAsRetransmitted();
        commitTransaction(ttok);
      }
      catch (DBException e)
      {
        logException(e, "Inside StandardHostServer-->markMessageAsRetransmitted");
        throw e;
      }
      finally
      {
        endTransaction(ttok);
      }      
    }
  }
  
  public void markMessageAsAckFailed(HostServerDelegate hostOutDelegate) throws DBException {

    if (hostOutDelegate instanceof HostOutDelegate) {
      TransactionToken ttok = null;
      try
      {
        ttok = startTransaction();
        ((HostOutDelegate)hostOutDelegate).markMessageAsAckFailed();
        commitTransaction(ttok);
      }
      catch (DBException e)
      {
        logException(e, "Inside StandardHostServer-->markMessageAsAckFailed");
        throw e;
      }
      finally
      {
        endTransaction(ttok);
      }      
    }
  }
    
  public void markMessageAsAcked(HostServerDelegate hostOutDelegate) throws DBException {

    if (hostOutDelegate instanceof HostOutDelegate) {
      TransactionToken ttok = null;
      try
      {
        ttok = startTransaction();
        ((HostOutDelegate)hostOutDelegate).markMessageAsAcked();
        commitTransaction(ttok);
      }
      catch (DBException e)
      {
        logException(e, "Inside StandardHostServer-->markMessageAsAcked");
        throw e;
      }
      finally
      {
        endTransaction(ttok);
      }      
    }
  }

  /**
   * This method is called from any object that wants to mark a message from the
   * inbound or outbound data queue as Process Error. This method exists to wrap
   * the update operation in a transaction.
   *
   * @param ipData data class containing original message to mark in error.
   * @throws DBException if there is an update error.
   */
  public void markMessageInError(AbstractSKDCData ipData) throws DBException
  {
    HostServerDelegate vpDelegate;
    if (ipData instanceof HostToWrxData)
    {
      vpDelegate = Factory.create(HostInDelegate.class);
      vpDelegate.setInfo(ipData);
      ((HostToWrxData) ipData).setMessageProcessed(DBConstants.PROC_ERROR);
    }
    else
      if (ipData instanceof WrxToHostData)
      {
        vpDelegate = Factory.create(HostOutDelegate.class);
        vpDelegate.setInfo(ipData);
        ((WrxToHostData) ipData).setMessageProcessed(DBConstants.PROC_ERROR);
      }
      else
      {
        throw new DBException(
            "Unknown data class passed to StandardHostServer.markMessageInError()");
      }
    toggleProcessedFlag(vpDelegate);
  }

  /**
   * Toggles processed/unprocessed flag of a host message.
   *
   * @param hostDelegate delegate with info. on toggling message.
   * @throws com.daifukuamerica.wrxj.jdbc.DBException if there is a modify
   *             error.
   */
  public void toggleProcessedFlag(HostServerDelegate hostDelegate)
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
      hostDelegate.toggleProcessedFlag();
      commitTransaction(ttok);
    }
    catch (DBException e)
    {
      logException(e, "Inside StandardHostServer-->toggleProcessedFlag");
      throw e;
    }
    finally
    {
      endTransaction(ttok);
    }
  }

  /**
   * Method to retrieve a List of inbound or outbound Host messages. <b>Note:</b>
   * This method is safe to use for a transporter as long as there is only one
   * transporter at work. When multiple transporters are involved, records
   * should be retrieved as needed to avoid record processing conflicts. For
   * display purposes this method suffices compared to reading each record
   * individually. Also, this method does <u>not</u> return any CLOB data.
   *
   * @param hostDelegate an object implementing the
   *            {@link com.daifukuamerica.wrxj.host.HostServerDelegate HostServerDelegate}
   *            interface.
   * @return List of inbound or outbound messages depending on the info. the
   *         delegate is carrying.
   * @throws DBException if there is a database connectivity problem.
   */
  public List<Map> getDataQueueMessages(HostServerDelegate hostDelegate)
      throws DBException
  {
    if (!hostDelegate.isInfoUnderstood())
    { // Delegate doesn't have enough information.
      throw new DBException(
          "Unspecified information for operation.... Request denied!");
    }

    return (hostDelegate.getMessages());
  }

  /**
   * Method to retrieve an inbound or outbound Host message. If the record is to
   * be read with a lock, the Delegate must have the lock flag set.
   *
   * @param hostDelegate an object implementing the
   *            {@link com.daifukuamerica.wrxj.host.HostServerDelegate HostServerDelegate}
   *            interface.
   * @return
   * @throws DBException if there is a database connectivity problem.
   * @see com.daifukuamerica.wrxj.dbadapter. AbstractSKDCData AbstractSKDCData
   */
  public AbstractSKDCData getDataQueueMessage(HostServerDelegate hostDelegate)
      throws DBException
  {
    if (!hostDelegate.isInfoUnderstood())
    { // Delegate doesn't have enough information.
      throw new DBException(
          "Unspecified information for operation.... Request denied!");
    }
    return (hostDelegate.getMessage());
  }

  /**
   * Method gets oldest message from a data queue.
   *
   * @param hostDelegate an object implementing the
   *            {@link com.daifukuamerica.wrxj.host.HostServerDelegate  HostServerDelegate}
   *            interface.
   * @return {@link com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData#AbstractSKDCData AbstractSKDCData}
   * @throws DBException if there is a database access error.
   */
  public AbstractSKDCData getOldestDataQueueMessage(
      HostServerDelegate hostDelegate) throws DBException
  {
    if (!hostDelegate.isInfoUnderstood())
    { // Delegate doesn't have enough information.
      throw new DBException(
          "Unspecified information for operation.... Request denied!");
    }
    return (hostDelegate.getOldestUnprocessedMessage());
  }

  /**
   * Method gets newest message from a data queue.
   *
   * @param hostDelegate an object implementing the
   *            {@link com.daifukuamerica.wrxj.host.HostServerDelegate HostServerDelegate}
   *            interface.
   *
   * @return {@link com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData#AbstractSKDCData AbstractSKDCData}
   * @throws DBException if there is a database access error.
   */
  public AbstractSKDCData getNewestDataQueueMessage(
      HostServerDelegate hostDelegate) throws DBException
  {
    if (!hostDelegate.isInfoUnderstood())
    { // Delegate doesn't have enough information.
      throw new DBException(
          "Unspecified information for operation.... Request denied!");
    }
    return (hostDelegate.getNewestMessage());
  }

  /**
   * Returns a String array of active Transporter names.
   * @return
   * @throws com.daifukuamerica.wrxj.jdbc.DBException
   */
  public String[] getActiveTransporters() throws DBException
  {
    return mpHostCfg.getActiveTransportChoices();
  }

  /**
   * This method is called from any object that wants to check if there is a
   * message to process in the database.
   *
   * @param hostDelegate an object implementing the
   *            {@link com.daifukuamerica.wrxj.host.HostServerDelegate HostServerDelegate}
   *            interface.
   * @return <code>true</code> if there are unprocessed messages.
   * @throws DBException if there is an update error.
   */
  public boolean unprocessedMessageAvailable(HostServerDelegate hostDelegate)
      throws DBException
  {
    if (!hostDelegate.isInfoUnderstood())
    { // Delegate doesn't have enough information.
      throw new DBException(
          "Unspecified information for operation.... Request denied!");
    }

    return (hostDelegate.unprocessedMessageAvailable());
  }

  /**
   * Method to check if a Comm. Group represents a Server Socket.
   * @param isCommGroup
   * @return <code>true</code> if Comm. Group is a Server Socket.
   * @throws DBException
   */
  public boolean isTcpServer(String isCommGroup) throws DBException
  {
    HostConfigData vpHCData = Factory.create(HostConfigData.class);
    vpHCData.setKey(HostConfigData.DATAHANDLER_NAME, isCommGroup);
    vpHCData.setKey(HostConfigData.GROUP_NAME, HostConfigData.HOST_TRANSPORT_GROUP_NAME);
    vpHCData.setKey(HostConfigData.PARAMETERNAME_NAME, "ConnType");
    vpHCData.setParameterValue("");
    List<Map> vpList = mpHostCfg.getSelectedColumnElements(vpHCData);
    String vsConnTypeVal = DBHelper.getStringField(vpList.get(0), HostConfigData.PARAMETERVALUE_NAME);

    return ConnectionType.isServer(vsConnTypeVal);
  }

  /**
   * {@inheritDoc}
   */
  public String[] getHostNames() throws DBException
  {
    return (Factory.create(HostOutDelegate.class).getHostNames());
  }

  /**
   * Gets a list of host message names.
   *
   * @param hostDelegate an object implementing the
   *            {@link com.daifukuamerica.wrxj.host.HostServerDelegate HostServerDelegate}
   *            interface.
   * @return string array of message names. Empty array if nothing is found.
   * @throws DBException for database access errors.
   */
  public String[] getMessageNames(HostServerDelegate hostDelegate)
      throws DBException
  {
    return (hostDelegate.getMessageNames());
  }

  /**
   * Tests if a message inbound or outbound.
   *
   * @param sMessageIdentifier the host message identifier.
   * @return <code>true</code> if this is a inbound message.
   */
  public boolean isInboundMessage(String sMessageIdentifier)
  {
    boolean rtn = true;
    try
    {
      String[] outboundMessageNames = getMessageNames(Factory.create(HostOutDelegate.class));
      for (String theMessage : outboundMessageNames)
      {
        if (theMessage.equalsIgnoreCase(sMessageIdentifier))
        {
          rtn = false;
          break;
        }
      }
    }
    catch (DBException e)
    {
      logError("DB Error retrieving message names... StandardHostServer-->isInboundMessage::"
          + e.getMessage());
    }

    return (rtn);
  }

  /**
   * Gets the Minimum and Maximum sequence numbers.
   *
   * @param hostDelegate delegate with search info.
   * @return an integer array with the first entry being the Min. and the second
   *         entry the Max. sequence number present in the HostToWrx or
   *         WrxToHost tables.
   * @throws com.daifukuamerica.wrxj.jdbc.DBException if there is a database
   *             access error
   */
  public int[] getMinMaxSequence(HostServerDelegate hostDelegate)
      throws DBException
  {
    if (!hostDelegate.isInfoUnderstood())
    { // Delegate doesn't have enough information.
      throw new DBException(
          "Unspecified information for operation.... Request denied!");
    }
    return (hostDelegate.getSequenceBoundaries());
  }

  /**
   * Gets the Minimum and Maximum sequence numbers.
   *
   * @param hostDelegate delegate with search info.
   * @return an integer array with the first entry being the Min. and the second
   *         entry the Max. sequence number present in the HostToWrx or
   *         WrxToHost tables.
   * @throws com.daifukuamerica.wrxj.jdbc.DBException if there is a database
   *             access error
   */
  public int[] getHostMinMaxSequence(HostServerDelegate hostDelegate)
      throws DBException
  {
    if (!hostDelegate.isInfoUnderstood())
    { // Delegate doesn't have enough information.
      throw new DBException(
          "Unspecified information for operation.... Request denied!");
    }
    return (hostDelegate.getHostSequenceBoundaries());
  }

  /*
   * ============================================================================
   * Following methods don't fit the Delegate paradigm used above since they are
   * only for Inbound messages, and the rules are different based on message
   * format.
   * ============================================================================
   */

  /**
   * Method processes messages that have a specially defined Header element in
   * it.
   * @param isMessage the data read off the socket minus the STX and ETX
   *        characters.
   * @param ipHostInData data class containing message content.
   * @return -1 if unsuccessful, 0 if sucessful
   */
  public int preprocessMessage(String isMessage, HostToWrxData ipHostInData)
  {
    int vnRtn = 0;

    switch(Application.getInt(HostConfigData.ACTIVE_DATA_TYPE, 0))
    {
      case DBConstants.XML:
        preProcessXMLMessage(isMessage, ipHostInData);
        break;

      case DBConstants.DELIMITED:
        preProcessDelimitedMessage(isMessage, ipHostInData);
        break;

      case DBConstants.FIXEDLENGTH:
        preProcessFixedLengthMessage(isMessage, ipHostInData);
        break;

      default:
        vnRtn = -1;
    }

    return(vnRtn);
  }

  /**
   * Method processes messages that have a specially defined Header element in
   * it.
   *
   * @param isMessage the data read off the socket minus the STX and ETX
   *            characters. This data will be of the following form:
   *            <b>[MessageHeader]Message data</b>
   *            <p>
   *            The Message Header will be of the exact form:
   *            <b>[iMessageSequence;sMessageIdentifier;]</b>
   *            </p>
   * @param ipHostInData data class containing message content.
   * @return -1 if unsuccessful, 0 if sucessful
   */
  protected int preProcessXMLMessage(String isMessage, HostToWrxData ipHostInData)
  {
    int vnRtn = 0;

    Scanner vpContentScanner = new Scanner(isMessage);
    vpContentScanner.useDelimiter("\\[|\\]");
    boolean vzHeader = true;
    while (vpContentScanner.hasNext())
    {
      String vsContent = vpContentScanner.next();
      if (vzHeader)
      {
        vzHeader = false;
        String[] vpHeaderArray = vsContent.split(";");
        if (vpHeaderArray != null
            && vpHeaderArray.length == NUMBER_HEADER_ELEMENTS)
        {
          ipHostInData.setOriginalMessageSequence(Integer.parseInt(vpHeaderArray[0]));
          ipHostInData.setMessageIdentifier(vpHeaderArray[1]);
        }
        else
        {
          logError("Missing header info... Message: " + isMessage);
          vnRtn = -1;
        }
      }
      else
      {
        ipHostInData.setMessageBytes(vsContent.getBytes());
      }
    }

    return (vnRtn);
  }

  /**
   * Method pre-processes messages that have no formal header element in them.
   * This is assumed to be a <b>non-XML</b> message.
   *
   * @param isMessage the data read off the socket minus the STX and ETX
   *            characters. The message is assumed to be of the form:
   *            <p>
   *            <b> iMessageSequence&lt;delimiter char&gt;sMessageIdentifier
   *            &lt;delimiter char&gt;Message Data&lt;delimiter char&gt;<b>
   *            </p>
   * @param ipHostInData data class containing message content.
   * @return 0 if sucessful, -1 otherwise.
   */
  private int preProcessDelimitedMessage(String isMessage,
      HostToWrxData ipHostInData)
  {
    int vnRtn = 0;

    Scanner vpContentScanner = new Scanner(isMessage);
    if (MessageHelper.HOST_MESSAGE_DELIM.charAt(0) == '|')
      vpContentScanner.useDelimiter("\\|");
    else
      vpContentScanner.useDelimiter(MessageHelper.HOST_MESSAGE_DELIM);

    if (vpContentScanner.hasNext())
    {
      // The first token should be the sequence number.
      String vsMessageSequence = vpContentScanner.next();
      ipHostInData.setOriginalMessageSequence(Integer.parseInt(vsMessageSequence));

      // The next token should be the Message Identifier.
      if (vpContentScanner.hasNext())
      {
        String vsMessageIdentifier = vpContentScanner.next();
        ipHostInData.setMessageIdentifier(vsMessageIdentifier);

        // Whatever is leftover is the actual data content.
        // Figure out offset of this content and save it.
        int vnDataContentOffset = vsMessageSequence.length()
            + vsMessageIdentifier.length() + 2;
        String vsDataContent = isMessage.substring(vnDataContentOffset);
        ipHostInData.setMessageBytes(vsDataContent.getBytes());
      }
      else
        vnRtn = -1;
    }
    else
    {
      vnRtn = -1;
    }

    return (vnRtn);
  }

  /**
   * Method processes messages that have no formal header element in them. This
   * is assumed to be a <b>non-XML</b> message.
   *
   * @param isMessage the data read off the socket minus the STX and ETX
   *            characters. The message is assumed to be of the form:
   *            <p>
   *            <b>iMessageSequence+sMessageIdentifier+Message Data</b>
   *            </p>
   *            <p>
   *            The iMessageSequence field is left justified and blank padded if
   *            necessary to 8 character length. <i>If the host chooses, it may
   *            also zero-prefill the sequence number to 8 chars. This code
   *            works either way.</i>
   *            </p>
   *            <p>
   *            The Message Identifier is assumed to be up to 30 chars. blank
   *            padded if necessary.
   *            </p>
   * @param ipHostInData data class containing message content.
   * @return 0 if sucessful, -1 otherwise.
   */
  protected int preProcessFixedLengthMessage(String isMessage,
      HostToWrxData ipHostInData)
  {
    int vnRtn = 0;
    try
    {
      // The first token should be the sequence number.
      String vsMessageSequence;
      vsMessageSequence = isMessage.substring(0,
          MessageHelper.MESSAGE_SEQUENCE_LENGTH);
      ipHostInData.setOriginalMessageSequence(Integer.parseInt(vsMessageSequence.trim()));

      // The next token should be the Message Identifier.
      String vsMessageIdentifier;
      vsMessageIdentifier = isMessage.substring(
          MessageHelper.MESSAGE_SEQUENCE_LENGTH,
          MessageHelper.MESSAGE_SEQUENCE_LENGTH
              + MessageHelper.MESSAGE_IDENTIFIER_LENGTH);
      ipHostInData.setMessageIdentifier(vsMessageIdentifier.trim());

      // Whatever is leftover is the actual data content.
      // Figure out offset of this content and save it.
      String vsDataContent;
      vsDataContent = isMessage.substring(MessageHelper.MESSAGE_SEQUENCE_LENGTH
          + MessageHelper.MESSAGE_IDENTIFIER_LENGTH);
      ipHostInData.setMessageBytes(vsDataContent.getBytes());
    }
    catch (IndexOutOfBoundsException iob)
    {
      vnRtn = -1;
      logError("Missing properly formatted Sequence # or Message "
          + "Identifier for message: " + isMessage);
    }
    catch (Exception ex)
    {
      vnRtn = -1;
      logException(ex,
          "Inside TCPClientTransport-->preProcessFixedLengthMessage");
    }

    return (vnRtn);
  }

  /**
   * Notifies HostController of waiting outbound message.
   */
  public void notifyHostOutMesg()
  {
    getSystemGateway().publishHostMesgSendEvent("MessageSend", 0);
  }

  /**
   * Method to create a TCP/IP acknowledge message for TCP/IP Hosts. This method
   * exists only for extensibility purposes since some customers have customised
   * Acknowledgement message content.
   * <p>
   * The content of this message for baseline is simply
   * <code>inOrigSeqNum + inAckNak</code>
   * </p>
   *
   * @param inOrigSeqNum Original sequence number.
   * @param inAckNak Positive or negative acknowledgement character.
   *
   * @return formatted string.
   */
  public String createTCPAcknowledgeContent(int inOrigSeqNum, int inAckNak)
  {
    String vsMesg = Integer.toString(SKDCConstants.MESG_STX);
    vsMesg += Integer.toString(inOrigSeqNum);
    vsMesg += Integer.toString(inAckNak);
    vsMesg += Integer.toString(SKDCConstants.MESG_ETX);

    return (vsMesg);
  }

 /**
  * Method to create TCP/IP Heartbeat message content. This method exists only
  * for extensibility purposes since some customers have customised Heartbeat
  * message content.
  *
  * @return WrxToHostData filled with minimum necessary content.
  */
  public WrxToHostData createHeartBeatMessageContent()
  {
    if (mpWrxToHostData == null)
      mpWrxToHostData = Factory.create(WrxToHostData.class);
    else
      mpWrxToHostData.clear();

    mpWrxToHostData.setMessageIdentifier("HeartBeatMessage");
    return (mpWrxToHostData);
  }

 /**
  * Method sends an Inventory Adjust message to the Host when inventory is
  * changed on the system.
  *
  * @param ipData data class holding item detail info.
  * @param isReasonCode The reason code for adjusting the item.
  * @throws DBException Exception when database access fails.
  */
  public void sendInventoryAdjust(LoadLineItemData ipData, String isReasonCode)
         throws DBException
  {
    /*
     * If the host system is not enabled or this outbound message isn't active,
     * don't try to send any messages.
     */
    if (!mzHasHostSystem ||
        Application.getInt(MessageOutNames.INVENTORY_ADJUST.getValue()) == SKDCConstants.INACTIVE_MESSAGE)
    {
      return;
    }

    InventoryAdjustment vpMesgOut = MessageOutFactory.getInstance(MessageOutNames.INVENTORY_ADJUST);
    vpMesgOut.setLoadID(ipData.getLoadID());
    vpMesgOut.setItem(ipData.getItem());
    vpMesgOut.setLot(ipData.getLot());
    vpMesgOut.setAdjustmentAmount(ipData.getCurrentQuantity());
    vpMesgOut.setReasonCode(isReasonCode);
    if (SKDCUserData.isLoggedIn())
    {
      vpMesgOut.setUserID(SKDCUserData.getLoginName());
    }
    vpMesgOut.format();
    addToDataQueue(new HostOutDelegate(vpMesgOut));
  }

 /**
  * Method sends an Inventory Status message to the Host when inventory is put
  * on Hold or released from hold.
  *
  * @param ipData data class holding item detail info.
  * @throws DBException Exception when database access fails.
  */
  public void sendInventoryStatus(LoadLineItemData ipData) throws DBException
  {
    /*
     * If the host system is not enabled or this outbound message isn't active,
     * don't try to send any messages.
     */
    if (!mzHasHostSystem ||
        Application.getInt(MessageOutNames.INVENTORY_STATUS.getValue()) == SKDCConstants.INACTIVE_MESSAGE)
    {
      return;
    }

    InventoryStatus vpMesgOut = MessageOutFactory.getInstance(MessageOutNames.INVENTORY_STATUS);
    vpMesgOut.setItem(ipData.getItem());
    vpMesgOut.setLot(ipData.getLot());
    vpMesgOut.setQuantity(ipData.getCurrentQuantity());
    vpMesgOut.setReasonCode(ipData.getHoldReason());
    vpMesgOut.setLoadID(ipData.getLoadID());
    if (SKDCUserData.isLoggedIn())
    {
      vpMesgOut.setUserID(SKDCUserData.getLoginName());
    }
    vpMesgOut.format();
    addToDataQueue(new HostOutDelegate(vpMesgOut));
  }

 /**
  * Method sends an Order Complete message.
  *
  * @param ipOrdData the Order data to include in the message.
  * @throws DBException if there is a database error, or a message formatting
  *         error.
  */
  public void sendOrderComplete(OrderHeaderData ipOrdData) throws DBException
  {
    /*
     * If the host system is not enabled or this outbound message isn't active,
     * don't try to send any messages.
     */
    if (!mzHasHostSystem ||
        Application.getInt(MessageOutNames.ORDER_COMPLETE.getValue()) == SKDCConstants.INACTIVE_MESSAGE)
    {
      return;
    }

    try
    {
      switch (ipOrdData.getOrderType())
      {
        case DBConstants.ITEMORDER:
        case DBConstants.FULLLOADOUT:
          OrderComplete vpMesgOut = MessageOutFactory.getInstance(MessageOutNames.ORDER_COMPLETE);
          vpMesgOut.setOrderID(ipOrdData.getOrderID());
          vpMesgOut.format();
          addToDataQueue(new HostOutDelegate(vpMesgOut));
          break;

        default:
          break;
      }
    }
    catch (DBException exc)
    {
      throw new DBException(
          "DB exception adding Order Complete Message to data queue.", exc);
    }
    catch (Exception exc)
    {
      throw new DBException(exc);
    }
  }

  /**
   * Method sends the host an Order Status message.
   *
   * @param isOrder the order id. this message is being sent for.
   * @param inOrderStatus the order status being sent. Currently the host is
   *          only notified of Orders that are in the <b>Done</b> state.
   * @throws DBException if there is a database error, or a message formatting
   *           error.
   */
  public void sendOrderStatus(String isOrder, int inOrderStatus)
         throws DBException
  {
    /*
     * If the host system is not enabled or this outbound message isn't active,
     * don't try to send any messages.
     */
    if (!mzHasHostSystem ||
        Application.getInt(MessageOutNames.ORDER_STATUS.getValue()) == SKDCConstants.INACTIVE_MESSAGE)
    {
      return;
    }

    OrderStatus mesgOut = MessageOutFactory.getInstance(MessageOutNames.ORDER_STATUS);
    mesgOut.setOrderID(isOrder);
    mesgOut.setOrderStatus(inOrderStatus);
    try
    {
      mesgOut.format();
      addToDataQueue(new HostOutDelegate(mesgOut));
    }
    catch (DBException exc)
    {
      throw new DBException(
          "DB exception adding Order Status Message to data queue.", exc);
    }
    catch (Exception exc)
    {
      throw new DBException(exc);
    }
  }

  /**
   * Method to send Pick Complete messsage to the host.
   *
   * @param ipMVData Move Data.
   * @param pickedQuantity the amount that is being picked.
   */
  public void sendPickComplete(MoveData ipMVData, double idPickQty)
         throws DBException
  {
    /*
     * If the host system is not enabled or this outbound message isn't active,
     * don't try to send any messages.
     */
    if (!mzHasHostSystem ||
        Application.getInt(MessageOutNames.PICK_COMPLETE.getValue()) == SKDCConstants.INACTIVE_MESSAGE)
    {
      return;
    }

    initializeLoadServer();
    String vsStation = mpLoadServ.getLoadAddress(ipMVData.getLoadID());

    if (vsStation.isEmpty())
    {
      vsStation = "   ";
    }

    try
    {
      initializeOrderServer();
      switch(mpOrderServ.getOrderTypeValue(ipMVData.getOrderID()))
      {
        case DBConstants.ITEMORDER:
        case DBConstants.FULLLOADOUT:
          PickComplete vpMessage = MessageOutFactory.getInstance(MessageOutNames.PICK_COMPLETE);
          vpMessage.setItem(ipMVData.getItem());
          vpMessage.setLot(ipMVData.getPickLot());
          vpMessage.setOrderID(ipMVData.getOrderID());
          vpMessage.setPickLoadID(ipMVData.getLoadID());
          vpMessage.setPickQuantity(idPickQty);
          vpMessage.setPickStation(vsStation);
          if (SKDCUserData.isLoggedIn())
          {
            vpMessage.setUserID(SKDCUserData.getLoginName());
          }
          vpMessage.format();
          addToDataQueue(new HostOutDelegate(vpMessage));
          break;
      }
    }
    catch (DBException exc)
    {
      logException("DB exception adding PickComplete Message to data queue.",
          exc);
      throw new DBException(
          "DB exception adding PickComplete Message to data queue.", exc);
    }
  }

 /**
  * Method sends a Receive Complete to the host.
  *
  * @param isPONum The Purchase Order Number. This parameter may be a
  *            <code>null</code> or empty string if no purchase order exists.
  * @param isStoreStation the station the load was stored from.
  * @param ipData the item detail of the product received.
  * @throws DBException if there are data formatting errors, or errors adding
  *             data to the the outbound data queue.
  */
  public void sendStoreComplete(String isPONum, String isStoreStation,
                                LoadLineItemData ipData) throws DBException
  {
    /*
     * If the host system is not enabled or this outbound message isn't active,
     * don't try to send any messages.
     */
    if (!mzHasHostSystem ||
        Application.getInt(MessageOutNames.STORE_COMPLETE.getValue()) == SKDCConstants.INACTIVE_MESSAGE)
    {
      return;
    }

    StoreComplete mesgOut = MessageOutFactory.getInstance(MessageOutNames.STORE_COMPLETE);
    if (isPONum != null && isPONum.trim().length() != 0)
      mesgOut.setPurchaseOrderID(isPONum);

    mesgOut.setItem(ipData.getItem());
    mesgOut.setLot(ipData.getLot());
    mesgOut.setReceivedLoadID(ipData.getLoadID());
    mesgOut.setReceiveQuantity(ipData.getCurrentQuantity());
    if (SKDCUserData.isLoggedIn())
    {
      mesgOut.setUserID(SKDCUserData.getLoginName());
    }
    mesgOut.setStoreStation(isStoreStation);
    mesgOut.format();
    addToDataQueue(new HostOutDelegate(mesgOut));
  }

  /**
   * Method sends the host an Device Status message.
   *
   * @param isDeviceID the Device id. this message is being sent for.
   * @param inDeviceStatus the Device status being sent.
   * @throws DBException if there is a database error, or a message formatting
   *           error.
   */
  public void sendDeviceStatus(String isDeviceID, String isDeviceStatus)
         throws DBException
  {
    /*
     * If the host system is not enabled or this outbound message isn't active,
     * don't try to send any messages.
     */
     if (!mzHasHostSystem ||
         Application.getInt(MessageOutNames.DEVICE_STATUS.getValue()) == SKDCConstants.INACTIVE_MESSAGE)
     {
      return;
     }

    DeviceStatus mesgOut = MessageOutFactory.getInstance(MessageOutNames.DEVICE_STATUS);
    mesgOut.setDeviceName(isDeviceID);
    mesgOut.setDeviceStatus(isDeviceStatus);
    try
    {
      mesgOut.format();
      addToDataQueue(new HostOutDelegate(mesgOut));
    }
    catch (DBException exc)
    {
      throw new DBException(
          "DB exception adding Order Status Message to data queue.", exc);
    }
    catch (Exception exc)
    {
      throw new DBException(exc);
    }
  }
  /**
   * Method sends an Expected Receipt Complete message to the Host.
   *
   * @param ipEHData the Expected Receipt header data
   * @throws DBException if there is a database error, or a message formatting
   *             error.
   */
  public void sendExpectedReceiptComplete(PurchaseOrderHeaderData ipEHData)
         throws DBException
  {
    /*
     * If the host system is not enabled or this outbound message isn't active,
     * don't try to send any messages.
     */
     if (!mzHasHostSystem ||
         Application.getInt(MessageOutNames.EXPECTED_RECEIPT_COMPLETE.getValue()) == SKDCConstants.INACTIVE_MESSAGE)
     {
      return;
     }

    ExpectedReceiptComplete vpMessage = MessageOutFactory.getInstance(
                                    MessageOutNames.EXPECTED_RECEIPT_COMPLETE);
    vpMessage.setOrderID(ipEHData.getOrderID());
    vpMessage.format();
    addToDataQueue(new HostOutDelegate(vpMessage));
  }

 /**
  * Method to send a Ship Complete message
  *
  * @param inOrderType - Used primarily for extensions that want to treat
  *            orders differently based on order type..
  * @param ipscdata - Ship Complete message structure filled out.
  * @throws DBException for DB access errors.
  */
  public void sendShipComplete(int inOrderType, ShipComplete ipscdata)
         throws DBException
  {
    /*
     * If the host system is not enabled or this outbound message isn't active,
     * don't try to send any messages.
     */
    if (!mzHasHostSystem ||
        Application.getInt(MessageOutNames.SHIP_COMPLETE.getValue()) == SKDCConstants.INACTIVE_MESSAGE)
    {
      return;
    }

    ipscdata.format();
    addToDataQueue(new HostOutDelegate(ipscdata));
  }

  /**
   * Method to send Location Arrival message when Load is stored into an AS/RS
   * location
   *
   * @param isLoadID the load being stored.
   * @param isStoreWhs the load's store warehouse.
   * @param isStoreAddr the load's store address
   * @throws DBException if there is a database update error.
   */
  public void sendLocationArrival(String isLoadID, String isStoreWhs,
                                     String isStoreAddr) throws DBException
  {
    /*
     * If the host system is not enabled or this outbound message isn't active,
     * don't try to send any messages.
     */
    if (!mzHasHostSystem ||
        Application.getInt(MessageOutNames.LOCATION_ARRIVAL.getValue()) == SKDCConstants.INACTIVE_MESSAGE)
    {
      return;
    }

    LocationArrival vpMesg = MessageOutFactory.getInstance(MessageOutNames.LOCATION_ARRIVAL);
    vpMesg.setLoadID(isLoadID);
    vpMesg.setLocation(isStoreWhs, isStoreAddr);
    vpMesg.format();
    addToDataQueue(new HostOutDelegate(vpMesg));
  }

  /**
   * Method to send Load Arrival when a load arrives at an output station or PD
   * stand.
   *
   * @param isLoadID the arriving load.
   * @param isStation the station at which load arrived.
   * @param isOrderID the Order with which load was requested.
   * @throws DBException if there is a database update error.
   */
  public void sendLoadArrival(String isLoadID, String isStation,
      String isOrderID) throws DBException
  {
    /*
     * If the host system is not enabled or this outbound message isn't active,
     * don't try to send any messages.
     */
    if (!mzHasHostSystem ||
        Application.getInt(MessageOutNames.LOAD_ARRIVAL.getValue()) == SKDCConstants.INACTIVE_MESSAGE)
    {
      return;
    }

    LoadArrival vpMesg = MessageOutFactory.getInstance(MessageOutNames.LOAD_ARRIVAL);
    vpMesg.setOrderID(isOrderID);
    vpMesg.setLoadID(isLoadID);
    vpMesg.setArrivalStation(isStation);
    vpMesg.format();
    addToDataQueue(new HostOutDelegate(vpMesg));
  }

  /**
   * {@inheritDoc}
   *
   * @param errorCode {@inheritDoc}
   * @param iOriginalSequence {@inheritDoc}
   * @param sHostName {@inheritDoc}
   * @param errorMessage {@inheritDoc}
   * @param errorFields {@inheritDoc}
   * @throws DBException when there are database access or write errors.
   * @see com.daifukuamerica.wrxj.host.messages.HostError#setErrorCode
   *      setErrorCode
   * @see com.daifukuamerica.wrxj.host.messages.HostError#setErrorMessage
   *      setErrorMessage
   */
  public void writeHostError(int errorCode, int iOriginalSequence,
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
  public void writeHostError(int errorCode, int iOriginalSequence,
      String sHostName, String errorMessage) throws DBException
  {
    writeHostError(errorCode, iOriginalSequence, sHostName, errorMessage, null);
  }

  /**
   * Convenience method to write error message to the host system.
   *
   * @param errorCode the error code indicating why a message failed to be
   *            integrated into WRx-J.
   * @param iOriginalSequence the original message sequence number from the
   *            host.
   * @param errorMessage an user defined error message.
   * @throws DBException when there are database write or access errors.
   * @see com.daifukuamerica.wrxj.host.messages.HostError#setErrorCode
   *      setErrorCode
   * @see com.daifukuamerica.wrxj.host.messages.HostError#setErrorMessage
   *      setErrorMessage
   */
  public void writeHostError(int errorCode, int iOriginalSequence,
      String errorMessage) throws DBException
  {
    writeHostError(errorCode, iOriginalSequence, "", errorMessage, null);
  }

  protected void initHostError()
  {
    if (mpHostError == null)
      mpHostError = MessageOutFactory.getInstance(MessageOutNames.HOST_ERROR);
    else
      mpHostError.clearAllState();
  }

  protected void initializeLoadServer()
  {
    if (mpLoadServ == null)
    {
      mpLoadServ = Factory.create(StandardLoadServer.class);
    }
  }

  protected void initializeOrderServer()
  {
    if (mpOrderServ == null)
    {
      mpOrderServ = Factory.create(StandardOrderServer.class);
    }
  }
}
