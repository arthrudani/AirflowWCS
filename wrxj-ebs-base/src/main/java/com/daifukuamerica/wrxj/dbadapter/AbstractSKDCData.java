package com.daifukuamerica.wrxj.dbadapter;

import static com.daifukuamerica.wrxj.dbadapter.AbstractSKDCDataEnum.*;

import com.daifukuamerica.wrxj.jdbc.BetweenKeyObject;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.InKeyObject;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SkdRtException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:<BR>
 * Abstract class containing partial implementation of some methods, and
 * abstract methods for implementation by the user. This class is meant to be
 * used by the database interface data classes like LocationData, WarehouseData
 * etc.
 *
 * @author A.D.
 * @version 1.0 <BR>
 *          Created: 15-May-02<BR>
 *          Copyright (c) 2008-2008 Daifuku America Corporation<BR>
 */
public abstract class AbstractSKDCData implements Cloneable
{
  protected long miID = 0;				// Identity Column for both Oracle and SQLServer
  private String msAddMethod = "";
  private String msUpdMethod = "";
  private Date mpModifyTime = null;
  private static final Map<String, TableEnum> mpAbstractColumnMap = new ConcurrentHashMap<String, TableEnum>();
                                       // Map for SQL columns
  private Map<String, ColumnObject> mpCols = new HashMap<String, ColumnObject>();
                                       // Map for SQL key columns
  private List<KeyObject> mpKeyList = new ArrayList<KeyObject>();
  protected SimpleDateFormat sdf = new SimpleDateFormat(SKDCConstants.DateFormatString);
  protected List<String> mpOrderBy = new ArrayList<String>();

  public AbstractSKDCData()
  {
    initColumnMap(mpAbstractColumnMap, AbstractSKDCDataEnum.class);
  }

  /**
   *  To String method for outputting the ColumnObject(s)
   *  and KeyObject(s) this class knows about.
   *
   * @return internal data as a <code>String</code>
   */
  @Override
  public String toString()
  {
    String vsString = "";
                                       // Make sure to use the ColumnObject's built-
                                       // in toString.
    for(Map.Entry<String, ColumnObject> vpMapEntry : mpCols.entrySet())
      vsString += vpMapEntry.getValue().toString();

    if (!mpKeyList.isEmpty()) vsString += "\n";

                                       // Make sure to use the KeyObject's built-
                                       // in toString.
    for(KeyObject vpKey : mpKeyList)
      vsString += vpKey.toString();

    return(vsString);
  }

  /**
   *  Method to make a deep copy of this object.
   *
   *  @return copy of <code>AbstractSKDCData</code>.
   */
  @Override
  public AbstractSKDCData clone()
  {
    AbstractSKDCData clonedObject;

    try
    {
      clonedObject = (AbstractSKDCData)super.clone();

      clonedObject.sdf = (SimpleDateFormat)sdf.clone();
      clonedObject.mpCols = new HashMap<String, ColumnObject>();
      clonedObject.mpKeyList = new ArrayList<KeyObject>();

      Iterator<ColumnObject> colIter = mpCols.values().iterator();
      while(colIter.hasNext())
      {
        ColumnObject vpCO = (ColumnObject)(colIter.next()).clone();
        clonedObject.mpCols.put(vpCO.getColumnName(), vpCO);
      }

      for(Iterator<KeyObject> keyIter = mpKeyList.iterator(); keyIter.hasNext();)
      {
        KeyObject vpKO = keyIter.next();
        clonedObject.mpKeyList.add((KeyObject)vpKO.clone());
      }
    }
    catch (CloneNotSupportedException e)
    {
      throw new InternalError(e.toString());
    }
    return(clonedObject);
  }

  /**
   * Must also be overridden for consistency
   */
  @Override
  public int hashCode()
  {
    return toString().hashCode();
  }

  /**
   *  Method to check for null objects being passed as a string.
   *
   * @param str <code>String</code> to checked for <code>null</code> object.
   * @return empty string if passed argument is <code>null</code>, else returns
   *         passed in argument as is.
   */
  public String checkForNull(String str)
  {
    return str == null ? "" : str.trim();
  }

  /**
   *  Method to check for null objects being passed as a string without a trim.
   *
   * @param str <code>String</code> to checked for <code>null</code> object.
   * @return empty string if passed argument is <code>null</code>, else returns
   *         passed in argument as is.
   */
  public String checkForNullNoTrim(String str)
  {
    return str == null ? "" : str;
  }

