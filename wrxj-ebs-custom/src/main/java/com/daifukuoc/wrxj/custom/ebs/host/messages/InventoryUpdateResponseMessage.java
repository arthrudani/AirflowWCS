package com.daifukuoc.wrxj.custom.ebs.host.messages;

import com.daifukuamerica.wrxj.host.messages.MessageFormatter;
import com.daifukuamerica.wrxj.host.messages.MessageFormatterFactory;
import com.daifukuamerica.wrxj.host.messages.MessageOut;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;

public class InventoryUpdateResponseMessage extends MessageOut {

	// This message is not directly saved into DB, so the name of fields doesn't matter actually at all.
    public static final String MSGID_NAME = "MessageId";
    public static final String STATUS_NAME = "Status";
	
	/**
     * Default constructor. This constructor finds the correct message formatter to use for this message.
     */
	public InventoryUpdateResponseMessage() {
		this(null);
	}
	
	public InventoryUpdateResponseMessage(MessageFormatter messageFormatter) {
        messageFields = new ColumnObject[] {
                new ColumnObject(MSGID_NAME, SACControlMessage.INVENTORY_UPDATE_ACK_MSG_TYPE),
                new ColumnObject(STATUS_NAME, 0) };
        if (messageFormatter == null) {
            this.msgfmt  = MessageFormatterFactory.getInstance();
        } else {
            // Only for unit testing
            this.msgfmt = messageFormatter;
        }
        enumMessageName = MessageOutNames.INVENTORY_UPDATE;
	}
	
	 public void setStatus(short status) {
	        ColumnObject.modify(STATUS_NAME, status, messageFields);
	    }
	    
	    /**
	     * This is mainly for unit tests
	     * 
	     * @return The array of created ColumnObject
	     */
	    public ColumnObject[] getMessageFields() {
	        return messageFields;
	    }
	    
}
