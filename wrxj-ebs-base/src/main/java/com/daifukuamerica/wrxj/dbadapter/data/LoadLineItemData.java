package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import static com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:<BR>
 *   Class to handle Load Line Item Data operations.  This class treats columns, and
 *   Keys as Objects.
 *
 * @author       A.T.
 * @version      1.0
 */
public class LoadLineItemData extends AbstractSKDCData
{
  public static final String ACCEPTQUANTITY_NAME    = "FACCEPTQUANTITY";
  public static final String DATATYPE_NAME          = "SDATATYPE";
                                       // Used for Store screen related stuff.
  public static final String SCREEN_DATA_MNEMONIC = "ID";
  
  public static final String AGINGDATE_NAME         = AGINGDATE.getName();
  public static final String ALLOCATEDQUANTITY_NAME = ALLOCATEDQUANTITY.getName();
  public static final String CURRENTQUANTITY_NAME   = CURRENTQUANTITY.getName();
  public static final String EXPECTEDRECEIPT_NAME   = EXPECTEDRECEIPT.getName();
  public static final String EXPIRATIONDATE_NAME    = EXPIRATIONDATE.getName();
  public static final String HOLDREASON_NAME        = HOLDREASON.getName();
  public static final String HOLDTYPE_NAME          = HOLDTYPE.getName();
  public static final String ITEM_NAME              = ITEM.getName();
  public static final String LASTCCIDATE_NAME       = LASTCCIDATE.getName();
  public static final String LINEID_NAME            = LINEID.getName();
  public static final String LOADID_NAME            = LOADID.getName();
  public static final String LOT_NAME               = LOT.getName();
  public static final String POSITIONID_NAME        = POSITIONID.getName();
  public static final String ORDERID_NAME           = ORDERID.getName();
  public static final String ORDERLOT_NAME          = ORDERLOT.getName();
  public static final String PRIORITYALLOCATION_NAME = PRIORITYALLOCATION.getName();
  public static final String GLOBALID_NAME          = GLOBALID.getName();
  public static final String EXPECTEDDATE_NAME      = EXPECTEDDATE.getName();
  
  private String sItem = "";
  private String sLot = "";
  private String sLoadID = "";
  private String sPositionID = "";
  private String sHoldReason = "";
  private String sOrderID = "";
  private String sExpectedReceipt = "";
  private String sOrderLot = "";
  private String sLineID = "";
  private Date   dLastCCIDate        = new Date();
  private Date   dAgingDate          = new Date();
  private Date   dExpirationDate     = new Date();
  private double fCurrentQuantity    = 0;
  private double fAllocatedQuantity  = 0;
  private int    iHoldType           = DBConstants.ITMAVAIL;
  private int    iPriorityAllocation = DBConstants.NO;
  private String sGlobalID           = "";
  private Date   dExpectedDate		 = new Date();
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  public LoadLineItemData()
  {
    super();
    initColumnMap(mpColumnMap, LoadLineItemEnum.class);
  }

  /**
   * This helps in debugging when we want to print the whole structure.
   */
  @Override
  public String toString()
  {
    String s = "sItem:" + sItem +
               "\nsLot:" + sLot +
               "\nsLoadID:" + sLoadID +
               "\nsLine:" + sLineID +
               "\nsPositionID:" + sPositionID +
               "\nsHoldReason:" + sHoldReason +
               "\nsOrderID:" + sOrderID +
               "\nsOrderLot:" + sOrderLot +
               "\nsExpectedReceipt:" + sExpectedReceipt +
               "\ndLastCCIDate:" + sdf.format(dLastCCIDate).toString() +
               "\ndAgingDate:" + sdf.format(dAgingDate).toString() +
               "\nfCurrentQuantity:" + fCurrentQuantity +
               "\nfAllocatedQuantity:" + fAllocatedQuantity + 
               "\nsGlobalID:" + sGlobalID +
               "\ndExpectedDate:" + sdf.format(dExpectedDate).toString() +
               "\n";
    try
    {
      s = s + "iHoldType:" + DBTrans.getStringValue(HOLDTYPE_NAME, iHoldType) +
               "\niPriorityAllocation:" + DBTrans.getStringValue(PRIORITYALLOCATION_NAME,
                iPriorityAllocation) + "\n";
    }
    catch(NoSuchFieldException e)
    {
      s= s + "0";
    }
    s += super.toString();

    return(s);
  }

  /**
   *  Method to perform clone of <code>LoadData</code>.
   *
   *  @return copy of <code>LoadData</code>
   */
  @Override
  public LoadLineItemData clone()
  {
    LoadLineItemData vpClonedData = (LoadLineItemData)super.clone();
    vpClonedData.dAgingDate = (Date)dAgingDate.clone();
    vpClonedData.dLastCCIDate = (Date)dLastCCIDate.clone();

    return vpClonedData;
  }

