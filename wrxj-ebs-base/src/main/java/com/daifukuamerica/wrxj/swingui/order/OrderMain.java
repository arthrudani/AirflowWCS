package com.daifukuamerica.wrxj.swingui.order;

import com.daifukuamerica.wrxj.dataserver.standard.StandardCarrierServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardCustomerServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardMaintenanceOrderServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.swing.table.DacTableMouseListener;
import com.daifukuamerica.wrxj.swingui.load.OrderEmpties;
import com.daifukuamerica.wrxj.swingui.load.OrderLoad;
import com.daifukuamerica.wrxj.swingui.move.MoveListFrame;
import com.daifukuamerica.wrxj.swingui.utility.AllocationDiagnostic;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;

/**
 * Description:<BR>
 *    Primary frame for Order maintenance.
 *
 * @author       A.D.   Original Version   05/13/02
 * @author       RKM   Added Shipment and Carrier handlers.
 * @version      2.0
 */
@SuppressWarnings("serial")
public class OrderMain extends SKDCListFrame
{
  protected OrderSearchFrame searchFrame;
  
  protected   SKDCButton  btnHold;
  protected   SKDCButton  btnReady;
  protected   SKDCButton  btnActivate;
  protected   SKDCButton  btnMoves;
  protected SKDCButton  btnDiag;
  
  protected StandardOrderServer             mpOrderServer;
  protected StandardMaintenanceOrderServer  mpMaintServer;
  private StandardCustomerServer          mpCustomerServer;
  private StandardCarrierServer           mpCarrierServer;
  protected OrderHeaderData  searchCriteria;
  protected OrderLineData    searchLine;
  protected OHEventListener  evtListener;
  protected List<Map>        alist        = new ArrayList<Map>();
  protected OrderHeaderData  ohdata       = Factory.create(OrderHeaderData.class);
  protected OrderLineData    oldata       = Factory.create(OrderLineData.class);
  
  protected String           MOVE_BTN        = "MOVE_BTN";
  protected String           DIAG_BTN        = "DIAG_BTN";

  private StandardLoadServer mpLoadServer;
  
  protected PopupMenu mpAddMenu = new PopupMenu();

  public OrderMain() throws Exception
  {
    super("OrderHeader");
    setDisplaySearchCount(true, "Order");

    userData = new SKDCUserData();

    mpOrderServer = Factory.create(StandardOrderServer.class);
    mpMaintServer = Factory.create(StandardMaintenanceOrderServer.class);
    mpLoadServer = Factory.create(StandardLoadServer.class);
    mpCarrierServer = Factory.create(StandardCarrierServer.class);
    mpCustomerServer = Factory.create(StandardCustomerServer.class);
    evtListener = new OHEventListener();

    defineButtons();

    setSearchData("Order ID", DBInfo.getFieldLength(OrderHeaderData.ORDERID_NAME));
    setDetailSearchVisible(true);
    buildButtonPanel();
  }

  /**
  * Overridden method so we can get our screen permissions.
  *
  * @param ipEvent ignored
  */
  @Override
  public void internalFrameOpened(InternalFrameEvent ipEvent)
  {
    super.internalFrameOpened(ipEvent);

    if (ePerms == null)
    {
      ePerms = userData.getOptionPermissionsByClass(OrderMain.class);
    }
    
    addButton.setAuthorization(ePerms.iAddAllowed);
    modifyButton.setAuthorization(ePerms.iModifyAllowed);
    deleteButton.setAuthorization(ePerms.iDeleteAllowed);

    btnHold.setAuthorization(ePerms.iModifyAllowed);
    btnReady.setAuthorization(ePerms.iModifyAllowed);
    btnActivate.setVisible(shouldEnableActivate());

    btnMoves.setAuthorization(true);
    //btnDiag.setAuthorization(true);
    btnDiag.setVisible(shouldEnableActivate());
    resetSize();

    searchButtonPressed();
  }

