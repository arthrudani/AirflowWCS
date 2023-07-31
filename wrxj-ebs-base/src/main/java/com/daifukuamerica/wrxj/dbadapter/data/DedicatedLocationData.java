package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.DedicatedLocationEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * <B>Description:</B> Class to handle Dedication Data Object.<BR>
 * 
 * <B>NOTE:</B> Dedicated Type is not yet implemented.  The only valid value is
 * PIECEPICK.
 * 
 * 
 * @author       MDA
 * @version      1.0	16-Nov-04
 * 
 */
public class DedicatedLocationData extends AbstractSKDCData
{
  /*========================================================================*/
  /*  Column names                                                          */
  /*========================================================================*/
  public static String ITEM_NAME            = ITEM.getName();
  public static String WAREHOUSE_NAME       = WAREHOUSE.getName();
  public static String ADDRESS_NAME         = ADDRESS.getName();
  public static String MINIMUMQUANTITY_NAME = MINIMUMQUANTITY.getName();
  public static String MAXIMUMQUANTITY_NAME = MAXIMUMQUANTITY.getName();
  public static String DEDICATEDTYPE_NAME   = DEDICATEDTYPE.getName();
  public static String REPLENISHTYPE_NAME   = REPLENISHTYPE.getName();
  public static String REPLENISHNOW_NAME    = REPLENISHNOW.getName();
  public static String CURRENTQUANTITY_NAME = CURRENTQUANTITY.getName();
  public static String ENROUTEQUANTITY_NAME = ENROUTEQUANTITY.getName();

  /*========================================================================*/
  /*  Dedicated Location Table data                                         */
  /*========================================================================*/
  private String sItem;
  private String sWarehouse;
  private String sAddress;
  private double fMinimumQuantity;
  private double fMaximumQuantity;
  private double fCurrentQuantity;    // NOT IN DATABASE
  private double fEnrouteQuantity;    // NOT IN DATABASE
  private int    iReplenishNow;
  private int    iDedicatedType;
  private int    iReplenishType;

  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  /*========================================================================*/
  /*  Default data                                                          */
  /*========================================================================*/
  static private String DEFAULT_ITEM = "";
  static private String DEFAULT_WAREHOUSE = "";
  static private String DEFAULT_ADDRESS = "";
  static private double DEFAULT_MINIMUMQUANTITY = 1.0;
  static private double DEFAULT_MAXIMUMQUANTITY = 2.0;
  static private double DEFAULT_CURRENTQUANTITY = 0.0;
  static private double DEFAULT_ENROUTEQUANTITY = 0.0;
  static private int    DEFAULT_REPLENISHNOW = DBConstants.DLACTIVE;
  static private int    DEFAULT_DEDICATEDTYPE = DBConstants.PIECEPICK;
  static private int    DEFAULT_REPLENISHTYPE = DBConstants.LOAD;

  public DedicatedLocationData()
  {
    clear();     // set all values to default
    initColumnMap(mpColumnMap, DedicatedLocationEnum.class);
  }

  @Override
  public void clear()
  {
    sItem            = DEFAULT_ITEM;
    sWarehouse       = DEFAULT_WAREHOUSE;
    sAddress         = DEFAULT_ADDRESS;
    fMinimumQuantity = DEFAULT_MINIMUMQUANTITY;
    fMaximumQuantity = DEFAULT_MAXIMUMQUANTITY;
    fCurrentQuantity = DEFAULT_CURRENTQUANTITY;
    fEnrouteQuantity = DEFAULT_ENROUTEQUANTITY;
    iDedicatedType   = DEFAULT_DEDICATEDTYPE;
    iReplenishType   = DEFAULT_REPLENISHTYPE;
    iReplenishNow    = DEFAULT_REPLENISHNOW;
  }

  @Override
  public String toString()
  {
    String s = "sItem:"        + sItem      +
               "\nsWarehouse:" + sWarehouse +
               "\nsAddress:"   + sAddress   +
               "\nfMinimumQuantity:" + Double.toString(fMinimumQuantity) +
               "\nfMaximumQuantity:" + Double.toString(fMaximumQuantity);
    try
    {
      s = s + "\niDedicatedType:" + DBTrans.getStringValue(DEDICATEDTYPE_NAME,
                                                           iDedicatedType) +
               "\niReplenishType:" + DBTrans.getStringValue(REPLENISHTYPE_NAME,
                                                             iReplenishType) +
               "\niReplenishNow:" + DBTrans.getStringValue(REPLENISHNOW_NAME,
                                                            iReplenishNow);
    }
    catch(NoSuchFieldException e)
    {
      s = s + "0";
    }
    s += super.toString();

    return s;
  }

  @Override
  public boolean equals(AbstractSKDCData ipAbstractData)
  {
    DedicatedLocationData dl = (DedicatedLocationData)ipAbstractData;
    if ((dl.getWarehouse().equals(getWarehouse())) &&
        (dl.getAddress().equals(getAddress())) &&
		    (dl.getItem().equals(getItem())))
    {
      return( true );
    }
    return( false );
  }

