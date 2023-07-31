package com.daifukuamerica.wrxj.device.agv.messages.parsers;

import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageConstants;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageHelper;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageNameEnum;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageParseException;
import com.daifukuamerica.wrxj.device.agv.messages.AbstractMessageParser;
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
public class ERRParserImpl extends AbstractMessageParser
{
  private static final String REASONCODE_NAME = "REASONCODE";
  private static final String REASONTEXT_NAME = "REASONTEXT";

  private String msReasonCode = "";
  private String msReasonText = "";

  private Map<String, Integer> mpParsingTemplate = null;

  public ERRParserImpl()
  {
    super();
    mpParsingTemplate = new LinkedHashMap<String, Integer>(3);
    mpParsingTemplate.put(AGVMessageConstants.MESSAGEIDENTIFIER_NAME,
                          Integer.valueOf(AGVMessageConstants.MESSAGEID_LEN));
    mpParsingTemplate.put(REASONCODE_NAME,
                          Integer.valueOf(AGVMessageConstants.REASON_CODE_LEN));
    mpParsingTemplate.put(REASONTEXT_NAME,
                          Integer.valueOf(AGVMessageConstants.REASON_TEXT_LEN));
  }

 /*----------------------------------------------------------------------------
   Getters Methods in case the user of this class wants some or all of the
   parsed fields for informational purposes.
  ----------------------------------------------------------------------------*/
  public String getReasonCode()
  {
    return(msReasonCode);
  }

  public String getReasonText()
  {
    return(msReasonText);
  }

/*----------------------------------------------------------------------------
                           Interface Methods
  ----------------------------------------------------------------------------*/
  @Override
  public void clear()
  {
    super.clear();
    msReasonCode  = "";
    msReasonText  = "";
  }

  @Override
  public void parseMessage(String isMessage) throws AGVMessageParseException
  {
    parse(AGVMessageNameEnum.ERR_REQUEST_RESPONSE, mpParsingTemplate, isMessage);
  }

 /**
  * Method to set the fields for the ERR message.
  * @param isColumnName the field name.
  * @param isColumnValue the field value.
  * @return -1 if passed field is not found; 0 otherwise.
  * @throws AGVMessageParseException if there is an integer conversion problem.
  *         This usually means we received a non-integer in a field that should
  *         be one.
  */
  @Override
  protected int setField(String isColumnName, String isColumnValue) throws AGVMessageParseException
  {
    int vnRtn = 0;

    if (super.setField(isColumnName, isColumnValue) == -1)
    {
      if (isColumnName.equals(REASONCODE_NAME))
      {
        msReasonCode = isColumnValue;
      }
      else if (isColumnName.equals(REASONTEXT_NAME))
      {
        msReasonText = isColumnValue;
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
    return(vnRecvdLength == AGVMessageConstants.CONFIRMED_ERR_LEN);
  }

 /**
  * Method to create formatted logs.
  */
  @Override
  public StringBuffer getFormattedLogFields()
  {
    String vsDesc = "Error Message. Reason Code: " + msReasonCode;
    StringBuffer vpLogBuffer = AGVMessageHelper.getStandardLogFields(
                      msMessageIdentifier, vsDesc, mnMessageSequence, mnConfirmLength);
    vpLogBuffer.append("Reason Code: ").append(msReasonCode)
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Reason Text: ").append(msReasonText);

    return(vpLogBuffer);
  }
}
