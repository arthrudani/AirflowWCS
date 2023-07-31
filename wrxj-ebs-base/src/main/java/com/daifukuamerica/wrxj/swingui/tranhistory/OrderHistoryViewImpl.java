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
 *    View for Order Transactions.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 02-May-03<BR>
 *     Copyright (c) 2003<BR>
 *     Company:  SK Daifuku Corporation
 */
public class OrderHistoryViewImpl extends TransactionHistoryView
{
  private static final long serialVersionUID = 0L;
  
  private int[] manOrderActions;

  public OrderHistoryViewImpl(int inInterfaceType, ActionListener ipListener)
  {
    super(inInterfaceType, ipListener, "Transaction_Order");
  }

/*===========================================================================
                  ****** Action methods go here ******
  ===========================================================================*/
  @Override
  public void searchButtonPressed()
  {
    searchButtonPressed(DBConstants.ORDER_TRAN);
  }

  @Override
  public TransactionSearch getSearchFrameInstance()
  {
    return(new OrderSearchImpl(interfaceType, manOrderActions));
  }

  @Override
  public DacTable getViewTableInstance()
  {
    return(mpDacTable);
  }

/*===========================================================================
              ****** All other methods go here ******
  ===========================================================================*/
  @Override
  protected JPanel buildSearchPanel()
  {
    msViewHeader = "Order Transactions";
    manOrderActions = new int[] {DBConstants.ADD_ORDER,
                              DBConstants.MODIFY_ORDER,
                              DBConstants.DELETE_ORDER,
                              DBConstants.COMPLETION,
                              DBConstants.ADD_ORDER_LINE,
                              DBConstants.MODIFY_ORDER_LINE,
                              DBConstants.DELETE_ORDER_LINE};
    try
    {
      mpActionTypeCombo = new SKDCTranComboBox(
          TransactionHistoryData.TRANTYPE_NAME, manOrderActions, true);
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
