package com.daifukuamerica.wrxj.archive.tranhist;

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

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.archive.Archive;
import com.daifukuamerica.wrxj.archive.ArchiveException;
import com.daifukuamerica.wrxj.archive.ExportFormat;
import com.daifukuamerica.wrxj.archive.Exporter;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCDataEnum;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistory;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Description:<BR>
 *   Class to handle Transaction History specific export operations.
 *
 * @author       A.D.
 * @version      1.0
 */
public class TransactionHistoryArchiver implements Archive
{
  public static final int DEFAULT_TRANSACTION_FILE_COUNT = 30;
  public static final int TRANSACTIONS_PER_FILE = 10000;
  
  private boolean mzDeleteData = true;
  private int   mnArchiveFileCount = DEFAULT_TRANSACTION_FILE_COUNT;
  private int   mnExportFormat = -1;
  private int   mnTranCategory = SKDCConstants.ALL_INT;
  private int[] mpCategories;
  private Date  mpBeginDate;
  private Date  mpEndDate;
  private String msBaseArchivePath = "";
  private TransactionHistory mpTranHist;
  private TransactionHistoryData mpTranHistData;

  public TransactionHistoryArchiver()
  {
    super();
    mpCategories = DBTrans.getIntegerList(TransactionHistoryData.TRANCATEGORY_NAME);
    mpTranHist = Factory.create(TransactionHistory.class);
    mpTranHistData = Factory.create(TransactionHistoryData.class);

    msBaseArchivePath = Application.getString(ControllerConsts.ROOT_PATH_PROPERTY);
    msBaseArchivePath += (File.separator + "logs" + File.separator + "TranArchive");
  }

 /**
  * {@inheritDoc} For this implementation this is the Transaction category.
  * @param selectFilter the search filter. The transaction category.
  */
  public void setSelectionFilter(Object ipSelectionFilter)
  {
    mnTranCategory = ((Integer)ipSelectionFilter).intValue();
  }
  
 /**
  * Method to set the data format for this archiver.
  * @param mnExportFormat <code>int</code> containing type of export to do
  *        (eg. XML, HTML etc.)
  */
  public void setExportFormat(int inExportFormat)
  {
    this.mnExportFormat = inExportFormat;
  }

 /**
  * Set beginning date for data to archive.
  * @param mpBeginDate <code>java.util.Date</code> object representing begin date.
  */
  public void setBeginningDate(Date ipBeginDate)
  {
    this.mpBeginDate = ipBeginDate;
  }
  
 /**
  * Set ending date for data to archive.
  * @param mpEndDate <code>java.util.Date</code> object representing end date.
  */
  public void setEndingDate(Date ipEndDate)
  {
    this.mpEndDate = ipEndDate;
  }
 
 /**
  * Method sets option to delete data after archiving.  By default this option
  * is set to true.
  * @param izDeleteData flag to indicate if data deletion should take place.
  */
  public void setDeleteOption(boolean izDeleteData)
  {
    if (mnArchiveFileCount == 0)
      mzDeleteData = true;
    else
      mzDeleteData = izDeleteData;
  }

 /**
  * Method to maintain a fixed count of archive files. <u>The default is 30 files.</u>
  * 
  * @param archiveCount <code>int</code> containing number of files to maintain.
  *        If the current number of archived files exceeds this parameter, the
  *        oldest file is deleted. <b>Note: </b><i>If this parameter is set to 
  *        zero, no files will be saved and the default behaviour of data delete
  *        will be enforced.</i>
  */
  public void setArchivedFileCount(int inArchiveCount)
  {
    mnArchiveFileCount = inArchiveCount;
    if (inArchiveCount == 0) mzDeleteData = true;
  }

 /**
  * {@inheritDoc} If this is not set, the default value is the same as the
  * baseline logger directory.  <b>Note:</b> this must be an absolute path, and
  * must <u>not</u> end in a file path separation character. Archive files will
  * be stored under this log path in a folder called "TranArchive."
  * 
  * @param isLogPath the base path to the output directory.  Sample path settings:<p>
  *        "C:/Daifuku/wrxj",
  *        "C:\\Daifuku\\wrxj"
  *        "\\\\10.16.1.105\\Daifuku"
  */
  public void setArchiveOutputDirectory(String isLogPath)
  {
    msBaseArchivePath = isLogPath + File.separator + "TranArchive";
  }
  
 /**
  * Method to get the oldest date of a record to archive.
  * @return The oldest date of a record that can be archived.
  * @throws ArchiveException if there is a problem determining oldest recorded date.
  */
  public Date getOldestRecordedDate() throws ArchiveException
  {
    Date vpOldestDate;
    
    try
    {
      vpOldestDate = mpTranHist.getOldestDate(true);
      if (vpOldestDate == null)
        throw new ArchiveException("No data to archive...");
    }
    catch(DBException exc)
    {
      throw new ArchiveException("Error determining oldest recorded date of " +
                                 "Transaction History...", exc);
    }

    return(vpOldestDate);
  }

