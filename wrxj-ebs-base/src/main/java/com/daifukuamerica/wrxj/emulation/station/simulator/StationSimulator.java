package com.daifukuamerica.wrxj.emulation.station.simulator;

import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.emulation.station.SimulationController;
import com.daifukuamerica.wrxj.jdbc.DBException;

/**
 * Abstract implementation of a simulator for a station.  
 * 
 * Inheriting classes implement the simulate method to perform
 * the actions that would occur when a load arrives at this station.
 * 
 * @author karmstrong
 * @see SimulationController
 */

public abstract class StationSimulator
{
  protected StationData mpSD;
  protected String msName;
  
  public StationSimulator(StationData ipSD)
  {
    mpSD = ipSD;
    msName = ipSD.getStationName();
  }
  
  /**
   * Indicates if this station is simulating load stores or not.
   * This method should be overridden to do something besides just
   * return false if a simulator needs to handle any kind of timer 
   * based storing.
   * 
   * @return true if the station is supposed to simulate load stores.
   */
  public boolean isStoringStation()
  {
    return false;
  }
  
  /**
   * Sets the station data associated with the simulator.
   * 
   * Overriding methods may alter the behavior of a simulator if some
   * station parameter change dictates it.
   * @param ipSD New station information.
   */
  public void setStationData(StationData ipSD)
  {
    mpSD = ipSD;
  }
  
  /**
   * Checks to see if the simulation interval has changed.  Useful for store timers.
   * @param inInterval new interval
   * @return true if the current interval differs from the input parameter
   */
  public boolean intervalChanged(int inInterval)
  {
    return (inInterval != mpSD.getSimInterval());
  }
  
  /**
   * Simulate the arrival of a load at this station.
   * @param isLoad
   * @return The name of the next station to simulate or null if there is none
   * @throws DBException
   */
  public abstract String simulate(String isLoad) throws DBException;
  
  protected void logDebug(String isMsg)
  {
//    System.out.println(isMsg);
  }

}
