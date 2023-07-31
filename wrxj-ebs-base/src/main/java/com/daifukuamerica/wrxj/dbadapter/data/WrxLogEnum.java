/* ***************************************************************************
  Copyright (c) 2019 Daifuku North America Holding Company. All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 * Enum class representing the WRXLOG table fields.
 */
public enum WrxLogEnum implements TableEnum
{
  AREA("AREA"),
  DATE_TIME("DATE_TIME"),
  DESCRIPTION("DESCRIPTION"),
  IDENTITY_NAME("IDENTITY_NAME"),
  INFOWARNFATAL("INFOWARNFATAL"),
  POSITION("POSITION"),
  SOURCE("SOURCE"),
  SUBJECT("SUBJECT");

  private String msColumnName;

  WrxLogEnum(String isColumnName)
  {
    msColumnName = isColumnName;
  }

  @Override
  public String getName()
  {
    return(msColumnName);
  }
}
