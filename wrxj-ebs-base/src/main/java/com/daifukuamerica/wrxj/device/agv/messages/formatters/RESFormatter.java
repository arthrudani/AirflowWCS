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
 * <b>Message direction: WRx-J -----> CMS.</b>  Class to format the RES message.
 * This message clears/resets the AGV command buffer.
 *
 * @author A.D.
 * @since  12-May-2009
 */
public class RESFormatter extends AbstractMessageFormatter
{
  private int mnMesgSequence = 0;

  public RESFormatter()
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
      new FieldTemplate(AGVMessageNameEnum.RES_REQUEST,
                                AGVMessageConstants.MESSAGEID_LEN),
    };

    return(format(vapFormatTemplate));
  }

 /**
  * Method to delete any moves that have been sent to them (but not responded 
  * to yet)since they will clean out their work queues of any command not
  * started yet.
  * @param ipDBInterface the database interface reference.
  * @throws AGVException for DB errors.
  */
  @Override
  public void postSendProcessing(AGVDBInterface ipDBInterface) throws AGVException
  {
  }

 @Override
  public StringBuffer getFormattedLogFields()
  {
    String vsDesc = "Reset/Clear Queued Move Requests.  ";
    StringBuffer vpLogBuffer = AGVMessageHelper.getStandardLogFields(
            AGVMessageNameEnum.RES_REQUEST.getValue(), vsDesc, mnMesgSequence,
            mnConfirmLength);
    
    return(vpLogBuffer);
  }
}
