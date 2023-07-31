package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itempickedup;

import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuoc.wrxj.custom.ebs.plc.acp.route.EBSAddress;
import com.daifukuoc.wrxj.custom.ebs.plc.acp.route.RouteManagerConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.StandardItemPickedupTransaction;
import com.daifukuoc.wrxj.custom.ebs.plc.queue.CommandQueueManagerImpl;

/**
 * The transaction wrapper for Item Picked up message process.
 * 
 * @author KR
 *
 */
public class PLCItemPickedUpTransaction extends StandardItemPickedupTransaction {
    protected Load mpLoad = Factory.create(Load.class);
    protected Location mpLocation = Factory.create(Location.class);

    public PLCItemPickedUpTransaction() {
        super(true);
    }

    @Override
    protected void executeBody(ItemPickedUpContext plcItemPickUpContext) throws DBException {
        LoadData loadData = mpLoad.getLoadData(plcItemPickUpContext.getLoadID());
        if (loadData != null) {
            switch (loadData.getLoadMoveStatus()) {

            case DBConstants.RETRIEVEPENDING:
                loadData.setLoadMoveStatus(DBConstants.RETRIEVING);
                break;
            case DBConstants.STOREPENDING:
                loadData.setLoadMoveStatus(DBConstants.STORING);
                break;
            default:
                break;
            }
            loadData.setCurrentAddress(plcItemPickUpContext.getToLocation());

            mpLoad.updateLoadInfo(loadData);
            String warehouse = loadData.getWarehouse();
            LocationData locationData = mpLocation.getLocation(warehouse, plcItemPickUpContext.getFromLocation());
            if (locationData != null && locationData.getLocationType() == DBTrans.LCASRS) {
                mpLocation.setEmptyFlagValue(warehouse, plcItemPickUpContext.getFromLocation(), DBTrans.UNOCCUPIED);
            }
            
            CommandQueueManagerImpl cmdQueueManager = Factory.create(CommandQueueManagerImpl.class);
            EBSAddress fromAddress = new EBSAddress(plcItemPickUpContext.getFromLocation());
            if (fromAddress.getType().equalsIgnoreCase(RouteManagerConstants.LCSHUTTLE_PICKUP_STATION_TYPE)
            		|| fromAddress.getType().equalsIgnoreCase(RouteManagerConstants.LCLIFTER_LAYER_PICKUP_STATION_TYPE)){
            	cmdQueueManager.update(plcItemPickUpContext.getLoadID(), DBConstants.CMD_DELETED);
            }
            cmdQueueManager.update(plcItemPickUpContext.getLoadID(), DBConstants.CMD_PROCCESSING);
            plcItemPickUpContext.setSuccess(true);
        } else {
            logger.logError(String.format("The load for %s is not found for item picked up message processing.",
                    plcItemPickUpContext.getLoadID()));
        }
    }

}
