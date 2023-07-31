package com.daifukuoc.wrxj.custom.ebs.integration;

import java.util.List;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.IpcMessage;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSTableJoin;
import com.daifukuoc.wrxj.custom.ebs.host.messages.MessageUtil;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.host.util.EBSHostMessageConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound.PLCBagDataUpdateMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound.PLCFlightDataUpdateMessage;

/**
 * This takes care of integration between host/SAC and PLC/ACP
 * 
 * @author LK
 *
 */
public class HostPlcIntegrator extends Controller {
    private final ReadOnlyProperties config;
    private String controllerKeyName = "";
    private EBSTableJoin tableJoin = Factory.create(EBSTableJoin.class);

    public HostPlcIntegrator(ReadOnlyProperties config) {
        super();
        this.config = config;
    }

    public static Controller create(ReadOnlyProperties config) throws ControllerCreationException {
        return new HostPlcIntegrator(config);
    }

    @Override
    public void initialize(String controllerKeyName) {
        super.initialize(controllerKeyName);

        logger.logDebug(getClass().getSimpleName() + ".initialize() - Start");

        // controllerKeyName will be HostIntegrator which is the name of this controller
        this.controllerKeyName = controllerKeyName;

        // Subscribe to host to plc and plc to host events
        subscribeHostToPlcEvent("%");
        subscribePlcToHostEvent("%");

        logger.logDebug(getClass().getSimpleName() + ".initialize() - End");
    }

    @Override
    public void startup() {
        // Not calling AbstractIPCMessenger.startup() as it starts an unnecessary timer thread by default.
        logger.logDebug(getClass().getSimpleName() + ".startup() - Start");

        setControllerStatus(ControllerConsts.STATUS_RUNNING);

        logger.logDebug(getClass().getSimpleName() + ".startup() - End");
    }

    @Override
    protected void shutdown() {
        logger.logDebug(getClass().getSimpleName() + ".shutdown() -- Start");

        logger.logDebug(getClass().getSimpleName() + ".shutdown() -- End");

        super.shutdown();
    }

    @Override
    protected void processIPCReceivedMessage() {
        super.processIPCReceivedMessage();

        if (!receivedMessageProcessed) {
            switch (receivedEventType) {
            case MessageEventConsts.HOST_TO_PLC_EVENT_TYPE:
                processHostToPlcEvent(receivedText);
                receivedMessageProcessed = true;
                break;
            case MessageEventConsts.PLC_TO_HOST_EVENT_TYPE:
                receivedMessageProcessed = true;
                break;
            default:
                receivedMessageProcessed = false;
                break;
            }
        }
    }

    private void processHostToPlcEvent(String hostMessage) {
        String[] split = hostMessage.split(MessageUtil.HOST_INBOUND_MESSAGE_DELIMITER);
        if (split.length == SACControlMessage.FLIGHT_DATA_UPDATE_SPLIT_LEN
                && split[2].equals(String.valueOf(SACControlMessage.FLIGHT_DATA_UPDATE_MSG_TYPE))) {
            // Header has 8 fields, so the first field of body is at 8
            String flight = split[8];
            String finalSortLocationIdString = split[11];
            if (flight == null || flight.isBlank()) {
                logger.logError("Flight number shouldn't be null or empty");
            } else if (finalSortLocationIdString == null || finalSortLocationIdString.isBlank()) {
                logger.logError("Final sort location id shouldn't be null or empty");
            } else {
                int finalSortLocationId = -1;
                try {
                    finalSortLocationId = Integer.parseInt(finalSortLocationIdString);
                } catch (NumberFormatException e) {
                    logger.logError("Final sort location id must be numeric");
                }
                if (finalSortLocationId >= 0) {
                    List<String> devicesToUpdate = null;
                    try {
                        devicesToUpdate = tableJoin.getDevicesWithLoadsOfFlight(EBSHostMessageConstants.WAREHOUSE_NAME,
                                flight);
                    } catch (DBException e) {
                        logger.logException("Failed to get the devices with loads of the flight " + flight, e);
                    }
                    if (devicesToUpdate != null && !devicesToUpdate.isEmpty()) {
                        PLCFlightDataUpdateMessage plcFlightDataUpdateMessage = Factory.create(PLCFlightDataUpdateMessage.class, flight, String.valueOf(finalSortLocationId));
                        for (String device : devicesToUpdate) {
                            plcFlightDataUpdateMessage.setDeviceId(device);
                            plcFlightDataUpdateMessage.sendMessageToPlc();
                        }
                    }
                }
            }
        }
        
		if (split.length == SACControlMessage.INVENTORY_UPDATE_SPLIT_LEN
				&& split[2].equals(String.valueOf(SACControlMessage.INVENTORY_UPDATE_MSG_TYPE))) {
			// Header has 8 fields, so the first field of body is at 8
			String loadID = split[9];
			if (loadID == null || loadID.isBlank()) {
				logger.logError("Load id shouldn't be null or empty");
			} else {
				String deviceToUpdate = null;
				List<LoadLineItemData> loadLineItems = null;
				try {
					deviceToUpdate = tableJoin.getDeviceWithLoad(EBSHostMessageConstants.WAREHOUSE_NAME, loadID);
					loadLineItems = DBHelper.convertData(tableJoin.getLoadLineItemsForThisLoad(loadID), LoadLineItemData.class);
				} catch (DBException e) {
					logger.logException("Failed to get the devices or load line items with load id : " + loadID, e);
				}
				if (deviceToUpdate != null && !deviceToUpdate.isEmpty()) {
					 // TODO: Create and send the message to PLC
					 PLCBagDataUpdateMessage plcBagDataUpdateMessage = Factory.create(PLCBagDataUpdateMessage.class);
					 plcBagDataUpdateMessage.setLoadId(loadID);
					 plcBagDataUpdateMessage.setGlobalId(split[10]);
					 plcBagDataUpdateMessage.setLineId(split[11]);
					 plcBagDataUpdateMessage.setLocationID(split[12]);
					 plcBagDataUpdateMessage.setUpdateType(Integer.parseInt(split[13]));
					 if (!loadLineItems.isEmpty() && loadLineItems != null) 
						 plcBagDataUpdateMessage.setLot(loadLineItems.get(0).getLot());
					 plcBagDataUpdateMessage.setDeviceId(deviceToUpdate);
					 plcBagDataUpdateMessage.sendMessageToPlc();
				}
			}
		}
    }

    @Override
    protected void decodeIpcMessage(IpcMessage receivedMessage) {
        // FIXME: This is only required for unit testing
        super.decodeIpcMessage(receivedMessage);
    }
}
