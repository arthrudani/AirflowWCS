package com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound;

import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;

/**
 * PLC flight data update message sent to PLC/ACP
 * 
 * @author LK
 *
 */
public class PLCFlightDataUpdateMessage extends StandardOutboundMessage {
    private String lot = "";
    private String finalSortLocation = "0";
    
    public PLCFlightDataUpdateMessage() {
    }

    public PLCFlightDataUpdateMessage(String lot, String finalSortLocation) {
        super();

        this.lot = lot;
        this.finalSortLocation = finalSortLocation;
    }

    public String getLot() {
        return lot;
    }

    public String getFinalSortLocation() {
        return finalSortLocation;
    }

    @Override
    public String constructSendMessagetoPlc() {
        StringBuilder sendMessageBuilder = new StringBuilder();

        sendMessageBuilder.append(PLCConstants.PLC_FLIGHT_DATA_UPDATE_MSG_TYPE);
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        sendMessageBuilder.append(getSerialNum());
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        sendMessageBuilder.append(getLot());
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        sendMessageBuilder.append(getFinalSortLocation());

        return sendMessageBuilder.toString();
    }
}