  /**
   * Defines equality between two LoadLineItemData objects.
   *
   * @param  absID <code>AbstractSKDCData</code> reference whose runtime type
   *         is expected to be <code>LoadLineItemData</code>
   */
  @Override
  public boolean equals(AbstractSKDCData absID)
  {
    LoadLineItemData id = (LoadLineItemData)absID;
    return(id.sItem.equals(this.sItem) && id.sLoadID.equals(this.sLoadID) &&
           id.sLot.equals(this.sLot) && id.sLineID.equals(this.sLineID));
  }

  /**
   * Resets the data in this class to the default.
   */
  @Override
  public void clear()
  {
    super.clear();

    dLastCCIDate.setTime(System.currentTimeMillis());
    dAgingDate.setTime(System.currentTimeMillis());
    fCurrentQuantity    = 1;
    fAllocatedQuantity  = 0;
    iHoldType           = DBConstants.ITMAVAIL;
    iPriorityAllocation = DBConstants.NO;

    sLineID = "";
    sItem = "";
    sLot = "";
    sLoadID = "";
    sPositionID = "";
    sOrderID = "";
    sExpectedReceipt = "";
    sOrderLot = "";
    sHoldReason = "";
    sGlobalID = "";
    dExpectedDate.setTime(System.currentTimeMillis());
  }

/*---------------------------------------------------------------------------
                     Column value get methods go here.
  ---------------------------------------------------------------------------*/
  /**
   * Fetches Item
   * @return Item as string
   */
  public String getItem()
  {
    return sItem;
  }
  /**
   * Fetches Lot
   * @return Lot as string
   */
  public String getLot()
  {
    return sLot;
  }
  /**
   * Fetches Line number
   * @return Line number as a string
   */
  public String getLineID()
  {
    return sLineID;
  }
  /**
   * Fetches Load ID
   * @return Load ID as string
   */
  public String getPositionID()
  {
    return sPositionID;
  }
  
  public String getLoadID()
  {
    return sLoadID;
  }
  /**
   * Fetches Hold Reason
   * @return Hold Reason as string
   */
  public String getHoldReason()
  {
    return sHoldReason;
  }
  /**
   * Fetches Last CCI Date
   * @return Last CCI Date as Date
   */
  public Date getLastCCIDate()
  {
    return dLastCCIDate;
  }
  /**
   * Fetches Aging Date
   * @return Aging Date as Date
   */
  public Date getAgingDate()
  {
    return dAgingDate;
  }
  /**
   * Fetches Expiration Date
   * @return Expiration Date as Date
   */
  public Date getExpirationDate()
  {
    return dExpirationDate;
  }
  /**
   * Fetches Current Quantity
   * @return Current Quantity as double
   */
  public double getCurrentQuantity()
  {
    return fCurrentQuantity;
  }
  /**
   * Fetches AllocatedQuantity
   * @return AllocatedQuantity as double
   */
  public double getAllocatedQuantity()
  {
    return fAllocatedQuantity;
  }
  /**
   * Fetches Order ID
   * @return Order ID as string
   */
  public String getOrderID()
  {
    return sOrderID;
  }
  /**
   * Fetches ExpectedReceipt
   * @return ExpectedReceipt as string
   */
  public String getExpectedReceipt()
  {
    return sExpectedReceipt;
  }
  /**
   * Fetches Ordered Lot
   * @return Ordered Lot as string
   */
  public String getOrderLot()
  {
    return sOrderLot;
  }
  /**
   * Fetches Hold Type
   * @return Hold Type as integer
   */
  public int getHoldType()
  {
    return iHoldType;
  }
  /**
   * Fetches Priority Allocation
   * @return Priority Allocation as integer
   */
  public int getPriorityAllocation()
  {
    return iPriorityAllocation;
  }
  
