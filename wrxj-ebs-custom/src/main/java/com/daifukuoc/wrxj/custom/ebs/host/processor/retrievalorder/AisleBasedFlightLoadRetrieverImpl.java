package com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalorder;

import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeader;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSInventoryServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLoadServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSOrderServer;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSTableJoin;
import com.daifukuoc.wrxj.custom.ebs.host.messages.RetrievalOrderMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalorder.exception.RetrievalOrderFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.util.ConversionUtil;
import com.daifukuoc.wrxj.custom.ebs.host.util.EBSHostMessageConstants;

/**
 * Aisle based flight load retriever implementation which tries to maximize the retrieval across aisles(device) in
 * parallel
 * 
 * @author LK
 *
 */
public class AisleBasedFlightLoadRetrieverImpl implements LoadRetriever {

    private EBSTableJoin tableJoin = Factory.create(EBSTableJoin.class);
    private EBSLoadServer loadServer = Factory.create(EBSLoadServer.class);
    private StandardStationServer stationServer = Factory.create(StandardStationServer.class);
    private EBSOrderServer orderServer = Factory.create(EBSOrderServer.class);

    protected Logger logger = Logger.getLogger();

    /**
     * Retrieve the N or all bags for flight on the specified flight scheduled date times
     * 
     * @param retrievalOrderMessageData The received retrieval order message
     * @return The number of retrieved bag, should be >= 0
     * @throws RetrievalOrderFailureException If anything goes wrong
     */
    @Override
    public short retrieve(RetrievalOrderMessageData retrievalOrderMessageData) throws RetrievalOrderFailureException {

        // Validate RetrievalOrderMessageData
        if (retrievalOrderMessageData == null) {
            throw new RetrievalOrderFailureException("Retrieval order message data shouldn't be null ");
        }
        if (!retrievalOrderMessageData.isValid()) {
            throw new RetrievalOrderFailureException("Retrieval order message data is not valid ");
        }

        // Check if the order id already is found in orderheader table
        isOrderIdAlreadyProcessed(retrievalOrderMessageData.getOrderId());

        // Populate the list of loadlineitem to retrieve
        List<LoadLineItemData> loadLineItems = populateLoadLineItemDataToRetrieve(retrievalOrderMessageData);

        // Now let's create records in orderheader/orderline tables
        short numberOfBagsRetrieved = 0;
        if (loadLineItems != null && !loadLineItems.isEmpty()) {
            try {
                // As this retrieval is per flight and flight scheduled datetime, final sort location of all loads
                // should be same. So, getting final sort location id from the first entry in the list would be ok.
                processRetrievalOrder(retrievalOrderMessageData,
                        getFinalSortLocationOfLoad(loadLineItems.get(0).getLoadID()), loadLineItems);
            } catch (DBException e) {
                throw new RetrievalOrderFailureException(
                        "Failed to save records in orderheader/orderline tables for retrieval order processing");
            }
            numberOfBagsRetrieved = (short) (loadLineItems.size());
        }

        return numberOfBagsRetrieved;
    }

    /**
     * Check if the given order id is already in orderheader table
     * 
     * @param orderId The order id in the request message
     * @throws RetrievalOrderFailureException If the order is already in orderheader table
     */
    private void isOrderIdAlreadyProcessed(String orderId) throws RetrievalOrderFailureException {
        try {
            if (orderServer.isAlreadyProcessed(orderId)) {
                throw new RetrievalOrderFailureException(
                        "The retrieval order's order id already exists in orderheader table");
            }
        } catch (DBException | RetrievalOrderFailureException e2) {
            throw new RetrievalOrderFailureException("Failed to check if the order id already exists in DB");
        }
    }

    /**
     * Populate the list of loadlineitem for retrieval order processing
     * 
     * @param retrievalOrderMessageData The received message
     * @return The list of loadlineitem for retrieval order processing
     * @throws RetrievalOrderFailureException If anything goes wrong
     */
    private List<LoadLineItemData> populateLoadLineItemDataToRetrieve(
            RetrievalOrderMessageData retrievalOrderMessageData) throws RetrievalOrderFailureException {

        short numberOfBagsToRetrieve = Short.parseShort(retrievalOrderMessageData.getNumberOfBags());

        try {
            // FIXME: How do we determine warehouse from retrieval order message?
            return tableJoin.getAllRetrievableLoadLineItemsForScheduledFlight(
                    EBSHostMessageConstants.WAREHOUSE_NAME, retrievalOrderMessageData.getLot(),
                    ConversionUtil.convertDateStringToDate(retrievalOrderMessageData.getFlightScheduledDateTime()),
                    null, numberOfBagsToRetrieve);
        } catch (DateTimeParseException | DBException e1) {
            throw new RetrievalOrderFailureException(
                    "Failed to populate a list of retrievable loadlineitem for flight and flight scheduled datetime");
        }
    }