  /**
   *  Method to create an array of column objects from the data that is built whenever
   *  someone does a setXYZ method on one of the data classes.
   *
   *  @return Array of the column objects.
   */
  public ColumnObject[] getColumnArray()
  {
    return(ColumnObject.toColumnArray(mpCols.values()));
  }

 /**
  *  Return <code>ColumnObject</code> that corresponds to colName.
  *
  * @return <code>ColumnObject</code> that corresponds to colName.
  */
  public ColumnObject getColumnObject(String colName)
  {
    return(mpCols.get(colName));
  }

 /**
  * Fetches all known Key objects in the List container.
  *
  * @return <code>KeyObject</code> containing List of Key objects.
  */
  public KeyObject[] getKeyArray()
  {
    return(KeyObject.toKeyArray(mpKeyList));
  }

 /**
  *  Return the first occurrence of a KeyObject with the associated key name.
  *
  *  @param isKeyName <code>String</code> containing the key name.
  *  @return <code>null</code> object if not found.
  */
  public KeyObject getKeyObject(String isKeyName)
  {
    KeyObject vpRtnObj = null;
/*===========================================================================
  A linear search is okay here since there won't be that many key specifications
  in a simple SQL construct.  The amount of time we saved in the previous version
  using a indexed hash lookup is negligible!
 ===========================================================================*/
    for(KeyObject vpKey : mpKeyList)
    {
      if (vpKey.getColumnName().equalsIgnoreCase(isKeyName))
      {
        vpRtnObj = vpKey;
        break;
      }
    }
    return(vpRtnObj);
  }

  /**
   *  Gets the number of columns currently specified.  This is useful
   *  for determining how many columns are specified for change.
   *
   * @return <code>int</code> containing the number of keys that were specified.
   */
  public int getColumnCount()
  {
    return(mpCols.size());
  }

  /**
   *  Gets the number of key columns currently specified.  This is useful
   *  when building composite keys.
   *
   * @return <code>int</code> containing the number of keys that were specified.
   */
  public int getKeyCount()
  {
    return(mpKeyList.size());
  }

 /**
  * Method to set column values for SQL keys.  This method maintains a container
  * in the background (a linked-list) that will be used to build the SQL key
  * clauses for an SQL statement.
  *
  * @param  kobj <code>KeyObject</code> containing key information.
  * @see com.daifukuamerica.wrxj.common.jdbc.KeyObject
  */
  public void addKeyObject(KeyObject kobj)
  {
    if (kobj.getColumnValue() == null) kobj.setColumnValue("");
    mpKeyList.add(kobj);
  }

 /**
  *  Adds a column object to this class.
  *  @param cobj <code>ColumnObject</code> containing column to store.
  */
  public void addColumnObject(ColumnObject cobj)
  {
    mpCols.put(cobj.getColumnName(), cobj);
  }

 /**
  *  Modifies a column object of this class.
  *  @param columnName <code>String</code> string containing column name
  *         of the column to modify.
  *  @param newValue <code>String</code> String containing new column value.
  */
  public void modifyColumnObject(String columnName, String newValue)
  {
    ColumnObject cobjTemp = mpCols.get(columnName);
    if (cobjTemp == null)
      return;
    cobjTemp.setColumnValue(newValue);
  }

  /**
   *  Deletes a column object from this class.
   *  @param columnName <code>String</code> string containing column name
   *         of the column to delete.
   */
  public void deleteColumnObject(String columnName)
  {
    mpCols.remove(columnName);
  }

  /**
   * Deletes a key object from this class.
   * @param keyName <code>String</code> string containing column name
   *        of the key to delete.
   * @deprecated This method should not be used because it does not work!
   * This method should be replaced by the alternate method
   * {@link #deleteKeyObject(com.daifukuamerica.wrxj.jdbc.KeyObject) deleteKeyObject(KeyObject)}<br>
   */
  @Deprecated
  public void deleteKeyObject(String keyName)
  {
    mpKeyList.remove(keyName);
  }

  /**
   *  Deletes a key object from this class.
   *  @param ipKeyElement <code>KeyObject</code> reference containing Key element
   *         to remove.  Typical usage is to delete a particular Key element and
   *         perhaps retry a search with a new Key element
   */
  public void deleteKeyObject(KeyObject ipKeyElement)
  {
    mpKeyList.remove(ipKeyElement);
  }

