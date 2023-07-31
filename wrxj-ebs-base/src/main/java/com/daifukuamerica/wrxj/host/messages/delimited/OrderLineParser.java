package com.daifukuamerica.wrxj.host.messages.delimited;

import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
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
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Scanner;


/**
  *  Class to handle the parsing of Order Line data from a host system. <p>Order Line
  *  messages from the host are of the form:
  *   <ol type="A">
  *     <li><code>'OrderLineMessage;ADD;field1;field2;...;fieldN' or</code></li>
  *     <li><code>'OrderLineMessage;MODIFY;searchKeyField;field2;...;fieldN'</code> or </li>
  *     <li><code>'OrderLineMessage;DELETE;searchKeyField1;...;searchKeyFieldN'</code></li>
  *   </ol>
  * where the <b>';'</b> character is a delimiter.  The delimiter is configurable
  * and specified in the HostController section of the WRx-J configuration file.</p>
  *
  * <p>Each message starts with its name, and the action to be carried out. Valid
  * actions are <b>ADD</b>, <b>MODIFY</b>, and <b>DELETE</b>.</p>
  *
  * <p>In form <b>B.</b> the <b>searchKeyField</b> field represents key information
  * that is required to carry out the intended action of the message.  Care must
  * be exercised by the message sender that enough fields are present to allow the
  * MODIFY or DELETE operations to be carried out properly.  The system will
  * require enough information so that at least a unique database record locator (key) is
  * formed for forms <b>B.</b> and <b>C.</b></p>
  *
  *  @author A.D.    08-May-06
  */
public class OrderLineParser implements MessageParser
{
  protected static ColumnObject[]  mpFieldDefs = null;
  protected OrderLineData mpOldata;
  protected StandardOrderServer   mpOrderServer;
//  private   Logger        mpLogger;
  private   DBObject      mpDBObj;

 /**
  * Default constructor for a delimited Order line parser.
  */
  public OrderLineParser() throws DBException
  {
    mpDBObj = new DBObjectTL().getDBObject();
    if (!mpDBObj.checkConnected()) mpDBObj.connect();

//    mpLogger = Logger.getLogger();
    mpOrderServer = Factory.create(StandardOrderServer.class, "OrderLineParser");
    mpOldata  = Factory.create(OrderLineData.class);
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
                                   "OrderLine Message...", exc);
    }
    MessageHelper.valFieldCount(ipHostData.getMessageBytes(), mpFieldDefs.length);

    InputStream vpInpStrm = new ByteArrayInputStream(ipHostData.getMessageBytes());
    Scanner vpContentScanner = new Scanner(vpInpStrm);
    vpContentScanner.useDelimiter(MessageHelper.HOST_MESSAGE_DELIM);

    String vsErrorMessage = "";
    
    if ((vnAction = MessageHelper.getDelimitedDataAction(vpContentScanner)) > 0)
    {
      MessageHelper.setDelimitedDataObjectAttrib(vpContentScanner, mpOldata,
                                                 mpFieldDefs);
      vpContentScanner.close();
      
      boolean vzErrorCondition = true;
      if (mpOldata.getOrderID().trim().length() == 0)
      {
        vsErrorMessage = "Order ID. is missing!";
      }
      else
      {
        vzErrorCondition = false;
      }

      if (vzErrorCondition)
      {
        try { rollbackOrder(); }
        catch(DBException exc) { throw new DBRuntimeException(exc); }
        throw new InvalidHostDataException(HostError.INVALID_DATA,
                                           vsErrorMessage + " Attempted operation failed...",
                                           ipHostData.getOriginalMessageSequence());
      }
      execTransaction(vnAction, ipHostData.getOriginalMessageSequence());
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
    if (mpOrderServer != null)
    {
      mpOrderServer.cleanUp();
      mpOrderServer = null;
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
            throws InvalidHostDataException, DBRuntimeException
  {
//    int vnErrorCode = HostError.INVALID_DATA;

    try
    {
      if (!mpOrderServer.orderHeaderExists(mpOldata.getOrderID()))
      {
        throw new InvalidHostDataException(HostError.INVALID_DATA,
                                          "Order header record missing for order " +
                                          mpOldata.getOrderID() + "...", inOriginalSequence);
      }

      int vnOrderType = mpOrderServer.getOrderTypeValue(mpOldata.getOrderID());
      switch(inAction)
      {
        case DBConstants.ADD:
//          vnErrorCode = HostError.ADD_ERROR;
          if (vnOrderType == DBConstants.FULLLOADOUT)
          {
            if (mpOldata.getLoadID().trim().length() == 0)
              throw new DBException("Load Orders must have a load specified. Order = " +
                                    mpOldata.getOrderID());
            else
              mpOrderServer.addOrderLine(mpOldata, false);
          }
          else if (vnOrderType == DBConstants.ITEMORDER)
          {
            mpOrderServer.addOrderLine(mpOldata);
          }
          else
          {
            throw new DBException("Order Type for order " + mpOldata.getOrderID() +
                                  " not found!");
          }
                                       // If they've told us that this is the
                                       // last line, see what the order status
                                       // should be.
          if (mpOldata.getLastLine() == DBConstants.YES)
          {
                                       // Make sure we really received all the lines.
            int vnCurrentLineCount = mpOrderServer.getOrderLineCount(mpOldata.getOrderID(), "", "");
            int vnHostLineCount = mpOrderServer.getOrderHostLineCount(mpOldata.getOrderID());
            
            if (vnCurrentLineCount == vnHostLineCount)
            {
              mpOrderServer.setHostOrderStatus(mpOldata.getOrderID());
            }
            else
            {
              throw new InvalidHostDataException(HostError.INVALID_DATA,
                                                 "Last record mark received but not " +
                                                 "enough Order Lines sent.", inOriginalSequence);
            }
          }
          break;
 
        case DBConstants.MODIFY:
//          vnErrorCode = HostError.MODIFY_ERROR;
          mpOrderServer.modifyOrderLine(mpOldata, true);
          break;
 
        case DBConstants.DELETE:
//          vnErrorCode = HostError.DELETE_ERROR;
          mpOrderServer.deleteOrderLine(mpOldata.getOrderID(), mpOldata.getLineID());
      }
    }
    catch(DBException e)
    {
      InvalidHostDataException vpBadDataExcep = MessageHelper.getInvalidDataExcep(e, inOriginalSequence);
      if (vpBadDataExcep.getErrorCode() != HostError.DUPLICATE_DATA)
      {
        try { rollbackOrder(); }
        catch(DBException exc) { throw new DBRuntimeException(exc); }
      }
      throw vpBadDataExcep;
    }
  } 

 /**
  * Method deletes this order in the case that one or more lines are in error.
  */
  protected void rollbackOrder() throws DBException
  {
    try
    {
      mpOrderServer.deleteOrder(mpOldata.getOrderID());
    }
    catch(DBException e)
    {
      throw new DBException("Attempt to delete order " + mpOldata.getOrderID() + 
                            " failed.  Delete Order was initiated by invalid " +
                            "host data for item " + mpOldata.getItem() + " and lot " +
                            mpOldata.getOrderLot() + "...", e);
    }
  }
}
