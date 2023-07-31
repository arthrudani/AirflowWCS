package com.daifukuamerica.wrxj.swingui.equipment.button;

import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentMonitorFrame;
import com.daifukuamerica.wrxj.swingui.equipment.GroupPanel;
import com.daifukuamerica.wrxj.swingui.location.LocationUtilizationFrame;
import java.awt.Polygon;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * <B>Description:</B> Display a Rack Fullness indicator.  One is needed 
 * per device.  This particular button does no calculations on its own; it 
 * relies on the FullnessMonitor or CaptiveFullnessMonitor task.
 * 
 * <P>Sample equipment configuration (must remove parenthetical notes):
 * <pre>
 *  EquipmentName   SRC-5:Fullness
 *  Description     FullnessMonitor
 *  Category        Equipment           (pretend this is monitoring equipment)
 *  ErrorSet        AS21
 *  MCController    Monitor:Fullness    (FULLNESS_MONITOR_NAME)
 *  MOSController   *NONE*
 *  AisleGroup      *NONE*
 *  HostID          *NONE*
 *  DeviceID        *NONE*
 *  StationID       *NONE*
 *  MCID            SRC5                (device)
 *  MOSID           SRC5                (device)
 *  GraphicClass    FullnessMonitorButton
 * </pre></P>
 *
 * @author       mandrus<BR>
 * @version      1.0
 * 
 * <BR>Copyright (c) 2009 by Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class FullnessMonitorButton extends PortButton
{
  protected String msToolTip;
  
  EquipmentMonitorFrame mpEMF;

  /**
   * Public constructor for Factory
   * 
   * @param ipEMF - Our Equipment Monitor Frame
   * @param inPanelIndex - The index of our panel
   * @param ipParent - Our panel
   * @param isGroupName - Our group name
   * @param isDeviceName - Our device name
   * @param ipPolygon - Our shape
   * @param ipProperties - Our properties
   * @param izCanTrack - Can we display tracking?
   * @param ipPermissions
   */
  public FullnessMonitorButton(EquipmentMonitorFrame ipEMF, int inPanelIndex,
      GroupPanel ipParent, String isGroupName, String isDeviceName,
      Polygon ipPolygon, Map<String, String> ipProperties, boolean izCanTrack,
      SKDCScreenPermissions ipPermissions)
  {
    super(ipEMF, inPanelIndex, ipParent, isGroupName, isDeviceName, ipPolygon,
        ipProperties, izCanTrack, ipPermissions);
    super.setStatusText("Unknown");
    mpEMF = ipEMF;
  }
  
  /**
   * Set the graphics properties
   * 
   * @param ipProperties
   */
  @Override
  protected void setProperties(Map<String, String> ipProperties)
  {
    mnDefaultFontSize = 18;
    
    super.setProperties(ipProperties);
    mzCanTrack = false;
    
    refreshStatus();
  }
  
  /**
   * Set the tool-tip text
   */
  @Override
  public void setToolTipText(String text)
  {
    if (text != null)
    {
      msToolTip = text;
      super.setToolTipText(text + "  ("
          + new SimpleDateFormat("HH:mm:ss").format(new Date()) + ")");
    }
  }
  
  /**
   * Insert the device ID at the beginning of the text
   */
  @Override
  public void setStatusText(String isStatusText)
  {
    setToolTipText("Device " + getMOSID() + ": " + isStatusText + " Full");
    super.setStatusText(isStatusText);
  }
  
  /**
   * This method is called on a double-click
   */
  @Override
  protected void displayTracking()
  {
    LocationUtilizationFrame vpLUF = Factory.create(
        LocationUtilizationFrame.class, getMOSID());
    mpEMF.addSKDCInternalFrame(vpLUF);
  }
}
