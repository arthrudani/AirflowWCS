package com.daifukuamerica.wrxj.dbadapter.data;
/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright (c) 2004-2008 Daifuku America Corporation  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED,
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN
  CONSENT OF Daifuku America Corporation ANY USE OR EXPLOITATION OF THIS WORK
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL
  LIABILITY.
 ****************************************************************************/

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.messages.HostError;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DACCLOB;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.KeyObject;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Description:<BR>
 *  Class to handle HostToWrx table operations.
 *
 * @author       A.D.
 * @version      1.0   02/08/2005
 */
public class HostToWrx extends BaseDBInterface
{
  public static final String INBOUND_SEQUENCER = "HostToWrx";
  private static final int HOST_MAX_ROWS = 1000;
  protected HostToWrxData mpHostToWrxData;

  public HostToWrx()
  {
    this("HostToWrx");
  }

  public HostToWrx(String isObjectName)
  {
    super(isObjectName);
    mpHostToWrxData = Factory.create(HostToWrxData.class);
  }

  /**
   * {@inheritDoc} This method is meant to be used by the
   * {@link com.daifukuamerica.wrxj.host.HostMessageIntegrator#HostMessageIntegrator HostMessageIntegrator}
   *
   * @param eskdata {@inheritDoc} Note: the Clob Retrieval flag must be set in
   *            the key for the CLOB to be retrieved.
   * @param withLock {@inheritDoc}
   * @return {@inheritDoc}
   * @throws DBException
   */
  @Override
  public AbstractSKDCData getElement(AbstractSKDCData eskdata, int withLock)
         throws DBException
  {
    HostToWrxData keyData = (HostToWrxData)eskdata;
    HostToWrxData outboundData = super.getElement(keyData, withLock);
    if (outboundData != null && keyData.getClobRetrieval())
    {
      // This will retrieve just one record.
      StringBuilder vpSql = new StringBuilder("SELECT sMessage FROM ").append(getWriteTableName()).append(" WHERE ")
               .append("sHostName = '").append(outboundData.getHostName()).append("' AND ")
               .append("iMessageProcessed = ").append(outboundData.getMessageProcessed()).append(" AND ")
               .append("iMessageSequence = ").append(outboundData.getMessageSequence());
      byte[] clobBytes = DBHelper.readClob(WrxToHostData.MESSAGE_NAME,
                                           vpSql.toString());
      outboundData.setMessageBytes(clobBytes);
    }

    return(outboundData);
  }

