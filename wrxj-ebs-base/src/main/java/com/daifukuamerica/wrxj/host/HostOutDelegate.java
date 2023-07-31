package com.daifukuamerica.wrxj.host;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCDataEnum;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.HostConfig;
import com.daifukuamerica.wrxj.dbadapter.data.HostOutAccess;
import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.WrxSequencer;
import com.daifukuamerica.wrxj.dbadapter.data.WrxToHost;
import com.daifukuamerica.wrxj.dbadapter.data.WrxToHostData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.messages.MessageOut;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.log.Logger;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is responsible for writing into, and reading messages
 * from the WrxToHost table.
 * @author       A.D.
 * @version      1.0   02/18/2005
 */
public class HostOutDelegate implements HostServerDelegate
{
  protected Object[]      mapDelegateInfo = null;
  protected WrxToHost     mpHostOut       = null;
  protected WrxToHostData mpHoData        = null;
  protected HostOutAccess mpHostOutAccess = null;
  protected boolean       mzDataSpecified = false;
  protected boolean       mzReadWithLock  = false;

  public HostOutDelegate()
  {
    this(null);
  }

 /**
  * Constuctor takes one argument containing information for delegate.
  * @param ipDelegateInfo
  */
  public HostOutDelegate(Object ipDelegateInfo)
  {
    if (ipDelegateInfo != null)
    {
      mapDelegateInfo = new Object[1];
      mapDelegateInfo[0] = ipDelegateInfo;
      mzDataSpecified = true;
    }
    mpHostOut = Factory.create(WrxToHost.class);
    mpHoData = Factory.create(WrxToHostData.class);
    mpHostOutAccess = Factory.create(HostOutAccess.class);
  }

 /**
  * {@inheritDoc}
  */
  public boolean isInfoUnderstood()
  {
    return(mzDataSpecified);
  }

 /**
  * {@inheritDoc}
  * @param iapDelegateInfo Object containing delegate information.
  * @throws DBException if delegate info. is not given.
  */
  public void setInfo(Object ... iapDelegateInfo) throws DBException
  {
    if (iapDelegateInfo == null)
      throw new DBException("HostOutDelegate.setInfo-->NULL data type passed...");
    mapDelegateInfo = iapDelegateInfo;
    mzDataSpecified = true;
  }

 /**
  * {@inheritDoc}
  */
  public void setReadLockInfo(boolean izReadWithLock)
  {
    mzReadWithLock = izReadWithLock;
  }

  /**
   * Adds an outbound message to the data queue. <i>This delegate is expected to
   * have a reference to {@link com.daifukuamerica.wrxj.host.messages.MessageOut#MessageOut MessageOut}</i>
   * @throws DBException when there is a database add error.
   */
   public void addToDataQueue() throws DBException
   {
       addToDataQueue(-1);
   }
  
 /**
  * Adds an outbound message to the data queue. <i>This delegate is expected to
  * have a reference to {@link com.daifukuamerica.wrxj.host.messages.MessageOut#MessageOut MessageOut}</i>
  * @throws DBException when there is a database add error.
  */
  public void addToDataQueue(int originalMessageSequenceNumber) throws DBException
  {
    mzDataSpecified = false;
    if (mapDelegateInfo == null || !(mapDelegateInfo[0] instanceof MessageOut))
      throw new DBException("Host Delegate information not initialized correctly!");
    MessageOut vpMesgOut = (MessageOut)mapDelegateInfo[0];

/*============================================================================
   If the host name is specified, then send message to that host.  If the Host
   name is not specified, send the message to all hosts defined in HostOutAccess.
  ============================================================================*/
    if (vpMesgOut.getAssignedHostName().trim().length() != 0)
    {
      addMessageWithValidation(originalMessageSequenceNumber, vpMesgOut.getAssignedHostName(), vpMesgOut);
    }
    else
    {
      WrxSequencer sequencer = Factory.create(WrxSequencer.class);
      String[] hostNames = sequencer.getEndDeviceNames(DBConstants.HOST_SEQ);
      for(String vsHostName : hostNames)
      {
        addMessageWithValidation(originalMessageSequenceNumber, vsHostName, vpMesgOut);
      }
    }

    vpMesgOut.clearAllState();
  }
  
