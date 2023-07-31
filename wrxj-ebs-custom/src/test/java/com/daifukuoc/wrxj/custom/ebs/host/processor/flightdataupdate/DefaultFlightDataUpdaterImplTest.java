package com.daifukuoc.wrxj.custom.ebs.host.processor.flightdataupdate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSInventoryServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLoadServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSPoReceivingServer;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSPurchaseOrderLineData;
import com.daifukuoc.wrxj.custom.ebs.host.messages.FlightDataUpdateMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.processor.flightdataupdate.exception.FlightDataUpdateFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.util.ConversionUtil;

@ExtendWith(MockitoExtension.class)
class DefaultFlightDataUpdaterImplTest {

    private static final String FLIGHT_NUMBER = "FL100";
    private static final String VALID_FLIGHT_SCHEDULED_DATETIME = "20221201001122";
    private static final String INVALID_FLIGHT_SCHEDULED_DATETIME = "20229901001122";
    private static final String VALID_DEFAULT_RETRIEVAL_DATETIME = "20221201000909";
    private static final String INVALID_DEFAULT_RETRIEVAL_DATETIME = "20229901000909";
    private static final String FINAL_SORT_LOCATION = "1234";
    private static final String ORDER_ID = "10671067";
    private static final String LOAD_ID = "1067";
    private static final String ITEM = "Bag_On_Tray";
    private static final String BAG_ID = "BAG1067";

    MockedStatic<Factory> mockedFactory;

    @Mock
    EBSPoReceivingServer poServer;

    @Mock
    EBSInventoryServer inventoryServer;
    
    @Mock
    EBSLoadServer loadServer;

    @Mock
    FlightDataUpdateMessageData flightDataUpdateMessageData;

    @Mock
    PurchaseOrderLineData purchaseOrderLineData;

    @Mock
    LoadLineItemData loadLineItemData;

    FlightDataUpdater updater;

    @BeforeEach
    void setUp() throws Exception {
        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(() -> Factory.create(EBSPoReceivingServer.class)).thenReturn(poServer);
        mockedFactory.when(() -> Factory.create(EBSInventoryServer.class)).thenReturn(inventoryServer);
        mockedFactory.when(() -> Factory.create(EBSLoadServer.class)).thenReturn(loadServer);        
        mockedFactory.when(() -> Factory.create(EBSPurchaseOrderLineData.class))
                .thenReturn(new EBSPurchaseOrderLineData());
        mockedFactory.when(() -> Factory.create(PurchaseOrderHeaderData.class))
                .thenReturn(new PurchaseOrderHeaderData());
        mockedFactory.when(() -> Factory.create(LoadLineItemData.class)).thenReturn(new LoadLineItemData());
        mockedFactory.when(() -> Factory.create(LoadData.class)).thenReturn(new LoadData());

        updater = new DefaultFlightDataUpdaterImpl();
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedFactory.close();
    }

    @Test
    void shouldThrowAnExceptionIfFlightDataUpdateMessageDataIsNull() {
        assertThrows(FlightDataUpdateFailureException.class, () -> {
            updater.update(null);
        });
    }

    @Test
    void shouldThrowAnExceptionIfFlightDataUpdateMessageDataIsInvalid() {
        when(flightDataUpdateMessageData.isValid()).thenReturn(false);

        assertThrows(FlightDataUpdateFailureException.class, () -> {
            updater.update(flightDataUpdateMessageData);
        });

        verify(flightDataUpdateMessageData, times(1)).isValid();
    }

    @Test
    void shouldThrowAnExceptionIfFlightScheduledDataTimeIsInvalid() {
        when(flightDataUpdateMessageData.isValid()).thenReturn(true);
        when(flightDataUpdateMessageData.getFlightScheduledDateTime()).thenReturn(INVALID_FLIGHT_SCHEDULED_DATETIME);

        assertThrows(FlightDataUpdateFailureException.class, () -> {
            updater.update(flightDataUpdateMessageData);
        });

        verify(flightDataUpdateMessageData, times(1)).isValid();
    }

