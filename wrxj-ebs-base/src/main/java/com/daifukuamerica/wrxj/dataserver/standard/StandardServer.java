package com.daifukuamerica.wrxj.dataserver.standard;

/*
                    Daifuku America Corporation
                       International Center
                   5202 Douglas Corrigan Way
                Salt Lake City, Utah  84116-3192
                        (801) 359-9900

   This software is furnished under a license and may be used and copied only 
   in accordance with the terms of such license. This software or any other 
   copies thereof in any form, may not be provided or otherwise made available, 
   to any other person or company without written consent from Daifuku America 
   Corporation.

   Daifuku America Corporation assumes no responsibility for the use or 
   reliability of software which has been modified without approval.
*/

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistory;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;
import com.daifukuamerica.wrxj.device.gateway.SystemGateway;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.jdbc.StoredProcedureParameter;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import javax.swing.SwingUtilities;

/**
 * Base DataServer class containing methods for Database connectivity, Database
 * manipulation, and logging. All application DataServers connecting to the
 * database should extend this class.
 * 
 * @author Stephen Kendorski
 * @author A.D.
 * @version 1.0
 */
public abstract class StandardServer
{
  protected static boolean mzHasHostSystem;
  
  protected Logger mpLogger = Logger.getLogger();
  private DBObject mpDBObj;
  
  private TransactionHistory mpTranHistory = Factory.create(TransactionHistory.class);
  protected TransactionHistoryData tnData = Factory.create(TransactionHistoryData.class);

  static
  {
    mzHasHostSystem = Application.getBoolean("HostSystemEnabled");
    Logger.getLogger().logDebug("StandardServer - HostEvents " + mzHasHostSystem);
  }
  
  /**
   * Constructor
   */
  public StandardServer()
  {
    this(null);
  }

  /**
   * Constructor
   * 
   * @param isKeyName - Currently unused
   */
  public StandardServer(String isKeyName)
  {
    /*
     * Effectively create a DBObject variable as a ThreadLocal variable, and
     * then do a "get" call on the variable (getDBObject) to actually allocate
     * the memory for it (this only happens on the first "get", or in this case
     * the "getDBObject" call which ends up calling "initialValue"). All
     * subsequent calls to getDBObject returns just the reference to the
     * ThreadLocal variable. This allows us to share the same connection
     * wherever needed under this program's execution thread.
     */
    mpDBObj = new DBObjectTL().getDBObject();

    try
    {
      // Get connection from connection pool.
      mpDBObj.connect();
    }
    catch (DBException e)
    {
      logException(e, "Error opening Database Connection");
    }
  }
  
  public StandardServer(String isKeyName, DBObject dbo)
  {
	  mpDBObj = dbo; 
	    try
	    {
	      // Get connection from connection pool.
	      mpDBObj.connect();
	    }
	    catch (DBException e)
	    {
	      logException(e, "Error opening Database Connection");
	    }
  }
  
  /**
   * Method to disconnect from the database.
   */
  public void cleanUp()
  {
    closeDBConnection();
  }

  /**
   * Helper method to close instantiated servers
   * 
   * @param ipServer
   */
  protected void cleanUp(StandardServer ipServer)
  {
    if (ipServer != null)
    {
      ipServer.cleanUp();
    }
  }

  /**
   * Helper method to close instantiated DB interfaces
   * 
   * @param ipDbInt
   */
  protected void cleanUp(BaseDBInterface ipDbInt)
  {
    if (ipDbInt != null)
    {
      ipDbInt.cleanUp();
    }
  }

  /*========================================================================*/
  /* JMS Methods                                                            */
  /*========================================================================*/

  /**
   * Get the SystemGateway for publishing a JMS message
   * 
   * @return <code>SystemGateway</code>
   */
  protected SystemGateway getSystemGateway()
  {
    return ThreadSystemGateway.get();
  }

  /*========================================================================*/
  /*  Database Methods                                                      */
  /*========================================================================*/
  
  /**
   * Disconnect the database connection.
   */
  private void closeDBConnection()
  {
    if (isDBConnected())
    {
      try
      {
        mpDBObj.disconnect(true);
        if (!SwingUtilities.isEventDispatchThread())
        {
          mpDBObj = null;
        }
      }
      catch (Exception e)
      {
        logException(e, "Error closing Database Connection");
      }
    }
  }

