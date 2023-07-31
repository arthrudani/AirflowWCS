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
  CONSENT OF Daifuku America Corporation ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/

import static com.daifukuamerica.wrxj.dbadapter.data.RouteEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:<BR>
 *   Class to handle Route Data operations.  This class treats columns, and
 *   Keys as Objects.
 *
 * @author       A.T.
 * @version      1.0
 */
public class RouteData extends AbstractSKDCData
{
  public static final String ROUTEID_NAME    = ROUTEID.getName();
  public static final String FROMID_NAME     = FROMID.getName();
  public static final String DESTID_NAME     = DESTID.getName();
  public static final String FROMTYPE_NAME   = FROMTYPE.getName();
  public static final String DESTTYPE_NAME   = DESTTYPE.getName();
  public static final String ROUTEONOFF_NAME = ROUTEONOFF.getName();

  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  private String sRouteID = "";
  private String sFromID = "";
  private String sDestID = "";
  private int    iRouteOnOff;
  private int    iFromType;
  private int    iDestType;

  public RouteData()
  {
//    super();
    initColumnMap(mpColumnMap, RouteEnum.class);
    clear();
  }

  /**
   * Description: Clears all data
   */
  @Override
  public void clear()
  {
    super.clear();

    sRouteID = "";
    sFromID = "";
    sDestID = "";
    iRouteOnOff = DBConstants.ON;
    iFromType = DBConstants.STATION;
    iDestType = DBConstants.STATION;
  }


  /**
   * This helps in debugging when we want to print the whole structure.
   */
  @Override
  public String toString()
  {
    String s = "sRouteID:'" + sRouteID +
               "\nsFromID:'" + sFromID +
               "\nsDestID:'" + sDestID + "\n";

    try
    {
      s = s + "iFromType:" + DBTrans.getStringValue( "iFromType", iFromType);
      s = s + "iDestType:" + DBTrans.getStringValue( "iDestType", iDestType);
      s = s + "iRouteOnOff:" + DBTrans.getStringValue( "iRouteOnOff", iRouteOnOff);
    }
    catch(NoSuchFieldException e)
    {
      s = s + "0";
    }
    s += super.toString();

    return(s);
  }

  /**
   * Defines equality between two RouteData objects.
   *
   * @param  absRT <code>AbstractSKDCData</code> reference whose runtime type
   *         is expected to be <code>RouteData</code>
   */
  @Override
  public boolean equals(AbstractSKDCData absRT)
  {
    if (absRT == null || !(absRT instanceof RouteData))
    {
      return(false);
    }

    RouteData rtdata = (RouteData)absRT;
    return(rtdata.sRouteID.equals(this.sRouteID) &&
           rtdata.sFromID.equals(this.sFromID) &&
           rtdata.sDestID.equals(this.sDestID) &&
           rtdata.iRouteOnOff == this.iRouteOnOff &&
           rtdata.iFromType == this.iFromType &&
           rtdata.iDestType == this.iDestType);
  }

