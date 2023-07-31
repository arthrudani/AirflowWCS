package com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound;

import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;

/**
 * PLC Item Stored Acknowledgement message class is used to send to ACP/PLC to acknowledge receiving the Item Stroed
 * message
 * 
 * @author KR
 *
 */
public class PLCStandardAckMessage extends StandardOutboundMessage {
    protected String status = "";
    protected String messageType;
    
    public PLCStandardAckMessage() {
    }

    public PLCStandardAckMessage(String status) {
        this.status = status;
    }

    public PLCStandardAckMessage(String status, String iMessageType) {
        this.status = status;
        messageType = iMessageType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    @Override
    public String constructSendMessagetoPlc() {
        StringBuilder sendMessageBuilder = new StringBuilder();

        sendMessageBuilder.append(this.messageType);
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        sendMessageBuilder.append(this.getSerialNum());
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        sendMessageBuilder.append(getStatus());

        return sendMessageBuilder.toString();
    }
}
