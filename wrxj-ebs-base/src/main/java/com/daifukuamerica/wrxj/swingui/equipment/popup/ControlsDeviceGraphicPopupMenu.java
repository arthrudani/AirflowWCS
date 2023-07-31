package com.daifukuamerica.wrxj.swingui.equipment.popup;

import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentGraphic;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentMonitorFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created on Jan 27, 2004
 * @author Stephen Kendorski
 *
 */
@SuppressWarnings("serial")
public class ControlsDeviceGraphicPopupMenu extends EquipmentGraphicPopupMenu
{
  private static final String POPUP_COMM = "CommTest";
  
  /**
   * @param ipEMF
   * @param inPanelIndex
   * @param isGroupName
   * @param ipGraphic
   * @param izFrameHasTracking
   * @param ipPermissions
   */
  public ControlsDeviceGraphicPopupMenu(EquipmentMonitorFrame ipEMF,
      int inPanelIndex, String isGroupName, EquipmentGraphic ipGraphic,
      boolean izFrameHasTracking, SKDCScreenPermissions ipPermissions)
  {
    super(ipEMF, inPanelIndex, isGroupName, ipGraphic, izFrameHasTracking,
        ipPermissions);
  }

  /**
   * @see com.daifukuamerica.wrxj.swingui.equipment.popup.EquipmentGraphicPopupMenu#addMenuItems(
   *      com.daifukuamerica.wrxj.swingui.equipment.EquipmentGraphic,
   *      boolean, com.daifukuamerica.wrxj.swing.SKDCScreenPermissions)
   */
  @Override
  protected void addMenuItems(EquipmentGraphic ipGraphic,
      boolean izFrameHasTracking, SKDCScreenPermissions ipPermissions)
  {
    add("Comm Test", POPUP_COMM, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        sendCommTest((EquipmentGraphic)getInvoker());
      }
    });
  }
  
  /**
   * Send a comm test
   * 
   * @param vpEG
   */
  protected void sendCommTest(EquipmentGraphic vpEG)
  {
    if (vpEG != null)
    {
      String vsDeviceName = vpEG.getGraphicName();
      String vsGroupName = vsDeviceName.substring(0, vsDeviceName.indexOf(':'));
      String vsController = vpEG.getMCController();
      mpEMF.sendControlsCommTest(vsGroupName, vsController);
    }
  }
}
