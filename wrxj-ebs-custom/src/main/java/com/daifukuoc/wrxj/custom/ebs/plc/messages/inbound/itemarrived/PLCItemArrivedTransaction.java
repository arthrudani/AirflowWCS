package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemarrived;

import java.util.Date;
import java.util.Objects;

import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItem;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.MoveCommand;
import com.daifukuamerica.wrxj.dbadapter.data.MoveCommandData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSInventoryServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLocationServer;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSTableJoin;
import com.daifukuoc.wrxj.custom.ebs.host.messages.EBSItemReleaseMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.acp.route.EBSAddress;
import com.daifukuoc.wrxj.custom.ebs.plc.acp.route.RouteManagerFailureException;
import com.daifukuoc.wrxj.custom.ebs.plc.acp.route.RouteManagerImpl;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.StandardItemArrivedTransaction;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound.PLCMoveOrderMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.queue.CommandQueueManagerImpl;

/**
 * Item Arrived Transaction does process body of Item Arrived message [51]
 * 
 * @author ST
 *
 */
public class PLCItemArrivedTransaction extends StandardItemArrivedTransaction {
 
    protected EBSInventoryServer mpEBSInventory = Factory.create(EBSInventoryServer.class);
    protected StandardOrderServer mpStandardOrderServer = Factory.create(StandardOrderServer.class);
    protected Load load = Factory.create(Load.class);
    protected LoadLineItem loadLineItem = Factory.create(LoadLineItem.class);
    protected EBSTableJoin ebsTJ = Factory.create(EBSTableJoin.class);
    protected EBSLocationServer locationServer = Factory.create(EBSLocationServer.class);
    
    
    /**
     * Constructor.
     */
    public PLCItemArrivedTransaction() {
        super(true);
    }

    @Override
    protected void executeBody(ItemArrivedContext plcItemArrivedContext) throws DBException {
        if (plcItemArrivedContext.getStationData() == null) {
            logger.logError("Station Record for Station " + plcItemArrivedContext.getStationId() + " not found!");
            return;
        }

        if (plcItemArrivedContext.getLoadData() == null) {
			try {
				createUnknownLoadAndLoadLineItem(plcItemArrivedContext);
			} catch (DBException e) {
				logger.logError("Could not add load or load line item for given load id : " + plcItemArrivedContext.getLoadId());
				return;
			}
		}
        
        if (isOutBoundStation(plcItemArrivedContext)) {
        	completeReleaseProcess(plcItemArrivedContext);
        } else {
            doInProgressProcess(plcItemArrivedContext);
        }
        plcItemArrivedContext.setSuccess(true);
    }
    
    private void createUnknownLoadAndLoadLineItem(ItemArrivedContext plcItemArrivedContext) throws DBException {
		LoadData loadData = Factory.create(LoadData.class);
		loadData.setDeviceID(plcItemArrivedContext.getDeviceId());
		loadData.setLoadMoveStatus(DBConstants.TRANSFERRING_OUT);
		loadData.setCurrentAddress(plcItemArrivedContext.getStationId());
		loadData.setLoadID(plcItemArrivedContext.getLoadId());
		loadData.setParentLoadID(plcItemArrivedContext.getLoadId());
		loadData.setContainerType(plcItemArrivedContext.getStationData().getContainerType());
		loadData.setWarehouse(plcItemArrivedContext.getStationData().getWarehouse());
		LoadLineItemData loadLineItemData = Factory.create(LoadLineItemData.class);
		loadLineItemData.setLoadID(loadData.getLoadID());
		loadLineItemData.setLineID(PLCConstants.UNKNOWN_BARCODE);
		loadLineItemData.setItem(PLCConstants.UNKNOWN_ITEM);
		loadLineItemData.setCurrentQuantity(1);

		// Add unknown Load
		load.addElement(loadData);
		// Add unknown Load Line Item
		loadLineItem.addElement(loadLineItemData);
		
		// set load and load line item in context
		plcItemArrivedContext.setLoadData(loadData);
		plcItemArrivedContext.setLoadLineItemData(loadLineItemData);
	}

