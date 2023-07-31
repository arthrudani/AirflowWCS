package com.daifukuamerica.wrxj.allocator;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.dataserver.standard.StandardAllocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardConfigurationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardRouteServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLine;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.SysConfig;
import com.daifukuamerica.wrxj.dbadapter.data.TableJoin;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.messageformat.controlevent.ControlEventDataFormat;
import com.daifukuamerica.wrxj.timer.RestartableTimerTask;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Description:<BR>
 *  Controller Class to decide which allocation Strategy to use.  This controller
 *  subscribes for Allocate and Order events and publishes Scheduler events.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 20-Jul-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  SKDC Corporation
 */
public class AllocationController extends Controller
{
  private   String subscriberKeyName;
  protected   boolean            mzSplitSystem;
  protected   String[]           masCollaborators;
  protected List<AllocationMessageDataFormat> allocatedLoadList = null;
  protected Map<String,AllocationStrategy>
                               allocTypes = new HashMap<String,AllocationStrategy>();
  protected Map<String,String> hungryStations = Collections.synchronizedMap(new HashMap<String,String>());

                                       // Data objects.
  protected OrderHeaderData ordData = Factory.create(OrderHeaderData.class);
  protected OrderLineData   olData  = Factory.create(OrderLineData.class);

                                       // BaseDBInterface objects.
  protected OrderLine     orderLine;
  protected TableJoin     tj;
  protected DBObject      dbobj;
                                       // Server objects.
  protected StandardAllocationServer    mpAllocServer;
  protected StandardDeviceServer        mpDevServer;
  protected StandardLoadServer          mpLoadServer;
  protected StandardLocationServer      mpLocServer;
  protected StandardOrderServer         mpOrderServer;
  protected StandardRouteServer         mpRouteServer;
  protected StandardStationServer       mpStationServer;
  protected StandardConfigurationServer mpConfigServer;
                               //Allocation Strategies
  protected AllocationStrategy mpFullLoadOutStrategy;
  protected AllocationStrategy mpContainerStrategy;
  protected AllocationStrategy mpCycleCountStrategy;
  protected AllocationStrategy mpReplenishStrategy;
  protected AllocationStrategy mpLineStrategy;
  protected AllocationStrategy mpPieceStrategy;

  protected AllocationMessageDataFormat mpIPCAllocData;
  protected AllocationProbe  allocProbe = null;

  protected String msAllocatorName = "";

  private CheckHungryStationsTask mpHungryTask = new CheckHungryStationsTask();
  private int mnDefaultCheckInterval = 60000;

  public AllocationController()
  {
  }

  /**
   *  Sets the the status of this controller to RUNNING.
   */
  @Override
  public void startup()
  {
    super.startup();
    logger.logDebug("AllocationController.startup() - Start");

    mpIPCAllocData = Factory.create(AllocationMessageDataFormat.class);

/*==========================================================================
   These are the objects that involve thread local.  In order to assure that
   this thread gets its own copy of a thread local object and then uses this
   same object subsequently throughout any BaseDBInterface object.
  ==========================================================================*/
    dbobj = new DBObjectTL().getDBObject();
    orderLine = Factory.create(OrderLine.class);
    tj        = Factory.create(TableJoin.class);

    mpAllocServer   = Factory.create(StandardAllocationServer.class, getName());
    mpDevServer     = Factory.create(StandardDeviceServer.class, getName());
    mpLoadServer    = Factory.create(StandardLoadServer.class, getName());
    mpLocServer     = Factory.create(StandardLocationServer.class, getName());
    mpOrderServer   = Factory.create(StandardOrderServer.class, getName());
    mpRouteServer   = Factory.create(StandardRouteServer.class, getName());
    mpStationServer = Factory.create(StandardStationServer.class, getName());
    mpConfigServer  = Factory.create(StandardConfigurationServer.class, getName());

    try
    {
      dbobj.connect();                 // Get connection from connection pool.

                                       // Load some objects for the lifetime
                                       // of this controller.
      try
      {
        Map<String, String> vpStratMap = mpConfigServer.getCachedSysNameValuePairs(SysConfig.ALLOCATION_STRATEGY);
        Iterator<String> vpIter = vpStratMap.keySet().iterator();

        while(vpIter.hasNext())
        {
          String vsParamName = vpIter.next();
                                       // The class name.
          String vsParamValue = vpStratMap.get(vsParamName);
          try
          {

            if (!vsParamValue.startsWith("com"))
              vsParamValue = AllocationStrategy.class.getPackage().getName() +
                            '.' + vsParamValue;

            Class<? extends AllocationStrategy> vpClass = null;
            vpClass = Class.forName(vsParamValue)
                           .asSubclass(AllocationStrategy.class);
            AllocationStrategy vpStrategy = Factory.create(vpClass);
            loadPersistentStrategy(vpStrategy, vsParamValue);
          }
          catch(Exception ex)
          {
            logger.logException("Error initializing allocation strategy - " +
                                vsParamName, ex);
          }
        }

        mzSplitSystem = mpConfigServer.isSplitSystem();
      }
      catch (DBException ex)
      {
        logger.logException("Error loading allocation strategies.", ex);
      }

                                       // Mark this controller as running.
      super.setControllerStatus(ControllerConsts.STATUS_RUNNING);

      findLoadMoves();                 // Find any moves that haven't been done
                                       // by the scheduler and let it know.
    }
    catch (DBException e)
    {
      logger.logException(e, "Error opening Database Connection");
    }

    setupCheckHungryStationsTask();

    logger.logDebug("AllocationController.startup() - End");
  }

