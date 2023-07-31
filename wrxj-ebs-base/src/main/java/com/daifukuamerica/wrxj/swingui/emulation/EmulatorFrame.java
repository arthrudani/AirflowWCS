package com.daifukuamerica.wrxj.swingui.emulation;

import com.daifukuamerica.wrxj.clc.ControllerListConfiguration;
import com.daifukuamerica.wrxj.clc.ControllerTypeDefinition;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerFactory;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.emulation.agc.AGCDeviceEmulator;
import com.daifukuamerica.wrxj.emulation.arc9y.Arc9yDeviceEmulator;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.messageformat.controlevent.ControlEventDataFormat;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCCheckBox;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.swingui.station.StationComboBox;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;

/**
 * Swing user interface for AGC Emulator control.
 *
 * @author  Stephen Kendorski
 * @version 1.0
 */
@SuppressWarnings("serial")
public class EmulatorFrame extends DacInputFrame implements ActionListener
{
  protected List<String> mpEmulatorDevices = new ArrayList<String>();

  StandardDeviceServer mpDevServer = null;
  StandardStationServer mpStationServer = null;
  
  protected StationComboBox mpStationComboBox;
  protected SKDCComboBox mpDeviceComboBox;
  protected SKDCComboBox mpArrivalComboBox;
  protected SKDCTextField mpMCKeyText;
  protected SKDCTextField mpBarCodeText;
  protected SKDCTextField mpControlText;
  protected SKDCComboBox mpHeightComboBox;
  protected SKDCCheckBox mpChkLoadPresent;

  private static String AR_STORE = "Store";
  private static String AR_FINAL = "Final";
  
  public EmulatorFrame()
  {
    super("Emulation", "Load Arrival Report Details");
    setAllowDuplicateScreens(true);
    jbInit();
  }
  
