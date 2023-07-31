package com.daifukuoc.wrxj.custom.ebs.host.handler;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.IpcMessage;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSInventoryServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSPoReceivingServer;
import com.daifukuoc.wrxj.custom.ebs.host.messages.ExpectedReceiptAckMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.ExpectedReceiptMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.messages.ExpectedReceiptResponseMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.host.processor.ProcessorFactory;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.EmptyLocationFinder;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.AlreadyStoredLoadException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.InvalidExpectedReceiptException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.LoadCreationOrUpdateFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.LoadSearchingFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.LocationReservationFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.LocationSearchingFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.NoRemainingEmptyLocationException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.POCreationFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.StationSearchingFailureException;

/**
 * This host message handler processes only expected receipt message event - ExpectedReceiptParser saves expected
 * receipt message into purchase order header/line tables and then sends an host expected receipt message event - This
 * handler monitors the event and then searches the empty location to send a reply back to Host with the found empty
 * location's entrance station id
 * 
 * @author LK
 *
 */
public class HostExpectedReceiptMessageHandler extends Controller {

    private ExpectedReceiptMessageData expectedReceiptMessageData;
    private EBSInventoryServer inventoryServer;
    private EmptyLocationFinder emptyLocationFinder;

    public HostExpectedReceiptMessageHandler() {
        super();
    }

    /**
     * See {@link com.daifukuamerica.wrxj.controller.ControllerFactory#startController(String)} ControllerFactory
     * expects this method to be available in the controller class
     * 
     * @param controllerConfigs ReadOnlyProperties
     * @return Controller
     * @throws ControllerCreationException When failed to create a controller
     */
    public static Controller create(ReadOnlyProperties controllerConfigs) throws ControllerCreationException {
        Controller thisController = Factory.create(HostExpectedReceiptMessageHandler.class);
        return thisController;
    }

    @Override
    protected void initialize(String isControllerKeyName) {
        super.initialize(isControllerKeyName);

        logger.logDebug("HostExpectedReceiptMessageHandler.initialize() - Start");

        // Subscribe to the 'ExpectedReceipt' event
        super.subscribeHostExpectedReceiptEvent("%");

        logger.logDebug("HostExpectedReceiptMessageHandler.initialize() - End");
    }

    @Override
    protected void startup() {
        super.startup();

        logger.logDebug("HostExpectedReceiptMessageHandler.startup() - Start");

        try {
            expectedReceiptMessageData = Factory.create(ExpectedReceiptMessageData.class);
            inventoryServer = Factory.create(EBSInventoryServer.class);

            // Load a processor configured in host config table
            emptyLocationFinder = (EmptyLocationFinder) ProcessorFactory.get(controllersKeyName,
                    EmptyLocationFinder.NAME);

            // Mark this controller as running
            super.setControllerStatus(ControllerConsts.STATUS_RUNNING);

        } catch (Exception e) {
            setControllerStatus(ControllerConsts.STATUS_ERROR);
            logger.logException(e, "Error in starting up HostExpectedReceiptMessageHandler");
        }

        logger.logDebug("HostExpectedReceiptMessageHandler.startup() - End");
        setDetailedControllerStatus("HostExpectedReceiptMessageHandler started up.");
    }

    @Override
    protected void shutdown() {
        logger.logDebug("HostExpectedReceiptMessageHandler.shutdown() - Start");

        if (inventoryServer != null) {
            inventoryServer.cleanUp();
        }

        logger.logDebug("HostExpectedReceiptMessageHandler.shutdown() - End");
        setDetailedControllerStatus("HostExpectedReceiptMessageHandler terminated.");

        super.shutdown();
    }

