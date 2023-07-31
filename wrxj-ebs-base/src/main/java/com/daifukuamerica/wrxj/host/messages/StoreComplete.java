package com.daifukuamerica.wrxj.host.messages;

import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.Date;

/* Description:<BR>
 *  Store Complete outbound message builder.
 *
 * @author   A.D.
 * @version  1.0
 * @since    02-Mar-2005
 */
public class StoreComplete extends MessageOut 
{
  protected final String TRAN_DATE_NAME = "dTransactionTime";
  protected final String ORDERID_NAME   = "sOrderID";
  protected final String ITEM_NAME      = "sItem";
  protected final String LOT_NAME       = "sLot";
  protected final String RECV_QTY_NAME  = "fReceivedQuantity";
  protected final String LOADID_NAME    = "sLoadID";
  protected final String STATION_NAME   = "sStationName";
  protected final String USERID_NAME    = "sUserID";
  
 /**
  * Default constructor. This constructor finds the correct message formatter to
  * use for this message.
  */
  public StoreComplete()
  {
    messageFields = new ColumnObject[]
    {
      new ColumnObject(TRAN_DATE_NAME, new Date()),
      new ColumnObject(ORDERID_NAME, ""),
      new ColumnObject(ITEM_NAME, ""),
      new ColumnObject(LOT_NAME, ""),
      new ColumnObject(RECV_QTY_NAME, Double.valueOf(0.0)),
      new ColumnObject(LOADID_NAME, ""),
      new ColumnObject(STATION_NAME, ""),
      new ColumnObject(USERID_NAME, "")
    };
    msgfmt = MessageFormatterFactory.getInstance();
    enumMessageName = MessageOutNames.STORE_COMPLETE;
  }

  public void setTransactionTime(Date dAddDateTime)
  {
    ColumnObject.modify(TRAN_DATE_NAME, dAddDateTime, messageFields);
  }
  
  public void setPurchaseOrderID(String sOrderID)
  {
    ColumnObject.modify(ORDERID_NAME, sOrderID, messageFields);
  }
  
  public void setItem(String sItem)
  {
    ColumnObject.modify(ITEM_NAME, sItem, messageFields);
  }

  public void setLot(String sLot)
  {
    ColumnObject.modify(LOT_NAME, sLot, messageFields);
  }
  
  public void setReceivedLoadID(String sLoadID)
  {
    ColumnObject.modify(LOADID_NAME, sLoadID, messageFields);
  }

  public void setReceiveQuantity(double recvQuantity)
  {
    ColumnObject.modify(RECV_QTY_NAME,
                        SKDCUtility.getTrucatedDoubleObj(recvQuantity),
                        messageFields);
  }
  
  public void setUserID(String sUserID)
  {
    ColumnObject.modify(USERID_NAME, sUserID, messageFields);
  }
 
  public void setStoreStation(String sStationName)
  {
    ColumnObject.modify(STATION_NAME, sStationName, messageFields);
  }
}
