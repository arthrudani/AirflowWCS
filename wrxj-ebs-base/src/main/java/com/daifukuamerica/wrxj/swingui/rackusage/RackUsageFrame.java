package com.daifukuamerica.wrxj.swingui.rackusage;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.RackLocationParser;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swingui.location.LocationMain;

/**
 * <B>Description:</B> Frame to show location usage to visually inspect rack 
 * usage.
 * 
 * <P>This screen has not been extensively tested.  <B>THIS IS FOR DEVELOPER USE 
 * ONLY.</B></P>
 * 
 * <P>This screen was originally created for Meyer to facilitate double-deep
 * testing, then restructured for baseline.  This screen no longer supports
 * double-deep (use the double deep one instead).</P>
 * 
 * <P>This class currently makes all sorts of assumptions about the rack, the 
 * most important being:
 * <UL>
 * <LI>the banks are contiguously numbered.</LI>
 * <LI>the banks for a given device all have the same number of bays and tiers.</LI>
 * <LI>the bays start numbering at 1</LI>
 * <LI>the tiers start numbering at 1</LI>
 * </UL>
 * </P>
 *
 * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
public class RackUsageFrame extends SKDCInternalFrame
{
  public static final String ASCENDING = "Ascending";
  public static final String DESCENDING = "Descending";

  // Search Panel
  protected SKDCComboBox mpDeviceCombo, mpOrderCombo;
  protected RackInfoCombo mpDisplaySelector;
  protected SKDCButton mpSearchButton;
  protected SKDCButton mpRefreshButton;

  // Rack Usage Panel
  protected JPanel mpRackUsagePanel;
  protected RackUsagePropertyChangeListener mpRUPCL;
  protected final int START_X = 20;
  protected boolean mzSettingDevice = false;
  
  protected SKDCLabel mpTitle;
  
  // Top-Down view
  protected RackUsageBankTier[] mapBanks;
  protected JSlider mpTierSlider;

  // Legends
  protected RackUsageLegend mpUsageLegend;
  protected RackSpeedLegend mpSpeedLegend;
  
  // Side view
  public RackUsageBank mpSide;
  protected JSlider mpBankSlider;

  // Bottom Button Panel
  protected SKDCButton mpCloseButton;
  
  /**
   * Constructor
   */
  public RackUsageFrame()
  {
    buildScreen();
  }

  /**
   * Initialize the components that will be on the screen
   */
  protected void initializeScreenComponents()
  {
    Location vpLoc = Factory.create(Location.class);
    
    mpDeviceCombo = new SKDCComboBox();
    try
    {
      mpDeviceCombo.setComboBoxData(vpLoc.getRackUsageDeviceList());
    }
    catch (DBException dbe)
    {
      logAndDisplayException("Error getting combo box data", dbe);
    }
    mpOrderCombo = new SKDCComboBox(new String[] { ASCENDING, DESCENDING });
    mpDisplaySelector = new RackInfoCombo();
    
    mpRUPCL = new RackUsagePropertyChangeListener();
    mpRackUsagePanel = buildRackUsagePanel();
    
    mpSearchButton = new SKDCButton("Search", "Search", 'S');
    mpSearchButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        searchButtonPressed();
      }});
    
    mpRefreshButton = new SKDCButton("Refresh", "Refresh", 'R');
    mpRefreshButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        refreshButtonPressed();
      }});
    
    mpCloseButton = new SKDCButton("Close", "Close", 'C');
    mpCloseButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        closeButtonPressed();
      }});
  }
  
  /**
   * Build the screen
   */
  protected void buildScreen()
  {
    initializeScreenComponents();
    
    JPanel vpSearchPanel = getEmptyListSearchPanel();
    vpSearchPanel.add(new SKDCLabel("Device:"));
    vpSearchPanel.add(mpDeviceCombo);
    vpSearchPanel.add(Box.createHorizontalStrut(5));
    vpSearchPanel.add(new SKDCLabel("Order:"));
    vpSearchPanel.add(mpOrderCombo);
    vpSearchPanel.add(Box.createHorizontalStrut(5));
    vpSearchPanel.add(new SKDCLabel("Display:"));
    vpSearchPanel.add(mpDisplaySelector);
    vpSearchPanel.add(Box.createHorizontalStrut(5));
    vpSearchPanel.add(mpSearchButton);
    vpSearchPanel.add(mpRefreshButton);
    
    JPanel vpSouthPanel = new JPanel(new BorderLayout());
    vpSouthPanel.add(getInfoPanel(), BorderLayout.CENTER);
    JPanel vpButtonPanel = getEmptyButtonPanel();
    vpButtonPanel.add(mpCloseButton);
    vpSouthPanel.add(vpButtonPanel, BorderLayout.SOUTH);
    
    getContentPane().add(vpSearchPanel, BorderLayout.NORTH);
    getContentPane().add(mpRackUsagePanel, BorderLayout.CENTER);
    getContentPane().add(vpSouthPanel, BorderLayout.SOUTH);
    
    setResizable(false);
  }
  
  /**
   * Enable or disable the whole frame
   * 
   * @param izEnabled
   */
  public void setAllEnabled(boolean izEnabled)
  {
    mpDeviceCombo.setEnabled(izEnabled);
    mpOrderCombo.setEnabled(izEnabled);
    mpDisplaySelector.setEnabled(izEnabled);
    mpSearchButton.setEnabled(izEnabled);
    mpRefreshButton.setEnabled(izEnabled);
    
    mpTierSlider.setEnabled(izEnabled);
    mpBankSlider.setEnabled(izEnabled);
    
    mpCloseButton.setEnabled(izEnabled);
  }
  
  /**
   * The Search button was pressed
   */
  protected void searchButtonPressed()
  {
    final String vsSelection = mpDeviceCombo.getSelectedItem().toString();
    setAllEnabled(false);
    mpSpeedLegend.setVisible(mpDisplaySelector.isSpeedSelected());
    mpUsageLegend.setVisible(mpDisplaySelector.isStatusSelected());
    displayInfoAutoTimeOut("Gathering data... please wait.");
    SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run()
        {
          setDevice(vsSelection.substring(0,4), vsSelection);
          displayInfoAutoTimeOut("");
          setAllEnabled(true);
        }
      });
  }

  /**
   * The Search button was pressed
   */
  protected void refreshButtonPressed()
  {
    setAllEnabled(false);
    mpSpeedLegend.setVisible(mpDisplaySelector.isSpeedSelected());
    mpUsageLegend.setVisible(mpDisplaySelector.isStatusSelected());
    displayInfoAutoTimeOut("Gathering data... please wait.");
    SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run()
        {
          refreshRackUsagePanel();
          displayInfoAutoTimeOut("");
          setAllEnabled(true);
        }
      });
  }

  /**
   * Initialize the top view's bank/tier views
   * 
   * @param inX
   * @param inTopY
   * @param inBottomY
   * @param inWidth
   * @param inHeight
   * @param ipRUPCL
   */
  protected void initializeTopViewBankTiers(int inX, int inTopY, int inBottomY,
      int inWidth, int inHeight, PropertyChangeListener ipRUPCL)
  {
    mapBanks = new RackUsageBankTier[2];

    // Top
    mapBanks[0] = Factory.create( RackUsageBankTier.class, inX, inTopY, inWidth, inHeight, 
        true, false, ipRUPCL, mpDisplaySelector);

    // Bottom
    mapBanks[1] =  Factory.create( RackUsageBankTier.class, inX, inBottomY, inWidth, inHeight,
        true, false, ipRUPCL, mpDisplaySelector);
  }
  
  /**
   * Build the rack usage panel
   * 
   * @return
   */
  protected JPanel buildRackUsagePanel()
  {
    JPanel vpRUPanel = new JPanel();
    int vnPanelWidth = 1000;
    int vnCraneWidth = vnPanelWidth - 90;
    int vnTopBankHeight = 41;
    int vnSideBankHeight = 210;
    
    vpRUPanel.setBorder(new TitledBorder(new EtchedBorder(), "Rack Usage"));
    vpRUPanel.setLayout(null);
    vpRUPanel.setPreferredSize(new Dimension(vnPanelWidth, 465));
    
    mpTitle = new SKDCLabel("");
    mpTitle.setBounds(vnPanelWidth/2-85, 20, 160, 20);
    vpRUPanel.add(mpTitle);
    
    /*
     * Top-down view
     */
    RackUsageCrane vpCrane = new RackUsageCrane(START_X, 95, vnCraneWidth, 32);
    vpRUPanel.add(vpCrane);
    
    initializeTopViewBankTiers(START_X, 50, 130, vnCraneWidth, vnTopBankHeight,
        mpRUPCL);
    for (RackUsageBankTier vpRUBT : mapBanks)
    {
      vpRUPanel.add(vpRUBT);
    }
    
    SKDCLabel vpTierLabel = new SKDCLabel("Tier");
    vpTierLabel.setBounds(vnPanelWidth-58, 15, 25, 20);
    vpRUPanel.add(vpTierLabel);
    
    mpTierSlider = new JSlider(JSlider.VERTICAL);
    mpTierSlider.setBounds(vnPanelWidth-60, 35, 50, 150);
    mpTierSlider.setPaintTicks(true);
    mpTierSlider.setMajorTickSpacing(1);
    mpTierSlider.setPaintLabels(true);
    mpTierSlider.setMaximum(2);
    mpTierSlider.setMinimum(1);
    mpTierSlider.setValue(1);
    mpTierSlider.setSnapToTicks(true);
    mpTierSlider.setEnabled(false);
    mpTierSlider.addChangeListener(new ChangeListener() {
      int mnCurrentValue = 1;
      @Override
      public void stateChanged(ChangeEvent e)
      {
        if (!mpTierSlider.getValueIsAdjusting())
        {
          if (mnCurrentValue != mpTierSlider.getValue())
          {
            mnCurrentValue = mpTierSlider.getValue();
            setTier(mnCurrentValue);
          }
        }
      }});
    vpRUPanel.add(mpTierSlider);
    
    /*
     * Legend (display only one at a time)
     */
    mpUsageLegend = Factory.create(RackUsageLegend.class, START_X, 190,
        vnPanelWidth - START_X * 2, 30);
    vpRUPanel.add(mpUsageLegend);

    mpSpeedLegend = Factory.create(RackSpeedLegend.class, START_X, 190,
        vnPanelWidth - START_X * 2, 30);
    vpRUPanel.add(mpSpeedLegend);
    
    mpDisplaySelector.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e)
      {
        for (RackUsageBankTier vpRUBT : mapBanks)
        {
          vpRUBT.repaint();
        }
        mpUsageLegend.setVisible(mpDisplaySelector.isStatusSelected());
        mpSpeedLegend.setVisible(mpDisplaySelector.isSpeedSelected());
        mpSide.repaint();
      }
    });
    
    /*
     * Side view
     */
    mpSide = new RackUsageBank(START_X, 240, vnCraneWidth, vnSideBankHeight,
        mpRUPCL, mpDisplaySelector);
    vpRUPanel.add(mpSide);

    SKDCLabel vpBankLabel = new SKDCLabel("Bank");
    vpBankLabel.setBounds(vnPanelWidth-60, 250, 30, 20);
    vpRUPanel.add(vpBankLabel);

    mpBankSlider = new JSlider(JSlider.VERTICAL);
    mpBankSlider.setBounds(vnPanelWidth-60, 270, 50, 160);
    mpBankSlider.setPaintTicks(true);
    mpBankSlider.setMajorTickSpacing(1);
    mpBankSlider.setPaintLabels(true);
    mpBankSlider.setMaximum(4);
    mpBankSlider.setMinimum(1);
    mpBankSlider.setValue(1);
    mpBankSlider.setSnapToTicks(true);
    mpBankSlider.setEnabled(false);
    mpBankSlider.addChangeListener(new ChangeListener() {
      int mnCurrentValue = 1;
      @Override
      public void stateChanged(ChangeEvent e)
      {
        if (!mpBankSlider.getValueIsAdjusting())
        {
          if (mnCurrentValue != mpBankSlider.getValue())
          {
            mnCurrentValue = mpBankSlider.getValue();
            setBank(mnCurrentValue);
          }
        }
      }});
    vpRUPanel.add(mpBankSlider);
    
    return vpRUPanel;
  }
  
  /**
   * Set the device
   * 
   * @param isDevice
   * @param isTitle
   */
  public void setDevice(String isDevice, String isTitle)
  {
    mzSettingDevice = true;
    try
    {
      /*
       * Title
       */
      mpTitle.setText(isTitle);

      /*
       * Top-down
       */
      Location vpLoc = Factory.create(Location.class);
      String[] vasAddressRange = vpLoc.getAddressRangeForDevice(isDevice);

      String vsWarehouse = vasAddressRange[0];
      RackLocationParser vpStartLocParser = RackLocationParser.parse(
          vasAddressRange[1], true);
      RackLocationParser vpEndLocParser = RackLocationParser.parse(
          vasAddressRange[2], true);

      int vnStartBank = vpStartLocParser.getBankInteger();
      int vnEndBank = vpEndLocParser.getBankInteger();
      int vnBays = vpEndLocParser.getBayInteger();
      int vnTiers = vpEndLocParser.getTierInteger();

      boolean vzAscending = mpOrderCombo.getSelectedItem().equals(ASCENDING);
      setTopViewData(vsWarehouse, vnStartBank, vnEndBank, vnBays, vpLoc,
          vzAscending);

      mpTierSlider.setMaximum(vnTiers);
      if (mpTierSlider.getValue() != 1)
      {
        mpTierSlider.setValue(1);
      }
      else
      {
        setTier(1);
      }

      /*
       * Side
       */
       mpSide.setData(vsWarehouse, vnBays, vnTiers);

       mpBankSlider.setMinimum(vnStartBank);
       mpBankSlider.setMaximum(vnEndBank);
       mpBankSlider.setInverted(vzAscending);
       if (vzAscending)
       {
         if (mpBankSlider.getValue() != mpBankSlider.getMaximum())
         {
           mpBankSlider.setValue(mpBankSlider.getMaximum());
         }
         else
         {
           setBank(mpBankSlider.getMaximum());
         }
       }
       else
       {
         if (mpBankSlider.getValue() != mpBankSlider.getMinimum())
         {
           mpBankSlider.setValue(mpBankSlider.getMinimum());
         }
         else
         {
           setBank(mpBankSlider.getMinimum());
         }
       }
    }
    catch (Exception e)
    {
      logAndDisplayException("Error", e);
    }
    mzSettingDevice = false;
  }

  /**
   * Configure the top view (1 tier, 2 banks)
   * 
   * @param isWarehouse
   * @param inStartBank
   * @param inEndBank
   * @param inBays
   * @param ipLoc
   * @param iapBankTiers 4 RackUsageBankTiers (resolve to RackUsageBankTier[4])
   * @throws DBException
   */
  protected void setTopViewData(String isWarehouse, int inStartBank,
      int inEndBank, int inBays, Location ipLoc, boolean izAscending)
      throws DBException
  {
    // Clear everything
    for (RackUsageBankTier vpRUBT : mapBanks)
    {
      vpRUBT.clearData();
    }

    // Set the new data
    int vnBank = izAscending ? inStartBank : inEndBank;
    int vnViewBank = 0;
    if (izAscending)
    {
      while (vnBank <= inEndBank)
      {
        mapBanks[vnViewBank++].setData(isWarehouse, vnBank++, inBays);
      }
    }
    else
    {
      while (vnBank >= inStartBank)
      {
        mapBanks[vnViewBank++].setData(isWarehouse, vnBank--, inBays);
      }
    }
  }

  /**
   * Refresh the Rack Usage Panel
   */
  public void refreshRackUsagePanel()
  {
    int vnTier = mpTierSlider.getValue();
    for (RackUsageBankTier vpRUBT : mapBanks)
    {
      vpRUBT.getTierInfo(vnTier);
    }

    int vnBank = mpBankSlider.getValue();
    mpSide.getBankInfo(vnBank);
  }

  /**
   * The tier slider changed
   * 
   * @param inTier
   */
  protected void setTier(final int inTier)
  {
    if (mzSettingDevice)
    {
      for (RackUsageBankTier vpRUBT : mapBanks)
      {
        vpRUBT.getTierInfo(inTier);
      }
    }
    else
    {
      setAllEnabled(false);
      displayInfoAutoTimeOut("Gathering data... please wait.");
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run()
        {
          for (RackUsageBankTier vpRUBT : mapBanks)
          {
            vpRUBT.getTierInfo(inTier);
          }

          displayInfoAutoTimeOut("");
          setAllEnabled(true);
        }
      });
    }
  }

  /**
   * The bank slider changed
   * 
   * @param inBank
   */
  protected void setBank(final int inBank)
  {
    if (mzSettingDevice)
    {
      mpSide.getBankInfo(inBank);
    }
    else
    {
      setAllEnabled(false);
      displayInfoAutoTimeOut("Gathering data... please wait.");
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run()
        {
          mpSide.getBankInfo(inBank);

          displayInfoAutoTimeOut("");
          setAllEnabled(true);
        }
      });
    }
  }

  /*========================================================================*/
  /* RackUsagePropertyChangeListener                                        */
  /*========================================================================*/

  /**
   * <B>Description:</B> Pick up property changes from the tier (generated by a
   * valid double-click.
   *
   * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
   *
   * @author       mandrus
   * @version      1.0
   */
  protected class RackUsagePropertyChangeListener implements PropertyChangeListener
  {
    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
      propertyChangeEvent(evt);
    }
  }
  /*
   * 
   */
  protected void propertyChangeEvent(PropertyChangeEvent evt)
  {
    if (evt.getPropertyName().equals(RackUsageBankTier.ADDRESS_CLICKED))
    {
      String[] vsLocation = evt.getNewValue().toString().split("-");
      if (vsLocation.length == 2)
      {
        LocationMain vpLocMain = Factory.create(LocationMain.class,
            vsLocation[0], vsLocation[1]);
        addSKDCInternalFrame(vpLocMain);
      }
    }
  }
}
