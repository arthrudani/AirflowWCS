package com.daifukuamerica.wrxj.swingui.utility;

import com.daifukuamerica.wrxj.dataserver.standard.StandardConfigurationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoginData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.swingui.station.StationComboBox;
import com.daifukuamerica.wrxj.util.SKDCConstants;

/**
 * <B>Description:</B> This is a screen for setting GUI options on a
 * computer-by-computer basis.  Currently, it only supports a default station,
 * but it could be expanded to support a default printer, default screen size,
 * or whatever.
 * 
 * <P>Copyright (c) 2008 by Daifuku America Corporation</P>
 * 
 * @author mandrus
 * @version 1.0
 */
@SuppressWarnings("serial")
public class GuiOptionFrame extends DacInputFrame
{
  private StandardConfigurationServer mpConfigServer;

  private SKDCTextField mpTxtMachine;
  private SKDCTextField mpTxtIPAddress;
  private StationComboBox mpCmbStation;
  
  /**
   * 
   */
  public GuiOptionFrame()
  {
    super("GUI Options", "Computer Information");
    mpConfigServer = Factory.create(StandardConfigurationServer.class);
    buildScreen();
  }
  
  /**
   * Build the screen
   */
  private void buildScreen()
  {
    mpTxtMachine = new SKDCTextField(LoginData.MACHINENAME_NAME);
    mpTxtIPAddress = new SKDCTextField(LoginData.IPADDRESS_NAME);
    mpCmbStation = new StationComboBox();
    
    addInput("Computer Name", mpTxtMachine);
    addInput("IP Address", mpTxtIPAddress);
    addInput("Default Station", mpCmbStation);
    
    try
    {
      mpCmbStation.fill(null, SKDCConstants.EMPTY_VALUE);
    }
    catch (DBException dbe)
    {
      logAndDisplayException("Error getting stations", dbe);
    }
    
    clearButtonPressed();
  }
  
  /**
   * @see com.daifukuamerica.wrxj.swing.DacInputFrame#clearButtonPressed()
   */
  @Override
  protected void clearButtonPressed()
  {
    mpTxtMachine.setText(SKDCUserData.getMachineName());
    mpTxtIPAddress.setText(SKDCUserData.getIPAddress());
    mpCmbStation.selectDefaultStation();
  }
  
  /**
   * @see com.daifukuamerica.wrxj.swing.DacInputFrame#okButtonPressed()
   */
  @Override
  protected void okButtonPressed()
  {
    try
    {
      mpConfigServer.setDefaultStation(mpTxtMachine.getText(),
          mpTxtIPAddress.getText(), mpCmbStation.getSelectedStation());
      displayInfoAutoTimeOut("Defaults updated");
    }
    catch (DBException dbe)
    {
      logAndDisplayException("Error setting the default station", dbe);
    }
  }
}
