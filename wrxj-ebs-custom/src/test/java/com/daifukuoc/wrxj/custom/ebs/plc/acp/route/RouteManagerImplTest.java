package com.daifukuoc.wrxj.custom.ebs.plc.acp.route;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSTableJoin;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;

/**
 * Unit test case class for RouteManagerImpl
 * @author MT
 *
 */
@ExtendWith(MockitoExtension.class)
class RouteManagerImplTest {
	
	private RouteManagerImpl routeManagerImpl;
	
	MockedStatic<Factory> mockedFactory;
	
	@Mock
	EBSTableJoin ebsTJ;
	
	private static final String DEVICE_ID = "9001";
	private static final String LOAD_SADDRESS = "2000200301";
	private static final String LIFTER_AISLE_PICKUP_STATION = "0200199901";
	
	private static final String SHUTTLE_PICKUP_STATION_ADDRESS = "1200100001";
	private static final String LIFT_LAYER_PICKUP_STATION_ADDRESS = "0500299901";
	private static final String LIFTER_AISLE_DEPOSIT_STATION = "0300299901";
	private static final String DESTINATION_ADDRESS_FROM_ROUTE = "XX002YYY01";
	
	@BeforeEach
	public void setUp() {
		mockedFactory = Mockito.mockStatic(Factory.class);
		mockedFactory.when(() -> Factory.create(EBSTableJoin.class)).thenReturn(ebsTJ);
		routeManagerImpl = new RouteManagerImpl();
	}
	
	@AfterEach
    public void tearDown() {
        mockedFactory.close();
    }
	
	@Test
	public void testFindNextDestinationForInboundAndLiftPickupStation() throws RouteManagerFailureException, DBException {
		
		LoadData load = new LoadData();
		load.setAddress(LOAD_SADDRESS);
		load.setLoadMoveStatus(DBConstants.ARRIVEPENDING);
		String currentAddress = LIFTER_AISLE_PICKUP_STATION;
		String nextAddress = routeManagerImpl.findNextDestination(load, currentAddress);
		String moveType = routeManagerImpl.getMoveType();
		assertEquals(SHUTTLE_PICKUP_STATION_ADDRESS, nextAddress);
		assertEquals(String.valueOf(PLCConstants.MoveOrder.MOVE_TYPE.DIRECT), moveType);
		
	}
	
	@Test
	public void testFindNextDestinationForInboundAndShuttlePickupStation() throws RouteManagerFailureException, DBException {
		
		LoadData load = new LoadData();
		load.setAddress(LOAD_SADDRESS);
		load.setLoadMoveStatus(DBConstants.STOREPENDING);
		String currentAddress = SHUTTLE_PICKUP_STATION_ADDRESS;
		String nextAddress = routeManagerImpl.findNextDestination(load, currentAddress);
		String moveType = routeManagerImpl.getMoveType();
		assertEquals(LOAD_SADDRESS, nextAddress);
		assertEquals(String.valueOf(PLCConstants.MoveOrder.MOVE_TYPE.STORAGE), moveType);
	}
	
	@Test
	public void testFindNextDestinationForOutboundAndLoadAddress() throws RouteManagerFailureException, DBException {
		
		LoadData load = new LoadData();
		load.setAddress(LOAD_SADDRESS);
		load.setLoadMoveStatus(DBConstants.NOMOVE);
		load.setDeviceID(DEVICE_ID);
		String currentAddress = LOAD_SADDRESS;
		
		doReturn(LIFT_LAYER_PICKUP_STATION_ADDRESS).when(ebsTJ).getDestinationIdFromRoute(DESTINATION_ADDRESS_FROM_ROUTE);
		String nextAddress = routeManagerImpl.findNextDestination(load, currentAddress);
		String moveType = routeManagerImpl.getMoveType();
		assertEquals(LIFT_LAYER_PICKUP_STATION_ADDRESS, nextAddress);
		assertEquals(String.valueOf(PLCConstants.MoveOrder.MOVE_TYPE.RETRIEVAL), moveType);
		
	}
	
	@Test
	public void testFindNextDestinationForOutboundAndLiftPickupStation() throws RouteManagerFailureException, DBException {
		
		LoadData load = new LoadData();
		load.setAddress(LOAD_SADDRESS);
		load.setDeviceID(DEVICE_ID);
		load.setLoadMoveStatus(DBConstants.RETRIEVEPENDING);
		String currentAddress = LIFT_LAYER_PICKUP_STATION_ADDRESS;
		doReturn(LIFTER_AISLE_DEPOSIT_STATION).when(ebsTJ).getDestinationIdFromRoute(LIFT_LAYER_PICKUP_STATION_ADDRESS);
		String nextAddress = routeManagerImpl.findNextDestination(load, currentAddress);
		String moveType = routeManagerImpl.getMoveType();
		assertEquals(LIFTER_AISLE_DEPOSIT_STATION, nextAddress);
		assertEquals(String.valueOf(PLCConstants.MoveOrder.MOVE_TYPE.DIRECT), moveType);
		
	}
	
}
