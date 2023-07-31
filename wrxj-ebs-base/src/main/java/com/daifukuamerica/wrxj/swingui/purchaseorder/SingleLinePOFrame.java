/**
 * 
 */
package com.daifukuamerica.wrxj.swingui.purchaseorder;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPoReceivingServer;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swingui.item.ItemNumberInput;
import com.daifukuamerica.wrxj.swingui.station.StationComboBox;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <B>Description:</B> Add/Modify/View a single-line expected receipt<BR>
 *
 * @author       mandrus<BR>
 * @version      1.0
 * 
 * <BR>Copyright (c) 2008 by Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class SingleLinePOFrame extends AbstractPOFrame
{
  PurchaseOrderLineData mpPOLData = Factory.create(PurchaseOrderLineData.class);
  
  protected StationComboBox  mpStation;
  protected ItemNumberInput  mpItem;
  protected SKDCTextField    mpItemDesc;
  protected SKDCTextField    mpLot;
  protected SKDCIntegerField mpReceivedQty;
  protected SKDCIntegerField mpExpectedQty;

  protected StandardInventoryServer   mpInvServer;
  private StandardPoReceivingServer mpPOServer;
  
  private boolean mzAutoClearOnAdd = true;

  public SingleLinePOFrame()
  {
    super("Single Item Expected Receipt", "Expected Receipt Information");

    mpPOHData = Factory.create(PurchaseOrderHeaderData.class);
    
    mpInvServer = Factory.create(StandardInventoryServer.class);
    mpPOServer  = Factory.create(StandardPoReceivingServer.class);

    buildScreen();
    setAddMode();
  }

  /**
   * @see com.daifukuamerica.wrxj.swingui.purchaseorder.AbstractPOFrame#buildScreen()
   */
  @Override
  protected void buildScreen()
  {
    mpStation = new StationComboBox();
    mpItem = new ItemNumberInput(mpInvServer, true, false);
    mpItemDesc = new SKDCTextField(ItemMasterData.DESCRIPTION_NAME);
    mpLot = new SKDCTextField(PurchaseOrderLineData.LOT_NAME);
    mpExpectedQty = new SKDCIntegerField(1, 8);
    mpReceivedQty = new SKDCIntegerField(0, 8);
    
    mpOrderID.setText(createOrderIDByDateTime());
    try
    {
      mpStation.fill(new int[] { DBConstants.USHAPE_IN, DBConstants.PDSTAND,
          DBConstants.INPUT, DBConstants.REVERSIBLE },
          SKDCConstants.NO_PREPENDER);
    }
    catch (DBException dbe)
    {
      logAndDisplayException(dbe);
    }
    mpItemDesc.setEnabled(false);
    mpItem.linkDescription(mpItemDesc);
    mpReceivedQty.setEnabled(false);
    
    addInput("Expected Receipt ID:", mpOrderID);
    addInput("Station:", mpStation);
    addInput("Item:", mpItem);
    addInput("Item Description:", mpItemDesc);
    addInput("Lot:", mpLot);
    addInput("Expected Quantity:", mpExpectedQty);
    addInput("Received Quantity:", mpReceivedQty);
    
    setInputVisible(mpReceivedQty, false);
    
    useAddButtons();
  }

  /**
   * @see com.daifukuamerica.wrxj.swingui.purchaseorder.AbstractPOFrame#correctFieldEnabledValues()
   */
  @Override
  protected void correctFieldEnabledValues()
  {
    mpOrderID.setEnabled(mzAdding);
    mpStation.setEnabled(!mzDisplayOnly);
    mpItem.setEnabled(mzAdding);
    mpLot.setEnabled(mzAdding);
    mpExpectedQty.setEnabled(!mzDisplayOnly);
  }

  /**
   * @see com.daifukuamerica.wrxj.swingui.purchaseorder.AbstractPOFrame#setAddMode()
   */
  @Override
  protected void setAddMode()
  {
    setTitle("Add Expected Receipt");
    mzAdding = true;
  }

  /**
   * @see com.daifukuamerica.wrxj.swingui.purchaseorder.AbstractPOFrame#setDisplayMode(com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData)
   */
  @Override
  protected void setDisplayMode(PurchaseOrderHeaderData ipPOHData)
  {
    setTitle("Display Expected Receipt");
    mzAdding = false;
    mzDisplayOnly = true;
    mpPOHData = ipPOHData.clone();
    mpOrderID.setText(mpPOHData.getOrderID());

    setInputVisible(mpReceivedQty, true);
    
    mpBtnSubmit.setVisible(false);
    mpBtnClear.setVisible(false);
    
    getPOLData();

    correctFieldEnabledValues();
    clearButtonPressed();
  }

  /**
   * @see com.daifukuamerica.wrxj.swingui.purchaseorder.AbstractPOFrame#setModifyMode(com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData)
   */
  @Override
  protected void setModifyMode(PurchaseOrderHeaderData ipPOHData)
  {
    setTitle("Modify Expected Receipt");
    mzAdding = false;
    mpPOHData = ipPOHData.clone();
    mpOrderID.setText(mpPOHData.getOrderID());
    
    useModifyButtons();
    
    setInputVisible(mpReceivedQty, true);
    
    getPOLData();

    correctFieldEnabledValues();
    clearButtonPressed();
  }

  /**
   * Clean up and close
   */
  @Override
  protected void closeButtonPressed()
  {
    mpInvServer.cleanUp();
    mpPOServer.cleanUp();
    close();
  }
  
  /**
   * Reset to defaults
   */
  @Override
  protected void clearButtonPressed()
  {
    if (mzAdding)
    {
      mpPOHData.clear();
    }
    mpStation.setSelectedStation(mpPOLData.getRouteID());
    mpItem.setSelectedItem(mpPOLData.getItem());
    mpLot.setText(mpPOLData.getLot());
    mpExpectedQty.setValue((int)mpPOLData.getExpectedQuantity());
    if (mzAdding)
    {
      mpOrderID.setText(createOrderIDByDateTime());
      mpOrderID.requestFocus();
    }
  }

  /**
   * Create the order
   */
  @Override
  protected void okButtonPressed()
  {
    if (mzAdding)
      addSingleLinePO();
    else
      updateSingleLinePO();
  } 

  /**
   * New order
   */
  protected void addComplete()
  {
    if (mzAutoClearOnAdd)
    {
      clearButtonPressed();
    }
    else
    {
      mpOrderID.setText(createOrderIDByDateTime());
      mpOrderID.requestFocus();
    }
  }

  /**
   * Create the purchase order or expected receipt or whatever we're calling it
   * nowadays.
   */
  protected void addSingleLinePO()
  {
    String vsOrderID = mpOrderID.getText();
    String vsStation = mpStation.getSelectedStation();
    String vsItem    = mpItem.getSelectedItem().toString();
    String vsLot     = mpLot.getText();
    int    vnQty     = mpExpectedQty.getValue();
    
    /*
     * Validation
     */
    if (vsOrderID.trim().length() == 0)
    {
      displayError("Expected Receipt ID can not be blank.", "Validation Error");
      return;
    }

    if (vsStation.trim().length() == 0)
    {
      displayError("Station can not be blank.", "Validation Error");
      return;
    }
    
    if (vsItem.trim().length() == 0)
    {
      displayError("Item can not be blank.", "Validation Error");
      return;
    }
    
    if (vnQty < 1)
    {
      displayError("Quantity can not be less than 1.", "Validation Error");
      return;
    }
    
    /*
     * Add the purchase order
     */
    PurchaseOrderHeaderData vpPOHData = Factory.create(PurchaseOrderHeaderData.class);
    vpPOHData.setOrderID(vsOrderID);
    vpPOHData.setOrderStatus(DBConstants.EREXPECTED);
    vpPOHData.setExpectedDate(new Date());
    vpPOHData.setLastActivityTime(new Date());

    PurchaseOrderLineData vpPOLData = Factory.create(PurchaseOrderLineData.class);
    vpPOLData.clear();
    vpPOLData.setOrderID(vsOrderID);
    vpPOLData.setRouteID(vsStation);
    vpPOLData.setLineID("1");
    vpPOLData.setItem(vsItem);
    vpPOLData.setLot(vsLot);
    vpPOLData.setExpectedQuantity(vnQty);

    List<PurchaseOrderLineData> vpPOLList = new ArrayList();
    vpPOLList.add(vpPOLData);
    try
    {
      mpPOServer.buildPO(vpPOHData, vpPOLList);
      displayInfoAutoTimeOut("Expected Receipt " + vsOrderID + " added successfully.");
      changed(null, vpPOHData);
      addComplete();
//      close();
    }
    catch (DBException dbe)
    {
      logAndDisplayException(dbe);
    }
  }

  /**
   * Create the order
   */
  protected void updateSingleLinePO()
  {
    /*
     * Currently, the only thing they can change is quantity...
     */
    int vnNewQty = mpExpectedQty.getValue();
    if (vnNewQty >= mpPOLData.getReceivedQuantity())
    {
      PurchaseOrderLineData vpOLData = Factory.create(PurchaseOrderLineData.class);

      vpOLData.setKey(PurchaseOrderLineData.ORDERID_NAME, mpPOLData.getOrderID());
      vpOLData.setKey(PurchaseOrderLineData.ITEM_NAME, mpPOLData.getItem());
      vpOLData.setKey(PurchaseOrderLineData.LOT_NAME, mpPOLData.getLot());
      vpOLData.setKey(PurchaseOrderLineData.LINEID_NAME, mpPOLData.getLineID());
      
      vpOLData.setRouteID(mpStation.getSelectedStation());
      vpOLData.setExpectedQuantity(vnNewQty);
      
      try
      {
        mpPOServer.modifyPOLine(vpOLData);
        changed(null, mpPOHData);
        close();
      }
      catch (DBException dbe)
      {
        displayError(dbe.getMessage());
        logger.logException(dbe);
      }
    }
    else
    {
      displayError("Expected quantity can not be less than received quantity.");
    }
  }

  /**
   * Get the PO Line Data for modify/display
   */
  protected void getPOLData()
  {
    mpOrderID.setText(mpPOHData.getOrderID());
    try
    {
      List<Map> vpOLList= mpPOServer.getPurchaseOrderLines(mpPOHData.getOrderID());
      mpPOLData.dataToSKDCData(vpOLList.get(0));
    }
    catch (DBException dbe)
    {
      logAndDisplayException(dbe);
    }
  }
}
