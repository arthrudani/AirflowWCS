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
 * <b>Message direction: WRx-J -----> CMS.</b>  Class to format the CAN message.
 * This message commands the CMS to cancel a move request.  This command is
 * successful only if the CMS has not dispatched an AGV for this move.
 *
 * @author A.D.
 * @since  12-May-2009
 */
public class CANFormatter extends AbstractMessageFormatter
{
  private AGVData mpData;

  public CANFormatter()
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
    mpData = ipDBInterface.getData(AGVMessageConstants.MOVE_CATEGORY,
                                           inSequenceNumber);
    if (mpData == null)
    {
      throw new AGVException("Warning: AGV Move record not found for move " +
                             "sequence " + inSequenceNumber);
    }
    String vsMesg = "";
    
    try
    {
      FieldTemplate[] vapFormatterTemplate = new FieldTemplate[]
      {
        new FieldTemplate(Integer.valueOf(inSequenceNumber), AGVMessageConstants.SERIAL_NUMBER_LEN),
        new FieldTemplate(AGVMessageNameEnum.CAN_REQUEST, AGVMessageConstants.MESSAGEID_LEN),
        new FieldTemplate(mpData.getRequestID(), AGVMessageConstants.REQUESTID_LEN),
        new FieldTemplate(mpData.getCurrStation(), AGVMessageConstants.LOCATION_LEN),
        new FieldTemplate(Integer.valueOf(mpData.getPickLocationHeight()), AGVMessageConstants.LOCATION_HEIGHT_LEN),
        new FieldTemplate(Integer.valueOf(mpData.getPickLocationDepth()), AGVMessageConstants.LOCATION_DEPTH_LEN),
        new FieldTemplate(mpData.getLoadID(), AGVMessageConstants.LOAD_LEN)
      };
      vsMesg = format(vapFormatterTemplate);
    }
    catch(UnsupportedOperationException exc)
    {
      throw new AGVMessageFormatterException("Error formatting " +
                                 AGVMessageNameEnum.CAN_REQUEST.getValue() +
                                 " message. " + exc.getMessage());
    }
    
    return(vsMesg);
  }

  @Override
  public void postSendProcessing(AGVDBInterface ipDBInterface) throws AGVException
  {
    ipDBInterface.updateAGVMoveStatus(mpData.getLoadID(),
                                      AGVDBInterface.LOAD_MOVE_CANCEL_SENT);
  }

  @Override
  public StringBuffer getFormattedLogFields()
  {
    String vsDesc = "Cancel Message. Load ID: " + mpData.getLoadID() + "  ";
    StringBuffer vpLogBuffer = AGVMessageHelper.getStandardLogFields(AGVMessageNameEnum.CAN_REQUEST.getValue(),
                         vsDesc, mpData.getSequenceNumber(), mnConfirmLength);
    vpLogBuffer.append("Request ID: ").append(mpData.getRequestID())
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Current Station: ").append(mpData.getCurrStation())
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Locn. Height: ").append(Integer.toString(mpData.getPickLocationHeight()))
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Locn. Depth: ").append(Integer.toString(mpData.getPickLocationDepth()))
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Load ID: ").append(mpData.getLoadID());

    return(vpLogBuffer);
  }
}
