package com.daifukuoc.wrxj.custom.ebs.dataserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dataserver.standard.StandardSchedulerServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeader;
import com.daifukuamerica.wrxj.dbadapter.data.ReasonCode;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.device.gateway.SystemGateway;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.InvalidDataException;
import com.daifukuamerica.wrxj.messageformat.loadevent.LoadEventDataFormat;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSLoad;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSStationData;
import com.daifukuoc.wrxj.custom.ebs.jdbc.BCSMessage;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSConstants;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSDBConstants;

public class EBSSchedulerServer extends StandardSchedulerServer {

    private BCSServer mpBCSServer;
    private EBSLoadServer mpEBSLoadServer;
    protected EBSLoad mpEBSLoad = Factory.create(EBSLoad.class);
    protected EBSInventoryServer mpEBSInvServer;
    protected EBSHostServer mpEBSHostServer;
    protected EBSLocationServer mpEBSLocationServer;
    protected EBSPoReceivingServer mpEBSPoReceivingServer;
    private SystemGateway systemGateway = ThreadSystemGateway.get();
    private PurchaseOrderHeader vpEH;
    private Location location;

    /**
     * Constructor w/o key name
     */
    public EBSSchedulerServer() {
        super();
    }

    /**
     * Constructor with key name
     *
     * @param isKeyName
     */
    public EBSSchedulerServer(String isKeyName) {
        super(isKeyName);
    }

    /**
     * Web application constructor for per user connection pooling
     * 
     * @param keyName
     * @param dbo
     */
    public EBSSchedulerServer(String keyName, DBObject dbo) {
        super(keyName, dbo);
    }

