package com.daifukuamerica.wrxj.swingui.equipment.popup;

import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentGraphic;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentMonitorFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * <B>Description:</B> Adds the "Send Bar Code" option
 *
 * <P>Copyright (c) 2008 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
@SuppressWarnings("serial")
public class BcrButtonPopupMenu extends EquipmentGraphicPopupMenu
{
  // Send bar code to bar code reader
  protected static final String POPUP_SEND_BAR_CODE = "SendBarCode";

  /**
   * @param ipEMF
   * @param inPanelIndex
   * @param isGroupName
   * @param ipGraphic
   * @param izFrameHasTracking
   * @param ipPermissions
   */
  public BcrButtonPopupMenu(EquipmentMonitorFrame ipEMF, int inPanelIndex,
      String isGroupName, EquipmentGraphic ipGraphic,
      boolean izFrameHasTracking, SKDCScreenPermissions ipPermissions)
  {
    super(ipEMF, inPanelIndex, isGroupName, ipGraphic, izFrameHasTracking,
        ipPermissions);
  }

  /**
   * @see com.daifukuamerica.wrxj.swingui.equipment.popup.EquipmentGraphicPopupMenu#addGraphicSpecificPopupMenuItems()
   */
  @Override
  protected void addGraphicSpecificPopupMenuItems(
      SKDCScreenPermissions ipPermissions)
  {
    if (ipPermissions.iAddAllowed)
    {
      addSeparator();
      add("Send Bar Code", POPUP_SEND_BAR_CODE, new BcrActionListener());
    }
  }
  
  /**
   * Action listener for BcrButton pop-up items
   */
  private class BcrActionListener implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      mpEMF.sendBarCode(msGroupName, mpGraphic.getStationId(), 
          mpGraphic.getMOSID(), mpGraphic.getMOSController());
    }
  }
}
