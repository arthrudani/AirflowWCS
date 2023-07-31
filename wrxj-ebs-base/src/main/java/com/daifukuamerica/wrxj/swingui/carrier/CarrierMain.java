package com.daifukuamerica.wrxj.swingui.carrier;

import com.daifukuamerica.wrxj.dataserver.standard.StandardCarrierServer;
import com.daifukuamerica.wrxj.dbadapter.data.CarrierData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;

/**
 * Description:<BR>
 *    Primary frame for Carrier maintenance.
 *
 * @author       R.M.
 * @version      1.0
 * <BR>Created: 24-Nov-04<BR>
 *     Copyright (c) 2004<BR>
 *     Company:  Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class CarrierMain extends SKDCListFrame
{
  protected StandardCarrierServer mpCarrierServer;
  protected CarrierSearchFrame    searchFrame;
  protected CarrierData           searchCriteria;
  protected List                  alist  = new ArrayList();
  protected CarrierData           cadata = Factory.create(CarrierData.class);

  public CarrierMain() throws Exception
  {
    super("Carrier");

    mpCarrierServer = Factory.create(StandardCarrierServer.class);
    setSearchData("Carrier ID:", DBInfo.getFieldLength(CarrierData.CARRIERID_NAME));
    setDetailSearchVisible(true);
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

    searchButtonPressed();
  }

  @Override
  public void cleanUpOnClose()
  {
    mpCarrierServer.cleanUp();
    mpCarrierServer = null;
  }

/*===========================================================================
                  ****** Action methods go here ******
  ===========================================================================*/
  @Override
  protected void searchButtonPressed()
  {
    String carrier = getEnteredSearchText();
    if (searchCriteria == null)
    {
      searchCriteria = cadata.clone();
    }

    searchCriteria.clear();
    if (carrier.length() > 0)
    {
      searchCriteria.setKey(CarrierData.CARRIERID_NAME, carrier, KeyObject.LIKE);
    }
    refreshButtonPressed();
  }

  /**
   *  Show the order lines for this order header.
   */
  @Override
  protected void detailedSearchButtonPressed()
  {
    searchFrame = Factory.create(CarrierSearchFrame.class, mpCarrierServer);
    addSKDCInternalFrameModal(searchFrame, new JPanel[] {searchPanel, buttonPanel},
        new CarrierDetailSearchFrameHandler());
  }

  /**
   *  Handles Refresh button.
   */
  @Override
  protected void refreshButtonPressed()
  {
    if (searchCriteria != null)
    {
      try
      {                                  // Get data from the Order server.
        alist = mpCarrierServer.getCarrierList(searchCriteria);

        refreshTable(alist);
        if (alist.isEmpty())
        {
          sktable.clearTable();
          displayInfoAutoTimeOut("No data found", "Search Result");
        }
      }
      catch(DBException exc)
      {
        exc.printStackTrace(System.out);
        displayError(exc.getMessage(), "DB Error");
      }
    }
  }

  /**
   *  Add the Carrier to the system.
   */
  @Override
  protected void addButtonPressed()
  {
    AddCarrierFrame addFrame = Factory.create(AddCarrierFrame.class, mpCarrierServer);
    addSKDCInternalFrameModal(addFrame, new JPanel[] { searchPanel, buttonPanel },
        new CarrierAddFrameHandler());
  }

  /**
   *  Modify the Carrier.
   */
  @Override
  protected void modifyButtonPressed()
  {
    if (isSelectionValidForModify(false))
    {
      CarrierData myCarrierData = convertCarrierData(sktable.getSelectedRowData());
  
      ModifyCarrierFrame modifyFrame = Factory.create(ModifyCarrierFrame.class, mpCarrierServer);
      modifyFrame.setCurrentData(myCarrierData);
  
      addSKDCInternalFrameModal(modifyFrame, 
          new JPanel[] { searchPanel, buttonPanel }, 
          new CarrierModifyFrameHandler());
    }
  }

  /**
   *  Handles Delete button.
   */
  @Override
  protected void deleteButtonPressed()
  {
    int totalSelected = sktable.getSelectedRowCount();
    
    if (isSelectionValidForDelete(true))
    {
      boolean deleteCarrier = false;
      deleteCarrier = displayYesNoPrompt(
          "Do you really want to Delete\nall selected Carriers", 
          "Delete Confirmation");
      if (deleteCarrier)
      {
        String[] delCarrierList = null;
        // Get selected list of Order IDs
        delCarrierList = sktable.getSelectedColumnData(CarrierData.CARRIERID_NAME);

        int delCount = 0;
        int[] deleteIndices = sktable.getSelectedRows();
        for(int row = 0; row < totalSelected; row++)
        {
          int newCarrierStatus;
          try
          {
            newCarrierStatus = mpCarrierServer.deleteCarrier(delCarrierList[row]);
            if (newCarrierStatus == 1)
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
          displayInfoAutoTimeOut("Deleted " + delCount + " of " + totalSelected
              + " selected rows", "Delete Result");
        }
        else
        {
          displayInfoAutoTimeOut("Deleted " + delCount + " of " + totalSelected
              + " selected rows", "Delete Result");
        }
        sktable.deleteSelectedRows();    // Update the display.
      }
    }
  }

  /**
   * Convert a Map to a CarrierData
   * 
   * @param tmap
   * @return
   */
  protected CarrierData convertCarrierData(Map tmap)
  {
    CarrierData theData = cadata.clone();
    theData.dataToSKDCData(tmap);

    return(theData);
  }


/*===========================================================================
              ****** All Listener classes go here ******
  ===========================================================================*/

  /**
   *   Property Change event listener for Add frame.
   */
  private class CarrierAddFrameHandler implements PropertyChangeListener
  {
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(FRAME_CHANGE))
      {
        cadata = (CarrierData)pcevt.getNewValue();
        sktable.appendRow(cadata);
      }
    }
  }

  /**
   *   Property Change event listener for Modify frame.
   */
  private class CarrierModifyFrameHandler implements PropertyChangeListener
  {
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(FRAME_CHANGE))
      {
        cadata = (CarrierData)pcevt.getNewValue();
        sktable.modifySelectedRow(cadata);
      }
    }
  }

  /**
   * Property Change event listener for Search frame for the Carrier frame.
   */
  private class CarrierDetailSearchFrameHandler implements PropertyChangeListener
  {
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(FRAME_CHANGE))
      {
        alist = (List)pcevt.getNewValue();
        refreshTable(alist);
        searchCriteria = searchFrame.getSearchCriteria();
        searchField.setText("");
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
    return CarrierMain.class;
  }
}
