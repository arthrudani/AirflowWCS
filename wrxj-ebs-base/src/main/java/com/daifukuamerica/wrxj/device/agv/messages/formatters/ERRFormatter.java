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
 * <b>Message direction: WRx-J -----> CMS.</b>  Class to format the MOV message.
 * This command moves a load from a source to a destination location.
 *
 * @author A.D.
 * @since  12-May-2008
 */
public class ERRFormatter extends AbstractMessageFormatter
{
  public static final String DB_PROCESSING_ERROR = "PE";
  private int   mnMesgSequence = 0;
  private String msReasonCode  = "";
  private String msReasonText  = "";
  
  public ERRFormatter()
  {
    super();
  }

 /*----------------------------------------------------------------------------
                           Setter Methods
  ----------------------------------------------------------------------------*/
 /**
  * Sets this applications specific reason code.
  * @param isReasonCode possible value {@link ERRFormatter#DB_PROCESSING_ERROR DB_PROCESSING_ERROR}
  *        for Processing Error.
  */
  public void setReasonCode(String isReasonCode)
  {
    msReasonCode = isReasonCode;
  }

  public void setReasonText(String isReasonText)
  {
    msReasonText = isReasonText;
  }

 /*----------------------------------------------------------------------------
                           Interface Methods
  ----------------------------------------------------------------------------*/
  @Override
  public String format(int inSequenceNumber, AGVDBInterface ipDBInterface)
         throws AGVMessageFormatterException, AGVException
  {
    String vsMesg = "";
    mnMesgSequence = inSequenceNumber;

    try
    {
      FieldTemplate[] vapFormatTemplete = new FieldTemplate[]
      {
        new FieldTemplate(Integer.valueOf(inSequenceNumber), AGVMessageConstants.SERIAL_NUMBER_LEN),
        new FieldTemplate(AGVMessageNameEnum.ERR_REQUEST_RESPONSE, AGVMessageConstants.MESSAGEID_LEN),
        new FieldTemplate(msReasonCode, AGVMessageConstants.REASON_CODE_LEN),
        new FieldTemplate(msReasonText, AGVMessageConstants.REASON_TEXT_LEN)
      };
      vsMesg = format( vapFormatTemplete);
    }
    catch(UnsupportedOperationException exc)
    {
      throw new AGVMessageFormatterException("Error formatting " +
                                 AGVMessageNameEnum.ERR_REQUEST_RESPONSE.getValue() +
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
    String vsDesc = "Error Message. Reason Code: " + msReasonCode;
    StringBuffer vpLogBuffer = AGVMessageHelper.getStandardLogFields(
            AGVMessageNameEnum.ERR_REQUEST_RESPONSE.getValue(), vsDesc,
            mnMesgSequence, mnConfirmLength);
    
    vpLogBuffer.append("Reascon Code: ").append(msReasonCode)
               .append(AGVMessageConstants.EOL_CHAR)
               .append("Reason Text: ").append(msReasonText);

    return(vpLogBuffer);
  }
}
