package com.daifukuamerica.wrxj.swingui.tranhistory;

import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.swing.table.DacTable;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Description:<BR>
 *    View for User Transactions.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 02-May-03<BR>
 *     Copyright (c) 2003<BR>
 *     Company:  SK Daifuku Corporation
 */
public class UserHistoryViewImpl extends TransactionHistoryView
{
  private static final long serialVersionUID = 0L;
  
  private int[] userActions;

  public UserHistoryViewImpl(int inInterfaceType, ActionListener ipListener)
  {
    super(inInterfaceType, ipListener, "Transaction_User");
  }

/*===========================================================================
                  ****** Action methods go here ******
  ===========================================================================*/
  @Override
  public void searchButtonPressed()
  {
    searchButtonPressed(DBConstants.USER_TRAN);
  }

  @Override
  public TransactionSearch getSearchFrameInstance()
  {
    return(new UserSearchImpl(interfaceType, userActions));
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
    msViewHeader = "User Transaction";
    userActions = new int[] {DBConstants.LOGIN,
                             DBConstants.LOGOUT};
    try
    {
      mpActionTypeCombo = new SKDCTranComboBox(TransactionHistoryData.TRANTYPE_NAME,
                                               userActions, true);
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
