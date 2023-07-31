package com.daifukuamerica.wrxj.swingui.main;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dataserver.standard.StandardUserServer;
import com.daifukuamerica.wrxj.dbadapter.data.EmployeeData;
import com.daifukuamerica.wrxj.dbadapter.data.SysConfig;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.JDBCConnectionImpl;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCGUIConstants;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SpringUtilities;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.WrxjVersion;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SpringLayout;


/**
 * A screen class for user login validation.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class Login extends JDialog implements SKDCGUIConstants
{
  protected SKDCTextField mpTextFieldUser;
  protected JPasswordField mpPasswordField;
  
  protected SKDCButton mpButtonClear;
  protected SKDCButton mpButtonOK;
  protected SKDCButton mpButtonExit;

  protected String msUserName = "";
  protected boolean mzLoggedIn = false;
  protected boolean pressedOK = false;
  protected boolean mzExitProgram = false;
  protected String msMachine = "Unknown";
  protected String msIPAddress = "Unknown";

  /**
   * Create login screen class.
   * 
   * @param ipFrame Parent frame.
   * @param isTitle Title to be displayed.
   * @param izModal True if it is to be modal.
   */
  public Login(Frame ipFrame, String isTitle, boolean izModal)
  {
    super(ipFrame, isTitle, izModal);
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);

    msMachine = Application.getString(SKDCConstants.MACHINE_NAME);
    msIPAddress = Application.getString(SKDCConstants.IPADDRESS_NAME);
    
    try
    {
      jbInit();
      pack();
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }

    // Auto-add the user name if one was supplied
    String vsLogin = Application.getString("AutoUser");
    if (vsLogin != null)
    {
      mpTextFieldUser.setText(vsLogin);
    }
  }

  /**
   * @see java.awt.Container#getPreferredSize()
   */
  @Override
  public Dimension getPreferredSize()
  {
    Dimension vpDim = super.getPreferredSize();
    vpDim.width += 25;
    return vpDim;
  }
  
  /**
   * Method to display an Informational message in an option pane.
   * 
   * @param prompt Text to be displayed.
   */
  public boolean displayYesNoPrompt(String prompt)
  {
    int vnResponse = JOptionPane.showConfirmDialog(null,
        prompt + "?", "Question", JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE);
    return (vnResponse == JOptionPane.YES_OPTION);
  }
   
  /**
   * Method to allow parent frame to get user name.
   * 
   * @return string containing user name.
   */
  public String getUserName()
  {
    return msUserName;
  }

  /**
   * Method to allow parent frame to get the Machine Name.
   * 
   * @return string containing Machine Name.
   */
  public String getMachineName()
  {
    return msMachine;
  }

  /**
   * Method to allow parent frame to get the IP Address.
   * 
   * @return string containing IP Address.
   */
  public String getIPAddress()
  {
    return msIPAddress;
  }

  /**
   * Method to allow parent frame to know if user login was good.
   * 
   * @return boolean of <code>true</code> if it was good.
   */
  public boolean loggedIn()
  {
    return mzLoggedIn;
  }

  /**
   * Method to allow parent frame to know if user hit the Exit button.
   * 
   * @return boolean of <code>true</code> if Exit button was pressed.
   */
  public boolean isExiting()
  {
    return mzExitProgram;
  }

  /**
   * Initialize components that will be placed in the dialog
   */
  protected void initializeComponents()
  {
    mpTextFieldUser = new SKDCTextField(EmployeeData.USERID_NAME);
    mpTextFieldUser.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          okButtonPressed();
        }
      });

    mpPasswordField = new JPasswordField(8);
    mpPasswordField.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          okButtonPressed();
        }
      });
    
    mpButtonOK = new SKDCButton("OK");
    mpButtonOK.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          okButtonPressed();
        }
      });
    
    mpButtonClear = new SKDCButton("Clear");
    mpButtonClear.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          clearButtonPressed();
        }
      });
    
    mpButtonExit = new SKDCButton("Exit");
    mpButtonExit.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          exitButtonPressed();
        }
      });

  }
  
  /**
   * Method to initialize screen components. This adds the components to the
   * screen and adds listeners as needed.
   * 
   * @exception Exception
   */
  protected void jbInit() throws Exception
  {
    initializeComponents();
    
    setResizable(false);

    SKDCLabel vpPasswordLabel = new SKDCLabel("Password:");
    SKDCLabel vpUserLabel = new SKDCLabel("Login Name:");
    SKDCLabel vpLabelImage = new SKDCLabel();

    // Use a custom logo if there is one otherwise use our logo
    String vsLoginImage = Application.getString("LoginGraphic");
    if (vsLoginImage == null)
    {
      vsLoginImage = "/graphics/CompanyLogo.png";
    }
    URL url = Login.class.getResource(vsLoginImage);
    if (url == null)
    {
      JOptionPane.showMessageDialog(getParent(), "Login could not find graphics");
      System.exit(1);
    }
    vpLabelImage.setIcon(new ImageIcon(url));
    JPanel vpLogoPanel = new JPanel();
    vpLogoPanel.add(vpLabelImage);

    JPanel vpInputPanel = new JPanel(new SpringLayout());
    vpUserLabel.setHorizontalAlignment(SKDCLabel.TRAILING);
    vpInputPanel.add(vpUserLabel);
    vpUserLabel.setLabelFor(mpTextFieldUser);

    mpTextFieldUser.setText(Factory.create(SysConfig.class).rememberLastLogin(msMachine));
    vpInputPanel.add(mpTextFieldUser);
    vpPasswordLabel.setHorizontalAlignment(SKDCLabel.TRAILING);
    vpInputPanel.add(vpPasswordLabel);
    vpPasswordLabel.setLabelFor(mpPasswordField);
    vpInputPanel.add(mpPasswordField);
    
    SpringUtilities.makeCompactGrid(vpInputPanel, vpInputPanel.getComponentCount()/2, 2, 10, 10, 10, 15);

    JPanel vpButtonPanel = new JPanel();
    vpButtonPanel.add(mpButtonOK);
    vpButtonPanel.add(mpButtonClear);
    vpButtonPanel.add(mpButtonExit);
    ((FlowLayout)vpButtonPanel.getLayout()).setHgap(10);

    JPanel vpLoginPanel = new JPanel(new BorderLayout());
//    vpLogin.setBorder(new EtchedBorder());
    vpLoginPanel.add(vpInputPanel, BorderLayout.CENTER);
    vpLoginPanel.add(vpButtonPanel, BorderLayout.SOUTH);
    
    JPanel vpCopyright = new JPanel();
    vpCopyright.add(new SKDCLabel("<HTML><FONT SIZE=-2>" 
        + WrxjVersion.getCopyrightString() + "</FONT></HTML>"));
    
    getContentPane().add(vpLogoPanel, BorderLayout.NORTH);
    getContentPane().add(vpLoginPanel, BorderLayout.CENTER);
    getContentPane().add(vpCopyright, BorderLayout.SOUTH);
  }

  /**
   * Overridden method so we can move frame to front.
   * 
   * @param vis True or false, depending on caller.
   */
  @Override
  public void setVisible(boolean vis)
  {
    if (vis)
    {
      super.toFront();
    }
    super.setVisible(vis);
  }

  /**
   * Overridden method so we can exit when window is closed.
   * 
   * @param e Window event
   * 
   */
  @Override
  protected void processWindowEvent(WindowEvent e) {
    if (e.getID() == WindowEvent.WINDOW_CLOSING)
    {
//      System.out.println("Login: WINDOW_CLOSING" );
      mzLoggedIn = false;
      mzExitProgram = true;
      this.firePropertyChange(FRAME_CLOSING,"Old","New");
      return;
    }
    else if (e.getID() == WindowEvent.WINDOW_DEACTIVATED)
    {
//      System.out.println("Login: WINDOW_DEACTIVATED" );
      return;
    }
    super.processWindowEvent(e);
  }

  /**
   * Action method to handle OK button. Verifies that the user login is valid,
   * password is acceptable, then fires property change event so parent frame
   * can do the actual login.
   */
  protected void okButtonPressed()
  {
                // The user can hit return 3-4 times before the screen gets done logging in
                // so catch the extra hits and just get out and let the login complete.
    if(pressedOK == true)
    {
//      System.out.println("OK Button pressed again...but getting out");
      return;
    }
    else
    {
      pressedOK = true;
    }
    int vdLoginState;
    
    if (mpTextFieldUser.getText().length() > 0)
    {
      String pWord = new String(mpPasswordField.getPassword());
      mpPasswordField.setText("");
      msUserName = mpTextFieldUser.getText();
      try
      { 
        StandardUserServer userServ = Factory.create(StandardUserServer.class, "Login");
        vdLoginState = userServ.validateLogin(msUserName, pWord, 
            msMachine, msIPAddress);
        if (vdLoginState == StandardUserServer.LOGIN_IN_USE)
        {
          String vsTxt = "User: " + msUserName;
          if(displayYesNoPrompt(vsTxt + " is already in use, do you need to replace that login"))
          {
            userServ.logOut(msUserName, msMachine);
            vdLoginState = userServ.validateLogin(msUserName, pWord, 
                msMachine, msIPAddress);
            if (vdLoginState != StandardUserServer.LOGIN_OKAY)
            {
              displayError("Unable to Replace Login");
              pressedOK = false;
            }
          }
        }
      }
      catch (DBException e2)
      {
        pressedOK = false;
        if (!DBObject.isWRxJConnectionActive())
        {
          displayWarning("Unable to connect to database");
          mzLoggedIn = false;
          mzExitProgram = true;
          this.firePropertyChange(FRAME_CLOSING,"Old","New");
        }
        else
        {
          displayError("Unable to get user data");
        }
        return;
      }

      if (!DBObject.isWRxJConnectionActive())
      {
        displayWarning("Unable to connect to database");
        mzLoggedIn = false;
        mzExitProgram = true;
        this.firePropertyChange(FRAME_CLOSING,"Old","New");
      }
      else
      {
        if (JDBCConnectionImpl.getConnectionFailed())
        {
          displayWarning(JDBCConnectionImpl.getConnectionFailureReason());
        }
        else
        {
          switch (vdLoginState)
          {
            case StandardUserServer.LOGIN_OKAY:      // login okay
              break;
            case StandardUserServer.LOGIN_INVALID:   // invalid login
              displayWarning("Invalid login");
              pressedOK = false;
              return;
            case StandardUserServer.LOGIN_EXPIRED:   // Password expired
              displayWarning("Password Expired");
              pressedOK = false;
              return;
            case StandardUserServer.LOGIN_IN_USE:    // Device In Use
              displayWarning("User: " + msUserName + " in use at " + msMachine);
              pressedOK = false;
              return;
            default:   // Unknown Login Error
              displayWarning("Login Error...please Try Again");
              pressedOK = false;
              return;
          }
        }
      }
      mzLoggedIn = true;
      firePropertyChange(FRAME_CLOSING,"Old","New");
    }
    else
    {
      displayWarning("Login name must be entered");
      pressedOK = false;
    }
  }

  /**
   * Action method to handle Clear button. Reset fields to default values.
   */
  protected void clearButtonPressed()
  {
    mpTextFieldUser.setText("");
    mpPasswordField.setText("");
  }

  /**
   * Action method to handle Close button. Fires a property change event for the
   * parent frame.
   * 
   */
  protected void exitButtonPressed()
  {
    mzLoggedIn = false;
    mzExitProgram = true;
    this.firePropertyChange(FRAME_CLOSING, "Old", "New");
  }

  /**
   * Method to display an Error message in an option pane.
   * 
   * @param s Text to be displayed.
   */
  private void displayError(String s)
  {
    JOptionPane.showMessageDialog(this, s, "Error", JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Method to display a Warning message in an option pane.
   * 
   * @param s Text to be displayed.
   */
  private void displayWarning(String s)
  {
    JOptionPane.showMessageDialog(null, s, "Warning",
        JOptionPane.WARNING_MESSAGE);
  }
}