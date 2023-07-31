package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Description:<BR>
 *   Concrete implementation of OrderInterface class for regular order
 *   headers (sales orders and internal orders). This Class will handle Order
 *   Header specific operations.
 *
 * @author       A.D.
 * @version      1.0   05/28/2002
 * @version      2.0   10/07/2004
 */
public class OrderHeader extends BaseDBInterface
{
  public static final String ORDER_ID_DELIMITER = "-";
  
  protected OrderHeaderData mpOHData;

  public OrderHeader()
  {
    super("OrderHeader");
    mpOHData = Factory.create(OrderHeaderData.class);
  }

  /**
   * Retrieves one column value from the OrderHeader table.
   * 
   * @param isOrderID the unique key to use in the search.
   * @param isColumnName the name of the column whose value is returned.
   * @return value of column specified by isColumnName as an <code>Object</code>.
   *         The caller is assumed to know what data type is actually in
   *         <code>Object</code>. <i>A</i> <code>null</code> <i>object is
   *         returned for no matching data</i>
   * @throws DBException when database access errors occur.
   */
  public Object getSingleColumnValue(String isOrderID, String isColumnName)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT ").append(isColumnName)
               .append(" FROM OrderHeader WHERE sOrderID = '")
               .append(isOrderID).append("'");
    List<Map> vpData = fetchRecords(vpSql.toString());

