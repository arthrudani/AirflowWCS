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
public class RetrievalOrderResponseMessage extends MessageOut {

    public static final String MSGID_NAME = "MsgID";
    public static final String ORDERID_NAME = "OrderID";
    public static final String STATUS_NAME = "Status";
    public static final String NUMBER_OF_MISSING_BAGS_NAME = "NumberOfMissingBags";

    /**
     * Default constructor. This constructor finds the correct message formatter to use for this message.
     */
    public RetrievalOrderResponseMessage() {
        this(null);
    }
    
    public RetrievalOrderResponseMessage(MessageFormatter messageFormatter) {
        messageFields = new ColumnObject[] {
                new ColumnObject(MSGID_NAME, SACControlMessage.RETRIEVAL_FLIGHT_RESPONSE_MSG_TYPE),
                new ColumnObject(ORDERID_NAME, 0), 
                new ColumnObject(STATUS_NAME, 1),
                new ColumnObject(NUMBER_OF_MISSING_BAGS_NAME, 0) };
        if (messageFormatter == null) {
            msgfmt = MessageFormatterFactory.getInstance();
        } else {
            // Only for unit testing
            msgfmt = messageFormatter;
        }
        enumMessageName = MessageOutNames.ORDER_COMPLETE;
    }

    public void setOrderID(short orderID) {
        ColumnObject.modify(ORDERID_NAME, orderID, messageFields);
    }

    public void setStatus(short status) {
        ColumnObject.modify(STATUS_NAME, status, messageFields);
    }

    public void setNumberOfMissingBags(short numberOfMissingBags) {
        ColumnObject.modify(NUMBER_OF_MISSING_BAGS_NAME, numberOfMissingBags, messageFields);
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
