package com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSInventoryServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLoadServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLocationServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSPoReceivingServer;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.ACPTableJoin;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSTableJoin;
import com.daifukuoc.wrxj.custom.ebs.host.messages.ExpectedReceiptMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.AlreadyStoredLoadException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.InvalidExpectedReceiptException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.LoadCreationOrUpdateFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.LoadSearchingFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.LocationReservationFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.LocationSearchingFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.NoRemainingEmptyLocationException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.POCreationFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.StationSearchingFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.util.ConversionUtil;
import com.daifukuoc.wrxj.custom.ebs.host.util.EBSHostMessageConstants;

@ExtendWith(MockitoExtension.class)
class AisleBasedEmptyLocationFinderImplTest {

    private static final String LOAD_ID = "1001";
    private static final String ORDER_ID = "1234";
    private static final String FLIGHT_SCHEDULED_DATETIME = "20221102130000";
    private static final String FLIGHT_NUMBER = "FL100";
    private static final String BAG_ID = "BAG1000";
    private static final String FINAL_SORT_LOCATION = "3600";
    private static final String EXPIRATION_DATETIME = "20221102190000";
    private static final String ITEM_NAME = "ItemName";
    private static final String CONTAINER_TYPE = "ContainerType";

    private static final String AISLE1_DEVICE = "ACP1";
    private static final String AISLE2_DEVICE = "ACP2";

    private static final String AISLE1_BANK1 = "001";
    private static final String AISLE1_BANK2 = "002";
    private static final String AISLE2_BANK3 = "003";
    private static final String AISLE2_BANK4 = "004";
    
    private static final String AISLE1_LEVEL1 = "01";
    private static final String AISLE1_LEVEL2 = "02";
    private static final String AISLE2_LEVEL3 = "03";
    private static final String AISLE2_LEVEL4 = "04";

    private static final String BAY = "001";
    private static final String LEVEL = "001";

    private static final String AISLE1_STATION_NAME = "1001";
    private static final String AISLE2_STATION_NAME = "1002";

    private static final String AISLE1_RESERVED_LOCATION = AISLE1_BANK1 + BAY + LEVEL;
    private static final String AISLE2_RESERVED_LOCATION = AISLE2_BANK3 + BAY + LEVEL;

    private static final String EMPTY_STRING = "";

    MockedStatic<Factory> mockedFactory;
    MockedStatic<DBHelper> mockedDBHelper;

    @Mock
    ExpectedReceiptMessageData expectedReceiptMessageData;

    @Mock
    LoadLineItemData loadLineItemData;

    @Mock
    LoadData loadData;

    @Mock
    PurchaseOrderHeaderData purchaseOrderHeaderData;

    @Mock
    PurchaseOrderLineData purchaseOrderLineData;

    @Mock
    LocationData locationData;

    @Mock
    StationData stationData;

    @Mock
    EBSPoReceivingServer poServer;

    @Mock
    EBSLocationServer locationServer;

    @Mock
    EBSLoadServer loadServer;

    @Mock
    StandardStationServer stationServer;

    @Mock
    EBSInventoryServer inventoryServer;

    @Mock
    ACPTableJoin tableJoin;

    EmptyLocationFinder finder;

    @BeforeEach
    void setUp() throws Exception {
        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedDBHelper = Mockito.mockStatic(DBHelper.class);

        mockedFactory.when(() -> Factory.create(ExpectedReceiptMessageData.class))
                .thenReturn(expectedReceiptMessageData);
        mockedFactory.when(() -> Factory.create(LoadLineItemData.class)).thenReturn(loadLineItemData);
        mockedFactory.when(() -> Factory.create(LoadData.class)).thenReturn(loadData);
        mockedFactory.when(() -> Factory.create(PurchaseOrderHeaderData.class)).thenReturn(purchaseOrderHeaderData);
        mockedFactory.when(() -> Factory.create(PurchaseOrderLineData.class)).thenReturn(purchaseOrderLineData);
        mockedFactory.when(() -> Factory.create(LocationData.class)).thenReturn(locationData);
        mockedFactory.when(() -> Factory.create(StationData.class)).thenReturn(stationData);

        mockedFactory.when(() -> Factory.create(EBSPoReceivingServer.class)).thenReturn(poServer);
        mockedFactory.when(() -> Factory.create(EBSLocationServer.class)).thenReturn(locationServer);
        mockedFactory.when(() -> Factory.create(EBSLoadServer.class)).thenReturn(loadServer);
        mockedFactory.when(() -> Factory.create(StandardStationServer.class)).thenReturn(stationServer);
        mockedFactory.when(() -> Factory.create(EBSInventoryServer.class)).thenReturn(inventoryServer);
        mockedFactory.when(() -> Factory.create(ACPTableJoin.class)).thenReturn(tableJoin);

        mockedDBHelper.when(() -> DBHelper.getStringField(anyMap(), eq(LoadLineItemData.LOT_NAME)))
                .thenReturn(FLIGHT_NUMBER);
        mockedDBHelper.when(() -> DBHelper.getStringField(anyMap(), eq(LoadLineItemData.LINEID_NAME)))
                .thenReturn(BAG_ID);

        finder = new AisleBasedEmptyLocationFinderImpl();
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedFactory.close();
        mockedDBHelper.close();
    }

    @Test
    void shouldThrowAnInvalidExpectedReceiptExceptionWhenExpectedReceiptMessageDataIsNull() {
        assertThrows(InvalidExpectedReceiptException.class, () -> {
            finder.find(null);
        });
    }

    @Test
    void shouldThrowAnInvalidExpectedReceiptExceptionWhenExpectedReceiptMessageDataIsInvalid() {
        when(expectedReceiptMessageData.isValid()).thenReturn(false);

        assertThrows(InvalidExpectedReceiptException.class, () -> {
            finder.find(expectedReceiptMessageData);
        });

        verify(expectedReceiptMessageData, times(1)).isValid();
    }

