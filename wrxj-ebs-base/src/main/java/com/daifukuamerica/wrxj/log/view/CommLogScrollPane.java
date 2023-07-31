package com.daifukuamerica.wrxj.log.view;

/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      SKDC Corp.
 * @author Stephen Kendorski
 * @version 1.0
 */

import com.daifukuamerica.wrxj.log.LogConsts;

public class CommLogScrollPane  extends AbstractLogScrollPane
{
  private static final long serialVersionUID = 0L;

  private static final String[] COM_LOG_COLUMN_NAMES = {"Number",
                                   "Timestamp",
                                   "Data Count",
                                   "Transmitted Data",
                                   "Received Data"};
  private static final String[] COM_LOG_COLUMN_WIDTHS = {" Number ",
                                     "      Timestamp      ",
                                   " Data Count ",
                                   "                       Transmitted Data                       ",
                                   "                        Received Data                                          "};

  public CommLogScrollPane()
  {
    super();
    columnNames = COM_LOG_COLUMN_NAMES;
    columnNamesDefaultWidth = COM_LOG_COLUMN_WIDTHS;
    dataMap = LogConsts.COM_LOG_COLUMN_FIELDS;
  }
}