  public void setupCheckHungryStationsTask()
  {
    int vnCheckInterval = getConfigPropertyAsInt("CheckHungryStationsInterval");
    if (vnCheckInterval == -1)         // The SHORT order timer can be intentionally
    {                                  // disabled by setting the check interval to -1.
      logger.logDebug("Allocation controller's internal SHORT order timer is " +
                      "disabled: CheckHungryStationsInterval = -1");
      return;
    }

    if (vnCheckInterval > 0)
    {
      logger.logDebug("CheckHungryStationsInterval: " + vnCheckInterval + " - startup()");
    }
    else
    {
      logger.logError("INVALID CheckHungryStationsInterval \"" + vnCheckInterval + "\" - startup()");
      logger.logDebug("Using Default CheckHungryStationsInterval: " + mnDefaultCheckInterval + " NO or INVALID CheckHungryStationsInterval in Config - startup()");
      vnCheckInterval = mnDefaultCheckInterval;
    }
    timers.setPeriodicTimerEvent(mpHungryTask, vnCheckInterval);
  }

  @Override
  public void shutdown()
  {
    logger.logDebug("AllocationController.shutdown() -- Start");
    try
    {
      dbobj.disconnect();
      logger.logDebug("Closing DB Connection...");
    }
    catch (DBException e)
    {
      logger.logException(e, "Error closing Database Connection");
    }
    timers.cancel(mpHungryTask);
    mpHungryTask = null;

    mpAllocServer.cleanUp();
    mpDevServer.cleanUp();
    mpLoadServer.cleanUp();
    mpLocServer.cleanUp();
    mpOrderServer.cleanUp();
    mpRouteServer.cleanUp();
    mpStationServer.cleanUp();
    mpConfigServer.cleanUp();

    logger.logDebug("AllocationController.shutdown() -- End");
    super.shutdown();
  }

  @Override
  public void initialize(String uniqueControllerName)
  {
    msAllocatorName = uniqueControllerName;
    super.initialize(uniqueControllerName);

    if (super.collaboratorCKN != null)
    {
      masCollaborators = SKDCUtility.getTokens(super.collaboratorCKN, ",");
      if (masCollaborators.length == 0)
      {
        masCollaborators = new String[] {super.collaboratorCKN};
      }
    }

    // Get all Allocation requests (primarily
    // from Scheduler).
    for(int i=0; i<masCollaborators.length; i++)
    {
      subscribeAllocateEvent(masCollaborators[i], false);
    }
    subscribeOrderEvent(msAllocatorName);             // Get all order events from order server.
    subscribeAllocationProbeEvent(msAllocatorName);
    subscribeControlEvent(msAllocatorName);
  }

