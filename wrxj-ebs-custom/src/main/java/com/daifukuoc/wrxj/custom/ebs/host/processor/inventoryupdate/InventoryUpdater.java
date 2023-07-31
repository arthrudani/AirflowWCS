package com.daifukuoc.wrxj.custom.ebs.host.processor.inventoryupdate;

import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryUpdateMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.processor.Processor;
import com.daifukuoc.wrxj.custom.ebs.host.processor.inventoryupdate.exception.InventoryUpdateFailureException;

/**
 * An interface responsible for updating bag data.
 * 
 * @author MT
 *
 */
public interface InventoryUpdater extends Processor {

	public static final String NAME = "InventoryUpdater";

    /**
     * Apply the updated inventory data to AirflowWCS
     * 
     * @param inventoryUpdateMessageData
     */
	public void update(InventoryUpdateMessageData inventoryUpdateMessageData) throws InventoryUpdateFailureException;
}