  /**
   * Adds an outbound message to the data queue. <i>This delegate is expected to
   * have a reference to {@link com.daifukuamerica.wrxj.host.messages.MessageOut#MessageOut MessageOut}</i>
   * @throws DBException when there is a database add error.
   */
   public void addToDataQueue(int inRequestId, String isLoadId) throws DBException
   {
     mzDataSpecified = false;
     if (mapDelegateInfo == null || !(mapDelegateInfo[0] instanceof MessageOut))
       throw new DBException("Host Delegate information not initialized correctly!");
     MessageOut vpMesgOut = (MessageOut)mapDelegateInfo[0];

 /*============================================================================
    If the host name is specified, then send message to that host.  If the Host
    name is not specified, send the message to all hosts defined in HostOutAccess.
   ============================================================================*/
     if (vpMesgOut.getAssignedHostName().trim().length() != 0)
     {
       addMessageWithValidation(vpMesgOut.getAssignedHostName(), vpMesgOut);
     }
     else
     {
       WrxSequencer sequencer = Factory.create(WrxSequencer.class);
       String[] hostNames = sequencer.getEndDeviceNames(DBConstants.HOST_SEQ);
       for(String vsHostName : hostNames)
       {
         addMessageWithValidation(vsHostName, vpMesgOut);
       }
     }

     vpMesgOut.clearAllState();
   }

 /**
  *  {@inheritDoc} <i>The delegate is expected to have a reference to</i>
  *  {@link com.daifukuamerica.wrxj.dbadapter.data.WrxToHostData#WrxToHostData WrxToHostData}
  *  <i>with the Host name, and Host Message Sequence specified</i>
  */
  public void deleteMessage() throws DBException
  {
    mzDataSpecified = false;
    if (mapDelegateInfo == null || !(mapDelegateInfo[0] instanceof WrxToHostData))
      throw new DBException("Data content for delete operation not correctly " +
                            "initialized! WrxToHostData data class not specified.");
                                       // The addElement in HostToWrx should
                                       // use the InputStream reference in the
                                       // data class to add the CLOB.
    WrxToHostData hostOutData = (WrxToHostData)mapDelegateInfo[0];
    hostOutData.setKey(WrxToHostData.MESSAGESEQUENCE_NAME,
      Integer.valueOf(hostOutData.getMessageSequence()));
    hostOutData.setKey(WrxToHostData.HOSTNAME_NAME, hostOutData.getHostName());
    mpHostOut.deleteElement(hostOutData);
  }