    boolean isOutBoundStation(ItemArrivedContext plcItemArrivedContext) {
        switch (plcItemArrivedContext.getStationData().getStationType()) {

        case DBConstants.OUTPUT:
            return true;

        case DBConstants.REVERSIBLE:
            switch (plcItemArrivedContext.getLoadData().getLoadMoveStatus()) {

            case DBConstants.RETRIEVEPENDING:
            case DBConstants.RETRIEVING:
                return true;

            default:
                return false;
            }

        default:
            return false;

        }

    }
    boolean isRetrieving (ItemArrivedContext plcItemArrivedContext) 
    {
    	return (plcItemArrivedContext.getLoadData().getLoadMoveStatus() == DBConstants.RETRIEVEPENDING 
    			|| plcItemArrivedContext.getLoadData().getLoadMoveStatus() == DBConstants.RETRIEVING);
    }

    void doInProgressProcess(ItemArrivedContext plcItemArrivedContext) throws DBException {
        try {
        	
            RouteManagerImpl routeManager = Factory.create(RouteManagerImpl.class);
            
            LoadData loadData = plcItemArrivedContext.getLoadData();
            StationData stationData = plcItemArrivedContext.getStationData();
            
			if (loadData.getLoadMoveStatus() != DBConstants.TRANSFERRING_OUT
					&& plcItemArrivedContext.getStationData().getStationType() == DBConstants.INPUT) {
				// check and handle if load arrived at wrong inbound station
				handleExceptionForWrongInboundStation(plcItemArrivedContext);

			} else if (plcItemArrivedContext.getStationData().getStationType() == DBConstants.SHUTTLE_TRANSFER_INPUT
					&& loadData.getLoadMoveStatus() != DBConstants.TRANSFERRING_OUT) {
				// check and handle if load arrived at wrong level
				handleExceptionForWrongLevel(plcItemArrivedContext);
			}
            
            String nextStation = routeManager.findNextDestination(plcItemArrivedContext.getLoadData(),
                    plcItemArrivedContext.getStationId());
            
            CommandQueueManagerImpl cmdQueueManager = Factory.create(CommandQueueManagerImpl.class);
            
            if (plcItemArrivedContext.getStationData().getStationType() == DBConstants.SHUTTLE_TRANSFER_INPUT 
            		|| plcItemArrivedContext.getStationData().getStationType() == DBConstants.LIFT_TRANSFER_OUT
            			|| plcItemArrivedContext.getStationData().getStationType() == DBConstants.LIFT_TRANSFER_REVERSIBLE) {
            	cmdQueueManager.update(plcItemArrivedContext.getLoadId(), DBConstants.CMD_COMPLETED);
            }
            MoveCommandData moveCommandData = constructMoveCommandData(plcItemArrivedContext, nextStation, 
            		routeManager.getMoveType(), ( isRetrieving(plcItemArrivedContext )?DBConstants.CMD_RETRIEVAL : DBConstants.CMD_STORAGE ));
            cmdQueueManager.enqueue(moveCommandData);

             if (loadData.getLoadMoveStatus() == DBConstants.ARRIVEPENDING) {
                loadData.setLoadMoveStatus(DBConstants.STOREPENDING);
            }

            // Update currentAddress to database
            loadData.setCurrentAddress(plcItemArrivedContext.getStationId());
            load.updateLoadInfo(loadData);
        } catch (RouteManagerFailureException ex) {
            logger.logError("Could not find the next station for " + plcItemArrivedContext.getStationId());

        }
    }

	private void handleExceptionForWrongInboundStation(ItemArrivedContext plcItemArrivedContext) throws DBException {
		// check load arrived at correct inbound station
		if (!checkLoadArrivedAtCorrectInboundStation(plcItemArrivedContext.getLoadData(),
				plcItemArrivedContext.getDeviceId(), plcItemArrivedContext.getStationData())) {
			// Find new location in any banks associated with device
			LocationData newLocationData = locationServer.reserveUnoccupiedLocationForDeviceIfAvailable(
					plcItemArrivedContext.getLoadData().getWarehouse(), plcItemArrivedContext.getDeviceId());
			locationServer.updateLocationEmptyFlag(plcItemArrivedContext.getLoadData().getWarehouse(),
					plcItemArrivedContext.getLoadData().getAddress(), DBConstants.LCAVAIL);
			if (Objects.isNull(newLocationData)) {
				// if not able to find the location take it out
				plcItemArrivedContext.getLoadData().setLoadMoveStatus(DBConstants.TRANSFERRING_OUT);
				plcItemArrivedContext.getLoadData().setAddress(null);
			} else {
				// update load address with new one
				plcItemArrivedContext.getLoadData().setAddress(newLocationData.getAddress());
				plcItemArrivedContext.getLoadData().setDeviceID(plcItemArrivedContext.getDeviceId());
			}
			
		}
	}

