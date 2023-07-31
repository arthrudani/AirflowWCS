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

import java.util.Date;

/**
 *  Description:<BR>
 *    Interface class for system wide data archivers.  This interface should be
 *    adhered to by any object trying to archive reports, transaction history,
 *    system logs, etc.
 *    
 *  @author       A.D.
 *  @version      1.0
 */
public interface Archive 
{
 /**
  * Sets the search filter for this archiver.
  * @param ipSelectFilter the search filter. This should be typecast as necessary
  *        to an implementations needs.
  */
  public void setSelectionFilter(Object ipSelectFilter);

 /**
  * Set beginning date for data to archive.  The implementation of this interface
  * should not demand that this value is set.
  * @param ipBeginDate <code>java.util.Date</code> object representing end date.
  */
  public void setBeginningDate(Date ipBeginDate);
  
 /**
  * Set ending date for data to archive.  The implementation of this interface
  * should not demand that this value is set.
  * @param ipEndData <code>java.util.Date</code> object representing end date.
  */
  public void setEndingDate(Date ipEndData);

 /**
  * Method to get the oldest date of a record to archive.
  * @return The oldest date of a record that can be archived.
  * @throws ArchiveException if there is a problem determining oldest recorded date.
  */
  public Date getOldestRecordedDate() throws ArchiveException;
  
 /**
  * Method to get the last date of a record to archive.
  * @return The last date of a record that can be archived.
  * @throws ArchiveException if there is a problem determining last recorded date.
  */
  public Date getLastRecordedDate() throws ArchiveException;
          
 /**
  * Method sets option to delete data in database after archiving.  By default
  * this option is set to true.
  * @param izDeleteData flag to indicate if data deletion should take place.
  */
  public void setDeleteOption(boolean izDeleteData);

 /**
  * Method to maintain a fixed count of archive files.
  * @param inExportFormat <code>int</code> containing type of export to do
  *        (eg. XML, HTML etc.)
  */
  public void setExportFormat(int inExportFormat);
  
 /**
  * Exports all data from a source to a file.  The format of the export file is
  * determined by the specific implementation of this interface.  <i><b>Note:</b>
  * if no date range is given (both arguments are <code>null</code>) all data
  * from the source is exported.</i>
  * @throws ArchiveException if there is a data export problem.
  */
  public void exportData() throws ArchiveException;

 /**
  * Method to maintain a fixed count of archive files.
  * @param archiveCount <code>int</code> containing number of files to maintain.
  *        If the current number of archived files exceeds this parameter, the
  *        oldest file is deleted.
  */
  public void setArchivedFileCount(int inArchiveCount);
  
 /**
  * Sets the base output directory for file archives.
  * @param isLogPath string containing base log path.
  */  
  public void setArchiveOutputDirectory(String isLogPath);
}