 /**
  *  Method Processes messages from JMS.  This method processes messages from
  *  the Order Server <code>StandardOrderServer</code> and the work scheduler
  *  <code>AGCScheduler</code>.
  *
  *  The Order Server simply passes the Order ID. of the Order that needs to be
  *  allocated.  The receivedData key portion of the message must contain
  *  {@link AllocationMessageDataFormat#NORMAL_ORDER} or
  *  {@link AllocationMessageDataFormat#CYCLECOUNT_ORDER} as the order type when the
  *  message comes from the Order Server.
  */
  @Override
  public void processIPCReceivedMessage()
  {
    super.processIPCReceivedMessage();
    if (!super.receivedMessageProcessed)
    {
      super.receivedMessageProcessed = true;
      try
      {
        if (!receivedSelector.startsWith(MessageEventConsts.ALLOCATION_PROBE_EVENT_TEXT) &&
            !receivedSelector.startsWith(MessageEventConsts.ORDER_EVENT_TEXT)  &&
            !receivedSelector.startsWith(MessageEventConsts.CUSTOM_EVENT_TYPE_TEXT))
        {                              // For now only decode Scheduler events.
          if (!mpIPCAllocData.decodeReceivedString(super.receivedText))
          {
            logger.logError("Error decoding scheduler message - processIPCReceivedMessage()");
            return;
          }
        }

        String outputStation, orderID;
        switch(super.receivedEventType)
        {                              // Should be a station name in message.
          case MessageEventConsts.ALLOCATE_EVENT_TYPE:
            allocProbe = null;
            outputStation = mpIPCAllocData.getOutputStation();
            if (destStationIsEmpty(outputStation))
            {
              logger.logError("processIPCReceivedMessage:"
                  + " Blank Station Name received from Scheduler!\n"
                  + receivedText);
            }
            else
            {
              logger.logDebug("Scheduler Event for station: " + outputStation
                  + " - Start");
              processSchedulerEvent(outputStation, receivedData);
              logger.logDebug("Scheduler Event for station: " + outputStation
                  + " - Done");
            }
            break;
                                       // Should be an order id. in message.
          case MessageEventConsts.ORDER_EVENT_TYPE:
            allocProbe = null;
            orderID = receivedText;
            logger.logDebug("In ORDER_EVENT_TYPE: orderID = " + orderID);
            if (orderID.trim().length() == 0)
            {
              logger.logError("AllocationController-->processIPCReceivedMessage:" +
                              " Blank order ID received from OrderServer!");
              break;
            }
            processOrderEvent(orderID);
            break;

          case MessageEventConsts.ALLOCATION_PROBE_EVENT_TYPE:
            orderID = outputStation = "";
            if (receivedData == AllocationMessageDataFormat.ORDER_DIAGNOSIS)
              orderID = receivedText;
            else
              outputStation = receivedText;

            subscriberKeyName = receivedCKN;
            allocProbe = new AllocationProbe();

            if (!destStationIsEmpty(outputStation))
            {
              processSchedulerEvent(outputStation, 1);
            }                          // Check code path by a particular order id.
            else if (orderID.trim().length() != 0)
            {
              processOrderEvent(orderID);
            }
            break;

          case MessageEventConsts.CUSTOM_EVENT_TYPE:
            String vsCustomField = super.receivedText;
            processCustomEvent(vsCustomField);
            break;

          default:
            super.receivedMessageProcessed = false;
        }
      }
      catch(DBException e)
      {
        logger.logException(e, "processIPCReceivedMessage");
        logProbe("AllocationController.processIPCReceivedMessage", e.getMessage());
      }
      finally
      {
        publishAllocationProbeResults();
      }
    }
  }

