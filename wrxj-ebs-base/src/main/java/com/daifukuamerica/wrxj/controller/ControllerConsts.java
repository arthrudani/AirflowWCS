package com.daifukuamerica.wrxj.controller;

import com.daifukuamerica.wrxj.log.LogConsts;
/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      SKDC Corp.
 * @author Stephen Kendorski
 * @version 1.0
 */

public interface ControllerConsts
{
  /*--------------------------------------------------------------------------*/
  /**
   * Property name for root-path name in command line parameter.  For example:
   * -Dcom.skdaifuku.wrxj.path="C:\java-skd".
   */
  static final String ROOT_PATH_PROPERTY = LogConsts.BaseLogPath;
  /*--------------------------------------------------------------------------*/
  /**
   * <i>Wildcard</i> event selector for subscribing to <i>ALL</i> types of status events.
   */
  static final char ALL_STATUSES      = '_';
  static final char BIDIRECTIONAL_STATUS    = 'B';
  /**
   * Selector for setting/publishing/subscribing to device/controller status events.
   */
  static final char CONTROLLER_STATUS = 'C';
  /**
   * Selector for setting/publishing/subscribing to device/controller detailed status events.
   */
  static final char DETAILED_STATUS   = 'D';
  /**
   * Selector for setting/publishing/subscribing to equipment status events.
   */
  static final char EQUIPMENT_STATUS  = 'E';
  /**
   * Selector for publishing/subscribing to 
   * {@link com.daifukuamerica.wrxj.common.device.monitor.SystemHealthMonitor SystemHealthMonitor} heartbeat status events.
   */
  static final char HEARTBEAT_STATUS  = 'H';
  /**
   * Selector for setting/publishing/subscribing to equipment status events.
   */
  static final char MACHINE_STATUS    = 'M';
  static final char NUDGE_EQUIPMENT_MONITOR   = 'N';
  static final char OPERATING_STATUS  = 'O';
  /**
   * Selector for setting/publishing/subscribing to productivity/statistics status events.
   */
  static final char PRODUCTIVITY_STATUS = 'P';
  /**
   * Selector for setting/publishing/subscribing to tracking status events.
   */
  static final char TRACKING_STATUS  = 'T';
  /**
   * Selector for setting/publishing/subscribing to Operating status events.
   */
  static final char UPDATE_STATUS  = 'U';
  
  /*--------------------------------------------------------------------------*/
  /**
   * Controller implementation's status is not yet known.
   */
  static final int STATUS_UNKNOWN       =  0;
  /**
   * Controller implementation is executing its <i>initialize()</i> method.
   */
  static final int STATUS_INITIALIZING  =  1;
  /**
   * Controller implementation has completed executing its <i>initialize()</i> method.
   */
  static final int STATUS_INITIALIZED   =  2;
  /**
   * Controller implementation.
   */
  static final int STATUS_STARTING      =  3;
  /**
   * Controller implementation.
   */
  static final int STATUS_STARTED       =  4;
  /**
   * Controller implementation has started, but is waiting for its equipment communication port to connect.
   */
  static final int STATUS_WAIT_PORT     =  5;
  /**
   * Controller Port implementation has started, but is waiting for its communication port to connect.
   */
  static final int STATUS_WAIT_COMPORT  =  6;
  /**
   * Controller implementation has started, but is waiting for its port to connect and its device to go online.
   */
  static final int STATUS_WAIT_DEVICE  =  7;
  /**
   * Controller implementation has started, but is waiting for its device to go online.
   */
  static final int STATUS_WAIT_PORT_AND_DEVICE  =  8;
  /**
   * Controller implementation is online, active, and can process events to do work.
   */
  static final int STATUS_RUNNING       =  9;
  /**
   * Controller implementation's equipment is going offline.
   */
  static final int STATUS_STOPPING      = 10;
  /**
   * Controller implementation's equipment is offline.
   */
  static final int STATUS_STOPPED       = 11;
  /**
   * Controller implementation is executing its <i>shutdown()</i> method.
   */
  static final int STATUS_SHUTTING_DOWN = 12;
  /**
   * Controller implementation has completed executing its <i>shutdown()</i> method.
   */
  static final int STATUS_SHUT_DOWN     = 13;
  /**
   * Controller implementation has detected a fault.
   */
  static final int STATUS_ERROR         = 14;
  /**
   * Controller implementation has been notifyed of a fault in its equipment communication port.
   */
  static final int STATUS_ERROR_PORT    = 15;
  /**
   * Controller implementation has detected an equipment fault.
   */
  static final int STATUS_ERROR_EQUIP   = 16;

  static final String STATUS_TEXT_RUNNING = "Running";
  static final String STATUS_TEXT_SHUTDOWN = "Shut-Down";
  /**
   * Array of Strings corresponding to status states.
   */
  static final String[] STATUS_TEXT = {
    "Unknown",
    "Initializing",
    "Initialized",
    "Starting",
    "Started",
    "Waiting for Port",
    "Waiting for ComPort",
    "Waiting for Device",
    "Waiting for Port/Device",
    STATUS_TEXT_RUNNING,
    "Stopping",
    "Stopped",
    "Shutting-Down",
    STATUS_TEXT_SHUTDOWN,
    "** Error **",
    "** Port Error **",
    "** Device/Equipment Error **"
  };

  static final int DS_UNKNOWN          =  0;

  static final String[] DETAIL_STATUS_TEXT = {
    "Unknown"
  };
}
