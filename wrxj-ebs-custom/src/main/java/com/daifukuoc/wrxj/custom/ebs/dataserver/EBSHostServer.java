package com.daifukuoc.wrxj.custom.ebs.dataserver;

import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dataserver.standard.StandardHostServer;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.WrxToHost;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.HostOutDelegate;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSTableJoin;
import com.daifukuoc.wrxj.custom.ebs.host.messages.EBSItemReleaseMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.EBSLoadArrival;
import com.daifukuoc.wrxj.custom.ebs.host.messages.EBSLocationArrival;
import com.daifukuoc.wrxj.custom.ebs.host.messages.EBSStoreCompletionNotifyMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.ExpectedReceiptResponseMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.FlightDataUpdateResponseMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryResponseMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.OrderResponse;
import com.daifukuoc.wrxj.custom.ebs.host.messages.RetrievalItemResponseMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.RetrievalOrderResponseMessage;
import com.daifukuoc.wrxj.custom.ebs.jdbc.BCSMessage;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSConstants;

public class EBSHostServer extends StandardHostServer {

    private EBSOrderServer mpEBSOrderServer;
    private BCSServer mpBCSServer;
    private EBSTableJoin mpTableJoin = new EBSTableJoin();
    protected WrxToHost mpHostHBOut = Factory.create(WrxToHost.class);

    public EBSHostServer() {
        super();
    }

    public EBSHostServer(String keyName) {
        super(keyName);
    }

    public EBSHostServer(String keyName, DBObject dbo) {
        super(keyName, dbo);
    }

    public int getNextSequenceNumberForHeartBeat(String sHostName) throws DBException {
        int vnSequenceNumber = mpHostHBOut.getNextSequenceNumber(sHostName);
        return vnSequenceNumber;
    }

    /**
     * Method to send Location Arrival message when Load is stored into an AS/RS location
     *
     * @param isLoadID the load being stored.
     * @param isStoreWhs the load's store warehouse.
     * @param isStoreAddr the load's store address
     * @throws DBException if there is a database update error.
     */
    public void sendLocationArrival(String isLoadID, String isStoreWhs, String isStoreAddr) throws DBException {
        /*
         * If the host system is not enabled or this outbound message isn't active, don't try to send any messages.
         */
        if (!mzHasHostSystem
                || Application.getInt(MessageOutNames.LOCATION_ARRIVAL.getValue()) == SKDCConstants.INACTIVE_MESSAGE) {
            return;
        }

        // Get item/bagID on load
        String sItem = mpTableJoin.getItemOnLoad(isLoadID);

        EBSLocationArrival vpMesg = new EBSLocationArrival();
        vpMesg.setLoadID(isLoadID);
        vpMesg.setItem(sItem);
        vpMesg.setLocation(isStoreWhs, isStoreAddr);
        vpMesg.format();
        addToDataQueue(new HostOutDelegate(vpMesg));
    }

    /**
     * Method to send Load Arrival when a load arrives at an output station or PD stand.
     *
     * @param isLoadID the arriving load.
     * @param isStation the station at which load arrived.
     * @param isOrderID the Order with which load was requested.
     * @throws DBException if there is a database update error.
     */
    public void sendLoadArrival(String isLoadID, String isStation, String isOrderID) throws DBException {
        /*
         * If the host system is not enabled or this outbound message isn't active, don't try to send any messages.
         */
        if (!mzHasHostSystem
                || Application.getInt(MessageOutNames.LOAD_ARRIVAL.getValue()) == SKDCConstants.INACTIVE_MESSAGE) {
            return;
        }

        // Get item/bagID on load
        String sItem = mpTableJoin.getItemOnLoad(isLoadID);

        EBSLoadArrival vpMesg = new EBSLoadArrival();
//	    vpMesg.setOrderID(isOrderID);
        vpMesg.setLoadID(isLoadID);
        vpMesg.setItem(sItem);
        vpMesg.setArrivalStation(isStation);
        vpMesg.format();
        addToDataQueue(new HostOutDelegate(vpMesg));
    }

