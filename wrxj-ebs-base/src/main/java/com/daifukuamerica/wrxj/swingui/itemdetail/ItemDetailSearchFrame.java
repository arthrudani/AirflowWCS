package com.daifukuamerica.wrxj.swingui.itemdetail;

import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;

/**
 * A screen class for collecting detailed search criteria for a list of
 * load line items.
 *
 * @author avt
 * @version 1.0
 */
public class ItemDetailSearchFrame extends DacInputFrame
{
  private static final long serialVersionUID = 0L;
  
  protected SKDCTextField mpLoadIDText;
  protected SKDCTextField mpItemText;
  protected SKDCTextField mpLotText;
  protected SKDCTranComboBox mpHoldComboBox;
  protected SKDCTranComboBox mpPriorityComboBox;
  protected SKDCTextField mpOrderText;

  /**
   *  Create load line item search frame.
   *
   */
  public ItemDetailSearchFrame()
  {
    super("Item Detail Search", "Item Detail Search Criteria");
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   *  Method to intialize screen components. This adds the components to the
   *  screen and adds listeners as needed.
   *
   *  @exception Exception
   */
  protected void jbInit() throws Exception
  {
    mpLoadIDText = new SKDCTextField(LoadLineItemData.LOADID_NAME);
    mpItemText   = new SKDCTextField(LoadLineItemData.ITEM_NAME);
    mpLotText    = new SKDCTextField(LoadLineItemData.LOT_NAME);
    mpOrderText  = new SKDCTextField(LoadLineItemData.ORDERID_NAME);
    try
    {
      mpPriorityComboBox = new SKDCTranComboBox(LoadLineItemData.PRIORITYALLOCATION_NAME, true);
      mpHoldComboBox     = new SKDCTranComboBox(LoadLineItemData.HOLDTYPE_NAME, true);
    }
    catch (NoSuchFieldException e2)
    {
      e2.printStackTrace(System.out);
    }

    addInput("Load:",     mpLoadIDText);
    addInput("Item:",     mpItemText);
    addInput("Lot:",      mpLotText);
    addInput("Hold:",     mpHoldComboBox);
    addInput("Priority:", mpPriorityComboBox);
    addInput("Order:",    mpOrderText);
    
    useSearchButtons();
  }

  /**
   *  Action method to handle Close button.
   */
  @Override
  protected void closeButtonPressed()
  {
    close();
  }

  /**
   *  Action method to handle Clear button.
   */
  @Override
  protected void clearButtonPressed()
  {
    mpLoadIDText.setText("");
    mpItemText.setText("");
    mpLotText.setText("");
    mpOrderText.setText("");
    mpLoadIDText.requestFocus();
  }

  /**
   *  Action method to handle Search button. Method fires a property change
   *  event so parent frame can refresh its display.
   */
  @Override
  protected void okButtonPressed()
  {
    this.changed();
  }

  /**
   *  Method to get the entered search criteria as a ColumnObject.
   *
   *  @return ColumnObject containing criteria to use in search
   */
  public LoadLineItemData getSearchData()
  {
    LoadLineItemData vpLLIData = Factory.create(LoadLineItemData.class);
    if (mpItemText.getText().trim().length() > 0)
    {
      vpLLIData.setKey(LoadLineItemData.ITEM_NAME, mpItemText.getText(), KeyObject.LIKE);
    }

    if (mpLotText.getText().trim().length() > 0)
    {
      vpLLIData.setKey(LoadLineItemData.LOT_NAME, mpLotText.getText(), KeyObject.LIKE);
    }

    if (mpLoadIDText.getText().trim().length() > 0)
    {
      vpLLIData.setKey(LoadLineItemData.LOADID_NAME, mpLoadIDText.getText(), KeyObject.LIKE);
    }

    if (mpOrderText.getText().trim().length() > 0)
    {
      vpLLIData.setKey(LoadLineItemData.ORDERID_NAME, mpOrderText.getText(), KeyObject.LIKE);
    }

    try
    {
      vpLLIData.setKey(mpHoldComboBox.getTranslationName(), mpHoldComboBox.getIntegerObject());
      vpLLIData.setKey(mpPriorityComboBox.getTranslationName(), mpPriorityComboBox.getIntegerObject());
    }
    catch(NoSuchFieldException e2)
    {
      e2.printStackTrace(System.out);
      vpLLIData = null;
    }

    return(vpLLIData);
  }

}