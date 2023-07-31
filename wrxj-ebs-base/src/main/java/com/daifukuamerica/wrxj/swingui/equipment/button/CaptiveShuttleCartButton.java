/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright © 2008 Daifuku America Corporation  All Rights Reserved.
 
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
import java.awt.Polygon;
import java.util.Map;

/**
 * <B>Description:</B> Captive Shuttle Cart
 * <BR>"Retrieve" really means "Retrieve for Storage"
 *
 * @author       mandrus<BR>
 * @version      1.0
 * 
 * <BR>Copyright (c) 2008 by Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class CaptiveShuttleCartButton extends ShuttleCartButton
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
  public CaptiveShuttleCartButton(EquipmentMonitorFrame ipEMF,
      int inPanelIndex, GroupPanel ipParent, String isGroupName,
      String isDeviceName, Polygon ipPolygon, Map<String, String> ipProperties,
      boolean izCanTrack, SKDCScreenPermissions ipPermissions)
  {
    super(ipEMF, inPanelIndex, ipParent, isGroupName, isDeviceName, ipPolygon,
        ipProperties, izCanTrack, ipPermissions);
  }

  /**
   * Assign a value to a parameter
   * 
   * @param isParameter
   * @param isValue
   * @return true if the parameter was assigned, false if invalid
   */
  @Override
  protected boolean assignParameter(String isParameter, String isValue)
  {
    if (isParameter.equals(STATION_LOCATION))
    {
      if (isValue.equals(TOP))
      {
        msRetrieve = "" + ARROW_UP + ARROW_DOWN;
        msStore = "" + ARROW_DOWN;
      }
      else if (isValue.equals(BOTTOM))
      {
        msRetrieve = "" + ARROW_DOWN + ARROW_UP;
        msStore = "" + ARROW_UP;
      }
      else if (isValue.equals(LEFT))
      {
        msRetrieve = "" + ARROW_LEFT + ARROW_RIGHT;
        msStore = "" + ARROW_RIGHT;
      }
      else if (isValue.equals(RIGHT))
      {
        msRetrieve = "" + ARROW_RIGHT + ARROW_LEFT;
        msStore = "" + ARROW_LEFT;
      }
      else
      {
        handleParameterError(isParameter, null);
      }
    }
    else
    {
      return super.assignParameter(isParameter, isValue);
    }
    return true;
  }
}
