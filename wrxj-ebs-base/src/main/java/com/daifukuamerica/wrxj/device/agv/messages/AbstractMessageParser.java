package com.daifukuamerica.wrxj.device.agv.messages;

import com.daifukuamerica.wrxj.device.agv.AGVDBInterface;
import com.daifukuamerica.wrxj.device.agv.AGVException;
import com.daifukuamerica.wrxj.device.agv.AGVLogger;
import com.daifukuamerica.wrxj.device.agv.CMSStatusNotifier;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;

/**
 * Interface to which all AGV message parsers must adhere.
 * 
 * @author A.D.
 * @since  12-May-2009
 */
public abstract class AbstractMessageParser
{
  protected CMSStatusNotifier mpAGCNotifier;
  protected AGVLogger mpBasicLogger;
  protected int    mnConfirmLength     = 0;
  protected int    mnMessageSequence   = 0;
  protected String msMessageIdentifier = "";

/*===========================================================================
 *                     Getters for standard fields.
 *===========================================================================*/
  public int getConfirmMessageLength()
  {
    return(mnConfirmLength);
  }
  
  public int getMessageSequence()
  {
    return(mnMessageSequence);
  }

  public String getMessageIdentifier()
  {
    return(msMessageIdentifier);
  }

  public boolean isAckNakMessage()
  {
    return(false);
  }

  public boolean isLSSMessage()
  {
    return(false);
  }

 /**
  * Method to set appropriate AGC notifier.
  *
  * @param ipNotifier implementation of the AGC notifier of CMS status.
  */
  public void setAGCNotifier(CMSStatusNotifier ipNotifier)
  {
    mpAGCNotifier = ipNotifier;
  }

 /**
  * Assigns this parser a logger so that it can log an error to the system and
  * then continue processing the message.  For those cases where parser
  * processing must stop, throw an exception and let caller handle problem.
  * @param ipMessageLogger reference to AGVLogger.
  */
  public void setMessageLogger(AGVLogger ipMessageLogger)
  {
    mpBasicLogger = ipMessageLogger;
  }
  
 /**
  * Method parses header so that the confirmation message length and sequence
  * number are available for evaluation.
  * @param isMessage the message from CMS.
  * @return message string <u>without</u> the header information.
  * @throws AGVMessageParseException if fields could
  */
  public String parseHeader(String isMessage) throws AGVMessageParseException
  {
    try
    {
      String[] vasHeader = AGVMessageHelper.peekMessageHeaderInfo(isMessage);
      setField(AGVMessageConstants.MESSAGELENGTH_NAME, vasHeader[0]);
      setField(AGVMessageConstants.MESSAGESEQUENCE_NAME, vasHeader[1]);
    }
    catch(StringIndexOutOfBoundsException exc)
    {
      throw new AGVMessageParseException("Malformed message encountered: " +
                                         isMessage);
    }

    return(isMessage.substring(AGVMessageConstants.MESSAGE_LENGTH_LEN +
                               AGVMessageConstants.SERIAL_NUMBER_LEN));
  }

  /**
  * Method to do the actual parsing.
  * @param ipParsingTemplate Hash map containing order of field occurance, and
  * each field's documented length.  This tells the actual parser how to parse
  * the message.
  * @param ipMessageEnum Enumerated name of the message.
  * @param isMessage the message from the CMS.
  * @throws AGVMessageParseException if there is a message parse exception.
  */
  protected void parse(AGVMessageNameEnum ipMessageEnum,
                       Map<String, Integer> ipParsingTemplate, String isMessage)
            throws AGVMessageParseException
  {
    String vsFieldName = "";
    String vsParsedFieldValue = "";
    StringReader vpReader = new StringReader(isMessage);
    try
    {
      for(Iterator<String> vpIter = ipParsingTemplate.keySet().iterator();
          vpIter.hasNext();)
      {
        vsFieldName = vpIter.next();
        int vnFieldLen = ipParsingTemplate.get(vsFieldName);
        char[] vacField = new char[vnFieldLen];
        if (vpReader.read(vacField, 0, vnFieldLen) < 0)
        {
          throw new IOException("Premature end of message reached!");
        }
        vsParsedFieldValue = new String(vacField);
        setField(vsFieldName, vsParsedFieldValue);
      }
    }
    catch(IOException ioe)
    {
      throw new AGVMessageParseException("Error parsing message "       +
                                         ipMessageEnum.getValue()       +
                                         ". Parsing stopped on field: " +
                                         vsFieldName + " with value: "  +
                                         vsParsedFieldValue + ". "      +
                                         ioe.getMessage());
    }
  }

 /**
  * Method to clear out any old data and start over.
  */
  public void clear()
  {
    mnMessageSequence = 0;
    msMessageIdentifier = "";
  }

 /**
  * Partial implementation to set the fields for this class.
  * @param isColumnName the field name.
  * @param isColumnValue the field value.
  * @return -1 if passed field is not found; 0 otherwise.
  * @throws AGVMessageParseException if there is an integer conversion problem.
  *         This usually means we received a non-integer in a field that should
  *         be one.
  */
  protected int setField(String isColumnName, String isColumnValue)
            throws AGVMessageParseException
  {
    int vnRtn = 0;

    try
    {
      if (isColumnName.equals(AGVMessageConstants.MESSAGELENGTH_NAME))
      {
        mnConfirmLength = Integer.parseInt(isColumnValue);
      }
      else if (isColumnName.equals(AGVMessageConstants.MESSAGEIDENTIFIER_NAME))
      {
        msMessageIdentifier = isColumnValue;
      }
      else if (isColumnName.equals(AGVMessageConstants.MESSAGESEQUENCE_NAME))
      {
        mnMessageSequence = Integer.parseInt(isColumnValue);
      }
      else
      {
        vnRtn = -1;
      }
    }
    catch(NumberFormatException nfe)
    {
      throw new AGVMessageParseException("Error parsing field " + isColumnName +
                                         " Field value: " + isColumnValue, nfe);
    }
    
    return(vnRtn);
  }

 /**
  * Partial implementation method to do database level processing. This method
  * should be called <u>after</u> the message has been parsed and the ACK sent.
  * @param ipDBInterface reference to AGVDBInterface implementation.
  * @throws AGVException if there is a database error, or skipped sequence.
  */
  public void processMessage(AGVDBInterface ipDBInterface) throws AGVException
  {
    ipDBInterface.incrementInboundSequence();
  }

 /**
  * Method to validate message length.
  * @param isMessage the actual message minus the message length qualifier and
  * sequence number.
  * @return {@code true} if message length is correct, {@code false} otherwise.
  */
  public abstract boolean isValidMessageLength(String isMessage);
  
 /**
  * Method to parse an AGV message.
  * @param isMessage the message.
  * @throws AGVMessageParseException if there is an error building message (e.g.
  *         required message fields are missing etc.)
  */
  public abstract void parseMessage(String isMessage)
         throws AGVMessageParseException;

 /**
  * Method returns formatted string to log.
  * @return StringBuffer containing logging formatted data.
  */
  public abstract StringBuffer getFormattedLogFields();
}
