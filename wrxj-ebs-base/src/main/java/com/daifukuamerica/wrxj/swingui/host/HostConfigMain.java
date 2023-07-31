package com.daifukuamerica.wrxj.swingui.host;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dataserver.standard.StandardConfigurationServer;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.HostConfig;
import com.daifukuamerica.wrxj.dbadapter.data.HostConfigData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;


/**
 * Description:<BR>
 *    Primary frame for configuring a Host interface.
 *
 * @author       A.D.
 * @version      1.0      18-Jun-2007
 */
@SuppressWarnings("serial")
public class HostConfigMain extends SKDCInternalFrame
{
  private static String XML_RADIO_NAME;
  private static String DELIMITED_RADIO_NAME;
  private static String FIXEDLENGTH_RADIO_NAME;

  private static final String DATA_TYPE_KEY = "ActiveDataType";
  private static final String COMM_TYPE_KEY = "ActiveTransportType";
  private static final String ACCEPT_CHANGE_BTN = "AcceptChanges";
  private static final String CONFIG_HOST_OUT_BTN = "ConfigHostOut";
  private static final String FORMAT_BORDER_TITLE = "Data Formats";
  private static final String TRANSPORT_BORDER_TITLE = "Transports";
  private static final String TRANSPORT_TAB_NAME = "Transport Config.";
  private static final String CONTROLLERS_TAB_NAME = "Controllers";
  private static final String PARSERS_TAB_NAME = "Parsers";
  private static final String FORMATTERS_TAB_NAME = "Formatters";
  
  private JTabbedPane     mpTabbedPane;
  private HostConfigPanel mpFormatterPanel;
  private HostConfigPanel mpParserPanel;
  private HostConfigPanel mpControllerPanel;
  private HostConfigPanel mpTransConfigPanel;
 
  private SKDCButton    mpAddButton;
  private SKDCButton    mpModifyButton;
  private SKDCButton    mpDeleteButton;
  private SKDCButton    mpAccpChanges;
  private SKDCButton    mpReset;
  private SKDCButton    mpConfigHostOut;
                                       // Data types.
  private JRadioButton  mpXMLRadioBtn;
  private JRadioButton  mpDelimitedRadioBtn;
  private JRadioButton  mpFixedLengthRadioBtn;
                                       // Communication types.
  private JRadioButton  mpJdbcOracle;
  private JRadioButton  mpTCPIP;
  private JRadioButton  mpJdbcSqlServer;
  private JRadioButton  mpJdbcDB2;
                                       // Radio Button Groups
  private ButtonGroup mpCommTypeBGroup;
  private ButtonGroup mpDataTypeBGroup;
  
  private StandardConfigurationServer mpConfigServ;
  private String  msSelectedDataType;
  private String  msSelectedCommType;
  private HashMap<String, JRadioButton> mpActiveConfigMap = new HashMap<String, JRadioButton>();
  
  public HostConfigMain()
  {
    super("Host System Configuration");
    try
    {
      XML_RADIO_NAME = DBTrans.getStringValue(HostConfigData.DATA_FORMAT_TRAN_NAME, DBConstants.XML);
      DELIMITED_RADIO_NAME = DBTrans.getStringValue(HostConfigData.DATA_FORMAT_TRAN_NAME, DBConstants.DELIMITED);
      FIXEDLENGTH_RADIO_NAME = DBTrans.getStringValue(HostConfigData.DATA_FORMAT_TRAN_NAME, DBConstants.FIXEDLENGTH);
    }
    catch(NoSuchFieldException nsf)
    {
      displayError("Exiting screen. No translations found for Radio Button Names!");
      return;
    }

    mpConfigServ = Factory.create(StandardConfigurationServer.class);
    createButtons();
    createPanels();
    createTabbedPane();
    setButtonListeners();
    Container contentPane = getContentPane();
    contentPane.add(buildWestPanel(), BorderLayout.WEST);
    contentPane.add(mpTabbedPane, BorderLayout.CENTER);
//    contentPane.add(configureButtonPanel(), BorderLayout.SOUTH);
    setToDefaultConfig();
  }

  @Override
  public Dimension getPreferredSize()
  {                                    // Search frame Width and Height.
    return(new Dimension(895, 420));
  }

