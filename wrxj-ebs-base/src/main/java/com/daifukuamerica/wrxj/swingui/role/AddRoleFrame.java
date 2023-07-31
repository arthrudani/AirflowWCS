package com.daifukuamerica.wrxj.swingui.role;


import com.daifukuamerica.wrxj.dataserver.standard.StandardUserServer;
import com.daifukuamerica.wrxj.dbadapter.data.RoleData;
import com.daifukuamerica.wrxj.dbadapter.data.RoleOptionData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Description:<BR>
 *    Sets up the Role add internal frame.  It fills in the contents of
 *    a JPanel before adding it to the Internal frame.  This method then
 *    returns the Internal Frame reference to the caller so that it can be
 *    added to the desktop when appropriate.
 *
 * @author       sbw
 * @version      1.0
 * <BR>Created: 24-Mar-03<BR>
 *     Copyright (c) 2003<BR>
 *     Company:  SKDC Corporation
 */
@SuppressWarnings("serial")
public class AddRoleFrame extends DacInputFrame
{
  RoleData rold = Factory.create(RoleData.class);
  StandardUserServer userServer;
  private SKDCScreenPermissions ePerms;
  private SKDCTextField txtRole = new SKDCTextField(RoleData.ROLE_NAME);
  private SKDCTextField txtRoleDescription = new SKDCTextField(RoleData.ROLEDESCRIPTION_NAME);

  public AddRoleFrame(StandardUserServer ipUserServer, SKDCScreenPermissions ePrms)
  {
    super("Add Role", "Role Information");
    this.userServer = ipUserServer;
    this.ePerms = ePrms;

    buildScreen();
  }

  @Override
  public void finalize()
  {
     rold = null;
  }

/*===========================================================================
              Methods for display formatting go in this section.
  ===========================================================================*/
  private void buildScreen()
  {
    addInput("Role:", txtRole);
    addInput("Role Description:", txtRoleDescription);
    
    useAddButtons();
  }

/*===========================================================================
                Methods for event handling go in this section.
  ===========================================================================*/
  /**
   *  Processes PO add request.  This method stuffs column objects into
   *  a PurchaseOrder instance container.
   */
  @Override
  protected void okButtonPressed()
  {
      rold.clear();                    // Make sure everything is defaulted
                                       // to begin with.

                                       // PO ID is a required field.
    if (txtRole.getText().length() != 0)
    {
        rold.setRole(txtRole.getText());
    }
    else
    {
      displayError("Role is required", "Entry Error");
      return;
    }
    if(txtRoleDescription.getText().length() > 0)
    {
      rold.setRoleDescription(txtRoleDescription.getText());
    }
    else
    {
      displayError("Role description is required", "Entry Error");
      return;
    }
    rold.setRoleType(DBConstants.WORKER);
    try
    {
        if (userServer.roleExists(txtRole.getText()) == true)
        {
            displayError("Role " + txtRole.getText() + " already exists",
                       "Entry Error");
        }
        else                             // If the Role doesn't exist, prompt
        {                                // for adding the Role Options.
            userServer.addRole(rold);
            changed(null, rold);
                  // Pass a blank role to the option list frame because there are
                  // no options for this user yet.
            showRoleOptionListFrame(txtRole.getText());          // Show Frame for adding Options.
        }
    }
    catch(Exception exc)
    {
      exc.printStackTrace(System.out);
      displayError(exc.getMessage(), "DB Error");
    }
    return;
  }

  /**
   *  Clear button event handler.  This method resets all fields in the
   *  Add PO Frame.
   */
  @Override
  protected void clearButtonPressed()
  {
    txtRole.setText("");
    txtRoleDescription.setText("");
    txtRole.requestFocus();
  }

  /**
   *  Cancel Button handler.
   */
  @Override
  protected void closeButtonPressed()
  {
    close();
  }

  /**
   *  Displays frame for Modifying Role Options.
   */
  private void showRoleOptionListFrame(String sRole)
  {
    try
    {

      RoleOptionListFrame roleOptionListFrame = new RoleOptionListFrame("Add Role",
                                                           sRole, ePerms);
      roleOptionListFrame.setCategoryAndOption(getCategory(), getOption());
      roleOptionListFrame.setTableData(sRole);
      addSKDCInternalFrameModal(roleOptionListFrame, mpButtonPanel,
                                 new RoleOptionListFrameHandler());
    }
    catch(Exception e)
    {
      e.printStackTrace(System.out);
      displayError(e.getMessage(), "Frame/Table Data Creation Error");
    }
  }

/*===========================================================================
              ****** All Listener classes go here ******
  ===========================================================================*/
  /**
   *   Property Change event listener for Add PO Line frame.
   */
  private class RoleOptionListFrameHandler implements PropertyChangeListener
  {
      String rtnMessage;

      public void propertyChange(PropertyChangeEvent pcevt)
      {
          String prop_name = pcevt.getPropertyName();
          if (prop_name.equals(FRAME_CLOSING))
          {
              close();
          }
          else if (prop_name.equals(FRAME_CHANGE))
          {                                // Get back the Role Option Array from
                                           // the AddRoleOptionLine frame.
              RoleOptionData[] roleOptionDataArray = (RoleOptionData[])pcevt.getNewValue();

              if (roleOptionDataArray != null)
              {
                  boolean addedOption = false;
                  try
                  {
                    RoleData newRoleData = Factory.create(RoleData.class);
                    for(int idx = 0; idx < roleOptionDataArray.length; idx++)
                    {
                                 // on the first one, fill out the
                                 // Role info
                      if(idx == 0)
                      {
                          newRoleData.setRole(rold.getRole());
                          newRoleData.setRoleDescription(rold.getRoleDescription());
                          newRoleData.setRoleType(DBConstants.WORKER);
                          userServer.addRole(newRoleData);
                      }
                      RoleOptionData newRoleOptD = Factory.create(RoleOptionData.class);
                      newRoleOptD = roleOptionDataArray[idx];
                      userServer.addRoleOption(newRoleOptD);
                      addedOption = true;
                    }
                                                     // If at least one line was added,
                    if (addedOption)                   // commit the order and line(s) to the
                    {                                // database.
                        try
                        {
                                       // Just add one line...don't do the
                                       // whole list again
                                changed(null, newRoleData);
                        }
                        catch(Exception exc)
                        {
                            rtnMessage = "Error Adding Role";
                            displayError(rtnMessage + exc.getMessage(), "Add Role");
                            exc.printStackTrace(System.out);
                        }
                    }
                    else                             // Case where nothing got added.
                    {
                        rtnMessage = "Error Adding Role";
                        displayError(rtnMessage, "Add Role");
                    }
                  }
                  catch(Exception exc)
                  {
                      rtnMessage = "Error Adding Role";
                      displayError(rtnMessage + exc.getMessage(), "Add Role");
                  }
              }
          }
      }
  }
}
