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
import com.daifukuamerica.wrxj.dbadapter.ModelInterface;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBResultSet;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.util.UnusedMethod;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A lower level data base object that interfaces to the Item Master table.
 *
 * @author avt
 * @version 1.0
 */
public class ItemMaster extends BaseDBInterface implements ModelInterface
{
  protected ItemMasterData mpIMData;
  protected static Map<String, String> itemMasterChecks = new HashMap<String, String>();
  protected DBResultSet myDBResultSet;

  // Public Methods for ItemMaster Object

  public ItemMaster()
  {
    super("ItemMaster");
    mpIMData = Factory.create(ItemMasterData.class);
    
    // add the checks need for item master deletion
    // key is the table, entry is the error message
    itemMasterChecks.put("LoadLineItem", "Load Line Items exist.");
    itemMasterChecks.put("OrderLine", "Order Lines exist.");
    itemMasterChecks.put("PurchaseOrderLine", "Purchase Order Lines exist.");
//    itemMasterChecks.put("CycleCount", "Cycle Count exists.");
    itemMasterChecks.put("Station", "Station with this item exists.");
    itemMasterChecks.put("DedicatedLocation", "Dedicated Location exists.");
    itemMasterChecks.put("Move", "Moves exist.");
  }
  
  /**
   * Retrieves one column value from the Item Master table.
   *
   * @param isItem the unique key to use in the search.
   * @param isColumnName the name of the column whose value is returned.
   * @return value of column specified by isColumnName as an <code>Object</code>.
   *         The caller is assumed to know what data type is actually in
   *         <code>Object</code>. <i>A</i> <code>null</code> <i>object is
   *         returned for no matching data</i>
   * @throws DBException when database access errors occur.
   */
  public Object getSingleColumnValue(String isItem, String isColumnName)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT ").append(isColumnName).append(" FROM ItemMaster WHERE ")
             .append("sItem = '").append(isItem).append("'");
    List<Map> vpData = fetchRecords(vpSql.toString());

