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
import com.daifukuamerica.wrxj.jdbc.DBResultSet;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.util.UnusedMethod;
import java.util.List;
import java.util.Map;

/**
 * A lower level data base object that interfaces to the Load Line Item table.
 *
 * @author avt
 * @version 1.0
 */
public class LoadLineItem extends BaseDBInterface
{
  protected LoadLineItemData mpLLIData;
  protected DBResultSet mpDBResultSet;

  public LoadLineItem()
  {
    super("LoadLineItem");
    mpLLIData = Factory.create(LoadLineItemData.class);
  }

  /**
   *  Method to add a load line item.
   *
   *  @param id Filled in load line item data object.
   *  @exception DBException
   */
  public void addLoadLineItem(LoadLineItemData id) throws DBException
  {
    addElement(id);
  }

  public String getNextLineID(String loadid) throws DBException
  {
    int newint;
    String maxLineID;
    String nextLineID = "99";   // If we ever have an error, set it to 99
       
    try
    {
                // Find the max line id and then add to it.
      mpDBResultSet = execute(
          "SELECT MAX(sLineID) AS \"MAXLINE\" FROM loadlineitem WHERE sLoadID = ?",
          loadid);
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
  *  Method to delete a load line item.
  *
  *  @param item Item name.
  *  @param lot Lot to.
  *  @param loadid Load ID item is on.
  *  @param order Item name.
  *  @param orderLot Lot.
  *  @param lineid Order Line.
  *  @exception DBException
  */
  public void deleteLoadLineItem(String item, String lot, String loadid,
            String order, String orderLot, String lineid, String positionID) 
            throws DBException
  {
    mpLLIData.clear();
    mpLLIData.setKey(LoadLineItemData.ITEM_NAME, item);
    mpLLIData.setKey(LoadLineItemData.LOT_NAME, lot);
    mpLLIData.setKey(LoadLineItemData.ORDERID_NAME, order);
    mpLLIData.setKey(LoadLineItemData.ORDERLOT_NAME, orderLot);
    mpLLIData.setKey(LoadLineItemData.LOADID_NAME, loadid);
    mpLLIData.setKey(LoadLineItemData.LINEID_NAME, lineid);
    mpLLIData.setKey(LoadLineItemData.POSITIONID_NAME, positionID);
    deleteElement(mpLLIData);
  }

  /**
   * Method to see if the specified load line item exists.
   * 
   * @param item Item to check.
   * @param lot Lot to check.
   * @param loadid LoadID to check.
   * @return boolean of <code>true</code> if it exists.
   * @throws DBException
   */
//  public boolean exists2(String item, String lot, String loadid)
//      throws DBException
//  {
//    mpLLIData.clear();
//    mpLLIData.setKey(LoadLineItemData.ITEM_NAME, item);
//    mpLLIData.setKey(LoadLineItemData.LOT_NAME, lot);
//    mpLLIData.setKey(LoadLineItemData.LOADID_NAME, loadid);
//    int theCount = getCount(mpLLIData);
//    return theCount > 0;
//  }
  
  /**
   * Method to see if the specified load line item exists.
   * 
   * @param item Item to check.
   * @param lot Lot to check.
   * @param loadid LoadID to check.
   * @return boolean of <code>true</code> if it exists.
   * @throws DBException
   */
  public boolean exists(String item, String lot, String loadid, String position)
      throws DBException
  {
    mpLLIData.clear();
    mpLLIData.setKey(LoadLineItemData.ITEM_NAME, item);
    mpLLIData.setKey(LoadLineItemData.LOT_NAME, lot);
    mpLLIData.setKey(LoadLineItemData.LOADID_NAME, loadid);
    mpLLIData.setKey(LoadLineItemData.POSITIONID_NAME, position);
    int theCount = getCount(mpLLIData);
    return theCount > 0;
  }
  
  /**
   * Method to see if the specified load line item exists.
   * 
   * @param item Item to check.
   * @param lot Lot to check.
   * @param loadid LoadID to check.
   * @param order Order number.
   * @param orderLot Ordered lot number.
   * @return boolean of <code>true</code> if it exists.
   * @throws DBException
   */
  public boolean exists(String item, String lot, String position, String loadid,
      String order, String orderLot) throws DBException
  {
    mpLLIData.clear();
    mpLLIData.setKey(LoadLineItemData.ITEM_NAME, item);
    mpLLIData.setKey(LoadLineItemData.LOT_NAME,  lot);
    mpLLIData.setKey(LoadLineItemData.ORDERID_NAME, order);
    mpLLIData.setKey(LoadLineItemData.ORDERLOT_NAME, orderLot);
    mpLLIData.setKey(LoadLineItemData.LOADID_NAME, loadid);
    mpLLIData.setKey(LoadLineItemData.POSITIONID_NAME, position);
    int theCount = getCount(mpLLIData);
    return theCount > 0;
  }

  /**
   * Sets Objects for garbage collection.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpLLIData = null;
    mpDBResultSet = null;
  }

  /**
   * Method to update a load line item.
   * 
   * @param id Filled in load line item data object.
   * @exception DBException
   */
  public void updateLoadLineItemInfo(LoadLineItemData id) throws DBException
  {
    mpLLIData.clear();
    mpLLIData.setHoldReason(id.getHoldReason());
    mpLLIData.setOrderID(id.getOrderID());
    mpLLIData.setOrderLot(id.getOrderLot());
    mpLLIData.setHoldType(id.getHoldType());
    mpLLIData.setPriorityAllocation(id.getPriorityAllocation());
    mpLLIData.setLastCCIDate(id.getLastCCIDate());
    mpLLIData.setAgingDate(id.getAgingDate());
    mpLLIData.setExpirationDate(id.getExpirationDate());
    mpLLIData.setCurrentQuantity(id.getCurrentQuantity());
    mpLLIData.setAllocatedQuantity(id.getAllocatedQuantity());
    mpLLIData.setPositionID(id.getPositionID());
    
    mpLLIData.setKey(LoadLineItemData.ITEM_NAME, id.getItem());
    mpLLIData.setKey(LoadLineItemData.LOT_NAME, id.getLot());
    mpLLIData.setKey(LoadLineItemData.ORDERID_NAME, id.getOrderID());
    mpLLIData.setKey(LoadLineItemData.ORDERLOT_NAME, id.getOrderLot());
    mpLLIData.setKey(LoadLineItemData.LOADID_NAME, id.getLoadID());
    mpLLIData.setKey(LoadLineItemData.LINEID_NAME, id.getLineID());
    mpLLIData.setKey(LoadLineItemData.POSITIONID_NAME, id.getPositionID());
    modifyElement(mpLLIData);
  }

  /**
   * Method to get item detail info. tailored to a store screen that will share
   * display of Item Details and Expected lines on the same screen.
   * 
   * @param isLoadID
   * @return
   * @throws DBException
   */
  public List<Map> getStoreScreenDataList(String isLoadID) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT '").append(LoadLineItemData.SCREEN_DATA_MNEMONIC).append("' AS \"")
             .append(LoadLineItemData.DATATYPE_NAME).append("\", sOrderID, ")
             .append("sItem, ").append("sLot, sPositionID, TO_NUMBER('0.00') AS \"")
             .append(PurchaseOrderLineData.EXPECTEDQUANTITY_NAME).append("\", ")
             .append("fCurrentQuantity AS \"")
             .append(PurchaseOrderLineData.RECEIVEDQUANTITY_NAME).append("\", ")
             .append("TO_NUMBER('0.00') AS \"")
             .append(LoadLineItemData.ACCEPTQUANTITY_NAME).append("\" FROM LoadLineItem ")
             .append("WHERE sLoadID = '").append(isLoadID).append("'");
    
    return fetchRecords(vpSql.toString());
  }
  
