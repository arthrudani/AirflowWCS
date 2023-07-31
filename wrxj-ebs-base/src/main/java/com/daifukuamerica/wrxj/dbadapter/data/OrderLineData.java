package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import static com.daifukuamerica.wrxj.dbadapter.data.OrderLineEnum.*;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.log.Logger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:<BR>
 *   Class to handle Order Line Data operations.
 *
 * @author       A.D.
 * @version      1.0    05/30/01
 * @version      2.0    01/11/05   Added Replenishment and CycleCount code.
 */
public class OrderLineData extends AbstractSKDCData
{
  public static final String ALLOCATEDQUANTITY_NAME = ALLOCATEDQUANTITY.getName();
  public static final String BEGINADDRESS_NAME      = BEGINADDRESS.getName();
  public static final String BEGINLOCATION_NAME     = BEGINLOCATION.getName();
  public static final String BEGINWAREHOUSE_NAME    = BEGINWAREHOUSE.getName();
  public static final String CONTAINERTYPE_NAME     = CONTAINERTYPE.getName();
  public static final String DESCRIPTION_NAME       = DESCRIPTION.getName();
  public static final String ENDINGADDRESS_NAME     = ENDINGADDRESS.getName();
  public static final String ENDINGLOCATION_NAME    = ENDINGLOCATION.getName();
  public static final String ENDINGWAREHOUSE_NAME   = ENDINGWAREHOUSE.getName();
  public static final String HEIGHT_NAME            = HEIGHT.getName();
  public static final String ITEM_NAME              = ITEM.getName();
  public static final String LASTLINE_NAME          = LASTLINE.getName();
  public static final String LINEID_NAME            = LINEID.getName();
  public static final String LINESHY_NAME           = LINESHY.getName();
  public static final String LOADID_NAME            = LOADID.getName();
  public static final String ORDERID_NAME           = ORDERID.getName();
  public static final String ORDERLOT_NAME          = ORDERLOT.getName();
  public static final String ORDERQUANTITY_NAME     = ORDERQUANTITY.getName();
  public static final String PICKQUANTITY_NAME      = PICKQUANTITY.getName();
  public static final String ROUTEID_NAME           = ROUTEID.getName();
  public static final String SHIPQUANTITY_NAME      = SHIPQUANTITY.getName();
  public static final String WAREHOUSE_NAME         = WAREHOUSE.getName();

  public static final int REPL_ITEM     = 1;
  public static final int REPL_LOCATION = 2;
  public static final int CC_ITEM       = 3;
  public static final int CC_ITEM_LOT   = 4;
  public static final int CC_LOCATION   = 5;
  public static final int CC_WAREHOUSE  = 6;
  public static final int CC_ITEM_LOT_WHS = 7;
  public static final int CC_ITEM_WHS   = 8;

/*---------------------------------------------------------------------------
                 Database fields for OrderLine table.
  ---------------------------------------------------------------------------*/
  private boolean mzBeginWarehouse   = false;
  private boolean mzEndingWarehouse  = false;
  private boolean mzBeginAddress     = false;
  private boolean mzEndingAddress    = false;
  private int     iLineShy           = DBConstants.NO;
  private int     iHeight            = 1;
  private double  fOrderQuantity     = 0;
  private double  fAllocatedQuantity = 0;
  private double  fPickQuantity      = 0;
  private double  fShipQuantity      = 0;
  private String  sOrderID           = "";
  private String  sItem              = "";
  private String  sOrderLot          = "";
  private String  sRouteID           = "";
  private String  sLoadID            = "";
  private String  sDescription       = "";
  private String  sContainerType     = "";
  private String  sLineID            = "";
  private String  sWarehouse         = "";
  private String  sBeginWarehouse    = "";
  private String  sBeginAddress      = "";
  private String  sBeginLocation     = "";
  private String  sEndingWarehouse   = "";
  private String  sEndingAddress     = "";
  private String  sEndingLocation    = "";
  private String  sLastLine          = "NO";
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();


  public OrderLineData()
  {
    super();
    initColumnMap(mpColumnMap, OrderLineEnum.class);
  }

