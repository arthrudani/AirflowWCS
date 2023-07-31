package com.daifukuoc.wrxj.custom.ebs.plc.commandallocator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.util.Arrays;
import java.util.Date;
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

import com.daifukuamerica.wrxj.dbadapter.data.MoveCommandData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dataserver.ACPMoveCommandServer;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.ACPTableJoin;
import com.daifukuoc.wrxj.custom.ebs.plc.commandallocation.ACPCommandAllocator;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound.PLCMoveOrderMessage;

/**
 * Unit test case class for ACPCommandAllocator
 * @author MT
 *
 */
@ExtendWith(MockitoExtension.class)
public class ACPCommandAllocatorTest {

	private ACPCommandAllocator acpCommandAllocator;
	
	MockedStatic<Factory> mockedFactory;
	
	@Mock
	ACPTableJoin mpTableJoin;
	
	@Mock
	PLCMoveOrderMessage plcMoveOrderMessage;
	
	@Mock
	ACPMoveCommandServer acpMoveCommandServer;
	
	@Mock
	MoveCommandData moveCommandData;
	
	private static final String DEVICEID = "9001";
	private static final String CMD_READY ="CMDREADY"; 
	private static final String CMD_WORKING ="CMDWORKING";
	private static final String CMD_DEST_STATION = "DESTSTATION";
	

	@BeforeEach
	public void setUp() {
		mockedFactory = Mockito.mockStatic(Factory.class);
		mockedFactory.when(() -> Factory.create(ACPTableJoin.class)).thenReturn(mpTableJoin);
		mockedFactory.when(() -> Factory.create(MoveCommandData.class)).thenReturn(moveCommandData);
		mockedFactory.when(() -> Factory.create(PLCMoveOrderMessage.class)).thenReturn(plcMoveOrderMessage);
		mockedFactory.when(() -> Factory.create(ACPMoveCommandServer.class)).thenReturn(acpMoveCommandServer);
		
		acpCommandAllocator = new ACPCommandAllocator(DEVICEID);
	}
	
	@AfterEach
    public void tearDown() {
        mockedFactory.close();
    }
	
	@Test
	public void testAllocateAll() throws DBException {
		Map<String, Object> map = new HashMap<>();
		map.put(CMD_READY, 1);
		map.put(CMD_WORKING, 1);
		map.put(CMD_DEST_STATION, "DEST");
		
		Map<String, Object> commandMap = new HashMap<>();
		List<Map<String, Object>> commandMapList = Arrays.asList(commandMap);
		List<Map<String, Object>> commandCountList = Arrays.asList(map);
		doReturn(commandCountList).when(mpTableJoin).getCMDCountForOutboundStations(DEVICEID);
		doReturn(commandMapList).when(mpTableJoin).getCMDFor(DEVICEID, 1, "DEST");
		doReturn(commandCountList).when(mpTableJoin).getCMDCountForLiftPickupTransferStations(DEVICEID);
		doReturn(commandCountList).when(mpTableJoin).getCMDCountForInputASRSLocations(DEVICEID);
		doReturn(commandCountList).when(mpTableJoin).getCMDCountForInputTransferStations(DEVICEID);
		doNothing().when(moveCommandData).dataToSKDCData(commandMap);
		doNothing().when(plcMoveOrderMessage).sendMessageToPlc();
		doReturn("ORDER").when(moveCommandData).getOrderid();
		doReturn("LOAD").when(moveCommandData).getLoadID();
		doReturn("ITEM").when(moveCommandData).getItemID();
		doReturn("GLOBAL").when(moveCommandData).getGlobalID();
		doReturn("FLIGHT").when(moveCommandData).getFlightNum();
		doReturn(new Date()).when(moveCommandData).getFlightSTD();
		doReturn("FINALSORTLOCATION").when(moveCommandData).getFinalSortLocationID();
		doReturn("FROM").when(moveCommandData).getFrom();
		doReturn("DEST").when(moveCommandData).getToDest();
		doReturn(DEVICEID).when(moveCommandData).getDeviceID();
		doReturn(2).when(moveCommandData).getCmdMoveType(); //String fromLocation,String toLocation
		doNothing().when(acpMoveCommandServer).updateMoveCmdStatusById(DEVICEID, "LOAD","FROM","DEST", DBConstants.CMD_COMMANDED);
		acpCommandAllocator.allocateAll();
		assertEquals(commandCountList.size(), 1);
		assertEquals(map, commandCountList.get(0));
		assertEquals(commandMapList.size(), 1);
		assertEquals(commandMap, commandMapList.get(0));
	}
	@Test
	public void testAllocateAllWithProcessInput_Exception() throws DBException {
		Map<String, Object> map = new HashMap<>();
		map.put(CMD_READY, 1);
		map.put(CMD_WORKING, 1);
		map.put(CMD_DEST_STATION, "DEST");
		
		Map<String, Object> commandMap = new HashMap<>();
		List<Map<String, Object>> commandMapList = Arrays.asList(commandMap);
		List<Map<String, Object>> commandCountList = Arrays.asList(map);
		doReturn(commandCountList).when(mpTableJoin).getCMDCountForOutboundStations(DEVICEID);
		doReturn(commandMapList).when(mpTableJoin).getCMDFor(DEVICEID, 1, "DEST");
		doReturn(commandCountList).when(mpTableJoin).getCMDCountForLiftPickupTransferStations(DEVICEID);
		doReturn(commandCountList).when(mpTableJoin).getCMDCountForInputASRSLocations(DEVICEID);
		doThrow(new DBException()).when(mpTableJoin).getCMDCountForInputTransferStations(DEVICEID);
		doNothing().when(moveCommandData).dataToSKDCData(commandMap);
		doNothing().when(plcMoveOrderMessage).sendMessageToPlc();
		doReturn("ORDER").when(moveCommandData).getOrderid();
		doReturn("LOAD").when(moveCommandData).getLoadID();
		doReturn("ITEM").when(moveCommandData).getItemID();
		doReturn("GLOBAL").when(moveCommandData).getGlobalID();
		doReturn("FLIGHT").when(moveCommandData).getFlightNum();
		doReturn(new Date()).when(moveCommandData).getFlightSTD();
		doReturn("FINALSORTLOCATION").when(moveCommandData).getFinalSortLocationID();
		doReturn("FROM").when(moveCommandData).getFrom();
		doReturn("DEST").when(moveCommandData).getToDest();
		doReturn(DEVICEID).when(moveCommandData).getDeviceID();
		doReturn(2).when(moveCommandData).getCmdMoveType();
		doNothing().when(acpMoveCommandServer).updateMoveCmdStatusById(DEVICEID, "LOAD","FROM","DEST", DBConstants.CMD_COMMANDED);
		acpCommandAllocator.allocateAll();
		assertEquals(commandCountList.size(), 1);
		assertEquals(map, commandCountList.get(0));
		assertEquals(commandMapList.size(), 1);
		assertEquals(commandMap, commandMapList.get(0));
	}
	
	@Test
	public void testAllocateAllWithProcessOutput_Exception() throws DBException {
		doThrow(new DBException()).when(mpTableJoin).getCMDCountForOutboundStations(DEVICEID);
		acpCommandAllocator.allocateAll();
	}
}
