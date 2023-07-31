package com.daifukuamerica.wrxj.swingui.purchaseorder;

import com.daifukuamerica.wrxj.dataserver.standard.StandardPoReceivingServer;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@SuppressWarnings("serial")
public class POMain extends SKDCListFrame
{
  protected POSearchFrame searchFrame;

  protected StandardPoReceivingServer mpPOServer;
  protected POHLSearch  srchpo = new POHLSearch();
  protected boolean detailsearch = false;
  
  /**
   * Default CONSTRUCTOR
   */
  public POMain()
  {
    this("PurchaseOrderHeader");
  }
  
  /**
   * Specialized constructor.
   * @param isTitle the title of the screen.
   */
  public POMain(String isTitle)
  {
    super(isTitle);
    setSearchData("Expected Receipt ID", DBInfo.getFieldLength(PurchaseOrderHeaderData.ORDERID_NAME));
    setDetailSearchVisible(true);
    setDisplaySearchCount(true, "expected receipt");
    
    mpPOServer = Factory.create(StandardPoReceivingServer.class);
    
    viewButton.setVisible(true);
  }
  
  /*========================================================================*/
  /*  Action Methods                                                        */
  /*========================================================================*/
  @Override
  protected void searchButtonPressed()
  {
    detailsearch = false;
    srchpo.clear();
    
    srchpo.podata.setOrderID(searchField.getText());
    srchpo.poldata.setItem("");
    srchpo.podata.setOrderStatus(SKDCConstants.ALL_INT);

    refreshButtonPressed();
  }

  /**
   *  Do a More detailed search for this PO header.
   */
  @Override
  protected void detailedSearchButtonPressed()
  {
    searchFrame = Factory.create(POSearchFrame.class, mpPOServer);
    addSKDCInternalFrameModal(searchFrame, getButtonPanel(),  new PODetailSearchFrameHandler());
  }

  /**
   *  Add the PO and PO lines to the system.
   */
  @Override
  protected void addButtonPressed()
  {
    AbstractPOFrame vpAddFrame = Factory.create(AbstractPOFrame.class);
    vpAddFrame.setAddMode();
    addSKDCInternalFrameModal(vpAddFrame, getButtonPanel(), new POAddFrameHandler());
  }

  /**
   *  refresh the screen.
   */
  @Override
  protected void refreshButtonPressed()
  {
    List alist = new ArrayList();

    if(detailsearch == true)
    {
      searchField.setText("");
      try
      {
      alist = mpPOServer.getPOSearchList(srchpo.podata, srchpo.poldata);
      }
      catch(Exception e)
      {
      displayError(e.getMessage(), "Search Information");
      }
    }
    else
    {
      searchField.setText(srchpo.podata.getOrderID());
      try
      {
        alist = mpPOServer.getPurchaseOrderList(srchpo.podata.getOrderID(), 
                                              srchpo.podata.getOrderStatus());
      }
      catch (Exception e)
      {
        displayError(e.getMessage(), "Search Information");
      }
    }
    refreshTable(alist);
  }

