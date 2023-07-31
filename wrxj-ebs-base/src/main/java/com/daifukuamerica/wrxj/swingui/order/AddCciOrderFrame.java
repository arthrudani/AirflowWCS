package com.daifukuamerica.wrxj.swingui.order;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardMaintenanceOrderServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCRadioButton;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swingui.item.ItemNumberInput;
import com.daifukuamerica.wrxj.swingui.location.AddressPanel;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.List;

/**
 * Description:<p>
 *    Frame to add Cycle Count orders to the system.</p>
 * 
 * @author       A.D.    Version 1.0.  Original version.
 * @version      1.0
 * @since: 04-Jun-02
 */
@SuppressWarnings("serial")
public class AddCciOrderFrame extends DacInputFrame
{
  protected StandardOrderServer            mpOrdServer;
  protected StandardMaintenanceOrderServer mpMaintOrderServer;

  protected static String ITEM_CC = "Item";
  protected static String LOCATION_CC = "Location";
  
  protected SKDCComboBox mpCCTypeCombo;
  protected SKDCComboBox mpLocWarehouseCombo;
  protected AddressPanel mpBegAddressPanel = new AddressPanel();
  protected AddressPanel mpEndAddressPanel = new AddressPanel();
  protected SKDCRadioButton mpRackFormat = new SKDCRadioButton();
  protected ItemNumberInput mpItemInput;
  protected SKDCTextField mpItemDesc = new SKDCTextField(OrderLineData.DESCRIPTION_NAME);
  protected SKDCTextField mpItemLotTxt = new SKDCTextField(OrderLineData.ORDERLOT_NAME);
  protected SKDCIntegerField mpPriorityTxt = new SKDCIntegerField(8,
      DBInfo.getFieldLength(OrderHeaderData.PRIORITY_NAME));
  protected SKDCTranComboBox mpStatusCombo;

  
  public AddCciOrderFrame()
  {
	this("Add Cycle Count Order");
  }
  
  public AddCciOrderFrame(String isTitle)
  {
    super(isTitle, "Cycle Count Order Information");
    init();
  }
  
