package com.daifukuoc.wrxj.custom.ebs.plc.messages;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.activemq.artemis.shaded.org.jgroups.util.ByteBufferOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.daifukuamerica.wrxj.log.Logger;

public class PLCBaseMsgBuilderTest extends PLCBaseMsgBuilder {
	
	private static final byte MAXIMUM_UNSIGNED_BYTE = (byte)0xff;
	private static final short MAXIMUM_UNSIGNED_SHORT = (short)0xFFFF;
	private static final int MAXIMUM_UNSIGNED_INT = 0xFFFFFFFF;
	
	private PLCBaseMsgBuilder plcBaseMsgBuilder;

	@BeforeEach
	public void setUp() throws Exception {
		plcBaseMsgBuilder = new PLCBaseMsgBuilder();
		plcBaseMsgBuilder.logger = Logger.getLogger();
	}

	@AfterEach
	public void tearDown() throws Exception {
		plcBaseMsgBuilder = null;
	}

	@Test
	public void headerShouldBeParsedCorrectly() throws IOException {
		byte[] header = generateHeader((short) 1, (short) 2, (short) 103, 1234, (byte) 12, (byte) 34, (short) 56789,
				(short) 56);

		PLCMessageHeader messageHeader = plcBaseMsgBuilder.processReceivedPLCHeader(header, 16);

		assertEquals(1, messageHeader.getMsgLength());
		assertEquals(2, messageHeader.getSeqNo());
		assertEquals(103, messageHeader.getMsgType());
		assertEquals("1234", messageHeader.getEquipmentID());
		assertEquals(12, messageHeader.getHours());
		assertEquals(34, messageHeader.getMinutes());
		assertEquals(56789, messageHeader.getMilliSeconds());
		assertEquals(56, messageHeader.getMsgVersion());
	}
	
	@Test
	public void headerShouldBeParsedCorrectlyEvenIfSTXIsOnTheFirstByte() throws IOException {
		byte[] header = generateHeader((short) 0x0201, (short) 2, (short) 103, 1234, (byte) 12, (byte) 34, (short) 56789,
				(short) 56);

		PLCMessageHeader messageHeader = plcBaseMsgBuilder.processReceivedPLCHeader(header, 16);

		assertEquals(0x0201, messageHeader.getMsgLength());
		assertEquals(2, messageHeader.getSeqNo());
		assertEquals(103, messageHeader.getMsgType());
		assertEquals("1234", messageHeader.getEquipmentID());
		assertEquals(12, messageHeader.getHours());
		assertEquals(34, messageHeader.getMinutes());
		assertEquals(56789, messageHeader.getMilliSeconds());
		assertEquals(56, messageHeader.getMsgVersion());
	}
	
	@Test
	public void headerShouldBeParsedCorrectlyWithSTX() throws IOException {
		byte[] header = generateHeaderWithSTX((byte)0x02, (short) 1, (short) 2, (short) 103, 1234, (byte) 12, (byte) 34, (short) 56789,
				(short) 56);

		PLCMessageHeader messageHeader = plcBaseMsgBuilder.processReceivedPLCHeader(header, 17);

		assertEquals(1, messageHeader.getMsgLength());
		assertEquals(2, messageHeader.getSeqNo());
		assertEquals(103, messageHeader.getMsgType());
		assertEquals("1234", messageHeader.getEquipmentID());
		assertEquals(12, messageHeader.getHours());
		assertEquals(34, messageHeader.getMinutes());
		assertEquals(56789, messageHeader.getMilliSeconds());
		assertEquals(56, messageHeader.getMsgVersion());
	}
	
	@Test
	public void headerShouldBeParsedWrongWithNonSTX() throws IOException {
		byte[] header = generateHeaderWithSTX((byte)0x03, (short) 1, (short) 2, (short) 103, 1234, (byte) 12, (byte) 34, (short) 56789,
				(short) 56);

		PLCMessageHeader messageHeader = plcBaseMsgBuilder.processReceivedPLCHeader(header, 17);
        assertNull(messageHeader);
	}

