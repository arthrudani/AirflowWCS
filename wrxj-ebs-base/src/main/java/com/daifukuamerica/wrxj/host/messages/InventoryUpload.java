package com.daifukuamerica.wrxj.host.messages;

import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.util.SKDCUtility;

/* Description:<BR>
 *  Inventory upload message builder.
 *
 * @author   A.D.
 * @version  1.0
 * @since    02-Mar-2005
 */
public class InventoryUpload extends MessageOut
{
  protected final String ITEM_NAME      = "sItem";
  protected final String LOT_NAME       = "sLot";
  protected final String TOTAL_QTY_NAME = "fQuantity";
  protected final String HOLD_RSN_NAME  = "sHoldReason";
  protected final String WAREHOUSE_NAME = "sWarehouse";
  protected final String LAST_MARK_NAME = "sLastRecord";

 /**
  * Default constructor. This constructor finds the correct message formatter to
  * use for this message.
  */
  public InventoryUpload()
  {
    messageFields = new ColumnObject[]
    {
      new ColumnObject(ITEM_NAME, ""),
      new ColumnObject(LOT_NAME, ""),
      new ColumnObject(TOTAL_QTY_NAME, Double.valueOf(0.0)),
      new ColumnObject(HOLD_RSN_NAME, ""),
      new ColumnObject(WAREHOUSE_NAME, ""),
      new ColumnObject(LAST_MARK_NAME, "")
    };
    msgfmt = MessageFormatterFactory.getInstance();
    enumMessageName = MessageOutNames.INVENTORY_UPLOAD;
  }

  public void setItem(String sItem)
  {
    ColumnObject.modify(ITEM_NAME, sItem, messageFields);
  }

  public void setLot(String sLot)
  {
    ColumnObject.modify(LOT_NAME, sLot, messageFields);
  }

  public void setQuantity(double fHoldQuantity)
  {
    ColumnObject.modify(TOTAL_QTY_NAME,
                        SKDCUtility.getTruncatedDouble(fHoldQuantity),
                        messageFields);
  }
  
  public void setHoldReason(String sHoldReason)
  {
    ColumnObject.modify(HOLD_RSN_NAME, sHoldReason, messageFields);
  }
  
 /**
  *  Sets the warehouse inventory is located in.
  *  @param sWarehouse the inventory warehouse.
  */
  public void setWarehouse(String sWarehouse)
  {
    ColumnObject.modify(WAREHOUSE_NAME, sWarehouse, messageFields);
  }

  public void setLastRecordFlag()
  {
    ColumnObject.modify(LAST_MARK_NAME, "Y", messageFields);
  }
}
