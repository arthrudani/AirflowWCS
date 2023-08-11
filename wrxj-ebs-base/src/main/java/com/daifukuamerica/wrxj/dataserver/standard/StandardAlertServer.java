package com.daifukuamerica.wrxj.dataserver.standard;

import com.daifukuamerica.wrxj.dbadapter.data.Alerts;
import com.daifukuamerica.wrxj.dbadapter.data.AlertsData;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadDataAndLLIData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItem;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadWord;
import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.Move;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.ReasonCode;
import com.daifukuamerica.wrxj.dbadapter.data.RouteData;
import com.daifukuamerica.wrxj.dbadapter.data.Station;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.dbadapter.data.TableJoin;
import com.daifukuamerica.wrxj.dbadapter.data.WrxSequencer;
import com.daifukuamerica.wrxj.device.agc.AGCDeviceConstants;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.UnusedMethod;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

/**
 * The StandardLoadServer is the object that performs everything that is done to
 * a load.
 *
 * @author Ed Askew
 * @version 1.0
 */
public class StandardAlertServer extends StandardServer
{
  protected StandardDedicationServer  mpDedServer     = null;
  protected StandardInventoryServer   mpInvServer     = null;
  protected StandardLocationServer    mpLocServer     = null;
  protected StandardMoveServer        mpMoveServer    = null;
  protected StandardPickServer        mpPickServer    = null;
  protected StandardPoReceivingServer mpPOServer      = null;
  protected StandardOrderServer       mpOrderServer   = null;
  protected StandardStationServer     mpStationServer = null;
  protected StandardRouteServer       mpRouteServer   = null;

  protected Alerts mpAlert = Factory.create(Alerts.class);

  /**
   * Constructor for alert with no parameters
   */
  public StandardAlertServer()
  {
    this(null);
  }

  /**
   *  Constructor for alert with name of who is creating it and the scheduler name
   *
   *  @param isKeyName name of creator
   */
  public StandardAlertServer(String isKeyName)
  {
    super(isKeyName);
  }

  /**
   * Web application constructor for per user connection pooling
   * @param keyName
   * @param dbo
   */
  public StandardAlertServer(String keyName, DBObject dbo)
  {
	  super(keyName, dbo);
	  logDebug("Creating " + getClass().getSimpleName());
  }

  /**
   *  Shuts down this controller by cancelling any timers and shutting down the
   *  Equipment.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();

    if (mpDedServer != null)
    {
      mpDedServer.cleanUp();
      mpDedServer = null;
    }
    if (mpInvServer != null)
    {
      mpInvServer.cleanUp();
      mpInvServer = null;
    }
    if (mpLocServer != null)
    {
      mpLocServer.cleanUp();
      mpLocServer = null;
    }
    if (mpMoveServer != null)
    {
      mpMoveServer.cleanUp();
      mpMoveServer = null;
    }
    if (mpOrderServer != null)
    {
      mpOrderServer.cleanUp();
      mpOrderServer = null;
    }
    if (mpPickServer != null)
    {
      mpPickServer.cleanUp();
      mpPickServer = null;
    }
    if (mpPOServer != null)
    {
      mpPOServer.cleanUp();
      mpPOServer = null;
    }
    if (mpRouteServer != null)
    {
      mpRouteServer.cleanUp();
      mpRouteServer = null;
    }
    if (mpStationServer != null)
    {
      mpStationServer.cleanUp();
      mpStationServer = null;
    }
  }

  /**
   *  Convenience method to read alert record with <i>no</i> lock.
   *
   *  @return AlertData containing alert record. <code>null</code> value if a
   *          database error occurs, or no record found.
   */
  public AlertsData getAlert(String alertID)
  {
    return(getAlert(alertID, false));    // Read with no write lock.
  }

  /**
   *  Method to read a alert record with/without a record lock.
   *
   *  @param alertID value of the alertid
   *  @param setWriteLock <code>boolean</code> set to <code>true</code> if
   *         record should be locked; <code>false</code> if read with <i>no</i> lock.
   *  @return AlertData containing alert record. <code>null</code> value if a
   *          database error occurs, or no record found.
   */
  public AlertsData getAlert(String alertID, boolean setWriteLock)
  {
    try
    {
      int lockFlag = (setWriteLock) ? DBConstants.WRITELOCK : DBConstants.NOWRITELOCK;
      return mpAlert.getAlertData(alertID, lockFlag);
    }
    catch(DBException e)
    {
      logException(e, "AlertId  \"" + alertID + "\" Read Error - "
          + getClass().getSimpleName() + ".getLoad");
      return null;
    }
  }
  