  /**
   * Processes Order Event Messages from the Order Server.  Note: It is assumed
   * that Full-Out orders will be for immediate processing.  The Order Server
   * should mark all Full-Out load orders as ALLOCATENOW if they aren't marked
   * that way already.  It is also assumed that for Full-Out orders the
   * destination station in the order will be the output station for the load
   * (since they know what aisle the load is on).
   *
   * @param orderID no information available
   * @throws DBException no information available
   */
  protected void processOrderEvent(String orderID) throws DBException
  {
    OrderHeaderData ohdata = mpOrderServer.getOrderHeaderRecord(orderID);
    int iCurrentOrdStat;

    if (ohdata == null)                // Something is really wrong if this
    {                                  // happens!
      String msg = "Order \"" + orderID + "\" not found on system!";
      logger.logDebug("AllocationController-->processOrderEvent():" + msg);
      throw new DBException(msg);
    }
    else
    {
      iCurrentOrdStat = ohdata.getOrderStatus();
    }

/*---------------------------------------------------------------------------
           Don't do allocation for stations that don't allow it.
  ---------------------------------------------------------------------------*/
    if (!destStationIsEmpty(ohdata.getDestinationStation()) &&
        !mpStationServer.isStationAllocationEnabled(ohdata.getDestinationStation()))
    {
      String vsMsg = "Station " + ohdata.getDestinationStation() +
                     " does not have allocation enabled.  Received request but " +
                     "no orders allocated...";
      logger.logDebug(vsMsg);
      logProbe("AllocationController.processOrderEvent",vsMsg);
      return;
    }

    if (mzSplitSystem && (ohdata.getOrderType() == DBConstants.FULLLOADOUT ||
        ohdata.getOrderType() == DBConstants.EMPTY_CONTAINER_ORDER))
    {
      handleSplitSystemOrder(ohdata);
    }
    else
    {
      if (iCurrentOrdStat != DBConstants.SHORT       &&
          iCurrentOrdStat != DBConstants.ALLOCATENOW &&
          iCurrentOrdStat != DBConstants.READY       &&
          iCurrentOrdStat != DBConstants.HOLD        &&
          iCurrentOrdStat != DBConstants.REALLOC)
      {
        try
        {
          String sOrderStat = DBTrans.getStringValue("iOrderStatus", iCurrentOrdStat);
          String mesg = "Order " + orderID + " is in " + sOrderStat +
                        " state. Can't retry allocation.";
          logProbe("AllocationController.processOrderEvent", mesg);
          logger.logDebug("AllocationController-->processOrderEvent():" + mesg);
        }
        catch(NoSuchFieldException e)
        {
          throw new DBException("Order " + orderID + " has an invalid status...", e);
        }
        return;
      }
      ordData = ohdata.clone();
      String sDestinationStation = ordData.getDestinationStation();

      if (ordData.getOrderType() == DBConstants.CYCLECOUNT ||
          ordData.getOrderType() == DBConstants.REPLENISHMENT)
      {
        try
        {
          AllocationStrategy allocStrategy = getAllocationStrategy("", ordData);
          execOrderAllocStrategy(allocStrategy, ordData, "");
        }
        catch(DBException exc)
        {
          logProbe("AllocationController.processOrderEvent", exc.getMessage());
          logger.logDebug(exc.getMessage() +
                          " IN:AllocationController.processOrderEvent()");
        }
      }
      else if (sDestinationStation.trim().length() == 0)
      {
        try
        {
          AllocationStrategy allocStrategy = getAllocationStrategy(sDestinationStation,
                                                                   ordData);
          if (allocStrategy != null)
            execLineAllocStrategy(allocStrategy, ordData);
        }
        catch(DBException exc)
        {
          if (exc.getCause() == null)
          {
            logger.logException(exc, ":::::: Trying to Allocate order " +
                                ordData.getOrderID() +
                                " AllocationController.processOrderEvent()");
            logProbe("AllocationController.processOrderEvent",
                     "Order allocation exception for order: " +
                     ordData.getOrderID() + "::" + exc.getMessage());
          }
          else
          {
            logger.logException(exc, DBException.toString(exc.getCause()) +
                                ":::::: Trying to Allocate order " + ordData.getOrderID() +
                                " AllocationController.processOrderEvent()");
            logProbe("AllocationController.processOrderEvent",
                     "Order allocation exception for order: " +
                     ordData.getOrderID() + "::" + exc.getMessage() + exc.getCause());
          }
        }
      }
      else
      {
        try
        {
          AllocationStrategy allocStrategy = getAllocationStrategy(sDestinationStation,
                                                                   ordData);
          if (allocStrategy != null)
            execOrderAllocStrategy(allocStrategy, ordData, sDestinationStation);
        }
        catch(DBException exc)
        {
          if (exc.getCause() == null)
          {
            logger.logException(exc, ":::::: Trying to Allocate order " +
                                ordData.getOrderID() +
                                " AllocationController.processOrderEvent()");
            logProbe("AllocationController.processOrderEvent",
                     "Order allocation exception for order: " +
                     ordData.getOrderID() + "::" + exc.getMessage());
          }
          else
          {
            logger.logException(exc, DBException.toString(exc.getCause()) +
                                ":::::: Trying to Allocate order " + ordData.getOrderID() +
                                " AllocationController.processOrderEvent()");
            logProbe("AllocationController.processOrderEvent",
                     "Order allocation exception for order: " +
                     ordData.getOrderID() + "::" + exc.getMessage() + exc.getCause());
          }
        }
      }
    }
  }

