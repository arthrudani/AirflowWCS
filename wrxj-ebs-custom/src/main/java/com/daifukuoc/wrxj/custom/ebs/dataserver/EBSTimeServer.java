package com.daifukuoc.wrxj.custom.ebs.dataserver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dataserver.standard.StandardServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardUserServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.TimeSlotConfig;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.TimeSlotConfigData;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.TimeSlotConfigEnum;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.util.TimeSliceManager;

public class EBSTimeServer extends StandardServer {

	protected String msMyClass = null;
	protected StandardUserServer mpUserServer = null;
	protected TimeSlotConfig mpEBSTimeSlot = Factory.create(TimeSlotConfig.class);
	protected TimeSlotConfigData timeSlotData = Factory.create(TimeSlotConfigData.class);
	protected TimeSliceManager timeSliceManager = Factory.create(TimeSliceManager.class);
	protected SimpleDateFormat parseDateFormat = null;
	protected SimpleDateFormat parseTimeFormat = null;

	/**
	 * Constructor w/o key name
	 */
	public EBSTimeServer() {
		this(null);
	}

	/**
	 * Constructor with key name
	 *
	 * @param isKeyName
	 */
	public EBSTimeServer(String isKeyName) {
		super(isKeyName);
		logDebug("Creating " + getClass().getSimpleName());

	}

	/**
	 * Web application constructor for per user connection pooling
	 * 
	 * @param keyName
	 * @param dbo
	 */
	public EBSTimeServer(String keyName, DBObject dbo) {
		super(keyName, dbo);
		logDebug("Creating " + getClass().getSimpleName());

	}

	protected void initializeUserServer() {
		if (mpUserServer == null) {
			mpUserServer = Factory.create(StandardUserServer.class, msMyClass);
		}
	}

	/**
	 * Shuts down this controller by canceling any timers and shutting down the
	 * Equipment.
	 */
	@Override
	public void cleanUp() {
		super.cleanUp();
	}

	public void addTimeSlot(String startTime, String schemaId) throws DBException {
		TransactionToken tt = null;
		try {
			tt = startTransaction();

			TimeSlotConfigData mpEBSTimeSlotData = Factory.create(TimeSlotConfigData.class);
			String maxTimeslotId = " (SELECT ISNULL(MAX(iTimeslotID) + 1, 1) FROM TIMESLOTCONFIG WHERE  [iSchemaID] ="
					+ schemaId + ") ";
			String timeSlotName = "TIMESLOT_" + startTime;

			mpEBSTimeSlotData.setTimeslotID(maxTimeslotId);
			mpEBSTimeSlotData.setSchemaID(schemaId);
			mpEBSTimeSlotData.setName(timeSlotName);
			mpEBSTimeSlotData.setStartTime(startTime);

			mpEBSTimeSlot.addTimeSlot(mpEBSTimeSlotData);
			commitTransaction(tt);

		} catch (Exception e) {
			logException("Error adding [" + startTime + "] for SchemaID : " + schemaId, e);
		} finally {
			endTransaction(tt);
		}
	}

	/**
	 * Method to delete a Time Slot.
	 *
	 * @param schemaId.
	 * @param timeSlot.
	 * @exception DBException
	 */
	public void deleteTimeSlot(String startTime, String schemaId) throws DBException {
		TransactionToken tt = null;
		try {
			tt = startTransaction();

			TimeSlotConfigData mpEBSTimeSlotData = Factory.create(TimeSlotConfigData.class);
			List<Map> vpList = mpEBSTimeSlot.getTimeSlotAndSchemaId(schemaId, startTime);

			if (vpList.size() == 1) {

				for (Map vpRowMap : vpList) {
					String vsTimeslotId = DBHelper.getStringField(vpRowMap, TimeSlotConfigEnum.TIMESLOT_ID.getName());
					System.out.println("IID- " + vsTimeslotId);

					mpEBSTimeSlotData.setKey(TimeSlotConfigData.TIMESLOTID, Integer.parseInt(vsTimeslotId));
					mpEBSTimeSlot.deleteTimeSlot(mpEBSTimeSlotData);
				}
			}
			commitTransaction(tt);

		} catch (Exception e) {
			logException("Error while deleting [" + startTime + "] for SchemaID : " + schemaId, e);
		} finally {
			endTransaction(tt);
		}
	}