  /**
   *  Method to get a load line item data for specified item, lot, and load.
   *
   *  @param isItem Item to get.
   *  @param isLot Lot to get.
   *  @param isLoadID LoadID to get.
   *  @param isLineID 
   *  @param isOrderID Order to get.
   *  @param isOrderLot Ordered lot to get.
   *  @return LoadLineItemData object containing load line item info. matching our
   *          search criteria.
   *  @exception DBException
   */
  public LoadLineItemData getLoadLineItemData(String isItem, String isLot,
      String isLoadID, String isOrderID, String isOrderLot, String isLineID,
      String positionID) throws DBException
  {
    return getLoadLineItemData(isItem, isLot, isLoadID, isOrderID, isOrderLot,
        isLineID, positionID, DBConstants.NOWRITELOCK);
  }
   
  /**
   *  Method to get a load line item data for specified item, lot, and load.
   *
   *  @param isItem Item to get.
   *  @param isLot Lot to get.
   *  @param isLoadID LoadID to get.
   *  @param isOrderID Order to get.
   *  @param isOrderLot Ordered lot to get.
   *  @param inLockFlag WRITELOCK, etc
   *  @return LoadLineItemData object containing load line item info. matching our
   *          search criteria.
   *  @exception DBException
   */
  public LoadLineItemData getLoadLineItemData(String isItem, String isLot,
      String isLoadID, String isOrderID, String isOrderLot, String isLineID,
      String isPositionID, int inLockFlag) throws DBException
  {
    mpLLIData.clear();
    mpLLIData.setKey(LoadLineItemData.ITEM_NAME, isItem);
    mpLLIData.setKey(LoadLineItemData.LOT_NAME, isLot);
    mpLLIData.setKey(LoadLineItemData.LOADID_NAME,isLoadID);
    if (isOrderID != null) mpLLIData.setKey(LoadLineItemData.ORDERID_NAME, isOrderID);
    if (isOrderLot != null) mpLLIData.setKey(LoadLineItemData.ORDERLOT_NAME, isOrderLot);
    if (isLineID != null) mpLLIData.setKey(LoadLineItemData.LINEID_NAME, isLineID);
    if (isPositionID != null) mpLLIData.setKey(LoadLineItemData.POSITIONID_NAME, isPositionID);

    return getElement(mpLLIData, inLockFlag);
  }
  
