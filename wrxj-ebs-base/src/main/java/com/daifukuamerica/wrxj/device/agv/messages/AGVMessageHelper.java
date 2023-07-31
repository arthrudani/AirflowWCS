package com.daifukuamerica.wrxj.device.agv.messages;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Helper class for AGV Messages.
 * @author A.D.
 * @since  12-May-2009
 */
public final class AGVMessageHelper
{
  public AGVMessageHelper()
  {
    super();
  }

  public static void loadEnvironment(String isFile)
         throws FileNotFoundException, IOException
  {
    Properties vpProp = System.getProperties();
    vpProp.load(new FileReader(isFile));
    System.setProperties(vpProp);
  }

 /**
  * Method to get the CMS-prescribed message length and sequence number from a
  * message.
  * @param isMessage the message being parsed.
  * @return String array containing the message length, and sequence number.
  * <ul>
  *   <li>String[0] = Message length</li>
  *   <li>String[1] = Message sequence</li>
  * </ul>
  */
  public synchronized static String[] peekMessageHeaderInfo(String isMessage)
  {
    String vsMsgLen;
    String vsSeq;

    try
    {
      int vnStart = 0;
      int vnEnd = AGVMessageConstants.MESSAGE_LENGTH_LEN;
      vsMsgLen = isMessage.substring(vnStart, vnEnd);

      vnStart = AGVMessageConstants.MESSAGE_LENGTH_LEN;
      vnEnd = vnStart + AGVMessageConstants.SERIAL_NUMBER_LEN;
      vsSeq = isMessage.substring(vnStart, vnEnd);
    }
    catch(StringIndexOutOfBoundsException iob)
    {
      vsMsgLen = "0";
      vsSeq = "0";
    }

    return(new String[] {vsMsgLen, vsSeq});
  }

 /**
  * Method to get standard fields that will be logged in every message.
  * @param isMessageId the message Identifier.
  * @param isMesgDesc Brief message description.
  * @param inMessageSequence message sequence.
  * @param inMessageLen the message length.
  * @return StringBuffer with formatted data.
  */
  public synchronized static StringBuffer getStandardLogFields(String isMessageId,
                             String isMesgDesc, int inMessageSequence, int inMessageLen)
  {
    StringBuffer vpLogMesg = new StringBuffer();
    vpLogMesg.append(isMessageId).append(" - ").append(isMesgDesc)
             .append(AGVMessageConstants.EOL_CHAR)
             .append("Sequence: ").append(Integer.toString(inMessageSequence))
             .append(AGVMessageConstants.EOL_CHAR)
             .append("Total Length: ").append(Integer.toString(inMessageLen + 4))
             .append("  (Length Indicator(4) + Sequence Length(4) + Data Length(" + (inMessageLen-4) + ")")
             .append(AGVMessageConstants.EOL_CHAR)
             .append("----------------------  End Summary ----------------------")
             .append(AGVMessageConstants.EOL_CHAR)
             .append(AGVMessageConstants.EOL_CHAR);

    return(vpLogMesg);
  }
}
