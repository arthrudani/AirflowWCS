package com.daifukuamerica.wrxj.host.messages;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.host.InvalidHostDataException;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 * Description:<BR>
 *  Helper class for formatting outbound messages as well as misc. operations
 *  for inbound messages.
 *
 * @author       A.D.
 * @version      1.0   03/01/2005
 */
public class MessageHelper
{
  protected static String ADD    = "ADD";
  protected static String MODIFY = "MODIFY";
  protected static String DELETE = "DELETE";

  // private Static declaration so they load at the same time as other static variables
  // using these definitions.
  private static String FORMATTER_PFX = Application.HOSTCFG_DOMAIN + "HostController.";

  public static String HOST_DATETIME_FORMAT;

  public static final int MESSAGE_LENGTH_PADDING = Application.getInt(
           FORMATTER_PFX + "MesgLengthSpecifierLength", 6);
  /**
   * The length of the Message Identifier. This is only useful for blank padded
   * messages.
   */
  public static final int MESSAGE_IDENTIFIER_LENGTH = Application.getInt(
           FORMATTER_PFX + "MesgIdentifierLength", 30);
  /**
   * The length of the Message sequence number field viewed as a string. This is
   * only useful for blank padded messages.
   */
  public static final int MESSAGE_SEQUENCE_LENGTH = Application.getInt(
           FORMATTER_PFX + "MesgSequenceLength", 8);

    /** The delimiter to use for delimited host messages. Default is semi-colon. */
  public static final String HOST_MESSAGE_DELIM = Application.getString(
           FORMATTER_PFX + "MesgDelimiter", ";");

  public static final boolean EMPTY_TAG_HANDLING = Application.getBoolean(
           FORMATTER_PFX + "SendEmptyFields", true);

  static
  {
    String vsDateTimeFmt = null;
    if ((vsDateTimeFmt = Application.getString(FORMATTER_PFX + "DateTimeFormat")) != null)
    {
      HOST_DATETIME_FORMAT = vsDateTimeFmt;
    }
    else
    {                                  // Default case when it's not defined in HostConfig.
      HOST_DATETIME_FORMAT = SKDCConstants.HOST_DATE_FORMAT;
    }
  }

/*===========================================================================
                  HELPER METHODS FOR OUTBOUND MESSAGES.
  ===========================================================================*/
 /**
  * Method resets format fields to default values.
  * @param fields {@link com.daifukuamerica.wrxj.jdbc.ColumnObject ColumnObject}
  * array of fields to default.
  */
  public static void defaultFields(ColumnObject[] fields)
  {
    for(ColumnObject vpCObj : fields)
    {
      switch(DBInfo.getFieldType(vpCObj.getColumnName()))
      {
        case Types.INTEGER:
          vpCObj.setColumnValue(Integer.valueOf(0));
          break;

        case Types.FLOAT:
        case Types.DOUBLE:
          vpCObj.setColumnValue(Double.valueOf(0.0));
          break;

        case Types.DATE:
        case Types.TIMESTAMP:
          vpCObj.setColumnValue(new Date());
          break;

        default:
          if (vpCObj.getColumnName().equalsIgnoreCase("dTransactionTime"))
            vpCObj.setColumnValue(new Date());
          else
            vpCObj.setColumnValue("");
      }
    }
  }

/*===========================================================================
                  HELPER METHODS FOR INBOUND MESSAGES.
  ===========================================================================*/
  public static void fillDataObject(AbstractSKDCData eskData, String fieldName,
                                    String fieldValue) throws InvalidHostDataException
  {
    String vsFieldName = fieldName.toUpperCase();

                                       // Handle translations.
    if (DBTrans.isTranslation(vsFieldName))
    {
      if (Character.isDigit(fieldValue.charAt(0)))
      {                                // In case they sent us the raw
                                       // translation (the integer value).
        eskData.setField(vsFieldName, Integer.valueOf(fieldValue));
      }
      else
      {
        String newFieldValue = fieldValue.replaceAll("_", " ");
        try
        {
          Object valueObject = DBTrans.getIntegerObject(vsFieldName, newFieldValue);
          eskData.setField(vsFieldName, valueObject);
        }
        catch(NoSuchFieldException e)
        {
          throw new InvalidHostDataException(e);
        }
      }
    }
    else
    {
      switch(DBInfo.getFieldType(vsFieldName))
      {
        case Types.INTEGER:
          if (fieldValue.trim().length() == 0) fieldValue = "0";
          eskData.setField(vsFieldName, Integer.valueOf(fieldValue.trim()));
          break;

        case Types.FLOAT:
        case Types.DOUBLE:
          if (fieldValue.trim().length() != 0)
          {
            double fNumber = Double.valueOf(fieldValue.trim());
            if (fNumber > 0) eskData.setField(vsFieldName, fNumber);
          }
          break;

        case Types.DATE:
        case Types.TIMESTAMP:
          if (fieldValue.trim().length() == 0)
          {
            eskData.setField(vsFieldName, new Date());
          }
          else
          {
            SimpleDateFormat sdf = new SimpleDateFormat(HOST_DATETIME_FORMAT);
            try { eskData.setField(vsFieldName, sdf.parse(fieldValue)); }
            catch(ParseException e)
            {
              throw new InvalidHostDataException("Error parsing date field...", e);
            }
          }
          break;

        default:
          eskData.setField(vsFieldName, fieldValue.trim());
      }
    }
  }

