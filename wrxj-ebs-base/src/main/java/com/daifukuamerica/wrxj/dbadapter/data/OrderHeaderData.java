package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:<BR>
 *   Class to handle Order Data operations.  This class treats columns, and
 *   Keys as Objects.
 *
 * @author       A.D.
 * @version      1.0      29-May-01
 */
public class OrderHeaderData extends AbstractSKDCData
{
  public static final String CARRIERID_NAME           = CARRIERID.getName();
  public static final String DESCRIPTION_NAME         = DESCRIPTION.getName();
  public static final String DESTADDRESS_NAME         = DESTADDRESS.getName();
  public static final String DESTINATIONSTATION_NAME  = DESTINATIONSTATION.getName();
  public static final String DESTWAREHOUSE_NAME       = DESTWAREHOUSE.getName();
  public static final String HOSTLINECOUNT_NAME       = HOSTLINECOUNT.getName();
  public static final String NEXTSTATUS_NAME          = NEXTSTATUS.getName();
  public static final String ORDEREDTIME_NAME         = ORDEREDTIME.getName();
  public static final String ORDERID_NAME             = ORDERID.getName();
  public static final String ORDERMESSAGE_NAME        = ORDERMESSAGE.getName();
  public static final String ORDERSTATUS_NAME         = ORDERSTATUS.getName();
  public static final String ORDERTYPE_NAME           = ORDERTYPE.getName();
  public static final String PRIORITY_NAME            = PRIORITY.getName();
  public static final String RELEASETOCODE_NAME       = RELEASETOCODE.getName();
  public static final String SCHEDULEDDATE_NAME       = SCHEDULEDDATE.getName();
  public static final String SHIPCUSTOMER_NAME        = SHIPCUSTOMER.getName();
  public static final String SHORTORDERCHECKTIME_NAME = SHORTORDERCHECKTIME.getName();


                                       // Constants pertaining to orders.
  public static final String CYCLECOUNT_PREFIX    = "CYC";
  public static final String REPLENISHMENT_PREFIX = "RPL";

/*---------------------------------------------------------------------------
                 Database fields for OrderHeader table.
  ---------------------------------------------------------------------------*/
  private int    iHostLineCount       = 0;
  private int    iNextStatus          = DBConstants.HOLD;
  private int    iOrderStatus         = DBConstants.ORBUILDING;
  private int    iOrderType           = DBConstants.ITEMORDER;
  protected int  iPriority            = 5;
  private Date   dOrderedTime         = new Date();
  private Date   dScheduledDate       = new Date();
  private Date   dShortOrderCheckTime = new Date();
  private String sCarrierID           = "";
  private String sDescription         = "";
  private String sDestAddress         = "";
  private String sDestinationStation  = "";
  private String sDestWarehouse       = "";
  private String sOrderID             = "";
  private String sOrderMessage        = "";
  private String sShipCustomer        = "";
  private String sReleaseToCode       = "";
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();
  
  public OrderHeaderData()
  {
    super();
    initColumnMap(mpColumnMap, OrderHeaderEnum.class);
  }

 /**
  * This helps in debugging when we want to print the whole structure.
  */
  @Override
  public String toString()
  {
    String s = "sOrderID:" + sOrderID +
               "\nsDestinationStation:" + sDestinationStation +
               "\nsDescription:" + sDescription +
               "\nsShipCustomer:" + sShipCustomer + 
               "\nreleaseToCode:" + sReleaseToCode +
               "\nsOrderMessage:" + sOrderMessage + 
               "\nsCarrierID:" + sCarrierID + 
               "\nsDestWarehouse:" + sDestWarehouse + 
               "\nsDestAddress:" + sDestAddress + "\n";

    try
    {
      s = s + "iOrderType:" + DBTrans.getStringValue(ORDERTYPE_NAME, iOrderType) +
               "\nOrderStatus:" + DBTrans.getStringValue(ORDERSTATUS_NAME, iOrderStatus) +
               "\nNextStatus:" + DBTrans.getStringValue(NEXTSTATUS_NAME, iNextStatus);
    }
    catch(NoSuchFieldException e)
    {
      s = s + "0";
    }

    s = s + "iPriority: "              + iPriority      +
            "\niHostLineCount: "       + iHostLineCount +
            "\ndOrderedTime: "         + sdf.format(dOrderedTime)   +
            "\ndScheduledDate: "       + sdf.format(dScheduledDate) +
            "\ndShortOrderCheckTime: " + sdf.format(dShortOrderCheckTime) +
               "\n\n";
    s += super.toString();

    return(s);
  }

