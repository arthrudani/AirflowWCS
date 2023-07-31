package com.daifukuamerica.wrxj.swingui.user;

import com.daifukuamerica.wrxj.dataserver.standard.StandardUserServer;
import com.daifukuamerica.wrxj.dbadapter.data.EmployeeData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.DacTranslator;
import com.daifukuamerica.wrxj.swing.ReleaseToCodeField;
import com.daifukuamerica.wrxj.swing.SKDCCheckBox;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCDateField;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.event.InternalFrameEvent;

/**
 * A screen class for updating users.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class UpdateUser extends DacInputFrame
{
  protected static int PASSWORD_LENGTH = DBInfo.getFieldLength(EmployeeData.PASSWORD_NAME);
  protected StandardUserServer mpUserServer = Factory.create(StandardUserServer.class);
  protected String mpUserID = "";
  protected boolean mzMultipleUserMod = false;
  protected String[] masUserArray;
  EmployeeData mpDefaultEmployeeData = Factory.create(EmployeeData.class);

  protected SKDCTextField mpTxtUserID;
  protected SKDCTextField mpTxtUserName;
  protected SKDCCheckBox mpCBChangePassword;
  protected JPasswordField mpPassword1;
  protected JPasswordField mpPassword2;
  protected PasswordStrengthChecker mpPSChecker;
  protected SKDCComboBox mpComboRoles;
  protected ReleaseToCodeField mpReleaseToCode;
  protected SKDCDateField mpDateExpire;
  protected SKDCCheckBox mpCBEnableExpireDate;
  protected SKDCComboBox mpLanguage;
  protected SKDCTranComboBox mpRememberLastLogin;
  protected JPanel mpExpiryPanel;

  protected boolean mzAdding = true;

  /**
   *  Create user screen class.
   *
   *  @param isTitle Title to be displayed.
   */
  public UpdateUser(String isTitle)
  {
    super(isTitle, "User Information");
    jbInit();
  }

  /**
   *  Create default user screen class.
   */
  public UpdateUser()
  {
    this("");
  }

  /**
   *  Method to set screen for adding.
   */
  public void setAdd()
  {
    mpUserID = "";
  }

  /**
   *  Method to set screen for modifying.
   *
   *  @param s User to be modified.
   */
  public void setModify(String s)
  {
    mzMultipleUserMod = false;
    mpUserID = s;
    mzAdding = false;
    insertInput(3, "Change Password", mpCBChangePassword);
    mpCBChangePassword.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          mpPassword1.setEnabled(mpCBChangePassword.isSelected());
          mpPassword2.setEnabled(mpCBChangePassword.isSelected());
          mpPSChecker.setEnabled(mpCBChangePassword.isSelected());
          mpPassword1.requestFocus();
        }
      });
    mpPassword1.setEnabled(false);
    mpPassword2.setEnabled(false);
    mpPSChecker.setEnabled(false);
    useModifyButtons();
  }

  /**
   *  Method to set screen for modifying.
   *
   *  @param sarray User to be modified.
   */
  public void setModify(Object[] sarray)
  {
    mzMultipleUserMod = true;
    masUserArray = (String[])sarray;
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

    if (mzMultipleUserMod == true || mpUserID.length() > 0) // we are modifying
    {
      EmployeeData vpEmpData = null;   // fill in current item data

      if(mzMultipleUserMod == false)
      {
        mpTxtUserID.setText(mpUserID);
        try
        {
          vpEmpData = mpUserServer.getEmployeeData(mpUserID);
        }
        catch (DBException e2)
        {
          displayError("Unable to get User data");
          return;
        }
        setData(vpEmpData);
        mpTxtUserName.requestFocus();
      }
      else
      {
        mpBtnClear.setVisible(false);
        mpTxtUserID.setText("Multiple");
        mpTxtUserName.setText("Multiple Users - Modify Role");
        mpTxtUserName.setEnabled(false);
        setInputVisible(mpPassword1, false);
        setInputVisible(mpPassword2, false);
        setInputVisible(mpExpiryPanel, false);
        setInputVisible(mpLanguage, false);
        setInputVisible(mpRememberLastLogin, false);
        setInputVisible(mpPSChecker, false);
      }
      mpTxtUserID.setEnabled(false);

    }
    else
    {
      setData(mpDefaultEmployeeData);
      mpDateExpire.setDate();
      mpCBEnableExpireDate.setSelected(false);
    }
  }

  /**
   *  Method to initialize screen components. This adds the components to the
   *  screen and adds listeners as needed.
   *
   *  @exception Exception
   */
  private void jbInit()
  {
    /*
     * Build the data panel input column
     */
    mpTxtUserID = new SKDCTextField(EmployeeData.USERID_NAME);
    mpTxtUserName = new SKDCTextField(EmployeeData.USERNAME_NAME);
    mpCBChangePassword = new SKDCCheckBox();
    mpPassword1 = new JPasswordField(PASSWORD_LENGTH);
    mpPassword2 = new JPasswordField(PASSWORD_LENGTH);
    mpPSChecker = Factory.create(PasswordStrengthChecker.class, mpPassword1,
        mpUserServer);
    mpComboRoles = new SKDCComboBox();
    mpReleaseToCode = new ReleaseToCodeField();
    mpDateExpire = new SKDCDateField(true);
    mpCBEnableExpireDate = new SKDCCheckBox();
    mpLanguage = new SKDCComboBox(DacTranslator.getLanguages());
    try
    {
      mpRememberLastLogin = new SKDCTranComboBox(EmployeeData.REMEMBERLASTLOGIN_NAME);
    }
    catch (NoSuchFieldException nsfe)
    {
      logAndDisplayException(nsfe);
    }

    mpExpiryPanel = new JPanel();
    mpExpiryPanel.add(mpCBEnableExpireDate);
    mpExpiryPanel.add(mpDateExpire);

    addInput("User ID:", mpTxtUserID);
    addInput("User Name:", mpTxtUserName);
    addInput("Role:", mpComboRoles);
    addInput("Password:", mpPassword1);
    addInput("Re-enter Password:", mpPassword2);
    addInput("Password Strength", mpPSChecker);
    addInput("Password Expiration:", mpExpiryPanel);
    if (ReleaseToCodeField.useReleaseToCode())
      addInput("Release-To Code:", mpReleaseToCode);
    if (DacTranslator.getLanguages().size() > 1)
      addInput("Language:", mpLanguage);
    addInput("Remember Login Name", mpRememberLastLogin);

    useAddButtons();

    rolesFill("");
  }

  /**
   *  Method to clean up as needed at closing.
   */
  @Override
  public void cleanUpOnClose()
  {
    if (mpUserServer != null)
    {
      mpUserServer.cleanUp();
      mpUserServer = null;
    }
  }

  /**
   * Method to populate the role combo box.
   *
   * @param srch Name to match.
   */
  void rolesFill(String srch)
  {
    try
    {
      mpComboRoles.setComboBoxData(mpUserServer.getRoleNameList(srch));
    }
    catch (DBException e)
    {
      displayError("Unable to get Roles");
    }
    mpComboRoles.setSelectedIndex(0);
  }

  /**
   * Method for the Submit button.
   */
  @Override
  protected void okButtonPressed()
  {
    String vsRole = mpComboRoles.getSelectedItem().toString();
    if(mzMultipleUserMod)
    {
      modifyRoleForMultipleUsers(vsRole);
    }
    else
    {
      updateSingleUser(vsRole);
      clearButtonPressed();
    }
  }

  /**
   * Modify the Role for multiple users
   * @param isRole
   */
  private void modifyRoleForMultipleUsers(String isRole)
  {
    int userCount = masUserArray.length;
    for (int i = 0; i < masUserArray.length; i++)
    {
      try
      {
        if (!SKDCUserData.isSuperUser() &&
            mpUserServer.isEmployeeSuperUser(masUserArray[i]))
        {
          displayError("Not authorized to modify employee " + masUserArray[i]);
          userCount--;
          continue;
        }
        if (!mzAdding && SKDCUserData.isSuperUser() &&
            mpUserServer.isEmployeeSuperUser(masUserArray[i]))
        {
          displayError("Cannot Change Role for user " + masUserArray[i]);
          userCount--;
          continue;
        }
      }
      catch (DBException e3)
      {
        e3.printStackTrace(System.out);
        displayError("Error checking authorization ");
        cleanUpOnClose();
        close();
      }
      try
      {
        if(masUserArray[i] != null)
        {
          if (((masUserArray[i].equals(SKDCConstants.USER_DAC_SUPERUSER)) ||
               (masUserArray[i].equals(SKDCConstants.USER_DAC_SUPPORT)) ||
               (masUserArray[i].equals(SKDCConstants.USER_ADMINISTRATOR))) &&
              !(isRole.equals(SKDCConstants.ROLE_DAC_SUPERROLE)))
          {
            // Don't let them change the role for su, Administrator, and
            // DAC users
            // Just update set it properly and go for other updates
            displayError("Cannot Change Role for user: " + masUserArray[i]);
          }
          else
          {
            mpUserServer.updateEmployeeRole(masUserArray[i], isRole);
          }
        }
      }
      catch (DBException e2)
      {
        e2.printStackTrace(System.out);
        displayError("Error Modifying Multiple User Roles ");
        cleanUpOnClose();
        close();
      }
    }
    changed();
    displayInfoAutoTimeOut(userCount + " Users updated");
  }

  /**
   * Add or modify a single user.
   *
   * @param isRole
   */
  private void updateSingleUser(String isRole)
  {
    boolean userExists;
    try
    {
      userExists = mpUserServer.employeeExists(mpTxtUserID.getText());
    }
    catch (DBException e2)
    {
      displayError("Unable to get User data");
      return;
    }

    if (mzAdding && userExists)
    {
      displayError("User " + mpTxtUserID.getText() + " already exists");
      return;
    }

    if (!mzAdding && !userExists)
    {
      displayError("User " + mpTxtUserID.getText() + " does not exist");
      return;
    }

    try
    {
      if (!mzAdding && !SKDCUserData.isSuperUser()
          && mpUserServer.isEmployeeSuperUser(mpTxtUserID.getText()))
      {
        displayError("Not authorized to modify this employee");
        return;
      }
    }
    catch (DBException e2)
    {
      e2.printStackTrace(System.out);
      displayError("Error checking authorization ");
      cleanUpOnClose();
      close();
    }

    EmployeeData vpEmployeeData = Factory.create(EmployeeData.class);
    // fill in Employee data

    if (mzAdding)
    {
      vpEmployeeData.setUserID(mpTxtUserID.getText());
      if (vpEmployeeData.getUserID().length() <= 0)  // required
      {
        displayError("User name is required");
        return;
      }
    }
    vpEmployeeData.setUserID(mpTxtUserID.getText());
    vpEmployeeData.setUserName(mpTxtUserName.getText());
    vpEmployeeData.setReleaseToCode(mpReleaseToCode.getText());
    vpEmployeeData.setLanguage(mpLanguage.getText());
    try
    {
      vpEmployeeData.setRememberLastLogin(mpRememberLastLogin.getIntegerValue());
    }
    catch (NoSuchFieldException nsfe)
    {
      logAndDisplayException(nsfe);
      return;
    }

    /*
     * Check the password
     */
    if (mzAdding || mpCBChangePassword.isSelected())
    {
      // make sure that the password length is not exceeded
      if ((mpPassword1.getPassword().length > PASSWORD_LENGTH) ||
          (mpPassword2.getPassword().length > PASSWORD_LENGTH))
      {
        displayError("Password size is a maximum of " + PASSWORD_LENGTH);
        return;
      }

      // make sure that the passwords match, if they are entered
      if (((mpPassword1.getPassword().length > 0) ||
           (mpPassword2.getPassword().length > 0)) &&
        !new String(mpPassword1.getPassword()).equals(new String(mpPassword2.getPassword())))
      {
        displayError("Password entries do not match");
        return;
      }

      vpEmployeeData.setPassword(new String(mpPassword1.getPassword()));
    }

    if (mpCBEnableExpireDate.isSelected())
    {
      vpEmployeeData.setPasswordExpiration(mpDateExpire.getDate());
    }
    else
    {
      vpEmployeeData.setPasswordExpiration(null);
    }

    if (((vpEmployeeData.getUserID().equals(SKDCConstants.USER_DAC_SUPERUSER)) ||
         (vpEmployeeData.getUserID().equals(SKDCConstants.USER_DAC_SUPPORT))) &&
        !(isRole.equals(SKDCConstants.ROLE_DAC_SUPERROLE)))
    {
      // Don't let them change the role for DAC users
      displayInfoAutoTimeOut("The " + vpEmployeeData.getUserID()
          + " user must have the " + SKDCConstants.ROLE_DAC_SUPERROLE
          + " role.");
      return;
    }
    else if (vpEmployeeData.getUserID().equals(SKDCConstants.USER_ADMINISTRATOR)
        && !(isRole.equals(SKDCConstants.ROLE_ADMINISTRATOR)))
    {
      // Don't let them change the role for Administrator user
      displayInfoAutoTimeOut("The " + SKDCConstants.USER_ADMINISTRATOR
          + " user must have the " + SKDCConstants.ROLE_ADMINISTRATOR
          + " role.");
      return;
    }
    else
    {
      vpEmployeeData.setRole(isRole);
    }
    if ((vpEmployeeData.getRole() == null) ||
        (vpEmployeeData.getRole().trim().length() == 0))
    {
        displayError("Role is required");
        return;
    }

    try
    {
      if (mzAdding)
      {
        mpUserServer.addEmployee(vpEmployeeData);
        changed(null, vpEmployeeData);
        displayInfoAutoTimeOut("User " + mpTxtUserID.getText() + " added");
      }
      else
      {
        mpUserServer.updateEmployeeInfo(vpEmployeeData);
        changed(null, vpEmployeeData);
        displayInfoAutoTimeOut("User " + mpTxtUserID.getText() + " updated");
      }
    }
    catch (DBException e2)
    {
      logAndDisplayException(e2);
      return;
    }
    if (!mzAdding)
    {
      cleanUpOnClose();
      close();
    }
  }

  /**
   * Method for the Clear button.
   */
  @Override
  protected void clearButtonPressed()
  {
    setData(mpDefaultEmployeeData);
    mpPSChecker.updateProgressBar(0);
    mpComboRoles.setSelectedIndex(0);
    mpTxtUserID.requestFocus();
  }

  /**
   *  Method to refresh screen fields.
   *
   *  @param ud User data to use in refreshing.
   */
  void setData(EmployeeData ud)
  {
    mpTxtUserID.setText(ud.getUserID());
    mpTxtUserName.setText(ud.getUserName());
    mpPassword1.setText(ud.getPassword());
    mpPassword2.setText(ud.getPassword());
    mpComboRoles.setSelectedItem(ud.getRole());
    mpReleaseToCode.setText(ud.getReleaseToCode());
    Date expDate = ud.getPasswordExpiration();
    mpDateExpire.setDate(expDate);
    if (expDate == null)
    {
      mpCBEnableExpireDate.setSelected(false);
    }
    else
    {
      mpCBEnableExpireDate.setSelected(true);
    }
    mpLanguage.setSelectedItem(ud.getLanguage());
    try
    {
      mpRememberLastLogin.setSelectedElement(ud.getRememberLastLogin());
    }
    catch (NoSuchFieldException e)
    {
      logAndDisplayException(e);
    }
  }

  /**
   * Method for the Close button.
   */
  @Override
  protected void closeButtonPressed()
  {
    cleanUpOnClose();
    close();
  }
}