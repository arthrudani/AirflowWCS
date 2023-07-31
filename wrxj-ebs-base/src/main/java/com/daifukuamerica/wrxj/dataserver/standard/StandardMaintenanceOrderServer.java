package com.daifukuamerica.wrxj.dataserver.standard;

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

import com.daifukuamerica.wrxj.allocator.AllocationMessageDataFormat;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.Move;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeader;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLine;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.TableJoin;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;

import java.util.List;
import java.util.Map;

public class StandardMaintenanceOrderServer extends StandardOrderServer
{
  private final OrderHeader mpOH = Factory.create(OrderHeader.class);
  private final OrderLine mpOL = Factory.create(OrderLine.class);
  private final OrderLineData mpOLData = Factory.create(OrderLineData.class);
  private final TableJoin mpTJ = Factory.create(TableJoin.class);
  
  public StandardMaintenanceOrderServer()
  {
    this(null);
  }

  public StandardMaintenanceOrderServer(String keyName)
  {
    super(keyName);
    logDebug("StandardMaintenanceOrderServer.createMaintenanceOrderServer()");
  }
  
  /**
   * Web application constructor for per user connection pooling
   * @param keyName
   * @param dbo
   */
  public StandardMaintenanceOrderServer(String keyName, DBObject dbo)
  {
	  super(keyName, dbo); 
	  logDebug("StandardMaintenanceOrderServer.createMaintenanceOrderServer()");
  }

  /**
   *  Gets a Order line record using unique key.
   */
  @Override
  public OrderLineData getOrderLineRecord(OrderLineData olData,
                                          boolean izWithLock) throws DBException
  {
    OrderLine ol = Factory.create(OrderLine.class);
    int vnLock = (izWithLock) ? DBConstants.WRITELOCK : DBConstants.NOWRITELOCK;
    return(ol.getMaintenanceOrderLineData(olData, vnLock));
  }

  /**
   *  Builds the whole order into the database based on Order Header and
   *  array of Order Line information.  This method publishes an Order Event to
   *  the allocator if the order status is ALLOCATENOW.
   *
   *  @param isOHData <code>OrderHeaderData</code> containing Order Header
   *         info.
   *  @param iapOLData array of <code>OrderLineData</code> containing all order
   *         line info.
   *  @return <code>String</code> value of success of failure message.
   * @throws DBException  if there is adatabase add or acccess error.
   */
  @Override
  public String buildOrder(OrderHeaderData isOHData, OrderLineData[] iapOLData)
         throws DBException
  {
    if (iapOLData.length > 1)
    {
      throw new DBException("Only one order line allowed for Maintenance Orders.");
    }

    String vsRtnMesg = "Order Added Successfully!";
    String vsOrderID = OrderHeader.generateOrderID(isOHData.getOrderType());
    TransactionToken vpTok = null;
    
    try
    {
      vpTok = startTransaction();
      isOHData.setOrderID(vsOrderID);
      mpOH.addElement(isOHData);

      try
      {
        iapOLData[0].setOrderID(vsOrderID);
        mpOL.addMaintenanceOrderLine(iapOLData[0], isOHData.getOrderType());
        commitTransaction(vpTok);
      }
      catch (DBException exc)
      {
        logException(exc, "buildOrder");
        vsRtnMesg = "Not all Order lines added!";
        throw exc;
      }
    }
    catch(DBException exc)
    {
      logException(exc,
        "Adding Item Maintenance Order-->StandardMaintenanceOrder.buildOrder()");
      throw exc;
    }
    finally
    {
      endTransaction(vpTok);
    }

    if (isOHData.getOrderStatus() == DBConstants.ALLOCATENOW ||
        isOHData.getOrderStatus() == DBConstants.READY)
    {                                  // Send message to allocator.
      notifyAllocator(iapOLData[0]);
    }

    return(vsRtnMesg);
  }

  /**
   * {@inheritDoc}
   * @param isOrderID order to allocate.
   * @throws DBException if there is a database access error.
   */
  @Override
  public void allocateOrder(String isOrderID) throws DBException
  {
//    int vnOrderType = getOrderTypeValue(isOrderID);
    List<Map> vpList = getOrderLineData(isOrderID);
    if (!vpList.isEmpty())
    {
      mpOLData.dataToSKDCData(vpList.get(0));
      notifyAllocator(mpOLData);
    }
  }
  
