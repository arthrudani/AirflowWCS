package com.daifukuamerica.wrxj.dbadapter.data.cmd;

/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright (c) 2016 Wynright Corporation.  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 * Enum class representing the CME_CFG_COMM_DATA_TABLE_SETTINGS table fields.
 */
public enum CfgCommDataTableSettingsEnum implements TableEnum
{
  DATA_TIMEOUT_INTERVAL_IN_MIN("DATA_TIMEOUT_INTERVAL_IN_MIN"),
  BATCH_MSG_DETAIL_TABLE("BATCH_MSG_DETAIL_TABLE"),
  BATCH_MSG_HEADER_TABLE("BATCH_MSG_HEADER_TABLE"),
  SINGLE_MSG_TABLE("SINGLE_MSG_TABLE"),
  DIRECTION_ID("DIRECTION_ID"),
  CLIENT_CONNECTION_NAME("CLIENT_CONNECTION_NAME"),
  COMMUNICATION_ID("COMMUNICATION_ID"),
  TRANSACTION_SEQ_NAME("TRANSACTION_SEQ_NAME");

  private String msColumnName;

  CfgCommDataTableSettingsEnum(String isColumnName)
  {
    msColumnName = isColumnName;
  }

  @Override
  public String getName()
  {
    return(msColumnName);
  }
}
