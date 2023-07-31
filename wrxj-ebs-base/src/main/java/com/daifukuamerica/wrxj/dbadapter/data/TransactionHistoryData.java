package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:<BR>
 *   Class to handle Transaction Data operations.  This class treats columns, and
 *   Keys as Objects.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 07-Apr-03<BR>
 *     Copyright (c) 2003<BR>
 *     Company:  Daifuku America Corporation
 */
public class TransactionHistoryData extends AbstractSKDCData
{
  public static final String ACTIONDESCRIPTION_NAME = ACTIONDESCRIPTION.getName();
  public static final String ADJUSTEDQUANTITY_NAME  = ADJUSTEDQUANTITY.getName();
  public static final String AGINGDATE_NAME         = AGINGDATE.getName();
  public static final String AISLEGROUP_NAME        = AISLEGROUP.getName();
  public static final String CARRIERID_NAME         = CARRIERID.getName();
  public static final String CURRENTQUANTITY_NAME   = CURRENTQUANTITY.getName();
  public static final String CUSTOMER_NAME          = CUSTOMER.getName();
  public static final String DEVICEID_NAME          = DEVICEID.getName();
  public static final String EXPECTEDQUANTITY_NAME  = EXPECTEDQUANTITY.getName();
  public static final String EXPIRATIONDATE_NAME    = EXPIRATIONDATE.getName();
  public static final String HOLDTYPE_NAME          = HOLDTYPE.getName();
  public static final String ITEM_NAME              = ITEM.getName();
  public static final String LASTCCIDATE_NAME       = LASTCCIDATE.getName();
  public static final String LINEID_NAME            = LINEID.getName();
  public static final String LOADID_NAME            = LOADID.getName();
  public static final String LOCATION_NAME          = LOCATION.getName();
  public static final String LOT_NAME               = LOT.getName();
  public static final String MACHINENAME_NAME       = MACHINENAME.getName();
  public static final String ORDERID_NAME           = ORDERID.getName();
  public static final String ORDERLOT_NAME          = ORDERLOT.getName();
  public static final String ORDERTYPE_NAME         = ORDERTYPE.getName();
  public static final String PICKQUANTITY_NAME      = PICKQUANTITY.getName();
  public static final String REASONCODE_NAME        = REASONCODE.getName();
  public static final String RECEIVEDQUANTITY_NAME  = RECEIVEDQUANTITY.getName();
  public static final String ROLE_NAME              = ROLE.getName();
  public static final String ROUTEID_NAME           = ROUTEID.getName();
  public static final String SHIPDATE_NAME          = SHIPDATE.getName();
  public static final String STATION_NAME           = STATION.getName();
  public static final String TOLOAD_NAME            = TOLOAD.getName();
  public static final String TOLOCATION_NAME        = TOLOCATION.getName();
  public static final String TOSTATION_NAME         = TOSTATION.getName();
  public static final String TRANCATEGORY_NAME      = TRANCATEGORY.getName();
  public static final String TRANSDATETIME_NAME     = TRANSDATETIME.getName();
  public static final String TRANTYPE_NAME          = TRANTYPE.getName();
  public static final String USERID_NAME            = USERID.getName();

/*---------------------------------------------------------------------------
    Database fields for Transaction table. (NOTE: the transaction date-time
    will be defaulted to the curent system date time in the database.
  ---------------------------------------------------------------------------*/

  private int    iTranCategory = 0;
  private int    iTranType     = 0;
  private int    iSequence     = 0;
  private int    iOrderType    = 0;
  private int    iAisleGroup   = 0;
  private int    iHoldType     = 0;
  private String sLocation     = "";
  private String sToLocation   = "";
  private String sLoadID       = "";
  private String sToLoad       = "";
  private String sItem         = "";
  private String sLot          = "";
  private String sOrderID      = "";
  private String sUserID       = "";
  private String sRole         = "";
  private String sCustomer     = "";
  private String sStation      = "";
  private String sToStation    = "";
  private String sDeviceID     = "";
  private String sRouteID      = "";
  private String sCarrierID    = "";
  private String sLineID       = "";
  private String sOrderLot     = "";
  private String sReasonCode   = "";
  private String sMachineName  = "";
  private String sActionDescription   = "";

