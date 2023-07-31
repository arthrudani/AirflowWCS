package com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound;

import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;

public class PLCFlushRequestMessage extends StandardOutboundMessage {
	public static final int REQUEST_TYPE_PROCESS = 0;
	public static final int REQUEST_TYPE_CANCEL = 1;
    public static final String LANE_DEFAULT_VAL = "NOLANE";// MAX it can be 12 character
    public static final int ORDER_DEFAULT_VAL = 0;

	String orderId = "";// Order Id - Move order request ID
	String laneId = ""; // lane Id
	int quantity = 0;// Quantity - 0 for All I
	int requestType = 0; // Request type - 0 = Process,1 = Cancel
	
	public PLCFlushRequestMessage() {
		
	}

	public String getOrderId() {
		if (orderId == null || orderId.isBlank()) {
            return String.valueOf(PLCFlushRequestMessage.ORDER_DEFAULT_VAL);
        } else {
            return orderId;
        }
	}


	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getLaneId() {
		 if (laneId == null || laneId.isBlank()) {
	            return String.valueOf(PLCFlushRequestMessage.LANE_DEFAULT_VAL);
	        } else {
	            return laneId;
	        }
	}


	public void setLaneId(String laneId) {
		this.laneId = laneId;
	}


	public int getQuantity() {
		return quantity;
	}


	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}


	public int getRequestType() {
		return requestType;
	}


	public void setRequestType(int requestType) {
		this.requestType = requestType;
	}
	
	public PLCFlushRequestMessage(String orderId, String laneId, int quantity, int requestType) {
		super();
		this.orderId = orderId;
		this.laneId = laneId;
		this.quantity = quantity;
		this.requestType = requestType;
	}
	
	@Override
	public String constructSendMessagetoPlc() {
		StringBuilder sendMessageBuilder = new StringBuilder();

        sendMessageBuilder.append(PLCConstants.PLC_FLUSH_REQUEST_MSG_TYPE);
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        // adding received serial number from the header
        sendMessageBuilder.append(getSerialNum());
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        sendMessageBuilder.append(getOrderId());
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        sendMessageBuilder.append(getLaneId());
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        sendMessageBuilder.append(getQuantity());
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        sendMessageBuilder.append(getRequestType());

        return sendMessageBuilder.toString();
	}

}
