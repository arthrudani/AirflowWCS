/**
 * 
 */
package com.daifukuoc.wrxj.custom.ebs.host.messages.delimited;

import org.apache.commons.lang3.StringUtils;

import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.device.gateway.SystemGateway;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.host.InvalidHostDataException;
import com.daifukuamerica.wrxj.host.messages.MessageParser;
import com.daifukuamerica.wrxj.jdbc.DBRuntimeException;
import com.daifukuamerica.wrxj.log.Logger;

/**
 * Parses the received inventory request by flight message and hand it over to handler.
 * 
 * @author BT
 */

public class InventoryReqByFlightParser implements MessageParser {

    protected Logger mpLogger = Logger.getLogger();
    private SystemGateway gateway = ThreadSystemGateway.get();

    /*
     * A constructor
     */
    public InventoryReqByFlightParser() {
    }

    
    @Override
    public void parse(HostToWrxData hostToWrxData) throws InvalidHostDataException {

        if (hostToWrxData == null) {
            throw new InvalidHostDataException(
                    "The given host to wrx data is null");
        }
        if (hostToWrxData.getMessage() == null) {
            throw new InvalidHostDataException(
                    "The received inventory req by flight message is null");
        }
        if (StringUtils.isBlank(hostToWrxData.getMessage())) {
            throw new InvalidHostDataException(
                    "The received inventory req by flight message is empty or blank only");
        }
        gateway.publishHostInventoryRequestEvent(hostToWrxData.getMessage());
    }

    @Override
    public void cleanUp() {
    }

}
