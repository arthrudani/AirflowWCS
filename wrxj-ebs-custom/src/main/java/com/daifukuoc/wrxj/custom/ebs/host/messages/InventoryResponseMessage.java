/**
 * 
 */
package com.daifukuoc.wrxj.custom.ebs.host.messages;

import com.daifukuamerica.wrxj.host.messages.MessageFormatter;
import com.daifukuamerica.wrxj.host.messages.MessageFormatterFactory;
import com.daifukuamerica.wrxj.host.messages.MessageOut;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;

/**
 * @author BT
 *
 */
public class InventoryResponseMessage extends MessageOut {

    protected final String MSGID_NAME = "sMsgID"; 
	protected final String REQUESTID_NAME = "sRequestID";
	protected final String STATUS_NAME = "iStatus";
	protected final String ARRAY_LENGTH = "iArrayLength";
	protected final String ARRAY_OF_BAGS = "";

	String sRequestID = ""; // Request Id
	int iStatus = 1; // Status (1=Succeed, 2=Failed, 3=Processed with shortage)
	int iArrayLength = 0;
	String sArrayOfBags = ""; // item id, 12 bytes character

	/**
	 * Default constructor. This constructor finds the correct message formatter to
	 * use for this message.
	 */
	 public InventoryResponseMessage() {
	        this(null);
	    }
	
	public InventoryResponseMessage(MessageFormatter messageFormatter) {
		messageFields = new ColumnObject[] {
				new ColumnObject(MSGID_NAME, SACControlMessage.INVENTORY_RESPONSE_MSG_TYPE),
				new ColumnObject(REQUESTID_NAME, sRequestID), new ColumnObject(STATUS_NAME, 1),
				new ColumnObject(ARRAY_LENGTH, iArrayLength),
				new ColumnObject(ARRAY_OF_BAGS, sArrayOfBags),

		};
		if (messageFormatter == null) {
            msgfmt = MessageFormatterFactory.getInstance();
        } else {
            // Only for unit testing
            msgfmt = messageFormatter;
        }
		enumMessageName = MessageOutNames.INVENTORY_REQUEST_BY_FLIGHT;
	}

	public void setOrderID(short sOrderID) {
		ColumnObject.modify(REQUESTID_NAME, sOrderID, messageFields);
	}

	public void setStatus(int iStatus) {
		ColumnObject.modify(STATUS_NAME, iStatus, messageFields);
	}

	public void setArrayLength(int iArrayLength) {
		ColumnObject.modify(ARRAY_LENGTH, iArrayLength, messageFields);
	}

	public void setArrayOfBags(String string) {
		ColumnObject.modify(ARRAY_OF_BAGS, string, messageFields);
	}

	/* get */
	public String getRequestID() {
		return sRequestID;
	}

	public int getStatus() {
		return iStatus;
	}

	public int getArrayLength() {
		return iArrayLength;
	}

	public String getArrayOfBags() {
		return sArrayOfBags;
	}
}
