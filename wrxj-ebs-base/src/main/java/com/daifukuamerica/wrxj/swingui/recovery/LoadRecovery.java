/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright (c) 2004-2009 Daifuku America Corporation  All Rights Reserved.
 
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS 
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND 
  CIVIL LIABILITY.
 ****************************************************************************/

package com.daifukuamerica.wrxj.swingui.recovery;

import com.daifukuamerica.wrxj.allocator.AllocationMessageDataFormat;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardMoveServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPickServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardRecoveryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardSchedulerServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.device.agc.AGCDeviceConstants;
import com.daifukuamerica.wrxj.device.gateway.SystemGateway;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.messageformat.loadevent.LoadEventDataFormat;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * @author Stephen Kendorski
 *
 */
public class LoadRecovery
{
  private static final String MOVEMENT_CANCELLED = 
    "This movement has been cancelled on Warehouse Rx. \n" +
    "Has it been deleted from the AGC";
  
  /**
   * The Logging implementation for this named subsystem to use.
   */
  protected Logger mpLogger = Logger.getLogger();

  protected SKDCInternalFrame mpParentFrame = null;

  protected StandardInventoryServer mpInvServer = null;
  protected StandardLoadServer mpLoadServer = null;
  protected StandardMoveServer mpMoveServer = null;
  protected StandardPickServer mpPickServer = null;
  protected StandardRecoveryServer mpRecoveryServer = null;
  protected StandardSchedulerServer mpSchedServer = null;
  protected StandardStationServer mpStnServer = null;
  protected Location mpLoc = Factory.create(Location.class);

  protected LoadEventDataFormat mpLEDF = null;
  
  protected String msRecoveryNote;  
  
  /**
   * Constructor
   */
  public LoadRecovery()
  {
    msRecoveryNote = "Recovered by " + SKDCUserData.getLoginName();
  }
  
  /**
   * Get the SystemGateway
   * 
   * @return <code>SystemGateway</code>
   */
  protected SystemGateway getSystemGateway()
  {
    return ThreadSystemGateway.get();
  }
  
  /**
   * Set the parent frame (needed for pop-up prompts)
   * 
   * @param ipFrame
   */
  public void setParentFrame(SKDCInternalFrame ipFrame)
  {
    mpParentFrame = ipFrame;
  }

