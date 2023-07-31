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

import static com.daifukuamerica.wrxj.dbadapter.data.aed.InstanceCommunicationsEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.dbadapter.WynsoftData;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to hold AES_SYS_INSTANCE_COMMUNICATIONS data
 */
public class InstanceCommunicationsData extends WynsoftData
{
  public static final String COMMUNICATION_TYPE_ID_NAME= COMMUNICATION_TYPE_ID.getName();
  public static final String RECEIVER_ID_NAME         = RECEIVER_ID.getName();
  public static final String SENDER_COMPONENT_ID_NAME = SENDER_COMPONENT_ID.getName();
  public static final String SENDER_ID_NAME           = SENDER_ID.getName();

  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<>();

  // ------------------- InstanceCommunications table data -----------------------------
  private int    mnCommunicationTypeId;
  private int    mnReceiverId;
  private int    mnSenderComponentId;
  private int    mnSenderId;

  //-------------------- InstanceCommunications default data ---------------------------
  public InstanceCommunicationsData()
  {
    super();
    clear();
    initColumnMap(mpColumnMap, InstanceCommunicationsEnum.class);
  }

  @Override
  public String toString()
  {
    StringBuffer myString = new StringBuffer(getClass().getCanonicalName()).append("\n");
    String[] vasKeys = mpColumnMap.keySet().toArray(new String[0]);
    Arrays.sort(vasKeys);
    for (String sKey : vasKeys) {
      ColumnObject vpVal = getColumnObject(sKey);
      String vsVal = vpVal == null ? null : 
        vpVal.getColumnValue() == null ? null : vpVal.getColumnValue().toString();
      myString.append(" * ").append(sKey).append(" = ").append(vsVal).append(";\n");
    }
    return myString.toString();
  }

  @Override
  public boolean equals(AbstractSKDCData absOther)
  {
    InstanceCommunicationsData other = (InstanceCommunicationsData)absOther;
    return other.getSenderId() == getSenderId()
        && other.getReceiverId() == getReceiverId()
        && other.getCommunicationTypeId() == getCommunicationTypeId();
  }


/*---------------------------------------------------------------------------
   Getters
 ---------------------------------------------------------------------------*/
  public int    getCommunicationTypeId(){  return mnCommunicationTypeId; }
  public int    getReceiverId()         {  return mnReceiverId;           }
  public int    getSenderComponentId()  {  return mnSenderComponentId;   }
  public int    getSenderId()           {  return mnSenderId;             }


/*---------------------------------------------------------------------------
   Setters
 ---------------------------------------------------------------------------*/
  public void setCommunicationTypeId(int inCOMMUNICATION_TYPE_ID)
  {
    mnCommunicationTypeId = inCOMMUNICATION_TYPE_ID;
    addColumnObject(new ColumnObject(COMMUNICATION_TYPE_ID_NAME, mnCommunicationTypeId));
  }
  public void setReceiverId(int inReceiverId)
  {
    mnReceiverId = inReceiverId;
    addColumnObject(new ColumnObject(RECEIVER_ID_NAME, mnReceiverId));
  }
  public void setSenderComponentId(int inSenderComponentId)
  {
    mnSenderComponentId = inSenderComponentId;
    addColumnObject(new ColumnObject(SENDER_COMPONENT_ID_NAME, mnSenderComponentId));
  }
  public void setSenderId(int inSenderId)
  {
    mnSenderId = inSenderId;
    addColumnObject(new ColumnObject(SENDER_ID_NAME, mnSenderId));
  }

  /**
   * Required set field method. This method figures out what column was passed
   * to it and sets the value. This allows us to have a generic method for all
   * DB interfaces.
   */
  @Override
  public int setField(String isColName, Object ipColValue)
  {
    TableEnum vpEnum = mpColumnMap.get(isColName);
    if (vpEnum == null) 
    {
      return super.setField(isColName, ipColValue);
    }
    
    switch ((InstanceCommunicationsEnum)vpEnum)
    {
      case COMMUNICATION_TYPE_ID:
        setCommunicationTypeId((Integer)ipColValue);
        break;
      case RECEIVER_ID:
        setReceiverId((Integer)ipColValue);
        break;
      case SENDER_COMPONENT_ID:
        setSenderComponentId((Integer)ipColValue);
        break;
      case SENDER_ID:
        setSenderId((Integer)ipColValue);
        break;
      default:
        throw new IllegalArgumentException("Unknown field " + isColName);
    }
    return 0;
  }
}
