package com.daifukuamerica.wrxj.swingui.monitor;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/**
 * <B>Description:</B> A tabbed pane that can have group tabbed panes if there
 * are too many tabs.
 * 
 * <P>Copyright (c) 2008 by Daifuku America Corporation</P>
 * 
 * @author mandrus
 * @version 1.0
 */
public class DacLogMonitorTabbedPane extends JTabbedPane
{
  Map<String,Component> mpLogMap = new TreeMap<String, Component>();
  
  String[] masTabOrder = null;
  Map<String,String> mpSpecifiedGroups = null;
  int mnMaxTabs = 8;
  
  /**
   * Constructor
   */
  public DacLogMonitorTabbedPane()
  {
    super();
  }

  /**
   * Constructor
   * @param inMaxTabs
   */
  public DacLogMonitorTabbedPane(int inMaxTabs)
  {
    this();
    mnMaxTabs = inMaxTabs;
  }
  
  /**
   * Constructor
   * @param inMaxTabs
   * @param iasTabOrder
   * @param ipGroups
   */
  public DacLogMonitorTabbedPane(int inMaxTabs, String[] iasTabOrder,
      Map<String, String> ipGroups)
  {
    this(inMaxTabs);
    masTabOrder = iasTabOrder;
    mpSpecifiedGroups = ipGroups;
  }
  
