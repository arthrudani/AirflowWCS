package com.daifukuamerica.wrxj.dbadapter.data;
/****************************************************************************
  $Workfile: VehiclePathsDataEnum.java
  $Revision: Baseline

  Copyright 2019 Daifuku America Corporation All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND
  TREATIES. NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED
  COPIED, DISTRIBUTED, REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED,
  EXPANDED, COLLECTED, COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED
  WITHOUT THE PRIOR WRITTEN CONSENT OF Daifuku America Corporation ANY
  USE OR EXPLOITATION OF THIS WORK WITHOUT AUTHORIZATION COULD SUBJECT THE
  PERPETRATOR TO CRIMINAL AND CIVIL LIABILITY.
 ****************************************************************************/
import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 * Enum corresponding to Vehicle Paths table.
 * @author A.D.
 * @since  30-Oct-2019
 */
public enum VehiclePathsEnum implements TableEnum
{
  PATHNUMBER("SPATHNUMBER"),
  FROMSTATION("SFROMSTATION"),
  TOSTATION("STOSTATION");

  private String msMessageName;

  VehiclePathsEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  @Override
  public String getName()
  {
    return(msMessageName);
  }
}