  /**
   * Update the alert in the database to the current values and (optionally)
   *
   * @param alertData alert that needs to be updated
   */
//  public void updateLoadData(LoadData loadData, boolean izUpdateMoveDate)
//  {
//    TransactionToken tt = null;
//    try
//    {
//      tt = startTransaction();
//
//      LoadData vpOldLoadData = getLoad(loadData.getLoadID());
//
//      if(!loadData.getParentLoadID().equals(loadData.getLoadID()))
//      {
//        if(anyItemMovesForLoad(loadData.getLoadID()))
//        {
//          throw new DBException("Load ''" + loadData.getLoadID()
//              + " has a item moves and cannot be Consolidated");
//        }
//        deleteLoadMoveForLoad(loadData.getLoadID());
//      }
//      if (izUpdateMoveDate)
//        loadData.setMoveDate();
//      mpAlert.updateLoadInfo(loadData);
//
//      /*
//       * Log transaction history if this is a move
//       */
//      if (!vpOldLoadData.getWarehouse().equals(loadData.getWarehouse()) ||
//          !vpOldLoadData.getAddress().equals(loadData.getAddress()))
//      {
//        logLoadMoveTransaction(loadData.getLoadID(),
//            vpOldLoadData.getWarehouse(), vpOldLoadData.getAddress(),
//            vpOldLoadData.getShelfPosition(), loadData.getWarehouse(),
//            loadData.getAddress(), loadData.getShelfPosition(),
//            loadData.getDeviceID());
//      }
//
//      commitTransaction(tt);
//    }
//    catch (DBException e)
//    {
//      logException(e, "LoadId \"" + loadData.getParentLoadID()
//          + "\" (Parent) Exception Changing Parent Load Status - "
//          + getClass().getSimpleName() + ".setParentLoadMoveStatus");
//    }
//    finally
//    {
//      endTransaction(tt);
//    }
//  }

 /**
  * Method to update a alert record.
  *
  * @param ipLoadInfo alert info. to update.
  * @throws DBException
  */
//  public void modifyLoad(LoadData ipNewLoadInfo) throws DBException
//  {
//    TransactionToken vpTok = null;
//    try
//    {
//      vpTok = startTransaction();
//      LoadData ipLoadInfo = mpAlert.getLoadData(ipNewLoadInfo.getLoadID());
//      mpAlert.modifyElement(ipNewLoadInfo);
//      logTransaction_LoadModify(ipNewLoadInfo, ipLoadInfo);
//      commitTransaction(vpTok);
//    }
//    catch(NoSuchElementException nse)
//    {
//      throw new DBException("Unable to modify load!", nse);
//    }
//    finally
//    {
//      endTransaction(vpTok);
//    }
//  }

  /**
   *  Method to add a alert without validation and without a transaction.
   *
   *  @param adData Filled in load data object.
   *  @exception DBException
   */
  protected boolean addAD(AlertsData adData) throws DBException
  {
    boolean vzRtn = false;
    vzRtn = mpAlert.createAlert(adData);
    // Record Load Add Transaction
    //logTransaction_AlertAdd(adData);
    return vzRtn;
  }
  

  /**
   *  Method to add a alert without validation.
   *
   *  @param ldData Filled in load data object.
   *  @exception DBException
   */
//  public boolean addAlert(AlertsData adData) throws DBException
//  {
//    initializeInventoryServer();
//    TransactionToken tt = null;
//    boolean vzRtn = false;
//    try
//    {
//      tt = startTransaction();
//      vzRtn = addAD(adData);
//      if (vzRtn)
//      {
//        commitTransaction(tt);
//      }
//    }
//    finally
//    {
//      endTransaction(tt);
//    }
//    return vzRtn;
//  }

  /**
   *  Method to see if the alert exists.
   *
   *  @param alertID Alert ID to look for.
   *  @return boolean of <code>true</code> if it exists.
   *  @exception DBException
   */
  public boolean alertExists(String alertID)
  {
    return mpAlert.exists(alertID);
  }
  