  /**
   *  Method to make a deep copy of this object.
   *
   *  @return copy of <code>OrderHeaderData</code>.
   */
  @Override
  public OrderHeaderData clone()
  {
    OrderHeaderData vpClonedData = (OrderHeaderData)super.clone();
    vpClonedData.dOrderedTime = (Date)dOrderedTime.clone();
    vpClonedData.dScheduledDate = (Date)dScheduledDate.clone();
    vpClonedData.dShortOrderCheckTime = (Date)dShortOrderCheckTime.clone();

    return vpClonedData;
  }

  /**
   * Defines equality between two OrderHeaderData objects.
   *
   * @param  absOH <code>AbstractSKDCData</code> reference whose runtime type
   *         is expected to be <code>OrderHeaderData</code>
   */
  @Override
  public boolean equals(AbstractSKDCData absOH)
  {
    OrderHeaderData oh = (OrderHeaderData)absOH;
    return(getOrderID().equals(oh.getOrderID()));
  }

  /**
   * Resets the data in this class to the default.
   */
  @Override
  public void clear()
  {
    super.clear();                     // Pull in default behaviour.

    sOrderID = "";
    sDestinationStation = "";
    sDescription = "";
    sShipCustomer = "";
    iPriority = 5;
    iOrderType = DBConstants.ITEMORDER;
    iOrderStatus = DBConstants.ORBUILDING;
    iNextStatus = DBConstants.HOLD;
    dOrderedTime.setTime(System.currentTimeMillis());
    dScheduledDate.setTime(System.currentTimeMillis());
    dShortOrderCheckTime.setTime(System.currentTimeMillis());
    sReleaseToCode      = "";
    sOrderMessage       = "";
    sCarrierID          = "";
    sDestWarehouse      = "";
    sDestAddress        = "";
    iHostLineCount      = 0;
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

      OrderHeaderEnum vpOHEnum = (OrderHeaderEnum)mpColumnMap.get(isColName);

      if (vpOHEnum == null)
      {
        s = s + " Old [" + ipOld.toString() + "] New [" + ipNew.toString() + "]";
      }
      else
      {
      // Construct Action Description string
        switch(vpOHEnum)
        {
          case ORDERSTATUS:
            vsOld = DBTrans.getStringValue(isColName, ((Integer)ipOld).intValue());
            vsNew = DBTrans.getStringValue(isColName, ((Integer)ipNew).intValue());
            break;

          default:
            vsOld = ipOld.toString();
            vsNew = ipNew.toString();
         }
         s = s + " Old [" + vsOld + "] New [" + vsNew + "]";
      }
    }
    catch(NoSuchFieldException e)
    {
      s = s + " #### NoSuchFieldException #### ";
    }
    
    return s;
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
   * Fetches Order Time
   * @return Order Time as Date.
   */
  public Date getOrderedTime()
  {
    return(dOrderedTime);
  }

  /**
   * Fetches Scheduled Date
   * @return Scheduled date as Date.
   */
  public Date getScheduledDate()
  {
    return(dScheduledDate);
  }

  /**
   * Fetches Allocation Time
   * @return Allocation time as java.util.Date.
   */
  public Date getShortOrderCheckTime()
  {
    return(dShortOrderCheckTime);
  }

  /**
   * Fetches Priority Order
   * @return Priority as integer.
   */
  public int getPriority()
  {
    return(iPriority);
  }

  /**
   * Fetches Order Type
   * @return Order Type as integer. Return Default of ITEMORDER if not set.
   */
  public int getOrderType()
  {
    return(iOrderType);
  }

  /**
   * Fetches Destination Station
   * @return DestinationStation as string
   */
  public String getDestinationStation()
  {
    return(sDestinationStation);
  }

  /**
   * Fetches Order Description
   * @return Description as string
   */
  public String getDescription()
  {
    return(sDescription);
  }

  /**
   * Fetches Order Status
   * @return Order Status as integer. Return Default of ORBUILDING if not set.
   */
  public int getOrderStatus()
  {
    return(iOrderStatus);
  }

  /**
   * Fetches Next Status order should assume (usually used from Host messaging)
   * @return Order Status as integer. Return Default of ORBUILDING if not set.
   */
  public int getNextStatus()
  {
    return(iNextStatus);
  }

  /**
   * Fetches Ship Customer ID for the order.
   * @return Ship Customer ID as string
   */
  public String getShipCustomer()
  {
    return(sShipCustomer);
  }

