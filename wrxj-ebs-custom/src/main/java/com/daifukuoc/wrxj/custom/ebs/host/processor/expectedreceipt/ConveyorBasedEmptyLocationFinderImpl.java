package com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
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
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.ConveyorTableJoin;
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



public class ConveyorBasedEmptyLocationFinderImpl implements EmptyLocationFinder {

    protected Logger logger = Logger.getLogger();
    private ConveyorTableJoin tableJoin = Factory.create(ConveyorTableJoin.class);
    private EBSInventoryServer inventoryServer = Factory.create(EBSInventoryServer.class);
    private EBSLoadServer loadServer = Factory.create(EBSLoadServer.class);
    private EBSPoReceivingServer poServer = Factory.create(EBSPoReceivingServer.class);
    private StandardStationServer stationServer = Factory.create(StandardStationServer.class);
    private EBSLocationServer locationServer = Factory.create(EBSLocationServer.class);

    public ConveyorBasedEmptyLocationFinderImpl() throws DBException {

    }

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
                if (locationDataOfExistingLoadData.getLocationStatus() == DBConstants.LCAVAIL 
                		&& locationDataOfExistingLoadData.getEmptyFlag() !=  DBConstants.FULL_LOCATION ) {
                	
                	return locationDataOfExistingLoadData.getAddress();
                	
                }else if (locationDataOfExistingLoadData.getEmptyFlag() == DBConstants.FULL_LOCATION) {
                    // The conveyor is full lets continue searching new location now
                	logger.logError("The Locaiton is full. LocaionID:"+locationDataOfExistingLoadData.getAddress()+
                			" - which was reserved for tray:"+ existingLoadData.getLoadID() );
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
        String locationId = findAnEmptyLocationFor( expectedReceiptMessageData) ;
        if(locationId != null && !locationId.isEmpty() && !locationId.isBlank())
        {
	        StationData stationData = null;
	        try {
	            stationData = getStationbyId(locationId);
	        } catch (DBException e) {
	            throw new LocationSearchingFailureException(
	                    "The input station of " + locationId + " is not found", e);
	        }
	        
	        try {
	            if (existingLoadLineItemData == null && existingLoadData == null) {
	            	// Create a new load on the found empty location
	            	createNewLoad(expectedReceiptMessageData, purchaseOrderLineData, stationData, locationId);
	            } else {
	                // Update the existing load for the reserved empty location
	                loadServer.updateReservedLocation(existingLoadData, locationId);
	            }
	            
	        } catch (DBException e) {
	            throw new LoadCreationOrUpdateFailureException("Failed to create a new load or update an existing load "
	                    + expectedReceiptMessageData.getLoadId() + " on the " + locationId, e);
	        }
	        
        }

        return locationId;

    }
    private void updateLocationBy(String sLocationID, int iEmptyFlag,String sWarehouse)
    {
    
    	LocationData vpLocData = Factory.create(LocationData.class);
		vpLocData.setKey(LocationData.WAREHOUSE_NAME, sWarehouse);
		vpLocData.setKey(LocationData.ADDRESS_NAME, sLocationID); 
    	vpLocData.setWarehouse(sWarehouse);
    	vpLocData.setAddress(sLocationID);
		vpLocData.setEmptyFlag(iEmptyFlag);
		StandardLocationServer vpLocServer = Factory.create(StandardLocationServer.class);
		try {
			vpLocServer.modifyLocation(vpLocData);
			
				//tableJoin.updateLocationBy(sLocationID, iEmptyFlag,sWarehouse);
		} catch (DBException e) {
			this.logger.logError("Failed to update the loation:"+sLocationID+" - Error-:"+ e.getMessage() );
			e.printStackTrace();
		}
    
    }
    /**
     * Find an entrance station of the given device
     * 
     * @param deviceId, device id
     * @return Entrance station data
     * @throws DBException When anything goes wrong
     */
    private StationData getStationbyId(String stationId) throws DBException {

        StationData stationData = stationServer.getStation(stationId);
        if(stationData != null )
        {
        	return stationData;
        }
        return null;
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
	
	// For Test case purpose
	public String findAnEmptyLocation(ExpectedReceiptMessageData expectedReceiptMessageData) {
		return findAnEmptyLocationFor(expectedReceiptMessageData);
	}
	
	 private String findAnEmptyLocationFor(ExpectedReceiptMessageData expectedReceiptMessageData) 
	 {
		 String emptyLocation = null;
		 
		 try 
		 {
			 Integer iReleaseWindowPeriodInMin = tableJoin.getReleaseWindowPeriodInMin();// 120; // release window period in minutes 
			 Boolean StoreNonOverlappingBags =tableJoin.shouldStoreNonOverlappingBags() ;// true; default
			 
			//find warehouse by Final Sort Location for next step
			 String sWH = findWarehouseByFinalSortLocation(expectedReceiptMessageData.getFinalSortLocation());
			 
			 if( sWH != null&& !sWH.isBlank()&& !sWH.isEmpty() )
			 {				 
				 //step1)If there is a bag from the same flight is stored in a lane, then the same lane is assigned.  
				 emptyLocation = tableJoin.findEmptyLocationForTheSameFlight(expectedReceiptMessageData,sWH);
				 if(emptyLocation != null && !emptyLocation.isBlank() && !emptyLocation.isEmpty())
				 {
					 return emptyLocation;
				 }
				 
				 /* step2) Release Time. 
			 		2.1 =>	If one or more non-empty lanes have a Lane Release Window that overlaps with the bags Flight Release Window, 
			 		then the first one is selected. 
			 		2.2 =>	If there are no lanes that overlap, or all overlapping lanes are full, then the first empty lane is selected, 
			 		and that lanes Lane Release Time and Lane Release Window is set to match the bags Flight Release Time and Flight Release Window.
				  */
				 
				 //execute 2.1 - try to find this in the specified WH first
				 emptyLocation = tableJoin.findEmptyLocationInOverlappingReleaseWindowBy(expectedReceiptMessageData,iReleaseWindowPeriodInMin,sWH);
				 if(emptyLocation != null && !emptyLocation.isBlank() && !emptyLocation.isEmpty())
				 {
					 return emptyLocation;
				 }

				 //execute 2.2 - there is no overlapping so let's find the empty lane in specified WH first
				 emptyLocation = tableJoin.findEmptyLaneBy(expectedReceiptMessageData,sWH);
				 if(emptyLocation != null && !emptyLocation.isBlank() && !emptyLocation.isEmpty())
				 {
					 return emptyLocation;
				 }
				//3. No match, No Space
				 /*
				  a.	If a bags Flight Release Window does not overlap with the Lane Release Window of any of the lanes and there are no empty lanes, 
				  		the bag may be stored in a lane that releases outside of its Flight Release Window, 
				  		depending on the Store Non Overlapping Bags configuration setting.
				  */
				if( StoreNonOverlappingBags ) 
				{
					emptyLocation = tableJoin.findEmptyLocationWhichHasClosestReleaseWindowBy(expectedReceiptMessageData,sWH);
					if(emptyLocation != null && !emptyLocation.isBlank() && !emptyLocation.isEmpty())
					{
						return emptyLocation;
					}
					//Last let's try just find a location in any lane in this WH
					emptyLocation = tableJoin.findLocationOnAnyLaneBy(expectedReceiptMessageData,sWH);
					if(emptyLocation != null && !emptyLocation.isBlank() && !emptyLocation.isEmpty())
					{
						return emptyLocation;
					}		
				}
				
				//OKay! failed to find an empty location in this WH!! let's continue searching in other WH
			 }
			 
			 //NOTE: We should not get here however we can try to find location in other WH
			
			 logger.logDebug("Faild to find the WH for this final sort location:"+expectedReceiptMessageData.getFinalSortLocation()+" - Continue searching in any WH" );
		
			//step1)If there is a bag from the same flight is stored in a lane, then the same lane is assigned.  
			 emptyLocation = tableJoin.findEmptyLocationForTheSameFlight(expectedReceiptMessageData,null);
			 if(emptyLocation != null && !emptyLocation.isBlank() && !emptyLocation.isEmpty())
			 {
				 return emptyLocation;
			 }
			//execute 2.1 - try to find this in any WH
			 emptyLocation = tableJoin.findEmptyLocationInOverlappingReleaseWindowBy(expectedReceiptMessageData,iReleaseWindowPeriodInMin,null);
			 if(emptyLocation != null && !emptyLocation.isBlank() && !emptyLocation.isEmpty())
			 {
				 return emptyLocation;
			 }
			 //execute 2.2 - there is no overlapping so let's find the empty lane in any WH
			 emptyLocation = tableJoin.findEmptyLaneBy(expectedReceiptMessageData,null);
			 if(emptyLocation != null && !emptyLocation.isBlank() && !emptyLocation.isEmpty())
			 {
				 return emptyLocation;
			 }
			//3. No match, No Space
			 /*
			  a.	If a bags Flight Release Window does not overlap with the Lane Release Window of any of the lanes and there are no empty lanes, 
			  		the bag may be stored in a lane that releases outside of its Flight Release Window, 
			  		depending on the Store Non Overlapping Bags configuration setting.
			  */
			 if( StoreNonOverlappingBags ) 
			 {
				emptyLocation = tableJoin.findEmptyLocationWhichHasClosestReleaseWindowBy(expectedReceiptMessageData,null);
				if(emptyLocation != null && !emptyLocation.isBlank() && !emptyLocation.isEmpty())
				{
					return emptyLocation;
				}
				//Last let's try just find a location in any lane in any WH
				emptyLocation = tableJoin.findLocationOnAnyLaneBy(expectedReceiptMessageData,null);
				if(emptyLocation != null && !emptyLocation.isBlank() && !emptyLocation.isEmpty())
				{
					return emptyLocation;
				}		
			 }
			 logger.logDebug("Totally Faild to find any location in any WH for order:"+expectedReceiptMessageData.getOrderId()+" - returning null!" );
			//OKay! it is totally failed! to find an empty location in any WH!! 
			 emptyLocation = null; // NO space!
		} catch (DBException e1) {
			logger.logError("Faild to find the location for "+expectedReceiptMessageData.getOrderId() );
		}

		 return emptyLocation;
	 }
	 
	 private String findWarehouseByFinalSortLocation(String sFindalSortLocation)
	 {
		String sWH = null;
		try {
			sWH = tableJoin.findWarehouseByFinalSortLocation(sFindalSortLocation);
		} catch (DBException e) {
			logger.logError("Failed to get the Warehouse from databse"+ e.getMessage());
		}
		
		return sWH;
	 }
	 private String findDefaultWarehouse()
	 {
		 String sWH = null;
		try 
		{
			//get the default WH
			sWH = tableJoin.getDefaultWarehouse();
		} catch (DBException e) {
			logger.logError("Failed to get the default Warehouse from databse"+ e.getMessage());
		}
			
		return sWH;
	 }

	@Override
	public void update(ExpectedReceiptMessageData expectedReceiptMessageData) throws InvalidExpectedReceiptException, LoadSearchingFailureException, DBException {
		// Validate ExpectedReceiptMessageData
        if (expectedReceiptMessageData == null) {
            throw new InvalidExpectedReceiptException("The expected receipt must not be null");
        }
        if (!expectedReceiptMessageData.isValid()) {
            throw new InvalidExpectedReceiptException("The expected receipt is not valid");
        }
        
        LoadData existingLoadData = loadServer.getLoad(expectedReceiptMessageData.getLoadId());
        if (existingLoadData == null) {
            throw new LoadSearchingFailureException(
                    "Failed to search a load:" + expectedReceiptMessageData.getLoadId());
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
        
        // Populate PurchaseOrderHeaderData
        PurchaseOrderHeaderData purchaseOrderHeaderData = null;
        try {
            purchaseOrderHeaderData = poServer.getPoHeaderRecord(expectedReceiptMessageData.getOrderId());
        } catch (DBException e) {
            throw new InvalidExpectedReceiptException(
                    "Failed to populate a purchase order header for " + expectedReceiptMessageData.getOrderId(), e);
        }
        

        if (purchaseOrderHeaderData.getOrderStatus() != DBConstants.ERCOMPLETE
        		&& existingLoadData.getLoadMoveStatus() == DBConstants.ARRIVEPENDING) {
        	
        	// Populate PurchaseOrderLineData
        	PurchaseOrderLineData purchaseOrderLineData = getPurchaseOrderLineData(expectedReceiptMessageData.getOrderId());
        	purchaseOrderLineData.setExpirationDate(ConversionUtil.convertDateStringToDate(expectedReceiptMessageData.getDefaultRetrievalDateTime()));
        	poServer.modifyPOLine(purchaseOrderLineData);

        	// Update POH
        	purchaseOrderHeaderData.setFinalSortLocationId(expectedReceiptMessageData.getFinalSortLocation());
        	poServer.modifyPOHead(purchaseOrderHeaderData);
        	
        	// Update Load Line Item Data
        	existingLoadLineItemData.setExpirationDate(ConversionUtil.convertDateStringToDate(expectedReceiptMessageData.getDefaultRetrievalDateTime()));
        	loadServer.modifyLoadLineItemData(existingLoadLineItemData);
        	
        	// Update Load Data
            existingLoadData.setFinalSortLocationID(expectedReceiptMessageData.getFinalSortLocation());
            loadServer.modifyLoad(existingLoadData);
            
        } else {
        	logger.logError("Can not update data for completed order id : " + expectedReceiptMessageData.getOrderId());
        }
        
        
	}

	@Override
	public void cancel(ExpectedReceiptMessageData expectedReceiptMessageData) throws InvalidExpectedReceiptException, LoadSearchingFailureException, DBException {
		// Validate ExpectedReceiptMessageData
        if (expectedReceiptMessageData == null) {
            throw new InvalidExpectedReceiptException("The expected receipt must not be null");
        }
        if (!expectedReceiptMessageData.isValid()) {
            throw new InvalidExpectedReceiptException("The expected receipt is not valid");
        }
        
        // Populate PurchaseOrderHeaderData
        PurchaseOrderHeaderData purchaseOrderHeaderData = null;
        try {
            purchaseOrderHeaderData = poServer.getPoHeaderRecord(expectedReceiptMessageData.getOrderId());
        } catch (DBException e) {
            throw new InvalidExpectedReceiptException(
                    "Failed to populate a purchase order header for " + expectedReceiptMessageData.getOrderId(), e);
        }
        
        LoadData loadData = loadServer.getLoad(expectedReceiptMessageData.getLoadId());
        if (loadData == null) {
        	throw new LoadSearchingFailureException(
        			"Failed to search a load:" + expectedReceiptMessageData.getLoadId());
        }
        
        if (purchaseOrderHeaderData.getOrderStatus() != DBConstants.ERCOMPLETE
        		&& loadData.getLoadMoveStatus() == DBConstants.ARRIVEPENDING) {

        	inventoryServer.processCancelERMessage(expectedReceiptMessageData.getLoadId());
        	
        } else {
        	logger.logError("Failed to cancel a purchase order header for completed order id : " + expectedReceiptMessageData.getOrderId());
        }
        
	}
}
