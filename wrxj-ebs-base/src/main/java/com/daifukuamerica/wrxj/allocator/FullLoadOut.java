package com.daifukuamerica.wrxj.allocator;

import com.daifukuamerica.wrxj.allocator.shortorder.ShortOrderAllocationException;
import com.daifukuamerica.wrxj.dataserver.standard.StandardAllocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.ParameterNameConstants;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Description:<BR>
 *  Class to do "Full Load Out".  This class will build moves to bring out a
 *  mpLoad.  Note:  the GUI screen already checks to see if the device this mpLoad
 *  belongs to is INOPERABLE.
 *
 * @author       A.D.
 * @version      1.0   07/29/02
 * @version      2.0   11/18/04   Moved over to AllocationServer calls.
 */
public class FullLoadOut extends AbstractAllocationStrategy implements AllocationStrategy
{
  protected   String                 msFirstOutputStn;
  protected   String                 msLoadLocn;
  protected   String                 msRouteID;
  protected   boolean                mzConventionalStn = false;
  protected   StandardOrderServer    mpOrderServ;
  protected   StandardDeviceServer   mpDeviceServ; 
  protected   StandardStationServer  mpStationServ;
  protected   StandardLoadServer     mpLoadServ;
  protected   DBObject               mpDBObj;
  protected   List<Map>              mpOLList;
  protected   List<AllocationMessageDataFormat> mpAllocatedDataList;
  protected   OrderLineData          mpOLData;
  protected   Load                   mpLoad;  

 /**
  *  Default constructor for Load allocation strategy.
  */
  public FullLoadOut()
  {
    super();
                                       // ***** Data server objects.  ******
    mpAllocServer = Factory.create(StandardAllocationServer.class, "FullLoadOut");
    mpOrderServ = Factory.create(StandardOrderServer.class, "FullLoadOut");
    mpDeviceServ = Factory.create(StandardDeviceServer.class, "FullLoadOut");
    mpStationServ = Factory.create(StandardStationServer.class, "FullLoadOut");
    mpLoadServ = Factory.create(StandardLoadServer.class, "FullLoadOut");
    
                                       // ***** DBAdapter objects.  ******
    mpOLData = Factory.create(OrderLineData.class);
    mpLoad = Factory.create(Load.class);    
    
    mpDBObj = new DBObjectTL().getDBObject();
    mpAllocatedDataList = new ArrayList<AllocationMessageDataFormat>();
  }

  /**
   *  {@inheritDoc}
   */
  @Override
  public void setOutputStation(StationData ipOutputStation)
  {
    super.setOutputStation(ipOutputStation);
    mzConventionalStn = mpStationServ.isStationConventional(ipOutputStation.getStationName());
  }
  
 /**
  *  Method to find all loads given in an order.  This method can handle
  *  multiple lines on an order that are all mpLoad-out requests.  For an
  *  automated system however, the orders will always be one line mpLoad
  *  requests.  It is assumed that the OrderHeader destination is filled in
  *  (in other words, all loads on the order have one destination and one
  *  route to get there).
  *
  * @return <code>List</code> of AllocatedData class.
  */
  public List<AllocationMessageDataFormat> allocate() 
         throws DBException, AllocationException
  {
    mpAllocatedDataList.clear();
    mpOLList = mpAllocServer.loadOrderPreallocation(mpOHD);
    TransactionToken vpTranTok = null;
    try
    {
      vpTranTok = mpDBObj.startTransaction();
/*===========================================================================
   If we made it to here we can try to allocate everything on the OL list
   provided order status hasn't changed, or the order hasn't been deleted by
   Short order configuration.
  ===========================================================================*/
      if (mpOrderServ.orderHeaderExists(msOrder) &&
          mpOrderServ.getOrderStatusValue(msOrder) == DBConstants.ALLOCATING)
      {
        processOrderLines();
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
   * Process the order lines
   * 
   * @throws DBException
   * @throws AllocationException
   */
  protected void processOrderLines() throws DBException, AllocationException
  {
    int listLength = mpOLList.size();
    for(int row = 0; row < listLength; row++)
    {
      Map currObj = mpOLList.get(row);
      mpOLData.clear();
      mpOLData.dataToSKDCData(currObj);
                                       // Load is considered to be in the rack
                                       // properly if it is a NOMOVE mpLoad.
      String vsMessage = mpAllocServer.isOrderedLoadInRack(mpOLData.getLoadID(),
          mpOLData.getOrderID(), false);
      if (vsMessage == null)
      {
                                       // Get route info. for this mpLoad.
        Map<String, String> hMap = mpAllocServer.getLoadOutputStation(
            mpOLData.getLoadID(), mpOutStation.getStationName(),
            mpOHD.getDestinationStation(), mnAisle);
        
        msFirstOutputStn = hMap.get(ParameterNameConstants.STATIONNAME);
        msLoadLocn = hMap.get(ParameterNameConstants.LOCATION);
        msRouteID = hMap.get(ParameterNameConstants.ROUTEID);
        
        processMove();
        mpAllocServer.updateOrderLine(msOrder, mpOLData.getLoadID());
      }
      else
      {
        if (mzAllocationDiagnostics)
        {
          mpProbe.addProbeDetails(getClass().getSimpleName()
              + ".processOrderLines", vsMessage);
        }
        mpOrderServ.setOrderLineShort(msOrder, mpOLData.getLoadID());
      }
    } // End for-loop
    
    mpAllocServer.checkOrderForShortage(msOrder);
  }
  
  protected void processMove() throws DBException, AllocationException
  {
    String vsDevice = mpLoadServ.getLoadDeviceID(mpOLData.getLoadID());
    if (mpDeviceServ.isStationDeviceInoperable(msFirstOutputStn) || 
        mpDeviceServ.isDeviceInoperable(vsDevice))
    {
      String ordLoad = "Order '" + msOrder + "' and Load '" + 
                       mpOLData.getLoadID() + "'";
      throw new AllocationException("Station's Device or Load Device is not " + 
                                    "operational! Allocation not performed " +
                                    "for " + ordLoad, AllocationException.DEVICE_INOP);
    }
      
    mpAllocServer.buildLoadMove(mpOLData.getLoadID(), msRouteID, vsDevice, mpOHD);
    mpAllocServer.buildReturnData(mpOLData.getLoadID(), msFirstOutputStn,
                                  mpAllocatedDataList);
  }
}
