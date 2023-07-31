package com.daifukuamerica.wrxj.dbadapter.data.aed;

/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright (c) 2017 Wynright Corporation.  All Rights Reserved.

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
 * Enum class representing the AES_SYS_INSTANCE_COMMUNICATIONS table fields.
 */
public enum InstanceCommunicationsEnum implements TableEnum
{
  COMMUNICATION_TYPE_ID("COMMUNICATION_TYPE_ID"),
  RECEIVER_ID("RECEIVER_ID"),
  SENDER_COMPONENT_ID("SENDER_COMPONENT_ID"),
  SENDER_ID("SENDER_ID");

  private String msColumnName;

  InstanceCommunicationsEnum(String isColumnName)
  {
    msColumnName = isColumnName;
  }

  @Override
  public String getName()
  {
    return(msColumnName);
  }
}