  protected boolean shouldEnableActivate()
  {
    return SKDCUserData.isSuperUser();
  }
  
  @Override
  public void cleanUpOnClose()
  {
    mpOrderServer.cleanUp();
    mpOrderServer = null;
    mpLoadServer.cleanUp();
    mpLoadServer = null;
    mpCarrierServer.cleanUp();
    mpCarrierServer = null;
    mpCustomerServer.cleanUp();
    mpCustomerServer = null;
  }

/*===========================================================================
                  ****** Action methods go here ******
  ===========================================================================*/
  @Override
  public void searchButtonPressed()
  {
    String ordnum = searchField.getText();
    if (searchCriteria == null)
    {
      searchCriteria = ohdata.clone();
    }
    if (searchLine == null)
    {
      searchLine = (OrderLineData)oldata.clone();
    }

    searchCriteria.clear();
    searchLine.clear();
    if (ordnum.length() > 0)
    {
      searchCriteria.setKey(OrderHeaderData.ORDERID_NAME, ordnum,
                            com.daifukuamerica.wrxj.jdbc.KeyObject.LIKE);
    }
    refreshButtonPressed();
  }

  /**
   *  Show the order lines for this order header.
   */
  @Override
  protected void detailedSearchButtonPressed()
  {
    searchFrame = Factory.create(OrderSearchFrame.class);
    addSKDCInternalFrameModal(searchFrame,
        new JPanel[] { searchPanel, buttonPanel },
        new OrderDetailSearchFrameHandler());
  }

  /**
   * Handles Refresh button.
   */
  @Override
  protected void refreshButtonPressed()
  {
    if (searchCriteria != null)
    {
      try
      {                                  // Get data from the Order server.
        if (searchLine.getKeyCount() == 0)
        {
          alist = mpOrderServer.getOrderHeaderData(searchCriteria);
        }
        else
        {
          alist = mpOrderServer.getOrderSearchList(searchCriteria, searchLine);
        }

        refreshTable(alist);
     }
      catch(DBException exc)
      {
        exc.printStackTrace(System.out);
        displayError(exc.getMessage(), "DB Error");
      }
    }
  }

  /**
   *  Add the Order and Order lines to the system.
   */
  @Override
  protected void addButtonPressed()
  {
    /*
     * This is the default add behavior
     */
    addItemOrder();
  }

  /**
   * Add a Cycle Count order
   */
  protected void addCycleCountOrder()
  {
    AddCciOrderFrame vpAddOrderFrame = Factory.create(AddCciOrderFrame.class);
    vpAddOrderFrame.setCategoryAndOption(getCategory(), "Orders");
    addSKDCInternalFrameModal(vpAddOrderFrame, new JPanel[] { searchPanel, buttonPanel },
        new OrderAddFrameHandler());
  }

  /**
   * Add an Empty Container order
   */
  protected void addEmptyContainerOrder()
  {
    OrderEmpties vpAddOrderFrame = Factory.create(OrderEmpties.class);
    vpAddOrderFrame.setCategoryAndOption(getCategory(), "Orders");
    addSKDCInternalFrameModal(vpAddOrderFrame, new JPanel[] { searchPanel, buttonPanel },
        new OrderAddFrameHandler());
  }

  /**
   * Add an Item Order
   */
  protected void addItemOrder()
  {
    ItemOrderFrame vpAddOrderFrame = Factory.create(ItemOrderFrame.class);
    vpAddOrderFrame.setCategoryAndOption(getCategory(), "Orders");
    addSKDCInternalFrameModal(vpAddOrderFrame, new JPanel[] { searchPanel, buttonPanel },
        new OrderAddFrameHandler());
  }

  /**
   * Add a Load order
   */
  protected void addLoadOrder()
  {
    OrderLoad vpAddOrderFrame = Factory.create(OrderLoad.class);
    vpAddOrderFrame.setCategoryAndOption(getCategory(), "Orders");
    addSKDCInternalFrameModal(vpAddOrderFrame, new JPanel[] { searchPanel, buttonPanel },
        new OrderAddFrameHandler());
  }

