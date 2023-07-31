package com.daifukuamerica.wrxj.archive.tranhist.xml;

import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.log.Logger;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * Description:<BR>
 *   Class containing miscellaneous helper methods for XML processing using
 *   JDOM.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 7-Jul-03<BR>
 *     Copyright (c) 2003<BR>
 *     Company:  SKDC Corporation
 */
public class JDOMHelper
{
  private static final String FILE_SUFFIX = ".xml";
  
  private Logger logger;
  protected TransactionHistoryData tndata = Factory.create(TransactionHistoryData.class);

  public JDOMHelper()
  {
    logger = Logger.getLogger("XMLArchive");
  }

  /**
   * Create the output file
   * 
   * @param isDocumentName
   * @param ipDocRoot
   * @param isOutputDir
   */
  protected void createFile(String isDocumentName, Document ipDocRoot, String isOutputDir)
  {
    Format outputFormatter = Format.getPrettyFormat();
    outputFormatter.setIndent("  ");
    XMLOutputter xmlout = new XMLOutputter(outputFormatter);
    
    File dirName = new File(isOutputDir);
    if (!dirName.isDirectory())
    {
      if (!dirName.mkdir())
      {
        logger.logError("Directory " + dirName + " not created for tran. history!");
        return;
      }
    }

    try
    {
      String vsOutputFile = getFileName(isOutputDir, isDocumentName);
      logger.logOperation("Exporting data to " + vsOutputFile);
      xmlout.output(ipDocRoot, new BufferedOutputStream(new FileOutputStream(vsOutputFile)));
    }
    catch(IOException e)
    {
      logger.logError(e.getMessage());
    }
  }
  
  /**
   * Get a unique file name so we don't overwrite an old one.
   * 
   * @param isDir
   * @param isDoc
   * @return
   */
  protected String getFileName(String isDir, String isDoc)
  {
    int vnCounter = 1;
    
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MMM-dd_HH.mm.ss");
    String vsFilePrefix = isDir + File.separator + isDoc + "."
        + df.format(new Date());
    
    String vsOutputFile;
    do
    {
      vsOutputFile = vsFilePrefix + "-" + vnCounter++ + FILE_SUFFIX;
    }
    while (new File(vsOutputFile).exists());
      
    return vsOutputFile;
  }

  /**
   * Build the Transaction History root element
   * 
   * @param rootName
   * @param tranCategory
   * @return
   */
  protected Element buildTranRootElement(String rootName, int tranCategory)
  {
    Element rootElem = new Element(rootName);
    try
    {
      String categoryValue = DBTrans.getStringValue(TransactionHistoryData.TRANCATEGORY_NAME,
                                                    tranCategory);
      rootElem.setAttribute(TransactionHistoryData.TRANCATEGORY_NAME, categoryValue);
    }
    catch(NoSuchFieldException exc)
    {
      logMessage(exc.getMessage());
      return(null);
    }

    return(rootElem);
  }

  protected Element buildTranTypeElement(Map columnMap)
  {
    Element tranType = new Element(TransactionHistoryData.TRANTYPE_NAME);
    int iTranType = DBHelper.getIntegerField(columnMap, TransactionHistoryData.TRANTYPE_NAME);
    try
    {
      String sTranType = DBTrans.getStringValue(TransactionHistoryData.TRANTYPE_NAME,
                                                iTranType);
      tranType.setAttribute("action", sTranType);
    }
    catch(NoSuchFieldException exc)
    {
      logMessage(exc.getMessage());
      return(null);
    }

    return(tranType);
  }

  protected Element buildTranDateElement(Map columnMap)
  {
    Element dateTime = new Element(TransactionHistoryData.TRANSDATETIME_NAME);
    Date dDateTime = DBHelper.getDateField(columnMap,
                                      TransactionHistoryData.TRANSDATETIME_NAME);
    dateTime.setText(dDateTime.toString());

    return(dateTime);
  }

  protected void logMessage(String msg)
  {
    if (logger != null)
      logger.logError("Invalid Translation:: " + msg);
    else
      System.err.println("Invalid Translation:: " + msg);
  }
}
