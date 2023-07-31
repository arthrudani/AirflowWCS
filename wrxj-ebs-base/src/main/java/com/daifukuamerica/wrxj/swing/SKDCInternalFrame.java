package com.daifukuamerica.wrxj.swing;

import com.daifukuamerica.wrxj.WarehouseRx;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.device.gateway.SystemGateway;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.MessageService;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.swing.table.DacTable;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.daifukuamerica.wrxj.util.UnreachableCodeException;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 * SK Daifuku Internal frame for screen.
 *
 * <p><b>Details:</b> <code>SKDCInternalFrame</code> is the base class for all
 * internal frames displayed in the application.  This class provides several
 * common services that most other internal frames use.</p>
 *
 * @author       A.T.
 * @version      1.0
 */
@SuppressWarnings("serial")
public abstract class SKDCInternalFrame extends JInternalFrame 
    implements InternalFrameListener, SKDCGUIConstants
{
  private boolean allowDuplicateScreens = false;
  private List<Object> buttonPanels = new ArrayList<Object>();
  private List<Object> parentFrames = new ArrayList<Object>();
  private String menuCategory = "";
  private String menuOption = "";
  private String originalTitle = null;
  private Icon mpJOptionIcon = null;

  /*
   * Info bar
   */
  private Date mpEraseTime = null;
  private int mnTimeOut = 10000;
  private Timer mpClearTimer = null;
  private JPanel mpInfoPanel = null;
  private SKDCLabel mpInfoLabel = new SKDCLabel(" ");
  private boolean mzShowInfoPanel = false;

  /**
   * A List of Integers that are used to assign a unique number to active
   * Monitor Frame Titles (such as: "System Monitor", "System Monitor2",
   * "System Monitor3", etc).
   */
  private static Map<String, List<Object>> instanceKeysMap = new HashMap<String, List<Object>>();
  /**
   * The Integer used to assign a unique number to the Title
   * of an instantiated Monitor Frame.
   */
  protected Integer instanceKey;
  protected String defaultTitle = null;
  /**
   * The name of instantiated Monitor Frame's Local Controller Group.
   */
  protected String groupName = null;

  /**
   * The user inactivity period (in milliseconds) for all components in this frame.
   *
   * @see #actionTimeout(ActionEvent e) actionTimeout(ActionEvent e)
   */
  protected int inactiveTimeOut = 0;
  /**
   * The user inactivity timer for all components in this frame.
   *
   * @see #actionTimeout(ActionEvent e) actionTimeout(ActionEvent e)
   */
  protected javax.swing.Timer activityTimer;
  protected JDesktopPane desktop = null;
  
  private float mfFontSize = 0;
  
  //
  // Logging and MessageService stuff.  <<<=====================================
  //

  protected SystemGateway getSystemGateway()
  {
    return ThreadSystemGateway.get();
  }

  /**
   * Find or create a logger for this named subsystem. If a logger has already been
   * created with the given name it is returned. Otherwise a new logger is created.
   */
  protected Logger logger = null;
  /**
   * The unique identifier for the frame.  We use the name from the instantianted logger.
   */
  protected String keyName = null;

  /**
   *  Create internal frame.
   *
   *  @param isTitle Frame title.
   *  @param izResizable Display resizeable icon.
   *  @param izClosable Display closeable icon.
   */
  public SKDCInternalFrame(String isTitle, boolean izResizable, boolean izClosable)
  {
    super(isTitle, izResizable, izClosable);
    instanceKey = getInstanceKey();

    groupName = Application.getString(WarehouseRx.RUN_MODE);
    String vsClassName = this.getClass().getName();
    int idx = vsClassName.lastIndexOf('.');
    keyName = vsClassName.substring(idx + 1);
    if (instanceKey.intValue() != 0)
    {
      keyName = keyName + instanceKey;
    }
    activateLogger();
    logger = Logger.getLogger(keyName);
    keyName = logger.getLoggerInstanceName();
    //
    setIconifiable(true);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    enableEvents(AWTEvent.ACTION_EVENT_MASK);
    addInternalFrameListener(this);
    setCloseOnCtrlF4(true); // default state
    //
    // Logging and MessageService stuff.  <<<=================================
    //
    createAndStartupSystemGateway();
  }

  /**
   *  Create internal frame with title.
   *
   *  @param isTitle Frame title.
   */
  public SKDCInternalFrame(String isTitle)
  {
    this(isTitle, true, true);
  }

  /**
   *  Create internal frame.
   *
   */
  public SKDCInternalFrame()
  {
    this("");
  }

  /**
   * I need a JavaDoc!
   */
  private void activateLogger()
  {
    if (Logger.getDefaultLoggerKey() == null)
    {
      Logger.setDefaultLoggerKey(keyName);
    }
  }
  
  /**
   * I need a JavaDoc!
   */
  public String getLogName()
  {
    return keyName;
  }

  /**
   * Method to do needed cleanup on close. May be overridden by classes that
   * extend this class.
   *
   * <P><B>NOTE:</B> This method is called during internalFrameClosing(), which
   * is NOT called if the internal frame never opens.</P>
   */
  public void cleanUpOnClose()
  {
  }

  /**
   * Method to update this frames title.
   * 
   * @param isTitle String containing new title for frame
   */
  @Override
  public void setTitle(String isTitle)
  {
    defaultTitle = DacTranslator.getTranslation(isTitle);

    if (originalTitle == null)
      originalTitle = defaultTitle;

    if (isTitle != null)
    {
      if (instanceKey != null && instanceKey.intValue() != 0)
      {
        defaultTitle = isTitle + instanceKey;
      }
      new SKDCTaskBar().updateTaskBarTaskTitle(this, defaultTitle);
    }
    super.setTitle(defaultTitle);
  }
  
  /**
   * I need a JavaDoc!
   */
  public String getOriginalTitle()
  {
    return originalTitle;
  }

  /**
   * Method to add or remove the key listener for the Ctrl-F4. We may not want
   * some screens to close on this key set.
   *
   * @param allowed ?
   */
  public void setCloseOnCtrlF4(boolean allowed)
  {
    if (allowed)
    {
      this.addKeyListener(new KeyAdapter()
      {
        @Override
        public void keyTyped(KeyEvent e)
        {
          if ((e.getKeyCode() == KeyEvent.VK_CONTROL) &&
             (e.getKeyCode() == KeyEvent.VK_F4))
          {
            close();
          }
        }
      });
    }
    else
    {
      KeyListener[] kl = this.getKeyListeners();
      if (kl == null) return;
      for (int i = kl.length; i > 0; i--)
      {
        removeKeyListener(kl[i-1]);
      }
    }
  }

  /**
   * I need a JavaDoc!
   */
  @Override
  public void internalFrameClosing(InternalFrameEvent e)
  {
    inactiveTimeOut = 0;
    for (int i = 0;  i < buttonPanels.size(); i++)
    {
      enablePanelComponents((JPanel)buttonPanels.get(i), true);
    }
    this.setParentTableEnabled();
    setParentCloseable(true);
    cleanUpOnClose();
    new SKDCTaskBar().removeFromTaskBar(this);
    firePropertyChange(FRAME_CLOSING,null,"New");
    desktop = getDesktopPane();
    if (desktop != null)
    {
      desktop.remove(this);
      desktop.repaint();
    }
  }

  /**
   * Performs clean-up after frame is closed.
   *
   * <p><b>Details:</b> <code>internalFrameClosed</code> is called by Swing
   * after the user has closed this frame.  This method responds by performing
   * view- and back end-<wbr>related clean-<wbr>up.</p>
   *
   * <p>This implementation shuts down the system gateway if one has been
   * instantiated for this frame.</p>
   *
   * @param e ignored
   */
  @Override
  public void internalFrameClosed(InternalFrameEvent e)
  {
    //
    // Logging and MessageService stuff.  <<<=================================
    //
    if (logger != null)
      logger.logDebug("SKDCInternalFrame.internalFrameClosed()");
    uncreate();
  }

  /**
   * If we select a frame from the main menu a frame will ALWAYS be created.
   * BUT, if that frame already exists and does NOT allow duplicates the newly
   * created un-allowable duplicate frame will be discarded (without ever being
   * opened).  So, we use this routine to undo what happened during creation. 
   */
  public void uncreate()
  {
    shutdownFrame();

    logger = null;
    keyName = null;
    menuCategory = null;
    menuOption = null;
    desktop = null;
    buttonPanels = null;
    parentFrames = null;
    super.setTitle(null);
    
    //
    // We probably sucked up a lot of memory, so let's try and free it quickly.
    //
    System.gc();
  }

  /**
   * Performs initialization before displaying frame.
   *
   * <p><b>Details:</b> <code>internalFrameOpened</code> is called by Swing just
   * before displaying this frame.  This method responds by performing various
   * initialization activities that could not be performed in the
   * constructor.</p>
   *
   * <p>This implementation creates a system gateway if one is required for the
   * frame.  Whether or not a system gateway is required is determined by
   * <code>getSystemGatewayNeeded</code>.</p>
   *
   * @param e ignored
   */
  @Override
  public void internalFrameOpened(InternalFrameEvent e)
  {
    if (logger != null)
      logger.logDebug("SKDCInternalFrame.internalFrameOpened()");

    /*
     * Configurable font-sizing
     */
    String vsKey = getTitle()+"-FontSize";
    vsKey = vsKey.replaceAll(" ", "_");
    float vfFontSize = Application.getFloat(vsKey);
    if (vfFontSize > 0)
    {
      setFontSize(vfFontSize);
    }
    float vfButtonSize = Application.getFloat("ButtonHeightRatio");
    if (vfButtonSize > 0)
    {
      setButtonSize(vfButtonSize);
    }
  }

  @Override
  public void internalFrameIconified(InternalFrameEvent e)
  {
    setVisible(false);
  }

  @Override
  public void internalFrameDeiconified(InternalFrameEvent e)
  {
    setVisible(true);
  }

  @Override
  public void internalFrameActivated(InternalFrameEvent e)
  {
    activateLogger();
    new SKDCTaskBar().selectTaskBarButton(this.getTitle());
    setVisible(true);
  }

  @Override
  public void internalFrameDeactivated(InternalFrameEvent e) {}

  /**
   *  Method to set whether or not duplicate screens are allowed. If not allowed
   *  then existing copy of this frame will be moved to the screen front.
   *
   *  @param duplAllow Allow duplicates.
   */
  public void setAllowDuplicateScreens(boolean duplAllow)
  {
    allowDuplicateScreens = duplAllow;
  }

  /**
   *  Method to see whether or not duplicate screens are allowed.
   *
   *  @return if true, allow duplicates.
   */
  public boolean getAllowDuplicateScreens()
  {
    return allowDuplicateScreens;
  }

  /**
   *  Method to fire a frame change event.
   */
  public void changed()
  {
    firePropertyChange(FRAME_CHANGE, null, "New");
  }

  /**
   *  Method to fire a frame change event.
   *
   *  @param oldValue Object containing old value to be included in event.
   *  @param newValue Object containing new value to be included in event.
   */
  public void changed(Object oldValue, Object newValue)
  {
    firePropertyChange(FRAME_CHANGE, oldValue, newValue);
  }

  /**
   *  Method to close this frame.
   */
  public void close()
  {
    try
    {
      setClosed(true);
    }
    catch(PropertyVetoException e)
    {
      e.printStackTrace();
    }
  }

  /**
   *  Method to set this frames menu category and option.
   *
   *  @param cat Menu category.
   *  @param opt Menu option.
   */
  public void setCategoryAndOption(String cat, String opt)
  {
    clearCategoryAndOption();
    menuCategory = cat;
    menuOption = opt;
  }

  /**
   *  Method to clear this frames menu category and option.
   */
  public void clearCategoryAndOption()
  {
    menuCategory = "";
    menuOption = "";
  }

  /**
   *  Method to get this frames menu category.
   *
   *  @return String containing menu category.
   */
  public String getCategory()
  {
    return (menuCategory);
  }

  /**
   *  Method to get this frames menu option.
   *
   *  @return String containing menu option.
   */
  public String getOption()
  {
    return (menuOption);
  }

  /**
   *  Specify the user inactivity period for all components in this frame. Find 
   *  all SKDC components in this frame and set the components' user inactivity 
   *  timers as needed. Timeout values greater than zero add timeout listeners, 
   *  sets the timer values and enables the timers. A timeout values of zero 
   *  disables the timers.
   *
   *  @param seconds Number of seconds to set timers to.
   */
  public void setTimeout(int seconds)
  {
    inactiveTimeOut = seconds;

    JPanel jp = (JPanel)this.getRootPane().getContentPane().getComponent(0);

    for (int x=0; x < jp.getComponentCount(); x++)
    {
      Object tmpObj = jp.getComponent(x);
      if (tmpObj instanceof JPanel)
      { // look through it components
        JPanel jp2 = (JPanel) jp.getComponent(x);
        for (int y=0; y < jp2.getComponentCount(); y++)
        {
          Object tmp = jp2.getComponent(y);
          if (seconds > 0)
          {
            addMyListeners(tmp);
            setTimerInstance(tmp,true);
          }
          else  // set timing off
          {
            setTimerInstance(tmp,false);
          }
        }
      }
      else
      {
        if (seconds > 0)
        {
          addMyListeners(tmpObj);
          setTimerInstance(tmpObj,true);
        }
        else  // set timing off
        {
          setTimerInstance(tmpObj,false);
        }
      }
    }
    if (seconds > 0)
    {
      // now start the timer on this component
      activityTimer = new javax.swing.Timer(inactiveTimeOut*1000,null);
      activityTimer.addActionListener(new java.awt.event.ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          activityTimer.stop();
          actionTimeout(e);
        }
      });
      activityTimer.start();
    }
  }

  /**
   *  Method activated when the user inactivity timer expires. Displays informational message then
   *  closes this frame.
   *
   *  @param e Action event.
   */
  public void actionTimeout(ActionEvent e)
  {
    if (inactiveTimeOut > 0)
    {
      inactiveTimeOut = 0;
      displayInfo("Timed out due to inactivity");
      close();
    }
  }

  /**
   *  Method called to add a property change listener to an SKDC screen component.
   *
   *  @param ob SKDC object to add property change listener to.
   */
  protected void addMyListeners(Object ob)
  {
    // Is there any reason why we can't reduce all this into a single test?
    // (ob isntanceof Component) 
    if
    (    (ob instanceof SKDCTextField)
      || (ob instanceof SKDCDateTextField)
      || (ob instanceof SKDCDateField)
      || (ob instanceof SKDCCalendar)
      || (ob instanceof SKDCIntegerField)
      || (ob instanceof SKDCDoubleField)
      || (ob instanceof SKDCComboBox)
      || (ob instanceof SKDCRadioButton)
      || (ob instanceof SKDCCheckBox)
      || (ob instanceof SKDCButton)
      || (ob instanceof SKDCTranComboBox)
      || (ob instanceof SKDCDateTimeSpinner)
    )
    {
      ((Component) ob).addPropertyChangeListener
      ( FRAME_TIMER_RESTART,
        new java.beans.PropertyChangeListener()
        { @Override
        public void propertyChange(PropertyChangeEvent e)
          { timeoutRestart(e);
          }
        }
      );
    }
  }

  /**
   *  Method called to enable / disable the timer on an SKDC screen component.
   *
   *  @param ob SKDC object to enable / disable.
   *  @param enabled True to enable, false to disable.
   */
  protected void setTimerInstance(Object ob, boolean enabled)
  {
    if (ob == null) return;
    if (ob instanceof SKDCTextField)
        ((SKDCTextField)ob).enableTimer(enabled);
    else if (ob instanceof SKDCDateTextField)
        ((SKDCDateTextField)ob).enableTimer(enabled);
    else if (ob instanceof SKDCIntegerField)
        ((SKDCIntegerField)ob).enableTimer(enabled);
    else if (ob instanceof SKDCDoubleField)
        ((SKDCDoubleField)ob).enableTimer(enabled);
    else if (ob instanceof SKDCComboBox)
        ((SKDCComboBox)ob).enableTimer(enabled);
    else if (ob instanceof SKDCRadioButton)
        ((SKDCRadioButton)ob).enableTimer(enabled);
    else if (ob instanceof SKDCCheckBox)
        ((SKDCCheckBox)ob).enableTimer(enabled);
    else if (ob instanceof SKDCButton)
        ((SKDCButton)ob).enableTimer(enabled);
    else if (ob instanceof SKDCTranComboBox)
        ((SKDCTranComboBox)ob).enableTimer(enabled);
    else if (ob instanceof SKDCDateTimeSpinner)
        ((SKDCDateTimeSpinner)ob).enableTimer(enabled);
    else if (ob instanceof SKDCDateField)
        ((SKDCDateField)ob).enableTimer(enabled);
    else if (ob instanceof SKDCCalendar)
        ((SKDCCalendar)ob).enableTimer(enabled);
  }

  /**
   *  Method called to restart the timer.
   *
   *  @param e Property change event that ocurred.
   */
  public void timeoutRestart(PropertyChangeEvent e)
  {

    if ((inactiveTimeOut > 0) && (activityTimer != null) && (activityTimer.isRunning()))
    {
      activityTimer.restart();
    }
  }

  /**
   *  Method to display an Error message in an option pane.
   *
   *  @param msg Text to be displayed.
   */
  public void displayError(String msg)
  {
    displayError(msg, "Error");
  }

  /**
   *  Method to display an Error message in an option pane.
   *
   *  @param msg Text to be displayed.
   *  @param isTitle Text to be displayed as title.
   */
  protected void displayError(String msg, String isTitle)
  {
    logger.logDebug(msg + " - displayError");
    JOptionPane.showMessageDialog(this, DacTranslator.getTranslation(msg),
        DacTranslator.getTranslation(isTitle), JOptionPane.ERROR_MESSAGE,
        mpJOptionIcon);
  }

  /**
   *  Method to display a Warning message in an option pane.
   *
   *  @param msg Text to be displayed.
   */
  protected void displayWarning(String msg)
  {
    displayWarning(msg, "Warning");
  }

  /**
   *  Method to display a Warning message in an option pane.
   *
   *  @param msg Text to be displayed.
   *  @param isTitle Text to be displayed as title.
   */
  protected void displayWarning(String msg, String isTitle)
  {
    logger.logDebug(msg + " - displayWarning");
    JOptionPane.showMessageDialog(this, DacTranslator.getTranslation(msg),
        DacTranslator.getTranslation(isTitle), JOptionPane.WARNING_MESSAGE,
        mpJOptionIcon);
  }

  /**
   *  Method to display an Informational message in an option pane.
   *
   *  @param msg Text to be displayed.
   */
  public void displayInfo(String msg)
  {
    displayInfo(msg, "Information Only");
  }

  /**
   *  Method to display an Informational message in an option pane.
   *
   *  @param msg Text to be displayed.
   *  @param isTitle Text to be displayed as title.
   */
  protected void displayInfo(String msg, String isTitle)
  {
    JOptionPane.showMessageDialog(this, DacTranslator.getTranslation(msg),
        DacTranslator.getTranslation(isTitle), JOptionPane.INFORMATION_MESSAGE,
        mpJOptionIcon);
  }

  /**
   * Method to display an Informational message in an option pane.
   *
   * @param prompt Text to be displayed.
   * @return ?
   */
  public boolean displayYesNoPrompt(String prompt)
  {
    return displayYesNoPrompt(prompt, "Question");
  }

  /**
   *  Method to display an Informational message in an option pane.
   *
   *  @param prompt Text to be displayed.
   *  @param isTitle Text to be displayed as title.
   *  @return true if yes
   */
  protected boolean displayYesNoPrompt(String prompt, String isTitle)
  {
    int resp = JOptionPane.showConfirmDialog(this,
        DacTranslator.getTranslation(prompt + "?"), 
        DacTranslator.getTranslation(isTitle), 
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
        mpJOptionIcon);
    return (resp == JOptionPane.YES_OPTION);
  }

  /**
   * Log and display an error
   * @param isError
   */
  protected void logAndDisplayError(String isError)
  {
    logger.logError(isError);
    displayError(isError);
  }
  
  /**
   * Log and display an error
   * @param isError
   */
  protected void logAndDisplayException(Exception e)
  {
    logAndDisplayException("", e);
  }
  
  /**
   * Log and display an error
   * @param isError
   */
  protected void logAndDisplayException(String isUserNote, Exception e)
  {
    logger.logException(e);
    if (e.getMessage() == null)
    {
      displayError(isUserNote + "\nNULL");
    }
    else
    {
      displayError(isUserNote + "\n"
          + e.getMessage().substring(e.getMessage().indexOf(':') + 1));
    }
  }
  
  /**
   * Method to add this frame. If duplicate screens are not allowed
   * then any existing copy of this frame will be moved to the screen front. If
   * it does not exist it will be added.
   *
   * @param ipFrame Frame to be added.
   * @param modal Specifies whether or not frame should act as modal frame.
   * @param forceCenter ?
   */
  private void addInternalFrame(SKDCInternalFrame ipFrame, boolean modal, boolean forceCenter)
  {
    desktop = getDesktopPane();
    if (!ipFrame.getAllowDuplicateScreens())
    {
      // check if already active and with the same title, if so bring it to the front
      String vsOriginalTitle = ipFrame.getOriginalTitle();
      for (int x = 0; x < desktop.getComponentCount() ; x++)
      {
        Component comp = desktop.getComponent(x);
        if (comp instanceof SKDCInternalFrame)
        {
          SKDCInternalFrame vpFrame = (SKDCInternalFrame)comp;
          if ((vpFrame.getClass().getName().equals(ipFrame.getClass().getName())) &&
            (vpFrame.getTitle().equals(vsOriginalTitle)))
          {
            if (vsOriginalTitle != null)
            {
              //
              // Restore the title in the TaskBar that the newly created frame
              // replaced.
              //
              new SKDCTaskBar().updateTaskBarTaskTitle(ipFrame, vsOriginalTitle);
            }
            ipFrame.uncreate();
            ipFrame = null;
            //
            // Show the existing Frame.
            //
            try
            {
              vpFrame.setSelected(true);
            }
            catch (PropertyVetoException pve) {}
            //
            // We need to transfer any parent frames and button panels to the existing frame.
            //
//            vpFrame.transferParentFramesAndButtonPanels(ipFrame.parentFrames, ipFrame.buttonPanels);
            vpFrame.moveToFront();
            return;
          }
        }
      }
    }

    ipFrame.setFontSize(mfFontSize);
    ipFrame.pack();
    // if this frame is wider than the screen, shrink it.
    if (ipFrame.getWidth() > desktop.getWidth())
    {
      ipFrame.setSize(desktop.getWidth(), ipFrame.getHeight());
    }

    if (forceCenter)
    {
      SKDCFrame.centerFrame(desktop,ipFrame);
    }
    else
    {
      SKDCFrame.positionFrame(desktop,ipFrame);
    }

    if (modal)
    {
      desktop.add(ipFrame, JLayeredPane.MODAL_LAYER);
    }
    else
    {
      desktop.add(ipFrame);
    }

    ipFrame.setVisible(true);
    ipFrame.toFront();
    new SKDCTaskBar().addToTaskBar(ipFrame);
  }

  /**
   *  Method to add an SKDC internal frame with a property change listener.
   *
   *  @param newFrame Frame to be added.
   *  @param listener Property change listener to be added.
   */
  public void addSKDCInternalFrame(SKDCInternalFrame  newFrame, PropertyChangeListener listener)
  {
    newFrame.addPropertyChangeListener(listener);
    this.addSKDCInternalFrame(newFrame);
  }

  /**
   *  Method to add an SKDC internal frame.
   *
   *  @param newFrame Frame to be added.
   */
  public void addSKDCInternalFrame(SKDCInternalFrame  newFrame)
  {
    this.addInternalFrame(newFrame, false, false);
    findIcon(newFrame); 
  }

  /**
   *  Method to add a modal SKDC internal frame with a property change listener and
   *  disables the buttons on the invoking panels.
   *
   *  @param newFrame Frame to be added.
   *  @param panel Panels whose buttons should be disabled while this frame is displayed.
   *  @param listener Property change listener to be added.
   */
  public void addSKDCInternalFrameModal(SKDCInternalFrame  newFrame, JPanel[] panel, PropertyChangeListener listener)
  {
    if(!newFrame.isClosed)
    {
      for (int i = 0; i < panel.length; i++)
      {
        newFrame.buttonPanels.add(panel[i]);
        enablePanelComponents(panel[i], false);
      }
      this.addSKDCInternalFrameModal(newFrame,listener);
    }
  }

  /**
   *  Method to add a modal SKDC internal frame with a property change listener and
   *  disables the buttons on the invoking panel.
   *
   *  @param newFrame Frame to be added.
   *  @param panel Panel whose buttons should be disabled while this frame is displayed.
   *  @param listener Property change listener to be added.
   */
  public void addSKDCInternalFrameModal(SKDCInternalFrame  newFrame, JPanel panel, PropertyChangeListener listener)
  {
    newFrame.buttonPanels.add(panel);
    enablePanelComponents(panel, false);
    this.addSKDCInternalFrameModal(newFrame, listener);
  }

  /**
   *  Method to add a modal SKDC internal frame and
   *  disable the buttons on the invoking panels.
   *
   *  @param newFrame Frame to be added.
   *  @param panel Panels whose buttons should be disabled while this frame is displayed.
   */
  public void addSKDCInternalFrameModal(SKDCInternalFrame  newFrame, JPanel[] panel)
  {
    for (int i = 0; i < panel.length; i++)
    {
      newFrame.buttonPanels.add(panel[i]);
      enablePanelComponents(panel[i], false);
    }
    this.addSKDCInternalFrameModal(newFrame);
  }

  /**
   *  Method to add a modal SKDC internal frame and
   *  disable the buttons on the invoking panel.
   *
   *  @param newFrame Frame to be added.
   *  @param panel Panel whose buttons should be disabled while this frame is displayed.
   */
  public void addSKDCInternalFrameModal(SKDCInternalFrame  newFrame, JPanel panel)
  {
    newFrame.buttonPanels.add(panel);
    enablePanelComponents(panel, false);
    this.addSKDCInternalFrameModal(newFrame);
  }

  /**
   *  Method to add a modal SKDC internal frame with a property change listener.
   *
   *  @param newFrame Frame to be added.
   *  @param listener Property change listener to be added.
   */
  public void addSKDCInternalFrameModal(SKDCInternalFrame  newFrame, PropertyChangeListener listener)
  {
    newFrame.addPropertyChangeListener(listener);
    this.addSKDCInternalFrameModal(newFrame);
  }

  /**
   *  Method to add a modal SKDC internal frame.
   *
   *  @param newFrame Frame to be added.
   */
  public void addSKDCInternalFrameModal(SKDCInternalFrame  newFrame)
  {
    newFrame.parentFrames.add(this);

    findIcon(newFrame);
    enableFrameComponents(this, false);
    setClosable(false);
    
    try
    {
      addInternalFrame(newFrame, true, false);
    }
    catch (Exception e)
    {
      newFrame.close();
      logger.logException(e);
      displayError("Error opening frame (see log)");
    }
  }

  /**
   *  Method to set the parents frame closable value.
   *
   *  @param enb True if closable, false if not.
   */
  private void setParentCloseable(boolean enb)
  {
    for(Iterator<Object> it = parentFrames.iterator(); it.hasNext();)
    {
      ((SKDCInternalFrame)it.next()).setClosable(enb);
    }
  }

  /**
   *  Method to set the parents table enabled.
   */
  private void setParentTableEnabled()
  {
    for(Iterator<Object> it = parentFrames.iterator(); it.hasNext();)
    {
      SKDCInternalFrame intFrame = (SKDCInternalFrame)it.next();
      if (!intFrame.isClosed())
      {
        enableFrameComponents(intFrame, true);
      }
    }
  }

  /**
   * I need a JavaDoc!
   */
  private void enableFrameComponents(SKDCInternalFrame vpFrame, boolean vzEnabled)
  {
    Component[] vpComp = vpFrame.getContentPane().getComponents();
    for(int vnCmp = 0; vnCmp < vpComp.length; vnCmp++)
    {
      if (vpComp[vnCmp] instanceof JPanel)
      {
        enablePanelComponents((JPanel)vpComp[vnCmp], vzEnabled);
      }
      else if (vpComp[vnCmp] instanceof javax.swing.JScrollPane)
      {
        Component vpView = ((javax.swing.JScrollPane)vpComp[vnCmp]).getViewport().getView();
        if (vpView instanceof DacTable)
        {
          ((DacTable)vpView).setTableEnabled(vzEnabled);
        }
      }
      else if (vpComp[vnCmp] instanceof JTabbedPane)
      {
        Component[] vpTabCmp = ((JTabbedPane)vpComp[vnCmp]).getComponents();
        for(int i = 0; i < vpTabCmp.length; i++)
        {
          if (vpTabCmp[i] instanceof JPanel)
          {
            enablePanelComponents((JPanel)vpTabCmp[i], vzEnabled);
          }
        }
      }
    }
  }
 
  /**
   *  Method to enable or disable components on a panel.
   *
   *  @param panel Panel whose components are to be enabled.
   *  @param enabled True if closeable, false if not.
   */
  public void enablePanelComponents(JPanel panel, boolean enabled)
  {
    if (panel == null)
    {
      return;
    }

    Component[] comp = panel.getComponents();

    for(int i = 0; i < comp.length; i++)
    {
      if (comp[i] instanceof JButton)
      {
        if (((SKDCButton)comp[i]).isDisableAllowed())
        {
          comp[i].setEnabled(enabled);
        }
      }
      else if (comp[i] instanceof javax.swing.JScrollPane)
      {
        Component vpView = ((javax.swing.JScrollPane)comp[i]).getViewport().getView();
        vpView.setEnabled(enabled);
      }
      else if (comp[i] instanceof JComboBox     ||
               comp[i] instanceof JRadioButton  ||
               comp[i] instanceof JCheckBox     ||
               comp[i] instanceof JSpinner      ||
               comp[i] instanceof JToggleButton ||
               comp[i] instanceof JPopupMenu    ||
               comp[i] instanceof JTextField)
      {
        comp[i].setEnabled(enabled);
      }
      else if (comp[i] instanceof JPanel)
      {
        enablePanelComponents((JPanel)comp[i], enabled);
      }
    }
  }

 /**
  * Method to enable or disable a specified set of lightweight components.
  * @param ipCompList a List of lightweight components.
  * @param izEnable boolean of <code>true</code> means enable the components.
  *                 <code>false</code> means disable the components.
  */
  public void enableLightWeightComponents(List<JComponent> ipCompList, boolean izEnable)
  {
    for(JComponent vpComp : ipCompList)
    {
      if (vpComp instanceof javax.swing.JScrollPane)
      {
        Component vpTable = ((javax.swing.JScrollPane)vpComp).getViewport().getView();
        if (vpTable instanceof DacTable)
        {
          ((DacTable)vpTable).setTableEnabled(izEnable);
        }
      }
      else if (vpComp instanceof DacTable)
      {
        ((DacTable)vpComp).setTableEnabled(izEnable);
      }
      else if (vpComp instanceof JPanel)
      {
        enablePanelComponents((JPanel)vpComp, izEnable);
      }
      else
      {
        vpComp.setEnabled(izEnable);
      }
    }
  }
  
  /**
   * Returns true if system gateway is needed.
   *
   * <p><b>Details:</b> <code>getSystemGatewayNeeded</code> declares whether or
   * not a system gateway is needed for this frame.  If a system gateway is
   * needed, this method returns <code>true</code>, <code>false</code>
   * otherwise.</p>
   *
   * <p>The default implementation of this method returns <code>true</code>.
   * Therefore, frames which do not desire a system gateway should override this
   * method to return <code>false</code>.</p>
   *
   * @return true iff a system gateway is needed
   * @see #createAndStartupSystemGateway()
   */
  protected boolean getSystemGatewayNeeded()
  {
    return true;
  }

  /**
   * I need a JavaDoc!
   */
  protected void shutdownFrame()
  {
    String vsClassName = this.getClass().getName();
    List<Object> vpInstanceKeys = instanceKeysMap.get(vsClassName);
    if (vpInstanceKeys == null)
    {
      vpInstanceKeys = new ArrayList<Object>();
      instanceKeysMap.put(vsClassName, vpInstanceKeys);
    }
    vpInstanceKeys.remove(instanceKey);
    instanceKey = null;
    defaultTitle = null;
    
    if (mpClearTimer != null)
    {
      mpClearTimer.cancel();
      mpClearTimer = null;
    }
  }

  /**
   * I need a JavaDoc!
   */
  @SuppressWarnings("unchecked")
  private static List<Object> toListOfObjects(Object ipList)
  {
    return (List<Object>) ipList;
  }
  
  /**
   * I need a JavaDoc!
   */
  private Integer getInstanceKey()
  {
    String vsClassName = this.getClass().getName();
    List<Object> vpInstanceKeys = toListOfObjects(instanceKeysMap.get(vsClassName));
    if (vpInstanceKeys == null || vpInstanceKeys.isEmpty())
    {
      vpInstanceKeys = new ArrayList<Object>();
      instanceKeysMap.put(vsClassName, vpInstanceKeys);
    }
    int i = -1;
    Integer vpInteger = null;
    while (true)
    {
      i++;
      if (i == 1)
      { 
        i++;
      }
      vpInteger = Integer.valueOf(i);
      if (! vpInstanceKeys.contains(vpInteger))
      {
        vpInstanceKeys.add(vpInteger);        
        break;
      }
    }
    return vpInteger;
  }

  /**
   * Creates and activates system gateway.
   *
   * <p><b>Details:</b> <code>createAndStartupSystemGateway</code> creates and
   * starts up a system gateway if one is needed for this frame.  It makes the
   * decision to create or not create the system gateway by calling
   * <code>getSystemGatewayNeeded</code>, which may be overridden in a
   * subclasses.</p>
   *
   * @see #getSystemGatewayNeeded()
   */
  private void createAndStartupSystemGateway()
  {
    if (!getSystemGatewayNeeded())
      return;
    String vsNameSpace = logger.getLoggerInstanceName();
    try
    {
      logger.logDebug("SystemGateway NameSpace " + vsNameSpace);
//      SkdContext.putSystemGateway(vsNameSpace, getSystemGateway());
    }
    catch (Exception e)
    {
      logger.logException(e, "SystemGateway NOT Set - Namespace " + vsNameSpace);
    }
    MessageService ms = Factory.create(MessageService.class);
    String s = ms.getStartupFailReason();
    if (s != null)
    {
      displayError("Warehouse Rx Message Service Failure!\n\n" +
                   "1. Confirm Warehouse Rx Message Service (" + ms.getProviderName() + ") is running on the Server.\n" +
                   "2. Shutdown this Warehouse Rx application & restart.");
    }
  }

  /**
   *  Method to get an SKDC internal frame.
   *
   *  @param className Class name to find.
   *  @return SKDCInternalFrame containing screen object if found, otherwise
   *  contains null
   */
  public SKDCInternalFrame getSKDCInternalFrame(String className)
  {
    desktop = getDesktopPane();
    // if it exists, find it
    for (int x = 0; x < desktop.getComponentCount() ; x++)
    {
      Component comp = desktop.getComponent(x);
      if (comp instanceof SKDCInternalFrame)
      {
        if (((SKDCInternalFrame)comp).getClass().getName().endsWith(className))
        {
          ((SKDCInternalFrame)comp).moveToFront();
          return (SKDCInternalFrame)comp;
        }
      }
      else if (comp instanceof javax.swing.JInternalFrame.JDesktopIcon)
      {
        // this is an iconified window
        if (((JInternalFrame.JDesktopIcon)comp).getInternalFrame().getClass().getName().endsWith(className))
        {
          if (((JInternalFrame.JDesktopIcon)comp).getInternalFrame().isIcon())
          {
            try
            {
              ((JInternalFrame.JDesktopIcon)comp).getInternalFrame().setIcon(false);
            }
            catch (PropertyVetoException pve) {}
          }
          ((JInternalFrame.JDesktopIcon)comp).getInternalFrame().moveToFront();
          return (SKDCInternalFrame)((JInternalFrame.JDesktopIcon)comp).getInternalFrame();
        }
      }
    }
    return null;
  }

