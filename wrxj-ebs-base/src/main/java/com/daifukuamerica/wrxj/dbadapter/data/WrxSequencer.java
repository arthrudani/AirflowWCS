package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.DBRuntimeException;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Description:<BR>
 *   Class for handling WrxSequencer table interactions.
 *
 * @author       A.D.
 * @version      1.0     10-Feb-05
 */
public class WrxSequencer extends BaseDBInterface
{
  /*
   * Hard-coded SQL for changeSequenceNumber(). Provided for performance
   * reasons.
   */
  private static final String GET_SEQ_QUERY_ORACLE = "SELECT * FROM WrxSequencer WHERE " +
                                                     "sSequenceIdentifier = ? AND "      +
                                                     "sEndDeviceName = ? AND "           +
                                                     "iSequenceType = ? FOR UPDATE";

  private static final String GET_SEQ_QUERY_SQLSERV= "SELECT * FROM WrxSequencer "       +
                                                     "WITH (UPDLOCK) WHERE  "            +
                                                     "sSequenceIdentifier = ? AND "      +
                                                     "sEndDeviceName = ? AND "           +
                                                     "iSequenceType = ?";

  private static final String UPDATE_SEQUENCE_SQL = "UPDATE WrxSequencer SET "     +
                                                    "iSequenceNumber = ? WHERE "   +
                                                    "sSequenceIdentifier = ? AND " +
                                                    "sEndDeviceName = ? AND "      +
                                                    "iSequenceType = ?";
  private static final String NEXT_SEQ_QUERY_SQLSERV = "SELECT NEXT VALUE FOR ";
  private static final String MIN_SEQ_QUERY_SQLSERV = "SELECT CAST("
	      + "MINIMUM_VALUE as int) as MINVAL FROM"
	      + " sys.sequences WHERE name =?";
  private static final String MAX_SEQ_QUERY_SQLSERV = "SELECT CAST("
      + "MAXIMUM_VALUE as int) as MAXVAL FROM"
      + " sys.sequences WHERE name =?";
  private static final String NEXT_SEQ_QUERY_ORACLE = ".NEXTVAL FROM DUAL";
  private static final String MIN_SEQ_QUERY_ORACLE = "SELECT MIN_VALUE AS MINVAL FROM "
	      + "USER_SEQUENCES WHERE SEQUENCE_NAME = ?";
  private static final String MAX_SEQ_QUERY_ORACLE = "SELECT MAX_VALUE AS MAXVAL FROM "
      + "USER_SEQUENCES WHERE SEQUENCE_NAME = ?";
  private static final String MINVAL_COL_NAME = "MINVAL";
  private static final String MAXVAL_COL_NAME = "MAXVAL";
  private static final String NEXTVAL_COL_NAME =  "NEXTVAL";

  /*
   * End hard-coded SQL
   */

  private WrxSequencerData mpSeqData;

  public WrxSequencer()
  {
    super("WrxSequencer");
    mpSeqData = Factory.create(WrxSequencerData.class);
  }

  /**
   * Method changes the EndDeviceName
   * @param isOldEndDevice the old endDeviceName
   * @param isNewEndDevice the new endDeviceName
   * @throws DBException
   */
  public void updateEndDevice(String isOldEndDevice, String isNewEndDevice)throws DBException
  {
    mpSeqData.clear();
    mpSeqData.setKey(WrxSequencerData.ENDDEVICENAME_NAME, isOldEndDevice);
    mpSeqData.setEndDeviceName(isNewEndDevice);
    modifyElement(mpSeqData);
  }

  /**
   * Method to modify a sequencer with a user specified sequence value.
   * @param isSequenceIdentifier string containing unique sequence id.
   * @param isEndDeviceName <code>String</code> name of the end device (such as
   *        the host name).
   * @param inSequenceType <code>translation</code> of sequence type.
   * @return the sequencer record.
   * @throws DBException if there is a database access error.
   */
  public void setNewSequenceValue(String isSequenceIdentifier,
                                  String isEndDeviceName, int inSequenceType,
                                  int inNewValue) throws DBException
  {
    mpSeqData.clear();
    mpSeqData.setKey(WrxSequencerData.SEQUENCEIDENTIFIER_NAME, isSequenceIdentifier);
    mpSeqData.setKey(WrxSequencerData.ENDDEVICENAME_NAME, isEndDeviceName);
    mpSeqData.setKey(WrxSequencerData.SEQUENCETYPE_NAME, Integer.valueOf(inSequenceType));
    mpSeqData.setSequenceNumber(inNewValue);
    modifyElement(mpSeqData);
  }

