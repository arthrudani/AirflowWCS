package com.daifukuoc.wrxj.custom.ebs.dataserver;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDeallocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardMoveCommandServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadTransactionHistory;
import com.daifukuamerica.wrxj.dbadapter.data.LoadTransactionHistoryData;
import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.Move;
import com.daifukuamerica.wrxj.dbadapter.data.MoveCommand;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeader;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.device.gateway.SystemGateway;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.InvalidHostDataException;
import com.daifukuamerica.wrxj.host.messages.HostError;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.util.UnusedMethod;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.ConveyorTableJoin;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSTableJoin;
import com.daifukuoc.wrxj.custom.ebs.host.messages.EBSItemReleaseMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.EBSStoreCompletionNotifyMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.ExpectedReceiptMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.host.util.ConversionUtil;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSConstants;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSDBConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemarrived.ItemArrivedContext;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemreleased.ItemReleasedContext;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemstored.ItemStoredContext;

/**
 * A EBS server that provides methods and transactions for use in inventory that inherit from the
 * StandardInventoryServer. Methods used to add, modify and delete containers, loads, item masters and details are
 * provided. Transactions are wrapped around calls to the lower level data base objects.
 */
public class EBSInventoryServer extends StandardInventoryServer {
    private BCSServer mpBCSServer;
    private StandardStationServer mpStationServer;
    private EBSHostServer mpEBSHostServer;
    private EBSPoReceivingServer mpEBSPOReceivingServer;
    private EBSTableJoin mpTableJoin;
    private LoadLineItemData mpLLIData;
    private StandardMoveCommandServer mpMoveCommandServer;
    
    private StandardLocationServer standardLocationServer;
    private PurchaseOrderHeader purchaseOrderHeader;
    protected Load mpLoad ;
    private Location mpLocation;
    private ConveyorTableJoin conveyorTableJoin;
    protected LoadTransactionHistory mpLoadTransactionHistory = Factory.create(LoadTransactionHistory.class);
   

    /**
     * Constructor
     */
    public EBSInventoryServer() {
        super();
    }

    /**
     * Constructor
     *
     * @param keyName
     */
    public EBSInventoryServer(String keyName) {
        super(keyName);
    }

    /**
     * Web application constructor for per user connection pooling
     * 
     * @param keyName
     * @param dbo
     */
    public EBSInventoryServer(String keyName, DBObject dbo) {
        super(keyName, dbo);
    }
    protected void initializeLocation() {
        if (mpLocation == null) {
        	mpLocation = Factory.create(Location.class);
        }
    }
    protected void initializeLoad() {
        if (mpLoad == null) {
            mpLoad = Factory.create(Load.class);
        }
    }
    protected void initializePurchaseOrderHeader() {
        if (purchaseOrderHeader == null) {
        	purchaseOrderHeader = Factory.create(PurchaseOrderHeader.class);
        }
    }

    /**
     * Add a new item to the load for the given lot id(flight number)
     * 
     * @param erMsgData Expected receipt message data
     * @param poLiData purchase order line data
     */
    public void addNewItemToLoadForLot(ExpectedReceiptMessageData erMsgData, PurchaseOrderLineData poLiData) {
        TransactionToken tt = null;

        try {
            tt = startTransaction();

            // LoadLineItem table
    		Date dExpirationDate = ConversionUtil.convertDateStringToDate(erMsgData.getDefaultRetrievalDateTime());
    		Date dExpectedDate = ConversionUtil.convertDateStringToDate(erMsgData.getFlightScheduledDateTime());
            
            LoadLineItemData loadItem = Factory.create(LoadLineItemData.class);
            loadItem.setItem(poLiData.getItem());
            loadItem.setLot(poLiData.getLot());
            loadItem.setLoadID(erMsgData.getLoadId());
            loadItem.setLineID(poLiData.getLineID());
            loadItem.setExpirationDate(dExpirationDate);
            loadItem.setCurrentQuantity(1);
            loadItem.setAllocatedQuantity(0);
            loadItem.setExpectedReceipt(erMsgData.getOrderId());
            loadItem.setGlobalID(erMsgData.getGlobalId());
            loadItem.setExpectedDate(dExpectedDate);
            
            this.addLoadLI(loadItem);

            commitTransaction(tt);
        } catch (DBException e) {
            logException(e, "Exception in adding expected load Item to Load  \"" + erMsgData.getLoadId()
                    + "\"  - EBSInventoryServer.addItemToLoadforLot");
        } finally {
            endTransaction(tt);
        }
    }

    /**
     * Method to add a Unknown Item to a load.
     *
     * @param loadID no information available
     */
    public void addUnknownItem(String loadID) {
        TransactionToken tt = null;

        try {
            tt = startTransaction();
            final String binFull = EBSConstants.EBS_UNKNOWN_ITEM;
            LoadLineItemData unkItem = Factory.create(LoadLineItemData.class);
            unkItem.setLoadID(loadID);
            unkItem.setItem(binFull);
            unkItem.setCurrentQuantity(1);
            if (!mpIM.exists(binFull)) {
                addItemMasterFromString(binFull, "Default Unknown Item", false);
            }
            addLoadLI(unkItem);

            if (sendIAForBEBF()) {
                if (mzHasHostSystem) {
                    initializeHostServer();
                    mpHostServ.sendInventoryAdjust(unkItem, "");
                }
            }

            commitTransaction(tt);
        } catch (DBException e) {
            logException(e, "Exception adding Unknown Item to Load  \"" + loadID
                    + "\"  - StandardInventoryServer.addUnknownItem");
        } finally {
            endTransaction(tt);
        }
    }

