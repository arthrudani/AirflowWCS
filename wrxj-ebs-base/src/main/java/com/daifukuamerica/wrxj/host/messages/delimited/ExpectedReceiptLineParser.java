package com.daifukuamerica.wrxj.host.messages.delimited;

import com.daifukuamerica.wrxj.dataserver.standard.StandardPoReceivingServer;
import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.InvalidHostDataException;
import com.daifukuamerica.wrxj.host.messages.HostError;
import com.daifukuamerica.wrxj.host.messages.MessageHelper;
import com.daifukuamerica.wrxj.host.messages.MessageParser;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.jdbc.DBRuntimeException;
import com.daifukuamerica.wrxj.log.Logger;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Scanner;

/**
  *  Class to handle the parsing of Expected Receipt (ER) Line data from a host
  *  system. <p>ER Line messages from the host are of the form:
  *   <ol type="A">
  *     <li><code>'ExpectedReceiptLineMessage;ADD;field1;field2;...;fieldN;' or</code></li>
  *     <li><code>'ExpectedReceiptLineMessage;MODIFY;searchKeyField;field2;...;fieldN;'</code> or </li>
  *     <li><code>'ExpectedReceiptLineMessage;DELETE;searchKeyField1;...;searchKeyFieldN;'</code></li>
  *   </ol>
  * where the <b>';'</b> character is a delimiter.  The delimiter is configurable
  * and specified in the HostController section of the WRx-J configuration file.</p>
  *
  * <p>Each message starts with its name, and the action to be carried out. Valid
  * actions are <b>ADD</b>, <b>MODIFY</b>, or <b>DELETE</b>.</p>
  *
  * <p>In form <b>B.</b> the <b>searchKeyField</b> field represents key information
  * that is required to carry out the intended action of the message.  Care must
  * be exercised by the message sender that enough fields are present to allow the
  * MODIFY or DELETE operations to be carried out properly.  The system will
  * require enough information so that at least a unique database record locator (key) is
  * formed for forms <b>B.</b> and <b>C.</b></p>
  *
  *  @author A.D.
  *  @since 22-May-06
  */
public class ExpectedReceiptLineParser implements MessageParser
{
  protected static ColumnObject[]  mpFieldDefs = null;
  protected PurchaseOrderLineData  mpELData;
  private   StandardPoReceivingServer mpPOServer;
  private   Logger                 mpLogger;
  private   DBObject               mpDBObj;

 /**
  * Default constructor for a delimited Order line parser.
  */
  public ExpectedReceiptLineParser() throws DBException
  {
    mpDBObj = new DBObjectTL().getDBObject();
    if (!mpDBObj.checkConnected()) mpDBObj.connect();

    mpLogger = Logger.getLogger();
    mpPOServer = Factory.create(StandardPoReceivingServer.class, "ExpectedReceiptLineParser");
    mpELData  = Factory.create(PurchaseOrderLineData.class);
  }

 /**
  * Method to do delimited parsing for an Order Line.  This method performs some
  * basic checks for field counts, then executes the desired transaction.  <u><i>All</i>
  * order line messages from the host must have the Order ID. and Order Line ID.
  * specified.</u>
  * @param ipHostData The data from the HostToWrx data queue (table).
  * @throws InvalidHostDataException when there are parsing errors due to malformed
  *         messages, or problems with message content validation.
  * @throws DBRuntimeException If there is an internal error that only the system
  *         needs to resolve.
  */
  public void parse(HostToWrxData ipHostData) throws InvalidHostDataException,
                                                     DBRuntimeException
  {
    int vnAction;
    
    try
    {
      if (mpFieldDefs == null || mpFieldDefs.length == 0)
        mpFieldDefs = MessageHelper.getFieldDefintions(ipHostData.getMessageIdentifier());
    }
    catch(DBException exc)
    {
      throw new DBRuntimeException("Error getting Field definition data for " +
                                   "ExpectedReceiptLine Message...", exc);
    }

    MessageHelper.valFieldCount(ipHostData.getMessageBytes(), mpFieldDefs.length);

    InputStream vpInpStrm = new ByteArrayInputStream(ipHostData.getMessageBytes());
    Scanner vpContentScanner = new Scanner(vpInpStrm);
    vpContentScanner.useDelimiter(MessageHelper.HOST_MESSAGE_DELIM);

    String vsErrorMessage = "";
    
    if ((vnAction = MessageHelper.getDelimitedDataAction(vpContentScanner)) > 0)
    {
      MessageHelper.setDelimitedDataObjectAttrib(vpContentScanner, mpELData,
                                                 mpFieldDefs);
      vpContentScanner.close();
      
      boolean vzErrorCondition = true;
      if (mpELData.getOrderID().trim().length() == 0)
      {
        vsErrorMessage = "Expected Receipt ID. is missing!";
      }
//      else if (mpELData.getLineID().trim().length() == 0)
//      {
//        vsErrorMessage = "Line ID. is missing! Expected Receipt " + mpELData.getOrderID();
//      }
      else if (mpELData.getItem().trim().length() == 0)
      {
        vsErrorMessage = "Item ID. is missing for Expected Receipt " + mpELData.getOrderID() + 
                         " Line ID " + mpELData.getLineID() + " !";
      }
      else
      {
        vzErrorCondition = false;
      }

      if (vzErrorCondition)
      {
        rollbackOrder();
        throw new InvalidHostDataException(HostError.INVALID_DATA,
                                           vsErrorMessage + "Attempted operation failed...",
                                           ipHostData.getOriginalMessageSequence());
      }
      execTransaction(vnAction, ipHostData.getMessageSequence());
    }
    else
    {
      throw new InvalidHostDataException(HostError.INVALID_DATA,
                                         "Unknown operation attempted! Legal values " +
                                         "are ADD, MODIFY or DELETE.",
                                         ipHostData.getOriginalMessageSequence());
    }
  }