  /**
   * Given an output station, this method finds a candidate order for this
   * station, then gets the preparatory data needed to get the correct
   * Allocation Strategy.
   *
   * @param isStation <code>String</code> that needs work according to
   *          scheduler.
   * @param inLoadsToStage - number of loads to stage
   */
  protected void processSchedulerEvent(String isStation, int inLoadsToStage)
      throws DBException
  {
/*---------------------------------------------------------------------------
           Don't do allocation for stations that don't allow it.
  ---------------------------------------------------------------------------*/
    if (!mpStationServer.isStationAllocationEnabled(isStation))
    {
      String vsMsg = "Station " + isStation + " does not have allocation " +
                     "enabled.  Received request but no orders allocated...";
      logger.logDebug(vsMsg);
      logProbe("AllocationController.processSchedulerEvent", vsMsg);
      return;
    }

    /*
     * This used to be in the scheduler, but timing issues between the scheduler
     * and the allocator could cause us to over-stage.  With the allocator
     * performing this check instead of the scheduler, over-staging should no
     * longer occur.
     */
    if (inLoadsToStage < 0)
    {
      inLoadsToStage = mpStationServer.getNumberOfLoadsToStage(isStation);
      if (inLoadsToStage < 1)
      {
        return;
      }
    }

    // TODO: Count by LOADS, not ORDERS.  In a load mover, this doesn't matter, but it will in an item mover.
    int vnOrdersStaged = 0;
    OrderHeaderData vpOHData = null;
    do
    {
      /*
       * Get the highest priority order with the oldest scheduled date that can
       * go through this output station.
       */
      vpOHData = tj.getOrdersByOutputStation(isStation);
      if (vpOHData != null)
      {
        try
        {
          // Find the allocation strategy to use for this order.
          AllocationStrategy vpAllocStrategy = getAllocationStrategy(isStation,
                                                                   vpOHData);
          if (vpAllocStrategy != null)
          {
            execOrderAllocStrategy(vpAllocStrategy, vpOHData, isStation);
          }
        }
        catch (DBException exc)
        {
          if (exc.getCause() == null)
          {
            String msg = exc.getMessage() + ":::::: Trying to Allocate order "
                + vpOHData.getOrderID();
            logProbe("AllocationController.processSchedulerEvent()", msg);
            logger.logError(msg);
          }
          else
          {
            String msg = exc.getMessage()
                + DBException.toString(exc.getCause())
                + ":::::: Trying to Allocate order " + vpOHData.getOrderID();
            logProbe("AllocationController.processSchedulerEvent()", msg);
            logger.logError(msg);
          }
        }
      }
      else
      {
        /*
         * There are no orders to allocate for this output station. Keep
         * reference to this station for later use.
         */
        addToHungryStationList(isStation);
      }
      vnOrdersStaged++;
    } while (vpOHData != null && vnOrdersStaged < inLoadsToStage);
  }

  /**
   * Process control events (usually from user forms)
   */
  @Override
  protected void processControlEvent()
  {
    super.processControlEvent();
    checkHungryStations();
  }

  protected void processCustomEvent(String isCustomText) throws DBException
  {
    // Override for custom processing.
  }

 /**
  * Figures out allocation strategy using Station setting or Order Type.
  *
  * @param outputStation <code>StationData</code> containing the output station
  *        that has an Allocation type.
  * @param ohData <code>AbstractSKDCData</code> containing Order data.
  *
  * @throws DBException if output staiotn is invalid.
  * @return AllocationStrategy to be used for current Order's (ohData)
  *         allocation.
  */
  protected AllocationStrategy getAllocationStrategy(String outputStation,
      OrderHeaderData ohData) throws DBException
  {
    AllocationStrategy vpStrategy = null;

    switch (ohData.getOrderType())
    {
      case DBConstants.FULLLOADOUT:
        initializeFullLoadOutStrategy();
        vpStrategy = mpFullLoadOutStrategy;
        break;

      case DBConstants.CONTAINER:
        initializeCountainerStrategy();
        vpStrategy = mpContainerStrategy;
        break;

      case DBConstants.CYCLECOUNT:
        initializeCycleStrategy();
        vpStrategy = mpCycleCountStrategy;
        break;

      case DBConstants.REPLENISHMENT:
        initializeReplenishStrategy();
        vpStrategy = mpReplenishStrategy;
        break;

      case DBConstants.ITEMORDER:
        if (outputStation.trim().length() > 0)
        {
          initializePieceStrategy();
          vpStrategy = mpPieceStrategy;
        }
        else
        {
          initializeLineStrategy();
          vpStrategy = mpLineStrategy;
        }
        break;

      default:
        if (!mpStationServer.exists(outputStation))
        {
          String msg = "Output Station \"" + outputStation
              + "\" NOT found on system for order " + ohData.getOrderID();
          logger.logDebug(msg
              + " - AllocationController.getAllocationStrategy()");
          throw new DBException(msg);
        }

        String vsAllocationType = mpStationServer.getAllocationType(outputStation);
        if ((vpStrategy = allocTypes.get(vsAllocationType)) == null)
        {
          vpStrategy = Factory.create(PieceAllocation.class);
        }
    }

    //  Make sure the strategy has a logger
    vpStrategy.setLogger(logger);

    return (vpStrategy);
  }