  /**
   *  Data format changer based on database Data.  Converts Database row data
   *  to one of the AbstractSKDCData data. This is normally used when data is
   *  retrieved from the database in a List.
   *
   * @param tmap <code>Map</code> containing columns of a particular row of
   *        data.
   */
  public void dataToSKDCData(Map tmap)
  {
    this.clear();
    Iterator<String> iter = tmap.keySet().iterator();
    while(iter.hasNext())
    {
      String sKey = iter.next();
      Object theValue = tmap.get(sKey);
      if (setField(sKey, theValue) == -1)
      {
        String message = "Warning - UNKNOWN FIELD - setField (" + sKey + ")";
        System.out.println(message);
        Exception e = new SkdRtException(message);
        e.printStackTrace();
      }
    }

    return;
  }

  /**
   *  Data format changer based on ColumnObject Data.  Converts ColumnObject
   *  data to one of the AbstractSKDCData data class formats.
   *
   * @param cobj <code>ColumnObject</code> containing columns of a particular
   *        row of data.
   */
  public void dataToSKDCData(ColumnObject[] cobj)
  {
    int idx = 0;

    this.clear();
    while(idx < cobj.length)
    {
      if (setField(cobj[idx].getColumnName(), cobj[idx].getColumnValue()) == -1)
      {
        System.out.println("ERROR - UNKNOWN FIELD - setField (" +
                           cobj[idx].getColumnName() + ")");
        new Throwable().printStackTrace(System.out);
      }
      idx++;
    }

    return;
  }

  /**
   *  Method for storing Key objects.  This method provides a one method
   *  front-end for the different things one can do with the KeyObject class.
   *
   * @param columnName <code>String</code> containing
   * @param columnValue <code>Object</code> containing value of a database column
   *        to be used in a keyed database search.  This value can be a String,
   *        Double, Integer or Date object.
   * @param comparison <code>int</code> containing comparison operator.
   * @param conjunction <code>int</code> containing conjunctive value.
   *
   * @see com.daifukuamerica.wrxj.common.jdbc.KeyObject
   */
  public void setKey(String columnName, Object columnValue, int comparison,
                     int conjunction)
  {
    KeyObject key = new KeyObject(columnName, columnValue);
    key.setComparison(comparison);
    if (conjunction != 0 && getKeyCount() > 0)
    {
      key.setConjunction(conjunction);
    }
    addKeyObject(key);
  }

 /**
  *  Convenience method for defaulting comparison, and conjunction.
  *
  * @param columnName <code>String</code> containing
  * @param columnValue <code>Object</code> containing value of a database column
  *        to be used in a keyed database search.  This value can be a String,
  *        Double, Integer or Date object.
  */
  public void setKey(String columnName, Object columnValue)
  {
    setKey(columnName, columnValue, KeyObject.EQUALITY, KeyObject.AND);
  }

 /**
  * Convenience method for defaulting conjunction.
  *
  * @param columnName <code>String</code> containing
  * @param columnValue <code>Object</code> containing value of a database column
  *        to be used in a keyed database search.  This value can be a String,
  *        Double, Integer or Date object.
   * @param comparison <code>int</code> containing comparison operator.
   */
  public void setKey(String columnName, Object columnValue, int comparison)
  {
    setKey(columnName, columnValue, comparison, KeyObject.AND);
  }

  /**
   * Convenience method for regular setColumnNameCompareKey method.
   * @param isColumnName1
   * @param isColumnName2
   * @param inComparison
   */
  public void setColumnNameCompareKey(String isColumnName1, String isColumnName2,
                                      int inComparison)
  {
    setColumnNameCompareKey(isColumnName1, isColumnName2, inComparison,
                            KeyObject.AND);
  }

  /**
   * Method to do comparison between two distinctly named columns. Example:
   * if there is a comparison such as fCurrentQuantity > fAllocatedQuantity
   * in the <code>WHERE</code> clause.
   *
   * @param isColumnName1 the first column name.
   * @param isColumnName2 the second column name.
   * @param inComparison Comparison operator.
   * @param inConjunction Conjunction value
   */
  public void setColumnNameCompareKey(String isColumnName1, String isColumnName2,
                                      int inComparison, int inConjunction)
  {
    KeyObject vpKey = new KeyObject(isColumnName1, isColumnName2);
    vpKey.setColumnNameComparison(inComparison);
    vpKey.setConjunction(inConjunction);
    addKeyObject(vpKey);
  }

 /**
  * Convenience method for defaulting conjunction to AND and allowing for wild card
  * prefix and suffix on search criteria.
  * @param isColumnName <code>String</code> containing
  * @param ipColumnValue <code>Object</code> containing value of a database column
  *        to be used in a keyed database search.  This value can be a String,
  *        Double, Integer or Date object.
  * @param izWildcardPrefixSuffix flag specifying if a prefix and a suffix wild
  *        card should be added.
  */
  public void setWildcardKey(String isColumnName, Object ipColumnValue,
                             boolean izWildcardPrefixSuffix)
  {
    KeyObject key = new KeyObject(isColumnName, ipColumnValue);
    key.setComparison(KeyObject.LIKE);
    key.setConjunction(KeyObject.AND);
    if (izWildcardPrefixSuffix) key.setPrefixSuffixWildcard();
    addKeyObject(key);
  }