  /**
   * Fetches Order Message for the order.
   * @return Order Message as string
   */
  public String getOrderMessage()
  {
    return(sOrderMessage);
  }

  /**
   * Fetches Release To Code for the order.
   * @return Release To Code as string
   */
  public String getReleaseToCode()
  {
    return sReleaseToCode;
  }

  /**
   * Fetches Carrier ID for the order.
   * @return Carrier ID as string
   */
  public String getCarrierID()
  {
    return(sCarrierID);
  }

  /**
   * Fetches Destination Warehouse for the order.
   * @return Destination Warehouse as string
   */
  public String getDestWarehouse()
  {
    return(sDestWarehouse);
  }

  /**
   * Fetches Destination Address for the order.
   * @return Destination Address as string
   */
  public String getDestAddress()
  {
    return(sDestAddress);
  }

  /**
   * Fetches the Order Line count.
   * @return the line count.
   */
  public int getHostLineCount()
  {
    return(iHostLineCount);
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
    addColumnObject(new ColumnObject(ORDERID_NAME, isOrderID));
  }

  /**
   * Sets Order creation time.
   */
  public void setOrderedTime(Date ipOrderedTime)
  {
    dOrderedTime = ipOrderedTime;
    addColumnObject(new ColumnObject(ORDEREDTIME_NAME, ipOrderedTime));
  }

  /**
   * Sets scheduled ship date.
   */
  public void setScheduledDate(Date ipScheduledDate)
  {
    dScheduledDate = ipScheduledDate;
    addColumnObject(new ColumnObject(SCHEDULEDDATE_NAME, ipScheduledDate));
  }

  /**
   * Sets the time when the allocator last tried to allocate this order.
   */
  public void setShortOrderCheckTime(Date ipShortOrderCheckTime)
  {
    dShortOrderCheckTime = ipShortOrderCheckTime;
    addColumnObject(new ColumnObject(SHORTORDERCHECKTIME_NAME, ipShortOrderCheckTime));
  }

  /**
   * Sets Order priority
   */
  public void setPriority(int inPriority)
  {
    iPriority = inPriority;
    addColumnObject(new ColumnObject(PRIORITY_NAME,
      Integer.valueOf(inPriority)));
  }

  /**
   * Sets Order Type
   */
  public void setOrderType(int inOrderType)
  {
    try
    {
      DBTrans.getStringValue("iOrderType", inOrderType);
    }
    catch(NoSuchFieldException e)
    {                                  // Passed value wasn't valid. Default it
      inOrderType = DBConstants.ITEMORDER;
    }
    iOrderType = inOrderType;
    addColumnObject(new ColumnObject(ORDERTYPE_NAME,
      Integer.valueOf(inOrderType)));
  }

  /**
   * Sets Destination Station value.
   */
  public void setDestinationStation(String isDestinationStation)
  {
    sDestinationStation = checkForNull(isDestinationStation);
    addColumnObject(new ColumnObject(DESTINATIONSTATION_NAME,
        isDestinationStation));
  }

  /**
   * Sets Order Description value.
   */
  public void setDescription(String isDescription)
  {
    sDescription = checkForNull(isDescription);
    addColumnObject(new ColumnObject(DESCRIPTION_NAME, isDescription));
  }

  /**
   * Sets Order Status.
   * @param inOrderStatus <code>int</code> containing translation value for order
   *        status.
   */
  public void setOrderStatus(int inOrderStatus)
  {
    try
    {
      DBTrans.getStringValue(ORDERSTATUS_NAME, inOrderStatus);
    }
    catch(NoSuchFieldException e)
    {                                  // Passed value wasn't valid. Default it
      inOrderStatus = DBConstants.ORBUILDING;
    }
    iOrderStatus = inOrderStatus;
    addColumnObject(new ColumnObject(ORDERSTATUS_NAME, inOrderStatus));
  }

  /**
   * Sets Next Order Status the order should assume once order line adds are
   * completed.
   * @param inNextStatus <code>int</code> containing translation value for order
   *        status.
   */
  public void setNextStatus(int inNextStatus)
  {
    try
    {
      DBTrans.getStringValue(NEXTSTATUS_NAME, inNextStatus);
    }
    catch(NoSuchFieldException e)
    {                                  // Passed value wasn't valid. Default it
      inNextStatus = DBConstants.HOLD;
    }
    iNextStatus = inNextStatus;
    addColumnObject(new ColumnObject(NEXTSTATUS_NAME,
      Integer.valueOf(inNextStatus)));
  }

