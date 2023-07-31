package com.daifukuamerica.wrxj.dataserver.standard;

/**
 * Title:        Route Server
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Daifuku America Corporation
 * @author
 * @version 1.0
 */

import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLine;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.Route;
import com.daifukuamerica.wrxj.dbadapter.data.RouteData;
import com.daifukuamerica.wrxj.dbadapter.data.TableJoin;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.UnusedMethod;
import java.util.List;
import java.util.Map;

public class StandardRouteServer extends StandardServer
{
  protected Route mpRoute = Factory.create(Route.class);
  private final TableJoin mpTabJoin = Factory.create(TableJoin.class);
  private final Load      mpLoad    = Factory.create(Load.class);

  /**
   * Constructor
   */
  public StandardRouteServer()
  {
    this(null);
  }

  /**
   * Constructor
   *
   * @param isKeyName
   */
  public StandardRouteServer(String isKeyName)
  {
    super(isKeyName);
  }
  
  /**
   * Web application constructor for per user connection pooling
   * @param keyName
   * @param dbo
   */
  public StandardRouteServer(String keyName, DBObject dbo)
  {
	  super(keyName, dbo); 
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/

  /**
   * Add a route
   *
   * @param ipRouteData
   * @return
   * @throws DBException
   */
  public String addRoute(RouteData ipRouteData) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      mpRoute.addElement(ipRouteData);

      tnData.clear();
      tnData.setTranCategory(DBConstants.SYSTEM_TRAN);
      tnData.setTranType(DBConstants.ADD);
      tnData.setRouteID(ipRouteData.getRouteID());
      String vsDescription = "From: " + ipRouteData.getFromID() +
                             " To: " + ipRouteData.getDestID();
      tnData.setActionDescription(vsDescription);
      logTransaction(tnData);

      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }

