package com.daifukuamerica.wrxj.dataserver.standard;

/*
                       Daifuku America Corporation
                          International Center
                       5202 Douglas Corrigan Way
                    Salt Lake City, Utah  84116-3192
                             (801) 359-9900

   This software is furnished under a license and may be used and copied only 
   in accordance with the terms of such license.  This software or any other 
   copies thereof in any form, may not be provided or otherwise made available, 
   to any other person or company without written consent from Daifuku America 
   Corporation.

   Daifuku America Corporation assumes no responsibility for the use or 
   reliability of software which has been modified without approval.
*/

import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeader;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Description:<BR>
 *   Server to handle Order Deallocation Specific operations.  This class wraps
 *   transactions around multiple table manipulations.
 *
 * @author  SBW
 * @version 1.0
 * @since   23-Feb-03
 */
public class StandardDeallocationServer extends StandardServer
{
  private final OrderLineData mpOLData = Factory.create(OrderLineData.class);
  private final MoveData mpMVData = Factory.create(MoveData.class);
  protected StandardInventoryServer        mpInvServer      = null;
  protected StandardLoadServer             mpLoadServer     = null;
  protected StandardMaintenanceOrderServer mpMaintOrdServer = null;
  protected StandardMoveServer             mpMoveServer     = null;
  protected StandardOrderServer            mpOrderServer    = null;

  /**
   * Constructor
   */
  public StandardDeallocationServer()
  {
    this(null);
  }

  /**
   * Constructor
   * 
   * @param isKeyName
   */
  public StandardDeallocationServer(String isKeyName)
  {
    super(isKeyName);
  }
  
  /**
   * Web application constructor for per user connection pooling
   * @param keyName
   * @param dbo
   */
  public StandardDeallocationServer(String keyName, DBObject dbo)
  {
	  super(keyName, dbo); 
  }


  /**
   * Method to disconnect from the database and cleanup.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    if (mpInvServer != null)
    {
      mpInvServer.cleanUp();
      mpInvServer = null;
    }
    if (mpLoadServer != null)
    {
      mpLoadServer.cleanUp();
      mpLoadServer = null;
    }
    if (mpMaintOrdServer != null)
    {
      mpMaintOrdServer.cleanUp();
      mpMaintOrdServer = null;
    }
    if (mpMoveServer != null)
    {
      mpMoveServer.cleanUp();
      mpMoveServer = null;
    }
    if (mpOrderServer != null)
    {
      mpOrderServer.cleanUp();
      mpOrderServer = null;
    }

  }

  /**
   * Method (deallocateIDMoves) to deallocate inventory etc for
   * an Adjusted Item Detail occurrence.
   */
  public void deallocateIDMoves(String sLoadID, String sItem,
              String sLot, double deAllocateQty) throws DBException
  {
    initializeMoveServer();
    
    KeyObject[] kobj = new KeyObject[3];
    MoveData moveData = Factory.create(MoveData.class);
    double totalcqty;
    double curalcqty;
    TransactionToken tt = null;

    // Set up column for requesting moves for the load ID
    kobj[0] = new KeyObject(MoveData.LOADID_NAME, sLoadID);
    kobj[1] = new KeyObject(MoveData.ITEM_NAME, sItem);
    kobj[2] = new KeyObject(MoveData.PICKLOT_NAME, sLot);

    List<Map> moveMovesList;
    try
    {
      // Get a list of moves for the item detail
      moveMovesList = mpMoveServer.getMoveDataList(kobj);
      
      // If the list size is zero that means we have may just have a load move
      // so look for just the load
      if (moveMovesList.size() <= 0)
      {
        kobj = new KeyObject[] { new KeyObject(MoveData.LOADID_NAME, sLoadID) };
        moveMovesList = mpMoveServer.getMoveDataList(kobj);
        
        // If the list size is still zero that means we can't find moves for
        // this load so just return.
        if (moveMovesList.size() <= 0)
        {
          return;
        }
      }
    }
    catch (DBException dbe)
    {
      logException("Error getting Move list for load in "
          + getClass().getSimpleName() + ".deallocateIDMoves()", dbe);
      throw dbe;
    }

    for(int i = 0; i < moveMovesList.size(); i++)
    {
      totalcqty = 0.0;
      curalcqty = 0.0;

      if(totalcqty < deAllocateQty)
      {       
        moveData.dataToSKDCData(moveMovesList.get(i));
        if (moveData.getMoveType() == DBConstants.EMPTYMOVE)
        {
            continue;
        }
        logDebug(getClass().getSimpleName() + ".deallocateIDMoves() - Start");
        try
        {
          tt = startTransaction();
          if (moveData.getMoveType() == DBConstants.LOADMOVE)    
          {
                    // Being a Load Move we don't have Item and Lot, so
                    // find them by reading item detail on load
                    //
                    // NOTE:  WE ASSUME only one item/lot detail per load!!!
                    //
                    // If that changes, we need to change this area.
                    // Then decrement by the allocated qty in the item detail
            initializeInventoryServer();
            List<Map> idarray =
              mpInvServer.getLoadLineItemDataListByLoadID(moveData.getLoadID());

            if (idarray.size() < 1)
            {
              throw new DBException("Item detail record not found for load: "
                  + moveData.getLoadID() + "\n::::: "
                  + getClass().getSimpleName() + ".deallocatedIDMoves() :::::");
            }
            LoadLineItemData currItemDet = Factory.create(LoadLineItemData.class);
            currItemDet.dataToSKDCData(idarray.get(0));

            curalcqty = currItemDet.getAllocatedQuantity();
          }
          else
          {
            curalcqty = moveData.getPickQuantity();
          }

          backOffInventoryForMove(moveData, true);
          commitTransaction(tt);
          totalcqty += curalcqty;
        }
        catch (DBException e)
        {
          logException(e, "Exception backing off move for load \""
              + moveData.getLoadID() + "\"  - " + getClass().getSimpleName()
              + ".deallocateIDMoves");
        }
        finally
        {
          endTransaction(tt);
        }
        logDebug(getClass().getSimpleName() + ".deallocateIDMoves() - End");
      } // end if totalcqty < dealocqty
      else
      {
        break;
      }
    } // End For
  }

