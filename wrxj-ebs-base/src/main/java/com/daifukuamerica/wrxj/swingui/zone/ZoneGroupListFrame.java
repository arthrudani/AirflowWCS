package com.daifukuamerica.wrxj.swingui.zone;

import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.data.ZoneGroup;
import com.daifukuamerica.wrxj.dbadapter.data.ZoneGroupData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ListSelectionModel;

/**
 * <B>Description:</B> Screen class for displaying a list of zone groups<BR>
 *
 * @author       mandrus<BR>
 * @version      1.0
 * 
 * <BR>Copyright (c) 2006 by Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class ZoneGroupListFrame extends SKDCListFrame
{
  StandardLocationServer mpLocServer = Factory.create(StandardLocationServer.class);

  /**
   * Create the list frame
   */
  public ZoneGroupListFrame()
  {
    super("ZoneGroup");
    sktable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setDisplaySearchCount(true, "zone group");
    setSearchData("Recommended Zone", DBInfo.getFieldLength(ZoneGroupData.ZONEGROUP_NAME));
    refreshDataList();
  }

  /**
   *  Refreshes data list display.
   */
  protected void refreshDataList()
  {
    try
    {
      refreshTable(mpLocServer.getZoneGroupList(getEnteredSearchText().trim()));
    }
    catch (DBException e)
    {
      e.printStackTrace(System.out);
      displayError("Database Error: " + e);
    }
  }

  /**
   *  Action method to handle Add button. Brings up screen to do the add.
   *
   */
  @Override
  protected void addButtonPressed()
  {
    UpdateZoneGroup vpUpdateZoneGroup = Factory.create(UpdateZoneGroup.class, 
        "Add Recommended Zone");
    addSKDCInternalFrameModal(vpUpdateZoneGroup, buttonPanel,
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
      Object cObj = sktable.getCurrentRowDataField(ZoneGroupData.ZONEGROUP_NAME);
      String vsZoneGroup = cObj.toString();
      int    vnPriority  = (Integer)sktable.getCurrentRowDataField(ZoneGroupData.PRIORITY_NAME);

      try
      {
        String conf_mesg = "Delete Zone " + cObj.toString() + " ";
        if (displayYesNoPrompt(conf_mesg, "Delete Confirmation"))
        {
          mpLocServer.deleteZoneGroupMember(vsZoneGroup, vnPriority);
          displayInfoAutoTimeOut(
              ZoneGroup.describeZoneGroupMember(vsZoneGroup, vnPriority) + 
              " Deleted", "Delete Result");
        }
      }
      catch (DBException e2)
      {
        displayError("Failed to delete " + e2.getMessage(), "Delete Result");
        logger.logException(e2);
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
      Object cObj = sktable.getCurrentRowDataField(ZoneGroupData.ZONEGROUP_NAME);
      String vsZoneGroup = cObj.toString();
      int    vnPriority  = (Integer)sktable.getCurrentRowDataField(ZoneGroupData.PRIORITY_NAME);

      UpdateZoneGroup vpUpdateZoneGroup = Factory.create(UpdateZoneGroup.class, 
          "Modify Recommended Zone");
      vpUpdateZoneGroup.setModify(vsZoneGroup, vnPriority);
      addSKDCInternalFrameModal(vpUpdateZoneGroup, buttonPanel,
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
    return ZoneGroupListFrame.class;
  }
}