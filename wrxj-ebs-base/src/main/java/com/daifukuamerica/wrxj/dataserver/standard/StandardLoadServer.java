package com.daifukuamerica.wrxj.dataserver.standard;

import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
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
public class StandardLoadServer extends StandardServer
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

  protected Load mpLoad = Factory.create(Load.class);
  protected   LoadData mpLoadData = Factory.create(LoadData.class);

  /**
   * Constructor for load with no parameters
   */
  public StandardLoadServer()
  {
    this(null);
  }

  /**
   *  Constructor for load with name of who is creating it and the scheduler name
   *
   *  @param isKeyName name of creator
   */
  public StandardLoadServer(String isKeyName)
  {
    super(isKeyName);
  }

  /**
   * Web application constructor for per user connection pooling
   * @param keyName
   * @param dbo
   */
  public StandardLoadServer(String keyName, DBObject dbo)
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
   *  Convenience method to read load record with <i>no</i> lock.
   *
   *  @return LoadData containing load record. <code>null</code> value if a
   *          database error occurs, or no record found.
   */
  public LoadData getLoad(String loadID)
  {
    return(getLoad(loadID, false));    // Read with no write lock.
  }

  /**
   *  Method to read a load record with/without a record lock.
   *
   *  @param loadID value of the loadid
   *  @param setWriteLock <code>boolean</code> set to <code>true</code> if
   *         record should be locked; <code>false</code> if read with <i>no</i> lock.
   *  @return LoadData containing load record. <code>null</code> value if a
   *          database error occurs, or no record found.
   */
  public LoadData getLoad(String loadID, boolean setWriteLock)
  {
    try
    {
      int lockFlag = (setWriteLock) ? DBConstants.WRITELOCK : DBConstants.NOWRITELOCK;
      return mpLoad.getLoadData(loadID, lockFlag);
    }
    catch(DBException e)
    {
      logException(e, "LoadId  \"" + loadID + "\" Read Error - "
          + getClass().getSimpleName() + ".getLoad");
      return null;
    }
  }
  /**
   * Gets the count of matching load.
   *
   * @param warehouse <code>String</code> containing location warehouse (null
   *            if all warehouses).
   * @param isDevice <code>String</code> containing location device (null if
   *            all devices).
   * @param empfyFlag <code>int</code> containing empty flag (0 if all
   *            states).
   * @param moveStatus <code>int</code> containing Move Status (0 if all
   *            states).
   */
  public int getLoadCount(String warehouse, String isDevice,
        int empfyFlag, int moveStatus) throws DBException
  {
    LoadData lddata = Factory.create(LoadData.class);

    if (warehouse != null && warehouse.trim().length() > 0)
    {
      lddata.setKey(LoadData.WAREHOUSE_NAME, warehouse);
    }

    if (isDevice != null && isDevice.trim().length() > 0)
    {
      lddata.setKey(LoadData.DEVICEID_NAME, isDevice);
    }

    if (empfyFlag > 0)
    {
      lddata.setKey(LoadData.AMOUNTFULL_NAME, Integer.valueOf(empfyFlag));
    }
    if (moveStatus > 0)
    {
      lddata.setKey(LoadData.LOADMOVESTATUS_NAME, Integer.valueOf(moveStatus));
    }

    return mpLoad.getCount(lddata);
  }

  /**
   *  read the database for the parent-load of the passed in load ID and return the value of the load table
   *  This data stays within the load server.
   *
   *  @param isLoadID value of the parentLoadid
   *  @return LoadData database table of all load information
   */
  public LoadData getParentLoad(String isLoadId)
  {
    try
    {
      return mpLoad.getParentLoadData(isLoadId);
    }
    catch (Exception e)
    {
      logException(e, "LoadId  \"" + isLoadId
          + "\" Exception getting Parent - " + getClass().getSimpleName()
          + ".getParentLoad");
      return null;
    }
  }

  /**
   *  Changes the move status of the parent load.
   *
   *  @param isParentLoad value of the parentLoadid
   *  @param inNewStatus of what you want for the load
   *  @param isMessage
   */
  public void setParentLoadMoveStatus(String isParentLoad, int inNewStatus,
      String isMessage)
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      if (inNewStatus == DBConstants.RETRIEVEPENDING)
      {
        LoadData vpLoadData = Factory.create(LoadData.class);
        vpLoadData.setKey(LoadData.PARENTLOAD_NAME, isParentLoad);
        vpLoadData.setLoadMoveStatus(inNewStatus);
        vpLoadData.setMoveDate(new Date());
        vpLoadData.setLoadMessage(isMessage);
        mpLoad.modifyElement(vpLoadData);
      }
      // TODO: It may be better to have Load.java handle this condition
      else if (inNewStatus == DBConstants.NOMOVE)
      {
        LoadData vpLoadData = Factory.create(LoadData.class);
        vpLoadData.setKey(LoadData.PARENTLOAD_NAME, isParentLoad);
        vpLoadData.setLoadMoveStatus(inNewStatus);
        vpLoadData.setMoveDate(new Date());
        vpLoadData.setNextWarehouse("");
        vpLoadData.setNextAddress("");
        vpLoadData.setFinalWarehouse("");
        vpLoadData.setFinalAddress("");
        mpLoad.modifyElement(vpLoadData);
      }
      else
      {
        setLoadMoveStatus(isParentLoad, inNewStatus, isMessage);
      }
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException(e, "LoadId \"" + isParentLoad
          + "\" Exception Changing Parent Load Status - "
          + getClass().getSimpleName() + ".setParentLoadMoveStatus");
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Get a random unique load id
   */
  public String createRandomLoadID()
  {
    Random rand = new Random();
    String loadID = "";
    boolean duplicateLoadID = true;
    do
    {
      loadID = "" + rand.nextInt(AGCDeviceConstants.LOADIDMAXVALUE);
      if(getLoad(loadID) == null)
      {
        duplicateLoadID = false;
      }
    }
    while(duplicateLoadID);

    return(loadID);
  }

  /**
   * Get a random unique load id using a prefix
   */
  public String createRandomLoadID(String isPrefix)
  {
    Random rand = new Random();
    String loadID = "";
    int maxValue = AGCDeviceConstants.LOADIDMAXVALUE;
    int vnMaxLength = AGCDeviceConstants.LNAGCLOADID;  // Easier tracking
    boolean duplicateLoadID = true;

    switch(isPrefix.length())
    {
    case 1:
    	maxValue = 9999998;
    	break;
    case 2:
    	maxValue = 999998;
    	break;
    case 3:
    	maxValue = 99998;
    	break;
    case 4:
    	maxValue = 9998;
    	break;
    }
    do
    {
      String vnRandString = "" + rand.nextInt(maxValue);
      while (vnRandString.length() < vnMaxLength-isPrefix.length())
      {
        vnRandString = "0" + vnRandString;
      }
      loadID = isPrefix + vnRandString;
      if(getLoad(loadID) == null)
      {
        duplicateLoadID = false;
      }
    }
    while(duplicateLoadID);

    return(loadID);
  }

  /**
   * Creates a Load ID from a sequence number and a word chosen from a list
   * @return A load ID with a word
   */
  public String createWordedLoadID()
  {
    TransactionToken tt;
    try
    {
      tt = startTransaction();
    }
    catch (DBException e)
    {
      return createRandomLoadID();
    }
    LoadWord vpLoadWord = Factory.create(LoadWord.class);
    WrxSequencer vpWrxSequencer = Factory.create(WrxSequencer.class);
    int vnLoadWordIndex;
    int vnLoadSequence;
    String vsLoadID;

    try
    {
      boolean vbDuplicateLoadID = true;
      do
      {
        vnLoadWordIndex = vpWrxSequencer.changeSequenceNumber("LoadWordIndex",
            "LoadServer", DBConstants.OTHER_SEQ);
        if (vnLoadWordIndex == 1)
        {
          vnLoadSequence = vpWrxSequencer.changeSequenceNumber("LoadSequence",
              "LoadServer", DBConstants.OTHER_SEQ);
        }
        else
        {
          vnLoadSequence = vpWrxSequencer.getCurrentSequenceNumber("LoadSequence",
              "LoadServer", DBConstants.OTHER_SEQ, DBConstants.NOWRITELOCK);
        }
        String vsLoadWord = vpLoadWord.getLoadWord(vnLoadWordIndex);
        vsLoadID = "" + vnLoadSequence + vsLoadWord;
        int vnLoadLength = DBInfo.getFieldLength(LoadData.LOADID_NAME);
        while (vsLoadID.length() < vnLoadLength)
        {
          vsLoadID = "0" + vsLoadID;
        }

        /*
         * This check is unnecessary for the first pass through, but there's
         * no guarantees after that.
         */
        if(getLoad(vsLoadID) == null)
        {
          vbDuplicateLoadID = false;
        }

      } while (vbDuplicateLoadID);
      commitTransaction(tt);
      return vsLoadID;
    }
    catch (Exception vpE)
    {
      logError("Error creating Load-Word ID");
      logException(vpE, "Error creating Load-Word ID");
      endTransaction(tt);
      return createRandomLoadID();
    }
  }

  /**
   * Get a random unique load id with a specified prefix.
   *
   * @param isPrefix prefix to use for new load id
   * @param ipLoadData load that needs a load id
   */
  public void createRandomLoad(String isPrefix, LoadData ipLoadData)
  {
    String vsLoadID = null;
    while(true)
    {
      vsLoadID = createRandomLoadID(isPrefix);
      if(getLoad(vsLoadID) == null)
      {
        break;
      }
    }

    ipLoadData.setParentLoadID(vsLoadID);
    ipLoadData.setLoadID(vsLoadID);
    ipLoadData.setMCKey("");
    ipLoadData.setMoveDate();
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      if(mpLoad.createLoad(ipLoadData) == false)
      {
        logError("LoadId \"" + ipLoadData.getParentLoadID()
            + "\" Already Exists - " + getClass().getSimpleName()
            + ".createRandomLoad(,)");
      }
      else
      {
        logTransaction_LoadAdd(ipLoadData);
        commitTransaction(tt);
      }
    }
    catch (Exception e)
    {
      logException(e, "LoadId \"" + vsLoadID + "\" Create Exception - "
          + getClass().getSimpleName() + ".createRandomLoad(,)");
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Get the oldest load that is at this station with this status
   * @param stationName station where oldest load is at
   * @param loadMoveStatus status of oldest load to get
   * @return LoadData of the oldest load
   */
  public LoadData getOldestLoadData(String stationName, int loadMoveStatus)
  {
    try
    {
      LoadData loadData = mpLoad.getOldestLoadData(stationName, loadMoveStatus);
      if (loadData != null)
      {
        logDebug("LoadId \"" + loadData.getLoadID()
            + "\" is oldest load at station " + stationName + " - "
            + getClass().getSimpleName() + ".getOldestLoadData()");
      }
      return loadData;
    }
    catch (Exception e)
    {
      logException(e, "Exception getting oldest Load at station " + stationName
          + " with move status of " + loadMoveStatus + " - "
          + getClass().getSimpleName() + ".getOldestLoad");
      return null;
    }
  }

  /**
   * Get the oldest load that is at this warehouse/station with this status
   * @param warehouse warehouse where oldest load is at
   * @param stationName station where oldest load is at
   * @param loadMoveStatus status of oldest load to get
   * @return LoadData of the oldest load
   */
  public LoadData getOldestLoadData(String warehouse, String stationName,
      int loadMoveStatus)
  {
    try
    {
      LoadData loadData = mpLoad.getOldestLoadData(warehouse, stationName, loadMoveStatus);
      if(loadData != null)
      {
        logDebug("LoadId \"" + loadData.getParentLoadID()
            + "\" oldest load at station " + stationName + " - "
            + getClass().getSimpleName() + ".getOldestLoadData()");
      }
      return loadData;
    }
    catch (Exception e)
    {
      logException(e, "Exception getting oldest load at station " + stationName
          + " with move status of " + loadMoveStatus + "\"  - "
          + getClass().getSimpleName() + ".getOldestLoad");
      return null;
    }
  }

  /**
   * Get the previous load ordered by Load ID
   * @param loadid where loadid is the current load
   * @return LoadData of the previous load
   */
  public LoadData getPreviousLoadData(String sLoadID)
  {
    try
    {
      LoadData loadData = mpLoad.getPreviousLoadData(sLoadID);
      if(loadData != null )
      {
        logDebug("Load ID \" " + sLoadID + "\" previous load"
            + loadData.getLoadID());
      }
      return loadData;
    }
    catch( Exception e)
    {
      logException(e, "Exception getting previous load data " + sLoadID + " - "
          + getClass().getSimpleName() + ".getPreviousLoad");
    }
    return null;
  }

  /**
   * Update the load in the database to the current values and (optionally)
   * change movedate.
   *
   * @param loadData load that needs to be updated
   */
  public void updateLoadData(LoadData loadData, boolean izUpdateMoveDate)
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();

      LoadData vpOldLoadData = getLoad(loadData.getLoadID());

      if(!loadData.getParentLoadID().equals(loadData.getLoadID()))
      {
        if(anyItemMovesForLoad(loadData.getLoadID()))
        {
          throw new DBException("Load ''" + loadData.getLoadID()
              + " has a item moves and cannot be Consolidated");
        }
        deleteLoadMoveForLoad(loadData.getLoadID());
      }
      if (izUpdateMoveDate)
        loadData.setMoveDate();
      mpLoad.updateLoadInfo(loadData);

      /*
       * Log transaction history if this is a move
       */
      if (!vpOldLoadData.getWarehouse().equals(loadData.getWarehouse()) ||
          !vpOldLoadData.getAddress().equals(loadData.getAddress()))
      {
        logLoadMoveTransaction(loadData.getLoadID(),
            vpOldLoadData.getWarehouse(), vpOldLoadData.getAddress(),
            vpOldLoadData.getShelfPosition(), loadData.getWarehouse(),
            loadData.getAddress(), loadData.getShelfPosition(),
            loadData.getDeviceID());
      }

      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException(e, "LoadId \"" + loadData.getParentLoadID()
          + "\" (Parent) Exception Changing Parent Load Status - "
          + getClass().getSimpleName() + ".setParentLoadMoveStatus");
    }
    finally
    {
      endTransaction(tt);
    }
  }

 /**
  * Method to update a load record.
  *
  * @param ipLoadInfo load info. to update.
  * @throws DBException
  */
  public void modifyLoad(LoadData ipNewLoadInfo) throws DBException
  {
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      LoadData ipLoadInfo = mpLoad.getLoadData(ipNewLoadInfo.getLoadID());
      mpLoad.modifyElement(ipNewLoadInfo);
      logTransaction_LoadModify(ipNewLoadInfo, ipLoadInfo);
      commitTransaction(vpTok);
    }
    catch(NoSuchElementException nse)
    {
      throw new DBException("Unable to modify load!", nse);
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

  /**
   *  Method to add a load without validation and without a transaction.
   *
   *  @param ldData Filled in load data object.
   *  @exception DBException
   */
  protected boolean addLD(LoadData ldData) throws DBException
  {
    boolean vzRtn = false;
    vzRtn = mpLoad.createLoad(ldData);
    // Record Load Add Transaction
    logTransaction_LoadAdd(ldData);
    return vzRtn;
  }

  /**
   *  Method to add a load without validation.
   *
   *  @param ldData Filled in load data object.
   *  @exception DBException
   */
  public boolean addLoad(LoadData ldData) throws DBException
  {
    initializeInventoryServer();
    TransactionToken tt = null;
    boolean vzRtn = false;
    try
    {
      tt = startTransaction();
      ldData.setMoveDate();
      vzRtn = addLD(ldData);
      if (vzRtn)
      {
        mpInvServer.setLocationEmptyStatus(ldData.getWarehouse(),
            ldData.getAddress(), ldData.getShelfPosition());
        commitTransaction(tt);
      }
    }
    finally
    {
      endTransaction(tt);
    }
    return vzRtn;
  }

  /**
   * Convenience method - calls StandardInventoryServer.deleteShippingLoad()
   *
   * @param loadID
   * @param reasonCode
   */
  public void deleteShippingLoad(String loadID, String reasonCode)
  {
    initializeInventoryServer();

    try
    {
      mpInvServer.deleteShippingLoad(loadID, reasonCode);
    }
    catch (DBException e)
    {
      logException(e, "LoadId \"" + loadID + "\" Delete Exception - "
          + getClass().getSimpleName() + ".deleteLoad");
    }
  }

  /**
   *  Update a load's next warehouse and address.
   *
   *  @param loadID <code>String</code> containing id. of load being modified.
   *  @param sWarehouse <code>String</code> containing next warehouse load is
   *         going to.
   *  @param nextAddress <code>String</code> containing next location address
   *         load is going to.
   *  @throws com.daifukuamerica.wrxj.common.jdbc.DBException
   */
  public void updateLoadNextLocation(String loadID, String sWarehouse,
      String nextAddress) throws DBException
  {
    TransactionToken vpTT = null;
    try
    {
      vpTT = startTransaction();
      LoadData loadData = Factory.create(LoadData.class);
      loadData.setNextWarehouse(sWarehouse);
      loadData.setNextAddress(nextAddress);
      loadData.setKey(LoadData.LOADID_NAME, loadID);

      mpLoad.modifyElement(loadData);
      commitTransaction(vpTT);
    }
    finally
    {
      endTransaction(vpTT);
    }
  }

  /**
   * Method to get load's current location. <strong>Note:</strong> this method
   * puts a dash between the warehouse and address. The caller can separate
   * these two pieces using <strong>Location.parseLocation(String locnString)</strong>
   * if necessary.
   *
   * @param isLoadID <code>String</code> containing load.
   * @return a string of the form WWW-AAAAAAAAA where W=warehouse, and
   *         A=address. <b>Note:</b> the warehouse is <i>not</i> blank padded
   *         if it is less than 3 characters. An empty string is returned if no
   *         matching load is found.
   */
  public String getLoadLocation(String isLoadID)
  {
    String vsLocation = "";

    try
    {
      vsLocation = mpLoad.getLoadLocation(isLoadID);
    }
    catch(DBException exc)
    {
      logDebug("No location string found for Load " + isLoadID +
              ":::: " + exc.getMessage());
    }

    return(vsLocation);
  }

  /**
   *  Method to get load's current address only not warehouse address.
   *  <strong>Note:</strong> this method only returns the warehouse
   *
   *  @param isLoadID the load id.
   *  @return <code>String</code> containing warehouse of load. Empty string
   *          if warehouse is missing.
   * @throws DBException DB errors.
   */
  public String getLoadWarehouse(String isLoadID) throws DBException
  {
    String vsLoadLocation = mpLoad.getLoadLocation(isLoadID);
    String[] vasLocn = Location.parseLocation(vsLoadLocation);
    if (vasLocn == null || vasLocn.length == 0)
    {
      return("");
    }
    return(vasLocn[0]);
  }

  /**
   *  Method to get load's current address only not warehouse address.
   *  <strong>Note:</strong> this method only returns the address
   *
   *  @param isLoadID the load id.
   *  @return <code>String</code> containing address of load. Empty string
   *          if address is missing.
   * @throws DBException DB errors.
   */
  public String getLoadAddress(String isLoadID) throws DBException
  {
    String vsLoadLocation = mpLoad.getLoadLocation(isLoadID);
    String[] vasLocn = Location.parseLocation(vsLoadLocation);
    if (vasLocn == null || vasLocn.length <= 1)
    {
      return("");
    }
    return(vasLocn[1]);
  }

  /**
   * Method to get load's Device ID. <strong>Note:</strong> this method only
   * returns the Device ID
   *
   * @param isLoadID <code>String</code> containing load.
   *
   * @return <code>String</code> containing Device ID of load.
   *         <code>null</code> if there is an error.
   */
  public String getLoadDeviceID(String isLoadID)
  {
    String vsDeviceID = null;
    try
    {
      vsDeviceID = (String)mpLoad.getSingleColumnValue(isLoadID,
          LoadData.DEVICEID_NAME);
      if (vsDeviceID == null)
        vsDeviceID = "";
    }
    catch(DBException e)
    {
      logException(e, "Error checking for Load " + isLoadID + "'s Device");
    }

    return vsDeviceID;
  }

  /**
   * Update a load's move status
   *
   * @param isLoadID
   * @param inMoveStatus
   * @param isMessage
   */
  public void setLoadMoveStatus(String isLoadID, int inMoveStatus,
      String isMessage)
  {
    String sPrev = "*None*";
    String s = "" + inMoveStatus;
    String field = "iLoadMoveStatus";
    int previousStatus = -1;
    try
    {
      previousStatus = mpLoad.getLoadMoveStatusValue(isLoadID);
    }
    catch (DBException e) {}

    try
    {
      if (previousStatus != -1)
      {
        sPrev = DBTrans.getStringValue(field, previousStatus);
      }
      s = DBTrans.getStringValue(field, inMoveStatus);
    }
    catch (Exception e)
    {
      logError(getClass().getSimpleName() + " - DBTrans CANNOT find " + field
          + " - setLoadDataMoveStatus()");
    }
    try
    {
      mpLoad.setLoadMoveStatusValue(isLoadID, inMoveStatus, isMessage);
    }
    catch (DBException e)
    {
      logException(e, "LoadId \"" + isLoadID + "\" - New Status: " + s);
    }
    logDebug("LoadId \"" + isLoadID + "\" - New Status: " + s
        + " - Previous Status: " + sPrev + " - Load - "
        + getClass().getSimpleName());
  }

  /**
   *  Method to see if the load exists.
   *
   *  @param loadID Load ID to look for.
   *  @return boolean of <code>true</code> if it exists.
   *  @exception DBException
   */
  public boolean loadExists(String loadID)
  {
    return mpLoad.exists(loadID);
  }

  /**
   *  Method to set the loads amount full value.
   *
   *  @param loadid Load ID to be set.
   *  @param amountFull Amount full value.
   *  @exception DBException
   */
  public void setLoadAmountFull(String loadid, int amountFull) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      mpLoad.updateLoadAmountFull(loadid, amountFull);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Method to set the group number that is used in a retrieve on a load
   * @param isLoadID
   * @param inGroupNo
   * @throws DBException
   */
  public void setGroupNoOnLoad(String isLoadID, int inGroupNo) throws DBException
  {
    mpLoadData.clear();
    mpLoadData.setGroupNo(inGroupNo);
    mpLoadData.setKey(LoadData.GROUPNO_NAME, isLoadID);
    modifyLoad(mpLoadData);
  }

/**
 * Method checks if a load at a given station.
 * @param isLoadID the load to check
 * @param isStation the station to check
 * @return <code>true</code> if load is present at station, <code>false</code>
 *         otherwise.
 */
  public boolean isLoadAtStation(String isLoadID, String isStation)
  {
    boolean vzRtn;

    try
    {
      String vsCurrStation = getLoadAddress(isLoadID).trim();
      vzRtn = (!vsCurrStation.isEmpty() && vsCurrStation.equals(isStation.trim()));
    }
    catch(DBException ex)
    {
      vzRtn = false;
      logError("Database access error finding load address for load "
          + isLoadID + ex.getMessage());
    }

    return(vzRtn);
  }

 /**
  * Method to tell if a load is empty.
  * @param isLoadID the load being examined.
  * @return <code>true</code>. if load is empty, <code>false</code> otherwise.
  */
  public boolean isLoadEmpty(String isLoadID)
  {
    initializeInventoryServer();
    return(mpInvServer.getLoadLineCount(isLoadID) == 0);
  }

  /**
   *  Method to check if a load is in the Rack.
   *
   *  @param sLoadID <code>String</code> containing load to check.
   *
   *  @return <code>boolean true</code> if load is in RETRIEVEPENDING or NOMOVE
   *          state. false otherwise.
   */
  public boolean isLoadInRack(String sLoadID)
  {
    boolean rtn = false;
    try
    {
      int ldMoveStatus = mpLoad.getLoadMoveStatusValue(sLoadID);
      if (ldMoveStatus == DBConstants.RETRIEVEPENDING
                         ||
          ldMoveStatus == DBConstants.NOMOVE)
      {
        rtn = true;
      }
    }
    catch(DBException e)
    {
      logDebug("LoadId \" " + sLoadID +
               "\" Error - StandardInventoryServer.isLoadStationary()");
    }

    return(rtn);
  }

  /**
   *  Method to update the load information.
   *
   *  @param ipNewLoadData Load information.
   *  @exception DBException
   */
  // TODO: Is this any different than updateLoadData?
  public void updateLoadInfo(LoadData ipNewLoadData) throws DBException
  {
    initializeInventoryServer();

    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      // if this load has changed locations then we need to update the
      // location empty statuses
      boolean vzLocationChanged = false;
      LoadData vpOldLoadData = getLoad(ipNewLoadData.getLoadID(), true);
      if ((vpOldLoadData != null)
        && ((!ipNewLoadData.getWarehouse().trim().equals(vpOldLoadData.getWarehouse().trim()))
        || (!ipNewLoadData.getAddress().trim().equals(vpOldLoadData.getAddress().trim()))))
      {
        vzLocationChanged = true;
      }

      /*
       * Update the load
       */
      mpLoad.updateLoadInfo(ipNewLoadData);

      /*
       * Record Load Change Transaction
       */
      logTransaction_LoadModify(ipNewLoadData, vpOldLoadData);

      /*
       * Update location empty statuses
       */
      if (vzLocationChanged)
      {
        mpInvServer.setLocationEmptyStatus(vpOldLoadData.getWarehouse(),
            vpOldLoadData.getAddress(), vpOldLoadData.getShelfPosition());
        mpInvServer.setLocationEmptyStatus(ipNewLoadData.getWarehouse(),
            ipNewLoadData.getAddress(), ipNewLoadData.getShelfPosition());
        logLoadMoveTransaction(ipNewLoadData.getLoadID(),
            vpOldLoadData.getWarehouse(), vpOldLoadData.getAddress(), vpOldLoadData.getShelfPosition(),
            ipNewLoadData.getWarehouse(), ipNewLoadData.getAddress(), ipNewLoadData.getShelfPosition(),
            ipNewLoadData.getDeviceID());
      }
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   *  Method to get data objects for matching loads using ColumnObject.
   *
   * @param loadSearch no information available
   *  @return List of <code>LoadData</code> objects.
   *  @exception DBException
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getLoadDataList(LoadData ipLoadKey) throws DBException
  {
    return mpLoad.getAllElements(ipLoadKey);
  }

  /**
   *  Method to get data objects for matching loads using KeyObject.
   *
   * @param loadSearch no information available
   *  @return List of <code>LoadData</code> objects.
   *  @exception DBException
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getLoadDataList(KeyObject[] loadSearch) throws DBException
  {
    return mpLoad.getLoadDataList(loadSearch);
  }

 /**
  * Method to get an array of Load IDs ordered by the ID's
  *
  * @param isFirstElement One of the following constants:<ul>
  *        <li>{@link com.daifukuamerica.wrxj.util.SKDCConstants#ALL_STRING
  *              SKDCConstants.ALL_STRING}
  *            which prepends the string "ALL" to the array.</li>
  *        <li>{@link com.daifukuamerica.wrxj.util.SKDCConstants#NONE_STRING
  *              SKDCConstants.NONE_STRING}
  *            which prepends the string "NONE" to the array</li>
  *        <li>{@link com.daifukuamerica.wrxj.util.SKDCConstants#EMPTY_VALUE
  *              SKDCConstants.EMPTY_VALUE}
  *            which prepends a blank string to the array.</li>
  *        <li>{@link com.daifukuamerica.wrxj.util.SKDCConstants#NO_PREPENDER
  *              SKDCConstants.NO_PREPENDER}
  *            which means there is no prepender (no pre-defined first element)
  *            to the list.</li></ul>
  *
  * @return ordered array of Load IDs.
  * @throws DBException if there is a DB access error.
  */
  public String[] getLoadIDList(final String isFirstElement) throws DBException
  {
    mpLoadData.clear();
    mpLoadData.setOrderByColumns(LoadData.LOADID_NAME);
    return(mpLoad.getSingleColumnValues(LoadData.LOADID_NAME, true, mpLoadData,
                                        isFirstElement));
  }

  /**
   * Method retrieves a load record using barcode (actually TrackingId) as key.
   *
   * @param isBarcode <code>String</code> containing barcode (tracking id) to
   *            search for.
   * @param withLock <code>int</code> flag indicating if record should be
   *            locked.
   *
   * @return <code>LoadData</code> object. <code>null</code> if no record
   *         found.
   */
  public LoadData getLoadDataFromTrackingId(String isTrackingId, int withLock)
      throws DBException
  {
    return mpLoad.getLoadDataFromTrackingId(isTrackingId, withLock);
  }

  /**
   *  Gets list of all loads on the order that fit the location criteria.
   */
  public String[] getLoadChoices(String order, LocationData lcdata, boolean insertAll)
      throws DBException
  {
    String[] loadlist = null;
    TableJoin tableJoin = Factory.create(TableJoin.class);

    try
    {
      if (insertAll)
        loadlist = tableJoin.getLoadChoices(order, lcdata, SKDCConstants.ALL_STRING);
      else
        loadlist = tableJoin.getLoadChoices(order, lcdata, "");
    }
    catch (DBException exc)
    {
      logException(exc, "getOrderLineChoices");
      throw exc;
    }

    return (loadlist);
  }

  /**
   *  Method to get a load data object by load ID.
   *
   *  @param loadID Load ID to get.
   *  @return LoadData object containing Load info. matching our
   *          search criteria.
   *  @exception DBException
   */
  public LoadData getLoad1(String loadID) throws DBException
  {
    return mpLoad.getLoadData(loadID);
  }

  /**
   *  Method to get a load data object for the oldest load at the specified
   *  location and with the specified load status.
   *
   *  @param warehouse Warehouse to match.
   *  @param address Address to match.
   *  @param loadStatus Load status to look for.
   *  @return LoadData object containing Load info. matching our
   *  search criteria.
   *  @exception DBException
   */
  public LoadData getOldestLoad(String warehouse, String address, int loadStatus)
      throws DBException
  {
    return mpLoad.getOldestLoadData(warehouse, address, loadStatus,
        DBConstants.NOWRITELOCK);
  }

  /**
   *  Method to get a load data object for the oldest load at the specified
   *  location.
   *
   *  @param warehouse Warehouse to match.
   *  @param address Address to match.
   *  @return LoadData object containing Load info. matching our
   *  search criteria.
   *  @exception DBException
   */
  public LoadData getOldestLoad(String warehouse, String address)
      throws DBException
  {
    return getOldestLoad(warehouse, address, 0);
  }

  /**
   *  Method to get a load move status
   *
   *  @param loadID
   *  @return loadMoveStatus
   *  @exception DBException
   */
  public int getLoadMoveStatus(String loadID) throws DBException
  {
      int ldMoveStatus = mpLoad.getLoadMoveStatusValue(loadID);
      return ldMoveStatus;
  }

  /**
   * Method checks if a load is at a conventional location.
   *
   * @param isLoadID the load in question.
   * @return <code>true</code> if the load is at a conventional location,
   *         <code>false</code> otherwise.
   * @throws DBException
   */
  @UnusedMethod
  public boolean isLoadInConventionalLocation(String isLoadID)
      throws DBException
  {
    initializeLocationServer();

    boolean vzRtn = false;
    try
    {
      String vpLocn[] = Location.parseLocation(mpLoad.getLoadLocation(isLoadID));
      vzRtn = mpLocServer.isConventionalLocation(vpLocn[0], vpLocn[1]);
    }
    catch(DBException e)
    {
      logDebug("LoadId \"" + isLoadID + "\" Error - "
          + getClass().getSimpleName() + ".isLoadInConventionalLocation()");
    }

    return(vzRtn);
  }

  /**
   *  Method to see if the load is stored in the rack or is at a station
   *
   *  @param loadID
   *  @param moveStatus
   *  @return True if the load is at a station or in a location
   *  search criteria.
   *  @exception DBException
   */
  public boolean isLoadMoveStatus(String loadID, int moveStatus)
  {
    try
    {
      int ldMoveStatus = mpLoad.getLoadMoveStatusValue(loadID);
      if(ldMoveStatus == moveStatus)
      {
        return true;
      }
      else
      {
        return false;
      }
    }
    catch(DBException e)
    {
      logDebug("LoadId \"" + loadID + "\" Error - "
          + getClass().getSimpleName() + ".isLoadAtStationOrInRack()");
      return false;
    }
  }

  /**
   * Returns the next warehouse of the loadid
   * @param loadId to get next warehouse
   * @return String nextWarehouse
   */
  public String getNextWarehouse(String loadId)
  {
    LoadData loadData = getLoad(loadId);
    return loadData.getNextWarehouse();
  }

  /**
   * Returns the next load data ordered by Load ID
   * @param current loadID to get next load data
   * @return LoadData
   */
  public LoadData getNextLoadData(String sLoadID)
  {
    try
    {
      LoadData loadData = mpLoad.getNextLoadData(sLoadID);
      if(loadData != null )
      {
        logDebug("Load ID " + sLoadID + "\" next load" +
                 loadData.getLoadID());
      }
      return loadData;
    }
    catch( Exception e)
    {
      logException(e, "Exception getting next load data " + sLoadID + " - "
          + getClass().getSimpleName() + ".getNextLoad");
    }
    return null;
  }

  /**
   * Get the next route location for a load.
   *
   * <p><b>Details:</b> This method gets the next warehouse and next address
   * for the load data passed in and set the properties accordingly.  This
   * information is determined from the route data and the loads current
   * station's route.
   *
   * <p>If the current load station's route is not defined not next route
   * data will be set.</p>
   *
   * @param ipLoadData  The load data to work on
   */
  public void getNextRouteLocation(StationData ipStationData, LoadData ipLoadData)
    throws DBException
  {
    StandardRouteServer routeServer = Factory.create(StandardRouteServer.class);

    RouteData vpRouteData = routeServer.getNextRouteData(ipStationData.getDefaultRoute(),
        ipLoadData.getAddress());

    if (vpRouteData != null)
    {
      ipLoadData.setNextWarehouse(ipLoadData.getWarehouse());

      // If we are a DestType of Equipment we don't want to set
      // the address here it will get set in the reserverLocationForDevice
      if (vpRouteData.getDestType() != DBConstants.EQUIPMENT)
      {
        ipLoadData.setNextAddress(vpRouteData.getDestID());
      }
    }
  }

  /**
   * Fetch a value that if true, the Storing Equipment will only accept
   * the Store command if a load is present at the Storing Station when
   * the command is received. If false, the Storing Equipment will
   * accept the Store command even if there is not a load at the Storing
   * Station when the command is received (the Store command will be
   * applied to the next arriving load).  The default value is TRUE
   * (only accept the command if a Load is present).
   *
   *  @param loadID load identifier
   *  @return true if Load Presence is Yes (Load must be at Store Station)
   */
  public boolean getLoadPresenceRequired(String isLoadID)
  {
    boolean vbResult = mpLoad.getLoadPresenceRequired(isLoadID);
    return vbResult;
  }

  /**
   * Move a load to a station and set the move status
   *
   * @param sLoadID - the load ID to move
   * @param sStationID - the station to which to move
   * @param iMoveStatus - the move status for the load, post-move
   * @param iMoveDateFlag - 0: make oldest, 1: make newest, 2: don't change
   */
  public void moveLoadToStation(String sLoadID, String sStationID,
    int iMoveStatus, int iMoveDateFlag)
  {
    initializeLocationServer();
    initializeStationServer();

    TransactionToken tt = null;
    String vsLastWarehouse, vsLastAddress, vsLastPosition;

    try
    {
      tt = startTransaction();
      LoadData vpLoadData = getLoad(sLoadID);
      if (vpLoadData != null)
      {
        vsLastWarehouse = vpLoadData.getWarehouse();
        vsLastAddress   = vpLoadData.getAddress();
        vsLastPosition  = vpLoadData.getShelfPosition();

        vpLoadData.setParentLoadID(vpLoadData.getLoadID());
        vpLoadData.setLoadID(vpLoadData.getLoadID());
        vpLoadData.setWarehouse(mpStationServer.getStationWarehouse(sStationID));
        vpLoadData.setAddress(sStationID);
        vpLoadData.setLoadMoveStatus(iMoveStatus);
        vpLoadData.setDeviceID(mpLocServer.getLocationDeviceId(vpLoadData.getWarehouse(), vpLoadData.getAddress()));
        switch (iMoveDateFlag)
        {
          case 0:  // Oldest move date
            LoadData oldestLoad = getOldestLoadData(sStationID, 0);
            if ((oldestLoad != null) &&
                (!oldestLoad.getLoadID().equals(vpLoadData.getLoadID())))
            {
              Date vpOldestDate = oldestLoad.getMoveDate();
              vpOldestDate.setTime(vpOldestDate.getTime() - 1000);
              vpLoadData.setMoveDate(vpOldestDate);
            }
            break;

          case 1:  // Current move date
            vpLoadData.setMoveDate();
            break;

          default:  // No change
            break;
        }
        mpLoad.updateLoadInfo(vpLoadData);

        /*
         * If the load came from an AS/RS location, update the status
         */
        if (mpLocServer.getLocationTypeValue(vsLastWarehouse, vsLastAddress)
            == DBConstants.LCASRS)
        {
          mpLocServer.setLocationEmptyFlag(vsLastWarehouse, vsLastAddress,
              vsLastPosition, DBConstants.UNOCCUPIED);
        }

        /*
         * Log the move
         */
        logLoadMoveTransaction(sLoadID, vsLastWarehouse, vsLastAddress, vsLastPosition,
            vpLoadData.getWarehouse(), vpLoadData.getAddress(), vpLoadData.getShelfPosition(),
            vpLoadData.getDeviceID());

        commitTransaction(tt);

        logDebug(getClass().getSimpleName() + ".moveLoadToStation - Load ID \""
            + sLoadID + "\" moved to Station " + sStationID);
      }
      else
      {
        logError(getClass().getSimpleName() + ".moveLoadToStation - Load ID \""
            + sLoadID + "\" NOT found");
      }
    }
    catch (DBException e)
    {
      logException(e, getClass().getSimpleName()
          + ".moveLoadToStation - LoadID \"" + sLoadID
          + "\" (Parent) Exception Changing Parent Load Status");
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Move a load to a Location and set the move status
   *
   * @param sLoadID - the load ID to move
   * @param sWarehouse - the Warehouse part of the location
   * @param sAddress   - the address part of the location
   * @param iMoveStatus - the move status for the load, post-move
   * @param iMoveDateFlag - 0: make oldest, 1: make newest, 2: don't change
   * TODO: Break this up!  It's nearly 400 lines!
   */
  @SuppressWarnings("rawtypes")
  public void moveLoadToLocation(String sLoadID, String sWarehouse,
      String sAddress, String sPosition) throws DBException
  {
    initializeDedicationServer();
    initializeInventoryServer();
    initializeLocationServer();
    initializeMoveServer();
    initializeOrderServer();
    initializeRouteServer();

    TransactionToken tt = null;
    boolean shiploc = false;
    boolean destIsShipLoc = false;
    boolean pickupToDevice = false;
    boolean conveyorDeposit = false;

    try
    {
      Move move = Factory.create(Move.class);
      LoadData loadData = Factory.create(LoadData.class);
      LocationData depositlcData = Factory.create(LocationData.class);

      depositlcData = mpLocServer.getLocationRecord(sWarehouse, sAddress);
      if(depositlcData == null)
      {
        throw new DBException("Invalid Location: " + sWarehouse + "-" + sAddress +
                 " Depositing Load: " + sLoadID);
      }

      // The following requested under Change request 684 for Merit
      if(depositlcData.getLocationStatus() == DBConstants.LCUNAVAIL)
      {
        throw new DBException("Location Unavailable: " + sWarehouse + "-" + sAddress);
      }

      int loctype = depositlcData.getLocationType();

      // The following requested under Change request 685 for Merit
      // Do not allow moves into dedicated locations for loads containing items not
      // dedicated to this location.
      if (loctype == DBConstants.LCDEDICATED)
      {
        moveLoadToLocationCheckDedication(sLoadID, sWarehouse, sAddress);
      }

      if(loctype == DBConstants.LCCONSOLIDATION || loctype == DBConstants.LCSTAGING ||
         loctype == DBConstants.LCSHIPPING)
      {
        shiploc = true;
      }
      else if(loctype == DBConstants.LCDEVICE)
      {
        pickupToDevice = true;
      }
      StationData stdataSearch = Factory.create(StationData.class);
      stdataSearch.setKey(StationData.STATIONNAME_NAME, depositlcData.getAddress());
      StationData stdata = Factory.create(Station.class).getElement(stdataSearch, DBConstants.NOWRITELOCK);
      if (stdata != null)
      {
        if(stdata.getStationType() == DBConstants.CONVEYOR)
        {
          conveyorDeposit = true;
        }
      }
      tt = startTransaction();

               // If trying to move a load that is part of a different Parent Load -
               // Throw an exception - they must move the parent load
      loadData = getParentLoad(sLoadID);
      if (!loadData.getLoadID().equals(sLoadID))
      {
        throw new DBException("Cannot Move Load: '" + sLoadID
            + "' - Must Move Parent Load: '" + loadData.getLoadID() + "'");
      }

      String oldWarehouse = loadData.getWarehouse();
      String oldAddress = loadData.getAddress();
      String oldPosition = loadData.getShelfPosition();
      String vpOrder = "";
      String nextWarehouse = "";
      String nextAddress = "";
      String destWarehouse = "";
      String destAddress = "";

      boolean finalloc = false;
      int mvcount = 0;
      List<Map> mvList;
      MoveData moveData = Factory.create(MoveData.class);

      moveData.setKey(MoveData.PARENTLOAD_NAME, loadData.getParentLoadID());
      moveData.setKey(MoveData.MOVETYPE_NAME, Integer.valueOf(DBConstants.LOADMOVE));

      mvList = mpMoveServer.getMoveDataList(moveData.getKeyArray());

      if(mvList != null)
      {
        mvcount = mvList.size();
        if (mvcount == 1)
        {
          moveData.dataToSKDCData(mvList.get(0));
          destWarehouse = moveData.getDestWarehouse();
          destAddress = moveData.getDestAddress();
          vpOrder = moveData.getOrderID();
          if(destWarehouse == null || destWarehouse.trim().length() < 1 ||
              destAddress == null || destAddress.trim().length() < 1)
          {
            if(vpOrder.trim().length() > 0)
            {
              OrderHeaderData orddata = mpOrderServer.getOrderHeaderRecord(vpOrder);
              destWarehouse = orddata.getDestWarehouse();
              destAddress = orddata.getDestAddress();
              MoveData mvUpdate = Factory.create(MoveData.class);
              mvUpdate.setDestWarehouse(destWarehouse);
              mvUpdate.setDestAddress(destAddress);
              mvUpdate.setKey(MoveData.MOVEID_NAME, moveData.getMoveID());
              move.modifyElement(mvUpdate);
            }
          }
        }
      }
      LocationData destlocData = Factory.create(LocationData.class);


      destlocData = mpLocServer.getLocationRecord(destWarehouse, destAddress);
      if(destlocData == null)
      {

        // If there was no destination found, just set the destination
        // to the warehouse and address we are moving the load to.
        destlocData = depositlcData;
      }
      int dloctype = destlocData.getLocationType();
      if (dloctype == DBConstants.LCCONSOLIDATION
          || dloctype == DBConstants.LCSTAGING
          || dloctype == DBConstants.LCSHIPPING)
      {
        destIsShipLoc = true;
      }

      // If this is the final location of the load or there are no moves
      // get rid of all of the moves (should be completed)

      if( mvcount < 1 ||
          ( loadData.getFinalWarehouse().equals(sWarehouse) &&
              loadData.getFinalAddress().equals(sAddress)) ||
              ( moveData.getDestWarehouse().equals(sWarehouse) &&
                  moveData.getDestAddress().equals(sAddress)) )
      {
        finalloc = true;
      }
      else
      {
        nextAddress = destAddress;
        nextWarehouse = destWarehouse;
        String tmpaddress = mpRouteServer.getNextRouteDest(
            moveData.getRouteID(), sAddress);
        if (tmpaddress != null && tmpaddress.trim().length() > 0)
        {
          stdataSearch = Factory.create(StationData.class);
          stdataSearch.setKey(StationData.STATIONNAME_NAME, nextAddress);
          stdata = Factory.create(Station.class).getElement(stdataSearch, DBConstants.NOWRITELOCK);
          if (stdata != null)
          {
            if(stdata.getStationType() == DBConstants.CONVEYOR)
            {
              conveyorDeposit = true;
            }
            String tmpwar = stdata.getWarehouse();
            if(tmpwar != null && tmpwar.trim().length() > 0)
            {
              nextWarehouse = tmpwar;
              nextAddress = tmpaddress;
            }
          }
        }
      }

      if(mvcount > 0)
      {
        for(int i = 0; i < mvList.size(); i++)
        {
          Map tmap = mvList.get(i);
          moveData.dataToSKDCData(tmap);
          moveData.setKey(MoveData.MOVEID_NAME, Integer.valueOf(moveData.getMoveID()));
          if(finalloc)
          {
            // We should only have to deal with load moves - the others should be gone.
            if(moveData.getMoveType() == DBConstants.LOADMOVE)
            {
              mpMoveServer.deleteMove(moveData);
            }
          }
          else
          {
            // We should only have to deal with load moves - the others should be gone.
            if(moveData.getMoveType() == DBConstants.LOADMOVE)
            {
              if(pickupToDevice == true)
              {
                moveData.setMoveStatus(DBConstants.ASSIGNED);
              }
              else
              {
                moveData.setMoveStatus(DBConstants.AVAILABLE);
                moveData.setDeviceID("");
              }
              moveData.setNextWarehouse(nextWarehouse);
              moveData.setNextAddress(nextAddress);
              move.modifyElement(moveData);
            }
          }
        }
      }
      else
      {
        // We may just be picking the load up to move it
        if(pickupToDevice == true)
        {
          finalloc = false;
        }
      }

      // Now update the Loads information
      loadData.setWarehouse(sWarehouse);
      loadData.setAddress(sAddress);
      loadData.setDeviceID(depositlcData.getDeviceID());
      loadData.setMoveDate();
      if(finalloc)
      {
        loadData.setLoadMoveStatus(DBConstants.NOMOVE);
        loadData.setNextWarehouse("");
        loadData.setNextAddress("");
        loadData.setFinalWarehouse("");
        loadData.setFinalAddress("");
        loadData.setLoadMessage("");
      }
      else
      {
        if(pickupToDevice == true)
        {
          loadData.setLoadMoveStatus(DBConstants.MOVING);
        }
        else
        {
          loadData.setLoadMoveStatus(DBConstants.MOVEPENDING);
        }
        loadData.setNextWarehouse(nextWarehouse);
        loadData.setNextAddress(nextAddress);
        loadData.setFinalWarehouse(destWarehouse);
        loadData.setFinalAddress(destAddress);
      }

      // Create the manifest load records if we are depositing at the final location
      // or a conveyor station because the conveyor will take it to the final location
      if(destIsShipLoc && (shiploc || conveyorDeposit))
      {
        loadData.setLoadMoveStatus(DBConstants.PICKED);
      }
      // Set the key and do the actual update ....
      // or if we are depositing in a location where the load should go away,
      // do special processing
      loadData.setKey(LoadData.LOADID_NAME, loadData.getLoadID());
      if (finalloc && loctype == DBConstants.LCDEDICATED)
      {
        moveLoadToDedicatedLocation(sLoadID, sWarehouse, sAddress, sPosition,
            loadData, moveData);
      }
      else
      {
        logOperation("Depositing Load: " + sLoadID + "  into location:"
            + sWarehouse + "-" + sAddress + getLoggedPosition(sPosition));
        // Update the load
        mpLoad.updateLoadInfo(loadData);
      }

      // Update Location Status
      updateLocationStatusForLoadMove(oldWarehouse, oldAddress, oldPosition,
          sWarehouse, sAddress, sPosition);

      // Log
      logLoadMoveTransaction(sLoadID, oldWarehouse, oldAddress, oldPosition,
          sWarehouse, sAddress, sPosition, loadData.getDeviceID());

      commitTransaction(tt);

      logDebug(getClass().getSimpleName() + ".moveLoadToStation - Load ID \""
          + sLoadID + "\" moved to Location " + sWarehouse + "-" + sAddress);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Check to see if this load can move to this dedicated location
   *
   * @param sLoadID
   * @param sWarehouse
   * @param sAddress
   * @throws DBException if there is a problem
   */
  @SuppressWarnings("rawtypes")
  protected void moveLoadToLocationCheckDedication(String sLoadID,
      String sWarehouse, String sAddress) throws DBException
  {
    String vsItem = "";

    List<Map> vpLLIDataList = mpInvServer.getLoadLineItemDataListByLoadID(sLoadID);
    LoadLineItemData vpLLIData = Factory.create(LoadLineItemData.class);
    boolean vzMultipleItems = false;
    for (Map vpMap: vpLLIDataList)
    {
      vpLLIData.dataToSKDCData(vpMap);
      if (vsItem.equals(""))
      {
        vsItem = vpLLIData.getItem();
      }
      else
      {
        if (!vsItem.equals(vpLLIData.getItem()))
        {
          vzMultipleItems = true;
          break;
        }
      }
    }

    if (vzMultipleItems)
    {
      throw new DBException("Cannot move load with multiple items into dedicated location");
    }
    if (!vsItem.equals(""))
    {
      if (!mpDedServer.isLocationDedicatedtoItem(sWarehouse, sAddress, vsItem))
      {
        throw new DBException("Cannot move load into location not dedicated to this item");
      }
    }
  }

  /**
   * Move a load to a dedicated location.  Extracted from moveLoadToLocation().
   *
   * @param sLoadID
   * @param sWarehouse
   * @param sAddress
   * @param sPosition
   * @param loadData
   * @param moveData
   * @throws DBException
   */
  @SuppressWarnings("rawtypes")
  protected void moveLoadToDedicatedLocation(String sLoadID, String sWarehouse,
      String sAddress, String sPosition, LoadData loadData, MoveData moveData)
      throws DBException
  {

    String newToLoad = null;
    String tmpOrder = null;
    // Update the item details with no order, etc and then
    // Find the load in the location and either tranfer this
    // loads inventory to it or just deposit this load in the
    // location.  If we transfer, delete the load we had
    // originally
    try
    {
      newToLoad = getLoadForLocation(sWarehouse, sAddress, true);
      LoadLineItemData lliData = Factory.create(LoadLineItemData.class);
      LoadLineItem lli = Factory.create(LoadLineItem.class);
      List<Map> llilist = mpInvServer.getLoadLineItemDataListByLoadID(sLoadID);
      if(llilist != null && llilist.size() > 0)
      {
        logOperation("Transferring inventory from Load: " + sLoadID
            + " to Load: " + newToLoad + " when depositing into location:"
            + sWarehouse + "-" + sAddress + " due to location type.");
        for(int i = 0; i < llilist.size(); i++)
        {
          lliData.dataToSKDCData(llilist.get(i));
          // Set the key we are modifying
          lliData.setKey(LoadLineItemData.ITEM_NAME, lliData.getItem());
          lliData.setKey(LoadLineItemData.LOT_NAME, lliData.getLot());
          lliData.setKey(LoadLineItemData.ORDERID_NAME, lliData.getOrderID());
          tmpOrder = lliData.getOrderID();
          // If we can't get the order out of the line item
          // see if we can get it out of the move we had.
          if(tmpOrder.length() < 1)
          {
            tmpOrder = moveData.getOrderID();
          }
          lliData.setKey(LoadLineItemData.ORDERLOT_NAME, lliData.getOrderLot());
          lliData.setKey(LoadLineItemData.LOADID_NAME, lliData.getLoadID());
          lliData.setKey(LoadLineItemData.LINEID_NAME, lliData.getLineID());

          // Now change the data to make the update
          lliData.setHoldType(DBConstants.ITMAVAIL);
          lliData.setOrderID("");
          lliData.setAllocatedQuantity(0.0);
          // Now modify it and then transfer it
          lli.modifyElement(lliData);

          // Transfer this line item if there is a new to load
          if(newToLoad != null)
          {
            mpInvServer.transferLoadLineItem(lliData, newToLoad,
                ReasonCode.getItemLoadTransferReasonCode());
          }
        }
      }
      // If it was just a deposit into the location...now
      // just update the load info.
      if(newToLoad == null)
      {
        // If there is not a load in the location, just put this one there
        // and don't worry about transferring inventory
        // Update the load

        mpLoad.updateLoadInfo(loadData);
        logOperation("Depositing Load: " + sLoadID + "  into location:"
            + sWarehouse + "-" + sAddress);
      }
      else
      {
        mpLoad.deleteElement(loadData);
        logOperation("Deleting Load: " + sLoadID
            + " when depositing into location:" + sWarehouse + "-"
            + sAddress + " due to location type.");
      }
      // Now check on the order if it was set
      if(tmpOrder == null)
      {
        // One last try to see if the order is set in the move
        tmpOrder = moveData.getOrderID();
      }
      if(tmpOrder != null)
      {
        initializePickServer();
        mpPickServer.checkOrderHeader(tmpOrder);
      }
    }
    catch (DBException dbe)
    {
      logException(dbe, "Error Creating Load for Location " + sWarehouse
          + "-" + sAddress);
      logOperation("Depositing Load: " + sLoadID + "  into location:"
          + sWarehouse + "-" + sAddress);
      //   If there is not a load in the location, just put this one there and
      //   don't worry about transferring inventory
      // Update the load
      mpLoad.updateLoadInfo(loadData);
    }
  }

  /**
   * Move a load to a AGV station and set the move status
   *
   * @param sLoadID - the load ID to move
   * @param sStationID - the station to which to move
   * @param iMoveStatus - the move status for the load, post-move
   * @param iMoveDateFlag - 0: make oldest, 1: make newest, 2: don't change
   * @throws NoSuchFieldException
   */
  @SuppressWarnings("rawtypes")
  public void moveLoadToAGVStation(String sLoadID, String sStationID,
                                   int iMoveStatus, int iMoveDateFlag) throws NoSuchFieldException
  {
    initializeLocationServer();
    initializeStationServer();

    TransactionToken tt = null;
    String vsLastWarehouse, vsLastAddress;

    try
    {
      tt = startTransaction();
      LoadData vpLoadData = getLoad(sLoadID);
      if (vpLoadData != null)
      {
        vsLastWarehouse = vpLoadData.getWarehouse();
        vsLastAddress   = vpLoadData.getAddress();

        vpLoadData.setParentLoadID(vpLoadData.getLoadID());
        vpLoadData.setLoadID(vpLoadData.getLoadID());
        vpLoadData.setWarehouse(mpStationServer.getStationWarehouse(sStationID));
        vpLoadData.setAddress(sStationID);
        vpLoadData.setRouteID("");
        vpLoadData.setNextWarehouse("");
        vpLoadData.setNextAddress("");
        vpLoadData.setFinalWarehouse("");
        vpLoadData.setFinalAddress("");
        vpLoadData.setLoadMoveStatus(iMoveStatus);
        vpLoadData.setDeviceID(mpLocServer.getLocationDeviceId(vpLoadData.getWarehouse(), vpLoadData.getAddress()));
        switch (iMoveDateFlag)
        {
          case 0:  // Oldest move date
            LoadData oldestLoad = getOldestLoadData(sStationID, 0);
            if ((oldestLoad != null) &&
                (!oldestLoad.getLoadID().equals(vpLoadData.getLoadID())))
            {
              Date vpOldestDate = oldestLoad.getMoveDate();
              vpOldestDate.setTime(vpOldestDate.getTime() - 1000);
              vpLoadData.setMoveDate(vpOldestDate);
            }
            break;

          case 1:  // Current move date
            vpLoadData.setMoveDate();
            break;

          default:  // No change
            break;
        }
        mpLoad.updateLoadInfo(vpLoadData);
        /*
         * Log the move
         */
        logLoadMoveTransaction(sLoadID, vsLastWarehouse, vsLastAddress,
            LoadData.DEFAULT_POSITION_VALUE, vpLoadData.getWarehouse(),
            vpLoadData.getAddress(), LoadData.DEFAULT_POSITION_VALUE,
            vpLoadData.getDeviceID());

        commitTransaction(tt);

        logDebug(getClass().getSimpleName() + ".moveLoadToAGVStation - Load ID \""
            + sLoadID + "\" moved to Station " + sStationID);
      }
    }
    catch (DBException e)
    {
      logException(e, getClass().getSimpleName()
          + ".moveLoadToAGVStation - LoadID \"" + sLoadID
          + "\" (Parent) Exception Changing Parent Load Status");
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Update location empty flag after a load movement
   *
   * @param isFromWarehouse
   * @param isFromAddress
   * @param isFromPosition
   * @param isToWarehouse
   * @param isToAddress
   * @param isToPosition
   * @throws DBException
   */
  protected void updateLocationStatusForLoadMove(String isFromWarehouse,
      String isFromAddress, String isFromPosition, String isToWarehouse,
      String isToAddress, String isToPosition) throws DBException
  {
    // Set the new location occupied
    mpLocServer.setLocationEmptyFlag(isToWarehouse, isToAddress, isToPosition,
        DBConstants.OCCUPIED);

    // Check the old location to see if we need to set its
    // occupied status flag
    LoadData lddatas = Factory.create(LoadData.class);
    lddatas.setKey(LoadData.WAREHOUSE_NAME, isFromWarehouse);
    lddatas.setKey(LoadData.ADDRESS_NAME, isFromAddress);
    if (mpLoad.getCount(lddatas) < 1)
    {
      mpLocServer.setLocationEmptyFlag(isFromWarehouse, isFromAddress,
          isFromPosition, DBConstants.UNOCCUPIED);
    }
  }

  /**
   * Generates and returns a random pick-to load and the specified location
   * with the specified container type.
   *
   * @param sWarehouse     - Warehouse for the load
   * @param sAddress       - Address for the load
   * @param sContainerType - ContainerType for the load
   * @return LoadData for the load that was created
   * @throws DBException
   */
  public LoadData generatePickToLoad(String isWarehouse, String isAddress,
      String isContainerType, String isRouteID)
    throws DBException
  {
    initializeLocationServer();

    LoadData vpLoadData = Factory.create(LoadData.class);
    String vsLoadID;

    vpLoadData.clear();
    vpLoadData.setWarehouse(isWarehouse);
    vpLoadData.setAddress(isAddress);
    vpLoadData.setContainerType(isContainerType);
    vpLoadData.setRouteID(isRouteID);
    vpLoadData.setMoveDate();

    /*
     * If Device ID wasn't in the load, we wouldn't need this.
     */
    vpLoadData.setDeviceID(mpLocServer.getLocationDeviceId(isWarehouse, isAddress));

    do
    {
      vsLoadID = createWordedLoadID();

      vpLoadData.setParentLoadID(vsLoadID);
      vpLoadData.setLoadID(vsLoadID);

    } while (mpLoad.createLoad(vpLoadData) == false);

    /*
     * Add a transaction history record
     */
    logTransaction_LoadAdd(vpLoadData);

    return vpLoadData;
  }

  /**
   * Generates and returns a random pick-to load and the specified location
   * with the specified container type within a transaction.
   *
   * @param sWarehouse     - Warehouse for the load
   * @param sAddress       - Address for the load
   * @param sContainerType - ContainerType for the load
   * @return LoadData for the load that was created
   * @throws DBException
   */
  public LoadData generatePickToLoadWithTransaction(String sWarehouse,
      String sAddress, String sContainerType) throws DBException
  {
    LoadData loadData = Factory.create(LoadData.class);
    String sLoadID;

    loadData.clear();
    loadData.setWarehouse(sWarehouse);
    loadData.setAddress(sAddress);
    loadData.setContainerType(sContainerType);
    loadData.setMoveDate();

    int tries = 0;
    boolean loadAdded = false;
    TransactionToken tt = null;
    while(!loadAdded && tries < 100)
    {
      tt = startTransaction();
      sLoadID = createWordedLoadID();
      loadData.setParentLoadID(sLoadID);
      loadData.setLoadID(sLoadID);
      loadAdded = mpLoad.createLoad(loadData);
      if(loadAdded)
      {
        /*
         * Add a transaction history record
         */
        logTransaction_LoadAdd(loadData);

        commitTransaction(tt);
        return loadData;
      }
      endTransaction(tt);
      tries++;
    }

    return null;
  }

  /**
   * Generates and returns a random pick-to load and the specified location
   * with the specified container type within a transaction.
   *
   * @param sWarehouse     - Warehouse for the load
   * @param sAddress       - Address for the load
   * @param createIfNeeded    - CreateThe Load If non existant
   * @return String Loadid  - when finding a load for the location if not found
   *                          it will throw an exception
   * @throws DBException
   */
  public String getLoadForLocation(String sWarehouse, String sAddress,
      boolean createIfNeeded) throws DBException
  {
    initializeLocationServer();
    initializeStationServer();

    String sLoadID = null;
    LoadData loadData;
    loadData = getOldestLoad(sWarehouse, sAddress);
    int maxtries = 40;

    if(loadData == null)
    {
      String tmpString;
      int tries = 0;
      boolean foundUnusedLoad = false;
      TransactionToken tt = null;

      for(tries = 0; tries < maxtries; tries++)
      {
        if(sAddress.trim().length() > 7)
        {
          if(tries == 0)
          {
            tmpString = sWarehouse.substring(0,1) + sAddress.substring(1,6) + sAddress.substring(7);
          }
          else
          {
            if(tries < 10)
            {
              tmpString = sWarehouse.substring(0,1) + tries + sAddress.substring(2,6) + sAddress.substring(7);
            }
            else
            {
              tmpString = sWarehouse.substring(0,1) + tries + sAddress.substring(3,6) + sAddress.substring(7);
            }
          }
        }
        else
        {
          if(sAddress.length() < 5)
          {
            if(tries == 0)
            {
              tmpString = sWarehouse + sAddress;
            }
            else if(tries < 10)
            {
              tmpString = sWarehouse.substring(0,2) + tries + sAddress;
            }
            else
            {
              tmpString = sWarehouse.substring(0,1) + tries + sAddress;
            }
          }
          else
          {
            if(tries < 10)
            {
              tmpString = sWarehouse.substring(0,1) + sAddress;
            }
            else
            {
              tmpString = sWarehouse.substring(0,1) + tries + sAddress.substring(1);
            }
          }
        }
        sLoadID = tmpString.trim();

        if (mpLoad.exists(sLoadID))
        {
          continue;
        }
        foundUnusedLoad = true;
                 // If we were just supposed to find a load then just get out and
                 // return the loadID we found that will work and don't create it
        if(createIfNeeded == false)
        {
          break;    // get out of the loop
        }
            // We were told to create the load if needed so Now add the load
        loadData = Factory.create(LoadData.class);
        loadData.clear();
        loadData.setLoadID(sLoadID);
        loadData.setParentLoadID(sLoadID);
        loadData.setWarehouse(sWarehouse);
        loadData.setAddress(sAddress);
        loadData.setMoveDate();
                // Get the container type from the station
        StationData stationData = Factory.create(StationData.class);
        stationData = mpStationServer.getControllingStationFromLocation(sWarehouse, sAddress);
        if(stationData == null)
        {
          loadData.setContainerType("PALLET");
        }
        else
        {
          loadData.setContainerType(stationData.getContainerType());
        }
              // Get the locations device id
        LocationData lcdata = mpLocServer.getLocationRecord(sWarehouse, sAddress);
        loadData.setDeviceID(lcdata.getDeviceID());

                // Do the actual creation
        tt = startTransaction();
        boolean loadAdded = mpLoad.createLoad(loadData);
        if(loadAdded)
        {
          logTransaction_LoadAdd(loadData);
          commitTransaction(tt);
          break;
        }
        endTransaction(tt);
        throw new DBException("Error creating Load: " + sLoadID + " for Location: " +
                                            sWarehouse + "-" + sAddress);
      }
             // If we didn't find an unused load id just return a null
      if(foundUnusedLoad == false)
      {
        sLoadID =  null;
      }
    }
    else
    {
      sLoadID = loadData.getLoadID();
    }
    if(sLoadID == null)
    {
      throw new DBException("Error finding a load for Location: " +
                                                       sWarehouse + "-" + sAddress);
    }
    return sLoadID;
  }

  /**
   * Get a list of picked loads for an order
   *
   * @return String of load IDs
   * @throws DBException
   */
  public String[] getPickedLoadsOnOrder(String orderId) throws DBException
  {
    return mpLoad.getPickedLoadsOnOrder(orderId);
  }

  /**
   * Get a list of loads for unpicking
   *
   * @return String of load IDs
   * @throws DBException
   */
  public String[] getUnpickLoadList() throws DBException
  {
    return mpLoad.getUnpickLoadList();
  }

  /**
   * Method to get list of children load IDs for a specified parent Load ID.
   *
   * @param srch Load IDs.
   * @return List of treemaps(load objects) of child loads for this specified loadid (srch).
   * @throws DBException no information available
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getChildrenList(String srch) throws DBException
  {
    return mpLoad.getChildLoads (srch);
  }

  /**
   * Method to get list of children loads for a specified Load IDs
   * ordered by last moved date in descending fashion.
   *
   * @param srch Load IDs.
   * @return List of treemaps (load objects) containing child Loads of the specified load (srch).
   * @throws DBException no information available
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getChildrenListOrderedByDate(String srch) throws DBException
  {
    return mpLoad.getChildLoadsOrderedByDate (srch);
  }

  /**
   * Method to get list of children load IDs for a specified parent Load ID.
   *
   * @param srch Load IDs.
   * @param isItemID - The String to place in the ItemID field
   * @return List of strings containing Load IDs.
   * @throws DBException no information available
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getChildrenListAsLLI(String srch, String isItemID) throws DBException
  {
    return mpLoad.getChildLoadsAsLLI(srch, isItemID);
  }

  /**
   * Method to get count of children load IDs for a specified parent Load ID.
   *
   * @param srch Load IDs.
   * @return int of strings containing Load IDs.
   * @throws DBException no information available
   */
  public int getChildrenCount(String srch)
  {
    int count = 0;

    try
    {
      count = mpLoad.getChildCount(srch);
    }
    catch (DBException e1)
    {
      logException(e1, "Counting Load Line Items.");
    }
    return count;
  }

 /**
  * Gets the load's container type.
  * @param isLoadID the load id.
  * @return empty string if the load does not exist. <b>Note:</b> The container
  *         type is a required field for the load record so a load is guaranteed
  *         to have a non-null container type.
  * @throws DBException
  */
  public String getLoadContainerType(String isLoadID) throws DBException
  {
    String vsContainerType = (String)mpLoad.getSingleColumnValue(isLoadID,
                                                                 LoadData.CONTAINERTYPE_NAME);
    return((vsContainerType == null) ? "" : vsContainerType);
  }

  public void updateLoadContainerType(String isLoadID, String isContainerType)
         throws DBException
  {
    LoadData vpLoadData = Factory.create(LoadData.class);
    vpLoadData.setContainerType(isContainerType);
    vpLoadData.setKey(LoadData.PARENTLOAD_NAME, isLoadID);
    vpLoadData.setKey(LoadData.LOADID_NAME, isLoadID);

    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      mpLoad.modifyElement(vpLoadData);
      commitTransaction(vpTok);
    }
    catch(NoSuchElementException nse)
    {
      throw new DBException("Container type modification failed!", nse);
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

  /**
   * Method to stage a Load.
   *
   * @param loadID load to be staged.
   * @param location new location of load - warehouse + address
   * @param allowMultCust true if multiple customers is allowed
   */
  @SuppressWarnings("rawtypes")
  public void stageLoad(String loadID, String location, boolean allowMultCust)
      throws DBException
  {
    initializeInventoryServer();
    initializeOrderServer();

    Set<String> orderSet = new HashSet<String>();
    if(anyItemMovesForLoad(loadID))
    {
      throw new DBException("Load ''" + loadID + " has a item moves and cannot be staged");
    }
    LoadData parentLoadData = getParentLoad(loadID);
    if(!parentLoadData.getLoadID().equals(loadID))
    {
      throw new DBException("Load '" + loadID + "' cannot be staged...Must Stage Parent Load '" +
                                  parentLoadData.getLoadID() + "'");
    }
    try
    {
      List<Map> lineItemList = mpInvServer.getLoadLineItemDataListByLoadID(loadID);
      // Create a set of unique order ID's contained in the load line item list
      for(Map lineHash: lineItemList)
      {
        LoadLineItemData lineItemData = Factory.create(LoadLineItemData.class);
        lineItemData.dataToSKDCData(lineHash);
        String vsOrderID = lineItemData.getOrderID();
        if (vsOrderID.length() == 0)
        {
          throw new DBException("Load " + loadID + " has a line item without an order and cannot be staged");
        }
        else
        {
          orderSet.add(vsOrderID);
        }
      }
    }
    catch(DBException e)
    {
      e.printStackTrace();
      throw new DBException("Unable to stage load");
    }

    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      deleteLoadMoveForLoad(loadID);
      mpLoad.stageLoad(loadID, location);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }

    // This used to work, but does not now.  If we get LDS Distribution, then
    // it'll get fixed.  If not, it can be deleted or moved to UnusedSource.
//    for(String orderID: orderSet)
//    {
//      if(!mpOrderServer.hasAssignedMoves(orderID))
//        mpOrderServer.updateOrderStatus(orderID);
//    }
  }

  /**
   * Method to ship a Load.
   *
   * @param loadID load to be shipped.
   * @param location new location of load
   * @param allowMultCust true if multiple customers is allowed
   */
  public void shipLoad(String loadID, String location, boolean allowMultCust)
      throws DBException
  {
    if(anyItemMovesForLoad(loadID))
    {
      throw new DBException("Load " + loadID + " has a item moves and cannot be shipped");
    }
//    if (!isLoadMoveStatus(loadID, DBConstants.STAGED))
//    {
//      stageLoad(loadID, location, allowMultCust);
//    }

    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      deleteLoadMoveForLoad(loadID);
      mpLoad.shipLoad(loadID, location);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Are there any item-moves for this load?
   *
   * @param isLoadID
   * @return
   */
  @SuppressWarnings("rawtypes")
  public boolean anyItemMovesForLoad(String isLoadID)
  {
    initializeMoveServer();

    List<Map> mvList = new ArrayList<Map>();
    MoveData moveData = Factory.create(MoveData.class);

    moveData.setKey(MoveData.LOADID_NAME, isLoadID);
    moveData.setKey(MoveData.MOVETYPE_NAME, Integer.valueOf(DBConstants.ITEMMOVE));

    try
    {
      mvList = mpMoveServer.getMoveDataList(moveData.getKeyArray());

    }
    catch (DBException e)
    {
      logException(e, "Getting moves for load \"" + isLoadID + "\"");
    }
    if(mvList != null)
    {
      int mvcount = mvList.size();
      if (mvcount > 0)
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Delete all load-moves for a load
   *
   * @param isLoadID
   * @throws DBException
   */
  @SuppressWarnings("rawtypes")
  protected void deleteLoadMoveForLoad(String isLoadID) throws DBException
  {
    initializeMoveServer();

    List<Map> mvList = new ArrayList<Map>();
    MoveData moveData = Factory.create(MoveData.class);

    moveData.setKey(MoveData.LOADID_NAME, isLoadID);
    moveData.setKey(MoveData.MOVETYPE_NAME, Integer.valueOf(DBConstants.LOADMOVE));

    try
    {
      mvList = mpMoveServer.getMoveDataList(moveData.getKeyArray());
    }
    catch (DBException e)
    {
      logException(e, "Getting moves for load \"" + isLoadID + "\"");
    }
    if(mvList != null)
    {
      int mvcount = mvList.size();
      if (mvcount > 0)
      {
          mpMoveServer.deleteMove(moveData);
      }
    }
  }

  /**
   * Check to see if a subload can be placed into a superload to prevent
   * circular links
   * @param isParentLoad - <code>String</Code> - The superload
   * @param isChildLoad - <code>String</Code> - The subload
   */
  public boolean canLoadContainLoad(String isParentLoad, String isChildLoad)
  {
    String vsCurrentChild = isParentLoad;
    String vsCurrentParent = "";
    boolean done = false;
    while(!done)
    {
      vsCurrentParent = getParentLoad(vsCurrentChild).getLoadID();
      if (vsCurrentParent.equals(vsCurrentChild) || vsCurrentParent.equals(""))
      {
        return true;
      }
      if (vsCurrentParent.equals(isChildLoad))
      {
        return false;
      }
      vsCurrentChild = vsCurrentParent;
    }
    return true;
  }

  /**
   * Set the parent load of a given load
   * circular links
   * @param isParentLoad - <code>String</Code> - The superload
   * @param isChildLoad - <code>String</Code> - The subload
   */
  public void setParentLoad(String isChildLoad, String isParentLoad) throws DBException
  {
    TransactionToken tt = null;
    LoadData vpLoadData = Factory.create(LoadData.class);
    LoadData vpParentLoadData = Factory.create(LoadData.class);
    vpLoadData = getLoad(isChildLoad);
    vpParentLoadData = getLoad(isParentLoad);
    if (!canLoadContainLoad(isParentLoad, isChildLoad))
    {
      throw new DBException(isParentLoad + " is contained within "
          + isChildLoad + " and cannot become superload");
    }
    try
    {
      tt = startTransaction();
      vpLoadData.setParentLoadID(isParentLoad);
      vpLoadData.setWarehouse(vpParentLoadData.getWarehouse());
      vpLoadData.setAddress(vpParentLoadData.getAddress());
      updateLoadData(vpLoadData, true);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Method to get the total fullness present in all loads in the rack for a
   * given device.
   *
   * @param isDevice the device
   * @return double value representing total amount full.
   * @throws DBException if there is a database access error.
   */
  public double getTotalLoadFullnessByDevice(String isDevice) throws DBException
  {
    return mpLoad.getTotalLoadFullnessByDevice(isDevice);
  }

  /**
   * Method gets the total load count of the loads in the rack for the
   * given device
   * @param isDevice
   * @return int the amount of loads
   * @throws DBException
   */
  public int getLoadCountByDevice(String isDevice) throws DBException
  {
    return mpLoad.getLoadCountByDevice(isDevice);
  }

  /*========================================================================*/
  /*  TRANSACTION HISTORY                                                   */
  /*========================================================================*/

  /**
   * Log a Load Add to transaction history
   * @param ipLoadData - <code>LoadData</Code> - The added load
   */
  public void logTransaction_LoadAdd(LoadData ipLoadData)
  {
    tnData.clear();
    tnData.setTranType(DBConstants.ADD_LOAD);
    tnData.setTranCategory(DBConstants.LOAD_TRAN);
    tnData.setLoadID(ipLoadData.getLoadID());
    tnData.setLocation(ipLoadData.getWarehouse(), ipLoadData.getAddress()
        + getLoggedPosition(ipLoadData.getShelfPosition()));
    tnData.setRouteID(ipLoadData.getRouteID());
    logTransaction(tnData);
  }

   /**
   * Log a Load Modify to transaction history
   * @param ipNewLoadData - <code>LoadData</Code> - The new load data
   * @param ipOldLoadData - <code>LoadData</Code> - The old load data
   */
  public void logTransaction_LoadModify(LoadData ipNewLoadData, LoadData ipOldLoadData)
  {
    tnData.clear();
    tnData.setTranType(DBConstants.MODIFY_LOAD);
    tnData.setTranCategory(DBConstants.LOAD_TRAN);
    tnData.setLoadID(ipNewLoadData.getLoadID());
    tnData.setLocation(ipOldLoadData.getWarehouse(), ipOldLoadData.getAddress());
    tnData.setToLocation(ipNewLoadData.getNextWarehouse(), ipNewLoadData.getNextAddress());
    tnData.setRouteID(ipNewLoadData.getRouteID());
    logTransaction(tnData);
  }

  /**
   * Log a load move transaction
   *
   * @param isLoadID
   * @param isFromWarehouse
   * @param isFromAddress
   * @param isFromPosition
   * @param isToWarehouse
   * @param isToAddress
   * @param isToPosition
   * @param isDeviceID
   */
  public void logLoadMoveTransaction(String isLoadID, String isFromWarehouse,
      String isFromAddress, String isFromPosition,
      String isToWarehouse, String isToAddress,
      String isToPosition, String isDeviceID)
  {
    if (isFromWarehouse.equals(isToWarehouse)
        && isFromAddress.equals(isToAddress)
        && isFromPosition.equals(isToPosition))
    {
      return;
    }

    tnData.clear();
    tnData.setTranCategory(DBConstants.LOAD_TRAN);
    tnData.setTranType(DBConstants.TRANSFER);
    tnData.setLoadID(isLoadID);
    tnData.setLocation(isFromWarehouse, isFromAddress + getLoggedPosition(isFromPosition));
    tnData.setToLoadID(isLoadID);
    tnData.setToLocation(isToWarehouse, isToAddress + getLoggedPosition(isToPosition));
    tnData.setDeviceID(isDeviceID);
    logTransaction(tnData);
  }

  /**
   * Get the logged shelf position (don't log the default 000)
   *
   * @param isPosition
   * @return
   */
  protected String getLoggedPosition(String isPosition)
  {
    if (LoadData.DEFAULT_POSITION_VALUE.equals(isPosition))
    {
      return "";
    }
    else
    {
      return isPosition;
    }
  }

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
