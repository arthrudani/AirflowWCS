/* ***************************************************************************
  Copyright (c) 2019 Daifuku North America Holding Company. All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED,
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Class to support database operations on the EQUIPMENTMONITORTRACKING table
 */
public class EquipmentMonitorTracking extends BaseDBInterface
{
  public EquipmentMonitorTracking()
  {
    super("EquipmentMonitorTracking");
  }

  /**
   * Get a EquipmentMonitorTracking list
   *
   * @return
   * @throws DBException
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getList(String isKey) throws DBException
  {
  	EquipmentMonitorTrackingData vpTrackingData = Factory.create(EquipmentMonitorTrackingData.class);
  	if (SKDCUtility.isNotBlank(isKey))
  	{
  	  vpTrackingData.setKey(EquipmentMonitorTrackingData.GRAPHICID_NAME, isKey);
  	}
    vpTrackingData.addOrderByColumn(EquipmentMonitorTrackingData.GRAPHICID_NAME);
    vpTrackingData.addOrderByColumn(EquipmentMonitorTrackingData.TRACKINGID_NAME);

    return getAllElements(vpTrackingData);
  }

  /**
   * Delete a particular EquipmentMonitorTracking record
   *
   * @param isKey
   * @throws DBException
   */
  public void delete(String isKey) throws DBException
  {
    try
    {
      EquipmentMonitorTrackingData vpTrackingData = Factory.create(EquipmentMonitorTrackingData.class);
      if (SKDCUtility.isNotBlank(isKey))
      {
        vpTrackingData.setKey(EquipmentMonitorTrackingData.GRAPHICID_NAME, isKey);
      }
      else
      {
        // Our database interface does not allow keyless deletes
        vpTrackingData.setKey(EquipmentMonitorTrackingData.GRAPHICID_NAME, "%", KeyObject.LIKE);
      }
      deleteElement(vpTrackingData);
    }
    catch (NoSuchElementException nsee)
    {
      // Ignore
    }
  }

  /**
   * Delete a all EquipmentMonitorTracking records for a controller
   *
   * @param isControllerID
   * @throws DBException
   */
  public void deleteAll(String isControllerID) throws DBException
  {
    try
    {
      /*
        delete EQUIPMENTMONITORTRACKING
         where SEMGRAPHICID in (select SEMGRAPHICID
           from EQUIPMENTMONITORSTATUS
            where SEMMCCONTROLLER=?
               or SEMMOSCONTROLLER=?)
       */
      String vsSQL = "delete " + getWriteTableName() + " where "
          + EquipmentMonitorTrackingData.GRAPHICID_NAME + " in (select "
          + EquipmentMonitorStatusData.GRAPHICID_NAME + " from "
          + new EquipmentMonitorStatus().getReadTableName() + " where "
          + EquipmentMonitorStatusData.MCCONTROLLER_NAME + "=? or "
          + EquipmentMonitorStatusData.MOSCONTROLLER_NAME + "=?)";
      execute(vsSQL, isControllerID, isControllerID);
    }
    catch (NoSuchElementException nsee)
    {
      // Ignore
    }
  }
}
