package com.daifukuamerica.wrxj.device.agv.messages.parsers;

import com.daifukuamerica.wrxj.device.agv.AGVDBInterface;
import com.daifukuamerica.wrxj.device.agv.AGVException;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageConstants;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageHelper;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageNameEnum;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageParseException;
import com.daifukuamerica.wrxj.device.agv.messages.AbstractMessageParser;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <b>Message direction: WRx-J <----- CMS.</b>  Message parser for LAL (Load at
 * Location) message.  This mesage is sent from the CMS when the load is dropped
 * off at its destination.
 * 
 * @author A.D.
 * @since  15-May-2009
 */
public class LALParserImpl extends AbstractMessageParser
{
  protected static final String REQUESTID_NAME      = "REQUESTID";
  protected static final String DROPLOCATION_NAME   = "DROPLOCATION";
  protected static final String LOCATIONHEIGHT_NAME = "LOCATIONHEIGHT";
  protected static final String LOCATIONDEPTH_NAME  = "LOCATIONDEPTH";
  protected static final String LOADID_NAME         = "LOADID";

  protected String msDropLocation = "";
  protected String msRequestID    = "";
  protected String msLoadID       = "";
  protected int    mnLocnHeight   = 0;
  protected int    mnLocnDepth    = 0;

  protected Map<String, Integer> mpParsingTemplate = null;

  public LALParserImpl()
  {
    super();
    mpParsingTemplate = new LinkedHashMap<String, Integer>(7);
    mpParsingTemplate.put(AGVMessageConstants.MESSAGEIDENTIFIER_NAME, Integer.valueOf(AGVMessageConstants.MESSAGEID_LEN));
    mpParsingTemplate.put(DROPLOCATION_NAME, Integer.valueOf(AGVMessageConstants.LOCATION_LEN));
    mpParsingTemplate.put(LOCATIONHEIGHT_NAME, Integer.valueOf(AGVMessageConstants.LOCATION_HEIGHT_LEN));
    mpParsingTemplate.put(LOCATIONDEPTH_NAME, Integer.valueOf(AGVMessageConstants.LOCATION_DEPTH_LEN));
    mpParsingTemplate.put(REQUESTID_NAME, Integer.valueOf(AGVMessageConstants.REQUESTID_LEN));
    mpParsingTemplate.put(LOADID_NAME, Integer.valueOf(AGVMessageConstants.LOAD_LEN));
  }

 /*----------------------------------------------------------------------------
   Getters Methods in case the user of this class wants some or all of the
   parsed fields for informational purposes.
  ----------------------------------------------------------------------------*/
  public int getLocnDepth()
  {
    return(mnLocnDepth);
  }

  public int getLocnHeight()
  {
    return(mnLocnHeight);
  }

  public String getLoadID()
  {
    return(msLoadID);
  }

  public String getRequestID()
  {
    return(msRequestID);
  }

  public String getDropLocation()
  {
    return(msDropLocation);
  }

 /*----------------------------------------------------------------------------
                           Interface Methods
  ----------------------------------------------------------------------------*/
  @Override
  public void clear()
  {
    super.clear();
    msDropLocation = "";
    msRequestID      = "";
    msLoadID         = "";
    mnLocnHeight     = 0;
    mnLocnDepth      = 0;
  }

  @Override
  public void parseMessage(String isMessage) throws AGVMessageParseException
  {
    parse(AGVMessageNameEnum.LAL_RESPONSE, mpParsingTemplate,isMessage);
  }

 /**
  * Method to set the fields for the LAL message.
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
    try
    {
      if (super.setField(isColumnName, isColumnValue) == -1)
      {
        if (isColumnName.equals(DROPLOCATION_NAME))
        {
          msDropLocation = isColumnValue;
        }
        else if (isColumnName.equals(REQUESTID_NAME))
        {
          msRequestID = isColumnValue;
        }
        else if (isColumnName.equals(LOADID_NAME))
        {
          msLoadID = isColumnValue;
        }
        else if (isColumnName.equals(LOCATIONHEIGHT_NAME))
        {
          mnLocnHeight = Integer.parseInt(isColumnValue);
        }
        else if (isColumnName.equals(LOCATIONDEPTH_NAME))
        {
          mnLocnDepth = Integer.parseInt(isColumnValue);
        }
        else
        {
          vnRtn = -1;
        }
      }
    }
    catch(NumberFormatException nfe)
    {
      throw new AGVMessageParseException("Error parsing field " + isColumnName +
                                " Field value: " + isColumnValue, nfe);
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
    return(vnRecvdLength == AGVMessageConstants.CONFIRMED_LAL_LEN);
  }

  @Override
  public void processMessage(AGVDBInterface ipDBInterface) throws AGVException
  {
    super.processMessage(ipDBInterface);
    ipDBInterface.updateAGVMoveStatus(msLoadID, AGVDBInterface.LOAD_AT_LOCATION);
    mpAGCNotifier.notifyLoadArrival(msDropLocation, msLoadID);
  }

 /**
  * Method to create formatted logs.
  */
  @Override
  public StringBuffer getFormattedLogFields()
  {
    String vsDesc = "Load at Location. Load ID: " + msLoadID + "  ";
    StringBuffer vpLogBuffer = AGVMessageHelper.getStandardLogFields(msMessageIdentifier,
                               vsDesc, mnMessageSequence, mnConfirmLength);
    vpLogBuffer.append("Load ID: ").append(msLoadID)
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Drop Locn: ").append(msDropLocation)
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Request ID: ").append(msRequestID)
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Locn. Height: ").append(Integer.toString(mnLocnHeight))
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Locn. Depth: ").append(Integer.toString(mnLocnDepth));

    return(vpLogBuffer);
  }
}