    @Test
    void shouldThrowALoadSearchingFailureExceptionWhenPopulatingLoadLineItemIsFailed() throws DBException {
        when(expectedReceiptMessageData.isValid()).thenReturn(true);
        when(expectedReceiptMessageData.getLoadId()).thenReturn(LOAD_ID);
        when(expectedReceiptMessageData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(expectedReceiptMessageData.getLineId()).thenReturn(BAG_ID);

        when(inventoryServer.getLoadLineItemDataListByLoadID(eq(LOAD_ID))).thenThrow(new DBException());

        assertThrows(LoadSearchingFailureException.class, () -> {
            finder.find(expectedReceiptMessageData);
        });

        verify(expectedReceiptMessageData, times(1)).isValid();
        verify(inventoryServer, times(1)).getLoadLineItemDataListByLoadID(eq(LOAD_ID));
    }

    @Test
    void shouldThrowALoadSearchingFailureExceptionWhenPopulatingLoadIsFailed() throws DBException {
        when(expectedReceiptMessageData.isValid()).thenReturn(true);
        when(expectedReceiptMessageData.getLoadId()).thenReturn(LOAD_ID);
        when(expectedReceiptMessageData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(expectedReceiptMessageData.getLineId()).thenReturn(BAG_ID);

        List<Map> loadLineItems = new ArrayList<>();
        loadLineItems.add(new HashMap());
        doNothing().when(loadLineItemData).dataToSKDCData(anyMap());
        when(inventoryServer.getLoadLineItemDataListByLoadID(eq(LOAD_ID))).thenReturn(loadLineItems);

        when(loadServer.getLoad(eq(LOAD_ID))).thenReturn(null);

        assertThrows(LoadSearchingFailureException.class, () -> {
            finder.find(expectedReceiptMessageData);
        });

        verify(expectedReceiptMessageData, times(1)).isValid();
        verify(inventoryServer, times(1)).getLoadLineItemDataListByLoadID(eq(LOAD_ID));
        verify(loadServer, times(1)).getLoad(eq(LOAD_ID));
    }

    @Test
    void shouldThrowALocationSearchingFailureExceptionWhenPopulatingLocationOfExistingLoadIsFailed()
            throws DBException {
        when(expectedReceiptMessageData.isValid()).thenReturn(true);
        when(expectedReceiptMessageData.getLoadId()).thenReturn(LOAD_ID);
        when(expectedReceiptMessageData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(expectedReceiptMessageData.getLineId()).thenReturn(BAG_ID);

        List<Map> loadLineItems = new ArrayList<>();
        loadLineItems.add(new HashMap());
        doNothing().when(loadLineItemData).dataToSKDCData(anyMap());
        when(inventoryServer.getLoadLineItemDataListByLoadID(eq(LOAD_ID))).thenReturn(loadLineItems);

        when(loadData.getAddress()).thenReturn(AISLE1_RESERVED_LOCATION);
        when(loadServer.getLoad(eq(LOAD_ID))).thenReturn(loadData);

        when(locationServer.getLocationRecord(eq(EBSHostMessageConstants.WAREHOUSE_NAME), eq(AISLE1_RESERVED_LOCATION)))
                .thenThrow(new DBException());

        assertThrows(LocationSearchingFailureException.class, () -> {
            finder.find(expectedReceiptMessageData);
        });

        verify(expectedReceiptMessageData, times(1)).isValid();
        verify(inventoryServer, times(1)).getLoadLineItemDataListByLoadID(eq(LOAD_ID));
        verify(loadServer, times(1)).getLoad(eq(LOAD_ID));
        verify(locationServer, times(1)).getLocationRecord(eq(EBSHostMessageConstants.WAREHOUSE_NAME),
                eq(AISLE1_RESERVED_LOCATION));
    }

    @Test
    void shouldThrowAStationSearchingFailureExceptionWhenPopulatingEntranceStationOfExistingLoadIsFailed()
            throws DBException {
        when(expectedReceiptMessageData.isValid()).thenReturn(true);
        when(expectedReceiptMessageData.getLoadId()).thenReturn(LOAD_ID);
        when(expectedReceiptMessageData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(expectedReceiptMessageData.getLineId()).thenReturn(BAG_ID);

        List<Map> loadLineItems = new ArrayList<>();
        loadLineItems.add(new HashMap());
        doNothing().when(loadLineItemData).dataToSKDCData(anyMap());
        when(inventoryServer.getLoadLineItemDataListByLoadID(eq(LOAD_ID))).thenReturn(loadLineItems);

        when(loadData.getAddress()).thenReturn(AISLE1_RESERVED_LOCATION);
        when(loadServer.getLoad(eq(LOAD_ID))).thenReturn(loadData);

        when(locationData.getLocationStatus()).thenReturn(DBConstants.LCAVAIL);
        when(locationData.getEmptyFlag()).thenReturn(DBConstants.LCRESERVED);
        when(locationData.getDeviceID()).thenReturn(AISLE1_DEVICE);
        when(locationServer.getLocationRecord(eq(EBSHostMessageConstants.WAREHOUSE_NAME), eq(AISLE1_RESERVED_LOCATION)))
                .thenReturn(locationData);

        when(stationServer.getStationByDeviceList(eq(AISLE1_DEVICE))).thenThrow(new DBException());

        assertThrows(StationSearchingFailureException.class, () -> {
            finder.find(expectedReceiptMessageData);
        });

        verify(expectedReceiptMessageData, times(1)).isValid();
        verify(inventoryServer, times(1)).getLoadLineItemDataListByLoadID(eq(LOAD_ID));
        verify(loadServer, times(1)).getLoad(eq(LOAD_ID));
        verify(locationServer, times(1)).getLocationRecord(eq(EBSHostMessageConstants.WAREHOUSE_NAME),
                eq(AISLE1_RESERVED_LOCATION));
        verify(stationServer, times(1)).getStationByDeviceList(eq(AISLE1_DEVICE));
    }

    @Test
    void shouldThrowAStationSearchingFailureExceptionWhenPopulatedEntranceStationOfExistingLoadIsNull()
            throws DBException {
        when(expectedReceiptMessageData.isValid()).thenReturn(true);
        when(expectedReceiptMessageData.getLoadId()).thenReturn(LOAD_ID);
        when(expectedReceiptMessageData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(expectedReceiptMessageData.getLineId()).thenReturn(BAG_ID);

        List<Map> loadLineItems = new ArrayList<>();
        loadLineItems.add(new HashMap());
        doNothing().when(loadLineItemData).dataToSKDCData(anyMap());
        when(inventoryServer.getLoadLineItemDataListByLoadID(eq(LOAD_ID))).thenReturn(loadLineItems);

        when(loadData.getAddress()).thenReturn(AISLE1_RESERVED_LOCATION);
        when(loadServer.getLoad(eq(LOAD_ID))).thenReturn(loadData);

        when(locationData.getLocationStatus()).thenReturn(DBConstants.LCAVAIL);
        when(locationData.getEmptyFlag()).thenReturn(DBConstants.LCRESERVED);
        when(locationData.getDeviceID()).thenReturn(AISLE1_DEVICE);
        when(locationServer.getLocationRecord(eq(EBSHostMessageConstants.WAREHOUSE_NAME), eq(AISLE1_RESERVED_LOCATION)))
                .thenReturn(locationData);

        when(stationServer.getStationByDeviceList(eq(AISLE1_DEVICE))).thenReturn(Collections.<Map> emptyList());

        assertThrows(StationSearchingFailureException.class, () -> {
            finder.find(expectedReceiptMessageData);
        });

        verify(expectedReceiptMessageData, times(1)).isValid();
        verify(inventoryServer, times(1)).getLoadLineItemDataListByLoadID(eq(LOAD_ID));
        verify(loadServer, times(1)).getLoad(eq(LOAD_ID));
        verify(locationServer, times(1)).getLocationRecord(eq(EBSHostMessageConstants.WAREHOUSE_NAME),
                eq(AISLE1_RESERVED_LOCATION));
        verify(stationServer, times(1)).getStationByDeviceList(eq(AISLE1_DEVICE));
    }

    @Test
    void shouldThrowAStationSearchingFailureExceptionWhenPopulatedEntranceStationOfExistingLoadHasEmptyName()
            throws DBException {
        when(expectedReceiptMessageData.isValid()).thenReturn(true);
        when(expectedReceiptMessageData.getLoadId()).thenReturn(LOAD_ID);
        when(expectedReceiptMessageData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(expectedReceiptMessageData.getLineId()).thenReturn(BAG_ID);

        List<Map> loadLineItems = new ArrayList<>();
        loadLineItems.add(new HashMap());
        doNothing().when(loadLineItemData).dataToSKDCData(anyMap());
        when(inventoryServer.getLoadLineItemDataListByLoadID(eq(LOAD_ID))).thenReturn(loadLineItems);

        when(loadData.getAddress()).thenReturn(AISLE1_RESERVED_LOCATION);
        when(loadServer.getLoad(eq(LOAD_ID))).thenReturn(loadData);

        when(locationData.getLocationStatus()).thenReturn(DBConstants.LCAVAIL);
        when(locationData.getEmptyFlag()).thenReturn(DBConstants.LCRESERVED);
        when(locationData.getDeviceID()).thenReturn(AISLE1_DEVICE);
        when(locationServer.getLocationRecord(eq(EBSHostMessageConstants.WAREHOUSE_NAME), eq(AISLE1_RESERVED_LOCATION)))
                .thenReturn(locationData);

        List<Map> stationDataList = new ArrayList<>();
        stationDataList.add(new HashMap());
        doNothing().when(stationData).dataToSKDCData(anyMap());
        when(stationData.getStationType()).thenReturn(DBConstants.INPUT);
        when(stationData.getStationName()).thenReturn(EMPTY_STRING);
        when(stationServer.getStationByDeviceList(eq(AISLE1_DEVICE))).thenReturn(stationDataList);

        assertThrows(StationSearchingFailureException.class, () -> {
            finder.find(expectedReceiptMessageData);
        });

        verify(expectedReceiptMessageData, times(1)).isValid();
        verify(inventoryServer, times(1)).getLoadLineItemDataListByLoadID(eq(LOAD_ID));
        verify(loadServer, times(1)).getLoad(eq(LOAD_ID));
        verify(locationServer, times(1)).getLocationRecord(eq(EBSHostMessageConstants.WAREHOUSE_NAME),
                eq(AISLE1_RESERVED_LOCATION));
        verify(stationServer, times(1)).getStationByDeviceList(eq(AISLE1_DEVICE));
    }

    @Test
    void shouldReturnEntranceStationNameWhenTheERWasAlreadyProcessedAndTheReservedLocationIsStillAvailableAndReserved()
            throws DBException, NoRemainingEmptyLocationException, LocationSearchingFailureException,
            InvalidExpectedReceiptException, LoadCreationOrUpdateFailureException, LoadSearchingFailureException,
            AlreadyStoredLoadException, StationSearchingFailureException, LocationReservationFailureException,
            POCreationFailureException {
        when(expectedReceiptMessageData.isValid()).thenReturn(true);
        when(expectedReceiptMessageData.getLoadId()).thenReturn(LOAD_ID);
        when(expectedReceiptMessageData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(expectedReceiptMessageData.getLineId()).thenReturn(BAG_ID);

        List<Map> loadLineItems = new ArrayList<>();
        loadLineItems.add(new HashMap());
        doNothing().when(loadLineItemData).dataToSKDCData(anyMap());
        when(inventoryServer.getLoadLineItemDataListByLoadID(eq(LOAD_ID))).thenReturn(loadLineItems);

        when(loadData.getAddress()).thenReturn(AISLE1_RESERVED_LOCATION);
        when(loadServer.getLoad(eq(LOAD_ID))).thenReturn(loadData);

        when(locationData.getLocationStatus()).thenReturn(DBConstants.LCAVAIL);
        when(locationData.getEmptyFlag()).thenReturn(DBConstants.LCRESERVED);
        when(locationData.getDeviceID()).thenReturn(AISLE1_DEVICE);
        when(locationServer.getLocationRecord(eq(EBSHostMessageConstants.WAREHOUSE_NAME), eq(AISLE1_RESERVED_LOCATION)))
                .thenReturn(locationData);

        List<Map> stationDataList = new ArrayList<>();
        stationDataList.add(new HashMap());
        doNothing().when(stationData).dataToSKDCData(anyMap());
        when(stationData.getStationType()).thenReturn(DBConstants.INPUT);
        when(stationData.getStationName()).thenReturn(AISLE1_STATION_NAME);
        when(stationServer.getStationByDeviceList(eq(AISLE1_DEVICE))).thenReturn(stationDataList);

        assertEquals(AISLE1_STATION_NAME, finder.find(expectedReceiptMessageData));

        verify(expectedReceiptMessageData, times(1)).isValid();
        verify(inventoryServer, times(1)).getLoadLineItemDataListByLoadID(eq(LOAD_ID));
        verify(loadServer, times(1)).getLoad(eq(LOAD_ID));
        verify(locationServer, times(1)).getLocationRecord(eq(EBSHostMessageConstants.WAREHOUSE_NAME),
                eq(AISLE1_RESERVED_LOCATION));
        verify(stationServer, times(1)).getStationByDeviceList(eq(AISLE1_DEVICE));
    }

    @Test
    void shouldThrowALocationReservationFailureExceptionWhenReservingExistingLocationOfAnExistingLoadIsFailed()
            throws DBException, NoRemainingEmptyLocationException, LocationSearchingFailureException,
            InvalidExpectedReceiptException, LoadCreationOrUpdateFailureException, LoadSearchingFailureException,
            AlreadyStoredLoadException, StationSearchingFailureException, LocationReservationFailureException,
            POCreationFailureException {
        when(expectedReceiptMessageData.isValid()).thenReturn(true);
        when(expectedReceiptMessageData.getLoadId()).thenReturn(LOAD_ID);
        when(expectedReceiptMessageData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(expectedReceiptMessageData.getLineId()).thenReturn(BAG_ID);

        List<Map> loadLineItems = new ArrayList<>();
        loadLineItems.add(new HashMap());
        doNothing().when(loadLineItemData).dataToSKDCData(anyMap());
        when(inventoryServer.getLoadLineItemDataListByLoadID(eq(LOAD_ID))).thenReturn(loadLineItems);

        when(loadData.getAddress()).thenReturn(AISLE1_RESERVED_LOCATION);
        when(loadServer.getLoad(eq(LOAD_ID))).thenReturn(loadData);

        when(locationData.getLocationStatus()).thenReturn(DBConstants.LCAVAIL);
        when(locationData.getEmptyFlag()).thenReturn(DBConstants.UNOCCUPIED);
        when(locationData.getWarehouse()).thenReturn(EBSHostMessageConstants.WAREHOUSE_NAME);
        when(locationData.getAddress()).thenReturn(AISLE1_RESERVED_LOCATION);
        when(locationData.getShelfPosition()).thenReturn(EMPTY_STRING);
        when(locationServer.getLocationRecord(eq(EBSHostMessageConstants.WAREHOUSE_NAME), eq(AISLE1_RESERVED_LOCATION)))
                .thenReturn(locationData);

        doThrow(new DBException()).when(locationServer).setLocationEmptyFlag(eq(EBSHostMessageConstants.WAREHOUSE_NAME),
                eq(AISLE1_RESERVED_LOCATION), eq(EMPTY_STRING), eq(DBConstants.LCRESERVED));

        assertThrows(LocationReservationFailureException.class, () -> {
            finder.find(expectedReceiptMessageData);
        });

        verify(expectedReceiptMessageData, times(1)).isValid();
        verify(inventoryServer, times(1)).getLoadLineItemDataListByLoadID(eq(LOAD_ID));
        verify(loadServer, times(1)).getLoad(eq(LOAD_ID));
        verify(locationServer, times(1)).getLocationRecord(eq(EBSHostMessageConstants.WAREHOUSE_NAME),
                eq(AISLE1_RESERVED_LOCATION));
        verify(locationServer, times(1)).setLocationEmptyFlag(eq(EBSHostMessageConstants.WAREHOUSE_NAME),
                eq(AISLE1_RESERVED_LOCATION), eq(EMPTY_STRING), eq(DBConstants.LCRESERVED));
    }

    @Test
    void shouldReturnEntranceStationNameWhenTheERWasAlreadyProcessedButTheReservedLocationIsNotReserved()
            throws DBException, NoRemainingEmptyLocationException, LocationSearchingFailureException,
            InvalidExpectedReceiptException, LoadCreationOrUpdateFailureException, LoadSearchingFailureException,
            AlreadyStoredLoadException, StationSearchingFailureException, LocationReservationFailureException,
            POCreationFailureException {
        when(expectedReceiptMessageData.isValid()).thenReturn(true);
        when(expectedReceiptMessageData.getLoadId()).thenReturn(LOAD_ID);
        when(expectedReceiptMessageData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(expectedReceiptMessageData.getLineId()).thenReturn(BAG_ID);

        List<Map> loadLineItems = new ArrayList<>();
        loadLineItems.add(new HashMap());
        doNothing().when(loadLineItemData).dataToSKDCData(anyMap());
        when(inventoryServer.getLoadLineItemDataListByLoadID(eq(LOAD_ID))).thenReturn(loadLineItems);

        when(loadData.getAddress()).thenReturn(AISLE1_RESERVED_LOCATION);
        when(loadServer.getLoad(eq(LOAD_ID))).thenReturn(loadData);

        when(locationData.getLocationStatus()).thenReturn(DBConstants.LCAVAIL);
        when(locationData.getEmptyFlag()).thenReturn(DBConstants.UNOCCUPIED);
        when(locationData.getDeviceID()).thenReturn(AISLE1_DEVICE);
        when(locationData.getWarehouse()).thenReturn(EBSHostMessageConstants.WAREHOUSE_NAME);
        when(locationData.getAddress()).thenReturn(AISLE1_RESERVED_LOCATION);
        when(locationData.getShelfPosition()).thenReturn(EMPTY_STRING);
        when(locationServer.getLocationRecord(eq(EBSHostMessageConstants.WAREHOUSE_NAME), eq(AISLE1_RESERVED_LOCATION)))
                .thenReturn(locationData);

        doNothing().when(locationServer).setLocationEmptyFlag(eq(EBSHostMessageConstants.WAREHOUSE_NAME),
                eq(AISLE1_RESERVED_LOCATION), eq(EMPTY_STRING), eq(DBConstants.LCRESERVED));

        List<Map> stationDataList = new ArrayList<>();
        stationDataList.add(new HashMap());
        doNothing().when(stationData).dataToSKDCData(anyMap());
        when(stationData.getStationType()).thenReturn(DBConstants.INPUT);
        when(stationData.getStationName()).thenReturn(AISLE1_STATION_NAME);
        when(stationServer.getStationByDeviceList(eq(AISLE1_DEVICE))).thenReturn(stationDataList);

        assertEquals(AISLE1_STATION_NAME, finder.find(expectedReceiptMessageData));

        verify(expectedReceiptMessageData, times(1)).isValid();
        verify(inventoryServer, times(1)).getLoadLineItemDataListByLoadID(eq(LOAD_ID));
        verify(loadServer, times(1)).getLoad(eq(LOAD_ID));
        verify(locationServer, times(1)).getLocationRecord(eq(EBSHostMessageConstants.WAREHOUSE_NAME),
                eq(AISLE1_RESERVED_LOCATION));
        verify(stationServer, times(1)).getStationByDeviceList(eq(AISLE1_DEVICE));
        verify(locationServer, times(1)).setLocationEmptyFlag(eq(EBSHostMessageConstants.WAREHOUSE_NAME),
                eq(AISLE1_RESERVED_LOCATION), eq(EMPTY_STRING), eq(DBConstants.LCRESERVED));
    }

    @Test
    void shouldThrowAnAlreadyStoredLoadExceptionWhenAnExistingLoadWasAlreadyStored()
            throws DBException, NoRemainingEmptyLocationException, LocationSearchingFailureException,
            InvalidExpectedReceiptException, LoadCreationOrUpdateFailureException, LoadSearchingFailureException,
            AlreadyStoredLoadException, StationSearchingFailureException, LocationReservationFailureException,
            POCreationFailureException {
        when(expectedReceiptMessageData.isValid()).thenReturn(true);
        when(expectedReceiptMessageData.getLoadId()).thenReturn(LOAD_ID);
        when(expectedReceiptMessageData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(expectedReceiptMessageData.getLineId()).thenReturn(BAG_ID);

        List<Map> loadLineItems = new ArrayList<>();
        loadLineItems.add(new HashMap());
        doNothing().when(loadLineItemData).dataToSKDCData(anyMap());
        when(inventoryServer.getLoadLineItemDataListByLoadID(eq(LOAD_ID))).thenReturn(loadLineItems);

        when(loadData.getAddress()).thenReturn(AISLE1_RESERVED_LOCATION);
        when(loadServer.getLoad(eq(LOAD_ID))).thenReturn(loadData);

        when(locationData.getLocationStatus()).thenReturn(DBConstants.LCAVAIL);
        when(locationData.getEmptyFlag()).thenReturn(DBConstants.OCCUPIED);
        when(locationData.getAddress()).thenReturn(AISLE1_RESERVED_LOCATION);
        when(locationServer.getLocationRecord(eq(EBSHostMessageConstants.WAREHOUSE_NAME), eq(AISLE1_RESERVED_LOCATION)))
                .thenReturn(locationData);

        assertThrows(AlreadyStoredLoadException.class, () -> {
            finder.find(expectedReceiptMessageData);
        });

        verify(expectedReceiptMessageData, times(1)).isValid();
        verify(inventoryServer, times(1)).getLoadLineItemDataListByLoadID(eq(LOAD_ID));
        verify(loadServer, times(1)).getLoad(eq(LOAD_ID));
        verify(locationServer, times(1)).getLocationRecord(eq(EBSHostMessageConstants.WAREHOUSE_NAME),
                eq(AISLE1_RESERVED_LOCATION));
    }

    /////////////////////////////////////////////////////////////////
    // When we need to find an empty location for ER
    // - A new ER
    // - An existing ER but location is not available
    // - An existing ER but location of the load is not valid
    /////////////////////////////////////////////////////////////////

    @Test
    void shouldThrowAnInvalidExpectedReceiptExceptionWhenPopulatingPOHeaderIsFailed() throws DBException {
        when(expectedReceiptMessageData.isValid()).thenReturn(true);
        when(expectedReceiptMessageData.getOrderId()).thenReturn(ORDER_ID);

        when(poServer.getPoHeaderRecord(expectedReceiptMessageData.getOrderId())).thenThrow(new DBException());

        assertThrows(InvalidExpectedReceiptException.class, () -> {
            finder.find(expectedReceiptMessageData);
        });

        verify(expectedReceiptMessageData, times(1)).isValid();
        verify(poServer, times(1)).getPoHeaderRecord(expectedReceiptMessageData.getOrderId());
    }

    @Test
    void shouldThrowAPOCreationFailureExceptionWhenCreatingPORecordIsFailed() throws DBException, ParseException {
        when(expectedReceiptMessageData.isValid()).thenReturn(true);
        when(expectedReceiptMessageData.getOrderId()).thenReturn(ORDER_ID);

        when(poServer.getPoHeaderRecord(expectedReceiptMessageData.getOrderId())).thenReturn(null);

        doThrow(new ParseException(EMPTY_STRING, 0)).when(poServer).addPOExpectedReceipt(expectedReceiptMessageData);

        assertThrows(POCreationFailureException.class, () -> {
            finder.find(expectedReceiptMessageData);
        });

        verify(expectedReceiptMessageData, times(1)).isValid();
        verify(poServer, times(1)).getPoHeaderRecord(expectedReceiptMessageData.getOrderId());
        verify(poServer, times(1)).addPOExpectedReceipt(expectedReceiptMessageData);
    }

    @Test
    void shouldCreateANewPOWhenPOHeaderIsNotFoundForER() throws DBException, ParseException {
        when(expectedReceiptMessageData.isValid()).thenReturn(true);
        when(expectedReceiptMessageData.getOrderId()).thenReturn(ORDER_ID);

        when(poServer.getPoHeaderRecord(expectedReceiptMessageData.getOrderId())).thenReturn(null);

        doNothing().when(poServer).addPOExpectedReceipt(expectedReceiptMessageData);

        // Don't want to verify the rest of processing, so just ignoring any exceptions
        try {
            finder.find(expectedReceiptMessageData);
        } catch (Exception e) {

        }

        verify(expectedReceiptMessageData, times(1)).isValid();
        verify(poServer, times(1)).getPoHeaderRecord(expectedReceiptMessageData.getOrderId());
        verify(poServer, times(1)).addPOExpectedReceipt(expectedReceiptMessageData);
    }

    @Test
    void shouldThrowAnInvalidExpectedReceiptExceptionWhenPOLineIsNotFound() throws DBException {
        when(expectedReceiptMessageData.isValid()).thenReturn(true);
        when(expectedReceiptMessageData.getOrderId()).thenReturn(ORDER_ID);

        when(poServer.getPoHeaderRecord(expectedReceiptMessageData.getOrderId())).thenReturn(purchaseOrderHeaderData);
        when(poServer.getPurchaseOrderLine(expectedReceiptMessageData.getOrderId(), null, null)).thenReturn(null);

        assertThrows(InvalidExpectedReceiptException.class, () -> {
            finder.find(expectedReceiptMessageData);
        });

        verify(expectedReceiptMessageData, times(1)).isValid();
        verify(poServer, times(1)).getPoHeaderRecord(expectedReceiptMessageData.getOrderId());
        verify(poServer, times(1)).getPurchaseOrderLine(expectedReceiptMessageData.getOrderId(), null, null);
    }

    @Test
    void shouldThrowALocationSearchingFailureExceptionWhenPopulatingLoadsPerDeviceIsFailed() throws DBException {
        when(expectedReceiptMessageData.isValid()).thenReturn(true);
        when(expectedReceiptMessageData.getOrderId()).thenReturn(ORDER_ID);
        when(expectedReceiptMessageData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(expectedReceiptMessageData.getFlightScheduledDateTime()).thenReturn(FLIGHT_SCHEDULED_DATETIME);

        when(poServer.getPoHeaderRecord(expectedReceiptMessageData.getOrderId())).thenReturn(purchaseOrderHeaderData);

        List<Map> purchaseOrderLineDataList = new ArrayList<>();
        purchaseOrderLineDataList.add(new HashMap());
        when(poServer.getPurchaseOrderLine(expectedReceiptMessageData.getOrderId(), null, null))
                .thenReturn(purchaseOrderLineDataList);

        doNothing().when(purchaseOrderLineData).dataToSKDCData(anyMap());

        when(tableJoin.getNumberOfLoadPerDeviceId(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(),
                ConversionUtil.convertDateStringToDate(expectedReceiptMessageData.getFlightScheduledDateTime())))
                        .thenThrow(new DBException());

        assertThrows(LocationSearchingFailureException.class, () -> {
            finder.find(expectedReceiptMessageData);
        });

        verify(expectedReceiptMessageData, times(1)).isValid();
        verify(poServer, times(1)).getPoHeaderRecord(expectedReceiptMessageData.getOrderId());
        verify(poServer, times(1)).getPurchaseOrderLine(expectedReceiptMessageData.getOrderId(), null, null);
        verify(purchaseOrderLineData, times(1)).dataToSKDCData(anyMap());
        verify(tableJoin, times(1)).getNumberOfLoadPerDeviceId(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(),
                ConversionUtil.convertDateStringToDate(expectedReceiptMessageData.getFlightScheduledDateTime()));
    }

    @Test
    void shouldThrowANoRemainingEmptyLocationExceptionWhenLoadsPerDeviceReturnsEmptyResult() throws DBException {
        when(expectedReceiptMessageData.isValid()).thenReturn(true);
        when(expectedReceiptMessageData.getOrderId()).thenReturn(ORDER_ID);
        when(expectedReceiptMessageData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(expectedReceiptMessageData.getFlightScheduledDateTime()).thenReturn(FLIGHT_SCHEDULED_DATETIME);

        when(poServer.getPoHeaderRecord(expectedReceiptMessageData.getOrderId())).thenReturn(purchaseOrderHeaderData);

        List<Map> purchaseOrderLineDataList = new ArrayList<>();
        purchaseOrderLineDataList.add(new HashMap());
        when(poServer.getPurchaseOrderLine(expectedReceiptMessageData.getOrderId(), null, null))
                .thenReturn(purchaseOrderLineDataList);

        doNothing().when(purchaseOrderLineData).dataToSKDCData(anyMap());

        // Empty map means no device is enabled
        Map<String, Integer> currentLoadsPerDevice = new HashMap<>();
        when(tableJoin.getNumberOfLoadPerDeviceId(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(),
                ConversionUtil.convertDateStringToDate(expectedReceiptMessageData.getFlightScheduledDateTime())))
                        .thenReturn(currentLoadsPerDevice);

        assertThrows(NoRemainingEmptyLocationException.class, () -> {
            finder.find(expectedReceiptMessageData);
        });

        verify(expectedReceiptMessageData, times(1)).isValid();
        verify(poServer, times(1)).getPoHeaderRecord(expectedReceiptMessageData.getOrderId());
        verify(poServer, times(1)).getPurchaseOrderLine(expectedReceiptMessageData.getOrderId(), null, null);
        verify(purchaseOrderLineData, times(1)).dataToSKDCData(anyMap());
        verify(tableJoin, times(1)).getNumberOfLoadPerDeviceId(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(),
                ConversionUtil.convertDateStringToDate(expectedReceiptMessageData.getFlightScheduledDateTime()));
    }

    @Test
    void shouldThrowLocationSearchingFailureExceptionWhenPopulatingLoadsPerBankIsFailed()
            throws DBException, NoRemainingEmptyLocationException, LocationSearchingFailureException,
            InvalidExpectedReceiptException, LoadCreationOrUpdateFailureException {
        when(expectedReceiptMessageData.isValid()).thenReturn(true);
        when(expectedReceiptMessageData.getOrderId()).thenReturn(ORDER_ID);
        when(expectedReceiptMessageData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(expectedReceiptMessageData.getFlightScheduledDateTime()).thenReturn(FLIGHT_SCHEDULED_DATETIME);

        when(poServer.getPoHeaderRecord(expectedReceiptMessageData.getOrderId())).thenReturn(purchaseOrderHeaderData);

        List<Map> purchaseOrderLineDataList = new ArrayList<>();
        purchaseOrderLineDataList.add(new HashMap());
        when(poServer.getPurchaseOrderLine(expectedReceiptMessageData.getOrderId(), null, null))
                .thenReturn(purchaseOrderLineDataList);

        doNothing().when(purchaseOrderLineData).dataToSKDCData(anyMap());

        // No load, so finder should reserve in aisle 1
        Map<String, Integer> currentLoadsPerDevice = new LinkedHashMap<>();
        currentLoadsPerDevice.put(AISLE1_DEVICE, 0);
        currentLoadsPerDevice.put(AISLE2_DEVICE, 0);
        when(tableJoin.getNumberOfLoadPerDeviceId(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(),
                ConversionUtil.convertDateStringToDate(expectedReceiptMessageData.getFlightScheduledDateTime())))
                        .thenReturn(currentLoadsPerDevice);

        when(tableJoin.getNumberOfLoadPerLevel(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(), AISLE1_DEVICE)).thenThrow(new DBException());

        assertThrows(LocationSearchingFailureException.class, () -> {
            finder.find(expectedReceiptMessageData);
        });

        verify(expectedReceiptMessageData, times(1)).isValid();
        verify(poServer, times(1)).getPoHeaderRecord(expectedReceiptMessageData.getOrderId());
        verify(poServer, times(1)).getPurchaseOrderLine(expectedReceiptMessageData.getOrderId(), null, null);
        verify(purchaseOrderLineData, times(1)).dataToSKDCData(anyMap());
        verify(tableJoin, times(1)).getNumberOfLoadPerDeviceId(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(),
                ConversionUtil.convertDateStringToDate(expectedReceiptMessageData.getFlightScheduledDateTime()));
        verify(tableJoin, times(1)).getNumberOfLoadPerLevel(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(), AISLE1_DEVICE);
        verify(tableJoin, never()).getNumberOfLoadPerLevel(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(), AISLE2_DEVICE);
    }

    @Test
    void shouldThrowNoRemainingEmptyLocationExceptionWhenLoadsPerBankReturnsEmptyResult()
            throws DBException, NoRemainingEmptyLocationException, LocationSearchingFailureException,
            InvalidExpectedReceiptException, LoadCreationOrUpdateFailureException {
        when(expectedReceiptMessageData.isValid()).thenReturn(true);
        when(expectedReceiptMessageData.getOrderId()).thenReturn(ORDER_ID);
        when(expectedReceiptMessageData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(expectedReceiptMessageData.getFlightScheduledDateTime()).thenReturn(FLIGHT_SCHEDULED_DATETIME);

        when(poServer.getPoHeaderRecord(expectedReceiptMessageData.getOrderId())).thenReturn(purchaseOrderHeaderData);

        List<Map> purchaseOrderLineDataList = new ArrayList<>();
        purchaseOrderLineDataList.add(new HashMap());
        when(poServer.getPurchaseOrderLine(expectedReceiptMessageData.getOrderId(), null, null))
                .thenReturn(purchaseOrderLineDataList);

        doNothing().when(purchaseOrderLineData).dataToSKDCData(anyMap());

        // No load, so finder should reserve in aisle 1
        Map<String, Integer> currentLoadsPerDevice = new LinkedHashMap<>();
        currentLoadsPerDevice.put(AISLE1_DEVICE, 0);
        currentLoadsPerDevice.put(AISLE2_DEVICE, 0);
        when(tableJoin.getNumberOfLoadPerDeviceId(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(),
                ConversionUtil.convertDateStringToDate(expectedReceiptMessageData.getFlightScheduledDateTime())))
                        .thenReturn(currentLoadsPerDevice);

        // Empty map means no bank is available
        Map<String, Integer> currentLoadsPerBank = new LinkedHashMap<>();
        when(tableJoin.getNumberOfLoadPerLevel(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(), AISLE1_DEVICE)).thenReturn(currentLoadsPerBank);
        when(tableJoin.getNumberOfLoadPerLevel(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(), AISLE2_DEVICE)).thenReturn(currentLoadsPerBank);

        assertThrows(NoRemainingEmptyLocationException.class, () -> {
            finder.find(expectedReceiptMessageData);
        });

        verify(expectedReceiptMessageData, times(1)).isValid();
        verify(poServer, times(1)).getPoHeaderRecord(expectedReceiptMessageData.getOrderId());
        verify(poServer, times(1)).getPurchaseOrderLine(expectedReceiptMessageData.getOrderId(), null, null);
        verify(purchaseOrderLineData, times(1)).dataToSKDCData(anyMap());
        verify(tableJoin, times(1)).getNumberOfLoadPerDeviceId(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(),
                ConversionUtil.convertDateStringToDate(expectedReceiptMessageData.getFlightScheduledDateTime()));
        verify(tableJoin, times(1)).getNumberOfLoadPerLevel(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(), AISLE1_DEVICE);
        verify(tableJoin, times(1)).getNumberOfLoadPerLevel(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(), AISLE2_DEVICE);
    }

    @Test
    void shouldThrowALocationSearchingFailureExceptionWhenPopulatingEntranceStationsOfDeviceIsFailed()
            throws DBException, NoRemainingEmptyLocationException, LocationSearchingFailureException,
            InvalidExpectedReceiptException, LoadCreationOrUpdateFailureException {
        when(expectedReceiptMessageData.isValid()).thenReturn(true);
        when(expectedReceiptMessageData.getOrderId()).thenReturn(ORDER_ID);
        when(expectedReceiptMessageData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(expectedReceiptMessageData.getFlightScheduledDateTime()).thenReturn(FLIGHT_SCHEDULED_DATETIME);

        when(poServer.getPoHeaderRecord(expectedReceiptMessageData.getOrderId())).thenReturn(purchaseOrderHeaderData);

        List<Map> purchaseOrderLineDataList = new ArrayList<>();
        purchaseOrderLineDataList.add(new HashMap());
        when(poServer.getPurchaseOrderLine(expectedReceiptMessageData.getOrderId(), null, null))
                .thenReturn(purchaseOrderLineDataList);

        doNothing().when(purchaseOrderLineData).dataToSKDCData(anyMap());

        // Only 1 load in aisle 1, so finder should reserve in aisle 2
        Map<String, Integer> currentLoadsPerDevice = new LinkedHashMap<>();
        currentLoadsPerDevice.put(AISLE1_DEVICE, 1);
        currentLoadsPerDevice.put(AISLE2_DEVICE, 0);
        when(tableJoin.getNumberOfLoadPerDeviceId(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(),
                ConversionUtil.convertDateStringToDate(expectedReceiptMessageData.getFlightScheduledDateTime())))
                        .thenReturn(currentLoadsPerDevice);

        // No load in both banks, so 1 location of bank 3 should be reserved
        Map<String, Integer> currentLoadsPerBank = new LinkedHashMap<>();
        currentLoadsPerBank.put(AISLE2_LEVEL3, 0);
        currentLoadsPerBank.put(AISLE2_LEVEL4, 0);
        when(tableJoin.getNumberOfLoadPerLevel(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(), AISLE2_DEVICE)).thenReturn(currentLoadsPerBank);

        when(locationData.getDeviceID()).thenReturn(AISLE2_DEVICE);
        when(locationData.getAddress()).thenReturn(AISLE2_RESERVED_LOCATION);
        when(locationServer.reserveUnoccupiedLocationOfLevelIfAvailable(EBSHostMessageConstants.WAREHOUSE_NAME,
                AISLE2_LEVEL3,AISLE2_DEVICE)).thenReturn(locationData);

        List<Map> stationDataList = new ArrayList<>();
        stationDataList.add(new HashMap());
        when(stationServer.getStationByDeviceList(AISLE2_DEVICE)).thenThrow(new DBException());

        assertThrows(LocationSearchingFailureException.class, () -> {
            finder.find(expectedReceiptMessageData);
        });

        verify(expectedReceiptMessageData, times(1)).isValid();
        verify(poServer, times(1)).getPoHeaderRecord(expectedReceiptMessageData.getOrderId());
        verify(poServer, times(1)).getPurchaseOrderLine(expectedReceiptMessageData.getOrderId(), null, null);
        verify(purchaseOrderLineData, times(1)).dataToSKDCData(anyMap());
        verify(tableJoin, times(1)).getNumberOfLoadPerDeviceId(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(),
                ConversionUtil.convertDateStringToDate(expectedReceiptMessageData.getFlightScheduledDateTime()));
        verify(tableJoin, times(1)).getNumberOfLoadPerLevel(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(), AISLE2_DEVICE);
        verify(locationServer, times(1))
                .reserveUnoccupiedLocationOfLevelIfAvailable(EBSHostMessageConstants.WAREHOUSE_NAME, AISLE2_LEVEL3, AISLE2_DEVICE);
        verify(stationServer, times(1)).getStationByDeviceList(AISLE2_DEVICE);
    }

    @Test
    void shouldThrowALoadCreationFailureExceptionWhenLoadCreationIsFailed()
            throws DBException, NoRemainingEmptyLocationException, LocationSearchingFailureException,
            InvalidExpectedReceiptException, LoadCreationOrUpdateFailureException {
        when(expectedReceiptMessageData.isValid()).thenReturn(true);
        when(expectedReceiptMessageData.getOrderId()).thenReturn(ORDER_ID);
        when(expectedReceiptMessageData.getLoadId()).thenReturn(LOAD_ID);
        when(expectedReceiptMessageData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(expectedReceiptMessageData.getFlightScheduledDateTime()).thenReturn(FLIGHT_SCHEDULED_DATETIME);

        when(poServer.getPoHeaderRecord(expectedReceiptMessageData.getOrderId())).thenReturn(purchaseOrderHeaderData);

        List<Map> purchaseOrderLineDataList = new ArrayList<>();
        purchaseOrderLineDataList.add(new HashMap());
        when(poServer.getPurchaseOrderLine(expectedReceiptMessageData.getOrderId(), null, null))
                .thenReturn(purchaseOrderLineDataList);

        doNothing().when(purchaseOrderLineData).dataToSKDCData(anyMap());

        // Only 1 load in aisle 1, so finder should reserve in aisle 2
        Map<String, Integer> currentLoadsPerDevice = new LinkedHashMap<>();
        currentLoadsPerDevice.put(AISLE1_DEVICE, 1);
        currentLoadsPerDevice.put(AISLE2_DEVICE, 0);
        when(tableJoin.getNumberOfLoadPerDeviceId(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(),
                ConversionUtil.convertDateStringToDate(expectedReceiptMessageData.getFlightScheduledDateTime())))
                        .thenReturn(currentLoadsPerDevice);

        // No load in both banks, so 1 location of bank 3 should be reserved
        Map<String, Integer> currentLoadsPerBank = new LinkedHashMap<>();
        currentLoadsPerBank.put(AISLE2_LEVEL3, 0);
        currentLoadsPerBank.put(AISLE2_LEVEL4, 0);
        when(tableJoin.getNumberOfLoadPerLevel(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(), AISLE2_DEVICE)).thenReturn(currentLoadsPerBank);

        when(locationData.getDeviceID()).thenReturn(AISLE2_DEVICE);
        when(locationData.getAddress()).thenReturn(AISLE2_RESERVED_LOCATION);
        when(locationServer.reserveUnoccupiedLocationOfLevelIfAvailable(EBSHostMessageConstants.WAREHOUSE_NAME,
                AISLE2_LEVEL3, AISLE2_DEVICE)).thenReturn(locationData);

        List<Map> stationDataList = new ArrayList<>();
        stationDataList.add(new HashMap());
        when(stationServer.getStationByDeviceList(AISLE2_DEVICE)).thenReturn(stationDataList);

        doNothing().when(stationData).dataToSKDCData(anyMap());
        when(stationData.getStationType()).thenReturn(DBConstants.INPUT);
        when(stationData.getStationName()).thenReturn(AISLE2_STATION_NAME);
        when(stationData.getWarehouse()).thenReturn(EBSHostMessageConstants.WAREHOUSE_NAME);
        when(stationData.getContainerType()).thenReturn(CONTAINER_TYPE);
        when(stationData.getDeviceID()).thenReturn(AISLE2_DEVICE);

        when(locationData.getAddress()).thenReturn(AISLE2_RESERVED_LOCATION);

        when(expectedReceiptMessageData.getFinalSortLocation()).thenReturn(FINAL_SORT_LOCATION);

        when(loadServer.addExpectedLoad(loadData)).thenReturn(false);

        assertThrows(LoadCreationOrUpdateFailureException.class, () -> {
            finder.find(expectedReceiptMessageData);
        });

        verify(expectedReceiptMessageData, times(1)).isValid();
        verify(poServer, times(1)).getPoHeaderRecord(expectedReceiptMessageData.getOrderId());
        verify(poServer, times(1)).getPurchaseOrderLine(expectedReceiptMessageData.getOrderId(), null, null);
        verify(purchaseOrderLineData, times(1)).dataToSKDCData(anyMap());
        verify(tableJoin, times(1)).getNumberOfLoadPerDeviceId(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(),
                ConversionUtil.convertDateStringToDate(expectedReceiptMessageData.getFlightScheduledDateTime()));
        verify(tableJoin, times(1)).getNumberOfLoadPerLevel(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(), AISLE2_DEVICE);
        verify(locationServer, times(1))
                .reserveUnoccupiedLocationOfLevelIfAvailable(EBSHostMessageConstants.WAREHOUSE_NAME, AISLE2_LEVEL3, AISLE2_DEVICE);
        verify(stationServer, times(1)).getStationByDeviceList(AISLE2_DEVICE);
        verify(stationData, times(1)).dataToSKDCData(anyMap());
        verify(loadData, times(1)).setParentLoadID(LOAD_ID);
        verify(loadData, times(1)).setLoadID(LOAD_ID);
        verify(loadData, times(1)).setWarehouse(EBSHostMessageConstants.WAREHOUSE_NAME);
        verify(loadData, times(1)).setAddress(AISLE2_RESERVED_LOCATION);
        verify(loadData, times(1)).setContainerType(CONTAINER_TYPE);
        verify(loadData, times(1)).setDeviceID(AISLE2_DEVICE);
        verify(loadData, times(1)).setAmountFull(DBConstants.EMPTY);
        verify(loadData, times(1)).setLoadMoveStatus(DBConstants.ARRIVEPENDING);
        verify(loadData, times(1)).setFinalSortLocationID(FINAL_SORT_LOCATION);
        verify(loadServer, times(1)).addExpectedLoad(loadData);
    }

    @Test
    void shouldThrowALocationSearchingFailureExceptionWhenReservingUnoccupiedLocationInBankIsFailed()
            throws DBException, NoRemainingEmptyLocationException, LocationSearchingFailureException,
            InvalidExpectedReceiptException, LoadCreationOrUpdateFailureException {
        when(expectedReceiptMessageData.isValid()).thenReturn(true);
        when(expectedReceiptMessageData.getOrderId()).thenReturn(ORDER_ID);
        when(expectedReceiptMessageData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(expectedReceiptMessageData.getFlightScheduledDateTime()).thenReturn(FLIGHT_SCHEDULED_DATETIME);

        when(poServer.getPoHeaderRecord(expectedReceiptMessageData.getOrderId())).thenReturn(purchaseOrderHeaderData);

        List<Map> purchaseOrderLineDataList = new ArrayList<>();
        purchaseOrderLineDataList.add(new HashMap());
        when(poServer.getPurchaseOrderLine(expectedReceiptMessageData.getOrderId(), null, null))
                .thenReturn(purchaseOrderLineDataList);

        doNothing().when(purchaseOrderLineData).dataToSKDCData(anyMap());

        // Only 1 load in aisle 1, so finder should reserve in aisle 2
        Map<String, Integer> currentLoadsPerDevice = new LinkedHashMap<>();
        currentLoadsPerDevice.put(AISLE1_DEVICE, 1);
        currentLoadsPerDevice.put(AISLE2_DEVICE, 0);
        when(tableJoin.getNumberOfLoadPerDeviceId(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(),
                ConversionUtil.convertDateStringToDate(expectedReceiptMessageData.getFlightScheduledDateTime())))
                        .thenReturn(currentLoadsPerDevice);

        // No load in both banks, so 1 location of bank 3 should be reserved
        Map<String, Integer> currentLoadsPerBank = new LinkedHashMap<>();
        currentLoadsPerBank.put(AISLE2_LEVEL3, 0);
        currentLoadsPerBank.put(AISLE2_LEVEL4, 0);
        when(tableJoin.getNumberOfLoadPerLevel(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(), AISLE2_DEVICE)).thenReturn(currentLoadsPerBank);

        when(locationServer.reserveUnoccupiedLocationOfLevelIfAvailable(EBSHostMessageConstants.WAREHOUSE_NAME,
                AISLE2_LEVEL3, AISLE2_DEVICE)).thenThrow(new DBException());

        assertThrows(LocationSearchingFailureException.class, () -> {
            finder.find(expectedReceiptMessageData);
        });

        verify(expectedReceiptMessageData, times(1)).isValid();
        verify(poServer, times(1)).getPoHeaderRecord(expectedReceiptMessageData.getOrderId());
        verify(poServer, times(1)).getPurchaseOrderLine(expectedReceiptMessageData.getOrderId(), null, null);
        verify(purchaseOrderLineData, times(1)).dataToSKDCData(anyMap());
        verify(tableJoin, times(1)).getNumberOfLoadPerDeviceId(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(),
                ConversionUtil.convertDateStringToDate(expectedReceiptMessageData.getFlightScheduledDateTime()));
        verify(tableJoin, times(1)).getNumberOfLoadPerLevel(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(), AISLE2_DEVICE);
        verify(locationServer, times(1))
                .reserveUnoccupiedLocationOfLevelIfAvailable(EBSHostMessageConstants.WAREHOUSE_NAME, AISLE2_LEVEL3, AISLE2_DEVICE);
    }

    @Test
    void shouldReserveALocationInTheFirstAisleWhenNoLoadIsStored()
            throws DBException, NoRemainingEmptyLocationException, LocationSearchingFailureException,
            InvalidExpectedReceiptException, LoadCreationOrUpdateFailureException, LoadSearchingFailureException,
            AlreadyStoredLoadException, StationSearchingFailureException, LocationReservationFailureException,
            POCreationFailureException {
        when(expectedReceiptMessageData.isValid()).thenReturn(true);
        when(expectedReceiptMessageData.getOrderId()).thenReturn(ORDER_ID);
        when(expectedReceiptMessageData.getLoadId()).thenReturn(LOAD_ID);
        when(expectedReceiptMessageData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(expectedReceiptMessageData.getFlightScheduledDateTime()).thenReturn(FLIGHT_SCHEDULED_DATETIME);

        when(poServer.getPoHeaderRecord(expectedReceiptMessageData.getOrderId())).thenReturn(purchaseOrderHeaderData);

        List<Map> purchaseOrderLineDataList = new ArrayList<>();
        purchaseOrderLineDataList.add(new HashMap());
        when(poServer.getPurchaseOrderLine(expectedReceiptMessageData.getOrderId(), null, null))
                .thenReturn(purchaseOrderLineDataList);

        doNothing().when(purchaseOrderLineData).dataToSKDCData(anyMap());

        // No load, so finder should reserve in aisle 1
        Map<String, Integer> currentLoadsPerDevice = new LinkedHashMap<>();
        currentLoadsPerDevice.put(AISLE1_DEVICE, 0);
        currentLoadsPerDevice.put(AISLE2_DEVICE, 0);
        when(tableJoin.getNumberOfLoadPerDeviceId(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(),
                ConversionUtil.convertDateStringToDate(expectedReceiptMessageData.getFlightScheduledDateTime())))
                        .thenReturn(currentLoadsPerDevice);

        // No load in both banks, so 1 location of bank 1 should be reserved
        Map<String, Integer> currentLoadsPerBank = new LinkedHashMap<>();
        currentLoadsPerBank.put(AISLE1_LEVEL1, 0);
        currentLoadsPerBank.put(AISLE1_LEVEL2, 0);
        when(tableJoin.getNumberOfLoadPerLevel(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(), AISLE1_DEVICE)).thenReturn(currentLoadsPerBank);

        when(locationData.getDeviceID()).thenReturn(AISLE1_DEVICE);
        when(locationData.getAddress()).thenReturn(AISLE1_RESERVED_LOCATION);
        when(locationServer.reserveUnoccupiedLocationOfLevelIfAvailable(EBSHostMessageConstants.WAREHOUSE_NAME,
                AISLE1_LEVEL1, AISLE1_DEVICE)).thenReturn(locationData);

        List<Map> stationDataList = new ArrayList<>();
        stationDataList.add(new HashMap());
        when(stationServer.getStationByDeviceList(AISLE1_DEVICE)).thenReturn(stationDataList);

        doNothing().when(stationData).dataToSKDCData(anyMap());
        when(stationData.getStationType()).thenReturn(DBConstants.INPUT);
        when(stationData.getStationName()).thenReturn(AISLE1_STATION_NAME);
        when(stationData.getWarehouse()).thenReturn(EBSHostMessageConstants.WAREHOUSE_NAME);
        when(stationData.getContainerType()).thenReturn(CONTAINER_TYPE);
        when(stationData.getDeviceID()).thenReturn(AISLE1_DEVICE);

        when(locationData.getAddress()).thenReturn(AISLE1_RESERVED_LOCATION);

        when(expectedReceiptMessageData.getFinalSortLocation()).thenReturn(FINAL_SORT_LOCATION);

        when(loadServer.addExpectedLoad(loadData)).thenReturn(true);

        Date dExpirationDate = ConversionUtil.convertDateStringToDate(EXPIRATION_DATETIME);
        doNothing().when(inventoryServer).addNewItemToLoadForLot(expectedReceiptMessageData, purchaseOrderLineData);

        assertEquals(AISLE1_STATION_NAME, finder.find(expectedReceiptMessageData));

        verify(expectedReceiptMessageData, times(1)).isValid();
        verify(poServer, times(1)).getPoHeaderRecord(expectedReceiptMessageData.getOrderId());
        verify(poServer, times(1)).getPurchaseOrderLine(expectedReceiptMessageData.getOrderId(), null, null);
        verify(purchaseOrderLineData, times(1)).dataToSKDCData(anyMap());
        verify(tableJoin, times(1)).getNumberOfLoadPerDeviceId(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(),
                ConversionUtil.convertDateStringToDate(expectedReceiptMessageData.getFlightScheduledDateTime()));
        verify(tableJoin, times(1)).getNumberOfLoadPerLevel(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(), AISLE1_DEVICE);
        verify(locationServer, times(1))
                .reserveUnoccupiedLocationOfLevelIfAvailable(EBSHostMessageConstants.WAREHOUSE_NAME, AISLE1_LEVEL1, AISLE1_DEVICE);
        verify(stationServer, times(1)).getStationByDeviceList(AISLE1_DEVICE);
        verify(stationData, times(1)).dataToSKDCData(anyMap());
        verify(loadData, times(1)).setParentLoadID(LOAD_ID);
        verify(loadData, times(1)).setLoadID(LOAD_ID);
        verify(loadData, times(1)).setWarehouse(EBSHostMessageConstants.WAREHOUSE_NAME);
        verify(loadData, times(1)).setAddress(AISLE1_RESERVED_LOCATION);
        verify(loadData, times(1)).setContainerType(CONTAINER_TYPE);
        verify(loadData, times(1)).setDeviceID(AISLE1_DEVICE);
        verify(loadData, times(1)).setAmountFull(DBConstants.EMPTY);
        verify(loadData, times(1)).setLoadMoveStatus(DBConstants.ARRIVEPENDING);
        verify(loadData, times(1)).setFinalSortLocationID(FINAL_SORT_LOCATION);
        verify(loadServer, times(1)).addExpectedLoad(loadData);
        verify(inventoryServer, times(1)).addNewItemToLoadForLot(expectedReceiptMessageData, purchaseOrderLineData);
    }

    @Test
    void shouldReserveALocationInTheAisleThatHasTheLeastNumberOfLoads()
            throws DBException, NoRemainingEmptyLocationException, LocationSearchingFailureException,
            InvalidExpectedReceiptException, LoadCreationOrUpdateFailureException, LoadSearchingFailureException,
            AlreadyStoredLoadException, StationSearchingFailureException, LocationReservationFailureException,
            POCreationFailureException {
        when(expectedReceiptMessageData.isValid()).thenReturn(true);
        when(expectedReceiptMessageData.getOrderId()).thenReturn(ORDER_ID);
        when(expectedReceiptMessageData.getLoadId()).thenReturn(LOAD_ID);
        when(expectedReceiptMessageData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(expectedReceiptMessageData.getFlightScheduledDateTime()).thenReturn(FLIGHT_SCHEDULED_DATETIME);

        when(poServer.getPoHeaderRecord(expectedReceiptMessageData.getOrderId())).thenReturn(purchaseOrderHeaderData);

        List<Map> purchaseOrderLineDataList = new ArrayList<>();
        purchaseOrderLineDataList.add(new HashMap());
        when(poServer.getPurchaseOrderLine(expectedReceiptMessageData.getOrderId(), null, null))
                .thenReturn(purchaseOrderLineDataList);

        doNothing().when(purchaseOrderLineData).dataToSKDCData(anyMap());

        // Only 1 load in aisle 1, so finder should reserve in aisle 2
        Map<String, Integer> currentLoadsPerDevice = new LinkedHashMap<>();
        currentLoadsPerDevice.put(AISLE1_DEVICE, 1);
        currentLoadsPerDevice.put(AISLE2_DEVICE, 0);
        when(tableJoin.getNumberOfLoadPerDeviceId(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(),
                ConversionUtil.convertDateStringToDate(expectedReceiptMessageData.getFlightScheduledDateTime())))
                        .thenReturn(currentLoadsPerDevice);

        // No load in both banks, so 1 location of bank 3 should be reserved
        Map<String, Integer> currentLoadsPerBank = new LinkedHashMap<>();
        currentLoadsPerBank.put(AISLE2_LEVEL3, 0);
        currentLoadsPerBank.put(AISLE2_LEVEL4, 0);
        when(tableJoin.getNumberOfLoadPerLevel(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(), AISLE2_DEVICE)).thenReturn(currentLoadsPerBank);

        when(locationData.getDeviceID()).thenReturn(AISLE2_DEVICE);
        when(locationData.getAddress()).thenReturn(AISLE2_RESERVED_LOCATION);
        when(locationServer.reserveUnoccupiedLocationOfLevelIfAvailable(EBSHostMessageConstants.WAREHOUSE_NAME,
                AISLE2_LEVEL3, AISLE2_DEVICE)).thenReturn(locationData);

        List<Map> stationDataList = new ArrayList<>();
        stationDataList.add(new HashMap());
        when(stationServer.getStationByDeviceList(AISLE2_DEVICE)).thenReturn(stationDataList);

        doNothing().when(stationData).dataToSKDCData(anyMap());
        when(stationData.getStationType()).thenReturn(DBConstants.INPUT);
        when(stationData.getStationName()).thenReturn(AISLE2_STATION_NAME);
        when(stationData.getWarehouse()).thenReturn(EBSHostMessageConstants.WAREHOUSE_NAME);
        when(stationData.getContainerType()).thenReturn(CONTAINER_TYPE);
        when(stationData.getDeviceID()).thenReturn(AISLE2_DEVICE);

        when(locationData.getAddress()).thenReturn(AISLE2_RESERVED_LOCATION);

        when(expectedReceiptMessageData.getFinalSortLocation()).thenReturn(FINAL_SORT_LOCATION);

        when(loadServer.addExpectedLoad(loadData)).thenReturn(true);

        doNothing().when(inventoryServer).addNewItemToLoadForLot(expectedReceiptMessageData, purchaseOrderLineData);

        assertEquals(AISLE2_STATION_NAME, finder.find(expectedReceiptMessageData));

        verify(expectedReceiptMessageData, times(1)).isValid();
        verify(poServer, times(1)).getPoHeaderRecord(expectedReceiptMessageData.getOrderId());
        verify(poServer, times(1)).getPurchaseOrderLine(expectedReceiptMessageData.getOrderId(), null, null);
        verify(purchaseOrderLineData, times(1)).dataToSKDCData(anyMap());
        verify(tableJoin, times(1)).getNumberOfLoadPerDeviceId(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(),
                ConversionUtil.convertDateStringToDate(expectedReceiptMessageData.getFlightScheduledDateTime()));
        verify(tableJoin, times(1)).getNumberOfLoadPerLevel(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(), AISLE2_DEVICE);
        verify(locationServer, times(1))
                .reserveUnoccupiedLocationOfLevelIfAvailable(EBSHostMessageConstants.WAREHOUSE_NAME, AISLE2_LEVEL3, AISLE2_DEVICE);
        verify(stationServer, times(1)).getStationByDeviceList(AISLE2_DEVICE);
        verify(stationData, times(1)).dataToSKDCData(anyMap());
        verify(loadData, times(1)).setParentLoadID(LOAD_ID);
        verify(loadData, times(1)).setLoadID(LOAD_ID);
        verify(loadData, times(1)).setWarehouse(EBSHostMessageConstants.WAREHOUSE_NAME);
        verify(loadData, times(1)).setAddress(AISLE2_RESERVED_LOCATION);
        verify(loadData, times(1)).setContainerType(CONTAINER_TYPE);
        verify(loadData, times(1)).setDeviceID(AISLE2_DEVICE);
        verify(loadData, times(1)).setAmountFull(DBConstants.EMPTY);
        verify(loadData, times(1)).setLoadMoveStatus(DBConstants.ARRIVEPENDING);
        verify(loadData, times(1)).setFinalSortLocationID(FINAL_SORT_LOCATION);
        verify(loadServer, times(1)).addExpectedLoad(loadData);
        verify(inventoryServer, times(1)).addNewItemToLoadForLot(expectedReceiptMessageData, purchaseOrderLineData);
    }

    private static Stream<Arguments> unavailableLocationStatusList() {
        return Stream.of(
                Arguments.of("Location is prohibited", DBConstants.LCPROHIBIT),
                Arguments.of("Location is unavailable to use", DBConstants.LCUNAVAIL));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("unavailableLocationStatusList")
    void shouldFindANewEmptyLocationWhenExistingLoadHasProhibitedOrUnavailableLocation(String test,
            int unavailableLocationStatus) throws DBException, NoRemainingEmptyLocationException,
            LocationSearchingFailureException, InvalidExpectedReceiptException, LoadCreationOrUpdateFailureException,
            LoadSearchingFailureException, AlreadyStoredLoadException, StationSearchingFailureException,
            LocationReservationFailureException, POCreationFailureException, ParseException {
        when(expectedReceiptMessageData.isValid()).thenReturn(true);
        when(expectedReceiptMessageData.getLoadId()).thenReturn(LOAD_ID);
        when(expectedReceiptMessageData.getLot()).thenReturn(FLIGHT_NUMBER);
        when(expectedReceiptMessageData.getLineId()).thenReturn(BAG_ID);
        when(expectedReceiptMessageData.getFlightScheduledDateTime()).thenReturn(FLIGHT_SCHEDULED_DATETIME);

        List<Map> loadLineItems = new ArrayList<>();
        loadLineItems.add(new HashMap());
        doNothing().when(loadLineItemData).dataToSKDCData(anyMap());
        when(inventoryServer.getLoadLineItemDataListByLoadID(eq(LOAD_ID))).thenReturn(loadLineItems);

        when(loadData.getAddress()).thenReturn(AISLE1_RESERVED_LOCATION);
        when(loadServer.getLoad(eq(LOAD_ID))).thenReturn(loadData);

        when(poServer.getPoHeaderRecord(expectedReceiptMessageData.getOrderId())).thenReturn(null);
        doNothing().when(poServer).addPOExpectedReceipt(expectedReceiptMessageData);

        List<Map> purchaseOrderLineDataList = new ArrayList<>();
        purchaseOrderLineDataList.add(new HashMap());
        when(poServer.getPurchaseOrderLine(expectedReceiptMessageData.getOrderId(), null, null))
                .thenReturn(purchaseOrderLineDataList);

        doNothing().when(purchaseOrderLineData).dataToSKDCData(anyMap());

        // No load, so finder should reserve in aisle 1
        Map<String, Integer> currentLoadsPerDevice = new LinkedHashMap<>();
        currentLoadsPerDevice.put(AISLE1_DEVICE, 0);
        currentLoadsPerDevice.put(AISLE2_DEVICE, 0);
        when(tableJoin.getNumberOfLoadPerDeviceId(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(),
                ConversionUtil.convertDateStringToDate(expectedReceiptMessageData.getFlightScheduledDateTime())))
                        .thenReturn(currentLoadsPerDevice);

        // No load in both banks, so 1 location of bank 1 should be reserved
        Map<String, Integer> currentLoadsPerBank = new LinkedHashMap<>();
        currentLoadsPerBank.put(AISLE1_LEVEL1, 0);
        currentLoadsPerBank.put(AISLE1_LEVEL2, 0);
        when(tableJoin.getNumberOfLoadPerLevel(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(), AISLE1_DEVICE)).thenReturn(currentLoadsPerBank);

        when(locationData.getDeviceID()).thenReturn(AISLE1_DEVICE);
        when(locationData.getAddress()).thenReturn(AISLE1_RESERVED_LOCATION);
        when(locationServer.reserveUnoccupiedLocationOfLevelIfAvailable(EBSHostMessageConstants.WAREHOUSE_NAME,
                AISLE1_LEVEL1,AISLE1_DEVICE)).thenReturn(locationData);

        List<Map> stationDataList = new ArrayList<>();
        stationDataList.add(new HashMap());
        when(stationServer.getStationByDeviceList(AISLE1_DEVICE)).thenReturn(stationDataList);

        doNothing().when(stationData).dataToSKDCData(anyMap());
        when(stationData.getStationType()).thenReturn(DBConstants.INPUT);
        when(stationData.getStationName()).thenReturn(AISLE1_STATION_NAME);

        when(locationData.getAddress()).thenReturn(AISLE1_RESERVED_LOCATION);

        doNothing().when(loadServer).updateReservedLocation(any(LoadData.class), eq(AISLE1_RESERVED_LOCATION));

        assertEquals(AISLE1_STATION_NAME, finder.find(expectedReceiptMessageData));

        verify(expectedReceiptMessageData, times(1)).isValid();
        verify(inventoryServer, times(1)).getLoadLineItemDataListByLoadID(eq(LOAD_ID));
        verify(loadServer, times(1)).getLoad(eq(LOAD_ID));
        verify(locationServer, times(1)).getLocationRecord(eq(EBSHostMessageConstants.WAREHOUSE_NAME),
                eq(AISLE1_RESERVED_LOCATION));
        verify(poServer, times(1)).getPoHeaderRecord(expectedReceiptMessageData.getOrderId());
        verify(poServer, times(1)).getPurchaseOrderLine(expectedReceiptMessageData.getOrderId(), null, null);
        verify(purchaseOrderLineData, times(1)).dataToSKDCData(anyMap());
        verify(tableJoin, times(1)).getNumberOfLoadPerDeviceId(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(),
                ConversionUtil.convertDateStringToDate(expectedReceiptMessageData.getFlightScheduledDateTime()));
        verify(tableJoin, times(1)).getNumberOfLoadPerLevel(EBSHostMessageConstants.WAREHOUSE_NAME,
                expectedReceiptMessageData.getLot(), AISLE1_DEVICE);
        verify(locationServer, times(1))
                .reserveUnoccupiedLocationOfLevelIfAvailable(EBSHostMessageConstants.WAREHOUSE_NAME, AISLE1_LEVEL1, AISLE1_DEVICE);
        verify(stationServer, times(1)).getStationByDeviceList(AISLE1_DEVICE);
        verify(stationData, times(1)).dataToSKDCData(anyMap());
        verify(loadServer, times(1)).updateReservedLocation(any(LoadData.class), eq(AISLE1_RESERVED_LOCATION));
    }
}
