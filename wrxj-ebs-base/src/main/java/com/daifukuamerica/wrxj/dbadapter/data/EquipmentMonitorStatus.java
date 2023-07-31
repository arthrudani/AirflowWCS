/* ***************************************************************************
  Copyright (c) 2018 Daifuku North America Holding Company. All Rights Reserved.

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
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import java.util.List;
import java.util.Map;

/**
 * Class to support database operations on the EQUIPMENTMONITORSHAPE table
 */
public class EquipmentMonitorStatus extends BaseDBInterface
{
  public EquipmentMonitorStatus()
  {
    super("EquipmentMonitorStatus");
  }

  /**
   * Get a particular EquipmentMonitorShape record
   * 
   * @param isKey
   * @return
   * @throws DBException
   */
  public EquipmentMonitorStatusData getData(String isKey)
      throws DBException
  {
  	EquipmentMonitorStatusData vpEquipmentMonitorShapeData = Factory.create(EquipmentMonitorStatusData.class);
    vpEquipmentMonitorShapeData.setKey(EquipmentMonitorStatusData.GRAPHICID_NAME, isKey);
    return getElement(vpEquipmentMonitorShapeData, DBConstants.NOWRITELOCK);
  }
  
  /**
   * Get a EquipmentMonitorShape list
   * 
   * @return
   * @throws DBException
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getList() throws DBException
  {
  	EquipmentMonitorStatusData vpEquipmentMonitorShapeData = Factory.create(EquipmentMonitorStatusData.class);
    vpEquipmentMonitorShapeData.addOrderByColumn(EquipmentMonitorStatusData.GRAPHICID_NAME);
    return getAllElements(vpEquipmentMonitorShapeData);
  }

  /**
   * Delete a particular EquipmentMonitorShape record
   * 
   * @param isKey
   * @throws DBException
   */
  public void delete(String isKey) throws DBException
  {
    if (exists(isKey))
    {
      EquipmentMonitorStatusData vpEquipmentMonitorShapeData = Factory.create(EquipmentMonitorStatusData.class);
      vpEquipmentMonitorShapeData.setKey(EquipmentMonitorStatusData.GRAPHICID_NAME, isKey);
      deleteElement(vpEquipmentMonitorShapeData);
    }
  }

  /**
   * Determines whether or not a EquipmentMonitorShape record exists
   * 
   * @param isKey
   * @return
   * @throws DBException
   */
  public boolean exists(String isKey) throws DBException
  {
    return getData(isKey) != null;
  }

  /**
   * Update a particular EquipmentMonitorShape record
   * 
   * @param ipEquipmentMonitorShapeData
   * @throws DBException
   */
  public void update(EquipmentMonitorStatusData ipEquipmentMonitorShapeData)
      throws DBException
  {
    if (ipEquipmentMonitorShapeData.getKeyCount() == 0)
    {
      ipEquipmentMonitorShapeData.setKey(EquipmentMonitorStatusData.GRAPHICID_NAME, ipEquipmentMonitorShapeData.getGraphicID());
      ipEquipmentMonitorShapeData.deleteColumnObject(EquipmentMonitorStatusData.GRAPHICID_NAME);
    }
    modifyElement(ipEquipmentMonitorShapeData);
  }
}