    @Override
    protected void processIPCReceivedMessage() {
        super.processIPCReceivedMessage();

        logger.logDebug("HostExpectedReceiptMessageHandler.processIPCReceivedMessage() - Start");
        if (!receivedMessageProcessed) {
            if (receivedEventType == MessageEventConsts.HOST_EXPECTED_RECEIPT_EVENT_TYPE) {
                logger.logDebug("HostExpectedReceiptMessageHandler received expected receipt event");

                // AbstractIPCMessenger#publishHostExpectedReceiptEvent(...) puts the csv formatted expected receipt
                // message into the event message
                if (expectedReceiptMessageData.parse(receivedText)) {
                    // Send ack before processing
                    sendExpectedReceiptAckMsg((short) expectedReceiptMessageData.getHeader().getSeqNo(), false);

                    // Now we have only ADD or CANCEL as UPDATE is not used at the moment
                    if (expectedReceiptMessageData
                            .getRequestType() == SACControlMessage.ExpectedReceiptsRequest.REQUEST_TYPE.ADD) {

                        String entranceStation = null;
                        boolean isError = true;
                        try {
                            // Let's find an empty location for the PO
                            entranceStation = emptyLocationFinder.find(expectedReceiptMessageData);

                            isError = false;
                        } catch (LoadSearchingFailureException e) {    
                            logger.logException("Failed to search a load", e);
                            entranceStation = null;
                        } catch (AlreadyStoredLoadException e) {    
                            logger.logException("Already stored load", e);
                            entranceStation = null;
                        } catch (StationSearchingFailureException e) {    
                            logger.logException("Failed to search a station of location", e);
                            entranceStation = null;
                        } catch (LocationReservationFailureException e) {    
                            logger.logException("Failed to reserve a location", e);
                            entranceStation = null;
                        } catch (POCreationFailureException e) {
                            logger.logException("Failed to create a new PO", e);
                            entranceStation = null;
                        } catch (NoRemainingEmptyLocationException e) {
                            // When storage is full, we should reply 0 which means storage full rather than 2(error)
                            logger.logException("No remaining empty location", e);
                            entranceStation = null;
                            isError = false;
                        } catch (LocationSearchingFailureException e) {
                            logger.logException("Location searching failed", e);
                            entranceStation = null;
                        } catch (InvalidExpectedReceiptException e) {
                            logger.logException("Invalid expected receipt", e);
                            entranceStation = null;
                        } catch (LoadCreationOrUpdateFailureException e) {
                            logger.logException("Failed to create or update a load", e);
                            entranceStation = null;
                        } finally {
                            // Now, let's send an expected receipt response message(message id 22) to SAC/HOST
                            // http://softwaredoc.daifukuoc.com/airflowwcs/technical-guide/messages/sac-messages/expected-receipt-response.html
                            sendExpectedReceiptResponseMsg((short) expectedReceiptMessageData.getHeader().getSeqNo(),
                                    expectedReceiptMessageData.getOrderId(), expectedReceiptMessageData.getLoadId(),
                                    expectedReceiptMessageData.getGlobalId(), expectedReceiptMessageData.getLineId(),
                                    entranceStation, isError);
                        }
                    } else if (expectedReceiptMessageData
                            .getRequestType() == SACControlMessage.ExpectedReceiptsRequest.REQUEST_TYPE.CANCEL) {
                        
                    	boolean isError = true;
                    	try {
							emptyLocationFinder.cancel(expectedReceiptMessageData);
							isError = false;
						} catch (InvalidExpectedReceiptException e) {
							logger.logException("Invalid expected receipt", e);
						} catch (LoadSearchingFailureException e) {
							logger.logException("Failed to search a load", e);
						} catch (DBException e) {
							logger.logException("Got database exception", e);
						} finally {
							sendExpectedReceiptResponseMsgForUpdateOrCancel((short) expectedReceiptMessageData.getHeader().getSeqNo(),
	                                expectedReceiptMessageData.getOrderId(), expectedReceiptMessageData.getLoadId(),
	                                expectedReceiptMessageData.getGlobalId(), expectedReceiptMessageData.getLineId(), null,
	                                isError);
						}
                    } else if (expectedReceiptMessageData.getRequestType() == SACControlMessage.ExpectedReceiptsRequest.REQUEST_TYPE.UPDATE) {
                    	boolean isError = true;
                    	try {
							emptyLocationFinder.update(expectedReceiptMessageData);
							isError = false;
						} catch (InvalidExpectedReceiptException e) {
							logger.logException("Invalid expected receipt", e);
						} catch (LoadSearchingFailureException e) {
							logger.logException("Failed to search a load", e);
						} catch (DBException e) {
							logger.logException("Got database exception", e);
						} finally {
							sendExpectedReceiptResponseMsgForUpdateOrCancel((short) expectedReceiptMessageData.getHeader().getSeqNo(),
	                                expectedReceiptMessageData.getOrderId(), expectedReceiptMessageData.getLoadId(),
	                                expectedReceiptMessageData.getGlobalId(), expectedReceiptMessageData.getLineId(), null,
	                                isError);
						}
                    }
                } else {
                    logger.logError(
                            "HostExpectedReceiptMessageHandler received an expected receipt message with invalid values:"
                                    + receivedText);
                    int originalSequenceNumber = 0;
                    if (expectedReceiptMessageData.getHeader() != null) {
                        originalSequenceNumber = expectedReceiptMessageData.getHeader().getSeqNo();
                    }

                    // Send ack before response
                    sendExpectedReceiptAckMsg((short) originalSequenceNumber, true);
                    // Send response
                    sendExpectedReceiptResponseMsg((short) originalSequenceNumber, "", "", "", "",
                            String.valueOf(SACControlMessage.ExpectedReceiptsResponse.LOCATION.NOT_FOUND), true);
                }
            } else {
                logger.logError(
                        "HostExpectedReceiptMessageHandler received unexpected event type: " + receivedEventType);
            }
            // Now mark as it's been processed
            receivedMessageProcessed = true;
        }
        logger.logDebug("HostExpectedReceiptMessageHandler.processIPCReceivedMessage() - End");
    }

