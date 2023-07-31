package com.daifukuamerica.wrxj.swingui.recovery;

import com.daifukuamerica.wrxj.dataserver.standard.StandardSchedulerServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.Route;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swingui.station.StationComboBox;

/**
 * <B>Description:</B> Choose an alternate location for a stranded load.
 *
 * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
public class AlternateLocationFrame extends DacInputFrame
{
  LoadData mpLoadData;
  
  StationComboBox mpAltStation;
  
  StandardSchedulerServer mpSchedServer = Factory.create(StandardSchedulerServer.class);
  StandardStationServer mpStationServer = Factory.create(StandardStationServer.class);
  
  /**
   * Constructor
   * 
   * @param ipLoadData - the load to recover
   */
  public AlternateLocationFrame(LoadData ipLoadData)
  {
    super("Recovery - Choose Alternate Location", "Alternate Location");
    mpLoadData = ipLoadData.clone();
    buildScreen();
  }

  /**
   * Build the screen
   */
  protected void buildScreen()
  {
    SKDCTextField vpTxtLoad = new SKDCTextField(LoadData.LOADID_NAME);
    SKDCIntegerField vpTxtDimension = new SKDCIntegerField(LoadData.HEIGHT_NAME);
    SKDCTextField vpTxtDevice = new SKDCTextField(LoadData.DEVICEID_NAME);
    
    vpTxtLoad.setText(mpLoadData.getLoadID());
    vpTxtDimension.setValue(mpLoadData.getHeight());
    vpTxtDevice.setText(mpLoadData.getDeviceID());
    
    vpTxtLoad.setEnabled(false);
    vpTxtDimension.setEnabled(false);
    vpTxtDevice.setEnabled(false);
    
    mpAltStation = new StationComboBox();
    try
    {
      String[] vasStations = Factory.create(Route.class).getOutputStationChoicesForDevice(
          mpLoadData.getDeviceID());
      mpAltStation.setComboBoxData(vasStations);
    }
    catch (DBException dbe)
    {
      logAndDisplayException("Error reading stations", dbe);
    }
    
    addInput("Load", vpTxtLoad);
    addInput("Load Height", vpTxtDimension);
    addInput("Device", vpTxtDevice);
    addInput("Station", mpAltStation);
    
    mpBtnClear.setVisible(false);
  }
  
  /**
   * @see com.daifukuamerica.wrxj.swing.DacInputFrame#okButtonPressed()
   */
  @Override
  protected void okButtonPressed()
  {
    String vsStn = mpAltStation.getSelectedStation();
    StationData vpStnData = mpStationServer.getStation(vsStn);

    if (vpStnData == null)
    {
      displayError("Station " + vsStn + " does not exist.");
      return;
    }

    try
    {
      mpSchedServer.sendAlternateLocation(mpLoadData, vpStnData);
    }
    catch (DBException dbe)
    {
      logAndDisplayException(dbe);
      return;
    }

    close();
  }
}
