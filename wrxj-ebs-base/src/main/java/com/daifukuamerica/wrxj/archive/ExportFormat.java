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

/**
 *  Description:<BR>
 *    Interface class for defining writer factory.
 *    
 *  @author       A.D.
 *  @version      1.0
 */
public interface ExportFormat
{
  public static final int XML_FORMAT = -1;
  public static final int HTML_FORMAT = -2;
  public static final int CSV_FORMAT = -3;

 /**
  *  Returns a writer or exporter class based on criteria.
  *  @param criteria <code>Object</code> containing criteria to get writer.
  *
  *  @return object implementing the Exporter interface.
  */
  public Exporter getDataWriter(Object criteria);
}