    /**
     * Method to add a Unknown Item to a load.
     *
     * @param loadID no information available
     */
    public void addEmptyTrayStackItem(String loadID) {
        TransactionToken tt = null;

        try {
            tt = startTransaction();
            final String binFull = EBSConstants.EMPTY_TRAY_STACK;
            LoadLineItemData unkItem = Factory.create(LoadLineItemData.class);
            unkItem.setLoadID(loadID);
            unkItem.setItem(binFull);
            unkItem.setCurrentQuantity(EBSConstants.EMPTY_TRAY_STACK_QTY);
            if (!mpIM.exists(binFull)) {
                addItemMasterFromString(binFull, "Default Empty Tray Stack Item", false);
            }
            addLoadLI(unkItem);

            if (sendIAForBEBF()) {
                if (mzHasHostSystem) {
                    initializeHostServer();
                    mpHostServ.sendInventoryAdjust(unkItem, "");
                }
            }

            commitTransaction(tt);
        } catch (DBException e) {
            logException(e, "Exception adding Empty Tray Stack to Load  \"" + loadID
                    + "\"  - StandardInventoryServer.addEmptyTrayStackItem");
        } finally {
            endTransaction(tt);
        }
    }

    /**
     * Method to add a BadRead Item to a load.
     *
     * @param loadID no information available
     */
    public void addBadReadItem(String loadID) {
        TransactionToken tt = null;

        try {
            tt = startTransaction();
            final String binFull = EBSConstants.EBS_BADREAD_ITEM;
            LoadLineItemData unkItem = Factory.create(LoadLineItemData.class);
            unkItem.setLoadID(loadID);
            unkItem.setItem(binFull);
            unkItem.setCurrentQuantity(1);
            if (!mpIM.exists(binFull)) {
                addItemMasterFromString(binFull, "Default BadRead Item", false);
            }
            addLoadLI(unkItem);

            if (sendIAForBEBF()) {
                if (mzHasHostSystem) {
                    initializeHostServer();
                    mpHostServ.sendInventoryAdjust(unkItem, "");
                }
            }

            commitTransaction(tt);
        } catch (DBException e) {
            logException(e, "Exception adding BadRead Item to Load  \"" + loadID
                    + "\"  - StandardInventoryServer.addBadReadItem");
        } finally {
            endTransaction(tt);
        }
    }

    /**
     * Method to add a BadRead Item to a load.
     *
     * @param loadID no information available
     */
    public void addDuplicateTrayItem(String loadID) {
        TransactionToken tt = null;

        try {
            tt = startTransaction();
            final String binFull = EBSConstants.EBS_DUPLICATE_TRAY;
            LoadLineItemData unkItem = Factory.create(LoadLineItemData.class);
            unkItem.setLoadID(loadID);
            unkItem.setItem(binFull);
            unkItem.setCurrentQuantity(1);
            if (!mpIM.exists(binFull)) {
                addItemMasterFromString(binFull, "Default Duplicate Tray Item", false);
            }
            addLoadLI(unkItem);

            if (sendIAForBEBF()) {
                if (mzHasHostSystem) {
                    initializeHostServer();
                    mpHostServ.sendInventoryAdjust(unkItem, "");
                }
            }

            commitTransaction(tt);
        } catch (DBException e) {
            logException(e, "Exception adding BadRead Item to Load  \"" + loadID
                    + "\"  - StandardInventoryServer.addBadReadItem");
        } finally {
            endTransaction(tt);
        }
    }

    /**
     * Method to add a BadRead Item to a load.
     *
     * @param loadID no information available
     */
    public void addDuplicateBagIdItem(String loadID) {
        TransactionToken tt = null;

        try {
            tt = startTransaction();
            final String binFull = EBSConstants.EBS_DUPLICATE_BAGID;
            LoadLineItemData unkItem = Factory.create(LoadLineItemData.class);
            unkItem.setLoadID(loadID);
            unkItem.setItem(binFull);
            unkItem.setCurrentQuantity(1);
            if (!mpIM.exists(binFull)) {
                addItemMasterFromString(binFull, "Default Duplicate BagId Item", false);
            }
            addLoadLI(unkItem);

            if (sendIAForBEBF()) {
                if (mzHasHostSystem) {
                    initializeHostServer();
                    mpHostServ.sendInventoryAdjust(unkItem, "");
                }
            }

            commitTransaction(tt);
        } catch (DBException e) {
            logException(e, "Exception adding BadRead Item to Load  \"" + loadID
                    + "\"  - StandardInventoryServer.addBadReadItem");
        } finally {
            endTransaction(tt);
        }
    }

