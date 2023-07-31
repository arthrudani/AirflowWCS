package com.daifukuamerica.wrxj.swingui.user;

import com.daifukuamerica.wrxj.dataserver.standard.StandardUserServer;
import com.daifukuamerica.wrxj.dbadapter.data.EmployeeData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import javax.swing.ListSelectionModel;

/**
 * A screen class for displaying a list of logged in users.
 * 
 * @author reg
 * @version 1.0
 */
@SuppressWarnings("serial")
public class LoginListFrame extends SKDCListFrame
{
  protected StandardUserServer userServ = Factory.create(StandardUserServer.class);
  protected boolean searchbyrole = false;
  protected String searchString;
  protected int numselected;

  /**
   * Create user list frame.
   * 
   */
  public LoginListFrame()
  {
    super("Login");
    setTitle("Current Users");
    setSearchData("User", DBInfo.getFieldLength(EmployeeData.USERID_NAME));
    setSearchVisible(true);
    sktable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    setDisplaySearchCount(true, "Current User");
    addExtraButtons();
    setSearchByName("");
  }

  /**
   * Method to clean up as needed at closing.
   * 
   */
  @Override
  public void cleanUpOnClose()
  {
    userServ.cleanUp();
  }

  /**
   * Method to filter by role. Refreshes display.
   * 
   * @param s Role to search for.
   */
  public void setSearchByRole(String s)
  {
    searchbyrole = true;
    searchString = s;
    if (SKDCUserData.isSuperUser() || SKDCUserData.isAdministrator())
    {
      searchString = s;
    }
    else
    {
      setSearchByName(SKDCUserData.getLoginName());
      return;
    }
    try
    {
      refreshTable(userServ.getLoginListByRole(s));
    }
    catch(DBException e)
    {
      e.printStackTrace(System.out);
      displayError("Database Error: " + e);
    }
  }

  /**
   * Method to filter by name. Refreshes display.
   * 
   * @param s User to search for.
   */
  public void setSearchByName(String s)
  {
    searchbyrole = false;

    try
    {
      if (SKDCUserData.isSuperUser() || SKDCUserData.isAdministrator())
      {
        searchString = s;
        refreshTable(userServ.getLoginDataList(searchString, false));
      }
      else
      {
        searchString = SKDCUserData.getLoginName();
        refreshTable(userServ.getLoginDataList(searchString, true));
      }
    }
    catch(DBException e)
    {
      e.printStackTrace(System.out);
      displayError("Database Error: " + e);
    }
  }

  /**
   * Method to intialize screen components. This adds the extra buttons to the
   * screen.
   * 
   */
  private void addExtraButtons()
  {
    buttonPanel.setVisible(false);
    addButton.setVisible(false);
    modifyButton.setVisible(false);
    deleteButton.setVisible(false);
  }

  /**
   * Action method to handle search button.
   */
  @Override
  protected void searchButtonPressed()
  {
    setSearchByName(searchField.getText());
  }

  /**
   * Action method to handle refresh button.
   */
  @Override
  protected void refreshButtonPressed()
  {
    if(searchbyrole == true)
    {
      setSearchByRole(searchString);
    }
    else
    {
      setSearchByName(searchString);
    }
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
    popupMenu.remove("Delete");
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
    return LoginListFrame.class;
  }
}