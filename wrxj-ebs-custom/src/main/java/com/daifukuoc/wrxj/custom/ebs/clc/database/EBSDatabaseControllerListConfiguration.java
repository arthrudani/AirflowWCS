package com.daifukuoc.wrxj.custom.ebs.clc.database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.clc.ControllerConfigurationException;
import com.daifukuamerica.wrxj.clc.ControllerDefinition;
import com.daifukuamerica.wrxj.clc.ControllerTypeDefinition;
import com.daifukuamerica.wrxj.clc.database.DatabaseControllerDefinition;
import com.daifukuamerica.wrxj.clc.database.DatabaseControllerListConfiguration;
import com.daifukuamerica.wrxj.clc.database.DatabaseControllerTypeDefinition;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSDBConstants;
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

public class EBSDatabaseControllerListConfiguration extends DatabaseControllerListConfiguration {
    private Logger mpLogger = Logger.getLogger();

    public EBSDatabaseControllerListConfiguration(String isConfigName) throws ControllerConfigurationException {
        super(isConfigName);

        // Register host message handlers
        addHostMessageHandlers();
    }

    @Override
    public ControllerTypeDefinition getControllerTypeDefinition(String isIdentifier)
            throws ControllerConfigurationException {
        return (ControllerTypeDefinition) new EBSDatabaseControllerTypeDefinition(isIdentifier);
    }

    private void addHostMessageHandlers() {
        HostConfig vpHostCfg = Factory.create(HostConfig.class);
        try {
            if (mpConfigServ.isSplitSystem() && !mpConfigServ.isThisPrimaryJVM()) {
                return;
            }

            String[] vasHostControllers = vpHostCfg.getControllerNames();
            for (String vsHostController : vasHostControllers) {
                Map<String, String> vpProps = vpHostCfg.getControllerConfigurations(vsHostController);
                if (vsHostController
                        .startsWith(EBSDatabaseControllerTypeDefinition.HOST_EXPECTED_RECEIPT_MESSAGE_HANDLER_TYPE)) {
                    DatabaseControllerDefinition vpDef = new EBSDatabaseControllerDefinition(vsHostController,
                            EBSDatabaseControllerTypeDefinition.HOST_EXPECTED_RECEIPT_MESSAGE_HANDLER_TYPE, vpProps);
                    mpDefinitionList.put(vpDef.getName(), vpDef);
                } else if (vsHostController
                        .startsWith(EBSDatabaseControllerTypeDefinition.HOST_FLIGHT_DATA_UPDATE_HANDLER_TYPE)) {
                    DatabaseControllerDefinition vpDef = new EBSDatabaseControllerDefinition(vsHostController,
                            EBSDatabaseControllerTypeDefinition.HOST_FLIGHT_DATA_UPDATE_HANDLER_TYPE, vpProps);
                    mpDefinitionList.put(vpDef.getName(), vpDef);
                } else if (vsHostController
                        .startsWith(EBSDatabaseControllerTypeDefinition.HOST_INVENTORY_UPDATE_HANDLER_TYPE)) {
                    DatabaseControllerDefinition vpDef = new EBSDatabaseControllerDefinition(vsHostController,
                            EBSDatabaseControllerTypeDefinition.HOST_INVENTORY_UPDATE_HANDLER_TYPE, vpProps);
                    mpDefinitionList.put(vpDef.getName(), vpDef);
                } else if (vsHostController
                        .startsWith(EBSDatabaseControllerTypeDefinition.HOST_RETRIEVAL_ORDER_HANDLER_TYPE)) {
                    DatabaseControllerDefinition vpDef = new EBSDatabaseControllerDefinition(vsHostController,
                            EBSDatabaseControllerTypeDefinition.HOST_RETRIEVAL_ORDER_HANDLER_TYPE, vpProps);
                    mpDefinitionList.put(vpDef.getName(), vpDef);
                } else if (vsHostController
                            .startsWith(EBSDatabaseControllerTypeDefinition.HOST_RETRIEVAL_ITEM_HANDLER_TYPE)) {
                        DatabaseControllerDefinition vpDef = new EBSDatabaseControllerDefinition(vsHostController,
                                EBSDatabaseControllerTypeDefinition.HOST_RETRIEVAL_ITEM_HANDLER_TYPE, vpProps);
                        mpDefinitionList.put(vpDef.getName(), vpDef);
                } else if (vsHostController
                        .startsWith(EBSDatabaseControllerTypeDefinition.HOST_INVENTORY_REQ_BY_FLIGHT_HANDLER_TYPE)) {
                    DatabaseControllerDefinition vpDef = new EBSDatabaseControllerDefinition(vsHostController,
                            EBSDatabaseControllerTypeDefinition.HOST_INVENTORY_REQ_BY_FLIGHT_HANDLER_TYPE, vpProps);
                    mpDefinitionList.put(vpDef.getName(), vpDef);                        
                } else if (vsHostController
                        .startsWith(EBSDatabaseControllerTypeDefinition.HOST_PORT_TYPE)) {
                    DatabaseControllerDefinition vpDef = new EBSDatabaseControllerDefinition(vsHostController,
                            EBSDatabaseControllerTypeDefinition.HOST_PORT_TYPE, vpProps);
                    mpDefinitionList.put(vpDef.getName(), vpDef);
                } else if (vsHostController
                        .startsWith(EBSDatabaseControllerTypeDefinition.HOST_INTEGRATOR_TYPE)) {
                    DatabaseControllerDefinition vpDef = new EBSDatabaseControllerDefinition(vsHostController,
                            EBSDatabaseControllerTypeDefinition.HOST_INTEGRATOR_TYPE, vpProps);
                    mpDefinitionList.put(vpDef.getName(), vpDef);
                } else if (vsHostController
                        .startsWith(EBSDatabaseControllerTypeDefinition.HOST_PLC_INTEGRATOR_TYPE)) {
                    DatabaseControllerDefinition vpDef = new EBSDatabaseControllerDefinition(vsHostController,
                            EBSDatabaseControllerTypeDefinition.HOST_PLC_INTEGRATOR_TYPE, vpProps);
                    mpDefinitionList.put(vpDef.getName(), vpDef);
                } else if (vsHostController
                        .startsWith(EBSDatabaseControllerTypeDefinition.HOST_INVENTORY_REQUEST_BY_WAREHOUSE_HANDLER_TYPE)) {
                    DatabaseControllerDefinition vpDef = new EBSDatabaseControllerDefinition(vsHostController,
                            EBSDatabaseControllerTypeDefinition.HOST_INVENTORY_REQUEST_BY_WAREHOUSE_HANDLER_TYPE, vpProps);
                    mpDefinitionList.put(vpDef.getName(), vpDef);
                } else {
                    mpLogger.logError(vsHostController + " is not a host message handler");
                }
            }
        } catch (DBException e) {
            mpLogger.logException("Error loading host message handlers.", e);
        }
    }