  /**
   * Create buttons
   */
  private void createButtons()
  {
    mpAddButton = new SKDCButton("Add Config.", "Add Configuration to a group",
        'A');
    mpModifyButton = new SKDCButton("Modify Config.",
        "Modify selected Configuration", 'M');
    mpDeleteButton = new SKDCButton("Delete Config.",
        "Delete Selected Configuration(s)", 'D');
    mpAccpChanges = new SKDCButton("Accept",
        "Update to displayed Configuration.", 'C');
    mpReset = new SKDCButton("Reset", "Reset to original settings", 'R');
    mpConfigHostOut = new SKDCButton("Configure Host Out",
            "Host Out Configuration", 'H');
  }
/*
 * ===========================================================================
 * ***** Event Listeners go here ******
 * ===========================================================================
 */
  /**
   *  Defines all buttons on the main Host Config frame, including Radio Buttons,
   *  and adds listeners to them.
   */
  private void setButtonListeners()
  {
    ActionListener vpButtonListener = new ConfigButtonListener();
    mpAddButton.addEvent(ADD_BTN, vpButtonListener);
    mpModifyButton.addEvent(MODIFY_BTN, vpButtonListener);
    mpDeleteButton.addEvent(DELETE_BTN, vpButtonListener);
    mpAccpChanges.addEvent(ACCEPT_CHANGE_BTN, vpButtonListener);
    mpConfigHostOut.addEvent(CONFIG_HOST_OUT_BTN, vpButtonListener);
    mpReset.addEvent(RESET_BTN, vpButtonListener);
    
    ItemListener vpItemListener = new RadioButtonListener();
                                       // Setup Transport Type Radio Buttons.
    mpCommTypeBGroup = new ButtonGroup();
    
    mpJdbcOracle = new JRadioButton();
    mpJdbcOracle.setName(HostConfigData.JDBC_ORACLE_TRANSPORT);
    mpJdbcOracle.addItemListener(vpItemListener);
    mpCommTypeBGroup.add(mpJdbcOracle);

    mpJdbcSqlServer = new JRadioButton();
    mpJdbcSqlServer.setName(HostConfigData.JDBC_SQLSERVER_TRANSPORT);
    mpJdbcSqlServer.addItemListener(vpItemListener);
    mpCommTypeBGroup.add(mpJdbcSqlServer);

    mpJdbcDB2 = new JRadioButton();
    mpJdbcDB2.setName(HostConfigData.JDBC_DB2_TRANSPORT);
    mpJdbcDB2.addItemListener(vpItemListener);
    mpCommTypeBGroup.add(mpJdbcDB2);

    mpTCPIP = new JRadioButton();
    mpTCPIP.setName(HostConfigData.TCPIP_TRANSPORT);
    mpTCPIP.addItemListener(vpItemListener);
    mpCommTypeBGroup.add(mpTCPIP);
                                       // Setup Data Type Radio Button.
    mpDataTypeBGroup = new ButtonGroup();
    mpXMLRadioBtn = new JRadioButton();
    mpXMLRadioBtn.setName(XML_RADIO_NAME);
    mpXMLRadioBtn.addItemListener(vpItemListener);
    mpDataTypeBGroup.add(mpXMLRadioBtn);
    
    mpDelimitedRadioBtn = new JRadioButton();
    mpDelimitedRadioBtn.setName(DELIMITED_RADIO_NAME);
    mpDelimitedRadioBtn.addItemListener(vpItemListener);
    mpDataTypeBGroup.add(mpDelimitedRadioBtn);
    
    mpFixedLengthRadioBtn = new JRadioButton();
    mpFixedLengthRadioBtn.setName(FIXEDLENGTH_RADIO_NAME);
    mpFixedLengthRadioBtn.addItemListener(vpItemListener);
    mpDataTypeBGroup.add(mpFixedLengthRadioBtn);
  }

