package com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSInventoryServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLoadServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLocationServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSPoReceivingServer;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.ConveyorTableJoin;
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

@ExtendWith(MockitoExtension.class)
public class ConveyorBasedEmptyLocationFinderImplTest {

    private static final String LOAD_ID = "1001";
    private static final String ORDER_ID = "1234";
    private static final String FLIGHT_SCHEDULED_DATETIME = "20221102130000";
    private static final String FLIGHT_NUMBER = "FL100";
    private static final String BAG_ID = "BAG1000";
    private static final String FINAL_SORT_LOCATION = "WHN-1";
    private static final String EXPIRATION_DATETIME = "20221102190000";
    private static final String ITEM_NAME = "ItemName";
    private static final String CONTAINER_TYPE = "ContainerType";
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
    ConveyorTableJoin tableJoin;

    ConveyorBasedEmptyLocationFinderImpl finder;
    
    private static final String AISLE1_RESERVED_LOCATION = "6111";
    private static final String AISLE2_RESERVED_LOCATION = "6211";
    private static final String WHS = "WHS";
    private static final String AISLE1_DEVICE = "9001";
    private static final String AISLE2_DEVICE = "9002";
    private static final Integer RELEASE_WINDOW_TIME_IN_MIN = 120;
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
        mockedFactory.when(() -> Factory.create(ConveyorTableJoin.class)).thenReturn(tableJoin);

        mockedDBHelper.when(() -> DBHelper.getStringField(anyMap(), eq(LoadLineItemData.LOT_NAME)))
                .thenReturn(FLIGHT_NUMBER);
        mockedDBHelper.when(() -> DBHelper.getStringField(anyMap(), eq(LoadLineItemData.LINEID_NAME)))
                .thenReturn(BAG_ID);
        

        finder = new ConveyorBasedEmptyLocationFinderImpl();
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
	void shouldFindALocationInCurrentWarehouseAndSameFlight() throws DBException, NoRemainingEmptyLocationException,
			LocationSearchingFailureException, InvalidExpectedReceiptException, LoadCreationOrUpdateFailureException,
			LoadSearchingFailureException, AlreadyStoredLoadException, StationSearchingFailureException,
			LocationReservationFailureException, POCreationFailureException {
		when(expectedReceiptMessageData.getFinalSortLocation()).thenReturn(FINAL_SORT_LOCATION);
		when(tableJoin.findWarehouseByFinalSortLocation(FINAL_SORT_LOCATION)).thenReturn(WHS);
		when(tableJoin.findEmptyLocationForTheSameFlight(expectedReceiptMessageData, WHS))
				.thenReturn(AISLE1_RESERVED_LOCATION);

		String location = finder.findAnEmptyLocation(expectedReceiptMessageData);
		assertEquals(AISLE1_RESERVED_LOCATION, location);

	}
	
	@Test
	void shouldThrowDBExceptionWhileFetchWarehouse() throws DBException, NoRemainingEmptyLocationException,
			LocationSearchingFailureException, InvalidExpectedReceiptException, LoadCreationOrUpdateFailureException,
			LoadSearchingFailureException, AlreadyStoredLoadException, StationSearchingFailureException,
			LocationReservationFailureException, POCreationFailureException {
		when(expectedReceiptMessageData.getFinalSortLocation()).thenReturn(FINAL_SORT_LOCATION);
		when(tableJoin.findWarehouseByFinalSortLocation(FINAL_SORT_LOCATION)).thenReturn(WHS);
		when(tableJoin.findEmptyLocationForTheSameFlight(expectedReceiptMessageData, WHS)).thenThrow(new DBException());

		String location = finder.findAnEmptyLocation(expectedReceiptMessageData);
		assertEquals(null, location);

	}
	