 /**
  * This helps in debugging when we want to print the whole structure.
  */
  @Override
  public String toString()
  {
    String s = "sOrderID:" + sOrderID +
               "\nsItem:" + sItem +
               "\nsOrderLot:" + sOrderLot +
               "\nsRouteID:" + sRouteID +
               "\nsLoadID:" + sLoadID +
               "\nsLineID:" + sLineID +
               "\nDescription:" + sDescription +
               "\nsContainerType:" + sContainerType + "\n";

    try
    {
     s = s + "iLineShy:" + DBTrans.getStringValue(LINESHY.getName(), iLineShy) +
               "\niHeight:" + iHeight + "\n";
}
    catch(NoSuchFieldException e)
    {
      s = s + "0";
    }

    s = s + "fOrderQuantity:"          + Double.toString(fOrderQuantity)     +
            "\nfAllocatedQuantity:" + Double.toString(fAllocatedQuantity) +
            "\nfPickQuantity:"      + Double.toString(fPickQuantity)      +
            "\nfShipQuantity:"      + Double.toString(fShipQuantity)      +
            "\nsWarehouse:"       + sWarehouse       + 
            "\nsBeginWarehouse:"  + sBeginWarehouse  +
            "\nsBeginAddress:"    + sBeginAddress    +
            "\nsBeginLocation:"   + sBeginLocation   +
            "\nsEndingWarehouse:" + sEndingWarehouse +
            "\nsEndingAddress:"   + sEndingAddress   +
            "\nsEndingLocation:"  + sEndingLocation  +
            "\n\n";

    s += super.toString();

    return(s);
  }

 /**
  * Defines equality between two OrderLineData objects.
  *
  * @param  absOL <code>AbstractSKDCData</code> reference whose runtime type
  *         is expected to be <code>OrderLineData</code>
  */
  @Override
  public boolean equals(AbstractSKDCData absOL)
  {
    boolean rtn = false;

    OrderLineData ol = (OrderLineData)absOL;

                                       // If the order doesn't even match...
    if (!sOrderID.equals(ol.getOrderID()))
    {
      return(false);
    }

    if (ol.getLoadID().trim().length() != 0)
    {                                  // Order Line is Load based.
      if (this.getLoadID().equals(ol.getLoadID()))
      {
        rtn = true;
      }
    }
    else
    {                                  // Order Line is Item based.
      if (this.getLineID().equals(ol.getLineID()) &&
          this.getItem().equals(ol.getItem()) &&
          this.getOrderLot().equals(ol.getOrderLot()))
      {
        rtn = true;
      }
    }

    return(rtn);
  }

  /**
   * This generates the string for the field that is changed.
   */
  @Override
  public String getActionDesc(String isColName, Object ipOld, Object ipNew)
  {
    String s = "Field " + isColName + ":";
    
    try
    {
      String vsOld = "";
      String vsNew = "";
  
      // Construct Action Description string
      switch((OrderLineEnum)mpColumnMap.get(isColName))
      {
      case LINESHY:
        vsOld = DBTrans.getStringValue(isColName, ((Integer)ipOld).intValue());
        vsNew = DBTrans.getStringValue(isColName, ((Integer)ipNew).intValue());
        break;
        
      default:
        vsOld = ipOld.toString();
        vsNew = ipNew.toString();
      }
      s = s + " Old [" + vsOld + "] New [" + vsNew + "]";
    }
    catch(NoSuchFieldException e)
    {
      s = s + " #### NoSuchFieldException #### ";
    }
    return s;
  }

 /**
  *  Method to convert a location key to a warehouse address key combination.
  *  @param  keyName <code>String</code> containing the location name such as
  *          "SBEGINLOCATION" or "SENDINGLOCATION".
  */
  public void mapLocationKey(String keyName)
  {
    KeyObject locnKey = getKeyObject(keyName);
    if (locnKey != null)
    {
      String sWhsName, sAddrName;
      if (keyName.equalsIgnoreCase(BEGINLOCATION.getName()))
      {
        sWhsName = BEGINWAREHOUSE.getName();
        sAddrName = BEGINADDRESS.getName();
      }
      else
      {
        sWhsName = ENDINGWAREHOUSE.getName();
        sAddrName = ENDINGADDRESS.getName();
      }

      String[] locn = Location.parseLocation(locnKey.getColumnValue().toString());
      setKey(sWhsName, locn[0]);
      setKey(sAddrName, locn[1], locnKey.getComparisonSymbolicName());
      deleteKeyObject(keyName);
    }
  }