    /**
     * Check to see if station has a load to Retrieve. If station is online and there is not already a load that has had
     * a command sent and we are waiting for a retrieve response ok then see if max enroute is less that current
     *
     * @param ipSD - StationData of station that might want work
     * @param isSchedulerName
     * @param vnNumberOfLoadsToRetrieve
     *
     * @return <code>ArrayList</code>
     */
    public ArrayList<LoadEventDataFormat> anyLoadsToRetrieveToStation(StationData ipSD, String isSchedulerName,
            int vnNumberOfLoadsToRetrieve) {
        initializeLoadServer();
        initializeStationServer();

        String vsStation = ipSD.getStationName();

        TransactionToken tt = null;
        ArrayList<LoadEventDataFormat> moveCommandList = new ArrayList<LoadEventDataFormat>();
        if (mpStationServer.canStationRetrieveALoad(ipSD) == false) {
            //
            // This station is the correct type of station to accept a retrieved
            // load and its device is Online.
            //
            return moveCommandList;
        }

        // Can this scheduler schedule for this station
        if (mpStationServer.isStationScheduler(isSchedulerName, ipSD.getStationName()) == false) {
            logDebug("Scheduler doesn't schedule for Station " + vsStation);
            return moveCommandList;
        }
        // can we schedule additional loads to be retrieved to this station.
        if (isStationWaitingForRetrieveOkResponse(vsStation, ipSD.getMaxAllowedEnroute())) {
            logDebug("Station " + vsStation + " is waiting for Retrieve Ok Response");
            return moveCommandList;
        }
        int enrouteLoadCount = getEnrouteLoadCount(vsStation);
        if (ipSD.getMaxAllowedEnroute() >= enrouteLoadCount + vnNumberOfLoadsToRetrieve) {
            List<LoadData> retrievePendingLoads = getRetrievePendingLoads(ipSD, isSchedulerName);
            if (retrievePendingLoads == null) {
                logDebug("Station " + vsStation + " has NO Retrieve Pending Loads - anyLoadsToRetrieveToStation()");
                return moveCommandList;
            } else if (retrievePendingLoads.size() < vnNumberOfLoadsToRetrieve) {
                // wait till I have number of retrieve pending loads that are retrieve pending
                logDebug("Station " + vsStation + " has only " + retrievePendingLoads.size()
                        + " Retrieve Pending Loads - We want " + vnNumberOfLoadsToRetrieve
                        + " retrieve pending Loads - anyLoadsToRetrieveToStation() ");
                return moveCommandList;
            }

            /*
             * If we get this far then we have at least one load to schedule.
             */
            int vnEnrouteAllowed = ipSD.getMaxAllowedEnroute() - enrouteLoadCount;
            while (moveCommandList.size() < vnEnrouteAllowed && retrievePendingLoads.size() > 0) {
                LoadData loadData = retrievePendingLoads.remove(0);
                String vsLoadID = loadData.getLoadID();

                try {
                    String vsRouteID = "";
                    StationData vpFinalDestination = null;
                    String vsFinalWarehouse = null;
                    String vsFinalAddress = null;

                    /*
                     * If this station is a transfer or AGC transfer station, check the enroute count at the final
                     * destination (from the load's or move's route)
                     */
                    if (ipSD.getStationType() == DBConstants.TRANSFER_STATION
                            || ipSD.getStationType() == DBConstants.AGC_TRANSFER
                            || ipSD.getStationType() == DBConstants.OUTPUT) // MCM, EBS - added output so that final
                                                                            // address is updated
                    {
                        vsRouteID = mpLoad.getRetrieveRoute(vsLoadID, vsStation, isSchedulerName);
                        vpFinalDestination = mpStationServer.getStation(vsRouteID);
                        if (vpFinalDestination != null) {
                            int vnEnrouteToDest = mpLoad.getEnrouteCountPlusAtStation(vsRouteID);
                            if (vnEnrouteToDest >= vpFinalDestination.getMaxAllowedEnroute()) {
                                continue;
                            }
                            vsFinalWarehouse = vpFinalDestination.getWarehouse();
                            vsFinalAddress = vpFinalDestination.getStationName();
                        }
                    }

                    /*
                     * Lock the load. If it is not Retrieve Pending, then we have already processed it.
                     */
                    tt = startTransaction();
                    LoadData vpLDData = mpLoadServer.getLoad(vsLoadID, true);

                    if (vpLDData.getLoadMoveStatus() != DBConstants.RETRIEVEPENDING
                            || !doPreliminaryRetrieveChecks(vpLDData, ipSD, isSchedulerName, moveCommandList)) {
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
                    LoadEventDataFormat vpLEM = changeLoadToRetrieveSent(vpLDData, isSchedulerName, vsRouteID,
                            vnPriorityCategory, vnReInputFlag, vnRetrieveCommandDetail, ipSD.getWarehouse(), vsStation,
                            LoadData.DEFAULT_POSITION_VALUE, vsFinalWarehouse, vsFinalAddress);

                    commitTransaction(tt);
                    if (vpLEM != null) {
                        moveCommandList.add(vpLEM);
                        logDebug("LoadId \"" + vsLoadID + "\" Scheduling to move to " + "station " + vsStation);
                    }
                } catch (DBException e) {
                    logException(e, "LoadId \"" + vsLoadID + "\" (Parent) Exception Changing Parent Load Status  - "
                            + getClass().getSimpleName() + ".anyLoadsToRetrieveToStation");
                } finally {
                    endTransaction(tt);
                }
            }
        } else {
            logDebug("Station " + vsStation + " has " + enrouteLoadCount + " enroute loads, doesn't need any more");
        }

        return moveCommandList;
    }

    /**
     * Get the retrieve pending loads for a station and scheduler. It is assumed that it has already been asserted that
     * this scheduler schedules for the station.
     *
     * @param ipSTData object containing station data.
     * @param isScheduler The scheduler responsible for sending retrieve commands.
     */
    @SuppressWarnings("rawtypes")
    protected List<LoadData> getRetrievePendingLoads(StationData ipSTData, String isScheduler) {
        initializeDeviceServer();

        List<LoadData> vpRetrievePendingLoads = new ArrayList<LoadData>();
        List<String> vpRetrievePendingLoadIdList = new ArrayList<String>();
        try {
            List<Map> vpAllRetrievePendLoads = null;

            if (mpDeviceServer.isDeviceInoperable(((EBSStationData) ipSTData).getSecondaryDeviceID())) {
                vpAllRetrievePendLoads = mpEBSLoad.getRetrievePendingLoadsCombinedAisle(ipSTData.getStationName(),
                        isScheduler);
                logDebug("Station " + ipSTData.getStationName() + " has up to " + vpAllRetrievePendLoads.size()
                        + " Combined Aisle Retrieve Pending Loads - getRetrievePendingLoadsCombinedAisle()"
                        + " (list may contain duplicates)");
            } else {
                vpAllRetrievePendLoads = mpEBSLoad.getRetrievePendingLoads(ipSTData.getStationName(), isScheduler);
                logDebug("Station " + ipSTData.getStationName() + " has up to " + vpAllRetrievePendLoads.size()
                        + " Retrieve Pending Loads - getRetrievePendingLoads()" + " (list may contain duplicates)");
            }
            /*
             * Make sure the loads can make it all the way to the final destination
             */
            initializeMoveServer();
            initializeRouteServer();
            for (Map m : vpAllRetrievePendLoads) {
                String vsRouteID = m.remove("MOVEROUTE").toString();
                if (mpRouteServer.checkPath(vsRouteID, ipSTData.getStationName(), vsRouteID)) {
                    LoadData vpLD = Factory.create(LoadData.class);
                    vpLD.dataToSKDCData(m);
                    // skip the load if it is already on the list
                    String vsLoadId = vpLD.getLoadID();
                    if (vpRetrievePendingLoadIdList.isEmpty() == false
                            && vpRetrievePendingLoadIdList.contains(vsLoadId)) {
                        continue;
                    }
                    // add the load to the list
                    vpRetrievePendingLoadIdList.add(vsLoadId);
                    vpRetrievePendingLoads.add(vpLD);
                }
            }
        } catch (Exception e) {
            logException(e, "Exception Getting Retrieve Pending Loads for " + ipSTData.getStationName() + " - "
                    + msMyClass + ".getRetrievePendingLoads()");
        }
        if (vpRetrievePendingLoads.isEmpty()) {
            return null;
        } else {
            return vpRetrievePendingLoads;
        }
    }

    /**
     * Auto pick a load going to a station. It is assumed the the next address is a valid station.
     * 
     * @param loadData
     */
    public void autoPickLoad(LoadData loadData) {
        initializeStationServer();
        initializePickServer();
        initializeInventoryServer();

        if (mpStationServer.isStationAutoPick(loadData.getNextAddress())) {
            logDebug("LoadId \"" + loadData.getLoadID() + "\" AutoPick load - " + msMyClass + ".autoPickLoad()");
            try {
                String sItem = "Unknown";
                int iTrayStatus = EBSConstants.BCS_TRAY_UNKNOWN;

                // Get item to be passed to doProject...
                List<Map> idarray = mpInvServer.getLoadLineItemDataListByLoadID(loadData.getLoadID());

                if (idarray.size() > 0) {
                    LoadLineItemData currItemDet = Factory.create(LoadLineItemData.class);
                    currItemDet.dataToSKDCData(idarray.get(0));
                    sItem = currItemDet.getItem();
                    if (sItem.equals(EBSConstants.EMPTY_TRAY_STACK)) {
                        iTrayStatus = EBSConstants.BCS_TRAY_STACK;
                    } else {
                        iTrayStatus = EBSConstants.BCS_TRAY_OCCUPIED;
                    }
                }

                initializePickServer();
                mpPickServer.autoPickLoadFromStation(loadData.getLoadID());

                doProjectSpecificCleanUpForLoadDeletion(loadData, sItem, iTrayStatus);
            } catch (DBException e) {
                logError("EBSSchedulerServer.autoPickLoad() exception: " + e.getMessage());
            }
        }
    }

    /**
     * Auto pick a load going to a station. It is assumed the the next address is a valid station.
     * 
     * @param loadData
     */
    public void autoPickArrivedLoad(LoadData loadData) {
        initializeStationServer();
        initializePickServer();
        initializeInventoryServer();

        if (mpStationServer.isStationAutoPick(loadData.getAddress())) {
            logOperation(
                    "LoadId \"" + loadData.getLoadID() + "\" AutoPick Arrived load - " + msMyClass + ".autoPickLoad()");
            try {
                String sItem = "Unknown";
                int iTrayStatus = EBSConstants.BCS_TRAY_UNKNOWN;

                // Get item to be passed to doProject...
                List<Map> idarray = mpInvServer.getLoadLineItemDataListByLoadID(loadData.getLoadID());

                if (idarray.size() > 0) {
                    LoadLineItemData currItemDet = Factory.create(LoadLineItemData.class);
                    currItemDet.dataToSKDCData(idarray.get(0));
                    sItem = currItemDet.getItem();
                    if (sItem.equals(EBSConstants.EMPTY_TRAY_STACK)) {
                        iTrayStatus = EBSConstants.BCS_TRAY_STACK;
                    } else {
                        iTrayStatus = EBSConstants.BCS_TRAY_OCCUPIED;
                    }
                }

                initializePickServer();
                mpPickServer.autoPickLoadFromStation(loadData.getLoadID());

                doProjectSpecificCleanUpForLoadDeletion(loadData, sItem, iTrayStatus);
            } catch (DBException e) {
                logError("EBSSchedulerServer.autoPickLoad() exception: " + e.getMessage());
            }
        }
    }

    /**
     * A generic function for inserting project-specific code into a load deletion method inside of the transaction.
     * <b>Note:</b><i>The Load may have already been deleted by the time this method is called. This stub exists for
     * clean up of any related tables that are project specific or otherwise.
     *
     * @param ipLoadData preserved load data regardless of whether load still exists.
     */
    protected void doProjectSpecificCleanUpForLoadDeletion(LoadData ipLoadData, String sItem, int iTrayStatus)
            throws DBException {
        String isBCSDeviceid = "";

        initializeBCSServer();
        String sFinalDest = ipLoadData.getFinalAddress().trim();
        String sWarehouse = ipLoadData.getWarehouse();
        String sCurrAddress = ipLoadData.getAddress();
        String sOutputStation = ipLoadData.getNextAddress();
        String sTrayID = ipLoadData.getLoadID();

        if (!sFinalDest.isEmpty()) {
            String isInfo = BCSMessage.BCS_TRAY_DESTINATION + "," + sTrayID + "," + sFinalDest + "," + sItem + ","
                    + iTrayStatus + "," + sOutputStation;
            if (mpStationServer.exists(sOutputStation)) {
                isBCSDeviceid = mpBCSServer.getStationsBCSDevice(sOutputStation);
            } else {
                isBCSDeviceid = mpBCSServer.getLocationsBCSDevice(sWarehouse, sCurrAddress);
            }

            mpBCSServer.sendEventToBCSDeviceHandler(isBCSDeviceid, isInfo);
        }
    }

    /**
     * Join an arrival pending with an ID pending load at a station
     * 
     * @param isInputStation the input station that has the id pending load.
     *
     * @return <code>true</code> if successful.
     */
    public boolean checkToJoinIDPendingLoad(String isInputStation) throws DBException {
        initializeLoadServer();
        initializeStationServer();

        boolean vzRtn = false;
        String vsStationWarhse = mpStationServer.getStationWarehouse(isInputStation);
        LoadData IDPendingLoad = mpLoadServer.getOldestLoadData(vsStationWarhse, isInputStation, DBConstants.IDPENDING);
        if (IDPendingLoad == null) {
            // no IDPending loads at this station so wait for arrival from equipment
            logDebug(msMyClass + ".joinLoads() - No IdPending Load");
        } else {
            // There is not be a BCR reader
            LoadData loadData = mpLoadServer.getOldestLoadData(vsStationWarhse, isInputStation,
                    DBConstants.ARRIVEPENDING);
            if (loadData == null) {
                /*
                 * Should never happen if screen tells me to move load should have arrive pending load
                 */
                logDebug("General move load message with no arrive pending load - " + msMyClass + ".joinLoads()");
            } else {
                vzRtn = changeLoadToStorePending(loadData, IDPendingLoad);
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
    protected boolean changeLoadToStorePending(LoadData loadData, LoadData idPend) throws DBException {
        initializeEBSLoadServer();
        initializeInventoryServer();

        // Check for multiple ArrivalPending loads
        List<LoadData> arrList = mpEBSLoadServer.getLoadAtAddressList(loadData.getWarehouse(), loadData.getAddress(),
                DBConstants.ARRIVEPENDING);
        if (arrList.size() > 1) {
            String vsStation = loadData.getAddress();

            // no IDPending loads at this station so wait for arrival from equipment
            logError(msMyClass + ".changeLoadToStorePending() - Tray WTF!" + loadData.getLoadID() + " NOT Storing, "
                    + " Mulitiple Trays in ArrivalPending Status at station " + loadData.getAddress());

            // MCM Sep2020
            // Delete the ArrivalPending's and create a dummy load to reject
            // so that we somewhat autorecover this scenario
            LoadData currRow = null;
            String sLoadID = "";
            for (int ldIdx = 0; ldIdx < arrList.size(); ldIdx++) {
                currRow = arrList.get(ldIdx);
                sLoadID = currRow.getLoadID();
                mpEBSLoadServer.deleteShippingLoad(sLoadID, "DUP");
                logError(msMyClass + ".changeLoadToStorePending() - Tray " + sLoadID + " DELETED due to, "
                        + " Mulitiple Trays in ArrivalPending Status at station " + vsStation);
            }
            // Add an IDPending load at this station, it will get rejected
            logError(msMyClass + ".changeLoadToStorePending() - Adding IDPending Tray to REJECT due to, "
                    + " Mulitiple Trays in ArrivalPending Status at station " + vsStation);

            createIDPendingLoad(vsStation);

            return true;
        }

        if (idPend != null) {
            // delete IDpending load
            mpInvServer.deleteLoad(idPend.getLoadID(), ReasonCode.getDaifukuReasonCode());
        }
        setLoadDataMoveStatus(loadData, DBConstants.STOREPENDING);
        mpEBSLoadServer.updateLoadData(loadData, true);
        return true;
    }

    /**
     * Update Load for a Store Arrival at a Station
     *
     * @param ipLEDF
     * @param ipStationData
     * @return
     */
    protected int updateLoadForStoreArrivalAtRegularStation(LoadEventDataFormat ipLEDF, StationData ipStationData) {
        String vsMethodInfo = msMyClass + ".updateLoadForStoreArrivalAtRegularStation()";

        initializeStationServer();

        LoadData vpLoadData = Factory.create(LoadData.class);

        try {
            // Not all zeros, and not empty string
            if (ipLEDF.isBCRValid()) {
                vpLoadData = getLoadWithValidBCR(ipLEDF, ipStationData);
            } else // Don't have BCR reader. Get oldest ARRIVEPENDING Load
            {
                vpLoadData = getLoadWithoutBCR(ipLEDF, ipStationData);
            }
        } catch (DBException dbe) {
            return STORE_LOAD_FAILED;
        } catch (InvalidDataException ive) {
            logError("##EXCEPTION## Invalid Data Exception " + ive.getMessage());
            return STORE_LOAD_FAILED;
        }

        if (vpLoadData == null) {
            /*
             * We have an arrival, but couldn't identify the load
             */
            createIDPendingLoad(ipLEDF);
            return STORE_LOAD_FAILED;
        } else if (vpLoadData.getLoadMoveStatus() != DBConstants.ARRIVEPENDING
                && !mpStationServer.isStationAutoStore(ipLEDF.getSourceStation())) {
            /*
             * Don't care about ID Pending loads at an AutoStore Station at this point
             *
             * Load is NOT arrive pending. No release done yet, so create ID Pending load.
             */
            logDebug("LoadId \"" + vpLoadData.getLoadID() + "\" is not arrive pending - " + vsMethodInfo);
            createIDPendingLoad(ipLEDF);
            return STORE_LOAD_FAILED;
        } else if (vpLoadData.getLoadMoveStatus() == DBConstants.ARRIVEPENDING) {
            /*
             * Load is created and arrived Pending so store it
             */
            // MCM, EBS Mar2020
            // The dimesion info in the dummy arrival is incorrect so ignore it
            // Update the height
//		      if (vpLoadData.getHeight() != ipLEDF.getDimensionInfo())
//		      {
//		        logDebug("LoadId \"" + vpLoadData.getLoadID()
//		            + "\" Arrived Load height is different - " + vsMethodInfo);
//		        vpLoadData.setHeight(ipLEDF.getDimensionInfo());
//		      }

            // Check for multiple ArrivalPending loads
            List<LoadData> arrList;
            try {
                initializeEBSLoadServer();
                arrList = mpEBSLoadServer.getLoadAtAddressList(vpLoadData.getWarehouse(), vpLoadData.getAddress(),
                        DBConstants.ARRIVEPENDING);

                if (arrList.size() > 1) {
                    // no IDPending loads at this station so wait for arrival from equipment
                    logError(msMyClass + ".updateLoadForStoreArrivalAtRegularStation() - Tray " + vpLoadData.getLoadID()
                            + " NOT Storing, " + " Mulitiple Trays in ArrivalPending Status at station "
                            + vpLoadData.getAddress());
                    String vsStation = vpLoadData.getAddress();
                    // MCM Sep2020
                    // Delete the ArrivalPending's and create a dummy load to reject
                    // so that we somewhat autorecover this scenario
                    LoadData currRow = null;
                    String sLoadID = "";
                    for (int ldIdx = 0; ldIdx < arrList.size(); ldIdx++) {
                        currRow = arrList.get(ldIdx);
                        sLoadID = currRow.getLoadID();
                        mpEBSLoadServer.deleteShippingLoad(sLoadID, "DUP");
                        logError(msMyClass + ".updateLoadForStoreArrivalAtRegularStation() - Tray " + sLoadID
                                + " DELETED due to, " + " Mulitiple Trays in ArrivalPending Status at station "
                                + vsStation);
                    }
                    // Add an IDPending load at this station, it will get rejected
                    logError(msMyClass
                            + ".updateLoadForStoreArrivalAtRegularStation() - Adding IDPending Tray to REJECT due to, "
                            + " Mulitiple Trays in ArrivalPending Status at station " + vsStation);
                    createIDPendingLoad(vsStation);
                    return STORE_LOAD_FAILED;
                }
            } catch (DBException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // Load presence?
            if (vpLoadData.getLoadPresenceCheck() != ipLEDF.getResults()) {
                logDebug("LoadId \"" + vpLoadData.getLoadID() + "\" Arrived Load Presence is different - "
                        + vsMethodInfo);
            }

            // If next is a station, make it MOVEPENDING, otherwise STOREPENDING
            if (mpStationServer.exists(vpLoadData.getNextAddress())) {
                setLoadDataMoveStatus(vpLoadData, DBConstants.MOVEPENDING);
            } else {
                setLoadDataMoveStatus(vpLoadData, DBConstants.STOREPENDING);
            }
            logDebug("LoadId \"" + vpLoadData.getLoadID() + "\" LoadData Updated - " + vsMethodInfo);
            mpLoadServer.updateLoadData(vpLoadData, true);
            return STORE_LOAD_OK;
        } else if (vpLoadData.getLoadMoveStatus() == DBConstants.STOREPENDING) {
            /*
             * Load is created (probably by auto-store) and is store-pending
             */
            return STORE_LOAD_OK;
        } else {
            /*
             * The scheduler doesn't need to do anything (ID Pending or Store Error)
             */
            return STORE_LOAD_FAILED;
        }
    }

    /**
     * No load created yet, just put on conveyor. Store not completed, so create an ID Pending load.
     *
     * @param ipLEDF
     */
    public void createIDPendingLoad(String isStation) {
        String vsMethodInfo = msMyClass + ".createIDPendingLoad()";

        logDebug("Create ID Pending Load - " + vsMethodInfo);
        LoadData IDPendingLoad = Factory.create(LoadData.class);
        List<Object> stationDefaults = mpStationServer.getStationDefaults(isStation);

        IDPendingLoad.setWarehouse((String) stationDefaults.get(0));
        IDPendingLoad.setDeviceID((String) stationDefaults.get(1));
        IDPendingLoad.setContainerType((String) stationDefaults.get(2));
        IDPendingLoad.setAddress(isStation);
        IDPendingLoad.setHeight(2);
        IDPendingLoad.setBCRData("");
        IDPendingLoad.setLoadMoveStatus(DBConstants.IDPENDING);

        mpLoadServer.createRandomLoad("IP", IDPendingLoad);

        logDebug("LoadId \"" + IDPendingLoad.getLoadID() + "\" at Station " + isStation
                + " created as new random IdPending Load - " + vsMethodInfo);
    }

    
    @Override
    protected void emptyFromLocation(String isWarehouse, String isAddress, String isPosition) throws DBException {
        // DK: Don't do anything because each location hold more than 1 bag in the location so if the bag is relocated
        // from one location from another
//			 Cannot make directly empty flag to the location. so disabling this functionality.
        LocationData vpFromLoc = mpEBSLocationServer.getLocationRecord(isWarehouse, isAddress);
        if (vpFromLoc != null && vpFromLoc.getLocationType() == DBConstants.LCASRS) {
            mpLocServer.setLocationEmptyFlag(isWarehouse, isAddress, isPosition, DBConstants.UNOCCUPIED);
        } else if (vpFromLoc != null && (vpFromLoc.getLocationType() == EBSDBConstants.Location.LOCATION_TYPE.STANDARD
                || vpFromLoc.getLocationType() == EBSDBConstants.Location.LOCATION_TYPE.OVER_SIZE)) {
            mpLocServer.setLocationEmptyFlag(isWarehouse, isAddress, isPosition,
                    EBSDBConstants.Location.EMPTY_FLAG.EMPTY);
        }
    }

    /**
     * This method will update the load and location information for the storage completion.
     * 
     * @param loadData
     */
    protected void moveLoadForStore(LoadData loadData) {
        initializeLoadServer();
        initializeLocationServer();

        try {
            if (loadData != null) {
                loadData.setMoveDate();
                String vsDeviceId = mpLocServer.getLocationDeviceId(loadData.getWarehouse(), loadData.getAddress());
                loadData.setDeviceID(vsDeviceId);
                mpLoad.updateLoadInfo(loadData);
                mpLocServer.setLocationEmptyFlag(loadData.getWarehouse(), loadData.getAddress(),
                        loadData.getShelfPosition(), EBSDBConstants.Location.EMPTY_FLAG.FULL);

            } else {
                logError("LoadId \"null \" Error - " + msMyClass + ".moveLoadForStore()");
            }
        } catch (DBException e) {
            logException(e, "LoadId \"" + loadData.getParentLoadID()
                    + "\" (Parent) Exception Changing Parent Load Status - " + msMyClass + ".setParentLoadMoveStatus");
        }
    }

    protected void initializeBCSServer() {
        if (mpBCSServer == null) {
            mpBCSServer = Factory.create(BCSServer.class);
        }
    }

    protected void initializeEBSLoadServer() {
        if (mpEBSLoadServer == null) {
            mpEBSLoadServer = Factory.create(EBSLoadServer.class);
        }
    }

    protected void initializeEBSInventoryServer() {
        if (mpEBSInvServer == null) {
            mpEBSInvServer = Factory.create(EBSInventoryServer.class);
        }
    }

    protected void initializeEBSHostServer() {
        if (mpEBSHostServer == null) {
            mpEBSHostServer = Factory.create(EBSHostServer.class);
        }
    }

    protected void initializeEBSLocationServer() {
        if (mpEBSLocationServer == null) {
            mpEBSLocationServer = Factory.create(EBSLocationServer.class);
        }
    }

    protected void initializePurchaseOrderHeader() {
        if (vpEH == null) {
            vpEH = Factory.create(PurchaseOrderHeader.class);
        }
    }

    protected void initializeLocation() {
        if (location == null) {
            location = Factory.create(Location.class);
        }
    }

}