  private class ConfigButtonListener implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      String vsPressedButton = e.getActionCommand();
      if (vsPressedButton.equals(ADD_BTN))
      {
        addButtonPressed();
      }
      else if (vsPressedButton.equals(MODIFY_BTN))
      {
        modifyButtonPressed();
      }
      else if (vsPressedButton.equals(DELETE_BTN))
      {
        deleteButtonPressed();
      }
      else if (vsPressedButton.equals(ACCEPT_CHANGE_BTN))
      {
        acceptChangesButtonPressed();
      }
      else if (vsPressedButton.equals(CONFIG_HOST_OUT_BTN))
      {
        configureHostOutButtonPressed();
      }
      else if (vsPressedButton.equals(RESET_BTN))
      {
        resetButtonPressed();
      }
    }
  }
  
  private class RadioButtonListener implements ItemListener
  {
    @Override
    public void itemStateChanged(ItemEvent vpEvt)
    {
      if (vpEvt.getStateChange() == ItemEvent.SELECTED)
      {
        Object vpSourceObj = vpEvt.getSource();
        
        if (vpSourceObj == mpXMLRadioBtn ||vpSourceObj == mpDelimitedRadioBtn ||
            vpSourceObj == mpFixedLengthRadioBtn)
        {
          if (vpSourceObj == mpActiveConfigMap.get(DATA_TYPE_KEY))
          {
            updateGlobalDataType(vpSourceObj);
            populateFormatterTable();
            populateParserTable();
            mpAccpChanges.setEnabled(false);
          }
          else
          {
            updateGlobalDataType(vpSourceObj);
            populateParserTable(msSelectedDataType);
            populateFormatterTable(msSelectedDataType);
            mpAccpChanges.setEnabled(true);
          }
        }
        else if (vpSourceObj == mpJdbcOracle || vpSourceObj == mpTCPIP
            || vpSourceObj == mpJdbcDB2 || vpSourceObj == mpJdbcSqlServer)
        {
          if (vpEvt.getItem() == mpActiveConfigMap.get(COMM_TYPE_KEY))
          {
            updateGlobalCommType(vpSourceObj);
            populateTransporterConfig();
            populateControllerTable();
            mpAccpChanges.setEnabled(false);
          }
          else
          {
            updateGlobalCommType(vpSourceObj);
            populateTransporterConfig(msSelectedCommType);
            populateControllerTable(msSelectedCommType);
            mpAccpChanges.setEnabled(true);
          }
        }
      }
    }
  }

  private void updateGlobalDataType(Object ipRadioSource)
  {
    if (ipRadioSource == mpXMLRadioBtn)
      msSelectedDataType = XML_RADIO_NAME;
    else if (ipRadioSource == mpDelimitedRadioBtn)
      msSelectedDataType = DELIMITED_RADIO_NAME;
    else if (ipRadioSource == mpFixedLengthRadioBtn)
      msSelectedDataType = FIXEDLENGTH_RADIO_NAME;
  }
  
  private void updateGlobalCommType(Object ipRadioSource)
  {
    if (ipRadioSource == mpJdbcOracle)
      msSelectedCommType = HostConfigData.JDBC_ORACLE_TRANSPORT;
    if (ipRadioSource == mpJdbcSqlServer)
      msSelectedCommType = HostConfigData.JDBC_SQLSERVER_TRANSPORT;
    if (ipRadioSource == mpJdbcDB2)
      msSelectedCommType = HostConfigData.JDBC_DB2_TRANSPORT;
    if (ipRadioSource == mpTCPIP)
      msSelectedCommType = HostConfigData.TCPIP_TRANSPORT;
  }
  
