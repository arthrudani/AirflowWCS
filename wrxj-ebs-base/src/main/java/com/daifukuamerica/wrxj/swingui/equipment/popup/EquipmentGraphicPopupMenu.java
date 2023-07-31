package com.daifukuamerica.wrxj.swingui.equipment.popup;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.device.agc.AGCMOSMessage;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentGraphic;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentMonitorFrame;
import com.daifukuamerica.wrxj.swingui.equipment.button.ConveyorButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * <B>Description:</B> Pop-up menu for 9y, AS21 SRC and AS21 AGC equipment
 * graphics buttons.
 * 
 * <P>Copyright (c) 2008 by Daifuku America Corporation</P>
 * 
 * @author mandrus
 * @version 1.0
 */
@SuppressWarnings("serial")
public class EquipmentGraphicPopupMenu extends SKDCPopupMenu
{
  // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  // +  More or less universal items                                         +
  // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

  // HTML Error help
  protected static final String POPUP_DISPLAY_ERROR_HELP = "Dsp-Err-Hlp";
  
  // Simply text pop-up
  protected static final String POPUP_DISPLAY_ERROR_TEXT = "Dsp-Err";

  // Communications test
  protected static final String POPUP_MC_COMM = "MC-Comm";

  // Status request
  protected static final String POPUP_MC_STATUS = "MC-Status";

  // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  // +  MOS-related items                                                    +
  // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  
  // Tracking
  protected static final String POPUP_DETAILSON = "DetailsON";
  protected static final String POPUP_DETAILSOFF = "DetailsOFF";

  // Start/stop this one
  protected static final String POPUP_START_EQUIP = "StartOne";
  protected static final String POPUP_STOP_EQUIP = "StopOne";
  
  // Start/stop this aisle
  protected static final String POPUP_START_AISLE = "StartAisle";
  protected static final String POPUP_STOP_AISLE = "StopAisle";
  
  // Silence/reset error
  protected static final String POPUP_SILENCE = "Silence";
  protected static final String POPUP_RESET = "Reset";
  
  // Disconnect/recover equipment
  protected static final String POPUP_DISCONNECT_EQUIPMENT = "DiscEqpmnt";
  protected static final String POPUP_RECOVER_EQUIPMENT = "RcvrEqpmnt";

  // Latch clear
  protected static final String POPUP_LATCH_CLEAR = "LatchClear";
  protected static final String POPUP_LATCH_CLEAR_ALL = "LatchClearAll";

  // Save logs
  protected static final String POPUP_SAVE_LOGS = "SvLgs";
  
  // Communications test
  protected static final String POPUP_MOS_COMM = "MOS-Comm";

  // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  // +  MC-related items                                                     +
  // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

  // Start/stop SRC
  protected static final String POPUP_SRC_ONLINE = "SRCOnline";
  protected static final String POPUP_SRC_OFFLINE = "SRCOffline";
  
  //==========================================================================

  protected EquipmentMonitorFrame mpEMF;
  protected int mnPanelIndex;
  protected String msGroupName;
  protected EquipmentGraphic mpGraphic;

  /**
   * Constructor
   */
  public EquipmentGraphicPopupMenu(EquipmentMonitorFrame ipEMF,
      int inPanelIndex, String isGroupName, EquipmentGraphic ipGraphic,
      boolean izFrameHasTracking, SKDCScreenPermissions ipPermissions)
  {
    super();
    mpEMF = ipEMF;
    mnPanelIndex = inPanelIndex;
    msGroupName = isGroupName;
    mpGraphic = ipGraphic;
    
    addMenuItems(ipGraphic, izFrameHasTracking, ipPermissions);
  }

