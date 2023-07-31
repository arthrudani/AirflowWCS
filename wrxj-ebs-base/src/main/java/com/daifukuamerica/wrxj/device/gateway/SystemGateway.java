package com.daifukuamerica.wrxj.device.gateway;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.AbstractIPCMessenger;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.ipc.MessageService;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.util.Reflected;
import java.util.ArrayList;
import java.util.List;

/**
 * Type-safe IPC-only controller.
 *
 * <p><b>Details:</b> <code>SystemGateway</code> is a type-safe representation
 * of an IPC-only controller.  The original <code>ControllerImplFactory</code>
 * implementation instantiated <code>ControllerImpl</code> directly as an
 * IPC-only device type, while all other controllers were subclasses of
 * <code>ControllerImpl</code>.  To promote congruency in the set of available
 * <code>Controller</code> definitions, <code>ControllerImpl</code> was made
 * abstract so that it could not be instantiated directly, and this concrete
 * subclass was introduced for use as a "gateway only" pseudo-<wbr>controller.
 * Other than, <code>create</code>, the factory method, which was moved out of
 * <code>ControllerImpl</code> into this class, <code>SystemGateway</code> adds
 * no new features to the superclass, except for RTTI and type safety at compile
 * time and RTTI.</p>
 *
 * Copied from defunct ObservableController interface:
 * 
 * This class represents an observable object, or "data" in the model-view
 * paradigm. It can be subclassed to represent an object that the application
 * wants to have observed. An observable object can have one or more observers.
 *  An observer may be any object that implements interface Observer. After an
 * observable instance changes, an application calling the ObservableController's
 * notifyObservers method causes all of its observers to be notified of the
 * change by a call to their update method. The order in which notifications
 * will be delivered is unspecified. The default implementation provided in the
 * ObservableController class will notify Observers in the order in which they registered
 * interest, but subclasses may change this order, use no guaranteed order,
 * deliver notifications on separate threaads, or may guarantee that their
 * subclass follows this order, as they choose. Note that this notification
 * mechanism is has nothing to do with threads and is completely separate from
 * the wait and notify mechanism of class Object. When an observable object is
 * newly created, its set of observers is empty. Two observers are considered
 * the same if and only if the equals method returns true for them.
 * 
 * @author Sharky
 */
public final class SystemGateway extends AbstractIPCMessenger
{
  /**
   * Default constructor.
   */
  public SystemGateway()
  {
    setName(null);
  }

  
  /*========================================================================*/
  /*  Getter & Setter Methods                                               */
  /*========================================================================*/
  /**
   * Fetch the unique String identifier for the Controller within the entire system.
   *
   * @return value the unique key
   */
  public String getControllersKeyName()
  {
    return controllersKeyName;
  }

  
  /*========================================================================*/
  /*  Create & Destroy Methods                                              */
  /*========================================================================*/

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
  @Reflected
  public static SystemGateway create(ReadOnlyProperties ipConfig)
      throws ControllerCreationException
  {
    SystemGateway vpSystemGateway = new SystemGateway();
    return vpSystemGateway;
  }

  /**
   * Creates stand-alone SystemGateway instance.
   * 
   * @see #destroy(SystemGateway)
   *
   * @param ipLogger logger for recording creation activity details
   * @return the created SystemGateway
   */
  public static SystemGateway create(Logger ipLogger)
  {
    return create(ipLogger.getLoggerInstanceName(), ipLogger);
  }

  /**
   * Creates stand-alone SystemGateway instance.
   * 
   * @see #destroy(SystemGateway)
   * 
   * @param isSenderKey the name of the sender who owns this SystemGateway
   * @param ipLogger logger for recording creation activity details
   * @return the created SystemGateway
   */
  public static SystemGateway create(String isSenderKey, Logger ipLogger)
  {
    SystemGateway vpGateway = new SystemGateway();
    MessageService vpMessageService = Factory.create(MessageService.class);
    vpMessageService.initialize(isSenderKey);
    vpMessageService.setMessageEventHandler(vpGateway);
    vpGateway.setMessageService(vpMessageService);
    vpMessageService.startup();
    vpGateway.setLogger(ipLogger);
    vpGateway.initialize(isSenderKey);
    vpGateway.initiateStartup();
    vpGateway.startProcessingMessages();
    return vpGateway;
  }
  
  /**
   * Destroys stand-alone SystemGateway instance.
   *
   * <p><b>Details:</b> <code>destroy</code> is an "anti-<wbr>factory" that
   * destroys <code>SystemGateway</code> instances that were created using the
   * <code>create(Logging)</code> factory.  All <code>SystemGateway</code>s
   * created using that factory must be destroyed with this
   * anti-<wbr>factory.</p>
   *
   * @param ipSystemGateway the instance to destroy
   * @see #create(Logger)
   */
  public static void destroy(SystemGateway ipSystemGateway)
  {
    ipSystemGateway.initiateShutdown();
    MessageService vpMessageService = ipSystemGateway.getMessageService();
    vpMessageService.shutdown();
  }


  /*========================================================================*/
  /*  Event / Message Reception                                             */
  /*========================================================================*/

  /**
   * Child Messagers can use this method for any event processing beyond that
   * required for Inter-Process-Communication message processing.
   */
  @Override
  protected final void processLocalEvent()
  {
    runInvokeLaters();
  }

