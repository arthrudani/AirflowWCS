package com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt;

import java.text.ParseException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSInventoryServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLoadServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLocationServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSPoReceivingServer;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.ACPTableJoin;
import com.daifukuoc.wrxj.custom.ebs.host.messages.ExpectedReceiptMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.AlreadyStoredLoadException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.InvalidExpectedReceiptException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.LoadCreationOrUpdateFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.LoadSearchingFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.LocationReservationFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.LocationSearchingFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.NoRemainingEmptyLocationException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.POCreationFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.StationSearchingFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.util.ConversionUtil;
import com.daifukuoc.wrxj.custom.ebs.host.util.EBSHostMessageConstants;

/**
 * Empty location finder implementation
 * 
 * @author LK
 *
 */
public class AisleBasedEmptyLocationFinderImpl implements EmptyLocationFinder {

    private EBSPoReceivingServer poServer = Factory.create(EBSPoReceivingServer.class);
    private EBSLocationServer locationServer = Factory.create(EBSLocationServer.class);
    private EBSLoadServer loadServer = Factory.create(EBSLoadServer.class);
    private StandardStationServer stationServer = Factory.create(StandardStationServer.class);
    private EBSInventoryServer inventoryServer = Factory.create(EBSInventoryServer.class);
    private ACPTableJoin tableJoin = Factory.create(ACPTableJoin.class);
    protected Logger logger = Logger.getLogger();

