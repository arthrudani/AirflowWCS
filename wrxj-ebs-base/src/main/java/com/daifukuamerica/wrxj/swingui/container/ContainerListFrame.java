package com.daifukuamerica.wrxj.swingui.container;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dbadapter.data.ContainerTypeData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ListSelectionModel;

/**
 * A screen class for displaying a list of containers.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class ContainerListFrame extends SKDCListFrame
{
  StandardInventoryServer invtServ = Factory.create(StandardInventoryServer.class);

  /**
   *  Create container list frame.
   */
  public ContainerListFrame()
  {
    super("Container");
    sktable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    refreshTable();
  }

  /**
   *  Method to filter by name. Refreshes display.
   */
  public void refreshTable()
  {
    try
    {
      refreshTable(invtServ.getContainerDataList());
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
    UpdateContainer updateContainer = Factory.create(UpdateContainer.class, "Add Container");
    addSKDCInternalFrameModal(updateContainer, buttonPanel,
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
      String vsContainer = sktable.getCurrentRowDataField(
          ContainerTypeData.CONTAINERTYPE_NAME).toString();
      if (vsContainer != null)
      {
        try
        {
          String conf_mesg = "Delete Container " + vsContainer;
          if (displayYesNoPrompt(conf_mesg, "Delete Confirmation"))
          {
            invtServ.deleteContainer(vsContainer);
            displayInfoAutoTimeOut("Container \"" + vsContainer + "\" Deleted", 
                "Delete Result");
          }
        }
        catch (DBException e2)
        {
          displayError("Failed to delete " + e2.getMessage(), "Delete Result");
        }
        refreshTable();
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
      String vsContainer = sktable.getCurrentRowDataField(
          ContainerTypeData.CONTAINERTYPE_NAME).toString();
      UpdateContainer updateContainer = Factory.create(UpdateContainer.class,
          "Modify Container");
      updateContainer.setModify(vsContainer);
      addSKDCInternalFrameModal(updateContainer, buttonPanel,
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
    return ContainerListFrame.class;
  }
}