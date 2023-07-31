package com.daifukuamerica.wrxj.swingui.equipment.properties;

import com.daifukuamerica.wrxj.WarehouseRx;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.controller.status.StatusModel;
import com.daifukuamerica.wrxj.io.PropertyReader;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentGraphic;
import com.daifukuamerica.wrxj.swingui.equipment.button.PolygonButton;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.awt.Polygon;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * <B>Description:</B> Properties for the Equipment Monitor
 *
 * <P>Copyright (c) 2008 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
public class EquipmentMonitorProperties extends PropertyReader
{
  // Tags in equipment.properties that we care about
  private static final String EQUIPMENT_STATUS_GROUP = "EquipmentStatusGroup";
  private static final String EQUIPMENT_STATUS_GROUP_END = "EquipmentStatusGroupEnd";
  private static final String EQUIPMENT_GROUP = "EquipmentGroup";
  private static final String TAB_GROUP = "TabGroup";
  private static final String LIBRARY_DEVICE = "LibraryDevice";
  private static final String LIBRARY_GROUP = "LibraryGroup";

  // Base X/Y coordinates on extra tabs
  private static final int UPPER_LEFT_X = 10;
  private static final int UPPER_LEFT_Y = 40;
  
  // Logger
  private Logger mpLogger;
  
  // Properties File & Status Model
  private String msPropertiesFileName;

  // Site Equipment
  private String msSiteEquipment;
  
  // Collections
  private List<List<String>> mpEquipmentGroupList;
  private List<List<String>> mpTabEquipmentGroupList;
  private List<List<String>> mpLibraryGroupList;
  private Map<String, String> mpLibraryDeviceMap;
  private Map<String, Map<String,String>> mpStatusItems;
  
  // Tab groups
  private String[] masTabGroups;
  private boolean mzSpecifyTabs;
  
  // MOS Connections
  private Map<String,Boolean> mpMosConnections;
  private boolean mzAnyMosConnections;
  
  // Scalers
  private int[] manVertScaling;
  private int[] manHorzScaling;
  
  // Logo placement
  private int[] manLogoX;
  private int[] manLogoY;
  
  // Light Tower Legend Panel
  private int[] manLightTowerX;
  private int[] manLightTowerY;
  private int[] manLightTowerShow;
  
  // Equipment Monitor Legend Panel
  private int[] manLegendX;
  private int[] manLegendY;
  private int[] manLegendShow;

  // Tracking panel configurations
  private int mnTrackingPanelHeight;
  private int mnTrackingPanelWidth;
  private boolean mzLongLoadIDs;
  private boolean mzAllowWRxRecovery;

  /**
   * Constructor 
   */
  public EquipmentMonitorProperties(Logger ipLogger)
  {
    mpLogger = ipLogger;
    initialize();
  }

  /**
   * @see com.daifukuamerica.wrxj.io.PropertyReader#stringToReader(java.lang.String)
   */
  @Override
  protected BufferedReader stringToReader(String isPath)
      throws FileNotFoundException, IOException
  {
    BufferedReader vpReader;
    if (Application.getBoolean(WarehouseRx.LOAD_CONFIGS_FROM_RESOURCE, false))
    {
      vpReader = new BufferedReader(new InputStreamReader(
          getClass().getResourceAsStream(isPath)));
      if (vpReader == null)
      {
        throw new FileNotFoundException("Resource \"" + isPath + "\" not found!");
      }
    }
    else
    {
      vpReader = new BufferedReader(new FileReader(isPath));
      if (vpReader == null)
      {
        throw new FileNotFoundException("File \"" + isPath + "\" not found!");
      }
    }
    return vpReader;
  }
  
  /**
   * Initialize by reading the properties file
   */
  private void initialize()
  {
    try
    {
      String vsPath = Application.getString(StatusModel.EQUIPMENT_CONFIGURATION_KEY);
      if (vsPath == null)
      {
        throw new IOException("Application property "
            + StatusModel.EQUIPMENT_CONFIGURATION_KEY + " not defined.");
      }
      mpLogger.logDebug("Loading configuration from \"" + vsPath + "\".");
      readProperties(vsPath);
    }
    catch (final IOException ve)
    {
      mpLogger.logError("Error loading configuration file: " + ve);
    }
  }
  
