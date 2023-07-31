package com.daifukuamerica.wrxj.dataserver.standard;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.Move;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.ReasonCode;
import com.daifukuamerica.wrxj.dbadapter.data.RouteData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.dbadapter.data.TableJoin;
import com.daifukuamerica.wrxj.device.agc.AGCDeviceConstants;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.messages.HostError;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.jdbc.InvalidDataException;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.messageformat.controlevent.ControlEventDataFormat;
import com.daifukuamerica.wrxj.messageformat.loadevent.LoadEventDataFormat;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * <B>Description:</B> Scheduler process methods<BR>
 *
 * @author       mandrus<BR>
 * @version      1.0
 *
 * <BR>Copyright (c) 2008 by Daifuku America Corporation
 */
public class StandardSchedulerServer extends StandardServer
{
  public static final String BIN_FULL_PREFIX = "BF";
  
  protected String msBarCodeTrackingId = null;
  protected int msBarCodeTrackingIdStart = 0;
  protected int msBarCodeTrackingIdEnd = 0;

  public static final int STORE_LOAD_OK = 0;
  public static final int STORE_LOAD_FAILED = -1;

  protected StandardHostServer        mpHostServer    = null;
  protected StandardDeviceServer      mpDeviceServer  = null;
  protected StandardInventoryServer   mpInvServer     = null;
  protected StandardLoadServer        mpLoadServer    = null;
  protected StandardLocationServer    mpLocServer     = null;
  protected StandardMoveServer        mpMoveServer    = null;
  protected StandardPickServer        mpPickServer    = null;
  protected StandardPoReceivingServer mpPOServer      = null;
  protected StandardRouteServer       mpRouteServer   = null;
  protected StandardStationServer     mpStationServer = null;

  protected Load mpLoad = Factory.create(Load.class);

  protected String msMyClass = null;

  /**
   * Constructor w/o key name
   */
  public StandardSchedulerServer()
  {
    this(null);
  }

  /**
   * Constructor with key name
   *
   * @param isKeyName
   */
  public StandardSchedulerServer(String isKeyName)
  {
    super(isKeyName);
    msMyClass = getClass().getSimpleName();

    //
    // See if we have a property to extract a Tracking Id from a field in
    // a Load's Barcode.  Property is a String specifying the substring
    // (inclusive) start and end positions.
    // Example "0-6"    0123456789012345678901234
    //                  ^^^^^^^
    //
    //         "12-19"  0123456789012345678901234
    //                              ^^^^^^^^
    //
    msBarCodeTrackingId = Application.getString("BarCodeTrackingId");
    if (msBarCodeTrackingId != null)
    {
      try
      {
        StringTokenizer vpBarCodeTrackingIdIterator = new StringTokenizer(msBarCodeTrackingId, "-");
        //
        String vsBarCodeTrackingIdStart = vpBarCodeTrackingIdIterator.nextToken();
        Integer vpInteger = Integer.valueOf(vsBarCodeTrackingIdStart);
        msBarCodeTrackingIdStart = vpInteger.intValue();
        //
        String vsBarCodeTrackingIdEnd = vpBarCodeTrackingIdIterator.nextToken();
        vpInteger = Integer.valueOf(vsBarCodeTrackingIdEnd);
        msBarCodeTrackingIdEnd = vpInteger.intValue() + 1;
        //
        logDebug("Using BarCodeTrackingId Characters \"" + msBarCodeTrackingId
            + "\" (" + msBarCodeTrackingIdStart + "-" + msBarCodeTrackingIdEnd
            + ")");
      }
      catch (Exception e)
      {
        logException(e, "BarCodeTrackingId Characters \"" + msBarCodeTrackingId + "\"");
      }
    }
  }
  
  /**
   * Web application constructor for per user connection pooling
   * @param keyName
   * @param dbo
   */
  public StandardSchedulerServer(String keyName, DBObject dbo)
  {
	  super(keyName, dbo); 
	  msMyClass = getClass().getSimpleName();

	    //
	    // See if we have a property to extract a Tracking Id from a field in
	    // a Load's Barcode.  Property is a String specifying the substring
	    // (inclusive) start and end positions.
	    // Example "0-6"    0123456789012345678901234
	    //                  ^^^^^^^
	    //
	    //         "12-19"  0123456789012345678901234
	    //                              ^^^^^^^^
	    //
	    msBarCodeTrackingId = Application.getString("BarCodeTrackingId");
	    if (msBarCodeTrackingId != null)
	    {
	      try
	      {
	        StringTokenizer vpBarCodeTrackingIdIterator = new StringTokenizer(msBarCodeTrackingId, "-");
	        //
	        String vsBarCodeTrackingIdStart = vpBarCodeTrackingIdIterator.nextToken();
	        Integer vpInteger = Integer.valueOf(vsBarCodeTrackingIdStart);
	        msBarCodeTrackingIdStart = vpInteger.intValue();
	        //
	        String vsBarCodeTrackingIdEnd = vpBarCodeTrackingIdIterator.nextToken();
	        vpInteger = Integer.valueOf(vsBarCodeTrackingIdEnd);
	        msBarCodeTrackingIdEnd = vpInteger.intValue() + 1;
	        //
	        logDebug("Using BarCodeTrackingId Characters \"" + msBarCodeTrackingId
	            + "\" (" + msBarCodeTrackingIdStart + "-" + msBarCodeTrackingIdEnd
	            + ")");
	      }
	      catch (Exception e)
	      {
	        logException(e, "BarCodeTrackingId Characters \"" + msBarCodeTrackingId + "\"");
	      }
	    }
	  
  }

  /**
   *  Shuts down this controller by cancelling any timers and shutting down the
   *  Equipment.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();

    if (mpHostServer != null)
    {
      mpHostServer.cleanUp();
      mpHostServer = null;
    }
    if (mpInvServer != null)
    {
      mpInvServer.cleanUp();
      mpInvServer = null;
    }
    if (mpLoadServer != null)
    {
      mpLoadServer.cleanUp();
      mpLoadServer = null;
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
   * Check to see if station has a load to Retrieve.  If station is online and
   * there is not already a load that has had a command sent and we are waiting
   * for a retrieve response ok then see if max enroute is less that current
   *
   * @param ipSD - StationData of station that might want work
   * @param isSchedulerName
   * @param vnNumberOfLoadsToRetrieve
   *
   * @return <code>ArrayList</code>
   */
  public ArrayList<LoadEventDataFormat> anyLoadsToRetrieveToStation(StationData ipSD,
                         String isSchedulerName, int vnNumberOfLoadsToRetrieve)
  {
    initializeLoadServer();
    initializeStationServer();

    String vsStation = ipSD.getStationName();

    TransactionToken tt = null;
    ArrayList<LoadEventDataFormat> moveCommandList = new ArrayList<LoadEventDataFormat>();
    if (mpStationServer.canStationRetrieveALoad(ipSD) == false)
    {
      //
      // This station is the correct type of station to accept a retrieved
      // load and its device is Online.
      //
      return moveCommandList;
    }

    // Can this scheduler schedule for this station
    if (mpStationServer.isStationScheduler(isSchedulerName, ipSD.getStationName()) == false)
    {
      logDebug("Scheduler doesn't schedule for Station " + vsStation);
      return moveCommandList;
    }
    // can we schedule additional loads to be retrieved to this station.
    if (isStationWaitingForRetrieveOkResponse(vsStation, ipSD.getMaxAllowedEnroute()))
    {
      logDebug("Station " + vsStation + " is waiting for Retrieve Ok Response");
      return moveCommandList;
    }
    int enrouteLoadCount = getEnrouteLoadCount(vsStation);
    if (ipSD.getMaxAllowedEnroute() >= enrouteLoadCount + vnNumberOfLoadsToRetrieve)
    {
      List<LoadData> retrievePendingLoads = getRetrievePendingLoads(ipSD,
          isSchedulerName);
      if (retrievePendingLoads == null)
      {
        logDebug("Station " + vsStation
            + " has NO Retrieve Pending Loads - anyLoadsToRetrieveToStation()");
        return moveCommandList;
      }
      else if (retrievePendingLoads.size() < vnNumberOfLoadsToRetrieve)
      {
        // wait till I have number of retrieve pending loads that are retrieve pending
        logDebug("Station " + vsStation + " has only "
              + retrievePendingLoads.size()
              + " Retrieve Pending Loads - We want " + vnNumberOfLoadsToRetrieve
              + " retrieve pending Loads - anyLoadsToRetrieveToStation() ");
        return moveCommandList;
      }

      /*
       * If we get this far then we have at least one load to schedule.
       */
      int vnEnrouteAllowed = ipSD.getMaxAllowedEnroute() - enrouteLoadCount;
      while (moveCommandList.size() < vnEnrouteAllowed && retrievePendingLoads.size() > 0)
      {
        LoadData loadData = retrievePendingLoads.remove(0);
        String vsLoadID = loadData.getLoadID();

        try
        {
          String vsRouteID = "";
          StationData vpFinalDestination = null;
          String vsFinalWarehouse = null;
          String vsFinalAddress = null;

          /*
           * If this station is a transfer or AGC transfer station, check the
           * enroute count at the final destination (from the load's or move's
           * route)
           */
          if (ipSD.getStationType() == DBConstants.TRANSFER_STATION ||
              ipSD.getStationType() == DBConstants.AGC_TRANSFER)
          {
            vsRouteID = mpLoad.getRetrieveRoute(vsLoadID, vsStation, isSchedulerName);
            vpFinalDestination = mpStationServer.getStation(vsRouteID);
            if (vpFinalDestination != null)
            {
              int vnEnrouteToDest = mpLoad.getEnrouteCountPlusAtStation(vsRouteID);
              if (vnEnrouteToDest >= vpFinalDestination.getMaxAllowedEnroute())
              {
                continue;
              }
              vsFinalWarehouse = vpFinalDestination.getWarehouse();
              vsFinalAddress = vpFinalDestination.getStationName();
            }
          }

          /*
           * Lock the load.  If it is not Retrieve Pending, then we have already
           * processed it.
           */
          tt = startTransaction();
          LoadData vpLDData = mpLoadServer.getLoad(vsLoadID, true);

          if (vpLDData.getLoadMoveStatus() != DBConstants.RETRIEVEPENDING
                                    ||
              !doPreliminaryRetrieveChecks(vpLDData, ipSD, isSchedulerName,
                                           moveCommandList))
          {
            continue;
          }
                                       // Make sure we use the determined route
                                       // from above if necessary.
          if (!vsRouteID.isEmpty())
            vpLDData.setRouteID(vsRouteID);

          int vnPriorityCategory = ipSD.getRetrievalPriority();
          int vnReInputFlag = ipSD.getReinputFlag();
          int vnRetrieveCommandDetail = ipSD.getRetrieveCommandDetail();

          /*
           * Update the load that we will retrieve
           */
          LoadEventDataFormat vpLEM = changeLoadToRetrieveSent(vpLDData,
              isSchedulerName, vsRouteID, vnPriorityCategory, vnReInputFlag,
              vnRetrieveCommandDetail, ipSD.getWarehouse(), vsStation,
              LoadData.DEFAULT_POSITION_VALUE,
              vsFinalWarehouse, vsFinalAddress);

          commitTransaction(tt);
          if (vpLEM != null)
          {
            moveCommandList.add(vpLEM);
            logDebug("LoadId \"" + vsLoadID + "\" Scheduling to move to " +
                    "station " + vsStation);
          }
        }
        catch (DBException e)
        {
          logException(e, "LoadId \"" + vsLoadID
              + "\" (Parent) Exception Changing Parent Load Status  - "
              + getClass().getSimpleName() + ".anyLoadsToRetrieveToStation");
        }
        finally
        {
          endTransaction(tt);
        }
      }
    }
    else
    {
      logDebug("Station " + vsStation + " has " + enrouteLoadCount
          + " enroute loads, doesn't need any more");
    }

    return moveCommandList;
  }

  /**
   * This method will check the station to see if it is a store station, online
   * and that the device is online. If it is then. This method looks for the
   * oldest loads at the station that are store-pending depending on the number
   * in numberOfLoadsToStore. check the location that is in the next address
   * field of the load to see if it is a valued station. If it is sends
   * transport command. If not then check to see if next address is a valid
   * location for the load by height, aisle group, zone, warehouse and location
   * type. If it is it sends the transport command. If not then it requests a
   * correct location and sends the transport command.
   *
   * @param ipSD the station to check
   * @param isScheduler the scheduler this is for
   * @return ArrayList of LoadEventDataFormat messages of the load/s to store
   */
  public ArrayList<LoadEventDataFormat> anyLoadsToStoreAtStation(
      StationData ipSD, String isScheduler)
  {
    initializeLoadServer();
    initializeStationServer();

    ArrayList<LoadEventDataFormat> vpStoreCmds = new ArrayList<LoadEventDataFormat>();
    String vsStation = ipSD.getStationName();

    String vsMethodInfo = " - " + msMyClass + ".anyLoadsToStoreAtStation() ";
    String vsErrorMsg = mpStationServer.canStationStoreALoad(ipSD, isScheduler,
        mpLoadServer);
    if (vsErrorMsg.length() == 0)
    {
      List<LoadData> vpStorePendingLoads = doesLoadNeedStoring(vsStation,1);
      if(vpStorePendingLoads == null || vpStorePendingLoads.size() == 0)
      {
        logDebug("Station " + vsStation + " has NO Store Pending Loads"
            + vsMethodInfo);
      }
      else
      {
        LoadData vpLD = vpStorePendingLoads.get(0);
        if(vpLD != null)
        {
          String vsNextAddress = vpLD.getNextAddress();

          if (mpStationServer.isStationCaptiveForStore(ipSD))
          {
            /*
             * this is a captive station and not captive insert so store
             * location should be next address
             */
            if (vpLD.getLoadPresenceCheck() != DBConstants.YES)
            {
              logError("LoadId \"" + vpLD.getLoadID()
                  + "\" Station is captive, must be in captive insert to store "
                  + "new load at Location " + vpLD.getNextAddress());
              // Nothing to do
              return vpStoreCmds;
            }

            if (vsNextAddress.trim().length() == 0)
            {
              logError("LoadId \"" + vpLD.getLoadID()
                  + "\" Station is captive, next address must be set."
                  + vpLD.getNextAddress());
              // Nothing to do
              return vpStoreCmds;
            }
          }

          /*
           * We have something to do.  Get the store command
           */
          try
          {
            LoadEventDataFormat vpStoreCmd = getStoreCommand(vpLD, ipSD, isScheduler);
            if (vpStoreCmd != null)
            {
              vpStoreCmds.add(vpStoreCmd);
            }
          }
          catch (DBException dbe)
          {
            logException("Error getting store command for \""
                + vpLD.getLoadID() + "\"", dbe);
            // Change it to Store Error so we don't keep trying it.
            // Actually, don't.  If it can't store, then stuff behind it can't,
            // either.
//            setLoadDataMoveStatus(vpLD, DBConstants.STOREERROR);
            mpLoadServer.updateLoadData(vpLD, false);
          }
        }
      }
    }
    else    // Uh oh, can't store.
    {
      logDebug("Can't store a load: " + vsErrorMsg + vsMethodInfo);
    }

    return vpStoreCmds;
  }

  /**
   * Get the store command
   *
   * @param ipLD
   * @param ipSD
   * @param isScheduler
   * @return
   * @throws DBException
   */
  protected LoadEventDataFormat getStoreCommand(LoadData ipLD,
      StationData ipSD, String isScheduler) throws DBException
  {
    initializeRouteServer();
    initializeStationServer();

    // Check the next location (populate if necessary)
    validateNextLocation(ipLD, ipSD, isScheduler);

    // Now we are ready to store
    if (ipLD.getNextAddress().trim().length() > 0)
    {
      if (mpStationServer.exists(ipLD.getNextAddress()))
      {
        // Station-To-Station Store
        return getStnToStnStoreCommand(ipLD, ipSD, isScheduler);
      }
      else
      {
        // Station-To-Location Store
        return getStnToLocStoreCommand(ipLD, ipSD, isScheduler);
      }
    }
    else
    {
      // We ran out of empty locations and no reject is defined.
      return null;
    }
  }

  /**
   * Route a load to a reject station.  Does NOT update the database.
   *
   * @param ipLD - Load that needs destination information
   * @param ipSD - Current station of the load
   */
  public void directLoadToReject(LoadData ipLD, StationData ipSD)
  {
    try
    {
      // Send the load to a reject station if one exists.
      String vsRejectWarehouse = ipSD.getWarehouse();
      String vsRejectRoute = mpRouteServer.getNextRouteDest(
          ipSD.getRejectRoute(), ipSD.getStationName());

      if (vsRejectWarehouse.trim().length() != 0 &&
          vsRejectRoute.trim().length() != 0)
      {
        logDebug("Attempting to send Load " + ipLD.getLoadID()
            + " for height " + ipLD.getHeight()
            + " to reject station " + vsRejectRoute
            + " since no suitable empty locations exist...");
        ipLD.setNextWarehouse(vsRejectWarehouse);
        ipLD.setNextAddress(vsRejectRoute);
      }
      else
      {
        // Turn the rack-full light on.
        sendNoEmptyLocation(ipSD, true);
      }
    }
    catch (DBException ex)
    {
      logException(ex, "Exception trying to store to reject station.");
    }
  }

