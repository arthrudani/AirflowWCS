package com.daifukuamerica.wrxj.device.agv.messages;

/**
 * Interface containing field lengths.
 *
 * @author A.D.
 * @since  12-May-2009
 */
public interface AGVMessageConstants
{
  public static final String CMS_DATETIME_FORMAT = "yyyy/MM/dd HH:mm:ss";

 /*===========================================================================
  *                          Common Field Names
  *===========================================================================*/
  public static final String MESSAGELENGTH_NAME = "MESSAGELENGTH";
  public static final String MESSAGEIDENTIFIER_NAME = "MESSAGEIDENTIFIER";
  public static final String MESSAGESEQUENCE_NAME = "MESSAGESEQUENCE";

 /*===========================================================================
  *                            Field lengths
  *===========================================================================*/
  public static final int ALARM_NUMBER_LEN     = 5;
  public static final int ALARM_STATE_LEN      = 1;
  public static final int ALARM_TEXT_LEN       = 30;
  public static final int ALARM_TYPE_LEN       = 1;
  public static final int CANCEL_STATUS_LEN    = 2;
  public static final int DATE_TIME_LEN        = 19;
  public static final int ERROR_CODE_LEN       = 1;
  public static final int ERROR_NUMBER_LEN     = 2;
  public static final int ERROR_TEXT_LEN       = 80;
  public static final int LOAD_LEN             = 20;
  public static final int LOCATION_DEPTH_LEN   = 2;
  public static final int LOCATION_HEIGHT_LEN  = 2;
  public static final int LOCATION_LEN         = 4;
  public static final int LOCATION_STATUS_LEN  = 1;
  public static final int MESSAGEID_LEN        = 3;
  public static final int MOVE_SEQUENCE_LEN    = 11;
  public static final int PICKUP_STATUS_LEN    = 1;
  public static final int REASON_CODE_LEN      = 2;
  public static final int REASON_TEXT_LEN      = 80;
  public static final int REPORT_INDICATOR_LEN = 3;
  public static final int REQUEST_TYPE_LEN     = 3;
  public static final int REQUESTID_LEN        = 10;
  public static final int VEHICLE_NUMBER_LEN   = 3;
  public static final int VEHICLE_STATUS_LEN   = 2;

 /*===========================================================================
  *        General message formatting lengths and offsets.
  *===========================================================================*/
  public static final int ETX_LEN            = 1;
  public static final int MESSAGE_LENGTH_LEN = 4;
  public static final int SERIAL_NUMBER_LEN  = 4;
  public static final int STX_LEN            = 1;
  public static final String EOL_CHAR = System.getProperty("line.separator");

 /*===========================================================================
  *            Confirmation Message Lengths (sequence + message text).
  *===========================================================================*/
  public static final int CONFIRMED_ACK_LEN  = 7;
  public static final int CONFIRMED_ALM_LEN  = 55;
  public static final int CONFIRMED_END_LEN  = 10;
  public static final int CONFIRMED_ERR_LEN  = 89;
  public static final int CONFIRMED_LAL_LEN  = 45;
  public static final int CONFIRMED_LPC_LEN  = 48;
  public static final int CONFIRMED_LSS_LEN  = 26;
  public static final int CONFIRMED_MAB_LEN  = 47;
  public static final int CONFIRMED_MRC_LEN  = 58;
  public static final int CONFIRMED_NAK_LEN  = 88;
  public static final int CONFIRMED_SSR_LEN  = 12;

 /*===========================================================================
  *                           Misc. constants.
  *===========================================================================*/
 /**
  * SYSTEM category message. These are WRx-J to CMS messages that change some
  * parameter(s) in the CMS system like station status', System Hold and release
  * status' etc.
  */
  public static final int SYSTEM_CHANGE_CATEGORY = 50;
 /**
  * Movement category message. These are WRx-J to CMS messages that are directly
  * related to moving loads in the system.  (Movement cancellation, and change
  * messages also fall in this category).
  */
  public static final int MOVE_CATEGORY = 51;

                                       // Codes related to NAK messages.
  public static final int INVALID_MESSAGE_CODE = 1;
  public static final int SKIPPED_MESSAGE_CODE = 2;
                                       // Station status' from SSR messsage.
  public static final String STATION_ENABLED  = "E";
  public static final String STATION_DISABLED = "D";
                                       // Move cancellation return status'
  public static final int MOVE_CANCEL_SUCCESS = 1;
  public static final int MOVE_CANCEL_TOOLATE = 2;
  public static final int MOVE_CANCEL_NONEXISTANT = 3;
                                       // Alarm types in ALM message.
  public static final String CONDITION_ALARM = "C";
  public static final String ERROR_ALARM     = "E";
  public static final String FATAL_ALARM     = "F";
  public static final String SYSTEM_ALARM    = "S";
                                       // Alarm status in ALM message.
  public static final String ALARM_SET   = "S";
  public static final String ALARM_RESET = "R";
                                       // Some error codes in MAB message.
  public static final int VEHICLE_FAULT = 3;
  public static final int OPERATOR_DELETED_MOVE = 9;

}