  /**
   * This generates the string for the field that is changed with old and new values.
   */
  @Override
  public String getActionDesc(String isColName, Object ipOld, Object ipNew)
  {
    String s = "Field " + isColName + ":";
    
    try
    {
      String vsOld = "";
      String vsNew = "";

      // Construct Action Description string
      switch((RouteEnum)mpColumnMap.get(isColName))
      {
      case DESTTYPE:
      case FROMTYPE:
      case ROUTEONOFF:
        vsOld = DBTrans.getStringValue(isColName, ((Integer)ipOld).intValue());
        vsNew = DBTrans.getStringValue(isColName, ((Integer)ipNew).intValue());
        break;

      default:
        vsOld = ipOld.toString();
        vsNew = ipNew.toString();
      }
      s = s + " Old [" + vsOld + "] New [" + vsNew + "]";
    }
    catch(NoSuchFieldException e)
    {
      s = s + " #### NoSuchFieldException #### ";
    }
    return s;
  }

/*---------------------------------------------------------------------------
                     Column value get methods go here.
  ---------------------------------------------------------------------------*/
  /**
   * Fetches Route ID
   * @return Route ID as string
   */
  public String getRouteID()
  {
    return(sRouteID);
  }
  /**
   * Fetches From Station
   * @return From Station as string
   */
  public String getFromID()
  {
    return(sFromID);
  }
  /**
   * Fetches To Station
   * @return To Station as string
   */
  public String getDestID()
  {
    return(sDestID);
  }
  /**
   * Fetches From Station Type
   * @return From Station Type as integer
   */
  public int getFromType()
  {
    return(iFromType);
  }
  /**
   * Fetches To Station Type
   * @return To Station Type as integer
   */
  public int getDestType()
  {
    return(iDestType);
  }
  /**
   * Fetches Route OnOff
   * @return Route OnOff as integer
   */
  public int getRouteOnOff()
  {
    return(iRouteOnOff);
  }

/*---------------------------------------------------------------------------
               ******** Column Setting methods go here. ********
  ---------------------------------------------------------------------------*/
  /**
   * Sets Route value.
   */
  public void setRouteID(String isRouteID)
  {
    sRouteID = checkForNull(isRouteID);
    addColumnObject(new ColumnObject(ROUTEID_NAME, isRouteID));
  }
  /**
   * Sets Destination ID value.
   */
  public void setDestID(String isDestID)
  {
    sDestID = checkForNull(isDestID);
    addColumnObject(new ColumnObject(DESTID_NAME, isDestID));
  }
  /**
   * Sets From ID value.
   */
  public void setFromID(String isFromID)
  {
    sFromID = checkForNull(isFromID);
    addColumnObject(new ColumnObject(FROMID_NAME, isFromID));
  }
  /**
   * Sets From Station Type value.
   */
  public void setFromType(int inFromType)
  {
    try
    {
      DBTrans.getStringValue(FROMTYPE_NAME, inFromType);
    }
    catch(NoSuchFieldException e)
    {                                  // Passed value wasn't valid. Default it
      inFromType = DBConstants.STATION;
    }
    iFromType = inFromType;
    addColumnObject(new ColumnObject(FROMTYPE_NAME, Integer.valueOf(inFromType)));
  }
  /**
   * Sets To Station Type value.
   */
  public void setDestType(int inDestType)
  {
    try
    {
      DBTrans.getStringValue(DESTTYPE_NAME, inDestType);
    }
    catch(NoSuchFieldException e)
    {                                  // Passed value wasn't valid. Default it
      inDestType = DBConstants.STATION;
    }
    iDestType = inDestType;
    addColumnObject(new ColumnObject(DESTTYPE_NAME, Integer.valueOf(inDestType)));
  }
  /**
   * Sets Route OnOff value.
   */
  public void setRouteOnOff(int inRouteOnOff)
  {
    try
    {
      DBTrans.getStringValue(ROUTEONOFF_NAME, inRouteOnOff);
    }
    catch(NoSuchFieldException e)
    {                                  // Passed value wasn't valid. Default it
      inRouteOnOff = DBConstants.OFF;
    }
    iRouteOnOff = inRouteOnOff;
    addColumnObject(new ColumnObject(ROUTEONOFF_NAME, Integer.valueOf(inRouteOnOff)));
  }
   
   
  /**
   *  Required set field method.  This method figures out what column was
   *  passed to it and sets the value.  This allows us to have a generic
   *  method for all DB interfaces.
   */
  @Override
  public int setField(String isColName, Object ipColValue)
  {
    TableEnum vpEnum = mpColumnMap.get(isColName);
    if (vpEnum == null)
    {
      return(super.setField(isColName, ipColValue));
    }

    switch((RouteEnum)vpEnum)
    {
      case ROUTEID:
        setRouteID(ipColValue.toString());
        break;
        
      case FROMID:
        setFromID(ipColValue.toString());
        break;
        
      case DESTID:
        setDestID(ipColValue.toString());
        break;

      case FROMTYPE:
        setFromType(((Integer)ipColValue).intValue());
        break;
        
      case DESTTYPE:
        setDestType(((Integer)ipColValue).intValue());
        break;
        
      case ROUTEONOFF:
        setRouteOnOff(((Integer)ipColValue).intValue());
        break;
    }                                  

    return 0;
  }
}
