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
public class SystemHistoryViewImpl extends TransactionHistoryView
{
  private int[] systemActions;

  public SystemHistoryViewImpl(int inInterfaceType, ActionListener ipListener)
  {
    super(inInterfaceType, ipListener, "Transaction_System");
  }

/*===========================================================================
                  ****** Action methods go here ******
  ===========================================================================*/
  @Override
  public void searchButtonPressed()
  {
    searchButtonPressed(DBConstants.SYSTEM_TRAN);
  }

  @Override
  public TransactionSearch getSearchFrameInstance()
  {
    return(new SystemSearchImpl(interfaceType, systemActions));
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
    msViewHeader = "System Transactions";
    systemActions = new int[] { DBConstants.DELETE,
                                DBConstants.MODIFY };
    try
    {
      mpActionTypeCombo = new SKDCTranComboBox(TransactionHistoryData.TRANTYPE_NAME,
                                             systemActions, true);
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
