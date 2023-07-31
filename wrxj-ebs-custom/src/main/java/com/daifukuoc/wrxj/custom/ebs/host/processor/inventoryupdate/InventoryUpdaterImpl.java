package com.daifukuoc.wrxj.custom.ebs.host.processor.inventoryupdate;

import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryUpdateMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.processor.inventoryupdate.exception.InventoryUpdateFailureException;

/**
 * Apply the updated inventory data to all relevant data - load/loadlineitem
 * 
 * @author MT
 *
 */
public class InventoryUpdaterImpl implements InventoryUpdater {

	@Override
	public void update(InventoryUpdateMessageData inventoryUpdateMessageData) throws InventoryUpdateFailureException {

		if(inventoryUpdateMessageData == null) {
			throw new InventoryUpdateFailureException("Inventory update message data shouldn't be null ");
		}
		
		if(!inventoryUpdateMessageData.isValid()) {
			throw new InventoryUpdateFailureException("Inventory update message data is not valid ");
		}
		// Need to implement bussiness logic for Inventory Update Message
	}


}