  /**
   * Carries out the work of calling the allocator, and processing its
   * return.
   *
   * @param allocStrategy <code>AllocationStrategy</code> type to call.
   * @param ipOrderHeaderData Order to allocate
   *
   * @throws DBException no information available
   */
  protected void execOrderAllocStrategy(AllocationStrategy allocStrategy,
                                   OrderHeaderData ipOrderHeaderData,
                                   String requestingStation) throws DBException
  {
    if (allocatedLoadList != null)
    {                                  // Clear out allocated load list.
      allocatedLoadList.clear();
      allocatedLoadList = null;
    }

/*---------------------------------------------------------------------------
             Communicate to the allocation strategy its data.
  ---------------------------------------------------------------------------*/
    if (ipOrderHeaderData.getOrderType() != DBConstants.CYCLECOUNT &&
        ipOrderHeaderData.getOrderType() != DBConstants.REPLENISHMENT)
    {
      allocStrategy.setAisleGroup(mpStationServer.getStationAisleGroup(requestingStation));
      allocStrategy.setOutputStation(mpStationServer.getStation(requestingStation));
    }
    logger.logDebug("IN AllocationController-->execOrderAllocStrategy(): ohdata = " + ipOrderHeaderData.toString());
    allocStrategy.setAllocationOrder(ipOrderHeaderData);
    if (allocProbe != null) allocStrategy.setAllocationProbe(allocProbe);

                                       // allocate() starts and finishes its trans.
    try
    {
      allocatedLoadList = allocStrategy.allocate();
                                       // Mark order scheduled and mark loads
                                       // Retrieve Pending.
      markLoadsRetrievePending();

/*============================================================================
 *    If nothing was allocated for the intended station, put the station into
 *    the hungry stations list.
 *============================================================================*/
      if (ipOrderHeaderData.getOrderType() != DBConstants.CYCLECOUNT &&
          (allocatedLoadList == null || allocatedLoadList.isEmpty()))
      {
        addToHungryStationList(requestingStation);
      }
    }
    catch(AllocationException aexc)
    {
      logger.logError(aexc.getMessage());
    }
  }

  /**
   * Causes the allocation of a single order line to occur.
   *
   * @param allocStrategy <code>AllocationStrategy</code> type to call.
   * @param ipOrderLineData Order line to allocate
   *
   * @throws DBException no information available
   */
  protected void execLineAllocStrategy(AllocationStrategy allocStrategy,
                                       OrderHeaderData ipOHData) throws DBException
  {
    allocStrategy.setAisleGroup(0);
//    allocStrategy.setOutputStation(null);

    OrderHeaderData vpOHData = mpOrderServer.getOrderHeaderRecord(ipOHData.getOrderID());
    allocStrategy.setAllocationOrder(vpOHData);
    try
    {
      allocatedLoadList = allocStrategy.allocate();
      markLoadsRetrievePending();
    }
    catch(AllocationException aexc)
    {
      logger.logError(aexc.getMessage());
    }
  }

 /**
  * Method to handle orders from an Order Event.
  * @param isOrderId
  */
  protected void handleSplitSystemOrder(OrderHeaderData ipOHData)
          throws DBException
  {
    OrderHeaderData vpOrdData = ipOHData.clone();
    try
    {
      String isDestStation = vpOrdData.getDestinationStation();
      AllocationStrategy vpAllocStrat = getAllocationStrategy(isDestStation,
                                                              vpOrdData);
      if (vpAllocStrat != null)
        execOrderAllocStrategy(vpAllocStrat, vpOrdData, isDestStation);
    }
    catch(DBException exc)
    {
      if (exc.getCause() == null)
      {
        logger.logException(exc, ":::::: Trying to Allocate order " +
                            vpOrdData.getOrderID() +
                            " AllocationController.processOrderEvent()");
        logProbe("AllocationController.handleSplitSystemOrder",
                 "Order allocation exception for order: " +
                 vpOrdData.getOrderID() + "::" + exc.getMessage());
      }
      else
      {
        logger.logException(exc, DBException.toString(exc.getCause()) +
                            ":::::: Trying to Allocate order " + vpOrdData.getOrderID() +
                            " AllocationController.processOrderEvent()");
        logProbe("AllocationController.handleSplitSystemOrder",
                 "Order allocation exception for order: " +
                 vpOrdData.getOrderID() + "::" + exc.getMessage() + exc.getCause());
      }
    }
  }

  protected boolean destStationIsEmpty(String isStationName)
  {
    return(isStationName.trim().length() == 0);
  }

