package com.daifukuamerica.wrxj.clc.database;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.clc.ControllerConfigurationException;
import com.daifukuamerica.wrxj.clc.ControllerDefinition;
import com.daifukuamerica.wrxj.clc.ControllerListConfiguration;
import com.daifukuamerica.wrxj.clc.ControllerTypeDefinition;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.dataserver.standard.StandardConfigurationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPortServer;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.dbadapter.data.HostConfig;
import com.daifukuamerica.wrxj.dbadapter.data.PortData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DatabaseControllerListConfiguration implements ControllerListConfiguration
{
  protected Map<String, DatabaseControllerDefinition> mpDefinitionList = new HashMap<String, DatabaseControllerDefinition>();
  protected StandardDeviceServer mpDevServ = Factory.create(StandardDeviceServer.class);
  protected StandardConfigurationServer mpConfigServ = Factory.create(StandardConfigurationServer.class);
  Logger mpLogger = Logger.getLogger();
  
  public DatabaseControllerListConfiguration(String isConfigName) throws ControllerConfigurationException
  {
    if (isConfigName.equals(CLC_EMULATORS))
    {
      addStandardCtlrs();
      addEmulatedDeviceCtlrs();
    }
    else if (!isConfigName.equals(CLC_CLIENT))
    {
      addStandardCtlrs();
      addDeviceCtlrs();
      addDeclaredControllers();
    }
  }

  @Override
  public List<String> listControllerNames() throws ControllerConfigurationException
  {
    // Only initialize it the first time
    List<String> vpNames = new ArrayList<String>(mpDefinitionList.keySet());
    Collections.sort(vpNames);
    return vpNames;
  }

  @Override
  public ControllerDefinition getControllerDefinition(String isName) throws ControllerConfigurationException
  {
    ControllerDefinition vpDef = mpDefinitionList.get(isName);
    if(vpDef == null)
    {
      throw new ControllerConfigurationException("Controller " + isName + " doesn't exist.");
    }
    return vpDef;
  }

  @Override
  public ControllerTypeDefinition getControllerTypeDefinition(String isIdentifier) throws ControllerConfigurationException
  {
    return new DatabaseControllerTypeDefinition(isIdentifier);
  }
  
  /**
   * Add the standard server mode controllers.  These controllers will always
   * be started in server mode, but never started in client mode.
   *
   */
  private void addStandardCtlrs()
  {
    DatabaseControllerDefinition vpDef;
    // TODO: This should be somewhere else once the property refactoring is done
    Map<String,String> vpProps = new HashMap<String, String>();
    vpProps.put("SiteEquipment", Application.getString("SiteEquipment"));
    vpDef = new DatabaseControllerDefinition(SKDCConstants.SYSTEM_HEALTH_MONITOR, 
        DatabaseControllerTypeDefinition.MONITOR_TYPE, vpProps);
    mpDefinitionList.put(SKDCConstants.SYSTEM_HEALTH_MONITOR, vpDef);
    vpDef = new DatabaseControllerDefinition(SKDCConstants.LOG_SERVER, 
        DatabaseControllerTypeDefinition.LOGSERV_TYPE);
    mpDefinitionList.put(SKDCConstants.LOG_SERVER, vpDef);
    vpDef = new DatabaseControllerDefinition(SKDCConstants.CONTROLLER_SERVER, 
        DatabaseControllerTypeDefinition.CTLSERV_TYPE);
    mpDefinitionList.put(SKDCConstants.CONTROLLER_SERVER, vpDef);
    
    // The host may or may not be turned on
    boolean vzUseHost = Application.getBoolean("HostSystemEnabled");
    if (vzUseHost)
      addHostCtlrs();
  }
  
  /**
   * Add any controllers that can be determined from the device records to our list of controllers
   * @param isCtlrs List of String to which device controller names will be added.
   */
  protected void addDeviceCtlrs() throws ControllerConfigurationException
  {
    if (!DBObject.isWRxJConnectionActive())
    {
      throw new ControllerConfigurationException("No database connection " +
                                          "available to get Controller devices");
    }

    List<Map> vpDevices;
    try
    {
      vpDevices = mpDevServ.getCtlrDevices();
    }
    catch (DBException ex)
    {
      throw new ControllerConfigurationException(ex);
    }

    DeviceData vpDD = Factory.create(DeviceData.class);
    for (Map vpMap : vpDevices)
    {
      vpDD.dataToSKDCData(vpMap);
      // Add the device to the list
      String vsDevice = vpDD.getDeviceID();
      // Create the device's controller definition
      Map<String, String> vpProps = new HashMap<String, String>();
      
      vpProps.put(Controller.DEVICE_ID, vsDevice);
      
      /*
       * Port 1 is SEND/RECEIVE
       * Port 2 is RECEIVE only
       */
      vpProps.put(Controller.DEVICE_PORT, vpDD.getCommSendPort());
      if (!vpDD.getCommReadPort().equals(""))
        vpProps.put(Controller.DEVICE_PORT2, vpDD.getCommReadPort());
      
      int vnType = vpDD.getDeviceType();
      String vsType = null;
      if (vnType == DBConstants.SRC5 ||  vnType == DBConstants.AGC)
      {
        vsType = DatabaseControllerTypeDefinition.SRC_TYPE;
      }
      else if (vnType == DBConstants.ARC100 || vnType == DBConstants.SRC9Y)
      {
        vsType = DatabaseControllerTypeDefinition.ARC_TYPE;
      }
      else if (vnType == DBConstants.MOS_DEVICE)
      {
        vsType = DatabaseControllerTypeDefinition.MOS_TYPE;
      }
      else if(vnType == DBConstants.SRC9X || vnType == DBConstants.AGC9X)
      {
    	  vsType = DatabaseControllerTypeDefinition.ARC_TYPE;
      }
      else if(vnType == DBConstants.SCALE)
      {
      	vsType = DatabaseControllerTypeDefinition.SCALE_TYPE;
      }
      // If vsType is null, DON'T add the controllers.  Let such devices be
      // configured by the ControllerConfig
      if (vsType != null)
      {
        mpDefinitionList.put(vsDevice, 
            new DatabaseControllerDefinition(vsDevice, vsType, vpProps));
      }
      // Add any additional controllers that may be needed
      addAdditionalControllers(vpDD);
    }
  }
  
  protected void addEmulatedDeviceCtlrs() throws ControllerConfigurationException
  {
    if (!DBObject.isWRxJConnectionActive())
    {
      throw new ControllerConfigurationException("No database connection " +
                                          "available to get Controller devices");
    }

    List<Map> vpDevices;
    try
    {
      vpDevices = mpDevServ.getCtlrDevices();
    }
    catch(DBException exc)
    {
      throw new ControllerConfigurationException(exc);
    }

    DeviceData vpDD = Factory.create(DeviceData.class);
    for (Map vpMap : vpDevices)
    {
      vpDD.dataToSKDCData(vpMap);
      vpDD.setEmulationMode(DBConstants.FULLEMU);
      switch (vpDD.getDeviceType())
      {
        case DBConstants.SRC5 :
        case DBConstants.AGC :
          addAGCEmulationControllers(vpDD);
          break;
        case DBConstants.ARC100 :
        case DBConstants.SRC9Y :        
          addARCEmulationControllers(vpDD);
          break;
        case DBConstants.SRC9X :
        case DBConstants.AGC9X :
          add9xEmulationControllers(vpDD);
          break;
        case DBConstants.SCALE :
          addScaleEmulationControllers(vpDD);
          break;
        default:
          return;
      }
    }
  }
  
  /**
   * This adds Controllers for ports and emulators to the controller list if needed.
   * 
   * @param ipDD <code>DeviceData</code> for which we are checking
   * @param ipControllerList String list of controllers that will be added to.
   */
  protected void addAdditionalControllers(DeviceData ipDD)
  {
    int vnType = ipDD.getDeviceType();
    switch(vnType)
    {
      case DBConstants.SRC5 :
        addMosControllers(ipDD);
      case DBConstants.AGC :
        addPortController(ipDD, DatabaseControllerTypeDefinition.PORT_TYPE);
        addAGCEmulationControllers(ipDD);
        addScheduler(ipDD);
        addAllocator(ipDD);
        break;
      case DBConstants.ARC100 :
      case DBConstants.SRC9Y :        
        addPortController(ipDD, DatabaseControllerTypeDefinition.ARCPORT_TYPE);
        addARCEmulationControllers(ipDD);
        addScheduler(ipDD);
        addAllocator(ipDD);
        break;
      case DBConstants.SRC9X :
      case DBConstants.AGC9X :
    	addPortController(ipDD, DatabaseControllerTypeDefinition.ARC9XPORT_TYPE);
    	add9xEmulationControllers(ipDD);
    	addScheduler(ipDD);
    	addAllocator(ipDD);
    	break;
      case DBConstants.SCALE :
      	addPortController(ipDD, DatabaseControllerTypeDefinition.SCALEPORT_TYPE);
      	addScaleEmulationControllers(ipDD);
      	break;
      default:
        return;
    }
  }

  private void addMosControllers(DeviceData ipDD)
  {
    if (ipDD == null) return;

    // Mos Device
    String vsMosDevice = ipDD.getMosDevice();
    Map<String,String> vpProps = new HashMap<String,String>();
    vpProps.put(Controller.DEVICE_ID, vsMosDevice);
    vpProps.put(Controller.DEVICE_PORT, ipDD.getMosPortName());
    mpDefinitionList.put(vsMosDevice, new DatabaseControllerDefinition(
        vsMosDevice, DatabaseControllerTypeDefinition.MOS_TYPE, vpProps));
    
    // Mos Port
    String vsPort = ipDD.getMosPortName();
    mpDefinitionList.put(vsPort, new DatabaseControllerDefinition(vsPort,
        DatabaseControllerTypeDefinition.PORT_TYPE));
  }
  
  private void addPortController(DeviceData ipDD, String isType)
  {
    if (ipDD == null) return;
    String vsDevice = ipDD.getDeviceID();
    // Add the device's (write-only or bi-directional) port to the list
    String vsPort = ipDD.getCommSendPort();
    if (vsPort != null && !vsPort.equals(""))
    {
      mpDefinitionList.put(vsPort, new DatabaseControllerDefinition(vsPort, isType));
    }
    else
      System.err.println("Warning: Could not find port for device " + vsDevice);  
    
    // Check for a second (read-only) port
    vsPort = ipDD.getCommReadPort();
    if (vsPort != null && !vsPort.equals(""))
      mpDefinitionList.put(vsPort, new DatabaseControllerDefinition(vsPort, isType));
  }
  
  private void addAGCEmulationControllers(DeviceData ipDD)
  {
    String vsDevice = ipDD.getDeviceID();
    
    //  Find out if we are emulating the device
    if (ipDD.getEmulationMode() == DBConstants.FULLEMU)
    {
      Map<String,String> vpEmuProps = new HashMap<String,String>();
      vpEmuProps.put(Controller.DEVICE_ID, vsDevice);
        
      // Get the emulator's port
      StandardPortServer vpPortServ = Factory.create(StandardPortServer.class);
      List<PortData> vpPortList = vpPortServ.getEmulatorPorts(vsDevice);
      PortData vpPD = null;
      if (vpPortList.size() > 0)
      {
        vpPD = vpPortList.get(0);
      }
      if (vpPD != null)
      {
        vpEmuProps.put(Controller.DEVICE_PORT, vpPD.getPortName());
        mpDefinitionList.put(vpPD.getPortName(),
            new DatabaseControllerDefinition(vpPD.getPortName(),
                DatabaseControllerTypeDefinition.PORT_TYPE));
      }
      else
      {
        System.err.println("Warning: Could not find emulation port for device " + vsDevice);
      }
      
      // Check for a MOS emulator port as well
      String vsCommDevice = ipDD.getMosDevice();
      if (!vsCommDevice.equals(""))
      {
        vpEmuProps.put(Controller.MOS_DEVICE, vsCommDevice);
        List<PortData> vpEmuPortList = vpPortServ.getEmulatorPorts(vsCommDevice);
        PortData vpMosPD = null;
        if (vpEmuPortList.size() > 0)
          vpMosPD = vpEmuPortList.get(0);
        if (vpMosPD != null)
        {
          vpEmuProps.put(Controller.DEVICE_PORT2, vpMosPD.getPortName());
          mpDefinitionList.put(vpMosPD.getPortName(), 
              new DatabaseControllerDefinition(vpMosPD.getPortName(), 
                  DatabaseControllerTypeDefinition.PORT_TYPE));
        }
        else
        {
          System.out.println("No emulation port for " + vsCommDevice);
        }
      }
      
      // Add a Station simulator for this device
      if (Application.getString("SimulationEnabled", "NO").equals("YES"))
      {
        String vsSimName = vsDevice+"-"+Controller.SIMULATOR;
        Map<String,String> vpSimProps = new HashMap<String,String>();
        vpSimProps.put(Controller.DEVICE_ID, vsDevice);
        vpSimProps.put(Controller.DEVICE_PORT, ipDD.getCommSendPort());
        mpDefinitionList.put(vsSimName, new DatabaseControllerDefinition(vsSimName, DatabaseControllerTypeDefinition.SIMULATOR_TYPE, vpSimProps));
      }

      // Finally, add the emulator itself
      String vsEmulator = vsDevice + Controller.EMULATOR;
      mpDefinitionList.put(vsEmulator, new DatabaseControllerDefinition(vsEmulator, DatabaseControllerTypeDefinition.EMULATOR_TYPE, vpEmuProps));
    }
  }
  
  /** 
   * Method creates Scale Emulator if Emulation is enabled
   * @param ipDD The scale Device
   */
  private void addScaleEmulationControllers(DeviceData ipDD)
  {
    String vsDevice = ipDD.getDeviceID();

    // Find out if we are emulating the device
    if (ipDD.getEmulationMode() == DBConstants.FULLEMU)
    {
      Map<String, String> vpEmuProps = new HashMap<String, String>();
      vpEmuProps.put(Controller.DEVICE_ID, vsDevice);

      // Get the emulator's port
      StandardPortServer vpPortServ = Factory.create(StandardPortServer.class);
      List<PortData> vpPortList = vpPortServ.getEmulatorPorts(vsDevice);
      PortData vpPD = null;
      if (vpPortList.size() > 0)
      {
        vpPD = vpPortList.get(0);
      }
      if (vpPD != null)
      {
        vpEmuProps.put(Controller.DEVICE_PORT, vpPD.getPortName());
        mpDefinitionList.put(vpPD.getPortName(),
            new DatabaseControllerDefinition(vpPD.getPortName(),
                DatabaseControllerTypeDefinition.SCALEEMULATORPORT_TYPE));
      }
      else
      {
        System.err.println("Warning: Could not find emulation port for device "
            + vsDevice);
      }
      // Finally, add the emulator itself
      String vsEmulator = vsDevice + Controller.EMULATOR;
      mpDefinitionList.put(vsEmulator, new DatabaseControllerDefinition(vsEmulator, DatabaseControllerTypeDefinition.SCALEEMULATOR_TYPE, vpEmuProps));
    }
  }
  
  private void addARCEmulationControllers(DeviceData ipDD)
  {
    String vsDevice = ipDD.getDeviceID();
    
    //  Find out if we are emulating the device
    if (ipDD.getEmulationMode() == DBConstants.FULLEMU)
    {
      Map<String,String> vpEmuProps = new HashMap<String,String>();
      vpEmuProps.put(Controller.DEVICE_ID, vsDevice);
        
      // Get the emulator's port(s)
      StandardPortServer vpPortServ = Factory.create(StandardPortServer.class);
      List<PortData> vpList = vpPortServ.getEmulatorPorts(vsDevice);
      if (vpList.size() > 0)
      {
        for (PortData p : vpList)
        {
          vpEmuProps.put(p.getDirection() == DBConstants.INBOUND ? 
              Controller.DEVICE_PORT2 : Controller.DEVICE_PORT, p.getPortName());
          mpDefinitionList.put(p.getPortName(),
              new DatabaseControllerDefinition(p.getPortName(),
                  DatabaseControllerTypeDefinition.ARCPORT_TYPE));
        }
      }
      else
      {
        System.err.println("Warning: Could not find emulation port for device " + vsDevice);
      }
      
      // Add a Station simulator for this device
      if (Application.getString("SimulationEnabled", "NO").equals("YES"))
      {
        String vsSimName = vsDevice+"-"+Controller.SIMULATOR;
        Map<String,String> vpSimProps = new HashMap<String,String>();
        vpSimProps.put(Controller.DEVICE_ID, vsDevice);
        vpSimProps.put(Controller.DEVICE_PORT, ipDD.getCommSendPort());
        vpSimProps.put(Controller.DEVICE_PORT2, ipDD.getCommReadPort());
        mpDefinitionList.put(vsSimName, new DatabaseControllerDefinition(vsSimName, DatabaseControllerTypeDefinition.SIMULATOR_TYPE, vpSimProps));
      }

      // Finally, add the emulator itself
      String vsEmulator = vsDevice + SKDCConstants.EMULATION_SUFFIX;
      mpDefinitionList.put(vsEmulator, new DatabaseControllerDefinition(vsEmulator, DatabaseControllerTypeDefinition.ARCEMU_TYPE, vpEmuProps));
    }
  }
  private void add9xEmulationControllers(DeviceData ipDD)
  {
    String vsDevice = ipDD.getDeviceID();
    
    //  Find out if we are emulating the device
    if (ipDD.getEmulationMode() == DBConstants.FULLEMU)
    {
      Map<String,String> vpEmuProps = new HashMap<String,String>();
      vpEmuProps.put(Controller.DEVICE_ID, vsDevice);
        
      // Get the emulator's port(s)
      StandardPortServer vpPortServ = Factory.create(StandardPortServer.class);
      List<PortData> vpList = vpPortServ.getEmulatorPorts(vsDevice);
      if (vpList.size() > 0)
      {
        for (PortData p : vpList)
        {
          vpEmuProps.put(p.getDirection() == DBConstants.INBOUND ? 
              Controller.DEVICE_PORT2 : Controller.DEVICE_PORT, p.getPortName());
          mpDefinitionList.put(p.getPortName(),
              new DatabaseControllerDefinition(p.getPortName(),
                  DatabaseControllerTypeDefinition.ARC9XPORT_TYPE));
        }
      }
      else
      {
        System.err.println("Warning: Could not find emulation port for device " + vsDevice);
      }
      
      // Add a Station simulator for this device
      if (Application.getString("SimulationEnabled", "NO").equals("YES"))
      {
        String vsSimName = vsDevice+"-"+Controller.SIMULATOR;
        Map<String,String> vpSimProps = new HashMap<String,String>();
        vpSimProps.put(Controller.DEVICE_ID, vsDevice);
        vpSimProps.put(Controller.DEVICE_PORT, ipDD.getCommSendPort());
        vpSimProps.put(Controller.DEVICE_PORT2, ipDD.getCommReadPort());
        mpDefinitionList.put(vsSimName, new DatabaseControllerDefinition(vsSimName, DatabaseControllerTypeDefinition.SIMULATOR_TYPE, vpSimProps));
      }

      // Finally, add the emulator itself
      String vsEmulator = vsDevice + SKDCConstants.EMULATION_SUFFIX;
      mpDefinitionList.put(vsEmulator, new DatabaseControllerDefinition(vsEmulator, DatabaseControllerTypeDefinition.ARCEMU_TYPE, vpEmuProps));
    }
  }
  /**
   * Add the device's scheduler if necessary - it may not be necessary if
   * another device using the same scheduler has already added it.
   * @param ipDD
   */
  private void addScheduler(DeviceData ipDD)
  {
    String vsScheduler = ipDD.getSchedulerName();
    if (vsScheduler != null && !vsScheduler.equals("") && !mpDefinitionList.containsKey(vsScheduler))
    {
      // create the scheduler's definition
      Map<String,String> vpProps = new HashMap<String,String>();
      List<String> vpDevs = mpDevServ.getDevicesForScheduler(vsScheduler);
      String vsCollaborator = "";
      if (vpDevs.size() > 0)
      {
        vsCollaborator = vpDevs.remove(0);
        for (String vsDev : vpDevs)
        {
          vsCollaborator += "," + vsDev;
        }
      }
      vpProps.put(Controller.COLLABORATOR, vsCollaborator);
      mpDefinitionList.put(vsScheduler,
          new DatabaseControllerDefinition(vsScheduler,
              DatabaseControllerTypeDefinition.SCHEDULER_TYPE, vpProps));
    }
  }
  
  /**
   * Add the device's allocation controller if necessary
   * @param ipDD
   */
  private void addAllocator(DeviceData ipDD)
  {
    String vsAllocator = ipDD.getAllocatorName();
    if (vsAllocator != null && !vsAllocator.equals("") && !mpDefinitionList.containsKey(vsAllocator))
    {
      Map<String,String> vpProps = new HashMap<String,String>();
      List<String> vpScheds = mpDevServ.getSchedulersForAllocator(vsAllocator);
      String vsCollaborator = "";
      if (vpScheds.size() > 0)
      {
        vsCollaborator = vpScheds.remove(0);
        for (String vsSched : vpScheds)
        {
          vsCollaborator += "," + vsSched;
        }
      }
      vpProps.put(Controller.COLLABORATOR, vsCollaborator);
      //    TODO: This needs to be somewhere else - waiting for property refactoring.
      vpProps.put("ShortOrderProcessing", Application.getString("ShortOrderProcessing"));
      vpProps.put("CheckHungryStationsInterval", Application.getString("CheckHungryStationsInterval"));
      mpDefinitionList.put(vsAllocator, new DatabaseControllerDefinition(vsAllocator, DatabaseControllerTypeDefinition.ALLOCATOR_TYPE, vpProps)); 
    }
  }
  
  /**
   * Add controllers associated with the host along with their properties.
   */
  private void addHostCtlrs()
  {
    HostConfig vpHostCfg = Factory.create(HostConfig.class);
    try
    {
      if (mpConfigServ.isSplitSystem() && !mpConfigServ.isThisPrimaryJVM())
      {
        return;
      }

      String[] vasHostControllers = vpHostCfg.getControllerNames();
      for(String vsHostController : vasHostControllers)
      {
        Map<String, String> vpProps = vpHostCfg.getControllerConfigurations(vsHostController);
        if (vsHostController.startsWith(DatabaseControllerTypeDefinition.HOST_TYPE))
        {
          DatabaseControllerDefinition vpDef = new DatabaseControllerDefinition
          ( vsHostController,
            DatabaseControllerTypeDefinition.HOST_TYPE,
            vpProps
          );
          mpDefinitionList.put(vpDef.getName(), vpDef);
        }
        else if (vsHostController.startsWith(DatabaseControllerTypeDefinition.INTEGRATOR_TYPE))
        {
          DatabaseControllerDefinition vpDef = new DatabaseControllerDefinition
          ( vsHostController,
            DatabaseControllerTypeDefinition.INTEGRATOR_TYPE,
            vpProps
          );
          mpDefinitionList.put(vpDef.getName(), vpDef);
        }
        else
        {
          mpLogger.logError("Controller type is unknown for " + vsHostController);
        }
      }
    }
    catch(DBException e)
    {
      mpLogger.logException("Error loading Host related controller.", e);
    }
  }
  
  protected void addDeclaredControllers()
  {
    Set<String> vpRawNames = Application.getPropertyNames(Application.CONTROLLERCFG_DOMAIN);
    for (String vsRawName: vpRawNames)
    {
      int vnDot2 = vsRawName.indexOf('.', Application.CONTROLLERCFG_DOMAIN.length());
      if (vnDot2 < 0)
        continue;
      vsRawName = vsRawName.substring(Application.CONTROLLERCFG_DOMAIN.length(), vnDot2);
      String vsTypePropertyName = Application.CONTROLLERCFG_DOMAIN + vsRawName + ".type";
      String vsType = Application.getString(vsTypePropertyName);
      if (vsType == null)
      {
        continue;
      }
      else if (vsType.equals(DatabaseControllerTypeDefinition.AGV_TYPE) &&
               !Application.getBoolean("AGVEnabled", false))
      {
        continue;
      }
      DatabaseControllerDefinition vpDefinition = new DatabaseControllerDefinition(vsRawName, vsType);
      mpDefinitionList.put(vsRawName, vpDefinition);
    }
  }
}

