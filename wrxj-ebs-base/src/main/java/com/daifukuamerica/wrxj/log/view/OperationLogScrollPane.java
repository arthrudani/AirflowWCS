package com.daifukuamerica.wrxj.log.view;

/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

import com.daifukuamerica.wrxj.log.LogConsts;

/**
 * @author Stephen Kendorski
 * @version 1.0
 */
public class OperationLogScrollPane  extends AbstractLogScrollPane
{
  private static final long serialVersionUID = 0L;

  private static final String[] OPR_LOG_COLUMN_NAMES = {"Number",
                                   "Timestamp",
                                   "Logger's Name",
                                   "Type",
                                   "Key",
                                   "Operation Description"};
  private static final String[] OPR_LOG_COLUMN_WIDTHS  = {" Number ",
                                     "      Timestamp      ",
                                     "          Logger's Name          ",
                                     "Operation Type",
                                     "Operation Key",
                                     "                      Operation Description                             "};

  public OperationLogScrollPane()
  {
    super();
    columnNames = OPR_LOG_COLUMN_NAMES;
    columnNamesDefaultWidth = OPR_LOG_COLUMN_WIDTHS;
    dataMap = LogConsts.OPR_LOG_COLUMN_FIELDS;
  }
}