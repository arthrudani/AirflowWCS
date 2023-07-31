package com.daifukuamerica.wrxj.swingui.tranhistory;

import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
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
public class LoadHistoryViewImpl extends TransactionHistoryView
{
  private static final long serialVersionUID = 0L;
  
  private int[] manLoadActions;

  public LoadHistoryViewImpl(int inInterfaceType, ActionListener ipListener)
  {
    super(inInterfaceType, ipListener, "Transaction_Load");
  }

/*===========================================================================
                  ****** Action methods go here ******
  ===========================================================================*/
  @Override
  public void searchButtonPressed()
  {
    searchButtonPressed(DBConstants.LOAD_TRAN);
  }

  @Override
  public TransactionSearch getSearchFrameInstance()
  {
    return(new LoadSearchImpl(interfaceType, manLoadActions));
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
    msViewHeader = "Load Transactions";
    manLoadActions = new int[] {DBConstants.ADD_LOAD,
                                DBConstants.COMPLETION,
                                DBConstants.DELETE_LOAD,
                                DBConstants.LOAD_SCHED,
                                DBConstants.MODIFY_LOAD,
                                DBConstants.TRANSFER};
    try
    {
      mpActionTypeCombo = new SKDCTranComboBox(TransactionHistoryData.TRANTYPE_NAME,
                                             manLoadActions, true);
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