    return((!vpData.isEmpty()) ? (vpData.get(0)).get(isColumnName.toUpperCase()) : null);
  }
  
  /**
   *  Method to delete an item master.
   *
   *  @param isItem Item name.
   *  @exception DBException
   */
  public void deleteItemMaster(String isItem) throws DBException
  {
    // check for any dependencies. If they exist, we reject request
    for (String vsKey : itemMasterChecks.keySet())
    {
      int vnCount = -1;

      String vsCountSQL = "SELECT COUNT (sItem) AS \"rowCount\" FROM " + vsKey
          + " WHERE sItem = ?";
      myDBResultSet = execute(vsCountSQL, isItem);
      while (myDBResultSet.hasNext())  // should be just one
      {
        Map mpResult = (Map) myDBResultSet.next();
        vnCount = DBHelper.getIntegerField(mpResult, "rowCount");
      }
      if (vnCount > 0)
      {
        DBHelper.dbThrow("Cannot delete ItemMaster: " + isItem + ", "
            + itemMasterChecks.get(vsKey));
      }
    }

    mpIMData.clear();
    mpIMData.setKey(ItemMasterData.ITEM_NAME, isItem);
    deleteElement(mpIMData);
  }

  /**
  *  Method to add a item master.
  *
  *  @param imd Filled in item master data object.
  *  @exception DBException
  */
  public void addItemMaster(ItemMasterData imd) throws DBException
  {
    addElement(imd);
    return;
  }

  /**
  *  Method to add a Defaulted values Item Master for Expected Receipts where
  *  the item master does not exist yet.
  *
  *  @exception DBException
  */
  public void addDefaultItemMaster(String newItem) throws DBException
  {
    mpIMData.clear();
    mpIMData.setItem(newItem);
    mpIMData.setDescription("Defaulted Item Added");
    addItemMaster(mpIMData);
  }

 /**
  *  Method to get a list of matching item names.
  *
  *  @param srchItem Item name to match.
  *  @return List of item names.
  *  @exception DBException
  */
  public List<String> getItemMasterNameList(String srchItem) throws DBException
  {
    List<String> ItemList = new ArrayList<String>();

    myDBResultSet = execute(
        "SELECT sItem FROM itemmaster WHERE sItem like ? order by sitem",
        srchItem + "%");
    Map row;
    while (myDBResultSet.hasNext())  // may be multiple rows
    {
      row = (Map) myDBResultSet.next();
      String nameStr = DBHelper.getStringField(row, ItemMasterData.ITEM_NAME);
      ItemList.add(nameStr);
    }
    return(ItemList);
  }

  /**
   * Method to get a item master data for specified item.
   * 
   * @param itemName Item number.
   * @return ItemMasterData object containing Item info. matching our search
   *         criteria.
   * @exception DBException
   */
  public ItemMasterData getItemMasterData(String itemName) throws DBException
  {
    if (mpIMData == null) mpIMData = Factory.create(ItemMasterData.class);
    else                mpIMData.clear();

    mpIMData.setKey(ItemMasterData.ITEM_NAME, itemName);
    ItemMasterData itemMasterData = getElement(mpIMData, DBConstants.NOWRITELOCK);

    return(itemMasterData);
  }

  /**
   * Method to get Item Storage Flag.
   * 
   * @param absData <code>AbstractSKDCData</code> object. This object should
   *          have Key information in it already.
   * @return int containing storage flag.
   */
  @UnusedMethod
  public int getItemStorageFlagValue(AbstractSKDCData absData) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT iStorageFlag FROM ItemMaster")
             .append(DBHelper.buildWhereClause(absData.getKeyArray()));

    return(getIntegerColumn(ItemMasterData.STORAGEFLAG_NAME, vpSql.toString()));
  }

  /**
   * Method to get description for an item.
   * 
   * @param itemName Item name.
   * @return string containing item's description.
   * @exception DBException
   */
  public String getItemMasterDescription(String itemName) throws DBException
  {
    String description = null;

    myDBResultSet = execute(
        "SELECT sDescription FROM itemmaster WHERE sItem = ?", itemName);
    switch (myDBResultSet.getRowCount()){
      case 0:  // not found
        return (null);
      case 1:
        Map row;
        while (myDBResultSet.hasNext())  // should be just one row
        {
          row = (Map) myDBResultSet.next();
          description = DBHelper.getStringField(row,"sDescription");
        }
        break;
      default:  // Multiple matches
        DBHelper.dbThrow("Multiple matches on key: " + itemName);
        return (null);
    }  //switch
    return(description);
  }

  /**
   * Method to see if the specified item master exists.
   * 
   * @param itemName Item name.
   * @return boolean of <code>true</code> if it exists.
   */
  public boolean exists(String itemName)
  {
    boolean rtn;
    try
    {
      mpIMData.clear();
      mpIMData.setKey(ItemMasterData.ITEM_NAME, itemName);
      rtn = (getCount(mpIMData) > 0);
    }
    catch(DBException e)
    {
      e.printStackTrace(System.out);
      System.out.println("Error " + e + " Checking Item Existance");
      rtn = false;
    }

    return(rtn);
  }

 /**
  * Checks if an item has expiration date checking.
  * @param sItem The item being checked.
  * @return <code>true</code> if the item is expiration checked.
  */
  public boolean isExpirationRequired(String sItem) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT iExpirationRequired FROM ").append(getWriteTableName())
             .append(" WHERE ").append("sItem = '").append(sItem).append("'");
    
    int iExpirationRequired = getIntegerColumn(ItemMasterData.EXPIRATIONREQUIRED_NAME, vpSql.toString());
    
    return(iExpirationRequired == DBConstants.YES);
  }

 /**
  *  Method to update an item master.
  *
  *  @param imd Filled in item master data object.
  *  @exception DBException
  */
  public void updateItemInfo(ItemMasterData imd) throws DBException
  {
    // Must have item number as key at least.
    if (imd.getKeyObject(ItemMasterData.ITEM_NAME) == null)
    {
      imd.setKey(ItemMasterData.ITEM_NAME, imd.getItem());
    }
    modifyElement(imd);
  }


  /**
   *  Method to get a list of recommended zones.
   *
   *  @param srch Zone name to match.
   *  @return List of zone names.
   */
  @UnusedMethod // Only called from an UnusedMethod
  public List<String> getItemRecZoneList(String srch) throws DBException
  {
    List<String> RecommendedList = new ArrayList<String>();
    try
    {
      myDBResultSet = execute(
          "SELECT sZone FROM zone WHERE sZone LIKE ? ORDER BY sZone", srch + "%");
      Map row;
      while (myDBResultSet.hasNext())  // may be multiple rows
      {
        row = (Map) myDBResultSet.next();
        String nameStr = DBHelper.getStringField(row,"sZone");
        if (nameStr.trim().length() > 0)
        {
          RecommendedList.add(nameStr);
        }
      }
    }
    catch(DBException e)
    {
      throw new DBException("Error " + e + " Getting Item Recommended Zone List");
    }
    return(RecommendedList);
  }

