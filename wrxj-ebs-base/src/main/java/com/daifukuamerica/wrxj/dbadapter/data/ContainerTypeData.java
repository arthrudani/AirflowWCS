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
  CONSENT OF Daifuku America Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/

import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import static com.daifukuamerica.wrxj.dbadapter.data.ContainerTypeEnum.*;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:<BR>
 * Title: Class to handle Container Data Object. Description: Class to handle
 * Container Data Object
 * 
 * @author REA
 * @version 1.0 04-Jan-02
 * @version 2.0 16-Nov-04
 * @version 3.0 06-Jun-08
 */
public class ContainerTypeData extends AbstractSKDCData
{
  public static final String CONTAINERTYPE_NAME = CONTAINERTYPE.getName();
  public static final String CONTHEIGHT_NAME = CONTHEIGHT.getName();
  public static final String CONTLENGTH_NAME = CONTLENGTH.getName();
  public static final String CONTWIDTH_NAME = CONTWIDTH.getName();
  public static final String MAXWEIGHT_NAME = MAXWEIGHT.getName();
  public static final String WEIGHT_NAME = WEIGHT.getName();

  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();
  
  // -------------------Container Table data -----------------------------
  private String sContainerType;
  private double fWeight;
  private double fMaxWeight;
  private double fContLength;
  private double fContWidth;
  private double fContHeight;

  //-----------------------  default data ---------------------------------
  private String DEFAULT_CONTAINERTYPE = "";
  private int    DEFAULT_WEIGHT = 0;
  private double DEFAULT_MAXWEIGHT = 0.0;
  private double DEFAULT_CONTLENGTH = 0.0;
  private double DEFAULT_CONTWIDTH = 0.0;
  private double DEFAULT_CONTHEIGHT = 0.0;

  @Override
  public void clear()
  {
    super.clear();
    sContainerType  = DEFAULT_CONTAINERTYPE;
    fWeight         = DEFAULT_WEIGHT;
  	fMaxWeight      = DEFAULT_MAXWEIGHT;
  	fContLength     = DEFAULT_CONTLENGTH;
  	fContWidth      = DEFAULT_CONTWIDTH;
  	fContHeight     = DEFAULT_CONTHEIGHT;
  }

  public ContainerTypeData()
  {
    super();
    clear();
    initColumnMap(mpColumnMap, ContainerTypeEnum.class);
  }

  @Override
  public String toString()
  {
    StringBuffer myString = new StringBuffer();
    myString.append(": sContainerType = "  + sContainerType);
    myString.append(": fWeight = "         + fWeight);
  	myString.append(": fMaxWeight = "      + fMaxWeight);
  	myString.append(": fContLength = "     + fContLength);
  	myString.append(": fContWidth = "      + fContWidth);
  	myString.append(": fContHeight = "     + fContHeight);

    return(myString.toString() + super.toString());
  }

  @Override
  public boolean equals(AbstractSKDCData absCT)
  {
    ContainerTypeData ct = (ContainerTypeData)absCT;
    return(ct.getContainer().equals(getContainer()));
  }


/*---------------------------------------------------------------------------
   Getters
 ---------------------------------------------------------------------------*/
  public String getContainer()     { return sContainerType; }
  public double getWeight()        { return fWeight; }          // TODO: Implement!
  public double getMaxWeight()     { return fMaxWeight; }       // TODO: Implement!
  public double getContLength()    { return fContLength; }
  public double getContWidth()     { return fContWidth; }
  public double getContHeight()    { return fContHeight; }

  
/*---------------------------------------------------------------------------
   Setters
 ---------------------------------------------------------------------------*/
  public void setContainer(String isContainer)
  {
    sContainerType = checkForNull(isContainer);
    addColumnObject(new ColumnObject(CONTAINERTYPE_NAME, sContainerType));
  }

  public void setWeight(double idWeight)
  {
    fWeight = idWeight;
    addColumnObject(new ColumnObject(WEIGHT_NAME, Double.valueOf(idWeight)));
  }

  public void setMaxWeight(double idMaxWeight)
  {
	fMaxWeight = idMaxWeight;
	addColumnObject(new ColumnObject(MAXWEIGHT_NAME, Double.valueOf(idMaxWeight)));
  }
  
  public void setContLength(double idContLength)
  {
	fContLength = idContLength;
	addColumnObject(new ColumnObject(CONTLENGTH_NAME, Double.valueOf(idContLength)));
  }
  
  public void setContWidth(double idContWidth)
  {
	fContWidth = idContWidth;
	addColumnObject(new ColumnObject(CONTWIDTH_NAME, Double.valueOf(idContWidth)));
  }
  
  public void setContHeight(double idContHeight)
  {
	fContHeight = idContHeight;
	addColumnObject(new ColumnObject(CONTHEIGHT_NAME, Double.valueOf(idContHeight)));
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
    
    switch ((ContainerTypeEnum)vpEnum)
    {
      case CONTAINERTYPE:
        setContainer((String)ipColValue);
        break;

      case CONTHEIGHT:
        setContHeight(((Double)ipColValue).doubleValue());
        break;
        
      case CONTLENGTH:
        setContLength(((Double)ipColValue).doubleValue());
        break;
        
      case CONTWIDTH:
        setContWidth(((Double)ipColValue).doubleValue());
        break;
        
      case MAXWEIGHT:
        setMaxWeight(((Double)ipColValue).doubleValue());
        break;
        
      case WEIGHT:
        setWeight(((Double)ipColValue).doubleValue());
        break;
    }
    return 0;
  }
}
