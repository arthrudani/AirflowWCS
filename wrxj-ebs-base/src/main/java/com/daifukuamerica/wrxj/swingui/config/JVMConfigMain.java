package com.daifukuamerica.wrxj.swingui.config;

import com.daifukuamerica.wrxj.clc.database.DatabaseControllerTypeDefinition;
import com.daifukuamerica.wrxj.controller.NamedThread;
import com.daifukuamerica.wrxj.dataserver.standard.StandardConfigurationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.dbadapter.data.JVMConfigData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;

/**
 * Main screen for configuring split systems.
 *
 * @author A.D.
 * @since  16-Apr-2009
 */
@SuppressWarnings("serial")
public class JVMConfigMain extends DacInputFrame
{
  private Pattern mpRegex = Pattern.compile("(\\p{Alpha}+)(\\p{ASCII}*)");
  private List<Map>                   mpDataList;
  private String[]                    mpDeviceArray;

  private SKDCButton                  mpBtnCalcConfiguration;
  private SKDCButton                  mpBtnApplyConfiguration;
  private SKDCButton                  mpBtnRemoveConfiguration;
  private SKDCButton                  mpBtnReset;
  private SKDCComboBox                mpAreaCombo;
  private SKDCIntegerField            mpIntegerTotalAisles;
  private SKDCIntegerField            mpIntegerTotalJVMs;
  private SKDCIntegerField            mpIntegerAislesPerJVM;
  private SKDCIntegerField            mpIntegerStartJVM;
  private SKDCTextField               mpTxtPrimaryJVM;
  private SKDCTextField               mpTxtSchedNamePrefix;
  private SKDCTextField               mpTxtAllocNamePrefix;
  private SKDCTextField               mpTxtServer;

  private int                         mnJVMStartValue;
  private String                      msPrimaryJVMID;
  private String                      msSchedulerPrefix;
  private String                      msAllocatorPrefix;
  private StandardConfigurationServer mpConfigServ;
  private StandardLocationServer      mpLocnServ;

  public JVMConfigMain()
  {
    super("JVM Config.", "");
    mpConfigServ = Factory.create(StandardConfigurationServer.class);
    mpLocnServ = Factory.create(StandardLocationServer.class);

    setResizable(true);
    validateConfiguration();
    initSwingComponents();
  }

  @Override
  public Dimension getPreferredSize()
  {
    return(new Dimension(930, 500));
  }

  @Override
  public void internalFrameOpened(InternalFrameEvent e)
  {
    super.internalFrameOpened(e);
    setupAreaCombo();
  }

  public void initSwingComponents()
  {
    createCustomButtons();
    
    mpAreaCombo = new SKDCComboBox();
    mpTxtPrimaryJVM = new SKDCTextField(JVMConfigData.JVMIDENTIFIER_NAME);
    mpIntegerStartJVM = new SKDCIntegerField(1, 2);
    mpIntegerTotalAisles = new SKDCIntegerField(0, 3);
    mpIntegerTotalAisles.setEnabled(false);
    mpIntegerTotalJVMs = new SKDCIntegerField(0, 3);
    mpIntegerTotalJVMs.setEnabled(false);
    mpIntegerAislesPerJVM = new SKDCIntegerField(0, 3);
    mpTxtSchedNamePrefix = new SKDCTextField(DeviceData.SCHEDULERNAME_NAME);
    mpTxtAllocNamePrefix = new SKDCTextField(DeviceData.ALLOCATORNAME_NAME);
    mpTxtServer = new SKDCTextField(JVMConfigData.SERVERNAME_NAME);

    addInput("Area", mpAreaCombo);
    addInput("Total Aisles", mpIntegerTotalAisles);
    addInput("Total JVMs", mpIntegerTotalJVMs);
    addInput("Aisles Assigned Per JVM", mpIntegerAislesPerJVM);
    addInput("JVM Start Index", mpIntegerStartJVM);
    addInput("Primary JVM", mpTxtPrimaryJVM);
    addInput("Scheduler Name Prefix", mpTxtSchedNamePrefix);
    addInput("Allocation Name Prefix", mpTxtAllocNamePrefix);
    addInput("Server", mpTxtServer);

    setInputColumns(2);
    showTable("JVMConfigMain");        // Build table and display data.
  }

