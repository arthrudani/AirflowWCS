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
 * Description:<BR>
 *   Interface defining factory for formatters.
 *
 * @author       A.D.
 * @version      1.0
 */
public interface FormatFactory
{
 /**
  *  Method to set the export type.
  */
  public void setExportType(int exportType);
 /**
  *  Returns an export factory based on export type specified using
  *  setExportType() method.
  *
  *  @return object implementing the ExportFormat interface.
  */
  public ExportFormat getExportFactory();
}
