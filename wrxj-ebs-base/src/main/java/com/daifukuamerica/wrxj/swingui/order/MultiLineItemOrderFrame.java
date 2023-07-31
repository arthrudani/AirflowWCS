/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright © 2007 Daifuku America Corporation  All Rights Reserved.
 
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS 
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND 
  CIVIL LIABILITY.
 ****************************************************************************/

package com.daifukuamerica.wrxj.swingui.order;

import com.daifukuamerica.wrxj.dataserver.standard.StandardCarrierServer;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLine;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.ReleaseToCodeField;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCDateField;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swing.table.DacTableMouseListener;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class MultiLineItemOrderFrame extends ItemOrderFrame
{
  protected StandardCarrierServer mpCarServer;

  protected SKDCTextField mpDescription;
  protected SKDCTranComboBox mpOrderStatus;
  protected SKDCDateField mpSchedDate;
  protected SKDCIntegerField mpPriority;
  protected ReleaseToCodeField mpRTC;
  protected SKDCComboBox mpCarrier;
  protected int mnLineID = 0;
  
  protected OrderChangeListener mpOCL = new OrderChangeListener();
  
  protected List<OrderLineData> mpOLList; 
  
  /**
   * Constructor
   */
  public MultiLineItemOrderFrame()
  {
    super("Multi-Line Item Order", "Order Information");
    mpCarServer = Factory.create(StandardCarrierServer.class);
    showTableWithDefaultButtons("AddOrderLineFrame");
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
    mpOHData = Factory.create(OrderHeaderData.class);
    
    mpBtnModLine.setVisible(false);
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

    addChangeListeners();
    mpBtnSubmit.setEnabled(false);
    mpBtnModLine.setVisible(true);
    addMovesButton();
    
    changeTable("OrderLine");
    
    correctFieldEnabledValues();
    clearButtonPressed();
    refreshOrderLines();
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

    try
    {
      // Show all translations in display mode
      mpOrderStatus.setComboBoxData(OrderHeaderData.ORDERSTATUS_NAME, 
          DBTrans.getIntegerList(OrderHeaderData.ORDERSTATUS_NAME));
    }
    catch (NoSuchFieldException e)
    {
      e.printStackTrace();
    }
    
    mpBtnSubmit.setVisible(!mzDisplayOnly);
    mpBtnAddLine.setVisible(!mzDisplayOnly);
    mpBtnModLine.setVisible(!mzDisplayOnly);
    mpBtnDelLine.setVisible(!mzDisplayOnly);
    mpBtnClear.setVisible(!mzDisplayOnly);
    addMovesButton();
    
    changeTable("OrderLine");

    correctFieldEnabledValues();
    clearButtonPressed();
    refreshOrderLines();
    setPermissions();
  }

  /**
   * Build the screen
   */
  @Override
  protected void buildScreen()
  {
    mpOLList = new ArrayList<OrderLineData>();

    mpDescription = new SKDCTextField(OrderHeaderData.ORDERID_NAME);
    mpDescription.setMaxColumns(DBInfo.getFieldLength(OrderHeaderData.DESCRIPTION_NAME));
    try
    {
      mpOrderStatus  = new SKDCTranComboBox(OrderHeaderData.ORDERSTATUS_NAME,
                     new int[] {DBConstants.READY, DBConstants.HOLD}, false);
    }
    catch (NoSuchFieldException e)
    {
      e.printStackTrace();
    }
    mpSchedDate = new SKDCDateField(true);
    mpPriority = new SKDCIntegerField(5,1);
    mpRTC = new ReleaseToCodeField();
    mpCarrier = new SKDCComboBox();

    addInput("Order ID", mpOrderID);
    addInput("Description", mpDescription);
    addInput("Status", mpOrderStatus);
    addInput("Scheduled Date", mpSchedDate);
    addInput("Priority", mpPriority);
    if (ReleaseToCodeField.useReleaseToCode())
      addInput("Release-to Code", mpRTC);
    addInput("Carrier", mpCarrier);
    addInput("Destination", mpDestStation);

    fillCarriers();
    
    setInputColumns(2);
  } 
  
  /**
   * Fill the carrier combo box (hide it if it is empty)
   */
  protected void fillCarriers()
  {
    mpCarrier.addItemListener(new ItemListener()
      {
        public void itemStateChanged(ItemEvent e)
        {
          try
          {
            String vsCarrier = mpCarrier.getText().toString();
            if (vsCarrier.trim().length() > 0)
            {
              String vsStation = mpCarServer.getCarrierStation(vsCarrier);
              mpDestStation.setSelectedStation(vsStation);
            }
            else
            {
              mpDestStation.resetDefaultSelection();
            }
          }
          catch (DBException dbe)
          {
            displayError(dbe.getMessage());
            logger.logException(dbe);
          }
        }
      });
    
    try
    {
      String[] carrierList = mpCarServer.getCarrierChoices();
      int vnLength = carrierList.length;
      if (vnLength > 0)
        mpCarrier.setComboBoxData(carrierList, true);
      else
        setInputVisible(mpCarrier, false);
    }
    catch (DBException dbe)
    {
      logger.logException(dbe);
      displayError(dbe.getMessage());
      setInputVisible(mpCarrier, false);
    }
  }
  
  /**
   * Activate the Submit button on a modify if the Order Header changes
   */
  protected void addChangeListeners()
  {
    mpDescription.addKeyListener(mpOCL);
    mpOrderStatus.addActionListener(mpOCL);
    mpSchedDate.addActionListener(mpOCL);
    mpPriority.addKeyListener(mpOCL);
    mpRTC.addKeyListener(mpOCL);
    mpDestStation.addActionListener(mpOCL);
  }

  /**
   * Activate the Submit button on a modify if the Order Header changes
   */
  private class OrderChangeListener implements KeyListener, ActionListener
  {
    public void keyPressed(KeyEvent e)          {     checkForOHChange();    }
    public void keyReleased(KeyEvent e)         {     checkForOHChange();    }
    public void keyTyped(KeyEvent e)            {     checkForOHChange();    }
    public void actionPerformed(ActionEvent e)  {     checkForOHChange();    }
  }
  
  /**
   * Create the order
   */
  @Override
  protected void okButtonPressed()
  {
    if (mzAdding)
    {
      if (displayYesNoPrompt("Add order"))
        addMultiLineOrder();
    }
    else
    {
      if (displayYesNoPrompt("Update order"))
        updateMultiLineOrder();
    }
  } 
  
  /**
   * Create the order
   */
  protected void addMultiLineOrder()
  {
    String vsOrderID = mpOrderID.getText().trim();
    String vsStation = mpDestStation.getSelectedStation();
    
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
    
    if (mpOLList.size() < 1)
    {
      displayError("Order must have at least one line.");
      return;
    }
    
    /*
     * Re-sequence order lines and make sure the order ID is correct.
     */
    int i = 1;
    for (OrderLineData vpOLD : mpOLList)
    {
      vpOLD.setLineID("" + i++);
      vpOLD.setOrderID(vsOrderID);
    }
    
    /*
     * Add the order
     */
    mpOHData = Factory.create(OrderHeaderData.class);
    mpOHData.clear();
    mpOHData.setOrderID(vsOrderID);
    mpOHData.setOrderStatus(DBConstants.READY);
    mpOHData.setDescription(mpDescription.getText());
    try
    {
      mpOHData.setOrderType(DBConstants.ITEM_ORDER);
      mpOHData.setOrderStatus(mpOrderStatus.getIntegerValue());
    }
    catch (NoSuchFieldException e)
    {
      e.printStackTrace();
    }
    mpOHData.setScheduledDate(mpSchedDate.getDate());
    mpOHData.setPriority(mpPriority.getValue());
    mpOHData.setReleaseToCode(mpRTC.getText());
    mpOHData.setCarrierID(mpCarrier.getText());
    mpOHData.setDestinationStation(vsStation);
    
    buildItemOrder(mpOHData, mpOLList.toArray(new OrderLineData[0]));
  }
  
  /**
   * Update the order
   */
  protected void updateMultiLineOrder()
  {
    mpOHData.setKey(OrderHeaderData.ORDERID_NAME, mpOrderID.getText());
    mpOHData.setOrderID(mpOrderID.getText());
    mpOHData.setDescription(mpDescription.getText());
    try
    {
      mpOHData.setOrderType(DBConstants.ITEM_ORDER);
      mpOHData.setOrderStatus(mpOrderStatus.getIntegerValue());
    }
    catch (NoSuchFieldException e)
    {
      e.printStackTrace();
    }
    mpOHData.setScheduledDate(mpSchedDate.getDate());
    mpOHData.setPriority(mpPriority.getValue());
    mpOHData.setReleaseToCode(mpRTC.getText());
    mpOHData.setCarrierID(mpCarrier.getText());
    mpOHData.setDestinationStation(mpDestStation.getSelectedStation());
    mpOHData.setDestWarehouse("");
    mpOHData.setDestAddress("");
    
    try
    {
      mpOrderServer.modifyOrderHeader(mpOHData);
      changed(null, mpOHData);
      close();
    }
    catch (DBException dbe)
    {
      displayError(dbe.getMessage());
      logger.logException(dbe);
    }
  }
  
  /**
   * Add an order line
   */
  @Override
  protected void addLineButtonPressed()
  {
    if (mzAdding)
    {
      mnLineID++;
    }
    else
    {
      OrderLine vpOL = Factory.create(OrderLine.class);
      String sLineID;
      try
      {
        sLineID = vpOL.getNextLineID(mpOrderID.getText());
        mnLineID = Integer.parseInt(sLineID);
      }
      catch (DBException err)
      {
        displayWarning(err.getMessage(), "Cannot generate Line ID");
        mnLineID += 33;
      }
    } 
    String vsOrderID = mpOrderID.getText();
    if (vsOrderID.length() > 0)
    {
      UpdateOrderLineFrame addLineFrame = Factory.create(
          UpdateOrderLineFrame.class, mpOrderServer, vsOrderID, mnLineID, !mzAdding);
      addSKDCInternalFrameModal(addLineFrame, mpButtonPanel,
          new OrderLineAddFrameHandler());
    }
  }
  
  /**
   * Property Change event listener for Add Line frame.
   */
  public class OrderLineAddFrameHandler implements PropertyChangeListener
  {
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(FRAME_CHANGE))
      {
        OrderLineData oldata = (OrderLineData)pcevt.getNewValue();
        if (mzAdding)
        {
          mpOLList.add(oldata);
        }
        mpTable.appendRow(oldata);
      }
      else if (prop_name.equals(FRAME_CLOSING))
      {
        correctFieldEnabledValues();
      }
    }
  }
  
  /**
   * Add an order line
   */
  @Override
  protected void modifyLineButtonPressed()
  {
    int[] selection = mpTable.getSelectedRows();
    if (selection.length == 0)
    {
      displayInfoAutoTimeOut("No row selected to Modify", "Selection Error");
      return;
    }
    else if (selection.length > 1)
    {
      displayInfoAutoTimeOut("Only one row can be selected to Modify at a time", "Selection Error");
      return;
    }
    OrderLineData oldata = Factory.create(OrderLineData.class);
    oldata.dataToSKDCData(mpTable.getSelectedRowData());

    UpdateOrderLineFrame modifyLineFrame = Factory.create(
        UpdateOrderLineFrame.class, mpOrderServer, oldata);

    addSKDCInternalFrameModal(modifyLineFrame, mpButtonPanel,
        new OrderLineModifyFrameHandler());
  }
  
  /**
   *   Property Change event listener for Modify Line frame.
   */
  private class OrderLineModifyFrameHandler implements PropertyChangeListener
  {
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(FRAME_CHANGE))
      {
        refreshOrderLines();
      }
      else if (prop_name.equals(FRAME_CLOSING))
      {
        correctFieldEnabledValues();
      }
    }
  }

  /**
   * Delete Line
   */
  @Override
  protected void deleteLineButtonPressed()
  {
    int totalSelected = mpTable.getSelectedRowCount();
    if (totalSelected  == 0)
    {
      displayInfoAutoTimeOut("No row selected to Delete", "Selection Error");
      return;
    }

    boolean deleteLines = false;
    deleteLines = displayYesNoPrompt("Do you really want to Delete" + SKDCConstants.EOL_CHAR +
                                     "all selected Lines", "Delete Confirmation");
    if (deleteLines)
    {
      String vsOrderID,  vsItem;
      String vsOrderLot, vsLineID;
      
      int[] vanSelectedRows = mpTable.getSelectedRows();
      for(int vnIdx = 0; vnIdx < vanSelectedRows.length; vnIdx++)
      {
        Map vpSelectedData = mpTable.getRowData(vanSelectedRows[vnIdx]);
        vsOrderID = DBHelper.getStringField(vpSelectedData, OrderLineData.ORDERID_NAME);
        vsItem = DBHelper.getStringField(vpSelectedData, OrderLineData.ITEM_NAME);
        vsOrderLot = DBHelper.getStringField(vpSelectedData, OrderLineData.ORDERLOT_NAME);
        vsLineID = DBHelper.getStringField(vpSelectedData, OrderLineData.LINEID_NAME);
        try
        {
          mpOrderServer.deleteItemOrderLine(vsOrderID, vsItem, vsOrderLot, 
                                            vsLineID);
        }
        catch(DBException exc)
        {
          mpTable.deselectRow(vanSelectedRows[vnIdx]);
          displayInfo("Error deleting line for item \'" + vsItem + "\', Lot \'" +
                      vsOrderLot + "\'. " + exc.getMessage());
        }
      }

      mpTable.deleteSelectedRows();
      if (mpTable.getRowCount() == 0)
      {
        changed(null, null);
        super.closeButtonPressed();
      }
    }
  }
  
  /**
   * Keep our Order Line list in sync with our table 
   * 
   * @param oldata
   */
