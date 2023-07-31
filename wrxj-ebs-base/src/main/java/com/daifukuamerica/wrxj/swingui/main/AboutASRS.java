package com.daifukuamerica.wrxj.swingui.main;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBMetaData;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.util.WrxjVersion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EtchedBorder;

/**
 * A screen class that displays basic version information about the system.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class AboutASRS extends JDialog implements ActionListener
{
  private static String ABOUT_SHOW_SUPPORT  = "About.ShowSupportTabForAllRoles";
  private static String ABOUT_CALLCENTER_P1 = "About.CallCenterPhoneTollFree";
  private static String ABOUT_CALLCENTER_EM = "About.CallCenterEMail";

  protected String msProduct = "Warehouse Rx";

  SKDCButton mpBtnOK;

  /**
   * Create about screen class.
   *
   * @param frame Parent frame.
   * @param title Title to be displayed.
   * @param modal True if it is to be modal.
   */
  public AboutASRS(Frame frame, String title, boolean modal)
  {
    super(frame, title, modal);
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    pack();
  }

  /**
   * Method to initialize screen components.
   *
   * @exception Exception
   */
  protected void jbInit() throws Exception
  {
    setTitle("About " + msProduct);
    setResizable(false);

    // Warehouse Rx info
    JPanel vpTextPanel = new JPanel(new GridBagLayout());
    GridBagConstraints vpGBC = new GridBagConstraints();
    vpGBC.gridx = 0;
    vpGBC.fill = GridBagConstraints.BOTH;
    vpGBC.anchor = GridBagConstraints.WEST;
    vpGBC.insets = new Insets(2, 2, 2, 2);

    SKDCLabel vpWarehouseRx = new SKDCLabel(" " + msProduct + "®");
    vpWarehouseRx.setFont(vpWarehouseRx.getFont().deriveFont(Font.BOLD));
    vpTextPanel.add(vpWarehouseRx, vpGBC);
    String vsVersion = WrxjVersion.getSoftwareVersion().replace(msProduct + " - ", "");
    vpGBC.insets = new Insets(2, 2, 2, 2);
    vpTextPanel.add(new SKDCLabel(" Version: " + vsVersion), vpGBC);
    vpTextPanel.setBackground(Color.WHITE);

    JPanel vpNorthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    vpNorthPanel.setBackground(Color.WHITE);
    vpNorthPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
    vpNorthPanel.add(vpTextPanel);

    // Additional information
    JTabbedPane vpTabbedPane = new JTabbedPane();
    vpTabbedPane.add("About", getAboutPanel());
    vpTabbedPane.add("Build", getBuildInfo());
    vpTabbedPane.add("Database", getDatabaseInfo());
    vpTabbedPane.add("Java", getJavaInfo());
    if (Application.getBoolean(ABOUT_SHOW_SUPPORT, false) ||
        SKDCUserData.isAdministrator() ||
        SKDCUserData.isSuperUser())
    {
      vpTabbedPane.add("Support", getSupportInfo());
    }

    // Copyright
    vpGBC.insets = new Insets(8, 2, 10, 2);

    mpBtnOK = new SKDCButton("   OK   ");
    mpBtnOK.addActionListener(this);

    JPanel vpSouthPanel = new JPanel();
    vpSouthPanel.add(mpBtnOK);
    vpSouthPanel.setBorder(new EtchedBorder());

    getContentPane().add(vpNorthPanel, BorderLayout.NORTH);
    getContentPane().add(vpTabbedPane, BorderLayout.CENTER);
    getContentPane().add(vpSouthPanel, BorderLayout.SOUTH);
  }

  /**
   * About Warehouse Rx
   * @return
   */
  protected JPanel getAboutPanel()
  {
    SKDCLabel vpLogoLabel = new SKDCLabel();
    String vsCompanyLogo = Application.getString("LoginGraphic", "/graphics/DACLogo.png");
    vpLogoLabel.setIcon(new ImageIcon(AboutASRS.class.getResource(vsCompanyLogo)));
    JPanel vpLogoPanel = new JPanel();
    vpLogoPanel.add(vpLogoLabel);

    String vsCopyright = " " + WrxjVersion.getCopyrightString() + " ";

    JPanel vpAboutPanel = new JPanel(new BorderLayout());
    vpAboutPanel.setBorder(BorderFactory.createEtchedBorder());
    vpAboutPanel.add(vpLogoPanel, BorderLayout.CENTER);
    vpAboutPanel.add(new SKDCLabel(vsCopyright), BorderLayout.SOUTH);

    return vpAboutPanel;
  }

  /**
   * Get all of the build information for Warehouse Rx
   * @return
   */
  private JPanel getBuildInfo()
  {
    AboutDetailPanel vpPanel = new AboutDetailPanel();

    // Base
    vpPanel.add(WrxjVersion.getVersionId(), WrxjVersion.getBuildTime());

    // Double-Deep (optional)
    if (!WrxjVersion.getDoubleDeepVersionId().equals(WrxjVersion.UNKNOWN))
    {
      vpPanel.add(WrxjVersion.getDoubleDeepVersionId(),
          WrxjVersion.getDoubleDeepBuildTime());
    }

    // Custom (optional)
    if (!WrxjVersion.getCustomVersionId().equals(WrxjVersion.UNKNOWN))
    {
      vpPanel.add(WrxjVersion.getCustomVersionId(),
          WrxjVersion.getCustomBuildTime());
    }

    return vpPanel;
  }

  /**
   * Get all of the current run information for Warehouse Rx
   * @return
   */
  private JPanel getJavaInfo()
  {
    AboutDetailPanel vpPanel = new AboutDetailPanel();

    vpPanel.add("Vendor", System.getProperty("java.vendor"));
    vpPanel.add("Version", System.getProperty("java.version"));

    return vpPanel;
  }

  /**
   * Get all of the current database information for Warehouse Rx
   * @return
   */
  private JPanel getDatabaseInfo()
  {
    AboutDetailPanel vpPanel = new AboutDetailPanel();

    DBMetaData vpDBMetaData = Factory.create(DBMetaData.class);

    vpPanel.add("Vendor", vpDBMetaData.getDatabaseVendorName());
    vpPanel.add("Version", vpDBMetaData.getDatabaseVersion());
    vpPanel.add("Machine", vpDBMetaData.getDatabaseServerName());
    vpPanel.add("Instance", vpDBMetaData.getDatabaseInstanceName());
    vpPanel.add("Started", vpDBMetaData.getDatabaseStartupTime());

    return vpPanel;
  }

  /**
   * Get the support information for Warehouse Rx
   *
   * @return
   */
  private JPanel getSupportInfo()
  {
    AboutDetailPanel vpPanel = new AboutDetailPanel();

    vpPanel.add("24/7 Call Center (toll free)", Application.getString(
        ABOUT_CALLCENTER_P1, "888-996-0099"));
    vpPanel.add("M-F 8am-5pm Central", Application.getString(
        ABOUT_CALLCENTER_EM, "Client.Support@Wynright.com"));

    return vpPanel;
  }

  /**
   * Overridden method so we can exit when window is closed.
   *
   * @param e Window event
   */
  @Override
  protected void processWindowEvent(WindowEvent e)
  {
    if (e.getID() == WindowEvent.WINDOW_CLOSING)
    {
      cancel();
    }
    super.processWindowEvent(e);
  }

  /**
   * Method to close the dialog.
   */
  void cancel()
  {
    dispose();
  }

  /**
   * Method to close the dialog on a button event.
   *
   * @param e Action event
   */
  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource() == mpBtnOK)
    {
      cancel();
    }
  }

  /**
   * <B>Description:</B> Details panel for the About pop-up
   *
   * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
   *
   * @author       mandrus
   * @version      1.0
   */
  private class AboutDetailPanel extends JPanel // implements MouseListener
  {
    private GridBagConstraints mpGBC;

    /**
     * Constructor
     *
     * @param isTitle
     */
    public AboutDetailPanel()
    {
      super(new GridBagLayout());
      setBorder(BorderFactory.createEtchedBorder());
      setLayout(new GridBagLayout());
      mpGBC = new GridBagConstraints();
      mpGBC.insets = new Insets(2, 2, 2, 2);
//      mpGBC.fill = GridBagConstraints.BOTH;
      setVisible(false);
    }

    /**
     * Add details
     */
    public void add(String isLabel, String isValue)
    {
      SKDCLabel vpLabel = new SKDCLabel("<html><b> " + isLabel + ": </b> </html>");
      mpGBC.gridx = 0;
      mpGBC.anchor = GridBagConstraints.EAST;
      add(vpLabel, mpGBC);
      mpGBC.gridx = 1;
      mpGBC.anchor = GridBagConstraints.WEST;
      add(new SKDCLabel(isValue), mpGBC);
    }
  }
}