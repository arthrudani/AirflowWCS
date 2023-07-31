package com.daifukuamerica.wrxj.dbadapter.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

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

import com.daifukuamerica.wrxj.allocator.AllocationProbe;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBResultSet;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.daifukuamerica.wrxj.util.UnusedMethod;

/**
 * Description:<BR>
 *   Class to contain a collection of Table Join methods.
 *
 * @author       A.D.
 * @version      1.0     08/05/02
 */
public class TableJoin extends BaseDBInterface
{
  public static final String AGC_TRANSFER_SQL = "SELECT sDestID FROM STATION, ROUTE "
      + "WHERE sFromID = sstationname AND iStationType = "
      + DBConstants.AGC_TRANSFER + " AND sDestID = ?";
  public static final String EMPTY_BASE_SQL =
      "SELECT ld.* FROM Load ld, Location lc, Device dv, Station st ";

  protected AllocationProbe  allocDiag = null;
  protected OrderHeaderData  ohdata    = Factory.create(OrderHeaderData.class);
  protected StringBuffer     subquery1 = new StringBuffer();
  protected StringBuffer     subquery2 = new StringBuffer();
  protected boolean        mzAllowAltLots = true;

  public TableJoin()
  {
    super();
    mzAllowAltLots = Application.getBoolean("AllowAlternateLots", false);
  }

 /**
  * Method attaches an Allocation diagnostic object to this object so that the
  * caller can see what sorts of problems may occur when finding data for
  * allocation.
  * @param allocProbe Reference to an Allocation diagnostic object.
  */
  public void attachAllocationProbe(AllocationProbe allocProbe)
  {
    allocDiag = allocProbe;
  }

  /**
   *  Find all order lines that match a primary Route with our destination
   *  station. Then find the highest priority order header with the oldest
   *  scheduled date for that order lines order.
   *
   *  @param outputStation <code>String</code> containing destination station,
   *         which is also the route name.
   *  @param allocProbe  Added allocation probe to diagnose potential failures
   *         when finding allocation data.
   *  @return OrderHeaderData object containing order header info matching our
   *          search criteria.  <code>null</code> is returned if no match is
   *          found.
   */
  public OrderHeaderData getOrdersByOutputStation(String outputStation)
         throws DBException
  {
    // We are staging by route.  Make sure that the route is active.
    // In all cases, the route name should be the name of the final destination
    // station of the route.
    RouteData vpRouteKey = Factory.create(RouteData.class);
    vpRouteKey.setKey(RouteData.ROUTEID_NAME, outputStation);
    vpRouteKey.setKey(RouteData.DESTID_NAME, outputStation);
    vpRouteKey.setKey(RouteData.ROUTEONOFF_NAME, DBConstants.ON);
    Route vpRoute = Factory.create(Route.class);
    List<Map> vpRouteList = vpRoute.getAllElements(vpRouteKey);
    if (vpRouteList == null || vpRouteList.size() == 0)
    {
      // Allocation Probe
      if (allocDiag != null)
      {
        vpRouteKey.clear();
        vpRouteKey.setKey(RouteData.ROUTEID_NAME, outputStation);
        vpRouteKey.setKey(RouteData.DESTID_NAME, outputStation);
        vpRouteKey.setKey(RouteData.ROUTEONOFF_NAME, DBConstants.OFF);
        vpRouteList = vpRoute.getAllElements(vpRouteKey);

        if (vpRouteList == null || vpRouteList.size() == 0)
        {
          allocDiag.addProbeDetails("Tablejoin.getOrdersByOutputStation",
              "Route not found: " + outputStation);
        }
        else
        {
          allocDiag.addProbeDetails("Tablejoin.getOrdersByOutputStation",
              "Route off: " + outputStation);
        }
      }
      return(null);
    }

/*============================================================================
  Orders that are marked SHORT must be reactivated by the user.  Orders that
  automatically get marked as REALLOC (based on ShortOrderProcessor setting in
  Factory.properties) will be periodically checked for reallocation by a Timed
  Event Task (ShortOrderTask).  Only check for READY and ALLOCATENOW status orders
  here.
  ============================================================================*/
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT oh.* ")
               .append("FROM OrderHeader oh, OrderLine ol WHERE ")
               .append("oh.sOrderID = ol.sOrderID AND ")
               .append("ol.fAllocatedQuantity < ol.fOrderQuantity AND ")
               .append("oh.iOrderStatus IN (")
               .append(DBConstants.READY).append(", ")
               .append(DBConstants.ALLOCATENOW).append(") AND ")
               .append("(oh.sDestinationStation = ? OR ol.sRouteID = ?) ")
               .append("ORDER BY iPriority, dScheduledDate");

    OrderHeaderData myData = null;
    List<Map> arrList = fetchRecords(vpSql.toString(), outputStation,
        outputStation);
    if (!arrList.isEmpty())
    {
      ohdata.clear();
      ohdata.dataToSKDCData(arrList.get(0));
      myData = ohdata.clone();
    }
    // Allocation Probe
    else
      if (allocDiag != null)
      {
        String sql = "SELECT DISTINCT oh.* FROM OrderHeader oh, OrderLine ol "
            + "WHERE oh.sOrderID = ol.sOrderID AND ol.fAllocatedQuantity < ol.fOrderQuantity ";
        List<Map> ohTry1 = fetchRecords(sql);
        if (ohTry1.isEmpty())
        {
          allocDiag.addProbeDetails("Tablejoin.getOrdersByOutputStation",
              "Order Lines are filled for order on Route " + outputStation
                  + " or no orders exist.");
        }
        else
        {
          String tmpStr = DBConstants.READY + ", " + DBConstants.ALLOCATENOW;
          sql = sql + "AND oh.iOrderStatus IN (" + tmpStr + ") ";
          List<Map> ohTry2 = fetchRecords(sql);
          if (ohTry2.isEmpty())
          {
            allocDiag.addProbeDetails("TableJoin.getOrdersByOutputStation",
                "Invalid Order Statuses for allocations on " + "route "
                    + outputStation);
          }
        }
      }

    return (myData);
  }

 /**
  * Method to check if any Load Order Lines need to be allocated for this JVM.
  * @param isOrderID the order id.
  * @param isJVMId the JVM ID of this JVM.
  * @return <code>true</code> if this JVM has any Load Order Lines that still need
  *         to be allocated.
  * @throws DBException if there is a database access error.
  */
  public boolean anyLoadOrderLinesToAllocate(String isOrderID, String isJVMId)
         throws DBException
  {
    final String COUNT_COLUMN = "ROWCOUNT";

    StringBuilder vpSql = new StringBuilder("SELECT COUNT(ol.sLoadID) AS \"ROWCOUNT\" FROM ")
               .append("OrderLine ol, Load ld, Device dv, JVMConfig jvm WHERE ")
               .append("ol.sLoadID = ld.sLoadID AND ")
               .append("dv.sDeviceID = ld.sDeviceID AND ")
               .append("jvm.sJVMIdentifier = dv.sJVMIdentifier AND ")
               .append("ol.sOrderID = ? AND ")
               .append("ol.fAllocatedQuantity < ol.fOrderQuantity AND ")
               .append("jvm.sJVMIdentifier = ?");

    return(getRecordCount(vpSql.toString(), COUNT_COLUMN, isOrderID, isJVMId) > 0);
  }

 /**
  * Method gets all order lines for a given Order for which this JVM is
  * responsible.
  * @param isOrderID the order id.
  * @param isJVMId the JVM ID of this JVM.
  * @return List of order lines that can be allocaed for this JVM.
  * @throws DBException if there is a database access error.
  */
  public List<Map> getOrderLinesUnderThisJVM(String isOrderID, String isJVMId)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT ol.* FROM OrderLine ol, Load ld, Device dv, ")
               .append("JVMConfig jvm WHERE ")
               .append("ol.sLoadID = ld.sLoadID AND ")
               .append("dv.sDeviceID = ld.sDeviceID AND ")
               .append("jvm.sJVMIdentifier = dv.sJVMIdentifier AND ")
               .append("ol.sOrderID = ? AND ")
               .append("ol.fAllocatedQuantity < ol.fOrderQuantity AND ")
               .append("jvm.sJVMIdentifier = ? ORDER BY ol.sLoadID");