/**
 *  Method to get a list of Item Store routes.
 *
 *  @param srch Route name to match.
 *  @return List of route names.
 */
  public List<String> getItemRouteIDList(String srch)
  {
    List<String> RecommendedList = new ArrayList<String>();
    try
    {
      myDBResultSet = execute(
          "SELECT sRouteID FROM route WHERE sRouteID LIKE ? ORDER BY sRouteID",
          srch + "%");
      Map row;
      while (myDBResultSet.hasNext())  // may be multiple rows
      {
        row = (Map) myDBResultSet.next();
        String nameStr = DBHelper.getStringField(row,"sRouteID");
        if (nameStr.trim().length() > 0)
        {
          RecommendedList.add(nameStr);
        }
      }
    }
    catch(DBException e)
    {
      e.printStackTrace(System.out);
      System.out.println("Error " + e + " Getting Item Store Route ID List");
      return(null);
    }
    return(RecommendedList);
  }

  /**
   * Method to get Item Master count.
   *
   * @param srch Item name to match.
   * @return int containing count of matching Item Masters.
   */
  @UnusedMethod // Only called from an UnusedMethod
  public int getItemMasterCountByName(String srch) throws DBException
  {
    mpIMData.clear();
    mpIMData.setKey(ItemMasterData.ITEM_NAME, srch, KeyObject.LIKE);
    return(getCount(mpIMData));
  }

  /**
   *  Method to get item master hold for an item.
   *
   *  @param itemName Item name.
   *  @return integer containing item's hold value.
   *  @exception DBException
   */
  public int getItemMasterHold(String itemName) throws DBException
  {
    int vnHoldType = 0;

    myDBResultSet = execute("SELECT iholdtype FROM itemmaster WHERE sItem = ?",
        itemName);
    switch (myDBResultSet.getRowCount()){
      case 0:  // not found
        return (0);
      case 1:
        Map row;
        while (myDBResultSet.hasNext())  // should be just one row
        {
          row = (Map) myDBResultSet.next();
          vnHoldType = DBHelper.getIntegerField(row,"iholdtype");
        }
        break;
      default:  // Multiple matches
        DBHelper.dbThrow("Multiple matches on key: " + itemName);
      return (0);
    }  //switch
    return(vnHoldType);
  }

  /**
   * Get item quantities
   * @param isItemID
   * @return double[] { TotalQuantity, AllocatedQuantity, ExpectedQuantity }
   * @throws DBException
   */
  public double[] getItemQuantities(String isItemID) throws DBException
  {
    double[] vadQty = new double[3];
    /*
     * Total & Allocated Quantities
     */
    StringBuilder vpSql = new StringBuilder("SELECT sum(").append(LoadLineItemData.CURRENTQUANTITY_NAME)
             .append(") AS \"FTOTALQTY\", sum(").append(LoadLineItemData.ALLOCATEDQUANTITY_NAME)
             .append(") AS \"FALLOCQTY\" FROM ")
             .append(" LOADLINEITEM ")  // TODO: Find a way to not hard-code this
             .append(" WHERE ")
             .append(LoadLineItemData.ITEM_NAME)
             .append(" = ?");
    List<Map> vpList = fetchRecords(vpSql.toString(), isItemID);
    
    vadQty[0] = Double.parseDouble(vpList.get(0).get("FTOTALQTY").toString());
    vadQty[1] = Double.parseDouble(vpList.get(0).get("FALLOCQTY").toString());
    
    /*
     * Expected Quantity
     */
    vpSql.setLength(0);
    vpSql.append("SELECT sum(").append(PurchaseOrderLineData.EXPECTEDQUANTITY_NAME)
             .append(" - ").append(PurchaseOrderLineData.RECEIVEDQUANTITY_NAME)
             .append(") AS \"FEXPECTQTY\" FROM ")
             .append(" PURCHASEORDERLINE ")
             .append(" WHERE ")
             .append(PurchaseOrderLineData.ITEM_NAME)
             .append(" = ?");
    vpList = fetchRecords(vpSql.toString(), isItemID);
    
    vadQty[2] = Double.parseDouble(vpList.get(0).get("FEXPECTQTY").toString());

    return vadQty;
  }

  /**
   * Get a list of Lot/Hold/Total/Allocated/Expected
   * @param isItemID
   * @return List<Map>
   * @throws DBException
   */
  public List<Map> getItemQuantitiesByLot(String isItemID) throws DBException
  {
    /*
     * First, get the list of existing lot/hold from LoadLineItem
     */
    StringBuilder vpSql = new StringBuilder("SELECT ").append(LoadLineItemData.LOT_NAME)
             .append(", ").append(LoadLineItemData.HOLDTYPE_NAME)
             .append(", sum(").append(LoadLineItemData.CURRENTQUANTITY_NAME)
             .append(") AS \"FTOTALQTY\", sum(").append(LoadLineItemData.ALLOCATEDQUANTITY_NAME)
             .append(") AS \"FALLOCQTY\" FROM ")
             .append(" LOADLINEITEM ")
             .append(" WHERE ").append(LoadLineItemData.ITEM_NAME).append(" = ?")
             .append(" GROUP BY ").append(LoadLineItemData.LOT_NAME)
             .append(", ").append(LoadLineItemData.HOLDTYPE_NAME)
//	TLarson - 2/14/2017 - The following "nulls first" doesn't work in SQL Server so by adding
//                        the "CASE" statement then we get the same results in Oracle and SQL SERVER.
//           .append(" nulls first, ").append(LoadLineItemData.HOLDTYPE_NAME);
             .append(" ORDER BY (CASE WHEN ").append(LoadLineItemData.LOT_NAME)
             .append(" IS NULL THEN 0 ELSE 1 END), ").append(LoadLineItemData.LOT_NAME)
             .append(", ").append(LoadLineItemData.HOLDTYPE_NAME);
    List<Map> vpLLIList = fetchRecords(vpSql.toString(), isItemID);
    
    /*
     * Second, get the list of expected lot/hold from PurchaseOrderLine
     */
    vpSql.setLength(0);
    vpSql.append("SELECT ").append(PurchaseOrderLineData.LOT_NAME)
             .append(", CASE ").append(PurchaseOrderLineData.INSPECTION_NAME)
             .append(" WHEN ").append(DBConstants.NO).append(" THEN ").append(DBConstants.ITMAVAIL) 
             .append(" WHEN ").append(DBConstants.YES).append(" THEN ").append(DBConstants.ITMHOLD) 
             .append(" END AS \"") 
             .append(LoadLineItemData.HOLDTYPE_NAME) 
             .append("\"") 
             .append(", sum(").append(PurchaseOrderLineData.EXPECTEDQUANTITY_NAME)
             .append(" - ").append(PurchaseOrderLineData.RECEIVEDQUANTITY_NAME)
             .append(") AS \"FEXPECTQTY\" FROM ")
             .append(" PURCHASEORDERLINE ")
             .append(" WHERE ").append(PurchaseOrderLineData.ITEM_NAME).append(" = ?")
             .append(" GROUP BY ").append(PurchaseOrderLineData.LOT_NAME)
             .append(", ").append(PurchaseOrderLineData.INSPECTION_NAME)
//         	TLarson - 2/14/2017 - The following "nulls first" doesn't work in SQL Server so by adding
//           the "CASE" statement then we get the same results in Oracle and SQL SERVER.
//           .append(" nulls first, 2");
             .append(" ORDER BY (CASE WHEN ").append(PurchaseOrderLineData.LOT_NAME)
             .append(" IS NULL THEN 0 ELSE 1 END), ").append(PurchaseOrderLineData.LOT_NAME)
             .append(", 2");
    List<Map> vpPOLList = fetchRecords(vpSql.toString(), isItemID);

    /*
     * Merge the two lists
     */
    List<Map> vpList = new ArrayList<Map>();
    int vnLLI = 0;
    int vnPOL = 0;
    while (vnLLI < vpLLIList.size() || vnPOL < vpPOLList.size())
    {
      Map vpLM = null; 
      Map vpPM = null;
      
      if (vnLLI < vpLLIList.size())
        vpLM = vpLLIList.get(vnLLI);
      if (vnPOL < vpPOLList.size())
        vpPM = vpPOLList.get(vnPOL);
      
      int x = 0;

      if (vpPM == null) x = -1;
      else if (vpLM == null) x = 1;
      else
      {
        String vsLItem = vpLM.get(LoadLineItemData.LOT_NAME).toString();
        String vsPItem = vpPM.get(PurchaseOrderLineData.LOT_NAME).toString();
        
        int vnLHold = Integer.parseInt(vpLM.get(LoadLineItemData.HOLDTYPE_NAME).toString());
        int vnPHold = Integer.parseInt(vpPM.get(LoadLineItemData.HOLDTYPE_NAME).toString());
        
        x = vsLItem.compareTo(vsPItem);
        if (x == 0)
        {
          x = vnLHold - vnPHold;
        }
      }
      
      /*
       * Merge
       */
      if (x == 0)
      {
        vpLM.put("FEXPECTQTY", vpPM.get("FEXPECTQTY"));
        vpList.add(vpLM);
        vnLLI++;
        vnPOL++;
      }
      /*
       * Use POL
       */
      else if (x > 0)
      {
        vpPM.put("FTOTALQTY", "0.0");
        vpPM.put("FALLOCQTY", "0.0");
        vpList.add(vpPM);
        vnPOL++;
      }
      /*
       * Use LLI
       */
      else
      {
        vpLM.put("FEXPECTQTY", "0.0");
        vpList.add(vpLM);
        vnLLI++;
      }
    }
    
    return vpList;
  }

  /**
   * Update the last CCI date
   * @param isItem
   */
  public void updateLastCCIDate(String isItem) throws DBException
  {
    LoadLineItem vpLLI = Factory.create(LoadLineItem.class);
    LoadLineItemData vpLLIData = Factory.create(LoadLineItemData.class);
    vpLLIData.setKey(LoadLineItemData.ITEM_NAME, isItem);
    if (vpLLI.exists(vpLLIData))
    {
      // There is an item detail to get the date from
      String vsUpdateCCISql = "UPDATE " + getReadTableName() + " SET "
          + ItemMasterData.LASTCCIDATE_NAME + "=(SELECT MIN("
          + LoadLineItemData.LASTCCIDATE_NAME + ") FROM LOADLINEITEM WHERE "
          + LoadLineItemData.ITEM_NAME + "=?) WHERE "
          + ItemMasterData.ITEM_NAME + "=?";
      execute(vsUpdateCCISql, isItem, isItem);
    }
    else
    {
      // There are no item details
      mpIMData.clear();
      mpIMData.setKey(ItemMasterData.ITEM_NAME, isItem);
      mpIMData.setLastCCIDate(new Date());
      modifyElement(mpIMData);
    }
  }
}
