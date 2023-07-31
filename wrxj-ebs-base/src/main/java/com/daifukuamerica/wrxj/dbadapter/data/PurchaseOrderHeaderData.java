package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:<BR>
 *   Class to handle Purchase Order Header Data operations.
 *
 * @author       A.D.
 * @version      1.0
 * @since  29-May-01
 */
public class PurchaseOrderHeaderData extends AbstractSKDCData
{
  public static final String EXPECTEDDATE_NAME     = EXPECTEDDATE.getName();
  public static final String HOSTLINECOUNT_NAME    = HOSTLINECOUNT.getName();
  public static final String LASTACTIVITYTIME_NAME = LASTACTIVITYTIME.getName();
  public static final String ORDERID_NAME          = ORDERID.getName();
  public static final String PURCHASEORDERSTATUS_NAME = PURCHASEORDERSTATUS.getName();
  public static final String VENDORID_NAME         = VENDORID.getName();
  public static final String STORESTATION_NAME     = STORESTATION.getName();
  public static final String FINALSORTLOCATIONID_NAME     = FINALSORTLOCATIONID.getName();


/*---------------------------------------------------------------------------
                 Database fields for OrderHeader table.
  ---------------------------------------------------------------------------*/
  private String sOrderID            = "";
  private String sStoreStation       = "";
  private String sVendorID           = "";
  private Date dExpectedDate         = new Date();
  private int iPurchaseOrderStatus   = DBConstants.ERBUILDING;
  private int iHostLineCount         = 0;
  private Date dLastActivityTime     = new Date();
  private String finalSortLocationId      = "";
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  public PurchaseOrderHeaderData()
  {
    super();
    initColumnMap(mpColumnMap, PurchaseOrderHeaderEnum.class);
  }

 /**
  * This helps in debugging when we want to print the whole structure.
  */
  @Override
  public String toString()
  {
    String s = "sOrderID:"           + sOrderID                      +
               "\nsStoreStation: "   + sStoreStation                 + 
               "\nsVendorID: "       + sVendorID                     +
               "\ndExpectedDate: "   + sdf.format(dExpectedDate)     +
               "\ndLastActivityTime" + sdf.format(dLastActivityTime) +
               "\niHostLineCount: "  + iHostLineCount +
               "\nfinalSortLocationId" + finalSortLocationId + "\n";

    try
    {
      s = s + "iPurchaseOrderStatus:" + 
          DBTrans.getStringValue(PURCHASEORDERSTATUS.getName(), iPurchaseOrderStatus);
    }
    catch(NoSuchFieldException e)
    {
      s = s + "0";
    }

    s = s +  "\n\n" + super.toString();

    return(s);
  }

 /**
  *  Method to make a deep copy of this object.
  *
  *  @return copy of <code>PurchaseOrderHeaderData</code>.
  */
  @Override
  public PurchaseOrderHeaderData clone()
  {
    PurchaseOrderHeaderData vpClonedData = (PurchaseOrderHeaderData)super.clone();
    vpClonedData.dLastActivityTime = (Date)dLastActivityTime.clone();
    vpClonedData.dExpectedDate = (Date)dExpectedDate.clone();

    return vpClonedData;
  }

 /**
  * Defines equality between two OrderHeaderData objects.
  *
  * @param  absPo <code>AbstractSKDCData</code> reference whose runtime type
  *         is expected to be <code>OrderHeaderData</code>
  */
  @Override
  public boolean equals(AbstractSKDCData absPoh)
  {
    PurchaseOrderHeaderData poh = (PurchaseOrderHeaderData)absPoh;
    return(getOrderID().equals(poh.getOrderID()));
  }

 /**
  * Resets the data in this class to the default.
  */
  @Override
  public void clear()
  {
    super.clear();                     // Pull in default behaviour.

    sOrderID = "";
    sVendorID = "";
    sStoreStation = "";
    iPurchaseOrderStatus = DBConstants.ORBUILDING;
    finalSortLocationId = "";
    iHostLineCount      = 0;
    dLastActivityTime.setTime(System.currentTimeMillis());
    dExpectedDate.setTime(System.currentTimeMillis());
  }

