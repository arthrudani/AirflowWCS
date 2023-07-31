package com.daifukuoc.wrxj.custom.ebs.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.WrxSequencer;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.messages.HostError;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DACCLOB;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSConstants;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSDBConstants;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Description:<BR>
 *  Class to handle WrxToPlc table operations.
 *
 * @author       A.D.
 * @version      1.0   02/08/2005
 */
public class WrxToPlc extends BaseDBInterface
{
 /** The name of the Outbound Port sequencer. */
  public static final String OUTBOUND_SEQUENCER = "WrxToPlc";
  private static final int RECORD_MAX_ROWS = 2000;

  protected WrxToPlcData mpWrxToPlcData;

  public WrxToPlc()
  {
    this("WrxToPlc");
  }

  public WrxToPlc(String isObjectName)
  {
    super(isObjectName);
    mpWrxToPlcData = Factory.create(WrxToPlcData.class);
  }

  /**
   * {@inheritDoc}
   * @param ipData
   * @param inWithLock
   */
  @Override
  public <Type extends AbstractSKDCData> Type getElement(Type ipData, int inWithLock)
         throws DBException
  {
    Type vpRtnData = super.getElement(ipData, inWithLock);
    if (vpRtnData != null && ((WrxToPlcData)ipData).getClobRetrieval())
    {
      WrxToPlcData vpLocalData = (WrxToPlcData)vpRtnData;
                                       // This will retrieve just one record.
      StringBuilder vpSql = new StringBuilder("SELECT sMessage FROM ").append(getWriteTableName()).append(" WHERE ")
               .append("sPortName = '").append(vpLocalData.getPortName()).append("' AND ")
               .append("iMessageProcessed = ").append(vpLocalData.getMessageProcessed()).append(" AND ")
               .append("iMessageSequence = ").append(vpLocalData.getMessageSequence());
      byte[] clobBytes = DBHelper.readClob(WrxToPlcData.MESSAGE_NAME,
                                           vpSql.toString());
      vpLocalData.setMessageBytes(clobBytes);
    }

    return vpRtnData;
  }

  /**
   * {@inheritDoc} This method will order the list from the oldest to the newest
   * data. <b>Note:</b> this method does <u>not</u> return any CLOB data.
   *
   * @param {@inheritDoc}
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
   * {@inheritDoc} This method is meant to be used to add records to the
   * WrxToPlc table only.  This method must be called from within a transaction.
   *
   * @param {@inheritDoc}
   * @throws DBException for database errors, or stream IO errors when writing
   *             to the CLOB.
   */
  @Override
  public void addElement(AbstractSKDCData eskdata) throws DBException
  {
    WrxToPlcData vpSendData = (WrxToPlcData)eskdata;
    StringBuilder vpSql = new StringBuilder("INSERT INTO WrxToPlc ")
               .append("(sPortName, iMessageSequence, sMessageIdentifier, sMessage) ")
               .append("VALUES(?, ?, ?, ?)");
    execute(vpSql.toString(), vpSendData.getPortName(), vpSendData.getMessageSequence(),
            vpSendData.getMessageIdentifier(), new DACCLOB(vpSendData.getMessageBytes()));
  }

 /**
  *  Method to check if a message has been processed.
  *
  *  @param sPortName <code>String</code> containing Port Name.
  *  @param iMessageSequence <code>String</code> containing Message sequence.
  *
  *  @return <code>boolean</code> value of true if message has been processed.
  *  @throws DBException if there is a serious database read error.
  */
  public boolean isMessageProcessed(String sPortName, int iMessageSequence)
         throws DBException
  {
    mpWrxToPlcData.clear();
    mpWrxToPlcData.setKey(WrxToPlcData.MESSAGESEQUENCE_NAME,
                        new Integer(iMessageSequence));
    mpWrxToPlcData.setKey(WrxToPlcData.PORTNAME_NAME, sPortName);
    mpWrxToPlcData.setKey(WrxToPlcData.MESSAGEPROCESSED_NAME,
                        new Integer(DBConstants.YES));
    return(exists(mpWrxToPlcData));
  }

