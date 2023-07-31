package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSSchedulerServer;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSDBConstants.MSG_ACKNOWLEDGEMENT_STATUS;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemstored.ItemStoredContext;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemstored.ItemStoredMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemstored.PLCItemStoredTransaction;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound.PLCStandardAckMessage;

@ExtendWith(MockitoExtension.class)
class PLCItemStoredMessageTest {
    MockedStatic<Factory> mockedFactory;

    @Mock
    Load mpLoad;

    @Mock
    PLCStandardAckMessage plcStandardAckMessage;

    @Mock
    EBSSchedulerServer ebsSchedulerServer;

    @Mock
    LoadData loadData;

    @Mock
    StandardItemStoredTransaction plcItemStoredTransaction;

    private ItemStoredMessage plcItemStoredMessage;

    @BeforeEach
    public void setUp() {
        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(() -> Factory.create(Load.class)).thenReturn(mpLoad);
        mockedFactory.when(() -> Factory.create(PLCStandardAckMessage.class)).thenReturn(plcStandardAckMessage);
        mockedFactory.when(() -> Factory.create(EBSSchedulerServer.class)).thenReturn(ebsSchedulerServer);
        mockedFactory.when(() -> Factory.create(StandardItemStoredTransaction.class)).thenReturn(plcItemStoredTransaction);
        plcItemStoredMessage = new ItemStoredMessage();
    }

    @AfterEach
    public void tearDown() {
        plcItemStoredMessage = null;
        mockedFactory.close();
    }

    @ParameterizedTest
    @MethodSource("setAddressId_SetsValuesAsExpected_Param")
    void setAddressId_SetsValuesAsExpected(String addressId, String expectation) {
        plcItemStoredMessage.setAddressId(addressId);
        assertEquals(expectation, plcItemStoredMessage.getAddressId());
    }

    private static Stream<Arguments> setAddressId_SetsValuesAsExpected_Param() {
        return Stream.of(Arguments.of("A", "A"), Arguments.of("123", "0000000123"),
                Arguments.of("1234567890", "1234567890"), Arguments.of(null, null), Arguments.of("", ""));
    }

    @ParameterizedTest(name = "{index} : {0}")
    @MethodSource("setStatus_statusIsSetAsExpected_Param")
    void setStatus_statusIsSetAsExpected(String desc, String status, int expectation) {
        plcItemStoredMessage.setStatus(status);
        assertEquals(expectation, plcItemStoredMessage.getStatus());
    }

    private static Stream<Arguments> setStatus_statusIsSetAsExpected_Param() {
        return Stream.of(Arguments.of("Number is set", "123", 123),
                Arguments.of("Illegal string is defaulted to 2", "Hello", 2));
    }

    @Test
    void isValid_normalCase() throws DBException {
        doReturn(loadData).when(mpLoad).getLoadData("LOAD");

        plcItemStoredMessage.setLoadId("LOAD");
        plcItemStoredMessage.setAddressId("ADDRESS");

        assertTrue(plcItemStoredMessage.isValid());

        verify(mpLoad, times(1)).getLoadData("LOAD");
    }

    @Test
    void isValid_abnormalCase_loadDataIsNotFound() throws DBException {
        doReturn(null).when(mpLoad).getLoadData("LOAD");

        plcItemStoredMessage.setLoadId("LOAD");
        plcItemStoredMessage.setAddressId("ADDRESS");
        assertThrows(DBException.class, () -> plcItemStoredMessage.isValid());

        verify(mpLoad, times(1)).getLoadData("LOAD");
    }

    @ParameterizedTest
    @MethodSource("isValid_abNormalCase_InvalidLoadIDAndOrAddressId_Param")
    void isValid_abNormalCase_InvalidLoadIDAndOrAddressId(String loadId, String addressId) throws DBException {
        plcItemStoredMessage.setLoadId(loadId);
        plcItemStoredMessage.setAddressId(addressId);

        assertTrue(plcItemStoredMessage.isValid());

    }

