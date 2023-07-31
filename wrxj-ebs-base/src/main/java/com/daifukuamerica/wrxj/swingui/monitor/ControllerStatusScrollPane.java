package com.daifukuamerica.wrxj.swingui.monitor;

import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.observer.ObservableControllerImpl;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusEventDataFormat;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusInfo;
import com.daifukuamerica.wrxj.swing.AbstractMonitorScrollPane;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;

/**
 * A Table view for displaying status of the Controllers in the application.
 *
 * @author Stephen Kendorski
 */
public class ControllerStatusScrollPane extends AbstractMonitorScrollPane
{

  private static final long serialVersionUID = 0L;
  
  private Color notOnColor = Color.yellow;

  private static final int[] STATUS_COLUMN_FIELDS = {0, 1, 2, 3, 4, 5};

  private static final String[] STATUS_COLUMN_NAMES = 
  { "   ",
    "Controller",
    "Status",
    "Detail Status",
    "Last Update",
    "Heartbeat"
  };
  
  private String[] STATUS_COLUMN_WIDTHS = 
  { "   ",
    "                   Controller                   ",
    "        Status        ",
    "                   Detail Status                   ",
    "          Last Update          ",
    "          Heartbeat          "
  };

  public ControllerStatusScrollPane()
  {
    super();
    columnNames = STATUS_COLUMN_NAMES;
    columnNamesDefaultWidth = STATUS_COLUMN_WIDTHS;
    dataMap = STATUS_COLUMN_FIELDS;
  }

  StatusEventDataFormat mpSEDF = new StatusEventDataFormat(getClass().getSimpleName());

  /**
   * This method is called whenever a Controller Status Event is processed by
   * the 
   * {@link com.daifukuamerica.wrxj.common.device.monitor.SystemHealthMonitor 
   * SystemHealthMonitor}
   * {@link com.daifukuamerica.wrxj.common.controller.Controller Controller}.
   *
   * @param o the observable object
   * @param arg an argument
   */
  public void update(Observable o, Object arg)
  {
    int initialRowCount = 0;
    try
    {
      initialRowCount = dataTable.getRowCount();
    }
    catch (Exception e)
    {
      return;
    }
    ObservableControllerImpl observableImpl = (ObservableControllerImpl)o;
    String sText = observableImpl.getStringData();
    mpSEDF.setMessage(sText);

    try
    {
      switch (mpSEDF.getType())
      {
        case ControllerConsts.CONTROLLER_STATUS:
          processStatusChanges(mpSEDF.getStatusList());
          break;
        case ControllerConsts.UPDATE_STATUS:
          processStatusUpdates(mpSEDF.getStatusList());
          break;
      }
    }
    catch (Exception e)
    {
      logger.logException(e, "\n\n" + sText + "\n\n");
    }
    int[] selectedRows = dataTable.getSelectedRows();
    fireTableDataChanged();
    int currentRowCount = dataTable.getRowCount();
    if (initialRowCount == currentRowCount)
    {
      int selectedRowCount = selectedRows.length;
      for (int i = 0; i < selectedRowCount; i++)
      {
        int row = selectedRows[i];
        dataTable.addRowSelectionInterval(row, row);
      }
    }
  }

