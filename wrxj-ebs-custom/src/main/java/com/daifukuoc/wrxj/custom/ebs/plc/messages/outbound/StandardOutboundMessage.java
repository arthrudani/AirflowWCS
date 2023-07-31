package com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound;

import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuoc.wrxj.custom.ebs.dataserver.BCSServer;

/**
 * StandardOutboundMessage abstract class used to send message to the PLC.
 * 
 * @author KR
 *
 */
public abstract class StandardOutboundMessage {

    // FIXME: Remove this 
    private BCSServer mpBCSServer = Factory.create(BCSServer.class);

    private String deviceId = "";
    private String serialNum = "0"; // default

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSerialNum() {
        return this.serialNum;
    }

    public void setSerialNum(String serialNumber) {
        this.serialNum = serialNumber;
    }

    /**
     * This is to construct a message to be sent to PLC in comma separated string.
     */
    public abstract String constructSendMessagetoPlc();

    /**
     * Method construct the message in comma separated string and send the message to the PLC device
     */
    // FIXME: Move this to somewhere else
    public void sendMessageToPlc() {
        String messageToSend = constructSendMessagetoPlc();
        mpBCSServer.sendEventToBCSDeviceHandler(getDeviceId(), messageToSend);
//        logger.logDebug("Outbound Message:" + messageToSend);
    }
}