  /**
   * Sets Ship Customer ID for the order.
   */
  public void setShipCustomer(String isShipCustomer)
  {
    sShipCustomer = checkForNull(isShipCustomer);
    addColumnObject(new ColumnObject(SHIPCUSTOMER_NAME, isShipCustomer));
  }

  /**
   * Sets Order Message for the order.
   */
  public void setOrderMessage(String isOrderMessage)
  {
    sOrderMessage = checkForNull(isOrderMessage);
    addColumnObject(new ColumnObject(ORDERMESSAGE_NAME, isOrderMessage));
  }

  /**
   * Sets Release To Code for the order.
   */
  public void setReleaseToCode(String isReleaseToCode)
  {
    sReleaseToCode = checkForNull(isReleaseToCode);
    addColumnObject(new ColumnObject(RELEASETOCODE_NAME, isReleaseToCode));
  }

  /**
   * Sets Carrier ID for the order.
   */
  public void setCarrierID(String isCarrierID)
  {
    sCarrierID = checkForNull(isCarrierID);
    addColumnObject(new ColumnObject(CARRIERID_NAME, isCarrierID));
  }

  /**
   * Sets Destination Warehouse for the order.
   */
  public void setDestWarehouse(String isDestWarehouse)
  {
    sDestWarehouse = checkForNull(isDestWarehouse);
    addColumnObject(new ColumnObject(DESTWAREHOUSE_NAME, isDestWarehouse));
  }

  /**
   * Sets Destination Address for the order.
   */
  public void setDestAddress(String isDestAddress)
  {
    sDestAddress = checkForNull(isDestAddress);
    addColumnObject(new ColumnObject(DESTADDRESS_NAME, isDestAddress));
  }

  /**
   *  Method sets the Host Line count for order lines.  This number represents
   *  a contract between our system and the host on how many lines they will be
   *  sending, and is used for validation.
   *
   *  @param inhostLineCount <code>int</code> containing the Host Line Count.
   */
  public void setHostLineCount(int inhostLineCount)
  {
    iHostLineCount = inhostLineCount;
    addColumnObject(new ColumnObject(HOSTLINECOUNT_NAME, Integer.valueOf(inhostLineCount)));
  }

  /**
   * Returns the Enum of the specified Column Name.
   * @param isColName
   * @return
   */
  public TableEnum getEnum(String isColName)
  {
    return mpColumnMap.get(isColName);
  }
  
 /**
   * Required set field method. This method figures out what column was passed
   * to it and sets the value. This allows us to have a generic method for all
   * DB interfaces.
   */
  @Override
  public int setField(String isColName, Object ipColValue)
  {
    TableEnum vpEnum = mpColumnMap.get(isColName);
                                       // Special case for when the user specified
    if (vpEnum == null)                // column is unknown.
    {
      return super.setField(isColName, ipColValue);
    }
    
    switch((OrderHeaderEnum)vpEnum)
    {
      case ORDERID:
        setOrderID((String)ipColValue);
        break;

      case DESTINATIONSTATION:
        setDestinationStation((String)ipColValue);
        break;

      case DESCRIPTION:
        setDescription((String)ipColValue);
        break;

      case SHIPCUSTOMER:
        setShipCustomer((String)ipColValue);
        break;

      case PRIORITY:
        setPriority((Integer)ipColValue);
        break;

      case HOSTLINECOUNT:
        setHostLineCount((Integer)ipColValue);
        break;

      case ORDERTYPE:
        setOrderType((Integer)ipColValue);
        break;

      case ORDERSTATUS:
        setOrderStatus((Integer)ipColValue);
        break;

      case NEXTSTATUS:
        setNextStatus((Integer)ipColValue);
        break;

      case ORDEREDTIME:
        setOrderedTime((Date)ipColValue);
        break;

      case SCHEDULEDDATE:
        setScheduledDate((Date)ipColValue);
        break;

      case SHORTORDERCHECKTIME:
        setShortOrderCheckTime((Date)ipColValue);
        break;

      case ORDERMESSAGE:
        setOrderMessage((String)ipColValue);
        break;

      case RELEASETOCODE:
        setReleaseToCode((String)ipColValue);
        break;

      case CARRIERID:
        setCarrierID((String)ipColValue);
        break;

      case DESTWAREHOUSE:
        setDestWarehouse((String)ipColValue);
        break;

      case DESTADDRESS:
        setDestAddress((String)ipColValue);
        break;
    }

    return(0);
  }
}