  /**
   * Read the equipment properties file.
   *  
   * @see com.daifukuamerica.wrxj.io.ResourceFileReader#readProperties(java.lang.String)
   */
  @Override
  public void readProperties(String isConfigFileName)
      throws FileNotFoundException, IOException
  {
    super.readProperties(isConfigFileName);
    
    msPropertiesFileName = isConfigFileName;
    
    msSiteEquipment = getProperty("SiteEquipment");
    
    mzSpecifyTabs = false;
    String vsTabGroupParameter = getProperty("ExtraTabGroups");
    if (vsTabGroupParameter == null) vsTabGroupParameter = "";
    masTabGroups = SKDCUtility.getTokens(vsTabGroupParameter, ",");
    if (masTabGroups.length > 0)
    {
      mpLogger.logDebug("Using Tabbed Groups");
    }
    else
    {
      mpLogger.logDebug("NOT using Tabbed Groups");
      vsTabGroupParameter = getProperty("DrawTabs");
      if (vsTabGroupParameter == null) vsTabGroupParameter = "";
      masTabGroups = SKDCUtility.getTokens(vsTabGroupParameter, ",");
      if (masTabGroups.length > 0)
      {
        mpLogger.logDebug("Using Specified Tabs");
        mzSpecifyTabs = true;
      }
    }

    manHorzScaling = initTabProperties("TabPanelsHorzScaling", masTabGroups.length, 100);
    manVertScaling = initTabProperties("TabPanelsVertScaling", masTabGroups.length, 100);
    manLogoX = initTabProperties("EquipmentLogoX", masTabGroups.length, 400);
    manLogoY = initTabProperties("EquipmentLogoY", masTabGroups.length, 400);
    manLightTowerX = initTabProperties("LightTowerPanelX", masTabGroups.length, 400);
    manLightTowerY = initTabProperties("LightTowerPanelY", masTabGroups.length, 0);
    manLightTowerShow = initTabProperties("LightTowerPanelShowing", masTabGroups.length, 0);
    manLegendX = initTabProperties("LegendPanelX", masTabGroups.length, 400);
    manLegendY = initTabProperties("LegendPanelY", masTabGroups.length, 150);
    manLegendShow = initTabProperties("LegendPanelShowing", masTabGroups.length, 0);
    
    mnTrackingPanelHeight = getIntProperty("LoadStatusPanelHeight", 160);
    mnTrackingPanelWidth = getIntProperty("LoadStatusPanelWidth", 950);
    mzLongLoadIDs = Boolean.parseBoolean(getProperty("UseLongLoadIDs"));
    mzAllowWRxRecovery = Boolean.parseBoolean(getProperty("AllowWRxRecovery"));

    getMOSConnections();
    
    mpEquipmentGroupList = getGraphicPropertiesCollection(EQUIPMENT_GROUP, true);
    mpTabEquipmentGroupList = getGraphicPropertiesCollection(TAB_GROUP, false);
    mpLibraryGroupList = getGraphicPropertiesCollection(LIBRARY_GROUP, true);
    mpLibraryDeviceMap = getDevicePointsMap();
    
    if (mpEquipmentGroupList == null || mpLibraryDeviceMap == null
        || mpLibraryGroupList == null)
    {
      mpLogger.logError("Error Parsing " + msPropertiesFileName 
          + "\n * EquipmentGroupList=" + mpEquipmentGroupList.toString()
          + "\n * LibraryDeviceList=" + mpLibraryDeviceMap.toString()
          + "\n * LibraryGroupList=" + mpLibraryGroupList.toString());
    }
    
    readStatusItems();
  }
  
  /**
   * Get properties that span tabs
   * @param isProperty
   * @return
   */
  private int[] initTabProperties(String isProperty, int inExtraTabs, int inDefault)
  {
    String vsParameter = getProperty(isProperty);
    int vnTabs = inExtraTabs+1;
    int[] vanValues = new int[vnTabs];
    for (int i = 0; i < vnTabs; i++)
    {
      vanValues[i] = inDefault;
    }
    
    if (vsParameter == null) vsParameter = "";
    String[] vasValues = SKDCUtility.getTokens(vsParameter, ",");
    if (vasValues.length == 0)
    {
      mpLogger.logError(isProperty + " is not defined; using default value ("
          + inDefault + ").");
    }
    else
    {
      if (vnTabs > vasValues.length)
      {
        mpLogger.logError("Insufficient values for " + isProperty 
            + ": expected " + vnTabs + " but found " + vasValues.length 
            + "; using default value (" + inDefault + ") when not defined.");
      }
      for (int i = 0; (i < vasValues.length) && (i < vnTabs); i++)
      {
        vanValues[i] = Integer.parseInt(vasValues[i]);
      }
    }
    return vanValues.clone();
  }

