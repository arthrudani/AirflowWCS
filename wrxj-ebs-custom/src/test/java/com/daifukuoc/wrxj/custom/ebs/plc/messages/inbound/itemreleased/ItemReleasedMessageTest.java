package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemreleased;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSInventoryServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLoadServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLocationServer;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSDBConstants.MSG_ACKNOWLEDGEMENT_STATUS;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.StandardItemReleasedTransaction;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound.PLCMoveOrderMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound.PLCStandardAckMessage;

@ExtendWith(MockitoExtension.class)
public class ItemReleasedMessageTest {
	MockedStatic<Factory> mockedFactory;

	@Mock
	StandardStationServer stnServer;

	@Mock
	Load mpLoad;

	@Mock
	EBSLoadServer mpEBSLoadServer;

	@Mock
	EBSInventoryServer mpEBSInventory;

	@Mock
	PLCStandardAckMessage itemAckMsg;

	@Mock
	PLCMoveOrderMessage moveOrder;

	@Mock
	EBSLocationServer ebsLocationServer;

	@Mock
	StationData stationData;

	@Mock
	LoadData loadData;

	@Mock
	LoadLineItemData loadLineData;

	@Mock
	LocationData locationData;

	@Mock
	PLCMoveOrderMessage plcMoveOrderMessage;

	@Mock
	StandardItemReleasedTransaction plcItemReleasedTransaction;

	private ItemReleasedMessage plcItemReleasedMessage;

	@BeforeEach
	public void setUp() {
		mockedFactory = Mockito.mockStatic(Factory.class);
		mockedFactory.when(() -> Factory.create(StandardStationServer.class)).thenReturn(stnServer);
		mockedFactory.when(() -> Factory.create(Load.class)).thenReturn(mpLoad);
		mockedFactory.when(() -> Factory.create(EBSLoadServer.class)).thenReturn(mpEBSLoadServer);
		mockedFactory.when(() -> Factory.create(EBSInventoryServer.class)).thenReturn(mpEBSInventory);
		mockedFactory.when(() -> Factory.create(PLCStandardAckMessage.class)).thenReturn(itemAckMsg);
		mockedFactory.when(() -> Factory.create(PLCMoveOrderMessage.class)).thenReturn(moveOrder);
		mockedFactory.when(() -> Factory.create(EBSLocationServer.class)).thenReturn(ebsLocationServer);
		mockedFactory.when(() -> Factory.create(StationData.class)).thenReturn(stationData);
		mockedFactory.when(() -> Factory.create(LoadData.class)).thenReturn(loadData);
		mockedFactory.when(() -> Factory.create(LoadLineItemData.class)).thenReturn(loadLineData);
		mockedFactory.when(() -> Factory.create(StandardItemReleasedTransaction.class)).thenReturn(plcItemReleasedTransaction);

		plcItemReleasedMessage = new ItemReleasedMessage();
	}

	@AfterEach
	public void tearDown() {
		plcItemReleasedMessage = null;
		mockedFactory.close();
	}

	//TODO: Need to update this when update the implementation for isValid() method.
	/*@Test
	void isValid_returnsFalse_WhenBothAreNull() throws DBException {
		doReturn(null).when(stnServer).getStation("STATION");
		doReturn(null).when(mpLoad).getLoadData("LOAD");

		plcItemReleasedMessage.setStationId("STATION");
		plcItemReleasedMessage.setLoadId("LOAD");
		assertFalse(plcItemReleasedMessage.isValid());

		verify(stnServer, times(1)).getStation("STATION");
		verify(mpLoad, times(1)).getLoadData("LOAD");
	}

	@Test
	void isValid_returnsFalse_WhenStationDataIsNull() throws DBException {
		doReturn(null).when(stnServer).getStation("STATION");
		doReturn(loadData).when(mpLoad).getLoadData("LOAD");

		plcItemReleasedMessage.setStationId("STATION");
		plcItemReleasedMessage.setLoadId("LOAD");
		assertFalse(plcItemReleasedMessage.isValid());

		verify(stnServer, times(1)).getStation("STATION");
		verify(mpLoad, times(1)).getLoadData("LOAD");
	}

	@Test
	void isValid_returnsFalse_WhenLoaddataIsNull() throws DBException {
		doReturn(stationData).when(stnServer).getStation("STATION");
		doReturn(null).when(mpLoad).getLoadData("LOAD");

		plcItemReleasedMessage.setStationId("STATION");
		plcItemReleasedMessage.setLoadId("LOAD");
		assertFalse(plcItemReleasedMessage.isValid());

		verify(stnServer, times(1)).getStation("STATION");
		verify(mpLoad, times(1)).getLoadData("LOAD");
	}

	@Test
	void isValid_returnsFalse_WhenBothAreFound() throws DBException {
		doReturn(stationData).when(stnServer).getStation("STATION");
		doReturn(loadData).when(mpLoad).getLoadData("LOAD");

//		plcItemReleasedMessage.setStationId("STATION");
		plcItemReleasedMessage.setLoadId("LOAD");
		assertTrue(plcItemReleasedMessage.isValid());

//		verify(stnServer, times(1)).getStation("STATION");
		verify(mpLoad, times(1)).getLoadData("LOAD");
	}

	@ParameterizedTest
	@NullAndEmptySource
	void parse_returnsFalse_WhenInputIsNullOrEmpty(String sMsg) {
		assertFalse(plcItemReleasedMessage.parse(sMsg));
	}*/

