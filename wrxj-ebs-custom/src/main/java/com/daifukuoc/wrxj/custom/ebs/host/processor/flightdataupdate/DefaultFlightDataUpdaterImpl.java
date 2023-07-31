package com.daifukuoc.wrxj.custom.ebs.host.processor.flightdataupdate;

import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSInventoryServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLoadServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSPoReceivingServer;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSPurchaseOrderLineData;
import com.daifukuoc.wrxj.custom.ebs.host.messages.FlightDataUpdateMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.processor.flightdataupdate.exception.FlightDataUpdateFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.util.ConversionUtil;

/**
 * Apply the updated flight data to all relevant data - purchaseorder header/line and load/loadlineitem
 * 
 * @author LK
 *
 */
public class DefaultFlightDataUpdaterImpl implements FlightDataUpdater {
   
    private EBSPoReceivingServer poServer = Factory.create(EBSPoReceivingServer.class);
    private EBSInventoryServer inventoryServer = Factory.create(EBSInventoryServer.class);
    private EBSLoadServer loadServer = Factory.create(EBSLoadServer.class);
    
    @Override
    public void update(FlightDataUpdateMessageData flightDataUpdateMessageData)
            throws FlightDataUpdateFailureException {

        if (flightDataUpdateMessageData == null) {
            throw new FlightDataUpdateFailureException("Flight data update message data shouldn't be null ");
        }
        if (!flightDataUpdateMessageData.isValid()) {
            throw new FlightDataUpdateFailureException("Flight data update message data is not valid ");
        }

        Date flightScheduledDateTime;
        Date defaultRetrievalDateTime;
        try {
            flightScheduledDateTime = ConversionUtil
                    .convertDateStringToDate(flightDataUpdateMessageData.getFlightScheduledDateTime());
        } catch (DateTimeParseException e) {
            throw new FlightDataUpdateFailureException("Failed to convert flight scheduled datetime "
                    + flightDataUpdateMessageData.getFlightScheduledDateTime(), e);
        }
        try {
            defaultRetrievalDateTime = ConversionUtil
                    .convertDateStringToDate(flightDataUpdateMessageData.getDefaultRetrievalDateTime());
        } catch (DateTimeParseException e) {
            throw new FlightDataUpdateFailureException("Failed to convert default retrieval datetime "
                    + flightDataUpdateMessageData.getDefaultRetrievalDateTime(), e);
        }

        List<PurchaseOrderLineData> polsToUpdate;
        try {
            polsToUpdate = poServer.getPurchaseOrderLinesByLot(flightDataUpdateMessageData.getFlightNumber());
        } catch (DBException e) {
            throw new FlightDataUpdateFailureException(
                    "Failed to get the list of purchaseorderlines for " + flightDataUpdateMessageData.getFlightNumber(),
                    e);
        }
        for (PurchaseOrderLineData polToUpdate : polsToUpdate) {
            EBSPurchaseOrderLineData pol = Factory.create(EBSPurchaseOrderLineData.class);
            pol.setKey(PurchaseOrderLineData.ORDERID_NAME, polToUpdate.getOrderID());
            pol.setKey(PurchaseOrderLineData.ITEM_NAME, polToUpdate.getItem());
            pol.setKey(PurchaseOrderLineData.LOT_NAME, polToUpdate.getLot());
            pol.setKey(PurchaseOrderLineData.LINEID_NAME, polToUpdate.getLineID());
            pol.setExpirationDate(defaultRetrievalDateTime);
            try {
                poServer.modifyPOLine(pol);
            } catch (DBException e) {
                throw new FlightDataUpdateFailureException(
                        "Failed to update purchaseorderline for " + flightDataUpdateMessageData.getFlightNumber(), e);
            }
        }

        List<String> uniqueOrderIdList = polsToUpdate.stream().map(pol -> pol.getOrderID()).distinct()
                .collect(Collectors.toList());
        for (String orderId : uniqueOrderIdList) {
            PurchaseOrderHeaderData poh = Factory.<PurchaseOrderHeaderData>create(PurchaseOrderHeaderData.class);
            poh.setKey(PurchaseOrderHeaderData.ORDERID_NAME, orderId);
            poh.setExpectedDate(flightScheduledDateTime);
            poh.setFinalSortLocationId(flightDataUpdateMessageData.getFinalSortLocation());
            try {
                poServer.modifyPOHead(poh);
            } catch (DBException e) {
                throw new FlightDataUpdateFailureException(
                        "Failed to update purchaseorderheader for " + flightDataUpdateMessageData.getFlightNumber(), e);
            }
        }

        List<LoadLineItemData> llisToUpdate;
        try {
            llisToUpdate = inventoryServer.getLoadLineItemDataListByLot(flightDataUpdateMessageData.getFlightNumber());
        } catch (DBException e) {
            throw new FlightDataUpdateFailureException(
                    "Failed to get the list of loadlineitem for " + flightDataUpdateMessageData.getFlightNumber(), e);
        }
        for (LoadLineItemData lliToUpdate : llisToUpdate) {
            LoadLineItemData lli = Factory.create(LoadLineItemData.class);
            lli.setKey(LoadLineItemData.LOADID_NAME, lliToUpdate.getLoadID());
            lli.setKey(LoadLineItemData.ITEM_NAME, lliToUpdate.getItem());
            lli.setKey(LoadLineItemData.LOT_NAME, lliToUpdate.getLot());
            lli.setKey(LoadLineItemData.LINEID_NAME, lliToUpdate.getLineID());
            lli.setKey(LoadLineItemData.ORDERID_NAME, lliToUpdate.getOrderID());
            lli.setExpirationDate(defaultRetrievalDateTime);
            try {
                inventoryServer.modifyLoadLineItem(lli);
            } catch (DBException e) {
                throw new FlightDataUpdateFailureException(
                        "Failed to update loadlineitem for " + flightDataUpdateMessageData.getFlightNumber(), e);
            }
        }

        List<String> uniqueLoadIdList = llisToUpdate.stream().map(lli -> lli.getLoadID()).distinct()
                .collect(Collectors.toList());
        for (String loadId : uniqueLoadIdList) {
            LoadData load = Factory.create(LoadData.class);
            load.setKey(LoadData.LOADID_NAME, loadId);
            load.setFinalSortLocationID(flightDataUpdateMessageData.getFinalSortLocation());
            try {
                loadServer.modifyLoad(load);
            } catch (DBException e) {
                throw new FlightDataUpdateFailureException(
                        "Failed to update load for " + flightDataUpdateMessageData.getFlightNumber(), e);
            }
        }
    }
}
