/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright (c) 2017 Daifuku North America Holding Company. All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.dbadapter.data.aed;

import com.daifukuamerica.wrxj.dbadapter.WynsoftDBInterface;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to support database operations on the AES_SYS_INSTANCES table
 */
public class Instance extends WynsoftDBInterface
{
  public Instance()
  {
    super("AED", "AES_SYS_INSTANCES", "AES_SYS_INSTANCES",
        Factory.create(InstanceData.class));
  }

  /**
   * Get a particular Instance record
   * 
   * @param inId
   * @return
   * @throws DBException
   */
  public InstanceData getData(Integer inId)
      throws DBException
  {
    InstanceData vpInstanceData = Factory.create(InstanceData.class);
    vpInstanceData.setKey(InstanceData.ID_NAME, inId);
    return getElement(vpInstanceData, DBConstants.NOWRITELOCK);
  }
  
  /**
   * Get a particular Instance record
   * 
   * @param isIdentityName
   * @return
   * @throws DBException
   */
  public InstanceData getData(String isIdentityName)
      throws DBException
  {
    InstanceData vpInstanceData = Factory.create(InstanceData.class);
    vpInstanceData.setKey(InstanceData.IDENTITY_NAME_NAME, isIdentityName);
    return getElement(vpInstanceData, DBConstants.NOWRITELOCK);
  }
  
  /**
   * Get a Instance list
   * 
   * @return
   * @throws DBException
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getList() throws DBException
  {
  	InstanceData vpInstanceData = Factory.create(InstanceData.class);
    vpInstanceData.addOrderByColumn(InstanceData.ID_NAME);
    return getAllElements(vpInstanceData);
  }

  /**
   * Get a Instance list for a product
   * 
   * @param inProductId
   * @return
   * @throws DBException
   */
  public List<InstanceData> getDataList(int inProductId) throws DBException
  {
    InstanceData vpInstanceData = Factory.create(InstanceData.class);
    vpInstanceData.setKey(InstanceData.PRODUCT_ID_NAME, inProductId);
    vpInstanceData.addOrderByColumn(InstanceData.ID_NAME);
    return getAllElementsAsData(vpInstanceData);
  }

  /**
   * Get a CommunicationType list for combo boxes
   * 
   * @return
   * @throws DBException
   */
  public Map<Integer, String> getTranslationMap() throws DBException
  {
    Map<Integer, String> m = new HashMap<>();

    InstanceData vpInstanceData = Factory.create(InstanceData.class);
    vpInstanceData.addOrderByColumn(CommunicationTypeData.ID_NAME);
    List<InstanceData> vpList =  getAllElementsAsData(vpInstanceData);
    for (InstanceData vpData : vpList)
    {
      // WRx uses a long ID, but we need integers for the translation map.
      // This should be okay, since there should not be any numeric overflow 
      // in this data.
      m.put((int)vpData.getID(), vpData.getIdentityName());
    }
    return m;
  }
  
  /**
   * Delete a particular Instance record
   * 
   * @param inID
   * @throws DBException
   */
  public void delete(Integer inID) throws DBException
  {
    if (exists(inID))
    {
      InstanceData vpInstanceData = Factory.create(InstanceData.class);
      vpInstanceData.setKey(InstanceData.ID_NAME, inID);
      deleteElement(vpInstanceData);
    }
  }

  /**
   * Determines whether or not a Instance record exists
   * 
   * @param inID
   * @return
   * @throws DBException
   */
  public boolean exists(Integer inID) throws DBException
  {
    return getData(inID) != null;
  }

  /**
   * Update a particular Instance record
   * 
   * @param ipInstanceData
   * @throws DBException
   */
  public void update(InstanceData ipInstanceData)
      throws DBException
  {
    if (ipInstanceData.getKeyCount() == 0)
    {
      ipInstanceData.setKey(InstanceData.ID_NAME, ipInstanceData.getID());
      ipInstanceData.deleteColumnObject(InstanceData.ID_NAME);
    }
    modifyElement(ipInstanceData);
  }
}
