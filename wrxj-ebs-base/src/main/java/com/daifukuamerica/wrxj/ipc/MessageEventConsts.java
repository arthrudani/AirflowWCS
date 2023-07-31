package com.daifukuamerica.wrxj.ipc;

import com.daifukuamerica.wrxj.controller.ControllerConsts;

/**
 * Invariants used by the inter-process-communication message service.
 *
 * @author Stephen Kendorski
 */
public interface MessageEventConsts
{

  /**
   * Event category for Controller management.
   */
  public static final int CONTROL_EVENT_TYPE = 0;

  /**
   * Event category for reporting Controller condition and state.
   */
  public static final int STATUS_EVENT_TYPE = 1;

  /**
   * Event category for reporting information/device updates.
   */
  public static final int UPDATE_EVENT_TYPE = 2;

  /**
   * Event category for handling load movement.
   */

  public static final int LOAD_EVENT_TYPE = 3;

  /**
   * Event category for handling load station condition and state.
   */

  public static final int STATION_EVENT_TYPE = 4;

  /**
   * Event category for handling data transfer to and from physical apparatus
   * external to the application.
   */
  public static final int EQUIPMENT_EVENT_TYPE = 5;

  /**
   * Event category for reporting ComPort condition and state.
   */
  public static final int COMM_EVENT_TYPE = 6;

  /**
   *  Event category for reporting Exceptions.
   */
  public static final int EXCEPTION_EVENT_TYPE = 7;

  /**
   *  Event category for requesting a Controller to publish a heartbeat response
   *  event.
   */
  public static final int HEARTBEAT_REQUEST_EVENT_TYPE = 8;

  /**
   *  Event category for a Controller to publish in response to a heartbeat
   *  request event.
   */
  public static final int HEARTBEAT_RESPONSE_EVENT_TYPE = 9;

  /**
   *  Event category for load movement supervision.
   */
  public static final int SCHEDULER_EVENT_TYPE = 10;

  /**
   * Event category for handling work requests.
   */
  public static final int ORDER_EVENT_TYPE = 11;

  /**
   * Event category for handling controller requests.
   */
  public static final int CONTROLLER_REQUEST_EVENT_TYPE = 12;

  /**
   * Event category for handling Allocation Event notification.
   */
  public static final int ALLOCATE_EVENT_TYPE = 13;
 
  /**
   * Event category for handling Project Specific Allocation notification. 
   */
  public static final int CUSTOM_ALLOCATION_EVENT_TYPE = 14;

  /**
   * Event category for handling Log Server data requests.
   */
  public static final int LOG_EVENT_TYPE = 15;

  /**
   * Event category for handling Host message receipt notification.
   */
  public static final int HOST_MESG_RECV_EVENT_TYPE = 16;

  /**
   * Event category for handling Host message send notification.
   */
  public static final int HOST_MESG_SEND_EVENT_TYPE = 17;

  /**
   * Event category for Conveyor communication
   */
  public static final int STATION_LOAD_EVENT_TYPE = 18;

  /**
   * Event for Allocation Probe.
   */
  public static final int ALLOCATION_PROBE_EVENT_TYPE = 19;

  /**
   * Event category for handling Project-Specific notification.
   */
  public static final int CUSTOM_EVENT_TYPE = 20;

  /**
   * Event category for handling AE Messenger communication.
   */
  public static final int AE_MESSENGER_EVENT_TYPE = 21;

  /**
   * Event category for handling expected receipt request message.
   */
  public static final int HOST_EXPECTED_RECEIPT_EVENT_TYPE = 22;
  
  /**
   * Event category for handling flight data update request message.
   */
  public static final int HOST_FLIGHT_DATA_UPDATE_EVENT_TYPE = 23;
  
  /**
   * Event category for handling retrieval order request message.
   */
  public static final int HOST_RETRIEVAL_ORDER_EVENT_TYPE = 24;
  
  /**
   * Event category for handling interaction from host to plc
   */
  public static final int HOST_TO_PLC_EVENT_TYPE = 25;
  
  /**
   * Event category for handling interaction from plc to host
   */
  public static final int PLC_TO_HOST_EVENT_TYPE = 26;
  
