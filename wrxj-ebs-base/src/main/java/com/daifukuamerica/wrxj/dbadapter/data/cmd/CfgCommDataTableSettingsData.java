package com.daifukuamerica.wrxj.dbadapter.data.cmd;

/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright (c) 2016 Wynright Corporation.  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/

import static com.daifukuamerica.wrxj.dbadapter.data.cmd.CfgCommDataTableSettingsEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.dbadapter.WynsoftData;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to hold CME_CFG_COMM_DATA_TABLE_SETTINGS data
 */
public class CfgCommDataTableSettingsData extends WynsoftData
{
  public static final int DIRECTION_TO_WYNSOFT = 1;
  public static final int DIRECTION_FROM_WYNSOFT = 2;
  
  public static final String DATATIMEOUTINTERVALINMIN_NAME= DATA_TIMEOUT_INTERVAL_IN_MIN.getName();
  public static final String BATCHMSGDETAILTABLE_NAME = BATCH_MSG_DETAIL_TABLE.getName();
  public static final String BATCHMSGHEADERTABLE_NAME = BATCH_MSG_HEADER_TABLE.getName();
  public static final String SINGLEMSGTABLE_NAME      = SINGLE_MSG_TABLE.getName();
  public static final String DIRECTIONID_NAME         = DIRECTION_ID.getName();
  public static final String CLIENTCONNECTIONNAME_NAME= CLIENT_CONNECTION_NAME.getName();
  public static final String COMMUNICATIONID_NAME     = COMMUNICATION_ID.getName();
  public static final String TRANSACTIONSEQNAME_NAME  = TRANSACTION_SEQ_NAME.getName();

  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<>();

  // ------------------- CfgCommDataTableSettings table data -----------------------------
  private int    mDataTimeoutIntervalInMin;
  private String mBatchMsgDetailTable;
  private String mBatchMsgHeaderTable;
  private String mSingleMsgTable;
  private int    mDirectionId;
  private String mClientConnectionName;
  private int    mCommunicationId;
  private String mTransactionSeqName;

  //-------------------- CfgCommDataTableSettings default data ---------------------------
  public CfgCommDataTableSettingsData()
  {
    super();
    clear();
    initColumnMap(mpColumnMap, CfgCommDataTableSettingsEnum.class);
  }

  @Override
  public String toString()
  {
    StringBuffer myString = new StringBuffer(getClass().getCanonicalName()).append("\n");
    String[] vasKeys = mpColumnMap.keySet().toArray(new String[0]);
    Arrays.sort(vasKeys);
    for (String sKey : vasKeys) {
      ColumnObject vpVal = getColumnObject(sKey);
      String vsVal = vpVal == null ? null : 
        vpVal.getColumnValue() == null ? null : vpVal.getColumnValue().toString();
      myString.append(" * ").append(sKey).append(" = ").append(vsVal).append(";\n");
    }
    return myString.toString();
  }

  @Override
  public boolean equals(AbstractSKDCData absOther)
  {
    CfgCommDataTableSettingsData other = (CfgCommDataTableSettingsData)absOther;
    return other.getCommunicationId() == getCommunicationId()
        && other.getClientConnectionName().equals(getClientConnectionName())
        && other.getDirectionId() == getDirectionId();
  }


/*---------------------------------------------------------------------------
   Getters
 ---------------------------------------------------------------------------*/
  public int    getDataTimeoutIntervalInMin(){  return mDataTimeoutIntervalInMin;}
  public String getBatchMsgDetailTable() {  return mBatchMsgDetailTable;    }
  public String getBatchMsgHeaderTable() {  return mBatchMsgHeaderTable;    }
  public String getSingleMsgTable()      {  return mSingleMsgTable;         }
  public int    getDirectionId()         {  return mDirectionId;            }
  public String getClientConnectionName(){  return mClientConnectionName;   }
  public int    getCommunicationId()     {  return mCommunicationId;        }
  public String getTransactionSeqName()  {  return mTransactionSeqName;     }


/*---------------------------------------------------------------------------
   Setters
 ---------------------------------------------------------------------------*/
  public void setDataTimeoutIntervalInMin(int iDataTimeoutIntervalInMin)
  {
    mDataTimeoutIntervalInMin = iDataTimeoutIntervalInMin;
    addColumnObject(new ColumnObject(DATATIMEOUTINTERVALINMIN_NAME, mDataTimeoutIntervalInMin));
  }
  public void setBatchMsgDetailTable(String iBatchMsgDetailTable)
  {
    mBatchMsgDetailTable = iBatchMsgDetailTable;
    addColumnObject(new ColumnObject(BATCHMSGDETAILTABLE_NAME, mBatchMsgDetailTable));
  }
  public void setBatchMsgHeaderTable(String iBatchMsgHeaderTable)
  {
    mBatchMsgHeaderTable = iBatchMsgHeaderTable;
    addColumnObject(new ColumnObject(BATCHMSGHEADERTABLE_NAME, mBatchMsgHeaderTable));
  }
  public void setSingleMsgTable(String iSingleMsgTable)
  {
    mSingleMsgTable = iSingleMsgTable;
    addColumnObject(new ColumnObject(SINGLEMSGTABLE_NAME, mSingleMsgTable));
  }
  public void setDirectionId(int iDirectionId)
  {
    mDirectionId = iDirectionId;
    addColumnObject(new ColumnObject(DIRECTIONID_NAME, mDirectionId));
  }
  public void setClientConnectionName(String iClientConnectionName)
  {
    mClientConnectionName = iClientConnectionName;
    addColumnObject(new ColumnObject(CLIENTCONNECTIONNAME_NAME, mClientConnectionName));
  }
  public void setCommunicationId(int iCommunicationId)
  {
    mCommunicationId = iCommunicationId;
    addColumnObject(new ColumnObject(COMMUNICATIONID_NAME, mCommunicationId));
  }
  public void setTransactionSeqName(String iTransactionSeqName)
  {
    mTransactionSeqName = iTransactionSeqName;
    addColumnObject(new ColumnObject(TRANSACTIONSEQNAME_NAME, mTransactionSeqName));
  }

  /**
   * Required set field method. This method figures out what column was passed
   * to it and sets the value. This allows us to have a generic method for all
   * DB interfaces.
   */
  @Override
  public int setField(String isColName, Object ipColValue)
  {
    TableEnum vpEnum = mpColumnMap.get(isColName);
    if (vpEnum == null) 
    {
      return super.setField(isColName, ipColValue);
    }
    
    switch ((CfgCommDataTableSettingsEnum)vpEnum)
    {
      case DATA_TIMEOUT_INTERVAL_IN_MIN:
        setDataTimeoutIntervalInMin((Integer)ipColValue);
        break;
      case BATCH_MSG_DETAIL_TABLE:
        setBatchMsgDetailTable((String)ipColValue);
        break;
      case BATCH_MSG_HEADER_TABLE:
        setBatchMsgHeaderTable((String)ipColValue);
        break;
      case SINGLE_MSG_TABLE:
        setSingleMsgTable((String)ipColValue);
        break;
      case DIRECTION_ID:
        setDirectionId((Integer)ipColValue);
        break;
      case CLIENT_CONNECTION_NAME:
        setClientConnectionName((String)ipColValue);
        break;
      case COMMUNICATION_ID:
        setCommunicationId((Integer)ipColValue);
        break;
      case TRANSACTION_SEQ_NAME:
        setTransactionSeqName((String)ipColValue);
        break;
      default:
        throw new IllegalArgumentException("Unknown field " + isColName);
    }
    return 0;
  }
}
