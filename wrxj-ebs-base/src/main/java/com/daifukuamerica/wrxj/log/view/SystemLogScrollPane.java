package com.daifukuamerica.wrxj.log.view;
/*
 * Created on Jan 6, 2004
 * @author Stephen Kendorski
 *
 */
import com.daifukuamerica.wrxj.log.LogConsts;

/**
 * @author Stephen Kendorski
 * @version 1.0
 */
public class SystemLogScrollPane extends AbstractLogScrollPane
{
  private static final long serialVersionUID = 0L;

  private static final String[] SYS_LOG_COLUMN_NAMES = {"Number",
                                   "Timestamp",
                                   "Logger's Name",
                                   "Message/Event Type",
                                   "Description"};
  private static final String[] SYS_LOG_COLUMN_WIDTHS  = {" Number ",
                                     "      Timestamp      ",
                                     "          Logger's Name          ",
                                     " Message/Event Type ",
                                     "                                  Description                                               "};
  public SystemLogScrollPane()
  {
    super();
    columnNames = SYS_LOG_COLUMN_NAMES;
    columnNamesDefaultWidth = SYS_LOG_COLUMN_WIDTHS;
    dataMap = LogConsts.SYS_LOG_COLUMN_FIELDS;
  }
}