    private void sendExpectedReceiptResponseMsgForUpdateOrCancel(short originalSequenceNumber, String orderId, String loadId, String globalId,
			String lineId, String stationId, boolean isError) {

    	ExpectedReceiptResponseMessage responseMessage = Factory.create(ExpectedReceiptResponseMessage.class);

        responseMessage.setOrderId(orderId);
        responseMessage.setLoadId(loadId);
        responseMessage.setGlobalId(globalId);
        responseMessage.setLineId(lineId);
        responseMessage.setEntranceStationId(String.valueOf(SACControlMessage.ExpectedReceiptsResponse.LOCATION.NOT_FOUND));
        
        if(isError) {
        	responseMessage.setStatus(SACControlMessage.ExpectedReceiptsResponse.STATUS.ERROR);
        } else {
        	responseMessage.setStatus(SACControlMessage.ExpectedReceiptsResponse.STATUS.SUCCESS);
        }
        
        responseMessage.format();
        byte[] encoded = responseMessage.prepareMessageToSend((int) originalSequenceNumber);
        String messageToSend = new String(encoded);
        publishHostMesgSendEvent(messageToSend, 0,SACControlMessage.HOST_PORT_EVENT);
	}

	private void sendExpectedReceiptAckMsg(short originalSequenceNumber, boolean isError) {
        ExpectedReceiptAckMessage ackMessage = Factory.create(ExpectedReceiptAckMessage.class);

        if (isError) {
            // When error is set
            ackMessage.setStatus(SACControlMessage.AckStatus.MESSAGE_ERROR.getValue());
        } else {
            ackMessage.setStatus(SACControlMessage.AckStatus.OK.getValue());
        }

        ackMessage.format();
        byte[] encoded = ackMessage.prepareMessageToSend((int) originalSequenceNumber);
        String messageToSend = new String(encoded);
        publishHostMesgSendEvent(messageToSend, 0, SACControlMessage.HOST_PORT_EVENT);
    }

    private void sendExpectedReceiptResponseMsg(short originalSequenceNumber, String orderId, String loadId,
            String globalId, String lineId,
            String entranceStationId, boolean isError) {
        ExpectedReceiptResponseMessage responseMessage = Factory.create(ExpectedReceiptResponseMessage.class);

        responseMessage.setOrderId(orderId);
        responseMessage.setLoadId(loadId);
        responseMessage.setGlobalId(globalId);
        responseMessage.setLineId(lineId);

        if (isError) {
            // When error is set
            responseMessage.setEntranceStationId(
                    String.valueOf(SACControlMessage.ExpectedReceiptsResponse.LOCATION.NOT_FOUND));
            responseMessage.setStatus(SACControlMessage.ExpectedReceiptsResponse.STATUS.ERROR);
        } else if (entranceStationId != null && entranceStationId.trim().length() > 0) {
            // When empty location is found
            responseMessage.setEntranceStationId(entranceStationId);
            responseMessage.setStatus(SACControlMessage.ExpectedReceiptsResponse.STATUS.SUCCESS);
        } else {
            // When no empty location is left
            responseMessage.setEntranceStationId(
                    String.valueOf(SACControlMessage.ExpectedReceiptsResponse.LOCATION.NOT_FOUND));
            responseMessage.setStatus(SACControlMessage.ExpectedReceiptsResponse.STATUS.NOT_AVAILABLE);
        }

        responseMessage.format();
        byte[] encoded = responseMessage.prepareMessageToSend((int) originalSequenceNumber);
        String messageToSend = new String(encoded);
        publishHostMesgSendEvent(messageToSend, 0,SACControlMessage.HOST_PORT_EVENT);
    }

    @Override
    protected void decodeIpcMessage(IpcMessage receivedMessage) {
        // FIXME: This is only required for unit testing
        super.decodeIpcMessage(receivedMessage);
    }
}
