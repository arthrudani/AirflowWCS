package com.daifukuamerica.wrxj.dbadapter.data;

/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright (c) 2004-2008 Daifuku America Corporation  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Description:<BR>
 *   Concrete implementation of OrderInterface class for regular order
 *   headers (sales orders and internal orders). This Class will handle Order
 *   Header specific operations.
 *
 * @author       sbw
 * @author       A.D.
 * @version      1.0    05/28/02
 * @version      2.0    10/13/04
 */
public class PurchaseOrderHeader extends BaseDBInterface
{
  protected PurchaseOrderHeaderData mpPOHData;

  public PurchaseOrderHeader()
  {
    super("PurchaseOrderHeader");
    mpPOHData = Factory.create(PurchaseOrderHeaderData.class);
  }

  /**
   * Retrieves one column value from the PurchaseOrderHeader (P.O.) table.
   * 
   * @param isPONum the unique key to use in the search.
   * @param isColumnName the name of the column whose value is returned.
   * @return value of column specified by isColumnName as an <code>Object</code>.
   *         The caller is assumed to know what data type is actually in
   *         <code>Object</code>. <i>A</i> <code>null</code> <i>object is
   *         returned for no matching data</i>
   * @throws DBException when database access errors occur.
   */
  public Object getSingleColumnValue(String isPONum, String isColumnName)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT ").append(isColumnName)
             .append(" FROM PurchaseOrderHeader WHERE ")
             .append("sOrderID = '").append(isPONum).append("'");