  /**
   * This generates the string for the field that is changed.
   * 
   * @param isColName
   * @param ipOld
   * @param ipNew
   */
  @Override
  public String getActionDesc(String isColName, Object ipOld, Object ipNew)
  {
    String s = "Field [" + isColName + "]:";

    String vsOld = ipOld.toString();
    String vsNew = ipNew.toString();

    s = s + " Old [" + vsOld + "] New [" + vsNew + "]";
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
  * Fetches Store Station
  * @return sStoreStation the Store station value.
  */
  public String getStoreStation()
  {
    return(sStoreStation);
  }

 /**
  * Fetches Vendor ID
  * @return VendorID as string
  */
  public String getVendorID()
  {
    return(sVendorID);
  }

 /**
  * Fetches LastActivityTime 
  * @return LastActivityTime as Date.
  */
  public Date getLastActivityTime()
  {
    return(dLastActivityTime);
  }

 /**
  * Fetches Expected Date
  * @return Expected date as Date.
  */
  public Date getExpectedDate()
  {
    return(dExpectedDate);
  }

 /**
  * Fetches Order Status
  * @return Order Status as integer. Return Default of ORBUILDING if not set.
  */
  public int getOrderStatus()
  {
    return(iPurchaseOrderStatus);
  }

 /**
  * Fetches the Purchase Order Line count as expected from the host.
  * @return the line count.
  */
  public int getHostLineCount()
  {
    return(iHostLineCount);
  }
  
  /**
   * Fetches final sort location id
   * @return The current final sort location id of the current POH record
   */
  public String getFinalSortLocationId()
  {
      return finalSortLocationId;
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
    addColumnObject(new ColumnObject(ORDERID.getName(), isOrderID));
  }

 /**
  * Sets Storage Station value.
  */
  public void setStoreStation(String isStoreStation)
  {
    sStoreStation = checkForNull(isStoreStation);
    addColumnObject(new ColumnObject(STORESTATION.getName(), isStoreStation));
  }

  /**
  * Sets Vendor ID value.
  */
  public void setVendorID(String isVendorID)
  {
    sVendorID = isVendorID;
    addColumnObject(new ColumnObject(VENDORID.getName(), isVendorID));
  }

 /**
  * Sets Order LastActivity time.
  */
  public void setLastActivityTime(Date ipLastActivityTime)
  {
    dLastActivityTime = ipLastActivityTime;
    addColumnObject(new ColumnObject(LASTACTIVITYTIME.getName(), ipLastActivityTime));
  }

 /**
  * Sets scheduled ship date.
  */
  public void setExpectedDate(Date ipExpectedDate)
  {
    dExpectedDate = ipExpectedDate;
    addColumnObject(new ColumnObject(EXPECTEDDATE.getName(), ipExpectedDate));
  }

 /**
  * Sets Purchase Order Status.
  * @param iPurchaseOrderStatus <code>int</code> containing translation value for
  * Purchase order status.
  */
  public void setOrderStatus(int iOrderStatus)
  {
    if(iOrderStatus != SKDCConstants.ALL_INT)
    {
      try
      {
        DBTrans.getStringValue("iPurchaseOrderStatus", iOrderStatus);
      }
      catch(NoSuchFieldException e)
      {                                  // Passed value wasn't valid. Default it
        iOrderStatus = DBConstants.ERBUILDING;
      }
    }
    iPurchaseOrderStatus = iOrderStatus;
    addColumnObject(new ColumnObject(PURCHASEORDERSTATUS.getName(),
      Integer.valueOf(iOrderStatus)));
  }

 /**
  *  Method sets the Host Line count for order lines.  This number represents
  *  a contract between our system and the host on how many lines they will be
  *  sending, and is used for validation.
  *
  *  @param ihostLineCount <code>int</code> containing the Host Line Count.
  */
  public void setHostLineCount(int ihostLineCount)
  {
    iHostLineCount = ihostLineCount;
    addColumnObject(new ColumnObject(HOSTLINECOUNT.getName(), Integer.valueOf(ihostLineCount)));
  }
  
  public void setFinalSortLocationId(String finalSortLocationId)
  {
    this.finalSortLocationId = finalSortLocationId;
    addColumnObject(new ColumnObject(FINALSORTLOCATIONID.getName(),  finalSortLocationId));
  }

 /**
  *  Required set field method.  This method figures out what column was
  *  passed to it and sets the value.  This allows us to have a generic
  *  method for all DB interfaces.
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
    
    switch((PurchaseOrderHeaderEnum)vpEnum)
    {
      case EXPECTEDDATE:
        setExpectedDate((Date)ipColValue);
        break;

      case HOSTLINECOUNT:
        setHostLineCount((Integer)ipColValue);
        break;

      case LASTACTIVITYTIME:
        setLastActivityTime((Date)ipColValue);
        break;

      case ORDERID:
        setOrderID((String)ipColValue);
        break;

      case PURCHASEORDERSTATUS:
        setOrderStatus((Integer)ipColValue);
        break;

      case VENDORID:
        setVendorID((String)ipColValue);
        
      case STORESTATION:
          setStoreStation((String)ipColValue);
          break;
          
      case FINALSORTLOCATIONID:
          setFinalSortLocationId((String)ipColValue);
          break;
    }

    return(0);
  }
}