  /**
   *  Method to get multiple load line item data for specified item and lot.
   *
   *  @param item Item to get.
   *  @param lot Lot to get.
   *  @return List containing matching load line item information.
   *  @exception DBException
   */
  public List<Map> getLoadLineItemDataListByItemLot(String item, String lot)
      throws DBException
  {
    LoadLineItemData vpLLIData = Factory.create(LoadLineItemData.class);
    
    vpLLIData.setKey(LoadLineItemData.ITEM_NAME, item);
    vpLLIData.setKey(LoadLineItemData.LOT_NAME, lot);
    vpLLIData.addOrderByColumn(LoadLineItemData.ITEM_NAME);
    vpLLIData.addOrderByColumn(LoadLineItemData.LOT_NAME);
    
    return getAllElements(vpLLIData);
  }

  /**
   *  Method to get multiple load line item data for specified load.
   *
   *  @param loadid Load ID to get.
   *  @return List containing matching load line item information.
   *  @exception DBException
   */
  public List<Map> getLoadLineItemDataListByLoadID(String loadid)
      throws DBException
  {
    LoadLineItemData vpLLIData = Factory.create(LoadLineItemData.class);
    
    vpLLIData.setKey(LoadLineItemData.LOADID_NAME, loadid);
    vpLLIData.addOrderByColumn(LoadLineItemData.ITEM_NAME);
    vpLLIData.addOrderByColumn(LoadLineItemData.LOT_NAME);
    
    return getAllElements(vpLLIData);
  }

  /**
   *  Method to get multiple load line item data for specified criteria.
   *
   *  @param keyData KeyObject[] containing search criteria.
   *  @return List containing matching load line item information.
   *  @exception DBException
   */
  public List<Map> getLoadLineItemDataList(LoadLineItemData ipKey)
      throws DBException
  {
    ipKey.addOrderByColumn(LoadLineItemData.LOADID_NAME);
    ipKey.addOrderByColumn(LoadLineItemData.ITEM_NAME);
    ipKey.addOrderByColumn(LoadLineItemData.LOT_NAME);
    
    return getAllElements(ipKey);
  }

  /**
   * Method to get Load Line Item count.
   *
   * @param srch Item name to match.
   * @return int containing count of matching load line items.
   */
  public int getLoadLineItemCount(String srch) throws DBException
  {
    mpLLIData.clear();
    mpLLIData.setKey(LoadLineItemData.ITEM_NAME, srch, KeyObject.LIKE);
    return getCount(mpLLIData);
  }

