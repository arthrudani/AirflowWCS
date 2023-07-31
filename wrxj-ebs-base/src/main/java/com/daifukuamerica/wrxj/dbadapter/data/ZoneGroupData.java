package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import static com.daifukuamerica.wrxj.dbadapter.data.ZoneGroupEnum.*;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ZoneGroupData extends AbstractSKDCData
{
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  /*========================================================================*/
  /*  Column names                                                          */
  /*========================================================================*/
  public static final String ZONEGROUP_NAME   = ZONEGROUP.getName();
  public static final String PRIORITY_NAME    = PRIORITY.getName();
  public static final String ZONE_NAME        = ZONE.getName();

  /*========================================================================*/
  /*  Table Data                                                            */
  /*========================================================================*/
  private String sZoneGroup = "";
  private int    iPriority = 1;
  private String sZone = "";

  public ZoneGroupData()
  {
    super();
    initColumnMap(mpColumnMap, ZoneGroupEnum.class);
  }

  /**
   * Clear
   */
  @Override
  public void clear()
  {
    super.clear();
    
    sZoneGroup = "";
    iPriority = 1;
    sZone = "";
  }
  
  /**
   * Equals
   */
  @Override
  public boolean equals(AbstractSKDCData eskdata)
  {
    ZoneGroupData vpZGData = (ZoneGroupData)eskdata;
    
    return vpZGData.sZoneGroup.equals(sZoneGroup) &&
           vpZGData.iPriority == iPriority &&
           vpZGData.sZone.equals(sZone);
  }

  @Override
  public String toString()
  {
    String s = "sZoneGroup: " + sZoneGroup +
               "iPriority: "  + iPriority +
               "sZone: " + sZone;
    s += super.toString();

    return s;
  }

  /*========================================================================*/
  /*  Getters                                                               */
  /*========================================================================*/
  public String getZoneGroup()
  {
    return sZoneGroup;
  }
  public int getPriority()
  {
    return iPriority;
  }
  public String getZone()
  {
    return sZone;
  }
  
  /*========================================================================*/
  /*  Setters                                                               */
  /*========================================================================*/
  public void setZoneGroup(String isZoneGroup)
  {
    sZoneGroup = isZoneGroup;
    addColumnObject(new ColumnObject(ZONEGROUP_NAME, isZoneGroup));
  }
  public void setPriority(int inPriority)
  {
    iPriority = inPriority;
    addColumnObject(new ColumnObject(PRIORITY_NAME, Integer.valueOf(inPriority)));
  }
  public void setZone(String isZone)
  {
    sZone = isZone;
    addColumnObject(new ColumnObject(ZONE_NAME, isZone));
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

    switch ((ZoneGroupEnum)vpEnum)
    {
      case ZONEGROUP:
        setZoneGroup(ipColValue.toString());
        break;

      case PRIORITY:
        setPriority(((Integer)ipColValue).intValue());
        break;

      case ZONE:
        setZone(ipColValue.toString());
        break;

      default:
        return -1;
    }

    return 0;
  }

}