  /**
   * Method (deallocateOneMove) to deallocate inventory etc for one specific
   *  Move.
   */
  public void deallocateOneMove(int iMoveID)throws DBException
  {
    initializeMoveServer();
    
    KeyObject[] kobj = new KeyObject[1];
    MoveData moveData = Factory.create(MoveData.class);
    TransactionToken tt = null;

                // Set up column for requesting moves for the moveid
    kobj[0] = new KeyObject(MoveData.MOVEID_NAME, Integer.valueOf(iMoveID));
    List<Map> moveMovesList = new ArrayList<Map>();

    try
    {
                // Get a list of moves for the load
      moveMovesList = mpMoveServer.getMoveDataList(kobj);
    }
    catch(DBException be)
    {
      logError( "Error getting Move list for load in DeallocationServer-->deallocateOneMove()");
      logException(be, "In DeallocationServer-->deallocatedOneMove()");
      throw new DBException(be.getMessage());
    }

    for(int i = 0; i < moveMovesList.size(); i++)
    {
      moveData.dataToSKDCData(moveMovesList.get(i));
      logDebug("DeallocationServer.deallocateOneMove() - Start");
      try
      {
          tt = startTransaction();
          backOffInventoryForMove(moveData, true);
          commitTransaction(tt);
      }
      catch (DBException e)
      {
          logException(e, "Exception backing off move for load \"" +
                             moveData.getLoadID() +
                             "\"  - DeallocationServer.deallocateOneMove");
      }
      finally
      {
        endTransaction(tt);
      }
      logDebug("DeallocationServer.deallocateOneMove() - End");

    }
  }