    /**
     * Method to correctly set a storage location's empty status. Status is determined as follows: Reserved - No load
     * present, but load enroute. Occupied - Load is in location. Unoccupied - No load present or enroute.
     *
     * @param warehouse Warehouse location is in.
     * @param address Address of the location.
     * @throws DBException for DB access errors.
     */
    public void setLocationEmptyStatus(String warehouse, String address, String isShelfPosition) throws DBException {
        StandardLocationServer mpLocServer = Factory.create(StandardLocationServer.class);
        int vnEmptyFlag = DBConstants.OCCUPIED;

        LocationData lcdataSearch = Factory.create(LocationData.class);
        LoadData lddataSearch = Factory.create(LoadData.class);
        Location location = Factory.create(Location.class);
        // validate the locations passed in, be sure they are real
        if ((warehouse.trim().length() == 0) || (address.trim().length() == 0)) {
            return;
        }

        // MCM, Below queries were taking up to 8 seconds
        // we dont need to update staton status anyway
        initializeStationServer();
        if (mpStationServer.exists(address)) {
            return;
        }

        // only deal with locations that are regular locations
        lcdataSearch.setKey(LocationData.WAREHOUSE_NAME, warehouse);
        lcdataSearch.setKey(LocationData.ADDRESS_NAME, address);
        lcdataSearch.setInKey(LocationData.LOCATIONTYPE_NAME, KeyObject.AND, DBConstants.LCASRS,
                DBConstants.LCCONSOLIDATION, DBConstants.LCCONVSTORAGE, DBConstants.LCRECEIVING, DBConstants.LCSHIPPING,
                DBConstants.LCSTAGING, DBConstants.LCDEDICATED);
        LocationData lcdata = location.getElement(lcdataSearch, DBConstants.WRITELOCK);
        if (lcdata != null) {
            // see if loads exist in the location
            lddataSearch.clear();
            lddataSearch.setKey(LoadData.WAREHOUSE_NAME, warehouse);
            lddataSearch.setKey(LoadData.ADDRESS_NAME, address);
            lddataSearch.setKey(LoadData.SHELFPOSITION_NAME, isShelfPosition);
            boolean loadsExist = mpLoad.exists(lddataSearch);

            // check if any loads enroute to this location
            // need to check both final and next destinations
            lddataSearch.clear();
            lddataSearch.setKey(LoadData.FINALWAREHOUSE_NAME, warehouse);
            lddataSearch.setKey(LoadData.FINALADDRESS_NAME, address);
            boolean loadsEnroute = mpLoad.exists(lddataSearch);

            if (!loadsEnroute) {
                lddataSearch.clear();
                lddataSearch.setKey(LoadData.NEXTWAREHOUSE_NAME, warehouse);
                lddataSearch.setKey(LoadData.NEXTADDRESS_NAME, address);
                lddataSearch.setKey(LoadData.NEXTSHELFPOSITION_NAME, isShelfPosition);
                loadsEnroute = mpLoad.exists(lddataSearch);
            }

            lcdata.setKey(LocationData.WAREHOUSE_NAME, warehouse);
            lcdata.setKey(LocationData.ADDRESS_NAME, address);
            if (loadsExist) {
                vnEmptyFlag = DBConstants.OCCUPIED;
            } else if (loadsEnroute) {
                vnEmptyFlag = DBConstants.LCRESERVED;
            } else {
                vnEmptyFlag = DBConstants.UNOCCUPIED;
            }

            // update the iEmptyFlag in the location table
            mpLocServer.setLocationEmptyFlag(warehouse, address, isShelfPosition, vnEmptyFlag);
        }
    }

    /**
     * Method checks for the existence of a particular item in the system.
     * 
     * @param sItem
     * @return code>boolean</code> indicating if item is found.
     */
    @UnusedMethod
    public boolean itemExists(String sItem) {
        boolean rtnval;
        LoadLineItemData llidataSearch = Factory.create(LoadLineItemData.class);

        llidataSearch.setKey(LoadLineItemData.ITEM_NAME, sItem);
        try {
            rtnval = mpLLI.getCount(llidataSearch) > 0;
        } catch (DBException e) {
            rtnval = false;
        }

        return (rtnval);
    }

    protected void initializeStationServer() {
        if (mpStationServer == null) {
            mpStationServer = Factory.create(StandardStationServer.class);
        }
    }

    protected void initializeBCSServer() {
        if (mpBCSServer == null) {
            mpBCSServer = Factory.create(BCSServer.class);
        }
    }

    protected void initializeEBSHostServer() {
        if (mpEBSHostServer == null) {
            mpEBSHostServer = Factory.create(EBSHostServer.class);
        }
    }

    protected void initializeTableJoin() {
        if (mpTableJoin == null) {
            mpTableJoin = Factory.create(EBSTableJoin.class);
        }
    }

    protected void initializePOServer() {
        if (mpEBSPOReceivingServer == null) {
            mpEBSPOReceivingServer = Factory.create(EBSPoReceivingServer.class);
        }
    }
    
    protected void initializeMoveCommandServer() {
        if (mpMoveCommandServer == null) {
            mpMoveCommandServer = Factory.create(StandardMoveCommandServer.class);
        }
    }
    protected void initializeStandardLocationServer() {
        if (standardLocationServer == null) {
        	standardLocationServer = Factory.create(StandardLocationServer.class);
        }
    }
    