  /**
   *  Modify the Order.
   */
  @Override
  protected void modifyButtonPressed()
  {                                    // Get data on current row as
                                       // OrderHeader Data structure.
    int totalSelected;
    if ((totalSelected = sktable.getSelectedRowCount()) == 0)
    {
      displayInfoAutoTimeOut("No row selected to Modify", "Selection Error");
      return;
    }
    else if (totalSelected > 1)
    {
      displayInfoAutoTimeOut("Only one row can be selected to Modify at a time", "Selection Error");
      return;
    }

    OrderHeaderData myOrderData = convertOrderData(sktable.getSelectedRowData());

    try
    {
      int ortype = mpOrderServer.getOrderTypeValue(myOrderData.getOrderID());
      if (ortype != DBConstants.ITEMORDER)
      {
        displayError("Only ITEM orders may be modified");
        return;
      }
    }
    catch (DBException e)
    {
      displayError(e.getMessage(), "Getting Order Type");
      return;
    }
    
    ItemOrderFrame vpModifyFrame = Factory.create(ItemOrderFrame.class);
    vpModifyFrame.setCategoryAndOption(getCategory(), "Orders");
    vpModifyFrame.setModifyMode(myOrderData);
    addSKDCInternalFrameModal(vpModifyFrame, new JPanel[] { searchPanel, buttonPanel },
        new OrderModifyFrameHandler());
  }

  /**
   *  Handles Delete button.
   */
  @Override
  protected void deleteButtonPressed()
  {
    int totalSelected;
    if ((totalSelected = sktable.getSelectedRowCount()) == 0)
    {
      displayInfoAutoTimeOut("No row selected to Delete", "Selection Error");
      return;
    }

    boolean deleteOrders = false;
    deleteOrders = displayYesNoPrompt("Do you really want to Delete\nall selected Orders", "Delete Confirmation");
    if (deleteOrders)
    {
      String[] delOrderList = null;
                                       // Get selected list of Order IDs
      delOrderList = sktable.getSelectedColumnData(OrderHeaderData.ORDERID_NAME);

      int delCount = 0;
      int[] deleteIndices = sktable.getSelectedRows();
      for(int row = 0; row < totalSelected; row++)
      {
        int newOrderStatus;
        try
        {
          newOrderStatus = mpOrderServer.deleteOrder(delOrderList[row]);
          if (newOrderStatus == DBConstants.KILLED)
          {
            OrderHeaderData myOrderData = convertOrderData(sktable.getRowData(deleteIndices[row]));
            myOrderData.setOrderStatus(DBConstants.KILLED);
            sktable.modifyRow(deleteIndices[row], myOrderData);
            sktable.deselectRow(deleteIndices[row]);
          }
          else
          {
            delCount++;
          }
        }
        catch(DBException exc)
        {
          displayError(exc.getMessage(), "Delete Error");
                                       // De-Select the troubling row!
          sktable.deselectRow(deleteIndices[row]);
        }
      }
      if (delCount != totalSelected)
      {
        displayInfoAutoTimeOut("Deleted " +  delCount + " of " + totalSelected +
                    " selected rows",
                    "Delete Result");
      }
      else
      {
        displayInfoAutoTimeOut("Deleted " +  delCount + " of " + totalSelected +
                               " selected rows", "Delete Result");
      }
      refreshButtonPressed();    // Update the display.
    }
  }