    return "Route added successfully.";
  }


  /**
   * Modifies a route provided no loads are already using it. The reasoning for
   * this is as follows: if any load is using a route and then their next
   * location field has already been updated based on the route's station links.
   * In this case we don't want to update the route station fields at least
   * because such a modification will possibly invalidate the next location or
   * final location fields of the load.
   *
   * @param ipKeyData
   * @param ipNewData
   * @return String
   * @throws DBException
   */
  public String modifyRoute(RouteData ipKeyData, RouteData ipNewData)
      throws DBException
  {
    // Get the Route data before the change
    RouteData vpOldRouteData = getRouteRecord(ipKeyData);

    LoadData lddata = null;
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      lddata = Factory.create(LoadData.class);
      lddata.setKey(RouteData.ROUTEID_NAME, ipKeyData.getRouteID());
      if (Factory.create(Load.class).getCount(lddata) > 0)
      {
        throw new DBException("There are loads on this route still!");
      }
      // Log changes made
      logModifyTransaction(vpOldRouteData, ipNewData);

      // Update data
      ipNewData.setKey(RouteData.ROUTEID_NAME, ipKeyData.getRouteID());
      ipNewData.setKey(RouteData.FROMID_NAME,  ipKeyData.getFromID());
      ipNewData.setKey(RouteData.DESTID_NAME,  ipKeyData.getDestID());
      mpRoute.modifyElement(ipNewData);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }

    return "Route modified successfully";
  }


  /**
   * Deletes an entry for a given route and current station.
   *
   * @param routeID
   * @param currentStation
   * @param toStation
   * @throws DBException
   */
  public void deleteRoute(String routeID, String currentStation,
      String toStation) throws DBException
  {
    LoadData      lddata = null;
    OrderLineData oldata = null;
    TransactionToken tt = null;

    RouteData rtData = Factory.create(RouteData.class);
    rtData.setRouteID(routeID);
    rtData.setKey(RouteData.ROUTEID_NAME, routeID);
    rtData.setFromID(currentStation);
    rtData.setKey(RouteData.FROMID_NAME, currentStation);
    rtData.setDestID(toStation);
    rtData.setKey(RouteData.DESTID_NAME, toStation);

    try
    {
      tt = startTransaction();
      lddata = Factory.create(LoadData.class);
      lddata.setKey(LoadData.ROUTEID_NAME, rtData.getRouteID());
      if (Factory.create(Load.class).getCount(lddata) > 0)
      {
        throw new DBException("There are still loads on this route!");
      }

      oldata = Factory.create(OrderLineData.class);
      oldata.setKey(OrderLineData.ROUTEID_NAME, rtData.getRouteID());
      if (Factory.create(OrderLine.class).getCount(oldata) > 0)
      {
        throw new DBException("There are Order Lines\nstill using this Route!");
      }

      rtData.setKey(RouteData.ROUTEID_NAME, rtData.getRouteID());
      mpRoute.deleteElement(rtData);

      tnData.clear();
      tnData.setTranCategory(DBConstants.SYSTEM_TRAN);
      tnData.setTranType(DBConstants.DELETE);
      tnData.setRouteID(rtData.getRouteID());
      String vsDescription = "From: " + rtData.getFromID() +
                             " To: " + rtData.getDestID();
      tnData.setActionDescription(vsDescription);
      logTransaction(tnData);

      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
      if (oldata != null)
      {
        oldata.clear();
        oldata = null;
      }
      lddata = null;
    }
  }

 /**
  * Method finds a valid output route for a load to be cycle-counted.
  * @param isLoadID the Load to cycle count.
  * @return Route/station that allows Cycle Counts and has least number of
  *         enroute loads.
  * @throws DBException if there is a DB access error.
  */
  public String getCCIRoute(String isLoadID) throws DBException
  {
    boolean  vzFirstTime = true;
    String[] vasRoutes = mpTabJoin.getCCIRoutes(isLoadID);
    int vnMinEnrCount = 0;
    String vsBestRoute;

    if (vasRoutes.length > 0)
    {
      vsBestRoute = vasRoutes[0];
      for(String vsRoute : vasRoutes)
      {
                                       // Choose the route with the least
                                       // enroute loads.
        int vnEnrCount = mpLoad.getEnrouteCountPlusAtStation(vsRoute);
        if (vzFirstTime)
        {
          vnMinEnrCount = vnEnrCount;
          vsBestRoute = vsRoute;
          vzFirstTime = false;
        }
        else if (vnMinEnrCount > vnEnrCount)
        {
          vnMinEnrCount = vnEnrCount;
          vsBestRoute = vsRoute;
        }
      }
    }
    else
    {
      vsBestRoute = "";
      logError("No best route found for CCI load " + isLoadID);
    }

    return(vsBestRoute);
  }

  /**
   *  Gets a set of routes based on criteria given in rtdata.
   */
  public List<Map> getRouteData(RouteData rtdata) throws DBException
  {
    return mpRoute.getAllElements(rtdata);
  }


  /**
   *  Retrieves one route record.
   */
  public RouteData getRouteRecord(RouteData rtdata) throws DBException
  {
    return mpRoute.getElement(rtdata, DBConstants.NOWRITELOCK);
  }


  /**
   * Get a list of route names
   * @param routeID
   * @return
   * @throws DBException
   */
  public List<String> getRouteNameList(String routeID) throws DBException
  {
    return mpRoute.getRouteNameList(routeID);
  }


  /**
   * Get route names (Simulation)
   *
   * @param isRouteID
   * @param inFromType
   * @param inDestType
   * @return
   * @throws DBException
   */
  public List<String> getRouteNameList(String isRouteID, int inFromType,
      int inDestType) throws DBException
  {
    return mpRoute.getRouteNameList(isRouteID, inFromType, inDestType);
  }


  /**
   * Does this route exist?
   *
   * @param ipRouteData
   * @return
   * @throws DBException
   */
  @UnusedMethod
  public boolean exists(RouteData ipRouteData) throws DBException
  {
    return mpRoute.exists(ipRouteData);
  }


  /**
   *  Method checks for the existence of a route.
   *  @param isRouteID <code>String</code> containing the route identifier.
   *
   *  @return <code>boolean</code> of true if the route exists. false otherwise.
   */
  public boolean exists(String isRouteID)
  {
    RouteData vpRouteData = Factory.create(RouteData.class);
    vpRouteData.setKey(RouteData.ROUTEID_NAME, isRouteID);
    return mpRoute.exists(vpRouteData);
  }

  /**
   * Get a route that connects the source and destination locations
   *
   * @param isFromWarehouse
   * @param isFromAddress
   * @param isDestWarehouse
   * @param isDestAddress
   * @return <code>String</route> with a route name if one exists, null otherwise
   * @throws DBException
   */
  public String getFromToRoute(String isFromWarehouse, String isFromAddress,
      String isDestWarehouse, String isDestAddress) throws DBException
  {
    StandardLocationServer vpLocServer =
      Factory.create(StandardLocationServer.class, "StandardRouteServer");

    //Get LocationData
    LocationData vpFromLocationData = vpLocServer.getLocationRecord(isFromWarehouse, isFromAddress);
    LocationData vpDestLocationData = vpLocServer.getLocationRecord(isDestWarehouse, isDestAddress);

    if (vpFromLocationData == null)
    {
      throw new DBException("Invalid From Location [" + isFromWarehouse + "-"
          + isFromAddress + "]!");
    }
    if (vpDestLocationData == null)
    {
      throw new DBException("Invalid Destination Location [" + isDestWarehouse
          + "-" + isDestAddress + "]!");
    }

    return mpRoute.getRouteFromTo(vpFromLocationData, vpDestLocationData);
  }


  /**
   * Method to get the next station on a route.
   *
   * <p><b>Details:</b> This will return one RouteData per RouteID and FromID
   * combination.  The route must be ON in order to be found.</p>
   *
   * @param isRouteID The Route ID.
   * @param isFromID From Location.
   * @return The Next Route Data.
   * @throws DBException
   *
   * @author cstrong
   */
  public RouteData getNextRouteData(String isRouteID, String isFromID) throws DBException
  {
    RouteData rtdataSearch = Factory.create(RouteData.class);
    RouteData mpRouteData = null;

    rtdataSearch.setKey(RouteData.ROUTEID_NAME, isRouteID);
    rtdataSearch.setKey(RouteData.FROMID_NAME, isFromID);
    rtdataSearch.setKey(RouteData.ROUTEONOFF_NAME, Integer.valueOf(DBConstants.ON));
    List<Map> tmplist = mpRoute.getAllElements(rtdataSearch);

    /*
     * If the list has nothing in it now, do nothing, we couldn't find a next
     * address.
     *
     * If the list has multiple entries, return the first one.
     * TODO: Smarter decision making
     */
    if (tmplist != null && tmplist.size() > 0)
    {
      mpRouteData = Factory.create(RouteData.class);
      mpRouteData.dataToSKDCData(tmplist.get(0));
    }
    return mpRouteData;
  }


  /**
   * Get the next destination for a route.
   * TODO: Handle parallel route segments better
   */
  public String getNextRouteDest(String isRouteID, String isFromID) throws DBException
  {
    RouteData vpRD = getNextRouteData(isRouteID, isFromID);
    if (vpRD == null)
      return "";
    else
      return vpRD.getDestID();
  }

  /**
   * Method to get the names of routes that have segments <i>originating</i> from passed in
   * station.
   * @param isFromStation the originating station for any segment of a route.
   *
   * @return String array containing route names.  Empty array if no routes found.
   * @throws DBException if these is a DB access error.
   */
  public String[] getRoutesFromStation(String isFromStation) throws DBException
  {
    RouteData vpRTData = Factory.create(RouteData.class);

    vpRTData.setKey(RouteData.FROMID_NAME, isFromStation);
    return(mpRoute.getSingleColumnValues(RouteData.ROUTEID_NAME, true, vpRTData, SKDCConstants.NO_PREPENDER));
  }
  /**
   * Get a list of next stations for a given device and final destination
   *
   * @param isDestination
   * @param isDeviceID
   * @return
   * @throws DBException
   */
  public List<String> getFirstStationsList(String isDestination,
      String isDeviceID) throws DBException
  {
    return mpRoute.getFirstStationsList(isDestination, isDeviceID);
  }


  /**
   * Format the string used to describe a route on a GUI
   */
  public String describeRouteSegment(String isRouteID, String isFrom, String isTo)
  {
    return "Route " + isRouteID + " (from" + isFrom + " to " + isTo + ")";
  }


  /**
   * Ensure that the entire route from source to destination is active and
   * actually connects.
   *
   * @param isRoute
   * @param isSource
   * @param isDest
   * @return
   * @throws DBException
   */
  public boolean checkPath(String isRoute, String isSource, String isDest)
      throws DBException
  {
    return mpRoute.checkPath(isRoute, isSource, isDest, 0);
  }

  /**
   *  Log a Modify Transaction History record
   *
   *  @param ipOldData <code>String</code> containing data of old Route record.
   *  @param ipNewData <code>String</code> containing data of new Route record.
   *
   *  @throws <code>DBException</code> if a database add error.
   */
  private void logModifyTransaction(RouteData ipOldData, RouteData ipNewData) throws DBException
  {
    // We only log the transaction if there is any change
    if (ipNewData.getColumnCount() > 0)
    {
      tnData.clear();
      tnData.setTranCategory(DBConstants.SYSTEM_TRAN);
      tnData.setTranType(DBConstants.MODIFY);
      tnData.setRouteID(ipNewData.getRouteID());
      if (logDataChanged(ipOldData, ipNewData) == true)
      {
        logTransaction(tnData);
      }
    }
  }
}