    @Override
    public String find(ExpectedReceiptMessageData expectedReceiptMessageData) throws NoRemainingEmptyLocationException,
            LocationSearchingFailureException, InvalidExpectedReceiptException, LoadCreationOrUpdateFailureException,
            LoadSearchingFailureException, AlreadyStoredLoadException, StationSearchingFailureException,
            LocationReservationFailureException, POCreationFailureException {

        // Validate ExpectedReceiptMessageData
        if (expectedReceiptMessageData == null) {
            throw new InvalidExpectedReceiptException("The expected receipt must not be null");
        }
        if (!expectedReceiptMessageData.isValid()) {
            throw new InvalidExpectedReceiptException("The expected receipt is not valid");
        }

        LoadLineItemData existingLoadLineItemData = null;
        try {
            existingLoadLineItemData = getLoadLineItemDataOfALoad(expectedReceiptMessageData.getLoadId(),
                    expectedReceiptMessageData.getLot(), expectedReceiptMessageData.getLineId());
        } catch (DBException e) {
            throw new LoadSearchingFailureException(
                    "Failed to search a load line item: " + expectedReceiptMessageData.getLoadId()
                            + ", lot: " + expectedReceiptMessageData.getLot() + ", line id: "
                            + expectedReceiptMessageData.getLineId(),
                    e);
        }

        // Please note that getLoad() doesn't throw an exception. Instead it returns null when the search failed.
        LoadData existingLoadData = null;
        if (existingLoadLineItemData != null) {
            existingLoadData = loadServer.getLoad(expectedReceiptMessageData.getLoadId());
            if (existingLoadData == null) {
                throw new LoadSearchingFailureException(
                        "Failed to search a load:" + expectedReceiptMessageData.getLoadId());
            }
        }

        if (existingLoadLineItemData != null && existingLoadData != null) {
            LocationData locationDataOfExistingLoadData = null;
            try {
                locationDataOfExistingLoadData = locationServer
                        .getLocationRecord(EBSHostMessageConstants.WAREHOUSE_NAME, existingLoadData.getAddress());
            } catch (DBException e) {
                throw new LocationSearchingFailureException(
                        "Failed to get a location data of " + existingLoadData.getAddress(), e);
            }

            // The load is already created
            if (locationDataOfExistingLoadData != null) {
                // The location is available
                if (locationDataOfExistingLoadData.getLocationStatus() == DBConstants.LCAVAIL) {
                    // The location is still reserved for the bag
                    if (locationDataOfExistingLoadData.getEmptyFlag() == DBConstants.LCRESERVED) {
                        StationData stationData = null;
                        try {
                            stationData = findEntranceStationOfDevice(locationDataOfExistingLoadData.getDeviceID());
                        } catch (DBException e) {
                            throw new StationSearchingFailureException("The input station of "
                                    + locationDataOfExistingLoadData.getDeviceID() + " is not found", e);
                        }
                        if (stationData == null || stationData.getStationName().isEmpty()) {
                            throw new StationSearchingFailureException(
                                    "The input station of " + locationDataOfExistingLoadData.getDeviceID()
                                            + " shouldn't be empty or null");
                        }
                        // Now return the entrance station of the existing reserved location for the load
                        return stationData.getStationName();
                    } else if (locationDataOfExistingLoadData.getEmptyFlag() == DBConstants.UNOCCUPIED) {
                        // The location is empty for the bag, so change the empty flag to reserved
                        try {
                            locationServer.setLocationEmptyFlag(locationDataOfExistingLoadData.getWarehouse(),
                                    locationDataOfExistingLoadData.getAddress(),
                                    locationDataOfExistingLoadData.getShelfPosition(), DBConstants.LCRESERVED);
                        } catch (DBException e) {
                            throw new LocationReservationFailureException(
                                    "Failed to reserve the location " + locationDataOfExistingLoadData.getAddress(), e);
                        }

                        StationData stationData = null;
                        try {
                            stationData = findEntranceStationOfDevice(locationDataOfExistingLoadData.getDeviceID());
                        } catch (DBException e) {
                            throw new StationSearchingFailureException("The input station of "
                                    + locationDataOfExistingLoadData.getDeviceID() + " is not found", e);
                        }
                        if (stationData == null || stationData.getStationName().isEmpty()) {
                            throw new StationSearchingFailureException(
                                    "The input station of " + locationDataOfExistingLoadData.getDeviceID()
                                            + " shouldn't be empty or null");
                        }

                        // Now return the entrance station of the location linked for the load as the location is
                        // reserved again for the load
                        return stationData.getStationName();
                    } else if (locationDataOfExistingLoadData.getEmptyFlag() == DBConstants.OCCUPIED) {
                        // The bag is already at the location, so let's throw an exception to reply a response with
                        // error status
                        throw new AlreadyStoredLoadException("The load " + existingLoadData.getLoadID()
                                + " is already at the location " + locationDataOfExistingLoadData.getAddress());
                    }
                }
            }
        }

        // Now we have to find an empty location for a new or an existing load
        // Case 1. The ER for the load was never processed before
        // Case 2. The location of the load for the ER is set to unavailable or prohibited, so it's necessary to
        // find another location for the existing load
        // Case 3. The load's location is not valid, so it's necessary to find another location for the existing load

        // Populate PurchaseOrderHeaderData
        PurchaseOrderHeaderData purchaseOrderHeaderData = null;
        try {
            purchaseOrderHeaderData = poServer.getPoHeaderRecord(expectedReceiptMessageData.getOrderId());
        } catch (DBException e) {
            throw new InvalidExpectedReceiptException(
                    "Failed to populate a purchase order header for " + expectedReceiptMessageData.getOrderId(), e);
        }
        // Add a new PO(header and line) only if PO is not created yet
        if (purchaseOrderHeaderData == null) {
            try {
                poServer.addPOExpectedReceipt(expectedReceiptMessageData);
            } catch (ParseException e) {
                throw new POCreationFailureException("Failed to create a new purchase order header/line for "
                        + expectedReceiptMessageData.getOrderId(), e);
            }
        }

        // Populate PurchaseOrderLineData
        PurchaseOrderLineData purchaseOrderLineData = getPurchaseOrderLineData(expectedReceiptMessageData.getOrderId());

        // Find an empty location
        // FIXME: How do we determine warehouse from expected receipt message?
        LocationData locationData = findAnEmptyLocationForLot(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(), expectedReceiptMessageData.getFlightScheduledDateTime());
        if (locationData == null || locationData.getDeviceID() == null || locationData.getDeviceID().isEmpty()
                || locationData.getAddress() == null || locationData.getAddress().isEmpty()) {
            throw new LocationSearchingFailureException("Failed to search an empty location");
        }

        StationData stationData = null;
        try {
            stationData = findEntranceStationOfDevice(locationData.getDeviceID());
        } catch (DBException e) {
            throw new LocationSearchingFailureException(
                    "The input station of " + locationData.getDeviceID() + " is not found", e);
        }
        if (stationData == null || stationData.getStationName().isEmpty()) {
            throw new LocationSearchingFailureException(
                    "The input station of " + locationData.getDeviceID() + " shouldn't be empty or null");
        }

        try {
            if (existingLoadLineItemData == null && existingLoadData == null) {
            	// Create a new load on the found empty location
            	createNewLoad(expectedReceiptMessageData, purchaseOrderLineData, stationData, locationData.getAddress());
            } else {
                // Update the existing load for the reserved empty location
                loadServer.updateReservedLocation(existingLoadData, locationData.getAddress());
            }
        } catch (DBException e) {
            throw new LoadCreationOrUpdateFailureException("Failed to create a new load or update an existing load "
                    + expectedReceiptMessageData.getLoadId() + " on the " + locationData.getAddress(), e);
        }

        // Now return the entrance station of the found empty location for the load
        return stationData.getStationName();
    }

