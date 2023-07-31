package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.log.Logger;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:<BR>
 *   Class to handle Purchase Order Line Data operations.
 *
 * @author       sbw
 * @version      1.0
 * @since 30-May-04
 */
public class PurchaseOrderLineData extends AbstractSKDCData
{
                                       // Formalize Pseudo-Column Names.
  public static final String ACCEPTQUANTITY_NAME = "FACCEPTQUANTITY";
  public static final String DATATYPE_NAME = "SDATATYPE";
                                       // Used for Store screen related stuff.
  public static final String SCREEN_DATA_MNEMONIC = "ER";
                                       // Formalize DB column Names.
  public static final String CASEQUANTITY_NAME     = CASEQUANTITY.getName();
  public static final String EXPECTEDQUANTITY_NAME = EXPECTEDQUANTITY.getName();    
  public static final String EXPIRATIONDATE_NAME   = EXPIRATIONDATE.getName();
  public static final String HOLDREASON_NAME       = HOLDREASON.getName();
  public static final String INSPECTION_NAME       = INSPECTION.getName();
  public static final String ITEM_NAME             = ITEM.getName();
  public static final String LASTLINE_NAME         = LASTLINE.getName();
  public static final String LINEID_NAME           = LINEID.getName();
  public static final String LOADID_NAME           = LOADID.getName();
  public static final String LOT_NAME              = LOT.getName();
  public static final String ORDERID_NAME          = ORDERID.getName();
  public static final String RECEIVEDQUANTITY_NAME = RECEIVEDQUANTITY.getName();    
  public static final String ROUTEID_NAME          = ROUTEID.getName();
  public static final String GLOBALID_NAME         = GLOBALID.getName();

/*---------------------------------------------------------------------------
                 Database fields for PurchaseOrderLine table.
  ---------------------------------------------------------------------------*/
  private String sOrderID          = "";
  private String sItem             = "";
  private String sLoadID           = "";
  private String sLot              = "";
  private String sRouteID          = "";
  private double fExpectedQuantity = 0;
  private double fReceivedQuantity = 0;
  private double fCaseQuantity     = 0;
  private int iInspection          = DBConstants.NO;
  private String sHoldReason       = "";
  private String sLineID           = "";
  private Date dExpirationDate     = new Date();
  private String   sLastLine         = "NO";
  private String sGlobalID         = "";
  public static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  public PurchaseOrderLineData()
  {
    super();
    initColumnMap(mpColumnMap, PurchaseOrderLineEnum.class);
  }

  /**
   * This helps in debugging when we want to print the whole structure.
   */
  @Override
  public String toString()
  {
    String s = 
        "sOrderID: = " + sOrderID +
        "sItem: " +  sItem +
        "sLot: " + sLot +
        "sLoadID: " + sLoadID +
        "dExpectedQuantity: " + fExpectedQuantity +
        "dReceivedQuantity: " + fReceivedQuantity +
        "dCaseQuantity: "     + fCaseQuantity +
        "sHoldReason: "       + sHoldReason +
        "sRouteID: "          + sRouteID +
        "dExpirationDate: " + sdf.format(dExpirationDate) +
        "sLineID: " + sLineID +
        "sLastLine: " + sLastLine + 
        "sGlobalID: " + sGlobalID;

    try
    {
      s = s + "iInspection:"
          + DBTrans.getStringValue(INSPECTION.getName(), iInspection);
    }
    catch (NoSuchFieldException e)
    {
      s = s + "0";
    }
    s += super.toString();

    return(s);
  }

  /**
   * Defines equality between two PurchaseOrderLineData objects.
   *
   * @param  absOL <code>AbstractSKDCData</code> reference whose runtime type
   *         is expected to be <code>PurchaseOrderLineData</code>
   */
  @Override
  public boolean equals(AbstractSKDCData absPOL)
  {
    boolean rtn = false;

    PurchaseOrderLineData pol = (PurchaseOrderLineData)absPOL;

                                       // If the order doesn't even match...
    if (getOrderID().equals(pol.getOrderID()) == false)
    {
      return(false);
    }
                                 // Order Line is Item based.
    if (getItem().equals(pol.getItem()) &&
        getLot().equals(pol.getLot()))
    {
      rtn = true;
    }
    
    return(rtn);
  }

