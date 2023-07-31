package com.daifukuamerica.wrxj.swingui.role;

import com.daifukuamerica.wrxj.dataserver.standard.StandardUserServer;
import com.daifukuamerica.wrxj.dbadapter.data.RoleData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCTextField;

/**
 * Description:<BR>
 *    Sets up the Order modify internal frame.  It fills in the contents of
 *    a JPanel before adding it to the Internal frame.  This method then
 *    returns the Internal Frame reference to the caller so that it can be
 *    added to the desktop when appropriate.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 17-Jun-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  SKDC Corporation
 */
@SuppressWarnings("serial")
public class ModifyRoleFrame extends DacInputFrame
{
  private RoleData      currentRoleData = Factory.create(RoleData.class);
  private RoleData      newRoleData = Factory.create(RoleData.class);
  private StandardUserServer    userServer;
  private SKDCTextField txtRole = new SKDCTextField(RoleData.ROLE_NAME);
  private SKDCTextField txtRoleDescription = new SKDCTextField(RoleData.ROLEDESCRIPTION_NAME);

  public ModifyRoleFrame(StandardUserServer ipUserServer)
  {
    super("Modify Role", "Role Information");

    userServer = ipUserServer;
    buildScreen();
  }

  @Override
  public void finalize()
  {
    currentRoleData = null;
    newRoleData = null;
  }

  /**
   *   Load the modify screen with the current selected row of data from the
   *   table.  It is assumed that this frame has already been built when this
   *   method is called.
   */
  public void setCurrentData(RoleData ipCurrentRoleData)
  {
    currentRoleData = ipCurrentRoleData;
    newRoleData = ipCurrentRoleData;

    txtRole.setText(ipCurrentRoleData.getRole());
    txtRoleDescription.setText(ipCurrentRoleData.getRoleDescription());
  }

/*===========================================================================
              Methods for display formatting go in this section.
  ===========================================================================*/
  private void buildScreen()
  {
    txtRole.setEnabled(false);
    
    addInput("Role:", txtRole);
    addInput("Role Description:", txtRoleDescription);

    useModifyButtons();
  }

/*===========================================================================
                Methods for event handling go in this section.
  ===========================================================================*/
  /**
   *  Processes Order modify request.
   */
  @Override
  protected void okButtonPressed()
  {
    newRoleData.clear();                 // Make sure everything is defaulted
                                       // to begin with.

    if (txtRoleDescription.getText().trim().length() > 0)
    {
      newRoleData.setRoleDescription(txtRoleDescription.getText());
    }
    else
    {
      newRoleData.setRoleDescription(" ");
    }
    
    try
    {                                  // Set the key for the modify.
      newRoleData.setKey(RoleData.ROLE_NAME, txtRole.getText());
      userServer.updateRoleInfo(newRoleData);
                                       // Get fresh data for screen update.
      changed(null, newRoleData);
      displayInfoAutoTimeOut("Role Successfully Modified", "Modify Confirmation");
      Thread.sleep(30);
      close();
    }
    catch(DBException e)
    {
      displayError(e.getMessage(), "Role Modify Error");
    }
    catch(InterruptedException e)
    {  // ignore it!.
    }

    return;
  }

  /**
   *  Clear button event handler.  This method resets all fields in the
   *  Modify Order dialog.
   */
  @Override
  protected void clearButtonPressed()
  {
    setCurrentData(currentRoleData);
    txtRoleDescription.requestFocus();
  }

  /**
   *  Cancel Button handler.
   */
  @Override
  protected void closeButtonPressed()
  {
    close();
  }
}
