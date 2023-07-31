package com.daifukuamerica.wrxj.device.agv.messages.parsers;

import com.daifukuamerica.wrxj.device.agv.AGVDBInterface;
import com.daifukuamerica.wrxj.device.agv.AGVException;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageConstants;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageHelper;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageNameEnum;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageParseException;
import com.daifukuamerica.wrxj.device.agv.messages.AbstractMessageParser;
import java.util.LinkedHashMap;

/**
 * Message from AGV containing AGV Station status reports. This message is
 * received for each AGV station.  The last message indicating the end of the
 * report is the END message.
 *
 * @author A.D.
 * @since  13-May-2009
 */
public class SSRParserImpl extends AbstractMessageParser
{
  private static final String SOURCESTATION_NAME = "SOURCESTATION";
  private static final String STATIONSTATUS_NAME = "STATIONSTATUS";

  private String msSourceStation = "";
  private String msStationStatus = "";

  private LinkedHashMap<String, Integer> mpParsingTemplate = null;

  public SSRParserImpl()
  {
    super();
    mpParsingTemplate = new LinkedHashMap<String, Integer>(3);
    mpParsingTemplate.put(AGVMessageConstants.MESSAGEIDENTIFIER_NAME,
                          Integer.valueOf(AGVMessageConstants.MESSAGEID_LEN));
    mpParsingTemplate.put(SOURCESTATION_NAME,
                          Integer.valueOf(AGVMessageConstants.LOCATION_LEN));
    mpParsingTemplate.put(STATIONSTATUS_NAME,
                      Integer.valueOf(AGVMessageConstants.LOCATION_STATUS_LEN));
  }

  public String getSourceStation()
  {
    return(msSourceStation);
  }

  public String getStationStatus()
  {
    return(msStationStatus);
  }

/*----------------------------------------------------------------------------
                           Interface Methods
  ----------------------------------------------------------------------------*/
  @Override
  public void clear()
  {
    super.clear();
    msSourceStation = "";  
    msStationStatus = "";  
  }

  @Override
  public void parseMessage(String isMessage) throws AGVMessageParseException
  {
    parse(AGVMessageNameEnum.SSR_RESPONSE, mpParsingTemplate,isMessage);
  }

 /**
  * Method to set the fields for the SSR message.
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

    if (super.setField(isColumnName, isColumnValue) == -1)
    {
      if (isColumnName.equals(SOURCESTATION_NAME))
      {
        msSourceStation = isColumnValue;
      }
      else if (isColumnName.equals(STATIONSTATUS_NAME))
      {
        msStationStatus = isColumnValue;
      }
      else
      {
        vnRtn = -1;
      }
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
    return(vnRecvdLength == AGVMessageConstants.CONFIRMED_SSR_LEN);
  }

  @Override
  public void processMessage(AGVDBInterface ipDBInterface) throws AGVException
  {
    super.processMessage(ipDBInterface);
    ipDBInterface.updateAGVStationStatus(msSourceStation, msStationStatus);
  }

 /**
  * Method to create formatted logs.
  */
  @Override
  public StringBuffer getFormattedLogFields()
  {
    String vsDesc = "Station Status Report. Station ID: " + msSourceStation + "  ";
    StringBuffer vpLogBuffer = AGVMessageHelper.getStandardLogFields(
                      msMessageIdentifier, vsDesc, mnMessageSequence, mnConfirmLength);
    vpLogBuffer.append("Station: ").append(msSourceStation)
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Status: ").append(msStationStatus);

    return(vpLogBuffer);
  }
}