  /**
   * Initialize screen components and build the screen
   */
  protected void init()
  {
    mpOrdServer = Factory.create(StandardOrderServer.class);
    mpMaintOrderServer = Factory.create(StandardMaintenanceOrderServer.class);
    StandardLocationServer vpLocServ = Factory.create(
                                                  StandardLocationServer.class);
    try
    {
      String[] vpLstWarehouse = vpLocServ.getRegularWarehouseChoices(
                                                     SKDCConstants.NO_PREPENDER);
      mpLocWarehouseCombo = new SKDCComboBox(vpLstWarehouse);
    }
    catch (DBException e)
    {
      displayWarning(e.getMessage(), "Location List Warning");
    }
    
    mpCCTypeCombo = new SKDCComboBox(new String[] {ITEM_CC, LOCATION_CC} );
    mpCCTypeCombo.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent arg0)
      {
        boolean vzItmMode = mpCCTypeCombo.getSelectedItem().equals(ITEM_CC);
        boolean vzLocMode = mpCCTypeCombo.getSelectedItem().equals(LOCATION_CC);
         
        mpItemInput.setEnabled(vzItmMode);
        mpItemLotTxt.setEnabled(vzItmMode);

        mpBegAddressPanel.setEnabled(vzLocMode);
        mpEndAddressPanel.setEnabled(vzLocMode);
        mpRackFormat.setEnabled(vzLocMode);
      }
    });
    
    int[] cycOrderStatus = {DBConstants.READY, DBConstants.HOLD};
    try
    {
      mpStatusCombo = new SKDCTranComboBox(OrderHeaderData.ORDERSTATUS_NAME, cycOrderStatus, false);
      mpStatusCombo.setSelectedElement(DBConstants.READY);
    }
    catch(java.lang.NoSuchFieldException e)
    {
      displayWarning("Translation Error for Order Status");
    }
    mpRackFormat.setSelected(true);
    mpRackFormat.setToolTipText("Switch location format");
    mpRackFormat.addItemListener(new java.awt.event.ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        if (e.getStateChange() == ItemEvent.SELECTED ||
            e.getStateChange() == ItemEvent.DESELECTED)
        {
          setRackFormat(e.getStateChange() == ItemEvent.SELECTED);
        }
      }
    });
    mpBegAddressPanel.add(mpRackFormat);
    mpItemInput = new ItemNumberInput(
                   Factory.create(StandardInventoryServer.class), true, false);

    buildScreen();
  }
  
  /**
   * Build the screen
   */
  protected void buildScreen()
  {
    mpItemDesc.setEnabled(false);
    mpItemInput.linkDescription(mpItemDesc);
    
    addInput("Cycle Count Type:", mpCCTypeCombo);
    addInput("Warehouse:", mpLocWarehouseCombo);
    addInput("Beginning Address:", mpBegAddressPanel);
    addInput("Ending Address:", mpEndAddressPanel);
    addInput("Item:", mpItemInput);
    addInput("Item Description:", mpItemDesc);
    addInput("Lot:", mpItemLotTxt);
    addInput("Priority:", mpPriorityTxt);
    addInput("Status:", mpStatusCombo);

    mpCCTypeCombo.setSelectedItem(LOCATION_CC);
  }
  
  /**
   *  Method to switch between rack and non-rack location format.
   *
   *  @param izRack True if setting to rack format.
   */
  protected void setRackFormat(boolean izRack)
  {
    mpRackFormat.setSelected(izRack);
    mpBegAddressPanel.setRackMode(izRack);
    mpEndAddressPanel.setRackMode(izRack);
  }

  /**
   * Clear Button handler.
   */
  @Override
  protected void clearButtonPressed()
  {
    mpLocWarehouseCombo.setSelectedIndex(0);
    mpBegAddressPanel.reset();
    mpEndAddressPanel.reset();
    mpItemInput.setSelectedIndex(0);
    mpItemLotTxt.setText("");
    mpStatusCombo.setSelectedIndex(0);
    mpPriorityTxt.setValue(8);
  }

  /**
   *  Action method to handle submit button. Verifies that entered data is valid,
   *  then adds an order for the cycle count.
   */
  @Override
  protected void okButtonPressed()
  {
    OrderHeaderData vpOHData = Factory.create(OrderHeaderData.class);
    OrderLineData   vpOLData = Factory.create(OrderLineData.class);
    int vnPriority = 0;
    int vnOrderStatus = 0;
    String vsRTC = "";

    /*
     * Validation
     */
    try
    {
      vnOrderStatus = mpStatusCombo.getIntegerValue();
    }
    catch (NoSuchFieldException e1)
    {
      displayWarning("Translation Error for Order Status");
    }

    vnPriority = mpPriorityTxt.getValue();
    if (vnPriority < 1 || vnPriority > 9)
    {
      displayError("Priority must be 1-9");
      mpPriorityTxt.requestFocus();
      return;
    }

    if (vpOHData.getOrderID().trim().length() == 0)
    {
       vpOHData.setOrderID(mpMaintOrderServer.createRandomOrderID());
    }
    
    /*
     * Actually add the order
     */
    try
    {
      /*
       * Location Cycle Count
       */
      if (mpCCTypeCombo.getSelectedItem().equals(LOCATION_CC))
      {
        String vsBeginAddress = mpBegAddressPanel.getAddress();
        String vsEndAddress   = mpEndAddressPanel.getAddress();

        vpOHData.setDescription("Cycle Count Order By Location Range");
        vpOLData.setBeginWarehouse(mpLocWarehouseCombo.getText());
        vpOLData.setBeginAddress(vsBeginAddress);
        vpOLData.setEndingWarehouse(mpLocWarehouseCombo.getText());
        vpOLData.setEndingAddress(vsEndAddress);
      }
      /*
       * Item Cycle Count
       */
      else if (mpCCTypeCombo.getSelectedItem().equals(ITEM_CC))
      {
        if (mpItemInput.getText().trim().length() == 0)
        {
          displayError("Item ID is a required field");
          mpItemInput.requestFocus();
          return;
        }
   
        vpOHData.setDescription("Cycle Count Order By Item/lot");
        vpOLData.setItem(mpItemInput.getText());
        vpOLData.setOrderLot(mpItemLotTxt.getText());
        vpOLData.setWarehouse(mpLocWarehouseCombo.getText());

        mpItemInput.setSelectedIndex(0);
        mpItemLotTxt.setText("");
        mpItemInput.requestFocus();
      }
      
      vpOHData.setPriority(vnPriority);
      vpOHData.setOrderStatus(vnOrderStatus);
      vpOHData.setOrderType(DBConstants.CYCLECOUNT);
      vpOHData.setReleaseToCode(vsRTC);
      
      mpMaintOrderServer.buildOrder(vpOHData, new OrderLineData[] {vpOLData});
    }
    catch(DBException e)
    {
      displayError("\n" + e.getMessage(), "Add Order Error");
      return;
    }
    
    try
    {
      // refresh list of orders.
      List alist = mpOrdServer.getOrderHeaderData(OrderHeaderData.ORDERID_NAME,
          vpOHData.getOrderID());

      String confirmMessage;
      String orderID = vpOHData.getOrderID();
      if (!alist.isEmpty())
      {
        changed(FRAME_CHANGE, vpOHData);
        confirmMessage = "Cycle-Count Order " + orderID + " added.";
      }
      else
      {
        confirmMessage = "Failed to add Cycle-Count Order: " + orderID;
      }

      displayInfoAutoTimeOut(confirmMessage, "Add Cycle-Count Confirmation");
      try { Thread.sleep(30); } catch(Exception e) { }
      close();
    }
    catch(DBException exc)
    {
      displayError("Error adding Cycle-Count Order: " + exc.getMessage());
    }
  }
}