 /**
  *  Method to convert a location column to a warehouse address combination.
  *  @param  keyName <code>String</code> containing the location name such as
  *          "SBEGINLOCATION" or "SENDINGLOCATION".
  */
  public void mapLocationColumn(String colName)
  {
    ColumnObject locnColumn = getColumnObject(colName);

    if (locnColumn != null)
    {
      String sWhsName, sAddrName;
      if (colName.equalsIgnoreCase(BEGINLOCATION.getName()))
      {
        sWhsName = BEGINWAREHOUSE.getName();
        sAddrName = BEGINADDRESS.getName();
      }
      else
      {
        sWhsName = ENDINGWAREHOUSE.getName();
        sAddrName = ENDINGADDRESS.getName();
      }
      String[] locn = Location.parseLocation(locnColumn.getColumnValue().toString());
      addColumnObject(new ColumnObject(sWhsName, locn[0]));
      addColumnObject(new ColumnObject(sAddrName, locn[1]));
      deleteColumnObject(colName);
    }
  }

 /**
  * Resets the data in this class to the default.
  */
  @Override
  public void clear()
  {
    super.clear();                     // Pull in default behaviour.

    sOrderID        = "";
    sItem           = "";
    sOrderLot       = "";
    sRouteID        = "";
    sLoadID         = "";
    sLineID         = "";
    sDescription    = "";
    sContainerType  = "";
    sWarehouse      = "";
    sBeginLocation  = "";
    sEndingLocation = "";
    sLastLine       = "NO";
    fOrderQuantity  = 0;
    fPickQuantity   = 0;
    fShipQuantity   = 0;
    iLineShy        = DBConstants.NO;
    iHeight         = 1;
    mzBeginWarehouse = mzEndingWarehouse = mzBeginAddress = mzEndingAddress = false; 
  }

/*---------------------------------------------------------------------------
                       Column value get methods go here.
  ---------------------------------------------------------------------------*/
 /**
  * Fetches Order ID
  * @return OrderID as string
  */
  public String getOrderID()
  {
    return(sOrderID);
  }

 /**
  * Fetches Order Line Item
  * @return Item as string
  */
  public String getItem()
  {
    return(sItem);
  }

 /**
  * Fetches Order Lot
  * @return Order Lot as string
  */
  public String getOrderLot()
  {
    return(sOrderLot);
  }

 /**
  * Fetches Line number
  * @return Line number as a string
  */
  public String getLineID()
  {
    return(sLineID);
  }

 /**
  * Fetches Route ID for the ordered Load (if orders are by load vs Items).
  * @return Route ID as string
  */
  public String getRouteID()
  {
    return(sRouteID);
  }

 /**
  * Fetches Ordered Load ID (if ordering by loads).
  * @return Load ID as String.
  */
  public String getLoadID()
  {
    return(sLoadID);
  }

 /**
  * Fetches Order Line Description
  * @return Description as String.
  */
  public String getDescription()
  {
    return(sDescription);
  }

 /**
  * Fetches Order Line ContainerType
  * @return ContainerType as String.
  */
  public String getContainerType()
  {
    return(sContainerType);
  }

 /**
  * Fetches Ordered quantity for line (if ordering by items).
  * @return Ordered Quantity as a double.
  */
  public double getOrderQuantity()
  {
    return(fOrderQuantity);
  }

 /**
  * Fetches Allocated quantity for line (if ordering by items).
  * @return Allocated Quantity as a double.
  */
  public double getAllocatedQuantity()
  {
    return(fAllocatedQuantity);
  }

 /**
  * Fetches Pick quantity for line (if ordering by items).
  * @return Pick Quantity as a double.
  */
  public double getPickQuantity()
  {
    return(fPickQuantity);
  }

 /**
  * Fetches Ship quantity for line (if ordering by items).
  * @return Ship Quantity as a double.
  */
  public double getShipQuantity()
  {
    return(fShipQuantity);
  }