  /**
   *  Method for storing Key objects.  This method provides a one method
   *  front-end for the different things one can do with the BetweenKeyObject class.
   *
   * @param columnName <code>String</code> containing
   * @param columnStartValue <code>Object</code> containing value of a database column
   *        to be used in a keyed database search.  This value can be a String,
   *        Double, Integer or Date object.
   * @param columnEndValue <code>Object</code> containing value of a database column
   *        to be used in a keyed database search.  This value can be a String,
   *        Double, Integer or Date object.
   *
   * @see com.daifukuamerica.wrxj.common.jdbc.KeyObject
   */
  public void setBetweenKey(String columnName, Object columnStartValue,
                            Object columnEndValue, int conjunction)
         throws IllegalArgumentException
  {
    KeyObject key = new BetweenKeyObject(columnName, columnStartValue, columnEndValue);
    if (conjunction != 0 && getKeyCount() > 0)
    {
      key.setConjunction(conjunction);
    }
    addKeyObject(key);
  }

  /**
   * Convenience method to default conjunction to " AND "
   * @param columnName
   * @param columnStartValue
   * @param columnEndValue
   * @throws IllegalArgumentException
   */
  public void setBetweenKey(String columnName, Object columnStartValue,
                            Object columnEndValue) throws IllegalArgumentException
  {
    setBetweenKey(columnName, columnStartValue, columnEndValue, KeyObject.AND);
  }

  /**
   * Method for storing Key objects.  This method provides a one method
   * front-end for the different things one can do with the BetweenKeyObject class.
   *
   * @param columnName <code>String</code> containing
   * @param conjunction <code>int</code> containing conjunctive value.
   * @param columnValues <code>VarArray</code> of <code>Objects</code> containing
   *        values of a database column
   *        to be used in a keyed database search.  These values can be Strings,
   *        Doubles, Integers or Date objects but must be of the same type.
   * @throws IllegalArgumentException if the var. args are not of the same type.
   * @see com.daifukuamerica.wrxj.jdbc.InKeyObject InKeyObject
   */
  public void setInKey(String columnName, int conjunction, Object... columnValues )
         throws IllegalArgumentException
  {
    KeyObject key = new InKeyObject(columnName, columnValues);
    if (conjunction != 0 && getKeyCount() > 0)
    {
      key.setConjunction(conjunction);
    }
    addKeyObject(key);
  }

 /**
  * Method to add all keys specified by caller to this data object internal key
  * list.
  * @param iapKeys array of keys filled in by caller.
  */
  public void setKeys(KeyObject[] iapKeys)
  {
    for(int vnIdx = 0; vnIdx < iapKeys.length; vnIdx++)
    {
      addKeyObject(iapKeys[vnIdx]);
    }
  }

 /**
  *  Method to clear out internal List.
  */
  public void clear()
  {
	miID = 0;
    msAddMethod = "";
    msUpdMethod = "";
    mpModifyTime = null;
    mpCols.clear();
    mpKeyList.clear();
    clearOrderByColumns();
  }

 /**
  *  Clear out the Keys and Columns only.  The clear() method above will be
  *  called by the sub-class clear().  This method gives us the ability to be
  *  more selective on what is cleared out.
  */
  public void clearKeysColumns()
  {
    mpCols.clear();
    mpKeyList.clear();
  }

  /**
   * Method to clear the keys.  This method gives us the ability to be
   * more selective on what is cleared out.
   */
  public void clearKeys()
  {
  	mpKeyList.clear();
  }


  /**
   * Clear the Order By columns
   */
  public void clearOrderByColumns()
  {
    mpOrderBy.clear();
  }

  /**
   * Set the Order By columns (Clear current and add new)
   *
   * @param iasOrderBy - Column names to order by (all in ASCending order)
   */
  public void setOrderByColumns(String... iasOrderBy)
  {
    clearOrderByColumns();
    for (String s : iasOrderBy)
    {
      addOrderByColumn(s, false);
    }
  }

  /**
   * Add a single order-by column (ASCending)
   *
   * @param isColumn - Column names to order by (ASCending)
   */
  public void addOrderByColumn(String isColumn)
  {
    addOrderByColumn(isColumn, false);
  }

