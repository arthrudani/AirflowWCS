package com.daifukuamerica.wrxj.swingui.port;

import com.daifukuamerica.wrxj.dataserver.standard.StandardPortServer;
import com.daifukuamerica.wrxj.dbadapter.data.PortData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.NoSuchElementException;
import javax.swing.ListSelectionModel;
import javax.swing.event.InternalFrameEvent;

/**
 * A screen class for displaying a list of ports.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class PortListFrame extends SKDCListFrame
{
  StandardPortServer portServ = Factory.create(StandardPortServer.class);

  /**
   *  Create port list frame.
   */
  public PortListFrame()
  {
    super("Port");
    sktable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
   *  Method to filter by name. Refreshes display.
   */
  public void refreshTable()
  {
    try
    {
      refreshTable(portServ.getPortlist());
    }
    catch (DBException e)
    {
      e.printStackTrace(System.out);
      displayError("Database Error: " + e);
    }
    catch (NoSuchElementException e){}
  }

  /**
   *  Action method to handle Add button. Brings up screen to do the add.
   */
  @Override
  protected void addButtonPressed()
  {
    UpdatePort updatePort = Factory.create(UpdatePort.class, "Add Port");
    addSKDCInternalFrameModal(updatePort, buttonPanel,
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
      String vsPort = sktable.getCurrentRowDataField(PortData.PORTNAME_NAME).toString();
      String vsConfirmMessage = "Delete Port " + vsPort;
      if (displayYesNoPrompt(vsConfirmMessage, "Delete Confirmation"))
      {
        try
        {
          portServ.deletePort(vsPort);
          refreshTable();
          displayInfoAutoTimeOut("Port " + vsPort + " deleted");
        }
        catch (DBException e2)
        {
          displayError("Failed to delete port: " + vsPort);
          logger.logException(e2);
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
      String vsPort = sktable.getCurrentRowDataField(PortData.PORTNAME_NAME).toString();
      UpdatePort updatePort = Factory.create(UpdatePort.class, "Modify Port");
      updatePort.setModify(vsPort);
      addSKDCInternalFrameModal(updatePort, buttonPanel,
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
    return PortListFrame.class;
  }
}