 /**
  * Fetches Line shy flag.
  * @return Line Shy flag as an integer.
  */
  public int getLineShy()
  {
    return(iLineShy);
  }

 /**
  * Fetches Order Load/Container Height.
  * @return Height..
  */
  public int getHeight()
  {
    return(iHeight);
  }

 /**
  * Fetches Maintenance Order Warehouse.
  * @return warehouse as String.
  */
  public String getWarehouse()
  {
    return(sWarehouse);
  }

 /**
  * Fetches Maintenance Order Beginning Location.
  * @return beginning Location as String.
  */
  public String getBeginLocation()
  {
    return(sBeginLocation);
  }

  /**
   * Fetches Maintenance Order Beginning warehouse
   * @return Beginning Warehouse as String.
   */
   public String getBeginWarehouse()
   {
     return(sBeginWarehouse);
   }

  /**
   * Fetches Maintenance Order Beginning address.
   * @return Beginning Address as String.
   */
   public String getBeginAddress()
   {
     return(sBeginAddress);
   }
 /**
  * Fetches Maintenance Order Ending Location.
  * @return Ending Location as String.
  */
  public String getEndingLocation()
  {
    return(sEndingLocation);
  }

 /**
  * Fetches Maintenance Order Ending warehouse
  * @return Ending Warehouse as String.
  */
  public String getEndingWarehouse()
  {
    return(sEndingWarehouse);
  }

 /**
  * Fetches Maintenance Order Ending address.
  * @return ending Address as String.
  */
  public String getEndingAddress()
  {
    return(sEndingAddress);
  }

   /**
  *  Gets the flag indicating if this is the last line of an order.
  * @return translation Yes or NO.
  */
  public int getLastLine()
  {
    int iLastLine = (sLastLine.equalsIgnoreCase("NO")) ? DBConstants.NO : 
                                                         DBConstants.YES;
    return(iLastLine);
  }

/*---------------------------------------------------------------------------
               ******** Column Setting methods go here. ********
  ---------------------------------------------------------------------------*/
 /**
  * Sets Order ID value.
  */
  public void setOrderID(String isOrderID)
  {
    sOrderID = checkForNull(isOrderID);
    addColumnObject(new ColumnObject(ORDERID.getName(), sOrderID));
  }

 /**
  * Sets Order Item value.
  */
  public void setItem(String isItem)
  {
    sItem = checkForNull(isItem);
    addColumnObject(new ColumnObject(ITEM.getName(), sItem));
  }

 /**
  * Sets Order Lot value.
  */
  public void setOrderLot(String isOrderLot)
  {
    sOrderLot = checkForNull(isOrderLot);
    addColumnObject(new ColumnObject(ORDERLOT.getName(), sOrderLot));
  }

 /**
  * Sets Line identifier.
  */
  public void setLineID(String isLineID)
  {
    sLineID = checkForNull(isLineID);
    addColumnObject(new ColumnObject(LINEID.getName(), sLineID));
  }

 /**
  * Sets Route ID for the order load.
  */
  public void setRouteID(String isRouteID)
  {
    sRouteID = checkForNull(isRouteID);
    addColumnObject(new ColumnObject(ROUTEID.getName(), sRouteID));
  }

 /**
  * Sets Ordered Load value.
  */
  public void setLoadID(String isLoadID)
  {
    sLoadID = checkForNull(isLoadID);
    addColumnObject(new ColumnObject(LOADID.getName(), sLoadID));
  }

 /**
  * Sets Order Line Description
  */
  public void setDescription(String isDescription)
  {
    sDescription = checkForNull(isDescription);
    addColumnObject(new ColumnObject(DESCRIPTION.getName(), this.sDescription));
  }

 /**
  * Sets Order Line ContainerType
  */
  public void setContainerType(String isContainerType)
  {
    sContainerType = checkForNull(isContainerType);
    addColumnObject(new ColumnObject(CONTAINERTYPE.getName(), sContainerType));
  }

 /**
  * Sets Warehouse for Maintenance Order.
  */
  public void setWarehouse(String isWarehouse)
  {
    sWarehouse = checkForNull(isWarehouse);
    addColumnObject(new ColumnObject(WAREHOUSE.getName(), sWarehouse));
  }

