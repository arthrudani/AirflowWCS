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
 * <b>Message direction: WRx-J -----> CMS.</b>  Class to format the PIC 
 * (Pickup Control) message.  This message enables or disables the pickup cycle
 * of the AGV.
 *
 * @author A.D.
 * @since  12-May-2009
 */
public class PICFormatter extends AbstractMessageFormatter
{
  private AGVData mpData;

  public PICFormatter()
  {
    super();
  }

/*----------------------------------------------------------------------------
                           Interface Methods
  ----------------------------------------------------------------------------*/
  @Override
  public String format(int inSequenceNumber,AGVDBInterface ipDBInterface)
         throws AGVMessageFormatterException, AGVException
  {
    mpData = ipDBInterface.getData(inSequenceNumber);
    if (mpData == null) return("");

    FieldTemplate[] vapFormatTemplate = new FieldTemplate[]
    {
      new FieldTemplate(Integer.valueOf(inSequenceNumber),
                                AGVMessageConstants.SERIAL_NUMBER_LEN),
      new FieldTemplate(AGVMessageNameEnum.PIC_REQUEST,
                                AGVMessageConstants.MESSAGEID_LEN),
      new FieldTemplate(mpData.getCommandValue(),
                                AGVMessageConstants.PICKUP_STATUS_LEN)
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
    String vsDesc = "System Pickup Enable/Disable.  ";
    StringBuffer vpLogBuffer = AGVMessageHelper.getStandardLogFields(
            AGVMessageNameEnum.PIC_REQUEST.getValue(), vsDesc,
            mpData.getSequenceNumber(), mnConfirmLength);
    vpLogBuffer.append("Pickup Status: ").append(mpData.getCommandValue());

    return(vpLogBuffer);
  }
}
