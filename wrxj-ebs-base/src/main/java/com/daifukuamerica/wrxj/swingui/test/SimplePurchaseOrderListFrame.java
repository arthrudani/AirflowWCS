package com.daifukuamerica.wrxj.swingui.test;

import com.daifukuamerica.wrxj.dataserver.standard.StandardPoReceivingServer;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;

@SuppressWarnings("serial")
public class SimplePurchaseOrderListFrame extends SKDCListFrame
{
  public SimplePurchaseOrderListFrame()
  {
    super("SimpleER");
    modifyButton.setVisible(false);
    
    JPanel vpWarningPanel = getEmptyListSearchPanel();
    vpWarningPanel.add(new SKDCLabel("WARNING: FOR DEVELOPER USE ONLY!"));
    getContentPane().add(vpWarningPanel, BorderLayout.NORTH);
    
   
    buttonPanel.add(refreshButton);
    refreshButton.setVisible(true);
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
    String vsTest = "Select poh.sorderid as \"SORDERID\", " +
                    " poh.dExpectedDate as \"DEXPECTEDDATE\"," +
                    " pol.sRouteID as \"SSTATIONNAME\"," +
                    " pol.sItem as \"SITEM\"," +
                    " pol.sLot as \"SLOT\"," +
                    " pol.fExpectedQuantity as \"FEXPECTED\"" +
                    " from purchaseorderheader poh, purchaseorderline pol" +
                    " where poh.sorderid=pol.sorderid";
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
    SimplePurchaseOrderFrame vpAddFrame = new SimplePurchaseOrderFrame();
    this.addSKDCInternalFrameModal(vpAddFrame, buttonPanel,
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
    StandardPoReceivingServer vpPOServer = Factory.create(StandardPoReceivingServer.class);
    
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
        try
        {
          vpPOServer.deletePO(delOrderList[row]);
          delCount++;
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
    return SimplePurchaseOrderListFrame.class;
  }
}