 /**
  *  Send all/any schedulers events of allocated loads.  This method will get the
  *  correct device of the load before publishing a message to that device's
  *  scheduler.
  */
  protected void publishSchedulerNotifications()
  {
    for(AllocationMessageDataFormat vpAllocatedData : allocatedLoadList)
    {
      String vsAllocatedLoad = vpAllocatedData.getOutBoundLoad();
      if (vsAllocatedLoad != null && vsAllocatedLoad.trim().length() != 0)
      {
        sendToCollaborators(mpLoadServer.getLoadDeviceID(vsAllocatedLoad),
                            vpAllocatedData.createStringToSend());
      }
    }
  }

  private void publishAllocationProbeResults()
  {
    if (allocProbe != null)
    {
      if (allocProbe.diagnosticsCollected())
        publishAllocationProbeEvent(allocProbe.toString(), 0, subscriberKeyName);
      else
        publishAllocationProbeEvent("No problems found in allocating.", 0,
                                    subscriberKeyName);
      allocProbe.reset();
    }
  }

  /**
   * Notifies scheduler of any load moves it needs to do when this controller
   * starts up.
   *
   * Note:  Future enhancement.  If we allow for individual scheduler to station
   * associations (via a scheduler name field in the Station record, then this
   * method will look at all station scheduler names and notify them individually.
   *
   * @throws DBException no information available
   */
  protected void findLoadMoves() throws DBException
  {
    Map currRow = null;
    String  superLoad;
    String  currWarehouse;
    String  currAddress;
    String  nextAddress;
    String  loadDevice;

    for(String vsSchedulerName : masCollaborators)
    {
      List<Map> arrList = tj.getUnscheduledLoadMoves(vsSchedulerName);
      for(int ldIdx = 0; ldIdx < arrList.size(); ldIdx++)
      {
        currRow = arrList.get(ldIdx);
        superLoad = DBHelper.getStringField(currRow, LoadData.PARENTLOAD_NAME);
        currWarehouse = DBHelper.getStringField(currRow, LoadData.WAREHOUSE_NAME);
        currAddress = DBHelper.getStringField(currRow, LoadData.ADDRESS_NAME);
        nextAddress = DBHelper.getStringField(currRow, LoadData.NEXTADDRESS_NAME);

        mpIPCAllocData.clear();
        mpIPCAllocData.setOutBoundLoad(superLoad);
        mpIPCAllocData.setFromWarehouse(currWarehouse);
        mpIPCAllocData.setFromAddress(currAddress);
        mpIPCAllocData.setOutputStation(nextAddress);
        mpIPCAllocData.createDataString();

        loadDevice = DBHelper.getStringField(currRow, LoadData.DEVICEID_NAME);
        sendToCollaborators(loadDevice, mpIPCAllocData.createStringToSend());
      }
    }
  }

  /**
   *  Adds a station to the list of stations that need work.
   */
  protected void addToHungryStationList(String outputStation)
  {
    if (!destStationIsEmpty(outputStation) &&
        !hungryStations.containsKey(outputStation))
    {
      hungryStations.put(outputStation, outputStation);
    }
  }


  /**
   *  Tries to allocate for stations in the list of hungry stations.
   */
  protected void checkHungryStations()
  {
    Collection<String> vpStations = hungryStations.keySet();
    vpStations = new HashSet<String>(vpStations);
    for(String vsStation : vpStations)
    {
      try
      {
        processSchedulerEvent(vsStation, 1);
      }
      catch(DBException e)
      {
        logProbe("AllocationController.checkHungryStations", e.getMessage());
        logger.logException(e, "checkHungryStations");
      }
    }
  }

  /**
   * Mark all allocated loads as RETRIEVEPENDING and notify Scheduler(s).
   */
  synchronized protected void markLoadsRetrievePending()
  {
    if (allocatedLoadList == null || allocatedLoadList.isEmpty())
    {
      return;
    }

    int loadListLength = allocatedLoadList.size();
    for(int row = 0; row < loadListLength; row++)
    {
      mpIPCAllocData = allocatedLoadList.get(row);

      // Take this station off the list of hungry stations.
      if (hungryStations.containsKey(mpIPCAllocData.getOutputStation()))
      {
        hungryStations.remove(mpIPCAllocData.getOutputStation());
      }

      try
      {
        mpAllocServer.changeLoadToRetrievePending(mpIPCAllocData.getOutBoundLoad());
      }
      catch(DBException e)
      {
        logProbe("AllocationController.markLoadsRetrievePending", e.getMessage());
        logger.logException(e, "AllocationController-->markLoadsRetrievePending");
      }
    } // *** End for loop ***
    publishSchedulerNotifications();
  }