  /**
   *  Display the Order.
   */
  @Override
  protected void viewButtonPressed()
  {
    int totalSelected;
    if ((totalSelected = sktable.getSelectedRowCount()) == 0)
    {
      displayInfoAutoTimeOut("No row selected to Display", "Selection Error");
      return;
    }
    else if (totalSelected > 1)
    {
      displayInfoAutoTimeOut("Only one row can be selected to display at a time", "Selection Error");
      return;
    }

    OrderHeaderData myOrderData = convertOrderData(sktable.getSelectedRowData());
    ItemOrderFrame vpDisplayFrame = Factory.create(ItemOrderFrame.class);
    vpDisplayFrame.setCategoryAndOption(getCategory(), "Orders");
    vpDisplayFrame.setDisplayMode(myOrderData);
    addSKDCInternalFrameModal(vpDisplayFrame, new JPanel[] { searchPanel, buttonPanel },
        new OrderModifyFrameHandler());
  }

  /**
   * Show the moves for this order
   */
  protected void movesButtonPressed()
  {
    if (isSelectionValid("Display Moves", false))
    {
      OrderHeaderData myData = convertOrderData(sktable.getSelectedRowData());
  
      List<KeyObject> vpKeys = new LinkedList<KeyObject>();
      vpKeys.add(new KeyObject(MoveData.ORDERID_NAME, myData.getOrderID()));
      
      MoveListFrame vpViewMoves = new MoveListFrame(KeyObject.toKeyArray(vpKeys));
      vpViewMoves.setAllowDuplicateScreens(true);
      addSKDCInternalFrameModal(vpViewMoves);
    }
  }

  /**
   * Diagnose this order
   */
  private void diagButtonPressed()
  {
    if (isSelectionValid("run Allocation Diag", false))
    {
      OrderHeaderData vpOHData = convertOrderData(sktable.getSelectedRowData());
  
      AllocationDiagnostic allocDiag = new AllocationDiagnostic(vpOHData.getOrderID());
      addSKDCInternalFrameModal(allocDiag);
      allocDiag.runDiagnostic();
    }
  }
  
  protected void ordStatusChange(int status)
  {
    int totalSelected;
    if ((totalSelected = sktable.getSelectedRowCount()) == 0)
    {
      displayInfoAutoTimeOut("No row selected", "Selection Error");
      return;
    }

    String message;
    switch(status)
    {
      case DBConstants.HOLD:
        message = "Do you really want to Hold\nall selected Orders";
        break;

      case DBConstants.ALLOCATENOW:
        message = "Do you really want to submit all\nselected orders for immediate\nallocation";
        break;

      case DBConstants.READY:
        message = "Do you really want to make READY\nall selected Orders";
        break;

      default:                         // Should never happen!
      displayInfoAutoTimeOut("Unknown Button pressed...", "Selection Error");
        return;
    }

    if (displayYesNoPrompt(message, "Status Change Confirmation"))
    {                                  // Get selected list of Order data
      List<OrderHeaderData> changeList = DBHelper.convertData(
                      sktable.getSelectedRowDataArray(), OrderHeaderData.class);
      if (changeList.isEmpty())
      {
        return;
      }

      int changeCount = 0;
      int[] changeIndex = sktable.getSelectedRows();
      for(int row = 0; row < totalSelected; row++)
      {                                // Stuff the current displayed line in
                                       // a OrderHeaderData buffer.
        OrderHeaderData dispBuffer = changeList.get(row);
        
        if (!canOrderChangeStatus(dispBuffer, status))
        {
          return;
        }
        
        try
        {
          if (status == DBConstants.HOLD)
          {
            if (dispBuffer.getOrderStatus() == DBConstants.ALLOCATING)
            {
              message = "Order " + dispBuffer.getOrderID().trim() + " is being allocated.  Hold anyway";
              if (!displayYesNoPrompt(message, "Status Change Confirmation"))
              {
                sktable.deselectRow(changeIndex[row]);
                continue;
              }
            }
            mpOrderServer.holdOrder(dispBuffer.getOrderID());
            dispBuffer.setOrderStatus(DBConstants.HOLD);
          }
          else if (status == DBConstants.ALLOCATENOW)
          {
            if (!mpOrderServer.isMaintenanceOrder(dispBuffer.getOrderID()))
              mpOrderServer.allocateOrder(dispBuffer.getOrderID());
            else
              mpMaintServer.allocateOrder(dispBuffer.getOrderID());

            dispBuffer.setOrderStatus(DBConstants.ALLOCATENOW);
          }
          else
          {                            // Mark the order as READY in the DB.
            mpOrderServer.readyOrder(dispBuffer.getOrderID());
                                       // Mark the display line buffer as READY
                                       // and update the display.
            dispBuffer.setOrderStatus(DBConstants.READY);
          }
          sktable.modifyRow(changeIndex[row], dispBuffer);
          sktable.deselectRow(changeIndex[row]);
          changeCount++;
        }
        catch(DBException exc)
        {
          displayError(exc.getMessage(), "Status Change Error");
        }
      }

      if (changeCount > 0)             // If something was held display message.
      {
        // re-sort the data
        refreshButtonPressed();

        // Now that we've re-sorted, lets go back through the original list of items that were changed,
        // find them in the model, and highlight their corresponding rows, and move the scroll bar to
        // the last selected item.
        sktable.clearSelection();
        for(int row = 0; row < totalSelected; row++)
        {   
          OrderHeaderData dispBuffer = changeList.get(row);
          
          for(int searchRow = 0; searchRow < sktable.getRowCount(); searchRow++) //find the orderid that we just changed and highlight it
          {
            OrderHeaderData thisRowsData = convertOrderData(sktable.getRowData(searchRow));
            if(thisRowsData.getOrderID().equals(dispBuffer.getOrderID()))
            {
              sktable.addRowSelectionInterval(searchRow,searchRow);
              sktable.moveScrollToRow(searchRow);
            }
          }
        }
        
        String operationType = (status == DBConstants.HOLD) ? "Held " :
                               (status == DBConstants.ALLOCATENOW) ? "Allocated " : "made Ready ";
        displayInfoAutoTimeOut(operationType +  changeCount + " of " + totalSelected +
                    "\n Orders", "Status Change Confirmation");
      }
    }
  }

