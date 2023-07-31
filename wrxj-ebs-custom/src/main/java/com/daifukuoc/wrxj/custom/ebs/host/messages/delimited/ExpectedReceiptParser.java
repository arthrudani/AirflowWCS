package com.daifukuoc.wrxj.custom.ebs.host.messages.delimited;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.device.gateway.SystemGateway;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.InvalidHostDataException;
import com.daifukuamerica.wrxj.host.messages.MessageHelper;
import com.daifukuamerica.wrxj.host.messages.MessageParser;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBRuntimeException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSInventoryServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSPoReceivingServer;
import com.daifukuoc.wrxj.custom.ebs.host.messages.ExpectedReceiptMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;

/**
 * Class to handle the parsing of Expected Receipt Message from host/SAC system. The message is defined in
 * http://softwaredoc.daifukuoc.com/airflowwcs/technical-guide/messages/sac-messages/expected-receipt.html The processed
 * message will be in comma separated format. for example (Message header + Message body) :
 * 2,555,1,0,0,0,77,1,5555,7890,12345098769,4567A,20221201134500,20221201134501,3456,1,1
 *
 * The Request-Type in the message specifies the required operation (New = 1, Update = 2, Cancel =3)
 * 
 * @author KR 13-May-2022
 */
public class ExpectedReceiptParser implements MessageParser {

    public static final String PARSER_NAME = "ExpectedReceiptParser";

    protected ExpectedReceiptMessageData mpROMData;
    protected EBSPoReceivingServer mpPOServer;
    protected EBSInventoryServer inventoryServer;
    protected Logger logger = Logger.getLogger();
    private SystemGateway gateway = ThreadSystemGateway.get();

    /**
     * Default constructor for a delimited Item Master parser.
     */
    public ExpectedReceiptParser() throws DBException {
        mpPOServer = Factory.create(EBSPoReceivingServer.class, PARSER_NAME);
        inventoryServer = Factory.create(EBSInventoryServer.class, PARSER_NAME);
        mpROMData = Factory.create(ExpectedReceiptMessageData.class);
    }

    /**
     * Method to do delimited parsing for an Expected Receipt.
     * 
     * @param hostToWrxData The data from the HostToWrx data queue (table).
     * @throws InvalidHostDataException when there are parsing errors due to malformed messages, or problems with
     *         message content validation.
     * @throws DBRuntimeException If there is an internal error that only the system needs to resolve.
     */
    public void parse(HostToWrxData hostToWrxData) throws InvalidHostDataException {
        if (hostToWrxData == null) {
            throw new InvalidHostDataException("The given host to wrx data is null");
        }
        if (hostToWrxData.getMessage() == null) {
            throw new InvalidHostDataException("The received flight data update message is null");
        }
        if (StringUtils.isBlank(hostToWrxData.getMessage())) {
            throw new InvalidHostDataException("The received expected receipt message is empty or blank only");
        }

        if (mpROMData.parse(hostToWrxData.getMessage())) {
            try {
                execExpectedReceiptTransaction_PurchaseOrder(mpROMData.getRequestType(),
                        hostToWrxData.getOriginalMessageSequence(), mpROMData, hostToWrxData.getMessage());
            } catch (ParseException e) {
                logger.logError("Failed to save the received expected receipt message to DB");

                // Send out host expected receipt event so that host expected receipt message handler can
                // send a reply back to host
                gateway.publishHostExpectedReceiptEvent(hostToWrxData.getMessage());
            }
        } else {
            logger.logError("Invalid expected receipt message received");

            // Send out host expected receipt event so that host expected receipt message handler can
            // send a reply back to host
            gateway.publishHostExpectedReceiptEvent(hostToWrxData.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void cleanUp() {
        if (mpPOServer != null) {
            mpPOServer.cleanUp();
            mpPOServer = null;
        }
        /*
         * if(mpMessageProcessor != null) { mpMessageProcessor.cleanUp(); }
         */
        if (inventoryServer != null) {
            inventoryServer.cleanUp();
            inventoryServer = null;
        }
    }

    protected void execExpectedReceiptTransaction_PurchaseOrder(int inAction, int originalMessageSequence,
            ExpectedReceiptMessageData expectedReceiptMessageData, String receivedMessage) throws ParseException {
        try {
            switch (inAction) {
            case SACControlMessage.ExpectedReceiptsRequest.REQUEST_TYPE.ADD:
                // If the received expected receipt message is for existing bag for the flight, the message should
                // be silently ignored, even without sending a reply.
                boolean existingItem = false;
                List<Map> loadLineItems = inventoryServer
                        .getLoadLineItemDataListByLoadID(expectedReceiptMessageData.getLoadId());
                if (loadLineItems != null && loadLineItems.size() > 0) {
                    for (Map loadLineItem : loadLineItems) {
                        String lotId = DBHelper.getStringField(loadLineItem, LoadLineItemData.LOT_NAME);
                        String lineId = DBHelper.getStringField(loadLineItem, LoadLineItemData.LINEID_NAME);
                        if (lotId != null && lotId.equals(expectedReceiptMessageData.getLot()) && lineId != null
                                && lineId.equals(expectedReceiptMessageData.getLineId())) {
                            existingItem = true;
                            break;
                        }
                    }
                }
                if (existingItem) {
                    logger.logError("The received expected receipt message is for existing load/lot: "
                            + expectedReceiptMessageData.getLineId());
                    logger.logError("No reply will be sent back");
                    return;
                }

                if (expectedReceiptMessageData.isValid()) {
                    // Create a new entry in purchase order header/line tables
                    mpPOServer.addPOExpectedReceipt(expectedReceiptMessageData);
                }

                // Send out host expected receipt event so that host expected receipt message handler can
                // find an empty location and then send a reply back to host
                gateway.publishHostExpectedReceiptEvent(receivedMessage);
                break;

            case SACControlMessage.ExpectedReceiptsRequest.REQUEST_TYPE.UPDATE:
                mpPOServer.updatePOExpectedReceipt(expectedReceiptMessageData);
                break;

            case SACControlMessage.ExpectedReceiptsRequest.REQUEST_TYPE.CANCEL:
                mpPOServer.deletePO(expectedReceiptMessageData.getOrderId());
                break;
            }

        } catch (DBException e) {
            throw MessageHelper.getInvalidDataExcep(e, originalMessageSequence);
        }
    }
}
