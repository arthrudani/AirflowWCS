package com.daifukuoc.wrxj.custom.ebs.host.processor.invreqbywarehouse;

import java.util.List;

import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryReqByWarehouseMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryResponseItem;
import com.daifukuoc.wrxj.custom.ebs.host.processor.Processor;
import com.daifukuoc.wrxj.custom.ebs.host.processor.inventoryrequest.exception.InventoryRequestFailureException;

/**
 * 
 * @author MT
 * Inventory Request by warehouse message processor.
 *
 */
public interface InvReqByWarehouseProcessor extends Processor {

	public static final String NAME = "InvReqByWarehouseProcessor";

	List<InventoryResponseItem> getResponseList(InventoryReqByWarehouseMessageData messageData) 
			throws InventoryRequestFailureException;
}
