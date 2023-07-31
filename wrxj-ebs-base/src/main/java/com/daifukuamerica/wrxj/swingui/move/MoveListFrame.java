package com.daifukuamerica.wrxj.swingui.move;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDeallocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardMoveServer;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;

/**
 * A screen class for displaying a list of move.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class MoveListFrame extends SKDCListFrame
{
  StandardDeallocationServer mpDeallocServer = Factory.create(StandardDeallocationServer.class);
  StandardMoveServer mpMoveServer = Factory.create(StandardMoveServer.class);
  
  protected KeyObject[] mapFilterData = null;

  /**
   *  Create move list frame.
   */
  public MoveListFrame()
  {
    super("Move");
    userData = new SKDCUserData();
    ePerms = userData.getOptionPermissionsByClass(getClass());
    setCategoryAndOption(ePerms.getCategory(), ePerms.getOption());
    setSearchData("Load", DBInfo.getFieldLength(MoveData.LOADID_NAME));
    setDetailSearchVisible(true);
  }

  public MoveListFrame(KeyObject[] iapSearchKeys)
  {
    this();
    setFilter(iapSearchKeys);
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
    if (ePerms == null)
      return;
    popupMenu.setAuthorization("Delete Move", ePerms.iDeleteAllowed);
    addButton.setEnabled(false);
    modifyButton.setEnabled(false);
    deleteButton.setEnabled(ePerms.iDeleteAllowed);
    addButton.setVisible(false);
    modifyButton.setVisible(false);
    deleteButton.setVisible(ePerms.iDeleteAllowed);

    if (mapFilterData != null)
    {
      refreshTable();
    }
    else
    {
      searchButtonPressed();
    }
  }

  /**
   *  Method to clean up as needed at closing.
   */
  @Override
  public void cleanUpOnClose()
  {
    mpMoveServer.cleanUp();
    mpMoveServer = null;
  }

  /**
   *  Method to set the search criteria filter.
   *
   *  @param iapSearchKeys KeyObject[] containing criteria to use in search.
   */
  public void setFilter(KeyObject[] iapSearchKeys)
  {
    mapFilterData = iapSearchKeys;
  }

  /**
   * Action method to handle search button.
   */
  @Override
  protected void searchButtonPressed()
  {
    setLoadFilter(getEnteredSearchText());
    refreshTable();
  }

  /**
   * Method to set the search criteria filter.
   * 
   * @param isLoadID criteria to use in search.
   */
  public void setLoadFilter(String isLoadID)
  {
    searchField.setText(isLoadID);
    MoveData vpMoveData = Factory.create(MoveData.class);
    if (isLoadID.length() > 0)
    {
      KeyObject vpMoveKeyObject = new KeyObject(MoveData.LOADID_NAME, isLoadID);
      vpMoveData.addKeyObject(vpMoveKeyObject);
    }
    setFilter(vpMoveData.getKeyArray());
  }

  /**
   *  Method to filter by extended search. Refreshes display.
   */
  public void refreshTable()
  {
    try
    {
      refreshTable(mpMoveServer.getMoveDataList(mapFilterData));
    }
    catch (DBException e)
    {
      e.printStackTrace(System.out);
      displayError("Database Error: " + e);
    }
  }

  /**
   * Action method to handle Delete button.
   */
  @Override
  protected void deleteButtonPressed()
  {
    if (sktable.getSelectedRowCount() == 0)
    {
      displayInfoAutoTimeOut("No row selected", "Selection Error");
      return;
    }
     
    boolean vzDealloc = displayYesNoPrompt(
        "Do you want to de-allocate all selected Moves\n"
            + "(recommended instead of deleting)", "De-Allocate Confirmation");
    
    String vsAction = (vzDealloc ? "de-allocate" : "delete");
    if (!displayYesNoPrompt("Are you sure you want to " + vsAction
        + " the selected move(s)", "Delete Confirmation"))
    {
      return;
    }
    
    MoveData vpMoveData = Factory.create(MoveData.class);
    
    // Get list of selected moves and delete/de-allocate them
    String[] vasMoveIDs = sktable.getSelectedColumnData(MoveData.MOVEID_NAME);
    for (int i = 0; i < vasMoveIDs.length; i++)
    {
      logger.logError("User: " + SKDCUserData.getLoginName() + " at "
          + SKDCUserData.getMachineName() + " " + vsAction + "d move.");
      Integer tmpint = Integer.valueOf(vasMoveIDs[i]);
      try
      {
        if (vzDealloc == true)
        {
          mpDeallocServer.deallocateOneMove(tmpint);
        }
        else
        {
          vpMoveData.clear();
          vpMoveData.setKey(MoveData.MOVEID_NAME, Integer.valueOf(tmpint));
          mpMoveServer.deleteMove(vpMoveData);
        }
      }
      catch (DBException dbe)
      {
        displayError("Database Error: " + dbe);
      }
    }
    refreshTable(); // Update the display.
  }
  
  /**
   * Action method to handle Detailed Search button. Brings up form with
   * extended search criteria, gets criteria the operator entered, then refreshs
   * list screen.
   */
  @Override
  protected void detailedSearchButtonPressed()
  {
    final MoveDetailedSearchFrame searchMove = Factory.create(MoveDetailedSearchFrame.class);
    searchField.setText("");
    addSKDCInternalFrameModal(searchMove, new JPanel[] {buttonPanel, searchPanel},
      new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
          String prop = e.getPropertyName();
          if (prop.equals(FRAME_CHANGE))
          {
          // get the search criteria
            setFilter(searchMove.getSearchData());
            try
            {
              searchMove.setClosed(true);
            }
            catch (PropertyVetoException pve) {}
            refreshTable();
          }
      }
    });
  }

  /**
   * Mouse listener for the table 
   */
  @Override
  protected void setTableMouseListener()
  {
    super.setTableMouseListener();
    popupMenu.remove("Add");
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
    return MoveListFrame.class;
  }
}