package com.daifukuamerica.wrxj.scheduler.ship;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardShipmentServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.scheduler.Scheduler;
import java.util.List;
import java.util.Map;

/**
 * The ShipmentScheduler Ships Loads that are waiting to be shipped.
 * 
 * <P>Note: This controller is set up as a scheduler so that it may be event
 * driven. It may also "drive" itself via polling.</P>
 * 
 * @author Ryan Graham
 * @version 1.0
 */
public class ShipmentScheduler extends Scheduler
{ 
  // This variable should be read from the environment, but
  // we'll set default values here in case it isn't set.
  protected int mnMaxPerPass = 100;
  
  protected StandardLoadServer mpLoadServer;
  protected StandardOrderServer mpOrderServer;
  protected StandardShipmentServer mpShipServer;
  
  /**
   * Constructor
   */
  public ShipmentScheduler()
  {
    this(null);
  }
  
  /**
   * Constructor
   * 
   * @param isName
   */
  public ShipmentScheduler(String isName)
  {
    super(isName);
    initScheduler();
  }
  
  /**
   * Method to read the environment variables in initialize the scheduler.
   */
  protected void initScheduler()
  {
    // Check the environment for the maximum number of orders to process / pass
    String vsMaxPerPass = Application.getString("shipping.maxPerPass");
    if (vsMaxPerPass != null)
    {
      mnMaxPerPass = Integer.valueOf(vsMaxPerPass).intValue();
    }
  }
  
  /**
   * Method to check for waiting loads.
   */
  public void checkLoads()
  {
    // Get the list of waiting loads
    LoadData vpLoadKey = Factory.create(LoadData.class);
    vpLoadKey.addKeyObject(new KeyObject(LoadData.LOADMOVESTATUS_NAME, DBConstants.SHIPWAIT));
    vpLoadKey.addOrderByColumn(LoadData.MOVEDATE_NAME);
    
    List<Map> loadList = null;
    try
    {
      loadList = mpLoadServer.getLoadDataList(vpLoadKey);
    }
    catch(DBException exp)
    {
      logger.logException("Error getting SHIPWAIT loads", exp);
      return;
    }
    
    // Ship each load.
    for (int i = 0; i < loadList.size(); i++)
    {
      LoadData lddata = Factory.create(LoadData.class);
      lddata.dataToSKDCData(loadList.get(i));

      /*
       * Ship the load
       */
      try
      {
        mpShipServer.shipLoad(lddata.getLoadID(), true);
      }
      catch (DBException exp)
      {
        exp.printStackTrace();
        continue;
      }
    }
  }
  
  @Override
  protected void processStationEvent(String receivedString) {}
  @Override
  protected void processLoadEvent(String receivedString) {}
  @Override
  protected void processSchedulerEvent(String receivedString) {}

  /**
   * This method processes the Inter-Process-Communication Message received to
   * the correct message and calls the process method for that particular
   * message.
   */
  @Override
  protected void processIPCReceivedMessage()
  {
    super.processIPCReceivedMessage();
    checkLoads();  //  TODO: This should become a timed event
  }
  
  /**
   * Factory for ControllerImplFactory.
   *
   * <p><b>Details:</b> <code>create</code> is a factory method used exclusively
   * by <code>ControllerImplFactory</code>.  Configurable properties of a new
   * controller created using this method are initialized using data in the
   * supplied properties object.  If the controller cannot be created, a
   * <code>ControllerCreationException</code> is thrown.</p>
   *
   * @param ipConfig configurable property definitions
   * @return the created controller
   * @throws ControllerCreationException if an error occurred while creating the controller
   */
  public static Controller create(ReadOnlyProperties ipConfig) throws ControllerCreationException
  {
    Controller vpController = Factory.create(ShipmentScheduler.class);
    return vpController;
  }
  
  /**
   * Method to Initialize everything need to run the AGCSCHEDULER.
   */
  @Override
  public void startup()
  {
    super.startup();
    logger.logDebug("ShipScheduler.startup() - Start Scheduler"
        + getSchedulerName());
    mpLoadServer = Factory.create(StandardLoadServer.class);
    mpOrderServer = Factory.create(StandardOrderServer.class);
    mpShipServer = Factory.create(StandardShipmentServer.class);
    logger.logDebug("ShipScheduler.startup() - End");
  }

  /**
   * Shuts down this controller by cancelling any timers and shutting down the
   * Equipment.
   */
  @Override
  public void shutdown()
  {
    logger.logDebug("ShipScheduler.shutdown() -- Start");
    mpLoadServer.cleanUp();
    mpLoadServer = null;
    mpOrderServer.cleanUp();
    mpOrderServer = null;
    mpShipServer.cleanUp();
    mpShipServer = null;
    logger.logDebug("ShipScheduler.shutdown() -- End");
    super.shutdown();
  }
}