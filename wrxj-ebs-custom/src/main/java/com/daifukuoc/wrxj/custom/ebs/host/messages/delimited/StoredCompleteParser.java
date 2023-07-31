package com.daifukuoc.wrxj.custom.ebs.host.messages.delimited;

import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.host.InvalidHostDataException;
import com.daifukuamerica.wrxj.host.messages.MessageParser;
import com.daifukuamerica.wrxj.jdbc.DBRuntimeException;

/**
 * Class to handle the parsing of stored complete ack message from host/SAC system.
 * The message is defined in
 * http://softwaredoc.daifukuoc.com/airflowwcs/technical-guide/messages/sac-messages/stored-complete-response.html
 * 
 * @author LK 09-Nov-2022
 */
public class StoredCompleteParser implements MessageParser {
	/**
	 * Method to do delimited parsing for a stored complete ack message
	 * At the moment, we don't have any business logic for stored complete ack message sent by SAC
	 * As HostMessageIntegrator expects a matching parser for any message from host, we need to implement this parser
	 * 
	 * @param ipHostData The data from the HostToWrx data queue (table).
	 * @throws InvalidHostDataException when there are parsing errors due to
	 *                                  malformed messages, or problems with message
	 *                                  content validation.
	 * @throws DBRuntimeException       If there is an internal error that only the
	 *                                  system needs to resolve.
	 */
    public void parse(HostToWrxData ipHostData) throws InvalidHostDataException, DBRuntimeException {
    }

    @Override
    public void cleanUp() {
    }
}
