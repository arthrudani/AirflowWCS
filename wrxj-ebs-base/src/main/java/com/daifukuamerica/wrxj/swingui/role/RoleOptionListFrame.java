package com.daifukuamerica.wrxj.swingui.role;

import com.daifukuamerica.wrxj.dataserver.standard.StandardUserServer;
import com.daifukuamerica.wrxj.dbadapter.data.RoleOptionData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCHeaderLabel;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import com.daifukuamerica.wrxj.swing.table.DacModel;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.SKDCRadioButton;
import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swing.table.DacTable;
import com.daifukuamerica.wrxj.swing.table.DacTableMouseListener;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A screen class for displaying a list of role options.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class RoleOptionListFrame extends SKDCListFrame
{
  private String            RADIO_VIEW_Y   = "RVIEW_Y";
  private String            RADIO_TOOL_Y   = "RTOOL_Y";
  private String            RADIO_ADD_Y    = "RADD_Y";
  private String            RADIO_MODIFY_Y = "RMODIFY_Y";
  private String            RADIO_DELETE_Y = "RDELETE_Y";
  private String            RADIO_VIEW_N   = "RVIEW_N";
  private String            RADIO_TOOL_N   = "RTOOL_N";
  private String            RADIO_ADD_N    = "RADD_N";
  private String            RADIO_MODIFY_N = "RMODIFY_N";
  private String            RADIO_DELETE_N = "RDELETE_N";
  private final String      MODIFY_SCREEN_BTN = "ModifyScreen";
  private int Xaxis = BoxLayout.X_AXIS;
  StandardUserServer userServ;
  String defaultTitle = "Role Menu Items";
  private SKDCButton           mpBtnModifyMenu;
  private SKDCButton           mpBtnModifyScreen;
  private SKDCButton           mpBtnAdd;
  private SKDCButton           mpBtnDelete;
  private SKDCButton           mpBtnCopyOptions;
  private RoleButtonListener   mpButtonListener = new RoleButtonListener();
  private ButtonGroup          mpBtnGroup = new ButtonGroup();
  private SKDCRadioButton      mpAllowViewRoleBtnYes;
  private SKDCRadioButton      mpAllowViewRoleBtnNo;
  private ButtonGroup          mpToolBtnGroup = new ButtonGroup();
  private SKDCRadioButton      mpToolbarRoleBtnYes;
  private SKDCRadioButton      mpToolbarRoleBtnNo;
  private ButtonGroup          mpAddBtnGroup = new ButtonGroup();
  private SKDCRadioButton      mpAllowAddRoleBtnYes;
  private SKDCRadioButton      allowAddRBtnNo;
  private ButtonGroup          mpModifyBtnGroup = new ButtonGroup();
  private SKDCRadioButton      allowModifyRBtnYes;
  private SKDCRadioButton      allowModifyRBtnNo;
  private ButtonGroup          mpDeleteBtnGroup = new ButtonGroup();
  private SKDCRadioButton      allowDeleteRBtnYes;
  private SKDCRadioButton      allowDeleteRBtnNo;
  private SKDCComboBox         mpRoleComboBox;
  private SKDCLabel            lblCopyOpt;
  private SKDCLabel            lblYesNo = new SKDCLabel(" Yes  No");
  private String msRole;
  private boolean superuser;
  private boolean administrator;
  private GridBagConstraints gbconst;
  private boolean          canModify    = false;
  private JCheckBox mpCopyPermissions;

  /**
   *  Create role option list frame.
   */
  public RoleOptionListFrame(String isKeyName, String isRole,
      SKDCScreenPermissions ePrms)
  {
    super("Role Menu Items");
    msRole = isRole;
    superuser = false;
    administrator = false;

    superuser = SKDCUserData.isSuperUser();
    administrator = SKDCUserData.isAdministrator();

    userServ = Factory.create(StandardUserServer.class);
    ePerms = ePrms;
    canModify = ePerms.iModifyAllowed;
    setTitle(defaultTitle);
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
    setPermissions();
    sktable.resizeColumns();
    pack();
    setSize(new Dimension(800,600));
  }

  /**
   *  Method to clean up as needed at closing.
   */
  @Override
  public void cleanUpOnClose()
  {
    // The user server is passed into us so don't shut it down, otherwise
    // other spots that use it will not be able to.
    if(userServ != null)
    {
      userServ.cleanUp();
    }
  }

  /**
   *  Fills in the Role Option List table frame.
   */
  public void setTableData(String isRole) throws Exception
  {
    msRole = isRole;            // Role this Frame is concerned with.
                                       // Set up the keys for retrieving the
                                       // role Options.
    try
    {
                                       // Get the Role Option Data for this Role
                                       // from the server.
      dataList = userServ.getRoleOptionsList(isRole);
      if (dataList == null)
      {
        throw new DBException("No Rows found!");
      }
    }
    catch(DBException e)
    {
      displayError(e.getMessage(), "DB Error");
      throw new Exception("Table build error");
    }

    sktable = new DacTable(new DacModel(dataList, "RoleOption"));
    sktable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    sktable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
      {
        public void valueChanged(ListSelectionEvent e)
        {
          loadOptionPermissions();
        }
      });

    Container cp = getContentPane();
    cp.add(new SKDCHeaderLabel("Role Menu Items For Role: " + isRole),
                                                     BorderLayout.NORTH);
    if(canModify)
    {
      cp.add(buttonPanel(), BorderLayout.EAST);
      cp.add(sktable.getScrollPane(), BorderLayout.CENTER);
    }
    else
    {
                                     // Add the scroll pane to this frame.
      cp.add(sktable.getScrollPane(), BorderLayout.CENTER);
                                       // Add the button panel.
      cp.add(buttonPanel(), BorderLayout.SOUTH);
    }
  }

  /**
   *  Adds Buttons to a panel.
   */
  private JPanel buttonPanel()
  {
    if(canModify)
    {
                                       // Create Grid bag constraints.
      gbconst = new GridBagConstraints();
      gbconst.insets = new Insets(2, 2, 2, 2);
      buttonPanel.setLayout(new GridBagLayout());
    }
    else
    {
      buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    }

    buttonPanel.setBorder(BorderFactory.createEtchedBorder());

    if(canModify)
    {
      gbconst.gridx = 0;
      gbconst.gridy = 0;
      gbconst.gridwidth = GridBagConstraints.REMAINDER;
      gbconst.anchor = GridBagConstraints.WEST;

                 // Blank Line for spacing of buttons
      SKDCLabel lblBlankLine1 = new SKDCLabel("        ");

      // First turn off the default add button
      // and turn off the default modify button
      addButton.setAuthorization(false);
      modifyButton.setAuthorization(false);

                 // Set up the Modify Button
      if (msRole.equals(SKDCConstants.ROLE_MASTER))
      {
        mpBtnModifyScreen = new SKDCButton("Modify Screen Info", "Modify Screen Info.", 'c');
        mpBtnAdd    = new SKDCButton("  Add New Screen ", "Add New Screen.", 'a');
        mpBtnDelete = new SKDCButton("    Delete Screen    ", "Delete Screen.", 'd');
      }
//      else
//      {
        mpCopyPermissions = new JCheckBox("Copy on Click");
        mpCopyPermissions.setSelected(true);

        mpBtnModifyMenu = new SKDCButton("Modify Menu Items", "Modify Menu Items.", 'o');

                   // Set up the radio boxes
        mpAllowViewRoleBtnYes = new SKDCRadioButton("", 'V');
        mpAllowViewRoleBtnYes.eventListener(RADIO_VIEW_Y, mpButtonListener);
        mpBtnGroup.add(mpAllowViewRoleBtnYes);
        mpAllowViewRoleBtnNo = new SKDCRadioButton("Allow View", 'i');
        mpAllowViewRoleBtnNo.eventListener(RADIO_VIEW_N, mpButtonListener);
        mpAllowViewRoleBtnNo.setSelected(true);
        mpBtnGroup.add(mpAllowViewRoleBtnNo);

        JPanel viewBox = buildBox(Xaxis);
        viewBox.add(mpAllowViewRoleBtnYes);
        viewBox.add(mpAllowViewRoleBtnNo);

        mpToolbarRoleBtnYes = new SKDCRadioButton("", 'T');
        mpToolbarRoleBtnYes.eventListener(RADIO_TOOL_Y, mpButtonListener);
        mpToolBtnGroup.add(mpToolbarRoleBtnYes);
        mpToolbarRoleBtnNo = new SKDCRadioButton("Toolbar Button", 'l');
        mpToolbarRoleBtnNo.eventListener(RADIO_TOOL_N, mpButtonListener);
        mpToolbarRoleBtnNo.setSelected(true);
        mpToolBtnGroup.add(mpToolbarRoleBtnNo);

        JPanel toolBox = buildBox(Xaxis);
        toolBox.add(mpToolbarRoleBtnYes);
        toolBox.add(mpToolbarRoleBtnNo);

        mpAllowAddRoleBtnYes = new SKDCRadioButton("", 'A');
        mpAllowAddRoleBtnYes.eventListener(RADIO_ADD_Y, mpButtonListener);
        mpAddBtnGroup.add(mpAllowAddRoleBtnYes);
        allowAddRBtnNo = new SKDCRadioButton("Allow Add", 'd');
        allowAddRBtnNo.eventListener(RADIO_ADD_N, mpButtonListener);
        allowAddRBtnNo.setSelected(true);
        mpAddBtnGroup.add(allowAddRBtnNo);

        JPanel addBox = buildBox(Xaxis);
        addBox.add(mpAllowAddRoleBtnYes);
        addBox.add(allowAddRBtnNo);

        allowModifyRBtnYes = new SKDCRadioButton("", 'M');
        allowModifyRBtnYes.eventListener(RADIO_MODIFY_Y, mpButtonListener);
        mpModifyBtnGroup.add(allowModifyRBtnYes);
        allowModifyRBtnNo = new SKDCRadioButton("Allow Modify", 'o');
        allowModifyRBtnNo.eventListener(RADIO_MODIFY_N, mpButtonListener);
        allowModifyRBtnNo.setSelected(true);
        mpModifyBtnGroup.add(allowModifyRBtnNo);

        JPanel modifyBox = buildBox(Xaxis);
        modifyBox.add(allowModifyRBtnYes);
        modifyBox.add(allowModifyRBtnNo);

        allowDeleteRBtnYes = new SKDCRadioButton("", 'D');
        allowDeleteRBtnYes.eventListener(RADIO_DELETE_Y, mpButtonListener);
        mpDeleteBtnGroup.add(allowDeleteRBtnYes);
        allowDeleteRBtnNo = new SKDCRadioButton("Allow Delete", 'e');
        allowDeleteRBtnNo.eventListener(RADIO_DELETE_N, mpButtonListener);
        allowDeleteRBtnNo.setSelected(true);
        mpDeleteBtnGroup.add(allowDeleteRBtnNo);

        JPanel deleteBox = buildBox(Xaxis);
        deleteBox.add(allowDeleteRBtnYes);
        deleteBox.add(allowDeleteRBtnNo);

        buttonPanel.add(mpCopyPermissions, gbconst);

                // Now we have done our first add, set relative so it adds in a line
                // down
        gbconst.gridy = GridBagConstraints.RELATIVE;
               // add the radio boxes
        buttonPanel.add(lblYesNo, gbconst);
        buttonPanel.add(viewBox, gbconst);
        buttonPanel.add(toolBox, gbconst);
        buttonPanel.add(addBox, gbconst);
        buttonPanel.add(modifyBox, gbconst);
        buttonPanel.add(deleteBox, gbconst);
//      }

      gbconst.anchor = GridBagConstraints.CENTER;

           // Modify Menu Button
           // Now turn on the new modify button
      mpBtnModifyMenu.addEvent(MODIFY_BTN, mpButtonListener);
      mpBtnModifyMenu.setAuthorization(true);
      buttonPanel.add(mpBtnModifyMenu, gbconst);

             // Blank Line
      buttonPanel.add(lblBlankLine1, gbconst);

      if (!msRole.equals(SKDCConstants.ROLE_MASTER)
                  &&
         ((superuser && !msRole.equals(SKDCConstants.ROLE_DAC_SUPERROLE))
                             ||
          (administrator && !msRole.equals(SKDCConstants.ROLE_ADMINISTRATOR))))
      {
                 //      Copy option role combo box and button
           // Set up the roles combo box and the role copy button
        lblCopyOpt = new SKDCLabel("Copy Options from Role");
        mpRoleComboBox = new SKDCComboBox();
        mpBtnCopyOptions = new SKDCButton("    Role Copy    ", "Copy Role Menu Items", 'c');

        buttonPanel.add(lblCopyOpt, gbconst);
        buttonPanel.add(mpRoleComboBox, gbconst);
        RolesFill("");
        mpBtnCopyOptions.addEvent(COPY_BTN, mpButtonListener);
        mpBtnCopyOptions.setAuthorization(true);
        buttonPanel.add(mpBtnCopyOptions, gbconst);
      }
      else if(superuser && msRole.equals(SKDCConstants.ROLE_MASTER))
      {
        gbconst.gridy = GridBagConstraints.RELATIVE;
              // Add, Modify, Delete Buttons - for new Screen add ins

        mpBtnAdd.addEvent(ADD_BTN, mpButtonListener);
        mpBtnAdd.setAuthorization(true);
        buttonPanel.add(mpBtnAdd, gbconst);

        mpBtnModifyScreen.addEvent(MODIFY_SCREEN_BTN, mpButtonListener);
        mpBtnModifyScreen.setAuthorization(true);
        buttonPanel.add(mpBtnModifyScreen, gbconst);


        mpBtnDelete.addEvent(DELETE_BTN, mpButtonListener);
        mpBtnDelete.setAuthorization(true);
        buttonPanel.add(mpBtnDelete, gbconst);

      }
      else         // Other users can only do the Toolbar button
      {
                   // Set up the check boxes
        mpAllowViewRoleBtnYes.setEnabled(false);
        mpAllowViewRoleBtnNo.setEnabled(false);
        mpToolbarRoleBtnYes.setEnabled(true);
        mpToolbarRoleBtnNo.setEnabled(true);
        mpAllowAddRoleBtnYes.setEnabled(false);
        allowAddRBtnNo.setEnabled(false);
        allowModifyRBtnYes.setEnabled(false);
        allowModifyRBtnNo.setEnabled(false);
        allowDeleteRBtnYes.setEnabled(false);
        allowDeleteRBtnNo.setEnabled(false);

      }

                         // Show the Close Button
      buttonPanel.add(new SKDCLabel(" "), gbconst);
      buttonPanel.add(closeButton, gbconst);
      closeButton.setVisible(true);
    }
    else
    {
                       // Show the Close Button
      buttonPanel.add(closeButton);
      closeButton.setVisible(true);
    }
    return(buttonPanel);
  }

  protected JPanel buildBox(int whataxis)
  {
    JPanel thisbox = new JPanel();
    thisbox.setLayout(new BoxLayout(thisbox, whataxis));
    return(thisbox);
  }

  /**
   *  Method to populate the role combo box.
   *
   *  @param srch Name to match.
   */
  void RolesFill(String srch)
  {
    try
    {
      mpRoleComboBox.setComboBoxData(userServ.getRoleNameList(srch));
    }
    catch (DBException e)
    {
      displayError("Unable to get Roles");
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
        if (canModify)
        {
          popupMenu.setAuthorization("Modify Menu Items", true);
          popupMenu.add("Modify Menu", MODIFY_BTN, mpButtonListener);
          popupMenu.add("Modify Screen", MODIFY_SCREEN_BTN, mpButtonListener);
          popupMenu.add("Close Frame", CLOSE_BTN, mpButtonListener);

        }
        else
        {
          popupMenu.add("Close Frame", CLOSE_BTN, mpButtonListener);
        }

        return(popupMenu);
      }
     /**
      *  Display detail screen.
      */
      @Override
      public void displayDetail()
      {
      }
    });
  }

  /**
   * Set Permissions for the user for this Screen
   */
  private void setPermissions()
  {
    addButton.setAuthorization(false);
    modifyButton.setAuthorization(canModify);
    deleteButton.setAuthorization(false);
    popupMenu.setAuthorization("Add", false);
    popupMenu.setAuthorization("Modify Menu Items", canModify);
    popupMenu.setAuthorization("Delete", false);
  }

  protected void modifyScreenButtonPressed()
  {
    RoleOptionData vpRoleOptData = Factory.create(RoleOptionData.class);
    Map vpRowMap = sktable.getSelectedRowData();
    if (vpRowMap != null && !vpRowMap.isEmpty())
    {
      vpRoleOptData.dataToSKDCData(vpRowMap);
      vpRoleOptData.setRole(msRole);
      modifyMasterOption(vpRoleOptData);
    }
  }

  /**
   *
   */
  protected void modifyMenuButtonPressed()
  {                                    // Load selected line data into poldata
    int[] selection = sktable.getSelectedRows();

    if (selection.length == 0)
    {
      displayInfoAutoTimeOut("No row selected to Modify", "Selection Error");
      return;
    }

    if (msRole.equals(SKDCConstants.ROLE_MASTER) && superuser)
    {
      if(selection.length > 1)
      {
        displayInfoAutoTimeOut("For " + SKDCConstants.ROLE_MASTER
            + " Role, only one menu option can be modified at a time",
        "Modify Info");
        return;
      }
    }
    if (msRole.equals(SKDCConstants.ROLE_MASTER) && !superuser)
    {
      displayError("Cannot Modify " + SKDCConstants.ROLE_MASTER + " Role",
          "Modify Error");
      return;
    }
    else if (msRole.equals(SKDCConstants.ROLE_ADMINISTRATOR) && !administrator
        && !superuser)
    {
      displayError("Cannot Modify " + SKDCConstants.ROLE_ADMINISTRATOR
          + " Role", "Modify Error");
      return;
    }
    else if (msRole.equals(SKDCConstants.ROLE_DAC_SUPERROLE) && !superuser)
    {
      displayError("Cannot Modify " + SKDCConstants.ROLE_DAC_SUPERROLE
          + " Role", "Modify Error");
      return;
    }
    // get the selected row and then we will
    // convert them to roleoptiondata to modify
    RoleOptionData vpRoleOptData = Factory.create(RoleOptionData.class);

    List modList = sktable.getSelectedRowDataArray();

    for(int i = 0; i < modList.size(); i++)
    {
      vpRoleOptData.dataToSKDCData((Map)modList.get(i));
      vpRoleOptData.setRole(msRole);

      try
      {
        boolean hasone = false;

        /*
         * If this user is modifying his own role, then he will not have access
         * to the other check boxes (he only has button bar changes) so we only
         * need to set the button bar value...so get his other setting for this
         * option and then set the button bar and then update it.
         */
        if (msRole.equals(SKDCUserData.getRole()))
        {
          if (vpRoleOptData.getAddAllowed() == DBConstants.YES ||
              vpRoleOptData.getViewAllowed() == DBConstants.YES ||
              vpRoleOptData.getDeleteAllowed() == DBConstants.YES ||
              vpRoleOptData.getModifyAllowed() == DBConstants.YES )
          {
            hasone = true;
          }
          if(mpToolbarRoleBtnYes.isSelected())
          {
            vpRoleOptData.setButtonBar(DBConstants.YES);
            hasone = true;
          }
          else
          {
            vpRoleOptData.setButtonBar(DBConstants.NO);
          }
        }
        else
        {
          // This is the Administrator or Daifuku modifying someone else...
          if(mpToolbarRoleBtnYes.isSelected())
          {
            vpRoleOptData.setButtonBar(DBConstants.YES);
            hasone = true;
          }
          else
          {
            vpRoleOptData.setButtonBar(DBConstants.NO);
          }
          // Just a quick check to verify that only the Administrator and Daifuku
          // can modify other than the tool bar button except on their own account
          if( (administrator && !msRole.equals(SKDCConstants.ROLE_ADMINISTRATOR))
              ||
              (superuser && !msRole.equals(SKDCConstants.ROLE_DAC_SUPERROLE)) )
          {
            if(mpAllowViewRoleBtnYes.isSelected())
            {
              vpRoleOptData.setViewAllowed(DBConstants.YES);
              hasone = true;
            }
            else
            {
              vpRoleOptData.setViewAllowed(DBConstants.NO);
            }
            if(mpAllowAddRoleBtnYes.isSelected())
            {
              vpRoleOptData.setAddAllowed(DBConstants.YES);
              hasone = true;
            }
            else
            {
              vpRoleOptData.setAddAllowed(DBConstants.NO);
            }
            if(allowModifyRBtnYes.isSelected())
            {
              vpRoleOptData.setModifyAllowed(DBConstants.YES);
              hasone = true;
            }
            else
            {
              vpRoleOptData.setModifyAllowed(DBConstants.NO);
            }
            if(allowDeleteRBtnYes.isSelected())
            {
              vpRoleOptData.setDeleteAllowed(DBConstants.YES);
              hasone = true;
            }
            else
            {
              vpRoleOptData.setDeleteAllowed(DBConstants.NO);
            }
          }
        }

        // Now make sure he at least has a view if he has something
        if (!msRole.equals(SKDCConstants.ROLE_MASTER) &&
            vpRoleOptData.getViewAllowed() == DBConstants.NO && hasone)
        {
          vpRoleOptData.setViewAllowed(DBConstants.YES);
        }
        // If the Role exists Modify it...
        // If it is a new assignment...Add it
        if (userServ.roleOptionExists(msRole, vpRoleOptData.getCategory(),
                                     vpRoleOptData.getOption()))
        {
          // If they have at least one checkbox ...modify it otherwise delete their option
          if (hasone)
          {
            userServ.updateRoleOptionDataInfo(vpRoleOptData);
          }
          else
          {
            if (msRole.equals(SKDCConstants.ROLE_MASTER))
              userServ.updateRoleOptionDataInfo(vpRoleOptData);
            else
              userServ.deleteRoleOption(msRole, vpRoleOptData.getCategory(),
                                        vpRoleOptData.getOption());
          }
        }
        else // it did not exist so add it to the role
        {
          // The Option does not exist for this user so
          // Check to see if the user giving it has this option...
          // He cannot give an option he does not have!
          // userData is the userData of the person giving it and we check his role
          if (userServ.roleOptionExists(SKDCUserData.getRole(),
              vpRoleOptData.getCategory(), vpRoleOptData.getOption()))
          {
            userServ.addRoleOption(vpRoleOptData);
          }
          else
          {
            String tmpstring = "User: " + SKDCUserData.getLoginName()
                + " Role: " + SKDCUserData.getRole()
                + "\n Does not possess Option: " + vpRoleOptData.getOption()
                + "\n And Cannot give it to Role: " + msRole;

            logger.logDebug(tmpstring);
            displayError(tmpstring, "Modify Error");
            return;
          }
        }

      }
      catch(DBException dbe)
      {
        System.out.println("modifying Menu Item: " + i);
        displayError("Error Modifying Options", "Modify Error");
        return;
      }
    }

    if (selection.length == 1)
    {
      sktable.modifySelectedRow(vpRoleOptData);
    }
    else
    {
      try
      {
        sktable.refreshData(userServ.getRoleOptionsList(msRole));
      }
      catch(DBException db)
      {
        displayError("Error Reading Data After Update", "Data Retrieval Error");
      }
    }
  }

  /**
   *
   */
  @Override
  protected void deleteButtonPressed()
  {                                    // Load selected line data into poldata
    int[] selection = sktable.getSelectedRows();
    boolean vzPreserveMasterOption = true;

    if (selection.length < 1)
    {
      displayInfoAutoTimeOut("No row selected to delete", "Selection Error");
      return;
    }
    else if (selection.length > 1)
    {
      displayInfoAutoTimeOut("Only one row can be Deleted at a time",
      "Selection Error");
      return;
    }

    if (!superuser)
    {
      displayError("Cannot Delete from " + SKDCConstants.ROLE_MASTER + " Role", "Delete Error");
      return;
    }
    // get the selected row and then we will
    // convert them to roleoptiondata to modify
    RoleOptionData rolOptD = Factory.create(RoleOptionData.class);

    List modList = sktable.getSelectedRowDataArray();

    for(int i = 0; i < modList.size(); i++)
    {

      rolOptD.dataToSKDCData((Map)modList.get(i));
      rolOptD.setRole(msRole);

      if (!displayYesNoPrompt("Are you sure you want to delete Role Option "
          + rolOptD.getOption() + " from all Users?"))
      {
        return;
      }

      if (displayYesNoPrompt("Do you want to delete Role Option "
          + rolOptD.getOption() + " from the " + SKDCConstants.ROLE_MASTER
          + " and " + SKDCConstants.ROLE_DAC_SUPERROLE + " Roles"))
      {
        vzPreserveMasterOption = false;
      }

      try
      {
        // If the Role exists Delete it...
        // If not, give an error
        if (userServ.roleOptionExists(SKDCConstants.ROLE_MASTER,
            rolOptD.getCategory(), rolOptD.getOption()))
        {
          userServ.deleteRoleOption(rolOptD.getOption(), rolOptD.getCategory(),
                                    vzPreserveMasterOption);

          String tmpstring;
          if (vzPreserveMasterOption)
          {
            tmpstring = "Option: " + rolOptD.getOption()
                + " successfully Deleted from all roles except "
                + SKDCConstants.ROLE_MASTER + " and "
                + SKDCConstants.ROLE_DAC_SUPERROLE;
          }
          else
          {
            tmpstring = "Option: " + rolOptD.getOption()
                + " successfully Deleted from all Roles";
          }
          logger.logOperation(tmpstring);
          displayInfoAutoTimeOut(tmpstring, "Delete Result");
          if (vzPreserveMasterOption)
          {
            return;
          }
        }
        else // it did not exist so add it to the role
        {
          String tmpstring = "Option: " + rolOptD.getOption() + " Does Not Exist";
          logger.logDebug(tmpstring);
          displayError(tmpstring, "Delete Error");
          return;
        }

      }
      catch(DBException dbe)
      {
        System.out.println("modifying Menu Item: " + i);
        displayError("Error Modifying Options", "Modify Error");
        return;
      }
      catch(java.util.NoSuchElementException ne)
      {
        String tmpstring = "Role Option " + rolOptD.getOption()
            + " does not exist for any Users except "
            + SKDCConstants.ROLE_MASTER + " and "
            + SKDCConstants.ROLE_DAC_SUPERROLE;
        displayInfoAutoTimeOut(tmpstring, "Delete Result");
        return;
      }
    }

    if (selection.length > 0)
    {
      sktable.deleteSelectedRows();
    }
    return;
  }

  void copyButtonPressed()
  {                                    // Load selected role to copy from
    String copyRole = mpRoleComboBox.getSelectedItem().toString();

    if ((copyRole == null) || (copyRole.trim().length() == 0))
    {
      displayError("Role is required");
      return;
    }
    if (copyRole.equals(msRole))
    {
      displayError("Cannot Copy Same Role Options");
      return;
    }

    if (msRole.equals(SKDCConstants.ROLE_MASTER))
    {
      displayError("Cannot Modify " + SKDCConstants.ROLE_MASTER + " Role",
          "Modify Error");
      return;
    }
    else if (msRole.equals(SKDCConstants.ROLE_ADMINISTRATOR))
    {
      displayError("Cannot Modify " + SKDCConstants.ROLE_ADMINISTRATOR
          + " Role", "Modify Error");
      return;
    }
    else if (msRole.equals(SKDCConstants.ROLE_DAC_SUPERROLE))
    {
      displayError("Cannot Modify " + SKDCConstants.ROLE_DAC_SUPERROLE
          + " Role", "Modify Error");
      return;
    }

    try
    {
      userServ.copyRoleOptions(msRole, copyRole);
    }
    catch(DBException dbe)
    {
      System.out.println("Error Copying Role Options from role" + copyRole);
      displayError("Error Copying Options", "Copy Error");
      return;
    }

    try
    {
      sktable.refreshData(userServ.getRoleOptionsList(msRole));
    }
    catch(DBException db)
    {
      displayError("Error Reading Data After Update", "Data Retrieval Error");
    }
  }

  /**
   *
   */
  @Override
  protected void addButtonPressed()
  {
    UpdateRoleOption updateRO = Factory.create(UpdateRoleOption.class, "Add");
    addSKDCInternalFrameModal(updateRO, buttonPanel,
      new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
          String prop = e.getPropertyName();

          if (prop.equals(FRAME_CHANGE))
          {
            RoleOptionData rodata = (RoleOptionData)e.getNewValue();
            // DEBUG: displayInfo(rold.toString());
            sktable.appendRow(rodata);
          }
      }
    });
  }

  void modifyMasterOption(RoleOptionData rd)
  {
    UpdateRoleOption updateRO = Factory.create(UpdateRoleOption.class, "Modify");
    updateRO.setModify(rd.getRole(), rd.getCategory(), rd.getOption());
    addSKDCInternalFrameModal(updateRO, buttonPanel,
      new PropertyChangeListener()
      {
        public void propertyChange(PropertyChangeEvent e)
        {
          String prop = e.getPropertyName();

          if (prop.equals(FRAME_CHANGE))
          {
            RoleOptionData rodata = (RoleOptionData)e.getNewValue();
            // DEBUG: displayInfo(rold.toString());
            sktable.modifySelectedRow(rodata);
          }
        }
      });
  }

  /**
   * Change the radio buttons to reflect the first row's permissions
   */
  private void loadOptionPermissions()
  {
    if (sktable.getSelectedRowCount() == 1 && mpCopyPermissions.isSelected())
    {
      String vsAllowView = sktable.getSelectedColumnData(RoleOptionData.VIEWALLOWED_NAME)[0];
      String vsButtonBar = sktable.getSelectedColumnData(RoleOptionData.BUTTONBAR_NAME)[0];
      String vsAllowAdd  = sktable.getSelectedColumnData(RoleOptionData.ADDALLOWED_NAME)[0];
      String vsAllowMod  = sktable.getSelectedColumnData(RoleOptionData.MODIFYALLOWED_NAME)[0];
      String vsAllowDel  = sktable.getSelectedColumnData(RoleOptionData.DELETEALLOWED_NAME)[0];

      boolean vzAllowView = Integer.parseInt(vsAllowView) == DBConstants.YES;
      boolean vzButtonBar = Integer.parseInt(vsButtonBar) == DBConstants.YES;
      boolean vzAllowAdd  = Integer.parseInt(vsAllowAdd)  == DBConstants.YES;
      boolean vzAllowMod  = Integer.parseInt(vsAllowMod)  == DBConstants.YES;
      boolean vzAllowDel  = Integer.parseInt(vsAllowDel)  == DBConstants.YES;

      mpAllowViewRoleBtnYes.setSelected(vzAllowView);
      mpToolbarRoleBtnYes.setSelected(vzButtonBar);
      mpAllowAddRoleBtnYes.setSelected(vzAllowAdd);
      allowModifyRBtnYes.setSelected(vzAllowMod);
      allowDeleteRBtnYes.setSelected(vzAllowDel);

      mpAllowViewRoleBtnNo.setSelected(!vzAllowView);
      mpToolbarRoleBtnNo.setSelected(!vzButtonBar);
      allowAddRBtnNo.setSelected(!vzAllowAdd);
      allowModifyRBtnNo.setSelected(!vzAllowMod);
      allowDeleteRBtnNo.setSelected(!vzAllowDel);
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
    return RoleOptionListFrame.class;
  }

  /**
   *  Button Listener class.
   */
  private class RoleButtonListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      String vsPressedButton = e.getActionCommand();
      if (vsPressedButton.equals(ADD_BTN))
      {
        addButtonPressed();
      }
      else if (vsPressedButton.equals(MODIFY_BTN))
      {
        modifyMenuButtonPressed();
      }
      else if (vsPressedButton.equals(MODIFY_SCREEN_BTN))
      {
        modifyScreenButtonPressed();
      }
      else if (vsPressedButton.equals(DELETE_BTN))
      {
        deleteButtonPressed();
      }
      else if (vsPressedButton.equals(CLOSE_BTN))
      {
        closeButtonPressed();
      }
      else if (vsPressedButton.equals(COPY_BTN))
      {
        copyButtonPressed();
      }
    }
  }
}
