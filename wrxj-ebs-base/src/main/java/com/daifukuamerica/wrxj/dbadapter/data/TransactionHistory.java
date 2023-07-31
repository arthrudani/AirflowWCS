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
  CONSENT OF Daifuku America Corporation ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * Description:<BR>
 *   Class to handle Transaction History specific operations.  This interface
 *   is only for the database interface.
 *
 * @author       A.D.
 * @version      1.0   04/15/03
 */
public class TransactionHistory extends BaseDBInterface 
{
  private TransactionHistoryData mpTHData;

  public TransactionHistory()
  {
    super("TransactionHistory");
    mpTHData = Factory.create(TransactionHistoryData.class);
  }

  /**
   * Sets Objects for garbage collection.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpTHData = null;
  }

  /**
   * Method returns the count of transaction records between a start and end
   * date period. If the ending date is passed as <code>null</code> the count
   * will be of all records greated than beginDate.
   * 
   * @param beginDate <code>Date</code> containing starting date.
   * @param endDate <code>Date</code> containing ending date.
   * 
   * @return <code>int</code> of record count.
   */
  public int getCount(Date beginDate, Date endDate) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT COUNT(")
               .append(TransactionHistoryData.TRANSDATETIME_NAME).append(") ")
               .append("AS \"").append(ROW_COUNT_NAME).append("\" ")
               .append("FROM TransactionHistory WHERE ");

    try
    {
      vpSql.append(DBHelper.getDateRangeSQL(
          TransactionHistoryData.TRANSDATETIME_NAME, beginDate, endDate));
    }
    catch(ParseException pe)
    {
      throw new DBException(pe.getMessage());
    }

    return getRecordCount(vpSql.toString(), ROW_COUNT_NAME);
  }

  /**
   * Method returns the count of transaction records in a particular category.
   * 
   * @param tranCategory <code>int</code> (translation) containing Transaction
   *            category.
   * 
   * @return <code>int</code> of record count.
   */
  public int getCount(int tranCategory) throws DBException
  {
    mpTHData.clear();
    mpTHData.setKey(TransactionHistoryData.TRANCATEGORY_NAME, tranCategory);
    return getCount(mpTHData);
  }

  /**
   * Method returns the count of transaction records in a particular category.
   * 
   * @param tranCategory <code>int</code> (translation) containing Transaction
   *            category.
   * @param tranType <code>int</code> (translation) containing Transaction
   *            type.
   * 
   * @return <code>int</code> of record count.
   */
  public int getCount(int tranCategory, int tranType) throws DBException
  {
    mpTHData.clear();
    mpTHData.setKey(TransactionHistoryData.TRANCATEGORY_NAME, tranCategory);
    mpTHData.setKey(TransactionHistoryData.TRANTYPE_NAME, tranType);
    return getCount(mpTHData);
  }

  /**
   * Method returns the count of transaction records in a particular category.
   * 
   * @param tranCategory <code>int</code> (translation) containing Transaction
   *            category.
   * @param tranType <code>int</code> (translation) containing Transaction
   *            type.
   * @param beginDate <code>Date</code> containing starting date.
   * @param endDate <code>Date</code> containing ending date.
   * 
   * @return <code>int</code> of record count.
   */
  public int getCount(int tranCategory, int tranType, Date beginDate,
      Date endDate) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT COUNT(")
               .append(TransactionHistoryData.TRANSDATETIME_NAME).append(") ")
               .append("AS \"").append(ROW_COUNT_NAME).append("\" ")
               .append("FROM TransactionHistory ")
               .append("WHERE ")
               .append(TransactionHistoryData.TRANCATEGORY_NAME).append(" = ")
               .append(tranCategory).append(" AND ")
               .append(TransactionHistoryData.TRANTYPE_NAME).append(" = ")
               .append(tranType).append(" AND ");
    try
    {
      vpSql.append(DBHelper.getDateRangeSQL(
          TransactionHistoryData.TRANSDATETIME_NAME, beginDate, endDate));
    }
    catch(ParseException pe)
    {
      throw new DBException(pe.getMessage());
    }

    return getRecordCount(vpSql.toString(), ROW_COUNT_NAME);
  }
  
  /**
   * Method returns the oldest Date a transaction record was written.
   * 
   * @param izPureDateValueOnly set to <code>true</code> if only the date
   *            (dd-MMM-YYYY) should be returned; the time will have zero value.
   *            Set to <code>false</code> to get back the date and time value.
   * @return the oldest transaction date. <code>null</code> if no record
   *         found.
   * @throws DBException if there is a database access error.
   */
  public Date getOldestDate(boolean izPureDateValueOnly) throws DBException
  {
    Date vpRtnDate;
    String vsOldestDate = "dOldestDate";
    
    StringBuilder vpSql = new StringBuilder("SELECT MIN(")
               .append(TransactionHistoryData.TRANSDATETIME_NAME).append(") ")
               .append("AS \"").append(vsOldestDate).append("\" ")
               .append("FROM TransactionHistory");
    vpRtnDate = getDateColumn(vsOldestDate, vpSql.toString());
    
    if (vpRtnDate != null && izPureDateValueOnly)
    {
      Calendar vpCal = Calendar.getInstance();
      vpCal.setTime(vpRtnDate);

      vpCal.set(Calendar.HOUR_OF_DAY, 0);
      vpCal.set(Calendar.MINUTE, 0);
      vpCal.set(Calendar.SECOND, 0);
      vpCal.set(Calendar.MILLISECOND, 0);
      vpRtnDate = vpCal.getTime();
    }

    return(vpRtnDate);
  }
  
  /**
   * Method returns the last Date a transaction record was written.
   * 
   * @param izPureDateValueOnly set to <code>true</code> if only the date
   *            (dd-MMM-YYYY) should be returned; the time will have zero value.
   *            Set to <code>false</code> to get back the date and time value.
   * @return the last transaction date. <code>null</code> if no record found.
   * @throws DBException if there is a database access error.
   */
  public Date getNewestDate(boolean izPureDateValueOnly) throws DBException
  {
    Date vpRtnDate;
    String vsNewestDate = "dNewestDate";
    
    StringBuilder vpSql = new StringBuilder("SELECT MAX(")
               .append(TransactionHistoryData.TRANSDATETIME_NAME).append(") ")
               .append("AS \"").append(vsNewestDate).append("\" ")
               .append("FROM TransactionHistory");
    vpRtnDate = getDateColumn(vsNewestDate, vpSql.toString());
    
    if (vpRtnDate != null && izPureDateValueOnly)
    {
      Calendar vpCal = Calendar.getInstance();
      vpCal.setTime(vpRtnDate);

      vpCal.set(Calendar.HOUR_OF_DAY, 0);
      vpCal.set(Calendar.MINUTE, 0);
      vpCal.set(Calendar.SECOND, 0);
      vpCal.set(Calendar.MILLISECOND, 0);
      vpRtnDate = vpCal.getTime();
    }
    
    return(vpRtnDate);
  }
}
