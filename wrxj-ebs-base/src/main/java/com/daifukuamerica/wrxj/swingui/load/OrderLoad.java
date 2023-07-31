package com.daifukuamerica.wrxj.swingui.load;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardMoveServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardRouteServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swingui.station.StationComboBox;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.event.InternalFrameEvent;

/**
 * A screen class for ordering loads.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class OrderLoad extends DacInputFrame
{
  SKDCLabel mpLoadLabel = new SKDCLabel("Load ID:");
  protected SKDCComboBox mpLoadComboBox = new SKDCComboBox();
  protected StationComboBox mpStationComboBox;
  protected SKDCIntegerField mpPriority = new SKDCIntegerField(5,1);

  protected StandardStationServer mpStnServer = Factory.create(StandardStationServer.class);
  protected StandardInventoryServer invtServ = Factory.create(StandardInventoryServer.class);
  protected StandardLoadServer mpLoadServer = Factory.create(StandardLoadServer.class);
  protected StandardRouteServer mpRouteServer = Factory.create(StandardRouteServer.class);
  protected StandardOrderServer orderServ = Factory.create(StandardOrderServer.class);
  protected OrderLineData oldata = Factory.create(OrderLineData.class);
  protected boolean closeAfterRequest = false;
  protected boolean refreshList = true;
  protected String[] masLoads = null;
  protected boolean mzRequestSuccessful = true;

  protected boolean orderAllLoadsInList = false;

  /**
   * Create order load screen class.
   */
  public OrderLoad()
  { 
    super("Retrieve Load", "Load Order Information");
    try
    {
      mpStationComboBox = Factory.create(StationComboBox.class);
      jbInit();
      pack();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * @see com.daifukuamerica.wrxj.swing.DacInputFrame#internalFrameOpened(javax.swing.event.InternalFrameEvent)
   */
  @Override
  public void internalFrameOpened(InternalFrameEvent e)
  {
    super.internalFrameOpened(e);

    fillStationList();
    
    pack();
  }
  
  /**
   * Method to create the station list.
   */
  protected void fillStationList()
  {
    try
    {
      mpStationComboBox.fillWithOutputs(SKDCConstants.NO_PREPENDER);
      
      // Limit the station choices a little
      LoadData vpLD = mpLoadServer.getLoad(mpLoadComboBox.getText());
      if (vpLD != null)
      {
        List<String> vpGoodStations = new ArrayList<String>();
        
        for (int i = 0; i < mpStationComboBox.getItemCount(); i++)
        {
          String vsComboString = mpStationComboBox.getItemAt(i).toString();
          String[] vsComboArray  = vsComboString.split(" ");
          String vsStation = vsComboArray[0];
          StationData vpSD = mpStnServer.getStation(vsStation);
          if (vpSD != null && mpRouteServer.getFromToRoute(vpLD.getWarehouse(),
              vpLD.getAddress(), vpSD.getWarehouse(), vpSD.getStationName()) != null)
          {
            vpGoodStations.add(mpStationComboBox.getItemAt(i).toString());
          }
        }
        if (vpGoodStations.size() > 0)
        {
          mpStationComboBox.setComboBoxData(vpGoodStations);
        }
      }
    }
    catch (DBException dbe)
    {
      displayError(dbe.getMessage(), "Unable to get Stations");
    }
  }

  /**
   * Method to set the load ID to be ordered.
   * 
   * @param s Load to be ordered.
   */
  public void setLoadID(String s)
  {
    mpLoadComboBox.setEnabled(true);
    requestedLoadFill();
    mpLoadComboBox.setSelectedItem(s);
    closeAfterRequest = true;
  }

  /**
   * Method to set the list of load IDs to be ordered.
   * 
   * @param loads List of loads to be ordered.
   */
  public void setListOfLoads(String[] loads)
  {
    masLoads = loads;
    mpLoadComboBox.setComboBoxData(masLoads);
    mpLoadComboBox.setEnabled(false);
    mpLoadComboBox.setVisible(false);
    mpLoadLabel.setVisible(false);
    closeAfterRequest = true;
    orderAllLoadsInList = true;
  }

  /**
   * Method to initialize screen components. This adds the components to the
   * screen and adds listeners as needed.
   * 
   * @exception Exception
   */
  protected  void jbInit() throws Exception
  {
    mpLoadComboBox.setEditable(true);

    addInput(mpLoadLabel, mpLoadComboBox);
    addInput("Destination:", mpStationComboBox);
    addInput("Priority:", mpPriority);

  }

  /**
   * @see javax.swing.JInternalFrame#show()
   */
  @Override
  public void show()
  {
    super.show();
    if (masLoads == null)
    {
      requestedLoadFill();
    }
  }

  /**
   *  Method to clean up as needed at closing.
   */
  @Override
  public void cleanUpOnClose()
  {
    invtServ.cleanUp();
    invtServ = null;
    orderServ.cleanUp();
    orderServ = null;
    mpStnServer.cleanUp();
    mpStnServer = null;
    mpRouteServer.cleanUp();
    mpRouteServer = null;
  }

  /**
   *  Method to populate the load combo box.
   */
  protected void requestedLoadFill()
  {
    try
    {
      if (masLoads == null || refreshList == true)
      {
      	refreshList = false;
        masLoads = mpLoadServer.getLoadIDList(SKDCConstants.EMPTY_VALUE);
      }
      mpLoadComboBox.setComboBoxData(masLoads);
    }
    catch (DBException e)
    {
      displayError("Unable to get Loads");
    }
  }

  /**
   *  Action method to handle OK button. Verifies that entered data is valid,
   *  then adds an order for the load.
   */
  @Override
  protected void okButtonPressed()
  {
    mzRequestSuccessful = true;
    
    if (!orderAllLoadsInList || masLoads == null || masLoads.length == 0)
    {
      if (mpLoadComboBox.getText().trim().length() == 0)
      {
        displayInfoAutoTimeOut("Load ID is required");
        mpLoadComboBox.requestFocus();
        return;
      }

      if (mpStationComboBox.getText().length() == 0)
      {
        displayInfoAutoTimeOut("Destination is required");
        return;
      }
      String selectedLoad = mpLoadComboBox.getText();
//      loadComboBox.getEditor().setItem("");
      requestLoad(selectedLoad);
    }
    else
    {
      int count = masLoads.length;
      for (int idx = 0; idx < count; idx++)
      {
        requestLoad(masLoads[idx].toString());
      }
    }
    if (closeAfterRequest && mzRequestSuccessful)
    {
      close();
    }

  }

  /**
   *  Action method to handle OK button. Verifies that entered data is valid,
   *  then adds an order for the load.
   *
   *  @param loadToRetrieve
   */
  @SuppressWarnings("rawtypes")
  protected void requestLoad(String loadToRetrieve)
  {
    String vsSelectedStation = null;
    try
    {
      // make sure load exists
      LoadData vpLoadData = mpLoadServer.getLoad1(loadToRetrieve.trim());
      if (vpLoadData == null)
      {
        displayInfoAutoTimeOut("Load ID \"" + loadToRetrieve + "\" not found");
        mzRequestSuccessful = false;
        return;
      }
      //
      // Get stationId from Description
      //
      vsSelectedStation = mpStationComboBox.getSelectedStation();
      StationData vpStationData = mpStnServer.getStation(vsSelectedStation);
      if (vpStationData == null)
      {
        displayInfoAutoTimeOut("Destination Station \"" + vsSelectedStation + "\" not found");
        mzRequestSuccessful = false;
        return;
      }
      oldata.clear();
      oldata.setKey(OrderLineData.LOADID_NAME, loadToRetrieve);
      if (orderServ.OrderLineExists(oldata))
      {
        displayInfoAutoTimeOut("Load ID \"" + loadToRetrieve + "\" already ordered out");
        mzRequestSuccessful = false;
        return;
      }
      // make sure there is a route
      if (mpRouteServer.getFromToRoute(vpLoadData.getWarehouse(), vpLoadData.getAddress(),
          vpStationData.getWarehouse(), vpStationData.getStationName()) == null)
      {
        String vsErrstr = "Load has no physical route from " + vpLoadData.getWarehouse() + 
                           "-" + vpLoadData.getAddress() + " to " + vsSelectedStation;
        displayInfoAutoTimeOut(vsErrstr);
        mzRequestSuccessful = false;
        return;
      }
      // verify device is not Inoperable
      StandardDeviceServer deviceServ = Factory.create(StandardDeviceServer.class);
      if (deviceServ.getOperationalStatus(vpStationData.getStationName()) == DBConstants.INOP)
      {
        displayInfoAutoTimeOut("Device for Destination Station \"" + vpStationData.getStationName() 
                    + "\" is Inoperable");
        mzRequestSuccessful = false;
        return;
      }
      // Make sure location is available.
      StandardLocationServer locnServ = Factory.create(StandardLocationServer.class);
      int lcstat = locnServ.getLocationStatusValue(vpLoadData.getWarehouse(),
          vpLoadData.getAddress(), vpLoadData.getShelfPosition());
      if (lcstat == DBConstants.LCUNAVAIL)
      {
        boolean prmpt = displayYesNoPrompt("Load Location \"" + vpLoadData.getWarehouse() + "-" +
                                           vpLoadData.getAddress() + "\" is UNAVAILABLE!\n" +
                                           "Make location AVAILABLE");
        if (prmpt)
          locnServ.setLocationStatus(vpLoadData.getWarehouse(),
              vpLoadData.getAddress(), vpLoadData.getShelfPosition(),
              DBConstants.LCAVAIL, true);
        else
        {
          mzRequestSuccessful = false;
          return;
        }
      }
      
      // Make sure that the load is not already allocated to something else
      StandardMoveServer vpMoveServer = Factory.create(StandardMoveServer.class);
      List<Map> vpMoves = vpMoveServer.getMoveDataList(loadToRetrieve);
      if (vpMoves.size() > 0)
      {
        boolean prmpt = displayYesNoPrompt("Load ID \"" + loadToRetrieve + 
            "\" has picks, and will not retrieve for this \norder until all picks are completed.  Order anyway");
        if (!prmpt)
        {
          mzRequestSuccessful = false;
          return;
        }
      }
      
      // Make sure the load is not moving.
      if (vpLoadData.getLoadMoveStatus() != DBConstants.NOMOVE)
      {
        displayError("Load is Active, cannot retrieve.");
        return;
      }
      
    }
    catch (DBException e2)
    {
      displayError("Unable to get data: " + e2.getMessage());
      return;
    }
    try
    {
      OrderHeaderData oh = orderServ.buildLoadOrder(loadToRetrieve, mpPriority.getValue(), vsSelectedStation);
      changed(null, oh);
//      orderServ.allocateOrder(loadOrderID);
      displayInfoAutoTimeOut("Order is added successfully.");
      if (!closeAfterRequest)
      {
        mpLoadComboBox.setSelectedItem(null);
        mpLoadComboBox.getEditor().setItem("");
        mpStationComboBox.setSelectedIndex(0);
        refreshList = true;
        requestedLoadFill();
      }
    }
    catch (DBException e2)
    {
      displayError("Unable to get Load ID \"" + loadToRetrieve + "\":" + e2.getMessage());
    }
  }

  /**
   *  Action method to handle Clear button.
   */
  @Override
  protected void clearButtonPressed()
  {
    mpLoadComboBox.setSelectedItem(null);
    mpStationComboBox.setSelectedIndex(0);
  }

  /**
   *  Action method to handle Close button.
   */
  @Override
  protected void closeButtonPressed()
  {
    close();
  }
}
