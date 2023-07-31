package com.daifukuamerica.wrxj.jdbc;

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

import com.daifukuamerica.wrxj.util.SkdRtException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Description:<BR>
 *  Class for holding name value pairs for a database table.
 *
 *  @author       A.D.
 *  @version      1.0
 *  @since 25-Jan-02
 */
public class ColumnObject implements Cloneable
{
  private String msColumnName  = "";
  private Object msColumnValue = null;

  public ColumnObject()
  {
    this("", "");
  }

  public ColumnObject(String isColumnName, Object isColumnValue)
  {
    super();
    msColumnName  = isColumnName;
    msColumnValue = isColumnValue;
  }

  @Override
  public String toString()
  {
    String vsStr = "  ColumnObject.name = " + msColumnName +
                   "\n  ColumnObject.value = " + msColumnValue + "\n";

    return(vsStr);
  }

 /**
  *  Method clones current object.
  *
  *  @return <code>Object</code> containing copy of current Column object.
  */
  @Override
  public Object clone()
  {
    ColumnObject vpClonedObject;

    try
    {
      vpClonedObject = (ColumnObject)super.clone();
      if (msColumnValue instanceof Date)
        vpClonedObject.msColumnValue = ((Date)msColumnValue).clone();
      else
        vpClonedObject.msColumnValue = msColumnValue;
    }
    catch(CloneNotSupportedException e)
    {
      throw new SkdRtException("Cloning error...", e);
    }

    return(vpClonedObject);
  }

 /**
  *  Method tests for equality of two column objects.  Two objects are considered
  *  equal if their name and value match.
  *
  *  @param ipColumnObj <code>ColumnObject</code> containing column object to
  *         compare.
  *
  *  @return <code>boolean</code> of <code>true</code> if equality.
  *   <code>false</code> otherwise.
  */
  public boolean equals(ColumnObject ipColumnObj)
  {
    if (ipColumnObj == null)
    {
      return(false);
    }
    else if (ipColumnObj == this)
    {
      return(true);
    }
    else if (!ipColumnObj.getColumnName().trim().equalsIgnoreCase(this.msColumnName.trim()))
    {
      return(false);
    }

    Object vpValue = ipColumnObj.getColumnValue();
    if (vpValue == this.msColumnValue)
    {
      return(true);
    }
    else if (vpValue instanceof Integer)
    {
      return(((Integer)vpValue).equals(this.msColumnValue));
    }
    else if (vpValue instanceof Double)
    {
      return(((Double)vpValue).equals(this.msColumnValue));
    }
    else if (vpValue instanceof Date)
    {
      return(((Date)vpValue).equals(this.msColumnValue));
    }

    return(false);
  }

 /**
  *  Method compares only the value of current <code>ColumnObject</code> and
  *  passed in value.
  *
  *  @param vpValue <code>Object</code> containing object to compare.  The
  *         object is assumed to be of type <code>Integer</code>,
  *         <code>Double</code>, <code>Date</code>, <code>String</code>, or
  *         <code>StringBuffer</code>
  *
  *  @return <code>boolean</code> of <code>true</code> if equality,
  *          <code>false></code> otherwise.
  */
  public boolean equalsValue(Object vpValue)
  {
    if (vpValue == null) return(false);
    if (vpValue == this.msColumnValue) return(true);

    if (vpValue instanceof Integer)
    {
      return(((Integer)vpValue).equals(this.msColumnValue));
    }
    else if (vpValue instanceof Double)
    {
      return(((Double)vpValue).equals(this.msColumnValue));
    }
    else if (vpValue instanceof Date)
    {
      return(((Date)vpValue).equals(this.msColumnValue));
    }
    else
    {
      return(vpValue.toString().equals(this.msColumnValue.toString()));
    }
  }

  public void setColumnName(String isColumnName)
  {
    msColumnName = isColumnName;
  }

  public void setColumnValue(Object isColumnValue)
  {
    msColumnValue = isColumnValue;
  }

  public String getColumnName()
  {
    return(msColumnName);
  }

  public Object getColumnValue()
  {
    return(msColumnValue);
  }

 /**
  * Fetches all Column Objects currently in a collection. The
  * reason for this routine is that we can work directly with the
  * ColumnObject array instead of the container.
  *
  * @return ColumnObject
  */
  public static ColumnObject[] toColumnArray(Collection<ColumnObject> ipCols)
  {
    int num_columns = ipCols.size();

    ColumnObject[] cobj = new ColumnObject[num_columns];
                                       // Load up a ColumnObject array.
    int col=0;
    for(ColumnObject vpCO : ipCols)
    {
      cobj[col] = vpCO;
      col++;
    }

    return(cobj);
  }

