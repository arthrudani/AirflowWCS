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
 * @author Administrator
 *
 */
public class RetrievalItemResponseMessage extends MessageOut {

    protected final String MSGID_NAME = "sMsgID"; // response msg id = 24
	protected final String ORDERID_NAME = "sOrderID";
	protected final String STATUS_NAME = "iStatus";
	protected final String ARRAY_LENGTH = "iArrayLength";
	protected final String ARRAY_OF_MISSING_BAGS = "";

	String sOrderID = ""; // Global Id
	int iStatus = 1; // Status (1=Succeed, 2=Failed, 3=Processed with shortage)
	int iArrayLength = 0;
	String sArrayOfMissingBags = ""; // item id, 12 bytes character

	/**
	 * Default constructor. This constructor finds the correct message formatter to
	 * use for this message.
	 */
	 public RetrievalItemResponseMessage() {
	        this(null);
	    }
	
	public RetrievalItemResponseMessage(MessageFormatter messageFormatter) {
		messageFields = new ColumnObject[] {
				new ColumnObject(MSGID_NAME, SACControlMessage.RETRIEVAL_ITEM_RESPONSE_MSG_TYPE),
				new ColumnObject(ORDERID_NAME, sOrderID), new ColumnObject(STATUS_NAME, 1),
				new ColumnObject(ARRAY_LENGTH, iArrayLength),
				new ColumnObject(ARRAY_OF_MISSING_BAGS, sArrayOfMissingBags),

		};
		if (messageFormatter == null) {
            msgfmt = MessageFormatterFactory.getInstance();
        } else {
            // Only for unit testing
            msgfmt = messageFormatter;
        }
		enumMessageName = MessageOutNames.ITEMS_ORDER_COMPLETE;
	}

	public void setOrderID(short sOrderID) {
		ColumnObject.modify(ORDERID_NAME, sOrderID, messageFields);
	}

	public void setStatus(int iStatus) {
		ColumnObject.modify(STATUS_NAME, iStatus, messageFields);
	}

	public void setArrayLength(int iArrayLength) {
		ColumnObject.modify(ARRAY_LENGTH, iArrayLength, messageFields);
	}

	public void setArrayOfMissingBags(String string) {
		ColumnObject.modify(ARRAY_OF_MISSING_BAGS, string, messageFields);
	}

	/* get */
	public String getOrderID() {
		return sOrderID;
	}

	public int getStatus() {
		return iStatus;
	}

	public int getArrayLength() {
		return iArrayLength;
	}

	public String getArrayOfMissingBags() {
		return sArrayOfMissingBags;
	}
}
