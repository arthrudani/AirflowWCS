package com.daifukuamerica.wrxj.swingui.utility;

import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.data.ControllerConfig;
import com.daifukuamerica.wrxj.dbadapter.data.ControllerConfigData;
import com.daifukuamerica.wrxj.dbadapter.data.SysConfig;
import com.daifukuamerica.wrxj.dbadapter.data.SysConfigData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.DacTranslator;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.swing.table.DacTable;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class SystemConfigMain extends SKDCInternalFrame
{
  private static final String TAB_NAME_TIMEDEVENT  = "Timed Events";
  private static final String TAB_NAME_DFTSTATION  = "Default Stations";
  private static final String TAB_NAME_SYSCONFIG   = "System Config";
  private static final String TAB_NAME_CNTLCONFIG  = "Controller Config";
  
  private static final int    TOTAL_TABCOUNT = 4;
  private static final int    TAB_TIMEDEVENT = 0;
  private static final int    TAB_DFTSTATION = 1;
  private static final int    TAB_SYSCONFIG  = 2;
  private static final int    TAB_CNTLCONFIG = 3;

  private JTabbedPane            mpTabbedPanel;
  private JPanel                 mpPnlDefaultStations;
  private JPanel                 mpPnlTimedEvents;
  private JPanel                 mpPnlSysConfig;
  private JPanel                 mpPnlCntrlConfig;
  private JPanel                 mpPnlButton;
  private ConfigurationFrame     mpDefaultStationFrame;
  private ConfigurationFrame     mpTimedEventFrame;
  private ConfigurationFrame     mpSysConfigFrame;
  private ConfigurationFrame     mpControllerConfigFrame;

  protected SKDCButton           mpBtnDelete;
  protected SKDCButton           mpBtnModify;
  protected SKDCButton           mpBtnAdd;
  protected SKDCButton           mpBtnClose;

  private DBObject               mpDBObject = null;
  private SKDCUserData           userData = null;
  private SKDCScreenPermissions  ePerms = null;
  private boolean                mzSuperUser = false;
  private boolean                mzAdminUser = false;
  private int                    miSelectedTabIndex = 0;
  private DacTable               mpSktable = null;
  private String                 msSingularRowDescription = "";
  private String                 msPluralRowDescription = "";
  private ControllerConfig       mpCC = Factory.create(ControllerConfig.class);
  private ControllerConfigData   mpCCD = Factory.create(ControllerConfigData.class);
  private SysConfig              mpSC = Factory.create(SysConfig.class);
  private SysConfigData          mpSCD = Factory.create(SysConfigData.class);

  public SystemConfigMain()
  {
    super("System Configuration");
    setMaximizable(true);
    
    // get user permission for the Order Main class
    userData = new SKDCUserData();
    ePerms = userData.getOptionPermissionsByClass(getClass());
    
    mzSuperUser = SKDCUserData.isSuperUser();
    mzAdminUser = SKDCUserData.isAdministrator();

    initialization();
    buildScreen();
    setupButtonListeners();
    
    Container contentPane = getContentPane();
    contentPane.add(mpTabbedPanel, BorderLayout.NORTH);
    contentPane.add(getInfoPanel(), BorderLayout.CENTER);
    contentPane.add(mpPnlButton, BorderLayout.SOUTH);
    
    setPreferredSize(new Dimension(1000, 580));
    
    pack();
    
    refreshDisplay();
  }
  
  /*
   * ===========================================================================
   * ***** Event Listeners go here ******
   * ===========================================================================
   */
  /**
   *  Defines all buttons on the main frame and adds listeners to them.
   */
  private void setupButtonListeners()
  {
    ActionListener vpButtonListener = new ConfigButtonListener();
    
    mpBtnAdd.addEvent(ADD_BTN, vpButtonListener);
    mpBtnModify.addEvent(MODIFY_BTN, vpButtonListener);
    mpBtnDelete.addEvent(DELETE_BTN, vpButtonListener);
    mpBtnClose.addEvent(CLOSE_BTN, vpButtonListener);
  }

  /**
   * This is the button listener.
   */
  private class ConfigButtonListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      String vsPressedButton = e.getActionCommand();
      
      // Add
      if(vsPressedButton.equals(ADD_BTN))
      {
        addButtonPressed();
      }
      // Modify
      else if(vsPressedButton.equals(MODIFY_BTN))
      {
        modifyButtonPressed();
      }
      // Delete
      else if(vsPressedButton.equals(DELETE_BTN))
      {
        deleteButtonPressed();
      }
      // Close
      else if(vsPressedButton.equals(CLOSE_BTN))
      {
        closeButtonPressed();
      }
    }
  }
  
  /**
   * Method to perform Add action
   */
  private void addButtonPressed()
  {
    switch (miSelectedTabIndex)
    {
    case TAB_TIMEDEVENT:
      addTimedEvent();
      break;
    case TAB_DFTSTATION:
      addDefaultStation();
      break;
    case TAB_SYSCONFIG:
      addSysConfig();
      break;
    case TAB_CNTLCONFIG:
      addControllerConfig();
      break;
    }
  }

  /**
   * Method to perform Modify action
   */
  private void modifyButtonPressed()
  {
    // Make sure only one row is selected
    if (mpSktable.getSelectedRowCount() > 1)
    {
      displayInfoAutoTimeOut("Only one row can be Modified at a time", "Selection Error");
      return;
    }
    else if (mpSktable.getSelectedRowCount() == 0)
    {
      displayInfoAutoTimeOut("No row selected", "Selection Error");
      return;
    }

    switch (miSelectedTabIndex)
    {
    case TAB_TIMEDEVENT:
      modifyTimedEvent();
      break;
    case TAB_DFTSTATION:
      modifyDefaultStation();
      break;
    case TAB_SYSCONFIG:
      modifySysConfig();
      break;
    case TAB_CNTLCONFIG:
      modifyControllerConfig();
      break;
    }
  }

  /**
   * Method to perform the Delete action
   */
  private void deleteButtonPressed()
  {
    // Make sure at least one row is selected
    if (mpSktable.getSelectedRowCount() <= 0)
    {
      displayInfoAutoTimeOut("No row selected", "Selection Error");
      return;
    }
    
    // Ask the confirmation from user
    else if (!displayYesNoPrompt("Are you sure?"))
    {
      return;
    }

    switch (miSelectedTabIndex)
    {
    case TAB_TIMEDEVENT:
      deleteTimedEvent();
      break;
    case TAB_DFTSTATION:
      deleteDefaultStation();
      break;
    case TAB_SYSCONFIG:
      deleteSysConfig();
      break;
    case TAB_CNTLCONFIG:
      deleteControllerConfig();
      break;
    }
  }
  
  /**
   * Configuration List
   */
  private class ConfigurationFrame extends SKDCListFrame
  {
    public ConfigurationFrame(String isSearchName, String isAsrsMetaName)
    {
      super(isAsrsMetaName);
      setDetailSearchVisible(false);
      setSearchVisible(false);
      setDisplaySearchCount(true, isSearchName);
      sktable.leftJustifyStrings();
    }
    
    @Override
    public JPanel getButtonPanel()
    {
      return southPanel;
    }
    
    public JPanel getScrollPanel()
    {
      return searchPanel;
    }
    
    public JScrollPane getSearchPanel()
    {
      return sktable.getScrollPane();
    }
    
    public DacTable getSktable()
    {
      return sktable;
    }
    
    public String getSingularRowDescription()
    {
      return msSingularRowDescription;
    }
    
    public String getPluralRowDescription()
    {
      return msPluralRowDescription;
    }

    /**
     *  Method to refreshes display with List.
     *
     *  @param aList List containing data for list.
     */
    @Override
    protected void refreshTable(List aList)
    {
      sktable.refreshData(aList);
      if (mzDisplayCountOnRefresh)
      {
        displayTableCount();
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
      return ConfigurationFrame.class;
    }

    /**
     * Get the class name that will be used in the RoleOptions table.  This 
     * method facilitates the getting of permissions when setCategoryAndOption()
     * is not called and the implemented class is different from the baseline
     * class.
     * 
     * @return <code>Class</code>
     */
    protected void setToolTipColumn(String isColumnName)
    {
      sktable.setToolTipColumns(isColumnName);
    }
  }

  /*
   * ===========================================================================
   * ***** Private methods go here ******
   * ===========================================================================
   */
  private void initialization()
  {
      // make sure connect to Database
    mpDBObject = new DBObjectTL().getDBObject();
    try { mpDBObject.connect(); }
    catch(DBException e) { }

      // Create buttons
    mpBtnAdd = new SKDCButton("Add", "Add", 'A');
    mpBtnModify = new SKDCButton("Modify", "Modify", 'M');
    mpBtnDelete = new SKDCButton("Delete", "Delete", 'D');
    mpBtnClose = new SKDCButton("Close", "Close", 'C');
    
      // Create Configuration panel 
    for (int idx = 0; idx < TOTAL_TABCOUNT; idx++)
    {
      switch (idx)
      {
      case TAB_TIMEDEVENT:
        mpTimedEventFrame = new ConfigurationFrame("TimedEvents", "TimedEvents");
        mpTimedEventFrame.setToolTipColumn(ControllerConfigData.PROPERTYDESC_NAME);
        mpPnlTimedEvents = buildConfigPanel(mpTimedEventFrame);
        break;
      case TAB_DFTSTATION:
        mpDefaultStationFrame = new ConfigurationFrame("DefaultStation", "DefaultStation");
        mpDefaultStationFrame.setToolTipColumn(SysConfigData.DESCRIPTION_NAME);
        mpPnlDefaultStations = buildConfigPanel(mpDefaultStationFrame);
        break;
      case TAB_SYSCONFIG:
        mpSysConfigFrame = new ConfigurationFrame("SystemConfig", "SysConfig");
        mpSysConfigFrame.setToolTipColumn(SysConfigData.PARAMETERVALUE_NAME);
        mpSysConfigFrame.setToolTipColumn(SysConfigData.DESCRIPTION_NAME);
        mpPnlSysConfig = buildConfigPanel(mpSysConfigFrame);
        break;
      case TAB_CNTLCONFIG:
        mpControllerConfigFrame = new ConfigurationFrame("ControllerConfig", "ControllerConfig");
        mpControllerConfigFrame.setToolTipColumn(ControllerConfigData.PROPERTYDESC_NAME);
        mpPnlCntrlConfig = buildConfigPanel(mpControllerConfigFrame);
        break;
      }
    }
  }
  
  /**
   * Build the screen
   */
  private void buildScreen()
  {
      // Create Button panel
    mpPnlButton = new JPanel(new FlowLayout(FlowLayout.CENTER));
    if (ePerms.iAddAllowed) mpPnlButton.add(mpBtnAdd, null);
    if (ePerms.iModifyAllowed) mpPnlButton.add(mpBtnModify, null);
    if (ePerms.iDeleteAllowed) mpPnlButton.add(mpBtnDelete, null);
    mpPnlButton.add(mpBtnClose, null);

    // Create Tabbed Panel
    mpTabbedPanel = new JTabbedPane();
    for (int idx = 0; idx < TOTAL_TABCOUNT; idx++)
    {
      switch (idx)
      {
      case TAB_TIMEDEVENT:
        mpTabbedPanel.addTab(TAB_NAME_TIMEDEVENT, mpPnlTimedEvents);
        break;
      case TAB_DFTSTATION:
        mpTabbedPanel.addTab(TAB_NAME_DFTSTATION, mpPnlDefaultStations);
        break;
      case TAB_SYSCONFIG:
        if (mzSuperUser || mzAdminUser)
          mpTabbedPanel.addTab(TAB_NAME_SYSCONFIG, mpPnlSysConfig);
        break;
      case TAB_CNTLCONFIG:
        if (mzSuperUser || mzAdminUser)
          mpTabbedPanel.addTab(TAB_NAME_CNTLCONFIG, mpPnlCntrlConfig);
        break;
      }
    }

      // select the first Tab
    mpTabbedPanel.setSelectedComponent(mpPnlTimedEvents);

    // set the Tab change listener
    mpTabbedPanel.addChangeListener(new ChangeListener()
    {
        // This method is called whenever the selected tab changes
      public void stateChanged(ChangeEvent evt)
      {
        JTabbedPane pane = (JTabbedPane) evt.getSource();

          // Get current tab
        miSelectedTabIndex = pane.getSelectedIndex();
          // refresh the display
        refreshDisplay();
        }
      }
    );
  }
  
  /**
   * Method to build Configuration panel
   * @return <code>JPanel</code> object of the Configuration panel
   */
  private JPanel buildConfigPanel(ConfigurationFrame ipConfigFrame)
  {
    JPanel vpPanel = new JPanel();
    vpPanel.setLayout(new BorderLayout());
    vpPanel.add(ipConfigFrame.getSearchPanel(), BorderLayout.NORTH);
    vpPanel.add(ipConfigFrame.getScrollPanel(), BorderLayout.CENTER);
    return vpPanel;
  }
  
  /**
   * Method to refresh the display
   */
  private void refreshDisplay()
  {
    if (miSelectedTabIndex >= 0)
    {
      switch (miSelectedTabIndex)
      {
      case TAB_TIMEDEVENT:
        mpSktable = mpTimedEventFrame.getSktable();
        msSingularRowDescription = "timed event entry";
        msPluralRowDescription = "timed event entries";
        break;
      case TAB_DFTSTATION:
        mpSktable = mpDefaultStationFrame.getSktable();
        msSingularRowDescription = "default station";
        msPluralRowDescription = msSingularRowDescription + "s";
        break;
      case TAB_SYSCONFIG:
        mpSktable = mpSysConfigFrame.getSktable();
        msSingularRowDescription = mpSysConfigFrame.getSingularRowDescription();
        msPluralRowDescription = mpSysConfigFrame.getPluralRowDescription();
        break;
      case TAB_CNTLCONFIG:
        mpSktable = mpControllerConfigFrame.getSktable();
        msSingularRowDescription = mpControllerConfigFrame.getSingularRowDescription();
        msPluralRowDescription = mpControllerConfigFrame.getPluralRowDescription();
        break;
      }
      refreshTable();
    }
  }

  /**
   * Method to refresh displayed table
   */
  private void refreshTable()
  {
    try
    {
      switch (miSelectedTabIndex)
      {
      case TAB_TIMEDEVENT:
        mpCCD.clear();
        mpCCD.setWildcardKey(ControllerConfigData.CONTROLLER_NAME, ControllerConfig.TIMEDEVENTSCHEDULER, false);
        mpCCD.setWildcardKey(ControllerConfigData.PROPERTYNAME_NAME, "Task", false);
        if (mzSuperUser == false)
        {
          mpCCD.setKey(ControllerConfigData.SCREENCHANGEALLOWED_NAME, DBConstants.YES);
        }
        mpCCD.addOrderByColumn(ControllerConfigData.PROPERTYNAME_NAME);
        mpTimedEventFrame.refreshTable(mpCC.getAllElements(mpCCD));
        break;
      case TAB_DFTSTATION:
        mpSCD.clear();
        mpSCD.setWildcardKey(SysConfigData.GROUP_NAME, SysConfig.DEFAULT_STATION, false);
        mpSCD.addOrderByColumn(SysConfigData.PARAMETERNAME_NAME);
        mpSCD.addOrderByColumn(SysConfigData.PARAMETERVALUE_NAME);
        mpSCD.addOrderByColumn(SysConfigData.SCREENTYPE_NAME);
        mpDefaultStationFrame.refreshTable(mpSC.getAllElements(mpSCD));
        break;
      case TAB_SYSCONFIG:
        mpSCD.clear();
        mpSCD.setKey(SysConfigData.GROUP_NAME, SysConfig.DEFAULT_STATION, KeyObject.NOT_EQUAL);
        if (mzSuperUser == false)
        {
          mpSCD.setKey(SysConfigData.SCREENCHANGEALLOWED_NAME, DBConstants.YES);
        }
        mpSCD.addOrderByColumn(SysConfigData.GROUP_NAME);
        mpSCD.addOrderByColumn(SysConfigData.PARAMETERNAME_NAME);
        mpSysConfigFrame.refreshTable(mpSC.getAllElements(mpSCD));
        break;
      case TAB_CNTLCONFIG:
        mpCCD.clear();
        mpCCD.setKey(ControllerConfigData.CONTROLLER_NAME, ControllerConfig.TIMEDEVENTSCHEDULER, KeyObject.NOT_EQUAL);
        if (mzSuperUser == false)
        {
          mpCCD.setKey(ControllerConfigData.SCREENCHANGEALLOWED_NAME, DBConstants.YES);
        }
        mpCCD.addOrderByColumn(ControllerConfigData.CONTROLLER_NAME);
        mpCCD.addOrderByColumn(ControllerConfigData.PROPERTYNAME_NAME);
        mpControllerConfigFrame.refreshTable(mpCC.getAllElements(mpCCD));
        break;
      }
      displayTableCount();
    }
    catch (DBException e)
    {
      e.printStackTrace(System.err);
      displayError("Database Error: " + e);
    }
  }
  
  /**
   * Display the count
   */
  protected void displayTableCount()
  {
    int vnEntries = mpSktable.getRowCount();
    switch (vnEntries)
    {
      case 0:
        displayInfoAutoTimeOut("No data found");
        break;
        
      case 1:
        displayInfoAutoTimeOut("1 " + msSingularRowDescription + " found");
        break;
        
      default:
        // Make it so you don't need a translation for every possible number
        String vsDisplay = DacTranslator.getTranslation("%d "
            + msPluralRowDescription + " found");
        vsDisplay = vsDisplay.replaceFirst("%d", "" + vnEntries);
        displayInfoAutoTimeOut(vsDisplay);
    }
  }

  /**
   * Add a Timed Event
   */
  private void addTimedEvent()
  {
    JPanel[] vpButtonPanel = new JPanel[] {mpTimedEventFrame.getButtonPanel()};
    UpdateTimedEvent vpUpdateFrame = Factory.create(UpdateTimedEvent.class,
                                                    "Add Event Parameter");
    addSKDCInternalFrameModal(vpUpdateFrame, vpButtonPanel, new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if (prop.equals(FRAME_CHANGE))
            {
              refreshDisplay();
            }
          }
        });
  }

  /**
   * Add a Default Station
   */
  private void addDefaultStation()
  {
    JPanel[] vpButtonPanel = new JPanel[] {mpDefaultStationFrame.getButtonPanel()};
    UpdateDefaultStation vpUpdateFrame = Factory.create(UpdateDefaultStation.class, 
                                                        "Add Default Station");
    addSKDCInternalFrameModal(vpUpdateFrame, vpButtonPanel, new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if (prop.equals(FRAME_CHANGE))
            {
              refreshDisplay();
            }
          }
        });
  }
  
  /**
   * Add a SysConfig
   */
  private void addSysConfig()
  {
    JPanel[] vpButtonPanel = new JPanel[] {mpSysConfigFrame.getButtonPanel()};
    UpdateSysConfig vpUpdateFrame = Factory.create(UpdateSysConfig.class,
                                                    "Add SysConfig");
    addSKDCInternalFrameModal(vpUpdateFrame, vpButtonPanel, new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if (prop.equals(FRAME_CHANGE))
            {
              refreshDisplay();
            }
          }
        });
  }
  
  /**
   * Add a Controller Config
   */
  private void addControllerConfig()
  {
    JPanel[] vpButtonPanel = new JPanel[] {mpControllerConfigFrame.getButtonPanel()};
    UpdateControllerConfig vpUpdateFrame = Factory.create(UpdateControllerConfig.class,
                                                          "Add Controller Config");
    addSKDCInternalFrameModal(vpUpdateFrame, vpButtonPanel, new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if (prop.equals(FRAME_CHANGE))
            {
              refreshDisplay();
            }
          }
        });
  }

  /**
   * Modify selected Timed Event
   */
  private void modifyTimedEvent()
  {
    ControllerConfigData vpCCD = Factory.create(ControllerConfigData.class);  
    vpCCD.dataToSKDCData(mpSktable.getSelectedRowData());
    
    /*
     * Do the update
     */
    UpdateTimedEvent updateEvent = Factory.create(UpdateTimedEvent.class,
                                                  "Modify Event Parameter");
    updateEvent.setModify(vpCCD,  ePerms.iModifyAllowed);
    JPanel[] vpButtonPanel = new JPanel[] {mpTimedEventFrame.getButtonPanel()};
    
    addSKDCInternalFrameModal(updateEvent, vpButtonPanel, new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent e)
          {
            String prop = e.getPropertyName();
            if (prop.equals(FRAME_CHANGE))
            {
              refreshDisplay();
            }
          }
        });
  }
  
  /**
   * Modify selected Default Station
   */
  private void modifyDefaultStation()
  {
    SysConfigData vpSCD = Factory.create(SysConfigData.class);  
    vpSCD.dataToSKDCData(mpSktable.getSelectedRowData());
    
    /*
     * Do the update
     */
    UpdateDefaultStation updateDftStn = Factory.create(UpdateDefaultStation.class,
                                                        "Modify Station Parameter");
    updateDftStn.setModify(vpSCD, ePerms.iModifyAllowed);
    JPanel[] vpButtonPanel = new JPanel[] {mpDefaultStationFrame.getButtonPanel()};
    
    addSKDCInternalFrameModal(updateDftStn, vpButtonPanel, new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent e)
          {
            String prop = e.getPropertyName();
            if (prop.equals(FRAME_CHANGE))
            {
              refreshDisplay();
            }
          }
        });
  }

  /**
   * Modify selected SysConfig
   */
  private void modifySysConfig()
  {
    SysConfigData vpSCD = Factory.create(SysConfigData.class);  
    vpSCD.dataToSKDCData(mpSktable.getSelectedRowData());
    
    /*
     * Do the update
     */
    UpdateSysConfig updateSysConfig = Factory.create(UpdateSysConfig.class,
                                                      "Modify System Config");
    updateSysConfig.setModify(vpSCD, ePerms.iModifyAllowed);
    JPanel[] vpButtonPanel = new JPanel[] {mpSysConfigFrame.getButtonPanel()};

    addSKDCInternalFrameModal(updateSysConfig, vpButtonPanel, new PropertyChangeListener()
      {
        public void propertyChange(PropertyChangeEvent e)
        {
          String prop = e.getPropertyName();
          if (prop.equals(FRAME_CHANGE))
          {
            refreshDisplay();
          }
        }
      });
  }

  /**
   * Modify selected Controller Config
   */
  private void modifyControllerConfig()
  {
    ControllerConfigData vpCCD = Factory.create(ControllerConfigData.class);  
    vpCCD.dataToSKDCData(mpSktable.getSelectedRowData());
    
    /*
     * Do the update
     */
    UpdateControllerConfig updateEvent = Factory.create(UpdateControllerConfig.class,
                                                  "Modify Controller Config");
    updateEvent.setModify(vpCCD, ePerms.iModifyAllowed);
    JPanel[] vpButtonPanel = new JPanel[] {mpControllerConfigFrame.getButtonPanel()};

    addSKDCInternalFrameModal(updateEvent, vpButtonPanel, new PropertyChangeListener()
      {
        public void propertyChange(PropertyChangeEvent e)
        {
          String prop = e.getPropertyName();
          if (prop.equals(FRAME_CHANGE))
          {
            refreshDisplay();
          }
        }
      });
  }

  /**
   * Delete selected Timed Event
   */
  private void deleteTimedEvent()
  {
    ControllerConfigData vpCCD = Factory.create(ControllerConfigData.class);
    ControllerConfig vpCC = Factory.create(ControllerConfig.class);
    
    int vnDelCount = 0;
    int vnTotalSelected = mpSktable.getSelectedRowCount();
    int[] vanDeleteIndices = mpSktable.getSelectedRows();
    for(int row = 0; row < vnTotalSelected; row++)
    {
      vpCCD.dataToSKDCData(mpSktable.getSelectedRowData());
      String vsController = vpCCD.getController();
      String vsName = vpCCD.getPropertyName();
      String vsValue = vpCCD.getPropertyValue();
     
      TransactionToken vpTT = null;
      try
      {
        vpCCD.setKey(ControllerConfigData.CONTROLLER_NAME, vsController);
        vpCCD.setKey(ControllerConfigData.PROPERTYNAME_NAME, vsName);
        vpCCD.setKey(ControllerConfigData.PROPERTYVALUE_NAME, vsValue);
        vpTT = mpDBObject.startTransaction();
        vpCC.deleteElement(vpCCD);
        mpDBObject.commitTransaction(vpTT);
        mpSktable.deselectRow(vanDeleteIndices[row]);
        vnDelCount++;
      }
      catch(DBException exc)
      {
        displayError(exc.getMessage(), "Delete Error");
                                     // De-Select the troubling row!
        mpSktable.deselectRow(vanDeleteIndices[row]);
      }
      finally
      {
        mpDBObject.endTransaction(vpTT);
      }
    }
    if (vnDelCount > 0)
    {
      String vsMsg = "Deleted " +  vnDelCount + " of " + vnTotalSelected +
                      " selected Row";
      if (vnDelCount > 1)
      {
        vsMsg = vsMsg + "s";
      }
      displayInfoAutoTimeOut(vsMsg, "Delete Result");
      refreshDisplay();
    }
  }
  
  /**
   * Delete selected Default Station
   */
  private void deleteDefaultStation()
  {
    SysConfigData vpSCD = Factory.create(SysConfigData.class);
    SysConfig vpSC = Factory.create(SysConfig.class);
    
    int vnDelCount = 0;
    int vnTotalSelected = mpSktable.getSelectedRowCount();
    int[] vanDeleteIndices = mpSktable.getSelectedRows();
    for(int row = 0; row < vnTotalSelected; row++)
    {
      vpSCD.dataToSKDCData(mpSktable.getSelectedRowData());
     
      TransactionToken vpTT = null;
      try
      {
        vpSCD.setKey(SysConfigData.GROUP_NAME, SysConfig.DEFAULT_STATION);
        vpSCD.setKey(SysConfigData.PARAMETERNAME_NAME, vpSCD.getParameterName());
        vpSCD.setKey(SysConfigData.PARAMETERVALUE_NAME, vpSCD.getParameterValue());
        vpSCD.setKey(SysConfigData.SCREENTYPE_NAME, vpSCD.getScreenType());
        vpTT = mpDBObject.startTransaction();
        vpSC.deleteElement(vpSCD);
        mpDBObject.commitTransaction(vpTT);
        mpSktable.deselectRow(vanDeleteIndices[row]);
        vnDelCount++;
      }
      catch(DBException exc)
      {
        displayError(exc.getMessage(), "Delete Error");
                                     // De-Select the troubling row!
        mpSktable.deselectRow(vanDeleteIndices[row]);
      }
      finally
      {
        mpDBObject.endTransaction(vpTT);
      }
    }
    if (vnDelCount > 0)
    {
      String vsMsg = "Deleted " +  vnDelCount + " of " + vnTotalSelected +
                      " selected Default Station";
      if (vnDelCount > 1)
      {
        vsMsg = vsMsg + "s";
      }
      displayInfoAutoTimeOut(vsMsg, "Delete Result");
      refreshDisplay();
    }
  }
  
  /**
   * Delete selected SysConfig
   */
  private void deleteSysConfig()
  {
    SysConfigData vpSCD = Factory.create(SysConfigData.class);
    SysConfig vpSC = Factory.create(SysConfig.class);
    
    int vnDelCount = 0;
    int vnTotalSelected = mpSktable.getSelectedRowCount();
    int[] vanDeleteIndices = mpSktable.getSelectedRows();
    for(int row = 0; row < vnTotalSelected; row++)
    {
      vpSCD.dataToSKDCData(mpSktable.getSelectedRowData());
     
      TransactionToken vpTT = null;
      try
      {
        vpSCD.setKey(SysConfigData.GROUP_NAME, vpSCD.getGroup());
        vpSCD.setKey(SysConfigData.PARAMETERNAME_NAME, vpSCD.getParameterName());
        vpTT = mpDBObject.startTransaction();
        vpSC.deleteElement(vpSCD);
        mpDBObject.commitTransaction(vpTT);
        mpSktable.deselectRow(vanDeleteIndices[row]);
        vnDelCount++;
      }
      catch(DBException exc)
      {
        displayError(exc.getMessage(), "Delete Error");
                                     // De-Select the troubling row!
        mpSktable.deselectRow(vanDeleteIndices[row]);
      }
      finally
      {
        mpDBObject.endTransaction(vpTT);
      }
    }
    if (vnDelCount > 0)
    {
      String vsMsg = "Deleted " +  vnDelCount + " of " + vnTotalSelected +
                      " selected Parameter";
      if (vnDelCount > 1)
      {
        vsMsg = vsMsg + "s";
      }
      displayInfoAutoTimeOut(vsMsg, "Delete Result");
      refreshDisplay();
    }
  }

  /**
   * Delete selected Controller Config
   */
  private void deleteControllerConfig()
  {
    ControllerConfigData vpCCD = Factory.create(ControllerConfigData.class);
    ControllerConfig vpCC = Factory.create(ControllerConfig.class);
    
    int vnDelCount = 0;
    int vnTotalSelected = mpSktable.getSelectedRowCount();
    int[] vanDeleteIndices = mpSktable.getSelectedRows();
    for(int row = 0; row < vnTotalSelected; row++)
    {
      vpCCD.dataToSKDCData(mpSktable.getSelectedRowData());
     
      TransactionToken vpTT = null;
      try
      {
        vpCCD.setKey(ControllerConfigData.CONTROLLER_NAME, vpCCD.getController());
        vpCCD.setKey(ControllerConfigData.PROPERTYNAME_NAME, vpCCD.getPropertyName());
        vpTT = mpDBObject.startTransaction();
        vpCC.deleteElement(vpCCD);
        mpDBObject.commitTransaction(vpTT);
        mpSktable.deselectRow(vanDeleteIndices[row]);
        vnDelCount++;
      }
      catch(DBException exc)
      {
        displayError(exc.getMessage(), "Delete Error");
                                     // De-Select the troubling row!
        mpSktable.deselectRow(vanDeleteIndices[row]);
      }
      finally
      {
        mpDBObject.endTransaction(vpTT);
      }
    }
    if (vnDelCount > 0)
    {
      String vsMsg = "Deleted " +  vnDelCount + " of " + vnTotalSelected +
                      " selected Row";
      if (vnDelCount > 1)
      {
        vsMsg = vsMsg + "s";
      }
      displayInfoAutoTimeOut(vsMsg, "Delete Result");
      refreshDisplay();
    }
  }
}
