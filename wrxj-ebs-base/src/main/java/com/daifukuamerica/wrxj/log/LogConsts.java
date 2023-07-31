package com.daifukuamerica.wrxj.log;

import java.io.File;

/**
 * Constants used in the {@link Logger} implementation.
 *
 * @author Stephen Kendorski
 * @version 1.0
 */
 public interface LogConsts
{
  /**
   * Constants used to define LogServer Control Events.
   */
  static final int SEND_CONTROLLER_GROUPS = 0;
  static final int SEND_LOG_LIST = 1;
  static final int SEND_LOG_ENTRY_COUNT = 2;
  static final int SEND_LOG_ENTRIES = 3;
  static final int SEND_FILTERED_LOG_ENTRY_COUNT = 4;
  static final int SEND_FILTERED_LOG_ENTRIES = 5;
  static final int SAVE_LOGS = 6;
  static final int ADD_LOG_ENTRY = 7;
  static final int FIND_TEXT = 8;
  /**
   * Separator and extension appended to log file name.
   */
  static final String LOG_FILE_EXTENSION = ".txt";
  /**
   * Separator and extension appended to log file name when log files are <i>zipped</i>.
   */
  static final String LOG_ZIP_FILE_EXTENSION = ".zip";
  /**
   * Separator and extension appended to log file name when log files are <i>un-zipped</i>.
   */
  static final String LOG_TMP_FILE_EXTENSION = ".tmp";
  /**
   * Descriptor applied to Operation logs.
   */
  static final String OPERATION_LOG_NAME = "OprLogs";
  /**
   * Descriptor applied to System logs.
   */
  static final String SYSTEM_LOG_NAME = "SysLogs";
  /**
   * Descriptor applied to Error logs.
   */
  static final String ERROR_LOG_NAME = "ErrLogs";
  /**
   * Descriptor applied to Communication logs.
   */
  static final String COMM_LOG_NAME = "ComLogs";
  /**
   * Descriptor applied to Equipment logs.
   */
  static final String EQUIPMENT_LOG_NAME = "EqtLogs";
  /**
   * Base directory path for log files.  This path is appended to
   * {@link com.daifukuamerica.wrxj.common.controller.ControllerConsts#ROOT_PATH_PROPERTY ROOT_PATH_PROPERTY},
   * the property name for the root-path name in command line parameter.
   */
  static final String LOG_PATH = "logs" + File.separator;
  /**
   * Directory for Operation log files.  The directory (and separator) is
   * appended to {@link #LOG_PATH LOG_PATH}.
   */
  static final String OPERATION_LOG_PATH = "operation" + File.separator;
  /**
   * Directory for System log files.  The directory (and separator) is
   * appended to {@link #LOG_PATH LOG_PATH}.
   */
  static final String SYSTEM_LOG_PATH = "system" + File.separator;
  /**
   * Directory for Error log files.  The directory (and separator) is
   * appended to {@link #LOG_PATH LOG_PATH}.
   */
  static final String ERROR_LOG_PATH = "error" + File.separator;
  /**
   * Directory for Communication log files.  The directory (and separator) is
   * appended to {@link #LOG_PATH LOG_PATH}.
   */
  static final String COMM_LOG_PATH = "communication" + File.separator;
  /**
   * Directory for Equipment log files.  The directory (and separator) is
   * appended to {@link #LOG_PATH LOG_PATH}.
   */
  static final String EQUIPMENT_LOG_PATH = "equipment" + File.separator;
  /**
   * Format String applied to log entry timestamp.
   */
  static final String LOG_ENTRY_DATE_TIME_FORMAT = "  HH:mm:ss.SSS   EEE  dd-MMM-yy";

