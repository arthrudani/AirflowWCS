package com.daifukuamerica.wrxj.swingui.purchaseorder;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPoReceivingServer;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCDoubleField;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swingui.item.ItemNumberInput;

/**
 * Description:<BR>
 *    Sets up a frame to add one order line to an existing order.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 20-Jun-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  SKDC Corporation
 */
@SuppressWarnings("serial")
public class UpdatePOLineFrame extends DacInputFrame
{
  protected StandardPoReceivingServer mpPOServer;
  
  protected PurchaseOrderLineData mpCurrentPOLdata = Factory.create(PurchaseOrderLineData.class);
  protected PurchaseOrderLineData mpNewPOLdata     = Factory.create(PurchaseOrderLineData.class);

  protected SKDCTextField   txtOrderID     = new SKDCTextField(PurchaseOrderLineData.ORDERID_NAME);
  protected SKDCTextField   txtLineID      = new SKDCTextField(PurchaseOrderLineData.LINEID_NAME);
  protected ItemNumberInput txtItem        = new ItemNumberInput(Factory.create(StandardInventoryServer.class), true, false);
  protected SKDCTextField   txtItemDesc    = new SKDCTextField(ItemMasterData.DESCRIPTION_NAME);
  protected SKDCTextField   txtLot         = new SKDCTextField(PurchaseOrderLineData.LOT_NAME);
  protected SKDCDoubleField txtExpectedQty = new SKDCDoubleField(PurchaseOrderLineData.EXPECTEDQUANTITY_NAME);

  // Modify Only
  protected SKDCDoubleField txtReceivedQty = new SKDCDoubleField(PurchaseOrderLineData.RECEIVEDQUANTITY_NAME);

  // Which mode
  protected boolean mzAdding = true;
  protected boolean mzAddToDB = false;
  
  /**
   * Constructor for ADD
   * @param ipOrdServer
   * @param isOrderID
   */
  public UpdatePOLineFrame(StandardPoReceivingServer ipOrdServer,
      String isOrderID, int inLineID, boolean izAddToDB)
  {
    super("Add Expected Receipt Line", "Expected Receipt Line Information");
    mpCurrentPOLdata.setOrderID(isOrderID);
    mpNewPOLdata.setOrderID(isOrderID);
    mpPOServer = ipOrdServer;
    mzAddToDB = izAddToDB;

    buildScreen();

    setLineID(inLineID);
    txtItem.requestFocus();
  }

  /**
   * Constructor for MODIFY
   * @param ipOrdServer
   * @param ipLineToModify
   */
  public UpdatePOLineFrame(StandardPoReceivingServer ipOrdServer, 
      PurchaseOrderLineData ipLineToModify)
  {
    super("Add Order Line", "Order Line Information");
    mpPOServer = ipOrdServer;
    mzAddToDB = true;

    buildScreen();
    setCurrentData(ipLineToModify);
  }

  /**
   * Set the line ID
   * @param inLineID
   */
  protected void setLineID(int inLineID)
  {
    txtLineID.setText("" + inLineID);
    mpCurrentPOLdata.setLineID("" + inLineID);
  }
  
  /**
   * Build the screen
   */
  protected void buildScreen()
  {
    txtOrderID.setEnabled(false);
    txtLineID.setEnabled(false);
    txtItemDesc.setEnabled(false);
    
    txtItem.linkDescription(txtItemDesc);
    
    addInput("Expected Receipt ID:", txtOrderID);
    addInput("Line ID:", txtLineID);
    addInput("Item:", txtItem);
    addInput("Item Description:", txtItemDesc);
    addInput("Lot:", txtLot);
    addInput("Expected Quantity:", txtExpectedQty);
    addInput("Received Quantity:", txtReceivedQty);

    setInputVisible(txtReceivedQty, false);
    
    useAddButtons();
    clearButtonPressed();
  }

  /**
   *   Load the modify screen with the current selected row of data from the
   *   table.  It is assumed that this frame has already been built when this
   *   method is called.
   */
  private void setCurrentData(PurchaseOrderLineData ipCurrentOLdata)
  {
    mpCurrentPOLdata = (PurchaseOrderLineData)ipCurrentOLdata.clone();
    mpNewPOLdata = (PurchaseOrderLineData)ipCurrentOLdata.clone();
    
    txtOrderID.setEnabled(false);      // These are the unchangeable fields.
    txtLineID.setEnabled(false);
    txtItem.setEnabled(false);
    txtLot.setEnabled(false);
    txtReceivedQty.setEnabled(false);

    setInputVisible(txtReceivedQty, true);
    
    setTitle("Modify Order Line");
    useModifyButtons();
    mzAdding = false;
    clearButtonPressed();
  }
  
/*===========================================================================
                Methods for event handling go in this section.
  ===========================================================================*/
  /**
   *  Processes Order Line Add request.
   */
  @Override
  protected void okButtonPressed()
  {
    if (mzAdding)
      addOrderLine();
    else
      modifyOrderLine();
  }
  
