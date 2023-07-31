package com.daifukuoc.wrxj.custom.ebs.dbadapter.data;


import java.util.List;
import java.util.Map;

/* ***************************************************************************
  $Workfile: PurchaseOrderLine.java$
  $Revision: 21$
  $Date: 7/20/2015 2:30:54 PM$

  Copyright (c) 2004-2008 Daifuku America Corporation  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLine;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBResultSet;

/**
 * Description:<BR>
 *   Concrete implementation of AbstractOrder class for regular order lines.
 *   (those belonging to Sales and internal orders).  This Class will handle
 *   Order Line specific operations.
 *
 * @author       sbw
 * @author       A.D.
 * @version      1.0      05/30/02
 * @version      2.0      10/13/04
 */
public class EBSPurchaseOrderLine extends BaseDBInterface
{
  private DBResultSet mpDBResultSet;
  private EBSPurchaseOrderLineData mpPOLData;

  public EBSPurchaseOrderLine()
  {
    super("EBSPurchaseOrderLine");
    mpPOLData = Factory.create(EBSPurchaseOrderLineData.class);
  }

 
  public List<Map> getTrayArrivalPurchaseOrderListForEmulation()
         throws DBException
  {                                    
    StringBuilder vpSql = new StringBuilder("SELECT pol.sloadid, poh.sstorestation ")
               .append(" FROM PurchaseOrderHeader poh, PurchaseOrderLine pol ")
               .append(" WHERE ")             
               .append(" poh.sOrderID = pol.sOrderID AND ")    
               .append(" poh.sStoreStation <> '' AND pol.sLoadid <> '' ")             
               .append(" ORDER BY poh.dExpectedDate DESC ");

    return fetchRecords(vpSql.toString());
  }
  
  public List<Map> getAisleRequestPurchaseOrderListForEmulation()
	         throws DBException
	  {                                    
	    StringBuilder vpSql = new StringBuilder("SELECT pol.sloadid, pol.sitem ")
	               .append(" FROM PurchaseOrderHeader poh, PurchaseOrderLine pol ")
	               .append(" WHERE ")             
	               .append(" poh.sOrderID = pol.sOrderID AND ")    
	               .append(" ( poh.sStoreStation= '' OR poh.sStoreStation is null) ")             
	               .append(" ORDER BY poh.dExpectedDate DESC ");

	    return fetchRecords(vpSql.toString());
	  }
  
  //US31492 - Getting the purchase order line for the given load id
	public PurchaseOrderLineData getPurchaseOrderLineByLoadId(String loadId) throws DBException {
		PurchaseOrderLine pol = Factory.create(PurchaseOrderLine.class);
		PurchaseOrderLineData poldata = Factory.create(PurchaseOrderLineData.class);
		poldata.setKey(PurchaseOrderLineData.LOADID_NAME, loadId);
		List<Map> list = pol.getAllElements(poldata);
		if (list != null && list.size() > 0) {
			poldata.dataToSKDCData(list.get(0));
		}
		return poldata;
	}
  /**
   * Sets Objects for garbage collection.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpPOLData = null;
  }
}
