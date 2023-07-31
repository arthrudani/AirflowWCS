package com.daifukuamerica.wrxj.dbadapter.data;
/****************************************************************************
  $Workfile: VehiclePaths.java
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
import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import java.util.List;

/**
 * Class to get Vehicle Paths for AGV's.
 *
 * @author A.D.
 * @since  30-Oct-2019
 */
public class VehiclePaths extends BaseDBInterface
{
  private VehiclePathsData mpVPData;

  public VehiclePaths()
  {
    super("VehiclePaths");
    mpVPData = Factory.create(VehiclePathsData.class);
  }

  /**
   * Gets all defined vehicle paths for a given path number.
   * @param idPathNumber if set to -1 get all paths ordered by path number.
   * @return
   * @throws DBException
   */
  public List<VehiclePathsData> getVehiclePaths(double idPathNumber) throws DBException
  {
    mpVPData.clear();
    if (idPathNumber != -1)
    {
      mpVPData.setKey(VehiclePathsData.PATH_NUMBER_NAME, idPathNumber);
    }
    mpVPData.setOrderByColumns("TO_NUMBER(REGEXP_SUBSTR(" +
                        VehiclePathsData.PATH_NUMBER_NAME + ", '^\\d{1,2}*'))");

    return DBHelper.convertData(getAllElements(mpVPData), VehiclePathsData.class);
  }

  /**
   * Gets all paths with same originating station.
   *
   * @param isFromStation
   * @return List&lt;VehiclePathsData&gt;
   * @throws DBException
   */
  public List<VehiclePathsData> getVehiclePaths(String isFromStation) throws DBException
  {
    mpVPData.clear();
    mpVPData.setKey(VehiclePathsData.FROM_STATION_NAME, isFromStation);
    mpVPData.setOrderByColumns(VehiclePathsData.PATH_NUMBER_NAME);

    return DBHelper.convertData(getAllElements(mpVPData), VehiclePathsData.class);
  }

  /**
   * Get all paths with common destination station.
   *
   * @param isToStation dest. station
   * @return List&lt;VehiclePathsData&gt;
   * @throws com.daifukuamerica.wrxj.jdbc.DBException
   */
  public List<VehiclePathsData> getVehiclePathsCommonDest(String isToStation) throws DBException
  {
    mpVPData.clear();
    mpVPData.setKey(VehiclePathsData.TO_STATION_NAME, isToStation);
    mpVPData.setOrderByColumns(VehiclePathsData.PATH_NUMBER_NAME);

    return DBHelper.convertData(getAllElements(mpVPData), VehiclePathsData.class);
  }
}
