/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright © 2007 Daifuku America Corporation  All Rights Reserved.
 
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS 
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND 
  CIVIL LIABILITY.
 ****************************************************************************/

package com.daifukuamerica.wrxj.swingui.equipment.button;

import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentMonitorFrame;
import com.daifukuamerica.wrxj.swingui.equipment.GroupPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.Map;

/**
 * <B>Description:</B> Display a text label on the Equipment Monitor<BR>
 *
 * @author       mandrus<BR>
 * @version      1.0
 * 
 * <BR>Copyright (c) 2007 by Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class LabelButton extends PolygonButton
{
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
  public LabelButton(EquipmentMonitorFrame ipEMF, int inPanelIndex,
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

  @Override
  public void setStationId(String s)
  {
    super.setStationId(s);
    setStatusText(s.replace('_', ' '));
  }
  
  /**
   * No ToolTip for a label
   */
  @Override
  public void setToolTipText(String text){}
  
  /**
   * Labels are always online
   */
  @Override
  public int getStatus()
  {
    // Future and non-JVM are always "no status"
    if (mzIsFuture || !mzIsAssignedJVM)
    {
      return NO_STATUS;
    }
    
    // Everything else is "online"
    return ONLINE;
  }
  
  /**
   * No polygon for a label
   */
  @Override
  public void paintComponent(Graphics g)
  {
    if (mzRotateText)
    {
      drawVerticalText(g, msStatusText);
    }
    else
    {
      drawText(g, msStatusText, msStatusText2);
    }
  }
  
  /**
   * Get the proper text color
   * 
   * @param ipTextColor
   * @return
   */
  @Override
  protected Color getTextColor(Color ipTextColor)
  {
    if (mzIsFuture || !mzIsAssignedJVM)
    {
      return Color.LIGHT_GRAY;
    }
    else
    {
      return Color.BLACK;
    }
  }

}
