package com.daifukuamerica.wrxj.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * <B>Description:</B> Handle large result sets
 *
 * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
public class DBLargeResultSet
{
  private PreparedStatement mpStatement = null;
  private ResultSet mpResultSet = null;
  private int mnResultsPerPage = -1;
  
  /**
   * Constructor
   */
  public DBLargeResultSet(PreparedStatement ipStatement, ResultSet ipResultSet)
  {
    super();
    mpStatement = ipStatement;
    mpResultSet = ipResultSet;
  }

  /**
   * Set the number of result rows per page
   * 
   * @param inResultsPerPage
   */
  public void setResultsPerPage(int inResultsPerPage)
  {
    mnResultsPerPage = inResultsPerPage;
  }
  
  /**
   * Cleanup 
   */
  public void cleanUp() throws DBException
  {
    try
    {
      if (mpResultSet != null)
      {
        mpResultSet.close();
        mpResultSet = null;
      }
      if (mpStatement != null)
      {
        mpStatement.close();
        mpStatement = null;
      }
    }
    catch (SQLException sqle)
    {
      throw new DBException(sqle);
    }
  }
  
  /**
   * @see java.lang.Object#finalize()
   */
  @Override
  protected void finalize() throws Throwable
  {
    cleanUp();
    super.finalize();
  }
  
  /**
   * Get the next page of results from a large result set
   * 
   * @return
   * @throws DBException
   */
  public List<Map> fetchNextLargeRecordListEntries()
      throws DBException
  {
    if (mpResultSet == null)
    {
      throw new DBException("Results not initialized!");
    }
    if (mnResultsPerPage < 0)
    {
      throw new DBException("ResultsPerPage not initialized!");
    }
    try
    {
      DBResultSet vpDBRS = new DBResultSet();
      
      int vnCount = 0;
      while (vnCount < mnResultsPerPage && mpResultSet.next())
      {
        vnCount++;
        vpDBRS.addRow(mpResultSet);
      }
      return vpDBRS.getRows();
    }
    catch (SQLException sqle)
    {
      throw new DBException(sqle);
    }
  }
}