//  private void delListData(OrderLineData oldata)
//  {
//    int lineIdx = 0;
//    int listSize = mpOLList.size();
//    boolean foundIt = false;
//
//    for(lineIdx = 0; lineIdx < listSize; lineIdx++)
//    {
//      OrderLineData ol = mpOLList.get(lineIdx);
//      if (ol.equals(oldata))
//      {
//        foundIt = true;
//        break;
//      }
//    }
//
//    if (foundIt) mpOLList.remove(lineIdx);
//  }


  /**
   * Populate the order lines list
   */
  protected void refreshOrderLines()
  {
    try
    {
      List l = mpOrderServer.getOrderLineData(mpOrderID.getText());
      mpTable.refreshData(l);
    }
    catch (DBException e)
    {
      displayError(e.getMessage());
      logger.logException(e);
    }
  }
  
  /**
   * Reset to defaults
   */
  @Override
  protected void clearButtonPressed()
  {
    super.clearButtonPressed();
    mpDescription.setText(mpOHData.getDescription());
    try
    {
      mpOrderStatus.setSelectedElement(mpOHData.getOrderStatus());
    }
    catch (NoSuchFieldException e)
    {
      e.printStackTrace();
    }
    mpSchedDate.setDate(mpOHData.getScheduledDate());
    mpPriority.setValue(mpOHData.getPriority());
    mpRTC.setText(mpOHData.getReleaseToCode());
    mpCarrier.setSelectedItem(mpOHData.getCarrierID());
    mpDestStation.setSelectedStation(mpOHData.getDestinationStation());

    refreshOrderLines();
    mpOLList.clear();
    mnLineID = 0;
  }
  
  /**
   * 
   */
  @Override
  protected void closeButtonPressed()
  {
    if (mpOLList.size() > 0)
    {
      if (!displayYesNoPrompt("Close without adding"))
      {
        return;
      }
    }
    super.closeButtonPressed();
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
  
    int[] selection = mpTable.getSelectedRows();
    if (selection.length == 1)
    {
      OrderLineData vpOLData = Factory.create(OrderLineData.class);
      vpOLData.dataToSKDCData(mpTable.getSelectedRowData());
      if (vpOLData.getItem().trim().length() > 0)
      {
        searchItem = vpOLData.getItem().trim();
        searchData.add(new KeyObject(MoveData.ITEM_NAME, searchItem));
      }
      if (vpOLData.getLoadID().trim().length() > 0)
      {
        searchLoad = vpOLData.getLoadID().trim();
        searchData.add(new KeyObject(MoveData.LOADID_NAME, searchLoad));
      }
    }
  
    showMoves(searchData);
  }
  
  /**
   * Enable/disable fields as appropriate
   */
  @Override
  protected void correctFieldEnabledValues()
  {
    mpOrderID.setEnabled(mzAdding && !mzDisplayOnly);
    mpDescription.setEnabled(!mzDisplayOnly);
    mpOrderStatus.setEnabled(!mzDisplayOnly);
    mpSchedDate.setEnabled(!mzDisplayOnly);
    mpPriority.setEnabled(!mzDisplayOnly);
    mpRTC.setEnabled(!mzDisplayOnly);
    mpCarrier.setEnabled(!mzDisplayOnly);
    mpDestStation.setEnabled(!mzDisplayOnly);
    checkForOHChange();
  }

  /**
   * Check for order header changes and set the Submit buttons enabled
   * status accordingly.  
   */
  protected void checkForOHChange()
  {
    boolean vzChanged = mzAdding;
    
    try
    {
      if (!mpDescription.getText().equals(mpOHData.getDescription()))
        vzChanged = true;
      if (mpOrderStatus.getIntegerValue() != mpOHData.getOrderStatus())
        vzChanged = true;
      if (!mpSchedDate.getDate().equals(mpOHData.getScheduledDate()))
        vzChanged = true;
      if (mpPriority.getValue() != mpOHData.getPriority())
        vzChanged = true;
      if (!mpRTC.getText().equals(mpOHData.getReleaseToCode()))
        vzChanged = true;
      if (!mpCarrier.getText().equals(mpOHData.getCarrierID()))
        vzChanged = true;
      if (!mpDestStation.getSelectedStation().equals(mpOHData.getDestinationStation()))
        vzChanged = true;
    }
    catch (NoSuchFieldException nsfe)
    {
      displayError(nsfe.getMessage());
      logger.logException(nsfe);
    }
    
    mpBtnSubmit.setEnabled(vzChanged);
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
    setTableMouseListener();
  }
  
  /**
   *  Set up the mouse listener for the table
   */
  @Override
  protected void setTableMouseListener()
  {
    mpPopupMenu = new SKDCPopupMenu();
    mpTable.addMouseListener(new DacTableMouseListener(mpTable)
    {
      /**
       *  Defines popup menu items for <code>SKDCTable</code>, and adds listeners
       *  to them.
       */
      @Override
      public SKDCPopupMenu definePopup()
      {

        if (!mzDisplayOnly)
        {
          mpPopupMenu.add("Add Line", ADD_BTN, new ActionListener()
          {
            public void actionPerformed(ActionEvent arg0)
            {
              addLineButtonPressed();
            }
          });

          if (!mzAdding)
          {
            mpPopupMenu.add("Modify Line", MODIFY_BTN, new ActionListener()
            {
              public void actionPerformed(ActionEvent arg0)
              {
                modifyLineButtonPressed();
              }
            });
          }
          mpPopupMenu.add("Delete Line", DELETE_BTN, new ActionListener()
          {
            public void actionPerformed(ActionEvent arg0)
            {
              deleteLineButtonPressed();
            }
          });

        }

        return(mpPopupMenu);
      }
      
      /**
       *  Display the screen.
       */
      @Override
      public void displayDetail()
      {
      }
    });
  }

}