	@Test
	void parse_returnsTrue_WhenInputIsCorrect() {
		assertTrue(plcItemReleasedMessage.parse("60,SERIAL,ORDER,LOAD,GLOBAL,LINE,1234567890"));

		assertEquals("SERIAL", plcItemReleasedMessage.getSerialNumber());
		assertEquals("ORDER", plcItemReleasedMessage.getOrderId());
		assertEquals("LOAD", plcItemReleasedMessage.getLoadId());
		assertEquals("GLOBAL", plcItemReleasedMessage.getGlogalId());
		assertEquals("LINE", plcItemReleasedMessage.getLineId());
		assertEquals("1234567890", plcItemReleasedMessage.getStationId());
	}

	@Test
	void parse_returnsTrue_WhenInputIsCorrect_StationIDIs0Prefixed() {
		assertTrue(plcItemReleasedMessage.parse("60,SERIAL,ORDER,LOAD,GLOBAL,LINE,1"));

		assertEquals("SERIAL", plcItemReleasedMessage.getSerialNumber());
		assertEquals("ORDER", plcItemReleasedMessage.getOrderId());
		assertEquals("LOAD", plcItemReleasedMessage.getLoadId());
		assertEquals("GLOBAL", plcItemReleasedMessage.getGlogalId());
		assertEquals("LINE", plcItemReleasedMessage.getLineId());
		assertEquals("1", plcItemReleasedMessage.getStationId());
	}

	@Test
	void processAck_setsValues() {
		doNothing().when(itemAckMsg).setDeviceId("DEVICE");
		doNothing().when(itemAckMsg).setSerialNum("SERIAL");
		doNothing().when(itemAckMsg).setMessageType(PLCConstants.PLC_ITEM_RELEASED_ACK_MSG_TYPE);
		doNothing().when(itemAckMsg).setStatus(String.valueOf(MSG_ACKNOWLEDGEMENT_STATUS.OK));
		doNothing().when(itemAckMsg).sendMessageToPlc();

		plcItemReleasedMessage.setDeviceId("DEVICE");
		plcItemReleasedMessage.setSerialNumber("SERIAL");
		plcItemReleasedMessage.processAck();

		verify(itemAckMsg, times(1)).setDeviceId("DEVICE");
		verify(itemAckMsg, times(1)).setSerialNum("SERIAL");
		verify(itemAckMsg, times(1)).setMessageType(PLCConstants.PLC_ITEM_RELEASED_ACK_MSG_TYPE);
		verify(itemAckMsg, times(1)).setStatus(String.valueOf(MSG_ACKNOWLEDGEMENT_STATUS.OK));
		verify(itemAckMsg, times(1)).sendMessageToPlc();
	}

	@Test
	void getLoadData_getsLoadWithLoadID_LoadServerIsNotNull() {
		doReturn(loadData).when(mpEBSLoadServer).getLoad("LOAD");
		plcItemReleasedMessage.setLoadId("LOAD");
		assertEquals(loadData, plcItemReleasedMessage.getLoadData());
		verify(mpEBSLoadServer, times(1)).getLoad("LOAD");
	}

	@Test
	void getLoadData_getsLoadWithLoadID_LoadServerIsNull() {
		doReturn(loadData).when(mpEBSLoadServer).getLoad("LOAD");
		plcItemReleasedMessage.mpEBSLoadServer = null;
		plcItemReleasedMessage.setLoadId("LOAD");
		assertEquals(loadData, plcItemReleasedMessage.getLoadData());
		verify(mpEBSLoadServer, times(1)).getLoad("LOAD");
	}

	@Test
	void getLoadLineData_getsLoadWithLoadID_LoadServerIsNotNull() {
		doReturn(loadLineData).when(mpEBSLoadServer).getLoadLineByLoadId("LOAD");
		plcItemReleasedMessage.setLoadId("LOAD");
		assertEquals(loadLineData, plcItemReleasedMessage.getLoadLineData());
		verify(mpEBSLoadServer, times(1)).getLoadLineByLoadId("LOAD");
	}

