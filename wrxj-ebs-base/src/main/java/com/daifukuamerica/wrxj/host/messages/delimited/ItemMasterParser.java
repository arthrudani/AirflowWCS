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

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
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
  *  Class to handle the parsing of Item Master data from a host system. <p>Item Master
  *  messages from the host are of the form:
  *   <ol type="A">
  *     <li><code>'ItemMasterMessage;ADD;field1;field2;...;fieldN' or</code></li>
  *     <li><code>'ItemMasterMessage;MODIFY;searchKeyField;field2;...;fieldN'</code> or </li>
  *     <li><code>'ItemMasterMessage;DELETE;searchKeyField1;...;searchKeyFieldN'</code></li>
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
public class ItemMasterParser implements MessageParser 
{
  protected static ColumnObject[]  mpFieldDefs = null;
  protected ItemMasterData  mpItemData;
  private StandardInventoryServer mpInventoryServer;
  private   DBObject      mpDBObj;

 /**
  * Default constructor for a delimited Item Master parser.
  */
  public ItemMasterParser() throws DBException
  {
    mpDBObj = new DBObjectTL().getDBObject();
    if (!mpDBObj.checkConnected()) mpDBObj.connect();

    mpInventoryServer = Factory.create(StandardInventoryServer.class, "ItemMasterParser");
    mpItemData  = Factory.create(ItemMasterData.class);
  }
  
 /**
  * Method to do delimited parsing for an Item master.
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
                                   "Item Master Message...", exc);
    }
                                       // Make sure the number of fields is what
                                       // is expected.
    MessageHelper.valFieldCount(ipHostData.getMessageBytes(), mpFieldDefs.length);
    
    InputStream vpInpStrm = new ByteArrayInputStream(ipHostData.getMessageBytes());
    Scanner vpContentScanner = new Scanner(vpInpStrm);
    vpContentScanner.useDelimiter(MessageHelper.HOST_MESSAGE_DELIM);

    if ((vnAction = MessageHelper.getDelimitedDataAction(vpContentScanner)) > 0)
    {
      MessageHelper.setDelimitedDataObjectAttrib(vpContentScanner, mpItemData,
                                                 mpFieldDefs);
      vpContentScanner.close();

      if (mpItemData.getItem().trim().length() == 0)
      {
        vpContentScanner.close();
        throw new InvalidHostDataException(HostError.INVALID_DATA,
                                           "Item field must be filled in! Attempted operation failed...",
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
    if (mpInventoryServer != null)
    {
      mpInventoryServer.cleanUp();
      mpInventoryServer = null;
    }
  }

 /**
  *  Method to carry out the requested add, modify or delete transaction.
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
          mpInventoryServer.addItemMaster(mpItemData);
          break;
    
        case DBConstants.MODIFY:
          mpItemData.setKey(ItemMasterData.ITEM_NAME, mpItemData.getItem());
          mpInventoryServer.updateItemInfo(mpItemData);
          break;

        case DBConstants.DELETE:
          mpInventoryServer.deleteItemMaster(mpItemData.getItem());
      }
    }
    catch(DBException e)
    {
      throw MessageHelper.getInvalidDataExcep(e, inOriginalSequence);
    }
  }
}
