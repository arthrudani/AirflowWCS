package com.daifukuamerica.wrxj.swingui.zone;

import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.data.ZoneData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ListSelectionModel;

/**
 * A screen class for displaying a list of zones.
 *
 * @author mike
 * @version 1.0
 * 
 * <BR>Copyright (c) 2006 by Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class ZoneListFrame extends SKDCListFrame
{
  StandardLocationServer mpLocServer = Factory.create(StandardLocationServer.class);

  /**
   *  Create zone list frame.
   */
  public ZoneListFrame()
  {
    super("Zone");
    sktable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setSearchData("Zone", DBInfo.getFieldLength(ZoneData.ZONE_NAME));
    refreshDataList();
  }

  /**
   *  Refreshes data list display.
   */
  protected void refreshDataList()
  {
    try
    {
      refreshTable(mpLocServer.getZones(getEnteredSearchText()));
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
    UpdateZone updateZone = Factory.create(UpdateZone.class, "Add Location Zone");
    addSKDCInternalFrameModal(updateZone, buttonPanel,
      new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
          String prop = e.getPropertyName();
          if (prop.equals(FRAME_CHANGE))
          {
            refreshDataList();
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
      Object cObj = sktable.getCurrentRowDataField(ZoneData.ZONE_NAME);
      try
      {
        String conf_mesg;
        String vsZone = cObj.toString();
        if (mpLocServer.isSafeToDeleteZone(vsZone))
        {
          conf_mesg = "Delete Zone " + vsZone + " ";
        }
        else
        {
          conf_mesg = "Zone " + vsZone + " is still in use.  Delete it (and all references) anyway";
        }
        
        if (displayYesNoPrompt(conf_mesg, "Delete Confirmation"))
        {
          mpLocServer.deleteZone(vsZone);
          displayInfoAutoTimeOut("Zone \"" + vsZone + "\" Deleted", "Delete Result");
        }
      }
      catch (DBException e2)
      {
        displayError("Failed to delete " + e2.getMessage(), "Delete Result");
      }
      refreshDataList();
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
      Object cObj = sktable.getCurrentRowDataField(ZoneData.ZONE_NAME);
      UpdateZone updateZone = Factory.create(UpdateZone.class, "Modify Location Zone");
      updateZone.setModify(cObj.toString());
      addSKDCInternalFrameModal(updateZone, buttonPanel,
        new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if (prop.equals(FRAME_CHANGE))
            {
               refreshDataList();
            }
        }
      });
    }
  }

  /**
   * Search button
   */
  @Override
  protected void searchButtonPressed()
  {
    refreshDataList();
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
    return ZoneListFrame.class;
  }
}