  /**
   * Can this order change status?
   * 
   * <P>Provided for extensibility.  Default return value is true.</P>
   * @param ipOHData
   * @return
   */
  protected boolean canOrderChangeStatus(OrderHeaderData ipOHData, int inNewOrdStatus)
  {
    return true;
  }
  
  /**
   * Convert a map to OrderHeaderData
   *  
   * @param tmap
   * @return OrderHeaderData
   */
  protected OrderHeaderData convertOrderData(Map tmap)
  {
    OrderHeaderData theData = ohdata.clone();
    theData.dataToSKDCData(tmap);

    return(theData);
  }

/*===========================================================================
              ****** All other private methods go here ******
  ===========================================================================*/
  protected void buildButtonPanel()
  {
    buttonPanel.add(addButton);              // Disable these buttons initially.
    buttonPanel.add(modifyButton);           // Add the buttons to the panel
    buttonPanel.add(deleteButton);
    
    buttonPanel.add(Box.createHorizontalStrut(10));
    
    buttonPanel.add(btnReady);
    buttonPanel.add(btnHold);
    buttonPanel.add(btnActivate);
    buttonPanel.add(btnDiag);

    buttonPanel.add(Box.createHorizontalStrut(10));

    buttonPanel.add(viewButton);
    buttonPanel.add(btnMoves);
    
    buttonPanel.add(Box.createHorizontalStrut(10));
    
    buttonPanel.add(closeButton);
    
    addAddButtonPopupMenu();
  }