    protected void initializeConveyorTableJoin() {
        if (conveyorTableJoin == null) {
        	conveyorTableJoin = Factory.create(ConveyorTableJoin.class);
        }
    }
    /**
     * Delete load and all associated records with load when bag is released (retrieved)
     * 
     * @param sOrderId
     * @param sLoadID
     * @param sGlobalId
     * @param sLineId
     * @param stationId
     * @param sReasonCode
     * @throws DBException
     */
    public void processReleasedLoadData(String sOrderId, String sLoadID, String sGlobalId, String sLineId,
            String stationId, String sReasonCode) throws DBException {
        
    	initializeTableJoin();
        // Read load and save data before deleting.
        LoadData vpLoadData = getInventoryLoad(sLoadID, DBConstants.NOWRITELOCK);

        String orderHeaderId = mpTableJoin.getOrderHeaderIdByLoadId(sLoadID);
        String purchaserOrderId = mpTableJoin.getPOIdByLoadId(sLoadID);

        logDebug("Deleting load  and order for loadId:" + sLoadID + " - and orderId:" + sOrderId + " - and POID:"
                + purchaserOrderId);

        if (orderHeaderId != null && !orderHeaderId.isEmpty()) {
           //Deleting only order-line which associated with this LOADID and deleting Order only if this is the last LOAD associated with it
        	
        	logDebug("Deleting Order and OrderLine for:" + orderHeaderId +" associated with Load:"+ sLoadID);
        	mpTableJoin.deleteOrderLineByLoadId(sLoadID);
        	//it will deleted only if no more order-line associated with it...
            mpTableJoin.deleteOrderHeaderByOrderId(orderHeaderId);
        }
        // delete purchase order by sLoadId
        if (purchaserOrderId != null && !purchaserOrderId.isEmpty()) {
            logDebug("Deleting PO and POL for:" + purchaserOrderId);
            mpTableJoin.deletePurchaseOrderLineByOrderId(purchaserOrderId);
            mpTableJoin.deletePurchaseOrderHeaderByOrderId(purchaserOrderId);
        }

        // delete load and LoadLine
        mpTableJoin.deleteLoadlineByLoadId(sLoadID);
        mpTableJoin.deleteLoadByLoadId(sLoadID);
        // delete move
        mpTableJoin.deleteMoveByLoadId(sLoadID);
        // Update location
        if (vpLoadData != null) {
           
            // location empty flags may need adjusting
            mpTableJoin.setLocationEmptyStatus(vpLoadData.getAddress(), DBConstants.UNOCCUPIED);

            logOperation(LogConsts.OPR_DSVR, "LoadId \"" + sLoadID + "\" - Deleted");
            tnData.clear();
            tnData.setTranCategory(DBConstants.LOAD_TRAN);
            tnData.setTranType(DBConstants.DELETE_LOAD);
            tnData.setLoadID(sLoadID);
            tnData.setToStation(stationId);
            tnData.setActionDescription("Item Deleted");
            tnData.setLineID(sLineId);
            tnData.setStation(vpLoadData.getCurrentAddress());
            tnData.setOrderID(sOrderId);
            tnData.setLocation(vpLoadData.getWarehouse(), vpLoadData.getAddress());
            tnData.setRouteID((vpLoadData.getRouteID()));
            logTransaction(tnData);

        }

    }
    
    public void processReleasedLoadDataForConveyor(ItemReleasedContext itemReleasedContext) throws DBException {
    	initializeTableJoin();
    	initializeLocation();
        // Read load and save data before deleting.
        LoadData vpLoadData = getInventoryLoad(itemReleasedContext.getLoadId(), DBConstants.NOWRITELOCK);

        String orderHeaderId = mpTableJoin.getOrderHeaderIdByLoadId(itemReleasedContext.getLoadId());
        String purchaserOrderId = mpTableJoin.getPOIdByLoadId(itemReleasedContext.getLoadId());

        logDebug("Deleting load  and order for loadId:" + itemReleasedContext.getLoadId() + " - and orderId:" + itemReleasedContext.getOrderId() + " - and POID:"
                + purchaserOrderId);

        if (orderHeaderId != null && !orderHeaderId.isEmpty()) {
           //Deleting only order-line which associated with this LOADID and deleting Order only if this is the last LOAD associated with it
        	
        	logDebug("Deleting Order and OrderLine for:" + orderHeaderId +" associated with Load:"+ itemReleasedContext.getLoadId());
        	mpTableJoin.deleteOrderLineByLoadId(itemReleasedContext.getLoadId());
        	//it will deleted only if no more order-line associated with it...
            mpTableJoin.deleteOrderHeaderByOrderId(orderHeaderId);
        }
        // delete purchase order by sLoadId
        if (purchaserOrderId != null && !purchaserOrderId.isEmpty()) {
            logDebug("Deleting PO and POL for:" + purchaserOrderId);
            mpTableJoin.deletePurchaseOrderLineByOrderId(purchaserOrderId);
            mpTableJoin.deletePurchaseOrderHeaderByOrderId(purchaserOrderId);
        }

        // delete load and LoadLine
        mpTableJoin.deleteLoadlineByLoadId(itemReleasedContext.getLoadId());
        mpTableJoin.deleteLoadByLoadId(itemReleasedContext.getLoadId());
        
        // Update location
        if (vpLoadData != null) {
        	itemReleasedContext.setSuccess(true);
        	int newShelfPostion = Integer.parseInt(vpLoadData.getShelfPosition()) - 1;
        	if(newShelfPostion == DBConstants.DEFAULT_SHELF_POS) {
        		// location empty flags may need adjusting
        		mpTableJoin.setLocationEmptyStatus(vpLoadData.getAddress(), DBConstants.UNOCCUPIED);
        	}
        	
        	// Update location shelf position value
        	mpLocation.setShelfPositionValue(vpLoadData.getWarehouse(),vpLoadData.getAddress() , String.format("%03d", newShelfPostion));
        	
        	// Log Transaction History
            logOperation(LogConsts.OPR_DSVR, "LoadId \"" + itemReleasedContext.getLoadId() + "\" - Deleted");
            tnData.clear();
            tnData.setTranCategory(DBConstants.LOAD_TRAN);
            tnData.setTranType(DBConstants.DELETE_LOAD);
            tnData.setLoadID(itemReleasedContext.getLoadId());
            tnData.setToStation(itemReleasedContext.getStationId());
            tnData.setActionDescription("Load is released and record is deleted");
            tnData.setLineID( itemReleasedContext.getLineId());
            tnData.setStation(vpLoadData.getCurrentAddress());
            tnData.setOrderID(  itemReleasedContext.getOrderId());
            tnData.setLocation(vpLoadData.getWarehouse(), vpLoadData.getAddress());
            tnData.setRouteID((vpLoadData.getRouteID()));
            tnData.setReasonCode(EBSDBConstants.DELETE_REASON_CODE.RELEASED);
            logTransaction(tnData);

        }
    }
    
