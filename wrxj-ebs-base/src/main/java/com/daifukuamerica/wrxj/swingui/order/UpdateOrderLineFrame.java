package com.daifukuamerica.wrxj.swingui.order;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCDoubleField;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swingui.item.ItemNumberInput;
import javax.swing.JCheckBox;

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
public class UpdateOrderLineFrame extends DacInputFrame
{
  protected StandardOrderServer mpOrderServer;
  
  protected OrderLineData mpCurrentOLdata = Factory.create(OrderLineData.class);
  protected OrderLineData mpNewOLdata     = Factory.create(OrderLineData.class);

  protected SKDCTextField   txtOrderID     = new SKDCTextField(OrderLineData.ORDERID_NAME);
  protected SKDCTextField   txtLineID      = new SKDCTextField(OrderLineData.LINEID_NAME);
  protected SKDCTextField   txtDescription = new SKDCTextField(OrderLineData.DESCRIPTION_NAME);
  protected ItemNumberInput txtItem        = new ItemNumberInput(Factory.create(StandardInventoryServer.class), true, false);
  protected SKDCTextField   txtItemDesc    = new SKDCTextField(ItemMasterData.DESCRIPTION_NAME);
  protected SKDCTextField   txtOrderLot    = new SKDCTextField(OrderLineData.ORDERLOT_NAME);
  protected SKDCDoubleField txtOrderedQty  = new SKDCDoubleField(OrderLineData.ORDERQUANTITY_NAME);

  // Modify Only
  protected SKDCDoubleField txtPickedQty  = new SKDCDoubleField(OrderLineData.PICKQUANTITY_NAME);
  protected JCheckBox         cboxLineShy   = new JCheckBox();

  // Which mode
  protected boolean mzAdding = true;
  protected boolean mzAddToDB = false;
  
  
  /**
   * Constructor for ADD
   * @param ipOrdServer
   * @param isOrderID
   */
  public UpdateOrderLineFrame(StandardOrderServer ipOrdServer, String isOrderID,
      int inLineID, boolean izAddToDB)
  {
    super("Add Order Line", "Order Line Information");
    mpCurrentOLdata.setOrderID(isOrderID);
    mpNewOLdata.setOrderID(isOrderID);
    mpOrderServer = ipOrdServer;
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
  public UpdateOrderLineFrame(StandardOrderServer ipOrdServer, 
      OrderLineData ipLineToModify)
  {
    super("Add Order Line", "Order Line Information");
    mpOrderServer = ipOrdServer;
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
    mpCurrentOLdata.setLineID("" + inLineID);
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
    
    addInput("Order ID:", txtOrderID);
    addInput("Line ID:", txtLineID);
    addInput("Item:", txtItem);
    addInput("Item Description:", txtItemDesc);
    addInput("Order Lot:", txtOrderLot);
    addInput("Line Description:", txtDescription);
    addInput("Allocated/Picked Quantity:", txtPickedQty);
    addInput("Order Quantity:", txtOrderedQty);
    addInput("Line Short:", cboxLineShy);

    setInputVisible(txtPickedQty, false);
    setInputVisible(cboxLineShy, false);
    
    useAddButtons();
    clearButtonPressed();
  }

  /**
   *   Load the modify screen with the current selected row of data from the
   *   table.  It is assumed that this frame has already been built when this
   *   method is called.
   */
  public void setCurrentData(OrderLineData ipCurrentOLdata)
  {
    mpCurrentOLdata = (OrderLineData)ipCurrentOLdata.clone();
    mpNewOLdata = (OrderLineData)ipCurrentOLdata.clone();
    
    txtOrderID.setEnabled(false);      // These are the unchangeable fields.
    txtLineID.setEnabled(false);
    txtItem.setEnabled(false);
    txtOrderLot.setEnabled(false);
    txtPickedQty.setEnabled(false);
    cboxLineShy.setEnabled(false);

    setInputVisible(txtPickedQty, true);
    setInputVisible(cboxLineShy, true);
    
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
    OrderLineData oldata = Factory.create(OrderLineData.class);
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
    else if (mpOrderServer.itemExists(txtItem.getText()) == false)
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
    
    if (txtOrderedQty.getValue() == 0)
    {
      displayInfoAutoTimeOut("Order Quantity must be greater than 0", "Entry Error");
      txtOrderedQty.requestFocus();
      return;
    }
    oldata.setItem(txtItem.getText());
    if (txtOrderLot.getText().trim().length() != 0)
    {
      oldata.setOrderLot(txtOrderLot.getText());
    }

    if (txtDescription.getText().trim().length() != 0)
    {
      oldata.setDescription(txtDescription.getText());
    }
    oldata.setOrderQuantity(txtOrderedQty.getValue());

    try
    {
      if (mzAddToDB)
      {
        String mesg = mpOrderServer.addOrderLine(oldata);
                                         // Get fresh data for screen update.
        OrderLineData newdata = mpOrderServer.getOrderLineRecord(oldata);
        changed(null, newdata);
        displayInfoAutoTimeOut(mesg, "Add Confirmation");
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
   
    mpNewOLdata.clear();
    
    // Set the Line description.
    mpNewOLdata.setDescription(txtDescription.getText());

    // Validate Order quantity if it has changed.
    double newOrdQty = txtOrderedQty.getValue();
    double oldOrdQty = mpCurrentOLdata.getOrderQuantity();
    if (newOrdQty != oldOrdQty)
    {
      boolean quantityProblem = false;
      String errMessage = "";

      if (newOrdQty < txtPickedQty.getValue())
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
        txtOrderedQty.requestFocus();
        errMessage = null;
        return;
      }
      // Add Ordered Quantity
      mpNewOLdata.setOrderQuantity(txtOrderedQty.getValue());
    }

    try
    {
      // Composite key that will be and'ed
      // together by default.
      mpNewOLdata.setOrderID(txtOrderID.getText());
      mpNewOLdata.setItem(txtItem.getText());
      mpNewOLdata.setOrderLot(txtOrderLot.getText());
      mpNewOLdata.setLineID(txtLineID.getText());

      String mesg = mpOrderServer.modifyOrderLine(mpNewOLdata, true);

      changed(null, mpNewOLdata);
      displayInfoAutoTimeOut(mesg, "Line Modify Confirmation");
      Thread.sleep(30);
      close();
    }
    catch (Exception e)
    {
      displayError(e.getMessage(), "Line Modify Error");
    }

    return;
  }

  /**
   * Clear Button handler.
   */
  @Override
  protected void clearButtonPressed()
  {
    txtOrderID.setText(mpCurrentOLdata.getOrderID());
    txtItem.setText(mpCurrentOLdata.getItem());
    txtOrderLot.setText(mpCurrentOLdata.getOrderLot());
    txtLineID.setText(mpCurrentOLdata.getLineID());
    txtDescription.setText(mpCurrentOLdata.getDescription());
    // double idPicked = currentOLdata.getPickQuantity();
    double idAllocd = mpCurrentOLdata.getAllocatedQuantity();
    txtPickedQty.setValue(idAllocd);
    txtOrderedQty.setValue(mpCurrentOLdata.getOrderQuantity());
    cboxLineShy.setSelected((mpCurrentOLdata.getLineShy() == DBConstants.YES));

    if (mzAdding)
      txtItem.requestFocus();
    else
      txtDescription.requestFocus();
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
