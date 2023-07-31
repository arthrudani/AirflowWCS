
package com.daifukuamerica.wrxj.device.agv.messages.parsers;

import com.daifukuamerica.wrxj.device.agv.AGVDBInterface;
import com.daifukuamerica.wrxj.device.agv.AGVException;
import com.daifukuamerica.wrxj.device.agv.messages.AGVData;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageConstants;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageNameEnum;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageParseException;
import com.daifukuamerica.wrxj.device.agv.messages.AbstractMessageParser;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ACK message parser.
 * @author A.D.
 * @since  13-May-2009
 */
public class ACKParserImpl extends AbstractMessageParser
{
  private Map<String, Integer> mpParsingTemplate = null;

  public ACKParserImpl()
  {
    super();
    mpParsingTemplate = new LinkedHashMap<String, Integer>(3);
    mpParsingTemplate.put(AGVMessageConstants.MESSAGEIDENTIFIER_NAME,
                          Integer.valueOf(AGVMessageConstants.MESSAGEID_LEN));
  }

/*----------------------------------------------------------------------------
                           Interface Methods
  ----------------------------------------------------------------------------*/
  @Override
  public void clear()
  {
    super.clear();
  }

  @Override
  public boolean isAckNakMessage()
  {
    return(true);
  }

  @Override
  public void parseMessage(String isMessage) throws AGVMessageParseException
  {
    parse(AGVMessageNameEnum.ERR_REQUEST_RESPONSE, mpParsingTemplate, isMessage);
  }

 @Override
  public boolean isValidMessageLength(String isMessage)
  {
    /*
     * The sequence number was already stripped out for use in ACK/NAK responses
     * early on.  So we need to add this length to compensate.
     */
    int vnRecvdLength = isMessage.length() + AGVMessageConstants.SERIAL_NUMBER_LEN;
    return(vnRecvdLength == AGVMessageConstants.CONFIRMED_ACK_LEN);
  }

  @SuppressWarnings("incomplete-switch")
  @Override
  public void processMessage(AGVDBInterface ipDBInterface) throws AGVException
  {
    // TODO: implement state machine for acks coming back to us for commands
    // we send them.
    int vnSeq = getMessageSequence();
    AGVData vpAGVData = ipDBInterface.getData(vnSeq);
    if (vpAGVData != null)
    {           // Get the original message name for which we received the ack
      switch(vpAGVData.getMessageName())
      {
        case MOV_REQUEST:
          ipDBInterface.updateAGVMoveStatus(vnSeq,
                                            AGVDBInterface.LOAD_MOVE_PENDING);
          break;

        case CAN_REQUEST:
          ipDBInterface.updateAGVMoveStatus(vnSeq,
                                       AGVDBInterface.LOAD_MOVE_CANCEL_PENDING);
          break;

        case XMT_REQUEST:
        case PIC_REQUEST:
        case HLD_REQUEST:
        case RES_REQUEST:
        case RSU_REQUEST:
          ipDBInterface.updateAGVSystemCmdStatus(vnSeq, AGVDBInterface.SYSCMD_PENDING);
          break;
      }
    }
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
