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

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.WynsoftDBInterface;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBResultSet;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to support database operations on the AES_SYS_COMMUNICATION_TYPES table
 */
public class CommunicationType extends WynsoftDBInterface
{
  public CommunicationType()
  {
    super("AED", "AES_SYS_COMMUNICATION_TYPES", "AES_SYS_COMMUNICATION_TYPES",
        Factory.create(CommunicationTypeData.class));
  }

  /**
   * Get a particular CommunicationType record
   * 
   * @param inKey
   * @return
   * @throws DBException
   */
  public CommunicationTypeData getData(Integer inKey)
      throws DBException
  {
  	CommunicationTypeData vpCommunicationTypeData = Factory.create(CommunicationTypeData.class);
    vpCommunicationTypeData.setKey(CommunicationTypeData.ID_NAME, inKey);
    return getElement(vpCommunicationTypeData, DBConstants.NOWRITELOCK);
  }
  
  /**
   * Get a CommunicationType list
   * 
   * @return
   * @throws DBException
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getList() throws DBException
  {
  	CommunicationTypeData vpCommunicationTypeData = Factory.create(CommunicationTypeData.class);
    vpCommunicationTypeData.addOrderByColumn(CommunicationTypeData.ID_NAME);
    return getAllElements(vpCommunicationTypeData);
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

    CommunicationTypeData vpCommunicationTypeData = Factory.create(CommunicationTypeData.class);
    vpCommunicationTypeData.addOrderByColumn(CommunicationTypeData.ID_NAME);
    List<CommunicationTypeData> vpList =  getAllElementsAsData(vpCommunicationTypeData);
    for (CommunicationTypeData vpData : vpList)
    {
      m.put(vpData.getId(), vpData.getName());
    }
    return m;
  }
  
  /*========================================================================*/
  /* No write operations to enum table                                      */
  /*========================================================================*/
  @Override
  protected DBResultSet addData(ColumnObject[] iapColumns, String isTableName)
      throws DBException
  {
    throw new UnsupportedOperationException("Cannot insert to " + getWriteTableName());
  }
  
  @Override
  public void addElement(AbstractSKDCData ipAddData) throws DBException
  {
    throw new UnsupportedOperationException("Cannot insert to " + getWriteTableName());
  }
  
  @Override
  public Object addElementWithKey(AbstractSKDCData ipAddData) throws DBException
  {
    throw new UnsupportedOperationException("Cannot insert to " + getWriteTableName());
  }

  @Override
  protected void deleteData(KeyObject[] iapKeys, String isTableName)
      throws DBException
  {
    throw new UnsupportedOperationException("Cannot delete from " + getWriteTableName());
  }
  
  @Override
  public void deleteElement(AbstractSKDCData ipDelKey) throws DBException
  {
    throw new UnsupportedOperationException("Cannot delete from " + getWriteTableName());
  }
  
  @Override
  protected void modifyData(ColumnObject[] iapColumns, KeyObject[] iapKeys,
      String isTableName) throws DBException
  {
    throw new UnsupportedOperationException("Cannot update " + getWriteTableName());
  }
  
  @Override
  public void modifyElement(AbstractSKDCData ipModData) throws DBException
  {
    throw new UnsupportedOperationException("Cannot update " + getWriteTableName());
  }
}