  /**
   * Method to check if we are connected to the database.
   *
   * @return <code>boolean</code> indicating if we have a valid connection.
   */
  private boolean isDBConnected()
  {
    return (mpDBObj.checkConnected());
  }

  /**
   * Method to start a transaction.
   *
   * @exception DBException
   */
  protected TransactionToken startTransaction() throws DBException
  {
    return (mpDBObj.startTransaction());
  }

  /**
   * Method to commit a transaction.
   *
   * @exception DBException
   */
  protected void commitTransaction(TransactionToken ipTT) throws DBException
  {
    mpDBObj.commitTransaction(ipTT);
  }

  /**
   * End a transaction
   * 
   * @param ipTT
   */
  protected void endTransaction(TransactionToken ipTT)
  {
    mpDBObj.endTransaction(ipTT);
  }

  /**
   * Execute a stored procedure
   * 
   * @param isProcName
   * @param iapParams
   * @throws DBException
   */
  public StoredProcedureParameter[] executeStoreProcedure(String isProcName,
      StoredProcedureParameter... iapParams) throws DBException
  {
    return mpDBObj.executeStoreProcedure(isProcName, iapParams);
  }

  /*========================================================================*/
  /*  Transaction History Methods                                           */
  /*========================================================================*/

  /**
   * Log a Transaction History entry 
   * @param ipTranData
   */
  public void logTransaction(TransactionHistoryData ipTranData)
  {
    if (ipTranData.getUserID().trim().length() < 1)
    {
      ipTranData.setUserID(SKDCUserData.getLoginName());
      ipTranData.setRole(SKDCUserData.getRole());
      ipTranData.setMachineName(SKDCUserData.getMachineName());
    }

    TransactionToken vpToken = null;
    try
    {
      vpToken = startTransaction();
      mpTranHistory.addElement(ipTranData);
      commitTransaction(vpToken);
    }
    catch (DBException e)
    {
      logException(e, "Adding Database Transaction History..." + ipTranData.toString());
    }
    finally
    {
      endTransaction(vpToken);
    }
  }

  /*========================================================================*/
  /*  Logger Methods                                                        */
  /*========================================================================*/
  
  protected void logDebug(String logText)
  {
    mpLogger.logDebug(logText);
  }

  protected void logOperation(String logText)
  {
    mpLogger.logOperation(logText);
  }

  protected void logOperation(int type, String logText)
  {
    mpLogger.logOperation(type, logText);
  }

  protected void logError(String logText)
  {
    mpLogger.logError(logText);
  }

  protected void logException(Exception e, String sText)
  {
    mpLogger.logException(e, sText);
  }

  protected void logException(String sText, Exception e)
  {
    mpLogger.logException(e, sText);
  }

  protected String getExceptionString(Throwable t)
  {
    return t.getClass().getName() + " : " + t.getMessage();
  } 
  
  /**
   * Method finds fields that have been changed and log them into Transaction 
   * History record. If column is not a defined column in Transaction History 
   * record, the difference between old and new values is appended to Action
   * Description column.
   * 
   * @param <Type>  Data type
   * @param ipOldData Old data
   * @param ipNewData New data
   */
  protected <Type extends AbstractSKDCData> boolean logDataChanged(Type ipOldData, Type ipNewData)
  {
    boolean vzChange = false;
    String actDesc = "";
    ColumnObject[] vpNewColumns = ipNewData.getColumnArray();
    ColumnObject[] vpOldColumns = ipOldData.getColumnArray();
    
    // For each changed columns, prepare the transaction data
    for(int i = 0; i < vpNewColumns.length; i++)
    {
      String vsColName = vpNewColumns[i].getColumnName();
      Object vpNewValue = vpNewColumns[i].getColumnValue();
      Object vpOldValue = ColumnObject.getValueByName(vsColName, vpOldColumns);
      
      if (!vpNewValue.equals(vpOldValue))
      {
        vzChange = true;
        // Place the data into the transaction column. If the field is not
        // defined, place/append the data into field ActionDescription.
        if (tnData.setField(vsColName, vpNewValue) == -1)
        {
          actDesc = actDesc 
                  + ipNewData.getActionDesc(vsColName, vpOldValue, vpNewValue)
                  + "\n";
        }
      }
    }
    tnData.setActionDescription(actDesc);
    return vzChange;
  }
}