  /**
   * Validate the next address if it is populated.  Populate it if necessary
   * and possible.
   *
   * @param ipLD
   * @param ipSD
   * @param isScheduler
   * @throws DBException
   */
  protected void validateNextLocation(LoadData ipLD, StationData ipSD,
                                      String isScheduler) throws DBException
  {
    initializeLocationServer();
    initializeRouteServer();
    initializeStationServer();

    // If it's already got a next address make sure it's OK to use.
    if(ipLD.getNextAddress().trim().length() > 0)
    {
      // It is set - make sure it is valid
      if (!mpStationServer.exists(ipLD.getNextAddress()))
      {
        // Load is going to a location - make sure it's valid.
        int vnEmptyFlag = mpLocServer.getEmptyFlagValue(ipLD.getNextWarehouse(),
            ipLD.getNextAddress(), ipLD.getNextShelfPosition());
        if(vnEmptyFlag == DBConstants.UNOCCUPIED)
        {
          // is not occupied so we can use it.
          mpLocServer.setLocationEmptyFlag(ipLD.getNextWarehouse(),
              ipLD.getNextAddress(), ipLD.getNextShelfPosition(),
              DBConstants.LCRESERVED);
        }
        else if(vnEmptyFlag == DBConstants.LCRESERVED)
        {
          // reserved for this load already
          if (getLoadCountInRouteOrAtDestination(
              ipLD.getNextWarehouse(), ipLD.getNextAddress()) > 1 )
          {
            // Someone else is going to this location, better go somewhere else.
            ipLD.setNextAddress("");
          }
        }
        else if(vnEmptyFlag == DBConstants.OCCUPIED)
        {
          // someone's already there, better go somewhere else
          ipLD.setNextAddress("");
        }
      }
    }

    // Now see if the next address needs to be set
    if(ipLD.getNextAddress().trim().length() == 0)
    {
      String location[] = null;
      try
      {
        location = mpLocServer.findLocationForStoring(ipSD, ipLD);
      }
      catch(InvalidDataException eID)
      {
        logException(eID, "Error validating next location");
      }

      if (location == null)
      {
        // No empty locations.  Route to reject.
        directLoadToReject(ipLD, ipSD);
      }
      else     // We found a storage location.
      {
        ipLD.setNextWarehouse(location[0]);
        ipLD.setNextAddress(location[1]);
                                       // Turn the rack-full light off.
        sendNoEmptyLocation(ipSD, false);
      }
    }

    // Check routing
    if (ipLD.getNextAddress().trim().length() > 0 &&
        !mpStationServer.canStationStoreToLocation(ipSD,
        ipLD.getNextAddress(), ipLD.getHeight()))
    {
      /*
       * We can't store to the next location directly, but can we might be
       * able to route there via another station
       */
      try
      {
        String vsRoute = mpRouteServer.getFromToRoute(
            ipSD.getWarehouse(), ipSD.getStationName(),
            ipLD.getNextWarehouse(), ipLD.getNextAddress());
        String vsNextDest = mpRouteServer.getNextRouteDest(
            vsRoute, ipSD.getStationName());

        // vsNextDest SHOULD be a station.  Update the next location
        StationData vpNextStation = mpStationServer.getStation(vsNextDest);

        // ipLD.setRouteID(vsRoute);
        ipLD.setNextWarehouse(vpNextStation.getWarehouse());
        ipLD.setNextAddress(vpNextStation.getStationName());
      }
      catch (Exception e)
      {
        /*
         * Nope, we can't (a null pointer or DB exception proves it!)
         */
        throw new DBException("LoadId \"" + ipLD.getLoadID()
            + "\" cannot be stored to next location "
            + ipLD.getNextAddress(), e);
      }
    }
  }

  /**
   * Get a station-to-station transport command.
   *
   * @param ipLD
   * @param ipSD
   * @param isScheduler
   * @return
   */
  protected LoadEventDataFormat getStnToStnStoreCommand(LoadData ipLD,
      StationData ipSD, String isScheduler)
  {
    LoadEventDataFormat vpStoreCmd = Factory.create(LoadEventDataFormat.class,
                                                    isScheduler);

    // station to station move
    logDebug("LoadId \"" + ipLD.getLoadID() + "\" transfer to "
        + ipLD.getNextAddress());

    setLoadDataMoveStatus(ipLD, DBConstants.MOVESENT);
    mpLoadServer.updateLoadData(ipLD, false);

    String vsBarcode = ipLD.getBCRData();
    if (vsBarcode.trim().length() == 0)
    {
      vsBarcode = ipLD.getParentLoadID();
    }
    sendNoEmptyLocation(ipSD, false);
    vpStoreCmd.moveLoadStationStation(ipLD.getLoadID(), ipLD.getAddress(),
        ipLD.getNextAddress(), ipLD.getWarehouse(), vsBarcode, "", ipLD.getHeight());
    return vpStoreCmd;
  }

  /**
   * Get a station-to-location transport command.
   *
   * <P>This needs to be protected; it is overridden for double-deep.</P>
   *
   * @param ipLD
   * @param ipSD
   * @param isScheduler
   * @return
   */
  protected LoadEventDataFormat getStnToLocStoreCommand(LoadData ipLD,
      StationData ipSD, String isScheduler)
  {
    LoadEventDataFormat vpStoreCmd = Factory.create(LoadEventDataFormat.class,
        isScheduler);

    logDebug("LoadId \"" + ipLD.getParentLoadID() + "\" store to location "
        + ipLD.getNextWarehouse() + "-" + ipLD.getNextAddress());

    setLoadDataMoveStatus(ipLD, DBConstants.STORESENT);
    mpLoadServer.updateLoadData(ipLD, false);

    String vsBarcode = ipLD.getBCRData();
    if (vsBarcode.trim().length() == 0)
    {
      vsBarcode = ipLD.getParentLoadID();
    }
    sendNoEmptyLocation(ipSD, false);
    String vsEquipWhs = mpLocServer.getEquipWarehouse(ipLD.getWarehouse());

    vpStoreCmd.storeLoadStationLocation(ipLD.getParentLoadID(),
                                        ipLD.getAddress(), ipLD.getNextAddress(),
                                        ipLD.getNextShelfPosition(),
                                        vsEquipWhs, vsBarcode, null,
                                        ipLD.getHeight());

    return vpStoreCmd;
  }

  /**
   * If this is a U-Shape station or a P&D station and there is no Item move
   * (just a load retrieve), then we need to complete the move
   *
   * @param loadData
   * @throws DBException
   */
  protected void autoCompleteLoadMove(LoadData loadData) throws DBException
  {
    int stationType = mpStationServer.getStationType(loadData.getNextAddress());

    if (stationType == DBConstants.PDSTAND ||
        stationType == DBConstants.REVERSIBLE ||
        stationType == DBConstants.USHAPE_OUT)
    {
      TransactionToken tt = null;
      MoveData moveData;
      if ((moveData = mpMoveServer.getNextMoveRecord(
          loadData.getParentLoadID(), DBConstants.ITEMMOVE)) == null)
      {
        try
        {
          initializePickServer();

          tt = startTransaction();

          if ((moveData = mpMoveServer.getNextMoveRecord(
              loadData.getParentLoadID(), DBConstants.LOADMOVE)) != null)
          {
            mpPickServer.completeLoadRetrieveMove(moveData);
            logDebug("LoadId \"" + loadData.getLoadID()
                + "\" moved to Station " + loadData.getNextAddress()
                + " Load Move Deleted due to no item moves and P&D OR USHAPE");
          }
          else if ((moveData = mpMoveServer.getNextMoveRecord(
              loadData.getParentLoadID(), DBConstants.EMPTYMOVE)) != null)
          {
            mpPickServer.completeEmptyContainerMove(moveData);
            logDebug("LoadId \"" + loadData.getLoadID()
                + "\" moved to Station " + loadData.getNextAddress()
                + " Empty Container Move Deleted due to no item moves and P&D OR USHAPE");
          }

          commitTransaction(tt);
        }
        finally
        {
          endTransaction(tt);
        }
      }
    }
  }

  /**
   * Send no empty Location message
   * This is a stub method so that custom projects
   * can implement a 50 message
   */
  protected void sendNoEmptyLocation(StationData ipStationData, boolean izLightOn)
  {

  }


  /**
   * Auto pick a load going to a station.
   * It is assumed the the next address is a valid station.
   * @param loadData
   */
  public void autoPickLoad(LoadData loadData)
  {
    initializeStationServer();

    if (mpStationServer.isStationAutoPick(loadData.getNextAddress()))
    {
      logDebug("LoadId \"" + loadData.getLoadID() + "\" AutoPick load - "
          + msMyClass + ".autoPickLoad()");

      initializePickServer();
      mpPickServer.autoPickLoadFromStation(loadData.getLoadID());
    }
  }

  /**
   * Auto-store a load
   *
   * @param loadEventMessage
   * @return null if we couldn't find the load to store.
   * @throws DBException if there are database problems or if the load is not
   *         where it should be during an auto-store.
   */
  public LoadData autoStoreLoad(LoadEventDataFormat loadEventMessage)
         throws DBException, InvalidDataException
  {
    String vsLoadId;

    initializeInventoryServer();
    initializeLoadServer();
    initializePOReceivingServer();
    initializeStationServer();

    String vsSourceStation = loadEventMessage.getSourceStation();

    StationData vpStationData = mpStationServer.getStation(vsSourceStation);

    /*
     * If we got here and have a valid BCR then use it as load ID, otherwise we
     * must be creating the load ID from the load prefix
     */
    if (loadEventMessage.isBCRValid()
        && loadEventMessage.isBCRGoodRead()
        && !mpStationServer.isStationAutoStoreBCRAsItem(vsSourceStation))
    {
      vsLoadId = loadEventMessage.getFullBarCode();
    }
    else
    {
      // create unique load ID using station prefix if any
      vsLoadId = mpLoadServer.createRandomLoadID(vpStationData.getLoadPrefix());
    }

    /*
     * Get the auto-store load.  If it is null, then for some reason we will
     * not be storing.
     */
    LoadData vpLoadData = getAutoStoreLoad(vsLoadId, loadEventMessage);
    if (vpLoadData == null)
    {
      return null;
    }

    /*
     * Update vpLoadData's route.  This does NOT update the database.
     */
    try
    {
      getNextRouteLocation(vpStationData, vpLoadData);
    }
    catch(DBException dbe)
    {
      logException(dbe, msMyClass + ".autoStoreLoad");
    }

    /*
     * Do the right kind of auto-store based upon the station's configuration
     */
    switch (vpStationData.getAutoLoadMovementType())
    {
      case DBConstants.AUTORECEIVE_ER:
      case DBConstants.BOTH:
        vpLoadData = handleAutoStoreWithER(vpLoadData.getLoadID());
        break;
      case DBConstants.AUTORECEIVE_EXPECTED_LOAD:
        vpLoadData = handleAutoStoreERLoad(vsLoadId, vpStationData.getStationName());
        break;
      case DBConstants.AUTORECEIVE_ITEM:
        vpLoadData = handleAutoStoreWithItem(vsLoadId, vsSourceStation, vpLoadData);
        break;
      case DBConstants.AUTORECEIVE_LOAD:
        vpLoadData = handleAutoStoreLoad(vpLoadData);
        break;
      case DBConstants.AUTORECEIVE_BCR:
        vpLoadData = handleAutoStoreWithBCRAsItem(loadEventMessage, vpLoadData);
        break;
      default:
        logError("Invalid auto-store type: "
            + vpStationData.getAutoLoadMovementType());
        return null;
    }

    return vpLoadData;
  }

  /**
   *  Method to modify move status of a load.  This method should be called from
   *  within a transaction.  IF the new moves status is NOMOVE, This Method
   *  BLANKS OUT Next Warehouse, Final Warehouse Next address and final address.
   *
   * @param isLoadID <code>String</code> containing load ID.
   * @param inNewStatus <code>int</code> containing status to change to.
   */
  public void changeLoadMoveStatus(String isLoadID, int inNewStatus)
          throws DBException
  {
    LoadData lddata = Factory.create(LoadData.class);
    lddata.setKey(LoadData.LOADID_NAME, isLoadID);

    setLoadDataMoveStatus(lddata, inNewStatus);

    mpLoad.modifyElement(lddata);
  }

  /**
   * Change a load to Retrieve Sent and set it up for retrieval
   *
   * @param ipLoadData
   * @param isSchedulerName
   * @param isRouteID
   * @param inPriorityCategory
   * @param inReInputFlag
   * @param inRetrieveCommandDetail
   * @param isNextWarehouse
   * @param isNextAddress
   * @param isNextPosition
   * @param isFinalWarehouse
   * @param isFinalAddress
   * @param izUpdateMoves
   * @throws DBException
   */
  protected LoadEventDataFormat changeLoadToRetrieveSent(LoadData ipLoadData,
      String isSchedulerName, String isRouteID, int inPriorityCategory,
      int inReInputFlag, int inRetrieveCommandDetail, String isNextWarehouse,
      String isNextAddress, String isNextPosition, String isFinalWarehouse,
      String isFinalAddress) throws DBException
  {
    initializeLocationServer();

    /*
     * Mark the load as Retrieve Sent. It isn't really, quite yet, but we
     * don't want to process this load again and the sender doesn't want
     * to update the load anyway (it just forwards the message to the
     * device).
     */
    ipLoadData.setLoadMoveStatus(DBConstants.RETRIEVESENT);

    /*
     * Set next address to station
     */
    ipLoadData.setNextWarehouse(isNextWarehouse);
    ipLoadData.setNextAddress(isNextAddress);
    ipLoadData.setNextShelfPosition(isNextPosition);
    ipLoadData.setRouteID(isRouteID);
    if (isFinalWarehouse != null)
    {
      ipLoadData.setFinalWarehouse(isFinalWarehouse);
      ipLoadData.setFinalAddress(isFinalAddress);
    }
    mpLoad.updateLoadInfo(ipLoadData);

    /*
     * Build the proper message to retrieve the load.
     */
    String vsBarcode = ipLoadData.getBCRData();
    if (vsBarcode.trim().length() == 0)
    {
      vsBarcode = ipLoadData.getParentLoadID();
    }

    String vsEquipWhs = mpLocServer.getEquipWarehouse(ipLoadData.getWarehouse());
    LoadEventDataFormat vpLEM = Factory.create(LoadEventDataFormat.class,
                                               isSchedulerName);
    vpLEM.retrieveLoadLocationStation(ipLoadData.getParentLoadID(),
                                      ipLoadData.getAddress(),
                                      ipLoadData.getShelfPosition(), vsEquipWhs,
                                      ipLoadData.getNextAddress(),
                                      inPriorityCategory,
                                      inReInputFlag,
                                      inRetrieveCommandDetail,
                                      vsBarcode,
                                      null, ipLoadData.getHeight(), ipLoadData.getGroupNo());
    return vpLEM;
  }

  /**
   * Create a load in response to an arrival with bad barcode data
   *
   * @param loadEventMessage
   * @return data object representing newly created load.
   * @throws DBException for database error when adding load.
   */
  protected LoadData createBadBCRLoad(LoadEventDataFormat loadEventMessage)
            throws DBException
  {
    initializeLoadServer();
    initializeStationServer();

    LoadData newLoad = Factory.create(LoadData.class);
    newLoad = setLoadToStationDefaults(newLoad, loadEventMessage.getSourceStation());
    newLoad.setBCRData(loadEventMessage.getBarCode());
    newLoad.setAddress(loadEventMessage.getSourceStation());
    setLoadDataMoveStatus(newLoad, DBConstants.ARRIVEPENDING);
    newLoad.setHeight(loadEventMessage.getDimensionInfo());
    newLoad.setNextWarehouse(newLoad.getWarehouse());
    newLoad.setNextAddress(mpStationServer.getRejectStation(loadEventMessage.getSourceStation()));
    mpLoadServer.createRandomLoad(SKDCConstants.BR_LOAD_PREFIX, newLoad);
    return newLoad;
  }