/*===========================================================================
            ****** Button pressed action methods go here ******
  ===========================================================================*/
 /**
  *  Method allows a message(s) to be marked as processed or unprocessed.
  */
  private void addButtonPressed()
  {
    displayInfo("Add Requested.");
  }

 /**
  *  Method handles the modify button pressed
  */
  private void modifyButtonPressed()
  {
    String vsTabName = mpTabbedPane.getTitleAt(mpTabbedPane.getSelectedIndex());
    if(vsTabName.equals(TRANSPORT_TAB_NAME))
    {
      String vsOriginalDataType = mpActiveConfigMap.get(COMM_TYPE_KEY).getName();
      if(msSelectedCommType.equals(vsOriginalDataType))
      {
        mpTransConfigPanel.commitEdit();
        updateTransportConfig();
      }
      else
      {
        displayInfo("Transports changes must be Accepted or Reset before modifying Transport Config.");
        return;
      }
    }
  }

  void deleteButtonPressed()
  {
    displayInfo("Delete Requested.");
//    addSKDCInternalFrameModal(searchFrame, ipanel,
//                              new LocationDetailSearchFrameHandler());
  }

  void acceptChangesButtonPressed()
  {
    if (displayYesNoPrompt("Change active configuration to " + SKDCConstants.EOL_CHAR +
                           "displayed settings"))
    {
      try
      {
        String vsOriginalDataType = mpActiveConfigMap.get(DATA_TYPE_KEY).getName();
        if (!msSelectedDataType.equals(vsOriginalDataType))
        {
          mpConfigServ.updateActiveHostDataType(msSelectedDataType);
          updateLocalActiveMap(DATA_TYPE_KEY);
          mpAccpChanges.setEnabled(false);
          populateTransporterConfig();
        }

        String vsOriginalCommType = mpActiveConfigMap.get(COMM_TYPE_KEY).getName();
        if (!msSelectedCommType.equals(vsOriginalCommType))
        {
          mpConfigServ.updateActiveHostCommType(msSelectedCommType);
          updateLocalActiveMap(COMM_TYPE_KEY);
          mpAccpChanges.setEnabled(false);
          populateTransporterConfig();
        }
      }
      catch(DBException ex)
      {
        displayError("Error updating to currently selected Active Configuration! " +
                     ex.getMessage());
      }
    }
  }

  /**
   * Method to configure Host Out Access
   */
  void configureHostOutButtonPressed()
  {
    addSKDCInternalFrameModal(new HostOutAccessListFrame());
  }

 /**
  * Handler for Reset button.
  */
  void resetButtonPressed()
  {
    String vsOriginalDataType = mpActiveConfigMap.get(DATA_TYPE_KEY).getName();
    String vsOriginalCommType = mpActiveConfigMap.get(COMM_TYPE_KEY).getName();
    if (!msSelectedDataType.equals(vsOriginalDataType) ||
        !msSelectedCommType.equals(vsOriginalCommType))
    {
      setToDefaultConfig();
      mpTabbedPane.setSelectedIndex(0);
      mpAccpChanges.setEnabled(false);
    }
    else
    {
      displayInfo("No changes made!");
    }
  }
  
