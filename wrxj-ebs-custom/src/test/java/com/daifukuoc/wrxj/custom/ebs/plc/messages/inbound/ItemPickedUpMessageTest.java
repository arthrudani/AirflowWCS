package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

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

import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLoadServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLocationServer;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSDBConstants.MSG_ACKNOWLEDGEMENT_STATUS;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itempickedup.ItemPickedUpContext;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itempickedup.ItemPickedUpMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itempickedup.PLCItemPickedUpTransaction;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound.PLCStandardAckMessage;

@ExtendWith(MockitoExtension.class)
class ItemPickedUpMessageTest {

	MockedStatic<Factory> mockedFactory;

	@Mock
	StandardItemPickedupTransaction plcItemPickedUpTransaction;

	@Mock
	EBSLocationServer ebsLocationServer;

	@Mock
	LoadData loadData;

	@Mock
	PLCStandardAckMessage plcStandardAckMessage;

	@Mock
	LocationData locationData;

	@Mock
	Load mpLoad;

	@Mock
	EBSLoadServer mpEBSLoadServer;

	@Mock
	LoadLineItemData loadLineItemData;

	private ItemPickedUpMessage plcItemPickedUpMessage = null;

	@BeforeEach
	public void setUp() {
		mockedFactory = Mockito.mockStatic(Factory.class);
		mockedFactory.when(() -> Factory.create(StandardItemPickedupTransaction.class))
				.thenReturn(plcItemPickedUpTransaction);
		mockedFactory.when(() -> Factory.create(PLCStandardAckMessage.class)).thenReturn(plcStandardAckMessage);
		mockedFactory.when(() -> Factory.create(Load.class)).thenReturn(mpLoad);
		mockedFactory.when(() -> Factory.create(EBSLoadServer.class)).thenReturn(mpEBSLoadServer);

		plcItemPickedUpMessage = new ItemPickedUpMessage();
	}

	@AfterEach
	public void tearDown() {
		plcItemPickedUpMessage = null;
		mockedFactory.close();
	}

	@ParameterizedTest
	@NullAndEmptySource
	void parseReturnsFalseToNummOrEmptyInput(String input) {
		assertFalse(plcItemPickedUpMessage.parse(input));
	}

	@Test
	void parsePopulatesValuesAsExpected() {
		assertTrue(plcItemPickedUpMessage.parse("53,123,222,456,789,abcdabcdabcd,123,456,5"));
		assertEquals("222", plcItemPickedUpMessage.getOrderID());
		assertEquals("456", plcItemPickedUpMessage.getLoadId());
		assertEquals("789", plcItemPickedUpMessage.getGlogalId());
		assertEquals("abcdabcdabcd", plcItemPickedUpMessage.getLineId());
		assertEquals("0000000123", plcItemPickedUpMessage.getFromLocationID());
		assertEquals("0000000456", plcItemPickedUpMessage.getToLocationID());
		assertEquals(5, plcItemPickedUpMessage.getStatus());
	}

	@Test
	void parsePopulatesValuesAsExpected_statusIsReplacedWith1InNonNumeric() {
		assertTrue(plcItemPickedUpMessage.parse("53,123,222,456,789,abcdabcdabcd,123,456,a"));
		assertEquals("222", plcItemPickedUpMessage.getOrderID());
		assertEquals("456", plcItemPickedUpMessage.getLoadId());
		assertEquals("789", plcItemPickedUpMessage.getGlogalId());
		assertEquals("abcdabcdabcd", plcItemPickedUpMessage.getLineId());
		assertEquals("0000000123", plcItemPickedUpMessage.getFromLocationID());
		assertEquals("0000000456", plcItemPickedUpMessage.getToLocationID());
		assertEquals(1, plcItemPickedUpMessage.getStatus());
	}