    List<Map> vpData = fetchRecords(vpSql.toString());
    return((!vpData.isEmpty()) ? (vpData.get(0)).get(isColumnName.toUpperCase()) : null);
  }

  /**
   * Method to get Order Status.
   * 
   * @param absData <code>AbstractSKDCData</code> object containing key
   *            information to do the lookup. If there is no key info. then we
   *            do a wild-card search.
   * @return int containing order status.
   */
  public int getOrderStatusValue(AbstractSKDCData absData) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT iPurchaseOrderStatus FROM PurchaseOrderHeader")
             .append(DBHelper.buildWhereClause(absData.getKeyArray()));

    return getIntegerColumn(PurchaseOrderHeaderData.PURCHASEORDERSTATUS_NAME, vpSql.toString());
  }

  /**
   * Method to set Order Status.
   */
  public void setOrderStatusValue(String order, int newStatus)
      throws DBException
  {
    mpPOHData.clear();
    mpPOHData.setOrderStatus(newStatus);
    mpPOHData.setKey(PurchaseOrderHeaderData.ORDERID_NAME, order);
    modifyElement(mpPOHData);
  }

  /**
   * Method to get List of order IDs..
   * 
   * @param allOrNone <code>String</code> containing string to start list
   *            with. The values are SKDCConstants.ALL_STRING or
   *            SKDCConstants.NONE_STRING
   * 
   * @return StringBuffer Order IDs.
   */
  public String[] getOrderChoices() throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT sOrderID FROM PurchaseOrderHeader");

    List<Map> newlist = fetchRecords(vpSql.toString());
    return((String[])(newlist.toArray()));
  }

  /**
   * Retrieves a String[] list of all old PurchaseOrders
   *
   * @param iDaysOld <code>int</code> Number of days past.
   * @return reference to an String[] of PurchaseOrder Numbers that
   *          are old and need to be cleaned up.
   * @exception DBException
   */
  public String[] getOldPOStringList(int iDaysOld) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT sOrderID FROM PurchaseOrderHeader")
             .append(" WHERE (iPurchaseOrderStatus = ")
             .append(DBConstants.ERHISTORY)
             .append(" OR iPurchaseOrderStatus = ")
             .append(DBConstants.ERFORCED)
             .append(") AND dLastActivityTime < (CURRENT_TIMESTAMP - INTERVAL \'")
             .append(iDaysOld)
             .append("\' DAY)");

    return getList(vpSql.toString(), PurchaseOrderHeaderData.ORDERID_NAME,
                   SKDCConstants.NO_PREPENDER);
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
      vpSql.append("SELECT * FROM PurchaseOrderHeader");
      needwhere = true;
    }
    // If requesting a specific POStatus set the key...
    // otherwise just get all types.
    if (iPOStatus != SKDCConstants.ALL_INT)
    {
      if (needwhere == true)
      {
        needwhere = false;
        vpSql.append(" WHERE iPurchaseOrderStatus = " + iPOStatus);
      }
      else
      {
        vpSql.append(" AND iPurchaseOrderStatus = " + iPOStatus);
      }
    }
    vpSql.append(" ORDER BY SORDERID");

    return fetchRecords(vpSql.toString());
  }

  /**
   * Retrieves a list of PurchaseOrder records using either a unique key or by
   * getting the whole list....This is a separate module to provide capability
   * to do a join on the search.
   * 
   * @param sPONum <code>String</code> object.
   * @param iPOType <code>int</code> object.
   * @param sItem <code>String</code> object.
   * @return reference to an List of PurchaseOrderHeader objects containing null
   *         reference if no PurchaseOrders found.
   * @exception DBException
   */
   public List<Map> getPOSearchList(String sPONum, int iPOStatus, String sItem,
      String sLot) throws DBException
   {
               // If the Item is not blank we will have to do
               // a Table Join so just do that.
     if ((sItem.trim().length() != 0) || sLot.trim().length() != 0)
     {
       TableJoin tj = Factory.create(TableJoin.class);
       return(tj.getPOSearchList(sPONum, iPOStatus, sItem, sLot));
     }

     return(getPOList(sPONum, iPOStatus));
   }
    
  /**
   * Retrieves a list of PurchaseOrder records using either the keys passed in
   * the po header data and the PurchaseOrderLine or by getting the whole
   * list....This is a separate module to provide capability to do a join on the
   * search.
   * 
   * @param pohdata
   * @param eldata
   * @return
   * @throws DBException
   */
  public List<Map> getPOSearchList(PurchaseOrderHeaderData pohdata,
      PurchaseOrderLineData eldata) throws DBException
  {
    TableJoin tj = Factory.create(TableJoin.class);
    return (tj.getPOSearchList(pohdata, eldata));
  }

  /**
   * Method to check for Order existence.
   *
   * @param orderID <code>String</code> containing order ID.
   * @return boolean of true if it exists, false otherwise.
   */
  public boolean exists(String orderID)
  {
    mpPOHData.clear();
    mpPOHData.setKey(PurchaseOrderHeaderData.ORDERID_NAME, orderID);
    return(exists(mpPOHData));
  }

  /**
   * Method gets the expected HostLineCount for a given order in the
   * OrderHeader.
   * 
   * @param sOrderID the Purchase order id. being searched for.
   * @return an integer representing
   * @throws DBException
   */
  public int getHostLineCount(String sOrderID) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT iHostLineCount FROM PurchaseOrderHeader WHERE ")
             .append("sOrderID = '").append(sOrderID).append("'");
    
    return getIntegerColumn(PurchaseOrderHeaderData.HOSTLINECOUNT_NAME, vpSql.toString());
  }
  
  /**
   * Retrieves a String[] list of all PurchaseOrderHeaders with 
   * an incorrect ihostlinecnt
   *
   * @return reference to an String[] of PurchaseOrder Numbers    
   *         
   * @exception DBException
   */
  public List<Map> getPOsWithInvalidLinecount() throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT sOrderID, iHostLineCount FROM PurchaseOrderHeader")
         .append(" WHERE PurchaseOrderHeader.iHostLineCount != ")
         .append("(SELECT COUNT(1) FROM PURCHASEORDERLINE ")
         .append(" WHERE PURCHASEORDERLINE.SOrderID = PurchaseOrderHeader.sOrderID)");

    return fetchRecords(vpSql.toString());
  }

  /**
   * Retrieves a String[] list of all Loads with 
   * that are in a moving, storing, xxxing, etc...status for to long
   *
   * @return reference to an String[] of PurchaseOrder Numbers    
   *         
   * @exception DBException
   */
  public List<Map> getPOsExistingToLong(Date moveDate) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT sOrderID, dExpectedDate ")
             .append("From PurchaseOrderHeader ")
             .append("Where (iPurchaseOrderStatus = ")
             .append(DBConstants.ERBUILDING)
             .append(" or iPurchaseOrderStatus = ")
             .append(DBConstants.EREXPECTED)
             .append(" ) and dExpectedDate < ")
             .append(DBHelper.convertDateToDBString(moveDate))
             .append(" order by iPurchaseOrderStatus, dExpectedDate ");

    return fetchRecords(vpSql.toString());
  }

  /**
   * Sets Objects for garbage collection.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpPOHData = null;
  }
}
