package com.daifukuamerica.wrxj.dbadapter.data;

/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright (c) 2004-2009 Daifuku America Corporation  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/

import static com.daifukuamerica.wrxj.dbadapter.data.ReasonCodeEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Title:        Class to handle ReasonCodeData Object
 * Description:  Handles all data for Reason Code
 *               This class treats columns and keys as Objects
 * Copyright:    Copyright (c) 2004
 * Company:      Daifuku America Corp.
 *
 * @author       jan
 * @version      1.0
 *               Created 11-Jan-05
 * @file		 ReasonCodeData.java
 */

public class ReasonCodeData extends AbstractSKDCData
{
  public static String REASONCODE_NAME     = REASONCODE.getName();
  public static String DESCRIPTION_NAME    = DESCRIPTION.getName();
  public static String REASONCATEGORY_NAME = REASONCATEGORY.getName();

  // ----------- Reason Code Table data --------------
  private String sReasonCode  = "";
  private String sDescription = "";
  private int iReasonCategory = DBConstants.REASONHOLD;
  
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  /**
   * Constructor
   */
  public ReasonCodeData()
  {
    super();
    initColumnMap(mpColumnMap, ReasonCodeEnum.class);
  }

  /**
   * @see com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData#clear()
   */
  @Override
  public void clear()
  {
    super.clear();
    
    sReasonCode = "";
    sDescription = "";
    iReasonCategory = DBConstants.REASONHOLD;
  }

  /**
   * This helps in debugging when we want to print the whole structure.
   */
  @Override
  public String toString()
  {
    String s = "sReasonCode:" + sReasonCode +
               "\nsReasonCodeDescription:" + sDescription + "\n";
    try
    {
      s = s + "iReasonCategory:"
          + DBTrans.getStringValue(REASONCATEGORY_NAME, iReasonCategory);
    }
    catch(NoSuchFieldException e)
    {
       s = s + "0\n";
    }
    s += super.toString();

    return(s);
  }
  
  /**
   * Defines equality between two ReasonCodeData objects.
   * 
   * @param ipReasonCodeData <code>AbstractSKDCData</code> reference whose
   *          runtime type is expected to be <code>ReasonCodeData</code>
   */
  @Override
  public boolean equals(AbstractSKDCData ipReasonCodeData)
  {
    ReasonCodeData rcdata = (ReasonCodeData)ipReasonCodeData;
    return (rcdata.sReasonCode.equals(sReasonCode));
  }

  /*========================================================================*/
  /*  Getters                                                               */
  /*========================================================================*/
  public String getReasonCode()             { return sReasonCode;     }
  public String getReasonCodeDescription()  { return sDescription;    }
  public int    getReasonCategory()         { return iReasonCategory; }
  
  /*========================================================================*/
  /*  Setters                                                               */
  /*========================================================================*/
  /**
   * Sets ReasonCode
   */
  public void setReasonCode(String isReasonCode)
  {
    sReasonCode = checkForNull(isReasonCode);
    addColumnObject(new ColumnObject(REASONCODE_NAME, isReasonCode));
  }

  /**
   * Sets ReasonCode Description
   */
  public void setDescription(String isDescription)
  {
    if (isDescription == null || isDescription.trim().length() < 1)
    {
      sDescription = " ";
    }
    else
    {
      sDescription = isDescription;
    }
    addColumnObject(new ColumnObject(DESCRIPTION_NAME, sDescription));
  }

  /**
   * Sets Reason Category
   */
  public void setReasonCategory(int inReasonCategory)
  {
    try
    {
      DBTrans.getStringValue(REASONCATEGORY_NAME, inReasonCategory);
    }
    catch(NoSuchFieldException e)
    {
      inReasonCategory = DBConstants.REASONHOLD;
    }
    iReasonCategory = inReasonCategory;
    addColumnObject(new ColumnObject(REASONCATEGORY_NAME, iReasonCategory));
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

    switch ((ReasonCodeEnum)vpEnum)
    {
      case REASONCODE:
        setReasonCode(ipColValue.toString());
        break;
      case DESCRIPTION:
        setDescription(ipColValue.toString());
        break;
      case REASONCATEGORY:
        setReasonCategory(((Integer)ipColValue).intValue());
        break;
    }

    return 0;
  }
}
