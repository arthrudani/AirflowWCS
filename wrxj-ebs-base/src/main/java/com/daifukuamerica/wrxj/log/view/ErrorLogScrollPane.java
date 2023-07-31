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

public class ErrorLogScrollPane  extends AbstractLogScrollPane
{
  private static final long serialVersionUID = 0L;

  private static final String[] ERR_LOG_COLUMN_NAMES = {"Number",
                                   "Timestamp",
                                   "Device",
                                   "Error Code",
                                   "Error Description"};
  private static final String[] ERR_LOG_COLUMN_WIDTHS = {" Number ",
                                     "      Timestamp      ",
                                     "                Device                ",
                                    "  Error Code  ",
  "                                         Error Description                                  "};

  public ErrorLogScrollPane()
  {
    super();
    columnNames = ERR_LOG_COLUMN_NAMES;
    columnNamesDefaultWidth = ERR_LOG_COLUMN_WIDTHS;
    dataMap = LogConsts.ERR_LOG_COLUMN_FIELDS;
  }
}