/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright © 2004 Daifuku America Corporation  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED,
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND
  CIVIL LIABILITY.
 ****************************************************************************/

package com.daifukuamerica.wrxj.swingui.order;

import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swingui.item.ItemNumberInput;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class SingleLineItemOrderFrame extends ItemOrderFrame
{
  protected OrderLineData mpOLData = Factory.create(OrderLineData.class);

  protected SKDCTextField    mpOrderDesc;
  protected SKDCTextField    mpLoad;
  protected ItemNumberInput  mpItem;
  protected SKDCTextField    mpItemDesc;
  protected SKDCTextField    mpLot;
  protected SKDCIntegerField mpAllocQty;
  protected SKDCIntegerField mpQty;
  protected SKDCIntegerField mpPickQty;

  private boolean mzAutoClearOnAdd = true;

  /**
   * Constructor
   */
  public SingleLineItemOrderFrame()
  {
    super("Single Item Order", "Order Information");
    mpOHData = Factory.create(OrderHeaderData.class);
    buildScreen();
    setAddMode();
  }

  /**
   * Add mode
   */
  @Override
  protected void setAddMode()
  {
    setTitle("Add Item Order");
    mzAdding = true;
    useAddButtons();

    correctFieldEnabledValues();
    clearButtonPressed();
    setPermissions();
  }

  /**
   * Modify Mode
   */
  @Override
  protected void setModifyMode(OrderHeaderData ipOHData)
  {
    setTitle("Modify Item Order");
    mzAdding = false;
    mpOHData = ipOHData.clone();
    mpOrderID.setText(mpOHData.getOrderID());

    useModifyButtons();

    setInputVisible(mpAllocQty, true);

    getOLData();

    correctFieldEnabledValues();
    clearButtonPressed();
    setPermissions();
  }

  /**
   * Display Mode
   */
  @Override
  protected void setDisplayMode(OrderHeaderData ipOHData)
  {
    setTitle("Display Item Order");
    mzAdding = false;
    mzDisplayOnly = true;
    mpOHData = ipOHData.clone();
    mpOrderID.setText(mpOHData.getOrderID());

    setInputVisible(mpLoad, true);
    setInputVisible(mpAllocQty, true);
    setInputVisible(mpPickQty, true);

    mpBtnSubmit.setVisible(false);
    mpBtnClear.setVisible(false);

    getOLData();

    correctFieldEnabledValues();
    clearButtonPressed();
    setPermissions();
  }

  /**
   * Build the input panel
   */
  @Override
  protected void buildScreen()
  {
    mpOrderDesc   = new SKDCTextField(OrderHeaderData.DESCRIPTION_NAME);
    mpLoad        = new SKDCTextField(OrderLineData.LOADID_NAME);
    mpItem        = new ItemNumberInput(mpInvServer, true, false);
    mpItemDesc    = new SKDCTextField(ItemMasterData.DESCRIPTION_NAME);
    mpLot         = new SKDCTextField(OrderLineData.ORDERLOT_NAME);
    mpAllocQty    = new SKDCIntegerField(OrderLineData.ALLOCATEDQUANTITY_NAME);
    mpQty         = new SKDCIntegerField(OrderLineData.ORDERQUANTITY_NAME);
    mpPickQty     = new SKDCIntegerField(OrderLineData.PICKQUANTITY_NAME);

    mpItemDesc.setEnabled(false);
    mpItem.linkDescription(mpItemDesc);

    addInput("Order ID:",          mpOrderID);
    addInput("Order Description:", mpOrderDesc);
    addInput("Station:",           mpDestStation);
    addInput("Item:",              mpItem);
    addInput("Item Description:",  mpItemDesc);
    addInput("Lot:",               mpLot);
    addInput("Load:",              mpLoad);
    addInput("Allocated Qty:",     mpAllocQty);
    addInput("Ordered Qty:",       mpQty);
    addInput("Picked Qty:",        mpPickQty);

    setInputVisible(mpLoad, false);
    setInputVisible(mpAllocQty, false);
    setInputVisible(mpPickQty, false);
  }

  /**
   * Get the Order Line Data for modify/display
   */
  protected void getOLData()
  {
    addMovesButton();
    mpOrderID.setText(mpOHData.getOrderID());
    try
    {
      List<Map> vpOLList= mpOrderServer.getOrderLineData(mpOHData.getOrderID());
      if (vpOLList.size() == 0)
      {
        mpOLData.clear();
        displayError("Order " + mpOHData.getOrderID()
            + " has no lines... has it completed?");
        return;
      }
      mpOLData.dataToSKDCData(vpOLList.get(0));
    }
    catch (DBException dbe)
    {
      logAndDisplayException("Error reading order line.", dbe);
    }
  }

  /**
   * Reset to defaults
   */
  @Override
  protected void clearButtonPressed()
  {
    if (mzAdding)
    {
      mpOHData.clear();
    }
    mpOrderDesc.setText(mpOHData.getDescription());
    if (!mzAdding)
      mpDestStation.setSelectedStation(mpOHData.getDestinationStation());
    mpItem.setSelectedItem(mpOLData.getItem());
    mpLot.setText(mpOLData.getOrderLot());
    mpLoad.setText(mpOLData.getLoadID());
    mpAllocQty.setValue((int)mpOLData.getAllocatedQuantity());
    mpPickQty.setValue((int)mpOLData.getPickQuantity());
    mpQty.setValue((int)mpOLData.getOrderQuantity());
    super.clearButtonPressed();
  }

  /**
   * Create the order
   */
  @Override
  protected void okButtonPressed()
  {
    if (mzAdding)
      addSingleLineOrder();
    else
      updateSingleLineOrder();
  }

  /**
   * Create the order
   */
  protected void addSingleLineOrder()
  {
    String vsOrderID = mpOrderID.getText().trim();
    String vsStation = mpDestStation.getSelectedStation();
    String vsItem    = mpItem.getSelectedItem().toString();
    String vsLot     = mpLot.getText().trim();
    int    vnQty     = mpQty.getValue();

    /*
     * Validation
     */
    if (vsOrderID.trim().length() == 0)
    {
      displayError("Order ID can not be blank.", "Validation Error");
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
     * Add the order
     */
    mpOHData = Factory.create(OrderHeaderData.class);
    mpOHData.clear();
    mpOHData.setOrderID(vsOrderID);
    mpOHData.setOrderStatus(DBConstants.READY);
    mpOHData.setOrderType(DBConstants.ITEM_ORDER);
    mpOHData.setDestinationStation(vsStation);
    if (mpOrderDesc.getText().length() == 0)
      mpOHData.setDescription("Order Item: " + vsItem + " (" + vnQty + ")");
    else
      mpOHData.setDescription(mpOrderDesc.getText());

    OrderLineData vpOLData = Factory.create(OrderLineData.class);
    vpOLData.clear();
    vpOLData.setOrderID(vsOrderID);
    vpOLData.setLineID("1");
    vpOLData.setItem(vsItem);
    vpOLData.setOrderLot(vsLot);
    vpOLData.setOrderQuantity(vnQty);

    buildItemOrder(mpOHData, vpOLData);
  }

  /**
   * Create the order
   */
  protected void updateSingleLineOrder()
  {
    /*
     * Currently, the only thing they can change is quantity...
     */
    int vnNewQty = mpQty.getValue();
    if (vnNewQty >= mpOLData.getAllocatedQuantity())
    {
      OrderLineData vpOLData = Factory.create(OrderLineData.class);

      vpOLData.setOrderID(mpOLData.getOrderID());
      vpOLData.setItem(mpOLData.getItem());
      vpOLData.setOrderLot(mpOLData.getOrderLot());
      vpOLData.setLineID(mpOLData.getLineID());

      vpOLData.setOrderQuantity(vnNewQty);

      try
      {
        mpOrderServer.modifyOrderLine(vpOLData, false);
        changed(null, mpOHData);
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
      displayError("Ordered quantity can not be less than allocated quantity.");
    }
  }

  /**
   * Show moves
   */
  @Override
  protected void movesButtonPressed()
  {
    List<KeyObject> searchData = new LinkedList<KeyObject>();
    String searchItem = "";
    String searchLoad = "";

    searchData.add(new KeyObject(MoveData.ORDERID_NAME, mpOrderID.getText()));

    if (mpOLData.getItem().trim().length() > 0)
    {
      searchItem = mpOLData.getItem().trim();
      searchData.add(new KeyObject(MoveData.ITEM_NAME, searchItem));
    }
    if (mpOLData.getLoadID().trim().length() > 0)
    {
      searchLoad = mpOLData.getLoadID().trim();
      searchData.add(new KeyObject(MoveData.LOADID_NAME, searchLoad));
    }

    showMoves(searchData);
  }

  /**
   * Enable/disable fields as appropriate
   */
  @Override
  protected void correctFieldEnabledValues()
  {
    mpOrderID.setEnabled(mzAdding);
    mpOrderDesc.setEnabled(!mzDisplayOnly);
    mpDestStation.setEnabled(mzAdding);
    mpItem.setEnabled(mzAdding);
    mpLot.setEnabled(mzAdding);
    mpLoad.setEnabled(false);
    mpAllocQty.setEnabled(false);
    mpPickQty.setEnabled(false);
    mpQty.setEnabled(!mzDisplayOnly);
  }

  /**
   * Set permissions for buttons
   */
  @Override
  protected void setPermissions()
  {
    if (mzAdding && !ePerms.iAddAllowed)
    {
      displayError("Role permissions do not allow Add.");
      close();
    }
    if (!mzAdding && !mzDisplayOnly && !ePerms.iModifyAllowed)
    {
      displayError("Role permissions do not allow Modify.");
      close();
    }
  }

  /**
   * Set to false to not clear after an add (default is true)
   * @param izAutoClearOnAdd
   */
  public void setAutoClearOnAdd(boolean izAutoClearOnAdd)
  {
    mzAutoClearOnAdd = izAutoClearOnAdd;
  }

  /**
   * New order
   */
  @Override
  protected void addComplete()
  {
    changed(null, mpOHData);
    if (mzAutoClearOnAdd)
      clearButtonPressed();
    else
      super.clearButtonPressed();  // Just the Order ID
  }
}