 /**
  * {@inheritDoc}
  */
  public void cleanUp()
  {
    if (mpPOServer != null)
    {
      mpPOServer.cleanUp();
      mpPOServer = null;
    }
  }

 /**
  * Method to carry out the requested add, modify or delete transaction.
  * @param inAction translation value of the requested action.
  * @param inOriginalSequence the original host message sequence.  This is part 
  *        of the data used to report errors to the host.
  * @throws InvalidHostDataException if there is a database error.
  */
  protected void execTransaction(int inAction, int inOriginalSequence)
            throws InvalidHostDataException
  {
//    int vnErrorCode = HostError.INVALID_DATA;
    
    try
    {
      switch(inAction)
      {
        case DBConstants.ADD:
//          vnErrorCode = HostError.ADD_ERROR;
          mpPOServer.addPOLine(mpELData);
                                       // If they've told us that this is the
                                       // last line, set the ER to "Expected."
          if (mpELData.getLastLine() == DBConstants.YES)
          {
                                       // Make sure we really received all the lines.
            int vnCurrentLineCount = mpPOServer.getOrderLineCount(mpELData.getOrderID(), "", "");
            int vnHostLineCount = mpPOServer.getHostLineCount(mpELData.getOrderID());
            if (vnCurrentLineCount == vnHostLineCount)
            {
              mpPOServer.setPurchaseOrderStatusValue(mpELData.getOrderID(), DBConstants.EREXPECTED);
            }
            else
            {
              throw new InvalidHostDataException(HostError.INVALID_DATA,
                    "Last record mark received but not " +
                    "enough Order Lines sent." +
                    " Expected " + vnHostLineCount +
                    " Found " + vnCurrentLineCount, inOriginalSequence);
            }
          }
          break;
 
        case DBConstants.MODIFY:
          mpELData.setKey(PurchaseOrderLineData.ORDERID_NAME, mpELData.getOrderID());
          mpELData.setKey(PurchaseOrderLineData.LINEID_NAME, mpELData.getLineID());
          mpPOServer.modifyPOLineWithValidation(mpELData);
          break;
 
        case DBConstants.DELETE:
          mpPOServer.deletePOLine(mpELData.getOrderID(), mpELData.getLineID());
      }
    }
    catch(DBException e)
    {
      InvalidHostDataException vpBadDataExcep = MessageHelper.getInvalidDataExcep(e, inOriginalSequence);
      if (vpBadDataExcep.getErrorCode() != HostError.DUPLICATE_DATA)
      {
        rollbackOrder();
      }
      throw vpBadDataExcep;
    }
  } 

 /**
  * Method deletes this order in the case that one or more lines are in error.
  */
  protected void rollbackOrder()
  {
    try
    {
      mpPOServer.deletePO(mpELData.getOrderID());
    }
    catch(DBException e)
    {
      mpLogger.logError("Attempt to delete Purchase order " + mpELData.getOrderID() + 
                        " failed.  Delete Order was initiated by invalid " +
                        "host data for order line # " + mpELData.getLineID() +
                        " and item " + mpELData.getItem() + "..." + e.getMessage());
    }
  }
}
