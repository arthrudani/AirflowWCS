
package com.daifukuamerica.wrxj.device.agv.messages.parsers;

import com.daifukuamerica.wrxj.device.agv.AGVDBInterface;
import com.daifukuamerica.wrxj.device.agv.AGVException;
import com.daifukuamerica.wrxj.device.agv.messages.*;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageParseException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Message from AGV indicating a Move Request Cancel or Change.  This message
 * is in direct response to the CANcel message from Wrx-J.
 *
 * @author A.D.
 * @since  13-May-2009
 */
public class MRCParserImpl extends AbstractMessageParser
{
  private static final String REQUESTTYPE_NAME    = "REQUESTTYPE";
  private static final String REQUESTID_NAME      = "REQUESTID";
  private static final String LOCATION_NAME       = "LOCATION";
  private static final String LOCATIONHEIGHT_NAME = "LOCATIONHEIGHT";
  private static final String LOCATIONDEPTH_NAME  = "LOCATIONDEPTH";
  private static final String DROPLOCATION_NAME   = "DROPLOCATION";;
  private static final String DROPLOCATIONHEIGHT_NAME = "DROPLOCATIONHEIGHT";
  private static final String DROPLOCATIONDEPTH_NAME  = "DROPLOCATIONDEPTH";
  private static final String LOADID_NAME         = "LOADID";
  private static final String STATUS_NAME         = "STATUS";

  private String msRequestType  = "";
  private String msRequestID    = "";
  private String msLocation     = "";
  private String msDropLocation = "";
  private String msLoadID       = "";
  private int    mnCancelStatus    = 0;
  private int    mnLocnHeight      = 0;
  private int    mnLocnDepth       = 0;
  private int    mnDropLocnHeight  = 0;
  private int    mnDropLocnDepth   = 0;

  private Map<String, Integer> mpParsingTemplate = null;

  public MRCParserImpl()
  {
    super();
    mpParsingTemplate = new LinkedHashMap<String, Integer>(7);
    mpParsingTemplate.put(AGVMessageConstants.MESSAGEIDENTIFIER_NAME, Integer.valueOf(AGVMessageConstants.MESSAGEID_LEN));
    mpParsingTemplate.put(REQUESTTYPE_NAME, Integer.valueOf(AGVMessageConstants.MESSAGEID_LEN));
    mpParsingTemplate.put(REQUESTID_NAME, Integer.valueOf(AGVMessageConstants.REQUESTID_LEN));
    mpParsingTemplate.put(LOCATION_NAME, Integer.valueOf(AGVMessageConstants.LOCATION_LEN));
    mpParsingTemplate.put(LOCATIONHEIGHT_NAME, Integer.valueOf(AGVMessageConstants.LOCATION_HEIGHT_LEN));
    mpParsingTemplate.put(LOCATIONDEPTH_NAME, Integer.valueOf(AGVMessageConstants.LOCATION_DEPTH_LEN));
    mpParsingTemplate.put(DROPLOCATION_NAME, Integer.valueOf(AGVMessageConstants.LOCATION_LEN));
    mpParsingTemplate.put(DROPLOCATIONHEIGHT_NAME, Integer.valueOf(AGVMessageConstants.LOCATION_HEIGHT_LEN));
    mpParsingTemplate.put(DROPLOCATIONDEPTH_NAME, Integer.valueOf(AGVMessageConstants.LOCATION_DEPTH_LEN));
    mpParsingTemplate.put(LOADID_NAME, Integer.valueOf(AGVMessageConstants.LOAD_LEN));
    mpParsingTemplate.put(STATUS_NAME, Integer.valueOf(AGVMessageConstants.CANCEL_STATUS_LEN));
  }

 /*----------------------------------------------------------------------------
   Getters Methods in case the user of this class wants some or all of the
   parsed fields for informational purposes.
  ----------------------------------------------------------------------------*/
  public int getDropLocnDepth()
  {
    return(mnDropLocnDepth);
  }