/*===========================================================================
              ****** All other private methods go here ******
  ===========================================================================*/
  private void createPanels()
  {
    mpFormatterPanel  = new HostConfigPanel();
    mpParserPanel     = new HostConfigPanel();
    mpControllerPanel = new HostConfigPanel();
    mpTransConfigPanel = new HostConfigPanel(mpModifyButton);
  }
  
  private void createTabbedPane()
  {
    mpTabbedPane = new JTabbedPane();
    mpTabbedPane.addTab(FORMATTERS_TAB_NAME, mpFormatterPanel);
    mpTabbedPane.addTab(PARSERS_TAB_NAME, mpParserPanel);
    mpTabbedPane.addTab(CONTROLLERS_TAB_NAME, mpControllerPanel);
    mpTabbedPane.addTab(TRANSPORT_TAB_NAME, mpTransConfigPanel);
  }
  
  private JPanel buildWestPanel()
  {
    JPanel vpWestPanel = new JPanel();
    vpWestPanel.setLayout(new BoxLayout(vpWestPanel, BoxLayout.Y_AXIS));
    vpWestPanel.add(Box.createVerticalStrut(20));
    vpWestPanel.add(setupDataFormatPanel());
    vpWestPanel.add(Box.createVerticalStrut(10));
    vpWestPanel.add(setupTransportTypePanel());
    vpWestPanel.add(setupWestButtonPanel());

    return(vpWestPanel);
  }

  private JPanel setupWestButtonPanel()
  {
    JPanel vpButtonPanel = new JPanel(new GridBagLayout());
    GridBagConstraints vpGBConstraint = new GridBagConstraints();

    vpGBConstraint.gridwidth = 1;
    vpGBConstraint.weighty = 0.5;
    vpGBConstraint.fill = GridBagConstraints.HORIZONTAL;    

    vpGBConstraint.insets = new Insets(15, 1, 5, 1);
    vpGBConstraint.gridx = vpGBConstraint.gridy = 0;
    vpGBConstraint.anchor = GridBagConstraints.WEST;
    vpButtonPanel.add(mpAccpChanges, vpGBConstraint);

    vpGBConstraint.insets.set(5, 1, 15, 1);
    vpGBConstraint.gridy = GridBagConstraints.RELATIVE;
    vpButtonPanel.add(mpReset, vpGBConstraint);
    vpButtonPanel.add(mpConfigHostOut, vpGBConstraint);

    return(vpButtonPanel);
  }

  private JPanel setupTransportTypePanel()
  {
    Border vpBorder = BorderFactory.createEtchedBorder();
    TitledBorder vpTitledBorder = BorderFactory.createTitledBorder(vpBorder,
                                                                   TRANSPORT_BORDER_TITLE);
    JPanel vpTransportTypePanel = new JPanel(new GridBagLayout());
    vpTransportTypePanel.setBorder(vpTitledBorder);

    GridBagConstraints vpGBConstraint = new GridBagConstraints();
    vpGBConstraint.insets = new Insets(1, 2, 5, 1);
    vpGBConstraint.gridwidth = 1;
    vpGBConstraint.weighty = 0.8;

    vpGBConstraint.gridx = vpGBConstraint.gridy = 0;
    vpGBConstraint.anchor = GridBagConstraints.WEST;
    vpTransportTypePanel.add(new SKDCLabel("JDBC Oracle "), vpGBConstraint);
    vpGBConstraint.gridy = GridBagConstraints.RELATIVE;
    vpTransportTypePanel.add(new SKDCLabel("JDBC SQL Server "), vpGBConstraint);
    vpGBConstraint.gridy = GridBagConstraints.RELATIVE;
    vpTransportTypePanel.add(new SKDCLabel("JDBC DB2 "), vpGBConstraint);
    vpGBConstraint.gridy = GridBagConstraints.RELATIVE;
    vpTransportTypePanel.add(new SKDCLabel("TCPIP "), vpGBConstraint);

    vpGBConstraint.gridx = 1;
    vpGBConstraint.gridy = 0;
    vpGBConstraint.anchor = GridBagConstraints.EAST;
    vpTransportTypePanel.add(mpJdbcOracle, vpGBConstraint);
    vpGBConstraint.gridy = GridBagConstraints.RELATIVE;
    vpTransportTypePanel.add(mpJdbcSqlServer, vpGBConstraint);
    vpGBConstraint.gridy = GridBagConstraints.RELATIVE;
    vpTransportTypePanel.add(mpJdbcDB2, vpGBConstraint);
    vpGBConstraint.gridy = GridBagConstraints.RELATIVE;
    vpTransportTypePanel.add(mpTCPIP, vpGBConstraint);

    return(vpTransportTypePanel);
  }

  private JPanel setupDataFormatPanel()
  {
    Border vpBorder = BorderFactory.createEtchedBorder();
    TitledBorder vpTitledBorder = BorderFactory.createTitledBorder(vpBorder,
                                                                   FORMAT_BORDER_TITLE);
    JPanel vpDataFormatPanel = new JPanel(new GridBagLayout());
    vpDataFormatPanel.setBorder(vpTitledBorder);

    GridBagConstraints vpGBConstraint = new GridBagConstraints();
    vpGBConstraint.insets = new Insets(1, 2, 5, 1);
    vpGBConstraint.gridwidth = 1;
    vpGBConstraint.weighty = 0.8;

    vpGBConstraint.gridx = vpGBConstraint.gridy = 0;
    vpGBConstraint.anchor = GridBagConstraints.EAST;
    vpDataFormatPanel.add(new SKDCLabel("XML "), vpGBConstraint);
    vpGBConstraint.gridy = GridBagConstraints.RELATIVE;
    vpDataFormatPanel.add(new SKDCLabel("Delimited "), vpGBConstraint);
    vpDataFormatPanel.add(new SKDCLabel("Fixed Length "), vpGBConstraint);

    vpGBConstraint.gridx = 1;
    vpGBConstraint.gridy = 0;
    vpGBConstraint.anchor = GridBagConstraints.WEST;
    vpDataFormatPanel.add(mpXMLRadioBtn, vpGBConstraint);
    vpGBConstraint.gridy = GridBagConstraints.RELATIVE;
    vpDataFormatPanel.add(mpDelimitedRadioBtn, vpGBConstraint);
    vpDataFormatPanel.add(mpFixedLengthRadioBtn, vpGBConstraint);

    return(vpDataFormatPanel);
  }

  private void populateParserTable()
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        StandardConfigurationServer vpConfigServ = Factory.create(StandardConfigurationServer.class);
        try
        {
          @SuppressWarnings("rawtypes")
          List<Map> vpParserList = vpConfigServ.getHostConfigList(HostConfigData.HOST_PARSER_GROUP_NAME,
                                                                  DBConstants.YES);
          mpParserPanel.update(vpParserList);
        }
        catch(DBException e)
        {
          displayError(e.getMessage(), "Data retrieve error");
        }
      }
    });
  }
  
  private void populateFormatterTable()
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      @SuppressWarnings("rawtypes")
      public void run()
      {
        StandardConfigurationServer vpConfigServ = Factory.create(StandardConfigurationServer.class);
        try
        {
          List<Map> vpFormatterList = vpConfigServ.getHostConfigList(HostConfigData.HOST_FORMATTER_GROUP_NAME,
                                                                     DBConstants.YES);
          List<Map> vpFormProp = vpConfigServ.getHostConfigList(msSelectedDataType + "Formatter",
                    HostConfigData.HOST_FORMAT_PROP_GROUP_NAME, DBConstants.YES);
          vpFormatterList.addAll(vpFormProp);
          mpFormatterPanel.update(vpFormatterList);
        }
        catch(DBException e)
        {
          displayError(e.getMessage(), "Data retrieve error");
        }
      }
    });
  }
  
  private void populateControllerTable()
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        StandardConfigurationServer vpConfigServ = Factory.create(StandardConfigurationServer.class);
        try
        {
          @SuppressWarnings("rawtypes")
          List<Map> vpControllerList = vpConfigServ.getHostConfigList(HostConfigData.HOST_CONTROLLER_GROUP_NAME,
                                                                      DBConstants.YES);
          mpControllerPanel.update(vpControllerList);
        }
        catch(DBException e)
        {
          displayError(e.getMessage(), "Data retrieve error");
        }
      }
    });
  }

  private void populateTransporterConfig()
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        StandardConfigurationServer vpConfigServ = Factory.create(StandardConfigurationServer.class);
        try
        {
          @SuppressWarnings("rawtypes")
          List<Map> vpTransporterList = vpConfigServ.getHostConfigList(HostConfigData.HOST_TRANSPORT_GROUP_NAME,
                                                                       DBConstants.YES);
          mpTransConfigPanel.update(vpTransporterList);
          mpTransConfigPanel.setColumnToEdit(HostConfigData.PARAMETERVALUE_NAME);
          mpTransConfigPanel.setNonEditableRowsByColumn(HostConfigData.PARAMETERNAME_NAME, "class", "driver", "maximum");
        }
        catch(DBException e)
        {
          displayError(e.getMessage(), "Data retrieve error");
        }
      }
    });
  }
  
  private void populateParserTable(final String isDataType)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        StandardConfigurationServer vpConfigServ = Factory.create(StandardConfigurationServer.class);
        try
        {
          @SuppressWarnings("rawtypes")
          List<Map> vpParserList = vpConfigServ.getHostConfigList(isDataType + HostConfigData.HOST_PARSER_GROUP_NAME);
          mpParserPanel.update(vpParserList);
        }
        catch(DBException e)
        {
          displayError(e.getMessage(), "Data retrieve error");
        }
      }
    });
  }
  
  /**
   * Method updates the database based on changes to the Transport Configuration Tab
   */
  @SuppressWarnings("rawtypes")
  private void updateTransportConfig()
  {
    try
    {
      List<Map> vpTransporterList = mpConfigServ.getHostConfigList(
          HostConfigData.HOST_TRANSPORT_GROUP_NAME, DBConstants.YES);
      List<Map> vpDisplayList = mpTransConfigPanel.getAllRows();
      if (vpTransporterList != null && vpDisplayList != null)
      {
        for (Map vpMap : vpTransporterList)
        {
          String vsConfigName = DBHelper.getStringField(vpMap,
              HostConfigData.PARAMETERNAME_NAME.trim());
          String vsConfigValue = DBHelper.getStringField(vpMap,
              HostConfigData.PARAMETERVALUE_NAME.trim());

          for (Map vpDisplayMap : vpDisplayList)
          {
            String vsDisplayConfigName = DBHelper.getStringField(vpDisplayMap,
                HostConfigData.PARAMETERNAME_NAME.trim());
            if (vsDisplayConfigName.equals(vsConfigName))
            {
              String vsDisplayConfigValue = DBHelper.getStringField(
                  vpDisplayMap, HostConfigData.PARAMETERVALUE_NAME.trim());
              if (!vsDisplayConfigValue.equals(vsConfigValue))
              {
                HostConfigData vpHostData = Factory.create(HostConfigData.class);
                vpHostData.dataToSKDCData(vpDisplayMap);
                try
                {
                  if (vpHostData.getParameterName().equals("HostName"))
                  {
                    
                    mpConfigServ.changeHostName(vpHostData.getDataHandler(), vpHostData.getGroup(),
                       vsConfigValue,
                        vpHostData.getParameterValue()); 
                    displayInfo("Host Name changed from: "+ vsConfigValue +
                        " to " + vpHostData.getParameterValue());

                  }
                  else
                  {
                    mpConfigServ.updateHostConfigValue(
                        vpHostData.getDataHandler(), vpHostData.getGroup(),
                        vpHostData.getParameterName(),
                        vpHostData.getParameterValue());
                    displayInfo("Updating " + vpHostData.getParameterName() + " From " + vsConfigValue + " To "
                        + vsDisplayConfigValue);
                  }
                }
                catch (DBException dbe)
                {
                  logAndDisplayError("Error updating transport config.");
                  populateTransporterConfig();
                }
              }
              break;
            }
          }
        }
      }
    }
    catch (DBException e)
    {
      displayError(e.getMessage(), "Data retrieve error");
    }
  }
  
  @SuppressWarnings("rawtypes")
  private void populateFormatterTable(final String isDataType)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        StandardConfigurationServer vpConfigServ = Factory.create(StandardConfigurationServer.class);
        try
        {
          List<Map> vpFormatterList = vpConfigServ.getHostConfigList(HostConfigData.HOST_FORMATTER_GROUP_NAME,
                                                                     DBConstants.YES);
          List<Map> vpFormProp = vpConfigServ.getHostConfigList(isDataType + "Formatter", 
                    HostConfigData.HOST_FORMAT_PROP_GROUP_NAME, DBConstants.NO);
          vpFormatterList.addAll(vpFormProp);
          mpFormatterPanel.update(vpFormatterList);
        }
        catch(DBException e)
        {
          displayError(e.getMessage(), "Data retrieve error");
        }
      }
    });
  }
  
  /**
   *  Finds a list of Host controllers that are using a particular protocol for
   *  communication.
   */
  private void populateControllerTable(final String isCommType)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        StandardConfigurationServer vpConfigServ = Factory.create(StandardConfigurationServer.class);
        try
        {
          @SuppressWarnings("rawtypes")
          List<Map> vpControllerList = vpConfigServ.getControllerWithProtocol(isCommType);
          mpControllerPanel.update(vpControllerList);
        }
        catch(DBException e)
        {
          displayError(e.getMessage(), "Data retrieve error");
        }
      }
    });
  }

  private void populateTransporterConfig(final String isDataType)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        StandardConfigurationServer vpConfigServ = Factory.create(StandardConfigurationServer.class);
        try
        {
          @SuppressWarnings("rawtypes")
          List<Map> vpTransporterList = vpConfigServ.getTransportDefinitions(HostConfigData.HOST_TRANSPORT_GROUP_NAME,
                                                                             isDataType, DBConstants.NO);
          mpTransConfigPanel.update(vpTransporterList);
        }
        catch(DBException e)
        {
          displayError(e.getMessage(), "Data retrieve error");
        }
      }
    });
  }

 /**
  *  Method updates radio button selection to reflect current settings of data
  *  format.
  *  @throws DBException if there is a data access error.
  */
  private void showActiveDataType() throws DBException
  {
    switch(Application.getInt(HostConfigData.ACTIVE_DATA_TYPE))
    {
      case DBConstants.XML:
        mpActiveConfigMap.put(DATA_TYPE_KEY, mpXMLRadioBtn);
        msSelectedDataType = XML_RADIO_NAME;
        mpXMLRadioBtn.setSelected(true);
        break;
        
      case DBConstants.DELIMITED:
        mpActiveConfigMap.put(DATA_TYPE_KEY, mpDelimitedRadioBtn);
        msSelectedDataType = DELIMITED_RADIO_NAME;
        mpDelimitedRadioBtn.setSelected(true);
        break;
        
      case DBConstants.FIXEDLENGTH:
        mpActiveConfigMap.put(DATA_TYPE_KEY, mpFixedLengthRadioBtn);
        msSelectedDataType = FIXEDLENGTH_RADIO_NAME;
        mpFixedLengthRadioBtn.setSelected(true);
    }
  }

 /**
  *  Method updates radio button selection to reflect current settings of data
  *  transport.
  *  @throws DBException if there is a data access error.
  */
  private void showActiveTransport() throws DBException
  {
    switch(Application.getInt(HostConfigData.ACTIVE_TRANSPORT_TYPE))
    {
      case HostConfig.JDBCORACLE:
        mpActiveConfigMap.put(COMM_TYPE_KEY, mpJdbcOracle);
        msSelectedCommType = HostConfigData.JDBC_ORACLE_TRANSPORT;
        mpJdbcOracle.setSelected(true);
        break;
             
      case HostConfig.JDBCSQLSERVER:
        mpActiveConfigMap.put(COMM_TYPE_KEY, mpJdbcSqlServer);
        msSelectedCommType = HostConfigData.JDBC_SQLSERVER_TRANSPORT;
        mpJdbcSqlServer.setSelected(true);
        break;
        
      case HostConfig.JDBCDB2:
        mpActiveConfigMap.put(COMM_TYPE_KEY, mpJdbcDB2);
        msSelectedCommType = HostConfigData.JDBC_DB2_TRANSPORT;
        mpJdbcDB2.setSelected(true);
        break;

      case HostConfig.TCPIP:
        mpActiveConfigMap.put(COMM_TYPE_KEY, mpTCPIP);
        msSelectedCommType = HostConfigData.TCPIP_TRANSPORT;
        mpTCPIP.setSelected(true);
    }
  }

 /**
  *  Method sets the configuration to the default settings <i>provided no changes
  *  have been made yet.</i>
  */
  private void setToDefaultConfig()
  {
    try
    {
      showActiveDataType();
      showActiveTransport();
      mpAccpChanges.setEnabled(false);
    }
    catch(DBException ex)
    {
      displayError("Error getting active format..." + ex.getMessage());
    }
  }
  
  private void updateLocalActiveMap(String isMapKeyName)
  {
    if (isMapKeyName.equals(DATA_TYPE_KEY))
    {
      for(Enumeration<AbstractButton> vpDataTypeIter = mpDataTypeBGroup.getElements();
          vpDataTypeIter.hasMoreElements(); )
      {
        JRadioButton vpRadioButton = (JRadioButton)vpDataTypeIter.nextElement();
        if (vpRadioButton.getName().equals(msSelectedDataType))
        {
          mpActiveConfigMap.put(isMapKeyName, vpRadioButton);
          break;
        }
      }
    }
    else
    {
      for(Enumeration<AbstractButton> vpCommTypeIter = mpCommTypeBGroup.getElements();
          vpCommTypeIter.hasMoreElements(); )
      {
        JRadioButton vpRadioButton = (JRadioButton)vpCommTypeIter.nextElement();
        if (vpRadioButton.getName().equals(msSelectedCommType))
        {
          mpActiveConfigMap.put(isMapKeyName, vpRadioButton);
          break;
        }
      }
    }
  }
}
