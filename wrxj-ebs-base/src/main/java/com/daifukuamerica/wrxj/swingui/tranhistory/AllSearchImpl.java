package com.daifukuamerica.wrxj.swingui.tranhistory;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCDataEnum;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;
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
public class AllSearchImpl extends TransactionSearch
{
  private SKDCTextField mpTxtFromLoad;
  private SKDCTextField mpTxtToLoad;
  private SKDCTextField mpTxtItem;
  private SKDCTextField mpTxtLot;
  private SKDCTextField mpTxtOrder;
  private SKDCTextField mpTxtUser;

  /**
   * Constructor
   * 
   * @param inInterfaceType
   * @param ianAllActions
   */
  public AllSearchImpl(int inInterfaceType, int[] ianAllActions)
  {
    super("All History Search", ianAllActions);
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
    mpTxtFromLoad = new SKDCTextField(TransactionHistoryData.LOADID_NAME);
    mpTxtToLoad = new SKDCTextField(TransactionHistoryData.TOLOAD_NAME);
    mpTxtItem = new SKDCTextField(TransactionHistoryData.ITEM_NAME);
    mpTxtLot = new SKDCTextField(TransactionHistoryData.LOT_NAME);
    mpTxtOrder = new SKDCTextField(TransactionHistoryData.ORDERID_NAME);
    mpTxtUser = new SKDCTextField(TransactionHistoryData.USERID_NAME);

    addInput("Action Type:", mpActionTypeCombo);
    addInput("Beginning Date:", mpBeginDateField);
    addInput("Ending Date:", mpEndingDateField);
    addInput("From Load:", mpTxtFromLoad);
    addInput("To Load:", mpTxtToLoad);
    addInput("Item:", mpTxtItem);
    addInput("Lot:", mpTxtLot);
    addInput("Order:", mpTxtOrder);
    addInput("User:", mpTxtUser);
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
    mpTNSearchData.setDateRangeKey(mpBeginDateField.getDate(),
        mpEndingDateField.getDate());

    // Add From Load entry as Key.
    if (mpTxtFromLoad.getText().trim().length() != 0)
    {
      mpTNSearchData.setLoadKey(mpTxtFromLoad.getText());
    }
    // Add To-Load as Key.
    if (mpTxtToLoad.getText().trim().length() != 0)
    {
      mpTNSearchData.setToLoadKey(mpTxtToLoad.getText());
    }
    // Add Item as Key.
    if (mpTxtItem.getText().trim().length() != 0)
    {
      mpTNSearchData.setItemKey(mpTxtItem.getText());
    }
    // Add Lot as Key.
    if (mpTxtLot.getText().trim().length() != 0)
    {
      mpTNSearchData.setLotKey(mpTxtLot.getText());
    }
    // Add Order as Key.
    if (mpTxtOrder.getText().trim().length() != 0)
    {
      mpTNSearchData.setOrderIDKey(mpTxtOrder.getText());
    }
    // Add User as Key.
    if (mpTxtUser.getText().trim().length() != 0)
    {
      mpTNSearchData.setUserIDKey(mpTxtUser.getText());
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
    mpTxtFromLoad.setText("");
    mpTxtToLoad.setText("");
    mpTxtItem.setText("");
    mpTxtLot.setText("");
    mpTxtOrder.setText("");
    mpTxtUser.setText("");
  }
}
