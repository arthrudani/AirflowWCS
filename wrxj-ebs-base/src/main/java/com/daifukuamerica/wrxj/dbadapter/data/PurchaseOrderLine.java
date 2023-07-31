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

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBResultSet;
import java.util.List;
import java.util.Map;

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
public class PurchaseOrderLine extends BaseDBInterface
{
  private DBResultSet mpDBResultSet;
  private PurchaseOrderLineData mpPOLData;

  public PurchaseOrderLine()
  {
    super("PurchaseOrderLine");
    mpPOLData = Factory.create(PurchaseOrderLineData.class);
  }

  /**
   * Adds one PurchaseOrder Line record using unique key (sPONum, sItem, sLot).
   *
   * @param PurchaseOrderLine <code>PurchaseOrder</code> object.
   * @return TRUE if Successful or FALSE if not
   * @exception DBException
   */
  public boolean addPurchaseOrderLine(PurchaseOrderLineData ipELData) throws DBException
  {
    if(ipELData.getLineID().trim().length() < 1)
    {
      ipELData.setLineID(getNextLineID(ipELData.getOrderID()));
    }

    boolean rtn = false;
    try
    {
      addElement(ipELData);
      rtn = true;
    }
    catch(DBException e)
    {
      throw e;
    }

    return(rtn);
  }

  public String getNextLineID(String orderid) throws DBException
  {
    int newint;
    String maxLineID;
    String nextLineID = "99";   // If we ever have an error, set it to 99
       
    try
    {
                // Find the max line id and then add to it.
      mpDBResultSet = execute(
          "SELECT MAX(sLineID) AS \"MAXLINE\" FROM purchaseorderline WHERE sOrderID = ?",
          orderid);
      if (mpDBResultSet.getRowCount() > 0)
      {
        Map row;
        while (mpDBResultSet.hasNext())  // should be just one
        {
          row = (Map) mpDBResultSet.next();
          maxLineID = DBHelper.getStringField(row,"MAXLINE");
          if(maxLineID.trim().length() < 1)
          {
            newint = 1;
          }
          else
          {
            newint = Integer.valueOf(maxLineID).intValue();
            newint= newint + 1;
          }
          nextLineID = "" + newint;
          break;
        }
      }
    }
    catch(DBException e)
    {
      e.printStackTrace(System.out);
      System.out.println("Error " + e + " getting next LoadLineItem Line ID");
    }
    return(nextLineID);
  }
  
