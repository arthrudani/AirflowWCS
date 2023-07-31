package com.daifukuamerica.wrxj.allocator;

import com.daifukuamerica.wrxj.allocator.shortorder.ShortOrderAllocationException;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import java.util.List;

/**
 * Allocation strategy for Full Load allocation on a split system with large
 * load orders where the loads are distributed across multiple aisles in a way
 * that multiple JVMs will allocate the order.  <b>If your Load orders are all
 * one line, then the standard {@link FullLoadOut FullLoadOut} allocator will
 * work as is.</b>
 *
 * @author A.D.
 * @since  17-Mar-2009
 */
public class SplitSystemLoadAllocation extends FullLoadOut
{
  public SplitSystemLoadAllocation()
  {
    super();
  }

  @Override
  public List<AllocationMessageDataFormat> allocate() throws DBException,
                                                             AllocationException
  {
    mpAllocatedDataList.clear();
    TransactionToken vpTok = null;
    try
    {
      vpTok = mpDBObj.startTransaction();
/*===========================================================================
   If we made it to here we can try to allocate everything on the OL list
   provided order status hasn't changed, or the order hasn't been deleted by
   Short order configuration.
  ===========================================================================*/
      if (mpOrderServ.orderHeaderExists(msOrder) && 
          mpAllocServer.anyLoadOrderLinesToAllocate(msOrder))
      {
        mpOLList = mpAllocServer.getOrderLinesUnderThisJVM(msOrder);
        processOrderLines();
        mpShortOrderProcess.setNextOrderStateWithNotification(msOrder, mpOHD.getDestinationStation());
      }
      mpDBObj.commitTransaction(vpTok);
    }
    catch(ShortOrderAllocationException ae)
    {
      mpDBObj.endTransaction(vpTok);
      mpAllocatedDataList.clear();
      mpShortOrderProcess.auxiliaryOrderHandling(msOrder);
    }
    catch(DBException exc)
    {
      mpDBObj.endTransaction(vpTok);
      mpAllocServer.revertOrderStatus(mpOHD.getOrderID(), "", iOriginalOrderStatus);
      throw exc;
    }
    catch(AllocationException mesg)
    {
      mpDBObj.endTransaction(vpTok);
      String vsOrdMsg = "Application Error in allocator. Exception of type " +
                         mesg.getClass().getCanonicalName() + " caught!";

      mpAllocServer.revertOrderStatus(msOrder, vsOrdMsg, DBConstants.ORERROR);
      throw mesg;
    }
    catch(Exception exc)
    {
      mpDBObj.endTransaction(vpTok);

      String vsOrdMsg = "Application Error in allocator. Exception of type " +
                         exc.getClass().getCanonicalName() + " caught!";

      mpAllocServer.revertOrderStatus(msOrder, vsOrdMsg, DBConstants.ORERROR);
      throw new DBException("Order " + msOrder + " Not Allocated...", exc);
    }
    finally
    {
      mpDBObj.endTransaction(vpTok);
    }

    return(mpAllocatedDataList);
  }
}
