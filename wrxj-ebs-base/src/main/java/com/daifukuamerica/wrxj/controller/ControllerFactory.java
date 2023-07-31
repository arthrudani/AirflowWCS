package com.daifukuamerica.wrxj.controller;

import com.daifukuamerica.wrxj.clc.ControllerConfigurationException;
import com.daifukuamerica.wrxj.clc.ControllerDefinition;
import com.daifukuamerica.wrxj.clc.ControllerListConfiguration;
import com.daifukuamerica.wrxj.clc.ControllerProperties;
import com.daifukuamerica.wrxj.clc.ControllerTypeDefinition;
import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.MessageService;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.util.UnderConstructionException;
import com.daifukuamerica.wrxj.util.UnreachableCodeException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages controllers.
 * 
 * <p><b>Details:</b> This singleton class manages the life and death of 
 * Warehouse Rx controllers.  The controllers managed by this class are created 
 * using the information provided by a global 
 * {@link ControllerListConfiguration}, which is stored in this class.</p>
 * 
 * <p>This class replaced the infamous <code>ControllerImplFactory</code> and 
 * humiliated it with its ridiculous simplicity.  Everyone throughout the land 
 * said "Yeah!", and people were happy, celebrating and feasting on all manner 
 * of sugared breakfast cereals.</p>
 * 
 * @author Sharky
 */
public final class ControllerFactory
{

  /**
   * Disables default constructor.
   */
  private ControllerFactory()
  {
    throw new UnreachableCodeException();
  }
  
  /**
   * Global CLC.
   * 
   * @see #getClc()
   * @see #setClc(ControllerListConfiguration)
   */
  private static ControllerListConfiguration gpClc;
  
  /**
   * Returns CLC property.
   * 
   * <p><b>Details:</b> This method returns the controller list configuration 
   * (CLC) used by the {@link ControllerFactory}.  Every running instance of 
   * <cite>Warehouse Rx</cite> must have exactly one CLC.</p>
   * 
   * @return the property
   * 
   * @see #gpClc
   * @see #setClc(ControllerListConfiguration)
   */
  public static ControllerListConfiguration getClc()
  {
    return gpClc;
  }

  /**
   * Sets CLC property.
   * 
   * @param ipClc the new value
   * 
   * @see #gpClc
   * @see #getClc()
   */
  public static void setClc(ControllerListConfiguration ipClc)
  {
    gpClc = ipClc;
  }

  /**
   * Running controllers.
   * 
   * <p><b>Details:</b> This field maps controller names to actual running 
   * controller instances.  Some names may map to <code>null</code>, indicating
   * that the controller terminated.</p> 
   */
  private static Map<String, Controller> gpControllers = new HashMap<String, Controller>();

  /**
   * Lists names of started controllers.
   * 
   * <p><b>Details:</b> This method returns a list of the names of all 
   * controllers that have ever been or could be started.</p>
   * 
   * @return the list
   */
  public synchronized static List<String> getControllerNames()
  {
    try
    {
      return gpClc.listControllerNames();
    }
    catch (ControllerConfigurationException ex)
    {
      throw new UnderConstructionException(ex);
    }
  }

