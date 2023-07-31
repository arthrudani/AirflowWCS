package com.daifukuamerica.wrxj.swingui.itemdetail;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPoReceivingServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCTextField;


/**
 * <B>Description:</B> A screen class for storing load line items.
 *
 * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
public class StoreLoadLineItem extends AddLoadLineItem
{
  // New screen field
  protected SKDCTextField mpExpectedReceipt;

  // Server
  protected StandardPoReceivingServer mpPOServer;

  /**
   * Constructor
   *
   * @param isTitle
   * @param isLoadID          - Initial load ID (null if none)
   * @param isItem            - Initial item ID (null if none)
   * @param isExpectedReceipt - Initial expected receipt ID (null if none)
   */
  public StoreLoadLineItem(String isTitle, String isLoadID, String isItem,
      String isExpectedReceipt)
  {
    super(isTitle, isLoadID, isItem);

    mpPOServer = Factory.create(StandardPoReceivingServer.class);

    if (isExpectedReceipt != null)
    {
      mpDefaultLLIData.setExpectedReceipt(isExpectedReceipt);
    }

    setData(mpDefaultLLIData);
  }


  /*========================================================================*/
  /* Overridden AddItemDetail methods                                       */
  /*========================================================================*/

  /**
   * Initialize the screen components
   *
   * @see com.daifukuamerica.wrxj.swingui.itemdetail.AddLoadLineItem#initializeScreenComponents()
   */
  @Override
  protected void initializeScreenComponents()
  {
    super.initializeScreenComponents();
    mpExpectedReceipt = new SKDCTextField(LoadLineItemData.EXPECTEDRECEIPT_NAME);
    mpExpectedReceipt.setEnabled(false);
  }

  /**
   * Method to initialize screen components. This adds the components to the
   * screen and adds listeners as needed.
   *
   * @see com.daifukuamerica.wrxj.swingui.itemdetail.AddLoadLineItem#buildScreen()
   */
  @Override
  protected void buildScreen()
  {
    super.buildScreen();
    insertInput(0, "Expected Receipt", mpExpectedReceipt);
    setHeaderRows(4);
  }

  /**
   * Method to refresh screen fields.
   *
   * @see com.daifukuamerica.wrxj.swingui.itemdetail.AddLoadLineItem#setData(com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData)
   *
   * @param ipLLIData Load line item data to use in refreshing.
   */
  @Override
  protected void setData(LoadLineItemData ipLLIData)
  {
    super.setData(ipLLIData);

    mpExpectedReceipt.setText(ipLLIData.getExpectedReceipt());
  }

  /**
   * Make sure the inputs are valid
   *
   * @see com.daifukuamerica.wrxj.swingui.itemdetail.AddLoadLineItem#validateInputs()
   *
   * @return true if all is well, false otherwise
   */
  @Override
  protected boolean validateInputs()
  {
    boolean vzPassed = super.validateInputs();
    if (vzPassed)
    {
      vzPassed = validateER();
    }
    return vzPassed;
  }

  /**
   * Convert the inputs to a LoadLineItemData
   *
   * @see com.daifukuamerica.wrxj.swingui.itemdetail.AddLoadLineItem#getLoadLineItemDataFromInputs()
   *
   * @return <code>LoadLineItemData</code>
   */
  @Override
  protected LoadLineItemData getLoadLineItemDataFromInputs()
  {
    LoadLineItemData vpLLIData = super.getLoadLineItemDataFromInputs();
    vpLLIData.setExpectedReceipt(mpExpectedReceipt.getText());
    return vpLLIData;
  }

  /**
   * Get the reason code for the adjustment and do the update.
   *
   * <P>For Storing actions, there is no adjustment reason code.  Just do the
   * update.</P>
   *
   * @see com.daifukuamerica.wrxj.swingui.itemdetail.AddLoadLineItem#getAdjustmentReasonCodeAndUpdateLoadLineItem(com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData)
   *
   * @param ipLLIData
   */
  @Override
  protected void getAdjustmentReasonCodeAndUpdateLoadLineItem(
      LoadLineItemData ipLLIData)
  {
    // We don't need an adjustment reason code for a store
    commitChanges(ipLLIData, "");
  }

  /**
   * Actually add the item detail
   *
   * @see com.daifukuamerica.wrxj.swingui.itemdetail.AddLoadLineItem#commitChanges(com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData, java.lang.String)
   *
   * @param ipLLIData
   * @param isReasonCode
   */
  @Override
  protected void commitChanges(LoadLineItemData ipLLIData, String isReasonCode)
  {
    try
    {
      if (mpExpectedReceipt.getText().length() > 0)
      {
        // Store with Expected Receipt
        mpPOServer.receivePOLine(ipLLIData.getExpectedReceipt(),
                                 ipLLIData.getLoadID(), ipLLIData.getItem(),
                                 ipLLIData.getLot(), ipLLIData.getPositionID(),
                                 ipLLIData.getCurrentQuantity(), ipLLIData.getExpirationDate(),
                                 ipLLIData.getAgingDate());
      }
      else
      {
        // Store without Expected Receipt
        mpInvServer.addLoadLIWithValidation(ipLLIData,
            MessageOutNames.STORE_COMPLETE, isReasonCode);
      }
      changed(null, ipLLIData);
      displayInfoAutoTimeOut("Item " + ipLLIData.getItem() + " added");
      clearButtonPressed();
    }
    catch (DBException dbe)
    {
      logAndDisplayException("Error storing item", dbe);
    }
  }


  /*========================================================================*/
  /* new protected methods                                                  */
  /*========================================================================*/

  /**
   * Make sure we have a valid expected receipt
   *
   * @return
   */
  public boolean validateER()
  {
    try
    {
      String vsER = mpExpectedReceipt.getText();
      if (vsER.length() == 0 && Application.getBoolean("PORequired", true))
      {
        displayWarning("PO is required.");
        return false;
      }
      if (vsER.length() > 0 && mpPOServer.getPoHeaderRecord(vsER) == null)
      {
        displayWarning("Invalid PO number.");
        return false;
      }
    }
    catch (DBException dbe)
    {
      logAndDisplayException("Error validating PO number", dbe);
      return false;
    }
    return true;
  }
}
