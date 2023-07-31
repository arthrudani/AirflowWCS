package com.daifukuamerica.wrxj.swingui.recovery;
/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

// Java imports
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.device.agc.AGCDeviceConstants;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.messageformat.loadevent.LoadEventDataFormat;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.event.InternalFrameEvent;

/**
 * A screen class that handles Send Arrival functionality for WRxJ. 
 *
 * @author avt
 * @version 0.1
 */
public class SendArrivalFrame extends DacInputFrame 
{
  private static final long serialVersionUID = 0L;
  
  // Station ID
 
  protected SKDCComboBox stationComboBox = new SKDCComboBox();
  protected SKDCTextField loadidField = new SKDCTextField(LoadData.LOADID_NAME);
  protected SKDCTextField bcrField  = new SKDCTextField(LoadData.BCRDATA_NAME);


  protected SKDCComboBox heightBox= new SKDCComboBox();

    // Station Server
  protected StandardStationServer mpStationServer = null;
  protected StandardLoadServer mpLoadServer= null;
  
  // Map containing station data for combo box
  // Allows us to display station description and have the name too.
  protected Map<String, String> stationsMap = null;  
  
  // Types of stations to be put in combo box
  protected int[] srcStations = {DBConstants.USHAPE_OUT, DBConstants.PDSTAND,
                        DBConstants.INPUT, DBConstants.USHAPE_IN,
                        DBConstants.OUTPUT, DBConstants.REVERSIBLE};
  protected LoadEventDataFormat mpLEDF = Factory.create(
      LoadEventDataFormat.class, "SendArrival");

  public SendArrivalFrame()
  {
    this("Send Arrival", "");
  }
  
  public SendArrivalFrame(String isFrameTitle, String isInputTitle)
  {
    super (isFrameTitle, isInputTitle);

    try
    {
      jbInit();
      pack();
    }
    catch(Exception exp)
    {
      exp.printStackTrace();
    }
  }
  /**
   *  Method to intialize screen components. This adds the components to the
   *  screen and adds listeners as needed.
   *
   */
  protected void jbInit()
  {

   
    mpStationServer = Factory.create(StandardStationServer.class);
    mpLoadServer = Factory.create(StandardLoadServer.class);
    stationFill();
    heightFill();

    // Send Arrival Button
    mpBtnSubmit.setText("Send Arrival");

    // Cancel Button
    mpBtnClear.setVisible(false);
    mpBtnClose.setText("Cancel");
 
    // Main Panel
 
    addInput("Station:", stationComboBox);
    addInput("LoadID:", loadidField);
    addInput("Bar Code Data:",bcrField);
    addInput("Height:",heightBox);
 
    // Button Panel
 
    loadidField.requestFocus();
  }

  @Override
  public void internalFrameActivated(InternalFrameEvent e)
  {

    super.internalFrameActivated(e);
    stationChange();
  }