	@Test
	void getLoadLineData_getsLoadWithLoadID_LoadServerIsNull() {
		doReturn(loadLineData).when(mpEBSLoadServer).getLoadLineByLoadId("LOAD");
		plcItemReleasedMessage.mpEBSLoadServer = null;
		plcItemReleasedMessage.setLoadId("LOAD");
		assertEquals(loadLineData, plcItemReleasedMessage.getLoadLineData());
		verify(mpEBSLoadServer, times(1)).getLoadLineByLoadId("LOAD");
	}

	@Test
	void findAndSetStation_setsNullToTheInstanceFieldIfNotFound() {
		doReturn(null).when(stnServer).getStation("STATION");

		plcItemReleasedMessage.stationData = stationData;
		plcItemReleasedMessage.setStationId("STATION");
		plcItemReleasedMessage.findAndSetStation();

		assertNull(plcItemReleasedMessage.stationData);

		verify(stnServer, times(1)).getStation("STATION");
	}

	@Test
	void findAndSetStation_setsNullToTheInstanceFieldIfExceptionIsThrown() {
		doThrow(new IllegalStateException()).when(stnServer).getStation("STATION");

		plcItemReleasedMessage.stationData = stationData;
		plcItemReleasedMessage.setStationId("STATION");
		plcItemReleasedMessage.findAndSetStation();

		assertNull(plcItemReleasedMessage.stationData);

		verify(stnServer, times(1)).getStation("STATION");
	}

	@Test
	void process_normalCase() throws DBException {
		ItemReleasedMessage spiedPlcItemReleasedMessage = spy(plcItemReleasedMessage);

		ItemReleasedContext plcItemReleasedContext = new ItemReleasedContext(null, loadData, loadLineData, "", "",
				"", "", "", "0", "");

		doNothing().when(spiedPlcItemReleasedMessage).findAndSetStation();
		doReturn(loadData).when(spiedPlcItemReleasedMessage).getLoadData();
		doReturn(loadLineData).when(spiedPlcItemReleasedMessage).getLoadLineData();
		doNothing().when(plcItemReleasedTransaction).execute(plcItemReleasedContext);

		spiedPlcItemReleasedMessage.process();

		verify(spiedPlcItemReleasedMessage, times(1)).findAndSetStation();
		verify(spiedPlcItemReleasedMessage, times(1)).getLoadData();
		verify(spiedPlcItemReleasedMessage, times(1)).getLoadLineData();
		verify(plcItemReleasedTransaction, times(1)).execute(plcItemReleasedContext);
	}

	@Test
	void process_abnormalCase_DBException() throws DBException {
		ItemReleasedMessage spiedPlcItemReleasedMessage = spy(plcItemReleasedMessage);

		ItemReleasedContext plcItemReleasedContext = new ItemReleasedContext(null, loadData, loadLineData, "", "",
				"", "", "", "0", "");

		doNothing().when(spiedPlcItemReleasedMessage).findAndSetStation();
		doReturn(loadData).when(spiedPlcItemReleasedMessage).getLoadData();
		doReturn(loadLineData).when(spiedPlcItemReleasedMessage).getLoadLineData();
		doThrow(new DBException()).when(plcItemReleasedTransaction).execute(plcItemReleasedContext);

		spiedPlcItemReleasedMessage.process();

		verify(spiedPlcItemReleasedMessage, times(1)).findAndSetStation();
		verify(spiedPlcItemReleasedMessage, times(1)).getLoadData();
		verify(spiedPlcItemReleasedMessage, times(1)).getLoadLineData();
		verify(plcItemReleasedTransaction, times(1)).execute(plcItemReleasedContext);
	}

	@Test
	void outputTransactionLog_expectCallsLogMEthodAsExpected() {
		StationData stationData = new StationData();
		LoadData loadData = new LoadData();
		LoadLineItemData loadLineItemData = new LoadLineItemData();

		ItemReleasedContext plcItemReleasedContext = new ItemReleasedContext(stationData, loadData, loadLineItemData,
				"STATION", "ORDER", "LOAD", "GLOBAL", "LINE", "SERIAL", "DEVICE");

		doNothing().when(mpEBSLoadServer).logLoadItemReleasedTransaction(loadData, loadLineItemData, "STATION", "DEVICE",
				"LINE", "ORDER");
		plcItemReleasedMessage.outputTransactionLog(plcItemReleasedContext);
		verify(mpEBSLoadServer, times(1)).logLoadItemReleasedTransaction(loadData, loadLineItemData, "STATION", "DEVICE",
				"LINE", "ORDER");

	}
}
