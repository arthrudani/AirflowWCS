package com.daifukuamerica.wrxj.swingui.main;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.swing.BareBonesBrowserLaunch;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * A screen class that displays basic version information about the system.
 *
 * @author avt
 * @version 1.0
 */

@SuppressWarnings("serial")
public class HelpLauncher extends JDialog implements ActionListener
{
  private static String DOC_DIR = "./doc/";
  private static String ICON_SUFFIX = ".png";
  protected String msProduct = "Warehouse Rx";
  protected Frame mpParentFrame;

  /**
   *  Create about screen class.
   *
   *  @param frame Parent frame.
   *  @param title Title to be displayed.
   *  @param modal True if it is to be modal.
   */
  private HelpLauncher(Frame frame, String title, boolean modal)
  {
    super(frame, title, modal);
    mpParentFrame = frame;
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
   * Try to make a file name look like human-readable text
   * @param isFileName
   * @return
   */
  private String prettyFileName(String isFileName)
  {
    String vsBaseName = isFileName.substring(0, isFileName.indexOf('.'));
    vsBaseName = vsBaseName.replaceAll("_", " ");

    String vsPretty = "" + vsBaseName.charAt(0);
    for (int i = 1; i < vsBaseName.length(); i++)
    {
      if (Character.isLowerCase(vsBaseName.charAt(i-1)) && 
          Character.isUpperCase(vsBaseName.charAt(i)))
      {
        vsPretty += ' ';
      }
      vsPretty += vsBaseName.charAt(i);
    }
    return vsPretty;
  }
  
  /**
   *  Method to initialize screen components.
   *
   *  @exception Exception
   */
  protected void jbInit() throws Exception
  {
    String[] vasHelpFiles = getHelpFiles();
    if (vasHelpFiles == null)
    {
      vasHelpFiles = new String[0];
    }
    
    JPanel vpButtonPanel = new JPanel(new GridLayout(vasHelpFiles.length+1,1));
    
    // Make sure the User Guide is listed first
    String vsUserGuidePath = Application.getString("UserGuide");
    for (String vsHelpFile : vasHelpFiles)
    {
      if (vsUserGuidePath.contains(vsHelpFile))
      {
        vpButtonPanel.add(getHelpFileButton(vsHelpFile));
      }
    }
    
    // List everything else
    for (String vsHelpFile : vasHelpFiles)
    {
      if (!vsUserGuidePath.contains(vsHelpFile))
      {
        vpButtonPanel.add(getHelpFileButton(vsHelpFile));
      }
    }
    
    setTitle(msProduct + " Help Selector");
    setResizable(false);

    // Don't forget the cancel button
    SKDCButton vpCancelButton = new SKDCButton("Cancel");
    vpCancelButton.addEvent(SKDCInternalFrame.CANCEL_BTN, this);
    vpButtonPanel.add(vpCancelButton);

    getContentPane().add(vpButtonPanel, BorderLayout.CENTER);
  }

  /**
   * Get the button for a help file
   * 
   * @param isHelpFile
   * @return
   */
  private SKDCButton getHelpFileButton(String isHelpFile)
  {
    SKDCButton vpButton = new SKDCButton(prettyFileName(isHelpFile));
    vpButton.setIcon(getHelpIcon(isHelpFile));
    vpButton.addEvent(DOC_DIR + isHelpFile, this);
    vpButton.setHorizontalAlignment(SwingConstants.LEFT);
    vpButton.setFont(getFont().deriveFont(Font.BOLD, getFont().getSize()+6));
    return vpButton;
  }
  
  /**
   * Get the icon for a help file (png with same base name)
   * @param isHelpFile
   * @return
   */
  private ImageIcon getHelpIcon(String isHelpFile)
  {
    try
    {
      String vsIconName = isHelpFile.substring(0, isHelpFile.indexOf('.'));
      ImageIcon vpIcon = new ImageIcon(DOC_DIR + vsIconName + ICON_SUFFIX);
      if (vpIcon.getIconHeight() > 0)
      {
        return vpIcon;
      }
      return new ImageIcon(getClass().getResource("/graphics/help.png"));
    }
    catch (Exception e)
    {
      System.out.println("Error finding icon for " + isHelpFile);
      return null;
    }
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
      dispose();
    }
    super.processWindowEvent(e);
  }

  /**
   * Method to close the dialog on a button event.
   * 
   * @param e Action event
   * 
   */
  public void actionPerformed(ActionEvent e)
  {
    if (e.getActionCommand().equals(SKDCInternalFrame.CANCEL_BTN))
    {
      dispose();
    }
    else
    {
      launchHelp(mpParentFrame, e.getActionCommand());
      dispose();
    }
  }
  
  /**
   * Get the help files
   * 
   * @return
   */
  private static String[] getHelpFiles()
  {
    File vpDocDir = new File(DOC_DIR);
    
    FilenameFilter vpFilter = new FilenameFilter() {
        public boolean accept(File ipFile, String isName)
        {
          return isName.endsWith(".pdf") || isName.endsWith(".html");
        }
      };

    return vpDocDir.list(vpFilter);
  }
  
  /**
   * Are there additional help files for the user to choose from?
   * 
   * @return
   */
  public static boolean hasExtraHelpFiles()
  {
    String[] vasHelpDocs = getHelpFiles();
    if (vasHelpDocs == null)
    {
      return false;
    }
    else
    {
      return vasHelpDocs.length > 1;
    }
  }
  
  /**
   * Launch the help chooser
   * 
   * @param ipFrame
   */
  public static void launchHelp(Frame ipFrame)
  {
    if (hasExtraHelpFiles())
    {
      JDialog dlg = new HelpLauncher(ipFrame, "Help", true);
      Dimension dlgSize = dlg.getPreferredSize();
      Dimension frmSize = ipFrame.getSize();
      Point loc = ipFrame.getLocation();
      dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
      dlg.setVisible(true);
    }
    else
    {
      String vsUserGuidePath = Application.getString("UserGuide");
      launchHelp(ipFrame, vsUserGuidePath);
    }
  }
  
  /**
   * Launch a specific help file
   * 
   * @param ipFrame
   * @param isFileName
   */
  private static void launchHelp(Frame ipFrame, String isFileName)
  {
    File vpHelpFile = new File(isFileName);
    if (vpHelpFile.exists())
    {
      BareBonesBrowserLaunch.openURL(vpHelpFile.getAbsolutePath());
    }
    else if (isFileName.startsWith("http"))
    {
      BareBonesBrowserLaunch.openURL(isFileName);
    }
    else
    {
      JOptionPane.showMessageDialog(ipFrame,
          isFileName + " does not exist",
          "Help File Error", JOptionPane.ERROR_MESSAGE);
    }
  }
}