	@Test
	void shouldFindALocationInOtherWarehouseAndSameFlight() throws DBException, NoRemainingEmptyLocationException,
			LocationSearchingFailureException, InvalidExpectedReceiptException, LoadCreationOrUpdateFailureException,
			LoadSearchingFailureException, AlreadyStoredLoadException, StationSearchingFailureException,
			LocationReservationFailureException, POCreationFailureException {
		when(expectedReceiptMessageData.getFinalSortLocation()).thenReturn(FINAL_SORT_LOCATION);
		when(tableJoin.findWarehouseByFinalSortLocation(FINAL_SORT_LOCATION)).thenReturn(null);
		when(tableJoin.findEmptyLocationForTheSameFlight(expectedReceiptMessageData, null))
				.thenReturn(AISLE1_RESERVED_LOCATION);

		String location = finder.findAnEmptyLocation(expectedReceiptMessageData);
		assertEquals(AISLE1_RESERVED_LOCATION, location);

	}

	@Test
	void shouldFindALocationInCurrentWarehouseAndOverLappingWindow() throws DBException,
			NoRemainingEmptyLocationException, LocationSearchingFailureException, InvalidExpectedReceiptException,
			LoadCreationOrUpdateFailureException, LoadSearchingFailureException, AlreadyStoredLoadException,
			StationSearchingFailureException, LocationReservationFailureException, POCreationFailureException {
		when(expectedReceiptMessageData.getFinalSortLocation()).thenReturn(FINAL_SORT_LOCATION);
		when(tableJoin.findWarehouseByFinalSortLocation(FINAL_SORT_LOCATION)).thenReturn(WHS);
		when(tableJoin.getReleaseWindowPeriodInMin()).thenReturn(RELEASE_WINDOW_TIME_IN_MIN);
		when(tableJoin.findEmptyLocationForTheSameFlight(expectedReceiptMessageData, WHS)).thenReturn(null);
		when(tableJoin.findEmptyLocationInOverlappingReleaseWindowBy(expectedReceiptMessageData,
				RELEASE_WINDOW_TIME_IN_MIN, WHS)).thenReturn(AISLE1_RESERVED_LOCATION);
		
		String location = finder.findAnEmptyLocation(expectedReceiptMessageData);
		assertEquals(AISLE1_RESERVED_LOCATION, location);
	}
	
	@Test
	void shouldFindALocationInOtherWarehouseAndOverLappingWindow() throws DBException,
			NoRemainingEmptyLocationException, LocationSearchingFailureException, InvalidExpectedReceiptException,
			LoadCreationOrUpdateFailureException, LoadSearchingFailureException, AlreadyStoredLoadException,
			StationSearchingFailureException, LocationReservationFailureException, POCreationFailureException {
		when(expectedReceiptMessageData.getFinalSortLocation()).thenReturn(FINAL_SORT_LOCATION);
		when(tableJoin.findWarehouseByFinalSortLocation(FINAL_SORT_LOCATION)).thenReturn(null);
		when(tableJoin.getReleaseWindowPeriodInMin()).thenReturn(RELEASE_WINDOW_TIME_IN_MIN);
		when(tableJoin.findEmptyLocationForTheSameFlight(expectedReceiptMessageData, null)).thenReturn(null);
		when(tableJoin.findEmptyLocationInOverlappingReleaseWindowBy(expectedReceiptMessageData,
				RELEASE_WINDOW_TIME_IN_MIN, null)).thenReturn(AISLE1_RESERVED_LOCATION);
		
		String location = finder.findAnEmptyLocation(expectedReceiptMessageData);
		assertEquals(AISLE1_RESERVED_LOCATION, location);
	}
	
