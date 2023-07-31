package com.daifukuamerica.wrxj.host.messages;

import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.Date;

public class PickComplete extends MessageOut 
{
  protected final String TRAN_DATE_NAME = "dTransactionTime";
  protected final String ORDERID_NAME   = "sOrderID";
  protected final String ITEM_NAME      = "sItem";
  protected final String LOT_NAME       = "sLot";
  protected final String PICK_QTY_NAME  = "fPickQuantity";
  protected final String LOADID_NAME    = "sLoadID";
  protected final String STATION_NAME   = "sStationName";
  protected final String USERID_NAME    = "sUserID";

  public PickComplete()
  {
    messageFields = new ColumnObject[]
    {
      new ColumnObject(TRAN_DATE_NAME, new Date()),
      new ColumnObject(ORDERID_NAME, ""),
      new ColumnObject(ITEM_NAME, ""),
      new ColumnObject(LOT_NAME, ""),
      new ColumnObject(PICK_QTY_NAME, Double.valueOf(0.0)),
      new ColumnObject(LOADID_NAME, ""),
      new ColumnObject(STATION_NAME, ""),
      new ColumnObject(USERID_NAME, "")
    };
    enumMessageName = MessageOutNames.PICK_COMPLETE;
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
  
  public void setItem(String sItem)
  {
    ColumnObject.modify(ITEM_NAME, sItem, messageFields);
  }

  public void setLot(String sLot)
  {
    ColumnObject.modify(LOT_NAME, sLot, messageFields);
  }

  public void setPickQuantity(double pickQuantity)
  {
    ColumnObject.modify(PICK_QTY_NAME,
                        SKDCUtility.getTrucatedDoubleObj(pickQuantity),
                        messageFields);
  }
  
  public void setPickLoadID(String sLoadID)
  {
    ColumnObject.modify(LOADID_NAME, sLoadID, messageFields);
  }
  
  public void setPickStation(String sPickStation)
  {
    ColumnObject.modify(STATION_NAME, sPickStation, messageFields);
  }

  public void setUserID(String sUserID)
  {
    ColumnObject.modify(USERID_NAME, sUserID, messageFields);
  }
}
