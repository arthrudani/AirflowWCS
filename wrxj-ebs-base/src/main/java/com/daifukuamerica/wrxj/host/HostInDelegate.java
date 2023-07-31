package com.daifukuamerica.wrxj.host;

/****************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright ? 2004 Daifuku America Corporation  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND
  TREATIES. NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED
  COPIED, DISTRIBUTED, REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED,
  EXPANDED, COLLECTED, COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED
  WITHOUT THE PRIOR WRITTEN CONSENT OF Daifuku America Corporation ANY
  USE OR EXPLOITATION OF THIS WORK WITHOUT AUTHORIZATION COULD SUBJECT THE
  PERPETRATOR TO CRIMINAL AND CIVIL LIABILITY.
 ****************************************************************************/
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCDataEnum;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.HostConfig;
import com.daifukuamerica.wrxj.dbadapter.data.HostToWrx;
import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.SynonymData;
import com.daifukuamerica.wrxj.dbadapter.data.TableJoin;
import com.daifukuamerica.wrxj.dbadapter.data.WrxToHostData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.log.Logger;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * {@inheritDoc}
 * @author       A.D.
 * @version      1.0   02/18/2005
 */
public class HostInDelegate implements HostServerDelegate 
{
  private HostToWrx     mpHostIn;
  private HostToWrxData mpHiData;
  private boolean       mzReadWithLock  = false;
  protected boolean     mzDataSpecified = false;
  protected Object[]    mapDelegateInfo  = null;
  
  public HostInDelegate()
   {
    this(null);
  }

