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

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.List;
import java.util.Map;


/**
 * Description:<BR>
 *   Class for handling AsrsMetaData table interactions.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 23-Oct-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  Daifuku America Corporation
 */
public class AsrsMetaData extends BaseDBInterface
{
  protected AsrsMetaDataData mpMDData;

  public AsrsMetaData()
  {
    super("AsrsMetaData");
    mpMDData = Factory.create(AsrsMetaDataData.class);
  }

  /**
   * Retrieves List of AsrsMetaData data.
   * @param  ipMDKey <code>AbstractSKDCData</code> object containing key
   *         information to do the lookup.  If there is no key info. then
   *         we do a wild-card search.
   *
   * @return List of data.
   */
  @Override
  public List<Map> getAllElements(AbstractSKDCData ipMDKey) throws DBException
  {
    ipMDKey.addOrderByColumn(AsrsMetaDataData.DISPLAYORDER_NAME);
    return super.getAllElements(ipMDKey);
  }

  /**
   * Method to retrieve columns from ASRS MetaData table ignoring
   * non-displayable columns.
   * 
   * @param isMetaDataScheme <code>String</code> containing Meta-Data name.
   * 
   * @return Array of strings containing column names.
   * @throws DBException if there is a database access error.
   */
  public String[] getOrderedColumns(String isMetaDataScheme)
         throws DBException
  {
    return getOrderedColumns(isMetaDataScheme, true);
  }

  /**
   * Method to return column names in asrsmetadata using the ordering as setup
   * in the table.
   * 
   * @param isMetadataViewName the name of the view.
   * @param izOnlyDisplayable return only displayable columns (those with values >=
   *            0) if set to <code>true</code>
   * @return Array of strings of column names.
   * @throws DBException if there is a database access error.
   */
  public String[] getOrderedColumns(String isMetadataViewName,
      boolean izOnlyDisplayable) throws DBException
  {
    mpMDData.clear();
    mpMDData.setKey(AsrsMetaDataData.DATAVIEWNAME_NAME, isMetadataViewName);
    if (izOnlyDisplayable)
    {
      mpMDData.setKey(AsrsMetaDataData.DISPLAYORDER_NAME, -1, KeyObject.NOT_EQUAL);
    }
    mpMDData.addOrderByColumn(AsrsMetaDataData.DISPLAYORDER_NAME);
    
    return getSingleColumnValues(AsrsMetaDataData.COLUMNNAME_NAME, false,
                                 mpMDData, SKDCConstants.NO_PREPENDER);
  }
  
  /**
   * Method to get array of undisplayed columns in the view.
   * 
   * @param metaDataScheme
   * @return
   * @throws DBException if there is a DB access error.
   */
  public String[] getNonDisplayColumns(String metaDataScheme) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT sColumnName FROM AsrsMetaData WHERE ")
             .append("sDataViewName = '").append(metaDataScheme).append("' AND ")
             .append("iDisplayOrder = -1 ")
             .append("ORDER BY sColumnName");
    
    return SKDCUtility.toStringArray(fetchRecords(vpSql.toString()),
                                     AsrsMetaDataData.COLUMNNAME_NAME);
  }
  
  /**
   *  Method gets a list of all asrs meta-data views.
   * @param izInsertAll if <code>true</code> the "ALL" string appears as first
   *                    entry of returned array.
   * @return array of asrs metadata view names.
   * @throws DBException if there is a DB access error.
   */
  public String[] getAsrsMetaDataChoices(boolean izInsertAll) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT sDataViewName FROM AsrsMetadata")
             .append(" ORDER BY sDataViewName");
    return getList(vpSql.toString(), AsrsMetaDataData.DATAVIEWNAME_NAME,
                  (izInsertAll)? SKDCConstants.ALL_STRING : SKDCConstants.NO_PREPENDER);
  }

  /**
   *  Sets Objects for garbage collection.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpMDData = null;
  }

  /**
   * Get descriptive name of column.
   * 
   * @param isDataView the asrs metadata view name.
   * @param isColumnName the abbreviated column name which is the database
   *        column name
   * @return descriptive name of DB column
   * @throws DBException if there is a DB access error.
   */
  public String getFullName(String isDataView, String isColumnName)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT sFullName FROM AsrsMetaData WHERE ")
             .append("sDataViewName = '").append(isDataView).append("' AND ")
             .append("sColumnName = '").append(isColumnName).append("'");
    
    return getStringColumn(AsrsMetaDataData.FULLNAME_NAME, vpSql.toString());
  }
}
