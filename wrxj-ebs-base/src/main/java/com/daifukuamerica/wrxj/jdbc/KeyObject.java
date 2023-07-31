package com.daifukuamerica.wrxj.jdbc;

import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import java.sql.Types;
import java.util.Collection;
import java.util.Date;

/**
 * Description:<BR>
 *   Class for building SQL key clauses.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 21-Jan-02<BR>
 *     Copyright (c) 2002<BR>
 */
public class KeyObject extends ColumnObject
{
  public static final int EQUALITY               = 0;
  public static final int NOT_EQUAL              = 1;
  public static final int GREATER_THAN           = 2;
  public static final int LESS_THAN              = 3;
  public static final int GREATER_THAN_INCLUSIVE = 4;
  public static final int LESS_THAN_INCLUSIVE    = 5;
  public static final int LIKE                   = 6;
  public static final int AND                    = 8;
  public static final int OR                     = 9;

  private boolean mzPrefixSuffixWildcard = false;
  private boolean mzColumnNameComparison = false;

  private int mnConjunctionSymbolName;
  private int mnComparisonSymbolName;

  private String msConjunction        = "";
  private String msComparisonOperator = "";

  public KeyObject(String isColumnName, Object ipColumnValue)
  {
    super(isColumnName, ipColumnValue);
    msComparisonOperator = " = ";
  }

  /**
   * Constructor
   *
   * @param isColumnName
   * @param ipColumnValue
   * @param inComparison
   * @param inConjunction
   */
  public KeyObject(String isColumnName, Object ipColumnValue, int inComparison,
      int inConjunction)
  {
    this(isColumnName, ipColumnValue);
    setComparison(inComparison);
    setConjunction(inConjunction);
  }

  @Override
  public String toString()
  {
    String s = "  KeyObject.name = " + super.getColumnName() +
               "\n  KeyObject.value = " + super.getColumnValue() + "\n";

    return(s);
  }

  @Override
  public Object clone()
  {
    return(super.clone());
  }

  public String getComparisonOperator()
  {
    return(msComparisonOperator);
  }

  public int getComparisonSymbolicName()
  {
    return(mnComparisonSymbolName);
  }

  public String getConjunctionString()
  {
    return(msConjunction);
  }

  public int getConjunctionTranslation()
  {
    return(mnConjunctionSymbolName);
  }

  public void setPrefixSuffixWildcard()
  {
    mzPrefixSuffixWildcard = true;
  }

  public String getSQLString()
  {
    StringBuffer vpSqlString = new StringBuffer();
    vpSqlString.append(getColumnName());

    if (mzColumnNameComparison)
    {
      vpSqlString.append(getComparisonOperator())
                 .append(getColumnValue());
    }
    else
    {
      int vnDataType = DBInfo.getFieldType(getColumnName());
      if (vnDataType == Types.VARCHAR || vnDataType == Types.CLOB)
      {
        String vsColValue = getColumnValue().toString();
        if (vsColValue.length() == 0)
        {
          if (getComparisonOperator().trim().equals("!="))
            vpSqlString.append(" IS NOT NULL");
          else
            vpSqlString.append(" IS NULL");
        }
        else if (getComparisonOperator().trim().equals("LIKE"))
        {
          if (mzPrefixSuffixWildcard)    // Add wild card before and after column value.
            vpSqlString.append(getComparisonOperator())
                       .append("'%").append(getColumnValue()).append("%'");
          else
            vpSqlString.append(getComparisonOperator()).append("'")
                       .append(getColumnValue()).append("%'");
        }
        else
        {
          vpSqlString.append(getComparisonOperator()).append("'")
                     .append(getColumnValue()).append("'");
        }
      }
      else if (vnDataType == Types.DATE)
      {
        // Check if value is null
        String vsColValue = getColumnValue().toString();
        if (vsColValue.length() == 0)
        {
          if (getComparisonOperator().trim().equals("!=")) {
            vpSqlString.append(" IS NOT NULL");
          }
          else {
            vpSqlString.append(" IS NULL");
          }
        } else {
          vpSqlString
            .append(getComparisonOperator())
            .append(DBHelper.convertDateToDBString((Date)getColumnValue()));            
        }          
      }
      else if (getColumnValue() instanceof String)
      {
        // If it looks like a string...
        String vsColValue = getColumnValue().toString();
        if (vsColValue.length() == 0)
        {
          if (getComparisonOperator().trim().equals("!="))
            vpSqlString.append(" IS NOT NULL");
          else
            vpSqlString.append(" IS NULL");
        }
        else if (getComparisonOperator().trim().equals("LIKE"))
        {
          if (mzPrefixSuffixWildcard)    // Add wild card before and after column value.
            vpSqlString.append(getComparisonOperator())
                       .append("'%").append(getColumnValue()).append("%'");
          else
            vpSqlString.append(getComparisonOperator()).append("'")
                       .append(getColumnValue()).append("%'");
        }
        else
        {
          vpSqlString.append(getComparisonOperator()).append("'")
                     .append(getColumnValue()).append("'");
        }
      }
      else
      {
        vpSqlString.append(getComparisonOperator())
                   .append(getColumnValue());
      }
    }

    return(vpSqlString.toString());
  }

