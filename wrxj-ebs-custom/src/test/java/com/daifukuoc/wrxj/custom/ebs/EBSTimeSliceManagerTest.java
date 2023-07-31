package com.daifukuoc.wrxj.custom.ebs;

//@ExtendWith(MockitoExtension.class)
public class EBSTimeSliceManagerTest /*extends TestCase */ {
    // FIXME: Put this in the same package where the target object is
 // Commented out as not completed and failing
//	TimeSliceManager timeSliceMgr = Factory.create(TimeSliceManager.class);;
//
//	protected void setUp() throws Exception {
//		super.setUp();
//	}
//
//	protected void tearDown() throws Exception {
//		super.tearDown();
//	}
//
//	@Test
//	public void testHourRangeTc1() {
//		String inTime = "03:15";
//		timeSliceMgr.setHoursRange("01");
//		timeSliceMgr.setMinsRange("00");
//		String expectedResultTime = "03:00";
//		String resultTime = timeSliceMgr.findTime(inTime);
//		assertEquals(expectedResultTime, resultTime);
//	}
//
//	@Test
//	public void testHourRangeTc2() {
//		String inTime = "03:15";
//		timeSliceMgr.setHoursRange("02");
//		timeSliceMgr.setMinsRange("00");
//		String expectedResultTime = "02:00";
//		String resultTime = timeSliceMgr.findTime(inTime);
//		assertEquals(expectedResultTime, resultTime);
//	}
//
//	@Test
//	public void testHourRangeTc3() {
//		String inTime = "03:15";
//		timeSliceMgr.setHoursRange("03");
//		timeSliceMgr.setMinsRange("00");
//		String expectedResultTime = "03:00";
//		String resultTime = timeSliceMgr.findTime(inTime);
//		assertEquals(expectedResultTime, resultTime);
//	}
//
//	@Test
//	public void testHourRangeTc4() {
//		String inTime = "03:15";
//		timeSliceMgr.setHoursRange("04");
//		timeSliceMgr.setMinsRange("00");
//		String expectedResultTime = "00:00";
//		String resultTime = timeSliceMgr.findTime(inTime);
//		assertEquals(expectedResultTime, resultTime);
//	}
//
//	@Test
//	public void testHourRangeTc5() {
//		String inTime = "03:15";
//		timeSliceMgr.setHoursRange("05");
//		timeSliceMgr.setMinsRange("00");
//		String expectedResultTime = "00:00";
//		String resultTime = timeSliceMgr.findTime(inTime);
//		assertEquals(expectedResultTime, resultTime);
//	}
//
//	@Test
//	public void testHourRangeTc6() {
//		String inTime = "13:15";
//		timeSliceMgr.setHoursRange("01");
//		timeSliceMgr.setMinsRange("00");
//		String expectedResultTime = "13:00";
//		String resultTime = timeSliceMgr.findTime(inTime);
//		assertEquals(expectedResultTime, resultTime);
//	}
//
//	@Test
//	public void testHourRangeTc7() {
//		String inTime = "13:15";
//		timeSliceMgr.setHoursRange("02");
//		timeSliceMgr.setMinsRange("00");
//		String expectedResultTime = "12:00";
//		String resultTime = timeSliceMgr.findTime(inTime);
//		assertEquals(expectedResultTime, resultTime);
//	}
//
//	@Test
//	public void testHourRangeTc8() {
//		String inTime = "13:15";
//		timeSliceMgr.setHoursRange("03");
//		timeSliceMgr.setMinsRange("00");
//		String expectedResultTime = "12:00";
//		String resultTime = timeSliceMgr.findTime(inTime);
//		assertEquals(expectedResultTime, resultTime);
//	}
//
//	@Test
//	public void testHourRangeTc9() {
//		String inTime = "13:15";
//		timeSliceMgr.setHoursRange("04");
//		timeSliceMgr.setMinsRange("00");
//		String expectedResultTime = "12:00";
//		String resultTime = timeSliceMgr.findTime(inTime);
//		assertEquals(expectedResultTime, resultTime);
//	}
//
//	@Test
//	public void testHourRangeTc10() {
//		String inTime = "13:15";
//		timeSliceMgr.setHoursRange("05");
//		timeSliceMgr.setMinsRange("00");
//		String expectedResultTime = "10:00";
//		String resultTime = timeSliceMgr.findTime(inTime);
//		assertEquals(expectedResultTime, resultTime);
//	}
//
//	@Test
//	public void testHourRangeTc11() {
//		String inTime = "13:15";
//		timeSliceMgr.setHoursRange("06");
//		timeSliceMgr.setMinsRange("00");
//		String expectedResultTime = "12:00";
//		String resultTime = timeSliceMgr.findTime(inTime);
//		assertEquals(expectedResultTime, resultTime);
//	}
//
//	@Test
//	public void testHourRangeTc12() {
//		String inTime = "13:15";
//		timeSliceMgr.setHoursRange("07");
//		timeSliceMgr.setMinsRange("00");
//		String expectedResultTime = "07:00";
//		String resultTime = timeSliceMgr.findTime(inTime);
//		assertEquals(expectedResultTime, resultTime);
//	}
//
//	@Test
//	public void testHourRangeTc13() {
//		String inTime = "13:15";
//		timeSliceMgr.setHoursRange("12");
//		timeSliceMgr.setMinsRange("00");
//		String expectedResultTime = "12:00";
//		String resultTime = timeSliceMgr.findTime(inTime);
//		assertEquals(expectedResultTime, resultTime);
//	}
//
//	@Test
//	public void testMinRangeTc1() {
//		String inTime = "03:15";
//		timeSliceMgr.setHoursRange("00");
//		timeSliceMgr.setMinsRange("01");
//		String expectedResultTime = "03:15";
//		String resultTime = timeSliceMgr.findTime(inTime);
//		assertEquals(expectedResultTime, resultTime);
//	}
//
//	@Test
//	public void testMinRangeTc2() {
//		String inTime = "03:15";
//		timeSliceMgr.setHoursRange("00");
//		timeSliceMgr.setMinsRange("02");
//		String expectedResultTime = "03:14";
//		String resultTime = timeSliceMgr.findTime(inTime);
//		assertEquals(expectedResultTime, resultTime);
//	}
//
//	@Test
//	public void testMinRangeTc3() {
//		String inTime = "03:15";
//		timeSliceMgr.setHoursRange("00");
//		timeSliceMgr.setMinsRange("03");
//		String expectedResultTime = "03:15";
//		String resultTime = timeSliceMgr.findTime(inTime);
//		assertEquals(expectedResultTime, resultTime);
//	}
//
//	@Test
//	public void testMinRangeTc4() {
//		String inTime = "03:15";
//		timeSliceMgr.setHoursRange("00");
//		timeSliceMgr.setMinsRange("04");
//		String expectedResultTime = "03:12";
//		String resultTime = timeSliceMgr.findTime(inTime);
//		assertEquals(expectedResultTime, resultTime);
//	}
//
//	@Test
//	public void testMinRangeTc5() {
//		String inTime = "03:15";
//		timeSliceMgr.setHoursRange("00");
//		timeSliceMgr.setMinsRange("05");
//		String expectedResultTime = "03:15";
//		String resultTime = timeSliceMgr.findTime(inTime);
//		assertEquals(expectedResultTime, resultTime);
//	}
//
//	@Test
//	public void testMinRangeTc6() {
//		String inTime = "03:15";
//		timeSliceMgr.setHoursRange("00");
//		timeSliceMgr.setMinsRange("10");
//		String expectedResultTime = "03:10";
//		String resultTime = timeSliceMgr.findTime(inTime);
//		assertEquals(expectedResultTime, resultTime);
//	}
//
//	@Test
//	public void testMinRangeTc7() {
//		String inTime = "03:25"; // 03:00-03:15, 03:16-03:30, 03:31-03:45, 03:46-03
//		timeSliceMgr.setHoursRange("00");
//		timeSliceMgr.setMinsRange("15");
//		String expectedResultTime = "03:15";
//		String resultTime = timeSliceMgr.findTime(inTime);
//		assertEquals(expectedResultTime, resultTime);
//	}
//
//	@Test
//	public void testMinRangeTc8() {
//		String inTime = "03:25";
//		timeSliceMgr.setHoursRange("00");
//		timeSliceMgr.setMinsRange("20");
//		String expectedResultTime = "03:20";
//		String resultTime = timeSliceMgr.findTime(inTime);
//		assertEquals(expectedResultTime, resultTime);
//	}
//
//	@Test
//	public void testMinRangeTc9() {
//		String inTime = "03:25";
//		timeSliceMgr.setHoursRange("00");
//		timeSliceMgr.setMinsRange("30");
//		String expectedResultTime = "03:00";
//		String resultTime = timeSliceMgr.findTime(inTime);
//		assertEquals(expectedResultTime, resultTime);
//	}
}