	@Test
	void shouldFindALocationInCurrentWarehouseAndNoOverLappingWindow() throws DBException,
			NoRemainingEmptyLocationException, LocationSearchingFailureException, InvalidExpectedReceiptException,
			LoadCreationOrUpdateFailureException, LoadSearchingFailureException, AlreadyStoredLoadException,
			StationSearchingFailureException, LocationReservationFailureException, POCreationFailureException {
		when(expectedReceiptMessageData.getFinalSortLocation()).thenReturn(FINAL_SORT_LOCATION);
		when(tableJoin.findWarehouseByFinalSortLocation(FINAL_SORT_LOCATION)).thenReturn(WHS);
		when(tableJoin.getReleaseWindowPeriodInMin()).thenReturn(RELEASE_WINDOW_TIME_IN_MIN);
		when(tableJoin.findEmptyLocationInOverlappingReleaseWindowBy(expectedReceiptMessageData,
				RELEASE_WINDOW_TIME_IN_MIN, WHS)).thenReturn(null);
		when(tableJoin.findEmptyLaneBy(expectedReceiptMessageData, WHS)).thenReturn(AISLE1_RESERVED_LOCATION);

		String location = finder.findAnEmptyLocation(expectedReceiptMessageData);
		assertEquals(AISLE1_RESERVED_LOCATION, location);
	}
	
	@Test
	void shouldFindALocationInOtherWarehouseAndNoOverLappingWindow() throws DBException,
			NoRemainingEmptyLocationException, LocationSearchingFailureException, InvalidExpectedReceiptException,
			LoadCreationOrUpdateFailureException, LoadSearchingFailureException, AlreadyStoredLoadException,
			StationSearchingFailureException, LocationReservationFailureException, POCreationFailureException {
		when(expectedReceiptMessageData.getFinalSortLocation()).thenReturn(FINAL_SORT_LOCATION);
		when(tableJoin.findWarehouseByFinalSortLocation(FINAL_SORT_LOCATION)).thenReturn(null);
		when(tableJoin.getReleaseWindowPeriodInMin()).thenReturn(RELEASE_WINDOW_TIME_IN_MIN);
		when(tableJoin.findEmptyLocationInOverlappingReleaseWindowBy(expectedReceiptMessageData,
				RELEASE_WINDOW_TIME_IN_MIN, null)).thenReturn(null);
		when(tableJoin.findEmptyLaneBy(expectedReceiptMessageData, null)).thenReturn(AISLE1_RESERVED_LOCATION);

		String location = finder.findAnEmptyLocation(expectedReceiptMessageData);
		assertEquals(AISLE1_RESERVED_LOCATION, location);
	}
	
	@Test
	void shouldFindALocationInCurrentWarehouseAndStoreOverLappingWindow() throws DBException,
			NoRemainingEmptyLocationException, LocationSearchingFailureException, InvalidExpectedReceiptException,
			LoadCreationOrUpdateFailureException, LoadSearchingFailureException, AlreadyStoredLoadException,
			StationSearchingFailureException, LocationReservationFailureException, POCreationFailureException {
		when(expectedReceiptMessageData.getFinalSortLocation()).thenReturn(FINAL_SORT_LOCATION);
		when(tableJoin.findWarehouseByFinalSortLocation(FINAL_SORT_LOCATION)).thenReturn(WHS);
		when(tableJoin.getReleaseWindowPeriodInMin()).thenReturn(RELEASE_WINDOW_TIME_IN_MIN);
		when(tableJoin.findEmptyLocationInOverlappingReleaseWindowBy(expectedReceiptMessageData,
				RELEASE_WINDOW_TIME_IN_MIN, WHS)).thenReturn(null);
		when(tableJoin.findEmptyLaneBy(expectedReceiptMessageData, WHS)).thenReturn(null);
		when(tableJoin.shouldStoreNonOverlappingBags()).thenReturn(true);
		when(tableJoin.findEmptyLocationWhichHasClosestReleaseWindowBy(expectedReceiptMessageData, WHS))
		.thenReturn(AISLE1_RESERVED_LOCATION);

		String location = finder.findAnEmptyLocation(expectedReceiptMessageData);
		assertEquals(AISLE1_RESERVED_LOCATION, location);
	}
	
