package com.daifukuamerica.wrxj.device.agv.messages.parsers;

import com.daifukuamerica.wrxj.device.agv.AGVDBInterface;
import com.daifukuamerica.wrxj.device.agv.AGVException;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageConstants;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageHelper;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageNameEnum;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageParseException;
import com.daifukuamerica.wrxj.device.agv.messages.AbstractMessageParser;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Report message from AGV system containing Alarm info. The CMS sends an alarm
 * message for each alarm currently set, followed by an END of report message.
 *
 * @author A.D.
 * @since  13-May-2009
 */
public class ALMParserImpl extends AbstractMessageParser
{
  private static final String ALARMTYPE_NAME         = "ALARMTYPE";
  private static final String ALARMNUMBER_NAME       = "ALARMNUMBER";
  private static final String ALARMTEXT_NAME         = "ALARMTEXT";
  private static final String ALARMSTATE_NAME        = "ALARMSTATE";
  private static final String VEHICLENUMBER_NAME     = "VEHICLENUMBER";
  protected static final String SOURCESTATION_NAME   = "SOURCESTATION";
  protected static final String LOCATIONHEIGHT_NAME  = "LOCATIONHEIGHT";
  protected static final String LOCATIONDEPTH_NAME   = "LOCATIONDEPTH";

  private String msAlaramType        = "";
  private String msAlarmText         = "";
  private String msAlarmState        = "";
  private String msVehicleID         = "";
  private String msSourceStation     = "";
  private int    mnAlarmNumber       = 0;
  protected int  mnLocnHeight        = 0;
  protected int  mnLocnDepth         = 0;

  private Map<String, Integer> mpParsingTemplate = null;

  public ALMParserImpl()
  {
    super();
    mpParsingTemplate = new LinkedHashMap<String, Integer>(9);
    mpParsingTemplate.put(AGVMessageConstants.MESSAGEIDENTIFIER_NAME,
                          Integer.valueOf(AGVMessageConstants.MESSAGEID_LEN));
    mpParsingTemplate.put(ALARMTYPE_NAME,
                          Integer.valueOf(AGVMessageConstants.ALARM_TYPE_LEN));
    mpParsingTemplate.put(ALARMNUMBER_NAME,
                         Integer.valueOf(AGVMessageConstants.ALARM_NUMBER_LEN));
    mpParsingTemplate.put(ALARMTEXT_NAME,
                          Integer.valueOf(AGVMessageConstants.ALARM_TEXT_LEN));
    mpParsingTemplate.put(ALARMSTATE_NAME,
                          Integer.valueOf(AGVMessageConstants.ALARM_STATE_LEN));
    mpParsingTemplate.put(VEHICLENUMBER_NAME,
                      Integer.valueOf(AGVMessageConstants.VEHICLE_NUMBER_LEN));
    mpParsingTemplate.put(SOURCESTATION_NAME,
                          Integer.valueOf(AGVMessageConstants.LOCATION_LEN));
    mpParsingTemplate.put(LOCATIONHEIGHT_NAME,
                     Integer.valueOf(AGVMessageConstants.LOCATION_HEIGHT_LEN));
    mpParsingTemplate.put(LOCATIONDEPTH_NAME,
                      Integer.valueOf(AGVMessageConstants.LOCATION_DEPTH_LEN));
  }

 /*----------------------------------------------------------------------------
   Getters Methods in case the user of this class wants some or all of the
   parsed fields for informational purposes.
  ----------------------------------------------------------------------------*/
  public String getAlaramType()
  {
    return(msAlaramType);
  }

  public String getAlarmState()
  {
    return(msAlarmState);
  }

  public String getAlarmText()
  {
    return(msAlarmText);
  }

  public String getSourceStation()
  {
    return(msSourceStation);
  }

  public String getVehicleID()
  {
    return(msVehicleID);
  }

  public int getAlarmNumber()
  {
    return(mnAlarmNumber);
  }

  public int getLocnDepth()
  {
    return(mnLocnDepth);
  }

  public int getLocnHeight()
  {
    return(mnLocnHeight);
  }

/*----------------------------------------------------------------------------
                           Interface Methods
  ----------------------------------------------------------------------------*/
  @Override
  public void clear()
  {
    super.clear();
    msAlaramType        = "";
    msAlarmText         = "";
    msAlarmState        = "";  
    msVehicleID         = "";  
    msSourceStation     = "";  
    mnAlarmNumber       = 0;   
    mnLocnHeight        = 0;  
    mnLocnDepth         = 0;
  }

