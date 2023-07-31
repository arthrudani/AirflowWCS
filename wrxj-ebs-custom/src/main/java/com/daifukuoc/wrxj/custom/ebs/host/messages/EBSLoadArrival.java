package com.daifukuoc.wrxj.custom.ebs.host.messages;

import com.daifukuamerica.wrxj.host.messages.MessageFormatterFactory;
import com.daifukuamerica.wrxj.host.messages.MessageOut;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import java.util.Date;

/**
 *  Class to setup a Load Arrival notification to the host when a load arrives
 *  at a station.
 *
 *  @author   A.D.
 *  @version  1.0
 *  @since    14-Nov-2007
 */
public class EBSLoadArrival extends MessageOut
{
  private final String TRAN_DATE_NAME = "dTransactionTime";
  //private final String ORDERID_NAME   = "sOrderID";
  private final String LOADID_NAME    = "sLoadID";
  private final String ITEM_NAME    = "sItem";
  private final String STATION_NAME   = "sStationName";

  public EBSLoadArrival()
  {
    messageFields = new ColumnObject[]
    {
      new ColumnObject(TRAN_DATE_NAME, new Date()),
//      new ColumnObject(ORDERID_NAME, ""),
      new ColumnObject(LOADID_NAME, ""),
      new ColumnObject(ITEM_NAME, ""),
      new ColumnObject(STATION_NAME, "")
    };
    msgfmt = MessageFormatterFactory.getInstance();
    enumMessageName = MessageOutNames.LOAD_ARRIVAL;
  }

  public void setTransactionTime(Date ipTranDate)
  {
    ColumnObject.modify(TRAN_DATE_NAME, ipTranDate, messageFields);
  }

//  public void setOrderID(String sOrderID)
//  {
//    ColumnObject.modify(ORDERID_NAME, sOrderID, messageFields);
//  }

  public void setLoadID(String isLoadID)
  {
    ColumnObject.modify(LOADID_NAME, isLoadID, messageFields);
  }
  
  public void setItem(String isItem)
  {
    ColumnObject.modify(ITEM_NAME, isItem, messageFields);
  }
    
  public void setArrivalStation(String isArrivalStation)
  {
    ColumnObject.modify(STATION_NAME, isArrivalStation, messageFields);
  }
}
