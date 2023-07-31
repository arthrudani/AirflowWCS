package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemarrived;

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
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.StandardItemArrivedTransaction;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemarrived.ItemArrivedContext;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemarrived.ItemArrivedMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemarrived.PLCItemArrivedTransaction;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound.PLCMoveOrderMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound.PLCStandardAckMessage;

@ExtendWith(MockitoExtension.class)
public class ItemArrivedMessageTest {
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
	StandardItemArrivedTransaction plcItemArrivedTransaction;

	private ItemArrivedMessage plcItemArrivedMessage;

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
		mockedFactory.when(() -> Factory.create(StandardItemArrivedTransaction.class)).thenReturn(plcItemArrivedTransaction);

		plcItemArrivedMessage = new ItemArrivedMessage();
	}

	@AfterEach
	public void tearDown() {
		plcItemArrivedMessage = null;
		mockedFactory.close();
	}

	@Test
	void isValid_returnsFalse_WhenBothAreNull() throws DBException {
		doReturn(null).when(stnServer).getStation("STATION");

		plcItemArrivedMessage.setStationId("STATION");
		plcItemArrivedMessage.setLoadId("LOAD");
		assertFalse(plcItemArrivedMessage.isValid());

		verify(stnServer, times(1)).getStation("STATION");
	}

	@Test
	void isValid_returnsFalse_WhenStationDataIsNull() throws DBException {
		doReturn(null).when(stnServer).getStation("STATION");

		plcItemArrivedMessage.setStationId("STATION");
		plcItemArrivedMessage.setLoadId("LOAD");
		assertFalse(plcItemArrivedMessage.isValid());

		verify(stnServer, times(1)).getStation("STATION");
	}

	@Test
	void isValid_returnsFalse_WhenLoaddataIsNull() throws DBException {
		doReturn(stationData).when(stnServer).getStation("STATION");

		plcItemArrivedMessage.setStationId("STATION");
		plcItemArrivedMessage.setLoadId("LOAD");
		assertTrue(plcItemArrivedMessage.isValid());

		verify(stnServer, times(1)).getStation("STATION");
	}

	@Test
	void isValid_returnsFalse_WhenBothAreFound() throws DBException {
		doReturn(stationData).when(stnServer).getStation("STATION");

		plcItemArrivedMessage.setStationId("STATION");
		plcItemArrivedMessage.setLoadId("LOAD");
		assertTrue(plcItemArrivedMessage.isValid());

		verify(stnServer, times(1)).getStation("STATION");
	}

	@ParameterizedTest
	@NullAndEmptySource
	void parse_returnsFalse_WhenInputIsNullOrEmpty(String sMsg) {
		assertFalse(plcItemArrivedMessage.parse(sMsg));
	}

	@Test
	void parse_returnsTrue_WhenInputIsCorrect() {
		assertTrue(plcItemArrivedMessage.parse("60,SERIAL,ORDER,LOAD,GLOBAL,LINE,1234567890"));

		assertEquals("SERIAL", plcItemArrivedMessage.getSerialNumber());
		assertEquals("ORDER", plcItemArrivedMessage.getOrderId());
		assertEquals("LOAD", plcItemArrivedMessage.getLoadId());
		assertEquals("GLOBAL", plcItemArrivedMessage.getGlogalId());
		assertEquals("LINE", plcItemArrivedMessage.getLineId());
		assertEquals("1234567890", plcItemArrivedMessage.getStationId());
	}

	@Test
	void parse_returnsTrue_WhenInputIsCorrect_StationIDIs0Prefixed() {
		assertTrue(plcItemArrivedMessage.parse("60,SERIAL,ORDER,LOAD,GLOBAL,LINE,1"));

		assertEquals("SERIAL", plcItemArrivedMessage.getSerialNumber());
		assertEquals("ORDER", plcItemArrivedMessage.getOrderId());
		assertEquals("LOAD", plcItemArrivedMessage.getLoadId());
		assertEquals("GLOBAL", plcItemArrivedMessage.getGlogalId());
		assertEquals("LINE", plcItemArrivedMessage.getLineId());
		assertEquals("0000000001", plcItemArrivedMessage.getStationId());
	}

	@Test
	void processAck_setsValues() {
		doNothing().when(itemAckMsg).setDeviceId("DEVICE");
		doNothing().when(itemAckMsg).setSerialNum("SERIAL");
		doNothing().when(itemAckMsg).setMessageType(PLCConstants.PLC_ITEM_ARRIVED_ACK_MSG_TYPE);
		doNothing().when(itemAckMsg).setStatus(String.valueOf(MSG_ACKNOWLEDGEMENT_STATUS.OK));
		doNothing().when(itemAckMsg).sendMessageToPlc();

		plcItemArrivedMessage.setDeviceId("DEVICE");
		plcItemArrivedMessage.setSerialNumber("SERIAL");
		plcItemArrivedMessage.processAck();

		verify(itemAckMsg, times(1)).setDeviceId("DEVICE");
		verify(itemAckMsg, times(1)).setSerialNum("SERIAL");
		verify(itemAckMsg, times(1)).setMessageType(PLCConstants.PLC_ITEM_ARRIVED_ACK_MSG_TYPE);
		verify(itemAckMsg, times(1)).setStatus(String.valueOf(MSG_ACKNOWLEDGEMENT_STATUS.OK));
		verify(itemAckMsg, times(1)).sendMessageToPlc();
	}

	@Test
	void getLoadData_getsLoadWithLoadID_LoadServerIsNotNull() {
		doReturn(loadData).when(mpEBSLoadServer).getLoad("LOAD");
		plcItemArrivedMessage.setLoadId("LOAD");
		assertEquals(loadData, plcItemArrivedMessage.getLoadData());
		verify(mpEBSLoadServer, times(1)).getLoad("LOAD");
	}

	@Test
	void getLoadData_getsLoadWithLoadID_LoadServerIsNull() {
		doReturn(loadData).when(mpEBSLoadServer).getLoad("LOAD");
		plcItemArrivedMessage.mpEBSLoadServer = null;
		plcItemArrivedMessage.setLoadId("LOAD");
		assertEquals(loadData, plcItemArrivedMessage.getLoadData());
		verify(mpEBSLoadServer, times(1)).getLoad("LOAD");
	}

	@Test
	void getLoadLineData_getsLoadWithLoadID_LoadServerIsNotNull() {
		doReturn(loadLineData).when(mpEBSLoadServer).getLoadLineByLoadId("LOAD");
		plcItemArrivedMessage.setLoadId("LOAD");
		assertEquals(loadLineData, plcItemArrivedMessage.getLoadLineData());
		verify(mpEBSLoadServer, times(1)).getLoadLineByLoadId("LOAD");
	}

	@Test
	void getLoadLineData_getsLoadWithLoadID_LoadServerIsNull() {
		doReturn(loadLineData).when(mpEBSLoadServer).getLoadLineByLoadId("LOAD");
		plcItemArrivedMessage.mpEBSLoadServer = null;
		plcItemArrivedMessage.setLoadId("LOAD");
		assertEquals(loadLineData, plcItemArrivedMessage.getLoadLineData());
		verify(mpEBSLoadServer, times(1)).getLoadLineByLoadId("LOAD");
	}

	@Test
	void findAndSetStation_setsNullToTheInstanceFieldIfNotFound() {
		doReturn(null).when(stnServer).getStation("STATION");

		plcItemArrivedMessage.stationData = stationData;
		plcItemArrivedMessage.setStationId("STATION");
		plcItemArrivedMessage.findAndSetStation();

		assertNull(plcItemArrivedMessage.stationData);

		verify(stnServer, times(1)).getStation("STATION");
	}

	@Test
	void findAndSetStation_setsNullToTheInstanceFieldIfExceptionIsThrown() {
		doThrow(new IllegalStateException()).when(stnServer).getStation("STATION");

		plcItemArrivedMessage.stationData = stationData;
		plcItemArrivedMessage.setStationId("STATION");
		plcItemArrivedMessage.findAndSetStation();

		assertNull(plcItemArrivedMessage.stationData);

		verify(stnServer, times(1)).getStation("STATION");
	}

	@Test
	void process_normalCase() throws DBException {
		ItemArrivedMessage spiedPlcItemArrivedMessage = spy(plcItemArrivedMessage);

		ItemArrivedContext plcItemArrivedContext = new ItemArrivedContext(null, loadData, loadLineData, "", "",
				"", "", "", "0", "");

		doNothing().when(spiedPlcItemArrivedMessage).findAndSetStation();
		doReturn(loadData).when(spiedPlcItemArrivedMessage).getLoadData();
		doReturn(loadLineData).when(spiedPlcItemArrivedMessage).getLoadLineData();
		doNothing().when(plcItemArrivedTransaction).execute(plcItemArrivedContext);

		spiedPlcItemArrivedMessage.process();

		verify(spiedPlcItemArrivedMessage, times(1)).findAndSetStation();
		verify(spiedPlcItemArrivedMessage, times(1)).getLoadData();
		verify(spiedPlcItemArrivedMessage, times(1)).getLoadLineData();
		verify(plcItemArrivedTransaction, times(1)).execute(plcItemArrivedContext);
	}

	@Test
	void process_abnormalCase_DBException() throws DBException {
		ItemArrivedMessage spiedPlcItemArrivedMessage = spy(plcItemArrivedMessage);

		ItemArrivedContext plcItemArrivedContext = new ItemArrivedContext(null, loadData, loadLineData, "", "",
				"", "", "", "0", "");

		doNothing().when(spiedPlcItemArrivedMessage).findAndSetStation();
		doReturn(loadData).when(spiedPlcItemArrivedMessage).getLoadData();
		doReturn(loadLineData).when(spiedPlcItemArrivedMessage).getLoadLineData();
		doThrow(new DBException()).when(plcItemArrivedTransaction).execute(plcItemArrivedContext);

		spiedPlcItemArrivedMessage.process();

		verify(spiedPlcItemArrivedMessage, times(1)).findAndSetStation();
		verify(spiedPlcItemArrivedMessage, times(1)).getLoadData();
		verify(spiedPlcItemArrivedMessage, times(1)).getLoadLineData();
		verify(plcItemArrivedTransaction, times(1)).execute(plcItemArrivedContext);
	}

	@Test
	void outputTransactionLog_expectCallsLogMEthodAsExpected() {
		StationData stationData = new StationData();
		LoadData loadData = new LoadData();
		LoadLineItemData loadLineItemData = new LoadLineItemData();

		ItemArrivedContext plcItemArrivedContext = new ItemArrivedContext(stationData, loadData, loadLineItemData,
				"STATION", "ORDER", "LOAD", "GLOBAL", "LINE", "SERIAL", "DEVICE");

		doNothing().when(mpEBSLoadServer).logLoadItemArrivedTransaction(loadData, loadLineItemData, "STATION", "DEVICE",
				"LINE", "ORDER");
		plcItemArrivedMessage.outputTransactionLog(plcItemArrivedContext);
		verify(mpEBSLoadServer, times(1)).logLoadItemArrivedTransaction(loadData, loadLineItemData, "STATION", "DEVICE",
				"LINE", "ORDER");

	}
}
