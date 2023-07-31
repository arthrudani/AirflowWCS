package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemstored;

import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLoadServer;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSDBConstants.MSG_ACKNOWLEDGEMENT_STATUS;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.StandardInboundMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.StandardItemStoredTransaction;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound.PLCStandardAckMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.transaction.EBSTransactionContext;

import io.netty.util.internal.StringUtil;

/**
 * Item Storage completion message received from PLC.
 * 
 * @author DK
 *
 */
public class ItemStoredMessage extends StandardInboundMessage {
    private EBSLoadServer mpEBSLoadServer = Factory.create(EBSLoadServer.class);

    String sOrderId = "";
    String sLoadId = ""; // Tray,Container id
    String sGlobalId = ""; // Global Id
    String sLineId = ""; // sBarcode
    String addressId = ""; // Lane ID / Unique Device ID
    int status; // Status Flags

    public ItemStoredMessage() {
        super();
    }

    public ItemStoredMessage(String fullMsg) {
        parse(fullMsg);
    }

    public String getGlobalId() {
        return sGlobalId;
    }

    public void setGlobalId(String globalId) {
        this.sGlobalId = globalId;
    }

    public String getLoadId() {
        return sLoadId;
    }

    public void setLoadId(String loadId) {
        this.sLoadId = loadId;
    }

    public String getOrderId() {
        return sOrderId;
    }

    public void setOrderId(String orderId) {
        this.sOrderId = orderId;
    }

    public String getLineId() {
        return sLineId;
    }

    public void setLineId(String lineId) {
        this.sLineId = lineId;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        // this.addressId = addressId;
        if (addressId != null) {
            try {
                addressId = String.format("%010d", Integer.parseInt(addressId));// Conversion is required as the storage
                                                                                // location address is 9 digits

            } catch (NumberFormatException e) {

            }
        }

        this.addressId = addressId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setStatus(String status) {
        try {
            this.status = Integer.parseInt(status);
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            this.status = 2;
        }
    }

    @Override
    public boolean isValid() throws DBException {
        boolean isValid = true;

        if (!StringUtil.isNullOrEmpty(getLoadId()) && !StringUtil.isNullOrEmpty(getAddressId())) {
            Load mpLoad = Factory.create(Load.class);
            LoadData loadData = mpLoad.getLoadData(getLoadId());
            if (loadData == null) {
                isValid = false;
                logger.logError(
                        "The load for load id " + getLoadId() + " not found for item stored message processing");
                throw new DBException("Load Record for Load " + getLoadId() + " not found!");
            }
        }

        return isValid;
    }

    @Override
    public void processAck() {
        PLCStandardAckMessage itemAckMsg = Factory.create(PLCStandardAckMessage.class);
        itemAckMsg.setDeviceId(getDeviceId());
        itemAckMsg.setSerialNum(getSerialNumber());
        itemAckMsg.setMessageType(PLCConstants.PLC_ITEM_STORED_ACK_MSG_TYPE);
        itemAckMsg.setStatus(String.valueOf(MSG_ACKNOWLEDGEMENT_STATUS.OK));
        itemAckMsg.sendMessageToPlc();
    }

    @Override
    public EBSTransactionContext process() {
        ItemStoredContext plcItemStoredContext = null;
        try {
            logger.logDebug("Calling Process store complete method, Load Id" + getLoadId());
            plcItemStoredContext = new ItemStoredContext(getOrderId(), getLoadId(), getAddressId(), getStatus(),
                    getGlobalId(), getLineId(), getDeviceId());
            Factory.create(StandardItemStoredTransaction.class).execute(plcItemStoredContext);
        } catch (DBException ex) {
            logger.logException(ex);
        }
        return plcItemStoredContext;
    }

    @Override
    public boolean parse(String receivedText) {
        boolean isParsingSuccess = false;
        if (!StringUtil.isNullOrEmpty(receivedText)) {
            String[] splitedMsg = receivedText.split(PLCConstants.DELIM_COMMA);
            if (splitedMsg != null) {
                // the first item will be MsgType and second will be the SerialNum from header
                setSerialNumber(splitedMsg[1]);
                // Message body start from here
                setOrderId(splitedMsg[2]);
                setLoadId(splitedMsg[3]); // Tray,Container id
                setGlobalId(splitedMsg[4]); // Global Id
                setLineId(splitedMsg[5]); // Bar-code ID
                setAddressId(splitedMsg[6]);// Lane ID / Unique Device ID
                setStatus(splitedMsg[7]);// Status
                isParsingSuccess = true;
            }
        }
        return isParsingSuccess;
    }

    @Override
    public void outputTransactionLog(EBSTransactionContext ebsTransactionContext) {
        ItemStoredContext itemStoredContext = (ItemStoredContext) ebsTransactionContext;
        LoadData loadData = itemStoredContext.getLoadData();
        if (loadData != null) {
            mpEBSLoadServer.logLoadStorageCompleteTransaction(loadData.getLoadID(), loadData.getAddress(),
                    loadData.getDeviceID());
        }
    }

}