  /**
   * Define the pop-up based upon the graphic type
   * 
   * @param ipGraphic
   * @param izFrameHasTracking
   * @param ipPermissions
   */
  protected void addMenuItems(EquipmentGraphic ipGraphic,
      boolean izFrameHasTracking, SKDCScreenPermissions ipPermissions)
  {
    ActionListener vpListener = new EGPMActionListener();
    
    boolean vzHasMcConnection  = ipGraphic.getMCController() != null;
    boolean vzHasMosConnection = ipGraphic.getMOSController() != null;

    // If this frame has tracking, show the menu options for them.
    if (izFrameHasTracking)
    {
      add("Load Tracking ON", POPUP_DETAILSON, vpListener);
      add("Load Tracking OFF", POPUP_DETAILSOFF, vpListener);
      addSeparator();
    }
    
    // Show the menu options for the error display.
    if (ipGraphic.getErrorSet() != null)
    {
      if (mpEMF.doesErrorClassHaveHelp(ipGraphic.getErrorClass()))
      {
        add("Display Error Help (" + ipGraphic.getErrorSet() + ")", 
            POPUP_DISPLAY_ERROR_HELP, vpListener);
      }
      add("Display Error Text (" + ipGraphic.getErrorSet() + ")", 
          POPUP_DISPLAY_ERROR_TEXT, vpListener);
    }
    
    // If there is a MOS connection, allow options to reset/silence errors
    if (vzHasMosConnection && ipPermissions.iModifyAllowed)
    {
      add("Reset Error", POPUP_RESET, vpListener);
      add("Silence Alarm", POPUP_SILENCE, vpListener);
    }

    // This is a good place to place graphic specific options
    addGraphicSpecificPopupMenuItems(ipPermissions);
    
    // Options needed if there is a MOS connection
    if (vzHasMosConnection)
    {
      if (ipPermissions.iModifyAllowed)
      {
        // TODO: Test start/stop one piece of equipment with 21 (MOS) with real equipment
        // Start/stop one piece of equipment with 21 (MOS)
        addSeparator();
        add("Start Equipment", POPUP_START_EQUIP, vpListener);
        add("Stop Equipment", POPUP_STOP_EQUIP, vpListener);
        
        // Start SRC with 01 (MC)
//        addSeparator();
//        add("SRC Online", POPUP_SRC_ONLINE, vpListener);
//        add("SRC Offline", POPUP_SRC_OFFLINE, vpListener);
        
        // Start SRC with 21 (MOS)
        addSeparator();
        add("SRC Online", POPUP_START_AISLE, vpListener);
        add("SRC Offline", POPUP_STOP_AISLE, vpListener);
        
        // Disconnect/recover the SRC (MOS)
        addSeparator();
        add("Disconnect Equipment", POPUP_DISCONNECT_EQUIPMENT, vpListener);
        add("Recover Equipment", POPUP_RECOVER_EQUIPMENT, vpListener);

        // Latch clear (MOS) (only for 121xxx)
        addSeparator();
        String vsMachineId = ipGraphic.getMOSID();
        vsMachineId = vsMachineId.substring(vsMachineId.indexOf(':') + 1);
        if (vsMachineId.startsWith("121") || ipGraphic instanceof ConveyorButton)
        {
          add("Latch Clear", POPUP_LATCH_CLEAR, vpListener);
        }
        add("Latch Clear (ALL)", POPUP_LATCH_CLEAR_ALL, vpListener);
      }
    }
    // Options needed if there is only a MC connection
    else if (vzHasMcConnection && ipPermissions.iModifyAllowed)
    {
      addSeparator();
      String vsDeviceType = mpEMF.getDeviceTypeForGroup(msGroupName);
      add(vsDeviceType + " Online", POPUP_SRC_ONLINE, vpListener);
      add(vsDeviceType + " Offline", POPUP_SRC_OFFLINE, vpListener);
    }
    
    addSeparator();
    
    // More MOS stuff
    if (vzHasMosConnection)
    {
      if (ipPermissions.iAddAllowed)
      {
        add("Save Logs", POPUP_SAVE_LOGS, vpListener);
      }
      add("MOS Comm Test", POPUP_MOS_COMM, vpListener);
    }

    // Send MC communications test (TODO: Change from global to group)
    if (Application.getBoolean("MCCommTest", true))
    {
      add("MC Comm Test", POPUP_MC_COMM, vpListener);
    }
    
    // Send MC status request (TODO: Change from global to group)
    if (Application.getBoolean("MCStatusRequest", false))
    {
      add("MC Status Request", POPUP_MC_STATUS, vpListener);
    }
  }

  /**
   * Graphic specific menu items are inserted immediately following the standard
   * error items.
   * 
   * @param ipPermissions
   */
  protected void addGraphicSpecificPopupMenuItems(
      SKDCScreenPermissions ipPermissions)
  {
    // None for the base version
  }
  
