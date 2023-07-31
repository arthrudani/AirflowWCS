package com.daifukuamerica.wrxj.swingui.station;

import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ListSelectionModel;
import javax.swing.event.InternalFrameEvent;

/**
 * A screen class for displaying a list of stations.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class StationListFrame extends SKDCListFrame
{
  StandardStationServer stationServ = Factory.create(StandardStationServer.class);

  /**
   *  Create station list frame.
   */
  public StationListFrame()
  {
    super("Station");
    sktable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setSearchData("Station", DBInfo.getFieldLength(StationData.STATIONNAME_NAME));
  }

  
  /**
   * The search button was pressed
   */
  @Override
  protected void searchButtonPressed()
  {
    refreshTable();
  }
  
  /**
   *  Method to filter by name. Refreshes display.
   *
   *  @param s Station to search for.
   */
  protected void refreshTable()
  {
    try
    {
      refreshTable(stationServ.getStationDataListByStation(getEnteredSearchText()));
    }
    catch (DBException e)
    {
      e.printStackTrace(System.out);
      displayError("Database Error: " + e);
    }
  }

  /**
   *  Method to clean up as needed at closing.
   */
  @Override
  public void cleanUpOnClose()
  {
    stationServ.cleanUp();
  }

  /**
   * Sets screen permissions.
   *
   * <p><b>Details:</b> <code>internalFrameOpened</code> augments the
   * supermethod by setting the screen permissions.</p>
   *
   * @param ipEvent ignored
   */
  @Override
  public void internalFrameOpened(InternalFrameEvent ipEvent)
  {
    super.internalFrameOpened(ipEvent);

    refreshTable();
  }

  /**
   *  Action method to handle Add button. Brings up screen to do the add.
   */
  @Override
  protected void addButtonPressed()
  {
    UpdateStation updateStation = Factory.create(UpdateStation.class, "Add Station");
    addSKDCInternalFrameModal(updateStation, buttonPanel,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent e)
          {
            String prop = e.getPropertyName();
            if (prop.equals(FRAME_CHANGE))
            {
              refreshTable();
            }
          }
        });
  }

  /**
   *  Action method to handle Delete button.
   */
  @Override
  protected void deleteButtonPressed()
  {
    if (isSelectionValidForDelete(false))
    {
      String vsStation = sktable.getCurrentRowDataField(StationData.STATIONNAME_NAME).toString();
      if (displayYesNoPrompt("Delete Station " + vsStation, "Delete Confirmation"))
      {
        try
        {
          stationServ.deleteStation(vsStation);
          sktable.deleteSelectedRows();
          displayInfoAutoTimeOut("Station " + vsStation + " Deleted", "Delete Result");
        }
        catch (DBException e2)
        {
          displayError("Failed to Delete station " + vsStation, "Delete Result");
        }
        catch (Exception e2)
        {
          displayError("Failed to update screen " + vsStation, "Delete Result");
        }
      }
    }
  }

  /**
   *  Action method to handle Modify button. Brings up screen to do the update.
   */
  @Override
  protected void modifyButtonPressed()
  {
    if (isSelectionValidForModify(false))
    {
      Object cObj = sktable.getCurrentRowDataField(StationData.STATIONNAME_NAME);
      UpdateStation updateStation = Factory.create(UpdateStation.class, "Modify Station");
      updateStation.setModify(cObj.toString());
      addSKDCInternalFrameModal(updateStation, buttonPanel,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e)
            {
              String prop = e.getPropertyName();
              if (prop.equals(FRAME_CHANGE))
              {
                refreshTable();
              }
            }
          });
    }
  }

  /**
   * Action method to handle View details button. Brings up screen to show the
   * load line items for this item master.
   */
  @Override
  protected void viewButtonPressed()
  {
    if (isSelectionValid("View", false))
    {
      Object cObj = sktable.getCurrentRowDataField(StationData.STATIONNAME_NAME);
      UpdateStation updateStation = Factory.create(UpdateStation.class, "View Station");
      updateStation.setView(cObj.toString());
      addSKDCInternalFrameModal(updateStation, buttonPanel,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e)
            {
              String prop = e.getPropertyName();
              if (prop.equals(FRAME_CHANGE))
              {
                refreshTable();
              }
            }
          });
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
    return StationListFrame.class;
  }
}