 /**
  * Method to deallocate item details that are to be returned to the rack when
  * an operator decides not to pick remaining items on a load. This method
  * allows order quantites to be adjusted down by the amount that was not
  * picked; this keeps the allocator from trying to reallocate these items for
  * this order.
  * 
  * @param isOrderID The order being picked.
  * @param isLoadID The load that is at the pick station.
  * @param izAdjustOrderQty if <code>true</code> the order quantity is adjusted,
  *        down by the deallocated amount. <code>false</code> means the order
  *        will be reallocated.
  * @throws DBException if there is a Database access or update error.
  */
  public void deallocPickStationLoad(String isOrderID, String isLoadID,
                                     boolean izAdjustOrderQty) throws DBException
  {
    TransactionToken vpTok = null;
    try
    {
      initializeInventoryServer();
      initializeMoveServer();
      initializeOrderServer();
      
      vpTok = startTransaction();
                                       // Get all the moves for this order on
                                       // this load.
      mpMVData.clear();
      mpMVData.setKey(MoveData.ORDERID_NAME, isOrderID);
      mpMVData.setKey(MoveData.LOADID_NAME, isLoadID);
      List<Map> vpMoveList = mpMoveServer.getMoveDataList(mpMVData.getKeyArray());
      
      for(Map vpMoveMap : vpMoveList)
      {
        mpMVData.dataToSKDCData(vpMoveMap);

        String vsItem = mpMVData.getItem();
        String vsPickLot = mpMVData.getPickLot();
        String vsOrderLot = mpMVData.getOrderLot();
        String vsLineID = mpMVData.getLineID();
        String vsPosition = mpMVData.getPositionID();
        double vdPickQty = mpMVData.getPickQuantity();
        
                                       // Back-off Item detail by pick amount
        mpInvServer.deallocateInventory(isLoadID, vsItem, vsPickLot, vsLineID,
                                        vsPosition, vdPickQty);
        
                                       // Get rid of the move.
        mpMoveServer.deleteMove(mpMVData.getMoveID());
        
                                       // Find the correct order line to update.
        mpOLData.clear();
        mpOLData.setKey(OrderLineData.ORDERID_NAME, isOrderID);
        mpOLData.setKey(OrderLineData.ITEM_NAME, vsItem);
        mpOLData.setKey(OrderLineData.ORDERLOT_NAME, vsOrderLot);
        mpOLData.setKey(OrderLineData.LINEID_NAME, vsLineID);
        OrderLineData vpOLData = mpOrderServer.getOrderLineRecord(mpOLData, true);

                                       // If allowed, adjust the ordered quantity
                                       // to prevent reallocation.  In this case
                                       // delete order line if ordqty = pickqty.
        boolean vzLineDeleted = false;
        if (izAdjustOrderQty)
        {
          if (vpOLData.getOrderQuantity() == vdPickQty)
          {
            mpOrderServer.deleteOrderLine(isOrderID, vsItem, vsOrderLot, vsLineID);
            vzLineDeleted = true;
          }
          else
          {
            double vdOrderQty = vpOLData.getOrderQuantity() - vdPickQty;
            mpOLData.setOrderQuantity(vdOrderQty);
            
            tnData.clear();
            tnData.setTranCategory(DBConstants.INVENTORY_TRAN);
            tnData.setTranType(DBConstants.MODIFY_ORDER_LINE);
            tnData.setOrderID(isOrderID);
            tnData.setItem(vsItem);
            tnData.setLot(vsOrderLot);
            tnData.setLineID(vsLineID);
            tnData.setCurrentQuantity(vpOLData.getOrderQuantity());
            tnData.setAdjustedQuantity(-vdPickQty);
            logTransaction(tnData);
          }
        }
        
        if (!vzLineDeleted)            // Back-off Order Line alloc. qty. by 
        {                              // pick amount.
          mpOLData.setAllocatedQuantity(vpOLData.getAllocatedQuantity() - vdPickQty);
          mpOrderServer.modifyOrderLine(mpOLData);
        }
      }
                                       // Finally, if all this made the order
                                       // complete, delete it.
      if (mpOrderServer.orderIsComplete(isOrderID))
      {
        mpOrderServer.deleteOrder(isOrderID);
      }
      commitTransaction(vpTok);
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

  /**
  * Method (deallocateMovesForLoad)to deallocate all Moves for a specific
  *  Load.
  */
  public void deallocateMovesForLoad(String sLoadID) throws DBException
  {
    initializeMoveServer();
    
    KeyObject[] kobj = new KeyObject[1];
    TransactionToken tt = null;

                // Set up column for requesting moves for the load
    kobj[0] = new KeyObject(MoveData.LOADID_NAME, sLoadID);

                // Get a list of moves for the load
    List<Map> loadMovesList = mpMoveServer.getMoveDataList(kobj);

    for(int i = 0; i < loadMovesList.size(); i++)
    {
      MoveData moveDataFor1 = Factory.create(MoveData.class);
      moveDataFor1.dataToSKDCData(loadMovesList.get(i));
      logDebug("DeallocationServer.deallocateMovesForLoad() - Start");
      try
      {
          tt = startTransaction();
          backOffInventoryForMove(moveDataFor1, true);
          commitTransaction(tt);
      }
      catch (DBException e)
      {
        logException(e, "Exception backing off move for load \"" +
                     moveDataFor1.getLoadID() +
                     "\"  - DeallocationServer.deallocateMovesForLoad");
      }
      finally
      {
        endTransaction(tt);
      }
      logDebug("DeallocationServer.deallocateMovesForLoad() - End");
    }
  }

  /**
   * Method (deallocateMovesForDevice)to deallocate all Moves for a specific
   *  Device.
   *
   *  @param sDeviceID Device needing deallocation.
   *  @exception DBException
   */
  public void deallocateMovesForDevice(String sDeviceID) throws DBException
  {
    initializeMoveServer();
    
    TransactionToken tt = null;

    List<Map> deviceMovesList = mpMoveServer.getMovesByLoadDevice(sDeviceID);

    for(int i = 0; i < deviceMovesList.size(); i++)
    {
        MoveData moveData = Factory.create(MoveData.class);
        moveData.dataToSKDCData(deviceMovesList.get(i));
        logDebug("DeallocationServer.deallocateMovesForDevice() - Start");
        try
        {
            tt = startTransaction();
            backOffInventoryForMove(moveData, true);
            commitTransaction(tt);
        }
        catch (DBException e)
        {
            logException(e, "Exception backing off move for load \"" +
                               moveData.getLoadID() +
                               "\"  - DeallocationServer.deallocateMovesForDevice");
        }
        finally
        {
          endTransaction(tt);
        }
        logDebug("DeallocationServer.deallocateMovesForDevice() - End");

    }
  }

 /**
  *  Method to decrement all necessary data related to a Move/OrderLine Allocation.<BR><BR>
  *  <p> This method first finds UNASSIGNED moves for the order/item/lot
  *  specified and decrements the order allocated quantity and item detail
  *  allocated quantity by the move pick quantity and then deletes the move(s).
  *
  *  @param ipMoveData moves to back off.
  *  @param izIgnoreMoveStatus <code>boolean</code> deallocate regardless of
  *         move status.
  */
  public void backOffInventoryForMove(MoveData ipMoveData, boolean izIgnoreMoveStatus)
         throws DBException
  {
    TransactionToken tt = null;

    initializeInventoryServer();
    initializeLoadServer();
    initializeOrderServer();
    
    String vsOrderID = ipMoveData.getOrderID();
    String vsLoadID  = ipMoveData.getLoadID();
    
    tt = startTransaction();

    try
    {
      // Lock the Order Header first!
      OrderHeader vpOH = Factory.create(OrderHeader.class);
      OrderHeaderData vpOHData = Factory.create(OrderHeaderData.class);
      vpOHData.setKey(OrderHeaderData.ORDERID_NAME, vsOrderID);
      vpOHData = vpOH.getElement(vpOHData, DBConstants.WRITELOCK);

                   // Lock the Load record.
      Load load = Factory.create(Load.class);
      LoadData loadData = load.getLoadData(vsLoadID,
                                           DBConstants.WRITELOCK);
      if (loadData == null)
      {
         throw new DBException("Load Record for Load " + vsLoadID +
                              " not found!");
      }

        /*  if we don't care if it is done, check reasons why it shouldn't
         ** be done, .... but we we want it done REGARDLESS - don't check for
         ** reasons not to
         */
      if (izIgnoreMoveStatus == false)
      {
                                         // Leave move alone if it's been ASSIGNED.
        if (ipMoveData.getMoveStatus() == DBConstants.ASSIGNED)
        {
          logError("Cannot Deallocate move for load: " + vsLoadID +
                    "  because it is ASSIGNED");
          throw new DBException("Cannot Deallocate move for load: " +
              vsLoadID +  "  because it is ASSIGNED");
        }

        if (loadData.getLoadMoveStatus() != DBConstants.NOMOVE &&
            loadData.getLoadMoveStatus() != DBConstants.RETRIEVEPENDING)
        {
          logError("Cannot Deallocate move for load: " + vsLoadID +
                   "  because it is MOVING");
          throw new DBException("Cannot Deallocate move for load: " +
              vsLoadID +  "  because it is MOVING");
        }
      }
      else
      {
        if (loadData.getLoadMoveStatus() == DBConstants.RETRIEVEPENDING)
        {
          loadData.setLoadMoveStatus(DBConstants.NOMOVE);
          loadData.setNextAddress("");
          loadData.setNextWarehouse("");
          mpLoadServer.updateLoadData(loadData, true);       
        }
      }

      switch(ipMoveData.getMoveType())
      {
        case DBConstants.LOADMOVE:
          /*
           *  Being a Load Move we don't have Item and Lot, so find them by 
           *  reading item detail on load.
           */
          List<Map> idarray =
                          mpInvServer.getLoadLineItemDataListByLoadID(vsLoadID);

          for(Iterator<Map> it = idarray.iterator(); it.hasNext();)
          {
            LoadLineItemData currItemDet = Factory.create(LoadLineItemData.class);
            currItemDet.dataToSKDCData(it.next());

            double decQty = currItemDet.getAllocatedQuantity();
            if (vsOrderID.equals(""))
            {
              vsOrderID = currItemDet.getOrderID();
            }

                // Decrement the allocated qty in the item detail
            mpInvServer.deallocateInventory(vsLoadID, currItemDet.getItem(),
                                            currItemDet.getLot(), 
                                            currItemDet.getLineID(), 
                                            currItemDet.getPositionID(),decQty);
          }
              // there may not be any inventory on the load
              // if so the order line item will have a allocated qty = 1
              // Decrement the order line accordingly
          mpOrderServer.backoffOrderLineQty(vsOrderID, vsLoadID, null, null, null, 1.0);
          break;
          
        case DBConstants.EMPTYMOVE:
              // there may not be any inventory on the load
              // if so the order line item will have a allocated qty = 1
              // Decrement the order line accordingly
              // Don't pass in the load id because the order line does not have one...it just
              // allocated what it found...there should only be one line item for this order
              // because it is an "Request Empties" order.
          mpOrderServer.backoffOrderLineQty(vsOrderID, null, null, null, null, 1.0);
          break;
          
        case DBConstants.CYCLECOUNTMOVE:
        case DBConstants.REPLENISHMENTMOVE:
          break;

        default:
          try
          {
                          // Because this is an item move,
                          // Decrement the allocated qty in the item detail
                          // by the pick qty in the move being changed
            mpInvServer.deallocateInventory(vsLoadID, ipMoveData.getItem(), 
                                                ipMoveData.getPickLot(), 
                                                ipMoveData.getLineID(),
                                                ipMoveData.getPositionID(),
                                                ipMoveData.getPickQuantity());
          }
          catch(DBException db)
          {
            logException(db.getMessage(), db);
          }
          try
          {

                          // Decrement the order line allocated qty
            mpOrderServer.backoffOrderLineQty(vsOrderID, vsLoadID, ipMoveData.getItem(),
                                            ipMoveData.getOrderLot(), ipMoveData.getLineID(),
                                            ipMoveData.getPickQuantity());
          }
          catch(DBException db)
          {
            logException(db.getMessage(), db);
          }
      }

      try
      {
           // Delete the move
        ipMoveData.setKey(MoveData.MOVEID_NAME, Integer.valueOf(ipMoveData.getMoveID()));

        initializeMoveServer();
        mpMoveServer.deleteMove(ipMoveData);

                                       // For Cycle-Counts, just delete the order
                                       // if there are no more moves.
        if (ipMoveData.getMoveType() == DBConstants.CYCLECOUNTMOVE ||
            ipMoveData.getMoveType() == DBConstants.REPLENISHMENTMOVE)
        {
          initializeMaintenanceServer();
          mpMaintOrdServer.deleteOrder(vsOrderID);
        }
        else if(vsOrderID != null && !vsOrderID.equals(""))
        {
          // Now update the order to Realloc (reallocate)
          // Do we want to do this for every move?
          // ...is there a way to do it once per order even if we are just
          // going by moves right now?
          //
          // If this is a FULLLOADOUT order, we cannot determine if we should delete it here
          // or not because here we are just deleting inventory for one move...where the load
          // is deleted is where we need to delete the order...standard inventory server.
          
          
                                 // Allow Reallocate only under certain conditions.
          int vnCurrentOrderStatus = mpOrderServer.getOrderStatusValue(vsOrderID);
          if (vnCurrentOrderStatus == DBConstants.SHORT     ||
              vnCurrentOrderStatus == DBConstants.SCHEDULED || 
              vnCurrentOrderStatus == DBConstants.READY     ||
              vnCurrentOrderStatus == DBConstants.ORERROR   ||
              vnCurrentOrderStatus == DBConstants.ALLOCATENOW)
            mpOrderServer.setOrderStatusValue(vsOrderID,
                                              mpOrderServer.getOrderStatusValue(vsOrderID),
                                              DBConstants.REALLOC);
        }
        commitTransaction(tt);
      }
      catch(DBException dbe)
      {
        throw(new DBException(dbe.getMessage()));
      }
    }
    catch (DBException dbe)
    {
      logError( "Error Fixing Inventory In DeallocationServer-->backOffInventory()");
      logException(dbe, "In DeallocationServer-->backOffInventoryForMove()");
      throw new DBException(dbe.getMessage());
    }
    finally
    {
      endTransaction(tt);
    }
  }
  
  /**
   *  Method to deallocate load line item.
   *
   *  @param loadID Load ID to check.
   *  @param item Item number.
   *  @param lot Lot number.
   *  @param order Order number.
   *  @param orderLot Ordered lot number.
   *  @exception DBException
   */
  public void deallocateLoadLineItem( LoadLineItemData ipLoadLineItemData) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      String vsOrder = ipLoadLineItemData.getOrderID();
      String vsLoadID = ipLoadLineItemData.getLoadID();
      String vsItem = ipLoadLineItemData.getItem();
      String vsOrderLot = ipLoadLineItemData.getOrderLot();
      String vsLineID = ipLoadLineItemData.getLineID();
      String vsPosition = ipLoadLineItemData.getPositionID();
      String vsLot = ipLoadLineItemData.getLot();
      double vdAllocatedQty = ipLoadLineItemData.getAllocatedQuantity();
      
      // if order is not blank then adjust order line record
      if ((vsOrder != null) && (vsOrder.length() > 0))
      {
        initializeOrderServer();
        mpOrderServer.backoffOrderLineQtyAfterPick(vsOrder, vsLoadID, vsItem,
            vsOrderLot, vsLineID, vdAllocatedQty);
      }

      // adjust the item detail record
      initializeInventoryServer();
      mpInvServer.deallocateInventory(vsLoadID, vsItem, vsLot, vsLineID,
                                      vsPosition, vdAllocatedQty);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }
  
  /*========================================================================*/
  /*  The following initialize other servers if/when we need them.  This    */
  /*  is cheaper than both constantly creating/destroying them and          */
  /*  needlessly creating them when we create the server.                   */
  /*========================================================================*/
  protected void initializeInventoryServer()
  {
    if (mpInvServer == null)
    {
      mpInvServer = Factory.create(StandardInventoryServer.class, getClass().getSimpleName());
    }
  }

  protected void initializeLoadServer()
  {
    if (mpLoadServer == null)
    {
      mpLoadServer = Factory.create(StandardLoadServer.class, getClass().getSimpleName());
    }
  }

  protected void initializeMaintenanceServer()
  {
    if (mpMaintOrdServer == null)
    {
      mpMaintOrdServer = Factory.create(StandardMaintenanceOrderServer.class, getClass().getSimpleName());
    }
  }

  protected void initializeMoveServer()
  {
    if (mpMoveServer == null)
    {
      mpMoveServer = Factory.create(StandardMoveServer.class, getClass().getSimpleName());
    }
  }

  protected void initializeOrderServer()
  {
    if (mpOrderServer == null)
    {
      mpOrderServer = Factory.create(StandardOrderServer.class, getClass().getSimpleName());
    }
  }
}
