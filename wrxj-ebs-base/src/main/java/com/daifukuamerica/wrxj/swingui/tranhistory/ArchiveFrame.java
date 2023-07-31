package com.daifukuamerica.wrxj.swingui.tranhistory;

import com.daifukuamerica.wrxj.archive.Archive;
import com.daifukuamerica.wrxj.archive.ExportFormat;
import com.daifukuamerica.wrxj.archive.tranhist.TransactionHistoryArchiver;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistory;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.swing.SKDCCheckBox;
import com.daifukuamerica.wrxj.swing.SKDCDateField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import java.util.Date;

/**
 * Description:<BR>
 *    Sets up the Archiving options internal frame.
 *
 * @author    A.D.
 * @version   1.0
 * @since     25-May-03
 */
public class ArchiveFrame extends TransactionSearch
{
  private SKDCTranComboBox mpTranCatCombo;
  private SKDCCheckBox     mpDeleteCheckBox;
  private Archive          mpArchiver;
  private DBObject         mpDBObj;
  
  /**
   * Constructor
   * 
   * @param inInterfaceType
   * @param ianActions
   */
  public ArchiveFrame(int inInterfaceType, int[] ianActions)
  {
    super("Archiving", ianActions);
    mpBtnSubmit.setText("Submit");
    mpBtnSubmit.setToolTipText("Submit Archive Task");
    
    mpBeginDateField.setDate(getOldestBeginDate());
                                       // Get our database connection.
    if (mpDBObj == null || !mpDBObj.checkConnected())
    {
      mpDBObj = new DBObjectTL().getDBObject();
      try { mpDBObj.connect(); }
      catch(DBException e) { return; }
    }
                                       // Setup the archiver.
    mpArchiver = new TransactionHistoryArchiver();
    mpTranHist = Factory.create(TransactionHistory.class);
  }

  /**
   * Build the screen
   * 
   * @param ianActionTypes
   */
  @Override
  protected void buildScreen(int[] ianActionTypes) throws NoSuchFieldException
  {
    mpTranCatCombo = new SKDCTranComboBox(
        TransactionHistoryData.TRANCATEGORY_NAME, ianActionTypes, true);
    mpBeginDateField = new SKDCDateField(false);
    mpEndingDateField = new SKDCDateField(false);
    mpDeleteCheckBox = new SKDCCheckBox();

    addInput("Transaction Category:", mpTranCatCombo);
    addInput("Beginning Date:", mpBeginDateField);
    addInput("Ending Date:", mpEndingDateField);
    addInput("Delete DB Records:", mpDeleteCheckBox);
  }

  /**
   * Archive
   */
  @Override
  protected void okButtonPressed()
  {
    TransactionToken vpToken = null;
    try
    {                                  // Set archiving parameters.
      mpArchiver.setSelectionFilter(mpTranCatCombo.getIntegerObject());
      mpArchiver.setExportFormat(ExportFormat.XML_FORMAT);
      mpArchiver.setBeginningDate(mpBeginDateField.getDate());
      mpArchiver.setEndingDate(mpEndingDateField.getDate());
      mpArchiver.setDeleteOption(mpDeleteCheckBox.isSelected());
      
      vpToken = mpDBObj.startTransaction();
      mpArchiver.exportData();      
      displayInfo("Archive operation successful", "Archive");
      mpDBObj.commitTransaction(vpToken);
    }
    catch(Exception e)
    {
      displayInfo("Archiving operation failed... Please see error log.",
          "Translation");
      logger.logException(
          "Error Archiving data in Transaction History Archiver frame.", e);
    }
    finally
    {
      mpDBObj.endTransaction(vpToken);
    }
  }

  /**
   * Clear Button handler.
   */
  @Override
  protected void clearButtonPressed()
  {
    mpTranCatCombo.setSelectedIndex(0);
    mpBeginDateField.setDate();
    mpEndingDateField.setDate();
  }

  /*=======================================================================*/
  /*  Methods private to this class go in this section.                    */
  /*=======================================================================*/

  /**
   * 
   * @return
   */
  private Date getOldestBeginDate()
  {
    Date rtnDate = null;
    
    try
    {
      rtnDate = mpTranHist.getOldestDate(false);
    }
    catch(DBException exc)
    {
      // There is really nothing to do in this case...
    }
    return((rtnDate == null) ? new Date() : rtnDate);
  }
}
