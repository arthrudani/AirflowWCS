package com.daifukuoc.wrxj.custom.ebs.plc;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.daifukuamerica.wrxj.device.port.PortConsts;

class PLCPortTest extends PLCPort {
	private PLCPort plcPort;

	@BeforeEach
	void setUp() throws Exception {
		plcPort = new PLCPort();
	}

	@AfterEach
	void tearDown() throws Exception {
		plcPort = null;
	}

	@Test
	void equimentIDIsExtracted() {
		byte[] header = 
				new byte[] {
						(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 
						(byte)0xff, (byte)0xff, (byte)0x01, (byte)0x02, 
						(byte)0x03, (byte)0x04, (byte)0xff, (byte)0xff, 
						(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff};
		
		assertEquals(0x01020304, plcPort.getEquipmentIdFromHeader(header, false));
	}
	
	@Test
	void equimentIDIsExtractedWithSTX() {
		byte[] header = 
				new byte[] {
						(byte)0x02, 
						(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 
						(byte)0xff, (byte)0xff, (byte)0x01, (byte)0x02, 
						(byte)0x03, (byte)0x04, (byte)0xff, (byte)0xff, 
						(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff};
		
		assertEquals(0x01020304, plcPort.getEquipmentIdFromHeader(header, true));
	}
	
	@Test
	void messageShouldBeAbleToBeWrappedWithSETAndETXIsNecessary() {
		byte[] source = new byte[] {0x11, 0x12, 0x13};
		byte[] wrappetSource = plcPort.wrap(source);
		
		assertEquals(PortConsts.STX,  wrappetSource[0]);
		assertEquals(0x11,  wrappetSource[1]);
		assertEquals(0x12,  wrappetSource[2]);
		assertEquals(0x13,  wrappetSource[3]);
		assertEquals(PortConsts.ETX, wrappetSource[4]);
		
		assertEquals(2, wrappetSource.length - source.length);	
	}
	
	@Test
	void whenMessageHasSTXAndETXItRippsOff() {
		byte[] src = new byte[] {0x02, 0x01, 0x02, 0x03, 0x04, 0x03};
		byte[] result = plcPort.removeSTXETX(src, src.length);
		
		assertEquals(0x01,  result[0]);
		assertEquals(0x02,  result[1]);
		assertEquals(0x03,  result[2]);
		assertEquals(0x04,  result[3]);
		assertEquals(src.length, result.length);
	}
	
	@Test
	void whenMessageDoesNotHaveSTXAndETXReturnsTheReceivedDataWithoutChange() {
		byte[] src = new byte[] {0x01, 0x02, 0x03, 0x04};
		byte[] result = plcPort.removeSTXETX(src, src.length);
		
		assertEquals(0x01,  result[0]);
		assertEquals(0x02,  result[1]);
		assertEquals(0x03,  result[2]);
		assertEquals(0x04,  result[3]);	
		assertEquals(src.length, result.length);
	}
	
	@Test
	void whenMessageDoesNotHaveSTXOrETXReturnsTheReceivedDataWithoutChange() {
		byte[] src1 = new byte[] {0x02, 0x01, 0x02, 0x03, 0x04};
		byte[] src2 = new byte[] {0x01, 0x02, 0x03, 0x04, 0x03};
		byte[] result1 = plcPort.removeSTXETX(src1, src1.length);
		
		assertEquals(0x02,  result1[0]);
		assertEquals(0x01,  result1[1]);
		assertEquals(0x02,  result1[2]);
		assertEquals(0x03,  result1[3]);
		assertEquals(0x04,  result1[4]);
		assertEquals(src1.length, result1.length);

		byte[] result2 = plcPort.removeSTXETX(src2, src2.length);
		
		assertEquals(0x01,  result2[0]);
		assertEquals(0x02,  result2[1]);
		assertEquals(0x03,  result2[2]);
		assertEquals(0x04,  result2[3]);
		assertEquals(0x03,  result2[4]);
		assertEquals(src2.length, result2.length);

	}

}
