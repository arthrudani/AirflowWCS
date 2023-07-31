package com.daifukuamerica.wrxj.device.agv.messages.parsers;

import com.daifukuamerica.wrxj.device.agv.AGVDBInterface;
import com.daifukuamerica.wrxj.device.agv.AGVException;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageConstants;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageHelper;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageNameEnum;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Message from AGV indicating a Move Abort.
 * 
 * @author A.D.
 * @since  13-May-2009
 */
public class MABParserImpl extends LPCParserImpl
{
  protected int mnErrorNumber = 0;
  private static final Map<Integer, String> mpErrorCodeMap = new HashMap<Integer, String>();

  protected static final String ERRORNUMBER_NAME = "ERRORNUMBER";

  public MABParserImpl()
  {
    super();
    if (mpErrorCodeMap.isEmpty())
    {
      mpErrorCodeMap.put(Integer.valueOf(1), "Vehicle disabled");
      mpErrorCodeMap.put(Integer.valueOf(2), "Destination disabled");
      mpErrorCodeMap.put(Integer.valueOf(3), "Vehicle fault, move aborted by vehicle operator");
      mpErrorCodeMap.put(Integer.valueOf(4), "Vehicle at wrong location");
      mpErrorCodeMap.put(Integer.valueOf(5), "No load present");
      mpErrorCodeMap.put(Integer.valueOf(6), "Move buffer full");
      mpErrorCodeMap.put(Integer.valueOf(7), "Load already present");
      mpErrorCodeMap.put(Integer.valueOf(8), "Invalid move");
      mpErrorCodeMap.put(Integer.valueOf(9), "Move deleted by CMS operator");
      mpErrorCodeMap.put(Integer.valueOf(10), "Duplicate move, same pick");
    }
    mpParsingTemplate.clear();
    mpParsingTemplate.put(AGVMessageConstants.MESSAGEIDENTIFIER_NAME, Integer.valueOf(AGVMessageConstants.MESSAGEID_LEN));
    mpParsingTemplate.put(REQUESTID_NAME, Integer.valueOf(AGVMessageConstants.REQUESTID_LEN));
    mpParsingTemplate.put(DROPLOCATION_NAME, Integer.valueOf(AGVMessageConstants.LOCATION_LEN));
    mpParsingTemplate.put(LOCATIONHEIGHT_NAME, Integer.valueOf(AGVMessageConstants.LOCATION_HEIGHT_LEN));
    mpParsingTemplate.put(LOCATIONDEPTH_NAME, Integer.valueOf(AGVMessageConstants.LOCATION_DEPTH_LEN));
    mpParsingTemplate.put(LOADID_NAME, Integer.valueOf(AGVMessageConstants.LOAD_LEN));
    mpParsingTemplate.put(ERRORNUMBER_NAME, Integer.valueOf(AGVMessageConstants.ERROR_NUMBER_LEN));
  }

/*----------------------------------------------------------------------------
   Getters Methods in case the user of this class wants some or all of the
   parsed fields for informational purposes.
  ----------------------------------------------------------------------------*/
  public int getErrorNumber()
  {
    return(mnErrorNumber);
  }
  
/*----------------------------------------------------------------------------
                           Interface Methods
  ----------------------------------------------------------------------------*/
  @Override
  public void clear()
  {
    super.clear();
    mnErrorNumber = 0;
  }

  @Override
  public void parseMessage(String isMessage) throws AGVMessageParseException
  {
    parse(AGVMessageNameEnum.MAB_RESPONSE, mpParsingTemplate, isMessage);
  }

 /**
  * Method to set the fields for the MAB message.
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
    int vnRtn = super.setField(isColumnName, isColumnValue);

    if (vnRtn == -1)
    {
      if (isColumnName.equals(ERRORNUMBER_NAME))
      {
        try
        {
          mnErrorNumber = Integer.parseInt(isColumnValue);
        }
        catch(NumberFormatException nfe)
        {
          throw new AGVMessageParseException("Error parsing Error code value of " +
                                      isColumnValue + " in MAB message!", nfe);
        }
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
    return(vnRecvdLength == AGVMessageConstants.CONFIRMED_MAB_LEN);
  }

  @Override
  public void processMessage(AGVDBInterface ipDBInterface) throws AGVException
  {
    ipDBInterface.incrementInboundSequence();
    if (mnErrorNumber == AGVMessageConstants.VEHICLE_FAULT)
    {                                  // Manual intervention.  Will need to
                                       // resend command from AGV recovery screen.
      ipDBInterface.updateAGVMoveStatus(msLoadID, AGVDBInterface.LOAD_MOVE_CANCELED);
    }
    else if (mnErrorNumber == AGVMessageConstants.OPERATOR_DELETED_MOVE)
    {                                  // Move will need to be recreated.
      ipDBInterface.deleteAGVMove(msLoadID);
    }
    else
    {
      ipDBInterface.updateAGVMoveStatus(msLoadID, AGVDBInterface.LOAD_MOVE_ERROR);
    }
    
    if (mpBasicLogger != null)
    {
      mpBasicLogger.logErrorMessage("Move aborted for Load " + msLoadID +
                                  " destined for station " + msDropLocation +
                                  ". " + mpErrorCodeMap.get(mnErrorNumber));
    }
  }

 /**
  * {@inheritDoc}
  */
  @Override
  public StringBuffer getFormattedLogFields()
  {
    String vsDesc = "Move Aborted: " + msLoadID + "  ";
    StringBuffer vpBuffer = AGVMessageHelper.getStandardLogFields(msMessageIdentifier,
                               vsDesc, mnMessageSequence, mnConfirmLength);

    vpBuffer.append("Load ID: ").append(msLoadID)
            .append(AGVMessageConstants.EOL_CHAR)
            .append("Pick Locn: ").append(msDropLocation)
            .append(AGVMessageConstants.EOL_CHAR)
            .append("Request ID: ").append(msRequestID)
            .append(AGVMessageConstants.EOL_CHAR)
            .append("Locn. Height: ").append(Integer.toString(mnLocnHeight))
            .append(AGVMessageConstants.EOL_CHAR)
            .append("Locn. Depth: ").append(Integer.toString(mnLocnDepth))
            .append(AGVMessageConstants.EOL_CHAR)
            .append("Error Code: ").append(Integer.toString(mnErrorNumber))
            .append(" (").append(mpErrorCodeMap.get(mnErrorNumber)).append(")");

    return(vpBuffer);
  }
}
