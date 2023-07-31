package com.daifukuamerica.wrxj.swingui.role;

import com.daifukuamerica.wrxj.dataserver.standard.StandardUserServer;
import com.daifukuamerica.wrxj.dbadapter.data.RoleData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.swing.table.DacTableMouseListener;
import com.daifukuamerica.wrxj.swingui.user.UserListFrame;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.InternalFrameEvent;

/**
 * A screen class for displaying a list of roles.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class RoleListFrame extends SKDCListFrame
{
  private JPanel           btnpanel;
  private JPanel           ipanel;
  StandardUserServer userServ = Factory.create(StandardUserServer.class);
  SKDCButton mpBtnUsers;
  SKDCButton mpBtnAllUsers;
  private AddRoleFrame    addFrame;
  private ModifyRoleFrame modifyFrame;
  private RoleOptionListFrame   roleOptionListFrame;
  RoleData rold = Factory.create(RoleData.class);
  private static String SHOWALLUSERS_BTN = "SHOWALLUSERS_BTN";
  UserListFrame uf = null;
  private boolean superuser;


 /**
  *  Create role list frame.
  *
  */
  public RoleListFrame()
  {
    super("Role");
    sktable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setViewButtonVisible(true);
    userData = new SKDCUserData();
    superuser = false;
    
    if( SKDCUserData.isSuperUser()) 
    {
      superuser = true;
    }
    refresh();
  }

 /**
  *  Method to clean up as needed at closing.
  *
  */
  @Override
  public void cleanUpOnClose()
  {
    if(userServ != null)
    {
      userServ.cleanUp();
      userServ = null;
    }
  }

  /**
   *  Method to filter by name. Refreshes display.
   */
  protected void refresh()
  {
    try
    {
      if( superuser ) 

      {
        refreshTable(userServ.getRoleDataList(true, true));
      }
      else
      {
        if(SKDCUserData.isAdministrator())
        {
          refreshTable(userServ.getRoleDataList(true, false));
        }
        else
        {
          refreshTable(userServ.getRoleDataList(SKDCUserData.getRole(), false, false));
        }
      }
      
    }
    catch (DBException e)
    {
      e.printStackTrace(System.out);
      displayError("Database Error: " + e);
    }
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
    sktable.resizeColumns();
    if (ePerms.iModifyAllowed)
    {
      viewButton.setText("Modify Menu Items");
      viewButton.setToolTipText("Modify Menu Items");
      viewButton.setMnemonic('I');
    }
  }

  /**
   *  Method to intialize screen components. This adds the extra buttons
   *  to the screen.
   */
  private void addExtraButtons()
  {
    mpBtnUsers = new SKDCButton("View Role Users", "View Role Users", 'R');
    mpBtnAllUsers = new SKDCButton("View All Users", "View All Users", 'U');

    viewButton.setText("View Menu Items");
    viewButton.setToolTipText("View Menu Items");
    viewButton.setMnemonic('I');

    if(!SKDCUserData.isSuperUser() && !SKDCUserData.isAdministrator())
    {
      addButton.setVisible(false);
      deleteButton.setVisible(false);
    }    
    if(SKDCUserData.isSuperUser() || SKDCUserData.isAdministrator())
    {
      getButtonPanel().add(mpBtnUsers);
      getButtonPanel().add(mpBtnAllUsers);
    }
  }

  /**
   * 
   */
  @Override
  protected void addActionListeners()
  {
    super.addActionListeners();
    
    addExtraButtons();
    
    mpBtnUsers.addEvent(SHOWDETAIL_BTN, new buttonListener());
    mpBtnAllUsers.addEvent(SHOWALLUSERS_BTN, new buttonListener());
  }

  /**
   *  Button Listener class.
   */
  private class buttonListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      String which_button = e.getActionCommand();

      if (which_button.equals(SHOWDETAIL_BTN))
      {
        buttonUsersPressed();
      }
      else if (which_button.equals(SHOWALLUSERS_BTN))
      {
        buttonAllUsersPressed();
      }
    }
  }

  /**
   *  Action method to handle Add button. Brings up screen to do the add.
   */
  @Override
  protected void addButtonPressed()
  {
    addFrame = Factory.create(AddRoleFrame.class, userServ, ePerms);
    addFrame.setCategoryAndOption(getCategory(), getOption());
    addSKDCInternalFrameModal(addFrame, btnpanel, new RoleAddFrameHandler());
  }

  /**
   *  Action method to handle Delete button.
   */
  @Override
  protected void deleteButtonPressed()
  {
    if (sktable.getSelectedRowCount() < 1)
    {
      displayInfoAutoTimeOut("No row selected to Delete", "Selection Error");
      return;
    }
    
    Object cObj = sktable.getCurrentRowDataField(RoleData.ROLE_NAME);
    if (cObj == null)
    {
      displayError("Error Identifying Role to Delete", "Selection Error");
      return;
    }
    
    String vsRole = cObj.toString();
    if (vsRole.equals(SKDCConstants.ROLE_MASTER) ||
        vsRole.equals(SKDCConstants.ROLE_ADMINISTRATOR) ||
        vsRole.equals(SKDCConstants.ROLE_DAC_SUPERROLE))
    {
      displayError("Cannot Delete " + vsRole + " Role", "Delete Error");
      return;
    }
    
    if (!displayYesNoPrompt("Ready to Delete Role"))
    {
      return;
    }
    
    try
    {
      if (userServ.roleEmployeeExists(vsRole))
      {
        displayError("Employee(s) exist for this Role - Cannot Delete",
            "Delete Error");
        return;
      }
      userServ.deleteRole(vsRole);
      sktable.deleteSelectedRows();    // Update the display.
    }
    catch(DBException dbe)
    {
      displayError("Error Deleting Role", "Delete Error");
      return;
    }
  }

  /**
   *  Action method to handle View Users button.
   */
  void buttonUsersPressed()
  {

    if (sktable.getSelectedRowCount() < 1)
    {
      displayInfoAutoTimeOut("No row selected to View", "Selection Error");
      return;
    }
    Object cObj = sktable.getCurrentRowDataField(RoleData.ROLE_NAME);
    if (cObj != null)
    {
      uf = (UserListFrame)getSKDCInternalFrame("UserListFrame");

      if (uf == null)
      {
        uf = new UserListFrame();
        uf.setCategoryAndOption(getCategory(), "Users");
//        setAllowDuplicateScreens(true);
        addSKDCInternalFrame(uf);
      }
      else
      {
        if (uf.isIcon())
        {
          try
          {
            uf.setIcon(false);
          }
          catch (PropertyVetoException pve) {}
        }
      }
      uf.setSearchByRole(cObj.toString());
    }
  }

 /**
  *  Action method to handle View All Users button.
  *
  */
  void buttonAllUsersPressed()
  {

    uf = (UserListFrame)getSKDCInternalFrame("UserListFrame");

    if (uf == null)
    {
      uf = new UserListFrame();
      uf.setCategoryAndOption(getCategory(), "Users");
      addSKDCInternalFrame(uf);
    }
    else
    {
      if (uf.isIcon())
      {
        try
        {
          uf.setIcon(false);
        }
        catch (PropertyVetoException pve) {}
      }
    }
    uf.setSearchByRole("");
  }

  /**
   *  Action method to handle View Options button.
   */
  @Override
  protected void viewButtonPressed()
  {
    if (!isSelectionValid("view", false))
    {
      return;
    }
    
    RoleData roleData = Factory.create(RoleData.class);
    roleData.dataToSKDCData(sktable.getRowData(sktable.getSelectedRow()));
    
    if (roleData.getRole().equals(SKDCConstants.ROLE_MASTER))
    {
      if (!superuser)
      {
        displayError("Cannot Modify " + SKDCConstants.ROLE_MASTER
            + " Role Options", "Modify Error");
        return;
      }
    }
    else if (roleData.getRole().equals(SKDCConstants.ROLE_ADMINISTRATOR))
    {
      if (!superuser && !SKDCUserData.isAdministrator())
      {
        displayError("Cannot Modify " + SKDCConstants.ROLE_ADMINISTRATOR
            + " Role", "Modify Error");
        return;
      }
    }
    else if(roleData.getRole().equals(SKDCConstants.ROLE_DAC_SUPERROLE))
    {
      if (!superuser)
      {
        displayError("Cannot Modify " + SKDCConstants.ROLE_DAC_SUPERROLE
            + " Role", "Modify Error");
        return;
      }
    }
    else if(!roleData.getRole().equals(SKDCUserData.getRole()) && superuser == false &&
             !SKDCUserData.isAdministrator()) 
    {
      displayError("Cannot modify Role Options other than your own", "Modify Error");
      return;
    }
    try
    {
      roleOptionListFrame = Factory.create(RoleOptionListFrame.class, 
          "Menu Item List", roleData.getRole(), ePerms);
      roleOptionListFrame.setCategoryAndOption(getCategory(), getOption());
      roleOptionListFrame.setTableData(roleData.getRole());
      addSKDCInternalFrameModal(roleOptionListFrame);
    }
    catch(Exception e)
    {
      e.printStackTrace(System.out);
      roleOptionListFrame.close();
    }
  }

  /**
   *  Action method to handle Modify button. Brings up screen to do the update.
   */
  @Override
  protected void modifyButtonPressed()
  {                                    // Get data on current row as
                                       // OrderHeader Data structure.
    int totalSelected;

    if ((totalSelected = sktable.getSelectedRowCount()) == 0)
    {
      displayInfoAutoTimeOut("No row selected to modify", "Selection Error");
      return;
    }
    else if (totalSelected > 1)
    {
      displayInfoAutoTimeOut("Only one row can be selected to modify", "Modify Error");
      return;
    }

    rold.dataToSKDCData(sktable.getSelectedRowData());
    if(rold.getRole().equals(SKDCConstants.ROLE_MASTER))
    {
      if (!superuser)
      {
        displayError("Cannot Modify " + SKDCConstants.ROLE_MASTER
            + " Role", "Modify Error");
        return;
      }
    }
    else if(rold.getRole().equals(SKDCConstants.ROLE_ADMINISTRATOR))
    {
      if (!superuser)
      {
        displayError("Cannot Modify " + SKDCConstants.ROLE_ADMINISTRATOR
            + " Role", "Modify Error");
        return;
      }
    }
    else if(rold.getRole().equals(SKDCConstants.ROLE_DAC_SUPERROLE))
    {
      if (!superuser)
      {
        displayError("Cannot Modify " + SKDCConstants.ROLE_DAC_SUPERROLE
            + " Role", "Modify Error");
        return;
      }
    }
    else if(!rold.getRole().equals(SKDCUserData.getRole()) && superuser == false &&
             !SKDCUserData.isAdministrator()) 
    {
      displayError("Cannot modify Role other than your own", "Modify Error");
      return;
    }
    modifyFrame = Factory.create(ModifyRoleFrame.class, userServ);
    modifyFrame.setCurrentData(rold);
    modifyFrame.setCategoryAndOption(getCategory(), getOption());

    addSKDCInternalFrameModal(modifyFrame, new JPanel[] { ipanel, btnpanel },
                              new RoleModifyFrameHandler());
    return;

  }
 /**
   *  Handles close frame notification.
   */
  @Override
  protected void closeButtonPressed()
  {
    close();
  }
  /**
   *   Property Change event listener for Add frame.
   */
  private class RoleAddFrameHandler implements PropertyChangeListener
  {
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(FRAME_CHANGE))
      {
        rold = (RoleData)pcevt.getNewValue();
        // DEBUG: displayInfo(rold.toString());
        sktable.appendRow(rold);
      }
    }
  }

  /**
   *   Property Change event listener for Modify frame.
   */
  private class RoleModifyFrameHandler implements PropertyChangeListener
  {
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(FRAME_CHANGE))
      {
        rold = (RoleData)pcevt.getNewValue();
//System.out.println("roledata: " + rold.tostring());
        sktable.modifySelectedRow(rold);
      }
    }
  }

  /**
   * Mouse listener for the table 
   */
  @Override
  protected void setTableMouseListener()
  {
    sktable.addMouseListener(new DacTableMouseListener(sktable)
    {
     /**
      *  Defines popup menu items for <code>SKDCTable</code>, and adds listeners
      *  to them.
      */
      @Override
      public SKDCPopupMenu definePopup()
      {
        if (ePerms == null)
        {
          ePerms = getPermissions();
        }
        
        popupMenu.add("Add", ADD_BTN, getDefaultListener());
        popupMenu.add("Modify", MODIFY_BTN, getDefaultListener());
        popupMenu.add("Delete", true, DELETE_BTN, getDefaultListener());
        
        if (ePerms.iModifyAllowed)
          popupMenu.add("Modify Menu Items", VIEW_BTN, getDefaultListener());
        else
          popupMenu.add("View Menu Items", VIEW_BTN, getDefaultListener());
        popupMenu.add("View Role Users", SHOWDETAIL_BTN, new buttonListener());
        popupMenu.add("View All Users", SHOWALLUSERS_BTN, new buttonListener());

        if (ePerms != null)
        {
          popupMenu.setAuthorization("Add", ePerms.iAddAllowed);
          popupMenu.setAuthorization("Modify", ePerms.iModifyAllowed);
          popupMenu.setAuthorization("Delete", ePerms.iDeleteAllowed);
        }

        return(popupMenu);
      }
      /**
       *  View the menu items
       */
      @Override
      public void displayDetail()
      {
        viewButtonPressed();
      }
    });
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
    return RoleListFrame.class;
  }
}