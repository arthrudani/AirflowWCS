package com.daifukuamerica.wrxj.host.messages;

import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import java.util.HashMap;

/* Description:<BR>
 *  Host Error message builder.
 *
 * @author       A.D.
 * @version      1.0     03/02/05
 */
public class HostError extends MessageOut
{
  protected final String ERROR_CODE_NAME      = "iErrorCode";
  protected final String ORIG_SEQ_NAME        = "iOriginalSequence";
  protected final String HOST_ERROR_TEXT_NAME = "sHostErrorText";
  protected final String ERROR_MESSAGE_NAME   = "sMessage";

  public static final int DUPLICATE_DATA     = 1;
  public static final int NO_DATA_FOUND      = 2;
  public static final int ADD_ERROR          = 3;
  public static final int MODIFY_ERROR       = 4;
  public static final int DELETE_ERROR       = 5;
  public static final int INVALID_DATA       = 6; 
  public static final int DATA_QUEUE_FULL    = 7;
  public static final int BIN_EMPTY_ERROR    = 8;
  
  protected static HashMap<Integer, String> mpErrorMap = new HashMap<Integer, String>();
  
 /**
  * Default constructor. This constructor finds the correct message formatter to
  * use for this message.
  */
  public HostError()
  {
    initErrorMap();    
    messageFields = new ColumnObject[]
    {
      new ColumnObject(ERROR_CODE_NAME, Integer.valueOf(0)),
      new ColumnObject(ORIG_SEQ_NAME, Integer.valueOf(0)),
      new ColumnObject(HOST_ERROR_TEXT_NAME, ""),
      new ColumnObject(ERROR_MESSAGE_NAME, "")
    };
    msgfmt = MessageFormatterFactory.getInstance();
    enumMessageName = MessageOutNames.HOST_ERROR;
  }

 /**
   * Method allows for setting the error code.
   * @param inErrorCode the error code indicating why a message failed to be
   * integrated into WRx-J.  This parameter should be set to one of the
   * following HostError code Names:<br>
   * 
   * <CENTER>
   * <TABLE border=1>
   * <TR>
   * <TH BGCOLOR = '#CCFFFF'>HostError Code Name</TH>
   * <TH BGCOLOR = '#CCFFFF'>HostError Code Value</TH>
   * <TH BGCOLOR = '#CCFFFF'>Associated HostError String</TH>
   * </TR>
   * <TR>
   * <TD ALIGN = 'CENTER'>DUPLICATE_DATA</TD>
   * <TD ALIGN = 'CENTER'>1</TD>
   * <TD ALIGN = 'CENTER'>Duplicate data found. Data not added to WRx-j system.</TD>
   * </TR>
   * <TR>
   * <TD ALIGN = 'CENTER'>NO_DATA_FOUND</TD>
   * <TD ALIGN = 'CENTER'>2</TD>
   * <TD ALIGN = 'CENTER'>No Data Found...</TD>
   * </TR>
   * <TR>
   * <TD ALIGN = 'CENTER'>ADD_ERROR</TD>
   * <TD ALIGN = 'CENTER'>3</TD>
   * <TD ALIGN = 'CENTER'>Add operation failed.</TD>
   * </TR>
   * <TR>
   * <TD ALIGN = 'CENTER'>MODIFY_ERROR</TD>
   * <TD ALIGN = 'CENTER'>4</TD>
   * <TD ALIGN = 'CENTER'>Delete operation failed.</TD>
   * </TR>
   * <TR>
   * <TD ALIGN = 'CENTER'>DELETE_ERROR</TD>
   * <TD ALIGN = 'CENTER'>5</TD>
   * <TD ALIGN = 'CENTER'>Modify operation failed.</TD>
   * </TR>
   * <TR>
   * <TD ALIGN = 'CENTER'>INVALID_DATA</TD>
   * <TD ALIGN = 'CENTER'>6</TD>
   * <TD ALIGN = 'CENTER'>Invalid data found in message...</TD>
   * </TR>
   * <TR>
   * <TD ALIGN = 'CENTER'>DATA_QUEUE_FULL</TD>
   * <TD ALIGN = 'CENTER'>7</TD>
   * <TD ALIGN = 'CENTER'>Insufficient space available in HostToWRx data queue. No new messages accepted!</TD>
   * </TR>
   * <TR>
   * <TD ALIGN = 'CENTER'>BIN_EMPTY_ERROR</TD>
   * <TD ALIGN = 'CENTER'>8</TD>
   * <TD ALIGN = 'CENTER'>Load Retrieval failed. Load is missing.</TD>
   * </TR>
   * </TABLE>
   * </CENTER>
   */
  public void setErrorCode(int inErrorCode)
  {
    ColumnObject.modify(ERROR_CODE_NAME, Integer.valueOf(inErrorCode), messageFields);
    ColumnObject.modify(HOST_ERROR_TEXT_NAME, mpErrorMap.get(Integer.valueOf(inErrorCode)),
                        messageFields);
  }

 /**
  *  Method specifies the host sent sequence number for an error message.
  *  @param inOriginalSequence The original message sequence from the host.
  */
  public void setOriginalMessageSequence(int inOriginalSequence)
  {
    ColumnObject.modify(ORIG_SEQ_NAME, Integer.valueOf(inOriginalSequence),
                        messageFields);
  }
  
 /**
  *  Optional setter method.  Method allows for sending the original offending
  *  fields back to the host, along with a user specified error message.  This
  *  method may be called to give more details to the host than what the default
  *  {@link #setErrorCode} method specifies.
  *  @param isErrMesg an user defined error message.
  *  @param iasFields the original offending error fields.
  */
  public void setErrorMessage(String isErrMesg, String[] iasFields)
  {
    String vsMessage = isErrMesg;
    if (iasFields != null)
    {
      vsMessage += " Problem fields: ";
      for(int f = 0; f < iasFields.length; f++)
      {
        vsMessage += iasFields[f];
        if (f + 1 < iasFields.length) vsMessage += ", ";
      }
    }
    ColumnObject.modify(ERROR_MESSAGE_NAME, vsMessage, messageFields);
  }
  
  protected void initErrorMap()
  {
    mpErrorMap.put(Integer.valueOf(HostError.DUPLICATE_DATA),
                   "Duplicate data found. Data not added to WarehouseRx.");
    mpErrorMap.put(Integer.valueOf(HostError.NO_DATA_FOUND), 
                   "No Data Found...");
    mpErrorMap.put(Integer.valueOf(HostError.ADD_ERROR),
                   "Add operation failed.");
    mpErrorMap.put(Integer.valueOf(HostError.MODIFY_ERROR),
                   "Modify operation failed.");
    mpErrorMap.put(Integer.valueOf(HostError.DELETE_ERROR),
                   "Delete operation failed.");
    mpErrorMap.put(Integer.valueOf(HostError.INVALID_DATA), 
                   "Invalid data found in message.");
    mpErrorMap.put(Integer.valueOf(HostError.DATA_QUEUE_FULL), 
       "Insufficient space available in data queue. No new messages accepted!");
    mpErrorMap.put(Integer.valueOf(HostError.BIN_EMPTY_ERROR), 
                   "Load Retrieval failed. Load is missing.");
  }
}