  /**
   * Method to retrieve the current sequence number. If the writeLock flag is
   * set to DBConstants.WRITELOCK, then it is assumed that the caller is in a
   * transaction, and/or will update the record later on.
   *
   * @param sSequenceIdentifier string containing unique sequence id.
   * @param sEndDeviceName <code>String</code> name of the end device (such as
   *        the host name).
   * @param iSequenceType <code>translation</code> of sequence type.
   * @param withLock flag indicating if record should be locked.
   * @return int of current sequence number <strong>previous</strong> to any
   *         increment or decrement.
   *  @throws DBException for serious DB access errors.
   *  @throws DBRuntimeException if no records are found.
   */
  public int getCurrentSequenceNumber(String sSequenceIdentifier,
      String sEndDeviceName, int iSequenceType, int withLock)
      throws DBRuntimeException, DBException
  {
    mpSeqData.clear();
    mpSeqData.setKey(WrxSequencerData.SEQUENCEIDENTIFIER_NAME, sSequenceIdentifier);
    mpSeqData.setKey(WrxSequencerData.ENDDEVICENAME_NAME, sEndDeviceName);
    mpSeqData.setKey(WrxSequencerData.SEQUENCETYPE_NAME, Integer.valueOf(iSequenceType));

    StringBuilder vpSql = new StringBuilder("SELECT ")
             .append(WrxSequencerData.SEQUENCENUMBER_NAME)
             .append(" FROM WrxSequencer");

    if (withLock == DBConstants.WRITELOCK)
    {
      if (DBInfo.USING_ORACLE_DB)
      {
        vpSql.append(DBHelper.buildWhereClause(mpSeqData.getKeyArray()));
        vpSql.append(" FOR UPDATE");
      }
      else if (DBInfo.USING_SQL_SERVER)
      {
        vpSql.append(" WITH (UPDLOCK) WHERE ")
                   .append(DBHelper.buildWhereClause(mpSeqData.getKeyArray(), true));
      }
    }
    else
    {
      vpSql.append(DBHelper.buildWhereClause(mpSeqData.getKeyArray()));
    }

    int  seqNumber;
    List<Map> arr_list = fetchRecords(vpSql.toString());

    if (!arr_list.isEmpty())
    {
      seqNumber = DBHelper.getIntegerField(arr_list.get(0),
          WrxSequencerData.SEQUENCENUMBER_NAME);
    }
    else
    {
      throw new DBRuntimeException("Sequence number not found for "
          + sSequenceIdentifier);
    }

    return(seqNumber);
  }

  /**
   *  Method to retrieve all distinct end-device names given a sequence type.
   *
   *  @param iSequenceType translation of sequence type.
   *  @return String array containing end-device names. Empty array if no records
   *          are found.
   *  @throws DBException for serious DB access errors.
   *  @throws DBRuntimeException if no records are found.
   */
  public String[] getEndDeviceNames(int iSequenceType)
         throws DBRuntimeException, DBException
  {
    mpSeqData.clear();
    mpSeqData.setKey(WrxSequencerData.SEQUENCETYPE_NAME, Integer.valueOf(iSequenceType));

    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT ")
             .append(WrxSequencerData.ENDDEVICENAME_NAME)
             .append(" FROM WrxSequencer")
             .append(DBHelper.buildWhereClause(mpSeqData.getKeyArray()));

    return(SKDCUtility.toStringArray(fetchRecords(vpSql.toString()), WrxSequencerData.ENDDEVICENAME_NAME));
  }

