package com.daifukuamerica.wrxj.host.messages.fixedlength;

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

import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.host.InvalidHostDataException;
import com.daifukuamerica.wrxj.host.messages.HostError;
import com.daifukuamerica.wrxj.host.messages.MessageHelper;
import com.daifukuamerica.wrxj.host.messages.delimited.ExpectedReceiptLineParser;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBRuntimeException;

/**
  *  Class to handle the parsing of Expected Receipt (ER) Line data from a host
  *  system. <p>ER Line messages from the host are of the form:
  *   <ol type="A">
  *     <li><code>'ExpectedReceiptLine     ADD   field1     field2     ...     fieldN' or</code></li>
  *     <li><code>'ExpectedReceiptLine     MODIFYsearchKeyField     field2     ...     fieldN'</code> or </li>
  *     <li><code>'ExpectedReceiptLine     DELETEsearchKeyField1     ...     searchKeyFieldN'</code></li>
  *   </ol>
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
  *  @since 29-May-06
  */
public class FixedLengthExpectedReceiptLineParser extends ExpectedReceiptLineParser
{
 /**
  * Default constructor for a fixed length Expected Receipt Line parser.
  */
  public FixedLengthExpectedReceiptLineParser() throws DBException
  {
    super();
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
  @Override
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
                                   "ExpectedReceiptLine Message...", exc);
    }
    
    String vsErrorMessage = "";
    
    if ((vnAction = MessageHelper.getFixedLengthDataAction(ipHostData.getMessageBytes())) > 0)
    {
      MessageHelper.setFixedLenDataObjectAttrib(ipHostData.getMessageBytes(),
                                                mpELData, mpFieldDefs, true);

      boolean vzErrorCondition = true;
      if (mpELData.getOrderID().trim().length() == 0)
      {
        vsErrorMessage = "Expected Receipt ID. is missing!";
      }
      else if (mpELData.getLineID().trim().length() == 0)
      {
        vsErrorMessage = "Line ID. is missing! Expected Receipt " + mpELData.getOrderID();
      }
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
                                           vsErrorMessage + " Attempted operation failed...",
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
}
