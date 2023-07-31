/**
 * 
 */
package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.locationstatus;

import java.util.ArrayList;
import java.util.List;

import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSDBConstants.MSG_ACKNOWLEDGEMENT_STATUS;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.StandardInboundMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.locationstatus.PLCLocationStatusTransaction.LocationStatus;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound.PLCStandardAckMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.transaction.EBSTransactionContext;
import com.daifukuoc.wrxj.custom.ebs.util.EBSLogger;

import io.netty.util.internal.StringUtil;

public class LocationStatusMessage extends StandardInboundMessage {

    protected EBSLogger logger = new EBSLogger(Logger.getLogger());

    int sStatus; // 1 = succeed, 2 = Error, 3 = Unexpected Bin Empty Error
    String sOrderId = "";// Order Id - Move order request ID
    String sLoadId = ""; // Tray,Container id
    String sGlobalId = "";// Global I
    String sLineId = ""; // sBarcode
    String sFromLocationId = ""; // Storage location/inbound/outbound station ID where the item has been picked
                                 // up
    String sToLocationId = ""; // Lifter/shuttle location of where the item is now if succeed. 0 if pickup
                               // failed.

    List<LocationStatus> locationStatusList;

    public List<LocationStatus> getLocationStatusList() {
        return locationStatusList;
    }

    public void setLocationStatusList(List<LocationStatus> locationStatusList) {
        this.locationStatusList = locationStatusList;
    }

    public LocationStatusMessage() {
        // TODO Auto-generated constructor stub
    }

    /*
     * Validate the PLC response message based on the message type the validation would be different need to add pass
     * the parse manager object and the msg type
     */
    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean parse(String sMsg) {
        logger.logDebug("Parse PLC Location Status Message : %s", sMsg);
        setLocationStatusList(new ArrayList<>());
        if (!StringUtil.isNullOrEmpty(sMsg)) {
            String[] splitedMsg = sMsg.split(PLCConstants.DELIM_COMMA);
            if (splitedMsg.length > 2) {
                setSerialNumber(splitedMsg[1]);

                if (splitedMsg != null && (splitedMsg.length - 2) % 2 == 0) {
                    for (int i = 2; i < splitedMsg.length; i += 2) {
                        try {
                            getLocationStatusList().add(new PLCLocationStatusTransaction.LocationStatus(
                                    String.format("%010d", Integer.parseInt(splitedMsg[i])),
                                    Short.parseShort(splitedMsg[i + 1])));
                        } catch (NumberFormatException ex) {
                            logger.logDebug("Wrong input");
                            getLocationStatusList().clear();
                            return false;
                        }
                    }
                    logger.logDebug("PLC Location Status Message Parsed sucessfully");
                    return true;
                } else {
                    logger.logDebug("Invalid PLC Location Status Message , number of elemens is %d", splitedMsg.length);
                    return false;
                }
            }
            return false;
        } else {
            logger.logDebug("PLC Location Status Message from PLC for parsing Parse is Empty");
            return false;
        }
    }

    @Override
    public EBSTransactionContext process() {
        LocationStatusContext plcLocationStatusContext = new LocationStatusContext(getDeviceId(),
                getLocationStatusList());
        try {
            Factory.create(PLCLocationStatusTransaction.class).execute(plcLocationStatusContext);
        } catch (DBException ex) {
            logger.logException(ex);
        }
        return plcLocationStatusContext;
    }

    @Override
    public void processAck() {
        PLCStandardAckMessage itemAckMsg = Factory.create(PLCStandardAckMessage.class);
        itemAckMsg.setDeviceId(getDeviceId());
        itemAckMsg.setSerialNum(getSerialNumber());
        itemAckMsg.setMessageType(PLCConstants.PLC_LOCATION_STATUS_ACK_MSG_TYPE);
        itemAckMsg.setStatus(String.valueOf(MSG_ACKNOWLEDGEMENT_STATUS.OK));
        itemAckMsg.sendMessageToPlc();
    }

}
