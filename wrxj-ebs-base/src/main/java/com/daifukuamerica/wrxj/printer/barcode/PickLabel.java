/**
 * 
 */
package com.daifukuamerica.wrxj.printer.barcode;

import com.daifukuamerica.wms.printer.DacLabelGenerator;
import com.daifukuamerica.wms.printer.barcode.LabelGeneratorException;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.printer.DacLabelPrinterLoggerWRx;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * <B>Description:</B> This class populates the xxxPickLabel.bpt template<BR>
 *
 * @author       mandrus<BR>
 * @version      1.0
 * 
 * <BR>Copyright (c) 2008 by Daifuku America Corporation
 */
public class PickLabel
{
  /**
   * Public constructor for Factory
   */
  public PickLabel()
  {
  }
  
  /**
   * Print a Pick Label
   * @param isPrinterName
   * @param ipMoveData
   * @param isToLoad
   * @param idQtyPicked
   * @throws LabelGeneratorException
   */
  public void printPickLabel(String isPrinterName, MoveData ipMoveData,
      String isToLoad, double idQtyPicked, Date ipPickDate, boolean izReprint)
      throws LabelGeneratorException
  {
    /*
     *  Set up the label information
     */
    HashMap<String,String> vpLabelDataMap = new HashMap<String,String>();
    vpLabelDataMap.put("SITEM", ipMoveData.getItem());
    vpLabelDataMap.put("SLOT", ipMoveData.getPickLot());
    vpLabelDataMap.put("FQUANTITY", Double.toString(SKDCUtility.getTruncatedDouble(idQtyPicked)));
    vpLabelDataMap.put("DPICKDATE", DateFormat.getDateTimeInstance().format(ipPickDate));
    vpLabelDataMap.put("SLOADID", ipMoveData.getLoadID());
    vpLabelDataMap.put("STOLOAD", isToLoad);
    vpLabelDataMap.put("SORDERID", ipMoveData.getOrderID());
    vpLabelDataMap.put("SSTATIONNAME", ipMoveData.getAddress());
    vpLabelDataMap.put("SPOSITIONID", ipMoveData.getPositionID());
    vpLabelDataMap.put("SREPRINT", izReprint ? "(REPRINT)" : "");
    
    // Now that we have everything - print the label
    DacLabelGenerator.print(isPrinterName, "PickLabel", vpLabelDataMap,
        new DacLabelPrinterLoggerWRx());
  }
}