    return(fetchRecords(vpSql.toString(), isOrderID, isJVMId));
  }

  /**
   *  Find all item details that belong to a particular Aisle Group and find
   *  the quantity that best fits our order line quantity.
   *
   *  @param inAisleGroup <code>int</code> Aisle group for which we need to find
   *                                    an item detail.
   *  @param isItem <code>String</code> containing item.
   *  @param usLot <code>String</code> containing lot.
   *  @param idOrderQty <code>double</code> Order Line quantity we're trying to
   *                                       match.
   *
   *  @return LoadLineItemData object containing Item Detail info. matching our
   *          search criteria.
   */
  public LoadLineItemData getBestFitItemDetail(int inAisleGroup, String isItem,
                                               String usLot, double idOrderQty)
         throws DBException
  {
    String vsLocTypeList = DBConstants.LCASRS        + "," +
                           DBConstants.LCCONVSTORAGE + "," +
                           DBConstants.LCDEDICATED   + "," +
                           DBConstants.LCFLOW;

                                       // Figure out what how to deal with the
                                       // lot number.
    String vsLotString = "";
    if (usLot.trim().length() == 0)
      vsLotString = "sLot IS NULL ";
    else
      vsLotString = "sLot = '" + usLot + "' ";

    StringBuilder vpSql = new StringBuilder("SELECT ld.sLoadID, ")//AS \"sLoadID\", ")
        .append("id.sItem, ")
        .append("id.sLot, ")
        .append("id.fCurrentQuantity FROM ")
        .append("Load ld, LoadLineItem id, Location lc, ItemMaster im, ")
        .append("Device dv WHERE ld.sLoadID = id.sLoadID AND ")
        .append("lc.sWarehouse = ld.sWarehouse AND ")
        .append("lc.sAddress = ld.sAddress AND ")
        .append("im.sItem = id.sItem AND ")
        .append("dv.sDeviceID = lc.sDeviceID AND ")
        .append("im.iHoldType = ").append(DBConstants.ITMAVAIL).append(" AND ")
        .append("id.iHoldType = ").append(DBConstants.ITMAVAIL).append(" AND ")
        .append("lc.iAisleGroup = ").append(inAisleGroup).append(" AND ")
        .append("lc.iLocationStatus = ").append(DBConstants.LCAVAIL).append(" AND ")
        .append("lc.iLocationType in ( " + vsLocTypeList + ") AND ")
        .append("ld.iLoadMoveStatus = ").append(DBConstants.NOMOVE).append(" AND ")
        .append("dv.iOperationalStatus != ").append(DBConstants.INOP).append(" AND ")
        .append("id.sItem = '").append(isItem).append("' AND ")
        .append(vsLotString).append(" AND ")
        .append("id.fAllocatedQuantity = 0 AND ")
        .append("id.fCurrentQuantity BETWEEN 0 AND ").append(idOrderQty)
        .append(" ORDER BY id.fCurrentQuantity DESC");

    List<Map> vpItemDetailList = fetchRecords(vpSql.toString());
    if (vpItemDetailList == null || vpItemDetailList.size() == 0)
    {
      return null;
    }

    // Find single item loads from our list and check the quantities.
    int vdIDIdx = 0;
    double vdIDQty = 0;
    String vsBadLoad = "";
    Map vpLLIMap = null;
    LoadLineItem vpLLI = Factory.create(LoadLineItem.class);
    LoadLineItemData vpLLIData = Factory.create(LoadLineItemData.class);

    for (vdIDIdx = 0; vdIDIdx < vpItemDetailList.size(); vdIDIdx++)
    {
      vpLLIMap = vpItemDetailList.get(vdIDIdx);

      // If there is more than one item detail in this load, skip it.
      String vsLoadID = DBHelper.getStringField(vpLLIMap, LoadLineItemData.LOADID_NAME);
      if (vsLoadID.equals(vsBadLoad))
      {
        // Same load as last one. Skip it.
        continue;
      }

      vpLLIData.clear();
      vpLLIData.setKey(LoadLineItemData.LOADID_NAME, vsLoadID);
      if (vpLLI.getCount(vpLLIData) > 1)
      {
        if (allocDiag != null)
        {
          allocDiag.addProbeDetails("getBestFitItemDetail", vsLoadID
              + " has multiple item details.");
        }
        vsBadLoad = vsLoadID;
        continue;
      }
      vdIDQty = DBHelper.getDoubleField(vpLLIMap, LoadLineItemData.CURRENTQUANTITY_NAME);
      if (vdIDQty <= idOrderQty)
      {
        // This Item Detail will do. Convert it all to LoadLineItemData object.
        vpLLIData.dataToSKDCData(vpLLIMap);
        break;
      }
      else if (allocDiag != null)
      {
        allocDiag.addProbeDetails("getBestFitItemDetail", vsLoadID
            + " has more than the ordered quantity of " + isItem);
      }
    }

    if (vdIDIdx == vpItemDetailList.size())          // No item-detail found case.
    {
      if (allocDiag != null)
      {
        allocDiag.addProbeDetails("getBestFitItemDetail",
            "No appropriate loads found for item " + isItem + " (quantity="
                + idOrderQty + ")");
      }
      vpLLIData = null;
    }
    return vpLLIData;
  }

  /**
   * Find the oldest item details that are available, from locations that are
   * available. The oldest item detail could be by Aging Date (defaults to store
   * date) or by Expiration Date.
   *
   * @param isItem <code>String</code> containing item.
   * @param isLot <code>String</code> containing lot.
   * @param izSearchByZeroAllocated <code>boolean</code> if <code>true</code>
   *            means the search should be by loads that have no allocations
   *            against them.
   *
   * @param ipCustomObj var. arg. to allow for custom objects to be passed
   *        in for project customisations.
   * @return LoadLineItemData object containing Item Detail info. matching our
   *         search criteria.
   * @throws DBException
   */
  public List<Map> getOldestItemDetails(String isItem, String isLot,
                                        boolean izSearchByZeroAllocated,
                                        Object...ipCustomObj)
         throws DBException
  {
                                       // Figure out what how to deal with the
                                       // lot number.
    String lotString = null;
    String quantitySearch, orderByClause;

    if (isLot.trim().length() > 0)
    {
      lotString = "sLot = '" + isLot + "' ";
    }

    if (izSearchByZeroAllocated)
      quantitySearch = "id.fAllocatedQuantity = 0 ";
    else
      quantitySearch = "(id.fCurrentQuantity - id.fAllocatedQuantity) > 0 ";

    if (Factory.create(ItemMaster.class).isExpirationRequired(isItem))
      orderByClause = "ORDER BY id.iPriorityAllocation, id.dExpirationDate, id.dAgingDate";
    else
      orderByClause = "ORDER BY id.iPriorityAllocation, id.dAgingDate";

    StringBuffer tmpString = new StringBuffer();
    tmpString.append("SELECT id.* FROM ")
             .append("Load ld, LoadLineItem id, Location lc, ItemMaster im, ")
             .append("Device dv WHERE ld.sLoadID = id.sLoadID AND ")
             .append("ld.sWarehouse = lc.sWarehouse AND ")
             .append("ld.sAddress = lc.sAddress AND ")
             .append("im.sItem = id.sItem AND ")
             .append("dv.sDeviceID = lc.sDeviceID AND ")
             .append("im.iHoldType = ").append(DBConstants.ITMAVAIL).append(" AND ")
             .append("id.iHoldType = ").append(DBConstants.ITMAVAIL).append(" AND ")
             .append("lc.iLocationStatus = ").append(DBConstants.LCAVAIL).append(" AND ")
             .append("dv.iOperationalStatus != ").append(DBConstants.INOP).append(" AND ")
             .append("id.sItem = '").append(isItem).append("' ");

    StringBuilder vpSql = new StringBuilder(tmpString).append("AND ");
    if (lotString != null) vpSql.append(lotString).append(" AND ");

    vpSql.append(quantitySearch).append(orderByClause);

    List<Map> idList = fetchRecords(vpSql.toString());
    if ((mzAllowAltLots) && lotString != null && (idList == null || idList.size() == 0))
    {                                  // Try again with no Lot specified.
      vpSql.setLength(0);
      vpSql.append(tmpString).append("AND ")
               .append(quantitySearch).append(orderByClause);
      idList = fetchRecords(vpSql.toString());
    }

    String lotSQL = (lotString == null) ? "" : "AND " + lotString;
    if (allocDiag != null && idList.isEmpty())
    {
      String sql = "SELECT id.* FROM Load ld, LoadLineItem id, Location lc, " +
                   "ItemMaster im, Device dv WHERE ld.sLoadID = id.sLoadID AND " +
                   "ld.sWarehouse = lc.sWarehouse AND ld.sAddress = lc.sAddress AND " +
                   "im.sItem = id.sItem AND dv.sDeviceID = lc.sDeviceID AND " +
                   "id.sItem = \'" + isItem + "\' ";
      List<Map> tryList1 = fetchRecords(sql + lotSQL);
      if (tryList1.isEmpty())
      {
        if (!mzAllowAltLots)
        {
          allocDiag.addProbeDetails("TableJoin.getOldestItemDetails",
                                    "Allocation likely failed for one the following reasons:" +
                                    "(1) The location device(s) do not match the " +
                                    "aisle controlling device. " +
                                    "(2) There are no item details for item " + isItem +
                                    " and lot " + isLot + "\n" +
                                    "NOTE: Alternate lot search is not enabled.");
        }
        else
        {
          tryList1 = fetchRecords(sql);
          if (tryList1.isEmpty())
          {
            allocDiag.addProbeDetails("TableJoin.getOldestItemDetails",
                                      "Allocation likely failed for one the following reasons:" +
                                      "(1) The location device(s) do not match the " +
                                      "aisle controlling device. " +
                                      "(2) There are no item details for item " + isItem +
                                      "NOTE: Alternate lot search is not enabled.");
          }
        }
      }

      if (!tryList1.isEmpty())
      {
        sql = sql + lotSQL;
        sql = sql + "AND im.iHoldType = " + DBConstants.ITMAVAIL + " AND " +
              "id.iHoldType = " + DBConstants.ITMAVAIL + " AND " +
              "lc.iLocationStatus = " + DBConstants.LCAVAIL + " ";
        List<Map> tryList2 = fetchRecords(sql);
        if (tryList2.isEmpty())
        {
          allocDiag.addProbeDetails("TableJoin.getOldestItemDetails",
                                    "Allocation failed because one or more of the " +
                                    "following must be marked available: Location," +
                                    "ItemMaster, or Item Detail for item " + isItem);
        }
        else
        {
          sql = sql + "AND ld.iLoadMoveStatus IN (" + DBConstants.NOMOVE + ", " +
                DBConstants.RETRIEVEPENDING + ", " + DBConstants.RETRIEVING + ") ";
          List<Map> tryList3 = fetchRecords(sql);
          if (tryList3.isEmpty())
          {
            allocDiag.addProbeDetails("TableJoin.getOldestItemDetails",
                                      "Allocation failed because load with oldest " +
                                      "item detail for item " + isItem +
                                      " must have one of the following status\': " +
                                      "(1) No Move " +
                                      "(2) Retrieve Pending " +
                                      "(3) Retrieving");
          }
          else
          {
            sql = sql + "AND dv.iOperationalStatus != " + DBConstants.INOP + " ";
            List<Map> tryList4 = fetchRecords(sql);
            if (tryList4.isEmpty())
            {
              allocDiag.addProbeDetails("TableJoin.getOldestItemDetails",
                                        "Allocation failed because devices " +
                                        "of aisles with item " + isItem + " are not " +
                                        "operational.");
            }
          }
        }
      }
    }

    return(idList);
  }

 /**
  * Method finds the oldest item detail in dedicated locations.
  * @param sItem String containing the item for which item details must be found.
  * @param sLot  String containing the lot number to search by.
  * @throws DBException if there is a database access error.
  */
  public List<Map> getOldestDedicatedItemDetails(String sItem, String sLot) throws DBException
  {
                                       // Figure out what how to deal with the
                                       // lot number.
    String lotClause = " ";
    if (sLot.trim().length() > 0)
    {
      lotClause = "AND sLot = '" + sLot + "' ";
    }

    StringBuilder sqlBuffer = new StringBuilder();
    sqlBuffer.append("SELECT id.* FROM ")
             .append("Load ld, LoadLineItem id, Location lc, DedicatedLocation dl, ")
             .append("ItemMaster im, Device dv WHERE ")
             .append("ld.sLoadID = id.sLoadID AND ")
             .append("ld.sWarehouse = lc.sWarehouse AND ")
             .append("ld.sAddress = lc.sAddress AND ")
             .append("dl.sWarehouse = lc.sWarehouse AND ")
             .append("dl.sAddress = lc.sAddress AND ")
             .append("dl.sItem = id.sItem AND ")
             .append("im.sItem = id.sItem AND ")
             .append("dl.iReplenishNow = ").append(DBConstants.DLACTIVE).append(" AND ")
             .append("dv.sDeviceID = lc.sDeviceID AND ")
             .append("im.iHoldType = ").append(DBConstants.ITMAVAIL).append(" AND ")
             .append("id.iHoldType = ").append(DBConstants.ITMAVAIL).append(" AND ")
             .append("lc.iLocationType = ").append(DBConstants.LCDEDICATED).append(" AND ")
             .append("lc.iLocationStatus = ").append(DBConstants.LCAVAIL).append(" AND ")
             .append("ld.iLoadMoveStatus IN (")
             .append(DBConstants.NOMOVE).append(", ")
             .append(DBConstants.RETRIEVEPENDING).append(", ")
             .append(DBConstants.RETRIEVING).append(") AND ")
             .append("(id.fCurrentQuantity - id.fAllocatedQuantity) > 0 AND ")
             .append("id.sItem = '").append(sItem).append("' ");

    StringBuilder vpSql = new StringBuilder(sqlBuffer).append(lotClause)
             .append("ORDER BY id.iPriorityAllocation, id.dAgingDate");
    List<Map> idList = fetchRecords(vpSql.toString());

    if (idList.isEmpty() && mzAllowAltLots)
    {                                  // Try with no lot specified.
      vpSql.setLength(0);
      vpSql.append(sqlBuffer).append("ORDER BY id.iPriorityAllocation, id.dAgingDate");
      idList = fetchRecords(vpSql.toString());
    }

    if (allocDiag != null && idList.isEmpty())
    {
      sqlBuffer.setLength(0);
      sqlBuffer.append("SELECT id.* FROM ")
               .append("Load ld, LoadLineItem id, Location lc, DedicatedLocation dl, ")
               .append("ItemMaster im, Device dv WHERE ")
               .append("ld.sLoadID = id.sLoadID AND ")
               .append("ld.sWarehouse = lc.sWarehouse AND ")
               .append("ld.sAddress = lc.sAddress AND ")
               .append("dl.sWarehouse = lc.sWarehouse AND ")
               .append("dl.sAddress = lc.sAddress AND ")
               .append("dl.sItem = id.sItem AND ")
               .append("im.sItem = id.sItem AND ")
               .append("dl.iReplenishNow = ").append(DBConstants.DLACTIVE).append(" AND ")
               .append("lc.iLocationType = ").append(DBConstants.LCDEDICATED);
      vpSql.setLength(0);
      vpSql.append(sqlBuffer).append(" AND id.sItem = '").append(sItem).append("' ");
      List<Map> tryList = fetchRecords(vpSql.toString());
      if (tryList.isEmpty())
      {
        allocDiag.addProbeDetails("TableJoin.getOldestDedicatedItemDetails",
                                  "Warning: Allocation failed since dedicated location for item " +
                                  sItem + " is either not ACTIVE, or this item " +
                                  "is not a dedicated item!\nTrying Allocation " +
                                  "from alternate location...");
      }
      else
      {
        vpSql.append(" AND iLocationStatus = ").append(DBConstants.LCAVAIL);
        tryList = fetchRecords(vpSql.toString());
        if (tryList.isEmpty())
        {
          allocDiag.addProbeDetails("TableJoin.getOldestDedicatedItemDetails",
                                    "Warning: Dedicated location for item " + sItem +
                                    " is unavailable!\nTrying Allocation " +
                                    "from alternate location...");
        }
      }
    }

    return(idList);
  }

 /**
  * Finds item details for replenishment.  In the case of Full Load
  * replenishments all loads should be single part number loads, and have no
  * previous allocations.  Item details are found by oldest aging date or oldest
  * expiration date and the pieces per inner pack settings in the item master.
  * @param sItem  The item to replenish.
  * @param iReplenishAllocType the type of allocation that should be done to fill
  *        a dedicated location.
  * @param iAisleGroup  the aislegroup in which to search for replenishment data.
  *        If this is set to -1, it means any bulk area is open for a search.
  * @param iLocationType The location types to search in. This is set to ASRS or
  *        Conventional Storage.
  * @param fOrderQty The quantity on order.
  * @param dataList List of Item details that will be filled here.
  * @return preliminary calculation of what could be allocated for this item.
  * @throws DBException for database access errors.
  */
  public double getReplenItemDetails(String sItem, int iReplenishAllocType,
                                     int iAisleGroup, int iLocationType, double fOrderQty,
                                     List<Map> dataList) throws DBException
  {
    String orderByClause = "";
    String aisleGroupClause = "";
    String quantityClause = "";
    String loadStatusClause = "";

    if (iAisleGroup != -1)             // Use the aislegroup in the search for ASRS.
      aisleGroupClause = "lc.iAisleGroup = " + Integer.toString(iAisleGroup) +
                         " AND ";

    if (Factory.create(ItemMaster.class).isExpirationRequired(sItem))
      orderByClause = "ORDER BY id.iPriorityAllocation, id.dExpirationDate";
    else
      orderByClause = "ORDER BY id.iPriorityAllocation, id.dAgingDate";

    if (iReplenishAllocType == DBConstants.LOAD)
    {
      quantityClause = "id.fAllocatedQuantity = 0.0 AND ";
      loadStatusClause = "ld.iLoadMoveStatus = " + DBConstants.NOMOVE + " AND ";
    }
    else
    {
      quantityClause = "id.fCurrentQuantity - id.fAllocatedQuantity > 0.0 AND ";
      loadStatusClause = "ld.iLoadMoveStatus IN (" + DBConstants.NOMOVE + ", " +
                         DBConstants.RETRIEVEPENDING + ", " +
                         DBConstants.RETRIEVING + ") AND ";
    }

    StringBuilder vpSql = new StringBuilder("SELECT id.* FROM ")
             .append("Load ld, LoadLineItem id, Location lc, ")
             .append("ItemMaster im, Device dv WHERE ld.sLoadID = id.sLoadID AND ")
             .append("ld.sWarehouse = lc.sWarehouse AND ")
             .append("ld.sAddress = lc.sAddress AND ")
             .append("im.sItem = id.sItem AND ")
             .append("dv.sDeviceID = lc.sDeviceID AND ")
             .append("im.iHoldType = ").append(DBConstants.ITMAVAIL).append(" AND ")
             .append("id.iHoldType = ").append(DBConstants.ITMAVAIL).append(" AND ")
             .append("lc.iLocationStatus = ").append(DBConstants.LCAVAIL).append(" AND ")
             .append("lc.iLocationType = ").append(iLocationType).append(" AND ")
             .append(aisleGroupClause).append(loadStatusClause)
             .append("dv.iOperationalStatus != ").append(DBConstants.INOP).append(" AND ")
             .append(quantityClause)
             .append("id.sItem = '").append(sItem).append("' ").append(orderByClause);

    List<Map> idList = fetchRecords(vpSql.toString());

/*---------------------------------------------------------------------------
     Refine our list: exclude any multi-part number loads if the load is in an
     ASRS (since multi-part loads and flow rack dedicated locations don't make
     sense), or this replenishment is for a full load dedicated location. Also
     do a pre-allocation check and grab just enough item details to fill the
     order line.
  ---------------------------------------------------------------------------*/
    double allocatedAmount = 0.0;

    if (!idList.isEmpty())
    {
      double  idQty = 0.0;
      Map     currItemDet = null;
      int     listLength = idList.size();

      LoadLineItem itemDet = Factory.create(LoadLineItem.class);
      for(int idx = 0; idx < listLength; idx++)
      {
        currItemDet = idList.get(idx);
        if (iReplenishAllocType == DBConstants.LOAD)
        {
          String loadID = DBHelper.getStringField(currItemDet, LoadLineItemData.LOADID_NAME);
          if (!itemDet.isSinglePartLoad(loadID))
          {
            String errstr = "Cannot Allocate Multi Item load: " + loadID
                + " for Item: " + sItem
                + " for Full Load only dedicated location";
            getLogger().logOperation(errstr);
            logDiagnostic("Tablejoin.getReplenItemDetails", errstr);
            continue;
          }
        }

        idQty = DBHelper.getDoubleField(currItemDet, LoadLineItemData.CURRENTQUANTITY_NAME);
        if (idQty <= fOrderQty)
        {
          dataList.add(idList.get(idx));
          fOrderQty -= idQty;
          allocatedAmount += idQty;
        }
        else if (idQty > fOrderQty)    // We have enough.  Stop and return.
        {
          dataList.add(idList.get(idx));
          fOrderQty -= idQty;
          allocatedAmount += idQty;
          break;
        }
      } // End for-loop.
    }
    else if (allocDiag != null)
    {
      if (allocDiag != null && idList.isEmpty())
      {
        String sql = "SELECT id.* FROM Load ld, LoadLineItem id, Location lc, " +
                     "ItemMaster im, Device dv WHERE ld.sLoadID = id.sLoadID AND " +
                     "ld.sWarehouse = lc.sWarehouse AND " +
                     "ld.sAddress = lc.sAddress AND "     +
                     "im.sItem = id.sItem AND "           +
                     "dv.sDeviceID = lc.sDeviceID AND "   +
                     aisleGroupClause + quantityClause    +
                     "id.sItem = \'" + sItem + "\' ";

        List<Map> tryList1 = fetchRecords(sql);
        if (tryList1.isEmpty())
        {
          String msgAppender = (iReplenishAllocType != DBConstants.PIECE) ?
                              "Attempting to allocate a full load/full case and not enough product is available." :
                              "Attempting to allocate by pieces and not enough product is available.";
          allocDiag.addProbeDetails("TableJoin.getReplenItemDetails",
                                    "Allocation likely failed for one the following reasons:" +
                                    "(1) The location device(s) do not match the " +
                                    "aisle controlling device. " +
                                    "(2) There are no item details for item " + sItem + " " +
                                    "(3) " + msgAppender);
        }
        else
        {
          sql = sql + "AND im.iHoldType = " + DBConstants.ITMAVAIL + " AND " +
                "id.iHoldType = " + DBConstants.ITMAVAIL + " AND " +
                "lc.iLocationStatus = " + DBConstants.LCAVAIL + " ";
          List<Map> tryList2 = fetchRecords(sql);
          if (tryList2.isEmpty())
          {
            allocDiag.addProbeDetails("TableJoin.getReplenItemDetails",
                                      "Allocation failed because one or more of the " +
                                      "following must be marked available for item " + sItem + " " +
                                      "(1) Location(s) containing this item. " +
                                      "(2) ItemMaster or Item Detail");
          }
          sql = sql + "AND ld.iLoadMoveStatus IN (" + DBConstants.NOMOVE + ", " +
                DBConstants.RETRIEVEPENDING + ", " + DBConstants.RETRIEVING + ") ";
          List<Map> tryList3 = fetchRecords(sql);
          if (tryList3.isEmpty())
          {
            allocDiag.addProbeDetails("TableJoin.getReplenItemDetails",
                                      "Allocation failed because load with oldest " +
                                      "item detail for item " + sItem +
                                      " must have one of the following status\': " +
                                      "(1) No Move " +
                                      "(2) Retrieve Pending " +
                                      "(3) Retrieving");
          }
          else
          {
            sql = sql + "AND dv.iOperationalStatus != " + DBConstants.INOP + " ";
            List<Map> tryList4 = fetchRecords(sql);
            if (tryList4.isEmpty())
            {
              allocDiag.addProbeDetails("TableJoin.getReplenItemDetails",
                                        "Allocation failed because devices " +
                                        "of aisles with item " + sItem + " are not " +
                                        "operational.");
            }
          }
        }
      }
    }

    return(allocatedAmount);
  }

 /**
  * Method to fetch aggregates of Item details for a given warehouse, item, and
  * lot.
  * @param sWarehouse the warehouse where item details are to be found (Optional).
  * @param sItem the item id. of item details to be found (Optional).
  * @param sLot the lot number of item details to be found (Optional).
  * @return List of LoadLineItem data.
  * @throws DBException for DB access errors.
  */
  public List<Map> getLoadLineItemTotals(String sWarehouse, String sItem, String sLot) throws DBException
  {
    String whsSQL = (sWarehouse == null || sWarehouse.trim().length() == 0) ? ""
                                                   : "ld.sWarehouse = \'" + sWarehouse + "\' AND ";
    String lotSQL = (sLot == null || sLot.trim().length() == 0) ? ""
                                             : "id.sLot = \'" + sLot + "\' AND ";
    String itemSQL = (sItem == null || sItem.trim().length() == 0) ? ""
                                               : "id.sItem = \'" + sItem + "\' AND ";
    StringBuilder vpSql = new StringBuilder("SELECT ld.sWarehouse, id.sItem, id.sLot, id.sHoldReason, ")
          .append("SUM(id.fCurrentQuantity) AS \"FAVAILQUANTITY\" ")
          .append("FROM LoadLineItem id, Load ld, Location lc ")
          .append("WHERE ld.sLoadID = id.sLoadID AND ")
          .append("lc.sWarehouse = ld.sWarehouse AND ")
          .append("lc.sAddress = ld.sAddress AND ")
          .append(whsSQL).append(itemSQL).append(lotSQL)
          .append("id.fCurrentQuantity > 0 ")

          .append("GROUP BY ld.sWarehouse, id.sItem, id.sLot, id.sHoldReason ")
          .append("ORDER BY id.sItem");

    return fetchRecords(vpSql.toString());
  }

  /**
   * Method to fetch aggregates of Item details for a given warehouse, item, and
   * lot.
   * @param sItem the search item
   * @return List of LoadLineItem greater than sItem.
   * @throws DBException for DB access errors.
   */
   public List<Map> getItemTotalsGreaterThanItem(String sWarehouse, String sItem) throws DBException
   {
     String whsSQL = (sWarehouse == null || sWarehouse.trim().length() == 0) ? ""
         : "ld.sWarehouse = \'" + sWarehouse + "\' AND ";

     StringBuilder vpSql = new StringBuilder("SELECT ld.sWarehouse, id.sItem, id.sLot, id.sHoldReason, ")
           .append("SUM(id.fCurrentQuantity) AS \"FAVAILQUANTITY\" ")
           .append("FROM LoadLineItem id, Load ld, Location lc ")
           .append("WHERE ld.sLoadID = id.sLoadID AND ")
           .append("lc.sWarehouse = ld.sWarehouse AND ")
           .append("lc.sAddress = ld.sAddress AND ")
           .append(whsSQL)
           .append("id.sItem > \'").append(sItem).append("\' AND ")
           .append("id.fCurrentQuantity > 0 ")
           .append("GROUP BY ld.sWarehouse, id.sItem, id.sLot, id.sHoldReason ")
           .append("ORDER BY id.sItem");

     return fetchRecords(vpSql.toString());
   }

 /**
  * Method will retrieve a set of item details for a particular item that can be
  * put on hold.
  * @param sItem The item of the item detail that can be put on hold.
  * @param sLot The Lot number of the item detail.  If the Lot number is passed as
  *        a null, it means the lot will not be used in the search.  If a non-
  *        null pointer is passed, then it will be used in the search.
  * @return <code>java.util.List</code> of item details that can be put on hold.
  * @throws DBException
  */
  public List<Map> getHoldableLoadLineItems(String sItem, String sLot)
        throws DBException
  {
    String lotSQL = "";
    if (sLot != null)
    {
      if (sLot.trim().length() == 0) lotSQL = "AND sLot IS NULL ";
      else                           lotSQL = "AND sLot = \'" + sLot + "\' ";
    }
    StringBuilder vpSql = new StringBuilder("SELECT id.* FROM LoadLineItem id, Load ld, Location lc WHERE ")
             .append("ld.sLoadID = id.sLoadID AND ")
             .append("lc.sWarehouse = ld.sWarehouse AND ")
             .append("lc.sAddress = ld.sAddress AND ")
             .append("id.iHoldType = ").append(DBConstants.ITMAVAIL).append(" AND ")
             .append("id.sItem = '").append(sItem).append("' ")
             .append(lotSQL);

    return fetchRecords(vpSql.toString());
  }

 /**
  * Method to get total available inventory for an item and lot and Aislegroup.
  * If the warehouse is unknown or not important in the search it can be excluded.
  *
  * @param isItem the inventory item.
  * @param isLot the lot to search by.  If this is passed as a <code>null</code>
  *        pointer, it will not be used in the search.  If it is passed as an empty
  *        string, a search will be conducted for a NULL lot.
  * @param isWarehouse the warehouse in which to search for inventory.  This
  *        parameter is optional.
  * @param inAisleGroup the aisle group by which to filter search.
  *
  * @return the total available quantity of inventory in an aisle group.
  * @throws DBException if there is a database access error.
  */
  public double getTotalAvailableQuantity(String isItem, String isLot,
                                          String isWarehouse, int inAisleGroup)
         throws DBException
  {
  //TODO: will be used in future refactoring(s) of baseline allocator.  Currently
  // used at COX. -- A.D.

    String vsWhsSQL = "", vsLotSQL = "";
    if (isWarehouse != null && isWarehouse.trim().length() != 0)
    {
      vsWhsSQL = "AND lc.sWarehouse = \'" + isWarehouse + "\' ";
    }

    if (isLot != null)
    {
      if (isLot.trim().length() == 0) vsLotSQL = "AND sLot IS NULL ";
      else                            vsLotSQL = "AND sLot = \'" + isLot + "\' ";
    }

    StringBuilder vpSql = new StringBuilder("SELECT SUM(id.fCurrentQuantity) AS \"fTotalQty\", ")
             .append("SUM(id.fAllocatedQuantity) AS \"fTotalAllocatedQty\" FROM ")
             .append("LoadLineItem id, Load ld, Location lc, Device dv, ItemMaster im ")
             .append("WHERE ld.sLoadid = id.sLoadID AND ")
             .append("lc.sWarehouse = ld.sWarehouse AND ")
             .append("lc.sAddress = ld.sAddress AND ")
             .append("im.sItem = id.sItem AND ")
             .append("dv.sDeviceID = lc.sDeviceID AND ")
             .append("im.iHoldType = ").append(DBConstants.ITMAVAIL).append(" AND ")
             .append("id.iHoldType = ").append(DBConstants.ITMAVAIL).append(" AND ")
             .append("lc.iLocationStatus = ").append(DBConstants.LCAVAIL).append(" AND ")
             .append("ld.iLoadMoveStatus = ").append(DBConstants.NOMOVE).append(" AND ")
             .append("dv.iOperationalStatus != ").append(DBConstants.INOP).append(" AND ")
             .append("lc.iAisleGroup = ").append(inAisleGroup).append(" AND ")
             .append("id.sItem = '").append(isItem).append("' ")
             .append(vsLotSQL).append(vsWhsSQL);

    List<Map> vpList = fetchRecords(vpSql.toString());
    double vdTotalQty = 0, vdAllocatedQty = 0;

    if (!vpList.isEmpty())
    {
      vdTotalQty     = DBHelper.getDoubleField(vpList.get(0), "fTotalQty");
      vdAllocatedQty = DBHelper.getDoubleField(vpList.get(0), "fTotalAllocatedQty");
    }

    return(vdTotalQty - vdAllocatedQty);
  }

  /**
   * Best Unit fit item detail finder.
   * @param inAisleGroup
   * @param isItem
   * @param isLot
   * @param idOrderQty
   * @return LoadLineItemData
   * @throws DBException
   */
  @UnusedMethod
  public LoadLineItemData getBestUnitFitItemDetail(int inAisleGroup,
      String isItem, String isLot, double idOrderQty) throws DBException
  {
    double vdNewOrderQty = 0;

    ItemMaster vpIM = Factory.create(ItemMaster.class);
    ItemMasterData vpIMData = vpIM.getItemMasterData(isItem);

    int vpPiecesPerUnit = vpIMData.getPiecesPerUnit();
    if (vpPiecesPerUnit == 0)
    {
      throw new DBException("Invalid pieces per unit found for item " + isItem);
    }
                                       // Convert Order Qty. to Pieces-Per-Unit.
    if (vpPiecesPerUnit > 0)
    {
      int ppunitOrdqty = (int)Math.floor(idOrderQty/vpPiecesPerUnit);
      if (ppunitOrdqty == 0)
      {
        vdNewOrderQty = idOrderQty;
      }
      else if (ppunitOrdqty > 0)
      {
        vdNewOrderQty = (ppunitOrdqty*vpPiecesPerUnit);
      }
    }
    else
    {
      vdNewOrderQty = idOrderQty;
    }

    return getBestFitItemDetail(inAisleGroup, isItem, isLot, vdNewOrderQty);
  }

  /**
   * Method to get partially full containers.
   *
   * @param isZoneGroup
   * @param inAisleGroup
   * @param ipOLData
   * @param inTranAmtFullValue
   * @param izWithLike
   * @param isDestination
   * @return
   * @throws DBException
   */
  public List<Map> getPartialEmpties(String isZoneGroup, int inAisleGroup,
      OrderLineData ipOLData, int inTranAmtFullValue, boolean izWithLike,
      String isDestination) throws DBException
  {
    String vsLocTypeList = DBConstants.LCASRS + "," + DBConstants.LCCONVSTORAGE;

    List<Map> ldList = null;
    StringBuilder vpSql = new StringBuilder();
    if (izWithLike == false)
    {
      List<Object> vpParameters = new ArrayList<Object>();
      vpParameters.add(ipOLData.getContainerType());
      vpParameters.add(inAisleGroup);
      vpParameters.add(ipOLData.getHeight());

      vpSql.append(EMPTY_BASE_SQL);
      if (isZoneGroup.trim().length() > 0)
      {
        vpSql.append(", ZoneGroup zg ");
      }
      vpSql.append("WHERE sParentLoad = sLoadID AND ")
               .append("ld.sContainerType = ? AND ")
               .append("sParentLoad = sLoadID AND ")
               .append("iLoadMoveStatus = ").append(DBConstants.NOMOVE).append(" AND ")
               .append("ld.iAmountFull <= ").append(inTranAmtFullValue).append(" AND ")
               .append("ld.iAmountFull > ").append(DBConstants.EMPTY).append(" AND ")
               .append("lc.sWarehouse = ld.sWarehouse AND ")
               .append("lc.sAddress = ld.sAddress AND ")
               .append("lc.iAisleGroup = ? AND ")
               .append("lc.iHeight >= ? AND ")
               .append("lc.iLocationStatus = ").append(DBConstants.LCAVAIL).append(" AND ")
               .append("lc.iLocationType in (").append(vsLocTypeList).append(") ");

      // TODO: There should be route checking.  For the time being, however...
      // The load needs to have the same device as the destination
      vpSql.append(" AND st.sStationName = ? ");
      vpParameters.add(isDestination);
      vpSql.append(" AND ((ld.sDeviceID=st.sDeviceID AND dv.sDeviceID=st.sDeviceID)");
      // or have the same comm device as the station's device
      vpSql.append(" OR (ld.sDeviceID=dv.sDeviceID AND dv.sCommDevice=st.sDeviceID)) ");

      if (isZoneGroup.trim().length() > 0)
      {
        vpSql.append(" AND zg.SZONEGROUP=? AND zg.SZONE=lc.sZone ");
        vpParameters.add(isZoneGroup);
        vpSql.append("ORDER BY ld.iAmountFull DESC, ld.dMoveDate, zg.iPriority");
      }
      else
      {
        vpSql.append("ORDER BY ld.iAmountFull DESC, ld.dMoveDate");
      }

      ldList = fetchRecords(vpSql.toString(), vpParameters.toArray());
    }
    else
    {
      ldList = getLikeItemsContainer(inAisleGroup, inTranAmtFullValue, ipOLData);
    }

    return(ldList);
  }

  /**
   * Gets totally empty containers by matching aisle group and container type.
   *
   * @param isZoneGroup <code>String</code> ZoneGroup to search
   * @param inAisleGroup <code>int</code> Aisle group to check against.
   * @param isContainerType <code>String</code> containing Container Type of
   *            the load.
   * @param inHeight <code>int</code> the location height
   * @param isDestination <code>String</code> destination station
   * @return List of Empty <code>LoadData</code> maps.
   * @throws DBException
   */
  public List<Map> getCompleteEmpties(String isZoneGroup, int inAisleGroup,
      String isContainerType, int inHeight, String isDestination)
      throws DBException
  {
    List<Object> vpParameters = new ArrayList<Object>();
    vpParameters.add(isContainerType);
    vpParameters.add(inAisleGroup);
    vpParameters.add(inHeight);

    String vsLocTypeList = DBConstants.LCASRS + "," + DBConstants.LCCONVSTORAGE;
    StringBuilder vpSql = new StringBuilder(EMPTY_BASE_SQL);
    if (isZoneGroup.trim().length() > 0)
    {
      vpSql.append(", ZoneGroup zg ");
    }
    vpSql.append("WHERE sParentLoad = sLoadID AND ")
             .append("iLoadMoveStatus = ").append(DBConstants.NOMOVE).append(" AND ")
             .append("ld.sContainerType = ? AND ")
             .append("lc.sWarehouse = ld.sWarehouse AND ")
             .append("lc.sAddress = ld.sAddress AND ")
             .append("ld.iAmountFull = ").append(DBConstants.EMPTY).append(" AND ")
             .append("lc.iLocationStatus = ").append(DBConstants.LCAVAIL).append(" AND ")
             .append("lc.iLocationType in (" + vsLocTypeList + ") AND ")
             .append("lc.iAisleGroup = ? AND lc.iHeight >= ?");

    vpSql.append(" AND st.sStationName = ? ");
    vpParameters.add(isDestination);
    vpSql.append(" AND ((ld.sDeviceID=st.sDeviceID AND dv.sDeviceID=st.sDeviceID)");
    // or have the same comm device as the station's device
    vpSql.append(" OR (ld.sDeviceID=dv.sDeviceID AND dv.sCommDevice=st.sDeviceID)) ");

    if (isZoneGroup.trim().length() > 0)
    {
      vpSql.append(" AND zg.SZONEGROUP=? AND zg.SZONE=lc.sZone ");
      vpParameters.add(isZoneGroup);
      vpSql.append("ORDER BY zg.iPriority, dMoveDate");
    }
    else
    {
      vpSql.append("ORDER BY dMoveDate");
    }

    return(fetchRecords(vpSql.toString(), vpParameters.toArray()));
  }

  /**
   * Checks if a load is in in the rack with specified aisle group.
   *
   * @param isLoadID <code>String</code> containing Load in question.
   * @param isFinalStation <code>String</code> final destination for the load.
   * @param izCheckRetrievePend Check for Retrieve Pending loads too.
   * @return boolean of <code>true</code> if load is in aisle group.
   * @throws DBException if there is a DB access error.
   */
  public boolean isLoadAllocatable(String isLoadID, String isFinalStation,
      boolean izCheckRetrievePend) throws DBException
  {
    String vsMoveStatusSQL;

    if (izCheckRetrievePend)
    {
      vsMoveStatusSQL = "ld.iLoadMoveStatus IN (" + DBConstants.NOMOVE + ", " +
                        DBConstants.RETRIEVEPENDING + ")";
    }
    else
    {
      vsMoveStatusSQL = "ld.iLoadMoveStatus = " + DBConstants.NOMOVE;
    }

    StringBuilder vpSql = new StringBuilder("SELECT COUNT(DISTINCT ld.sLoadID) AS \"rowCount\" FROM ")
             .append("Location lc, Load ld, Route rt WHERE ")
             .append("ld.sLoadID = ? AND rt.sRouteID = ? AND ")
             .append("ld.sDeviceID = rt.sFromID AND ")
             .append("lc.sWarehouse = ld.sWarehouse AND ")
             .append("lc.sAddress = ld.sAddress AND ")
             .append("lc.iLocationStatus = ").append(DBConstants.LCAVAIL).append(" AND ")
             .append(vsMoveStatusSQL);

    return getRecordCount(vpSql.toString(), "rowCount", isLoadID, isFinalStation) > 0;
  }

  /**
   * Method checks if a location is Conventional by checking its device.
   * @param sWarehouse the warehouse of the location.
   * @param sAddress the address of the location.
   * @return boolean of <code>true</code> if location is conventional,
   *         <code>false</code> otherwise.
   * @throws DBException if there is a serious database access error.
   */
  public boolean isConventionalLocation(String sWarehouse, String sAddress)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT dv.sDeviceID FROM Location lc, Device dv WHERE ")
             .append("dv.sDeviceID = lc.sDeviceID AND ")
             .append("dv.iDeviceType = ").append(DBConstants.CONV_DEVICE).append(" AND ")
             .append("lc.sWarehouse = '").append(sWarehouse).append("' AND ")
             .append("lc.sAddress = '").append(sAddress).append("'");

    return getStringColumn("SDEVICEID", vpSql.toString()).trim().length() != 0;
  }


 /**
  * Method gets all loads within a range of locations <u>provided</u> the loads
  * don't have allocated product in them.
  *
  * @param isWarehouse The location warehouse
  * @param isBeginAddress the location range start address.
  * @param isEndAddress the location range ending address.
  * @return List&lt;Map&gt; of Load records.
  * @throws DBException if there is a database access error.
  */
  public List<Map> getLoadsInLocationRange(String isWarehouse,
                                           String isBeginAddress,
                                           String isEndAddress)
         throws DBException
  {
    String vsLoadFilter = "SELECT DISTINCT ld.sLoadID FROM Load ld, " +
                          "LoadLineItem id WHERE ld.sLoadID = id.sLoadID AND " +
                          "ld.sWarehouse = ? AND ld.sAddress BETWEEN ? AND ?";

    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT ld.* FROM Load ld, LoadLineItem id WHERE ")
               .append("ld.sLoadID = id.sLoadID AND ")
               .append("id.fAllocatedQuantity = 0 AND ")
               .append("ld.sWarehouse = ? AND ")
               .append("ld.sAddress BETWEEN ? AND ? UNION ")
               .append("SELECT * FROM Load WHERE ")
               .append("sLoadID NOT IN (").append(vsLoadFilter).append(") AND ")
               .append("sWarehouse = ? AND ")
               .append("sAddress BETWEEN ? AND ?");

    List<Map> vpList = fetchRecords(vpSql.toString(),
                                    isWarehouse, isBeginAddress, isEndAddress,
                                    isWarehouse, isBeginAddress, isEndAddress,
                                    isWarehouse, isBeginAddress, isEndAddress);
    return(vpList);
  }

 /**
  *  Given a range of locations, this method returns a list of Item Details in
  *  them if the locations are (ASRS, Conventional, Dedicated, or Flow Rack)
  *  and AVAILABLE. This method does not depend on Locations as being marked
  *  occupied, but rather joins the load and location table to find the correct
  *  data.
  *
  *  @param beginLocation <code>String</code> containing warehouse of locations.
  *  @param endLocation <code>String</code> containing Beginning location
  *         in range.
  *
  *  @return <code>List[]</code> containing Item Details that are in this
  *          location range.
  */
  public List<Map> getItemDetailsInRange(String beginLocation,
                                         String endLocation) throws DBException
  {
    String vsLocTypeList = DBConstants.LCASRS        + "," +
                           DBConstants.LCCONVSTORAGE + "," +
                           DBConstants.LCDEDICATED;
// Can't physically count things in the flow rack...  DBConstants.LCFLOW;

    String[] begLocn = Location.parseLocation(beginLocation);
    String[] endLocn = Location.parseLocation(endLocation);
    String addressSQL = "";

    if (endLocn[1].trim().length() == 0)
    {
      addressSQL = " ld.sWarehouse = '" + begLocn[0] + "' AND ld.sAddress >= '" +
                   begLocn[1] + "' ";
    }
    else
    {
      if (begLocn[0].equals(endLocn[0]))
      {
        addressSQL = " ld.sWarehouse = '" + begLocn[0] + "' AND ld.sAddress " +
                     " BETWEEN '" + begLocn[1] + "' AND '" + endLocn[1] + "' ";
      }
      else
      {
        addressSQL = " ld.sWarehouse IN ('" + begLocn[0] + "', '" + endLocn[0] +
                     "') AND ld.sAddress " + " BETWEEN '" + begLocn[1] +
                     "' AND '" + endLocn[1] + "' ";
      }
    }

    StringBuilder vpSql = new StringBuilder("SELECT id.* FROM ")
             .append("Load ld, LoadLineItem id, Location lc WHERE ")
             .append("lc.sWarehouse = ld.sWarehouse AND ")
             .append("lc.sAddress = ld.sAddress AND ")
             .append("ld.sLoadID = id.sLoadID AND ")
             .append("lc.iLocationType in ( " + vsLocTypeList + ") AND ")
             .append("iLocationStatus = ").append(DBConstants.LCAVAIL)
             .append(" AND ").append(addressSQL);

    return fetchRecords(vpSql.toString());
  }

  /**
   * Method returns true if the Order item has any inventory that can be
   * allocated.
   *
   * @param isOrderID <code>String</code> containing order being checked
   *            against.
   * @param isItem <code>String</code> containing orderline item being checked
   *            against.
   *
   * @param isOrdLot the order lot.
   * @param isOrdDest the order destination station.
   * @return <code>boolean</code> of <code>true</code> if there is any
   *         allocatable inventory.
   * @throws DBException
   */
  public boolean orderLineHasSufficientInventory(String isOrderID, String isItem,
                                                 String isOrdLot, String isOrdDest)
         throws DBException
  {
    String vsLotSQL = "";
    if (!mzAllowAltLots)
    { // AllowAltLots = false, use lots exactly as specified by caller.
      if (isOrdLot == null || isOrdLot.isEmpty())
      {
        vsLotSQL = "sLot IS NULL AND ";
      }
      else
      {
        vsLotSQL = "sLot = \'" + isOrdLot + "\' AND ";
      }
    }

    StringBuilder vpSql = new StringBuilder("SELECT COUNT(id.sItem) AS \"rowCount\" FROM ")
               .append("Load ld, LoadLineItem id, OrderLine ol, Device dv, Route rt WHERE ")
               .append("ol.sItem = id.sItem AND ")
               .append("ld.sLoadID = id.sLoadID AND ")
               .append("ol.sOrderID = ? AND ")
               .append("id.sItem = ? AND ").append(vsLotSQL)
               .append("rt.sRouteID = ? AND ")
               .append("rt.iFromType = ").append(DBConstants.EQUIPMENT).append(" AND ")
               .append("rt.sFromID = ld.sDeviceID AND ")
               .append("id.iHoldType = ").append(DBConstants.ITMAVAIL).append(" AND ")
               .append("ld.sDeviceID = dv.sDeviceID AND ")
               .append("dv.iOperationalStatus != ").append(DBConstants.INOP).append(" AND ")
               .append("id.fCurrentQuantity - id.fAllocatedQuantity >= ")
               .append("ol.fOrderQuantity - ol.fAllocatedQuantity AND ")
               .append("NOT EXISTS(SELECT sStationName FROM Station WHERE ")
               .append("sStationName = ld.sFinalAddress)");

    return getRecordCount(vpSql.toString(), "rowCount", isOrderID, isItem, isOrdDest) > 0;
  }

  /**
   * Method to find all location addresses belonging to a particular item.
   * <strong>Note:</strong> this method does not account for super warehouses.
   *
   * @param warhse <code>String</code> containing Warehouse for which to find
   *            an empty location. This can be a super warehouse if necessary.
   * @param item <code>String</code> containing item to match locations with.
   * @param iAisleGroup <code>int</code> integer containing aisle group from a
   *            putaway station.
   * @param height <code>int</code> Height of the location to search for
   *            (presumably dictated by the load height).
   *
   * @return <code>String[]</code> containing list of location addresses
   *         containing items that are the same as the passed in item.
   *
   * @exception DBException if database error.
   */
  public String[] getLikeItemLocation(String warhse, String item,
      int iAisleGroup, int height) throws DBException
  {
    String vsLocTypeList = DBConstants.LCASRS        + "," +
                           DBConstants.LCCONVSTORAGE + "," +
                           DBConstants.LCDEDICATED   + "," +
                           DBConstants.LCFLOW;

    String sqlString;
    sqlString = "SELECT lc.sAddress FROM Location lc, Load ld, "           +
                "LoadLineItem id WHERE lc.sWarehouse = ld.sWarehouse AND " +
                "lc.sAddress = ld.sAddress AND "               +
                "ld.sLoadID = id.sLoadID AND "                 +
                "lc.sWarehouse = \'" + warhse      + "\' AND " +
                "lc.iLocationType in ( " + vsLocTypeList + ") AND " +
                "lc.iAislegroup = "  + iAisleGroup + " AND "   +
                "lc.iHeight >= "     + height      + " AND  "  +
                "id.sItem = \'"      + item        + "\' "     +
                "ORDER BY lc.sAddress";

    return(SKDCUtility.toStringArray(fetchRecords(sqlString), "SADDRESS"));
  }

 /**
  *  Gets loads that have moves that are AVAILABLE.
  *
  *  @param isSchedulerName a scheduler that may have unscheduled moves.
  * @return <code>List</code> of Load moves that the scheduler does not
  *          know about.
  */
  public List<Map> getUnscheduledLoadMoves(String isSchedulerName)
         throws DBException
  {
    if (subquery1.length() > 0) subquery1.setLength(0);
    if (subquery2.length() > 0) subquery2.setLength(0);

    subquery1.append("(SELECT DISTINCT sParentLoad FROM MoveView WHERE ")
             .append("sSchedulerName = '").append(isSchedulerName).append("' AND ")
             .append("sParentLoad IS NOT NULL) ");

    subquery2.append("(SELECT DISTINCT sLoadID FROM MoveView WHERE ")
             .append("sSchedulerName = '").append(isSchedulerName).append("') ");

    StringBuilder vpSql = new StringBuilder("SELECT ld.* FROM Load ld, Move mv WHERE ")
               .append("ld.sLoadID = mv.sLoadID AND ")
               .append("ld.sParentLoad = mv.sParentLoad AND ")
               .append("mv.iMoveStatus = ").append(DBConstants.AVAILABLE)
               .append(" AND ld.iLoadMoveStatus = ").append(DBConstants.RETRIEVEPENDING)
               .append(" AND (")
               .append("mv.sParentLoad IN ").append(subquery1).append(" OR ")
               .append("mv.sLoadID IN ").append(subquery2).append(")");

    return fetchRecords(vpSql.toString());
  }

 /**
  *  Get the recommended warehouse for a load
  *
  *  @return <code>String</code> containing load's recommended warehouse
  */
  public String getLoadsRecommendedWarehouse(String loadID) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT lli.sItem, ")
             .append("im.sRecommendedWarehouse AS \"sRecommendedWarehouse\" FROM ")
             .append("LoadLineItem lli, ItemMaster im, Load ld WHERE ")
             .append("ld.sLoadID = '").append(loadID).append("' AND ")
             .append("ld.sLoadID = lli.sLoadID AND ")
             .append("lli.sItem = im.sItem AND ")
             .append("im.sRecommendedWarehouse IS NOT NULL");

    List<Map> returnList = fetchRecords(vpSql.toString());
    if (returnList == null || returnList.size() == 0)
    {
      return(null);
    }

    for(Iterator<Map> it = returnList.iterator(); it.hasNext();)
    {
      Map tMap = it.next();
      return (DBHelper.getStringField(tMap, "sRecommendedWarehouse"));
     }

    return(null);
  }

 /**
  *  Get the recommended zone for a load
  *
  *  @return <code>String</code> containing load's recommended zone
  */
  public String getLoadsRecommendedZone(String loadID) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT lli.sItem, ")
             .append("im.sRecommendedZone AS \"sRecommendedZone\" FROM ")
             .append("LoadLineItem lli, ItemMaster im, Load ld WHERE ")
             .append("ld.sLoadID = '").append(loadID).append("' AND ")
             .append("ld.sLoadID = lli.sLoadID AND ")
             .append("lli.sItem = im.sItem AND ")
             .append("im.sRecommendedZone IS NOT NULL");

    List<Map> returnList = fetchRecords(vpSql.toString());
    if (returnList == null || returnList.size() == 0)
    {
      return(null);
    }

    for(Iterator<Map> it = returnList.iterator(); it.hasNext();)
    {
      Map tMap = it.next();
      return (DBHelper.getStringField(tMap, "sRecommendedZone"));
     }

    return(null);
  }


  /**
   * Get the PO's with the specific POSearch, POType, Item, Lot on them
   *
   * @return <code>List</code> of Purchase Order Headers that match the
   *         criteria
   */
  public List<Map> getPOSearchList(PurchaseOrderHeaderData pohdata, PurchaseOrderLineData poldata) throws DBException
  {
    subquery1.setLength(0);
    subquery2.setLength(0);

    subquery1.append(DBHelper.buildWhereClause(pohdata.getKeyArray(), "POH", true));
    subquery2.append(DBHelper.buildWhereClause(poldata.getKeyArray(), "POL", true));

    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT POH.* ")
      .append("FROM PURCHASEORDERHEADER POH, PURCHASEORDERLINE POL ")
      .append(" WHERE POH.SORDERID = POL.SORDERID ");
    if (subquery1.length() > 1) // buildWhereClause may return a " "
    {
      vpSql.append(" AND ").append(subquery1);
    }
    if (subquery2.length() > 1) // buildWhereClause may return a " "
    {
      vpSql.append(" AND ").append(subquery2);
    }
    vpSql.append(" ORDER BY POH.SORDERID");


    return fetchRecords(vpSql.toString());
  }

  public List<Map> getPOSearchList(String sPONum, int iPOStatus, String sItem,
                                   String sLot) throws DBException
  {
    // We will always use item or lot, otherwise we would not be called here
    // we are here only because we need to do a join between the
    // PO Header table and the PO Line table because the item was not blank


    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT POH.* ")
      .append("FROM PURCHASEORDERHEADER POH, PURCHASEORDERLINE POL ")
      .append(" WHERE POH.SORDERID = POL.SORDERID ");

    if(sPONum.trim().length() > 0)
    {
      vpSql.append(" AND POH.SORDERID LIKE '").append(sPONum).append("%' ");
    }

    if(sItem.trim().length() > 0)
    {
      vpSql.append(" AND POL.SITEM LIKE '").append(sItem).append("%' ");
    }

    if(sLot.trim().length() > 0)
    {
      vpSql.append(" AND POL.SLOT LIKE '").append(sLot).append("%' ");
    }

    if(iPOStatus != SKDCConstants.ALL_INT)
    {
      vpSql.append(" AND IPURCHASEORDERSTATUS = ").append(iPOStatus);
    }

    vpSql.append(" ORDER BY POH.SORDERID");
    return fetchRecords(vpSql.toString());
  }

  /**
   *  Get the Order's with the specific Order Search, Order Type, Item, Lot on them
   *
   *  @return <code>List</code> of Purchase Order Headers that match the
   *  criteria
   */
   public List<Map> getOrderSearchList(OrderHeaderData ipOHD, OrderLineData ipOLD) throws DBException
   {
     String vsOHString = DBHelper.buildWhereClause(ipOHD.getKeyArray(), "OH", false);

     // We will always use item or lot, otherwise we would not be called here
     // we are here only because we need to do a join between the
     // Order Header table and the Order Line table because the item or lot was not blank
     String tmpstring = DBHelper.buildWhereClause(ipOLD.getKeyArray(), "OL", true);

     StringBuilder vpSql = new StringBuilder("SELECT DISTINCT OH.* ")
       .append("FROM ORDERHEADER OH, ORDERLINE OL ");
     if (vsOHString.trim().length() > 0)
     {
       vpSql.append(vsOHString)
                .append(" AND OH.SORDERID = OL.SORDERID ");
     }
     else
     {
       vpSql.append(" WHERE OH.SORDERID = OL.SORDERID ");
     }

     if(tmpstring.trim().length() > 0)
     {
       vpSql.append(" AND ")
                .append(DBHelper.buildWhereClause(ipOLD.getKeyArray(), "OL", true));
     }

     vpSql.append(" ORDER BY OH.SORDERID");
     return fetchRecords(vpSql.toString());
   }

  /**
   * Delete all Item masters that have iDeleteAtZeroQuantity set to Yes, and
   * have no other database table dependencies.
   */
  public void deleteAllAutoDeletableItemMasters() throws DBException
  {
    String s = "DELETE itemMaster WHERE" +
      " (itemMaster.iDeleteAtZeroQuantity = " + DBConstants.YES +
      ") AND NOT EXISTS (SELECT sItem FROM LoadLineItem WHERE LoadLineItem.sItem = itemMaster.sItem)" +
      " AND NOT EXISTS (SELECT sItem FROM OrderLine WHERE OrderLine.sItem = itemMaster.sItem)" +
      " AND NOT EXISTS (SELECT sItem FROM Move WHERE Move.sItem = itemMaster.sItem)" +
      " AND NOT EXISTS (SELECT sItem FROM DedicatedLocation WHERE DedicatedLocation.sItem = itemMaster.sItem)" +
      " AND NOT EXISTS (SELECT sItem FROM Station WHERE Station.sItem = itemMaster.sItem)" +
      " AND NOT EXISTS (SELECT sItem FROM PurchaseOrderLine WHERE PurchaseOrderLine.sItem = itemMaster.sItem)";

    try
    {
      execute(s, (Object[])null);
    }
    catch (NoSuchElementException e)
    {
      // We don't care if we did not delete any rows
    }

    try
    {
      // now cleanup any synonyms whose item master does not exist
      String s2 = "DELETE Synonyms WHERE NOT EXISTS (SELECT sItem FROM ItemMaster WHERE Synonyms.sItem = itemMaster.sItem)";
      execute(s2, (Object[])null);
    }
    catch (NoSuchElementException e)
    {
      // We don't care if we did not delete any rows
    }
  }

  /**
   * return a list of all Item masters that have iDeleteAtZeroQuantity set to Yes, and
   * have no other database table dependencies.
   */
  public List<Map> getAllAutoDeletableItemMasters() throws DBException
  {
	  StringBuilder vpSql = new StringBuilder("SELECT * FROM itemMaster WHERE ")
			  .append( " (itemMaster.iDeleteAtZeroQuantity = " + DBConstants.YES )
    		  .append(") AND NOT EXISTS (SELECT sItem FROM LoadLineItem WHERE LoadLineItem.sItem = itemMaster.sItem)" )
    		  .append(" AND NOT EXISTS (SELECT sItem FROM OrderLine WHERE OrderLine.sItem = itemMaster.sItem)" )
    		  .append(" AND NOT EXISTS (SELECT sItem FROM Move WHERE Move.sItem = itemMaster.sItem)" )
    		  .append(" AND NOT EXISTS (SELECT sItem FROM DedicatedLocation WHERE DedicatedLocation.sItem = itemMaster.sItem)" )
    		  .append(" AND NOT EXISTS (SELECT sItem FROM Station WHERE Station.sItem = itemMaster.sItem)" )
    		  .append(" AND NOT EXISTS (SELECT sItem FROM PurchaseOrderLine WHERE PurchaseOrderLine.sItem = itemMaster.sItem)" );

      return fetchRecords(vpSql.toString());
  }
  
  
  /**
   * Retrun a list of all Synonyms without ItemMaster entries
   */
  public List<Map> getAllAutoDeletableSynonyms() throws DBException
  {
	  StringBuilder vpSql = new StringBuilder("SELECT * FROM Synonyms WHERE NOT EXISTS (SELECT sItem FROM ItemMaster WHERE Synonyms.sItem = itemMaster.sItem)");
      
      return fetchRecords(vpSql.toString());
  }
  
 /**
  *  Try to delete a specific Item master that has iDeleteAtZeroQuantity set to Yes,
  *  and has no other database table dependencies.
  *
  */
  public void deleteAutoDeletableItemMaster(String sItem) throws DBException
  {
    String s = "DELETE itemMaster WHERE" +
      " sItem = ?" +
      " AND (itemMaster.iDeleteAtZeroQuantity = " + DBConstants.YES +
      ") AND NOT EXISTS (SELECT sItem FROM LoadLineItem WHERE LoadLineItem.sItem = itemMaster.sItem)" +
      " AND NOT EXISTS (SELECT sItem FROM OrderLine WHERE OrderLine.sItem = itemMaster.sItem)" +
      " AND NOT EXISTS (SELECT sItem FROM Move WHERE Move.sItem = itemMaster.sItem)" +
      " AND NOT EXISTS (SELECT sItem FROM DedicatedLocation WHERE DedicatedLocation.sItem = itemMaster.sItem)" +
      " AND NOT EXISTS (SELECT sItem FROM Station WHERE Station.sItem = itemMaster.sItem)" +
      " AND NOT EXISTS (SELECT sItem FROM PurchaseOrderLine WHERE PurchaseOrderLine.sItem = itemMaster.sItem)";

    execute(s, sItem);

    // now cleanup any synonyms for this item master
    String s2 = "DELETE Synonyms WHERE NOT EXISTS (SELECT sItem FROM ItemMaster WHERE Synonyms.sItem = ?)";
    execute(s2, sItem);
  }

  /**
   * Try to change the item name. Changes name in item masters, load line items,
   * order lines, moves, stations, dedicated locations, purchase order lines,
   * and any where else the item name appears in the data base.
   *
   * @param isOldItemName - (String) The old name of the item
   * @param isNewItemName - (String) The new name of the item
   * @throws DBException
   */
  public void changeItemMasterName(String isOldItemName, String isNewItemName)
      throws DBException
  {
    String vsItemColumns = "";
    for (ItemMasterEnum vpIME : ItemMasterEnum.values())
    {
      if (vpIME != ItemMasterEnum.ITEM)
      {
        vsItemColumns += ", " + vpIME.getName();
      }
    }

    execute("INSERT into ItemMaster "
        + "(" + ItemMasterData.ITEM_NAME + vsItemColumns + ") "
        + "SELECT ?" + vsItemColumns + " FROM ItemMaster WHERE "
        + ItemMasterData.ITEM_NAME + " = ?", isNewItemName, isOldItemName);

    try
    {
      execute("UPDATE LoadLineItem SET sItem = ? WHERE sItem = ?",
          isNewItemName, isOldItemName);
    }
    catch (NoSuchElementException nsee) {};

    try
    {
      execute("UPDATE OrderLine SET sItem = ? WHERE sItem = ?",
          isNewItemName, isOldItemName);
    }
    catch (NoSuchElementException nsee) {};

    try
    {
      execute("UPDATE Move SET sItem = ? WHERE sItem = ?",
          isNewItemName, isOldItemName);
    }
    catch (NoSuchElementException nsee) {};

    try
    {
      execute("UPDATE DedicatedLocation SET sItem = ? WHERE sItem = ?",
          isNewItemName, isOldItemName);
    }
    catch (NoSuchElementException nsee) {};

    try
    {
      execute("UPDATE Station SET sItem = ? WHERE sItem = ?",
          isNewItemName, isOldItemName);
    }
    catch (NoSuchElementException nsee) {};

    try
    {
      execute("UPDATE PurchaseOrderLine SET sItem = ? WHERE sItem = ?",
          isNewItemName, isOldItemName);
    }
    catch (NoSuchElementException nsee) {};

    try
    {
      execute("UPDATE Synonyms SET sItem = ? WHERE sItem = ?",
          isNewItemName, isOldItemName);
    }
    catch (NoSuchElementException nsee) {};

    try
    {
      execute("DELETE ItemMaster WHERE sItem = ?", isOldItemName);
    }
    catch (NoSuchElementException nsee) {};
  }

  /**
   *  Method counts all loads not in NOMOVE and/or RETRIEVEPENDING state for a
   *  given order.
   *
   *  @param orderID <code>String</code> containing order number.
   *
   *  @return <code>int</code> containing count of loads
   */
  public int countMovingLoads(String orderID) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT COUNT(ld.sLoadID) AS \"rowCount\" FROM ")
        .append("Load ld, Move mv where mv.sOrderID = '")
        .append(orderID).append("' AND mv.sLoadID = ld.sLoadID AND ")
        .append("ld.iLoadMoveStatus NOT IN (")
        .append(DBConstants.NOMOVE).append(", ")
        .append(DBConstants.RETRIEVEPENDING).append(")");

    return getRecordCount(vpSql.toString(), "rowCount");
  }

 /**
  *  Method determines if employee is a SuperUser
  *
  *  @param empID <code>String</code> containing employee ID
  *
  *  @return <code>boolean</code> True id Super User
  */
  public boolean isEmployeeSuperUser(String empID) throws DBException
  {
    DBResultSet myDBResultSet = execute
      ("SELECT sUserId FROM Employee em, Role ro where em.sUserId = ? "
       + "AND em.sRole = ro.sRole AND ro.iRoleType = "
       + DBConstants.SUPER_USER, empID);
    if (myDBResultSet.getRowCount() > 0)
    {
        return(true);
    }
    else
    {
      return (false);
    }
  }

 /**
  *  Sets Objects for garbage collection.
  */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    ohdata    = null;
    subquery1 = null;
    subquery2 = null;
  }

  /**
   *  Get list of order ID's with a specific Type, Item, Lot etc.
   *
   *  @return <code>List</code> of Purchase Order Headers that match the
   *  criteria
   */
   public String[] getOrderChoices(OrderHeaderData pohdata, OrderLineData poldata,
                               String isAllOrNone) throws DBException
   {
     subquery1.setLength(0);
     subquery2.setLength(0);

     if (pohdata != null)
       subquery1.append(DBHelper.buildWhereClause(pohdata.getKeyArray(), "OH", true));
     if (poldata != null)
       subquery2.append(DBHelper.buildWhereClause(poldata.getKeyArray(), "OL", true));

     StringBuilder vpSql = new StringBuilder("SELECT DISTINCT OH.SORDERID ")
       .append("FROM ORDERHEADER OH, ORDERLINE OL ")
       .append("WHERE OH.SORDERID = OL.SORDERID");
     if (subquery1.length() > 1) // buildWhereClause may return a " "
     {
       vpSql.append(" AND ").append(subquery1);
     }
     if (subquery2.length() > 1) // buildWhereClause may return a " "
     {
       vpSql.append(" AND ").append(subquery2);
     }
     vpSql.append(" ORDER BY OH.SORDERID");

     return(getList(vpSql.toString(), OrderHeaderData.ORDERID_NAME,
                    isAllOrNone));
   }

  /**
   *  Get list of load ID's for a specific Order, and in the locations
   *  specified.
   *
   * @param order String orderID
   * @param lcdata LocationData with
   * @param isAllOrNone special string to add to list
   * @return String[] of loadID's
   * @throws DBException
   */
  public String[] getLoadChoices(String order, LocationData lcdata, String isAllOrNone)
      throws DBException
  {
    subquery1.setLength(0);

    if (!(lcdata == null))
    {
      subquery1.append(DBHelper.buildWhereClause(lcdata.getKeyArray(), "LC", true));
    }

    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT LD.SLOADID ")
      .append("FROM LOAD LD, LOCATION LC, LOADLINEITEM LLI ")
      .append("WHERE LLI.SLOADID = LD.SLOADID");
    if (order.length() > 1) // buildWhereClause may return a " "
    {
      vpSql.append(" AND LLI.SORDERID = '").append(order).append("'");
    }
    if (subquery1.length() > 1) // buildWhereClause may return a " "
    {
      vpSql.append(" AND LD.SWAREHOUSE = LC.SWAREHOUSE AND ")
        .append("LD.SADDRESS = LC.SADDRESS AND ").append(subquery1);
    }

    return getList(vpSql.toString(), LoadData.LOADID_NAME, isAllOrNone);
  }

 /**
  * Method to retrieve a list of Load records for a particular JVM ID.
  *
  * @param isJVMId the JVM Identifier
  * @param iapKeys Search keys.
  * @return List of Maps containing Load data.
  * @throws DBException for database errors.
  */
  public List<Map> getLoadDataListByJVM(String isJVMId, KeyObject[] iapKeys)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT ld.* FROM Load ld, Device dv ");

    // Rename key columns so they are not ambiguously defined
    // We can only do this with the local copy in case the key is used elsewhere.
    KeyObject[] vapKeys = new KeyObject[iapKeys.length];
    for (int i = 0; i < vapKeys.length; i++)
    {
      vapKeys[i] = (KeyObject)iapKeys[i].clone();
    }
    for (KeyObject k : vapKeys)
    {
      if (k.getColumnName().equals(LoadData.DEVICEID_NAME))
      {
        k.setColumnName("ld." + LoadData.DEVICEID_NAME);
      }
      else if (k.getColumnName().equals(LoadData.WAREHOUSE_NAME))
      {
        k.setColumnName("ld." + LoadData.WAREHOUSE_NAME);
      }
    }
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
    vpSql.append("ld.sDeviceID = dv.sDeviceID AND ")
               .append("dv.sJVMIdentifier = '").append(isJVMId).append("' ")
               .append("ORDER BY ld.sLoadID");

    return(fetchRecords(vpSql.toString()));
  }

 /**
  * Method gets a list of possible allocators that can allocate a given item.
  * @param isItem the item.
  * @return array of allocators. An empty array is returned when no allocators
  *         are found.
  * @throws DBException if there is a database access error.
  */
  public String[] getAllocatorChoices(String isItem) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT dv.sAllocatorName FROM ")
               .append("Load ld, LoadLineItem id, Device dv WHERE ")
               .append("ld.sLoadID = id.sLoadID AND ")
               .append("dv.sDeviceID = ld.sDeviceID AND ")
               .append("sItem = ?");

    return getList(vpSql.toString(), DeviceData.ALLOCATORNAME_NAME,
                   SKDCConstants.NO_PREPENDER, isItem);
  }

 /**
  * Method gets a list of possible allocators that can allocate for a given
  * range of locations in the same warehouse.
  * @param isWarehouse the location warehouse.
  * @param isBeginAddress first location address in range.
  * @param isEndAddress ending location address in range.
  * @return array of allocators. An empty array is returned when no allocators
  *         are found.
  * @throws DBException if there is a database access error.
  */
  public String[] getAllocatorChoices(String isWarehouse, String isBeginAddress,
                                      String isEndAddress) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT dv.sAllocatorName FROM ")
               .append("Load ld, Location lc, Device dv WHERE ")
               .append("lc.sWarehouse = ld.sWarehouse AND ")
               .append("lc.sAddress = ld.sAddress AND ")
               .append("lc.sDeviceID = ld.sDeviceID AND ")
               .append("dv.sDeviceID = lc.sDeviceID AND ")
               .append("ld.sWarehouse = ? AND ")
               .append("ld.sAddress BETWEEN ? AND ?");

    return getList(vpSql.toString(), DeviceData.ALLOCATORNAME_NAME,
                   SKDCConstants.NO_PREPENDER, isWarehouse, isBeginAddress,
                   isEndAddress);
  }

 /**
  * Method to get all accessible routes for a load allowing it to get to a
  * Cycle-Count station.
  *
  * @param isLoadID the Load for which the output station(s) is retrieved.
  * @return array of possible routes for this load.
  * @throws DBException if there is a database access error.
  */
  public String[] getCCIRoutes(String isLoadID) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT rt.sRouteID FROM ")
               .append("Load ld, Device dv, Station st, Route rt WHERE ")
               .append("dv.sDeviceID = ld.sDeviceID AND ")
