package com.daifukuamerica.wrxj.jdbc;

import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.jdbc.sqlserver.OffsetDateTimeUtil;
import java.sql.Types;
import java.util.Date;

/**
 * @author Pete Madsen
 *
 */
public class BetweenKeyObject extends KeyObject
{
  public static final int BETWEEN = 7;

  private class BetweenSet
  {
    public Object mpStartValue;
    public Object mpEndValue;
    
    BetweenSet(Object ipStartValue, Object ipEndValue)
    {
      mpStartValue = ipStartValue;
      mpEndValue = ipEndValue;
    }
  }

  /**
   * Constructor
   * 
   * @param isColumnName String
   * @param ipColumnValues variable number of Objects
   */
  public BetweenKeyObject(String isColumnName, Object ipStartColumnValue,
      Object ipEndColumnValue) throws IllegalArgumentException
  {
    super(isColumnName, "");
    
    // We need to verify that both objects are of the same type
    if (!ipStartColumnValue.getClass().equals(ipEndColumnValue.getClass()))
    {
      IllegalArgumentException vpIllegalArgumentException = new IllegalArgumentException(
          "Both ColumnValue objects added to BetweenKeyObject must be of the same class");
      throw vpIllegalArgumentException;
    }
    BetweenSet vpValueSet = new BetweenSet(ipStartColumnValue, ipEndColumnValue);
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
    return(" BETWEEN ");
  }

  @Override
  public int getComparisonSymbolicName()
  {
    return(BETWEEN);
  }
  
  @Override
  public String getSQLString()
  {
    BetweenSet vpBetweenSet = (BetweenSet)getColumnValue();
    StringBuffer vpSQLBuffer = new StringBuffer();
    vpSQLBuffer.append(getColumnName());
    
    int datatype = DBInfo.getFieldType(getColumnName());
    if (datatype == Types.VARCHAR)
    {
      vpSQLBuffer.append(" BETWEEN '").append(vpBetweenSet.mpStartValue)
                 .append("' AND '").append(vpBetweenSet.mpEndValue).append("'");
    }
    else if (datatype == Types.DATE)
    {
      vpSQLBuffer.append(" BETWEEN ")
                 .append(DBHelper.convertDateToDBString((Date)vpBetweenSet.mpStartValue))
                 .append(" AND ")
                 .append(DBHelper.convertDateToDBString((Date)vpBetweenSet.mpEndValue));
      
    }
    else if (datatype == DBInfo.MSSQL_DATETIMEOFFSET)
    {
      vpSQLBuffer.append(" BETWEEN ")
        .append(OffsetDateTimeUtil.toString((Date)vpBetweenSet.mpStartValue))
        .append(" AND ")
        .append(OffsetDateTimeUtil.toString((Date)vpBetweenSet.mpEndValue));
    }
    else
    {
      vpSQLBuffer.append(" BETWEEN ").append(vpBetweenSet.mpStartValue)
                 .append(" AND ").append(vpBetweenSet.mpEndValue);
    }
    
    return vpSQLBuffer.toString();
  }

}
