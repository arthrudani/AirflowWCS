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
package com.daifukuoc.wrxj.custom.ebs.dbadapter.data;

import static com.daifukuoc.wrxj.custom.ebs.dbadapter.data.WrxHostLogEnum.*;

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
public class WrxHostLogData extends AbstractSKDCData
{
	  
  public static final String DATE_TIME_NAME           	= DATE_TIME.getName();
  public static final String HOSTNAME_NAME       	  	= HOSTNAME.getName();
  public static final String DIRECTION_NAME           	= DIRECTION.getName();
  public static final String MESSAGESEQUENCE_NAME       = MESSAGESEQUENCE.getName();
  public static final String MESSAGEPROCESSED_NAME      = MESSAGEPROCESSED.getName();
  public static final String MESSAGEIDENTIFIER_NAME     = MESSAGEIDENTIFIER.getName();
  public static final String MESSAGE_NAME               = MESSAGE.getName();

  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  // ------------------- WrxLogError table data -----------------------------
  private Date   mdDateTime;
  private String msHostName;
  private String msDirection;
  private int    mnSequence;
  private int    mnProcessed;
  private String msIdentifier;
  private String msMessage;

  //-------------------- WrxLogError default data ---------------------------
  public WrxHostLogData()
  {
    super();
    clear();
    initColumnMap(mpColumnMap, WrxHostLogEnum.class);
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
    WrxHostLogData other = (WrxHostLogData)absOther;
    return other.getDateTime().equals(getDateTime());
  }

/*---------------------------------------------------------------------------
   Getters
 ---------------------------------------------------------------------------*/
  public Date   getDateTime()           {  return mdDateTime;             }
  public String getHostName()        	{  return msHostName;         }
  public String getDirection()       	{  return msDirection;         }
  public int    getSequence()           {  return mnSequence;              }
  public int    getSProcessed()         {  return mnProcessed;              }
  public String getIdentifier()         {  return msIdentifier;                }
  public String getMessage()            {  return msMessage;               }


/*---------------------------------------------------------------------------
   Setters
 ---------------------------------------------------------------------------*/

  public void setDateTime(Date idDateTime)
  {
    mdDateTime = idDateTime;
    addColumnObject(new ColumnObject(DATE_TIME_NAME, mdDateTime));
  }
  public void setHostname(String isHostName)
  {
	  msHostName = isHostName;
    addColumnObject(new ColumnObject(HOSTNAME_NAME, msHostName));
  }
  public void setDirection(String isDirection)
  {
	msDirection = isDirection;
    addColumnObject(new ColumnObject(DIRECTION_NAME, msDirection));
  }
  public void setSequence(int inSequence)
  {
	mnSequence = inSequence;
    addColumnObject(new ColumnObject(MESSAGESEQUENCE_NAME, mnSequence));
  }
  public void setProcessed(int inProcessed)
  {
	mnProcessed = inProcessed;
    addColumnObject(new ColumnObject(MESSAGEPROCESSED_NAME, mnProcessed));
  }
  public void setIdentifier(String isIdentifier)
  {
	msIdentifier = isIdentifier;
    addColumnObject(new ColumnObject(MESSAGEIDENTIFIER_NAME, msIdentifier));
  }
  public void setMessage(String isMessage)
  {
	msMessage = isMessage;
    addColumnObject(new ColumnObject(MESSAGE_NAME, msMessage));
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
    
    switch ((WrxHostLogEnum)vpEnum)
    {
      case DATE_TIME:
        setDateTime((Date)ipColValue);
        break;
      default:
        throw new IllegalArgumentException("Unknown field " + isColName);
    }
    return 0;
  }
}