  /**
   * Handle a Bin-Empty Error
   * @param isLoadID
   */
  public void createBinEmptyLoad(String isLoadID)
  {
    initializeInventoryServer();
    initializeLoadServer();
    initializeLocationServer();
    StandardDeallocationServer deallocationServer = Factory.create(StandardDeallocationServer.class);

    logDebug("LoadId \"" + isLoadID + "\" Creating Bin Empty Load - "
        + msMyClass + ".createBinEmptyLoad()");
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      LoadData missingLoad = mpLoadServer.getLoad(isLoadID);
      if (missingLoad == null)
      {
        // Someone deleted this load.
        throw new DBException("Load \"" + isLoadID + "\" not found!");
      }
                                   // Mark location where this occurred as
                                   // UNAVAILABLE.
      mpLocServer.setLocationStatus(missingLoad.getWarehouse(),
          missingLoad.getAddress(), missingLoad.getShelfPosition(),
          DBConstants.LCUNAVAIL, false);
                                       // Notify host if bin empty occurs.
      sendHostBinEmptyError(missingLoad.getLoadID());
      deallocationServer.deallocateMovesForLoad(missingLoad.getLoadID());
      mpInvServer.addBinEmptyItem(missingLoad.getLoadID());
      mpLoadServer.setParentLoadMoveStatus(isLoadID, DBConstants.NOMOVE,
                                           "Bin empty");
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException(e, "LoadId \"" + isLoadID
          + "\" Exception Changing Parent Load Status" + " - " + msMyClass
          + ".createBinEmptyLoad");
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Create the Bin-Full load and update the load's location.
   *
   * <P><I>Note: This method does must be called within a transaction.</I></P>
   *
   * @param ipOldLoadData - The load that was storing when the bin-full error
   *          occurred.
   * @param isDeviceID - The device for the location that had the bin-full error
   * @throws DBException
   */
  protected void createBinFullLoad(LoadData ipOldLoadData, String isDeviceID)
      throws DBException
  {
    initializeInventoryServer();
    initializeLoadServer();
    initializeLocationServer();

    // For the bin-full load, start with a clone of the existing load
    LoadData vpBinFullLoad = ipOldLoadData.clone();

    // Do NOT Clone the Tracking Id or Device Id.
    vpBinFullLoad.setBCRData("");
    vpBinFullLoad.setDeviceID(isDeviceID);

    // The bin-full load is not moving at the old load's next address
    vpBinFullLoad.setAddress(ipOldLoadData.getNextAddress());
    vpBinFullLoad.setWarehouse(ipOldLoadData.getNextWarehouse());
    vpBinFullLoad.setShelfPosition(ipOldLoadData.getNextShelfPosition());
    vpBinFullLoad.setNextAddress(null);
    vpBinFullLoad.setNextWarehouse(null);
    vpBinFullLoad.setNextShelfPosition(null);
    setLoadDataMoveStatus(vpBinFullLoad, DBConstants.NOMOVE);

    // Create the load
    mpLoadServer.createRandomLoad(BIN_FULL_PREFIX, vpBinFullLoad);

    // Add the bin-full item
    mpInvServer.addBinFullItem(vpBinFullLoad.getLoadID());

    // Update the location
    mpLocServer.setLocationEmptyFlag(vpBinFullLoad.getWarehouse(),
                                     vpBinFullLoad.getAddress(),
                                     vpBinFullLoad.getShelfPosition(),
                                     DBConstants.OCCUPIED);
    mpLocServer.setLocationStatus(vpBinFullLoad.getWarehouse(),
                                  vpBinFullLoad.getAddress(),
                                  vpBinFullLoad.getShelfPosition(),
                                  DBConstants.LCUNAVAIL, false);

    // Log the recovery
    logOperation(LogConsts.OPR_DEVICE, "LoadId \"" + vpBinFullLoad.getLoadID()
        + "\" Bin Full Load created - Location " + vpBinFullLoad.getAddress()
        + " - " + msMyClass + ".createBinFullLoadFindNewLocation()");
  }

  /**
   * Create a Bin-Full load and find a new location
   *
   * @param isLoadID
   * @param isSchedulerName
   * @return LoadEventMessage string for alternate location. If the device is
   *         offline or cannot be found for some reason, return empty string.
   */
  public String createBinFullLoadFindNewLocation(String isLoadID,
                                                 String isSchedulerName)
  {
    initializeInventoryServer();
    initializeLoadServer();
    initializeLocationServer();
    initializeStationServer();

//    logDebug(msMyClass + ".createBinFullLoadFindNewLocation() - Start");
    TransactionToken tt = null;
    LoadData vpOldLoad = null;
    String vsDeviceId = "";
    LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class,
        isSchedulerName);

    try
    {
      tt = startTransaction();

      // Create the Bin Full load
      vpOldLoad = mpLoad.getLoadData(isLoadID);
      vsDeviceId = mpLocServer.getLocationDeviceId(
          vpOldLoad.getWarehouse(), vpOldLoad.getNextAddress());
      createBinFullLoad(vpOldLoad, vsDeviceId);

      // Don't find an alternate location on a captive system
      if (mpStationServer.getStationCaptiveType(vpOldLoad.getAddress()) == DBConstants.CAPTIVE)
      {
        logDebug("New Location not needed on captive system "
            + vpOldLoad.getLoadID() + " - " + msMyClass
            + ".createBinFullLoadFindNewLocation()");
        commitTransaction(tt);
        vpOldLoad.setNextAddress(mpStationServer.getRejectStation(vpOldLoad.getAddress()));
        if (vpOldLoad.getNextAddress().trim().length() == 0)
        {
          logError(vpOldLoad.getLoadID() + ": New Location not available "
              + "on captive and no reject defined for bin-full!");
          return (vpLEDF.binFullNoNewLocation(vpOldLoad.getLoadID()));
        }
        else
        {
          logDebug("Bring to reject " + vpOldLoad.getNextAddress() + " - "
              + msMyClass + ".createBinFullLoadFindNewLocation()");
          return (vpLEDF.binFullMoveStation(vpOldLoad.getLoadID(),
              vpOldLoad.getNextAddress(), vpOldLoad.getHeight()));
        }
      }

      //
      // Find a NEW location for the Load still on  the crane.
      //
      String newLocation[] = mpLocServer.reserveAlternateLocationForDevice(
          vpOldLoad.getNextWarehouse(), vsDeviceId, vpOldLoad.getHeight(),
          vpOldLoad.getLength(), vpOldLoad.getWidth(), vpOldLoad.getRecommendedZone());
      if (newLocation == null)
      {
        logError("Problem with finding alternate location!  Make sure device is online.");
        vpOldLoad.setNextAddress("");
      }

      String vsEquipWhs =  mpLocServer.getEquipWarehouse(vpOldLoad.getWarehouse());

      String vsAltLocnCmd;
      if (mpLocServer.exists(newLocation))
      {
        vpOldLoad.setNextWarehouse(newLocation[0]);
        vpOldLoad.setNextAddress(newLocation[1]);
        if (newLocation.length == 3)
        {
          vpOldLoad.setNextShelfPosition(newLocation[2]);
        }
        mpLoadServer.updateLoadData(vpOldLoad, true);
        logDebug("New Location " + vpOldLoad.getNextAddress() + " - " + msMyClass
            + ".createBinFullLoadFindNewLocation()");
        commitTransaction(tt);
        vsAltLocnCmd = vpLEDF.binFullNewLocation(vpOldLoad.getLoadID(),
            vpOldLoad.getNextAddress(), vpOldLoad.getNextShelfPosition(),
            vsEquipWhs, vpOldLoad.getHeight());
        return(vsAltLocnCmd);
      }
      else   // Attempt to send load to reject station case.
      {
        directLoadToReject(vpOldLoad, mpStationServer.getStation(vpOldLoad.getAddress()));
        if (vpOldLoad.getNextAddress().trim().length() > 0)
        {
          mpLoadServer.updateLoadData(vpOldLoad, true);

          logDebug("Bring to reject " + vpOldLoad.getNextAddress() + " - "
              + msMyClass + ".createBinFullLoadFindNewLocation()");

          vsAltLocnCmd = vpLEDF.binFullMoveStation(vpOldLoad.getLoadID(),
              vpOldLoad.getNextAddress(), vpOldLoad.getHeight());
        }
        else
        {
          logError(vpOldLoad.getLoadID() + ": No empty locations available"
              + " and no reject defined for bin-full!");

          vsAltLocnCmd = null;
        }
        commitTransaction(tt);

        return(vsAltLocnCmd);
      }
    }
    catch (DBException e)
    {
      logException(e, "LoadId \"" + isLoadID
          + "\" Exception Creating Bin Full load and cleaning up locations"
          + " - " + msMyClass + ".createBinFullLoadFindNewLocation");
      return null;
    }
    finally
    {
      endTransaction(tt);

      if (vpOldLoad != null && vpOldLoad.getNextAddress().trim().length() == 0)
      {
        markLoadAsStuckOnCrane(vpOldLoad, vsDeviceId, DBConstants.BINFULL_ERROR);
      }
    }
  }

  /**
   * Mark a load as stuck on a crane when there is an error and no alternate
   * location and no reject station can be found. The user will have to manually
   * recover this load.
   *
   * @param ipLoadData
   * @param isDeviceID
   * @param inErrorStatus
   */
  protected void markLoadAsStuckOnCrane(LoadData ipLoadData, String isDeviceID,
      int inErrorStatus)
  {
    initializeLoadServer();
    TransactionToken vpTT = null;
    try
    {
      logError("No alternate location for " + ipLoadData.getLoadID()
          + "; please send an alternate location command or recover manually.");
      vpTT = startTransaction();
      ipLoadData.setLoadMoveStatus(inErrorStatus);
      ipLoadData.setDeviceID(isDeviceID);
      ipLoadData.setMoveDate();
      ipLoadData.setLoadMessage("On crane " + isDeviceID);
      mpLoadServer.updateLoadInfo(ipLoadData);
      commitTransaction(vpTT);
    }
    catch (DBException dbe)
    {
      logException("Error setting " + ipLoadData.getLoadID()
          + " to ERROR status.", dbe);
    }
    finally
    {
      endTransaction(vpTT);
    }
  }

  /**
   * This method is for loading a captive system with a barcode reader
   *
   * @param ipLEDF
   * @param ipStationData
   * @return LoadData of new load
   * @throws DBException
   */
  protected LoadData createCaptiveInsertLoad(LoadEventDataFormat ipLEDF,
      StationData ipStationData) throws DBException, InvalidDataException
  {
    initializeLoadServer();
    initializeLocationServer();

    String vsLoadID = ipLEDF.getBarCode();
    LoadData vpTempLoadData = mpLoadServer.getLoad(vsLoadID);
    if (vpTempLoadData != null)
    {
      throw new DBException("Load already exists \"" + vsLoadID
          + "\" Can't Create load at " + ipStationData.getStationName());
    }
    vpTempLoadData = Factory.create(LoadData.class);
    vpTempLoadData.setHeight(ipLEDF.getDimensionInfo());
    vpTempLoadData.setLoadID(vsLoadID);
    vpTempLoadData.setParentLoadID(vsLoadID);
    String[] vasAddress = mpLocServer.findLocationForStoring(ipStationData,
                                                             vpTempLoadData);
    String vsNextAddress = "";
    if (vasAddress != null)
    {
        vsNextAddress = vasAddress[1];
    }
    if (vsNextAddress.length() == 0)
    {
      throw new DBException("No next location for load \"" + vsLoadID);
    }
    LocationData vpLC = null;
    vpLC = mpLocServer.getLocationRecord(ipStationData.getWarehouse(),
                                         vsNextAddress);
    if (vpLC == null)
    {
      throw new DBException("Location " + ipStationData.getWarehouse() + "-"
          + vsNextAddress + " does not exist.");
    }

    /*
     * Create the load
     */
    vpTempLoadData.setContainerType(ipStationData.getContainerType());
    vpTempLoadData.setWarehouse(ipStationData.getWarehouse());
    vpTempLoadData.setAddress(ipStationData.getStationName());
    vpTempLoadData.setNextWarehouse(ipStationData.getWarehouse());
    vpTempLoadData.setNextAddress(vsNextAddress);
    vpTempLoadData.setFinalWarehouse(ipStationData.getWarehouse());
    vpTempLoadData.setFinalAddress(vsNextAddress);
    vpTempLoadData.setDeviceID(ipStationData.getDeviceID());
    vpTempLoadData.setAmountFull(DBConstants.EMPTY);
    vpTempLoadData.setLoadMoveStatus(DBConstants.STOREPENDING);
    mpLoadServer.addLoad(vpTempLoadData);

    return vpTempLoadData;
  }

  /**
   * No load created yet, just put on conveyor. Store not completed, so create
   * an ID Pending load.
   *
   * @param ipLEDF
   */
  public void createIDPendingLoad(LoadEventDataFormat ipLEDF)
  {
    String vsMethodInfo = msMyClass + ".createIDPendingLoad()";

    logDebug("Create ID Pending Load - " + vsMethodInfo);
    LoadData IDPendingLoad = Factory.create(LoadData.class);
    List<Object> stationDefaults = mpStationServer.getStationDefaults(ipLEDF.getSourceStation());

    IDPendingLoad.setWarehouse((String) stationDefaults.get(0));
    IDPendingLoad.setDeviceID((String) stationDefaults.get(1));
    IDPendingLoad.setContainerType((String) stationDefaults.get(2));
    IDPendingLoad.setAddress(ipLEDF.getSourceStation());
    IDPendingLoad.setHeight(ipLEDF.getDimensionInfo());
    IDPendingLoad.setBCRData(ipLEDF.getBarCode());
    IDPendingLoad.setLoadMoveStatus(DBConstants.IDPENDING);

    mpLoadServer.createRandomLoad("IP", IDPendingLoad);

    if (ipLEDF.getBarCode().trim().length() > 0)
    {
      logDebug("LoadId \"" + IDPendingLoad.getLoadID()
          + "\" with BarCode \"" + ipLEDF.getBarCode()
          + "\" at Station " + ipLEDF.getSourceStation()
          + " created as new random IdPending Load - " + vsMethodInfo);
    }
    else
    {
      logDebug("LoadId \"" + IDPendingLoad.getLoadID()
          + "\" at Station " + ipLEDF.getSourceStation()
          + " created as new random IdPending Load - " + vsMethodInfo);
    }
  }

  /**
   * Create a new load at a station using the station defaults
   *
   * @param isLoadID the Load ID.
   * @param isStationName  the station where load should be created.
   * @param izCheckStationMode <code>true</code> means validate if station mode
   *        setting allows load creation there.
   * @return boolean of <code>true</code> if successful, <code>false</code>
   *         otherwise.
   */
  public boolean createNewLoadAtStation(String isLoadID, String isStationName,
      boolean izCheckStationMode)
  {
    initializeLoadServer();
    initializeStationServer();

    logDebug("LoadId \"" + isLoadID + "\" Created at Station - " + msMyClass
        + ".createNewLoadAtStation()");
    boolean vzRtn = false;

    TransactionToken vpTok = null;
    try
    {
      if (izCheckStationMode)
      {
        /*
         * Throws exception if this station or its linked station configuration
         * is not correct to insert a load.
         */
        mpStationServer.canStationInsertALoad(isStationName);
      }
      vpTok = startTransaction();
      LoadData vpNewLoad = Factory.create(LoadData.class);
      vpNewLoad.setLoadID(isLoadID);
      vpNewLoad = setLoadToStationDefaults(vpNewLoad, isStationName);
      vpNewLoad.setLoadMoveStatus(DBConstants.ARRIVED);
      vpNewLoad.setAddress(isStationName);
      vpNewLoad.setLoadPresenceCheck(DBConstants.YES);
      vpNewLoad.setParentLoadID(isLoadID);
      mpLoadServer.addLoad(vpNewLoad);
      commitTransaction(vpTok);
      vzRtn = true;
    }
    catch (DBException e)
    {
      logException(e, "LoadId \"" + isLoadID
          + "\" Exception Changing Parent Load Status" + " - " + msMyClass
          + ".createNewLoadAtStation");
    }
    finally
    {
      endTransaction(vpTok);
    }
    return vzRtn;
  }

  /**
   * Create a random load for a duplicate load ID
   *
   * @param loadEventMessage
   * @return
   */
  protected LoadData createRandomDuplicateLoad(LoadEventDataFormat loadEventMessage)
  {
    initializeLoadServer();
    initializeStationServer();

    LoadData newLoad = Factory.create(LoadData.class);
    newLoad = setLoadToStationDefaults(newLoad, loadEventMessage.getSourceStation());
    newLoad.setBCRData(loadEventMessage.getBarCode());
    newLoad.setAddress(loadEventMessage.getSourceStation());
    if (mpStationServer.isStationAutoStore(loadEventMessage.getSourceStation()))
    {
      String vsReject = mpStationServer.getRejectStation(loadEventMessage.getSourceStation());
      if (vsReject.trim().length() > 0)
      {
        newLoad.setNextWarehouse(newLoad.getWarehouse());
        newLoad.setNextAddress(vsReject);
      }
    }
    setLoadDataMoveStatus(newLoad, DBConstants.STOREPENDING);
    newLoad.setMoveDate();
    newLoad.setHeight(loadEventMessage.getDimensionInfo());
    newLoad.setLoadMessage("Duplicate of " + loadEventMessage.getBarCode());
    mpLoadServer.createRandomLoad(SKDCConstants.DL_LOAD_PREFIX, newLoad);

    return newLoad;
  }

  /**
   * Check to see if station has a load to Store. If station is online and there
   * is not already a load that has had a command sent and we are waiting for a
   * store response ok and we have the correct number of store-pending loads
   *
   * @param stationName the station to check
   * @param numberLoadStoreAtATime the number of store-pending loads we need
   * @return List of <code>LoadData</code> if store pending loads or null
   */
  protected List<LoadData> doesLoadNeedStoring(String stationName,
      int numberLoadStoreAtATime)
  {
    if (isStationWaitingForStoreOkResponse(stationName,numberLoadStoreAtATime) == false)
    {
      List<LoadData> storePendingLoads = getStorePendingLoads(stationName);
      if(storePendingLoads.isEmpty())
      {
        return null;
      }
      else
        if(storePendingLoads.size() < numberLoadStoreAtATime)
        {
          logDebug("Station " + stationName + " Only "
              + storePendingLoads.size() + " store pending loads - We want "
              + numberLoadStoreAtATime + " - " + msMyClass
              + ".doesStationHaveLoadToStore()");
          return null;
        }
        else
        {
          for(int i = 0; i<numberLoadStoreAtATime; i++)
          {
            if(storePendingLoads.get(i) == null)
            {
              return null;
            }

          }
          return storePendingLoads;
        }
    }
    // Station is waiting for response ok from last command or offline
    return null;
  }

  /**
   * Do preliminary retrieve checks.  This method is provided to ease
   * extensibility for double-deep.  If a child-class implementation returns
   * false, it is responsible for logging the reason why it did so.
   *
   * @param ipLD - The load to be retrieved
   * @param ipSD - The destination station for the retrieval
   * @param isSchedulerName - The scheduler responsible for the retrieve
   * @param ipMoveCommands - Add any preliminary commands to this list
   * @return true if we can retrieve the load, false otherwise.
   */
  protected boolean doPreliminaryRetrieveChecks(LoadData ipLD, StationData ipSD,
      String isSchedulerName, List<LoadEventDataFormat> ipMoveCommands)
  {
    return true;
  }

