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

import static com.daifukuamerica.wrxj.dbadapter.data.WrxLogEnum.*;

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
public class WrxLogData extends AbstractSKDCData
{
  public static final String AREA_NAME                = AREA.getName();
  public static final String DATE_TIME_NAME           = DATE_TIME.getName();
  public static final String DESCRIPTION_NAME         = DESCRIPTION.getName();
  public static final String IDENTITY_NAME_NAME       = IDENTITY_NAME.getName();
  public static final String INFOWARNFATAL_NAME       = INFOWARNFATAL.getName();
  public static final String POSITION_NAME            = POSITION.getName();
  public static final String SOURCE_NAME              = SOURCE.getName();
  public static final String SUBJECT_NAME             = SUBJECT.getName();

  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  // ------------------- WrxLogError table data -----------------------------
  private String msArea;
  private Date   mdDateTime;
  private String msDescription;
  private String msIdentityName;
  private String msInfoWarnFatal;
  private int    mnPosition;
  private String msSource;
  private String msSubject;

  //-------------------- WrxLogError default data ---------------------------
  public WrxLogData()
  {
    super();
    clear();
    initColumnMap(mpColumnMap, WrxLogEnum.class);
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
    WrxLogData other = (WrxLogData)absOther;
    return other.getDateTime().equals(getDateTime())
        && other.getDescription().equals(getDescription());
  }


/*---------------------------------------------------------------------------
   Getters
 ---------------------------------------------------------------------------*/
  public String getArea()                {  return msArea;                  }
  public Date   getDateTime()            {  return mdDateTime;             }
  public String getDescription()         {  return msDescription;           }
  public String getIdentityName()        {  return msIdentityName;         }
  public String getInfoWarnFatal()       {  return msInfoWarnFatal;         }
  public int    getPosition()            {  return mnPosition;              }
  public String getSource()              {  return msSource;                }
  public String getSubject()             {  return msSubject;               }


/*---------------------------------------------------------------------------
   Setters
 ---------------------------------------------------------------------------*/
  public void setArea(String isArea)
  {
    msArea = isArea;
    addColumnObject(new ColumnObject(AREA_NAME, msArea));
  }
  public void setDateTime(Date idDateTime)
  {
    mdDateTime = idDateTime;
    addColumnObject(new ColumnObject(DATE_TIME_NAME, mdDateTime));
  }
  public void setDescription(String isDescription)
  {
    msDescription = isDescription;
    addColumnObject(new ColumnObject(DESCRIPTION_NAME, msDescription));
  }
  public void setIdentityName(String isIdentityName)
  {
    msIdentityName = isIdentityName;
    addColumnObject(new ColumnObject(INFOWARNFATAL_NAME, msIdentityName));
  }
  public void setInfoWarnFatal(String isInfoWarnFatal)
  {
    msInfoWarnFatal = isInfoWarnFatal;
    addColumnObject(new ColumnObject(INFOWARNFATAL_NAME, msInfoWarnFatal));
  }
  public void setPosition(int inPosition)
  {
    mnPosition = inPosition;
    addColumnObject(new ColumnObject(POSITION_NAME, mnPosition));
  }
  public void setSource(String isSource)
  {
    msSource = isSource;
    addColumnObject(new ColumnObject(SOURCE_NAME, msSource));
  }
  public void setSubject(String isSubject)
  {
    msSubject = isSubject;
    addColumnObject(new ColumnObject(SUBJECT_NAME, msSubject));
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
    
    switch ((WrxLogEnum)vpEnum)
    {
      case AREA:
        setArea((String)ipColValue);
        break;
      case DATE_TIME:
        setDateTime((Date)ipColValue);
        break;
      case DESCRIPTION:
        setDescription((String)ipColValue);
        break;
      case IDENTITY_NAME:
        setIdentityName((String)ipColValue);
        break;
      case INFOWARNFATAL:
        setInfoWarnFatal((String)ipColValue);
        break;
      case POSITION:
        setPosition((Integer)ipColValue);
        break;
      case SOURCE:
        setSource((String)ipColValue);
        break;
      case SUBJECT:
        setSubject((String)ipColValue);
        break;
      default:
        throw new IllegalArgumentException("Unknown field " + isColName);
    }
    return 0;
  }
}
