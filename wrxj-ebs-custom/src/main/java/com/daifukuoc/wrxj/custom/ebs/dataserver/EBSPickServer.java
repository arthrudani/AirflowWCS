package com.daifukuoc.wrxj.custom.ebs.dataserver;

import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPickServer;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItem;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.Move;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLine;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;

public class EBSPickServer extends StandardPickServer
{

	  protected StandardInventoryServer   mpInvServer     = null;
	  
	// Public Methods for Pick Server
	  public EBSPickServer()
	  {
	    super();
	  }

	  public  EBSPickServer(String keyName)
	  {
	    super(keyName);
	  }
	  
	
	  /**
	   *  Method to adjust the order line item for this item move. After the
	   *  pick, the order line item will be marked Shy if the amount of the pick
	   *  quantity is equal to the allocated quantity and the allocated quantity
	   *  is less than the order quantity. If the pick quantity equals the allocated
	   *  quantity then we will recheck the order status.
	   *
	   *  @param moveData Move data object to be completed.
	   *  @param qtyPicked Quantity that was picked.
	   *  @exception DBException
	   */
	   protected String adjustOrderLine(MoveData moveData, double qtyPicked) throws DBException
	   {
	     OrderLine orderLine = Factory.create(OrderLine.class);
	     OrderLineData oldataSearch = Factory.create(OrderLineData.class);
	     // get the order line so we increment the qty
	     // MCM, EBS
	     // The move item may not be the orderline item, if they have ordered by F# or lot
	     // There should only be 1 move per load so we can update by orderID only at EBS
	     oldataSearch.setKey(OrderLineData.ORDERID_NAME, moveData.getOrderID());
	     //oldataSearch.setKey(OrderLineData.ITEM_NAME, moveData.getItem());
	     //oldataSearch.setKey(OrderLineData.ORDERLOT_NAME, moveData.getOrderLot());
	     oldataSearch.setKey(OrderLineData.LINEID_NAME, moveData.getLineID());
	     OrderLineData oldata = orderLine.getElement(oldataSearch, DBConstants.WRITELOCK);
	     oldata.setPickQuantity(oldata.getPickQuantity() + qtyPicked);
	     if (oldata.getAllocatedQuantity() < oldata.getOrderQuantity())
	     {
	       oldata.setLineShy(DBConstants.YES);
	     }
	     else
	     {
	       oldata.setLineShy(DBConstants.NO);
	     }
	     oldata.setKey(OrderLineData.ORDERID_NAME, oldata.getOrderID());
	     oldata.setKey(OrderLineData.ITEM_NAME, oldata.getItem());
	     oldata.setKey(OrderLineData.ORDERLOT_NAME, oldata.getOrderLot());
	     oldata.setKey(OrderLineData.LINEID_NAME, oldata.getLineID());
	     orderLine.modifyElement(oldata);
	     checkOrderHeader(oldata.getOrderID());

	     return (oldata.getLineID());
	   }
	   
	   /**
	    * Method to do some final checks before deleting Auto-Pick Orders.
	    * @param ipOHData Order header data.
	    * @throws DBException if there is a DB error.
	    */
	    protected void deleteAutoPickOrderIfNecessary(OrderHeaderData ipOHData)
	              throws DBException
	    {
	      initializeOrderServer();
	      String vsOrderId = ipOHData.getOrderID();
	      int vnOrdStat = mpOrderServer.getOrderStatusValue(vsOrderId);

	      // MCM, EBS 
	      // Delete short orders once all retrieves are complete
	      if ((vnOrdStat == DBConstants.KILLED ||vnOrdStat == DBConstants.SCHEDULED ||
	           vnOrdStat == DBConstants.DONE || vnOrdStat == DBConstants.REALLOC ) && !mpOrderServer.orderHasMoves(vsOrderId))
	      {
	        mpOrderServer.executeDeletion(ipOHData);
	        if (mzHasHostSystem)
	        {                                // Generate an order completion message.
	          initializeHostServer();
	          mpHostServ.sendOrderComplete(ipOHData);
	        }
	      }
	    }
	   
	   
	  
	  protected void initializeInventoryServer()
	  {
	    if (mpInvServer == null)
	    {
	      mpInvServer = Factory.create(StandardInventoryServer.class);
	    }
	  }
}
