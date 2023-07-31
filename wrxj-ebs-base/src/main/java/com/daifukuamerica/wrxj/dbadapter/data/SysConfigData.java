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

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;

import static com.daifukuamerica.wrxj.dbadapter.data.SysConfigEnum.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:<BR>
 *   Class to handle SysConfig Data operations.  This class treats columns, and
 *   Keys as Objects.
 *
 * @author       sbw
 * @version      1.0    02/15/05
 */
public class SysConfigData extends AbstractSKDCData
{
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();
  
/*============================================================================
 *                            COLUMN NAMES
 *============================================================================*/
  public static final String GROUP_NAME          = GROUP.getName();
  public static final String PARAMETERNAME_NAME  = PARAMETERNAME.getName();
  public static final String PARAMETERVALUE_NAME = PARAMETERVALUE.getName();
  public static final String DESCRIPTION_NAME = DESCRIPTION.getName();
  public static final String ENABLED_NAME = ENABLED.getName();
  public static final String SCREENCHANGEALLOWED_NAME = SCREENCHANGEALLOWED.getName();
  public static final String SCREENTYPE_NAME = SCREENTYPE.getName();

/*---------------------------------------------------------------------------
                 Database fields for SysConfig table.
  ---------------------------------------------------------------------------*/
  private String sGroup          = "";
  private String sParameterName  = "";
  private String sParameterValue = "";
  private String sDescription    = "";
  private int    mnEnabled       = 1;
  private int    mnScreenChangeAllowed = 1;
  private String msScreenType    = "";

  public SysConfigData()
  {
    super();
    initColumnMap(mpColumnMap, SysConfigEnum.class);
  }

  /**
   * This helps in debugging when we want to print the whole structure.
   */
  @Override
  public String toString()
  {
    String s = 
      GROUP_NAME               + ":" + sGroup          + SKDCConstants.EOL_CHAR +
      PARAMETERNAME_NAME       + ":" + sParameterName  + SKDCConstants.EOL_CHAR +
      PARAMETERVALUE_NAME      + ":" + sParameterValue + SKDCConstants.EOL_CHAR +
      DESCRIPTION_NAME         + ":" + sDescription    + SKDCConstants.EOL_CHAR +
      ENABLED_NAME             + ":" + mnEnabled       + SKDCConstants.EOL_CHAR +
      SCREENCHANGEALLOWED_NAME + ":" + mnScreenChangeAllowed + SKDCConstants.EOL_CHAR +
      SCREENTYPE_NAME          + ":" + msScreenType;
    s += super.toString();

    return(s);
  }

  /**
   * Defines equality between two SysConfigData objects.
   * 
   * @param absCI <code>AbstractSKDCData</code> reference whose runtime type
   *            is expected to be <code>SysConfigData</code>
   */
  @Override
  public boolean equals(AbstractSKDCData absSC)
  {
    SysConfigData sc = (SysConfigData)absSC;
    return(getParameterName().equals(sc.getParameterName()));
  }

  /**
   * Resets the data in this class to the default.
   */
  @Override
  public void clear()
  {
    super.clear();                     // Pull in default behaviour.

    sGroup = "";
    sParameterName = "";
    sParameterValue = "";
    sDescription = "";
    mnEnabled = 1;
    mnScreenChangeAllowed = 1;
  }

/*---------------------------------------------------------------------------
                     Column value get methods go here.
  ---------------------------------------------------------------------------*/
 
  /**
   * Fetches SysConfig Group
   * @return Group as string
   */
  public String getGroup()
  {
    return(sGroup);
  }


  /**
   * Fetches ParameterName 
   * @return ParameterName as string
   */
  public String getParameterName()
  {
    return(sParameterName);
  }

  /**
   * Fetches SysConfig ParameterValue
   * @return ParameterValue.
   */
  public String getParameterValue()
  {
    return(sParameterValue);
  }

  /**
   * Fetches SysConfig Description
   * @return Description.
   */
  public String getDescription()
  {
    return(sDescription);
  }

  /**
   * Fetches SysConfig Enabled
   * @return Enabled.
   */
  public int getEnabled()
  {
    return(mnEnabled);
  }

  /**
   * Fetches SysConfig ScreenChangeAllowed
   * @return ScreenChangeAllowed.
   */
  public int getScreenChangeAllowed()
  {
    return(mnScreenChangeAllowed);
  }

  /**
   * Fetches SysConfig ScreenType
   * @return ScreenType.
   */
  public String getScreenType()
  {
    return(msScreenType);
  }

/*---------------------------------------------------------------------------
               ******** Column Setting methods go here. ********
  ---------------------------------------------------------------------------*/
 
  /**
   * Sets SysConfig Group value.
   */
  public void setGroup(String isGroup)
  {
    sGroup = checkForNull(isGroup);
    addColumnObject(new ColumnObject(GROUP.getName(), sGroup));
  }

  /**
   * Sets SysConfig ParameterName value.
   */
  public void setParameterName(String isParameterName)
  {
    sParameterName = checkForNull(isParameterName);
    addColumnObject(new ColumnObject(PARAMETERNAME.getName(), sParameterName));
  }

  /**
   * Sets SysConfig ParameterValue.
   */
  public void setParameterValue(String isParameterValue)
  {
    sParameterValue = checkForNull(isParameterValue);
    addColumnObject(new ColumnObject(PARAMETERVALUE.getName(), sParameterValue));
  }

  /**
   * Sets SysConfig Description.
   */
  public void setDescription(String isDescription)
  {
    sDescription = checkForNull(isDescription);
    addColumnObject(new ColumnObject(DESCRIPTION.getName(), sDescription));
  }

  /**
   * Sets SysConfig Enabled.
   */
  public void setEnabled(int inEnabled)
  {
    mnEnabled = inEnabled;
    addColumnObject(new ColumnObject(ENABLED.getName(), mnEnabled));
  }

  /**
   * Sets SysConfig ScreenChangeAllowed.
   */
  public void setScreenChangeAllowed(int inScreenChangeAllowed)
  {
    mnScreenChangeAllowed = inScreenChangeAllowed;
    addColumnObject(new ColumnObject(SCREENCHANGEALLOWED.getName(), mnScreenChangeAllowed));
  }

  /**
   * Sets SysConfig ScreenType.
   */
  public void setScreenType(String isScreenType)
  {
    msScreenType = checkForNull(isScreenType);
    addColumnObject(new ColumnObject(SCREENTYPE.getName(), msScreenType));
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

    switch ((SysConfigEnum)vpEnum)
    {
      case GROUP:
        setGroup((String)ipColValue);
        break;
        
      case PARAMETERNAME:
        setParameterName((String)ipColValue);
        break;
        
      case PARAMETERVALUE:
        setParameterValue((String)ipColValue);
        break;
        
      case DESCRIPTION:
        setDescription((String)ipColValue);
        break;
        
      case ENABLED:
        setEnabled((Integer)ipColValue);
        break;
        
      case SCREENCHANGEALLOWED:
        setScreenChangeAllowed((Integer)ipColValue);
        break;
        
      case SCREENTYPE:
        setScreenType((String)ipColValue);
        break;
    }

    return(0);
  }
}