    /**
     * Create new records in orderheader/orderline tables for the loadlineitems to retrieve
     * 
     * @param retrievalOrderMessageData The received message
     * @param finalSortLocation The final sort location
     * @param loadLineItems the loadlineitems to retrieve
     * @return The number of persisted record
     * @throws DBException If anything goes wrong
     */
    private int processRetrievalOrder(RetrievalOrderMessageData retrievalOrderMessageData, String finalSortLocation,
            List<LoadLineItemData> loadLineItems) throws DBException {
    	Date flightScheduledDateTime = ConversionUtil
                .convertDateStringToDate(retrievalOrderMessageData.getFlightScheduledDateTime());
    	
    	  OrderHeaderData orderHeaderData = prepareOrderHeaderDataToPersist(retrievalOrderMessageData.getOrderId(), flightScheduledDateTime,
                  null, finalSortLocation);
    	 
    	ArrayList<OrderLineData>  orderLineDataList = new ArrayList<OrderLineData>();
        short savedRecord = 0;
        for (LoadLineItemData loadLineItemData : loadLineItems) {
       
            LoadData loadData = loadServer.getLoad(loadLineItemData.getLoadID());
            OrderLineData orderLineData = prepareOrderLineDataToPersist(retrievalOrderMessageData.getOrderId(), loadLineItemData, loadData);
            orderLineDataList.add(orderLineData);
            
            updateLoadMoveStatus(loadData);
           
            loadLineItemData.setOrderID(retrievalOrderMessageData.getOrderId());
            loadServer.updatLoadLineItem(loadLineItemData);

            savedRecord++;
        }
        if( orderLineDataList.size() > 0 )
        {
        	orderServer.buildOrder(orderHeaderData, orderLineDataList.toArray( new OrderLineData[orderLineDataList.size()] ));
        }

        return savedRecord;
    }

    /**
     * Prepare orderheaderdata object to persist
     * 
     * @param generatedOrderId The generated order id
     * @param flightScheduledDateTime Flight scheduled datetime
     * @param outputStation The output station name
     * @param finalSortLocation The final sort location
     * @return The generated OrderHeaderData
     */
    private OrderHeaderData prepareOrderHeaderDataToPersist(String generatedOrderId, Date flightScheduledDateTime,
            String outputStation, String finalSortLocation) {
        OrderHeaderData orderHeaderData = Factory.create(OrderHeaderData.class);
        orderHeaderData.setOrderID(generatedOrderId);
        orderHeaderData.setScheduledDate(flightScheduledDateTime);
        //orderHeaderData.setOrderType(DBConstants.ITEMORDER);
        orderHeaderData.setOrderType(DBConstants.FULLLOADOUT); //To identify orders from SAC
        orderHeaderData.setDestinationStation(outputStation);
        orderHeaderData.setOrderStatus(DBConstants.READY);
        orderHeaderData.setDestAddress(finalSortLocation);

        return orderHeaderData;
    }

    /**
     * Prepare orderlinedata object to persist
     * 
     * @param generatedOrderId The generated order id
     * @param loadLineItem The LoadLineItemData to retrieve
     * @param loadData The LoadData to retrieve
     * @return The generated OrderLineData
     */
    private OrderLineData prepareOrderLineDataToPersist(String generatedOrderId, LoadLineItemData loadLineItem,
            LoadData loadData) {

        OrderLineData orderLineData = Factory.create(OrderLineData.class);
        orderLineData.setOrderID(generatedOrderId);
        orderLineData.setItem(loadLineItem.getItem());
        orderLineData.setOrderLot(loadLineItem.getLot());
        orderLineData.setLineID(loadLineItem.getLineID());
        orderLineData.setOrderQuantity(loadLineItem.getCurrentQuantity());
        orderLineData.setLoadID(loadLineItem.getLoadID());
        orderLineData.setContainerType(loadData.getContainerType());
        orderLineData.setWarehouse(loadData.getWarehouse());

        return orderLineData;
    }

    /**
     * Change the load move status to retrieve pending
     * 
     * @param loadData
     */
    private void updateLoadMoveStatus(LoadData loadData) {
        loadData.setLoadMoveStatus(DBConstants.RETRIEVEPENDING);
        loadServer.updateLoadData(loadData, false);
    }

    /**
     * Get final sort location of the given load id
     * 
     * @param loadID The load id
     * @return The final sort location
     */
    private String getFinalSortLocationOfLoad(String loadID) {

        LoadData loadData = loadServer.getLoad(loadID);
        return loadData.getFinalSortLocationID();
    }

    /**
     * Get output station number of the given device id
     * 
     * @param deviceId The device id
     * @return The output station number
     */
    private String getOutputStationOfDevice(String deviceId) {

        try {
            Optional<String> outputStationName = stationServer.getStationByDeviceList(deviceId).stream()
                    .map(mapData -> {
                        StationData stationData = Factory.create(StationData.class);
                        stationData.dataToSKDCData(mapData);
                        return stationData;
                    }).filter(stationData -> stationData.getStationType() == DBConstants.OUTPUT)
                    .map(stationData -> stationData.getStationName()).findAny();

            if (outputStationName.isPresent()) {
                return outputStationName.get();
            }
        } catch (DBException e) {
            logger.logException(e);
        }

        return null;
    }
}
