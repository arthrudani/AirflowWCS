package com.daifukuamerica.wrxj.dbadapter.data;

/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import static com.daifukuamerica.wrxj.dbadapter.data.SynonymEnum.*;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:<BR>
 *   Class to handle synonym Data operations.  This class treats columns, and
 *   Keys as Objects.
 *
 * @author       jan
 * @version      1.0
 * @created      2/16/05
 * @file         SynonymData.java
 *  
 *   sItem              VARCHAR(20)    NOT NULL,
 *   sSynonym           VARCHAR(30)    NULL,
 *   iUPCFlag           INTEGER        DEFAULT 2
 * 
 */
public class SynonymData extends AbstractSKDCData
{
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  /*========================================================================*/
  /*  Column names                                                          */
  /*========================================================================*/
  public static final String ITEM_NAME    = ITEM.getName();
  public static final String SYNONYM_NAME = SYNONYM.getName();
  
  /*========================================================================*/
  /*  Table Data                                                            */
  /*========================================================================*/
  protected String sItemID  = "";
  protected String sSynonym = "";
  
  public SynonymData()
  {
  	clear();
    initColumnMap(mpColumnMap, SynonymEnum.class);
  }
  
  @Override
  public void clear()
  {
  	super.clear();
  	sItemID  = "";
  	sSynonym = "";
  }

  /**
   * This helps in debugging when we want to print the whole structure.
   */
  @Override
  public String toString()
  {
    String s = "sItemID:"  + sItemID +
    	       "sSynonym:" + sSynonym + "\n";

    s += super.toString();
    return(s);
  }

  /**
   * Defines equality between two SynonymData objects.
   *
   * @param  absSYNONYM <code>AbstractSKDCData</code> reference whose runtime type
   *         is expected to be <code>synonymData</code>
   */
  @Override
  public boolean equals(AbstractSKDCData absSYNONYM)
  {
  	SynonymData sydata = (SynonymData)absSYNONYM;
    return (sydata.sSynonym.equals(sSynonym));
  }

/*---------------------------------------------------------------------------
                       Get methods go here.
  ---------------------------------------------------------------------------*/
  public String getItemID()        { return sItemID;  }
  public String getSynonym()       { return sSynonym; }

/*---------------------------------------------------------------------------
               ******** Column Setting methods go here. ********
  ---------------------------------------------------------------------------*/
  /**
   * Sets Item ID value.
   */
  public void setItemID(String isItemID)
  {
    sItemID = checkForNull(isItemID);
    addColumnObject(new ColumnObject(ITEM_NAME, isItemID));
  }

  /**
   * Sets synonym value.
   */
  public void setSynonym(String isSynonym)
  {
    sSynonym = checkForNull(isSynonym);
    addColumnObject(new ColumnObject(SYNONYM_NAME, isSynonym));
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
      return super.setField(isColName, ipColValue);
    }
    
    switch ((SynonymEnum)vpEnum)
    {
      case ITEM:
        setItemID(ipColValue.toString());
        break;

      case SYNONYM:
        setSynonym(ipColValue.toString());
        break;
        
      default:
        return -1;
    }

    return 0;
  }
}
