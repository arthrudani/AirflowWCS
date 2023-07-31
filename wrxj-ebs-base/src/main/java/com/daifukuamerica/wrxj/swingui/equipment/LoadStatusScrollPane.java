package com.daifukuamerica.wrxj.swingui.equipment;

import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.observer.ObservableControllerImpl;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusEventDataFormat;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusInfo;
import com.daifukuamerica.wrxj.swing.AbstractMonitorScrollPane;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Stephen Kendorski
 * @version 1.0
 */
@SuppressWarnings("serial")
public class LoadStatusScrollPane extends AbstractMonitorScrollPane
{
  public static final String NONE_ACTIVE = "*NONE*";
  public static final String DISPLAY_NONE = "None";
  private List<String> mpDisplayedLoads = new ArrayList<String>();
  private String activeGroupName = NONE_ACTIVE;
  private Map<String, Map> mpCurrentDBLoads = new HashMap<String, Map>();
  private StatusEventDataFormat mpLastStatusUpdate = null;
  
  private static final int[] STATUS_COLUMN_FIELDS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

  public static final int VSS_COLUMN = 0;
  public static final int EQUIP_COLUMN = 1;
  public static final int MCKEY_COLUMN = 2;
  public static final int BAR_CODE_COLUMN = 3;
  public static final int WRX_STATUS_COLUMN = 4;
  public static final int SRC_STATUS_COLUMN = 5;
  public static final int FROM_COLUMN = 6;
  public static final int DEST_COLUMN = 7;
  public static final int LOAD_SIZE_COLUMN = 8;
  public static final int TIME_COLUMN = 9;
  public static final int MACHINEID_COLUMN = 10;

  private static final String[] STATUS_COLUMN_NAMES_NORMAL = {
                                   " ",
                                   "Equipment ID",
                                   "Load ID",
                                   "Bar Code",
                                   "WRx Status",
                                   "SRC Status",
                                   "From",
                                   "To",
                                   "Size",
                                   "Last Update"};
  private static final String[] STATUS_COLUMN_NAMES_LONG_LOADID = {
                                   " ",
                                   "Equipment ID",
                                   "MC Key",
                                   "Bar Code (Load ID)",
                                   "WRx Status",
                                   "SRC Status",
                                   "From",
                                   "To",
                                   "Size",
                                   "Last Update"};
  private static final String[] STATUS_COLUMN_WIDTHS = {
                                   "     ",
                                   "     Equipment ID      ",
                                   "    MC-Key    ",
                                   "      Bar Code      ",
                                   "           WRx Status           ",
                                   "  Move Type  ",
                                   "      Move Load From      ",
                                   "       Move Load To       ",
                                   "Size",
                                   "      Last Update     "};

  // Keys for the DB data map
  public static final String DB_MCKEY   = "mckey";
  public static final String DB_DEVICE  = "device";
  public static final String DB_TIME    = "time";
  public static final String DB_SIZE    = "size";
  public static final String DB_BCRDATA = "bcrData";
  public static final String DB_FROM    = "from";
  public static final String DB_TO      = "to";
  public static final String DB_STATUS  = "status";

  // Filtering by a single graphic
  private String msGraphicFilter = "";
  private List<String[]> mpAllTrackingData = new ArrayList<String[]>();
  
  /**
   * Constructor
   * 
   * @param izLongLoadIDs - true if Barcode with > 8 characters is load ID
   */
  public LoadStatusScrollPane(boolean izLongLoadIDs)
  {
    super();
    columnNames = izLongLoadIDs ? STATUS_COLUMN_NAMES_LONG_LOADID
        : STATUS_COLUMN_NAMES_NORMAL;
    columnNamesDefaultWidth = STATUS_COLUMN_WIDTHS;
    dataMap = STATUS_COLUMN_FIELDS;
  }

  public void setActiveGroupName(String isActiveGroupName)
  {
    activeGroupName = isActiveGroupName;
    if (isActiveGroupName.equals(NONE_ACTIVE))
    {
      dataList.clear();
      mpAllTrackingData.clear();
      mpDisplayedLoads.clear();
      msGraphicFilter = "";
      mpLastStatusUpdate = null;
    }
    processStatusChanges(mpLastStatusUpdate);
  }

