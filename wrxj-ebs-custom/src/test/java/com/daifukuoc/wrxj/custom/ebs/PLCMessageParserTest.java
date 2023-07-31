/**
 * 
 */
package com.daifukuoc.wrxj.custom.ebs;

/**
 * This Test class use to Test the PLC message manger logics
 * @author Nalin : 2022-07-01
 */
//@ExtendWith(MockitoExtension.class)
class PLCMessageParserTest /*extends TestCase*/ {
    // FIXME: Put this in the same package where the target object is
// Commented out as not completed and failing
    
//	protected Logger mpLogger;
//	protected StandardStationServer stnServer;
//	protected StationData stdata;
//	protected PLCMessageParser plcMsgParser;
//	String msgType = "";
//
//	@BeforeEach
//	protected void setUp() throws Exception {
//		super.setUp();
//		stnServer = Factory.create(StandardStationServer.class);
//		mpLogger = Logger.getLogger();
//		plcMsgParser = Factory.create(PLCMessageParser.class);
//	}
//
//	protected void tearDown() throws Exception {
//		super.tearDown();
//	}
//
//	// Checking Parse method read the invalid station data
//	@Test
//	public void testParsevalidData() {
//		// Arrange
//		msgType = "23";
//		String plcMsg = "23,6511,1"; // Header + Body <Station+ status>
//		boolean result = plcMsgParser.parse(plcMsg, msgType);
//		assertEquals(result, true);
//	}
//
//	// Checking Parse method read the invalid station data
//	@Test
//	public void testParseWithNull() {
//		// Arrange
//		msgType = "23";
//		String plcMsg = null; // Header + Body <Station+ status>
//		boolean result = plcMsgParser.parse(plcMsg, msgType);
//		assertEquals(result, false);
//	}
//
//	// Checking Parse method read the data with empty
//	@Test
//	public void testParseWithEmptyString() {
//		// Arrange
//		msgType = "23";
//		String plcMsg = ""; // Header + Body <Station+ status>
//		// Act
//		boolean result = plcMsgParser.parse(plcMsg, msgType);
//		// Assert
//		assertEquals(result, false);
//	}
//
//	// Checking Parse method read the data with empty
//	@Test
//	public void testParseWithInvalidMessagebodyLength() {
//		// Arrange
//		msgType = "23";
//		String plcMsg = "23,2456"; // Header + Body
//		// Act
//		boolean result = plcMsgParser.parse(plcMsg, msgType);
//		// Assert
//		assertEquals(result, false);
//	}
//
//	// Checking Parse method read the correct data for the
//	// PLC_ITEM_RELEAZED_MSG_TYPE
//	@Test
//	public void testParseMsgTypeItemRelease() {
//		// Arrange
//		msgType = "24";
//		String plcMsg = "24,S42-LL1A,1,OR153765478,001122334455,6111"; // Header + Body
//		// Act
//		boolean result = plcMsgParser.parse(plcMsg, msgType);
//		// Assert
//		assertEquals(result, true);
//	}
//
//	// Checking Parse method read the correct data for the
//	// PLC_ITEM_RELEAZED_MSG_TYPE
//	@Test
//	public void testParseDataCorrectItemRelease() {
//		// Arrange
//		msgType = "24";
//		String plcMsg = "24,S42-LL1A,1,OR153765478,001122334455,6111"; // Header + Body
//		// Act
//		plcMsgParser.parse(plcMsg, msgType);
//		// Assert
//		assertEquals(plcMsgParser.getStationID(), "6111");
//		assertEquals(plcMsgParser.getOrderID(), "OR153765478");
//		assertEquals(plcMsgParser.getLoadId(), "1");
//		assertEquals(plcMsgParser.getLineId(), "001122334455");
//	}
//
//	// Checking Parse method read the correct data for the
//	// PLC_ITEM_RELEAZED_MSG_TYPE
//	@Test
//	public void testParseInvalidMsgType() {
//		// Arrange
//		msgType = "2";
//		String plcMsg = "2,S42-LL1A,1,OR153765478,001122334455,6111"; // Header + Body
//		// Act
//		boolean result = plcMsgParser.parse(plcMsg, msgType);
//		// Assert
//		assertEquals(result, false);
//	}

}