  /**
   * Handle MOS connections on a group-by-group basis 
   */
  private void getMOSConnections()
  {
    mpMosConnections = new TreeMap<String,Boolean>();

    String vsParameter = getProperty("GroupHasMosConnection");
    if (vsParameter == null) vsParameter = "";
    String[] vasValues = SKDCUtility.getTokens(vsParameter, ",");
    if (vasValues.length == 0)
    {
      mpLogger.logError("GroupHasMosConnection is not defined"
          + "--MOS Connection will not be used.");
    }
    else
    {
      for (String s : vasValues)
      {
        String[] vasGroup = SKDCUtility.getTokens(s, ":");
        if (vasGroup.length != 2)
        {
          mpLogger.logError("Badly formed GroupHasMosConnection parameter: "
              + s);
        }
        Boolean b = Boolean.parseBoolean(vasGroup[1]);
        mpMosConnections.put(vasGroup[0], b);
        if (b) mzAnyMosConnections = true;
      }
    }
  }
  
  /**
   * Return a List whose entries are collections of properties for the
   * collection name.
   * 
   * @param isCollectionName
   * @param izLogMissing
   * @return
   */
  private List<List<String>> getGraphicPropertiesCollection(
      String isCollectionName, boolean izLogMissing)
  {
    List<List<String>> vpCollectionList = null;
    try
    {
      vpCollectionList = getAllPropertyCollections(msPropertiesFileName,
          isCollectionName);
    }
    catch (Exception ex) 
    {
      mpLogger.logException(ex, "Error reading equipment file.");
    }
    if (vpCollectionList == null || vpCollectionList.isEmpty())
    {
      if (izLogMissing)
      {
        mpLogger.logError("NO MOS " + isCollectionName + " Configurations");
      }
      vpCollectionList = null;
    }
    return vpCollectionList;
  }

  /**
   * @return a Map of Name:Points pairs
   */
  private Map<String, String> getDevicePointsMap()
  {
    Map<String, String> vpLDMap = new HashMap<String, String>();
    
    List<List<String>> vpLDs = getGraphicPropertiesCollection(LIBRARY_DEVICE, true);
    for (List<String> vpLD : vpLDs)
    {
      Iterator<String> vpIterator = vpLD.iterator();
      vpIterator.next(); // skip off "LibraryDevice"
      String vsKey = vpIterator.next(); // Library device name
      String vsPoints = vpIterator.next(); // Points
      
      vpLDMap.put(vsKey, vsPoints);
    }
    
    return vpLDMap;
  }
  
  /*========================================================================*/
  /*  Scalers                                                               */
  /*========================================================================*/

  /**
   * Get X,Y coordinates from a String
   * 
   * @param isCoordinates
   * @param inTabIndex
   * @return pre-scaled int[X,Y]
   */
  private int[] getXY(String isCoordinates, int inTabIndex)
  {
    int[] vanXY = new int[2];
    StringTokenizer st = new StringTokenizer(isCoordinates, ",");
    if (st.hasMoreTokens())
    {
      vanXY[0] = getScaledX(Integer.parseInt(st.nextToken()), inTabIndex);
      if (st.hasMoreTokens())
      {
        vanXY[1] = getScaledY(Integer.parseInt(st.nextToken()), inTabIndex);
      }
      else
      {
        mpLogger.logError("MISSING " + isCoordinates + " value");
      }
    }
    else
    {
      mpLogger.logError("MISSING " + isCoordinates + " value");
    }
    return vanXY;
  }

  /**
   * Resize the X dimension based upon the scaling
   *
   * @param inValue
   * @param inPanelIndex
   * @return int containing corrected X value (may not have changed)
   */
  public int getScaledX(int inValue, int inPanelIndex)
  {
    if ((manHorzScaling == null) || (inPanelIndex >= manHorzScaling.length))
    {
      return inValue;
    }
    return (inValue * manHorzScaling[inPanelIndex]) / 100;
  }

  /**
   * Resize the Y dimension based upon the scaling
   *
   * @param inValue
   * @param inPanelIndex
   * @return int containing corrected X value (may not have changed)
   */
  public int getScaledY(int inValue, int inPanelIndex)
  {
    if ((manVertScaling == null) || (inPanelIndex >= manVertScaling.length))
    {
      return inValue;
    }

    return (inValue * manVertScaling[inPanelIndex]) / 100;
  }
  
  /*========================================================================*/
  /*  Getters                                                               */
  /*========================================================================*/

