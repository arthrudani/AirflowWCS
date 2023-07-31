package com.daifukuamerica.wrxj.swingui.role;

import com.daifukuamerica.wrxj.dataserver.standard.StandardUserServer;
import com.daifukuamerica.wrxj.dbadapter.data.RoleOptionData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import javax.swing.event.InternalFrameEvent;

/**
 * A screen class for updating screens.
 *

 * @version 1.0
 */
@SuppressWarnings("serial")
public class UpdateRoleOption extends DacInputFrame
{
  protected SKDCTextField mpTxtOptionName = new SKDCTextField(RoleOptionData.OPTION_NAME);
  protected SKDCTextField mpTxtCategory = new SKDCTextField(RoleOptionData.CATEGORY_NAME);
  protected SKDCTextField mpTxtIconPath = new SKDCTextField(RoleOptionData.ICONNAME_NAME);
  protected SKDCTextField mpTxtClassNamePath = new SKDCTextField(RoleOptionData.CLASSNAME_NAME);

  protected StandardUserServer userServ = Factory.create(StandardUserServer.class);

  protected String screenOption = "";
  protected String screenRole = "";
  protected String screenCategory = "";

  RoleOptionData defaultRoleOptionData = Factory.create(RoleOptionData.class);

  boolean mzAdding = true;

  /**
   *  Create RoleOption screen class.
   *
   *  @param isTitle Title to be displayed.
   */
  public UpdateRoleOption(String isTitle)
  {
    super(isTitle, "Screen Information");
    try
    {
      jbInit();
      pack();
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
  }

  /**
   *  Create default RoleOption screen class.
   *
   */
  public UpdateRoleOption()
  {
    this("");
  }

  /**
   *  Method to set screen for modifying.
   *
   *  @param screen - Screen to be modified.
   */
  public void setModify(String role, String category, String screen)
  {
    screenRole = role;
    screenCategory = category;
    screenOption = screen;
    mzAdding = false;
    useModifyButtons();
  }

  /**
   * Overridden method so we can set up frame for either an add or modify
   *
   * @param ipEvent ignored
   */
  @Override
  public void internalFrameOpened(InternalFrameEvent ipEvent)
  {
    super.internalFrameOpened(ipEvent);

    if (screenOption.length() > 0) // we are modifying
    {
      try
      {
        defaultRoleOptionData = userServ.getRoleOptionData(screenRole,
            screenCategory, screenOption);
      }
      catch (DBException e2)
      {
        displayError("Unable to get Screen data");
        return;
      }

      mpTxtCategory.requestFocus();
      mpTxtOptionName.setEnabled(false);
    }
    setData(defaultRoleOptionData);
  }

  /**
   *  Method to intialize screen components. This adds the components to the
   *  screen and adds listeners as needed.
   *
   *  @exception Exception
   */
  private void jbInit() throws Exception
  {
    addInput("Screen Name", mpTxtOptionName);
    addInput("Screen Category", mpTxtCategory);
    addInput("Icon Path ( /graphics/... )", mpTxtIconPath);
    addInput("Class Path ( package.class )", mpTxtClassNamePath);

    mzAdding = true;
    useAddButtons();
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
   *  Action method to handle OK button. Verifies that entered data is valid,
   *  then adds a new user to the database.
   */
  @Override
  protected void okButtonPressed()
  {
    boolean adding = false;

    boolean vzOptionExists;
    if (mzAdding)
    {
      adding = true;
      screenRole = SKDCConstants.ROLE_DAC_SUPERROLE;
    }
    vzOptionExists = userServ.roleOptionExists(SKDCConstants.ROLE_MASTER,
        screenCategory, screenOption);

    if (adding && vzOptionExists)
    {
      displayError("Screen: " + mpTxtOptionName.getText() + " already exists");
      return;
    }

    if (!adding && !vzOptionExists)
    {
      displayError("Screen: " + mpTxtOptionName.getText()
          + " cannot be modifed.  It does not exist");
      return;
    }

    if (!adding && !SKDCUserData.isSuperUser())
    {
      displayError("Not authorized to modify screens");
      return;
    }

    RoleOptionData vpRDChanged = Factory.create(RoleOptionData.class);
    // fill in IM data

    if (adding)
    {
      vpRDChanged.setOption(mpTxtOptionName.getText());
      if (vpRDChanged.getOption().length() <= 0)  // required
      {
        displayError("Screen name is required");
        return;
      }
      vpRDChanged.setCategory(mpTxtCategory.getText());
      if (vpRDChanged.getCategory().length() <= 0)  // required
      {
        displayError("Category name is required");
        return;
      }
      vpRDChanged.setRole(SKDCConstants.ROLE_MASTER);
    }
    else
    {
      vpRDChanged.setOption(screenOption);
      vpRDChanged.setRole(screenRole);
      vpRDChanged.setCategory(mpTxtCategory.getText());
      if (vpRDChanged.getCategory().length() <= 0)  // required
      {
        displayError("Category name is required");
        return;
      }
    }
    vpRDChanged.setClassName(mpTxtClassNamePath.getText());
    vpRDChanged.setIconName(mpTxtIconPath.getText());

    try
    {
      if (adding)
      {
        vpRDChanged.setButtonBar(DBConstants.YES);
        vpRDChanged.setAddAllowed(DBConstants.YES);
        vpRDChanged.setViewAllowed(DBConstants.YES);
        vpRDChanged.setModifyAllowed(DBConstants.YES);
        vpRDChanged.setDeleteAllowed(DBConstants.YES);

        userServ.addRoleOption(vpRDChanged);
        // Now that we have added it to the Master Role, add it to the Daifuku Role
        vpRDChanged.setRole(SKDCConstants.ROLE_DAC_SUPERROLE);
        userServ.addRoleOption(vpRDChanged);
        changed(null, vpRDChanged);
        displayInfoAutoTimeOut("Screen: " + mpTxtOptionName.getText()
            + " successfully added");
      }
      else
      {
        userServ.updateRoleOptionDataInfoForAll(defaultRoleOptionData, vpRDChanged);
        changed(null, vpRDChanged);
        displayInfoAutoTimeOut("Screen: " + mpTxtOptionName.getText()
            + " updated for all roles");
      }
    }
    catch (DBException e2)
    {
      e2.printStackTrace(System.out);
      if (adding)
      {
        displayError("Error adding screen: " + mpTxtOptionName.getText());
      }
      else
      {
        displayError("Error updating screen: " + mpTxtOptionName.getText());
      }
    }
    if (!adding)
    {
      cleanUpOnClose();
      close();
    }
  }

  /**
   *  Action method to handle Clear button.
   */
  @Override
  protected void clearButtonPressed()
  {
    mpTxtOptionName.setText(defaultRoleOptionData.getOption());
    mpTxtCategory.setText(defaultRoleOptionData.getCategory());
    mpTxtIconPath.setText(defaultRoleOptionData.getIconName());
    mpTxtClassNamePath.setText(defaultRoleOptionData.getClassName());
    if (mzAdding)
      mpTxtOptionName.requestFocus();
    else
      mpTxtCategory.requestFocus();
  }

  /**
   *  Method to refresh screen fields.
   *
   *  @param rodata Screen data to use in refreshing.
   */
  void setData(RoleOptionData rodata)
  {
    defaultRoleOptionData = (RoleOptionData)rodata.clone();
    clearButtonPressed();
  }

  /**
   *  Action method to handle Close button.
   */
  @Override
  protected void closeButtonPressed()
  {
    cleanUpOnClose();
    close();
  }

}