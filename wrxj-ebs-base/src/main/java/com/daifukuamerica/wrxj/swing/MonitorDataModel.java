package com.daifukuamerica.wrxj.swing;
/*
 * Created on Jan 9, 2004
 * Title:        WRx-J
 * Description:
 * Copyright:    Copyright (c) 2004
 * Company:      SKDC Corp.
 * @author       Stephen Kendorski
 */

import javax.swing.table.TableModel;

public interface MonitorDataModel extends TableModel
{
  /**
   * Perform any final clean-up before closing.
   */
  public void cleanUpOnClose();
  /**
   * Fetch an array containing the map of displayed columns to the data
   * fields in a Model.
   *
   * @return the ordered array
   */
  public int[] getColumnToFieldMap();
  /**
   * Specify an array containing the map of displayed columns to the data
   * fields in a Model.
   *
   * @param dataMap the ordered array
   */
  public void setColumnToFieldMap(int[] dataMap);
}