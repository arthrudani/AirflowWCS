package com.daifukuoc.wrxj.custom.ebs.dbadapter.data;

import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeader;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.util.SKDCConstants;

public class EBSPurchaseOrderHeader extends PurchaseOrderHeader
{

	public EBSPurchaseOrderHeader()
	{
	    super();
	}

	
	/**
	   * Retrieves a list of PurchaseOrder records using either a unique key
	   * or by getting the whole list.
	   *
	   * @param sPONum <code>String</code> object.
	   * @return reference to an List of PurchaseOrderHeader objects containing
	   *          null reference if no PurchaseOrders found.
	   * @exception DBException
	   */
	  public List<Map> getPOList(String sPONum, int iPOStatus) throws DBException
	  {
	    boolean needwhere = false;

	    StringBuilder vpSql = new StringBuilder();
	    if (sPONum.trim().length() != 0)
	    {
	      vpSql.append("SELECT * FROM PurchaseOrderHeader WHERE"
	          + " sOrderid like '" + sPONum + "%' ");
	    }
	    else
	    {
	      vpSql.append("SELECT poh.*, pol.sItem, pol.sLot, pol.sLoadid FROM PurchaseOrderHeader poh, PurchaseOrderLine pol WHERE poh.sOrderID = pol.sOrderID ");
	    }
	    // If requesting a specific POStatus set the key...
	    // otherwise just get all types.
	    if (iPOStatus != SKDCConstants.ALL_INT)
	    {
	      if (needwhere == true)
	      {
	        needwhere = false;
	        vpSql.append(" WHERE poh.iPurchaseOrderStatus = " + iPOStatus);
	      }
	      else
	      {
	        vpSql.append(" AND poh.iPurchaseOrderStatus = " + iPOStatus);
	      }
	    }
	    vpSql.append(" ORDER BY SORDERID");

	    return fetchRecords(vpSql.toString());
	  }
	  
	  /**
	   * Retrieves a String[] list of all old PurchaseOrders
	   *
	   * @param iDaysOld <code>int</code> Number of hours past.
	   * @return reference to an String[] of PurchaseOrder Numbers that
	   *          are old and need to be cleaned up.
	   * @exception DBException
	   */
	  public String[] getOldPOStringListByHours(int iHoursOld) throws DBException
	  {
	    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT sOrderID FROM PurchaseOrderHeader")
	             .append(" WHERE dExpectedDate < dateadd( hour, -" + iHoursOld + ", CURRENT_TIMESTAMP )");

	    return getList(vpSql.toString(), PurchaseOrderHeaderData.ORDERID_NAME,
	                   SKDCConstants.NO_PREPENDER);
	  }
	  
	  /**
	   * Retrieves a String[] list of all old PurchaseOrders matching trayid
	   *
	   * @return reference to an String[] of PurchaseOrder Numbers that
	   *          are old and need to be cleaned up.
	   * @exception DBException
	   */
	  public String[] getOlderEmptyTrayPOForTray(String sRequestid, String sLoadid) throws DBException
	  {
//	    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT pol.sOrderID FROM PurchaseOrderLine pol, PurchaseOrderHeader poh ")
//	             .append(" WHERE pol.sOrderID = poh.sOrderID AND pol.sOrderID != '" + sRequestid + "' AND pol.sLoadID = '" + sLoadid + "' AND ")
//	             .append(" (poh.sStoreStation IS NOT NULL AND poh.sStoreStation != '')" );
	    
	    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT pol.sOrderID FROM PurchaseOrderLine pol, PurchaseOrderHeader poh ")
	             .append(" WHERE pol.sOrderID = poh.sOrderID AND pol.sOrderID != '" + sRequestid + "' AND pol.sLoadID = '" + sLoadid + "' ");

	    return getList(vpSql.toString(), PurchaseOrderHeaderData.ORDERID_NAME,
	                   SKDCConstants.NO_PREPENDER);
	  }
	  
	  /**
	   * Retrieves a String[] list of all old PurchaseOrders matching trayid
	   *
	   * @return reference to an String[] of PurchaseOrder Numbers that
	   *          are old and need to be cleaned up.
	   * @exception DBException
	   */
	  public String[] getOlderPOForTrayItem(String sRequestid, String sLoadid, String sItem) throws DBException
	  {
//	    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT pol.sOrderID FROM PurchaseOrderLine pol, PurchaseOrderHeader poh ")
//	             .append(" WHERE pol.sOrderID = poh.sOrderID AND pol.sOrderID != '" + sRequestid + "' AND pol.sLoadID = '" + sLoadid + "' AND ")
//	             .append(" (poh.sStoreStation IS NOT NULL AND poh.sStoreStation != '')" );
	    
	    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT pol.sOrderID FROM PurchaseOrderLine pol, PurchaseOrderHeader poh ")
	             .append(" WHERE pol.sOrderID = poh.sOrderID AND pol.sOrderID != '" + sRequestid + "' AND pol.sLoadID = '" + sLoadid + "' AND pol.sItem != '" + sItem + "' ");

	    return getList(vpSql.toString(), PurchaseOrderHeaderData.ORDERID_NAME,
	                   SKDCConstants.NO_PREPENDER);
	  }
	  /**
	   * Find the purchase order for this Load
	   * @param sLoadid
	   * @param sItem
	   * @param sGlobalId
	   * @return
	   * @throws DBException
	   */
	  public String[] getPOForTrayItem(String sLoadid, String sGlobalId ,String sItem ) throws DBException
	  {
	    
	    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT pol.sOrderID FROM PurchaseOrderLine pol, PurchaseOrderHeader poh ")
	             .append(" WHERE pol.sOrderID = poh.sOrderID AND pol.sGlobalID = '" + sGlobalId + "' AND pol.sLoadID = '" + sLoadid + "' AND pol.sLineID = '" + sItem + "' ");

	    return getList(vpSql.toString(), PurchaseOrderHeaderData.ORDERID_NAME,
	                   SKDCConstants.NO_PREPENDER);
	  }
}
