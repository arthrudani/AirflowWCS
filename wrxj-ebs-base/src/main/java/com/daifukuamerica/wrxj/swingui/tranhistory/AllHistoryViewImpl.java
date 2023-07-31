package com.daifukuamerica.wrxj.swingui.tranhistory;

import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swing.table.DacTable;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Description:<BR>
 *    View for Load Transactions.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 02-May-03<BR>
 *     Copyright (c) 2003<BR>
 *     Company:  SK Daifuku Corporation
 */
public class AllHistoryViewImpl extends TransactionHistoryView
{
  private static final long serialVersionUID = 0L;
  
  private int[] mpAllActions;

  public AllHistoryViewImpl(int inInterfaceType, ActionListener ipListener)
  {
    super(inInterfaceType, ipListener, "Transaction_All");
    mpDacTable.setToolTipColumns(TransactionHistoryData.ACTIONDESCRIPTION_NAME);
  }

/*===========================================================================
                  ****** Action methods go here ******
  ===========================================================================*/
  @Override
  public void searchButtonPressed()
  {
    searchButtonPressed(-1);
  }

  @Override
  public TransactionSearch getSearchFrameInstance()
  {
    return(new AllSearchImpl(interfaceType, mpAllActions));
  }

  @Override
  public DacTable getViewTableInstance()
  {
    return(mpDacTable);
  }

/*===========================================================================
              ****** All other private methods go here ******
  ===========================================================================*/
  @Override
  protected JPanel buildSearchPanel()
  {
    msViewHeader = "All Transactions";
    try
    {
      mpAllActions = DBTrans.getIntegerList(TransactionHistoryData.TRANTYPE_NAME);
      mpActionTypeCombo = new SKDCTranComboBox(TransactionHistoryData.TRANTYPE_NAME,
                                             mpAllActions, true);
    }
    catch(NoSuchFieldException err)
    {
      JOptionPane.showMessageDialog(null, err.getMessage(),
                                    "Translation Warning",
                                    JOptionPane.WARNING_MESSAGE);
      return(new JPanel());
    }

    return(super.buildSearchPanel());
  }
}
