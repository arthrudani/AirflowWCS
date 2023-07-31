package com.daifukuamerica.wrxj.printer;

import com.daifukuamerica.wms.printer.logging.LabelPrinterLogger;
import com.daifukuamerica.wrxj.log.Logger;

/**
 * Class for implementing label printer logging for Warehouse Rx.
 *
 * <p><b>Details:</b> <code>FadaLabelPrinterLog</code> ties the label printer 
 * logging into the debug log of a WRxJ application.</p>
 */
public class DacLabelPrinterLoggerWRx extends LabelPrinterLogger
{
  static private Logger mpLogger = Logger.getLogger("LabelPrinter");

  /**
   * Constructor
   */
  public DacLabelPrinterLoggerWRx()
  {
  }

  /**
   * Log a debug message
   * 
   * @see com.daifukuamerica.wms.printer.barcode.LabelPrinterLogger#logDebug(java.lang.String)
   */
  @Override
  public void logDebug(String ipText)
  {
    mpLogger.logDebug(ipText);
  }

  /**
   * Log an error
   *  
   * @see com.daifukuamerica.wms.printer.barcode.LabelPrinterLogger#logError(java.lang.String)
   */
  @Override
  public void logError(String ipText)
  {
    mpLogger.logError(ipText);
  }

  /**
   * Log an exception
   * 
   * @see com.daifukuamerica.wms.printer.barcode.LabelPrinterLogger#logException(java.lang.Exception, java.lang.String)
   */
  @Override
  public void logException(Exception ipE, String ipText)
  {
    mpLogger.logException(ipE, ipText);
  }
  
  /**
   * Log an operation
   * 
   * @see com.daifukuamerica.wms.printer.barcode.LabelPrinterLogger#logOperation(java.lang.String)
   */
  @Override
  public void logOperation(String ipText)
  {
    mpLogger.logOperation(ipText);
  }
}