  /**
   * Build the screen
   * 
   * @throws Exception
   */
  protected void jbInit()
  {
    String [] vapArrivalTypes = {AR_STORE, AR_FINAL};
    mpStationComboBox = new StationComboBox();
    mpDeviceComboBox = new SKDCComboBox();
    mpArrivalComboBox = new SKDCComboBox(vapArrivalTypes);
    mpArrivalComboBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent arg0)
        {
          mpChkLoadPresent.setSelected(true);
          if (mpArrivalComboBox.getSelectedItem() == AR_STORE)
          {
            mpMCKeyText.setText("99999999");
            mpMCKeyText.setEnabled(false);
          }
          else
          {
            mpMCKeyText.setText("");
            mpMCKeyText.setEnabled(true);
          }
        }
      });
    mpMCKeyText = new SKDCTextField(8);
    mpBarCodeText = new SKDCTextField(30);
    mpControlText = new SKDCTextField(30);
    mpHeightComboBox = new SKDCComboBox();
    mpChkLoadPresent = new SKDCCheckBox();

    mpDevServer = Factory.create(StandardDeviceServer.class);
    mpStationServer = Factory.create(StandardStationServer.class);
    EmulatorFill();
    stationFill();
    heightFill();
    mpArrivalComboBox.setSelectedItem(AR_STORE);

    addInput("Emulator:", mpDeviceComboBox);
    addInput("Station:", mpStationComboBox);
    addInput("Arrival Type:", mpArrivalComboBox);
    addInput("MC Key:", mpMCKeyText);
    addInput("Bar Code:", mpBarCodeText);
    addInput("Control Info:", mpControlText);
    addInput("Height:", mpHeightComboBox);
    addInput("Load Present:", mpChkLoadPresent);
    mpChkLoadPresent.setSelected(true);

    mpBtnSubmit.setText("Guess Load ID");
    mpBtnSubmit.setToolTipText(null);
    mpBtnClear.setText("Send Arrival Report");
    mpBtnClear.setToolTipText(null);
    
    SKDCButton vpDeleteBtn = new SKDCButton("Tracking Delete");
    vpDeleteBtn.addActionListener(this);
    mpButtonPanel.add(vpDeleteBtn);
    mpButtonPanel.add(mpBtnClose); // move to end
  }

  /**
   * Action listener for new buttons
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e)
  {
    String vsEmulatorDeviceName = (String)mpDeviceComboBox.getSelectedItem();

    String vsMCKey = JOptionPane.showInputDialog(this, "Enter the MC Key");
    if (vsMCKey != null)
    {
      String vsStation = JOptionPane.showInputDialog(this, "Enter the Station");
      if (vsStation != null)
      {
        String vsControl = JOptionPane.showInputDialog(this,
            "Enter the Control Info");
        if (vsControl != null)
        {
          String vsCmdText = ControlEventDataFormat.getTransportDataDeleteCommand(
              vsStation, vsMCKey, vsControl);
          publishControlEvent(vsCmdText,
              ControlEventDataFormat.SEND_TRANSPORT_DATA_DELETE_TO_EMULATOR,
              vsEmulatorDeviceName);
          displayInfoAutoTimeOut("Message sent");
          return;
        }
      }
    }
    displayInfoAutoTimeOut("No message sent");
  }

  /**
   * Populate the emulator list 
   */
  public void EmulatorFill()
  {
    try
    {
      ControllerListConfiguration vpClc = ControllerFactory.getClc();
      Iterator<String> vpDeviceNames = vpClc.listControllerNames().iterator();
      while (vpDeviceNames.hasNext())
      {
        String vsDevname = vpDeviceNames.next();
        ControllerTypeDefinition vpClcType = vpClc.getControllerTypeDefinition(vpClc.getControllerDefinition(vsDevname).getType());
        Class<? extends Controller> vpClass = vpClcType.getImplementingClass();
        if (AGCDeviceEmulator.class.isAssignableFrom(vpClass))
          mpEmulatorDevices.add(vsDevname);
        else if (Arc9yDeviceEmulator.class.isAssignableFrom(vpClass))
          mpEmulatorDevices.add(vsDevname);
      }
      // If this is a client, it can't see the controllers anymore.  Guess.
      if (mpEmulatorDevices.size() == 0 && SKDCUserData.isSuperUser())
      {
        displayInfoAutoTimeOut("No controllers found... guessing");
        List<Map> vpDevices = mpDevServer.getCtlrDevices();
        DeviceData vpDevData = Factory.create(DeviceData.class);
        for (Map m : vpDevices)
        {
           vpDevData.dataToSKDCData(m);
           switch (vpDevData.getDeviceType())
           {
             case DBConstants.AGC:
             case DBConstants.AGC9X:
             case DBConstants.ARC100:
             case DBConstants.SRC5:
             case DBConstants.SRC9X:
             case DBConstants.SRC9Y:
               mpEmulatorDevices.add(vpDevData.getDeviceID()
                   + SKDCConstants.EMULATION_SUFFIX);
             // No default
           }
        }
      }
    }
    catch (Exception ve)
    {
      logger.logError("Error determining AGC device emulator name: " + ve);
    }
    mpDeviceComboBox.setComboBoxData(mpEmulatorDevices);
  }

  
  /**
   * Fill the station combo box
   */
  protected void stationFill()
  {
    try
    {
      int[] vanInputStations = { DBConstants.USHAPE_IN, DBConstants.PDSTAND,
          DBConstants.INPUT, DBConstants.REVERSIBLE, DBConstants.OUTPUT,    // Kang 06-20-2013
          DBConstants.TRANSFER_STATION, DBConstants.AGC_TRANSFER };
      mpStationComboBox.fill(vanInputStations, SKDCConstants.NO_PREPENDER);
      mpStationComboBox.addItemListener(new ItemListener()
          {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
              guessEmulator();
            }
          });
      guessEmulator();
    }
    catch (DBException e)
    {
      displayError(e.getMessage(), "Unable to get Stations");
    }
  }

  /**
   * Guess which emulator to use.
   */
  protected void guessEmulator()
  {
    StationData vpSD = mpStationServer.getStation(mpStationComboBox.getSelectedStation());
    if (vpSD != null)
    {
      DeviceData vpDD = mpDevServer.getDeviceData(vpSD.getDeviceID());
      String vsEmulator;
      switch (vpDD.getDeviceType())
      {
        case DBConstants.AGC:
        case DBConstants.AGC9X:
        case DBConstants.ARC100:
        case DBConstants.SRC5:
        case DBConstants.SRC9Y:
        case DBConstants.SRC9X:
          vsEmulator = vpDD.getDeviceID() + SKDCConstants.EMULATION_SUFFIX;
          break;
        default:
          vsEmulator = vpDD.getCommDevice() + SKDCConstants.EMULATION_SUFFIX;
      }
      mpDeviceComboBox.setSelectedItem(vsEmulator);
    }
  }
  
  /**
   * Fill the height combo box
   */
  protected void heightFill()
  {
    for (int i = 0; i < 6; i++)
    {
      String s = String.valueOf(i);
      mpHeightComboBox.addItem(s);
    }
  }

  
  /**
   * Clean up on close
   */
  @Override
  public void cleanUpOnClose()
  {
    mpStationServer.cleanUp();
    mpStationServer = null;
  }

  /**
   * Guess the load ID for the MC Key or BCR data
   */
  @Override
  protected void okButtonPressed()
  {
    StandardLoadServer vpLoadServer = Factory.create(StandardLoadServer.class); 
    String vsStation = mpStationComboBox.getSelectedStation();

    LoadData vpLoadData = vpLoadServer.getOldestLoadData(vsStation, DBConstants.ARRIVEPENDING);
    if (vpLoadData == null)
    {
      vpLoadData = vpLoadServer.getOldestLoadData(vsStation, DBConstants.MOVING);
    }
    if (vpLoadData == null)
    {
      vpLoadData = vpLoadServer.getOldestLoadData(vsStation, DBConstants.ARRIVED);
    }
    if (vpLoadData != null)
    {
      if (mpArrivalComboBox.getSelectedItem() == AR_STORE)
      {
        mpBarCodeText.setText(vpLoadData.getMCKey());
      }
      else
      {
        mpMCKeyText.setText(vpLoadData.getMCKey());
      }
      mpHeightComboBox.setSelectedItem("" + vpLoadData.getHeight());
    }
    else
    {
      displayInfo("I give up!  What is it?");
    }
    vpLoadServer.cleanUp();
    vpLoadServer = null;
  }

  
  /**
   * Tell the emulator to send the arrival
   */
  @Override
  protected void clearButtonPressed()
  {
    String vsEmulatorDeviceName = (String)mpDeviceComboBox.getSelectedItem();
    //
    // Get stationId from Description
    //
    String vsStation = mpStationComboBox.getSelectedStation();
    int vnDimenInfo = getDimensionInfo();
    int vnLoadInfo = getLoadInfo();
    
    logger.logOperation(LogConsts.OPR_EMULATOR,
        "Send Load Arrival Report - Station \"" + vsStation + "\"  BarCode \""
            + getBarCodeData() + "\"  Dimen. \"" + vnDimenInfo + "\"");

    String vsCmdText = ControlEventDataFormat.getArrivalCommand(vsStation,
                                       getBarCodeData(), getMCKeyText(),
                                       getControlInfo(), vnDimenInfo,
                                       vnLoadInfo);
    
    publishControlEvent(vsCmdText, ControlEventDataFormat.SEND_ARRIVAL_TO_EMULATOR,
                        vsEmulatorDeviceName);
    if (getBarCodeData().equals("null"))
    {
      displayInfoAutoTimeOut("Arrival Report Sent With EMPTY BarCode (All Spaces)");
    }
  }

  protected String getBarCodeData()
  {
    return(mpBarCodeText.getText());
  }

  protected String getMCKeyText()
  {
    return(mpMCKeyText.getText().trim());
  }

  protected String getControlInfo()
  {
    return(mpControlText.getText());
  }

  protected int getDimensionInfo()
  {    // Baseline cares about heights only since that's what most systems use.
    return(Integer.parseInt((String)mpHeightComboBox.getSelectedItem()));
  }

  protected int getLoadInfo()
  {
    int vnLoadInfo = 0;
    if (mpChkLoadPresent.isSelected())
    {
      vnLoadInfo = 1;
    }
    return(vnLoadInfo);
  }

  /**
   * Actually publish the control event to the emulator
   * 
   * @param sEvent
   * @param iEvent
   * @param sCKN
   */
  protected void publishControlEvent(String sEvent, int iEvent, String sCKN)
  {
    getSystemGateway().publishControlEvent(sEvent, iEvent, sCKN);
  }
}