  /**
   * Method adds Order Line to the database. <B>Not supported for replenishment
   * or cycle-count orders.</B>
   * 
   * @throws <code>DBException</code> if this method is called!.
   */
  @Override
  public String addOrderLine(OrderLineData oldata, boolean checkItemMaster)
      throws DBException
  {
    throw new DBException("Operation not supported for Maintenance Orders");
  }

  /**
   * Deletes a Maintenance Order and related data out of the Maintenance Order
   * table. this method fails if there are any ASSIGNED Maintenance Order moves.
   */
  @Override
  public int deleteOrder(String sOrderID) throws DBException
  {
    Move move = Factory.create(Move.class);
    MoveData mvdata = Factory.create(MoveData.class);
    mvdata.setKey(MoveData.ORDERID_NAME, sOrderID);
    mvdata.setKey(MoveData.MOVESTATUS_NAME, Integer.valueOf(DBConstants.ASSIGNED));
    if (move.getCount(mvdata) > 0)
    {
      throw new DBException("Maintenance Order " + sOrderID +
                            " has\nASSIGNED moves and may not\nbe deleted!");
    }

    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
                                       // Delete the Moves.
      mvdata.clear();
      mvdata.setKey(MoveData.ORDERID_NAME, sOrderID);
      if (move.getCount(mvdata) > 0)
      {
        move.deleteElement(mvdata);
      }
      executeDeletion(sOrderID);
      commitTransaction(tt);
    }
    catch (DBException exc)
    {
      throw new DBException("Trying to delete MaintenanceOrder: " +
                            exc.getMessage());
    }
    finally
    {
      endTransaction(tt);
    }

