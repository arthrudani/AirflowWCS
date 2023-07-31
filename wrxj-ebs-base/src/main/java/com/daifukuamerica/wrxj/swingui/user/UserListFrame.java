package com.daifukuamerica.wrxj.swingui.user;
/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

import com.daifukuamerica.wrxj.dataserver.standard.StandardUserServer;
import com.daifukuamerica.wrxj.dbadapter.data.EmployeeData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;
import javax.swing.ListSelectionModel;

/**
 * A screen class for displaying a list of users.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class UserListFrame extends SKDCListFrame
{
  protected StandardUserServer userServ = Factory.create(StandardUserServer.class);
  protected boolean searchbyrole = false;
  protected String searchString;

  /**
   *  Create user list frame.
   */
  public UserListFrame()
  {
    super("Employee");
    setTitle("Users");
    setSearchData("User", DBInfo.getFieldLength(EmployeeData.USERID_NAME));
    setSearchVisible(true);
    sktable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    sktable.setDateDisplayFormat(SKDCConstants.DATE_FORMAT2);
    setDisplaySearchCount(true, "User");
    addExtraButtons();
    setSearchByName("");
  }

  /**
   *  Method to filter by role. Refreshes display.
   *
   *  @param s Role to search for.
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
      refreshTable(userServ.getEmployeeDataListByRole(s));
    }
    catch (DBException e)
    {
      e.printStackTrace(System.out);
      displayError("Database Error: " + e);
    }
  }

  /**
   *  Method to filter by name. Refreshes display.
   *
   *  @param s User to search for.
   */
  public void setSearchByName(String s)
  {
    searchbyrole = false;

    try
    {
      if(SKDCUserData.isSuperUser() || SKDCUserData.isAdministrator())
      {
        searchString = s; 
        refreshTable(userServ.getEmployeeDataList(searchString,false));     
      }
      else
      {
        searchString = SKDCUserData.getLoginName();
        refreshTable(userServ.getEmployeeDataList(searchString, true));
      }
    }
    catch (DBException e)
    {
      e.printStackTrace(System.out);
      displayError("Database Error: " + e);
    }
  }

  /**
   *  Method to intialize screen components. This adds the extra buttons
   *  to the screen.
   */
  private void addExtraButtons()
  {
    if(!SKDCUserData.isSuperUser() && !SKDCUserData.isAdministrator())
    {
      addButton.setVisible(false);
      deleteButton.setVisible(false);
    }
  }

  /**
   *  Action method to handle search button.
   */
  @Override
  protected void searchButtonPressed()
  {
    setSearchByName(searchField.getText());
  }

  /**
   *  Action method to handle refresh button.
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
   *  Action method to handle Add button. Brings up screen to do the add.
   */
  @Override
  protected void addButtonPressed()
  {
    UpdateUser updateUser = Factory.create(UpdateUser.class, "Add User");
    addSKDCInternalFrameModal(updateUser, buttonPanel,
      new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
          String prop = e.getPropertyName();
          if (prop.equals(FRAME_CHANGE))
          {
            EmployeeData vpEmpdata = (EmployeeData)e.getNewValue();
            sktable.appendRow(vpEmpdata);
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
    List <Map> vpData;   
  
    String[] vasUserIDs = sktable.getSelectedColumnData(EmployeeData.USERID_NAME);
    int vnNumSelected = vasUserIDs == null ? 0 : vasUserIDs.length;
    if (vnNumSelected > 0)
    {
      if (vnNumSelected > 1)
      {
        if (!displayYesNoPrompt(
            "Delete MULTIPLE (" + vnNumSelected + ") Users",
            "Delete Confirmation"))
        {
          return;
        }
      }
      else
      {
        if (!displayYesNoPrompt("Delete User \"" + vasUserIDs[0] + "\"",
            "Delete Confirmation"))
        {
          return;
        }
      }
    }
    else
    {
      displayInfoAutoTimeOut("No row selected to Delete", "Selection Error");
      return;
    }

    if (vasUserIDs != null)
    {
        for (int i = 0; i < vasUserIDs.length; i++)
        {
          try
          {
            if (!SKDCUserData.isSuperUser() && userServ.isEmployeeSuperUser(vasUserIDs[i]))
            {
              displayError("Not authorized to delete User " + vasUserIDs[i]);
              vnNumSelected--;
              continue;
            }
            if ((vasUserIDs[i].equals(SKDCConstants.USER_DAC_SUPERUSER)) || 
                (vasUserIDs[i].equals(SKDCConstants.USER_DAC_SUPPORT)) ||
                (vasUserIDs[i].equals(SKDCConstants.USER_ADMINISTRATOR)))
            {
              // Don't let them delete su, Administrator, and Daifuku users
              displayError("Not authorized to delete User " + vasUserIDs[i]);
              vnNumSelected--;
              continue;
            }
          }
          catch (DBException e3)
          {
            e3.printStackTrace(System.out);
            displayError("Error checking authorization");
            cleanUpOnClose();
            close();
          }
 
          try
          {
            vpData  = userServ.getLoginDataList(vasUserIDs[i], true);
          }
          catch (DBException e3)
          {
            e3.printStackTrace(System.out);
            displayError("Error geting Login data");
            continue;
          }
          if ( vpData.size() > 0)
          {   
             if (!displayYesNoPrompt("User may still be active on the system.  Delete anyway"))
             {
               vnNumSelected--;
               continue ;
             }
           }  
          try
          {
            userServ.deleteEmployee(vasUserIDs[i]);
          }
          catch (DBException e2)
          {
             displayError("Failed to delete User " + vasUserIDs[i], "Delete Result");
             logger.logException(e2);
          }
        }
        refreshButtonPressed();
        displayInfoAutoTimeOut(vnNumSelected + " Users deleted", "Delete Result");
    }
    else
    {
      displayInfoAutoTimeOut("No row selected", "Selection Error");
    }
  }

  /**
   *  Action method to handle Modify button. Brings up screen to do the update.
   */
  @Override
  protected void modifyButtonPressed()
  {
    final String[] vasUserIDs = sktable.getSelectedColumnData(EmployeeData.USERID_NAME);
    if (vasUserIDs == null || vasUserIDs.length == 0)
    {
      displayInfoAutoTimeOut("No row selected to Modify", "Selection Error");
      return;
    }
    else
    {
      UpdateUser updateUser = Factory.create(UpdateUser.class, "Modify User");
      if (vasUserIDs.length > 1)
      {
        updateUser.setModify(vasUserIDs);
      }
      else
      {
        updateUser.setModify(vasUserIDs[0].toString());
      }
      addSKDCInternalFrameModal(updateUser, buttonPanel,
        new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if (prop.equals(FRAME_CHANGE))
            {
              if(vasUserIDs.length > 1)
              {
                refreshButtonPressed();
              }
              else
              {
                EmployeeData vpEmpdata = (EmployeeData)e.getNewValue();
                sktable.modifySelectedRow(vpEmpdata);
              }
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
    return UserListFrame.class;
  }
}