  /*========================================================================*/
  /*  Getters                                                               */
  /*========================================================================*/
  public String getItem()            { return sItem; }
  public String getWarehouse()       { return sWarehouse; }
  public String getAddress()         { return sAddress; }
  public double getMinimumQuantity() { return fMinimumQuantity; }
  public double getMaximumQuantity() { return fMaximumQuantity; }
  public double getCurrentQuantity() { return fCurrentQuantity; }
  public double getEnrouteQuantity() { return fEnrouteQuantity; }
  public int    getDedicatedType()   { return iDedicatedType; }
  public int    getReplenishType()   { return iReplenishType; }
  public int    getReplenishNow()    { return iReplenishNow; }

  
  /*========================================================================*/
  /*  Setters                                                               */
  /*========================================================================*/
  public void setItem(String item)
  {
    sItem = checkForNull(item);
    addColumnObject(new ColumnObject(ITEM_NAME, sItem));
  }

  public void setWarehouse(String warehouse)
  {
    sWarehouse = checkForNull(warehouse);
    addColumnObject(new ColumnObject(WAREHOUSE_NAME, sWarehouse));
  }

  public void setAddress(String address)
  {
    sAddress = checkForNull(address);
    addColumnObject(new ColumnObject(ADDRESS_NAME, sAddress));
  }

  public void setMinimumQuantity(double idMinimumQuantity)
  {
    if (idMinimumQuantity < 0.0)
    {
      idMinimumQuantity = 0.0;
    }
    fMinimumQuantity = idMinimumQuantity;
    addColumnObject(new ColumnObject(MINIMUMQUANTITY_NAME, fMinimumQuantity));
  }

  public void setMaximumQuantity(double idMaximumQuantity)
  {
    if (idMaximumQuantity < 1.0)
    {
      idMaximumQuantity = 1.0;
    }
    fMaximumQuantity = idMaximumQuantity;
    addColumnObject(new ColumnObject(MAXIMUMQUANTITY_NAME, fMaximumQuantity));
  }

  public void setCurrentQuantity(double idCurrentQuantity)
  {
    if (idCurrentQuantity < 0.0)
    {
      idCurrentQuantity = 0.0;
    }
    fCurrentQuantity = idCurrentQuantity;
    // This is not in the database, do not add a ColumnObject!
  }

  public void setEnrouteQuantity(double idEnrouteQuantity)
  {
    if (idEnrouteQuantity < 0.0)
    {
      idEnrouteQuantity = 0.0;
    }
    fEnrouteQuantity = idEnrouteQuantity;
    // This is not in the database, do not add a ColumnObject!
  }

  public void setDedicatedType(int dedicatedType)
  {
      /*
       * Dedicated Type is not yet implemented.  The only valid value is
       * PIECEPICK 
       * 
    try
    {
      DBTrans.getStringValue("iDedicatedType", dedicatedType);
    }
    catch(NoSuchFieldException e)
       */
    {                                  // Passed value wasn't valid. Default it
      dedicatedType = DEFAULT_DEDICATEDTYPE;
    }
    iDedicatedType = dedicatedType;
           
    addColumnObject(new ColumnObject(DEDICATEDTYPE_NAME, dedicatedType));
  }
  
  public void setReplenishType(int inReplenishType)
  {
    try
    {
      DBTrans.getStringValue(REPLENISHTYPE_NAME, inReplenishType);
    }
    catch(NoSuchFieldException e)
    {                                  // Passed value wasn't valid. Default it
      inReplenishType = DEFAULT_REPLENISHTYPE;
    }
    iReplenishType = inReplenishType;
    
    addColumnObject(new ColumnObject(REPLENISHTYPE_NAME, inReplenishType));
  }

  public void setReplenishNow(int replenishNow)
  {
    try
    {
      DBTrans.getStringValue(REPLENISHNOW_NAME, replenishNow);
    }
    catch(NoSuchFieldException e)
    {                                  // Passed value wasn't valid. Default it
      replenishNow = DEFAULT_REPLENISHNOW;
    }
    iReplenishNow = replenishNow;
    addColumnObject(new ColumnObject(REPLENISHNOW_NAME, Integer.valueOf(replenishNow)));
  }

  /**
   * Required set field method. This method figures out what column was passed
   * to it and sets the value. This allows us to have a generic method for all
   * DB interfaces.
   */
  @Override
  public int setField(String ipColName, Object ipColValue)
  {
    TableEnum vpEnum = mpColumnMap.get(ipColName);
    if (vpEnum == null) 
    {
      return(super.setField(ipColName, ipColValue));
    }

    switch ((DedicatedLocationEnum)vpEnum)
    {
      case ITEM:
        setItem(ipColValue.toString());
        break;
      case WAREHOUSE:
        setWarehouse(ipColValue.toString());
        break;
      case ADDRESS:
        setAddress(ipColValue.toString());
        break;
      case MINIMUMQUANTITY:
        setMinimumQuantity(((Double)ipColValue).doubleValue());
        break;
      case MAXIMUMQUANTITY:
        setMaximumQuantity(((Double)ipColValue).doubleValue());
        break;
      case DEDICATEDTYPE:
        setDedicatedType(((Integer)ipColValue).intValue());
        break;
      case REPLENISHTYPE:
        setReplenishType(((Integer)ipColValue).intValue());
        break;
      case REPLENISHNOW:
        setReplenishNow(((Integer)ipColValue).intValue());
        break;
      case CURRENTQUANTITY:
        setCurrentQuantity(((Double)ipColValue).doubleValue());
        break;
      case ENROUTEQUANTITY:
        setEnrouteQuantity(((Double)ipColValue).doubleValue());
        break;
    }

    return 0;
  }
}
