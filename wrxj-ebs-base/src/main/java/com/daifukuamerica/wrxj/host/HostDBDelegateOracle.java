package com.daifukuamerica.wrxj.host;
/****************************************************************************
$Workfile: HostDBDelegateOracle.java
$Revision: IKEA

Copyright 2019 Daifuku America Corporation All Rights Reserved.

THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND
TREATIES. NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED
COPIED, DISTRIBUTED, REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED,
EXPANDED, COLLECTED, COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED
WITHOUT THE PRIOR WRITTEN CONSENT OF Daifuku America Corporation ANY
USE OR EXPLOITATION OF THIS WORK WITHOUT AUTHORIZATION COULD SUBJECT THE
PERPETRATOR TO CRIMINAL AND CIVIL LIABILITY.
****************************************************************************/
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import java.util.List;
import java.util.Map;

/**
 * DB Delegate to handle Oracle hosted transfer table.
 *
 * @author A.D.
 * @since  16-Aug-2019
 */
public class HostDBDelegateOracle extends DefaultHostDBDelegate
{
  public HostDBDelegateOracle(String isCommGroup)
  {
    this(null, isCommGroup);
  }

  public HostDBDelegateOracle(Object ipDelegateInfo, String isCommGroup)
  {
    super(ipDelegateInfo, isCommGroup);
  }

  public HostDBDelegateOracle(Object ipDelegateInfo, DBObject ipHostConnObj)
  {
    super(ipDelegateInfo, ipHostConnObj);
  }

  /**
   * {@inheritDoc} This method is used for reading from am Oracle host's outbound
   * table.
   */
   @Override
   public AbstractSKDCData getOldestUnprocessedMessage() throws DBException
   {
     dataSpecified = false;
     mpSQLString.setLength(0);

     mpSQLString.append("SELECT * FROM (SELECT * FROM ").append(sOutboundTableName)
                .append(" ORDER BY ").append(getMessageSequenceName())
                .append(") WHERE ROWNUM < 2");

     List<Map> recordList = hostDBObj.execute(mpSQLString.toString()).getRows();
     if (recordList.isEmpty())
     {
       return(null);
     }
     Map tm = recordList.get(0);
     mpHostToWrxData.clear();
     mpHostToWrxData.setMessageSequence(DBHelper.getIntegerField(tm, getMessageSequenceName()));
     mpHostToWrxData.setOriginalMessageSequence(DBHelper.getIntegerField(tm, getMessageSequenceName()));
     mpHostToWrxData.setMessageIdentifier(DBHelper.getStringField(tm, getMessageIdentifierName()).trim());


     mpSQLString.setLength(0);
     mpSQLString.append("SELECT * FROM HostToWrx ");
                                        // This will retrieve just one record.
     HostToWrxData vpKeyedData = Factory.create(HostToWrxData.class);
     vpKeyedData.setKey(HostToWrxData.MESSAGESEQUENCE_NAME, mpHostToWrxData.getMessageSequence());

     if (mzReadWithLock)
     {
       mpSQLString.append(DBHelper.buildWhereClause(vpKeyedData.getKeyArray()))
                  .append(" FOR UPDATE");
     }
     else
     {
       mpSQLString.append(DBHelper.buildWhereClause(vpKeyedData.getKeyArray()));
     }

     mzReadWithLock = false;

     byte[] clobBytes = DBHelper.readClob(sCommGroup, getMessageName(), mpSQLString.toString());
     mpHostToWrxData.setMessageBytes(clobBytes);

     return mpHostToWrxData.clone();
   }
}