    @Test
    void shouldThrowAnExceptionIfDefaultRetrievalDataTimeIsInvalid() {
        when(flightDataUpdateMessageData.isValid()).thenReturn(true);
        when(flightDataUpdateMessageData.getFlightScheduledDateTime()).thenReturn(VALID_FLIGHT_SCHEDULED_DATETIME);
        when(flightDataUpdateMessageData.getDefaultRetrievalDateTime()).thenReturn(INVALID_DEFAULT_RETRIEVAL_DATETIME);

        assertThrows(FlightDataUpdateFailureException.class, () -> {
            updater.update(flightDataUpdateMessageData);
        });

        verify(flightDataUpdateMessageData, times(1)).isValid();
    }

    @Test
    void shouldThrowAnExceptionIfRetrievingPurchaseOrderLineDataFailed() throws DBException {
        when(flightDataUpdateMessageData.isValid()).thenReturn(true);
        when(flightDataUpdateMessageData.getFlightScheduledDateTime()).thenReturn(VALID_FLIGHT_SCHEDULED_DATETIME);
        when(flightDataUpdateMessageData.getDefaultRetrievalDateTime()).thenReturn(VALID_DEFAULT_RETRIEVAL_DATETIME);
        when(flightDataUpdateMessageData.getFlightNumber()).thenReturn(FLIGHT_NUMBER);

        when(poServer.getPurchaseOrderLinesByLot(flightDataUpdateMessageData.getFlightNumber()))
                .thenThrow(new DBException());

        assertThrows(FlightDataUpdateFailureException.class, () -> {
            updater.update(flightDataUpdateMessageData);
        });

        verify(flightDataUpdateMessageData, times(1)).isValid();
        verify(poServer, times(1)).getPurchaseOrderLinesByLot(flightDataUpdateMessageData.getFlightNumber());
    }

