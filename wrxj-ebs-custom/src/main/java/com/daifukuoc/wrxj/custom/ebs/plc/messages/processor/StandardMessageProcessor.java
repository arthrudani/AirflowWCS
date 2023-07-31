package com.daifukuoc.wrxj.custom.ebs.plc.messages.processor;

import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCBaseMsgBuilder;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCMessageData;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCMessageHeader;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.decoder.StandardMessageDecoder;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.decoder.StandardMessageDecoderImpl;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.StandardInboundMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemarrived.ItemArrivedMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itempickedup.ItemPickedUpMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemreleased.ItemReleasedMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemstored.ItemStoredMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.locationstatus.LocationStatusMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.transaction.EBSTransactionContext;

/**
 * This class will process the incoming plc message. Received text is converted and invoke the business logic for the
 * message.
 * 
 * @author Administrator
 *
 */
public class StandardMessageProcessor extends PLCBaseMsgBuilder {

    protected static final class MSG_PROCESS_STATUS {
        /** 1:Success */
        public static final int SUCCESS = 1;

        /** 0:Failure */
        public static final int FAILURE = 0;
    }

    protected PLCMessageData mpMessageData;

    protected Logger logger = Logger.getLogger();

    public StandardMessageProcessor() {

    }

    /**
     * This method will process the received message from PLC/ACP.
     * 
     * @return {@link MSG_PROCESS_STATUS#FAILURE} for fail / {@link MSG_PROCESS_STATUS#SUCCESS} for successful finish.
     */
    public int process() {
        int status = MSG_PROCESS_STATUS.FAILURE;// fail
        try {
            // Set the received text message type
            // Getting the PLC Inbound Message
            StandardInboundMessage plcInboundMessage = getMessageHandler();
            if (plcInboundMessage != null) {
                // Parse the received message into Inbound Message
                plcInboundMessage.parse(getReceivedText());
                // From the device which message was sent
                plcInboundMessage.setDeviceId(getDeviceId());
                // send the Ack msg
                plcInboundMessage.processAck();

                // Check whether it is valid
                if (plcInboundMessage.isValid()) {
                    // Do processing of the message.
                    EBSTransactionContext ebsTransactionContext = plcInboundMessage.process();
                    if (ebsTransactionContext != null && ebsTransactionContext.isSuccess()) {
                        plcInboundMessage.outputTransactionLog(ebsTransactionContext);
                        status = MSG_PROCESS_STATUS.SUCCESS;
                    }

                }

            } else {
                // log message is add in the StandardInboundMessageFactory class.
                logger.logError("PLC Device Msg Type is missing. \"" + mpMessageData.getDeviceId() + "\" Msg:"
                        + getReceivedText());
            }
        } catch (DBException e) {
            logger.logRxEquipmentMessage(getReceivedText(), getReceivedText());
            logger.logError("PLC Device Msg Type \"" + mpMessageData.getDeviceId() + "\" NOT Processed.");
            logger.logError(
                    "PLC Device Msg \"" + getReceivedText() + "\" NOT Processed. Due to exception:" + e.getMessage());
        } catch (Throwable t) {
            // try to write out something
            String message = "Exception = deviceID = " + getDeviceId() + ", message type = " + getMsgType()
                    + ", received text = " + getReceivedText() + ", exception message = " + t.getMessage();
            logger.logError(message);
            logger.logException(message, t);
            throw t;
        }

        return status;
    }

    public PLCMessageData getMessageData() {
        return mpMessageData;
    }

    public void setMessageData(PLCMessageData mpBCSMessage) {
        this.mpMessageData = mpBCSMessage;
    }

    public String getReceivedText() {
        return mpMessageData.getMessageAsString();
    }

    public String getMsgType() {
        return mpMessageData.getMessageId();
    }

    public String getDeviceId() {
        return mpMessageData.getDeviceId();
    }

    /**
     * Decodes the receive message from PORT
     * 
     * @param receivedByteBuffer
     * @param mpBCSMessageHeader
     * @return
     */
    public String decodeReceivedData(byte[] receivedByteBuffer, PLCMessageHeader mpBCSMessageHeader) {

        String receivedDataString = "";
        // instantiate standard decoder-> this can be override in factory.property file
        StandardMessageDecoder mInboundMessageDecoder = Factory.create(StandardMessageDecoderImpl.class);
        receivedDataString = mInboundMessageDecoder.decode(mpBCSMessageHeader, receivedByteBuffer);

        return receivedDataString;
    }

    public StandardInboundMessage getMessageHandler() {

        StandardInboundMessage plcInboundMessage = null;

        switch (getMsgType()) {
        case PLCConstants.PLC_ITEM_STORED_MSG_TYPE:
            plcInboundMessage = Factory.create(ItemStoredMessage.class);
            break;
        case PLCConstants.PLC_ITEM_PICKEDUP_MSG_TYPE:
            plcInboundMessage = Factory.create(ItemPickedUpMessage.class);
            break;
        case PLCConstants.PLC_LOCATION_STATUS_MSG_TYPE:
            plcInboundMessage = Factory.create(LocationStatusMessage.class);
            break;
        case PLCConstants.PLC_ITEM_ARRIVED_MSG_TYPE:
            plcInboundMessage = Factory.create(ItemArrivedMessage.class);
            break;
        case PLCConstants.PLC_ITEM_RELEASED_MSG_TYPE:
            plcInboundMessage = Factory.create(ItemReleasedMessage.class);
            break;
        default:
            logger.logRxEquipmentMessage(getReceivedText(), getReceivedText());
            logger.logError("PLC Device Msg Type \"" + getMsgType() + "\" NOT Processed - processEquipmentEvent()");
            break;
        }

        return plcInboundMessage;
    }
}