    /**
     * Delete load and all associated records with load when bag is released (retrieved)
     * 
     * @param sOrderId
     * @param sLoadID
     * @param sGlobalId
     * @param sLineId
     * @param stationId
     * @param sReasonCode
     * @throws DBException
     */
    public void processReleasedLoadData(ItemArrivedContext plcItemArrivedContext ) throws DBException {
        
    	initializeTableJoin();
        // Read load and save data before deleting.
        LoadData vpLoadData = getInventoryLoad(plcItemArrivedContext.getLoadId(), DBConstants.NOWRITELOCK);

        String orderHeaderId = mpTableJoin.getOrderHeaderIdByLoadId(plcItemArrivedContext.getLoadId());
        String purchaserOrderId = mpTableJoin.getPOIdByLoadId(plcItemArrivedContext.getLoadId());

        logDebug("Deleting load  and order for loadId:" + plcItemArrivedContext.getLoadId() + " - and orderId:" + plcItemArrivedContext.getLoadId() + " - and POID:"
                + purchaserOrderId);

        if (orderHeaderId != null && !orderHeaderId.isEmpty()) {
           //Deleting only order-line which associated with this LOADID and deleting Order only if this is the last LOAD associated with it
        	
        	logDebug("Deleting Order and OrderLine for:" + orderHeaderId +" associated with Load:"+ plcItemArrivedContext.getLoadId());
        	mpTableJoin.deleteOrderLineByLoadId(plcItemArrivedContext.getLoadId());
        	//it will deleted only if no more order-line associated with it...
            mpTableJoin.deleteOrderHeaderByOrderId(orderHeaderId);
        }
        // delete purchase order by sLoadId
        if (purchaserOrderId != null && !purchaserOrderId.isEmpty()) {
            logDebug("Deleting PO and POL for:" + purchaserOrderId);
            mpTableJoin.deletePurchaseOrderLineByOrderId(purchaserOrderId);
            mpTableJoin.deletePurchaseOrderHeaderByOrderId(purchaserOrderId);
        }

        // Delete Movecommand for given load
        mpTableJoin.deleteMoveCommand(plcItemArrivedContext.getLoadId());
        // delete load and LoadLine
        mpTableJoin.deleteLoadlineByLoadId(plcItemArrivedContext.getLoadId());
        mpTableJoin.deleteLoadByLoadId(plcItemArrivedContext.getLoadId());
        // delete move
        mpTableJoin.deleteMoveByLoadId(plcItemArrivedContext.getLoadId());
        // Update location
        if (vpLoadData != null) {
        	plcItemArrivedContext.setSuccess(true);
            // location empty flags may need adjusting
            mpTableJoin.setLocationEmptyStatus(vpLoadData.getAddress(), DBConstants.UNOCCUPIED);

            logOperation(LogConsts.OPR_DSVR, "LoadId \"" + plcItemArrivedContext.getLoadId() + "\" - Deleted");
            tnData.clear();
            tnData.setTranCategory(DBConstants.LOAD_TRAN);
            tnData.setTranType(DBConstants.DELETE_LOAD);
            tnData.setLoadID(plcItemArrivedContext.getLoadId());
            tnData.setToStation(plcItemArrivedContext.getStationId());
            tnData.setActionDescription("Load is released and record is deleted");
            tnData.setLineID( plcItemArrivedContext.getLineId());
            tnData.setStation(vpLoadData.getCurrentAddress());
            tnData.setOrderID(  plcItemArrivedContext.getOrderId());
            tnData.setLocation(vpLoadData.getWarehouse(), vpLoadData.getAddress());
            tnData.setRouteID((vpLoadData.getRouteID()));
            logTransaction(tnData);

        }

    }
    
    
    public void processStoreComplete(ItemStoredContext plcItemStoredContext) throws DBException {
    
    	initializeTableJoin();
    	initializeStandardLocationServer();
    	initializePurchaseOrderHeader();
    	LoadData vpLoadData = getInventoryLoad(plcItemStoredContext.getLoadId(), DBConstants.NOWRITELOCK);

         if (vpLoadData == null) {
             logError("Load data for " + plcItemStoredContext.getLoadId() + " not found");

         } else {

             if (plcItemStoredContext.getStatus() == SACControlMessage.StoreCompletionNotify.STATUS.SUCCESS) {
                 purchaseOrderHeader.setOrderStatusValue(plcItemStoredContext.getOrderId(), DBConstants.ERCOMPLETE);
                 vpLoadData.setCurrentAddress(plcItemStoredContext.getAddressId());
                 vpLoadData.setAddress(plcItemStoredContext.getAddressId());
                 vpLoadData.setLoadMoveStatus(EBSDBConstants.Load.MOVE_STATUS.STORED);
                 
                 moveLoadForStore(vpLoadData, getClass().getSimpleName());
                 plcItemStoredContext.setLoadData(vpLoadData);
                 MoveCommand moveCommand = Factory.create(MoveCommand.class);
                 moveCommand.updateMoveCommandStatusByLoadId(plcItemStoredContext.getLoadId(), DBConstants.CMD_DELETED);                 
             }

             plcItemStoredContext.setSuccess(true);
         }    	
    }
    