  /**
   * Initialize
   */
  public void initialize()
  {
    mpLEDF = Factory.create(LoadEventDataFormat.class, "Recovery");
    //
    mpInvServer = Factory.create(StandardInventoryServer.class);
    mpLoadServer = Factory.create(StandardLoadServer.class);
    mpMoveServer = Factory.create(StandardMoveServer.class);
    mpPickServer = Factory.create(StandardPickServer.class);
    mpRecoveryServer = Factory.create(StandardRecoveryServer.class);
    mpSchedServer = Factory.create(StandardSchedulerServer.class);
    mpStnServer = Factory.create(StandardStationServer.class);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Get the list to display in the Recovery screen
   * 
   * @param iapColData
   * @return
   */
  @SuppressWarnings("rawtypes")
  public List getLoadList(KeyObject[] iapColData)
  {
    List vpList = null;
    try
    {
      vpList = mpRecoveryServer.getRecoveryLoadDataList(iapColData);
    }
    catch (DBException e)
    {
      mpLogger.logException("Error reading recovery list", e);
      displayError("Database Error: " + e);
    }
    return vpList;
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Delete a load
   *  
   * @param isLoadId
   */
  public void deleteLoad(String isLoadId)
  {
    try
    {
      mpInvServer.deleteLoad(isLoadId, "");
    }
    catch (DBException e)
    {
      displayError("Failed to delete load \"" + isLoadId + "\" - "
          + e.getMessage());
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public void recoverArrival(String isLoadId, String isBarCode,
      String isStation, int iiLoadHeight)
  {
    // Find out who is scheduling this station
    String scheduler;
    try
    {
      scheduler = mpStnServer.getStationsScheduler(isStation);
    }
    catch(DBException ex)
    {
      displayError("Error getting Scheduler attached to this station.");
      return;
    }
    
    // Send the scheduler event message
    String cmdstr = mpLEDF.processArrivalReport(
        AGCDeviceConstants.AGCDUMMYLOAD, isStation, iiLoadHeight, 1, isBarCode,
        "");
    getSystemGateway().publishLoadEvent(cmdstr, 0, scheduler);
    mpLogger.logOperation(LogConsts.OPR_USER, "Load \"" + isLoadId
        + "\" \"Dummy Arrival\" Created");
    displayInfoAutoTimeOut("Load \"" + isLoadId
        + "\" \"Dummy Arrival\" Created");
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public void recoverLoad(String isLoadId)
  {
    String loadMoveStatusText = null;
    try
    {
      LoadData vpLoadData = mpLoadServer.getLoad1(isLoadId);
      if (vpLoadData == null)
      {
        displayError("Load \"" + isLoadId + "\" no longer exists!");
        return;
      }
      int loadMoveStatus = vpLoadData.getLoadMoveStatus();
      try
      {
        loadMoveStatusText = DBTrans.getStringValue(
            LoadData.LOADMOVESTATUS_NAME, loadMoveStatus);
      }
      catch (Exception eTrans)
      {
        mpLogger.logError("DBTrans CANNOT find " + LoadData.LOADMOVESTATUS_NAME);
      }
      if (loadMoveStatusText == null)
      {
        loadMoveStatusText = "" + loadMoveStatus;
      }
      mpLogger.logDebug("Load \"" + isLoadId + "\" Status: "
          + loadMoveStatusText + " - Recovering");

      // Decide what to do
      switch(loadMoveStatus)
      {
        case DBConstants.ARRIVEPENDING:
          recoverArrivalPendingLoad(vpLoadData);
          break;
          
        case DBConstants.ARRIVED:
          recoverArrivedLoad(vpLoadData);
          break;
          
        case DBConstants.IDPENDING:
          recoverIDPendingLoad(isLoadId);
          break;

        case DBConstants.MOVEERROR:
        case DBConstants.MOVING:
        case DBConstants.MOVESENT:
          recoverMovingLoad(vpLoadData);
          break;
          
        case DBConstants.RETRIEVEERROR:
        case DBConstants.RETRIEVING:
        case DBConstants.RETRIEVESENT:
          recoverRetrievingLoad(vpLoadData);
          break;
          
        case DBConstants.STOREERROR:
        case DBConstants.STORING:
        case DBConstants.STORESENT:
          recoverStoringLoad(vpLoadData);
          break;
        
        case DBConstants.STOREPENDING:
          if (recoverStorePendingLoad(vpLoadData)) break;
          // Intentional conditional fall-through
        case DBConstants.MOVEPENDING:
          displayInfoAutoTimeOut("Load " + isLoadId + " is \""
              + loadMoveStatusText
              + "\"\nbecause the scheduler hasn't scheduled it yet."
              + "\n\nIf the move status does not change automatically,"
              + "\nplease check the Error Log in the System Monitor.");
          break;
          
        case DBConstants.RETRIEVEPENDING:
          if (recoverRetrievePendingLoad(vpLoadData)) break;
          displayInfoAutoTimeOut("Load " + isLoadId + " is \""
              + loadMoveStatusText
              + "\"\nbecause the scheduler hasn't scheduled it yet."
              + "\n\nIf the move status does not change automatically,"
              + "\nplease check the Error Log in the System Monitor.");
          break;

        case DBConstants.BINFULL_ERROR:
        case DBConstants.SIZE_ERROR:
          recoverErrorLoad(vpLoadData);
          break;
          
        default:
          displayInfoAutoTimeOut("\"" + loadMoveStatusText 
              + "\" Recovery not implemented yet");
          break;
      }
    }
    catch (DBException e)
    {
      displayError("Error recovering load \"" + isLoadId + "\" - "
          + e.getMessage());
      mpLogger.logException(e, "Load \"" + isLoadId + "\" Status: "
          + loadMoveStatusText);
    }
  }

  /**
   * Recover an Arrival Pending load
   * @param isLoadId
   * @param ipLoadData
   * @throws DBException
   */
  protected void recoverArrivalPendingLoad(LoadData ipLoadData) 
    throws DBException
  {
    String vsLoadID = ipLoadData.getLoadID();
    String vsDialogText = null;
    StationData vsStationData = mpStnServer.getStation(ipLoadData.getAddress());
    if ((vsStationData.getStationType() != DBConstants.PDSTAND) &&
        (vsStationData.getStationType() != DBConstants.REVERSIBLE) &&
        (vsStationData.getStationType() != DBConstants.AGC_TRANSFER))
    {
      vsDialogText = "Have you pushed the \"Work Complete\" button";
      if (!displayYesNoPrompt(vsDialogText))
      {
        return;
      }
    }
    
    vsDialogText = "Do you want to re-send the Arrival for load \"" + vsLoadID 
            + "\"";
    if (displayYesNoPrompt(vsDialogText))
    {
      // find out who is scheduling this station
      String scheduler = mpStnServer.getStationsScheduler(vsStationData.getStationName());
      // send the scheduler event message
      try
      {
        String cmdstr = mpLEDF.processArrivalReport(AGCDeviceConstants.AGCDUMMYLOAD, 
            vsStationData.getStationName(), displayHeightPrompt(), 1, vsLoadID, "");
        getSystemGateway().publishLoadEvent(cmdstr, 0, scheduler);
        mpLogger.logOperation(LogConsts.OPR_USER, "Load \"" + vsLoadID
            + "\" at station " + vsStationData.getStationName()
            + ": re-sending Arrival");
        displayInfoAutoTimeOut("Arrival sent for load \"" + vsLoadID 
            + "\" at station " + vsStationData.getStationName());
      }
      catch (NullPointerException npe)
      {
        // This happens if someone clicks the cancel button on the height pop-up
        displayInfo("Arrival NOT sent for load \"" + vsLoadID + "\"");
      }
    }
  }
  
  /**
   * Recover Arrived loads
   * @param loadData
   */
  protected void recoverArrivedLoad(LoadData loadData)
  {
    StationData vsStationData = mpStnServer.getStation(loadData.getAddress());
    if (vsStationData == null)
    {
      mpLoadServer.setParentLoadMoveStatus(loadData.getLoadID(),
          DBConstants.NOMOVE, "");
    }
    else if (vsStationData.getStationType() == DBConstants.OUTPUT  &&
             vsStationData.getDeleteInventory() == DBConstants.YES &&
             (vsStationData.getAutoLoadMovementType() == DBConstants.AUTOPICK ||
              vsStationData.getAutoLoadMovementType() == DBConstants.BOTH))
    {
      String vsDialogText = "Auto-Pick load \"" + loadData.getLoadID() + "\"";
      if (displayYesNoPrompt(vsDialogText))
      {
        mpPickServer.autoPickLoadFromStation(loadData.getLoadID());
        return;
      }
    }
    displayInfoAutoTimeOut("Load \"" + loadData.getLoadID()
        + "\" does not need recovery.");
  }
  
  /**
   * Recover (delete) an ID Pending load
   * @param isLoadID
   * @throws DBException
   */
  protected void recoverIDPendingLoad(String isLoadID) throws DBException
  {
    String vsDialogText = "Delete \"ID Pending\" load \"" + isLoadID
        + "\" from the Warehouse Rx database";
    if (displayYesNoPrompt(vsDialogText))
    {
      mpInvServer.deleteLoad(isLoadID, "");
      String vsResult = "Load \"" + isLoadID
          + "\" deleted from the Warehouse Rx database.";
      mpLogger.logOperation(LogConsts.OPR_USER, vsResult);
      displayInfoAutoTimeOut(vsResult);
    }
  }

  /**
   * Recover a Moving load
   * @param ipLoadData
   * @throws DBException
   */
  protected void recoverMovingLoad(LoadData ipLoadData) throws DBException
  {
    /*
     * A moving load might be a location-location load
     */
    if (mpStnServer.getStation(ipLoadData.getNextAddress()) == null)
    {
      recoverLocationToLocationLoad(ipLoadData);
      return;
    }
    
    /*
     * If it isn't at a station, or if the current station==next station,
     * then this is a retrieving moving load
     */
    StationData vpStationData = mpStnServer.getStation(ipLoadData.getAddress());
    if (vpStationData == null || 
        ipLoadData.getAddress().equals(ipLoadData.getNextAddress()))
    {
      recoverRetrieveMovingLoad(ipLoadData);
    }
    else
    {
      recoverTransferMovingLoad(ipLoadData);
    }
  }

  /**
   * Recovers a Moving load that is retrieving from a rack to a station
   * 
   * @param ipLoadData
   * @throws DBException
   */
  protected void recoverRetrieveMovingLoad(LoadData ipLoadData) throws DBException
  {
    String vsLoadId = ipLoadData.getLoadID();
    StationData vpStationData = null;
    String vsDialogText = "Has load \"" + ipLoadData.getLoadID()
        + "\" arrived at " 
        + mpLoc.describeLocation(
            ipLoadData.getNextWarehouse(), ipLoadData.getNextAddress());
    if (displayYesNoPrompt(vsDialogText))
    {
      //    find out who is scheduling this station
      vpStationData = mpStnServer.getStation(ipLoadData.getAddress());
      if (vpStationData == null)
      {
        vpStationData = mpStnServer.getStation(ipLoadData.getNextAddress());
      }
      String scheduler = mpStnServer.getStationsScheduler(vpStationData.getStationName());

      //  send required completion message
      if (vpStationData.getArrivalRequired() == DBConstants.YES)
      {
        // If an arrival is required, send the arrival
        String cmdstr2 = mpLEDF.processArrivalReport(ipLoadData.getLoadID(),
            vpStationData.getStationName(), vpStationData.getHeight(), 1,
            ipLoadData.getBCRData(), "");
        getSystemGateway().publishLoadEvent(cmdstr2, 0, scheduler);
      }
      else
      {
        // If an arrival is not required, send the work complete
        String cmdstr2 = mpLEDF.processOperationCompletion(
            ipLoadData.getLoadID(),
            0, // Normal
            2, // Retrieval
            "",
            vpStationData.getStationName(),
            ipLoadData.getAddress(),
            ipLoadData.getShelfPosition(),
            "",
            "",
            ipLoadData.getHeight(),
            ipLoadData.getBCRData(),
            "",
            "");
        getSystemGateway().publishLoadEvent(cmdstr2, 0, scheduler);
      }

      mpLogger.logOperation(LogConsts.OPR_USER, "Load \"" + vsLoadId 
          + "\" Station: " + vpStationData.getStationName() + " Re-Sent Arrival");
      displayInfoAutoTimeOut("Messages sent for load \"" + vsLoadId + "\".");
      return;
    }
    vsDialogText = "Is load \"" + ipLoadData.getLoadID() + "\" still in "
        + mpLoc.describeLocation(ipLoadData.getWarehouse(), ipLoadData.getAddress());
    if (displayYesNoPrompt(vsDialogText))
    {
      vsDialogText = "Do you want to re-schedule the retrieval of load \""
          + vsLoadId + "\"";
      if (displayYesNoPrompt(vsDialogText))
      {
        vpStationData = mpStnServer.getStation(ipLoadData.getNextAddress());

        //  find out who is scheduling this station
        if (vpStationData == null)
        {
          vpStationData = mpStnServer.getControllingStationFromLocation(ipLoadData.getWarehouse(), 
              ipLoadData.getAddress());
        }
        String scheduler = mpStnServer.getStationsScheduler(vpStationData.getStationName());

        //  reset the load move status
        mpLoadServer.setParentLoadMoveStatus(ipLoadData.getLoadID(),
            DBConstants.RETRIEVEPENDING, msRecoveryNote);

        //  send the scheduler event message
        String cmdstr = mpLEDF.processArrivalReport(AGCDeviceConstants.AGCDUMMYLOAD, 
            vpStationData.getStationName(), vpStationData.getHeight(), 1, vsLoadId, "");
        getSystemGateway().publishSchedulerEvent(cmdstr,0,scheduler);

        mpLogger.logOperation(LogConsts.OPR_USER, "Load \"" + vsLoadId
            + "\" at station " + vpStationData.getStationName()
            + ": re-scheduled Retrieval");
        displayInfoAutoTimeOut("Re-scheduled Retrieval for load \""
            + vsLoadId + "\"");
      }
    }
  }

  /**
   * Recovers a Moving, Move Sent, or Move Error load that is moving station 
   * to station.
   * 
   * This is nearly identical to recoverStoringLoad().  Perhaps the two should
   * be combined.
   * 
   * @param ipLoadData
   * @throws DBException
   */
  protected void recoverTransferMovingLoad(LoadData ipLoadData) throws DBException
  {
    StationData vpStationData = mpStnServer.getStation(ipLoadData.getAddress());
    String vsDialogText = null;
    String isLoadId = ipLoadData.getLoadID();
    int vnLoadMoveStatus = ipLoadData.getLoadMoveStatus();
    LoadData vpTempLoadData = null;
    
    if (vnLoadMoveStatus == DBConstants.MOVEERROR ||
        vnLoadMoveStatus == DBConstants.MOVING)
    {
      //
      // First Check for older Move Error loads...if there are any,
      // make them recover them first
      //
      vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(), 
          DBConstants.MOVEERROR);
      if(vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(isLoadId))
      {
        displayInfo("There Are \"Move Error\" loads...\n" + 
        "Recover them first");
        return;
      }
      else
      {
        // Then Check for older Moving loads...if there are any, 
        // make them recover them first 
        vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(), 
            DBConstants.MOVING); 
        if(vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(isLoadId))
        {
          displayInfo("There are older \"Moving\" loads...\n" +
          "Either allow them to complete moving or recover them first");
          return;
        }
      }
      vsDialogText = "Has load \"" + ipLoadData.getLoadID()
          + "\" arrived at "
          + mpLoc.describeLocation(
              ipLoadData.getNextWarehouse(), ipLoadData.getNextAddress());
      if (displayYesNoPrompt(vsDialogText))
      {
        vpStationData = mpStnServer.getStation(ipLoadData.getNextAddress());
        // find out who is scheduling this station
        String scheduler = mpStnServer.getStationsScheduler(ipLoadData.getNextAddress());
        // send required completion messages
        String cmdstr;
        String vsRecoverType;
        if (vpStationData.getArrivalRequired() == DBConstants.YES)
        {
          cmdstr = mpLEDF.processArrivalReport(ipLoadData.getLoadID(), 
              ipLoadData.getNextAddress(), ipLoadData.getHeight(), 0, 
              ipLoadData.getLoadID(), "");
          vsRecoverType = "Arrival";
        }
        else
        {
          cmdstr = mpLEDF.processOperationCompletion(ipLoadData.getLoadID(),
              1, 0, ipLoadData.getAddress(), ipLoadData.getNextAddress(), 
              ipLoadData.getNextAddress(), ipLoadData.getShelfPosition(),
              "000000000", ipLoadData.getNextShelfPosition(), 0, "", "", "");
          vsRecoverType = "Store Complete";
        }
        // send the load event message
        getSystemGateway().publishLoadEvent(cmdstr,0,scheduler);
        mpLogger.logOperation(LogConsts.OPR_USER, "Load \"" + isLoadId
            + "\" at station " + ipLoadData.getAddress() + ": Re-Sent "
            + vsRecoverType);
        displayInfoAutoTimeOut(vsRecoverType + " sent for load \"" + isLoadId
            + "\"");
        return;
      }
    }
    else if (vnLoadMoveStatus == DBConstants.MOVESENT)
    {
      vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(), 
          DBConstants.MOVEERROR);
      //
      // First Check for Move Error loads...if there are any, 
      // make them recover them first
      //
      if(vpTempLoadData != null)
      {
        displayInfo("There are \"Move Error\" loads...\nRecover them first");
        return;
      }
      vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(), 
          DBConstants.MOVING);
      //
      // First Check for Moving loads...if there are any, make them recover
      // them first (or wait till they are done moving)
      //
      if(vpTempLoadData != null)
      {
        displayInfo("There are Moving loads...\n" + 
            "Either allow them to complete Moving or recover them first");
        return;
      }
      else
      {
        // Then Check for OLDER Move sent loads...if there are any, 
        // make them recover them first 
        vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(), 
            DBConstants.MOVESENT); 
        if(vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(isLoadId))
        {
          displayInfo("There Are older \"Move Sent\" loads...\nRecover them first");
          return;
        }
      }
    }
    
    vsDialogText = "Do you want to re-schedule the movement of load \""
          + ipLoadData.getLoadID() + "\"";
    if (displayYesNoPrompt(vsDialogText))
    {
      // find out who is scheduling this station
      String scheduler = mpStnServer.getStationsScheduler(vpStationData.getStationName());
      // reset the load move status
      mpLoadServer.setParentLoadMoveStatus(ipLoadData.getLoadID(),
          DBConstants.MOVEPENDING, msRecoveryNote);
      // send the scheduler event message to wake up the scheduler
      String cmdstr = mpLEDF.moveLoadStationStation(isLoadId, 
          vpStationData.getStationName(), vpStationData.getStationName(), 
          null, isLoadId, "", ipLoadData.getHeight());
      ThreadSystemGateway.get().publishLoadEvent(cmdstr,0,scheduler);

      mpLogger.logOperation(LogConsts.OPR_USER, "Load \"" + isLoadId
          + "\": re-scheduled Store");
      displayInfoAutoTimeOut("Re-scheduled Store for load \"" + isLoadId + "\"");
    }
  }

  
  /**
   * Recover Retrieving, Retrieve Sent, and Retrieve Error loads
   * @param ipLoadData
   * @throws DBException
   */
  protected void recoverRetrievingLoad(LoadData ipLoadData) throws DBException
  {
    StationData vpStationData = mpStnServer.getStation(ipLoadData.getNextAddress());
    
    /*
     * If the station is null, this is hopefully a location-location move.
     */
    if (vpStationData == null)
    {
      recoverLocationToLocationLoad(ipLoadData);
      return;
    }
    
    String vsDialogText = null;
    String vsLoadID = ipLoadData.getLoadID();
    int vnLoadMoveStatus = ipLoadData.getLoadMoveStatus();
    LoadData vpTempLoadData = null;
    
    if (vnLoadMoveStatus == DBConstants.RETRIEVEERROR ||
        vnLoadMoveStatus == DBConstants.RETRIEVING)
    {
      //
      // First Check for older retrieve error loads. If there are any,
      // make them recover them first (or wait till they are done retrieving)
      //
      vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(), 
                                                          DBConstants.RETRIEVEERROR);
      if(vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(vsLoadID))
      {
        displayInfo("There are \"Retrieve Error\" loads...\nRecover them first");
        return;
      }
      else
      {
        // Then Check for OLDER retrieving loads. If there are any, 
        // make them recover them first.
        vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(), 
                                                        DBConstants.RETRIEVING); 
        if(vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(vsLoadID))
        {
          displayInfo("There are older \"Retrieving\" loads...\n"
              + "Either allow them to complete Retrieving or Recover them first");
          return;
        }
      }
      vsDialogText = "Has load \"" + ipLoadData.getLoadID()
          + "\" been Retrieved from " 
          + mpLoc.describeLocation(ipLoadData.getWarehouse(), ipLoadData.getAddress()) 
          + " to " 
          + mpLoc.describeLocation(ipLoadData.getNextWarehouse(), ipLoadData.getNextAddress());
      if (displayYesNoPrompt(vsDialogText))
      {
       vsDialogText = "Do you want to move load " + ipLoadData.getLoadID() +
                    " to " + ipLoadData.getNextAddress();
        if (displayYesNoPrompt(vsDialogText))
        {
          // find out who is scheduling this station
          String scheduler = mpStnServer.getStationsScheduler(vpStationData.getStationName());
            // send work complete to everyone
          String cmdstr = mpLEDF.processOperationCompletion(ipLoadData.getLoadID(),
              2,0,AGCDeviceConstants.RACKSTATION, vpStationData.getStationName(), 
              ipLoadData.getAddress(), ipLoadData.getShelfPosition(), "",
              ipLoadData.getNextShelfPosition(), ipLoadData.getHeight(), "", "", "");
          
          getSystemGateway().publishLoadEvent(cmdstr,0,scheduler);
          if(vpStationData.getArrivalRequired() == DBConstants.YES)
          { // Send arrival only to stations that require it
            String cmdstr2 = mpLEDF.processArrivalReport(ipLoadData.getLoadID(), 
                vpStationData.getStationName(), vpStationData.getHeight(), 1,
                ipLoadData.getBCRData(), "");
            getSystemGateway().publishLoadEvent(cmdstr2,0,scheduler);
          }
          mpLogger.logOperation(LogConsts.OPR_USER, "Load \"" + vsLoadID
              + "\" at station: " + vpStationData.getStationName()
              + ": Re-Sent Arrival");
          displayInfoAutoTimeOut("Messages sent for load \"" + vsLoadID + "\"");
        }
        else
        {
          displayInfoAutoTimeOut("Move load \"" + vsLoadID + "\" manually.");
        }
        return;
      }
    }
    else if (vnLoadMoveStatus == DBConstants.RETRIEVESENT)
    {
      vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(), 
          DBConstants.RETRIEVEERROR);
      //
      // First Check for retrieve error loads...if there are any, 
      // make them recover them first
      //
      if(vpTempLoadData != null)
      {
        displayInfo("There are \"Retrieve Error\" loads...\nRecover them first");
        return;
      }
      vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(),
          DBConstants.RETRIEVING);
      //
      // First Check for retrieving loads...if there are any, make them recover
      // them first (or wait till they are done retrieving)
      //
      if(vpTempLoadData != null)
      {
        displayInfo("There are Retrieving loads...\n" + 
          "Either allow them to complete Retrieving or Recover them first");
        return;
      }
      else
      {
        //
        // Then Check for OLDER retrieve sent loads...if there are any, 
        // make them recover them first 
        //
        vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(), 
            DBConstants.RETRIEVESENT); 
        if(vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(vsLoadID))
        {
          displayInfo("There are older \"Retrieve Sent\" loads...\n" + 
            "Recover them first");
          return;
        }
      }
    }

    /*
     * If this load movement was been cancelled on WRx, there's no point in 
     * setting it back to retrieve pending.
     */
    if (!recoverRetrievePendingLoad(ipLoadData))
    {
      if (canRescheduleLoad(ipLoadData))
      {
        vsDialogText = "Do you want to re-schedule the retrieval of load \""
          + vsLoadID + "\"";
        if (displayYesNoPrompt(vsDialogText))
        {
          rescheduleRetrieve(ipLoadData, vpStationData);
        }
      }
    }
  }

  /**
   * Set a load back to retrieve pending and wake up the load's scheduler
   * 
   * @param ipLoadData
   * @param vpStationData
   * @throws DBException
   */
  protected void rescheduleRetrieve(LoadData ipLoadData,
      StationData vpStationData) throws DBException
  {
    String vsLoadID = ipLoadData.getLoadID();
    
    // find out who is scheduling this station
    String scheduler = mpStnServer.getStationsScheduler(vpStationData.getStationName());
    
    // reset the load move status
    mpLoadServer.setParentLoadMoveStatus(ipLoadData.getLoadID(),
        DBConstants.RETRIEVEPENDING, msRecoveryNote);
    
    // send the scheduler event message
    AllocationMessageDataFormat vpAllocData = new AllocationMessageDataFormat();
    vpAllocData.setOutBoundLoad(vsLoadID);
    vpAllocData.setFromWarehouse(ipLoadData.getWarehouse());
    vpAllocData.setFromAddress(ipLoadData.getAddress());
    vpAllocData.setOutputStation(ipLoadData.getNextAddress());
    vpAllocData.createDataString();
    String cmdstr = vpAllocData.createStringToSend();
    getSystemGateway().publishSchedulerEvent(cmdstr,0,scheduler);

    mpLogger.logOperation(LogConsts.OPR_USER, "Load \"" + vsLoadID
        + "\" at station " + vpStationData.getStationName()
        + ": re-scheduled Retrieval");
    displayInfoAutoTimeOut("Re-scheduled Retrieval for load \"" + vsLoadID
        + "\"");
  }

  /**
   * Set a load back to retrieve pending and wake up the load's scheduler for 
   * location-to-location moves
   * 
   * @param ipLoadData
   * @throws DBException
   */
  protected void rescheduleRetrieve(LoadData ipLoadData) throws DBException
  {
    String vsLoadID = ipLoadData.getLoadID();
    
    // find out who is scheduling this load
    String vsScheduler = Factory.create(StandardDeviceServer.class)
        .getSchedulerName(ipLoadData.getDeviceID());
    
    // reset the load move status
    mpLoadServer.setParentLoadMoveStatus(ipLoadData.getLoadID(),
        DBConstants.RETRIEVEPENDING, msRecoveryNote);
    
    // send the scheduler event message
    LoadEventDataFormat vpSwapMessage = Factory.create(
        LoadEventDataFormat.class, vsScheduler);
    String vsCommand = vpSwapMessage.moveLoadLocationLocation(
                ipLoadData.getParentLoadID(), ipLoadData.getAddress(),
                ipLoadData.getShelfPosition(), ipLoadData.getWarehouse(),
                ipLoadData.getNextAddress(), ipLoadData.getNextShelfPosition(),
                ipLoadData.getWarehouse(), ipLoadData.getHeight());

    getSystemGateway().publishLoadEvent(vsCommand, 0, ipLoadData.getDeviceID());

    String vsMessage = ipLoadData.getNextAddress().trim().length() > 0 ? 
        "Re-scheduled Loc-to-Loc Retrieval for load \""
        + vsLoadID + "\" to " + ipLoadData.getNextAddress()
        : "Re-scheduled Retrieval for load \"" + vsLoadID + "\"";
    mpLogger.logOperation(LogConsts.OPR_USER, vsMessage);
    displayInfoAutoTimeOut(vsMessage);
  }

  /**
   * Recover Storing, Store Sent, and Store Error loads
   * @param ipLoadData
   * @throws DBException
   */
  protected void recoverStoringLoad(LoadData ipLoadData) throws DBException
  {
    StationData vpStationData = mpStnServer.getStation(ipLoadData.getAddress());
    if (vpStationData == null)
    {
      recoverBadStoringLoad(ipLoadData);
      return;
    }
    String vsDialogText = null;
    String isLoadId = ipLoadData.getLoadID();
    int vnLoadMoveStatus = ipLoadData.getLoadMoveStatus();
    LoadData vpTempLoadData = null;
    
    if (vnLoadMoveStatus == DBConstants.STOREERROR ||
        vnLoadMoveStatus == DBConstants.STORING)
    {
      //
      // First Check for older Store error loads...if there are any,
      // make them recover them first
      //
      vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(), 
          DBConstants.STOREERROR);
      if(vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(isLoadId))
      {
        displayInfo("There are \"Store Error\" loads...\nRecover them first.");
        return;
      }
      else
      {
        // Then Check for OLDER storing loads...if there are any, 
        // make them recover them first 
        vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(), 
            DBConstants.STORING); 
        if(vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(isLoadId))
        {
          displayInfo("There are older \"Storing\" loads...\n"
              + "Either allow them to complete Storing or Recover them first");
          return;
        }
      }
      vsDialogText = "Has load \"" + ipLoadData.getLoadID()
          + "\" been stored at " 
          + mpLoc.describeLocation(
              ipLoadData.getNextWarehouse(), ipLoadData.getNextAddress());
      if (displayYesNoPrompt(vsDialogText))
      {
        vpStationData = mpStnServer.getStation(ipLoadData.getAddress());
        // find out who is scheduling this station
        String scheduler = mpStnServer.getStationsScheduler(vpStationData.getStationName());
        // send required completion messages
        String cmdstr = mpLEDF.processOperationCompletion(ipLoadData.getLoadID(),
            1, 0, vpStationData.getStationName(), AGCDeviceConstants.RACKSTATION, 
            ipLoadData.getNextAddress(), ipLoadData.getNextShelfPosition(), 
            "000000000", "000", 0, "", "", "");
        // send the load event message
        getSystemGateway().publishLoadEvent(cmdstr,0,scheduler);
        mpLogger.logOperation(LogConsts.OPR_USER, "Load \"" + isLoadId
            + "\" at station " + vpStationData.getStationName()
            + ": Re-Sent Store Complete");
        displayInfoAutoTimeOut("Store Complete sent for load \"" + isLoadId
            + "\"");
        return;
      }
    }
    else if (vnLoadMoveStatus == DBConstants.STORESENT)
    {
      vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(), 
          DBConstants.STOREERROR);
      //
      // First Check for Store error loads...if there are any, 
      // make them recover them first
      //
      if(vpTempLoadData != null)
      {
        displayInfo("There are \"Store Error\" loads...\nRecover them first");
        return;
      }
      vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(), 
          DBConstants.STORING);
      //
      // First Check for storing loads...if there are any, make them recover
      // them first (or wait till they are done storing)
      //
      if(vpTempLoadData != null)
      {
        displayInfo("There are Storing loads...\n"
              + "Either allow them to complete Storing or Recover them first");
        return;
      }
      else
      {
        // Then Check for OLDER Store sent loads...if there are any, 
        // make them recover them first 
        vpTempLoadData = mpLoadServer.getOldestLoadData(vpStationData.getStationName(), 
            DBConstants.STORESENT); 
        if(vpTempLoadData != null && !vpTempLoadData.getLoadID().equals(isLoadId))
        {
          displayInfo("There are older \"Store Sent\" loads...\nRecover them first");
          return;
        }
      }
    }
    
    vsDialogText = "Do you want to re-schedule the storage of load \""
          + ipLoadData.getLoadID() + "\"";
    if (displayYesNoPrompt(vsDialogText))
    {
      // find out who is scheduling this station
      String scheduler = mpStnServer.getStationsScheduler(vpStationData.getStationName());
      // reset the load move status
      mpLoadServer.setParentLoadMoveStatus(ipLoadData.getLoadID(),
          DBConstants.STOREPENDING, msRecoveryNote);
      // send the scheduler event message to wake up the scheduler
      String cmdstr = mpLEDF.moveLoadStationStation(isLoadId, 
          vpStationData.getStationName(), vpStationData.getStationName(),
          null, isLoadId, "", ipLoadData.getHeight());
      ThreadSystemGateway.get().publishLoadEvent(cmdstr,0,scheduler);

      mpLogger.logOperation(LogConsts.OPR_USER, "Load \"" + isLoadId + "\": re-scheduled Store");
      displayInfoAutoTimeOut("Re-scheduled Store for load \"" + isLoadId + "\"");
    }
  }

  /**
   * Recover a load that claims to be "Storing" but is in the rack.
   *  
   * @param ipLoadData
   */
  protected void recoverBadStoringLoad(LoadData ipLoadData) throws DBException
  {
    String vsDialogText = "This load should not be Storing.\nCancel the Store";
    if (displayYesNoPrompt(vsDialogText))
    {
      mpLoadServer.setParentLoadMoveStatus(ipLoadData.getLoadID(),
          DBConstants.NOMOVE, "");
      displayInfoAutoTimeOut("Cancelled Store for load \"" 
          + ipLoadData.getLoadID() + "\"");
    }
  }

  /**
   * Recover a load that is moving from one rack location to another rack
   * location (or at least not moving to a station).
   * 
   * @param ipLoadData
   * @throws DBException
   */
  protected void recoverLocationToLocationLoad(LoadData ipLoadData) 
    throws DBException
  {
    String vsDialogText = "Has load \"" + ipLoadData.getLoadID()
        + "\" been Retrieved \nfrom " 
        + mpLoc.describeLocation(ipLoadData.getWarehouse(), ipLoadData.getAddress()) 
        + " to " 
        + mpLoc.describeLocation(ipLoadData.getNextWarehouse(), ipLoadData.getNextAddress());
    if (ipLoadData.getNextAddress().trim().length() > 0 &&
        displayYesNoPrompt(vsDialogText))
    {
      vsDialogText = "Do you want to complete the movement";
      if (displayYesNoPrompt(vsDialogText))
      {
        mpLEDF.setLoadID(ipLoadData.getLoadID());
        mpLEDF.setSourceLocation(ipLoadData.getNextAddress());
        mpLEDF.setDestinationLocation(ipLoadData.getNextAddress());

        mpSchedServer.updateLoadForShelfToShelfStoreComplete(mpLEDF);
        mpLogger.logOperation("User " + SKDCUserData.getLoginName()
            + " recovered load \"" + ipLoadData.getLoadID() + "\": moved to "
            + mpLoc.describeLocation(ipLoadData.getNextWarehouse(), 
                ipLoadData.getNextAddress()));
      }
    }
    else
    {
      if (canRescheduleLoad(ipLoadData))
      {
        vsDialogText = "Do you want to re-schedule the movement of load \""
            + ipLoadData.getLoadID() + "\"";
        if (displayYesNoPrompt(vsDialogText))
        {
          rescheduleRetrieve(ipLoadData);
        }
      }
    }
  }
  
  /**
   * Recover a Retrieve Pending load that is stuck because its order/move was
   * forcibly deleted.
   * 
   * @param ipLD - LoadData
   * @return true if it was recovered, false if it wasn't.
   * @throws DBException
   */
  protected boolean recoverRetrievePendingLoad(LoadData ipLD) throws DBException
  {
    /*
     * If there are no moves for this load, and the AGC/SRC isn't going
     * to move it, then cancel the move.
     */
    MoveData vpMoveData = mpMoveServer.getNextMoveRecord(ipLD.getLoadID());
    if (vpMoveData == null)
    {
      if (displayYesNoPrompt(MOVEMENT_CANCELLED))
      {
        mpLoadServer.setParentLoadMoveStatus(ipLD.getLoadID(), DBConstants.NOMOVE, "");
        ipLD.setLoadMoveStatus(DBConstants.NOMOVE);
        return true;
      }
    }
    return false;
  }

  /**
   * Recover a Store Pending load that is stuck because the mode change stuff
   * on the SRC is retarded and reports Retrieve when it is really in a post-
   * pick store state.
   * 
   * @param ipLD - LoadData
   * @return true if it was recovered, false if it doesn't need it.
   */
  protected boolean recoverStorePendingLoad(LoadData ipLD)
  {
    StationData vpSD = mpStnServer.getStation(ipLD.getAddress());
    if (vpSD != null && vpSD.getStationType() == DBConstants.REVERSIBLE)
    {
      if (vpSD.getBidirectionalStatus() != DBConstants.STOREMODE)
      {
        if (displayYesNoPrompt("Force station " + vpSD.getStationName()
            + " into Store Mode in Warehouse Rx"))
        {
          mpStnServer.setBidirectionalMode(vpSD.getStationName(), 
              DBConstants.STOREMODE);
          
          String cmdstr = mpLEDF.moveLoadStationStation(ipLD.getLoadID(), 
              vpSD.getStationName(), vpSD.getStationName(), 
              null, ipLD.getLoadID(), "", ipLD.getHeight());
          String vsScheduler;
          try
          {
            vsScheduler =  mpStnServer.getStationsScheduler(vpSD.getStationName());
          }
          catch(DBException e)
          {
            displayError("Scheduler not found attached to station " + vpSD.getStationName());
            return false;
          }

          ThreadSystemGateway.get().publishLoadEvent(cmdstr, 0, vsScheduler);

          mpLogger.logOperation(LogConsts.OPR_USER,
              "Forced Store Mode for station " + vpSD.getStationName());
          displayInfoAutoTimeOut("Forced Store Mode for station "
              + vpSD.getStationName());
        }
        
        return true;
      }
    }
    return false;
  }

  /**
   * Recover a load that is has an ERROR status. This load is most likely stuck
   * on the crane due to a bin-full or size-mismatch error that could not be
   * automatically recovered (no appropriate empty locations and no reject route
   * defined).
   * 
   * @param ipLoadData
   */
  protected void recoverErrorLoad(LoadData ipLoadData)
  {
    AlternateLocationFrame vpALF = Factory.create(AlternateLocationFrame.class, ipLoadData);
    mpParentFrame.addSKDCInternalFrameModal(vpALF);
  }
  
  /*--------------------------------------------------------------------------*/
  /**
   *  Method to display an Informational message in an option pane.
   *
   *  @param msg Text to be displayed.
   */
  protected void displayInfo(String msg)
  {
    JOptionPane.showMessageDialog(mpParentFrame, msg, "Information Only",
        JOptionPane.INFORMATION_MESSAGE);
  }

  /*--------------------------------------------------------------------------*/
  /**
   *  Method to display an Informational message in an option pane.
   *
   *  @param prompt Text to be displayed.
   */
  protected boolean displayYesNoPrompt(String prompt)
  {
    int resp = JOptionPane.showConfirmDialog(mpParentFrame, prompt + "?",
        "Question", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    return (resp == JOptionPane.YES_OPTION);
  }

  /*--------------------------------------------------------------------------*/
  /**
   *  Method to display an Informational message for a period of time.
   *
   *  @param msg Text to be displayed.
   */
  protected void displayInfoAutoTimeOut(String msg)
  {
    if (!msg.endsWith("."))
    {
      msg += ".";
    }
    mpParentFrame.displayInfo(msg);
  }

  /*--------------------------------------------------------------------------*/
  /**
   *  Method to display an Error message in an option pane.
   *
   *  @param msg Text to be displayed.
   */
  protected void displayError(String msg)
  {
    mpLogger.logDebug(msg + " - displayError()");
    JOptionPane.showMessageDialog(mpParentFrame, msg, "Error",
        JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Display a prompt for the height if one is required
   * @return
   */
  protected int displayHeightPrompt()
  {
    Integer[] vapHeights = { 0, 1, 2, 3 };
    try
    {
      vapHeights = Factory.create(StandardLocationServer.class).getLocationHeights();
    }
    catch (DBException dbe)
    {
      mpLogger.logException(dbe);
    }
    
    if (vapHeights.length == 1)
    {
      return vapHeights[0];
    }
    
    Object vpHeight = JOptionPane.showInputDialog(mpParentFrame, 
        "What height is the load?", "Question", JOptionPane.QUESTION_MESSAGE, 
        null, vapHeights, vapHeights[0]);
    int vnHeight = Integer.valueOf(vpHeight.toString()).intValue();
    return (vnHeight);
  }
  
  /**
   * Can this movement be rescheduled?
   * @param ipLoadData
   * @return
   */
  protected boolean canRescheduleLoad(LoadData ipLoadData)
  {
    return true;
  }
}