    /**
     * Method sends the host an Order Response message.
     *
     */
    public void sendOrderResponse(String isOrder, int inErrorcode, String sErrorDesc) throws DBException {
        OrderLineData vpOLD = Factory.create(OrderLineData.class);

        initializeEBSOrderServer();

        // Get the OrderHeader
        OrderHeaderData ohdata = mpEBSOrderServer.getOrderHeaderRecord(isOrder);
        if (ohdata == null) // Something is really wrong if this
        {
            // log Error
        }

        // don't send unless it's an item order
        if (ohdata.getOrderType() != DBConstants.ITEMORDER) {
            return;
        }

        // Get the OrderLine
        List<Map> vpList = mpEBSOrderServer.getOrderLineData(isOrder);
        if (vpList.size() <= 0) {
            // log Error
        } else {
            vpOLD.dataToSKDCData(vpList.get(0));
        }

        // determine if the Order Response should go to SmartFlow (empty tray request)
        // or BagStage (bag order)
        if (vpOLD.getItem().equals(EBSConstants.EMPTY_TRAY_STACK)) {
            // empty tray requests go to SmartFlow

            // MCM, 11Aug2020
            // Per SmartFlow request respond with Tray stack qty of 3

            // MCM, Sep2020
            // don't send if it's an internal WRx order

            if (isNumeric(ohdata.getOrderID())) {
                sendSmartFlowOrderResponse(ohdata.getDestinationStation(), (int) vpOLD.getOrderQuantity(),
                        EBSConstants.EMPTY_TRAY_STACK_QTY, ohdata.getOrderID());
            }
        } else {
            // All others go to BagStage
            int inOrderStatus = 1; // Successfull

            if (ohdata.getOrderStatus() != DBConstants.SCHEDULED) {
                inOrderStatus = 2;
                inErrorcode = 2;
                sErrorDesc = "Requested Data Not Found";
            }
            sendBagStageOrderResponse(isOrder, vpOLD.getAllocatedQuantity(), inOrderStatus, inErrorcode, sErrorDesc);
        }

    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Method sends the BagStage host an Order Response message.
     *
     */
    public void sendBagStageOrderResponse(String isOrder, double inQtyScheduled, int inOrderStatus, int inErrorcode,
            String sErrorDesc) throws DBException {
        /*
         * If the host system is not enabled or this outbound message isn't active, don't try to send any messages.
         */
        if (!mzHasHostSystem || Application.getInt("OrderResponse") == SKDCConstants.INACTIVE_MESSAGE) {
            return;
        }

        // OrderResponse mesgOut =
        // MessageOutFactory.getInstance(EBSMessageOutNames.ORDER_RESPONSE);
        OrderResponse mesgOut = new OrderResponse();
        mesgOut.setOrderID(isOrder);
        mesgOut.setOrderStatus(inOrderStatus);
        mesgOut.setQtyScheduled(inQtyScheduled);
        mesgOut.setErrorCode(inErrorcode);
        mesgOut.setErrorDesc(sErrorDesc);

        try {
            mesgOut.format();
            addToDataQueue(new HostOutDelegate(mesgOut));
        } catch (DBException exc) {
            throw new DBException("DB exception adding Order Status Message to data queue.", exc);
        } catch (Exception exc) {
            throw new DBException(exc);
        }
    }

    /**
     * Method sends the SmartFlow an Order Response message.
     *
     */
    public void sendSmartFlowOrderResponse(String isDest, int inQtyOrdered, int inQtyScheduled, String isOrder)
            throws DBException {
        initializeBCSServer();

        String isInfo = BCSMessage.BCS_TRAY_RELEASE_RESPONSE + "," + isDest + "," + inQtyOrdered + "," + inQtyScheduled
                + "," + isOrder;

        mpBCSServer.sendEventToBCSDeviceHandler(EBSConstants.BCS_DEVICEID, isInfo);
    }

    /**
     * Method to retrieve a List of inbound or outbound Host messages. <b>Note:</b> This method is safe to use for a
     * transporter as long as there is only one transporter at work. When multiple transporters are involved, records
     * should be retrieved as needed to avoid record processing conflicts. For display purposes this method suffices
     * compared to reading each record individually. Also, this method does <u>not</u> return any CLOB data.
     *
     * @param hostDelegate an object implementing the {@link com.daifukuamerica.wrxj.host.HostServerDelegate
     *        HostServerDelegate} interface.
     * @return List of inbound or outbound messages depending on the info. the delegate is carrying.
     * @throws DBException if there is a database connectivity problem.
     */
    public List<Map> getDataQueueMessagesForWeb() throws DBException {

        List<Map> rtnList = null;
        rtnList = mpTableJoin.getDataQueueMessagesForWeb();
        return rtnList;
    }

    protected void initializeEBSOrderServer() {
        if (mpEBSOrderServer == null) {
            mpEBSOrderServer = Factory.create(EBSOrderServer.class);
        }
    }

    protected void initializeBCSServer() {
        if (mpBCSServer == null) {
            mpBCSServer = Factory.create(BCSServer.class);
        }
    }

    /* KR new methods for */
    /**
     * Adding Expected Receipt Response message to database
     * 
     * @param originalSequenceNumber The sequence number of the request message sent by Host to WRX
     * @param mpResponseMsg
     * @throws DBException
     */
    public void sendExpectedReceiptResponseToHost(int originalSequenceNumber,
            ExpectedReceiptResponseMessage mpResponseMsg) throws DBException {
        /*
         * If the host system is not enabled or this outbound message isn't active, don't try to send any messages.
         */
        if (!mzHasHostSystem || Application
                .getInt(MessageOutNames.EXPECTED_RECEIPT_COMPLETE.getValue()) == SKDCConstants.INACTIVE_MESSAGE) {
            return;
        }
        // format it and save it to db -> asrs.WRXTOHOST
        mpResponseMsg.format();
        addToDataQueue(originalSequenceNumber, new HostOutDelegate(mpResponseMsg));
    }

    // DK:28372 - Bag store complete response message to host
    /**
     * Adding Expected Receipt Response message to database
     * 
     * @param mpResponseMsg
     * @throws DBException
     */
    public void sendBagStoreCompleteResponseToHost(EBSStoreCompletionNotifyMessage mpHostNotifyMessage)
            throws DBException {
        /*
         * If the host system is not enabled or this outbound message isn't active, don't try to send any messages.
         */
        if (!mzHasHostSystem
                || Application.getInt(MessageOutNames.STORE_COMPLETE.getValue()) == SKDCConstants.INACTIVE_MESSAGE) {
            return;
        }
        // format it and save it to db -> asrs.WRXTOHOST
        mpHostNotifyMessage.format();
        addToDataQueue(new HostOutDelegate(mpHostNotifyMessage));
    }

    /**
     * Method to send Load Arrival when a load arrives at an output station (item release).
     *
     * @param isLoadID the arriving load.
     * @param isStation the station at which load arrived.
     * @param isOrderID the Order with which load was requested.
     * @param sItem the Order with which sItem was requested.
     * @throws DBException if there is a database update error.
     */
    public void sendItemReleaseMessageResponseToHost(EBSItemReleaseMessage vpMesg) throws DBException {
        /*
         * If the host system is not enabled or this out bound message isn't active, don't try to send any messages.
         */
        if (!mzHasHostSystem
                || Application.getInt(MessageOutNames.LOAD_ARRIVAL.getValue()) == SKDCConstants.INACTIVE_MESSAGE) {
            return;
        }
        vpMesg.format();
        addToDataQueue(new HostOutDelegate(vpMesg));
    }

    /**
     * Send a retrieval order response message to host
     * @param originalSequenceNumber  The sequence number of the request message sent by Host to WRX
     * @param responseMessage RetrievalOrderResponseMessage to send
     * 
     * @throws DBException If anything goes wrong
     */
    public void sendRetrievalOrderResponseToHost(int originalSequenceNumber, RetrievalOrderResponseMessage responseMessage) throws DBException {
        if (!mzHasHostSystem
                || Application.getInt(MessageOutNames.ORDER_COMPLETE.getValue()) == SKDCConstants.INACTIVE_MESSAGE) {
            return;
        }
        // format it and save it to db -> asrs.WRXTOHOST
        responseMessage.format();
        addToDataQueue(originalSequenceNumber, new HostOutDelegate(responseMessage));
    }

    /**
     * Send a retrieval order list response message to host
     * 
     * @param responseMessage RetrievalItemResponseMessage to send
     * @throws DBException If anything goes wrong
     */
    public void sendRetrievalOrderListResponseToHost(RetrievalItemResponseMessage responseMessage)
            throws DBException {

        if (!mzHasHostSystem
                || Application.getInt(MessageOutNames.ITEMS_ORDER_COMPLETE.getValue()) == SKDCConstants.INACTIVE_MESSAGE) {
            return;
        }
        // format it and save it to db -> asrs.WRXTOHOST
        responseMessage.format();
        addToDataQueue(new HostOutDelegate(responseMessage));
    }
    
    public void sendInventoryResponseToHost(InventoryResponseMessage responseMessage)
            throws DBException {

        if (!mzHasHostSystem
                || Application.getInt(MessageOutNames.INVENTORY_REQUEST_BY_FLIGHT.getValue()) == SKDCConstants.INACTIVE_MESSAGE) {
            return;
        }
        // format it and save it to db -> asrs.WRXTOHOST
        responseMessage.format();
        addToDataQueue(new HostOutDelegate(responseMessage));
    }

    /**
     * Send a flight data update ack message to host
     * @param originalSequenceNumber  The sequence number of the request message sent by Host to WRX
     * @param responseMessage Flight data update ack message
     * 
     * @throws DBException if anything goes wrong
     */
    public void sendFlightDataUpdateAckResponseToHost(int originalSequenceNumber, FlightDataUpdateResponseMessage responseMessage)
            throws DBException {
        /*
         * If the host system is not enabled or this outbound message isn't active, don't try to send any messages.
         */
        if (!mzHasHostSystem || Application
                .getInt(MessageOutNames.FLIGHT_DATA_UPDATE.getValue()) == SKDCConstants.INACTIVE_MESSAGE) {
            return;
        }
        // format it and save it to db -> asrs.WRXTOHOST
        responseMessage.format();
        addToDataQueue(originalSequenceNumber, new HostOutDelegate(responseMessage));
    }
}
