package com.daifukuamerica.wrxj.jdbc;

import java.sql.Types;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A KeyObject that uses the SQL 'IN' key word as a comparison operator
 *
 */
public class InKeyObject extends KeyObject
{
  public static final int IN = 10;
  /**
   * Constructor
   *
   * @param isColumnName String
   * @param iapColumnValues variable number of Objects
   */
  public InKeyObject(String isColumnName, Object... iapColumnValues)
      throws IllegalArgumentException
  {
    super(isColumnName, "");

    Set<Object> vpValueSet = new HashSet<Object>(iapColumnValues.length*2);
    // We need to verify that all objects are of the same type
    for (int viIndex = 0; viIndex < iapColumnValues.length; viIndex++)
    {
      if (!iapColumnValues[viIndex].getClass().equals(iapColumnValues[0].getClass()))
      {
        IllegalArgumentException vpIllegalArgumentException = new IllegalArgumentException(
            "All ColumnValue objects added to InKeyObject must be of the same class");
        throw vpIllegalArgumentException;
      }
      vpValueSet.add(iapColumnValues[viIndex]);
    }
    setColumnValue(vpValueSet);
  }

  @Override
  public String toString()
  {
    String vsString = "  KeyObject.name = " + super.getColumnName() +
                      "\n  KeyObject.value = " + super.getColumnValue() + "\n";

    return(vsString);
  }

  @Override
  public String getComparisonOperator()
  {
    return(" IN ");
  }

  @Override
  public int getComparisonSymbolicName()
  {
    return(IN);
  }

  @Override
  public String getSQLString()
  {
    Set<Object> vpValueSet = (Set<Object>) getColumnValue();
    Iterator<Object> vpIterator = vpValueSet.iterator();
    StringBuffer vpSQLBuffer = new StringBuffer();
    vpSQLBuffer.append(getColumnName()).append(" IN (");

    int datatype = DBInfo.getFieldType(getColumnName());
    if (datatype == Types.VARCHAR)
    {
      while(vpIterator.hasNext())
      {
        vpSQLBuffer.append("'").append(vpIterator.next()).append("'");
        if(vpIterator.hasNext()) vpSQLBuffer.append(", ");
      }
    }
    else
    {
      while(vpIterator.hasNext())
      {
        vpSQLBuffer.append(vpIterator.next());
        if(vpIterator.hasNext()) vpSQLBuffer.append(", ");
      }
    }
    vpSQLBuffer.append(")");
    return vpSQLBuffer.toString();
  }
}
