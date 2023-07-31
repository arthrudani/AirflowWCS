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

import static com.daifukuamerica.wrxj.dbadapter.data.aed.CommunicationTypeEnum.ID;
import static com.daifukuamerica.wrxj.dbadapter.data.aed.CommunicationTypeEnum.NAME;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.dbadapter.WynsoftData;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to hold AES_SYS_COMMUNICATION_TYPES data
 */
public class CommunicationTypeData extends WynsoftData
{
  public static final String ID_NAME                  = ID.getName();
  public static final String NAME_NAME                = NAME.getName();

  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<>();

  // This DB table is really a container for an enum.  These are the values 
  // that are understood:
  public static final int COMM_TYPE_DB = 1;
  public static final int COMM_TYPE_TCP = 2; 
  
  // ------------------- CommunicationType table data -----------------------------
  private int    mnId;
  private String msName;

  //-------------------- CommunicationType default data ---------------------------
  public CommunicationTypeData()
  {
    super();
    clear();
    initColumnMap(mpColumnMap, CommunicationTypeEnum.class);
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
    CommunicationTypeData other = (CommunicationTypeData)absOther;
    return other.getId() == getId();
  }

/*---------------------------------------------------------------------------
   Getters
 ---------------------------------------------------------------------------*/
  public int    getId()                  {  return mnId;                    }
  public String getName()                {  return msName;                  }


/*---------------------------------------------------------------------------
   Setters
 ---------------------------------------------------------------------------*/
  public void setId(int inId)
  {
    mnId = inId;
    addColumnObject(new ColumnObject(ID_NAME, mnId));
  }
  public void setName(String isName)
  {
    msName = isName;
    addColumnObject(new ColumnObject(NAME_NAME, msName));
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
    
    switch ((CommunicationTypeEnum)vpEnum)
    {
      case ID:
        setId((Integer)ipColValue);
        break;
      case NAME:
        setName((String)ipColValue);
        break;
      default:
        throw new IllegalArgumentException("Unknown field " + isColName);
    }
    return 0;
  }
}