  static final int MAX_LOGS = 20000;
  static final int MAX_ERROR_LOGS = 2000;
  static final int MAX_OPERATION_LOGS = 1000;
  /**
   * Constant defining a {@link LogEntry} data record that is not
   * a Communication log record.
   */
  static final int NOT_COM_LOG_DATA = 0;
  /**
   * Constant defining a {@link LogEntry} data record that is
   * a Receive Communication log record.
   */
  static final int COM_LOG_RX_DATA  = 1;
  /**
   * Constant defining a {@link LogEntry} data record that is
   * a Transmit Communication log record.
   */
  static final int COM_LOG_TX_DATA  = 2;
  //
  // Log Entry Fields common to ALL types of logs.
  //
  /**
   * Constant defining the index of the <i>sequential number</i> field in a
   * {@link LogEntry} data record.  This field is common to ALL
   * types of loggers.
   */
  static final int LOG_ENTRY_NUMBER_IDX           = 0;
  /**
   * Constant defining the index of the <i>logging Controller's unique identifier</i> field in a
   * {@link LogEntry} data record.  This field is common to ALL
   * types of loggers.
   */
  static final int LOG_ENTRY_CTLRKEY_IDX          = 1;
  /**
   * Constant defining the index of the <i>TimeStamp</i> field in a
   * {@link LogEntry} data record.  This field is common to ALL
   * types of loggers.
   */
  static final int LOG_ENTRY_TIMESTAMP_IDX        = 2;
  /**
   * Constant defining the index of the <i>String TimeStamp</i> field in a
   * {@link LogEntry} data record.  This field is common to ALL
   * types of loggers.
   */
  static final int LOG_ENTRY_TIMESTAMP_STRING_IDX = 3;
  //
  /**
   * Constant defining the total number of fields in a
   * {@link LogEntry} data record.
   */
  static final int LOG_ENTRY_MAX_FIELDS           = 8;
  //
  // Debug Log Fields
  //
  /**
   * Constant defining the index of the <i>Description Text</i> field in a
   * {@link LogEntry} data record.  This field is used in a DEBUG
   * type of logger.
   */
  static final int LOG_ENTRY_TEXT_IDX             = 4;
  //
  // System Log Fields
  //
  /**
   * Constant defining the index of the <i>logging Controller and Description Text</i> field in a
   * {@link LogEntry} data record.  This field is used in a SYSTEM
   * type of logger.
   */
  static final int LOG_ENTRY_DIRECTION_IDX        = 4;
  /**
   * Constant defining the index of the <i>Message Type</i> field in a
   * {@link LogEntry} data record.  This field is used in a SYSTEM
   * type of logger.
   */
  static final int LOG_ENTRY_MSG_TYPE_IDX         = 5;
  /**
   * Constant defining the index of the <i>Message Description Text</i> field in a
   * {@link LogEntry} data record.  This field is used in a SYSTEM
   * type of logger.
   */
  static final int LOG_ENTRY_MSG_DESC_IDX         = 6;
  //
  // Error Log Fields
  //
  /**
   * Constant defining the index of the <i>logging Controller's Name</i> field in a
   * {@link LogEntry} data record.  This field is used in a ERROR
   * type of logger.
   */
  static final int LOG_ENTRY_DEVICE_IDX           = 4;
  /**
   * Constant defining the index of the <i>Error Number</i> field in a
   * {@link LogEntry} data record.  This field is used in a ERROR
   * type of logger.
   */
  static final int LOG_ENTRY_ERROR_CODE_IDX       = 5;
  /**
   * Constant defining the index of the <i>Error Description Text</i> field in a
   * {@link LogEntry} data record.  This field is used in a ERROR
   * type of logger.
   */
  static final int LOG_ENTRY_ERROR_DESC_IDX       = 6;
  //
  // Communication Log Fields
  //
  /**
   * Constant defining the index of the <i>Rx/Tx Data Length</i> field in a
   * {@link LogEntry} data record.  This field is used in a COMMUNICATION
   * type of logger.
   */
  static final int LOG_ENTRY_DATA_COUNT_IDX       = 4;
  /**
   * Constant defining the index of the <i>Transmitted Data</i> field in a
   * {@link LogEntry} data record.  This field is used in a COMMUNICATION
   * type of logger.
   */
  static final int LOG_ENTRY_TX_DATA_IDX          = 5;
  /**
   * Constant defining the index of the <i>Received Data</i> field in a
   * {@link LogEntry} data record.  This field is used in a COMMUNICATION
   * type of logger.
   */
  static final int LOG_ENTRY_RX_DATA_IDX          = 6;
  //
  /**
   * Constant specifying that COMMUNICATION logs should be diaplayed in raw
   * HEXADECIMAL format, for example: "[41][42][31][32]", instead of "AB12".
   */
  static final int HEX_DISPLAY_FORMAT           = 0;
  /**
   * Constant specifying that COMMUNICATION logs should be diaplayed in ASCII
   * format when displayable characters exist, for example: "AB12[02][03]",
   * where [02] and [03] are control codes in the data stream (<STx> and <ETX>).
   */
  static final int ASCII_DISPLAY_FORMAT         = 1;
  /**
   * Constant specifying that COMMUNICATION logs should be diaplayed in ASCII
   * format when displayable characters exist and non-displayable characters
   * should be displayed as their ASCII control definition, for example:
   * "AB<STX><ETX>[F3]", where <STx> and <ETX> are ASCII control definitions
   * for [02] and [03], and F3] does not have an ASCII control definition.
   */
  static final int ASCII_CONTROL_DISPLAY_FORMAT = 2;
  //
  // Operation Log Fields
  //
  /**
   * Constant defining the index of the <i>logging Controller's Description Text</i> field in a
   * {@link LogEntry} data record.  This field is used in a OPERATION
   * type of logger.
   */
  static final int LOG_ENTRY_USER_IDX     = 4;
  /**
   * Constant defining the index of the <i>Category Description Text</i> field in a
   * {@link LogEntry} data record.  This field is used in a OPERATION
   * type of logger.
   */
  static final int LOG_ENTRY_OPR_TYPE_IDX = 5;
  /**
   * Constant defining the index of the <i>Category Key Text</i> field in a
   * {@link LogEntry} data record.  This field is used in a OPERATION
   * type of logger.
   */
  static final int LOG_ENTRY_OPR_KEY_IDX = 6;
  /**
   * Constant defining the index of the <i>Description Text</i> field in a
   * {@link LogEntry} data record.  This field is used in a OPERATION
   * type of logger.
   */
  static final int LOG_ENTRY_OPR_DESC_IDX = 7;
  //
  /**
   * Constant specifying a OPERATION log record category type of "NONE".
   */
  static final int OPR_NONE     = 0;
  /**
   * Constant specifying a OPERATION log record category type of "USER".
   */
  static final int OPR_USER     = 1;
  /**
   * Constant specifying a OPERATION log record category type of "DEVICE".
   */
  static final int OPR_DEVICE   = 2;
  /**
   * Constant specifying a OPERATION log record category type of "LOG".
   */
  static final int OPR_LOG      = 3;
  /**
   * Constant specifying a OPERATION log record category type of "LOAD".
   */
  static final int OPR_LOAD     = 4;
  /**
   * Constant specifying a OPERATION log record category type of "ORDER".
   */
  static final int OPR_ORDER    = 5;
  /**
   * Constant specifying a OPERATION log record category type of "ITEM".
   */
  static final int OPR_ITEM     = 6;
  /**
   * Constant specifying a OPERATION log record category type of "EMULATOR".
   */
  static final int OPR_EMULATOR = 7;
  /**
   * Constant specifying a OPERATION log record category type of "MOS"
   * (<b>M</b>onitoring and <b>O</b>perating Support <b>S</b>ystem).
   */
  static final int OPR_MOS = 8;
  /**
   * Constant specifying a OPERATION log record category type of "HOST"
   * (The Customer's System).
   */
  static final int OPR_HOST = 9;
  /**
   * Constant specifying a OPERATION log record category type of "DATA-SERVER"
   */
  static final int OPR_DSVR = 10;
  /**
   * Constant specifying a OPERATION log record category type of "ERROR"
   */
  static final int OPR_ERROR = 11;
  /**
   * A String array containing the displayable text descriptors for the OPERATION
   * caregories. The field order should correspond to the values of: {@link #OPR_NONE OPR_NONE},
   * {@link #OPR_USER OPR_USER}, {@link #OPR_DEVICE OPR_DEVICE},
   * {@link #OPR_LOG OPR_LOG}, {@link #OPR_LOAD OPR_LOAD},
   * {@link #OPR_ORDER OPR_ORDER}, {@link #OPR_ITEM OPR_ITEM},
   * {@link #OPR_EMULATOR OPR_EMULATOR}, {@link #OPR_EMULATOR OPR_MOS},
   * {@link #OPR_EMULATOR OPR_HOST} and {@link #OPR_EMULATOR OPR_ERROR}.
   */
  static final String[] OPR_LOG_TYPE_NAMES = {"NONE",
                                       "USER",
                                       "DEVICE",
                                       "LOG",
                                       "LOAD",
                                       "ORDER",
                                       "ITEM",
                                       "EMULATOR",
                                       "MOS",
                                       "HOST",
                                       "DATA-SERVER",
                                       "ERROR"};