  public void setCurrentDBLoads(Map ipCurrentDBLoads)
  {
    mpCurrentDBLoads = ipCurrentDBLoads;
    processStatusChanges(mpLastStatusUpdate);
  }


  public void update(Observable o, Object arg)
  {
    ObservableControllerImpl observableImpl = (ObservableControllerImpl)o;
    String sText = observableImpl.getStringData();
    StatusEventDataFormat vpSEDF = new StatusEventDataFormat("Parse");
    vpSEDF.setMessage(sText);
    try
    {
      switch (vpSEDF.getType())
      {
        case ControllerConsts.CONTROLLER_STATUS:
        case ControllerConsts.UPDATE_STATUS:
          processStatusChanges(vpSEDF);
          break;
      }
    }
    catch (Exception e)
    {
      logger.logException(e, "\n\n" + sText + "\n\n");
    }
  }

  /*--------------------------------------------------------------------------*/
  private void processStatusChanges(StatusEventDataFormat ipSEDF)
  {
    String vsLastMCKey = null;
    int vnSelected = dataTable.getSelectedRow();
    if (vnSelected >= 0)
    {
      vsLastMCKey = (String) getValueAt(vnSelected, MCKEY_COLUMN);
    }
    mpLastStatusUpdate = ipSEDF;
    Set<String> vpUsedDBLoads = new TreeSet<String>();

    if (ipSEDF != null)
    {
      dataList.clear();
      mpAllTrackingData.clear();
      mpDisplayedLoads.clear();
      
      for (StatusInfo s : ipSEDF.getStatusList())
      {
        String[] statusEntry = null;
        String keyId = s.getTrackName();
        String machineId = s.getTrackMachine();
        String mcKey = s.getTrackKey();
        String bcrData = s.getTrackBCR();
        bcrData = bcrData.trim();
        if (bcrData.length() == 0)
        {
          bcrData = " ";
        }
        String transportType = s.getTrackType();
        String src = s.getTrackSrc();
        String dst = s.getTrackDest();
        String loadSize = s.getTrackSize();
        if (loadSize.charAt(0) == '0')
        {
          loadSize = loadSize.substring(1,loadSize.length());
        }
        String trackingUpdateTime = s.getTrackTime();
        if (transportType.equals(StatusEventDataFormat.STATUS_NOT_APPLICABLE))
        {
          /*
           * A transportType of "N/A" tells us that the active group's record
           * count was some non-zero count, but is now zero. In this case we
           * skip this entry.
           */
          mpLastStatusUpdate = null;
          continue;
        }
        //
        // Only display the Load Tracking Status for the one active device group.
        //
        if (keyId.startsWith(activeGroupName))
        {
          //
          // If load status is not "N/A" then there ARE valid load statuses.
          //
          statusEntry = new String[STATUS_COLUMN_NAMES_NORMAL.length + 1];
          int index = dataList.size();
          String vss = "" + (index + 1);
          statusEntry[VSS_COLUMN] = vss;
          statusEntry[EQUIP_COLUMN] = keyId;
          statusEntry[MCKEY_COLUMN] = mcKey;
          mpDisplayedLoads.add(mcKey);
          statusEntry[BAR_CODE_COLUMN] = bcrData;
          //
          // If the mcKey or bcrData matches a LoadId in the WRx-J database
          // add the WRx-J status to the display.
          //
          if (mpCurrentDBLoads.containsKey(mcKey))
          {
            Map dbLoad = mpCurrentDBLoads.get(mcKey);
            statusEntry[WRX_STATUS_COLUMN] = (String)dbLoad.get(DB_STATUS);
            vpUsedDBLoads.add(mcKey);
          }
          else if (mpCurrentDBLoads.containsKey(bcrData))
          {
            Map dbLoad = mpCurrentDBLoads.get(bcrData);
            String vsMCKey = (String)dbLoad.get(DB_MCKEY);
            if (vsMCKey.equals(mcKey))
            {
              statusEntry[WRX_STATUS_COLUMN] = (String)dbLoad.get(DB_STATUS);
              vpUsedDBLoads.add(bcrData);
            }
          }
          else
          {
            statusEntry[WRX_STATUS_COLUMN] = DISPLAY_NONE;
          }
          statusEntry[SRC_STATUS_COLUMN] = transportType;
          statusEntry[FROM_COLUMN] = src;
          statusEntry[DEST_COLUMN] = dst;
          statusEntry[LOAD_SIZE_COLUMN] = loadSize;
          statusEntry[TIME_COLUMN] = trackingUpdateTime;
          statusEntry[MACHINEID_COLUMN] = machineId;
          if (msGraphicFilter.length() == 0 || keyId.equals(msGraphicFilter))
          {
            dataList.add(statusEntry);
          }
          mpAllTrackingData.add(statusEntry);
        }
      }
    }
    else
    {
      removeDBOnlyLoads();
    }

    if (!mpCurrentDBLoads.isEmpty())
    {
      for (String vsLoadID : mpCurrentDBLoads.keySet())
      {
        if (!vpUsedDBLoads.contains(vsLoadID))
        {
          addDBEntryToDataList(vsLoadID);
        }
      }
    }
    
    // Update the table
    fireTableDataChanged();
    
    // Try to remember the last selection
    if (vsLastMCKey != null)
    {
      for (int i = 0; i < dataList.size(); i++)
      {
        if (vsLastMCKey.equals((dataList.get(i))[MCKEY_COLUMN]))
        {
          dataTable.addRowSelectionInterval(i,i);
        }
      }
    }
  }

