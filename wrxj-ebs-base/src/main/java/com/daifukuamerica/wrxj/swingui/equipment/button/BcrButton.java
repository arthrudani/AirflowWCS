package com.daifukuamerica.wrxj.swingui.equipment.button;

import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentMonitorFrame;
import com.daifukuamerica.wrxj.swingui.equipment.GroupPanel;
import com.daifukuamerica.wrxj.swingui.equipment.popup.BcrButtonPopupMenu;
import java.awt.Polygon;
import java.util.Map;

/**
 * <B>Description:</B> Graphic for barcode reader
 *
 * <P>Copyright (c) 2008 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
@SuppressWarnings("serial")
public class BcrButton extends PolygonButton
{
  /**
   * @param ipEMF
   * @param inPanelIndex
   * @param ipParent
   * @param isGroupName
   * @param isDeviceName
   * @param ipPolygon
   * @param ipProperties
   * @param izCanTrack
   * @param ipPermissions
   */
  public BcrButton(EquipmentMonitorFrame ipEMF, int inPanelIndex,
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
    if (!mzIsFuture && mzIsAssignedJVM)
    {
      mpPopup = Factory.create(BcrButtonPopupMenu.class, ipEMF,
          inPanelIndex, isGroupName, this, izCanTrack, ipPermissions);
    }
  }
}