  /**
   * Event category for handling inventory update request message.
   */
  public static final int HOST_INVENTORY_UPDATE_EVENT_TYPE = 27;
  
  public static final int HOST_RETRIEVAL_ITEM_EVENT_TYPE = 28;
  
  
  public static final int HOST_INVENTORY_REQUEST_BY_FLIGHT_EVENT_TYPE = 29;

  /**
   * Event category for handling inventory request by warehouse message.
   */
  public static final int HOST_INVENTORY_REQUEST_BY_WAREHOUSE_EVENT_TYPE = 30;
  
  
  /**
   * Separator for sub-events within event categories.
   */
  public static final String SUB_EVENT_TEXT = "~";

  public static final String CONTROL_EVENT_TEXT = "Control";

  public static final String STATUS_EVENT_TEXT = "Status";

  public static final String UPDATE_EVENT_TEXT = "Update";

  public static final String LOAD_EVENT_TEXT = "Load";

  public static final String STATION_EVENT_TEXT = "Station";

  public static final String EQUIPMENT_EVENT_TEXT = "Eqpmnt";

  public static final String COMM_EVENT_TEXT = "Comm";

  public static final String EXCEPTION_EVENT_TEXT = "Excptn";

  public static final String HEARTBEAT_REQUEST_EVENT_TEXT = "HrtbtRqst";

  public static final String HEARTBEAT_RESPONSE_EVENT_TEXT = "HrtbtRspns";

  public static final String SCHEDULER_EVENT_TEXT = "Sched";

  public static final String ORDER_EVENT_TEXT = "Order";

  public static final String CONTROLLER_REQUEST_EVENT_TEXT = "CtlrRqst";

  public static final String ALLOCATE_EVENT_TEXT = "Allocate";

  public static final String CUSTOM_ALLOCATION_EVENT_TEXT = "CustomAllocation";

  public static final String LOG_EVENT_TEXT = "Log";

  public static final String HOST_MESG_RECV_EVENT_TEXT = "HostRecv";

  public static final String HOST_MESG_SEND_EVENT_TEXT = "HostSend";

  public static final String STATION_LOAD_EVENT_TEXT = "StationLoad";

  public static final String ALLOCATION_PROBE_EVENT_TEXT = "AllocationProbe";

  public static final String CUSTOM_EVENT_TEXT = "Custom";

  public static final String AE_MESSENGER_EVENT_TEXT = "AEMessenger";
  
  public static final String HOST_EXPECTED_RECEIPT_EVENT_TEXT = "HostExpectedReceipt";
  
  public static final String HOST_FLIGHT_DATA_UPDATE_EVENT_TEXT = "FlightDataUpdate";
  
  public static final String HOST_RETRIEVAL_ORDER_EVENT_TEXT = "RetrievalOrder";
  
  public static final String HOST_RETRIEVAL_ITEM_EVENT_TEXT = "RetrievalItem";
  
  public static final String HOST_TO_PLC_EVENT_TEXT = "HostToPlc";
  
  public static final String PLC_TO_HOST_EVENT_TEXT = "PlcToHost";
  
  public static final String HOST_INVENTORY_UPDATE_EVENT_TEXT = "InventoryUpdate";
  
  public static final String HOST_INVENTORY_REQUEST_BY_WAREHOUSE_EVENT_TEXT = "InventoryRequestByWarehouse";
  public static final String HOST_INVENTORY_REQUEST_EVENT_TEXT = "InventoryRequest";
  

