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
import com.wynright.wrxj.app.Wynsoft;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to support database operations on the AES_SYS_INSTANCE_COMMUNICATIONS table
 */
public class InstanceCommunications extends WynsoftDBInterface
{
  public InstanceCommunications()
  {
    super("AED", 
        "AES_SYS_INSTANCE_COMMUNICATIONS",
        "AES_SYS_INSTANCE_COMMUNICATIONS",
        Factory.create(InstanceCommunicationsData.class));
  }

  /**
   * Get a list of AEMessenger connections.
   * 
   * TODO: Base Wynsoft-Move this somewhere more appropriate when implementing AES_SYS_INSTANCE_COMMUNICATIONS, AES_SYS_INSTANCES, and AES_SYS_PRODUCTS
   * 
   * @return
   */
  @SuppressWarnings("rawtypes")
  public Map<Integer, String> getAeMessengerConnections() throws DBException
  {
    Map<Integer, String> vpConnectionMap = new HashMap<>();
    /*
      select c.SENDER_ID, p.NAME from AES_SYS_INSTANCE_COMMUNICATIONS c
        inner join AES_SYS_INSTANCES i on i.ID = c.SENDER_ID
        inner join AES_SYS_PRODUCTS p on p.ID = i.PRODUCT_ID
      where RECEIVER_ID=? and COMMUNICATION_TYPE_ID=2
      order by c.SENDER_ID
     */
    StringBuilder sql = new StringBuilder();
    sql.append("select c.SENDER_ID, p.NAME from ").append(getSchema("AED")).append("AES_SYS_INSTANCE_COMMUNICATIONS c")
       .append(" inner join ").append(getSchema("AED")).append("AES_SYS_INSTANCES i on i.ID = c.SENDER_ID")
       .append(" inner join ").append(getSchema("AED")).append("AES_SYS_PRODUCTS p on p.ID = i.PRODUCT_ID")
       .append(" where RECEIVER_ID=? and COMMUNICATION_TYPE_ID=2")
       .append(" order by c.SENDER_ID");
    List<Map> vpGsList = fetchRecords(sql.toString(), Wynsoft.getInstanceId());
    for (Map m : vpGsList)
    {
      Integer vnInstance = (Integer)m.get("SENDER_ID");
      String vsSenderProduct = (String)m.get("NAME");
      vpConnectionMap.put(vnInstance, vsSenderProduct);
    }
    return vpConnectionMap;
  }
  
  /**
   * Get a particular InstanceCommunications record
   * 
   * @param inSender
   * @param inReceiver
   * @param inCommType
   * @return
   * @throws DBException
   */
  public InstanceCommunicationsData getData(int inSender, int inCompID,
      int inReceiver, int inCommType) throws DBException
  {
  	InstanceCommunicationsData vpInstanceCommunicationsData = Factory.create(InstanceCommunicationsData.class);
    vpInstanceCommunicationsData.setKey(InstanceCommunicationsData.SENDER_ID_NAME, inSender);
    vpInstanceCommunicationsData.setKey(InstanceCommunicationsData.SENDER_COMPONENT_ID_NAME, inCompID);
    vpInstanceCommunicationsData.setKey(InstanceCommunicationsData.RECEIVER_ID_NAME, inReceiver);
    vpInstanceCommunicationsData.setKey(InstanceCommunicationsData.COMMUNICATION_TYPE_ID_NAME, inCommType);
    
    return getElement(vpInstanceCommunicationsData, DBConstants.NOWRITELOCK);
  }
  