	@Test
	void shouldFindALocationInOtherWarehouseAndStoreOverLappingWindow() throws DBException,
			NoRemainingEmptyLocationException, LocationSearchingFailureException, InvalidExpectedReceiptException,
			LoadCreationOrUpdateFailureException, LoadSearchingFailureException, AlreadyStoredLoadException,
			StationSearchingFailureException, LocationReservationFailureException, POCreationFailureException {
		when(expectedReceiptMessageData.getFinalSortLocation()).thenReturn(FINAL_SORT_LOCATION);
		when(tableJoin.findWarehouseByFinalSortLocation(FINAL_SORT_LOCATION)).thenReturn(null);
		when(tableJoin.getReleaseWindowPeriodInMin()).thenReturn(RELEASE_WINDOW_TIME_IN_MIN);
		when(tableJoin.findEmptyLocationInOverlappingReleaseWindowBy(expectedReceiptMessageData,
				RELEASE_WINDOW_TIME_IN_MIN, null)).thenReturn(null);
		when(tableJoin.findEmptyLaneBy(expectedReceiptMessageData, null)).thenReturn(null);
		when(tableJoin.shouldStoreNonOverlappingBags()).thenReturn(true);
		when(tableJoin.findEmptyLocationWhichHasClosestReleaseWindowBy(expectedReceiptMessageData, null))
		.thenReturn(AISLE1_RESERVED_LOCATION);

		String location = finder.findAnEmptyLocation(expectedReceiptMessageData);
		assertEquals(AISLE1_RESERVED_LOCATION, location);
	}
	
	@Test
	void shouldFindALocationInCurrentWarehouseAndStoreOverLappingWindowInAnyLane() throws DBException,
			NoRemainingEmptyLocationException, LocationSearchingFailureException, InvalidExpectedReceiptException,
			LoadCreationOrUpdateFailureException, LoadSearchingFailureException, AlreadyStoredLoadException,
			StationSearchingFailureException, LocationReservationFailureException, POCreationFailureException {
		when(expectedReceiptMessageData.getFinalSortLocation()).thenReturn(FINAL_SORT_LOCATION);
		when(tableJoin.findWarehouseByFinalSortLocation(FINAL_SORT_LOCATION)).thenReturn(WHS);
		when(tableJoin.getReleaseWindowPeriodInMin()).thenReturn(RELEASE_WINDOW_TIME_IN_MIN);
		when(tableJoin.findEmptyLocationInOverlappingReleaseWindowBy(expectedReceiptMessageData,
				RELEASE_WINDOW_TIME_IN_MIN, WHS)).thenReturn(null);
		when(tableJoin.findEmptyLaneBy(expectedReceiptMessageData, WHS)).thenReturn(null);
		when(tableJoin.shouldStoreNonOverlappingBags()).thenReturn(true);
		when(tableJoin.findEmptyLocationWhichHasClosestReleaseWindowBy(expectedReceiptMessageData, WHS))
		.thenReturn(null);
		
		when(tableJoin.findLocationOnAnyLaneBy(expectedReceiptMessageData, WHS))
		.thenReturn(AISLE1_RESERVED_LOCATION);
		

		String location = finder.findAnEmptyLocation(expectedReceiptMessageData);
		assertEquals(AISLE1_RESERVED_LOCATION, location);
	}
	
	@Test
	void shouldFindALocationInOtherWarehouseAndStoreOverLappingWindowInAnyLane() throws DBException,
			NoRemainingEmptyLocationException, LocationSearchingFailureException, InvalidExpectedReceiptException,
			LoadCreationOrUpdateFailureException, LoadSearchingFailureException, AlreadyStoredLoadException,
			StationSearchingFailureException, LocationReservationFailureException, POCreationFailureException {
		when(expectedReceiptMessageData.getFinalSortLocation()).thenReturn(FINAL_SORT_LOCATION);
		when(tableJoin.findWarehouseByFinalSortLocation(FINAL_SORT_LOCATION)).thenReturn(null);
		when(tableJoin.getReleaseWindowPeriodInMin()).thenReturn(RELEASE_WINDOW_TIME_IN_MIN);
		when(tableJoin.findEmptyLocationInOverlappingReleaseWindowBy(expectedReceiptMessageData,
				RELEASE_WINDOW_TIME_IN_MIN, null)).thenReturn(null);
		when(tableJoin.findEmptyLaneBy(expectedReceiptMessageData, null)).thenReturn(null);
		when(tableJoin.shouldStoreNonOverlappingBags()).thenReturn(true);
		when(tableJoin.findEmptyLocationWhichHasClosestReleaseWindowBy(expectedReceiptMessageData, null))
		.thenReturn(null);
		
		when(tableJoin.findLocationOnAnyLaneBy(expectedReceiptMessageData, null))
		.thenReturn(AISLE1_RESERVED_LOCATION);
		

		String location = finder.findAnEmptyLocation(expectedReceiptMessageData);
		assertEquals(AISLE1_RESERVED_LOCATION, location);
	}
	