  /**
   * Creates controller.
   * 
   * <p><b>Details:</b> This method creates and starts the named controller, 
   * using parameters provided by the CLC.</p>
   * 
   * @param vsDeviceName the controller name
   * 
   * @see #startControllers(List)
   * @see #startAllControllers()
   */
  public static synchronized void startController(String vsDeviceName)
  {
    Controller vpController = gpControllers.get(vsDeviceName);
    if (vpController != null)
      return;
    try
    {
      ControllerDefinition vpDevDef = gpClc.getControllerDefinition(vsDeviceName);
      String vsDevTypeIdent = vpDevDef.getType();
      ControllerTypeDefinition vpDevTypeDef = gpClc.getControllerTypeDefinition(vsDevTypeIdent);
      Class<? extends Controller> vpDeviceClass = vpDevTypeDef.getImplementingClass();
      Method vpFactory = vpDeviceClass.getMethod
      ( "create", 
        new Class[] {ReadOnlyProperties.class}
      );
      ReadOnlyProperties vpConfig = new ControllerProperties(vpDevTypeDef, vpDevDef);
      vpController = (Controller) vpFactory.invoke(null, new Object[] {vpConfig});
      vpController.setProperties(vpConfig);
      gpControllers.put(vsDeviceName, vpController);
      MessageService controllerMessageService = Factory.create(MessageService.class);
      controllerMessageService.initialize(vsDeviceName);
      controllerMessageService.setMessageEventHandler(vpController);
      vpController.setMessageService(controllerMessageService);
      controllerMessageService.startup();
      vpController.setLogger(Logger.getLogger(vsDeviceName));
      try
      {
        vpController.initialize(vsDeviceName);
      }
      catch (Exception ex)
      {
        Logger.getLogger().logException(ex, "initializeController() - " + vsDeviceName);
      }
      vpController.setStatus(ControllerConsts.STATUS_INITIALIZED);
      
      Logger.getLogger().logDebug(
          "Starting controller " + vpController.getName());
      synchronized (vpController)
      {
        vpController.initiateStartup();
        vpController.startProcessingMessages();
      }
    }
    catch (Exception ex)
    {
      Logger.getLogger().logException(ex);
    }
  }

  /**
   * Starts multiple controllers.
   * 
   * <p><b>Details:</b> This method starts the controllers whose names are 
   * provided in the given list by calling {@link #startController(String)} for
   * each controller named in the list.</p>
   * 
   * @param ipControllers the list
   * 
   * @see #startController(String)
   */
  public static synchronized void startControllers(List<String> ipControllers)
  {
    for (String vsController: ipControllers)
      startController(vsController);
  }

  /**
   * Starts all controllers.
   * 
   * <p><b>Details:</b> This method attempts to start every controller listed
   * in the CLC.</p>  
   */
  public static synchronized void startAllControllers()
  {
    try
    {
      List<String> vpNames = gpClc.listControllerNames();
      startControllers(vpNames);
    }
    catch (ControllerConfigurationException ex)
    {
      Logger.getLogger().logException(ex);
    }
  }

  /**
   * Stops controller.
   * 
   * <p><b>Details:</b> This method stops and destroys the named controller.  If
   * the named controller does not exist or is already stopped, this method does
   * nothing and returns without complaining.</p>
   * 
   * @param isController the controller's name
   * 
   * @see #stopControllers(List)
   * @see #stopAllControllers()
   */
  public static synchronized void stopController(String isController)
  {
    Controller vpController = gpControllers.get(isController);
    if (vpController instanceof DaemonController)
      return;
    if (vpController == null)
      return;
    MessageService vpMessageService;
    synchronized (vpController)
    {
      vpController.initiateShutdown();
      vpMessageService = vpController.getMessageService();
      vpController.setMessageService(null);
      vpController.setLogger(null);
    }
    vpMessageService.shutdown();
    gpControllers.put(isController, null);
  }
  
  /**
   * Stops multiple controllers.
   * 
   * <p><b>Details:</b> This method stops the controllers whose names are 
   * provided in the given list by calling {@link #stopController(String)} for 
   * each controller named in the list.</p>
   * 
   * @param ipControllers
   * 
   * @see #stopController(String)
   */
  public static synchronized void stopControllers(List<String> ipControllers)
  {
    for (String vsController: ipControllers)
      stopController(vsController);
  }

  /**
   * Stops all controllers.
   * 
   * <p><b>Details:</b> This method stops all controllers listed in the CLC.
   * Controllers that are already stopped will remain stopped.</p>
   */
  public static synchronized void stopAllControllers()
  {
    try
    {
      List<String> vpNames = gpClc.listControllerNames();
      stopControllers(vpNames);
    }
    catch (ControllerConfigurationException ex)
    {
      Logger.getLogger().logException(ex);
    }
  }

}

