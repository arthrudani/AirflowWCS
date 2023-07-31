package com.daifukuamerica.wrxj.swingui.tranhistory;

import com.daifukuamerica.wrxj.dbadapter.ParameterNameConstants;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swing.table.DacTable;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Description:<BR>
 *    View for Inventory Transactions.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 02-May-03<BR>
 *     Copyright (c) 2003<BR>
 *     Company:  SK Daifuku Corporation
 */
public class InventoryHistoryViewImpl extends TransactionHistoryView
{
  private static final long serialVersionUID = 0L;
  
  private int[]              manInventoryActions;
  protected SKDCIntegerField mpPickCountField;
  protected SKDCIntegerField mpStoreCountField;

  public InventoryHistoryViewImpl(int inInterfaceType, ActionListener ipListener)
  {
    super(inInterfaceType, ipListener, "Transaction_Inventory");
    add(recordCountPanel(), BorderLayout.SOUTH);
  }

/*===========================================================================
                  ****** Action methods go here ******
  ===========================================================================*/
  @Override
  public void searchButtonPressed()
  {
    if (searchButtonPressed(DBConstants.INVENTORY_TRAN) == -1)
    {
      mpPickCountField.setValue(0);
      mpStoreCountField.setValue(0);
    }
    else
    {
      int vnPickCount = getCount(DBConstants.INVENTORY_TRAN, DBConstants.ITEM_PICK);
      int vnStoreCount = getCount(DBConstants.INVENTORY_TRAN, DBConstants.ITEM_RECEIPT);
      mpPickCountField.setValue(vnPickCount);
      mpStoreCountField.setValue(vnStoreCount);
    }
  }

  @Override
  public TransactionSearch getSearchFrameInstance()
  {
    return(new InventorySearchImpl(interfaceType, manInventoryActions));
  }

  @Override
  public DacTable getViewTableInstance()
  {
    return(mpDacTable);
  }

/*===========================================================================
              ****** All other public methods go here ******
  ===========================================================================*/
 /**
  *  Method builds search panel for the Inventory Transaction View.
  *
  *
  *  @return <code>JPanel</code> containing drop-down list of the following
  *          inventory action types.
  *  <p><code>
  *   DBConstants.ADD           -- Inventory ADD transactions without P.O.<br>
  *   DBConstants.CYCLE_COUNT   -- Inventory Cycle-Count transactions.<br>
  *   DBConstants.COUNT         -- Inventory Count transactions without Cycle-Count
  *                                formality<br>
  *   DBConstants.DELETE        -- Inventory delete transaction without Shipping.<br>
  *   DBConstants.ITEM_PICK     -- Inventory pick transaction.<br>
  *   DBConstants.ITEM_RECEIVE  -- Inventory receipt transaction.<br>
  *   DBConstants.ITEM_SHIPPING -- Inventory shipping transaction.<br>
  *   DBConstants.MODIFY        -- Inventory modify without PO.<br>
  *   DBConstants.TRANSFER      -- Inventory transfer between loads.
  *   </code></p>
  */
  @Override
  protected JPanel buildSearchPanel()
  {
    msViewHeader = "Inventory Transactions";

    manInventoryActions = new int[] {DBConstants.ADD,
                             DBConstants.ADD_ITEM,
                             DBConstants.ADD_LOAD,
                             DBConstants.CYCLE_COUNT,
                             DBConstants.COUNT,
                             DBConstants.DELETE,
                             DBConstants.DELETE_ITEM,
                             DBConstants.DELETE_LOAD,
                             DBConstants.ITEM_PICK,
                             DBConstants.ITEM_RECEIPT,
                             DBConstants.ITEM_SHIP,
                             DBConstants.MODIFY,
                             DBConstants.MODIFY_ITEM,
                             DBConstants.MODIFY_LOAD,
                             DBConstants.TRANSFER,
                             DBConstants.ADD_ITEM_MASTER,
                             DBConstants.MODIFY_ITEM_MASTER,
                             DBConstants.DELETE_ITEM_MASTER};
    try
    {
      mpActionTypeCombo = new SKDCTranComboBox(TransactionHistoryData.TRANTYPE_NAME,
                                             manInventoryActions, true);
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

  private JPanel recordCountPanel()
  {
    int LNPICKCOUNT = DBInfo.getFieldLength(ParameterNameConstants.PICKQUANTITY);
    int LNSTORECOUNT = DBInfo.getFieldLength(ParameterNameConstants.RECEIVEDQUANTITY);

    JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
    countPanel.setBorder(BorderFactory.createEtchedBorder());

    countPanel.add(new SKDCLabel("Pick Transactions: "));
    mpPickCountField = new SKDCIntegerField(0, LNPICKCOUNT);
    mpPickCountField.setEnabled(false);
    countPanel.add(mpPickCountField);
    countPanel.add(Box.createHorizontalStrut(12));

    countPanel.add(new SKDCLabel("Store Transactions: "));
    mpStoreCountField = new SKDCIntegerField(0, LNSTORECOUNT);
    mpStoreCountField.setEnabled(false);
    countPanel.add(mpStoreCountField);

    return(countPanel);
  }
}
