package com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalorder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.dbadapter.data.StationEnum;
import com.daifukuamerica.wrxj.dbadapter.data.TableJoin;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLoadServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSOrderServer;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSTableJoin;
import com.daifukuoc.wrxj.custom.ebs.host.messages.RetrievalOrderMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalorder.exception.RetrievalOrderFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.util.ConversionUtil;
import com.daifukuoc.wrxj.custom.ebs.host.util.EBSHostMessageConstants;

@ExtendWith(MockitoExtension.class)
class AisleBasedFlightLoadRetrieverImplTest {

    private static final short ONE_ITEM = (short) 1;
    private static final int RETRIEVABLE_BAGS_AT_DEVICE2 = 4;
    private static final int RETRIEVABLE_BAGS_AT_DEVICE1 = 5;
    private static final String ORDER_ID = "100";
    private static final String FLIGHT_NUMBER = "FL100";
    private static final String FLIGHT_SCHEDULED_DATETIME_STRING = "20221201001122";
    private static final String WAREHOUSE = "EBS";
    private static final String FINAL_SORT_LOCATION = "9999";
    private static final Date FLIGHT_SCHEDULED_DATETIME_DATE = ConversionUtil
            .convertDateStringToDate(FLIGHT_SCHEDULED_DATETIME_STRING);
    private static final String NUMBER_OF_BAGS_RETRIEVE_ALL = "0";
    private static final String NUMBER_OF_BAGS_RETRIEVE_PARTIAL = "2";
    private static final short NO_BAGS_RETRIEVED = 0;
    private static final int ONE_ITEM_ONLY = 1;
    private static final int TWO_ITEMS = 2;
    private static final String LOAD1_GENERATED_ORDER_ID = ORDER_ID + "-1";
    private static final String LOAD1_LOADID = "1000";
    private static final String LOAD1_DEVICEID = "DEVICE1";
    private static final String LOAD1_OUTPUT_STATION_NAME = "1234";
    private static final String LOAD1_ITEM_NAME = "ItemName1";
    private static final String LOAD1_BAG_ID = "BagId1";
    private static final double LOAD1_CURRENT_QUANTITY = 1.0d;
    private static final String LOAD1_CONTAINER_TYPE = "ContainerType1";

    private static final String LOAD2_GENERATED_ORDER_ID = ORDER_ID + "-2";
    private static final String LOAD2_LOADID = "2000";
    private static final String LOAD2_DEVICEID = "DEVICE2";
    private static final String LOAD2_OUTPUT_STATION_NAME = "5678";
    private static final String LOAD2_ITEM_NAME = "ItemName2";
    private static final String LOAD2_BAG_ID = "BagId2";
    private static final double LOAD2_CURRENT_QUANTITY = 2.0d;
    private static final String LOAD2_CONTAINER_TYPE = "ContainerType2";

    MockedStatic<Factory> mockedFactory;
    MockedStatic<DBTrans> mockedDBTrans;

    @Mock
    EBSTableJoin tableJoin;

    @Mock
    EBSLoadServer loadServer;

    @Mock
    StandardStationServer stationServer;
    
    @Mock
    StandardInventoryServer inventoryServer;

    @Mock
    EBSOrderServer orderServer;

    @Mock
    RetrievalOrderMessageData retrievalOrderMessageData;

    @Mock
    LoadLineItemData loadLineItemData1;

    @Mock
    LoadLineItemData loadLineItemData2;

    @Mock
    LoadData loadData1;

    @Mock
    LoadData loadData2;

    List<LoadLineItemData> noLoadLineItem;

    List<LoadLineItemData> allLoadLineItems;

    List<LoadLineItemData> loadLineItemOfDevice1;

    List<LoadLineItemData> loadLineItemOfDevice2;

    Map<String, Integer> retrievableLoadLineItemsPerDevice;

    StationData stationData = new StationData();

    @Mock
    OrderHeaderData orderHeaderData;

    @Mock
    OrderLineData orderLineData;

    LoadRetriever retriever;

