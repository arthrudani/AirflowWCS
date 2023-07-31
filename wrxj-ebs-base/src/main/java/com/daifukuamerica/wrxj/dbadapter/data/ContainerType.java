package com.daifukuamerica.wrxj.dbadapter.data;

/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright (c) 2004-2008 Daifuku America Corporation  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Description:<BR>
 * Title: Class to handle Container Type Object. Description : Handles all
 * reading and writing database for ContainerType
 * 
 * @author REA
 * @version 1.0 02/25/02
 * @version 2.0 16-Nov-04
 * @version 3.0 06-Jun-08
 */
public class ContainerType extends BaseDBInterface
{
  
  private ContainerTypeData mpCTData;

  public ContainerType()
  {
    super("ContainerType");
    mpCTData = Factory.create(ContainerTypeData.class);
  }

  /**
   * Get a particular ContainerType record
   * 
   * @param isContainerType
   * @return
   * @throws DBException
   */
  public ContainerTypeData getContainerData(String isContainerType)
      throws DBException
  {
    mpCTData.clear();
    mpCTData.setKey(ContainerTypeData.CONTAINERTYPE_NAME, isContainerType);
    
    return getElement(mpCTData, DBConstants.NOWRITELOCK);
  }
  
  /**
   * Get a list of containers in the system
   * 
   * @return
   * @throws DBException
   */
  public List<Map> getContainerDataList() throws DBException
  {
    mpCTData.clear();
    mpCTData.addOrderByColumn(ContainerTypeData.CONTAINERTYPE_NAME);

    return getAllElements(mpCTData);
  }

  /**
   * Delete a container
   * 
   * @param isContainerType
   * @throws DBException
   */
  public void deleteContainer(String isContainerType) throws DBException
  {
    if (doesContainerExist(isContainerType))
    {
      mpCTData.clear();
      mpCTData.setKey(ContainerTypeData.CONTAINERTYPE_NAME, isContainerType);
      deleteElement(mpCTData);
    }
  }

  /**
   * Determines whether or not a container exists
   * 
   * @param container
   * @return
   * @throws DBException
   */
  public boolean doesContainerExist(String container) throws DBException
  {
    return getContainerData(container) != null;
  }

  /**
   * Method to get List of ContainerType Types
   *
   * @return <code>List</code> of container types.
   * @throws DBException
   */
  public List<String> getContainerTypeList() throws DBException
  {
    mpCTData.clear();
    mpCTData.addOrderByColumn(ContainerTypeData.CONTAINERTYPE_NAME);
    List<Map> vpContainerList = getAllElements(mpCTData);
    List<String> vpContainerTypeList = new ArrayList<String>();
    for (Map m : vpContainerList)
    {
      String vsContainerType = DBHelper.getStringField(m,
          ContainerTypeData.CONTAINERTYPE_NAME);
      if (vsContainerType.trim().length() > 0)
      {
        vpContainerTypeList.add(vsContainerType);
      }
    }
    return vpContainerTypeList;
  }

  /**
   * Update container data
   * 
   * @param ipContainerData
   * @throws DBException
   */
  public void updateContainer(ContainerTypeData ipContainerData)
      throws DBException
  {
    ipContainerData.setKey(ContainerTypeData.CONTAINERTYPE_NAME,
        ipContainerData.getContainer());
    modifyElement(ipContainerData);
  }

  /**
   * Sets Objects for garbage collection.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpCTData = null;
  }
}
