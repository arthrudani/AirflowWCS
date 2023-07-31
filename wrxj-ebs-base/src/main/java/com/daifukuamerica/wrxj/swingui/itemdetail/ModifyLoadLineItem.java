package com.daifukuamerica.wrxj.swingui.itemdetail;

import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCDoubleField;
import com.daifukuamerica.wrxj.swing.SKDCUserData;

/**
 * <B>Description:</B> A screen class for modifying load line items.
 *
 * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
public class ModifyLoadLineItem extends AddLoadLineItem
{
  protected SKDCDoubleField mpAllocatedQtyTxt;

  /**
   * Constructor
   * 
   * @param isTitle
   * @param ipLLIData
   */
  public ModifyLoadLineItem(String isTitle, LoadLineItemData ipLLIData)
  {
    super(isTitle, ipLLIData.getLoadID(), ipLLIData.getItem());
    
    mpDefaultLLIData = ipLLIData.clone();
    
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
    
    // Disable fields that cannot be modified
    mpItemTxt.setEnabled(false);
    mpLotTxt.setEnabled(false);
    mpLoadTxt.setEnabled(false);
    mpLineIDTxt.setEnabled(false);
    mpSubLocTxt.setEnabled(false);

    // Initialize the new fields
    mpAllocatedQtyTxt = new SKDCDoubleField(10);
    mpAllocatedQtyTxt.setEnabled(SKDCUserData.isSuperUser());
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
    
    updateInputTitle(mpCurrentQtyTxt, "Current Quantity");
    addInput("Allocated Quantity", mpAllocatedQtyTxt);
    
    useModifyButtons();
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
    
    mpAllocatedQtyTxt.setValue(ipLLIData.getAllocatedQuantity());
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
      /*
       * Do minimal checking for the allocated quantity
       */
      if (mpAllocatedQtyTxt.getValue() != mpDefaultLLIData.getAllocatedQuantity())
      {
        if (mpAllocatedQtyTxt.getValue() < 0 || 
            mpAllocatedQtyTxt.getValue() > mpDefaultLLIData.getCurrentQuantity())
        {
          displayError("Invalid Allocated Quantity");
          mpAllocatedQtyTxt.requestFocus();
          vzPassed = false;
        }
        else
        {
          if (!displayYesNoPrompt("Are you sure you want to change the allocated quantity"))
          {
            displayInfo("Resetting allocated quantity");
            mpAllocatedQtyTxt.setValue(mpDefaultLLIData.getAllocatedQuantity());
            vzPassed = false;
          }
        }
      }
    }
    return vzPassed;
  }
  
  /**
   * Check for an existing Load Line Item
   * 
   * @see com.daifukuamerica.wrxj.swingui.itemdetail.AddLoadLineItem#checkExistingLLI()
   * 
   * @return true if the load line item we are going to modify is still valid,
   *         false otherwise
   */
  @Override
  protected boolean checkExistingLLI()
  {
    // There should ALWAYS be an existing load line item for a modify
    return true;
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
    vpLLIData.setAllocatedQuantity(mpAllocatedQtyTxt.getValue());
    return vpLLIData;
  }
  
  /**
   * Get the reason code for the adjustment and do the update.
   * 
   * <P>For Modify actions, there is only an adjustment reason code if the 
   * quantity has changed.</P>
   * 
   * @see com.daifukuamerica.wrxj.swingui.itemdetail.AddLoadLineItem#getAdjustmentReasonCodeAndUpdateLoadLineItem(com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData)
   * 
   * @param ipLLIData
   */
  @Override
  protected void getAdjustmentReasonCodeAndUpdateLoadLineItem(
      LoadLineItemData ipLLIData)
  {
    // Only get an adjustment reason code when the quantity changes
    if (mpDefaultLLIData.getCurrentQuantity() != ipLLIData.getCurrentQuantity())
    {
      super.getAdjustmentReasonCodeAndUpdateLoadLineItem(ipLLIData);
    }
    else
    {
      commitChanges(ipLLIData, "");
    }
  }
  
  /**
   * Actually modify the item detail
   * 
   * @see com.daifukuamerica.wrxj.swingui.itemdetail.AddLoadLineItem#commitChanges(com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData, java.lang.String)
   * 
   * @param ipLLIData
   * @param isReasonCode
   */
  @Override
  protected void commitChanges(LoadLineItemData ipLLIData, String isReasonCode)
  {
    // if the hold status/reason changed, we need to tell the host
    boolean vzSendInventoryStatus = false;
    if (ipLLIData.getHoldType() != mpDefaultLLIData.getHoldType())
    {
      vzSendInventoryStatus = true;
    }
    else if (!ipLLIData.getHoldReason().equals(mpDefaultLLIData.getHoldReason()))
    {
      vzSendInventoryStatus = true;
    }
    
    try
    {
      mpInvServer.updateLoadLineItemInfo(ipLLIData, isReasonCode,
          vzSendInventoryStatus, true);
      
      changed();
      displayInfoAutoTimeOut("Item " + ipLLIData.getItem() + " updated");
      closeButtonPressed();
    }
    catch(DBException exp)
    {
      logAndDisplayException(exp);
    }
  }
}
