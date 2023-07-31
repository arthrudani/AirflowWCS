package com.daifukuamerica.wrxj.swingui.tranhistory;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCDataEnum;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.swing.SKDCDateField;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;

/**
 * Description:<BR>
 * Sets up the Inventory History search internal frame
 * 
 * @author A.D.
 * @version 1.0 <BR>
 *          Created: 12-May-03<BR>
 *          Copyright (c) 2003<BR>
 *          Company: SKDC Corporation
 */
@SuppressWarnings("serial")
public class InventorySearchImpl extends TransactionSearch
{
  private SKDCTextField mpFromLoadField;
  private SKDCTextField mpToLoadField;
  private SKDCTextField mpItemField;
  private SKDCTextField mpLotField;
  private SKDCTextField mpOrderField;

  /**
   * Constructor
   * 
   * @param inInterfaceType
   * @param ianInvtActions
   */
  public InventorySearchImpl(int inInterfaceType, int[] ianInvtActions)
  {
    super("Inventory History Search", ianInvtActions);
  }

  /**
   * Build the screen
   * 
   * @param ianActionTypes
   */
  @Override
  protected void buildScreen(int[] ianActionTypes) throws NoSuchFieldException
  {
    mpActionTypeCombo = new SKDCTranComboBox(
        TransactionHistoryData.TRANTYPE_NAME, ianActionTypes, true);
    mpBeginDateField = new SKDCDateField(false);
    mpBeginDateField.setDate(mpBegDate);
    mpEndingDateField = new SKDCDateField(false);
    mpFromLoadField = new SKDCTextField(TransactionHistoryData.LOADID_NAME);
    mpToLoadField = new SKDCTextField(TransactionHistoryData.TOLOAD_NAME);
    mpItemField = new SKDCTextField(TransactionHistoryData.ITEM_NAME);
    mpLotField = new SKDCTextField(TransactionHistoryData.LOT_NAME);
    mpOrderField = new SKDCTextField(TransactionHistoryData.ORDERID_NAME);

    addInput("Action Type:", mpActionTypeCombo);
    addInput("Beginning Date:", mpBeginDateField);
    addInput("Ending Date:", mpEndingDateField);
    addInput("From Load:", mpFromLoadField);
    addInput("To Load:", mpToLoadField);
    addInput("Item:", mpItemField);
    addInput("Lot:", mpLotField);
    addInput("Order:", mpOrderField);
  }

  /**
   * Processes Search Order request.
   */
  @Override
  protected void okButtonPressed()
  {
    mpTNSearchData.clear();
    try
    {
      mpTNSearchData.setTranTypeKey(mpActionTypeCombo.getIntegerValue());
    }
    catch (NoSuchFieldException e)
    {
      displayInfo(e.getMessage(), "Translation");
      return;
    }
    mpTNSearchData.setTranCategoryKey(DBConstants.INVENTORY_TRAN);
    mpTNSearchData.setDateRangeKey(mpBeginDateField.getDate(),
        mpEndingDateField.getDate());

    // Add From Load entry as Key.
    if (mpFromLoadField.getText().trim().length() != 0)
    {
      mpTNSearchData.setLoadKey(mpFromLoadField.getText());
    }
    // Add To-Load as Key.
    if (mpToLoadField.getText().trim().length() != 0)
    {
      mpTNSearchData.setToLoadKey(mpToLoadField.getText());
    }
    // Add Item as Key.
    if (mpItemField.getText().trim().length() != 0)
    {
      mpTNSearchData.setItemKey(mpItemField.getText());
    }
    // Add Lot as Key.
    if (mpLotField.getText().trim().length() != 0)
    {
      mpTNSearchData.setLotKey(mpLotField.getText());
    }
    // Add Order as Key.
    if (mpOrderField.getText().trim().length() != 0)
    {
      mpTNSearchData.setOrderIDKey(mpOrderField.getText());
    }
    
    mpTNSearchData.addOrderByColumn(TransactionHistoryData.TRANSDATETIME_NAME);
    mpTNSearchData.addOrderByColumn(AbstractSKDCDataEnum.ID.getName());

    checkForData();
  }

  /**
   * Clear Button handler.
   */
  @Override
  protected void clearButtonPressed()
  {
    mpActionTypeCombo.setSelectedIndex(0);
    mpBeginDateField.setDate(mpBegDate);
    mpEndingDateField.setDate();
    mpFromLoadField.setText("");
    mpToLoadField.setText("");
    mpItemField.setText("");
    mpLotField.setText("");
    mpOrderField.setText("");
  }
}