  /**
   * Find a new location when we get a height mismatch error from the crane.
   *
   * @param loadID
   * @param schedulerName
   * @param newHeight
   * @return
   */
  public String findNewLocationForHeightMismatch(String loadID,
      String schedulerName, int newHeight)
  {
    initializeInventoryServer();
    initializeLoadServer();
    initializeLocationServer();
    initializeStationServer();

//    logDebug(msMyClass + ".findNewLocationForHeightMismatch() - Start");
    LoadData wrongHeightLoad = null;
    String vsDeviceId = "";
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      wrongHeightLoad = mpLoadServer.getLoad(loadID);
      //
      // Get the Device Id for the Location.
      //
      vsDeviceId = mpLocServer.getLocationDeviceId(
          wrongHeightLoad.getWarehouse(), wrongHeightLoad.getNextAddress());
      wrongHeightLoad.setDeviceID(vsDeviceId);
      // Set the first location of wrong height to available from reserved.
      mpLocServer.setLocationEmptyFlag(wrongHeightLoad.getNextWarehouse(),
          wrongHeightLoad.getNextAddress(), wrongHeightLoad.getNextShelfPosition(), DBConstants.UNOCCUPIED);
      logOperation(LogConsts.OPR_DEVICE, "LoadId \""
          + wrongHeightLoad.getLoadID()
          + "\" Changed first location to avail - Location "
          + wrongHeightLoad.getNextWarehouse() + "-"
          + wrongHeightLoad.getNextAddress() + " - " + msMyClass
          + ".findNewLocationForHeightMismatch()");

      String vsMisMatchCmd = null;
      LoadEventDataFormat loadEventMessage = Factory.create(LoadEventDataFormat.class, schedulerName);

      // Don't look for an alternate location on a captive system
      if (mpStationServer.getStationCaptiveType(wrongHeightLoad.getAddress()) == DBConstants.CAPTIVE)
      {
        logDebug("New Location not needed on captive system "
            + wrongHeightLoad.getLoadID() + " - " + msMyClass
            + ".findNewLocationForHeightMismatch()");
        wrongHeightLoad.setNextAddress(mpStationServer.getRejectStation(wrongHeightLoad.getAddress()));
//        wrongHeightLoad.setLoadMoveStatus(DBConstants.SIZE_ERROR);
//        mpLoadServer.updateLoadData(wrongHeightLoad, true);
        commitTransaction(tt);

        if (wrongHeightLoad.getNextAddress().trim().length() > 0)
        {
          vsMisMatchCmd = loadEventMessage.binDimensionMismatchMoveStation(
              wrongHeightLoad.getLoadID(), wrongHeightLoad.getNextAddress(),
              wrongHeightLoad.getHeight());
        }
        return vsMisMatchCmd;
      }

      //
      // Find a NEW location for the Load still on the crane with correct height.
      //
      int vnOldHeight = wrongHeightLoad.getHeight();
      wrongHeightLoad.setHeight(newHeight);
      if (Application.getBoolean("AddItemForHeightMismatch", false))
      {
        mpInvServer.addBinHeightItem(wrongHeightLoad.getLoadID());
      }
      String vsAltLocation[] = mpLocServer.reserveAlternateLocationForDevice(
          wrongHeightLoad.getNextWarehouse(), vsDeviceId,
          wrongHeightLoad.getHeight(), wrongHeightLoad.getLength(),
          wrongHeightLoad.getWidth(), wrongHeightLoad.getRecommendedZone());
      wrongHeightLoad.setLoadMessage("Size mismatch on " + vsDeviceId +
          " (" + vnOldHeight + " -> " + newHeight + ")");

      if (mpLocServer.exists(vsAltLocation))
      {
        wrongHeightLoad.setNextWarehouse(vsAltLocation[0]);
        wrongHeightLoad.setNextAddress(vsAltLocation[1]);
        mpLoadServer.updateLoadData(wrongHeightLoad, true);
        logDebug("New Location " + wrongHeightLoad.getNextAddress() + " - "
            + msMyClass + ".findNewLocationForHeightMismatch()");
        commitTransaction(tt);

        vsMisMatchCmd = loadEventMessage.binDimensionMismatchNewLocation(
            wrongHeightLoad.getLoadID(), wrongHeightLoad.getNextAddress(),
            wrongHeightLoad.getNextShelfPosition(), wrongHeightLoad.getHeight());
        return(vsMisMatchCmd);
      }
      else
      {
        directLoadToReject(wrongHeightLoad, mpStationServer.getStation(wrongHeightLoad.getAddress()));
        if (wrongHeightLoad.getNextAddress().trim().length() > 0)
        {
          mpLoadServer.updateLoadData(wrongHeightLoad, true);

          logDebug("Bring to reject " + wrongHeightLoad.getNextAddress() + " - "
              + msMyClass + ".createBinFullLoadFindNewLocation()");

          vsMisMatchCmd = loadEventMessage.binDimensionMismatchMoveStation(
              wrongHeightLoad.getLoadID(), wrongHeightLoad.getNextAddress(),
              wrongHeightLoad.getHeight());
        }
        else
        {
          wrongHeightLoad.setNextWarehouse("");
          wrongHeightLoad.setNextAddress("");
          mpLoadServer.updateLoadData(wrongHeightLoad, true);

          logError(wrongHeightLoad.getLoadID() + ": No empty locations available"
              + " and no reject defined for bin-full!");

          vsMisMatchCmd = null;
        }
        commitTransaction(tt);

        return vsMisMatchCmd;
      }
    }
    catch (DBException e)
    {
      logException(e, "LoadId \"" + loadID
          + "\" Exception Creating Bin Full load and cleaning up locations"
          + " - " + msMyClass + ".findNewLocationForHeightMismatch");
      return null;
    }
    finally
    {
      endTransaction(tt);

      if (wrongHeightLoad != null &&
          wrongHeightLoad.getNextAddress().trim().length() == 0)
      {
        markLoadAsStuckOnCrane(wrongHeightLoad, vsDeviceId,
            DBConstants.SIZE_ERROR);
      }
    }
  }

  /**
   * Method sends an Operation complete load event from the MC
   * @param isLoadID
   * @param isSourceStation
   * @param isSchedulerName
   * @return
   */
  public String SendMCOperationComplete(String isLoadID, String isSourceStation,
      String isSchedulerName)
  {
    initializeLoadServer();
    LoadData vpLoadData = null;
    String vsMessage = "";
    if (mpLoad.exists(isLoadID))
    {
      vpLoadData = mpLoadServer.getLoad(isLoadID);
      if(vpLoadData.getAddress().equals(isSourceStation))
      {
        LoadEventDataFormat vpLEM = Factory.create(LoadEventDataFormat.class, isSchedulerName);
        vsMessage =  vpLEM.screenOperationComplete(isLoadID, isSourceStation);
      }
      else
      {
        String vsErrorMessage = "Load: " + isLoadID + " is not at Station: " +
          isSourceStation + " actual location " + vpLoadData.getAddress();
        logError(vsErrorMessage);
      }
    }
    else
    {
      String vsErrorMessage = "Load: " + isLoadID + " does not exist in system." +
        " SchedulerServer.sendMCOperationComplete()";
      logError(vsErrorMessage);
    }
    return vsMessage;
  }

  /**
   * Find an alternate location
   *
   * <p><b>Details:</b> This method searches for a alternate location in
   * response to a ID28 (Request alternate location) message from the AGC.
   * It always looks for a storage location.  If a location can not be found
   * it send '0' padded location.  This will result in a Impossible Location
   * response to the AGC</p>
   *
   * <p><B>NOTE: Right now, this doesn't do much of anything.</B></p>
   *
   * @param isLoadID the load we are processing
   * @param isShedulerName the Scheduler that is handling request
   * @param isInfo the AGC Data that is passed back to AGC in message
   */
  public String findRejectLocation(String isLoadId, String isSchedulerName,
      String isInfo)
  {
    LoadEventDataFormat vpLEM = Factory.create(LoadEventDataFormat.class, isSchedulerName);
    return vpLEM.rejectLoad(isLoadId, "", "", isInfo);
  }

  /**
   * Get the load data for the auto store load (create if necessary)
   *
   * @param isLoadId
   * @param ipLEDF
   * @return LoadData if all is well, null if we shouldn't store
   */
  protected LoadData getAutoStoreLoad(String isLoadId,
                                      LoadEventDataFormat ipLEDF)
  {
    LoadData vpLoadData = null;
    String vsSourceStation = ipLEDF.getSourceStation();

    if (mpLoad.exists(isLoadId))
    {
      logDebug("LoadId \"" + isLoadId + "\" exists for expected receipt - "
          + msMyClass + ".autoStoreLoad()");
      vpLoadData = mpLoadServer.getLoad(isLoadId);
      if (vpLoadData.getAddress().equals(vsSourceStation))
      {
        /*
         * In this case, we'll re-use the existing load to hopefully avoid
         * storing one-off in the case of an operator pulling back a load and
         * then trying to re-store it.  If there really are two loads with the
         * same barcode, then the duplicate data error from the second store
         * command should keep it from storing.
         */
        logError("Duplicate barcode \"" + isLoadId + "\" at station "
            + vsSourceStation + " -- resending Store command");
        setLoadDataMoveStatus(vpLoadData, DBConstants.ARRIVEPENDING);
        vpLoadData.setHeight(ipLEDF.getDimensionInfo());
        mpLoadServer.updateLoadData(vpLoadData, true);
      }
      else
      {   // already have this load at another address
        vpLoadData = createRandomDuplicateLoad(ipLEDF);
        logError("Load \"" + isLoadId
            + "\" already exists - Creating new temporary Load \""
            + vpLoadData.getLoadID() + "\"");
      }
    }
    else if (mpStationServer.isStationAutoStoreERLoad(vsSourceStation) &&
             !mpPOServer.expectedLoadExists(isLoadId))
    {         // If this is an auto-store E.R. Load station, an E.R. with this
              // load better exist. If not load stays ID Pending.
      return null;
    }
    else if (mpStationServer.isStationAutoStoreWithER(vsSourceStation) &&
             !mpPOServer.exists(isLoadId))
    {         // If this is an Auto-Store E.R station, an E.R. ID. matching
              // this Load ID. better exist. If not load stays ID Pending.
      return null;
    }
    else
    {
      //
      // This is a NEW Load entering the system.  Add the Load using its barcode
      // as the LoadId.
      //
      vpLoadData = createNewLoad(isLoadId, vsSourceStation, ipLEDF);
    }
    return vpLoadData;
  }

  /**
   * Create a load for a store arrival with a valid barcode
   * 
   * @param isLoadId
   * @param isSourceStation
   * @param ipLEDF
   * @return
   */
  protected LoadData createNewLoad(String isLoadId, String isSourceStation,
      LoadEventDataFormat ipLEDF)
  {
    logOperation(LogConsts.OPR_DSVR,
        "LoadId \"" + isLoadId + "\" Created from BarCode");
    LoadData vpLoadData = Factory.create(LoadData.class);
    vpLoadData = setLoadToStationDefaults(vpLoadData, isSourceStation);
    vpLoadData.setParentLoadID(isLoadId);
    vpLoadData.setLoadID(isLoadId);
    if (ipLEDF.isBCRValid())
    {
      vpLoadData.setBCRData(ipLEDF.getBarCode());
    }
    vpLoadData.setAddress(isSourceStation);
    setLoadDataMoveStatus(vpLoadData, DBConstants.ARRIVEPENDING);
    vpLoadData.setHeight(ipLEDF.getDimensionInfo());

    try
    {
      mpLoadServer.addLoad(vpLoadData);
    }
    catch (DBException dbe)
    {
      logException("Adding load", dbe);
      return null;
    }
    return vpLoadData;
  }
  
  /**
   * See how many loads are enroute to station retrieve sent + retrieving +
   * moving loads + arrived at the station
   *
   * @param destination station to check
   * @return int number of loads enroute.
   */
  public int getEnrouteLoadCount(String destination)
  {
    initializeStationServer();

    int currentEnroute = 0;
    try
    {
      currentEnroute = mpLoad.getEnrouteCountPlusAtStation(destination);
      logDebug("Enroute and at station " + destination + " Count: "
          + currentEnroute + " - " + msMyClass + ".getEnrouteLoadCount()");
    }
    catch (Exception e)
    {
      logException(e, "Exception getting enroute loads for station "
          + destination + " - " + msMyClass + ".getEnrouteLoadCount");
    }
    return currentEnroute;
  }

  /**
   * Get the number of loads at or enroute to a destination
   *
   * @param isWarehouse
   * @param isAddress
   * @return
   */
  public int getLoadCountInRouteOrAtDestination(String isWarehouse,
      String isAddress)
  {
    int count = 0;
    LoadData loadData = Factory.create(LoadData.class);
    loadData.setKey(LoadData.NEXTWAREHOUSE_NAME, isWarehouse);
    loadData.setKey(LoadData.NEXTADDRESS_NAME, isAddress);
    try
    {
      count = mpLoad.getCount(loadData);
      loadData.clearKeys();
      loadData.setKey(LoadData.WAREHOUSE_NAME, isWarehouse);
      loadData.setKey(LoadData.ADDRESS_NAME, isAddress);
      count += mpLoad.getCount(loadData);
    }
    catch (Exception e)
    {
      logException(e, "Exception getting load count " + isAddress + " - "
          + msMyClass + ".getLoadCountInRouteOrAtDestination(" + isWarehouse
          + "-" + isAddress + ")");
    }
    return count;
  }

  /**
   * Get the loads for an AGC Transfer Station
   * @throws DBException
   */
  public String[] getLoadsForAGCTransferStation(String isStation)
      throws DBException
  {
    TableJoin vpTJ = Factory.create(TableJoin.class);

    return vpTJ.getLoadsForAGCTransferStation(isStation);
  }

  /**
   * Get a load for a valid BCR in a store arrival
   *
   * @param ipLEDF
   * @param ipStationData
   * @return LoadData, or null if there is nothing else for the caller to do
   * @throws DBException
   * @throws InvalidDataException if an autostore load is not at the store station.
   */
  protected LoadData getLoadWithValidBCR(LoadEventDataFormat ipLEDF,
      StationData ipStationData) throws DBException, InvalidDataException
  {
    String vsMethodInfo = msMyClass + ".getLoadWithValidBCR()";

    LoadData vpLoadData = null;

    // Have BCR
    if (ipLEDF.isBCRGoodRead())
    {
      logDebug("BarCode \"" + ipLEDF.getBarCode()
          + "\" Read at Station " + ipLEDF.getSourceStation()
          + " - " + vsMethodInfo);

      // For Captive Insert, try to create a new load with barcode=location
      if (ipStationData.getCaptive() == DBConstants.CAPTIVE
          && ipStationData.getStatus() == DBConstants.CAPTIVEINSERT)
      {
        try
        {
          vpLoadData = createCaptiveInsertLoad(ipLEDF, ipStationData);
        }
        catch (DBException dbe)
        {
          logException(dbe, vsMethodInfo);
          vpLoadData = mpLoadServer.getParentLoad(ipLEDF.getBarCode());
        }
      }
      // For Auto-store, do it
      else if (mpStationServer.isStationAutoStore(ipLEDF.getSourceStation()))
      {
        logDebug("Station Autostore - " + vsMethodInfo);
        vpLoadData = autoStoreLoad(ipLEDF);
      }
      // Not Auto-store so just get the LoadData of BCR (BCR=Load ID)
      else
      {
        vpLoadData = mpLoadServer.getParentLoad(ipLEDF.getBarCode());
      }
    }
    else
    {
      // create load and send it to reject station
      vpLoadData = createBadBCRLoad(ipLEDF);
      logError("Barcode \"" + ipLEDF.getBarCode()
          + "\" Bad/No Read -  Send to reject " + vpLoadData.getNextAddress()
          + " - " + vsMethodInfo);
    }
    return vpLoadData;
  }

  /**
   * Get a load without a BCR for a store arrival
   *
   * @param ipLEDF
   * @param ipStationData
   * @return LoadData, or null if there is nothing else for the caller to do
   * @throws DBException
   * @throws InvalidDataException if an autostore load is not at the store station.
   */
  protected LoadData getLoadWithoutBCR(LoadEventDataFormat ipLEDF,
      StationData ipStationData) throws DBException, InvalidDataException
  {
    String vsMethodInfo = msMyClass + ".getLoadWithoutBCR()";
    logDebug("BarCode field zeros or blanks - Get oldest load - "
        + vsMethodInfo);

    LoadData vpLoadData = null;
    if (mpStationServer.isStationAutoStore(ipLEDF.getSourceStation()))
    {
      // For AutoStore stations that create a load using the station Load
      // Prefix
      if (mpStationServer.isStationAutoStoreBCRAsItem(ipLEDF.getSourceStation())
          || mpStationServer.isStationAutoStoreItem(ipLEDF.getSourceStation())
          || mpStationServer.isStationAutoStoreLoad(ipLEDF.getSourceStation()))
      {
        logDebug("Station Autostore - " + vsMethodInfo);
        vpLoadData = autoStoreLoad(ipLEDF);
      }
      else
      {
        // TODO: This should be a different exception
        throw new DBException("Station Autostore - No Bar Code Error");
      }
    }
    else
    {
      vpLoadData = mpLoadServer.getOldestLoadData(ipLEDF.getSourceStation(),
          DBConstants.ARRIVEPENDING);
    }
    return vpLoadData;
  }

  /**
   * Find and return Retrieve Pending loads awaiting a location-to-location
   * move. If any exist, it is likely caused by recovery on a double-deep
   * system, but it may come up if we have to do recovery on a system where we
   * agree to shuffle loads.
   *
   * @param isScheduler
   * @return
   * @throws DBException
   */
  public String[] getLocToLocRetrievePendingLoads(String isScheduler)
      throws DBException
  {
    return mpLoad.getLocToLocRetrievePendingLoads(isScheduler);
  }

  /**
   * Get the location-to-location retrieve command for a load.
   *
   * @param isLoadID
   * @return
   * @throws DBException
   */
  public LoadEventDataFormat getLocToLocRetrieve(String isLoadID)
      throws DBException
  {
    initializeLoadServer();
    initializeLocationServer();

    LoadEventDataFormat vpLocToLocMessage = null;
    TransactionToken vpTT = null;

    try
    {
      vpTT = startTransaction();

      LoadData vpLD = mpLoadServer.getLoad(isLoadID, true);
      if (vpLD == null)
      {
        return null;
      }
      if (vpLD.getLoadMoveStatus() != DBConstants.RETRIEVEPENDING)
      {
        return null;
      }
      if (vpLD.getNextAddress().length() < DBInfo.getFieldLength(LocationData.ADDRESS_NAME))
      {
        return null;
      }

      String vsEquipWhs = mpLocServer.getEquipWarehouse(vpLD.getWarehouse());
      vpLocToLocMessage = Factory.create(LoadEventDataFormat.class, isLoadID);
      vpLocToLocMessage.moveLoadLocationLocation(vpLD.getParentLoadID(),
                                                 vpLD.getAddress(),
                                                 vpLD.getShelfPosition(),
                                                 vsEquipWhs,
                                                 vpLD.getNextAddress(),
                                                 vpLD.getNextShelfPosition(),
                                                 vsEquipWhs, vpLD.getHeight());

      vpLD.setLoadMoveStatus(DBConstants.RETRIEVESENT);
      mpLoad.updateLoadInfo(vpLD);
      commitTransaction(vpTT);
    }
    finally
    {
      endTransaction(vpTT);
    }

    return(vpLocToLocMessage);
  }

  /**
   * Get the next route location for a load.
   *
   * <p>
   * <b>Details:</b> This method gets the next warehouse and next address for
   * the load data passed in and set the properties accordingly. This
   * information is determined from the route data and the loads current
   * station's route.
   *
   * <p>
   * If the current load station's route is not defined not next route data will
   * be set.
   * </p>
   *
   * @param ipLoadData The load data to work on
   */
  protected void getNextRouteLocation(StationData ipStationData,
      LoadData ipLoadData) throws DBException
  {
    StandardRouteServer routeServer = Factory.create(StandardRouteServer.class);

    RouteData vpRouteData = routeServer.getNextRouteData(
        ipStationData.getDefaultRoute(), ipLoadData.getAddress());

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
   * Get the retrieve pending loads for a station and scheduler.
   * It is assumed that it has already been asserted that this scheduler schedules
   * for the station.
   *
   * @param ipSTData object containing station data.
   * @param isScheduler The scheduler responsible for sending retrieve commands.
   */
  @SuppressWarnings("rawtypes")
  protected List<LoadData> getRetrievePendingLoads(StationData ipSTData,
      String isScheduler)
  {
    List<LoadData> vpRetrievePendingLoads = new ArrayList<LoadData>();
    List<String> vpRetrievePendingLoadIdList = new ArrayList<String>();
    try
    {
      List<Map> vpAllRetrievePendLoads = null;

      vpAllRetrievePendLoads = mpLoad.getRetrievePendingLoads(ipSTData.getStationName(),
          isScheduler);
      logDebug("Station " + ipSTData.getStationName() + " has up to "
          + vpAllRetrievePendLoads.size()
          + " Retrieve Pending Loads - getRetrievePendingLoads()"
          + " (list may contain duplicates)");

      /*
       * Make sure the loads can make it all the way to the final destination
       */
      initializeMoveServer();
      initializeRouteServer();
      for (Map m : vpAllRetrievePendLoads)
      {
        String vsRouteID = m.remove("MOVEROUTE").toString();
        if (mpRouteServer.checkPath(vsRouteID, ipSTData.getStationName(), vsRouteID))
        {
          LoadData vpLD = Factory.create(LoadData.class);
          vpLD.dataToSKDCData(m);
            // skip the load if it is already on the list
          String vsLoadId = vpLD.getLoadID();
          if (vpRetrievePendingLoadIdList.isEmpty() == false &&
              vpRetrievePendingLoadIdList.contains(vsLoadId))
          {
            continue;
          }
            // add the load to the list
          vpRetrievePendingLoadIdList.add(vsLoadId);
          vpRetrievePendingLoads.add(vpLD);
        }
      }
    }
    catch (Exception e)
    {
      logException(e, "Exception Getting Retrieve Pending Loads for "
          + ipSTData.getStationName() + " - " + msMyClass + ".getRetrievePendingLoads()");
    }
    if (vpRetrievePendingLoads.isEmpty())
    {
      return null;
    }
    else
    {
      return vpRetrievePendingLoads;
    }
  }

  /**
   * Get Store Pending loads at a station
   * @param stationName
   * @return
   */
  @SuppressWarnings("rawtypes")
  public List<LoadData> getStorePendingLoads(String stationName)
  {
    List<LoadData> vpStorePendLoads = null;
    try
    {
      List<Map> vpLoadList = mpLoad.getStorePendingLoads(stationName);
      vpStorePendLoads = DBHelper.convertData(vpLoadList, LoadData.class);
    }
    catch(Exception e)
    {
      logException(e, "Exception Getting Store Pending Loads for station "
          + stationName + " - " + msMyClass + ".getStorePendingLoads");
    }
    return vpStorePendLoads;
  }

  /**
   * Method exists for extensibility.  Handles auto-store ER. Load behavior.
   * @param isStoreLoad the load to auto-store.
   * @param isStoreStn the station from which load is being stored.
   * @return LoadData object representing load that is being stored.
   * @throws InvalidDataException if the load being stored is not at the current
   *         station (so the caller has passed us bad data).
   */
  public LoadData handleAutoStoreERLoad(String isStoreLoad, String isStoreStn)
            throws InvalidDataException
  {
    initializeLoadServer();
    initializePOReceivingServer();

    if (!mpLoadServer.isLoadAtStation(isStoreLoad, isStoreStn))
      throw new InvalidDataException("Load " + isStoreLoad + " is not at " +
                                     "expected station " + isStoreStn);
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      mpLoadServer.setParentLoadMoveStatus(isStoreLoad, DBConstants.STOREPENDING,
                                           null);

      mpPOServer.deleteExpectedLoadLine(isStoreStn, isStoreLoad);
      commitTransaction(vpTok);
    }
    catch(DBException exc)
    {                                  // This will only be due to database
                                       // errors.
      logException(exc, "Error finding Expected Receipt Line for Load "
          + isStoreLoad + " at " + isStoreStn);
    }
    finally
    {
      endTransaction(vpTok);
    }
    return mpLoadServer.getLoad(isStoreLoad);
  }

  /**
   * Auto-Store Load
   *
   * @param ipLD
   * @return
   */
  protected LoadData handleAutoStoreLoad(LoadData ipLD)
  {
    initializeLoadServer();
    initializePOReceivingServer();

    ipLD.setLoadMoveStatus(DBConstants.STOREPENDING);
    ipLD.setNextWarehouse(null);
    ipLD.setNextAddress(null);
    LoadLineItemData vpLLIData = Factory.create(LoadLineItemData.class);
    vpLLIData.setLoadID(ipLD.getLoadID());
    vpLLIData.setCurrentQuantity(1);
    try
    {
      if (mzHasHostSystem)
      {
        initializeHostServer();
        mpHostServer.sendStoreComplete("", ipLD.getAddress(), vpLLIData);
      }
      mpLoadServer.updateLoadInfo(ipLD);
    }
    catch(DBException exc)
    {
      logException("Auto-Store Load Receive Complete Error.", exc);
    }

    return ipLD;
  }

  /**
   * Auto-Store with BCR as Item ID
   *
   * @param ipLEDF the event
   * @param ipLD the load to store
   * @return LoadData of load to store
   */
  protected LoadData handleAutoStoreWithBCRAsItem(LoadEventDataFormat ipLEDF,
      LoadData ipLD)
  {
    initializeHostServer();

    if (ipLEDF.isBCRValid() && ipLEDF.isBCRGoodRead())
    {
      String vsItem = ipLEDF.getFullBarCode();

      double vdReceiveQty = mpStationServer.getStationOrderQty(ipLEDF.getSourceStation());
      if (vdReceiveQty == 0)
      {
        vdReceiveQty = 1;
      }

      // Add the item detail, and set the load to Store Pending.
      mpInvServer.addItemToLoad(ipLD.getLoadID(), vsItem, vdReceiveQty, true);

      try
      {
      	if(mzHasHostSystem)
      	{
          LoadLineItemData iddata = Factory.create(LoadLineItemData.class);
          iddata.setItem(vsItem);
          iddata.setLoadID(ipLD.getLoadID());
          iddata.setCurrentQuantity(vdReceiveQty);
          mpHostServer.sendStoreComplete("", ipLEDF.getSourceStation(), iddata);
      	}
        ipLD.setLoadMoveStatus(DBConstants.STOREPENDING);
        mpLoadServer.updateLoadInfo(ipLD);
      }
      catch(DBException exc)
      {
        logException("Auto-Store Item Receive Complete Error.", exc);
      }
    }

    return ipLD;
  }

  /**
   * Method to handle full receipt of a load at an auto-store station. This load
   * is assumed to have an accompanying Expected Receipt (ER). If no ER is
   * present, the load is stored with a Temporary Item.
   *
   * @param isStoreLoadID the load to auto-store.
   * @return LoadData of the storing load
   */
  protected LoadData handleAutoStoreWithER(String isStoreLoadID)
  {
    initializeLoadServer();
    initializePOReceivingServer();

    if (!mpPOServer.receiveEntirePO(isStoreLoadID))
    {
      if (Application.getBoolean("TemporaryStore", true))
      {
         initializeInventoryServer();

         mpInvServer.addTemporaryItem(isStoreLoadID);
         mpLoadServer.setParentLoadMoveStatus(isStoreLoadID,
            DBConstants.STOREPENDING, null);
      }
      else
      {
        mpLoadServer.setParentLoadMoveStatus(isStoreLoadID,
            DBConstants.IDPENDING, null);
      }
    }
    return mpLoadServer.getLoad(isStoreLoadID);
  }

  /**
   * Method to Auto Store a load and add an Item to it.
   *
   * @param isStoreLoadID the load to auto-store.
   * @param isStoreStation the auto-store station.
   * @param ipLData the load to store
   * @return LoadData of the load to store
   */
  protected LoadData handleAutoStoreWithItem(String isStoreLoadID,
      String isStoreStation, LoadData ipLData)
  {
    initializeInventoryServer();
    initializeLoadServer();
    initializePOReceivingServer();

    int vnLoadMoveStatus = DBConstants.STOREPENDING;

    String vsItem = mpStationServer.getStationItem(isStoreStation);
    double vdReceiveQty = mpStationServer.getStationOrderQty(isStoreStation);

    // Add the item detail, and set the load to Store Pending.
    mpInvServer.addItemToLoad(isStoreLoadID, vsItem, vdReceiveQty, false);

    LoadLineItemData vpIDData = Factory.create(LoadLineItemData.class);
    vpIDData.setItem(vsItem);
    vpIDData.setLoadID(isStoreLoadID);
    vpIDData.setCurrentQuantity(vdReceiveQty);
    try
    {
      if (mzHasHostSystem)
      {
        initializeHostServer();
        mpHostServer.sendStoreComplete("", isStoreStation, vpIDData);
      }
    }
    catch(DBException exc)
    {
      logException("Auto-Store Item Receive Complete Error.", exc);
      vnLoadMoveStatus = DBConstants.STOREERROR;
    }

    try
    {
      ipLData.setAmountFull(ipLData.getAmountFullnessTrans());
      ipLData.setLoadMoveStatus(vnLoadMoveStatus);
      mpLoadServer.updateLoadInfo(ipLData);
    }
    catch (DBException dbe)
    {
      logException("Auto-Store Item Receive Complete Error.", dbe);
    }

    return ipLData;
  }

  /**
   * Check to see if there already has been a retrieve command sent and we are
   * waiting for a response of OK before we send another. The load status will
   * be RetrieveSent and numberAllowed is the number of Retrieve-sent loads you
   * can have per station.
   *
   * @param stationName Station to see if waiting for a retrieve response
   * @param numberAllowed number of retrieve sents that there should be.
   * @return True if there are less retrievesent than the numberAllowed
   */
  protected boolean isStationWaitingForRetrieveOkResponse(
      String destinationStation, int iiMaxRetrieveSendsAllowed)
  {
    int count = mpLoad.getRetrievesSentLoadCount(destinationStation);
    return (count >= iiMaxRetrieveSendsAllowed);
  }

  /**
   * Check to see if there already has been a store command sent and we are
   * waiting for a response of OK before we send another. The load status will
   * be storeSent and numberAllowed is the number of store sents you can have
   * per station.
   *
   * @param stationName Station to see if waiting for a store response
   * @param numberAllowed number of store sents that there should be.
   * @return True if there are less storesent than the numberAllowed
   */
  protected boolean isStationWaitingForStoreOkResponse(String stationName,
      int numberAllowed)
  {
    int count = 0;
    try
    {
      count = mpLoad.getStoreWaitingLoadCount(stationName);
    }
    catch (Exception e)
    {
      logException(e, "Exception getting load count for station " + stationName
          + " - " + msMyClass + ".isStationWaitingForStoreOkResponse");
    }
    if(count < numberAllowed)
    {
      return false;
    }
    else
    {
      return true;
    }
  }

  /**
   * Join an arrival pending with an ID pending load at a station
   * @param isInputStation the input station that has the id pending load.
   *
   * @return <code>true</code> if successful.
   */
  public boolean joinLoads(String isInputStation) throws DBException
  {
    initializeLoadServer();
    initializeStationServer();

    boolean vzRtn = false;
    String vsStationWarhse = mpStationServer.getStationWarehouse(isInputStation);
    LoadData IDPendingLoad = mpLoadServer.getOldestLoadData(vsStationWarhse,
        isInputStation, DBConstants.IDPENDING);
    if (IDPendingLoad == null)
    {
      // no IDPending loads at this station so wait for arrival from equipment
      logDebug(msMyClass + ".joinLoads() - No IdPending Load");
    }
    else
    {    // See if we have a Bar Code
      if (IDPendingLoad.isBCRValidLoadID())
      {
        /*
         * The BCR of the IDPending load is not zero's or blank must have
         * matching load
         */
        logDebug("LoadId \"" + IDPendingLoad.getBCRData() + "\" Barcode - "
            + msMyClass + ".joinLoads()");
        // Get load that matches BCR
        LoadData loadData = mpLoadServer.getLoad(IDPendingLoad.getBCRData());
        if (loadData == null)
        {
          // Sometimes the MC Key may not match the Load ID
          loadData = mpLoadServer.getLoad(getLoadIdFromTrackingId(IDPendingLoad.getBCRData()));
          if (loadData == null)
          {
            // No load created yet return
            logError("LoadId \"" + IDPendingLoad.getBCRData()
                + "\"  Barcode - NO IdPending Barcode Match");
          }
        }
        if (loadData != null)
        {
          /*
           * Have a load that matches oldest IDPending Load see if it has been
           * released.
           */
          logOperation(LogConsts.OPR_DEVICE, "LoadId \"" + loadData.getLoadID()
              + "\" Found - Delete IdPending LoadId \""
              + IDPendingLoad.getLoadID() + "\" - " + msMyClass
              + ".joinLoads()");
          vzRtn = changeLoadToStorePending(loadData, IDPendingLoad);
        }
      }
      else
      { // There must not be a BCR reader
        LoadData loadData = mpLoadServer.getOldestLoadData(vsStationWarhse,
            isInputStation, DBConstants.ARRIVEPENDING);
        if( loadData == null)
        {
          /*
           * Should never happen if screen tells me to move load should have
           * arrive pending load
           */
          logDebug("General move load message with no arrive pending load - "
              + msMyClass + ".joinLoads()");
        }
        else
        {
          vzRtn = changeLoadToStorePending(loadData, IDPendingLoad);
        }
      }
    }

    return vzRtn;
  }

  /**
   * Marry-up an ID-pending load and an Arrival-Pending load
   *
   * @param loadData
   * @param idPend
   * @throws DBException
   * @return
   */
  protected boolean changeLoadToStorePending(LoadData loadData, LoadData idPend)
      throws DBException
  {
    initializeLoadServer();
    initializeInventoryServer();
    if(idPend != null)
    {
      if (idPend.getHeight() != loadData.getHeight())
      {
        loadData.setHeight(idPend.getHeight());
        logDebug("Arrived Load height is different - " + msMyClass
            + ".processStoreArrival()");
      }
      if (idPend.getLoadPresenceCheck() != loadData.getLoadPresenceCheck())
      {
        logDebug("Arrived Load Presence is different - " + msMyClass
            + ".processStoreArrival()");
      }
      // delete IDpending load
      mpInvServer.deleteLoad(idPend.getLoadID(),
          ReasonCode.getDaifukuReasonCode());
    }
    setLoadDataMoveStatus(loadData, DBConstants.STOREPENDING);
    mpLoadServer.updateLoadData(loadData, true);
    return true;
  }

  /**
   * Load is now at the next location but moving there--no arrival yet. Set
   * location of load to destination station and status to moving. Keep next
   * address = to station. If captive change location empty flag to reserved
   * else if semi- or non-captive change to empty.
   *
   * @param parentLoadID value of the parentLoadid
   * @param locationEmptyFlag value of the locationEmptyFlag
   * @param moveStatus value of the moveStatus
   */
  public void moveLoadForRetrieve(String parentLoadID, String isNextStn,
      int locationEmptyFlag, int moveStatus)
  {
    boolean vzCaptiveSystem = false;

    initializeInventoryServer();
    initializeLoadServer();
    initializeLocationServer();
    initializeMoveServer();
    initializeRouteServer();
    initializeStationServer();

    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      LoadData loadData = mpLoadServer.getParentLoad(parentLoadID);

      if (loadData != null)
      {
        // make sure the next address is set correctly
        if (!loadData.getNextAddress().equals(isNextStn))
        {
          String vsWarehouse = mpStationServer.getStationWarehouse(isNextStn);
          loadData.setNextWarehouse(vsWarehouse);
          loadData.setNextAddress(isNextStn);
          mpLoadServer.updateLoadData(loadData, true);
        }

        vzCaptiveSystem = mpStationServer.isStationCaptive(loadData.getNextAddress());

        loadData.setMoveDate();
        setLoadDataMoveStatus(loadData, moveStatus);
        String vsDeviceID = mpLocServer.getLocationDeviceId(loadData.getNextWarehouse(),
                                                            loadData.getNextAddress());
        mpLoadServer.logLoadMoveTransaction(loadData.getLoadID(),
            loadData.getWarehouse(), loadData.getAddress(), loadData.getShelfPosition(),
            loadData.getNextWarehouse(), loadData.getNextAddress(), loadData.getNextShelfPosition(),
            vsDeviceID);
        
        if (vzCaptiveSystem)
        {
          initializeDeviceServer();
          int vnAisleGroup = mpDeviceServer.getDeviceAisleGroup(vsDeviceID);
          String vsHomeAddress = StandardLocationServer.getAddressFromLoadID(
                      loadData.getLoadID(), vnAisleGroup);
          mpLoad.setLoadForCaptiveRetrieve(loadData, vsHomeAddress, moveStatus, vsDeviceID);
        }
        else
        {
          mpLoad.setLoadForRetrieve(loadData, moveStatus, locationEmptyFlag, vsDeviceID);
        }

        mpLocServer.setLocationEmptyFlag(loadData.getWarehouse(),
            loadData.getAddress(), loadData.getShelfPosition(),
            locationEmptyFlag);
        commitTransaction(tt);

        if (mpStationServer.isStationAutoPick(loadData.getNextAddress()))
        {
          logDebug("LoadId \"" + loadData.getLoadID()
              + "\" At AutoPick Station");
          if (loadData.isBCRNR())
          {
            logError("BarCode + \"" + loadData.getLoadID()
                + "\" Bad/No Read - Deleting load");
            mpInvServer.deleteLoad(loadData.getLoadID(),
                ReasonCode.getDaifukuReasonCode());
          }
          else
          {
            autoPickLoad(loadData);
            if (vzCaptiveSystem)
            {

              String linkedStation = mpRouteServer.getNextRouteDest(
                  mpStationServer.getStationLinkedRoute(
                      loadData.getNextAddress()), loadData.getNextAddress());

              if (linkedStation.length() > 0)
              {
                logDebug("LoadId \"" + loadData.getLoadID()
                    + "\" Captive station transfer load to linked station "
                    + linkedStation
                    + " - " + msMyClass + ".moveLoadForRetrieve()");
                loadData = mpLoadServer.getParentLoad(parentLoadID);
                loadData.setAddress(linkedStation);
                mpLoadServer.updateLoadData(loadData, true);
              }
            }
          }
        }
        else
        {
          logDebug("LoadId \"" + loadData.getLoadID() + "\" moved to Station "
              + loadData.getNextAddress()
              + " not autopick - " + msMyClass + ".moveLoadForRetrieve()");
          autoCompleteLoadMove(loadData);
        }
      }
      else
      {
        logError("LoadId \"" + parentLoadID + "\" NOT found "
            + " - " + msMyClass + ".moveLoadForRetrieve()");
      }
    }
    catch (DBException e)
    {
      logException(e, "LoadId \"" + parentLoadID
          + "\" (Parent) Exception Changing Parent Load Status"
          + " - " + msMyClass + ".setParentLoadMoveStatus");
    }
    catch(InvalidDataException ide)
    {
      logException(ide, "LoadId \"" + parentLoadID
          + "\" (Parent) Exception Changing Parent Load Status"
          + " - " + msMyClass + ".setParentLoadMoveStatus");
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Load is at next address which is a location set location to occupied and
   * call updateLoadInfo to update location and status.
   * @param loadData load that has moved
   * @param isOldWarehouse the from-warehouse of the move.
   * @param isOldAddress the from-address of the move.
   * @param isOldShelfPos the from-position of the move.
   */
  protected void moveLoadForStore(LoadData loadData, String isOldWarehouse,
                                  String isOldAddress, String isOldShelfPos)
  {
    initializeLoadServer();
    initializeLocationServer();

    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      if (loadData != null)
      {
        loadData.setMoveDate();
        String vsDeviceId = mpLocServer.getLocationDeviceId(
            loadData.getWarehouse(), loadData.getAddress());
        loadData.setDeviceID(vsDeviceId);
        mpLoad.updateLoadInfo(loadData);

        emptyFromLocation(isOldWarehouse, isOldAddress, isOldShelfPos);

        mpLocServer.setLocationEmptyFlag(loadData.getWarehouse(),
            loadData.getAddress(), loadData.getShelfPosition(), DBConstants.OCCUPIED);

        mpLoadServer.logLoadMoveTransaction(loadData.getLoadID(),
            isOldWarehouse, isOldAddress, isOldShelfPos, loadData.getWarehouse(),
            loadData.getAddress(), loadData.getShelfPosition(), loadData.getDeviceID());
        commitTransaction(tt);
      }
      else
      {
        logError("LoadId \"null \" Error - " + msMyClass +
                 ".moveLoadForStore()");
      }
    }
    catch (DBException e)
    {
      logException(e, "LoadId \"" + loadData.getParentLoadID() +
                   "\" (Parent) Exception Changing Parent Load Status - " +
                   msMyClass + ".setParentLoadMoveStatus");
    }
    finally
    {
      endTransaction(tt);
    }
    //    logDebug(msMyClass + ".moveLoadForStore() - End");
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
    initializeLoadServer();
    initializeLocationServer();
    initializeStationServer();

    TransactionToken tt = null;
    String vsLastWarehouse, vsLastAddress, vsLastShelfPosition;

    try
    {
      mpStationServer.getStation(sStationID);

      tt = startTransaction();
      LoadData vpLoadData = mpLoadServer.getLoad(sLoadID);
      if (vpLoadData != null)
      {
        vsLastWarehouse = vpLoadData.getWarehouse();
        vsLastAddress   = vpLoadData.getAddress();
        vsLastShelfPosition = vpLoadData.getShelfPosition();
        vpLoadData.setParentLoadID(vpLoadData.getLoadID());
        vpLoadData.setLoadID(vpLoadData.getLoadID());
        vpLoadData.setWarehouse(mpStationServer.getStationWarehouse(sStationID));
        vpLoadData.setAddress(sStationID);
        vpLoadData.setLoadMoveStatus(iMoveStatus);
        vpLoadData.setDeviceID(mpLocServer.getLocationDeviceId(
            vpLoadData.getWarehouse(), vpLoadData.getAddress()));
        switch (iMoveDateFlag)
        {
          case 0:  // Oldest move date
            LoadData oldestLoad = mpLoadServer.getOldestLoadData(sStationID, 0);
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
        emptyFromLocation(vsLastWarehouse, vsLastAddress, vsLastShelfPosition);

        /*
         * Log the move
         */
        mpLoadServer.logLoadMoveTransaction(sLoadID, vsLastWarehouse,
            vsLastAddress, vsLastShelfPosition, vpLoadData.getWarehouse(),
            vpLoadData.getAddress(), vpLoadData.getShelfPosition(),
            vpLoadData.getDeviceID());

        commitTransaction(tt);

        logDebug(msMyClass + ".moveLoadToStation - Load ID \"" + sLoadID +
          "\" moved to Station " + sStationID);
      }
      else
      {
        logError(msMyClass + ".moveLoadToStation - Load ID \"" + sLoadID +
          "\" NOT found");
      }
    }
    catch (DBException e)
    {
      logException(e, msMyClass + ".moveLoadToStation - LoadID \"" + sLoadID +
         "\" (Parent) Exception Changing Parent Load Status");
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Mark the from-location as empty
   *
   * @param isWarehouse the location warehouse
   * @param isAddress the location address.
   * @param isPosition the shelf position within the location.
   * @throws DBException when there is a DB update error.
   */
  protected void emptyFromLocation(String isWarehouse, String isAddress,
                                   String isPosition) throws DBException
  {
    LocationData vpFromLoc = mpLocServer.getLocationRecord(isWarehouse, isAddress);
    if (vpFromLoc.getLocationType() == DBConstants.LCASRS)
    {
      mpLocServer.setLocationEmptyFlag(isWarehouse, isAddress, isPosition,
          DBConstants.UNOCCUPIED);
    }
  }

  /**
   * Set a load's move status (validating the status).
   * <BR><B>NOTE:</B> Does NOT update the database.
   *
   * @param ipLoadData
   * @param inMoveStatus
   */
  public void setLoadDataMoveStatus(LoadData ipLoadData, int inMoveStatus)
  {
    String sPrev = "";
    String s = "" + inMoveStatus;
    String field = LoadData.LOADMOVESTATUS_NAME;
    int previousStatus = ipLoadData.getLoadMoveStatus();
    try
    {
      sPrev = DBTrans.getStringValue(field, previousStatus);
      s = DBTrans.getStringValue(field, inMoveStatus);
    }
    catch (Exception e)
    {
      logError(msMyClass + " - DBTrans CANNOT find " + field
          + " - setLoadDataMoveStatus()");
    }
    ipLoadData.setLoadMoveStatus(inMoveStatus);

    if (inMoveStatus == DBConstants.NOMOVE
        // This can happen if a load is allocated at the same time that it is
        // being scheduled for a location-to-location move.  When the location-
        // to-location move completes, clear everything out and let it re-
        // schedule normally.  This is most likely to occur in a double-deep
        // system in conjunction with a swap, but it could happen in any system
        // with location-to-location moves.
        || (inMoveStatus == DBConstants.RETRIEVEPENDING &&
            ipLoadData.getAddress().equals(ipLoadData.getNextAddress())))
    {
      ipLoadData.setMoveDate();
      ipLoadData.setNextAddress(null);
      ipLoadData.setNextWarehouse(null);
      ipLoadData.setNextShelfPosition(null);
      ipLoadData.setLoadPresenceCheck(DBConstants.YES);
    }

    logDebug("LoadId \"" + ipLoadData.getLoadID() + "\" - New Status: " + s
        + " - Previous Status: " + sPrev + " - Load Data - " + msMyClass);
  }

  /**
   * Set load to station default values
   *
   * @param loadData
   * @param stationName
   * @return
   */
  protected LoadData setLoadToStationDefaults(LoadData loadData, String stationName)
  {
    initializeStationServer();

    List<Object> stationDefaults = mpStationServer.getStationDefaults(stationName);
    loadData.setWarehouse((String)stationDefaults.get(0));
    loadData.setDeviceID((String)stationDefaults.get(1));
    loadData.setContainerType((String)stationDefaults.get(2));
    loadData.setAmountFull((Integer)stationDefaults.get(3));
    loadData.setRouteID((String)stationDefaults.get(4));

    return loadData;
  }

  /**
   * Process the Alternate Location Response message
   *
   * @param ipLEDF
   */
  public void updateLoadForAlternateLocationResponse(LoadEventDataFormat ipLEDF)
  {
    TransactionToken vpTT = null;
    String vsLoadID = ipLEDF.getLoadID();

    try
    {
      vpTT = startTransaction();

      LoadData vpLD = mpLoadServer.getLoad(vsLoadID, true);
      if (vpLD.getLoadMoveStatus() == DBConstants.MOVESENT)
      {
        vpLD.setLoadMoveStatus(DBConstants.MOVING);
        mpLoadServer.updateLoadData(vpLD, false);
      }
      
      logLoadScheduled(vpLD.getLoadID(), vpLD.getWarehouse(), vpLD.getAddress(),
          vpLD.getShelfPosition(), vpLD.getNextWarehouse(),
          vpLD.getNextAddress(), vpLD.getNextShelfPosition(),
          vpLD.getDeviceID(), null);

      commitTransaction(vpTT);
    }
    catch (DBException dbe)
    {
      logException("Error updating load " + vsLoadID, dbe);
    }
    finally
    {
      endTransaction(vpTT);
    }
  }

  /**
   * Update a load in response to a Final Arrival from the AGC
   *
   * @param loadEventMessage - the message to process
   * @throws DBException
   */
  public String updateLoadForFinalArrival(LoadEventDataFormat loadEventMessage)
      throws DBException
  {
    initializeInventoryServer();
    initializeLoadServer();
    initializeRouteServer();
    initializeStationServer();

    String newCommand = "";
    String vsSourceStation = loadEventMessage.getSourceStation();
    String vsLoadID        = loadEventMessage.getLoadID();

    StationData vpStationData = mpStationServer.getStation(vsSourceStation);
    if (vpStationData == null)
    {
      logError("Station " + vsSourceStation + " Does NOT Exist - "
          + msMyClass + ".updateLoadForFinalArrival()");
      newCommand = "";
    }
    else
    {
      /*
       * TRANSFER_STATION
       */
      if (vpStationData.getStationType() == DBConstants.TRANSFER_STATION)
      {
        newCommand = updateLoadForFinalArrivalAtTransferStation(
            loadEventMessage, vpStationData);
      }
      /*
       * All but P&D, Reversible, and Transfers
       */
      else if(mpStationServer.doesStationGetRetrieveArrival(vsSourceStation))
      {
        logDebug(msMyClass +
            ".updateLoadForFinalArrival() - All Station except PD, Reversible, and Transfers");

        try
        {
          LoadData vpLD = mpLoadServer.getLoad(vsLoadID);
          if (vpLD == null)
          {
            logError("Load \"" + vsLoadID + "\" not found for final arrival.");
            return newCommand;
          }

          if (vpLD.getAddress().equals(vsSourceStation) &&
              vpStationData.getStationType() != DBConstants.OUTPUT)
          {
            loadEventMessage.changeFinalArrivalToStoreArrival();
            updateLoadForStoreArrival(loadEventMessage);
          }
          else
          {
            initializeLocationServer();
            int locEmptyFlag = mpLocServer.getEmptyFlagAfterRetrieve(vpStationData, vpLD);

            initializeMoveServer();
            String[] vasOrders = mpMoveServer.getAssociatedOrdersForLoad(vsLoadID);
            String vsOrderID = "";
            if (vasOrders.length > 0)
            {
              vsOrderID = vasOrders[0];
            }

            // Send LoadArrival message before moving the loads, since moving
            // the load generates OrderComplete messages and it doesn't make
            // sense to complete an order before the load arrives.
            if(mzHasHostSystem)
            {
              initializeHostServer();
              mpHostServer.sendLoadArrival(vsLoadID, vsSourceStation, vsOrderID);
            }

            // Move the load
            moveLoadForRetrieve(vsLoadID, vsSourceStation, locEmptyFlag,
                                DBConstants.ARRIVED);
            newCommand = loadEventMessage.screenLoadArrivedAtStation(vsLoadID,
                                                                     vsSourceStation);
          }
        }
        catch (Exception ex)
        {
          logException("Error", ex);
        }
      }
      /*
       * No arrival, just work complete
       */
      else
      {
        // This is a reversible or PD stand that doesn't get arrivals just work completes
        int loadPresence = loadEventMessage.getResults();
        if(loadPresence == 0)
        {
          mpInvServer.deleteLoad(loadEventMessage.getLoadID(),
                ReasonCode.getDaifukuReasonCode());
            logOperation(LogConsts.OPR_DEVICE, "LoadId \""
                + loadEventMessage.getLoadID()
                + "\" Deleted - Removed from PD or Reversible Station during Pick - "
                + msMyClass + ".updateLoadForFinalArrival()");
        }
        else
        {
          // this is then a store arrival for a PD Stand
          loadEventMessage.changeFinalArrivalToStoreArrival();
          updateLoadForStoreArrival(loadEventMessage);
        }
        newCommand = "";
      }
    }
    logDebug(msMyClass + ".updateLoadForFinalArrival() - End");
    return newCommand;
  }

  /**
   * Update Load for a Final Arrival at a Transfer Station
   * Move the load to the linked station & complete any moves/orders
   *
   * @param ipLoadEventMessage
   * @param ipStationData
   * @return
   */
  protected String updateLoadForFinalArrivalAtTransferStation(
      LoadEventDataFormat ipLoadEventMessage, StationData ipStationData)
  {
    initializeLoadServer();
    initializeMoveServer();
    initializeStationServer();
    initializeRouteServer();

    String vsLoadID = ipLoadEventMessage.getLoadID();
    String vsSourceStation = ipStationData.getStationName();
    String vsReturn = "";

    logDebug(msMyClass + ".updateLoadForFinalArrivalAtTransferStation()");

    try
    {
      /*
       * Move the load to the next station
       */
      String vsNextStation = mpRouteServer.getNextRouteDest(
          ipStationData.getLinkRoute(), vsSourceStation);
      moveLoadToStation(vsLoadID, vsNextStation, DBConstants.ARRIVEPENDING, 1);

      /*
       * Complete moves
       */
      LoadData vpLoadData = mpLoadServer.getLoad(vsLoadID);
      // completeMovesForLoad(vpLoadData);

      /*
       * Update the next location
       */
      String vsRoute = "";
      // First try move route
      MoveData vpMoveData = mpMoveServer.getNextMoveRecord(
          vpLoadData.getParentLoadID());
      if (vpMoveData != null)
      {
        vsRoute = vpMoveData.getRouteID();
      }
      else
      {
        // next try this station's route
        vsRoute = ipStationData.getLinkRoute();

        if (vsRoute.trim().length() == 0)
        {
          // finally use the load route
          vsRoute = vpLoadData.getRouteID();
        }
      }
      RouteData vpNextRoute = mpRouteServer.getNextRouteData(vsRoute,
          vsNextStation);
      if (vpNextRoute != null)
      {
        if (vpNextRoute.getDestType() == DBConstants.STATION)
        {
          vpLoadData.setNextWarehouse(
              mpStationServer.getStationWarehouse(vpNextRoute.getDestID()));
          vpLoadData.setNextAddress(vpNextRoute.getDestID());
        }
        else
        // Equipment route
        {
          vpLoadData.setNextWarehouse(vpLoadData.getFinalWarehouse());
          vpLoadData.setNextAddress(vpLoadData.getFinalAddress());
        }
        mpLoadServer.updateLoadData(vpLoadData, true);
      }

      /*
       * Wake-up the other scheduler
       */
      LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class, msMyClass);
      String vsWakeUp = vpLEDF.screenLoadRelease(vsLoadID, vsNextStation);
      String vsNextSched = mpStationServer.getStationsScheduler(vsNextStation);
      getSystemGateway().publishLoadEvent(vsWakeUp, 0, vsNextSched);
    }
    catch (DBException dbe)
    {
      logException(dbe, "Finding next station on route "
          + ipStationData.getLinkRoute());
    }
    return vsReturn;
  }

  /**
   * Update a load for a retrieve complete
   *
   * @param loadEventMessage
   * @return
   */
  public String updateLoadForRetrieveComplete(LoadEventDataFormat loadEventMessage)
  {
    initializeLoadServer();
    initializeLocationServer();
    initializeStationServer();

    LoadData loadData = mpLoadServer.getParentLoad(loadEventMessage.getLoadID());
    StationData vpSD = mpStationServer.getStation(loadEventMessage.getDestinationStation());
    String newCommand = "";

    if (loadData != null)
    {     // Does load exist
      if(mpStationServer.isStationTheRackMaster(loadEventMessage.getDestinationStation()))
      {   // if Destination is rack then shelf to shelf move
        logDebug("Shelf-To-Shelf Move - " + msMyClass + ".updateLoadForRetrieveComplete()");
        mpLoadServer.setParentLoadMoveStatus(loadData.getLoadID(),
                                             DBConstants.MOVING, null);
      }
      else
      {   // Regular retrieve
        if (vpSD.getArrivalRequired() == DBConstants.YES)
        {
          mpLoadServer.setParentLoadMoveStatus(loadEventMessage.getLoadID(),
                                               DBConstants.MOVING, null);
        }
        else
        {
          /*
           * If this is a reversible station that does not get retrieve
           * arrivals, do the auto-mode-change.
           */
          try
          {
            initializeMoveServer();
            String[] vasOrders = mpMoveServer.getAssociatedOrdersForLoad(loadData.getLoadID());

            if (vpSD.getStationType() == DBConstants.REVERSIBLE)
            {
              logDebug("Station " + vpSD.getStationName()
                  + " auto-mode-change to STORE");
              mpStationServer.setBidirectionalMode(vpSD.getStationName(),
                  DBConstants.STOREMODE);
            }
            int locEmptyFlag = mpLocServer.getEmptyFlagAfterRetrieve(vpSD,
                                                                     loadData);
            moveLoadForRetrieve(loadEventMessage.getLoadID(),
                                loadEventMessage.getDestinationStation(),
                                locEmptyFlag, DBConstants.ARRIVED);
            newCommand = loadEventMessage.screenLoadArrivedAtStation(
                                      loadEventMessage.getLoadID(),
                                      loadEventMessage.getDestinationStation());

                                       // Send Load Arrival message to Host.
            if(mzHasHostSystem)
            {
              initializeHostServer();
              mpHostServer.sendLoadArrival(loadData.getLoadID(), vpSD.getStationName(),
                            (vasOrders.length > 0) ? vasOrders[0] : "");
            }
          }
          catch(DBException exc)
          {
            logError("DB Error sending host Load Arival message!" + exc.getMessage());
            newCommand = "";
          }
        }
      }
    }
    else
    {
      logError("LoadId \"null\" - " + msMyClass + ".updateLoadForRetrieveComplete()");
    }

    return newCommand;
  }

  /**
   * Set load to retrieve pending and resend retrieve command when device ready
   *
   * @param isLoadID
   * @param isMessage
   */
  public void updateLoadForRetrieveResponseDeviceError(String isLoadID,
      String isMessage)
  {
    initializeLoadServer();
    logDebug("LoadId \"" + isLoadID + "\" RETRIEVEPENDING - " + msMyClass
        + ".updateLoadForRetrieveResponseDeviceError()");
    mpLoadServer.setParentLoadMoveStatus(isLoadID, DBConstants.RETRIEVEPENDING,
        isMessage);
  }

  /**
   * Set load to retrieve error and if it finally retrieves it will go to moving
   * or arrived on work complete
   *
   * @param isLoadID
   * @param isMessage
   */
  public void updateLoadForRetrieveResponseError(String isLoadID,
      String isMessage)
  {
    initializeLoadServer();
    logDebug("LoadId \"" + isLoadID + "\" RETRIEVEERROR - " + msMyClass
        + ".updateLoadForRetrieveResponseError()");
    mpLoadServer.setParentLoadMoveStatus(isLoadID, DBConstants.RETRIEVEERROR,
        isMessage);
  }

  /**
   * Update a load for a Retrieve Response
   *
   * @param isLoadID
   * @param isSchedulerName
   * @param inMaxRetrieveSendsAllowed
   * @return <code>LinkedHashMap</code> of 'R' +
   *         <code>LoadEventDataFormat</code> strings for additional retrieves
   */
  public LinkedHashMap<String, String> updateLoadForRetrieveResponseOK(String isLoadID,
      String isSchedulerName, int inMaxRetrieveSendsAllowed)
  {
    initializeLoadServer();
    initializeStationServer();

    // Must be a LinkedHashMap because order is important
    LinkedHashMap<String, String> vpCommandList = new LinkedHashMap<String, String>();

    logDebug(msMyClass
        + ".updateLoadForRetrieveResponseOK() - Start - Scheduler name: "
        + isSchedulerName);
    //
    // Response from Equipment to Load Retrieve command was "OK", so set
    // Load move status to "Retrieving".
    //
    mpLoadServer.setParentLoadMoveStatus(isLoadID, DBConstants.RETRIEVING, null);
    LoadData vpLoadData = mpLoadServer.getParentLoad(isLoadID);
    String vsStationId = vpLoadData.getNextAddress();
    StationData vpSD = mpStationServer.getStation(vsStationId);
    if (vpSD != null)
    {
      //
      // See if we can schedule any additional loads to be retrieved to the
      // Station.
      //
      List<LoadEventDataFormat> retrieveCommandList = anyLoadsToRetrieveToStation(
          vpSD, isSchedulerName, inMaxRetrieveSendsAllowed);
      if ((retrieveCommandList != null) && (!retrieveCommandList.isEmpty()))
      {
        StandardDeviceServer vpDevServ = Factory.create(StandardDeviceServer.class);
        for (LoadEventDataFormat loadEvent : retrieveCommandList)
        {
          String newCommand = loadEvent.createStringToSend();
          DeviceData vpDD = vpDevServ.getDeviceData(vpSD.getDeviceID());
          String vsCommDevice = vpSD.getDeviceID();
          if(vpDD != null && vpDD.getCommDevice().trim().length() > 0)
          {
            vsCommDevice = vpDD.getCommDevice();
          }
          vpCommandList.put("R" + newCommand, vsCommDevice);
        }
      }
      //
      // See if we can stage additional loads to the Station.
      //
      // TODO: The caller doesn't use these results.  Why not?  I'll comment these out. -Mike
//      if (mpStationServer.stageLoadToStation(vsStationId) > 0)
//      {
//        vpCommandList.put("S" + vsStationId, vpSD.getDeviceID());
//      }
    }

    // Log that the retrieval was scheduled
    try
    {
      initializeMoveServer();
      MoveData vpMoveData = mpMoveServer.getNextMoveRecord(isLoadID);
      String vsOrderID = (vpMoveData == null) ? null : vpMoveData.getOrderID();
      logLoadScheduled(isLoadID, vpLoadData.getWarehouse(),
          vpLoadData.getAddress(), vpLoadData.getShelfPosition(),
          vpLoadData.getNextWarehouse(), vpLoadData.getNextAddress(),
          vpLoadData.getNextShelfPosition(), vpLoadData.getDeviceID(),
          vsOrderID);
    }
    catch (DBException dbe)
    {
      logException("Error logging transaction history", dbe);
    }
    return vpCommandList;
  }

  /**
   * Update the load to be in rack
   *
   * @param ipLEDF
   * @param stationServer
   */
  public void updateLoadForShelfToShelfStoreComplete(LoadEventDataFormat ipLEDF)
  {
     /*
      * For now we will just call updateLoadForStoreComplete() but we may want
      * to do something else in the future
      */
    ipLEDF.setSourceLocation(ipLEDF.getDestinationLocation());
    updateLoadForStoreComplete(ipLEDF);
  }

  /**
   * Creates or updates a load when scheduler get a store arrival.
   *
   * @param loadEventMessage the arrival event received by the scheduler.
   * @return STORE_LOAD_OK indicates store command can be sent by scheduler.
   *         STORE_LOAD_FAILED indicates Store command should not be sent due to
   *         some type of error.
   */
  public int updateLoadForStoreArrival(LoadEventDataFormat loadEventMessage)
  {
    initializeLoadServer();
    initializeStationServer();

    // logDebug("Start Dummy Arrival - " + msMyClass + ".updateLoadForStoreArrival()");
    StationData vpStationData = mpStationServer.getStation(loadEventMessage.getSourceStation());
    if (vpStationData == null)
    {
      logError("Station \"" + loadEventMessage.getSourceStation()
          + "\" Doesn't Exist - " + msMyClass + ".updateLoadForStoreArrival()");
      return STORE_LOAD_FAILED;
    }

    if (vpStationData.getStationType() == DBConstants.AGC_TRANSFER)
    {
      // station is portal from one AGC to another (good idea to have a BCR here)
      return updateLoadForStoreArrivalAtAGCTransferStation(loadEventMessage,
          vpStationData);
    }
    else
    {
      // Station is a regular PD/Input/Whatever station
      return updateLoadForStoreArrivalAtRegularStation(loadEventMessage,
          vpStationData);
    }
  }

  /**
   * Update Load for a Final Arrival at an AGC Transfer Station
   * & re-send the store command
   *
   * @param ipLoadEventMessage
   * @param ipStationData
   * @return
   */
  protected int updateLoadForStoreArrivalAtAGCTransferStation(
      LoadEventDataFormat ipLoadEventMessage, StationData ipStationData)
  {
    initializeLoadServer();
    logDebug(msMyClass + ".updateLoadForStoreArrivalAtAGCTransferStation()");

    if(!ipLoadEventMessage.isBCRValid() || !ipLoadEventMessage.isBCRGoodRead())
    {
      try
      {
        LoadData vpOldestLoad = mpLoadServer.getOldestLoad(ipStationData.getWarehouse(),
            ipStationData.getStationName(), DBConstants.ARRIVEPENDING);
        moveLoadToStation(vpOldestLoad.getLoadID(),
            ipStationData.getStationName(), DBConstants.MOVEPENDING, 1);
      }
      catch (Exception e)
      {
        try
        {
          LoadData vpLoadData = createBadBCRLoad(ipLoadEventMessage);
          logError("Barcode \"" + ipLoadEventMessage.getBarCode()
              + "\" Bad/No Read -  Send to reject " + vpLoadData.getNextAddress()
              + " - " + msMyClass + ".updateLoadForStoreArrival()");
        }
        catch (DBException dbe)
        {
          logException("Unrecoverable error", dbe);
        }
      }
    }
    else
    {
      if(!mpLoad.exists(ipLoadEventMessage.getBarCode()))
      {
        createNewLoadAtStation(ipLoadEventMessage.getBarCode(),
            ipStationData.getStationName(), false);
      }
      moveLoadToStation(ipLoadEventMessage.getBarCode(),
          ipStationData.getStationName(), DBConstants.MOVEPENDING, 1);
    }
    return STORE_LOAD_OK;
  }

  /**
   * Update Load for a Store Arrival at a Station
   *
   * @param ipLEDF
   * @param ipStationData
   * @return
   */
  protected int updateLoadForStoreArrivalAtRegularStation(
      LoadEventDataFormat ipLEDF, StationData ipStationData)
  {
    String vsMethodInfo = msMyClass + ".updateLoadForStoreArrivalAtRegularStation()";

    initializeStationServer();

    LoadData vpLoadData = Factory.create(LoadData.class);

    try
    {
      // Not all zeros, and not empty string
      if (ipLEDF.isBCRValid())
      {
        vpLoadData = getLoadWithValidBCR(ipLEDF, ipStationData);
      }
      else    // Don't have BCR reader. Get oldest ARRIVEPENDING Load
      {
        vpLoadData = getLoadWithoutBCR(ipLEDF, ipStationData);
      }
    }
    catch(DBException dbe)
    {
      return STORE_LOAD_FAILED;
    }
    catch(InvalidDataException ive)
    {
      logError("##EXCEPTION## Invalid Data Exception " + ive.getMessage());
      return STORE_LOAD_FAILED;
    }

    if (vpLoadData == null)
    {
      /*
       * We have an arrival, but couldn't identify the load
       */
      createIDPendingLoad(ipLEDF);
      return STORE_LOAD_FAILED;
    }
    else if (vpLoadData.getLoadMoveStatus() != DBConstants.ARRIVEPENDING
          && !mpStationServer.isStationAutoStore(ipLEDF.getSourceStation()))
    {
      /*
       * Don't care about ID Pending loads at an AutoStore Station at this point
       *
       * Load is NOT arrive pending. No release done yet, so create ID Pending
       * load.
       */
      logDebug("LoadId \"" + vpLoadData.getLoadID()
            + "\" is not arrive pending - " + vsMethodInfo);
      createIDPendingLoad(ipLEDF);
      return STORE_LOAD_FAILED;
    }
    else if (vpLoadData.getLoadMoveStatus() == DBConstants.ARRIVEPENDING)
    {
      /*
       * Load is created and arrived Pending so store it
       */
      // Update the height
      if (vpLoadData.getHeight() != ipLEDF.getDimensionInfo())
      {
        logDebug("LoadId \"" + vpLoadData.getLoadID()
            + "\" Arrived Load height is different - " + vsMethodInfo);
        vpLoadData.setHeight(ipLEDF.getDimensionInfo());
      }

      // Load presence?
      if (vpLoadData.getLoadPresenceCheck() != ipLEDF.getResults())
      {
        logDebug("LoadId \"" + vpLoadData.getLoadID()
            + "\" Arrived Load Presence is different - " + vsMethodInfo);
      }

      // If next is a station, make it MOVEPENDING, otherwise STOREPENDING
      if (mpStationServer.exists(vpLoadData.getNextAddress()))
      {
        setLoadDataMoveStatus(vpLoadData, DBConstants.MOVEPENDING);
      }
      else
      {
        setLoadDataMoveStatus(vpLoadData, DBConstants.STOREPENDING);
      }
      logDebug("LoadId \"" + vpLoadData.getLoadID() + "\" LoadData Updated - "
          + vsMethodInfo);
      mpLoadServer.updateLoadData(vpLoadData, true);
      return STORE_LOAD_OK;
    }
    else if (vpLoadData.getLoadMoveStatus() == DBConstants.STOREPENDING)
    {
      /*
       * Load is created (probably by auto-store) and is store-pending
       */
      return STORE_LOAD_OK;
    }
    else
    {
      /*
       * The scheduler doesn't need to do anything (ID Pending or Store Error)
       */
      return STORE_LOAD_FAILED;
    }
  }

  /**
   * Update the load to be in rack.
   * <P>
   * <I>NOTE: The station and the rack location must be in the same warehouse.</I>
   * </P>
   *
   * @param ipLoadEventMessage
   */
  public void updateLoadForStoreComplete(LoadEventDataFormat ipLoadEventMessage)
  {
    initializeLoadServer();
    initializeStationServer();

    LoadData vpLoadData = mpLoadServer.getParentLoad(ipLoadEventMessage.getLoadID());
    if (vpLoadData != null)
    {
      String vsStoreWhs = vpLoadData.getNextWarehouse();
      String vsStoreAddr = vpLoadData.getNextAddress();
      String vsFromAddress = vpLoadData.getAddress();
      String vsFromPosition = vpLoadData.getShelfPosition();

      vpLoadData.setLoadID(vpLoadData.getParentLoadID());
      vpLoadData.setLoadPresenceCheck(DBConstants.YES);
      vpLoadData.setAddress(ipLoadEventMessage.getSourceLocation());
      vpLoadData.setShelfPosition(ipLoadEventMessage.getSourceLocnShelfPosition());

      vpLoadData.setFinalAddress(null);
      vpLoadData.setFinalWarehouse(null);
      if (mpStationServer.exists(ipLoadEventMessage.getSourceLocation()))
      { // if location is a station set load move status to arrived station to
        // station transfer
        setLoadDataMoveStatus(vpLoadData, DBConstants.ARRIVED);
        vpLoadData.setMoveDate();
        vpLoadData.setNextAddress(null);
        vpLoadData.setNextWarehouse(null);
        vpLoadData.setNextShelfPosition(null);
        vpLoadData.setLoadPresenceCheck(DBConstants.YES);
      }
      else
      {
        /*
         * set load move status to RETRIEVEPENDING if there are any moves for
         * this load, otherwise set it to NOMOVE in the rack
         */
        initializeMoveServer();
        if (mpMoveServer.moveExists(vpLoadData.getLoadID()))
          setLoadDataMoveStatus(vpLoadData, DBConstants.RETRIEVEPENDING);
        else
          setLoadDataMoveStatus(vpLoadData, DBConstants.NOMOVE);

        // Location Arrival host message
        if (mzHasHostSystem)
        {
          try
          {
            initializeHostServer();
            mpHostServer.sendLocationArrival(vpLoadData.getLoadID(), vsStoreWhs,
                vsStoreAddr);
          }
          catch (DBException ex)
          {
            logError("Error sending Location Arrival message for Load "
                + vpLoadData.getLoadID() + " at location " + vsStoreWhs + "-"
                + vsStoreAddr);
          }
        }

        updateMCKeyForStoreComplete(vpLoadData);
      }

      moveLoadForStore(vpLoadData, vpLoadData.getWarehouse(), vsFromAddress,
                       vsFromPosition);
    }
  }

  /**
   * Update the MC Key when a load is stored
   *
   * @param ipLoadData
   */
  public void updateMCKeyForStoreComplete(LoadData ipLoadData)
  {
    /*
     * If 1) the MC Key doesn't match the load ID and
     *    2) the load ID is not too long and
     *    3) the load ID is not used for another load's MC Key
     * then make the MC Key match the load ID for better visibility the next
     * time we move the load.
     */
    if (!ipLoadData.getLoadID().equals(ipLoadData.getMCKey()) &&
        ipLoadData.getLoadID().length() <= AGCDeviceConstants.LNAGCLOADID)
    {
      try
      {
        if (mpLoad.getLoadDataFromTrackingId(ipLoadData.getLoadID(),
            DBConstants.NOWRITELOCK) == null)
        {
          ipLoadData.setMCKey(ipLoadData.getLoadID());
        }
      }
      catch (DBException dbe)
      {
        /*
         * Something Bad (tm) happened, but we wont worry about it.  We'll
         * just not change the tracking ID and pretend all is well.
         */
      }
    }
  }

  /**
   * Set load to store pending and resend store command when device ready
   *
   * @param isLoadID
   * @param isMessage
   */
  public void updateLoadForStoreResponseDeviceError(String isLoadID, String isMessage)
  {
    initializeLoadServer();
    int vnNewStatus = DBConstants.STOREPENDING;
    try
    {
      if (mpLoadServer.getLoadMoveStatus(isLoadID) != DBConstants.STORESENT)
      {
        vnNewStatus = DBConstants.MOVEPENDING;
      }
    }
    catch (DBException dbe)
    {
      logException("Error getting load status for " + isLoadID, dbe);
    }
    mpLoadServer.setParentLoadMoveStatus(isLoadID, vnNewStatus, isMessage);
  }

  /**
   * Set load to send error and if it finally stores it will go to NOMOVE on
   * work complete
   *
   * @param isLoadID
   * @param isMessage
   */
  public void updateLoadForStoreResponseError(String isLoadID, String isMessage)
  {
    initializeLoadServer();
    int vnNewStatus = DBConstants.STOREERROR;
    try
    {
      if (mpLoadServer.getLoadMoveStatus(isLoadID) != DBConstants.STORESENT)
      {
        vnNewStatus = DBConstants.MOVEERROR;
      }
    }
    catch (DBException dbe)
    {
      logException("Error getting load status for " + isLoadID, dbe);
    }
    mpLoadServer.setParentLoadMoveStatus(isLoadID, vnNewStatus, isMessage);
  }

  /**
   * Set load to storing and return station name to store load from
   *
   * @param isLoadID
   */
  public String updateLoadForStoreResponseOK(String isLoadID)
  {
    String vsMessage = null;
    initializeLoadServer();
    int vnNewStatus = DBConstants.STORING;
    LoadData vpCurrentLoad = mpLoadServer.getLoad(isLoadID);
    if (vpCurrentLoad == null)
    {
      logError("Error getting load status for " + isLoadID + "; load NOT FOUND!");
      return "";
    }
    switch (vpCurrentLoad.getLoadMoveStatus())
    {
      case DBConstants.STOREERROR:
      case DBConstants.STORESENT:
        // default is STORING
        break;

      case DBConstants.MOVEERROR:
      case DBConstants.MOVESENT:
        vsMessage = vpCurrentLoad.getLoadMessage();
        vnNewStatus = DBConstants.MOVING;
        break;

      default:
        String vsLMS = "" + vpCurrentLoad.getLoadMoveStatus();
        try
        {
          vsLMS = DBTrans.getStringValue(LoadData.LOADMOVESTATUS_NAME,
              vpCurrentLoad.getLoadMoveStatus());
        }
        catch (NoSuchFieldException e) {}
        logError("Unexpected Store Response for load \"" + isLoadID + "\" ("
            + vsLMS + ")!");
        return "";
    }

    mpLoadServer.setParentLoadMoveStatus(isLoadID, vnNewStatus, vsMessage);

    LoadData loadData = mpLoadServer.getLoad(isLoadID);
    logLoadScheduled(isLoadID, loadData.getWarehouse(), loadData.getAddress(),
        loadData.getShelfPosition(), loadData.getNextWarehouse(),
        loadData.getNextAddress(), loadData.getNextShelfPosition(),
        loadData.getDeviceID(), null);

    return loadData.getAddress();
  }

  /**
   * Update a load in response to a Trigger of Operation from the AGC
   *
   * @param ipLEDF Load Event
   * @return String an empty string
   * @throws DBException
   */
  public String updateLoadForTriggerOfOperation(LoadEventDataFormat ipLEDF)
      throws DBException
  {
    initializeStationServer();

    StationData vpStationData = mpStationServer.getStation(ipLEDF.getDestinationStation());
    if (vpStationData == null)
    {
      logError("Station \"" + ipLEDF.getDestinationStation()
          + "\" Doesn't Exist - " + msMyClass
          + ".updateLoadForTriggerOfOperation()");
    }
    return "";
  }
  
  /**
   * Update a load in response to a Trigger of Operation from the AGC
   *
   * @param ipLEDF Load Event
   * @return String an empty string
   * @throws DBException
   */
  public String processRetrievalTrigger(LoadEventDataFormat ipLEDF)
      throws DBException
  {
    String vsStation = ipLEDF.getDestinationStation();
   
    logError("Station \"" + vsStation
          + "\" RetrievalTrigger recieved - " + msMyClass
          + ".processRetievalTrigger()");
    
    return "";
  }

  /**
   * Update a load in response to a Work Complete with the Cancel flag set.
   *
   * @param ipLEDF <code>LoadEventDataFormat</code>
   */
  public void updateLoadForWorkCompleteCancel(LoadEventDataFormat ipLEDF)
  {
    initializeLoadServer();
    mpLoadServer.setParentLoadMoveStatus(ipLEDF.getLoadID(),
        DBConstants.RETRIEVEERROR, "Movement canceled by equipment");
  }

  /**
   * Send an alternate location message for a load
   *
   * @param ipLoadData
   * @param ipStnData
   */
  public void sendAlternateLocation(LoadData ipLoadData, StationData ipStnData)
  throws DBException
  {
    initializeLoadServer();

    LoadEventDataFormat vpLEDF = new LoadEventDataFormat("AltLoc");
    String vsMessage;

    TransactionToken vpTT = null;
    try
    {
      vpTT = startTransaction();

      LoadData vpLockedLoadData = mpLoadServer.getLoad(ipLoadData.getLoadID(), true);
      switch (vpLockedLoadData.getLoadMoveStatus())
      {
        case DBConstants.BINFULL_ERROR:
          vsMessage = vpLEDF.binFullMoveStation(vpLockedLoadData.getLoadID(),
              ipStnData.getStationName(), vpLockedLoadData.getHeight());
          break;
        case DBConstants.SIZE_ERROR:
          vsMessage = vpLEDF.binDimensionMismatchMoveStation(
              vpLockedLoadData.getLoadID(), ipStnData.getStationName(),
              vpLockedLoadData.getHeight());
          break;
        default:
          throw new DBException("Load \"" + ipLoadData.getLoadID()
              + "\" does not need an alternate location command.");
      }

      vpLockedLoadData.setNextWarehouse(ipStnData.getWarehouse());
      vpLockedLoadData.setNextAddress(ipStnData.getStationName());
      vpLockedLoadData.setLoadMoveStatus(DBConstants.MOVESENT);

      mpLoadServer.updateLoadInfo(vpLockedLoadData);

      String vsScheduler = Factory.create(StandardDeviceServer.class).getSchedulerName(
          ipLoadData.getDeviceID());

      commitTransaction(vpTT);

      // We have to do this outside of the transaction
      getSystemGateway().publishControlEvent(
          ControlEventDataFormat.getAlternateLocationCommand(vsMessage),
          ControlEventDataFormat.RECOVERY_ALTLOC, vsScheduler);
    }
    finally
    {
      endTransaction(vpTT);
    }

  }

  /*========================================================================*/
  /*  Tracking ID / MC Key methods                                          */
  /*========================================================================*/

  /**
   * Return a Load Id associated with a Load Tracking Id.
   *
   * @param isTrackingId load id that needs a tracking id
   * @return the load id associated with the load tracking id
   * @throws DBException
   */
  public String getLoadIdFromTrackingId(String isTrackingId) throws DBException
  {
    initializeLoadServer();

    String vsLoadId = isTrackingId;
    if (!mpLoadServer.loadExists(isTrackingId))
    {
      //
      // SRC's TrackingId (MCKey) is NOT the WRx-J LoadId.
      // Find the WRx-J LoadId from the SRC Tracking Id.
      //
      vsLoadId = mpLoad.getLoadIdFromTrackingId(isTrackingId);
      logDebug("LoadId \"" + vsLoadId + "\" using TrackingId \""
          + isTrackingId + "\" - getLoadIdFromTrackingId()");
    }
    return vsLoadId;
  }

  /**
   * Return a Load Tracking Id associated with the Load Id.  The caller should
   * have already determined that a Tracking Id is needed.
   *
   * @param isLoadId load id that needs a tracking id
   * @param inTrackingIdMaxValue max value of the needed tracking id
   * @return the tracking id to be used with the load id
   */
  public String getTrackingId(String isLoadId, int inTrackingIdMaxValue)
  {
    initializeLoadServer();

    // If someone deleted the load, we have no choice but to hope the load is
    // the tracking ID.
    LoadData vpLoadData = mpLoadServer.getParentLoad(isLoadId);
    if (vpLoadData == null)
    {
      return isLoadId;
    }

    // Do we already have a tracking ID?
    String vsTrackingId = vpLoadData.getMCKey();
    if ((vsTrackingId != null) && (vsTrackingId.length() > 0))
    {
      // This Load already has a Tracking Id we can use - Return.
      return vsTrackingId;
    }

    // Use the load ID if we can.
    if (isLoadId.length() <= AGCDeviceConstants.LNAGCLOADID)
    {
      setTrackingID(isLoadId, isLoadId);
      return isLoadId;
    }

    /*
     * Current Load TrackingId either does not yet exist or is too long for
     * caller - Create a shorter TrackingId that meets the caller's
     * requirements.
     */
    setTrackingID(isLoadId, "");
    vsTrackingId = null;

    /*
     * Need a Tracking Id for this load, but it does NOT already exist.
     * Create one.
     */
    if (msBarCodeTrackingId != null)
    {
      // We need to extract the Tracking Id from a field within the bar code.
      try
      {
        if (msBarCodeTrackingIdEnd < isLoadId.length())
        {
          vsTrackingId = isLoadId.substring(msBarCodeTrackingIdStart, msBarCodeTrackingIdEnd);
        }
        else
        {
          vsTrackingId = isLoadId.substring(msBarCodeTrackingIdStart);
        }
        setTrackingID(isLoadId, vsTrackingId);
      }
      catch(Exception e)
      {
        logError("LoadId \"" + isLoadId + "\" \"" + msBarCodeTrackingId
            + "\" CharactersAsTrackingId ERROR - " + e.getMessage());
        vsTrackingId = null;
      }
    }

    /*
     * We still don't have a valid tracking ID.  Generate a random one.
     */
    if (vsTrackingId == null)
    {
      Random rand = new Random();
      while (true)
      {
        vsTrackingId = "" + rand.nextInt(inTrackingIdMaxValue);
        try
        {
          if (mpLoad.getLoadDataFromTrackingId(vsTrackingId,
              DBConstants.NOWRITELOCK) == null)
          {
            setTrackingID(isLoadId, vsTrackingId);
            logDebug("LoadId \"" + isLoadId + "\" using TrackingId \""
                + vsTrackingId + "\" - getLoadsTrackingId()");
            break;
          }
        }
        catch(DBException e)
        {
          logException(e, "TrackingId  \"" + vsTrackingId + "\" Read Error - "
              + getClass().getSimpleName() + ".getTrackingId");
          return null;
        }
      }
    }
    return vsTrackingId;
  }

  /**
   *  Sets the bar code for a load.
   *
   *  @param isLoadID <code>String</code> containing load being modified.
   *  @param isMCKey <code>String</code> containing tracking ID
   */
  public void setTrackingID(String isLoadID, String isMCKey)
  {
    LoadData vpLoadData = Factory.create(LoadData.class);
    vpLoadData.setKey(LoadData.LOADID_NAME, isLoadID);
    vpLoadData.setMCKey(isMCKey);

    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      mpLoad.modifyElement(vpLoadData);
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException(e, "LoadId \"" + isLoadID + "\" in setTrackingID()");
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /*========================================================================*/
  /*  TRANSACTION HISTORY                                                   */
  /*========================================================================*/

  /**
   * Log Load move to retrieving
   *
   * @param isLoadID
   * @param isFromWarehouse
   * @param isFromAddress
   * @param isFromPosition
   * @param isToWarehouse
   * @param isToAddress
   * @param isToPosition
   * @param isDeviceID
   * @param isOrderID
   */
  public void logLoadScheduled(String isLoadID, String isFromWarehouse,
      String isFromAddress, String isFromPosition, String isToWarehouse,
      String isToAddress, String isToPosition, String isDeviceID,
      String isOrderID)
  {
    tnData.clear();
    tnData.setTranCategory(DBConstants.LOAD_TRAN);
    tnData.setTranType(DBConstants.LOAD_SCHED);
    tnData.setLoadID(isLoadID);
    tnData.setOrderID(isOrderID);
    if (isFromPosition.equals(LoadData.DEFAULT_POSITION_VALUE))
      tnData.setLocation(isFromWarehouse, isFromAddress);
    else
      tnData.setLocation(isFromWarehouse, isFromAddress + isFromPosition);
    if (isToPosition.equals(LoadData.DEFAULT_POSITION_VALUE))
      tnData.setToLocation(isToWarehouse, isToAddress);
    else
      tnData.setToLocation(isToWarehouse, isToAddress + isToPosition);
    tnData.setDeviceID(isDeviceID);
    logTransaction(tnData);
  }

  /*========================================================================*/
  /*  End TRANSACTION HISTORY                                               */
  /*========================================================================*/

  /*========================================================================*/
  /*  Host messages                                                         */
  /*========================================================================*/

  /**
   * Method sends host system notification for missing load.
   *
   * @param isMissingLoad the bin empty load.
   * @return order associated with this load. Empty string if no orders are
   *         associated to this load.
   * @throws DBException if there is a database error.
   */
  protected String sendHostBinEmptyError(String isMissingLoad) throws DBException
  {
    if (mzHasHostSystem)
    {
      String[] vasOrders = Factory.create(Move.class)
          .getAssociatedOrdersForLoad(isMissingLoad);
      /*-----------------------------------------------------------------------
       * If more than one order was to be picked from this load, don't specify
       * order id. in the message.
       *---------------------------------------------------------------------*/
      String vsErrorMsg;
      if (vasOrders == null || vasOrders.length == 0 || vasOrders.length > 1)
        vsErrorMsg = "Load " + isMissingLoad
            + " is not physically at its location!";
      else
        vsErrorMsg = "Load " + isMissingLoad + " for order " + vasOrders[0]
            + " is not physically at its location.  Order is cancelled!";

      initializeHostServer();
      mpHostServer.writeHostError(HostError.BIN_EMPTY_ERROR, 0, vsErrorMsg);
      logError(vsErrorMsg);

      return ((vasOrders == null || vasOrders.length == 0) ? "" : vasOrders[0]);
    }
    else
    {
      return "";
    }
  }

  /*========================================================================*/
  /*  The following initialize other servers if/when we need them.  This    */
  /*  is cheaper than both constantly creating/destroying them and          */
  /*  needlessly creating them when we create the server.                   */
  /*========================================================================*/
  protected void initializeHostServer()
  {
    if (mpHostServer == null)
    {
      mpHostServer = Factory.create(StandardHostServer.class, msMyClass);
    }
  }

  protected void initializeInventoryServer()
  {
    if (mpInvServer == null)
    {
      mpInvServer = Factory.create(StandardInventoryServer.class, msMyClass);
    }
  }

  protected void initializeLoadServer()
  {
    if (mpLoadServer == null)
    {
      mpLoadServer = Factory.create(StandardLoadServer.class, msMyClass);
    }
  }

  protected void initializeLocationServer()
  {
    if (mpLocServer == null)
    {
      mpLocServer = Factory.create(StandardLocationServer.class, msMyClass);
    }
  }

  protected void initializeMoveServer()
  {
    if (mpMoveServer == null)
    {
      mpMoveServer = Factory.create(StandardMoveServer.class, msMyClass);
    }
  }

  protected void initializePickServer()
  {
    if (mpPickServer == null)
    {
      mpPickServer = Factory.create(StandardPickServer.class, msMyClass);
    }
  }

  protected void initializePOReceivingServer()
  {
    if (mpPOServer == null)
    {
      mpPOServer = Factory.create(StandardPoReceivingServer.class, msMyClass);
    }
  }

  protected void initializeRouteServer()
  {
    if (mpRouteServer == null)
    {
      mpRouteServer = Factory.create(StandardRouteServer.class, msMyClass);
    }
  }

  protected void initializeStationServer()
  {
    if (mpStationServer == null)
    {
      mpStationServer = Factory.create(StandardStationServer.class, msMyClass);
    }
  }

  protected void initializeDeviceServer()
  {
    if (mpDeviceServer == null)
    {
      mpDeviceServer = Factory.create(StandardDeviceServer.class, msMyClass);
    }
  }

  /*========================================================================*/
  /*  End helper-server initialization methods                              */
  /*========================================================================*/
}
