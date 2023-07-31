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
 * <b>Message direction: WRx-J -----> CMS.</b>  Class to format a CHG command.
 * This command instructs the CMS to change a previously sent MOV command if
 * possible.  The CHG command is effective up until the moment the AGV decides
 * to drop off a load.
 * @author A.D.
 * @since  12-May-2009
 */
public class CHGFormatter extends AbstractMessageFormatter
{
  private AGVData mpData;
 /**
  * Default constructor.
  */
  public CHGFormatter()
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
    mpData = ipDBInterface.getData(AGVMessageConstants.MOVE_CATEGORY, inSequenceNumber);
    if (mpData == null) return("");
    
    String vsMesg = "";

    try
    {
      FieldTemplate[] vapFormatTemplate = new FieldTemplate[]
      {
        new FieldTemplate(Integer.valueOf(inSequenceNumber),
                                  AGVMessageConstants.SERIAL_NUMBER_LEN),
        new FieldTemplate(AGVMessageNameEnum.CHG_REQUEST,
                                  AGVMessageConstants.MESSAGEID_LEN),
        new FieldTemplate(mpData.getRequestID(),
                                  AGVMessageConstants.REQUESTID_LEN),
        new FieldTemplate(mpData.getCurrStation(),
                                  AGVMessageConstants.LOCATION_LEN),
        new FieldTemplate(Integer.valueOf(mpData.getPickLocationHeight()),
                                  AGVMessageConstants.LOCATION_HEIGHT_LEN),
        new FieldTemplate(Integer.valueOf(mpData.getPickLocationDepth()),
                                  AGVMessageConstants.LOCATION_DEPTH_LEN),
        new FieldTemplate(mpData.getDestStation(),
                                  AGVMessageConstants.LOCATION_LEN),
        new FieldTemplate(Integer.valueOf(mpData.getDropLocationHeight()),
                                  AGVMessageConstants.LOCATION_HEIGHT_LEN),
        new FieldTemplate(Integer.valueOf(mpData.getDropLocationDepth()),
                                  AGVMessageConstants.LOCATION_DEPTH_LEN),
        new FieldTemplate(mpData.getLoadID(), AGVMessageConstants.LOAD_LEN)
      };
      vsMesg = format(vapFormatTemplate);
    }
    catch(UnsupportedOperationException exc)
    {
      throw new AGVMessageFormatterException("Error formatting " +
                                 AGVMessageNameEnum.CHG_REQUEST.getValue() +
                                 " message. " + exc.getMessage());
    }

    return(vsMesg);
  }

  @Override
  public void postSendProcessing(AGVDBInterface ipDBInterface) throws AGVException
  {
  }

  @Override
  public StringBuffer getFormattedLogFields()
  {
    String vsDesc = "Change Load Move Request. Load ID: " + mpData.getLoadID() + "  ";
    StringBuffer vpLogBuffer = AGVMessageHelper.getStandardLogFields(AGVMessageNameEnum.CHG_REQUEST.getValue(),
                           vsDesc, mpData.getSequenceNumber(), mnConfirmLength);
    
    vpLogBuffer.append("Request ID: ").append(mpData.getRequestID())
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Current Station: ").append(mpData.getCurrStation())
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Locn. Height: ").append(Integer.toString(mpData.getPickLocationHeight()))
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Locn. Depth: ").append(Integer.toString(mpData.getPickLocationDepth()))
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Drop Locn: ").append(mpData.getDestStation())
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Drop Location Height: ").append(Integer.toString(mpData.getDropLocationHeight()))
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Drop Location Depth: ").append(Integer.toString(mpData.getDropLocationDepth()))
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Load ID: ").append(mpData.getLoadID());

    return(vpLogBuffer);
  }
}