//  /**
//   *  Method to add parent frames to this internal frame.
//   *
//   * @param parents List containing SKDCInternalFrames to be transferred.
//   * @param panels List containing JPanels to be transferred.
//   */
//  protected void transferParentFramesAndButtonPanels(List parents, List panels)
//  {
//    for(Iterator it = parents.iterator(); it.hasNext();)
//    {
//      this.parentFrames.add(it.next());
//    }
//    for(Iterator it = panels.iterator(); it.hasNext();)
//    {
//      this.buttonPanels.add(it.next());
//    }
//  }

  /**
   * Current wait frame.
   *
   * <p><b>Details:</b> <code>mpWaitFrames</code> is the set of all wait frames
   * currently displayed and associated with this frame.  Each wait frame stored
   * in this map is keyed by the handle returned by <code>openWaitFrame</code>
   * when it was created.</p>
   *
   * <p>In order to reduce the overhead of the wait frame feature when it is not
   * being used, this field is set to <code>null</code> when there are no wait
   * frames to track.</p>
   *
   * @see #openWaitFrame(String)
   * @see #closeWaitFrame(Object)
   */
  private Map<Object, Object> mpWaitFrames;

  /**
   * Displays wait dialog.
   *
   * <p><b>Details:</b> <code>openWaitFrame</code> creates and displays a
   * non-<wbr>interactive "wait dialog (frame)" over this frame.  The supplied
   * title is displayed in the wait frame's caption bar.  It is acceptable to
   * display multiple, independent wait frames associated with a common parent
   * frame.</p>
   *
   * <p>You should utilize this feature whenever your frame must execute a
   * procedure that takes longer than a "single moment of user interactivity."
   * Contemporary opinion defines this interval to be approximately 0.5
   * secs.</p>
   *
   * <p><code>openWaitFrame</code> returns a handle associated with the newly
   * displayed wait frame.  To close a wait frame after it has been displayed,
   * call <code>closeWaitFrame</code> with the handle that was returned by
   * <code>openWaitFrame</code>.</p>
   *
   * <p>This method is thread-safe and can be called from any thread.</p>
   *
   * <p><b>Design:</b> A different strategy for managing multiple wait frames,
   * which was considered, involves creating and displaying the
   * <code>SKDCProgressFrame</code>, as is currently done, and then returning
   * the actual SKDCProgressFrame instance instead of the handle.  This approach
   * was not used because it seemed better to insulate the client from all
   * implementation details.  A different strategy may become necessary when we
   * begin using the wait frames as progress frames (i.e., showing correlated
   * progress instead of animated loops).</p>
   *
   * @param isTitle text displayed in wait frame
   * @return handle to close frame
   * @see #closeWaitFrame(Object)
   */
  protected Object openWaitFrame(final String isTitle)
  {
    if (! SwingUtilities.isEventDispatchThread())
    {
      final class OpenWaitFrame implements Runnable
      { Object mpHandle;
        @Override
        public void run() {mpHandle = openWaitFrame(isTitle);}
      }
      final OpenWaitFrame vpInvoker = new OpenWaitFrame();
      try
      {
        SwingUtilities.invokeAndWait(vpInvoker);
      }
      catch (Throwable ve)
      {
        throw new UnreachableCodeException(SKDCUtility.rethrow(ve));
      }
      return vpInvoker.mpHandle;
    }
    if (mpWaitFrames == null)
      mpWaitFrames = new HashMap<Object, Object>();
    final Object mpHandle = new Object();
    final SKDCProgressFrame vpWaitFrame = new SKDCProgressFrame(isTitle);
    addInternalFrame(vpWaitFrame, true, false);
    mpWaitFrames.put(mpHandle, vpWaitFrame);
    return mpHandle;
  }

  /**
   * Closes wait frame.
   *
   * <p><b>Details:</b> <code>closeWaitFrame</code> closes the wait frame
   * associated with the given handle, which was returned by a prior call to
   * <code>openWaitFrame</code>.</p>
   *
   * <p>This method is thread-safe and can be called from any thread.</p>
   *
   * @param ipHandle handle to wait frame
   * @see #openWaitFrame(String)
   */
  protected void closeWaitFrame(final Object ipHandle)
  {
    if (! SwingUtilities.isEventDispatchThread())
    {
      SwingUtilities.invokeLater
      ( new Runnable() {@Override
      public void run() {closeWaitFrame(ipHandle);}}
      );
      return;
    }
    final SKDCProgressFrame vpWaitFrame = (SKDCProgressFrame) mpWaitFrames.remove(ipHandle);
    if (vpWaitFrame == null)
      throw new IllegalArgumentException("Invalid handle; ipHandle=" + ipHandle);
    vpWaitFrame.close();
    if (mpWaitFrames.isEmpty())
      mpWaitFrames = null;
  }

  /**
   * Finds a system icon for an internal frame.
   *
   * <p><b>Details:</b> <code>findIcon</code> uses a <code>SKDCUserData</code>
   * instance to locate a default icon for an <code>SKDCInternalFrame</code>.  If one cannot be 
   * found the icon from the current <code>SKDCInternalFrame</code> will be used.</p>
   *
   * @param ipFrame SKDCInternalFrame
   */
  protected void findIcon(SKDCInternalFrame ipFrame)
  {
    // Lets try and find an icon
    SKDCUserData vpUserData = new SKDCUserData();
    String vsIconName = vpUserData.getIcon(ipFrame.getClass().getName());
    if (vsIconName == null)
    {
      // if there is not an icon for this frame then use the current frames icon
      ipFrame.setFrameIcon(this.frameIcon);
    }
    else
    {
      Image vpImage = new ImageIcon(SKDCInternalFrame.class.getResource(vsIconName)).getImage();
      // lets resize the icon so it will fit in the frame
      ImageIcon vpIcon = new ImageIcon(vpImage.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
      ipFrame.setFrameIcon(vpIcon);
    }
  }

  /**
   *  Action method to handle Clear button.
   */
  protected void clearButtonPressed()
  {
  }

  /**
   *  Action method to handle OK button.
   */
  protected void okButtonPressed()
  {
  }

  /**
   *  Action method to handle Close button.
   */
  protected void closeButtonPressed()
  {
    close();
  }

  /*========================================================================*/
  /*  Default methods to standardize insets for label/input columns         */
  /*========================================================================*/
  
  /**
   *  Get the default label column insets (for GridBagLayout) 
   */
  public Insets getLabelColumnInsets()
  {
    return new Insets(4, 40, 4, 2);
  }
  
  /**
   *  Get the default label column insets (for GridBagLayout) 
   */
  public Insets getInnerLabelColumnInsets()
  {
    return new Insets(4, 0, 4, 2);
  }
  
  /**
   *  Get the default label column insets (for GridBagLayout) 
   */
  public Insets getInputColumnInsets()
  {
    return new Insets(4, 2, 4, 40);
  }

  /**
   * Set up the GridBagConstraint defaults for the Label Column
   * @param ipGBC
   */
  public void setLabelColumnGridBagConstraints(GridBagConstraints ipGBC)
  {
    ipGBC.gridx = 0;
    ipGBC.gridy = GridBagConstraints.RELATIVE;
    ipGBC.gridwidth = 1;
    ipGBC.anchor = GridBagConstraints.EAST;
    ipGBC.weightx = 0.2;
    ipGBC.weighty = 0.8;
    ipGBC.insets = getLabelColumnInsets();
  }

  /**
   * Set up the GridBagConstraint defaults for the Label Column
   * @param ipGBC
   */
  public void setInnerLabelColumnGridBagConstraints(GridBagConstraints ipGBC)
  {
    ipGBC.gridx = 0;
    ipGBC.gridy = GridBagConstraints.RELATIVE;
    ipGBC.gridwidth = 1;
    ipGBC.anchor = GridBagConstraints.EAST;
    ipGBC.weightx = 0.2;
    ipGBC.weighty = 0.8;
    ipGBC.insets = getInnerLabelColumnInsets();
  }

  /**
   * Set up the GridBagConstraint defaults for the Input Column
   * @param ipGBC
   */
  public void setInputColumnGridBagConstraints(GridBagConstraints ipGBC)
  {
    ipGBC.gridx = 1;
    ipGBC.gridy = GridBagConstraints.RELATIVE;  
    ipGBC.gridwidth = 1;
    ipGBC.anchor = GridBagConstraints.WEST;
    ipGBC.weightx = 0.2;
    ipGBC.weighty = 0.8;
    ipGBC.insets = getInputColumnInsets();
  }
  
  /**
   * Get a default style button panel
   * @return
   */
  public JPanel getEmptyButtonPanel()
  {
    JPanel vpButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    vpButtonPanel.setBorder(BorderFactory.createEtchedBorder());
    return vpButtonPanel;
  }
  
  /**
   * Get a default style button panel
   * @return
   */
  public JPanel getEmptyListSearchPanel()
  {
    JPanel vpButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 20));
    vpButtonPanel.setBorder(BorderFactory.createEtchedBorder());
    return vpButtonPanel;
  }

  /**
   * Get a default style input panel (GridBagLayout & Titled/Etched border)
   * @return
   */
  public JPanel getEmptyInputPanel(String isTitle)
  {
    JPanel vpInputPanel = new JPanel(new GridBagLayout());
    vpInputPanel.setBorder(new TitledBorder(new EtchedBorder(), DacTranslator.getTranslation(isTitle)));
    return vpInputPanel;
  }
  
  /**
   * Set the frame icon and maintain a larger icon for JOptionPanes 
   * @param image 32x32 image
   */
  public void setFrameIcon(Image image)
  {
    ImageIcon vpIcon = new ImageIcon(image.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
    setFrameIcon(vpIcon);
//    mpJOptionIcon = new ImageIcon(image.getScaledInstance(32, 32, Image.SCALE_SMOOTH));
  }
  
  /*========================================================================*/
  /*  Methods for the information bar                                       */
  /*========================================================================*/

  /**
   * Get the panel for the displayInfoAutoTimeOut display.
   * This disables the displayInfoAutoTimeOut pop-up.
   * @return
   */
  protected JPanel getInfoPanel()
  {
    mzShowInfoPanel = true;
    if (mpInfoPanel == null)
    {
      mpInfoPanel = getEmptyButtonPanel();
      mpInfoPanel.add(mpInfoLabel);
    }
    if (mpClearTimer == null)
    {
      mpClearTimer = new Timer();
      mpClearTimer.schedule(new AutoTimeOutTimerTask(), 5000, 5000);
    }
    return mpInfoPanel; 
  }
  
  /**
   * Display info on the info bar
   * @param isInfo
   */
  public void setInfo(String isInfo)
  {
    setInfo(isInfo, Font.PLAIN);
  }
  
  /**
   * Display an error on the info bar and beep
   * @param isError
   */
  public void setError(String isError)
  {
    setInfo(isError, Font.BOLD);
    Toolkit.getDefaultToolkit().beep();
  }

  /**
   * Display a message on the info bar
   * @param isInfo
   * @param inFontWeight
   */
  private void setInfo(String isInfo, int inFontWeight)
  {
    if (isInfo == null || isInfo.length() == 0)
      isInfo = " ";

    mpInfoLabel.setText(isInfo);
    Font vpFont = mpInfoLabel.getFont();
    Font vpFont2 = new Font(vpFont.getName(), inFontWeight, vpFont.getSize());
    mpInfoLabel.setFont(vpFont2);
    mpInfoLabel.setToolTipText(isInfo);
    mpEraseTime = null;
  }

  /**
   * Display a temporary message on the info bar
   * @param isInfo
   */
  public void displayInfoAutoTimeOut(String isInfo)
  {
    displayInfoAutoTimeOut(isInfo, null);
  }
  
  /**
   * Display a temporary message on the info bar
   * @param isInfo
   * @param isTitle
   */
  public void displayInfoAutoTimeOut(String isInfo, String isTitle)
  {
    if (mzShowInfoPanel)
    {
      String vsDisplayString = "";
      if (isTitle != null)
      {
        vsDisplayString = DacTranslator.getTranslation(isTitle) + ": ";
      }
      vsDisplayString += DacTranslator.getTranslation(isInfo);
      
      if (isTitle != null && isTitle.endsWith("Error"))
      {
        setError(vsDisplayString);
      }
      else
      {
        setInfo(vsDisplayString);
      }
      mpEraseTime = new Date(new Date().getTime() + mnTimeOut);
    }
    else if (isShowing())
    {
      if (isTitle == null)
      {
        isTitle = "Information Only";
      }
      SKDCFlashScreen flash = 
        new SKDCFlashScreen(DacTranslator.getTranslation(isTitle), 
            DacTranslator.getTranslation(isInfo));
      flash.setFrameIcon(frameIcon);
      setClosable(true);
      addInternalFrame(flash, true, true);
    }
  }

  /**
   * <B>Description:</B> Task for clearing the info bar
   *
   * @author       mandrus<BR>
   * @version      1.0
   * 
   * <BR>Copyright (c) 2007 by Daifuku America Corporation
   */
  private class AutoTimeOutTimerTask extends TimerTask
  {
    @Override
    public void run()
    {
      if (mpEraseTime != null)
      {
        Date vpCurrent = new Date();
        if (vpCurrent.getTime() > mpEraseTime.getTime())
        {
          mpEraseTime = null;
          SwingUtilities.invokeLater(new Runnable() 
            {
              @Override
              public void run()
              {
                setInfo(" ");
              }
            });
        }
      }
    }
  }
  
  /**
   * Change the font size of labels, inputs, and the info bar.  This method
   * only adjusts the size of current screen components; it does not adjust
   * the size of future components.
   * 
   * <BR>NOTE: This method does not support font sizes smaller than 10. 
   * 
   * @param ifSize (if ifSize >= 10, change fonts to that size)
   */
  protected void setFontSize(float ifSize)
  {
    if (ifSize < 10)
    {
      return;
    }
    mfFontSize = ifSize;
    for (Component j : getContentPane().getComponents())
    {
        setFontSize(ifSize, j);
    }
    
    pack();
    
    /*
     * The above changes could make us run off of the screen.  If so, adjust
     * our position.  We haven't actually displayed yet, so this shouldn't 
     * confuse anyone.
     * 
     * Unfortunately, at this time getDesktopPane() does not work for child 
     * screen.  Until this is fixed, we can only do this for screens started
     * from the desktop.
     */
    if (getDesktopPane() != null)
    {
      int vnX = getLocation().x;
      int vnY = getLocation().y;
      int vnPadding = 15;
      if (vnX + getWidth() + vnPadding > getDesktopPane().getWidth())
      {
        vnX = Math.max(5, getDesktopPane().getWidth() - getWidth() - vnPadding);
      }
      if (vnY + getHeight() + vnPadding > getDesktopPane().getHeight())
      {
        vnY = Math.max(5, getDesktopPane().getHeight() - getHeight() - vnPadding);
      }
      setLocation(vnX, vnY);
    }
  }
  
  /**
   * Change the font size of a JComponent
   * 
   * @param ifSize
   * @param ipComp
   */
  protected void setFontSize(float ifSize, Component ipComp)
  {
    if ((ipComp instanceof JPanel) || 
        (ipComp instanceof JScrollPane) ||
        (ipComp instanceof JViewport)) 
    {
      for (Component c : ((JComponent)ipComp).getComponents())
      {
        setFontSize(ifSize, c);
      }
      return;
    }
    
    Font vpFont = ipComp.getFont();
    float vfOriginal = vpFont.getSize2D();
    ipComp.setFont(vpFont.deriveFont(ifSize));
    
    /*
     * For some reason, SKDCComboBoxes don't scale well by themselves.
     * This tries to correct that.
     */
    if (ipComp instanceof JComboBox)
    {
      float vnScale = ifSize / vfOriginal;
      Dimension vpDim = ipComp.getPreferredSize();
      vpDim.height = (int)ifSize + 14;
      vpDim.width *= vnScale;
      ipComp.setPreferredSize(vpDim);
      ipComp.setMinimumSize(vpDim);
    }
    /*
     * Neither do JTable rows
     */
    else if (ipComp instanceof JTable)
    {
      ((JTable)ipComp).setRowHeight((int)(ifSize + 4));
    }
    
    if (ipComp instanceof JButton)
    {
      Dimension  vpSize = ipComp.getPreferredSize();
      vpSize.height = (int)(vpSize.height * Application.getDouble("ButtonHeightRatio", 1.0));
      ipComp.setPreferredSize(vpSize);
    }

  }
  /**
   * Change the button size.  This method
   * only adjusts the size of current screen components; it does not adjust
   * the size of future components.
   * 
   * <BR>NOTE: This method does not support font sizes smaller than 10. 
   * 
   * @param ifSize (if ifSize >= 10, change fonts to that size)
   */
  protected void setButtonSize(float ifSize)
  {

    for (Component j : getContentPane().getComponents())
    {
        setButtonSize(ifSize, j);
    }
    
    pack();
    
  }
  
  /**
   * Change the button size of a JComponent
   * 
   * @param ifSize
   * @param ipComp
   */
  protected void setButtonSize(float ifSize, Component ipComp)
  {
    if ((ipComp instanceof JPanel) || 
        (ipComp instanceof JScrollPane) ||
        (ipComp instanceof JViewport)) 
    {
      for (Component c : ((JComponent)ipComp).getComponents())
      {
        setButtonSize(ifSize, c);
      }
      return;
    }
    /*
     * For some reason, SKDCComboBoxes don't scale well by themselves.
     * This tries to correct that.
     */
    if (ipComp instanceof JButton)
    {
      Dimension  vpSize = ipComp.getPreferredSize();
      vpSize.height = (int)(vpSize.height * ifSize);
      ipComp.setPreferredSize(vpSize);
    }
  }
}
