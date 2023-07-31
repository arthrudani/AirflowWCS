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

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBResultSet;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A lower level data base object that interfaces to the ReasonCode table.
 *
 * @author  jan
 * @version 1.0
 * @file    ReasonCode.java
 */
public class ReasonCode extends BaseDBInterface
{
  protected ReasonCodeData mpRCData;
  protected DBResultSet mpDBResultSet;

  public ReasonCode()
  {
    super("ReasonCode");  // Table name
    mpRCData = Factory.create(ReasonCodeData.class);
  }

  /**
   * Get the default Daifuku reason code.  If DefaultAutoPickReasonCode is not
   * defined, it defaults to "AUT".
   *
   * @return String
   */
  public static String getAutoPickReasonCode()
  {
    return Application.getString("DefaultAutoPickReasonCode", "AUT");
  }

  /**
   * Get the default Cycle Count reason code. If DefaultCycleCountReasonCode is
   * not defined, it defaults to "CYC".
   *
   * @return String
   */
  public static String getCycleCountReasonCode()
  {
    return Application.getString("DefaultCycleCountReasonCode", "CYC");
  }

  /**
   * Get the default Daifuku reason code.  If DefaultDaifukuReasonCode is not
   * defined, it defaults to "DAC".
   *
   * @return String
   */
  public static String getDaifukuReasonCode()
  {
    return Application.getString("DefaultDaifukuReasonCode", "DAC");
  }

  /**
   * Get the default Cycle Count reason code. If
   * DefaultInventoryAdjustReasonCode is not defined, it defaults to "CYC".
   *
   * @return String
   */
  public static String getDefaultInvAdjustReasonCode()
  {
    return Application.getString("DefaultInventoryAdjustReasonCode", "IA");
  }

  /**
   * Get the default Shipping reason code. If DefaultShippingReasonCode is
   * not defined, it defaults to "SHP".
   *
   * @return
   */
  public static String getItemLoadTransferReasonCode()
  {
    return Application.getString("DefaultItemLoadTransferReasonCode", "IA");
  }
  /**
   * Get the default Shipping reason code. If DefaultShippingReasonCode is
   * not defined, it defaults to "SHP".
   *
   * @return
   */
  public static String getShippingReasonCode()
  {
    return Application.getString("DefaultShippingReasonCode", "SHP");
  }

  /**
   * Retrieves List of all Reason Codes in a specified Category
   * and their descriptions on the system.
   * The data returned from here is primarily meant to be used in choice lists.
   *
   * @param int iiReasonCategory - the category for which to get the reason codes.
   * @return String array of reason codes and descriptions.
   */
  public String[] getReasonCodeChoices(int iiReasonCategory) throws DBException
  {
                                         // Clear out Sql String buffer.
    StringBuilder vpSql = new StringBuilder("SELECT CONCAT(sReasonCode, ")
        .append("CONCAT(': ', sDescription)) AS \"SREASONCODE\" ")
        .append("FROM ReasonCode WHERE ").append("iReasonCategory = ")
        .append(iiReasonCategory).append(" ").append("ORDER BY sReasonCode");

     return SKDCUtility.toStringArray(fetchRecords(vpSql.toString()),ReasonCodeData.REASONCODE_NAME);
  }

  /**
   * Get list of Reason Codes
   */
  public List<Map> getReasonCodeDataList(KeyObject[] iapKeys)
      throws DBException
  {
    List<Map> reasonCodeList = new ArrayList<Map>();
    StringBuilder vpSql = new StringBuilder("SELECT * FROM ").append(getWriteTableName())
             .append(DBHelper.buildWhereClause(iapKeys))
             .append(" ORDER BY sReasonCode");

    mpDBResultSet = execute(vpSql.toString(), (Object[])null);

    Map row;
    while (mpDBResultSet.hasNext())  // may be multiple rows
    {
      row = (Map) mpDBResultSet.next();
      reasonCodeList.add(row);
    }
    return(reasonCodeList);
  }

  /**
   * Method to get Reason Code data for specified Category and Reason Code.
   *
   * @param inReasonCategory ReasonCategory.
   * @param isReasonCode ReasonCode.
   * @return ReasonCodeData object containing ReasonCode info. matching our
   *         search criteria.
   * @exception DBException
   */
  public ReasonCodeData getReasonCodeData(int inReasonCategory,
      String isReasonCode) throws DBException
  {
    mpRCData.clear();
    mpRCData.setKey(ReasonCodeData.REASONCODE_NAME, isReasonCode);
    mpRCData.setKey(ReasonCodeData.REASONCATEGORY_NAME, inReasonCategory);
    return(getElement(mpRCData, DBConstants.NOWRITELOCK));
  }

  /**
   * Method to see if the specified reason category and code exist.
   *
   * @param inReasonCategory ReasonCategory.
   * @param isReasonCode ReasonCode
   * @return boolean of <code>true</code> if it exists.
   * @exception DBException
   */
  public boolean existReasonCode(int inReasonCategory, String isReasonCode)
      throws DBException
  {
    mpRCData.clear();
    mpRCData.setKey(ReasonCodeData.REASONCATEGORY_NAME, inReasonCategory);
    mpRCData.setKey(ReasonCodeData.REASONCODE_NAME, isReasonCode);
    return(getCount(mpRCData) > 0);
  }

  /**
   * Method to delete a reason code.
   *
   * @param inReasonCategory ReasonCategory.
   * @param isReasonCode
   * @exception DBException
   */
  public void deleteReasonCode(int inReasonCategory, String isReasonCode)
      throws DBException
  {
    if (existReasonCode(inReasonCategory, isReasonCode))
    {
      mpRCData.clear();
      mpRCData.setKey(ReasonCodeData.REASONCATEGORY_NAME, inReasonCategory);
      mpRCData.setKey(ReasonCodeData.REASONCODE_NAME, isReasonCode);
      deleteElement(mpRCData);
    }
  }

  /**
   * Method to add this ReasonCode to the database.
   *
   * @exception DBException
   */
  public void addReasonCode(int iiReasonCategory, String isReasonCode,
      String isDescription) throws DBException
  {
    mpRCData.clear();
    mpRCData.setReasonCategory(iiReasonCategory);
    mpRCData.setReasonCode(isReasonCode);
    mpRCData.setDescription(isDescription);
    addElement(mpRCData);
  }

  /**
   *  Method to update a reason code.
   *
   *  @param ipRCData Filled in reason code data object.
   *  @exception DBException
   */
  public void updateReasonCodeInfo(ReasonCodeData ipRCData) throws DBException
  {
    mpRCData.clear();
    mpRCData.setKey(ReasonCodeData.REASONCODE_NAME, ipRCData.getReasonCode());
    mpRCData.setReasonCategory(ipRCData.getReasonCategory());
    mpRCData.setDescription(ipRCData.getReasonCodeDescription());
    modifyElement(mpRCData);
  }

  /**
   *  Sets Objects for garbage collection.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpRCData = null;
    mpDBResultSet = null;
  }

}