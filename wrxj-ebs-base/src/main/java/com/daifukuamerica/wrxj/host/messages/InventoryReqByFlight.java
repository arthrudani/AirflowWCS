package com.daifukuamerica.wrxj.host.messages;

import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import java.util.Date;

public class InventoryReqByFlight extends MessageOut
{
  private final String TRAN_DATE_NAME  = "dTransactionTime";
  private final String ORDERID_NAME    = "sOrderID";

  /**
   * Default constructor. This constructor finds the correct message formatter
   * to use for this message.
   */
  public InventoryReqByFlight()
  {
    messageFields = new ColumnObject[]
    {
      new ColumnObject(ORDERID_NAME, ""),
      new ColumnObject(TRAN_DATE_NAME, new Date())
    };
    enumMessageName = MessageOutNames.INVENTORY_REQUEST_BY_FLIGHT;
    msgfmt = MessageFormatterFactory.getInstance();
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