 /**
  *  Sends messages to the correct collaborator (scheduler) for this
  *  controller.
  *
  *  @param deviceID <code>String</code> containing device ID of the load.  In
  *         the case of a manual system, the device ID won't be assigned to
  *         a load, in which case this method will do a broadcast to all
  *         schedulers this controller collaborates with.
  *
  *  @param message <code>String</code> containing message to send.
  */
  protected void sendToCollaborators(String deviceID, String message)
  {
                                       // If there is no device ID, broadcast
                                       // the message to all known schedulers.
    if (deviceID.trim().length() == 0)
    {
      for(int i = 0; i < masCollaborators.length; i++)
      {
        publishSchedulerEvent(message, 0, masCollaborators[i]);
        logger.logDebug("AllocationController Publishing to Scheduler " +
                        masCollaborators[i]);
      }
    }
    else
    {
      try
      {
                                         // Get the scheduler for this device.
        String collaboratorName = mpDevServer.getSchedulerName(deviceID);
        for(int i = 0; i < masCollaborators.length; i++)
        {
          if (collaboratorName.equalsIgnoreCase(masCollaborators[i]))
          {
            publishSchedulerEvent(message, 0, masCollaborators[i]);
            logger.logDebug("AllocationController Publishing to Scheduler " +
                            masCollaborators[i]);
          }
        }
      }
      catch(DBException e)
      {
        logProbe("AllocationController.sendToCollaborators", e.getMessage());
        logger.logError("AllocationController.sendToCollaborators --> Getting Scheduler name: " + e.getMessage());
      }
    }
  }

  /**
   *  Load allocation strategy from a persistence layer.
   */
  protected void loadPersistentStrategy(AllocationStrategy ipStrategy,
                                      String strategyTypeKey)
  {
    ipStrategy.setLogger(logger);

    allocTypes.put(strategyTypeKey, ipStrategy);
  }

  protected void logProbe(String sMethodName, String sMessageLog)
  {
    if (allocProbe != null)
    {
      allocProbe.addProbeDetails(sMethodName, sMessageLog);
    }
  }

  private class CheckHungryStationsTask extends RestartableTimerTask
  {
    /*------------------------------------------------------------------------*/
    /**
     * Sends a message to initiate a call to the <code>AllocationController</code>'s checkHungryStations method
     *
     * @see AllocationController#checkHungryStations()
     *
     * The IPC service is used to ensure that the checkHungryStations method is called from the
     * AllocationController thread instead of the timer thread
     */
    public void run()
    {
      publishControlEvent(ControlEventDataFormat.TEXT_ALLOC_HUNGRY_STATION,
          ControlEventDataFormat.TEXT_MESSAGE, msAllocatorName);
    }
  }

  /**
   * Initializer for Allocation Strategy
   */
  protected void initializeFullLoadOutStrategy()
  {
    if(mpFullLoadOutStrategy == null)
    {
      mpFullLoadOutStrategy = Factory.create(FullLoadOut.class);
    }
  }

  /**
   * Initializer for Allocation Strategy
   */
  protected void initializeCountainerStrategy()
  {
    if(mpContainerStrategy == null)
    {
       mpContainerStrategy = Factory.create(EmptyContainerAllocation.class);
    }
  }

  /**
   * Initializer for Allocation Strategy
   */
  protected void initializeCycleStrategy()
  {
    if(mpCycleCountStrategy == null)
    {
      mpCycleCountStrategy = Factory.create(CycleCountAllocation.class);
    }
  }

  /**
   * Initializer for Allocation Strategy
   */
  protected void initializeReplenishStrategy()
  {
    if(mpReplenishStrategy == null)
    {
      mpReplenishStrategy = Factory.create(ReplenishAllocation.class);
    }
  }

  /**
   * Initializer for Allocation Strategy
   */
  protected void initializeLineStrategy()
  {
    if(mpLineStrategy == null)
    {
      mpLineStrategy = Factory.create(LineAllocation.class);
    }
  }

  /**
   * Initializer for Allocation Strategy
   */
  protected void initializePieceStrategy()
  {
    if(mpPieceStrategy == null)
    {
      mpPieceStrategy = Factory.create(PieceAllocation.class);
    }
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
   * <p>This factory initializes the device port and collaborator.</p>
   *
   * @param ipConfig configurable property definitions
   * @return the created controller
   * @throws ControllerCreationException if an error occurred while creating the controller
   */
  public static Controller create(ReadOnlyProperties ipConfig) throws ControllerCreationException
  {
    Controller vpController = Factory.create(AllocationController.class);
    vpController.setCollaboratorCKN(ipConfig.getString(COLLABORATOR));
    return vpController;
  }
}
