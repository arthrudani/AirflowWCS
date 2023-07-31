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

import com.daifukuamerica.wrxj.archive.ExportFormat;
import com.daifukuamerica.wrxj.archive.FormatFactory;
import com.daifukuamerica.wrxj.archive.tranhist.xml.TransactionCategoryXMLFactory;

/**
 * Description:<BR>
 *   Factory for producing different Transaction Formatter factories objects
 *   based on format types selected by user.
 *
 * @author       A.D.
 * @version      1.0
 */
public class TransactionHistoryFormatFactory implements FormatFactory
{
  private int exportType;

 /**
  *  Default constructor.  Creating an object using this constructor
  *  assumes data will be exported in XML format.
  */
  public TransactionHistoryFormatFactory()
  {                                    // Default export type.
    this(ExportFormat.XML_FORMAT);
  }

 /**
  *  Constructor allowing the data export format to be specified.
  *  @param inExportType -- indicates format to use for data export.
  */
  public TransactionHistoryFormatFactory(int inExportType)
  {
    exportType = inExportType;
  }

 /**
  *  Method to set the export type.
  */
  public void setExportType(int inExportType)
  {
    exportType = inExportType;
  }

 /**
  *  Returns an export factory based on export type specified using
  *  setExportType() method.
  *
  *  @return object implementing the ExportFormat interface.
  */
  public ExportFormat getExportFactory()
  {
    ExportFormat expFormat = null;

    switch(exportType)
    {
      case ExportFormat.XML_FORMAT:
        expFormat = new TransactionCategoryXMLFactory();
        break;
    
      case ExportFormat.HTML_FORMAT:
//        expFormat = new TransactionCategoryHTMLFactory();
        break;
    
      case ExportFormat.CSV_FORMAT:
//        expFormat = new TransactionCategoryCSVFactory();
        break;
    
      default:                         // Default to XML format.
        expFormat = new TransactionCategoryXMLFactory();
    }

    return(expFormat);
  }
}
