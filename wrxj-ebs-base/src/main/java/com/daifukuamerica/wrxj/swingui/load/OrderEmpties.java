package com.daifukuamerica.wrxj.swingui.load;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.AmountFullTransMapper;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swingui.item.ItemNumberInput;
import com.daifukuamerica.wrxj.swingui.station.StationComboBox;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

/**
 * A screen class for ordering out empty containers.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class OrderEmpties extends DacInputFrame
{
  protected Map<String, AmountFullTransMapper> mpPartialQtyMap;
  protected StandardInventoryServer mpInvServ = Factory.create(StandardInventoryServer.class);
  protected StandardStationServer mpStationServ = Factory.create(StandardStationServer.class);
  protected StandardOrderServer mpOrderServ = Factory.create(StandardOrderServer.class);

  protected SKDCComboBox mpContType;
  protected StationComboBox mpStationComboBox;
  protected SKDCComboBox mpPartialEmptyCombo;
  protected SKDCIntegerField mpRequestedQuantity;
  protected SKDCIntegerField mpHeight;
  protected ItemNumberInput mpItem;
  protected SKDCTextField mpItemDesc;
  protected SKDCTextField mpLot;
  
  protected Integer[] manHeights = new Integer[] {0,1,2,3,4};
  
  /**
   *  Create order empties screen class.
   */
  public OrderEmpties()
  {
    super("Empty Container Order", "Empty Container Information");

    try
    {
      mpPartialQtyMap = LoadData.getAmountFullDecimalMap();
      buildScreen();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   *  Method to initialize screen components. This adds the components to the
   *  screen and adds listeners as needed.
   *
   *  @exception Exception
   */
  protected void buildScreen() throws Exception
  {
    getSystemHeights();
    
    mpContType = new SKDCComboBox();
    mpContType.setEditable(false);
    mpRequestedQuantity = new SKDCIntegerField(1,1);
    mpRequestedQuantity.setText(mpRequestedQuantity.getText());
    
    // Build Combo Box with blank first entry
    mpPartialEmptyCombo = new SKDCComboBox();
    mpPartialEmptyCombo.setComboBoxData(mpPartialQtyMap.keySet().toArray(), true);
    
    mpStationComboBox = Factory.create(StationComboBox.class);
    mpStationComboBox.addActionListener(new StationComboActionListener());
    mpHeight = new SKDCIntegerField(manHeights[0],1);
    mpItem = new ItemNumberInput(mpInvServ, true, false);
    mpItemDesc = new SKDCTextField(DBInfo.getFieldLength(OrderLineData.ITEM_NAME));
    mpLot = new SKDCTextField(DBInfo.getFieldLength(OrderLineData.ORDERLOT_NAME));

    mpItemDesc.setEnabled(false);
    mpItem.linkDescription(mpItemDesc);
    
    JPanel vpPanelRequestQty = new JPanel();
    vpPanelRequestQty.add(mpRequestedQuantity);
    vpPanelRequestQty.add(new SKDCLabel(" and "));
    vpPanelRequestQty.add(mpPartialEmptyCombo);
    
    addInput("Container:",   mpContType);
    addInput("Amount Empty:",vpPanelRequestQty);
    addInput("Destination:", mpStationComboBox);
    addInput("Height:",      mpHeight);
    addInput("Item:",        mpItem);
    addInput("Description:", mpItemDesc);
    addInput("Lot:",         mpLot);

    containerFill();
    try
    {
      mpStationComboBox.fillWithOutputs(SKDCConstants.NO_PREPENDER);
    }
    catch (DBException dbe)
    {
      displayError(dbe.getMessage(), "Unable to get Stations");
    }
    resetData();
  }
  
  /**
   * Get the valid heights in this system
   */
  protected void getSystemHeights()
  {
    StandardLocationServer vpLocServer = Factory.create(StandardLocationServer.class);
    try
    {
      manHeights = vpLocServer.getLocationHeights();
    }
    catch (DBException dbe)
    {
      logAndDisplayException(dbe);
    }
  }
  
  /**
   * Action listener to update DB on selection change
   */
  public class StationComboActionListener implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent arg0)
    {
      String vsStationID = mpStationComboBox.getSelectedStation();
      StationData vpStationData = mpStationServ.getStation(vsStationID);
      
      String vsContainer = vpStationData.getContainerType();
      mpContType.setSelectedItem(vsContainer);
    }
  }

  /**
   *  Method to populate the container type combo box.
   */
  protected void containerFill()
  {
    try
    {
      List<String> vpContainerList = mpInvServ.getContainerTypeList();
      mpContType.setComboBoxData(vpContainerList);
      mpContType.setEnabled(vpContainerList.size() > 1);
    }
    catch (DBException dbe)
    {
      logAndDisplayException(dbe);
    }
  }

  /**
   *  Method to clean up as needed at closing.
   *
   */
  @Override
  public void cleanUpOnClose()
  {
    mpInvServ.cleanUp();
    mpInvServ = null;
    mpOrderServ.cleanUp();
    mpOrderServ = null;
    mpStationServ.cleanUp();
    mpStationServ = null;
  }

  /**
   *  Action method to handle OK button. Verifies that entered data is valid,
   *  then adds an order for the requested number and type of empty containers.
   */
  @Override
  protected void okButtonPressed()
  {
    if (mpContType.getText().length() == 0)
    {
      displayInfoAutoTimeOut("Container is required");
      return;
    }

    if (mpStationComboBox.getText().length() == 0)
    {
      displayInfoAutoTimeOut("Destination is required");
      return;
    }

    if (mpItem.getText().length() > 0)
    {
      try
      {
        if (!mpInvServ.itemMasterExists(mpItem.getText().trim()))
        {
          displayInfoAutoTimeOut("Item does not exist");
          return;
        }
      }
      catch (DBException e2)
      {
        displayInfoAutoTimeOut("Unable to verify Item Master");
        return;
      }
    }

    /*
     * Validate the height
     */
    boolean vzGoodHeight = false;
    for (int i = 0; i < manHeights.length; i++)
    {
      if (mpHeight.getValue() == manHeights[i])
      {
        vzGoodHeight = true;
        break;
      }
    }
    if (!vzGoodHeight)
    {
      String vsHeightError = "Height must be in (";
      for (int i = 0; i < manHeights.length; i++)
      {
        vsHeightError += manHeights[i] + (i < manHeights.length-1 ? "," : "");
      }
      vsHeightError += ")";
      displayInfoAutoTimeOut(vsHeightError);
      return;
    }

    try
    {
      // check if we already have empties in enroute to this station
      OrderHeaderData oh = Factory.create(OrderHeaderData.class);
      oh.clear();
      
      // fill in what is needed for an empty container order

      // make sure order doesn't already exist
      // if it does try anorther orderID

      String contOrderID = mpOrderServ.createRandomOrderID("MT", "MT");
      oh.setOrderID(contOrderID);
      oh.setOrderStatus(DBConstants.ALLOCATENOW);
      oh.setOrderType(DBConstants.CONTAINER);
      oh.setPriority(7);
      //
      // Get stationId from Description
      //
      String vsStation = mpStationComboBox.getSelectedStation();
      oh.setDestinationStation(vsStation);
      oh.setDescription("Empty Container Order");

      // fill in the line item with what the allocator needs

      OrderLineData ol = Factory.create(OrderLineData.class);
      ol.clear();
      ol.setOrderID(contOrderID);
      ol.setContainerType(mpContType.getText());
      /*
       * The requested Quantity is in units of "amount empty" not "amount full"
       */
      double vdAmtEmptyDecim = mpRequestedQuantity.getValue();
      
      String vsPartialSelection = (String)mpPartialEmptyCombo.getSelectedItem();
      if (vsPartialSelection.trim().length() > 0)
      {
        AmountFullTransMapper vpTranMapper = mpPartialQtyMap.get(vsPartialSelection);
        vdAmtEmptyDecim += vpTranMapper.getPartialAmtFullDecimal();
      }
      ol.setOrderQuantity(vdAmtEmptyDecim);
      ol.setHeight(mpHeight.getValue());
      ol.setAllocatedQuantity(0.0);
      ol.setPickQuantity(0.0);
      ol.setLineShy(DBConstants.NO);
      ol.setItem(mpItem.getText().trim());
      ol.setOrderLot(mpLot.getText().trim());
      ol.setDescription("Empty Container Order");

      OrderLineData[] lineList = {ol};
      String mesg = mpOrderServ.buildOrder(oh, lineList);
      changed(null, oh);
      displayInfoAutoTimeOut(mesg, "Add Information");
      resetData();
    }
    catch (DBException e2)
    {
      displayInfoAutoTimeOut("Unable to get Load");
    }
  }

  /**
   *  Action method to handle Clear button.
   */
  @Override
  protected void clearButtonPressed()
  {
    resetData();
  }

  /**
   *  Action method to handle Close button.
   */
  @Override
  protected void closeButtonPressed()
  {
    close();
  }

  /**
   *  Method to reset screen fields to defaults.
   *
   */
  protected void resetData()
  {
    mpItem.setText("");
    mpLot.setText("");
    mpRequestedQuantity.setValue(1);
    mpHeight.setValue(manHeights[0]);
    mpPartialEmptyCombo.setSelectedIndex(0);
  }
}
