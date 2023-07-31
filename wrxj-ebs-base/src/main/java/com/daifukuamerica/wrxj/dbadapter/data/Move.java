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
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBResultSet;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Title: Move Class
 * Description: Provides all functionality to the move object
 * Copyright:    Copyright (c) 2001
 * Company: SKDC
 * @author : Ed Askew
 *          A.D.  Refactored to fit BaseDBInterface.
 * @version 1.0
 */
public class Move extends BaseDBInterface
{
  protected MoveData mpMoveData;
  private DBResultSet mpDBResultSet;

  /**
   * Constructor
   */
  public Move()
  {
    super("Move", "MoveView");
    mpMoveData = Factory.create(MoveData.class);
  }

  /**
   * Get a Move element
   */
  @Override
  public <Type extends AbstractSKDCData> Type getElement(Type ipData,
      int inLockFlag) throws DBException
  {
    if (inLockFlag == DBConstants.WRITELOCK)
    {
      Type vpLockedMove = super.getElement(ipData, DBConstants.WRITELOCK);
      if (vpLockedMove != null)
      {
        // The read without a lock gets more data (from the MoveView).
        // After locking the record, re-read from the view and return that data.
        return super.getElement(ipData, DBConstants.NOWRITELOCK);
      }
      return vpLockedMove;
    }
    else
    {
      return super.getElement(ipData, inLockFlag);
    }
  };
  
  /**
   * Method to get the Move Type of a move record.
   * @param ipMoveData Key information to find move record.  The key should provide
   *        enough info. for a unique record.  If it does not, <u>the move type of the
   *        first move found will be returned.</u>
   * @return the move type of the move. -1 if nothing is found.
   * @throws DBException if there is a database access error.
   */
  public int getMoveTypeValue(MoveData ipMoveData) throws DBException
  {
    ipMoveData.setMoveType(0);
    List<Map> vpList = getSelectedColumnElements(ipMoveData);

    if (vpList.isEmpty())
    {
      return(-1);
    }
    
    return(DBHelper.getIntegerField(vpList.get(0), MoveData.MOVETYPE_NAME));
  }
  
  /**
   *  Get move records for a load.
   */
  public List<Map> getMovesByLoadID(String isLoadID) throws DBException
  {
    mpMoveData.clear();
    mpMoveData.setKey(MoveData.LOADID_NAME, isLoadID);
    return(getAllElements(mpMoveData));
  }

  /**
   * Method gets all order ids. associated with one or more moves for a Load. It
   * is possible to have multiple order picks from the same load and so a string
   * array of order ids. is returned.
   * 
   * @param isLoadID the load id. that has all the associated moves.
   * @return a string array of all associated orders with this load. An empty
   *         array if no moves are found.
   * @throws DBException if there is a database access errors.
   */
  public String[] getAssociatedOrdersForLoad(String isLoadID) throws DBException
  {
    mpMoveData.clear();
    mpMoveData.setKey(MoveData.LOADID_NAME, isLoadID);
    
    return getSingleColumnValues(MoveData.ORDERID_NAME, true, mpMoveData,
        SKDCConstants.NO_PREPENDER);
  }
  
  /**
   *  Method gets the order associated with a move.
   *
   *  @return <code>String</code> containing order id.
   *  @throws DBException if there is a serious DB error.
   */
  public String getMoveOrderID(int inMoveID) throws DBException
  {
    mpMoveData.clear();
    mpMoveData.setKey(MoveData.MOVEID_NAME, Integer.valueOf(inMoveID));
    MoveData myMoveData = getElement(mpMoveData, DBConstants.NOWRITELOCK);

    return myMoveData.getOrderID();
  }

  /**
   *  Get move records for a load, item and lot.
   */
  public List<Map> getMTMovesByLoadID(String isLoadID, String isItem,
      String isOrderLot) throws DBException
  {
    // There can be multiple MT requests find all matching moves moves can be 
    // for different orders

    mpMoveData.clear();
    mpMoveData.setKey(MoveData.LOADID_NAME, isLoadID);
    mpMoveData.setKey(MoveData.MOVETYPE_NAME, DBConstants.EMPTYMOVE);
    if (SKDCUtility.isFilledIn(isItem))
    {
      mpMoveData.setKey(MoveData.ITEM_NAME, isItem);
      if (SKDCUtility.isFilledIn(isOrderLot))
      {
        mpMoveData.setKey(MoveData.ORDERLOT_NAME, isOrderLot);
      }
    }

    return getAllElements(mpMoveData);
  }

