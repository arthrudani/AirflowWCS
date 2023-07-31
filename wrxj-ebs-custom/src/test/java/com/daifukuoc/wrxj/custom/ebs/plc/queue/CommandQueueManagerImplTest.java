package com.daifukuoc.wrxj.custom.ebs.plc.queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.dataserver.standard.StandardMoveCommandServer;
import com.daifukuamerica.wrxj.dbadapter.data.MoveCommandData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;

/**
 * Unit test case class for CommandQueueManagerImpl
 * @author MT
 *
 */

@ExtendWith(MockitoExtension.class)
class CommandQueueManagerImplTest {

	private CommandQueueManagerImpl cmdQueueManagerImpl;
	
	MockedStatic<Factory> mockedFactory;
	
	@Mock
	StandardMoveCommandServer standardMoveCommandServer;
	
	@Mock
	MoveCommandData moveCommandData;
	
	private static final String DEVICE_ID = "9001";
	
	@BeforeEach
	public void setUp() {
		mockedFactory = Mockito.mockStatic(Factory.class);
		mockedFactory.when(() -> Factory.create(StandardMoveCommandServer.class)).thenReturn(standardMoveCommandServer);
		cmdQueueManagerImpl = new CommandQueueManagerImpl();
	}
	
	@AfterEach
    public void tearDown() {
        mockedFactory.close();
    }
	
	@Test
	public void testEnqueue() throws DBException {
		doNothing().when(standardMoveCommandServer).addMoveCommand(moveCommandData);
		cmdQueueManagerImpl.enqueue(moveCommandData);
		verify(standardMoveCommandServer, times(1)).addMoveCommand(moveCommandData);
	}
	
	@Test
	public void testDequue() throws DBException {
		doReturn(moveCommandData).when(standardMoveCommandServer).getOldestMoveCommandForLoadId("LOADID");
		doReturn("LOADID").when(moveCommandData).getLoadID();
		var result = cmdQueueManagerImpl.dequeue("LOADID");
		assertEquals("LOADID", result.getLoadID());
	}
	
	@Test
	public void testClearAll() throws DBException {
		doNothing().when(standardMoveCommandServer).deleteAllMoveCommand();
		cmdQueueManagerImpl.clearAll();
		verify(standardMoveCommandServer, times(1)).deleteAllMoveCommand();
	}
	
	@Test
	public void testClear() throws DBException {
		doNothing().when(standardMoveCommandServer).deleteAllMoveCommandForDevice("DEVICEID");
		cmdQueueManagerImpl.clear("DEVICEID");
		verify(standardMoveCommandServer, times(1)).deleteAllMoveCommandForDevice("DEVICEID");
	}
	
	@Test
	public void testUpdate() throws DBException {
		doNothing().when(standardMoveCommandServer).updateMoveCommand("LOADID", DBConstants.CMD_COMPLETED);
		cmdQueueManagerImpl.update("LOADID", DBConstants.CMD_COMPLETED);
		verify(standardMoveCommandServer, times(1)).updateMoveCommand("LOADID", DBConstants.CMD_COMPLETED);
	}
	
	@Test
	public void testDelete() throws DBException {
		doNothing().when(standardMoveCommandServer).deleteMoveCommand("LOADID");
		cmdQueueManagerImpl.delete("LOADID");
		verify(standardMoveCommandServer, times(1)).deleteMoveCommand("LOADID");
	}
	
	@Test
	public void testToList() throws DBException {
		doReturn(Arrays.asList(moveCommandData)).when(standardMoveCommandServer).getAllMoveCommandList("DEVICEID");
		var result = cmdQueueManagerImpl.toList("DEVICEID");
		assertEquals(1, result.size());
		assertEquals(moveCommandData, result.get(0));
	}
	
	@Test
	public void testLoad() throws DBException {
		doReturn(Arrays.asList(moveCommandData)).when(standardMoveCommandServer).getAllMoveCommandListForDeviceWithReadyStatus("DEVICEID");
		var result = cmdQueueManagerImpl.load("DEVICEID");
		assertEquals(1, result.size());
		assertEquals(moveCommandData, result.get(0));
	}
}