  /**
   * <B>Description:</B> ActionListener for pop-up
   *
   * <P>Copyright (c) 2008 by Daifuku America Corporation</P>
   *
   * @author       mandrus
   * @version      1.0
   */
  private class EGPMActionListener implements ActionListener
  {
    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
      String actionCommand = e.getActionCommand();
      if (actionCommand.equals(POPUP_DETAILSON))
      {
        mpEMF.trackingOn(mnPanelIndex, msGroupName,
            mpGraphic.getMOSController(), mpGraphic.getGraphicParent());
      }
      else if (actionCommand.equals(POPUP_DETAILSOFF))
      {
        mpEMF.trackingOff(msGroupName, mpGraphic.getMOSController());
      } 
      else if (actionCommand.equals(POPUP_DISPLAY_ERROR_TEXT))
      {
        mpEMF.displayErrorText(mpGraphic.getGraphicName(),
            mpGraphic.getErrorClass(), mpGraphic.getErrorCode());
      }
      else if (actionCommand.equals(POPUP_DISPLAY_ERROR_HELP))
      {
        mpEMF.displayErrorHelp(mpGraphic.getGraphicName(),
            mpGraphic.getErrorClass(), mpGraphic.getErrorCode());
      }
      else if (actionCommand.equals(POPUP_START_EQUIP))
      {
        mpEMF.startEquipment(msGroupName, mpGraphic.getMOSController(),
            mpGraphic.getMOSID());
      }
      else if (actionCommand.equals(POPUP_STOP_EQUIP))
      {
        mpEMF.stopEquipment(msGroupName, mpGraphic.getMOSController(),
            mpGraphic.getMOSID(), mpGraphic.getGraphicName());
      }
      else if (actionCommand.equals(POPUP_START_AISLE))
      {
        mpEMF.startAisle(msGroupName, mpGraphic.getMOSController());
      }
      else if (actionCommand.equals(POPUP_STOP_AISLE))
      {
        mpEMF.stopAisle(msGroupName, mpGraphic.getMOSController(),
            mpGraphic.getMCController());
      }
      else if (actionCommand.equals(POPUP_RESET))
      {
        mpEMF.resetError(msGroupName, mpGraphic.getMOSID(), mpGraphic.getMOSController());
      }
      else if (actionCommand.equals(POPUP_SILENCE))
      {
        mpEMF.silenceAlarms(msGroupName, mpGraphic.getMOSController());
      }
      else if (actionCommand.equals(POPUP_DISCONNECT_EQUIPMENT))
      {
        mpEMF.disconnectEquipment(msGroupName, mpGraphic.getMOSID(), 
            mpGraphic.getMOSController());
      }
      else if (actionCommand.equals(POPUP_RECOVER_EQUIPMENT))
      {
        mpEMF.reconnectEquipment(msGroupName, mpGraphic.getMOSID(), 
            mpGraphic.getMOSController());
      }
      else if (actionCommand.equals(POPUP_SRC_ONLINE))
      {
        mpEMF.startDevice(msGroupName, mpGraphic.getMCController());
      }
      else if (actionCommand.equals(POPUP_SRC_OFFLINE))
      {
        mpEMF.stopDevice(msGroupName, mpGraphic.getMCController());
      }
      else if (actionCommand.equals(POPUP_SAVE_LOGS))
      {
        mpEMF.saveLogs(msGroupName, mpGraphic.getMOSController());
      }
      else if (actionCommand.equals(POPUP_MC_COMM))
      {
        mpEMF.sendMcCommTest(msGroupName, mpGraphic.getMCController());
      }
      else if (actionCommand.equals(POPUP_MC_STATUS))
      {
        mpEMF.sendStatusRequest(msGroupName, mpGraphic.getMCController());
      }
      else if (actionCommand.equals(POPUP_MOS_COMM))
      {
        mpEMF.sendMosCommTest(msGroupName, mpGraphic.getMOSController());
      }
      else if (actionCommand.equals(POPUP_LATCH_CLEAR))
      {
        mpEMF.latchClear(mpGraphic.getMOSID(), mpGraphic.getMOSController());
      }
      else if (actionCommand.equals(POPUP_LATCH_CLEAR_ALL))
      {
        mpEMF.latchClear(AGCMOSMessage.LATCH_CLEAR_ALL_MOSID,
            mpGraphic.getMOSController());
      }
      else
      {
        mpEMF.unknownCommand(mpGraphic.getGraphicName(), actionCommand);
      }
    }
  }
}
