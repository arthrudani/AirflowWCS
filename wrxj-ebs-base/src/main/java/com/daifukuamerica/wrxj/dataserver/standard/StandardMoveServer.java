package com.daifukuamerica.wrxj.dataserver.standard;

import com.daifukuamerica.wrxj.dbadapter.data.Move;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Title:       StandardMoveServer
 * Description: Transaction-handling class for moves
 * Copyright:   Copyright (c) 2001-2008
 * Company:     Daifuku America Corporation
 */
public class StandardMoveServer extends StandardServer
{
  protected Move mpMove = Factory.create(Move.class);
   
  /**
   * Constructor
   */
  public StandardMoveServer()
  {
    this(null);
  }

  /**
   * Constructor
   */
  public StandardMoveServer(String isKeyName)
  {
    super(isKeyName);
  }
  
  /**
   * Web application constructor for per user connection pooling
   * @param keyName
   * @param dbo
   */
  public StandardMoveServer(String keyName, DBObject dbo)
  {
	  super(keyName, dbo); 
	  logDebug("StandardOrderServer.createOrderServer()");
  }

 /**
  * deletes a move record using the move ID.
  * @param inMoveID the unique move id.
  * @throws DBException if there is a DB access error.
  */
  public void deleteMove(int inMoveID) throws DBException
  {
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      mpMove.deleteByMoveID(inMoveID);
      commitTransaction(vpTok);
    }
    finally
    {
      endTransaction(vpTok);
    }
  }
  
  /**
   * Delete a move
   * 
   * @param ipMoveKey
   * @throws DBException
   */
  public void deleteMove(MoveData ipMoveKey) throws DBException
  {
    // Begin Transaction
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      // At least make sure the id key is set
      if(ipMoveKey.getKeyObject(MoveData.MOVEID_NAME) == null && ipMoveKey.getMoveID() != 0)
      {
        ipMoveKey.setKey(MoveData.MOVEID_NAME, ipMoveKey.getMoveID());
      }
      mpMove.deleteElement(ipMoveKey);
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException(e, "Exception Deleting Move" + " - MoveServer.deleteMove");
      throw e;
    }
    catch (NoSuchElementException e)
    {
      logException(e, "Exception Deleting Move" + " - No Rows Deleted");
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Add a move
   * 
   * @param ipMoveData
   * @throws DBException
   */
  public void addMove(MoveData ipMoveData) throws DBException
  {
    // Begin Transaction
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      mpMove.addElement(ipMoveData);
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException(e, "Exception Adding Move" + " - MoveServer.addMove");
      throw e;
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Get a move record without a write lock
   * 
   * @param ipMoveKey
   * @return
   * @throws DBException
   */
  public MoveData getMoveRecord(MoveData ipMoveKey) throws DBException
  {
    return getMoveRecord(ipMoveKey, false);
  }

 /**
  * Gets unique move record based on specified key.
  * @param ipMoveKey the criteria to find unique move record.
  * @param izWithLock <code>true</code> indicates read with lock. <b>Note:</b>
  *        <u>This method must be called from within a transaction if this flag
  *        is set to <code>true</code>.</u>
  * @return null if no records are found.
  * @throws DBException If a unique record can't be found based on specified
  *         key (but something was still found).  Also if DB access errors.
  */
  public MoveData getMoveRecord(MoveData ipMoveKey, boolean izWithLock)
         throws DBException
  {
    return mpMove.getElement(ipMoveKey, (izWithLock) ? DBConstants.WRITELOCK 
                                                     : DBConstants.NOWRITELOCK);
  }

  /**
   * Get the number of moves matching the key data
   * 
   * @param ipMoveKey
   * @return
   * @throws DBException
   */
  public int getMoveCount(MoveData ipMoveKey) throws DBException
  {
    return mpMove.getCount(ipMoveKey);
  }
 
  /**
   * Method to get move count by a combination of key values of Order, Load, and
   * Item.
   * 
   * @param isOrderID the order id on the move. This may be passed as empty
   *            string.
   * @param isLoad the Pick-From load for item moves or the Load being moved for
   *            Load Moves. This may be passed as empty string.
   * @param isItem the item ID on an item move. This may be passed as empty
   *            string.
   * @return the number of moves matching passed in key values.
   */
  public int getMoveCount(String isOrderID, String isLoad, String isItem)
         throws DBException
  {
    MoveData vpMoveData = Factory.create(MoveData.class);
    if (isOrderID.trim().length() > 0)
    {
      vpMoveData.setKey(MoveData.ORDERID_NAME, isOrderID);
    }
    
    if (isLoad.trim().length() > 0)
    {
      vpMoveData.setKey(MoveData.LOADID_NAME, isLoad);
    }

    if (isItem.trim().length() > 0)
    {
      vpMoveData.setKey(MoveData.ITEM_NAME, isItem);
    }
    
    return getMoveCount(vpMoveData);
  }
  
  /**
   *  Method to get data objects for matching moves.
   *
   *  @param isLoadID Load ID to search for.
   *  @return List of <code>MoveData</code> objects.
   *  @exception DBException
   */
  public List<Map> getMoveDataList(String isLoadID) throws DBException
  {
    KeyObject[] kobj = new KeyObject[1];

    kobj[0] = new KeyObject(MoveData.LOADID_NAME, isLoadID);
    return mpMove.getMoveDataList(kobj);
  }

  /**
   * Retrieves a list of Move records that contain loadIDs that have the Device
   * ID that is passed in ...used to identify what needs to be deallocated when
   * a device is marked inoperable.
   * 
   * @param isDeviceID <code>String</code> object.
   * 
   * @return reference to an List of Move objects containing null reference if
   *         no Moves found.
   * @exception DBException
   */
  public List<Map> getMovesByLoadDevice(String isDeviceID) throws DBException
  {
    // This Call Requires a Table Join.
    return mpMove.getMovesByLoadDevice(isDeviceID);
  }

 /**
  * Get a list of moves using normal move data class.
  * @param ipMVKeyInfo the move data class containing key info.
  * @return List of Moves.
  * @throws DBException
  */
  public List<Map> getMoveDataList(MoveData ipMVKeyInfo) throws DBException
  {
    return(mpMove.getAllElements(ipMVKeyInfo));
  }

  /**
   *  Method to get data objects for matching moves using ColumnObject.
   *
   *  @return List of <code>MoveData</code> objects.
   *  @exception DBException
   */
  public List<Map> getMoveDataList(KeyObject[] iapMovesKeys) throws DBException
  {
    // Read without the warehouse/Address ...just move data
    return mpMove.getMoveDataList(iapMovesKeys);
  }

  /**
   * Method to get the next move to be processed for a load.
   * 
   * @param isLoadID Load ID to check.
   * @param inMoveType Specifies what type of move to look for. 0 = ALL
   * @return MoveData object containing Move info. matching our search criteria.
   * @exception DBException
   */
  public MoveData getNextMoveRecord(String isLoadID, int inMoveType)
      throws DBException
  {
    MoveData vpMoveKey = Factory.create(MoveData.class);
    vpMoveKey.setKey(MoveData.PARENTLOAD_NAME, isLoadID);
    if (inMoveType != 0)
    {
      vpMoveKey.setKey(MoveData.MOVETYPE_NAME, Integer.valueOf(inMoveType));
    }
    vpMoveKey.addOrderByColumn(MoveData.PRIORITY_NAME);
    vpMoveKey.addOrderByColumn(MoveData.MOVEDATE_NAME);

    List<Map> vpMoveList = mpMove.getAllElements(vpMoveKey);
    if (!vpMoveList.isEmpty())
    {
      MoveData m = Factory.create(MoveData.class);
      m.dataToSKDCData(vpMoveList.get(0));
      return (m);
    }
    return null;
  }

  /**
   * Method to get the next move to be processed for a load.
   * 
   * @param isLoadID Load ID to check.
   * @return MoveData object containing Move info. matching our search criteria.
   * @exception DBException
   */
  public MoveData getNextMoveRecord(String isLoadID) throws DBException
  {
    return getNextMoveRecord(isLoadID, 0);
  }

  /**
   * Method to get a matching empty container move on a load. Tries to match by
   * load, item and lot, then by load and item only, then by load only. An item
   * that is stored on a load could complete any of the above mentioned empty
   * container requests.
   * 
   * @param isLoadID Load ID to check.
   * @param isItem Item number.
   * @param isLot Lot number.
   * @return MoveData object containing Move info. matching our search criteria.
   * @exception DBException
   */
  public MoveData getEmptyContainerMove(String isLoadID, String isItem,
      String isLot) throws DBException
  {
    MoveData mvdataSearch = Factory.create(MoveData.class);
    mvdataSearch.setKey(MoveData.PARENTLOAD_NAME, isLoadID);
    mvdataSearch.setKey(MoveData.ITEM_NAME, isItem);
    mvdataSearch.setKey(MoveData.ORDERLOT_NAME, isLot);
    mvdataSearch.setKey(MoveData.MOVETYPE_NAME, DBConstants.EMPTYMOVE);
    MoveData mvdata = mpMove.getElement(mvdataSearch, DBConstants.NOWRITELOCK);
    if (mvdata != null)
      return (mvdata);
    
    mvdataSearch.deleteKeyObject(MoveData.ORDERLOT_NAME);
    mvdata = mpMove.getElement(mvdataSearch, DBConstants.NOWRITELOCK);
    if (mvdata != null)
      return (mvdata);
    
    mvdataSearch.deleteKeyObject(MoveData.ITEM_NAME);
    mvdata = mpMove.getElement(mvdataSearch, DBConstants.NOWRITELOCK);
    return (mvdata);
  }

 /**
  * Method to check if a move record exists for a particular load id.
  * @param isLoadID the load id.
  * @return <code>true</code> if move exists.
  */
  public boolean moveExists(String isLoadID)
  {
    MoveData vpMVData = Factory.create(MoveData.class);
    vpMVData.setKey(MoveData.LOADID_NAME, isLoadID);

    return(mpMove.exists(vpMVData));
  }

  /**
   * Modify a move
   *  
   * @param ipMoveData
   * @throws DBException
   */
  public void modifyMove(MoveData ipMoveData) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      mpMove.modifyElement(ipMoveData);
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException(e, "Exception Modifying Move" + " - MoveServer.modifyMove");
      throw e;
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Get all moves for a load
   * 
   * @param isLoadID
   * @return
   * @throws DBException
   */
  public List <Map>getMovesByLoadID(String isLoadID) throws DBException
  {
      return mpMove.getMovesByLoadID(isLoadID);
  }
  
  /**
   * Get all orders for a load
   * 
   * @param isLoadID
   * @return
   * @throws DBException
   */
  public String[] getAssociatedOrdersForLoad(String isLoadID) throws DBException
  {
    return mpMove.getAssociatedOrdersForLoad(isLoadID);
  }
}
