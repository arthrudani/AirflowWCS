package com.daifukuamerica.wrxj.swingui.route;

import com.daifukuamerica.wrxj.dataserver.standard.StandardRouteServer;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.RouteData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;


/**
 * A screen class for displaying a list of routes.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class RouteListFrame extends SKDCListFrame
{
  StandardRouteServer routeServer = Factory.create(StandardRouteServer.class);

  /**
   *  Create route list frame.
   */
  public RouteListFrame()
  {
    super("Route");
    setSearchData("Route Name", DBInfo.getFieldLength(RouteData.ROUTEID_NAME));
    setDetailSearchVisible(false);
    modifyButton.setEnabled(true);
    modifyButton.setVisible(true);
    setDisplaySearchCount(true, "route entry", "route entries");
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

    refreshTable(searchField.getText());
  }

  /**
   *  Method to clean up as needed at closing.
   *
   */
  @Override
  public void cleanUpOnClose()
  {
    routeServer.cleanUp();
  }

  /**
   *  Action method to handle search button.
   */
  @Override
  protected void searchButtonPressed()
  {
    refreshTable(searchField.getText());
  }

  /**
   *  Method to filter by name. Refreshes display.
   *
   *  @param s Load to search for.
   */
  protected void refreshTable(String s)
  {
    try
    {
      RouteData rtSearchData = Factory.create(RouteData.class);
      if (s.trim().length() > 0)
      {
        rtSearchData.setKey(RouteData.ROUTEID_NAME, s, KeyObject.LIKE);
      }
      rtSearchData.addOrderByColumn(RouteData.ROUTEID_NAME);
      rtSearchData.addOrderByColumn(RouteData.FROMID_NAME);
      rtSearchData.addOrderByColumn(RouteData.DESTID_NAME);
      refreshTable(routeServer.getRouteData(rtSearchData));
    }
    catch (DBException e)
    {
      e.printStackTrace(System.out);
      displayError("Database Error: " + e);
    }
  }

  /**
   *  Action method to handle Add button. Brings up screen to do the add.
   */
  @Override
  protected void addButtonPressed()
  {
    UpdateRoute updateRoute = Factory.create(UpdateRoute.class, "Add");
    addSKDCInternalFrameModal(updateRoute, new JPanel[] {buttonPanel, searchPanel},
      new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
          String prop = e.getPropertyName();
          if (prop.equals(FRAME_CHANGE))
          {
            refreshTable(searchField.getText());
          }
      }
    });
  }

  /**
   *  Action method to handle Modify button. Brings up screen to do the update.
   */
  @Override
  protected void modifyButtonPressed()
  {
    /*
     * Make sure only one row is selected
     */
    if (isSelectionValidForModify(false))
    {
      /*
       * Find out what we are going to modify
       */
      RouteData vpRouteData = Factory.create(RouteData.class);
      vpRouteData.dataToSKDCData(sktable.getSelectedRowData());
      String vsRouteID = vpRouteData.getRouteID();
      String vsFrom    = vpRouteData.getFromID();
      String vsTo      = vpRouteData.getDestID();

      /*
       * Do the update
       */
      UpdateRoute updateRoute = Factory.create(UpdateRoute.class, "Modify");
      updateRoute.setModify(vsRouteID, vsFrom, vsTo);

      addSKDCInternalFrameModal(updateRoute, new JPanel[] {buttonPanel, searchPanel},
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
              String prop = e.getPropertyName();
              if (prop.equals(FRAME_CHANGE))
              {
                refreshTable(searchField.getText());
              }
            }
          });
    }
  }

  /**
   *  Action method to handle Delete button.
   */
  @Override
  protected void deleteButtonPressed()
  {
    int totalSelected;
    if ((totalSelected = sktable.getSelectedRowCount()) == 0)
    {
      displayInfoAutoTimeOut("No row selected", "Selection Error");
      return;
    }

    if (displayYesNoPrompt("Delete selected routes", "Delete Confirmation"))
    {
      int delCount = 0;
      int[] deleteIndices = sktable.getSelectedRows();
      
      List<RouteData> vpDeletionList = DBHelper.convertData(
                           sktable.getSelectedRowDataArray(), RouteData.class);
      int vnRow = 0;
      for(RouteData vpRouteData : vpDeletionList)
      {
        String vsRouteID = vpRouteData.getRouteID();
        String vsFrom    = vpRouteData.getFromID();
        String vsTo      = vpRouteData.getDestID();
        try
        {
          routeServer.deleteRoute(vsRouteID, vsFrom, vsTo);
          delCount++;
        }
        catch(DBException exc)
        {
          String vsRouteDesc = routeServer.describeRouteSegment(vsRouteID, vsFrom, vsTo); 
          displayError("Error deleting route " + vsRouteDesc + exc.getMessage(), "Delete Error");
                                       // De-Select the troubling row!
          sktable.deselectRow(deleteIndices[vnRow]);
        }
        vnRow++;
      }

      if ((delCount > 1) && (delCount != totalSelected))
      {
        displayInfoAutoTimeOut("Deleted " +  delCount + " of " + totalSelected +
                               " selected Routes", "Delete Result");
      }
      refreshTable(searchField.getText());
    }
  }

  /**
   * Mouse listener for the table 
   */
  @Override
  protected void setTableMouseListener()
  {
    super.setTableMouseListener();
    popupMenu.remove("Modify");
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
    return RouteListFrame.class;
  }
}
