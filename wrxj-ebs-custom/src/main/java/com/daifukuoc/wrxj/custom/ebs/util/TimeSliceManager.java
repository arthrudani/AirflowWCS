package com.daifukuoc.wrxj.custom.ebs.util;

public class TimeSliceManager {

	static String hoursRange = "03";// Sys config
	static String minsRange = "00";// Sys config

	public static void main(String[] args) {
		String inTime = "06:15";

		System.out.println("Result time to filter:" + findTime(inTime));
	}

	public static String findTime(String dateTime) {
		StringBuffer resultTime = new StringBuffer();
		String hour = dateTime.substring(0, 2);
		String timeSeperator = ":";
		String min = dateTime.substring(3);

		int hoursInt, hourDiff = 0;

		int hoursSlice = Integer.parseInt(hoursRange);

		if ((hoursSlice) > 1) {
			// Hours range calculation
			hoursInt = Integer.parseInt(hour);

			hourDiff = hoursInt % hoursSlice;

			System.out.println("hours Diff:" + hourDiff);

			if (hourDiff >= 1) {
				hoursInt = hoursInt - hourDiff;
				hour = String.format("%02d", hoursInt);
			}

			min = "00";
		} else if ((hoursSlice) == 1) {
			min = "00";
		} else {
			min = "00";
			// Minute range calculation
		}

		return resultTime.append(hour).append(timeSeperator).append(min).toString();
	}

	public static String getHoursRange() {
		return hoursRange;
	}

	public static void setHoursRange(String hoursRange) {
		TimeSliceManager.hoursRange = hoursRange;
	}

	public static String getMinsRange() {
		return minsRange;
	}

	public static void setMinsRange(String minsRange) {
		TimeSliceManager.minsRange = minsRange;
	}

}