  /**
   *  Method to populate the station combo box.
   */
  protected void stationFill()
  {
      // Get the work station list
    try
    {
      int[] inputStations =
      { DBConstants.USHAPE_IN, DBConstants.PDSTAND, DBConstants.REVERSIBLE, DBConstants.INPUT };
      stationsMap = mpStationServer.getStationsByStationType(inputStations);
      Object[] stationsArray = stationsMap.keySet().toArray();
      stationComboBox.setComboBoxData(stationsArray);
      stationComboBox.setSelectedIndex(0);
      stationComboBox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent arg0)
        {
          stationChange();
        }
      });
    }
    catch (DBException e)
    {
      displayError("Unable to get Stations");
    }
  }

  /**
   *  Method to populate the height combo box.
   */
  protected void heightFill()
  {
    Integer[] vapHeights = { 0, 1, 2, 3 };
    try
    {
      vapHeights = Factory.create(StandardLocationServer.class).getLocationHeights();
    }
    catch (DBException dbe)
    {
      logger.logException(dbe);
    }
    heightBox.setComboBoxData(vapHeights);
  }
    
  /**
   *  Method to handle a station change. When a station change occurs it
   *  rechecks the subscriptions and rechecks loads at station.
   */
  protected void stationChange()
  {
    //
    // Get stationName that goes with the Description
    //
    String vsStation = stationsMap.get(stationComboBox.getSelectedItem());
    StationData stationData = mpStationServer.getStation(vsStation);
    if (stationData == null)
    {
      return;
    }
    if (stationData.getArrivalRequired() != DBConstants.YES)
    {
      displayInfo("Station does not require an arrival");
      return;
    }
    loadidField.setText("");
    switch (stationData.getStationType())
    {
      case DBConstants.USHAPE_IN:
      case DBConstants.INPUT:
        loadidField.setText(AGCDeviceConstants.AGCDUMMYLOAD);
        break;
      case DBConstants.USHAPE_OUT:
      case DBConstants.OUTPUT:
        break;
      case DBConstants.PDSTAND:
          if (stationData.getCaptive() == DBConstants.CAPTIVE && 
              stationData.getStatus() == DBConstants.CAPTIVEINSERT)
          {
            loadidField.setText(AGCDeviceConstants.AGCDUMMYLOAD);
            break;
          }
      case DBConstants.REVERSIBLE:
        loadidField.setText(AGCDeviceConstants.AGCDUMMYLOAD);
        try
        {
 
          LoadData loadData = mpLoadServer.getOldestLoad(stationData.getWarehouse(),
                                stationData.getStationName());
          if (loadData != null)
          {
            bcrField.setText(loadData.getLoadID());
          }
        }
        catch (DBException e2)
        {
          displayError("Error accessing load table: " + e2.getMessage());
          return;
        }
    }
  }

  protected int getDimensionInfo()
  {    // Baseline cares about heights only since that's what most systems use.
    return(Integer.parseInt(heightBox.getText()));
  }
   
   /**
   *  Method to send the pick to arrival.
   * 
   */
  @Override
  protected void okButtonPressed()
  {
    try
    {
      if (loadidField.getText().length() == 0)
      {
        displayInfo("Load ID cannot be blank");
        return;
      }
      if (isallchr(loadidField.getText(), '0', DBInfo.getFieldLength(LoadData.LOADID_NAME)) ||
          isallchr(loadidField.getText(), ' ', DBInfo.getFieldLength(LoadData.LOADID_NAME)))
      {
        displayInfo("Load ID not valid");
        return;
      }
 
      //
      // Get stationName that goes with the Description
      //

      final StationData stationData;
   
      String vsStation = stationsMap.get(stationComboBox.getSelectedItem());
      stationData = mpStationServer.getStation(vsStation);
 
      if (stationData.getArrivalRequired() != DBConstants.YES)
      {
        displayInfo("Station does not require an arrival");
        return;
      }
      switch (stationData.getStationType())
      {
        case DBConstants.USHAPE_IN:
        case DBConstants.INPUT:
          if (!isallchr(loadidField.getText(), '9', DBInfo.getFieldLength(LoadData.LOADID_NAME)))
          {
            displayInfo("Load ID at this station must be all 9s");
            return;
          }
          break;
        case DBConstants.USHAPE_OUT:
        case DBConstants.OUTPUT:
          if (isallchr(loadidField.getText(), '9', DBInfo.getFieldLength(LoadData.LOADID_NAME)))
          {
            displayInfo("Load ID at this station cannot be all 9s");
            return;
          }
            // see if load exists
          if (!mpLoadServer.loadExists(loadidField.getText()))
          {
            displayError("Load " + loadidField.getText() + " does not exist");
            return;
          }
          break;
        case DBConstants.PDSTAND:
          if(isallchr(loadidField.getText(), '9', DBInfo.getFieldLength(LoadData.LOADID_NAME))&&
              bcrField.getText().trim().length() == 0)
            break;
          if (stationData.getCaptive() == DBConstants.CAPTIVE && 
              stationData.getStatus() == DBConstants.CAPTIVEINSERT) break;
        case DBConstants.REVERSIBLE:
        default:
          if (isallchr(loadidField.getText(), '9', DBInfo.getFieldLength(LoadData.LOADID_NAME)))
          {
            // Inbound arrivals
            // see if load specified in BCR exists and where it is
            LoadData loadData = mpLoadServer.getLoad1(bcrField.getText());
            if (loadData == null)
            {
              displayError("Load " + bcrField.getText() + " does not exist");
              return;
            }
            else
            {
              StandardLocationServer locnServ = Factory.create(StandardLocationServer.class);
              int lctype = locnServ.getLocationTypeValue(loadData.getWarehouse(),
                                                           loadData.getAddress());
              if (lctype != DBConstants.LCSTATION)
              {
                displayError("Load " + bcrField.getText() + " not at station");
                return;
              }
            }
          }
          else
          {
            // Outbound arrivals
            // see if load exists and where it is
            LoadData loadData = mpLoadServer.getLoad1(loadidField.getText());
            if (loadData == null)
            {
              displayError("Load " + loadidField.getText() + " does not exist");
              return;
            }
            StandardLocationServer locnServ = Factory.create(StandardLocationServer.class);
            int lctype = locnServ.getLocationTypeValue(loadData.getWarehouse(),
                                                         loadData.getAddress());
            if (lctype == DBConstants.LCSTATION)
            {
              displayError("Load " + loadidField.getText() + " already at station");
              return;
            }
          }
          break;
      }
      
      // find out who is scheduling this station
      String scheduler = mpStationServer.getStationsScheduler(stationData.getStationName());

      // send the scheduler event message
      String cmdstr = mpLEDF.processArrivalReport(loadidField.getText(),
          stationData.getStationName(), getDimensionInfo(), 0, bcrField.getText(), null);
      getSystemGateway().publishLoadEvent(cmdstr, 0, scheduler);
      logger.logOperation(LogConsts.OPR_USER, "LoadId \"" + loadidField.getText() + "\" Station: " + stationData.getStationName() + " Re-Sending Arrival");
      displayInfoAutoTimeOut("Arrival sent for LoadId \"" + loadidField.getText() + "\" at Station: " + stationData.getStationName());
      clearScreen();
    }
    catch (DBException e)
    {
      displayError(e.getMessage());
    }
    catch(Exception e)
    {
      displayError( e.getMessage());
    }

  }

  /**
   *  Method to detmine if a string is all the same character.
   * 
  *  @param str String to be checked.
  *  @param val Character to check for.
  *  @param len Length to check.
   */
  protected boolean isallchr(String str, char val, int len)
  {
    boolean rtnval = false;
    if (str.length() == len)
    {
      rtnval = true;
      for (int i = 0; i < len && rtnval; i++)
      {
        if (str.charAt(i) != val)
        {
          rtnval = false;
        }
      }
    }
    return (rtnval);
  }

  /**
   *  Method to clear the screen.
   * 
   */
  protected void clearScreen()
  {
    loadidField.setText("");
    bcrField.setText("");
    heightBox.setSelectedIndex(0);
//    stationComboBox.setSelectedIndex(0);
  }

}
