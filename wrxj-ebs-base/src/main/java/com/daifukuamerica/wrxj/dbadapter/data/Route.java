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

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBResultSet;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Title: Route Class
 * Description: Provides all functionality to the route object.
 * Copyright:    Copyright (c) 2002
 * Company: SKDC
 * @author : avt
 * @author : A.D.  Refactored to fit BaseDBInterface.
 * @version 1.0
 */
public class Route extends BaseDBInterface
{
  /**
   * Maximum number of hops for a route from the source to the destination.
   */
  private static final int MAX_HOPS = Application.getInt("MaxHopsOnRoute", 3);

  private RouteData    mpRouteData;
  private DBResultSet  myDBResultSet;

  /**
   * Constructor
   */
  public Route()
  {
    super("Route");
    mpRouteData = Factory.create(RouteData.class);
  }

  /**
   *  Method to get a list of matching route names.
   *
   *  @param srchRoute Route name to match.
   *  @param routeType Route type to match.
   *  @return List of route names.
   *  @exception DBException
   */
  public List<String> getRouteNameList(String isSrchRoute, int inFromType,
      int inDestType) throws DBException
  {
    List<String> routeList = new ArrayList<String>();

    myDBResultSet = execute("SELECT DISTINCT sRouteID FROM route WHERE sRouteID"
        + " like ? AND iFromType = ? AND iDestType = ? ORDER BY sRouteID",
        isSrchRoute + "%", inFromType, inDestType);
    Map row;
    while (myDBResultSet.hasNext())  // may be multiple rows
    {
      row = (Map) myDBResultSet.next();
      String nameStr = DBHelper.getStringField(row,"sRouteID");
      routeList.add(nameStr);
    }
    return(routeList);
  }


  /**
   *  Method to get a list of matching route names.
   *
   *  @param srchRoute Route name to match.
   *  @return List of route names.
   *  @exception DBException
   */
  public List<String> getRouteNameList(String srchRoute) throws DBException
  {
    List<String> routeList = new ArrayList<String>();
    myDBResultSet = execute("SELECT DISTINCT sRouteID FROM route WHERE sRouteID "
        + " LIKE ? ORDER BY sRouteID", srchRoute + "%");
    Map row;
    while (myDBResultSet.hasNext()) // may be multiple rows
    {
      row = (Map) myDBResultSet.next();
      String nameStr = DBHelper.getStringField(row, "sRouteID");
      routeList.add(nameStr);
    }
    return (routeList);
  }


  /**
   * Sets Objects for garbage collection.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpRouteData        = null;
    myDBResultSet = null;
  }


  /**
   * Get a route that exists for a given Location and Destination.
   *
   * @param ipFromLocation <code>LocationData</code> of from-location
   * @param ipDestLocation <code>LocationData</code> of to-location
   *
   * @return <code>String</code> The route or <code>null</code> if none exist
   */
  public String getRouteFromTo(LocationData ipFromLocation,
      LocationData ipDestLocation) throws DBException
  {
    StringBuffer subquery1 = new StringBuffer();

    String vsStart;
    String vsEnd;

    /*
     * Get a list of routes that have this destination
     */
    if (ipDestLocation.getLocationType() == DBConstants.LCSTATION)
    {
      vsEnd = ipDestLocation.getAddress();
    }
    else
    {
      vsEnd = ipDestLocation.getDeviceID();
    }
    mpRouteData.clear();
    mpRouteData.setKey(RouteData.DESTID_NAME, vsEnd);
    mpRouteData.setKey(RouteData.ROUTEONOFF_NAME, DBConstants.ON);

    Route vpRoute = Factory.create(Route.class);
    String[] vasRoutes = vpRoute.getSingleColumnValues(RouteData.ROUTEID_NAME, true, mpRouteData,
                                                       SKDCConstants.NO_PREPENDER);
    if (vasRoutes.length == 0)
    {
      // No routes have this destination
      return null;
    }

    for (int i = 0; i < vasRoutes.length; i++)
    {
      subquery1.append("\'"
          + vasRoutes[i] + "\'");
      if (i + 1 < vasRoutes.length)
      {
        subquery1.append(", ");
      }
    }

    /*
     * See if any of the routes we have contain the source location
     */
    if (ipFromLocation.getLocationType() == DBConstants.LCSTATION)
    {
      vsStart = ipFromLocation.getAddress();
    }
    else
    {
      vsStart = ipFromLocation.getDeviceID();
    }
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT rt.sRouteID AS \"SROUTEID\" FROM")
             .append(" Route rt WHERE rt.sFromID=?")
             .append("  AND rt.iRouteOnOff = ").append(DBConstants.ON)
             .append("  AND  rt.sRouteID IN (").append(subquery1).append(")");