	private void handleExceptionForWrongLevel(ItemArrivedContext plcItemArrivedContext) throws DBException {
		// check load arrived at correct level
		if (!checkLoadArrivedAtCorrectLevel(plcItemArrivedContext.getLoadData().getAddress(),
				 plcItemArrivedContext.getStationData().getStationName())) {
			locationServer.updateLocationEmptyFlag(plcItemArrivedContext.getLoadData().getWarehouse(),
					plcItemArrivedContext.getLoadData().getAddress(), DBConstants.LCAVAIL);
			LocationData newLocationData = locationServer.reserveUnoccupiedLocationOfLevelIfAvailable(
					plcItemArrivedContext.getLoadData().getWarehouse(), EBSAddress.parseLevel(plcItemArrivedContext.getStationId()),
					plcItemArrivedContext.getDeviceId());
			if (Objects.isNull(newLocationData)) {
				// if not able to find the location take it out
				plcItemArrivedContext.getLoadData().setLoadMoveStatus(DBConstants.TRANSFERRING_OUT);
				plcItemArrivedContext.getLoadData().setAddress(null);
			} else {
				// update load address with new one
				plcItemArrivedContext.getLoadData().setAddress(newLocationData.getAddress());
			}
		}
		
	}
	
	private boolean checkLoadArrivedAtCorrectInboundStation(LoadData loadData, String deviceID, StationData stationData)
			throws DBException {
			String locationDeviceID = ebsTJ.getDeviceForLocationAddress(loadData.getAddress());
			return locationDeviceID.equals(deviceID);
	}

	/**
     * Method to construct MoveCommand with data
     * @param plcItemArrivedContext
     * @param nextStation - next station location
     * @param moveType - type of move 
     * @return MoveCommand with all the data
     */
    public MoveCommandData constructMoveCommandData(ItemArrivedContext plcItemArrivedContext, String nextStation, String moveType, Integer cmdOrderType) {
    	MoveCommandData moveCommandData = Factory.create(MoveCommandData.class);
        moveCommandData.setCreatedDate(new Date(System.currentTimeMillis()));
        moveCommandData.setLoadID(plcItemArrivedContext.getLoadId());
        moveCommandData.setFinalSortLocationID(plcItemArrivedContext.getLoadData().getFinalSortLocationID());
        moveCommandData.setCmdStatus(DBConstants.CMD_READY);
        moveCommandData.setGlobalID(plcItemArrivedContext.getGlobalId());
        moveCommandData.setOrderid(plcItemArrivedContext.getOrderId());
        moveCommandData.setCmdMoveType( (moveType.isEmpty()? 0 : Integer.valueOf(moveType) ));
        moveCommandData.setFrom(plcItemArrivedContext.getStationId());
        moveCommandData.setToDest(nextStation);
        moveCommandData.setCmdOrderType(cmdOrderType);
        moveCommandData.setItemID(plcItemArrivedContext.getLineId());
        moveCommandData.setFlightNum(plcItemArrivedContext.getLoadLineItemData().getLot());
        moveCommandData.setFlightSTD(plcItemArrivedContext.getLoadLineItemData().getExpectedDate());
        moveCommandData.setDeviceID(plcItemArrivedContext.getDeviceId());
		return moveCommandData;
	}

	/**
     * Populate the Move order object with the data
     * 
     * @param loadData
     * @param loadLineData
     * @return
     */
    public PLCMoveOrderMessage constructMoveOrderData(ItemArrivedContext plcItemArrivedContext, String nextStation,
            String moveType) {
        PLCMoveOrderMessage moveOrder = Factory.create(PLCMoveOrderMessage.class);
        moveOrder.setSerialNum(plcItemArrivedContext.getSerialNumber()); 
        moveOrder.setOrderId(plcItemArrivedContext.getLoadLineItemData().getOrderID());
        moveOrder.setLoadId(plcItemArrivedContext.getLoadData().getLoadID());
        moveOrder.setGlobalId(plcItemArrivedContext.getLoadLineItemData().getGlobalID());
        moveOrder.setLineId(plcItemArrivedContext.getLoadLineItemData().getLineID());
        moveOrder.setLot(plcItemArrivedContext.getLoadLineItemData().getLot());
        moveOrder.setFlightSchduledDateTime(plcItemArrivedContext.getLoadLineItemData().getExpirationDate());
        moveOrder.setFinalSortLocation(plcItemArrivedContext.getLoadData().getFinalSortLocationID());
        moveOrder.setFromLocation(plcItemArrivedContext.getStationId());
        moveOrder.setToLocation(nextStation);
        moveOrder.setDeviceId(plcItemArrivedContext.getDeviceId());
        moveOrder.setMoveType(moveType);
        return moveOrder;
    }