 /**
  * Parses the beginning portion of a delimited host message to figure out the
  * type of action being requested.  Valid actions are ADD, MODIFY, and DELETE.
  * @param ipScanner the token scanner.
  *
  * @return the requested action. -1 if the action is not defined properly.
  */
  public static int getDelimitedDataAction(Scanner ipScanner)
  {
    int vnOperationType = -1;
    if (ipScanner.hasNext())
    {
      String vsField = ipScanner.next();
      vnOperationType = (vsField.equals(ADD))    ? DBConstants.ADD    :
                        (vsField.equals(MODIFY)) ? DBConstants.MODIFY :
                        (vsField.equals(DELETE)) ? DBConstants.DELETE :
                                                   -1;
    }

    return(vnOperationType);
  }

  /**
   * Parses the beginning portion of a blank padded host message to figure out the
   * type of action being requested.  Valid actions are ADD, MODIFY, and DELETE.
   * @param ipHostMessage the raw host message.
   *
   * @return the requested action. -1 if the action is not defined properly.
   */
   public static int getFixedLengthDataAction(byte[] ipHostMessage)
   {
     int vnOperationType = -1;
     String vsSample = new String(ipHostMessage, 0, 7);

     if (vsSample.contains(ADD.subSequence(0, ADD.length())))
       vnOperationType = DBConstants.ADD;
     else if (vsSample.contains(MODIFY.subSequence(0, MODIFY.length())))
       vnOperationType = DBConstants.MODIFY;
     else if (vsSample.contains(DELETE.subSequence(0, DELETE.length())))
       vnOperationType = DBConstants.DELETE;

     return(vnOperationType);
   }

  /**
   * Method fills in Data object with parsed data fields.  <b>Note:</b> <i>For every
   * field in the host message there should be a corresponding entry in the Fields
   * definition file for things to parse correctly.</i>
   * @param ipScanner Token Scanner to parse data.
   * @param ipData The data object being filled in.
   * @param ipColumns array of Column names and default values as specified in
   *        the Field definition files.
   * @throws InvalidHostDataException if there is an error filling in the data object.
   */
  public static void setDelimitedDataObjectAttrib(Scanner ipScanner, AbstractSKDCData ipData,
                                                  ColumnObject[] ipColumns)
         throws InvalidHostDataException
  {
    ipData.clear();

    for(int vnField = 0; ipScanner.hasNext(); vnField++)
    {
      String vsField = ipScanner.next();
      if (vsField == null || vsField.trim().length() == 0)
      {                                // See if there is a default.
        vsField = (String)ipColumns[vnField].getColumnValue();
        if (vsField.trim().length() == 0) continue;
      }
      MessageHelper.fillDataObject(ipData, ipColumns[vnField].getColumnName(), vsField);
    }
  }

