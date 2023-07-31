package com.daifukuamerica.wrxj.controller.status;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardSchedulerServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStatusServer;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.errorcodes.api.ErrorDescriptions;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusEventDataFormat;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusInfo;
import com.daifukuamerica.wrxj.swingui.equipment.properties.EquipmentMonitorProperties;
import com.daifukuamerica.wrxj.time.SkDateTime;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class StatusModel
{
  // For handling the equipment.properties file
  EquipmentMonitorProperties mpEMP;

  /**
   * Key to equipment configuration file.
   *
   * <p><b>Details:</b> <code>EQUIPMENT_CONFIGURATION_KEY</code>, defined as
   * "<code>{@value}</code>", is a the key to the application property
   * containing the path to the equipment configuration file.</p>
   */
  public static final String EQUIPMENT_CONFIGURATION_KEY = "EquipmentMonitorFrame.graphics";

  // Items that should be in the equipment.properties file
  public static final String EQUIPMENT_NAME = "EquipmentName";
  public static final String DESCRIPTION = "Description";
  public static final String CATEGORY = "Category";
  public static final String ERROR_SET = "ErrorSet";
  public static final String MC_CONTROLLER = "MCController";
  public static final String MOS_CONTROLLER = "MOSController";
  public static final String AISLE_GROUP = "AisleGroup";
  public static final String HOST_ID = "HostID";
  public static final String DEVICE_ID = "DeviceID";
  public static final String STATION_ID = "StationID";
  public static final String MC_ID = "MCID";
  public static final String MOS_ID = "MOSID";

  // Optional items that might be in the equipment.properties file
  public static final String GRAPHIC_CLASS = "GraphicClass";
  public static final String GRAPHIC_PARAMETER = "GraphicParam";
  public static final String VISIBILITY_BEHAVIOR = "VisibilityBehavior";

  // Items that are not in the equipment.properties file
  public  static final String INACTIVE = "Inactive";
  private static final String NAME = "Name";
  private static final String LAST_UPDATE_TIME = "LastUDTime";
  private static final String OPERATING_MODE = "OperatingMode";
  private static final String ONLINE_STATUS = "Status";
  private static final String OPERATING_STATUS = "OpStts";
  private static final String OPERATING_STATUS_UPDATE_TIME = "OpSttsUDTime";
  private static final String OPERATING_STATUS_DETAIL = "OpSttsDetail";
  private static final String OPERATING_STATUS_DETAIL_UPDATE_TIME = "OpSttsDetailUDTime";
  private static final String HEARTBEAT_RESPONSE_TIME = "HeartbeatRspTime";
  private static final String HEARTBEAT_UPDATE_TIME = "HeartbeatUDTime";
  private static final String ERROR = "Error";
  private static final String ERROR_UPDATE_TIME = "ErrorUDTime";
  private static final String TRACKING = "Trking";
  private static final String TRACKING_UPDATE_TIME = "TrkingUDTime";

  // Values
  public static final String NO_VALUE = "*NONE*";
  public static final String UNKNOWN = "*Unknown*";

  // Categories
  // Right now only Equipment matters.
  public static final String CAT_EQUIPMENT = "Equipment";
  public static final String CAT_MONITOR = "Monitor";
  public static final String CAT_NOSTATUS = "NoStatus";

  // Stupid global variables for messaging that Mike wants to eliminate
  private String idName = null;
  private String statusString = null;
  private long statusDataLong = 0;
  private long statusRxTime = 0;
  private String controllerName = null;

  // Internal status data
  private Map<String,String> mpAliases = new HashMap<>();
  private Map<String, Map<String,String>> mpStatusItems = new TreeMap<>();
  private Map<String,String> mpStatusData = null;
  private String statusUpdateTime = null;
  private SkDateTime updateDateTime = new SkDateTime(SKDCConstants.STATUS_DATE_FORMAT);

  // Database
  private StandardDeviceServer mpDeviceServer = null;
  private StandardLoadServer mpLoadServer = null;
  private StandardSchedulerServer mpSchedServer = null;
  private StandardStationServer mpStationServer = null;
  private StandardStatusServer mpStatusServer = null;
  private boolean mzInitializeStationStatus = false;

  // Status messages
  private StatusEventDataFormat mpSEDF;
  private StatusEventDataFormat mpTrackingSEDF;
  private boolean mzHasEmptyTracking = false;

  // Error descriptions for error messages
  private Map<String,ErrorDescriptions> mpErrorDescriptions = new TreeMap<>();

  // Logger
  private Logger mpLogger = Logger.getLogger();

  /**
   * Constructor
   */
  public StatusModel()
  {
    mpSEDF = new StatusEventDataFormat(getClass().getSimpleName());
    mpSEDF.setType(ControllerConsts.CONTROLLER_STATUS);

    mpTrackingSEDF = new StatusEventDataFormat(getClass().getSimpleName());
    mpTrackingSEDF.setType(ControllerConsts.CONTROLLER_STATUS);
  }

  /**
   * Clean up and shut down
   */
  public void cleanUp()
  {
    if (mpDeviceServer != null)
    {
      mpDeviceServer.cleanUp();
      mpDeviceServer = null;
    }
    if (mpLoadServer != null)
    {
      mpLoadServer.cleanUp();
      mpLoadServer = null;
    }
    if (mpSchedServer != null)
    {
      mpSchedServer.cleanUp();
      mpSchedServer = null;
    }
    if (mpStationServer != null)
    {
      mpStationServer.cleanUp();
      mpStationServer = null;
    }
    mpLogger = null;
  }

  /**
   * Should we initialize station statuses to OFFLINE
   *
   * @param izValue
   */
  public void initializeStationStatuses(boolean izValue)
  {
    mzInitializeStationStatus = izValue;
  }

  /*------------------------------------------------------------------------*/
  /*  Status/tracking changes                                               */
  /*------------------------------------------------------------------------*/

  /**
   * Get the status changes
   *
   * @return
   */
  public String getStatusChanges()
  {
    String result = mpSEDF.createStringToSend();
    mpSEDF.clearAllData();

    return result;
  }

  /**
   * Get the tracking changes
   *
   * @return
   */
  public String getTrackingChanges()
  {
    String result = mpTrackingSEDF.createStringToSend();
    mpTrackingSEDF.clearAllData();
    if (result == null && mzHasEmptyTracking)
    {
      result = "" + mpTrackingSEDF.getType();
      mzHasEmptyTracking = false;
    }
    return result;
  }

  /*------------------------------------------------------------------------*/
  /*  Status message setters                                                */
  /*------------------------------------------------------------------------*/

  /**
   * Set the status string
   *
   * @param isStatusString
   */
  public void setStatusString(String isStatusString)
  {
    statusString = isStatusString;
  }

  public void setStatusDataLong(long ilStatusDataLong)
  {
    statusDataLong = ilStatusDataLong;
  }

  public void setStatusTxTime(long ilStatusTxTime)
  {
    String updateDT = updateDateTime.getlongDateTimeAsString(ilStatusTxTime);
    statusUpdateTime = updateDT;
  }

  public void setStatusRxTime(long ilStatusRxTime)
  {
    statusRxTime = ilStatusRxTime;
  }

  public void setControllerName(String isControllerName)
  {
    controllerName = isControllerName;
  }

  /*------------------------------------------------------------------------*/
  /*  Properties for an item                                                */
  /*------------------------------------------------------------------------*/

  /**
   * Getter for particular status data item
   *
   * @param isIdName
   * @param key
   * @return
   */
  public String get(String isIdName, String key)
  {
    idName = mpAliases.get(isIdName);
    String value = null;
    if (idName != null)
    {
      mpStatusData = getMachineStatusDataAndSetIdName(idName);
      value = mpStatusData.get(key);
    }
    if (value == null)
    {
      value = UNKNOWN;
    }
    return value;
  }

  /**
   * Get a map of properties for a piece of equipment
   *
   * @param isIDName
   * @return
   */
  public Map<String,String> get(String isIDName)
  {
    String vsIDName = mpAliases.get(isIDName);
    if (vsIDName != null)
    {
      return getMachineStatusDataAndSetIdName(vsIDName);
    }
    return null;
  }

  /*------------------------------------------------------------------------*/
  /*  Get things for which we track status                                  */
  /*------------------------------------------------------------------------*/

  /**
   * Add the site equipment to the status model
   */
  public void addSiteEquipment()
  {
    try
    {
      mpEMP = new EquipmentMonitorProperties(mpLogger);
      Collection<Map<String,String>> vpStatusItems = mpEMP.getStatusItems();
      for (Map<String,String> m : vpStatusItems)
      {
        addEquipment(m);
      }
    }
    catch (Exception e)
    {
      mpLogger.logException("Failed to read equipment!", e);
    }
  }

  /**
   * Add the properties for a single piece of equipment to the status model
   *
   * @param ipProperties
   */
  public void addEquipment(Map<String,String> ipProperties)
  {
    if (mpStatusServer == null)
    {
      mpStatusServer = Factory.create(StandardStatusServer.class);
    }

    String keyName = ipProperties.get(EQUIPMENT_NAME);
    mpAliases.put(keyName, keyName);
    idName = keyName;
    mpStatusData = mpStatusItems.get(keyName);
    if (mpStatusData != null)
    {
      mpStatusItems.remove(keyName);
    }
    mpStatusData = ipProperties;
    mpStatusItems.put(keyName, mpStatusData);

    mpStatusData.put(StatusModel.NAME, keyName);
    mpStatusData.put(StatusModel.OPERATING_STATUS, UNKNOWN);
    mpStatusData.put(StatusModel.ERROR, " ");
    mpStatusData.put(StatusModel.LAST_UPDATE_TIME, " ");

    String vsMCID = mpStatusData.get(StatusModel.MC_ID);
    String controller = mpStatusData.get(StatusModel.MC_CONTROLLER);
    String mcName = controller + ":" + vsMCID;
    if (mcName.indexOf("*NONE") == -1)
    {
      mpStatusData.put(StatusModel.MC_ID, mcName);
      mpAliases.put(mcName, keyName);
    }
    else
    {
      mcName = "";
    }
    //
    String vsMOSID = mpStatusData.get(StatusModel.MOS_ID);
    controller = mpStatusData.get(StatusModel.MOS_CONTROLLER);
    String mosName = controller + ":" + vsMOSID;
    if (mosName.indexOf("*NONE") == -1)
    {
      mpStatusData.put(StatusModel.MOS_ID, mosName);
      mpAliases.put(mosName, keyName);
    }
    else
    {
      mosName = "";
    }
    //
    String vsStationID = mpStatusData.get(StatusModel.STATION_ID);
    controller = mpStatusData.get(StatusModel.MOS_CONTROLLER);
    String stationName = controller + ":" + vsStationID;
    if (stationName.indexOf("*NONE") == -1)
    {
      mpAliases.put(stationName, keyName);
    }
    else
    {
      stationName = "";
    }
    mpLogger.logDebug("StatusModel -- " + keyName + " - " +
                                        keyName + " - " +
                                        mcName + " - " +
                                        mosName +
                                        stationName + " -- Keys Added");
    if (mzInitializeStationStatus)
    {
    	// MCM Dec2017
    	// Make sure we are only stopping devices controlled by this JVM
    	 String vsStationId = mpStatusData.get(StatusModel.STATION_ID);
    	 if (mpStationServer == null)
    	 {
    	    mpStationServer = Factory.create(StandardStationServer.class);
    	 }
    	 if (mpDeviceServer == null)
    	 {
    		 mpDeviceServer = Factory.create(StandardDeviceServer.class);
    	 }
        if( !vsStationId.isEmpty() &&  mpStationServer.exists(vsStationId))
        {
        	try
        	{
        		String vsJVMId = Application.getString(SKDCConstants.JVM_IDENTIFIER_KEY);
        		String vsStationDevice = mpStationServer.getStationsDevice(vsStationId);
	            DeviceData vpDD = mpDeviceServer.getDeviceData(vsStationDevice);
	            if( vpDD != null )
	            {
	            	String vsStationJVMID = vpDD.getJVMIdentifier();


	            	if (vsJVMId == null || vsJVMId.isEmpty())
	            	{	// If no JVM configured, turn station offline
	            		updateStationStatus(StatusEventDataFormat.STATUS_OFFLINE);
	            	}
	            	else if( vsJVMId.equals(vsStationJVMID))
	            	{	// if JVM is configured, make sure were only turning sttions offline for
	            		// this JVM
	            		updateStationStatus(StatusEventDataFormat.STATUS_OFFLINE);
	            	}
            	}
			}
        	catch (DBException e)
        	{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

    // Skip fake, no-status objects that are only to beautify the WRx Swing
    // Equipment Monitor
    String vsCategory = mpStatusData.get(StatusModel.CATEGORY);

    if (!StatusModel.CAT_NOSTATUS.equals(vsCategory))
    {
      mpStatusServer.addEquipment(keyName,
          vsMCID,  mpStatusData.get(StatusModel.MC_CONTROLLER),
          vsMOSID, mpStatusData.get(StatusModel.MOS_CONTROLLER),
          mpStatusData.get(StatusModel.ERROR_SET),
          vsStationID,
          mpStatusData.get(StatusModel.DEVICE_ID),
          mpStatusData.get(StatusModel.GRAPHIC_CLASS),
          mpStatusData.get(StatusModel.DESCRIPTION),
          mpStatusData.get(StatusModel.VISIBILITY_BEHAVIOR));

      // Delete existing tracking on start up
      mpStatusServer.deleteTracking(keyName);
    }
  }

  /*--------------------------------------------------------------------------*/
  public void clearHeartbeatResponseTimes()
  {
    String updateDT = updateDateTime.getCurrentDateTimeAsString();
    Iterator<Map<String,String>> statusesIterator = mpStatusItems.values().iterator();
    while (statusesIterator.hasNext())
    {
      try
      {
        mpStatusData = statusesIterator.next();
      }
      catch (Exception e)
      {
        mpLogger.logException(e, "clearHeartbeatResponseTimes - Exception");
        mpStatusData = null;
        break;
      }
      String previousResponse = mpStatusData.get(StatusModel.HEARTBEAT_RESPONSE_TIME);
      updateStatusData(StatusModel.HEARTBEAT_RESPONSE_TIME, " ");
      //
      // Now Assemble Changes for publishing.
      //
      String sName = mpStatusData.get(StatusModel.NAME);
      //
      // If a Frame no longer responds remove it from our list.
      //
      if ((previousResponse.equals(" ")) &&
          (sName.indexOf("SystemGateway") == 0))
      {
        statusesIterator.remove();
        mpAliases.remove(sName);

        mpSEDF.addStatusMessage(sName, ControllerConsts.OPERATING_STATUS,
            ControllerConsts.STATUS_TEXT_SHUTDOWN,
            StatusEventDataFormat.NO_DATA, updateDT);
      }
      else
      {
        mpSEDF.addStatusMessage(sName, ControllerConsts.HEARTBEAT_STATUS,
            StatusEventDataFormat.NO_DATA, StatusEventDataFormat.NO_DATA,
            updateDT);
      }
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public void updateStatus()
  {
    switch (statusString.charAt(0))
    {
      case ControllerConsts.BIDIRECTIONAL_STATUS: updateBidirectionalStatus(); break;
      case ControllerConsts.CONTROLLER_STATUS: updateControllerStatus(); break;
      case ControllerConsts.DETAILED_STATUS: updateControllerStatusDetail(); break;
      case ControllerConsts.HEARTBEAT_STATUS: updateControllerHeartbeatStatus(); break;
      case ControllerConsts.MACHINE_STATUS: updateMachineStatus(); break;
      case ControllerConsts.EQUIPMENT_STATUS: updateEquipmentStatus(); break;
      case ControllerConsts.TRACKING_STATUS: updateTrackingStatus(); break;
      case ControllerConsts.PRODUCTIVITY_STATUS: updateProductivityStatus(); break;
      default: break;
    }
  }

  private ErrorDescriptions getErrorSet(String isErrorSet)
  {
    // See if we already have the error descriptions
    ErrorDescriptions vpErrorDesc = mpErrorDescriptions.get(isErrorSet);
    if (vpErrorDesc != null)
      return vpErrorDesc;

    // We don't.  Find them!
    try
    {
      // Probably a fully qualified path name
      if (isErrorSet.startsWith("com."))
      {
        vpErrorDesc = (ErrorDescriptions)Class.forName(isErrorSet).getDeclaredConstructor().newInstance();
      }
      else // Alias
      {
        String vsErrorClass = mpEMP.getErrorClass(isErrorSet);
        if (vsErrorClass == null)
        {
          return null;
        }
        vpErrorDesc = (ErrorDescriptions) Class.forName(vsErrorClass).getDeclaredConstructor().newInstance();
      }

      mpErrorDescriptions.put(isErrorSet, vpErrorDesc);
      return vpErrorDesc;
    }
    catch (final Exception e)
    {
      mpLogger.logException(isErrorSet, e);
    }
    return null;
  }

  /*--------------------------------------------------------------------------*/
  private String getKeyName(String isIdName)
  {
    return mpAliases.get(isIdName);
  }

  /*--------------------------------------------------------------------------*/
  private Map<String,String> getControllerStatusData()
  {
    String keyName = controllerName;
    idName = getKeyName(keyName);
    if (idName == null)
    {
      mpAliases.put(keyName, keyName);
      mpLogger.logDebug("Adding key/value: " + keyName + " - " + keyName + " - StatusModel");
      idName = keyName;
    }
    try
    {
      mpStatusData = mpStatusItems.get(idName);
    }
    catch (NullPointerException e)
    {
      //
      // Update failed while shutting down.
      //
      return null;
    }
    if (mpStatusData == null)
    {
      mpLogger.logDebug("Adding StatusData for \"" + idName + "\" - StatusModel");
      mpStatusData = new HashMap<>();
      mpStatusItems.put(keyName, mpStatusData);
      mpStatusData.put(StatusModel.NAME, idName);
      mpStatusData.put(StatusModel.OPERATING_MODE, UNKNOWN);
      mpStatusData.put(StatusModel.ONLINE_STATUS, UNKNOWN);
      mpStatusData.put(StatusModel.OPERATING_STATUS, UNKNOWN);
      mpStatusData.put(StatusModel.OPERATING_STATUS_DETAIL, " ");
      mpStatusData.put(StatusModel.HEARTBEAT_RESPONSE_TIME, " ");
      mpStatusData.put(StatusModel.ERROR, UNKNOWN);
      mpStatusData.put(StatusModel.LAST_UPDATE_TIME, " ");
    }
    return mpStatusData;
  }

  /*--------------------------------------------------------------------------*/
  protected void updateStatusData(String key, String value)
  {
    mpStatusData.put(key, value);
  }

  /*--------------------------------------------------------------------------*/
  protected void updateStatusDataUpdateTime(String key)
  {
    mpStatusData.put(key, statusUpdateTime);
    mpStatusData.put(StatusModel.LAST_UPDATE_TIME, statusUpdateTime);
  }

  /*--------------------------------------------------------------------------*/
  private void updateControllerStatus()
  {
    mpStatusData = getControllerStatusData();
    if (mpStatusData == null)
    {
      return;
    }
    statusString = statusString.substring(2); // lose "C ";
    updateStatusData(StatusModel.OPERATING_STATUS, statusString);
    String errorData = "---";
    updateStatusDataUpdateTime(StatusModel.OPERATING_STATUS_UPDATE_TIME);

    //
    // Now Assemble Changes for publishing.
    //
    mpSEDF.addStatusMessage(controllerName, ControllerConsts.OPERATING_STATUS,
        statusString, errorData, statusUpdateTime);

    //
    // If a Frame shuts down remove it from our list.
    //
    if ((statusString.equals(ControllerConsts.STATUS_TEXT_SHUTDOWN)) &&
        (controllerName.indexOf("SystemGateway") == 0))
    {
      mpAliases.remove(controllerName);
      mpStatusItems.remove(controllerName);
    }
  }

  /*--------------------------------------------------------------------------*/
  private void updateControllerStatusDetail()
  {
    mpStatusData = getControllerStatusData();
    statusString = statusString.substring(2); // lose "D ";
    updateStatusData(StatusModel.OPERATING_STATUS_DETAIL, statusString);
    String errorData = "---";
    updateStatusDataUpdateTime(StatusModel.OPERATING_STATUS_DETAIL_UPDATE_TIME);
    //
    // Now Assemble Changes for publishing.
    //
    mpSEDF.addStatusMessage(controllerName, ControllerConsts.DETAILED_STATUS,
        statusString, errorData, statusUpdateTime);
  }

  /*--------------------------------------------------------------------------*/
  private void updateControllerHeartbeatStatus()
  {
    if (controllerName != null)
    {
      if (controllerName.length() == 0)
      {
        mpLogger.logError("NO controllerName - updateControllerHeartbeatStatus()");
        return;
      }
    }
    else
    {
      mpLogger.logError("NO controllerName - updateControllerHeartbeatStatus()");
      return;
    }
    mpStatusData = getControllerStatusData();
    //
    // statusDataLong = time the System Health Monitor transmitted the request
    //                  that generated this response.
    //
    String responseTime = updateDateTime.getElapsedDateTimeAsString(statusRxTime - statusDataLong);
    updateStatusData(StatusModel.HEARTBEAT_RESPONSE_TIME, responseTime);
    updateStatusData(StatusModel.HEARTBEAT_UPDATE_TIME, statusUpdateTime);
    //
    // Now Assemble Changes for publishing.
    //
    mpSEDF.addStatusMessage(controllerName, ControllerConsts.HEARTBEAT_STATUS,
        responseTime, StatusEventDataFormat.NO_DATA, statusUpdateTime);
  }

  /*--------------------------------------------------------------------------*/
  private void updateProductivityStatus()
  {
    // statusTxTime
    // statusRxTime
    // statusString
    // statusDataLong
    // controllerName

    statusString = statusString.substring(2); // lose "P ";
  }

  /*--------------------------------------------------------------------------*/
  protected Map<String,String> getMachineStatusDataAndSetIdName(String isControllerName, String machineId)
  {
    String keyName = isControllerName + ":" + machineId;
    return getMachineStatusDataAndSetIdName(keyName);
  }

  /*--------------------------------------------------------------------------*/
  private Map<String,String> getMachineStatusDataAndSetIdName(String keyName)
  {
    idName = getKeyName(keyName);
    if (idName == null)
    {
      mpAliases.put(keyName, keyName);
      mpLogger.logDebug("Adding key/value: " + keyName + " - " + keyName + " - StatusModel");
      idName = keyName;
    }
    mpStatusData = mpStatusItems.get(idName);
    if (mpStatusData == null)
    {
      mpLogger.logDebug("Adding StatusData for \"" + idName + "\" - StatusModel");
      mpStatusData = new HashMap<>();
      mpStatusItems.put(keyName, mpStatusData);
      mpStatusData.put(StatusModel.NAME, idName);
      mpStatusData.put(StatusModel.OPERATING_STATUS, UNKNOWN);
      mpStatusData.put(StatusModel.ERROR, " ");
      mpStatusData.put(StatusModel.LAST_UPDATE_TIME, " ");
    }
    return mpStatusData;
  }

  /**
   * Get the MC machine ID for a given controller's station
   * @param isController
   * @param isStation
   * @return
   */
  private String getMachineId(String isController, String isStation)
  {
    Set<String> vpKeys = mpStatusItems.keySet();
    for (String vsKey : vpKeys)
    {
        Map<String,String> vpStatusData = mpStatusItems.get(vsKey);
        String vsStation = vpStatusData.get(StatusModel.STATION_ID);
        String vsMCDevice = vpStatusData.get(StatusModel.MC_CONTROLLER);
        if (vsStation != null && vsStation.equals(isStation) &&
            vsMCDevice != null && vsMCDevice.equals(isController))
        {
          String vsMachineID = vpStatusData.get(StatusModel.MC_ID);
          if (vsMachineID.indexOf(':') > 0)
            vsMachineID = vsMachineID.substring(vsMachineID.indexOf(':')+1);
          return vsMachineID;
        }
    }
    return null;
  }

  /**
   * Update the bidirectional status of a station (MC port)
   */
  private void updateBidirectionalStatus()
  {
    StatusEventDataFormat vpSEDF = new StatusEventDataFormat("Parse");
    vpSEDF.setMessage(statusString);
    for (StatusInfo s : vpSEDF.getStatusList())
    {
      String vsStation = s.getBidirStation();
      String vsBidirStatus = s.getBidirStatus();

      try
      {
        String vsMachineId = getMachineId(controllerName, vsStation);
        if (vsMachineId == null)
        {
          mpLogger.logError("Cannot find machine ID for station " + vsStation);
          continue;
        }
        mpStatusData = getMachineStatusDataAndSetIdName(controllerName, vsMachineId);

        updateStatusData(StatusModel.MC_ID, controllerName + ":" + vsMachineId);

        String vsStationStatus = mpStatusData.get(StatusModel.OPERATING_STATUS);
        if (vsStationStatus.indexOf('|') > 0)
        {
          int vnOldExtraStatusIndex = vsStationStatus.indexOf("|");
          vsStationStatus = vsStationStatus.substring(0, vnOldExtraStatusIndex+1);
          vsStationStatus += vsBidirStatus;
        }
        else
        {
          vsStationStatus += '|' + vsBidirStatus;
        }
        updateStatusData(StatusModel.OPERATING_STATUS, vsStationStatus);
        updateStatusDataUpdateTime(StatusModel.OPERATING_STATUS_UPDATE_TIME);

        String machineErrorSet = mpStatusData.get(StatusModel.ERROR_SET);
        //
        // Now Assemble Changes for publishing.
        //
        mpSEDF.addStatusMessage(idName, vsStationStatus,
            mpStatusData.get(StatusModel.ERROR), machineErrorSet,
            statusUpdateTime);
        updateStationStatus(vsStationStatus);
      }
      catch (Exception e)
      {
        mpLogger.logError("Exception - MC updateBidirectionalStatus\n" + statusString);
        mpLogger.logException(statusString, e);
      }
    }
  }

  /**
   * Machine status received from MC port.
   * Find all stations in the same group and give them the same status
   *
   * Pass through tokenized string like we do in the parent class, but build a
   * list of all reporting stations that have a RepresentativeStation.
   *
   * Pass through the list of reporting stations we created and...
   *   If this station reported online then set the representative station
   *     online as well, exit
   *   If this station reported offline then we have to check the status off all
   *     other stations grouped under the representative station, and if all
   *     other stations are also offline, then we can set the representative
   *     station offline.  Otherwise leave the representative station alone.
   *   Multiple stations grouped under the representative station may be
   *     reporting in this statusString, but our approach as described above
   *     will allow ANY station that reports online to turn on the
   *     representative station, but NO station reporting offline can turn off
   *     the representative station unless all other stations in the group have
   *     already reported offline.
   */
  private void updateMachineStatus()
  {
    String[] stationStatusEntry = null;
    List<String[]> stationStatusList = new ArrayList<>();
    StandardStationServer vpStnServ = Factory.create(StandardStationServer.class);

    StatusEventDataFormat vpSEDF = new StatusEventDataFormat("Parse");
    vpSEDF.setMessage(statusString);
    for (StatusInfo s : vpSEDF.getStatusList())
    {
      String vsMachineId = s.getMachineID();
      String vsMachineStatusDescription = s.getMachineDesc();
      String vsMachineErrorData = s.getMachineError();

      try
      {
        mpStatusData = getMachineStatusDataAndSetIdName(controllerName, vsMachineId);
        //
        updateStatusData(StatusModel.MC_ID, controllerName + ":" + vsMachineId);
        updateStatusData(StatusModel.ERROR, vsMachineErrorData);
        if (!vsMachineErrorData.equals(StatusEventDataFormat.NONE))
        {
          updateStatusDataUpdateTime(StatusModel.ERROR_UPDATE_TIME);
          logEquipmentError(vsMachineStatusDescription, vsMachineErrorData,
              statusUpdateTime.split(" ")[0]);
        }
        // Carry along extra info if we have it
        if (vsMachineStatusDescription.indexOf('|') < 0)
        {
          String vsOldStatus = mpStatusData.get(StatusModel.OPERATING_STATUS);
          int vnOldExtraStatusIndex = vsOldStatus.indexOf("|");
          if (vnOldExtraStatusIndex > 0)
          {
            vsMachineStatusDescription += vsOldStatus.substring(vnOldExtraStatusIndex);
          }
        }
        updateStatusData(StatusModel.OPERATING_STATUS, vsMachineStatusDescription);
        updateStatusDataUpdateTime(StatusModel.OPERATING_STATUS_UPDATE_TIME);
      }
      catch (Exception e)
      {
        mpLogger.logException("Exception - MC updateMachineStatus\n" + statusString, e);
      }
      String machineErrorSet = mpStatusData.get(StatusModel.ERROR_SET);
      //
      // Now Assemble Changes for publishing.
      //
      mpSEDF.addStatusMessage(idName, vsMachineStatusDescription,
          vsMachineErrorData, machineErrorSet, statusUpdateTime);
      updateStationStatus(vsMachineStatusDescription);

      // Persist
      if (mpStatusServer == null)
      {
        mpStatusServer = Factory.create(StandardStatusServer.class);
      }
      mpStatusServer.updateMCStatus(controllerName, vsMachineId,
          vsMachineStatusDescription, vsMachineErrorData);

      // Check the station data to see if we're part of a group (has
      // RepresentativeStation), if so add it to my local list
      if (mpStationServer == null)
      {
        mpStationServer = Factory.create(StandardStationServer.class);
      }
      if (mpStationServer.exists(mpStatusData.get(StatusModel.STATION_ID)))
      {
        String vsStationId = mpStatusData.get(StatusModel.STATION_ID);
        String reprStationName = vpStnServ.getReprStationName(vsStationId);
        if (reprStationName.length()> 0)
        {
          //Avoid circular references by not adding child stations if they already appear in the list
          //as some other station's representative station.  Also don't add representative stations if
          //they already appear as a child station.
          boolean circularReferenceFound = false;
          for(String[] stationStatusReading : stationStatusList)
          {
            if (stationStatusReading[0] == reprStationName)
            {
              circularReferenceFound = true;
              break;
            }
            if (stationStatusReading[1] == vsStationId)
            {
              circularReferenceFound = true;
              break;
            }
          }
          if (!circularReferenceFound)
          {
            stationStatusEntry = new String[3];
            stationStatusEntry[0] = vsStationId;
            stationStatusEntry[1] = reprStationName;
            stationStatusEntry[2] = vsMachineStatusDescription;
            stationStatusList.add(stationStatusEntry);
          }
        }
      }
    }

    for(String[] stationStatusReading : stationStatusList) //get a station that reported its status
    {
      try
      {
        //if this station reported online then we can set his representative station's status to online
        if ((stationStatusReading[2].startsWith(StatusEventDataFormat.STATUS_RUNNING)) ||
            (stationStatusReading[2].startsWith(StatusEventDataFormat.STATUS_ONLINE)))
        {
          vpStnServ.setPhysicalStatus(stationStatusReading[1], DBConstants.ONLINE);
        }
        else //this station is not online but maybe some other station in the group is online
        {
          String[] groupedStations = vpStnServ.getStationNameListByReprStation(stationStatusReading[1]); //get station names of all stations grouped with the reporting station
          boolean canStopRepresentativeStation = true;
          for (String groupedStation : groupedStations)
          {
            if (vpStnServ.getPhysicalStatus(groupedStation) == DBConstants.ONLINE)
            {
              canStopRepresentativeStation = false;
              break;  //if any station in the group is online, then leave the representative station alone
            }
          }
          if (canStopRepresentativeStation)
          {
            vpStnServ.setPhysicalStatus(stationStatusReading[1], DBConstants.OFFLINE);
          }
        }
      }
      catch (DBException ex)
      {
        mpLogger.logException(ex, "Getting representatives grouped stns - Exception");
        break;
      }
    }
  }
  /*--------------------------------------------------------------------------*/
  // Machine status received from MOS port.
  /*--------------------------------------------------------------------------*/
  private void updateEquipmentStatus()
  {
    StatusEventDataFormat vpSEDF = new StatusEventDataFormat("Parse");
    vpSEDF.setMessage(statusString);
    for (StatusInfo s : vpSEDF.getStatusList())
    {
      try
      {
        String vsMachineId = s.getMachineID();
        String vsMachineStatusDescription = s.getMachineDesc();
        String vsMachineErrorData = s.getMachineError();
        String vsMachineReportTime = s.getMachineTime();

        try
        {
          mpStatusData = getMachineStatusDataAndSetIdName(controllerName, vsMachineId);
          //
          updateStatusData(StatusModel.MOS_ID, controllerName + ":" + vsMachineId);
          updateStatusData(StatusModel.ERROR, vsMachineErrorData);
          if (!vsMachineErrorData.equals(StatusEventDataFormat.NONE))
          {
            //
            // We DO have an equipment error to log.
            //
            updateStatusDataUpdateTime(StatusModel.ERROR_UPDATE_TIME);
            logEquipmentError(vsMachineStatusDescription, vsMachineErrorData,
                vsMachineReportTime);
          }
          // Carry along extra info if we have it
          if (vsMachineStatusDescription.indexOf('|') < 0)
          {
            String vsOldStatus = mpStatusData.get(StatusModel.OPERATING_STATUS);
            int vnOldExtraStatusIndex = vsOldStatus.indexOf("|");
            if (vnOldExtraStatusIndex > 0)
            {
              vsMachineStatusDescription += vsOldStatus.substring(vnOldExtraStatusIndex);
            }
          }
          updateStatusData(StatusModel.OPERATING_STATUS, vsMachineStatusDescription);
          updateStatusDataUpdateTime(StatusModel.OPERATING_STATUS_UPDATE_TIME);
        }
        catch (Exception e)
        {
          mpLogger.logError("Exception - MOS updateEquipmentStatus\n" + statusString);
          mpLogger.logException(statusString, e);
        }
        String machineErrorSet = mpStatusData.get(StatusModel.ERROR_SET);
        //
        // Now Assemble Changes for publishing.
        //
        mpSEDF.addStatusMessage(idName, vsMachineStatusDescription,
            vsMachineErrorData, machineErrorSet, statusUpdateTime);
        updateStationStatus(vsMachineStatusDescription);

        // Persist
        if (mpStatusServer == null)
        {
          mpStatusServer = Factory.create(StandardStatusServer.class);
        }
        mpStatusServer.updateMOSStatus(controllerName, vsMachineId,
            vsMachineStatusDescription, vsMachineErrorData);
      }
      catch (Exception e)
      {
        mpLogger.logError("Error parsing status string '" + statusString + "' -- "
            + e.getMessage());
      }
    }
  }

  /**
   * Log an equipment error
   *
   * @param isMachineStatusDescription
   * @param isMachineErrorData
   * @param isMachineReportTime
   */
  private void logEquipmentError(String isMachineStatusDescription,
      String isMachineErrorData, String isMachineReportTime)
  {
    if (!isMachineStatusDescription.equals(StatusEventDataFormat.STATUS_NO_LOG)
        && SKDCUtility.isNotBlank(isMachineErrorData))
    {
      int vnMachineErrorCode = Integer.valueOf(isMachineErrorData);
      String vsErrorSet = mpStatusData.get(StatusModel.ERROR_SET);
      ErrorDescriptions vpErrorDescriptions = getErrorSet(vsErrorSet);
      String vsErrorText = (vpErrorDescriptions == null) ? "*UNKNOWN*"
          : vpErrorDescriptions.getDescription(isMachineErrorData);
      mpLogger.logError(idName + " (" + isMachineReportTime + ") "
          + vsErrorText, vnMachineErrorCode);
    }
  }

  /*--------------------------------------------------------------------------*/
  public void setEquipmentStatuses(String statusDescription)
  {
    Iterator<Map<String,String>> statusesIterator = mpStatusItems.values().iterator();
    while (statusesIterator.hasNext())
    {
      mpStatusData = statusesIterator.next();
      updateStationStatus(statusDescription);
    }
  }

  /**
   * Get the device ID for a given name in the status updates
   *
   * @param isName
   * @return
   */
  public String getDeviceForStatusName(String isName)
  {
    for (Map<String,String> m : mpStatusItems.values())
    {
      String vsName = m.get(StatusModel.NAME);
      if (vsName != null && vsName.equals(isName))
      {
        return m.get(StatusModel.DEVICE_ID);
      }
    }
    return null;
  }

  /*--------------------------------------------------------------------------*/
  // Update Station's "Physical Status" in database.
  /*--------------------------------------------------------------------------*/
  protected void updateStationStatus(String statusDescription)
  {
    if (mpStationServer == null)
    {
      mpStationServer = Factory.create(StandardStationServer.class);
    }

    String vsStationId = mpStatusData.get(StatusModel.STATION_ID);
    if (vsStationId != null && !vsStationId.equals(StatusEventDataFormat.NONE) &&
        mpStationServer.exists(vsStationId))
    {
      try
      {
        /*
         * Don't touch any station that's not part of this JVM
         */
        if (mpStationServer.isStationPartOfAnySplitSystem(vsStationId))
        {
          if (! mpStationServer.isStationPartOfThisSplitSystem(vsStationId))
          {
            return;
          }
        }

        int physicalStatus = DBConstants.OFFLINE;
        if (statusDescription.startsWith(StatusEventDataFormat.STATUS_RUNNING) ||
            statusDescription.startsWith(StatusEventDataFormat.STATUS_ONLINE))
        {
          physicalStatus = DBConstants.ONLINE;
        }
        mpLogger.logDebug("Station \"" + vsStationId + "\"  Status: "
            + statusDescription + " - updateStationStatus()");
        mpStationServer.setPhysicalStatus(vsStationId, physicalStatus);
      }
      catch(Exception e1)
      {
        mpLogger.logException(e1);
      }

      //
      // Update Station Store/Retrieve Mode (if reported).
      //
      int storeRetrieveMode = 0;
      boolean modeChange = false;
      if (statusDescription.indexOf(StatusEventDataFormat.STATUS_RETRIEVE) != -1)
      {
        if (mpLoadServer == null)
        {
          mpLoadServer = Factory.create(StandardLoadServer.class);
        }
        LoadData vpLD = mpLoadServer.getOldestLoadData(vsStationId, 0);
        if (vpLD == null || vpLD.getLoadMoveStatus() == DBConstants.STORING)
        {
          storeRetrieveMode = DBConstants.RETRIEVEMODE;
          modeChange = true;
        }
        else
        {
          mpLogger.logDebug("Ignoring bidirectional status change for "
              + vsStationId + "--Station has load " + vpLD.getLoadID() + ".");
        }
      }
      else if (statusDescription.indexOf(StatusEventDataFormat.STATUS_STORE) != -1)
      {
        storeRetrieveMode = DBConstants.STOREMODE;
        modeChange = true;
      }
      if (modeChange)
      {
        try
        {
          mpLogger.logDebug("Station \"" + vsStationId + "\"  Mode: "
              + statusDescription + " - updateStationStatus()");
          mpStationServer.setBidirectionalMode(vsStationId, storeRetrieveMode);
        }
        catch (Exception e1)
        {
          mpLogger.logException(e1);
        }
      }
    }
    else
    {
      if (mpDeviceServer == null)
      {
        mpDeviceServer = Factory.create(StandardDeviceServer.class);
      }
      String deviceId = mpStatusData.get(StatusModel.DEVICE_ID);
      if (deviceId != null && ! deviceId.equals(StatusEventDataFormat.NONE) &&
          mpDeviceServer.exists(deviceId))
      {
        /*
         * Don't touch any device that's not part of this JVM
         */
        try
        {
          if (mpDeviceServer.isDevicePartOfAnySplitSystem(deviceId))
          {
            if (! mpDeviceServer.isDevicePartOfThisSplitSystem(deviceId))
            {
              return;
            }
          }

          int physicalStatus = DBConstants.OFFLINE;
          if ((statusDescription.startsWith(StatusEventDataFormat.STATUS_RUNNING)) ||
              (statusDescription.startsWith(StatusEventDataFormat.STATUS_ONLINE)))
          {
            physicalStatus = DBConstants.ONLINE;
          }
          mpLogger.logDebug("Device \"" + deviceId + "\"  Status: "
              + statusDescription + " - updateStationStatus()");
          mpDeviceServer.setPhysicalStatus(deviceId, physicalStatus);
        }
        catch(Exception e1)
        {
          mpLogger.logException(e1);
        }
      }
    }
  }


  /*--------------------------------------------------------------------------*/
  // Machine Load Tracking status received from MOS port.
  /*--------------------------------------------------------------------------*/
  private void updateTrackingStatus()
  {
    String vsController = "NotInitialized";
    //
    // Any update means we should clear any accumulated tracking.
    //
    clearAllTrackingStatuses();
    StatusEventDataFormat vpSEDF = new StatusEventDataFormat("Parse");
    vpSEDF.setMessage(statusString);
    for (StatusInfo vpSI : vpSEDF.getStatusList())
    {
      String machineId = vpSI.getTrackMachine();
      String mcKey = vpSI.getTrackKey();
      String bcrData = vpSI.getTrackBCR();
      String transportType = vpSI.getTrackType();
      String src = vpSI.getTrackSrc();
      String dst = vpSI.getTrackDest();
      String loadSize = vpSI.getTrackSize();

      if (bcrData.trim().length() == 0)
      {
        //
        // No barcode - Use the Tracking Id (mckey) to find a barcode.
        //
        bcrData = getTrackingsLoadId(mcKey);
      }
      //
      if (transportType.equals(StatusEventDataFormat.STATUS_STORE))
      {
        mpStatusData = getMachineStatusDataAndSetIdName(controllerName, src);
        String description = mpStatusData.get(StatusModel.DESCRIPTION);
        src = src + "  (" + description + ")";
      }
      else if (transportType.equals(StatusEventDataFormat.STATUS_RETRIEVE))
      {
        mpStatusData = getMachineStatusDataAndSetIdName(controllerName, dst);
        String description = mpStatusData.get(StatusModel.DESCRIPTION);
        dst = dst + "  (" + description + ")";
      }
      else if (transportType.equals(StatusEventDataFormat.STATUS_TRANSFER))
      {
        mpStatusData = getMachineStatusDataAndSetIdName(controllerName, src);
        String description = mpStatusData.get(StatusModel.DESCRIPTION);
        src = src + "  (" + description + ")";
        mpStatusData = getMachineStatusDataAndSetIdName(controllerName, dst);
        description = mpStatusData.get(StatusModel.DESCRIPTION);
        dst = dst + "  (" + description + ")";
      }
      mpStatusData = getMachineStatusDataAndSetIdName(controllerName, machineId);

      // Persistence to database (start)
      if (!vsController.equals(idName))
      {
        vsController = idName;
        mpStatusServer.deleteTracking(idName);
      }
      if (!StatusEventDataFormat.STATUS_NOT_APPLICABLE.equals(transportType))
      {
        mpStatusServer.addTracking(idName, machineId, mcKey, bcrData,
            transportType, src, dst, loadSize);
      }
      // Persistence to database (end)

      mpTrackingSEDF.addTrackingMessage(idName, machineId, mcKey, bcrData,
          transportType, src, dst, loadSize, statusUpdateTime);
    }

    String vsTrackingInfo = mpTrackingSEDF.getMessageWithoutHeader();
    mzHasEmptyTracking = (vsTrackingInfo.length() == 0);
    if (mzHasEmptyTracking)
    {
      mpStatusServer.deleteAllTracking(controllerName);
    }
    updateStatusData(StatusModel.TRACKING, vsTrackingInfo);
    updateStatusDataUpdateTime(StatusModel.TRACKING_UPDATE_TIME);
  }

  /*--------------------------------------------------------------------------*/
  public void clearAllTrackingStatuses()
  {
    Collection<Map<String,String>> statusesCollection = mpStatusItems.values();
    if (statusesCollection != null)
    {
      Iterator<Map<String,String>> statusesIterator = statusesCollection.iterator();
      while (statusesIterator.hasNext())
      {
        Map<String,String> vpStatusData = statusesIterator.next();
        vpStatusData.put(StatusModel.TRACKING, null);
      }
    }
  }

  /*--------------------------------------------------------------------------*/
  public static final String[] CONTROLLER_STATUS_UPDATE = {
    StatusModel.NAME,
    StatusModel.OPERATING_MODE,
    StatusModel.ONLINE_STATUS,
    StatusModel.OPERATING_STATUS,
    StatusModel.OPERATING_STATUS_DETAIL,
    StatusModel.HEARTBEAT_RESPONSE_TIME,
    StatusModel.ERROR,
    StatusModel.LAST_UPDATE_TIME
  };

  public static final String[] EQUIPMENT_STATUS_UPDATE = {
    StatusModel.NAME,
    StatusModel.OPERATING_STATUS,
    StatusModel.ERROR,
    StatusModel.ERROR_SET,
    StatusModel.LAST_UPDATE_TIME
  };

  public static final String[] TRACKING_STATUS_UPDATE = {
    StatusModel.TRACKING,
  };

  /*--------------------------------------------------------------------------*/
  public String getAllStatusesUpdate(String[] dataNames)
  {
    StatusEventDataFormat vpSEDF = new StatusEventDataFormat(getClass().getSimpleName());
    vpSEDF.setType(ControllerConsts.UPDATE_STATUS);

    Collection<Map<String,String>> statusesCollection = mpStatusItems.values();
    if (statusesCollection != null)
    {
      Iterator<Map<String,String>> statusesIterator = statusesCollection.iterator();
      while (statusesIterator.hasNext())
      {
        Map<String,String> vpStatusData = statusesIterator.next();
        boolean haveRealStatusValues = false;
        int viDataNamesLength = dataNames.length;
        String[] vasParams = new String[dataNames.length];
        for (int i = 0; i < viDataNamesLength; i++)
        {
          String key = dataNames[i];
          String value = vpStatusData.get(key);
          if (value == null)
          {
            value = UNKNOWN;
          }
          else
          {
            // Inactive equipment has the INACTIVE property
            if (vpStatusData.get(INACTIVE) == null)
            {
              haveRealStatusValues = true;
            }
          }
          vasParams[i] = value;
        }
        if (haveRealStatusValues)
        {
          vpSEDF.addStatusMessage(vasParams);
        }
      }
    }

    return vpSEDF.createStringToSend();
  }

  /**
   * Return a Load ID for the caller's Tracking ID. The caller's Tracking ID may
   * be the Load ID if the Load ID is not long enough to be need a shorter
   * Tracking ID.
   *
   * @param isTrackingId tracking id that may be the loadID
   * @return a load ID (may be TrackingId)
   */
  private String getTrackingsLoadId(String isTrackingId)
  {
    if (mpSchedServer == null)
    {
      mpSchedServer = Factory.create(StandardSchedulerServer.class);
    }
    try
    {
      return mpSchedServer.getLoadIdFromTrackingId(isTrackingId);
    }
    catch (DBException dbe)
    {
      mpLogger.logException(dbe);
      return isTrackingId;
    }
  }
}

