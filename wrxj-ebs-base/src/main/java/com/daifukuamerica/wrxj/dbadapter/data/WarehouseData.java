package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.WarehouseEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:<BR>
 *   Class to handle Warehouse Data operations.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created:  20-Feb-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  Daifuku America Corporation
 */
public class WarehouseData extends AbstractSKDCData
{
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  /*========================================================================*/
  /*  Column names                                                          */
  /*========================================================================*/
  public static final String DESCRIPTION_NAME     = DESCRIPTION.getName();
  public static final String EQUIPWAREHOUSE_NAME  = EQUIPWAREHOUSE.getName();
  public static final String ONELOADPERLOC_NAME   = ONELOADPERLOC.getName();
  public static final String SUPERWAREHOUSE_NAME  = SUPERWAREHOUSE.getName();
  public static final String WAREHOUSE_NAME       = WAREHOUSE.getName();
  public static final String WAREHOUSESTATUS_NAME = WAREHOUSESTATUS.getName();
  public static final String WAREHOUSETYPE_NAME   = WAREHOUSETYPE.getName();

  /*========================================================================*/
  /*  Table Data                                                            */
  /*========================================================================*/
  private String sSuperWarehouse  = "";
  private String sWarehouse       = "";
  private String sDescription     = "";
  private int    iWarehouseType   = DBConstants.REGULAR;
  private int    iWarehouseStatus = DBConstants.WARAVAIL;
  private int    iOneLoadPerLoc   = DBConstants.YES;
  private String sEquipWarehouse    = "";

  public WarehouseData()
  {
    super();
    initColumnMap(mpColumnMap, WarehouseEnum.class);
  }

  /**
   * This helps in debugging when we want to print the whole structure.
   */
  @Override
  public String toString()
  {
    StringBuffer s = new StringBuffer();
    s.append("sSuperWarehouse:'").append(sSuperWarehouse)
     .append(SKDCConstants.EOL_CHAR)
     .append("sWarehouse:'").append(sWarehouse)
     .append(SKDCConstants.EOL_CHAR)
     .append("sDescription:'").append(sDescription)
     .append(SKDCConstants.EOL_CHAR)
     .append("sEquipWarehouse:'").append(sEquipWarehouse);

    try
    {
      s.append("iWarehouseStatus:")
       .append(DBTrans.getStringValue(WAREHOUSESTATUS_NAME, iWarehouseStatus))
       .append(SKDCConstants.EOL_CHAR)
       .append("iWarehouseType:")
       .append(DBTrans.getStringValue(WAREHOUSETYPE_NAME, iWarehouseType))
       .append(SKDCConstants.EOL_CHAR)
       .append(SKDCConstants.EOL_CHAR)
       .append("iOneLoadPerLoc:")
       .append(DBTrans.getStringValue(ONELOADPERLOC_NAME, iOneLoadPerLoc));
    }
    catch(NoSuchFieldException e)
    {
      s.append("0");
    }
                                       // Throw in the Key and Column info.
    s.append(SKDCConstants.EOL_CHAR).append(SKDCConstants.EOL_CHAR);

    return(s.toString() + super.toString());
  }

  @Override
  public boolean equals(AbstractSKDCData absWT)
  {
    if (absWT == null) return(false);
    WarehouseData wt = (WarehouseData)absWT;
    return(wt.getWarehouse().equals(this.getWarehouse()));
  }

  /**
   * Resets the data in this class to the default.
   */
  @Override
  public void clear()
  {
    super.clear();                     // Pull in the default behaviour

    sSuperWarehouse  = "";
    sWarehouse       = "";
    sDescription     = "";
    iWarehouseType   = DBConstants.REGULAR;
    iWarehouseStatus = DBConstants.WARAVAIL;
    iOneLoadPerLoc   = DBConstants.YES;
    sEquipWarehouse  = "";
  }

/*---------------------------------------------------------------------------
      Column value get methods go here. These methods do some basic checking
      in most cases to return the default value in case something is not set
      correctly.
  ---------------------------------------------------------------------------*/
  /**
   * Fetches Parent Warehouse
   * @return Parent Warehouse as string
   */
  public String getSuperWarehouse()
  {
    return(sSuperWarehouse);
  }

  /**
   * Fetches Warehouse
   * @return warehouse as string
   */
  public String getWarehouse()
  {
    return(sWarehouse);
  }

  /**
   * Fetches Warehouse Description
   * @return Description as string
   */
  public String getDescription()
  {
    return(sDescription);
  }
  
  /**
   * Fetches Equipment Warehouse
   * @return Equipment Warehouse as a string
   */
  public String getEquipWarehouse()
  {
    return(sEquipWarehouse);
  }
  