  /*------------------------------------------------------------------------*/
  /*  Getters - Collections                                                 */
  /*------------------------------------------------------------------------*/

  /**
   * @return Equipment group properties
   */
  public List<EquipmentGroupProperty> getEquipmentGroupList(int inTabIndex)
  {
    for (List<String> vpPropList : mpEquipmentGroupList)
    {
      String vsEquipName = getProperty(vpPropList, EQUIPMENT_GROUP);
      if (vsEquipName.equals(msSiteEquipment))
      {
        if (vpPropList.isEmpty())
        {
          mpLogger.logError("MISSING MOS EquipmentGroup Configuration");
          return null;
        }
        else
        {
          List<EquipmentGroupProperty> vpList = new ArrayList<EquipmentGroupProperty>();
          
          Iterator<String> vpIterator = vpPropList.iterator();
          vpIterator.next(); // Skip off "EquipmentGroup"
          vpIterator.next(); // Skip off EquipmentGroupName
 
          while (vpIterator.hasNext())
          {
            EquipmentGroupProperty vpEGP = new EquipmentGroupProperty();
            vpEGP.msName = vpIterator.next();
            if (vpEGP.msName.equals("EquipmentGroupEnd"))
            {
              break;
            }
            vpEGP.manSize = getXY(vpIterator.next(), inTabIndex);
            if (inTabIndex == 0 || mzSpecifyTabs)
            {
              vpEGP.manLocation = getXY(vpIterator.next(), inTabIndex);
            }
            else
            {
              vpIterator.next();
              vpEGP.manLocation = new int[] { UPPER_LEFT_X, UPPER_LEFT_Y };
            }
            vpEGP.msLibraryGroup = vpIterator.next();
            vpList.add(vpEGP);
          }
          
          return vpList;
        }
      }
    }
    
    mpLogger.logError("MISSING EquipmentGroup \"" + msSiteEquipment + "\"");
    return null;
  }
  
  /**
   * @return Equipment group properties for a specified tab
   */
  public List<EquipmentGroupProperty> getTabEquipmentGroupList(int inTabIndex)
  {
    for (List<String> vpPropList : mpTabEquipmentGroupList)
    {
      String vsEquipName = getProperty(vpPropList, TAB_GROUP);
      if (vsEquipName.equals(masTabGroups[inTabIndex]))
      {
        if (vpPropList.isEmpty())
        {
          if (getEquipmentGroupList(0) == null)
          {
            mpLogger.logError("MISSING MOS TabGroup Configuration");
          }
          return null;
        }
        else
        {
          List<EquipmentGroupProperty> vpList = new ArrayList<EquipmentGroupProperty>();
          
          Iterator<String> vpIterator = vpPropList.iterator();
          vpIterator.next(); // Skip off "TabGroup"
          vpIterator.next(); // Skip off TabGroupName
 
          while (vpIterator.hasNext())
          {
            EquipmentGroupProperty vpEGP = new EquipmentGroupProperty();
            vpEGP.msName = vpIterator.next();
            if (vpEGP.msName.equals("TabGroupEnd"))
            {
              break;
            }
            vpEGP.manSize = getXY(vpIterator.next(), inTabIndex);
            if (inTabIndex == 0 || mzSpecifyTabs)
            {
              vpEGP.manLocation = getXY(vpIterator.next(), inTabIndex);
            }
            else
            {
              vpIterator.next();
              vpEGP.manLocation = new int[] { UPPER_LEFT_X, UPPER_LEFT_Y };
            }
            vpEGP.msLibraryGroup = vpIterator.next();
            vpList.add(vpEGP);
          }
          
          return vpList;
        }
      }
    }
    
    mpLogger.logError("MISSING TabGroup \"" + masTabGroups[inTabIndex] + "\"");
    return null;
  }
  