  @Override
  public void parseMessage(String isMessage) throws AGVMessageParseException
  {
    parse(AGVMessageNameEnum.ALM_RESPONSE, mpParsingTemplate,isMessage);
  }

 /**
  * Method to set the fields for the ALM message.
  * @param isColumnName the field name.
  * @param isColumnValue the field value.
  * @return -1 if passed field is not found; 0 otherwise.
  * @throws AGVMessageParseException if there is an integer conversion problem.
  *         This usually means we received a non-integer in a field that should
  *         be one.
  */
  @Override
  protected int setField(String isColumnName, String isColumnValue)
            throws AGVMessageParseException
  {
    int vnRtn = 0;
    try
    {
      if (super.setField(isColumnName, isColumnValue) == -1)
      {
        if (isColumnName.equals(ALARMTYPE_NAME))
        {
          msAlaramType = isColumnValue;
        }
        else if (isColumnName.equals(ALARMNUMBER_NAME))
        {
          mnAlarmNumber = Integer.parseInt(isColumnValue);
        }
        else if (isColumnName.equals(ALARMTEXT_NAME))
        {
          msAlarmText = isColumnValue;
        }
        else if (isColumnName.equals(ALARMSTATE_NAME))
        {
          msAlarmState = isColumnValue;
        }
        else if (isColumnName.equals(VEHICLENUMBER_NAME))
        {
          msVehicleID = isColumnValue;
        }
        else if (isColumnName.equals(SOURCESTATION_NAME))
        {
          msSourceStation = isColumnValue;
        }
        else if (isColumnName.equals(LOCATIONHEIGHT_NAME))
        {
          mnLocnHeight = Integer.parseInt(isColumnValue);
        }
        else if (isColumnName.equals(LOCATIONDEPTH_NAME))
        {
          mnLocnDepth = Integer.parseInt(isColumnValue);
        }
        else
        {
          vnRtn = -1;
        }
      }
    }
    catch(NumberFormatException nfe)
    {
      throw new AGVMessageParseException("Error parsing " + isColumnName + 
                                         " Value: " + isColumnValue +
                                         " in ALM message!", nfe);
    }

    return(vnRtn);
  }

  @Override
  public boolean isValidMessageLength(String isMessage)
  {
    /*
     * The sequence number was already stripped out for use in ACK/NAK responses
     * early on.  So we need to add this length to compensate.
     */
    int vnRecvdLength = isMessage.length() + AGVMessageConstants.SERIAL_NUMBER_LEN;
    return(vnRecvdLength == AGVMessageConstants.CONFIRMED_ALM_LEN);
  }

 /**
  * {@inheritDoc}. <b>MAB-Specific:</b> notify AGC if there is a serious Alarm
  * set in the CMS system.
  * @param ipDBInterface the database interface implementation.
  * @throws AGVException if there is a database error.
  */
  @Override
  public void processMessage(AGVDBInterface ipDBInterface) throws AGVException
  {
    super.processMessage(ipDBInterface);
    mpAGCNotifier.notifyCMSAvailable(!msAlarmState.equals(AGVMessageConstants.ALARM_SET));
  }

 /**
  * Method to create formatted logs.
  */
  @Override
  public StringBuffer getFormattedLogFields()
  {
    String vsDesc = "Alarm Message. Vehicle ID: " + msVehicleID + "  ";
    StringBuffer vpLogBuffer = AGVMessageHelper.getStandardLogFields(
                      msMessageIdentifier, vsDesc, mnMessageSequence, mnConfirmLength);

    vpLogBuffer.append("Alarm Type: ").append(msAlaramType)
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Alarm Text: ").append(msAlarmText)
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Alarm State: ").append(msAlarmState)
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Vehicle ID: ").append(msVehicleID)
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Source Station: ").append(msSourceStation)
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Alarm Number: ").append(mnAlarmNumber)
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Locn. Height: ").append(Integer.toString(mnLocnHeight))
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Locn. Depth: ").append(Integer.toString(mnLocnDepth));

    return(vpLogBuffer);
  }
}