	@Test
	void shouldNotFindALocationInAnyWarehouse() throws DBException,
			NoRemainingEmptyLocationException, LocationSearchingFailureException, InvalidExpectedReceiptException,
			LoadCreationOrUpdateFailureException, LoadSearchingFailureException, AlreadyStoredLoadException,
			StationSearchingFailureException, LocationReservationFailureException, POCreationFailureException {
		when(expectedReceiptMessageData.getFinalSortLocation()).thenReturn(FINAL_SORT_LOCATION);
		when(tableJoin.findWarehouseByFinalSortLocation(FINAL_SORT_LOCATION)).thenReturn(null);
		when(tableJoin.getReleaseWindowPeriodInMin()).thenReturn(RELEASE_WINDOW_TIME_IN_MIN);
		when(tableJoin.findEmptyLocationInOverlappingReleaseWindowBy(expectedReceiptMessageData,
				RELEASE_WINDOW_TIME_IN_MIN, null)).thenReturn(null);
		when(tableJoin.findEmptyLaneBy(expectedReceiptMessageData, null)).thenReturn(null);
		when(tableJoin.shouldStoreNonOverlappingBags()).thenReturn(true);
		when(tableJoin.findEmptyLocationWhichHasClosestReleaseWindowBy(expectedReceiptMessageData, null))
		.thenReturn(null);
		
		when(tableJoin.findLocationOnAnyLaneBy(expectedReceiptMessageData, null))
		.thenReturn(null);
		

		String location = finder.findAnEmptyLocation(expectedReceiptMessageData);
		assertEquals(null, location);
	}
	
    /*
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

        when(locationServer.getLocationRecord(eq(WHS), eq(AISLE1_RESERVED_LOCATION)))
                .thenThrow(new DBException());

        assertThrows(LocationSearchingFailureException.class, () -> {
            finder.find(expectedReceiptMessageData);
        });

        verify(expectedReceiptMessageData, times(1)).isValid();
        verify(inventoryServer, times(1)).getLoadLineItemDataListByLoadID(eq(LOAD_ID));
        verify(loadServer, times(1)).getLoad(eq(LOAD_ID));
        verify(locationServer, times(1)).getLocationRecord(eq(WHS),
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
        when(locationServer.getLocationRecord(eq(WHS), eq(AISLE1_RESERVED_LOCATION)))
                .thenReturn(locationData);

        when(stationServer.getStationByDeviceList(eq(AISLE1_DEVICE))).thenThrow(new DBException());

        assertThrows(StationSearchingFailureException.class, () -> {
            finder.find(expectedReceiptMessageData);
        });

        verify(expectedReceiptMessageData, times(1)).isValid();
        verify(inventoryServer, times(1)).getLoadLineItemDataListByLoadID(eq(LOAD_ID));
        verify(loadServer, times(1)).getLoad(eq(LOAD_ID));
        verify(locationServer, times(1)).getLocationRecord(eq(WHS),
                eq(AISLE1_RESERVED_LOCATION));
        verify(stationServer, times(1)).getStationByDeviceList(eq(AISLE1_DEVICE));
    }
*/
}
