package com.daifukuamerica.wrxj.device.agv.messages.formatters;

import com.daifukuamerica.wrxj.device.agv.AGVDBInterface;
import com.daifukuamerica.wrxj.device.agv.AGVException;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageConstants;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageFormatterException;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageHelper;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageNameEnum;
import com.daifukuamerica.wrxj.device.agv.messages.AbstractMessageFormatter;
import com.daifukuamerica.wrxj.device.agv.messages.FieldTemplate;
import java.util.Date;

/**
 * <b>Message direction: WRx-J -----> CMS.</b> Class to format the HLD message.
 * This message instructs CMS to Hold all AGV vehicles in the system.  Upon
 * receiving this command the CMS will direct all AGVs to Hold Stations.
 * Previously sent commands will be suspended.
 *
 * @author A.D.
 * @since  12-May-2009
 */
public class LSSFormatter extends AbstractMessageFormatter
{
  private int mnMesgSequence = 0;
  private Date mdDateTime = new Date();

  public LSSFormatter()
  {
    super();
  }

 /*----------------------------------------------------------------------------
                           Setter Methods
  ----------------------------------------------------------------------------*/
  public void setDateTime(Date idDateTime)
  {
    mdDateTime = idDateTime;
  }

/*----------------------------------------------------------------------------
                           Interface Methods
  ----------------------------------------------------------------------------*/
  @Override
  public String format(int inSequenceNumber, AGVDBInterface ipDBInterface)
         throws AGVMessageFormatterException, AGVException
  {
    mnMesgSequence = ipDBInterface.getFormatSequenceNumber();

    FieldTemplate[] vapFormatTemplate = new FieldTemplate[]
    {
      new FieldTemplate(Integer.valueOf(mnMesgSequence), AGVMessageConstants.SERIAL_NUMBER_LEN),
      new FieldTemplate(AGVMessageNameEnum.LSS_REQUEST_RESPONSE, AGVMessageConstants.MESSAGEID_LEN),
      new FieldTemplate(mdDateTime, AGVMessageConstants.DATE_TIME_LEN)
    };
    
    return(format(vapFormatTemplate));
  }

  @Override
  public void postSendProcessing(AGVDBInterface ipDBInterface) throws AGVException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public StringBuffer getFormattedLogFields()
  {
    String vsDesc = "Link Startup Synchronization.  ";
    StringBuffer vpLogBuffer = AGVMessageHelper.getStandardLogFields(
            AGVMessageNameEnum.LSS_REQUEST_RESPONSE.getValue(), vsDesc,
            mnMesgSequence, mnConfirmLength);
    vpLogBuffer.append("Wrx Date/Time: ").append(mdDateTime);

    return(vpLogBuffer);
  }
}