    String[] vasPossibleRoutes = getList(vpSql.toString(),
                                         RouteData.ROUTEID_NAME,
                                         SKDCConstants.NO_PREPENDER, vsStart);
    if (vasPossibleRoutes.length == 0)
    {
      // No routes have this source and destination
      return null;
    }

    /*
     * Ensure that the entire route from source to destination is active and
     * actually connects
     */
    for (String vsRoute : vasPossibleRoutes)
    {
      if (checkPath(vsRoute, vsStart, vsEnd, 1))
      {
        return vsRoute;
      }
    }

    // Something didn't work out
    return null;
  }

  /**
   * Ensure that the entire route from source to destination is active and
   * actually connects.
   *
   * @param isRoute
   * @param isSource
   * @param isDest
   * @param inHops
   * @return
   * @throws DBException
   */
  public boolean checkPath(String isRoute, String isSource, String isDest,
      int inHops) throws DBException
  {
    if (isSource.equals(isDest))
    {
      return true;
    }
    if (inHops > MAX_HOPS)
    {
      return false;
    }
    mpRouteData.clear();
    mpRouteData.setKey(RouteData.ROUTEID_NAME, isRoute);
    mpRouteData.setKey(RouteData.FROMID_NAME, isSource);
    List<Map> vpSegments = getAllElements(mpRouteData);
    for (Map m : vpSegments)
    {
      mpRouteData.dataToSKDCData(m);
      if (mpRouteData.getRouteOnOff() != DBConstants.OFF)
      {
        /*
         * TODO: Check device/station statuses
         */
        if (mpRouteData.getDestID().equals(isDest))
        {
          return true;
        }
        if (checkPath(isRoute, mpRouteData.getDestID(), isDest, ++inHops))
        {
          return true;
        }
      }
    }
    return false;
  }


  /**
   * Get a list of stations that could be the next destination for a load for
   * the given device destined for the given destination.
   *
   *  <P><B>NOTE:</B> The final destination station is also the route ID.</P>
   *
   * @param isDestination
   * @param isDeviceID
   * @return
   * @throws DBException
   */
  public List<String> getFirstStationsList(String isDestination,
      String isDeviceID) throws DBException
  {
    List<String> vpFirstDestinations = new ArrayList<String>();

    mpRouteData.clear();
    mpRouteData.setKey(RouteData.ROUTEID_NAME, isDestination);
    mpRouteData.setKey(RouteData.FROMID_NAME, isDeviceID);
    mpRouteData.setKey(RouteData.ROUTEONOFF_NAME, DBConstants.ON);

    /*
     * If you want to do special weighting in the case of multiple results,
     * this is where you would put it.  As it is, they are processed in order
     * and only spill over from one to the next when enroute counts are met.
     */
    String[] vasDestinations = SKDCUtility.toStringArray(
        getAllElements(mpRouteData), RouteData.DESTID_NAME);
    for (String s : vasDestinations)
    {
      if (checkPath(isDestination, s, isDestination, 1))
      {
        vpFirstDestinations.add(s);
      }
    }

    return vpFirstDestinations;
  }


  /**
   * Get a list of routes that go through an output station.
   *
   * @param sStationName the name of the output station.
   */
  public String[] getRoutesByOutputStation(String sStationName)
      throws DBException
  {
    StringBuilder vpSql = new StringBuilder(
        "SELECT sRouteID FROM Route WHERE sDestID = ? AND iDestType = ")
        .append(DBConstants.STATION);

    return SKDCUtility.toStringArray(fetchRecords(vpSql.toString(),
        sStationName), RouteData.ROUTEID_NAME);
  }

  /**
   * Get a list of possible output stations for a given device based upon
   * existing routes.
   *
   * @param isDevice
   * @return
   * @throws DBException
   */
  public String[] getOutputStationChoicesForDevice(String isDevice)
      throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT CONCAT(sDestID, CONCAT(' - ', sDescription)) AS \"Choices\" ")
               .append("FROM Route JOIN Station ON (sDestID = sStationName) ")
               .append("WHERE sFromID = ? AND ")
               .append("iFromType = ").append(DBConstants.EQUIPMENT).append(" AND ")
               .append("iDestType = ").append(DBConstants.STATION).append(" ")
               .append("ORDER BY 1");

    return SKDCUtility.toStringArray(fetchRecords(vpSql.toString(),
        isDevice), "Choices");
  }
}
