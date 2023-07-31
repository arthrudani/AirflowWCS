/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$
  
  Copyright (c) 2017 Daifuku North America Holding Company. All Rights Reserved.
  
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.dataserver.standard;

import com.daifukuamerica.wrxj.dbadapter.data.cmd.CfgCommDataTableSettings;
import com.daifukuamerica.wrxj.dbadapter.data.cmd.CfgCommDataTableSettingsData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.StoredProcedureParameter;

/**
 * Library for sending messages to places via Wynsoft Communicator
 * 
 * @author mandrus
 */
public class StandardCommunicatorServer extends StandardServer
{
  private CfgCommDataTableSettings mpCommHandler = Factory.create(
      CfgCommDataTableSettings.class);
  
  /**
   * Constructor
   */
  public StandardCommunicatorServer()
  {
    super();
  }

  /**
   * Constructor
   * 
   * @param isKeyName
   */
  public StandardCommunicatorServer(String isKeyName)
  {
    super(isKeyName);
  }

  /**
   * Send a message via Communicator
   *  
   * @param ipSsccData
   * @throws DBException
   */
  public void sendOutboundMessage(String isConnection, String isMessageType, String isMessage)
      throws DBException
  {
    DBObject mpDBO = new DBObject();
    try
    {
      mpDBO.connect();
      
      TransactionToken tt = null;
      try
      {
        tt = mpDBO.startTransaction();
        
        // See [FLGES_CMD].[dbo].[CME_CFG_COMM_DATA_TABLE_SETTINGS]
        CfgCommDataTableSettingsData vpCommData = mpCommHandler.getData(
            isConnection, CfgCommDataTableSettingsData.DIRECTION_FROM_WYNSOFT);
        if (vpCommData == null)
        {
          throw new DBException(
              "Comm Data for " + isConnection + " NOT FOUND!");
        }
  
        // S_CMD_P_CME_CREATE_SINGLE_OUT_REC is a synonym to 
        // FLGES_CMD.dbo.P_CME_CREATE_SINGLE_OUT_REC
        //
        // EXEC S_CMD_P_CME_CREATE_SINGLE_OUT_REC
        //      isConnection,
        //      'WRx', 
        //      isMessageType, 
        //      isMessage, 
        //      vpCommData.getBatchMsgHeaderTable(), 
        //      vpCommData.getBatchMsgDetailTable(), 
        //      vpCommData.getTransactionSeqName(), 
        //      output
        StoredProcedureParameter[] vapResults = mpDBO.executeStoreProcedure(
            "S_CMD_P_CME_CREATE_SINGLE_OUT_REC",
            new StoredProcedureParameter(isConnection, null),
            new StoredProcedureParameter("WRx", null),
            new StoredProcedureParameter(isMessageType, null),
            new StoredProcedureParameter(isMessage, null),
            new StoredProcedureParameter(vpCommData.getBatchMsgHeaderTable(), null),
            new StoredProcedureParameter(vpCommData.getBatchMsgDetailTable(), null),
            new StoredProcedureParameter(vpCommData.getTransactionSeqName(), null),
            new StoredProcedureParameter(null, Integer.valueOf(0)));
        if ((Integer)vapResults[7].getOutParam() == 0)
        {
          throw new DBException("Failed to send outbound message: "
              + isMessageType + ":" + isMessage);
        }
        mpDBO.commitTransaction(tt);
      }
      finally
      {
        mpDBO.endTransaction(tt);
      }
    }
    finally
    {
      mpDBO.disconnect(false);
    }
  }
}