  /**
   * Right-click pop-up on add button
   */
  protected void addAddButtonPopupMenu()
  {
    /*
     * These are the menu items
     */
    MenuItem vpAddCciOrder = new MenuItem("Cycle Count Order");
    vpAddCciOrder.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          addCycleCountOrder();
        }
      });
    MenuItem vpAddECOrder = new MenuItem("Empty Container Order");
    vpAddECOrder.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          addEmptyContainerOrder();
        }
      });
    MenuItem vpAddItemOrder = new MenuItem("Item Order");
    vpAddItemOrder.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          addItemOrder();
        }
      });
    MenuItem vpAddLoadOrder = new MenuItem("Load Order");
    vpAddLoadOrder.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          addLoadOrder();
        }
      });
    
    /*
     * This is the menu
     */
    mpAddMenu.add(vpAddCciOrder);
    mpAddMenu.add(vpAddECOrder);
    mpAddMenu.add(vpAddItemOrder);
    mpAddMenu.add(vpAddLoadOrder);
    addButton.add(mpAddMenu);
    
    /*
     * Show the menu on a right-click
     */
    addButton.addMouseListener(new MouseListener()
      {
        public void mouseClicked(MouseEvent e)  {}
        public void mouseEntered(MouseEvent e)  {}
        public void mouseReleased(MouseEvent e) {}
        public void mouseExited(MouseEvent e)   {}
  
        public void mousePressed(MouseEvent e)
        {
          if (e.getButton() == MouseEvent.BUTTON3 &&
              addButton.isEnabled())
          {
            mpAddMenu.show(e.getComponent(), e.getX(), e.getY());
          }
        }
      });
  }
  
  /**
   *  Defines all buttons on the main Order Panels, and adds listeners
   *  to them.
   */
  private void defineButtons()
  {
    btnReady   = new SKDCButton("Mark Ready", "Mark Order Ready", 'k');
    btnHold    = new SKDCButton("Hold", "Hold Order", 'H');
    btnActivate = new SKDCButton("Allocate", "Ask Allocator for immediate allocation", 'e');
    btnMoves  = new SKDCButton("Moves", "Display Moves", 'v');
    btnDiag  = new SKDCButton("Diag", "Allocation Diag", 'g');
    viewButton.setToolTipText("Show Order details");
    viewButton.setVisible(true);
    
                                       // Attach listeners.
    searchButton.addEvent(SEARCH_BTN, evtListener);
    detailedSearchButton.addEvent(DETSEARCH_BTN, evtListener);
    addButton.addEvent(ADD_BTN, evtListener);
    modifyButton.addEvent(MODIFY_BTN, evtListener);
    deleteButton.addEvent(DELETE_BTN, evtListener);
    btnReady.addEvent(READY_BTN, evtListener);
    btnHold.addEvent(HOLD_BTN, evtListener);
    btnActivate.addEvent(ACTIVATE_BTN, evtListener);
    refreshButton.addEvent(REFRESH_BTN, evtListener);
    btnMoves.addEvent(MOVE_BTN, evtListener);
    btnDiag.addEvent(DIAG_BTN, evtListener);
  }

  /**
   *  Method to set the search criteria filter.
   *
   *  @param order ColumnObject containing criteria to use in search.
   */
   public void setSearchCriteria(OrderHeaderData ipOrderHeaderData, OrderLineData ipOrderLineData)
   {
     searchCriteria = ipOrderHeaderData;
     if (ipOrderLineData == null)
     {
       oldata.clear();
       searchLine = oldata;
     }
     else
     {
       searchLine = ipOrderLineData;
     }
     refreshButtonPressed();
   }