    private static Stream<Arguments> isValid_abNormalCase_InvalidLoadIDAndOrAddressId_Param() {
        return Stream.of(Arguments.of("ABC", null), Arguments.of("ABC", ""), Arguments.of(null, "ABC"),
                Arguments.of(null, ""), Arguments.of(null, null));
    }

    @Test
    void processAck_chackBehavior() {
        doNothing().when(plcStandardAckMessage).setDeviceId("DEVICE");
        doNothing().when(plcStandardAckMessage).setSerialNum("SERIAL");
        doNothing().when(plcStandardAckMessage).setMessageType(PLCConstants.PLC_ITEM_STORED_ACK_MSG_TYPE);
        doNothing().when(plcStandardAckMessage).setStatus(String.valueOf(MSG_ACKNOWLEDGEMENT_STATUS.OK));
        doNothing().when(plcStandardAckMessage).sendMessageToPlc();

        plcItemStoredMessage.setDeviceId("DEVICE");
        plcItemStoredMessage.setSerialNumber("SERIAL");
        plcItemStoredMessage.processAck();

        verify(plcStandardAckMessage, times(1)).setDeviceId("DEVICE");
        verify(plcStandardAckMessage, times(1)).setSerialNum("SERIAL");
        verify(plcStandardAckMessage, times(1)).setMessageType(PLCConstants.PLC_ITEM_STORED_ACK_MSG_TYPE);
        verify(plcStandardAckMessage, times(1)).setStatus(String.valueOf(MSG_ACKNOWLEDGEMENT_STATUS.OK));
        verify(plcStandardAckMessage, times(1)).sendMessageToPlc();
    }

    @Test
    void parse_normalCase() {
        assertTrue(plcItemStoredMessage.parse("1,2,3,4,5,6,7,8"));

        assertEquals("2", plcItemStoredMessage.getSerialNumber());
        assertEquals("3", plcItemStoredMessage.getOrderId());
        assertEquals("4", plcItemStoredMessage.getLoadId());
        assertEquals("5", plcItemStoredMessage.getGlobalId());
        assertEquals("6", plcItemStoredMessage.getLineId());
        assertEquals("0000000007", plcItemStoredMessage.getAddressId());
        assertEquals(8, plcItemStoredMessage.getStatus());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void parse_returnsFalse_whenNullOrEmptyText(String receivedText) {
        assertFalse(plcItemStoredMessage.parse(receivedText));
    }

    @Test
    void process_normalCase() throws DBException {
        ItemStoredContext plcItemStoredContext = new ItemStoredContext("ORDER", "LOAD", "ADDRESS", 0, "GLOBAL",
                "LINE", "DEVICE");
        doNothing().when(plcItemStoredTransaction).execute(plcItemStoredContext);

        plcItemStoredMessage.setOrderId("ORDER");
        plcItemStoredMessage.setLoadId("LOAD");
        plcItemStoredMessage.setAddressId("ADDRESS");
        plcItemStoredMessage.setStatus(0);
        plcItemStoredMessage.setGlobalId("GLOBAL");
        plcItemStoredMessage.setLineId("LINE");
        plcItemStoredMessage.process();

        verify(plcItemStoredTransaction).execute(plcItemStoredContext);

    }

    @Test
    void process_abnormalCase_DBExceptionIsThrown() throws DBException {
        ItemStoredContext plcItemStoredContext = new ItemStoredContext("ORDER", "LOAD", "ADDRESS", 0, "GLOBAL",
                "LINE", "DEVICE");
        doThrow(new DBException()).when(plcItemStoredTransaction).execute(plcItemStoredContext);

        plcItemStoredMessage.setOrderId("ORDER");
        plcItemStoredMessage.setLoadId("LOAD");
        plcItemStoredMessage.setAddressId("ADDRESS");
        plcItemStoredMessage.setStatus(0);
        plcItemStoredMessage.setGlobalId("GLOBAL");
        plcItemStoredMessage.setLineId("LINE");
        plcItemStoredMessage.process();

        verify(plcItemStoredTransaction).execute(plcItemStoredContext);

    }

}