  public static final String[] EVENT_TEXT = 
  { CONTROL_EVENT_TEXT, 
    STATUS_EVENT_TEXT, 
    UPDATE_EVENT_TEXT, 
    LOAD_EVENT_TEXT, 
    STATION_EVENT_TEXT, 
    EQUIPMENT_EVENT_TEXT, 
    COMM_EVENT_TEXT, 
    EXCEPTION_EVENT_TEXT, 
    HEARTBEAT_REQUEST_EVENT_TEXT, 
    HEARTBEAT_RESPONSE_EVENT_TEXT, 
    SCHEDULER_EVENT_TEXT, 
    ORDER_EVENT_TEXT, 
    CONTROLLER_REQUEST_EVENT_TEXT, 
    ALLOCATE_EVENT_TEXT,
    CUSTOM_ALLOCATION_EVENT_TEXT,
    LOG_EVENT_TEXT, 
    HOST_MESG_RECV_EVENT_TEXT, 
    HOST_MESG_SEND_EVENT_TEXT, 
    STATION_LOAD_EVENT_TEXT, 
    ALLOCATION_PROBE_EVENT_TEXT,
    CUSTOM_EVENT_TEXT,
    AE_MESSENGER_EVENT_TEXT,
    HOST_EXPECTED_RECEIPT_EVENT_TEXT,
    HOST_FLIGHT_DATA_UPDATE_EVENT_TEXT,
    HOST_RETRIEVAL_ORDER_EVENT_TEXT,
    HOST_RETRIEVAL_ITEM_EVENT_TEXT,
    HOST_INVENTORY_UPDATE_EVENT_TEXT,
    HOST_INVENTORY_REQUEST_EVENT_TEXT,
    HOST_TO_PLC_EVENT_TEXT,
    PLC_TO_HOST_EVENT_TEXT,
    HOST_INVENTORY_REQUEST_BY_WAREHOUSE_EVENT_TEXT
  };

  public static final int INVENTORY_ADJUST_TYPE = 0;

  public static final int ORDER_COMPLETE_TYPE = 1;

  public static final int PICK_COMPLETE_TYPE = 2;

  public static final int STORE_COMPLETE_TYPE = 3;

  public static final int PRINT_LABEL_TYPE = 4;

  /*
   * Currently used selectors
   * 
   * AllStatusesEventTypeText                    =     Status~_~
   * CommEventTypeText                           =     Comm~
   * ControlEventTypeText                        =     Control~
   * ControllerStatusEventTypeText               =     Status~C~
   * ControllerDetailedStatusEventTypeText       =     Status~D~
   * ControllerProductivityStatusEventTypeText   =     Status~P~
   * EqpmntEventTypeText                         =     Eqpmnt~
   * ExcptnEventTypeText                         =     Excptn~
   * HeartbeatRequestEventTypeText               =     HrtbtRqst~
   * HeartbeatResponseEventTypeText              =     Status~H~
   * AllocateEventTypeText                       =     Allocate~
   * CustomAllocationText                        =     CustomAllocation~
   * HostEventTypeText                           =     Host~
   * HostMesgRecvText                            =     HostRecv~
   * HostMesgSendText                            =     HostSend~
   * LoadEventTypeText                           =     Load~
   * LogEventTypeText                            =     Log~
   * OrderEventTypeText                          =     Order~
   * PortEqpmntEventTypeText                     =     Eqpmnt~
   * RequestEventTypeText                        =     CtlrRqst~
   * SchedulerEventTypeText                      =     Sched~
   * StationEventTypeText                        =     Station~
   * UpdateEventTypeText                         =     Update~
   */

  public static final String ALL_STATUSES_EVENT_TYPE_TEXT = 
    STATUS_EVENT_TEXT + SUB_EVENT_TEXT + ControllerConsts.ALL_STATUSES + SUB_EVENT_TEXT;

  public static final String COMM_EVENT_TYPE_TEXT = 
    COMM_EVENT_TEXT + SUB_EVENT_TEXT; // + name;

  public static final String CONTROL_EVENT_TYPE_TEXT = 
    CONTROL_EVENT_TEXT + SUB_EVENT_TEXT;

  public static final String CONTROLLER_STATUS_EVENT_TYPE_TEXT = 
    STATUS_EVENT_TEXT + SUB_EVENT_TEXT + ControllerConsts.CONTROLLER_STATUS + SUB_EVENT_TEXT;

  public static final String CONTROLLER_DETAILED_STATUS_EVENT_TYPE_TEXT = 
    STATUS_EVENT_TEXT + SUB_EVENT_TEXT + ControllerConsts.DETAILED_STATUS + SUB_EVENT_TEXT;