    /**
     * Populate PO line data of the given order id
     * 
     * @param orderId the order id
     * @return PO line data of the given order id
     * @throws InvalidExpectedReceiptException
     */
    private PurchaseOrderLineData getPurchaseOrderLineData(String orderId) throws InvalidExpectedReceiptException {
        PurchaseOrderLineData purchaseOrderLineData = Factory.create(PurchaseOrderLineData.class);
        List<Map> purchaseOrderLineDataList = poServer.getPurchaseOrderLine(orderId, null, null);
        if (purchaseOrderLineDataList == null || purchaseOrderLineDataList.isEmpty()) {
            throw new InvalidExpectedReceiptException("Failed to populate purchase order lines for " + orderId);
        }
        for (Map purchaseOrderLineMap : purchaseOrderLineDataList) {
            // Convert the populated map to PurchaseOrderLineData
            purchaseOrderLineData.dataToSKDCData(purchaseOrderLineMap);
            // Please note that currently we will have only 1 item on 1 tray
            break;
        }
        return purchaseOrderLineData;
    }

    /**
     * Populate load line item data of the given load
     * 
     * @param loadId the id of the load
     * @param lotId the lot id of the load
     * @param lineId the line id of the load
     * @return load line item data of the given load
     * @throws DBException
     */
    private LoadLineItemData getLoadLineItemDataOfALoad(String loadId, String lotId, String lineId) throws DBException {
        LoadLineItemData loadLineItemData = null;

        List<Map> loadLineItems = inventoryServer.getLoadLineItemDataListByLoadID(loadId);
        if (loadLineItems != null && loadLineItems.size() > 0) {
            for (Map loadLineItem : loadLineItems) {
                String existingLotId = DBHelper.getStringField(loadLineItem, LoadLineItemData.LOT_NAME);
                String existingLineId = DBHelper.getStringField(loadLineItem, LoadLineItemData.LINEID_NAME);
                if (existingLotId != null && existingLotId.equals(lotId) && existingLineId != null
                        && existingLineId.equals(lineId)) {
                    loadLineItemData = Factory.create(LoadLineItemData.class);
                    loadLineItemData.dataToSKDCData(loadLineItem);
                    break;
                }
            }
        }

        return loadLineItemData;
    }

    /**
     * Find an empty location for the given lot
     * 
     * @param warehouse warehouse name
     * @param lotId lot id
     * @param flightScheduledDateTime flight scheduled datetime
     * @return the found empty location data
     * @throws LocationSearchingFailureException
     * @throws NoRemainingEmptyLocationException
     */
    private LocationData findAnEmptyLocationForLot(String warehouse, String lotId, String flightScheduledDateTime)
            throws LocationSearchingFailureException, NoRemainingEmptyLocationException {

        // Get the number of load of the lot id per device id(= per aisle, as device takes care of each aisle)
        Map<String, Integer> currentLoadsPerDevice;
        try {
            currentLoadsPerDevice = tableJoin.getNumberOfLoadPerDeviceId(warehouse, lotId,
                    ConversionUtil.convertDateStringToDate(flightScheduledDateTime));
        } catch (DBException e) {
            throw new LocationSearchingFailureException("Failed to get the number of load of " + lotId, e);
        }

        // Sort by number of load as we will put load to an aisle which has the least number of load for the given lot
        Map<String, Integer> currentLoadsPerDeviceSortedByLoadCount = currentLoadsPerDevice.entrySet().stream()
                .sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        // Search an unoccupied location using the device list sorted by the number of load
        LocationData emptyLocation = null;
        for (Map.Entry<String, Integer> entry : currentLoadsPerDeviceSortedByLoadCount.entrySet()) {
            LocationData reservedLocation = reserveUnoccupiedLocationOfDevice(warehouse, lotId, entry.getKey());
            if (reservedLocation != null) {
                emptyLocation = reservedLocation;
                break;
            }
        }

        // Throw an exception if no location is found
        if (emptyLocation == null) {
            throw new NoRemainingEmptyLocationException("There's no empty location for " + lotId);
        }
        return emptyLocation;
    }

