package com.daifukuamerica.wrxj.swingui.developer;

/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$
  
  Copyright (c) 2015 Wynright Corporation.  All Rights Reserved.
  
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/

import java.util.Comparator;

/**
 * Comparator for sorting db column names while ignoring the Hungarian notation
 * @author mandrus
 */
public class DbColumnComparator implements Comparator<String>
{
  boolean mzUsesHungarianNotation = false;
  public DbColumnComparator(boolean izUsesHungarianNotation)
  {
    mzUsesHungarianNotation = izUsesHungarianNotation;
  }

  /**
   * ID column first, followed by other in alphabetical order ignoring 
   * Hungarian notation, if used.
   */
  @Override
  public int compare(String o1, String o2)
  {
    if (o1.equalsIgnoreCase(DBTableInfo.ID_NAME)) return -1;
    if (o2.equalsIgnoreCase(DBTableInfo.ID_NAME)) return 1;
    if (mzUsesHungarianNotation)
      return o1.substring(1).compareTo(o2.substring(1));
    else
      return o1.compareTo(o2);
  }
}
