package com.daifukuamerica.wrxj.swingui.equipment;

import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.observer.ObservableControllerImpl;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusEventDataFormat;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Stephen Kendorski
 * @version 1.0
 */
 public class GraphicDeviceView implements Observer
{
  // Logger
  protected Logger mpLogger = Logger.getLogger();

  // This view (tab) name and map of devices for this view
  protected String msEquipmentGroup = null;
  protected HashMap<String, EquipmentGraphic> mpDeviceGraphics;

  // Use for missing graphics so we don't log the missing graphic ad infinitum
  protected EquipmentGraphic mpMissingGraphic = null;

  /**
   * Constructor
   */
  public GraphicDeviceView()
  {
  }

  /**
   * Set the graphics for the view 
   * 
   * @param ipGraphics
   */
  public void setGraphicsMap(Map<String,EquipmentGraphic> ipGraphics)
  {
    mpDeviceGraphics = new HashMap<>(ipGraphics);
  }

  /**
   * Set the view name
   * 
   * @param isEquipmentGroup
   */
  public void setEquipmentGroup(String isEquipmentGroup)
  {
    msEquipmentGroup = isEquipmentGroup;
  }

  /**
   * Set a default graphic to use in the place of missing ones
   * 
   * @param ipEquipmentGraphic
   */
  public void setMissingGraphic(EquipmentGraphic ipEquipmentGraphic)
  {
    mpMissingGraphic = ipEquipmentGraphic;
  }
  
  /**
   * Process update messages
   */
  @Override
  public void update(Observable o, Object arg)
  {
    ObservableControllerImpl observableImpl = (ObservableControllerImpl)o;
    String sText = observableImpl.getStringData();
    switch (sText.charAt(0))
    {
      case ControllerConsts.CONTROLLER_STATUS:
      case ControllerConsts.UPDATE_STATUS:
        processStatusChanges(sText);
        break;
      case ControllerConsts.NUDGE_EQUIPMENT_MONITOR:
        nudgeGraphicsDisplay();
        break;
    }
  }

  /**
   * Process status change messages
   * 
   * @param isStatusMessage
   */
  private void processStatusChanges(String isStatusMessage)
  {
    StatusEventDataFormat vpSEDF = new StatusEventDataFormat(getClass().getSimpleName());
    vpSEDF.setMessage(isStatusMessage);
    List<StatusInfo> vpStatusList = vpSEDF.getStatusList() ;
    for (StatusInfo s : vpStatusList)
    {
      String vsMachineId = s.getUpdateMachine();
      String vsStatus = s.getUpdateDesc();
      String vsErrorCode = s.getUpdateError();
      
      if (vsErrorCode.equals(StatusEventDataFormat.NONE))
      {
        vsErrorCode = "";
      }
      EquipmentGraphic vpGraphicDevice = mpDeviceGraphics.get(vsMachineId);
      if (vpGraphicDevice != null)
      {
        vpGraphicDevice.setErrorCode(vsErrorCode);
        if ((vsStatus.indexOf(StatusEventDataFormat.STATUS_ONLINE) == 0) ||
            (vsStatus.indexOf(StatusEventDataFormat.STATUS_RUNNING) == 0))
        {
          vpGraphicDevice.setStatus(EquipmentGraphic.ONLINE);
        }
        else if((vsStatus.indexOf(StatusEventDataFormat.STATUS_STOPPED) == 0) ||
                (vsStatus.indexOf(StatusEventDataFormat.STATUS_OFFLINE) == 0))
        {
          vpGraphicDevice.setStatus(EquipmentGraphic.OFFLINE);
        }
        else if(vsStatus.indexOf(StatusEventDataFormat.STATUS_UNKNOWN) != -1)
        {
          vpGraphicDevice.setStatus(EquipmentGraphic.UNKNOWN);
        }
        else if(vsStatus.indexOf(StatusEventDataFormat.STATUS_ERROR) != -1)
        {
          vpGraphicDevice.setStatus(EquipmentGraphic.ERROR);
        }
        else if(vsStatus.indexOf(StatusEventDataFormat.STATUS_DISCONNECT) != -1)
        {
          vpGraphicDevice.setStatus(EquipmentGraphic.DISCONNECTED);
        }
        else if(vsStatus.indexOf(StatusEventDataFormat.STATUS_LOCTOLOC) != -1)
        {
          vpGraphicDevice.setStatus(EquipmentGraphic.PROCESSING);
        }
        else
          vpGraphicDevice.setStatus(EquipmentGraphic.ALARM);
        int newLine = vsStatus.indexOf('|');
        if (newLine != -1)
        {
          vpGraphicDevice.setStatusText(vsStatus.substring(newLine + 1));
        }
      }
      else
      {
        if (msEquipmentGroup == null ||
            vsMachineId.substring(0,vsMachineId.indexOf(':')).equalsIgnoreCase(msEquipmentGroup))
        {
          mpLogger.logError("NO GraphicDevice for \"" + vsMachineId + "\" - GraphicDeviceView");
          mpDeviceGraphics.put(vsMachineId, mpMissingGraphic);
        }
      }
    }
  }

  /**
   * Process a nudge message
   */
  protected void nudgeGraphicsDisplay()
  {
    for (EquipmentGraphic vpEG : mpDeviceGraphics.values())
    {
      vpEG.refreshStatus();
    }
  }
}