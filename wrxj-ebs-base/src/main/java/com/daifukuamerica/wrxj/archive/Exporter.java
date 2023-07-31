package com.daifukuamerica.wrxj.archive;

/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright (c) 2004-2008 Daifuku America Corporation  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/

import java.util.List;

/**
 * Description:<BR>
 *   Interface defining contract for all Exporter classes.
 *
 * @author       A.D.
 * @version      1.0
 */
public interface Exporter
{
 /**
  *  Writes data in a <code>List</code> to a file.
  *  @param ipList <code>List</code> containing data to write.
  *  @param isArchivePath <code>List</code> the out
  */
  public void writeData(List ipList, String isArchivePath);
}
