package com.daifukuoc.wrxj.custom.ebs.dataserver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSTableJoin;
import com.daifukuamerica.wrxj.dataserver.standard.StandardAllocationServer;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.ParameterNameConstants;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;

public class EBSAllocationServer extends StandardAllocationServer
{

	 protected EBSTableJoin      mpEBSTJ = Factory.create(EBSTableJoin.class);
	 
	 public EBSAllocationServer()
	  {
	    this(null);
	  }

	  public EBSAllocationServer(String keyName)
	  {
	    super(keyName);
	    gpShortOrderProcess = Application.getString("ShortOrderProcessing");
	    logDebug("StandardAllocationServer()");
	  }
	  
	  public EBSAllocationServer(String keyName, DBObject dbo)
	  {
	    super(keyName, dbo);
	    gpShortOrderProcess = Application.getString("ShortOrderProcessing");
	    logDebug("StandardAllocationServer()");
	  }
	
	  public List<Map> getOldestItemDetails(String sItem, String sOrderLot,
              double neededAmount, Object...ipCustomObj)throws DBException
	  {
		  String sLoadId = (ipCustomObj != null && ipCustomObj.length > 0 ) ? String.valueOf(ipCustomObj[0]) : null;
		  List<Map> rtnList = null;
		  
		  rtnList = mpEBSTJ.getLoadLineItemsForThisItem(sItem, sOrderLot, sLoadId );
		  
		  return rtnList;
				  
	  }
	  
	 
	
	/**
	  *  Method finds oldest item details in the system for the item, order lot
	  *  combination. First preference is given to Dedicated Locations, and then
	  *  other locations.
	  *  @param sItem  <code>String</code> containing order line item.
	  *  @param sOrderLot  <code>String</code> containing ordered lot.
	  *  @param neededAmount  <code>double</code> value indicating amount needed to
	  *         fill the order line.
	  *  @param ipCustomObj Object containing custom data that may be used in projects.
	  * @return  <code>List</code> of oldest item detail records ordered by
	  *           Allocation priority, and scheduled date.
	  *  @throws DBException when a database error occurs.
	  */
	  public List<Map> getOldestItemDetailsForEmptyTrayStack(String sItem, String sOrderLot,
	                                        double neededAmount, Object...ipCustomObj)
	         throws DBException
	  {
	    initializeDedicationServer();

	    List<Map> rtnList = null;

	    List<Map> deviceOrderedList = null;
	    
	    // Get Ordered list of devices
	    deviceOrderedList = mpEBSTJ.getOrderedDeviceListForEmptyTrays();
	    int devListLen = deviceOrderedList.size();
        for(int dedIdx = 0; dedIdx < devListLen; dedIdx++)
        {
            String vsDeviceID = DBHelper.getStringField(deviceOrderedList.get(dedIdx),
                                                     LoadData.DEVICEID_NAME);

            List<Map> idList = mpEBSTJ.getOldestItemDetailsByAisle(vsDeviceID, sItem, sOrderLot, false, ipCustomObj);
	        if (idList.isEmpty())
	        {
	          continue;
	        }
	        else
	        {
	        	if( rtnList == null )
	        	{
	        		rtnList = idList;
	        	}
	        	else
	        	{
	        		rtnList.addAll(idList);
	        	}
	        }	
        }
	        	
	        	
	    return(rtnList);
	  }


	  public Map<String, String> getLoadOutputStation(String isLoadID,
		      String isRequestingStation, String isOrderDestStation,
		      int inDestAisleGroup) throws DBException
		  {
		    initializeLoadServer();
		    initializeLocationServer();
		    initializeRouteServer();
		    initializeStationServer();

		    String vsLoadLocation = mpLoadServer.getLoadLocation(isLoadID);
		    LocationData vpLoadLocData = mpLocServer.getLocationRecord(vsLoadLocation);
		    String vsLoadDevice = vpLoadLocData.getDeviceID();

		    /*
		     * Get the route (probably same name as sOrderDestStation)
		     */
		    String vsRoute = mpRouteServer.getFromToRoute(vpLoadLocData.getWarehouse(),
		        vpLoadLocData.getAddress(), mpStationServer.getStationWarehouse(isOrderDestStation),
		        isOrderDestStation);

		    if (vsRoute == null)
		    {
		      throw new DBException("No route from " + vpLoadLocData.getWarehouse()
		          + "-" + vpLoadLocData.getAddress() + " to " + isOrderDestStation
		          + " for Load "+isLoadID );
		    }

		    Map<String, String> hMap = new HashMap<String, String>();
		    hMap.put(ParameterNameConstants.ROUTEID, vsRoute);
		    hMap.put(ParameterNameConstants.LOCATION, vsLoadLocation);
		    hMap.put(ParameterNameConstants.STATIONNAME, isOrderDestStation); //KR:this is the destination
		    hMap.put(ParameterNameConstants.DEVICEID, vsLoadDevice);

		    return(hMap);
		  }
}
