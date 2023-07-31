package com.daifukuamerica.wrxj.dbadapter.data;
/****************************************************************************
  $Workfile: VehiclePathsData.java
  $Revision: Baseline

  Copyright 2019 Daifuku America Corporation All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND
  TREATIES. NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED
  COPIED, DISTRIBUTED, REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED,
  EXPANDED, COLLECTED, COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED
  WITHOUT THE PRIOR WRITTEN CONSENT OF Daifuku America Corporation ANY
  USE OR EXPLOITATION OF THIS WORK WITHOUT AUTHORIZATION COULD SUBJECT THE
  PERPETRATOR TO CRIMINAL AND CIVIL LIABILITY.
 ****************************************************************************/
import static com.daifukuamerica.wrxj.dbadapter.data.VehiclePathsEnum.*;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Vehicle Paths Data.
 *
 * @author A.D.
 * @since  30-Oct-2019
 */
public class VehiclePathsData extends AbstractSKDCData
{
  public static final String PATH_NUMBER_NAME = PATHNUMBER.getName();
  public static final String FROM_STATION_NAME = FROMSTATION.getName();
  public static final String TO_STATION_NAME = TOSTATION.getName();

  private String msPathNumber  = "1.0";
  private String msFromStation = "";
  private String msToStation   = "";

  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  public VehiclePathsData()
  {
    super();
    initColumnMap(mpColumnMap, VehiclePathsEnum.class);
    clear();                           // set all values to default
  }

  @Override
  public void clear()
  {
    super.clear();                     // Pull in the default behaviour.

    msPathNumber  = "1.0";
    msFromStation = "";
    msToStation   = "";
  }

  /**
   * String representation of this object.
   */
  @Override
  public String toString()
  {
    String vsStr = "msPathNumber : " + msPathNumber + SKDCConstants.EOL_CHAR +
                   "msFromStation: " + msFromStation + SKDCConstants.EOL_CHAR +
                   "msToStation  : " + msToStation + SKDCConstants.EOL_CHAR;
    return vsStr;
  }

  @Override
  public boolean equals(AbstractSKDCData ipData)
  {
    VehiclePathsData vpVPData = (VehiclePathsData)ipData;

    return vpVPData.getPathNumber().equals(msPathNumber)   &&
           vpVPData.getFromStation().equals(msFromStation) &&
           vpVPData.getToStation().equals(msToStation);
  }

  public String getPathNumber()
  {
    return msPathNumber;
  }

  public String getFromStation()
  {
    return msFromStation;
  }

  public String getToStation()
  {
    return msToStation;
  }

  /**
   * Sets the Path Number for a path. the path consists of a major and a minor number.
   * The minor number designates alternate paths to a primary one.
   * If the path is a single one, the minor number is 0.  For example if the
   * primary path is 1.0, a set of alternates could be 1.1, 1.2, 1.3 etc. Using
   * this scheme is strictly a matter of choice if, for example, the primary and
   * alternates are to be tied together.
   *
   * @param idPathNumber
   */
  public void setPathNumber(String isPathNumber)
  {
    msPathNumber = isPathNumber;
    addColumnObject(new ColumnObject(PATHNUMBER.getName(), isPathNumber));
  }

  public void setFromStation(String isFromStation)
  {
    msFromStation = isFromStation;
    addColumnObject(new ColumnObject(FROMSTATION.getName(), isFromStation));
  }

  public void setToStation(String isToStation)
  {
    msToStation = isToStation;
    addColumnObject(new ColumnObject(TOSTATION.getName(), isToStation));
  }

 @Override
  public int setField(String isColName, Object ipColValue)
  {
    TableEnum vpEnum = mpColumnMap.get(isColName);
    if (vpEnum == null)
    {
      return(super.setField(isColName, ipColValue));
    }

    switch((VehiclePathsEnum)vpEnum)
    {
      case FROMSTATION:
        setFromStation((String)ipColValue);
        break;

      case TOSTATION:
        setToStation((String)ipColValue);
        break;

      case PATHNUMBER:
        setPathNumber((String)ipColValue);
        break;
    }

    return(0);
  }
}