  /**
   *   Method to modify a stored column from a column array.
   */
  public static ColumnObject[] modify(String columnName, Object new_value,
                                      ColumnObject[] col_array)
  {
    int mod_idx = 0;
    ColumnObject[] new_array = col_array;
                                       // First find the column index of the
                                       // column to modify.  Then build a new
    do                                 // column array skipping this index.
    {
      if (new_array[mod_idx].getColumnName().equals(columnName))
      {
        new_array[mod_idx].setColumnValue(new_value);
        break;
      }
    } while(++mod_idx < new_array.length);

                                       // Didn't find any matching column!
    if (mod_idx == new_array.length)
    {
      return(null);
    }

    return(new_array);
  }

 /**
  * Method to remove a stored column from a column array.
  * @param columnName the name of the ColumnObject to delete from the array.
  * @param col_array the array to delete object from.
  * @return a new ColumnObject array with the appropriate object removed.
  */
  public static ColumnObject[] delete(String columnName,
                                      ColumnObject[] col_array)
  {
    int del_idx = 0;
                                       // First find the column index of the
                                       // column to delete.  Then build a new
                                       // column array skipping this index.
    do
    {
      if (col_array[del_idx].getColumnName().equals(columnName))
      {
        break;
      }
    } while(del_idx++ < col_array.length);

                                       // Didn't find any matching column!
    if (del_idx == col_array.length)
    {
      return(col_array);
    }
                                       // Allocate a new array with length one
                                       // less than the one passed in.
    ColumnObject[] new_array = new ColumnObject[col_array.length-1];

    for(int new_idx = 0, old_idx = 0; new_idx < col_array.length-1;
        new_idx++, old_idx++)
    {
      if (old_idx == del_idx) old_idx++;
      new_array[new_idx] = col_array[old_idx];
    }

    return(new_array);
  }

  /**
   *  Searches a ColumnObject array for a column name and returns its index.
   *  Note: if there are duplicate names in this array then the index of first
   *  occurance is returned.
   *  @param colname the column name for which to get the index.
   *  @param cobj array of ColumnObject's that is being searched.
   *  @return
   */
  public static int getColumnObjectIndex(String colname, ColumnObject[] cobj)
  {
    int c_idx;

    for(c_idx = 0; c_idx < cobj.length; c_idx++)
    {
      if (cobj[c_idx].getColumnName().equalsIgnoreCase(colname))
      {
          break;
      }
    }

    if (c_idx == cobj.length)
    {
      c_idx = -1;                      // Didn't find the entry.
    }

    return(c_idx);
  }

 /**
  * Searches a ColumnObject array for a column name and returns its value.
  * Note: if there are duplicate names in this array then the first occurance
  * is returned.
  * 
  * @param colname the name of the column that is being searched for.
  * @param cobj the array of Column Objects name is searched for.  <b>Note that
  *        this is a sequential search</b>
  * @return the column value corresponding to the name being searched for.
  */
  public static Object getValueByName(String colname, ColumnObject[] cobj)
  {
    int theIndex = ColumnObject.getColumnObjectIndex(colname, cobj);
    if (theIndex == -1) return(null);

    return(cobj[theIndex].getColumnValue());
  }

  /**
   * Retrieves a ColumnObject from a List of ColumnObjects based on a caller
   * specified name.
   * @param isColumnName the name of the column to use in the search.
   * @param ipList the list of ColumnObject's to search.
   * @return the appropriate ColumnObject if found, or else a null.
   */
  public static ColumnObject getMatchingObject(String isColumnName, 
                                               List<ColumnObject> ipList)
  {
    ColumnObject vpColumn = null;
    for(ColumnObject vpObj : ipList)
    {
      if (vpObj.getColumnName().equalsIgnoreCase(isColumnName))
      {
        vpColumn = (ColumnObject)vpObj.clone();
        break;
      }
    }
    return(vpColumn);
  }

  public void clear(ColumnObject[] cobj)
  {
    for(int idx = 0; idx < cobj.length; idx++)
    {
        cobj[idx].setColumnName("");
        cobj[idx].setColumnValue(null);
        cobj[idx] = null;
    }

    System.gc();
  }
}
