package com.daifukuamerica.wrxj.swingui.tranhistory;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCDataEnum;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.swing.SKDCDateField;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;

/**
 * Description:<BR>
 * Sets up the Order History search internal frame.
 * 
 * @author A.D.
 * @version 1.0 <BR>
 *          Created: 14-May-03<BR>
 *          Copyright (c) 2003<BR>
 *          Company: SKDC Corporation
 */
@SuppressWarnings("serial")
public class OrderSearchImpl extends TransactionSearch
{
  private SKDCTextField mpOrderField;
  private SKDCTextField mpDestStationField;
  private SKDCTranComboBox mpOrderTypeCombo;

  /**
   * Constructor
   * 
   * @param inInterfaceType
   * @param ianOrderActions
   */
  public OrderSearchImpl(int inInterfaceType, int[] ianOrderActions)
  {
    super("Order History Search", ianOrderActions);
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
    mpOrderField = new SKDCTextField(TransactionHistoryData.ORDERID_NAME);
    mpDestStationField = new SKDCTextField(TransactionHistoryData.STATION_NAME);
    mpOrderTypeCombo = new SKDCTranComboBox(
        TransactionHistoryData.ORDERTYPE_NAME, true);

    addInput("Action Type:", mpActionTypeCombo);
    addInput("Beginning Date:", mpBeginDateField);
    addInput("Ending Date:", mpEndingDateField);
    addInput("Order:", mpOrderField);
    addInput("Order Type:", mpOrderTypeCombo);
    addInput("To Station:", mpDestStationField);
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
      mpTNSearchData.setOrderTypeKey(mpOrderTypeCombo.getIntegerValue());
    }
    catch (NoSuchFieldException e)
    {
      displayInfo(e.getMessage(), "Translation");
      return;
    }
    mpTNSearchData.setTranCategoryKey(DBConstants.ORDER_TRAN);
    mpTNSearchData.setDateRangeKey(mpBeginDateField.getDate(),
        mpEndingDateField.getDate());

    // Add From Order as Key.
    if (mpOrderField.getText().trim().length() != 0)
    {
      mpTNSearchData.setOrderIDKey(mpOrderField.getText());
    }
    // Add Dest. Station as Key.
    if (mpDestStationField.getText().trim().length() != 0)
    {
      mpTNSearchData.setToStationKey(mpDestStationField.getText());
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
    mpOrderTypeCombo.setSelectedIndex(0);
    mpBeginDateField.setDate(mpBegDate);
    mpEndingDateField.setDate();
    mpOrderField.setText("");
    mpDestStationField.setText("");
  }
}