    /**
     * Reserve 1 unoccupied location in the area managed by the device
     * 
     * @param warehouse warehouse name
     * @param lotId lot id
     * @param deviceId device id
     * @return The reserved location data
     * @throws LocationSearchingFailureException
     * @throws NoRemainingEmptyLocationException
     */
    private LocationData reserveUnoccupiedLocationOfDevice(String warehouse, String lotId, String deviceId)
            throws LocationSearchingFailureException, NoRemainingEmptyLocationException {
        // Get the number of load of the lot id per Level
        Map<String, Integer> currentLoadsPerLevel;
        try {
        	currentLoadsPerLevel = tableJoin.getNumberOfLoadPerLevel(warehouse, lotId, deviceId);
        } catch (DBException e) {
            throw new LocationSearchingFailureException(
                    "Failed to get the number of load of " + deviceId + " for " + lotId, e);
        }

        LocationData emptyLocation = null;
        for (Map.Entry<String, Integer> entry : currentLoadsPerLevel.entrySet()) {
            LocationData reservedLocation;
            try {
                reservedLocation = reserveUnoccupiedLocationOfLevelIfAvailable(warehouse, entry.getKey(),deviceId);
            } catch (DBException e) {
                throw new LocationSearchingFailureException("Failed to reserve an emtpy location " + entry.getKey(), e);
            }
            if (reservedLocation != null) {
                emptyLocation = reservedLocation;
                break;
            }
        }
  
        /*
        // Sort by number of load as we will put load to a bank which has the least number of load
        Map<String, Integer> currentLoadsPerBankByLoadCount = currentLoadsPerBank.entrySet().stream()
                .sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
                     

        // Search an unoccupied location using the bank list sorted by the number of load
        LocationData emptyLocation = null;
        for (Map.Entry<String, Integer> entry : currentLoadsPerBankByLoadCount.entrySet()) {
            LocationData reservedLocation;
            try {
                reservedLocation = reserveUnoccupiedLocationOfBankIfAvailable(warehouse, entry.getKey());
            } catch (DBException e) {
                throw new LocationSearchingFailureException("Failed to reserve an emtpy location " + entry.getKey(), e);
            }
            if (reservedLocation != null) {
                emptyLocation = reservedLocation;
                break;
            }
        }
  
         */
     
        return emptyLocation;
    }
    private LocationData reserveUnoccupiedLocationOfLevelIfAvailable(String warehouse, String level, String deviceID) throws DBException {
        return locationServer.reserveUnoccupiedLocationOfLevelIfAvailable(warehouse, level, deviceID);
    }
    /**
     * Reserve 1 unoccupied location in the bank managed by the device
     * 
     * @param warehouse warehouse name
     * @param bank bank
     * @return The reserved location data
     * @throws DBException When anything goes wrong
    */
    private LocationData reserveUnoccupiedLocationOfBankIfAvailable(String warehouse, String bank) throws DBException {
        return locationServer.reserveUnoccupiedLocationOfBankIfAvailable(warehouse, bank);
    } 

    /**
     * Find an entrance station of the given device
     * 
     * @param deviceId, device id
     * @return Entrance station data
     * @throws DBException When anything goes wrong
     */
    private StationData findEntranceStationOfDevice(String deviceId) throws DBException {
        StationData foundStation = null;

        List<Map> stationDataList = stationServer.getStationByDeviceList(deviceId);
        StationData stationData = Factory.create(StationData.class);
        for (Map stationMap : stationDataList) {
            // Convert the populated map to StationData
            stationData.dataToSKDCData(stationMap);

            // Check if the station type is input(224) or reversible(225)
            if (stationData.getStationType() == DBConstants.INPUT
                    || stationData.getStationType() == DBConstants.REVERSIBLE) {
                foundStation = stationData;
                break;
            }
        }
        return foundStation;
    }
    
    /**
     * Creates a new expected load on the reserved location
     * 
     * @param erMsgData				expected receipt message data
     * @param poLiData				purchase order line data
     * @param stnData				entrance station data
     * @param emptyLocAddress	 	address
     * @throws DBException When anything goes wrong
     */
	private void createNewLoad(ExpectedReceiptMessageData erMsgData, PurchaseOrderLineData poLiData,
			StationData stnData, String emptyLocAddress) throws DBException {
		// Load table
		LoadData loadData = Factory.create(LoadData.class);
		loadData.setParentLoadID(erMsgData.getLoadId());
		loadData.setLoadID(erMsgData.getLoadId());
		loadData.setWarehouse(stnData.getWarehouse());
		loadData.setAddress(emptyLocAddress);
		loadData.setContainerType(stnData.getContainerType());
		loadData.setDeviceID(stnData.getDeviceID());
		loadData.setAmountFull(DBConstants.EMPTY);
		loadData.setLoadMoveStatus(DBConstants.ARRIVEPENDING);
		loadData.setFinalSortLocationID(erMsgData.getFinalSortLocation());

		if (!loadServer.addExpectedLoad(loadData)) {
			throw new DBException("Failed to create a new load: " + loadData.toString());
		}
		
		inventoryServer.addNewItemToLoadForLot(erMsgData, poLiData);
	}

	@Override
	public void update(ExpectedReceiptMessageData expectedReceiptMessageData)
			throws InvalidExpectedReceiptException, LoadSearchingFailureException, DBException {
		logger.logDebug("Need to Implement For ShuttleRack");
	}

	@Override
	public void cancel(ExpectedReceiptMessageData expectedReceiptMessageData)
			throws InvalidExpectedReceiptException, LoadSearchingFailureException, DBException {
		logger.logDebug("Need to Implement For ShuttleRack");
	}
}