    @BeforeEach
    void setUp() throws Exception {
        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(() -> Factory.create(EBSTableJoin.class)).thenReturn(tableJoin);
        mockedFactory.when(() -> Factory.create(EBSLoadServer.class)).thenReturn(loadServer);
        mockedFactory.when(() -> Factory.create(StandardStationServer.class)).thenReturn(stationServer);
        mockedFactory.when(() -> Factory.create(StandardInventoryServer.class)).thenReturn(inventoryServer);
        mockedFactory.when(() -> Factory.create(EBSOrderServer.class)).thenReturn(orderServer);
        mockedFactory.when(() -> Factory.create(StationData.class)).thenReturn(stationData);
        mockedFactory.when(() -> Factory.create(OrderHeaderData.class)).thenReturn(orderHeaderData);
        mockedFactory.when(() -> Factory.create(OrderLineData.class)).thenReturn(orderLineData);

        mockedDBTrans = Mockito.mockStatic(DBTrans.class);
        mockedDBTrans.when(() -> DBTrans.getStringValue(eq(StationEnum.STATIONTYPE.getName()), eq(DBConstants.OUTPUT)))
                .thenReturn(StationEnum.STATIONTYPE.getName());

        noLoadLineItem = Collections.emptyList();

        allLoadLineItems = Arrays.asList(loadLineItemData1, loadLineItemData2);

        loadLineItemOfDevice1 = Arrays.asList(loadLineItemData1,loadLineItemData2);

        loadLineItemOfDevice2 = Arrays.asList(loadLineItemData2);

        retriever = new AisleBasedFlightLoadRetrieverImpl();
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedFactory.close();
        mockedDBTrans.close();
    }

    @Test
    void shouldThrowAnExceptionIfRetrievalOrderMessageDataIsNull() {
        assertThrows(RetrievalOrderFailureException.class, () -> {
            retriever.retrieve(null);
        });
    }

    @Test
    void shouldThrowAnExceptionIfRetrievalOrderMessageDataIsInvalid() {
        when(retrievalOrderMessageData.isValid()).thenReturn(false);

        assertThrows(RetrievalOrderFailureException.class, () -> {
            retriever.retrieve(retrievalOrderMessageData);
        });

        verify(retrievalOrderMessageData, times(1)).isValid();
    }

    @Test
    void shouldThrowAnExceptionIfCheckingIfTheOrderIdWasAlreadyProcessedIsFailed() throws DBException {
        when(retrievalOrderMessageData.isValid()).thenReturn(true);
        when(retrievalOrderMessageData.getOrderId()).thenReturn(ORDER_ID);

        when(orderServer.isAlreadyProcessed(ORDER_ID)).thenThrow(new DBException());

        assertThrows(RetrievalOrderFailureException.class, () -> {
            retriever.retrieve(retrievalOrderMessageData);
        });

        verify(retrievalOrderMessageData, times(1)).isValid();
        verify(orderServer, times(1)).isAlreadyProcessed(ORDER_ID);
    }

    @Test
    void shouldThrowAnExceptionIfTheOrderIdWasAlreadyProcessed() throws DBException {
        when(retrievalOrderMessageData.isValid()).thenReturn(true);
        when(retrievalOrderMessageData.getOrderId()).thenReturn(ORDER_ID);

        when(orderServer.isAlreadyProcessed(ORDER_ID)).thenReturn(true);

        assertThrows(RetrievalOrderFailureException.class, () -> {
            retriever.retrieve(retrievalOrderMessageData);
        });

        verify(retrievalOrderMessageData, times(1)).isValid();
        verify(orderServer, times(1)).isAlreadyProcessed(ORDER_ID);
    }