  private void processStatusChanges(List<StatusInfo> ipStatusList)
  {
    for (StatusInfo s : ipStatusList)
    {
      int index = -1;
      String[] statusEntry = null;
      
      String controllerId = s.getControlName();
      String controllerStatusType = s.getControlType();
      String controllerStatus = s.getControlStatus();
      String controllerStatusUpdateTime = s.getControlTime();
      
      // Check if we only want to display statuses of Controllers in our local
      // application/client (and not all controllers in the entire distributed
      // system).
      if ((controllerId.indexOf(":") != -1) &&
          (controllerId.indexOf(groupName) == -1))
      {
        continue;
      }
      boolean found = false;
      for (int i = 0; i < dataList.size(); i++)
      {
        statusEntry = dataList.get(i);
        if (controllerId.equals(statusEntry[1]))
        {
          found = true;
          index = i;
          break;
        }
      }
      if (!found)
      {
        statusEntry = new String[STATUS_COLUMN_NAMES.length];
        index = dataList.size();
        String ss = Integer.toString(index + 1);
        statusEntry[0] = ss;
        statusEntry[1] = controllerId;
        // Set all fields to something because as soon as we add the entry to 
        // the list the screen can get it for an update at any time.
        Arrays.fill(statusEntry, 2, statusEntry.length, "");
        dataList.add(statusEntry);
      }
      // Resolve warning.
      assert(statusEntry != null);
      // If a Frame shuts down remove it from the display.
      if ((controllerStatus.equals(ControllerConsts.STATUS_TEXT_SHUTDOWN)) &&
          (controllerId.indexOf("SystemGateway") == 0))
      {
        dataList.remove(index);
        addSelectedKey(index, null);
        continue;
      }
      switch (controllerStatusType.charAt(0))
      {
        case ControllerConsts.OPERATING_STATUS:
          statusEntry[2] = controllerStatus;
          statusEntry[4] = controllerStatusUpdateTime;
          if (controllerStatus.equals(ControllerConsts.STATUS_TEXT_RUNNING))
          {
            addSelectedKey(index, Color.white);
          }
          else
          {
            addSelectedKey(index, notOnColor);
          }
          break;
        case ControllerConsts.DETAILED_STATUS:
          statusEntry[3] = controllerStatus;
          statusEntry[4] = controllerStatusUpdateTime;
          break;
        case ControllerConsts.HEARTBEAT_STATUS:
          statusEntry[5] = controllerStatus;
          break;
      }
    }
  }

  private void processStatusUpdates(List<StatusInfo> ipStatusList)
  {
    for (StatusInfo s : ipStatusList)
    {
      int index = -1;
      String[] statusEntry = null;
      String controllerId = s.getControlUName();
      String controllerStatus = s.getControlUStatus();
      String controllerStatusDetail = s.getControlUDetail();
      String controllerHeartbeat = s.getControlUHBeat();
      String controllerStatusUpdateTime = s.getControlUTime();
      
      if ((controllerId.indexOf(":") != -1) &&
          (controllerId.indexOf(groupName) == -1))
      {
        continue;
      }
      boolean found = false;
      for (int i = 0; i < dataList.size(); i++)
      {
        statusEntry = dataList.get(i);
        if (controllerId.equals(statusEntry[1]))
        {
          found = true;
          index = i;
          break;
        }
      }
      if (!found)
      {
        statusEntry = new String[STATUS_COLUMN_NAMES.length];
        index = dataList.size();
        String ss = "" + (index + 1);
        statusEntry[0] = ss;
        statusEntry[1] = controllerId;
        int viStatusEntryLength = statusEntry.length;
        for (int i = 2; i < viStatusEntryLength; i++)
        {
          // Set all fields to something because as soon as we add the entry
          // to the list the screen can get it for an update at any time.
          statusEntry[i] = "";
        }
        dataList.add(statusEntry);
      }
      assert(statusEntry != null);
      // If a Frame shuts down remove it from the display.
      if ((controllerStatus.equals(ControllerConsts.STATUS_TEXT_SHUTDOWN)) &&
          (controllerId.indexOf("SystemGateway") == 0))
      {
        dataList.remove(index);
        addSelectedKey(index, null);
        continue;
      }
      statusEntry[2] = controllerStatus;
      if (controllerStatus.equals(ControllerConsts.STATUS_TEXT_RUNNING))
      {
        addSelectedKey(index, Color.white);
      }
      else
      {
        addSelectedKey(index, notOnColor);
      }
      statusEntry[3] = controllerStatusDetail;
      statusEntry[4] = controllerStatusUpdateTime;
      statusEntry[5] = controllerHeartbeat;
    }
  }

  /**
   * Returns name of controller at row.
   * 
   * <p><b>Details:</b> This method returns the name of the controller 
   * represented by the given row.  The first row is indexed with zero.  If the 
   * provided row number is out of range, an exception will be thrown.</p>
   * 
   * @param inRow the row index
   * @return the controller name
   */
  String getControllerNameAtRow(int inRow)
  {
    String[] vasRow = dataList.get(inRow);
    String vsName = vasRow[1];
    return vsName;
  }
  
}