 /**
  * Method to get the last date of a record to archive.
  * @return The last date of a record that can be archived.
  * @throws ArchiveException if there is a problem determining last recorded date,
  *         or if no record exists.
  */
  public Date getLastRecordedDate() throws ArchiveException
  {
    Date vpLastDate;
    
    try
    {
      vpLastDate = mpTranHist.getNewestDate(true);
      if (vpLastDate == null)
        throw new ArchiveException("No data to archive...");
    }
    catch(DBException exc)
    {
      throw new ArchiveException("Error determining last recorded date of " +
                                 "Transaction History...", exc);
    }

    return(vpLastDate);
  }
  
  /**
   * Exports all data from a source between inclusive date range to a file.
   * <i><b>Note:</b> if no date range is given (both arguments are
   * <code>null</code>) all data from the source is exported.</i>  This method
   * should be called from within a transaction.
   */
  public void exportData() throws ArchiveException
  {
    try
    {
      if (mnTranCategory == SKDCConstants.ALL_INT)
      {
        for (int idx = 0; idx < mpCategories.length; idx++)
        {
          exportAndDeleteData(mpCategories[idx]);
        }
      }
      else
      {
        exportAndDeleteData(mnTranCategory);
      }

      if (mnArchiveFileCount != 0)
        enforceFileCount();
    }
    catch (DBException exc)
    {
      throw new ArchiveException("Error retrieving data from database.", exc);
    }
    catch (ClassCastException cce)
    {
      throw new ArchiveException("Non-integer object passed!", cce);
    }
  }
  
/*===========================================================================
                       All private methods go here.
  ===========================================================================*/
  /**
   * Export and delete Transaction History data
   * 
   * @param inTranCategory
   * @throws DBException
   */
  private void exportAndDeleteData(int inTranCategory) throws DBException
  {
    // Do not export to files if the file count is 0
    if (mnArchiveFileCount != 0)
    {
      // Set up the key
      mpTranHistData.clear();
      mpTranHistData.setKey(TransactionHistoryData.TRANCATEGORY_NAME,
          inTranCategory);
      
      if (mpBeginDate.compareTo(mpEndDate) == 0)
        mpTranHistData.setKey(TransactionHistoryData.TRANSDATETIME_NAME,
            mpBeginDate);
      else mpTranHistData.setBetweenKey(
          TransactionHistoryData.TRANSDATETIME_NAME, mpBeginDate, mpEndDate);
      
      mpTranHistData.addOrderByColumn(TransactionHistoryData.TRANSDATETIME_NAME);
      mpTranHistData.addOrderByColumn(AbstractSKDCDataEnum.ID.getName());
      
      // Read and archive the data
      mpTranHist.initializeLargeRecordList(TRANSACTIONS_PER_FILE, mpTranHistData);
      List<Map> vpArchiveData = mpTranHist.fetchNextLargeRecordListEntries();
      while (vpArchiveData.size() > 0)
      {
        doExport(inTranCategory, vpArchiveData);
        vpArchiveData = mpTranHist.fetchNextLargeRecordListEntries();
      }
    }
    
    // Delete data only if specified (default = true).
    if (mzDeleteData)
    {
      try
      {
        deleteDBData(inTranCategory, mpBeginDate, mpEndDate);
      }
      catch (NoSuchElementException nsee)
      {
        // This is okay
      }
    }
  }

  /**
   * Export the data
   * 
   * @param inTranCategory
   * @param ipDataList
   */
  private void doExport(int inTranCategory, List<Map> ipDataList)
  {
                                       // Get the correct format factory.
    TransactionHistoryFormatFactory vpFmtFactory;
    if (mnExportFormat == -1)          // Data format is unspecified. Default to XML.
      vpFmtFactory = new TransactionHistoryFormatFactory();
    else
      vpFmtFactory = new TransactionHistoryFormatFactory(mnExportFormat);

    ExportFormat vpDataFormat = vpFmtFactory.getExportFactory();
                                       // Get the correct category data writer
                                       // from the format factory.
    Exporter vpDataWriter = vpDataFormat.getDataWriter(Integer.valueOf(inTranCategory));
    vpDataWriter.writeData(ipDataList, msBaseArchivePath);
  }
  
  /**
   * Delete the data
   * 
   * @param inTranCategory
   * @param ipBeginDate
   * @param ipEndingDate
   * @throws DBException
   */
  private void deleteDBData(int inTranCategory, Date ipBeginDate, Date ipEndingDate)
          throws DBException
  {
    mpTranHistData.clear();
    mpTranHistData.setKey(TransactionHistoryData.TRANCATEGORY_NAME, Integer.valueOf(inTranCategory));
    mpTranHistData.setBetweenKey(TransactionHistoryData.TRANSDATETIME_NAME, ipBeginDate, ipEndingDate);
    mpTranHist.deleteElement(mpTranHistData);
  }

 /**
  * Method maintains a specified count of archived files.
  */
  private void enforceFileCount()
  {
    File[] vapArchivedFiles = SKDCUtility.getFilesSortedByModifyDate(
        msBaseArchivePath, true);
    int vnTotalFileCount = vapArchivedFiles.length;
                                       // If the total count of archived files
                                       // is more than the maintainable count,
                                       // we have something to delete!
    if (vnTotalFileCount > mnArchiveFileCount)
    {
      for (int vnFileIdx = mnArchiveFileCount; vnFileIdx < vnTotalFileCount; vnFileIdx++)
      {
        vapArchivedFiles[vnFileIdx].delete();
      }
    }
  }
}