  /**
   *  Method increments, or decrements the current sequence number.
   *  <strong>Note:</strong> this method must be called from within a
   *  transaction.  This method checks the rollover point when calculating the
   *  new sequence number.
   *
   *  @param sSequenceIdentifier <code>String</code> Name of the sequencer.
   *  @param sEndDeviceName <code>String</code> name of the end device (such as
   *         the host name).
   *  @param iSequenceType <code>translation</code> of sequence type.
   *  @return Updated sequence number.
   *  @throws DBException if a DB error.
   */
  public int changeSequenceNumber(String sSequenceIdentifier,
      String sEndDeviceName, int iSequenceType) throws DBException
  {
    StringBuilder vpSql = new StringBuilder();
    if (DBInfo.USING_ORACLE_DB)
      vpSql.append(GET_SEQ_QUERY_ORACLE);
    else if (DBInfo.USING_SQL_SERVER)
      vpSql.append(GET_SEQ_QUERY_SQLSERV);

    WrxSequencerData vpSeqData;

    vpSeqData = Factory.create(WrxSequencerData.class);
    List<Map> vpResults = fetchRecords(vpSql.toString(),
        sSequenceIdentifier, sEndDeviceName, iSequenceType);
    vpSeqData.dataToSKDCData(vpResults.get(0));

    int iIncrement = vpSeqData.getIncrementFactor();
    int iRolloverPoint = vpSeqData.getRestartValue();
    int iNewSequence =  vpSeqData.getSequenceNumber() + iIncrement;

    if (iRolloverPoint == 0)           // Restart value not specified case.
    {
      iRolloverPoint = (iIncrement < 0) ? Integer.MIN_VALUE+1 : Integer.MAX_VALUE-1;
    }

    if (iNewSequence > iRolloverPoint)
    {
      iNewSequence = vpSeqData.getStartValue();
    }

    try
    {
      execute(UPDATE_SEQUENCE_SQL, iNewSequence, sSequenceIdentifier,
          sEndDeviceName, iSequenceType);
    }
    catch (NoSuchElementException e)
    {
      String errStr = "Modify failed using key: " + sSequenceIdentifier
      + " + " + vpSeqData.getSequenceNumber();
      throw new DBException(errStr, e);
    }
    return iNewSequence;
  }

  /**
   * Method will reset the sequence number to start value defined in the
   * sequencer record on user demand (instead of waiting for min. or max. value
   * to be hit).
   *
   * @param sSequenceIdentifier the sequencer's name.
   * @throws DBException if a DB error.
   */
  public void resetSequenceNumber(String sSequenceIdentifier,
      String sEndDeviceName, int iSequenceType) throws DBException
  {
    mpSeqData.clear();
    mpSeqData.setKey(WrxSequencerData.SEQUENCEIDENTIFIER_NAME, sSequenceIdentifier);
    mpSeqData.setKey(WrxSequencerData.ENDDEVICENAME_NAME, sEndDeviceName);
    mpSeqData.setKey(WrxSequencerData.SEQUENCETYPE_NAME, Integer.valueOf(iSequenceType));
    WrxSequencerData seqData = getElement(mpSeqData, DBConstants.WRITELOCK);
    mpSeqData.setSequenceNumber(seqData.getStartValue());

    try
    {
      modifyElement(mpSeqData);
    }
    catch(NoSuchElementException e)
    {
      String errStr = "Modify failed using key: " + sSequenceIdentifier + " + " +
                      seqData.getSequenceNumber();
      throw new DBException(errStr, e);
    }
  }

  /**
   * Method gets the initial value for this sequencer.
   *
   * @param isSequenceIdentifier <code>String</code> Name of the sequencer.
   * @param isEndDeviceName <code>String</code> name of the end device (such
   *            as the host name).
   * @param inSequenceType <code>translation</code> of sequence type.
   * @return The restart point for this sequencer.
   * @throws DBException for a database access error.
   */
  public int getStartValue(String isSequenceIdentifier, String isEndDeviceName,
      int inSequenceType) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT iStartValue FROM ").append(getWriteTableName())
             .append(" WHERE sSequenceIdentifier = '")
             .append(isSequenceIdentifier).append("' AND sEndDeviceName = '")
             .append(isEndDeviceName).append("' AND iSequenceType = ")
             .append(inSequenceType);