   /**
    * Method to set Move Status for a given parent load.
    * 
    * @param isParentLoad <code>String</code> the Load ID of the parent load
    * @param inStatus <code>int</code> the new Move Status
    * @throws DBException if there is a serious DB error.
    */
    public void setMoveStatusByParentLoad(String isParentLoad, int inStatus)
           throws DBException
    {
      mpMoveData.clear();
      mpMoveData.setMoveStatus(inStatus);

      mpMoveData.setKey(MoveData.PARENTLOAD_NAME, isParentLoad);
      modifyElement(mpMoveData);
    }
  
  /**
   * Method checks if a load already has a move.
   * 
   * @param sSuperLoad <code>String</code> containing Super load id.
   * @param sLoadID <code>String</code> containing Load id.
   * @return <code>true</code> if there are <b>no</b> moves in the system,
   *         <code>false</code> otherwise.
   */
  public boolean loadNeedsMove(String isSuperLoad, String isLoadid)
      throws DBException
  {
    mpMoveData.clear();
    mpMoveData.setKey(MoveData.PARENTLOAD_NAME, isSuperLoad);
    mpMoveData.setKey(MoveData.LOADID_NAME, isLoadid);
    
    // Get back zero if Parent load, and load combination have no move records.
    return getCount(mpMoveData) == 0;
  }
  
  /**
   * Method checks if a load already has a move using load id. and move type in
   * the check.
   * 
   * @param isLoadID <code>String</code> containing Load id.
   * @param inMoveType <code>int</code> containing move type to check for.
   * @return <code>true</code> if there are <b>no</b> moves in the system,
   *         <code>false</code> otherwise.
   */
  public boolean loadNeedsMove(String isLoadID, int inMoveType)
      throws DBException
  {
    mpMoveData.clear();
    mpMoveData.setKey(MoveData.LOADID_NAME, isLoadID);
    mpMoveData.setKey(MoveData.MOVETYPE_NAME, Integer.valueOf(inMoveType));

    return getCount(mpMoveData) == 0;
  }
  
  /**
   * Method checks if a load already has a move.
   * 
   * @param isOrderID <code>String</code> containing move Order id.
   * @param isSuperLoad <code>String</code> containing Super load id.
   * @param isLoadID <code>String</code> containing Load id.
   * @param isItem <code>String</code> containing Item.
   * @param isPickLot <code>String</code> containing Pick lot.
   * @param isOrderLot <code>String</code> containing order lot.
   * @param isLineID <code>String</code> containing order Line ID.
   * 
   * @return <code>true</code> if there are <b>no</b> moves in the system,
   *         <code>false</code> otherwise.
   * @throws com.daifukuamerica.wrxj.common.jdbc.DBException
   */
  public boolean loadNeedsMove(String isOrderID, String isSuperLoad,
      String isLoadID, String isItem, String isPickLot, String isOrderLot,
      String isLineID) throws DBException
  {
    mpMoveData.clear();
    mpMoveData.setKey(MoveData.ORDERID_NAME, isOrderID);
    mpMoveData.setKey(MoveData.PARENTLOAD_NAME, isSuperLoad);
    mpMoveData.setKey(MoveData.LOADID_NAME, isLoadID);
    mpMoveData.setKey(MoveData.ITEM_NAME, isItem);
    mpMoveData.setKey(MoveData.PICKLOT_NAME, isPickLot);
    mpMoveData.setKey(MoveData.ORDERLOT_NAME, isOrderLot);
    mpMoveData.setKey(MoveData.LINEID_NAME, isLineID);
    
    // Get back zero if Parent load, and load combination have no move records.
    return getCount(mpMoveData) == 0;
  }

  /**
   * Delete a move
   * 
   * @param inMoveID
   * @throws DBException
   */
  public void deleteByMoveID(int inMoveID) throws DBException
  {
    mpMoveData.clear();
    mpMoveData.setKey(MoveData.MOVEID_NAME, inMoveID);
    deleteElement(mpMoveData);
  }

