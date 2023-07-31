/* ***************************************************************************
  Copyright (c) 2018 Daifuku North America Holding Company. All Rights Reserved.

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
 * Enum class representing the EQUIPMENTMONITORSTATUS table fields.
 */
public enum EquipmentMonitorStatusEnum implements TableEnum
{
  ALTGRAPHICID("SEMALTGRAPHICID"),
  BEHAVIOR("SEMBEHAVIOR"),
  CANTRACK("IEMCANTRACK"),
  DESCRIPTION("SEMDESCRIPTION"),
  DEVICEID("SEMDEVICEID"),
  ERRORCODE("SEMERRORCODE"),
  ERRORSET("SEMERRORSET"),
  ERRORTEXT("SEMERRORTEXT"),
  GRAPHICID("SEMGRAPHICID"),
  MCCONTROLLER("SEMMCCONTROLLER"),
  MCID("SEMMCID"),
  MOSCONTROLLER("SEMMOSCONTROLLER"),
  MOSID("SEMMOSID"),
  STATIONID("SEMSTATIONID"),
  STATUSID("SEMSTATUSID"),
  STATUSTEXT1("SEMSTATUSTEXT1"),
  STATUSTEXT2("SEMSTATUSTEXT2");

  private String msColumnName;

  EquipmentMonitorStatusEnum(String isColumnName)
  {
    msColumnName = isColumnName;
  }

  @Override
  public String getName()
  {
    return(msColumnName);
  }
}