  private double fCurrentQuantity  = 0;
  private double fExpectedQuantity = 0;
  private double fReceivedQuantity = 0;
  private double fPickQuantity     = 0;
  private double fAdjustedQuantity = 0;

  private Date dTransDateTime  = new Date();
  private Date dLastCCIDate;
  private Date dAgingDate;
  private Date dExpirationDate;
  private Date dShipDate;
  
  // These are not data fields.  They're here to support weirdness in the 
  // transaction history screens.  Someday that will hopefully be changed.
  private Date mpStartingDate;
  private Date mpEndingDate;
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();


  public TransactionHistoryData()
  {
    super();
    initColumnMap(mpColumnMap, TransactionHistoryEnum.class);
  }

 /**
  * This helps in debugging when we want to print the whole structure.
  */
  @Override
  public String toString()
  {
    String s = "";
    try
    {
      s = "iTranCategory:" + DBTrans.getStringValue(TRANCATEGORY.getName(), iTranCategory);
      s = s + "\niTranType:" + DBTrans.getStringValue(TRANTYPE.getName(), iTranType) + "\n";
      s = s + "\niOrderType:" + DBTrans.getStringValue(ORDERTYPE.getName(), iOrderType) + "\n";
      s = s + "\niHoldType:" + DBTrans.getStringValue(HOLDTYPE.getName(), iHoldType) + "\n";
    }
    catch(NoSuchFieldException e)
    {
      s = s + "Translation Fail Error...";
    }

    s = s + "\niAisleGroup:" + iAisleGroup;   
    s = s + "\nsLocation:" + sLocation;    
    s = s + "\nsToLocation:" + sToLocation;  
    s = s + "\nsLoadID:" + sLoadID;      
    s = s + "\nsToLoad:" + sToLoad;      
    s = s + "\nsItem:" + sItem;        
    s = s + "\nsLot:" + sLot;         
    s = s + "\nsOrderID:" + sOrderID;     
    try
    {
      s = s + "\niOrderType:" + DBTrans.getStringValue("iOrderType", iOrderType) + "\n";
    }
    catch(NoSuchFieldException e)
    {
      s = s + "\niOrderType:Unknown";
    }
    s = s + "\nsUserID:" + sUserID;      
    s = s + "\nsRole:" + sRole;        
    s = s + "\nsMachineName:" + sMachineName;      
    s = s + "\nsCustomer:" + sCustomer;    
    s = s + "\nsStation:" + sStation;     
    s = s + "\nsToStation:" + sToStation;   
    s = s + "\nsDeviceID:" + sDeviceID;    
    s = s + "\nsDeviceID:" + sRouteID;     
    s = s + "\nsCarrierID:" + sCarrierID;
    s = s + "\nsLineID:" + sLineID;
    s = s + "\nsOrderLot:" + sOrderLot;
    try
    {
      s = s + "\niHoldType:" + DBTrans.getStringValue("iHoldType", iHoldType) + "\n";
    }
    catch(NoSuchFieldException e)
    {
      s = s + "\niHoldType:Unknown";
    }
    s = s + "\nsReasonCode:" + sReasonCode;

    s = s + "\nfCurrentQuantity:" + fCurrentQuantity;  
    s = s + "\nfExpectedQuantity:" + fExpectedQuantity;
    s = s + "\nfReceivedQuantity:" + fReceivedQuantity; 
    s = s + "\nfPickQuantity:" + fPickQuantity;     
    s = s + "\nfAdjustedQuantity:" + fAdjustedQuantity; 

    s = s + "\ndTransDateTime:" + dTransDateTime;
    s = s + "\niSequence:" + iSequence;
    s = s + "\ndLastCCIDate:" + dLastCCIDate;
    s = s + "\ndAgingDate:" + dAgingDate;
    s = s + "\ndExpirationDate:" + dExpirationDate;
    s = s + "\ndShipDate:" + dShipDate;
    s = s + "\nsActionDescription:" + sActionDescription;

    s += super.toString();

    return(s);
  }

