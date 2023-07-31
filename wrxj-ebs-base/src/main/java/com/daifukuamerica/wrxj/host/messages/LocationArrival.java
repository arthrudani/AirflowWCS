package com.daifukuamerica.wrxj.host.messages;
/****************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright ? 2005 Daifuku America Corporation  All Rights Reserved.

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

/**
 *  Class to setup a Location Arrival notification to the host when a load arrives
 *  at a Rack Location.
 *
 *  @author   A.D.
 *  @version  1.0
 *  @since    29-May-2008
 */
public class LocationArrival extends MessageOut
{
  protected final String TRAN_DATE_NAME = "dTransactionTime";
  protected final String LOADID_NAME    = "sLoadID";
  protected final String LOCATION_NAME  = "sLocation";
  
  public LocationArrival()
  {
    messageFields = new ColumnObject[]
    {
      new ColumnObject(TRAN_DATE_NAME, new Date()),
      new ColumnObject(LOADID_NAME, ""),
      new ColumnObject(LOCATION_NAME, "")
    };
    msgfmt = MessageFormatterFactory.getInstance();
    enumMessageName = MessageOutNames.LOCATION_ARRIVAL;
  }

  public void setTransactionTime(Date ipTranDate)
  {
    ColumnObject.modify(TRAN_DATE_NAME, ipTranDate, messageFields);
  }

  public void setLoadID(String isLoadID)
  {
    ColumnObject.modify(LOADID_NAME, isLoadID, messageFields);
  }
    
  public void setLocation(String isWarehouse, String isAddress)
  {
    ColumnObject.modify(LOCATION_NAME, isWarehouse + "-" + isAddress, messageFields);
  }
}
