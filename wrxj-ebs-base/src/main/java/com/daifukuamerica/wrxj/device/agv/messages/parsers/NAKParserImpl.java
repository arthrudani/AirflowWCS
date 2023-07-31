
package com.daifukuamerica.wrxj.device.agv.messages.parsers;

import com.daifukuamerica.wrxj.device.agv.AGVDBInterface;
import com.daifukuamerica.wrxj.device.agv.AGVException;
import com.daifukuamerica.wrxj.device.agv.messages.AGVData;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageConstants;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageHelper;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageNameEnum;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageParseException;
import com.daifukuamerica.wrxj.device.agv.messages.AbstractMessageParser;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class to handle NAK messages fromthe CMS system.
 * @author A.D.
 * @since  15-May-2009
 */
public class NAKParserImpl extends AbstractMessageParser
{
  private static final String ERRORCODE_NAME   = "ERRORCODE";
  private static final String TEXT_NAME        = "TEXT";

  private int    mnErrorCode = 0;
  private String msText = "";
  private Map<String, Integer> mpParsingTemplate = null;

  public NAKParserImpl()
  {
    super();
    mpParsingTemplate = new LinkedHashMap<String, Integer>(3);
    mpParsingTemplate.put(AGVMessageConstants.MESSAGEIDENTIFIER_NAME,
                          Integer.valueOf(AGVMessageConstants.MESSAGEID_LEN));
    mpParsingTemplate.put(ERRORCODE_NAME,
                          Integer.valueOf(AGVMessageConstants.ERROR_CODE_LEN));
    mpParsingTemplate.put(TEXT_NAME,
                          Integer.valueOf(AGVMessageConstants.ERROR_TEXT_LEN));
  }

/*----------------------------------------------------------------------------
   Getters Methods in case the user of this class wants some or all of the
   parsed fields for informational purposes.
  ----------------------------------------------------------------------------*/
  public int getErrorCode()
  {
    return(mnErrorCode);
  }

 /**
  * Method gets the returned text from the CMS system.  This text is the first
  * 80 characters of the message that was rejected.
  * @return rejected message text.
  */
  public String getText()
  {
    return(msText);
  }
/*----------------------------------------------------------------------------
                           Interface Methods
  ----------------------------------------------------------------------------*/
  @Override
  public void clear()
  {
    super.clear();
    mnErrorCode = 0;
    msText      = "";
  }

    @Override
  public boolean isAckNakMessage()
  {
    return(true);
  }

  @Override
  public void parseMessage(String isMessage) throws AGVMessageParseException
  {
    parse(AGVMessageNameEnum.NAK_REQUEST_RESPONSE, mpParsingTemplate, isMessage);
  }

 /**
  * Method to set the fields for the NAK message.
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
      if (isColumnName.equals(ERRORCODE_NAME))
      {
        mnErrorCode = Integer.parseInt(isColumnValue);
      }
      else if (isColumnName.equals(TEXT_NAME))
      {
        msText = isColumnValue;
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
    return(vnRecvdLength == AGVMessageConstants.CONFIRMED_NAK_LEN);
  }

  @SuppressWarnings("incomplete-switch")
  @Override
  public void processMessage(AGVDBInterface ipDBInterface) throws AGVException
  {
    int vnSeq = getMessageSequence();
    AGVData vpAGVData = ipDBInterface.getData(vnSeq);
    if (vpAGVData == null)
    {
      mpBasicLogger.logErrorMessage("Could not NAK for sequence " + vnSeq);
      return;
    }
                                       // Do normal processing if it's a
                                       // skipped sequence.
    if (getErrorCode() == AGVMessageConstants.SKIPPED_MESSAGE_CODE)
    {
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
    else
    {
      mpBasicLogger.logErrorMessage("Marking WarehouseRx to CMS message with " +
                                    "sequence " + vnSeq + " in error. " +
                                    "CMS received bad data!");

      switch(vpAGVData.getMessageName())
      {
        case MOV_REQUEST:
          ipDBInterface.updateAGVMoveStatus(vnSeq,
                                            AGVDBInterface.LOAD_MOVE_ERROR);
          break;

        case CAN_REQUEST:
          ipDBInterface.updateAGVMoveStatus(vnSeq,
                                       AGVDBInterface.LOAD_MOVE_CANCEL_ERROR);
          break;

        case XMT_REQUEST:
        case PIC_REQUEST:
        case HLD_REQUEST:
        case RES_REQUEST:
        case RSU_REQUEST:
          ipDBInterface.updateAGVSystemCmdStatus(vnSeq, AGVDBInterface.SYSCMD_ERROR);
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
    String vsDesc = "Negative Acknowledgement. " + msText + "  ";
    StringBuffer vpLogBuffer = AGVMessageHelper.getStandardLogFields(
                      msMessageIdentifier, vsDesc, mnMessageSequence, mnConfirmLength);
    vpLogBuffer.append("Error Code: ").append(Integer.toString(mnErrorCode))
               .append("  Error Text: ").append(msText);

    return(vpLogBuffer);
  }
}
