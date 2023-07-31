package com.daifukuamerica.wrxj.swingui.location;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentGraphic;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

/**
 * <B>Description:</B> Show location utilization in a pie chart
 *
 * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
@SuppressWarnings("serial")
public class LocationUtilizationFrame extends SKDCInternalFrame
{
  protected Color mpColorProhibited = Color.YELLOW;
  protected Color mpColorUnavailable = Color.RED;
  protected Color mpColorAvailableEmpty = Color.GREEN;
  protected Color mpColorAvailableReserved = new Color(0, 255, 255);
  protected Color mpColorAvailableDDMove = new Color(0, 225, 225);
  protected Color mpColorAvailableSwap = new Color(200, 200, 200);
  protected Color[] mapColorAvailableOccupied = new Color[] {
      new Color(0, 200, 0), new Color(0, 0, 250), new Color(0, 0, 200),
      new Color(0, 0, 150), new Color(0, 0, 100) };
  
  protected StandardDeviceServer mpDeviceServer = Factory.create(StandardDeviceServer.class);
  protected StandardLocationServer mpLocationServer = Factory.create(StandardLocationServer.class);
  protected Location mpLocation = Factory.create(Location.class);
  
  protected SKDCComboBox mpDeviceCombo, mpHeightCombo;
  protected SKDCButton mpSearchButton, mpCloseButton;
  protected ChartPanel mpChartPanel;
  
  protected Integer[] manHeights;
  
  /**
   * Constructor
   */
  public LocationUtilizationFrame()
  {
    super();
    setTitle("Location Utilization");
    setResizable(false);
    setAllowDuplicateScreens(true);
    buildScreen();
  }
  
  /**
   * Constructor
   * @param isInitialDevice
   */
  public LocationUtilizationFrame(String isInitialDevice)
  {
    this();
    mpDeviceCombo.setSelectedItem(isInitialDevice);
    searchButtonPressed();
  }

  /**
   * Build the screen
   */
  protected void buildScreen()
  {
    /*
     * Initialize screen components
     */
    mpDeviceCombo = new SKDCComboBox();
    fillDevicesCombo();
    
    mpHeightCombo = new SKDCComboBox();
    try
    {
      manHeights = mpLocationServer.getLocationHeights();
      if (manHeights.length > 1)
      {
        mpHeightCombo.setComboBoxData(manHeights, SKDCConstants.ALL_STRING);
        mpDeviceCombo.addItemListener(new ItemListener() {
          @Override
          public void itemStateChanged(ItemEvent e)
          {
            fillHeightsByDevice();
          }
        });
      }
      else
      {
        mpHeightCombo.setComboBoxData(manHeights);
        mpHeightCombo.setEnabled(false);
      }
    }
    catch (DBException dbe)
    {
      logAndDisplayException(dbe);
    }
    
    mpSearchButton = new SKDCButton("Search", "Search", 'S');
    mpSearchButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        searchButtonPressed();
      }});

    JFreeChart vpChart = ChartFactory.createPieChart("", null, true, true,
        false);
    vpChart.getPlot().setBackgroundPaint(EquipmentGraphic.DAIFUKU_LIGHT_PURPLE);
    vpChart.setBackgroundPaint(new JPanel().getBackground());
    mpChartPanel = new ChartPanel(vpChart);
    
    mpCloseButton = new SKDCButton("Close", "Close", 'C');
    mpCloseButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        closeButtonPressed();
      }});

    /*
     * Build the screen
     */
    JPanel vpSearchPanel = getEmptyListSearchPanel();
    vpSearchPanel.add(new SKDCLabel("Device:"));
    vpSearchPanel.add(mpDeviceCombo);
    if (mpHeightCombo.isEnabled())
    {
      vpSearchPanel.add(new SKDCLabel(" Height:"));
      vpSearchPanel.add(mpHeightCombo);
    }
    vpSearchPanel.add(mpSearchButton);

    JPanel vpChartPanel = getEmptyButtonPanel();
    vpChartPanel.add(mpChartPanel);
    
    JPanel vpButtonPanel = getEmptyButtonPanel();
    vpButtonPanel.add(mpCloseButton);
    
    getContentPane().add(vpSearchPanel, BorderLayout.NORTH);
    getContentPane().add(vpChartPanel, BorderLayout.CENTER);
    getContentPane().add(vpButtonPanel, BorderLayout.SOUTH);
  }

  /**
   * Fill the devices combo
   */
  private void fillDevicesCombo()
  {
    try
    {
      String[] masDevices = mpLocation.getARSRLocationDevices();
      mpDeviceCombo.setComboBoxData(masDevices, SKDCConstants.ALL_STRING);
    }
    catch (DBException dbe)
    {
      logAndDisplayException(dbe);
    }
  }
  
  /**
   * Fill the height combo with only heights for the selected device
   */
  protected void fillHeightsByDevice()
  {
    String vsDevice = mpDeviceCombo.getSelectedItem().toString();
    if (vsDevice.equals(SKDCConstants.ALL_STRING))
    {
      mpHeightCombo.setComboBoxData(manHeights, SKDCConstants.ALL_STRING);
    }
    else
    {
      try
      {
        mpHeightCombo.setComboBoxData(
            mpLocation.getARSRLocationHeightsForDevice(vsDevice),
            SKDCConstants.ALL_STRING);
      }
      catch (Exception e)
      {
        logAndDisplayException(e);
      }
    }
  }
  
  /**
   * The search button was pressed
   */
  private void searchButtonPressed()
  {
    try
    {
      JFreeChart vpChart;

      String vsDevice = mpDeviceCombo.getSelectedItem().toString();
      if (vsDevice.equals(SKDCConstants.ALL_STRING))
      {
        vsDevice = "";
      }

      String vsHeight = mpHeightCombo.getSelectedItem().toString();
      if (vsHeight.equals(SKDCConstants.ALL_STRING))
      {
        vpChart = getLocationUtilization(vsDevice, -1);
      }
      else
      {
        vpChart = getLocationUtilization(vsDevice, Integer.parseInt(vsHeight));
      }
      vpChart.setBackgroundPaint(new JPanel().getBackground());
      vpChart.getPlot().setNoDataMessage("No locations for this device and/or height.");
      
      mpChartPanel.setChart(vpChart);
    }
    catch (Exception e)
    {
      logAndDisplayException(e);
    }
  }

  /**
   * Get the location utilization for one height (pie chart)
   * 
   * @param isDevice
   * @param inHeight
   * @return
   * @throws Exception
   */
  protected JFreeChart getLocationUtilization(String isDevice, int inHeight)
      throws Exception
  {
    // Chart title
    String vsChartTitle = "";
    DeviceData vpDevData = mpDeviceServer.getDeviceData(isDevice);
    if (vpDevData != null)
    {
      String vsPS = DBTrans.getStringValue(DeviceData.PHYSICALSTATUS_NAME,
          vpDevData.getPhysicalStatus());
      String vsOS = DBTrans.getStringValue(DeviceData.OPERATIONALSTATUS_NAME,
          vpDevData.getOperationalStatus());
      vsChartTitle = "Device: " + isDevice + " (" + vsOS + "/" + vsPS + ")"; 
    }
    else
    {
      vsChartTitle = "Device: " + mpDeviceCombo.getSelectedItem();
    }
    vsChartTitle += " - Height: " + mpHeightCombo.getSelectedItem();
      
    DefaultPieDataset vpPieData = new DefaultPieDataset();
    JFreeChart vpChart = ChartFactory.createPieChart(vsChartTitle, vpPieData,
        true, true, false);
    PiePlot vpPlot = (PiePlot)vpChart.getPlot();
    vpPlot.setBackgroundPaint(EquipmentGraphic.DAIFUKU_LIGHT_PURPLE);

    // Get the data
    List<Map> vpResults = mpLocation.getLocationUtilization(isDevice, inHeight);
    for (Map m : vpResults)
    {
      int vnStatus = (Integer)(m.get(LocationData.LOCATIONSTATUS_NAME));
      int vnEmptyFlag = (Integer)(m.get(LocationData.EMPTYFLAG_NAME));
      int vnFullness = (Integer)(m.get(LoadData.AMOUNTFULL_NAME));
      int vnCount = (Integer)(m.get("ICOUNT"));
      
      String vsTitle = "";
      switch (vnStatus)
      {
        case DBConstants.LCPROHIBIT:
          vsTitle = DBTrans.getStringValue(LocationData.LOCATIONSTATUS_NAME,
              DBConstants.LCPROHIBIT);
          vpPlot.setSectionPaint(vsTitle, mpColorProhibited);
          break;
          
        case DBConstants.LCUNAVAIL:
          vsTitle = DBTrans.getStringValue(LocationData.LOCATIONSTATUS_NAME,
              DBConstants.LCUNAVAIL);
          vpPlot.setSectionPaint(vsTitle, mpColorUnavailable);
          break;
          
        case DBConstants.LCAVAIL:
          vsTitle = DBTrans.getStringValue(LocationData.LOCATIONSTATUS_NAME,
              DBConstants.LCAVAIL) + "-" +
              DBTrans.getStringValue(LocationData.EMPTYFLAG_NAME, vnEmptyFlag);
          switch (vnEmptyFlag)
          {
            case DBConstants.LCRESERVED:
              vpPlot.setSectionPaint(vsTitle, mpColorAvailableReserved);
              break;
              
            case DBConstants.LC_DDMOVE:
              vpPlot.setSectionPaint(vsTitle, mpColorAvailableDDMove);
              break;
              
            case DBConstants.LC_SWAP:
              vpPlot.setSectionPaint(vsTitle, mpColorAvailableSwap);
              break;
              
            case DBConstants.UNOCCUPIED:
              vpPlot.setSectionPaint(vsTitle, mpColorAvailableEmpty);
              break;
              
            case DBConstants.OCCUPIED:
              try
              {
                vsTitle += "-"
                    + DBTrans.getStringValue(LoadData.AMOUNTFULL_NAME,
                        vnFullness);
                int vnIndex = vnFullness - DBConstants.EMPTY;
                vpPlot.setSectionPaint(vsTitle, mapColorAvailableOccupied[vnIndex]);
              }
              catch (NoSuchFieldException nsfe)
              {
                // Occupied location without a load
                vsTitle += "-Error";
                vpPlot.setSectionPaint(vsTitle, mpColorUnavailable);
              }
              break;
          }
          break;
          
        default:
          vsTitle = "Unknown";
          vpPlot.setSectionPaint(vsTitle, Color.WHITE);
          break;
      }
      vpPieData.setValue(vsTitle, vnCount);
    }

    // Show percentages on labels
    NumberFormat vpPercentFormat = NumberFormat.getPercentInstance();
    vpPercentFormat.setMaximumFractionDigits(1);
    vpPercentFormat.setMinimumFractionDigits(1);
    vpPlot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} = {2}",
        NumberFormat.getNumberInstance(), vpPercentFormat));
    
    return vpChart;
  }
}
