package com.daifukuoc.wrxj.custom.ebs.host.processor.invreqbywarehouse;

import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.ConveyorTableJoin;
import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryReqByFlightMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryReqByWarehouseMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryResponseItem;
import com.daifukuoc.wrxj.custom.ebs.host.processor.inventoryrequest.exception.InventoryRequestFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalorder.exception.RetrievalOrderFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.util.ConversionUtil;
import com.daifukuoc.wrxj.custom.ebs.host.util.EBSHostMessageConstants;

/**
 * 
 * @author MT
 * Inventory request by warehouse impl class
 *
 */

public class InvReqByWarehouseProcessorImpl implements InvReqByWarehouseProcessor {

	protected Logger logger = Logger.getLogger();
	private ConveyorTableJoin tableJoin = Factory.create(ConveyorTableJoin.class);
	
	@Override
	public List<InventoryResponseItem> getResponseList(InventoryReqByWarehouseMessageData messageData)
			throws InventoryRequestFailureException {
		 // Validate RetrievalOrderMessageData
        if (messageData == null) {
            throw new InventoryRequestFailureException("Inventoy Request message data shouldn't be null ");
        }
        if (!messageData.isValid()) {
            throw new InventoryRequestFailureException("Inventoy Request message data is not valid ");
        }
		
        List<LoadLineItemData> loadLineItems = populateLoadLineItemDataToRetrieve(messageData);
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
    		InventoryReqByWarehouseMessageData messageData) throws InventoryRequestFailureException {

        try {
        	return tableJoin.getAllRetrievableLoadLineItemsByWarehouseID(messageData.getWarehouseID());
        } catch (DateTimeParseException | DBException e1) {
            throw new InventoryRequestFailureException(
                    "Failed to populate a list of retrievable loadlineitem for flight and flight scheduled datetime");
        }
    }
}
