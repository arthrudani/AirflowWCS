package com.daifukuoc.wrxj.custom.ebs.dbadapter.data;

import com.daifukuamerica.wrxj.dataserver.standard.StandardConfigurationServer;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSConstants;

/**
 * This class is used to fetch all the system config data from the database
 * 
 * @author Administrator
 * 
 * 
 *         DK:30148 - Fetch location for the given retrieval date time of
 *         expected
 * 
 */

public class EBSSysConfig extends StandardConfigurationServer {

	/**
	 * This method get the no of location count associated to the standard store
	 * location. If there is any error / parameter value is not present then return
	 * the default value from constants.
	 * 
	 * @return
	 */
	public int getNoOfLocationForStandardStoreLoc() {
		return getCachedSysConfigInt(EBSConstants.SYSCONFIG_CONSTANTS.LOCATION_GROUP.GROUP_NAME,
				EBSConstants.SYSCONFIG_CONSTANTS.LOCATION_GROUP.PARAM_NAME.STANDARD,
				EBSConstants.SYSCONFIG_CONSTANTS.LOCATION_GROUP.DEFAULT_LOC_COUNT.STANDARD);
	}

	/**
	 * This method get the no of location count associated to the OOG store
	 * location. If there is any error / parameter value is not present then return
	 * the default value from constants.
	 * 
	 * @return
	 */
	public int getNoOfLocationForOOGStoreLoc() {
		return getCachedSysConfigInt(EBSConstants.SYSCONFIG_CONSTANTS.LOCATION_GROUP.GROUP_NAME,
				EBSConstants.SYSCONFIG_CONSTANTS.LOCATION_GROUP.PARAM_NAME.OOG,
				EBSConstants.SYSCONFIG_CONSTANTS.LOCATION_GROUP.DEFAULT_LOC_COUNT.OOG);
	}

	/**
	 * This method is used to get the time slice for the hour
	 * 
	 * @return
	 */
	public String getTimeSliceOfHour() {
		String hour = "";

		hour = getCachedSysConfigString(EBSConstants.SYSCONFIG_CONSTANTS.TIME_SLICE_GROUP.GROUP_NAME,
				EBSConstants.SYSCONFIG_CONSTANTS.TIME_SLICE_GROUP.TIME_SLICE.PARAM_NAME.HOUR,
				EBSConstants.SYSCONFIG_CONSTANTS.TIME_SLICE_GROUP.TIME_SLICE.DEFAULT.HOUR);

		if (hour == null || hour.isEmpty()) {
			hour = EBSConstants.SYSCONFIG_CONSTANTS.TIME_SLICE_GROUP.TIME_SLICE.DEFAULT.HOUR;
		}

		return hour;
	}

	/**
	 * This method is used to get the time slice for the minute
	 * 
	 * @return
	 */
	public String getTimeSliceOfMin() {
		String min = "";

		min = getCachedSysConfigString(EBSConstants.SYSCONFIG_CONSTANTS.TIME_SLICE_GROUP.GROUP_NAME,
				EBSConstants.SYSCONFIG_CONSTANTS.TIME_SLICE_GROUP.TIME_SLICE.PARAM_NAME.MIN,
				EBSConstants.SYSCONFIG_CONSTANTS.TIME_SLICE_GROUP.TIME_SLICE.DEFAULT.MIN);

		if (min == null || min.isEmpty()) {
			min = EBSConstants.SYSCONFIG_CONSTANTS.TIME_SLICE_GROUP.TIME_SLICE.DEFAULT.MIN;
		}

		return min;
	}

}