    void completeReleaseProcess(ItemArrivedContext plcItemArrivedContext) {
        try {
        	MoveCommand moveCommand = Factory.create(MoveCommand.class);
        	moveCommand.updateMoveCommandStatusByLoadId(plcItemArrivedContext.getLoadId(), DBConstants.CMD_DELETED);
        	
        	//publish this first before calling processReleasedLoadData which deletes all data
        	if(mpEBSInventory.mzHasHostSystem)
        	{
        		publishItemReleasedToHost(plcItemArrivedContext);
        	}

        	mpEBSInventory.processReleasedLoadData( plcItemArrivedContext);

        } catch (DBException e) {
            logger.logError(
                    "DB Exception item arrived: " + plcItemArrivedContext.getStationId() + ". Error:" + e.getMessage());
        }
    }
    private void publishItemReleasedToHost(ItemArrivedContext plcItemArrivedContext) {
    	EBSItemReleaseMessage mpResponseMsg = Factory
                .create(EBSItemReleaseMessage.class);
    	
        mpResponseMsg.setOrderID(plcItemArrivedContext.getOrderId());
        mpResponseMsg.setLoadID(plcItemArrivedContext.getLoadId());
        mpResponseMsg.setGlobalID(plcItemArrivedContext.getGlobalId());
        mpResponseMsg.setLineID(plcItemArrivedContext.getLineId());
        mpResponseMsg.setArrivalStation(plcItemArrivedContext.getStationId());
        mpResponseMsg.setStatus(identifiyReleaseStatusBy(plcItemArrivedContext ));

        mpResponseMsg.format();
        String messageToSend = new String(mpResponseMsg.prepareMessageToSend(0));
        //publish this to host
        ThreadSystemGateway.get().publishHostMesgSendEvent(messageToSend, 0,SACControlMessage.HOST_PORT_EVENT);
    }
    
    private int identifiyReleaseStatusBy( ItemArrivedContext plcItemArrivedContext)
    {
    	int releaseStatus = SACControlMessage.ItemRelease.STATUS.UNKNOWN_TRAY ; // unknown tray 
    	// try to find the order type
    	if( plcItemArrivedContext.getOrderId() != null &&  !plcItemArrivedContext.getOrderId().isEmpty())
    	{
    	
    		try {
    			OrderHeaderData mpOrderHeader = mpStandardOrderServer.getOrderHeaderRecord (plcItemArrivedContext.getOrderId());
    			
    			if(mpOrderHeader != null)
    			{
    				if(mpOrderHeader.getOrderType() == DBConstants.FULLLOADOUT)
    				{
    					releaseStatus = SACControlMessage.ItemRelease.STATUS.NORMAL_RETRIEVAL; // normal order from SAC
    				}else if(mpOrderHeader.getOrderType() == DBConstants.ITEMORDER)
    				{
    					releaseStatus = SACControlMessage.ItemRelease.STATUS.OPERATOR_RETRIEVAL; // order from web/desktop client screen 
    				}
    			}else
    			{
    				//Error! 
    				releaseStatus = SACControlMessage.ItemRelease.STATUS.ERROR_RECOVERY;
    			}
				
			} catch (DBException e) {
				
				logger.logError(
	                    "DB Exception item arrived -identifiyReleaseStatusBy: " + plcItemArrivedContext.getStationId() + ". Error:" + e.getMessage());
			}
    	}else
    	{
    		//there is no order associated with this release so
    		releaseStatus = SACControlMessage.ItemRelease.STATUS.UNKNOWN_TRAY;
    	}
    	
    	return releaseStatus;
    	
    }
    
    private boolean checkLoadArrivedAtCorrectInboundStation(String address, String deviceID) throws DBException {
		String locationDeviceID = ebsTJ.getDeviceForLocationAddress(address);
		
		return locationDeviceID.equals(deviceID);
	}
    
    private boolean checkLoadArrivedAtCorrectLevel(String loadAddress, String station) throws DBException {
    	return EBSAddress.parseLevel(loadAddress).equals(EBSAddress.parseLevel(station));
	}
}
