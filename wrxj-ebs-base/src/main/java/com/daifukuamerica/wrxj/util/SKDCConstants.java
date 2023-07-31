package com.daifukuamerica.wrxj.util;

/*--------------------------------------------------*
 * This file is manually generated.                *
 *--------------------------------------------------*/

public interface SKDCConstants
{
// This matches the database output format
  final String dbOutDateFormat      = "yyyy-MM-dd";
  final String dbOutTimeStampFormat = "yyyy-MM-dd HH:mm:ss.SSS";

                                   /** Default date time format for SKTable */
  final String DEF_SKTABLE_DATETIME_FORMAT = "E MMM d HH:mm:ss z yyyy";
  final String DateFormatString = "HH:mm:ss.SSS MM-dd-yyyy";
  final String dbInDateFormat   = "HH24:MI:SS.XFF MM-dd-yyyy";
  final String DATETIME_FORMAT1 = "MM-dd-yyyy HH:mm:ss";
  final String DATETIME_FORMAT2 = "dd-MMM-yyyy HH:mm:ss";
  final String DATE_FORMAT1     = "MM-dd-yyyy";
  final String DATE_FORMAT2     = "dd-MMM-yyyy";
  final String DATE_FORMATDB2   = "dd-MM-yyyy";
  final String TIME_FORMAT      = "HH:mm:ss";
  //final String HOST_DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";
  final String HOST_DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";// MCM EBS
  final String LEGACY_HOST_DATE_FORMAT = "ddMMyyHHmmss";
  final String STATUS_DATE_FORMAT = "HH:mm:ss  EEE  MM/dd/yy";

                                       // Line separator is different between
                                       // Unix, Windows, and Mac implementations.
                                       // Use this define for portable code --A.D.
  final String EOL_CHAR = System.getProperty("line.separator");
      /** STX char. to denote message beginning.*/
  final int MESG_STX = 0x02;
      /** ETX char. to denote message end.     */
  final int MESG_ETX = 0x03;
      /** ACK char. for positive acknowledgment of message. */
  final int MESG_ACK = 0x06;
      /** NAK char. for negative acknowledgment of message. */
  final int MESG_NAK = 0x15;

  final String STX_STRING = "0x02";
  final String ETX_STRING = "0x03";
  final String ACK_STRING = "0x06";
  final String NAK_STRING = "0x15";

  final int ALL_INT = -33;
                                       // String used for filling in a choice list.
  final String ALL_STRING = "ALL";
  final String NONE_STRING = "NONE";
  final String EMPTY_VALUE = "EMPTY";
  final String NO_PREPENDER = "NO_PREPEND";
  final int DBINTERFACE  = 0;
  final int XMLINTERFACE = 1;

  final int ACTIVE_MESSAGE = 1;
  final int INACTIVE_MESSAGE = -1;

  /*
   * Standard Environment Key name for JVM identifier.
   */
  static final String JVM_IDENTIFIER_KEY = "JVMID";
  static final String JVM_SERVER_KEY = "JVMSERVER";
  static final String JVM_JMSTOPIC_KEY = "JMSTOPIC";

  /*
   * Standard Environment Key names for Machine info.
   */
  static final String MACHINE_NAME = "MACHINE";
  static final String IPADDRESS_NAME = "IPADDRESS";

  /*
   * Default System Controller Names
   */
  public static final String CONTROLLER_SERVER = "ControllerServer";
  public static final String LOG_SERVER = "LogServer";
  public static final String SYSTEM_HEALTH_MONITOR = "SystemHealthMonitor";

  /*
   * Default Required Users & Roles
   */
  public static final String ROLE_ADMINISTRATOR = "Administrator";
  public static final String ROLE_DAC_SUPERROLE = "SKDaifuku";
  public static final String ROLE_MASTER        = "Master";

  public static final String USER_ADMINISTRATOR = "Administrator";
  public static final String USER_DAC_SUPERUSER = "su";
  public static final String USER_DAC_SUPPORT   = "SKDaifuku";

  /*
   * Emulation
   */
  public static final String EMULATION_SUFFIX = "-Emulator";

  public static final String DESCRIPTION_SEPARATOR  = "-";
  /** Bad read Load Prefix   */
  public static final String BR_LOAD_PREFIX = "BR";
  /** Duplicate Load Prefix   */
  public static final String DL_LOAD_PREFIX = "DL";
  /** Bin-Full Load Prefix   */
  public static final String BF_LOAD_PREFIX = "BF";
  /** ID Pending Load prefix. */
  public static final String IP_LOAD_PREFIX = "IP";

}
