package com.daifukuamerica.wrxj.dbadapter.data;

/*
                    Daifuku America Corporation
                       International Center
                   5202 Douglas Corrigan Way
                Salt Lake City, Utah  84116-3192
                        (801) 359-9900

   This software is furnished under a license and may be used and copied only 
   in accordance with the terms of such license. This software or any other 
   copies thereof in any form, may not be provided or otherwise made available, 
   to any other person or company without written consent from Daifuku America 
   Corporation.

   Daifuku America Corporation assumes no responsibility for the use or 
   reliability of software which has been modified without approval.
*/

import static com.daifukuamerica.wrxj.dbadapter.data.AsrsMetaDataEnum.*;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:<BR>
 *   Class to handle AsrsMetaData Data operations.  This class treats columns,
 *   and Keys as Objects.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 23-Oct-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  Daifuku America Corporation
 */
public class AsrsMetaDataData extends AbstractSKDCData
{
  public static final String DATAVIEWNAME_NAME  = DATAVIEWNAME.getName();
  public static final String COLUMNNAME_NAME    = COLUMNNAME.getName();
  public static final String FULLNAME_NAME      = FULLNAME.getName();
  public static final String ISTRANSLATION_NAME = ISTRANSLATION.getName();
  public static final String DISPLAYORDER_NAME  = DISPLAYORDER.getName();

  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

/*---------------------------------------------------------------------------
                 Database fields for AsrsMetaData table.
  ---------------------------------------------------------------------------*/
  private String sDataViewName  = "";
  private String sColumnName    = "";
  private String sFullName      = "";
  private String sIsTranslation = "";
  private int    iDisplayOrder  = 0;

  public AsrsMetaDataData()
  {
    super();
    clear();
    initColumnMap(mpColumnMap, AsrsMetaDataEnum.class);
  }

  /**
   * This helps in debugging when we want to print the whole structure.
   */
  @Override
  public String toString()
  {
    String s = "sDataViewName:" + sDataViewName +
               "\nsColumnName:" + sColumnName +
               "\nsFullName:" + sFullName +
               "\nsIsTranslation:" + sIsTranslation +
               "\niDisplayOrder:" + iDisplayOrder + "\n";

    s += super.toString();
    
    return(s);
  }

  /**
   * Defines equality between two AsrsMetaDataData objects.
   * 
   * @param absMD <code>AbstractSKDCData</code> reference whose runtime type
   *            is expected to be <code>AsrsMetaDataData</code>
   */
  @Override
  public boolean equals(AbstractSKDCData absMD)
  {
    if (this == absMD) return(true);
    
    AsrsMetaDataData md = (AsrsMetaDataData)absMD;
    return(sDataViewName.equals(md.getDataViewName())   &&
           sColumnName.equals(md.getColumnName())       &&
           sFullName.equals(md.getFullName())           &&
           sIsTranslation.equals(md.getIsTranslation()) &&
           iDisplayOrder == md.getDisplayOrder());
  }

  /**
   * Resets the data in this class to the default.
   */
  @Override
  public void clear()
  {
    super.clear();

    sDataViewName  = "";
    sColumnName    = "";
    sFullName      = "";
    sIsTranslation = "";
    iDisplayOrder  = 0;
  }

/*---------------------------------------------------------------------------
                     Column value get methods go here.
  ---------------------------------------------------------------------------*/
  /**
   * Fetches Meta Data schema name
   * @return Data view as string
   */
  public String getDataViewName()
  {
    return(sDataViewName);
  }

  /**
   * Fetches actual Column Name of the meta data
   * @return Column Name as string
   */
  public String getColumnName()
  {
    return(sColumnName);
  }

  /**
   * Fetches Full name of the column
   * @return full name as string
   */
  public String getFullName()
  {
    return(sFullName);
  }

  /**
   * Fetches flag to indicate if this meta data field is a translation
   * @return translation flag.
   */
  public String getIsTranslation()
  {
    return(sIsTranslation);
  }

  /**
   * Fetches Display order of a column
   * @return display order as int
   */
  public int getDisplayOrder()
  {
    return(iDisplayOrder);
  }

/*---------------------------------------------------------------------------
               ******** Column Setting methods go here. ********
  ---------------------------------------------------------------------------*/
  /**
   * Sets Data View Name value.
   */
  public void setDataViewName(String isDataViewName)
  {
    sDataViewName = checkForNull(isDataViewName);
    addColumnObject(new ColumnObject(DATAVIEWNAME_NAME, isDataViewName));
  }

  /**
   * Sets Column Name value.
   */
  public void setColumnName(String isColumnName)
  {
    sColumnName = checkForNull(isColumnName);
    addColumnObject(new ColumnObject(COLUMNNAME_NAME, isColumnName));
  }

  /**
   * Sets Full Name value.
   */
  public void setFullName(String isFullName)
  {
    sFullName = checkForNull(isFullName);
    addColumnObject(new ColumnObject(FULLNAME_NAME, isFullName));
  }

  /**
   * Sets Flag to indicate if this meta data is a translation.
   */
  public void setIsTranslation(String isIsTranslation)
  {
    sIsTranslation = checkForNull(isIsTranslation);
    addColumnObject(new ColumnObject(ISTRANSLATION_NAME, isIsTranslation));
  }

  /**
   * Sets Display Order of the current column.
   */
  public void setDisplayOrder(int inDisplayOrder)
  {
    iDisplayOrder = inDisplayOrder;
    addColumnObject(new ColumnObject(DISPLAYORDER_NAME,
      Integer.valueOf(inDisplayOrder)));
  }

  /**
   * Required set field method. This method figures out what column was passed
   * to it and sets the value. This allows us to have a generic method for all
   * DB interfaces.
   * 
   * @param isColName
   * @param ipColValue
   */
  @Override
  public int setField(String isColName, Object ipColValue)
  {
    TableEnum vpEnum = mpColumnMap.get(isColName);
    if (vpEnum == null) 
    {
      return super.setField(isColName, ipColValue);
    }
    
    switch ((AsrsMetaDataEnum)vpEnum)
    {
      case COLUMNNAME:
        setColumnName((String)ipColValue);
        break;
        
      case DATAVIEWNAME:
        setDataViewName((String)ipColValue);
        break;
        
      case DISPLAYORDER:
        setDisplayOrder(((Integer)ipColValue).intValue());
        break;
        
      case FULLNAME:
        setFullName((String)ipColValue);
        break;
        
      case ISTRANSLATION:
        setIsTranslation((String)ipColValue);
        break;
    }
    
    return 0;
  }
}
