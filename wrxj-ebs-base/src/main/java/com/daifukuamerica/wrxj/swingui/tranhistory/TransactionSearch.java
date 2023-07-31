package com.daifukuamerica.wrxj.swingui.tranhistory;

import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistory;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCDateField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import java.util.Date;

/**
 * Description:<BR>
 *    Base class for setting up Transaction History detail search dialog.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 14-May-03<BR>
 *     Copyright (c) 2003<BR>
 *     Company:  SKDC Corporation
 */
@SuppressWarnings("serial")
public abstract class TransactionSearch extends DacInputFrame
{
  private static long ONE_WEEK_SECONDS  = 604800;
  protected Date mpBegDate = new Date();

  protected TransactionHistoryData mpTNSearchData = Factory.create(TransactionHistoryData.class);
  protected TransactionHistory mpTranHist = Factory.create(TransactionHistory.class);

  // All of the detailed search screens have these
  protected SKDCTranComboBox mpActionTypeCombo;
  protected SKDCDateField    mpBeginDateField;
  protected SKDCDateField    mpEndingDateField;

  /**
   * Constructor
   * 
   * @param isFrameTitle
   * @param ianActionTypes
   */
  public TransactionSearch(String isFrameTitle, int[] ianActionTypes)
  {
    super(isFrameTitle, isFrameTitle);
    
    mpBegDate.setTime((mpBegDate.getTime() - (ONE_WEEK_SECONDS * 1000)));

    try
    {
      buildScreen(ianActionTypes);
      useSearchButtons();
    }
    catch (Exception e)
    {
      logAndDisplayException(e);
    }
  }

  /**
   * Check to make sure these search options return results
   */
  protected void checkForData()
  {
    try
    {
      if (mpTranHist.getCount(mpTNSearchData) != 0)
      {
        // If something comes back, notify main frame of the change.
        changed(null, mpTNSearchData.clone());
        closeButtonPressed();
      }
      else
      {
        displayInfoAutoTimeOut("No data found.", "Search Information");
        mpActionTypeCombo.requestFocus();
      }
    }
    catch(Exception e)
    {
      logAndDisplayException("Search Error", e);
    }
  }

  /*========================================================================*/
  /* Abstract methods                                                       */
  /*========================================================================*/
  
  protected abstract void buildScreen(int[] ianActionTypes) throws NoSuchFieldException;
}