  /**
   * Calculates total available quantity on the system for a given item.
   * @param  item <code>String</code> containing item for this calculation.
   * @param  excludeHolds <code>boolean</code> flag to exclude hold item details.
   *         If set to <code>true</code> only ITMAVAIL item details are included
   *         in the calculation.
   *
   * @return <code>double</code> value containing total quantity minus
   *         allocated quantity.
   */
  public double getTotalAvailableQuantity(String item, boolean excludeHolds)
         throws DBException
  {
    double totalQty = 0;
    double allocatedQty = 0;
    
       // First Check to see if there are any of this item not on hold in the first place
       // because if not, the conversion from the sum returns a blank field not a zero
      
    StringBuilder vpSql = new StringBuilder("SELECT sItem FROM LoadLineItem WHERE ")
               .append("sItem = '").append(item).append("' ");
    if (excludeHolds) vpSql.append("AND iHoldType = ").append(DBConstants.ITMAVAIL);

    
    List<Map> aList = fetchRecords(vpSql.toString());

    if (aList.size() > 0)
    {
      vpSql.setLength(0);
      vpSql.append("SELECT SUM(fCurrentQuantity) AS \"fTotalQty\", ")
               .append("SUM(fAllocatedQuantity) AS \"fTotalAllocatedQty\" ")
               .append("FROM LoadLineItem WHERE ")
               .append("sItem = '").append(item).append("' ");
      if (excludeHolds) vpSql.append("AND iHoldType = ").append(DBConstants.ITMAVAIL);
  
      
      aList = fetchRecords(vpSql.toString());
  
      if (aList.size() > 0)
      {
        totalQty     = DBHelper.getDoubleField(aList.get(0), "fTotalQty");
        allocatedQty = DBHelper.getDoubleField(aList.get(0), "fTotalAllocatedQty");
      }      
    }
    return(totalQty - allocatedQty);
  }

