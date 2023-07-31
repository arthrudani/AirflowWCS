package com.daifukuamerica.wrxj.web.core;

/**
 * @author dystout
 * Date: Sep 24, 2016
 *
 * Description: Constants for ScanUI database actions
 * 
 * TODO move this to web.properties 
 */
public interface DBConstantsWeb 
{

	/**
	 * Name of key looked up in wrxj.properties file for DB connection settings
	 */
	final String DB_NAME = "OracleDB";
	
	/**
	 * Database codes
	 */
	final Integer UITHEME_PREF = 100; 
	final Integer MESSAGEBOX_PREF = 101; 
	final Integer DEBUG_PREF = 102;
	final Integer COLUMN_PREF = 103; 
	
	final String DEFAULT_USER_ROLE = "ROLE_USER"; 


}
