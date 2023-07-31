package com.daifukuamerica.wrxj.clc.database;

import com.daifukuamerica.wrxj.allocator.AllocationController;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.clc.ControllerConfigurationException;
import com.daifukuamerica.wrxj.clc.ControllerTypeDefinition;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.device.agc.AGCMOSDevice;
import com.daifukuamerica.wrxj.device.agc.AGCPort;
import com.daifukuamerica.wrxj.device.agc.AGCStationDevice;
import com.daifukuamerica.wrxj.device.agv.AGVController;
import com.daifukuamerica.wrxj.device.arc9y.Arc9xPort;
import com.daifukuamerica.wrxj.device.arc9y.Arc9yDevice;
import com.daifukuamerica.wrxj.device.arc9y.Arc9yPort;
import com.daifukuamerica.wrxj.device.controllerserver.ControllerServer;
import com.daifukuamerica.wrxj.device.logserver.LogServer;
import com.daifukuamerica.wrxj.device.monitor.SystemHealthMonitor;
import com.daifukuamerica.wrxj.device.scale.ScaleDevice;
import com.daifukuamerica.wrxj.device.scale.ScalePort;
import com.daifukuamerica.wrxj.emulation.agc.AGCDeviceEmulator;
import com.daifukuamerica.wrxj.emulation.arc9y.Arc9yDeviceEmulator;
import com.daifukuamerica.wrxj.emulation.scale.ScaleEmulator;
import com.daifukuamerica.wrxj.emulation.scale.ScaleEmulatorPort;
import com.daifukuamerica.wrxj.emulation.station.SimulationController;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.HostController;
import com.daifukuamerica.wrxj.host.HostMessageIntegrator;
import com.daifukuamerica.wrxj.scheduler.agc.AGCScheduler;
import com.daifukuamerica.wrxj.scheduler.event.TimedEventScheduler;
import com.daifukuamerica.wrxj.web.stomp.controller.WebMessageController;

public class DatabaseControllerTypeDefinition implements ControllerTypeDefinition
{
  public static final String MONITOR_TYPE = "SystemMonitor";
  public static final String LOGSERV_TYPE = "LogServer";
  public static final String CTLSERV_TYPE = "ControllerServer";

  public static final String EVENT_SCHED_TYPE = "TimedEventScheduler";
  public static final String ALLOCATOR_TYPE = "AllocationController";
  public static final String HOST_TYPE = "HostController";
  public static final String INTEGRATOR_TYPE = "HostMessageIntegrator";
  public static final String AGV_TYPE = "AGVController";

  public static final String SRC_TYPE = "SRC";
  public static final String MOS_TYPE = "MOS";
  public static final String ARC9XPORT_TYPE = "9xPort";
  public static final String SCHEDULER_TYPE = "AGCScheduler";
  public static final String PORT_TYPE = "AGCPort";
  public static final String EMULATOR_TYPE = "AGCEmulator";
  public static final String SIMULATOR_TYPE = "StationSimulator";
  public static final String ARC_TYPE = "ARC";
  public static final String ARCPORT_TYPE = "ARCPort";
  public static final String ARCEMU_TYPE = "ARCEmulator";
  public static final String SCALE_TYPE = "Scale";
  public static final String SCALEPORT_TYPE = "ScalePort";
  public static final String SCALEEMULATOR_TYPE = "ScaleEmulator";
  public static final String SCALEEMULATORPORT_TYPE = "ScaleEmulatorPort";

  final String msIdentifier;

  public DatabaseControllerTypeDefinition(String isIdentifier)
  {
    super();
    msIdentifier = isIdentifier;
  }

  public String getIdentifier()
  {
    return msIdentifier;
  }

  protected static Class<? extends Controller> getDefaultClass(String msIdentifier)
  {
    if (msIdentifier == null)
      return null;
    if (msIdentifier.equals(MONITOR_TYPE))
      return Factory.getImplementation(SystemHealthMonitor.class);
    if (msIdentifier.equals(LOGSERV_TYPE))
      return Factory.getImplementation(LogServer.class);
    if (msIdentifier.equals(CTLSERV_TYPE))
      return Factory.getImplementation(ControllerServer.class);
    if (msIdentifier.equals(SRC_TYPE))
      return Factory.getImplementation(AGCStationDevice.class);
    if (msIdentifier.equals(MOS_TYPE))
      return Factory.getImplementation(AGCMOSDevice.class);
    if (msIdentifier.equals(SCHEDULER_TYPE))
      return Factory.getImplementation(AGCScheduler.class);
    if (msIdentifier.equals(PORT_TYPE))
      return Factory.getImplementation(AGCPort.class);
    if (msIdentifier.equals(EMULATOR_TYPE))
      return Factory.getImplementation(AGCDeviceEmulator.class);
    if (msIdentifier.equals(SIMULATOR_TYPE))
      return Factory.getImplementation(SimulationController.class);
    if (msIdentifier.equals(EVENT_SCHED_TYPE))
      return Factory.getImplementation(TimedEventScheduler.class);
    if (msIdentifier.equals(ALLOCATOR_TYPE))
      return Factory.getImplementation(AllocationController.class);
    if (msIdentifier.equals(HOST_TYPE))
      return Factory.getImplementation(HostController.class);
    if (msIdentifier.equals(INTEGRATOR_TYPE))
      return Factory.getImplementation(HostMessageIntegrator.class);
    if (msIdentifier.equals(AGV_TYPE))
      return Factory.getImplementation(AGVController.class);
    if (msIdentifier.equals(ARC_TYPE))
      return Factory.getImplementation(Arc9yDevice.class);
    if (msIdentifier.equals(ARCPORT_TYPE))
      return Factory.getImplementation(Arc9yPort.class);
    if (msIdentifier.equals(ARCEMU_TYPE))
      return Factory.getImplementation(Arc9yDeviceEmulator.class);
    if (msIdentifier.equals(ARC9XPORT_TYPE))
      return Factory.getImplementation(Arc9xPort.class);
    if (msIdentifier.equals(SCALE_TYPE))
      return Factory.getImplementation(ScaleDevice.class);
    if (msIdentifier.equals(SCALEPORT_TYPE))
      return Factory.getImplementation(ScalePort.class);
    if (msIdentifier.equals(SCALEEMULATOR_TYPE))
      return Factory.getImplementation(ScaleEmulator.class);
    if (msIdentifier.equals(SCALEEMULATORPORT_TYPE))
      return Factory.getImplementation(ScaleEmulatorPort.class);
    return null;
  }

  public Class<? extends Controller> getImplementingClass() throws ControllerConfigurationException, ClassNotFoundException
  {
    String vsPropertyName = "ControllerConfig." + msIdentifier + ".class";
    String vsClass = Application.getString(vsPropertyName);
    if (vsClass == null)
    {
      Class<? extends Controller> vtClass = getDefaultClass(msIdentifier);
      if (vtClass == null)
        throw new ControllerConfigurationException("No class for type " + msIdentifier + ".");
      return vtClass;
    }
    try
    {
      Class<?> vtClass = Class.forName(vsClass);
      return vtClass.asSubclass(Controller.class);
    }
    catch (ClassNotFoundException ex)
    {
      throw new ControllerConfigurationException(ex);
    }
  }

  public String getDefaultProperty(String isName) throws ControllerConfigurationException
  {
    String vsPropertyName = "ControllerConfig." + isName + "." + isName;
    String vsPropertyValue = Application.getString(vsPropertyName);
    if (vsPropertyValue == null)
      throw new ControllerConfigurationException("Type " + msIdentifier + " does not have property " + isName + " defined");
    return vsPropertyValue;
  }

}