  /**
   * Add an order line
   */
  protected void addOrderLine()
  {
    PurchaseOrderLineData oldata = Factory.create(PurchaseOrderLineData.class);
    oldata.clear();
    oldata.setOrderID(txtOrderID.getText());

    if (mzAddToDB)
    {
      if (!displayYesNoPrompt("Add this order line"))
        return;
    }
    
    /*
     * Validate the line ID
     */
    if (txtLineID.getText().trim().length() == 0)
    {
      displayInfoAutoTimeOut("Order Line ID must be entered", "Entry Error");
      txtLineID.requestFocus();
      return;
    }
    oldata.setLineID(txtLineID.getText());
    
    /*
     * Validate the item
     */
    if (txtItem.getText().trim().length() == 0)
    {
      displayInfoAutoTimeOut("Order Item must be entered", "Entry Error");
      txtItem.requestFocus();
      return;
    }
    else if (mpPOServer.itemExists(txtItem.getText()) == false)
    {
      // do item / synonym switch if needed
      String enteredItem = txtItem.getText();
      txtItem.setText(Factory.create(StandardInventoryServer.class).swapItemSynonymIfEntered(txtItem.getText()));
      
      if (txtItem.getText().compareTo(enteredItem) == 0) // did not change
      {
        displayInfoAutoTimeOut("Item " + txtItem.getText() + " does not exist",
                     "Entry Error");
        txtItem.requestFocus();
        return;
      }
    }
    
    if (txtExpectedQty.getValue() == 0)
    {
      displayInfoAutoTimeOut("Order Quantity must be greater than 0", "Entry Error");
      txtExpectedQty.requestFocus();
      return;
    }

    oldata.setItem(txtItem.getText());
    if (txtLot.getText().trim().length() != 0)
    {
      oldata.setLot(txtLot.getText());
    }

    oldata.setExpectedQuantity(txtExpectedQty.getValue());

    try
    {
      if (mzAddToDB)
      {
        mpPOServer.addPOLine(oldata);
                                         // Get fresh data for screen update.
        
        oldata.setKey(PurchaseOrderLineData.ORDERID_NAME, oldata.getOrderID());
        oldata.setKey(PurchaseOrderLineData.ITEM_NAME, oldata.getItem());
        oldata.setKey(PurchaseOrderLineData.LOT_NAME, oldata.getLot());
        oldata.setKey(PurchaseOrderLineData.LINEID_NAME, oldata.getLineID());

        PurchaseOrderLineData newdata = mpPOServer.getPoLineRecord(oldata);
        changed(null, newdata);
        displayInfoAutoTimeOut("Expected Receipt " + oldata.getOrderID() + " added successfully.");
        Thread.sleep(30);
        close();
      }
      else
      {
        changed(null, oldata);
        setLineID(Integer.parseInt(txtLineID.getText()) + 1);
        clearButtonPressed();
      }
    }
    catch(DBException e)
    {
      displayError(e.getMessage(), "Order Line Add Error");
    }
    catch(InterruptedException e)
    {  // ignore it!.
    }

    return;
  }

  /**
   * Modify the order line
   */
  protected void modifyOrderLine()
  {
    if (!displayYesNoPrompt("Modify this order line"))
      return;
   
    mpNewPOLdata.clear();
    
    // Validate Order quantity if it has changed.
    double newOrdQty = txtExpectedQty.getValue();
    double oldOrdQty = mpCurrentPOLdata.getExpectedQuantity();
    if (newOrdQty != oldOrdQty)
    {
      boolean quantityProblem = false;
      String errMessage = "";

      if (newOrdQty < txtReceivedQty.getValue())
      {
        errMessage = "Ordered Quantity can't\nbe less than Picked Quantity.";
        quantityProblem = true;
      }
      else
        if (newOrdQty == 0)
        {
          errMessage = "Ordered Quantity\ncan't be Zero.";
          quantityProblem = true;
        }

      if (quantityProblem)
      {
        displayInfoAutoTimeOut(errMessage, "Data Entry Error");
        txtExpectedQty.requestFocus();
        errMessage = null;
        return;
      }
      // Add Ordered Quantity
      mpNewPOLdata.setExpectedQuantity(txtExpectedQty.getValue());
    }

    try
    {
      mpNewPOLdata.setKey(PurchaseOrderLineData.ORDERID_NAME, txtOrderID.getText());
      mpNewPOLdata.setKey(PurchaseOrderLineData.ITEM_NAME, txtItem.getText());
      mpNewPOLdata.setKey(PurchaseOrderLineData.LOT_NAME, txtLot.getText());
      mpNewPOLdata.setKey(PurchaseOrderLineData.LINEID_NAME, txtLineID.getText());

      mpPOServer.modifyPOLine(mpNewPOLdata);

      changed(null, mpNewPOLdata);
      Thread.sleep(30);
      close();
    }
    catch (Exception e)
    {
      logAndDisplayException(e);
    }
  }

  /**
   * Clear Button handler.
   */
  @Override
  protected void clearButtonPressed()
  {
    txtOrderID.setText(mpCurrentPOLdata.getOrderID());
    txtItem.setText(mpCurrentPOLdata.getItem());
    txtLot.setText(mpCurrentPOLdata.getLot());
    txtLineID.setText(mpCurrentPOLdata.getLineID());
    txtExpectedQty.setValue(mpCurrentPOLdata.getExpectedQuantity());
    txtReceivedQty.setValue(mpCurrentPOLdata.getReceivedQuantity());

    if (mzAdding)
      txtItem.requestFocus();
    else
      txtExpectedQty.requestFocus();
  }

  /**
   *  Cancel Button handler.
   */
  @Override
  protected void closeButtonPressed()
  {
    close();
  }
}
