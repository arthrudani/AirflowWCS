package com.daifukuoc.wrxj.custom.ebs.plc;

import java.util.concurrent.TimeUnit;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.device.controls.AbstractControlsDevice;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.messageformat.controlevent.ControlEventDataFormat;
import com.daifukuamerica.wrxj.messageformat.loadevent.LoadEventDataFormat;
import com.daifukuoc.wrxj.custom.ebs.dataserver.BCSServer;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCMessageData;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.processor.StandardMessageProcessor;

public class PLCDevice extends AbstractControlsDevice {
    protected Integer mpLockExpectedReceipt = 1;
    private PLCMessageData mpBCSMessageData;
    private StandardStationServer mpStationServer;
    private StandardDeviceServer mpDeviceServer;
    protected LoadEventDataFormat mpLEDF = null;
    protected BCSServer mpBCSServer = Factory.create(BCSServer.class);
    protected Logger logger = Logger.getLogger();
    StandardMessageProcessor mpPlcInboundMsgProcessor = null;

    public PLCDevice() {
        mpBCSMessageData = Factory.create(PLCMessageData.class);
        mpControlsMessage = mpBCSMessageData;
    }

    @Override
    public void initialize(String controllerKeyName) {
        super.initialize(controllerKeyName);

        subscribeCustomEvent(getName());
        subscribeLoadEvent(getName());
        //if need to out put all the CMD
        logger.addEquipmentLogger();
    }

    @Override
    public void startup() {
        super.startup();

        mpStationServer = Factory.create(StandardStationServer.class);
        mpDeviceServer = Factory.create(StandardDeviceServer.class);
        mpLEDF = Factory.create(LoadEventDataFormat.class, getName());
    }

    public boolean isPortAvailable() {
        return super.mnEquipmentPortStatus == ControllerConsts.STATUS_RUNNING;
    }

    /**
     * Process System Inter-Process-Communication Message.
     */
    @Override
    protected void processIPCReceivedMessage() {
        super.processIPCReceivedMessage();

        /*
         *Sending message out 
         */

        if (!receivedMessageProcessed) {
            receivedMessageProcessed = true;
            mpLEDF.clearAllData();
            // MCM, EBS sure mnMessageID is reset
            mpLEDF.setMessageID(0);
            mpLEDF.decodeReceivedString(receivedText);
            
           logger.logRxEquipmentMessage(receivedText, "TO-"+ this.controllersKeyName);

            switch (receivedEventType) {
            case MessageEventConsts.EQUIPMENT_EVENT_TYPE:
                processEquipmentEvent();
                break;
            case MessageEventConsts.LOAD_EVENT_TYPE:
                // This was used to handle the messages that needed to sent to PLC, however it is changed and  
            	// for out going messages we don't use this anymore and message directly goes to ACPPort!
                processCustomEvent(super.receivedText);
                break;
            case MessageEventConsts.CUSTOM_EVENT_TYPE:                
                processCustomEvent(super.receivedText);
                break;

            default:
                receivedMessageProcessed = false;
            }
        }
        do {
            mzProcessAgain = false; // Some methods may set this to true.
        } while (mzProcessAgain);
    }

    /**
     * We have received a message from the PORT (PLC) that is connected to the actual Device/Equipment that this
     * Transporter is controlling.
     * 
     * <BR>
     * The received data (String) is in global field "receivedText".
     */
    @Override
    public void processEquipmentEvent() {
    	mpBCSMessageData.toDataValues(receivedText);
    	if(receivedText == null || receivedText.isEmpty()  || receivedText.isBlank())
    	{
    		//Do nothing
    		return;
    	}
        // add device id
        mpBCSMessageData.setDeviceId(this.controllersKeyName);
        if (!mpBCSMessageData.getValidMessage()) {
            String s = mpBCSMessageData.getInvalidMessageDescription();
            // logger.logRxEquipmentMessage(receivedText, s);
            logger.logError("PLCDevice->processEquipmentEvent() -- " + s);
        } else {

            // Process the received text from the PLC 
            setInboundProcessor();
            mpPlcInboundMsgProcessor.setMessageData(mpBCSMessageData);
            int msgProcessStatus = mpPlcInboundMsgProcessor.process();
            logger.logRxEquipmentMessage(receivedText,"FROM -"+this.controllersKeyName);
            //logger.logDebug("ReceivedText:" + receivedText + " processing status:" + msgProcessStatus);
        }
    }