 /**
  *  Sets Objects for garbage collection.
  */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpMoveData        = null;
  }

  /**
   * Get list of multiple Move Data
   * 
   * @param iapKeys key info. array.
   * @return
   * @throws DBException
   */
  public List<Map> getMoveDataList(KeyObject[] iapKeys) throws DBException
  {
    mpMoveData.clear();
    if (iapKeys != null)
      mpMoveData.setKeys(iapKeys);
    mpMoveData.setOrderByColumns(MoveData.LOADID_NAME);

    return getAllElements(mpMoveData);
  }

  /**
   *  Get the Move's that have Loads with the Device passed in
   *
   * @param sDeviceID <code>String</code> object.
   *
   * @return reference to an List of Move objects containing
   *          null reference if no Moves found.
   * @exception DBException
   */
  public List<Map> getMovesByLoadDevice(String sDeviceID) throws DBException
  {
    // Select all moves that have loads that have the device that is passed in.
    StringBuilder vpSql = new StringBuilder("SELECT MV.* FROM MOVE MV, LOAD LD ")
        .append("WHERE LD.SDEVICEID = '").append(sDeviceID).append("' AND ")
        .append("MV.SLOADID = LD.SLOADID ORDER BY MV.IMOVETYPE, MV.SLOADID");
    
    return fetchRecords(vpSql.toString());
  }
   
  /**
   * Method to get multiple move data for specified criteria.
   * 
   * @param iapKeys KeyObject containing search criteria.
   * @return <code>List</code> containing matching load line item information.
   * 
   * @exception DBException
   */
  public List<Map> getMovesDataList(KeyObject[] iapKeys) throws DBException
  {
    // Clear out Sql String buffer.
    StringBuilder vpSql = new StringBuilder("SELECT mv.*, ld.swarehouse, ld.saddress FROM MOVE mv, LOAD ld ");

    if (iapKeys == null || iapKeys.length == 0)
    {
      vpSql.append(" WHERE mv.sLoadID = ld.sLoadID ORDER BY mv.sLoadID ");
    }
    else
    {
      boolean foundone = false;
              // Make sure we are using a key, otherwise we could end up with no
              // "WHERE" and an "AND" and thus an sql error
      for(int idx = 0; idx < iapKeys.length; idx++)
      {

        if (iapKeys[idx] == null ||
            iapKeys[idx].equalsValue(SKDCConstants.ALL_STRING) ||
            iapKeys[idx].equalsValue(Integer.valueOf(SKDCConstants.ALL_INT)))
        {
          continue;
        }
        foundone = true;
      }
      if(foundone == true)
      {
        int loadIdx = KeyObject.getColumnObjectIndex(MoveData.LOADID_NAME, iapKeys);
        if (loadIdx != -1) 
          iapKeys[loadIdx].setColumnName("mv." + MoveData.LOADID_NAME);
        
        int vnDeviceIdx = KeyObject.getColumnObjectIndex(MoveData.DEVICEID_NAME, iapKeys);
        if (vnDeviceIdx != -1)
          iapKeys[vnDeviceIdx].setColumnName("mv." + MoveData.DEVICEID_NAME);

        String vsWhere = DBHelper.buildWhereClause(iapKeys);
        vpSql.append(vsWhere);
        
        // Add join keys
        if (vsWhere.trim().length() == 0)
        {
          vpSql.append(" WHERE ");
        }
        else
        {
          vpSql.append(" AND ");
        }
        vpSql.append(" mv.sLoadID = ld.sLoadID ORDER BY mv.sLoadID ");
      }
      else
      {
        vpSql.append(" WHERE mv.sLoadID = ld.sLoadID ORDER BY mv.sLoadID ");
      }
    }
    return fetchRecords(vpSql.toString());
  }
 
  /*========================================================================*/

  /**
   * List all loads onboard this device
   * @param isDeviceID
   * @return
   * @throws DBException
   */
  public List<Map> getLoadsOnboard(String isDeviceID) throws DBException
  {
    StringBuilder vpSql = new StringBuilder(" SELECT mv.IMOVEID, mv.SLOADID, mv.SNEXTWAREHOUSE, ")
             .append(" mv.SNEXTADDRESS, mv.SDESTWAREHOUSE, mv.SDESTADDRESS ")
             .append( " FROM MOVEVIEW mv, LOAD ld")
             .append(" WHERE mv.SDEVICEID = '").append(isDeviceID).append("'")
             .append(" AND mv.SLOADID = ld.SLOADID ")
             .append(" AND IMOVESTATUS = ").append( DBConstants.ASSIGNED)
             .append(" AND ILOADMOVESTATUS = ").append( DBConstants.MOVING);

    return fetchRecords(vpSql.toString());
  }
	  
  /**
   * Retrieves a String[] list of all Moves with Invalid Loadids
   * 
   * @return reference to an String[] of Moves
   * 
   * @exception DBException
   */
  public List<Map> getMovesWithInvalidLoadIDs() throws DBException
  {
    List<Map> MoveList = new ArrayList<Map>();

    String tmpstring = ("SELECT sLoadID, sItem, sOrderID, sRouteID FROM Move"
        + " Where Move.sloadid NOT IN (SELECT sloadid from load)");

    mpDBResultSet = execute(tmpstring);
    Map row;
    while (mpDBResultSet.hasNext()) // may be multiple rows
    {
      row = (Map) mpDBResultSet.next();
      MoveList.add(row);
    }
    return (MoveList);
  }
  
  /**
   * Get a list of moves by device
   * 
   * @param inMoveType
   * @return
   * @throws DBException
   */
  public List<Map> getMoveReportList(int inMoveType) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT ").append(MoveData.DEVICEID_NAME)
               .append(" FROM ").append(getReadTableName());
    
    List<Map> vpList;
    if (inMoveType == SKDCConstants.ALL_INT)
    {
      vpSql.append(" ORDER BY ").append(MoveData.DEVICEID_NAME);
      vpList = fetchRecords(vpSql.toString());
    }
    else
    {
      vpSql.append(" WHERE ").append(MoveData.MOVETYPE_NAME).append("=?")
                 .append(" ORDER BY ").append(MoveData.DEVICEID_NAME);
      vpList = fetchRecords(vpSql.toString(), inMoveType);
    }
    for (Map m : vpList)
    {
      for (int i=1; i<10; i++)
      {
        int vnPPicks = getPriorityMoves(
            m.get(MoveData.DEVICEID_NAME).toString(), i, inMoveType);
        m.put(Integer.valueOf(i).toString(), vnPPicks);
      }
    }
    
    /*
     * Total line
     */
    Map vpTotalMap = new TreeMap();
    vpTotalMap.put(MoveData.DEVICEID_NAME, "Total");
    for (int i=1; i<10; i++)
    {
      int vnPPicks = 0;
      for (Map m : vpList)
      {
        vnPPicks += (Integer)m.get(Integer.valueOf(i).toString());
      }

      vpTotalMap.put(Integer.valueOf(i).toString(), vnPPicks);
    }
    vpList.add(vpTotalMap);

    /*
     * Total column
     */
    for (Map m : vpList)
    {
      int vnPPicks = 0;
      for (int i=1; i<10; i++)
      {
        vnPPicks += (Integer)m.get(Integer.valueOf(i).toString());
      }
      m.put("ITOTAL", vnPPicks);
    }
    
    
    return vpList;
  }

  /**
   * Get the count of moves for a given priority and device
   * 
   * @param isDevice
   * @param inPriority
   * @param inMoveType
   * @return
   * @throws DBException
   */
  private int getPriorityMoves(String isDevice, int inPriority, int inMoveType)
      throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT COUNT(").append(MoveData.MOVEID_NAME)
               .append(") AS \"IPICKS\" FROM ").append(getReadTableName())
               .append(" WHERE ").append(MoveData.DEVICEID_NAME).append("=?")
               .append(" AND ").append(MoveData.PRIORITY_NAME).append("=?");

    if (inMoveType == SKDCConstants.ALL_INT)
    {
      return getIntegerColumn("IPICKS", vpSql.toString(), isDevice, inPriority);
    }
    else
    {
      vpSql.append(" AND ").append(MoveData.MOVETYPE_NAME).append("=?");
      
      return getIntegerColumn("IPICKS", isDevice, inPriority, inMoveType);
    }
  }
}