  /**
   * Resets the data in this class to the default.
   */
  @Override
  public void clear()
  {
    super.clear();                     // Pull in default behaviour.

    sOrderID          = "";
    sItem             = "";
    sLot              = "";
    sLoadID           = "";
    sRouteID          = "";
    fExpectedQuantity = 0;
    fReceivedQuantity = 0;
    fCaseQuantity     = 0;
    iInspection       = DBConstants.NO;
    sHoldReason       = "";
    sLineID           = "";
    sLastLine         = "NO";
    dExpirationDate   = new Date();
    sGlobalID         = "";
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

    try
    {
      String vsOld = "";
      String vsNew = "";

      // Construct Action Description string
      switch ((PurchaseOrderLineEnum)mpColumnMap.get(isColName))
      {
        case INSPECTION:
          vsOld = DBTrans.getStringValue(isColName, ((Integer)ipOld).intValue());
          vsNew = DBTrans.getStringValue(isColName, ((Integer)ipNew).intValue());
          break;

        default:
          vsOld = ipOld.toString();
          vsNew = ipNew.toString();
      }
      s = s + " Old [" + vsOld + "] New [" + vsNew + "]";
    }
    catch (NoSuchFieldException e)
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
   * Fetches Order Line Item
   * @return Item as string
   */
  public String getItem()
  {
    return(sItem);
  }

    /**
   * Fetches Order Line Load
   * @return Load ID as string
   */
  public String getLoadID()
  {
    return(sLoadID);
  }

  /**
   * Fetches Order Lot
   * @return Order Lot as string
   */
  public String getLot()
  {
    return(sLot);
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
   * Fetches Expected quantity for line.
   * @return Expected Quantity as a double.
   */
  public double getExpectedQuantity()
  {
    return(fExpectedQuantity);
  }

  /**
   * Fetches Received quantity .
   * @return Received Quantity as a double.
   */
  public double getReceivedQuantity()
  {
    return(fReceivedQuantity);
  }

  /** 
   * Fetches Case size
   * @return Case size as a double.
   */
  public double getCaseQuantity()
  {
    return(fCaseQuantity);
  }
  /**
   * Fetches Inspection
   * @return Inspection as an int
   */
  public int getInspection()
  {
    return(iInspection);
  }

  /**
   * Fetches HoldReason
   * @return HoldReason as string
   */
  public String getHoldReason()
  {
    return(sHoldReason);
  }

  /**
   * Fetches ExpirationDate
   * @return ExpirationDate as a Date
   */
  public Date getExpirationDate()
  {
    return(dExpirationDate);
  }

  /**
   * Fetches LineID
   * @return LineID as string
   */
  public String getLineID()
  {
    return(sLineID);
  }

  /**
   *  Gets the flag indicating if this is the last line of an order.
   * @return translation Yes or NO.
   */
   public int getLastLine()
   {
     return((sLastLine.equalsIgnoreCase("NO")) ? DBConstants.NO : DBConstants.YES);
   }
   
   /**
    * Fetches GlobalID
    * @return GlobalID as string
    */
    public String getGlobalID()
    {
        return(sGlobalID);
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
   * Sets Order Item value.
   */
  public void setItem(String isItem)
  {
    sItem = checkForNull(isItem);
    addColumnObject(new ColumnObject(ITEM.getName(), isItem));
  }

  /**
   * Sets Order Load value.
   */
  public void setLoadID(String isLoadID)
  {
    sLoadID = checkForNull(isLoadID);
    addColumnObject(new ColumnObject(LOADID.getName(), isLoadID));
  }

  /**
   * Sets Lot value.
   */
  public void setLot(String isLot)
  {
    sLot = checkForNull(isLot);
    addColumnObject(new ColumnObject(LOT.getName(), isLot));
  }

  /**
   * Sets Route ID for the order load.
   */
  public void setRouteID(String isRouteID)
  {
    sRouteID = checkForNull(isRouteID);
    addColumnObject(new ColumnObject(ROUTEID.getName(), isRouteID));
  }


  /**
   * Sets Line ID
   */
  public void setLineID(String isLineID)
  {
    sLineID = checkForNull(isLineID);
    addColumnObject(new ColumnObject(LINEID.getName(), isLineID));
  }

  /**
   * Sets Expected Quantity
   */
  public void setExpectedQuantity(double idExpectedQuantity)
  {
    fExpectedQuantity = idExpectedQuantity;
    addColumnObject(new ColumnObject(EXPECTEDQUANTITY.getName(),
      Double.valueOf(idExpectedQuantity)));
  }

  /**
   * Sets Received Quantity for a line item 
   */
  public void setReceivedQuantity(double idReceivedQuantity)
  {
    fReceivedQuantity = idReceivedQuantity;
    addColumnObject(new ColumnObject(RECEIVEDQUANTITY.getName(),
      Double.valueOf(idReceivedQuantity)));
  }

  /**
   * Sets Case size for a line item
   * 
   * @param dCaseQuantity
   */
  public void setCaseQuantity(double idCaseQuantity)
  {
    fCaseQuantity = idCaseQuantity;
    addColumnObject(new ColumnObject(CASEQUANTITY.getName(), Double.valueOf(idCaseQuantity)));
  }

  /**
   * Sets ExpirationDate for a line item.
   */
  public void setExpirationDate(Date ipExpirationDate)
  {
    dExpirationDate = ipExpirationDate;
    addColumnObject(new ColumnObject(EXPIRATIONDATE.getName(),ipExpirationDate));
  }

  /**
   * Sets Inspection Flag value
   */
  public void setInspection(int inInspection)
  {
    try
    {
      DBTrans.getStringValue("iInspection", inInspection);
    }
    catch(NoSuchFieldException e)
    {                                  // Passed value wasn't valid. Default it
      inInspection = DBConstants.NO;
    }
    iInspection = inInspection;
    addColumnObject(new ColumnObject(INSPECTION.getName(), Integer.valueOf(inInspection)));
  }

 /**
  *  Sets the last record line mark for a host message. <b>Note:</b> this field
  *  is not in the database! This is used only for storage purposes in this 
  *  object.
  *  @param isLastLine Parameter indicating if this is the last line of an order.
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
   * Sets Hold Reason
   */
  public void setHoldReason(String isHoldReason)
  {
    sHoldReason = isHoldReason;
    addColumnObject(new ColumnObject(HOLDREASON.getName(), isHoldReason));
  }
  
  /**
   * Sets Global Id
   */
  public void setGlobalID(String isGlobalID)
  {
    sGlobalID = checkForNull(isGlobalID);
    addColumnObject(new ColumnObject(GLOBALID.getName(), isGlobalID));
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
      return(super.setField(isColName, ipColValue));
    }

    switch((PurchaseOrderLineEnum)vpEnum)
    {
      case CASEQUANTITY:
        setCaseQuantity((Double)ipColValue);
        break;

      case EXPECTEDQUANTITY:
        setExpectedQuantity((Double)ipColValue);
        break;

      case EXPIRATIONDATE:
        setExpirationDate((Date)ipColValue);
        break;

      case HOLDREASON:
        setHoldReason((String)ipColValue);
        break;

      case INSPECTION:
        setInspection((Integer)ipColValue);
        break;

      case ITEM:
        setItem((String)ipColValue);
        break;

      case LASTLINE:
        setLastLine((String)ipColValue);
        break;

      case LINEID:
        setLineID((String)ipColValue);
        break;

      case LOADID:
        setLoadID((String)ipColValue);
        break;

      case LOT:
        setLot((String)ipColValue);
        break;

      case ORDERID:
        setOrderID((String)ipColValue);
        break;

      case RECEIVEDQUANTITY:
        setReceivedQuantity((Double)ipColValue);
        break;

      case ROUTEID:
        setRouteID((String)ipColValue);
        
      case GLOBALID:
          setGlobalID((String)ipColValue);
    }

    return(0);
  }
}
