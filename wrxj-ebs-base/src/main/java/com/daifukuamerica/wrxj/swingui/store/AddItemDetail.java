package com.daifukuamerica.wrxj.swingui.store;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCDateField;
import com.daifukuamerica.wrxj.swing.SKDCDoubleField;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swingui.item.ItemNumberInput;

/**
 * A simple add item detail screen for the Store screen.
 * 
 * @author A.D.
 * @since 16-Apr-2008
 */
@SuppressWarnings("serial")
public class AddItemDetail extends DacInputFrame
{
  protected String msCurrentLoadID;
  protected LoadLineItemData mpLLIData;
  protected StandardInventoryServer mpInvServ = null;
                                       // GUI Components.
  protected ItemNumberInput mpItemText;
  protected SKDCTextField   mpItemDescText;
  protected SKDCTextField   mpLotText;
  protected SKDCTextField   mpLoadText;
  protected SKDCTextField   mpItemPosition;
  protected SKDCDateField   mpExpirationDate;
  protected SKDCDoubleField mpCurrentQty;

  public AddItemDetail(String isCurrentLoadID)
  {
    super("AddItemDetail", "");
    msCurrentLoadID = isCurrentLoadID;
    mpLLIData = Factory.create(LoadLineItemData.class);
    mpInvServ = Factory.create(StandardInventoryServer.class);

    buildScreen();
  }

  @Override
  protected void okButtonPressed()
  {
    try
    {
      String vsItem = mpItemText.getText();
      if (!mpInvServ.itemMasterExists(vsItem))
      {
        displayInfoAutoTimeOut("Item master for \"" + vsItem + "\" added.");
        mpInvServ.addDefaultItem(vsItem);
      }
      mpLLIData.clear();
      mpLLIData.setItem(mpItemText.getText());
      mpLLIData.setLot(mpLotText.getText());
      mpLLIData.setLoadID(msCurrentLoadID);
      mpLLIData.setPositionID(mpItemPosition.getText());
      mpLLIData.setExpirationDate(mpExpirationDate.getDate());
      mpLLIData.setCurrentQuantity(mpCurrentQty.getValue());

      if (mpItemText.getText().trim().length() == 0)
      {
        displayError("The Item must be entered.");
        mpItemText.requestFocus();
      }
      else if (mpCurrentQty.getValue() <= 0)
      {
        displayError("The Add Qty. must be greater than zero");
        mpCurrentQty.requestFocus();
      }
      else
      {
        changed(FRAME_CHANGE, mpLLIData);
        clearButtonPressed();
      }
    }
    catch(DBException ex)
    {
      displayError("Expected Receipt ");
    }
  }

  @Override
  protected void clearButtonPressed()
  {
    mpItemText.setText("");
    mpLotText.setText("");
    mpItemPosition.setText("");
    mpExpirationDate.setDate();
    mpCurrentQty.setValue(0.0);
    mpItemText.requestFocus();
  }

  protected void buildScreen()
  {
    mpItemText = new ItemNumberInput(mpInvServ, true, false);
    mpItemDescText = new SKDCTextField(DBInfo.getFieldLength(ItemMasterData.DESCRIPTION_NAME));
    mpLotText  = new SKDCTextField(DBInfo.getFieldLength(LoadLineItemData.LOT_NAME));
    mpLoadText  = new SKDCTextField(DBInfo.getFieldLength(LoadLineItemData.LOADID_NAME));
    mpItemPosition = new SKDCTextField(DBInfo.getFieldLength(LoadLineItemData.POSITIONID_NAME));
    mpCurrentQty = new SKDCDoubleField(0.0, 10);
    mpExpirationDate = new SKDCDateField();

    addInput("Item", mpItemText);
    addInput("Description", mpItemDescText);
    addInput("Lot", mpLotText);
    addInput("Load ID", mpLoadText);
    addInput("Sub Location", mpItemPosition);
    addInput("Expiration Date", mpExpirationDate);
    addInput("Add Quantity", mpCurrentQty);

    initFieldAttributes();
    useAddButtons();
    mpItemText.requestFocus();
  }

  protected void initFieldAttributes()
  {
    mpItemDescText.setEnabled(false);
    mpItemText.linkDescription(mpItemDescText);
    mpLoadText.setText(msCurrentLoadID);
    mpLoadText.setEnabled(false);
  }
}