 /**
  * {@inheritDoc} This method will order the list from the oldest to the newest
  *               data. <b>Note:</b> this method does <u>not</u> return any CLOB
  *               data.
  * @param  {@inheritDoc}
  * @return {@inheritDoc}
  */
  @Override
  public List<Map> getAllElements(AbstractSKDCData absData) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT * FROM ").append(getWriteTableName())
             .append(DBHelper.buildWhereClause(absData.getKeyArray()))
             .append(DBHelper.buildOrderByClause(absData.getOrderByColumns()));
    return fetchRecords(vpSql.toString());
  }

 /**
  * {@inheritDoc} This method is meant to be used to add records to the HostToWrx
  *               table only.
  * @param ipRecvData reference to received data.
  * @throws DBException for database errors, or stream IO errors when writing to
  *         the CLOB.
  */
  @Override
  public void addElement(AbstractSKDCData ipRecvData) throws DBException
  {
    HostToWrxData vpRecvdData = (HostToWrxData)ipRecvData;
    byte[] vabClobData = vpRecvdData.getMessageBytes();
    if (vabClobData != null)
    {
      /*
       * Insert the row
       */
      StringBuilder vpSql = new StringBuilder("INSERT INTO HostToWRx (")
                 .append("sHostName, ")
                 .append("iMessageSequence, ")
                 .append("iOriginalSequence, ")
                 .append("sMessageIdentifier, ")
                 .append("sMessage, ")
                 .append("iMessageProcessed")
                 .append(") VALUES (?, ?, ?, ?, ?, ?)");

      execute(vpSql.toString(), vpRecvdData.getHostName(),
              vpRecvdData.getMessageSequence(), vpRecvdData.getOriginalMessageSequence(),
              vpRecvdData.getMessageIdentifier(), new DACCLOB(vabClobData),
              vpRecvdData.getMessageProcessed());
    }
  }

 /**
  *  Method to check if a message has been processed.
  *
  *  @param sHostName <code>String</code> containing Host Name.
  *  @param iMessageSequence <code>String</code> containing Message sequence.
  *
  *  @return <code>boolean</code> value of true if message has been processed.
  *  @throws DBException if there is a serious database read error.
  */
  public boolean isMessageProcessed(String sHostName, int iMessageSequence)
         throws DBException
  {
    mpHostToWrxData.clear();
    mpHostToWrxData.setKey(WrxToHostData.MESSAGESEQUENCE_NAME,
      Integer.valueOf(iMessageSequence));
    mpHostToWrxData.setKey(WrxToHostData.HOSTNAME_NAME, sHostName);
    mpHostToWrxData.setKey(WrxToHostData.MESSAGEPROCESSED_NAME,
      Integer.valueOf(DBConstants.YES));
    return(exists(mpHostToWrxData));
  }

 /**
  *  Method to update message status as processed.
  *
  *  @param sHostName <code>String</code> containing Host Name.
  *  @param iMessageSequence <code>String</code> containing Message sequence.
  *  @throws DBException if there is a serious database read error.
  */
  public void markMessageProcessed(String sHostName, int iMessageSequence)
         throws DBException
  {
    mpHostToWrxData.clear();
    mpHostToWrxData.setKey(WrxToHostData.MESSAGESEQUENCE_NAME,
      Integer.valueOf(iMessageSequence));
    mpHostToWrxData.setKey(WrxToHostData.HOSTNAME_NAME, sHostName);

    mpHostToWrxData.setMessageProcessed(DBConstants.YES);
    modifyElement(mpHostToWrxData);
  }

 /**
  *  Method returns oldest processed or unprocessed message from the inbound
  *  data queue.
  *
  *  @param sHostName <code>String</code> containing Host Name.
  *  @param inMessageProcessed <code>int</code> flag indicating if message
  *         to search for is processed or unprocessed.
  *  @param izRetrieveCLOB <code>boolean</code> value of true indicates CLOB
  *         should be retrieved along with other columns. This flag should be
  *         prudently set to true since this entails an expensive operation to
  *         read the CLOB and store it in memory.  The only time this
  *         parameter should be set to true is if the XML message in the CLOB
  *         will be parsed.
  *  @param inWithLock <code>int</code> value of DBConstants.WRITELOCK, or
  *         DBConstants.NOWRITELOCK to indicate reading with, or without locking
  *         record. The caller must open a transaction if this flag is set to
  *         DBConstants.WRITELOCK.
  *  @return <code>AbstractSKDCData</code> containing requested data. <code>null</code>
  *          if no matching records are found.
  *  @throws DBException if there is a serious database read error.
  */
  public AbstractSKDCData getOldestMessage(String sHostName, int inMessageProcessed,
                                           boolean izRetrieveCLOB, int inWithLock)
         throws DBException
  {
    String sHostNameSQL = "";
    if (sHostName.trim().length() != 0)
      sHostNameSQL = "sHostName = \'" + sHostName + "\' AND ";

    StringBuilder vpSql = new StringBuilder();
    if (DBInfo.USING_ORACLE_DB)
    {
      vpSql.append("SELECT * FROM (SELECT * FROM HostToWrx WHERE ")
                 .append(sHostNameSQL)
                 .append("iMessageProcessed = ").append(inMessageProcessed).append(" ")
                 .append("ORDER BY dMessageAddTime, iMessageSequence) ")
                 .append("WHERE ROWNUM < 2");
    }
    else if (DBInfo.USING_SQL_SERVER)
    {
      vpSql.append("SELECT TOP 1 * FROM HostToWrx WHERE ")
                 .append(sHostNameSQL)
                 .append("iMessageProcessed = ").append(inMessageProcessed).append(" ")
                 .append("ORDER BY dMessageAddTime, iMessageSequence");
    }

    List<Map> arrList = fetchRecords(vpSql.toString());

    if (arrList.isEmpty())
    {
      return(null);
    }
    mpHostToWrxData.clear();
    mpHostToWrxData.dataToSKDCData(arrList.get(0));

    if (izRetrieveCLOB)
    {
      // This will retrieve just one record.
      vpSql.setLength(0);
      vpSql.append("SELECT sMessage FROM ").append(getWriteTableName());

      HostToWrxData vpKeyedData = Factory.create(HostToWrxData.class);
      vpKeyedData.setKey(HostToWrxData.HOSTNAME_NAME, mpHostToWrxData.getHostName());
      vpKeyedData.setKey(HostToWrxData.MESSAGEPROCESSED_NAME, inMessageProcessed);
      vpKeyedData.setKey(HostToWrxData.MESSAGESEQUENCE_NAME, mpHostToWrxData.getMessageSequence());

      if (inWithLock == DBConstants.WRITELOCK)
      {
        if (DBInfo.USING_ORACLE_DB)
        {
          vpSql.append(DBHelper.buildWhereClause(vpKeyedData.getKeyArray()))
                     .append(" FOR UPDATE");
        }
        else if (DBInfo.USING_SQL_SERVER)
        {
          vpSql.append(" WITH (UPDLOCK) WHERE ")
                     .append(DBHelper.buildWhereClause(vpKeyedData.getKeyArray(), true));
        }
      }
      else
      {
        vpSql.append(DBHelper.buildWhereClause(vpKeyedData.getKeyArray()));
      }
      byte[] clobBytes = DBHelper.readClob(WrxToHostData.MESSAGE_NAME,
                                         vpSql.toString());
      mpHostToWrxData.setMessageBytes(clobBytes);
    }

    return mpHostToWrxData.clone();
  }

 /**
  *  Method returns most recently added message from the inbound data queue.
  *
  *  @param isHostName <code>String</code> containing Host Name.
  *  @param izRetrieveCLOB <code>boolean</code> value of true indicates CLOB
  *         should be retrieved along with other columns. This flag should be
  *         set to true prudently since this entails an expensive operation to
  *         read the CLOB and store it in memory.  The only time this
  *         parameter should be set to true is if the XML message in the CLOB
  *         will be parsed.
  *  @param inWithLock <code>int</code> value of DBConstants.WRITELOCK, or
  *         DBConstants.NOWRITELOCK to indicate reading with, or without locking
  *         record. The caller must open a transaction if this flag is set to
  *         DBConstants.WRITELOCK.
  *  @return <code>AbstractSKDCData</code> containing requested data.
  *  @throws DBException if there is a serious database read error.
  */
  public AbstractSKDCData getNewestMessage(String isHostName, boolean izRetrieveCLOB,
                                           int inWithLock) throws DBException
  {
    StringBuilder vpSql = new StringBuilder();
    if (DBInfo.USING_ORACLE_DB)
    {
      vpSql.append("SELECT * FROM (SELECT * FROM HostToWrx WHERE ")
                 .append("sHostName = '").append(isHostName).append("' ")
                 .append("ORDER BY dMessageAddTime DESC, iMessageSequence DESC) ")
                 .append("WHERE ROWNUM < 2");
    }
    else if (DBInfo.USING_SQL_SERVER)
    {
      vpSql.append("SELECT TOP 1 * FROM HostToWrx WHERE ")
                 .append("sHostName = '").append(isHostName).append("' ")
                 .append("ORDER BY dMessageAddTime DESC, iMessageSequence DESC");
    }

    List<Map> arrList = fetchRecords(vpSql.toString());
    if (arrList.isEmpty())
    {
      return(null);
    }

    mpHostToWrxData.clear();
    mpHostToWrxData.dataToSKDCData(arrList.get(0));
    if (izRetrieveCLOB)
    {
      vpSql.setLength(0);
                                       // This will retrieve just one record.
      vpSql.append("SELECT sMessage FROM ").append(getWriteTableName());

      HostToWrxData vpKeyedData = Factory.create(HostToWrxData.class);
      vpKeyedData.setKey(HostToWrxData.HOSTNAME_NAME, mpHostToWrxData.getHostName());
      vpKeyedData.setKey(HostToWrxData.MESSAGESEQUENCE_NAME, mpHostToWrxData.getMessageSequence());

      if (inWithLock == DBConstants.WRITELOCK)
      {
        if (DBInfo.USING_ORACLE_DB)
        {
          vpSql.append(DBHelper.buildWhereClause(vpKeyedData.getKeyArray()))
                     .append(" FOR UPDATE");
        }
        else if (DBInfo.USING_SQL_SERVER)
        {
          vpSql.append(" WITH (UPDLOCK) WHERE ")
                     .append(DBHelper.buildWhereClause(vpKeyedData.getKeyArray(), true));
        }
      }
      else
      {
        vpSql.append(DBHelper.buildWhereClause(vpKeyedData.getKeyArray()));
      }

      byte[] vabClobBytes = DBHelper.readClob(WrxToHostData.MESSAGE_NAME,
                                              vpSql.toString());
      mpHostToWrxData.setMessageBytes(vabClobBytes);
    }

    return mpHostToWrxData.clone();
  }



  /**
   *  Method returns a pair of values representing the minimum and maximum
   *  sequence numbers respectively in the inbound host table.
   *
   *  @param isHostName <code>String</code> containing the host name.
   *
   *  @return <code>int[]</code> containing a pair of sequence numbers.  If no
   *          match is found and empty array is returned.
   *  @throws DBException if there is a database access error.
   */
  public int[] getMinMaxSequence(String isHostName) throws DBException
  {
    String vsColumnName = HostToWrxData.MESSAGESEQUENCE_NAME;
    return getMinMaxSequence(isHostName, vsColumnName);
  }

  /**
   *  Method returns a pair of values representing the minimum and maximum
   *  sequence numbers respectively in the inbound host table.
   *
   *  @param isHostName <code>String</code> containing the host name.
   *
   *  @return <code>int[]</code> containing a pair of sequence numbers.  If no
   *          match is found and empty array is returned.
   *  @throws DBException if there is a database access error.
   */
  public int[] getHostMinMaxSequence(String isHostName) throws DBException
  {
    String vsColumnName = HostToWrxData.ORIGINALSQUENCE_NAME;
    return getMinMaxSequence(isHostName, vsColumnName);
  }

  /**
   *  Method returns a pair of values representing the minimum and maximum
   *  sequence numbers respectively in the inbound host table.
   *
   *  @param isHostName <code>String</code> containing the host name.
   *
   *  @return <code>int[]</code> containing a pair of sequence numbers.  If no
   *          match is found and empty array is returned.
   *  @throws DBException if there is a database access error.
   */
  public int[] getMinMaxSequence(String isHostName, String isColumnName) throws DBException
  {
    // This will retrieve just one record.
    StringBuilder vpSql = new StringBuilder("SELECT MIN(").append(isColumnName).append(") AS \"IMINSEQUENCE\", ")
             .append("MAX(").append(isColumnName).append(") AS \"IMAXSEQUENCE\" ")
             .append("FROM ").append(getWriteTableName()).append(" WHERE ")
             .append("sHostName = '").append(isHostName).append("' ");
    List<Map> aList = fetchRecords(vpSql.toString());

    int[] minMax;
    if (!aList.isEmpty())
    {
      minMax = new int[2];
      minMax[0] = DBHelper.getIntegerField(aList.get(0), "IMINSEQUENCE");
      minMax[1] = DBHelper.getIntegerField(aList.get(0), "IMAXSEQUENCE");
    }
    else
    {
      minMax = new int[0];
    }

    return(minMax);
  }

 /**
  *  Sets Objects for garbage collection.
  */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpHostToWrxData = null;
  }

