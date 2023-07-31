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

import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentMonitorFrame;
import com.daifukuamerica.wrxj.swingui.equipment.GroupPanel;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.awt.Polygon;
import java.util.Map;

@SuppressWarnings("serial")
public class CaptiveFullnessButton extends FullnessButton
{
  StandardLoadServer mpLoadServer;
  StandardLocationServer mpLocnServer;

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
  public CaptiveFullnessButton(EquipmentMonitorFrame ipEMF, int inPanelIndex,
      GroupPanel ipParent, String isGroupName, String isDeviceName,
      Polygon ipPolygon, Map<String, String> ipProperties, boolean izCanTrack,
      SKDCScreenPermissions ipPermissions)
  {
    super(ipEMF, inPanelIndex, ipParent, isGroupName, isDeviceName, ipPolygon,
        ipProperties, izCanTrack, ipPermissions);
  }

  /**
   * @see com.daifukuamerica.wrxj.swingui.equipment.button.FullnessButton#setProperties(java.util.Map)
   */
  @Override
  protected void setProperties(Map<String, String> ipProperties)
  {
    super.setProperties(ipProperties);

    //  Since it'll be in a different thread the next time we need it, we'll
    //  set it to null and reinitialize it.
    mpLoadServer = null;
  }

  /**
   * Refresh the status of the instantiated EquipmentGraphic object.
   * This method is to allow the equipment monitor to update based on local
   * WarehouseRx screen events.
   *
   * @return status - the status
   */
  @Override
  public void refreshStatus()
  {
    int vnPercentFull = -1;
    if (mnTotalLocations > 0)
    {
      try
      {
        if (mpLoadServer == null)
          mpLoadServer = Factory.create(StandardLoadServer.class);

        if (mpLocServer == null)
          mpLocServer = Factory.create(StandardLocationServer.class);

        /*
         * The number calculated here means the total percentage of all loads that
         * have quantity in them. So for example suppose in the captive system there
         * are 132 total locations all with loads in them and 5 of those loads have product.
         * The number calculated here would be 4% meaning that's the percentage of loads
         * with product.
         */
        double vdFullness = mpLoadServer.getTotalLoadFullnessByDevice(getMCController());
        vdFullness = Math.ceil(vdFullness);
        int vnLoadCount = mpLocServer.getLocationDeviceCount(getMCController(),
                                                             DBConstants.LCASRS, 0);
        if (vnLoadCount != 0)
        {
          vnPercentFull = SKDCUtility.convertDoubleToInt(100*vdFullness/vnLoadCount);
        }
        else
        {
          vnPercentFull = 0;
        }
      }
      catch(DBException dbe)
      {
        mpLoadServer = null;
      }
    }

    setFullness(vnPercentFull);
    setToolTipText(msToolTip);
  }
}
