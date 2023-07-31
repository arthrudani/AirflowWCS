package com.daifukuamerica.wrxj.swingui.equipment;

import java.awt.Color;
import java.util.List;
import javax.swing.JComponent;

/**
 * @author Stephen Kendorski
 *
 */
public interface EquipmentGraphic
{
  // Daifuku purple!
  public static Color DAIFUKU_PURPLE = new Color(80, 64, 132);
  public static Color DAIFUKU_MEDIUM_PURPLE = new Color(189, 182, 214);
  public static Color DAIFUKU_LIGHT_PURPLE = new Color(222, 219, 239);
  
  // Status values (prioritized for the Equipment Monitor)
  static final int ALARM        = 0;
  static final int ERROR        = 1;
  static final int DISCONNECTED = 2;
  static final int OFFLINE      = 3;
  static final int UNKNOWN      = 4;
  static final int ONLINE       = 5;
  static final int PRISTINE     = 6;
  static final int NO_STATUS    = 7; // Future and/or non-JVM buttons
  static final int PROCESSING   = 8;
  
  // Corresponding button status colors
  static final Color[] STATUS_COLORS = { Color.orange, Color.red, Color.yellow,
      Color.blue, Color.lightGray, Color.green, Color.darkGray,
      DAIFUKU_MEDIUM_PURPLE, Color.CYAN };
  
  // Corresponding label status colors (green -> black)
  static final Color[] TEXT_STATUS_COLORS = { Color.orange, Color.red,
      Color.yellow, Color.blue, Color.lightGray, Color.black, Color.darkGray,
      DAIFUKU_MEDIUM_PURPLE, Color.CYAN };
  
  /**
   * Fetch the name of the instantiated EquipmentGraphic object.
   * 
   * @return the name
   */
  public abstract String getGraphicName();
  /**
   * Fetch the parent component of the instantiated EquipmentGraphic object.
   * 
   * @return the parent component
   */
  public abstract JComponent getGraphicParent();
  /*--------------------------------------------------------------------------*/
  /**
   * Specify the name of the Device that the instantiated EquipmentGraphic
   * object is associated with.
   * 
   * @param mcController the name of the controller
   */
  public abstract void setDeviceID(String isDeviceID);
  /**
   * Fetch the name of the Device that the instantiated EquipmentGraphic object
   * is associated with.
   * 
   * @return statusText the name of the controller
   */
  public abstract String getDeviceID();
  /*--------------------------------------------------------------------------*/
  /**
   * Specify the name of Station that the instantiated EquipmentGraphic object
   * is associated with.
   *   
   * @param s the station name
   */
  public abstract void setStationId(String s);
  /**
   * Fetch the name of Station that the instantiated EquipmentGraphic object
   * is associated with.
   * 
   * @return the station name
   */
  public abstract String getStationId();
  /*--------------------------------------------------------------------------*/
  /**
   * Specify the error code that the instantiated EquipmentGraphic object
   * is associated with.
   * 
   * @param errorCode the code
   */
  public abstract void setErrorCode(String errorCode);
  /**
   * Fetch the error code that the instantiated EquipmentGraphic object
   * is associated with.
  * 
   * @return the code
   */
  public abstract String getErrorCode();
  /*--------------------------------------------------------------------------*/
  /**
   * Specify the error set that the instantiated EquipmentGraphic object
   * is associated with.
   * 
   * @param errorSet the name of the error set
   */
  public abstract void setErrorSet(String errorSet);
  /**
   * Fetch the error set that the instantiated EquipmentGraphic object
   * is associated with.
   * 
   * @return the name of the error set
   */
  public abstract String getErrorSet();
  /*--------------------------------------------------------------------------*/
  /**
   * Set the error class that will be used to display error information
   * 
   * @param isErrorClass
   */
  public abstract void setErrorClass(String isErrorClass);
  /**
   * Fetch the error class that will be used to display error information
   * 
   * @return the name of the error set
   */
  public abstract String getErrorClass();
  /*--------------------------------------------------------------------------*/
  /**
   * Set the MOS ID for this graphic
   * 
   * @param isMOSID
   */
  public abstract void setMOSID(String isMOSID);
  /**
   * Fetch the MOS ID for this graphic
   * 
   * @return the name of the error set
   */
  public abstract String getMOSID();
  /*--------------------------------------------------------------------------*/
  /**
   * Specify the name of the Material Handling Computer (MC) Controller that
   * the instantiated EquipmentGraphic object is associated with.
   * 
   * @param mcController the name of the controller
   */
  public abstract void setMCController(String mcController);
  /**
   * Fetch the name of the Material Handling Computer (MC) Controller that the
   * instantiated EquipmentGraphic object is associated with.
   * 
   * @return statusText the name of the controller
   */
  public abstract String getMCController();
  /*--------------------------------------------------------------------------*/
  /**
   * Specify the name of the Monitoring & Operation Support Computer (MOS)
   * Controller that the instantiated EquipmentGraphic object is associated with.
   * 
   * @param mosController the name of the controller
   */
  public abstract void setMOSController(String mosController);
  /**
   * Fetch the name of the Monitoring & Operation Support Computer (MOS)
   * Controller that the instantiated EquipmentGraphic object is associated with.
   * 
   * @return the name of the controller
   */
  public abstract String getMOSController();
  /*--------------------------------------------------------------------------*/
  /**
   * Specify the status text the instantiated EquipmentGraphic object
   * is associated with.
   * 
   * @param statusText the text
   */
  public abstract void setStatusText(String statusText);
  /**
   * Specify the status/color of the instantiated EquipmentGraphic object.
   * 
   * @param status the status
   */
  public abstract void setStatus(int inStatus);
  public abstract void setStatusNoRepaint(int inStatus);
  /**
   * Refresh the status of the instantiated EquipmentGraphic object.
   * This method is to allow the equipment monitor to update based on local
   * WarehouseRx screen events.
   * 
   * @return status - the status
   */
  public abstract void refreshStatus();
  /*--------------------------------------------------------------------------*/
  /**
   * Handle additional parameters, if any
   */
  public abstract void setGraphicParameter(String isParameter);
  /*--------------------------------------------------------------------------*/
  /**
   * Is this graphic for future equipment?
   */
  public abstract boolean isFutureExpansion();
  /*--------------------------------------------------------------------------*/
  /**
   * Is this graphic for this JVM?
   */
  public abstract boolean isAssignedJVM();
  /*--------------------------------------------------------------------------*/
  /**
   * Set Mode
   * @param izTrackingMode
   */
  public abstract void setTrackingVisible(boolean izTrackingMode);
  /**
   * Set tracking data
   * @param ipTracking
   */
  public abstract void setTracking(List<String[]> ipTracking);
  /**
   * Set tracking data
   * @param ipTracking
   */
  public abstract void clearTrackingFilter();
}