    /**
     * Method to process store complete for conveyor based warehouse.
     * @param ItemStoredContext
     * @throws DBException
     */
    public void processStoreCompleteForConveyor(ItemStoredContext plcItemStoredContext) throws DBException {
        
    	initializeStandardLocationServer();
    	initializePurchaseOrderHeader();
    	initializeConveyorTableJoin();
    	initializeLoad();
    	initializeLocation();
    	LoadData vpLoadData = getInventoryLoad(plcItemStoredContext.getLoadId(), DBConstants.NOWRITELOCK);

         if (vpLoadData == null) {
             logError("Load data for " + plcItemStoredContext.getLoadId() + " not found");

         } else {

             if (plcItemStoredContext.getStatus() == SACControlMessage.StoreCompletionNotify.STATUS.SUCCESS) {
                 purchaseOrderHeader.setOrderStatusValue(plcItemStoredContext.getOrderId(), DBConstants.ERCOMPLETE);
                 vpLoadData.setCurrentAddress(plcItemStoredContext.getAddressId());
                 vpLoadData.setAddress(plcItemStoredContext.getAddressId());
                 vpLoadData.setLoadMoveStatus(EBSDBConstants.Load.MOVE_STATUS.STORED);
                 String shelfPos = mpLocation.getShelfPositionValue(vpLoadData.getWarehouse(), vpLoadData.getAddress());                	 
                 if (Objects.isNull(shelfPos) || shelfPos.isEmpty() || Integer.parseInt(shelfPos) == DBConstants.DEFAULT_SHELF_POS) {
                	 shelfPos = String.format("%03d", DBConstants.DEFAULT_SHELF_POS + 1);
                 } else {
                	 shelfPos = String.format("%03d", Integer.parseInt(shelfPos) + 1);
                 }
                 vpLoadData.setShelfPosition(shelfPos);
                 vpLoadData.setMoveDate();
                 mpLoad.updateLoadInfo(vpLoadData);
                 mpLocation.setEmptyFlagValue(vpLoadData.getWarehouse(), vpLoadData.getAddress(), DBConstants.OCCUPIED);
                 mpLocation.setShelfPositionValue(vpLoadData.getWarehouse(), vpLoadData.getAddress(), shelfPos);
                 plcItemStoredContext.setLoadData(vpLoadData);
             }
             plcItemStoredContext.setSuccess(true);
         }    	
    }

    @Override
    public void deleteLoadItems(String isLoadID, String isReasonCode,
    	      boolean izDeleteLoad) throws DBException
    {
    	initializePOServer();
    	String sPONum[] = mpEBSPOReceivingServer.getExpectdIDByLoadID(isLoadID);
    	if(sPONum.length!=0) {
    		mpEBSPOReceivingServer.deleteERByLoadID(isLoadID);
    	}
    	super.deleteLoadItems(isLoadID, isReasonCode, izDeleteLoad);  
    	
     }
    
    /**
     * Delete all items and child loads on a load and possibly the load itself
     * 
     * @param sLoadID
     * @param sReasonCode
     * @param izDeleteLoad True if the load itself should also be deleted. US: 30299: added by NHC
     **/
    public void deleteLoadItems(String sOrderId, String sLoadID, String sReasonCode, boolean isDeleteLoad,
            String stationId, String sLineId, String sLaneId) throws DBException {
        TransactionToken tt = null;
        initializeTableJoin();

        try {
            tt = startTransaction();

            // delete all load line items
            deleteloadLineItems(sLoadID, sReasonCode);

            // check if there are any moves left, maybe on load orders for loads without items
            // need to check do we need to do this step
            // instead directly delete move
            processLoadMoves(sLoadID);

            // Check for an order that is left over after deleting each line and its
            // moves but not the order..if this is a load order, we need to delete it.
            deleteLoadOrders(sLoadID, sOrderId);

            // Delete the load if requested
            if (isDeleteLoad) {
                // Read load and save data before deleting.
                LoadData vpLoadData = getInventoryLoad(sLoadID, DBConstants.NOWRITELOCK);
                if (vpLoadData != null) {
                    // delete the load by id
                    mpLoad.deleteLoad(sLoadID);

                    // location empty flags may need adjusting
                    setLocationEmptyStatus(vpLoadData.getWarehouse(), vpLoadData.getAddress(),
                            vpLoadData.getShelfPosition());

                    logOperation(LogConsts.OPR_DSVR, "LoadId \"" + sLoadID + "\" - Deleted");
                    tnData.clear();
                    tnData.setTranCategory(DBConstants.LOAD_TRAN);
                    tnData.setTranType(DBConstants.DELETE_LOAD);
                    tnData.setLoadID(sLoadID);
                    tnData.setToLocation(vpLoadData.getNextWarehouse(), vpLoadData.getNextAddress());
                    tnData.setLocation(vpLoadData.getWarehouse(), vpLoadData.getAddress());
                    tnData.setRouteID((vpLoadData.getRouteID()));
                    logTransaction(tnData);

                  //TODO:KR  // send the data to WRxTOHOST from here for SAC to Process
                   // sendItemReleaseResponseMsg(sLaneId, sLoadID, sOrderId, sLineId, stationId);

                } else {
                    vpLoadData = Factory.create(LoadData.class);
                    vpLoadData.setLoadID(sLoadID);
                }
            }
            commitTransaction(tt);
        } finally {
            endTransaction(tt);
        }
    }


    
	/**
	 * Method to delete a load. Deletes a load and its load line items. Deletes POH,
	 * POL, MoveCommand, Move, OH and OL.
	 *
	 * @param loadID     Load ID to be deleted.
	 * @param reasonCode reason for deleting load line item.
	 * @throws DBException for DB access errors.
	 */
	public void deleteLoadWithAllData(String sLoadID, String sReasonCode) throws DBException {
		TransactionToken tt = null;
		initializePOServer();
		initializeTableJoin();
		initializeMoveCommandServer();
		try {
			tt = startTransaction();
			// Delete all POH and POL associated with load
			mpEBSPOReceivingServer.deleteERByLoadID(sLoadID);

			// delete all load line items
			deleteloadLineItems(sLoadID, sReasonCode);

			// Check for an order that is left over after deleting each line and its
			// moves but not the order..if this is a load order, we need to delete it.
			deleteLoadOrders(sLoadID, "");

			// Delete all the move command which are associated with this load
			deleteLoadMoveCommands(sLoadID);

			// Delete the load if requested
			// Read load and save data before deleting.
			LoadData vpLoadData = getInventoryLoad(sLoadID, DBConstants.NOWRITELOCK);
			if (vpLoadData != null) {
				// delete the load by id
				mpLoad.deleteLoad(sLoadID);

				// location empty flags may need adjusting
				setLocationEmptyStatus(vpLoadData.getWarehouse(), vpLoadData.getAddress(),
						vpLoadData.getShelfPosition());

				logOperation(LogConsts.OPR_DSVR, "LoadId \"" + sLoadID + "\" - Deleted");
				tnData.clear();
				tnData.setTranCategory(DBConstants.LOAD_TRAN);
				tnData.setTranType(DBConstants.DELETE_LOAD);
				tnData.setLoadID(sLoadID);
				tnData.setToLocation(vpLoadData.getNextWarehouse(), vpLoadData.getNextAddress());
				tnData.setLocation(vpLoadData.getWarehouse(), vpLoadData.getAddress());
				tnData.setRouteID((vpLoadData.getRouteID()));
				logTransaction(tnData);

			}
			commitTransaction(tt);
		} finally {
			endTransaction(tt);
		}
	}
    