  public HostInDelegate(Object ipDelegateInfo)
  {
    if (ipDelegateInfo != null)
    {
      mapDelegateInfo = new Object[1];
      mapDelegateInfo[0] = ipDelegateInfo;
    }
    mpHostIn = Factory.create(HostToWrx.class);
    mpHiData = Factory.create(HostToWrxData.class);
    mzDataSpecified = (ipDelegateInfo != null);
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
  * @param {@inheritDoc}
  * @throws {@inheritDoc}
  */
  public void setInfo(Object ... iapDelegateInfo) throws DBException
  {
    mapDelegateInfo = iapDelegateInfo;
    mzDataSpecified = true;    
  }

 /**
  * {@inheritDoc}
  * @param {@inheritDoc}
  */
  public void setReadLockInfo(boolean izReadWithLock)
  {
    mzReadWithLock = izReadWithLock;
  }
  
 /**
  * Adds an inbound message to the data queue. This is normally called by the
  * Transporter after it receives a message.  <i>This delegate is expected to
  * have a reference to </i>{@link com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData#HostToWrxData HostToWrxData}
  * @throws {@inheritDoc}
  */
  public void addToDataQueue() throws DBException
  {
      addToDataQueue(0);
  }
  
  /**
   * Adds an inbound message to the data queue. This is normally called by the
   * Transporter after it receives a message.  <i>This delegate is expected to
   * have a reference to </i>{@link com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData#HostToWrxData HostToWrxData}
   * @throws {@inheritDoc}
   */
   public void addToDataQueue(int originalMessageSequenceNumber) throws DBException
   {
     mzDataSpecified = false;
     if (mapDelegateInfo == null || !(mapDelegateInfo[0] instanceof HostToWrxData))
       throw new DBException("Data content for add operation not correctly " +
                             "initialized! HostToWrxData data class not specified.");
     HostToWrxData vpHostInData = (HostToWrxData)mapDelegateInfo[0];
     vpHostInData.setOriginalMessageSequence(originalMessageSequenceNumber);
                                        // Get sequence number.
     int vnNewSequenceNumber = mpHostIn.getNextSequenceNumber(vpHostInData.getHostName());

     vpHostInData.setMessageSequence(vnNewSequenceNumber);
     mpHostIn.addElement(vpHostInData);
   }
  
  /**
   * Adds an inbound message to the data queue. This is normally called by the
   * Transporter after it receives a message.  <i>This delegate is expected to
   * have a reference to </i>{@link com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData#HostToWrxData HostToWrxData}
   * @throws {@inheritDoc}
   */
   public void addToDataQueue(int inRequestId, String isLoadId) throws DBException
   {
     mzDataSpecified = false;
     if (mapDelegateInfo == null || !(mapDelegateInfo[0] instanceof HostToWrxData))
       throw new DBException("Data content for add operation not correctly " +
                             "initialized! HostToWrxData data class not specified.");
     HostToWrxData vpHostInData = (HostToWrxData)mapDelegateInfo[0];
                                        // Get sequence number.
     int vnNewSequenceNumber = mpHostIn.getNextSequenceNumber(vpHostInData.getHostName());

     vpHostInData.setMessageSequence(vnNewSequenceNumber);
     mpHostIn.addElement(vpHostInData);
   }

 /**
  *  {@inheritDoc} <i>The delegate is expected to have a reference to
  *  </i>{@link com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData#HostToWrxData HostToWrxData}
  *  <i>with the Host name, and Host Message Sequence specified</i>
  *  @throws {@inheritDoc}
  */
  public void deleteMessage() throws DBException
  {
    mzDataSpecified = false;
    if (mapDelegateInfo == null || !(mapDelegateInfo[0] instanceof HostToWrxData))
      throw new DBException("Data content for delete operation not correctly " +
                            "initialized! HostToWrxData data class not specified.");
    HostToWrxData vpHostInData = (HostToWrxData)mapDelegateInfo[0];
    vpHostInData.setKey(WrxToHostData.MESSAGESEQUENCE_NAME,
      Integer.valueOf(vpHostInData.getMessageSequence()));
    vpHostInData.setKey(WrxToHostData.HOSTNAME_NAME, vpHostInData.getHostName());
    mpHostIn.deleteElement(vpHostInData);
  }

 /**
  *  {@inheritDoc} This implementation has the capability of deleting all processed
  *  messages previous to a given date. <i>The delegate is required to have the
  *  Host's name string, and possibly the cut-off date for the deletion.  <b> The
  *  cut-off date must follow the host name</b>  If the date is not specified,
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
   *  cut-off date must follow the host name</b>  If the date is not specified,
   *  all processed messages are deleted for a given host.</i>
   * @throws DBException if there is a database access error.
   */
   public void deleteProcessedMessagesSQLServer() throws DBException
   {
	   Map currRow = null;
		 
     mzDataSpecified = false;
     if (mapDelegateInfo == null || !(mapDelegateInfo[0] instanceof String))
       throw new DBException("Data content for delete operation not correctly " +
                             "initialized! Host name not specified.");

     mpHiData.clear();
     mpHiData.setKey(WrxToHostData.HOSTNAME_NAME, mapDelegateInfo[0]);
     mpHiData.setKey(WrxToHostData.MESSAGEPROCESSED_NAME, Integer.valueOf(DBConstants.NO),
                     KeyObject.NOT_EQUAL);

     if (mapDelegateInfo.length > 1)     // If the cut-off date is given, use it!
     {
       if (!(mapDelegateInfo[1] instanceof Date))
       {
         throw new DBException("Data content for delete operation not correctly " +
                               "initialized! Cut-off date not specified.");
       }
       mpHiData.setKey(WrxToHostData.MESSAGEADDTIME_NAME, mapDelegateInfo[1],
                       KeyObject.LESS_THAN_INCLUSIVE);
     }
     
     try 
	  {
		  // Get a list of HostToWRx rows that can be deleted
	   	List<Map> hmList = mpHostIn.getSelectedColumnElementsForDeletion(mpHiData);
	   	Logger.getLogger().logOperation("HostInDelegate, deleting " + hmList.size() + " records from " + mapDelegateInfo[0] );
	
	   	for(int ldIdx = 0; ldIdx < hmList.size(); ldIdx++)
	   	{
	   	     mpHiData.clear();
//	   	     mpHiData.dataToSKDCData(hmList.get(ldIdx));
	   	     
	   	     Map currObj = hmList.get(ldIdx);
	   	     long iID = DBHelper.getLongField(currObj, AbstractSKDCDataEnum.ID.getName());
	   	     mpHiData.setKey(AbstractSKDCDataEnum.ID.getName(), iID);
	   	   
	   	     mpHostIn.deleteElement(mpHiData);
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
    *  cut-off date must follow the host name</b>  If the date is not specified,
    *  all processed messages are deleted for a given host.</i>
    * @throws DBException if there is a database access error.
    */
    public void deleteProcessedMessagesOracle() throws DBException
    {
      mzDataSpecified = false;
      if (mapDelegateInfo == null || !(mapDelegateInfo[0] instanceof String))
        throw new DBException("Data content for delete operation not correctly " +
                              "initialized! Host name not specified.");

      mpHiData.clear();
      mpHiData.setKey(WrxToHostData.HOSTNAME_NAME, mapDelegateInfo[0]);
      mpHiData.setKey(WrxToHostData.MESSAGEPROCESSED_NAME, Integer.valueOf(DBConstants.NO),
                      KeyObject.NOT_EQUAL);

      if (mapDelegateInfo.length > 1)     // If the cut-off date is given, use it!
      {
        if (!(mapDelegateInfo[1] instanceof Date))
        {
          throw new DBException("Data content for delete operation not correctly " +
                                "initialized! Cut-off date not specified.");
        }
        mpHiData.setKey(WrxToHostData.MESSAGEADDTIME_NAME, mapDelegateInfo[1],
                        KeyObject.LESS_THAN_INCLUSIVE);
      }
      mpHostIn.deleteElement(mpHiData);
    }

 /**
  *  {@inheritDoc}<i>The delegate is expected to have a reference to </i>
  * {@link com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData#HostToWrxData HostToWrxData}
  */
  public void markMessageProcessed() throws DBException
  {
    mzDataSpecified = false;
    if (mapDelegateInfo == null || !(mapDelegateInfo[0] instanceof HostToWrxData))
      throw new DBException("Data content for modify operation not correctly " +
                            "initialized! HostToWrxData data class not specified.");
    HostToWrxData vpHostInData = (HostToWrxData)mapDelegateInfo[0];
    String theHostName = vpHostInData.getHostName();
    int vnSequenceNumber = vpHostInData.getMessageSequence();
    mpHostIn.markMessageProcessed(theHostName, vnSequenceNumber);
  }

 /**
  *  {@inheritDoc}<i>The delegate is expected to know the Host name as well as
  *  the sequence number in the HostToWrxData container (whether it's specified
  *  in key format or just in data field format). In addition it must
  *  know what the value of the processed flag should be set to.</i>
  */
  public void toggleProcessedFlag() throws DBException
  {
    mpHiData.clear();
    mzDataSpecified = false;
    if (mapDelegateInfo == null || !(mapDelegateInfo[0] instanceof HostToWrxData))
      throw new DBException("Data content for modify operation not correctly " +
                            "initialized! HostToWrxData data class not specified.");
    HostToWrxData vpHostInData = (HostToWrxData)mapDelegateInfo[0];
    
/*----------------------------------------------------------------------------
   If they provided key info. make sure it is valid.  If they provided field
   information convert it to key values.
  ----------------------------------------------------------------------------*/
    KeyObject vpSequenceKey = vpHostInData.getKeyObject(WrxToHostData.MESSAGESEQUENCE_NAME);
    KeyObject vpHostNameKey = vpHostInData.getKeyObject(WrxToHostData.HOSTNAME_NAME);
    ColumnObject processedFlag = vpHostInData.getColumnObject(WrxToHostData.MESSAGEPROCESSED_NAME);
    if (vpSequenceKey != null && vpHostNameKey != null && processedFlag != null)
    {
      mpHiData.addKeyObject(vpSequenceKey);
      mpHiData.addKeyObject(vpHostNameKey);
      mpHiData.addColumnObject(processedFlag);
    }
    else if (vpHostInData.getHostName().trim().length() != 0 && vpHostInData.getMessageSequence() != 0)
    {
      mpHiData.setKey(WrxToHostData.MESSAGESEQUENCE_NAME, Integer.valueOf(vpHostInData.getMessageSequence()));
      mpHiData.setKey(WrxToHostData.HOSTNAME_NAME, vpHostInData.getHostName());
      mpHiData.setMessageProcessed(vpHostInData.getMessageProcessed());
    }
    else
    {
      throw new DBException("Host delegate does not have enough information for " +
                            "the HostServer to complete the modify operation...");
    }
    mpHostIn.modifyElement(mpHiData);
  }
  
 /**
  * {@inheritDoc} <i>The delegate is expected to have the Host Name.</i>
  */
  public boolean unprocessedMessageAvailable() throws DBException
  {
    mzDataSpecified = false;
    mpHiData.clear();
    if (mapDelegateInfo != null && mapDelegateInfo[0] instanceof String)
    {                                  // If this is given assume it is the host name.
      mpHiData.setKey(WrxToHostData.HOSTNAME_NAME, mapDelegateInfo[0]);
    }
      
    mpHiData.setKey(WrxToHostData.MESSAGEPROCESSED_NAME, Integer.valueOf(DBConstants.NO));
    return(mpHostIn.exists(mpHiData));
  }

 /**
  * Retrieves inbound messages from the HostToWrx table. <i>the
  * delegate is expected to have a reference to a HostToWrxData object.</i>
  * @return java.util.List of records.
  * @throws DBException if there is a database access error.
  */
  public List<Map> getMessages() throws DBException
  {
    mzDataSpecified = false;
    if (mapDelegateInfo == null || !(mapDelegateInfo[0] instanceof HostToWrxData))
      throw new DBException("Data content for read operation not correctly " +
                            "initialized! HostToWrxData not specified.");

    return(mpHostIn.getAllElements((HostToWrxData)mapDelegateInfo[0]));
  }
  
 /**
  * Retrieves one inbound message from the HostToWrx table. <i>the
  * delegate is expected to have a reference to a HostToWrxData object containing
  * at least the Host Name, and a message sequence number.</i>
  * @return {@inheritDoc}
  * @throws DBException if there is a database access error.
  */
  public AbstractSKDCData getMessage() throws DBException
  {
    mzDataSpecified = false;
    if (mapDelegateInfo == null || !(mapDelegateInfo[0] instanceof HostToWrxData))
      throw new DBException("Data content for read operation not correctly " +
                            "initialized! HostToWrxData not specified.");
    int withLock =(mzReadWithLock) ? DBConstants.WRITELOCK : DBConstants.NOWRITELOCK;
    mzReadWithLock = false;
                            
    HostToWrxData vpHostInData = (HostToWrxData)mapDelegateInfo[0];
    if (vpHostInData.getHostName().trim().length() == 0 ||
        vpHostInData.getMessageSequence() <= 0)
    {
      throw new DBException("Data content for read operation not correctly " +
                            "initialized! Either one or both Host Name, and " +
                            "message sequence is not specified.");
    }
    
    vpHostInData.setKey(WrxToHostData.HOSTNAME_NAME, vpHostInData.getHostName());
    vpHostInData.setKey(WrxToHostData.MESSAGESEQUENCE_NAME, vpHostInData.getMessageSequence());
    
    return(mpHostIn.getElement(vpHostInData, withLock));
  }
  
 /**
  * Reads oldest unprocessed message from the inbound data queue.
  * <i>The delegate is not expected to know any information.</i>  If the host
  * name is part of its information, it will be used.
  * @return {@inheritDoc}
  * @throws {@inheritDoc}
  */
  public AbstractSKDCData getOldestUnprocessedMessage() throws DBException
  {
    String theHostName = "";
    mzDataSpecified = false;
    if (mapDelegateInfo != null && mapDelegateInfo[0] instanceof String)
    {                                  // If this is given, assume it's the host name.
      theHostName = (String)mapDelegateInfo[0];
    }
    int vnWithLock =(mzReadWithLock) ? DBConstants.WRITELOCK : DBConstants.NOWRITELOCK;
    mzReadWithLock = false;
    return(mpHostIn.getOldestMessage(theHostName, DBConstants.NO, true, vnWithLock));
  }

 /**
  * Reads newest message from the inbound data queue. <i>The delegate is
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
    return(mpHostIn.getNewestMessage((String)mapDelegateInfo[0], true, vnWithLock));
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

    return(mpHostIn.getMinMaxSequence(vsHostName));
  }
  
 /**
  *  Method gets the minimum, and maximum Host sequence numbers of in the appropriate 
  *  host tables. <i>The delegate is expected to know the host name.</i>
  *
  *  @return <code>int[]</code> array containing sequence number boundaries
  *                             (min/max).
  */
  public int[] getHostSequenceBoundaries() throws DBException
  {
    String vsHostName = "";
    mzDataSpecified = false;
    if (mapDelegateInfo != null && mapDelegateInfo[0] instanceof String)
    {                                  // If this is given, assume it's the host name.
      vsHostName = (String)mapDelegateInfo[0];
    }
    
    return(mpHostIn.getHostMinMaxSequence(vsHostName));
  }

 /**
  * {@inheritDoc} In particular, this method returns the inbound message names.
  */
  public String[] getMessageNames() throws DBException
  {
    HostConfig vpHostConfig = Factory.create(HostConfig.class);
    
    return(vpHostConfig.getInBoundMessageNames());
  }
}
