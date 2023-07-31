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
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.Date;

/* Description:<BR>
 *  Inventory Adjust message builder.
 *
 * @author       A.D.
 * @version      1.0     03/02/05
 */
public class InventoryAdjustment extends MessageOut
{
  protected final String TRAN_DATE_NAME   = "dTransactionTime";
  protected final String ITEM_NAME        = "sItem";
  protected final String LOT_NAME         = "sLot";
  protected final String ADJUSTQTY_NAME   = "fAdjustQuantity";
  protected final String LOADID_NAME      = "sLoadID";
  protected final String REASON_CODE_NAME = "sReasonCode";
  protected final String USERID_NAME      = "sUserID";

 /**
  * Default constructor. This constructor finds the correct message formatter to
  * use for this message.
  */
  public InventoryAdjustment()
  {
    messageFields = new ColumnObject[]
    {
      new ColumnObject(TRAN_DATE_NAME, new Date()),
      new ColumnObject(ITEM_NAME, ""),
      new ColumnObject(LOT_NAME, ""),
      new ColumnObject(ADJUSTQTY_NAME, Double.valueOf(0.0)),
      new ColumnObject(LOADID_NAME, ""),
      new ColumnObject(REASON_CODE_NAME, ""),
      new ColumnObject(USERID_NAME, "")
    };
    msgfmt = MessageFormatterFactory.getInstance();
    enumMessageName = MessageOutNames.INVENTORY_ADJUST;
  }

  public void setTransactionTime(Date dAddDateTime)
  {
    ColumnObject.modify(TRAN_DATE_NAME, dAddDateTime, messageFields);
  }
  
  public void setItem(String sItem)
  {
    ColumnObject.modify(ITEM_NAME, sItem, messageFields);
  }

  public void setLot(String sLot)
  {
    ColumnObject.modify(LOT_NAME, sLot, messageFields);
  }

 /**
  * Method allows for setting the amount being adjusted.
  * @param fAdjustQuantity amount being adjusted. It is assumed that a positive
  *        quantity implies a quantity increase, and a negative value implies
  *        a quantity decrease.
  */
  public void setAdjustmentAmount(double fAdjustQuantity)
  {
    ColumnObject.modify(ADJUSTQTY_NAME,
                        SKDCUtility.getTrucatedDoubleObj(fAdjustQuantity),
                        messageFields);
  }

  public void setLoadID(String sLoadID)
  {
    ColumnObject.modify(LOADID_NAME, sLoadID, messageFields);
  }
  
  public void setReasonCode(String sReasonCode)
  {
    ColumnObject.modify(REASON_CODE_NAME, sReasonCode, messageFields);
  }

 /**
  * Method sets the user id. of the user that performed the adjustment.
  * @param sUserID the system id. of the user.
  */
  public void setUserID(String sUserID)
  {
    ColumnObject.modify(USERID_NAME, sUserID, messageFields);
  }
}