  private void removeDBOnlyLoads()
  {
    int i = 0;
    while (i < dataList.size())
    {
      String[] vasStatus = dataList.get(i);
      if (vasStatus[SRC_STATUS_COLUMN].equals(DISPLAY_NONE))
      {
        dataList.remove(i);
        mpDisplayedLoads.remove(vasStatus[MCKEY_COLUMN]);
      }
      else
      {
        i++;
      }
    }
  }
  
  /*--------------------------------------------------------------------------*/
  private void addDBEntryToDataList(String isLoadID)
  {
    try
    {
      Map dbLoad = mpCurrentDBLoads.get(isLoadID);
      String[] statusEntry = new String[STATUS_COLUMN_NAMES_NORMAL.length + 1];
      int index = dataList.size();
      String s = "" + (index + 1);
      statusEntry[VSS_COLUMN] = s;
      statusEntry[EQUIP_COLUMN] = (String)dbLoad.get(DB_DEVICE); //keyId;
      statusEntry[MCKEY_COLUMN] = (String)dbLoad.get(DB_MCKEY);
      statusEntry[BAR_CODE_COLUMN] = (String)dbLoad.get(DB_BCRDATA);
      statusEntry[WRX_STATUS_COLUMN] = (String)dbLoad.get(DB_STATUS);
      statusEntry[SRC_STATUS_COLUMN] = DISPLAY_NONE; //transportType;
      statusEntry[FROM_COLUMN] = (String)dbLoad.get(DB_FROM); //src;
      statusEntry[DEST_COLUMN] = (String)dbLoad.get(DB_TO); //dst;
      statusEntry[LOAD_SIZE_COLUMN] = (String)dbLoad.get(DB_SIZE); //loadSize;
      statusEntry[TIME_COLUMN] = (String)dbLoad.get(DB_TIME); //trackingUpdateTime;
      statusEntry[MACHINEID_COLUMN] = ""; //machineId;
      // If we're filtering, skip DB-only loads
      if (msGraphicFilter.length() == 0)
      {
        dataList.add(statusEntry);
      }
      mpDisplayedLoads.add(isLoadID);
    }
    catch (Exception e)
    {
      logger.logException(e, "loadId " + isLoadID);
    }
  }

  /**
   * Clear the table
   */
  public void clearTable()
  {
    dataList.clear();
    mpAllTrackingData.clear();
    mpDisplayedLoads.clear();
    fireTableDataChanged();
  }
  
  /**
   * Get all of the displayed tracking data
   * 
   * @return
   */
  public List<String[]> getAllTrackingData()
  {
    return mpAllTrackingData;
  }
  
  /**
   * Set the filter
   * 
   * @param isFilter
   */
  public void setGraphicFilter(String isFilter)
  {
    msGraphicFilter = isFilter;
    processStatusChanges(mpLastStatusUpdate);
    fireTableDataChanged();
  }
}