  public int getDropLocnHeight()
  {
    return(mnDropLocnHeight);
  }

  public int getCancelStatus()
  {
    return(mnCancelStatus);
  }

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

  public String getLocation()
  {
    return(msLocation);
  }

  public String getDropLocation()
  {
    return(msDropLocation);
  }

  public String getRequestType()
  {
    return(msRequestType);
  }

/*----------------------------------------------------------------------------
                           Interface Methods
  ----------------------------------------------------------------------------*/
  @Override
  public void clear()
  {
    msRequestType     = "";
    msRequestID       = "";  
    msLocation        = "";  
    msDropLocation    = "";  
    msLoadID          = "";  
    mnCancelStatus    = 0;   
    mnLocnHeight      = 0;   
    mnLocnDepth       = 0;   
    mnDropLocnHeight  = 0;   
    mnDropLocnDepth   = 0;   
  }

  @Override
  public void parseMessage(String isMessage) throws AGVMessageParseException
  {
    parse(AGVMessageNameEnum.MRC_RESPONSE, mpParsingTemplate, isMessage);
  }

  @Override
  public boolean isValidMessageLength(String isMessage)
  {
    /*
     * The sequence number was already stripped out for use in ACK/NAK responses
     * early on.  So we need to add this length to compensate.
     */
    int vnRecvdLength = isMessage.length() + AGVMessageConstants.SERIAL_NUMBER_LEN;
    return(vnRecvdLength == AGVMessageConstants.CONFIRMED_MRC_LEN);
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
        if (isColumnName.equals(REQUESTTYPE_NAME))
        {
          msRequestType = isColumnValue;
        }
        else if (isColumnName.equals(REQUESTID_NAME))
        {
          msRequestID = isColumnValue;
        }
        else if (isColumnName.equals(LOCATION_NAME))
        {
          msLocation = isColumnValue;
        }
        else if (isColumnName.equals(DROPLOCATION_NAME))
        {
          msDropLocation = isColumnValue;
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
        else if (isColumnName.equals(DROPLOCATIONHEIGHT_NAME))
        {
          mnDropLocnHeight = Integer.parseInt(isColumnValue);
        }
        else if (isColumnName.equals(DROPLOCATIONDEPTH_NAME)) 
        {
          mnDropLocnDepth = Integer.parseInt(isColumnValue);
        }
        else if (isColumnName.equals(STATUS_NAME))
        {
          mnCancelStatus = Integer.parseInt(isColumnValue);
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
  public void processMessage(AGVDBInterface ipDBInterface) throws AGVException
  {
    super.processMessage(ipDBInterface);
    if (mnCancelStatus == AGVMessageConstants.MOVE_CANCEL_SUCCESS)
      ipDBInterface.updateAGVMoveStatus(msLoadID, AGVDBInterface.LOAD_MOVE_CANCELED);
  }

 /**
  * Method to create formatted logs.
  */
  @Override
  public StringBuffer getFormattedLogFields()
  {
    String vsDesc = "Move Request Changed/Cancelled. Load ID: " + msLoadID;
    StringBuffer vpLogBuffer = AGVMessageHelper.getStandardLogFields(
                      msMessageIdentifier, vsDesc, mnMessageSequence, mnConfirmLength);
    
    vpLogBuffer.append("Request Type: ").append(msRequestType)
               .append("  Request ID: ").append(msRequestID)
               .append("    Pickup Location: ").append(msLocation)
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Drop Location: ").append(msDropLocation)
               .append("  Load ID: ").append(msLoadID)
               .append("    Cancel Status: ").append(Integer.toString(mnCancelStatus))
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Pick Locn. Height: ").append(Integer.toString(mnLocnHeight))
               .append("  Pick Locn. Depth: ").append(Integer.toString(mnLocnDepth))
               .append("    Drop Location Height: ").append(Integer.toString(mnDropLocnHeight))
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Drop Location Depth: ").append(Integer.toString(mnDropLocnDepth));

    return(vpLogBuffer);
  }
}
