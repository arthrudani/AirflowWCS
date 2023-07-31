package com.daifukuamerica.wrxj.device.agv.messages.formatters;

import com.daifukuamerica.wrxj.device.agv.AGVDBInterface;
import com.daifukuamerica.wrxj.device.agv.AGVException;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageConstants;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageFormatterException;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageNameEnum;
import com.daifukuamerica.wrxj.device.agv.messages.AbstractMessageFormatter;
import com.daifukuamerica.wrxj.device.agv.messages.FieldTemplate;

/**
 * <b>Message direction: WRx-J -----> CMS.</b> Class to format the ACK message.
 *
 * @author A.D.
 * @since  12-May-2009
 */
public class ACKFormatter extends AbstractMessageFormatter
{
  public ACKFormatter()
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
    FieldTemplate[] vapFormatTemplate = new FieldTemplate[]
    {
      new FieldTemplate(Integer.valueOf(inSequenceNumber),
                                AGVMessageConstants.SERIAL_NUMBER_LEN),
      new FieldTemplate(AGVMessageNameEnum.ACK_REQUEST_RESPONSE,
                                AGVMessageConstants.MESSAGEID_LEN)
    };

    return(format(vapFormatTemplate));
  }

  @Override
  public void postSendProcessing(AGVDBInterface ipDBInterface) throws AGVException
  {
    // Implement state machine for inbound messages we are ack'ing.
  }

 /**
  * Method to create formatted logs.
  */
  @Override
  public StringBuffer getFormattedLogFields()
  {
    return(new StringBuffer());
  }
}
