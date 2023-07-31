package com.daifukuamerica.wrxj.device.agv.messages.parsers;

import com.daifukuamerica.wrxj.device.agv.AGVDBInterface;
import com.daifukuamerica.wrxj.device.agv.AGVException;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageConstants;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageHelper;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageNameEnum;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageParseException;
import com.daifukuamerica.wrxj.device.agv.messages.AbstractMessageParser;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Message from AGV containing AGV Station status reports. This message is
 * received for each AGV station.  The last message indicating the end of the
 * report is the END message.
 *
 * @author A.D.
 * @since  13-May-2009
 */
public class LSSParserImpl extends AbstractMessageParser
{
  private static final String DATETIME_NAME = "DATETIME";
  private SimpleDateFormat mpSDF = new SimpleDateFormat(AGVMessageConstants.CMS_DATETIME_FORMAT);

  private Date mpDateTime            = new Date();
  private Map<String, Integer> mpParsingTemplate = null;

  public LSSParserImpl()
  {
    super();
    mpParsingTemplate = new LinkedHashMap<String, Integer>(1);
    mpParsingTemplate.put(AGVMessageConstants.MESSAGEIDENTIFIER_NAME, Integer.valueOf(AGVMessageConstants.MESSAGEID_LEN));
    mpParsingTemplate.put(DATETIME_NAME, Integer.valueOf(AGVMessageConstants.DATE_TIME_LEN));
  }

/*----------------------------------------------------------------------------
                           Interface Methods
  ----------------------------------------------------------------------------*/
  @Override
  public void clear()
  {
    super.clear();
    mpDateTime.setTime(System.currentTimeMillis());
  }

  @Override
  public boolean isLSSMessage()
  {
    return(true);
  }

  @Override
  public void parseMessage(String isMessage) throws AGVMessageParseException
  {
    parse(AGVMessageNameEnum.LSS_REQUEST_RESPONSE, mpParsingTemplate,isMessage);
  }

 /**
  * Method to set the fields for the LSS message.
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
      if (isColumnName.equals(DATETIME_NAME))
      {
        try
        {
          mpDateTime= mpSDF.parse(isColumnValue);
        }
        catch(ParseException e)
        {
          throw new AGVMessageParseException("Error parsing date-time field from CMS. " +
                                             "Field value: " + isColumnValue, e);
        }
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
    return(vnRecvdLength == AGVMessageConstants.CONFIRMED_LSS_LEN);
  }

 /**
  * CMS wants us to sync. to their sequence number.  Update database accordingly.
  * @param ipDBInterface reference to database api impl.
  * @throws AGVException if there is an update problem.
  */
  @Override
  public void processMessage(AGVDBInterface ipDBInterface) throws AGVException
  {
    ipDBInterface.resyncInboundSequence(mnMessageSequence);
  }

 /**
  * Method to create formatted logs.
  */
  @Override
  public StringBuffer getFormattedLogFields()
  {
    String vsDesc = "Link Startup Synchronization. ";
    StringBuffer vpLogBuffer = AGVMessageHelper.getStandardLogFields(
                      msMessageIdentifier, vsDesc, mnMessageSequence, mnConfirmLength);
    vpLogBuffer.append("CMS Date/Time: ").append(mpDateTime);

    return(vpLogBuffer);
  }
}