  /**
   * @param isLibraryGroup
   * @param inTabIndex
   * @return Library group properties
   */
  public List<LibraryGroupProperty> getLibraryGroupList(String isLibraryGroup,
      int inTabIndex)
  {
    for (List<String> vpLibraryGroupList : mpLibraryGroupList)
    {
      if (vpLibraryGroupList.isEmpty())
      {
        continue;
      }
      
      String vsLibraryGroup = getProperty(vpLibraryGroupList,
          EquipmentMonitorProperties.LIBRARY_GROUP);
      if (vsLibraryGroup.equals(isLibraryGroup))
      {
        List<LibraryGroupProperty> vpList = new ArrayList<LibraryGroupProperty>();
        
        Iterator<String> vpIterator = vpLibraryGroupList.iterator();
        vpIterator.next(); // skip off "LibraryGroup"
        vpIterator.next(); // skip off LibraryGroup name
        while (vpIterator.hasNext())
        {
          LibraryGroupProperty vpLGP = new LibraryGroupProperty();
          vpLGP.msLibraryDevice = vpIterator.next();
          if (vpLGP.msLibraryDevice.equals("LibraryGroupEnd"))
          {
            break;
          }
          vpLGP.msName = vpIterator.next();
          vpLGP.manLocation = getXY(vpIterator.next(), inTabIndex);
          vpList.add(vpLGP);
        }
        return vpList;
      }
    }
    
    mpLogger.logError("MISSING LibraryGroup \"" + isLibraryGroup + "\"");
    return null;
  }

  /**
   * @param isDeviceName
   * @param inTabIndex
   * @param ianOffset - int[] { x , y }
   * @return Library Device properties
   */
  public Polygon getPolygon(String isDeviceName, int inTabIndex, int[] ianOffset)
  {
    String vsPoints = mpLibraryDeviceMap.get(isDeviceName);
    if (vsPoints == null)
    {
      mpLogger.logError(isDeviceName + " is not defined.  Using default shape.");
      vsPoints = "0,0,,20,0,,20,20,,0,20";
    }
    
    Polygon vpEquipmentShape = new Polygon();
    StringTokenizer tokenizer = new StringTokenizer(vsPoints, ",");
    while (tokenizer.hasMoreTokens())
    {
      String str = tokenizer.nextToken().trim();
      int x = getScaledX(Integer.parseInt(str) + ianOffset[0], inTabIndex);
      str = tokenizer.nextToken().trim();
      int y = getScaledY(Integer.parseInt(str) + ianOffset[1], inTabIndex);
      vpEquipmentShape.addPoint(x, y);
    }

    return vpEquipmentShape;
  }
  
  /*------------------------------------------------------------------------*/
  /* Getters - Frame                                                        */
  /*------------------------------------------------------------------------*/
  
  /**
   * @return the Site Equipment name
   */
  public String getSiteEquipment()
  {
    return msSiteEquipment;
  }
  
  /**
   * @return the frame height
   */
  public int getFrameHeight()
  {
    return getIntProperty("FrameHeight", 530);
  }
  
  /**
   * @return the frame width
   */
  public int getFrameWidth()
  {
    return getIntProperty("FrameWidth", 965);
  }
  
  /*------------------------------------------------------------------------*/
  /* Getters - Tabs & Groups                                                */
  /*------------------------------------------------------------------------*/

  /**
   * @return String[]
   */
  public String[] getTabGroups()
  {
    return masTabGroups;
  }
  
  /**
   * @return Are there tab groups?
   */
  public boolean hasTabGroups()
  {
    return masTabGroups.length > 0;
  }

  /**
   * Are there specified tabs?
   * 
   * @return
   */
  public boolean hasSpecifiedTabs()
  {
    return mzSpecifyTabs;  
  }
  
  /**
   * Does this equipment monitor have multiple equipment groups?
   * 
   * @return
   */
  public boolean hasMultipleEquipmentGroups()
  {
    return getGraphicPropertiesCollection(LIBRARY_GROUP, true).size() > 1; 
  }

  /**
   * Get the Device Type for a given group.  Default is "AGC".
   * @param isGroupName
   * @return
   */
  public String getDeviceTypeForGroup(String isGroupName)
  {
    String vsDeviceType = getProperty(isGroupName + ".DeviceType");
    if (vsDeviceType == null)
    {
      vsDeviceType = getProperty("Default" + ".DeviceType");
      if (vsDeviceType == null)
      {
        vsDeviceType = "AGC";
      }
    }
      
    return vsDeviceType;
  }

  /**
   * @return true if there are any MOS connections, false otherwise
   */
  public boolean anyMOSConnections()
  {
    return mzAnyMosConnections;
  }
  
  /**
   * @param isGroup
   * @return true if the group has a MOS connection, false otherwise
   */
  public boolean hasMOSConnection(String isGroup)
  {
    if (mpMosConnections == null)
      return false;
    
    Boolean b = mpMosConnections.get(isGroup);
    if (b == null)
    {
      b = mpMosConnections.get("Default");
      if (b == null)
      {
        return false;
      }
    }
    
    return b.booleanValue();
  }
  