 /**
  *  {@inheritDoc} This implementation has the capability of deleting all processed
  *  messages previous to a given date. <i>The delegate is required to have the
  *  Host's name string, and possibly the cut-off date for the deletion.  <b> The
  *  cut-off date must follow the host name.</b>  If the date is not specified,
  *  all processed messages are deleted for a given host.</i>
  * @throws DBException if there is a database access error.
  */
  public void deleteProcessedMessages() throws DBException
  {
	  if( DBInfo.USING_SQL_SERVER )
	  {
		  deleteProcessedMessagesSQLServer();
	  }
	  else if( DBInfo.USING_ORACLE_DB )
	  {
		  deleteProcessedMessagesOracle();
	  }
  }
  /**
   *  {@inheritDoc} This implementation has the capability of deleting all processed
   *  messages previous to a given date. <i>The delegate is required to have the
   *  Host's name string, and possibly the cut-off date for the deletion.  <b> The
   *  cut-off date must follow the host name.</b>  If the date is not specified,
   *  all processed messages are deleted for a given host.</i>
   * @throws DBException if there is a database access error.
   */
   public void deleteProcessedMessagesSQLServer() throws DBException
   {
     mzDataSpecified = false;
     if (mapDelegateInfo == null || !(mapDelegateInfo[0] instanceof String))
       throw new DBException("Data content for delete operation not correctly " +
                             "initialized! Host name not specified.");
     mpHoData.clear();
     mpHoData.setKey(WrxToHostData.HOSTNAME_NAME, mapDelegateInfo[0]);
     mpHoData.setKey(WrxToHostData.MESSAGEPROCESSED_NAME,
       Integer.valueOf(DBConstants.NO), KeyObject.NOT_EQUAL);

     if (mapDelegateInfo.length > 1)     // If the cut-off date is given, use it!
     {
       if (!(mapDelegateInfo[1] instanceof Date))
       {
         throw new DBException("Data content for delete operation not correctly " +
                               "initialized! Cut-off date not specified.");
       }
       mpHoData.setKey(WrxToHostData.MESSAGEADDTIME_NAME,
                       mapDelegateInfo[1], KeyObject.LESS_THAN_INCLUSIVE);
     }
     
     try 
	  {
		  // Get a list of WRxToHost rows that can be deleted
	   	List<Map> hmList = mpHostOut.getSelectedColumnElementsForDeletion(mpHoData);
	   	Logger.getLogger().logOperation("HostOutDelegate, deleting " + hmList.size() + " records from " + mapDelegateInfo[0] );
	
	   	for(int ldIdx = 0; ldIdx < hmList.size(); ldIdx++)
	   	{
	   		mpHoData.clear();
//	   		mpHoData.dataToSKDCData(hmList.get(ldIdx));
	   		
	   		Map currObj = hmList.get(ldIdx);
	   		long iID = DBHelper.getLongField(currObj, AbstractSKDCDataEnum.ID.getName());
	   		mpHoData.setKey(AbstractSKDCDataEnum.ID.getName(), iID);
   	   
	   		mpHostOut.deleteElement(mpHoData);
	   	}
	  } 
	  catch (DBException e) 
	  {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
   }
   
   /**
    *  {@inheritDoc} This implementation has the capability of deleting all processed
    *  messages previous to a given date. <i>The delegate is required to have the
    *  Host's name string, and possibly the cut-off date for the deletion.  <b> The
    *  cut-off date must follow the host name.</b>  If the date is not specified,
    *  all processed messages are deleted for a given host.</i>
    * @throws DBException if there is a database access error.
    */
    public void deleteProcessedMessagesOracle() throws DBException
    {
      mzDataSpecified = false;
      if (mapDelegateInfo == null || !(mapDelegateInfo[0] instanceof String))
        throw new DBException("Data content for delete operation not correctly " +
                              "initialized! Host name not specified.");
      mpHoData.clear();
      mpHoData.setKey(WrxToHostData.HOSTNAME_NAME, mapDelegateInfo[0]);
      mpHoData.setKey(WrxToHostData.MESSAGEPROCESSED_NAME,
        Integer.valueOf(DBConstants.NO), KeyObject.NOT_EQUAL);

      if (mapDelegateInfo.length > 1)     // If the cut-off date is given, use it!
      {
        if (!(mapDelegateInfo[1] instanceof Date))
        {
          throw new DBException("Data content for delete operation not correctly " +
                                "initialized! Cut-off date not specified.");
        }
        mpHoData.setKey(WrxToHostData.MESSAGEADDTIME_NAME,
                        mapDelegateInfo[1], KeyObject.LESS_THAN_INCLUSIVE);
      }
      mpHostOut.deleteElement(mpHoData);
    }

 /**
  *  {@inheritDoc}<i>The delegate is expected to know the Host name as well as
  *  the sequence number.</i>
  */
  public void markMessageProcessed() throws DBException
  {
    mzDataSpecified = false;
    if (mapDelegateInfo == null || !(mapDelegateInfo[0] instanceof WrxToHostData))
      throw new DBException("Data content for modify operation not correctly " +
                            "initialized! WrxToHostData data class not specified.");
    WrxToHostData hostOutData = (WrxToHostData)mapDelegateInfo[0];
    String theHostName = hostOutData.getHostName();
    int theSequenceNumber = hostOutData.getMessageSequence();
    mpHostOut.markMessageProcessed(theHostName, theSequenceNumber);
  }

 /**
  *  {@inheritDoc} <i>The delegate is expected to know the Host name as well as
  *  the sequence number in the WrxToHostData container (whether it's specified
  *  in key format or just in data field format). In addition it must
  *  know what the value of the processed flag should be set to.</i>
  */
  public void toggleProcessedFlag() throws DBException
  {
    mpHoData.clear();
    mzDataSpecified = false;
    if (mapDelegateInfo == null || !(mapDelegateInfo[0] instanceof WrxToHostData))
      throw new DBException("Data content for modify operation not correctly " +
                            "initialized! WrxToHostData data class not specified.");
    WrxToHostData vpHostOutData = (WrxToHostData)mapDelegateInfo[0];

/*----------------------------------------------------------------------------
   If they provided key info. make sure it is valid.  If they provided field
   information convert it to key values.
  ----------------------------------------------------------------------------*/
    KeyObject vpSequenceKey = vpHostOutData.getKeyObject(WrxToHostData.MESSAGESEQUENCE_NAME);
    KeyObject vpHostNameKey = vpHostOutData.getKeyObject(WrxToHostData.HOSTNAME_NAME);
    ColumnObject vpProcessedFlag = vpHostOutData.getColumnObject(WrxToHostData.MESSAGEPROCESSED_NAME);
    if (vpSequenceKey != null && vpHostNameKey != null && vpProcessedFlag != null)
    {
      mpHoData.addKeyObject(vpSequenceKey);
      mpHoData.addKeyObject(vpHostNameKey);
      mpHoData.addColumnObject(vpProcessedFlag);
    }
    else if (vpHostOutData.getHostName().trim().length() != 0 && vpHostOutData.getMessageSequence() != 0)
    {
      mpHoData.setKey(WrxToHostData.MESSAGESEQUENCE_NAME, Integer.valueOf(vpHostOutData.getMessageSequence()));
      mpHoData.setKey(WrxToHostData.HOSTNAME_NAME, vpHostOutData.getHostName());
      mpHoData.setMessageProcessed(vpHostOutData.getMessageProcessed());
    }
    else
    {
      throw new DBException("Host delegate does not have enough information for " +
                            "the HostServer to complete the modify operation...");
    }
    mpHostOut.modifyElement(mpHoData);
  }

 /**
  * {@inheritDoc} <i>The delegate is expected to have the Host Name.</i>
  */
  public boolean unprocessedMessageAvailable() throws DBException
  {
    mzDataSpecified = false;
    if (mapDelegateInfo == null && !(mapDelegateInfo[0] instanceof String))
      throw new DBException("Data content for read operation not correctly " +
                            "initialized! Host name not specified.");
    mpHoData.clear();
    mpHoData.setKey(WrxToHostData.HOSTNAME_NAME, mapDelegateInfo[0]);
    mpHoData.setKey(WrxToHostData.MESSAGEPROCESSED_NAME, Integer.valueOf(DBConstants.NO));
    return(mpHostOut.exists(mpHoData));
  }
  
  /**
   * {@inheritDoc} <i>The delegate is expected to have the Host Name.</i>
   */
   public boolean isPendingMessageFound(int ackTimeoutInSeconds) throws DBException
   {
     mzDataSpecified = false;
     if (mapDelegateInfo == null && !(mapDelegateInfo[0] instanceof String)) {
       throw new DBException("Data content for read operation not correctly " +
                             "initialized! Host name not specified.");
     }
     
     mpHoData.clear();
     mpHoData.setKey(WrxToHostData.HOSTNAME_NAME, mapDelegateInfo[0]);
     mpHoData.setKey(WrxToHostData.MESSAGEPROCESSED_NAME, Integer.valueOf(DBConstants.YES));
     mpHoData.setKey(WrxToHostData.SENT_NAME, null, KeyObject.NOT_EQUAL);
     mpHoData.setKey(WrxToHostData.SENT_NAME, Date.from(LocalDateTime.now().minus(ackTimeoutInSeconds, ChronoUnit.SECONDS).atZone(ZoneId.systemDefault()).toInstant()), KeyObject.LESS_THAN);
     mpHoData.setKey(WrxToHostData.ACKED_NAME, DBConstants.PENDING_ACK);
     
     return mpHostOut.exists(mpHoData);
   }

 /**
  * {@inheritDoc} Reads outbound messages from the WrxToHost table.
  * <i>The delegate is expected to have a reference to a WrxToHostData object, with
  * any key info. necessary filled in.</i>
  * @return java.util.List of records.
  * @throws DBException
  */
  public List<Map> getMessages() throws DBException
  {
    mzDataSpecified = false;
    if (mapDelegateInfo == null && !(mapDelegateInfo[0] instanceof WrxToHostData))
      throw new DBException("Data content for read operation not correctly " +
                            "initialized! WrxToHostData not specified.");

    return(mpHostOut.getAllElements((WrxToHostData)mapDelegateInfo[0]));
  }

 /**
  * Retrieves one inbound message from the WrxToHost table. <i>the
  * delegate is expected to have a reference to a HostToWrxData object containing
  * at least the Host Name, and a message sequence number.</i>
  * @return {@inheritDoc}
  * @throws DBException if there is a database access error.
  */
  public AbstractSKDCData getMessage() throws DBException
  {
    mzDataSpecified = false;
    if (mapDelegateInfo == null || !(mapDelegateInfo[0] instanceof WrxToHostData))
      throw new DBException("Data content for read operation not correctly " +
                            "initialized! WrxToHostData not specified.");

    int vnWithLock =(mzReadWithLock) ? DBConstants.WRITELOCK : DBConstants.NOWRITELOCK;
    mzReadWithLock = false;

    WrxToHostData vpHostOutData = (WrxToHostData)mapDelegateInfo[0];
    if (vpHostOutData.getHostName().trim().length() == 0 ||
        vpHostOutData.getMessageSequence() <= 0)
    {
      throw new DBException("Data content for read operation not correctly " +
                            "initialized! Either one or both Host Name, and " +
                            "message sequence is not specified.");
    }

    vpHostOutData.setKey(WrxToHostData.HOSTNAME_NAME, vpHostOutData.getHostName());
    vpHostOutData.setKey(WrxToHostData.MESSAGESEQUENCE_NAME, vpHostOutData.getMessageSequence());

    return(mpHostOut.getElement(vpHostOutData, vnWithLock));
  }

 /**
  * Reads oldest unprocessed message from the outbound data queue. <i>The delegate
  * is expected to know the host name, and whether the host record should be
  * read with a lock.</i>
  * @throws DBException if there is a database access error.
  */
  public AbstractSKDCData getOldestUnprocessedMessage() throws DBException
  {
    mzDataSpecified = false;
    if (mapDelegateInfo == null && !(mapDelegateInfo[0] instanceof String))
      throw new DBException("Data content for read operation not correctly " +
                            "initialized! Host name not specified.");

    int withLock =(mzReadWithLock) ? DBConstants.WRITELOCK : DBConstants.NOWRITELOCK;
    mzReadWithLock = false;
    return(mpHostOut.getOldestMessage((String)mapDelegateInfo[0], DBConstants.NO, true,
                                    withLock));
  }

 /**
  * Reads newest message from the outbound data queue. <i>The delegate is
  * expected to know the host name, and whether the host record should be read
  * with a lock (default is read with no lock).</i>
  * @throws DBException if there are connectivity errors.
  */
  public AbstractSKDCData getNewestMessage() throws DBException
  {
    mzDataSpecified = false;
    if (mapDelegateInfo == null && !(mapDelegateInfo[0] instanceof String))
      throw new DBException("Data content for read operation not correctly " +
                            "initialized! Host name not specified.");

    int vnWithLock =(mzReadWithLock) ? DBConstants.WRITELOCK : DBConstants.NOWRITELOCK;
    mzReadWithLock = false;
    return(mpHostOut.getNewestMessage((String)mapDelegateInfo[0], true, vnWithLock));
  }

 /**
  * {@inheritDoc} In particular, this method returns the outbound message names.
  */
  public String[] getMessageNames() throws DBException
  {
    HostConfig vpHostConfig = Factory.create(HostConfig.class);
    return(vpHostConfig.getOutBoundMessageNames());
  }

 /**
  * Method gets all distinct host names.
  * @return String array of host names.
  */
  public String[] getHostNames() throws DBException
  {
    WrxSequencer sequencer = Factory.create(WrxSequencer.class);
    return(sequencer.getEndDeviceNames(DBConstants.HOST_SEQ));
  }

 /**
  *  Method gets the minimum, and maximum sequence numbers in the appropriate
  *  host tables. <i>The delegate is expected to know the host name.</i>
  *
  *  @return <code>int[]</code> array containing sequence number boundaries
  *                             (min/max).
  */
  public int[] getSequenceBoundaries() throws DBException
  {
    String vsHostName = "";
    mzDataSpecified = false;
    if (mapDelegateInfo != null && mapDelegateInfo[0] instanceof String)
    {                                  // If this is given, assume it's the host name.
      vsHostName = (String)mapDelegateInfo[0];
    }

    return(mpHostOut.getMinMaxSequence(vsHostName));
  }

 /**
  *  Method gets the minimum, and maximum Host sequence numbers in the appropriate
  *  host tables. <i>The delegate is expected to know the host name.</i>
  *
  *  @return <code>int[]</code> array containing sequence number boundaries
  *                             (min/max).
  */
  public int[] getHostSequenceBoundaries() throws DBException
  {
    // since there is not such thing for Host Out, just return an empty arrary
    return(new int[0]);
  }

  /**
   * Worker method that adds a message to the outbound data queue.
   * @param isHostName name of the host getting the message.
   * @throws DBException if the Host message is not defined in HostOutAccess.
   */
   protected void addMessageWithValidation(String isHostName, MessageOut ipMesgOut) throws DBException
   {
       addMessageWithValidation(-1, isHostName, ipMesgOut);
   }
  
 /**
  * Worker method that adds a message to the outbound data queue.
  * @param isHostName name of the host getting the message.
  * @throws DBException if the Host message is not defined in HostOutAccess.
  */
  protected void addMessageWithValidation(int originalMessageSequenceNumber, String isHostName, MessageOut ipMesgOut) throws DBException
  {
    if (mpHostOutAccess.isLegalMessage(isHostName, ipMesgOut.getMessageIdentifier()))
    {
      int vnSequenceNumber = mpHostOut.getNextSequenceNumber(isHostName);
      mpHoData.clear();
      mpHoData.setMessageIdentifier(ipMesgOut.getMessageIdentifier());
      mpHoData.setMessageSequence(vnSequenceNumber);
      if (originalMessageSequenceNumber < 0) {
          // Set a new sequence number as this is for a request message from AirflowWCS to SAC
          int requestSequenceNumber = mpHostOut.getNextRequestSequenceNumber(isHostName);
          mpHoData.setOriginalMessageSequence(requestSequenceNumber);
          // This requires ack sent by SAC
          mpHoData.setAcked(DBConstants.PENDING_ACK); // Not acked yet
          // Reset retry count
          mpHoData.setRetryCount(0);
      } else {
          // Set the given original sequence number included request message from SAC to AirflowWCS
          // as this is for an ack message
          mpHoData.setOriginalMessageSequence(originalMessageSequenceNumber);
          // No need of waiting for ack
          mpHoData.setAcked(DBConstants.NO_ACK_REQUIRED);
          // Reset retry count
          mpHoData.setRetryCount(0);
      }
      mpHoData.setHostName(isHostName);
      mpHoData.setMessageBytes(ipMesgOut.prepareMessageToSend(vnSequenceNumber));
      mpHostOut.addElement(mpHoData);
    }
    else if (!mpHostOutAccess.exists(isHostName, ipMesgOut.getMessageIdentifier()))
    {
      throw new DBException("Host Message " + ipMesgOut.getMessageIdentifier() +
                            " is undefined in HostOutAccess");
    }
  }

  public List<WrxToHostData> getAllPendingMessages(int ackTimeoutInSeconds) throws DBException {
    mzDataSpecified = false;
    if (mapDelegateInfo == null && !(mapDelegateInfo[0] instanceof String)) {
      throw new DBException("Data content for read operation not correctly " +
                            "initialized! Host name not specified.");
    }
      
    // processed = 1
    // sent != null
    // retry count <= max retry count
    // sent < now - timeout
    // acked = 2
    mpHoData.clear();
    mpHoData.setKey(WrxToHostData.HOSTNAME_NAME, mapDelegateInfo[0]);
    mpHoData.setKey(WrxToHostData.MESSAGEPROCESSED_NAME, Integer.valueOf(DBConstants.YES));
    mpHoData.setKey(WrxToHostData.SENT_NAME, null, KeyObject.NOT_EQUAL);
    mpHoData.setKey(WrxToHostData.SENT_NAME, Date.from(LocalDateTime.now().minus(ackTimeoutInSeconds, ChronoUnit.SECONDS).atZone(ZoneId.systemDefault()).toInstant()), KeyObject.LESS_THAN);
    mpHoData.setKey(WrxToHostData.ACKED_NAME, DBConstants.PENDING_ACK);
    
    // Get the list of rows
    List<Map> results = mpHostOut.getAllElements(mpHoData);
    if (results == null || results.isEmpty()) {
        return List.<WrxToHostData>of();
    }
    
    // Convert to the list of WrxToHostData
    List<WrxToHostData> wrxToHostDataList = results.stream().map(row -> {
        WrxToHostData wrxToHostDataToReturn = Factory.create(WrxToHostData.class);
        wrxToHostDataToReturn.dataToSKDCData(row);
        return wrxToHostDataToReturn;
    }).collect(Collectors.toList());
    
    return wrxToHostDataList;
  }

  public void markMessageAsRetransmitted() throws DBException {
    // update sent to now
    // increase retry count
    // if retry count >= max, update acked = 3(no more try)
    mzDataSpecified = false;
    if (mapDelegateInfo == null || !(mapDelegateInfo[0] instanceof WrxToHostData)) {
      throw new DBException("Data content for modify operation not correctly " +
                              "initialized! WrxToHostData data class not specified.");
    }

    WrxToHostData hostOutData = (WrxToHostData)mapDelegateInfo[0];
    String theHostName = hostOutData.getHostName();
    int sequenceNumber = hostOutData.getMessageSequence();
    int originalRetryCount = hostOutData.getRetryCount();
    mpHostOut.markMessageAsRetransmitted(theHostName, sequenceNumber, originalRetryCount + 1);
  }
  
  public void markMessageAsAckFailed() throws DBException {
      mzDataSpecified = false;
      if (mapDelegateInfo == null || !(mapDelegateInfo[0] instanceof WrxToHostData)) {
        throw new DBException("Data content for modify operation not correctly " +
                                "initialized! WrxToHostData data class not specified.");
      }

      WrxToHostData hostOutData = (WrxToHostData)mapDelegateInfo[0];
      String theHostName = hostOutData.getHostName();
      int sequenceNumber = hostOutData.getMessageSequence();
      mpHostOut.markMessageAsAckFailed(theHostName, sequenceNumber);
    }

  public void markMessageAsAcked() throws DBException {
    mzDataSpecified = false;
    if (mapDelegateInfo == null || !(mapDelegateInfo[0] instanceof HostToWrxData)) {
      throw new DBException("Data content for modify operation not correctly " +
                              "initialized! HostToWrxData data class not specified.");
    }
    HostToWrxData vpHostInData = (HostToWrxData)mapDelegateInfo[0];
    mpHostOut.markMessageAsAcked(vpHostInData.getHostName(), vpHostInData.getOriginalMessageSequence());
    
  }
}