 /**
  * Sets the beginning warehouse.  This method exists only for backwards
  * compatibility with the item-based order line object.  This field only exists
  * for Cycle Counts and Replenishment orders.
  * @param sBeginWarehouse <code>String</code> containing Begininng Warehouse.
  */
  public void setBeginWarehouse(String isBeginWarehouse)
  {
    sBeginWarehouse = checkForNull(isBeginWarehouse);
    addColumnObject(new ColumnObject(BEGINWAREHOUSE.getName(), sBeginWarehouse));
    mzBeginWarehouse = true;
    if (mzBeginAddress)
      this.sBeginLocation = sBeginWarehouse + "-" + sBeginAddress;
  }
  
 /**
  * Sets the beginning warehouse.  This method exists only for backwards
  * compatibility with the item-based order line object.  This field only exists
  * for Cycle Counts and Replenishment orders.
  * @param sBeginWarehouse <code>String</code> containing Begininng Warehouse.
  */
  public void setBeginAddress(String isBeginAddress)
  {
    sBeginAddress = checkForNull(isBeginAddress);
    addColumnObject(new ColumnObject(BEGINADDRESS.getName(), sBeginAddress));
    mzBeginAddress = true;
    if (mzBeginWarehouse)
      this.sBeginLocation = sBeginWarehouse + "-" + sBeginAddress;
  }

 /**
  * Sets Beginning Location for Maintenance Order.
  */
  public void setBeginLocation(String isBeginLocation)
  {
    sBeginLocation = checkForNull(isBeginLocation);
    addColumnObject(new ColumnObject(BEGINLOCATION.getName(), sBeginLocation));
  }

 /**
  * Sets the beginning warehouse.  This method exists only for backwards
  * compatibility with the item-based order line object.  This field only exists
  * for Cycle Counts and Replenishment orders.
  * @param sBeginWarehouse <code>String</code> containing Begininng Warehouse.
  */
  public void setEndingWarehouse(String isEndingWarehouse)
  {
    sEndingWarehouse = checkForNull(isEndingWarehouse);
    addColumnObject(new ColumnObject(ENDINGWAREHOUSE.getName(), sEndingWarehouse));
    mzEndingWarehouse = true;
    if (mzEndingAddress)
      this.sEndingLocation = sEndingWarehouse + "-" + sEndingAddress;
  }
  
 /**
  * Sets the ending address.  This method exists only for backwards
  * compatibility with the item-based order line object.  This field only exists
  * for Cycle Counts and Replenishment orders.
  * @param sBeginWarehouse <code>String</code> containing Begininng Warehouse.
  */
  public void setEndingAddress(String isEndingAddress)
  {
    sEndingAddress = checkForNull(isEndingAddress);
    addColumnObject(new ColumnObject(ENDINGADDRESS.getName(), sEndingAddress));
    mzEndingAddress = true;
    if (mzEndingWarehouse)
      this.sEndingLocation = sEndingWarehouse + "-" + sEndingAddress;
  }

 /**
  * Sets ending Location for Maintenance Order.
  */
  public void setEndingLocation(String isEndingLocation)
  {
    sEndingLocation = checkForNull(isEndingLocation);
    addColumnObject(new ColumnObject(ENDINGLOCATION.getName(), sEndingLocation));    
  }

 /**
  * Sets Order Line Quantity
  */
  public void setOrderQuantity(double idOrderQuantity)
  {
    fOrderQuantity = idOrderQuantity;
    addColumnObject(new ColumnObject(ORDERQUANTITY.getName(),
                    fOrderQuantity));
  }

 /**
  * Sets allocated Quantity for a line item (presumably only used by the
  * allocator)
  */
  public void setAllocatedQuantity(double idAllocatedQuantity)
  {
    fAllocatedQuantity = idAllocatedQuantity;
    addColumnObject(new ColumnObject(ALLOCATEDQUANTITY.getName(), fAllocatedQuantity));
  }

 /**
  * Sets pick Quantity for a line item.
  */
  public void setPickQuantity(double idPickQuantity)
  {
    fPickQuantity = idPickQuantity;
    addColumnObject(new ColumnObject(PICKQUANTITY.getName(), fPickQuantity));
  }