	@Test
	void processMethodShouldCallTransactionWithParams() throws DBException {
		doNothing().when(plcItemPickedUpTransaction).execute(new ItemPickedUpContext("LOAD", "FROM", "TO"));

		plcItemPickedUpMessage.setLoadId("LOAD");
		plcItemPickedUpMessage.setFromLocationId("FROM");
		plcItemPickedUpMessage.setToLocationId("TO");
		plcItemPickedUpMessage.setDeviceId("DEVICE");
		plcItemPickedUpMessage.setLineId("LINE");
		plcItemPickedUpMessage.setOrderId("ORDER");

		plcItemPickedUpMessage.process();

		verify(plcItemPickedUpTransaction, times(1)).execute(new ItemPickedUpContext("LOAD", "FROM", "TO"));

	}

	@Test
	void processMethodShouldCatchDBException() throws DBException {
		doThrow(new DBException()).when(plcItemPickedUpTransaction)
				.execute(new ItemPickedUpContext("LOAD", "FROM", "TO"));

		plcItemPickedUpMessage.setLoadId("LOAD");
		plcItemPickedUpMessage.setFromLocationId("FROM");
		plcItemPickedUpMessage.setToLocationId("TO");
		plcItemPickedUpMessage.process();

		verify(plcItemPickedUpTransaction, times(1)).execute(new ItemPickedUpContext("LOAD", "FROM", "TO"));

	}

	@Test
	void processAckBehavesAsExpected() {
		doNothing().when(plcStandardAckMessage).setDeviceId("DEV");
		doNothing().when(plcStandardAckMessage).setSerialNum("S001");
		doNothing().when(plcStandardAckMessage).setMessageType(PLCConstants.PLC_ITEM_PICKEDUP_ACK_MSG_TYPE);
		doNothing().when(plcStandardAckMessage).setStatus(String.valueOf(MSG_ACKNOWLEDGEMENT_STATUS.OK));
		doNothing().when(plcStandardAckMessage).sendMessageToPlc();

		plcItemPickedUpMessage.setDeviceId("DEV");
		plcItemPickedUpMessage.setSerialNumber("S001");
		plcItemPickedUpMessage.processAck();

		verify(plcStandardAckMessage, times(1)).setDeviceId("DEV");
		verify(plcStandardAckMessage, times(1)).setSerialNum("S001");
		verify(plcStandardAckMessage, times(1)).setMessageType(PLCConstants.PLC_ITEM_PICKEDUP_ACK_MSG_TYPE);
		verify(plcStandardAckMessage, times(1)).setStatus(String.valueOf(MSG_ACKNOWLEDGEMENT_STATUS.OK));
		verify(plcStandardAckMessage, times(1)).sendMessageToPlc();

		verifyNoMoreInteractions(plcStandardAckMessage);
	}

	@Test
	void outputTransactionLog_callsMethodAsExpected() throws DBException {
		doReturn(loadData).when(mpLoad).getLoadData("LOAD");
		doReturn(loadLineItemData).when(mpEBSLoadServer).getLoadLineByLoadId("LOAD");
		doNothing().when(mpEBSLoadServer).logLoadItemPickedUpTransaction(loadData, loadLineItemData, "FROM", "DEVICE",
				"LINE", "ORDER");

		ItemPickedUpContext plcItemPickedUpContext = new ItemPickedUpContext("LOAD", "FROM", "TO");
		plcItemPickedUpMessage.setLoadId("LOAD");
		plcItemPickedUpMessage.setDeviceId("DEVICE");
		plcItemPickedUpMessage.setLineId("LINE");
		plcItemPickedUpMessage.setOrderId("ORDER");
		plcItemPickedUpMessage.outputTransactionLog(plcItemPickedUpContext);

		verify(mpLoad, times(1)).getLoadData("LOAD");
		verify(mpEBSLoadServer, times(1)).getLoadLineByLoadId("LOAD");
		verify(mpEBSLoadServer, times(1)).logLoadItemPickedUpTransaction(loadData, loadLineItemData, "FROM", "DEVICE",
				"LINE", "ORDER");

	}
}