    @Test
    void shouldThrowAnExceptionIfUpdatingPurchaseOrderLineFailed() throws DBException {
        when(flightDataUpdateMessageData.isValid()).thenReturn(true);
        when(flightDataUpdateMessageData.getFlightScheduledDateTime()).thenReturn(VALID_FLIGHT_SCHEDULED_DATETIME);
        when(flightDataUpdateMessageData.getDefaultRetrievalDateTime()).thenReturn(VALID_DEFAULT_RETRIEVAL_DATETIME);
        when(flightDataUpdateMessageData.getFlightNumber()).thenReturn(FLIGHT_NUMBER);

        when(purchaseOrderLineData.getOrderID()).thenReturn(ORDER_ID);
        when(purchaseOrderLineData.getItem()).thenReturn(ITEM);
        when(purchaseOrderLineData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(purchaseOrderLineData.getLineID()).thenReturn(BAG_ID);
        when(poServer.getPurchaseOrderLinesByLot(flightDataUpdateMessageData.getFlightNumber()))
                .thenReturn(Collections.singletonList(purchaseOrderLineData));
        when(poServer.modifyPOLine(any(PurchaseOrderLineData.class))).thenThrow(new DBException());

        assertThrows(FlightDataUpdateFailureException.class, () -> updater.update(flightDataUpdateMessageData));

        verify(flightDataUpdateMessageData, times(1)).isValid();

        verify(poServer, times(1)).getPurchaseOrderLinesByLot(flightDataUpdateMessageData.getFlightNumber());

        ArgumentCaptor<PurchaseOrderLineData> purchaseOrderLineDataCaptor = ArgumentCaptor
                .forClass(PurchaseOrderLineData.class);
        verify(poServer).modifyPOLine(purchaseOrderLineDataCaptor.capture());
        PurchaseOrderLineData updatedPurchaseOrderLineData = purchaseOrderLineDataCaptor.getValue();
        // Search keys
        assertEquals(4, updatedPurchaseOrderLineData.getKeyArray().length);
        assertTrue(Arrays.stream(updatedPurchaseOrderLineData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(PurchaseOrderLineData.ORDERID_NAME)));
        assertTrue(Arrays.stream(updatedPurchaseOrderLineData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(PurchaseOrderLineData.ITEM_NAME)));
        assertTrue(Arrays.stream(updatedPurchaseOrderLineData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(PurchaseOrderLineData.LOT_NAME)));
        assertTrue(Arrays.stream(updatedPurchaseOrderLineData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(PurchaseOrderLineData.LINEID_NAME)));
        // Updated columns
        assertEquals(ConversionUtil.convertDateStringToDate(VALID_DEFAULT_RETRIEVAL_DATETIME),
                updatedPurchaseOrderLineData.getExpirationDate());
    }

    @Test
    void shouldThrowAnExceptionIfUpdatingPurchaseOrderHeaderFailed() throws DBException {
        when(flightDataUpdateMessageData.isValid()).thenReturn(true);
        when(flightDataUpdateMessageData.getFlightScheduledDateTime()).thenReturn(VALID_FLIGHT_SCHEDULED_DATETIME);
        when(flightDataUpdateMessageData.getDefaultRetrievalDateTime()).thenReturn(VALID_DEFAULT_RETRIEVAL_DATETIME);
        when(flightDataUpdateMessageData.getFlightNumber()).thenReturn(FLIGHT_NUMBER);
        when(flightDataUpdateMessageData.getFinalSortLocation()).thenReturn(FINAL_SORT_LOCATION);

        when(purchaseOrderLineData.getOrderID()).thenReturn(ORDER_ID);
        when(purchaseOrderLineData.getItem()).thenReturn(ITEM);
        when(purchaseOrderLineData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(purchaseOrderLineData.getLineID()).thenReturn(BAG_ID);
        when(poServer.getPurchaseOrderLinesByLot(flightDataUpdateMessageData.getFlightNumber()))
                .thenReturn(Collections.singletonList(purchaseOrderLineData));
        when(poServer.modifyPOHead(any(PurchaseOrderHeaderData.class))).thenThrow(new DBException());

        assertThrows(FlightDataUpdateFailureException.class, () -> updater.update(flightDataUpdateMessageData));

        verify(flightDataUpdateMessageData, times(1)).isValid();

        verify(poServer, times(1)).getPurchaseOrderLinesByLot(flightDataUpdateMessageData.getFlightNumber());

        ArgumentCaptor<PurchaseOrderLineData> purchaseOrderLineDataCaptor = ArgumentCaptor
                .forClass(PurchaseOrderLineData.class);
        verify(poServer).modifyPOLine(purchaseOrderLineDataCaptor.capture());
        PurchaseOrderLineData updatedPurchaseOrderLineData = purchaseOrderLineDataCaptor.getValue();
        // Search keys
        assertEquals(4, updatedPurchaseOrderLineData.getKeyArray().length);
        assertTrue(Arrays.stream(updatedPurchaseOrderLineData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(PurchaseOrderLineData.ORDERID_NAME)));
        assertTrue(Arrays.stream(updatedPurchaseOrderLineData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(PurchaseOrderLineData.ITEM_NAME)));
        assertTrue(Arrays.stream(updatedPurchaseOrderLineData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(PurchaseOrderLineData.LOT_NAME)));
        assertTrue(Arrays.stream(updatedPurchaseOrderLineData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(PurchaseOrderLineData.LINEID_NAME)));
        // Updated columns
        assertEquals(ConversionUtil.convertDateStringToDate(VALID_DEFAULT_RETRIEVAL_DATETIME),
                updatedPurchaseOrderLineData.getExpirationDate());

        ArgumentCaptor<PurchaseOrderHeaderData> purchaseOrderHeaderDataCaptor = ArgumentCaptor
                .forClass(PurchaseOrderHeaderData.class);
        verify(poServer).modifyPOHead(purchaseOrderHeaderDataCaptor.capture());
        PurchaseOrderHeaderData updatedPurchaseOrderHeaderData = purchaseOrderHeaderDataCaptor.getValue();
        // Search keys
        assertEquals(1, updatedPurchaseOrderHeaderData.getKeyArray().length);
        assertTrue(Arrays.stream(updatedPurchaseOrderHeaderData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(PurchaseOrderHeaderData.ORDERID_NAME)));
        // Updated columns
        assertEquals(ConversionUtil.convertDateStringToDate(VALID_FLIGHT_SCHEDULED_DATETIME),
                updatedPurchaseOrderHeaderData.getExpectedDate());
        assertEquals(FINAL_SORT_LOCATION, updatedPurchaseOrderHeaderData.getFinalSortLocationId());
    }

    @Test
    void shouldThrowAnExceptionIfUpdatingLoadLineItemFailed() throws DBException {
        when(flightDataUpdateMessageData.isValid()).thenReturn(true);
        when(flightDataUpdateMessageData.getFlightScheduledDateTime()).thenReturn(VALID_FLIGHT_SCHEDULED_DATETIME);
        when(flightDataUpdateMessageData.getDefaultRetrievalDateTime()).thenReturn(VALID_DEFAULT_RETRIEVAL_DATETIME);
        when(flightDataUpdateMessageData.getFlightNumber()).thenReturn(FLIGHT_NUMBER);
        when(flightDataUpdateMessageData.getFinalSortLocation()).thenReturn(FINAL_SORT_LOCATION);

        when(purchaseOrderLineData.getOrderID()).thenReturn(ORDER_ID);
        when(purchaseOrderLineData.getItem()).thenReturn(ITEM);
        when(purchaseOrderLineData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(purchaseOrderLineData.getLineID()).thenReturn(BAG_ID);
        when(poServer.getPurchaseOrderLinesByLot(flightDataUpdateMessageData.getFlightNumber()))
                .thenReturn(Collections.singletonList(purchaseOrderLineData));

        when(loadLineItemData.getLoadID()).thenReturn(LOAD_ID);
        when(loadLineItemData.getItem()).thenReturn(ITEM);
        when(loadLineItemData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(loadLineItemData.getLineID()).thenReturn(BAG_ID);
        when(loadLineItemData.getOrderID()).thenReturn(ORDER_ID);
        when(inventoryServer.getLoadLineItemDataListByLot(flightDataUpdateMessageData.getFlightNumber()))
                .thenReturn(Collections.singletonList(loadLineItemData));
        when(inventoryServer.modifyLoadLineItem(any(LoadLineItemData.class))).thenThrow(new DBException());
        
        assertThrows(FlightDataUpdateFailureException.class, () -> updater.update(flightDataUpdateMessageData));

        verify(flightDataUpdateMessageData, times(1)).isValid();

        verify(poServer, times(1)).getPurchaseOrderLinesByLot(flightDataUpdateMessageData.getFlightNumber());

        ArgumentCaptor<PurchaseOrderLineData> purchaseOrderLineDataCaptor = ArgumentCaptor
                .forClass(PurchaseOrderLineData.class);
        verify(poServer).modifyPOLine(purchaseOrderLineDataCaptor.capture());
        PurchaseOrderLineData updatedPurchaseOrderLineData = purchaseOrderLineDataCaptor.getValue();
        // Search keys
        assertEquals(4, updatedPurchaseOrderLineData.getKeyArray().length);
        assertTrue(Arrays.stream(updatedPurchaseOrderLineData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(PurchaseOrderLineData.ORDERID_NAME)));
        assertTrue(Arrays.stream(updatedPurchaseOrderLineData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(PurchaseOrderLineData.ITEM_NAME)));
        assertTrue(Arrays.stream(updatedPurchaseOrderLineData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(PurchaseOrderLineData.LOT_NAME)));
        assertTrue(Arrays.stream(updatedPurchaseOrderLineData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(PurchaseOrderLineData.LINEID_NAME)));
        // Updated columns
        assertEquals(ConversionUtil.convertDateStringToDate(VALID_DEFAULT_RETRIEVAL_DATETIME),
                updatedPurchaseOrderLineData.getExpirationDate());

        ArgumentCaptor<PurchaseOrderHeaderData> purchaseOrderHeaderDataCaptor = ArgumentCaptor
                .forClass(PurchaseOrderHeaderData.class);
        verify(poServer).modifyPOHead(purchaseOrderHeaderDataCaptor.capture());
        PurchaseOrderHeaderData updatedPurchaseOrderHeaderData = purchaseOrderHeaderDataCaptor.getValue();
        // Search keys
        assertEquals(1, updatedPurchaseOrderHeaderData.getKeyArray().length);
        assertTrue(Arrays.stream(updatedPurchaseOrderHeaderData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(PurchaseOrderHeaderData.ORDERID_NAME)));
        // Updated columns
        assertEquals(ConversionUtil.convertDateStringToDate(VALID_FLIGHT_SCHEDULED_DATETIME),
                updatedPurchaseOrderHeaderData.getExpectedDate());
        assertEquals(FINAL_SORT_LOCATION, updatedPurchaseOrderHeaderData.getFinalSortLocationId());

        verify(inventoryServer, times(1)).getLoadLineItemDataListByLot(flightDataUpdateMessageData.getFlightNumber());

        ArgumentCaptor<LoadLineItemData> loadLineItemDataCaptor = ArgumentCaptor.forClass(LoadLineItemData.class);
        verify(inventoryServer).modifyLoadLineItem(loadLineItemDataCaptor.capture());
        LoadLineItemData updatedLoadLineItemData = loadLineItemDataCaptor.getValue();
        // Search keys
        assertEquals(5, updatedLoadLineItemData.getKeyArray().length);
        assertTrue(Arrays.stream(updatedLoadLineItemData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(LoadLineItemData.LOADID_NAME)));
        assertTrue(Arrays.stream(updatedLoadLineItemData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(LoadLineItemData.ITEM_NAME)));
        assertTrue(Arrays.stream(updatedLoadLineItemData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(LoadLineItemData.LOT_NAME)));
        assertTrue(Arrays.stream(updatedLoadLineItemData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(LoadLineItemData.LINEID_NAME)));
        assertTrue(Arrays.stream(updatedLoadLineItemData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(LoadLineItemData.ORDERID_NAME)));
        // Updated columns
        assertEquals(ConversionUtil.convertDateStringToDate(VALID_DEFAULT_RETRIEVAL_DATETIME),
                updatedLoadLineItemData.getExpirationDate());
    }
    
    @Test
    void shouldThrowAnExceptionIfUpdatingLoadFailed() throws DBException {
        when(flightDataUpdateMessageData.isValid()).thenReturn(true);
        when(flightDataUpdateMessageData.getFlightScheduledDateTime()).thenReturn(VALID_FLIGHT_SCHEDULED_DATETIME);
        when(flightDataUpdateMessageData.getDefaultRetrievalDateTime()).thenReturn(VALID_DEFAULT_RETRIEVAL_DATETIME);
        when(flightDataUpdateMessageData.getFlightNumber()).thenReturn(FLIGHT_NUMBER);
        when(flightDataUpdateMessageData.getFinalSortLocation()).thenReturn(FINAL_SORT_LOCATION);

        when(purchaseOrderLineData.getOrderID()).thenReturn(ORDER_ID);
        when(purchaseOrderLineData.getItem()).thenReturn(ITEM);
        when(purchaseOrderLineData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(purchaseOrderLineData.getLineID()).thenReturn(BAG_ID);
        when(poServer.getPurchaseOrderLinesByLot(flightDataUpdateMessageData.getFlightNumber()))
                .thenReturn(Collections.singletonList(purchaseOrderLineData));

        when(loadLineItemData.getLoadID()).thenReturn(LOAD_ID);
        when(loadLineItemData.getItem()).thenReturn(ITEM);
        when(loadLineItemData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(loadLineItemData.getLineID()).thenReturn(BAG_ID);
        when(loadLineItemData.getOrderID()).thenReturn(ORDER_ID);
        when(inventoryServer.getLoadLineItemDataListByLot(flightDataUpdateMessageData.getFlightNumber()))
                .thenReturn(Collections.singletonList(loadLineItemData));
        
        doThrow(DBException.class).when(loadServer).modifyLoad(any(LoadData.class));
        
        assertThrows(FlightDataUpdateFailureException.class, () -> updater.update(flightDataUpdateMessageData));

        verify(flightDataUpdateMessageData, times(1)).isValid();

        verify(poServer, times(1)).getPurchaseOrderLinesByLot(flightDataUpdateMessageData.getFlightNumber());

        ArgumentCaptor<PurchaseOrderLineData> purchaseOrderLineDataCaptor = ArgumentCaptor
                .forClass(PurchaseOrderLineData.class);
        verify(poServer).modifyPOLine(purchaseOrderLineDataCaptor.capture());
        PurchaseOrderLineData updatedPurchaseOrderLineData = purchaseOrderLineDataCaptor.getValue();
        // Search keys
        assertEquals(4, updatedPurchaseOrderLineData.getKeyArray().length);
        assertTrue(Arrays.stream(updatedPurchaseOrderLineData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(PurchaseOrderLineData.ORDERID_NAME)));
        assertTrue(Arrays.stream(updatedPurchaseOrderLineData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(PurchaseOrderLineData.ITEM_NAME)));
        assertTrue(Arrays.stream(updatedPurchaseOrderLineData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(PurchaseOrderLineData.LOT_NAME)));
        assertTrue(Arrays.stream(updatedPurchaseOrderLineData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(PurchaseOrderLineData.LINEID_NAME)));
        // Updated columns
        assertEquals(ConversionUtil.convertDateStringToDate(VALID_DEFAULT_RETRIEVAL_DATETIME),
                updatedPurchaseOrderLineData.getExpirationDate());

        ArgumentCaptor<PurchaseOrderHeaderData> purchaseOrderHeaderDataCaptor = ArgumentCaptor
                .forClass(PurchaseOrderHeaderData.class);
        verify(poServer).modifyPOHead(purchaseOrderHeaderDataCaptor.capture());
        PurchaseOrderHeaderData updatedPurchaseOrderHeaderData = purchaseOrderHeaderDataCaptor.getValue();
        // Search keys
        assertEquals(1, updatedPurchaseOrderHeaderData.getKeyArray().length);
        assertTrue(Arrays.stream(updatedPurchaseOrderHeaderData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(PurchaseOrderHeaderData.ORDERID_NAME)));
        // Updated columns
        assertEquals(ConversionUtil.convertDateStringToDate(VALID_FLIGHT_SCHEDULED_DATETIME),
                updatedPurchaseOrderHeaderData.getExpectedDate());
        assertEquals(FINAL_SORT_LOCATION, updatedPurchaseOrderHeaderData.getFinalSortLocationId());

        verify(inventoryServer, times(1)).getLoadLineItemDataListByLot(flightDataUpdateMessageData.getFlightNumber());

        ArgumentCaptor<LoadLineItemData> loadLineItemDataCaptor = ArgumentCaptor.forClass(LoadLineItemData.class);
        verify(inventoryServer).modifyLoadLineItem(loadLineItemDataCaptor.capture());
        LoadLineItemData updatedLoadLineItemData = loadLineItemDataCaptor.getValue();
        // Search keys
        assertEquals(5, updatedLoadLineItemData.getKeyArray().length);
        assertTrue(Arrays.stream(updatedLoadLineItemData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(LoadLineItemData.LOADID_NAME)));
        assertTrue(Arrays.stream(updatedLoadLineItemData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(LoadLineItemData.ITEM_NAME)));
        assertTrue(Arrays.stream(updatedLoadLineItemData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(LoadLineItemData.LOT_NAME)));
        assertTrue(Arrays.stream(updatedLoadLineItemData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(LoadLineItemData.LINEID_NAME)));
        assertTrue(Arrays.stream(updatedLoadLineItemData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(LoadLineItemData.ORDERID_NAME)));
        // Updated columns
        assertEquals(ConversionUtil.convertDateStringToDate(VALID_DEFAULT_RETRIEVAL_DATETIME),
                updatedLoadLineItemData.getExpirationDate());

        ArgumentCaptor<LoadData> loadDataCaptor = ArgumentCaptor.forClass(LoadData.class);
        verify(loadServer).modifyLoad(loadDataCaptor.capture());
        LoadData updatedLoadData = loadDataCaptor.getValue();
        // Search keys
        assertEquals(1, updatedLoadData.getKeyArray().length);
        assertTrue(Arrays.stream(updatedLoadData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(LoadData.LOADID_NAME)));
        // Updated columns
        assertEquals(FINAL_SORT_LOCATION, updatedLoadData.getFinalSortLocationID());
    }

    @Test
    void shouldCompleteIfNoPurchaseOrderLineDataAndLoadLineItemDataIsFoundWithTheFlightNumber() throws DBException {
        when(flightDataUpdateMessageData.isValid()).thenReturn(true);
        when(flightDataUpdateMessageData.getFlightScheduledDateTime()).thenReturn(VALID_FLIGHT_SCHEDULED_DATETIME);
        when(flightDataUpdateMessageData.getDefaultRetrievalDateTime()).thenReturn(VALID_DEFAULT_RETRIEVAL_DATETIME);
        when(flightDataUpdateMessageData.getFlightNumber()).thenReturn(FLIGHT_NUMBER);

        when(poServer.getPurchaseOrderLinesByLot(flightDataUpdateMessageData.getFlightNumber()))
                .thenReturn(Collections.emptyList());

        when(inventoryServer.getLoadLineItemDataListByLot(flightDataUpdateMessageData.getFlightNumber()))
                .thenReturn(Collections.emptyList());

        try {
            updater.update(flightDataUpdateMessageData);
        } catch (FlightDataUpdateFailureException e) {
            fail("Shouldn't throw an exception");
        }

        verify(flightDataUpdateMessageData, times(1)).isValid();
        verify(poServer, times(1)).getPurchaseOrderLinesByLot(flightDataUpdateMessageData.getFlightNumber());
        verify(poServer, never()).modifyPOLine(any());
        verify(poServer, never()).modifyPOHead(any());
        verify(inventoryServer, never()).modifyLoadLineItem(any());
        verify(loadServer, never()).modifyLoad(any());
    }

    @Test
    void shouldCompleteIfOnePurchaseOrderLineDataAndLoadLineItemDataIsFoundWithTheFlightNumber() throws DBException {
        when(flightDataUpdateMessageData.isValid()).thenReturn(true);
        when(flightDataUpdateMessageData.getFlightScheduledDateTime()).thenReturn(VALID_FLIGHT_SCHEDULED_DATETIME);
        when(flightDataUpdateMessageData.getDefaultRetrievalDateTime()).thenReturn(VALID_DEFAULT_RETRIEVAL_DATETIME);
        when(flightDataUpdateMessageData.getFlightNumber()).thenReturn(FLIGHT_NUMBER);
        when(flightDataUpdateMessageData.getFinalSortLocation()).thenReturn(FINAL_SORT_LOCATION);

        when(purchaseOrderLineData.getOrderID()).thenReturn(ORDER_ID);
        when(purchaseOrderLineData.getItem()).thenReturn(ITEM);
        when(purchaseOrderLineData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(purchaseOrderLineData.getLineID()).thenReturn(BAG_ID);
        when(poServer.getPurchaseOrderLinesByLot(flightDataUpdateMessageData.getFlightNumber()))
                .thenReturn(Collections.singletonList(purchaseOrderLineData));

        when(loadLineItemData.getLoadID()).thenReturn(LOAD_ID);
        when(loadLineItemData.getItem()).thenReturn(ITEM);
        when(loadLineItemData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(loadLineItemData.getLineID()).thenReturn(BAG_ID);
        when(loadLineItemData.getOrderID()).thenReturn(ORDER_ID);
        when(inventoryServer.getLoadLineItemDataListByLot(flightDataUpdateMessageData.getFlightNumber()))
                .thenReturn(Collections.singletonList(loadLineItemData));

        try {
            updater.update(flightDataUpdateMessageData);
        } catch (FlightDataUpdateFailureException e) {
            fail("Shouldn't throw an exception");
        }

        verify(flightDataUpdateMessageData, times(1)).isValid();

        verify(poServer, times(1)).getPurchaseOrderLinesByLot(flightDataUpdateMessageData.getFlightNumber());

        ArgumentCaptor<PurchaseOrderLineData> purchaseOrderLineDataCaptor = ArgumentCaptor
                .forClass(PurchaseOrderLineData.class);
        verify(poServer).modifyPOLine(purchaseOrderLineDataCaptor.capture());
        PurchaseOrderLineData updatedPurchaseOrderLineData = purchaseOrderLineDataCaptor.getValue();
        // Search keys
        assertEquals(4, updatedPurchaseOrderLineData.getKeyArray().length);
        assertTrue(Arrays.stream(updatedPurchaseOrderLineData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(PurchaseOrderLineData.ORDERID_NAME)));
        assertTrue(Arrays.stream(updatedPurchaseOrderLineData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(PurchaseOrderLineData.ITEM_NAME)));
        assertTrue(Arrays.stream(updatedPurchaseOrderLineData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(PurchaseOrderLineData.LOT_NAME)));
        assertTrue(Arrays.stream(updatedPurchaseOrderLineData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(PurchaseOrderLineData.LINEID_NAME)));
        // Updated columns
        assertEquals(ConversionUtil.convertDateStringToDate(VALID_DEFAULT_RETRIEVAL_DATETIME),
                updatedPurchaseOrderLineData.getExpirationDate());

        ArgumentCaptor<PurchaseOrderHeaderData> purchaseOrderHeaderDataCaptor = ArgumentCaptor
                .forClass(PurchaseOrderHeaderData.class);
        verify(poServer).modifyPOHead(purchaseOrderHeaderDataCaptor.capture());
        PurchaseOrderHeaderData updatedPurchaseOrderHeaderData = purchaseOrderHeaderDataCaptor.getValue();
        // Search keys
        assertEquals(1, updatedPurchaseOrderHeaderData.getKeyArray().length);
        assertTrue(Arrays.stream(updatedPurchaseOrderHeaderData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(PurchaseOrderHeaderData.ORDERID_NAME)));
        // Updated columns
        assertEquals(ConversionUtil.convertDateStringToDate(VALID_FLIGHT_SCHEDULED_DATETIME),
                updatedPurchaseOrderHeaderData.getExpectedDate());
        assertEquals(FINAL_SORT_LOCATION, updatedPurchaseOrderHeaderData.getFinalSortLocationId());

        verify(inventoryServer, times(1)).getLoadLineItemDataListByLot(flightDataUpdateMessageData.getFlightNumber());

        ArgumentCaptor<LoadLineItemData> loadLineItemDataCaptor = ArgumentCaptor.forClass(LoadLineItemData.class);
        verify(inventoryServer).modifyLoadLineItem(loadLineItemDataCaptor.capture());
        LoadLineItemData updatedLoadLineItemData = loadLineItemDataCaptor.getValue();
        // Search keys
        assertEquals(5, updatedLoadLineItemData.getKeyArray().length);
        assertTrue(Arrays.stream(updatedLoadLineItemData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(LoadLineItemData.LOADID_NAME)));
        assertTrue(Arrays.stream(updatedLoadLineItemData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(LoadLineItemData.ITEM_NAME)));
        assertTrue(Arrays.stream(updatedLoadLineItemData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(LoadLineItemData.LOT_NAME)));
        assertTrue(Arrays.stream(updatedLoadLineItemData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(LoadLineItemData.LINEID_NAME)));
        assertTrue(Arrays.stream(updatedLoadLineItemData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(LoadLineItemData.ORDERID_NAME)));
        // Updated columns
        assertEquals(ConversionUtil.convertDateStringToDate(VALID_DEFAULT_RETRIEVAL_DATETIME),
                updatedLoadLineItemData.getExpirationDate());

        ArgumentCaptor<LoadData> loadDataCaptor = ArgumentCaptor.forClass(LoadData.class);
        verify(loadServer).modifyLoad(loadDataCaptor.capture());
        LoadData updatedLoadData = loadDataCaptor.getValue();
        // Search keys
        assertEquals(1, updatedLoadData.getKeyArray().length);
        assertTrue(Arrays.stream(updatedLoadData.getKeyArray())
                .anyMatch(key -> key.getColumnName().equals(LoadData.LOADID_NAME)));
        // Updated columns
        assertEquals(FINAL_SORT_LOCATION, updatedLoadData.getFinalSortLocationID());
    }
}
