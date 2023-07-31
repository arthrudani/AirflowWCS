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
 * 
 * @author BT
 */

public class RetrievalItemParser implements MessageParser {

    protected Logger mpLogger = Logger.getLogger();
    private SystemGateway gateway = ThreadSystemGateway.get();

    /*
     * A constructor
     */
    public RetrievalItemParser() {
    }

    /**
     * Method to do delimited parsing for an Retrieval Item.
     * 
     * @param hostToWrxData The data from the HostToWrx data queue (table).
     * @throws InvalidHostDataException when there are parsing errors due to malformed messages, or problems with
     *         message content validation.
     * @throws DBRuntimeException If there is an internal error that only the system needs to resolve.
     */
    @Override
    public void parse(HostToWrxData hostToWrxData) throws InvalidHostDataException {

        if (hostToWrxData == null) {
            throw new InvalidHostDataException(
                    "The given host to wrx data is null");
        }
        if (hostToWrxData.getMessage() == null) {
            throw new InvalidHostDataException(
                    "The received retrieval Item message is null");
        }
        if (StringUtils.isBlank(hostToWrxData.getMessage())) {
            throw new InvalidHostDataException(
                    "The received retrieval Item message is empty or blank only");
        }
        gateway.publishHostRetrievalItemEvent(hostToWrxData.getMessage());
    }

    @Override
    public void cleanUp() {
    }
}
