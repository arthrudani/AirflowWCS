package com.daifukuamerica.wrxj.device.agv.messages;

import com.daifukuamerica.wrxj.device.agv.AGVDBInterface;
import com.daifukuamerica.wrxj.device.agv.AGVException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Interface to which all AGV message formatters must adhere.
 * 
 * @author A.D.
 * @since  12-May-2009
 */
public abstract class AbstractMessageFormatter
{
 /**
  * The message lenght the host uses for lenght confirmation.
  */
  protected int mnConfirmLength = 0;

 /**
  * Method does the actual work of formatting the message.
  * @param iapFormatTemplate array containing the formatting template.
  * @return formatted string.
  * @throws AGVMessageFormatterException if there is a formatting error.
  */
  protected String format(FieldTemplate[] iapFormatTemplate)
            throws AGVMessageFormatterException
  {
    String vsMessageName = "";
    StringBuilder vpFormattedStr = new StringBuilder();

    if (iapFormatTemplate != null && iapFormatTemplate.length != 0)
    {
      for(int vnIdx = 0; vnIdx < iapFormatTemplate.length; vnIdx++)
      {
        Object vpField = iapFormatTemplate[vnIdx].getField();
        int vnFieldLength = iapFormatTemplate[vnIdx].getFieldLength();

        if (vpField instanceof AGVMessageNameEnum)
        {
          vsMessageName = ((AGVMessageNameEnum)vpField).getValue();
          vpFormattedStr.append(vsMessageName);
        }
        else if (vpField instanceof String)
        {
          String vsField = String.format("%-" + vnFieldLength + "." +
                                         vnFieldLength + "s", (String)vpField);
          vpFormattedStr.append(vsField);
        }
        else if (vpField instanceof Integer)
        {
          String vsField = String.format("%0" + vnFieldLength + "d",
                                         ((Integer)vpField).intValue());
          vpFormattedStr.append(vsField);
        }
        else if (vpField instanceof Date)
        {
          SimpleDateFormat vpSDF = new SimpleDateFormat(AGVMessageConstants.CMS_DATETIME_FORMAT);
          vpFormattedStr.append(vpSDF.format((Date)vpField));
        }
        else
        {
          throw new AGVMessageFormatterException("Formatting error. " +
            "Unsupported data type! Error formatting " + vsMessageName +
            " message.");
        }
      }

      /*
       * Put in the computed message length.  It should be the message sequence
       * length + message length.
       */
      mnConfirmLength = vpFormattedStr.length();
      String vsMesgLen = String.format("%0" + AGVMessageConstants.MESSAGE_LENGTH_LEN + "d",
                                       mnConfirmLength);
      vpFormattedStr.insert(0, vsMesgLen);
    }

    return(vpFormattedStr.toString());
  }

 /**
  * The public version of the message formatter.
  * @param inSequenceNumber the sequence number associated with data to format.
  * @param ipDBInterface Generic reference to database api object.
  * @return Formatted string.
  * @throws AGVMessageFormatterException if there is an error building message (e.g.
  *         required message fields are missing etc.)
  * @throws AGVException if there is a sequence number generation problem.
  */
  public abstract String format(int inSequenceNumber, AGVDBInterface ipDBInterface)
         throws AGVMessageFormatterException, AGVException;

 /**
  * Method performs any post message send processing.
  * @param ipDBInterface reference to database interface impl.
  * @throws AGVException if there are any post processing errors.
  */
  public abstract void postSendProcessing(AGVDBInterface ipDBInterface)
         throws AGVException;
 /**
  * Method returns formatted string to log.
  * @return StringBuffer containing logging formatted data.
  */
  public abstract StringBuffer getFormattedLogFields();
}