    /**
     * We have received a message from: - the PORT that is connected to the actual Device/Equipment that this
     * Transporter is controlling. - OR EventTimer like Task.BCSExpectedReceiptProcessingTask.class which loads the
     * Expected Receipt from DB - OR from the WEB client like Flush a land request and create and sending Store-Order to
     * PLC
     * 
     * <BR>
     * The received data (String) is in global field "receivedText".
     */
    public void processCustomEvent(String isMessageData) {

        if (isMessageData == null || isMessageData.isBlank()) {
            logger.logError("Invalied message sent to processCustomEvent:" + isMessageData);
            return;
        }

        // We only need to find out if this message need to be sent to PLC port
        String[] sVars = isMessageData.split(PLCConstants.DELIM_COMMA);
        if (sVars == null || sVars.length == 0) {
            logger.logError("Invalied message sent to processCustomEvent:" + isMessageData);
            return;
        }

        switch (sVars[0]) {
        case PLCConstants.PLC_MOVE_ORDER_REQUEST_MSG_TYPE:
        case PLCConstants.PLC_FLUSH_REQUEST_MSG_TYPE:
        case PLCConstants.PLC_ITEM_ARRIVED_ACK_MSG_TYPE:
        case PLCConstants.PLC_ITEM_RELEASED_ACK_MSG_TYPE:
        case PLCConstants.PLC_ITEM_STORED_ACK_MSG_TYPE:
        case PLCConstants.PLC_ITEM_PICKEDUP_ACK_MSG_TYPE:
        case PLCConstants.PLC_LOCATION_STATUS_ACK_MSG_TYPE:
        case PLCConstants.PLC_LINK_STARTUP_MSG_TYPE:
        case PLCConstants.PLC_FLIGHT_DATA_UPDATE_MSG_TYPE:
        case PLCConstants.PLC_BAG_DATA_UPDATE_MSG_TYPE:
            sendToAssociatedPort(isMessageData);
            break;
        default:
            logger.logDebug("BCS Device Custom Msg Type \"" + sVars[0] + "\" NOT Processed - processCustomEvent()");
            break;
        }
    }

    public void sendToAssociatedPort(String isCmdStr) {
        //logger.logTxEquipmentMessage(isCmdStr,"Sending port-"+ this.controllersKeyName);
        transmitCustomEquipmentEvent(isCmdStr);
    }

    /**
     * Check staging, retrieving, and storing requirements
     * 
     * @param isDeviceID
     */
    protected void sendSchedulerWork(String isDeviceID, String sWRxAddress) {

        try {
            if (mpStationServer.exists(sWRxAddress)) {
                sendSchedulerStoreStations(isDeviceID);
                try {
                    TimeUnit.MILLISECONDS.sleep(300);
                } catch (InterruptedException ie) {
                }
                sendSchedulerRetrieveStations(isDeviceID);
            } else {
                sendSchedulerRetrieveStations(isDeviceID);
                try {
                    TimeUnit.MILLISECONDS.sleep(300);
                } catch (InterruptedException ie) {
                }
                sendSchedulerStoreStations(isDeviceID);
            }
        } catch (Exception e) {
            logger.logException("Error checking for work", e);
        }
    }

    /**
     * See if any of this schedulers stations that it is scheduling load to store from needs to have schedule a load to
     * store.
     *
     * @param isDeviceID
     */
    protected void sendSchedulerStoreStations(String isDeviceID) {
        try {
            String sSchedulerName = mpDeviceServer.getSchedulerName(isDeviceID);

            String[] vasStoreStations = mpStationServer.getStationsWithStorePendingLoads(sSchedulerName);
            if (vasStoreStations.length > 0) {
                ThreadSystemGateway.get()
                        .publishControlEvent(ControlEventDataFormat
                                .getCommandTargetListMessage(ControlEventDataFormat.STORE, vasStoreStations),
                                ControlEventDataFormat.TEXT_MESSAGE, sSchedulerName);
            }
        } catch (DBException ex) {
            logger.logException(ex, "Error in CheckForWorkTask");
        }
    }

