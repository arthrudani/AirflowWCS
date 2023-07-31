package com.daifukuamerica.wrxj.host;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.dbadapter.data.WrxToHostData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DACCLOB;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.jdbc.DBResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * {@inheritDoc} This class is used to write to the host's inbound table, and
 * read from its outbound table.  The inbound is assumed to be of the
 * form:
 * <p><code>WrxToHost
 *            sMessageIdentifier   VARCHAR(25)
 *            dHostModifyTime      Timestamp default systimestamp
 *            iMessageSequence     Integer
 *            sMessage             CLOB
 * </code></p>
 * The outbound host table should be of the same form except the name should be
 * MessageOutTable.  This may change due to a customer's request of course!  <b>
 * Note:</b> This class is known to be compatible with Oracle and DB2 hosted tables.
 *
 * @author       A.D.
 * @version      1.0   02/23/2005
 */
public class DefaultHostDBDelegate implements HostServerDelegate
{
  /** Keys for transaction map */
  protected static final String WRXJ_TRAN_TOK = "wrxjtok";
  protected static final String HOST_TRAN_TOK = "hosttok";

  private static final String HOST_INBOUND_TABLE_PROP = ".WRxToHostTableName";
  private static final String HOST_OUTBOUND_TABLE_PROP = ".HostToWRxTableName";

  private static final String DEFAULT_HOST_INBOUND_TABLE = "WrxToHost";
  private static final String DEFAULT_HOST_OUTBOUND_TABLE = "HostToWrx";

  protected final String sCommGroup;

  /** Name of the host's inbound table (table Wrx writes to). */
  protected String sInboundTableName;

  /** Name of the host's outbound table (table Wrx reads). */
  protected String sOutboundTableName;

  /** Host connection DB object.     */
  protected DBObject hostDBObj;
  protected boolean dataSpecified = false;
  protected Object[] delegateInfo;
  protected StringBuffer mpSQLString;
  protected boolean mzReadWithLock = false;
  protected HostToWrxData mpHostToWrxData;

  public DefaultHostDBDelegate(String isCommGroup)
  {
    this(null, isCommGroup);
  }

  public DefaultHostDBDelegate(Object ipDelegateInfo, String isCommGroup)
  {
    this(ipDelegateInfo, new DBObjectTL().getDBObject(isCommGroup));
  }

  /**
   * Constructor for convenience.  Allows direct reference to originally created
   * DBObject.
   * @param ipDelegateInfo Information for delegate to do its operations.
   * @param ipHostConnObj DBObject created by caller.
   */
  public DefaultHostDBDelegate(Object ipDelegateInfo, DBObject ipHostConnObj)
  {
    String vsCommGroup = ipHostConnObj.getDBIdentifier();

    // Get table names (direction relative to the host)
    sInboundTableName = Application.getString(vsCommGroup
        + HOST_INBOUND_TABLE_PROP, DEFAULT_HOST_INBOUND_TABLE);
    sOutboundTableName = Application.getString(vsCommGroup
        + HOST_OUTBOUND_TABLE_PROP, DEFAULT_HOST_OUTBOUND_TABLE);

    if (ipDelegateInfo != null)
    {
      delegateInfo = new Object[1];
      delegateInfo[0] = ipDelegateInfo;
    }
    sCommGroup = vsCommGroup;
    mpSQLString = new StringBuffer();
    hostDBObj = ipHostConnObj;
    mpHostToWrxData = Factory.create(HostToWrxData.class);
    dataSpecified = (ipDelegateInfo != null);
  }

 /**
  * {@inheritDoc}
  */
  @Override
  public boolean isInfoUnderstood()
  {
    return(dataSpecified);
  }

 /**
   * {@inheritDoc}
   *
   * @throws DBException No real exception thrown here. This is to satisfy
   *             interface.
   */
  @Override
  public void setInfo(Object... iapDelegateInfo) throws DBException
  {
    delegateInfo = iapDelegateInfo;
    dataSpecified = true;
  }

 /**
  * {@inheritDoc}
  * @param izmzReadWithLock
  */
  @Override
  public void setReadLockInfo(boolean izmzReadWithLock)
  {
    mzReadWithLock = izmzReadWithLock;
  }

 /**
  * Adds a sMessage to the Host side's inbound table. <i>This delegate
  * is expected to have a reference to a WrxToHostData object.</i>
  * @throws DBException when there is a database add error.
  */
  @Override
  public void addToDataQueue() throws DBException
  {
      addToDataQueue(0);
  }
  