 /**
  * Calculates total Allocated quantity on the system for a given item and lot.
  * Only item details that are available are included.
  *
  * @param  item <code>String</code> containing item for this calculation.
  * @param  lot <code>String</code> containing lot for this calculation.
  *
  * @return <code>double</code> value containing total allocated quantity.
  */
  public double getTotalAllocatedQuantity(String item, String lot)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT SUM(fAllocatedQuantity) AS \"fTotalAllocatedQty\" ")
             .append("FROM LoadLineItem WHERE iHoldType = ").append(DBConstants.ITMAVAIL)
             .append(" AND sItem = '").append(item).append("' AND ");
    if (lot == null || lot.trim().length() == 0)
      vpSql.append("sLot IS NULL ");
    else
      vpSql.append("sLot = '").append(lot).append("' ");

    List<Map> aList = fetchRecords(vpSql.toString());
    double allocatedQty = 0;
    if (aList.size() > 0)
    {
      allocatedQty = DBHelper.getDoubleField(aList.get(0), "fTotalAllocatedQty");
    }

    return(allocatedQty);
  }

  /**
   * Calculates total quantity on the system. If the lot number is passed as a
   * blank, then it is ignored altogether in the computation.
   * 
   * @param item <code>String</code> containing item for this calculation.
   * @param lot <code>String</code> containing lot for this calculation.
   * 
   * @return <code>double</code> value containing total quantity minus allocated
   *         quantity.
   */
  @UnusedMethod // Only called from an UnusedMethod
  public double getTotalQuantity(String item, String lot) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT SUM(fCurrentQuantity) AS \"fTotalCurrentQty\" ")
             .append("FROM LoadLineItem WHERE ")
             .append("sItem = '").append(item).append("' ");
    if (lot != null && lot.trim().length() != 0)
    {
      vpSql.append("AND sLot = '").append(lot).append("'");
    }
    List<Map> aList = fetchRecords(vpSql.toString());
    double totalQty = 0;

    if (aList.size() > 0)
    {
      totalQty = DBHelper.getDoubleField(aList.get(0), "fTotalCurrentQty");
    }

    return(totalQty);
  }

  /**
   * Method to get multiple load line item data for specified order.
   * 
   * @param isOrderID Order ID to get.
   * @return List containing matching load line item information.
   * @exception DBException
   */
  @UnusedMethod // Only called from an UnusedMethod
  public List<Map> getLoadLineItemDataListByOrderID(String isOrderID)
      throws DBException
  {
    mpLLIData.clear();
    mpLLIData.setKey(LoadLineItemData.ORDERID_NAME, isOrderID);
    mpLLIData.addOrderByColumn(LoadLineItemData.LOADID_NAME);
    mpLLIData.addOrderByColumn(LoadLineItemData.ITEM_NAME);
    mpLLIData.addOrderByColumn(LoadLineItemData.LOT_NAME);
    
    return getAllElements(mpLLIData);
  }

  /**
   * Method to check if there are multiple part numbers in a load.
   * 
   * @param sLoadID the load being checked.
   * @return true if this is a single part load, false otherwise.
   */
  public boolean isSinglePartLoad(String sLoadID) throws DBException
  {
    mpLLIData.clear();
    mpLLIData.setKey(LoadLineItemData.LOADID_NAME, sLoadID);
    
    String[] vasItems = getSingleColumnValues(LoadLineItemData.ITEM_NAME, true,
        mpLLIData, "");
    
    return vasItems.length == 1;
  }
  
  /**
   * Method to see if other items exists than the specified item in the same
   * load.
   * 
   * @param isItem Item to check.
   * @param isLoadid LoadID to check.
   * @return boolean of <code>true</code> if it exists.
   * @throws DBException
   */
  public boolean existsOtherItems(String isItem, String isLoadid)
      throws DBException
  {
    mpLLIData.clear();
    mpLLIData.setKey(LoadLineItemData.LOADID_NAME, isLoadid);
    mpLLIData.setKey(LoadLineItemData.ITEM_NAME, isItem);

    return getCount(mpLLIData) > 0;
  }

  /**
   * Method to see if other lots exists than the specified lot in the same load.
   * 
   * @param isLot Lot to check.
   * @param isLoadid LoadID to check.
   * @return boolean of <code>true</code> if it exists.
   * @throws DBException
   */
  public boolean existsOtherLots(String isItem, String isLot, String isLoadid)
      throws DBException
  {
    mpLLIData.clear();
    mpLLIData.setKey(LoadLineItemData.LOADID_NAME, isLoadid);
    mpLLIData.setKey(LoadLineItemData.ITEM_NAME, isItem);
    mpLLIData.setKey(LoadLineItemData.LOT_NAME, isLot);

    return getCount(mpLLIData) > 0;
  }

  /**
   *  Get the last storage location for specified item and lot.
   *
   *  @param isItem Item to get.
   *  @param isLot Lot to get.
   *  @return String - Warehouse+Addess
   *  @exception DBException
   */
  @UnusedMethod // Only called from an UnusedMethod
  public String getLastLocation(String isItem, String isLot) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT ld.swarehouse, ld.saddress FROM loadlineitem lli, load ld, location lc ")
             .append("WHERE ld.sLoadid = lli.sLoadid ")
             .append("  AND ld.swarehouse = lc.swarehouse ")
             .append("  AND ld.saddress = lc.saddress ")
             .append("  AND (lc.ilocationtype = " + DBConstants.LCASRS )
             .append(" OR lc.ilocationtype = " + DBConstants.LCCONVSTORAGE + ") ")
             .append("  AND lli.sitem = '"+isItem+"' ");
    if (isLot != null && isLot.length() > 0)
    {
      vpSql.append(" AND lli.slot = '"+isLot+"' ");
    }
    vpSql.append("ORDER BY lli.dagingdate");

    List<Map> aList = fetchRecords(vpSql.toString());
    if (aList.size() == 0)
    {
      return "";
    }
    else
    {
      Map vpMap = aList.get(aList.size()-1);
      String vsWarehouse = DBHelper.getStringField(vpMap,"SWAREHOUSE");
      String vsAddress = DBHelper.getStringField(vpMap, "SADDRESS");

      return(vsWarehouse+vsAddress);
    }
  }
   
  /**
   * Method to obtain all item details that can be ordered to a station.
   * 
   * @param isRoute
   * @param izRouteSet
   * @return List of <code>Map</code> objects representing
   *         <code>LoadLineItemData</code> objects
   * @throws DBException
   */
  public List<Map> getOrderableItemDetails(String isRoute, boolean izRouteSet)
      throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT loadlineitem.* FROM LoadLineItem, Load, ")
             .append("Route, Station, Device, ItemMaster, Location ")
             .append("WHERE  Route.sRouteID = ? ")
             .append("AND Route.iRouteOnOff = ? ")
             .append("AND Route.sDestID = Station.sStationName ")
             .append("AND Load.iLoadMoveStatus = ? ")
             .append("AND Load.sDeviceID = Device.sDeviceID ")
             .append("AND Station.sWarehouse = Location.sWarehouse ")
             .append("AND Station.sStationName = Location.sAddress ")
             .append("AND Device.iAisleGroup = Location.iAisleGroup ")
             .append("AND LoadLineItem.sLoadID = Load.sLoadID ")
             .append("AND ItemMaster.sItem = LoadLineItem.sItem ");
    if (izRouteSet)
    {
      vpSql.append("AND ItemMaster.sOrderRoute = Route.sRouteID");
    }
    return fetchRecords(vpSql.toString(), isRoute, DBConstants.ON,
        DBConstants.NOMOVE);
  }
   
  /**
   * Retrieves a String[] list of all Items with a negative current qty
   * 
   * @return reference to an String[] of Order Numbers
   * 
   * @exception DBException
   */
  public List<Map> getLoadLineItemWithNegCurQty() throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT  sLoadID, sItem, fCurrentQuantity FROM ")
             .append("LoadLineItem WHERE fCurrentQuantity < 0.0");

    return fetchRecords(vpSql.toString());
  }
    
  /**
   * Retrieves a String[] list of all Items with a bad allocated qty
   * 
   * @return reference to an String[] of Order Numbers
   * 
   * @exception DBException
   */
  public List<Map> getLoadLineItemWithBadAlcQty() throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT  sLoadID, sItem, fAllocatedQuantity ")
             .append("FROM LoadLineItem WHERE fAllocatedQuantity < 0.0 or")
             .append(" fAllocatedQuantity > fCurrentQuantity");

    return fetchRecords(vpSql.toString());
  }
     
  /**
   * Retrieves a String[] list of all Items with an alcqty and no move
   * 
   * @return reference to an String[] of Order Numbers
   * 
   * @exception DBException
   */
  public List<Map> getLoadLineItemAlcQtyWithOutMove() throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT  sLoadID, sItem, sLot, fAllocatedQuantity ")
             .append("FROM LoadLineItem")
             .append(" WHERE fAllocatedQuantity > 0.0 and")
             .append(" sLoadID NOT IN")
             .append(" (SELECT sLoadID FROM Move WHERE")
             .append(" LoadLineItem.sLoadID = Move.sLoadID and")
             .append(" LoadLineItem.sItem = Move.sItem and")
             .append(" LoadLineItem.sLot = Move.sPickLot)");

    return fetchRecords(vpSql.toString());
  }
  
  /**
   * Initialize a large record list (the results are probably greater than
   * DB_MAX_ROWS).
   * 
   * <P>When doing LoadLineItem large lists, join with the Load and Item Master
   * tables to get the location and description.</P>
   *  
   * @see com.daifukuamerica.wrxj.dbadapter.BaseDBInterface#initializeLargeRecordList(int, com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData)
   */
  @Override
  public void initializeLargeRecordList(int inRowsPerPage,
      AbstractSKDCData ipKey) throws DBException
  {
    // Clear out SQL String buffer.
    StringBuilder vpSql = new StringBuilder("SELECT id.*, ld.").append(LoadData.WAREHOUSE_NAME)
               .append(", ld.").append(LoadData.ADDRESS_NAME)
               .append(", im.").append(ItemMasterData.DESCRIPTION_NAME)
               .append(" FROM LoadLineItem id, Load ld, ItemMaster im");

    // Rename key columns so they are not ambiguously defined
    // We can only do this with the local copy in case the key is used elsewhere.
    KeyObject[] vapKeys = new KeyObject[ipKey.getKeyArray().length];
    for (int i = 0; i < vapKeys.length; i++)
    {
      KeyObject k = ipKey.getKeyArray()[i];
      vapKeys[i] = (KeyObject)k.clone();
    }
    for (KeyObject k : vapKeys)
    {
      if (k.getColumnName().equals(LoadLineItemData.LOADID_NAME))
      {
        k.setColumnName("id." + LoadLineItemData.LOADID_NAME);
      }
      else if (k.getColumnName().equals(LoadLineItemData.ITEM_NAME))
      {
        k.setColumnName("id." + LoadLineItemData.ITEM_NAME);
      } 
      else if (k.getColumnName().equals(LoadLineItemData.HOLDTYPE_NAME))
      {
        k.setColumnName("id." + LoadLineItemData.HOLDTYPE_NAME);
      } 
    }
    
    // Build the WHERE clause
    String vsWhere = DBHelper.buildWhereClause(vapKeys);
    vpSql.append(vsWhere);
    
    // Add join keys
    if (vsWhere.trim().length() == 0)
    {
      vpSql.append(" WHERE ");
    }
    else
    {
      vpSql.append(" AND ");
    }
    vpSql.append(" id.").append(LoadLineItemData.LOADID_NAME)
               .append(" = ld.").append(LoadData.LOADID_NAME)
               .append(" AND id.").append(LoadLineItemData.ITEM_NAME)
               .append(" = im.").append(ItemMasterData.ITEM_NAME);
    
    // Specify the table for order by fields
    String[] vasOrderBy = ipKey.getOrderByColumns();
    for (int i = 0; i < vasOrderBy.length; i++)
    {
      vasOrderBy[i] = "id." + vasOrderBy[i];
    }
    vpSql.append(DBHelper.buildOrderByClause(vasOrderBy));
    
    initializeLargeRecordList(inRowsPerPage, vpSql.toString(), (Object[])null);
  }
}
