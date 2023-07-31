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

import static com.daifukuamerica.wrxj.dbadapter.data.WrxEquipLogEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to hold WRX_LOG_ERR data
 */
public class WrxEquipLogData extends AbstractSKDCData
{
  public static final String IDENTITY_NAME_NAME       = IDENTITY_NAME.getName();
  public static final String DATE_TIME_NAME           = DATE_TIME.getName();
  public static final String DEVICEID_NAME       	  = DEVICEID.getName();
  public static final String DIRECTION_NAME           = DIRECTION.getName();
  public static final String COUNT_NAME               = COUNT.getName();
  public static final String DATA_NAME                = DATA.getName();

  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  // ------------------- WrxLogError table data -----------------------------
   private Date   mdDateTime;
  private String msIdentityName;
  private String msDeviceid;
  private int	 iDirection;
  private int    mnCount;
  private String msData;

  //-------------------- WrxLogError default data ---------------------------
  public WrxEquipLogData()
  {
    super();
    clear();
    initColumnMap(mpColumnMap, WrxEquipLogEnum.class);
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
    WrxEquipLogData other = (WrxEquipLogData)absOther;
    return other.getDateTime().equals(getDateTime());
  }


/*---------------------------------------------------------------------------
   Getters
 ---------------------------------------------------------------------------*/
  public String getIdentityName()        {  return msIdentityName;         }
  public Date   getDateTime()            {  return mdDateTime;             }
  public String getDeviceid()       {  return msDeviceid;         }
  public int    getDirection()            {  return iDirection;              }
  public int getCount()              {  return mnCount;                }
  public String getData()             {  return msData;               }


/*---------------------------------------------------------------------------
   Setters
 ---------------------------------------------------------------------------*/
  public void setDeviceid(String isDeviceid)
  {
	msDeviceid = isDeviceid;
    addColumnObject(new ColumnObject(DEVICEID_NAME, msDeviceid));
  }
  public void setDateTime(Date idDateTime)
  {
    mdDateTime = idDateTime;
    addColumnObject(new ColumnObject(DATE_TIME_NAME, mdDateTime));
  }
  public void setData(String isData)
  {
	msData = isData;
    addColumnObject(new ColumnObject(DATA_NAME, msData));
  }
  public void setIdentityName(String isIdentityName)
  {
    msIdentityName = isIdentityName;
    addColumnObject(new ColumnObject(IDENTITY_NAME_NAME, msIdentityName));
  }
  public void setDirection(int inDirection)
  {
	iDirection = inDirection;
    addColumnObject(new ColumnObject(DIRECTION_NAME, iDirection));
  }
  public void setCount(int inCount)
  {
	mnCount = inCount;
    addColumnObject(new ColumnObject(COUNT_NAME, mnCount));
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
    
    switch ((WrxEquipLogEnum)vpEnum)
    {
      case DATE_TIME:
        setDateTime((Date)ipColValue);
        break;
      case IDENTITY_NAME:
        setIdentityName((String)ipColValue);
        break;
      case COUNT:
        setCount((Integer)ipColValue);
        break;
      case DIRECTION:
          setDirection((Integer)ipColValue);
          break;
      case DEVICEID:
        setDeviceid((String)ipColValue);
        break;
      case DATA:
        setData((String)ipColValue);
        break;
      default:
        throw new IllegalArgumentException("Unknown field " + isColName);
    }
    return 0;
  }
}