//               .append("dv.sDeviceID = st.sDeviceID AND ") this prohibits STV use!
               .append("dv.sDeviceID = rt.sFromID AND ")
               .append("st.sStationName=rt.sDestID AND ")
               .append("rt.iFromType = ").append(DBConstants.EQUIPMENT).append(" AND ")
               .append("rt.iRouteOnOff = ").append(DBConstants.ON).append(" AND ")
               .append("st.iCCIAllowed = ").append(DBConstants.YES).append(" AND ")
	           .append("ld.sLoadID = ? ORDER BY rt.sRouteID");

    return getList(vpSql.toString(), RouteData.ROUTEID_NAME,
                   SKDCConstants.NO_PREPENDER, isLoadID);
//    String[] vasRoutes = getList(vpSql.toString(), RouteData.ROUTEID_NAME,
//                                 SKDCConstants.NO_PREPENDER, isLoadID);
//    return((vasRoutes.length > 0) ? vasRoutes[0] : "");
  }

 /*==========================================================================
                      Private Methods go in this section.
  ==========================================================================*/
 /**
  *  Gets partially empty containers by matching aisle group and item.
  *
  *  @param aisleGroup <code>int</code> Aisle group to check against.
  *  @param olData <code>OrderLineData</code> containing item and lot that must
  *         be in the container we find.
  *
  *  @return List of Empty <code>LoadData</code> objects.
  */
  public List<Map> getLikeItemsContainer(int aisleGroup, int amtFullTranslation,
                                          OrderLineData olData) throws DBException
  {
    String vsLocTypeList = DBConstants.LCASRS + "," + DBConstants.LCCONVSTORAGE;

    ItemMasterData vpIMData = Factory.create(ItemMaster.class).getItemMasterData(olData.getItem());
    if (vpIMData == null)
    {
      throw new DBException("Item Master not found for \"" + olData.getItem() + "\"");
    }

                                       // First get all loads that belong to
                                       // this aisle group, and item.
    StringBuilder vpSql = new StringBuilder("SELECT ld.sLoadID AS \"SLOADID\" FROM ")
               .append("Location lc, Load ld, LoadLineItem id WHERE ")
               .append("ld.iHeight >= ").append(olData.getHeight()).append(" AND ")
               .append("ld.sWarehouse = lc.sWarehouse AND ")
               .append("ld.sAddress = lc.sAddress AND ")
               .append("ld.iAmountFull <= ").append(amtFullTranslation).append(" AND ")
               .append("iAmountFull > ").append(DBConstants.EMPTY).append(" AND ")
               .append("ld.sContainerType = '").append(olData.getContainerType()).append("' AND ")
               .append("ld.iLoadMoveStatus = ").append(DBConstants.NOMOVE).append(" AND ")
               .append("lc.iLocationStatus = ").append(DBConstants.LCAVAIL).append(" AND ")
               .append("lc.iLocationType in (" + vsLocTypeList + ") AND ")
               .append("lc.iAisleGroup = ").append(aisleGroup).append(" AND ")
               .append("ld.sLoadID = id.sLoadID AND ")
               .append("id.sItem = '").append(olData.getItem()).append("' ");
    if (olData.getOrderLot().trim().length() == 0)
    {
      if (vpIMData.getStorageFlag() != DBConstants.MIXALL &&
          vpIMData.getStorageFlag() != DBConstants.MIXLOTS_ONEITEM)
      {
        vpSql.append("AND id.sLot IS NULL ");
      }
    }
    else
    {
      vpSql.append("AND id.sLot = '").append(olData.getOrderLot()).append("' ");
    }

    List<Map> ldList = fetchRecords(vpSql.toString());
                                       // Likely failed because there was no
                                       // matching lot. This is ok since we want
                                       // the exact lot number.
    if (ldList.isEmpty())
    {
      return(ldList);
    }
                                       // See if the item load we found is a
                                       // sub-load for a container.
    String itemDetLoad = "";
    String superLoad   = "";
    Load ld = Factory.create(Load.class);
    Map<String, String> ldHash = new HashMap<String, String>(30);
    StringBuffer ldSQLString = new StringBuffer();

    for(int k = 0; k < ldList.size(); k++)
    {
      itemDetLoad = DBHelper.getStringField(ldList.get(k), LoadData.LOADID_NAME);

      superLoad = ld.getTopLevelLoad(itemDetLoad);
      if (superLoad == null) superLoad = itemDetLoad;
                                       // Assemble a unique list of loads that
                                       // are the top-most level loads.
      if (ldHash.containsKey(superLoad) == false)
      {
        ldHash.put(superLoad, superLoad);
        if (ldSQLString.toString().trim().length() > 0)
        {
          ldSQLString.append(", ");
        }
        ldSQLString.append("'").append(superLoad).append("'");
      }
    }

    // Find complete super load records now.
    vpSql.setLength(0);
    vpSql.append("SELECT * FROM Load WHERE sLoadID IN (")
               .append(ldSQLString).append(") ORDER BY iAmountFull DESC");

    return fetchRecords(vpSql.toString());
  }

  /**
   * Log an allocation probe message
   *
   * @param sMethodName
   * @param sDiagMessage
   */
  private void logDiagnostic(String sMethodName, String sDiagMessage)
  {
    if (allocDiag != null)
      allocDiag.addProbeDetails(sMethodName, sDiagMessage);
  }

  /**
   * Does routing to this station require passing through an AGC transfer
   * station?
   *
   * @param isStation
   * @return
   * @throws DBException
   */
  @UnusedMethod
  public boolean doesStationNeedAGCTransfer(String isStation) throws DBException
  {
    return fetchRecords(AGC_TRANSFER_SQL, isStation).size() > 0;
  }

  /**
   * Get loads moving to (or from?) an AGC transfer station.
   *
   * TODO: Does this even work?  The Station table has no join field!
   *
   * @param isStation
   * @return
   * @throws DBException
   */
  public String[] getLoadsForAGCTransferStation(String isStation) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT LOAD.* FROM STATION, ROUTE, LOAD")
        .append(" WHERE iStationType = ").append(DBConstants.AGC_TRANSFER)
        .append(" AND sFromID = ?")
        .append(" AND sNextAddress = sDestID AND iLoadMoveStatus = ")
        .append(DBConstants.MOVING).append(" ORDER BY dMoveDate");
    return getList(vpSql.toString(), LoadData.LOADID_NAME,
        SKDCConstants.NO_PREPENDER, isStation);
  }

 /**
  * Method gets the stations attached to a particular JVM.
  * @param iapStationTypes array of station types.
  * @param isJVMId the JVM identifier for this instance wrx-j.
  * @param isFirstElement
  * @return
  * @throws DBException if there is a DB access error.
  */
  public Map<String, String> getStationsByStationType(int[] iapStationTypes,
                                                      final String isJVMId,
                                                      final String isFirstElement)
         throws DBException
  {
    String vsCombinedColName = "SSTATIONNAMEDESC";
    
    StringBuilder vpSql = new StringBuilder("SELECT st.sStationName, ")
               .append("CONCAT(st.sStationName, CONCAT(' - ', st.sDescription)) AS ")
               .append("\"").append(vsCombinedColName).append("\"")
               .append("  FROM Station st, Location lc, Device dv")
               .append(" WHERE st.sWarehouse = lc.sWarehouse")
               .append("   AND st.sStationName = lc.sAddress")
               .append("   AND dv.sDeviceID = lc.sDeviceID")
               .append("   AND dv.sJVMIdentifier = '").append(isJVMId).append("' ");

    if (iapStationTypes.length > 0)
    {
      vpSql.append("   AND st.iStationType IN (");
      for (int i = 0; i < iapStationTypes.length; i++)
      {
        if (i != 0)
        {
          vpSql.append(", ");
        }
        vpSql.append(iapStationTypes[i]);
      }
      vpSql.append(") ");
    }

    vpSql.append("ORDER BY st.sStationName");
    List<Map> vpList = fetchRecords(vpSql.toString());
    Map<String, String> vpStationMap = new LinkedHashMap<String, String>();

    if (isFirstElement.equals(SKDCConstants.ALL_STRING) ||
        isFirstElement.equals(SKDCConstants.NONE_STRING))
    {
      vpStationMap.put(isFirstElement, isFirstElement);
    }
    else if (isFirstElement.equals(SKDCConstants.EMPTY_VALUE))
    {
      vpStationMap.put("", "");
    }

    if (vpList.isEmpty())
    {
      if (vpStationMap.isEmpty())
        vpStationMap.put(SKDCConstants.NONE_STRING, SKDCConstants.NONE_STRING);
    }
    else
    {
      for(Map vpMap : vpList)
      {
        String vsStationNameDesc = DBHelper.getStringField(vpMap, vsCombinedColName);
        String vsStationName = DBHelper.getStringField(vpMap, StationData.STATIONNAME_NAME);
        vpStationMap.put(vsStationNameDesc, vsStationName);
      }
    }

    return(vpStationMap);
  }

  /**
   * Get all of the item details in available locations for a warehouse
   *
   * @param isItem
   * @param isLot
   * @param isWarehouse
   * @return
   * @throws DBException
   */
  public List<Map> getItemDetailsInWarehouse(String isItem, String isLot,
      String isWarehouse) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT id.* FROM ")
             .append("Load ld, LoadLineItem id, Location lc WHERE ")
             .append("lc.sWarehouse = ld.sWarehouse AND ")
             .append("ld.sLoadID = id.sLoadID AND ")
             .append("iLocationStatus = ").append(DBConstants.LCAVAIL)
             .append(" AND lc.sWarehouse = ?")
             .append(" AND id.sItem = ?");
    if (isLot.length() > 0)
      vpSql.append(" AND id.sLot = '").append(isLot).append("'");
    return fetchRecords(vpSql.toString(), isWarehouse, isItem);
  }

 /**
  * Indicates if this station is part of a split system.
  * @param isStation the station ID.
  * @return <code>true</code> if station is part of a split system.
  * @throws DBException if there is a DB access error.
  */
  public boolean isStationPartOfAnySplitSystem(String isStation) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT COUNT(jvm.sJVMIdentifier) AS \"iJVMCount\" FROM ")
               .append("Device dv, Station st, JVMConfig jvm WHERE ")
               .append("st.sStationName = '").append(isStation).append("' AND ")
               .append("dv.sDeviceID = st.sDeviceID AND ")
               .append("jvm.sJVMIdentifier = dv.sJVMIdentifier ");

    return(getRecordCount(vpSql.toString(), "iJVMCount") > 0);
  }

 /**
  * Indicates if this station belongs to current JVM in a split system.
  * @param isStation the station ID.
  * @return <code>true</code> if station is part of current split system.
  * @throws DBException if there is a DB access error.
  */
  public boolean isStationPartOfThisSplitSystem(String isStation) throws DBException
  {
    String vsJVMId = Application.getString(SKDCConstants.JVM_IDENTIFIER_KEY);

    StringBuilder vpSql = new StringBuilder("SELECT COUNT(jvm.sJVMIdentifier) AS \"iJVMCount\" FROM ")
               .append("Device dv, Station st, JVMConfig jvm WHERE ")
               .append("dv.sDeviceID = st.sDeviceID AND ")
               .append("jvm.sJVMIdentifier = dv.sJVMIdentifier AND ")
               .append("st.sStationName = '").append(isStation).append("' AND ")
               .append("jvm.sJVMIdentifier = '").append(vsJVMId).append("' ");

    return(getRecordCount(vpSql.toString(), "iJVMCount") > 0);
  }

 /**
  * Indicates if this device is part of a split system.
  * @param isDeviceID the device ID.
  * @return <code>true</code> if device is part of a split system.
  * @throws DBException if there is a DB access error.
  */
  public boolean isDevicePartOfAnySplitSystem(String isDeviceID) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT COUNT(jvm.sJVMIdentifier) AS \"iJVMCount\" FROM ")
               .append("Device dv, JVMConfig jvm WHERE ")
               .append("dv.sDeviceID = '").append(isDeviceID).append("' AND ")
               .append("jvm.sJVMIdentifier = dv.sJVMIdentifier ");

    return(getRecordCount(vpSql.toString(), "iJVMCount") > 0);
  }

 /**
  * Indicates if this device is part of this split system.
  * @param isDeviceID the device ID.
  * @return <code>true</code> if device is part of a split system.
  * @throws DBException if there is a DB access error.
  */
  public boolean isDevicePartOfThisSplitSystem(String isDeviceID) throws DBException
  {
    String vsJVMId = Application.getString(SKDCConstants.JVM_IDENTIFIER_KEY);

    StringBuilder vpSql = new StringBuilder("SELECT COUNT(jvm.sJVMIdentifier) AS \"iJVMCount\" FROM ")
               .append("Device dv, JVMConfig jvm WHERE ")
               .append("jvm.sJVMIdentifier = dv.sJVMIdentifier AND ")
               .append("dv.sDeviceID = '").append(isDeviceID).append("' AND ")
               .append("jvm.sJVMIdentifier = '").append(vsJVMId).append("' ");

    return(getRecordCount(vpSql.toString(), "iJVMCount") > 0);
  }

 /**
  * Gets the configuration of the current split system.
  *
  * @param isWarehouse The logical warehouse that will have different JVMs running.
  *                    This may be a super warehouse as well.
  * @return List of Maps containing combined data from the JVMConfig table and
  * Device table.
  * @throws DBException if there is a database access error.
  */
  public List<Map> getCurrentSplitSystemConfig(String isWarehouse) throws DBException
  {
    String vsWhsClause;
    Warehouse vpWhs = Factory.create(Warehouse.class);

    if (vpWhs.isSuperWarehouse(isWarehouse))
      vsWhsClause = "whs.sSuperWarehouse = ? ";
    else
      vsWhsClause = "whs.sWarehouse = ? ";

    StringBuilder vpSql = new StringBuilder("SELECT jvm.sJVMIdentifier, jvm.sJMSTopic, ")
               .append("dv.sDeviceID, jvm.sServerName, ")
               .append("dv.sSchedulerName, dv.sAllocatorName ")
               .append("FROM Device dv, JVMConfig jvm, Warehouse whs WHERE ")
               .append("jvm.sJVMIdentifier = dv.sJVMIdentifier AND ")
               .append("whs.sWarehouse = dv.sWarehouse AND ")
               .append(vsWhsClause).append("ORDER BY dv.sDeviceID");

    return(fetchRecords(vpSql.toString(), isWarehouse));
  }

 /**
  * Method to get the count of JVM's assigned to a warehouse.
  * @param isWarehouse the warehouse (may be super warehouse).
  * @return count of the number of JVMs in the given warehouse
  * @throws DBException if there is a database error.
  */
  public int getJVMCountPerWarehouse(String isWarehouse) throws DBException
  {
    String vsWhsClause;
    Warehouse vpWhs = Factory.create(Warehouse.class);

    if (vpWhs.isSuperWarehouse(isWarehouse))
      vsWhsClause = "whs.sSuperWarehouse = ?";
    else
      vsWhsClause = "whs.sWarehouse = ?";

    StringBuilder vpSql = new StringBuilder("SELECT COUNT(DISTINCT jvm.sJVMIdentifier) AS \"rowCount\" FROM ")
               .append("Device dv, JVMConfig jvm, Warehouse whs WHERE ")
               .append("jvm.sJVMIdentifier = dv.sJVMIdentifier AND ")
               .append("whs.sWarehouse = dv.sWarehouse AND ")
               .append(vsWhsClause);

    return(getRecordCount(vpSql.toString(), "rowCount", isWarehouse));
  }

 /**
   * Method gets all routes involving AGV routes in the system.
   * @return
   * @throws DBException
   */
  public String[] getAGVRouteLinks() throws DBException
  {
    final String SELECT_COLUMN = "SAGVROUTE";

    StringBuilder vpSql = new StringBuilder("SELECT CONCAT(CONCAT(rt.sFromID, '-'), rt.sDestID) AS ").append(SELECT_COLUMN).append(" ")
               .append("FROM Route rt, Station st WHERE ")
               .append("st.sStationName = rt.sRouteID AND ")
               .append("st.iStationType = ").append(DBConstants.AGV_STATION).append(" AND ")
               .append("rt.iFromType != ").append(DBConstants.EQUIPMENT).append(" AND ")
               .append("rt.iDestType != ").append(DBConstants.EQUIPMENT).append(" ")
               .append("ORDER BY rt.sFromID, rt.sDestID");

    return(getList(vpSql.toString(), SELECT_COLUMN,
                   SKDCConstants.NO_PREPENDER));
  }
  
  /**
	 * Retrieve all retrievable bags of loadlineitem table by Flight number
	 * @param lotId
	 * @return
	 * @throws DBException
	 */
  public List<LoadLineItemData> getAllRetrievableLoadLineItemsForThisFlight(String lotId) throws DBException {
  	 StringBuilder queryBuf = new StringBuilder();
       queryBuf.append(" SELECT  lli.*");
       queryBuf.append(" FROM LOADLINEITEM AS lli");
       
       queryBuf.append(" INNER JOIN LOAD AS ld");
       queryBuf.append(" ON ld.sLoadID = lli.sLoadID");
       queryBuf.append(" AND ld.iLoadMoveStatus = ").append(DBConstants.NOMOVE);

       queryBuf.append(" INNER JOIN LOCATION AS lc");
       queryBuf.append(" ON lc.sAddress = ld.sAddress");
       queryBuf.append(" AND lc.sDeviceID = ld.sDeviceID");
       queryBuf.append(" AND lc.sWarehouse = ld.sWarehouse");
       queryBuf.append(" AND lc.iLocationStatus = ").append(DBConstants.LCAVAIL);
      //queryBuf.append("        AND lc.iEmptyFlag = ").append(DBConstants.OCCUPIED);
  
       queryBuf.append(" INNER JOIN DEVICE AS dv");
       queryBuf.append("         ON dv.sDeviceID = ld.sDeviceID");
       queryBuf.append("        AND dv.iOperationalStatus != ").append(DBConstants.INOP);
       queryBuf.append("      WHERE lli.iHoldType = ").append(DBConstants.ITMAVAIL);
       queryBuf.append("        AND lli.sLot = '").append(lotId).append("'");
       queryBuf.append("   ORDER BY ld.sAddress ASC");
       
       List<Map> results = fetchRecords(queryBuf.toString());
       if (results == null || results.isEmpty()) {
           return new ArrayList<LoadLineItemData>();
       }

       List<LoadLineItemData> loadLineItemDataList = results.stream().map(row -> {
           LoadLineItemData liToReturn = Factory.create(LoadLineItemData.class);
           liToReturn.dataToSKDCData(row);
           return liToReturn;
       }).collect(Collectors.toList());

       return loadLineItemDataList;
  }
  /**
	 * Retrieve all retrievable bag of loadlineitem table by tray id
	 * @param lotId
	 * @return
	 * @throws DBException
	 */
 public List<LoadLineItemData> getAllRetrievableLoadLineItemsForThisTray(String trayId) throws DBException {
	 StringBuilder queryBuf = new StringBuilder();
     queryBuf.append(" SELECT  lli.*");
     queryBuf.append(" FROM LOADLINEITEM AS lli");
     
     queryBuf.append(" INNER JOIN LOAD AS ld");
     queryBuf.append(" ON ld.sLoadID = lli.sLoadID");
     queryBuf.append(" AND ld.iLoadMoveStatus = ").append(DBConstants.NOMOVE);

     queryBuf.append(" INNER JOIN LOCATION AS lc");
     queryBuf.append(" ON lc.sAddress = ld.sAddress");
     queryBuf.append(" AND lc.sDeviceID = ld.sDeviceID");
     queryBuf.append(" AND lc.sWarehouse = ld.sWarehouse");
     queryBuf.append(" AND lc.iLocationStatus = ").append(DBConstants.LCAVAIL);

     queryBuf.append(" INNER JOIN DEVICE AS dv");
     queryBuf.append("         ON dv.sDeviceID = ld.sDeviceID");
     queryBuf.append("        AND dv.iOperationalStatus != ").append(DBConstants.INOP);
     queryBuf.append("      WHERE lli.iHoldType = ").append(DBConstants.ITMAVAIL);
     queryBuf.append("        AND lli.sLoadID = '").append(trayId).append("'");
     queryBuf.append("   ORDER BY ld.sAddress ASC");
     
     List<Map> results = fetchRecords(queryBuf.toString());
     if (results == null || results.isEmpty()) {
         return new ArrayList<LoadLineItemData>();
     }

     List<LoadLineItemData> loadLineItemDataList = results.stream().map(row -> {
         LoadLineItemData liToReturn = Factory.create(LoadLineItemData.class);
         liToReturn.dataToSKDCData(row);
         return liToReturn;
     }).collect(Collectors.toList());

     return loadLineItemDataList;
}
}
