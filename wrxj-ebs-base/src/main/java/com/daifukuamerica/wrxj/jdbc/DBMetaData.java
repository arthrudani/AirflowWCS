package com.daifukuamerica.wrxj.jdbc;

/**
 * Interface to insulate system from vendor specific Meta Data when the vendor
 * has implemented extensions to the JDBC Meta-Data standard.
 *
 * @author A.D.
 * @since  24-Feb-2009
 */
public interface DBMetaData
{
 /**
  * Method gets the database vendor name.
  * @return String containing vendor name.
  */
  public String getDatabaseVendorName();

 /**
  * Method to get the network name of the server running the database.
  * @return String containing name.
  */
  public String getDatabaseServerName();

 /**
  * Method to get database instance name.
  * @return String containing name.
  */
  public String getDatabaseInstanceName();

 /**
  * Method to get the full database version name.
  * @return String containing version number.
  */
  public String getDatabaseVersion();

 /**
  * Method to get the start up time of the database.
  * @return String containing database startup time.  The format of the return
  *         string is dd-MMM-yyyy HH:mm:ss.
  */
  public String getDatabaseStartupTime();
}
