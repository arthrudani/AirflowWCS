package com.daifukuamerica.wrxj.host.messages.delimited;

/****************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright ? 2004 Daifuku America Corporation  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND
  TREATIES. NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED
  COPIED, DISTRIBUTED, REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED,
  EXPANDED, COLLECTED, COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED
  WITHOUT THE PRIOR WRITTEN CONSENT OF Daifuku America Corporation ANY
  USE OR EXPLOITATION OF THIS WORK WITHOUT AUTHORIZATION COULD SUBJECT THE
  PERPETRATOR TO CRIMINAL AND CIVIL LIABILITY.
 ****************************************************************************/

import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
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
  *  Class to handle the parsing of Order Header data from a host system. <p>Order Header
  *  messages from the host are of the form:
  *   <ol type="A">
  *     <li><code>'OrderHeaderMessage;ADD;field1;field2;...;fieldN' or</code></li>
  *     <li><code>'OrderHeaderMessage;MODIFY;searchKeyField;field2;...;fieldN'</code> or </li>
  *     <li><code>'OrderHeaderMessage;DELETE;searchKeyField1;...;searchKeyFieldN'</code></li>
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
  * MODIFY or DELETE operations can be carried out properly.  The system will
  * require enough information so that at least a unique database record locator (key) is
  * formed for forms <b>B.</b> and <b>C.</b></p>
  *
  *  @author A.D.    08-May-06
  */
public class OrderHeaderParser implements MessageParser
{
  protected static ColumnObject[]  mpFieldDefs = null;
  protected OrderHeaderData        mpOhdata;
  private   StandardOrderServer    mpOrderServer;
  private   DBObject               mpDBObj;

 /**
  * Default constructor for a delimited Order Header parser.
  */
  public OrderHeaderParser() throws DBException
  {
    mpDBObj = new DBObjectTL().getDBObject();
    if (!mpDBObj.checkConnected()) mpDBObj.connect();

    mpOrderServer = Factory.create(StandardOrderServer.class, "OrderHeaderParser");
    mpOhdata  = Factory.create(OrderHeaderData.class);
  }

 /**
  * Method to do delimited parsing for an Order Header.
  * @param ipHostData The data from the HostToWrx data queue (table).
  * @throws InvalidHostDataException when there are parsing errors due to malformed
  *         messages, or problems with message content validation.
  * @throws DBRuntimeException If there is an internal error that only the system
  *         needs to resolve.
  */
  public void parse(HostToWrxData ipHostData) throws InvalidHostDataException,
                                                     DBRuntimeException
  {
    int    vnAction;
    try
    {
      if (mpFieldDefs == null || mpFieldDefs.length == 0)
        mpFieldDefs = MessageHelper.getFieldDefintions(ipHostData.getMessageIdentifier());
    }
    catch(DBException exc)
    {
      throw new DBRuntimeException("Error getting Field definition data for " +
                                   "Order Header Message...", exc);
    }
    
    MessageHelper.valFieldCount(ipHostData.getMessageBytes(), mpFieldDefs.length);

    InputStream vpInpStrm = new ByteArrayInputStream(ipHostData.getMessageBytes());
    Scanner vpContentScanner = new Scanner(vpInpStrm);
    vpContentScanner.useDelimiter(MessageHelper.HOST_MESSAGE_DELIM);

    if ((vnAction = MessageHelper.getDelimitedDataAction(vpContentScanner)) > 0)
    {
      MessageHelper.setDelimitedDataObjectAttrib(vpContentScanner, mpOhdata,
                                                 mpFieldDefs);
      vpContentScanner.close();

      if (mpOhdata.getOrderID().trim().length() == 0)
      {
        throw new InvalidHostDataException(HostError.INVALID_DATA,
                                           "Order ID. field must be filled in! " +
                                           "Attempted operation failed...",
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
   * @throws InvalidHostDataException when attempted action is considered incorrect.
   */
  protected void execTransaction(int inAction, int inOriginalSequence)
            throws InvalidHostDataException
  {  
    try
    {
      switch(inAction)
      {
        case DBConstants.ADD:
/*----------------------------------------------------------------------------
      The order status by default is ORBUILDING unless specified; set the 
      Next Status to UNKNOWN in this case to determine actual order status
      once we receive all lines.
  ----------------------------------------------------------------------------*/
          if (mpOhdata.getOrderStatus() == DBConstants.ORBUILDING)
            mpOhdata.setNextStatus(DBConstants.UNKNOWN);
          else
            mpOhdata.setNextStatus(mpOhdata.getOrderStatus());

          mpOhdata.setOrderStatus(DBConstants.ORBUILDING);
          mpOrderServer.addOrderHeader(mpOhdata);
          break;

        case DBConstants.MODIFY:
          mpOhdata.setKey(OrderHeaderData.ORDERID_NAME, mpOhdata.getOrderID());
          mpOrderServer.modifyOrderHeader(mpOhdata);
          break;

        case DBConstants.DELETE:
          mpOrderServer.deleteOrder(mpOhdata.getOrderID());
      }
    }
    catch(DBException e)
    {
      throw MessageHelper.getInvalidDataExcep(e, inOriginalSequence);
    }
  }
}