 /**
  * Method to populate swing component using a worker thread and event thread.
  */
  protected void populateSwingComponents()
  {
    Thread vpThread = new NamedThread("JVMConfigMain.populateSwingComponents")
    {
      @Override
      public void run()
      {
        StandardConfigurationServer vpConfigServ = Factory.create(StandardConfigurationServer.class);
        StandardDeviceServer vpDeviceServ = Factory.create(StandardDeviceServer.class);
        try
        {
          String vsArea = mpAreaCombo.getText();
          final int vnTotalJVMs = vpConfigServ.getJVMCountPerWarehouse(vsArea);
          final int vnAislesPerJVM = vpDeviceServ.getPrimaryJVMAisleCount();
          mpDeviceArray = vpDeviceServ.getDeviceNamesPerSuperWarehouse(vsArea);

          mpDataList = vpConfigServ.getCurrentSplitSystemConfig(vsArea);

/*============================================================================
    Get the scheduler and allocator prefixes.  We are only interested in the
    prefix and so don't care which device record we pull this from.
  ============================================================================*/
          if (!mpDataList.isEmpty())
          {
            String vsSchedName = DBHelper.getStringField(mpDataList.get(0),
                                                 DeviceData.SCHEDULERNAME_NAME);
            msSchedulerPrefix = getSchedOrAllocPrefix(vsSchedName);
            if (msSchedulerPrefix.isEmpty())
              msSchedulerPrefix = DatabaseControllerTypeDefinition.SCHEDULER_TYPE;

            String vsAllocName = DBHelper.getStringField(mpDataList.get(0),
                                                 DeviceData.ALLOCATORNAME_NAME);
            msAllocatorPrefix = getSchedOrAllocPrefix(vsAllocName);
            if (msAllocatorPrefix.isEmpty())
              msAllocatorPrefix = DatabaseControllerTypeDefinition.ALLOCATOR_TYPE;

/*============================================================================
   It doesn't make sense to show the server field since we don't know which one
   to display (the JVM's could be distributed on multiple servers).
  ============================================================================*/
            removeInput(mpTxtServer);
          }
          else                         // The split system does not exist.
          {
            String vsSchedName = vpDeviceServ.getSchedulerName(mpDeviceArray[0]);
            msSchedulerPrefix = getSchedOrAllocPrefix(vsSchedName);
            if (msSchedulerPrefix.isEmpty())
              msSchedulerPrefix = DatabaseControllerTypeDefinition.SCHEDULER_TYPE;

            String vsAllocName = vpDeviceServ.getAllocatorName(mpDeviceArray[0]);
            msAllocatorPrefix = getSchedOrAllocPrefix(vsAllocName);
            if (msAllocatorPrefix.isEmpty())
              msAllocatorPrefix = DatabaseControllerTypeDefinition.ALLOCATOR_TYPE;
          }

          SwingUtilities.invokeLater(new Runnable()
          {
            @Override
            public void run()
            {
              mpIntegerTotalAisles.setValue(mpDeviceArray.length);
              mpIntegerTotalJVMs.setValue(vnTotalJVMs);
              mpIntegerAislesPerJVM.setValue((vnTotalJVMs == 0) ? mpDeviceArray.length
                                                                : vnAislesPerJVM);
              mpIntegerStartJVM.setValue(mnJVMStartValue);
              mpTxtPrimaryJVM.setText(msPrimaryJVMID);
              mpTxtSchedNamePrefix.setText(msSchedulerPrefix);
              mpTxtAllocNamePrefix.setText(msAllocatorPrefix);
              if (mpTxtServer.isVisible())
                mpTxtServer.setText(SKDCUserData.getMachineName());
              refreshTable(mpDataList);
            }
          });
        }
        catch(DBException exc)
        {
          displayError("DB error finding super warehouses. " + exc.getMessage());
        }
      }
    };

    vpThread.setPriority(Thread.NORM_PRIORITY);
    vpThread.start();
  }

