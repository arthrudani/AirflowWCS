package com.daifukuoc.wrxj.custom.ebs.host.processor.inventoryreqbyflight;

import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.ConveyorTableJoin;
import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryReqByFlightMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryResponseItem;
import com.daifukuoc.wrxj.custom.ebs.host.processor.inventoryrequest.exception.InventoryRequestFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalorder.exception.RetrievalOrderFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.util.ConversionUtil;
import com.daifukuoc.wrxj.custom.ebs.host.util.EBSHostMessageConstants;

/**
 * 
 * @author BT
 *
 */
public class InventoryReqByFlightImpl implements InventoryReqByFlight {

    private ConveyorTableJoin tableJoin = Factory.create(ConveyorTableJoin.class);

    protected Logger logger = Logger.getLogger();
    
    @Override
	public List<InventoryResponseItem> getResponseList(
			InventoryReqByFlightMessageData inventoryRequestByFlightMessageData) throws InventoryRequestFailureException {
		
		 // Validate RetrievalOrderMessageData
        if (inventoryRequestByFlightMessageData == null) {
            throw new InventoryRequestFailureException("Inventoy Request message data shouldn't be null ");
        }
        if (!inventoryRequestByFlightMessageData.isValid()) {
            throw new InventoryRequestFailureException("Inventoy Request message data is not valid ");
        }
		
        List<LoadLineItemData> loadLineItems = populateLoadLineItemDataToRetrieve(inventoryRequestByFlightMessageData);
        List<InventoryResponseItem>  list = new ArrayList<>();
        for(LoadLineItemData item : loadLineItems) {
        	InventoryResponseItem itemList = new InventoryResponseItem();
        	itemList.setLoadId(item.getLoadID());
        	itemList.setLineId(item.getLineID());
        	itemList.setGlobalID(item.getGlobalID());
        	itemList.setFlightSTD(item.getExpectedDate());
        	itemList.setFlightNumber(item.getLot());
        	itemList.setLocationID(item.getColumnObject("SADDRESS").getColumnValue().toString());
        	itemList.setWarehouseID(item.getColumnObject("SWAREHOUSE").getColumnValue().toString());
        	list.add(itemList);
        }
		return list;
	}

    /**
     * Populate the list of loadlineitem for retrieval order processing
     * 
     * @param retrievalOrderMessageData The received message
     * @return The list of loadlineitem for retrieval order processing
     * @throws RetrievalOrderFailureException If anything goes wrong
     */
    private List<LoadLineItemData> populateLoadLineItemDataToRetrieve(
            InventoryReqByFlightMessageData inventoryReqByFlightMessageData) throws InventoryRequestFailureException {

        try {
            return tableJoin.getAllRetrievableLoadLineItemsForScheduledFlight(inventoryReqByFlightMessageData.getLot(),
                    ConversionUtil.convertDateStringToDate(inventoryReqByFlightMessageData.getFlightScheduledDateTime()));
        } catch (DateTimeParseException | DBException e1) {
            throw new InventoryRequestFailureException(
                    "Failed to populate a list of retrievable loadlineitem for flight and flight scheduled datetime");
        }
    }

}