 /**
  * Defines equality between two TransactionHistoryData objects.
  *
  * @param  absTN <code>AbstractSKDCData</code> reference whose runtime type
  *         is expected to be <code>TransactionHistoryData</code>
  */
  @Override
  public boolean equals(AbstractSKDCData absTN)
  {
    TransactionHistoryData tn = (TransactionHistoryData)absTN;
    return(iTranType == tn.iTranType && iTranCategory == tn.iTranCategory);
  }

 /**
  * Resets the data in this class to the default.
  */
  @Override
  public void clear()
  {
    super.clear();                     // Pull in default behaviour.

    dTransDateTime = new Date();
    dLastCCIDate = null;
    dAgingDate = null;
    dExpirationDate = null;
    dShipDate = null;
    
    mpStartingDate = null;
    mpEndingDate   = null;

    iTranCategory = 0;
    iTranType     = 0;
    iOrderType    = 0;
    iAisleGroup   = 0;
    sLocation     = "";
    sToLocation   = "";
    sLoadID       = "";
    sToLoad       = "";
    sItem         = "";
    sLot          = "";
    sOrderID      = "";
    sUserID       = "";
    sRole         = "";
    sMachineName  = "";
    sCustomer     = "";
    sStation      = "";
    sToStation    = "";
    sDeviceID     = "";
    sRouteID      = "";
    sActionDescription = "";
    fCurrentQuantity  = 0;
    fExpectedQuantity = 0;
    fReceivedQuantity = 0;
    fPickQuantity     = 0;
    fAdjustedQuantity = 0;
  }

  public String getUserID()
  {
    return sUserID; 
  }
  
  public Date getStartingDate()
  {
    return mpStartingDate;
  }
  