  /**
   *  Modify the PO.
   */
  @Override
  protected void modifyButtonPressed()
  {
    int totalSelected;
    PurchaseOrderHeaderData podata = Factory.create(PurchaseOrderHeaderData.class);

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

    podata.dataToSKDCData(sktable.getSelectedRowData());

    /*
    * Make sure the PO still exists (and update the PO info)
    */
    try
    {
      PurchaseOrderHeaderData vpPOCheck = mpPOServer.getPoHeaderRecord(podata.getOrderID());
      if (vpPOCheck == null)
      {
        throw new NoSuchElementException();
      }
      podata = vpPOCheck;
    }
    catch (NoSuchElementException nsee)
    {
      displayError("Expected Receipt " + podata.getOrderID() + " no longer exists.");
      refreshButtonPressed();
      return;
    }
    catch (DBException dbe)
    {
      displayError(dbe.getMessage(), "Expected Receipt Error");
      refreshButtonPressed();
      return;
    }

    try
    {
                            // Check the PO status.
      int postat = mpPOServer.getPOStatus(podata.getOrderID());
      if (postat != DBConstants.EREXPECTED )
      {
        displayError("Expected Receipt " + podata.getOrderID() + " status must be\n" +
          "Expected or Receiving before Modification is allowed");
        return;
      }
    }
    catch(Exception e)
    {
      displayError(e.getMessage(), "Getting Expected Receipt Status");
      return;
    }

    AbstractPOFrame vpModifyFrame = Factory.create(AbstractPOFrame.class);
    vpModifyFrame.setModifyMode(podata);
    addSKDCInternalFrameModal(vpModifyFrame, getButtonPanel(), new POModifyFrameHandler());
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

    boolean deletePOs = false;
    deletePOs = displayYesNoPrompt("Do you really want to delete\n" +
                        "all selected Expected Receipts", "Delete Confirmation");
    if (deletePOs)
    {
      String[] delPOList = null;
                            // Get selected list of PO IDs
      delPOList = sktable.getSelectedColumnData(PurchaseOrderHeaderData.ORDERID_NAME);

      int delCount = 0;
      int[] deleteIndices = sktable.getSelectedRows();
      for(int row = 0; row < totalSelected; row++)
      {
        try
        {
          try
          {
            mpPOServer.deletePO(delPOList[row]);
          }
          catch (NoSuchElementException nsee) {}
          delCount++;
        }
        catch(Exception exc)
        {
          displayError(exc.getMessage(), "Delete Error");
                                    // De-Select the troubling row!
          sktable.deselectRow(deleteIndices[row]);
        }
      }
      if (delCount != totalSelected)
      {
        displayInfo("Deleted " +  delCount + " of " + totalSelected +
           " selected rows", "Delete Result");
      }
      else
      {
        displayInfoAutoTimeOut("Deleted " +  delCount + " of " + totalSelected +
                    " selected rows", "Delete Result");
      }
      sktable.deleteSelectedRows();    // Update the display.
    }

    return;
  }

  /**
   *  Handles Clear button.
   */
  @Override
  protected void clearButtonPressed()
  {
    searchField.setText("");
    sktable.clearTable();

    return;
  }

  /**
   *  Display the PO lines
   */
  @Override
  protected void viewButtonPressed()
  {
    PurchaseOrderHeaderData vpPOHData = Factory.create(PurchaseOrderHeaderData.class);

    int selectedRowIndex = sktable.getSelectedRow();
    if (selectedRowIndex == -1)
    {
      displayInfoAutoTimeOut("No row selected", "Selection Error");
      return;
    }
    vpPOHData.clear();
    vpPOHData.dataToSKDCData(sktable.getSelectedRowData());

    AbstractPOFrame vpPOFrame = Factory.create(AbstractPOFrame.class);
    vpPOFrame.setDisplayMode(vpPOHData);
    addSKDCInternalFrameModal(vpPOFrame, getButtonPanel(), new POLineFrameHandler());
  }
  
  /*========================================================================*/
  /*  All Listener classes go here                                          */
  /*========================================================================*/

  /**
   *  Property Change event listener for Add frame.
   */
  private class POAddFrameHandler implements PropertyChangeListener
  {
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(FRAME_CHANGE))
      {
        PurchaseOrderHeaderData newpo = (PurchaseOrderHeaderData)pcevt.getNewValue();
        sktable.appendRow(newpo);
      }
    }
  }

  /**
   *  Property Change event listener for Modify frame.
   */
  private class POModifyFrameHandler implements PropertyChangeListener
  {
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(FRAME_CHANGE))
      {
        refreshButtonPressed();
      }
    }
  }

  /**
   * Property Change event listener for PO Line frame.
   */
  private class POLineFrameHandler implements PropertyChangeListener
  {
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(FRAME_CLOSING))
      {
      }
      else if (prop_name.equals(FRAME_CHANGE))
      {
        refreshButtonPressed();
      }
    }
  }

  /**
   * Property Change event listener for Search frame for the PO line frame.
   */
  private class PODetailSearchFrameHandler implements PropertyChangeListener
  {
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(FRAME_CHANGE))
      {
        detailsearch = true;
        srchpo = (POHLSearch)pcevt.getNewValue();
        refreshButtonPressed();
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
    return POMain.class;
  }
}