  /*------------------------------------------------------------------------*/
  /* Getters - Title & Logo                                                 */
  /*------------------------------------------------------------------------*/

  /**
   * @return the title
   */
  public String getTitle()
  {
    String vsTitle = getProperty("SiteEquipment");
    if (vsTitle != null)
    {
      vsTitle = vsTitle.replace('|', ' ');
    }
    return vsTitle;
  }
  
  /**
   * @return the Title X coordinate
   */
  public int getTitleX()
  {
    return getIntProperty("EquipmentLabelX", 60);
  }
  
  /**
   * @return the Title Y coordinate
   */
  public int getTitleY()
  {
    return getIntProperty("EquipmentLabelY", 5);
  }

  /**
   * @return the URL for the logo
   */
  public URL getLogoURL()
  {
    String vsLogoPath = getProperty("EquipmentLogo");
    if (vsLogoPath != null)
    {
      return getClass().getResource(vsLogoPath);
    }
    else
    {
      return null;
    }
  }
  
  /**
   * @param inPanel - the panel index
   * @return the Logo X coordinate
   */
  public int getLogoX(int inPanel)
  {
    return manLogoX[inPanel];
  }

  /**
   * @param inPanel - the panel index
   * @return the Logo Y coordinate
   */
  public int getLogoY(int inPanel)
  {
    return manLogoY[inPanel];
  }

  /*------------------------------------------------------------------------*/
  /* Getters - Light Tower Legend Panel                                     */
  /*------------------------------------------------------------------------*/

  /**
   * @param inPanel - the panel index
   * @return true to show, false otherwise
   */
  public boolean needsLightTower(int inPanel)
  {
    return manLightTowerShow[inPanel] != 0;
  }

  /**
   * @param inPanel - the panel index
   * @return the LightTowerX
   */
  public int getLightTowerX(int inPanel)
  {
    return getScaledX(manLightTowerX[inPanel], inPanel);
  }

  /**
   * @param inPanel - the panel index
   * @return the LightTowerY
   */
  public int getLightTowerY(int inPanel)
  {
    return getScaledY(manLightTowerY[inPanel], inPanel);
  }

  /**
   * @return the width for the Light Tower Legend Panel
   */
  public int getLightTowerWidth()
  {
    return getIntProperty("LightTowerPanelWidth", 465);
  }

  /**
   * @return the width for the Light Tower Legend Panel
   */
  public int getLightTowerHeight()
  {
    return getIntProperty("LightTowerPanelHeight", 55);
  }

  /**
   * @return the width for the Light Tower Legend Panel
   */
  public int getLightTowerRows()
  {
    return getIntProperty("LightTowerPanelRows", 2);
  }

  /**
   * @return the width for the Light Tower Legend Panel
   */
  public int getLightTowerColumns()
  {
    return getIntProperty("LightTowerPanelCols", 2);
  }

  /*------------------------------------------------------------------------*/
  /* Getters - Equipment Monitor Legend Panel                               */
  /*------------------------------------------------------------------------*/

  /**
   * @param inPanel - the panel index
   * @return true to show, false otherwise
   */
  public boolean needsLegend(int inPanel)
  {
    return manLegendShow[inPanel] != 0;
  }

  /**
   * @param inPanel - the panel index
   * @return the LegendX
   */
  public int getLegendX(int inPanel)
  {
    return getScaledX(manLegendX[inPanel], inPanel);
  }

  /**
   * @param inPanel - the panel index
   * @return the LegendY
   */
  public int getLegendY(int inPanel)
  {
    return getScaledY(manLegendY[inPanel], inPanel);
  }

  /**
   * @return the width for the Legend Panel
   */
  public int getLegendWidth()
  {
    return getIntProperty("LegendPanelWidth", 465);
  }

  /**
   * @return the width for the Legend Panel
   */
  public int getLegendHeight()
  {
    return getIntProperty("LegendPanelHeight", 55);
  }

  /**
   * @return the width for the Legend Panel
   */
  public int getLegendRows()
  {
    return getIntProperty("LegendPanelRows", 2);
  }

  /**
   * @return the width for the Legend Panel
   */
  public int getLegendColumns()
  {
    return getIntProperty("LegendPanelCols", 2);
  }

  /*------------------------------------------------------------------------*/
  /* Getters - Tracking                                                     */
  /*------------------------------------------------------------------------*/

  /**
   * @return the Tracking Panel Height
   */
  public int getTrackingPanelX()
  {
    return getIntProperty("LoadStatusPanelX", 0);
  }

