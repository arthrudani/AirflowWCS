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
 * Class formats a NAK message.
 * @author A.D.
 * @since  19-May-2009
 */
public class NAKFormatter extends AbstractMessageFormatter
{
  private int    mnMesgSequence = 0;
  private int    mnErrorCode   = 0;
  private String msMessageText = "";

  public NAKFormatter()
  {
    super();
  }

 /*----------------------------------------------------------------------------
                           Setter Methods
  ----------------------------------------------------------------------------*/
  public void setErrorCode(int inErrorCode)
  {
    mnErrorCode = inErrorCode;
  }

  public void setMessageText(String isMessageText)
  {
    if (isMessageText.length() > 80)
      msMessageText = isMessageText.substring(0, 80);
    else
      msMessageText = isMessageText;
  }

 /*----------------------------------------------------------------------------
                           Interface Methods
  ----------------------------------------------------------------------------*/
  @Override
  public String format(int inSequenceNumber,AGVDBInterface ipDBInterface)
         throws AGVMessageFormatterException, AGVException
  {
    // stub to support interface.
    return("");
  }

 /**
  * The reference for this object will always need to be know outside to make
  * sure we can pass back the offending message sequence. Hence the reason for
  * this method.
  * @param inSequence the offending sequence number.
  * @return
  * @throws AGVMessageFormatterException
  * @throws AGVException
  */
  public String format(int inSequence) throws AGVMessageFormatterException
  {
    mnMesgSequence = inSequence;

    FieldTemplate[] vapFieldFormatTemplate = new FieldTemplate[]
    {
      new FieldTemplate(Integer.valueOf(inSequence), AGVMessageConstants.SERIAL_NUMBER_LEN),
      new FieldTemplate(AGVMessageNameEnum.NAK_REQUEST_RESPONSE, AGVMessageConstants.MESSAGEID_LEN),
      new FieldTemplate(Integer.valueOf(mnErrorCode), AGVMessageConstants.ERROR_CODE_LEN),
      new FieldTemplate(msMessageText, AGVMessageConstants.ERROR_TEXT_LEN)
    };

    return(format(vapFieldFormatTemplate));
  }
  
  @Override
  public void postSendProcessing(AGVDBInterface ipDBInterface) throws AGVException
  {
    // stub to support interface.
  }

 @Override
  public StringBuffer getFormattedLogFields()
  {
    String vsDesc = "Negative Acknowledgement. " + msMessageText + "  ";
    StringBuffer vpLogBuffer = AGVMessageHelper.getStandardLogFields(
            AGVMessageNameEnum.NAK_REQUEST_RESPONSE.getValue(), vsDesc,
            mnMesgSequence, mnConfirmLength);
    
    vpLogBuffer.append("Error Code: ").append(Integer.toString(mnErrorCode))
               .append(AGVMessageConstants.EOL_CHAR)
               .append("  Error Text: ").append(msMessageText);
    
    return(vpLogBuffer);
  }
}