    return (!vpData.isEmpty()) ? (vpData.get(0)).get(isColumnName.toUpperCase()) : null;
  }
  
 /**
  * Method to get Order Status.
  *
  * @param  absData <code>AbstractSKDCData</code> object containing key
  *         information to do the lookup.  If there is no key info. then
  *         we do a wild-card search.
  * @return int containing order status.
  */
  public int getOrderStatusValue(AbstractSKDCData absData) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT iOrderStatus FROM ").append(getWriteTableName())
             .append(DBHelper.buildWhereClause(absData.getKeyArray()));

    return getIntegerColumn(OrderHeaderData.ORDERSTATUS_NAME, vpSql.toString());
  }

 /**
  *  Method to set Order Status.
  *
  *  @param order <code>String</code> containing unique order id. to find data.
  *  @param newStatus <code>int</code> containing new update order status.
  */
  public void setOrderStatusValue(String order, int newStatus)
         throws DBException
  {
    mpOHData.clear();
    mpOHData.setOrderStatus(newStatus);
    mpOHData.setKey("SORDERID", order);
    modifyElement(mpOHData);
  }

 /**
  * Method fetches the next status an order is required to have.
  * @param isOrderID The order id. of the order.
  * @return the next status.
  * @throws DBException if there is a database access error.
  */
  public int getNextOrderStatusValue(String isOrderID) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT iNextStatus FROM OrderHeader WHERE ")
             .append("sOrderID = '").append(isOrderID).append("'");

    return getIntegerColumn(OrderHeaderData.NEXTSTATUS_NAME, vpSql.toString());
  }
  
 /**
  *  Method to set Order Status to the value specified in the Next Status field.
  *
  *  @param order <code>String</code> containing unique order id. to find data.
  *  @throws DBException if there is a database access error or update error.
  */
  public void setNextOrderStatusValue(String isOrder)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT iNextStatus FROM OrderHeader WHERE ")
             .append("sOrderID = '").append(isOrder).append("'");

    int iNextStatus = getIntegerColumn(OrderHeaderData.NEXTSTATUS_NAME, vpSql.toString());

    mpOHData.clear();
    mpOHData.setOrderStatus(iNextStatus);
    mpOHData.setKey(OrderHeaderData.ORDERID_NAME, isOrder);
    modifyElement(mpOHData);
  }

  /**
   *  Method to set Order Message.
   *
   *  @param order <code>String</code> containing unique order id. to find data.
   *  @param message <code>String</code> containing the new message.
   */
   public void setOrderMessage(String order, String message)
          throws DBException
   {
     mpOHData.clear();
     mpOHData.setOrderMessage(message);
     mpOHData.setKey("SORDERID", order);
     modifyElement(mpOHData);
   }
   
 /**
  *  Method to get Order Type
  *
  *  @param absData <code>AbstractSKDCData</code> containing key criteria to
  *         find data.
  *  @return int containing order type.
  */
  public int getOrderTypeValue(AbstractSKDCData absData) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT iOrderType FROM OrderHeader")
             .append(DBHelper.buildWhereClause(absData.getKeyArray()));

    return getIntegerColumn(OrderHeaderData.ORDERTYPE_NAME, vpSql.toString());
  }

 /**
  *  Method to set Order Type.
  *
  *  @param order <code>String</code> containing unique order id. to find data.
  *  @param newType <code>int</code> containing new update order type.
  */
  public void setOrderTypeValue(String order, int newType) throws DBException
  {
    mpOHData.clear();
    mpOHData.setOrderType(newType);
    mpOHData.setKey("SORDERID", order);
    modifyElement(mpOHData);
  }

 /**
  * Method to get List of order IDs..
  *  @param isAllOrNone <code>String</code> containing string to start list
  *         with.  The values are SKDCConstants.ALL_STRING or
  *         SKDCConstants.NONE_STRING
  *
  * @return StringBuffer Order IDs.
  */
  public String[] getOrderChoices(String isAllOrNone) throws DBException
  {
    return getDistinctColumnValues(OrderHeaderData.ORDERID_NAME, isAllOrNone);
  }

  /**
   * Method gets a list of order id's based on order status(s), and/or order
   * type. The returned list is ordered by the oldest checked short order to the
   * newest.
   * 
   * @param ipOrderStatuses translation values denoting order status.
   * @param inOrderType translation value denoting order type. <i>This is an
   *            optional parameter that provides additional filtering. <b>This
   *            parameter is ignored if it is passed as 0</b></i>
   * @return String array of order id's. ordered by the oldest to newest
   *         scheduled date.
   */
  public String[] getCheckedShortOrderList(int[] ipOrderStatuses,
      int inOrderType) throws DBException
  {
    String vsOrderStatusSQL = "";
    String vsOrderTypeSQL = "";

    if (inOrderType != 0)
      vsOrderTypeSQL = "AND iOrderType = " + inOrderType + " ";

    if (ipOrderStatuses.length == 1)
    {
      vsOrderStatusSQL = "iOrderStatus = " + ipOrderStatuses[0] + " ";
    }
    else
    {
      vsOrderStatusSQL = "iOrderStatus IN (";
      for(int idx = 0; idx < ipOrderStatuses.length; idx++)
      {
        vsOrderStatusSQL += Integer.toString(ipOrderStatuses[idx]);
        if (idx < ipOrderStatuses.length-1) vsOrderStatusSQL += ", ";
      }
      vsOrderStatusSQL += ") ";
    }

    StringBuilder vpSql = new StringBuilder("SELECT sOrderID FROM OrderHeader WHERE ")
             .append(vsOrderStatusSQL)
             .append(vsOrderTypeSQL).append("ORDER BY dShortOrderCheckTime");
             
    List<Map> vpArrList = fetchRecords(vpSql.toString());
    
    return SKDCUtility.toStringArray(vpArrList, OrderHeaderData.ORDERID_NAME);
  }

  /**
   * Method gets a list of order id's based on order status(s), and/or order
   * type. The returned list is ordered by the oldest checked short order to the
   * newest.
   * 
   * @param ipOrderStatuses translation values denoting order status.
   * @param inOrderType translation value denoting order type. <i>This is an
   *            optional parameter that provides additional filtering. <b>This
   *            parameter is ignored if it is passed as 0</b></i>
   * @return String array of order id's. ordered by the oldest to newest
   *         scheduled date.
   * @throws DBException
   */
  public String[] getOrderListByStatuses(int[] ipOrderStatuses, int inOrderType) throws DBException
  {
    String vsOrderStatusSQL = "";
    String vsOrderTypeSQL = "";

    if (inOrderType != 0)
      vsOrderTypeSQL = "AND iOrderType = " + inOrderType + " ";

    if (ipOrderStatuses.length == 1)
    {
      vsOrderStatusSQL = "iOrderStatus = " + ipOrderStatuses[0] + " ";
    }
    else
    {
      vsOrderStatusSQL = "iOrderStatus IN (";
      for(int idx = 0; idx < ipOrderStatuses.length; idx++)
      {
        vsOrderStatusSQL += Integer.toString(ipOrderStatuses[idx]);
        if (idx < ipOrderStatuses.length-1) vsOrderStatusSQL += ", ";
      }
      vsOrderStatusSQL += ") ";
    }

    StringBuilder vpSql = new StringBuilder("SELECT sOrderID FROM ")
             .append("OrderHeader WHERE dOrderedTime IN ")
             .append("(SELECT MIN(dOrderedTime) FROM OrderHeader WHERE ")
             .append(vsOrderStatusSQL)
             .append(vsOrderTypeSQL).append(") ").append("ORDER BY dShortOrderCheckTime");
              
    List<Map> vpArrList = fetchRecords(vpSql.toString());

    return SKDCUtility.toStringArray(vpArrList, OrderHeaderData.ORDERID_NAME);
  }
  
  /**
   * Method to check for Order existence.
   *
   * @param orderID <code>String</code> containing order ID.
   * @return boolean of true if it exists, false otherwise.
   */
  public boolean exists(String orderID)
  {
    mpOHData.clear();
    mpOHData.setKey(OrderHeaderData.ORDERID_NAME, orderID);
    return exists(mpOHData);
  }
  
  /**
   * Check if the order id prefix is already in orderheader table
   * 
   * @param orderIDPrefix order id
   * @return True if found
   * @throws DBException If anything goes wrong
   */
  public boolean orderIdPrefixExists(String orderIDPrefix) throws DBException
  {
      StringBuilder queryBuf = new StringBuilder()
              .append(ROW_COUNT_SQL) 
              .append(" orderheader as oh")
              .append(" WHERE oh.sOrderID like '").append(orderIDPrefix).append(ORDER_ID_DELIMITER).append("%'");
      int count = getRecordCount(queryBuf.toString(), ROW_COUNT_NAME);
      if (count > 0) {
          return true;
      }
      return false;
  }

  /**
   *  Method to generate a randomized order id.
   *  @param iOrderType <code>int</code> containing order type.
   *  @return String containing order identifier.  <code>null</code> if there is
   *          an error generating the order.
   */
  public static String generateOrderID(int iOrderType)
  {
    Random rand = new Random();
    String sOrderPrefix;

    switch(iOrderType)
    {
      case DBConstants.CYCLECOUNT:
        sOrderPrefix = "CYC";
        break;
    
      case DBConstants.REPLENISHMENT:
        sOrderPrefix = "RPL";
        break;
    
      default:
        sOrderPrefix = "";
    }
    
    return sOrderPrefix + Math.abs(rand.nextInt());
  }

 /**
  *  Sets Objects for garbage collection.
  */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpOHData = null;
  }

