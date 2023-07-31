package com.daifukuamerica.wrxj.scheduler.event;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardSchedulerServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.messageformat.controlevent.ControlEventDataFormat;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.List;

/**
 *  Periodic task for checking for stations that need work scheduled.
 *  
 *  This purpose of this task is to act as a safety net for work that somehow 
 *  missed being scheduled by triggers in the scheduler.  It should NOT be 
 *  counted on as the primary method of scheduling work.  The task finds 
 *  stations that need work and sends the scheduler a message of the form
 *                        "TYPEXstn1Xstn2Xst3..." 
 *  where TYPE is the type of work to check for (staged, store, retrieve, 
 *  etc.), X is the delimiting character, and stn1, stn2, etc. are the 
 *  stations to check.
 * 
 *  There is no guarantee that the stations sent may not already have triggers 
 *  requesting work as well.  However, the scheduler should be smart enough to 
 *  deal with the duplicate request.
 */
public class CheckForWorkTask extends TimedEventTask
{
  protected StandardDeviceServer mpDevServ = null;
  protected StandardStationServer mpStnServ = null;
  protected StandardSchedulerServer mpSchedServer = null;

  /**
   * Constructor
   * 
   * @param isName
   */
  public CheckForWorkTask(String isName)
  {
    super(isName);
  }
  
  @Override
  public String initTask()
  {
    mnInitialInterval = 12000;
    int vnSecs = getConfigValue(INTERVAL);
    msIntervalString = vnSecs + " seconds ";
    mnInterval = vnSecs * 1000;
    if (vnSecs < 1)
      return "INVALID CheckForWork interval - " + vnSecs + " CheckForWorkTask will not be started.";
    return null;
  }
  
  @Override
  public boolean cancel()
  {
    boolean rtnval = super.cancel();
    if (mpDevServ != null)
    {
      mpDevServ.cleanUp();
      mpDevServ = null;
    }
    if (mpSchedServer != null)
    {
      mpSchedServer.cleanUp();
      mpSchedServer = null;
    }
    if (mpStnServ != null)
    {
      mpStnServ.cleanUp();
      mpStnServ = null;
    }
    return(rtnval);
  }

  @Override
  public void run()
  {
    mpDevServ = Factory.create(StandardDeviceServer.class);
    mpSchedServer = Factory.create(StandardSchedulerServer.class);
    mpStnServ = Factory.create(StandardStationServer.class);

    ensureDBConnection();
    String[] vasScheds = mpDevServ.getSchedulerChoices(SKDCConstants.NO_PREPENDER,
                                                       true);
    for (String vsScheduler : vasScheds)
    {
      mpLogger.logDebug(vsScheduler + " - CheckForWorkTask beginning.");
      sendSchedulerWork(vsScheduler);
    }
  }
  
  /**
   * Check staging, retrieving, and storing requirements
   * 
   * @param isScheduler
   */
  protected void sendSchedulerWork(String isScheduler)
  {
    /*
     * TODO: Do this right: let it finish and then shut down gracefully instead
     * of yanking the rug out from under it and trying to detect the loss.
     */
    try
    {
      sendSchedulerStagedStations(isScheduler);
      sendSchedulerRetrieveStations(isScheduler);
      sendSchedulerIntraRackRetrieves(isScheduler);
      sendSchedulerStoreStations(isScheduler);
    }
    catch (Exception e)
    {
      if (!mzInterrupted)
      {
        if (mpLogger != null)
        {
          mpLogger.logException("Error checking for work", e);
        }
        else
        {
          e.printStackTrace();
        }
      }
      else
      {
        /*
         * Someone canceled our task, closing our database connection and 
         * setting our servers to null while we were in the middle of processing
         * (see TO-DO above).  Don't log this.
         */
      }
    }
  }

  /**
   * See if any of this schedulers stations that it is scheduling load to
   * retrieve to needs more retrieve pending loads.
   * 
   * @param schedulerName This schedulers name
   */
  protected void sendSchedulerStagedStations(String isScheduler)
  {
    List<String> vpStagedStations = mpStnServ.stageLoadsToStations(isScheduler);   // See what Stations need to move loads
    if(vpStagedStations.size() > 0 )
    {
      ThreadSystemGateway.get().publishControlEvent(
          ControlEventDataFormat.getCommandTargetListMessage(
              ControlEventDataFormat.STAGED, vpStagedStations.toArray(new String[0])), 
          ControlEventDataFormat.TEXT_MESSAGE, isScheduler);
    }
  }
  
  /**
   * See if any of this schedulers stations that it is scheduling load to
   * retrieve to can retrieve another load or if it has enough loads retrieving
   * already.
   */
  protected void sendSchedulerRetrieveStations(String isScheduler)
  {
    try
    {
      String[] vasRetvStations = 
          mpStnServ.getStationsWithRetrievePendingLoads(isScheduler);
      if(vasRetvStations.length > 0 )
      {
        ThreadSystemGateway.get().publishControlEvent(
            ControlEventDataFormat.getCommandTargetListMessage(
                ControlEventDataFormat.RETRIEVE, vasRetvStations),
            ControlEventDataFormat.TEXT_MESSAGE, isScheduler);
      }
    }
    catch(DBException ex)
    {
      mpLogger.logException(ex, "Error in CheckForWorkTask");
    }
  }

  /**
   * See if this scheduler has any intra-rack retrieves that need to be re-
   * scheduled.  If so, it is likely caused by recovery on a double-deep system,
   * but it may come up if we have to do recovery on a system where we agree
   * to shuffle loads.
   * 
   * @param isScheduler
   */
  protected void sendSchedulerIntraRackRetrieves(String isScheduler)
  {
    try
    {
      String[] vasLocToLocLoads = 
        mpSchedServer.getLocToLocRetrievePendingLoads(isScheduler);
      if (vasLocToLocLoads.length > 0)
      {
        ThreadSystemGateway.get().publishControlEvent(
            ControlEventDataFormat.getCommandTargetListMessage(
                ControlEventDataFormat.LOCTOLOC, vasLocToLocLoads),
            ControlEventDataFormat.TEXT_MESSAGE, isScheduler);
      }
    }
    catch(DBException ex)
    {
      mpLogger.logException(ex, "Error in CheckForWorkTask");
    }
  }
  
  /**
   * See if any of this schedulers stations that it is scheduling load to store
   * from needs to have schedule a load to store.
   *
   * @param schedulerName the name of this scheduler
   */
  protected void sendSchedulerStoreStations(String isScheduler)
  {
    try
    {
      if (mpStnServ != null)
      {
        String[] vasStoreStations = 
          mpStnServ.getStationsWithStorePendingLoads(isScheduler);
        if(vasStoreStations.length > 0 )
        {
          ThreadSystemGateway.get().publishControlEvent(
              ControlEventDataFormat.getCommandTargetListMessage(
                  ControlEventDataFormat.STORE, vasStoreStations),
              ControlEventDataFormat.TEXT_MESSAGE, isScheduler);
        }
      }
    }
    catch(DBException ex)
    {
      mpLogger.logException(ex, "Error in CheckForWorkTask");
    }
  }
}
