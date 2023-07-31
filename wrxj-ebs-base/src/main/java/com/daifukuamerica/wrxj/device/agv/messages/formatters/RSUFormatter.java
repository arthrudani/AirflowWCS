package com.daifukuamerica.wrxj.device.agv.messages.formatters;

import com.daifukuamerica.wrxj.device.agv.AGVDBInterface;
import com.daifukuamerica.wrxj.device.agv.AGVException;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageConstants;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageFormatterException;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageHelper;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageNameEnum;
import com.daifukuamerica.wrxj.device.agv.messages.AbstractMessageFormatter;
import com.daifukuamerica.wrxj.device.agv.messages.FieldTemplate;

/**
 * <b>Message direction: WRx-J -----> CMS.</b> Class to format a RSU (Resume
 * Vehicle Movement).  This message is the opposite of the HLD command.
 *
 * @author A.D.
 * @since  12-May-2009
 */
public class RSUFormatter extends AbstractMessageFormatter
{
  private int mnMesgSequence = 0;

  public RSUFormatter()
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
    mnMesgSequence = inSequenceNumber;

    FieldTemplate[] vapFormatTemplate = new FieldTemplate[]
    {
      new FieldTemplate(Integer.valueOf(inSequenceNumber),
                                AGVMessageConstants.SERIAL_NUMBER_LEN),
      new FieldTemplate(AGVMessageNameEnum.RSU_REQUEST,
                                AGVMessageConstants.MESSAGEID_LEN)
    };
    return(format(vapFormatTemplate));
  }

  @Override
  public void postSendProcessing(AGVDBInterface ipDBInterface) throws AGVException
  {
  }

  @Override
  public StringBuffer getFormattedLogFields()
  {
    String vsDesc = "Resume System AGV Movement.  ";
    StringBuffer vpLogBuffer = AGVMessageHelper.getStandardLogFields(
            AGVMessageNameEnum.RSU_REQUEST.getValue(), vsDesc, mnMesgSequence,
            mnConfirmLength);
    return(vpLogBuffer);
  }
}