 /**
  * Method to get List of Items on this order 
  *  @param isOrderID <code>String</code> containing order number.
  *  @param isAllOrNone <code>String</code> containing string to start list
  *         with.  The values are SKDCConstants.ALL_STRING or
  *         SKDCConstants.NONE_STRING
  *
  * @return StringBuffer Order IDs.
  */
  public String[] getOrderLineChoices(String isOrderID, String isAllOrNone)
         throws DBException
  {                                    // Find out order type first.
                                       //get listing of items on the order.
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT sItem AS \"sItem\" ")
               .append("FROM PurchaseOrderLine WHERE sOrderID = '")
               .append(isOrderID).append("'");
    return getList(vpSql.toString(), PurchaseOrderLineData.ITEM_NAME,
                   isAllOrNone);
  }

 /**
  * Method to get List of all orders that have a particular item.
  *  @param isItem <code>String</code> containing search item number.
  *  @param isAllOrNone <code>String</code> containing string to start list
  *         with.  The values are SKDCConstants.ALL_STRING or
  *         SKDCConstants.NONE_STRING
  *
  * @return StringBuffer Order IDs.
  */
  public String[] getOrderChoicesByItem(String isItem, String isAllOrNone)
         throws DBException
  {
    mpPOLData.clear();
    mpPOLData.setKey(PurchaseOrderLineData.ITEM_NAME, isItem);
    mpPOLData.addOrderByColumn(PurchaseOrderLineData.ORDERID_NAME);
    
    return getSingleColumnValues(PurchaseOrderLineData.ORDERID_NAME, true,
        mpPOLData, isAllOrNone);
  }

  /**
   * Method gets a list of distinct item names on this order. This is different
   * than getOrderLineChoices above in that there is no SKDCConstants.ALL_STRING
   * or SKDCConstants.NONE_STRING inserted at the start of the list.
   * 
   * @param isOrderID <code>String</code> containing order id. of the order
   *            being checked.
   * 
   * @return <code>String[]</code> array of item ids.
   */
  public String[] getDistinctItemList(String isOrderID) throws DBException
  {
    mpPOLData.clear();
    mpPOLData.setKey(PurchaseOrderLineData.ORDERID_NAME, isOrderID);
    mpPOLData.addOrderByColumn(PurchaseOrderLineData.ITEM_NAME);
    
    return getSingleColumnValues(PurchaseOrderLineData.ITEM_NAME, true,
        mpPOLData, "");
  }

  /**
   * Method to set inspection value.
   * 
   * @param isOrderID
   * @param isItem
   * @param isLot
   * @param inInspection
   * @throws DBException
   */
  public void setInspectionValue(String isOrderID, String isItem, String isLot,
      int inInspection) throws DBException
  {
    mpPOLData.clear();
    mpPOLData.setKey(PurchaseOrderLineData.ORDERID_NAME, isOrderID);
    mpPOLData.setKey(PurchaseOrderLineData.ITEM_NAME, isItem);
    mpPOLData.setKey(PurchaseOrderLineData.LOT_NAME, isLot);
    mpPOLData.setInspection(inInspection);
    modifyElement(mpPOLData);
  }

  /**
   *  Method to see if any lines on this Purchase order still have outstanding receipts
   *  @param orderID <code>String</code> containing search order.
   *
   * @return StringBuffer Order IDs.
   */
  public boolean hasRemainingReceipts(String orderID) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT COUNT(sOrderID) AS \"rowCount\" FROM PurchaseOrderLine ")
            .append("WHERE sOrderID = '").append(orderID).append("' AND ")
            .append("fExpectedQuantity > fReceivedQuantity");
           
    return getRecordCount(vpSql.toString(), "rowCount") > 0;
  }

 /**
  * Gets a Line count for a given P.O.
  * @param isPurchaseOrder the purchase Order ID.
  * @return the line count
  * @throws DBException if there is a database access error.
  */
  public int getLineCount(String isPurchaseOrder) throws DBException
  {
    mpPOLData.clear();
    mpPOLData.setKey(PurchaseOrderLineData.ORDERID_NAME, isPurchaseOrder);
    return(getCount(mpPOLData));
  }
  
  /**
   *  Method to see if any lines on this Purchase order have invalid items
   *
   * @return StringBuffer Order IDs.
   */
   public List<Map> getPOLinesWithInvalidItemMaster( ) throws DBException
   {
     StringBuilder vpSql = new StringBuilder("SELECT SorderID, sItem FROM PurchaseOrderLine ")
             .append("WHERE sItem NOT IN (Select sItem FROM ITEMMASTER)");
                 
     return fetchRecords(vpSql.toString());
   }
  
  /**
   * Gets a list of Purchase Order Lines for a given Order including pseudo-columns
   * showing amount that can still be received.
   * @param isPurchaseOrder the order id of the P.O.
   * @return List of rows matching criteria.
   * @throws DBException if there is a database access error.
   */
  public List<Map> getReceivablePOLines(String isPurchaseOrder) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT '").append(PurchaseOrderLineData.SCREEN_DATA_MNEMONIC).append("' AS \"")
             .append(PurchaseOrderLineData.DATATYPE_NAME).append("\", sOrderID, ")
             .append("sItem, ").append("sLot, ")
             .append("null AS \"").append(LoadLineItemData.POSITIONID_NAME).append("\", ")
             .append("fExpectedQuantity, fReceivedQuantity, ")
             .append("fExpectedQuantity - fReceivedQuantity AS \"")
             .append(PurchaseOrderLineData.ACCEPTQUANTITY_NAME).append("\" ")
             .append("FROM PurchaseOrderLine WHERE ")
             .append("sOrderID = '").append(isPurchaseOrder).append("' AND ")
             .append("fExpectedQuantity - fReceivedQuantity > 0");
                
    return fetchRecords(vpSql.toString());
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