/**
 * Method to Get the Oldest OrderID on Hold for a Station.
 *
 * @param stationName Destination station.
 * @param inOrderStatus the status of the order
 * @return OrderId of the oldest Order by status waiting for a station OR
 * an empty string if there are none.
 * @throws DBException  if there is a DB access error.
 */
  public String getOldestOrderForStation(String stationName, int inOrderStatus)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT sOrderID FROM ")
             .append("OrderHeader WHERE dOrderedTime IN ")
             .append("(SELECT MIN(dOrderedTime) FROM OrderHeader WHERE ")
             .append("iOrderStatus = ").append(inOrderStatus).append(" AND ")
             .append("sDestinationStation = '").append(stationName).append("')");
  
    return getStringColumn(OrderHeaderData.ORDERID_NAME, vpSql.toString());
  }
 
 /**
  * Method to Get the Oldest Order ID on Hold.
  *
  * @return OrderId of the oldest Order on Hold or an empty string if there are
  *         none.
  * @throws DBException if there is a DB access error.
  */
  public String getOldestHeldOrder() throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT sOrderID FROM ")
             .append("OrderHeader WHERE dOrderedTime IN ")
             .append("(SELECT MIN(dOrderedTime) FROM OrderHeader WHERE ")
             .append("iOrderStatus = ").append(DBConstants.HOLD).append(")");
  
    return getStringColumn(OrderHeaderData.ORDERID_NAME, vpSql.toString());
  }
  
  /**
   * Method to Get the Order ID of the oldest Short order.
   *
   * @return OrderId of the oldest Short Order or an empty string if there are
   *         none.
   * @throws DBException if there is a DB access error.
   */
   public String getOldestShortOrder() throws DBException
   {
     StringBuilder vpSql = new StringBuilder("SELECT sOrderID FROM ")
              .append("OrderHeader WHERE dOrderedTime IN ")
              .append("(SELECT MIN(dOrderedTime) FROM OrderHeader WHERE ")
              .append("iOrderStatus = ").append(DBConstants.SHORT).append(")");
   
     return getStringColumn(OrderHeaderData.ORDERID_NAME, vpSql.toString());
   }
  
  /**
   * Method gets the expected HostLineCount for a given order in the
   * OrderHeader.
   * 
   * @param sOrderID the order id. being searched for.
   * @return an integer representing
   * @throws DBException if there is a DB access error.
   */
  public int getHostLineCount(String sOrderID) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT iHostLineCount FROM OrderHeader WHERE ")
               .append("sOrderID = '").append(sOrderID).append("'");
    
    return getIntegerColumn(OrderHeaderData.HOSTLINECOUNT_NAME, vpSql.toString());
  }
    
  /**
   * Method to update the order status to the status of every load containing
   * inventory for this order. Does nothing if the status' are not identical
   */
  public void updateOrderStatus(String orderID)
  {
    try
    {
      int loadStatus = -1;
      Load load = Factory.create(Load.class);
      String[] loadIDArray = Factory.create(TableJoin.class).getLoadChoices(orderID, null, "");
      
      for(String loadID: loadIDArray)
      {
        LoadData loadData = load.getLoadData(loadID);
        if (loadStatus == -1)
        {
          loadStatus = loadData.getLoadMoveStatus();
        }
        else
        {
          if (loadStatus != loadData.getLoadMoveStatus()) return;
        }
      }
      
      // If we have not exited then all load status' are identical so change the
      // order header status to match
      setOrderStatusValue(orderID, loadStatus);
    }
    catch(DBException e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Retrieves a String[] list of all OrderHeaders with 
   * an incorrect ihostlinecnt
   *
   * @return reference to an String[] of Order Numbers    
   *         
   * @exception DBException
   */
  public List<Map> getOrderHeaderWithInvalidLinecount() throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT sOrderID, iHostLineCount FROM OrderHeader")
             .append(" WHERE OrderHeader.iHostLineCount != ")
             .append("(SELECT COUNT(1) FROM ORDERLINE ")
             .append(" WHERE ORDERLINE.SOrderID = OrderHeader.sOrderID)");

    return fetchRecords(vpSql.toString());
  }
   
  /**
   * Retrieves a String[] list of all OrderHeaders with an incorrect custid
   * 
   * @return reference to an String[] of Order Numbers
   * 
   * @exception DBException
   */
  public List<Map> getOrderHeaderWithInvalidShipCustomer() throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT sOrderID, sShipCustomer FROM OrderHeader")
             .append(" WHERE OrderHeader.sShipCustomer NOT LIKE ' %%' and")
             .append(" OrderHeader.sShipCustomer NOT IN")
             .append(" (SELECT SCUSTOMER FROM CUSTOMER)");

    return fetchRecords(vpSql.toString());
  }
    
  /**
   * Retrieves a String[] list of all OrderHeaders with an incorrect destination
   * 
   * @return reference to an String[] of Order Numbers
   * 
   * @exception DBException
   */
  public List<Map> getOrderHeaderWithInvalidDestination() throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT sOrderID, sDestWarehouse, sDestAddress FROM OrderHeader")
             .append(" WHERE OrderHeader.sDestWarehouse NOT IN")
             .append(" (SELECT SWAREHOUSE FROM LOCATION) OR")
             .append(" OrderHeader.sDestAddress NOT IN")
             .append(" (SELECT SADDRESS FROM LOCATION)");

    return fetchRecords(vpSql.toString());
  }
}