    /**
     * See if any of this schedulers stations that it is scheduling load to retrieve to can retrieve another load or if
     * it has enough loads retrieving already.
     */
    protected void sendSchedulerRetrieveStations(String isDeviceID) {
        try {
            String sSchedulerName = mpDeviceServer.getSchedulerName(isDeviceID);

            String[] vasRetvStations = mpStationServer.getStationsWithRetrievePendingLoads(sSchedulerName);
            if (vasRetvStations.length > 0) {
                ThreadSystemGateway.get()
                        .publishControlEvent(ControlEventDataFormat
                                .getCommandTargetListMessage(ControlEventDataFormat.RETRIEVE, vasRetvStations),
                                ControlEventDataFormat.TEXT_MESSAGE, sSchedulerName);
            }
        } catch (DBException ex) {
            logger.logException(ex, "Error in CheckForWorkTask");
        }
    }

    /**
     * Give the data to be transmitted to the associated Port and from PORT to PLC without logging
     */
    protected void transmitCustomEquipmentEvent(String isCustomEventString) {
        publishCustomEquipmentEvent(isCustomEventString, 0);
    }

    /**
     * Give the data to be transmitted to the Device/Equipment without logging
     */
    protected void transmitEquipmentEvent(String isEquipmentEventString) {
        publishEquipmentEvent(isEquipmentEventString, 0);
    }

    /**
     * Give the data to be transmitted to the Device/Equipment to the Port.
     */
    protected void transmitEquipmentEvent(String equipmentEventString, String isClarifier) {
        //logger.logTxEquipmentMessage(equipmentEventString, "Trns-"+ this.controllersKeyName);
        publishEquipmentEvent(equipmentEventString, 0);
    }

    /**
     * Factory for ControllerImplFactory.
     * 
     * <p>
     * <b>Details:</b> <code>create</code> is a factory method used exclusively by <code>ControllerImplFactory</code>.
     * Configurable properties of a new controller created using this method are initialized using data in the supplied
     * properties object. If the controller cannot be created, a <code>ControllerCreationException</code> is thrown.
     * </p>
     * 
     * @param ipConfig configurable property definitions
     * @return the created controller
     * @throws ControllerCreationException if an error occurred while creating the controller
     */
    public static Controller create(ReadOnlyProperties ipConfig) throws ControllerCreationException {
        Controller vpController = new PLCDevice();
        vpController.setEquipmentPortCKN(ipConfig.getString(DEVICE_PORT));
        return vpController;
    }

    /**
     * Call and trigger the PLC Flush from the WCS when pass the Device and Station data This will send message to the
     * PLC to clear the lane with the station this will call to the PLC device with the PLC flush request data as
     * follows ex: header + boady = {3,6211,0,0}
     * 
     * @param deviceID
     * @param stationId
     */
    public void sendFlushMessagetoPlc(String deviceID, String stationId) {

        String messageFlush = PLCConstants.PLC_FLUSH_REQUEST_MSG_TYPE + "," + stationId + ","
                + PLCConstants.PLC_FLUSH_REQUEST_MSG_BODY_QTY + ","
                + PLCConstants.PLC_FLUSH_REQUEST_MSG_BODY_RELEASE_INTVL; // sending msg to device to be flush

        mpBCSServer.sendEventToBCSDeviceHandler(deviceID, messageFlush);
        logger.logDebug("Flush msg:" + messageFlush);
    }

    private void setInboundProcessor() {
        if (mpPlcInboundMsgProcessor == null) {
            mpPlcInboundMsgProcessor = Factory.create(StandardMessageProcessor.class);
        }
    }

}
