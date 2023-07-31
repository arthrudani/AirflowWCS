package com.daifukuamerica.wrxj.allocator;

import com.daifukuamerica.wrxj.allocator.shortorder.ShortOrderAllocationException;
import com.daifukuamerica.wrxj.dataserver.standard.StandardAllocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.ParameterNameConstants;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLine;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Description:<BR>
 *  Class to carry out Empty Container Allocation.  This class will build moves
 *  to bring out a load.
 *
 * @author       A.D.
 * @version      1.0     09/06/02
 * @version      2.0     11/18/04   Moved over to AllocationServer calls.
 */
public class EmptyContainerAllocation extends AbstractAllocationStrategy
       implements AllocationStrategy
{
  protected   String           msFirstOutputStation;
  protected   String           msLoadLocation;
  protected   String           msRouteID;
  protected   List<Map>        mpLDList = null;
  protected   LoadData         mpLDData;
  protected   StandardOrderServer   mpOrderServer;
  protected   StandardDeviceServer  mpDeviceServer;
  private     StandardLoadServer    mpLoadServer;
  protected   OrderLine        mpOrderLine;
  protected   OrderLineData    mpOLData;
  protected   List<AllocationMessageDataFormat> mpAllocatedDataList;
  private     DBObject         mpDBObj;

  /**
   * Default constructor for Empty Container allocation strategy.
   */
  public EmptyContainerAllocation()
  {
    super();
                                       /* --- DBAdapter objects. ---         */
    mpOrderLine = Factory.create(OrderLine.class);
    mpOLData = Factory.create(OrderLineData.class);
    mpLDData = Factory.create(LoadData.class);

                                       /* --- Data Server objects. ---       */
    mpAllocServer = Factory.create(StandardAllocationServer.class, "EmptyContainerAllocation");
    mpOrderServer = Factory.create(StandardOrderServer.class, "EmptyContainerAllocation");
    mpDeviceServer = Factory.create(StandardDeviceServer.class, "EmptyContainerAllocation");
    mpLoadServer = Factory.create(StandardLoadServer.class, "EmptyContainerAllocation");

    mpDBObj = new DBObjectTL().getDBObject();
    mpAllocatedDataList = new ArrayList<AllocationMessageDataFormat>();
  }

  /**
   * Method to calculate amount empty that can be brought out. For example, if
   * the order line states 4.75 as the order quantity, we find containers whose
   * amount empty sums to 4.75 or 5.00 (1/4 being the smallest unit of empty).
   * We start by looking for containers that are 1's (i.e those that are totally
   * empty).
   */
  public List<AllocationMessageDataFormat> allocate()
         throws DBException, AllocationException
  {
    mpAllocatedDataList.clear();
    mpAllocServer.reserveOrder(msOrder);

    TransactionToken vpTranTok = null;
    try
    {
      vpTranTok = mpDBObj.startTransaction();
      if (mpOrderServer.orderHeaderExists(msOrder) &&
          mpOrderServer.getOrderStatusValue(msOrder) == DBConstants.ALLOCATING)
      {
        processOrderLine();
        mpShortOrderProcess.setNextOrderStateWithNotification(msOrder, mpOHD.getDestinationStation());
      }
      mpDBObj.commitTransaction(vpTranTok);
    }
    catch(ShortOrderAllocationException ae)
    {
      mpDBObj.endTransaction(vpTranTok);
      mpAllocatedDataList.clear();
      mpShortOrderProcess.auxiliaryOrderHandling(msOrder);
    }
    catch(DBException exc)
    {
      mpDBObj.endTransaction(vpTranTok);

      mpAllocServer.revertOrderStatus(mpOHD.getOrderID(), "", iOriginalOrderStatus);
      throw exc;
    }
    catch(AllocationException mesg)
    {
      mpDBObj.endTransaction(vpTranTok);

      String vsOrdMsg = "Application Error in allocator. Exception of type " +
                         mesg.getClass().getCanonicalName() + " caught!";
      mpAllocServer.revertOrderStatus(msOrder, vsOrdMsg, DBConstants.ORERROR);
      throw mesg;
    }
    catch(Exception exc)
    {
      mpDBObj.endTransaction(vpTranTok);

      String vsOrdMsg = "Application Error in allocator. Exception of type " +
                         exc.getClass().getCanonicalName() + " caught!";
      mpAllocServer.revertOrderStatus(msOrder, vsOrdMsg, DBConstants.ORERROR);
      throw new DBException("Order " + msOrder + " Not Allocated...", exc);
    }
    finally
    {
      mpDBObj.endTransaction(vpTranTok);
    }

    return(mpAllocatedDataList);
  }

  /**
   * Method does the work of getting the order line (there will be one line per
   * order for a empty container order) and finding empty container(s) that can
   * be brought out.
   */
  protected void processOrderLine() throws DBException, AllocationException
  {
    mpOLData.clear();
    mpOLData.setKey(OrderLineData.ORDERID_NAME, mpOHD.getOrderID());
    OrderLineData vpOLData = mpOrderLine.getElement(mpOLData, DBConstants.WRITELOCK);

    if (vpOLData == null)                // Serious problem here! No order lines.
    {
      throw new DBException("No order lines found for order " +
                            mpOHD.getOrderID());
    }

    mpOLData = (OrderLineData)vpOLData.clone();
/*==========================================================================
    If it's only a fractional amount that was ordered out, see if there are
    any containers that meet that quantity closely first before trying to
    bring out a totally empty container.
  ==========================================================================*/
                                       // Get ItemMaster Storage Flag value.
    boolean withLikeItems = (mpOLData.getItem().trim().length() > 0);

    double vdOrderQty = mpOLData.getOrderQuantity();
    double vdWholeEmptyAmount = 0;
    double vdPartialEmptyAmount = 0;
                                       // Break up the ordered empty amount
    if (vdOrderQty < 1)                // into partial and whole amounts full.
    {
      vdPartialEmptyAmount = vdOrderQty;
    }
    else
    {
      vdWholeEmptyAmount = Math.floor(vdOrderQty);
      vdPartialEmptyAmount = SKDCUtility.getTruncatedDouble(vdOrderQty % vdWholeEmptyAmount);
    }

    if (vdPartialEmptyAmount > 0)
    {
      allocatePartialEmpties(vdPartialEmptyAmount, mpOLData, withLikeItems);
    }

    if (vdWholeEmptyAmount > 0)
    {
      allocateTotalEmpties((int)vdWholeEmptyAmount, mpOLData);
    }
  }

  /**
   *  Method only complete empties (no partials allowed).
   *
   *  @param inWholeEmptyAmt <code>int</code> containing the non-fractional
   *         portion of the order quantity.
   *  @param ipOLData <code>OrderLineData</code> Order Line Data.
   */
  protected void allocateTotalEmpties(int inWholeEmptyAmt, OrderLineData ipOLData)
            throws DBException, AllocationException
  {
    String vsZone = mpOutStation.getRecommendedZone();
    List<Map> vpLDList = mpAllocServer.getCompleteEmpties(vsZone, mnAisle,
        ipOLData.getContainerType(), ipOLData.getHeight(),
        mpOutStation.getStationName());
    if (vpLDList == null || vpLDList.isEmpty())
    {
      mpLogger.logDebug("No Empty Containers found for order "
          + ipOLData.getOrderID());
    }

    for(int cnt = 0; cnt < vpLDList.size() && cnt < inWholeEmptyAmt; cnt++)
    {
      mpLDData.clear();
      mpLDData.dataToSKDCData(vpLDList.get(cnt));
      fillRouteInfo(mpLDData.getLoadID());
      processMove(ipOLData, mpLDData, 1);
      updateOrderLine(ipOLData, 1);
    }
  }

 /**
  *  Method to find a empty container that most closely fits a given partial
  *  amount.
  *
  * @param idFractionalEmptyAmt <code>double</code> containing order line
  *         empty amount that is a fractional value.
  * @param ipOLData <code>OrderLineData</code> Order Line of Order.
  * @param izWithItem <code>boolean</code> Flag to indicate if container must
  *         have matching mpItem on the load.
  * @throws DBException if there is a DB error.
  * @throws AllocationException
  */
  protected void allocatePartialEmpties(double idFractionalEmptyAmt,
                                        OrderLineData ipOLData, boolean izWithItem)
            throws DBException, AllocationException
  {
    String vsZone = mpOutStation.getRecommendedZone();
    int vnAmtFullTran = mpAllocServer.amountEmptyToAmountFullTrans(idFractionalEmptyAmt);

    if (vnAmtFullTran == -1)
    {
      throw new DBException("Unknown Order fractional amount: "
          + Double.toString(idFractionalEmptyAmt));
    }

    List<Map> vpLDList = mpAllocServer.getPartialEmpties(vsZone, mnAisle, ipOLData,
                                                         vnAmtFullTran, izWithItem,
                                                         mpOutStation.getStationName());
    if (!vpLDList.isEmpty())
      processPartialEmptyData(vnAmtFullTran, idFractionalEmptyAmt, ipOLData,
                              vpLDList);
    else
      allocateTotalEmpties(1, mpOLData);
  }

  /**
   * Process partial empties for an empty container order
   *
   * @param inAmtFullTrans
   * @param idFractionalEmptyAmt
   * @param ipOLData
   * @param ipLDList
   * @throws DBException
   * @throws AllocationException
   */
  protected void processPartialEmptyData(int inAmtFullTrans, double idFractionalEmptyAmt,
                                         OrderLineData ipOLData, List<Map> ipLDList)
            throws DBException, AllocationException
  {
    int vnLoadAmtFullTrans = DBHelper.getIntegerField(ipLDList.get(0),
        LoadData.AMOUNTFULL_NAME);

    if (vnLoadAmtFullTrans <= inAmtFullTrans)
    {
      mpLDData.clear();
      mpLDData.dataToSKDCData(ipLDList.get(0));
/*---------------------------------------------------------------------------
   Even though we may have found an empty that has more space than is
   required for the partial amount, update the order line allocated and move
   quantities with exactly what was ordered.
  ---------------------------------------------------------------------------*/
                                       // Get route info. for this load.
      fillRouteInfo(mpLDData.getLoadID());
      processMove(ipOLData, mpLDData, idFractionalEmptyAmt);
      updateOrderLine(ipOLData, idFractionalEmptyAmt);
    }
  }

  /**
   * Updates the order line allocated quantity.
   *
   * @param ipOLData <code>OrderLineData</code> containing order line data.
   * @param idAllocAmt <code>double</code> containing the best fit amount that
   *          was allocated.
   */
  protected void updateOrderLine(OrderLineData ipOLData, double idAllocAmt)
            throws DBException
  {
/*===========================================================================
   Update the Order Line allocated.  If we are over-allocating however, don't
   exceed the order quantity
  ===========================================================================*/
    boolean vzMarkShort = false;
    double vdTotalAllocQty = ipOLData.getAllocatedQuantity() + idAllocAmt;

    vzMarkShort = (vdTotalAllocQty < ipOLData.getOrderQuantity());
    if (vdTotalAllocQty > ipOLData.getOrderQuantity())
    {
      vdTotalAllocQty = ipOLData.getOrderQuantity();
    }

    ipOLData.setKey(OrderLineData.ORDERID_NAME, ipOLData.getOrderID());
    ipOLData.setKey(OrderLineData.ITEM_NAME, ipOLData.getItem());
    ipOLData.setKey(OrderLineData.ORDERLOT_NAME, ipOLData.getOrderLot());
    ipOLData.setAllocatedQuantity(vdTotalAllocQty);
    ipOLData.setLineShy((vzMarkShort) ? DBConstants.YES : DBConstants.NO);

    mpOrderLine.modifyElement(ipOLData);
  }

  /**
   *  Method to build move for the Empty Container.
   * @param olData
   * @param ipLDData
   * @param emptyAmount
   * @throws DBException
   * @throws AllocationException if the device is offline.
   */
  protected void processMove(OrderLineData olData, LoadData ipLDData,
                             double emptyAmount)
            throws DBException, AllocationException
  {
    String vsDevice = mpLoadServer.getLoadDeviceID(ipLDData.getLoadID());

    if (mpDeviceServer.isStationDeviceInoperable(msFirstOutputStation) ||
        mpDeviceServer.isDeviceInoperable(vsDevice))
    {
      String vsErr = "Station's Device or Load Device is not operational! "  +
                     "Allocation not performed for Order '" + msOrder +
                     "' for load '" + mpOLData.getLoadID() + "'";
      throw new AllocationException(vsErr, AllocationException.DEVICE_INOP);
    }
    mpAllocServer.buildEmptyContainerMove(olData, ipLDData.getLoadID(),
                                          mpOHD.getPriority(), msRouteID, vsDevice,
                                          emptyAmount);
    mpAllocServer.buildReturnData(ipLDData.getLoadID(),
                                  mpOHD.getDestinationStation(),
                                  mpAllocatedDataList);
  }

  /**
   * Get the route information for a load
   *
   * @param sLoadID
   * @throws DBException
   */
  protected void fillRouteInfo(String sLoadID) throws DBException
  {
    Map<String, String> hMap = null;
    hMap = mpAllocServer.getLoadOutputStation(sLoadID,
        mpOutStation.getStationName(), mpOHD.getDestinationStation(), mnAisle);
    msFirstOutputStation = hMap.get(ParameterNameConstants.STATIONNAME);
    msLoadLocation = hMap.get(ParameterNameConstants.LOCATION);
    msRouteID = hMap.get(ParameterNameConstants.ROUTEID);
  }
}

