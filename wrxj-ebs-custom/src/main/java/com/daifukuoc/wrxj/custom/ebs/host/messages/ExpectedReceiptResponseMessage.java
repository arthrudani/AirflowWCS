package com.daifukuoc.wrxj.custom.ebs.host.messages;

import com.daifukuamerica.wrxj.host.messages.MessageFormatter;
import com.daifukuamerica.wrxj.host.messages.MessageFormatterFactory;
import com.daifukuamerica.wrxj.host.messages.MessageOut;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;

/**
 * ExpectedReceiptResponseMessage to send from WRX to Host
 * 
 * @author KR
 *
 */
public class ExpectedReceiptResponseMessage extends MessageOut {
    public static final int NUMBER_OF_FIELDS = 9;

    // This message is not directly saved into DB, so the name of fields doesn't matter actually at all.
    public static final String MSGID_NAME = "MessageId";
    public static final String ORDER_ID_NAME = "OrderId";
    public static final String LOAD_ID_NAME = "LoadId"; // Tray id
    public static final String GLOBAL_ID_NAME = "GlobalId";
    public static final String LINE_ID_NAME = "LineId"; // Bag id
    public static final String ENTRANCE_STATION_ID_NAME = "EntranceStationId";
    public static final String STATUS_NAME = "Status";

    /**
     * Default constructor. This constructor finds the correct message formatter to use for this message.
     */
    public ExpectedReceiptResponseMessage() {
        this(null);
    }

    public ExpectedReceiptResponseMessage(MessageFormatter messageFormatter) {
        messageFields = new ColumnObject[] {
                new ColumnObject(MSGID_NAME, SACControlMessage.EXPECTED_RECIEPT_RESPONSE_MSG_TYPE),
                new ColumnObject(ORDER_ID_NAME, ""),
                new ColumnObject(LOAD_ID_NAME, ""),
                new ColumnObject(GLOBAL_ID_NAME, ""),
                new ColumnObject(LINE_ID_NAME, ""),
                new ColumnObject(ENTRANCE_STATION_ID_NAME, ""),
                new ColumnObject(STATUS_NAME, 1) };

        enumMessageName = MessageOutNames.EXPECTED_RECEIPT_COMPLETE;

        if (messageFormatter == null) {
            // DelimitedFormatter <-- DataType=Delimited in HostConfig table
            msgfmt = MessageFormatterFactory.getInstance();
        } else {
            // Only for unit testing
            msgfmt = messageFormatter;
        }
    }

    public void setOrderId(String orderId) {
        ColumnObject.modify(ORDER_ID_NAME, orderId, messageFields);
    }

    public void setLoadId(String loadId) {
        ColumnObject.modify(LOAD_ID_NAME, loadId, messageFields);
    }

    public void setGlobalId(String globalId) {
        ColumnObject.modify(GLOBAL_ID_NAME, globalId, messageFields);
    }

    public void setLineId(String lineId) {
        ColumnObject.modify(LINE_ID_NAME, lineId, messageFields);
    }

    public void setEntranceStationId(String entranceStationId) {
        ColumnObject.modify(ENTRANCE_STATION_ID_NAME, entranceStationId, messageFields);
    }

    public void setStatus(int status) {
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