  @Override
  public void addToDataQueue(int originalMessageSequenceNumber) throws DBException {
      dataSpecified = false;
      if (delegateInfo == null || !(delegateInfo[0] instanceof WrxToHostData))
        throw new DBException("Data content for host sMessage not initialized!");

      WrxToHostData vpWrxToHostData = (WrxToHostData)delegateInfo[0];
      byte[] vabClobData = vpWrxToHostData.getMessageBytes();
      if (vabClobData == null)
        throw new DBException("CLOB data not filled in!");

      mpSQLString.setLength(0);
      try
      {
        mpSQLString.append("SELECT ").append(getMessageIdentifierName())
                   .append(" FROM ").append(sInboundTableName).append(" WHERE ")
                   .append(getMessageSequenceName()).append(" = ")
                   .append(vpWrxToHostData.getMessageSequence());
        DBResultSet rset = hostDBObj.execute(mpSQLString.toString());
        if (rset.getRowCount() > 0)
        {
          /*
           * This row exists on the host side. In theory this should not happen if
           * they are processing messages correctly.
           */
          vpWrxToHostData.setMessageSequence(getMaxSequence() + 1);
        }

        /*
         * Insert the row
         */
        mpSQLString.setLength(0);
        mpSQLString.append("INSERT INTO ").append(sInboundTableName).append(" (")
                 .append(getMessageSequenceName()).append(", ")
                 .append(getMessageIdentifierName()).append(", ")
                 .append(getMessageName()).append(") VALUES (?, ?, ?)");

        hostDBObj.execute(mpSQLString.toString(),
                          vpWrxToHostData.getMessageSequence(),
                          vpWrxToHostData.getMessageIdentifier(),
                          new DACCLOB(vabClobData));
      }
      catch(Throwable e)
      {
        throw new DBException("Error adding data to host system...", e);
      }
  }

  /**
   * Adds a sMessage to the Host side's inbound table. <i>This delegate
   * is expected to have a reference to a WrxToHostData object.</i>
   * @throws DBException when there is a database add error.
   */
   @Override
   public void addToDataQueue(int inRequestId, String isLoadId) throws DBException
   {
     dataSpecified = false;
     if (delegateInfo == null || !(delegateInfo[0] instanceof WrxToHostData))
       throw new DBException("Data content for host sMessage not initialized!");

     WrxToHostData vpWrxToHostData = (WrxToHostData)delegateInfo[0];
     byte[] vabClobData = vpWrxToHostData.getMessageBytes();
     if (vabClobData == null)
       throw new DBException("CLOB data not filled in!");

     mpSQLString.setLength(0);
     try
     {
       mpSQLString.append("SELECT ").append(getMessageIdentifierName())
                  .append(" FROM ").append(sInboundTableName).append(" WHERE ")
                  .append(getMessageSequenceName()).append(" = ")
                  .append(vpWrxToHostData.getMessageSequence());
       DBResultSet rset = hostDBObj.execute(mpSQLString.toString());
       if (rset.getRowCount() > 0)
       {
         /*
          * This row exists on the host side. In theory this should not happen if
          * they are processing messages correctly.
          */
         vpWrxToHostData.setMessageSequence(getMaxSequence() + 1);
       }

       /*
        * Insert the row
        */
       mpSQLString.setLength(0);
       mpSQLString.append("INSERT INTO ").append(sInboundTableName).append(" (")
                .append(getMessageSequenceName()).append(", ")
                .append(getMessageIdentifierName()).append(", ")
                .append(getMessageName()).append(") VALUES (?, ?, ?)");

       hostDBObj.execute(mpSQLString.toString(),
                         vpWrxToHostData.getMessageSequence(),
                         vpWrxToHostData.getMessageIdentifier(),
                         new DACCLOB(vabClobData));
     }
     catch(Throwable e)
     {
       throw new DBException("Error adding data to host system...", e);
     }
   }

 /**
  *  {@inheritDoc} This method is for deleting records from the host's outbound
  *  table.  <i>The delegate is expected to have a reference to the unique
  *  sequence number of the record being deleted.</i>
  * @throws DBException
  */
  @Override
  public void deleteMessage() throws DBException
  {
    dataSpecified = false;
    if (delegateInfo == null || !(delegateInfo[0] instanceof Integer))
      throw new DBException("Data content for add operation not correctly " +
                            "initialized! WrxToHostData data class not specified.");
    int seqNumber = ((Integer)delegateInfo[0]).intValue();
    mpSQLString.setLength(0);
    mpSQLString.append("DELETE FROM ").append(sOutboundTableName).append(" WHERE ")
             .append(getMessageSequenceName()).append(" = ").append(seqNumber);
    try
    {
      hostDBObj.execute(mpSQLString.toString());
    }
    catch(NoSuchElementException nexc)
    {
      throw new DBException("Warning: data not deleted on host system...", nexc);
    }
  }

 /**
  * Deletes all processed outbound sMessage from the Host's outbound data queue.
  * <i>The delegate is expected to know the Host's name string, and the sMessage
  * sequence number.</i>
  * @throws DBException when there is a database add error.
  */
  @Override
  public void deleteProcessedMessages() throws DBException
  {
    deleteMessage();
  }

 /**
  *  {@inheritDoc}
  * @throws DBException
  */
  @Override
  public void markMessageProcessed() throws DBException
  {
    // Stub to satisfy interface.
  }

 /**
  *  {@inheritDoc}
  * @throws DBException
  */
  @Override
  public void toggleProcessedFlag() throws DBException
  {
    // Stub to satisfy interface.
  }

 /**
  * {@inheritDoc}
  */
  @Override
  public List<Map> getMessages() throws DBException
  {
    // Stub to satisfy interface.
    return(null);
  }

