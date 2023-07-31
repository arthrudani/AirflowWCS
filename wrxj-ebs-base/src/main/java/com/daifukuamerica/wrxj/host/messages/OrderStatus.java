package com.daifukuamerica.wrxj.host.messages;

/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright (c) 2004-2009 Daifuku America Corporation.  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED, COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/

import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.log.Logger;
import java.util.Date;

/**
 * Description:<BR>
 * Order Status outbound message builder.
 * 
 * @author A.D.
 * @version 1.0 03/02/05
 */
public class OrderStatus extends MessageOut
{
  private Logger logger = Logger.getLogger();
  
  // Message field names
  private final String TRAN_DATE_NAME    = "dTransactionTime";
  private final String ORDERID_NAME      = "sOrderID";
  private final String ORDER_STATUS_NAME = "sOrderStatus";
  
  /**
   * Default constructor. This constructor finds the correct message formatter
   * to use for this message.
   */
  public OrderStatus()
  {
    messageFields = new ColumnObject[]
    {
      new ColumnObject(TRAN_DATE_NAME, new Date()),
      new ColumnObject(ORDERID_NAME, ""),
      new ColumnObject(ORDER_STATUS_NAME, "")
    };
    msgfmt = MessageFormatterFactory.getInstance();
    enumMessageName = MessageOutNames.ORDER_STATUS;
  }

  /*========================================================================*/
  /*  Field setters                                                         */
  /*========================================================================*/

  public void setTransactionTime(Date dAddDateTime)
  {
    ColumnObject.modify(TRAN_DATE_NAME, dAddDateTime, messageFields);
  }
  
  public void setOrderID(String sOrderID)
  {
    ColumnObject.modify(ORDERID_NAME, sOrderID, messageFields);
  }
  
  public void setOrderStatus(int iOrderStatus)
  {
    try
    {
      String sOrderStatus = DBTrans.getStringValue(
          OrderHeaderData.ORDERSTATUS_NAME, iOrderStatus);
      ColumnObject.modify(ORDER_STATUS_NAME, sOrderStatus, messageFields);
    }
    catch(NoSuchFieldException nsf)
    {
      logger.logError(nsf.getMessage());
    }
  }
}
