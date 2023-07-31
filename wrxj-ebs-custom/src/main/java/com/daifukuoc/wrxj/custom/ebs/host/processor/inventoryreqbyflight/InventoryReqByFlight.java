package com.daifukuoc.wrxj.custom.ebs.host.processor.inventoryreqbyflight;

import java.util.List;

import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryReqByFlightMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryResponseItem;
import com.daifukuoc.wrxj.custom.ebs.host.processor.Processor;
import com.daifukuoc.wrxj.custom.ebs.host.processor.inventoryrequest.exception.InventoryRequestFailureException;

public interface InventoryReqByFlight extends Processor {
    public static final String NAME = "InventoryReqByFlight";
    
	List<InventoryResponseItem> getResponseList(InventoryReqByFlightMessageData inventoryRequestByFlightMessageData) throws InventoryRequestFailureException;
}
