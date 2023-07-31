package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.ZoneEnum.*;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ZoneData extends AbstractSKDCData
{
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  /*========================================================================*/
  /*  Column names                                                          */
  /*========================================================================*/
  public static final String ZONE_NAME        = ZONE.getName();
  public static final String DESCRIPTION_NAME = DESCRIPTION.getName();
  
  /*========================================================================*/
  /*  Table Data                                                            */
  /*========================================================================*/
  private String sZone = "";
  private String sDescription = "";
  
  public ZoneData()
  {
    super();
    initColumnMap(mpColumnMap, ZoneEnum.class);
  }

  /**
   * clear
   */
  @Override
  public void clear()
  {
    super.clear();
    
    sZone = "";
    sDescription = "";
  }
  
  /**
   * toString
   */
  @Override
  public String toString()
  {
    String s = "sZone: " + sZone +
               "\nsDescription: " + sDescription;
    s += super.toString();

    return s;
  }

  /**
   * Defines equality between two ZoneData objects.
   *
   * @param  ipAbsZone <code>AbstractSKDCData</code> reference whose runtime type
   *         is expected to be <code>ZoneData</code>
   */
  @Override
  public boolean equals(AbstractSKDCData ipAbsZone)
  {
    ZoneData vpZoneData = (ZoneData)ipAbsZone;
    return vpZoneData.sZone.equals(sZone);
  }

  /*========================================================================*/
  /*  Getters                                                               */
  /*========================================================================*/
  
  /**
   * Get the Zone
   */
  public String getZone()
  {
    return sZone;
  }
  
  /**
   * Get the Zone Description
   */
  public String getDescription()
  {
    return sDescription;
  }
  
  /*========================================================================*/
  /*  Setters                                                               */
  /*========================================================================*/

  /**
   * Set the Zone
   * 
   * @param isZone
   */
  public void setZone(String isZone)
  {
    sZone = isZone;
    addColumnObject(new ColumnObject(ZONE_NAME, isZone));
  }

  /**
   * Set the Description
   * 
   * @param isDescription
   */
  public void setDescription(String isDescription)
  {
    sDescription = isDescription;
    addColumnObject(new ColumnObject(DESCRIPTION_NAME, isDescription));
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

    switch ((ZoneEnum)vpEnum)
    {
      case ZONE:
        setZone(ipColValue.toString());
        break;

      case DESCRIPTION:
        setDescription(ipColValue.toString());
        break;
        
      default:
        return -1;
    }

    return 0;
  }
}