  /**
   * Fetches GlobalID
   * @return GlobalID as string
   */
   public String getGlobalID()
   {
       return(sGlobalID);
   }
   /**
    * Fetches Expected Date 
    * @return Expected Date as Date
    */
   public Date getExpectedDate()
   {
     return dExpectedDate;
   }
  /*---------------------------------------------------------------------------
               ******** Column Setting methods go here. ********
  ---------------------------------------------------------------------------*/
  /**
   * Sets Item value.
   */
  public void setItem(String s)
  {
    sItem = checkForNull(s);
    addColumnObject(new ColumnObject(ITEM.getName(), sItem));
  }
  /**
   * Sets Lot value.
   */
  public void setLot(String s)
  {
    sLot = checkForNull(s);
    addColumnObject(new ColumnObject(LOT.getName(), sLot));
  }
  /**
   * Sets Line identifier.
   */
  public void setLineID(String s)
  {
    sLineID = checkForNull(s);
    addColumnObject(new ColumnObject(LINEID.getName(), sLineID));
  }
  public void setPositionID(String s)
  {
    sPositionID = checkForNull(s);
    addColumnObject(new ColumnObject(POSITIONID.getName(), sPositionID));
  }
  /**
   * Sets Load ID value.
   */
  public void setLoadID(String s)
  {
    sLoadID = checkForNull(s);
    addColumnObject(new ColumnObject(LOADID.getName(), sLoadID));
  }
  /**
   * Sets ExpectedReceipt value.
   */
  public void setExpectedReceipt(String s)
  {
    sExpectedReceipt = checkForNull(s);
    addColumnObject(new ColumnObject(EXPECTEDRECEIPT.getName(), sExpectedReceipt));
  }
  /**
   * Sets Order ID value.
   */
  public void setOrderID(String s)
  {
    sOrderID = checkForNull(s);
    addColumnObject(new ColumnObject(ORDERID.getName(), sOrderID));
  }
  /**
   * Sets Ordered Lot value.
   */
  public void setOrderLot(String s)
  {
    sOrderLot = checkForNull(s);
    addColumnObject(new ColumnObject(ORDERLOT.getName(), sOrderLot));
  }
  /**
   * Sets Hold Reason value.
   */
  public void setHoldReason(String s)
  {
    sHoldReason = checkForNull(s);
    addColumnObject(new ColumnObject(HOLDREASON.getName(), sHoldReason));
  }
  /**
   * Sets Hold Type value.
   */
  public void setHoldType(int i)
  {
    iHoldType = i;
    addColumnObject(new ColumnObject(HOLDTYPE.getName(), iHoldType));
  }
  /**
   * Sets Priority Allocation value.
   */
  public void setPriorityAllocation(int i)
  {
    iPriorityAllocation = i;
    addColumnObject(new ColumnObject(PRIORITYALLOCATION.getName(),
        iPriorityAllocation));
  }
  /**
   * Sets Aging Date value.
   */
  public void setAgingDate(Date ipAgingDate)
  {
    dAgingDate = ipAgingDate;
    addColumnObject(new ColumnObject(AGINGDATE.getName(), dAgingDate));
  }
  /**
   * Sets Expiration Date value.
   */
  public void setExpirationDate(Date ipExpirationDate)
  {
    dExpirationDate = ipExpirationDate;
    addColumnObject(new ColumnObject(EXPIRATIONDATE.getName(), dExpirationDate));
  }
  /**
   * Sets Last CCI Date value.
   */
  public void setLastCCIDate(Date ipLastCCIDate)
  {
    dLastCCIDate = ipLastCCIDate;
    addColumnObject(new ColumnObject(LASTCCIDATE.getName(), dLastCCIDate));
  }
  /**
   * Sets Allocated Quantity value.
   */
  public void setAllocatedQuantity(double f)
  {
    fAllocatedQuantity = f;
    addColumnObject(new ColumnObject(ALLOCATEDQUANTITY.getName(), fAllocatedQuantity));
  }
  /**
   * Sets Current Quantity value.
   */
  public void setCurrentQuantity(double f)
  {
    fCurrentQuantity = f;
    addColumnObject(new ColumnObject(CURRENTQUANTITY.getName(), fCurrentQuantity));
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
   * Sets Expected Date value.
   */
  public void setExpectedDate(Date ipExpectedDate)
  {
    dExpectedDate = ipExpectedDate;
    addColumnObject(new ColumnObject(EXPECTEDDATE.getName(), dExpectedDate));
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

    switch((LoadLineItemEnum)vpEnum)
    {
      case ITEM:
        setItem((String)ipColValue);
        break;

      case LOT:
        setLot((String)ipColValue);
        break;

      case LINEID:
        setLineID((String)ipColValue);
        break;
      
      case  POSITIONID:
      setPositionID((String) ipColValue);
      break;

      case LOADID:
        setLoadID((String)ipColValue);
        break;

      case HOLDREASON:
        setHoldReason((String)ipColValue);
        break;

      case ORDERID:
        setOrderID((String)ipColValue);
        break;

      case EXPECTEDRECEIPT:
        setExpectedReceipt((String)ipColValue);
        break;

      case ORDERLOT:
        setOrderLot((String)ipColValue);
        break;

      case LASTCCIDATE:
        setLastCCIDate(((Date)ipColValue));
        break;

      case AGINGDATE:
        setAgingDate(((Date)ipColValue));
        break;

      case EXPIRATIONDATE:
        setExpirationDate(((Date)ipColValue));
        break;

      case CURRENTQUANTITY:
        setCurrentQuantity((Double)ipColValue);
        break;

      case ALLOCATEDQUANTITY:
        setAllocatedQuantity((Double)ipColValue);
        break;

      case HOLDTYPE:
        setHoldType((Integer)ipColValue);
        break;

      case PRIORITYALLOCATION:
        setPriorityAllocation((Integer)ipColValue);
        break;
        
      case GLOBALID:
        setGlobalID((String)ipColValue);
        break;
        
      case EXPECTEDDATE:
    	setExpectedDate(((Date) ipColValue));
    	break; 
    }

    return(0);
  }
}
