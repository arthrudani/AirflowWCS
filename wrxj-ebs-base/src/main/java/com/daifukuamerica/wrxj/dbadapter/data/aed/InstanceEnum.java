/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright (c) 2017 Daifuku North America Holding Company. All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.dbadapter.data.aed;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 * Enum class representing the AES_SYS_INSTANCES table fields.
 */
public enum InstanceEnum implements TableEnum
{
  ID("ID"),
  COMPUTER_NAME("COMPUTER_NAME"),
  DB_CONN_STR_PARAMS("DB_CONN_STR_PARAMS"),
  DB_NAME("DB_NAME"),
  DB_PASSWORD("DB_PASSWORD"),
  DB_SERVER("DB_SERVER"),
  DB_TYPE("DB_TYPE"),
  DB_USER("DB_USER"),
  IDENTITY_NAME("IDENTITY_NAME"),
  INTERNAL_PORT("INTERNAL_PORT"),
  IP_ADDRESS("IP_ADDRESS"),
  LOG_PORT("LOG_PORT"),
  PORT("PORT"),
  PROCESS_ID("PROCESS_ID"),
  PROCESS_NAME("PROCESS_NAME"),
  PRODUCT_ID("PRODUCT_ID"),
  RUN_PATH("RUN_PATH"),
  STATS_MEASURE_INTERVAL("STATS_MEASURE_INTERVAL"),
  STATS_MEASURE_READING_DELAY("STATS_MEASURE_READING_DELAY"),
  STATS_MEASURE_TIME("STATS_MEASURE_TIME"),
  THREAD_SLEEP_TIME("THREAD_SLEEP_TIME"),
  TIME_REGISTERED("TIME_REGISTERED"),
  TOTAL_DISK_SPACE("TOTAL_DISK_SPACE"),
  TOTAL_MEMORY("TOTAL_MEMORY"),
  USER_NAME("USER_NAME"),
  VERSION("VERSION");

  private String msColumnName;

  InstanceEnum(String isColumnName)
  {
    msColumnName = isColumnName;
  }

  @Override
  public String getName()
  {
    return(msColumnName);
  }
}