    return(DBConstants.DONE);
  }

  /**
   * Method modifies an Order Line. <B>Not supported for replenishment or
   * cycle-count orders.</B>
   * 
   * @throws <code>DBException</code> if this method is called!.
   */
  @Override
  public String modifyOrderLine(OrderLineData oldata, boolean izCheckOrderStatus)
         throws DBException
  {
    throw new DBException("Operation not supported for Maintenance Orders");
  }

 /**
  *  Method deletes an Order Line.  <B>Not supported for replenishment or
  *  cycle-count orders.  The whole Order must be deleted.</B>
  *  @throws <code>DBException</code> if this method is called!.
  */
  @Override
  public void deleteOrderLine(String sOrderID, String sLineId) throws DBException
  {
    throw new DBException("Operation not supported for Maintenance Orders");
  }

 /**
  *  Puts a Maintenance Order on hold after validating the requested
  *  transaction.
  */
  @Override
  public void holdOrder(String mntOrder) throws DBException
  {
    switch (getOrderStatusValue(mntOrder))
    {
      case DBConstants.HOLD :
        break;

      case DBConstants.READY :
      case DBConstants.ALLOCATENOW :
      case DBConstants.SCHEDULED :
        backOffMaintenanceOrderMoves(mntOrder);
        TransactionToken tt = null;
        try
        {
          tt = startTransaction();
          setOrderStatusValue(mntOrder, DBConstants.HOLD);
          commitTransaction(tt);
        }
        finally
        {
          endTransaction(tt);
        }
        break;

      default :
        throw new DBException("Maintenance Order is in wrong state to be HELD.");
    }
  }

 /**
  *  Method deletes a maintenance order move.  If this is the last move for the
  *  order, it deletes the order as well.
  *
  *  @param moveID <code>int</code> containing unique move id. of the move to be
  *         deleted.
  */
  public void deleteMaintenanceOrderMove(int moveID) throws DBException
  {
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      Move move = Factory.create(Move.class);
      String sOrderID = move.getMoveOrderID(moveID);
      move.deleteByMoveID(moveID);
                                         // Now see if any more moves exist.
      MoveData mvdata = Factory.create(MoveData.class);
      mvdata.setKey(MoveData.ORDERID_NAME, sOrderID);
      if (!move.exists(mvdata)) deleteOrder(sOrderID);
      commitTransaction(vpTok);
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

/*===========================================================================
                          PRIVATE METHODS SECTION
  ===========================================================================*/
 /**
  *  Worker method for deleting Maintenance orders.
  *
  *  @param sOrderID <code>String</code> containing Maintenance Order id.
  */
  private void executeDeletion(String sOrderID) throws DBException
  {
                                       // Delete the Maintenance Order
    OrderLine ol = Factory.create(OrderLine.class);
    OrderHeader oh = Factory.create(OrderHeader.class);
    OrderHeaderData ohData = Factory.create(OrderHeaderData.class);
    OrderLineData olData = Factory.create(OrderLineData.class);
    olData.setKey(OrderLineData.ORDERID_NAME, sOrderID);
    ol.deleteElement(olData);
    ohData.setKey(OrderHeaderData.ORDERID_NAME, sOrderID);
    oh.deleteElement(ohData);
                                       // Log the transaction.
    tnData.clear();
    tnData.setTranCategory(DBConstants.ORDER_TRAN);
    tnData.setTranType(DBConstants.DELETE_ORDER);
    tnData.setOrderID(sOrderID);
    tnData.setOrderType(getOrderTypeValue(sOrderID));
    tnData.setToStation("");
    logTransaction(tnData);
  }

  private void backOffMaintenanceOrderMoves(String orderID) throws DBException
  {
    Move move = Factory.create(Move.class);
    MoveData mvData = Factory.create(MoveData.class);
    mvData.setKey(MoveData.ORDERID_NAME, orderID);
    mvData.setKey(MoveData.MOVESTATUS_NAME, Integer.valueOf(DBConstants.AVAILABLE));
    List<Map> mntMoveList = move.getAllElements(mvData);

    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      for (int k = 0; k < mntMoveList.size(); k++)
      {
        mvData.clear();
        mvData.dataToSKDCData(mntMoveList.get(k));
                                       // Get the Load and make sure it can be
                                       // made stationary.
        Load theLoad = Factory.create(Load.class);
        LoadData lddata = theLoad.getLoadData(mvData.getLoadID(), DBConstants.WRITELOCK);
        if (lddata == null)
        {
          throw new DBException("Load Record for Load " + mvData.getLoadID() +
                                " not found!");
        }
        else if (lddata.getLoadMoveStatus() == DBConstants.NOMOVE ||
                 lddata.getLoadMoveStatus() == DBConstants.RETRIEVEPENDING)
        {
          changeMaintOrderLoadStatus(lddata.getLoadID(), DBConstants.NOMOVE);
                                       // Make sure it's still AVAILABLE and
                                       // delete it.
          int moveID = mvData.getMoveID();
          mvData.clear();
          mvData.setKey(MoveData.MOVEID_NAME, Integer.valueOf(moveID));
          MoveData currentMove = move.getElement(mvData, DBConstants.WRITELOCK);
          if (currentMove != null &&
              currentMove.getMoveStatus() == DBConstants.AVAILABLE)
          {
            move.deleteByMoveID(moveID);
          }
        }
      } // End for-loop
      commitTransaction(tt);
    }
    catch (DBException exc)
    {
      throw exc;
    }
    finally
    {
      endTransaction(tt);
    }
  }

 /**
  *  Method to modify move status of a load.  This method should be called from
  *  within a transaction.
  *
  * @param loadID <code>String</code> containing load ID.
  * @param newStatus <code>int</code> containing status to change to.
  */
  private void changeMaintOrderLoadStatus(String loadID, int newStatus)
          throws DBException
  {
    LoadData lddata = Factory.create(LoadData.class);
    lddata.setKey(LoadData.LOADID_NAME, loadID);
    lddata.setNextWarehouse("");
    lddata.setFinalWarehouse("");
    lddata.setNextAddress("");
    lddata.setFinalAddress("");
    lddata.setLoadMoveStatus(newStatus);
    Factory.create(Load.class).modifyElement(lddata);
  }
  
 /**
  * Method to notify all eligable allocators about a Cycle-Count order.
  * @param ipOLData
  * @throws DBException if there is a database access error.
  */
  private void notifyAllocator(final OrderLineData ipOLData) throws DBException
  {
    String[] vasAllocators;
    
    if (!ipOLData.getItem().isEmpty())
    {
      vasAllocators = mpTJ.getAllocatorChoices(ipOLData.getItem());
    }
    else if (!ipOLData.getBeginAddress().isEmpty())
    {
      vasAllocators = mpTJ.getAllocatorChoices(ipOLData.getBeginWarehouse(), 
                                               ipOLData.getBeginAddress(), 
                                               ipOLData.getEndingAddress());
    }
    else
    {
      vasAllocators = new String[0];
    }
    
    if (vasAllocators.length > 0)
    {
      for(int vnIdx = 0; vnIdx < vasAllocators.length; vnIdx++)
      {                               // If someday we have multiple allocators!
        getSystemGateway().publishOrderEvent(ipOLData.getOrderID(),
                                   AllocationMessageDataFormat.CYCLECOUNT_ORDER,
                                   vasAllocators[vnIdx]);
      }
    }
  }
}