	@Test
	public void headerWithTheMaximumNumberShouldBeParsedCorrectly() throws IOException {
		byte[] header = generateHeader(
				MAXIMUM_UNSIGNED_SHORT, 
				MAXIMUM_UNSIGNED_SHORT, 
				(short) 103, 
				MAXIMUM_UNSIGNED_INT, 
				MAXIMUM_UNSIGNED_BYTE, 
				MAXIMUM_UNSIGNED_BYTE, 
				MAXIMUM_UNSIGNED_SHORT,
				MAXIMUM_UNSIGNED_SHORT);

		PLCMessageHeader messageHeader = plcBaseMsgBuilder.processReceivedPLCHeader(header, 16);

		assertEquals(65535, messageHeader.getMsgLength());
		assertEquals(65535, messageHeader.getSeqNo());
		assertEquals(103, messageHeader.getMsgType());
		assertEquals("4294967295", messageHeader.getEquipmentID());
		assertEquals(255, messageHeader.getHours());
		assertEquals(255, messageHeader.getMinutes());
		assertEquals(65535, messageHeader.getMilliSeconds());
		assertEquals(65535, messageHeader.getMsgVersion());
	}

	// boundary test case for the 2nd argument.
	@Test
	public void returnsNullWnehLengthIsNot16Nor17() throws IOException {
		byte[] header = generateHeader((short) 1, (short) 2, (short) 103, 1234, (byte) 12, (byte) 34, (short) 56789,
				(short) 56);

		// does not care the given byte array, 2nd argument is the important
		PLCMessageHeader messageHeader = plcBaseMsgBuilder.processReceivedPLCHeader(header, 15);

		assertNull(messageHeader);

		// does not care the given byte array, 2nd argument is the important
		messageHeader = plcBaseMsgBuilder.processReceivedPLCHeader(header, 18);

		assertNull(messageHeader);
	}

	@Test
	public void returnsNullWhenMessageHeaderIsInvalid() throws IOException {

		byte[] header = generateHeader((short) 1, (short) 2, (short) 30, 1234, (byte) 12, (byte) 34, (short) 56789,
				(short) 56);

		PLCMessageHeader messageHeader = plcBaseMsgBuilder.processReceivedPLCHeader(header, 16);

		assertNull(messageHeader);
	}

	private byte[] generateHeader(short length, short sequenceNumber, short messageType, int deviceID, byte hour,
			byte minute, short millisecond, short messageVersion) throws IOException {
		ByteBufferOutputStream byteBufferOutputStream = new ByteBufferOutputStream(ByteBuffer.allocate(16));

		byteBufferOutputStream.writeShort(length);
		byteBufferOutputStream.writeShort(sequenceNumber);
		byteBufferOutputStream.writeShort(messageType);
		byteBufferOutputStream.writeInt(deviceID);
		byteBufferOutputStream.writeByte(hour);
		byteBufferOutputStream.writeByte(minute);
		byteBufferOutputStream.writeShort(millisecond);
		byteBufferOutputStream.writeShort(messageVersion);

		return byteBufferOutputStream.getBuffer().array();
	}
	
	private byte[] generateHeaderWithSTX(byte stx, short length, short sequenceNumber, short messageType, int deviceID, byte hour,
			byte minute, short millisecond, short messageVersion) throws IOException {
		ByteBufferOutputStream byteBufferOutputStream = new ByteBufferOutputStream(ByteBuffer.allocate(17));

		byteBufferOutputStream.write(stx);
		byteBufferOutputStream.writeShort(length);
		byteBufferOutputStream.writeShort(sequenceNumber);
		byteBufferOutputStream.writeShort(messageType);
		byteBufferOutputStream.writeInt(deviceID);
		byteBufferOutputStream.writeByte(hour);
		byteBufferOutputStream.writeByte(minute);
		byteBufferOutputStream.writeShort(millisecond);
		byteBufferOutputStream.writeShort(messageVersion);

		return byteBufferOutputStream.getBuffer().array();
	}
}