	/**
	 * Method to fetch the a Time Slot by date return List of time slot that comes
	 * under
	 * 
	 * @param timeSlot.
	 */
	@SuppressWarnings("rawtypes")
	public List<Map> getTimeSlotbyTime(String flightTime) throws ParseException, DBException {

		String flightime = flightTime; // "03:30"; //test #flightTime
		parseDateFormat = new SimpleDateFormat(SACControlMessage.DATE_FORMAT);
		Date flightDate = parseDateFormat.parse(flightime);

		parseTimeFormat = new SimpleDateFormat(SACControlMessage.TIME_FORMAT);
		Date maxHourDate = getMaxOrMinHourDate(flightDate, 1);
		String maxHour = parseTimeFormat.format(maxHourDate);

		Date minHourDate = getMaxOrMinHourDate(flightDate, -1);
		String minHour = parseTimeFormat.format(minHourDate);

		List<Map> dates = mpEBSTimeSlot.getTimeSlotbyStartTime(minHour, maxHour);
		List<Map> nearestDateList = getNearestDate(dates, flightDate);
		return nearestDateList;
	}

	// DK:30148 - Fetch timeslot for the expected receipt.
	/**
	 * This method fetchs all the time slot order by the start time.
	 * 
	 * @param retrvDateTime
	 * @return
	 * @throws ParseException
	 * @throws DBException
	 */
	public TimeSlotConfigData getTopRowTimeSlotListbyTime(String retrvDateTime) throws ParseException, DBException {
		// This is for future purpose.
		parseDateFormat = new SimpleDateFormat(SACControlMessage.DATE_FORMAT);
		Date retrvDateWithTime = parseDateFormat.parse(retrvDateTime);

		parseTimeFormat = new SimpleDateFormat(SACControlMessage.TIME_FORMAT);
		String retrvTime = parseTimeFormat.format(retrvDateWithTime);

		return mpEBSTimeSlot.getFirstRowTimeSlotbyStartTime(retrvTime);
	}

	/**
	 * Method is to check whether if there is a timeslot available for the given
	 * expiration date time of the flight
	 * 
	 * @throws DBException
	 * @throws ParseException
	 */
	@SuppressWarnings("rawtypes")
	public boolean isTimeSlotPresentForGivenTime(String retrvDateTime) throws ParseException, DBException {
		boolean isTimeSlotPresent = false;
		List<Map> timeSlotList = getTimeSlotbyTime(retrvDateTime);
		if (timeSlotList.size() > 0 && timeSlotList.contains(retrvDateTime)) {
			isTimeSlotPresent = true;
		}
		return isTimeSlotPresent;
	}
	// DK:30075 - Creating timeslot for the expected receipt.

	/**
	 * this is temp method that need to re design the logic to sent back the nearest
	 * date return Nearest date to the flight date with time
	 * 
	 * @param dataList   : List of TimeSlot date from the DB
	 * @param flightDate : flight date
	 */
	@SuppressWarnings("rawtypes")
	private List<Map> getNearestDate(List<Map> dataList, Date flightDate) throws ParseException {

		long currentTime = flightDate.getTime();
		List<Map> matchedDataList = new ArrayList<Map>();
		List<Long> calDateList = new ArrayList<Long>();

		for (Map vpRowMap : dataList) {
			String startTime = DBHelper.getStringField(vpRowMap, "SSTARTTIME");
			parseTimeFormat = new SimpleDateFormat(SACControlMessage.TIME_FORMAT);
			Date timeSlotDate = parseTimeFormat.parse(startTime);

			long diff = Math.abs(currentTime - timeSlotDate.getTime());
			if (!calDateList.contains(diff)) {
				calDateList.add(diff);
				matchedDataList.add(vpRowMap);
			}
		}

		int minIndex = calDateList.indexOf(Collections.min(calDateList));
		Map returnDate = matchedDataList.get(minIndex);
		return matchedDataList;
	}

	/**
	 * Method to create the additional hours after the flight time return date
	 * 
	 * @param date      : flight date
	 * @param hourCount : no of hours need to add or minus
	 */
	private Date getMaxOrMinHourDate(Date date, int hourCount) {
		logDebug("call getMaxOrMinHourDate with Date:  " + date + " hourCount : " + hourCount);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.HOUR_OF_DAY, hourCount);
		logDebug("return MaxOrMinHourDate :  " + calendar.getTime());

		return calendar.getTime();
	}

}
