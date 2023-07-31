package com.daifukuamerica.wrxj.swingui.test;

import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import com.daifukuamerica.wrxj.swingui.order.SingleLineItemOrderFrame;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;

@SuppressWarnings("serial")
public class SimpleItemOrderListFrame extends SKDCListFrame
{
  SKDCButton mpBtnHold;
  SKDCButton mpBtnActivate;
  StandardOrderServer mpOrderServer;
  
  public SimpleItemOrderListFrame()
  {
    super("SimpleOrder");
    modifyButton.setVisible(false);
    
    JPanel vpWarningPanel = getEmptyListSearchPanel();
    vpWarningPanel.add(new SKDCLabel("WARNING: FOR DEVELOPER USE ONLY!"));
    getContentPane().add(vpWarningPanel, BorderLayout.NORTH);
    
    buttonPanel.add(refreshButton);
    refreshButton.setVisible(true);
    
    mpBtnHold = new SKDCButton("Hold", "Hold Order");
    buttonPanel.add(mpBtnHold);
    mpBtnHold.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        ordStatusChange(DBConstants.HOLD);
      }
    });
    
    mpBtnActivate = new SKDCButton("Activate", "Activate Order");
    buttonPanel.add(mpBtnActivate);
    mpBtnActivate.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        ordStatusChange(DBConstants.ALLOCATENOW);
      }
    });
    
    mpOrderServer = Factory.create(StandardOrderServer.class, getClass().getSimpleName());
  }

  /**
   * Copied (more or less) from OrderMain.java
   * 
   * @param status
   */
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
      List<OrderHeaderData> changeList = convertTableData();
      if (changeList == null)
      {
        return;
      }

      int changeCount = 0;
      int[] changeIndex = sktable.getSelectedRows();
      for(int row = 0; row < totalSelected; row++)
      {                                // Stuff the current displayed line in
                                       // a OrderHeaderData buffer.
        OrderHeaderData dispBuffer = changeList.get(row);
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
            mpOrderServer.allocateOrder(dispBuffer.getOrderID());
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
        refreshTable();
        
        String operationType = (status == DBConstants.HOLD) ? "Held " :
                               (status == DBConstants.ALLOCATENOW) ? "Allocated " : "made Ready ";
        displayInfoAutoTimeOut(operationType +  changeCount + " of " + totalSelected +
                    "\n Orders", "Status Change Confirmation");
      }
    }
  }

  /**
   * Copied (more or less) from OrderMain.java
   * 
   * @return
   */
  protected List<OrderHeaderData> convertTableData()
  {
    List<OrderHeaderData> vpChangeList = new ArrayList<OrderHeaderData>();

    List<Map> vpSelectedData = sktable.getSelectedRowDataArray();
    for (Map m : vpSelectedData)
    {
      OrderHeaderData vpOHData = Factory.create(OrderHeaderData.class);
      vpOHData.setOrderID(m.get(OrderHeaderData.ORDERID_NAME).toString());
      vpOHData.setOrderStatus((Integer)m.get(OrderHeaderData.ORDERSTATUS_NAME));
      vpChangeList.add(vpOHData.clone());
    }

    return(vpChangeList);
  }
  
  /**
   * Do this when the frame opens
   */
  @Override
  public void internalFrameOpened(InternalFrameEvent ipEvent)
  {
    super.internalFrameOpened(ipEvent);
    refreshTable();
  }

  /**
   * REFRESH button
   */
  @Override
  protected void refreshButtonPressed()
  {
    refreshTable();
  }
  
  /**
   * REFRESH
   */
  protected void refreshTable()
  {
    DBObject vpDB = new DBObjectTL().getDBObject();
    String vsTest = "Select oh.sorderid as \"SORDERID\", " +
                    " oh.dOrderedTime as \"DORDEREDTIME\"," +
                    " oh.iOrderStatus as \"IORDERSTATUS\"," +
                    " oh.sDestinationStation as \"SSTATIONNAME\"," +
                    " ol.sItem as \"SITEM\"," +
                    " ol.sOrderLot as \"SLOT\"," +
                    " ol.fOrderQuantity as \"FORDERED\"," +
                    " ol.fPickQuantity as \"FPICK\"" +
                    " from orderheader oh, orderline ol" +
                    " where oh.sorderid=ol.sorderid";
    try
    {
      refreshTable(vpDB.execute(vsTest).getRows());
      displayInfoAutoTimeOut(sktable.getRowCount() + " record(s) found");
    }
    catch (DBException dbe)
    {
      dbe.printStackTrace();
    }
  }
  

  /**
   * ADD button pressed
   */
  @Override
  protected void addButtonPressed()
  {
    SingleLineItemOrderFrame vpAddFrame = new SingleLineItemOrderFrame();
    vpAddFrame.setAutoClearOnAdd(false);
    addSKDCInternalFrameModal(vpAddFrame, buttonPanel,
      new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e)
      {
        refreshTable();
      }
    });
  }

  
  /**
   * DELETE button pressed
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
      delOrderList = sktable.getSelectedColumnData("SORDERID");

      int delCount = 0;
      int[] deleteIndices = sktable.getSelectedRows();
      for(int row = 0; row < totalSelected; row++)
      {
        int newOrderStatus;
        try
        {
          newOrderStatus = mpOrderServer.deleteOrder(delOrderList[row]);
          if (newOrderStatus != DBConstants.KILLED)
          {
            delCount++;
          }
        }
        catch(DBException exc)
        {
          displayError(exc.getMessage(), "Delete Error");
          sktable.deselectRow(deleteIndices[row]);
        }
      }
      displayInfoAutoTimeOut("Deleted " +  delCount + " of " + totalSelected +
                               " selected rows", "Delete Result");
      refreshTable();
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
    return SimpleItemOrderListFrame.class;
  }
}
