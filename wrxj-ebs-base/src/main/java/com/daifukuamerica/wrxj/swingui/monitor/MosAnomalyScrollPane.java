package com.daifukuamerica.wrxj.swingui.monitor;

import com.daifukuamerica.wrxj.controller.observer.ObservableControllerImpl;
import com.daifukuamerica.wrxj.swing.AbstractMonitorScrollPane;
import com.daifukuamerica.wrxj.time.SkDateTime;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Stephen Kendorski
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MosAnomalyScrollPane extends AbstractMonitorScrollPane
                                          implements Observer

{
  private static final long serialVersionUID = 0L;
  
  private static final int[] STATUS_COLUMN_FIELDS = {0, 1, 2};

  private static final String[] STATUS_COLUMN_NAMES = {
                                   "   ",
                                   "Anomaly Description",
                                   "Last Update"};
  private static final String[] STATUS_COLUMN_WIDTHS = {
                                   "   ",
                                   "                      Anomaly Description                      ",
                                   "            Last Update            "};


  private static final int STATUS_COLUMN_NAMES_LENGTH = STATUS_COLUMN_NAMES.length;
  private SkDateTime dataDateTime = new SkDateTime("HH:mm:ss  EEE  MM/dd/yy");

  MosAnomalyScrollPane()
  {
    super();
    columnNames = STATUS_COLUMN_NAMES;
    columnNamesDefaultWidth = STATUS_COLUMN_WIDTHS;
    dataMap = STATUS_COLUMN_FIELDS;
  }
  public void update(Observable o, Object arg)
  {
    ObservableControllerImpl observableImpl = (ObservableControllerImpl)o;
    String sText = observableImpl.getStringData();
    int iStatus = observableImpl.getIntData();
    boolean addToDisplay = (iStatus == 0);
    String[] statusEntry = null;
    updatedIndex = -1;
    boolean found = false;
    for (int i = 0; i < dataList.size(); i++)
    {
      statusEntry = dataList.get(i);
      if (sText.equals(statusEntry[1]))
      {
        found = true;
        updatedIndex = i;
        break;
      }
    }
    if ((!found) && addToDisplay)
    {
      statusEntry = new String[STATUS_COLUMN_NAMES_LENGTH];
      for (int i = 0; i < STATUS_COLUMN_NAMES_LENGTH; i++)
      {
        statusEntry[i] = "";
      }
      updatedIndex = dataList.size();
      String s = "" + (updatedIndex + 1);
      statusEntry[0] = s;
      statusEntry[1] = sText;
      dataList.add(statusEntry);
      statusEntry[2] = dataDateTime.getCurrentDateTimeAsString();
    }
    else
    {
      if (found && (!addToDisplay))
      {
        dataList.remove(updatedIndex);
        for (int i = 0; i < dataList.size(); i++)
        {
          statusEntry = new String[STATUS_COLUMN_NAMES.length];
          String s = "" + (dataList.size() + 1);
          statusEntry[0] = s;
          updatedIndex = -1;
        }
      }
      else
      {
        if (found && addToDisplay)
        {
          statusEntry[2] = dataDateTime.getCurrentDateTimeAsString();
        }
      }
    }
  }
}
