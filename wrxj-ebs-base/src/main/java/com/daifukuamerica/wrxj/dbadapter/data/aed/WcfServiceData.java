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

import static com.daifukuamerica.wrxj.dbadapter.data.aed.WcfServiceEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.dbadapter.WynsoftData;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to hold AES_SYS_WCF_SERVICES data
 */
public class WcfServiceData extends WynsoftData
{
  public static final String ID_NAME                  = ID.getName();
  public static final String BINDINGID_NAME           = BINDINGID.getName();
  public static final String CLASSNAME_NAME           = CLASSNAME.getName();
  public static final String HOSTINGINSTANCEID_NAME   = HOSTINGINSTANCEID.getName();
  public static final String INTERFACENAME_NAME       = INTERFACENAME.getName();

  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<>();

  // ------------------- WcfService table data -----------------------------
  private int    mnId;
  private int    mnBindingId;
  private String msClassName;
  private int    mnHostingInstanceId;
  private String msInterfaceName;

  //-------------------- WcfService default data ---------------------------
  public WcfServiceData()
  {
    super();
    clear();
    initColumnMap(mpColumnMap, WcfServiceEnum.class);
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
    WcfServiceData other = (WcfServiceData)absOther;
    return other.getId() == getId();
  }


/*---------------------------------------------------------------------------
   Getters
 ---------------------------------------------------------------------------*/
  public int    getId()                  {  return mnId;                    }
  public int    getBindingId()           {  return mnBindingId;             }
  public String getClassName()           {  return msClassName;             }
  public int    getHostingInstanceId()   {  return mnHostingInstanceId;     }
  public String getInterfaceName()       {  return msInterfaceName;         }


/*---------------------------------------------------------------------------
   Setters
 ---------------------------------------------------------------------------*/
  public void setId(int inId)
  {
    mnId = inId;
    addColumnObject(new ColumnObject(ID_NAME, mnId));
  }
  public void setBindingId(int inBindingId)
  {
    mnBindingId = inBindingId;
    addColumnObject(new ColumnObject(BINDINGID_NAME, mnBindingId));
  }
  public void setClassName(String isClassName)
  {
    msClassName = isClassName;
    addColumnObject(new ColumnObject(CLASSNAME_NAME, msClassName));
  }
  public void setHostingInstanceId(int inHostingInstanceId)
  {
    mnHostingInstanceId = inHostingInstanceId;
    addColumnObject(new ColumnObject(HOSTINGINSTANCEID_NAME, mnHostingInstanceId));
  }
  public void setInterfaceName(String isInterfaceName)
  {
    msInterfaceName = isInterfaceName;
    addColumnObject(new ColumnObject(INTERFACENAME_NAME, msInterfaceName));
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
    
    switch ((WcfServiceEnum)vpEnum)
    {
      case ID:
        setId((Integer)ipColValue);
        break;
      case BINDINGID:
        setBindingId((Integer)ipColValue);
        break;
      case CLASSNAME:
        setClassName((String)ipColValue);
        break;
      case HOSTINGINSTANCEID:
        setHostingInstanceId((Integer)ipColValue);
        break;
      case INTERFACENAME:
        setInterfaceName((String)ipColValue);
        break;
      default:
        throw new IllegalArgumentException("Unknown field " + isColName);
    }
    return 0;
  }
}
