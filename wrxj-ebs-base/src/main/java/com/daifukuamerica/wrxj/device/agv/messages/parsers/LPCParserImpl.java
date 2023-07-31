package com.daifukuamerica.wrxj.device.agv.messages.parsers;

import com.daifukuamerica.wrxj.device.agv.AGVDBInterface;
import com.daifukuamerica.wrxj.device.agv.AGVException;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageConstants;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageHelper;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageNameEnum;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageParseException;

/**
 * Class to parse the LPC (Load Pickup Complete) message.
 * @author A.D.
 * @since  15-May-2009
 */
public class LPCParserImpl extends LALParserImpl
{
  protected static final String VEHICLENUMBER_NAME = "VEHICLENUMBER";
  private String msVehicleID  = "";

  public LPCParserImpl()
  {
    super();
    mpParsingTemplate.put(VEHICLENUMBER_NAME, Integer.valueOf(AGVMessageConstants.VEHICLE_NUMBER_LEN));
  }

 /*----------------------------------------------------------------------------
   Getters Methods in case the user of this class wants some or all of the
   parsed fields for informational purposes.
  ----------------------------------------------------------------------------*/
  public String getVehicleID()
  {
    return(msVehicleID);
  }

 /*----------------------------------------------------------------------------
                           Interface Methods
  ----------------------------------------------------------------------------*/
  @Override
  public void clear()
  {
    super.clear();
    msVehicleID = "";
  }

  @Override
  public void parseMessage(String isMessage) throws AGVMessageParseException
  {
    parse(AGVMessageNameEnum.LPC_RESPONSE, mpParsingTemplate, isMessage);
  }

 /**
  * Method to set the fields for the LPC message.
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
      if (isColumnName.equals(VEHICLENUMBER_NAME))
      {
        msVehicleID = isColumnValue;
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
    return(vnRecvdLength == AGVMessageConstants.CONFIRMED_LPC_LEN);
  }

  @Override
  public void processMessage(AGVDBInterface ipDBInterface) throws AGVException
  {
    ipDBInterface.incrementInboundSequence();
    ipDBInterface.updateAGVMoveStatus(msLoadID, AGVDBInterface.LOAD_PICKED_UP);
    ipDBInterface.updateVehicleID(msLoadID, msVehicleID);
    mpAGCNotifier.notifyLoadPickupComplete(msLoadID);
  }

 /**
  * {@inheritDoc}
  */
  @Override
  public StringBuffer getFormattedLogFields()
  {
    String vsDesc = "Load Pickup Complete: " + msLoadID + "  ";
    StringBuffer vpLogBuffer = AGVMessageHelper.getStandardLogFields(msMessageIdentifier,
                               vsDesc, mnMessageSequence, mnConfirmLength);

    vpLogBuffer.append("Load ID: ").append(msLoadID)
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Pick Locn: ").append(msDropLocation)
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Request ID: ").append(msRequestID)
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Locn. Height: ").append(Integer.toString(mnLocnHeight))
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Locn. Depth: ").append(Integer.toString(mnLocnDepth))
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Vehicle ID: ").append(msVehicleID);

    return(vpLogBuffer);
  }
}