  /**
   * Add a tab
   */
  @Override
  public void addTab(final String isTabName, final Component ipTabContents)
  {
    // We need to do this because the tabs can be added from multiple threads
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        // Store the tab in case we need to rebuild
        mpLogMap.put(isTabName, ipTabContents);
        
        // Add the new tab
        if (mpLogMap.size() > mnMaxTabs)
        {
          rebuildPanel();
        }
        else
        {
          addTabToMainTab(isTabName, ipTabContents);
        }
      }
    });
  }

  /**
   * Add the new tab to the main tab. We don't have enough tabs to generate tab
   * groups yet.
   * 
   * @param isTabName
   * @param ipTabContents
   */
  private void addTabToMainTab(String isTabName, Component ipTabContents)
  {
    /*
     * Add the component to the Tabbed Panel in alphanumeric order.
     */
    int vnInsertAtPosition = 0;
    while (vnInsertAtPosition < getTabCount()
        && isTabName.compareTo(getTitleAt(vnInsertAtPosition).trim()) > 0)
    {
      vnInsertAtPosition++;
    }
    insertTab(padTabName(isTabName), null, ipTabContents, null,
        vnInsertAtPosition);
  }

  /**
   * Add the new tab to the main tab. We don't have enough tabs to generate tab
   * groups yet.
   * 
   * @param isTabName
   * @param ipTabContents
   * @param ipGroupTab
   */
  private void addTabToGroupTab(String isTabName, Component ipTabContents,
      JTabbedPane ipGroupTab)
  {
    /*
     * Add the component to the Tabbed Panel in alphanumeric order.
     */
    int vnInsertAtPosition = 0;
    while (vnInsertAtPosition < ipGroupTab.getTabCount()
        && isTabName.compareTo(ipGroupTab.getTitleAt(vnInsertAtPosition)) > 0)
    {
      vnInsertAtPosition++;
    }
    ipGroupTab.insertTab(padTabName(isTabName), null, ipTabContents, null,
        vnInsertAtPosition);
  }

  /**
   * There are so many tabs that we need to break things up and build tab
   * groups.
   */
  private synchronized void rebuildPanel()
  {
    // Remove everything
    removeAll();
    
    // Get the log names
    String[] vasKeys = mpLogMap.keySet().toArray(new String[0]);
    Arrays.sort(vasKeys);
    
    // Add an emulator tab
    vasKeys = addEmulatorGroupTab(vasKeys);

    // Add system-specified tabs
    vasKeys = addSpecifiedGroupsTabs(vasKeys);
    
    // Figure out how many extra groups
    int vnGroups = vasKeys.length / mnMaxTabs;
    if (vasKeys.length % mnMaxTabs > 0) vnGroups++;
    
    // Build extra groups
    for (int i = 0; i < vnGroups; i++)
    {
      JTabbedPane vpGroup = new JTabbedPane();
      
      int vnStart = i * mnMaxTabs;
      int vnEnd = Math.min(i * mnMaxTabs + mnMaxTabs, vasKeys.length);
      
      String vsTitle = vasKeys[vnStart].trim() + " - "
          + vasKeys[vnEnd - 1].trim();
      addTabToMainTab(vsTitle, vpGroup);
      
      for (int j = vnStart; j < vnEnd; j++)
      {
        vpGroup.addTab(padTabName(vasKeys[j]), mpLogMap.get(vasKeys[j]));
      }
    }
    
    // Select the first tab
    setSelectedIndex(0);
  }

  /**
   * Add an emulator tab
   * 
   * @param iasLogs
   * @return
   */
  private String[] addEmulatorGroupTab(String[] iasLogs)
  {
    // Add an emulator tab
    JTabbedPane vpTP = new JTabbedPane();
    super.addTab(padTabName("Emulators"), vpTP);

    // Figure out where all of the components go
    List<String> vpKeyList = new ArrayList<String>();
    for (String s : iasLogs)
    {
      if (s.endsWith("Emulator"))
      {
        addTabToGroupTab(s, mpLogMap.get(s), vpTP); 
      }
      else
      {
        vpKeyList.add(s);
      }
    }

    // Remove any empty tabs
    if (vpTP.getTabCount() == 0)
    {
      removeTabAt(indexOfTab(padTabName("Emulators")));
    }

    // Any left-overs will get added normally
    return vpKeyList.toArray(new String[0]);
  }
  
  /**
   * Add specified group tabs
   * 
   * @param iasLogs
   * @return
   */
  private String[] addSpecifiedGroupsTabs(String[] iasLogs)
  {
    // If nothing is specified, then return
    if (masTabOrder == null || mpSpecifiedGroups == null)
    {
      return iasLogs;
    }

    // Add all of the tabs in the specified order
    Map<String,JTabbedPane> vpSpecifiedGroupMap = new TreeMap<String, JTabbedPane>();
    for (String s : masTabOrder)
    {
      JTabbedPane vpTP = new JTabbedPane();
      super.addTab(padTabName(s), vpTP);
      vpSpecifiedGroupMap.put(s, vpTP);
    }

    // Figure out where all of the components go
    List<String> vpKeyList = new ArrayList<String>();
    for (String s : iasLogs)
    {
      String vsGroup = mpSpecifiedGroups.get(s);
      // Get -Port, -Emulator, etc.
      if (vsGroup == null)
      {
        int vnDash = s.indexOf('-');
        if (vnDash > 0)
        {
          vsGroup = mpSpecifiedGroups.get(s.substring(0, vnDash));
        }
      }
      if (vsGroup == null)
      {
        // Save for unspecified tab
        vpKeyList.add(s);
      }
      else
      {
        JTabbedPane vpTP = vpSpecifiedGroupMap.get(vsGroup);
        if (vpTP == null)
        {
          // Save for unspecified tab
          vpKeyList.add(s);
        }
        else
        {
          // Add to the specified tab
          addTabToGroupTab(s, mpLogMap.get(s), vpTP);
        }
      }
    }

    // Remove any empty tabs
    for (String s : vpSpecifiedGroupMap.keySet())
    {
      if (vpSpecifiedGroupMap.get(s).getTabCount() == 0)
      {
        removeTabAt(indexOfTab(padTabName(s)));
      }
    }

    // Any left-overs will get added normally
    return vpKeyList.toArray(new String[0]);
  }
  
  /**
   * Pad short names to make bigger tabs
   * 
   * @param isTabName
   * @return
   */
  private String padTabName(String isTabName)
  {
    isTabName = isTabName.trim();
    if (isTabName.length() < 5)
    {
      isTabName = "   " + isTabName + "   ";
    }
    isTabName = isTabName.replace('|', ' ');
    return isTabName;
  }
}