 /**
  * Method to set SQL comparison operator. Translates one of the above
  * constants into a string representing an SQL operator..
  *
  * @param  inComparisonOperator constant to translate into SQL operator.
  */
  public void setComparison(int inComparisonOperator)
  {
    boolean vzValidComparison = true;

    switch(inComparisonOperator)
    {
      case KeyObject.EQUALITY:
        msComparisonOperator = " = ";
        break;

      case KeyObject.NOT_EQUAL:
        msComparisonOperator = " != ";
        break;

      case KeyObject.GREATER_THAN:
        msComparisonOperator = " > ";
        break;

      case KeyObject.LESS_THAN:
        msComparisonOperator = " < ";
        break;

      case KeyObject.GREATER_THAN_INCLUSIVE:
        msComparisonOperator = " >= ";
        break;

      case KeyObject.LESS_THAN_INCLUSIVE:
        msComparisonOperator = " <= ";
        break;

      case KeyObject.LIKE:
        msComparisonOperator = " LIKE ";
        break;

      default:
        msComparisonOperator = " = ";
        vzValidComparison = false;
    }

    mnComparisonSymbolName = (!vzValidComparison) ? KeyObject.EQUALITY
                                                  : inComparisonOperator;
  }

  public void setColumnNameComparison(int inComparison)
  {
    setComparison(inComparison);
    mzColumnNameComparison = true;
  }

 /**
  * Method to set SQL conjunctions.
  *
  * @param  msConjunction represents the AND or OR used to build composite keys.
  */
  public void setConjunction(int inConjunction)
  {
    boolean vzValidConjunction = true;

    switch(inConjunction)
    {
      case KeyObject.AND:
        msConjunction = " AND ";
        break;

      case KeyObject.OR:
        msConjunction = " OR ";
        break;

      default:
        msConjunction = "";
        vzValidConjunction = false;
    }

    mnConjunctionSymbolName = (!vzValidConjunction) ? 0 : inConjunction;
  }

 /**
  * Fetches all columns currently in a collection. The
  * reason for this routine is that we can work directly with the
  * ColumnObject array instead of the container.
  *
  * @return warehouse as string
  */
  public static KeyObject[] toKeyArray(Collection<KeyObject> ipKeys)
  {
    int vnNumKeys = ipKeys.size();
    KeyObject[] vapKeyObj = new KeyObject[vnNumKeys];

                                    // Load up a KeyObject array.
    int i=0;
    for(KeyObject vpKO : ipKeys)
    {
      vapKeyObj[i] = vpKO;
      i++;
    }

    return(vapKeyObj);
  }
}