  /**
   * Add a single order-by column
   *
   * @param isColumn - Column names to order by
   * @param izDescending - true for DESCending, false for default (ASCending)
   */
  public void addOrderByColumn(String isColumn, boolean izDescending)
  {
    mpOrderBy.add(isColumn + (izDescending ? " DESC" : "" ));
  }

  /**
   * Add a single order-by column
   *
   * @param isColumn - Column names to order by
   * @param izDescending - true for DESCending, false for default (ASCending)
   * @param izNulls - true Null fields first, false null fields last
   */
  public void addOrderByColumn(String isColumn, boolean izDescending, boolean izNulls)
  {
    mpOrderBy.add(isColumn + (izDescending ? " DESC" : "" )+( izNulls ? " NULLS First" : ""));
  }

  /**
   * Get the Order By columns
   * @return
   */
  public String[] getOrderByColumns()
  {
    return mpOrderBy.toArray(new String[] {});
  }

  /**
   * Method initializes the Map of column names used for this data class.
   *
   * @param ipMap The Map that will be initialized with a String key
   *            representing the column name, and an enum for the value.
   * @param ipEnumClass class type of the Enum. <b>Note:</b> this Enum class is
   *            required to implement the TableEnum interface.
   * @see com.daifukuamerica.wrxj.dbadapter.TableEnum
   */
  public <Type extends Enum<Type>> void initColumnMap(
      Map<String, TableEnum> ipMap, Class<Type> ipEnumClass)
  {
    if (ipMap.isEmpty())
    {
      EnumSet<Type> vpEnumSet = EnumSet.allOf(ipEnumClass);
      for(Type vpEnum : vpEnumSet)
      {
        TableEnum vpTableEnum = (TableEnum)vpEnum;
        ipMap.put(vpTableEnum.getName(), vpTableEnum);
      }
    }
  }

/*==========================================================================
        All required implementation methods go here (abstract methods).
  ==========================================================================*/
  public abstract boolean equals(AbstractSKDCData eskdata);

  /**
   * Method to be overridden by each Data class to return a String containing
   * the difference between old and new values.
   *
   * @param ColName Name of the Column
   * @param oldValue the Old value
   * @param newValue the new value
   * @return String containing the difference between old and new values.
   */
  public String getActionDesc(String ColName, Object oldValue, Object newValue)
  {
    String s = "";
    return s;
  }

  /**
   * Required set field method. This method figures out what column was passed
   * to it and sets the value. This allows us to have a generic method for all
   * DB interfaces.
   *
   * @param isColName the name of the column.
   * @param ipColValue the value of the column.
   * @return -1 if field look up fails, 0 otherwise.
   */
  public int setField(String columnName, Object columnValue)
  {
    if (mpAbstractColumnMap.get(columnName) == null) return(-1);
    return(0);
  }


  /*========================================================================*/
  /* Debug log helpers                                                      */
  /*========================================================================*/
  /**
   * Get the Id
   *
   * @return
   */
  public long getID()
  {
    return miID;
  }

  /**
   * Get the Add Method
   *
   * @return
   */
  public String getAddMethod()
  {
    return msAddMethod;
  }

  /**
   * Get the Update Method
   *
   * @return
   */
  public String getUpdateMethod()
  {
    return msUpdMethod;
  }

  /**
   * Get the Modify Time
   *
   * @return
   */
  public Date getModifyTime()
  {
    return mpModifyTime;
  }

  /**
   * Set the Id
   *
   * @param idcolValue
   */
  public void setID(long idcolValue)
  {
    miID = idcolValue;
    addColumnObject(new ColumnObject(ID.getName(), miID));
  }
  
  /**
   * Set the Add Method
   *
   * @param iscolValue
   */
  public void setAddMethod(String iscolValue)
  {
    msAddMethod = checkForNull(iscolValue);
    addColumnObject(new ColumnObject(ADDMETHOD.getName(), msAddMethod));
  }

  /**
   * Set the Modify Method
   *
   * @param iscolValue
   */
  public void setUpdMethod(String iscolValue)
  {
    msUpdMethod = checkForNull(iscolValue);
    addColumnObject(new ColumnObject(UPDATEMETHOD.getName(), msUpdMethod));
  }

  /**
   * Set the Modify Time
   *
   * @param ipcolValue
   */
  public void setModifyTime(Date ipcolValue)
  {
    mpModifyTime = ipcolValue;
    addColumnObject(new ColumnObject(MODIFYTIME.getName(), mpModifyTime));
  }

}