 /**
  * Method fills in Data object with fixed length parsed data fields.
  * @param ipMessageBytes the message string from host.
  * @param ipData The data object being filled in.
  * @param ipColumns array of Column names and default values as specified in
  *        the Field definition files.
  * @param izRemoveMessageAction set to <code>true</code> if the Message Action field
  *        (ADD, MODIFY, DELETE) must be removed from the message.  Set to
  *         <code>false</code>
  * @throws InvalidHostDataException if there is an error filling in the data object.
  */
  public static void setFixedLenDataObjectAttrib(byte[] ipMessageBytes, AbstractSKDCData ipData,
                                                 ColumnObject[] ipColumns, boolean izRemoveMessageAction)
         throws InvalidHostDataException
  {
    ipData.clear();

    StringReader vpReader = null;
    if (izRemoveMessageAction)
    {
                                       // Get rid of the Message Action field.
      byte[] vpMessageBytes = new byte[ipMessageBytes.length - 6];
      System.arraycopy(ipMessageBytes, 6, vpMessageBytes, 0, vpMessageBytes.length);
      vpReader = new StringReader(new String(vpMessageBytes));
    }
    else
    {
      vpReader = new StringReader(new String(ipMessageBytes));
    }

    int vnFieldPadLength = 0;
    String vsColumnName;
    String vsParsedField = null;

    try
    {
      for(ColumnObject vpCObj : ipColumns)
      {
        vsColumnName = vpCObj.getColumnName();
        vnFieldPadLength = getFieldLength(vsColumnName, vpCObj);

        char[] vpCharArray = new char[vnFieldPadLength];

        vpReader.read(vpCharArray, 0, vnFieldPadLength);
        vsParsedField = new String(vpCharArray);
        if (vsParsedField.trim().length() == 0)
        {                                // See if there is a default field
                                         // value assigned.
          ColumnObject vpUserAssigned = (ColumnObject)vpCObj.getColumnValue();
          vsParsedField = vpUserAssigned.getColumnName();
          if (vsParsedField.trim().length() == 0) continue;
        }
        MessageHelper.fillDataObject(ipData, vsColumnName, vsParsedField);
      }
    }
    catch (IOException vpExc)
    {
      InvalidHostDataException vpInvExc = new InvalidHostDataException(vpExc);
      vsParsedField = (vsParsedField == null) ? "" : vsParsedField;
      vpInvExc.setErrorCode(HostError.INVALID_DATA);
      vpInvExc.setErrorMessage("Parse Error occurred on or after field "
          + vsParsedField + ". Expected field of length " + vnFieldPadLength
          + " received " + vsParsedField.length());
      throw vpInvExc;
    }
  }

 /**
   * Method validates that the number of fields in the message is the expected
   * count.
   *
   * @param ipMessage byte array of host message.
   * @param inRequiredFieldCount the expected number of fields.
   *
   * @return the number of tokens found in the delimited host message.
   * @throws ParseException if the number of tokens in the message is not the
   *             same as the number expected.
   */
  public static int valFieldCount(byte[] ipMessage, int inRequiredFieldCount)
         throws InvalidHostDataException
  {
    int vnTokens = new String(ipMessage).split(HOST_MESSAGE_DELIM).length;

    inRequiredFieldCount += 1;         // The +1 is to account for the message
                                       // name action type (ADD,MODIFY, or
                                       // DELETE).
    if (vnTokens != inRequiredFieldCount)
      throw new InvalidHostDataException("The number of fields required in message is " +
                                         inRequiredFieldCount + ". Found " + vnTokens +
                                         ". Message will not be processed!");
    return(vnTokens);
  }