/*===========================================================================
              ****** All Listener classes go here ******
  ===========================================================================*/
  /**
   * Mouse listener for the table 
   */
  @Override
  protected void setTableMouseListener()
  {
    sktable.addMouseListener(new DacTableMouseListener(sktable)
    {
     /**
      *  Defines popup menu items for <code>SKDCTable</code>, and adds listeners
      *  to them.
      */
      @Override
      public SKDCPopupMenu definePopup()
      {
        popupMenu.add("Add", ADD_BTN, getDefaultListener());
        popupMenu.add("Modify", MODIFY_BTN, getDefaultListener());
        popupMenu.add("Delete", true, DELETE_BTN, getDefaultListener());
        popupMenu.add("Mark Ready", READY_BTN, evtListener);
        popupMenu.add("Hold", HOLD_BTN, evtListener);
        if (SKDCUserData.isSuperUser())
        {
          popupMenu.add("Activate Now", true, ACTIVATE_BTN, evtListener);
        }
        popupMenu.add("Display", VIEW_BTN, getDefaultListener());
        popupMenu.add("Moves", MOVE_BTN, evtListener);
        if (SKDCUserData.isSuperUser())
        {
          popupMenu.add("Allocation Diag.", DIAG_BTN, evtListener);
        }
        
        if (ePerms == null)
        {
          ePerms = getPermissions();
        }
        if (ePerms != null)
        {
          popupMenu.setAuthorization("Add", ePerms.iAddAllowed);
          popupMenu.setAuthorization("Modify", ePerms.iModifyAllowed);
          popupMenu.setAuthorization("Delete", ePerms.iDeleteAllowed);
          popupMenu.setAuthorization("Mark Ready", ePerms.iModifyAllowed);
          popupMenu.setAuthorization("Hold", ePerms.iModifyAllowed);
        }
        
        return(popupMenu);
      }
     /**
      *  Display the Order Line screen.
      */
      @Override
      public void displayDetail()
      {
        viewButtonPressed();
      }
    });
  }

  /**
   *   Property Change event listener for Add frame.
   */
  protected class OrderAddFrameHandler implements PropertyChangeListener
  {
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(FRAME_CHANGE))
      {
        ohdata = (OrderHeaderData)pcevt.getNewValue();
        sktable.appendRow(ohdata);
      }
    }
  }

  /**
   *   Property Change event listener for Modify frame.
   */
  private class OrderModifyFrameHandler implements PropertyChangeListener
  {
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(FRAME_CHANGE))
      {
        ohdata = (OrderHeaderData)pcevt.getNewValue();
        if (ohdata == null)
          refreshButtonPressed();
        else
          sktable.modifySelectedRow(ohdata);
      }
    }
  }

  /**
   * Property Change event listener for Search frame for the Order line frame.
   */
  private class OrderDetailSearchFrameHandler implements PropertyChangeListener
  {
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(FRAME_CHANGE))
      {
        alist = (List<Map>)pcevt.getNewValue();
        refreshTable(alist);
//        
//        int count = sktable.getRowCount();
//        if (count == 1)
//          displayInfoAutoTimeOut("1 Order found");
//        else
//          displayInfoAutoTimeOut(count + " Orders found");
//
        searchCriteria = searchFrame.getSearchCriteria();
        searchLine = searchFrame.getSearchLine();
        searchField.setText("");
      }
    }
  }

  /**
   *  Button Listener class.
   */
  private class OHEventListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      String which_button = e.getActionCommand();

      if (which_button.equals(HOLD_BTN))
      {
        ordStatusChange(DBConstants.HOLD);
      }
      else if (which_button.equals(READY_BTN))
      {
        ordStatusChange(DBConstants.READY);
      }
      else if (which_button.equals(REFRESH_BTN))
      {
        refreshButtonPressed();
      }
      else if (which_button.equals(ACTIVATE_BTN))
      {
        ordStatusChange(DBConstants.ALLOCATENOW);
      }
      else if (which_button.equals(MOVE_BTN))
      {
        movesButtonPressed();
      }
      else if (which_button.equals(DIAG_BTN))
      {
        diagButtonPressed();
      }
    }
  }
  
  /**
   * Get the class name that will be used in the RoleOptions table.  This 
   * method facilitates the getting of permissions when setCategoryAndOption()
   * is not called and the implemented class is different from the baseline
   * class.
   * 
   * @return <code>Class</code>
   */
  @Override
  protected Class getRoleOptionsClass()
  {
    return OrderMain.class;
  }
}