  public static final String CONTROLLER_PRODUCTIVITY_STATUS_EVENT_TYPE_TEXT = 
    STATUS_EVENT_TEXT + SUB_EVENT_TEXT + ControllerConsts.PRODUCTIVITY_STATUS + SUB_EVENT_TEXT;

  public static final String EQPMNT_EVENT_TYPE_TEXT = 
    EQUIPMENT_EVENT_TEXT + SUB_EVENT_TEXT; // + name;

  public static final String EXCPTN_EVENT_TYPE_TEXT = 
    EXCEPTION_EVENT_TEXT + SUB_EVENT_TEXT;

  public static final String HEARTBEAT_REQUEST_EVENT_TYPE_TEXT = 
    HEARTBEAT_REQUEST_EVENT_TEXT + SUB_EVENT_TEXT;

  public static final String HEARTBEAT_RESPONSE_EVENT_TYPE_TEXT = 
    STATUS_EVENT_TEXT + SUB_EVENT_TEXT + ControllerConsts.HEARTBEAT_STATUS + SUB_EVENT_TEXT;

  public static final String ALLOCATE_EVENT_TYPE_TEXT = 
    ALLOCATE_EVENT_TEXT + SUB_EVENT_TEXT;

  public static final String CUSTOM_ALLOCATION_EVENT_TYPE_TEXT = 
    CUSTOM_ALLOCATION_EVENT_TEXT + SUB_EVENT_TEXT;
    
  public static final String HOST_MESG_RECV_TEXT = 
    HOST_MESG_RECV_EVENT_TEXT + SUB_EVENT_TEXT;

  public static final String HOST_MESG_SEND_TEXT = 
    HOST_MESG_SEND_EVENT_TEXT + SUB_EVENT_TEXT;

  public static final String LOAD_EVENT_TYPE_TEXT = 
    LOAD_EVENT_TEXT + SUB_EVENT_TEXT;

  public static final String LOG_EVENT_TYPE_TEXT = 
    LOG_EVENT_TEXT + SUB_EVENT_TEXT;

  public static final String ORDER_EVENT_TYPE_TEXT = 
    ORDER_EVENT_TEXT + SUB_EVENT_TEXT;

  public static final String PORT_EQPMNT_EVENT_TYPE_TEXT = 
    EQUIPMENT_EVENT_TEXT + SUB_EVENT_TEXT;

  public static final String REQUEST_EVENT_TYPE_TEXT = 
    CONTROLLER_REQUEST_EVENT_TEXT + SUB_EVENT_TEXT;

  public static final String SCHEDULER_EVENT_TYPE_TEXT = 
    SCHEDULER_EVENT_TEXT + SUB_EVENT_TEXT;

  public static final String STATION_EVENT_TYPE_TEXT = 
    STATION_EVENT_TEXT + SUB_EVENT_TEXT;

  public static final String UPDATE_EVENT_TYPE_TEXT = 
    UPDATE_EVENT_TEXT + SUB_EVENT_TEXT;

  public static final String UPDATE_EVENT_TYPE_TEXT2 = 
    SUB_EVENT_TEXT; // + name;

  public static final String STATION_LOAD_EVENT_TYPE_TEXT = 
    STATION_LOAD_EVENT_TEXT + SUB_EVENT_TEXT;

  public static final String ALLOCATION_PROBE_EVENT_TYPE_TEXT = 
    ALLOCATION_PROBE_EVENT_TEXT + SUB_EVENT_TEXT;

  public static final String CUSTOM_EVENT_TYPE_TEXT = 
    CUSTOM_EVENT_TEXT + SUB_EVENT_TEXT;
  
  public static final String AE_MESSENGER_EVENT_TYPE_TEXT = 
      AE_MESSENGER_EVENT_TEXT + SUB_EVENT_TEXT;
  
  public static final String HOST_EXPECTED_RECEIPT_EVENT_TYPE_TEXT = 
          HOST_EXPECTED_RECEIPT_EVENT_TEXT + SUB_EVENT_TEXT;
  
  public static final String HOST_FLIGHT_DATA_UPDATE_EVENT_TYPE_TEXT = 
          HOST_FLIGHT_DATA_UPDATE_EVENT_TEXT + SUB_EVENT_TEXT;
  
