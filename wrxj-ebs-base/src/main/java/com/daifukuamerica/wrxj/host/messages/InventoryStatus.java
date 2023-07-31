package com.daifukuamerica.wrxj.host.messages;

import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.util.SKDCUtility;

/* Description:<BR>
 *  Inventory Status outbound message builder.
 *
 * @author       A.D.
 * @version      1.0     03/02/05
 */
public class InventoryStatus extends InventoryAdjustment
{
  protected final String QUARANTINE_QTY_NAME = "fQuantity";

 /**
  * Default constructor. This constructor finds the correct message formatter to
  * use for this message.
  */
  public InventoryStatus()
  {
    messageFields = new ColumnObject[]
    {
      new ColumnObject(ITEM_NAME, ""),
      new ColumnObject(LOT_NAME, ""),
      new ColumnObject(QUARANTINE_QTY_NAME, Double.valueOf(0.0)),
      new ColumnObject(LOADID_NAME, ""),
      new ColumnObject(REASON_CODE_NAME, ""),
      new ColumnObject(USERID_NAME, "")
    };
    msgfmt = MessageFormatterFactory.getInstance();
    enumMessageName = MessageOutNames.INVENTORY_STATUS;
  }
  
 /**
  * Method sets the amount being quarantined.
  * @param fQuarantineQty amount being quarantined or being released from quarantine.
  */
  public void setQuantity(double fQuarantineQty)
  {
    ColumnObject.modify(QUARANTINE_QTY_NAME,
                        SKDCUtility.getTrucatedDoubleObj(fQuarantineQty),
                        messageFields);
  }
}
