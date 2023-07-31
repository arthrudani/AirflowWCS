package com.daifukuoc.wrxj.custom.ebs.clc.database;

import com.daifukuamerica.wrxj.allocator.AllocationController;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.clc.ControllerConfigurationException;
import com.daifukuamerica.wrxj.clc.database.DatabaseControllerTypeDefinition;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuoc.wrxj.custom.ebs.host.communication.sac.SACIntegrator;
import com.daifukuoc.wrxj.custom.ebs.host.communication.sac.SACPort;
import com.daifukuoc.wrxj.custom.ebs.host.handler.HostExpectedReceiptMessageHandler;
import com.daifukuoc.wrxj.custom.ebs.host.handler.HostFlightDataUpdateMessageHandler;
import com.daifukuoc.wrxj.custom.ebs.host.handler.HostInvReqByWarehouseMessageHandler;
import com.daifukuoc.wrxj.custom.ebs.host.handler.HostInventoryReqByFlightMessageHandler;
import com.daifukuoc.wrxj.custom.ebs.host.handler.HostInventoryUpdateMessageHandler;
import com.daifukuoc.wrxj.custom.ebs.host.handler.HostRetrievalItemMessageHandler;
import com.daifukuoc.wrxj.custom.ebs.host.handler.HostRetrievalOrderMessageHandler;
import com.daifukuoc.wrxj.custom.ebs.integration.HostPlcIntegrator;
import com.daifukuoc.wrxj.custom.ebs.plc.PLCDevice;
import com.daifukuoc.wrxj.custom.ebs.plc.acp.ACPPort;
import com.daifukuoc.wrxj.custom.ebs.scheduler.plc.ACPScheduler;

public class EBSDatabaseControllerTypeDefinition extends DatabaseControllerTypeDefinition {
    // KR: for the PLC with new interface
    public static final String PLC_DEVICE_TYPE = "PLCDevice";
    public static final String PLC_PORT_TYPE = "PLCPort";

    public static final String HOST_EXPECTED_RECEIPT_MESSAGE_HANDLER_TYPE = "HostExpectedReceiptMessageHandler";
    
    public static final String HOST_FLIGHT_DATA_UPDATE_HANDLER_TYPE = "HostFlightDataUpdateMessageHandler";
    
    public static final String HOST_INVENTORY_UPDATE_HANDLER_TYPE = "HostInventoryUpdateMessageHandler";
    
    public static final String HOST_RETRIEVAL_ORDER_HANDLER_TYPE = "HostRetrievalOrderMessageHandler";

    public static final String HOST_RETRIEVAL_ITEM_HANDLER_TYPE = "HostRetrievalItemMessageHandler";
    
    public static final String HOST_INVENTORY_REQUEST_BY_WAREHOUSE_HANDLER_TYPE = "HostInvReqByWarehouseMessageHandler";
    
    public static final String HOST_INVENTORY_REQ_BY_FLIGHT_HANDLER_TYPE = "HostInventoryReqByFlightMessageHandler";

    public static final String HOST_PORT_TYPE = "HostPort";
    
    public static final String HOST_INTEGRATOR_TYPE = "HostIntegrator";
    
    public static final String HOST_PLC_INTEGRATOR_TYPE = "HostPlcIntegrator";

    public static final String BCSSCHEDULER_TYPE = "Scheduler"; // KR: Must be specified in the
                                                                   // ControlConfig->sControl column
    public static final String BCSALLOCATOR_TYPE = "BCSAllocator"; // KR: Must be specified in the
                                                                   // ControlConfig->sControl column

    public EBSDatabaseControllerTypeDefinition(String isIdentifier) {
        super(isIdentifier);
    }

    protected static Class<? extends Controller> getDefaultClass(String msIdentifier) {
        if (msIdentifier.equals(BCSALLOCATOR_TYPE))
            return Factory.getImplementation(AllocationController.class);

        // KR: added new types
        if (msIdentifier.equals(PLC_DEVICE_TYPE))
            return Factory.getImplementation(PLCDevice.class);
        if (msIdentifier.equals(PLC_PORT_TYPE))
            return Factory.getImplementation(ACPPort.class);
//            return Factory.getImplementation(PLCPort.class);

        // KR: adding device schduler : Not necessary if it is in the ControlConfig
         if (msIdentifier.equals(BCSSCHEDULER_TYPE))
        	 return Factory.getImplementation(ACPScheduler.class);
        // return Factory.getImplementation(AGCScheduler.class); //

        if (msIdentifier.equals(PLC_DEVICE_TYPE)) {
            return Factory.getImplementation(PLCDevice.class);
        }

        if (msIdentifier.equals(HOST_EXPECTED_RECEIPT_MESSAGE_HANDLER_TYPE)) {
            return Factory.getImplementation(HostExpectedReceiptMessageHandler.class);
        }
        
        if (msIdentifier.equals(HOST_INVENTORY_UPDATE_HANDLER_TYPE)) {
            return Factory.getImplementation(HostInventoryUpdateMessageHandler.class);
        }
        
        if (msIdentifier.equals(HOST_FLIGHT_DATA_UPDATE_HANDLER_TYPE)) {
            return Factory.getImplementation(HostFlightDataUpdateMessageHandler.class);
        }
        
        if (msIdentifier.equals(HOST_RETRIEVAL_ORDER_HANDLER_TYPE)) {
            return Factory.getImplementation(HostRetrievalOrderMessageHandler.class);
        }
        
        if (msIdentifier.equals(HOST_INVENTORY_REQ_BY_FLIGHT_HANDLER_TYPE)) {
            return Factory.getImplementation(HostInventoryReqByFlightMessageHandler.class);
        }
        
        if (msIdentifier.equals(HOST_RETRIEVAL_ITEM_HANDLER_TYPE)) {
            return Factory.getImplementation(HostRetrievalItemMessageHandler.class);
        }
        
        if (msIdentifier.equals(HOST_PORT_TYPE)) {
            return Factory.getImplementation(SACPort.class);
        }
        
        if (msIdentifier.equals(HOST_INTEGRATOR_TYPE)) {
            return Factory.getImplementation(SACIntegrator.class);
        }
        
        if (msIdentifier.equals(HOST_PLC_INTEGRATOR_TYPE)) {
            return Factory.getImplementation(HostPlcIntegrator.class);
        }
        if (msIdentifier.equals(HOST_INVENTORY_REQUEST_BY_WAREHOUSE_HANDLER_TYPE)) {
            return Factory.getImplementation(HostInvReqByWarehouseMessageHandler.class);
        }

        // Use the parent class' method for other types
        return DatabaseControllerTypeDefinition.getDefaultClass(msIdentifier);
    }

    /**
     * To use overriden EBS's controller list definition, it's necessary to override this method too
     */
    @Override
    public Class<? extends Controller> getImplementingClass()
            throws ControllerConfigurationException, ClassNotFoundException {

        String msIdentifier = getIdentifier();
        String vsPropertyName = "ControllerConfig." + msIdentifier + ".class";
        String vsClass = Application.getString(vsPropertyName);
        if (vsClass == null) {
            Class<? extends Controller> vtClass = getDefaultClass(msIdentifier);
            if (vtClass == null) {
                throw new ControllerConfigurationException("No class for type " + msIdentifier + ".");
            }
            return vtClass;
        }

        try {
            Class<?> vtClass = Class.forName(vsClass);
            return vtClass.asSubclass(Controller.class);
        } catch (ClassNotFoundException ex) {
            // If not found, use the parent class' method for other types
            return super.getImplementingClass();
        }
    }
}