    protected void addDeclaredControllers() {
        Set<String> vpRawNames = Application.getPropertyNames(Application.CONTROLLERCFG_DOMAIN);
        for (String vsRawName : vpRawNames) {
            int vnDot2 = vsRawName.indexOf('.', Application.CONTROLLERCFG_DOMAIN.length());
            if (vnDot2 < 0)
                continue;
            vsRawName = vsRawName.substring(Application.CONTROLLERCFG_DOMAIN.length(), vnDot2);
            String vsTypePropertyName = Application.CONTROLLERCFG_DOMAIN + vsRawName + ".type";
            String vsType = Application.getString(vsTypePropertyName);
            if (vsType == null) {
                continue;
            } else if (vsType.equals(DatabaseControllerTypeDefinition.AGV_TYPE)
                    && !Application.getBoolean("AGVEnabled", false)) {
                continue;
            }

            // MCM, IKEA 2017July12
            // Check if Controller is assigned to a JVM, for IKEA this should only apply to the OPC Controllers
            String vsAssignedToJVM = "";
            try {
                vsAssignedToJVM = mpConfigServ.getControllerConfigPropertyValue(vsRawName, "AssignedToJVM");

                if (vsAssignedToJVM != null) {
                    String vsJVMID = Application.getString(SKDCConstants.JVM_IDENTIFIER_KEY);
                    String vsJMSTopic = Application.getString(SKDCConstants.JVM_JMSTOPIC_KEY);

                    // if the controller is not assigned to this JVM, continue
                    if (!vsJVMID.equals(vsAssignedToJVM))
                        continue;
                }
            } catch (DBException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            mpDefinitionList.put(vsRawName, new EBSDatabaseControllerDefinition(vsRawName, vsType));
        }
    }

    /**
     * Add any controllers that can be determined from the device records to our list of controllers
     * 
     * @param isCtlrs List of String to which device controller names will be added.
     */
    protected void addDeviceCtlrs() throws ControllerConfigurationException {
        if (!DBObject.isWRxJConnectionActive()) {
            throw new ControllerConfigurationException(
                    "No database connection " + "available to get Controller devices");
        }

        List<Map> vpDevices;
        try {
            vpDevices = mpDevServ.getCtlrDevices();
        } catch (DBException ex) {
            throw new ControllerConfigurationException(ex);
        }

        // MCM, IKEA 2017July11
        // Check size of vpDevices, it now could be empty
        // because OPC Controllers don't have device entries
        if (vpDevices == null || vpDevices.isEmpty()) {
            return;
        }

        DeviceData vpDD = Factory.create(DeviceData.class);
        for (Map vpMap : vpDevices) {
            vpDD.dataToSKDCData(vpMap);
            // Add the device to the list
            String vsDevice = vpDD.getDeviceID();
            // Create the device's controller definition
            Map<String, String> vpProps = new HashMap<String, String>();

            vpProps.put(Controller.DEVICE_ID, vsDevice);

            /*
             * Port 1 is SEND/RECEIVE Port 2 is RECEIVE only
             */
            vpProps.put(Controller.DEVICE_PORT, vpDD.getCommSendPort());
            if (!vpDD.getCommReadPort().equals(""))
                vpProps.put(Controller.DEVICE_PORT2, vpDD.getCommReadPort());

            int vnType = vpDD.getDeviceType();
            String vsType = null;
            if (vnType == DBConstants.SRC5 || vnType == DBConstants.AGC) {
                vsType = DatabaseControllerTypeDefinition.SRC_TYPE;
            } else if (vnType == DBConstants.ARC100 || vnType == DBConstants.SRC9Y) {
                vsType = DatabaseControllerTypeDefinition.ARC_TYPE;
            } else if (vnType == DBConstants.MOS_DEVICE) {
                vsType = DatabaseControllerTypeDefinition.MOS_TYPE;
            } else if (vnType == DBConstants.SRC9X || vnType == DBConstants.AGC9X) {
                vsType = DatabaseControllerTypeDefinition.ARC_TYPE;
            } else if (vnType == DBConstants.SCALE) {
                vsType = DatabaseControllerTypeDefinition.SCALE_TYPE;

            } else if (vnType == EBSDBConstants.PLC) // KR: added PLC devices for new interface
            {
                vsType = EBSDatabaseControllerTypeDefinition.PLC_DEVICE_TYPE;
            }
            // If vsType is null, DON'T add the controllers. Let such devices be
            // configured by the ControllerConfig
            if (vsType != null) {
                mpDefinitionList.put(vsDevice, new EBSDatabaseControllerDefinition(vsDevice, vsType, vpProps));
            }
            // Add any additional controllers that may be needed
            addAdditionalControllers(vpDD);
        }
    }

    /*
     * protected void addEmulatedDeviceCtlrs() throws ControllerConfigurationException { if
     * (!DBObject.isWRxJConnectionActive()) { throw new ControllerConfigurationException("No database connection " +
     * "available to get Controller devices"); }
     * 
     * List<Map> vpDevices; try { vpDevices = mpDevServ.getCtlrDevices(); } catch(DBException exc) { throw new
     * ControllerConfigurationException(exc); }
     * 
     * DeviceData vpDD = Factory.create(DeviceData.class); for (Map vpMap : vpDevices) { vpDD.dataToSKDCData(vpMap);
     * vpDD.setEmulationMode(DBConstants.FULLEMU); switch (vpDD.getDeviceType()) { case EBSDBConstants.BCSDEVICE :
     * addBCSEmulationControllers(vpDD); break; } super.addEmulatedDeviceCtlrs(); } }
     */

    /**
     * This adds Controllers for ports and emulators to the controller list if needed.
     * 
     * @param ipDD <code>DeviceData</code> for which we are checking
     * @param ipControllerList String list of controllers that will be added to.
     */
    protected void addAdditionalControllers(DeviceData ipDD) {
        int vnType = ipDD.getDeviceType();
        switch (vnType) {
        // case EBSDBConstants.BCSPLCDEVICE: //KR: PLC devices handling new interface
        case EBSDBConstants.PLC:
            addBCSPortController(ipDD, EBSDatabaseControllerTypeDefinition.PLC_PORT_TYPE);
            addBCSScheduler(ipDD); //KR: no customization so it creates default Scheduler.
            addBCSAllocator(ipDD);

            break;
        }
        super.addAdditionalControllers(ipDD);

    }

    private void updatePortController(DeviceData ipDD, String isType) {
        if (ipDD == null)
            return;
        String vsDevice = ipDD.getDeviceID();
        // Add the device's (write-only or bi-directional) port to the list
        String vsPort = ipDD.getCommSendPort();
        if (vsPort != null && !vsPort.equals("")) {
            mpDefinitionList.put(vsPort, new EBSDatabaseControllerDefinition(vsPort, isType));
        } else
            System.err.println("Warning: Could not find port for device " + vsDevice);

        // Check for a second (read-only) port
        vsPort = ipDD.getCommReadPort();
        if (vsPort != null && !vsPort.equals(""))
            mpDefinitionList.put(vsPort, new EBSDatabaseControllerDefinition(vsPort, isType));
    }

    private void addBCSPortController(DeviceData ipDD, String isType) {
        if (ipDD == null)
            return;
        String vsDevice = ipDD.getDeviceID();
        // Add the device's (write-only or bi-directional) port to the list
        String vsPort = ipDD.getCommSendPort();
        if (vsPort != null && !vsPort.equals("")) {
            mpDefinitionList.put(vsPort, new EBSDatabaseControllerDefinition(vsPort, isType));
        } else
            System.err.println("Warning: Could not find port for device " + vsDevice);

        // Check for a second (read-only) port
        vsPort = ipDD.getCommReadPort();
        if (vsPort != null && !vsPort.equals(""))
            mpDefinitionList.put(vsPort, new EBSDatabaseControllerDefinition(vsPort, isType));
    }

    /**
     * Method creates BCS Emulator if Emulation is enabled
     * 
     * @param ipDD The scale Device
     * 
     *        private void addBCSEmulationControllers(DeviceData ipDD) { String vsDevice = ipDD.getDeviceID();
     * 
     *        // Find out if we are emulating the device if (ipDD.getEmulationMode() == DBConstants.FULLEMU) {
     *        Map<String, String> vpEmuProps = new HashMap<String, String>(); vpEmuProps.put(Controller.DEVICE_ID,
     *        vsDevice);
     * 
     *        // Get the emulator's port StandardPortServer vpPortServ = Factory.create(StandardPortServer.class);
     *        List<PortData> vpPortList = vpPortServ.getEmulatorPorts(vsDevice); PortData vpPD = null; if
     *        (vpPortList.size() > 0) { vpPD = vpPortList.get(0); } if (vpPD != null) {
     *        vpEmuProps.put(Controller.DEVICE_PORT, vpPD.getPortName()); mpDefinitionList.put(vpPD.getPortName(), new
     *        EBSDatabaseControllerDefinition(vpPD.getPortName(),
     *        EBSDatabaseControllerTypeDefinition.BCSDEVICEEMULATORPORT_TYPE)); } else { System.err.println("Warning:
     *        Could not find emulation port for device " + vsDevice); } // Finally, add the emulator itself String
     *        vsEmulator = vsDevice + Controller.EMULATOR; mpDefinitionList.put(vsEmulator, new
     *        EBSDatabaseControllerDefinition(vsEmulator, EBSDatabaseControllerTypeDefinition.BCSDEVICEEMULATOR_TYPE,
     *        vpEmuProps)); } }
     */

    /**
     * Add the device's scheduler if necessary - it may not be necessary if another device using the same scheduler has
     * already added it.
     * 
     * @param ipDD
     **/
    private void addBCSScheduler(DeviceData ipDD) {
        String vsScheduler = ipDD.getSchedulerName();
        if (vsScheduler != null && !vsScheduler.equals("") && !mpDefinitionList.containsKey(vsScheduler)) {
            // create the scheduler's definition
            Map<String, String> vpProps = new HashMap<String, String>();
            List<String> vpDevs = mpDevServ.getDevicesForScheduler(vsScheduler);
            String vsCollaborator = "";
            if (vpDevs.size() > 0) {
                vsCollaborator = vpDevs.remove(0);
                for (String vsDev : vpDevs) {
                    vsCollaborator += "," + vsDev;
                }
            }
            vpProps.put(Controller.COLLABORATOR, vsCollaborator);
            mpDefinitionList.put(vsScheduler, new EBSDatabaseControllerDefinition(vsScheduler,
                    EBSDatabaseControllerTypeDefinition.BCSSCHEDULER_TYPE, vpProps));
        }
    }

    /**
     * Add the device's allocation controller if necessary
     * 
     * @param ipDD
     */
    private void addBCSAllocator(DeviceData ipDD) {
        String vsAllocator = ipDD.getAllocatorName();
        if (vsAllocator != null && !vsAllocator.equals("") && !mpDefinitionList.containsKey(vsAllocator)) {
            Map<String, String> vpProps = new HashMap<String, String>();
            List<String> vpScheds = mpDevServ.getSchedulersForAllocator(vsAllocator);
            String vsCollaborator = "";
            if (vpScheds.size() > 0) {
                vsCollaborator = vpScheds.remove(0);
                for (String vsSched : vpScheds) {
                    vsCollaborator += "," + vsSched;
                }
            }
            vpProps.put(Controller.COLLABORATOR, vsCollaborator);
            mpDefinitionList.put(vsAllocator, new EBSDatabaseControllerDefinition(vsAllocator,
                    EBSDatabaseControllerTypeDefinition.BCSALLOCATOR_TYPE, vpProps));
        }
    }
}
