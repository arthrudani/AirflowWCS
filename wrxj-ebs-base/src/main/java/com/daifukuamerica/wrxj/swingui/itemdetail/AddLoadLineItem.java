package com.daifukuamerica.wrxj.swingui.itemdetail;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryAdjustServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCDateField;
import com.daifukuamerica.wrxj.swing.SKDCDoubleField;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swingui.item.ItemNumberInput;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

/**
 * A screen class for adding load line items. Load line items contain the
 * specific details about inventory.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class AddLoadLineItem extends DacInputFrame
{
  protected ItemNumberInput mpItemTxt;
  protected SKDCTextField mpDescTxt;
  protected SKDCTextField mpLotTxt;
  protected SKDCTextField mpLoadTxt;
  protected SKDCTextField mpLineIDTxt;
  protected SKDCTextField mpSubLocTxt;
  protected SKDCTranComboBox mpPriorityAllocationCombo;
  protected SKDCDoubleField mpCurrentQtyTxt;
  protected SKDCDateField mpAgingDateField;
  protected SKDCDateField mpExpirationDateField;
  protected SKDCDateField mpCCIDateField;
  protected SKDCTranComboBox mpHoldTypeCombo;
  protected SKDCComboBox mpHoldReasonCodeCombo;
  
  protected LoadLineItemData mpDefaultLLIData;
  protected String[] masReasonCodes;

  protected StandardInventoryServer mpInvServer;

  /*========================================================================*/
  /* Constructor methods                                                    */
  /*========================================================================*/
  
  /**
   * Constructor
   * 
   * @param isTitle  - Title to be displayed.
   * @param isLoadID - Initial load ID (null if none)
   * @param isItem   - Initial item ID (null if none)
   */
  public AddLoadLineItem(String isTitle, String isLoadID, String isItem)
  {
    // Build the screen
    super(isTitle, "Item Detail Information");
    mpInvServer = Factory.create(StandardInventoryServer.class);
    buildScreen();
    
    // Initialization
    mpDefaultLLIData = Factory.create(LoadLineItemData.class);
    
    if (isLoadID != null)
    {
      mpDefaultLLIData.setLoadID(isLoadID);
      mpLoadTxt.setEnabled(false);
    }
    
    if (isItem != null)
    {
      mpDefaultLLIData.setItem(isItem);
    }
    
    setData(mpDefaultLLIData);
  }

  /*========================================================================*/
  /* Overridden methods                                                     */
  /*========================================================================*/
  
  /**
   * Clean up
   * 
   * @see SKDCInternalFrame#cleanUpOnClose()
   */
  @Override
  public void cleanUpOnClose()
  {
    mpInvServer.cleanUp();
    super.cleanUpOnClose(); 
  }

  /**
   * Action method to handle Clear button Mouse Event.
   */
  @Override
  public void clearButtonPressed()
  {
    setData(mpDefaultLLIData);
  }
  
  /**
   * Action method to handle OK button. Verifies that entered data is valid,
   * then adds a new load line item to the database.
   */
  @Override
  protected void okButtonPressed()
  {
    // Make sure the inputs are valid
    if (!validateInputs())
    {
      return;
    }

    // Convert the inputs to a LoadLineItemData
    LoadLineItemData vpNewLLI = getLoadLineItemDataFromInputs();
    
    // Do the update
    getAdjustmentReasonCodeAndUpdateLoadLineItem(vpNewLLI);
  }


  /*========================================================================*/
  /* Protected methods                                                      */
  /*========================================================================*/

  /**
   * Initialize the screen components
   */
  protected void initializeScreenComponents()
  {
    mpDescTxt = new SKDCTextField(ItemMasterData.DESCRIPTION_NAME);
    mpLotTxt = new SKDCTextField(LoadLineItemData.LOT_NAME);
    mpLoadTxt = new SKDCTextField(LoadLineItemData.LOADID_NAME);
    mpLineIDTxt = new SKDCTextField(LoadLineItemData.LINEID_NAME);
    mpSubLocTxt = new SKDCTextField(LoadLineItemData.POSITIONID_NAME);
    mpCurrentQtyTxt = new SKDCDoubleField(10);
    mpAgingDateField = new SKDCDateField(false);
    mpExpirationDateField = new SKDCDateField(false);
    mpCCIDateField = new SKDCDateField(false);

    mpItemTxt = new ItemNumberInput(mpInvServer, true, false);
    try
    {
      mpPriorityAllocationCombo = new SKDCTranComboBox(LoadLineItemData.PRIORITYALLOCATION_NAME);
      mpHoldTypeCombo = new SKDCTranComboBox(LoadLineItemData.HOLDTYPE_NAME,
          new int[] { DBConstants.ITMAVAIL, DBConstants.ITMHOLD,
              DBConstants.QCHOLD }, false);
      mpHoldTypeCombo.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          holdType_actionPerformed();
        }
      });
    }
    catch(NoSuchFieldException e)
    {
      displayError(e.getMessage());
    }

    String vsLongestReasonCode = "";
    try
    {
      StandardInventoryAdjustServer vpInvAdjServer = 
        Factory.create(StandardInventoryAdjustServer.class);
      masReasonCodes = vpInvAdjServer.getReasonCodeChoiceList(DBConstants.REASONHOLD);
      if (masReasonCodes.length == 0)
      {
        masReasonCodes = new String[] { "" };
      }
      for (String s : masReasonCodes)
      {
        if (s.length() > vsLongestReasonCode.length())
        {
          vsLongestReasonCode = s;
        }
      }
    }
    catch (DBException dbe)
    {
      logAndDisplayException("Error getting hold reason codes", dbe);
    }
    mpHoldReasonCodeCombo = new SKDCComboBox(masReasonCodes);
    mpHoldReasonCodeCombo.setPrototypeDisplayValue(vsLongestReasonCode);
    mpCCIDateField.setEnabled(false);
    mpDescTxt.setEnabled(false);

    mpItemTxt.linkDescription(mpDescTxt);
  }
  
  /**
   * Method to initialize screen components. This adds the components to the
   * screen and adds listeners as needed.
   */
  protected void buildScreen()
  {
    // Initialize screen components
    initializeScreenComponents();
    
    // Put the screen components on the screen
    
    // Header Rows
    addInput("Item", mpItemTxt);
    addInput("Description", mpDescTxt);
    addInput("Lot", mpLotTxt);
    addInput("Line ID", mpLineIDTxt);
    
    // Column 1
    addInput("Load ID", mpLoadTxt);
    addInput("Hold Type", mpHoldTypeCombo);
    addInput("Hold Reason", mpHoldReasonCodeCombo);
    setInputVisible(mpHoldReasonCodeCombo, masReasonCodes.length > 1);
    addInput("Priority Allocation", mpPriorityAllocationCombo);
    addInput("Add Quantity", mpCurrentQtyTxt);
    
    // Column 2
    addInput("Sub Location", mpSubLocTxt);
    addInput("Aging Date", mpAgingDateField);
    addInput("Expiration Date", mpExpirationDateField);
    addInput("CCI Date", mpCCIDateField);

    setHeaderRows(4);
    setInputColumns(2);
    
    useAddButtons();
  }

  /**
   *  Action method to handle hold type changes.
   */
  protected void holdType_actionPerformed()
  {
    // Get the load line item
    String[] masBlank = { "" };
    String vsHoldType = (String) mpHoldTypeCombo.getSelectedItem();
    int vnHoldType = 0;
    try
    {
      vnHoldType = DBTrans.getIntegerValue(LoadLineItemData.HOLDTYPE_NAME, vsHoldType);
    }
    catch(NoSuchFieldException exp)
    {
      logger.logException(exp);
    }
    
    if (vnHoldType == DBConstants.ITMAVAIL || 
        vnHoldType == DBConstants.SHIPHOLD)
    {
      mpHoldReasonCodeCombo.setComboBoxData(masBlank);
      mpHoldReasonCodeCombo.setEnabled(false);
    }
    else
    {
      if (masReasonCodes == null || masReasonCodes.length == 0)
      {
        mpHoldReasonCodeCombo.setComboBoxData(masBlank);
        mpHoldReasonCodeCombo.setEnabled(false);
      }
      else
      {
        mpHoldReasonCodeCombo.setComboBoxData(masReasonCodes);
        mpHoldReasonCodeCombo.setEnabled(true);
      }
    }
  }
   
  /**
   * Method to refresh screen fields.
   * 
   * @param ipLLIData Load line item data to use in refreshing.
   */
  protected void setData(LoadLineItemData ipLLIData)
  {
    mpItemTxt.setText(ipLLIData.getItem());
    mpLotTxt.setText(ipLLIData.getLot());
    mpLoadTxt.setText(ipLLIData.getLoadID());
    Date tempDate = ipLLIData.getAgingDate();
    mpAgingDateField.setDate(tempDate);
    tempDate = ipLLIData.getExpirationDate();
    mpExpirationDateField.setDate(tempDate);
    mpCurrentQtyTxt.setValue(ipLLIData.getCurrentQuantity());
    mpLineIDTxt.setText(ipLLIData.getLineID());
    mpSubLocTxt.setText(ipLLIData.getPositionID());
    tempDate = ipLLIData.getLastCCIDate();
    mpCCIDateField.setDate(tempDate);
    
    try
    {
      mpHoldTypeCombo.setSelectedElement(ipLLIData.getHoldType());
      mpHoldReasonCodeCombo.selectItemBy(ipLLIData.getHoldReason());
      mpPriorityAllocationCombo.setSelectedElement(ipLLIData.getPriorityAllocation());
    }
    catch (NoSuchFieldException e2)
    {
      logAndDisplayException(e2);
    }
  }

  /**
   * Make sure the inputs are valid
   * 
   * @return true if all is well, false otherwise
   */
  protected boolean validateInputs()
  {
    boolean vzPassed = true;

    // The item is required
    if (vzPassed)
    {
      if (mpItemTxt.getText().length() <= 0) // required
      {
        displayError("Item ID is required");
        vzPassed = false;
      }
    }
    // The item master must exist
    if (vzPassed)
    {
      vzPassed = assertItemExists(mpItemTxt.getText());
    }

    // The load is required
    if (vzPassed)
    {
      if (mpLoadTxt.getText().length() <= 0) // required
      {
        displayError("Load ID is required");
        vzPassed = false;
      }
    }
    
    // A user-edited load must be valid
    if (vzPassed)
    {
      if (mpLoadTxt.isEnabled())
      {
        StandardLoadServer vpLoadServer = Factory.create(StandardLoadServer.class);
        if (vpLoadServer.getLoad(mpLoadTxt.getText()) == null)
        {
          displayError("Load \"" + mpLoadTxt.getText() + "\" is required");
          vzPassed = false;
        }
      }
    }

    // The quantity must be greater than 0
    if (vzPassed)
    {
      if (mpCurrentQtyTxt.getValue() <= 0)  // required
      {
        displayError("Quantity must be greater than zero");
        vzPassed = false;
      }
    }

    // If there is a matching, existing item detail warn the user
    if (vzPassed)
    {
      vzPassed = checkExistingLLI();
    }
    
    return vzPassed;
  }

  /**
   * Ensure that an item master exists.
   * 
   * Displays an error message and returns false if the item master does exist,
   * returns true otherwise.
   * @param isItem The item to check.
   * @return boolean stating whether an item master exists.
   */
  protected boolean assertItemExists(String isItem)
  {
    // Make sure the item master exists.
    boolean vzItemExists = false;
    try
    {
      vzItemExists = mpInvServer.itemMasterExists(isItem);
    }
    catch (DBException exp)
    {
      logAndDisplayException(exp);
      return false;
    }
    if (!vzItemExists)
    {
      displayInfoAutoTimeOut("Item Master '" + mpItemTxt.getText() + "' does not exist");
      setData(mpDefaultLLIData);
      return false;
    }
    return true;
  }
  
  /**
   * Check for an existing Load Line Item
   * 
   * @return true if there is not one or we can combine, false otherwise
   */
  protected boolean checkExistingLLI()
  {
    boolean vzPassed = true;
    
    try
    {
      LoadLineItemData vpExistingLLIData = mpInvServer.getLoadLineItem(
          mpLoadTxt.getText(), mpItemTxt.getText(), mpLotTxt.getText(), mpLineIDTxt.getText(), "",
          "", mpSubLocTxt.getText());
      if (vpExistingLLIData != null)
      {
        if (!displayYesNoPrompt("Load " + mpLoadTxt.getText() + 
            " already contains this item/lot/sub-location.  " +
            "Combine this with the existing Item Detail"))
        {
          vzPassed = false;
        }
      }
    }
    catch (DBException e)
    {
      logAndDisplayException("Unable to get Item Detail data", e);
      vzPassed = false;
    }
    return vzPassed;
  }

  /**
   * Convert the inputs to a LoadLineItemData
   * 
   * @return <code>LoadLineItemData</code>
   */
  protected LoadLineItemData getLoadLineItemDataFromInputs()
  {
    LoadLineItemData vpNewLLIData = Factory.create(LoadLineItemData.class);
    
    vpNewLLIData.setItem(mpItemTxt.getText());
    vpNewLLIData.setLoadID(mpLoadTxt.getText());
    vpNewLLIData.setAllocatedQuantity(0.0);
    vpNewLLIData.setLastCCIDate(new Date());
    vpNewLLIData.setLineID(mpLineIDTxt.getText());	
    vpNewLLIData.setPositionID(mpSubLocTxt.getText());
    vpNewLLIData.setCurrentQuantity(mpCurrentQtyTxt.getValue());
    vpNewLLIData.setLot(mpLotTxt.getText());
    vpNewLLIData.setAgingDate(mpAgingDateField.getDate());
    vpNewLLIData.setExpirationDate(mpExpirationDateField.getDate());
    
    String vsReasonCode = getReasonCodeFromChoice(
        mpHoldReasonCodeCombo.getSelectedItem().toString());
    try
    {
      int newHoldType = mpHoldTypeCombo.getIntegerValue();

      vpNewLLIData.setHoldType(newHoldType);
      vpNewLLIData.setHoldReason(vsReasonCode);
      vpNewLLIData.setPriorityAllocation(mpPriorityAllocationCombo.getIntegerValue());
    }
    catch (NoSuchFieldException e2)
    {
      displayError("No Such Field: " + e2);
    }
    
    return vpNewLLIData;
  }
  
  /**
   * Method to extract the reason code from the reasonCodeBox.  <b>Note:</b>The
   * reason code choice list has a colon in it to separate the Reason code from
   * a description.
   * @param index -- the index of entry in the combo box.
   */
  protected String getReasonCodeFromChoice(String isReasonChoice)
  {
    String vsReasonCode = "";

    int vnColonPos = isReasonChoice.indexOf(':');
    if (vnColonPos != -1)
      vsReasonCode = isReasonChoice.substring(0, vnColonPos);
  
    return vsReasonCode;
  }
  
  /**
   * Get the reason code for the adjustment and do the update
   * 
   * @param ipLLIData
   */
  protected void getAdjustmentReasonCodeAndUpdateLoadLineItem(final LoadLineItemData ipLLIData)
  {
    // Get the reason code for the adjustment
    ReasonCodeFrame reasonCodeFrame = new ReasonCodeFrame(DBConstants.REASONADJUST, 80);
    String[] vsChoices = reasonCodeFrame.getMsChoices();
    
    if (vsChoices == null || vsChoices.length == 0)
    {
      // There are no reason codes... use blank
      commitChanges(ipLLIData, "");
    }
    else if (vsChoices.length == 1)
    {
      // There is only one reason code... use it
      commitChanges(ipLLIData, getReasonCodeFromChoice(vsChoices[0]));
    }
    else
    {
      // There are more than one reason code... have the user choose one
      
      /*
       * Give the user extra time to pick the reason code... the list is HUGE!
       */
      setTimeout(90);

      addSKDCInternalFrameModal(reasonCodeFrame, new PropertyChangeListener() 
      {
        public void propertyChange(PropertyChangeEvent event) 
        {
          String prop = event.getPropertyName();
          if(prop.equals(FRAME_CHANGE))
          {
            String vsReasonCode = (String) event.getNewValue();
            if (vsReasonCode.trim().length() > 0)
            {
              commitChanges(ipLLIData, vsReasonCode);
            }
            else
            {
              displayError("No reason code selected");
            }
          }
        }
      }); 
    }
  }
  
  /**
   * Actually add the item detail
   * 
   * @param ipLLIData
   * @param isReasonCode
   */
  protected void commitChanges(LoadLineItemData ipLLIData, String isReasonCode)
  {
    try
    {
      mpInvServer.addLoadLIWithValidation(ipLLIData,
          MessageOutNames.INVENTORY_ADJUST, isReasonCode);

      changed(null, ipLLIData);
      displayInfoAutoTimeOut("Item " + ipLLIData.getItem() + " added");
      clearButtonPressed();
    }
    catch (DBException exp)
    {
      logAndDisplayException(exp);
      return;
    }
  }
}
