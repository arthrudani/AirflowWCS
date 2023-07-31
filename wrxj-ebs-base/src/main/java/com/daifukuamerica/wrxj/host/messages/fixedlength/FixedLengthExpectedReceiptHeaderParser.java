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
import com.daifukuamerica.wrxj.host.messages.delimited.ExpectedReceiptHeaderParser;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBRuntimeException;

 /**
  *  Class to handle the parsing of Expected Receipt (ER) Header data from a
  *  host system. <p>ER Header messages from the host are of the form:
  *   <ol type="A">
  *     <li><code>'ExpectedReceiptHeader     ADD   field1     field2     ...     fieldN' or</code></li>
  *     <li><code>'ExpectedReceiptHeader     MODIFYsearchKeyField     field2     ...     fieldN'</code> or </li>
  *     <li><code>'ExpectedReceiptHeader     DELETEsearchKeyField1     ...     searchKeyFieldN'</code></li>
  *   </ol>
  * <p>Each message starts with its name, and the action to be carried out. Valid
  * actions are <b>ADD</b>, <b>MODIFY</b>, or <b>DELETE</b>.</p>
  *
  * <p>In form <b>B.</b> the <b>searchKeyField</b> field represents key information
  * that is required to carry out the intended action of the message.  Care must
  * be exercised by the message sender that enough fields are present to allow the
  * MODIFY or DELETE operations can be carried out properly.  The system will
  * require enough information so that at least a unique database record locator (key) is
  * formed for forms <b>B.</b> and <b>C.</b></p>
  *
  *  @author A.D.
  *  @since 22-May-06
  */
public class FixedLengthExpectedReceiptHeaderParser extends ExpectedReceiptHeaderParser
{
 /**
  * Default constructor for a Fixed Length Expected Receipt Header parser.
  */
  public FixedLengthExpectedReceiptHeaderParser() throws DBException
  {
    super();
  }

 /**
  * Method to do delimited parsing for an Order Header.
  * @param ipHostData The data from the HostToWrx data queue (table).
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
                                   "ExpectedReceiptHeader Message...", exc);
    }

    if ((vnAction = MessageHelper.getFixedLengthDataAction(ipHostData.getMessageBytes())) > 0)
    {
      MessageHelper.setFixedLenDataObjectAttrib(ipHostData.getMessageBytes(),
                                                mpEhdata, mpFieldDefs, true);

      if (mpEhdata.getOrderID().trim().length() == 0)
      {
        throw new InvalidHostDataException("Purchase Order ID. field must be filled in! " +
                                           "Attempted operation failed...");
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
}