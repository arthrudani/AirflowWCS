package com.daifukuamerica.wrxj.swingui.equipment.button;

import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentMonitorFrame;
import com.daifukuamerica.wrxj.swingui.equipment.GroupPanel;
import java.awt.Polygon;
import java.util.Map;

/**
 * <B>Description:</B> This is a simple button used for display-only statuses
 * without a pop-up menu.
 *
 * <P>Copyright (c) 2008 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
@SuppressWarnings("serial")
public class PortButton extends PolygonButton
{
  private static String[] STATUS_TEXT = { "Alarm", "ERROR", "Disconnected",
    "Offline", "Unknown", "Online", "Unknown", "None", "Processing" };
  
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
  public PortButton(EquipmentMonitorFrame ipEMF, int inPanelIndex,
      GroupPanel ipParent, String isGroupName, String isDeviceName,
      Polygon ipPolygon, Map<String, String> ipProperties, boolean izCanTrack,
      SKDCScreenPermissions ipPermissions)
  {
    super(ipEMF, inPanelIndex, ipParent, isGroupName, isDeviceName, ipPolygon,
        ipProperties, izCanTrack, ipPermissions);
  }
  
  /**
   * Build this graphic's pop-up menu
   * 
   * @param ipEMF
   * @param inPanelIndex
   * @param isGroupName
   * @param izCanTrack
   * @param ipPermissions
   */
  @Override
  protected void buildPopup(EquipmentMonitorFrame ipEMF, int inPanelIndex,
      String isGroupName, boolean izCanTrack,
      SKDCScreenPermissions ipPermissions)
  {
    // No pop-up
  }

  /**
   * @see com.daifukuamerica.wrxj.swingui.equipment.button.PolygonButton#setStatus(int)
   */
  @Override
  public void setStatus(int inStatus)
  {
    try
    {
      mpStatusColor = STATUS_COLORS[inStatus];
      setStatusText(STATUS_TEXT[inStatus]);
    }
    catch (ArrayIndexOutOfBoundsException aioobe)
    {
      mpStatusColor = STATUS_COLORS[ALARM];
      repaint();
    }
  }
  
  /**
   * @see com.daifukuamerica.wrxj.swingui.equipment.button.PolygonButton#setStatusText(java.lang.String)
   */
  @Override
  public void setStatusText(String isStatusText)
  {
    if (mzIsFuture)
    {
      super.setStatusText("Future");
    }
    else if (!mzIsAssignedJVM)
    {
      super.setStatusText(msJVMNote);
    }
    else
    {
      super.setStatusText(isStatusText);
    }
  }
}
