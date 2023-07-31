package com.daifukuoc.wrxj.custom.ebs.host.messages.delimited;

import org.apache.commons.lang3.StringUtils;

import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.device.gateway.SystemGateway;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.host.InvalidHostDataException;
import com.daifukuamerica.wrxj.host.messages.MessageParser;
import com.daifukuamerica.wrxj.log.Logger;

/**
 * Parses the received inventory request by warehouse message and hand it over to handler.
 * 
 * - The message format:
 * http://softwaredoc.daifukuoc.com/airflowwcs/technical-guide/messages/sac-messages/inventory-update.html
 * 
 * @author MT
 */

public class InventoryRequestByWarehouseParser implements MessageParser {
	
	 protected Logger logger = Logger.getLogger();
	    private SystemGateway gateway = ThreadSystemGateway.get();

		@Override
	    public void parse(HostToWrxData hostToWrxData) throws InvalidHostDataException {
	        if (hostToWrxData == null) {
	            throw new InvalidHostDataException(
	                    "The given host to wrx data is null");
	        }
	        if (hostToWrxData.getMessage() == null) {
	            throw new InvalidHostDataException(
	                    "The received inventory request by warehouse message is null");
	        }
	        if (StringUtils.isBlank(hostToWrxData.getMessage())) {
	            throw new InvalidHostDataException(
	                    "The received inventory request by warehouse message is empty or blank only");
	        }
	        gateway.publishHostInventoryReqByWarehouseEvent(hostToWrxData.getMessage());
	    }

	@Override
	public void cleanUp() {
		
	}

}