  /**
   * @return the Tracking Panel Width
   */
  public int getTrackingPanelY()
  {
    return getIntProperty("LoadStatusPanelY", 300);
  }

  /**
   * @return the Tracking Panel Height
   */
  public int getTrackingPanelHeight()
  {
    return mnTrackingPanelHeight;
  }

  /**
   * @return the Tracking Panel Width
   */
  public int getTrackingPanelWidth()
  {
    return mnTrackingPanelWidth;
  }

  /**
   * @return Are we using long load IDs?
   */
  public boolean useLongLoadIDs()
  {
    return mzLongLoadIDs;
  }

  /**
   * @return do we allow Warehouse Rx recovery from the tracking panel?
   */
  public boolean allowWRxRecovery()
  {
    return mzAllowWRxRecovery;
  }
  
  /*------------------------------------------------------------------------*/
  /* Getters - Class definitions                                            */
  /*------------------------------------------------------------------------*/

  /**
   * @param isErrorSet
   * @return the class name for the error set
   */
  public String getErrorClass(String isErrorSet)
  {
    return getProperty("ErrorSet." + isErrorSet);
  }
  
  /**
   * @param isGraphicName
   * @return the class name for the error set
   */
  public String getGraphicClass(String isGraphicName)
  {
    return getProperty("GraphicClass." + isGraphicName);
  }
  
  /**
   * Get the class to use for the Graphic
   * @param isDevice
   * @return
   */
  public Class<EquipmentGraphic> getEquipmentGraphicClass(String isDevice)
  {
    // Default value
    Class vpEqClass = PolygonButton.class;
    
    // Get the status item
    Map<String,String> vpStatusItem = mpStatusItems.get(isDevice);
    if (vpStatusItem == null)
    {
      return vpEqClass;
    }
    
    // Get the graphic class
    String vsGraphicClass = vpStatusItem.get(StatusModel.GRAPHIC_CLASS);
    try
    {
      if (!vsGraphicClass.contains("."))
      {
        vsGraphicClass = getGraphicClass(vsGraphicClass);
      }
      if (vsGraphicClass != null)
      {
        vpEqClass = getClass().getClassLoader().loadClass(vsGraphicClass);
        if (!EquipmentGraphic.class.isAssignableFrom(vpEqClass))
        {
          vpEqClass = PolygonButton.class;
        }
      }
    }
    catch (ClassNotFoundException cnfe)
    {
      vpEqClass = PolygonButton.class;
    }
    return vpEqClass;
  }

  /*------------------------------------------------------------------------*/
  /* Getters - Status Model                                                 */
  /* These methods were stolen from the Status Model                        */
  /*------------------------------------------------------------------------*/

  private static final String[] EQUIPMENT_PROPERTIES = {
    StatusModel.EQUIPMENT_NAME,
    StatusModel.DESCRIPTION,
    StatusModel.CATEGORY,
    StatusModel.ERROR_SET,
    StatusModel.MC_CONTROLLER,
    StatusModel.MOS_CONTROLLER,
    StatusModel.AISLE_GROUP,
    StatusModel.HOST_ID,
    StatusModel.DEVICE_ID,
    StatusModel.STATION_ID,
    StatusModel.MC_ID,
    StatusModel.MOS_ID,
    StatusModel.GRAPHIC_CLASS,
    StatusModel.GRAPHIC_PARAMETER
  };

  /**
   * Read the items from the properties file that will require status checking
   */
  private void readStatusItems()
  {
    mpStatusItems = new TreeMap<String, Map<String,String>>();
    
    // Get a list of property collections
    List<List<String>> equipmentGroupList = getGraphicPropertiesCollection(
        EQUIPMENT_STATUS_GROUP, true);
    if (equipmentGroupList == null)
    {
      return;
    }
    
    // Find the property collection that matches our equipment group
    List<String> mpSiteStatusList = null;
    for (Iterator<List<String>> equipmentSiteGroupListIterator = 
      equipmentGroupList.iterator(); equipmentSiteGroupListIterator.hasNext();)
    {
      mpSiteStatusList = equipmentSiteGroupListIterator.next();
      if (mpSiteStatusList.isEmpty())
      {
        mpSiteStatusList = null;
        break;
      }
      String equipmentSiteGroup = getProperty(mpSiteStatusList,
          EQUIPMENT_STATUS_GROUP);
      if (msSiteEquipment.equals(equipmentSiteGroup))
      {
        break;
      }
      mpSiteStatusList = null;
    }
    if (mpSiteStatusList == null)
    {
      mpLogger.logError("StatusModel - MISSING " + EQUIPMENT_STATUS_GROUP + " \""
          + msSiteEquipment + "\"");
      return;
    }
    if (mpSiteStatusList.isEmpty())
    {
      mpLogger.logError("StatusModel - MISSING MOS " + EQUIPMENT_STATUS_GROUP
          + " Configuration");
      return;
    }
    
    //
    // We now have the SITE EQUIPMENT Groups to use in the StatusModel.
    // Iterate through all the equipment groups to build our StatusModel.
    //
    List<Map<String, String>> vpEquipmentPropertyList = getStatusItemsFromList(
        mpSiteStatusList);
    for (Map<String,String> m : vpEquipmentPropertyList)
    {
      addStatusItem(m);
    }
  }

