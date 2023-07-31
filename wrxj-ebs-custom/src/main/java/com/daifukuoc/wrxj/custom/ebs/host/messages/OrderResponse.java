package com.daifukuoc.wrxj.custom.ebs.host.messages;

/* ***************************************************************************
  $Workfile: OrderStatus.java$
  $Revision: 17$
  $Date: 6/30/2010 6:09:53 PM$

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
import com.daifukuamerica.wrxj.host.messages.MessageFormatterFactory;
import com.daifukuamerica.wrxj.host.messages.MessageOut;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
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
public class OrderResponse extends MessageOut
{
  private Logger logger = Logger.getLogger();
  
  // Message field names
  private final String ORDERID_NAME      = "sOrderID";
  private final String QTY_SCHEDULED_NAME = "iQtyScheduled";
  private final String ORDER_STATUS_NAME = "sOrderStatus";
  private final String ERROR_CODE_NAME = "iErrorCode";
  private final String ERROR_DESC_NAME = "sErrorDesc";
  
  /**
   * Default constructor. This constructor finds the correct message formatter
   * to use for this message.
   */
  public OrderResponse()
  {
    messageFields = new ColumnObject[]
    {
      new ColumnObject(ORDERID_NAME, ""),
      new ColumnObject(QTY_SCHEDULED_NAME, ""),
      new ColumnObject(ORDER_STATUS_NAME, ""),
      new ColumnObject(ERROR_CODE_NAME, ""),
      new ColumnObject(ERROR_DESC_NAME, "")
    };
    msgfmt = MessageFormatterFactory.getInstance();
    enumMessageName = MessageOutNames.ORDER_RESPONSE;
  }

  /*========================================================================*/
  /*  Field setters                                                         */
  /*========================================================================*/

  
  public void setOrderID(String sOrderID)
  {
    ColumnObject.modify(ORDERID_NAME, sOrderID, messageFields);
  }

  public void setQtyScheduled(double iQty)
  {
	  ColumnObject.modify(QTY_SCHEDULED_NAME, iQty, messageFields);
  }

  public void setOrderStatus(int  iStatus)
  {
    ColumnObject.modify(ORDER_STATUS_NAME, iStatus, messageFields);
  }
  
  public void setErrorCode(int iErrorCode)
  {
	  ColumnObject.modify(ERROR_CODE_NAME, iErrorCode, messageFields);
  }
  
  public void setErrorDesc(String sErrorDesc)
  {
    ColumnObject.modify(ERROR_DESC_NAME, sErrorDesc, messageFields);
  }


}