    @Test
    void shouldThrowAnExceptionIfFailedToGetAllRetrievableLoadLineItemsWhenRetrievingAllBags() throws DBException {
        when(retrievalOrderMessageData.isValid()).thenReturn(true);
        when(retrievalOrderMessageData.getOrderId()).thenReturn(ORDER_ID);
        when(retrievalOrderMessageData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(retrievalOrderMessageData.getFlightScheduledDateTime()).thenReturn(FLIGHT_SCHEDULED_DATETIME_STRING);
        when(retrievalOrderMessageData.getNumberOfBags()).thenReturn(NUMBER_OF_BAGS_RETRIEVE_ALL);

        when(orderServer.isAlreadyProcessed(ORDER_ID)).thenReturn(false);

        when(tableJoin.getAllRetrievableLoadLineItemsForScheduledFlight(eq(EBSHostMessageConstants.WAREHOUSE_NAME), eq(FLIGHT_NUMBER),
                eq(FLIGHT_SCHEDULED_DATETIME_DATE), isNull(), eq(Short.valueOf(NUMBER_OF_BAGS_RETRIEVE_ALL)))).thenThrow(new DBException());

        assertThrows(RetrievalOrderFailureException.class, () -> {
            retriever.retrieve(retrievalOrderMessageData);
        });

        verify(retrievalOrderMessageData, times(1)).isValid();
        verify(orderServer, times(1)).isAlreadyProcessed(ORDER_ID);
        verify(tableJoin, times(1)).getAllRetrievableLoadLineItemsForScheduledFlight(eq(EBSHostMessageConstants.WAREHOUSE_NAME), eq(FLIGHT_NUMBER),
                eq(FLIGHT_SCHEDULED_DATETIME_DATE), isNull(), eq(Short.valueOf(NUMBER_OF_BAGS_RETRIEVE_ALL)));
    }
    @Test
    void shouldReturnZeroIfNoRetrievableLoadLineItemIsFoundWhenRetrievingAllBags()
            throws DBException, RetrievalOrderFailureException {
        when(retrievalOrderMessageData.isValid()).thenReturn(true);
        when(retrievalOrderMessageData.getOrderId()).thenReturn(ORDER_ID);
        when(retrievalOrderMessageData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(retrievalOrderMessageData.getFlightScheduledDateTime()).thenReturn(FLIGHT_SCHEDULED_DATETIME_STRING);
        when(retrievalOrderMessageData.getNumberOfBags()).thenReturn(NUMBER_OF_BAGS_RETRIEVE_ALL);

        when(orderServer.isAlreadyProcessed(ORDER_ID)).thenReturn(false);

        when(tableJoin.getAllRetrievableLoadLineItemsForScheduledFlight(eq(EBSHostMessageConstants.WAREHOUSE_NAME), eq(FLIGHT_NUMBER),
                eq(FLIGHT_SCHEDULED_DATETIME_DATE), isNull(), eq(Short.valueOf(NUMBER_OF_BAGS_RETRIEVE_ALL)))).thenReturn(noLoadLineItem);

        assertEquals(NO_BAGS_RETRIEVED, retriever.retrieve(retrievalOrderMessageData));

        verify(retrievalOrderMessageData, times(1)).isValid();
        verify(orderServer, times(1)).isAlreadyProcessed(ORDER_ID);
        verify(tableJoin, times(1)).getAllRetrievableLoadLineItemsForScheduledFlight(eq(EBSHostMessageConstants.WAREHOUSE_NAME), eq(FLIGHT_NUMBER),
                eq(FLIGHT_SCHEDULED_DATETIME_DATE), isNull(), eq(Short.valueOf(NUMBER_OF_BAGS_RETRIEVE_ALL)));
    }

    @Test
    void shouldReturnTheNumberOfRetrievedLoadLineItemsIfAnyRetrievableLoadLineItemIsFoundWhenRetrievingAllBags()
            throws DBException, RetrievalOrderFailureException {
        when(retrievalOrderMessageData.isValid()).thenReturn(true);
        when(retrievalOrderMessageData.getOrderId()).thenReturn(ORDER_ID);
        when(retrievalOrderMessageData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(retrievalOrderMessageData.getFlightScheduledDateTime()).thenReturn(FLIGHT_SCHEDULED_DATETIME_STRING);
        when(retrievalOrderMessageData.getNumberOfBags()).thenReturn(NUMBER_OF_BAGS_RETRIEVE_ALL);

        when(orderServer.isAlreadyProcessed(ORDER_ID)).thenReturn(false);

        when(tableJoin.getAllRetrievableLoadLineItemsForScheduledFlight(eq(EBSHostMessageConstants.WAREHOUSE_NAME), eq(FLIGHT_NUMBER),
                eq(FLIGHT_SCHEDULED_DATETIME_DATE), isNull(), eq(Short.valueOf(NUMBER_OF_BAGS_RETRIEVE_ALL)))).thenReturn(allLoadLineItems);

        when(loadLineItemData1.getLoadID()).thenReturn(LOAD1_LOADID);
        when(loadLineItemData1.getItem()).thenReturn(LOAD1_ITEM_NAME);
        when(loadLineItemData1.getLot()).thenReturn(FLIGHT_NUMBER);
        when(loadLineItemData1.getLineID()).thenReturn(LOAD1_BAG_ID);
        when(loadLineItemData1.getCurrentQuantity()).thenReturn(LOAD1_CURRENT_QUANTITY);

        when(loadData1.getContainerType()).thenReturn(LOAD1_CONTAINER_TYPE);
        when(loadData1.getWarehouse()).thenReturn(WAREHOUSE);
        when(loadData1.getFinalSortLocationID()).thenReturn(FINAL_SORT_LOCATION);

        when(loadLineItemData2.getLoadID()).thenReturn(LOAD2_LOADID);
        when(loadLineItemData2.getItem()).thenReturn(LOAD2_ITEM_NAME);
        when(loadLineItemData2.getLot()).thenReturn(FLIGHT_NUMBER);
        when(loadLineItemData2.getLineID()).thenReturn(LOAD2_BAG_ID);
        when(loadLineItemData2.getCurrentQuantity()).thenReturn(LOAD2_CURRENT_QUANTITY);

        when(loadData2.getContainerType()).thenReturn(LOAD2_CONTAINER_TYPE);
        when(loadData2.getWarehouse()).thenReturn(WAREHOUSE);

        when(loadServer.getLoad(LOAD1_LOADID)).thenReturn(loadData1);
        when(loadServer.getLoad(LOAD2_LOADID)).thenReturn(loadData2);

        doNothing().when(loadServer).updateLoadData(loadData1, false);
        doNothing().when(loadServer).updateLoadData(loadData2, false);
        
        assertEquals(allLoadLineItems.size(), retriever.retrieve(retrievalOrderMessageData));

        verify(retrievalOrderMessageData, times(1)).isValid();
        verify(orderServer, times(1)).isAlreadyProcessed(ORDER_ID);
        verify(tableJoin, times(1)).getAllRetrievableLoadLineItemsForScheduledFlight(eq(EBSHostMessageConstants.WAREHOUSE_NAME), eq(FLIGHT_NUMBER),
                eq(FLIGHT_SCHEDULED_DATETIME_DATE), isNull(), eq(Short.valueOf(NUMBER_OF_BAGS_RETRIEVE_ALL)));

        verify(orderHeaderData, times(1)).setScheduledDate(eq(FLIGHT_SCHEDULED_DATETIME_DATE));
        verify(orderHeaderData, times(1)).setOrderType(eq(DBConstants.FULLLOADOUT));
        verify(orderHeaderData, times(1)).setOrderStatus(eq(DBConstants.READY));
        verify(orderHeaderData, times(1)).setDestAddress(eq(FINAL_SORT_LOCATION));

        verify(orderHeaderData, times(1)).setOrderID(eq(ORDER_ID));
       
        verify(orderLineData, times(2)).setOrderLot(eq(FLIGHT_NUMBER));
        verify(orderLineData, times(2)).setWarehouse(eq(WAREHOUSE));

        verify(orderLineData, times(2)).setOrderID(eq(ORDER_ID));
        verify(orderLineData, times(1)).setItem(eq(LOAD1_ITEM_NAME));
        verify(orderLineData, times(1)).setLineID(eq(LOAD1_BAG_ID));
        verify(orderLineData, times(1)).setOrderQuantity(eq(LOAD1_CURRENT_QUANTITY));
        verify(orderLineData, times(1)).setLoadID(eq(LOAD1_LOADID));
        verify(orderLineData, times(1)).setContainerType(eq(LOAD1_CONTAINER_TYPE));

        verify(orderLineData, times(2)).setOrderID(eq(ORDER_ID));
        verify(orderLineData, times(1)).setItem(eq(LOAD2_ITEM_NAME));
        verify(orderLineData, times(1)).setLineID(eq(LOAD2_BAG_ID));
        verify(orderLineData, times(1)).setOrderQuantity(eq(LOAD2_CURRENT_QUANTITY));
        verify(orderLineData, times(1)).setLoadID(eq(LOAD2_LOADID));
        verify(orderLineData, times(1)).setContainerType(eq(LOAD2_CONTAINER_TYPE));

        ArgumentCaptor<OrderHeaderData> orderHeaderDataCaptor = ArgumentCaptor.forClass(OrderHeaderData.class);
        ArgumentCaptor<OrderLineData[]> orderLineDataCaptor = ArgumentCaptor.forClass(OrderLineData[].class);
        verify(orderServer, times(1)).buildOrder(orderHeaderDataCaptor.capture(), orderLineDataCaptor.capture());
        assertEquals(orderHeaderData, orderHeaderDataCaptor.getValue());
        assertEquals(TWO_ITEMS, orderLineDataCaptor.getValue().length);
        assertEquals(orderLineData, orderLineDataCaptor.getValue()[0]);

        verify(loadServer, times(1)).updateLoadData(loadData1, false);
        verify(loadData1, times(1)).setLoadMoveStatus(DBConstants.RETRIEVEPENDING);
        verify(loadServer, times(1)).updateLoadData(loadData2, false);
        verify(loadData2, times(1)).setLoadMoveStatus(DBConstants.RETRIEVEPENDING);
    }
 
    @Test
    void shouldReturnTheNumberOfRetrievedLoadLineItemsIfAnyRetrievableLoadLineItemIsFoundWhenRetrievingOnlyOneBag()
            throws DBException, RetrievalOrderFailureException {
        when(retrievalOrderMessageData.isValid()).thenReturn(true);
        when(retrievalOrderMessageData.getOrderId()).thenReturn(ORDER_ID);
        when(retrievalOrderMessageData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(retrievalOrderMessageData.getFlightScheduledDateTime()).thenReturn(FLIGHT_SCHEDULED_DATETIME_STRING);
        when(retrievalOrderMessageData.getNumberOfBags()).thenReturn(NUMBER_OF_BAGS_RETRIEVE_PARTIAL);

        when(orderServer.isAlreadyProcessed(ORDER_ID)).thenReturn(false);

        retrievableLoadLineItemsPerDevice = new LinkedHashMap<>();
        retrievableLoadLineItemsPerDevice.put(LOAD1_DEVICEID, RETRIEVABLE_BAGS_AT_DEVICE1);
        retrievableLoadLineItemsPerDevice.put(LOAD2_DEVICEID, RETRIEVABLE_BAGS_AT_DEVICE2);

        when(tableJoin.getAllRetrievableLoadLineItemsForScheduledFlight(EBSHostMessageConstants.WAREHOUSE_NAME, FLIGHT_NUMBER,
                FLIGHT_SCHEDULED_DATETIME_DATE, null, Short.valueOf(NUMBER_OF_BAGS_RETRIEVE_PARTIAL)))
                        .thenReturn(loadLineItemOfDevice1);

        when(loadLineItemData1.getLoadID()).thenReturn(LOAD1_LOADID);
        when(loadLineItemData1.getItem()).thenReturn(LOAD1_ITEM_NAME);
        when(loadLineItemData1.getLot()).thenReturn(FLIGHT_NUMBER);
        when(loadLineItemData1.getLineID()).thenReturn(LOAD1_BAG_ID);
        when(loadLineItemData1.getCurrentQuantity()).thenReturn(LOAD1_CURRENT_QUANTITY);

        //when(loadData1.getDeviceID()).thenReturn(LOAD1_DEVICEID);
        when(loadData1.getContainerType()).thenReturn(LOAD1_CONTAINER_TYPE);
        when(loadData1.getWarehouse()).thenReturn(WAREHOUSE);
        when(loadData1.getFinalSortLocationID()).thenReturn(FINAL_SORT_LOCATION);

        when(loadLineItemData2.getLoadID()).thenReturn(LOAD2_LOADID);
        when(loadLineItemData2.getItem()).thenReturn(LOAD2_ITEM_NAME);
        when(loadLineItemData2.getLot()).thenReturn(FLIGHT_NUMBER);
        when(loadLineItemData2.getLineID()).thenReturn(LOAD2_BAG_ID);
        when(loadLineItemData2.getCurrentQuantity()).thenReturn(LOAD2_CURRENT_QUANTITY);

        when(loadData2.getContainerType()).thenReturn(LOAD2_CONTAINER_TYPE);
        when(loadData2.getWarehouse()).thenReturn(WAREHOUSE);

        when(loadServer.getLoad(LOAD1_LOADID)).thenReturn(loadData1);
        when(loadServer.getLoad(LOAD2_LOADID)).thenReturn(loadData2);

        doNothing().when(loadServer).updateLoadData(loadData1, false);
        doNothing().when(loadServer).updateLoadData(loadData2, false);

        assertEquals(loadLineItemOfDevice1.size(),
                retriever.retrieve(retrievalOrderMessageData));

        verify(retrievalOrderMessageData, times(1)).isValid();
        verify(orderServer, times(1)).isAlreadyProcessed(ORDER_ID);

        verify(orderHeaderData, times(1)).setScheduledDate(eq(FLIGHT_SCHEDULED_DATETIME_DATE));
        verify(orderHeaderData, times(1)).setOrderType(eq(DBConstants.FULLLOADOUT));
        verify(orderHeaderData, times(1)).setOrderStatus(eq(DBConstants.READY));
        verify(orderHeaderData, times(1)).setDestAddress(eq(FINAL_SORT_LOCATION));

        verify(orderHeaderData, times(1)).setOrderID(eq(ORDER_ID));
        

        verify(orderLineData, times(2)).setOrderLot(eq(FLIGHT_NUMBER));
        verify(orderLineData, times(2)).setWarehouse(eq(WAREHOUSE));

        verify(orderLineData, times(2)).setOrderID(eq(ORDER_ID));
        verify(orderLineData, times(1)).setItem(eq(LOAD1_ITEM_NAME));
        verify(orderLineData, times(1)).setLineID(eq(LOAD1_BAG_ID));
        verify(orderLineData, times(1)).setOrderQuantity(eq(LOAD1_CURRENT_QUANTITY));
        verify(orderLineData, times(1)).setLoadID(eq(LOAD1_LOADID));
        verify(orderLineData, times(1)).setContainerType(eq(LOAD1_CONTAINER_TYPE));

        verify(orderLineData, times(2)).setOrderID(eq(ORDER_ID));
        verify(orderLineData, times(1)).setItem(eq(LOAD2_ITEM_NAME));
        verify(orderLineData, times(1)).setLineID(eq(LOAD2_BAG_ID));
        verify(orderLineData, times(1)).setOrderQuantity(eq(LOAD2_CURRENT_QUANTITY));
        verify(orderLineData, times(1)).setLoadID(eq(LOAD2_LOADID));
        verify(orderLineData, times(1)).setContainerType(eq(LOAD2_CONTAINER_TYPE));

        ArgumentCaptor<OrderHeaderData> orderHeaderDataCaptor = ArgumentCaptor.forClass(OrderHeaderData.class);
        ArgumentCaptor<OrderLineData[]> orderLineDataCaptor = ArgumentCaptor.forClass(OrderLineData[].class);
        verify(orderServer, times(1)).buildOrder(orderHeaderDataCaptor.capture(), orderLineDataCaptor.capture());
        assertEquals(orderHeaderData, orderHeaderDataCaptor.getValue());
        assertEquals(TWO_ITEMS, orderLineDataCaptor.getValue().length);
        assertEquals(orderLineData, orderLineDataCaptor.getValue()[0]);

        verify(loadServer, times(1)).updateLoadData(loadData1, false);
        verify(loadData1, times(1)).setLoadMoveStatus(DBConstants.RETRIEVEPENDING);
        verify(loadServer, times(1)).updateLoadData(loadData2, false);
        verify(loadData2, times(1)).setLoadMoveStatus(DBConstants.RETRIEVEPENDING);
    }
   
}
