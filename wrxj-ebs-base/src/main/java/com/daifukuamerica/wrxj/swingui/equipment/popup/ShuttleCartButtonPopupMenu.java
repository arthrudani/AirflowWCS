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
public class ShuttleCartButtonPopupMenu extends EquipmentGraphicPopupMenu
{
  // Bidirectional crap
  protected static final String POPUP_STORE_MODE = "StrMd";
  protected static final String POPUP_RETRIEVE_MODE = "RtvMd";


  /**
   * @param ipEMF
   * @param inPanelIndex
   * @param isGroupName
   * @param ipGraphic
   * @param izFrameHasTracking
   * @param ipPermissions
   */
  public ShuttleCartButtonPopupMenu(EquipmentMonitorFrame ipEMF,
      int inPanelIndex, String isGroupName, EquipmentGraphic ipGraphic,
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
    if (ipPermissions.iModifyAllowed)
    {
      addSeparator();
      
      ActionListener vpListener = new ModeChangeActionListener();
      add("Store Mode", POPUP_STORE_MODE, vpListener);
      add("Retrieve Mode", POPUP_RETRIEVE_MODE, vpListener);
    }
  }
  
  /**
   * Action listener for ShuttleCartButton pop-up items
   */
  private class ModeChangeActionListener implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      String actionCommand = e.getActionCommand();
      if (actionCommand.equals(POPUP_STORE_MODE))
      {
        mpEMF.requestStoreMode(mpGraphic.getGraphicName(),
            mpGraphic.getStationId(), mpGraphic.getMCController());
      }
      else if (actionCommand.equals(POPUP_RETRIEVE_MODE))
      {
        mpEMF.requestRetrieveMode(mpGraphic.getGraphicName(), 
            mpGraphic.getStationId(), mpGraphic.getMCController());
      }    
      else
      {
        mpEMF.unknownCommand(mpGraphic.getGraphicName(), actionCommand);
      }
    }
  }
}