  public static final String HOST_INVENTORY_UPDATE_EVENT_TYPE_TEXT = 
          HOST_INVENTORY_UPDATE_EVENT_TEXT + SUB_EVENT_TEXT;
  
  public static final String HOST_INVENTORY_REQUEST_EVENT_TYPE_TEXT = 
          HOST_INVENTORY_REQUEST_EVENT_TEXT + SUB_EVENT_TEXT;
  
  public static final String HOST_RETRIEVAL_ORDER_EVENT_TYPE_TEXT = 
          HOST_RETRIEVAL_ORDER_EVENT_TEXT + SUB_EVENT_TEXT;
  
  public static final String HOST_RETRIEVAL_ITEM_EVENT_TYPE_TEXT = 
          HOST_RETRIEVAL_ITEM_EVENT_TEXT + SUB_EVENT_TEXT;
  
  public static final String HOST_INVENTORY_REQUEST_BY_WAREHOUSE_EVENT_TYPE_TEXT = 
		  HOST_INVENTORY_REQUEST_BY_WAREHOUSE_EVENT_TEXT + SUB_EVENT_TEXT;
  
  public static final String HOST_TO_PLC_EVENT_TYPE_TEXT = 
          HOST_TO_PLC_EVENT_TEXT + SUB_EVENT_TEXT;
  
  public static final String PLC_TO_HOST_EVENT_TYPE_TEXT = 
          PLC_TO_HOST_EVENT_TEXT + SUB_EVENT_TEXT;
  
  public static final String[] UniqueSelectors = 
  { ALL_STATUSES_EVENT_TYPE_TEXT, 
    COMM_EVENT_TYPE_TEXT, 
    CONTROL_EVENT_TYPE_TEXT, 
    CONTROLLER_STATUS_EVENT_TYPE_TEXT, 
    CONTROLLER_DETAILED_STATUS_EVENT_TYPE_TEXT, 
    CONTROLLER_PRODUCTIVITY_STATUS_EVENT_TYPE_TEXT, 
    EQPMNT_EVENT_TYPE_TEXT, 
    EXCPTN_EVENT_TYPE_TEXT, 
    HEARTBEAT_REQUEST_EVENT_TYPE_TEXT, 
    HEARTBEAT_RESPONSE_EVENT_TYPE_TEXT, 
    ALLOCATE_EVENT_TYPE_TEXT, 
    CUSTOM_ALLOCATION_EVENT_TEXT, 
    HOST_MESG_RECV_TEXT, 
    HOST_MESG_SEND_TEXT, 
    LOAD_EVENT_TYPE_TEXT, 
    LOG_EVENT_TYPE_TEXT, 
    ORDER_EVENT_TYPE_TEXT, 
    PORT_EQPMNT_EVENT_TYPE_TEXT, 
    REQUEST_EVENT_TYPE_TEXT, 
    SCHEDULER_EVENT_TYPE_TEXT, 
    STATION_EVENT_TYPE_TEXT, 
    UPDATE_EVENT_TYPE_TEXT, 
    UPDATE_EVENT_TYPE_TEXT2, 
    STATION_LOAD_EVENT_TYPE_TEXT, 
    ALLOCATION_PROBE_EVENT_TYPE_TEXT,
    CUSTOM_EVENT_TYPE_TEXT,
    AE_MESSENGER_EVENT_TEXT,
    HOST_EXPECTED_RECEIPT_EVENT_TYPE_TEXT,
    HOST_FLIGHT_DATA_UPDATE_EVENT_TYPE_TEXT,
    HOST_INVENTORY_UPDATE_EVENT_TYPE_TEXT,
    HOST_INVENTORY_REQUEST_EVENT_TYPE_TEXT,
    HOST_RETRIEVAL_ORDER_EVENT_TYPE_TEXT,
    HOST_RETRIEVAL_ITEM_EVENT_TYPE_TEXT,
    HOST_INVENTORY_REQUEST_BY_WAREHOUSE_EVENT_TYPE_TEXT,
    HOST_TO_PLC_EVENT_TYPE_TEXT,
    PLC_TO_HOST_EVENT_TYPE_TEXT
  };
  
}