 /**
  * Sets shipping Quantity for a line item.
  */
  public void setShipQuantity(double idShipQuantity)
  {
    fShipQuantity = idShipQuantity;
    addColumnObject(new ColumnObject(SHIPQUANTITY.getName(), fShipQuantity));
  }

 /**
  * Sets Shy Line Item Flag value
  */
  public void setLineShy(int inLineShy)
  {
    try
    {
      DBTrans.getStringValue(LINESHY.getName(), inLineShy);
    }
    catch(NoSuchFieldException e)
    {                                  // Passed value wasn't valid. Default it
      inLineShy = DBConstants.NO;
    }
    iLineShy = inLineShy;
    addColumnObject(new ColumnObject(LINESHY.getName(), iLineShy));
  }

 /**
  *  Sets the last record line mark for a host message. <b>Note:</b> this field
  *  is not in the database! This is used only for storage purposes in this 
  *  object.
  *  @param sLastLine Parameter indicating if this is the last line of an order.
  *         Acceptable values are the strings "YES" or "NO".
  */
  public void setLastLine(String isLastLine)
  {
    if (!isLastLine.equalsIgnoreCase("NO") && !isLastLine.equalsIgnoreCase("YES"))
    {
      Logger.getLogger().logError("Incorrect value received for the " + 
                                      "Last Line mark of order line: " + 
                                      "Order ID = " + sOrderID + ", " + 
                                      "Item = " + sItem + ", " + 
                                      "Line ID = " + sLineID);

      isLastLine = "NO";                // Default it to NO.
    }
    sLastLine = isLastLine;
  }

 /**
  * Sets Order Container Height
  */
  public void setHeight(int inHeight)
  {
    iHeight = inHeight;
    addColumnObject(new ColumnObject(HEIGHT.getName(), iHeight));
  }

  /**
   * {@inheritDoc}
   * @param {@inheritDoc}
   * @param {@inheritDoc}
   * @return {@inheritDoc}
   */
  @Override
  public int setField(String isColName, Object ipColValue)
  {
    TableEnum vpEnum = mpColumnMap.get(isColName);
    if (vpEnum == null)
    { 
      return super.setField(isColName, ipColValue);
    }

    switch((OrderLineEnum)vpEnum)
    {
      case ORDERID:
        setOrderID((String)ipColValue);
        break;
        
      case ITEM:
        setItem((String)ipColValue);
        break;

      case ORDERLOT:
        setOrderLot((String)ipColValue);
        break;

      case ROUTEID:
        setRouteID((String)ipColValue);
        break;

      case LOADID:
        setLoadID((String)ipColValue);
        break;

      case LINEID:
        setLineID((String)ipColValue);
        break;

      case DESCRIPTION:
        setDescription((String)ipColValue);
        break;

      case CONTAINERTYPE:
        setContainerType((String)ipColValue);
        break;

      case WAREHOUSE:
        setWarehouse((String)ipColValue);
        break;

      case BEGINWAREHOUSE:
        setBeginWarehouse((String)ipColValue);
        break;

      case BEGINADDRESS:
        setBeginAddress((String)ipColValue);
        break;

      case BEGINLOCATION:
        setBeginLocation((String)ipColValue);
        break;

      case ENDINGWAREHOUSE:
        setEndingWarehouse((String)ipColValue);
        break;

      case ENDINGADDRESS:
        setEndingAddress((String)ipColValue);
        break;

      case ENDINGLOCATION:
        setEndingLocation((String)ipColValue);
        break;

      case LASTLINE:
        setLastLine((String)ipColValue);
        break;

      case ORDERQUANTITY:
        setOrderQuantity((Double)ipColValue);
        break;

      case ALLOCATEDQUANTITY:
        setAllocatedQuantity((Double)ipColValue);
        break;

      case PICKQUANTITY:
        setPickQuantity((Double)ipColValue);
        break;

      case SHIPQUANTITY:
        setShipQuantity((Double)ipColValue);
        break;

      case LINESHY:
        setLineShy((Integer)ipColValue);
        break;

      case HEIGHT:
        setHeight((Integer)ipColValue);
    }

    return(0);
  }
}