    public void updateLoadLineOrderId(LoadLineItemData loadLineItemData, String orderId) throws DBException {
    		TransactionToken tt = null;
    		initializeTableJoin();
    		try {
    			tt = startTransaction();
    			mpTableJoin.updateLoadLineOrderId(loadLineItemData.getLoadID(), orderId);
    			commitTransaction(tt);
    		}finally {
    			endTransaction(tt);
			}
    	
    }

    private void deletePurchaseOrders(String sLoadId, String sGlobalId, String sItemId) {
        initializePOServer();
        mpEBSPOReceivingServer.deletePOForTrayItem(sLoadId, sGlobalId, sItemId);
    }

    /**
     * Check for an order that is left over after deleting each line and its moves but not the order..if this is a load
     * order, we need to delete it.
     * 
     * @param LoadId
     * @param OrderId
     * @throws DBException
     **/
	private void deleteLoadOrders(String sLoadId, String sOrderId) throws DBException {

		EBSOrderServer vpOrderServer = Factory.create(EBSOrderServer.class);
		List<Map> vapOLList = vpOrderServer.getOrderLineData(sOrderId, sLoadId);
		if (Objects.nonNull(vapOLList) && !vapOLList.isEmpty()) {
			List<OrderLineData> orderLineList = DBHelper.convertData(vapOLList, OrderLineData.class);
			for (OrderLineData orderLine : orderLineList) {
				String vsOrderID = orderLine.getOrderID();
				if (vpOrderServer.getOrderTypeValue(vsOrderID) == DBConstants.FULLLOADOUT
						|| vpOrderServer.getOrderTypeValue(vsOrderID) == DBConstants.ITEMORDER) {
					// For a load order, we have to delete the order because the load that
					// we ordered is now deleted and load orders only have one load on
					// them. If this changes, we will have to change this.
					vpOrderServer.deleteOrderLine(orderLine);
					vpOrderServer.deleteOrderHeaderIfNecessary(vsOrderID);
				}
			}
		}

	}

    /**
     * Check if there are any moves left, maybe on load orders for loads without items
     * 
     * @param String sLoadID
     **/
    private void processLoadMoves(String sLoadID) throws DBException {

        StandardDeallocationServer dealServer = Factory.create(StandardDeallocationServer.class);
        List<Map> moveList = Factory.create(Move.class).getMovesByLoadID(sLoadID);
        for (Iterator<Map> it = moveList.iterator(); it.hasNext();) {
            Map tMap = it.next();
            dealServer.deallocateOneMove(DBHelper.getIntegerField(tMap, MoveData.MOVEID_NAME));
        }
    }

    /**
     * Delete all load line items for the particular load
     * 
     * @param sLoadID
     * @param sReasonCode
     * @throws DBException
     **/
    private void deleteloadLineItems(String sLoadID, String sReasonCode) throws DBException {

        List<Map> loadLineItems = mpLLI.getLoadLineItemDataListByLoadID(sLoadID);
        for (Iterator<Map> it = loadLineItems.iterator(); it.hasNext();) {
            Map tMap = it.next();

            logDebug("Delete load line items, load line" + DBHelper.getStringField(tMap, LoadLineItemData.LOADID_NAME));
            deleteLoadLineItem(DBHelper.getStringField(tMap, LoadLineItemData.LOADID_NAME),
                    DBHelper.getStringField(tMap, LoadLineItemData.ITEM_NAME),
                    DBHelper.getStringField(tMap, LoadLineItemData.LOT_NAME),
                    DBHelper.getStringField(tMap, LoadLineItemData.ORDERID_NAME),
                    DBHelper.getStringField(tMap, LoadLineItemData.ORDERLOT_NAME),
                    DBHelper.getStringField(tMap, LoadLineItemData.LINEID_NAME),
                    DBHelper.getStringField(tMap, LoadLineItemData.POSITIONID_NAME), sReasonCode);
        }
    }

