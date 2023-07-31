package com.daifukuamerica.wrxj.swingui.station;

/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

/**
 * A Static class containing the current default station for this copy of the UI.
 *
 * @author avt
 * @version 1.0
 */
public class LocalWorkStation
{

  private static String station = "";

 /**
  *  Create local station class.
  *
  */
  public LocalWorkStation()
  {
  }

  /**
   * Get the station.
   *
   * @return String containing default station or null if no default
   */
  public static String getStation()
  {
    if (station.trim().length() > 0)
    {
      return station.trim();
    }
    return null;
  }

  /**
   * Set the station.
   *
   * @param s String containing new default station
   *
   */
  public static void setStation(String s)
  {
    station = s.trim();
    return;
  }
}