 /**
  *  Method to update message status as processed.
  *
  *  @param sPortName <code>String</code> containing Port Name.
  *  @param iMessageSequence <code>String</code> containing Message sequence.
  *  @throws DBException if there is a serious database read error.
  */
  public void markMessageProcessed(String sPortName, int iMessageSequence)
         throws DBException
  {
	  mpWrxToPlcData.clear();
	  mpWrxToPlcData.setKey(WrxToPlcData.MESSAGESEQUENCE_NAME,
                        new Integer(iMessageSequence));
	  mpWrxToPlcData.setKey(WrxToPlcData.PORTNAME_NAME, sPortName);

	  mpWrxToPlcData.setMessageProcessed(DBConstants.YES);
    modifyElement(mpWrxToPlcData);
  }

 /**
  *  Method returns oldest processed or unprocessed message from the outbound
  *  data queue.
  *
  *  @param isPortName <code>String</code> containing Port Name.
  *  @param inMessageProcessed <code>int</code> flag indicating if message
  *         to search for is processed or unprocessed.
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
  public AbstractSKDCData getOldestMessage(String isPortName, int inMessageProcessed,
                                           boolean izRetrieveCLOB, int inWithLock)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder();
    if (DBInfo.USING_ORACLE_DB)
    {
      vpSql.append("SELECT * FROM (SELECT * FROM ").append(getWriteTableName()).append(" WHERE ")
                 .append("sPortName = '").append(isPortName).append("' AND ")
                 .append("iMessageProcessed = ").append(inMessageProcessed).append(" ")
                 .append("ORDER BY dMessageAddTime, iMessageSequence) ")
                 .append("WHERE ROWNUM < 2");
    }
    else if (DBInfo.USING_SQL_SERVER)
    {
      vpSql.append("SELECT TOP 1 * FROM ").append(getWriteTableName()).append(" WHERE ")
                 .append("sPortName = '").append(isPortName).append("' AND ")
                 .append("iMessageProcessed = ").append(inMessageProcessed).append(" ")
                 .append("ORDER BY dMessageAddTime, iMessageSequence");
    }
    List<Map> arrList = fetchRecords(vpSql.toString());

    if (arrList.isEmpty())
    {
      return(null);
    }

    mpWrxToPlcData.clear();
    mpWrxToPlcData.dataToSKDCData(arrList.get(0));
    if (izRetrieveCLOB)
    {
      vpSql.setLength(0);
                                       // This will retrieve just one record.
      vpSql.append("SELECT sMessage FROM ").append(getWriteTableName());

      WrxToPlcData vpKeyedData = Factory.create(WrxToPlcData.class);
      vpKeyedData.setKey(WrxToPlcData.PORTNAME_NAME, isPortName);
      vpKeyedData.setKey(WrxToPlcData.MESSAGEPROCESSED_NAME, inMessageProcessed);
      vpKeyedData.setKey(WrxToPlcData.MESSAGESEQUENCE_NAME, mpWrxToPlcData.getMessageSequence());
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
      byte[] vabClobBytes = DBHelper.readClob(WrxToPlcData.MESSAGE_NAME,
                                             vpSql.toString());
      mpWrxToPlcData.setMessageBytes(vabClobBytes);
    }

    return mpWrxToPlcData.clone();
  }

 /**
  *  Method returns newest processed or unprocessed message from the outbound
  *  data queue.
  *
  *  @param isPortName <code>String</code> containing Port Name.
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
  public AbstractSKDCData getNewestMessage(String isPortName, boolean izRetrieveCLOB,
                                           int inWithLock) throws DBException
  {
    StringBuilder vpSql = new StringBuilder();
    if (DBInfo.USING_ORACLE_DB)
    {
      vpSql.append("SELECT * FROM (SELECT * FROM ").append(getWriteTableName()).append(" WHERE ")
                 .append("sPortName = '").append(isPortName).append("' ")
                 .append("ORDER BY dMessageAddTime DESC, iMessageSequence DESC) ")
                 .append("WHERE ROWNUM < 2");
    }
    else if (DBInfo.USING_SQL_SERVER)
    {
      vpSql.append("SELECT TOP 1 * FROM ").append(getWriteTableName()).append(" WHERE ")
                 .append("sPortName = '").append(isPortName).append("' ")
                 .append("ORDER BY dMessageAddTime DESC, iMessageSequence DESC");
    }

    List<Map> arrList = fetchRecords(vpSql.toString());
    if (arrList.isEmpty())
    {
      return(null);
    }

    mpWrxToPlcData.clear();
    mpWrxToPlcData.dataToSKDCData(arrList.get(0));
    if (izRetrieveCLOB)
    {
      vpSql.setLength(0);
                                       // This will retrieve just one record.
      vpSql.append("SELECT sMessage FROM ").append(getWriteTableName());

      WrxToPlcData vpKeyedData = Factory.create(WrxToPlcData.class);
      vpKeyedData.setKey(PlcToWrxData.PORTNAME_NAME, mpWrxToPlcData.getPortName());
      vpKeyedData.setKey(PlcToWrxData.MESSAGESEQUENCE_NAME, mpWrxToPlcData.getMessageSequence());

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
      byte[] vabClobBytes = DBHelper.readClob(WrxToPlcData.MESSAGE_NAME,
                                              vpSql.toString());
      mpWrxToPlcData.setMessageBytes(vabClobBytes);
    }

    return mpWrxToPlcData.clone();
  }


 /**
  *  Method returns a pair of values representing the minimum and maximum
  *  sequence numbers respectively in the inbound Port table.
  *
  *  @param sPortname <code>String</code> containing the Port name.
  *
  *  @return <code>int[]</code> containing a pair of sequence numbers.  If no
  *          match is found and empty array is returned.
  *  @throws DBException if there is a database access error.
  */
  public int[] getMinMaxSequence(String sPortname) throws DBException
  {
                                     // This will retrieve just one record.
    StringBuilder vpSql = new StringBuilder("SELECT MIN(iMessageSequence) AS \"IMINSEQUENCE\", ")
             .append("MAX(iMessageSequence) AS \"IMAXSEQUENCE\" ")
             .append("FROM ").append(getWriteTableName()).append(" WHERE ")
             .append("sPortName = '").append(sPortname).append("' ");
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
    mpWrxToPlcData = null;
  }

/*===========================================================================
                        Non-Public methods section.
  ===========================================================================*/
  public void deleteProcessedMessages(String isPortName, int inMessageStatus)
          throws DBException
  {
	  mpWrxToPlcData.clear();
	  mpWrxToPlcData.setKey(WrxToPlcData.PORTNAME_NAME, isPortName);
	  mpWrxToPlcData.setKey(WrxToPlcData.MESSAGEPROCESSED_NAME,
                         new Integer(inMessageStatus), KeyObject.NOT_EQUAL);
  
	  try
	  {
		  setMaxRows(RECORD_MAX_ROWS);
		  deleteWRxToPlcEntries(RECORD_MAX_ROWS, isPortName);
		  setMaxRows();
	  }
	  catch(NoSuchElementException e)
	  {
		  // Ok to ignore this.  It simply means there were no rows matching the
		  // specified criteria to delete.
    
		  // throw new DBException("Error deleting PortToWRx table entry...", e);
	  }
  }

