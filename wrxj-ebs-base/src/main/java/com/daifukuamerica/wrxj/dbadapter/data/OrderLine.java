package com.daifukuamerica.wrxj.dbadapter.data;

/*
                    Daifuku America Corporation
                       International Center
                   5202 Douglas Corrigan Way
                Salt Lake City, Utah  84116-3192
                        (801) 359-9900

   This software is furnished under a license and may be used and copied only 
   in accordance with the terms of such license. This software or any other 
   copies thereof in any form, may not be provided or otherwise made available, 
   to any other person or company without written consent from Daifuku America 
   Corporation.

   Daifuku America Corporation assumes no responsibility for the use or 
   reliability of software which has been modified without approval.
*/

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import java.util.List;
import java.util.Map;

/**
 * Description:<BR>
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 30-May-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  Daifuku America Corporation
 */
public class OrderLine extends BaseDBInterface
{
  private OrderLineData mpOLData;

  /**
   * Constructor
   */
  public OrderLine()
  {
    super("OrderLine");
    mpOLData = Factory.create(OrderLineData.class);
  }

  /**
   * Get the next line ID
   * @param isOrderID
   * @return
   * @throws DBException
   */
  public String getNextLineID(String isOrderID) throws DBException
  {
    mpOLData.clear();
    mpOLData.setKey(OrderLineData.ORDERID_NAME, isOrderID);
    int vnLineCount = getCount(mpOLData);

    return Integer.toString(++vnLineCount);
  }
  
  /**
   * Method to see if any lines on this order are shy. This method should only
   * be used when we have presumably tried to allocate an order and sometime
   * later we wish to see if anything was allocated and how much.
   * 
   * @param orderID <code>String</code> containing search order.
   * 
   * @return false if any lines exist that are not fully allocated.
   */
  public boolean isOrderFullyAllocated(String orderID) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT COUNT(sOrderID) AS \"rowCount\" FROM OrderLine ")
             .append("WHERE sOrderID = '").append(orderID).append("' AND ")
             .append("fAllocatedQuantity < fOrderQuantity");
             
    return getRecordCount(vpSql.toString(), "rowCount") == 0;
  }
  
  /**
   *  Method to see if any lines on this order still have outstanding picks
   *  @param orderID <code>String</code> containing search order.
   *
   * @return StringBuffer Order IDs.
   */
  public boolean hasRemainingPicks(String orderID) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT COUNT(sOrderID) AS \"rowCount\" FROM OrderLine ")
      .append("WHERE sOrderID = '").append(orderID).append("' AND ")
      .append("fOrderQuantity > fPickQuantity");

    return getRecordCount(vpSql.toString(), "rowCount") > 0;
  }

  /**
   * Method to modify order line short flag.
   * 
   * @param order <code>String</code> containing order id.
   * @param item <code>String</code> containing item.
   * @param ordlot <code>String</code> containing lot being ordered.
   * @param iLineShy <code>int</code> indicating if the line is short.
   * 
   * @throws DBException if there is a database error.
   */
  public void setLineShyValue(String order, String item, String ordlot,
                              int iLineShy) throws DBException
  {
    mpOLData.clear();
    mpOLData.setKey(OrderLineData.ORDERID_NAME, order);
    mpOLData.setKey(OrderLineData.ITEM_NAME, item);
    mpOLData.setKey(OrderLineData.ORDERLOT_NAME, ordlot);
    mpOLData.setLineShy(iLineShy);
    modifyElement(mpOLData);
  }

 /**
  *  Calculates total amount required to fill all orders containing a
  *  given item on the system.
  *
  *  @param  item <code>String</code> containing item for this calculation.
  *  @return <code>double</code> value containing sum total of ordered quantites
  *          minus sum total of allocated quantities.
  */
  public double getTotalLineFillQuantity(String item) throws DBException
  {
    double totalOrderedQuantites = 0;
    double totalAllocatedQuantites = 0;
    double fillableAmount = 0;
                                       // Make sure order lines exist with this
                                       // item atleast.
    StringBuilder vpSql = new StringBuilder("SELECT sItem FROM OrderLine WHERE ")
             .append("sItem = '").append(item).append("'");
    List<Map> aList = fetchRecords(vpSql.toString());

    if (aList.size() > 0)
    {
      vpSql.setLength(0);
      vpSql.append("SELECT SUM(fOrderQuantity) AS \"fTotalOrderedQty\", ")
               .append("SUM(fAllocatedQuantity) AS \"fTotalAllocatedQty\" ")
               .append("FROM OrderLine WHERE ")
               .append("sItem = '").append(item).append("'");
      aList = fetchRecords(vpSql.toString());
  
      if (aList.size() > 0)
      {
        totalOrderedQuantites = DBHelper.getDoubleField(aList.get(0),
                                                       "fTotalOrderedQty");
        totalAllocatedQuantites = DBHelper.getDoubleField(aList.get(0),
                                                        "fTotalAllocatedQty");
        fillableAmount = totalOrderedQuantites - totalAllocatedQuantites;
      }
    }

    return fillableAmount;
  }

