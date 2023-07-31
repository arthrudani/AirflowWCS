/**
 * 
 */
package com.daifukuamerica.wrxj.swingui.purchaseorder;

import com.daifukuamerica.wrxj.dataserver.standard.StandardPoReceivingServer;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLine;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCDateField;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.table.DacTableMouseListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <B>Description:</B> Add/Modify/View a multi-line expected receipt<BR>
 *
 * @author       mandrus<BR>
 * @version      1.0
 * 
 * <BR>Copyright (c) 2008 by Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class MultiLinePOFrame extends AbstractPOFrame
{
  protected SKDCTextField mpVendorID;
  protected SKDCDateField mpExpectedDate;
  protected int mnLineID = 0;
  
  protected StandardPoReceivingServer mpPOServer;

  protected List<PurchaseOrderLineData> mpPOLList;

  public MultiLinePOFrame()
  {
    super("Multi-Line Expected Receipt", "Expected Receipt Information");
    showTableWithDefaultButtons("AddPOLineFrame");
    
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
    resizable= true;
    mpPOLList = new ArrayList<PurchaseOrderLineData>();

    mpVendorID = new SKDCTextField(PurchaseOrderHeaderData.VENDORID_NAME);
    mpExpectedDate = new SKDCDateField(true);

    addInput("Expected Receipt ID", mpOrderID);
    addInput("Vendor", mpVendorID);
    addInput("Expected Date", mpExpectedDate);
  }

  /**
   * @see com.daifukuamerica.wrxj.swingui.purchaseorder.AbstractPOFrame#correctFieldEnabledValues()
   */
  @Override
  protected void correctFieldEnabledValues()
  {
    mpOrderID.setEnabled(mzAdding && !mzDisplayOnly);
    mpVendorID.setEnabled(!mzDisplayOnly);
    mpExpectedDate.setEnabled(!mzDisplayOnly);
    checkForPOHChange();
  }

  /**
   * @see com.daifukuamerica.wrxj.swingui.purchaseorder.AbstractPOFrame#setAddMode()
   */
  @Override
  protected void setAddMode()
  {
    setTitle("Add Expected Receipt");
    mzAdding = true;
    mpPOHData = Factory.create(PurchaseOrderHeaderData.class);
    
    mpBtnModLine.setVisible(false);
    correctFieldEnabledValues();
    clearButtonPressed();
    setTableMouseListener();
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

    mpBtnSubmit.setVisible(!mzDisplayOnly);
    mpBtnAddLine.setVisible(!mzDisplayOnly);
    mpBtnModLine.setVisible(!mzDisplayOnly);
    mpBtnDelLine.setVisible(!mzDisplayOnly);
    mpBtnClear.setVisible(!mzDisplayOnly);
    
    changeTable("PurchaseOrderLine");

    correctFieldEnabledValues();
    clearButtonPressed();
    refreshPOLines();
    setTableMouseListener();
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

    addChangeListeners();
    mpBtnSubmit.setEnabled(false);
    mpBtnModLine.setVisible(true);
    
    changeTable("PurchaseOrderLine");
    
    correctFieldEnabledValues();
    clearButtonPressed();
    refreshPOLines();
    setTableMouseListener();
  }
  
  /**
   * Activate the Submit button on a modify if the PO Header changes
   */
  protected void addChangeListeners()
  {
    POChangeListener vpOCL = new POChangeListener();
    
    mpVendorID.addKeyListener(vpOCL);
    mpExpectedDate.addActionListener(vpOCL);
  }

  /**
   * Activate the Submit button on a modify if the PO Header changes
   */
  private class POChangeListener implements KeyListener, ActionListener
  {
    public void keyPressed(KeyEvent e)          {     checkForPOHChange();    }
    public void keyReleased(KeyEvent e)         {     checkForPOHChange();    }
    public void keyTyped(KeyEvent e)            {     checkForPOHChange();    }
    public void actionPerformed(ActionEvent e)  {     checkForPOHChange();    }
  }
  
  /**
   * Create the order
   */
  @Override
  protected void okButtonPressed()
  {
    if (mzAdding)
    {
      if (displayYesNoPrompt("Add Expected Receipt"))
        addMultiLinePO();
    }
    else
    {
      if (displayYesNoPrompt("Update Expected Receipt"))
        updateMultiLinePO();
    }
  } 
  
  /**
   * Create the Expected Receipt
   */
  protected void addMultiLinePO()
  {
    String vsOrderID = mpOrderID.getText().trim();
    
    /*
     * Validation
     */
    if (vsOrderID.trim().length() == 0)
    {
      displayError("Expected Receipt ID can not be blank.", "Validation Error");
      return;
    }

    if (mpPOLList.size() < 1)
    {
      displayError("Expected Receipt must have at least one line.");
      return;
    }
    
    /*
     * Re-sequence order lines and make sure the order ID is correct.
     */
    int i = 1;
    for (PurchaseOrderLineData vpOLD : mpPOLList)
    {
      vpOLD.setLineID("" + i++);
      vpOLD.setOrderID(vsOrderID);
    }
    
    /*
     * Add the order
     */
    mpPOHData = Factory.create(PurchaseOrderHeaderData.class);
    mpPOHData.clear();
    mpPOHData.setOrderID(vsOrderID);
    mpPOHData.setVendorID(mpVendorID.getText());
    mpPOHData.setExpectedDate(mpExpectedDate.getDate());
    mpPOHData.setOrderStatus(DBConstants.EREXPECTED);
    mpPOHData.setLastActivityTime(new Date());
    
    try
    {
      mpPOServer.buildPO(mpPOHData, mpPOLList);
      displayInfoAutoTimeOut("Expected Receipt " + vsOrderID + " added successfully.");
      changed(null, mpPOHData);
      clearButtonPressed();
//      close();
    }
    catch (DBException dbe)
    {
      logAndDisplayException(dbe);
    }
  }
  
  /**
   * Update the order
   */
  protected void updateMultiLinePO()
  {
    mpPOHData.setKey(PurchaseOrderHeaderData.ORDERID_NAME, mpOrderID.getText());
    mpPOHData.setOrderID(mpOrderID.getText());
    mpPOHData.setVendorID(mpVendorID.getText());
    mpPOHData.setExpectedDate(mpExpectedDate.getDate());
    
    try
    {
      mpPOServer.modifyPOHead(mpPOHData);
      changed(null, mpPOHData);
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
      PurchaseOrderLine vpPOL = Factory.create(PurchaseOrderLine.class);
      String sLineID;
      try
      {
        sLineID = vpPOL.getNextLineID(mpOrderID.getText());
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
      UpdatePOLineFrame addLineFrame = Factory.create(
          UpdatePOLineFrame.class, mpPOServer, vsOrderID, mnLineID, !mzAdding);
      addSKDCInternalFrameModal(addLineFrame, mpButtonPanel,
          new POLineAddFrameHandler());
    }
  }
  
  /**
   * Property Change event listener for Add Line frame.
   */
  public class POLineAddFrameHandler implements PropertyChangeListener
  {
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(FRAME_CHANGE))
      {
        PurchaseOrderLineData oldata = (PurchaseOrderLineData)pcevt.getNewValue();
        if (mzAdding)
        {
          mpPOLList.add(oldata);
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
    PurchaseOrderLineData vpPOLData = Factory.create(PurchaseOrderLineData.class);
    vpPOLData.dataToSKDCData(mpTable.getSelectedRowData());

    UpdatePOLineFrame modifyLineFrame = Factory.create(
        UpdatePOLineFrame.class, mpPOServer, vpPOLData);

    addSKDCInternalFrameModal(modifyLineFrame, mpButtonPanel,
        new POLineModifyFrameHandler());
  }
  
  /**
   *   Property Change event listener for Modify Line frame.
   */
  private class POLineModifyFrameHandler implements PropertyChangeListener
  {
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(FRAME_CHANGE))
      {
        refreshPOLines();
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
    deleteLines = displayYesNoPrompt("Do you really want to Delete\nall selected Lines", "Delete Confirmation");
    if (deleteLines)
    {
      List<Map> delList = mpTable.getSelectedRowDataArray();
      PurchaseOrderLineData olData = Factory.create(PurchaseOrderLineData.class);
      for(int idx = 0; idx < delList.size(); idx++)
      {
        olData.dataToSKDCData(delList.get(idx));
        delListData(olData);
        try
        {
          mpPOServer.deletePOLine(olData.getOrderID(),olData.getLineID());
        }
        catch (DBException e)
        {
          logAndDisplayError("Failed to delete Expected Reciept Item "+ olData.getItem()+ 
                               "  Lot "+ olData.getLot());
          break;
        }
        olData.clear();
      }

      mpTable.deleteSelectedRows();
    }
  }
  
  /**
   * Keep our PO Line list in sync with our table 
   * 
   * @param oldata
   */
  private void delListData(PurchaseOrderLineData oldata)
  {
    int lineIdx = 0;
    int listSize = mpPOLList.size();
    boolean foundIt = false;

    for(lineIdx = 0; lineIdx < listSize; lineIdx++)
    {
      PurchaseOrderLineData ol = mpPOLList.get(lineIdx);
      if (ol.equals(oldata))
      {
        foundIt = true;
        break;
      }
    }

    if (foundIt) mpPOLList.remove(lineIdx);
  }


  /**
   * Populate the order lines list
   */
  private void refreshPOLines()
  {
    try
    {
      List l = mpPOServer.getPurchaseOrderLines(mpOrderID.getText());
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
    if (mzAdding)
    {
      mpOrderID.setText(createOrderIDByDateTime());
      mpOrderID.requestFocus();
    }
    mpVendorID.setText(mpPOHData.getVendorID());
    mpExpectedDate.setDate(mpPOHData.getExpectedDate());

    refreshPOLines();
    mpPOLList.clear();
    mnLineID = 0;
  }
  
  /**
   * 
   */
  @Override
  protected void closeButtonPressed()
  {
    if (mpPOLList.size() > 0)
    {
      if (!displayYesNoPrompt("Close without adding"))
      {
        return;
      }
    }
    mpPOServer.cleanUp();
    close();
  }
  
  /**
   * Check for order header changes and set the Submit buttons enabled
   * status accordingly.  
   */
  protected void checkForPOHChange()
  {
    boolean vzChanged = mzAdding;
    
    if (!mpVendorID.getText().equals(mpPOHData.getVendorID()))
      vzChanged = true;
    if (!mpExpectedDate.getDate().equals(mpPOHData.getExpectedDate()))
      vzChanged = true;
    
    mpBtnSubmit.setEnabled(vzChanged);
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