  /**
  * Method to populate the Super-warehouse combo box.
  */
  protected void setupAreaCombo()
  {
    try
    {
      String[] vasArea = mpLocnServ.getSuperWarehouseChoices(false);
      if (vasArea.length == 0)
      {
        displayError("Configuration Error!  Super warehouses not defined " +
                     "for grouping JVMs");
      }
      else
      {
        mpAreaCombo.addItemListener(new ItemListener()
        {
          @Override
          public void itemStateChanged(ItemEvent e)
          {
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
              populateSwingComponents();
            }
          }
        });
        mpAreaCombo.setComboBoxData(vasArea);
      }
    }
    catch(DBException exc)
    {
      displayError("DB error finding super warehouses. " + exc.getMessage());
    }
  }

  public void createCustomButtons()
  {
    mpBtnClear.setVisible(false);
    
    mpBtnApplyConfiguration = new SKDCButton("Apply Config.",
"Permanently apply shown configuration to the system (restart required for all Wrx-J instances).", 'A');
    mpBtnApplyConfiguration.setEnabled(false);
    mpBtnRemoveConfiguration = new SKDCButton("Remove Config.",
                "Permanently remove any configuration for a split system.", 'R');
    mpBtnCalcConfiguration = new SKDCButton("Calculate Config.",
       "JVM configuration as it would appear with configured parameters.", 'l');
    mpBtnReset = new SKDCButton("Reset", "Show original settings.", 'e');

    mpButtonPanel.removeAll();
    mpButtonPanel.add(mpBtnCalcConfiguration);
    mpButtonPanel.add(mpBtnApplyConfiguration);
    mpButtonPanel.add(mpBtnRemoveConfiguration);
    mpButtonPanel.add(mpBtnReset);
    mpButtonPanel.add(mpBtnClear);
    mpButtonPanel.add(mpBtnClose);

    CustomButtonListener vpListener = new CustomButtonListener();
    mpBtnApplyConfiguration.addActionListener(vpListener);
    mpBtnRemoveConfiguration.addActionListener(vpListener);
    mpBtnCalcConfiguration.addActionListener(vpListener);
    mpBtnReset.addActionListener(vpListener);
  }

  protected void resetOriginalConfigPressed()
  {
    populateSwingComponents();
  }
  
 /**
  * Method for the Apply button.  This method is for applying the chosen
  * settings permanently.
  */
  protected void applyButtonPressed()
  {
    try
    {
      mpConfigServ.createSplitSystemConfig(mpAreaCombo.getText(),
                                           mpTable.getTableData());
      mpBtnReset.setEnabled(false);
      mpBtnApplyConfiguration.setEnabled(false);
    }
    catch(DBException ex)
    {
      displayError(ex.getMessage());
    }
  }

 /**
  * Method for removing button.  This method is for removing split system
  * configuration from the whole system.
  */
  protected void removeButtonPressed()
  {
    try
    {
      mpConfigServ.removeSplitSystemConfig(SKDCConstants.ALL_STRING);
      populateSwingComponents();
    }
    catch(DBException e)
    {
      displayWarning(e.getMessage());
    }
  }

 /**
  * Method to show what the configuration will look like based on the 
  * "Aisles Assigned Per JVM" and "Schedulers Assigned Per JVM" parameter.
  */
  protected void calculateButtonPressed()
  {
    int[] vanAisleDistribution = calculateDistribution(mpIntegerTotalAisles.getValue(),
                                                       mpIntegerAislesPerJVM.getValue());
    mpIntegerTotalJVMs.setValue(vanAisleDistribution.length);


    List<Map> vpDispList = new ArrayList<Map>(mpTable.getRowCount());
    int vnCurrentJVMNumber = mpIntegerStartJVM.getValue();
    int vnCurrentDispRow = 0;

    for (int vnJVMCnt = 0; vnJVMCnt < vanAisleDistribution.length; vnJVMCnt++)
    {
      String vsJVMId = "JVM" + SKDCUtility.preZeroFill(vnCurrentJVMNumber, 2);
      String vsJMSTopic = "JMSTopic" + vnCurrentJVMNumber;
      for(int vnIdx = 0; vnIdx < vanAisleDistribution[vnJVMCnt]; vnIdx++)
      {
        Map<String, Object> vpNewMap = new TreeMap<String, Object>();
        vpNewMap.put(JVMConfigData.JVMIDENTIFIER_NAME, vsJVMId);
        vpNewMap.put(JVMConfigData.JMSTOPIC_NAME, vsJMSTopic);
        vpNewMap.put(DeviceData.DEVICEID_NAME, mpDeviceArray[vnCurrentDispRow]);
        vpNewMap.put(DeviceData.SCHEDULERNAME_NAME, mpTxtSchedNamePrefix.getText() + "-" + vnCurrentJVMNumber);
        vpNewMap.put(DeviceData.ALLOCATORNAME_NAME, mpTxtAllocNamePrefix.getText() + "-" + vnCurrentJVMNumber);
        vpNewMap.put(JVMConfigData.SERVERNAME_NAME, mpTxtServer.getText());
        vpDispList.add(vpNewMap);
        vnCurrentDispRow++;
      }
      vnCurrentJVMNumber++;
    }
    mpTable.refreshData(vpDispList);
    mpBtnApplyConfiguration.setEnabled(true);
  }

 /**
  * Method to determine how a total number of elements can be distributed into
  * a series of sub-element groups.  The user specifies the total number of
  * elements to split, and the <u>desired</u> number of sub-elements in the split.
  * This method then returns an array which shows how the split should actually
  * occur.
  * @param inTotalElements  the total number of elements to split.
  * @param inSubElementsPerGroup the number of subelements per group that is desired.
  * @return integer array containing distribution of elements.  The length of
  *         the array specifies the number of groups.
  */
  private int[] calculateDistribution(int inTotalElements, int inSubElementsPerGroup)
  {
    int[] vanSubElementDist;
    double vdTotalElements = inTotalElements;
    double vdSubElementsPerGroup = inSubElementsPerGroup;

    double vdRatio = vdTotalElements/vdSubElementsPerGroup;
    int vnRemElements = inTotalElements % inSubElementsPerGroup;

    if (vnRemElements >= 5)
    {
      int vnTotalGroups = (int)Math.ceil(vdRatio);
      vanSubElementDist= new int[vnTotalGroups];
      Arrays.fill(vanSubElementDist, 0, vnTotalGroups-1, inSubElementsPerGroup);
      vanSubElementDist[vnTotalGroups-1] = vnRemElements;
    }
    else
    {
      int vnTotalGroups = (int)Math.floor(vdRatio);
      vanSubElementDist= new int[vnTotalGroups];
      Arrays.fill(vanSubElementDist, 0, vnTotalGroups-1, inSubElementsPerGroup);
      vanSubElementDist[vnTotalGroups-1] = mpIntegerAislesPerJVM.getValue() + vnRemElements;
    }

    return(vanSubElementDist);
  }

  /**
   * Method for the Clear button.
   */
  @Override
  protected void clearButtonPressed()
  {
    mpTable.clearTable();
    mpAreaCombo.setSelectedIndex(0);
    mpIntegerTotalAisles.setValue(0);
    mpIntegerTotalJVMs.setValue(0);
    mpIntegerAislesPerJVM.setValue(0);
    mpTxtSchedNamePrefix.setText("");
    mpTxtAllocNamePrefix.setText("");
    mpTxtServer.setText("");
    msPrimaryJVMID = "";
    mpConfigServ.cleanUp();
    mpLocnServ.cleanUp();
    mpBtnApplyConfiguration.setEnabled(false);
  }

  private String getSchedOrAllocPrefix(String isPatternMatchStr)
  {
    String vsPatternMatchStr = "";
    Matcher m = mpRegex.matcher(isPatternMatchStr);
    if (m.matches())
      vsPatternMatchStr = m.group(1);
    
    return(vsPatternMatchStr);
  }

  private void validateConfiguration()
  {
    try
    {
      msPrimaryJVMID = mpConfigServ.getPrimaryJVMIdentifier();
      if (msPrimaryJVMID.isEmpty())
      {
        setError("No Primary JVM configured!");
      }
      else
      {
        Pattern vpJVMIDPattern = Pattern.compile("(\\p{Alpha}+)(\\p{Digit}{1,2})");
        Matcher vpMatcher = vpJVMIDPattern.matcher(msPrimaryJVMID);
        if (vpMatcher.matches())
        {
          mnJVMStartValue = Integer.parseInt(vpMatcher.group(2));
        }
      }
    }
    catch(DBException exc)
    {
      setError(exc.getMessage());
    }
  }

  private class CustomButtonListener implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      Object vpPressedButton = e.getSource();
      if (vpPressedButton == mpBtnRemoveConfiguration)
      {
        removeButtonPressed();
      }
      else if (vpPressedButton == mpBtnCalcConfiguration)
      {
        calculateButtonPressed();
      }
      else if (vpPressedButton == mpBtnApplyConfiguration)
      {
        applyButtonPressed();
      }
      else if (vpPressedButton == mpBtnReset)
      {
        resetOriginalConfigPressed();
      }
    }
  }
}