    /**
     * This method inserts the expected receipt response message in the WRX to host table with Item released data format
     * : [LaneId,sLoadID,sOrderID,sLineID,sStation]
     * 
     * @param LaneId
     * @param sLoadID
     * @param sStation
     * @param sOrderID
     */
    public void sendItemReleaseResponseMsg(String LaneId, String sLoadID, String sOrderID, String sLineID,
            String sStation) {

        EBSItemReleaseMessage vpMesg = new EBSItemReleaseMessage();
        vpMesg.setLoadID(sLoadID);
        vpMesg.setOrderID(sOrderID);
        vpMesg.setLineID(sLineID);
        vpMesg.setArrivalStation(sStation);

        try {
            initializeEBSHostServer();
            mpEBSHostServer.sendItemReleaseMessageResponseToHost(vpMesg);
        } catch (DBException e) {
            throw new InvalidHostDataException(HostError.ADD_ERROR,
                    "Unable to generate the Load Arrival mesage in WRXTOHOST.Exception details:" + e.getMessage());
        }
    }

    /**
     * Return the list of loadlineitem of the given lot/flight number
     * 
     * @param lot lot/flight number
     * @return the list of loadlineitem of the given lot/flight number
     * @throws DBException when anything goes wrong
     */
    public List<LoadLineItemData> getLoadLineItemDataListByLot(String lot) throws DBException {
        LoadLineItemData lliToSearch = Factory.create(LoadLineItemData.class);
        lliToSearch.setKey(LoadLineItemData.LOT_NAME, lot);

        return mpLLI.getAllElements(lliToSearch).stream().map(entry -> {
            LoadLineItemData liToReturn = Factory.create(LoadLineItemData.class);
            liToReturn.dataToSKDCData(entry);
            return liToReturn;
        }).collect(Collectors.toList());
    }

    public boolean modifyLoadLineItem(LoadLineItemData lli) throws DBException {
        boolean rtn = false;
        TransactionToken tt = null;
        try {
            tt = startTransaction();
            mpLLI.modifyElement(lli);
            commitTransaction(tt);
            rtn = true;
        } catch (Exception exc) {
            throw new DBException(exc.getMessage() + " -- LoadLineItem not Modified!");
        } finally {
            endTransaction(tt);
        }

        return rtn;
    }
    
    
	private void deleteLoadMoveCommands(String sLoadID) throws DBException {
		LoadData loadData = getInventoryLoad(sLoadID, DBConstants.NOWRITELOCK);
		// Delete all the move commands if finds any.
		mpMoveCommandServer.deleteMoveCommand(loadData.getLoadID());
		logOperation(LogConsts.OPR_DSVR, "LoadId \"" + loadData.getLoadID() + "\" - Deleted");
		tnData.clear();
		tnData.setTranCategory(DBConstants.LOAD_TRAN);
		tnData.setTranType(DBConstants.DELETE_MOVE_COMMAND);
		tnData.setLoadID(loadData.getLoadID());
		tnData.setToLocation(loadData.getNextWarehouse(), loadData.getNextAddress());
		tnData.setLocation(loadData.getWarehouse(), loadData.getAddress());
		tnData.setRouteID((loadData.getRouteID()));
		tnData.setActionDescription("Move Command Deleted");
		tnData.setStation(loadData.getCurrentAddress());
		tnData.setDeviceID(loadData.getDeviceID());
		logTransaction(tnData);
	}
	
	protected void moveLoadForStore(LoadData loadData, String msMyClass) {

        try {
        	initializeStandardLocationServer();
        	initializeLocation();
        	initializeLoad();
            loadData.setMoveDate();
            String warehouse = loadData.getWarehouse();
            String address = loadData.getAddress();
            String vsDeviceId = standardLocationServer.getLocationDeviceId(warehouse, address);
            loadData.setDeviceID(vsDeviceId);
           
            mpLoad.updateLoadInfo(loadData);
            mpLocation.setEmptyFlagValue(warehouse, address, DBConstants.OCCUPIED);

        } catch (DBException e) {
            logException(e, "LoadId \"" + loadData.getParentLoadID()
                    + "\" (Parent) Exception Changing Parent Load Status - " + msMyClass + ".setParentLoadMoveStatus");
        }
    }
	
	public void processCancelERMessage(String sLoadID) throws DBException {
		TransactionToken tt = null;
		initializePOServer();
		initializeLoad();
		try {
			tt = startTransaction();
			// logic
			// Delete all POH and POL associated with load
			mpEBSPOReceivingServer.deleteERByLoadID(sLoadID);

			// Delete all load line items
			deleteloadLineItems(sLoadID, "");

			// Delete the load if requested
			// Read load and save data before deleting.
			LoadData vpLoadData = getInventoryLoad(sLoadID, DBConstants.NOWRITELOCK);
			if (vpLoadData != null) {
				// delete the load by id
				mpLoad.deleteLoad(sLoadID);

				logOperation(LogConsts.OPR_DSVR, "LoadId \"" + sLoadID + "\" - Deleted");
				tnData.clear();
				tnData.setTranCategory(DBConstants.LOAD_TRAN);
				tnData.setTranType(DBConstants.DELETE_LOAD);
				tnData.setLoadID(sLoadID);
				tnData.setToLocation(vpLoadData.getNextWarehouse(), vpLoadData.getNextAddress());
				tnData.setLocation(vpLoadData.getWarehouse(), vpLoadData.getAddress());
				tnData.setRouteID((vpLoadData.getRouteID()));
				logTransaction(tnData);

			}
			commitTransaction(tt);
		} finally {
			endTransaction(tt);
		}

	}

	public List<Map> getLoadTransactionHistoryList(LoadTransactionHistoryData vpLoadTransactionKey) throws DBException {
		return mpLoadTransactionHistory.getAllElements(vpLoadTransactionKey);
	}
	
}
