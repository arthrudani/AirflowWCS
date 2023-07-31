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

import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusEventDataFormat;
import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentMonitorFrame;
import com.daifukuamerica.wrxj.swingui.equipment.GroupPanel;
import com.daifukuamerica.wrxj.swingui.equipment.popup.ShuttleCartButtonPopupMenu;
import java.awt.Polygon;
import java.util.Map;

/**
 * <B>Description:</B> Shuttle Cart
 * <BR>"Retrieve" really means "Retrieve"
 *
 * @author       mandrus<BR>
 * @version      1.0
 * 
 * <BR>Copyright (c) 2008 by Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class ShuttleCartButton extends StationButton
{
  /*
   * Parameter constants
   */
  protected static final String STATION_LOCATION = "Location";
  
  protected static final String BOTTOM = "Bottom";
  protected static final String LEFT = "Left";
  protected static final String RIGHT = "Right";
  protected static final String TOP = "Top";
  
  /*
   * Arrows
   */
  protected static final char ARROW_UP    = 0x25B2;
  protected static final char ARROW_RIGHT = 0x25BA;
  protected static final char ARROW_DOWN  = 0x25BC;
  protected static final char ARROW_LEFT  = 0x25C4;
  
  /*
   * Text
   */
  protected String msRetrieve;
  protected String msStore;
  
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
  public ShuttleCartButton(EquipmentMonitorFrame ipEMF, int inPanelIndex,
      GroupPanel ipParent, String isGroupName, String isDeviceName,
      Polygon ipPolygon, Map<String, String> ipProperties, boolean izCanTrack,
      SKDCScreenPermissions ipPermissions)
  {
    super(ipEMF, inPanelIndex, ipParent, isGroupName, isDeviceName, ipPolygon,
        ipProperties, izCanTrack, ipPermissions);
  }
  
  /**
   * Set the graphics properties
   * 
   * @param ipProperties
   */
  @Override
  protected void setProperties(Map<String, String> ipProperties)
  {
    msRetrieve = "Retrieve";
    msStore    = "Store";
    
    super.setProperties(ipProperties);
  }

  /**
   * Keep the station ID in the status message
   */
  @Override
  public void setStatusText(String isStatusText)
  {
    if (isStatusText.equals(StatusEventDataFormat.STATUS_RETRIEVE))
      isStatusText = msRetrieve;
    if (isStatusText.equals(StatusEventDataFormat.STATUS_STORE))
      isStatusText = msStore;
    
    super.setStatusText(isStatusText);
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
        msRetrieve = "" + ARROW_UP;
        msStore = "" + ARROW_DOWN;
      }
      else if (isValue.equals(BOTTOM))
      {
        msRetrieve = "" + ARROW_DOWN;
        msStore = "" + ARROW_UP;
      }
      else if (isValue.equals(LEFT))
      {
        msRetrieve = "" + ARROW_LEFT;
        msStore = "" + ARROW_RIGHT;
      }
      else if (isValue.equals(RIGHT))
      {
        msRetrieve = "" + ARROW_RIGHT;
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

  /**
   * Handle problems processing the graphic parameters
   * 
   * @param isParameter
   * @param ipException
   */
  @Override
  protected void handleParameterError(String isParameter, Exception ipException)
  {
    super.handleParameterError(isParameter, ipException);
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
    if (!mzIsFuture && mzIsAssignedJVM)
    {
      mpPopup = Factory.create(ShuttleCartButtonPopupMenu.class, ipEMF,
          inPanelIndex, isGroupName, this, izCanTrack, ipPermissions);
    }
  }
}