  /**
   * Get a InstanceCommunications list.  Returns InstanceCommunicationsData 
   * fields plus SENDER_NAME, RECEIVER_NAME, COMM_TYPE_NAME, and COMM_INTERFACE.
   * 
   * @return
   * @throws DBException
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getList(int inInstanceId) throws DBException
  {
    /*
      SELECT
          ic.SENDER_ID
        , si_s.IDENTITY_NAME as 'SENDER_NAME'
        , ic.SENDER_COMPONENT_ID
        , ic.RECEIVER_ID
        , si_r.IDENTITY_NAME as 'RECEIVER_NAME'
        , ic.COMMUNICATION_TYPE_ID
        , ct.Name as 'COMM_TYPE_NAME'
        , case  
              when ct.id=1 then si_r.DB_SERVER + '\' + si_r.DB_NAME 
              when ct.id=2 then cast(si_r.IP_ADDRESS as varchar) + ':' + cast(si_r.INTERNAL_PORT as varchar)
              when ct.id=3 then cast(si_r.IP_ADDRESS as varchar) + ':' + cast(si_r.INTERNAL_PORT as varchar)
              else 'Unknown'
          end as 'COMM_INTERFACE'
        FROM S_AED_AES_SYS_INSTANCE_COMMUNICATIONS ic
        inner join S_AED_AES_SYS_INSTANCES si_s on ic.SENDER_ID = si_s.ID
        inner join S_AED_AES_SYS_INSTANCES si_r on ic.RECEIVER_ID = si_r.ID
        inner join S_AED_AES_SYS_COMMUNICATION_TYPES ct on ic.COMMUNICATION_TYPE_ID = ct.Id
        where ic.SENDER_ID=? or ic.RECEIVER_ID=?
     */
    
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT")
       .append(" ic.SENDER_ID")
       .append(" , si_s.IDENTITY_NAME as 'SENDER_NAME'")
       .append(" , ic.SENDER_COMPONENT_ID")
       .append(" , ic.RECEIVER_ID")
       .append(" , si_r.IDENTITY_NAME as 'RECEIVER_NAME'")
       .append(" , ic.COMMUNICATION_TYPE_ID")
       .append(" , ct.Name as 'COMM_TYPE_NAME'")
       .append(" , case")
       .append("       when ct.id=1 then si_r.DB_SERVER + ':' + si_r.DB_NAME")
       .append("       when ct.id=2 then cast(si_r.IP_ADDRESS as varchar) + ':' + cast(si_r.INTERNAL_PORT as varchar)")
       .append("       when ct.id=3 then cast(si_r.IP_ADDRESS as varchar) + ':' + cast(si_r.INTERNAL_PORT as varchar)")
       .append("       else 'Unknown'")
       .append("   end as 'COMM_INTERFACE'")
       .append(" FROM S_AED_AES_SYS_INSTANCE_COMMUNICATIONS ic")
       .append(" inner join S_AED_AES_SYS_INSTANCES si_s on ic.SENDER_ID = si_s.ID")
       .append(" inner join S_AED_AES_SYS_INSTANCES si_r on ic.RECEIVER_ID = si_r.ID")
       .append(" inner join S_AED_AES_SYS_COMMUNICATION_TYPES ct on ic.COMMUNICATION_TYPE_ID = ct.Id")
       .append(" where ic.SENDER_ID=? or ic.RECEIVER_ID=?");
    
    return fetchRecords(sql.toString(), inInstanceId, inInstanceId);
  }

  /**
   * Delete a particular InstanceCommunications record
   * 
   * @param inSender
   * @param inCompID
   * @param inReceiver
   * @param inCommType
   * @throws DBException
   */
  public void delete(int inSender, int inCompID, int inReceiver, int inCommType)
      throws DBException
  {
    if (exists(inSender, inCompID, inReceiver, inCommType))
    {
      InstanceCommunicationsData vpInstanceCommunicationsData = Factory.create(InstanceCommunicationsData.class);
      vpInstanceCommunicationsData.setKey(InstanceCommunicationsData.SENDER_ID_NAME, inSender);
      vpInstanceCommunicationsData.setKey(InstanceCommunicationsData.SENDER_COMPONENT_ID_NAME, inCompID);
      vpInstanceCommunicationsData.setKey(InstanceCommunicationsData.RECEIVER_ID_NAME, inReceiver);
      vpInstanceCommunicationsData.setKey(InstanceCommunicationsData.COMMUNICATION_TYPE_ID_NAME, inCommType);
      deleteElement(vpInstanceCommunicationsData);
    }
  }

  /**
   * Determines whether or not a InstanceCommunications record exists
   * 
   * @param inSender
   * @param inCompID
   * @param inReceiver
   * @param inCommType
   * @return
   * @throws DBException
   */
  public boolean exists(int inSender, int inCompID, int inReceiver,
      int inCommType) throws DBException
  {
    return getData(inSender, inCompID, inReceiver, inCommType) != null;
  }
}