  public void deleteWRxToPlcEntries(int numToDelete, String sPortName) throws DBException
  {
	  String s = "WITH T AS (SELECT TOP " + numToDelete + " * FROM WRXTOPLC WHERE sPortName = '" + sPortName 
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
  * be cleaned by the time the sequence number rollover point is reached.  It is
  * also recommended that the rollover point be left as unspecified in which case
  * it will go as high as Integer.MAX_VALUE-1.
  * @param isPortName the Port name to search by.
  * @param ipSequencer reference to a sequencer object.
  * @return a usable sequence number.
  * @throws DBException
  */
  protected int getUnusedSequence(String isPortName, WrxSequencer ipSequencer)
            throws DBException
  {
    int vnCurrSeq = ipSequencer.changeSequenceNumber(WrxToPlc.OUTBOUND_SEQUENCER, isPortName,
                                                     EBSDBConstants.WrxToPlc.SEQUENCE.PORT_SEQ);
    mpWrxToPlcData.clear();
    mpWrxToPlcData.setKey(WrxToPlcData.MESSAGESEQUENCE_NAME,
                         new Integer(vnCurrSeq));
    mpWrxToPlcData.setKey(WrxToPlcData.PORTNAME_NAME, isPortName);
    if (exists(mpWrxToPlcData))
    {
      vnCurrSeq = getUnusedSequence(isPortName, ipSequencer);
    }

    return(vnCurrSeq);
  }

  /**
   * This method will try to find a valid sequence number slot that is not being
   * used.  In theory there should always be a slot open since this table should
   * be cleaned by the time the sequence number rollover point is reached.
   * @param isPortName the Port name to search by.
   * @return a usable sequence number.
   * @throws DBException if there is a record modify, or delete error.
   */
   public int getNextSequenceNumber(String isPortName) throws DBException
   {
    WrxSequencer vpSequencer = new WrxSequencer();
    String vsSequencerName = isPortName + WrxToPlc.OUTBOUND_SEQUENCER;
    int vnMaxPossibleMessages = vpSequencer.getMaxSequencerValue(vsSequencerName) - vpSequencer.getMinSequencerValue(vsSequencerName) + 1;

    mpWrxToPlcData.clear();
    mpWrxToPlcData.setKey(WrxToPlcData.PORTNAME_NAME, isPortName);


    int vnTotalMessageCount = getCount(mpWrxToPlcData);
    if (vnTotalMessageCount >= vnMaxPossibleMessages)
    {
/*---------------------------------------------------------------------------
   If there isn't any room left in the outbound table, delete all messages that
   are marked as Processed to make room.  If all messages are Unprocessed,
   throw an exception so that the message adder is made aware of it.  If the
   message adder is coded correctly then it should rollback the transaction and
   stop the business rule from completing until this situation is resolved. This
   case will only occur if the Port comm. is down for a lengthy period of time
   and messages can't accumulate anymore on the Warehouse Rx side.
  ---------------------------------------------------------------------------*/
      mpWrxToPlcData.clear();
      mpWrxToPlcData.setKey(WrxToPlcData.PORTNAME_NAME, isPortName);
      mpWrxToPlcData.setKey(WrxToPlcData.MESSAGEPROCESSED_NAME,
                             new Integer(DBConstants.NO));
      if (getCount(mpWrxToPlcData) == vnMaxPossibleMessages)
      {
        DBException vpExc = new DBException("No more message slots open in WRxToPlc Table...");
        vpExc.setErrorCode(HostError.DATA_QUEUE_FULL);
        throw vpExc;
      }

      // deleting not equal to NO
      deleteProcessedMessages(isPortName, DBConstants.NO);
    }

    return getUnusedSequence(isPortName, vsSequencerName, vpSequencer);
  }


  /**
   * This method will try to find a valid sequence number slot that is not being
   * used.  In theory there should always be a slot open since this table should
   * be cleaned by the time the sequence number rollover point is reached.  It is
   * also recommended that the rollover point be left as unspecified in which case
   * it will go as high as Integer.MAX_VALUE-1.
   * @param isPortName the Port name to search by.
   * @param ipSequencer reference to a sequencer object.
   * @return a usable sequence number.
   * @throws DBException
   */
   protected int getUnusedSequence(String isPortName, String isSequencerName, WrxSequencer ipSequence)
             throws DBException
   {
     int vnCurrSeq = -1;
     boolean vzUnassigned = true;
     while(vzUnassigned)
     {
       vnCurrSeq = ipSequence.getNextSequencerValue(isSequencerName);
       mpWrxToPlcData.clear();
       mpWrxToPlcData.setKey(WrxToPlcData.MESSAGESEQUENCE_NAME,
                          new Integer(vnCurrSeq));
       mpWrxToPlcData.setKey(WrxToPlcData.PORTNAME_NAME, isPortName);
       if (!exists(mpWrxToPlcData))
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
       vpRtnList = readAllData(vpSelectColumns, vpKeys, "WRxToPlc",
           DBHelper.buildOrderByClause(ipData.getOrderByColumns()));

     setMaxRows();
     return vpRtnList;
   }
}