  private List<Runnable> mpTasks = new ArrayList<Runnable>();
  
  private void runInvokeLaters()
  {
    Runnable vpRunnable;
    synchronized (mpTasks)
    {
      if (mpTasks.size() == 0)
        return;
      vpRunnable = mpTasks.remove(0);
    }
    vpRunnable.run();
  }
  
  public void invokeLater(Runnable ipRunnable)
  {
    if (ipRunnable == null)
      throw new NullPointerException();
    synchronized (mpTasks)
    {
      mpTasks.add(ipRunnable);
    }
    synchronized (this)
    {
      notifyAll();
    }
  }

  private boolean firstHeartbeatRequestEvent = true;
  
  /**
   * Handle a received "Heartbeat Request" message event. The received Date/Time
   * is used to compute the time taken to receive the heartbeat request. The
   * elapsed time is then sent back as part of the heartbeat response.
   */
  @Override
  protected void processHeartbeatRequestEvent()
  {
    if (firstHeartbeatRequestEvent)
    {
      firstHeartbeatRequestEvent = false;
      // Send an update in case this controller was up before the system health
      // monitor.
    }
    publishHeartbeatStatusEvent(receivedTxTime);
  }

  
  /*========================================================================*/
  /*  Subscriptions                                                         */
  /*========================================================================*/
  
  /**
   * Fetch a message event filter using the "Load" selector and the specified
   * named sub-system. The returned selector String can be used to subscribe or
   * un-subscribe for "Load" events.
   * 
   * @return the needed selector String
   */
  public String getAllocationProbeEventSelector()
  {
    return MessageEventConsts.ALLOCATION_PROBE_EVENT_TYPE_TEXT;
  }

  /**
   * Fetch a message event filter using the Update selector for all status
   * updates. The returned selector String can be used to subscribe or
   * un-subscribe for "Update" events.
   * 
   * @return
   */
  public String getAllUpdateEventSelector()
  {
    return MessageEventConsts.UPDATE_EVENT_TYPE_TEXT + "%";
  }

  /**
   * Fetch a message event filter using the "Control" selector and the specified
   * named sub-system. The returned selector String can be used to subscribe or
   * un-subscribe for "Control" events.
   * 
   * @param s the named sub-system
   * @return the needed selector String
   */
  public String getControlEventSelector(String s)
  {
    s = MessageEventConsts.CONTROL_EVENT_TYPE_TEXT + s;
    return s;
  }

  /**
   * Fetch a message event filter using the "Load" selector and the specified
   * named sub-system. The returned selector String can be used to subscribe or
   * un-subscribe for "Load" events.
   * 
   * @param s the named sub-system
   * @return the needed selector String
   */
  public String getLoadEventSelector(String s)
  {
    s = MessageEventConsts.LOAD_EVENT_TYPE_TEXT + s;
    return s;
  }

  /**
   * Fetch a message event filter using the "Log" selector and the specified
   * named sub-system. The returned selector String can be used to subscribe or
   * un-subscribe for "Load" events.
   * 
   * @param s the named sub-system
   * @return the needed selector String
   */
  public String getLogEventSelector()
  {
    return MessageEventConsts.LOG_EVENT_TYPE_TEXT;
  }

  /**
   * Subscribe for message events filtered by the "Load" selector and the
   * specified named sub-system.  The returned selector String can be used to
   * un-subscribe for "Load" events.
   *
   * @param isSelector the named sub-system
   * @return the created selector String
   */
  public synchronized String subscribeForLoadEvent(String isSelector)
  {
    // 3rd param "noLocal" - if set, inhibits the delivery of messages published
    // by its own connection.
    isSelector = MessageEventConsts.LOAD_EVENT_TYPE_TEXT + isSelector;
    startupTopicSubscriber(isSelector, true);
    return isSelector;
  }


  /*========================================================================*/
  /*  Thread Management Methods                                             */
  /*========================================================================*/

  /**
   * Bring the Controller implementation to a minimal working state. The
   * base-Class implementation initializes the instantiated Controller by
   * setting the Controller's KeyName, <i>controllersKeyName</i>, attaching
   * logging, creating event observers, creating message event selectors for the
   * message service, and starting the Thread where messages will be processed.
   * 
   * <p>A child Controller should find its collaborators (but <b>NOT</b> publish
   * to them), subscribe to any events it needs from its collaborators, and
   * perform any other simple tasks which do NOT require logging. Any object
   * creation beyond basic java classes should be done in the
   * {@link #startup() startup()} method where logging and message services are
   * available.
   * 
   * <p>Inter-Process-Communication event processing is <b>NOT</b> yet enabled.
   * 
   * @param isControllerKeyName the unique name that identifies this instance of
   *            Controller
   */
  @Override
  protected void initialize(String isControllerKeyName)
  {
    super.initialize(isControllerKeyName);
    
    subscribeRequestEvent();

    selfThread = new ControllerImplThread(this);
    selfThread.setName(getName());
    selfThread.start(); // Start the thread-- executes run().
    
    String vsNameSpace = logger.getLoggerInstanceName();
    logger.logDebug("NameSpace should be: SystemGW-" + vsNameSpace);
  }
}