  /**
   *  Method to get data objects for matching alerts using ColumnObject.
   *
   * @param alertSearch no information available
   *  @return List of <code>AlertData</code> objects.
   *  @exception DBException
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getAlertDataList(AlertsData vpLoadKey) throws DBException
  {
    return mpAlert.getAllElements(vpLoadKey);
  }

  /**
   *  Method to get data objects for matching loads using KeyObject.
   *
   * @param alertSearch no information available
   *  @return List of <code>AlertData</code> objects.
   *  @exception DBException
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getAlertDataList(KeyObject[] loadSearch) throws DBException
  {
    return mpAlert.getAlertDataList(loadSearch);
  }

  /**
   *  Method to get a alert data object by alert ID.
   *
   *  @param alertID alert ID to get.
   *  @return AlertData object containing Load info. matching our
   *          search criteria.
   *  @exception DBException
   */
  public AlertsData getAlertl(String alertID) throws DBException
  {
    return mpAlert.getAlertData(alertID);
  }

 
 
 
  /*========================================================================*/
  /*  TRANSACTION HISTORY                                                   */
  /*========================================================================*/

  /**
   * Log a Alert Add to transaction history
   * @param adData - <code>AlertData</Code> - The added load
   */
//  public void logTransaction_AlertAdd(AlertsData adData)
//  {
//    tnData.clear();
//    tnData.setTranType(DBConstants.ADD_LOAD);
//    tnData.setTranCategory(DBConstants.LOAD_TRAN);
//    logTransaction(tnData);
//  }

   /**
   * Log a Load Modify to transaction history
   * @param ipNewLoadData - <code>LoadData</Code> - The new load data
   * @param ipOldLoadData - <code>LoadData</Code> - The old load data
   */
//  public void logTransaction_LoadModify(LoadData ipNewLoadData, LoadData ipOldLoadData)
//  {
//    tnData.clear();
//    tnData.setTranType(DBConstants.MODIFY_LOAD);
//    tnData.setTranCategory(DBConstants.LOAD_TRAN);
//    tnData.setLoadID(ipNewLoadData.getLoadID());
//    tnData.setLocation(ipOldLoadData.getWarehouse(), ipOldLoadData.getAddress());
//    tnData.setToLocation(ipNewLoadData.getNextWarehouse(), ipNewLoadData.getNextAddress());
//    tnData.setRouteID(ipNewLoadData.getRouteID());
//    logTransaction(tnData);
//  }

 
  /*========================================================================*/
  /*  End TRANSACTION HISTORY                                               */
  /*========================================================================*/

  protected void initializeDedicationServer()
  {
    if (mpDedServer == null)
    {
      mpDedServer = Factory.create(StandardDedicationServer.class, getClass().getSimpleName());
    }
  }

  protected void initializeInventoryServer()
  {
    if (mpInvServer == null)
    {
      mpInvServer = Factory.create(StandardInventoryServer.class, getClass().getSimpleName());
    }
  }

  protected void initializeLocationServer()
  {
    if (mpLocServer == null)
    {
      mpLocServer = Factory.create(StandardLocationServer.class, getClass().getSimpleName());
    }
  }

  protected void initializeMoveServer()
  {
    if (mpMoveServer == null)
    {
      mpMoveServer = Factory.create(StandardMoveServer.class, getClass().getSimpleName());
    }
  }

  protected void initializeOrderServer()
  {
    if (mpOrderServer == null)
    {
      mpOrderServer = Factory.create(StandardOrderServer.class, getClass().getSimpleName());
    }
  }

  protected void initializePickServer()
  {
    if (mpPickServer == null)
    {
      mpPickServer = Factory.create(StandardPickServer.class, getClass().getSimpleName());
    }
  }

  protected void initializePOReceivingServer()
  {
    if (mpPOServer == null)
    {
      mpPOServer = Factory.create(StandardPoReceivingServer.class, getClass().getSimpleName());
    }
  }

  protected void initializeRouteServer()
  {
    if (mpRouteServer == null)
    {
      mpRouteServer = Factory.create(StandardRouteServer.class, getClass().getSimpleName());
    }
  }

  protected void initializeStationServer()
  {
    if (mpStationServer == null)
    {
      mpStationServer = Factory.create(StandardStationServer.class, getClass().getSimpleName());
    }
  }


  /*========================================================================*/
  /*  End helper-server initialization methods                              */
  /*========================================================================*/
}
