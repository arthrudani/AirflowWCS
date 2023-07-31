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
package com.daifukuamerica.wrxj.dbadapter.data.aed;

import com.daifukuamerica.wrxj.dbadapter.WynsoftDBInterface;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import java.util.List;
import java.util.Map;

/**
 * Class to support database operations on the AES_SYS_WCF_SERVICES table
 */
public class WcfService extends WynsoftDBInterface
{
  public WcfService()
  {
    super("AED", "AES_SYS_WCF_SERVICES", "AES_SYS_WCF_SERVICES",
        Factory.create(WcfServiceData.class));
  }

  /**
   * Get a particular WcfService record
   * 
   * @param inID
   * @return
   * @throws DBException
   */
  public WcfServiceData getData(int inID)
      throws DBException
  {
  	WcfServiceData vpWcfServiceData = Factory.create(WcfServiceData.class);
    vpWcfServiceData.setKey(WcfServiceData.ID_NAME, inID);
    
    return getElement(vpWcfServiceData, DBConstants.NOWRITELOCK);
  }

  /**
   * Get a particular WcfService record
   * 
   * @param isClassName
   * @return
   * @throws DBException
   */
  public WcfServiceData getData(String isClassName)
      throws DBException
  {
    WcfServiceData vpWcfServiceData = Factory.create(WcfServiceData.class);
    vpWcfServiceData.setKey(WcfServiceData.CLASSNAME_NAME, isClassName);
    
    return getElement(vpWcfServiceData, DBConstants.NOWRITELOCK);
  }

  /**
   * Get a WcfService list
   * 
   * @return
   * @throws DBException
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getList() throws DBException
  {
  	WcfServiceData vpWcfServiceData = Factory.create(WcfServiceData.class);
    vpWcfServiceData.addOrderByColumn(WcfServiceData.CLASSNAME_NAME);

    return getAllElements(vpWcfServiceData);
  }

  /**
   * Delete a particular WcfService record
   * 
   * @param inID
   * @throws DBException
   */
  public void delete(int inID) throws DBException
  {
    if (exists(inID))
    {
      WcfServiceData vpWcfServiceData = Factory.create(WcfServiceData.class);
      vpWcfServiceData.setKey(WcfServiceData.ID_NAME, inID);
      deleteElement(vpWcfServiceData);
    }
  }

  /**
   * Determines whether or not a WcfService record exists
   * 
   * @param inID
   * @return
   * @throws DBException
   */
  public boolean exists(int inID) throws DBException
  {
    return getData(inID) != null;
  }

  /**
   * Update a particular WcfService record
   * 
   * @param ipWcfServiceData
   * @throws DBException
   */
  public void update(WcfServiceData ipWcfServiceData)
      throws DBException
  {
    if (ipWcfServiceData.getKeyCount() == 0)
    {
      ipWcfServiceData.setKey(WcfServiceData.ID_NAME, ipWcfServiceData.getId());
      ipWcfServiceData.deleteColumnObject(WcfServiceData.ID_NAME);
    }
    modifyElement(ipWcfServiceData);
  }
}
