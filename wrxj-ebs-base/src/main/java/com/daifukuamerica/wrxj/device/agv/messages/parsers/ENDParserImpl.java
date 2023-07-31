
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
 * Report message from AGV system. This is the last message indicating the end
 * of the report.
 *
 * @author A.D.
 * @since  13-May-2009
 */
public class ENDParserImpl extends AbstractMessageParser
{
  private static final String REPORTINDICATOR_NAME   = "REPORTINDICATOR";
  private String msReportIndicator   = "";
  
  private Map<String, Integer> mpParsingTemplate = null;

  public ENDParserImpl()
  {
    super();
    mpParsingTemplate = new LinkedHashMap<String, Integer>(2);
    mpParsingTemplate.put(AGVMessageConstants.MESSAGEIDENTIFIER_NAME, Integer.valueOf(AGVMessageConstants.MESSAGEID_LEN));
    mpParsingTemplate.put(REPORTINDICATOR_NAME, Integer.valueOf(AGVMessageConstants.REPORT_INDICATOR_LEN));
  }

 /*----------------------------------------------------------------------------
   Getters Methods in case the user of this class wants some or all of the
   parsed fields for informational purposes.
  ----------------------------------------------------------------------------*/
  public String getReportIndicator()
  {
    return(msReportIndicator);
  }

/*----------------------------------------------------------------------------
                           Interface Methods
  ----------------------------------------------------------------------------*/
  @Override
  public void clear()
  {
    super.clear();
    msMessageIdentifier = "";
    msReportIndicator   = "";
  }

  @Override
  public void parseMessage(String isMessage) throws AGVMessageParseException
  {
    parse(AGVMessageNameEnum.END_RESPONSE, mpParsingTemplate,isMessage);
  }

 /**
  * Method to set the fields for the END message.
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
      if (isColumnName.equals(REPORTINDICATOR_NAME))
      {
        msReportIndicator = isColumnValue;
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
    return(vnRecvdLength == AGVMessageConstants.CONFIRMED_END_LEN);
  }

  @Override
  public void processMessage(AGVDBInterface ipDBInterface) throws AGVException
  {
    AGVMessageNameEnum vpReportName = AGVMessageNameEnum.getEnumObject(
                                                            msReportIndicator);
    if (vpReportName != null)
    {
      super.processMessage(ipDBInterface);
      ipDBInterface.updateAGVSystemCmdStatus(
                                     AGVMessageNameEnum.XMT_REQUEST.getValue(),
                                     vpReportName.getValue(),
                                     AGVDBInterface.SYSCMD_COMPLETE);
    }
    else
    {
      throw new AGVException("Invalid report indicator found in END " +
                             "report message! ", AGVDBInterface.DATA_NOT_FOUND);
    }
  }

 /**
  * Method to create formatted logs.
  */
  @Override
  public StringBuffer getFormattedLogFields()
  {
    String vsDesc = "End Report. " + msReportIndicator + "  ";
    StringBuffer vpLogBuffer = AGVMessageHelper.getStandardLogFields(
                      msMessageIdentifier, vsDesc, mnMessageSequence, mnConfirmLength);
    vpLogBuffer.append("Report Indicator: ").append(msReportIndicator)
               .append(AGVMessageConstants.EOL_CHAR);

    return(vpLogBuffer);
  }
}