  /**
   * An int array specifying the fields to use in a COMMUNICATION log record
   * (and their ordering). This is used in
   * {@link com.daifukuamerica.wrxj.common.log.view.CommLogFrameView CommLogFrameView}
   */
  static final int[] COM_LOG_COLUMN_FIELDS   = {LOG_ENTRY_NUMBER_IDX,
                                   LOG_ENTRY_TIMESTAMP_STRING_IDX,
                                   LOG_ENTRY_DATA_COUNT_IDX,
                                   LOG_ENTRY_TX_DATA_IDX,
                                   LOG_ENTRY_RX_DATA_IDX};
  /**
   * An int array specifying the fields to use in a SYSTEM log record
   * (and their ordering). This is used in
   * {@link com.daifukuamerica.wrxj.common.log.view.SystemLogFrameView SystemLogFrameView}
   */
  static final int[] SYS_LOG_COLUMN_FIELDS   = {LOG_ENTRY_NUMBER_IDX,
                                   LOG_ENTRY_TIMESTAMP_STRING_IDX,
                                   LOG_ENTRY_DIRECTION_IDX,
                                   LOG_ENTRY_MSG_TYPE_IDX,
                                   LOG_ENTRY_MSG_DESC_IDX,
                                   LOG_ENTRY_CTLRKEY_IDX};
  /**
   * An int array specifying the fields to use in a ERROR log record
   * (and their ordering). This is used in
   * {@link com.daifukuamerica.wrxj.common.log.view.ErrorLogFrameView ErrorLogFrameView}
   */
  static final int[] ERR_LOG_COLUMN_FIELDS   = {LOG_ENTRY_NUMBER_IDX,
                                   LOG_ENTRY_TIMESTAMP_STRING_IDX,
                                   LOG_ENTRY_DEVICE_IDX,
                                   LOG_ENTRY_ERROR_CODE_IDX,
                                   LOG_ENTRY_ERROR_DESC_IDX,
                                   LOG_ENTRY_CTLRKEY_IDX};
  /**
   * An int array specifying the fields to use in a DEBUG log record
   * (and their ordering). This is used in
   * {@link com.daifukuamerica.wrxj.common.log.view.DebugLogFrameView DebugLogFrameView}
   */
  static final int[] DBG_LOG_COLUMN_FIELDS   = {LOG_ENTRY_NUMBER_IDX,
                                   LOG_ENTRY_TIMESTAMP_IDX,
                                   LOG_ENTRY_TEXT_IDX,
                                   LOG_ENTRY_CTLRKEY_IDX};
  /**
   * An int array specifying the fields to use in a OPERATION log record
   * (and their ordering). This is used in
   * {@link com.daifukuamerica.wrxj.common.log.view.OperationLogFrameView OperationLogFrameView}
   */
  static final int[] OPR_LOG_COLUMN_FIELDS   = {LOG_ENTRY_NUMBER_IDX,
                                   LOG_ENTRY_TIMESTAMP_STRING_IDX,
                                   LOG_ENTRY_USER_IDX,
                                   LOG_ENTRY_OPR_TYPE_IDX,
                                   LOG_ENTRY_OPR_KEY_IDX,
                                   LOG_ENTRY_OPR_DESC_IDX,
                                   LOG_ENTRY_CTLRKEY_IDX};

  static final int AUTO_SAVE_LOGS_INTERVAL = 5000; // msecs
  static final int AUTO_CLEANUP_LOGS_INTERVAL = (3 * 60000) / AUTO_SAVE_LOGS_INTERVAL; // 3 minutes
  static final int AUTO_SAVE_LOG_MAX_FILE_SIZE = 0x800000;    // ~8MB
  static final int MAX_LOG_FILES_SIZE_MB = 80000000;          // ~80MB
  static final int MAX_LOG_ZIP_FILES_SIZE_MB = 250000000;     // ~250MB
  public static final String BaseLogPath="com.skdaifuku.wrxj.baselogpath";
}