 /**
  * Gets the message field definitions for delimited messages from a definiton file.
  * <i>It is assumed that the fields definition file name will be of the form:
  * <p>  MessageIdentifier + "Fields"</p></i>  <p>The format of the file contents is
  * assumed to be of the form: <code>fieldName;default value;</code>
  * @param isMessageIdentifier the message identifier.
  * @return ColumnObject Array.  Each array element contains the name of the
  *         field and the default field value if any.  The order of the array
  *         indicates the order the message fields should be in.
  * @throws DBException if the field definition file information can't be
  *         retrieved.
  */
  public static ColumnObject[] getFieldDefintions(String isMessageIdentifier)
         throws DBException
  {
    URL vpURL = MessageHelper.class.getResource("/hostconfig/" + isMessageIdentifier + "Fields.txt");
    ColumnObject[] vpObjectArray = null;
    try
    {
      List<String> vpFieldsList = SKDCUtility.getFileLines(vpURL);
      vpObjectArray = new ColumnObject[vpFieldsList.size()];

      for(int vnIdx = 0, vnListSize = vpFieldsList.size(); vnIdx < vnListSize; vnIdx++)
      {
        Scanner vpScanner = new Scanner(vpFieldsList.get(vnIdx));
        vpScanner.useDelimiter(";");
        if (vpScanner.hasNext())
        {
          /*
           * ColumnObject("fieldName", ColumnObject("defaultValue", fieldLength))
           */
          vpObjectArray[vnIdx] = new ColumnObject(vpScanner.next().trim(),
                  new ColumnObject(vpScanner.next().trim(), vpScanner.next()));
        }
        vpScanner.close();
      }
    }
    catch(IOException ioe)
    {
      throw new DBException("Field definition file error...", ioe);
    }

    return(vpObjectArray);
  }

 /**
  * Convenience method for setting up an InvalidHostDataException that is used
  * to warn the host of data problems.
  * @param ipException a DBException which will be wrapped along with other data
  *        into an InvalidHostDataException.
  * @param inOriginalSequence The original sequence number from the host of the
  *        offending message.
  * @return
  */
  public static InvalidHostDataException getInvalidDataExcep(DBException ipException,
                                                             int inOriginalSequence)
  {
    int    vnErrorCode = 0;
    String vsErrorMessage = "";

    Throwable vpThrowable = ipException.getCause();
    if (vpThrowable != null && vpThrowable instanceof DBException)
    {
      DBException vpCause = (DBException)vpThrowable;
      if (vpCause.isDuplicate())
      {
        vnErrorCode = HostError.DUPLICATE_DATA;
//        vsErrorMessage = vpCause.getMessage() + "::::" + ipException.getMessage();
      }
      vsErrorMessage = ipException.getMessage();
    }
    else
    {
      if (ipException.isDuplicate())
        vnErrorCode = HostError.DUPLICATE_DATA;
      vsErrorMessage = ipException.getMessage();
    }

    return(new InvalidHostDataException(vnErrorCode, vsErrorMessage,
                                        inOriginalSequence));
  }

  /**
   * Method to make sure that field lengths are defaulted if none were provided.
   *
   * @param isColumnName
   * @param ipColObj
   * @return
   */
  public static int getFieldLength(String isColumnName, ColumnObject ipColObj)
  {
    int vnFieldLength = 0;

    if (isColumnName.equalsIgnoreCase("sLastLine"))
    {
      vnFieldLength = 3;
    }
    else
    {
                                       // Look in the database if the length is
                                       // not defined in the ColumnObject.
      ColumnObject vpUserAssigned = (ColumnObject)ipColObj.getColumnValue();
      String vsUserAssignedFieldLen = (String)vpUserAssigned.getColumnValue();
      if (vsUserAssignedFieldLen != null && !vsUserAssignedFieldLen.trim().isEmpty())
      {
        vnFieldLength = Integer.parseInt(vsUserAssignedFieldLen);
        if (vnFieldLength == 0)
        {
          if (DBInfo.getFieldType(isColumnName) == Types.DATE)
            vnFieldLength = HOST_DATETIME_FORMAT.length();
          else
            vnFieldLength = DBInfo.getFieldLength(isColumnName);
        }
      }
      else
      {
        if (DBInfo.getFieldType(isColumnName) == Types.DATE)
          vnFieldLength = HOST_DATETIME_FORMAT.length();
        else
          vnFieldLength = DBInfo.getFieldLength(isColumnName);
      }
    }

    return(vnFieldLength);
  }
}