  /**
   * Add the status item to mpStatusItems (and initialize missing values)
   * @param ipProperties
   */
  public void addStatusItem(Map<String,String> ipProperties)
  {
    String vsKeyName = ipProperties.get(StatusModel.EQUIPMENT_NAME);
    mpStatusItems.put(vsKeyName, ipProperties);
    
    // Check for uninitialized values
    for (int i = 0; i < EQUIPMENT_PROPERTIES.length; i++)
    {
      String vsKey = EQUIPMENT_PROPERTIES[i];
      String vsData = ipProperties.get(vsKey);
      if (vsData == null)
      {
        vsData = StatusModel.UNKNOWN;
        ipProperties.put(vsKey, vsData);
      }
    }
    
    // If this is a rack or for future use, don't worry about status
    if (!ipProperties.get(StatusModel.CATEGORY).equals(StatusModel.CAT_EQUIPMENT)
        || ipProperties.get(StatusModel.GRAPHIC_PARAMETER).contains("Future"))
    {
      ipProperties.put(StatusModel.INACTIVE, "true");
    }
  }

  /**
   * Extract the status items
   * 
   * @param vpPropertyList
   * @return
   */
  private List<Map<String,String>> getStatusItemsFromList(List<String> vpPropertyList)
  {
    List<Map<String,String>> vpEquipmentList = new ArrayList<Map<String, String>>();
    Iterator<String> vpPropListIterator = vpPropertyList.iterator();
    //
    // Find first collectionSeparator.
    //
    String vsName = null;
    while (vpPropListIterator.hasNext())
    {
      vsName = vpPropListIterator.next();
      if ((vsName.equalsIgnoreCase(StatusModel.EQUIPMENT_NAME)) ||
          (vsName.equalsIgnoreCase(EQUIPMENT_STATUS_GROUP_END)))
      {
        break;
      }
    }
    //
    // At the top of this loop we should be at the property past the
    // first of equipment separators
    //
    while (vpPropListIterator.hasNext())
    {
      if (vsName.equalsIgnoreCase(EQUIPMENT_STATUS_GROUP_END))
      {
        break;
      }
      vsName = vpPropListIterator.next(); // skip off collectionSeparator
      
      if (vsName.equalsIgnoreCase(EQUIPMENT_STATUS_GROUP_END))
      {
        break; // Done with all pieces of equipment.
      }
      Map<String,String> propertiesCollection = new HashMap<String, String>();
      vpEquipmentList.add(propertiesCollection);
      // Add the equipment name
      propertiesCollection.put(StatusModel.EQUIPMENT_NAME, vsName);
      // Now add name value pairs for the rest of the equipment's properties
      while (vpPropListIterator.hasNext())
      {
        vsName = vpPropListIterator.next();
        
        if ((vsName.equalsIgnoreCase(StatusModel.EQUIPMENT_NAME)) ||
            (vsName.equalsIgnoreCase(EQUIPMENT_STATUS_GROUP_END)))
        {
          break; // Done with this piece of equipment.
        }
        
        // Make sure we have a valid name field before trying to get a value
        boolean vzValid = false;
        for(int i=0; i<EQUIPMENT_PROPERTIES.length; i++)
        {
          if(vsName.equalsIgnoreCase(EQUIPMENT_PROPERTIES[i]))
          {
            vzValid = true;
            break;
          }
        }
        // If it is a valid name the next token should be it's value
        if (vpPropListIterator.hasNext() && vzValid)
          propertiesCollection.put(vsName, vpPropListIterator.next());
      }
    }
    return vpEquipmentList;
  }

  /**
   * Get the status items
   * @return
   */
  public Collection<Map<String,String>> getStatusItems()
  {
    return mpStatusItems.values();
  }
}