    return getIntegerColumn(WrxSequencerData.STARTVALUE_NAME, vpSql.toString());
  }

  /**
   * Method gets the restart value or rollover point value for this sequencer.
   * @param sSequenceIdentifier <code>String</code> Name of the sequencer.
   * @param sEndDeviceName <code>String</code> name of the end device (such as
   *        the host name).
   * @param iSequenceType <code>translation</code> of sequence type.
   * @return The restart point for this sequencer.
   * @throws DBException for a database access error.
   */
  public int getRestartValue(String sSequenceIdentifier, String sEndDeviceName,
                             int iSequenceType) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT iRestartValue FROM ").append(getWriteTableName())
             .append(" WHERE sSequenceIdentifier = '")
             .append(sSequenceIdentifier).append("' AND sEndDeviceName = '")
             .append(sEndDeviceName).append("' AND iSequenceType = ")
             .append(iSequenceType);

    return getIntegerColumn(WrxSequencerData.RESTARTVALUE_NAME, vpSql.toString());
  }
  
  /**
   * Method returns the max value of a database sequencer
   * @param isSequencerName
   * @return
   * @throws DBException
   */
  public int getMaxSequencerValue(String isSequencerName) throws DBException
  {
    int vnMaxNum = -1;
    if (DBInfo.USING_SQL_SERVER)
    {
      vnMaxNum = getIntegerColumnWithoutMaxRows(MAXVAL_COL_NAME, MAX_SEQ_QUERY_SQLSERV,
          isSequencerName);
    }
    else if (DBInfo.USING_ORACLE_DB)
    {
       vnMaxNum =  getIntegerColumnWithoutMaxRows(MAXVAL_COL_NAME , MAX_SEQ_QUERY_ORACLE,
           isSequencerName.toUpperCase());
    }
    if(vnMaxNum < 0)
    {
      throw new DBException(
          "DB exception getting sequencer " + isSequencerName +  " max value.");
    }
    return vnMaxNum;
  }
  
  /**
   * Method returns the min value of a database sequencer
   * @param isSequencerName
   * @return
   * @throws DBException
   */
  public int getMinSequencerValue(String isSequencerName) throws DBException
  {
    int vnMaxNum = -1;
    if (DBInfo.USING_SQL_SERVER)
    {
      vnMaxNum = getIntegerColumnWithoutMaxRows(MINVAL_COL_NAME, MIN_SEQ_QUERY_SQLSERV,
          isSequencerName);
    }
    else if (DBInfo.USING_ORACLE_DB)
    {
       vnMaxNum =  getIntegerColumnWithoutMaxRows(MINVAL_COL_NAME , MIN_SEQ_QUERY_ORACLE,
           isSequencerName.toUpperCase());
    }
    if(vnMaxNum < 0)
    {
      throw new DBException(
          "DB exception getting sequencer " + isSequencerName +  " min value.");
    }
    return vnMaxNum;
  }
  
  
  /**
   * Method returns the next value of a database sequencer
   * @param isSequencerName
   * @return int vnNextSequenceNum
   * @throws DBException
   */
  public int getNextSequencerValue(String isSequencerName) throws DBException
  {
    int vnNextSequenceNum = -1;
    if (DBInfo.USING_SQL_SERVER)
    {
      StringBuilder vpSql = new StringBuilder(NEXT_SEQ_QUERY_SQLSERV)
          .append(isSequencerName)
          .append(" " )
          .append(NEXTVAL_COL_NAME);
       vnNextSequenceNum = getIntegerColumnWithoutMaxRows(NEXTVAL_COL_NAME, vpSql.toString());
    }
    else if (DBInfo.USING_ORACLE_DB)
    {
      StringBuilder vpSql = new StringBuilder("SELECT ")
          .append(isSequencerName)
          .append(NEXT_SEQ_QUERY_ORACLE);
      vnNextSequenceNum = getIntegerColumnWithoutMaxRows(NEXTVAL_COL_NAME, vpSql.toString());
    }
    return vnNextSequenceNum;
  }


  /**
   *  Sets Objects for garbage collection.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpSeqData = null;
  }
}