  public Date getEndingDate()
  {
    return mpEndingDate;
  }

/*----------------------------------------------------------------------------
 *                              Setter Methods
 *----------------------------------------------------------------------------*/
  public void setTranCategory(int inTranCategory)
  {
    iTranCategory = inTranCategory;
    addColumnObject(new ColumnObject(TRANCATEGORY_NAME, Integer.valueOf(iTranCategory)));
  }
  public void setTranType(int inTranType)
  {
    iTranType = inTranType;
    addColumnObject(new ColumnObject(TRANTYPE_NAME, Integer.valueOf(iTranType)));
  }
  public void setLocation(String isLocation)
  {
    String vsTmp = checkForNull(isLocation); 
    String[] locn = Location.parseLocation(vsTmp);
    setLocation(locn[0], locn[1]);
  }
  public void setLocation(String isWarehouse, String isAddress)
  {
    sLocation = checkForNull(isWarehouse) + "-" + checkForNull(isAddress); 
    addColumnObject(new ColumnObject(LOCATION_NAME, sLocation));         
  }
  public void setToLocation(String isToLocation)
  {
    String vsTmp = checkForNull(isToLocation); 
    String[] locn = Location.parseLocation(vsTmp);
    setToLocation(locn[0], locn[1]);
  }
  public void setToLocation(String isWarehouse, String isAddress)
  {
    sToLocation = checkForNull(isWarehouse) + "-" + checkForNull(isAddress);
    if (sToLocation.length() > 1) // If it's not just "-"
    {
      addColumnObject(new ColumnObject(TOLOCATION_NAME, sToLocation));
    }
  }
  public void setLoadID(String isLoadID)
  {
    sLoadID = checkForNull(isLoadID);
    addColumnObject(new ColumnObject(LOADID_NAME, sLoadID));
  }
  public void setToLoadID(String isToLoad)
  {
    sToLoad = checkForNull(isToLoad);
    addColumnObject(new ColumnObject(TOLOAD_NAME, sToLoad));
  }
  public void setItem(String isItem)
  {
    sItem = checkForNull(isItem);
    addColumnObject(new ColumnObject(ITEM_NAME, sItem));
  }
  public void setLot(String isLot)
  {
    sLot = checkForNull(isLot);
    addColumnObject(new ColumnObject(LOT_NAME, sLot));
  }
  public void setUserID(String isUserID)
  {
    sUserID = checkForNull(isUserID);
    addColumnObject(new ColumnObject(USERID_NAME, sUserID));           
  }
  public void setRole(String isRole)
  {
    sRole = checkForNull(isRole);
    addColumnObject(new ColumnObject(ROLE_NAME, sRole));             
  }
  public void setMachineName(String isMachineName)
  {
    sMachineName = checkForNull(isMachineName);
    addColumnObject(new ColumnObject(MACHINENAME_NAME, sMachineName));           
  }
  public void setStation(String isStation)
  {
    sStation = checkForNull(isStation);
    addColumnObject(new ColumnObject(STATION_NAME, sStation));          
  }
  public void setToStation(String isToStation)
  {
    sToStation = checkForNull(isToStation);
    addColumnObject(new ColumnObject(TOSTATION_NAME, sToStation));        
  }
  public void setDeviceID(String isDeviceID)
  {
    sDeviceID = checkForNull(isDeviceID);
    addColumnObject(new ColumnObject(DEVICEID_NAME, sDeviceID));         
  }
  public void setRouteID(String isRouteID)
  {
    sRouteID = checkForNull(isRouteID);
    addColumnObject(new ColumnObject(ROUTEID_NAME, sRouteID));
  }
  public void setOrderID(String isOrderID)
  {
    sOrderID = checkForNull(isOrderID);
    addColumnObject(new ColumnObject(ORDERID_NAME, sOrderID)); 
  }
  public void setCustomer(String isCustomer)
  {
    sCustomer = checkForNull(isCustomer);
    addColumnObject(new ColumnObject(CUSTOMER_NAME, sCustomer));    
  }
  public void setCarrierID(String isCarrierID)
  {
    sCarrierID = checkForNull(isCarrierID);
    addColumnObject(new ColumnObject(CARRIERID_NAME, sCarrierID));        
  }
  public void setLineID(String isLineID)
  {
    sLineID = checkForNull(isLineID);
    addColumnObject(new ColumnObject(LINEID_NAME, sLineID));           
  }
  public void setOrderLot(String isOrderLot)
  {
    sOrderLot = checkForNull(isOrderLot);
    addColumnObject(new ColumnObject(ORDERLOT_NAME, sOrderLot));         
  }
  public void setReasonCode(String isReasonCode)
  {
    sReasonCode = checkForNull(isReasonCode);
    addColumnObject(new ColumnObject(REASONCODE_NAME, sReasonCode));       
  }
  public void setOrderType(int inOrderType)
  {
    iOrderType = inOrderType;
    addColumnObject(new ColumnObject(ORDERTYPE_NAME, Integer.valueOf(iOrderType)));     
  }
  public void setAisleGroup(int inAisleGroup)
  {
    iAisleGroup = inAisleGroup;
    addColumnObject(new ColumnObject(AISLEGROUP_NAME, Integer.valueOf(iAisleGroup)));
  }
  public void setHoldType(int inHoldType)
  {
    iHoldType = inHoldType;
    addColumnObject(new ColumnObject(HOLDTYPE_NAME, Integer.valueOf(iHoldType)));
  }
  public void setCurrentQuantity(double idCurrentQuantity)
  {
    fCurrentQuantity = idCurrentQuantity;
    addColumnObject(new ColumnObject(CURRENTQUANTITY_NAME, Double.valueOf(fCurrentQuantity)));
  }
  public void setExpectedQuantity(double idExpectedQuantity)
  {
    fExpectedQuantity = idExpectedQuantity;
    addColumnObject(new ColumnObject(EXPECTEDQUANTITY_NAME, Double.valueOf(fExpectedQuantity)));
  }
  public void setReceivedQuantity(double idReceivedQuantity)
  {
    fReceivedQuantity = idReceivedQuantity;
    addColumnObject(new ColumnObject(RECEIVEDQUANTITY_NAME, Double.valueOf(fReceivedQuantity)));
  }
  public void setPickQuantity(double idPickQuantity)
  {
    fPickQuantity = idPickQuantity;
    addColumnObject(new ColumnObject(PICKQUANTITY_NAME, Double.valueOf(fPickQuantity)));
  }
  public void setAdjustedQuantity(double idAdjustedQuantity)
  {
    fAdjustedQuantity = idAdjustedQuantity;
    addColumnObject(new ColumnObject(ADJUSTEDQUANTITY_NAME, Double.valueOf(fAdjustedQuantity)));
  }
  public void setTransDateTime(Date ipdate)
  {
    dTransDateTime = ipdate;
    addColumnObject(new ColumnObject(TRANSDATETIME_NAME, dTransDateTime));  
  }
  public void setLastCCIDate(Date ipdate)
  {
    dLastCCIDate = ipdate;
    addColumnObject(new ColumnObject(LASTCCIDATE_NAME, dLastCCIDate));
  }
  public void setAgingDate(Date ipdate)
  {
    dAgingDate = ipdate;
    addColumnObject(new ColumnObject(AGINGDATE_NAME, dAgingDate));
  }
  public void setExpirationDate(Date ipdate)
  {
    dExpirationDate = ipdate;
    addColumnObject(new ColumnObject(EXPIRATIONDATE_NAME, dExpirationDate));
  }
  public void setShipDate(Date ipdate)
  {
    dShipDate = ipdate;
    addColumnObject(new ColumnObject(SHIPDATE_NAME, dShipDate));
  }
  public void setActionDescription(String isActionDescription)
  {
    sActionDescription = isActionDescription;
    addColumnObject(new ColumnObject(ACTIONDESCRIPTION_NAME, sActionDescription));
  }

/*--------------------------------------------------------------------------
    The following methods are used to do Key'ed searches of the transaction
    table.
  --------------------------------------------------------------------------*/
  public void setTranCategoryKey(int inTranCategory)
  {
    addKeyObject(new KeyObject(TRANCATEGORY_NAME, Integer.valueOf(inTranCategory)));
  }
  public void setTranTypeKey(int inTranType)
  {
    addKeyObject(new KeyObject(TRANTYPE_NAME, Integer.valueOf(inTranType)));
  }
  public void setDateRangeKey(Date ipStartDate, Date ipEndDate)
  {
    setBetweenKey(TRANSDATETIME_NAME, ipStartDate, ipEndDate);
    mpStartingDate = ipStartDate;
    mpEndingDate = ipEndDate;
  }
  public void setAisleGroupKey(int inAisleGroup)
  {
    addKeyObject(new KeyObject(AISLEGROUP_NAME, Integer.valueOf(inAisleGroup)));
  }
  public void setLocationKey(String isLocation)
  {
    String vsTmp = checkForNull(isLocation); 
    String[] locn = Location.parseLocation(vsTmp);
    addKeyObject(new KeyObject(LOCATION_NAME, locn[0] + "-" + locn[1]));
  }
  public void setToLocationKey(String isToLocation)
  {
    String vsTmp = checkForNull(isToLocation); 
    String[] locn = Location.parseLocation(vsTmp);
    addKeyObject(new KeyObject(TOLOCATION_NAME, locn[0] + "-" + locn[1]));
  }
  public void setLoadKey(String isLoadID)
  {
    addKeyObject(new KeyObject(LOADID_NAME, isLoadID));
  }
  public void setToLoadKey(String isToLoad)
  {
    addKeyObject(new KeyObject(TOLOAD_NAME, isToLoad));
  }
  public void setItemKey(String isItem)
  {
    addKeyObject(new KeyObject(ITEM_NAME, isItem));
  }
  public void setLotKey(String isLot)
  {
    addKeyObject(new KeyObject(LOT_NAME, isLot));
  }
  public void setOrderIDKey(String isOrderID)
  {
    addKeyObject(new KeyObject(ORDERID_NAME, isOrderID));
  }
  public void setOrderTypeKey(int inOrderType)
  {
    addKeyObject(new KeyObject(ORDERTYPE_NAME, Integer.valueOf(inOrderType)));
  }
  public void setUserIDKey(String isUserID)
  {
    addKeyObject(new KeyObject(USERID_NAME, isUserID));
  }
  public void setRoleKey(String isRole)
  {
    addKeyObject(new KeyObject(ROLE_NAME, isRole));
  }
  public void setMachineKey(String isMachineName)
  {
    addKeyObject(new KeyObject(MACHINENAME_NAME, isMachineName));
  }
  public void setCustomerKey(String isCustomer)
  {
    addKeyObject(new KeyObject(CUSTOMER_NAME, isCustomer));
  }
  public void setStationKey(String isStation)
  {
    addKeyObject(new KeyObject(STATION_NAME, isStation));
  }
  public void setToStationKey(String isToStation)
  {
    addKeyObject(new KeyObject(TOSTATION_NAME, isToStation));
  }
  public void setDeviceIDKey(String isDeviceID)
  {
    addKeyObject(new KeyObject(DEVICEID_NAME, isDeviceID));
  }
  public void setRouteIDKey(String isRouteID)
  {
    addKeyObject(new KeyObject(ROUTEID_NAME, isRouteID));
  }
  public void setActionDescriptionKey(String isActionDesc)
  {
    addKeyObject(new KeyObject(MACHINENAME_NAME, isActionDesc));
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

    switch((TransactionHistoryEnum)vpEnum)
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

      case LOADID:
        setLoadID((String)ipColValue);
        break;

      case TOLOAD:
        setToLoadID((String)ipColValue);
        break;

      case REASONCODE:
        setReasonCode((String)ipColValue);
        break;

      case ORDERID:
        setOrderID((String)ipColValue);
        break;

      case ORDERLOT:
        setOrderLot((String)ipColValue);
        break;

      case LASTCCIDATE:
        setLastCCIDate((Date)ipColValue);
        break;

      case AGINGDATE:
        setAgingDate((Date)ipColValue);
        break;

      case EXPIRATIONDATE:
        setExpirationDate((Date)ipColValue);
        break;

      case SHIPDATE:
        setShipDate((Date)ipColValue);
        break;

      case TRANSDATETIME:
        setTransDateTime((Date)ipColValue);
        break;

      case CURRENTQUANTITY:
        setCurrentQuantity((Double)ipColValue);
        break;

      case ADJUSTEDQUANTITY:
        setAdjustedQuantity((Double)ipColValue);
        break;

      case EXPECTEDQUANTITY:
        setExpectedQuantity((Double)ipColValue);
        break;

      case RECEIVEDQUANTITY:
        setReceivedQuantity((Double)ipColValue);
        break;

      case PICKQUANTITY:
        setPickQuantity((Double)ipColValue);
        break;

      case HOLDTYPE:
        setHoldType((Integer)ipColValue);
        break;

      case TRANCATEGORY:
        setTranCategory((Integer)ipColValue);
        break;

      case TRANTYPE:
        setTranType((Integer)ipColValue);
        break;

      case ORDERTYPE:
        setOrderType((Integer)ipColValue);
        break;

      case AISLEGROUP:
        setAisleGroup((Integer)ipColValue);
        break;

      case CARRIERID:
        setCarrierID((String)ipColValue);
        break;

      case LOCATION:
        setLocation((String)ipColValue);
        break;

      case TOLOCATION:
        setToLocation((String)ipColValue);
        break;

      case ROLE:
        setRole((String)ipColValue);
        break;

      case STATION:
        setStation((String)ipColValue);
        break;

      case TOSTATION:
        setToStation((String)ipColValue);
        break;

      case ROUTEID:
        setRouteID((String)ipColValue);
        break;

      case CUSTOMER:
        setCustomer((String)ipColValue);
        break;

      case USERID:
        setUserID((String)ipColValue);
        break;

      case MACHINENAME:
        setMachineName((String)ipColValue);
        break;

      case DEVICEID:
        setDeviceID((String)ipColValue);
        break;

      case ACTIONDESCRIPTION:
        setActionDescription((String)ipColValue);
        
    }
    
    return(0);
  }
}