  /**
   * Fetches Warehouse Type
   * @return Warehouse Type as integer. Return Default of REGULAR if not set.
   */
  public int getWarehouseType()
  {
    return(iWarehouseType);
  }

  /**
   * Fetches Warehouse Status
   * @return Warehouse status as integer. Return Default of WARAVAIL if not set.
   */
  public int getWarehouseStatus()
  {
    return(iWarehouseStatus);
  }

  /**
   * Fetches One Load per Location flag 
   * @return One Load per Location flag as integer.  Default is NO.
   */
  public int getOneLoadPerLoc()
  {
    return(iOneLoadPerLoc);
  }
  
/*---------------------------------------------------------------------------
               ******** Column Setting methods go here. ********
  ---------------------------------------------------------------------------*/
  /**
   * Sets Parent Warehouse value.
   */
  public void setSuperWarehouse(String isSuperWarehouse)
  {
    sSuperWarehouse = checkForNull(isSuperWarehouse);
    addColumnObject(new ColumnObject(SUPERWAREHOUSE_NAME, isSuperWarehouse));
  }

  /**
   * Sets Warehouse value.
   */
  public void setWarehouse(String isWarehouse)
  {
    sWarehouse = checkForNull(isWarehouse);
    addColumnObject(new ColumnObject(WAREHOUSE_NAME, isWarehouse));
  }

  /**
   * Sets Warehouse Description value.
   */
  public void setDescription(String isDescription)
  {
    sDescription = checkForNull(isDescription);
    addColumnObject(new ColumnObject(DESCRIPTION_NAME, isDescription));
  }

  /**
   * Sets Warehouse Description value.
   */
  public void setEquipWarehouse(String isEquipWarehouse)
  {
    sEquipWarehouse = checkForNull(isEquipWarehouse);
    addColumnObject(new ColumnObject(EQUIPWAREHOUSE_NAME, isEquipWarehouse));
  }
  /**
   * Sets Warehouse type value
   */
  public void setWarehouseType(int inWarehouseType)
  {
    try
    {
      DBTrans.getStringValue("iWarehouseType", inWarehouseType);
    }
    catch(NoSuchFieldException e)
    {                                  // Passed value wasn't valid. Default it
      inWarehouseType = DBConstants.REGULAR;
      Logger.getLogger().logException(e);
    }
    iWarehouseType = inWarehouseType;
    addColumnObject(new ColumnObject(WAREHOUSETYPE_NAME, Integer.valueOf(inWarehouseType)));
  }

  /**
   * Sets Warehouse Status value
   */
  public void setWarehouseStatus(int inWarehouseStatus)
  {
    try
    {
      DBTrans.getStringValue("iWarehouseStatus", inWarehouseStatus);
    }
    catch(NoSuchFieldException e)
    {                               // Passed value wasn't valid. Default it
      inWarehouseStatus = DBConstants.WARAVAIL;
      Logger.getLogger().logException(e);
    }
    iWarehouseStatus = inWarehouseStatus;
    addColumnObject(new ColumnObject(WAREHOUSESTATUS_NAME, Integer.valueOf(inWarehouseStatus)));
  }

  /**
   * Sets One Load Per Location value
   */
  public void setOneLoadPerLoc(int inOneLoadPerLoc)
  {
    try
    {
      DBTrans.getStringValue("iOneLoadPerLoc", inOneLoadPerLoc);
    }
    catch(NoSuchFieldException e)
    {                               // Passed value wasn't valid. Default it
      inOneLoadPerLoc = DBConstants.NO;
      Logger.getLogger().logException(e);
    }
    iOneLoadPerLoc = inOneLoadPerLoc;
    addColumnObject(new ColumnObject(ONELOADPERLOC_NAME,  Integer.valueOf(inOneLoadPerLoc)));
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
    if (vpEnum == null)
    {
      return super.setField(isColName, ipColValue);
    }

    switch ((WarehouseEnum)vpEnum)
    {
      case SUPERWAREHOUSE:
        setSuperWarehouse(ipColValue.toString());
        break;
        
      case WAREHOUSE:
        setWarehouse(ipColValue.toString());
        break;
        
      case DESCRIPTION:
        setDescription(ipColValue.toString());
        break;
        
      case EQUIPWAREHOUSE:
        setEquipWarehouse(ipColValue.toString());
        break;
      case WAREHOUSESTATUS:
        setWarehouseStatus(((Integer)ipColValue).intValue());
        break;
        
      case WAREHOUSETYPE:
        setWarehouseType(((Integer)ipColValue).intValue());
        break;

      case ONELOADPERLOC:
        setOneLoadPerLoc(((Integer)ipColValue).intValue());
    }

    return 0;
  }
}