/*===========================================================================
                        Non-Public methods section.
  ===========================================================================*/
  /*===========================================================================
  Non-Public methods section.
===========================================================================*/
  protected void deleteProcessedMessages(String isHostName, int inMessageStatus)
		  throws DBException
  {
	  mpHostToWrxData.clear();
	  mpHostToWrxData.setKey(WrxToHostData.HOSTNAME_NAME, isHostName);
	  mpHostToWrxData.setKey(WrxToHostData.MESSAGEPROCESSED_NAME,
     Integer.valueOf(inMessageStatus), KeyObject.NOT_EQUAL);

	  try
	  {
		  setMaxRows(HOST_MAX_ROWS);
		  deleteHostToWRxEntries(HOST_MAX_ROWS, isHostName);
		  //	deleteElement(mpHostToWrxData);
		  setMaxRows();
	  }
	  catch(NoSuchElementException e)
	  {
		  throw new DBException("Error deleting HostToWRx table entry...", e);
	  }
  }


  /**
   * Delete all Item masters that have iDeleteAtZeroQuantity set to Yes, and
   * have no other database table dependencies.
   */
  public void deleteHostToWRxEntries(int numToDelete, String sHostName) throws DBException
  {
	  String s = "WITH T AS (SELECT TOP " + numToDelete + " * FROM HOSTTOWRX WHERE sHostName = '" + sHostName 
			  + "' AND iMessageProcessed != " + DBConstants.NO + " ORDER BY dMessageAddTime ) DELETE FROM T ";

	  try
	  {
		  execute(s, (Object[])null);
	  }
	  catch (NoSuchElementException e)
	  {
		  // We don't care if we did not delete any rows
	  }
  }
  
  /**
   * This method will try to find a valid sequence number slot that is not being
   * used.  In theory there should always be a slot open since this table should
   * be cleaned by the time the sequence number rollover point is reached.
   * @param isHostName the host name to search by.
   * @return a usable sequence number.
   * @throws DBException if there is a record modify, or delete error.
   */
   public int getNextSequenceNumber(String isHostName) throws DBException
   {
   WrxSequencer vpSequencer = new WrxSequencer();
    String vsSequencerName = isHostName + HostToWrx.INBOUND_SEQUENCER;
    int vnMaxPossibleMessages = vpSequencer.getMaxSequencerValue(vsSequencerName) - vpSequencer.getMinSequencerValue(vsSequencerName) + 1;
    mpHostToWrxData.clear();
    mpHostToWrxData.setKey(WrxToHostData.HOSTNAME_NAME, isHostName);

   
    int vnTotalMessageCount = getCount(mpHostToWrxData);
    if (vnTotalMessageCount >= vnMaxPossibleMessages)
    {
/*---------------------------------------------------------------------------
   If there isn't any room left in the outbound table, delete all messages that
   are marked as Processed to make room.  If all messages are Unprocessed,
   throw an exception so that the message adder is made aware of it.  If the
   message adder is coded correctly then it should rollback the transaction and
   stop the business rule from completing until this situation is resolved. This
   case will only occur if the Host comm. is down for a lengthy period of time
   and messages can't accumulate anymore on the Warehouse Rx side.
  ---------------------------------------------------------------------------*/
      mpHostToWrxData.clear();
      mpHostToWrxData.setKey(WrxToHostData.HOSTNAME_NAME, isHostName);
      mpHostToWrxData.setKey(WrxToHostData.MESSAGEPROCESSED_NAME,
        Integer.valueOf(DBConstants.NO));
      if (getCount(mpHostToWrxData) == vnMaxPossibleMessages)
      {
        DBException vpExc = new DBException("No more message slots open in WRxToHost Table...");
        vpExc.setErrorCode(HostError.DATA_QUEUE_FULL);
        throw vpExc;
      }

      // deleting NOT EQUAL to NO
      deleteProcessedMessages(isHostName, DBConstants.NO);
    }

    return getUnusedSequence(isHostName, vsSequencerName, vpSequencer);
  }
  
  
  /**
   * This method will try to find a valid sequence number slot that is not being
   * used.  In theory there should always be a slot open since this table should
   * be cleaned by the time the sequence number rollover point is reached.  It is
   * also recommended that the rollover point be left as unspecified in which case
   * it will go as high as Integer.MAX_VALUE-1.
   * @param isHostName the host name to search by.
   * @param ipSequencer reference to a sequencer object.
   * @return a usable sequence number.
   * @throws DBException
   */
   protected int getUnusedSequence(String isHostName, String isSequencerName, WrxSequencer ipSequence)
             throws DBException
   {
     int vnCurrSeq = -1;
     boolean vzUnassigned = true;
     while(vzUnassigned)
     {
       vnCurrSeq = ipSequence.getNextSequencerValue(isSequencerName);
       mpHostToWrxData.clear();
       mpHostToWrxData.setKey(WrxToHostData.MESSAGESEQUENCE_NAME,
         Integer.valueOf(vnCurrSeq));
       mpHostToWrxData.setKey(WrxToHostData.HOSTNAME_NAME, isHostName);
       if (!exists(mpHostToWrxData))
       {
         vzUnassigned = false;
       }
     }
     return(vnCurrSeq);
   }


/**
   * Method gets records with selected columns specified in ColumnObjects
   * contained in {@link AbstractSKDCData AbstractSKDCData}
   * @param ipData Data object representing columns and search keys.
   * @return List of Maps with each Map being the DB Row that was read.
   * @throws DBException if there is a database access error.
   */
  public List<Map> getSelectedColumnElementsForDeletion(AbstractSKDCData ipData)
         throws DBException
  {
    List<Map> vpRtnList;
    ColumnObject[] vpSelectColumns = ipData.getColumnArray();
    KeyObject[] vpKeys = ipData.getKeyArray();

    setMaxRows(Integer.MAX_VALUE);
    if (vpSelectColumns.length == 0)
      vpRtnList = getAllElements(ipData);
    else
      vpRtnList = readAllData(vpSelectColumns, vpKeys, "HostToWRx",
          DBHelper.buildOrderByClause(ipData.getOrderByColumns()));

    setMaxRows();
    return vpRtnList;
  }
}