/*---------------------------------------------------------------------------
               METHODS FOR REPLENISHMENTS AND CYCLE-COUNTING.
  ---------------------------------------------------------------------------*/
 /**
  *  Makes sure at least one location in the range given exists.  If it does
  *  the allocator will build Maintenance Order moves for those locations that
  *  are valid.
  *  @param mntData <code>AbstractSKDCData</code> containing search info.
  */
  public void validateLocationRange(OrderLineData mntData) throws DBException
  {
    String beginLocation = mntData.getBeginLocation();
    String endingLocation = mntData.getEndingLocation();

    if (beginLocation.compareTo(endingLocation) > 0)
    {
      throw new DBException("Ending address must be greater than starting address!");
    }
    
    mntData.setKey(OrderLineData.BEGINWAREHOUSE_NAME, mntData.getBeginWarehouse());
    mntData.setKey(OrderLineData.BEGINADDRESS_NAME, mntData.getBeginAddress());
    mntData.setKey(OrderLineData.ENDINGWAREHOUSE_NAME, mntData.getEndingWarehouse());
    mntData.setKey(OrderLineData.ENDINGADDRESS_NAME, mntData.getEndingAddress());
    if (exists(mntData))
    {
      throw new DBException("Cycle Count Order already\nexists for this Location Range!");
    }

/*--------------------------------------------------------------------------
   Do a quick check for occupied locations in the range specified.  If there are
   no occupied locations in this range, reject the request.  In the case of
   Cycle-Counts, we should have at least one load to count in the range given.
   In the case of Replenishment orders, we should have dedicated loads at the
   locations.
  --------------------------------------------------------------------------*/
    Location vpLoc = Factory.create(Location.class);
    if (!vpLoc.isCycleCountLocation(beginLocation))
    {
      throw new DBException("Cycle Count not allowed\nat Beginning Location Type!");
    }
    
    if (!vpLoc.isCycleCountLocation(endingLocation))
    {
      throw new DBException("Cycle Count not allowed\nat Ending Location Type!");
    }
    
    LoadData vpLoadData;
    vpLoadData = Factory.create(LoadData.class);
    
    String[] begLocn = Location.parseLocation(beginLocation);
    String[] endLocn = Location.parseLocation(endingLocation);

    vpLoadData.setKey(LocationData.WAREHOUSE_NAME, begLocn[0]);
    vpLoadData.setBetweenKey(LocationData.ADDRESS_NAME, begLocn[1], endLocn[1]);

    if (Factory.create(Load.class).getCount(vpLoadData) == 0)
    {
      throw new DBException("Specified Location Range has\nno Loads!");
    }
  }

 /**
  *  Method does validates that another Cycle Count order with this order's
  *  item-lot combination does not exist.
  *
  *  @param mntData <code>AbstractSKDCData</code> containing search info.
  */
  public void validateItemLotMaintenanceOrder(OrderLineData mntData, int iniOrderType)
         throws DBException
  {
    String itemID = mntData.getItem();
    String lot = mntData.getOrderLot();

    ItemMaster itemMast = Factory.create(ItemMaster.class);
    if (!itemMast.exists(itemID))
    {
      throw new DBException("Item " + itemID + " does not exist!");
    }
                                       // If there is already a Maintenance Order
                                       // with this item/lot combination
                                       // reject the request for a new one.
    StringBuilder vpSql = new StringBuilder("SELECT COUNT(*) AS \"COUNT\" from orderheader oh, orderline ol WHERE " +
        "oh.sOrderID = ol.sOrderID AND oh.iordertype = " + iniOrderType + 
        " AND ol.sItem = '" + itemID + "'" +
        " AND ol.sOrderLot = '" + lot + "'" );

    int tmpcount = getIntegerColumn("COUNT", vpSql.toString());
    
    if(tmpcount > 0)
    {
      throw new DBException("Maintenance Order already\nexists for this item/lot!");
    }
  }

 /**
  * Retrieves one Maintenance Order record using unique key.
  *
  * @param ipOLKey <code>OrderLineData</code> object.  This object should
  *        have Key information in it already.
  * @param inWithLock integer whose value is WRITELOCK or NOWRITELOCK.
  *
  * @return reference to OrderLineData object containing current record.
  *         <code>null</code> if no orders found.
  *
  * @exception DBException
  */
  public OrderLineData getMaintenanceOrderLineData(OrderLineData ipOLKey,
      int inWithLock) throws DBException
  {
    ipOLKey.mapLocationKey(OrderLineData.BEGINLOCATION_NAME);
    ipOLKey.mapLocationKey(OrderLineData.ENDINGLOCATION_NAME);

    StringBuilder vpSql = new StringBuilder(getMaintenanceLineQuery())
             .append(DBHelper.buildWhereClause(ipOLKey.getKeyArray()));
    if (inWithLock == DBConstants.WRITELOCK)
    {
      vpSql.append(" FOR UPDATE");
    }

    OrderLineData vpResultData = null;
    List<Map> vpResultList = fetchRecords(vpSql.toString());

    if (vpResultList.size() > 0)
    {
      if (vpResultList.size() > 1)
      {
        throw new DBException("More than one record returned for a "
            + "single line query!  Non-Unique key specified...");
      }

      // Turn DB Data into Data class format.
      mpOLData.dataToSKDCData(vpResultList.get(0));
      vpResultData = (OrderLineData)mpOLData.clone();
      vpResultList = null;
    }

    return vpResultData;
  }

 /**
  * Method to add a row of data for a Replenishment or Cycle-Count order.
  *
  * @param  olData <code>OrderLineData</code> object containing columns
  *         to add.
  * @exception DBException
  */
  public void addMaintenanceOrderLine(OrderLineData olData, int iniOrderType)
         throws DBException
  {
    if (iniOrderType == DBConstants.CYCLECOUNT)
    {
      switch(OrderLine.getCycleCountOrderCategory(olData))
      {
        case OrderLineData.CC_ITEM:
        case OrderLineData.CC_ITEM_LOT:
        case OrderLineData.CC_ITEM_LOT_WHS:
        case OrderLineData.CC_ITEM_WHS:
        case OrderLineData.REPL_ITEM:
          validateItemLotMaintenanceOrder(olData, iniOrderType);
          break;

        case OrderLineData.CC_LOCATION:
        case OrderLineData.REPL_LOCATION:
          validateLocationRange(olData);
          break;
      
        default:
          throw new DBException("Unknown Maintenance Order Type ...");
      }
    }

    olData.mapLocationColumn(OrderLineData.BEGINLOCATION_NAME);
    olData.mapLocationColumn(OrderLineData.ENDINGLOCATION_NAME);
    addElement(olData);
  }

 /**
  * Method to delete a row of data.
  *
  * @param olData <code>OrderLineData</code> containing Key info. for the delete.
  * @exception DBException if there are errors during deletion.
  */
  public void deleteMaintenanceOrder(OrderLineData olData)
         throws DBException
  {
    olData.mapLocationKey(OrderLineData.BEGINLOCATION_NAME);
    olData.mapLocationKey(OrderLineData.ENDINGLOCATION_NAME);
    deleteElement(olData);
  }

 /**
  * Method checks if a Cycle-Count order is by location, item, or warehouse.
  * @param olData The Cycle Count Order Line Data.
  * @return one of the following constants:
  * <CENTER>
  *    <TABLE border=1>
  *    <TR>
  *       <TH BGCOLOR = '#CCFFFF'>Cycle-Count Category Constant</TH>
  *       <TH BGCOLOR = '#CCFFFF'>Definition</TH>
  *    </TR>
  *    <TR>
  *       <TD ALIGN = 'CENTER'>OrderLine.CC_ITEM</TD>
  *       <TD ALIGN = 'CENTER'>Cycle Count by item.</TD>
  *    </TR>
  *    <TR>
  *       <TD ALIGN = 'CENTER'>OrderLineData.CC_ITEM_LOT</TD>
  *       <TD ALIGN = 'CENTER'>Cycle-Count by item and lot.</TD>
  *    </TR>
  *    <TR>
  *       <TD ALIGN = 'CENTER'>OrderLineData.CC_LOCATION</TD>
  *       <TD ALIGN = 'CENTER'>Cycle-Count by Location Range.</TD>
  *    </TR>
  *    </TABLE>
  * </CENTER>
  */
  public static int getCycleCountOrderCategory(OrderLineData olData)
  {
    int rtn = -1;
    String ordPrefix = olData.getOrderID().substring(0, 3);
    int itemLen = olData.getItem().trim().length();
    int lotLen = olData.getOrderLot().trim().length();
    int locnLen = olData.getBeginLocation().trim().length();
    int whsLen = olData.getWarehouse().trim().length();

    if (ordPrefix.equals(OrderHeaderData.CYCLECOUNT_PREFIX))
    {
      if(itemLen != 0)
      {
        if(lotLen != 0)
        {
          if(whsLen != 0)
            rtn = OrderLineData.CC_ITEM_LOT_WHS;
          else
            rtn = OrderLineData.CC_ITEM_LOT;
        }
        else
        {
          if(whsLen != 0)
            rtn = OrderLineData.CC_ITEM_WHS;
          else
            rtn = OrderLineData.CC_ITEM;
        }
      }
      else if (locnLen != 0)
      {
        rtn = OrderLineData.CC_LOCATION;
      }
    }
    return rtn;
  }

  /**
   * Get a query for maintenance order lines
   * @return
   */
  private String getMaintenanceLineQuery()
  {
    String mntQuery = "SELECT sOrderID, sDescription, sItem, " +
                      "sOrderLot, sWarehouse, " +
                      "CONCAT(CONCAT(sBeginWarehouse, '-'), sBeginAddress) AS SBEGINLOCATION, " +
                      "CONCAT(CONCAT(sEndingWarehouse, '-'), sEndingAddress) AS SENDINGLOCATION " +
                      "FROM " + getWriteTableName() + " ";
    return mntQuery;
  }

  /**
   * Method to see if any lines on this Purchase order have invalid items
   * 
   * @return StringBuffer Order IDs.
   */
  public List<Map> getOrderLinesWithInvalidItemMaster() throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT SorderID, sItem FROM OrderLine ")
             .append("WHERE sItem NOT IN (Select sItem FROM ITEMMASTER)");

    return fetchRecords(vpSql.toString());
  }

  /**
   * Sets Objects for garbage collection.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpOLData = null;
  }

  /**
   * Method returns a list of outstanding OrderLine records sorted by the
   * OrderLine sequence
   * 
   * @param isOrderId <code>String</code> Order Id
   * @return List of outstanding order lines of the given order or none if no
   *         such order line found.
   * @throws DBException
   */
  public List<Map> getOutstandingOrderLinesByOrderId(String isOrderId)
      throws DBException
  {
    
    StringBuilder vpSql = new StringBuilder("SELECT * FROM OrderLine ")
             .append("  WHERE SORDERID = ? ")
             .append("    AND FALLOCATEDQUANTITY < FORDERQUANTITY ")
             .append("  ORDER BY SLINEID ");

    return fetchRecords(vpSql.toString(), isOrderId);
  }
}
