package com.daifukuamerica.wrxj.device.agv.messages.formatters;

import com.daifukuamerica.wrxj.device.agv.AGVDBInterface;
import com.daifukuamerica.wrxj.device.agv.AGVException;
import com.daifukuamerica.wrxj.device.agv.messages.AGVData;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageConstants;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageFormatterException;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageHelper;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageNameEnum;
import com.daifukuamerica.wrxj.device.agv.messages.AbstractMessageFormatter;
import com.daifukuamerica.wrxj.device.agv.messages.FieldTemplate;

/**
 * <b>Message direction: WRx-J -----> CMS.</b> Class to format a XMT message.
 * This message commands the CMS to report on system status.  The Report Type or
 * Report Indicator specifies the type of response report to send.
 * 
 * @author A.D.
 * @since  12-May-2009
 */
public class XMTFormatter extends AbstractMessageFormatter
{
  private AGVData mpData;

  public XMTFormatter()
  {
    super();
  }

/*----------------------------------------------------------------------------
                           Interface Methods
  ----------------------------------------------------------------------------*/
  @Override
  public String format(int inSequenceNumber, AGVDBInterface ipDBInterface)
         throws AGVMessageFormatterException, AGVException
  {
    mpData = ipDBInterface.getData(inSequenceNumber);
    if (mpData == null) return("");

    FieldTemplate[] vapFormatTemplate = new FieldTemplate[]
    {
      new FieldTemplate(Integer.valueOf(inSequenceNumber),
                                AGVMessageConstants.SERIAL_NUMBER_LEN),
      new FieldTemplate(AGVMessageNameEnum.XMT_REQUEST,
                                AGVMessageConstants.MESSAGEID_LEN),
      new FieldTemplate(mpData.getCommandValue(),
                                AGVMessageConstants.REPORT_INDICATOR_LEN)
    };

    return(format(vapFormatTemplate));
  }

  @Override
  public void postSendProcessing(AGVDBInterface ipDBInterface) throws AGVException
  {
    ipDBInterface.updateAGVSystemCmdStatus(mpData.getSequenceNumber(),
                                           AGVDBInterface.SYSCMD_SENT);
  }

  @Override
  public StringBuffer getFormattedLogFields()
  {
    String vsDesc = "Transmit Report. " + mpData.getCommandValue() + "  ";
    StringBuffer vpLogBuffer = AGVMessageHelper.getStandardLogFields(
            AGVMessageNameEnum.XMT_REQUEST.getValue(), vsDesc,
            mpData.getSequenceNumber(), mnConfirmLength);
    
    vpLogBuffer.append("Report Indicator: ").append(mpData.getCommandValue());

    return(vpLogBuffer);
  }
}
