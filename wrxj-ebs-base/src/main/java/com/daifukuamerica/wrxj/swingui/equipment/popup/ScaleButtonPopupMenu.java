package com.daifukuamerica.wrxj.swingui.equipment.popup;

import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentGraphic;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentMonitorFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * <B>Description:</B> Adds the bidirectional mode change options
 *
 * <P>Copyright (c) 2008 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
@SuppressWarnings("serial")
public class ScaleButtonPopupMenu extends EquipmentGraphicPopupMenu
{
  // Scale-related items
  protected static final String POPUP_WEIGHT_COMMAND = "WeightCommand";
  protected static final String POPUP_ENTER_WEIGHT = "WeightEnter";


  /**
   * @param ipEMF
   * @param inPanelIndex
   * @param isGroupName
   * @param ipGraphic
   * @param izFrameHasTracking
   * @param ipPermissions
   */
  public ScaleButtonPopupMenu(EquipmentMonitorFrame ipEMF, int inPanelIndex,
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
  protected void addGraphicSpecificPopupMenuItems(SKDCScreenPermissions ipPermissions)
  {
    if (ipPermissions.iAddAllowed)
    {
      addSeparator();
      
      ActionListener vpListener = new ScaleActionListener();
      add("Send Weight Command", POPUP_WEIGHT_COMMAND, vpListener);
      add("Enter Weight", POPUP_ENTER_WEIGHT, vpListener);
    }
  }
  
  /**
   * Action listener for ScaleButton pop-up items
   */
  private class ScaleActionListener implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      String actionCommand = e.getActionCommand();
      if (actionCommand.equals(POPUP_WEIGHT_COMMAND))
      {
        mpEMF.sendWeightCommand(mpGraphic.getStationId());
      }
      else if (actionCommand.equals(POPUP_ENTER_WEIGHT))
      {
        mpEMF.enterWeightCommand(mpGraphic.getStationId());
      }
      else
      {
        mpEMF.unknownCommand(mpGraphic.getGraphicName(), actionCommand);
      }
    }
  }
}