 /**
  * {@inheritDoc}
  */
  @Override
  public AbstractSKDCData getMessage() throws DBException
  {
    // Stub to satisfy interface.
    return(null);
  }

 /**
  * {@inheritDoc} Method is unimplemented.
  */
  @Override
  public String[] getMessageNames() throws DBException
  {
    return(new String[0]);
  }

 /**
  * Unimplemented.
  * @return
  */
  @Override
  public AbstractSKDCData getNewestMessage() throws DBException
  {
    // Currently no need for this method for remote hosts.
    return(null);
  }

 /**
  * {@inheritDoc} Method is designed for use with the hosts outbound table.
  * <i>The delegate is expected to have the Host Name.</i>
  * @return {@inheritDoc}
  */
  @Override
  public boolean unprocessedMessageAvailable() throws DBException
  {
    final String ROW_COUNT_NAME = "ROW_COUNT";
    dataSpecified = false;
    if (delegateInfo == null && !(delegateInfo[0] instanceof String))
      throw new DBException("Data content for read operation not correctly " +
                            "initialized! Host name not specified.");
    mpSQLString.setLength(0);
    mpSQLString.append("SELECT COUNT(").append(getMessageSequenceName())
               .append(") AS ").append(ROW_COUNT_NAME).append(" FROM ")
               .append(sOutboundTableName);

    List<Map> alist = hostDBObj.execute(mpSQLString.toString()).getRows();
    int row_count = 0;
    if (alist != null && alist.size() != 0)
    {
      String rec = ((alist.get(0)).get(ROW_COUNT_NAME)).toString();
      row_count = Integer.parseInt(rec);
    }
    return(row_count > 0);
  }

  /**
   * Gets the maximum message sequence currently in the host's database.
   *
   * @return integer containing maximum sequence number.
   * @throws DBException if there is a database access error.
   */
  protected int getMaxSequence() throws DBException
  {
    mpSQLString.setLength(0);
    mpSQLString.append("SELECT MAX(").append(getMessageSequenceName())
               .append(") AS \"IMAXSEQUENCE\" FROM ")
               .append(sInboundTableName);
    List<Map> theList = hostDBObj.execute(mpSQLString.toString()).getRows();

    int maxSequence = 0;
    if (!theList.isEmpty())
    {
      maxSequence = DBHelper.getIntegerField(theList.get(0), "IMAXSEQUENCE");
    }

    return(maxSequence);
  }

  @Override
  public int[] getSequenceBoundaries() throws DBException
  {
    return(new int[0]);
  }

  @Override
  public int[] getHostSequenceBoundaries() throws DBException
  {
    return(new int[0]);
  }

  /**
   * Start Warehouse Rx + Host DB Transactions
   *
   * @return
   * @throws DBException
   */
  public HashMap<String, TransactionToken> startTransactions() throws DBException
  {
    DBObject vpWrxjDBObj = new DBObjectTL().getDBObject();
    DBObject vpHostDBObj = new DBObjectTL().getDBObject(sCommGroup);

    HashMap<String, TransactionToken> vpTranTokMap = new HashMap<String, TransactionToken>();
    vpTranTokMap.put(WRXJ_TRAN_TOK, vpWrxjDBObj.startTransaction());
    vpTranTokMap.put(HOST_TRAN_TOK, vpHostDBObj.startTransaction());

    return(vpTranTokMap);
  }

  /**
   * Commit Warehouse Rx + Host DB Transactions
   *
   * @return
   * @throws DBException
   */
  public void commitTransactions(HashMap<String, TransactionToken> ipTranTokMap)
          throws DBException
  {
    DBObject vpWrxjDBObj = new DBObjectTL().getDBObject();
    DBObject vpHostDBObj = new DBObjectTL().getDBObject(sCommGroup);

    vpWrxjDBObj.commitTransaction(ipTranTokMap.get(WRXJ_TRAN_TOK));
    vpHostDBObj.commitTransaction(ipTranTokMap.get(HOST_TRAN_TOK));
  }

  /**
   * End Warehouse Rx + Host DB Transactions
   *
   * @return
   * @throws DBException
   */
  public void endTransactions(HashMap<String, TransactionToken> ipTranTokMap)
  {
    DBObject vpWrxjDBObj = new DBObjectTL().getDBObject();
    DBObject vpHostDBObj = new DBObjectTL().getDBObject(sCommGroup);

    vpWrxjDBObj.endTransaction(ipTranTokMap.get(WRXJ_TRAN_TOK));
    vpHostDBObj.endTransaction(ipTranTokMap.get(HOST_TRAN_TOK));
  }

  public String getMessageDateName()
  {
    return("dHostModifyTime");
  }

  public String getMessageIdentifierName()
  {
    return("sMessageIdentifier");
  }

  public String getMessageSequenceName()
  {
    return("iMessageSequence");
  }

  public String getMessageName()
  {
    return("sMessage");
  }

  /**
   * Default impl. is empty.  This method will need to be implemented for each
   * of the target Database interfaces on the Host.
   */
  @Override
  public AbstractSKDCData getOldestUnprocessedMessage() throws DBException
  {
    // TODO Auto-generated method stub
    return null;
  }
}
