package com.daifukuamerica.wrxj.host.messages;

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
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import java.util.Date;

/* Description:<BR>
 *  Expected Receipt Complete outbound message builder.
 *
 * @author       A.D.
 * @version      1.0     03/02/05
 */
public class ExpectedReceiptComplete extends MessageOut
{
  protected final String TRAN_DATE_NAME  = "dTransactionTime";
  protected final String ORDERID_NAME    = "sOrderID";

 /**
  * Default constructor. This constructor finds the correct message formatter to
  * use for this message.
  */
  public ExpectedReceiptComplete()
  {
    messageFields = new ColumnObject[]
    {
        new ColumnObject(ORDERID_NAME, ""),
        new ColumnObject(TRAN_DATE_NAME, new Date())
    };
    msgfmt = MessageFormatterFactory.getInstance();
    enumMessageName = MessageOutNames.EXPECTED_RECEIPT_COMPLETE;
  }

  public void setTransactionTime(Date dAddDateTime)
  {
    ColumnObject.modify(TRAN_DATE_NAME, dAddDateTime, messageFields);
  }
  
  public void setOrderID(String sOrderID)
  {
    ColumnObject.modify(ORDERID_NAME, sOrderID, messageFields);
  }
}
